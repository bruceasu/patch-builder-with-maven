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
import asu.patch.builder.util.Shell;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.PropertyConfigurator;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;

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
  private static final PropertiesProxy pp = new PropertiesProxy("D:\\workspaces\\patch-builder-with-maven\\src\\main\\resources");
  public static void main(String[] args) throws SVNException, IOException {
    PropertyConfigurator.configure(pp.get("log4j.file"));
    String home = "D:\\workspaces\\patch-builder-with-maven\\assembly";
    System.setProperty("my.home.dir", home);
    String svn = "file:///d:/tmp/test";
    String revisionStart = "582";
    String revisionEnd = "663";
    String workspace = "d:/tmp/svn-test";
    File allCodeDir = new File(workspace, "all-code");
    File patchDir = new File(workspace, "patch");
    File patchOutputDir = new File(workspace, "build");
    String mvn = "C:\\programs\\apache-maven-3.3.9\\bin\\mvn.cmd";
    SVNURL repositoryURL = null;
    try {
      repositoryURL = SVNURL.parseURIEncoded(svn);
    } catch (SVNException e) {
      LOGGER.error("", e);
      System.exit(1);
    }
    String name = pp.get("svn.username");
    String password = pp.get("svn.password");
    SVNClientManager ourClientManager = SVNUtils.create(name, password);
//    long export = SVNUtils.export(ourClientManager, repositoryURL, SVNRevision.parse(revisionEnd), allCodeDir);
//    System.out.println("export = " + export);

//    System.out.println("export patch:");
//    SVNChangedFiles svnChangedFiles = new SVNChangedFiles(svn, name, password,
//        Long.valueOf(revisionStart), Long.valueOf(revisionEnd), patchDir);
//    svnChangedFiles.export();

//    System.out.println("call maven:");
//    String s = Shell.execCommand(mvn, "-f", new File(allCodeDir, "pom.xml").getAbsolutePath(), "clean", "compile");
//    System.out.println(s);
//    s = Shell.execCommand(mvn, "-f", new File(allCodeDir, "pom.xml").getAbsolutePath(), "dependency:copy-dependencies", "-DoutputDirectory=target/lib");
//    System.out.println(s);
    boolean isWebApp = true;
    File outputClassesDir, outputResourcesDir;
    if (isWebApp) {
      outputClassesDir = new File(patchOutputDir, "WEB-INF/classes");
      if (!outputClassesDir.exists() && !outputClassesDir.isDirectory()) {
        outputClassesDir.mkdirs();
      }
      outputResourcesDir = outputClassesDir;
    } else {
      outputClassesDir = new File(patchOutputDir, "classes");
      if (!outputClassesDir.exists() && !outputClassesDir.isDirectory()) {
        outputClassesDir.mkdirs();
      }
      outputResourcesDir = new File(patchDir, "conf");
    }
    compilePatch(allCodeDir, patchDir, outputClassesDir);
    // process resources
    processResources(patchDir, outputResourcesDir);
    if (isWebApp) {
      LOGGER.info("process static files for web");
      File webapp = new File(patchDir, "src/main/webapp");
      Files.copyDir(webapp, patchOutputDir);
    }
  }

  private static void processResources(File patchDir, File outputDir) throws IOException {
    File resDir = new File(patchDir, "src/main/resources");
    List<String> files = makeResListFile(resDir);
    if (!files.isEmpty()) {
      LOGGER.info("going to copy " + files.size() + " to " + outputDir);
      for (String file : files) {
        Files.copy(new File(resDir, file), new File(outputDir, file));
      }
    }
  }

  private static void compilePatch(File allCodeDir, File patchDir, File outputClassesDir) throws IOException {
    LOGGER.info("compile patch:");
    File patchSrcDir = new File(patchDir, "src/main/java");
    List<String> files = makeSrcListFile(patchSrcDir);
    LOGGER.info("going to compile " + files.size() + " to " + outputClassesDir.getAbsolutePath());
    File tmpFile = File.createTempFile("src-", ".list");
    tmpFile.deleteOnExit();
    Files.write(tmpFile, join("\n", files));
    LOGGER.debug("write source files list to " + tmpFile);
    LOGGER.info("going to compile files: \n" + join(File.pathSeparator, files));
    String compiler = pp.get("javac");;
    String[] cmd = Lang.array(compiler,
        "-cp", join(File.pathSeparator,
            new File(allCodeDir, "target/lib/*").getAbsolutePath(),
            new File(allCodeDir, "target/classes").getAbsolutePath(),
            new File(allCodeDir, "src/main/webapp/WEB-INF/lib/*").getAbsolutePath()),
        "-sourcepath", patchSrcDir.getAbsolutePath(),
        "-s", patchSrcDir.getAbsolutePath(),
        "-d", outputClassesDir.getAbsolutePath(),
        "-encoding", "UTF-8",
        "-source", "1.8",
        "-target", "1.8",
        "@" + tmpFile);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("cmd = " + join(" ", cmd));
    }
    Shell.ShellCommandExecutor shellCommandExecutor = new Shell.ShellCommandExecutor(cmd, patchSrcDir);
    shellCommandExecutor.execute();
    int exitCode = shellCommandExecutor.getExitCode();
    String compileResult = shellCommandExecutor.getOutput();
    LOGGER.info("CompileResult {exitCode: " + exitCode + ", message:" + compileResult + "}");
  }

  private static List<String> makeSrcListFile(File srcDir) throws IOException {
    LinkedList<File> dirs = new LinkedList<>();
    ArrayList<String> files = new ArrayList<>();
    dirs.add(srcDir);
    while (!dirs.isEmpty()) {
      File dir = dirs.remove();
      File[] sub = dir.listFiles();
      if (sub != null && sub.length > 0) {
        for (File f : sub) {
          if (f.isFile()) {
            files.add(f.getAbsolutePath().substring(srcDir.getAbsolutePath().length() + 1));
          } else if (f.isDirectory()) {
            if (f.getName().equals(".") || f.getName().equals("..")) {
              continue;
            }
            dirs.add(f);
          }
        }
      }
    }

    return files;
  }

  private static List<String> makeResListFile(File resDir) throws IOException {
    LinkedList<File> dirs = new LinkedList<>();
    ArrayList<String> files = new ArrayList<>();
    dirs.add(resDir);
    while (!dirs.isEmpty()) {
      File dir = dirs.remove();
      File[] sub = dir.listFiles();
      if (sub != null && sub.length > 0) {
        for (File f : sub) {
          if (f.isFile()) {
            if (!f.getName().endsWith(".java")) {
              files.add(f.getAbsolutePath().substring(resDir.getAbsolutePath().length() + 1));
            }
          } else if (f.isDirectory()) {
            if (f.getName().equals(".") || f.getName().equals("..")) {
              continue;
            }
            dirs.add(f);
          }
        }
      }

    }
    return files;
  }


  /**
   * Concatenates strings, using a separator.
   *
   * @param separator to join with
   * @param strings   to join
   * @return the joined string
   */
  private static String join(CharSequence separator, String... strings) {
    return join(separator, Arrays.asList(strings));
  }

  private static String join(CharSequence separator, Collection<String> collection) {
    // Ideally we don't have to duplicate the code here if array is iterable.
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (String s : collection) {
      if (first) {
        first = false;
      } else {
        sb.append(separator);
      }
      sb.append(s);
    }
    return sb.toString();
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
