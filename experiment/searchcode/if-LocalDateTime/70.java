package by.q64.promo.utils.scheduler;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.q64.promo.dao.BaseRequest;
import by.q64.promo.data.Message;
import by.q64.promo.domain.Activity;
import by.q64.promo.domain.AppUser;
import by.q64.promo.domain.ChatContactUserUser;
import by.q64.promo.domain.ClientSchedule;
import by.q64.promo.domain.Colour;
import by.q64.promo.domain.Flow;
import by.q64.promo.domain.Instance;
import by.q64.promo.domain.Project;
import by.q64.promo.domain.Region;
import by.q64.promo.domain.ShopFormSchedule;
import by.q64.promo.domain.TrastPromoShedule;
import by.q64.promo.domain.UnitActivity;
import by.q64.promo.domain.UnitRegion;
import by.q64.promo.domain.dto.schedule.WeekScheduleDTO;
import by.q64.promo.domain.dto.schedule.WeekScheduleDataDTO;
import by.q64.promo.domain.dto.schedule.WeekScheduleSupervisorDTO;
import by.q64.promo.utils.chat.PromoChat;
import by.q64.promo.utils.mail.EmailManager;









import com.sun.star.auth.InvalidArgumentException;

@Transactional
@Service
public class Scheduler {

    @Autowired
    private BaseRequest baseRequest;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public WeekScheduleDTO getSchedule(int regionId, int project, String dateString, String email) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate monday = LocalDate.parse(dateString, dateTimeFormatter);
        int weekDay = monday.getDayOfWeek().getValue();
        weekDay--;
        monday = monday.minusDays(weekDay);

