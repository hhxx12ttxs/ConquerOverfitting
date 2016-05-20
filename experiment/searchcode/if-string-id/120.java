/**
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.mobicents.ssf.context.signal;

import java.io.Serializable;

/**
 * SipApplicationSession?????SipSession??????????????? 
 * 
 * @author nisihara
 *
 */
public class SignalingState implements Serializable {
	
	private static final long serialVersionUID = 3436348749548432880L;

	public enum Type {
        SIP_SESSION,
        SIP_APPLICATION_SESSION
    }
    
    /**
     * ???
     */
    private String name;
    
    /**
     * ????SipSession????SipApplicationSession?ID
     */
    private String id;
    
    /**
     * type
     */
    private Type type;

    /**
     * ??????????
     * @return ???
     */
    public String getName() {
        return name;
    }

    /**
     * ??????????
     * @param name ???
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * ID???????
     * @return ID
     */
    public String getId() {
        return id;
    }

    /**
     * ID???????
     * @param id ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * type???????
     * @return type
     */
    public Type getType() {
        return type;
    }

    /**
     * type???????
     * @param type type
     */
    public void setType(Type type) {
        this.type = type;
    }
    
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append("id=" + this.id);
    	sb.append("type=" + this.type);
    	sb.append("name=" + this.name);
    	
    	return sb.toString();
    }
}

