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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import name.huliqing.qblog.QBlog;
import name.huliqing.qblog.entity.ModuleEn;
import name.huliqing.qblog.processor.HtmlProcessor;
import name.huliqing.qblog.processor.attr.AttrInputText;
import name.huliqing.qblog.processor.attr.AttrInputTextarea;
import name.huliqing.qblog.processor.attr.AttrSelectBooleanCheckbox;
import name.huliqing.qblog.processor.attr.Attribute2;

/**
 *
 * @author huliqing
 */
public class TuDouFlashPlayerProcessor extends HtmlProcessor{

    @Override
    public String makeHTML(ModuleEn module) {
        String pid = null;        // Playid, iid or lid
        boolean isLid = false;  // Is it a lid
        AttrMap attrs = getAttributes(module);

        // 1.???URL???
        pid = QBlog.getParam("iid");
        if (pid != null) {
            isLid = false;
        } else {
            pid = QBlog.getParam("lid");
            if (pid != null) {
                isLid = true;
            }
        }
        
        // 2.????????
        if (pid == null) {
            // list?????? iid=3423424,iid=4343422,lid=3434433,
            String list = attrs.getAsString("List", "");
            Integer playIndex = attrs.getAsInteger("Play Index", null);
            String[] iids = list.split(",");
            if (iids.length > 0) {
                if (playIndex != null && playIndex > 0 && playIndex <= iids.length) {
                    pid = iids[playIndex - 1];
                } else {
                    pid = iids[new Random().nextInt(iids.length)];
                }
            }
            // ???lid(??)
            isLid = pid.contains("lid=");

            // ????=?
            if (pid.contains("=")) {
                pid = pid.substring(pid.indexOf("=") + 1);
            }
        }

        return render(pid, isLid, attrs);
    }
    
    private String render(String pid, boolean isLid, AttrMap attrs) {
        String width = attrs.getAsString("Width", "");
        String height = attrs.getAsString("Height", "");
        String autoStart = attrs.getAsString("Auto Start", "true");
        
        // ????:http://www.tudou.com/player/outside/player_outside_list.swf?lid=7136365&default_skin=http://js.tudouui.com/bin/player2/outside/Skin_outside_list_4.swf&autostart=true&autoPlay=true&rurl=
        // ????:http://www.tudou.com/player/outside/player_outside.swf?iid=58951991&snap_pic=http%3A%2F%2Fi01.img.tudou.com%2Fdata%2Fimgs%2Fi%2F058%2F951%2F991%2Fw.jpg&default_skin=http%3A%2F%2Fjs.tudouui.com%2Fbin%2Fplayer2%2Foutside%2FSkin_outside_52.swf&autostart=true&autoPlay=true&rurl=
        String url = null;
        if (isLid) { // lid
            url = "http://www.tudou.com/player/outside/player_outside_list.swf?lid=" + pid + "&amp;default_skin=http://js.tudouui.com/bin/player2/outside/Skin_outside_list_4.swf&amp;autostart=" + autoStart + "&amp;autoPlay=" + autoStart + "&amp;rurl=";
        } else { // iid
            url = "http://www.tudou.com/player/outside/player_outside.swf?iid=" + pid + "&amp;snap_pic=http%3A%2F%2Fi01.img.tudou.com%2Fdata%2Fimgs%2Fi%2F058%2F951%2F991%2Fw.jpg&amp;default_skin=http%3A%2F%2Fjs.tudouui.com%2Fbin%2Fplayer2%2Foutside%2FSkin_outside_52.swf&amp;autostart=" + autoStart + "&amp;autoPlay=" + autoStart + "&amp;rurl=";
        }
        StringBuilder obj = new StringBuilder();
        obj.append("<embed src=\"").append(url).append("\" ")
            .append(" type=\"application/x-shockwave-flash\" ")
            .append(" width=\"").append(width).append("\" ") 
            .append(" height=\"").append(height).append("\" ")
            .append(" wmode=\"opaque\" ")
            .append(" allowfullscreen=\"true\" ")
            .append(" ></embed>");
        
        return obj.toString();
    }

    public List<Attribute2> getRequiredAttributes() {
        ArrayList<Attribute2> attrs = new ArrayList<Attribute2>(5);
        // iid list / lid list
        AttrInputTextarea list = new AttrInputTextarea("List", "",
                "iid/lid????id???????(,)??," +
                "????iid=54602842,iid=55900471,lid=7136365??????? iid????????????????" +
                "54602842,55900471,lid=7136365(????????????????????????????????)");
        list.setStyle("width:99%;");
        
        // iid
        AttrInputText playIndex = new AttrInputText("Play Index", "1",
                "??List?????(?1??).???????.");

        // ????
        AttrSelectBooleanCheckbox autoStart = new AttrSelectBooleanCheckbox("Auto Start", "true", "??????");

        // width
        AttrInputText width = new AttrInputText("Width", "480", "??,???480");

        // height
        AttrInputText height = new AttrInputText("Height", "400", "??,???400");

        attrs.add(list);
        attrs.add(autoStart);
        attrs.add(playIndex);
        attrs.add(width);
        attrs.add(height);
        return attrs;
    }

    public String getName() {
        return "?????";
    }

    public String getDescription() {
        return "???????Flash??????";
    }

}

