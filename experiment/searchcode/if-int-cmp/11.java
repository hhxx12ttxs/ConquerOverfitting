/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
 * or http://www.opensolaris.org/os/licensing.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at usr/src/OPENSOLARIS.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */
/*
 * ident	"%Z%%M%	%I%	%E% SMI"
 *
 * Copyright 2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/**
 * GUI interface for Kerberos KDC
 */

// Java Workshop stuff
import sunsoft.jws.visual.rt.base.*;
import sunsoft.jws.visual.rt.base.Global;
import sunsoft.jws.visual.rt.awt.TabbedFolder;
import sunsoft.jws.visual.rt.awt.TextList;
import sunsoft.jws.visual.rt.awt.StringVector;
import sunsoft.jws.visual.rt.shadow.java.awt.*;

// Regular JDK stuff
import java.awt.*;
import java.awt.event.*;
import java.util.EventListener;
import java.util.Properties;
import java.util.Vector;
import java.util.Random;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

// Stuff to support I18N
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import java.util.Enumeration;

public class KdcGui extends Group {
    
    // Basics
    private KdcGuiRoot gui;
    private Krb5Conf kc;
    private Principal prin = null;
    private Policy pol = null;
    private Defaults defaults = null;
    private Defaults olddefaults = null;
    public Frame defaultsEditingFrame = null; // public since used
    // by ContextHelp class
    public Frame realMainFrame = null;
    public Frame realLoginFrame = null;
    
    public Kadmin Kadmin = null;
    
    // Privileges stuff: corresponds to ADMCIL set in kdc.conf
    public int privs = 0;
    public static final int PRIV_ADD	= 0x02;		// KADM5_PRIV_ADD
    public static final int PRIV_DELETE	= 0x08;		// KADM5_PRIV_DELETE
    public static final int PRIV_MODIFY	= 0x04;		// KADM5_PRIV_MODIFY
    public static final int PRIV_CHANGEPW	= 0x20;	// KADM5_PRIV_CPW
    public static final int PRIV_INQUIRE	= 0x01;	// KADM5_PRIV_GET
    public static final int PRIV_LIST	= 0x10;		// KADM5_PRIV_LIST
    public boolean noLists = false;
    
    // For modal warning dialog and context-sensitive help dialog
    private Dialog dialog;
    public ContextHelp cHelp = null; // tweaked from ContextHelp when
    // it is dismissed
    
    private static Toolkit toolkit;
    
    // For showDataFormatError() to determine what kind of error to show
    
    public static final int DURATION_DATA = 1;
    public static final int DATE_DATA = 2;
    public static final int NUMBER_DATA = 3;
    
    private static String[] durationErrorText = null;
    private static String[] dateErrorText = null;
    private static String[] numberErrorText = null;
    
    // For date & time helper dialogs
    private DateTimeDialog dateTimeDialog = null;
    private DurationHelper durationHelper = null;

    // For the encryption list helper dialog
    private EncListDialog encListDialog = null;
    
    // Important defaults and current settings
    private String DefName = null;
    private String DefRealm = null;
    private String DefServer = null;
    private String DefPort = "0";
    private String CurName, CurPass, CurRealm, CurServer;
    private int CurPort;
    private String CurPrincipal;
    private String CurPolicy;
    private String curPrPattern = "";
    private String curPoPattern = "";
    private int curPrListPos = 0;
    private int curPoListPos = 0;
    private String[] principalList = null;
    private Date principalListDate = new Date(0);
    private String[] policyList = null;
    private Date policyListDate = new Date(0);
    private static final long A_LONG_TIME = 1000 * 60 * 60 * 24 * 365;
    
    // General state variables
    private boolean prSelValid = false;
    private String[] prMulti = null;
    private boolean prNeedSave = false;
    private boolean poSelValid = false;
    private String[] poMulti = null;
    private boolean poNeedSave = false;
    private boolean glNeedSave = false;
    private boolean firsttime = true;
    private boolean prnameEditable = false;
    private boolean ponameEditable = false;
    
    // Support for context-sensitive help
    private static final int BUTTON_ACTION = 1;
    private static final int BUTTON_MOUSE = 2;
    private static final int TEXTFIELD_ACTION = 3;
    private static final int TEXTFIELD_MOUSE = 4;
    private static final int TEXTFIELD_KEY = 5;
    private static final int CHOICE_ITEM = 6;
    private static final int CHOICE_MOUSE = 7;
    private static final int CHECKBOX_ITEM = 8;
    private static final int CHECKBOX_MOUSE = 9;
    private static final int LABEL_MOUSE = 10;
    private static final int WINDOW_LISTENER = 11;
    
    private boolean loginListeners = false;
    private Vector LoginNormal = null;
    private Vector LoginHelp = null;
    private Vector LoginFixers = null;
    private Vector MainNormal = null;
    private Vector MainHelp = null;
    private Vector MainFixers = null;
    private Vector defaultsNormal = null;
    private Vector defaultsHelp = null;
    private Vector defaultsFixers = null;
    public boolean loginHelpMode = false;
    public boolean mainHelpMode = false;
    public boolean defaultsHelpMode = false;
    
    // For Principal and Policy Keystroke listeners
    private static final int PRINCIPAL_EDITING = 1;
    private static final int POLICY_EDITING = 2;
    private static final int DEFAULTS_EDITING = 3;
    private static final int PRINCIPAL_LIST = 4;
    private static final int POLICY_LIST = 5;
    
    // For status line
    private String OpString = "";
    private String ModeString = "";
    private String SaveString = "";
    
    // For I18N
    private static ResourceBundle rb;
    private static ResourceBundle hrb;
    private static DateFormat df;
    private static NumberFormat nf;
    
    private static String neverString;
    
    // For general pupose help
    Process browserProcess;
    String helpIndexFile = "file:/usr/lib/krb5/HelpIndex.html";
    
    // For performance monitoring
    boolean perfmon = false;
    Date pdateFirst;
    Date pdateAfterKrb5Conf;
    Date pdateEndGuiRoot;
    Date pdateBeginStartGroup;
    Date pdateStringsDone;
    Date pdateLoginReady;
    Date pdateLoginDone;
    Date pdateSessionUp;
    Date pdatePreMainShow;
    Date pdatePostMainShow;
    Date pdateMainActive;
    Date pdateStartPlist;
    Date pdateHavePlist;
    Date pdateEmptyPlist;
    Date pdateDonePlist;
    
    public void reportTime(String s0, Date curr, Date prev) {
        if (!perfmon)
            return;
        String s1 = curr.toString();
        long curdiff = curr.getTime() - prev.getTime();
        String s2 = (new Long(curdiff)).toString();
        long cumdiff = curr.getTime() - pdateFirst.getTime();
        String s3 = (new Long(cumdiff)).toString();
        System.out.println(s0+s1+" delta "+s2+" cume "+s3);
    }
    
    public void reportStartTimes() {
        if (!perfmon)
            return;
        System.out.println("");
        reportTime("First timestamp: ", pdateFirst, pdateFirst);
        reportTime("After krb5.conf: ", pdateAfterKrb5Conf, pdateFirst);
        reportTime("KdcGuiRoot done: ", pdateEndGuiRoot, pdateAfterKrb5Conf);
        reportTime("At startGroup  : ", pdateBeginStartGroup, pdateEndGuiRoot);
        reportTime("Strings set up : ", pdateStringsDone, pdateBeginStartGroup);
        reportTime("Login ready    : ", pdateLoginReady, pdateStringsDone);
        reportTime("Login complete : ", pdateLoginDone, pdateLoginReady);
        reportTime("Session set up : ", pdateSessionUp, pdateLoginDone);
        reportTime("Start main win : ", pdatePreMainShow, pdateSessionUp);
        reportTime("Done main win  : ", pdatePostMainShow, pdatePreMainShow);
        reportTime("Main win active: ", pdateMainActive, pdatePostMainShow);
    }
    
