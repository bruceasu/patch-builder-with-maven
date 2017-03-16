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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.wc.ISVNInfoHandler;
import org.tmatesoft.svn.core.wc.SVNInfo;



/**
 * An implementation of ISVNInfoHandler that is  used  in  WorkingCopy.java  to
 * display  info  on  a  working  copy path.  This implementation is passed  to
 *
 * <p>SVNWCClient.doInfo(File path, SVNRevision revision, boolean recursive,
 * ISVNInfoHandler handler)
 *
 * <p> For each item to be processed doInfo(..) collects information and creates an
 * SVNInfo which keeps that information. Then  doInfo(..)  calls  implementor's
 * handler.handleInfo(SVNInfo) where it passes the gathered info.
 */
public class InfoHandler implements ISVNInfoHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(InfoHandler.class);

  /**
   * This is an implementation  of  ISVNInfoHandler.handleInfo(SVNInfo info).
   * Just prints out information on a Working Copy path in the manner of  the
   * native SVN command line client.
   */
  public void handleInfo(SVNInfo info) {
    if (info == null) {
      return;
    }
    LOGGER.info("-----------------INFO-----------------");
    LOGGER.info("Local Path: {}", info.getURL().getPath());
    LOGGER.info("URL: {}", info.getURL());
    if (info.isRemote() && info.getRepositoryRootURL() != null) {
      LOGGER.info("Repository Root URL: {}", info.getRepositoryRootURL());
    }
    if (info.getRepositoryUUID() != null) {
      LOGGER.info("Repository UUID: {}", info.getRepositoryUUID());
    }
    LOGGER.info("Revision: {}", info.getRevision().getNumber());
    LOGGER.info("Node Kind: {}", info.getKind().toString());
    if (!info.isRemote()) {
      LOGGER.info("Schedule: "
          + (info.getSchedule() != null ? info.getSchedule() : "normal"));
    }
    LOGGER.info("Last Changed Author: {}", info.getAuthor());
    LOGGER.info("Last Changed Revision: {}", info.getCommittedRevision().getNumber());
    LOGGER.info("Last Changed Date: {}", info.getCommittedDate());
    if (info.getPropTime() != null) {
      LOGGER.info("Properties Last Updated: {}", info.getPropTime());
    }
    if (info.getKind() == SVNNodeKind.FILE && info.getChecksum() != null) {
      if (info.getTextTime() != null) {
        LOGGER.info("Text Last Updated: {}", info.getTextTime());
      }
      LOGGER.info("Checksum: " + info.getChecksum());
    }
    if (info.getLock() != null) {
      if (info.getLock().getID() != null) {
        LOGGER.info("Lock Token: {}", info.getLock().getID());
      }
      LOGGER.info("Lock Owner: {}", info.getLock().getOwner());
      LOGGER.info("Lock Created: {}", info.getLock().getCreationDate());
      if (info.getLock().getExpirationDate() != null) {
        LOGGER.info("Lock Expires: {}", info.getLock().getExpirationDate());
      }
      if (info.getLock().getComment() != null) {
        LOGGER.info("Lock Comment: {}", info.getLock().getComment());
      }
    }
  }
}