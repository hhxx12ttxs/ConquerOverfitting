/* =================================================================
Copyright (C) 2009 ADV/web-engineering All rights reserved.

This file is part of Mozart.

Mozart is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Mozart is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

Mozart
http://www.mozartcms.ru
================================================================= */
package ru.adv.db.handler;

import ru.adv.util.ErrorCodeException;

import java.sql.SQLException;

/**
 * User: roma
 * Date: 23.11.2004
 * Time: 17:03:51
 * $Id: HandlerCreationException.java 1106 2009-06-03 07:32:17Z vic $
 */
public class HandlerCreationException extends HandlerException {

    public HandlerCreationException(String id, SQLException e) {
        super(HandlerException.DB_CANNOT_CREATE_HANDLER, e, id);
    }

    public HandlerCreationException(String id, ErrorCodeException e) {
        super("Cannot create handler", e);
        setAttr("database", id);
    }

    public HandlerCreationException(String id, String message) {
        super(HandlerException.DB_CANNOT_CREATE_HANDLER, message, id);
    }
}

