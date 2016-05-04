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

package name.huliqing.qblog.task;

import java.util.logging.Logger;
import name.huliqing.qblog.dao.ArticleCounter;
import name.huliqing.qblog.dao.CounterDa.CounterType;
import name.huliqing.qblog.dao.Store;
import name.huliqing.qblog.entity.CounterEn;
import name.huliqing.qblog.enums.ArticleSecurity;

/**
 * ??article??
 * @author huliqing
 */
public class TaskArticleDraftCounter implements Task{
    private final static Logger logger = Logger.getLogger(TaskArticleDraftCounter.class.getName());

    // ????
    private int count;

    // ?????????
    private boolean isContinue = false;

    // ???????articleId??
    private Long lastArticleId;

    public boolean execute() {
        CounterEn ce = ArticleCounter.getInstance().findCounter(CounterType.article_draft);
        if (ce.getLastDate() != null) {
            long diff = System.currentTimeMillis() - ce.getLastDate().getTime();
            if (diff < 86400000) { // 1000 * 60 * 60 * 24
                // ?????????
                logger.info("??????draft article???????: " + (diff / 3600000) + " Hours????????.");
                return true;
            }
        }
        logger.info("????draft article???.");
        if (!isContinue) {
            // ?0,???lastArticleId
            count = 0;
            lastArticleId = 1L;
            isContinue = true;
        }
        Long startId = lastArticleId;
        Long lastId = null;
        Store store = new Store();
        while ((lastId = ArticleCounter.getInstance().countArticle(ArticleSecurity.DRAFT, startId, store)) != null) {
            count += store.getValue();
            lastArticleId = lastId;
            startId = lastId + 1;
            store.clear();
        }
        ArticleCounter.getInstance().updateDraftCounter(count);
        lastArticleId = null;
        isContinue = false;
        logger.info("??draft article ?????,???" + count);
        return true;
    }
}

