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

import asu.patch.builder.svn.SVNChangedFiles;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by suk on 2017/3/30.
 */
public class SvnExportChangeFilesTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(SvnExportChangeFilesTask.class);
  private final String svn;
  private final String password;
  private final String name;
  private String revisionStart;
  private String revisionEnd;
  private File patchDir;

  public SvnExportChangeFilesTask(String svn,
                                  String name,
                                  String password,
                                  String revisionStart,
                                  String revisionEnd,
                                  File patchDir) {
    this.svn = svn;
    this.name = name;
    this.password = password;
    this.revisionStart = revisionStart;
    this.revisionEnd = revisionEnd;
    this.patchDir = patchDir;

  }

  public void exec() {
    LOGGER.info("export patch:");
    SVNChangedFiles svnChangedFiles = new SVNChangedFiles(svn, name, password, Long.valueOf(revisionStart), Long.valueOf(revisionEnd), patchDir);
    svnChangedFiles.export();
  }
}
