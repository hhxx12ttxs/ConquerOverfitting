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

package name.huliqing.qblog.dao;

import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import name.huliqing.qblog.entity.CounterEn;
import name.huliqing.qblog.enums.ArticleSecurity;

/**
 *
 * @author huliqing
 */
public class ArticleCounter extends CounterDa{
    private final static ArticleCounter ins = new ArticleCounter();
    public final static ArticleCounter getInstance() {
        return ins;
    }

    /**
     * ????
     * @param ce
     */
    protected Integer synchronize(CounterType ct) {
        long startTime = System.currentTimeMillis();
        logger.info("?????????CounterType=" + ct.name());
        Store store = new Store();
        Long startId = 1L;
        Long lastId = null;
        if (ct == CounterType.article_draft) {
            while ((lastId = countArticle(ArticleSecurity.DRAFT, startId, store)) != null) {
                startId = lastId + 1;
            }
        } else if (ct == CounterType.article_public) {
            while ((lastId = countArticle(ArticleSecurity.PUBLIC, startId, store)) != null) {
                startId = lastId + 1;
            }
        } else if (ct == CounterType.article_private) {
            while ((lastId = countArticle(ArticleSecurity.PRIVATE, startId, store)) != null) {
                startId = lastId + 1;
            }
        }
        logger.info("?????????CounterType=" + ct.name() + ", ??total=" + store.getValue() + ", ????" + (System.currentTimeMillis() - startTime));
        return store.getValue();
    }

    /**
     * ??????
     * @param security ????
     * @param startId ?????articleId
     * @param store ??????????
     * @return
     */
    public Long countArticle(ArticleSecurity security, Long startId, Store store) {
        long startTime = System.currentTimeMillis();
        String hql = "select obj.articleId from ArticleEn obj " +
                " where obj.articleId >=:startId " +
                " and obj.security =:security " +
                " order by obj.articleId asc ";
        EntityManager em = getEM();
        int size = 0;
        Long lastId = null;
        try {
            Query q = em.createQuery(hql);
            q.setParameter("startId", startId);
            q.setParameter("security", security);
            q.setFirstResult(0);
            q.setMaxResults(1000);
            List<Long> aids = q.getResultList();
            if (aids != null && !aids.isEmpty()) {
                store.add(aids.size());
                lastId = aids.get(aids.size() - 1);
                size = aids.size();
            }
        } finally {
            em.close();
        }
        logger.info("count=" + size + ", use time=" + (System.currentTimeMillis() - startTime));
        return lastId;
    }

    public void eventSave(ArticleSecurity security) {
        if (security == ArticleSecurity.DRAFT) {
            increase(CounterType.article_draft);
        } else if (security == ArticleSecurity.PRIVATE) {
            increase(CounterType.article_private);
        } else if (security == ArticleSecurity.PUBLIC) {
            increase(CounterType.article_public);
        }
    }
    
    public void eventDelete(ArticleSecurity security) {
        if (security == ArticleSecurity.DRAFT) {
            decrease(CounterType.article_draft);
        } else if (security == ArticleSecurity.PRIVATE) {
            decrease(CounterType.article_private);
        } else if (security == ArticleSecurity.PUBLIC) {
            decrease(CounterType.article_public);
        }
    }

    /**
     * ???Security????????????????????
     * @param before ??Security???Article???
     * @param after ??Security???Article???
     */
    public void eventUpdate(ArticleSecurity before, ArticleSecurity after) {
        if (before == after) {
            return;
        }
        eventDelete(before);
        eventSave(after);
    }

    // ----

    public CounterEn findCounter(CounterType ct) {
        return super.find(ct);
    }

    public Integer getTotalDraft() {
        CounterEn ce = find(CounterType.article_draft);
        return ce.getTotal();
    }

    public Integer getTotalPrivate() {
        CounterEn ce = find(CounterType.article_private);
        return ce.getTotal();
    }

    public Integer getTotalPublic() {
        CounterEn ce = find(CounterType.article_public);
        return ce.getTotal();
    }

    // ----

    /**
     * ??Draft??????
     * @param total
     */
    public void updateDraftCounter(int total) {
        CounterEn ce = find(CounterType.article_draft);
        ce.setTotal(total);
        ce.setLastDate(new Date());
        super.update(ce);
    }
    /**
     * ??Public??????
     * @param total
     */
    public void updatePublicCounter(int total) {
        CounterEn ce = find(CounterType.article_public);
        ce.setTotal(total);
        ce.setLastDate(new Date());
        super.update(ce);
    }
    /**
     * ??Private??????
     * @param total
     */
    public void updatePrivateCounter(int total) {
        CounterEn ce = find(CounterType.article_private);
        ce.setTotal(total);
        ce.setLastDate(new Date());
        super.update(ce);
    }
}

