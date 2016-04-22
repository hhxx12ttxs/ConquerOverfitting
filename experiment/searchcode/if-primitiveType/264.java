/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.application.type;

import java.util.ArrayList;
import java.util.List;
import org.jevis.api.JEVisConstants;

/**
 * @deprecated This class is an temporary solution and maybe not used in the
 * futures
 *
 * @author fs
 */
public class GUIConstants {

    public static DisplayType BASIC_TEXT = new DisplayType("Text", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType BASIC_NUMBER = new DisplayType("Number", JEVisConstants.PrimitiveType.DOUBLE);
    public static DisplayType NUMBER_WITH_UNIT = new DisplayType("Number with Unit", JEVisConstants.PrimitiveType.DOUBLE);
    public static DisplayType BASIC_FILER = new DisplayType("File Selector", JEVisConstants.PrimitiveType.FILE);
    public static DisplayType BASIC_BOOLEAN = new DisplayType("Check Box", JEVisConstants.PrimitiveType.BOOLEAN);
    public static DisplayType PASSWORD_PBKDF2 = new DisplayType("Password PBKDF2", JEVisConstants.PrimitiveType.PASSWORD_PBKDF2);
    public static DisplayType BASIC_TEXT_MULTI = new DisplayType("Text Area", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType BASIC_TEXT_DATE_FULL = new DisplayType("Date", JEVisConstants.PrimitiveType.STRING);
    public static DisplayType BASIC_NUMBER_LONG = new DisplayType("Number", JEVisConstants.PrimitiveType.LONG);

    public static List<DisplayType> ALL = new ArrayList<DisplayType>() {
        {
            add(BASIC_TEXT);
            add(BASIC_TEXT_MULTI);
            add(BASIC_NUMBER);
            add(NUMBER_WITH_UNIT);
            add(BASIC_FILER);
            add(BASIC_BOOLEAN);
            add(PASSWORD_PBKDF2);
            add(BASIC_TEXT_DATE_FULL);
            add(BASIC_NUMBER_LONG);
        }
    };

    /**
     *
     * @param primitiveType
     * @see JEVisConstants.PrimitiveType
     * @return
     */
    public static List<DisplayType> getALL(int primitiveType) {
        List<DisplayType> all = new ArrayList<DisplayType>();
        for (DisplayType id : GUIConstants.ALL) {
            if (id.getPrimitivType() == primitiveType) {
                all.add(id);
            }

        }
        return all;

    }

}

