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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.nutz.lang.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by suk on 2017/3/29.
 */
public class CopyTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(CopyTask.class);
  private File dest;
  private File[] src;

  public CopyTask(String dest, String... src) throws IOException {
    File[] files = new File[src.length];
    for (int i = 0; i < src.length; i++) {
      files[i] = new File(src[i]);
    }
    this.dest = new File(dest);
    this.src = files;
  }

  public CopyTask(String dest, List<String> src) throws IOException {
    File[] files = new File[src.size()];
    for (int i = 0, j= src.size(); i < j; i++) {
      files[i] = new File(src.get(i));
    }
    this.dest = new File(dest);
    this.src = files;
  }
  public CopyTask(File dest, File... src) throws IOException {
    this.dest = dest;
    this.src = src;
  }

  public CopyTask(File dest, List<File> src) throws IOException {
    this.dest = dest;
    this.src = src.toArray(new File[0]);
  }

  public void exec() throws IOException {
    for (File file : src) {
      if (file.isFile()) {
        Files.copy(file, dest);
      } else {
        List<String> files = makeResListFile(file);
        if (!files.isEmpty()) {
          LOGGER.info("going to copy " + files.size() + " to " + dest);
          for (String subFile : files) {
            Files.copy(new File(file, subFile), new File(dest, subFile));
          }
        }
      }
    }
  }

  private List<String> makeResListFile(File resDir) throws IOException {
    LinkedList<File> dirs = new LinkedList<>();
    ArrayList<String> files = new ArrayList<>();
    dirs.add(resDir);
    int resDirNameLength = resDir.getAbsolutePath().length();
    while (!dirs.isEmpty()) {
      File dir = dirs.remove();
      File[] sub = dir.listFiles();
      if (sub != null && sub.length > 0) {
        for (File f : sub) {
          if (f.isFile()) {
            if (!f.getName().endsWith(".java")) {
              files.add(f.getAbsolutePath().substring(resDirNameLength + 1));
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
}
