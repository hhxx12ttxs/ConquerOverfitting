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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.Listener;

/**
 * Represents a text formatting block (bold, italic, etc).
 * 
 * @version $Id: cedd3c5d8e0bd09f4b0a20012d7202fca0a570e8 $
 * @since 1.6M1
 */
public class FormatBlock extends AbstractBlock
{
    /**
     * The formatting to apply to the children blocks.
     */
    private Format format;

    /**
     * @param childrenBlocks the nested children blocks
     * @param format the formatting to apply to the children blocks
     */
    public FormatBlock(List<Block> childrenBlocks, Format format)
    {
        this(childrenBlocks, format, Collections.<String, String> emptyMap());
    }

    /**
     * @param childrenBlocks the nested children blocks
     * @param format the formatting to apply to the children blocks
     * @param parameters the custom parameters
     */
    public FormatBlock(List<Block> childrenBlocks, Format format, Map<String, String> parameters)
    {
        super(childrenBlocks, parameters);
        this.format = format;
    }

    /**
     * @return the formatting to apply to the children blocks
     */
    public Format getFormat()
    {
        return this.format;
    }

    @Override
    public void before(Listener listener)
    {
        listener.beginFormat(getFormat(), getParameters());
    }

    @Override
    public void after(Listener listener)
    {
        listener.endFormat(getFormat(), getParameters());
    }
}

