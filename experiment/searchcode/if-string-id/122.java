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

package org.mobicents.ssf.flow.config;

import java.util.Set;

import org.mobicents.ssf.util.AssertUtils;


public class FlowLocation {
    private String id;
    
    private String path;
    
    private Set<FlowElementAttribute> attributes;
    
    public FlowLocation(String id, String path, Set<FlowElementAttribute> attributes) {
        //Assert.hasText(id, "The id is required.");
        AssertUtils.hasText(path, "The path is required.");
        
        this.id = id;
        this.path = path;
        this.attributes = attributes;
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public Set<FlowElementAttribute> getAttributes() {
        return attributes;
    }
}

