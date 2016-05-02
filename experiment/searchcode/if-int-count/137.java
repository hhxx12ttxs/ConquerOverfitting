/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/
package org.ala.spatial.services.dao;

import com.sun.media.sound.AlawCodec;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.sql.DataSource;

import org.ala.spatial.services.dto.Action;
import org.ala.spatial.services.dto.Breakdown;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.extra.spath.SPathFilter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.ala.spatial.services.dto.Service;
import org.ala.spatial.services.dto.Session;

/**
 * @author ajay
 */
@org.springframework.stereotype.Service("actionDao")
public class ActionDAOImpl implements ActionDAO {

    private static final Logger logger = Logger.getLogger(ActionDAOImpl.class);
    private SimpleJdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert insertAction;
    private SimpleJdbcInsert insertService;

    @Resource(name = "dataSource")
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
        this.insertAction = new SimpleJdbcInsert(dataSource).withTableName("actions").usingGeneratedKeyColumns("id");
        this.insertService = new SimpleJdbcInsert(dataSource).withTableName("services").usingGeneratedKeyColumns("id");
    }

    @Override
    public void addAction(Action action) {
        logger.info("Adding a new " + action.getType() + " action by " + action.getEmail() + " via " + action.getAppid());
        logger.info("\nlayers: " + action.getService().getLayers() + "\nextra: " + action.getService().getExtra());
        action.setTime(new Timestamp(new Date().getTime()));

        SqlParameterSource sprm = new BeanPropertySqlParameterSource(action.getService());
        Number serviceId = insertService.executeAndReturnKey(sprm);
        action.getService().setId(serviceId.longValue());
        action.setServiceid(serviceId.longValue());

        SqlParameterSource parameters = new BeanPropertySqlParameterSource(action);
        Number actionId = insertAction.executeAndReturnKey(parameters);
        action.setId(actionId.longValue());

    }

    @Override
    public void updateActionStatus(String pid, String status) {
        Service s = getServiceByProcessid(pid);
        if (s != null) {
            String sql = "UPDATE services SET status = ? WHERE processid = ?";
            Map<String, String> args = new HashMap<String, String>();
            args.put("status", status);
            args.put("processid", pid);
            int ret = jdbcTemplate.update(sql, args);
            System.out.println("UPDATE status: " + ret);
        }
    }

    @Override
    public List<Action> getActions() {
        logger.info("Getting a list of all actions");
        String sql = "select id, email, userip, time, type, sessionid, category1, category2, name, layers, specieslsid from actionservices";
        return jdbcTemplate.query(sql, new ActionServiceMapper());
    }

    @Override
    public List<Action> getActionsByPage(int start, int count) {
        logger.info("Getting a pagged list of actions");
        String sql = "select id, email, userip, time, type, sessionid, category1, category2, name, layers, specieslsid " +
                "from actionservices order by time desc LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new ActionServiceMapper(), count, start);
    }

    //    @Override
