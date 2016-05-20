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
 * <p>
 * SipApplicationSession?????SipSession???????????????
 * </p>
 * 
 * @author nisihara
 * 
 */
public class SignalingName implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 573590995007463738L;

    public enum Type {
        SIP_SESSION, SIP_APPLICATION_SESSION
    }

    /**
     * ID
     */
    private String id;

    /**
     * ??
     */
    private String name;

    /**
     * ???
     */
    private Type type;

    /**
     * ID???????
     * 
     * @return ID
     */
    public String getId() {
        return id;
    }

    /**
     * ID???????
     * 
     * @param id
     *            ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * ?????????
     * 
     * @return ??
     */
    public String getName() {
        return name;
    }

    /**
     * ?????????
     * 
     * @param name
     *            ??
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * ??????????
     * 
     * @return ???
     */
    public Type getType() {
        return type;
    }

    /**
     * ??????????
     * 
     * @param type
     *            ???
     */
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SignalingName:");
        sb.append("[id=" + id + "]");
        sb.append("[name=" + name + "]");
        sb.append("[type=" + type + "]");
        return sb.toString();
    }
}

