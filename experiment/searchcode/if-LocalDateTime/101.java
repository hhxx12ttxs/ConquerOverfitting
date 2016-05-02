package by.q64.promo.utils.scheduler;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sun.star.auth.InvalidArgumentException;

import by.q64.promo.dao.BaseRequest;
import by.q64.promo.data.Message;
import by.q64.promo.domain.Activity;
import by.q64.promo.domain.AppUser;
import by.q64.promo.domain.ClientSchedule;
import by.q64.promo.domain.Colour;
import by.q64.promo.domain.Project;
import by.q64.promo.domain.Region;
import by.q64.promo.domain.TrastPromoShedule;
import by.q64.promo.domain.UnitActivity;
import by.q64.promo.domain.UnitRegion;
import by.q64.promo.domain.dto.schedule.WeekScheduleDTO;
import by.q64.promo.domain.dto.schedule.WeekScheduleDataDTO;
import by.q64.promo.domain.dto.schedule.WeekScheduleSupervisorDTO;

@Service
public class ClientScheduleService {

    @Autowired
    BaseRequest baseRequest;

    @Autowired
    Scheduler scheduler;

    Logger logger = LoggerFactory.getLogger(getClass());

    public WeekScheduleDTO getSchedule(int regionId, int project, String dateString, String email) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate monday = LocalDate.parse(dateString, dateTimeFormatter);
        int weekDay = monday.getDayOfWeek().getValue();
        weekDay--;
        monday = monday.minusDays(weekDay);

        logger.info(monday.toString());
        Region region = baseRequest.getEntity(Region.class, regionId);
        AppUser appUser = baseRequest.getUserFromEmail(email);
        
        List<ClientSchedule> trastPromoShedules = getTrastPromoSheduleSEVENDAYSForUser(regionId, monday, project, appUser);
                
        List<AppUser> promoters = baseRequest.getRegionAppUsers(regionId, AppUser.PROMOTER);
        promoters.add(baseRequest.getEntity(AppUser.class, AppUser.PROMOTER_NONE_ID));
        List<Integer> promoterIds = scheduler.getAppUserIds(promoters);
        List<AppUser> supervisors = baseRequest.getRegionAppUsers(regionId, AppUser.SUPERVISOR);
        supervisors.add(baseRequest.getEntity(AppUser.class, AppUser.SUPERVISOR_NONE_ID));
        List<Integer> supervisorIds = scheduler.getAppUserIds(supervisors);
        List<UnitRegion> unitRegions = new ArrayList<UnitRegion>();
        List<Integer> usedShops=new ArrayList<Integer>();
        List<UnitRegion> regionShops = region.getUnitRegions();
        for (UnitRegion shop : regionShops) {
            if (baseRequest.getLastActivity(shop.getId(), project).getActivity() != Activity.NO_ACTIVITY && shop.getActive() == 1) {
                unitRegions.add(shop);
            }
        }
        for(ClientSchedule tps : trastPromoShedules){
            UnitRegion ur=baseRequest.getEntity(UnitRegion.class,tps.getShop());
            if(!unitRegions.contains(ur)){
                unitRegions.add(ur);
            }
            if(!usedShops.contains(ur.getId()))
            	usedShops.add(ur.getId());
        }
        WeekScheduleDTO weekScheduleDTO = new WeekScheduleDTO();
        weekScheduleDTO.setPromoters(promoters);
        weekScheduleDTO.setEdited(checkIfEdited(regionId,project,monday,appUser));
        weekScheduleDTO.setSupervisors(supervisors);
        weekScheduleDTO.setUsedShops(usedShops);
        weekScheduleDTO.setDate(scheduler.getSevenDays(new Date(Timestamp.valueOf(monday.atStartOfDay()).getTime())));// 7
        // days
        Collections.sort(unitRegions, new SortShopComparatorName());
        Collections.sort(unitRegions, new SortShopComparatorShopNetwork());
        weekScheduleDTO.setData(getData(trastPromoShedules, unitRegions, new Date(Timestamp.valueOf(monday.atStartOfDay()).getTime()), appUser.getRole(),
                supervisors, appUser, promoterIds, supervisorIds,project));
        if (logger.isDebugEnabled()) {
            logger.debug("REZULT \n\n\n\n\n\n");
            logger.debug(weekScheduleDTO.toString());
        }
        return weekScheduleDTO;
    }

    private boolean checkIfEdited(int regionId, int project, LocalDate monday, AppUser appUser) {
        if(appUser.getRole()==AppUser.MANAGER||appUser.getRole()==AppUser.HEAD_PROMO_DEPARTMENT){
            List<ClientSchedule> edited=baseRequest.getEditedClientSchedule(monday,regionId,project);
            if(edited.size()>0)
                return true;
        }
        else return false;
        return false;
    }

    public List<ClientSchedule> getTrastPromoSheduleSEVENDAYSForUser(int regionId, LocalDate monday, int project, AppUser appUser) {
        int role = appUser.getRole();

        List<ClientSchedule> trastPromoShedules = new ArrayList<ClientSchedule>();

        if ((role == AppUser.MANAGER) || (role == AppUser.HEAD_PROMO_DEPARTMENT) || (role == AppUser.DIRECTOR)
                || (role == AppUser.CLIENT) || (role == AppUser.ADMIN)) {
            trastPromoShedules = baseRequest.getSheduleSEVENDAYS(regionId, monday, project, null, null,role);
        }

        logger.info("USER : " + appUser.toString());
        for (ClientSchedule trastPromoShedule : trastPromoShedules) {
            logger.info(trastPromoShedule.toString());
        }

        return trastPromoShedules;
    }
