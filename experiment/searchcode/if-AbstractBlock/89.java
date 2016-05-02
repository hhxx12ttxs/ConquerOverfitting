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
import java.util.Map;

import org.xwiki.rendering.listener.Listener;

/**
 * Represents a cell of a table.
 * 
 * @version $Id: 81e638ca5fb5d4a2647910555ac1cc1c02ea1c83 $
 * @since 1.6M2
 */
public class TableCellBlock extends AbstractBlock
{
    /**
     * @param list the list of children blocks of the table head cell block.
     * @since 4.2M1
     */
    public TableCellBlock(List<Block> list)
    {
        super(list);
    }

    /**
     * @param list the list of children blocks of the table head cell block.
     * @param parameters the parameters of the table row.
     */
    public TableCellBlock(List<Block> list, Map<String, String> parameters)
    {
        super(list, parameters);
    }

    @Override
    public void before(Listener listener)
    {
        listener.beginTableCell(getParameters());
    }

    @Override
    public void after(Listener listener)
    {
        listener.endTableCell(getParameters());
    }
}

