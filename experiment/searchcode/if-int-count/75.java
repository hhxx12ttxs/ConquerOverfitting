/*    
Craftbook 
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook.access;

public final class Item {

    public final int id, count;

    public Item(int id, int count) {

        this.id = id;
        this.count = count;
    }

    public Item addItems(int count) {

        return new Item(id, this.count + count);
    }

    public Item removeItems(int count) {

        return new Item(id, this.count - count);
    }
}

