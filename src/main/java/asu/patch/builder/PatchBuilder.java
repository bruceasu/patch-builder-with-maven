/*
 * Copyright (c) 2017 Suk Honzeon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package asu.patch.builder;

import asu.patch.builder.svn.SVNUtils;
import asu.patch.builder.task.CompilePatchTask;
import asu.patch.builder.task.CopyTask;
import asu.patch.builder.task.MvnTask;
import asu.patch.builder.task.SvnExportChangeFilesTask;
import asu.patch.builder.task.SvnExportTask;
import asu.patch.builder.task.TreeTask;
import java.awt.*;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * ${document}
 * <p>2017 Victor All rights reserved.</p>
 *
 * @author <a href="mailto:victor.su@gwtsz.net">Victor Su&lt;victor.su@gwtsz.net&gt;</a>
 * @version 1.0.0
 * @since 2017/3/29 14:06
 */
public class PatchBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(PatchBuilder.class);

  public static void main(String[] args) throws SVNException, IOException {
    ApplicationConfig config = ApplicationConfig.getInstance();
    // PropertyConfigurator.configure("D:/workspaces/patch-builder-with-maven/src/main/resources/log4j.properties");
    String home = config.getAppHome();
    LOGGER.info("home = " + home);
    System.setProperty("my.home.dir", home);

    String svn = config.get("svn.url");
    SVNURL repositoryURL = null;
    try {
      repositoryURL = SVNURL.parseURIEncoded(svn);
    } catch (SVNException e) {
      LOGGER.error("", e);
      System.exit(1);
    }
    String name = config.get("svn.username");
    String password = config.get("svn.password");
    SVNClientManager ourClientManager = SVNUtils.create(name, password);
    /* show the head version */
    SVNUtils.showInfo(ourClientManager, repositoryURL, SVNRevision.HEAD, SVNRevision.HEAD, false);

    String revisionStart = prompt("revision from");
    String revisionEnd = prompt("revision to");
    /* create directories*/
    WorkspaceCreator workspaceCreator = new WorkspaceCreator().invoke();
    File allCodeDir = workspaceCreator.getAllCodeDir();
    File patchDir = workspaceCreator.getPatchDir();
    File patchOutputDir = workspaceCreator.getPatchOutputDir();

    /* export files from svn. */
    doSvnExport(repositoryURL, ourClientManager, revisionEnd, allCodeDir);
    /* maven compile all codes for patch classpath */
    doMaven(config, allCodeDir);
    /* export change files */
    doChangeFilesExport(svn, name, password, revisionStart, revisionEnd, patchDir, patchOutputDir);
    /* compile patch */
    doCompilePatch(allCodeDir, patchDir, patchOutputDir);
    /* copy resources */
    doCopyResources(patchDir, patchOutputDir);
    /* copy webapp */
    doCopyWebapp(patchDir, patchOutputDir);

    String tree = new TreeTask(patchOutputDir.getAbsolutePath()).exec();
    Files.write(new File(patchOutputDir, "tree.txt"), tree);
  }

  private static void doChangeFilesExport(String svn, String name, String password, String revisionStart, String revisionEnd, File patchDir, File patchOutputDir) {
    new SvnExportChangeFilesTask(svn, name, password, revisionStart, revisionEnd, patchDir)
        .exec();
    /* copy remove script */
    File file1 = new File(patchDir, "removes.sh");
    File file2 = new File(patchDir, "removes.bat");
    if (file1.exists()) {
      file1.renameTo(new File(patchOutputDir, file1.getName()));
    }
    if (file2.exists()) {
      file2.renameTo(new File(patchOutputDir, file2.getName()));
    }
  }

  private static void doSvnExport(SVNURL repositoryURL,
                                  SVNClientManager ourClientManager,
                                  String revisionEnd,
                                  File allCodeDir) throws SVNException {
    ApplicationConfig config = ApplicationConfig.getInstance();
    boolean isSkipExport = config.getBoolean("svn.skip.export-all-code", false);
    if (!isSkipExport) {
      new SvnExportTask(ourClientManager, repositoryURL, revisionEnd, allCodeDir).exec();
    }
  }

  private static void doCopyWebapp(File patchDir, File patchOutputDir) throws IOException {
    ApplicationConfig config = ApplicationConfig.getInstance();
    boolean isWebApp = config.getBoolean("isWebApp", true);
    if (isWebApp) {
      LOGGER.info("process static files for web");
      File webapp = new File(patchDir, config.get("webapp.path", "src/main/webapp"));
      Files.copyDir(webapp, patchOutputDir);
    }
  }

  private static void doMaven(ApplicationConfig config, File allCodeDir) throws IOException {
    String pom = new File(allCodeDir, "pom.xml").getAbsolutePath();
    String mvn = config.get("mvn", "mvn");
    String args = config.get("mvn.args");
    if (Strings.isBlank(args)) {
      new MvnTask(mvn, pom).exec();
    } else {
      String[] split = args.split("\\s+");
      new MvnTask(mvn, pom, split).exec();
    }
  }

  private static void doCopyResources(File patchDir, File patchOutputDir) throws IOException {
    ApplicationConfig config = ApplicationConfig.getInstance();
    boolean isWebApp = config.getBoolean("isWebApp", true);
    File outputResourcesDir;
    if (isWebApp) {
      outputResourcesDir = new File(patchOutputDir, "WEB-INF/classes");
      if (!outputResourcesDir.exists() && !outputResourcesDir.isDirectory()) {
        outputResourcesDir.mkdirs();
      }
    } else {
      outputResourcesDir = new File(patchOutputDir, "conf");
    }

    String pathes = config.get("resources.path", "src/main/resources");
    String[] split = pathes.split(":");
    File[] files = new File[split.length];
    for (int i = 0; i < files.length; i++) {
      files[i] = new File(patchDir, split[i]);
    }
    new CopyTask(outputResourcesDir, files).exec();
  }

  private static void doCompilePatch(File allCodeDir, File patchDir, File patchOutputDir) throws IOException {
    ApplicationConfig config = ApplicationConfig.getInstance();
    boolean isWebApp = config.getBoolean("isWebApp", true);
    File outputClassesDir;
    if (isWebApp) {
      outputClassesDir = new File(patchOutputDir, "WEB-INF/classes");
      if (!outputClassesDir.exists() && !outputClassesDir.isDirectory()) {
        outputClassesDir.mkdirs();
      }
    } else {
      outputClassesDir = new File(patchOutputDir, "classes");
      if (!outputClassesDir.exists() && !outputClassesDir.isDirectory()) {
        outputClassesDir.mkdirs();
      }
    }
    new CompilePatchTask(patchDir, outputClassesDir, allCodeDir).exec();
  }

  private static String prompt(String prompt) {
    String result = null;
    while (Strings.isBlank(result)) {
      try {
        result = JOptionPane.showInputDialog(null, prompt);
      } catch (HeadlessException ex) {
        Console console = System.console();
        if (console != null) {
          result = console.readLine(prompt);
        } else {
          System.out.printf("%s: ", prompt);
        }
      }
    }

    return result;
  }

  private static class WorkspaceCreator {
    private File patchDir;
    private File patchOutputDir;
    private File allCodeDir;

    public File getPatchDir() {
      return patchDir;
    }

    public File getPatchOutputDir() {
      return patchOutputDir;
    }

    public File getAllCodeDir() {
      return allCodeDir;
    }

    public WorkspaceCreator invoke() {
      ApplicationConfig config = ApplicationConfig.getInstance();
      String workspace = config.get("svn.workspace");
      patchDir = new File(workspace, "patch");
      patchOutputDir = new File(workspace, "build");
      allCodeDir = new File(workspace, "all-code");
      String allCodePath = config.get("svn.export.all-code-path");
      if (Strings.isNotBlank(allCodePath)) {
        File file = new File(allCodePath);
        if (file.isDirectory() && new File(file, "pom.xml").exists()) {
          allCodeDir = file;
        }
      }
      if (!allCodeDir.exists()) {
        allCodeDir.mkdirs();
      }
      if (!patchDir.exists()) {
        patchDir.mkdirs();
      }
      if (!patchOutputDir.exists()) {
        patchOutputDir.mkdirs();
      }
      return this;
    }
  }
}

