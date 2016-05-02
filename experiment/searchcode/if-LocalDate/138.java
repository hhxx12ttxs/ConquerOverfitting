package by.q64.promo.service.report;

import by.q64.promo.dao.BaseRequest;
import by.q64.promo.domain.*;
import by.q64.promo.excelgen.service.marketing.MarketingReportGenerator;
import by.q64.promo.excelgen.service.marketing.source.MarketingReportData;
import by.q64.promo.excelgen.service.marketing.source.MarketingReportDataManyCities;
import by.q64.promo.excelgen.service.marketing.source.MarketingSourceByCity;
import by.q64.promo.excelgen.service.marketing.source.PreviewReportingDocument;
import by.q64.promo.service.i.FileService;
import by.q64.promo.service.i.MarketingReportService;
import by.q64.promo.utils.chat.PromoChat;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MarketingReportServiceImpl implements MarketingReportService {

    @Autowired
    private BaseRequest baseRequest;
    @Autowired
    private MarketingReportGenerator marketingReportGenerator;
    @Autowired
    private FileService fileService;

    public List<MarketingReport> getMarketingReports(int region, LocalDate date,int promoForm,boolean ready) {
        return getMarketingReports(region, promoForm,date.withDayOfMonth(1), date.plusMonths(1).withDayOfMonth(1),ready);
    }

    //В какие дни работал пром в магазе
    @Transactional(readOnly=true)
    public List<Integer> setWorkingDays(MarketingReport report){
        List<TrastPromoShedule> scheduleCells=baseRequest.getCSchedulesForShop(LocalDate.parse(report.getStartDate()), LocalDate.parse(report.getEndDate()), report.getShop());
        List<Integer> days=new ArrayList<Integer>();
        for(TrastPromoShedule schedule:scheduleCells){
        	int day=schedule.getStart().getDate();
        	if(!days.contains(day))
        		days.add(day);
        }
        days.sort(new Comparator<Integer>(){
            @Override
            public int compare (Integer i1,Integer i2){
                return i1-i2;
            }
        });
        report.setDays(days);
        return days;
    }

    public MarketingReport getMarketingReport(UnitRegion shop, int promoForm, LocalDate startDate, LocalDate endDate) {
        MarketingReport result = baseRequest.getMarketingReportByShop(shop.getId(),promoForm, startDate, endDate);
        if (result == null) {
            result = makeNewMarketingReport(shop,promoForm, startDate, endDate);
        }
        return result;
    }

    public void startMonth() {
        List<UnitRegion> shops = baseRequest.getListEntity(UnitRegion.class, "active", 1);
        List<Project> projects=baseRequest.getListEntity(Project.class,"active",1);
        //confPrevMonth(Month.NOVEMBER.getValue());
        LocalDate curDate = LocalDate.now();
        for (UnitRegion shop : shops) {
        	for(Project proj:projects){
        		Activity activity=baseRequest.getLastActivity(shop.getId(), proj.getId());
        		if(activity.getActivity()!=Activity.NO_ACTIVITY)
        			getMarketingReport(shop,activity.getPromoForm(), curDate.withDayOfMonth(1), curDate.withDayOfMonth(1).plusMonths(1));
        	}
        }
    }

    public void startWithProjects() {
        List<Project> projects = baseRequest.getListEntity(Project.class, "active", 1);
        List<UnitRegion> shops = baseRequest.getListEntity(UnitRegion.class, "active", 1);
        LocalDate curDate = LocalDate.now();
        MarketingReport rep=null;
        for(UnitRegion shop :shops){
            for(Project project : projects){
            	Activity activity=baseRequest.getLastActivity(shop.getId(), project.getId());
                if(activity.getActivity()!=Activity.NO_ACTIVITY){
                        rep=getMarketingReport(shop, activity.getPromoForm(),curDate.withDayOfMonth(1), curDate.withDayOfMonth(1).plusMonths(1));
//                        rep.setState(5);
//                        baseRequest.saveOrUpdate(rep);
                    }
                }

            }
        }

    public void makeForm(LocalDate startDate){
    	List<MarketingReport> mrs=baseRequest.getListEntity(MarketingReport.class,"startDate",startDate.toString());
    	List<Project> projects=baseRequest.getListEntity(Project.class,"active",1);
    	Activity a;
    	for (MarketingReport mr : mrs){
    		for(Project p:projects){
    			a=baseRequest.getLastActivity(mr.getShop(), p.getId());
    			if(a.getPromoForm()!=Activity.NO_FORM){ 
    				mr.setPromoForm(a.getPromoForm());
    				continue;
    				}	
    		}
    	}
    }
	public String formToString(int id){
		switch(id){
		case Activity.INTEL_FORM: return "Поло Intel";
		case Activity.HP_FORM: return "Поло HP";
		case Activity.WIN_FORM: return "Поло WIN";
		default: return "Не опр";
		}
	}
    private MarketingReport makeNewMarketingReport(UnitRegion shop,int promoForm, LocalDate startDate, LocalDate endDate) {
        MarketingReport result = new MarketingReport();
        result.setShop(shop.getId());
        result.setStartDate(startDate.toString());
        result.setEndDate(endDate.toString());
        result.setPromoForm(promoForm);
        result.setRegion(shop.getRegion().getId());
        result.setUnitRegionName(shop.getName());
        result.setUnitRegionNetworkName(shop.getUnit().getUnitName());
        result.setPhoto1("https://pp.vk.me/c616316/v616316908/7fba/ddjycLSfStU.jpg");
        result.setPhoto2("https://pp.vk.me/c616316/v616316908/7fba/ddjycLSfStU.jpg");
        result.setPhoto3("https://pp.vk.me/c616316/v616316908/7fba/ddjycLSfStU.jpg");
        result.setState(MarketingReport.NOT_READY);
        baseRequest.saveOrUpdate(result);
        return result;
    }

    //this is for prev months
//    private void confPrevMonth(int month){
//        List<UnitRegion> shops=baseRequest.getListEntity(UnitRegion.class,"active",1);
//        for(UnitRegion shop:shops){
//            getMarketingReport(shop, LocalDate.now().withDayOfMonth(1).withMonth(month),null);
//        }
//
//    }
    public List<MarketingReport> getMarketingReports(int region, int promoForm,LocalDate startDate, LocalDate endDate,boolean ready) {
        List<MarketingReport> result = baseRequest.getMarketingReports(region,promoForm,startDate,endDate,ready);
        for (MarketingReport report : result) {
            setWorkingDays(report);
        }
        return result;
    }
    @Transactional(readOnly = true)
    @Override
    public List<Map<String, Object>> getMarketingReportForWebInterface(int region, LocalDate startDate) {
        List<MarketingReport> reports = getMarketingReports(region, startDate,0,false);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (MarketingReport report : reports) {
            result.add(getMarkReportForWeb(report));
        }
        return result;
    }
    
    @Override
    public Map<String, Object> getMarkReportForWeb(MarketingReport report) {
        UnitRegion shop = baseRequest.getEntity(UnitRegion.class, report.getShop());
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("supervisor", baseRequest.getEntity(AppUser.class, shop.getSupervisorId()).getFullName());
        row.put("id", report.getId());
        row.put("regionId", report.getRegion());
        row.put("regionName", baseRequest.getEntity(Region.class, report.getRegion()).getRegionName());
        row.put("shop", report.getUnitRegionName());
        row.put("shopNetwork", report.getUnitRegionNetworkName()+", "+formToString(report.getPromoForm()));
        row.put("days", report.getDays());
        row.put("photo1", report.getPhoto1());
        row.put("photo2", report.getPhoto2());
        row.put("photo3", report.getPhoto3());
        row.put("startDate", report.getStartDate());
        row.put("endDate", report.getEndDate());
        row.put("state", report.getState());
        row.put("comment", report.getComment()==null?"":report.getComment());
        return row;
    }

    @Transactional(readOnly = true)
    @Override
    public void getMarketingReport(OutputStream outputStream, Integer[] regions, int promoForm,LocalDate startDate) throws IOException {
        List<MarketingSourceByCity> cities = new ArrayList<>(regions.length);

        for (int region : regions) {
            List<MarketingReport> reports = getMarketingReports(region, startDate,promoForm,false);

            Region regEntity = baseRequest.getEntity(Region.class, region);
            //create data for generating report
            MarketingSourceByCity city = new MarketingSourceByCity();
            city.setSlides(reports);
            city.setCityName(regEntity.getRegionName());
            city.setPreviewData(reports.stream().map(r -> new PreviewReportingDocument(r.getUnitRegionNetworkName(),
                    r.getUnitRegionName())).collect(Collectors.toList()));
            cities.add(city);
        }

        //TODO city name
        MarketingReportDataManyCities data = new MarketingReportDataManyCities();

        MarketingReportData marketingReportData = new MarketingReportData();
        marketingReportData.setReportTitle("DemoDays Anti Epson \n" +
                " L-series attack Q115");
        marketingReportData.setYear(startDate.getYear());
        marketingReportData.setMonth(startDate.getMonthValue());
        data.setCities(cities);
        data.setMarketingReportData(marketingReportData);

        //
        marketingReportGenerator.generate(outputStream, data, s -> {
            try {
                if (s != null) {
                    // we guess we have hash in last part splitted by slash
                    String[] splited = s.split("/");
                    String lastPart = splited[splited.length - 1];
                    FileService.FileAnswerDTO file = fileService.getFile(lastPart);
                    if(file != null && file.getIo() != null) {
                        return IOUtils.toByteArray(file.getIo());
                    }
//                    File file = new File("D:/firstSlideImage.png");
//                    return IOUtils.toByteArray(new BufferedInputStream(new FileInputStream(file)));
                }
                return null;
            } catch (IOException e) {
                return null;
            }
        });
    }

	@Override
	public Map<String, Object> switchStateMarketingReport(String name,
			String comment, int id,int state) {
		MarketingReport mr=baseRequest.getEntity(MarketingReport.class, id);
		if(state==MarketingReport.NOT_READY)
			mr.setComment(comment);
		mr.setState(state);
		Map<String,Object> result=new HashMap<String,Object>();
		PromoChat pc=new PromoChat(name);
		UnitRegion shop=baseRequest.getEntity(UnitRegion.class, mr.getShop());
		ChatContactUserUser chat=pc.getRelation(shop.getSupervisorId());
		pc.addMessage("Системное сообщение: был отклонен маркетинговый отчет. Магазин: "+shop.getName()+". Комментарий: "+comment+".", chat.getId());
		baseRequest.saveOrUpdate(mr);
		result.put("success",true);
		return result;
	}


}