//    public Action getActionById(long id) {
//        logger.info("Getting user action info for id = " + id);
//        String sql = "select * from actions where id = ?";
//        Action action = jdbcTemplate.queryForObject(sql, new ActionMapper(), id);
//        if (action != null) {
//            action.setService(getService(action.getServiceid()));
//            return null;
//        }
//
//        return action;
//    }
    @Override
    public Action getActionById(long id) {
        logger.info("Getting user action info for id = " + id);
        String sql = "select * from actionservices where id=?";
        List<Action> l = jdbcTemplate.query(sql, new ActionServiceFullMapper(), id);
        if (l.size() > 0) {
            return l.get(0);
        } else {
            return null;
        }
    }

    private Service getService(long serviceid) {
        logger.info("Getting service action info for serviceid = " + serviceid);
        String sql = "select * from services where id = ?";
        return jdbcTemplate.queryForObject(sql, new ServiceMapper(), serviceid);
    }

    private Service getServiceByProcessid(String processid) {
        logger.info("Getting service action info for processid = " + processid);
        String sql = "select * from services where processid = ?";
        return jdbcTemplate.queryForObject(sql, new ServiceMapper(), processid);
    }

    @Override
    public Action getActionByService(long serviceid) {
        logger.info("Getting user action info for service id = " + serviceid);
        String sql = "select * from actions where serviceid = ?";
        List<Action> l = jdbcTemplate.query(sql, new ActionMapper(), serviceid);
        if (l.size() > 0) {
            Action action = l.get(0);
            if (action != null) {
                action.setService(getService(action.getServiceid()));
            }
            return action;
        } else {
            return null;
        }
    }

    @Override
    public List<Action> getActionsByType(String type) {
        logger.info("Getting a list of all actions for a type");
        String sql = "select * from actions where type = ?";
        return jdbcTemplate.query(sql, new ActionMapper(), type);
    }

    @Override
    public List<Action> getActionsByAppId(String appid) {
        logger.info("Getting a list of all actions for a appid");
        String sql = "select * from actions where appid = ?";
        return jdbcTemplate.query(sql, new ActionMapper(), appid);
    }

    @Override
    public List<Action> getActionsByEmail(String email) {
//        logger.info("Getting a list of all actions for an email");
//        String sql = "select * from actions where email = ?";
//        return jdbcTemplate.query(sql, new ActionMapper(), email);
        logger.info("Getting a list of all actions for an email");
        String sql = "select * from actionservices where email = ? order by time desc;";
        return jdbcTemplate.query(sql, new ActionServiceMapper(), email.toLowerCase());
    }

    @Override
    public List<Action> getActionsBySessionId(String sessionid) {
        logger.info("Getting a list of all actions for a session");
        String sql = "select * from actionservices where sessionid = ?";
        return jdbcTemplate.query(sql, new ActionServiceMapper(), sessionid);
    }

    @Override
    public List<Action> getActionsByEmailAndCategory1(String email, String category1) {
//        logger.info("Getting a list of all actions for an email");
//        String sql = "select * from actions where email = ?";
//        return jdbcTemplate.query(sql, new ActionMapper(), email);
        logger.info("Getting a list of all actions for an email");
        String sql = "select * from actionservices where email = ? and lower(category1)=? order by time desc;";
        return jdbcTemplate.query(sql, new ActionServiceMapper(), email.toLowerCase(), category1.toLowerCase());
    }

    @Override
    public List<Breakdown> getActionBreakdownByType() {
        logger.info("Getting a breakdown of all actions grouped by type");
        String sql = "select type as label, count(*) from actions group by type;";
        return jdbcTemplate.query(sql, new BreakdownMapper());
    }

    @Override
    public List<Breakdown> getActionBreakdownByDay() {
        logger.info("Getting a breakdown of all actions grouped by day");
        String sql = "select cast(time as date) as label, count(*) from actions group by label;";
        return jdbcTemplate.query(sql, new BreakdownMapper());
    }

    @Override
    public List<Breakdown> getActionBreakdownByDayUser(String email) {
        logger.info("Getting a breakdown of all actions for " + email + " grouped by day");
        String sql = "select cast(time as date) as label, count(*) from actions where email = ? group by label;";
        return jdbcTemplate.query(sql, new BreakdownMapper(), email.toLowerCase());
    }

    @Override
    public List<Breakdown> getActionBreakdownBy(String breakdown, String by) {
        return getActionBreakdownByWithUser("", breakdown, by);
    }

    @Override
    public List<Breakdown> getActionBreakdownUserBy(String email, String breakdown, String by) {
        return getActionBreakdownByWithUser(email.toLowerCase(), breakdown, by);
    }

    private List<Breakdown> getActionBreakdownByWithUser(String email, String breakdown, String by) {
        List<Breakdown> bdList;
        String colName = breakdown.toLowerCase();
        if (colName.toLowerCase().equals("layers")) {
            colName = "regexp_split_to_table(layers, ':')";
        }
        String sql = "";
        if (email.trim().equals("")) {
            if (colName.equals("category1") && !by.trim().equals("")) {
                logger.info("Getting a breakdown of all actions grouped by " + breakdown + " = " + by);
                sql = "select category2 as label, count(*) from actionservices where category1 = ? group by label;";
                bdList = jdbcTemplate.query(sql, new BreakdownMapper(), by);
            } else {
                logger.info("Getting a breakdown of all actions grouped by " + breakdown);
                sql = "select " + colName + " as label, count(*) from actionservices group by label;";
                bdList = jdbcTemplate.query(sql, new BreakdownMapper());
            }
        } else {
            if (colName.equals("category1") && !by.trim().equals("")) {
                logger.info("Getting a breakdown of all actions for " + email + " grouped by " + breakdown + " = " + by);
                sql = "select category2 as label, count(*) from actionservices where category1 = ? and email = ? group by label;";
                bdList = jdbcTemplate.query(sql, new BreakdownMapper(), by, email.toLowerCase());
            } else {
                logger.info("Getting a breakdown of all actions " + email + " grouped by " + breakdown);
                sql = "select " + colName + " as label, count(*) from actionservices where email = ? group by label;";
                bdList = jdbcTemplate.query(sql, new BreakdownMapper(), email.toLowerCase());
            }
        }
        return bdList;
    }

    @Override
    public List<Session> getActionsBySessions() {
        logger.info("Getting sessions lists");
        String sql = "select sessionid, array_to_string(array_agg(category1),',') as tasks, (EXTRACT(EPOCH FROM age(max(time),min(time)) ))::Integer AS totaltime, min(time) as starttime, max(time) as endtime, email, userip from actionservices group by sessionid, email, userip order by starttime desc;";
        return jdbcTemplate.query(sql, new SessionMapper());
    }

    @Override
    public List<Session> getActionsBySessionsPage(int start, int count) {
        logger.info("Getting pagged sessions lists");
        String sql = "select sessionid, array_to_string(array_agg(category1),',') as tasks, (EXTRACT(EPOCH FROM age(max(time),min(time)) ))::Integer AS totaltime, min(time) as starttime, max(time) as endtime, email, userip from actionservices group by sessionid, email, userip order by starttime desc LIMIT ? OFFSET ?;";
        return jdbcTemplate.query(sql, new SessionMapper(), count, start);
    }

    @Override
    public List<Session> getActionsBySessionsByUser(String email) {
        logger.info("Getting sessions lists");
        String sql = "select sessionid, array_to_string(array_agg(category1),',') as tasks, (EXTRACT(EPOCH FROM age(max(time),min(time)) ))::Integer AS totaltime, min(time) as starttime, max(time) as endtime, email, userip from actionservices where email=? group by sessionid, email, userip order by starttime desc;";
        return jdbcTemplate.query(sql, new SessionMapper(), email.toLowerCase());
    }

    private static final class ActionMapper implements RowMapper<Action> {

        public Action mapRow(ResultSet rs, int rowNum) throws SQLException {
            Action action = new Action();
            action.setAppid(rs.getString("appid"));
            action.setEmail(rs.getString("email"));
            action.setId(rs.getLong("id"));
            action.setServiceid(rs.getLong("serviceid"));
            action.setTime(rs.getTimestamp("time"));
            action.setType(rs.getString("type"));
            action.setUserip(rs.getString("userip"));
            action.setSessionid(rs.getString("sessionid"));
            action.setCategory1(rs.getString("category1"));
            action.setCategory2(rs.getString("category2"));
            return action;
        }
    }

    private static final class ServiceMapper implements RowMapper<Service> {

        public Service mapRow(ResultSet rs, int rowNum) throws SQLException {
            Service service = new Service();
            service.setArea(rs.getString("area"));
            service.setExtra(rs.getString("extra"));
            service.setId(rs.getLong("id"));
            service.setLayers(rs.getString("layers"));
            service.setName(rs.getString("name"));
            service.setPrivacy(rs.getBoolean("privacy"));
            service.setProcessid(rs.getLong("processid"));
            service.setSpecieslsid(rs.getString("specieslsid"));
            service.setStatus(rs.getString("status"));
            return service;
        }
    }

    private static final class ActionServiceMapper implements RowMapper<Action> {

        public Action mapRow(ResultSet rs, int rowNum) throws SQLException {
            Action action = new Action();
            //action.setAppid(rs.getString("appid"));
            action.setEmail(rs.getString("email"));
            action.setId(rs.getLong("id"));
            action.setTime(rs.getTimestamp("time"));
            action.setType(rs.getString("type"));
            action.setUserip(rs.getString("userip"));
            action.setSessionid(rs.getString("sessionid"));
            action.setCategory1(rs.getString("category1"));
            action.setCategory2(rs.getString("category2"));

            Service service = new Service();
//            //service.setArea(rs.getString("area"));
//            service.setArea("[currently available]");
            service.setLayers(rs.getString("layers"));
//            service.setExtra(rs.getString("extra"));
////            service.setId(rs.getLong("id"));
            service.setName(rs.getString("name"));
//            service.setPrivacy(rs.getBoolean("privacy"));
//            service.setProcessid(rs.getLong("processid"));
            service.setSpecieslsid(rs.getString("specieslsid"));
//            service.setStatus(rs.getString("status"));
//
            action.setService(service);
            return action;
        }
    }

    private static final class ActionServiceFullMapper implements RowMapper<Action> {

        public Action mapRow(ResultSet rs, int rowNum) throws SQLException {
            Action action = new Action();
            action.setAppid(rs.getString("appid"));
            action.setEmail(rs.getString("email"));
            action.setId(rs.getLong("id"));
            action.setTime(rs.getTimestamp("time"));
            action.setType(rs.getString("type"));
            action.setUserip(rs.getString("userip"));
            action.setSessionid(rs.getString("sessionid"));
            action.setCategory1(rs.getString("category1"));
            action.setCategory2(rs.getString("category2"));

            Service service = new Service();
            service.setArea(rs.getString("area"));
            service.setLayers(rs.getString("layers"));
            service.setExtra(rs.getString("extra"));
//            service.setId(rs.getLong("id"));
            service.setName(rs.getString("name"));
            service.setPrivacy(rs.getBoolean("privacy"));
            service.setProcessid(rs.getLong("processid"));
            service.setSpecieslsid(rs.getString("specieslsid"));
            service.setStatus(rs.getString("status"));

            action.setService(service);
            return action;
        }
    }

    private static final class BreakdownMapper implements RowMapper<Breakdown> {

        public Breakdown mapRow(ResultSet rs, int rowNum) throws SQLException {
            Breakdown breakdown = new Breakdown();
            breakdown.setLabel(rs.getString("label"));
            breakdown.setCount(rs.getInt("count"));
            return breakdown;
        }
    }

    private static final class SessionMapper implements RowMapper<Session> {

        public Session mapRow(ResultSet rs, int rowNum) throws SQLException {
            Session session = new Session();
            session.setUserip(rs.getString("userip"));
            session.setSessionid(rs.getString("sessionid"));
            session.setEmail(rs.getString("email"));
            session.setTasks(rs.getString("tasks"));
            session.setTotaltime(rs.getInt("totaltime"));
            session.setStartTime(rs.getTimestamp("starttime"));
            session.setEndTime(rs.getTimestamp("endtime"));
            return session;
        }
    }
}