//<<<<<<< HEAD

    @SuppressWarnings("deprecation")
    public List<WeekScheduleDataDTO> getData(List<ClientSchedule> trastPromoShedules, List<UnitRegion> unitRegions, Date monday, final int role,
            List<AppUser> supervisors, AppUser appUser, List<Integer> promoterIds, List<Integer> supervisorIds,int projectId) {
        List<WeekScheduleDataDTO> maps = new ArrayList<>();
        int number = 0;
        for (UnitRegion unitRegion : unitRegions) {
            WeekScheduleDataDTO weekScheduleDataDTO = new WeekScheduleDataDTO();
            weekScheduleDataDTO.setAddress(unitRegion.getName());
            weekScheduleDataDTO.setChainName(unitRegion.getUnit().getUnitName());
            weekScheduleDataDTO.setShopId(unitRegion.getId());
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
                ClientSchedule promoShedule = getTPS(monday, i, trastPromoShedules, unitRegion);
                // logger.info("TPS SIZE : " + trastPromoShedules.size());
                if (promoShedule == null) {
                    weekScheduleDayDTO.put("userId", "");
                    switch (i) {
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
                        supervisorName = scheduler.findSupervisor(supervisors, supervisorId).getSurname();
                    } else {
                        supervisorName = scheduler.getBannedName(supervisorId);
                    }

                    int promoterId = promoShedule.getPromoter();
                    if (promoterIds.contains(promoterId)) {
                        weekScheduleDayDTO.put("userId", promoterId);
                    } else {
                        weekScheduleDayDTO.put("userId", scheduler.getBannedName(promoterId));
                    }

                    weekScheduleDayDTO.put("start", promoShedule.getStart().getHours());
                    weekScheduleDayDTO.put("end", promoShedule.getStop().getHours());
                    weekScheduleDayDTO.put("id", promoShedule.getId());
                    weekScheduleDayDTO.put("colour", promoShedule.getColourToJson());

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

    private ClientSchedule getTPS(Date day, int i, List<ClientSchedule> trastPromoShedules, UnitRegion unitRegion) {
        if (trastPromoShedules == null) {
            return null;
        }

        Date date = scheduler.nextDay(day, i);
        long dl = date.getTime();
        Date date2 = scheduler.nextDay(date, 1);
        long dl2 = date2.getTime();

        int s = trastPromoShedules.size();
        List<ClientSchedule> promoShedulesReturn = new ArrayList<ClientSchedule>();

        // List<Integer> tpstoRemove = new LinkedList<Integer>();
        for (int j = 0; j < s; j++) {
            ClientSchedule promoShedule = trastPromoShedules.get(j);
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
                    ClientSchedule promoShedule = promoShedulesReturn.get(j);
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

    public String copyWeek(int region, int project, LocalDateTime localDateTime) {
        LocalDate localDateMonday = scheduler.getCurrentMonday(localDateTime);

        List<ClientSchedule> shedulesCurrentWeek = baseRequest.getCSchedules(localDateMonday.atStartOfDay(), localDateMonday.plusWeeks(1).atStartOfDay(), project, region,0);
        if (shedulesCurrentWeek.size() != 0) {
            for (ClientSchedule trastPromoShedule : shedulesCurrentWeek) {
                logger.info(trastPromoShedule.toString());
            }
            return "В этой неделе уже присутствует рассписание.";
        }

        logger.info("==============================================");
        List<TrastPromoShedule> trastPromoShedulesLastWeek = baseRequest.getTSchedules(localDateMonday.atStartOfDay(), localDateMonday.plusWeeks(1).atStartOfDay(),
                project, region);
        for (TrastPromoShedule trastPromoShedule : trastPromoShedulesLastWeek) {
            ClientSchedule clientSchedule = new ClientSchedule(trastPromoShedule);
            setColourToSchedule(clientSchedule);
            clientSchedule.setState(ClientSchedule.created);
            baseRequest.saveOrUpdate(clientSchedule);
        }

        return "Cкопировалось";
    }

    private void setColourToSchedule(ClientSchedule schedule) {
        if(hasChanged(schedule))
            schedule.setColour(baseRequest.getEntity(Colour.class,TrastPromoShedule.EDITED_SEEN));
        else
            schedule.setColour(baseRequest.getEntity(Colour.class,TrastPromoShedule.SEEN));
    }

    private boolean hasChanged(ClientSchedule schedule) {
        ClientSchedule oldTps=baseRequest.getMappedClientScheduleFromPrevWeek(schedule);
        if(oldTps==null)
            return false;
        if(oldTps.getPromoter()!=schedule.getPromoter())    
            return true;
        return false;
    }

    public boolean canEditShedulle(String email) {
        AppUser appUser = baseRequest.getUserFromEmail(email);
        int role = appUser.getRole();
        if ((role == AppUser.MANAGER) || (role == AppUser.HEAD_PROMO_DEPARTMENT)) {
            return true;
        }
        return false;
    }

    public Map<String, Object> addClientShedule(int id, int shop, int start, int stop, int promouter, int project, String dateSTR,
            int supervisor, String userActorEmail) {
        Map<String, Object> map = null;

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        Date date = null;
        try {
            date = scheduler.checkParams(dateSTR, start, stop);
            if ((start > -1) && (stop > -1)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                Timestamp timestamp = new Timestamp(dateFormat.parse(dateSTR).getTime());
                startTime = timestamp.toLocalDateTime();
                startTime=startTime.plusHours(start);
                endTime = timestamp.toLocalDateTime();
                startTime=endTime.plusHours(stop);
                if(stop<=start){
                    startTime=endTime.plusDays(1);
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

        ClientSchedule promoShedule = null;
        if (id == 0) {
            promoShedule = new ClientSchedule();
            } else {
            promoShedule = baseRequest.getEntity(ClientSchedule.class, id);
            promoShedule.setState(ClientSchedule.deleted);
            baseRequest.saveOrUpdate(promoShedule);
            promoShedule=new ClientSchedule();
        }
        if(promouter==AppUser.PROMOTER_NONE_ID){
            promoShedule.setState(ClientSchedule.deleted);
//            baseRequest.delete(promoShedule);
            map=new HashMap<String,Object>();
            Map<String,Object> entity=new HashMap<String,Object>();
            entity.put("userId", "");

            entity.put("start", "");
            entity.put("end", "");
            entity.put("id", 0);
            entity.put("colour", "");
            map.put("entity",entity);
        }
        else
            map = updateClientShedule(shop, startTime, endTime, promouter, project, promoShedule, supervisor, userActorEmail);
        map.put(Message.SUCCESS, true);
        return map;
    }


    @Transactional
    private Map<String, Object> updateClientShedule(int shop, LocalDateTime startTime, LocalDateTime stopTime, int promouter, int projectId,
            ClientSchedule promoShedule, int supervisor, String userActorEmail) {
        Project project = baseRequest.getEntity(Project.class, projectId);
        UnitRegion unitRegion = baseRequest.getEntity(UnitRegion.class, shop);
        Timestamp start = Timestamp.valueOf(startTime);
        Timestamp stop = Timestamp.valueOf(stopTime);
        Map<String, Object> map = new HashMap<>();
        updateClientShedule(shop, promouter, projectId, promoShedule, project, unitRegion, start, stop, supervisor);
        setColourToSchedule(promoShedule);
        ClientSchedule next=baseRequest.getMappedClientScheduleNextPrevWeek(promoShedule); //paint nextWeek schedule cas it will be changed
        if(next!=null){
            if(next.getPromoter()!=promoShedule.getPromoter())
                next.setColour(baseRequest.getEntity(Colour.class,TrastPromoShedule.EDITED_SEEN));
            else
                next.setColour(baseRequest.getEntity(Colour.class,TrastPromoShedule.SEEN));
            baseRequest.saveOrUpdate(next);
        }
        baseRequest.saveOrUpdate(promoShedule);
        map.put("entity", promoShedule);
        return map;
    }

    public Map<String, Object> updateTPSWeekSuperviser(List<Integer> days, int supervisor, String userActorEmail) {
        for (Integer integer : days) {
            if (integer != 0) {
                ClientSchedule clientSchedule = baseRequest.getEntity(ClientSchedule.class,integer);
                if (supervisor == 0) {
                    clientSchedule.setSupervisor(AppUser.SUPERVISOR_NONE_ID);
                } else {
                    clientSchedule.setSupervisor(supervisor);
                }

                baseRequest.saveOrUpdate(clientSchedule);
            }
        }
        return new HashMap<String, Object>();
    }
    
    @Transactional
    private ClientSchedule updateClientShedule(int shop, int promouter, int projectId, ClientSchedule promoShedule, Project project, UnitRegion unitRegion,
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
        promoShedule.setState(ClientSchedule.created);
        return promoShedule;
    }

    public Map<String, List<ClientSchedule>> commitClientSchedule(int region, int project,LocalDateTime date, String name) {
        LocalDate monday=scheduler.getCurrentMonday(date);
        Map<String,List<ClientSchedule>> map=baseRequest.getCSchedulesForCommit(monday.atStartOfDay(), monday.plusWeeks(1).atStartOfDay(), project, region);
        List<ClientSchedule> created=map.get("created");
        for(ClientSchedule cell:created){
            cell.setState(ClientSchedule.commited);
            baseRequest.saveOrUpdate(cell);
        }
        List<ClientSchedule> deleted=map.get("deleted");
        for(ClientSchedule cell:deleted){
            baseRequest.delete(cell);
        }
        return map;
    }

}

