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
 */
package asu.patch.builder.svn;

import java.io.File;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.*;

/**
 * Created by suk on 2017/3/15.
 */
public class SVNUtils {
  private static boolean initial = false;

  static {
    setupLibrary();

  }

  /*
   * Initializes the library to work with a repository via
   * different protocols.
   */
  synchronized public static void setupLibrary() {
    if (initial) {
      return;
    }
    /*
     * For using over http:// and https://
     */
    DAVRepositoryFactory.setup();
    /*
     * For using over svn:// and svn+xxx://
     */
    SVNRepositoryFactoryImpl.setup();

    /*
     * For using over file:///
     */
    FSRepositoryFactory.setup();

    initial =  true;
  }
  public static SVNClientManager create(String username, String password) {
    /*
     * Creates a default run-time configuration options driver. Default options
     * created in this way use the Subversion run-time configuration area (for
     * instance, on a Windows platform it can be found in the '%APPDATA%\Subversion'
     * directory).
     *
     * readonly = true - not to save  any configuration changes that can be done
     * during the program run to a config file (config settings will only
     * be read to initialize; to enable changes the readonly flag should be set
     * to false).
     *
     * SVNWCUtil is a utility class that creates a default options driver.
     */
    DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
    SVNClientManager ourClientManager = SVNClientManager.newInstance(options, username, password);
    return ourClientManager;
  }
  public static SVNClientManager create(ISVNAuthenticationManager authManager) {
    /*
     * Creates a default run-time configuration options driver. Default options
     * created in this way use the Subversion run-time configuration area (for
     * instance, on a Windows platform it can be found in the '%APPDATA%\Subversion'
     * directory).
     *
     * readonly = true - not to save  any configuration changes that can be done
     * during the program run to a config file (config settings will only
     * be read to initialize; to enable changes the readonly flag should be set
     * to false).
     *
     * SVNWCUtil is a utility class that creates a default options driver.
     */
    DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
    SVNClientManager ourClientManager = SVNClientManager.newInstance(options, authManager);
    return ourClientManager;
  }

  /**
   * Creates a new version controlled directory (doesn't create any intermediate
   * directories) right in a repository. Like 'svn mkdir URL -m "some comment"'
   * command. It's done by invoking
   */
  public static SVNCommitInfo makeDirectory(SVNClientManager ourClientManager,
                                            SVNURL url,
                                            String commitMessage) throws SVNException {
        /*
         * Returns SVNCommitInfo containing information on the new revision committed
         * (revision number, etc.)
         */
    return ourClientManager.getCommitClient().doMkDir(new SVNURL[]{url}, commitMessage);
  }

  /**
   * Imports an unversioned directory into a repository location denoted by a
   * destination URL (all necessary parent non-existent paths will be created
   * automatically). This operation commits the repository to a new revision.
   * Like 'svn import PATH URL (-N) -m "some comment"' command. It's done by
   * invoking
   */
  public static SVNCommitInfo importDirectory(
      SVNClientManager ourClientManager, File localPath, SVNURL dstURL, String commitMessage,
      boolean isRecursive) throws SVNException {
    /*
     * Returns SVNCommitInfo containing information on the new revision committed
     * (revision number, etc.)
     */
    return ourClientManager.getCommitClient().doImport(localPath, dstURL, commitMessage, null,
        true, true, SVNDepth.fromRecurse(isRecursive));

  }


  /**
   * Checks out a working copy from a repository. Like 'svn checkout URL[@REV] PATH (-r..)'
   * command; It's done by invoking
   */
  public static long checkout(SVNClientManager ourClientManager,
                               SVNURL url,
                              SVNRevision revision,
                              File destPath,
                              boolean isRecursive)
      throws SVNException {
    SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
    /*
     * sets externals not to be ignored during the checkout
     */
    updateClient.setIgnoreExternals(false);
    /*
     * returns the number of the revision at which the working copy is
     */
    return updateClient.doCheckout(url, destPath, revision, revision,
        SVNDepth.fromRecurse(isRecursive), false);
  }

  /**
   * Collects information on local path(s). Like 'svn info (-R)' command.
   * It's done by invoking
   */
  public static void showInfo(SVNClientManager ourClientManager,
                              File wcPath,
                              SVNRevision pegRevision,
                              SVNRevision revision,
                              boolean isRecursive) throws SVNException {
    /*
     * InfoHandler displays information for each entry in the console (in the manner of
     * the native Subversion command line client)
     */
    if (pegRevision == null) {
      pegRevision = SVNRevision.UNDEFINED;
    }
    if (revision == null) {
      revision = SVNRevision.HEAD;
    }
    ourClientManager.getWCClient().doInfo(wcPath,  pegRevision, revision,
        SVNDepth.getInfinityOrEmptyDepth(isRecursive), null, new InfoHandler());
  }
  /**
   * Collects information on local path(s). Like 'svn info (-R)' command.
   * It's done by invoking
   */
  public static void showInfo(SVNClientManager ourClientManager,
                              SVNURL url,
                              SVNRevision pegRevision,
                              SVNRevision revision,
                              boolean isRecursive) throws SVNException {
    /*
     * InfoHandler displays information for each entry in the console (in the manner of
     * the native Subversion command line client)
     */
    if (pegRevision == null) {
      pegRevision = SVNRevision.UNDEFINED;
    }
    if (revision == null) {
      revision = SVNRevision.HEAD;
    }
    ourClientManager.getWCClient().doInfo(url, pegRevision, revision,
        SVNDepth.getInfinityOrEmptyDepth(isRecursive), new InfoHandler());
  }
  /**
   * Puts directories and files under version control scheduling them for addition
   * to a repository. They will be added in a next commit. Like 'svn add PATH'
   * command. It's done by invoking
   */
  public static void addEntry(SVNClientManager ourClientManager, File wcPath) throws SVNException {
    ourClientManager.getWCClient().doAdd(new File[]{wcPath}, false, true, false,
        SVNDepth.fromRecurse(true), false, false, true);
  }

