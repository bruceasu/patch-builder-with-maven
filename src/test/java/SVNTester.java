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

import asu.patch.builder.svn.CommitEventHandler;
import asu.patch.builder.svn.SVNUtils;
import asu.patch.builder.svn.UpdateEventHandler;
import asu.patch.builder.svn.WCEventHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * Created by suk on 2017/3/15.
 */
public class SVNTester {
  private static final Logger LOGGER = LoggerFactory.getLogger(SVNTester.class);

  public static void main(String[] args) throws SVNException {
//    BasicConfigurator.configure();
    PropertyConfigurator.configure("D:\\03_projects\\patch-builder-with-maven\\src\\main\\resources\\log4j.properties");
//    System.setProperty("log4j.configurationFile",
//        "D:\\03_projects\\patch-builder-with-maven\\src\\main\\resources\\log4j.properties");
    SVNURL repositoryURL = null;
    try {
      repositoryURL = SVNURL.parseURIEncoded("file:///D:/temp/x");
    } catch (SVNException e) {
      //
    }
    String name = "";
    String password = "";
    String myWorkingCopyPath = "D:\\temp\\svn2";

    // test data
    String importDir = "/importDir";
    String importFile = importDir + "/importFile.txt";
    String importFileText = "This unversioned file is imported into a repository";

    String newDir = "/newDir";
    String newFile = newDir + "/newFile.txt";
    String fileText = "This is a new file added to the working copy";

    /*
     * That's where a new directory will be created
     */
    SVNURL url = repositoryURL.appendPath("MyRepos", false);
    /*
     * That's where '/MyRepos' will be copied to (branched)
     */
    SVNURL copyURL = repositoryURL.appendPath("MyReposCopy", false);
    /*
     * That's where a local directory will be imported into.
     * Note that it's not necessary that the '/importDir' directory must already
     * exist - the SVN repository server will take care of creating it.
     */
    SVNURL importToURL = url.appendPath(importDir, false);
    
    SVNClientManager ourClientManager = SVNUtils.create(name, password);
    CommitEventHandler myCommitEventHandler = new CommitEventHandler();
    UpdateEventHandler myUpdateEventHandler = new UpdateEventHandler();
    WCEventHandler myWCEventHandler = new WCEventHandler();
    /*
     * Sets a custom event handler for operations of an SVNCommitClient
     * instance
     */
    ourClientManager.getCommitClient().setEventHandler(myCommitEventHandler);

    /*
     * Sets a custom event handler for operations of an SVNUpdateClient
     * instance
     */
    ourClientManager.getUpdateClient().setEventHandler(myUpdateEventHandler);

    /*
     * Sets a custom event handler for operations of an SVNWCClient
     * instance
     */
    ourClientManager.getWCClient().setEventHandler(myWCEventHandler);

    long committedRevision = makeSVNDirectory(url, ourClientManager);
    committedRevision = importDirectoryToSVN(importDir, importFile, importFileText, importToURL,
        ourClientManager, committedRevision);
    /*
     * creates a local directory where the working copy will be checked out into
     */
    File wcDir = new File(myWorkingCopyPath);
    committedRevision = checkout(url, wcDir, ourClientManager);
    showInfo(ourClientManager, wcDir);
    addEntry(newDir, newFile, fileText, ourClientManager, wcDir);
    committedRevision = showStatus(ourClientManager, wcDir);
    committedRevision = update(ourClientManager, wcDir);

    committedRevision = commit(ourClientManager,wcDir);

    lock(newDir, newFile, ourClientManager, wcDir);

    committedRevision = showStatus(ourClientManager, wcDir);

    committedRevision = copy(url, copyURL, ourClientManager, committedRevision);
    committedRevision = switchTo(copyURL, ourClientManager, committedRevision, wcDir);
    showInfo(ourClientManager, wcDir);
    delete(newDir, wcDir, ourClientManager);
    committedRevision = showStatus(ourClientManager, wcDir);
    committedRevision = commit(newDir, wcDir, ourClientManager);
    System.exit(0);
  }

