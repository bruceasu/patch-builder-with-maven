package asu.patch.builder.svn;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.nutz.lang.Streams;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.ISVNDiffStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNDiffStatus;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class SVNChangedFiles {
  private final SVNClientManager svnClientManager;
  private SVNURL branchURL;
  private String username;
  private String password;
  private SVNRevision startingRevision;
  private SVNRevision endingRevision;
  private String destinationDirectory;
  private ISVNAuthenticationManager authManager;

  public SVNChangedFiles(String branchURL, String username, String password,
                         long startingRevision, long endingRevision, File destinationDirectory) {
    this(branchURL, username, password, startingRevision, endingRevision,
        destinationDirectory.getAbsolutePath());
  }
  public SVNChangedFiles(String branchURL, String username, String password,
                         long startingRevision, long endingRevision, String destinationDirectory) {
    try {
      SVNUtils.setupLibrary();
      this.branchURL = SVNURL.parseURIEncoded(branchURL);
      this.username = username;
      this.password = password;
      this.startingRevision = SVNRevision.create(startingRevision);
      this.endingRevision = SVNRevision.create(endingRevision);
      this.destinationDirectory = destinationDirectory;

      this.authManager = SVNWCUtil.createDefaultAuthenticationManager(
          this.username, this.password.toCharArray());
      this.svnClientManager = SVNUtils.create(authManager);
      SVNUtils.showInfo(svnClientManager, this.branchURL, this.startingRevision,
          this.endingRevision, false);
      svnClientManager.getLogClient().doLog(this.branchURL, new String[0], this.startingRevision,
          this.startingRevision, this.endingRevision, true, true, true, -1, new String[0],
          new LogHandler());
    } catch (SVNException e) {
      throw new RuntimeException(e);
    }
  }

  public void export() {
    export(null, null);
  }

  public void export(ArrayList<SVNDiffStatus> changes, ArrayList<SVNDiffStatus> removes) {
    ArrayList<SVNDiffStatus>[] diff = list();
    if (changes == null || changes.isEmpty()) {
      changes = diff[0];
    }
    if (removes == null || removes.isEmpty()) {
      removes = diff[1];
    }
    //
    //    SVNUpdateClient updateClient = new SVNUpdateClient(this.authManager,
    //        SVNWCUtil.createDefaultOptions(true));
    SVNUpdateClient updateClient = svnClientManager.getUpdateClient();
    try {
      for (int idx = 0; idx < changes.size(); idx++) {
        SVNDiffStatus change = changes.get(idx);
        File destination = new File(this.destinationDirectory + "\\" + change.getPath());
        updateClient.doExport(change.getURL(), destination, this.endingRevision,
            this.endingRevision, null, true, SVNDepth.fromRecurse(false));
      }
      if (!removes.isEmpty()) {
        createWinBat(removes);
        createLinuxSh(removes);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void createLinuxSh(ArrayList<SVNDiffStatus> removes) {
    File linuxSh = new File(this.destinationDirectory, "removes.sh");
    try (Writer writer = Streams.fileOutw(linuxSh)) {
      List<String> lines1 = new ArrayList<>();
      List<String> lines2 = new ArrayList<>();
      writer.write("!/bin/sh\n");
      writer.write("# clear the remove files.\n");
      for (SVNDiffStatus status : removes) {
        String path = status.getPath();
        if (path.endsWith(".java")) {
          path = path.replace('/', '.').replace(".java", "*.class");
          lines1.add("rm -f " + path);
        } else {
          lines2.add("rm -f " + path);
        }
      }
      writeLine(writer, lines1, "\n");
      writeLine(writer, lines2, "\n");
      Streams.safeClose(writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void createWinBat(ArrayList<SVNDiffStatus> removes) {
    File winBat = new File(this.destinationDirectory, "removes.bat");
    try (Writer writer = Streams.fileOutw(winBat)) {
      List<String> lines1 = new ArrayList<>();
      List<String> lines2 = new ArrayList<>();
      writer.write("@echo off\r\n");
      writer.write("rem clear the remove files.\r\n");
      for (SVNDiffStatus status : removes) {
        String path = status.getPath();
        if (path.endsWith(".java")) {
          path = path.replace('/', '.').replace(".java", "*.class");
          lines1.add("del /q /s " + path);
        } else {
          lines2.add("del /q /s " + path);
        }
      }
      writeLine(writer, lines1, "\r\n");
      writeLine(writer, lines2, "\r\n");
      Streams.safeClose(writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void writeLine(Writer writer, Collection<String> lines, String lineSplitor)
      throws IOException {
    for (String line : lines) {
      writer.write(line);
      writer.write(lineSplitor);
    }
  }

  public ArrayList<SVNDiffStatus>[] list() {
    try {
      // SVNDiffClient diffClient = new SVNDiffClient(this.authManager,
      //   SVNWCUtil.createDefaultOptions(true));
      SVNDiffClient diffClient = svnClientManager.getDiffClient();

      ArrayList<SVNDiffStatus> changes = new ArrayList<>();
      ArrayList<SVNDiffStatus> removes = new ArrayList<>();

      ImplISVNDiffStatusHandler handler = new ImplISVNDiffStatusHandler(changes, removes);


      diffClient.doDiffStatus(this.branchURL, this.startingRevision,
          this.branchURL, this.endingRevision,
          SVNDepth.getInfinityOrFilesDepth(true), false, handler);

      return new ArrayList[]{changes, removes};
    } catch (SVNException e) {
      throw new RuntimeException(e);
    }
  }

  private static class ImplISVNDiffStatusHandler
      implements ISVNDiffStatusHandler {
    private ArrayList changes;
    private ArrayList removes;

    public ImplISVNDiffStatusHandler(ArrayList changes, ArrayList removes) {
      this.changes = changes;
      this.removes = removes;
    }

    public void handleDiffStatus(SVNDiffStatus status)
        throws SVNException {
      boolean isFile = status.getKind() == SVNNodeKind.FILE;
      if (isFile) {
        boolean isOutput = (status.getModificationType() == SVNStatusType.STATUS_ADDED) ||
            (status.getModificationType() == SVNStatusType.STATUS_MODIFIED) ||
            (status.getModificationType() == SVNStatusType.STATUS_NONE);
        if (isOutput) {
          this.changes.add(status);
        } else {
          //          System.out.println("未导出文件\t[" + status.getKind() + "]:[" +
          //              status.getModificationType() + "]:[" + status.getPath() +
          //              "]");
          this.removes.add(status);
        }
      }
    }
  }
}
