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

import static org.junit.Assert.*;

import java.io.File;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;

/**
 * Created by suk on 2017/3/16.
 */
public class SVNChangedFilesTest {
  @Before
  public void setup() {
//    BasicConfigurator.configure();
        PropertyConfigurator.configure("D:\\03_projects\\patch-builder-with-maven\\src\\main\\resources\\log4j.properties");
  }

  @Test
  public void export() throws Exception {
    FSRepositoryFactory.setup();
    SVNChangedFiles svnChangedFiles;
//    svnChangedFiles = new SVNChangedFiles("file:///d:/temp/x", "", "",
//        1, 2, "d:/temp/z");
//    svnChangedFiles.export();
//    svnChangedFiles = new SVNChangedFiles("file:///d:/temp/x", "", "",
//        2, 3, "d:/temp/z1");
//    svnChangedFiles.export();
//    svnChangedFiles = new SVNChangedFiles("file:///d:/temp/x", "", "",
//        4, 5, "d:/temp/z2");
//    svnChangedFiles.export();
    svnChangedFiles = new SVNChangedFiles("file:///d:/temp/x", "", "",
        5, 6, "d:/temp/z3");
    svnChangedFiles.export();
    assertTrue(new File("d:/temp/z").exists());
  }

}