/**
 * Copyright (c) 2009, 5AM Solutions, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * - Neither the name of the author nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.fiveamsolutions.tissuelocator.service;


import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.Test;

import com.fiveamsolutions.tissuelocator.data.Institution;
import com.fiveamsolutions.tissuelocator.data.RequestStatus;
import com.fiveamsolutions.tissuelocator.data.Specimen;
import com.fiveamsolutions.tissuelocator.data.SpecimenRequest;
import com.fiveamsolutions.tissuelocator.data.SpecimenRequestLineItem;
import com.fiveamsolutions.tissuelocator.data.SpecimenRequestReviewVote;
import com.fiveamsolutions.tissuelocator.data.Vote;
import com.fiveamsolutions.tissuelocator.util.Email;
import com.fiveamsolutions.tissuelocator.util.RequestProcessingConfiguration;

/**
 * @author smiller
 *
 */
public class SpecimenRequestVoteAnalyzerTest {

    private static final double PERCENT_MULTIPLIER = 100.0;
    private static final int REVIEW_PERIOD = 10;
    private static final int VOTING_PERIOD = 15;
    private static final int MIN_AVAILABLE = 60;
    private static final int MIN_WINNING = 65;
    private static final int NUM_CONSORTIUM_REVIEWERS = 5;
    private final SpecimenRequestVoteAnalyzer analyzer;

    /**
     * default constructor.
     */
    public SpecimenRequestVoteAnalyzerTest() {
        RequestProcessingConfiguration config = new RequestProcessingConfiguration();
        config.setReviewPeriod(REVIEW_PERIOD);
        config.setVotingPeriod(VOTING_PERIOD);
        config.setMinPercentAvailableVotes(MIN_AVAILABLE);
        config.setMinPercentWinningVotes(MIN_WINNING);
        config.setReviewLateEmail(getEmail("reviewLate"));
        config.setVoteLateEmail(getEmail("voteLate"));
        config.setDeadlockEmail(getEmail("deadlock"));
        config.setDeadlockInstAdminEmail(getEmail("dealockInstAdmin"));
        config.setVoteFinalizedEmail(getEmail("voteFinalized"));
        config.setVoteFinalizedAssignmentEmail(getEmail("voteFinalizedAssignment"));
        analyzer = new SpecimenRequestVoteAnalyzer(config);
    }

    private Email getEmail(String content) {
        Email email = new Email();
        email.setSubject(content + " {1} {2}");
        email.setHtml(content + " {1} {2}");
        email.setText(content + " {1} {2}");
        return email;
    }

    private SpecimenRequest getTestRequest() {
        SpecimenRequest request = new SpecimenRequest();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1 * VOTING_PERIOD - 1);
        request.setUpdatedDate(cal.getTime());

        for (int i = 0; i < NUM_CONSORTIUM_REVIEWERS; i++) {
            Institution inst = new Institution();
            inst.setName("testInst" + i);
            SpecimenRequestReviewVote vote = new SpecimenRequestReviewVote();
            vote.setInstitution(inst);
            vote.setVote(Vote.APPROVE);
            request.getConsortiumReviews().add(vote);

            if (i < 2) {
                vote = new SpecimenRequestReviewVote();
                vote.setInstitution(inst);
                vote.setVote(Vote.APPROVE);
                request.getInstitutionalReviews().add(vote);

                SpecimenRequestLineItem li = new SpecimenRequestLineItem();
                li.setSpecimen(new Specimen());
                li.getSpecimen().setExternalIdAssigner(inst);
                request.getLineItems().add(li);
            }
        }