    /**
     * Sample method call ordering during a group's lifetime:
     *
     * Constructor
     * initRoot
     * initGroup
     * (setOnGroup and getOnGroup may be called at any time in any
     *  order after initGroup has been called)
     * createGroup
     * showGroup/hideGroup + startGroup/stopGroup
     * destroyGroup
     */
    
    /**
     * The constructor sets up defaults for login screen
     *
     */
    public KdcGui() {
        
        /*
         * Set up defaults from /etc/krb5/krb5.conf
         */
        
        pdateFirst = new Date();
        DefName = System.getProperty("user.name" /* NOI18N */)+
	    "/admin" /* NOI18N */;
        kc = new Krb5Conf();
        DefRealm = kc.getDefaultRealm();
        DefServer = kc.getRealmServer(DefRealm);
        DefPort = kc.getRealmPort(DefRealm);
        pdateAfterKrb5Conf = new Date();
        
        /*
         * Take care of Java Workshop attribute plumbing
         */
        addForwardedAttributes();
    }
    
    /**
     * Inherited from the Java Workshop skeleton
     *
     */
    protected Root initRoot() {
        /*
         * Initialize the gui components
         */
        gui = new KdcGuiRoot(this);
        pdateEndGuiRoot = new Date();
        
        /*
         * Take care of Java Workshop attribute plumbing.
         */
        addAttributeForward(gui.getMainChild());
        
        initLoginStrings();
        initMainStrings();
        pdateStringsDone = new Date();
        return gui;
    }
    
    /**
     * Set up the login screen properly.
     *
     */
    protected void startGroup() {
        pdateBeginStartGroup = new Date();
        realLoginFrame = (Frame)gui.loginframe.getBody();
        realLoginFrame.setTitle(getString("SEAM Administration Login"));
        setLoginDefaults();
        pdateLoginReady = new Date();
    }
    
    /**
     * All cleanup done here.
     */
    protected void stopGroup() {
        killHelpBrowser();
    }
    
    
    /**
     * Callbacks from Java workshop to decide whether to take the action
     * or show appropriate help for it.
     * 
     * 1. Actions that are triggered from all three - mainframe,
     *    loginframe, and defaultsEditingFrame - are: context sensitive help.
     * 2. Actions that are triggered only from mainframe are: printing,
     *    logging out, edit preferences.
     * 3. Actions that are triggered from mainframe and loginframe are:
     *    exit, general help, context sensitive help, about.
     */
    
    
    // All three frames
    
    public void checkContextSensitiveHelp(Frame frame) {
        if ((loginHelpMode && frame == realLoginFrame)
            || (mainHelpMode && frame == realMainFrame)
	    || (defaultsHelpMode && frame == defaultsEditingFrame))
	    showHelp("ContextSensitiveHelp");
        else
            contextHelp(frame);
    }
    
    // Mainframe only
    
    public void checkPrintCurPr() {
        if (mainHelpMode)
            showHelp("PrintCurrentPrincipal");
        else
            printCurPr();
    }
    
    public void checkPrintCurPol() {
        if (mainHelpMode)
            showHelp("PrintCurrentPolicy");
        else
            printCurPol();
    }
    
    public void checkPrintPrList() {
        if (mainHelpMode)
            showHelp("PrintPrincipalList");
        else
            printPrList();
    }
    
    public void checkPrintPoList() {
        if (mainHelpMode)
            showHelp("PrintPolicyList");
        else
            printPoList();
    }
    
    public void checkLogout() {
        if (mainHelpMode)
            showHelp("Logout");
        else if (okayToLeave(realMainFrame))
            logout();
    }
    
    public void checkEditPreferences() {
        if (mainHelpMode)
            showHelp("EditPreferences");
        else
            editPreferences();
    }
    
    public void checkRefreshPrincipals() {
        if (mainHelpMode)
            showHelp("RefreshPrincipals");
        else {
            principalList = null;
            fillPrincipalList(curPrPattern);
        }
    }
    
    public void checkRefreshPolicies() {
        if (mainHelpMode)
            showHelp("RefreshPolicies");
        else {
            policyList = null;
            fillPolicyList(curPoPattern);
        }
    }
    
    // Mainframe and loginframe
    
    public void checkExit(Frame frame) {
        if ((loginHelpMode && frame == realLoginFrame)
            || (mainHelpMode && frame == realMainFrame))
	    showHelp("Exit");
        else if (okayToLeave(frame))
            exit();
    }
    
    public void checkHelp(Frame frame) {
        if ((loginHelpMode && frame == realLoginFrame)
            || (mainHelpMode && frame == realMainFrame))
	    showHelp("HelpBrowser");
        else
            showHelpBrowser(frame);
    }
    
    public void checkAbout(Frame frame) {
        if ((loginHelpMode && frame == realLoginFrame)
            || (mainHelpMode && frame == realMainFrame))
	    showHelp("About");
        else
            doAbout(frame);
    }
    
    public boolean okayToLeave(Frame frame) {
        if (prNeedSave || poNeedSave || glNeedSave) {
            String text[] = {getString("You are about to lose changes."),
			     getString("Click Save to commit changes, "
				       +"Discard to discard changes, "
				       +"or Cancel to continue editing.")};
            String resp = confirmSave(frame, text);
            if (resp.equals(getString("Cancel")))
                return false;
            else if (resp.equals(getString("Save"))) {
                if (prNeedSave)
                    if (!prDoSave())
			return false; // found an error so cannot leave
                if (poNeedSave)
                    if (!poDoSave())
			return false; // found an error so cannot leave
                if (glNeedSave)
                    glDoSave(true);
            } else
                prNeedSave = poNeedSave = glNeedSave = false;
        }
        return true;
    }
    
    /**
     * We use the JDK 1.1 event model for most of our events, but
     * we do still need to handle old-style events because the
     * tabbed folder and the card panel(supplied by Java Workshop)
     * are not compatible with the new event model.  We use the
     * callouts from Java Workshop to deal with the card panel,
     * but we need to have some code here to do the right thing
     * when the user selects a new tab in the tabbed folder.
     *
     * It is important that not too many conditions are tested here,
     * because all events flow through this code path.
     *
     */
    public boolean handleEvent(Message msg, Event evt) {
        
        /*
         * Look for events from the principal and policy list.
         */
        
        if (evt.target == gui.Prlist.getBody()) {
            if (mainHelpMode) {
                if (evt.id == Event.ACTION_EVENT
		    || evt.id == Event.LIST_SELECT) {
                    restorePrListSelection();
                    showHelp(((Component)gui.Prlist.getBody()).getName());
                }
            } // end of help mode
            else if (evt.id == Event.ACTION_EVENT)
                prModify();
            else if (evt.id == Event.LIST_SELECT)
                lookAtPrList();
            return true;
        } // end of Prlist
        
        if (evt.target == gui.Pollist.getBody()) {
            if (mainHelpMode) {
                if (evt.id == Event.ACTION_EVENT
		    || evt.id == Event.LIST_SELECT) {
                    restorePoListSelection();
                    showHelp(((Component)gui.Pollist.getBody()).getName());
                }
            } // end of help mode
            else if (evt.id == Event.ACTION_EVENT)
                poSelected();
            else if (evt.id == Event.LIST_SELECT)
                lookAtPoList();
            return true;
        } // end of Pollist
        
        /*
         * Look for a unique event from the tabbed folder component;
         * if I see it, I know I have a chance to disallow a switch.
         * This makes sure data is saved before leaving a tab.
         */
        if (evt.id == TabbedFolder.CONFIRM_SWITCH) {
            // System.out.println("Got confirm for "+evt.arg);
            String e = (String)evt.arg;
            if (!mainHelpMode && okayToLeave(realMainFrame) == false) {
                // System.out.println("Denying switch");
                ((TabbedFolder)gui.tabbedfolder1.getBody()).cancelSwitch();
            }
            /*
             * Okay with switch; make sure the data is up to date
             */
            else if (e.compareTo(getString("Principals")) == 0) {
                if (mainHelpMode) {
                    showHelp("PrincipalTab");
                    ((TabbedFolder)gui.tabbedfolder1.getBody()).cancelSwitch();
                } else {
                    showPrincipalList(curPrPattern);
                    disablePolicyPrinting();
                }
            } else if (e.compareTo(getString("Policies")) == 0) {
                if (mainHelpMode) {
                    showHelp("PolicyTab");
                    ((TabbedFolder)gui.tabbedfolder1.getBody()).cancelSwitch();
                } else {
                    showPolicyList(curPoPattern);
                    disablePrincipalPrinting();
                }
            }
        }
        return super.handleEvent(msg, evt);
    }
    
