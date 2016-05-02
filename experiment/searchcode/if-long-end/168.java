/**
 * @copyright
 * ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one
 *    or more contributor license agreements.  See the NOTICE file
 *    distributed with this work for additional information
 *    regarding copyright ownership.  The ASF licenses this file
 *    to you under the Apache License, Version 2.0 (the
 *    "License"); you may not use this file except in compliance
 *    with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 * ====================================================================
 * @endcopyright
 */
package org.apache.subversion.javahl;

import org.apache.subversion.javahl.callback.*;
import org.apache.subversion.javahl.types.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * Tests the basic functionality of javahl binding (inspired by the
 * tests in subversion/tests/cmdline/basic_tests.py).
 */
public class BasicTests extends SVNTests
{
    /**
     * Base name of all our tests.
     */
    public final static String testName = "basic_test";

    public BasicTests()
    {
        init();
    }

    public BasicTests(String name)
    {
        super(name);
        init();
    }

    /**
     * Initialize the testBaseName and the testCounter, if this is the
     * first test of this class.
     */
    private void init()
    {
        if (!testName.equals(testBaseName))
        {
            testCounter = 0;
            testBaseName = testName;
        }
    }

    /**
     * Test LogDate().
     * @throws Throwable
     */
    public void testLogDate() throws Throwable
    {
        String goodDate = "2007-10-04T03:00:52.134992Z";
        String badDate = "2008-01-14";
        LogDate logDate;

        try
        {
            logDate = new LogDate(goodDate);
            assertEquals(1191466852134992L, logDate.getTimeMicros());
        } catch (ParseException e) {
            fail("Failed to parse date " + goodDate);
        }

        try
        {
            logDate = new LogDate(badDate);
            fail("Failed to throw exception on bad date " + badDate);
        } catch (ParseException e) {
        }
    }

    /**
     * Test SVNClient.getVersion().
     * @throws Throwable
     */
    public void testVersion() throws Throwable
    {
        try
        {
            Version version = client.getVersion();
            String versionString = version.toString();
            if (versionString == null || versionString.trim().length() == 0)
            {
                throw new Exception("Version string empty");
            }
        }
        catch (Exception e)
        {
            fail("Version should always be available unless the " +
                 "native libraries failed to initialize: " + e);
        }
    }

    /**
     * Test the JNIError class functionality
     * @throws Throwable
     */
    public void testJNIError() throws Throwable
    {
        // build the test setup.
        OneTest thisTest = new OneTest();

        // Create a client, dispose it, then try to use it later
        ISVNClient tempclient = new SVNClient();
        tempclient.dispose();

        // create Y and Y/Z directories in the repository
        addExpectedCommitItem(null, thisTest.getUrl().toString(), "Y", NodeKind.none,
                              CommitItemStateFlags.Add);
        Set<String> urls = new HashSet<String>(1);
        urls.add(thisTest.getUrl() + "/Y");
        try 
        {
            tempclient.mkdir(urls, false, null, new ConstMsg("log_msg"), null);
        } 
        catch(JNIError e)
        {
	        return; // Test passes!
        }
        fail("A JNIError should have been thrown here.");
    }

    /**
     * Tests Mergeinfo and RevisionRange classes.
     * @since 1.5
     */
    public void testMergeinfoParser() throws Throwable
    {
        String mergeInfoPropertyValue =
            "/trunk:1-300,305,307,400-405\n/branches/branch:308-400";
        Mergeinfo info = new Mergeinfo(mergeInfoPropertyValue);
        Set<String> paths = info.getPaths();
        assertEquals(2, paths.size());
        List<RevisionRange> trunkRange = info.getRevisionRange("/trunk");
        assertEquals(4, trunkRange.size());
        assertEquals("1-300", trunkRange.get(0).toString());
        assertEquals("305", trunkRange.get(1).toString());
        assertEquals("307", trunkRange.get(2).toString());
        assertEquals("400-405", trunkRange.get(3).toString());
        List<RevisionRange> branchRange =
            info.getRevisionRange("/branches/branch");
        assertEquals(1, branchRange.size());
    }

    /**
     * Test the basic SVNClient.status functionality.
     * @throws Throwable
     */
    public void testBasicStatus() throws Throwable
    {
        // build the test setup
        OneTest thisTest = new OneTest();

        // check the status of the working copy
        thisTest.checkStatus();

        // Test status of non-existent file
        File fileC = new File(thisTest.getWorkingCopy() + "/A", "foo.c");

        MyStatusCallback statusCallback = new MyStatusCallback();
        client.status(fileToSVNPath(fileC, false), Depth.unknown, false, true,
                    false, false, null, statusCallback);
        if (statusCallback.getStatusArray().length > 0)
            fail("File foo.c should not return a status.");

    }

