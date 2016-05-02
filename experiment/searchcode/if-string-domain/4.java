/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * The contents of this file are subject to the terms of the Common 
 * Development and Distribution License ("CDDL")(the "License"). You 
 * may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://open-dm-mi.dev.java.net/cddl.html
 * or open-dm-mi/bootstrap/legal/license.txt. See the License for the 
 * specific language governing permissions and limitations under the  
 * License.  
 *
 * When distributing the Covered Code, include this CDDL Header Notice 
 * in each file and include the License file at
 * open-dm-mi/bootstrap/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the 
 * fields enclosed by brackets [] replaced by your own identifying 
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 */
package com.sun.mdm.multidomain.services.model;

/**
 * Domain class.
 * @author cye
 */
public class Domain {
    private String name; 
    private String displayName;
    
    /**
     * Create an instance of domain.
     */
    public Domain() {        
    }
    
    /**
     * Create an instance of domain.
     * @param name Domain name.
     */
    public Domain(String name) {
        this.name = name;
        this.displayName = name;
    }
    
    /**
     * Get domain name.
     * @return String Domain name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set domain name,
     * @param name Domain name.
     */
    public void setName(String name) {        
        this.name = name;
    }     
    
    /**
     * Get domain display name.
     * @return String Domain display name.
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Set domain display name,
     * @param name Domain display name.
     */
    public void setDisplayName(String displayName) {        
        this.displayName = displayName;
    }     
    
}