    /*
     * New methods for the admin gui login screen.
     */
    
    /**
     * Set strings on login screen to their I18N'd values
     *
     */
    public void initLoginStrings() {
        gui.File2.set("text" /* NOI18N */, getString("File"));
        gui.Exit2.set("text" /* NOI18N */, getString("Exit"));
        gui.menu1.set("text" /* NOI18N */, getString("Help"));
        gui.browserHelp1.set("text" /* NOI18N */, getString("Help Contents"));
        gui.Context2.set("text" /* NOI18N */,
			 getString("Context-Sensitive Help"));
        gui.About2.set("text" /* NOI18N */, getString("About"));
        gui.LoginNameLabel.set("text" /* NOI18N */,
			       getString("Principal Name:"));
        gui.LoginPassLabel.set("text" /* NOI18N */, getString("Password:"));
        gui.LoginRealmLabel.set("text" /* NOI18N */, getString("Realm:"));
        gui.LoginServerLabel.set("text" /* NOI18N */, getString("Master KDC:"));
        gui.LoginOK.set("text" /* NOI18N */, getString("OK"));
        gui.LoginStartOver.set("text" /* NOI18N */, getString("Start Over"));
    }
    
    /**
     * Set strings on main screen to their I18N'd values
     *
     */
    public void initMainStrings() {
        gui.mainframe.set("title" /* NOI18N */,
			  getString("SEAM Administration Tool"));
        gui.File.set("text" /* NOI18N */, getString("File"));
        gui.Print.set("text" /* NOI18N */, getString("Print"));
        gui.PrintCurPr.set("text" /* NOI18N */, getString("Current Principal"));
        gui.PrintCurPol.set("text" /* NOI18N */, getString("Current Policy"));
        gui.PrintPrlist.set("text" /* NOI18N */, getString("Principal List"));
        gui.PrintPollist.set("text" /* NOI18N */, getString("Policy List"));
        gui.logout.set("text" /* NOI18N */, getString("Log Out"));
        gui.Exit.set("text" /* NOI18N */, getString("Exit"));
        gui.editMenu.set("text" /* NOI18N */, getString("Edit"));
        gui.editPreferences.set("text" /* NOI18N */,
				getString("Properties..."));
        gui.menu2.set("text" /* NOI18N */, getString("Refresh"));
        gui.refreshPrincipals.set("text" /* NOI18N */,
				  getString("Principal List"));
        gui.refreshPolicies.set("text" /* NOI18N */, getString("Policy List"));
        gui.Help.set("text" /* NOI18N */, getString("Help"));
        gui.browserHelp2.set("text" /* NOI18N */, getString("Help Contents"));
        gui.Context.set("text" /* NOI18N */,
			getString("Context-Sensitive Help"));
        gui.About.set("text" /* NOI18N */, getString("About"));
        
        gui.Prlisttab.set("layoutName", getString("Principals"));
        gui.Pollisttab.set("layoutName", getString("Policies"));
        
        gui.PrListLabel.set("text" /* NOI18N */, getString("Principal List"));
        gui.PrSearchLab.set("text" /* NOI18N */, getString("Filter Pattern:"));
        gui.PrListClear.set("text" /* NOI18N */, getString("Clear Filter"));
        gui.PrListModify.set("text" /* NOI18N */, getString("Modify"));
        gui.PrListAdd.set("text" /* NOI18N */, getString("Create New"));
        gui.PrListDelete.set("text" /* NOI18N */, getString("Delete"));
        gui.PrListDuplicate.set("text" /* NOI18N */, getString("Duplicate"));
        
        gui.PrBasicLabel.set("text" /* NOI18N */,
			     getString("Principal Basics"));
        gui.PrNameLabel1.set("text" /* NOI18N */, getString("Principal Name:"));
        gui.LabelBarGeneral.set("text" /* NOI18N */, getString("General"));
        gui.PrCommentsLabel.set("text" /* NOI18N */, getString("Comments:"));
        gui.PrPolicyLabel.set("text" /* NOI18N */, getString("Policy:"));
        gui.PrPasswordLabel.set("text" /* NOI18N */, getString("Password:"));
        gui.PrBasicRandomPw.set("text" /* NOI18N */,
				getString("Generate Random Password"));
        gui.EncListLabel.set("text" /* NOI18N */,
			getString("Encryption Key Types:"));
        gui.LabelBarPrincipal.set("text" /* NOI18N */,
				  getString("Admin History"));
        gui.PrLastChangedTimeLabel.set("text" /* NOI18N */,
				       getString("Last Principal Change:"));
        gui.PrLastChangedByLabel.set("text" /* NOI18N */,
				     getString("Last Changed By:"));
        gui.PrExpiryLabel.set("text" /* NOI18N */,
			      getString("Account Expires:"));
        gui.PrBasicSave.set("text" /* NOI18N */, getString("Save"));
        gui.PrBasicPrevious.set("text" /* NOI18N */, getString("Previous"));
        gui.PrBasicNext.set("text" /* NOI18N */, getString("Next"));
        gui.PrBasicCancel.set("text" /* NOI18N */, getString("Cancel"));
        
        gui.PrDetailLabel.set("text" /* NOI18N */,
			      getString("Principal Details"));
        gui.LabelBarPassword.set("text" /* NOI18N */, getString("Password"));
        gui.PrLastSuccessLabel.set("text" /* NOI18N */,
				   getString("Last Success:"));
        gui.PrLastFailureLabel.set("text" /* NOI18N */,
				   getString("Last Failure:"));
        gui.PrFailureCountLabel.set("text" /* NOI18N */,
				    getString("Failure Count:"));
        gui.PrPwLastChangedLabel.set("text" /* NOI18N */,
				     getString("Last Password Change:"));
        gui.PrPwExpiryLabel.set("text" /* NOI18N */,
				getString("Password Expires:"));
        gui.PrKvnoLabel.set("text" /* NOI18N */, getString("Key Version:"));
        gui.LabelBarTicket.set("text" /* NOI18N */,
			       getString("Ticket Lifetimes"));
        gui.PrMaxTicketLifetimeLabel.set("text" /* NOI18N */,
				 getString("Maximum Lifetime (seconds):"));
        gui.PrMaxTicketRenewalLabel.set("text" /* NOI18N */,
				getString("Maximum Renewal (seconds):"));
        gui.PrDetailSave.set("text" /* NOI18N */, getString("Save"));
        gui.PrDetailPrevious.set("text" /* NOI18N */, getString("Previous"));
        gui.PrDetailNext.set("text" /* NOI18N */, getString("Next"));
        gui.PrDetailCancel.set("text" /* NOI18N */, getString("Cancel"));
        
        gui.PrFlagLabel.set("text" /* NOI18N */, getString("Principal Flags"));
        gui.LabelBarSecurity.set("text" /* NOI18N */, getString("Security"));
        
        gui.PrLockAcct.set("text" /* NOI18N */,
			   Flags.getLabel(Flags.DISALLOW_ALL_TIX));
        gui.PrForcePwChange.set("text" /* NOI18N */,
				Flags.getLabel(Flags.REQUIRES_PWCHANGE));
        gui.LabelBarTickets.set("text" /* NOI18N */, getString("Ticket"));
        gui.PrAllowPostdated.set("text" /* NOI18N */,
				 Flags.getLabel(Flags.DISALLOW_POSTDATED));
        gui.PrAllowForwardable.set("text" /* NOI18N */,
				   Flags.getLabel(Flags.DISALLOW_FORWARDABLE));
        gui.PrAllowRenewable.set("text" /* NOI18N */,
				 Flags.getLabel(Flags.DISALLOW_RENEWABLE));
        gui.PrAllowProxiable.set("text" /* NOI18N */,
				 Flags.getLabel(Flags.DISALLOW_PROXIABLE));
        gui.PrAllowSvr.set("text" /* NOI18N */,
			   Flags.getLabel(Flags.DISALLOW_SVR));
        gui.LabelBarMiscellany.set("text" /* NOI18N */,
				   getString("Miscellaneous"));
        gui.PrAllowTGT.set("text" /* NOI18N */,
			   Flags.getLabel(Flags.DISALLOW_TGT_BASED));
        gui.PrAllowDupAuth.set("text" /* NOI18N */,
			       Flags.getLabel(Flags.DISALLOW_DUP_SKEY));
        gui.PrRequirePreAuth.set("text" /* NOI18N */,
				 Flags.getLabel(Flags.REQUIRE_PRE_AUTH));
        gui.PrRequireHwPreAuth.set("text" /* NOI18N */,
				   Flags.getLabel(Flags.REQUIRE_HW_AUTH));
        gui.PrFlagsSave.set("text" /* NOI18N */, getString("Save"));
        gui.PrFlagsPrevious.set("text" /* NOI18N */, getString("Previous"));
        gui.PrFlagsNext.set("text" /* NOI18N */, getString("Done"));
        gui.PrFlagsCancel.set("text" /* NOI18N */, getString("Cancel"));
        
        gui.PoListLabel.set("text" /* NOI18N */, getString("Policy List"));
        gui.PoListPatternLabel.set("text" /* NOI18N */,
				   getString("Filter Pattern:"));
        gui.PoListClear.set("text" /* NOI18N */, getString("Clear Filter"));
        gui.PoListModify.set("text" /* NOI18N */, getString("Modify"));
        gui.PoListAdd.set("text" /* NOI18N */, getString("Create New"));
        gui.PoListDelete.set("text" /* NOI18N */, getString("Delete"));
        gui.PoListDuplicate.set("text" /* NOI18N */, getString("Duplicate"));
        
        gui.PoDetailLabel.set("text" /* NOI18N */, getString("Policy Details"));
        gui.PoNameLabel.set("text" /* NOI18N */, getString("Policy Name:"));
        gui.PoMinPwLengthLabel.set("text" /* NOI18N */,
				   getString("Minimum Password Length:"));
        gui.PoMinPwClassLabel.set("text" /* NOI18N */,
				  getString("Minimum Password Classes:"));
        gui.PoSavedPasswordsLabel.set("text" /* NOI18N */,
				      getString("Saved Password History:"));
        gui.PoMinTicketLifetimeLabel.set("text" /* NOI18N */,
			 getString("Minimum Password Lifetime (seconds):"));
        gui.PoMaxTicketLifetimeLabel.set("text" /* NOI18N */,
			 getString("Maximum Password Lifetime (seconds):"));
        gui.PoReferencesLabel.set("text" /* NOI18N */,
				  getString("Principals Using This Policy:"));
        gui.PoDetailSave.set("text" /* NOI18N */, getString("Save"));
        gui.PoDetailPrevious.set("text" /* NOI18N */, getString("Previous"));
        gui.PoDetailDone.set("text" /* NOI18N */, getString("Done"));
        gui.PoDetailCancel.set("text" /* NOI18N */, getString("Cancel"));
    }
    
