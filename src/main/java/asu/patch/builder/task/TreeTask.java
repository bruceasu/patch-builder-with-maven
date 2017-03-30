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

package asu.patch.builder.task;

import asu.patch.builder.util.Shell;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.nutz.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ${document}
 * <p>2017 Victor All rights reserved.</p>
 *
 * @author <a href="mailto:victor.su@gwtsz.net">Victor Su&lt;victor.su@gwtsz.net&gt;</a>
 * @version 1.0.0
 * @since 2017/3/30 10:38
 */
public class TreeTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(TreeTask.class);
  private final String dir;

  public  TreeTask(String dir) {
    if (Strings.isBlank(dir)) {
      this.dir = ".";
    } else {
      this.dir = dir;
    }
  }

  public String  exec() throws IOException {
    List<String> cmds = new ArrayList<>();

    if (Shell.WINDOWS) {
      cmds.add("cmd.exe");
      cmds.add("/c");
      cmds.add("tree");
      cmds.add("/f");
    } else {
      cmds.add("tree");
      cmds.add("-A");
    }
    cmds.add(dir);
    return Shell.execCommand(cmds.toArray(new String[0]));
  }
}
