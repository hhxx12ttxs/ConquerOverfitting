/*
 * AdroitLogic UltraESB Enterprise Service Bus
 *
 * Copyright (c) 2010-2012 AdroitLogic Private Ltd. (http://adroitlogic.org). All Rights Reserved.
 *
 * GNU Affero General Public License Usage
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program (See LICENSE-AGPL.TXT).
 * If not, see http://www.gnu.org/licenses/agpl-3.0.html
 *
 * Commercial Usage
 *
 * Licensees holding valid UltraESB Commercial licenses may use this file in accordance with the UltraESB Commercial
 * License Agreement provided with the Software or, alternatively, in accordance with the terms contained in a written
 * agreement between you and AdroitLogic.
 *
 * If you are unsure which license is appropriate for your use, or have questions regarding the use of this file,
 * please contact AdroitLogic at info@adroitlogic.com
 */

package org.adroitlogic.ultraesb.admin.util;

import org.adroitlogic.ultraesb.admin.UserManagerAdmin;
import org.adroitlogic.ultraesb.admin.to.AccessRuleInfo;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniFactorySupport;
import org.apache.shiro.web.filter.mgt.*;
import org.apache.shiro.web.servlet.IniShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author sampath
 * @since 1.5.0
 */
public class ConsoleIniShiroFilter extends IniShiroFilter {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleIniShiroFilter.class);

    @Override
    public void init() throws Exception {
        super.init();
        UserManagerAdmin.Holder.instance.setFilter(this);
    }

    public Collection<AccessRuleInfo> getAccessRules() {
        Ini ini = IniFactorySupport.loadDefaultClassPathIni();
        Ini.Section section = ini.getSection("urls");
        FilterChainManager fcm = getFCM();
        Collection<AccessRuleInfo> accessRules = new ArrayList<AccessRuleInfo>(fcm.getChainNames().size());
        int order = 1;
        for (String name : fcm.getChainNames()) {
            String accessString = section.get(name);
            List<String> accessFilters = Arrays.asList(accessString.split(","));
            accessRules.add(new AccessRuleInfo(name, accessFilters, order++));
        }
        return accessRules;
    }

    public AccessRuleInfo getAccessRule(String url) {
        Ini ini = IniFactorySupport.loadDefaultClassPathIni();
        Ini.Section section = ini.getSection("urls");
        FilterChainManager fcm = getFCM();
        int order = 1;
        for (String name : fcm.getChainNames()) {
            if (name.equals(url)) {
                String accessString = section.get(name);
                List<String> accessFilters = Arrays.asList(accessString.split(","));
                return new AccessRuleInfo(name, accessFilters, order);
            }
            order++;
        }
        return null;
    }

    public void addAccessRule(AccessRuleInfo info) {
        DefaultFilterChainManager fcm = getFCM();
        Map<String, NamedFilterList> existingFilterChains = fcm.getFilterChains();
        if (existingFilterChains.containsKey(info.getUrl())) {
            throw new IllegalStateException("There is already an access rule for the path " + info.getUrl());
        }

        String accessString = info.getAccess().toString();
        accessString = accessString.substring(1, accessString.length() - 1);

        Ini ini = IniFactorySupport.loadDefaultClassPathIni();
        Ini.Section existingSection = ini.getSection("urls");

        LinkedHashMap<String, String> newSection = new LinkedHashMap<String, String>();
        LinkedHashMap<String, NamedFilterList> newFilterChains = new LinkedHashMap<String, NamedFilterList>();

        int i = 1;
        boolean added = false;
        for (Map.Entry<String, NamedFilterList> entry : existingFilterChains.entrySet()) {
            if (i++ == info.getOrder()) {
                newFilterChains.put(info.getUrl(), new SimpleNamedFilterList(info.getUrl()));
                newSection.put(info.getUrl(), accessString);
                added = true;
            }
            newFilterChains.put(entry.getKey(), entry.getValue());
            newSection.put(entry.getKey(), existingSection.get(entry.getKey()));
        }
        if (!added) {
            newFilterChains.put(info.getUrl(), new SimpleNamedFilterList(info.getUrl()));
            newSection.put(info.getUrl(), accessString);
        }
        fcm.setFilterChains(newFilterChains);
        fcm.createChain(info.getUrl(), accessString);
        existingSection.clear();
        existingSection.putAll(newSection);
        persistAccessRules(ini);
    }

    public void updateAccessRule(AccessRuleInfo info) {
        DefaultFilterChainManager fcm = getFCM();
        Map<String, NamedFilterList> existingFilterChains = fcm.getFilterChains();
        if (!existingFilterChains.containsKey(info.getUrl())) {
            throw new IllegalStateException("There is no access rule for the path " + info.getUrl());
        }

        String accessString = info.getAccess().toString();
        accessString = accessString.substring(1, accessString.length() - 1);

        Ini ini = IniFactorySupport.loadDefaultClassPathIni();
        Ini.Section section = ini.getSection("urls");
        LinkedHashMap<String, String> newSection = new LinkedHashMap<String, String>();
        LinkedHashMap<String, NamedFilterList> newFilterChains = new LinkedHashMap<String, NamedFilterList>();
        int i = 1;
        boolean added = false;
        for (Map.Entry<String, NamedFilterList> entry : existingFilterChains.entrySet()) {
            if (i++ == info.getOrder() && entry.getKey().equals(info.getUrl())) {
                newFilterChains.put(info.getUrl(), new SimpleNamedFilterList(info.getUrl()));
                newSection.put(info.getUrl(), accessString);
                added = true;
            } else {
                newFilterChains.put(entry.getKey(), entry.getValue());
                newSection.put(entry.getKey(), section.get(entry.getKey()));
            }
        }
        if (!added) {
            throw new IllegalArgumentException("The AccessRuleInfo provided is not valid in the given order");
        }
        fcm.setFilterChains(newFilterChains);
        fcm.createChain(info.getUrl(), accessString);
        section.clear();
        section.putAll(newSection);
        persistAccessRules(ini);
    }

    public void deleteAccessRule(String accessRuleURL) {
        DefaultFilterChainManager fcm = getFCM();
        Map<String, NamedFilterList> existingFilterChains = fcm.getFilterChains();
        if (!existingFilterChains.containsKey(accessRuleURL)) {
            throw new IllegalStateException("There is no access rule for the path " + accessRuleURL);
        }

        Ini ini = IniFactorySupport.loadDefaultClassPathIni();
        Ini.Section section = ini.getSection("urls");
        LinkedHashMap<String, String> newSection = new LinkedHashMap<String, String>();
        LinkedHashMap<String, NamedFilterList> newFilterChains = new LinkedHashMap<String, NamedFilterList>();
        for (Map.Entry<String, NamedFilterList> entry : existingFilterChains.entrySet()) {
            if (!entry.getKey().equals(accessRuleURL)) {
                newFilterChains.put(entry.getKey(), entry.getValue());
                newSection.put(entry.getKey(), section.get(entry.getKey()));
            }
        }
        fcm.setFilterChains(newFilterChains);
        section.clear();
        section.putAll(newSection);
        persistAccessRules(ini);
    }

    public void moveAccessRule(int currentOrder, int newOrder) {
        if (newOrder == currentOrder) {
            return;
        }
        DefaultFilterChainManager fcm = getFCM();
        Map<String, NamedFilterList> existingFilterChains = fcm.getFilterChains();
        if (currentOrder < 1 || newOrder < 1 || currentOrder > existingFilterChains.size()
            || newOrder > existingFilterChains.size()) {
            throw new IndexOutOfBoundsException("The move results in an non existing index");
        }

        Ini ini = IniFactorySupport.loadDefaultClassPathIni();
        Ini.Section section = ini.getSection("urls");
        LinkedHashMap<String, String> newSection = new LinkedHashMap<String, String>();
        LinkedHashMap<String, NamedFilterList> newFilterChains = new LinkedHashMap<String, NamedFilterList>();
        if (currentOrder > newOrder) {
            int i = 1;
            LinkedHashMap<String, String> tempSection = new LinkedHashMap<String, String>();
            LinkedHashMap<String, NamedFilterList> tempFilterChains = new LinkedHashMap<String, NamedFilterList>();
            for (Map.Entry<String, NamedFilterList> entry : existingFilterChains.entrySet()) {
                if (i < newOrder || i > currentOrder) {
                    newFilterChains.put(entry.getKey(), entry.getValue());
                    newSection.put(entry.getKey(), section.get(entry.getKey()));
                } else if (i >= newOrder && i < currentOrder) {
                    tempFilterChains.put(entry.getKey(), entry.getValue());
                    tempSection.put(entry.getKey(), section.get(entry.getKey()));
                } else {
                    newFilterChains.put(entry.getKey(), entry.getValue());
                    newSection.put(entry.getKey(), section.get(entry.getKey()));
                    newFilterChains.putAll(tempFilterChains);
                    newSection.putAll(tempSection);
                }
                i++;
            }
        } else {
            int i = 1;
            String currentURL = null;
            NamedFilterList currentFilterList = null;
            for (Map.Entry<String, NamedFilterList> entry : existingFilterChains.entrySet()) {
                if (i == currentOrder) {
                    currentURL = entry.getKey();
                    currentFilterList = entry.getValue();
                } else if (i == newOrder) {
                    newFilterChains.put(entry.getKey(), entry.getValue());
                    newSection.put(entry.getKey(), section.get(entry.getKey()));
                    newFilterChains.put(currentURL, currentFilterList);
                    newSection.put(currentURL, section.get(currentURL));
                } else {
                    newFilterChains.put(entry.getKey(), entry.getValue());
                    newSection.put(entry.getKey(), section.get(entry.getKey()));
                }
                i++;
            }
        }
        fcm.setFilterChains(newFilterChains);
        section.clear();
        section.putAll(newSection);
        persistAccessRules(ini);
    }

    private DefaultFilterChainManager getFCM() {
        if (!(getFilterChainResolver() instanceof PathMatchingFilterChainResolver)) {
            throw new UnsupportedOperationException("Access Rules can only be retrieved when the filter chain " +
                "resolver is a PathMatchingFilterChainResolver");
        }
        return (DefaultFilterChainManager)((PathMatchingFilterChainResolver) getFilterChainResolver()).getFilterChainManager();
    }

    private void persistAccessRules(Ini ini) {
        try {
            FileWriter writer = new FileWriter("uconsole/WEB-INF/classes/shiro.ini");
            for (Ini.Section section : ini.getSections()) {
                writer.write("[");
                writer.write(section.getName());
                writer.write("]");
                writer.write("\r\n");
                for (String key : section.keySet()) {
                    writer.write(key);
                    writer.write(" = ");
                    writer.write(section.get(key));
                    writer.write("\r\n");
                }
                writer.write("\r\n");
            }
            writer.flush();
        } catch (IOException e) {
            logger.error("Error in persisting the Access Rules, rules may not have been persisted", e);
        }
    }

}