    /**
     * Allow user to see a fatal error before exiting
     */
    public void fatalError(Frame frame, String[] text) {
        String title = getString("Error");
        String[] buttons = new String[1];
        buttons[0] = getString("OK");
        ChoiceDialog cd = new ChoiceDialog(frame, title, text, buttons);
        cd.getSelection();
        exit();
    }
    
    /**
     * Set the defaults for the login screen.  Called on startup,
     * when "Start Over" is pressed, or when "Log Out" is chosen
     * from the main screen's menu.
     *
     */
    public void setLoginDefaults() {
        CurName = DefName;
        CurPass = "";
        if (DefRealm != null)
            CurRealm = DefRealm;
        else {
            CurRealm = "";
            if (firsttime) {
                showLoginWarning(getString("Cannot find default realm; "
					   +"check /etc/krb5/krb5.conf"));
                firsttime = false;
            }
        }
        if (DefServer != null)
            CurServer = DefServer;
        else
            CurServer = "";
        CurPort = 0;
        try {
            Integer i = new Integer(DefPort);
            CurPort = i.intValue();
        } catch (NumberFormatException e) {}
        gui.LoginName.set("text" /* NOI18N */, CurName);
        gui.LoginPass.set("text" /* NOI18N */, CurPass);
        gui.LoginRealm.set("text" /* NOI18N */, CurRealm);
        gui.LoginServer.set("text" /* NOI18N */, CurServer);
        if (CurRealm.equals("___default_realm___")) {
            String[] error = new String[1];
            error[0] = getString(
				 "Kerberos /etc/krb5/krb5.conf configuration"
				 +" file not configured; exiting");
            fatalError(realLoginFrame, error);
        }
        if (!loginListeners)
            setupLoginNormalListeners();
        loginListeners = true;
        TextField name = (TextField)gui.LoginName.getBody();
        name.selectAll();
        name.requestFocus();
    }
    
    /**
     * React after new realm entered
     *
     */
    public void newRealm() {
        CurRealm = (String)gui.LoginRealm.get("text" /* NOI18N */);
        String s = kc.getRealmServer(CurRealm);
        if (s != null) {
            CurServer = s;
            gui.LoginServer.set("text" /* NOI18N */, CurServer);
            
        } else {
            showLoginWarning(getString("Cannot find default server for realm"));
            CurServer = "";
            gui.LoginServer.set("text" /* NOI18N */, CurServer);
            ((TextField)gui.LoginServer.getBody()).requestFocus();
        }
    }
    
    /**
     * React after new server entered
     *
     */
    public void newServer() {
        CurServer = (String)gui.LoginServer.get("text" /* NOI18N */);
        if (CurPass.compareTo("") != 0)
            loginComplete();
    }
    
    /**
     * React after username is complete
     *
     */
    public void nameComplete() {
        ((TextField)gui.LoginName.getBody()).select(0, 0);
        ((TextField)gui.LoginPass.getBody()).requestFocus();
    }
    
    /**
     * React after password is complete or "OK" button is pressed.
     * We insist that the realm and server are set here separately
     * so that we can permit field-to-field motion if /etc/krb5/krb5.conf
     * does not exist.
     *
     */
    public void passwordComplete() {
        CurPass = (String)gui.LoginPass.get("text" /* NOI18N */);
        if (CurRealm.compareTo("") == 0) {
            ((TextField)gui.LoginRealm.getBody()).requestFocus();
            return;
        }
        if (CurServer.compareTo("") == 0) {
            ((TextField)gui.LoginServer.getBody()).requestFocus();
            return;
        }
        loginComplete();
    }
    