    /**
     * Test the "out of date" info from {@link
     * org.apache.subversion.javahl.SVNClient#status()}.
     *
     * @throws SubversionException
     * @throws IOException
     */
    public void testOODStatus() throws SubversionException, IOException
    {
        // build the test setup
        OneTest thisTest = new OneTest();

        // Make a whole slew of changes to a WC:
        //
        //  (root)               r7 - prop change
        //  iota
        //  A
        //  |__mu
        //  |
        //  |__B
        //  |   |__lambda
        //  |   |
        //  |   |__E             r12 - deleted
        //  |   |  |__alpha
        //  |   |  |__beta
        //  |   |
        //  |   |__F             r9 - prop change
        //  |   |__I             r6 - added dir
        //  |
        //  |__C                 r5 - deleted
        //  |
        //  |__D
        //     |__gamma
        //     |
        //     |__G
        //     |  |__pi          r3 - deleted
        //     |  |__rho         r2 - modify text
        //     |  |__tau         r4 - modify text
        //     |
        //     |__H
        //        |__chi         r10-11 replaced with file
        //        |__psi         r13-14 replaced with dir
        //        |__omega
        //        |__nu          r8 - added file
        File file, dir;
        PrintWriter pw;
        Status status;
        MyStatusCallback statusCallback;
        long rev;             // Resulting rev from co or update
        long expectedRev = 2;  // Keeps track of the latest rev committed

        // ----- r2: modify file A/D/G/rho --------------------------
        file = new File(thisTest.getWorkingCopy(), "A/D/G/rho");
        pw = new PrintWriter(new FileOutputStream(file, true));
        pw.print("modification to rho");
        pw.close();
        addExpectedCommitItem(thisTest.getWCPath(),
                              thisTest.getUrl().toString(), "A/D/G/rho",
                              NodeKind.file, CommitItemStateFlags.TextMods);
        rev = commit(thisTest, "log msg");
        assertEquals("wrong revision number from commit", rev, expectedRev++);
        thisTest.getWc().setItemWorkingCopyRevision("A/D/G/rho", rev);
        thisTest.getWc().setItemContent("A/D/G/rho",
            thisTest.getWc().getItemContent("A/D/G/rho")
            + "modification to rho");

        statusCallback = new MyStatusCallback();
        client.status(thisTest.getWCPath() + "/A/D/G/rho", Depth.immediates,
                        false, true, false, false, null, statusCallback);
        status = statusCallback.getStatusArray()[0];
        long rhoCommitDate = status.getLastChangedDate().getTime();
        long rhoCommitRev = rev;
        String rhoAuthor = status.getLastCommitAuthor();

        // ----- r3: delete file A/D/G/pi ---------------------------
        client.remove(thisTest.getWCPathSet("/A/D/G/pi"),
                      false, false, null, null, null);
        addExpectedCommitItem(thisTest.getWCPath(), thisTest.getUrl().toString(),
                              "A/D/G/pi", NodeKind.file,
                              CommitItemStateFlags.Delete);
        rev = commit(thisTest, "log msg");
        assertEquals("wrong revision number from commit", rev, expectedRev++);
        thisTest.getWc().removeItem("A/D/G/pi");

        thisTest.getWc().setItemWorkingCopyRevision("A/D/G", rev);
        assertEquals("wrong revision from update",
                     update(thisTest, "/A/D/G"), rev);
        long GCommitRev = rev;

        // ----- r4: modify file A/D/G/tau --------------------------
        file = new File(thisTest.getWorkingCopy(), "A/D/G/tau");
        pw = new PrintWriter(new FileOutputStream(file, true));
        pw.print("modification to tau");
        pw.close();
        addExpectedCommitItem(thisTest.getWCPath(), thisTest.getUrl().toString(),
                              "A/D/G/tau",NodeKind.file,
                              CommitItemStateFlags.TextMods);
        rev = commit(thisTest, "log msg");
        assertEquals("wrong revision number from commit", rev, expectedRev++);
        thisTest.getWc().setItemWorkingCopyRevision("A/D/G/tau", rev);
        thisTest.getWc().setItemContent("A/D/G/tau",
                thisTest.getWc().getItemContent("A/D/G/tau")
                + "modification to tau");
        statusCallback = new MyStatusCallback();
        client.status(thisTest.getWCPath() + "/A/D/G/tau", Depth.immediates,
                      false, true, false, false, null, statusCallback);
        status = statusCallback.getStatusArray()[0];
        long tauCommitDate = status.getLastChangedDate().getTime();
        long tauCommitRev = rev;
        String tauAuthor = status.getLastCommitAuthor();

        // ----- r5: delete dir with no children  A/C ---------------
        client.remove(thisTest.getWCPathSet("/A/C"),
                      false, false, null, null, null);
        addExpectedCommitItem(thisTest.getWCPath(), thisTest.getUrl().toString(),
                              "A/C", NodeKind.dir,
                              CommitItemStateFlags.Delete);
        rev = commit(thisTest, "log msg");
        assertEquals("wrong revision number from commit", rev, expectedRev++);
        thisTest.getWc().removeItem("A/C");
        long CCommitRev = rev;

        // ----- r6: Add dir A/B/I ----------------------------------
        dir = new File(thisTest.getWorkingCopy(), "A/B/I");
        dir.mkdir();

        client.add(dir.getAbsolutePath(), Depth.infinity, false, false, false);
        addExpectedCommitItem(thisTest.getWCPath(), thisTest.getUrl().toString(),
                              "A/B/I", NodeKind.dir, CommitItemStateFlags.Add);
        rev = commit(thisTest, "log msg");
        assertEquals("wrong revision number from commit", rev, expectedRev++);
        thisTest.getWc().addItem("A/B/I", null);
        statusCallback = new MyStatusCallback();
        client.status(thisTest.getWCPath() + "/A/B/I", Depth.immediates,
                    false, true, false, false, null, statusCallback);
        status = statusCallback.getStatusArray()[0];
        long ICommitDate = status.getLastChangedDate().getTime();
        long ICommitRev = rev;
        String IAuthor = status.getLastCommitAuthor();

        // ----- r7: Update then commit prop change on root dir -----
        thisTest.getWc().setRevision(rev);
        assertEquals("wrong revision from update",
                     update(thisTest), rev);
        thisTest.checkStatus();
        setprop(thisTest.getWCPath(), "propname", "propval");
        thisTest.getWc().setItemPropStatus("", Status.Kind.modified);
        addExpectedCommitItem(thisTest.getWCPath(), thisTest.getUrl().toString(),
                             null, NodeKind.dir, CommitItemStateFlags.PropMods);
        rev = commit(thisTest, "log msg");
        assertEquals("wrong revision number from commit", rev, expectedRev++);
        thisTest.getWc().setItemWorkingCopyRevision("", rev);
        thisTest.getWc().setItemPropStatus("", Status.Kind.normal);

        // ----- r8: Add a file A/D/H/nu ----------------------------
        file = new File(thisTest.getWorkingCopy(), "A/D/H/nu");
        pw = new PrintWriter(new FileOutputStream(file));
        pw.print("This is the file 'nu'.");
        pw.close();
        client.add(file.getAbsolutePath(), Depth.empty, false, false, false);
        addExpectedCommitItem(thisTest.getWCPath(), thisTest.getUrl().toString(),
                              "A/D/H/nu", NodeKind.file,
                              CommitItemStateFlags.TextMods +
                              CommitItemStateFlags.Add);
        rev = commit(thisTest, "log msg");
        assertEquals("wrong revision number from commit", rev, expectedRev++);
        thisTest.getWc().addItem("A/D/H/nu", "This is the file 'nu'.");
        statusCallback = new MyStatusCallback();
        client.status(thisTest.getWCPath() + "/A/D/H/nu", Depth.immediates,
                    false, true, false, false, null, statusCallback);
        status = statusCallback.getStatusArray()[0];
        long nuCommitDate = status.getLastChangedDate().getTime();
        long nuCommitRev = rev;
        String nuAuthor = status.getLastCommitAuthor();

        // ----- r9: Prop change on A/B/F ---------------------------
        setprop(thisTest.getWCPath() + "/A/B/F", "propname", "propval");
        addExpectedCommitItem(thisTest.getWCPath(), thisTest.getUrl().toString(),
                              "A/B/F", NodeKind.dir,
                              CommitItemStateFlags.PropMods);
        rev = commit(thisTest, "log msg");
        assertEquals("wrong revision number from commit", rev, expectedRev++);
        thisTest.getWc().setItemPropStatus("A/B/F", Status.Kind.normal);
        thisTest.getWc().setItemWorkingCopyRevision("A/B/F", rev);
        statusCallback = new MyStatusCallback();
        client.status(thisTest.getWCPath() + "/A/B/F", Depth.immediates,
                    false, true, false, false, null, statusCallback);
        status = statusCallback.getStatusArray()[0];
        long FCommitDate = status.getLastChangedDate().getTime();
        long FCommitRev = rev;
        String FAuthor = status.getLastCommitAuthor();

        // ----- r10-11: Replace file A/D/H/chi with file -----------
        client.remove(thisTest.getWCPathSet("/A/D/H/chi"),
                      false, false, null, null, null);
        addExpectedCommitItem(thisTest.getWCPath(), thisTest.getUrl().toString(),
                              "A/D/H/chi", NodeKind.file,
                              CommitItemStateFlags.Delete);
        rev = commit(thisTest, "log msg");
        assertEquals("wrong revision number from commit", rev, expectedRev++);
        thisTest.getWc().removeItem("A/D/G/pi");

        file = new File(thisTest.getWorkingCopy(), "A/D/H/chi");
        pw = new PrintWriter(new FileOutputStream(file));
        pw.print("This is the replacement file 'chi'.");
        pw.close();
        client.add(file.getAbsolutePath(), Depth.empty, false, false, false);
        addExpectedCommitItem(thisTest.getWCPath(), thisTest.getUrl().toString(),
                              "A/D/H/chi", NodeKind.file,
                              CommitItemStateFlags.TextMods +
                              CommitItemStateFlags.Add);
        rev = commit(thisTest, "log msg");
        assertEquals("wrong revision number from commit", rev, expectedRev++);
        thisTest.getWc().addItem("A/D/H/chi",
                                 "This is the replacement file 'chi'.");
        statusCallback = new MyStatusCallback();
        client.status(thisTest.getWCPath() + "/A/D/H/chi", Depth.immediates,
                    false, true, false, false, null, statusCallback);
        status = statusCallback.getStatusArray()[0];
        long chiCommitDate = status.getLastChangedDate().getTime();
        long chiCommitRev = rev;
        String chiAuthor = status.getLastCommitAuthor();

        // ----- r12: Delete dir A/B/E with children ----------------
        client.remove(thisTest.getWCPathSet("/A/B/E"),
                      false, false, null, null, null);
        addExpectedCommitItem(thisTest.getWCPath(), thisTest.getUrl().toString(),
                              "A/B/E", NodeKind.dir,
                              CommitItemStateFlags.Delete);
        rev = commit(thisTest, "log msg");
        assertEquals("wrong revision number from commit", rev, expectedRev++);
        thisTest.getWc().removeItem("A/B/E/alpha");
        thisTest.getWc().removeItem("A/B/E/beta");
        thisTest.getWc().removeItem("A/B/E");

        thisTest.getWc().setItemWorkingCopyRevision("A/B", rev);
        assertEquals("wrong revision from update",
                     update(thisTest, "/A/B"), rev);
        Info Binfo = collectInfos(thisTest.getWCPath() + "/A/B", null, null,
                                   Depth.empty, null)[0];
        long BCommitDate = Binfo.getLastChangedDate().getTime();
        long BCommitRev = rev;
        long ECommitRev = BCommitRev;
        String BAuthor = Binfo.getLastChangedAuthor();

        // ----- r13-14: Replace file A/D/H/psi with dir ------------
        client.remove(thisTest.getWCPathSet("/A/D/H/psi"),
                      false, false, null, null, null);
        addExpectedCommitItem(thisTest.getWCPath(), thisTest.getUrl().toString(),
                              "A/D/H/psi", NodeKind.file,
                              CommitItemStateFlags.Delete);
        rev = commit(thisTest, "log msg");
        assertEquals("wrong revision number from commit", rev, expectedRev++);
        thisTest.getWc().removeItem("A/D/H/psi");
        thisTest.getWc().setRevision(rev);
        assertEquals("wrong revision from update",
                     update(thisTest), rev);
        thisTest.getWc().addItem("A/D/H/psi", null);
        dir = new File(thisTest.getWorkingCopy(), "A/D/H/psi");
        dir.mkdir();
        client.add(dir.getAbsolutePath(), Depth.infinity, false, false, false);
        addExpectedCommitItem(thisTest.getWCPath(), thisTest.getUrl().toString(),
                              "A/D/H/psi", NodeKind.dir,
                              CommitItemStateFlags.Add);
        rev = commit(thisTest, "log msg");
        assertEquals("wrong revision number from commit", rev, expectedRev++);
        statusCallback = new MyStatusCallback();
        client.status(thisTest.getWCPath() + "/A/D/H/psi", Depth.immediates,
                      false, true, false, false, null, statusCallback);
        status = statusCallback.getStatusArray()[0];
        long psiCommitDate = status.getLastChangedDate().getTime();
        long psiCommitRev = rev;
        String psiAuthor = status.getLastCommitAuthor();

        // ----- Check status of modfied WC then update it back
        // -----  to rev 1 so it's out of date
        thisTest.checkStatus();

        assertEquals("wrong revision from update",
                     client.update(thisTest.getWCPathSet(),
                                   Revision.getInstance(1), Depth.unknown,
                                   false, false, false, false)[0],
                     1);
        thisTest.getWc().setRevision(1);

        thisTest.getWc().setItemOODInfo("A", psiCommitRev, psiAuthor,
                                        psiCommitDate, NodeKind.dir);

        thisTest.getWc().setItemOODInfo("A/B", BCommitRev, BAuthor,
                                        BCommitDate, NodeKind.dir);

        thisTest.getWc().addItem("A/B/I", null);
        thisTest.getWc().setItemOODInfo("A/B/I", ICommitRev, IAuthor,
                                        ICommitDate, NodeKind.dir);
        thisTest.getWc().setItemTextStatus("A/B/I", Status.Kind.none);
        thisTest.getWc().setItemNodeKind("A/B/I", NodeKind.unknown);

        thisTest.getWc().addItem("A/C", null);
        thisTest.getWc().setItemReposLastCmtRevision("A/C", CCommitRev);
        thisTest.getWc().setItemReposKind("A/C", NodeKind.dir);

        thisTest.getWc().addItem("A/B/E", null);
        thisTest.getWc().setItemReposLastCmtRevision("A/B/E", ECommitRev);
        thisTest.getWc().setItemReposKind("A/B/E", NodeKind.dir);
        thisTest.getWc().addItem("A/B/E/alpha", "This is the file 'alpha'.");
        thisTest.getWc().addItem("A/B/E/beta", "This is the file 'beta'.");

        thisTest.getWc().setItemPropStatus("A/B/F", Status.Kind.none);
        thisTest.getWc().setItemOODInfo("A/B/F", FCommitRev, FAuthor,
                                        FCommitDate, NodeKind.dir);

        thisTest.getWc().setItemOODInfo("A/D", psiCommitRev, psiAuthor,
                                        psiCommitDate, NodeKind.dir);

        thisTest.getWc().setItemOODInfo("A/D/G", tauCommitRev, tauAuthor,
                                        tauCommitDate, NodeKind.dir);

        thisTest.getWc().addItem("A/D/G/pi", "This is the file 'pi'.");
        thisTest.getWc().setItemReposLastCmtRevision("A/D/G/pi", GCommitRev);
        thisTest.getWc().setItemReposKind("A/D/G/pi", NodeKind.file);

        thisTest.getWc().setItemContent("A/D/G/rho",
                                        "This is the file 'rho'.");
        thisTest.getWc().setItemOODInfo("A/D/G/rho", rhoCommitRev, rhoAuthor,
                                        rhoCommitDate, NodeKind.file);

        thisTest.getWc().setItemContent("A/D/G/tau",
                                        "This is the file 'tau'.");
        thisTest.getWc().setItemOODInfo("A/D/G/tau", tauCommitRev, tauAuthor,
                                        tauCommitDate, NodeKind.file);

        thisTest.getWc().setItemOODInfo("A/D/H", psiCommitRev, psiAuthor,
                                        psiCommitDate, NodeKind.dir);

        thisTest.getWc().setItemWorkingCopyRevision("A/D/H/nu",
            Revision.SVN_INVALID_REVNUM);
        thisTest.getWc().setItemTextStatus("A/D/H/nu", Status.Kind.none);
        thisTest.getWc().setItemNodeKind("A/D/H/nu", NodeKind.unknown);
        thisTest.getWc().setItemOODInfo("A/D/H/nu", nuCommitRev, nuAuthor,
                                        nuCommitDate, NodeKind.file);

        thisTest.getWc().setItemContent("A/D/H/chi",
                                        "This is the file 'chi'.");
        thisTest.getWc().setItemOODInfo("A/D/H/chi", chiCommitRev, chiAuthor,
                                        chiCommitDate, NodeKind.file);

        thisTest.getWc().removeItem("A/D/H/psi");
        thisTest.getWc().addItem("A/D/H/psi", "This is the file 'psi'.");
        // psi was replaced with a directory
        thisTest.getWc().setItemOODInfo("A/D/H/psi", psiCommitRev, psiAuthor,
                                        psiCommitDate, NodeKind.dir);

        thisTest.getWc().setItemPropStatus("", Status.Kind.none);
        thisTest.getWc().setItemOODInfo("", psiCommitRev, psiAuthor,
                                        psiCommitDate, NodeKind.dir);

        thisTest.checkStatus(true);
    }

