package org.grouplens.lenskit.bmaus.model;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.bmaus.data.TagGenome;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Builds a TagPreferenceModel from item ratings and tag genome data.
 *
 * @author Brandon Maus
 */
@NotThreadSafe
public class TagPreferenceModelProvider implements Provider<TagPreferenceModel> {

    private static final Logger logger = LoggerFactory.getLogger(TagPreferenceModelProvider.class);

    private TagGenome tagGenome;
    private DataAccessObject dao;

    @Inject
    public TagPreferenceModelProvider(TagGenome tagGenome,
                                      DataAccessObject dao) {
        this.tagGenome = tagGenome;
        this.dao = dao;
    }

    /**
     * Computes user preference profiles, returning them in
     * a TagPreferenceProfile.
     *
     * @return A TagPreferenceProfile giving user preferences
     *         to all tags in the tag genome.
     */
    public TagPreferenceModel get() {
        logger.info("Providing TagPreferenceModel");
        long startTime = System.currentTimeMillis();
        LongSet users = Cursors.makeSet(dao.getUsers());
        TagPreferenceModelAccumulator accumulator = new TagPreferenceModelAccumulator(tagGenome.getGenomeTagSet(), users);
        LongIterator tagIter = tagGenome.getGenomeTagSet().iterator();
        while (tagIter.hasNext()) {
            long tagId = tagIter.nextLong();
            LongIterator userIter = users.iterator();
            while (userIter.hasNext()) {
                long user = userIter.nextLong();
                double preference = 0.0;
                double totalRelevance = 0.0;
                UserHistory<Rating> ratings = dao.getUserHistory(user, Rating.class);
                for (Rating rating : ratings) {
                    if (tagGenome.checkItemInGenome(rating.getItemId())) {
                        double rel = tagGenome.getRelevance(rating.getItemId(), tagId);
                        totalRelevance += rel;
                        preference += rel * ((rating.getPreference().getValue() - 0.5) / 4.5);
                    }
                }
                preference = preference / totalRelevance;
                accumulator.put(user, tagId, preference);
            }
            accumulator.completeRow(tagId);
        }

        logger.info("took " + ((System.currentTimeMillis() - startTime) / 1000.0) + " seconds to build TagPrefModel");
        return accumulator.build();
    }

}