    /**
     * Check to see if we're happy with the login information.
     * We may want to go to the main screen, principal list tab.
     *
     */
    public void loginComplete() {
        pdateLoginDone = new Date();
        CurName   = (String)gui.LoginName.get("text" /* NOI18N */);
        CurPass   = (String)gui.LoginPass.get("text" /* NOI18N */);
        CurRealm  = (String)gui.LoginRealm.get("text" /* NOI18N */);
        CurServer = (String)gui.LoginServer.get("text" /* NOI18N */);
        if (CurPass.compareTo("") == 0) {
            showLoginWarning(getString("A password must be specified"));
            ((TextField)gui.LoginPass.getBody()).requestFocus();
            return;
        }
        if (CurRealm.compareTo("") == 0) {
            showLoginWarning(getString("A realm entry must be specified"));
            ((TextField)gui.LoginRealm.getBody()).requestFocus();
            return;
        }
        if (CurServer.compareTo("") == 0) {
            showLoginWarning(getString("A master KDC entry must be specified"));
            ((TextField)gui.LoginServer.getBody()).requestFocus();
            return;
        }
        
        realLoginFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Kadmin = new Kadmin();
        boolean b;
        try {
            b = Kadmin.sessionInit(CurName, CurPass, CurRealm, CurServer,
				   CurPort);
        } catch (Exception e) {
            b = false;
            realLoginFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            showLoginError(e.getMessage());
            return;
        }
        realLoginFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        if (b == false) {
            showLoginError(getString("Invalid login, please try again"));
            return;
        }
        pdateSessionUp = new Date();
        
        // Instantiate defaults for this user
        if (defaults == null)
            defaults = new Defaults(System.getProperty("user.home" /* NOI18N */)
				    + "/.gkadmin" /* NOI18N */,
			    (java.awt.Color)gui.mainframe.get("background"));
        else
            defaults.refreshDefaults();
        
        // Figure out what privileges we have
        try {
            privs = Kadmin.getPrivs();
        } catch (Exception e) {
            showLoginError(e.getMessage());
        }
        
        // Check privileges; if bad enough, we'll just give up.
        if (checkPrivs() == false) {
            try {
                Kadmin.sessionExit();
            } catch (Exception e) {}
            return;
        }
        reactToPrivs();
        
        prSetEditable(false);
        prSetCanSave(false);
        poSetEditable(false);
        poSetCanSave(false);
        prSelValid(false);
        poSelValid(false);
        gui.PrListPattern.set("text" /* NOI18N */, "");
        gui.PoListPattern.set("text" /* NOI18N */, "");
        
        // Disable login frame
        setListeners(LoginNormal, false);
        loginListeners = false;
        
        pdatePreMainShow = new Date();
        realLoginFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        gui.mainframe.show(true);	/* XXX - done waaay too early, fix */
        realLoginFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        pdatePostMainShow = new Date();
        realMainFrame  = (Frame)gui.mainframe.getBody();
        realMainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        gui.tabbedfolder1.show(getString("Principals"));
        gui.cardpanel2.show("List" /* NOI18N */);
        setupMainNormalListeners();
        setupDefaultsEditingFrame();
        realMainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        pdateMainActive = new Date();
        reportStartTimes();
        
        showPolicyList("");
        showPrincipalList("");
        setPolicyChoice();
        /* XXX - disabled multiple selection until double-click works */
        gui.Prlist.set("allowMultipleSelections" /* NOI18N */,
		       new Boolean(false));
        gui.Pollist.set("allowMultipleSelections" /* NOI18N */,
			new Boolean(false));
        if ((privs & PRIV_LIST) == 0) {
            showWarning(
	getString("Unable to access lists; please use the Name field."));
            ((TextField)gui.PrListPattern.getBody()).requestFocus();
        }
    }
    
