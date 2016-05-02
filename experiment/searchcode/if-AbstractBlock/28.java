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
 * Represents a new line or line break (it's up to the Renderers to decide if it should be outputted as a new
 * line or as a line break in the given syntax).
 *
 * @version $Id: 41eb17e67b43987166cfe02b489db4f287122fa6 $
 * @since 1.5M2
 */
public final class NewLineBlock extends AbstractBlock
{
    /**
     * A new line block. Note that we don't make NewLineBlock a singleton since that would cause problems when using
     * Block APIs to manipulate a tree of blocks (for example to find the position of a new line block in a list using
     * {@link java.util.List#indexOf(Object)} wich would always return the first new line block).
     * @since 2.6RC1
     */
    public NewLineBlock()
    {
        // We need to keep this constructor to override the ones in AbstractBlock
    }

    @Override
    public void traverse(Listener listener)
    {
        listener.onNewLine();
    }
}

