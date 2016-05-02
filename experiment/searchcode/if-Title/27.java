/*
 *  Copyright 2005 Blandware (http://www.blandware.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inwiss.platform.model.core;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.inwiss.platform.model.BaseObject;
import org.inwiss.platform.model.core.User;

/**
 * <p>
 * This class represents a group of users. It's convinient to quickly assign
 * a lot of roles to a user by placing that user to a group which has some
 * roles assigned.
 * </p>
 * <p><a href="Group.java.html"><i>View source</i></a></p>
 *
 * @author Roman Puchkovskiy <a href="mailto:roman.puchkovskiy@blandware.com">
 *         &lt;roman.puchkovskiy@blandware.com&gt;</a>
 * @version $Revision: 1.5 $ $Date: 2008/09/21 14:41:27 $
 * @see User
 * @see Role
 * @struts.form include-all="false" extends="BaseForm"
 * @hibernate.class table="al_core_group" lazy="false"
 * @hibernate.cache usage="read-write"
 */
@JsonIgnoreProperties({ "roles", "users" })
public class Group extends BaseObject implements Serializable {
    /**
     * Name of the group (used as a system identifier)
     */
    protected String name;
    /**
     * Human-readable name of the group
     */
    protected String title;
    /**
     * Description of the group; this may be quite long
     */
    protected String description;
    /**
     * Whether group is fixed. Fixed groups cannot be deleted
     */
	private Boolean fixed;
    /**
     * Version of this group (used in optimistic locking)
     */
    protected Long version;

    /**
     * List of roles which are assigned to this group
     */
    protected List roles = new ArrayList();

    /**
     * List of users which are members of this group
     */
    protected List users = new ArrayList();

    /**
     * Creates a group
     */
    public Group() {
    }

    /**
     * Creates a group with a given name
     *
     * @param name group name
     * @param title group title
     */
    public Group(String name, String title) {
        this.name = name;
        this.title = title;
    }

    //~ Methods ================================================================

    /**
     * Returns the name of the group (it's the analog of system identifier)
     *
     * @return group name
     * @struts.form-field
     * @struts.validator type="required"
     * @struts.validator type="mask" msgkey="core.cm.errors.identifier"
     * @struts.validator-args arg0resource="core.group.form.name"
     * @struts.validator-var name="mask" value="${identifier}"
     * @hibernate.id column="groupname" length="20"
     * generator-class="assigned" unsaved-value="null"
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns human-readable title of the group.
     *
     * @return group title
     * @struts.form-field
     * @struts.validator type="required"
     * @struts.validator-args arg0resource="core.group.form.title"
     * @hibernate.property
     * @hibernate.column name="title" not-null="true" unique="true" length="63"
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Returns the description of the group
     *
     * @return group description
     * @struts.form-field
     * @hibernate.property column="description" length="252"
     */
    public String getDescription() {
        return this.description;
    }

	/**
	 * Returns <code>Boolean.TRUE</code>, if group is fixed, i.e. it cannot be deleted
	 *
	 * @return whether group is fixed
	 * @hibernate.property column="fixed" not-null="true" type="true_false"
	 */
	public Boolean getFixed() {
		return fixed;
	}

    /**
     * Sets whether the group will be fixed (and no one will be able to delete it)
     *
     * @param fixed whether the group will be fixed
     */
	public void setFixed(Boolean fixed) {
		this.fixed = fixed;
	}

    /**
     * Sets name of the group
     *
     * @param name group name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets human-readable title of the group
     *
     * @param title group title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets description of the group
     *
     * @param description group description
     */
    public void setDescription(String description) {
        this.description = description;
    }

	/**
	 * Gets all group roles, represented as string (roles are separated with commas)
	 *
	 * @return roles separated with commas
	 */
	public String getRolesAsString() {
		StringBuffer roles = new StringBuffer();
		if ( this.roles != null && this.roles.size() > 0 ) {
			for ( Iterator i = this.roles.iterator(); i.hasNext(); ) {
				Role role = (Role) i.next();
				roles.append(role.getTitle());
				if ( i.hasNext() ) {
					roles.append(", ");
				}
			}
		}
		return roles.toString();
	}

    /**
     * Returns version of the group
     *
     * @return version
     * @struts.form-field
     * @hibernate.version column="version" type="long" unsaved-value="null"
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Sets version of the group
     *
     * @param version the version to set
     */
    public void setVersion(Long version) {
        this.version = version;
    }


    /**
     * Returns list of roles which are assigned to this group
     *
     * @return list of roles
     * @hibernate.bag table="al_core_group_role" cascade="none" lazy="true" inverse="false" outer-join="false"
     * @hibernate.collection-key
     * @hibernate.collection-key-column name="groupname" length="20"
     * @hibernate.collection-many-to-many class="com.blandware.atleap.model.core.Role"
     * column="rolename" outer-join="false"
     */
    public List getRoles() {
        return roles;
    }

    /**
     * Sets list of roles which assigned to this group
     *
     * @param roles list of roles
     */
    public void setRoles(List roles) {
        this.roles = roles;
    }

    /**
     * Returns list of users which are members of this group
     *
     * @return list of users
     * @hibernate.bag table="al_core_group_user" cascade="none" lazy="true" inverse="false" outer-join="false"
     * @hibernate.collection-key
     * @hibernate.collection-key-column name="groupname" length="20"
     * @hibernate.collection-many-to-many class="com.blandware.atleap.model.core.User"
     * column="username" outer-join="false"
     */
    public List getUsers() {
        return users;
    }

    /**
     * Sets list of users which are members of this group
     *
     * @param users list of users
     */
    public void setUsers(List users) {
        this.users = users;
    }

    /**
     * Adds a connection between this group and a given role
     *
     * @param role the role to be connected with
     */
    public void addRole(Role role) {
        if ( !role.getGroups().contains(this) ) {
            role.getGroups().add(this);
        }
        if ( !getRoles().contains(role) ) {
            getRoles().add(role);
        }
    }

    /**
     * Removes a connection between this group and a given role
     *
     * @param role the role
     */
    public void removeRole(Role role) {
        role.getGroups().remove(this);
        getRoles().remove(role);
    }

    /**
     * Adds a connection between this group and a given user
     *
     * @param user the user to be connected with
     */
    public void addUser(User user) {
        if ( !user.getGroups().contains(this) ) {
            user.getGroups().add(this);
        }
        if ( !getUsers().contains(user) ) {
            getUsers().add(user);
        }
    }

    /**
     * Removes a connection between this group and a given user
     *
     * @param user the user
     */
    public void removeUser(User user) {
        user.getGroups().remove(this);
        getUsers().remove(user);
    }

    public boolean equals(Object o) {
        if ( this == o ) {
            return true;
        }

        if ( !(o instanceof Group) ) {
            return false;
        }
        final Group group = (Group) o;

        if ( title != null ? !title.equals(group.title) : group.title != null ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        if ( title == null ) {
            return 0;
        } else {
            return title.hashCode();
        }
    }

}

