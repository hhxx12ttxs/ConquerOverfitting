/** BEGIN COPYRIGHT BLOCK
 * Copyright (C) 2001 Sun Microsystems, Inc. Used by permission.
 * Copyright (C) 2005 Red Hat, Inc.
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * END COPYRIGHT BLOCK **/

package com.netscape.admin.dirserv;

import java.util.*;
import java.util.zip.CRC32;
import java.io.UnsupportedEncodingException;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import java.awt.event.*;
import com.netscape.management.client.IMenuItem;
import com.netscape.management.client.IPage;
import com.netscape.management.client.MenuItemText;
import com.netscape.management.client.IResourceObject;
import com.netscape.management.client.ResourceObject;
import com.netscape.management.client.util.*;
import com.netscape.management.client.console.ConsoleInfo;
import com.netscape.management.client.acleditor.*;
import com.netscape.management.client.ug.*;
import com.netscape.management.nmclf.SuiConstants;
import netscape.ldap.*;
import netscape.ldap.util.*;
import com.netscape.admin.dirserv.panel.*;
import com.netscape.admin.dirserv.propedit.*;
import com.netscape.admin.dirserv.task.CreateVLVIndex;
import com.netscape.admin.dirserv.DSSchemaHelper;
import com.netscape.admin.dirserv.panel.UIFactory;
import com.netscape.admin.dirserv.panel.MappingUtils;
import com.netscape.admin.dirserv.roledit.ResEditorRoleInfo;
import com.netscape.admin.dirserv.roledit.ResEditorManagedRole;
import com.netscape.admin.dirserv.roledit.ResEditorFilteredRole;
import com.netscape.admin.dirserv.roledit.ResEditorNestedRole;
import com.netscape.admin.dirserv.cosedit.ResEditorCosInfo;
import com.netscape.admin.dirserv.cosedit.ResEditorCosAttributes;
import com.netscape.admin.dirserv.cosedit.ResEditorCosTitlePage;
import com.netscape.admin.dirserv.account.AccountInactivation;

/**
 * DSEntryObject is the node of each entry under the content view.
 *
 * @author  rweltman
 */
