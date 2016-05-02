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

/**
 * Represent a non-alphanumeric and non-space symbol (>, ]...).
 * 
 * @version $Id: 0f4712ca808ac1a8c62ac8ccb172c5efb0801ad9 $
 * @since 1.5M2
 */
public class SpecialSymbolBlock extends AbstractBlock
{
    /**
     * The symbol.
     */
    private char symbol;

    /**
     * @param symbol the symbol
     */
    public SpecialSymbolBlock(char symbol)
    {
        this.symbol = symbol;
    }

    /**
     * @return the symbol
     */
    public char getSymbol()
    {
        return this.symbol;
    }

    @Override
    public void traverse(Listener listener)
    {
        listener.onSpecialSymbol(getSymbol());
    }

    /**
     * {@inheritDoc}
     * @since 1.8RC2
     */
    @Override
    public String toString()
    {
        return String.valueOf(getSymbol());
    }
}