  /**
   * Collects status information on local path(s). Like 'svn status (-u) (-N)'
   * command. It's done by invoking
   */
  public static long showStatus(SVNClientManager ourClientManager,
                                File wcPath,
                                boolean isRecursive,
                                boolean isRemote,
                                boolean isReportAll,
                                boolean isIncludeIgnored,
                                boolean isCollectParentExternals)
      throws SVNException {
    /*
     * StatusHandler displays status information for each entry in the console (in the
     * manner of the native Subversion command line client)
     */
    return ourClientManager.getStatusClient().doStatus(wcPath, SVNRevision.HEAD,
        SVNDepth.fromRecurse(isRecursive), isRemote, isReportAll, isIncludeIgnored,
        isCollectParentExternals, new StatusHandler(isRemote), null);
  }

  /**
  * Updates a working copy (brings changes from the repository into the working copy).
  * Like 'svn update PATH' command; It's done by invoking
  *
  */
  public static long update(SVNClientManager ourClientManager,
                            File wcPath,
                            SVNRevision updateToRevision,
                            boolean isRecursive)
      throws SVNException {

    SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
    /*
     * sets externals not to be ignored during the update
     */
    updateClient.setIgnoreExternals(false);
    /*
     * returns the number of the revision wcPath was updated to
     */
    return updateClient.doUpdate(wcPath, updateToRevision, SVNDepth.fromRecurse(isRecursive),
        false, false);
  }

  /**
   * Committs changes in a working copy to a repository. Like
   * 'svn commit PATH -m "some comment"' command. It's done by invoking
   */
  public static SVNCommitInfo commit(SVNClientManager ourClientManager,
                                     File wcPath,
                                     boolean keepLocks,
                                     String commitMessage)
      throws SVNException {
    /*
     * Returns SVNCommitInfo containing information on the new revision committed
     * (revision number, etc.)
     */
    return ourClientManager.getCommitClient().doCommit(new File[]{wcPath}, keepLocks,
        commitMessage, null, null, false, false, SVNDepth.fromRecurse(true));
  }


  /**
   * Locks working copy paths, so that no other user can commit changes to them.
   * Like 'svn lock PATH' command. It's done by invoking
   */
  public static void lock(SVNClientManager ourClientManager,
                          File wcPath,
                          boolean isStealLock,
                          String lockComment) throws SVNException {
    ourClientManager.getWCClient().doLock(new File[]{wcPath}, isStealLock, lockComment);
  }


  /**
   * Duplicates srcURL to dstURL (URL->URL)in a repository remembering history.
   * Like 'svn copy srcURL dstURL -m "some comment"' command. It's done by
   * invoking
   */
  public static SVNCommitInfo copy(SVNClientManager ourClientManager,
                                   SVNURL srcURL,
                                   SVNURL dstURL,
                                   boolean isMove,
                                   String commitMessage) throws SVNException {
    /*
     * SVNRevision.HEAD means the latest revision.
     * Returns SVNCommitInfo containing information on the new revision committed
     * (revision number, etc.)
     */
    SVNCopySource svnCopySource = new SVNCopySource(SVNRevision.HEAD, SVNRevision.HEAD, srcURL);
    ourClientManager.getCopyClient().doCopy(new SVNCopySource[]{svnCopySource}, dstURL, isMove,
        true, true, commitMessage, new SVNProperties());

    return null;
  }

  /**
   * Updates a working copy to a different URL. Like 'svn switch URL' command.
   * It's done by invoking
   */
  public static long switchToURL(SVNClientManager ourClientManager,
                                 File wcPath,
                                 SVNURL url,
                                 SVNRevision updateToRevision,
                                 boolean isRecursive)
      throws SVNException {
    SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
    /*
     * sets externals not to be ignored during the switch
     */
    updateClient.setIgnoreExternals(false);
    /*
     * returns the number of the revision wcPath was updated to
     */
    return updateClient.doSwitch(wcPath, url, SVNRevision.UNDEFINED, updateToRevision,
        SVNDepth.getInfinityOrFilesDepth(isRecursive), false, false);
  }

  /**
   * Schedules directories and files for deletion from version control upon the next
   * commit (locally). Like 'svn delete PATH' command. It's done by invoking
   */
  public static void delete(SVNClientManager ourClientManager, File wcPath, boolean force)
      throws SVNException {
    ourClientManager.getWCClient().doDelete(wcPath, force, false);
  }


}
