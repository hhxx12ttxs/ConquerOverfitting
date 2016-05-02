/*

    Copyright (c) 2004 by Robert J Colquhoun, All Rights Reserved

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/

package org.maverickdbms.basic.text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.maverickdbms.basic.mvConstants;
import org.maverickdbms.basic.mvConstantString;
import org.maverickdbms.basic.mvException;
import org.maverickdbms.basic.Factory;
import org.maverickdbms.basic.mvString;

/**
* This is the basic string interface for the package.
*/
class PFormatter extends Formatter {

    private mvConstantString pattern;

    public PFormatter(Factory factory, int type, Object[] patterns, boolean convNull) {
        super(factory, type, patterns, convNull);
        String p = (String)patterns[1];
        int plen = p.length();
        boolean bracket = false;
        int pos = 0;
        char[] buff = new char[plen + 2];
        for (int i = 0; i < plen; i++) {
            char c = p.charAt(i);
            switch (c) {
                case '(':
                    if (pos > 0 && buff[pos - 1] == '\'') {
                        pos--;
                    } else {
                        buff[pos++] = '\'';
                    }
                    bracket = true;
                    break;
                case ')':
                    if (pos > 0 && buff[pos - 1] == '\'') {
                        pos--;
                    } else {
                        buff[pos++] = '\'';
                    }
                    bracket = false;
                    break;
                case ';':
                case '/':
                    if (!bracket) {
                        buff[pos++] = mvConstants.VM;
                    }
                    break;
                default:
                    if (Character.isDigit(c)) {
                        int index = i + 1;
                        while (index < plen && Character.isDigit(p.charAt(index))) {
                            index++;
                        }
                        if (index < plen) {
                            switch(p.charAt(index)) {
                                case 'A':
                                case 'N':
                                case 'X':
                                    if (pos > 0 && buff[pos - 1] == '\'') {
                                        pos--;
                                    } else {
                                        buff[pos++] = '\'';
                                    }
                                    if (pos + index - i + 3 >= buff.length) {
                                        char[] old = buff;
                                        buff = new char[old.length + (old.length >> 1)];
                                        System.arraycopy(old, 0, buff, 0, old.length);
                                    }
                                    while (i < index) {
                                        buff[pos++] = p.charAt(i++);
                                    }
                                    buff[pos++] = p.charAt(i);
                                    if (pos > 0 && buff[pos - 1] == '\'') {
                                        pos--;
                                    } else {
                                        buff[pos++] = '\'';
                                    }
                                    break;
                                default:
                                    buff[pos++] = c;
                                    break;
                            }
                        }
                    } else {
                        buff[pos++] = c;
                    }
                    if (pos >= buff.length) {
                        char[] old = buff;
                        buff = new char[old.length + (old.length >> 1)];
                        System.arraycopy(old, 0, buff, 0, old.length);
                    }
            }
        }
        this.pattern = factory.getConstant(new String(buff, 0, pos));
    }

    public mvString format(mvString result, mvString status, mvConstantString input) { 
        if (input.MATCH(pattern).equals(mvConstantString.ONE)) {
            result.set(input);
        } else {
            result.clear();
        }
        return result;
    }
}