    /**
     * React to main screen's "Log Out" choice by going back to login screen.
     *
     */
    public void logout() {
        realMainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        setListeners(MainNormal, false);
        setListeners(defaultsNormal, false);
        try {
            Kadmin.sessionExit();
            Kadmin = null;
        } catch (Exception e) {
            realMainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            showError(e.getMessage());
            return;
        }
        setLoginDefaults();
        principalList = null;
        gui.Prlist.set("items" /* NOI18N */, null);
        policyList = null;
        gui.Pollist.set("items" /* NOI18N */, null);
        gui.mainframe.show(false);
        curPrListPos = 0;
        curPrPattern = "";
        curPoListPos = 0;
        curPoPattern = "";
        
        // Forget this user's print preferences
        PrintUtil.reinitialize();
        
        realMainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    public void exit() {
        try {
            if (Kadmin != null)
                Kadmin.sessionExit();
        } catch (Exception e) {}
        super.exit();
    }
    
    /*
     * Methods for the principal list panel
     */
    
    /**
     * Update all principal text fields from gui.
     * Check to see if anyone of them had a parse error.
     * @param nullPasswdOK true if the password can be null. This is
     * allowed only when the operation is a modify on an existing
     * principal or if it is an attempt to print a new principal still
     * in creation.
     * @returns true if all is ok,  false if an error occurs
     */
    // Quits as soon as the first error is detected. The method that
    // detects the error also shows a dialog box with a message.
    public boolean prUpdateFromGui(boolean nullPasswdOK) {
        return (setPrName1() && setPrPassword(nullPasswdOK) && setPrExpiry() &&
		setPrComments() && setPrPwExpiry() && setPrKvno() &&
		setPrMaxlife() && setPrMaxrenew() && setEncType());
    }
    
    /**
     * Is the principal name field editable?
     *
     */
    public void prSetEditable(boolean editable) {
        prnameEditable = editable;
        Boolean b = new Boolean(editable);
        gui.PrName1.set("editable" /* NOI18N */, b);
    }
    
    /**
     * React to a change in the principal search pattern
     *
     */
    public void prPatternComplete() {
        curPrListPos = 0;
        String pattern = (String)gui.PrListPattern.get("text" /* NOI18N */);
        if (!noLists)
            showPrincipalList(pattern);
        else
            setCurPrincipal(pattern);
    }
    
    /**
     * Clear principal search pattern
     *
     */
    public void prPatternClear() {
        if (noLists) {
            gui.PrListPattern.set("text" /* NOI18N */, "");
            ((TextField)gui.PrListPattern.getBody()).requestFocus();
        } else {
            String tempName = CurPrincipal;
            fillPrincipalList("");
            selectPrincipal(tempName);
        }
    }
    
    /**
     * Show the principal list after applying the filter passed in.
     */
    public void showPrincipalList(String pattern) {
        prin = null; // we are not editing a principal
        fillPrincipalList(pattern);
        ModeString = "";
        OpString = "";
        updateStatus();
        gui.cardpanel1.show("List" /* NOI18N */);
        if (noLists)
            ((TextField)gui.PrListPattern.getBody()).requestFocus();
    }
    
    /**
     * Generate the principal list for the first time or after a pattern
     * has been chosen.
     *
     */
    public void fillPrincipalList(String pattern) {
        if (noLists) {
            setCurPrincipal((String)gui.PrListPattern.get("text" /* NOI18N */));
            ((TextField)gui.PrListPattern.getBody()).requestFocus();
            disablePrincipalPrinting();
            return;
        }
        realMainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        pdateStartPlist = new Date();
        // Do we still want to cache the principal list?
        long cachetime = A_LONG_TIME;
        if (!defaults.getStaticLists())
            cachetime = defaults.getCacheTime() * 1000;
        if (principalList != null
	    && ((new Date()).getTime() - principalListDate.getTime())
	    <= cachetime) {
            
            // Has the pattern changed?
            if (pattern.compareTo(curPrPattern) != 0)
                newPrPattern(pattern);
            realMainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            selectPrincipal(curPrListPos);
            return;
            
        }
        PrincipalList p = new PrincipalList(Kadmin);
        gui.StatusLine.set("text" /* NOI18N */,
			   getString("Loading principal list"));
        try {
            principalList = p.getPrincipalList(CurRealm);
            principalListDate = new Date();
        } catch (Exception e) {
            realMainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            showError(e.getMessage());
            updateStatus();
            return;
        }
        updateStatus();
        pdateHavePlist = new Date();
        reportTime("Fetched Plist  : ", pdateHavePlist, pdateStartPlist);
        newPrPattern(pattern);
        selectPrincipal(curPrListPos);
        realMainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        pdateDonePlist = new Date();
        reportTime("Completed Plist: ", pdateDonePlist, pdateHavePlist);
        if (perfmon)
            System.out.println("Principal list has "
	       +(new Integer(principalList.length)).toString()+" items");
    }
    
    private void newPrPattern(String pattern) {
        curPrPattern = pattern;
        gui.PrListPattern.set("text" /* NOI18N */, pattern);
        refreshPrincipalList();
    }
    
    private void refreshPrincipalList() {
        if (noLists)
            return;
        Filter f = new Filter(principalList, curPrPattern);
        gui.Prlist.set("items" /* NOI18N */, f.out);
    }
    
    private void selectPrincipal(int pos) {
        TextList list = (TextList)gui.Prlist.getBody();
        if (list.countItems() == 0) {
            setCurPrincipal("");
            return;
        }
        
        if (pos < 0)
            pos = 0;
        else if (pos >= list.countItems())
            pos = list.countItems() - 1;
        
        list.select(pos);
        enablePrincipalPrinting();
        list.makeVisible(pos);
        setCurPrincipal(list.getItem(pos));
    }
    
    private void selectPrincipal(String name) {
        String[] list = getItemsFromTextList(((TextList)gui.Prlist.getBody()));
        selectPrincipal(search(list, name));
    }
    
    private String[] getItemsFromTextList(TextList list) {
        StringVector v = list.items();
        String [] ret = new String[v.size()];
        v.copyInto(ret);
        return ret;
    }
    
    /**
     * Find index where "name" might go in a sorted string array;
     * returns either the element which matches "name" exactly
     * or the element just lexographically greater than "name".
     */
    private int search(String[] array, String name) {
        int lo = 0;
        int hi = array.length;
        int mid = hi;
        while (lo < hi) {
            mid = (lo + hi) / 2;
            int cmp = name.concat("@").compareTo(array[mid].concat("@"));
            if (hi - lo == 1) {
                if (cmp > 0)
                    mid = hi;
                break;
            }
            if (cmp == 0)
                break;
            if (cmp < 0)
                hi = mid;
            else if (cmp > 0)
                lo = mid;
        }
        return mid;
    }
    
    private String[] addToList(String[] list, String name) {
        if (list == null)
            return null;
        int index = search(list, name);
        int rem = list.length - index;
        String[] newlist = new String[list.length+1];
        if (index > 0)
            System.arraycopy(list, 0, newlist, 0, index);
        newlist[index] = name;
        if (rem > 0)
            System.arraycopy(list, index, newlist, index+1, rem);
        return newlist;
    }
    
    private String[] delFromList(String[] list, String name) {
        if (list == null)
            return null;
        int index = search(list, name);
        int rem = list.length - index;
        String[] newlist = new String[list.length-1];
        if (index > 0)
            System.arraycopy(list, 0, newlist, 0, index);
        if (rem > 1)
            System.arraycopy(list, index+1, newlist, index, rem-1);
        return newlist;
    }
    
    /**
     * Collect the policy choice entries
     *
     */
    public void setPolicyChoice() {
        String[] pols = null;
        if (!noLists) {
            PolicyList p = new PolicyList(Kadmin);
            try {
                pols = p.getPolicyList();
            } catch (Exception e) {
                showError(e.getMessage());
                return;
            }
        }
        Choice c = (Choice)gui.PrPolicy.getBody();
        c.removeAll();
        c.add(getString("(no policy)"));
        for (int i = 0; pols != null && i < pols.length; i++)
            c.add(pols[i]);
    }
    
    /**
     * Look at the principal list to see what's selected
     *
     */
    public void lookAtPrList() {
        if (noLists)
            return;
        TextList list = (TextList) gui.Prlist.getBody();
        prMulti = null;
        String[] sel = list.getSelectedItems();
        if (sel.length == 1) {
            setCurPrincipal(sel[0]);
            curPrListPos = list.getSelectedIndex();
        } else {
            if (sel.length > 0)
                prMulti = sel;
            setCurPrincipal("");
        }
    }
    
    private void restorePrListSelection() {
        if (noLists)
            return;
        TextList list = (TextList) gui.Prlist.getBody();
        list.select(curPrListPos);
    }
    
    /**
     * When the principal name choice changes, we want to reflect
     * the name in the other principal tabs.  We can also use this
     * opportunity to enable/disable buttons.
     *
     */
    public void setCurPrincipal(String name) {
        CurPrincipal = name;
        gui.PrName1.set("text" /* NOI18N */, name);
        gui.PrName2.set("text" /* NOI18N */, name);
        gui.PrName3.set("text" /* NOI18N */, name);
        if (name.compareTo("") == 0) {
            prSelValid(false);
            return;
        }
        prSelValid(true);
    }
    
    /**
     * Make Modify, Delete and Duplicate buttons react to what is selected.
     * Privileges:
     * If we have neither modify or inquire, we keep Modify disabled;
     * if we have no modify privileges, we permit Modify to see info,
     * but the principal panel components are disabled in reactToPrivs().
     * If we have add and inquire privileges, we can permit Duplicate;
     * no add also means Create New is permanently disabled in reactToPrivs().
     * If we have no delete privileges, we keep Delete disabled.
     */
    public void prSelValid(boolean selected) {
        prSelValid = selected;
        Boolean b = new Boolean(selected && (privs & PRIV_INQUIRE) != 0);
        gui.PrListModify.set("enabled" /* NOI18N */, b);
        int want = (PRIV_ADD | PRIV_INQUIRE);
        b = new Boolean(selected && (privs & want) == want);
        gui.PrListDuplicate.set("enabled" /* NOI18N */, b);
        b = new Boolean((selected || prMulti != null)
			&&(privs & PRIV_DELETE) != 0);
        gui.PrListDelete.set("enabled" /* NOI18N */, b);
    }
    
    /**
     * Make the Save button do the right thing.
     *
     */
    public void prSetCanSave(boolean ok) {
        Boolean b = new Boolean(ok);
        gui.PrBasicSave.set("enabled" /* NOI18N */, b);
        gui.PrDetailSave.set("enabled" /* NOI18N */, b);
        gui.PrFlagsSave.set("enabled" /* NOI18N */, b);
    }
    
    /**
     * Update status line with current information.
     *
     */
    public void updateStatus() {
        gui.StatusLine.set("text" /* NOI18N */, ModeString+OpString+SaveString);
    }
    
    /**
     * This is a way for the data modification actions to note that
     * the principal has edits outstanding.
     *
     */
    public void prSetNeedSave() {
        prNeedSave = true;
        prSetCanSave(true);
        SaveString = getString("- *CHANGES*");
        updateStatus();
    }
    
    public boolean prDoSave() {
        
        // before attempting to save make sure all text fields are in order
        if (prUpdateFromGui(!prin.isNew) == false)
            return false;
        
        boolean b = true;
        realMainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            b = prin.savePrincipal();
        } catch (Exception e) {
            b = false;
            showError(e.getMessage());
        }
        realMainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        if (!b)
            return false;
        if (prin.isNew) {
            principalList = addToList(principalList, prin.PrName);
            refreshPrincipalList();
            selectPrincipal(prin.PrName);
        }
        prin.isNew = false;
        gui.PrPassword.set("text" /* NOI18N */, "");
        prin.setPassword("");
        prSetEditable(false);
        prSetCanSave(false);
        prNeedSave = false;
        SaveString = "";
        updateStatus();
        return true;
    }
    