public class DSEntryObject extends ResourceObject
						   implements IDSEntryObject {
	/**
	 * Constructs a bogus entry object in order to display some information
	 * about why the object is bogus e.g. it's a bad referral, the user
	 * does not have permission to see it, etc.
	 *
	 * @param isBogus true if this object does not represent a real entry
	 */
    protected DSEntryObject( boolean isBogus ) {
		_isBogus = isBogus;
	}

	/**
	 * Constructor of the entry object. It will not load the directory
	 * information until later. This function will just initialize the
	 * internal variables.
	 *
	 * @param model Content model.
	 * @param dn DN of the Directory entry
	 * @param displayName Name to use for rendering this node
	 * @param showPrivateSuffixes false if only public suffixes will be visible
	 */
    public DSEntryObject( IDSModel model,
						  String dn,
						  String displayName,
						  boolean showPrivateSuffixes ) {
		_showPrivateSuffixes = showPrivateSuffixes;
		Debug.println(9, "DSEntryObject.DSEntryObject(4): dn=" + dn);
		initialize( model, dn, displayName );
	}

	/**
	 * Constructor of the entry object. It will not load the directory
	 * information until later. This function will just initialize the
	 * internal variables.
	 *
	 * @param model Content model.
	 * @param dn DN of the Directory entry
	 * @param displayName Name to use for rendering this node
	 */
    public DSEntryObject( IDSModel model,
						  String dn,
						  String displayName ) {
		this( model, dn, displayName, true );
		Debug.println(9, "DSEntryObject.DSEntryObject(3): dn=" + dn);
	}

	/**
	 * Constructor of the entry object. It will not load the directory
	 * information until later. This function will just initialize the
	 * internal variables.
	 *
	 * @param model Content model.
	 * @param dn DN of the Directory entry
	 */
    public DSEntryObject( IDSModel model,
						  String dn ) {
		String displayName = getDisplayNameFromDN(model, dn);
		initialize( model, dn, displayName );
	}

	/**
	 * Constructor of the entry object. It will not load the directory
	 * information until later. This function will just initialize the
	 * internal variables.
	 *
	 * @param dn DN of the Directory entry
	 */
    public DSEntryObject( String dn ) {
		this( null, dn );
		Debug.println(9, "DSEntryObject.DSEntryObject(1): dn=" + dn);
	}

	/**
	 * Constructor of the entry object.
	 *
	 * @param model Content model.
	 * @param entry LDAP entry.
	 */
    public DSEntryObject( IDSModel model,
						  LDAPEntry entry,
						  boolean referralsEnabled ) {
		this( model, entry.getDN() );
		Debug.println(9, "DSEntryObject.DSEntryObject(2a): dn=" + entry.getDN());
		_entry = entry;
		setReferralsEnabled(referralsEnabled);
		initializeFromEntry( _entry );
	}

	/**
	 * Constructor of the entry object. It will not load the directory
	 * information until later. This function will just initialize the
	 * internal variables.
	 *
	 * @param model Content model.
	 * @param dn DN of the Directory entry
	 * @param displayName Name to use for rendering this node
	 */
    protected void initialize( IDSModel model,
							   String dn,
							   String displayName ) {
		// initialize all the variables
		_dn = dn;
		_model = model;
		setAllowsChildren(true);
		RemoteImage icon;
		_sDisplayName = displayName;
		if ( isRootDSE() ) {
			/* This is the root DSE */
			_isPrivateSuffix = true;
			icon = DSUtil.getPackageImage( _serverIconName );
		} else {
			icon = DSUtil.getPackageImage( _defaultImageName );
		}
		setName( _sDisplayName );
		setIcon( icon );
		if ( _cContainers == null )
			_cContainers = initContainerNames();
		if (model instanceof DSContentModel) {
			_selectedPartitionView = ((DSContentModel)model).getSelectedPartitionView();
		}
		_suffixes = null;
			
	}

    public void setModel( IDSModel model ) {
		_model = model;
	}

	/**
	 * Return the associated LDAPEntry, if any.
	 *
	 * @return LDAPEntry, or null.
	 */
	public LDAPEntry getEntry() {
		Debug.println(9, "DSEntryObject.getEntry: " + getDN() );
		if ( _entry == null ) {
			try {
				/* Fetch all attributes of the entry */
				String[] editableAttributes = DSSchemaHelper.getOperationalAttributes(_model.getSchema());				
				String[] allAttributes = null;
				if (editableAttributes == null) {
					allAttributes = new String[1];
					allAttributes[0] = "*";
				} else {
					allAttributes = new String[editableAttributes.length + 1];
					for (int i=0; i < editableAttributes.length; i++) {
						allAttributes[i] = editableAttributes[i];
					}
					allAttributes[editableAttributes.length] = "*";
				}
				_entry = readEntry( getDN(), allAttributes );
				initializeFromEntry( _entry );
			} catch ( Exception ex ) {
				Debug.println( "DSEntryObject.getEntry <" +
							   getDN() + "> " + ex );
			}
		}		
		return _entry;
	}

	/**
	 * Sets the value of _entry
	 *
	 */
	public void setEntry(LDAPEntry entry) {
		_entry = entry;
	}

	/**
	 * Reset the internal LDAPEntry reference, e.g. to force reloading
	 */
	public void reset() {
		_entry = null;
	}

    /**
	 * Return the host of this entry
	 *
	 * @return The host of this entry
	 */
    public String getHost() {
		return getLDAPConnection().getHost();
	}

    /**
	 * Return the port of this entry
	 *
	 * @return The port of this entry
	 */
    public int getPort() {
		return getLDAPConnection().getPort();
	}

    /**
	 * Get object classes which are to be considered containers
	 *
	 */
    private static Hashtable initContainerNames() {
		Hashtable h = new Hashtable();
		String items = _resource.getString( _section, "containers" );
		if ( _verbose )
			Debug.println( "DSEntryObject.initContainerNames" );
		if ( items != null ) {
			StringTokenizer st = new StringTokenizer( items, " " );
			int i = 0;
			while ( st.hasMoreTokens() ) {
				String name = st.nextToken().toLowerCase();
				if ( _verbose )
					Debug.println( "  added container type " + name );
				h.put( name, name );
			}
		}
		/* Due to a server bug, "numsubordinates>=1" is not indexed, but
		   "(&(numsubordinates=*)(numsubordinates>=1))" is, in DS 4.0
		*/
		_childFilter = "(|(&(numsubordinates=*)(numsubordinates>=1))";
		Enumeration e = h.keys();
		while( e.hasMoreElements() )
			_childFilter += "(objectclass=" + (String)e.nextElement() + ")";
		_childFilter += ")";
		if ( _verbose )
			Debug.println( "DSEntryObject.initContainerNames: VList filter " +
						   "for tree = " + _childFilter );
		return h;
	}

    private static String initDefaultIconName() {
		String defaultImageName = _resource.getString( _section,
													   "default-icon" );
		if ( defaultImageName == null ) {
			defaultImageName = "genobject.gif";
		}
		
		return defaultImageName;
	}

    private static String initDefaultFolderIconName() {
		String defaultImageName = _resource.getString( _section,
													   "default-folder-icon" );
		if ( defaultImageName == null ) {
			defaultImageName = "folder.gif";
		}
	
		return defaultImageName;
	}


		

	/**
	 * Add the name of an object class to be considered a container.
	 *
	 * @param name Name of an object class to be considered a container.
	 */
    public static void addContainerName( String name ) {
		String lowerCaseName = name.toLowerCase();
		_cContainers.put(lowerCaseName , lowerCaseName);
	}

    public String getDN() {
		return _dn;
	}

    public void setDN( String dn ) {
		_dn = dn;
	}

    public LDAPConnection getLDAPConnection() {
		return _model.getServerInfo().getLDAPConnection();
	}

	/**
	 *	Check if there are children to this node.
	 */
	public void load() {
		String dn = getDN();
		Debug.println(9, "DSEntryObject.load <" + dn + ">" );
		if ( isRootDSE() ) {
			expandRoot();
		} else {
			removeAllChildren();
			String[] attrs = MINIMAL_ATTRS;
			LDAPConnection ldc = getLDAPConnection();
			if ( ldc == null ) {
				Debug.println( "DSEntryObject.load: " +
							   "no LDAP connection" );
				return;
			}
			LDAPEntry findEntry = readEntry( dn, attrs );
			if ( findEntry != null) {
				initializeFromEntry( findEntry );
			}
			_fLoaded = true;
			/* Force collecting all children */
			_iChildren = -1;
			Debug.println(9, "DSEntryObject.load: calling getChildCount()" );
			int nChildren = getChildCount();			
		}
		if ( _customizePanel != null )
			_customizePanel.recalculate();
	}

    DSEntryObject nodeFromEntry( LDAPEntry findEntry ) {
		Debug.println(9, "DSEntryObject.nodeFromEntry: " +
					   findEntry.getDN() );
		DSEntryObject deo = new DSEntryObject( _model, findEntry,
											   getReferralsEnabled());
		
		deo.setParentNode( this );
		return deo;
	}

    DSEntryObject addOneChild( LDAPEntry findEntry,
							   boolean containersOnly ) {
		if (findEntry == null) {
			return null;
		}
		DSEntryObject deo = nodeFromEntry( findEntry );
		if ( !containersOnly || !deo.isLeafType() ) {
			Debug.println(9, "DSEntryObject.addOneChild: Adding " +
						  deo.getDN() +
						  " to " + getDN() );
			add( deo );
			return deo;
		}
		Debug.println(9, "DSEntryObject.addOneChild: Rejected " +
					  deo.getDN() + " as child of " +
					  getDN() );
		
		return null;
	}
    private LDAPEntry readEntry( String dn, String[] attrs) {
		return readEntry(dn, attrs, false);
	}

    private LDAPEntry readEntry( String dn, String[] attrs, boolean searchOnlyOnViewedPartition ) {
		Debug.println(9, "DSEntryObject.readEntry: " + dn );
		try {
			LDAPConnection ldc = getLDAPConnection();
			if ( ldc == null ) {
				Debug.println( "DSEntryObject.readEntry: " +
							   "no LDAP connection" );
				return null;
			}
			LDAPSearchConstraints cons =
				(LDAPSearchConstraints)ldc.getSearchConstraints().clone();
			if ( !getReferralsEnabled() ) {
				Debug.println(9, "DSEntryObject.readEntry: no referrals" );
				if ((searchOnlyOnViewedPartition) && !_selectedPartitionView.equals(DSContentModel.ALL)) {
					try {
						byte[] vals = _selectedPartitionView.getBytes( "UTF8" );
						LDAPControl searchControl = new LDAPControl(SEARCH_OID, true, vals);
						LDAPControl[] controls = {_manageDSAITControl, searchControl};
						cons.setServerControls(controls);
					} catch ( UnsupportedEncodingException e ) {
						Debug.println( "DSEntryObject.readEntry: Error: UTF8 not supported" );
						cons.setServerControls(_manageDSAITControl);
					}					
				} else {
					cons.setServerControls( _manageDSAITControl );
				}
			} else {
				if ((searchOnlyOnViewedPartition) && !_selectedPartitionView.equals(DSContentModel.ALL)) {
					try {
						byte[] vals = _selectedPartitionView.getBytes( "UTF8" );
						LDAPControl searchControl = new LDAPControl(SEARCH_OID, true, vals);					
						cons.setServerControls(searchControl);
					} catch ( UnsupportedEncodingException e ) {
						Debug.println( "DSEntryObject.readEntry: Error: UTF8 not supported" );
					}
				}					
				Debug.println(9, "DSEntryObject.readEntry: referrals on" );
			}
			LDAPEntry findEntry = DSUtil.readEntry(ldc, dn, attrs, cons);
			if ( findEntry == null ) {
				Debug.println( "DSEntryObject.readEntry: unable to read <" +
							   dn + ">" );
			} else {
				Debug.println(9, "DSEntryObject.readEntry of " + dn +
							  " returned " + findEntry );
			}
			return findEntry;
		} catch ( LDAPReferralException e ) {
			Debug.println( "DSEntryObject.readEntry referral problem " +
								dn + ": " + e );
			LDAPUrl[] urlList = e.getURLs();
			for (int ii = 0; ii < urlList.length; ++ii) {
				Debug.println("DSEntryObject.readEntry: url=" + urlList[ii]);
			}
		} catch ( LDAPException e ) {
			Debug.println( "DSEntryObject.readEntry of " +
								dn + ": " + e );
		}
		return null;
	}

	/**
	 * Expand a single "root" node
	 */
	private DSEntryObject expandRootNode( String dn ) {
		if ( _verbose )
			Debug.println( "DSEntryObject.expandRootNode: <" + dn + ">" );
		DSEntryObject deo = null;
		LDAPConnection ldc = getLDAPConnection();
		if ( ldc == null ) {
			Debug.println( "DSEntryObject.expandRootNode: <" + dn +
						   ">, no LDAP connection" );
			return null;
		}
		String[] fattrs = MINIMAL_ATTRS;
		if ( isRootDSE( dn ) ) {
			return null;
		}
		Debug.println( "  NamingContext: " + dn );
		// ADD CONTROL
		LDAPEntry findEntry;
		if (_selectedPartitionView.equals(DSContentModel.ALL)) {
			findEntry = readEntry( dn, fattrs );
		} else {
			findEntry = readEntry( dn, fattrs, true );
		}
		if (_verbose) {
			Debug.println( "DSEntryObject.expandRootNode: " +
						   "Read of <" + dn + "> returned " +
						   findEntry );
		}
		if ( findEntry != null ) {
			deo = addOneChild( findEntry, false );
		}

		return deo;
	}

	/**
	 * create all the one level depth sub nodes defined in an enumeration
	 * of DNs.
	 */
	private void expandRootNode( Enumeration e ) {
		while ( e.hasMoreElements() ) {
			String dn = (String)e.nextElement();
			Debug.println( "DSEntryObject.expandRootNode: " + dn );
			DSEntryObject deo = expandRootNode( dn );
		}
	}

	/**
	 *	create all the one level depth sub nodes.
	 */
	private void expandRoot() {
		Debug.println( "DSEntryObject.expandRoot" );
		removeAllChildren();
		String dn = "";
		LDAPConnection ldc = getLDAPConnection();
		if ( ldc == null ) {
			Debug.println( "DSEntryObject.expandRoot: " +
						   "no LDAP connection" );
			return;
		}
		
		if (_selectedPartitionView.equals(DSContentModel.ALL)) {
			/* Root, above all suffixes. Read from the root DSE. */
			String[] attrs = { "namingcontexts" };			 
			LDAPEntry findEntry = readEntry( "", attrs );
			if ( findEntry == null ) {
				Debug.println( "DSEntryObject.expandRoot: readRoot " +
							   "returned null" );
				return;
			}
			checkObjectClasses( findEntry );
			LDAPAttribute attr = findEntry.getAttribute( attrs[0] );
			/* The attribute's values are the various suffixes */
			if ( attr != null ) {
				Enumeration e = attr.getStringValues();
				expandRootNode( e );
			}
			if ( _showPrivateSuffixes ) {
				/* Get private naming contexts */
				attrs[0] = "nsslapd-privatenamespaces";
				findEntry = readEntry( "cn=config", attrs );
				if ( findEntry == null ) {
					Debug.println( "DSEntryObject.expandRoot: cn=config " +
								   "returned null" );
				} else {
					attr = findEntry.getAttribute( attrs[0] );
					/* The attribute's values are the various suffixes */
					if ( attr != null ) {
						Enumeration e = attr.getStringValues();
						expandRootNode( e );
					}
				}
			}
		} else {
			String suffix = MappingUtils.getSuffixForBackend(ldc, _selectedPartitionView);	
			if (suffix != null) {
				expandRootNode(suffix);
			}
		}
		_fLoaded = true;
		_fContainer = true;
		_iChildren = super.getChildCount();
		Debug.println( "DSEntryObject.expandRoot found " + _iChildren +
						   " searchable suffixes" );
	}

	/**
	 * This method is used primarily by DSContentModel so that it can
	 * determine whether or not to allow the user to create a browsing
	 * index on this entry.
	 */
	public boolean isContainer() {
		return _fContainer;
	}

	private void initDataModel() {
		LDAPConnection ldc = getLDAPConnection();
		if ( ldc == null ) {
			Debug.println( "DSEntryObject.initDataModel: " +
						   "no LDAP connection" );
			return;
		}
		if ( _dataModel == null ) {
			_dataModel = new VListEntryModel( ldc,
											  getDN(),
											  ldc.SCOPE_ONE,
											  _childFilter,
											  _model );
			_dataModel.setReferralsEnabled( getReferralsEnabled() );
			_dataModel.setDebug( true );
		}
	}

	/**
	 *	create all the one level depth sub nodes that are containers.
	 *  if numSubOnly is true, getChildList will only check the one
	 *  level depth nodes for children, it will not recurse
	 */
	public void reload() {
		String dn = getDN();
		if ( _verbose )
			Debug.println( "DSEntryObject.reload for <" + dn + ">" );
		if ( isRootDSE() ) {
			expandRoot();
			return;
		}

		/* Get the child list for internal use */
		getChildList( true, false );
	}

	/**
	 *	create a vector of all the one level depth sub nodes.  returns true if the node
	 *  has child entries, false otherwise (including errors)
	 */
	private boolean getChildList( boolean addToSelf, boolean checkOnly ) {	
		String dn = getDN();	
		Debug.println(9, "DSEntryObject.getChildList for <" +
					  dn + "> check = " + checkOnly );			
		
		Vector v = new Vector();

		try {
			LDAPConnection ldc = getLDAPConnection();
			
			/* The numsubordinates attribute does not work between databases, so if the database 
			   root entry is the only descendant of an entry, this one will have numsubordinates = 0 (or it won't appear).
			   We get the list of suffixes for which this entry is the grand parent */
			
			if (_suffixes == null ) {
				_suffixes = DSContentModel.getSuffixes();
			}
			Vector grandSonSuffixList = new Vector();
			if (_suffixes != null) {
				DN entryDN = new DN(dn);			
				for (int i=0; i < _suffixes.length; i++) {
					DN suffixDN = new DN(_suffixes[i]);
					if ((suffixDN.getParent().getParent()).equals(entryDN)) {						
						if (grandSonSuffixList.indexOf(suffixDN) < 0) {
							// indexOf does not work with DN...
							boolean isAlreadyInVector = false;
							for (int j=0; j<grandSonSuffixList.size(); j++) {
								DN currentDN = (DN)grandSonSuffixList.elementAt(j);								
								if (currentDN.equals(suffixDN)) {
									isAlreadyInVector = true;
									break;
								}
							}
							if (!isAlreadyInVector) {								
								grandSonSuffixList.addElement(suffixDN); 
							}
						}
					}
				}
			}
			String[] attrs = MINIMAL_ATTRS;
			LDAPSearchConstraints cons =
				(LDAPSearchConstraints)ldc.getSearchConstraints().clone();
			cons.setMaxResults( 0 );			
			// ADD CONTROL
			LDAPSearchResults result;	
			if (_selectedPartitionView.equals(DSContentModel.ALL)) {
				if ( !getReferralsEnabled() ) {
					cons.setServerControls( _manageDSAITControl );
				}
				result =
					ldc.search( dn, ldc.SCOPE_ONE, _childFilter,
								attrs, false, cons );
			} else {
				if ( !getReferralsEnabled() ) {
					try {
						byte[] vals = _selectedPartitionView.getBytes( "UTF8" );
						LDAPControl searchControl = new LDAPControl(SEARCH_OID, true, vals);
						LDAPControl[] controls = {_manageDSAITControl, searchControl};
						cons.setServerControls(controls);
					} catch ( UnsupportedEncodingException e ) {
						Debug.println( "DSEntryObject.getChildList: Error: UTF8 not supported" );
						cons.setServerControls(_manageDSAITControl);
					}
				} else {
					try {
						byte[] vals = _selectedPartitionView.getBytes( "UTF8" );
						LDAPControl searchControl = new LDAPControl(SEARCH_OID, true, vals);					
						cons.setServerControls(searchControl);
					} catch ( UnsupportedEncodingException e ) {
						Debug.println( "DSEntryObject.getChildList: Error: UTF8 not supported" );
					}
				}	   
				result =
					ldc.search( dn, ldc.SCOPE_ONE, _childFilter,
								attrs, false, cons );
			}
			Debug.println(9, "DSEntryObject.getChildList search for <" +
						  dn + "> =" + result.hasMoreElements() +
						  " cons.getHopLimit()=" + cons.getHopLimit());
			while ( result.hasMoreElements() ) {
				LDAPEntry findEntry = (LDAPEntry)result.nextElement();				
				Debug.println(9, "DSEntryObject.getChildList adding <" +
							   findEntry.getDN() + "> check = " + checkOnly);
				/* We check if the current child of the list is the parent of one of the suffixes we found before.
				   If it is the case, we are handling them and we remove them from the list of Grand Son Suffixes */				
				DN childDN = new DN(findEntry.getDN());
				for (int i=grandSonSuffixList.size() -1; i >=0; i--) {
					DN suffixDN = (DN)grandSonSuffixList.elementAt(i);
					if (suffixDN.isDescendantOf(childDN)) {
						grandSonSuffixList.removeElementAt(i);						
					}
				}
				if ( checkOnly ) {
					if (getCountFromEntry(findEntry) > 0) {
						Debug.println(9, "DSEntryObject.getChildList: has children");
						ldc.abandon(result); // done
						return true; // child has children
					}
				} else {					
		            v.addElement( findEntry );
				}
			}
			ldc.abandon(result);

			/* Now we search for the entries that are the parents of the suffixes and that we didn't get
			 because numsubordinates does not work between different databases */
			Vector addedSuffixes = new Vector();
			for (int i=0; i< grandSonSuffixList.size(); i++) {
				DN suffixDN = (DN)grandSonSuffixList.elementAt(i);
				DN childToAddDN = suffixDN.getParent();
				
				/* We check that this entry was not already added.  An entry can be the parent of two (or more) suffixes.
				   So we might have several suffixes for just one entry.
				 */
				boolean isAlreadyAdded = false;
				for (int j=0; (j<addedSuffixes.size()) && !isAlreadyAdded; j++) {
					if (childToAddDN.equals((DN)addedSuffixes.elementAt(j))) {
						isAlreadyAdded = true;
					}
				}
				if (!isAlreadyAdded) {
					String childToAdd = childToAddDN.toRFCString();
					
					// check if the suffix entry exists...
					LDAPEntry suffixEntry = readEntry(suffixDN.toRFCString(), attrs, true);
					// check if the parent of the suffix entry exists...
					LDAPEntry findEntry = readEntry(childToAdd, attrs, true);
					if ((suffixEntry != null) &&
						(findEntry != null)) {
						if ( checkOnly ) {						
							return true; // child has children					
						} else {													
							v.addElement( findEntry );
							addedSuffixes.addElement(childToAddDN);
						}
					}				
				}
			}
			

			if (checkOnly) // no children have children
				return false;
			
			_iChildren = v.size();
			_fHasChildren = ( _iChildren > 0 );
			_fHasCheckedForChildren = true;
			Debug.println(9, "DSEntryObject.getChildList dn <" +
						  dn + "> nchildren=" + _iChildren );
		} catch (LDAPException e) {
			Debug.println( "DSEntryObject.getChildList cannot get " +
						   "entry <" + dn + ">" );
			return false;
		}		

        /* explode children */		
        if (addToSelf) {			
			removeAllChildren();
            int count = v.size ();
            int i;
            LDAPEntry entry;						
            for (i = 0; i < count; i++) {
				entry = (LDAPEntry)v.elementAt (i);				
				addOneChild( entry, true );
			}
        }
		/* For adding to self, the return value is ignored */
		return v == null;
	}

	/**
	 * check whether the node is loaded or not
	 *
	 * @return true if  the node is loaded, false otherwise.
	 */
	public boolean isLoaded() {
		return _fLoaded;
	}

	/**
	 * Create a list control with all children
	 *
	 * @return A panel with a list control with all children
	 */
	private DSEntryList createChildPanel() {
		/* If not the virtual root (above all defined suffixes), create
		   a virtual list view of the children */
		if ( !_isPrivateSuffix ) {			
			return new DSEntryList(
								   getLDAPConnection(), getDN(),
								   LDAPConnection.SCOPE_ONE, "|(objectclass=*)(objectclass=ldapsubentry)",
								   (DSBaseModel)_model, this );			
		} else {
			/* Make the suffixes appear as children to the root */
			Vector v = new Vector();
			Debug.println( "DSEntryObject.createChildPanel: root children =" );
			Enumeration e = children();
			while( e.hasMoreElements() ) {
				DSEntryObject deo = (DSEntryObject)e.nextElement();
				Debug.println( "  " + deo.getDN() );
				v.addElement( deo );
			}
			return new DSEntryList( v, (DSBaseModel)_model, this );
		}
	}

    private int getCountFromEntry( LDAPEntry entry ) {
		LDAPAttribute attr = entry.getAttribute( SUBORDINATE_ATTR );
		if ( attr != null ) {
			Enumeration e = attr.getStringValues();
			String s = (String)e.nextElement();
			int count = Integer.parseInt( s );
			if ( _verbose ) {
				Debug.println( "DSEntryObject.getCountFromEntry: " +
							   entry.getDN() + " = " + count );
			}
			return count;
		}
		return -1;
	}

	/**
	 *	return the attribute panel
	 *
	 * @return attribute panel of this DS entry node
	 */
	public Component getCustomPanel() {
		if (_isBogus)
			return null;	
		if (_customizePanel == null) {
			String dn = getDN();
			if ( _verbose )
				Debug.println( "DSEntryObject.getCustomPanel for <" +
							   dn + ">" );
			LDAPConnection ldc = getLDAPConnection();
			if ( ldc == null ) {
				Debug.println( "DSEntryObject.getCustomPanel: " +
							   "no LDAP connection" );
				return null;
			}
			LDAPEntry entry = readEntry( dn, MINIMAL_ATTRS );
			if ( entry == null ) {
				return null;
			}

			int count = getCountFromEntry( entry );
			Debug.println( "DSEntryObject.getCustomPanel: " +
						   "numsubordinates = " + count );
			initializeFromEntry( entry );
			/* If there are a lot of children to this node, and no
			   VLV index, don't even try to create a list */
			if ( (count > MAX_UNINDEXED_COUNT) &&
				 !nodeHasIndex( dn ) ) {
				return new NoListPanel();
			} else {
				/* Force collecting all children */
				if ( _verbose )
					Debug.println( "DSEntryObject.getCustomPanel: calling " +
								   "getChildCount()");
				int nChildren = getChildCount();
				if ( _verbose )
					Debug.println( "  Creating DSEntryList " +
								   "for <" + dn + ">" );
				_model.setWaitCursor(true);
				_customizePanel = createChildPanel();
				_model.setWaitCursor(false);
			}
		}
		if (_model instanceof DSContentModel) {
			((DSContentModel)_model).setDSEntryList(_customizePanel);
		}
		return _customizePanel;
	}

	class NoListPanel extends JPanel {
		NoListPanel() {
			super();
			String text = _resource.getString( _section, 
											   "too-many-children-label" );
			JLabel label = new JLabel( text );
			add( label );
		}
	}


	private boolean nodeHasIndex( String dn ) {
		return CreateVLVIndex.hasIndex(dn, (ConsoleInfo)_model.getServerInfo() );
	}

	public void initializeFromEntry( LDAPEntry findEntry ) {
		String dn = findEntry.getDN();
		if ( _verbose )
			Debug.println( "DSEntryObject.initializeFromEntry: " + dn );
		checkObjectClasses( findEntry );		
		checkForChildren( findEntry );
		if ( _verbose )
			Debug.println( "DSEntryObject.initializeFromEntry: " + dn );
		checkIfContainer();
		if ( !isRootDSE() ) {
			setIcon( checkIcon( _objectClasses, isLeafType() ) );
		}
		String displayName = getDisplayNameFromDN(_model, findEntry.getDN());
		setDisplayName( displayName );
		if ( _verbose )
			Debug.println( "DSEntryObject.initializeFromEntry: end dn=" + dn );
		_suffixes = null;
	}

	private boolean checkForChildren( LDAPEntry findEntry ) {
	    if ( _fHasCheckedForChildren ) {
			if ( _verbose )
				Debug.println( "DSEntryObject.checkForChildren: already " +
							   "checked - " + _fHasChildren );
	        return _fHasChildren;
		}

		if ( _verbose )
			Debug.println( "DSEntryObject.checkForChildren: Checking if <" +
						   findEntry.getDN() +
						   "> has children" );
		int nChildren = super.getChildCount();
		if ( nChildren > 0 ) {
			_iChildren = nChildren;
			setHasChildren( true );
			if ( _verbose )
				Debug.println( "... " + nChildren + " children" );
			return true;
		}
		int count = getCountFromEntry( findEntry );
		if ( count > 0 ) {
			setHasChildren( true );
			return true;
		}
		/* The numsubordinates attribute does not work between databases, so if the database 
		   root entry is the only descendant of an entry, this one will have numsubordinates = 0.
		   We get the list of suffixes for which this entry is the parent */
		
		if (_suffixes == null) {
			_suffixes = DSContentModel.getSuffixes();
		}
		if (_suffixes != null) {
			DN entryDN = new DN(findEntry.getDN());			
			for (int i=0; i < _suffixes.length; i++) {
				DN suffixDN = new DN(_suffixes[i]);
				if ((suffixDN.getParent()).equals(entryDN)) {					
					if (readEntry(suffixDN.toRFCString(), null, true) != null) {
						setHasChildren( true );	  
						return true;
					}										
				}
			}
		}		
		if ( _verbose )
			Debug.println( "... numsubordinates = " + count );
		setHasChildren( false );
		_iChildren = 0;
		return false;
	}

    private void checkObjectClasses( String[] names ) {
		if ( _verbose )
			Debug.println( "DSEntryObject.checkObjectClasses: " + getDN() );
		if ( _objectClasses != null )
			return;
		_objectClasses = new Hashtable();
		int i = 0;
		for( i = 0; i < names.length; i++ ) {
			_objectClasses.put( names[i].toLowerCase(), names[i] );
		}
		/* Create a hash code for the objectclass set */
		if ( i > 0 ) {
			DSUtil.trimAndBubbleSort( names, true );
			String s = names[0];
			for( i = 1; i < names.length; i++ ) {
				s += names[i];
			}
			_objectCode = DSUtil.getCRC32( s );
		}
	}

    private void checkObjectClasses( LDAPEntry findEntry ) {
		if ( _objectClasses != null )
			return;
		LDAPAttribute attr = findEntry.getAttribute(
			OBJECTCLASS_ATTR );
		String[] names = { "top" };;
		/* attr should never be null, but there is a bug in
		   "cn=monitor,cn=ldbm" */
		if ( attr != null ) {
			names = new String[attr.size()];
			Enumeration e = attr.getStringValues();
			int i = 0;
			while ( e.hasMoreElements() ) {
				names[i] = (String)e.nextElement();
				i++;
			}
		}
		checkObjectClasses( names );
	}

    public long getObjectClassCode() {
		return _objectCode;
	}

    private void checkIfContainer() {
		if ( _verbose )
			Debug.println( "DSEntryObject.checkIfContainer: <" +
						   getDN() + ">" );
		if ( _fHasChildren ) {
			if ( _verbose )
                Debug.println( "  It has children, so yes" );
			_fContainer = true;
			return;
		}
		Enumeration e = _objectClasses.elements();
		while ( e.hasMoreElements() ) {
			String s = (String)e.nextElement();
			if ( _verbose )
				Debug.println( "  Looking up object class " + s );
			if ( _cContainers.get( s.toLowerCase() ) != null ) {
				if ( _verbose )
					Debug.println( "  It is an " + s + ", so yes" );
				_fContainer = true;
				return;
			}
		}
		if ( _verbose )
			Debug.println( "  ...no" );
	}

	RemoteImage checkIcon( Hashtable objectClasses,
								  boolean isLeafNode ) {
		if ( _verbose )
			Debug.println( "DSEntryObject.checkIcon: isLeaf = " + isLeafNode );
		String iconName = "";
		Enumeration e = objectClasses.keys();
		while ( e.hasMoreElements() ) {
			String s = ((String)e.nextElement()).toLowerCase();
			iconName = (String)_icons.get( s );
			if ( iconName == null ) {
				iconName = _resource.getString( _section,
												s+"-icon" );
				if ( iconName == null )
					iconName = "";
				_icons.put( s, iconName );
			}
			if ( !iconName.equals( "" ) )
				break;
		}
		if ( iconName.equals( "" ) ) {
			if ( isLeafNode )
				iconName = _defaultImageName;
			else
				iconName = _defaultFolderImageName;
		}
		
		if (!isActivated()) {
			if (isRole()) {
				return DSUtil.getInactivatedRolePackageImage(iconName);
			} else {
				return DSUtil.getInactivatedPackageImage(iconName);
			}
		} else {
			return DSUtil.getPackageImage(iconName);
		}
	}

	private static String getFirstValue( LDAPEntry entry, String attrName ) {
		LDAPAttribute attr = entry.getAttribute( attrName );
		if ( attr != null ) {
			Enumeration e = attr.getStringValues();
			if ( e.hasMoreElements() ) {
				return (String)e.nextElement();
			}
		}
		return null;
	}

    /* 
	 * Figure out a good display name for a given dn
	 *
	 * @param model Content model.
	 * @param dn of the directory entry
	 * @returns the String with the display name 
	 */				   
	protected String getDisplayNameFromDN( IDSModel model, String dn ) {
		String displayName = "";
		if ( !isRootDSE( dn ) ) {
			String[] rdns = LDAPDN.explodeDN( dn, true );
			if ( (rdns != null) && (rdns[0] != null) ) {
				displayName = rdns[0];
			} else {
				displayName = dn;
				Debug.println( "DSEntryObject.checkDisplayName: cannot explode " +
							   dn );
			}
		} else {
			if ( model != null ) {
				ConsoleInfo info = model.getServerInfo();
				LDAPConnection ldc = info.getLDAPConnection();
				if ( ldc != null ) {
					displayName = ldc.getHost() + ":" + ldc.getPort();
				} else {
					displayName = info.getHost() + ":" + info.getPort();
				}
			}
		}
		return displayName;
	}

	void setHasChildren( boolean has ) {
		if ( _verbose )
			Debug.println( "DSEntryObject.setHasChildren: <" +
						   getDN() + "> - " + has );
		_fHasChildren = has;
        _fHasCheckedForChildren = true;
	}

	void setHasGrandChildren( boolean has ) {
		if ( _verbose ) {
			Debug.println( "DSEntryObject.setHasGrandChildren: <" +
						   getDN() + "> - " + has );
		}
		_fHasGrandChildren = has;
		_fHasCheckedForGrandChildren = true;
	}

    /**
	 * Return a specific child of this node, by index. This currently
	 * assumes that all nodes in the tree are explicitly managed by
	 * JFC (and not by a virtual tree where we supply the contents)
	 *
	 * @param index Zero-based index of child to return
	 * @return The node at the requested index, or null
	 */
    public javax.swing.tree.TreeNode getChildAt( int index ) {
		javax.swing.tree.TreeNode node = null;
		if ( _verbose )
			Debug.println( "DSEntryObject.getChildAt: <" +
						   getDN() + "> index " + index );
		if ( _iChildren < 0 ) {
			reload();
		}
		try {
			node = super.getChildAt( index );
		} catch ( Exception e ) {
			/* Request for node outside of range */
		}
		if ( node != null )
			((DSEntryObject)node).setParentNode( this );
		return node;
	}

    /**
	 * Report the number of children (containers only) of this node
	 *
	 * @return The number of container nodes that are children of this node
	 */
    public int getChildCount() {
		if ( _verbose ) {
			Debug.println( "DSEntryObject.getChildCount for <" + getDN() + ">" );
			Thread.dumpStack();
		}
		if ( _iChildren < 0 ) {
			_model.setWaitCursor(true);
			reload();
			_model.setWaitCursor(false);
		}
		int count = super.getChildCount();
		if ( _verbose )
			Debug.println( "DSEntryObject.getChildCount: <" +
						   getDN() + "> - " + count );			
		return count;
	}

    /**
	 * Remove all children of this node
	 */
    public void removeAllChildren() {
		_iChildren = 0;
		super.removeAllChildren();
	}

	/**
	 * Check whether the node is a leaf node or not. Since this is used
	 * by JTree to determine whether or not put an expander on the tree,
	 * return true if the node currently has no children.
	 *
	 * A node is a leaf node if it has no children OR
	 * if it has no grandchildren.  The intention is that leaf nodes
	 * will be displayed on the right hand pane.
	 *
	 * @param node node to be checked
	 * @return true if the node is leaf, false otherwise.
	 */
	public boolean isLeaf() {
	    if ( !_fHasCheckedForChildren && !isLoaded() ) {
			if ( _verbose )
				Debug.println( "DSEntryObject.isLeaf: <" + getDN() +
							   "> before load()" );
			load();
			if ( _verbose )
				Debug.println( "DSEntryObject.isLeaf: <" + getDN() +
							   "> after load()" );
		}
		if ( _verbose )
			Debug.println( "DSEntryObject.isLeaf: <" + getDN() +
						   ">  - " + !_fContainer );

		if ( !_fHasCheckedForGrandChildren) {
			_fHasGrandChildren = hasGrandchildren();
			_fHasCheckedForGrandChildren = true;
		}

		return !( _fHasGrandChildren || (getChildCount() > 0));
	}

	private boolean hasGrandchildren() {
		// if the tree has already been initialized, if the node has
		// children, then assume those children have children
		if (super.getChildCount() > 0)
			return true;

		// the easy case - the node has no children, so it cannot
		// have grandchildren		
		if (_fHasCheckedForChildren && !_fHasChildren)
			return false;

		// the hard case - we are doing initialization - we need to check
		// the numsub attr of each child of this entry to see if any of them
		// are > 0; note that this is wasteful in that it reads the child
		// entries but does not add them to the tree; this must be done in
		// a second pass if the node is expanded
		return getChildList(false, true);
	}

		

	/**
	 * Check whether the node is a of leaf node type or not. For this
	 * to be true, it must not have children and it must not be of a
	 * container type.
	 *
	 * @param node node to be checked
	 * @return true if the node is leaf type, false otherwise.
	 */
	private boolean isLeafType() {
		isLeaf();
		return ( !_fContainer );
	}


    /**
     * Return an appropriate display name for a list or tree
     *
     * @return An appropriate display string
     **/
    public String getDisplayName () {
		if (_sDisplayName == null) {
			Debug.println(9, "DSEntryObject.getDisplayName is null");
		}
		return _sDisplayName;
    }

    /**
     * Set an appropriate display name for a list or tree
     *
     * @param s An appropriate display string
     **/
    public void setDisplayName ( String s ) {
		_sDisplayName = s;
    }

    public void setShowAliases( boolean showAliases ) {
		_showAliases = showAliases;
	}


	private DSEntryDialog doGenericDialog( boolean modal ) {
		return doGenericDialog(modal, DSEntryPanel.SHOWINGDN_NONAMING);
	}

    private DSEntryDialog doGenericDialog( boolean modal, int options ) {
		_model.setWaitCursor( true );
		/* Make a list of attribute names */
		LDAPAttributeSet set = _entry.getAttributeSet();
		int i = 0;
        LDAPSchema schema = _model.getSchema();
        
        Enumeration attrSet = set.getAttributes();
        Vector v = new Vector();
		while( attrSet.hasMoreElements() ) {
			LDAPAttribute attr = (LDAPAttribute)attrSet.nextElement();
			
			v.addElement( attr.getName().toLowerCase() );						
		}		
		
		String[] names = new String[v.size()];
		String[] labels = new String[v.size()];
		for( i = 0; i < names.length; i++ )
		    names[i] = (String)v.elementAt( i );

		/* Look up a friendly presentation of each attribute */
		if ( _showAliases ) {
			AttributeAlias.getAliases( names, labels );
		} else {
			/* Sort the attributes alphabetically by name */
			DSUtil.trimAndBubbleSort( names, true );
			for( i = 0; i < names.length; i++ ) {
				labels[i] = names[i];
			}
		}

		/* Make a single page with all attributes */
		EntryPageDescription page =
			new EntryPageDescription( names, labels );
		/* Data model for property editor */
		DSPropertyModel model = new DSPropertyModel( _model.getSchema(),
													 _entry, page );
		/* Editor window */
		DSEntryPanel child = new DSEntryPanel( model , options, _model.getServerInfo().getLDAPConnection());
		/* Do it as a dialog */
		DSEntryDialog dlg = new DSEntryDialog( _model.getFrame(), child );
		dlg.setTitle( _resource.getString( _section, "property-dialog-title",
										   getDisplayName() ) );
		dlg.setLocationRelativeTo(_model.getFrame());
		_model.setWaitCursor( false );
		if (Debug.getTrace() && Debug.getTraceLevel() > 7) {
			System.out.println("DSEntryObject.doGenericDialog: stack trace:");
			Thread.dumpStack();
		}
		dlg.packAndShow();
		DSUtil.dialogCleanup();
		if ( !dlg.isCancel() ) {
			return dlg;
		}
		return null;
	}

    DSEntryDialog editGenericDialog( boolean modal ) {
		return doGenericDialog( modal );
	}
							   
	DSEntryDialog editGenericDialog( boolean modal, int options ) {
		return doGenericDialog( modal, options );
	}

    DSEntryDialog addGenericDialog( boolean modal ) {
		return doGenericDialog( modal );
	}
	
	DSEntryDialog addGenericDialog( boolean modal, int options ) {
		return doGenericDialog( modal, options );
	}

	/**
	 * Put up a generic property editing dialog for a single DS object.
	 *
	 * @param modal true if the dialog is to be modal
	 * @param save  true if the changed information should be saved;
	 *              for example, if this dialog is being called as the
	 *              Advanced dialog from the UserGroup editor, then
	 *              the UserGroup editor is responsible for saving the
	 *              changes back to the directory, and the DSEntryDialog
	 *              should just report back those changes to the
	 *              UserGroup dialog
	 *              If this dialog is being called as a generic editor
	 *              directly, then save should be true
	 */
    public DSEntryDialog editGeneric( boolean modal, boolean save ) {
		if (_isBogus)
			return null;

		DSEntryDialog dlg = editGenericDialog( modal );
		if (!save)
			return dlg;
		
		if (dlg == null) {
			return null;
		} else {			
			LDAPModificationSet mods = dlg.getChanges();
			if ( mods != null ) {												
				DN oldDN = new DN(getDN());
				DN newDN = new DN(dlg.getDN());
				
				String olddn = getDN();

				boolean modrdn=false;
						
				if (!oldDN.equals(newDN)) {
					setDN(dlg.getDN());
					modrdn = true;
				}				
			
				if ( modrdn ) {
					String[] rdns = LDAPDN.explodeDN(getDN(), false);
					String newrdn = rdns[0];
					String dn = getDN();
					try {						
						getLDAPConnection().rename( olddn, newrdn, true );
					} catch ( LDAPException ex ) {
						/* Something went wrong when renaming: go back to the old dn */
						setDN(olddn);
						if ( ex.getLDAPResultCode() != ex.NO_SUCH_OBJECT ) {
							Debug.println( "DSEntryObject.editGeneric: renaming <" +
										   olddn +
										   "> to <" + getDN() + ">, " + ex );
							DSUtil.showLDAPErrorDialog( _model.getFrame(),
														ex, "111-title" );
							return null;
						} else if ( ex.getLDAPResultCode() != ex.INVALID_DN_SYNTAX ) {
							if (dlg.isValidDN()) {
								DSUtil.showLDAPErrorDialog( _model.getFrame(),
															ex, "111-title" );
							}
							return null;
						}
					}
					if ( !saveChanges( mods, dlg.getAttributes() ) ) {
						dlg = null;
					}
					DSEntryObject deo = (DSEntryObject)getParentNode();
					if ( deo != null ) {
						deo.redisplay();
					} else {
						redisplay();
					}
					return dlg;
				} else {
					return saveChanges( mods, dlg.getAttributes() ) ? dlg : null;
				}
			}
		}
		return null;
	}

	/**
	 * Put up a generic property editing dialog for a single DS object.
	 *
	 * @param modal true if the dialog is to be modal
	 */
    public boolean addGeneric( boolean modal ) {
		LDAPAttributeSet attrs = null;
		DSEntryDialog dlg = addGenericDialog( modal, DSEntryPanel.SHOWINGDN_NAMING );
		if ( dlg != null ) {
			attrs = dlg.getAttributes();
		}
		if ( attrs == null ) {
			return false;
		}
		
		setDN(dlg.getDN());
		return addEntry( attrs, dlg );
	}


	/**
	 * Put up a generic property editing dialog for a single DS object.
	 * The naming attribute can not be edited
	 *
	 * @param modal true if the dialog is to be modal
	 */
    public boolean addGenericNoNaming( boolean modal ) {
		LDAPAttributeSet attrs = null;
		DSEntryDialog dlg = addGenericDialog( modal, DSEntryPanel.SHOWINGDN_NONAMING );
		if ( dlg != null ) {
			attrs = dlg.getAttributes();
		}
		if ( attrs == null ) {
			return false;
		}
		
		setDN(dlg.getDN());
		return addEntry( attrs, dlg );
	}

	/**
	 * Put up a generic property editing dialog for any DS object(s).
	 *
	 * @param list One or more objects to edit
	 * @param modal true if the dialog is to be modal
	 */
    public DSEntryDialog editGeneric( IResourceObject[] list, boolean modal ) {
		DSEntryDialog dlg = editGenericDialog( modal, DSEntryPanel.NOSHOWINGDN );
		if ( dlg != null ) {
			LDAPModificationSet mods = dlg.getChanges();
			if ( mods != null ) {
				for( int i = 0; i < list.length; i++ ) {
					if ( !((DSEntryObject)list[i]).saveChanges( mods,
													   dlg.getAttributes()) )
						return null;
				}
				return dlg;
			}
		}
		return null;
	}

    boolean saveChanges( LDAPModificationSet mods, LDAPAttributeSet set ) {
		/* Loop here, in case reauthentication is required */
		LDAPConnection ldc = getLDAPConnection();
		if ( ldc == null ) {
			Debug.println( "DSEntryObject.saveChanges: no LDAP connection" );
			return false;
		}
		LDAPSearchConstraints cons =
			(LDAPSearchConstraints)ldc.getSearchConstraints().clone();
		if ( !getReferralsEnabled() ) {
			cons.setServerControls( _manageDSAITControl );
		}
		try {
			while ( mods != null ) {
				try {
					ldc.modify( getDN(), mods, cons );
					/* If we got past that, we succeded */
					/* Best to refresh from the Directory */
					_entry = null;
					getEntry();
					return true;
				} catch ( LDAPException e ) {
					Debug.println( "Modifying " + getDN() + ", " + e );
					/* Allow reauthenticating on insufficient privileges */
					if ( e.getLDAPResultCode() ==
						 e.INSUFFICIENT_ACCESS_RIGHTS ) {
						/* Put up dialog indicating the problem */
						DSUtil.showPermissionDialog( _model.getFrame(), ldc );
						/* If the user cancels reauthentication, bail */
						if ( !_model.getNewAuthentication(false) )
							return false;
					} else if ( e.getLDAPResultCode() ==
								e.NO_SUCH_OBJECT ) {
						/* Entry doesn't exist, try adding it */
						return addEntry( set );
					} else {
						/* Some other error. Just show a dialog. */
						DSUtil.showLDAPErrorDialog( _model.getFrame(),
													e, "111-title" );
						return false;
					}
				}
			}
		} finally {
			// make sure we notify others if we changed authentication above
			_model.notifyAuthChangeListeners();
		}
		return false;
	}

    boolean addEntry( LDAPAttributeSet attrs ) {
		return addEntry(attrs, null);
	}

    boolean addEntry( LDAPAttributeSet attrs, DSEntryDialog dlg ) {
		/* Loop here, in case reauthentication is required */
		LDAPConnection ldc = getLDAPConnection();
		if ( ldc == null ) {
			Debug.println( "DSEntryObject.addEntry: no LDAP connection" );
			return false;
		}
		LDAPEntry entry = new LDAPEntry( getDN(), attrs );
		try {
			while ( true ) {
				try {
					ldc.add( entry );
					/* If we got past that, we succeded */
					/* Best to refresh from the Directory */
					_entry = null;
					getEntry();
					/* We see if we added one of the root suffixes, and if so, tell to update the menu */
					if (_model instanceof DSContentModel) {
						Vector listOfSuffix = ((DSContentModel)_model).getSuffixWithNoEntryList();
						DN dn1 = new DN (getDN());
						for (int i=0; i< listOfSuffix.size(); i++) {
							DN dn2 = new DN((String) listOfSuffix.elementAt(i));
							if (dn1.equals(dn2)) {
								((DSContentModel)_model).updateNewRootEntryMenu(true);
							}
						}
					}					
					/* If we are here we added the entry:  now we try to see if the user can see it or not,
					   in order to display a dialog telling him/her why he/she can't see it */
					if (!_selectedPartitionView.equals(DSContentModel.ALL)) {
						LDAPSearchConstraints cons =
							(LDAPSearchConstraints)ldc.getSearchConstraints().clone();
						byte[] vals = null;
						try {
							vals = _selectedPartitionView.getBytes( "UTF8" );
						} catch ( UnsupportedEncodingException uEEx ) {
							Debug.println( "DSEntryObject.addEntry() "+uEEx);
							return true;
						}			
						LDAPControl searchControl = new LDAPControl(SEARCH_OID, true, vals);
						cons.setServerControls(searchControl);
						cons.setMaxResults( 0 );
						try {										
							entry = DSUtil.readEntry(ldc, getDN(), null, cons);
							if (entry == null) {																					
								DSUtil.showInformationDialog(_model.getFrame(),"add-entry-to-different-partition", (String)null);
							}
						} catch (LDAPException lde) {
							if (lde.getLDAPResultCode() == LDAPException.NO_SUCH_OBJECT) {
								DSUtil.showInformationDialog(_model.getFrame(),"add-entry-to-different-partition", (String)null);
							} else {
								Debug.println("DSEntryObject.addEntry() " +lde);
							}
						}
					}
					return true;
				} catch ( LDAPException e ) {
					Debug.println( "Adding " + getDN() + ", " + e );
					/* Allow reauthenticating on insufficient privileges */
					if ( e.getLDAPResultCode() ==
						 e.INSUFFICIENT_ACCESS_RIGHTS ) {
						/* Put up dialog indicating the problem */
						DSUtil.showPermissionDialog( _model.getFrame(), ldc );
						/* If the user cancels reauthentication, bail */
						if ( !_model.getNewAuthentication(false) )
							return false;
					} else if ( e.getLDAPResultCode() ==
								e.NO_SUCH_OBJECT ) {
						/* Put up dialog indicating the problem */
						DSUtil.showErrorDialog(_model.getFrame(), "noParentForAdd",
											   "", _section);
						return false;
					} else if ( e.getLDAPResultCode() ==
								e.INVALID_DN_SYNTAX ) {
						if (dlg != null) {
							if (dlg.isValidDN()) {
								DSUtil.showLDAPErrorDialog( _model.getFrame(),
															e, "111-title" );
							}
						}
						return false;
					} else {
						/* Some other error. Just show a dialog. */
						DSUtil.showLDAPErrorDialog( _model.getFrame(),
													e, "111-title" );
						return false;
					}
				}
			}
		} finally {
			// make sure we notify others if we changed authentication above
			_model.notifyAuthChangeListeners();
		}
	}

   void newPredefinedObject( Vector objectClass ) {
		_model.setWaitCursor( true );
		 ConsoleInfo info = (ConsoleInfo)_model.getConsoleInfo().clone();
		 ConsoleInfo serverInfo = (ConsoleInfo)_model.getServerInfo();
		 info.setLDAPConnection( serverInfo.getLDAPConnection() );
		 info.setAuthenticationDN( serverInfo.getAuthenticationDN() );
		 info.setAuthenticationPassword( serverInfo.getAuthenticationPassword() );
		 info.setUserLDAPConnection( serverInfo.getLDAPConnection());
		 info.setUserHost( serverInfo.getLDAPConnection().getHost());
		 info.setUserPort( serverInfo.getLDAPConnection().getPort());
		 String topSuffix = MappingUtils.getTopSuffixForEntry(getDN(), DSContentModel.getSuffixes());
		 if (topSuffix != null) {
			 info.setUserBaseDN(topSuffix);
		 }		 
		 info.put( "NewUserBaseDN", getDN() );

		 //DSAdmin.dumpConsoleInfo( info );
		 ResourceEditor ed = null;
         try {
			 ed = new DSResourceEditor( _model.getFrame(),
								 info, objectClass, getDN() );
			_model.setWaitCursor( false );
         }
         catch(ArrayIndexOutOfBoundsException x) {
         	// This exception means that the resource extension for
            // objectClass is not correctly setup.
            // It's probably for a role entry.
         	Debug.println("DSEntryObject.newPredefinedObject: got" + x +
                          " while instantiating a resource editor");

			// We popup a dialog to warn the user
    		_model.setWaitCursor(false);
            JOptionPane.showMessageDialog(null,
                    _resource.getString(_section, "role-extension-error"),
                    _resource.getString(_section, "role-extension-title"), 
                    JOptionPane.ERROR_MESSAGE);
            ModalDialogUtil.sleep();
         }
         
         if (ed != null) {
		     String buttonText =
			     _resource.getString( "UserGroup", "Advanced-label" );
		     /* Install an "Advanced" button for generic editing */
		     ed.registerAdvancedOption( new UserGroupAdapter(
			     buttonText ) );
            ed.showModal();
            boolean saved = ed.getSaveStatus();
			ModalDialogUtil.disposeAndRaise(ed, _model.getFrame());
            if ( saved ) {
				LDAPConnection ldc = getLDAPConnection();
				String dn = ed.getLDAPEntry().getDN();
				/* We see if we added one of the root suffixes, and if so, tell to update the menu */
				if (_model instanceof DSContentModel) {
					Vector listOfSuffix = ((DSContentModel)_model).getSuffixWithNoEntryList();
					DN dn1 = new DN (dn);
					for (int i=0; i< listOfSuffix.size(); i++) {
						DN dn2 = new DN((String) listOfSuffix.elementAt(i));
						if (dn1.equals(dn2)) {
							((DSContentModel)_model).updateNewRootEntryMenu(true);
						}
					}
				}
				/* If we are here we added the entry:  now we try to see if the user can see it or not,
				   in order to display a dialog telling him/her why he/she can't see it */
				if (!_selectedPartitionView.equals(DSContentModel.ALL)) {
					LDAPSearchConstraints cons =
						(LDAPSearchConstraints)ldc.getSearchConstraints().clone();
					byte[] vals = null;
					try {
						vals = _selectedPartitionView.getBytes( "UTF8" );
					} catch ( UnsupportedEncodingException uEEx ) {
						Debug.println( "DSEntryObject.addEntry() "+uEEx);
						updateInTree();
						return;
					}			
					LDAPControl searchControl = new LDAPControl(SEARCH_OID, true, vals);
						cons.setServerControls(searchControl);
						cons.setMaxResults( 0 );
						try {										
							LDAPEntry entry = DSUtil.readEntry(ldc, dn, null, cons);
							if (entry == null) {																					
								DSUtil.showInformationDialog(_model.getFrame(),"add-entry-to-different-partition", (String)null);
							}
						} catch (LDAPException lde) {
							if (lde.getLDAPResultCode() == LDAPException.NO_SUCH_OBJECT) {
								DSUtil.showInformationDialog(_model.getFrame(),"add-entry-to-different-partition", (String)null);
							} else {
								Debug.println("DSEntryObject.addEntry() " +lde);
							}
						}
				}
				reload(); // to show a new container child when one is added
				updateInTree();
            }
        }
    }

	/**
	 * Check if this note is to be managed by a virtual tree, and add it
	 * if so to that tree; check if this is a container itself.
	 */
	private void updateInTree() {
		if (getParent() == null) {
			DSEntryObject pdeo = (DSEntryObject)getParentNode();
			if (!pdeo.childExists(this)) {				
				pdeo.add(this);
			}
		}
		setHasChildren(true);
		if (getParentNode() != null) {
			((DSEntryObject)getParentNode()).setHasGrandChildren(true);
		}
		checkIfContainer();
		redisplay();
	}

	private void redisplay() {
		/* Force regeneration of the tree */
		 _model.fireTreeStructureChanged( this );
		_model.setSelectedNode( (ResourceObject)this );
		/* Force a regeneration of the right-hand pane */
		if ( _customizePanel != null )
			_customizePanel.recalculate();
	}

	 /**
	  * Create a new user node under this node
	  */
	 public void newUser() {
		Vector objectClass = (Vector) ResourceEditor.getNewObjectClasses().get(ResourceEditor.KEY_NEW_USER_OBJECTCLASSES);

		newPredefinedObject( objectClass );
	}

	 /**
	  * Create a new groupOfUniqueNames node under this node
	  */
    public void newGroup() {
		Vector objectClass = (Vector) ResourceEditor.getNewObjectClasses().get(ResourceEditor.KEY_NEW_GROUP_OBJECTCLASSES);
		newPredefinedObject( objectClass );
	}



	 /**
	  * Create a new organizationalUnit node under this node
	  */
    public void newOrganizationalUnit() {
		Vector objectClass = (Vector) ResourceEditor.getNewObjectClasses().get(ResourceEditor.KEY_NEW_OU_OBJECTCLASSES);
		newPredefinedObject( objectClass );
	}

	 /**
	  * Create a new nsManagedRoleDefinition node under this node
	  */
    public void newRole() {
		Vector objectClass = new Vector();
		objectClass.addElement("top");
		objectClass.addElement("ldapsubentry");
		objectClass.addElement("nsroledefinition");

		newPredefinedObject( objectClass );
	}

	 /**
	  * Create a new nsManagedRoleDefinition node under this node
	  */
    public void newCos() {
		Vector objectClass = new Vector();
		objectClass.addElement("top");
		objectClass.addElement("ldapsubentry");
		objectClass.addElement("cossuperdefinition");	
		newPredefinedObject( objectClass );
	}

	/**
	 * Check if an object class contains among its required or optional
	 * attributes one of the specified ones.
	 */
    private String findPrefix( String oclass, String[] requested,
							   boolean required ) {
		String prefix = null;
		if ( oclass != null ) {
			/* Get all required attributes */
			LDAPObjectClassSchema oc =
				_model.getSchema().getObjectClass( oclass );
			if ( oc == null ) {
				oclass = null;
				return null;
			}
			Enumeration e;
			if ( required ) {
				Hashtable ht = new Hashtable();
				DSSchemaHelper.allRequiredAttributes(oclass, _model.getSchema(), ht);
				e = ht.elements();	
			} else {		
				Hashtable ht = new Hashtable();
				DSSchemaHelper.allOptionalAttributes(oclass, _model.getSchema(), ht);
				e = ht.elements();					
			}
			while( e.hasMoreElements() ) {
				String name = (String)e.nextElement();
				for( int i = 0; i < requested.length; i++ ) {
					if ( name.equalsIgnoreCase( requested[i] ) ) {
						prefix = requested[i];
						break;
					}
				}
				if ( prefix != null ) {
					break;
				}
			}			
		}
		return prefix;
	}

    private String findRequiredPrefix( String oclass ) {
		return findPrefix( oclass, PREFIXES, true );
	}

    private String findOptionalPrefix( String oclass ) {
		return findPrefix( oclass, PREFIXES, false );
	}

	 /**
	  * Prompt for an objectclass and then put up a generic property
	  * editor for a new child object to this node
	  */
    public void newObject() {

		// Display the class chooser dialog
		LDAPSchema schema = _model.getSchema();
		JFrame frame = _model.getFrame();
		ChooseObjectClassDialog dlg = new ChooseObjectClassDialog(frame, schema);
		dlg.show();
		dlg.dispose();
		
		if ( ! dlg.isCancel() ) {
			String selectedValue = dlg.getSelectedValue();
			Vector v = getObjectClassVector( selectedValue, schema );

			if ( isStandardObjectClass( v.elements(),
										_model.getConsoleInfo() ) ) {
				/* Yes, it is. So call the UserGroup editor. */
				newPredefinedObject( v );
				return;
			}

			/* Not a standard object class, so do it ourselves */
			/* Get all required attributes */
			Hashtable ht = new Hashtable();
			DSSchemaHelper.allRequiredAttributes(selectedValue, schema, ht);	
			Enumeration e = ht.elements();
	
			LDAPAttributeSet set = new LDAPAttributeSet();
			String anAttr = null;
			while( e.hasMoreElements() ) {
				String name = (String)e.nextElement();

				LDAPAttribute attr = new LDAPAttribute( name );
				/* Initialize the objectclass attribute with the
				   chain of superiors */
				if ( name.equalsIgnoreCase( OBJECTCLASS_ATTR ) ) {
					v = getObjectClassVector( selectedValue, schema );
					for( int i = v.size() - 1; i >= 0; i-- ) {
						attr.addValue( (String)v.elementAt( i ) );
					}
				} else {
					/* Not objectclass, initialize with the empty string */
					attr.addValue( "" );
					if ( (anAttr == null) && !name.equalsIgnoreCase( ACI_ATTR ) ) {
						anAttr = name;
					}
				}
				set.add( attr );
			}							
							
			/* Does the object class require one of the common RDN
			   attributes? */
			String prefix = findRequiredPrefix( selectedValue );			
			if ( prefix == null )
				prefix = findOptionalPrefix( selectedValue );
			if ( prefix == null ) {
				for( int i = 0; i < PREFIXES.length; i++ ) {
					if ( set.getAttribute( PREFIXES[i] ) != null ) {
						prefix = PREFIXES[i];
						break;
					}
				}
			}
			/* If none of the common attributes is required, optional, or defined,
			   use the first required attribute which is not objectclass */
			if ( prefix == null ) {
				prefix = anAttr;
			}
			/* Last resort, first optional attribute */
			if ( prefix == null ) {	
				ht = new Hashtable();	
				DSSchemaHelper.allOptionalAttributes(selectedValue, schema, ht);
				e = ht.elements();				
	
				if ( e.hasMoreElements() ) {
					prefix = (String)e.nextElement();
					/* aci is not valid for use as an RDN */
					if ( prefix.equalsIgnoreCase( ACI_ATTR ) ) {
						if ( e.hasMoreElements() ) {
							prefix = (String)e.nextElement();
						} else {
							prefix = null;
						}
					}
				}
			}
			/* Can't figure out what attribute to use for RDN */
			if ( prefix == null ) {
				DSUtil.showErrorDialog( _model.getFrame(), "noRDN", selectedValue,
										_section );
				return;
			}

			/* Preliminary DN ??? */
			String value = DEFAULT_NEW;
			LDAPAttribute attr = set.getAttribute( prefix );
			if ( attr != null ) {
				Debug.println( "DSEntryObject.newObject: prefix attribute = " + attr );
				Enumeration en = attr.getStringValues();
				if ( (en != null) && en.hasMoreElements() ) {
					value = (String)en.nextElement();
					if ( value.length() < 1 ) {
						value = DEFAULT_NEW;
						set.remove( prefix );
						set.add( new LDAPAttribute( prefix, value ) );
					}
				} else {
					attr.addValue( value );
				}
			} else {
				attr = new LDAPAttribute( prefix, value );
				set.add( attr );
			}
			String dn = prefix + "=" + value;
			String parent = getDN();
			if ( (parent != null) && (parent.length() > 0) ) {
				DN newDN = new DN( parent );
				newDN.addRDN( new RDN( dn ) );
				dn = newDN.toRFCString();
			}
			LDAPEntry entry = new LDAPEntry( dn, set );
			DSEntryObject deo = nodeFromEntry( entry );
			Debug.println( "DSEntryObject.newObject: editing entry: " +
						   entry );
			if ( deo.addGeneric( true ) ) {
				reload(); // to show a new container child when one is added 
				updateInTree();
			}
		}
	}

	protected void getObjectClassVector( String name, LDAPSchema schema, Vector v) {
		DSSchemaHelper.getObjectClassVector( name, schema, v);
	}

    protected Vector getObjectClassVector( String name,
												LDAPSchema schema ) {
		return DSSchemaHelper.getObjectClassVector( name, schema);
	}

	static private boolean isStandardObjectClass( Enumeration en,
												  ConsoleInfo info ) {
		/* Check if this has an objectclass known to the UserGroup
		   editor. Get the object class to java class association. */
		Hashtable htable = ResourceEditor.getResourceEditorExtension();
		if ( htable == null ) {
			Debug.println( "DSEntryObject.isStandardObjectClass: " +
                           "No resourceEditorExtension in ConsoleInfo" );
			return false;
		}
		while( en.hasMoreElements() ) {
			/* Check each object class of this entry to see if
			   it is one of the known ones. */
			String name = ((String)en.nextElement()).toLowerCase();
			if ( htable.get( name ) != null ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Put up a generic property editor for this node
	 */
    public void editProperties() {
		if (_isBogus)
			return;

		_model.setWaitCursor( true );
		try {
			IDSEntryObject deo = getParentNode();
			Debug.println( "DSEntryObject.editProperties: model = " +
						   _model + ", panel = " + _customizePanel +
						   ", parent node = " + deo );
			// Force reading of entire object

			String name = getDisplayName();
			_entry = null;
			getEntry();
			if ( _entry == null ) {
				DSUtil.showErrorDialog( _model.getFrame(),
										"entryNotFound", name );
				_model.removeElement( this );
				return;
			}						


			ConsoleInfo info = (ConsoleInfo)_model.getServerInfo().clone();
			info.setUserLDAPConnection(info.getLDAPConnection());
			info.setUserHost(info.getLDAPConnection().getHost());
			info.setUserPort(info.getLDAPConnection().getPort());
			String topSuffix = MappingUtils.getTopSuffixForEntry(getDN(), DSContentModel.getSuffixes());
			if (topSuffix != null) {
				info.setUserBaseDN(topSuffix);
			}		
			/* Check if this has an objectclass known to the UserGroup
			   editor. Get the object class to java class association. */
			if ( isStandardObjectClass( _objectClasses.elements(),
										info ) ) {				
				/* Yes, it is. So call the UserGroup editor. */	
				ResourceEditor ed = new DSResourceEditor(
					_model.getFrame(), info, _entry );	

				String buttonText =
					_resource.getString( "UserGroup",
										 "Advanced-label" );
				/* Install an "Advanced" button for generic editing */
				ed.registerAdvancedOption( new UserGroupAdapter(
					buttonText ) );
				_model.setWaitCursor( false );
				ed.showModal();
				ModalDialogUtil.disposeAndRaise(ed, _model.getFrame());
				/* It is possible the ResourceEditor changed the RDN */
				setDN( ed.getLDAPEntry().getDN() );
				String displayName = getDisplayNameFromDN(_model, getDN()); 
				setDisplayName( displayName ); 
				setName( displayName ); 
				/* It is possible that nsAccountLock changed. So let's refresh the entry */
				_entry = null;
				getEntry();
				return;
			}

			/* Got this far because there is no known customized editor
			   for this object. */
			_model.setWaitCursor( false );
			editGeneric( true, true );
			
			/* It is possible that nsAccountLock changed. So let's refresh the entry */
			_entry = null;
			getEntry();
			/* It is possible the state of the account changed */
			if ( !isRootDSE() ) {
				setIcon( checkIcon( _objectClasses, isLeafType() ) );
			}
		} catch ( Exception ex ) {
			Debug.println( "DSEntryObject.editProperties <" +
						   getDN() + "> " + ex );
			ex.printStackTrace();
		} finally {
			_model.setWaitCursor( false );
		}
	}

	/**
	 * Callback for the UserGroup editor, when the Advanced button
	 * is hit.
	 */
    class UserGroupAdapter implements IResEditorAdvancedOpt {
 	    UserGroupAdapter( String buttonText ) {
		    _buttonText = buttonText;
		}
	    public String getButtonText() { return _buttonText; }
		public boolean run( ConsoleInfo info,
							ResourcePageObservable observableentry ) {
			boolean status;
			LDAPEntry entry;
			DSEntryDialog dlg;
			if ( observableentry.isNewUser() ) {
				/* Preliminary DN - null from Kingpin */
				String dn = observableentry.getDN();
				Debug.println( "DSEntryObject.UserGroupAdapter.run: DN = " +
							   dn );
				/* Get all attributes set by the custom editor */
				LDAPAttributeSet set = new LDAPAttributeSet();
				Enumeration en = observableentry.getAttributesList();
				if ( en != null ) {
					while( en.hasMoreElements() ) {
						String attrName = (String)en.nextElement();
						Vector v = observableentry.get( attrName );
						String[] values = new String[v.size()];
						v.copyInto( values );
						set.add( new LDAPAttribute( attrName, values ) );
						v.removeAllElements();
					}
				}
				/* Get all required attributes for the object classes */
				String key = "cn";
				Vector v = observableentry.get( OBJECTCLASS_ATTR );
				LDAPSchema schema = _model.getSchema();
				en = v.elements();
				while( en.hasMoreElements() ) {
					String ocName = (String)en.nextElement();					
					Enumeration e = DSSchemaHelper.allRequiredAttributes(ocName, schema);
					while( e.hasMoreElements() ) {
						String name = (String)e.nextElement();
						if ( set.getAttribute( name ) == null ) {
							set.add( new LDAPAttribute( name, "" ) );
						}
					}
					if ( ocName.equalsIgnoreCase( "person" ) ) {
						key = "uid";
					} else if ( ocName.equalsIgnoreCase(
						"organizationalunit" ) ) {
						key = "ou";
					}
				}
				Debug.println( "DSEntryObject.UserGroupAdapter.run: " +
							   "key = " + key );
				if ( dn == null ) {
					/* Get rdn */
					String rdn = getFirstStringValue( set, key );
					if ( (rdn == null) || (rdn.length() < 1) ) {
						rdn = DEFAULT_NEW;
//						set.remove( key );
						Debug.println( "DSEntryObject.UserGroupAdapter." +
									   "run: Adding " + key + "=" + rdn );
//						set.add( new LDAPAttribute( key, rdn ) );
					}
					dn = key + "=" + rdn + ", " + getDN();
					Debug.println( "DSEntryObject.UserGroupAdapter.run: " +
								   "Setting dn = " + dn );
				}

				entry = new LDAPEntry( dn, set );
				DSEntryObject deo = nodeFromEntry( entry );
			    dlg = deo.editGeneric( true, false );
				entry = deo.getEntry();
			} else {
				dlg = editGeneric( true, false );
				entry = getEntry();
			}
			if ( dlg != null ) {
				/* Notify the observable from the kingpin editor */
				
				LDAPModificationSet mods = dlg.getChanges();
				if ( mods != null ) {
					
					Debug.println(9, "DSEntryObject.UserGroupAdapter.run: " +
								  "dialog mod set=" + mods.elementAt(0));
					updateObservable( observableentry, mods );
				}				
			}
			return (dlg != null);
		}
		private String getFirstStringValue( LDAPAttributeSet set,
											String name ) {
			LDAPAttribute attr = set.getAttribute( name );
			if ( attr != null ) {
				Enumeration en = attr.getStringValues();
				if ( (en != null) && en.hasMoreElements() ) {
					return ((String)en.nextElement()).trim();
				}
			}
			return null;
		}
		private void updateObservable(
			ResourcePageObservable observableentry,
			LDAPModificationSet mods ) {
			
			Vector v = new Vector();
			for( int i = 0; i < mods.size(); i++ ) {
				LDAPModification mod = mods.elementAt( i );
				LDAPAttribute attr = mod.getAttribute();
	
				Enumeration en = attr.getByteValues();
				if ( en == null ) {					
					en = attr.getStringValues();	
				}
				while( en.hasMoreElements() ) {
					v.addElement( en.nextElement() );
				}
				switch( mod.getOp() ) {
				case LDAPModification.ADD: {
					observableentry.add( attr.getName(), v );
					break;
				}
				case LDAPModification.REPLACE: {
					observableentry.replace( attr.getName(), v );
					break;
				}
				case LDAPModification.DELETE: {
					// Kingpin doesn't support this yet...
//					observableentry.delete( attr.getName(), v );
					// So try for strings...
					Enumeration vals = attr.getStringValues();
					if ( vals != null ) {		
						if (vals.hasMoreElements()) {
							observableentry.delete(
								attr.getName(),
								(String)vals.nextElement() );
						} else {
							observableentry.delete( attr.getName() );
						}											
						while( vals.hasMoreElements() ) {
							observableentry.delete(
								attr.getName(),
								(String)vals.nextElement() );
						}
					} else {
						// Not strings, so just delete all values
						observableentry.delete( attr.getName() );
					}
					break;
				}
				}
				v.removeAllElements();
			}
		}
		private String _buttonText = null;
	}

	/**
	 *	perform action on this node
	 */
	public void performAction(String command) {
		Debug.println( "DSEntryObject.performAction " + command );
	}

	/**
     * Override no-op action
	 *
	 */
	public boolean run(IPage viewInstance) {
		editProperties();
		return false;
	}

	/**
     * Notification that a run action (aka: execute, perform) needs to be
     * taken on this object.  Return value is now obsolete.
     * Called by: ResourceModel
	 *
	 */
	public boolean run(IPage viewInstance, IResourceObject selectionList[]) {
		/* ??? Need to do multiple simultaneous edit */
		editProperties();
		return false;
	}

	/**
	 * Called when this object is unselected.
     * Called by: ResourceModel
	 */
	public void unselect(IPage viewInstance) {
		if ( releaseOnUnselect ) {
			if ( _dataModel != null ) {
				_dataModel.reset();
				_dataModel = null;
			}
			if ( _customizePanel != null ) {
				_customizePanel.removeAllElements();
				_customizePanel = null;
			}
		}
	}

	/**
	 * Called when this object is selected.
     * Called by: ResourceModel
	 */
	public void select(IPage viewInstance) {
	}

   /**
	* Return the parent node
	* @return the parent DSEntryObject, if any
	*/
    public IDSEntryObject getParentNode() {
		return _parent;
	}

   /**
	* Set the parent node
	* @param o the parent DSEntryObject, if any
	*/
    public void setParentNode( IDSEntryObject o ) {
		_parent = o;
	}

	/**
	 * Set a parameter for future searches, which determines if the
	 * ManagedSAIT control is sent with each search. If referrals are
	 * disabled, the control is sent and you will receive the referring
	 * entry back.
	 *
	 * @param on true (the default) if referrals are to be followed
	 */
    public void setReferralsEnabled( boolean on ) {
		if ( _dataModel != null ) {
			Debug.println(9, "DSEntryObject.setReferralsEnabled: " +
						  "setting referrals on=" + on + " for " +
						  "model=" + _dataModel);
			_dataModel.setReferralsEnabled( on );
		} else {
			Debug.println(9, "DSEntryObject.setReferralsEnabled: " +
						  "_dataModel is null this=" + this +
						  " on=" + on);
		}
		_followReferrals = on;
	}

	/**
	 * Get the parameter which determines if the
	 * ManagedSAIT control is sent with each search.
	 *
	 * @return true if referrals are to be followed
	 */
    public boolean getReferralsEnabled() {
		Debug.println(9, "DSEntryObject.getReferralsEnabled: " +
					  "this=" + this +
					  " referrals=" +
					  _followReferrals);
		return _followReferrals;
	}

	/**
	 * Set a parameter for future searches, which determines if the
	 * ManagedSAIT control is sent with each search. If referrals are
	 * disabled, the control is sent and you will receive the referring
	 * entry back. The change is applied to the entire subtree, starting
	 * at this node.
	 *
	 * @param on true (the default) if referrals are to be followed
	 */
    public void propagateReferralsEnabled( boolean on ) {
		Debug.println(9, "DSEntryObject.propagateReferralsEnabled: " +
					  "referrals on=" + on);
		/* Propagate change down the tree */
		Enumeration en = breadthFirstEnumeration();
		/* This node is the first returned in the enumeration */
		while( en.hasMoreElements() ) {
			DSEntryObject deo = (DSEntryObject)en.nextElement();
			Debug.println(9, "DSEntryObject.propagateReferralsEnabled: " +
						  "setting referrals to " + on + " for " +
						  deo);
			deo.setReferralsEnabled( on );
		}
	}

    public String toString() {
		return "DSEntryObject for " + getDN();
	}

	/**
	 * Sort an entry alphabetically after attribute name
	 *
	 */
    private LDAPEntry sortByAttribute( LDAPEntry e ) {
		LDAPAttributeSet attrSet = e.getAttributeSet();
		int nAttrs = attrSet.size();
		LDAPAttribute[] attrs = new LDAPAttribute[nAttrs];
		for ( int i = 0; i < nAttrs; i++ ) {
			attrs[i] = attrSet.elementAt( i );
		}

		for ( int i = 0; i < nAttrs - 1; i++ ) {
			for ( int j = i + 1; j < nAttrs; j++ ) {
				if (attrs[i].getName().compareTo(attrs[j].getName()) > 0) {
					LDAPAttribute a = attrs[i];
					attrs[i] = attrs[j];
					attrs[j] = a;
				}
			}
		}
		LDAPAttributeSet set = new LDAPAttributeSet();
		for ( int i = 0; i < nAttrs; i++ ) {
			set.add( attrs[i] );
		}
		return new LDAPEntry( e.getDN(), set );
	}

	/**
	 * Sorts the vector of entries using bubble sort.
	 * @param v The vector being sorted.
	 */
    private void sortEntryVector( Vector v ) {

		int nChildren = v.size();
		if ( nChildren > 1 ) {
			Hashtable hash = new Hashtable();
			String[] str = new String[nChildren];
			Enumeration e = v.elements();
			int n = 0;
			while( e.hasMoreElements() ) {
				DSEntryObject deo = (DSEntryObject)e.nextElement();
				String key = deo.getDisplayName().toLowerCase();
				str[n] = key;
				n++;
				hash.put( key, deo );
			}
			DSUtil.trimAndBubbleSort( str, false );
			v.removeAllElements();
			for( n = 0; n < str.length; n++ )
				v.addElement( hash.get( str[n] ) );
		}
	}

    public boolean childExists(IDSEntryObject deo) {
        DSEntryObject ch;
        Enumeration e = children();
		while( e.hasMoreElements() ) {
			ch = (DSEntryObject)e.nextElement();
            if (ch.getDN().equals(deo.getDN()))
                return true;
        }
        return false;
    }

	static public DSEntryObject getBogusEntryObject() {
		DSEntryObject bogus = new DSEntryObject(true);
		bogus.setDN(BOGUS_LABEL);
		bogus.setDisplayName(BOGUS_LABEL);
		bogus.setIcon(BOGUS_ICON);
		return bogus;
	}

	static private boolean isRootDSE( String dn ) {
		return ( (dn == null) || dn.equals("") );
	}

	private boolean isRootDSE() {
		return isRootDSE( getDN() );
	}
   

	public void setSelectedPartitionView(String selectedPartitionView) {
		_selectedPartitionView = selectedPartitionView;
	}

	public String getSelectedPartitionView() {
		return _selectedPartitionView ;
	}

	/**
	 * If the user chose to see the state of the account, it says if the entry represented
	 * by the DSEntryObject is activated or not.
	 * @return true if the user didn't choose to see the state of the account.  The state of the account
	 * otherwise.
	 */
	private boolean isActivated() {
		/* If the user does not want to see the account state we don't do any calculation */
		if (_model instanceof DSContentModel) {
			if (!((DSContentModel)_model).isViewAccountInactivationSelected()) {
				return true;
			}
		}
		
		LDAPEntry entry = getEntry();
		if (entry == null) {
			return true;
		}
		AccountInactivation account = new AccountInactivation(entry);
	
		boolean state = true;
		try {
			state = !account.isLocked(getLDAPConnection());
		} catch (LDAPException e) {
			Debug.println("DSEntryObject.isActivated(): "+e);
		}
		return state;
	}

	/**
	 * Check if the entry represented by this DSEntryObject is a role
	 * @return true if the entry is a role
	 */
	private boolean isRole() {
		LDAPEntry entry = getEntry();
		if (entry == null) {
			return false;
		}
		LDAPAttribute objectclassAttribute = entry.getAttribute(OBJECTCLASS_ATTR);
		if (objectclassAttribute!=null) {
			Enumeration e = objectclassAttribute.getStringValues();
			String value;
			boolean isRoleDefinition=false;
			boolean isLDAPSubentry=false;
			while (e.hasMoreElements()) {
				value = (String)e.nextElement();
				if (value!=null) {
					if (value.equalsIgnoreCase("nsroledefinition")) {
						isRoleDefinition=true;
					} else if (value.equalsIgnoreCase("ldapsubentry")) {
						isLDAPSubentry = true;
					}
					if (isRoleDefinition && isLDAPSubentry) {
						return true;
					}
				}
			}			
		}
		return false;				
	}

		
	static ResourceSet _resource = DSUtil._resource;
    private IDSEntryObject _parent = null;	
    private String _selectedPartitionView = DSContentModel.ALL;
	// Default image name for tree nodes
	
	private String[] _suffixes;
	static private String _defaultImageName = initDefaultIconName();
	static private String _defaultFolderImageName =
	                                          initDefaultFolderIconName();

    private String _dn;                 // Full DN of this entry

	private String _sDisplayName;		// display name
	private boolean _fLoaded = false;	// flag to indicated whether the
	                                    // node is loaded or not
	private int _iChildren = -1;
	private boolean _fHasChildren = false;
	private boolean _fHasCheckedForChildren = false;
	private boolean _fHasGrandChildren = false;
	private boolean _fHasCheckedForGrandChildren = false;
    private boolean _fContainer = false; // True if it can have children
	private DSEntryList _customizePanel = null; // customize panel
	                                                    //of this node
	private long _objectCode = 0;
	// DONT USE VERBOSE!  SOME OF THE PRINTLN STATEMENTS HAVE BAD
	// SIDE EFFECTS WHICH WILL RENDER DEBUGGING FUTILE.  OF COURSE,
	// SOME KIND SOUL MAY FIX THEM AT SOME POINT . . .
	static final private boolean _verbose =
	             (System.getProperty("verbose") != null);

    static private Hashtable _cContainers = null;
	                                            // List of possible container
                                               	// object classes
	static private Hashtable _icons = new Hashtable();
	private Hashtable _objectClasses = null;
	private IDSModel _model = null;
	private LDAPEntry _entry = null;
	private VListEntryModel _dataModel = null;	// data model for the entries
	private static String _childFilter;
	private boolean _isPrivateSuffix = false;
	private boolean _isBogus = false; // true if this is a bogus entry e.g. bad referral
    /* This variable determines if the attribute names or the "friendly"
	   names are used as labels */
    private static boolean _showAliases = true;
    /* The node can be made to free its panel and other resource when
	   deselected. This reduces memory usage substantially, but decreases
	   performance when clicking around among node. */
	private static final boolean releaseOnUnselect = true; 

	private boolean _followReferrals = true;
	private static LDAPControl _manageDSAITControl =
			new LDAPControl( LDAPControl.MANAGEDSAIT, true, null );

    /* Set this to false to NOT show private suffixes */
	private boolean _showPrivateSuffixes = true;

	/* Operational attributes */
	private static final String SUBORDINATE_ATTR = "numsubordinates";
	private static final String NSROLEDN_ATTR = "nsroledn";
	private static final String NSROLE_ATTR = "nsrole";							   
	private static final String NSLOOKTROUGHLIMIT_ATTR = "nslookthroughlimit";
	private static final String NSSIZELIMIT_ATTR = "nssizelimit";
	private static final String NSTIMELIMIT_ATTR = "nstimelimit";
	private static final String NSIDLETIMEOUT_ATTR = "nsidletimeout";
	private static final String NSACCOUNTLOCK_ATTR = "nsaccountlock";
	private static final String ACI_ATTR = "aci";
	private static final String USERPASSWORD_ATTR = "userpassword";
	private static final String OBJECTCLASS_ATTR = "objectclass";
    private static final String NSROLEDEFINITION_ATTR = "nsroledefinition";				   
	private static final String INACTIVATED = "true";


	/* The list of attributes to query when building DSEntryObject */
	/* This list is also used by VListEntryModel */
    public static final String[] MINIMAL_ATTRS = {
		OBJECTCLASS_ATTR,
		SUBORDINATE_ATTR,
		NSACCOUNTLOCK_ATTR
	}; 							  

    private static final int MAX_UNINDEXED_COUNT = 1000;

	private static final String _section = "EntryObject";
	private static final String _serverIconName = "directory.gif";
	/* This lists, in order, the preferred attribute to use when composing the
	   rdn of a new entry. The first one present as a required or optional attribute
	   for the objectclasses of the entry is used. */
    private static String[] PREFIXES = { "cn", "uid", "ou", "dc", "o" };
	private static final String DEFAULT_NEW =
						  _resource.getString(_section, "defaultnew");
	private static final String BOGUS_ICON_NAME =
		_resource.getString(_section, "bogus-icon");
	private static final RemoteImage BOGUS_ICON =
		DSUtil.getPackageImage(BOGUS_ICON_NAME);
	private static final String BOGUS_LABEL =
		_resource.getString(_section, "bogus-label");

	public static final String SEARCH_OID = "2.16.840.1.113730.3.4.14";
}

