/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 huliqing, huliqing.cn@gmail.com
 *
 * This file is part of QBlog.
 * QBlog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QBlog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with QBlog.  If not, see <http://www.gnu.org/licenses/>.
 *
 * ?????QBlog?????
 * ?????????????????????????????.
 * QBlog????????????????????????????????
 * ????????????????????LGPL3????????????.
 * ??LGPL????????COPYING?COPYING.LESSER???
 * ????QBlog????????LGPL??????
 * ??????????? http://www.gnu.org/licenses/ ???
 *
 * - Author: Huliqing
 * - Contact: huliqing.cn@gmail.com
 * - License: GNU Lesser General Public License (LGPL)
 * - Blog and source code availability: http://www.huliqing.name/
 */

package name.huliqing.qblog.processor.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import name.huliqing.qblog.QBlog;

/**
 *
 * @author huliqing
 */
public class RSSDataTable extends HtmlDataTable implements java.io.Serializable{

    // ????
    private List<String> tagsAll;

    // ??RSS?????
    private List<String> tagsRSS;

    // ??????
    private Boolean showIndex;

    // ???????????? _self,???_blank
    private String target;

    // RSS????,??????
    private String rssImage;

    // ?????Tag?????Map, K -> tagName, V -> article total
    private Map<String, Integer> sumArticleMap;

    // ??ModuleId
    private Long moduleId;

    private Object[] _values;
    @Override
    public void restoreState(FacesContext fc, Object state) {
        _values = (Object[]) state;
        super.restoreState(fc, _values[0]);
        this.tagsAll = (List<String>) _values[1];
        this.tagsRSS = (List<String>) _values[2];
        this.rssImage = (String) _values[3];
        this.target = (String) _values[4];
        this.showIndex = (Boolean) _values[5];
        this.sumArticleMap = (Map<String, Integer>) _values[6];
        this.moduleId = (Long) _values[7];
    }

    @Override
    public Object saveState(FacesContext fc) {
        if (_values == null) {
            _values = new Object[8];
        }
        _values[0] = super.saveState(fc);
        _values[1] = this.tagsAll;
        _values[2] = this.tagsRSS;
        _values[3] = this.rssImage;
        _values[4] = this.target;
        _values[5] = this.showIndex;
        _values[6] = this.sumArticleMap;
        _values[7] = this.moduleId;
        return _values;
    }

    public List<String> getTagsAll() {
        return tagsAll;
    }

    public void setTagsAll(List<String> tagsAll) {
        this.tagsAll = tagsAll;
    }

    public List<String> getTagsRSS() {
        return tagsRSS;
    }

    public void setTagsRSS(List<String> tagsRSS) {
        this.tagsRSS = tagsRSS;
    }

    public String getRssImage() {
        return rssImage;
    }

    public void setRssImage(String rssImage) {
        this.rssImage = rssImage;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Boolean getShowIndex() {
        return showIndex;
    }

    public void setShowIndex(Boolean showIndex) {
        this.showIndex = showIndex;
    }

    public Map<String, Integer> getSumArticleMap() {
        return sumArticleMap;
    }

    public void setSumArticleMap(Map<String, Integer> sumArticleMap) {
        this.sumArticleMap = sumArticleMap;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {}

    @Override
    public void encodeEnd(FacesContext context) throws IOException {}

    @Override
    public void encodeChildren(FacesContext fc) throws IOException {
        if (moduleId == null)
            throw new NullPointerException("RSS????moduleId???null");

        if (tagsAll != null && !tagsAll.isEmpty()) {
            Long pageId = QBlog.getPageId();
            ResponseWriter rw = fc.getResponseWriter();
            int i = 0;
            for (String tag : tagsAll) {
                i++;
                encodeTag(rw, tag, pageId, i);
            }
        }
    }

    private void encodeTag(ResponseWriter rw, String tag, Long pageId, int index) throws IOException {
        rw.startElement("div", this);
        rw.writeAttribute("style", "height:20px;line-height:1.2;", null);
            // ??
            if (showIndex != null && showIndex) {
                rw.startElement("span", this);
                rw.writeAttribute("style", "color:gray;", null);
                rw.writeText(index + ". ", null);
                rw.endElement("span");
            }

            // ????
            if ("all".equals(tag)) {
                rw.startElement("span", this);
                rw.writeAttribute("style", "color:gray;", null);
                rw.writeText("????", null);
                rw.endElement("span");
            } else {
                rw.startElement("a", this);
                rw.writeAttribute("href", "/articles/pageId=" + pageId + ",tag=" + URLEncoder.encode(tag, "utf8"), null);
                rw.writeAttribute("target", target, null);
                rw.writeText(tag, null);
                rw.endElement("a");
            }

            // ?????
            if (sumArticleMap != null && sumArticleMap.containsKey(tag)) {
                long total = sumArticleMap.get(tag);
                if (total > 0) {
                    String totalStr = total >= 1000 ? "1000+" : String.valueOf(total);
                    rw.startElement("span", this);
                    rw.writeAttribute("style", "color:gray;", null);
                    rw.writeText(" (" + totalStr + ") ", null);
                    rw.endElement("span");
                }
            }

            // ??RSS??
            if (isRSSTag(tag)) {
                rw.startElement("a", this);
                rw.writeAttribute("href", "/rss/?moduleId=" + moduleId + "&tag=" + tag, null);
                rw.writeAttribute("target", "_blank", null);
                rw.startElement("img", this);
                rw.writeAttribute("style", "vertical-align:middle;margin-left:3px;", null);
                rw.writeAttribute("src", this.rssImage, null);
                rw.writeAttribute("alt", "RSS", null);
                rw.endElement("img");
                rw.endElement("a");
            }
        rw.endElement("div");
    }

    private boolean isRSSTag(String tag) {
        return (tagsRSS != null && tagsRSS.contains(tag));
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}