    /**
     * Test the basic SVNClient.checkout functionality.
     * @throws Throwable
     */
    public void testBasicCheckout() throws Throwable
    {
        // build the test setup
        OneTest thisTest = new OneTest();
        try
        {
            // obstructed checkout must fail
            client.checkout(thisTest.getUrl() + "/A", thisTest.getWCPath(),
                            null, null, Depth.infinity, false, false);
            fail("missing exception");
        }
        catch (ClientException expected)
        {
        }
        // modify file A/mu
        File mu = new File(thisTest.getWorkingCopy(), "A/mu");
        PrintWriter muWriter = new PrintWriter(new FileOutputStream(mu, true));
        muWriter.print("appended mu text");
        muWriter.close();
        thisTest.getWc().setItemTextStatus("A/mu", Status.Kind.modified);

        // delete A/B/lambda without svn
        File lambda = new File(thisTest.getWorkingCopy(), "A/B/lambda");
        lambda.delete();
        thisTest.getWc().setItemTextStatus("A/B/lambda", Status.Kind.missing);

        // remove A/D/G
        client.remove(thisTest.getWCPathSet("/A/D/G"),
                      false, false, null, null, null);
        thisTest.getWc().setItemTextStatus("A/D/G", Status.Kind.deleted);
        thisTest.getWc().setItemTextStatus("A/D/G/pi", Status.Kind.deleted);
        thisTest.getWc().setItemTextStatus("A/D/G/rho", Status.Kind.deleted);
        thisTest.getWc().setItemTextStatus("A/D/G/tau", Status.Kind.deleted);

        // check the status of the working copy
        thisTest.checkStatus();

        // recheckout the working copy
        client.checkout(thisTest.getUrl().toString(), thisTest.getWCPath(),
                   null, null, Depth.infinity, false, false);

        // deleted file should reapear
        thisTest.getWc().setItemTextStatus("A/B/lambda", Status.Kind.normal);

        // check the status of the working copy
        thisTest.checkStatus();
    }

