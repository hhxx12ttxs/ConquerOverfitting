package com.drotposta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import org.joda.time.LocalDate;
import org.joda.time.Period;

/**
 *
 * @author elek
 */
@Stateless
public class LogService {

    @Resource(name = "jdbc/jira4")
    DataSource ds;

    public List<UserItem> getUsers() throws SQLException {
        List<UserItem> items = new ArrayList();
        Connection con = ds.getConnection();
        try {
            String query = "select distinct author as c from worklog order by author";
            PreparedStatement st = con.prepareStatement(query);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                items.add(new UserItem(rs.getString("c")));
            }
        } finally {
            con.close();
        }
        return items;

    }

    public double getHoursPerMonth(String user, int year, int month) throws SQLException {
        Connection con = ds.getConnection();
        try {
            List<IssueItem> issues = new ArrayList();
            String query =
                    "select "
                    + "  sum(timeworked/3600) as time "
                    + "from worklog JOIN jiraissue on jiraissue.id= worklog.issueid "
                    + "where author = ? "
                    + "AND to_char(trunc(startdate),'YYYYMM')=? ";
            System.out.println(query);
            PreparedStatement st = null;
            try {
                st = con.prepareStatement(query);
                st.setString(1, user);
                st.setString(2, String.format("%04d%02d", year, month));

                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("time");
                }


            } finally {
                if (st != null) {
                    st.close();
                }
            }
        } finally {
            con.close();
        }

        return 0;


    }

    public List<IssueItem> getIssuesPerMonth(String user, int year, int month) throws SQLException {
        Connection con = ds.getConnection();

        List<IssueItem> issues = new ArrayList();
        try {
            String query =
                    "select "
                    + "  pkey,"
                    + " jiraissue.summary as summary,"
                    + "  sum(timeworked/3600) as time "
                    + "from worklog JOIN jiraissue on jiraissue.id= worklog.issueid "
                    + "where author = ? "
                    + "AND to_char(trunc(startdate),'YYYYMM')=? "
                    + " group by pkey,summary "
                    + "order by pkey";
            System.out.println(query);
            PreparedStatement st = null;


            try {
                st = con.prepareStatement(query);
                st.setString(1, user);
                    st.setString(2, String.format("%04d%02d", year, month));

                ResultSet rs = st.executeQuery();



                while (rs.next()) {

                    issues.add(new IssueItem(rs.getString("pkey"), rs.getString("summary"), rs.getDouble("time")));



                }

            } finally {
                if (st != null) {
                    st.close();
                }
            }

        } finally {
            con.close();
        }
        return issues;

    }

    public List<DailyLogItem> getLogEveryDay(String user, int year, int month) throws SQLException {
        long dn = 60l * 60 * 24 * 1000;
        Map<LocalDate, DailyLogItem> items = new TreeMap<LocalDate, DailyLogItem>(new Comparator<LocalDate>() {

            @Override
            public int compare(LocalDate o1, LocalDate o2) {
                return o1.compareTo(o2) * -1;
            }
        });
        Connection con = ds.getConnection();
        try {
            String query =
                    "select "
                    + "  pkey,"
                    + " jiraissue.summary as summary,"
                    + "  timeworked/3600 as time, "
                    + "  trunc(startdate) as c "
                    + "from worklog JOIN jiraissue on jiraissue.id= worklog.issueid "
                    + "where author = ? "
                    + "AND to_char(trunc(startdate),'YYYYMM')=? "
                    + "order by c desc";
            System.out.println(query);
            if (user != null) {
                PreparedStatement st = null;
                try {
                    st = con.prepareStatement(query);
                    st.setString(1, user);
                    st.setString(2, String.format("%04d%02d", year, month));

                    ResultSet rs = st.executeQuery();

                    while (rs.next()) {
                        LocalDate date = new LocalDate(rs.getDate("c"));
                        DailyLogItem i = items.get(date);
                        if (i == null) {
                            i = new DailyLogItem(date);
                            items.put(date, i);
                        }
                        i.addIssue(new IssueItem(rs.getString("pkey"), rs.getString("summary"), rs.getDouble("time")));

                    }
                } finally {
                    if (st != null) {
                        st.close();
                    }
                }

            }
            LocalDate date = new LocalDate().withYear(year).withMonthOfYear(month).withDayOfMonth(1);

            while (date.getMonthOfYear() == month) {
                if (items.get(date) == null) {
                    items.put(date, new DailyLogItem(date));
                }
                date = date.plus(Period.days(1));
            }
        } finally {
            con.close();
        }
        return new ArrayList(items.values());


    }
    private final static long MIN_IN_SEC = 60;
    private final static long HOUR_IN_SEC = MIN_IN_SEC * 60;
    private final static long DAY_IN_SEC = HOUR_IN_SEC * 24;

    private String format(long duration) {
        long days = duration / DAY_IN_SEC;
        duration -= days * DAY_IN_SEC;
        long hours = duration / HOUR_IN_SEC;
        duration -= hours * HOUR_IN_SEC;
        long mins = duration / MIN_IN_SEC;
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d");
        }
        if (hours > 0) {
            sb.append(hours).append("h");
        }
        if (mins > 0) {
            sb.append(mins).append("m");
        }

        if (sb.length() == 0) {
            sb.append("0");
        }
        return sb.toString();
    }

    public List<Issue> getIssues(String user) throws SQLException {
        List<Issue> items = new ArrayList();
        Connection con = ds.getConnection();
        try {
            String recentQ = "select MAX(W.UPDATED) UPDATED, I.SUMMARY SUMMARY, I.TIMEORIGINALESTIMATE TIMEORIGINALESTIMATE, sum(w.timeworked) TIMESPENT, I.PKEY PKEY, S.PNAME ISSUESTATUS "
                    + "from WORKLOG W JOIN JIRAISSUE I ON W.ISSUEID = I.ID JOIN ISSUESTATUS S ON I.ISSUESTATUS = S.ID "
                    + "where AUTHOR = ? "
                    + "group by I.PKEY, I.TIMEORIGINALESTIMATE, I.SUMMARY, I.ISSUESTATUS, S.PNAME "
                    + "order by UPDATED desc";

            PreparedStatement st = con.prepareStatement(recentQ);
            st.setString(1, user);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Issue issue = new Issue();
                issue.setKey(rs.getString("PKEY"));
                issue.setSummary(rs.getString("SUMMARY"));
                long timeSpent = rs.getLong("TIMESPENT");
                long originalEstimate = rs.getLong("TIMEORIGINALESTIMATE");
                long timeLeft = originalEstimate - timeSpent;
                issue.setOriginal(format(originalEstimate));
                issue.setTimeLeft(format(timeLeft));
                issue.setTimeSpent(format(timeSpent));
                String status = rs.getString("ISSUESTATUS");
                issue.setStatus(status);

                LocalDate updated = new LocalDate(rs.getDate("UPDATED"));
                boolean recent  = updated.plusDays(30).isAfter(new LocalDate());
                if (timeLeft == 0 || "Closed".equals(issue.getStatus())) {
                    issue.setStyleClass("full");
                } else {
                    issue.setStyleClass("recent");
                }

                if (recent || (!"Closed".equals(issue.getStatus()))) {
                    items.add(issue);
                }
            }
            rs.close();
            st.close();

            String query = "select PKEY, SUMMARY, TIMEORIGINALESTIMATE, TIMEESTIMATE, TIMESPENT, PNAME ISSUESTATUS, UPDATED "
                    + "from JIRAISSUE join ISSUESTATUS on JIRAISSUE.ISSUESTATUS = ISSUESTATUS.ID "
                    + "where ASSIGNEE = ? and TIMESPENT is null "
                    + "order by UPDATED desc";
            st = con.prepareStatement(query);
            st.setString(1, user);
            rs = st.executeQuery();
            while (rs.next()) {
                Issue issue = new Issue();
                issue.setKey(rs.getString("PKEY"));
                issue.setSummary(rs.getString("SUMMARY"));
                issue.setOriginal(format(rs.getLong("TIMEORIGINALESTIMATE")));
                issue.setTimeLeft(format(rs.getLong("TIMEESTIMATE")));
                issue.setTimeSpent("0");
                issue.setStatus(rs.getString("ISSUESTATUS"));

                LocalDate updated = new LocalDate(rs.getDate("UPDATED"));
                boolean recent  = updated.plusDays(30).isAfter(new LocalDate());
                issue.setStyleClass("recent");

                if (recent || (!"Closed".equals(issue.getStatus()))) {
                    items.add(issue);
                }

                issue.setStyleClass("");
            }
        } finally {
            con.close();
        }
        return items;


    }
}

