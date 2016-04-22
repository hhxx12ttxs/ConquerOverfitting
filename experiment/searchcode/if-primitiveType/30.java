/**
 *all rights reserved,@copyright 2003
 */
package com.cots.mvc.model.ws;

import com.cots.bean.PrimitiveType;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.XMLType;


/**
 * Description:
 * <p/>
 * User: chuguanghua
 * Date: 2005-2-2
 * Time: 8:51:27
 * Version: 1.0
 */
public class WSTypeMapping {
    public static QName getQName(String type){
        if(PrimitiveType.BYTE.equals(type)){
            return XMLType.XSD_BYTE;
        }else if(PrimitiveType.INT.equals(type)){
            return XMLType.XSD_INT;
        }else if(PrimitiveType.INTEGER.equals(type)){
            return XMLType.XSD_INTEGER;
        }else if(PrimitiveType.CHAR.equals(type)){
            return XMLType.XSD_STRING;
        }else if(PrimitiveType.STRING.equals(type)){
            return XMLType.XSD_STRING;
        }else if(PrimitiveType.FLOAT.equals(type)){
            return XMLType.XSD_FLOAT;
        }else if(PrimitiveType.DOUBLE.equals(type)){
            return XMLType.XSD_DOUBLE;
        }else if(PrimitiveType.DATE.equals(type)){
            return XMLType.XSD_DATETIME;
        }else if(PrimitiveType.SHORT.equals(type)){
            return XMLType.XSD_SHORT;
        }else if(PrimitiveType.LONG.equals(type)){
            return XMLType.XSD_LONG;
        }else{
            return XMLType.XSD_HEXBINARY;
        }
    }
}