  private static long commit(String newDir, File wcDir, SVNClientManager ourClientManager) {
    File aNewDir = new File(wcDir, newDir);
    long committedRevision = -1;
    LOGGER.info("Committing changes for '" + wcDir.getAbsolutePath() + "'...");
    try {
      /*
       * lastly commits changes in wcDir to the repository; all items that
       * were locked by the user (if any) will be unlocked after the commit
       * succeeds; this commit will remove aNewDir from the repository.
       */
      committedRevision = SVNUtils.commit(ourClientManager,
          wcDir,
          false,
          "deleting '" + aNewDir.getAbsolutePath()
              + "' from the filesystem as well as from the repository").getNewRevision();
    } catch (SVNException svne) {
      error("error while committing changes to the working copy '"
          + wcDir.getAbsolutePath()
          + "'", svne);
    }
    LOGGER.info("Committed to revision " + committedRevision);
    return committedRevision;
  }

  private static void delete(String newDir, File wcDir, SVNClientManager ourClientManager) {
    File aNewDir = new File(wcDir, newDir);
    LOGGER.info("Scheduling '" + aNewDir.getAbsolutePath() + "' for deletion ...");
    try {
      /*
       * schedules aNewDir for deletion (with forcing)
       */
      SVNUtils.delete(ourClientManager, aNewDir, true);
    } catch (SVNException svne) {
      error("error while schediling '"
          + wcDir.getAbsolutePath() + "' for deletion", svne);
    }
    LOGGER.info("");
  }

  private static long switchTo(SVNURL copyURL, SVNClientManager ourClientManager, long committedRevision, File wcDir) {
  /*
   * displays what revision the repository was committed to
   */
    LOGGER.info("Committed to revision " + committedRevision);
    LOGGER.info("");

    LOGGER.info("Switching '" + wcDir.getAbsolutePath() + "' to '"
        + copyURL + "'...");
    try {
      /*
       * recursively switches wcDir to copyURL in the latest revision
       * (SVNRevision.HEAD)
       */
      committedRevision = SVNUtils.switchToURL(ourClientManager, wcDir, copyURL, SVNRevision.HEAD, true);
    } catch (SVNException svne) {
      error("error while switching '"
          + wcDir.getAbsolutePath() + "' to '" + copyURL + "'", svne);
    }
    LOGGER.info("");
    return committedRevision;
  }

  private static long copy(SVNURL url, SVNURL copyURL, SVNClientManager ourClientManager, long committedRevision) {
    LOGGER.info("Copying '" + url + "' to '" + copyURL + "'...");
    try {
      /*
       * makes a branch of url at copyURL - that is URL->URL copying
       * with history
       */
      SVNUtils.copy(ourClientManager, url, copyURL, false,
          "remotely copying '" + url + "' to '" + copyURL + "'");
      committedRevision++;
    } catch (SVNException svne) {
      error("error while copying '" + url + "' to '"
          + copyURL + "'", svne);
    }
    return committedRevision;
  }

  private static void lock(String newDir, String newFile, SVNClientManager ourClientManager, File wcDir) {
    File aNewDir = new File(wcDir, newDir);
    File aNewFile = new File(aNewDir, SVNPathUtil.tail(newFile));
    LOGGER.info("Locking (with stealing if the entry is already locked) '"
        + aNewFile.getAbsolutePath() + "'.");
    try {
      /*
       * locks aNewFile with stealing (if it has been already locked by someone
       * else), providing a lock comment
       */
      SVNUtils.lock(ourClientManager, aNewFile, true, "locking '/newDir/newFile.txt'");
    } catch (SVNException svne) {
      error("error while locking the working copy file '"
          + aNewFile.getAbsolutePath() + "'", svne);
    }
    LOGGER.info("");
  }

