package by.q64.promo.utils.form;

import by.q64.promo.dao.BaseRequest;
import by.q64.promo.data.Action;
import by.q64.promo.data.Situation;
import by.q64.promo.domain.*;
import by.q64.promo.excelgen.service.promoter.source.PromoterSalesContainer;
import by.q64.promo.service.NaireService;
import by.q64.promo.service.PromoterSalaryService;
import by.q64.promo.service.report.utils.ActualLaptopAndPrintersManager;
import by.q64.promo.service.report.utils.ShipmentSourceSpecImpl;
import by.q64.promo.utils.form.domain.CategoryShipmentWithShipments;
import by.q64.promo.utils.form.domain.ShipmentForm;
import by.q64.promo.utils.form.promoter.*;
import by.q64.promo.utils.promoform.PromoterFormService;
import by.q64.promo.utils.workflow.AndrewService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sun.star.embed.Actions;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Transactional
@Service
public class ReportConverter {

    @Autowired
    private BaseRequest baseRequest;

    @Autowired
    private FiltrService filtrService;

    @Autowired
    private FormPromoter formPromoter;
    

    @Autowired
    private NaireService naireService;

    @Autowired
    private WorkTime workTime;

    @Autowired
    private InStoskOrNo inStoskOrNo;
    
    @Autowired
    PromoterSalaryService promoSalaryService;
    

    @Autowired
    by.q64.promo.utils.workflow.AndrewService service;

    @Autowired
    private Sales sales;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
	private PromoterFormService promoterFormService;
    