    /**
     * Test the basic SVNClient.commit functionality.
     * @throws Throwable
     */
    public void testBasicCommit() throws Throwable
    {
        // build the test setup
        OneTest thisTest = new OneTest();

        // modify file A/mu
        File mu = new File(thisTest.getWorkingCopy(), "A/mu");
        PrintWriter muWriter = new PrintWriter(new FileOutputStream(mu, true));
        muWriter.print("appended mu text");
        muWriter.close();
        thisTest.getWc().setItemWorkingCopyRevision("A/mu", 2);
        thisTest.getWc().setItemContent("A/mu",
                thisTest.getWc().getItemContent("A/mu") + "appended mu text");
        addExpectedCommitItem(thisTest.getWCPath(),
                thisTest.getUrl().toString(), "A/mu",NodeKind.file,
                CommitItemStateFlags.TextMods);

        // modify file A/D/G/rho
        File rho = new File(thisTest.getWorkingCopy(), "A/D/G/rho");
        PrintWriter rhoWriter =
            new PrintWriter(new FileOutputStream(rho, true));
        rhoWriter.print("new appended text for rho");
        rhoWriter.close();
        thisTest.getWc().setItemWorkingCopyRevision("A/D/G/rho", 2);
        thisTest.getWc().setItemContent("A/D/G/rho",
                thisTest.getWc().getItemContent("A/D/G/rho")
                + "new appended text for rho");
        addExpectedCommitItem(thisTest.getWCPath(),
                thisTest.getUrl().toString(), "A/D/G/rho",NodeKind.file,
                CommitItemStateFlags.TextMods);

        // commit the changes
        checkCommitRevision(thisTest, "wrong revision number from commit", 2,
                            thisTest.getWCPathSet(), "log msg", Depth.infinity,
                            false, false, null, null);

        // check the status of the working copy
        thisTest.checkStatus();
    }

    /**
     * Test the basic property setting/getting functionality.
     * @throws Throwable
     */
    public void testBasicProperties() throws Throwable
    {
        OneTest thisTest = new OneTest();
        WC wc = thisTest.getWc();

        // Check getting properties the non-callback way
        String itemPath = fileToSVNPath(new File(thisTest.getWCPath(),
                                                 "iota"),
                                        false);

        byte[] BINARY_DATA = {-12, -125, -51, 43, 5, 47, 116, -72, -120,
                2, -98, -100, -73, 61, 118, 74, 36, 38, 56, 107, 45, 91, 38, 107, -87,
                119, -107, -114, -45, -128, -69, 96};
        setprop(itemPath, "abc", BINARY_DATA);
        Map<String, byte[]> properties = collectProperties(itemPath, null,
                                                    null, Depth.empty, null);

        assertTrue(Arrays.equals(BINARY_DATA, properties.get("abc")));

        wc.setItemPropStatus("iota", Status.Kind.modified);
        thisTest.checkStatus();

        // Check getting properties the callback way
        itemPath = fileToSVNPath(new File(thisTest.getWCPath(),
                                          "/A/B/E/alpha"),
                                 false);
        String alphaVal = "qrz";
        setprop(itemPath, "cqcq", alphaVal.getBytes());

        final Map<String, Map<String, byte[]>> propMaps =
                                    new HashMap<String, Map<String, byte[]>>();
        client.properties(itemPath, null, null, Depth.empty, null,
            new ProplistCallback () {
                public void singlePath(String path, Map<String, byte[]> props)
                { propMaps.put(path, props); }
            });
        Map<String, byte[]> propMap = propMaps.get(itemPath);
        for (String key : propMap.keySet())
        {
            assertEquals("cqcq", key);
            assertEquals(alphaVal, new String(propMap.get(key)));
        }

        wc.setItemPropStatus("A/B/E/alpha", Status.Kind.modified);
        thisTest.checkStatus();
    }

    /**
     * Test the basic SVNClient.update functionality.
     * @throws Throwable
     */
    public void testBasicUpdate() throws Throwable
    {
        // build the test setup. Used for the changes
        OneTest thisTest = new OneTest();

        // build the backup test setup. That is the one that will be updated
        OneTest backupTest = thisTest.copy(".backup");

        // modify A/mu
        File mu = new File(thisTest.getWorkingCopy(), "A/mu");
        PrintWriter muWriter = new PrintWriter(new FileOutputStream(mu, true));
        muWriter.print("appended mu text");
        muWriter.close();
        thisTest.getWc().setItemWorkingCopyRevision("A/mu", 2);
        thisTest.getWc().setItemContent("A/mu",
                thisTest.getWc().getItemContent("A/mu") + "appended mu text");
        addExpectedCommitItem(thisTest.getWCPath(),
                thisTest.getUrl().toString(), "A/mu",NodeKind.file,
                CommitItemStateFlags.TextMods);

        // modify A/D/G/rho
        File rho = new File(thisTest.getWorkingCopy(), "A/D/G/rho");
        PrintWriter rhoWriter =
            new PrintWriter(new FileOutputStream(rho, true));
        rhoWriter.print("new appended text for rho");
        rhoWriter.close();
        thisTest.getWc().setItemWorkingCopyRevision("A/D/G/rho", 2);
        thisTest.getWc().setItemContent("A/D/G/rho",
                thisTest.getWc().getItemContent("A/D/G/rho")
                + "new appended text for rho");
        addExpectedCommitItem(thisTest.getWCPath(),
                thisTest.getUrl().toString(), "A/D/G/rho",NodeKind.file,
                CommitItemStateFlags.TextMods);

        // commit the changes
        checkCommitRevision(thisTest, "wrong revision number from commit", 2,
                            thisTest.getWCPathSet(), "log msg", Depth.infinity,
                            false, false, null, null);

        // check the status of the working copy
        thisTest.checkStatus();

        // update the backup test
        assertEquals("wrong revision number from update",
                     update(backupTest), 2);

        // set the expected working copy layout for the backup test
        backupTest.getWc().setItemWorkingCopyRevision("A/mu", 2);
        backupTest.getWc().setItemContent("A/mu",
                backupTest.getWc().getItemContent("A/mu") + "appended mu text");
        backupTest.getWc().setItemWorkingCopyRevision("A/D/G/rho", 2);
        backupTest.getWc().setItemContent("A/D/G/rho",
                backupTest.getWc().getItemContent("A/D/G/rho")
                + "new appended text for rho");

        // check the status of the working copy of the backup test
        backupTest.checkStatus();
    }

