/*

ATHENA Project: Management Tools for the Cultural Sector
Copyright (C) 2010, Fractured Atlas

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/

 */
package org.fracturedatlas.athena.reports.manager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.fracturedatlas.athena.client.AthenaComponent;
import org.fracturedatlas.athena.client.PTicket;
import org.fracturedatlas.athena.exception.AthenaException;
import org.fracturedatlas.athena.reports.model.AthenaReport;
import org.fracturedatlas.athena.reports.model.GlanceEventReport;
import org.fracturedatlas.athena.reports.model.GlancePerformanceReport;
import org.fracturedatlas.athena.reports.model.GrossComped;
import org.fracturedatlas.athena.reports.model.GrossNet;
import org.fracturedatlas.athena.search.AthenaSearch;
import org.fracturedatlas.athena.search.Operator;
import org.fracturedatlas.athena.util.date.DateUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GlanceReporter implements Reporter {

    @Autowired
    AthenaComponent athenaStage;

    @Autowired
    AthenaComponent athenaTix;

    Logger logger = LoggerFactory.getLogger(this.getClass().getName());


    @Override
    public AthenaReport getReport(Map<String, List<String>> queryParameters) {

        AthenaReport report;

        if(isEmpty(queryParameters)) {
            throw new AthenaException("Please specify either eventId or performanceId");
        } else if (isEventQuery(queryParameters)) {
            String eventId = queryParameters.get("eventId").get(0);
            String organizationId = queryParameters.get("organizationId").get(0);
            report = loadGlanceEventReport(eventId, organizationId);
        } else if (isPerformanceQuery(queryParameters)) {
            String performanceId = queryParameters.get("performanceId").get(0);
            String organizationId = queryParameters.get("organizationId").get(0);
            report = loadGlancePerformanceReport(performanceId, organizationId);
        } else {
            throw new AthenaException("Please specify either eventId or performanceId");
        }
        
        return report;
    }

    public Boolean isEventQuery(Map<String, List<String>> queryParameters) {
        return queryParameters.get("eventId") != null;
    }

    public Boolean isPerformanceQuery(Map<String, List<String>> queryParameters) {
        return queryParameters.get("performanceId") != null;
    }

    private Boolean soldToday(DateTime now, PTicket ticket) {
        try{
            DateTime soldAt = DateUtil.parseDateTime(ticket.get("soldAt"));
            return soldAt.isAfter(now.toDateMidnight());
        } catch (Exception e) {
            logger.error("soldAt of ticket [{}] was either null or in incorrect format", ticket.getId());
            logger.error("{}", e.getMessage());
        }

        return false;
    }

    private Boolean alreadyPlayed(DateTime now, PTicket ticket) {
        try{
            DateTime performanceTime = DateUtil.parseDateTime(ticket.get("performance"));
            return performanceTime.isBefore(now);
        } catch (Exception e) {
            logger.error("performance time of ticket [{}] was either null or in incorrect format", ticket.getId());
            logger.error("{}", e.getMessage());
        }

        return false;
    }

    public GlancePerformanceReport loadGlancePerformanceReport(String performanceId, String organizationId) {
        long start = System.currentTimeMillis();

        GlancePerformanceReport report = new GlancePerformanceReport();

        AthenaSearch search = new AthenaSearch.Builder()
                                              .type("ticket")
                                              .and("performanceId", Operator.EQUALS, performanceId)
                                              .and("organizationId", Operator.EQUALS, organizationId)
                                              .build();
        Double originalPotential = 0D;
        Double totalSales = 0D;
        Double totalSalesToday = 0D;
        Double potentialRemaining = 0D;
        Integer totalTicketsSold = 0;
        Integer totalTicketsSoldToday = 0;
        Integer totalTicketsComped = 0;
        Integer totalTicketsCompedToday = 0;
        Integer totalTicketsAvailable = 0;

      
        logger.debug("Searching for tickets matching {}", search);
        long searchStart = System.currentTimeMillis();
        Collection<PTicket> tickets = athenaTix.find("ticket", search);
        long searchEnd = System.currentTimeMillis();
        logger.debug("Search took [{}ms]", searchEnd-searchStart);
        logger.debug("Found {} tickets", tickets.size());
        DateTime now = new DateTime();
        
        long calStart = System.currentTimeMillis();
        
        for(PTicket ticket : tickets) {
            originalPotential += Double.parseDouble(ticket.get("price"));
            if("sold".equals(ticket.get("state"))) {
                totalSales += Double.parseDouble(ticket.get("soldPrice"));
                totalTicketsSold++;

                if(soldToday(now, ticket)) {
                    totalTicketsSoldToday++;
                    totalSalesToday += Double.parseDouble(ticket.get("soldPrice"));
                }
            } else if ("comped".equals(ticket.get("state"))) {
                totalTicketsSold++;
                totalTicketsComped++;

                if(soldToday(now, ticket)) {
                    totalTicketsSoldToday++;
                    totalTicketsCompedToday++;
                }
            } else if ("on_sale".equals(ticket.get("state"))) {
                totalTicketsAvailable++;
                potentialRemaining += Double.parseDouble(ticket.get("price"));
            } else if ("off_sale".equals(ticket.get("state"))) {
                potentialRemaining += Double.parseDouble(ticket.get("price"));
            }
        }
        
        long calEnd = System.currentTimeMillis();
        logger.debug("Calculations took [{}ms]", calEnd-calStart);

        report.getRevenue().setSoldToday(new GrossNet(totalSalesToday, 0D));
        report.getRevenue().setPotentialRemaining(new GrossNet(potentialRemaining, 0D));
        report.getRevenue().setOriginalPotential(new GrossNet(originalPotential, 0D));
        report.getRevenue().setTotalSales(new GrossNet(totalSales, 0D));

        report.getTickets().setSold(new GrossComped(totalTicketsSold, totalTicketsComped));
        report.getTickets().setSoldToday(new GrossComped(totalTicketsSoldToday, totalTicketsCompedToday));
        report.getTickets().setAvailable(totalTicketsAvailable);

        long end = System.currentTimeMillis();
        logger.debug("Glance report loaded in [{}ms]", end-start);
        return report;
    }
    
    public GlanceEventReport loadGlanceEventReport(String eventId, String organizationId) {

        GlanceEventReport report = new GlanceEventReport();

        AthenaSearch performancesSearch = new AthenaSearch.Builder()
                                              .type("performance")
                                              .and("eventId", Operator.EQUALS, eventId)
                                              .and("state", Operator.EQUALS, "on_sale")
                                              .and("organizationId", Operator.EQUALS, organizationId)
                                              .build();

        Collection<PTicket> performances = athenaStage.find("performance", performancesSearch);
        report.setPerformancesOnSale(performances.size());

        AthenaSearch search = new AthenaSearch.Builder()
                                              .type("ticket")
                                              .and("eventId", Operator.EQUALS, eventId)
                                              .and("organizationId", Operator.EQUALS, organizationId)
                                              .build();

        /*
         * HACK: TODO: This is nuts to do this twice.  Haven't had time to refactor it out.
         */

        Double originalPotential = 0D;
        Double totalSales = 0D;
        Double totalSalesToday = 0D;
        Double totalAdvanceSales = 0D;
        Double totalPlayed = 0D;
        Double potentialRemaining = 0D;
        Integer totalTicketsSold = 0;
        Integer totalTicketsSoldToday = 0;
        Integer totalTicketsComped = 0;
        Integer totalTicketsCompedToday = 0;
        Integer totalTicketsAvailable = 0;
        Integer totalTicketsPlayed = 0;
        Integer totalTicketsCompedAndPlayed = 0;

        logger.debug("Searching for tickets matching {}", search);
        Collection<PTicket> tickets = athenaTix.find("ticket", search);
        logger.debug("Found {} tickets", tickets.size());
        DateTime now = new DateTime();
        for(PTicket ticket : tickets) {
            originalPotential += Double.parseDouble(ticket.get("price"));
            if("sold".equals(ticket.get("state"))) {
                totalSales += Double.parseDouble(ticket.get("soldPrice"));
                totalTicketsSold++;

                if(soldToday(now, ticket)) {
                    totalTicketsSoldToday++;
                    totalSalesToday += Double.parseDouble(ticket.get("soldPrice"));
                }

                if(alreadyPlayed(now, ticket)) {
                    totalPlayed += Double.parseDouble(ticket.get("soldPrice"));
                    totalTicketsPlayed++;
                } else {
                    totalAdvanceSales += Double.parseDouble(ticket.get("soldPrice"));
                }
            } else if ("comped".equals(ticket.get("state"))) {
                totalTicketsSold++;
                totalTicketsComped++;

                if(soldToday(now, ticket)) {
                    totalTicketsSoldToday++;
                    totalTicketsCompedToday++;
                }

                if(alreadyPlayed(now, ticket)) {
                    totalTicketsCompedAndPlayed++;
                }
            } else if ("on_sale".equals(ticket.get("state"))) {
                totalTicketsAvailable++;
                potentialRemaining += Double.parseDouble(ticket.get("price"));
            } else if ("off_sale".equals(ticket.get("state"))) {
                potentialRemaining += Double.parseDouble(ticket.get("price"));
            }
        }


        report.getRevenue().setAdvanceSales(new GrossNet(totalAdvanceSales, 0D));
        report.getRevenue().setSoldToday(new GrossNet(totalSalesToday, 0D));
        report.getRevenue().setPotentialRemaining(new GrossNet(potentialRemaining, 0D));
        report.getRevenue().setOriginalPotential(new GrossNet(originalPotential, 0D));
        report.getRevenue().setTotalSales(new GrossNet(totalSales, 0D));
        report.getRevenue().setTotalPlayed(new GrossNet(totalPlayed, 0D));

        report.getTickets().setSold(new GrossComped(totalTicketsSold, totalTicketsComped));
        report.getTickets().setSoldToday(new GrossComped(totalTicketsSoldToday, totalTicketsCompedToday));
        report.getTickets().setPlayed(new GrossComped(totalTicketsPlayed, totalTicketsCompedAndPlayed));
        report.getTickets().setAvailable(totalTicketsAvailable);
        
        return report;
    }

    private boolean isEmpty(Map map) {
        return (map != null) && (map.keySet().size() == 0);
    }

    public AthenaComponent getAthenaStage() {
        return athenaStage;
    }

    public void setAthenaStage(AthenaComponent athenaStage) {
        this.athenaStage = athenaStage;
    }

    public AthenaComponent getAthenaTix() {
        return athenaTix;
    }

    public void setAthenaTix(AthenaComponent athenaTix) {
        this.athenaTix = athenaTix;
    }
}

