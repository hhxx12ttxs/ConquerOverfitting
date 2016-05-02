/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rendering.block;

import java.util.Map;

import org.xwiki.rendering.listener.Listener;

/**
 * Represents a Horizontal line.
 * 
 * @version $Id: 77e93241c8c6b6cf37dda1ba5f3c918820449075 $
 * @since 1.6M1
 */
public class HorizontalLineBlock extends AbstractBlock
{
    /**
     * Construct a Horizontal Line Block with no parameters.
     */
    public HorizontalLineBlock()
    {
        super();
    }

    /**
     * Construct a Horizontal Line Block with parameters.
     *
     * @param parameters see {@link org.xwiki.rendering.block.AbstractBlock#getParameter(String)} for more details on
     *        parameters
     */
    public HorizontalLineBlock(Map<String, String> parameters)
    {
        super(parameters);
    }

    @Override
    public void traverse(Listener listener)
    {
        listener.onHorizontalLine(getParameters());
    }
}