    /**
     * Test basic SVNClient.mkdir with URL parameter functionality.
     * @throws Throwable
     */
    public void testBasicMkdirUrl() throws Throwable
    {
        // build the test setup.
        OneTest thisTest = new OneTest();

        // create Y and Y/Z directories in the repository
        addExpectedCommitItem(null, thisTest.getUrl().toString(), "Y", NodeKind.none,
                              CommitItemStateFlags.Add);
        addExpectedCommitItem(null, thisTest.getUrl().toString(), "Y/Z", NodeKind.none,
                              CommitItemStateFlags.Add);
        Set<String> urls = new HashSet<String>(2);
        urls.add(thisTest.getUrl() + "/Y");
        urls.add(thisTest.getUrl() + "/Y/Z");
        client.mkdir(urls, false, null, new ConstMsg("log_msg"), null);

        // add the new directories the expected working copy layout
        thisTest.getWc().addItem("Y", null);
        thisTest.getWc().setItemWorkingCopyRevision("Y", 2);
        thisTest.getWc().addItem("Y/Z", null);
        thisTest.getWc().setItemWorkingCopyRevision("Y/Z", 2);

        // update the working copy
        assertEquals("wrong revision from update",
                     update(thisTest), 2);

        // check the status of the working copy
        thisTest.checkStatus();
    }

    /**
     * Test the {@link SVNClientInterface.copy()} API.
     * @since 1.5
     */
    public void testCopy()
        throws SubversionException, IOException
    {
        OneTest thisTest = new OneTest();

        WC wc = thisTest.getWc();
        final Revision firstRevision = Revision.getInstance(1);
        final Revision pegRevision = null;  // Defaults to Revision.HEAD.

        // Copy files from A/B/E to A/B/F.
        String[] srcPaths = { "alpha", "beta" };
        List<CopySource> sources = new ArrayList<CopySource>(srcPaths.length);
        for (String fileName : srcPaths)
        {
            sources.add(
                new CopySource(new File(thisTest.getWorkingCopy(),
                                        "A/B/E/" + fileName).getPath(),
                               firstRevision, pegRevision));
            wc.addItem("A/B/F/" + fileName,
                       wc.getItemContent("A/B/E/" + fileName));
            wc.setItemWorkingCopyRevision("A/B/F/" + fileName, 2);
            addExpectedCommitItem(thisTest.getWCPath(),
                                 thisTest.getUrl().toString(),
                                  "A/B/F/" + fileName, NodeKind.file,
                                  CommitItemStateFlags.Add |
                                  CommitItemStateFlags.IsCopy);
        }
        client.copy(sources,
                    new File(thisTest.getWorkingCopy(), "A/B/F").getPath(),
                    true, false, false, null, null, null);

        // Commit the changes, and check the state of the WC.
        checkCommitRevision(thisTest,
                            "Unexpected WC revision number after commit", 2,
                            thisTest.getWCPathSet(), "Copy files",
                            Depth.infinity, false, false, null, null);
        thisTest.checkStatus();

        assertExpectedSuggestion(thisTest.getUrl() + "/A/B/E/alpha", "A/B/F/alpha", thisTest);

        // Now test a WC to URL copy
        List<CopySource> wcSource = new ArrayList<CopySource>(1);
        wcSource.add(new CopySource(new File(thisTest.getWorkingCopy(),
                                        "A/B").getPath(), Revision.WORKING,
                                    Revision.WORKING));
        client.copy(wcSource, thisTest.getUrl() + "/parent/A/B",
                    true, true, false, null,
                    new ConstMsg("Copy WC to URL"), null);

        // update the WC to get new folder and confirm the copy
        assertEquals("wrong revision number from update",
                     update(thisTest), 3);
    }

    /**
     * Test the {@link SVNClientInterface.move()} API.
     * @since 1.5
     */
    public void testMove()
        throws SubversionException, IOException
    {
        OneTest thisTest = new OneTest();
        WC wc = thisTest.getWc();

        // Move files from A/B/E to A/B/F.
        Set<String> relPaths = new HashSet<String>(2);
        relPaths.add("alpha");
        relPaths.add("beta");
        Set<String> srcPaths = new HashSet<String>(2);
        for (String fileName : relPaths)
        {
            srcPaths.add(new File(thisTest.getWorkingCopy(),
                                  "A/B/E/" + fileName).getPath());

            wc.addItem("A/B/F/" + fileName,
                       wc.getItemContent("A/B/E/" + fileName));
            wc.setItemWorkingCopyRevision("A/B/F/" + fileName, 2);
            addExpectedCommitItem(thisTest.getWCPath(), thisTest.getUrl().toString(),
                                  "A/B/F/" + fileName, NodeKind.file,
                                  CommitItemStateFlags.Add |
                                  CommitItemStateFlags.IsCopy);

            wc.removeItem("A/B/E/" + fileName);
            addExpectedCommitItem(thisTest.getWCPath(), thisTest.getUrl().toString(),
                                  "A/B/E/" + fileName, NodeKind.file,
                                  CommitItemStateFlags.Delete);
        }
        client.move(srcPaths,
                    new File(thisTest.getWorkingCopy(), "A/B/F").getPath(),
                    false, true, false, null, null, null);

        // Commit the changes, and check the state of the WC.
        checkCommitRevision(thisTest,
                            "Unexpected WC revision number after commit", 2,
                            thisTest.getWCPathSet(), "Move files",
                            Depth.infinity, false, false, null, null);
        thisTest.checkStatus();

        assertExpectedSuggestion(thisTest.getUrl() + "/A/B/E/alpha", "A/B/F/alpha", thisTest);
    }

    /**
     * Assert that the first merge source suggested for
     * <code>destPath</code> at {@link Revision#WORKING} and {@link
     * Revision#HEAD} is equivalent to <code>expectedSrc</code>.
     * @exception SubversionException If retrieval of the copy source fails.
     * @since 1.5
     */
    private void assertExpectedSuggestion(String expectedSrc,
                                          String destPath, OneTest thisTest)
        throws SubversionException
    {
        String wcPath = fileToSVNPath(new File(thisTest.getWCPath(),
                                               destPath), false);
        Set<String> suggestions = client.suggestMergeSources(wcPath,
                                                             Revision.WORKING);
        assertNotNull(suggestions);
        assertTrue(suggestions.size() >= 1);
        assertTrue("Copy source path not found in suggestions: " +
                   expectedSrc,
                   suggestions.contains(expectedSrc));

        // Same test using URL
        String url = thisTest.getUrl() + "/" + destPath;
        suggestions = client.suggestMergeSources(url, Revision.HEAD);
        assertNotNull(suggestions);
        assertTrue(suggestions.size() >= 1);
        assertTrue("Copy source path not found in suggestions: " +
                   expectedSrc,
                   suggestions.contains(expectedSrc));

    }

    /**
     * Tests that the passed start and end revision are contained
     * within the array of revisions.
     * @since 1.5
     */
    private void assertExpectedMergeRange(long start, long end,
                                          long[] revisions)
    {
        Arrays.sort(revisions);
        for (int i = 0; i < revisions.length; i++) {
            if (revisions[i] <= start) {
                for (int j = i; j < revisions.length; j++)
                {
                    if (end <= revisions[j])
                        return;
                }
                fail("End revision: " + end + " was not in range: " + revisions[0] +
                        " : " + revisions[revisions.length - 1]);
                return;
            }
        }
        fail("Start revision: " + start + " was not in range: " + revisions[0] +
                " : " + revisions[revisions.length - 1]);
    }

