/*
 ************************************************************************************
 * Copyright (C) 2001-2011 encuestame: system online surveys Copyright (C) 2011
 * encuestame Development Team.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.encuestame.persistence.dao.imp;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.encuestame.persistence.dao.ITweetPoll;
import org.encuestame.persistence.domain.HashTag;
import org.encuestame.persistence.domain.question.QuestionAnswer;
import org.encuestame.persistence.domain.security.Account;
import org.encuestame.persistence.domain.security.SocialAccount;
import org.encuestame.persistence.domain.security.UserAccount;
import org.encuestame.persistence.domain.survey.Poll;
import org.encuestame.persistence.domain.survey.Survey;
import org.encuestame.persistence.domain.tweetpoll.TweetPoll;
import org.encuestame.persistence.domain.tweetpoll.TweetPollFolder;
import org.encuestame.persistence.domain.tweetpoll.TweetPollResult;
import org.encuestame.persistence.domain.tweetpoll.TweetPollSavedPublishedStatus;
import org.encuestame.persistence.domain.tweetpoll.TweetPollSwitch;
import org.encuestame.utils.DateUtil;
import org.encuestame.utils.RestFullUtil;
import org.encuestame.utils.enums.SearchPeriods;
import org.encuestame.utils.enums.Status;
import org.encuestame.utils.enums.TypeSearchResult;
import org.encuestame.utils.social.SocialProvider;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

/**
 * TweetPoll Dao Implementation.
 *
 * @author Picado, Juan juanATencuestame.org
 * @since Feb 17, 2010 8:26:57 PM
 */
@Repository("tweetPollDao")
public class TweetPollDao extends AbstractHibernateDaoSupport implements ITweetPoll {

    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    public TweetPollDao(SessionFactory sessionFactory) {
        setSessionFactory(sessionFactory);
    }