/*
  -g                         生成所有调试信息
  -g:none                    不生成任何调试信息
  -g:{lines,vars,source}     只生成某些调试信息
  -nowarn                    不生成任何警告
  -verbose                   输出有关编译器正在执行的操作的消息
  -deprecation               输出使用已过时的 API 的源位置
  -classpath <路径>            指定查找用户类文件和注释处理程序的位置
  -cp <路径>                   指定查找用户类文件和注释处理程序的位置
  -sourcepath <路径>           指定查找输入源文件的位置
  -bootclasspath <路径>        覆盖引导类文件的位置
  -extdirs <目录>              覆盖所安装扩展的位置
  -endorseddirs <目录>         覆盖签名的标准路径的位置
  -proc:{none,only}          控制是否执行注释处理和/或编译。
  -processor <class1>[,<class2>,<class3>...] 要运行的注释处理程序的名称; 绕过默认的
  -processorpath <路径>        指定查找注释处理程序的位置
  -parameters                生成元数据以用于方法参数的反射
  -d <目录>                    指定放置生成的类文件的位置
  -s <目录>                    指定放置生成的源文件的位置
  -h <目录>                    指定放置生成的本机标头文件的位置
  -implicit:{none,class}     指定是否为隐式引用文件生成类文件
  -encoding <编码>             指定源文件使用的字符编码
  -source <发行版>              提供与指定发行版的源兼容性
  -target <发行版>              生成特定 VM 版本的类文件
  -profile <配置文件>            请确保使用的 API 在指定的配置文件中可用
  -version                   版本信息
  -help                      输出标准选项的提要
  -A关键字[=值]                  传递给注释处理程序的选项
  -X                         输出非标准选项的提要
  -J<标记>                     直接将 <标记> 传递给运行时系统
  -Werror                    出现警告时终止编译
  @<文件名>                     从文件读取选项和文件名
 */