    /**
     * Test the basic SVNClient.update functionality with concurrent
     * changes in the repository and the working copy.
     * @throws Throwable
     */
    public void testBasicMergingUpdate() throws Throwable
    {
        // build the first working copy
        OneTest thisTest = new OneTest();

        // append 10 lines to A/mu
        File mu = new File(thisTest.getWorkingCopy(), "A/mu");
        PrintWriter muWriter = new PrintWriter(new FileOutputStream(mu, true));
        String muContent = thisTest.getWc().getItemContent("A/mu");
        for (int i = 2; i < 11; i++)
        {
            muWriter.print("\nThis is line " + i + " in mu");
            muContent = muContent + "\nThis is line " + i + " in mu";
        }
        muWriter.close();
        thisTest.getWc().setItemWorkingCopyRevision("A/mu", 2);
        thisTest.getWc().setItemContent("A/mu", muContent);
        addExpectedCommitItem(thisTest.getWorkingCopy().getAbsolutePath(),
                              thisTest.getUrl().toString(), "A/mu", NodeKind.file,
                              CommitItemStateFlags.TextMods);

        // append 10 line to A/D/G/rho
        File rho = new File(thisTest.getWorkingCopy(), "A/D/G/rho");
        PrintWriter rhoWriter =
            new PrintWriter(new FileOutputStream(rho, true));
        String rhoContent = thisTest.getWc().getItemContent("A/D/G/rho");
        for (int i = 2; i < 11; i++)
        {
            rhoWriter.print("\nThis is line " + i + " in rho");
            rhoContent = rhoContent + "\nThis is line " + i + " in rho";
        }
        rhoWriter.close();
        thisTest.getWc().setItemWorkingCopyRevision("A/D/G/rho", 2);
        thisTest.getWc().setItemContent("A/D/G/rho", rhoContent);
        addExpectedCommitItem(thisTest.getWCPath(),
                              thisTest.getUrl().toString(), "A/D/G/rho",
                              NodeKind.file, CommitItemStateFlags.TextMods);

        // commit the changes
        checkCommitRevision(thisTest, "wrong revision number from commit", 2,
                            thisTest.getWCPathSet(), "log msg", Depth.infinity,
                            false, false, null, null);

        // check the status of the first working copy
        thisTest.checkStatus();

        // create a backup copy of the working copy
        OneTest backupTest = thisTest.copy(".backup");

        // change the last line of A/mu in the first working copy
        muWriter = new PrintWriter(new FileOutputStream(mu, true));
        muContent = thisTest.getWc().getItemContent("A/mu");
        muWriter.print(" Appended to line 10 of mu");
        muContent = muContent + " Appended to line 10 of mu";
        muWriter.close();
        thisTest.getWc().setItemWorkingCopyRevision("A/mu", 3);
        thisTest.getWc().setItemContent("A/mu", muContent);
        addExpectedCommitItem(thisTest.getWCPath(),
                              thisTest.getUrl().toString(), "A/mu", NodeKind.file,
                              CommitItemStateFlags.TextMods);

        // change the last line of A/mu in the first working copy
        rhoWriter = new PrintWriter(new FileOutputStream(rho, true));
        rhoContent = thisTest.getWc().getItemContent("A/D/G/rho");
        rhoWriter.print(" Appended to line 10 of rho");
        rhoContent = rhoContent + " Appended to line 10 of rho";
        rhoWriter.close();
        thisTest.getWc().setItemWorkingCopyRevision("A/D/G/rho", 3);
        thisTest.getWc().setItemContent("A/D/G/rho", rhoContent);
        addExpectedCommitItem(thisTest.getWCPath(),
                              thisTest.getUrl().toString(), "A/D/G/rho",
                              NodeKind.file,
                              CommitItemStateFlags.TextMods);

        // commit these changes to the repository
        checkCommitRevision(thisTest, "wrong revision number from commit", 3,
                            thisTest.getWCPathSet(), "log msg", Depth.infinity,
                            false, false, null, null);

        // check the status of the first working copy
        thisTest.checkStatus();

        // modify the first line of A/mu in the backup working copy
        mu = new File(backupTest.getWorkingCopy(), "A/mu");
        muWriter = new PrintWriter(new FileOutputStream(mu));
        muWriter.print("This is the new line 1 in the backup copy of mu");
        muContent = "This is the new line 1 in the backup copy of mu";
        for (int i = 2; i < 11; i++)
        {
            muWriter.print("\nThis is line " + i + " in mu");
            muContent = muContent + "\nThis is line " + i + " in mu";
        }
        muWriter.close();
        backupTest.getWc().setItemWorkingCopyRevision("A/mu", 3);
        muContent = muContent + " Appended to line 10 of mu";
        backupTest.getWc().setItemContent("A/mu", muContent);
        backupTest.getWc().setItemTextStatus("A/mu", Status.Kind.modified);

        // modify the first line of A/D/G/rho in the backup working copy
        rho = new File(backupTest.getWorkingCopy(), "A/D/G/rho");
        rhoWriter = new PrintWriter(new FileOutputStream(rho));
        rhoWriter.print("This is the new line 1 in the backup copy of rho");
        rhoContent = "This is the new line 1 in the backup copy of rho";
        for (int i = 2; i < 11; i++)
        {
            rhoWriter.print("\nThis is line " + i + " in rho");
            rhoContent = rhoContent + "\nThis is line " + i + " in rho";
        }
        rhoWriter.close();
        backupTest.getWc().setItemWorkingCopyRevision("A/D/G/rho", 3);
        rhoContent = rhoContent + " Appended to line 10 of rho";
        backupTest.getWc().setItemContent("A/D/G/rho", rhoContent);
        backupTest.getWc().setItemTextStatus("A/D/G/rho", Status.Kind.modified);

        // update the backup working copy
        assertEquals("wrong revision number from update",
                     update(backupTest), 3);

        // check the status of the backup working copy
        backupTest.checkStatus();
    }

