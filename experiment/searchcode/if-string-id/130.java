/* Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS.
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.google.ie.business.domain;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
@Searchable(alias = "User")
public class User implements Serializable {

    /** A unique identifier for the class */
    private static final long serialVersionUID = -1507109590723476666L;
    /** Constant for rolename user */
    public static final String ROLE_USER = "user";
    /** Constant for rolename admin */
    public static final String ROLE_ADMIN = "admin";

    /** Constant for rolename admin */
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_BANNED = "banned";

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
    @SearchableId
    private String userKey;

    @Persistent
    private long reputationPoints;
    @Persistent
    private String roleName;
    @Persistent
    private String id;
    @Persistent
    private Set<String> ideaKeys;
    @Persistent
    private Date createdOn;
    @Persistent
    private String thumbnailUrl;
    @Persistent
    @SearchableProperty
    private String displayName;

    @Persistent
    private String emailId;

    @Persistent
    private String status;

    /**
     * @return the emailId
     */
    public String getEmailId() {
        return emailId;
    }

    /**
     * @param emailId the emailId to set
     */
    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public User() {
        this.status = STATUS_ACTIVE;

    }

    /**
     * @return the ideaKeys
     */
    public Set<String> getIdeaKeys() {
        if (ideaKeys == null)
            ideaKeys = new HashSet<String>();
        return ideaKeys;
    }

    /**
     * @param ideaKeys the ideaKeys to set
     */
    public void setIdeaKeys(Set<String> ideaKeys) {
        this.ideaKeys = ideaKeys;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return id;
    }

    /**
     * @return the key
     */
    public String getUserKey() {
        return userKey;
    }

    /**
     * @return the reputationPoints
     */
    public long getReputationPoints() {
        return reputationPoints;
    }

    /**
     * @return the roleName
     */
    public String getRoleName() {
        return roleName;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param key the key to set
     */
    public void setUserKey(String key) {
        this.userKey = key;
    }

    /**
     * @param reputationPoints the reputationPoints to set
     */
    public void setReputationPoints(long reputationPoints) {
        this.reputationPoints = reputationPoints;
    }

    /**
     * @param roleName the roleName to set
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * @param userIdeaKeys the userIdeaKeys to set
     */
    public void setUserIdeaKeys(Set<String> userIdeaKeys) {
        this.ideaKeys = userIdeaKeys;
    }

    /**
     * @return the userIdeaKeys
     */
    public Set<String> getUserIdeaKeys() {
        return ideaKeys;
    }

    /**
     * @param createdOn the createdOn to set
     */
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * @return the createdOn
     */
    public Date getCreatedOn() {
        return createdOn;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "User [createdOn=" + createdOn + ", displayName=" + displayName + ", emailId="
                        + emailId + ", id=" + id + ", ideaKeys=" + ideaKeys + ", reputationPoints="
                        + reputationPoints + ", roleName=" + roleName + ", status=" + status
                        + ", thumbnailUrl=" + thumbnailUrl + ", userKey=" + userKey + "]";
    }

}


