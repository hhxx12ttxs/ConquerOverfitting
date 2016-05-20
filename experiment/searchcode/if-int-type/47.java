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
// -*- java -*-
// File: QuestionEvent.java
//
// Created: Thu Dec  6 17:44:05 2001
//
// $Id: QuestionEvent.java 1106 2009-06-03 07:32:17Z vic $
// $Name:  $
//

package ru.adv.event;

import java.io.Serializable;

/**
 * ?????????, ???????????? ???????, ???????????? ????????? {@link ru.adv.event.QuestionListener}.
 *
 * @version $Revision: 1.2 $
 */
public class QuestionEvent implements Serializable {

    private static final long serialVersionUID = 0xe34785e101e3c36dL;
    public static final int BOOL_QUESTION = 0;
    public static final int STRING_QUESTION = 1;
    public static final int LOG = 2;
    public static final int LOG_ACTION = 3;
    public static final int ERROR = 4;
    public static final int INIT_PROGRESS = 5;
    public static final int CURR_PROGRESS = 6;
    public static final int PROGRESS_DONE = 7;

    private int type;
    private String message;
    private String userData = null;
    private long longValue;

    /**
     * ???????????
     *
     * @param type        ??? ??????
     * @param msg         ????????
     * @param userMessage ?????????????? ?????? ?????????
     */
    public QuestionEvent(int type, String msg, String userMessage, long longValue) {
        this.type = type;
        this.message = msg;
        this.userData = userMessage;
        this.longValue = longValue;
    }

    /**
     * ???????????
     *
     * @param type ??? ??????
     * @param msg  ????????
     */
    public QuestionEvent(int type, String msg) {
        this(type, msg, null, 0);
    }

    public QuestionEvent(int type, String msg, long longValue) {
        this(type, msg, null, longValue);
    }

    /**
     * ??????????? ??????? ???? {@link #BOOL_QUESTION}
     */
    public QuestionEvent(String msg) {
        this(msg, null);
    }

    /**
     * ??????????? ??????? ???? {@link #BOOL_QUESTION}
     */
    public QuestionEvent(String msg, String userData) {
        this(BOOL_QUESTION, msg, userData, 0);
    }

    /**
     * ?????? ??????? ???????? ???????, ?? ??????? ????????? ????????
     *
     * @see ru.adv.db.create.DBCreateHandler
     */
    public boolean isQuestion() {
        return type == BOOL_QUESTION || type == STRING_QUESTION;
    }

    public boolean isBoolQuestion() {
        return type == BOOL_QUESTION;
    }

    public boolean isStringQuestion() {
        return type == STRING_QUESTION;
    }

    public boolean isLogAction() {
        return type == LOG_ACTION;
    }

    public boolean isError() {
        return type == ERROR;
    }

    public boolean isInitProgress() {
        return type == INIT_PROGRESS;
    }

    public boolean isCurrentProgress() {
        return type == CURR_PROGRESS;
    }

    public boolean isProgressDone() {
        return type == PROGRESS_DONE;
    }

    public long getLongValue() {
        return longValue;
    }

    /**
     *
     */
    public String getMessage() {
        return message;
    }

    /**
     * ??????? ????????????? ?????? ?????????
     *
     * @return null ???? ?? ??????????? ?????????????? ??????
     */
    public String getUserMessage() {
        return userData;
    }

    public String toString() {
        return typeToString(type) + " : " + message;
    }

    private String typeToString(int type) {
        String result = "UNKNOWN";
        switch (type) {
            case BOOL_QUESTION:
                result = "BOOL_QUESTION";
                break;
            case STRING_QUESTION:
                result = "STRING_QUESTION";
                break;
            case LOG:
                result = "LOG";
                break;
            case LOG_ACTION:
                result = "LOG_ACTION";
                break;
            case ERROR:
                result = "ERROR";
                break;
            case INIT_PROGRESS:
                result = "INIT_PROGRESS";
                break;
            case CURR_PROGRESS:
                result = "CURR_PROGRESS";
                break;
            case PROGRESS_DONE:
                result = "PROGRESS_DONE";
                break;
        }
        return result;
    }

}