    /**
     * Test the basic SVNClient.update functionality with concurrent
     * changes in the repository and the working copy that generate
     * conflicts.
     * @throws Throwable
     */
    public void testBasicConflict() throws Throwable
    {
        // build the first working copy
        OneTest thisTest = new OneTest();

        // copy the first working copy to the backup working copy
        OneTest backupTest = thisTest.copy(".backup");

        // append a line to A/mu in the first working copy
        File mu = new File(thisTest.getWorkingCopy(), "A/mu");
        PrintWriter muWriter = new PrintWriter(new FileOutputStream(mu, true));
        String muContent = thisTest.getWc().getItemContent("A/mu");
        muWriter.print("\nOriginal appended text for mu");
        muContent = muContent + "\nOriginal appended text for mu";
        muWriter.close();
        thisTest.getWc().setItemWorkingCopyRevision("A/mu", 2);
        thisTest.getWc().setItemContent("A/mu", muContent);
        addExpectedCommitItem(thisTest.getWCPath(),
                              thisTest.getUrl().toString(), "A/mu", NodeKind.file,
                              CommitItemStateFlags.TextMods);

        // append a line to A/D/G/rho in the first working copy
        File rho = new File(thisTest.getWorkingCopy(), "A/D/G/rho");
        PrintWriter rhoWriter =
            new PrintWriter(new FileOutputStream(rho, true));
        String rhoContent = thisTest.getWc().getItemContent("A/D/G/rho");
        rhoWriter.print("\nOriginal appended text for rho");
        rhoContent = rhoContent + "\nOriginal appended text for rho";
        rhoWriter.close();
        thisTest.getWc().setItemWorkingCopyRevision("A/D/G/rho", 2);
        thisTest.getWc().setItemContent("A/D/G/rho", rhoContent);
        addExpectedCommitItem(thisTest.getWCPath(),
                              thisTest.getUrl().toString(), "A/D/G/rho", NodeKind.file,
                              CommitItemStateFlags.TextMods);

        // commit the changes in the first working copy
        checkCommitRevision(thisTest, "wrong revision number from commit", 2,
                            thisTest.getWCPathSet(), "log msg", Depth.infinity,
                            false, false, null, null);

        // test the status of the working copy after the commit
        thisTest.checkStatus();

        // append a different line to A/mu in the backup working copy
        mu = new File(backupTest.getWorkingCopy(), "A/mu");
        muWriter = new PrintWriter(new FileOutputStream(mu, true));
        muWriter.print("\nConflicting appended text for mu");
        muContent = "<<<<<<< .mine\nThis is the file 'mu'.\n"+
                    "Conflicting appended text for mu=======\n"+
                    "This is the file 'mu'.\n"+
                    "Original appended text for mu>>>>>>> .r2";
        muWriter.close();
        backupTest.getWc().setItemWorkingCopyRevision("A/mu", 2);
        backupTest.getWc().setItemContent("A/mu", muContent);
        backupTest.getWc().setItemTextStatus("A/mu", Status.Kind.conflicted);
        backupTest.getWc().addItem("A/mu.r1", "");
        backupTest.getWc().setItemNodeKind("A/mu.r1", NodeKind.unknown);
        backupTest.getWc().setItemTextStatus("A/mu.r1",
                                             Status.Kind.unversioned);
        backupTest.getWc().addItem("A/mu.r2", "");
        backupTest.getWc().setItemNodeKind("A/mu.r2", NodeKind.unknown);
        backupTest.getWc().setItemTextStatus("A/mu.r2",
                                             Status.Kind.unversioned);
        backupTest.getWc().addItem("A/mu.mine", "");
        backupTest.getWc().setItemNodeKind("A/mu.mine", NodeKind.unknown);
        backupTest.getWc().setItemTextStatus("A/mu.mine",
                                             Status.Kind.unversioned);

        // append a different line to A/D/G/rho in the backup working copy
        rho = new File(backupTest.getWorkingCopy(), "A/D/G/rho");
        rhoWriter = new PrintWriter(new FileOutputStream(rho, true));
        rhoWriter.print("\nConflicting appended text for rho");
        rhoContent = "<<<<<<< .mine\nThis is the file 'rho'.\n"+
                    "Conflicting appended text for rho=======\n"+
                    "his is the file 'rho'.\n"+
                    "Original appended text for rho>>>>>>> .r2";
        rhoWriter.close();
        backupTest.getWc().setItemWorkingCopyRevision("A/D/G/rho", 2);
        backupTest.getWc().setItemContent("A/D/G/rho", rhoContent);
        backupTest.getWc().setItemTextStatus("A/D/G/rho",
                                             Status.Kind.conflicted);
        backupTest.getWc().addItem("A/D/G/rho.r1", "");
        backupTest.getWc().setItemNodeKind("A/D/G/rho.r1", NodeKind.unknown);
        backupTest.getWc().setItemTextStatus("A/D/G/rho.r1",
                                             Status.Kind.unversioned);
        backupTest.getWc().addItem("A/D/G/rho.r2", "");
        backupTest.getWc().setItemNodeKind("A/D/G/rho.r2", NodeKind.unknown);
        backupTest.getWc().setItemTextStatus("A/D/G/rho.r2",
                                             Status.Kind.unversioned);
        backupTest.getWc().addItem("A/D/G/rho.mine", "");
        backupTest.getWc().setItemNodeKind("A/D/G/rho.mine", NodeKind.unknown);
        backupTest.getWc().setItemTextStatus("A/D/G/rho.mine",
                                             Status.Kind.unversioned);

        // update the backup working copy from the repository
        assertEquals("wrong revision number from update",
                     update(backupTest), 2);

        // check the status of the backup working copy
        backupTest.checkStatus();

        // flag A/mu as resolved
        client.resolve(backupTest.getWCPath()+"/A/mu", Depth.empty,
                ConflictResult.Choice.chooseMerged);
        backupTest.getWc().setItemTextStatus("A/mu", Status.Kind.modified);
        backupTest.getWc().removeItem("A/mu.r1");
        backupTest.getWc().removeItem("A/mu.r2");
        backupTest.getWc().removeItem("A/mu.mine");

        // flag A/D/G/rho as resolved
        client.resolve(backupTest.getWCPath()+"/A/D/G/rho", Depth.empty,
                ConflictResult.Choice.chooseMerged);
        backupTest.getWc().setItemTextStatus("A/D/G/rho",
                                             Status.Kind.modified);
        backupTest.getWc().removeItem("A/D/G/rho.r1");
        backupTest.getWc().removeItem("A/D/G/rho.r2");
        backupTest.getWc().removeItem("A/D/G/rho.mine");

        // check the status after the conflicts are flaged as resolved
        backupTest.checkStatus();
    }

    /**
     * Test the basic SVNClient.cleanup functionality.
     * Without a way to force a lock, this test just verifies
     * the method can be called succesfully.
     * @throws Throwable
     */
    public void testBasicCleanup() throws Throwable
    {
        // create a test working copy
        OneTest thisTest = new OneTest();

        // run cleanup
        client.cleanup(thisTest.getWCPath());

    }

    /**
     * Test the basic SVNClient.revert functionality.
     * @throws Throwable
     */
    public void testBasicRevert() throws Throwable
    {
        // create a test working copy
        OneTest thisTest = new OneTest();

        // modify A/B/E/beta
        File file = new File(thisTest.getWorkingCopy(), "A/B/E/beta");
        PrintWriter pw = new PrintWriter(new FileOutputStream(file, true));
        pw.print("Added some text to 'beta'.");
        pw.close();
        thisTest.getWc().setItemTextStatus("A/B/E/beta", Status.Kind.modified);

        // modify iota
        file = new File(thisTest.getWorkingCopy(), "iota");
        pw = new PrintWriter(new FileOutputStream(file, true));
        pw.print("Added some text to 'iota'.");
        pw.close();
        thisTest.getWc().setItemTextStatus("iota", Status.Kind.modified);

        // modify A/D/G/rho
        file = new File(thisTest.getWorkingCopy(), "A/D/G/rho");
        pw = new PrintWriter(new FileOutputStream(file, true));
        pw.print("Added some text to 'rho'.");
        pw.close();
        thisTest.getWc().setItemTextStatus("A/D/G/rho", Status.Kind.modified);

        // create new file A/D/H/zeta and add it to subversion
        file = new File(thisTest.getWorkingCopy(), "A/D/H/zeta");
        pw = new PrintWriter(new FileOutputStream(file, true));
        pw.print("Added some text to 'zeta'.");
        pw.close();
        thisTest.getWc().addItem("A/D/H/zeta", "Added some text to 'zeta'.");
        thisTest.getWc().setItemTextStatus("A/D/H/zeta", Status.Kind.added);
        client.add(file.getAbsolutePath(), Depth.empty, false, false, false);

        // test the status of the working copy
        thisTest.checkStatus();

        // revert the changes
        client.revert(thisTest.getWCPath()+"/A/B/E/beta", Depth.empty, null);
        thisTest.getWc().setItemTextStatus("A/B/E/beta", Status.Kind.normal);
        client.revert(thisTest.getWCPath()+"/iota", Depth.empty, null);
        thisTest.getWc().setItemTextStatus("iota", Status.Kind.normal);
        client.revert(thisTest.getWCPath()+"/A/D/G/rho", Depth.empty, null);
        thisTest.getWc().setItemTextStatus("A/D/G/rho", Status.Kind.normal);
        client.revert(thisTest.getWCPath()+"/A/D/H/zeta", Depth.empty, null);
        thisTest.getWc().setItemTextStatus("A/D/H/zeta",
                Status.Kind.unversioned);
        thisTest.getWc().setItemNodeKind("A/D/H/zeta", NodeKind.unknown);

        // test the status of the working copy
        thisTest.checkStatus();

        // delete A/B/E/beta and revert the change
        file = new File(thisTest.getWorkingCopy(), "A/B/E/beta");
        file.delete();
        client.revert(file.getAbsolutePath(), Depth.empty, null);

        // resurected file should not be readonly
        assertTrue("reverted file is not readonly",
                file.canWrite()&& file.canRead());

        // test the status of the working copy
        thisTest.checkStatus();

        // create & add the directory X
        client.mkdir(thisTest.getWCPathSet("/X"), false, null, null, null);
        thisTest.getWc().addItem("X", null);
        thisTest.getWc().setItemTextStatus("X", Status.Kind.added);

        // test the status of the working copy
        thisTest.checkStatus();

        // remove & revert X
        removeDirOrFile(new File(thisTest.getWorkingCopy(), "X"));
        client.revert(thisTest.getWCPath()+"/X", Depth.empty, null);
        thisTest.getWc().removeItem("X");

        // test the status of the working copy
        thisTest.checkStatus();

        // delete the directory A/B/E
        client.remove(thisTest.getWCPathSet("/A/B/E"), true,
                      false, null, null, null);
        removeDirOrFile(new File(thisTest.getWorkingCopy(), "A/B/E"));
        thisTest.getWc().setItemTextStatus("A/B/E", Status.Kind.deleted);
        thisTest.getWc().setItemTextStatus("A/B/E/alpha", Status.Kind.deleted);
        thisTest.getWc().setItemTextStatus("A/B/E/beta", Status.Kind.deleted);

        // test the status of the working copy
        thisTest.checkStatus();

        // revert A/B/E -> this will resurect it
        client.revert(thisTest.getWCPath()+"/A/B/E", Depth.infinity, null);
        thisTest.getWc().setItemTextStatus("A/B/E", Status.Kind.normal);
        thisTest.getWc().setItemTextStatus("A/B/E/alpha", Status.Kind.normal);
        thisTest.getWc().setItemTextStatus("A/B/E/beta", Status.Kind.normal);

        // test the status of the working copy
        thisTest.checkStatus();
    }