  private static long commit(SVNClientManager ourClientManager, File wcDir) {
    long committedRevision = -1;
    LOGGER.info("Committing changes for '" + wcDir.getAbsolutePath() + "'...");
    try {
      /*
       * commits changes in wcDir to the repository with not leaving items
       * locked (if any) after the commit succeeds; this will add aNewDir &
       * aNewFile to the repository.
       */
      committedRevision = SVNUtils.commit(ourClientManager, wcDir, false,
          "'/newDir' with '/newDir/newFile.txt' were added")
          .getNewRevision();
    } catch (SVNException svne) {
      error("error while committing changes to the working copy at '"
          + wcDir.getAbsolutePath()
          + "'", svne);
    }
    LOGGER.info("Committed to revision " + committedRevision);
    LOGGER.info("");
    return committedRevision;
  }

  private static long update(SVNClientManager ourClientManager, File wcDir) {
    long committedRevision = -1;
    LOGGER.info("Updating '" + wcDir.getAbsolutePath() + "'...");
    try {
      /*
       * recursively updates wcDir to the latest revision (SVNRevision.HEAD)
       */
      committedRevision = SVNUtils.update(ourClientManager, wcDir, SVNRevision.HEAD, true);
    } catch (SVNException svne) {
      error("error while recursively updating the working copy at '"
          + wcDir.getAbsolutePath() + "'", svne);
    }
    LOGGER.info("");
    return committedRevision;
  }

  private static long showStatus(SVNClientManager ourClientManager, File wcDir) {
    boolean isRecursive = true;
    boolean isRemote = true;
    boolean isReportAll = false;
    boolean isIncludeIgnored = true;
    boolean isCollectParentExternals = false;
    LOGGER.info("Status for '" + wcDir.getAbsolutePath() + "':");
    long committedRevision = -1;
    try {
      /*
       * gets and shows status information for the WC directory.
       * status will be recursive on wcDir, will also cover the repository,
       * won't cover unmodified entries, will disregard 'svn:ignore' property
       * ignores (if any), will ignore externals definitions.
       */
      committedRevision = SVNUtils.showStatus(ourClientManager, wcDir, isRecursive, isRemote,
          isReportAll, isIncludeIgnored, isCollectParentExternals);
    } catch (SVNException svne) {
      error("error while recursively performing status for '"
          + wcDir.getAbsolutePath() + "'", svne);
    }
    LOGGER.info("");
    return committedRevision;
  }

  private static void addEntry(String newDir, String newFile, String fileText, SVNClientManager ourClientManager, File wcDir) {
    File aNewDir = new File(wcDir, newDir);
    File aNewFile = new File(aNewDir, SVNPathUtil.tail(newFile));
    /*
     * creates a new local directory - 'wcDir/newDir' and a new file -
     * '/MyWorkspace/newDir/newFile.txt'
     */
    createLocalDir(aNewDir, new File[]{aNewFile}, new String[]{fileText});

    LOGGER.info("Recursively scheduling a new directory '"
        + aNewDir.getAbsolutePath() + "' for addition...");
    try {
      /*
       * recursively schedules aNewDir for addition
       */
      SVNUtils.addEntry(ourClientManager, aNewDir);
    } catch (SVNException svne) {
      error("error while recursively adding the directory '"
          + aNewDir.getAbsolutePath() + "'", svne);
    }
    LOGGER.info("");
  }

  private static void showInfo(SVNClientManager ourClientManager, File wcDir) {
  /*
   * recursively displays info for wcDir at the current working revision
   * in the manner of 'svn info -R' command
   */
    try {
      SVNUtils.showInfo(ourClientManager, wcDir, null, SVNRevision.WORKING, true);
    } catch (SVNException svne) {
      error("error while recursively getting info for the working copy at'"
          + wcDir.getAbsolutePath() + "'", svne);
    }
    LOGGER.info("");
  }

