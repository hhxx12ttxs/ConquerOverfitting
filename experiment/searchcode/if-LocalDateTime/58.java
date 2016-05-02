package by.q64.promo.dao;

import by.q64.promo.data.Action;
import by.q64.promo.data.GlobalSettings;
import by.q64.promo.data.Situation;
import by.q64.promo.domain.*;
import by.q64.promo.domain.dto.PromoterSalesByPromoDTO;
import by.q64.promo.domain.dto.SaleHasPromoStockDTO;
import by.q64.promo.domain.old.OldReportPromoter;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.hibernate.criterion.Restrictions.*;

@SuppressWarnings("unchecked")
@Transactional
@Service("BaseRequest")
public class BaseRequest {

    public static BaseRequest instance;

    @Autowired
    @Qualifier("BaseKernelImpl")
    BaseKernel baseKernel;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public <T> List<T> getListEntity(Class<T> entityClass) {
        return baseKernel.getListEntity(entityClass);
    }


    public <T> List<T> getListEntity(Class<T> entityClass, int active) {
        try {
            Criteria criteria = baseKernel.getCriteria(entityClass);
            if (active != 2)
                criteria.add(like("active", active));
            List<T> listEntity = (List<T>) criteria.list();
            return listEntity;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<T>();
        }
    }

    public Object saveOrUpdate(Object object) {
        return baseKernel.saveOrUpdate(object);
    }

    public Object save(Object object) {
        return baseKernel.save(object);
    }

    public <T> T getEntity(Class<T> entityClass, Integer id) {
        return baseKernel.getEntity(entityClass, id);
    }

    public <T> T getOneValue(List<T> listEntity) {
        switch (listEntity.size()) {
            case 0:
                return null;
            case 1:
                return listEntity.get(0);
            default:
                logger.warn("SIZE > 1" + Arrays.toString(listEntity.toArray()));
                return listEntity.get(0);
        }
    }

    public List<NaireQuestion> getSpecialQuestion(Integer[] ids) {
        try {
            Criteria criteria = baseKernel.getCriteria(NaireQuestion.class);
            criteria.add(Restrictions.in("id", ids));
            List<NaireQuestion> questions = (List<NaireQuestion>) criteria
                    .list();
            return questions;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<NaireQuestion>();
        }
    }

    public List<TrastPromoShedule> getSchedules() {
        Criteria result = baseKernel.getCriteria(TrastPromoShedule.class);
        result.add(Restrictions.like("status", 0));
        result.add(Restrictions.lt("start", Timestamp.valueOf(LocalDateTime.now())));
        return (List<TrastPromoShedule>) result.list();
    }

    public List<TrastPromoShedule> getActualSchedulers(AppUser appUser,
                                                       LocalDateTime monday, int region) {
        try {
            Criteria criteria = baseKernel.getCriteria(TrastPromoShedule.class);
            switch (appUser.getRole()) {
                case AppUser.COORDINATOR:
                    criteria.add(Restrictions.like("region", region));
                    break;
                case AppUser.PROMOTER:
                case AppUser.SUPERVISOR:
                    logger.error("Супервайзер и промоутер не могут стартовать инстансы");
                    return new ArrayList<TrastPromoShedule>();
            }
            LocalDateTime nextMonday = monday.plusDays(7);
            criteria.add(Restrictions.like("status", 0));
            criteria.add(Restrictions.ge("start", Timestamp.valueOf(monday)));
            criteria.add(Restrictions.lt("start", Timestamp.valueOf(nextMonday)));
            List<TrastPromoShedule> trastPromoShedules = (List<TrastPromoShedule>) criteria
                    .list();
            if (logger.isDebugEnabled()) {
                for (TrastPromoShedule trastPromoShedule : trastPromoShedules) {
                    logger.info(trastPromoShedule.toString());
                }
            }
            return trastPromoShedules;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<TrastPromoShedule>();
        }
    }

    // private List<Region> getRegionsCoordinator(int coordinatorId) {
    // try {
    // Criteria criteria = baseKernel.getCriteria(Region.class);
    // criteria.add(like("coordinator", coordinatorId));
    // return (List<Region>) criteria.list();
    // } catch (Exception e) {
    // e.printStackTrace();
    // return new ArrayList<Region>();
    // }
    // }

    public AppUser getUserFromEmail(String email) {
        try {
            Criteria criteria = baseKernel.getCriteria(AppUser.class);
            criteria.add(like("email", email));
            return (AppUser) criteria.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return baseKernel.getEntity(AppUser.class, 3);
        }
    }

    public boolean delete(Object object) {
        logger.warn("DELETE : " + object.toString());
        return baseKernel.delete(object);

    }

    public void addFormHasAttribute(int form, int attribute) {
        String queryName = "INSERT INTO [dbo].[form_has_attribute] ([form],[attribute]) VALUES ("
                + form + "," + attribute + ")";
        SQLQuery query = baseKernel.createSQLQuery(queryName);
        query.executeUpdate();
    }

    public void addProjectShipment(int project, int[] array) {
        StringBuilder request = new StringBuilder(
                "INSERT INTO [dbo].[project_has_shipment] ([project] ,[shipment]) VALUES ");
        for (int i : array) {
            request.append("(" + project + "," + i + "),");
        }
        request.deleteCharAt(request.length() - 1);
        String queryName = request.toString();
        SQLQuery query = baseKernel.createSQLQuery(queryName);
        query.executeUpdate();
    }

    public void addShopsToProject(int project, int[] array) {
        StringBuilder request = new StringBuilder(
                "INSERT INTO [dbo].[project_has_shop] ([project] ,[shop]) VALUES ");
        for (int i : array) {
            request.append("(" + project + "," + i + "),");
        }
        request.deleteCharAt(request.length() - 1);
        String queryName = request.toString();
        SQLQuery query = baseKernel.createSQLQuery(queryName);
        query.executeUpdate();
    }

    public static BaseRequest getInstance() {
        return BaseRequest.instance;
    }

    /*
     * public List<ChatMessage> getChatMessages(int chat, int last, int
     * maxResults) { List<ChatMessage> chatMessages = null; try { Criteria
     * criteria = baseKernel.getCriteria(ChatMessage.class);
     * criteria.add(Restrictions.like("chat", chat));
     * criteria.setMaxResults(maxResults); criteria.addOrder(Order.desc("id"));
     * chatMessages = (List<ChatMessage>) criteria.list(); } catch (Exception e)
     * { chatMessages = new ArrayList<ChatMessage>(); } return chatMessages; }
     */

    public List<ChatMessage> getChatMessages(int chat, int minId, int maxResults) {
        List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
        try {
            Criteria criteria = baseKernel.getCriteria(ChatMessage.class);
            criteria.add(like("chat", chat));
            if (minId > 0) {
                criteria.add(lt("id", minId));
            }
            criteria.setMaxResults(maxResults);
            criteria.addOrder(Order.desc("id"));
            chatMessages = (List<ChatMessage>) criteria.list();
        } catch (Exception e) {
        }
        return chatMessages;
    }

    public List<ChatMessage> getNewChatMessages(int chat, int maxId) {
        List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
        try {
            Criteria criteria = baseKernel.getCriteria(ChatMessage.class);
            criteria.add(like("chat", chat));
            criteria.add(gt("id", maxId));
            criteria.addOrder(Order.desc("id"));
            chatMessages = (List<ChatMessage>) criteria.list();
        } catch (Exception e) {
        }
        return chatMessages;
    }

