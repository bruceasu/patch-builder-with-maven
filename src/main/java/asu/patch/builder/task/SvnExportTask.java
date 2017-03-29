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

import asu.patch.builder.svn.SVNUtils;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * Created by suk on 2017/3/30.
 */
public class SvnExportTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(SvnExportTask.class);
  private SVNClientManager ourClientManager;
  private SVNURL repositoryURL;
  private String revisionEnd;
  private File allCodeDir;

  public SvnExportTask(SVNClientManager ourClientManager,
                       SVNURL repositoryURL,
                       String revisionEnd,
                       File allCodeDir) {
    this.ourClientManager = ourClientManager;
    this.repositoryURL = repositoryURL;
    this.revisionEnd = revisionEnd;
    this.allCodeDir = allCodeDir;
  }

  public void exec() throws SVNException {
    long export = SVNUtils.export(ourClientManager, repositoryURL,
        SVNRevision.parse(revisionEnd), allCodeDir);
    LOGGER.info("export = " + export);
  }
}