  private static long checkout(SVNURL url, File wcDir, SVNClientManager ourClientManager) {
    if (wcDir.exists()) {
      error("the destination directory '"
          + wcDir.getAbsolutePath() + "' already exists!", null);
    }
    wcDir.mkdirs();
    long committedRevision = -1;
    LOGGER.info("Checking out a working copy from '" + url + "'...");
    try {
      /*
       * recursively checks out a working copy from url into wcDir.
       * SVNRevision.HEAD means the latest revision to be checked out.
       */
      committedRevision = SVNUtils.checkout(ourClientManager, url, SVNRevision.HEAD, wcDir, true);
    } catch (SVNException svne) {
      error("error while checking out a working copy for the location '"
          + url + "'", svne);
    }
    LOGGER.info("");
    return committedRevision;
  }

  private static long importDirectoryToSVN(String importDir, String importFile, String importFileText, SVNURL importToURL, SVNClientManager ourClientManager, long committedRevision) {
    File anImportDir = new File(importDir);
    File anImportFile = new File(anImportDir, SVNPathUtil.tail(importFile));
    /*
     * creates a new local directory - './importDir' and a new file -
     * './importDir/importFile.txt' that will be imported into the repository
     * into the '/MyRepos/importDir' directory
     */
    createLocalDir(anImportDir, new File[]{anImportFile}, new String[]{importFileText});

    LOGGER.info("Importing a new directory into '" + importToURL + "'...");
    try {
      /*
       * recursively imports an unversioned directory into a repository
       * and displays what revision the repository was committed to
       */
      boolean isRecursive = true;
      committedRevision = SVNUtils.importDirectory(ourClientManager, anImportDir, importToURL,
          "importing a new directory '" + anImportDir.getAbsolutePath() + "'", isRecursive)
          .getNewRevision();
    } catch (SVNException svne) {
      error("error while importing a new directory '" + anImportDir.getAbsolutePath()
          + "' into '" + importToURL + "'", svne);
    }
    LOGGER.info("Committed to revision " + committedRevision);
    LOGGER.info("");
    return committedRevision;
  }

  private static long makeSVNDirectory(SVNURL url, SVNClientManager ourClientManager) {
    long committedRevision = -1;
    LOGGER.info("Making a new directory at '" + url + "'...");
    try {
      /*
       * creates a new version comtrolled directory in a repository and
       * displays what revision the repository was committed to
       */
      committedRevision = SVNUtils.makeDirectory(ourClientManager, url,
          "making a new directory at '" + url + "'").getNewRevision();
    } catch (SVNException svne) {
      error("error while making a new directory at '" + url + "'", svne);
    }
    LOGGER.info("Committed to revision " + committedRevision);
    LOGGER.info("");
    return committedRevision;
  }

  /*
 * Displays error information and exits.
 */
  private static void error(String message, Exception e) {
    LOGGER.error(message + (e != null ? ": " + e.getMessage() : ""));
    System.exit(1);
  }

  /*
   * This method does not relate to SVNKit API. Just a method which creates
   * local directories and files :)
   */
  private static final void createLocalDir(File aNewDir, File[] localFiles, String[] fileContents) {
    if (!aNewDir.mkdirs()) {
      error("failed to create a new directory '" + aNewDir.getAbsolutePath() + "'.", null);
    }
    for (int i = 0; i < localFiles.length; i++) {
      File aNewFile = localFiles[i];
      try {
        if (!aNewFile.createNewFile()) {
          error("failed to create a new file '"
              + aNewFile.getAbsolutePath() + "'.", null);
        }
      } catch (IOException ioe) {
        aNewFile.delete();
        error("error while creating a new file '"
            + aNewFile.getAbsolutePath() + "'", ioe);
      }

      String contents = null;
      if (i > fileContents.length - 1) {
        continue;
      }
      contents = fileContents[i];

	        /*
           * writing a text into the file
	         */
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(aNewFile);
        fos.write(contents.getBytes());
      } catch (FileNotFoundException fnfe) {
        error("the file '" + aNewFile.getAbsolutePath() + "' is not found", fnfe);
      } catch (IOException ioe) {
        error("error while writing into the file '"
            + aNewFile.getAbsolutePath() + "'", ioe);
      } finally {
        if (fos != null) {
          try {
            fos.close();
          } catch (IOException ioe) {
            //
          }
        }
      }
    }
  }
}
