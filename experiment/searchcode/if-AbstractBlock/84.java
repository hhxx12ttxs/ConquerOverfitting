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

import org.xwiki.rendering.listener.Listener;

import java.util.List;

/**
 * Represents a definition description. For exampe in HTML this is the equivalent of &lt;dt&gt;.
 *
 * @version $Id: 50a74dc95899602b4b134639375476df2725336a $
 * @since 1.6M2
 */
public class DefinitionTermBlock extends AbstractBlock
{
    /**
     * Construct a Definition Term block.
     *
     * @param childrenBlocks the blocks making the Definition Term
     */
    public DefinitionTermBlock(List<Block> childrenBlocks)
    {
        super(childrenBlocks);
    }

    @Override
    public void before(Listener listener)
    {
        listener.beginDefinitionTerm();
    }

    @Override
    public void after(Listener listener)
    {
        listener.endDefinitionTerm();
    }
}

