/*
*  InstallManager
*
* Copyright (c) 2000 - 2011 Samsung Electronics Co., Ltd. All rights reserved.
*
* Contact: 
* Wooyoung Cho <wooyoung1.cho@samsung.com>
* Shihyun Kim <shihyun.kim@samsung.com>
* Taeyoung Son <taeyoung2.son@samsung.com>
* Yongsung kim <yongsung1.kim@samsung.com>
* 
 * Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* Contributors:
* - S-Core Co., Ltd
*
*/

package org.tizen.installmanager.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.tizen.installmanager.pkg.lib.PackageManager;
import org.tizen.installmanager.pkg.model.Package;
import org.tizen.installmanager.pkg.model.PackageSet;

/**
 * This class support items for tree view. 
 * @author Taeyong Son <taeyong2.son@samsung.com>
 */
public class Item implements Comparable<Item> {
	private Package latestPackage;
	private Package installedPackage;
	private Item parent;
	private ArrayList<Item> children;
	private CheckState  checkState= CheckState.UNCHECKED;
	private InstallState installState = InstallState.INSTALL;

	/**
	 * Package item status enumeration.<br>
	 * INSTALL : This item need to install.<br>
	 * UPDATE : This item need to update.<br>
	 * UNINSTALL : This item need to uninstall.
	 */
	public enum InstallState {
		INSTALL,
		UPDATE,
		SNAPSHOT,
		UNINSTALL,
	}
	
	/**
	 * item check status enumeration.<br>
	 * CHECKED : this item's check state is checked.<br>
	 * GRAYED : this item's check state is grayed.<br>
	 * UNCHECKED : this item's check state is unchecked.
	 */
	public enum CheckState {
		CHECKED,
		GRAYED,
		UNCHECKED
	}

	/**
	 * Construct Item class.
	 * @param parent
	 * @param name is item name.
	 * @param latestVersion is the latest version of item.
	 * @param installedVersion is a version of installed item already. 
	 * @param size is a item size.
	 * @param state is a item state.
	 */
	public Item(Package latestPackage, Package installedPackage, InstallState state) {
		setLatestPackage(latestPackage);
		setInstalledPackage(installedPackage);
		setInstallState(state);
		
		this.children = new ArrayList<Item>();
	}

	/**
	 * Get display name of item.
	 * @return
	 */
	public String getDisplayName() {
		if (latestPackage != null) {
			return latestPackage.getLabel();				
		} else {
			return "";
		}
	}
	
	/**
	 * Get package name of item.
	 * @return
	 */
	public String getPackageName() {
		if (latestPackage != null) {
			return latestPackage.getPackageName();
		} else {
			return "";
		}
	}

	/**
	 * Get package item current version.
	 * @return
	 */
	public String getLatestVersion() {
		if (latestPackage != null) {
			return latestPackage.getVersion().toString();
		} else {
			return "";
		}
	}

	/**
	 * Get package item installed version
	 * @return
	 */
	public String getInstalledVersion() {
		if (installedPackage != null) {
			return installedPackage.getVersion().toString();
		} else {
			return "";
		}
	}
	
	/**
	 * Get total checked items size 
	 * @return Checked items size
	 */
	public Long getSize() {
		PackageManager pm = PackageManager.getInstance();
		
		if (pm == null) {
			return 0l;
		} else {
			long size = 0l;
			PackageSet checkedChildPackages = getCheckedPackages();
			
			if (installState == InstallState.UNINSTALL) {
				size = pm.getTotalSizeOfRemovablePackages(checkedChildPackages);
			} else {
				size = pm.getTotalSizeOfInstallablePackages(checkedChildPackages);
			}
			
			return size;
		}
	}
	
	private ArrayList<Item> getCheckedItems() {		
		ArrayList<Item> checkedItems = getCheckedChildItems();
		
		if (this.getCheckState() == CheckState.CHECKED) {
			checkedItems.add(this);
		}
		
		return checkedItems;
	}
	
	private ArrayList<Item> getCheckedChildItems() {
		ArrayList<Item> checkedItems = new ArrayList<Item>();
		
		ArrayList<Item> childItems = getChildren();
		for (Item childItem : childItems) {
			if (childItem.getCheckState() == CheckState.CHECKED) {
				checkedItems.add(childItem);
			} else if (childItem.getCheckState() == CheckState.GRAYED) {
				checkedItems.addAll(childItem.getCheckedChildItems());
			}
		}
		
		return checkedItems;
	}
	