    /**
     * Test the basic SVNClient.switch functionality.
     * @throws Throwable
     */
    public void testBasicSwitch() throws Throwable
    {
        // create the test working copy
        OneTest thisTest = new OneTest();

        // switch iota to A/D/gamma
        String iotaPath = thisTest.getWCPath() + "/iota";
        String gammaUrl = thisTest.getUrl() + "/A/D/gamma";
        thisTest.getWc().setItemContent("iota",
                greekWC.getItemContent("A/D/gamma"));
        thisTest.getWc().setItemIsSwitched("iota", true);
        client.doSwitch(iotaPath, gammaUrl, null, Revision.HEAD, Depth.unknown,
                        false, false, false, true);

        // check the status of the working copy
        thisTest.checkStatus();

        // switch A/D/H to /A/D/G
        String adhPath = thisTest.getWCPath() + "/A/D/H";
        String adgURL = thisTest.getUrl() + "/A/D/G";
        thisTest.getWc().setItemIsSwitched("A/D/H",true);
        thisTest.getWc().removeItem("A/D/H/chi");
        thisTest.getWc().removeItem("A/D/H/omega");
        thisTest.getWc().removeItem("A/D/H/psi");
        thisTest.getWc().addItem("A/D/H/pi",
                thisTest.getWc().getItemContent("A/D/G/pi"));
        thisTest.getWc().addItem("A/D/H/rho",
                thisTest.getWc().getItemContent("A/D/G/rho"));
        thisTest.getWc().addItem("A/D/H/tau",
                thisTest.getWc().getItemContent("A/D/G/tau"));
        client.doSwitch(adhPath, adgURL, null, Revision.HEAD, Depth.files,
                        false, false, false, true);

        // check the status of the working copy
        thisTest.checkStatus();
    }

    /**
     * Test the basic SVNClient.remove functionality.
     * @throws Throwable
     */
    public void testBasicDelete() throws Throwable
    {
        // create the test working copy
        OneTest thisTest = new OneTest();

        // modify A/D/H/chi
        File file = new File(thisTest.getWorkingCopy(), "A/D/H/chi");
        Set<String> pathSet = new HashSet<String>();
        PrintWriter pw = new PrintWriter(new FileOutputStream(file, true));
        pw.print("added to chi");
        pw.close();
        thisTest.getWc().setItemTextStatus("A/D/H/chi", Status.Kind.modified);

        // set a property on A/D/G/rho file
        pathSet.clear();
        pathSet.add(thisTest.getWCPath()+"/A/D/G/rho");
        client.propertySetLocal(pathSet, "abc", (new String("def")).getBytes(),
                                Depth.infinity, null, false);
        thisTest.getWc().setItemPropStatus("A/D/G/rho", Status.Kind.modified);

        // set a property on A/B/F directory
        setprop(thisTest.getWCPath()+"/A/B/F", "abc", "def");
        thisTest.getWc().setItemPropStatus("A/B/F", Status.Kind.modified);

        // create a unversioned A/C/sigma file
        file = new File(thisTest.getWCPath(),"A/C/sigma");
        pw = new PrintWriter(new FileOutputStream(file));
        pw.print("unversioned sigma");
        pw.close();
        thisTest.getWc().addItem("A/C/sigma", "unversioned sigma");
        thisTest.getWc().setItemTextStatus("A/C/sigma", Status.Kind.unversioned);
        thisTest.getWc().setItemNodeKind("A/C/sigma", NodeKind.unknown);

        // create unversioned directory A/C/Q
        file = new File(thisTest.getWCPath(), "A/C/Q");
        file.mkdir();
        thisTest.getWc().addItem("A/C/Q", null);
        thisTest.getWc().setItemNodeKind("A/C/Q", NodeKind.unknown);
        thisTest.getWc().setItemTextStatus("A/C/Q", Status.Kind.unversioned);

        // create & add the directory A/B/X
        file = new File(thisTest.getWCPath(), "A/B/X");
        pathSet.clear();
        pathSet.add(file.getAbsolutePath());
        client.mkdir(pathSet, false, null, null, null);
        thisTest.getWc().addItem("A/B/X", null);
        thisTest.getWc().setItemTextStatus("A/B/X", Status.Kind.added);

        // create & add the file A/B/X/xi
        file = new File(file, "xi");
        pw = new PrintWriter(new FileOutputStream(file));
        pw.print("added xi");
        pw.close();
        client.add(file.getAbsolutePath(), Depth.empty, false, false, false);
        thisTest.getWc().addItem("A/B/X/xi", "added xi");
        thisTest.getWc().setItemTextStatus("A/B/X/xi", Status.Kind.added);

        // create & add the directory A/B/Y
        file = new File(thisTest.getWCPath(), "A/B/Y");
        pathSet.clear();
        pathSet.add(file.getAbsolutePath());
        client.mkdir(pathSet, false, null, null, null);
        thisTest.getWc().addItem("A/B/Y", null);
        thisTest.getWc().setItemTextStatus("A/B/Y", Status.Kind.added);

        // test the status of the working copy
        thisTest.checkStatus();

        // the following removes should all fail without force

        try
        {
            // remove of A/D/H/chi without force should fail, because it is
            // modified
            client.remove(thisTest.getWCPathSet("/A/D/H/chi"),
                    false, false, null, null, null);
            fail("missing exception");
        }
        catch(ClientException expected)
        {
        }

        try
        {
            // remove of A/D/H without force should fail, because A/D/H/chi is
            // modified
            client.remove(thisTest.getWCPathSet("/A/D/H"),
                    false, false, null, null, null);
            fail("missing exception");
        }
        catch(ClientException expected)
        {
        }

        try
        {
            // remove of A/D/G/rho without force should fail, because it has
            // a new property
            client.remove(thisTest.getWCPathSet("/A/D/G/rho"),
                    false, false, null, null, null);
            fail("missing exception");
        }
        catch(ClientException expected)
        {
        }

        try
        {
            // remove of A/D/G without force should fail, because A/D/G/rho has
            // a new property
            client.remove(thisTest.getWCPathSet("/A/D/G"),
                    false, false, null, null, null);
            fail("missing exception");
        }
        catch(ClientException expected)
        {
        }

        try
        {
            // remove of A/B/F without force should fail, because it has
            // a new property
            client.remove(thisTest.getWCPathSet("/A/B/F"),
                    false, false, null, null, null);
            fail("missing exception");
        }
        catch(ClientException expected)
        {
        }

        try
        {
            // remove of A/B without force should fail, because A/B/F has
            // a new prop
