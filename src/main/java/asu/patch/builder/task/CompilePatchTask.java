/*
 * Copyright (C) 2017 Bruce Asu<bruceasu@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions:
 *  　　
 * 　　The above copyright notice and this permission notice shall
 * be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package asu.patch.builder.task;

import static asu.patch.builder.util.Strings.join;

import asu.patch.builder.ApplicationConfig;
import asu.patch.builder.util.Shell;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by suk on 2017/3/30.
 */
public class CompilePatchTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(CompilePatchTask.class);
  private File patchDir;
  private File outputClassesDir;
  private File allCodeDir;

  public CompilePatchTask(File patchDir, File outputClassesDir, File allCodeDir) {
    this.patchDir = patchDir;
    this.outputClassesDir = outputClassesDir;
    this.allCodeDir = allCodeDir;
  }

  public void exec() throws IOException {
    ApplicationConfig config = ApplicationConfig.getInstance();
    LOGGER.info("compile patch:");
    String srcDir = config.get("source.path");
    File patchSrcDir = new File(patchDir, srcDir);
    List<String> files = makeSrcListFile(patchSrcDir);
    LOGGER.info("going to compile " + files.size() + " to " + outputClassesDir.getAbsolutePath());
    File tmpFile = File.createTempFile("src-", ".list");
    tmpFile.deleteOnExit();
    Files.write(tmpFile, join("\n", files));
    LOGGER.debug("write source files list to " + tmpFile);
    LOGGER.info("going to compile files: \n" + join(File.pathSeparator, files));
    String compiler = config.get("javac");
    List<String> classpath = new ArrayList<>();
    String cl = config.get("compile.classpath");
    if (Strings.isNotBlank(cl)) {
      String[] split = cl.split(":");
      for (String s : split) {
        classpath.add(new File(allCodeDir, s).getAbsolutePath());
      }
    }
    cl = config.get("compile.classpath.ext");
    if (Strings.isNotBlank(cl)) {
      String[] split = cl.split(":");
      for (String s : split) {
        classpath.add(s);
      }
    }
    String[] cmd = Lang.array(compiler,
        "-cp", join(File.pathSeparator, classpath),
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


  private List<String> makeSrcListFile(File srcDir) throws IOException {
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

}
