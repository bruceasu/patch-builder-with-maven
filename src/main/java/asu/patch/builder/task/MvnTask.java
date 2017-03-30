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

import asu.patch.builder.util.Shell;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by suk on 2017/3/30.
 */
public class MvnTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(MvnTask.class);

  private final String[] args;
  private final String mvn;
  private final String pom;

  public MvnTask(String mvn, String pom, String... args) {
    this.mvn = mvn;
    this.pom = pom;
    this.args = args;
  }

  public void exec() throws IOException {
    LOGGER.info("call maven:");

    List<String> cmds = new LinkedList<>();
    cmds.add(mvn);
    if (args != null && args.length > 0) {
      for (String s : args) {
        cmds.add(s);
      }
    }
    cmds.add("-f");
    cmds.add(pom);
    cmds.add("clean");
    cmds.add("compile");
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("execute mvn: " + cmds);
    }
    String s = Shell.execCommand(cmds.toArray(new String[0]));
    LOGGER.info(s);
    LOGGER.info("export the dependencies jars...");
    s = Shell.execCommand(mvn, "-f", pom, "dependency:copy-dependencies",
        "-DoutputDirectory=target/lib");
    LOGGER.info(new String(s.getBytes("UTF-8"), "GBK"));
  }
}
