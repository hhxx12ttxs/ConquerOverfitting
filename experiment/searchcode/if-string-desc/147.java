/*
 * Copyright 2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.hotswap.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.alibaba.hotswap.runtime.HotswapMethodIndexHolder;
import com.alibaba.hotswap.util.HotswapFieldUtil;
import com.alibaba.hotswap.util.HotswapMethodUtil;

/**
 * @author yong.zhuy 2012-5-18 12:39:09
 */
public class ClassMeta {

    public String                  name;
    public String                  path;
    public long                    lastModified;
    public int                     loadedIndex         = 0;
    public Class<?>                clazz;
    public boolean                 isInterface         = false;
    public byte[]                  loadedBytes;
    public Class<?>                vClass;
    public String                  vClassName;
    public ClassLoader             loader;

    // Class's fields
    public Map<String, FieldMeta>  fieldMetas          = new HashMap<String, FieldMeta>();
    public Map<String, FieldNode>  loadedFieldNodes    = new HashMap<String, FieldNode>();
    public Map<String, FieldNode>  primaryFieldNodes   = new HashMap<String, FieldNode>();
    public List<String>            primaryFieldKeyList = new ArrayList<String>();

    // Class's methods
    public Map<String, MethodMeta> initMetas           = new HashMap<String, MethodMeta>();
    public Map<String, MethodNode> primaryInitNodes    = new HashMap<String, MethodNode>();
    public List<String>            primaryInitKeyList  = new ArrayList<String>();

    public void addloadedFieldMeta(FieldNode fn) {
        loadedFieldNodes.put(HotswapFieldUtil.getFieldKey(fn.name, fn.desc), fn);
    }

    public void putFieldMeta(FieldMeta fm) {
        fm.loadedIndex = this.loadedIndex;
        fieldMetas.put(fm.getKey(), fm);
    }

    // for primary field
    public void putFieldMeta(int access, String name, String desc, String signature, Object value) {
        String key = HotswapFieldUtil.getFieldKey(name, desc);
        FieldMeta fm = fieldMetas.get(key);
        if (fm == null) {
            fm = new FieldMeta(access, name, desc, signature, value);
            fm.added = false;
            primaryFieldKeyList.add(key);
        }

        fm.access = access;
        fm.name = name;
        fm.desc = desc;
        fm.signature = signature;
        fm.value = value;
        fm.loadedIndex = this.loadedIndex;
        fieldMetas.put(key, fm);
    }

    // for new field
    public void addFieldMeta(int access, String name, String desc, String signature, Object value) {
        FieldMeta fm = new FieldMeta(access, name, desc, signature, value);
        String key = fm.getKey();

        fm.loadedIndex = this.loadedIndex;
        fm.added = true;

        fieldMetas.put(key, fm);
    }

    public void removeFieldMeta(String fieldKey) {
        fieldMetas.remove(fieldKey);
    }

    public boolean containField(String fieldKey) {
        return fieldMetas.containsKey(fieldKey);
    }

    public FieldMeta getFieldMeta(String fieldKey) {
        FieldMeta fm = fieldMetas.get(fieldKey);
        return fm;
    }

    public void refreshInitMeta(MethodMeta initMeta, boolean added) {
        initMeta.setIndex(HotswapMethodIndexHolder.getMethodIndex(name, initMeta.name, initMeta.desc));

        initMeta.added = added;
        initMeta.loadedIndex = this.loadedIndex;
        initMetas.put(initMeta.getKey(), initMeta);
    }

    public boolean isPrimaryInitMethod(String name, String desc) {
        String mk = HotswapMethodUtil.getMethodKey(name, desc);
        MethodMeta mm = initMetas.get(mk);
        return primaryInitKeyList.contains(mk) && mm != null && !mm.isAdded() && !mm.isDeleted(loadedIndex);
    }

    public boolean isLoaded() {
        return clazz != null;
    }

    public void reset() {
        loadedFieldNodes.clear();
        this.loadedIndex++;
    }

    public String toString() {
        return name + " [" + (clazz != null) + "], loadedIndex [" + loadedIndex + "], fieldMeta {"
               + getFieldMetasString() + "}";
    }

    private String getFieldMetasString() {
        StringBuilder sb = new StringBuilder();
        int index = 0, size = fieldMetas.size();
        for (FieldMeta fm : fieldMetas.values()) {
            sb.append(fm.toString(this.loadedIndex));
            if (++index != size) {
                sb.append("; ");
            }
        }

        return sb.toString();
    }
}

