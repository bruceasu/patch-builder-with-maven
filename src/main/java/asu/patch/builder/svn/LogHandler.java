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

package asu.patch.builder.svn;

import java.util.Iterator;
import java.util.Map;
import org.nutz.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

/**
 * Created by suk on 2017/3/16.
 */
public class LogHandler implements ISVNLogEntryHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(LogHandler.class);

  @Override
  public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
    if (logEntry == null) {
      return;
    }
    StringBuffer result = new StringBuffer();
    result.append("\n").append(Strings.dup('=', 30)).append("LOG").append(Strings.dup('=', 30))
        .append("\nr").append(logEntry.getRevision()).append('\t').append(logEntry.getDate())
        .append('\t').append(logEntry.getAuthor()).append('\n').append(logEntry.getMessage());

    Map<String, SVNLogEntryPath> changedPaths = logEntry.getChangedPaths();
    if (changedPaths != null && !changedPaths.isEmpty()) {
      for (Iterator paths = changedPaths.values().iterator(); paths.hasNext(); ) {
        result.append('\n');
        SVNLogEntryPath path = (SVNLogEntryPath) paths.next();
        result.append(path.toString());
      }
    }
    LOGGER.info(result.toString());
  }
}

