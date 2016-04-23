package com.niwodai.toolkit.framework.dao.mapping;

import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.niwodai.toolkit.util.ClassUtil;

//原始类型操作工具类
public enum PrimitiveType {
    STRING(Types.VARCHAR), SHORT(Types.TINYINT), 
    INTEGER(Types.INTEGER), LONG(Types.BIGINT), 
    DOUBLE(Types.DECIMAL),
    BOOLEAN(Types.TINYINT),
    DATE(Types.TIMESTAMP);
   
    private static Logger log = LoggerFactory.getLogger(PrimitiveType.class);
    public static final int VARCHAR_MAX_LENGTH = 8000;
    private int sqlType;

    PrimitiveType(int sqlType) {
        this.sqlType = sqlType;
    }

    public static PrimitiveType get(Class clazz) {
        if (clazz.getSuperclass() == Enum.class) {
            return PrimitiveType.SHORT;
        }

        if (ClassUtil.isInstance(clazz, java.util.Date.class)) {
            return DATE;
        }

        for (PrimitiveType type : PrimitiveType.values()) {
            if (type.toString().equals(clazz.getSimpleName().toUpperCase())) {
                return type;
            }
        }
        throw new RuntimeException("找不到"+clazz+"对应的PrimitiveType");
    }

    public int sqlType() {
        return sqlType;
    }

    public Object convert(Object obj) {
        switch (this) {
        case STRING:
            return ClassUtil.toString(obj);
        case INTEGER:
            return ClassUtil.toInt(obj);
        case LONG:
            return ClassUtil.toLong(obj);
        case SHORT:
            return ClassUtil.toShort(obj);
        case DATE:
            return ClassUtil.toDate(obj);
        case DOUBLE:
            return ClassUtil.toDouble(obj);
        case BOOLEAN:
            return ClassUtil.toBoolean(obj);
        }
        throw new RuntimeException("无法将数据" + obj + "转换为" + this);
    }
    
    //该字段是否能进行比较,如果为true,查询时页面可以通过结尾增加Begin和End
    public boolean compareAble() {
    	if(this==STRING){
    		return false;
    	}
    	if(this==SHORT){
    		return false;
    	}
        return true;
    }
}