        return request;
    }

    /**
     * test success.
     * @throws Exception on error.
     */
    @Test
    public void testApproval() throws Exception {
        assertEquals(Vote.APPROVE.getStatus(), analyzer.tabulateVotes(getTestRequest()));
    }

    /**
     * test denied due to all failed institutional reviews.
     * @throws Exception on error.
     */
    @Test
    public void testInstiutionalDenial() throws Exception {
        SpecimenRequest request = getTestRequest();
        for (SpecimenRequestReviewVote vote : request.getInstitutionalReviews()) {
            vote.setVote(Vote.DENY);
        }
        assertEquals(Vote.DENY.getStatus(), analyzer.tabulateVotes(request));
    }

    /**
     * test partial approval.
     * @throws Exception on error.
     */
    @Test
    public void testParitalApproval() throws Exception {
        SpecimenRequest request = getTestRequest();
        request.getInstitutionalReviews().iterator().next().setVote(Vote.DENY);
        assertEquals(RequestStatus.PARTIALLY_APPROVED, analyzer.tabulateVotes(request));
    }

    /**
     * test incomplete voting period.
     * @throws Exception on error.
     */
    @Test
    public void testIncompleteVotingPeriod() throws Exception {
        SpecimenRequest request = getTestRequest();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1 * VOTING_PERIOD + 1);
        request.setUpdatedDate(cal.getTime());
        assertEquals(null, analyzer.tabulateVotes(request));
    }

    /**
     * test incomplete scientific review votes.
     * @throws Exception on error
     */
    @Test
    public void testIncompleteScientificReview() throws Exception {
        SpecimenRequest request = getTestRequest();
        int i = 0;
        for (SpecimenRequestReviewVote vote : request.getConsortiumReviews()) {
            i++;
            vote.setVote(null);
            int numVotes = NUM_CONSORTIUM_REVIEWERS - i;
            double percentAvailableVotes = PERCENT_MULTIPLIER * (double) numVotes / (double) NUM_CONSORTIUM_REVIEWERS;
            try {
                RequestStatus status = analyzer.tabulateVotes(request);
                assertTrue(status != null && percentAvailableVotes >= MIN_AVAILABLE);
            } catch (NotEnoughVotesException e) {
                assertTrue(percentAvailableVotes < MIN_AVAILABLE || percentAvailableVotes < MIN_WINNING);
                assertFalse(e.isConsortiumReviewComplete());
                assertTrue(e.isInstitutionalReviewComplete());
                assertTrue(e.getEmail().getSubject().contains("voteLate"));
                assertTrue(e.getEmail().getHtml().contains("voteLate"));
                assertTrue(e.getEmail().getText().contains("voteLate"));
            }
        }
    }
    
    /**
     * Test that the minimum number of allowed votes and minimum number of
     * approval votes result in approved status.
     * @throws Exception on error
     */
    @Test
    public void testIncompleteValidScientificReview() throws Exception {
        SpecimenRequest request = getTestRequest();
        int minVotesAvailable = (int) Math.ceil((double) NUM_CONSORTIUM_REVIEWERS 
                * ((double) MIN_AVAILABLE / PERCENT_MULTIPLIER));
        int minVotesApprove = (int) Math.ceil((double) minVotesAvailable * ((double) MIN_WINNING / PERCENT_MULTIPLIER));
        
        int i = 0;
        for (SpecimenRequestReviewVote vote : request.getConsortiumReviews()) {
            if (i < NUM_CONSORTIUM_REVIEWERS - minVotesAvailable) {
                vote.setVote(null);
                RequestStatus status = analyzer.tabulateVotes(request);
                assertTrue(status.equals(RequestStatus.APPROVED));
            } else if (i < NUM_CONSORTIUM_REVIEWERS - minVotesApprove) {
                vote.setVote(Vote.DENY);
                RequestStatus status = analyzer.tabulateVotes(request);
                assertTrue(status.equals(RequestStatus.APPROVED));
            } else {
                vote.setVote(Vote.DENY);
                RequestStatus status = analyzer.tabulateVotes(request);
                assertFalse(status.equals(RequestStatus.APPROVED));
            }
            i++;
        }           
    }

    /**
     * test incomplete institutional review votes.
     * @throws Exception on error.
     */
    @Test
    public void testIncompleteInstReview() throws Exception {
        SpecimenRequest request = getTestRequest();
        request.getInstitutionalReviews().iterator().next().setVote(null);
        try {
            analyzer.tabulateVotes(request);
            fail("exception expected");
        } catch (NotEnoughVotesException e) {
            assertTrue(e.isConsortiumReviewComplete());
            assertFalse(e.isInstitutionalReviewComplete());
            assertTrue(e.getEmail().getSubject().contains("voteLate"));
            assertTrue(e.getEmail().getHtml().contains("voteLate"));
            assertTrue(e.getEmail().getText().contains("voteLate"));
        }
    }

    /**
     * test incomplete institutional review votes.
     * @throws Exception on error.
     */
    @Test
    public void testIncompleteInstReview2() throws Exception {
        SpecimenRequest request = getTestRequest();
        request.getInstitutionalReviews().clear();
        try {
            analyzer.tabulateVotes(request);
            fail("exception expected");
        } catch (NotEnoughVotesException e) {
            assertTrue(e.isConsortiumReviewComplete());
            assertFalse(e.isInstitutionalReviewComplete());
            assertTrue(e.getEmail().getSubject().contains("voteLate"));
            assertTrue(e.getEmail().getHtml().contains("voteLate"));
            assertTrue(e.getEmail().getText().contains("voteLate"));
        }
    }

    /**
     * test denied scientific review votes.
     * @throws Exception on error.
     */
    @Test
    public void testDeniedScientificReview() throws Exception {
        SpecimenRequest request = getTestRequest();
        int i = 0;
        for (SpecimenRequestReviewVote vote : request.getConsortiumReviews()) {
            i++;
            vote.setVote(Vote.DENY);
            int numApprovals = NUM_CONSORTIUM_REVIEWERS - i;
            double percentApprovalVotes = PERCENT_MULTIPLIER
                    * (double) numApprovals / (double) NUM_CONSORTIUM_REVIEWERS;

            try {
                RequestStatus status = analyzer.tabulateVotes(request);
                if (percentApprovalVotes >= MIN_WINNING) {
                    assertEquals(Vote.APPROVE.getStatus(), status);
                } else if ((PERCENT_MULTIPLIER - percentApprovalVotes) >= MIN_WINNING) {
                    assertEquals(Vote.DENY.getStatus(), status);
                } else {
                    fail("should get an exception.");
                }
            } catch (NotEnoughVotesException e) {
                assertTrue((PERCENT_MULTIPLIER - percentApprovalVotes) < MIN_WINNING);
                assertTrue(percentApprovalVotes < MIN_WINNING);
                assertFalse(e.isConsortiumReviewComplete());
                assertTrue(e.isInstitutionalReviewComplete());
                assertTrue(e.isVoteDeadLocked());
                assertTrue(e.getEmail().getSubject().contains("deadlock"));
                assertTrue(e.getEmail().getHtml().contains("deadlock"));
                assertTrue(e.getEmail().getText().contains("deadlock"));
            }
        }
    }
}

