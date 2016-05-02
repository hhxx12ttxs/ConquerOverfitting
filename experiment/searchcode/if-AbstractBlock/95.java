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
 * Represents a table.
 * 
 * @version $Id: adc96aff62b77989a3e31e790d05579e19e22225 $
 * @since 1.6M2
 */
public class TableBlock extends AbstractBlock
{
    /**
     * @param list the list of children blocks of the table block (generally a list of {@link TableRowBlock}).
     * @since 4.2M1
     */
    public TableBlock(List<Block> list)
    {
        super(list);
    }

    /**
     * @param list the list of children blocks of the table block (generally a list of {@link TableRowBlock}).
     * @param parameters the parameters of the table.
     */
    public TableBlock(List<Block> list, Map<String, String> parameters)
    {
        super(list, parameters);
    }

    @Override
    public void before(Listener listener)
    {
        listener.beginTable(getParameters());
    }

    @Override
    public void after(Listener listener)
    {
        listener.endTable(getParameters());
    }
}