        logger.info(monday.toString());
        Region region = baseRequest.getEntity(Region.class, regionId);
        AppUser appUser = baseRequest.getUserFromEmail(email);
        List<TrastPromoShedule> trastPromoShedules = getTrastPromoSheduleSEVENDAYSForUser(regionId, monday, project, appUser);
        List<AppUser> promoters = baseRequest.getRegionAppUsers(regionId, AppUser.PROMOTER);
        promoters.add(baseRequest.getEntity(AppUser.class, AppUser.PROMOTER_NONE_ID));
        List<Integer> promoterIds  = getAppUserIds(promoters);
        List<AppUser> supervisors = baseRequest.getListEntity(AppUser.class,"role",AppUser.SUPERVISOR,"active",1);
        supervisors.add(baseRequest.getEntity(AppUser.class, AppUser.SUPERVISOR_NONE_ID));
        List<Integer> supervisorIds  = getAppUserIds(supervisors);
        List<UnitRegion> unitRegions = new ArrayList<UnitRegion>();
        List<Integer> usedShops = new ArrayList<Integer>();
        List<UnitRegion> regionShops = region.getUnitRegions();
        for (UnitRegion shop : regionShops) {
        	Activity activity=baseRequest.getLastActivity(shop.getId(), project);
            if (activity.getActivity() != Activity.NO_ACTIVITY && shop.getActive() == 1) {
                unitRegions.add(shop);
            }
        }
        for(TrastPromoShedule tps : trastPromoShedules){
            UnitRegion ur=baseRequest.getEntity(UnitRegion.class,tps.getShop());
            if(!unitRegions.contains(ur)){
                unitRegions.add(ur);
            }
            if(!usedShops.contains(ur.getId()))
            	usedShops.add(ur.getId());
        }
        WeekScheduleDTO weekScheduleDTO = new WeekScheduleDTO();
        weekScheduleDTO.setPromoters(promoters);
        weekScheduleDTO.setSupervisors(supervisors);
        weekScheduleDTO.setDate(getSevenDays(new Date(Timestamp.valueOf(monday.atStartOfDay()).getTime())));// 7
        weekScheduleDTO.setUsedShops(usedShops);                                                                                                    // days
        Collections.sort(unitRegions, new SortShopComparatorName());
        Collections.sort(unitRegions, new SortShopComparatorShopNetwork());
        weekScheduleDTO.setData(getData(trastPromoShedules, unitRegions, new Date(Timestamp.valueOf(monday.atStartOfDay()).getTime()), appUser.getRole(),
                supervisors, appUser,promoterIds,supervisorIds,project));
        if (logger.isDebugEnabled()) {
            logger.debug("REZULT \n\n\n\n\n\n");
            logger.debug(weekScheduleDTO.toString());
        }
        return weekScheduleDTO;
    }

    public List<Integer> getAppUserIds(List<AppUser> promoters) {
    	List<Integer> userIds = new LinkedList<Integer>();
    	for (AppUser appUser : promoters) {
    		userIds.add(appUser.getId());
		}
		return userIds;
	}

	public List<TrastPromoShedule> getTrastPromoSheduleSEVENDAYSForUser(int regionId, LocalDate monday, int project, AppUser appUser) {
        int role = appUser.getRole();

        List<TrastPromoShedule> trastPromoShedules = new ArrayList<TrastPromoShedule>();

        if (role == AppUser.PROMOTER) {
            trastPromoShedules = baseRequest.getTrastPromoSheduleSEVENDAYS(regionId, monday, project, appUser.getId(), null);
            makeSeen(trastPromoShedules);
        }

        if (role == AppUser.SUPERVISOR) {
            trastPromoShedules = baseRequest.getTrastPromoSheduleSEVENDAYS(regionId, monday, project, null, appUser.getId());
        }

        if ((role == AppUser.MANAGER) || (role == AppUser.HEAD_PROMO_DEPARTMENT) || (role == AppUser.DIRECTOR)
                || (role == AppUser.CLIENT) || (role == AppUser.ADMIN)) {
            trastPromoShedules = baseRequest.getTrastPromoSheduleSEVENDAYS(regionId, monday, project, null, null);
        }

        if (role == AppUser.COORDINATOR) {
            Region region=baseRequest.getEntity(Region.class, regionId);
            if (appUser.getId() == region.getCoordinator()) {
                trastPromoShedules = baseRequest.getTrastPromoSheduleSEVENDAYS(regionId, monday, project, null, null);
            }
        }

        logger.info("USER : " + appUser.toString());
        for (TrastPromoShedule trastPromoShedule : trastPromoShedules) {
            logger.info(trastPromoShedule.toString());
        }

        return trastPromoShedules;
    }

    private void makeSeen(List<TrastPromoShedule> trastPromoShedules) {
        for(TrastPromoShedule tps : trastPromoShedules){
            if(tps.getColour().getId()==TrastPromoShedule.NOT_SEEN)
                tps.setColour(baseRequest.getEntity(Colour.class,TrastPromoShedule.SEEN));
            if(tps.getColour().getId()==TrastPromoShedule.EDITED_NOT_SEEN)
                tps.setColour(baseRequest.getEntity(Colour.class,TrastPromoShedule.EDITED_SEEN));
            baseRequest.saveOrUpdate(tps);
        }
        
        
    }

    @SuppressWarnings("deprecation")
	public List<WeekScheduleDataDTO> getData(List<TrastPromoShedule> trastPromoShedules, List<UnitRegion> unitRegions, Date monday, final int role,
            List<AppUser> supervisors, AppUser appUser, List<Integer> promoterIds, List<Integer> supervisorIds,int projectId) {
        List<WeekScheduleDataDTO> maps = new ArrayList<>();
        int number = 0;
        for (UnitRegion unitRegion : unitRegions) {
            WeekScheduleDataDTO weekScheduleDataDTO = new WeekScheduleDataDTO();
            weekScheduleDataDTO.setAddress(unitRegion.getName());
            weekScheduleDataDTO.setChainName(unitRegion.getUnit().getUnitName());
            weekScheduleDataDTO.setShopId(unitRegion.getId());
            weekScheduleDataDTO.setShopForm(getShopForm(monday,unitRegion,projectId));
            Activity activity = baseRequest.getLastActivity(unitRegion.getId(), projectId);
            UnitActivity unitActivity = baseRequest.getEntity(UnitActivity.class, activity.getActivity());
            weekScheduleDataDTO.setDescription("Магазин: " + unitRegion.getName() + "<br>" + "Категория магазина: "
                    + unitActivity.getActivityName() + "<br>" + "Метро: " + unitRegion.getSubway());
            boolean emptyReportShop = true;
            int supervisorId = unitRegion.getSupervisorId();
            AppUser supervisor = baseRequest.getEntity(AppUser.class, supervisorId);
            String supervisorName = supervisor.getSurname();

            for (int i = 0; i < 7; i++) {
                // Map<String, Object> dayNumber = new HashMap<>();
                Map<String, Object> weekScheduleDayDTO = new HashMap<String, Object>();
                TrastPromoShedule promoShedule = getTPS(monday, i, trastPromoShedules, unitRegion);
                // logger.info("TPS SIZE : " + trastPromoShedules.size());
                if (promoShedule == null) {
                    weekScheduleDayDTO.put("userId", "");
                    switch (i) {
                    case 4:
                        weekScheduleDayDTO.put("start", "16");
                        weekScheduleDayDTO.put("end", "20");
                        break;
                    case 5:
                    case 6:
                        weekScheduleDayDTO.put("start", "12");
                        weekScheduleDayDTO.put("end", "20");
                        break;

                    default:
                        weekScheduleDayDTO.put("start", "");
                        weekScheduleDayDTO.put("end", "");
                        break;
                    }
                    weekScheduleDayDTO.put("id", 0);
                    weekScheduleDayDTO.put("colour", "");
                } else {
                    emptyReportShop = false;

                    supervisorId = promoShedule.getSupervisor();
                    if (supervisorIds.contains(supervisorId)) {
                    	supervisorName = findSupervisor(supervisors, supervisorId).getSurname();
                    } else {
                    	supervisorName = getBannedName(supervisorId);
                    }
                    
                    int promoterId = promoShedule.getPromoter();
                    if (promoterIds.contains(promoterId)) {
                    	weekScheduleDayDTO.put("userId", promoterId);
                    } else {
                    	weekScheduleDayDTO.put("userId", getBannedName(promoterId));
                    }
                    
                    weekScheduleDayDTO.put("start", promoShedule.getStart().getHours());
                    weekScheduleDayDTO.put("end", promoShedule.getStop().getHours());
                    weekScheduleDayDTO.put("id", promoShedule.getId());
                    if (appUser.getRole() != AppUser.CLIENT) {
                        weekScheduleDayDTO.put("colour", promoShedule.getColourToJson());
                    } else {
                        weekScheduleDayDTO.put("colour", "");
                    }

                }
                weekScheduleDataDTO.setDay(i + 1, weekScheduleDayDTO);
            }
            WeekScheduleSupervisorDTO weekScheduleSupervisorDTO = new WeekScheduleSupervisorDTO(supervisorId, supervisorName);
            weekScheduleDataDTO.setSupervisor(weekScheduleSupervisorDTO);

            if ((role == AppUser.MANAGER) || (role == AppUser.MANAGER) || (role == AppUser.HEAD_PROMO_DEPARTMENT) || (role == AppUser.DIRECTOR)
                    || (role == AppUser.CLIENT) || (role == AppUser.ADMIN) || (role == AppUser.COORDINATOR)) {
                weekScheduleDataDTO.setNumber(number++);
                maps.add(weekScheduleDataDTO);
            } else {
                if (!emptyReportShop) {
                    weekScheduleDataDTO.setNumber(number++);
                    maps.add(weekScheduleDataDTO);
                }
            }
        }
        return maps;
    }

    public ShopFormSchedule getShopForm(Date monday,UnitRegion shop,int project) {
        Timestamp mondayTimestamp=Timestamp.from(monday.toInstant());
        Activity activity = baseRequest.getLastActivity(shop.getId(), project);

        List<ShopFormSchedule> form=baseRequest.getShopFormSchedules(shop.getId(),project,mondayTimestamp);
        ShopFormSchedule shopForm;
        if(form.size()==0){
            shopForm=new ShopFormSchedule();
            shopForm.setMonday(mondayTimestamp);
            shopForm.setProject(project);
            shopForm.setShop(shop.getId());
            shopForm.setValue(activity.getPromoForm());
        }
        else
            shopForm=form.get(0);
        return shopForm;
    }

    public String getBannedName(int userId) {
    	AppUser bannedSupervisor = baseRequest.getEntity(AppUser.class, userId);
    	if (bannedSupervisor != null) {
    		return bannedSupervisor.getSurname() + "(x)";
    	} else {
    		return "Пользователь не найден";
    	}
	}

	public AppUser findSupervisor(List<AppUser> supervisors, int supervisorId) {
        for (AppUser appUser : supervisors) {
            if (appUser.getId() == supervisorId) {
                return appUser;
            }
        }
        logger.error("I can not find supervisor id = " + supervisorId);
        for (AppUser appUser : supervisors) {
            logger.error(appUser.toString());
        }
        return new AppUser().setFirstname("").setSurname("");
    }

    private TrastPromoShedule getTPS(Date day, int i, List<TrastPromoShedule> trastPromoShedules, UnitRegion unitRegion) {
        if (trastPromoShedules == null) {
            return null;
        }

        Date date = nextDay(day, i);
        long dl = date.getTime();
        Date date2 = nextDay(date, 1);
        long dl2 = date2.getTime();

        int s = trastPromoShedules.size();
        List<TrastPromoShedule> promoShedulesReturn = new ArrayList<TrastPromoShedule>();

        // List<Integer> tpstoRemove = new LinkedList<Integer>();
        for (int j = 0; j < s; j++) {
            TrastPromoShedule promoShedule = trastPromoShedules.get(j);
            if (promoShedule.getShop() == unitRegion.getId()) {
                long pst = promoShedule.getStart().getTime();
                if ((dl < pst) && (dl2 > pst)) {
                    // tpstoRemove.add(j);
                    promoShedulesReturn.add(promoShedule);
                }
            }
        }
        //
        // for (Integer integer : tpstoRemove) {
        // trastPromoShedules.remove(integer);
        // }
        //
        if (promoShedulesReturn.size() > 0) {
            if (promoShedulesReturn.size() > 1) {
                for (int j = 1; j < promoShedulesReturn.size(); j++) {
                    TrastPromoShedule promoShedule = promoShedulesReturn.get(j);
                    if (promoShedule.getStatus() == 0) {
                        try {
                            baseRequest.delete(promoShedule);
                            logger.info("DELETE " + promoShedule.toString());
                        } catch (Exception e) {
                            logger.error("FAIL DELETE " + promoShedule.toString());
                        }
                    }
                }
            }
            return promoShedulesReturn.get(0);
        } else {
            return null;
        }
    }

    public List<String> getSevenDays(Date date) {
        List<String> strings = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        Date day = new Date(date.getTime());
        for (int i = 0; i < 7; i++) {
            strings.add(dateFormat.format(day));
            day = nextDay(day, 1);
        }
        return strings;
    }

    @Transactional
    public Map<String, Object> addTrastPromoShedule(int id, int shop, int start, int stop, int promouter, int project, String dateSTR,
            int supervisor, String userActorEmail) {

        Map<String, Object> map = null;

        LocalDateTime startTime = null;// = LocalDateTime.of(year, month,
        // dayOfMonth, start, 0);
        LocalDateTime endTime = null;// = LocalDateTime.of(year, month,
        // dayOfMonth, start, 0);
        Date date = null;
        try {
            date = checkParams(dateSTR, start, stop);
            if ((start > -1) && (stop > -1)) {
                // DateTimeFormatter formatter =
                // DateTimeFormatter.ofPattern("HH");
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                Timestamp timestamp = new Timestamp(dateFormat.parse(dateSTR).getTime());
                startTime = timestamp.toLocalDateTime();
                startTime=startTime.plusHours(start);
                endTime = timestamp.toLocalDateTime();
                endTime=endTime.plusHours(stop);
                if(start>=stop){
                    endTime=endTime.plusDays(1);
                }
            } else {
                throw new InvalidArgumentException();
            }
        } catch (Exception ex) {
            map = new HashMap<>();
            map.put("success", false);
            map.put("message", "Не правильные параметры времени, date");
            return map;
        }

        TrastPromoShedule promoShedule = null;
        if (id == 0) {
            promoShedule = new TrastPromoShedule();
        } else {
            promoShedule = baseRequest.getEntity(TrastPromoShedule.class, id);
        }
        
//        List<TrastPromoShedule> tpsCollision = baseRequest.findCollisionTps(promouter, startTime, endTime);         
//        if (tpsCollision.size() > 0) {
//            StringBuilder message = new StringBuilder();
//            for (TrastPromoShedule tps : tpsCollision) {
//                message.append(baseRequest.getEntity(UnitRegion.class, tps.getShop()).getName() + ", ");
//                logger.info(tps.toString());
//            }
//            map = new HashMap<String, Object>();
//            map.put(Message.SUCCESS, false);
//            map.put(Message.MESAGE, "Данный промоутер уже работает в это время (" + message.toString() + ")");
//            return map;
//        }
        LocalDateTime currentDate=LocalDateTime.now();
        //(toTimestamp(date,start).toLocalDateTime().compareTo(LocalDateTime.now())<0)
        if(promouter==AppUser.PROMOTER_NONE_ID&&currentDate.compareTo(startTime)>0){
            delete(promoShedule);
            map=new HashMap<String,Object>();
            Map<String,Object> entity=new HashMap<String,Object>();
            entity.put("userId", "");
            int i=date.getDay();
            switch(i){
            case 0:
            case 6:
                entity.put("start", "12");
                entity.put("end", "20");
                break;
            case 5:
                entity.put("start", "16");
                entity.put("end", "20");
                break;
                default:
                    entity.put("start", "");
                    entity.put("end", "");
            }
            entity.put("id", 0);
            entity.put("colour", "");
            map.put("entity",entity);
        }
        else
            map = updateTrastPromoShedule(shop, startTime, endTime, promouter, project, promoShedule, supervisor, userActorEmail);
        map.put(Message.SUCCESS, true);
        return map;
    }


    private void delete(TrastPromoShedule promoShedule) {
        if(promoShedule.getStatus()==1){
            List<Instance> instance=baseRequest.getListEntity(Instance.class,"schedule",promoShedule.getId());
            List<Flow> flows=baseRequest.getListEntity(Flow.class,"instance",instance.get(0).getId());
            for(Flow flow:flows){
                flow.setAppUser(new AppUser().setId(9));
                flow.setHemReport(null);
                baseRequest.saveOrUpdate(flow);
            }
        }
        baseRequest.delete(promoShedule);
        
    }

    @Transactional
    private Map<String, Object> updateTrastPromoShedule(int shop, LocalDateTime startTime, LocalDateTime stopTime, int promouter, int projectId,
            TrastPromoShedule promoShedule, int supervisor, String userActorEmail) {
        final TrastPromoShedule olDpromoShedule = TrastPromoShedule.clone(promoShedule);
        Project project = baseRequest.getEntity(Project.class, projectId);
        UnitRegion unitRegion = baseRequest.getEntity(UnitRegion.class, shop);            
        Timestamp start = Timestamp.valueOf(startTime);
        Timestamp stop = Timestamp.valueOf(stopTime);
        Map<String, Object> map = new HashMap<>();
        updateTPS(shop, promouter, projectId, promoShedule, project, unitRegion, start, stop, supervisor);
        if (olDpromoShedule.getId() > 1) {
            updateFlows(promoShedule, olDpromoShedule, userActorEmail);
            // color="#ccffcc" замены и изменения
//            promoShedule.setColour(baseRequest.getEntity(Colour.class,TrastPromoShedule.EDITED_NOT_SEEN));
        }
        setColourToShedule(promoShedule);
        TrastPromoShedule next=baseRequest.getMappedScheduleNextPrevWeek(promoShedule); //paint nextWeek schedule cas it will be changed
        if(next!=null){
            if(next.getPromoter()!=promoShedule.getPromoter()){
                if(next.getColour().getId()==TrastPromoShedule.NOT_SEEN||next.getColour().getId()==TrastPromoShedule.EDITED_NOT_SEEN)
                    next.setColour(baseRequest.getEntity(Colour.class,TrastPromoShedule.EDITED_NOT_SEEN));
                if(next.getColour().getId()==TrastPromoShedule.SEEN||next.getColour().getId()==TrastPromoShedule.EDITED_SEEN)
                    next.setColour(baseRequest.getEntity(Colour.class,TrastPromoShedule.EDITED_SEEN));
            }
            else{
                if(next.getColour().getId()==TrastPromoShedule.NOT_SEEN||next.getColour().getId()==TrastPromoShedule.EDITED_NOT_SEEN)
                    next.setColour(baseRequest.getEntity(Colour.class,TrastPromoShedule.NOT_SEEN));
                if(next.getColour().getId()==TrastPromoShedule.SEEN||next.getColour().getId()==TrastPromoShedule.EDITED_SEEN)
                    next.setColour(baseRequest.getEntity(Colour.class,TrastPromoShedule.SEEN));
            }
                baseRequest.saveOrUpdate(next);
        }
        baseRequest.saveOrUpdate(promoShedule);
        map.put("entity", promoShedule);
        return map;
    }

    private void setColourToShedule(TrastPromoShedule promoShedule) {
        if(hasChanged(promoShedule))
            promoShedule.setColour(baseRequest.getEntity(Colour.class,TrastPromoShedule.EDITED_NOT_SEEN));
        else
            promoShedule.setColour(baseRequest.getEntity(Colour.class,TrastPromoShedule.NOT_SEEN));

    }

    private boolean hasChanged(TrastPromoShedule promoShedule) {
        TrastPromoShedule oldTps=baseRequest.getMappedTPSFromPrevWeek(promoShedule);
        if(oldTps==null)
            return false;
        if(oldTps.getPromoter()!=promoShedule.getPromoter())    
            return true;
        return false;
    }

    @Transactional
    private void updateFlows(TrastPromoShedule promoShedule, final TrastPromoShedule olDpromoShedule, String userActorEmail) {
        AppUser promoter = baseRequest.getEntity(AppUser.class, promoShedule.getPromoter());
        AppUser supervisor = baseRequest.getEntity(AppUser.class, promoShedule.getSupervisor());
        List<Instance> instances = baseRequest.getListEntity(Instance.class, "schedule", promoShedule.getId());
        if (instances.size() == 0) {
            return;
        }
        Instance instance = instances.get(0);

        try {
            if (instances.size() > 1) {
                String messageBody = "TPS ID = " + promoShedule.getId();
                for (Instance myInstance : instances) {
                    messageBody += "Instance id " + myInstance.getId();
                }

                EmailManager.send("andrei.ladyka@quadrate64.com", null, "Ошибка с колличеством инстансов", messageBody, null);
            }
        } catch (Exception ex) {
        }

        instance.setStarttimepromouter(promoShedule.getStart());
        instance.setStoptimepromouter(promoShedule.getStop());
        // List<Flow> flows = instance.getFlows();
        List<Flow> flows = baseRequest.getFlowsFromInstance(instance);
        for (Flow flow : flows) {
            AppUser appUser = flow.getAppUser();
            int role = appUser.getRole();

            switch (role) {
            case AppUser.PROMOTER: {
                flow.setAppUser(promoter);
            }
                break;

            case AppUser.SUPERVISOR: {
                flow.setAppUser(supervisor);
            }
                break;
            }

            flow.setAccepted(Flow.ACCEPTED_INBOX);
            flow.setStarttime(promoShedule.getStart());
            flow.setStoptime(promoShedule.getStop());

            if ((promoter.getId() == AppUser.PROMOTER_NONE_ID) || (supervisor.getId() == AppUser.SUPERVISOR_NONE_ID)) {
                flow.setHemReport(null);
            }

            baseRequest.saveOrUpdate(flow);
        }
        baseRequest.saveOrUpdate(instance);

        AppUser promoterWas = baseRequest.getEntity(AppUser.class, olDpromoShedule.getPromoter());
        AppUser supervisorWas = baseRequest.getEntity(AppUser.class, olDpromoShedule.getSupervisor());
        String was = getMessageText(olDpromoShedule, promoterWas, supervisorWas);
        String now = getMessageText(promoShedule, promoter, supervisor);
        sendMessage(was, now, userActorEmail);
    }

    @Transactional
    private void sendMessage(String was, String now, String userActorEmail) {
        AppUser userActor = baseRequest.getUserFromEmail(userActorEmail);
        String message = "Системное сообщение: Изменено рассписание: " + "\n Было :" + was + "\nСтало : " + now + "\nИзменил " + userActor.getFullName()
                + "\n\n";
        List<AppUser> appUsers = new ArrayList<AppUser>();
        List<AppUser> appUsersLEAD_PROJECT_MANAGER = baseRequest.getListEntity(AppUser.class, "role", AppUser.MANAGER);
        appUsers.addAll(appUsersLEAD_PROJECT_MANAGER);
        List<AppUser> appUsersMANAGER = baseRequest.getListEntity(AppUser.class, "role", AppUser.MANAGER);
        appUsers.addAll(appUsersMANAGER);
        List<AppUser> appUsersHEAD_PROMO_DEPARTMENT = baseRequest.getListEntity(AppUser.class, "role", AppUser.HEAD_PROMO_DEPARTMENT);
        appUsers.addAll(appUsersHEAD_PROMO_DEPARTMENT);
        sendMessageToUsers(userActorEmail, appUsers, message);

    }

    @Transactional
    private void sendMessageToUsers(String userActorEmail, List<AppUser> appUsers, String message) {
        PromoChat promoChat = new PromoChat(userActorEmail);
        for (AppUser appUser : appUsers) {
            try {
                ChatContactUserUser chatContactUserUser = promoChat.getRelation(appUser.getId());
                promoChat.addMessage(message, chatContactUserUser.getId());
            } catch (java.lang.NullPointerException ex) {

            }

        }

    }

    @Transactional
    private String getMessageText(TrastPromoShedule promoShedule, AppUser promoter, AppUser supervisor) {
        UnitRegion unitRegion = baseRequest.getEntity(UnitRegion.class, promoShedule.getShop());
        return "\nМагазин : " + unitRegion.getName() + "\n Промоутер : " + promoter.getFullName() + "\n Супервайзер : " + supervisor.getFullName()
                + "\n Время начала : " + promoShedule.getStart().toString() + "\n Время окончания : " + promoShedule.getStop().toString();

    }

    @Transactional
    private TrastPromoShedule updateTPS(int shop, int promouter, int projectId, TrastPromoShedule promoShedule, Project project, UnitRegion unitRegion,
            Timestamp start, Timestamp stop, int supervisor) {
        promoShedule.setPromoter(promouter);
        promoShedule.setSupervisor(supervisor);
        promoShedule.setProject(projectId);
        promoShedule.setFormtemplate(project.getFormTemplateId());
        promoShedule.setStart(start);
        // promoShedule.setStatus(0);
        promoShedule.setStop(stop);
        promoShedule.setShop(shop);
        promoShedule.setShopnetwork(unitRegion.getUnit().getId());
        promoShedule.setRegion(unitRegion.getRegion().getId());
        return promoShedule;
    }

    Timestamp toTimestamp(Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, hours);
        long time = calendar.getTimeInMillis();
        Timestamp timestamp = new Timestamp(time);
        return timestamp;
    }

    public Date nextDay(Date date, int i) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, i); // minus number would decrement the days
        return cal.getTime();
    }

    // две функціональності в одном методе

    /**
     * @param dateSTR
     * @param start
     * @param stop
     * @return
     * @throws ParseException
     * @throws InvalidArgumentException
     */
    @Deprecated Date checkParams(String dateSTR, int start, int stop) throws ParseException, InvalidArgumentException {
        if ((start > -1) && (stop > -1)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Date date = dateFormat.parse(dateSTR);
            return date;
        } else {
            throw new InvalidArgumentException();
        }
    }

    public boolean canEditShedulle(String email) {
        AppUser appUser = baseRequest.getUserFromEmail(email);
        int role = appUser.getRole();
        if ((role == AppUser.COORDINATOR) || (role == AppUser.MANAGER) || (role == AppUser.HEAD_PROMO_DEPARTMENT)) {
            return true;
        }
        return false;
    }

    public Map<String, Object> updateTPSWeekSuperviser(List<Integer> days, int supervisor, String userActorEmail) {
        for (Integer integer : days) {
            if (integer != 0) {
                TrastPromoShedule tps = baseRequest.getEntity(TrastPromoShedule.class, integer);
                final TrastPromoShedule olDpromoShedule = TrastPromoShedule.clone(tps);
                if (supervisor == 0) {
                    tps.setSupervisor(AppUser.SUPERVISOR_NONE_ID);
                } else {
                    tps.setSupervisor(supervisor);
                }
                if ((olDpromoShedule.getId() > 1) && (olDpromoShedule.getStatus() != 0)) {
                    updateFlows(tps, olDpromoShedule, userActorEmail);
                    // color="#ccffcc" замены и изменения
                    tps.setColour(baseRequest.getEntity(Colour.class,TrastPromoShedule.EDITED_NOT_SEEN));
                }

                baseRequest.saveOrUpdate(tps);
            }
        }
        return new HashMap<String, Object>();
    }

    public String copyWeek(int region, int project,LocalDateTime date) {
        LocalDate localDateMonday = getCurrentMonday(date);
        LocalDate prevMonday = localDateMonday.minusDays(7);
        //Ментя терзают смутные сомненія по поводу правільності работы этого, 
        List<TrastPromoShedule> trastPromoShedulesCurrentWeek = baseRequest.getTSchedules(localDateMonday.atStartOfDay(), null, project, region);
        if (trastPromoShedulesCurrentWeek.size() != 0) {
            for (TrastPromoShedule trastPromoShedule : trastPromoShedulesCurrentWeek) {
                logger.info(trastPromoShedule.toString());
            }
            return "В этой неделе уже присутствует расписание.";
        }

        logger.info("==============================================");
        //..і тут 
        //copyForms(prevMonday,project,region);
        List<TrastPromoShedule> trastPromoShedulesLastWeek = baseRequest.getTSchedules(prevMonday.atStartOfDay(), localDateMonday.atStartOfDay(),
                project, region);
        for (TrastPromoShedule trastPromoShedule : trastPromoShedulesLastWeek) {
            if(baseRequest.getEntity(UnitRegion.class,trastPromoShedule.getShop()).getActive()==1){
            TrastPromoShedule trastPromoSheduleNew = new TrastPromoShedule(trastPromoShedule);

            LocalTime localTimeStop = trastPromoShedule.getStop().toLocalDateTime().toLocalTime();
            LocalTime localTimeStart = trastPromoShedule.getStart().toLocalDateTime().toLocalTime();
            // TODO potential bug
            if (localTimeStop.equals(localTimeStart)) {
                continue;
            }

            AppUser promoter = baseRequest.getEntity(AppUser.class, trastPromoShedule.getPromoter());
            if (promoter.getActive() == 0) {
                trastPromoSheduleNew.setPromoter(AppUser.PROMOTER_NONE_ID);
            }
            AppUser supervisor = baseRequest.getEntity(AppUser.class, trastPromoShedule.getSupervisor());
            if (supervisor.getActive() == 0) {
                trastPromoSheduleNew.setSupervisor(AppUser.SUPERVISOR_NONE_ID);
            }
            trastPromoSheduleNew.setColour(baseRequest.getEntity(Colour.class,TrastPromoShedule.FROM_PREV_WEEK));
            // trastPromoSheduleNew.setColour("background-color: #fcfbb5;");

            LocalDateTime start = trastPromoShedule.getStart().toLocalDateTime().plusDays(7);
            trastPromoSheduleNew.setStart(Timestamp.valueOf(start));

            // this code fix bad times
            LocalDateTime stop = trastPromoShedule.getStop().toLocalDateTime().plusDays(7);

            trastPromoSheduleNew.setStop(Timestamp.valueOf(stop));
            baseRequest.save(trastPromoSheduleNew);
            }
        }

        return "Cкопировалось";
    }

    private void copyForms(LocalDate prevMonday,int project,int region) {
        Timestamp ts=Timestamp.valueOf(LocalDateTime.of(prevMonday, LocalTime.of(0, 0)));
        List<ShopFormSchedule> forms=baseRequest.getShopFormSchedules(0, project, ts);
        for(ShopFormSchedule form : forms){
        	if(baseRequest.getEntity(UnitRegion.class, form.getShop()).getRegion().getId()==region){
        		ShopFormSchedule newForm=new ShopFormSchedule();
        		newForm.setShop(form.getShop());
        		newForm.setProject(project);
        		newForm.setMonday(Timestamp.valueOf(LocalDateTime.of(prevMonday.plusWeeks(1), LocalTime.of(0, 0))));
        		newForm.setValue(form.getValue());
        		baseRequest.saveOrUpdate(newForm);
        	}
        }
    }

    public LocalDate getCurrentMonday(LocalDateTime localDateTime) {
        return getCurrentMonday(localDateTime.toLocalDate());
    }

    private LocalDate getCurrentMonday(LocalDate localDate) {
        int weekDay = localDate.getDayOfWeek().getValue();
        weekDay--;
        return localDate.minusDays(weekDay);
    }

    public void editShopForm(int id,int shop,int project,long monday,int value){
        Timestamp ts=new Timestamp(monday);
        ShopFormSchedule shopFormSchedule=baseRequest.getEntity(ShopFormSchedule.class,id);
        if(shopFormSchedule==null){
            shopFormSchedule=new ShopFormSchedule();
            shopFormSchedule.setProject(project);
            shopFormSchedule.setMonday(ts);
            shopFormSchedule.setShop(shop);
        }
        shopFormSchedule.setValue(value);
        baseRequest.saveOrUpdate(shopFormSchedule);
        
    }
}