    public ReportPromoter convertPromoter(Flow flow, String email) {

        ReportPromoter reportPromoter = new ReportPromoter();
        try {

            AppUser actor = baseRequest.getUserFromEmail(email);
            QFForm form = flow.getQfforms().get(0);
            Instance instance = baseRequest.getEntity(Instance.class, flow.getInstance());
            Project project = instance.getProject();
            TrastPromoShedule trastPromoShedule = baseRequest.getEntity(TrastPromoShedule.class, instance.getSchedule());
            AppUser promoter = baseRequest.getEntity(AppUser.class, trastPromoShedule.getPromoter());
            AppUser supervisor = baseRequest.getEntity(AppUser.class, trastPromoShedule.getSupervisor());
            LocalDateTime localDateTime = trastPromoShedule.getStart().toLocalDateTime();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            UnitRegion shop = baseRequest.getEntity(UnitRegion.class, trastPromoShedule.getShop());
            Activity lastActivity = baseRequest.getLastActivity(shop.getId(), project.getId());
            UnitActivity unitActivity = baseRequest.getEntity(UnitActivity.class, lastActivity.getActivity());

            Unit shopNetwork = shop.getUnit();
            Region region = shop.getRegion();
            reportPromoter.setFlowid(flow.getId());
            reportPromoter.setCity(region.getRegionName());
            reportPromoter.setPartner(shopNetwork.getUnitName());
            reportPromoter.setStoreAddress(shop.getName());
            reportPromoter.setCategoryShop(unitActivity.getActivityName());
            reportPromoter.setDate(dateFormatter.format(localDateTime));
            reportPromoter.setPromoterId(promoter.getId());
            reportPromoter.setProjectId(project.getId());
            List<QFComplexData> complexDatas = form.getQfcomplexData();

            String timework = "";
            Float countHour = null;
            QFComplexData onSchedule = workTime.getOnSchedule(complexDatas, form, actor);
            if (onSchedule.getDataValue().length() < 2) {

                LocalTime startWork = LocalTime.of(Integer.valueOf(workTime.getStartWorkhh(complexDatas, form, actor).getDataValue()),
                        Integer.valueOf(workTime.getStartWorkmm(complexDatas, form, actor).getDataValue()));
                LocalTime stopWork = LocalTime.of(Integer.valueOf(workTime.getEndWorkHH(complexDatas, form, actor).getDataValue()),
                        Integer.valueOf(workTime.getEndWorkmm(complexDatas, form, actor).getDataValue()));
                timework = timeFormatter.format(startWork) + " - " + timeFormatter.format(stopWork);
                float count=stopWork.getHour()-startWork.getHour();
                countHour = ((float)ChronoUnit.MINUTES.between(startWork, stopWork))/60f;
                if(count<=0){
                    countHour+=24;
                }
            } else {
                LocalDateTime startWork = trastPromoShedule.getStart().toLocalDateTime();
                LocalDateTime stopWork = trastPromoShedule.getStop().toLocalDateTime();
                timework = timeFormatter.format(startWork) + " - " + timeFormatter.format(stopWork);
                float count=stopWork.getHour()-startWork.getHour();
                countHour = ((float)ChronoUnit.MINUTES.between(startWork, stopWork))/60f;
                if(count<=0){
                    countHour+=24;
                }
            }

            reportPromoter.setCountHour(countHour.toString());
            reportPromoter.setPromoter(promoter.getSurname());
            AppUser coordinator = baseRequest.getEntity(AppUser.class, region.getCoordinator());
            reportPromoter.setCoordinator(coordinator.getSurname());
            reportPromoter.setSupervisor(supervisor.getSurname());
            reportPromoter.setCommentTime(workTime.getComment(complexDatas, form, actor).getDataValue());
            String ans1 = naireService.setPromoterAnswers(reportPromoter, email, flow);
            Integer rate=region.getRate();
            reportPromoter.setRate(rate.toString());

            Float sellPlanCommon = getCooficient(project, unitActivity, ProductType.FULL,region.getId());
            Float sellPlanNotebook = getCooficient(project, unitActivity, ProductType.NOTEBOOK,region.getId());
            Float sellPlanPrinter = getCooficient(project, unitActivity, ProductType.PRINTER,region.getId());

            reportPromoter.setSellPlanCommon(sellPlanCommon.toString());
            reportPromoter.setSellPlanNotebook(sellPlanNotebook.toString());
            reportPromoter.setSellPlanPrinter(sellPlanPrinter.toString());

            // Общие продажи
            // Количество продаж
            reportPromoter.setCommonCountSell("0");
            // Сумма цен всех проданных моделей
            reportPromoter.setCommonSumPrice("0");
            reportPromoter.setNotebookCountSell("0");
            reportPromoter.setNotebookSumPrice("0");
            reportPromoter.setPrinterCountSell("0");
            reportPromoter.setPrinterSumPrice("0");
            reportPromoter.setPlanFactCommon("0");
            reportPromoter.setPlanFactNotebook("0");
            reportPromoter.setPlanFactPrinter("0");

            reportPromoter.setBonus("0");

            Integer penalty = getFullPenaltyOfInstance(instance);
            reportPromoter.setPenalty(penalty.toString());
//            Double salaryPromoter = getSalary(bonus, countHour, rate, penalty);
            reportPromoter.setPromoterGet("0");

            QFComplexData complexData_IN_STOCK = getFlag(FormPromoter.IN_STOCK, complexDatas, form, actor);
            if (complexData_IN_STOCK.getDataValue().length() < 2) {
                reportPromoter.setNotebookHave(getListShipments(1005, InStoskOrNo.IN_STOCK, complexDatas));
                reportPromoter.setMonoHave(getListShipments(1007, InStoskOrNo.IN_STOCK, complexDatas));
                reportPromoter.setBlockHave(getListShipments(1006, InStoskOrNo.IN_STOCK, complexDatas));
                reportPromoter.setPrinterHave(getListShipments(1008, InStoskOrNo.IN_STOCK, complexDatas));
                reportPromoter.setAccessoryHave(getListShipments(1009, InStoskOrNo.IN_STOCK, complexDatas));
                reportPromoter.setCartridgeHave(getListShipments(1010, InStoskOrNo.IN_STOCK, complexDatas));
                reportPromoter.setPOSHave(getListShipments(1011, InStoskOrNo.IN_STOCK, complexDatas));

            } else {
                reportPromoter.setNotebookHave("");
                reportPromoter.setMonoHave("");
                reportPromoter.setBlockHave("");
                reportPromoter.setPrinterHave("");
                reportPromoter.setAccessoryHave("");
                reportPromoter.setCartridgeHave("");
                reportPromoter.setPOSHave("");
            }

            QFComplexData complexData_NO = getFlag(FormPromoter.NO, complexDatas, form, actor);
            if (complexData_NO.getDataValue().length() < 2) {
                reportPromoter.setNotebookNotHave(getListShipments(1005, InStoskOrNo.NO, complexDatas));
                reportPromoter.setMonoNotHave(getListShipments(1007, InStoskOrNo.NO, complexDatas));
                reportPromoter.setBlockNotHave(getListShipments(1006, InStoskOrNo.NO, complexDatas));
                reportPromoter.setPrinterNotHave(getListShipments(1008, InStoskOrNo.NO, complexDatas));
                reportPromoter.setAccessoryNotHave(getListShipments(1009, InStoskOrNo.NO, complexDatas));
                reportPromoter.setCartridgeNotHave(getListShipments(1010, InStoskOrNo.NO, complexDatas));
                reportPromoter.setPOSNotHave(getListShipments(1011, InStoskOrNo.NO, complexDatas));

            } else {
                reportPromoter.setNotebookNotHave("");
                reportPromoter.setMonoNotHave("");
                reportPromoter.setBlockNotHave("");
                reportPromoter.setPrinterNotHave("");
                reportPromoter.setAccessoryNotHave("");
                reportPromoter.setCartridgeNotHave("");
                reportPromoter.setPOSNotHave("");
            }

//            reportPromoter.setHaveOrNotAccessories("");
//            reportPromoter.setHaveOrNotCartridges("");
//            reportPromoter.setHaveOrNotPos("test");

            switch (localDateTime.getDayOfWeek().getValue()) {
            case 1:
                reportPromoter.setTraficMon(ans1);
                reportPromoter.setDateMon(timework);
                break;
            case 2:
                reportPromoter.setTraficTue(ans1);
                reportPromoter.setDateTue(timework);
                break;
            case 3:
                reportPromoter.setTraficWed(ans1);
                reportPromoter.setDateWed(timework);
                break;
            case 4:
                reportPromoter.setTraficThu(ans1);
                reportPromoter.setDateThu(timework);
                break;
            case 5:
                reportPromoter.setTraficFri(ans1);
                reportPromoter.setDateFri(timework);
                break;
            case 6:
                reportPromoter.setTraficSat(ans1);
                reportPromoter.setDateSat(timework);
                break;
            case 7:
                reportPromoter.setTraficSun(ans1);
                reportPromoter.setDateSun(timework);
                break;
            }
            QFComplexData noPhoto = promoterFormService.getCD(3, FormPromoter.ADDINFO, complexDatas, form, promoter);
            if (noPhoto.getDataValue().equals("")) {
                reportPromoter.setYesNoPhoto("есть");
            } else {
                reportPromoter.setYesNoPhoto("нет");
            }
            QFComplexData dinnerComment=promoterFormService.getCD(WorkTime.dinner_comment, FormPromoter.ADDINFO, complexDatas, form, promoter);
            reportPromoter.setDinnerComment(dinnerComment.getDataValue());
            reportPromoter.setState(ReportPromoter.NOT_READY);
            logger.info(reportPromoter.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(flow.toString());
        }

        return reportPromoter;
    }

    public void fillData(ReportPromoter reportPromoter) {
        List<Shipment> actualShipments = baseRequest.getActualShipments();
        ActualLaptopAndPrintersManager actualLaptopAndPrintersManager = new ActualLaptopAndPrintersManager();
        actualLaptopAndPrintersManager.process(actualShipments);
        Map<String, Shipment> actualLaptops = actualLaptopAndPrintersManager.getActualLaptops();
        Map<String, Shipment> actualPrinters = actualLaptopAndPrintersManager.getActualPrinters();
        Comparator<ShipmentSourceSpecImpl> comparator = (s1, s2) -> s1.getName().compareTo(s2.getName());

        Set<ShipmentSourceSpecImpl> laptopActualShipments = new TreeSet<>(comparator);
        Set<ShipmentSourceSpecImpl> laptopOtherShipments = new TreeSet<>(comparator);
        Set<ShipmentSourceSpecImpl> printersActualShipments = new TreeSet<>(comparator);
        Set<ShipmentSourceSpecImpl> printersOtherShipments = new TreeSet<>(comparator);
        
        List<PromoterSalesContainer> promoterLaptopSalesContainers = new ArrayList<>();
        List<PromoterSalesContainer> promoterPrinterSalesContainers = new ArrayList<>();
        promoSalaryService.getData(reportPromoter,laptopActualShipments,laptopOtherShipments,printersActualShipments,printersOtherShipments
                ,actualLaptops,actualPrinters,promoterLaptopSalesContainers,promoterPrinterSalesContainers);
        
    }

    private String getListShipments(int categoryId, int inStockOrNo, List<QFComplexData> complexDatas) {
        Categoryshipment categoryshipment = baseRequest.getEntity(Categoryshipment.class, categoryId);
        List<Categoryshipment> categoryshipments = new ArrayList<Categoryshipment>();
        categoryshipments.add(categoryshipment);
        List<CategoryShipmentWithShipments> categoryshipmentWithShipments = getInStockOrNo(inStockOrNo, categoryshipments, categoryshipment.getShipments(),
                complexDatas);
        logger.info(categoryshipmentWithShipments.size() + " " + categoryId);
        for (CategoryShipmentWithShipments categoryshipmentWithShipments2 : categoryshipmentWithShipments) {
            logger.info(categoryshipmentWithShipments2.toString());
        }
        return getListShipments(categoryshipmentWithShipments);
    }

    private String getListShipments(List<CategoryShipmentWithShipments> categoryshipmentWithShipments) {
        StringBuilder stringBuilder = new StringBuilder(" ");
        for (CategoryShipmentWithShipments categoryShipmentWithShipments : categoryshipmentWithShipments) {
            List<ShipmentForm> l = categoryShipmentWithShipments.getShipments();
            for (ShipmentForm shipmentForm : l) {
                stringBuilder.append(shipmentForm.getName() + " " + shipmentForm.getCount() + " ");
            }
        }
        return stringBuilder.toString();
    }

    public ReportPromoter convertPromoter(int flowId, String email) {
        Flow flow = baseRequest.getEntity(Flow.class, flowId);
        return convertPromoter(flow, email);
    }

    public List<ReportSupervisor> convertSupervisor(Flow flow) {
        Instance instance = baseRequest.getEntity(Instance.class, flow.getInstance());
        Project project = instance.getProject();
        TrastPromoShedule trastPromoShedule = baseRequest.getEntity(TrastPromoShedule.class, instance.getSchedule());

        AppUser supervisor = baseRequest.getEntity(AppUser.class, trastPromoShedule.getSupervisor());
        LocalDateTime localDateTime = trastPromoShedule.getStart().toLocalDateTime();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // DateTimeFormatter timeFormatter =
        // DateTimeFormatter.ofPattern("HH:mm");
        UnitRegion shop = baseRequest.getEntity(UnitRegion.class, trastPromoShedule.getShop());
        Unit shopNetwork = shop.getUnit();
        List<ReportSupervisor> reportSupervisors = new LinkedList<>();
        for (QFForm qfForm : flow.getQfforms()) {
            try {
                ReportSupervisor e = new ReportSupervisor();
                e.setFlowid(flow.getId());
                e.setSupervisor(supervisor.getSurname());
                e.setDate(dateFormatter.format(localDateTime));
                e.setShopAdres(shop.getName());
                e.setShopName(shopNetwork.getUnitName());
                e.setStock(project.getProjecName());
                List<QFComplexData> complexDatas = qfForm.getQfcomplexData();
                e.setTimeVizit(filtrService.getCD(SupervizerForm.TIMEVIZIT, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                int promoterId = Integer.valueOf(filtrService.getCD(SupervizerForm.PROMOTER, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                AppUser promoter = baseRequest.getEntity(AppUser.class, promoterId);
                e.setPromoter(promoter.getSurname());
                e.setComment(filtrService.getCD(SupervizerForm.COMMENT, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                e.setTimeStart(filtrService.getCD(SupervizerForm.TIME_START, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                e.setAppeearance(filtrService.getCD(SupervizerForm.APPEEARANCE, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                e.setRemark(filtrService.getCD(SupervizerForm.REMARK, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                e.setFioStoreEmployee(filtrService.getCD(SupervizerForm.FIO_STORE_EMPLOYEE, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                e.setRezume(filtrService.getCD(SupervizerForm.REZUME, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                e.setWiches(filtrService.getCD(SupervizerForm.WICHES, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                e.setPhoto1(filtrService.getCD(SupervizerForm.PHOTO1, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                e.setPhoto2(filtrService.getCD(SupervizerForm.PHOTO2, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                e.setPhoto3(filtrService.getCD(SupervizerForm.PHOTO3, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                reportSupervisors.add(e);
            } catch (java.lang.IndexOutOfBoundsException exception) {
                logger.error(qfForm.toString());
            }
        }
        return reportSupervisors;
    }

    public List<ReportSupervisor> convertSupervisor(int flowId) {
        Flow flow = baseRequest.getEntity(Flow.class, flowId);
        return convertSupervisor(flow);

    }

    public Object getReportData(int flowId, String email) {
        Flow flow = baseRequest.getEntity(Flow.class, flowId);
        Situation situation = Situation.getFromId(flow.getSituation());
        int formTempateId = situation.getFormTemplate();
        switch (formTempateId) {
        case 2:
            return convertPromoter(flowId, email);
        case 3:
            return convertSupervisor(flowId);
        }
        return "FAIL";
    }

    public Float getCooficient(Project project, UnitActivity activity, ProductType pt,int region) {
        switch(region){
        case 4:
            switch (project.getId()) {
            case 2: {
                switch (activity.getId()) {
                case 1: {
                    switch (pt) {
                    case FULL:
                        // Private Const ppsA_full = 1.2
                        return 1.4f;
                    case NOTEBOOK:
                        // Private Const ppsA_laptop = 0.3
                        return 0.4f;
                    case PRINTER:
                        // Private Const ppsA_printers = 0.35
                        return 0.5f;
                    }
    
                }
                case 2: {
                    switch (pt) {
                    case FULL:
                        // Private Const ppsB_full = 0.8
                        return 1.05f;
                    case NOTEBOOK:
                        // Private Const ppsB_laptop = 0.2
                        return 0.3f;
                    case PRINTER:
                        // Private Const ppsB_printers = 0.25
                        return 0.35f;
                    }
                }
                case 3: {
                    switch (pt) {
                    case FULL:
                        // Private Const ppsC_full = 0.65
                        return 0.8f;
                    case NOTEBOOK:
                        // Private Const ppsC_laptop = 0.15
                        return 0.25f;
                    case PRINTER:
                        // Private Const ppsC_printers = 0.2
                        return 0.3f;
                    }
                }
                case 4: {
                    switch (pt) {
                    case FULL:
                        // Private Const ppsD_full = 0.4
                        return 0.5f;
                    case NOTEBOOK:
                        // Private Const ppsD_laptop = 0.1
                        return 0.2f;
                    case PRINTER:
                        // Private Const ppsD_printers = 0.15
                        return 0.25f;
                    }
                }
                }
            }
            case 3: {
                switch (activity.getId()) {
                case 1: {
                    switch (pt) {
                    case FULL:
                        // Private Const psgA_full = 0.7
                        return 1.4f;
                    case NOTEBOOK:
                        // Private Const psgA_laptop = 0
                        return 1.4f;
                    case PRINTER:
                        // Private Const psgA_printers = 0
                        return 0f;
                    }
                }
                case 2: {
                    switch (pt) {
                    case FULL:
                        // Private Const psgB_full = 0.5
                        return 1.1f;
                    case NOTEBOOK:
                        // Private Const psgB_laptop = 0
                        return 1.1f;
                    case PRINTER:
                        // Private Const psgB_printers = 0
                        return 0f;
                    }
                }
                case 3: {
                    switch (pt) {
                    case FULL:
                        // Private Const psgC_full = 0.4
                        return 0.8f;
                    case NOTEBOOK:
                        // Private Const psgC_laptop = 0
                        return 0.8f;
                    case PRINTER:
                        // Private Const psgC_printers = 0
                        return 0f;
                    }
                }
                case 4: {
                    switch (pt) {
                    case FULL:
                        // Private Const psgD_full = 0.3
                        return 0.4f;
                    case NOTEBOOK:
                        // Private Const psgD_laptop = 0
                        return 0.4f;
                    case PRINTER:
                        // Private Const psgD_printers = 0
                        return 0f;
                    }
                }
    
                }
            }
            case 4: {
                switch (activity.getId()) {
                case 1: {
                    switch (pt) {
                    case FULL:
                        // Private Const ipgA_full = 0.9
                        return 2.1f;
                    case NOTEBOOK:
                        // Private Const ipgA_laptop = 0
                        return 0f;
                    case PRINTER:
                        // Private Const ipgA_printers = 0
                        return 2.1f;
                    }
                }
                case 2: {
                    switch (pt) {
                    case FULL:
                        // Private Const ipgB_full = 0.8
                        return 1.75f;
                    case NOTEBOOK:
                        // Private Const ipgB_laptop = 0
                        return 0f;
                    case PRINTER:
                        // Private Const ipgB_printers = 0
                        return 1.75f;
                    }
                }
                case 3: {
                    switch (pt) {
                    case FULL:
                        // Private Const ipgC_full = 0.5
                        return 1.25f;
                    case NOTEBOOK:
                        // Private Const ipgC_laptop = 0
                        return 0f;
                    case PRINTER:
                        // Private Const ipgC_printers = 0
                        return 1.25f;
                    }
                }
                case 4: {
                    switch (pt) {
                    case FULL:
                        // Private Const ipgD_full = 0.3
                        return 0.7f;
                    case NOTEBOOK:
                        // Private Const ipgD_laptop = 0
                        return 0f;
                    case PRINTER:
                        // Private Const ipgD_printers = 0
                        return 0.7f;
                    }
                }
    
                }
            }
        }
        default:                
            switch (project.getId()) {
            case 2: {
                switch (activity.getId()) {
                case 1: {
                    switch (pt) {
                    case FULL:
                        // Private Const ppsA_full = 1.2
                        return 1.2f;
                    case NOTEBOOK:
                        // Private Const ppsA_laptop = 0.3
                        return 0.3f;
                    case PRINTER:
                        // Private Const ppsA_printers = 0.35
                        return 0.35f;
                    }
    
                }
                case 2: {
                    switch (pt) {
                    case FULL:
                        // Private Const ppsB_full = 0.8
                        return 0.8f;
                    case NOTEBOOK:
                        // Private Const ppsB_laptop = 0.2
                        return 0.2f;
                    case PRINTER:
                        // Private Const ppsB_printers = 0.25
                        return 0.25f;
                    }
                }
                case 3: {
                    switch (pt) {
                    case FULL:
                        // Private Const ppsC_full = 0.65
                        return 0.65f;
                    case NOTEBOOK:
                        // Private Const ppsC_laptop = 0.15
                        return 0.15f;
                    case PRINTER:
                        // Private Const ppsC_printers = 0.2
                        return 0.2f;
                    }
                }
                case 4: {
                    switch (pt) {
                    case FULL:
                        // Private Const ppsD_full = 0.4
                        return 0.4f;
                    case NOTEBOOK:
                        // Private Const ppsD_laptop = 0.1
                        return 0.1f;
                    case PRINTER:
                        // Private Const ppsD_printers = 0.15
                        return 0.15f;
                    }
                }
                }
            }
            case 3: {
                switch (activity.getId()) {
                case 1: {
                    switch (pt) {
                    case FULL:
                        // Private Const psgA_full = 0.7
                        return 0.7f;
                    case NOTEBOOK:
                        // Private Const psgA_laptop = 0
                        return 0.7f;
                    case PRINTER:
                        // Private Const psgA_printers = 0
                        return 0f;
                    }
                }
                case 2: {
                    switch (pt) {
                    case FULL:
                        // Private Const psgB_full = 0.5
                        return 0.5f;
                    case NOTEBOOK:
                        // Private Const psgB_laptop = 0
                        return 0.5f;
                    case PRINTER:
                        // Private Const psgB_printers = 0
                        return 0f;
                    }
                }
                case 3: {
                    switch (pt) {
                    case FULL:
                        // Private Const psgC_full = 0.4
                        return 0.4f;
                    case NOTEBOOK:
                        // Private Const psgC_laptop = 0
                        return 0.4f;
                    case PRINTER:
                        // Private Const psgC_printers = 0
                        return 0f;
                    }
                }
                case 4: {
                    switch (pt) {
                    case FULL:
                        // Private Const psgD_full = 0.3
                        return 0.3f;
                    case NOTEBOOK:
                        // Private Const psgD_laptop = 0
                        return 0.3f;
                    case PRINTER:
                        // Private Const psgD_printers = 0
                        return 0f;
                    }
                }
    
                }
            }
            case 4: {
                switch (activity.getId()) {
                case 1: {
                    switch (pt) {
                    case FULL:
                        // Private Const ipgA_full = 0.9
                        return 0.9f;
                    case NOTEBOOK:
                        // Private Const ipgA_laptop = 0
                        return 0f;
                    case PRINTER:
                        // Private Const ipgA_printers = 0
                        return 0.9f;
                    }
                }
                case 2: {
                    switch (pt) {
                    case FULL:
                        // Private Const ipgB_full = 0.8
                        return 0.8f;
                    case NOTEBOOK:
                        // Private Const ipgB_laptop = 0
                        return 0f;
                    case PRINTER:
                        // Private Const ipgB_printers = 0
                        return 0.8f;
                    }
                }
                case 3: {
                    switch (pt) {
                    case FULL:
                        // Private Const ipgC_full = 0.5
                        return 0.5f;
                    case NOTEBOOK:
                        // Private Const ipgC_laptop = 0
                        return 0f;
                    case PRINTER:
                        // Private Const ipgC_printers = 0
                        return 0.5f;
                    }
                }
                case 4: {
                    switch (pt) {
                    case FULL:
                        // Private Const ipgD_full = 0.3
                        return 0.3f;
                    case NOTEBOOK:
                        // Private Const ipgD_laptop = 0
                        return 0f;
                    case PRINTER:
                        // Private Const ipgD_printers = 0
                        return 0.3f;
                    }
                }
    
                }
            }
            }
        }
        return 0f;
    }

    public Float getPlanFact(Float countHour, Integer countSales, Float planSales) {
        if ((planSales == 0) || countHour == null || (countHour == 0)) {
            return 0f;
        }
        return countSales / (countHour * planSales);
    }

    public Double getBonus(Float fullRation, Float printerRation, Float lapTopRation) {
        return lapTopRation > 1 && printerRation > 1 ? (fullRation < 0.7 ? 0.7 : (fullRation > 1.3 ? 1.3 : fullRation)) : (fullRation > 1 ? 1
                : (fullRation < 0.7 ? 0.7 : (fullRation > 1 ? 1 : fullRation)));
    }

    public Double getSalary(Double bonus, Float countHour, int rate, int penalty) {
        return bonus * countHour * rate - penalty;
    }

    public ReportView getHemPromoterTaskView(ReportView result, List<? extends FlowInterface> flows, int hemReportState, boolean manager) {
        if (!manager) {
            if(hemReportState==HemReport.CREATED_STATE||hemReportState==HemReport.REFUSED_STATE){
                result.addModel("actions");
                result.addColumn("Действия", "actions", 130, ReportView.ACTION);
            }
            result.addModel("picture");
            result.addColumn("", "picture", 50, ReportView.NORMAL_TEXT);
            result.addModel("state");
            result.addColumn("Статус", "state", 130, ReportView.NORMAL_TEXT);
        }
        else
        {
            if(hemReportState==HemReport.SENT_STATE){
            result.addModel("actions");
            result.addColumn("Действия", "actions", 130, ReportView.ACTION);
            }
        }
        result.addModel("flowId");
        result.addColumn("", "flowId", true);
        result.addModel("situationId");
        result.addColumn("", "situationId", true);
        result.addModel("user");
        result.addColumn("Промоутер", "user", 100, ReportView.NORMAL_TEXT);
        result.addModel("shop");
        result.addColumn("Магазин", "shop", 110, ReportView.MANY_TEXT);
        result.addModel("chain");
        result.addColumn("Сеть", "chain", 100, ReportView.NORMAL_TEXT);
        result.addModel("worktime");
        result.addColumn("Время работы", "worktime", 100, ReportView.TIME);
//        result.addModel("trafic");
//        result.addColumn("Трафик покупателей", "trafic",100,ReportView.NORMAL_TEXT);
        result.addModel("dinnerComment");
        result.addColumn("Причина", "dinnerComment", 100, ReportView.MANY_TEXT);
        result.addModel("comment");
        result.addColumn("Комментарий", "comment", 100, ReportView.MANY_TEXT);
        result.addModel("haveProblems");
        result.addColumn("Наличие проблем","haveProblems",100,ReportView.MANY_TEXT);
        result.addModel("computerSales");
        result.addModel("computerCount");
        result.addModel("printerSales");
        result.addModel("printerCount");
        result.addModel("hasOrNoBonus");
        result.addColumn("Комп. техника", result.createColumn("Кол.", "computerCount", 50, ReportView.NORMAL_TEXT),
                result.createColumn("Сумма", "computerSales", 100, ReportView.MONEY));
        result.addColumn("Печатная техника", result.createColumn("Кол.", "printerCount", 50, ReportView.NORMAL_TEXT),
                result.createColumn("Сумма", "printerSales", 100, ReportView.MONEY));
        result.addColumn("Бонусы", "hasOrNoBonus",100,ReportView.NORMAL_TEXT);
//        result.addModel("photo");
//        result.addColumn("Фото к заданию", "photo", 80, ReportView.NORMAL_TEXT);
        List<Categoryshipment> categoryShipments = baseRequest.getCategoryshipments(Categoryshipment.INFO);
        for (Categoryshipment category : categoryShipments) {

            result.addModel("yes" + category.getId());
            result.addModel("no" + category.getId());

            result.addColumn(category.getName(), result.createColumn("В наличии", "yes" + category.getId(), 100, ReportView.MANY_TEXT),
                    result.createColumn("Не хватает", "no" + category.getId(), 100, ReportView.MANY_TEXT));
        }
        SimpleDateFormat worktimeFormat = new SimpleDateFormat("HH:mm");
        if (manager) {
            List<FlowInterface> managerFlows = new ArrayList<FlowInterface>();
            for (FlowInterface flow : flows) {
                if (((flow.getSituation()==8 || flow.getSituation() == 19 || flow.getSituation() == 21)&&hemReportState==HemReport.SENT_STATE)|| (flow.getSituation()==8&&hemReportState==HemReport.COMPLETED_STATE))
                    managerFlows.add(flow);
            }
            flows = managerFlows;
        }
        for (FlowInterface flow : flows) {
            Map<String, Object> row = result.createRow();
            setStatePromoter(flow,row);
            row.put("actions",getActionsPromoter(flow,hemReportState,manager));
            
            
            
            //TODO: костыль, просто не вывожу проблему..

            
            row.put("flowId", flow.getFlowId());
            row.put("situationId", flow.getSituation());
            ReportPromoter rp=baseRequest.getEntity(ReportPromoter.class, flow.getFlowId());
            
//            if(user == null) {
//                continue;
//            }
            
            boolean haveBonus=false;
            QFForm form=flow.getQfforms().get(0);
            List<Sale> sales=form.getSales();
            for(Sale sale:sales){
            	List<SaleHasPromoStock> shps=sale.getSaleHasPromoStocks();
            	for(SaleHasPromoStock ss:shps)
            		if(ss.getValue()==1)
            			haveBonus=true;
            }
            row.put("hasOrNoBonus",haveBonus? "есть":"");
            AppUser user;
            if(rp==null){
                rp=new ReportPromoter();
                UnitRegion ur=baseRequest.getEntity(UnitRegion.class,flow.getUnitRegion());
                rp.setStoreAddress(ur.getName());
                rp.setPartner(ur.getUnit().getUnitName());
                rp.setDate(flow.getStarttime().toLocalDateTime().toLocalDate().toString());
                Instance inst=baseRequest.getEntity(Instance.class, flow.getInstance());
                TrastPromoShedule tps=baseRequest.getEntity(TrastPromoShedule.class, inst.getSchedule());
                user=baseRequest.getEntity(AppUser.class,tps.getPromoter());
                
            } else
            	user = baseRequest.getEntity(AppUser.class, rp.getPromoterId());
            row.put("user", user.getFullName());
            row.put("shop", rp.getStoreAddress());
            row.put("chain", rp.getPartner());
            try {
                Map<String, Object> data = result.createData();
                data.put("date", rp.getDate());

                data.put("worktime", getWorktime(rp));
                row.put("comment", rp.getCommentPromoter());
                row.put("haveProblems", rp.getHaveProplems());

                row.put("worktime", data);
                if(flow.getAccepted()==Flow.ACCEPTED_DECLINE){
                    row.replace("comment", flow.getComment());
                }
                 row.put("computerSales", rp.getNotebookSumPrice());
                 row.put("computerCount", rp.getNotebookCountSell());
                 row.put("printerSales", rp.getPrinterSumPrice());
                 row.put("printerCount", rp.getPrinterCountSell());
                 row.put("allSales", rp.getCommonSumPrice());
                 row.put("allCount", rp.getCommonCountSell());
                 row.put("dinnerComment", rp.getDinnerComment());

                 row.put("yes1005", rp.getNotebookHave());
                 row.put("yes1006", rp.getBlockHave());
                 row.put("yes1007", rp.getMonoHave());
                 row.put("yes1008", rp.getPrinterHave());
                 row.put("yes1009", rp.getAccessoryHave());
                 row.put("yes1010", rp.getCartridgeHave());
                 row.put("yes1011", rp.getPOSHave());
                 row.put("no1005", rp.getNotebookNotHave());
                 row.put("no1006", rp.getBlockNotHave());
                 row.put("no1007", rp.getMonoNotHave());
                 row.put("no1008", rp.getPrinterNotHave());
                 row.put("no1009", rp.getAccessoryNotHave());
                 row.put("no1010", rp.getCartridgeNotHave());
                 row.put("no1011", rp.getPOSNotHave());




            } catch (IndexOutOfBoundsException e) {

            }

            result.addRow(row);
        }

        return result;
    }
    public List<Action> getActionsPromoter(FlowInterface flow, int hemReportState,
			boolean manager) {
        List<Action> actions = null;
        actions = new ArrayList<Action>();
        if (!manager) {  
            
            if(hemReportState==HemReport.CREATED_STATE||hemReportState==HemReport.REFUSED_STATE){
            switch(flow.getSituation()){
            case 4:
                actions.add(Action.getFromId(4));
                actions.add(Action.getFromId(5));
                break;
            case 17:
                actions.add(Action.getFromId(6));
                break;
            case 2:
                if(flow.getSituationPrev()==4||flow.getSituationPrev()==17)
                    actions.add(Action.getFromId(7));
                if(flow.getSituationPrev()==15)
                	actions.add(Action.getFromId(31));
                break;
            case 15:
            	actions.add(Action.getFromId(29));
            }
            }
        } else{
            if(hemReportState==HemReport.SENT_STATE)
                if(flow.getSituation()==19)
                    actions.add(Action.getFromId(20));
        }
		return actions;
	}

	private Object getTrafic(ReportPromoter rp) {
        return rp.getTraficFri()+rp.getTraficMon()+rp.getTraficSat()+rp.getTraficSun()+rp.getTraficThu()+rp.getTraficTue()+rp.getTraficWed();
    }

    private String getWorktime(ReportPromoter rp) {
        return rp.getDateFri()+rp.getDateMon()+rp.getDateSat()+rp.getDateSun()+rp.getDateThu()+rp.getDateTue()+rp.getDateWed();
    }

    public ReportView getPromoterTaskView(ReportView result, List<? extends FlowInterface> flows, Integer accepted, boolean manager) {
        switch (accepted) {
        case Flow.ACCEPTED_INBOX:
            result.addModel("actions");
            result.addColumn("Действия", "actions", 130, ReportView.ACTION);
            break;
        case Flow.ACCEPTED_DECLINE:
        case Flow.ACCEPTED_SEND:
            result.addModel("whyDecline");
            result.addColumn("Почему отклонено", "whyDecline", 200, ReportView.NORMAL_TEXT);
            break;
        case Flow.ALL_ACCEPTEDS:
            if (manager) {
                result.addModel("actions");
                result.addColumn("Действия", "actions", 130, ReportView.ACTION);
            } else {
                result.addModel("actions");
                result.addColumn("Действия", "actions", 130, ReportView.ACTION);
                result.addModel("picture");
                result.addColumn("", "picture", 50, ReportView.NORMAL_TEXT);
                result.addModel("state");
                result.addColumn("Статус", "state", 130, ReportView.NORMAL_TEXT);
            }
            break;
        default:

            break;
        }

        result.addModel("flowId");
        result.addColumn("", "flowId", true);
        result.addModel("situationId");
        result.addColumn("", "situationId", true);
        result.addModel("user");
        result.addColumn("Промоутер", "user", 100, ReportView.NORMAL_TEXT);
        result.addModel("shop");
        result.addColumn("Магазин", "shop", 110, ReportView.MANY_TEXT);
        result.addModel("chain");
        result.addColumn("Сеть", "chain", 100, ReportView.NORMAL_TEXT);
        result.addModel("worktime");
        result.addColumn("Время работы", "worktime", 100, ReportView.TIME);
        result.addModel("comment");
        result.addColumn("Комментарий", "comment", 100, ReportView.MANY_TEXT);
        result.addModel("computerSales");
        result.addModel("computerCount");
        result.addModel("printerSales");
        result.addModel("printerCount");
        result.addModel("allSales");
        result.addModel("allCount");
        result.addModel("computerSales");
        result.addModel("computerCount");
        result.addModel("printerSales");
        result.addModel("printerCount");
        result.addModel("allSales");
        result.addModel("allCount");
        result.addColumn("Компьютерная техника", result.createColumn("Количество", "computerCount", 80, ReportView.NORMAL_TEXT),
                result.createColumn("Сумма", "computerSales", 90, ReportView.MONEY));
        result.addColumn("Печатная техника", result.createColumn("Количество", "printerCount", 80, ReportView.NORMAL_TEXT),
                result.createColumn("Сумма", "printerSales", 90, ReportView.MONEY));
        result.addColumn("Общие продажи", result.createColumn("Количество", "allCount", 80, ReportView.NORMAL_TEXT),
                result.createColumn("Сумма", "allSales", 90, ReportView.MONEY));
        result.addModel("photo");
        result.addColumn("Фото к заданию", "photo", 80, ReportView.NORMAL_TEXT);

        Integer[] ids = { 1, 27, 28 };
        List<NaireQuestion> questions = baseRequest.getSpecialQuestion(ids);
        for (NaireQuestion question : questions) {
            result.addModel("q" + question.getId());
            result.addColumn(question.getText(), "q" + question.getId(), 110, ReportView.MANY_TEXT);
        }

        List<Categoryshipment> categoryShipments = baseRequest.getCategoryshipments(Categoryshipment.INFO);
        List<Shipment> shipmentsYESorNO = new ArrayList<Shipment>();
        for (Categoryshipment category : categoryShipments) {

            result.addModel("yes" + category.getId());
            result.addModel("no" + category.getId());

            result.addColumn(category.getName(), result.createColumn("В наличии", "yes" + category.getId(), 100, ReportView.MANY_TEXT),
                    result.createColumn("Не хватает", "no" + category.getId(), 100, ReportView.MANY_TEXT));
            List<Shipment> shipments = category.getShipments();
            shipmentsYESorNO.addAll(shipments);
        }
        //TODO

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat worktimeFormat = new SimpleDateFormat("HH:mm");
        if (manager && accepted == Flow.ALL_ACCEPTEDS) {
            List<FlowInterface> managerFlows = new ArrayList<FlowInterface>();
            for (FlowInterface flow : flows) {
                if (flow.getSituation() == 19 || flow.getSituation() == 21)
                    managerFlows.add(flow);
            }
            flows = managerFlows;
        }

        List<Shipment> computers = baseRequest.getListEntity(Shipment.class, "active", 1, "category", 3);
        List<Shipment> printers = baseRequest.getListEntity(Shipment.class, "active", 1, "category", 4);
        for (FlowInterface flow : flows) {
            String state = "";
            Map<String, Object> row = result.createRow();
            List<Action> actions = null;
            Situation situation = null;
            switch (accepted) {
            case Flow.ACCEPTED_INBOX:
                situation = Situation.getFromId(flow.getSituation());
                actions = situation.getActionsTo();
                row.put("actions", actions);
                break;
            case Flow.ACCEPTED_DECLINE:
            case Flow.ACCEPTED_SEND:
                row.put("whyDecline", flow.getComment());
                break;
            case Flow.ALL_ACCEPTEDS:
                actions = new ArrayList<Action>();
                if (manager) {         
                    actions.add(Action.getFromId(20));
                    row.put("actions", actions);
                } else{
                    state = setStatePromoter(flow, row);
                    if(flow.getSituation()==4){
                        actions.add(Action.getFromId(4));
                        actions.add(Action.getFromId(5));
                    }
                    if(flow.getSituation()==17){
                        actions.add(Action.getFromId(6));
                    }
                    row.put("actions", actions);
                }
                if(flow.getSituationPrev()==21){
                    row.put("comment", flow.getComment());
                }
                break;
            default:
                break;
            }

            AppUser user = service.getActorExec(baseRequest.getEntity(Instance.class, flow.getInstance()), AppUser.PROMOTER);
            
            row.put("user", user.getFullName());
            row.put("flowId", flow.getFlowId());
            row.put("situationId", flow.getSituation());
            UnitRegion unitRegion = baseRequest.getEntity(UnitRegion.class, flow.getUnitRegion());
            row.put("shop", unitRegion.getName());
            row.put("chain", unitRegion.getUnit().getUnitName());

            List<NaireUserAnswer> naireUserAnswers = baseRequest.getAnswersByQuestions(flow, ids);
            for (NaireUserAnswer answer : naireUserAnswers) {
                row.put("q" + answer.getQuestionId(), answer.getAnswer());
            }
            try {

                QFForm form = flow.getQfforms().get(0);
                List<QFComplexData> complexDatas = form.getQfcomplexData();

                String date = dateFormat.format(flow.getStoptime());
                Map<String, Object> data = result.createData();
                data.put("date", date);
                QFComplexData onSchedule = workTime.getOnSchedule(complexDatas, form, user);
                if (onSchedule.getDataValue().equals("") && (!state.equals("Не прислал"))) {
                    String worktime = workTime.getStartWorkhh(complexDatas, form, user).getDataValue() + ":"
                            + workTime.getStartWorkmm(complexDatas, form, user).getDataValue() + " - "
                            + workTime.getEndWorkHH(complexDatas, form, user).getDataValue() + ":"
                            + workTime.getEndWorkmm(complexDatas, form, user).getDataValue();
                    data.put("worktime", worktime);
                    row.put("comment", workTime.getComment(complexDatas, form, user).getDataValue());
                } else {
                    String worktime = worktimeFormat.format(flow.getStarttime()) + " - " + worktimeFormat.format(flow.getStoptime());
                    data.put("worktime", worktime);
                    row.put("comment", "");
                }
                row.put("worktime", data);

                QFComplexData noSales = promoterFormService.getCD(2, FormPromoter.ADDINFO, complexDatas, form, user);
                if ((!state.equals("Не прислал")) && noSales.getDataValue().equals("")) {
                    row.put("sales", "есть");
                    Integer printerSales = 0, printerCount = 0, computerSales = 0, computerCount = 0;
                    for (Shipment computer : computers) {
                        try {
                            Integer price = Integer.parseInt(promoterFormService.getCD(complexDatas, computer.getId(), 3, form, user).getDataValue());
                            Integer count = Integer.parseInt(promoterFormService.getCD(complexDatas, computer.getId(), 2, form, user).getDataValue());
                            computerCount += count;
                            computerSales += count * price;
                        } catch (NumberFormatException ex) {

                        }

                    }
                    // 3 - category 20 - SHIPMENT_SALES(MAGIC ANDREW)
                    List<QFComplexData> cdComputers = filtrService.getCD(3, 20, complexDatas);
                    for (QFComplexData computer : cdComputers) {
                        try {
                            Integer price = Integer.parseInt(promoterFormService.getCD(complexDatas, computer.getId(), 3, form, user).getDataValue());
                            Integer count = Integer.parseInt(promoterFormService.getCD(complexDatas, computer.getId(), 2, form, user).getDataValue());
                            computerCount += count;
                            computerSales += count * price;
                        } catch (NumberFormatException ex) {

                        }
                    }
                    row.put("computerSales", computerSales);
                    row.put("computerCount", computerCount);

                    for (Shipment printer : printers) {
                        try {
                            Integer price = Integer.parseInt(promoterFormService.getCD(complexDatas, printer.getId(), 3, form, user).getDataValue());
                            Integer count = Integer.parseInt(promoterFormService.getCD(complexDatas, printer.getId(), 2, form, user).getDataValue());
                            printerCount += count;
                            printerSales += count * price;
                        } catch (NumberFormatException ex) {

                        }

                    }
                    // 4 - category 20 - SHIPMENT_SALES(MAGIC ANDREW)
                    List<QFComplexData> cdPrinters = filtrService.getCD(4, 20, complexDatas);
                    for (QFComplexData computer : cdPrinters) {
                        try {
                            Integer price = Integer.parseInt(promoterFormService.getCD(complexDatas, computer.getId(), 3, form, user).getDataValue());
                            Integer count = Integer.parseInt(promoterFormService.getCD(complexDatas, computer.getId(), 2, form, user).getDataValue());
                            printerCount += count;
                            printerSales += count * price;
                        } catch (NumberFormatException ex) {

                        }
                    }
                    row.put("printerSales", printerSales);
                    row.put("printerCount", printerCount);

                    row.put("allSales", printerSales + computerSales);
                    row.put("allCount", printerCount + computerCount);
                } else {
                    row.put("sales", "нет");
                }

                QFComplexData noPhoto = promoterFormService.getCD(3, FormPromoter.ADDINFO, complexDatas, form, user);
                if (noPhoto.getDataValue().equals("") && (!state.equals("Не прислал"))) {
                    row.put("photo", "есть");
                } else {
                    row.put("photo", "нет");
                }

                List<CategoryShipmentWithShipments> categoryWithShipments = inStoskOrNo.getInStockOrNo(FormPromoter.IN_STOCK, categoryShipments,
                        shipmentsYESorNO, complexDatas);

                for (CategoryShipmentWithShipments categoryWithShipment : categoryWithShipments) {
                    List<ShipmentForm> shipmentForms = categoryWithShipment.getShipments();
                    StringBuilder value = new StringBuilder();
                    for (ShipmentForm shipmentForm : shipmentForms) {
                        value.append(shipmentForm.getName() + ", ");
                    }
                    row.put("yes" + categoryWithShipment.getId(), value);
                }

                List<CategoryShipmentWithShipments> categoryWithShipments2 = inStoskOrNo.getInStockOrNo(FormPromoter.NO, categoryShipments, shipmentsYESorNO,
                        complexDatas);

                for (CategoryShipmentWithShipments categoryWithShipment : categoryWithShipments2) {
                    List<ShipmentForm> shipmentForms = categoryWithShipment.getShipments();
                    StringBuilder value = new StringBuilder();
                    for (ShipmentForm shipmentForm : shipmentForms) {
                        value.append(shipmentForm.getName() + ", ");
                    }
                    row.put("no" + categoryWithShipment.getId(), value);
                }
            } catch (IndexOutOfBoundsException e) {

            }

            result.addRow(row);
        }

        return result;
    }

    // Код из инсток или нет

    public String setStatePromoter(FlowInterface flow, Map<String, Object> row) {
        String state = null;
        boolean ready = false;
        switch (flow.getSituation()) {
            case 2:
                if (flow.getSituationPrev() == 4||flow.getSituationPrev()==17) {
                    state = "Отклонен";
                } else {
                    state = "Не прислал";
                }
                break;
            case 4:
                if (flow.getSituationPrev() == 21) {
                    state = "Отклонен менеджером";
                }
                if (flow.getSituationPrev() == 2) {
                    state = "Не проверен";
                }
                break;
            case 15:
                state = "Не работал";
                break;
            case 17:
                ready = true;
                state = "Принят координатором";
                break;
            case 19:
                ready = true;
                state = "Отправлен менеджеру";
                break;
            case 21:
                ready = true;
                state = "Отправлен менеджеру";
                break;
            case 8:
                ready = true;
                state = "Выполнен";
                break;
        default:
            state = "Ошибка";
    }
        String picture = ready ? "<img src = \"resources/images/icons/fam/accept.gif\"/>" : "<img src = \"resources/images/icons/fam/delete.gif\"/>";
        row.put("picture", picture);
        row.put("state", state);
        return state;

    }

    public static final int ADD_SHIPMENT_IN_STOCK_OR_NO = 21;

    public List<CategoryShipmentWithShipments> getInStockOrNo(final int paramIN_STOCK_OR_NO, final List<Categoryshipment> categoryshipments,
            List<Shipment> shipments,

            List<QFComplexData> qfComplexDatas) {

        List<QFComplexData> complexDatasInStock = filtrService.getCD(0, paramIN_STOCK_OR_NO, qfComplexDatas);
        Map<Integer, CategoryShipmentWithShipments> categoryhipmentwithShipments = getCategoryhipmentwithShipments(categoryshipments);
        List<QFComplexData> complexDataShipments = filtrService.getCD(0, ADD_SHIPMENT_IN_STOCK_OR_NO, qfComplexDatas);

        for (QFComplexData qfComplexData : complexDatasInStock) {

            ShipmentForm shipmentForm = null;
            int categoryShipmentId = 0;
            // подотовіл данные
            //TODO пока не раскидали категории
            Shipment shipment = baseRequest.getEntity(Shipment.class,qfComplexData.getQfba1());
            if (shipment != null) {
                Categoryshipment categoryshipment = filtrService.getCategoryshipment(qfComplexData.getAddictiveData(), categoryshipments);
                // создал суўность заполненія
                shipmentForm = new ShipmentForm(qfComplexData.getId(), qfComplexData.getQfba1(), shipment.getName(), categoryshipment.getName(),
                        qfComplexData.getDataValue());
                categoryShipmentId = qfComplexData.getAddictiveData();
            } else {
                QFComplexData complexDataShipment = getShipmentComplexData(complexDataShipments, qfComplexData.getQfba1());
                if (complexDataShipment == null) {
                    logger.error("complexDataShipment == null");
                    continue;
                }
                categoryShipmentId = qfComplexData.getAddictiveData();
                Categoryshipment categoryshipment = filtrService.getCategoryshipment(categoryShipmentId, categoryshipments);
                shipmentForm = new ShipmentForm(qfComplexData.getId(), qfComplexData.getQfba1(), complexDataShipment.getDataValue(),
                        categoryshipment.getName(), qfComplexData.getDataValue());

            }

            CategoryShipmentWithShipments categoryshipmentWithShipments = categoryhipmentwithShipments.get(categoryShipmentId);
            if (categoryshipmentWithShipments == null) {
                // TODO fix thiss
                continue;
            }
            List<ShipmentForm> a = categoryshipmentWithShipments.getShipments();
            a.add(shipmentForm);
            categoryshipmentWithShipments.setShipments(a);
            categoryhipmentwithShipments.put(categoryShipmentId, categoryshipmentWithShipments);
        }
        Set<Integer> keys = categoryhipmentwithShipments.keySet();
        ArrayList<Integer> arrayList = new ArrayList<Integer>(keys);
        Collections.sort(arrayList);
        List<CategoryShipmentWithShipments> returnValue = new ArrayList<CategoryShipmentWithShipments>();
        for (Integer integer : arrayList) {
            returnValue.add(categoryhipmentwithShipments.get(integer));
        }
        return returnValue;
    }

    private Map<Integer, CategoryShipmentWithShipments> getCategoryhipmentwithShipments(List<Categoryshipment> categoryshipments) {
        Map<Integer, CategoryShipmentWithShipments> map = new HashMap<Integer, CategoryShipmentWithShipments>();
        for (Categoryshipment categoryshipment : categoryshipments) {
            // map.put(categoryshipment.getId(),
            // Categoryshipment.getClone(categoryshipment));
            map.put(categoryshipment.getId(), new CategoryShipmentWithShipments(categoryshipment));
        }
        return map;
    }

    private QFComplexData getShipmentComplexData(List<QFComplexData> complexDataShipments, int qfba1) {
        for (QFComplexData qfComplexData : complexDataShipments) {
            if (qfComplexData.getId() == qfba1) {
                return qfComplexData;
            }
        }
        return null;
    }

    public QFComplexData getFlag(int inStock_NO, List<QFComplexData> complexDatas, QFForm qfform, AppUser appUser) {
        return promoterFormService.getCD(inStock_NO, FormPromoter.ADDINFO, complexDatas, qfform, appUser);
    }
    public void getHemSupervisorTaskView(ReportView result, List<? extends FlowInterface> flows, int hemReportState, boolean manager) {
        if (!manager) {
            if(hemReportState==HemReport.CREATED_STATE||hemReportState==HemReport.REFUSED_STATE){
                result.addModel("actions");
                result.addColumn("Действия", "actions", 130, ReportView.ACTION);
            }
            result.addModel("picture");
            result.addColumn("", "picture", 50, ReportView.NORMAL_TEXT);
            result.addModel("state");
            result.addColumn("Статус", "state", 130, ReportView.NORMAL_TEXT);
        }
        else
        {
            if(hemReportState==HemReport.SENT_STATE){
            result.addModel("actions");
            result.addColumn("Действия", "actions", 130, ReportView.ACTION);
            }
        }
        result.addModel("flowId");
        result.addColumn("", "flowId", true);
        result.addModel("situationId");
        result.addColumn("", "situationId", true);
        result.addModel("supervisor");
        result.addColumn("Супервайзер", "supervisor", 100, ReportView.NORMAL_TEXT);
        result.addModel("date");
        result.addColumn("Дата", "date", 100, ReportView.NORMAL_TEXT);
        result.addModel("shop");
        result.addColumn("Адресс магазина", "shop", 110, ReportView.MANY_TEXT);
        result.addModel("type");
        result.addModel("comment");
        result.addColumn("Комментарий", "comment", 100, ReportView.MANY_TEXT);
        result.addColumn("Тип продаж", "type", 80, ReportView.NORMAL_TEXT);
        result.addModel("chain");
        result.addColumn("Название магазина", "chain", 80, ReportView.NORMAL_TEXT);
        result.addModel("visiting");
        result.addColumn("Время посещения", "visiting", 80, ReportView.NORMAL_TEXT);
        result.addModel("promoter");
        result.addColumn("Промоутер", "promoter", 100, ReportView.NORMAL_TEXT);
        result.addModel("start");
        result.addColumn("Время начала работы", "start", 80, ReportView.NORMAL_TEXT);
        result.addModel("appearance");
        result.addColumn("Описание внешнего вида", "appearance", 100, ReportView.MANY_TEXT);
        result.addModel("remark");
        result.addColumn("Замечания к работе промоутера", "remark", 120, ReportView.MANY_TEXT);
        result.addModel("penalty");
        result.addColumn("Штрафы", "penalty", 120, ReportView.MANY_TEXT);
        result.addModel("seller");
        result.addColumn("ФИО сотрудника магазина", "seller", 100, ReportView.NORMAL_TEXT);
        result.addModel("resume");
        result.addColumn("Резюме разговора с представителем магазина", "resume", 120, ReportView.MANY_TEXT);
        result.addModel("need");
        result.addColumn("Пожелания промоутера", "need", 100, ReportView.MANY_TEXT);
        result.addModel("photo1");
        result.addColumn("Фото", "photo1", 100, ReportView.PHOTO);
        result.addModel("photo2");
        result.addColumn("Фото", "photo2", 100, ReportView.PHOTO);
        result.addModel("photo3");
        result.addColumn("Фото", "photo3", 100, ReportView.PHOTO);
//        result.addModel("comment");
//        result.addColumn("Комментарий", "comment", 100, ReportView.MANY_TEXT);
        if (manager) {
            List<FlowInterface> managerFlows = new ArrayList<FlowInterface>();
            for (FlowInterface flow : flows) {
                if ((flow.getSituation()==8||flow.getSituation() == 20 || flow.getSituation() == 22)&&hemReportState==HemReport.SENT_STATE)
                    managerFlows.add(flow);
                if(flow.getSituation()==8&&hemReportState==HemReport.COMPLETED_STATE)
                    managerFlows.add(flow);
            }
            flows = managerFlows;
        }

        for (FlowInterface flow : flows) {
            Instance instance = baseRequest.getEntity(Instance.class, flow.getInstance());
            Project project = instance.getProject();
            TrastPromoShedule trastPromoShedule = baseRequest.getEntity(TrastPromoShedule.class, instance.getSchedule());
            if(trastPromoShedule==null)
                continue;
            AppUser supervisor = baseRequest.getEntity(AppUser.class, trastPromoShedule.getSupervisor());
            LocalDateTime localDateTime = trastPromoShedule.getStart().toLocalDateTime();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            UnitRegion shop = baseRequest.getEntity(UnitRegion.class, trastPromoShedule.getShop());
            Unit shopNetwork = shop.getUnit();

            List<PromoterMoney> penalties = baseRequest.getPromoterMoney(flow.getInstance());
            if(flow.getQfforms().size()==0){
            		Map<String, Object> row = result.createRow();
                	setStateSupervisor(flow, row);
                    row.put("actions", getActionsSupervisor(flow,hemReportState,manager));
                    row.put("flowId", flow.getFlowId());
                    row.put("situationId", flow.getSituation());
                    row.put("supervisor", supervisor.getFullName());
                    row.put("date", dateFormatter.format(localDateTime));
                    row.put("shop", shop.getName());
                    row.put("chain", shopNetwork.getUnitName());
                    row.put("type", project.getProjecName());
                    AppUser user = baseRequest.getEntity(AppUser.class,trastPromoShedule.getPromoter());
                    row.put("promoter", user.getFullName());
                    result.addRow(row);                
            }

            for (QFForm form : flow.getQfforms()) {
                Map<String, Object> row = result.createRow();
                setStateSupervisor(flow, row);
                StringBuilder text = new StringBuilder();
                for (PromoterMoney penalty : penalties) {
                    String resolve;
                    switch (penalty.getCount()) {
                    case -1:
                        resolve = "Не определено";
                        break;
                    case -2:
                        resolve = "Предупреждение";
                        break;
                    case -4:
                        resolve = "Не выплата заработной платы";
                        break;
                    case -8:
                        resolve = "Увольнение";
                        break;
                    case -12:
                        resolve = "Увольнение и не выплата зп";
                        break;
                    default:
                        resolve = Integer.toString(penalty.getCount());
                        break;
                    }
                    text.append(resolve + " - " + penalty.getComment() + "\n");
                }
                row.put("penalty", text.toString());
                if(form==flow.getQfforms().get(0))
                	row.put("actions", getActionsSupervisor(flow,hemReportState,manager));
                    
                    
//                    if(flow.getSituationPrev()==22){
//                        row.put("comment", flow.getComment());
//                    }
                row.put("flowId", flow.getFlowId());
                row.put("situationId", flow.getSituation());
                row.put("supervisor", supervisor.getFullName());
                row.put("date", dateFormatter.format(localDateTime));
                row.put("shop", shop.getName());
                if(flow.getAccepted()==Flow.ACCEPTED_DECLINE){
                    row.put("comment", flow.getComment());
                }
                row.put("chain", shopNetwork.getUnitName());
                row.put("type", project.getProjecName());
                try {
                    List<QFComplexData> complexDatas = form.getQfcomplexData();
                    row.put("visiting", filtrService.getCD(SupervizerForm.TIMEVIZIT, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    try {
                        int promoterId = Integer.valueOf(filtrService.getCD(SupervizerForm.PROMOTER, SupervizerForm.ONEVALUE, complexDatas).get(0)
                                .getDataValue());
                        AppUser promoter = baseRequest.getEntity(AppUser.class, promoterId);
                        row.put("promoter", promoter.getFullName());
                    } catch (NumberFormatException e) {

                    }
                    row.put("appearance", filtrService.getCD(SupervizerForm.APPEEARANCE, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue() + ","
                            + filtrService.getCD(SupervizerForm.COMMENT, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("start", filtrService.getCD(SupervizerForm.TIME_START, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("remark", filtrService.getCD(SupervizerForm.REMARK, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("seller", filtrService.getCD(SupervizerForm.FIO_STORE_EMPLOYEE, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("resume", filtrService.getCD(SupervizerForm.REZUME, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("need", filtrService.getCD(SupervizerForm.WICHES, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("photo1", filtrService.getCD(SupervizerForm.PHOTO1, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("photo2", filtrService.getCD(SupervizerForm.PHOTO2, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("photo3", filtrService.getCD(SupervizerForm.PHOTO3, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                } catch (IndexOutOfBoundsException e) {

                }
                result.addRow(row);
            }
        }
    }
    public List<Action> getActionsSupervisor(FlowInterface flow,int hemReportState,boolean manager){
        List<Action> actions = new ArrayList<Action>();
        
        if (!manager) {  
            if(hemReportState==HemReport.CREATED_STATE||hemReportState==HemReport.REFUSED_STATE){
                switch(flow.getSituation()){
                case 13:
                    actions.add(Action.getFromId(13));
                    actions.add(Action.getFromId(14));
                    break;
                case 18:
                    actions.add(Action.getFromId(11));
                    break;
                case 11:
                    if(flow.getSituationPrev()==13||flow.getSituationPrev()==18)
                        actions.add(Action.getFromId(10));
                    if(flow.getSituationPrev()==16)
                    	actions.add(Action.getFromId(32));
                    break;
                case 16:
                	actions.add(Action.getFromId(30));
                	break;
                }
            }
        } else{
            if(hemReportState==HemReport.SENT_STATE&&flow.getSituation()==20){
                actions.add(Action.getFromId(23));
            }
        }
        return actions;
    }
    
    public void getSupervisorTaskView(ReportView result, List<? extends FlowInterface> flows, int accepted, boolean manager) {
        switch (accepted) {
        case Flow.ACCEPTED_INBOX:
            result.addModel("actions");
            result.addColumn("Действия", "actions", 130, ReportView.ACTION);
            break;
        case Flow.ACCEPTED_DECLINE:
        case Flow.ACCEPTED_SEND:
            result.addModel("whyDecline");
            result.addColumn("Почему отклонено", "whyDecline", 200, ReportView.NORMAL_TEXT);
            break;
        case Flow.ALL_ACCEPTEDS:
            if (manager) {
                result.addModel("actions");
                result.addColumn("Действия", "actions", 130, ReportView.ACTION);;
            }
            else{
                result.addModel("picture");
                result.addColumn("", "picture", 50, ReportView.NORMAL_TEXT);
                result.addModel("state");
                result.addColumn("Статус", "state", 130, ReportView.NORMAL_TEXT);

            }
            break;
        default:
            break;
        }
        result.addModel("flowId");
        result.addColumn("", "flowId", true);
        result.addModel("situationId");
        result.addColumn("", "situationId", true);
        result.addModel("supervisor");
        result.addColumn("Супервайзер", "supervisor", 100, ReportView.NORMAL_TEXT);
        result.addModel("date");
        result.addColumn("Дата", "date", 100, ReportView.NORMAL_TEXT);
        result.addModel("shop");
        result.addColumn("Адресс магазина", "shop", 110, ReportView.MANY_TEXT);
        result.addModel("type");
        result.addColumn("Тип продаж", "type", 80, ReportView.NORMAL_TEXT);
        result.addModel("chain");
        result.addColumn("Название магазина", "chain", 80, ReportView.NORMAL_TEXT);
        result.addModel("visiting");
        result.addColumn("Время посещения", "visiting", 80, ReportView.NORMAL_TEXT);
        result.addModel("promoter");
        result.addColumn("Промоутер", "promoter", 100, ReportView.NORMAL_TEXT);
        result.addModel("start");
        result.addColumn("Время начала работы", "start", 80, ReportView.NORMAL_TEXT);
        result.addModel("appearance");
        result.addColumn("Описание внешнего вида", "appearance", 100, ReportView.MANY_TEXT);
        result.addModel("remark");
        result.addColumn("Замечания к работе промоутера", "remark", 120, ReportView.MANY_TEXT);
        result.addModel("penalty");
        result.addColumn("Штрафы", "penalty", 120, ReportView.MANY_TEXT);
        result.addModel("seller");
        result.addColumn("ФИО сотрудника магазина", "seller", 100, ReportView.NORMAL_TEXT);
        result.addModel("resume");
        result.addColumn("Резюме разговора с представителем магазина", "resume", 120, ReportView.MANY_TEXT);
        result.addModel("need");
        result.addColumn("Пожелания промоутера", "need", 100, ReportView.MANY_TEXT);
        result.addModel("photo1");
        result.addColumn("Фото", "photo1", 100, ReportView.PHOTO);
        result.addModel("photo2");
        result.addColumn("Фото", "photo2", 100, ReportView.PHOTO);
        result.addModel("photo3");
        result.addColumn("Фото", "photo3", 100, ReportView.PHOTO);
//        result.addModel("comment");
//        result.addColumn("Комментарий", "comment", 100, ReportView.MANY_TEXT);
        if (manager&&accepted==Flow.ALL_ACCEPTEDS) {
            List<FlowInterface> managerFlows = new ArrayList<FlowInterface>();
            for (FlowInterface flow : flows) {
                if (flow.getSituation() == 20 || flow.getSituation() == 22)
                    managerFlows.add(flow);
            }
            flows = managerFlows;
        }

        for (FlowInterface flow : flows) {
            String state = "";
            Instance instance = baseRequest.getEntity(Instance.class, flow.getInstance());
            Project project = instance.getProject();
            TrastPromoShedule trastPromoShedule = baseRequest.getEntity(TrastPromoShedule.class, instance.getSchedule());
            if(trastPromoShedule==null)
                continue;
            AppUser supervisor = baseRequest.getEntity(AppUser.class, trastPromoShedule.getSupervisor());
            LocalDateTime localDateTime = trastPromoShedule.getStart().toLocalDateTime();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            UnitRegion shop = baseRequest.getEntity(UnitRegion.class, trastPromoShedule.getShop());
            Unit shopNetwork = shop.getUnit();

            List<PromoterMoney> penalties = baseRequest.getPromoterMoney(flow.getInstance());
            if(flow.getQfforms().size()==0){
                Map<String, Object> row = result.createRow();
                if(accepted==Flow.ALL_ACCEPTEDS){
                    if (manager) {
                        List<Action> actions = new ArrayList<Action>();
                        actions.add(Action.getFromId(23));
                        row.put("actions", actions);
                    } else
                        state = setStateSupervisor(flow, row);
                    }
                if(accepted==Flow.ACCEPTED_INBOX){
                    Situation situation = Situation.getFromId(flow.getSituation());
                    List<Action> actions = situation.getActionsTo();
                    row.put("actions", actions);
                    }
                    row.put("flowId", flow.getFlowId());
                    row.put("situationId", flow.getSituation());
                    row.put("supervisor", supervisor.getFullName());
                    row.put("date", dateFormatter.format(localDateTime));
                    row.put("shop", shop.getName());
                    row.put("chain", shopNetwork.getUnitName());
                    row.put("type", project.getProjecName());
                    AppUser user = service.getActorExec(baseRequest.getEntity(Instance.class, flow.getInstance()), AppUser.PROMOTER);
                    row.put("promoter", user.getFullName());
                    result.addRow(row);                
            }

            for (QFForm form : flow.getQfforms()) {
                Map<String, Object> row = result.createRow();

                StringBuilder text = new StringBuilder();
                for (PromoterMoney penalty : penalties) {
                    String resolve;
                    switch (penalty.getCount()) {
                    case -1:
                        resolve = "Не определено";
                        break;
                    case -2:
                        resolve = "Предупреждение";
                        break;
                    case -4:
                        resolve = "Не выплата заработной платы";
                        break;
                    case -8:
                        resolve = "Увольнение";
                        break;
                    case -12:
                        resolve = "Увольнение и не выплата зп";
                        break;
                    default:
                        resolve = Integer.toString(penalty.getCount());
                        break;
                    }
                    text.append(resolve + " - " + penalty.getComment() + "\n");
                }
                row.put("penalty", text.toString());

                switch (accepted) {
                case Flow.ACCEPTED_INBOX:
                    Situation situation = Situation.getFromId(flow.getSituation());
                    List<Action> actions = situation.getActionsTo();
                    row.put("actions", actions);
                    break;
                case Flow.ACCEPTED_DECLINE:
                case Flow.ACCEPTED_SEND:
                    row.put("whyDecline", flow.getComment());
                    break;
                case Flow.ALL_ACCEPTEDS:
                    actions = new ArrayList<Action>();
                    if (manager) {         
                        actions.add(Action.getFromId(23));
                        row.put("actions", actions);
                    } else{
                        state = setStateSupervisor(flow, row);
//                        if(flow.getSituation()==13){
//                            actions.add(Action.getFromId(13));
//                            actions.add(Action.getFromId(14));
//                        }
//                        if(flow.getSituation()==18){
//                            actions.add(Action.getFromId(11));
//                        }
//                        row.put("actions", actions);
                    }
//                    if(flow.getSituationPrev()==22){
//                        row.put("comment", flow.getComment());
//                    }
                    break;
                default:
                    break;
                }
                row.put("flowId", flow.getFlowId());
                row.put("situationId", flow.getSituation());
                row.put("supervisor", supervisor.getFullName());
                row.put("date", dateFormatter.format(localDateTime));
                row.put("shop", shop.getName());
                row.put("chain", shopNetwork.getUnitName());
                row.put("type", project.getProjecName());
                try {
                    List<QFComplexData> complexDatas = form.getQfcomplexData();
                    row.put("visiting", filtrService.getCD(SupervizerForm.TIMEVIZIT, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    try {
                        int promoterId = Integer.valueOf(filtrService.getCD(SupervizerForm.PROMOTER, SupervizerForm.ONEVALUE, complexDatas).get(0)
                                .getDataValue());
                        AppUser promoter = baseRequest.getEntity(AppUser.class, promoterId);
                        row.put("promoter", promoter.getFullName());
                    } catch (NumberFormatException e) {

                    }
                    row.put("appearance", filtrService.getCD(SupervizerForm.APPEEARANCE, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue() + ","
                            + filtrService.getCD(SupervizerForm.COMMENT, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("start", filtrService.getCD(SupervizerForm.TIME_START, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("remark", filtrService.getCD(SupervizerForm.REMARK, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("seller", filtrService.getCD(SupervizerForm.FIO_STORE_EMPLOYEE, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("resume", filtrService.getCD(SupervizerForm.REZUME, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("need", filtrService.getCD(SupervizerForm.WICHES, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("photo1", filtrService.getCD(SupervizerForm.PHOTO1, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("photo2", filtrService.getCD(SupervizerForm.PHOTO2, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                    row.put("photo3", filtrService.getCD(SupervizerForm.PHOTO3, SupervizerForm.ONEVALUE, complexDatas).get(0).getDataValue());
                } catch (IndexOutOfBoundsException e) {

                }
                result.addRow(row);
            }
        }
    }

    public String setStateSupervisor(FlowInterface flow, Map<String, Object> row) {
        String state = null;
        boolean ready = false;
        switch (flow.getSituation()) {
        case 11:
            if (flow.getSituationPrev() == 13||flow.getSituationPrev()==18) {
                state = "Отклонен";
            } else {
                state = "Не прислал";
            }
            break;
        case 13:
            if (flow.getSituationPrev() == 22) {
                state = "Отклонен менеджером";
            }
            if (flow.getSituationPrev() == 11) {
                state = "Не проверен";
            }
            break;
        case 16:
            state = "Не посещал";
            break;
        case 18:
            ready = true;
            state = "Принят координатором";
            break;
        case 20:
            ready = true;
            state = "Отправлен менеджеру";
            break;
        case 22:
            ready = true;
            state = "Отправлен менеджеру";
            break;
        case 8:
            ready = true;
            state = "Выполнен";
            break;
    default:
        state = "Ошибка";
}
        String picture = ready ? "<img src = \"resources/images/icons/fam/accept.gif\"/>" : "<img src = \"resources/images/icons/fam/delete.gif\"/>";
        row.put("picture", picture);
        row.put("state", state);
        return state;

    }

    public Integer getFullPenaltyOfInstance(Instance instance){
        List<PromoterMoney> promoterMoneys = baseRequest.getListEntity(PromoterMoney.class, "instance", instance.getId());
        int sum=0;
        for(PromoterMoney pm:promoterMoneys){
        	sum+=pm.getCount()>0?pm.getCount():0;
        }
        return sum;
    }

    public ReportView getPromoterCompletedTaskView(ReportView result, List<Flow> flows) {
        result.addModel("flowId");
        result.addColumn("", "flowId", true);
        result.addModel("user");
        result.addColumn("Промоутер", "user", 100, ReportView.NORMAL_TEXT); // Выпилим
        result.addModel("shop");
        result.addColumn("Магазин", "shop", 110, ReportView.MANY_TEXT);
        result.addModel("worktime");
        result.addColumn("Время работы", "worktime", 100, ReportView.TIME);
        result.addModel("allSales");
        result.addModel("allCount");
        result.addColumn("Общие продажи", result.createColumn("Количество", "allCount", 80, ReportView.NORMAL_TEXT),
                result.createColumn("Сумма", "allSales", 90, ReportView.MONEY));


        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat worktimeFormat = new SimpleDateFormat("HH:mm");

        List<Shipment> computers = baseRequest.getListEntity(Shipment.class, "active", 1, "category", 3);
        List<Shipment> printers = baseRequest.getListEntity(Shipment.class, "active", 1, "category", 4);
        for (FlowInterface flow : flows) {
            Map<String, Object> row = result.createRow();
            AppUser user = service.getActorExec(baseRequest.getEntity(Instance.class, flow.getInstance()), AppUser.PROMOTER);
            
            //TODO: костыль, просто не вывожу проблему..
            if(user == null) {
                continue;
            }
            row.put("user", user.getFullName());
            row.put("flowId", flow.getFlowId());
            UnitRegion unitRegion = baseRequest.getEntity(UnitRegion.class, flow.getUnitRegion());
            row.put("shop", unitRegion.getName()+" "+unitRegion.getUnit().getUnitName());

            try {

                QFForm form = flow.getQfforms().get(0);
                List<QFComplexData> qfComplexDatas = form.getQfcomplexData();
                Map<Integer, Map<Integer, List<QFComplexData>>> mapQfComplexDatas = promoterFormService.getSortComplexData(qfComplexDatas);
                String date = dateFormat.format(flow.getStoptime());
                Map<String, Object> data = result.createData();
                data.put("date", date);
                QFComplexData onSchedule = workTime.getOnSchedule(qfComplexDatas, form, user);
                if (onSchedule.getDataValue().equals("")) {
                    String worktime = workTime.getStartWorkhh(qfComplexDatas, form, user).getDataValue() + ":"
                            + workTime.getStartWorkmm(qfComplexDatas, form, user).getDataValue() + " - "
                            + workTime.getEndWorkHH(qfComplexDatas, form, user).getDataValue() + ":"
                            + workTime.getEndWorkmm(qfComplexDatas, form, user).getDataValue();
                    data.put("worktime", worktime);
                    row.put("comment", workTime.getComment(qfComplexDatas, form, user).getDataValue());
                } else {
                    String worktime = worktimeFormat.format(flow.getStarttime()) + " - " + worktimeFormat.format(flow.getStoptime());
                    data.put("worktime", worktime);
                    row.put("comment", "");
                }
                row.put("worktime", data);
                QFComplexData noSales = promoterFormService.getElement(2, FormPromoter.ADDINFO, mapQfComplexDatas);
                if(noSales!=null){
                    if (noSales.getDataValue().equals("")) {
                        row.put("sales", "есть");
                        Integer printerSales = 0, printerCount = 0, computerSales = 0, computerCount = 0;
                        for (Shipment computer : computers) {
                            try {
                                Integer price = Integer.parseInt(promoterFormService.getCD(qfComplexDatas, computer.getId(), 3, form, user).getDataValue());
                                Integer count = Integer.parseInt(promoterFormService.getCD(qfComplexDatas, computer.getId(), 2, form, user).getDataValue());
                                computerCount += count;
                                computerSales += count * price;
                            } catch (NumberFormatException ex) {

                            }

                        }
                    // 3 - category 20 - SHIPMENT_SALES(MAGIC ANDREW)
                    List<QFComplexData> cdComputers = filtrService.getCD(3, 20, qfComplexDatas);
                    for (QFComplexData computer : cdComputers) {
                        try {
                            Integer price = Integer.parseInt(promoterFormService.getCD(qfComplexDatas, computer.getId(), 3, form, user).getDataValue());
                            Integer count = Integer.parseInt(promoterFormService.getCD(qfComplexDatas, computer.getId(), 2, form, user).getDataValue());
                            computerCount += count;
                            computerSales += count * price;
                        } catch (NumberFormatException ex) {

                        }
                    }

                    for (Shipment printer : printers) {
                        try {
                            Integer price = Integer.parseInt(promoterFormService.getCD(qfComplexDatas, printer.getId(), 3, form, user).getDataValue());
                            Integer count = Integer.parseInt(promoterFormService.getCD(qfComplexDatas, printer.getId(), 2, form, user).getDataValue());
                            printerCount += count;
                            printerSales += count * price;
                        } catch (NumberFormatException ex) {

                        }

                    }
                    // 4 - category 20 - SHIPMENT_SALES(MAGIC ANDREW)
                    List<QFComplexData> cdPrinters = filtrService.getCD(4, 20, qfComplexDatas);
                    for (QFComplexData computer : cdPrinters) {
                        try {
                            Integer price = Integer.parseInt(promoterFormService.getCD(qfComplexDatas, computer.getId(), 3, form, user).getDataValue());
                            Integer count = Integer.parseInt(promoterFormService.getCD(qfComplexDatas, computer.getId(), 2, form, user).getDataValue());
                            printerCount += count;
                            printerSales += count * price;
                        } catch (NumberFormatException ex) {

                        }
                    }

                    row.put("allSales", printerSales + computerSales);
                    row.put("allCount", printerCount + computerCount);
                } else {
                    row.put("sales", "нет");
                }




                }
            } catch (IndexOutOfBoundsException e) {

            }

            result.addRow(row);
        }

        return result;
        
    }
    //TODO config old reports (recount)
    public void configAllPromoter() {
        AppUser user=baseRequest.getEntity(AppUser.class, 111);
        List<ReportPromoter> reports=baseRequest.getListEntity(ReportPromoter.class);
        for(ReportPromoter report : reports){
            Flow flow=report.getFlow();
            QFForm form=flow.getQfforms().get(0);
            QFComplexData qfcd=promoterFormService.getCD(WorkTime.dinner_comment, FormPromoter.ADDINFO, form.getQfcomplexData(), form, baseRequest.getEntity(AppUser.class,report.getPromoterId()));
            report.setDinnerComment(qfcd.getDataValue());
            if(report.getPOSHave()==null||report.getPOSHave()=="")
            	report.setPOSHave("");
            if(report.getPOSNotHave()==null||report.getPOSNotHave()=="")
            	report.setPOSNotHave("");
            if(report.getDinnerComment()==null||report.getDinnerComment()=="")
            	report.setDinnerComment("");
            baseRequest.saveOrUpdate(report);
        }
        
    }
    public void configPromoter(Region region) {
    	String regionName=region.getRegionName();
        List<ReportPromoter> reports=baseRequest.getListEntity(ReportPromoter.class,"city",regionName);
        for(ReportPromoter report : reports){
            Flow flow=baseRequest.getEntity(Flow.class,report.getFlowid());
            if(flow.getSituation()!=2){
                Project project=baseRequest.getEntity(Project.class,report.getProjectId());
                UnitActivity unitActivity=baseRequest.getListEntity(UnitActivity.class,"activityName",report.getCategoryShop()).get(0);
                Float sellPlanCommon = getCooficient(project, unitActivity, ProductType.FULL,region.getId());
                Float sellPlanNotebook = getCooficient(project, unitActivity, ProductType.NOTEBOOK,region.getId());
                Float sellPlanPrinter = getCooficient(project, unitActivity, ProductType.PRINTER,region.getId());

                report.setSellPlanCommon(sellPlanCommon.toString());
                report.setSellPlanNotebook(sellPlanNotebook.toString());
                report.setSellPlanPrinter(sellPlanPrinter.toString());
                fillData(report);
                baseRequest.saveOrUpdate(report);
            }
        }
        
    }

	public void configOnePromoter(int id) {
		ReportPromoter report=baseRequest.getEntity(ReportPromoter.class, id);
		AppUser user=baseRequest.getEntity(AppUser.class, report.getPromoterId());
		Flow flow=baseRequest.getEntity(Flow.class, id);
		service.saveReportShipmentSales(flow, user,report);
        if(flow.getSituation()!=2){
            QFForm form = flow.getQfforms().get(0);
            List<QFComplexData> complexDatas = form.getQfcomplexData();
            AppUser promoter=baseRequest.getEntity(AppUser.class, report.getPromoterId());
            QFComplexData noPhoto = promoterFormService.getCD(3, FormPromoter.ADDINFO, complexDatas, form, promoter);
            if (noPhoto.getDataValue().equals("")) {
                report.setYesNoPhoto("есть");
            } else {
                report.setYesNoPhoto("нет");
            }
            QFComplexData complexData_IN_STOCK = getFlag(FormPromoter.IN_STOCK, complexDatas, form, user);
            if (complexData_IN_STOCK.getDataValue().length() < 2) {
                report.setNotebookHave(getListShipments(1005, InStoskOrNo.IN_STOCK, complexDatas));
                report.setMonoHave(getListShipments(1007, InStoskOrNo.IN_STOCK, complexDatas));
                report.setBlockHave(getListShipments(1006, InStoskOrNo.IN_STOCK, complexDatas));
                report.setPrinterHave(getListShipments(1008, InStoskOrNo.IN_STOCK, complexDatas));
                report.setAccessoryHave(getListShipments(1009, InStoskOrNo.IN_STOCK, complexDatas));
                report.setCartridgeHave(getListShipments(1010, InStoskOrNo.IN_STOCK, complexDatas));
            } else {
                report.setNotebookHave("");
                report.setMonoHave("");
                report.setBlockHave("");
                report.setPrinterHave("");
                report.setAccessoryHave("");
                report.setCartridgeHave("");
            }

            QFComplexData complexData_NO = getFlag(FormPromoter.NO, complexDatas, form, user);
            if (complexData_NO.getDataValue().length() < 2) {
                report.setNotebookNotHave(getListShipments(1005, InStoskOrNo.NO, complexDatas));
                report.setMonoNotHave(getListShipments(1007, InStoskOrNo.NO, complexDatas));
                report.setBlockNotHave(getListShipments(1006, InStoskOrNo.NO, complexDatas));
                report.setPrinterNotHave(getListShipments(1008, InStoskOrNo.NO, complexDatas));
                report.setAccessoryNotHave(getListShipments(1009, InStoskOrNo.NO, complexDatas));
                report.setCartridgeNotHave(getListShipments(1010, InStoskOrNo.NO, complexDatas));
            } else {
                report.setNotebookNotHave("");
                report.setMonoNotHave("");
                report.setBlockNotHave("");
                report.setPrinterNotHave("");
                report.setAccessoryNotHave("");
                report.setCartridgeNotHave("");
            }
            fillData(report);
            baseRequest.saveOrUpdate(report);
        }
		
	}
}