	private PackageSet getCheckedPackages() {
		PackageSet checkedPackages = new PackageSet();
		
		ArrayList<Item> checkedItems = getCheckedItems();
		
		for (Item checkedItem : checkedItems) {
			if (installState == InstallState.UNINSTALL) {
				checkedPackages.add(checkedItem.getInstalledPackage());
			} else {
				checkedPackages.add(checkedItem.getLatestPackage());
			}
		}
		
		return checkedPackages;
	}
	
	/**
	 * Get package item order
	 * @return item order
	 */
	public int getPackageOrder() {
		if (latestPackage != null) {
			return latestPackage.getOrder();
		} else {
			return 0;
		}
	}

	/**
	 * add child item on current item.
	 */
	public void addChildItem(Item item) {
		this.children.add(item);
	}
	
	/**
	 * Clear children items on current item.
	 */
	public void clearChildrenItems() {
		this.children.clear();
	}
	
	/**
	 * Get dependent package item on current item. 
	 * @return
	 */
	public ArrayList<Item> getChildren() {
		return children;
	}

	/**
	 * Check package is checked.
	 * @return true is checked, false is unchecked.
	 */
	public CheckState getCheckState() {
		return checkState;
	}

	/**
	 * Check package has child package.
	 * @return If package has children, return true. if not, return false.
	 */
	public boolean hasChildren() {
		return !getChildren().isEmpty();
	}
	
	/**
	 * Get parent package.
	 * @return parent package.
	 */
	public Item getParent() {		
		return parent;
	}
	
	/**
	 * Get latest package.
	 * @return
	 */
	public Package getLatestPackage() {
		return latestPackage;
	}
	
	/**
	 * Get installed package.
	 * @return
	 */
	public Package getInstalledPackage() {
		return latestPackage;
	}
	
	/**
	 * Get package that depends on the state.
	 * @return
	 */
	public Package getPackage() {
		if (this.installState == InstallState.UNINSTALL) {
			return installedPackage;
		} else {
			return latestPackage;
		}
	}
	
	/**
	 * Set latest package for tree item.
	 * @param pkg
	 */
	public void setLatestPackage(Package pkg) {
		this.latestPackage = pkg;
	}
	
	/**
	 * Set installed package for tree item.
	 * @param pkg
	 */
	public void setInstalledPackage(Package pkg) {
		this.installedPackage = pkg;
	}

	/**
	 * Set parent package.
	 * @param parent
	 */
	public void setParent(Item parentItem) {
		parent = parentItem;
	}

	/**
	 * Set package's checked flag.
	 * @param CHECKED is true, this package is selected by tree view.
	 */
	public void setCheckState(CheckState state) {
		this.checkState = state;
	}
	
	public void setCheckState(boolean state) {
		if (state) {
			this.checkState = CheckState.CHECKED;
		} else {
			this.checkState = CheckState.UNCHECKED;
		}
	}

	/**
	 * Set item's state.
	 * @param state
	 * @see InstallState
	 */
	public void setInstallState(InstallState state) {
		if (state == InstallState.UPDATE) {
			this.checkState = CheckState.CHECKED;
		} else if (state == InstallState.UNINSTALL) {
			this.checkState = CheckState.UNCHECKED;
		}
		
		this.installState = state;
	}

	/**
	 * Get item's state
	 * @return state
	 * @see InstallState
	 */
	public InstallState getInstallState() {
		return installState;
	}
	
	/**
	 * Get item's display order
	 * @return display order
	 */
	public int getOrder() {
		return latestPackage.getOrder();
	}
	
	/**
	 * Set state by children's state
	 */
	public void setStateByChildren() {
		setCheckStateByChildren(this);
		setInstallStateByChildren(this);
	}
	
	private void setCheckStateByChildren(Item parentItem) {
		if (!parentItem.hasChildren()) {
			return;
		} else {
			for (Item item : parentItem.getChildren()) {
				setCheckStateByChildren(item);
			}
			
			if (parentItem.isChildrenAllChecked()) {
				parentItem.setCheckState(CheckState.CHECKED);
			} else if (parentItem.isChildrenAllUnchecked()) {
				parentItem.setCheckState(CheckState.UNCHECKED);
			} else {
				parentItem.setCheckState(CheckState.GRAYED);
			}
		}
	}
	
