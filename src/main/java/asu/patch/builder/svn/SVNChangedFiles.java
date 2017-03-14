package asu.patch.builder.svn;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNDiffStatusHandler;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNDiffStatus;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class SVNChangedFiles
{
  private SVNURL branchURL;
  private String username;
  private String password;
  private SVNRevision startingRevision;
  private SVNRevision endingRevision;
  private String destinationDirectory;
  private ISVNAuthenticationManager authManager;
  
  public SVNChangedFiles(String branchURL, String username, String password, long startingRevision, long endingRevision, String destinationDirectory)
  {
    DAVRepositoryFactory.setup();
    try
    {
      this.branchURL = SVNURL.parseURIEncoded(branchURL);
      this.username = username;
      this.password = password;
      this.startingRevision = SVNRevision.create(startingRevision);
      this.endingRevision = SVNRevision.create(endingRevision);
      this.destinationDirectory = destinationDirectory;
      
      this.authManager = SVNWCUtil.createDefaultAuthenticationManager(
        this.username, this.password);
    }
    catch (SVNException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public void export()
  {
    export(null);
  }
  
  public void export(ArrayList changes)
  {
    if (changes == null) {
      changes = list();
    }
    SVNUpdateClient updateClient = new SVNUpdateClient(this.authManager, 
      SVNWCUtil.createDefaultOptions(true));
    try
    {
      for (int idx = 0; idx < changes.size(); idx++)
      {
        SVNDiffStatus change = (SVNDiffStatus)changes.get(idx);
        File destination = new File(this.destinationDirectory + "\\" + 
          change.getPath());
        


        updateClient.doExport(change.getURL(), destination, this.endingRevision, 
          this.endingRevision, null, true, SVNDepth.fromRecurse(false));
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public ArrayList list()
  {
    try
    {
      SVNDiffClient diffClient = new SVNDiffClient(this.authManager, 
        SVNWCUtil.createDefaultOptions(true));
      
      ArrayList changes = new ArrayList();
      

      ImplISVNDiffStatusHandler handler = new ImplISVNDiffStatusHandler(changes);
      



      diffClient.doDiffStatus(this.branchURL, this.startingRevision, 
        this.branchURL, this.endingRevision, 
        SVNDepth.getInfinityOrFilesDepth(true), false, handler);
      
      return changes;
    }
    catch (SVNException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  private static class ImplISVNDiffStatusHandler
    implements ISVNDiffStatusHandler
  {
    private ArrayList changes;
    
    public ImplISVNDiffStatusHandler(ArrayList changes)
    {
      this.changes = changes;
    }
    
    public void handleDiffStatus(SVNDiffStatus status)
      throws SVNException
    {
      boolean isFile = status.getKind() == SVNNodeKind.FILE;
      if (isFile)
      {
        boolean isOutput = (status.getModificationType() == SVNStatusType.STATUS_ADDED) || 
          (status.getModificationType() == SVNStatusType.STATUS_MODIFIED) || 
          (status.getModificationType() == SVNStatusType.STATUS_NONE);
        if (isOutput) {
          this.changes.add(status);
        } else {
          System.out.println("未导出文件\t[" + status.getKind() + "]:[" + 
            status.getModificationType() + "]:[" + status.getPath() + 
            "]");
        }
      }
    }
  }
}