    /**
     * Get TweetPoll by Id.
     *
     * @param tweetPollId
     *            tweetPollId
     * @return {@link TweetPoll}
     * @throws HibernateException
     *             exception
     */
    public TweetPoll getTweetPollById(final Long tweetPollId)
            throws HibernateException {
        return (TweetPoll) getHibernateTemplate().get(TweetPoll.class,
                tweetPollId);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.encuestame.persistence.dao.ITweetPoll#getPublicTweetPollById(java
     * .lang.Long)
     */
    @SuppressWarnings("unchecked")
    public TweetPoll getPublicTweetPollById(final Long tweetPollId) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPoll.class);
        criteria.add(Restrictions.eq("tweetPollId", tweetPollId));
        criteria.add(Restrictions.eq("publishTweetPoll", Boolean.TRUE));
        return (TweetPoll) DataAccessUtils.uniqueResult(getHibernateTemplate()
                .findByCriteria(criteria));
    }

    /**
     * Retrieve Tweets Poll by User Id.
     *
     * @param userId
     *            userId
     * @return list of tweet pools.
     */
    @SuppressWarnings("unchecked")
    public List<TweetPoll> retrieveTweetsByUserId(final String keyWord, final Long userId,
            final Integer maxResults, final Integer start,
            final Boolean isCompleted, final Boolean isScheduled,
            final Boolean isPublished, final Boolean isFavourite,
            final String period) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPoll.class);
        criteria.createAlias("tweetOwner", "tweetOwner");
        // removed because is the advancedSearchOptions should inject this value
        // criteria.add(Restrictions.eq("publishTweetPoll", Boolean.TRUE));
        criteria.add(Restrictions.eq("tweetOwner.id", userId));
        criteria.addOrder(Order.desc("createDate"));
        advancedSearchOptions(criteria, isCompleted, isScheduled, isFavourite, isPublished, keyWord, period);
        return (List<TweetPoll>) filterByMaxorStart(criteria, maxResults, start);
    }

    /**
     * Get TweetPoll by Question Name.
     *
     * @param keyWord
     *            keyword
     * @param userId
     *            user Id.
     * @return
     */
    @SuppressWarnings("unchecked")
    // TODO: migrate search to Hibernate Search.
    public List<TweetPoll> retrieveTweetsByQuestionName(final String keyWord,
            final Long userId, final Integer maxResults, final Integer start,
            final Boolean isCompleted, final Boolean isScheduled,
            final Boolean isFavourite, final Boolean isPublished, final String period) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPoll.class);
        criteria.createAlias("tweetOwner", "tweetOwner");
        criteria.add(Restrictions.eq("tweetOwner.id", userId));
        advancedSearchOptions(criteria, isCompleted, isScheduled, isFavourite, isPublished, keyWord, period);
        return (List<TweetPoll>) filterByMaxorStart(criteria, maxResults, start);
    }

    /**
     * Retrieve TweetPoll Today.
     *
     * @param keyWord
     * @param userId
     * @return
     */
    public List<TweetPoll> retrieveTweetPollToday(final Account account,
            final Integer maxResults, final Integer start,
            final Boolean isCompleted, final Boolean isScheduled,
            final Boolean isFavourite, final Boolean isPublished, final String keyword, final String period) {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return retrieveTweetPollByDate(account,  maxResults,
                start, isCompleted, isScheduled, isFavourite, isPublished, keyword, period, cal.getTime());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.encuestame.persistence.dao.ITweetPoll#retrieveTweetPollByDate(org
     * .encuestame.persistence.domain.security.Account, java.util.Date,
     * java.lang.Integer, java.lang.Integer, java.lang.Boolean,
     * java.lang.Boolean, java.lang.Boolean, java.lang.Boolean,
     * java.lang.String, java.lang.Integer)
     */
    @SuppressWarnings("unchecked")
    public List<TweetPoll> retrieveTweetPollByDate(final Account account,
            final Integer maxResults, final Integer start,
            final Boolean isCompleted, final Boolean isScheduled,
            final Boolean isFavourite, final Boolean isPublished,
            final String keyword, final String period, final Date initDate) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPoll.class);
        criteria.createAlias("tweetOwner", "tweetOwner");
        criteria.add(Restrictions.eq("tweetOwner", account));
        criteria.add(Restrictions.between("createDate", initDate,
             getNextDayMidnightDate()));
        advancedSearchOptions(criteria, isCompleted, isScheduled, isFavourite,
                isPublished, keyword, period);
        return (List<TweetPoll>) filterByMaxorStart(criteria, maxResults, start);
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * org.encuestame.persistence.dao.ITweetPoll#retrieveTweetPollLastWeek(org
     * .encuestame.persistence.domain.security.Account, java.lang.Integer,
     * java.lang.Integer, java.lang.Boolean, java.lang.Boolean,
     * java.lang.Boolean, java.lang.Boolean, java.lang.String,
     * java.lang.Integer)
     */
    public List<TweetPoll> retrieveTweetPollLastWeek(final Account account,
            final Integer maxResults, final Integer start,
            final Boolean isCompleted, final Boolean isScheduled,
            final Boolean isFavourite, final Boolean isPublished,
            final String keyword, final String period) {
          final Date initDate = DateUtil.decreaseDateAsWeek(Calendar.getInstance().getTime());
         return retrieveTweetPollByDate(account,
                maxResults, start, isCompleted, isScheduled, isFavourite,
                isPublished, keyword, period, initDate);
    }

    /**
     * Retrieve Favourites TweetPolls.
     *
     * @param keyWord
     * @param userId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<TweetPoll> retrieveFavouritesTweetPoll(final Account account,
            final Integer maxResults, final Integer start,
            final Boolean isCompleted, final Boolean isScheduled,
            final Boolean isFavourite, final Boolean isPublished,
            final String keyword, final String period) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPoll.class);
        criteria.createAlias("tweetOwner", "tweetOwner");
        criteria.add(Restrictions.eq("tweetOwner", account));
        advancedSearchOptions(criteria, isCompleted, isScheduled, Boolean.TRUE,
                isPublished, keyword, period);
        return (List<TweetPoll>) filterByMaxorStart(criteria, maxResults, start);
    }

    /**
     * Retrieve Favourites TweetPolls.
     *
     * @param keyWord
     * @param userId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<TweetPoll> retrieveScheduledTweetPoll(final Long userId,
            final Integer maxResults, final Integer start,
            final Boolean isCompleted, final Boolean isScheduled,
            final Boolean isFavourite, final Boolean isPublished,
            final String keyword, final String period) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPoll.class);
        criteria.createAlias("tweetOwner", "tweetOwner");
        criteria.add(Restrictions.eq("tweetOwner.uid", userId));
        // To retrieve all and only scheduled Tweetpoll period should be = ALLTIME
        advancedSearchOptions(criteria, isCompleted, isScheduled, isFavourite,
                isPublished, keyword, period);
        return (List<TweetPoll>) filterByMaxorStart(criteria, maxResults, start);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.encuestame.persistence.dao.ITweetPoll#
     * retrievePublishedUnpublishedTweetPoll
     * (org.encuestame.persistence.domain.security.Account, java.lang.Integer,
     * java.lang.Integer, java.lang.Boolean)
     */
    @SuppressWarnings("unchecked")
    public List<TweetPoll> retrievePublishedUnpublishedTweetPoll(final Account account,
            final Integer maxResults, final Integer start, final Boolean isPublished) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPoll.class);
        criteria.createAlias("tweetOwner", "tweetOwner");
        criteria.add(Restrictions.eq("publishTweetPoll", isPublished));
        criteria.add(Restrictions.eq("tweetOwner", account));
        return (List<TweetPoll>) filterByMaxorStart(criteria, maxResults, start);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.encuestame.persistence.dao.ITweetPoll#retrieveCompletedTweetPoll(
     * org.encuestame.persistence.domain.security.Account, java.lang.Integer,
     * java.lang.Integer)
     */
    @SuppressWarnings("unchecked")
    public List<TweetPoll> retrieveCompletedTweetPoll(final Account account,
            final Integer maxResults, final Integer start, final Boolean isComplete) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPoll.class);
        criteria.createAlias("tweetOwner", "tweetOwner");
        criteria.add(Restrictions.eq("publishTweetPoll", Boolean.TRUE));
        criteria.add(Restrictions.eq("tweetOwner", account));
        criteria.add(Restrictions.eq("completed",isComplete));
        return (List<TweetPoll>) filterByMaxorStart(criteria, maxResults, start);
    }


    /**
     * Retrieve {@link TweetPollSwitch} by code.
     *
     * @param tweetCode
     *            tweetCode code.
     * @return switch {@link TweetPollSwitch}
     */
    public TweetPollSwitch retrieveTweetsPollSwitch(final String tweetCode) {
        log.debug("retrieveTweetsPollSwitch codeTweet:" + tweetCode);
        return this.searchByParamStringTweetPollSwitch("codeTweet", tweetCode);
    }

    /**
     * Search By Param String {@link TweetPollSwitch}.
     *
     * @param param
     *            param
     * @param value
     *            value
     * @return
     */
    @SuppressWarnings("unchecked")
    private TweetPollSwitch searchByParamStringTweetPollSwitch(
            final String param, final String value) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPollSwitch.class);
        criteria.add(Restrictions.eq(param, value));
        return (TweetPollSwitch) DataAccessUtils
                .uniqueResult(getHibernateTemplate().findByCriteria(criteria));
    }

    /**
     * Validate Vote IP.
     * @param ip  ip
     * @param tweetPoll tweetPoll
     * @return {@link TweetPollSwitch}
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public TweetPollResult validateVoteIP(final String ip,
            final TweetPoll tweetPoll) {
        return (TweetPollResult) DataAccessUtils
                .uniqueResult(getHibernateTemplate()
                        .findByNamedParam(
                                "from TweetPollResult where ipVote = :ipVote and  tweetPollSwitch.tweetPoll = :tweetPoll",
                                new String[] { "ipVote", "tweetPoll" },
                                new Object[] { ip, tweetPoll }));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.encuestame.persistence.dao.ITweetPoll#validateTweetPollResultsIP(
     * java.lang.String, org.encuestame.persistence.domain.tweetpoll.TweetPoll)
     */
    @SuppressWarnings("unchecked")
    public List<TweetPollResult> validateTweetPollResultsIP(final String ip, final TweetPoll tweetPoll) {
        return getHibernateTemplate()
                .findByNamedParam(
                        "from TweetPollResult where ipVote = :ipVote and  tweetPollSwitch.tweetPoll = :tweetPoll",
                        new String[] { "ipVote", "tweetPoll" },
                        new Object[] { ip, tweetPoll });
    }

    /**
     * Get Results By {@link TweetPoll} && {@link QuestionAnswer}.
     *
     * @param tweetPoll
     *            {@link TweetPoll}
     * @param answers
     *            {@link QuestionAnswer}
     * @return List of {@link TweetPollResult}
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> getResultsByTweetPoll(final TweetPoll tweetPoll,
            final QuestionAnswer answers) {
        return getHibernateTemplate()
                .findByNamedParam(
                        "select tweetPollSwitch.answers.answer, count(tweetPollResultId) from TweetPollResult "
                                + "where tweetPollSwitch.tweetPoll = :tweetPoll and tweetPollSwitch.answers = :answer group by tweetPollSwitch.answers.answer",
                        new String[] { "tweetPoll", "answer" },
                        new Object[] { tweetPoll, answers });
    }

    /**
     *
     * @param tweetPollId
     * @param answerId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> getResultsByTweetPoll(final Long tweetPollId,
            final Long answerId) {
        return getHibernateTemplate()
                .findByNamedParam(
                        " select tweetPollSwitch.answers.answer, tweetPollSwitch.answers.color, count(tweetPollResultId)"
                                + " from TweetPollResult "
                                + " where tweetPollSwitch.tweetPoll.tweetPollId = :tweetPoll "
                                + " and tweetPollSwitch.answers.questionAnswerId = :answer group by tweetPollSwitch.answers.answer, tweetPollSwitch.answers.color",
                        new String[] { "tweetPoll", "answer" },
                        new Object[] { tweetPollId, answerId });
    }

    /**
     * Get List of Switch Answers by TweetPoll.
     *
     * @param tweetPoll
     *            {@link TweetPoll}.
     * @return List of {@link TweetPollSwitch}
     */
    @SuppressWarnings("unchecked")
    public List<TweetPollSwitch> getListAnswesByTweetPoll(
            final TweetPoll tweetPoll) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPollSwitch.class);
        criteria.add(Restrictions.eq("tweetPoll", tweetPoll));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.encuestame.persistence.dao.ITweetPoll#getAnswerTweetSwitch(org.encuestame
     * .persistence.domain.tweetpoll.TweetPoll,
     * org.encuestame.persistence.domain.question.QuestionAnswer)
     */
    @SuppressWarnings("unchecked")
    public TweetPollSwitch getAnswerTweetSwitch(final TweetPoll tweetPoll,
            final QuestionAnswer questionAnswer) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPollSwitch.class);
        criteria.add(Restrictions.eq("tweetPoll", tweetPoll));
        criteria.add(Restrictions.eq("answers", questionAnswer));
        return (TweetPollSwitch) DataAccessUtils
                .uniqueResult(getHibernateTemplate().findByCriteria(criteria));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.encuestame.persistence.dao.ITweetPoll#getAnswerTweetSwitch(org.encuestame
     * .persistence.domain.question.QuestionAnswer)
     */
    @SuppressWarnings("unchecked")
    public List<TweetPollSwitch> getAnswerTweetSwitch(
            final QuestionAnswer questionAnswer) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPollSwitch.class);
        criteria.add(Restrictions.eq("answers", questionAnswer));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /**
     * Get Votes By {@link TweetPollSwitch}..
     *
     * @param pollSwitch
     *            {@link TweetPollSwitch}..
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Long> getVotesByAnswer(final TweetPollSwitch pollSwitch) {
        return getHibernateTemplate()
                .findByNamedParam(
                        "select count(tweetPollResultId) "
                                + " from TweetPollResult where tweetPollSwitch = :tweetPollSwitch",
                        "tweetPollSwitch", pollSwitch);
    }

    /**
     * Get Total Votes By {@link TweetPoll}.
     *
     * @param tweetPoll
     *            {@link TweetPoll}.
     * @return List of Votes.
     */
    public List<Object[]> getTotalVotesByTweetPoll(final Long tweetPollId) {
        final List<Object[]> result = new ArrayList<Object[]>();
        final List<TweetPollSwitch> answers = this
                .getListAnswesByTweetPoll(this.getTweetPollById(tweetPollId));
        for (TweetPollSwitch tweetPollSwitch : answers) {
            final List<Long> answerResult = this
                    .getVotesByAnswer(tweetPollSwitch);
            final Object[] objects = {
                    tweetPollSwitch.getAnswers().getAnswer(),
                    answerResult.get(0) };
            result.add(objects);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.encuestame.persistence.dao.ITweetPoll#getTotalVotesByTweetPollId(java.lang.Long)
     */
    public Long getTotalVotesByTweetPollId(final Long tweetPollId) {
        Long totalvotes = 0L;
        // Type YES-NO
        final List<TweetPollSwitch> answers = this
                .getListAnswesByTweetPoll(this.getTweetPollById(tweetPollId));
        for (TweetPollSwitch tweetPollSwitch : answers) {
            final List<Long> answerResult = this
                    .getVotesByAnswer(tweetPollSwitch); // Count
            for (Long objects : answerResult) {
                if (objects != null) {
                    totalvotes += objects;
                }
            }
            log.info("Total Votes: " + totalvotes);
        }
        return totalvotes;
    }

    /**
     * Retrieve Tweet Polls Folders By UserId
     *
     * @param userId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<TweetPollFolder> retrieveTweetPollFolderByAccount(
            final Account account) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPollFolder.class);
        criteria.add(Restrictions.eq("users", account));
        criteria.add(Restrictions.eq("status",
                org.encuestame.utils.enums.Status.ACTIVE));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /**
     * Retrieve TweetPoll by Folder
     *
     * @param userId
     * @param folderId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<TweetPoll> retrieveTweetPollByFolder(final Long userId,
            final Long folderId) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPoll.class);
        criteria.add(Restrictions.eq("tweetOwner.uid", userId));
        criteria.add(Restrictions.eq("tweetPollFolder.id", folderId));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /**
     * Get TweetPoll Folder By Id.
     *
     * @param folderId
     * @return
     */
    public TweetPollFolder getTweetPollFolderById(final Long folderId) {
        return getHibernateTemplate().get(TweetPollFolder.class, folderId);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.encuestame.persistence.dao.ITweetPoll#getTweetPollFolderByIdandUser
     * (java.lang.Long, org.encuestame.persistence.domain.security.Account)
     */
    @SuppressWarnings("unchecked")
    public TweetPollFolder getTweetPollFolderByIdandUser(final Long folderId,
            final Account account) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPollFolder.class);
        criteria.add(Restrictions.eq("users", account));
        criteria.add(Restrictions.eq("status",
                org.encuestame.utils.enums.Status.ACTIVE));
        criteria.add(Restrictions.eq("id", folderId));
        return (TweetPollFolder) DataAccessUtils
                .uniqueResult(getHibernateTemplate().findByCriteria(criteria));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.encuestame.persistence.dao.ITweetPoll#getTweetPollByIdandUserId(java
     * .lang.Long, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    public TweetPoll getTweetPollByIdandUserId(final Long tweetPollId,
            final Long userId) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPoll.class);
        criteria.add(Restrictions.eq("tweetOwner.uid", userId));
        criteria.add(Restrictions.eq("tweetPollId", tweetPollId));
        return (TweetPoll) DataAccessUtils.uniqueResult(getHibernateTemplate()
                .findByCriteria(criteria));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.encuestame.persistence.dao.ITweetPoll#getTweetPollByIdandSlugName
     * (java.lang.Long, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public TweetPoll getTweetPollByIdandSlugName(final Long tweetPollId,
            final String slugName) throws UnsupportedEncodingException {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPoll.class);
        criteria.createAlias("question", "q");
        criteria.add(Restrictions.eq("tweetPollId", tweetPollId));
        criteria.add(Restrictions.eq("q.slugQuestion",
                     RestFullUtil.encodeUTF8(slugName)));
        return (TweetPoll) DataAccessUtils.uniqueResult(getHibernateTemplate()
                .findByCriteria(criteria));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.encuestame.persistence.dao.ITweetPoll#getTweetpollByHashTagName(java
     * .lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<TweetPoll> getTweetpollByHashTagName(
            final String tagName,
            final Integer startResults,
            final Integer limitResults,
            final TypeSearchResult filterby,
            final SearchPeriods periods) {
        final DetachedCriteria detached = DetachedCriteria
                .forClass(TweetPoll.class)
                .createAlias("hashTags", "hashTags")
                .setProjection(Projections.id())
                .add(Subqueries.propertyIn(
                        "hashTags.hashTagId",
                        DetachedCriteria
                                .forClass(HashTag.class, "hash")
                                .setProjection(Projections.id())
                                .add(Restrictions.in("hash.hashTag",
                                        new String[] { tagName }))));
        final DetachedCriteria criteria = DetachedCriteria.forClass(
                TweetPoll.class, "tweetPoll");
        criteria.add(Subqueries.propertyIn("tweetPoll.tweetPollId", detached));
        //if filters are defined.
        if (filterby != null) {
            if (filterby.equals(TypeSearchResult.HASHTAG)) {
                criteria.addOrder(Order.desc("tweetPoll.createDate"));
            } else if (filterby.equals(TypeSearchResult.HASHTAGRATED)) {
                criteria.addOrder(Order.desc("numbervotes"));
            }
        }
        criteria.add(Restrictions.eq("publishTweetPoll", Boolean.TRUE));
        calculateSearchPeriodsDates(periods, detached, "createDate");
        return (List<TweetPoll>) filterByMaxorStart(criteria, limitResults, startResults);
    }

    @SuppressWarnings("unchecked")
    public TweetPoll checkIfTweetPollHasHashTag(
            final String tagName,
            final SearchPeriods periods, final Long tpoll) {
        final DetachedCriteria detached = DetachedCriteria
                .forClass(TweetPoll.class)
                .createAlias("hashTags", "hashTags")
                .setProjection(Projections.id())
                .add(Restrictions.eq("tweetPoll.tweetPollId", tpoll))
                .add(Subqueries.propertyIn(
                        "hashTags.hashTagId",
                        DetachedCriteria
                                .forClass(HashTag.class, "hash")
                                .setProjection(Projections.id())
                                .add(Restrictions.in("hash.hashTag",
                                        new String[] { tagName }))  ));
        final DetachedCriteria criteria = DetachedCriteria.forClass(
                TweetPoll.class, "tweetPoll");
        criteria.add(Subqueries.propertyIn("tweetPoll.tweetPollId", detached));
        criteria.add(Restrictions.eq("publishTweetPoll", Boolean.TRUE));
        calculateSearchPeriodsDates(periods, detached, "createDate");
        return (TweetPoll) DataAccessUtils.uniqueResult(getHibernateTemplate()
                .findByCriteria(criteria));
    }

    /*
     * (non-Javadoc)
     * @see org.encuestame.persistence.dao.ITweetPoll#getTweetPollsbyHashTagNameAndDateRange(java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    @SuppressWarnings("unchecked")
    public List<TweetPoll> getTweetPollsbyHashTagNameAndDateRange(
            final String tagName,
            final SearchPeriods period) {
        final DetachedCriteria detached = DetachedCriteria
                .forClass(TweetPoll.class)
                .createAlias("hashTags", "hashTags")
                .setProjection(Projections.id())
                .add(Subqueries.propertyIn(
                        "hashTags.hashTagId",
                        DetachedCriteria
                                .forClass(HashTag.class, "hash")
                                .setProjection(Projections.id())
                                .add(Restrictions.in("hash.hashTag",
                                        new String[] { tagName }))));
        final DetachedCriteria criteria = DetachedCriteria.forClass(
                TweetPoll.class, "tweetPoll");
        criteria.add(Subqueries.propertyIn("tweetPoll.tweetPollId", detached));
        criteria.addOrder(Order.desc("tweetPoll.createDate"));
        criteria.add(Restrictions.eq("publishTweetPoll", Boolean.TRUE));
        ProjectionList projList = Projections.projectionList();
        projList.add(Projections.groupProperty("createDate"));
        projList.add(Projections.rowCount());
        criteria.setProjection(projList);
       // calculateSearchPeriodsDates(period, criteria, "createDate");

        return getHibernateTemplate().findByCriteria(criteria);
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * org.encuestame.persistence.dao.ITweetPoll#getLinksByTweetPoll(org.encuestame
     * .persistence.domain.tweetpoll.TweetPoll,
     * org.encuestame.persistence.domain.survey.Survey,
     * org.encuestame.persistence.domain.survey.Poll,
     * org.encuestame.utils.enums.TypeSearchResult)
     */
    @SuppressWarnings("unchecked")
    //TODO: please use FrontEndDao new method.
    /*
     * List<TweetPollSavedPublishedStatus> getLinksByHomeItem replace this method.
     */
    public List<TweetPollSavedPublishedStatus> getLinksByTweetPoll(
            final TweetPoll tweetPoll, final Survey survey, final Poll poll,
            final TypeSearchResult itemType) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPollSavedPublishedStatus.class);
        if (itemType.equals(TypeSearchResult.TWEETPOLL)) {
            criteria.add(Restrictions.eq("tweetPoll", tweetPoll));
            // criteria.addOrder(Order.desc("tweetPoll.createDate"));
        } else if (itemType.equals(TypeSearchResult.POLL)) {
            criteria.add(Restrictions.eq("poll", poll));
            // criteria.addOrder(Order.desc("survey.createdAt"));
        } else if (itemType.equals(TypeSearchResult.SURVEY)) {
            criteria.add(Restrictions.eq("survey", survey));
            // criteria.addOrder(Order.desc("poll.createdAt"));
        } else {
            log.error("Item type not valid: " + itemType);
        }
        criteria.addOrder(Order.desc("publicationDateTweet"));
        criteria.add(Restrictions.isNotNull("apiType"));
        criteria.add(Restrictions.isNotNull("tweetId"));
        criteria.add(Restrictions.eq("status", Status.SUCCESS));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /*
     * (non-Javadoc)
     * @see org.encuestame.persistence.dao.ITweetPoll#getAllLinks(org.encuestame.persistence.domain.tweetpoll.TweetPoll, org.encuestame.persistence.domain.survey.Survey, org.encuestame.persistence.domain.survey.Poll, org.encuestame.utils.enums.TypeSearchResult)
     */
    public List<TweetPollSavedPublishedStatus> getAllLinks(
            final TweetPoll tweetPoll, final Survey survey, final Poll poll,
            final TypeSearchResult itemType) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPollSavedPublishedStatus.class);
        if (itemType.equals(TypeSearchResult.TWEETPOLL)) {
            criteria.add(Restrictions.eq("tweetPoll", tweetPoll));
            // criteria.addOrder(Order.desc("tweetPoll.createDate"));
        } else if (itemType.equals(TypeSearchResult.POLL)) {
            criteria.add(Restrictions.eq("poll", poll));
            // criteria.addOrder(Order.desc("survey.createdAt"));
        } else if (itemType.equals(TypeSearchResult.SURVEY)) {
            criteria.add(Restrictions.eq("survey", survey));
            // criteria.addOrder(Order.desc("poll.createdAt"));
        } else {
            log.error("Item type not valid: " + itemType);
        }
        criteria.addOrder(Order.desc("publicationDateTweet"));
        return getHibernateTemplate().findByCriteria(criteria);
    }    
    
    /*
     * (non-Javadoc)
     * @see org.encuestame.persistence.dao.ITweetPoll#getSocialLinksByTweetPollSearch(org.encuestame.persistence.domain.tweetpoll.TweetPoll, org.encuestame.utils.enums.TypeSearchResult, java.util.List, java.util.List)
     */
    @SuppressWarnings("unchecked")
    public List<TweetPollSavedPublishedStatus> getSocialLinksByTweetPollSearch(
            final TweetPoll tweetPoll, final TypeSearchResult itemType, final List<SocialProvider> splist, final List<SocialAccount> socialAccounts) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPollSavedPublishedStatus.class);
        criteria.createAlias("socialAccount", "socialAccount");
        if (itemType.equals(TypeSearchResult.TWEETPOLL)) {
            criteria.add(Restrictions.eq("tweetPoll", tweetPoll));
            criteria.add(Restrictions.isNotNull("tweetId"));
            criteria.add(Restrictions.eq("status", Status.SUCCESS));
            criteria.add(Restrictions.in("apiType", splist));
            if (socialAccounts.size() > 0) {
                criteria.add(Restrictions.in("socialAccount", socialAccounts));
            }
        }
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.encuestame.persistence.dao.ITweetPoll#getMaxTweetPollLikeVotesbyUser
     * (java.lang.Long, java.util.Date, java.util.Date)
     */
    public Long getMaxTweetPollLikeVotesbyUser(final Long userId) {
        DetachedCriteria criteria = DetachedCriteria.forClass(TweetPoll.class);
        criteria.setProjection(Projections.max("likeVote"));
        criteria.createAlias("editorOwner", "editorOwner");
        criteria.add(Restrictions.eq("editorOwner.uid", userId));
        @SuppressWarnings("unchecked")
        List<Long> results = getHibernateTemplate().findByCriteria(criteria);
        return (Long) (results.get(0) == null ? 0 : results.get(0));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.encuestame.persistence.dao.ITweetPoll#getTweetPolls(java.lang.Integer
     * , java.lang.Integer, java.util.Date)
     */
    @SuppressWarnings("unchecked")
    public List<TweetPoll> getTweetPolls(
            final Integer maxResults,
            final Integer start,
            final Date range) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPoll.class);
        criteria.add(Restrictions.eq("publishTweetPoll", Boolean.TRUE));
        // criteria.add(Restrictions.gt("createDate", range));
        criteria.addOrder(Order.desc("createDate"));
        return (List<TweetPoll>) filterByMaxorStart(criteria, maxResults, start);
    }

   /*
    * (non-Javadoc)
    * @see org.encuestame.persistence.dao.ITweetPoll#getTweetPollByUsername(java.lang.Integer, org.encuestame.persistence.domain.security.UserAccount)
    */
    @SuppressWarnings("unchecked")
    public List<TweetPoll> getTweetPollByUsername(
            final Integer limitResults,
            final UserAccount account) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPoll.class);
        criteria.add(Restrictions.eq("publishTweetPoll", Boolean.TRUE));
        criteria.add(Restrictions.eq("editorOwner", account));
        criteria.addOrder(Order.desc("createDate"));
        return (List<TweetPoll>) filterByMaxorStart(criteria, limitResults, 0);
    }


    /*
     * (non-Javadoc)
     * @see org.encuestame.persistence.dao.ITweetPoll#getTotalTweetPoll(org.encuestame.persistence.domain.security.UserAccount, java.lang.Boolean)
     */
    public final Long getTotalTweetPoll(final UserAccount user,
            final Boolean publishTweetPoll) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPoll.class);
        criteria.setProjection(Projections.rowCount());
        criteria.add(Restrictions.eq("editorOwner", user));
        criteria.add(Restrictions.eq("publishTweetPoll", publishTweetPoll));
        @SuppressWarnings("unchecked")
        List<Long> results = getHibernateTemplate().findByCriteria(criteria);
        log.trace("Retrieve total tweetPolls by  " + user.getUsername()
                + "--->" + results.size());
        return (Long) (results.get(0) == null ? 0 : results.get(0));
    }

    @SuppressWarnings("unchecked")
    public Long getSocialLinksByType(final TweetPoll tweetPoll,
            final Survey survey, final Poll poll,
            final TypeSearchResult itemType) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPollSavedPublishedStatus.class);
        criteria.setProjection(Projections.rowCount());
        if (itemType.equals(TypeSearchResult.TWEETPOLL)) {
            criteria.add(Restrictions.eq("tweetPoll", tweetPoll));
        } else if (itemType.equals(TypeSearchResult.SURVEY)) {
            criteria.createAlias("survey", "survey");
            criteria.add(Restrictions.eq("survey", survey));
            // criteria.addOrder(Order.desc("survey.createdAt"));
        } else if (itemType.equals(TypeSearchResult.POLL)) {
            criteria.add(Restrictions.eq("poll", poll));
            // criteria.addOrder(Order.desc("poll.createdAt"));
        } else {
            log.error("Item type not valid: " + itemType);
        }
        criteria.add(Restrictions.isNotNull("apiType"));
        criteria.add(Restrictions.isNotNull("tweetId"));
        criteria.add(Restrictions.eq("status", Status.SUCCESS));

        List<Long> results = getHibernateTemplate().findByCriteria(criteria);
        log.debug("Retrieve total Social Links:" + "--->" + results.size());
        return (Long) (results.get(0) == null ? 0 : results.get(0));

    }

    /*
     * (non-Javadoc)
     * @see org.encuestame.persistence.dao.ITweetPoll#getSocialLinksByTypeAndDateRange(org.encuestame.persistence.domain.tweetpoll.TweetPoll, org.encuestame.persistence.domain.survey.Survey, org.encuestame.persistence.domain.survey.Poll, org.encuestame.utils.enums.TypeSearchResult, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    @SuppressWarnings("unchecked")
    public List<TweetPollSavedPublishedStatus> getSocialLinksByTypeAndDateRange(final TweetPoll tweetPoll,
            final Survey survey, final Poll poll,
            final TypeSearchResult itemType) {

        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPollSavedPublishedStatus.class);
        if (itemType.equals(TypeSearchResult.TWEETPOLL)) {
            criteria.createAlias("tweetPoll", "tweetPoll");
            criteria.add(Restrictions.eq("tweetPoll", tweetPoll));

        } else if (itemType.equals(TypeSearchResult.SURVEY)) {
            criteria.createAlias("survey", "survey");
            criteria.add(Restrictions.eq("survey", survey));

        } else if (itemType.equals(TypeSearchResult.POLL)) {
            criteria.add(Restrictions.eq("poll", poll));

        } else {
            log.error("Item type not valid: " + itemType);
        }

        criteria.add(Restrictions.isNotNull("apiType"));
        criteria.add(Restrictions.isNotNull("tweetId"));
        criteria.add(Restrictions.eq("status", Status.SUCCESS));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /*
     * (non-Javadoc)
     * @see org.encuestame.persistence.dao.ITweetPoll#getTotalVotesByTweetPollIdAndDateRange(java.lang.Long, java.lang.Integer)
     */
    public Long getTotalVotesByTweetPollIdAndDateRange(final Long tweetPollId, final String period) {
        Long totalvotes = 0L;
        final TweetPoll tpoll = this.getTweetPollById(tweetPollId);
        final List<TweetPollSwitch> tpSwitchAnswers = this
                .getListAnswersByTweetPollAndDateRange(tpoll);
        final SearchPeriods searchPeriods = SearchPeriods.getPeriodString(period);
        for (TweetPollSwitch tweetPollSwitch : tpSwitchAnswers) {
            totalvotes += this.getTotalTweetPollResultByTweetPollSwitch(tweetPollSwitch, searchPeriods);
            log.info("Total Votes: " + totalvotes);
        }
        return totalvotes;
    }

    /*
     * (non-Javadoc)
     * @see org.encuestame.persistence.dao.ITweetPoll#getTotalTweetPollResultByTweetPollSwitch(org.encuestame.persistence.domain.tweetpoll.TweetPollSwitch)
     */
    public final Long getTotalTweetPollResultByTweetPollSwitch(
            final TweetPollSwitch pollSwitch,
            final SearchPeriods period) {

        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPollResult.class);
        criteria.setProjection(Projections.rowCount());
        criteria.add(Restrictions.eq("tweetPollSwitch", pollSwitch));
        calculateSearchPeriodsDates(period, criteria, "tweetResponseDate");
        @SuppressWarnings("unchecked")
        List<Long> results = getHibernateTemplate().findByCriteria(criteria);
        log.debug("Retrieve total tweetPolls by  " + pollSwitch.getAnswers().getAnswer()
                + "--->" + results.size());
        return (Long) (results.get(0) == null ? 0 : results.get(0));
    }

    /*
     * (non-Javadoc)
     * @see org.encuestame.persistence.dao.ITweetPoll#getListAnswersByTweetPollAndDateRange(org.encuestame.persistence.domain.tweetpoll.TweetPoll, java.lang.Integer)
     */
    @SuppressWarnings("unchecked")
    public List<TweetPollSwitch> getListAnswersByTweetPollAndDateRange(
            final TweetPoll tweetPoll) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPollSwitch.class);
        criteria.createAlias("tweetPoll","tweetPoll");
        criteria.add(Restrictions.eq("tweetPoll", tweetPoll));
        return getHibernateTemplate().findByCriteria(criteria);
    }


    /*
     * (non-Javadoc)
     * @see org.encuestame.persistence.dao.ITweetPoll#getTweetPollResultsByTweetPollSwitch(org.encuestame.persistence.domain.tweetpoll.TweetPollSwitch)
     */
    @SuppressWarnings("unchecked")
    public final List<TweetPollResult> getTweetPollResultsByTweetPollSwitch(final TweetPollSwitch pollSwitch) {
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPollResult.class);
        criteria.add(Restrictions.eq("tweetPollSwitch", pollSwitch));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.encuestame.persistence.dao.ITweetPoll#
     * retrieveTweetPollsBySearchRadiusOfGeoLocation(double, double, double,
     * double, int, org.encuestame.utils.enums.TypeSearchResult,
     * org.encuestame.utils.enums.SearchPeriods)
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> retrieveTweetPollsBySearchRadiusOfGeoLocation(
            final double latitude, final double longitude,
            final double distance, final double radius, final int maxItems,
            final TypeSearchResult type, final SearchPeriods period) {
        String queryStr = "";
        final DateTime startDate= this.calculateSearchPeriodForGeo(period);
        final DateTime endDate = new DateTime();
        if (type.equals(TypeSearchResult.TWEETPOLL)) {
            queryStr = this.getQueryStringForGeoLocation("tweetPollId", "locationLatitude", "locationLongitude", "question.question",
                    "TweetPoll", "createDate");
        } else if (type.equals(TypeSearchResult.POLL)) {
            queryStr = this.getQueryStringForGeoLocation("pollId", "locationLatitude", "locationLongitude", "question.question", "Poll",  "createdAt");
        } else if (type.equals(TypeSearchResult.SURVEY)) {
            queryStr = this.getQueryStringForGeoLocation("sid", "locationLatitude", "locationLongitude", "name", "Survey",  "createdAt");
        } else if (type.equals(TypeSearchResult.HASHTAG)) {
            // TODO: Define how to should store geolocations for hashtags, maybe
            // in tweetpoll_hashtags
        }
        else {
            log.error("Item type not valid: " + type);
        }
         //calculateSearchPeriodsDates(period, criteria, "tweetResponseDate");
        getHibernateTemplate().setMaxResults(maxItems);
        return this.findByNamedParamGeoLocationItems(queryStr, latitude,
                longitude, distance, radius, maxItems, startDate.toDate(), endDate.toDate());
    }

    // NOTE: Missing search by Social Links and text
    @SuppressWarnings("unchecked")
    public List<TweetPoll> advancedSearch(final Boolean isPublished,
            final Boolean isComplete, final Boolean favourites,
            final Boolean scheduled, final Account user, final Integer start,
            final Integer max, final Integer period, final String keyword) {
        final SearchPeriods searchPeriods = SearchPeriods
                .getPeriodString(period.toString());
        final DetachedCriteria criteria = DetachedCriteria
                .forClass(TweetPoll.class);
        criteria.createAlias("question", "question");
        criteria.add(Restrictions.like("question.question", keyword,
                MatchMode.ANYWHERE));

        criteria.add(Restrictions.eq("completed", isComplete));
        criteria.add(Restrictions.eq("scheduleTweetPoll", scheduled));
        criteria.add(Restrictions.eq("publishTweetPoll", isPublished));
        criteria.add(Restrictions.eq("favourites", favourites));
        criteria.add(Restrictions.eq("tweetOwner", user));
        calculateSearchPeriodsDates(searchPeriods, criteria, "createDate");
        return getHibernateTemplate().findByCriteria(criteria);
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.encuestame.persistence.dao.ITweetPoll#getTweetPollsRangeStats(java
	 * .lang.String, org.encuestame.utils.enums.SearchPeriods)
	 */
    @SuppressWarnings("unchecked")
    public List<Object[]> getTweetPollsRangeStats(
            final String tagName,
            final SearchPeriods period) {
        final DetachedCriteria detached = DetachedCriteria
                .forClass(TweetPoll.class)
                 .createAlias("hashTags", "hashTags")
                .setProjection(Projections.id())
                .add(Subqueries.propertyIn(
                        "hashTags.hashTagId",
                        DetachedCriteria
                                .forClass(HashTag.class, "hash")
                                .setProjection(Projections.id())
                                .add(Restrictions.in("hash.hashTag",
                                        new String[] { tagName }))));
        final DetachedCriteria criteria = DetachedCriteria.forClass(
                TweetPoll.class, "tweetPoll");
        criteria.add(Subqueries.propertyIn("tweetPoll.tweetPollId", detached));

        criteria.addOrder(Order.desc("tweetPoll.createDate"));
        criteria.add(Restrictions.eq("publishTweetPoll", Boolean.TRUE));
        ProjectionList projList = Projections.projectionList();
         projList.add(Projections.groupProperty("createDate"));
       //  projList.add(Projections.sqlGroupProjection("DATE({alias}.create_date) as fecha", "fecha", new String[] { "fecha" }, new Type[] { StandardBasicTypes.DATE }));

        projList.add(Projections.rowCount());
        criteria.setProjection(projList);
        //projectionList.add(Projections.sqlGroupProjection("date(dateCreated) as createdDate", "createdDate", new String[] { "createdDate" }, new Type[] { StandardBasicTypes.DATE }));
       // calculateSearchPeriodsDates(period, criteria, "createDate");

        return getHibernateTemplate().findByCriteria(criteria);
    }
}