    public ChatMessage skipMessages(int chat, int messageId, int countToSkip) {
        List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
        try {
            Criteria criteria = baseKernel.getCriteria(ChatMessage.class);
            criteria.add(like("chat", chat));
            criteria.add(gt("id", messageId));
            criteria.setMaxResults(countToSkip);
            criteria.addOrder(Order.asc("id"));
            chatMessages = (List<ChatMessage>) criteria.list();
            return chatMessages.get(chatMessages.size() - 1);
        } catch (Exception e) {
        }
        return null;
    }

    public List<ChatContactUserUser> getChatContactUserUser(AppUser owner,
                                                            AppUser contact) {
        List<ChatContactUserUser> chatContactUserUsers = null;
        try {
            Criteria criteria = baseKernel
                    .getCriteria(ChatContactUserUser.class);
            criteria.add(Restrictions.like("appUserOwner", owner));
            criteria.add(Restrictions.like("appUserContact", contact));
            chatContactUserUsers = (List<ChatContactUserUser>) criteria.list();
            for (int i = 1; i < chatContactUserUsers.size(); i++) {
                delete(chatContactUserUsers.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chatContactUserUsers;
    }

    public List<AppUser> getRegionAppUsers(int region, int role) {
        List<AppUser> appUsers = null;
        try {
            Criteria criteria = baseKernel.getCriteria(AppUser.class);
            criteria.add(like("role", role));
            criteria.add(like("active", 1));
            criteria.add(like("region", region));
            appUsers = (List<AppUser>) criteria.list();
        } catch (Exception e) {
            e.printStackTrace();
            appUsers = new LinkedList<AppUser>();
        }
        return appUsers;
    }

    public List<NaireQuestion> getNQuestions(Integer projectId) {
        Query query = baseKernel
                .createSQLQuery(
                        "select q.* from naire_question q inner join (select question_id from naire_question_to_project where project_id = :fid) fq on fq.question_id = q.id")
                .addEntity(NaireQuestion.class);
        query.setParameter("fid", projectId);
        return query.list();
    }

    public NaireUserAnswer getNUserAnswers(Integer questionId, Integer flowId) {
        Query query = baseKernel
                .createQuery("from NaireUserAnswer a where a.questionId = :qid and a.flowId = :fid");
        query.setParameter("qid", questionId);
        query.setParameter("fid", flowId);
        Object o = query.uniqueResult();
        return o != null ? (NaireUserAnswer) o : null;
    }

    public List<Sale> getSales(int shipmentId, QFForm qfform) {
        List<Sale> listEntity = null;
        try {
            Criteria criteria = baseKernel.getCriteria(Sale.class);
            criteria.add(like("shipment", shipmentId));
            criteria.add(like("qfform", qfform.getId()));
            listEntity = (List<Sale>) criteria.list();
        } catch (Exception ex) {
            listEntity = new ArrayList<>();
        }
        return listEntity;
    }

    public List<SaleHasPromoStock> getSaleHasPromoStocks(Sale sale) {
        List<SaleHasPromoStock> listEntity = null;
        try {
            Criteria criteria = baseKernel.getCriteria(SaleHasPromoStock.class);
            criteria.add(like("sale", sale.getId()));
            listEntity = (List<SaleHasPromoStock>) criteria.list();
        } catch (Exception ex) {
            listEntity = new ArrayList<>();
        }
        return listEntity;
    }

    public List<AppUser> getAppUsers(Integer region, Integer role) {
        List<AppUser> listEntity = null;
        try {
            Criteria criteria = baseKernel.getCriteria(AppUser.class);
            criteria.add(sqlRestriction("id > 9"));
            if (region != null) {
                criteria.add(like("region", region));
            }
            if (role != null) {
                criteria.add(like("role", role));
            }
            criteria.add(like("active", 1));
            listEntity = (List<AppUser>) criteria.list();
        } catch (Exception ex) {
            listEntity = new ArrayList<>();
        }
        return listEntity;
    }

    public List<AppUser> getRightUsers(Integer region, int active) {
        List<AppUser> listEntity = null;
        try {
            Criteria criteria = baseKernel.getCriteria(AppUser.class);
            criteria.add(sqlRestriction("id > 9"));

            criteria.add(Restrictions.sqlRestriction("id != "
                    + AppUser.PROMOTER_NONE_ID));
            criteria.add(Restrictions.sqlRestriction("id != "
                    + AppUser.SUPERVISOR_NONE_ID));
            if (active != 2)
                criteria.add(like("active", active));

            if (region != null) {
                criteria.add(like("region", region));
            }
            listEntity = (List<AppUser>) criteria.list();
        } catch (Exception ex) {
            listEntity = new ArrayList<>();
        }
        return listEntity;
    }

    public List<AppUser> getRightUsers(int active) {
        return getRightUsers(null, active);
    }

    public List<FlowLog> getFlowLog(int flow_id) {
        List<FlowLog> listEntity = null;
        try {
            Criteria criteria = baseKernel.getCriteria(FlowLog.class);
            criteria.add(like("flow_id", flow_id));
            listEntity = (List<FlowLog>) criteria.list();
        } catch (Exception ex) {
            listEntity = new ArrayList<>();
        }
        return listEntity;
    }

    public <T> List<T> getListEntity(Class<T> entityClass, String string,
                                     Object object) {
        try {
            Criteria criteria = baseKernel.getCriteria(entityClass);
            criteria.add(like(string, object));
            List<T> listEntity = (List<T>) criteria.list();
            return listEntity;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<T>();
        }
    }

    public <T> List<T> getListEntity(Class<T> entityClass, String string,
                                     Object object, int active) {
        try {
            Criteria criteria = baseKernel.getCriteria(entityClass);
            criteria.add(like(string, object));
            if (active != 2)
                criteria.add(like("active", active));
            List<T> listEntity = (List<T>) criteria.list();
            return listEntity;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<T>();
        }
    }

    public <T> List<T> getListEntity(Class<T> entityClass, String string,
                                     Object object, String string2, Object object2) {
        try {
            Criteria criteria = baseKernel.getCriteria(entityClass);
            criteria.add(Restrictions.like(string, object));
            criteria.add(Restrictions.like(string2, object2));
            List<T> listEntity = (List<T>) criteria.list();
            return listEntity;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<T>();
        }
    }

    public <T> List<T> getListEntity(Class<T> entityClass, String string,
                                     Object object, String string2, Object object2, String string3,
                                     Object object3) {
        try {
            Criteria criteria = baseKernel.getCriteria(entityClass);
            criteria.add(Restrictions.like(string, object));
            criteria.add(Restrictions.like(string2, object2));
            criteria.add(Restrictions.like(string3, object3));
            List<T> listEntity = (List<T>) criteria.list();
            return listEntity;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<T>();
        }
    }

    public List<NaireUserAnswer> getAnswersByQuestions(FlowInterface flow,
                                                       Integer[] ids) {
        try {
            Criteria criteria = baseKernel.getCriteria(NaireUserAnswer.class);
            criteria.add(Restrictions.like("flowId", flow.getFlowId()));
            criteria.add(Restrictions.in("questionId", ids));
            List<NaireUserAnswer> listEntity = (List<NaireUserAnswer>) criteria
                    .list();
            return listEntity;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<NaireUserAnswer>();
        }
    }

    public List<NaireUserAnswer> getNAnswers(FlowInterface flow) {
        try {
            Criteria criteria = baseKernel.getCriteria(NaireUserAnswer.class);
            criteria.add(Restrictions.like("flowId", flow.getFlowId()));
            List<NaireUserAnswer> listEntity = (List<NaireUserAnswer>) criteria
                    .list();
            return listEntity;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<NaireUserAnswer>();
        }
    }

    public List<TrastPromoShedule> getTrastPromoSheduleSEVENDAYS(
            Integer regionId, LocalDate monday, Integer project,
            Integer promoter, Integer supervisor) {
        List<TrastPromoShedule> promoShedules = null;
        try {
            Criteria criteria = baseKernel.getCriteria(TrastPromoShedule.class);
            scheduleSearchCriteria(criteria, regionId, monday, project, promoter, supervisor);
            promoShedules = (List<TrastPromoShedule>) criteria.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return promoShedules;
    }

    public List<ClientSchedule> getSheduleSEVENDAYS(
            Integer regionId, LocalDate monday, Integer project,
            Integer promoter, Integer supervisor, int role) {
        List<ClientSchedule> promoShedules = null;
        try {
            Criteria criteria = baseKernel.getCriteria(ClientSchedule.class);
            scheduleSearchCriteria(criteria, regionId, monday, project, promoter, supervisor);
            Disjunction disjunction = disjunction();
            disjunction.add(eq("state", ClientSchedule.commited));
            if (role == AppUser.CLIENT) {
                disjunction.add(eq("state", ClientSchedule.deleted));
            }
            if (role == AppUser.MANAGER || role == AppUser.HEAD_PROMO_DEPARTMENT)
                disjunction.add(eq("state", ClientSchedule.created));
            criteria.add(disjunction);
            promoShedules = (List<ClientSchedule>) criteria.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return promoShedules;
    }

    private void scheduleSearchCriteria(Criteria criteria, Integer regionId, LocalDate monday, Integer project,
                                        Integer promoter, Integer supervisor) {
        if (regionId != null) {
            criteria.add(like("region", regionId));
        }
        if (project != null) {
            criteria.add(like("project", project));
        }
        if (promoter != null) {
            criteria.add(like("promoter", promoter));
        }
        if (supervisor != null) {
            criteria.add(like("supervisor", supervisor));
        }
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd");
        criteria.add(Restrictions.sqlRestriction("start >= '"
                + monday.format(formatter) + "'"));
        criteria.add(Restrictions.sqlRestriction("start < '"
                + monday.plusWeeks(1).format(formatter) + "'"));
    }

    public List<PromoterMoney> getPromoterMoney(int instanceId) {
        try {
            Criteria criteria = baseKernel.getCriteria(PromoterMoney.class);
            criteria.add(like("instance", instanceId));
            // criteria.add(Restrictions.like("flowId", flow.getId()));
            // criteria.add(Restrictions.like("userId", appUser.getId()));
            List<PromoterMoney> listEntity = (List<PromoterMoney>) criteria
                    .list();
            return listEntity;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<PromoterMoney>();
        }
    }

    public List<FlowLog> getFlowLogDublicateFlow(Flow flow) {
        Criteria criteria = baseKernel.getCriteria(FlowLog.class);
        criteria.add(like("flow_id", flow.getId()));
        Action action = Action.getFromId(flow.getAction());
        Situation situation = Situation.getFromId(action.getSituationFrom());
        criteria.add(like("situation", situation.getId()));
        // criteria.add(Restrictions.like("situation",
        // flow.getAction().getSituationFrom()));
        return (List<FlowLog>) criteria.list();
    }

    public QFComplexData getDescriptionForSupervizertask(int flowId,
                                                         AppUser appUser) {
        Criteria criteria = baseKernel.getCriteria(QFComplexData.class);
        criteria.add(Restrictions.like("FV2_ValueType_id",
                QFComplexData.FV2_V_SUPERVISOR_TASK));
        criteria.add(Restrictions.like("qfba1", flowId));
        QFComplexData complexData = (QFComplexData) criteria.uniqueResult();
        if (complexData == null) {
            try {
                complexData = QFComplexData.getEmpty();
                complexData.setAppUser(appUser);
                complexData
                        .setFV2_ValueType_id(QFComplexData.FV2_V_SUPERVISOR_TASK);
                complexData.setQfba1(flowId);
                baseKernel.saveOrUpdate(complexData);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return complexData;
    }

    public List<Categoryshipment> getCategoryshipments(int type, int active) {
        Criteria criteria = baseKernel.getCriteria(Categoryshipment.class);
        criteria.add(like("type", type));
        criteria.add(Restrictions.ne("id", Categoryshipment.BONUS));
        if ((active == 0) || (active == 1)) {
            criteria.add(like("active", active));
        }

        List<Categoryshipment> list = (List<Categoryshipment>) criteria.list();
        return list;
    }

    public List<PromoStock> getStocks(LocalDateTime starttime, int category,int region,int shopNetwork) {
        Criteria criteria = baseKernel.getCriteria(PromoStock.class);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        criteria.add(Restrictions.sqlRestriction("start <= '" + starttime.format(formatter) + "'"));
        criteria.add(Restrictions.sqlRestriction("stop >= '" + starttime.format(formatter) + "'"));
        Disjunction dis=disjunction();
        Conjunction con=conjunction();
        dis.add(Restrictions.eq("region", region));
        dis.add(Restrictions.eq("region", 1));
        con.add(dis);
        dis=disjunction();
        dis.add(Restrictions.eq("shopNetwork",0));
        dis.add(Restrictions.eq("shopNetwork", shopNetwork));
        con.add(dis);
        criteria.add(con);
        
        if (category!=Categoryshipment.BONUS&&category != 0)
            criteria.add(Restrictions.like("category", category));
        List<PromoStock> list = (List<PromoStock>) criteria.list();
        return list;
    }

    public List<PromoStock> getStocks(Integer active, Integer category) {
        Criteria criteria = baseKernel.getCriteria(PromoStock.class);
        if (active.equals(1)) {
            criteria.add(Restrictions.ge("stop", Timestamp.valueOf(LocalDateTime.now())));
        }
        if (active.equals(0)) {
            criteria.add(Restrictions.lt("stop", Timestamp.valueOf(LocalDateTime.now())));
        }
        if (category != null) {
            criteria.add(Restrictions.eq("category", category));
        }
        return (List<PromoStock>) criteria.list();
    }

    /**
     * Если промоутер занят в это время, то false
     *
     * @param promoter
     * @param startTime
     * @param endTime
     * @return
     */
    public List<TrastPromoShedule> findCollisionTps(Integer promoter,
                                                    LocalDateTime startTime, final LocalDateTime endTime) {

        if (promoter != AppUser.PROMOTER_NONE_ID) {
            Criteria criteria = baseKernel.getCriteria(TrastPromoShedule.class);

            criteria.add(Restrictions.like("promoter", promoter));
            DateTimeFormatter formatter = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime localDateTimeBefore = startTime.toLocalDate()
                    .atStartOfDay().minusHours(1);
            LocalDateTime localDateTimeAfter = startTime.toLocalDate()
                    .plusDays(1).atStartOfDay().plusHours(1);
            logger.info(promoter.toString());
            logger.info("localDateTimeBefore" + localDateTimeBefore.toString());
            logger.info("localDateTimeAfter" + localDateTimeAfter.toString());
            criteria.add(Restrictions.sqlRestriction("start > '"
                    + localDateTimeBefore.format(formatter) + "'"));
            criteria.add(Restrictions.sqlRestriction("start < '"
                    + localDateTimeAfter.format(formatter) + "'"));

            // criteria.add(Restrictions.sqlRestriction("start > '" +
            // startTime.format(formatter) + "'"));
            // criteria.add(Restrictions.sqlRestriction("start < '" +
            // endTime.format(formatter) + "'"));
            List<TrastPromoShedule> promoShedules = (List<TrastPromoShedule>) criteria
                    .list();
            return promoShedules;
        }
        return new ArrayList<>();
    }

    public List<UnitRegion> getShops(int shopNetwork, int region,
                                     int unitActivity) {
        Criteria criteria = baseKernel.getCriteria(UnitRegion.class);
        if (shopNetwork > 0) {

            criteria.add(Restrictions.like("unit",
                    new Unit().setId(shopNetwork)));
        }
        if (unitActivity > 0) {
            criteria.add(Restrictions.like("unitActivity",
                    new UnitActivity().setId(unitActivity)));
        }

        if (region > 0) {
            criteria.add(like("region", new Region().setId(region)));
        }

        List<UnitRegion> list = (List<UnitRegion>) criteria.list();
        return list;
    }

    public int getCountChatMessages(int id) {
        String queryString = "SELECT count(*) FROM chatmessage where chat = "
                + id;
        SQLQuery query = baseKernel.createSQLQuery(queryString);
        Object object = query.uniqueResult();
        return (int) object;
    }

    /**
     * ****************
     */

    public List<Categoryshipment> getCategoryshipments(int type) {
        return getCategoryshipments(type, 1);
    }

    /**
     * ****************
     */

    public void addShipmentCategory(int shipment, int category) {
        String queryName = "INSERT INTO [dbo].[category_has_shipment] ([category],[shipment]) VALUES ("
                + category + "," + shipment + ")";
        SQLQuery query = baseKernel.createSQLQuery(queryName);
        query.executeUpdate();
    }

    public List<ClientSchedule> getCSchedules(LocalDateTime localDateFrom,
                                              LocalDateTime localDateTo, Integer project, Integer region, int role) {
        Criteria criteria = baseKernel.getCriteria(ClientSchedule.class);
        setGetShedukesCriteria(criteria, localDateFrom, localDateTo, project, region);
        criteria.add(Restrictions.eq("state", ClientSchedule.commited));

        List<ClientSchedule> list = (List<ClientSchedule>) criteria
                .list();
        return list;
    }

    public List<TrastPromoShedule> getTSchedules(LocalDateTime localDateFrom,
                                                 LocalDateTime localDateTo, Integer project, Integer region) {
        Criteria criteria = baseKernel.getCriteria(TrastPromoShedule.class);
        setGetShedukesCriteria(criteria, localDateFrom, localDateTo, project, region);

        List<TrastPromoShedule> list = (List<TrastPromoShedule>) criteria
                .list();
        return list;
    }

    private void setGetShedukesCriteria(Criteria criteria, LocalDateTime localDateFrom,
                                        LocalDateTime localDateTo, Integer project, Integer region) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:ss");
        if (localDateFrom != null) {
            criteria.add(Restrictions.sqlRestriction("start > '"
                    + localDateFrom.format(formatter) + "'"));
        }
        if (localDateTo != null) {
            criteria.add(Restrictions.sqlRestriction("start < '"
                    + localDateTo.format(formatter) + "'"));
        }
        criteria.add(Restrictions.ne("promoter", AppUser.PROMOTER_NONE_ID));
        criteria.add(Restrictions.like("project", project));
        criteria.add(Restrictions.like("region", region));
    }

    /**
     * *************************
     */

    public <T> List<T> getFlows(Class<T> entityClass, AppUser appUser,
                                Integer formtemplate, List<Situation> situations,
                                LocalDateTime dateTill, LocalDateTime dateUntill, String search,
                                List<Integer> accepteds) {
        try {
            Criteria criteria = baseKernel.getCriteria(entityClass);
            if (appUser != null) {
                criteria.add(like("appUser", appUser));
            }
            criteria.addOrder(Order.desc("changeTime"));
            Disjunction acceptedDisjunction = disjunction();
            for (Integer accepted : accepteds) {
                acceptedDisjunction.add(eq("accepted", accepted));
            }
            criteria.add(acceptedDisjunction);

            Disjunction situationDisjunction = disjunction();
            for (Situation situation : situations) {
                situationDisjunction.add(eq("situation", situation.getId()));
            }

            criteria.add(situationDisjunction);
            criteria.setMaxResults(GlobalSettings.TASKS_IN_FOLDER);

            if (null != formtemplate) {
                criteria.add(eq("formTemplate", formtemplate));
            }
            DateTimeFormatter formatter = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:ss");

            if (dateUntill != null) {
                try {
                    String startTime = "starttime > '"
                            + dateUntill.format(formatter) + "'";

                    // logger.info(dateUntill.toString());
                    // logger.info(startTime);

                    criteria.add(sqlRestriction(startTime));
                } catch (Exception ex) {
                    logger.error("FAIL CONVERT!!!");
                }

            }

            if (dateTill != null) {
                criteria.add(sqlRestriction("stoptime < '"
                        + dateTill.plusDays(1).format(formatter) + "'"));
            }

            if (search != null) {
                if (search.length() > 1) {
                    search = "%" + search + "%";
                    Disjunction searchDisjunction = disjunction();
                    searchDisjunction.add(like("shopActivityName", search));
                    searchDisjunction.add(like("instanceName", search));
                    searchDisjunction.add(like("chainName", search));
                    searchDisjunction.add(like("regionName", search));
                    searchDisjunction.add(like("shopName", search));
                    searchDisjunction.add(like("namefrom", search));
                    criteria.add(searchDisjunction);
                    logger.info(criteria.toString());
                }
            }

            List<T> listEntity = (List<T>) criteria.list();
            if (listEntity.size() != 0) {
                logger.info(criteria.toString());
                logger.info(String.valueOf(listEntity.size()));
            }

            return listEntity;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<T>();
        }
    }

    //
    // add List<Situation> situations
    // public <T> List<T> getFlows(Class<T> entityClass, AppUser appUser,
    // List<Situation> situations,
    // List<Integer> accepteds, LocalDateTime filterUntill,
    // LocalDateTime filterTill, String filterSearch) {
    // try {
    // Criteria criteria = baseKernel.getCriteria(entityClass);
    // if (appUser != null) {
    // criteria.add(Restrictions.like("appUser", appUser));
    // }
    // criteria.addOrder(Order.desc("changeTime"));
    // Disjunction acceptedDisjunction = Restrictions.disjunction();
    // Conjunction dateConjunction = Restrictions.conjunction();
    // Disjunction filterSearchDisjunction = Restrictions.disjunction();
    // filterSearchDisjunction.add(Restrictions.like("fromName", "%"
    // + filterSearch + "%"));
    // dateConjunction.add(Restrictions.gt("date", filterUntill));
    // dateConjunction.add(Restrictions.lt("date", filterTill));
    // for (Integer accepted : accepteds) {
    // acceptedDisjunction.add(Restrictions.eq("accepted", accepted));
    // }
    // criteria.add(acceptedDisjunction);
    // criteria.add(dateConjunction);
    //
    // Disjunction situationDisjunction = Restrictions.disjunction();
    // for (Situation situation : situations) {
    // situationDisjunction.add(Restrictions.eq("situation",
    // situation.getId()));
    // }
    // @SuppressWarnings("unchecked")
    // List<T> listEntity = (List<T>) criteria.list();
    // return listEntity;
    // } catch (Exception e) {
    // e.printStackTrace();
    // return new ArrayList<T>();
    // }
    // }

    public <T> List<T> getFlows(Class<T> entityClass, AppUser appUser,
                                Integer formtemplate, List<Situation> situations,
                                LocalDateTime dateTill, LocalDateTime dateUntill, String search,
                                Integer... accepteds) {
        return getFlows(entityClass, appUser, formtemplate, situations,
                dateTill, dateUntill, search, Arrays.asList(accepteds));
    }

    public <T> Integer getCountFlows(Class<T> entityClass, Integer region,
                                     Integer formTemplate, AppUser appUser, List<Situation> situations,
                                     List<Integer> accepteds) {
        StringBuffer stringBuffer = new StringBuffer(
                "SELECT count(*) FROM [dbo].[");
        stringBuffer.append(entityClass.getSimpleName());
        stringBuffer.append("] where ");
        if (appUser != null) {
            stringBuffer.append(" User_id = " + appUser.getId());
            stringBuffer.append(" and ");
        }
        boolean noFirst = false;
        if (accepteds.size() > 0) {
            stringBuffer.append("(");
            for (Integer integer : accepteds) {
                if (noFirst) {
                    stringBuffer.append(" or ");
                } else {
                    noFirst = true;
                }
                stringBuffer.append(" Accepted = " + integer + " ");
            }
            stringBuffer.append(" ) ");
        }
        if (entityClass == Flow.class) {
            if (null != region) {
                stringBuffer.append(" and region=" + region);
            }
        }

        if (null != formTemplate) {
            stringBuffer.append(" and formTemplate=" + formTemplate);
        }

        boolean noFirstSituation = false;
        if (situations.size() > 0) {
            stringBuffer.append(" and (");
            for (Situation situation : situations) {
                if (noFirstSituation) {
                    stringBuffer.append(" or ");
                } else {
                    noFirstSituation = true;
                }
                stringBuffer.append(" State_id = " + situation.getId() + " ");
            }
            stringBuffer.append(" ) ");
        }

        String request = stringBuffer.toString();
        logger.info(request);
        return getCountRezult(request);
    }

    // <<<<<<< HEAD

    private int getCountRezult(String string) {
        String queryString = string;
        SQLQuery query = baseKernel.createSQLQuery(queryString);
        Object object = query.uniqueResult();
        int count = (int) object;
        return count;
    }

    public <T> Integer getCountFlows(Class<T> entityClass, Integer region,
                                     Integer formTemplate, AppUser appUser, List<Situation> situations,
                                     Integer... accepteds) {
        return getCountFlows(entityClass, region, formTemplate, appUser,
                situations, Arrays.asList(accepteds));
    }

    public boolean checkJoin(int mainThread) {
        try {
            // Колличество потоков, которые дочерние ГЛАВНОГО ТРЕДА и ещё не
            // окончены
            int a = baseKernel
                    .createQuery(
                            "SELECT f FROM Flow f WHERE f.mainThread = "
                                    + mainThread + " AND f.accepted != "
                                    + Flow.ACCEPTED_COMPLETE).list().size();
            logger.info("CHECK JOIN : " + a);
            // Если это последний тред, то колличество будет рано единице.
            return a == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public UnitRegion findUnitRegion(int unit, int region) {
        Criteria criteria = baseKernel.getCriteria(UnitRegion.class);
        criteria.add(like("unit", unit));
        criteria.add(like("region", region));
        List<UnitRegion> listEntity = (List<UnitRegion>) criteria.list();
        UnitRegion unitRegion = listEntity.get(0);
        return unitRegion;
    }

    // public <T> Integer getCountFlows(Class<T> entityClass ,AppUser
    // appUser,Integer...accepteds) {
    // return getCountFlows(entityClass, null, null, appUser, accepteds);
    // }

    // >>>>>/>> reportList

    public UnitActivity getUnitActivity(int unitRegionId, int projectId) {
        Criteria criteria = baseKernel.getCriteria(Activity.class);
        criteria.add(like("unitRegion", unitRegionId));
        criteria.add(like("project", projectId));
        Activity activity = (Activity) criteria.uniqueResult();
        return getEntity(UnitActivity.class, activity.getActivity());
    }

    public Activity getLastActivity(int unitRegionId, int projectId) {
        Criteria criteria = baseKernel.getCriteria(Activity.class);
        criteria.add(like("unitRegion", unitRegionId));
        criteria.add(like("project", projectId));
        criteria.addOrder(Order.desc("started"));
        criteria.setMaxResults(1);

        Activity activity = (Activity) criteria.uniqueResult();

        if (activity == null) {
            activity = Activity.getActivity(unitRegionId, projectId,
                    Activity.NO_ACTIVITY, null, null,Activity.NO_FORM);
        }
        return activity;

    }

    public List<Activity> getLastActivities(int unitRegionId) {
        List<Project> projects = getListEntity(Project.class, 1);
        List<Activity> result = new ArrayList<>();
        for (Project proj : projects) {
            result.add(getLastActivity(unitRegionId, proj.getId()));
        }

        return result;
    }

    public List<ChatContactUserUser> getChatContactUserUsersOwner(
            AppUser owner, int active) {
        Criteria criteria = baseKernel.getCriteria(ChatContactUserUser.class);
        criteria.add(Restrictions.like("appUserOwner", owner));
        criteria.add(Restrictions.like("active", active));
        List<ChatContactUserUser> result = (List<ChatContactUserUser>) criteria
                .list();
        return result;
    }

    public List<ChatContactUserUser> getChatContactUserUsersOwner(AppUser owner) {
        Criteria criteria = baseKernel.getCriteria(ChatContactUserUser.class);
        criteria.add(Restrictions.like("appUserOwner", owner));
        List<ChatContactUserUser> result = (List<ChatContactUserUser>) criteria
                .list();
        return result;
    }

    public List<Region> getRegions(AppUser user) {
        List<Region> listEntity = null;
        try {
            Criteria criteria = baseKernel.getCriteria(Region.class);
            criteria.add(sqlRestriction("id > 1"));
            criteria.addOrder(Order.asc("regionName"));
            switch (user.getRole()) {
                case AppUser.ADMIN:
                case AppUser.HEAD_PROMO_DEPARTMENT:
                case AppUser.CLIENT:
                case AppUser.MANAGER: {
                    break;
                }
                case AppUser.COORDINATOR:
                    criteria.add(Restrictions.like("coordinator", user.getId()));
                    break;
                default:
                    List<Region> result = new ArrayList<Region>();
                    result.add(getEntity(Region.class, user.getRegion()));
                    return result;
            }
            listEntity = (List<Region>) criteria.list();
        } catch (Exception ex) {
            ex.printStackTrace();
            listEntity = new ArrayList<>();
        }
        return listEntity;
    }

    public List<Flow> getFlowsFromInstance(Instance instanceI) {
        return getListEntity(Flow.class, "instance", instanceI.getId());
    }

    public int deleteReportSalesInfo(int flowId) {
        Query query = baseKernel
                .createQuery("delete from ReportSalesInfoEntity where flowId = "
                        + flowId);
        return query.executeUpdate();
    }

    public int deleteReportShipmentSales(int flowId) {
        Query query = baseKernel
                .createQuery("delete from ReportShipmentSalesEntity where reportSalesInfoEntity.flowId = "
                        + flowId);
        return query.executeUpdate();
    }

    public List<ReportShipmentSalesEntity> getReportShipmentSalesByDatePeriodInclusive(
            Integer unitRegionId, Integer promoterId, Date start, Date end,
            Integer categoryId) {
        Query query = baseKernel
                .createQuery("select rsse from ReportShipmentSalesEntity rsse inner join rsse.reportSalesInfoEntity rsie where rsie.promoterId = :promoterId "
                        + " and rsie.unitRegionId = :unitRegionId and rsie.date between :from and :to and rsse.categoryId = :categoryId");
        query.setParameter("from", start);
        query.setParameter("to", end);
        query.setParameter("categoryId", categoryId);
        query.setParameter("unitRegionId", unitRegionId);
        query.setParameter("promoterId", promoterId);
        return query.list();
    }

    public List<PromoNew> getPromoNews(int region, int active) {
        Criteria criteria = baseKernel.getCriteria(PromoNew.class);
        if (region != 0)
            criteria.add(like("region", region));
        criteria.add(like("active", active));
        criteria.addOrder(Order.desc("id"));
        List<PromoNew> result = (List<PromoNew>) criteria.list();
        return result;
    }

    public List<ReportSalesInfoEntity> getSalesInfoEntity(Date start, Date end) {
        return baseKernel.createQuery("FROM ReportSalesInfoEntity where date between :start and :end")
                .setDate("start", start)
                .setDate("end", end).list();
    }

    public List<ReportPromoter> getPromoterReports(Date start, Date end, Integer[] regionIds, int projectId) {
        if (regionIds.length == 0) {
            return new ArrayList<>();
        }


        String queryString = "SELECT rp FROM ReportPromoter rp join fetch rp.flow f  join fetch  rp.reportSalesInfoEntity " +
                " where f.starttime >= :start and f.starttime <= :end and rp.state = :state and f.region in (:regionId)";
        //TODO bad code a little
        if (projectId != 0) {
            queryString += " and rp.projectId = :project";
        }
        Query query = baseKernel.createQuery(queryString)
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameterList("regionId", Arrays.asList(regionIds))
                .setParameter("state", 1);
        if (projectId != 0) {
            query.setParameter("project", projectId);
        }
        return query.list();
    }

    public List<UnitRegion> getUnitRegionsInIds(Collection<Integer> unitRegionsIds) {
        if (unitRegionsIds.size() == 0) {
            return new ArrayList<>();
        }
        return baseKernel.getCriteria(UnitRegion.class)
                .add(Restrictions.in("id", unitRegionsIds))
                .setFetchMode("unitRegionsIds", FetchMode.JOIN).list();
    }

    public List<ReportSalesInfoEntity> getReportSalesByRegionAndDateSuration(
            int regionId, Date start, Date end) {
        return baseKernel.getNamedQuery("findSalesInfoByRegionAndDateDuration")
                .setDate("start", start).setDate("end", end)
                .setInteger("stateId", 8) //completed
                .setInteger("regionId", regionId)
                        // .setResultTransformer(Transformers.aliasToBean(PromoterSalesByPromoDTO.class))
                .list();
    }

    public List<PromoterSalesByPromoDTO> getPromoterBonusesByPromoDTOs(
            Date start, Date end) {
        List list = baseKernel
                .getNamedQuery("findBonusesCountByStock")
                .setDate("start", start)
                .setDate("end", end)
                .setInteger("role", AppUser.PROMOTER)
                .setInteger("stateId", 8) // completed
                .setInteger("value", 1)
                .setResultTransformer(
                        Transformers.aliasToBean(PromoterSalesByPromoDTO.class))
                .list();
        return list;
    }

    public List<PromoStock> getPromoStockInPeriod(Date start, Date end) {
        return baseKernel
                .getCriteria(PromoStock.class)
                .add(not(or(and(gt("start", start), gt("start", end)),
                        and(le("stop", start), le("stop", end))))).list();
    }

    public <T> int getCountEntity(Class<T> entityclass,
                                  Map<String, Object> params) {
        StringBuilder StringBuilder = new StringBuilder(
                "SELECT count(*) FROM [dbo].[");
        StringBuilder.append(entityclass.getSimpleName());
        StringBuilder.append("] where ");
        Iterator<String> iterator = params.keySet().iterator();
        if (params.size() != 0) {
            String key = iterator.next();
            StringBuilder.append(key + "=");
            StringBuilder.append(params.get(key));
        }
        while (iterator.hasNext()) {
            String str = iterator.next();
            StringBuilder.append(" and " + str + " = ");
            StringBuilder.append(params.get(str));

        }
        return getCountRezult(StringBuilder.toString());
    }

    public List<HemReport> getHemReportWeek(int region, LocalDateTime monday,
                                            int type) {
        LocalDateTime nextMonday = monday.plusDays(7);
        Criteria criteria = baseKernel.getCriteria(HemReport.class);
        criteria.add(Restrictions.like("type", type));
        criteria.add(Restrictions.like("region", region));

        criteria.add(Restrictions.ge("started", Timestamp.valueOf(monday)));
        criteria.add(Restrictions.lt("started", Timestamp.valueOf(nextMonday)));
        List<HemReport> result = (List<HemReport>) criteria.list();

        return result;
    }

    public List<Flow> getFlowsThatWeek(String regionName, LocalDateTime monday,
                                       int type) {
        LocalDateTime nextMonday = monday.plusDays(7);
        Criteria criteria = baseKernel.getCriteria(Flow.class);
        if (regionName != null)
            criteria.add(Restrictions.like("regionName", regionName));
        criteria.add(Restrictions.ge("starttime", Timestamp.valueOf(monday)));
        criteria.add(Restrictions.lt("starttime", Timestamp.valueOf(nextMonday)));
        criteria.add(Restrictions.like("formTemplate", type));

        List<Flow> result = (List<Flow>) criteria.list();
        return result;
    }

    public List<Map<String, Object>> getRegionsView(AppUser appUser) {
        List<Region> regions = getRegions(appUser);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Map<String, Object> regionView;
        for (Region region : regions) {
            if (region.getCoordinator() != 0) {
                regionView = new HashMap<String, Object>();
                regionView.put("id", region.getId());
                regionView.put("regionName", region.getRegionName());
                regionView.put("coordinator",
                        getEntity(AppUser.class, region.getCoordinator())
                                .getFullName());
                regionView.put("rate", region.getRate());
                result.add(regionView);
            }
        }
        return result;
    }

    public List<Shipment> getActualShipments() {
        return baseKernel.createQuery("from Shipment s join fetch s.actualShipmentEntity ase").list();
    }

    public List<Shipment> getActualAndOtherShipmentsOfCategory(Integer categoryId) {
        return baseKernel.createQuery("select s from Shipment s join s.categoryshipments cs " +
                "left join fetch s.actualShipmentEntity ase where cs.id = :categoryId and s.active = 1").setParameter("categoryId", categoryId).list();
    }

    public QFComplexData getComplexData(int ba1, int ba2, int formId) {
        List<QFComplexData> listEntity = getListEntity(QFComplexData.class, "qfba1", ba1, "qfba2", ba2, "qfform", new QFForm(formId));
        return getOneValue(listEntity);
    }


    public TrastPromoShedule getMappedTPSFromPrevWeek(TrastPromoShedule tps) {
        Criteria criteria = baseKernel.getCriteria(TrastPromoShedule.class);
        criteria.add(Restrictions.like("project", tps.getProject()));
        criteria.add(Restrictions.like("shop", tps.getShop()));
        LocalDateTime start = tps.getStart().toLocalDateTime().withHour(0).withMinute(0).minusDays(7);
        criteria.add(Restrictions.ge("start", Timestamp.valueOf(start)));
        criteria.add(Restrictions.lt("start", Timestamp.valueOf(start.plusDays(1))));
        List<TrastPromoShedule> result = (List<TrastPromoShedule>) criteria.list();
        if (result.size() == 1)
            return result.get(0);
        else
            return null;

    }

    public ClientSchedule getMappedClientScheduleFromPrevWeek(ClientSchedule tps) {
        Criteria criteria = baseKernel.getCriteria(ClientSchedule.class);
        criteria.add(Restrictions.like("project", tps.getProject()));
        criteria.add(Restrictions.like("shop", tps.getShop()));
        LocalDateTime start = tps.getStart().toLocalDateTime().withHour(0).withMinute(0).minusDays(7);
        criteria.add(Restrictions.ge("start", Timestamp.valueOf(start)));
        criteria.add(Restrictions.lt("start", Timestamp.valueOf(start.plusDays(1))));
        List<ClientSchedule> result = (List<ClientSchedule>) criteria.list();
        if (result.size() == 1)
            return result.get(0);
        else
            return null;

    }

    public ClientSchedule getMappedClientScheduleNextPrevWeek(ClientSchedule tps) {
        Criteria criteria = baseKernel.getCriteria(ClientSchedule.class);
        criteria.add(Restrictions.like("project", tps.getProject()));
        criteria.add(Restrictions.like("shop", tps.getShop()));
        LocalDateTime start = tps.getStart().toLocalDateTime().withHour(0).withMinute(0).plusWeeks(1);
        criteria.add(Restrictions.ge("start", Timestamp.valueOf(start)));
        criteria.add(Restrictions.lt("start", Timestamp.valueOf(start.plusDays(1))));
        List<ClientSchedule> result = (List<ClientSchedule>) criteria.list();
        if (result.size() == 1)
            return result.get(0);
        else
            return null;

    }

    public TrastPromoShedule getMappedScheduleNextPrevWeek(TrastPromoShedule tps) {
        Criteria criteria = baseKernel.getCriteria(TrastPromoShedule.class);
        criteria.add(Restrictions.like("project", tps.getProject()));
        criteria.add(Restrictions.like("shop", tps.getShop()));
        LocalDateTime start = tps.getStart().toLocalDateTime().withHour(0).withMinute(0).plusWeeks(1);
        criteria.add(Restrictions.ge("start", Timestamp.valueOf(start)));
        criteria.add(Restrictions.lt("start", Timestamp.valueOf(start.plusDays(1))));
        List<TrastPromoShedule> result = (List<TrastPromoShedule>) criteria.list();
        if (result.size() == 1)
            return result.get(0);
        else
            return null;

    }

    public Map<String, List<ClientSchedule>> getCSchedulesForCommit(LocalDateTime monday, LocalDateTime nextMonday, int project, int region) {
        Map<String, List<ClientSchedule>> map = new HashMap<String, List<ClientSchedule>>();
        Criteria criteria = baseKernel.getCriteria(ClientSchedule.class);
        criteria.add(Restrictions.like("region", region));
        criteria.add(Restrictions.like("project", project));
        criteria.add(Restrictions.ge("start", Timestamp.valueOf(monday)));
        criteria.add(Restrictions.lt("start", Timestamp.valueOf(nextMonday)));
        criteria.add(Restrictions.eq("state", ClientSchedule.created));
        List<ClientSchedule> list = (List<ClientSchedule>) criteria.list();
        map.put("created", list);
        criteria = baseKernel.getCriteria(ClientSchedule.class);
        criteria.add(Restrictions.like("region", region));
        criteria.add(Restrictions.like("project", project));
        criteria.add(Restrictions.ge("start", Timestamp.valueOf(monday)));
        criteria.add(Restrictions.lt("start", Timestamp.valueOf(nextMonday)));
        criteria.add(Restrictions.eq("state", ClientSchedule.deleted));
        list = (List<ClientSchedule>) criteria.list();
        map.put("deleted", list);
        return map;

    }


    public List<ClientSchedule> getEditedClientSchedule(LocalDate monday, int region, int project) {
        LocalDateTime localDateMonday = monday.atStartOfDay();
        LocalDateTime nextMonday = localDateMonday.plusWeeks(1);
        Criteria criteria = baseKernel.getCriteria(ClientSchedule.class);
        criteria.add(Restrictions.like("region", region));
        criteria.add(Restrictions.like("project", project));
        criteria.add(Restrictions.ge("start", Timestamp.valueOf(localDateMonday)));
        criteria.add(Restrictions.lt("start", Timestamp.valueOf(nextMonday)));
        Disjunction disjunction = disjunction();
        disjunction.add(eq("state", ClientSchedule.created));
        disjunction.add(eq("state", ClientSchedule.deleted));
        criteria.add(disjunction);
        List<ClientSchedule> result = (List<ClientSchedule>) criteria.list();
        return result;
    }


    public List<ReportPromoter> getPromoterReportsForPromoter(AppUser appUser, LocalDateTime till, LocalDateTime untill, String search) {
        StringBuilder queryString = new StringBuilder("SELECT REPORT_PROMOTER.* from [dbo].[REPORT_PROMOTER] join [dbo].[flow] on flowid=flow.id" +
                " where [REPORT_PROMOTER].[state]=1 and promoterId=" + appUser.getId());

        if (untill != null) {
            queryString.append(" and date >'").append(Timestamp.valueOf(untill).toString().substring(0, 10)).append("'");
        }
        if (till != null) {
            queryString.append(" and date <'").append(Timestamp.valueOf(till.plusDays(1)).toString().substring(0, 10)).append("'");
        }
        if (search != null) {
            if (search.length() > 1) {
                search = "%" + search + "%";
                queryString.append(" and (flow.shopActivityName like ").append("'").append(search).append("'");
                queryString.append(" or flow.instanceName like '").append(search).append("'");
                queryString.append(" or flow.chainName like '").append(search).append("'");
                queryString.append(" or flow.regionName like '").append(search).append("'");
                queryString.append(" or flow.shopName like '").append(search).append("')");
            }
        }
        Query query = baseKernel.createSQLQuery(queryString.toString()).addEntity(ReportPromoter.class);
        List<ReportPromoter> reports = (List<ReportPromoter>) query.list();

        return reports;
    }


    public int getProjectByPromoterReport(ReportPromoter report) {
        StringBuilder queryString = new StringBuilder("SELECT instance.Project_id" +
                " FROM [replicationrelease].[dbo].[REPORT_PROMOTER]" +
                " join [replicationrelease].[dbo].[flow] on REPORT_PROMOTER.flowid=flow.id" +
                " join [replicationrelease].[dbo].[instance] on instance.id=flow.Instance_id" +
                " where REPORT_PROMOTER.flowid=" + report.getFlowid());

        return (int) baseKernel.createSQLQuery(queryString.toString()).uniqueResult();
    }


    public List<MarketingReport> getMarketingReports(int region,int promoForm, LocalDate startDate, LocalDate endDate,boolean ready) {
        Criteria criteria = baseKernel.getCriteria(MarketingReport.class);
        criteria.add(Restrictions.eq("startDate", startDate.toString()));
        criteria.add(Restrictions.eq("endDate", endDate.toString()));
        criteria.add(Restrictions.eq("region", region));
        if(ready)
        	criteria.add(Restrictions.eq("state", MarketingReport.READY));
        if(promoForm!=0)
        	criteria.add(Restrictions.eq("promoForm", promoForm));
        return (List<MarketingReport>) criteria.list();
    }


    public List<TrastPromoShedule> getCSchedulesForShop(LocalDate localDateFrom, LocalDate localDateTo, int shop) {
        Criteria criteria = baseKernel.getCriteria(TrastPromoShedule.class);
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd");
        if (localDateFrom != null) {
            criteria.add(Restrictions.sqlRestriction("start >= '"
                    + localDateFrom.format(formatter) + "'"));
        }
        if (localDateTo != null) {
            criteria.add(Restrictions.sqlRestriction("start < '"
                    + localDateTo.format(formatter) + "'"));
        }
        criteria.add(Restrictions.eq("shop", shop));
//        Disjunction disjunction=disjunction();
//        disjunction.add(Restrictions.like("state", ClientSchedule.commited));
//        disjunction.add(Restrictions.like("state", ClientSchedule.deleted));
//        criteria.add(disjunction);
        return (List<TrastPromoShedule>) criteria.list();
    }


    public MarketingReport getMarketingReportByShop(int shop,int promoForm, LocalDate dateStart, LocalDate dateEnd) {
        Criteria criteria = baseKernel.getCriteria(MarketingReport.class);
        criteria.add(Restrictions.eq("shop", shop));
        criteria.add(Restrictions.eq("promoForm", promoForm));
        criteria.add(Restrictions.eq("startDate", dateStart.toString()));
        criteria.add(Restrictions.eq("endDate", dateEnd.toString()));
        return (MarketingReport) criteria.uniqueResult();
    }


    public List<UnitRegion> getActualShops(int region, LocalDate date) {
        Criteria criteria = baseKernel.getCriteria(UnitRegion.class);
        criteria.add(Restrictions.eq("region", region));
        Disjunction disjunction = disjunction();
        disjunction.add(Restrictions.eq("active", 1));
        disjunction.add(Restrictions.gt("ended", Timestamp.valueOf(LocalDateTime.of(date, LocalTime.of(0, 0)))));
        criteria.add(disjunction);
        return (List<UnitRegion>) criteria.list();
    }


    public List<ShopFormSchedule> getShopFormSchedules(int shop, int project, Timestamp mondayTimestamp) {
        Criteria criteria = baseKernel.getCriteria(ShopFormSchedule.class);
        if (shop != 0)
            criteria.add(Restrictions.eq("shop", shop));
        criteria.add(Restrictions.eq("project", project));
        criteria.add(Restrictions.eq("monday", mondayTimestamp));

        return criteria.list();
    }


    public List<ChatContactUserUser> getChats(AppUser appUser, boolean owner) {
        Criteria criteria=baseKernel.getCriteria(ChatContactUserUser.class);
        criteria.add(Restrictions.like(owner?"appUserOwner":"appUserContact", appUser));
        return criteria.list();
    }


	public ShopFormSchedule getLastShopFormSchedule(UnitRegion ur, Project project) {
		Criteria criteria=baseKernel.getCriteria(ShopFormSchedule.class);
		criteria.add(Restrictions.like("shop", ur.getId()));
		criteria.add(Restrictions.like("project", project.getId()));
		criteria.addOrder(Order.desc("monday"));
        criteria.setMaxResults(1);
		return (ShopFormSchedule) criteria.uniqueResult();
		
		
	}


	public List<Map<String, Object>> getOldReportPromoters(LocalDate startDate,
			LocalDate endDate, int region, int project) {
		
        StringBuilder queryString = new StringBuilder("SELECT * FROM [dbo].[OLD_REPORT_PROMOTER]" +
                " where date>='"+startDate.toString()+
                "' and date<='"+endDate.toString()+"'");

        if (region != 1) {
        	Region reg=getEntity(Region.class,region);
            queryString.append(" and city like '").append(reg.getRegionName()).append("'");
        }
        if (project != 0) {
            queryString.append(" and project=").append(project);
        }
        Query query = baseKernel.createSQLQuery(queryString.toString()).addEntity(OldReportPromoter.class);
        logger.info("1st"+queryString.toString());
		List<OldReportPromoter> list=query.list();
		logger.info("2nd"+queryString.toString());
		logger.info(startDate.toString());
		logger.info(endDate.toString());
		logger.info("result.size="+list.size());
		List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
		for(OldReportPromoter oldRP:list){
			Map<String,Object> row=new HashMap<String,Object>();
			row.put("date", oldRP.getDate());
			row.put("commonCountSell", oldRP.getNotebookCountSell()+oldRP.getPrinterCountSell());
			row.put("commonSumPrice", oldRP.getPrinterSumPrice()+oldRP.getNotebookSumPrice());
			row.put("printerCountSell", oldRP.getPrinterCountSell());
			row.put("printerSumPrice", oldRP.getPrinterSumPrice());
			row.put("notebookCountSell", oldRP.getNotebookCountSell());
			row.put("notebookSumPrice", oldRP.getNotebookSumPrice());
			row.put("project", oldRP.getProject());
			result.add(row);
		}
		return result;
		
	}


	public List<ReportPromoter> getReportPromotersForGraphic(
			LocalDate startDate, LocalDate endDate, int region,
			int project) {
        StringBuilder queryString = new StringBuilder("SELECT * FROM [dbo].[REPORT_PROMOTER]" +
                " where date>='"+startDate.toString()+
                "' and date<='"+endDate.toString()+"'");

        if (region != 1) {
        	Region reg=getEntity(Region.class,region);
            queryString.append(" and city like '").append(reg.getRegionName()).append("'");
        }
        if (project != 0) {
            queryString.append(" and projectId=").append(project);
        }
        Query query = baseKernel.createSQLQuery(queryString.toString()).addEntity(ReportPromoter.class);
        logger.info("1st "+queryString.toString());
		List<ReportPromoter> list=query.list();
		logger.info("2nd "+queryString.toString());
		return list;
	}


	public List<SaleHasPromoStockDTO> getSaleHasPromoStockDTO(
			LocalDate startDate, LocalDate endDate, int region) {
		StringBuilder queryString=new StringBuilder("SELECT ps.bonus,rp.date FROM [dbo].[SaleHasPromoStock] shps "
				+ "join [dbo].[Sale] s on shps.sale=s.id "
				+ "join [dbo].[qfform] f on s.qfform=f.id "
				+ "join [dbo].[REPORT_PROMOTER] rp on rp.flowid=f.Flow_id "
				+ "join [dbo].[PromoStock] ps on shps.promostock=ps.id "
				+ "where date>='"+startDate.toString()
				+ "' and date<='"+endDate.toString()+"' "
						//TODO till bonus 5 is active
                + "and ((shps.value=1 and s.shipment!="+Shipment.BONUS+") or (ps.id=5 and s.shipment="+Shipment.BONUS+")) and rp.state="+ReportPromoter.READY);
		if(region!=1){
        	Region reg=getEntity(Region.class,region);
            queryString.append("and rp.city like '").append(reg.getRegionName()).append("' ");
		}
		Query query = baseKernel.createSQLQuery(queryString.toString()).setResultTransformer(Transformers.aliasToBean(SaleHasPromoStockDTO.class));
		List<SaleHasPromoStockDTO> list=query.list();
		return list;

//		StringBuilder addictive=new StringBuilder("SELECT rp.date,ps.bonus FROM [dbo].[Sale] s "
//				+ "join [dbo].[qfform] f on s.qfform=f.id "
//				+ "join [dbo].[REPORT_PROMOTER] rp on rp.flowid=f.Flow_id "
//				+ "where date>='"+startDate.toString()
//				+ "' and date<='"+endDate.toString()+"' ");
//		if(region!=1){
//        	Region reg=getEntity(Region.class,region);
//        	addictive.append("and rp.city like '").append(reg.getRegionName()).append("' ");
//		}
//		query=baseKernel.createSQLQuery(addictive.toString());
//		int count=(int)query.uniqueResult();
//		for(int i=0;i<count;i++){
//			list.add(new SaleHasPromoStockDTO())
//		}
//		return list;
	}
	
	


}