    /**
     * React to a choice from the principal list via double-click or
     * single-click+Modify; we want to go to the next tab in each case.
     * If we don't have modify privileges, we need to simply show values.
     */
    public void prModify() {
        enablePrincipalPrinting();
        if (!prNeedSave) {
            prSetEditable(false);
            prSetCanSave(false);
        }
        if (noLists)
            CurPrincipal = (String)gui.PrListPattern.get("text" /* NOI18N */);
        realMainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        enablePrAttributes(new Boolean((privs & (PRIV_ADD|PRIV_MODIFY)) != 0));
        Boolean b = new Boolean((privs & PRIV_CHANGEPW) != 0);
        gui.PrPassword.set("enabled" /* NOI18N */, b);
        gui.PrBasicRandomPw.set("enabled" /* NOI18N */, b);
        gui.EncList.set("enabled" /* NOI18N */, b);
        try {
            prin = new Principal(Kadmin, CurPrincipal);
        } catch (Exception e) {
            showError(e.getMessage());
            realMainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            return;
        }
        realMainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        showPrincipal(prin);
        String policy = (String)gui.PrPolicy.get("selectedItem" /* NOI18N */);
        if (policy.compareTo(getString("(no policy)")) == 0)
            policy = "";
        else
            setDefaultPolicy(policy);
        ModeString = getString("Modify")+" ";
        OpString = getString("Principal");
        updateStatus();
        gui.cardpanel1.show("Basics" /* NOI18N */);
    }
    
    /**
     * React to add principal button
     * If we got here, we need to enable attributes since we have privs.
     */
    public void prAdd() {
        enablePrincipalPrinting();
        setCurPrincipal("");
        prSelValid = true;
        prSetEditable(true);
        prSetNeedSave();
        realMainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Boolean b = new Boolean(true);
        enablePrAttributes(b);
        gui.PrPassword.set("enabled" /* NOI18N */, b);
        gui.PrBasicRandomPw.set("enabled" /* NOI18N */, b);
        gui.EncList.set("enabled" /* NOI18N */, b);
        try {
            prin = new Principal(Kadmin, defaults);
        } catch (Exception e) {
            showError(e.getMessage());
            realMainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            return;
        }
        realMainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        showPrincipal(prin);
        ModeString = getString("Create New")+" ";
        OpString = getString("Principal");
        updateStatus();
        gui.cardpanel1.show("Basics" /* NOI18N */);
        ((TextField)gui.PrName1.getBody()).requestFocus();
    }
    
