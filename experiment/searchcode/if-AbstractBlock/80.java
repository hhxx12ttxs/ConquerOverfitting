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
 * Represents the row of a table. Contains {@link TableCellBlock} objects.
 * 
 * @version $Id: f23123b26ccd13c5243864c62de4bf6c4d49c0f0 $
 * @since 1.6M2
 */
public class TableRowBlock extends AbstractBlock
{
    /**
     * @param list the list of children blocks of the table row block (generally a list of {@link TableCellBlock}).
     * @since 4.2M1
     */
    public TableRowBlock(List<Block> list)
    {
        super(list);
    }

    /**
     * @param list the list of children blocks of the table row block (generally a list of {@link TableCellBlock}).
     * @param parameters the parameters of the table row.
     */
    public TableRowBlock(List<Block> list, Map<String, String> parameters)
    {
        super(list, parameters);
    }

    @Override
    public void before(Listener listener)
    {
        listener.beginTableRow(getParameters());
    }

    @Override
    public void after(Listener listener)
    {
        listener.endTableRow(getParameters());
    }
}

