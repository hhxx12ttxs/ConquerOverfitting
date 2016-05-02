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

import java.util.List;

import org.xwiki.rendering.listener.Listener;

/**
 * Represents a quotation line. There can be several quotation lines in a quotation block.
 * 
 * @version $Id: c369eb7698585534803b7b60429943b946e40e9e $
 * @since 1.6M2
 */
public class QuotationLineBlock extends AbstractBlock
{
    /**
     * @param blocks the children of the quotation line
     */
    public QuotationLineBlock(List<Block> blocks)
    {
        super(blocks);
    }

    @Override
    public void before(Listener listener)
    {
        listener.beginQuotationLine();
    }

    @Override
    public void after(Listener listener)
    {
        listener.endQuotationLine();
    }
}