    /**
     * React to duplicate principal button
     *
     */
    public void prDuplicate() {
        enablePrincipalPrinting();
        if (noLists)
            CurPrincipal = (String)gui.PrListPattern.get("text" /* NOI18N */);
        realMainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            prin = new Principal(Kadmin, CurPrincipal);
        } catch (Exception e) {
            showError(e.getMessage());
            realMainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            return;
        }
        realMainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        setCurPrincipal("");
        prSelValid = true;
        prSetEditable(true);
        prSetNeedSave();
        realMainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Boolean b = new Boolean(true);
        enablePrAttributes(b);
        gui.PrPassword.set("enabled" /* NOI18N */, b);
        gui.PrBasicRandomPw.set("enabled" /* NOI18N */, b);
        gui.PrBasicRandomPw.set("enabled" /* NOI18N */, b);
        try {
            prin = new Principal(Kadmin, prin);
        } catch (Exception e) {
            showError(e.getMessage());
            realMainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            return;
        }
        realMainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        prin.PrName = "";
        showPrincipal(prin);
        ModeString = getString("Duplicate")+" ";
        OpString = getString("Principal");
        updateStatus();
        gui.cardpanel1.show("Basics" /* NOI18N */);
        ((TextField)gui.PrName1.getBody()).requestFocus();
    }
    
    /**
     * React to delete principal button
     */
    public void prDelete() {
        String text[] = {getString("You are about to destroy data."),
			 getString("Click OK to proceed or"
				   +" Cancel to continue editing.")};
        String resp = confirmAction(realMainFrame, text);
        if (resp.equals(getString("Cancel")))
            return;
        if (noLists)
            CurPrincipal = (String)gui.PrListPattern.get("text" /* NOI18N */);
        boolean b = false;
        try {
            b = Kadmin.deletePrincipal(CurPrincipal);
        } catch (Exception e) {
            showError(e.getMessage());
            return;
        }
        if (!b)
            return;
        principalList = delFromList(principalList, CurPrincipal);
        refreshPrincipalList();
        setCurPrincipal("");
        prSelValid = true;
        prSetEditable(true);
        if (curPrListPos == ((TextList)gui.Prlist.getBody()).countItems())
            curPrListPos--;
        showPrincipalList(curPrPattern);
    }
    
    /**
     * React to Previous button on basic screen
     *
     */
    public void prBasicPrevious() {
        prCancel();
    }
    
    /**
     * React to Next button on basic screen. If some changes were made
     * then check to see if they contain a parse error. If so, do
     * nothing. The method that checks for error messages also displays
     * the error message.
     *
     */
    public void prBasicNext() {
        if (prNeedSave)
            if (!prUpdateFromGui(!prin.isNew))
		return;
        
        updateStatus();
        gui.cardpanel1.show("Details" /* NOI18N */);
    }
    
    /**
     * React to Previous button on detail screen. If some changes were made
     * then check to see if they contain a parse error. If so, do
     * nothing. The method that checks for error messages also displays
     * the error message.
     */
    public void prDetailPrevious() {
        if (prNeedSave)
            if (!prUpdateFromGui(!prin.isNew))
		return;
        
        updateStatus();
        gui.cardpanel1.show("Basics" /* NOI18N */);
    }
    
    /**
     * React to Next button on detail screen. If some changes were made
     * then check to see if they contain a parse error. If so, do
     * nothing. The method that checks for error messages also displays
     * the error message.
     *
     */
    public void prDetailNext() {
        if (prNeedSave)
            if (!prUpdateFromGui(!prin.isNew))
		return;
        
        updateStatus();
        gui.cardpanel1.show("Flags" /* NOI18N */);
    }
    
    /**
     * React to Previous button on flags screen
     *
     */
    public void prFlagsPrevious() {
        updateStatus();
        gui.cardpanel1.show("Details" /* NOI18N */);
    }
    
    /**
     * React to Done button on flags screen. If any changes were made to
     * the principal, then try to save them. If the save fails for any
     * reason, do not return to the principal list.
     *
     */
    public void prFlagsDone() {
        if (prNeedSave && prDoSave() == false)
            return;
        showPrincipalList(curPrPattern);
    }
    
    /**
     * React to save principal button
     *
     */
    public void prSave() {
        prDoSave();
    }
    
    /**
     * React to cancel principal button
     *
     */
    public void prCancel() {
        if (prNeedSave) {
            String text[] = {getString("You are about to lose changes."),
			     getString("Click Save to commit changes, "
				       +"Discard to discard changes, "
				       +"or Cancel to continue editing.")};
            String resp = confirmSave(realMainFrame, text);
            if (resp.equals(getString("Cancel")))
                return;
            if (resp.equals(getString("Save")))
                if (!prDoSave())
		    return;
        }
        prSetEditable(false);
        prSetCanSave(false);
        prNeedSave = false;
        lookAtPrList();
        SaveString = "";
        showPrincipalList(curPrPattern);
    }
    
    /*
     * Methods for the principal attribute panels
     */
    
    public boolean setPrName1() {
        if (!prnameEditable)
            return true;
        
        String p = ((String)gui.PrName1.get("text" /* NOI18N */)).trim();
        if (p.compareTo("") == 0) {
            showError(getString("Please enter a principal name or cancel"));
            ((TextField)gui.PrName1.getBody()).requestFocus();
            return false;
        }
        // visually delete any white space that was at the start or end
        // by resetting the field to the trimmmed String.
        gui.PrName1.set("text" /* NOI18N */, p);
        setCurPrincipal(p);
        prin.setName(p);
        return true;
    }
    
    public boolean setPrComments() {
        prin.setComments((String)gui.PrComments.get("text" /* NOI18N */));
        return true;
    }
    
    public boolean setEncType() {
        if (prin.setEncType((String)gui.EncList.get("text" /* NOI18N */))) {
            // visually delete any extraneous data that was ignored in the
            // parsing by resetting the gui data
            gui.EncList.set("text" /* NOI18N */,  prin.getEncType());
            return true;
        } else
            return false;
    }

    public boolean setPrExpiry() {
        if (prin.setExpiry((String)gui.PrExpiry.get("text" /* NOI18N */))) {
            // visually delete any extraneous data that was ignored in the
            // parsing by resetting the gui data
            gui.PrExpiry.set("text" /* NOI18N */,  prin.getExpiry());
            return true;
        } else {
            showDataFormatError(((TextField)gui.PrExpiry.getBody()),
				DATE_DATA);
            return false;
        }
    }
    
    public boolean setPrPassword(boolean nullOK) {
        String p = (String)gui.PrPassword.get("text" /* NOI18N */);
        if (p.compareTo("") == 0) {
            if (!nullOK) {
                showError(getString("Please enter a password or cancel"));
                ((TextField)gui.PrPassword.getBody()).requestFocus();
                return false;
            } else return true;
	}
        
        prin.setPassword(p);
        return true;
    }
    
    public void genRandomPassword() {
        int n, count = 0;
        byte[] buf = new byte[20];
        byte b;
        Random r = new Random();
        String passlist = "abcdefghijklmnopqrstuvwxyz1234567890!#$%&*+@"
	    /* NOI18N */;
        
        gui.PrPassword.set("text" /* NOI18N */, "");
        while (count < 10) {
            n = r.nextInt() & 0x7F;
            b = (byte)n;
            if (passlist.indexOf(b) == -1)
                continue;
            buf[count++] = b;
        }
        buf[count] = 0;
        CurPass = new String(buf);
        gui.PrPassword.set("text" /* NOI18N */, CurPass);
        prin.setPassword((String)gui.PrPassword.get("text" /* NOI18N */));
    }
    
    public void setPrPolicy() {
        if (prin == null)
                return;
        String policy = (String)gui.PrPolicy.get("selectedItem" /* NOI18N */);
        if (policy.compareTo(getString("(no policy)")) == 0)
            policy = "";
        try {
                prin.setPolicy(policy);
        } catch (Exception e) {};
        setDefaultPolicy(policy);
    }
    
    public boolean setPrMaxlife() {
        if (prin.setMaxlife((String)gui.PrMaxLifetime.get("text"
							  /* NOI18N */))) {
            // visually delete any extraneous data that was ignored in the
            // parsing by resetting the gui data
            gui.PrMaxLifetime.set("text" /* NOI18N */, prin.getMaxLife());
            return true;
        } else {
            showDataFormatError(((TextField)gui.PrMaxLifetime.getBody()),
				DURATION_DATA);
            return false;
        }
    }
    
    public boolean setPrMaxrenew() {
        if (prin.setMaxrenew((String)gui.PrMaxRenewal.get(
						  "text" /* NOI18N */))) {
            // visually delete any extraneous data that was ignored in the
            // parsing  by resetting the gui data
            gui.PrMaxRenewal.set("text" /* NOI18N */, prin.getMaxRenew());
            return true;
        } else {
            showDataFormatError(((TextField)gui.PrMaxRenewal.getBody()),
				DURATION_DATA);
            return false;
        }
    }
    
    public boolean setPrKvno() {
        if (prin.setKvno((String)gui.PrKvno.get("text" /* NOI18N */))) {
            // visually delete any extraneous data that was ignored in the
            // parsing by resetting the gui data
            gui.PrKvno.set("text" /* NOI18N */, nf.format(prin.Kvno));
            return true;
        } else {
            showDataFormatError(((TextField)gui.PrKvno.getBody()), NUMBER_DATA);
            return false;
        }
    }
    
    public boolean setPrPwExpiry() {
        if (prin.setPwExpiry((String)gui.PrPwExpiry.get("text" /* NOI18N */))) {
            // visually delete any extraneous data that was ignored in the
            // parsing by resetting the gui data
            gui.PrPwExpiry.set("text" /* NOI18N */, prin.getPwExpireTime());
            return true;
        } else {
            showDataFormatError(((TextField)gui.PrPwExpiry.getBody()),
				DATE_DATA);
            return false;
        }
    }
    
    public void setPrFlag(int bitmask) {
        prin.flags.toggleFlags(bitmask);
    }
    
    /**
     * Update components to reflect data in this principal
     *
     */
    public void showPrincipal(Principal p) {
        
        gui.PrName1.set("text" /* NOI18N */, p.PrName);
        gui.PrName2.set("text" /* NOI18N */, p.PrName);
        gui.PrName3.set("text" /* NOI18N */, p.PrName);
        gui.PrComments.set("text" /* NOI18N */, p.Comments);
        String policy = p.Policy;
        if (policy.compareTo("") == 0)
            policy = getString("(no policy)");
        gui.PrPolicy.set("selectedItem" /* NOI18N */, policy);
        gui.PrPassword.set("text" /* NOI18N */, "");
        
        gui.PrLastChangedTime.set("text" /* NOI18N */, p.getModTime());
        gui.PrLastChangedBy.set("text" /* NOI18N */,   p.ModName);
        gui.PrExpiry.set("text" /* NOI18N */,          p.getExpiry());
        gui.EncList.set("text" /* NOI18N */,           p.getEncType());
        gui.PrLastSuccess.set("text" /* NOI18N */,     p.getLastSuccess());
        gui.PrLastFailure.set("text" /* NOI18N */,     p.getLastFailure());
        gui.PrFailCount.set("text" /* NOI18N */, nf.format(p.NumFailures));
        gui.PrLastPwChange.set("text" /* NOI18N */,    p.getLastPwChange());
        gui.PrPwExpiry.set("text" /* NOI18N */,        p.getPwExpireTime());
        gui.PrKvno.set("text" /* NOI18N */, nf.format(p.Kvno));
        gui.PrMaxLifetime.set("text" /* NOI18N */, p.getMaxLife());
        gui.PrMaxRenewal.set("text" /* NOI18N */, p.getMaxRenew());
        
        gui.PrLockAcct.set("state" /* NOI18N */,
		   new Boolean(p.flags.getFlag(Flags.DISALLOW_ALL_TIX)));
        gui.PrForcePwChange.set("state" /* NOI18N */,
			new Boolean(p.flags.getFlag(Flags.REQUIRES_PWCHANGE)));
        gui.PrAllowPostdated.set("state" /* NOI18N */,
		 new Boolean(!p.flags.getFlag(Flags.DISALLOW_POSTDATED)));
        gui.PrAllowForwardable.set("state" /* NOI18N */,
		   new Boolean(!p.flags.getFlag(Flags.DISALLOW_FORWARDABLE)));
        gui.PrAllowRenewable.set("state" /* NOI18N */,
		 new Boolean(!p.flags.getFlag(Flags.DISALLOW_RENEWABLE)));
        gui.PrAllowProxiable.set("state" /* NOI18N */,
		 new Boolean(!p.flags.getFlag(Flags.DISALLOW_PROXIABLE)));
        gui.PrAllowSvr.set("state" /* NOI18N */,
			   new Boolean(!p.flags.getFlag(Flags.DISALLOW_SVR)));
        gui.PrAllowTGT.set("state" /* NOI18N */,
		   new Boolean(!p.flags.getFlag(Flags.DISALLOW_TGT_BASED)));
        gui.PrAllowDupAuth.set("state" /* NOI18N */,
		       new Boolean(!p.flags.getFlag(Flags.DISALLOW_DUP_SKEY)));
        gui.PrRequirePreAuth.set("state" /* NOI18N */,
			 new Boolean(p.flags.getFlag(Flags.REQUIRE_PRE_AUTH)));
        gui.PrRequireHwPreAuth.set("state" /* NOI18N */,
			   new Boolean(p.flags.getFlag(Flags.REQUIRE_HW_AUTH)));
    }
    
    /**
     * 