	private void setInstallStateByChildren(Item parentItem) {
		if (!parentItem.hasChildren()) {
			return;
		} else {
			for (Item item : parentItem.getChildren()) {
				setInstallStateByChildren(item);
			}
			
			if (parentItem.isChildrenAllUpdate()) {
				parentItem.setInstallState(InstallState.UPDATE);
			} else if (parentItem.isChildrenAllUninstall()) {
				parentItem.setInstallState(InstallState.UNINSTALL);
			} else {
				parentItem.setInstallState(InstallState.INSTALL);
			}
		}
	}
	
	
	/**
	 * check children's checkbox state.
	 * @return if true, children's checkbox state is all checked.
	 */
	public boolean isChildrenAllChecked() {
		for (Item item : getChildren()) {
			if (item.getCheckState() != CheckState.CHECKED) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * check children's checkbox state.
	 * @return if true, children's checkbox state is all unchecked.
	 */
	public boolean isChildrenAllUnchecked() {
		for (Item item : getChildren()) {
			if (item.getCheckState() != CheckState.UNCHECKED) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isChildrenAllUpdate() {
		for (Item item : getChildren()) {
			if (item.getInstallState() != InstallState.UPDATE) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isChildrenAllUninstall() {
		for (Item item : getChildren()) {
			if (item.getInstallState() != InstallState.UNINSTALL) {
				return false;
			}
		}
		return true;
	}
	
	public String getStateToString() {
		if (hasChildren() || getParent() == null) {
			return "";
		} else {
			switch(installState) {
			case INSTALL:
				return "Not installed";
			case UPDATE:
				return "Updatable";
			case UNINSTALL:
				return "Installed";
			default:
				return "";
			}
		}
	}

	/**
	 * If parent item is checked, child items should be checked. 
	 * @param state is true, every child items' checked flag is true. false is the opposite. 
	 */
	public void setSubItemChecked(boolean state) {
		setCheckState(state);

		if (hasChildren()) {
			for (int i = 0; i < getChildren().size(); i++) {
				((Item) getChildren().get(i)).setSubItemChecked(state);
			}
		}
	}

	/**
	 * Check the existing children and checked flag, add to installed package list.
	 * @param installList is packages list which is installed already.
	 * @param checked is true, checking in box on tree view. false is the opposite.
	 */
	public void getTerminalPackages(List<String> installList, boolean checked) {
		if (hasChildren()) {
			for (Item childItem : getChildren()) {
				childItem.getTerminalPackages(installList, checked);
			}
		} 
		
		if (getChildren().size() <= 0) {
			if (getCheckState() == CheckState.CHECKED) {
				installList.add(getPackageName());
			}
		}
	}

	/**
	 * Get child items list.
	 * @return child items list.
	 */
	public List<Item> getDescendants() {
		List<Item> descendants = new ArrayList<Item>();
		descendants.add(this);
		if (hasChildren()) {
			for (int i = 0; i < getChildren().size(); i++) {
				descendants.addAll(((Item) getChildren().get(i)).getDescendants());
			}
		}
		return descendants;
	}
	
	public boolean hasGrayedChild(List<Item> grayCheckedItems) {
		for (int i = 0; i < grayCheckedItems.size(); i++) {
			for (int j = 0; j < getChildren().size(); j++) {
				if (grayCheckedItems.get(i) == getChildren().get(j))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * check children's checkbox state.
	 * @return if true, child item's checkbox state is grayed.
	 */
	public boolean hasGrayedChild() {
		for (Item item : getChildren()) {
			if (item.getCheckState() == CheckState.GRAYED) {
				return true;
			}
		}
		return false;
	}

	public void checkSuperItemGrayed(
			List<Item> grayCheckedItems, List<Item> checkedItems, List<Item> unCheckedItems) {
		if (hasChildren()) {
			boolean isAllChecked = true;
			boolean isAllUnchecked = true;
			for (int i = 0; i < getChildren().size(); i++) {
				if (((Item) getChildren().get(i)).getCheckState() == CheckState.UNCHECKED)
					isAllChecked = false;
				else
					isAllUnchecked = false;
			}
			if (isAllChecked == false && isAllUnchecked == false || hasGrayedChild(grayCheckedItems) == true) {
				grayCheckedItems.add(this);
//				setChecked(true);
			} else if (isAllChecked == true && isAllUnchecked == false && hasGrayedChild(grayCheckedItems) == false) {
				checkedItems.add(this);
				setCheckState(CheckState.CHECKED);
			} else if (isAllChecked == false && isAllUnchecked == true && hasGrayedChild(grayCheckedItems) == false) {
				unCheckedItems.add(this);
				setCheckState(CheckState.UNCHECKED);
			}
		}
		if (getParent() != null) {
			getParent().checkSuperItemGrayed(grayCheckedItems, checkedItems, unCheckedItems);
		}
	}
	
	public String toString() {
		return getPackageName();
	}

	@Override
	public int compareTo(Item item) {
		int order = item.getOrder();
		if (order > getOrder()) {
			return -1;
		} else if (order == getOrder()) {
			return 0;
		} else if (order < getOrder()) {
			return 1;
		}
		return 0;
	}
}

