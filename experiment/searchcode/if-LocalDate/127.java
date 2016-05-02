package by.q64.promo.service;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import by.q64.promo.dao.BaseRequest;
import by.q64.promo.domain.Flow;
import by.q64.promo.domain.PromoStock;
import by.q64.promo.domain.QFForm;
import by.q64.promo.domain.ReportPromoter;
import by.q64.promo.domain.Sale;
import by.q64.promo.domain.SaleHasPromoStock;
import by.q64.promo.domain.dto.SaleHasPromoStockDTO;

@Service
public class GraphicService {
	
	@Autowired
	BaseRequest baseRequest;

	private static final LocalDate startDB=LocalDate.of(2014, 12, 1);
	
	public List<Map<String, Object>> getSalesData(LocalDate startDate,
			LocalDate endDate, int region, int project,String field) {
		if(startDate==null){
			startDate=startDB.minusYears(1);
		}
		if(endDate==null){
			endDate=LocalDate.now();
		}
		//till 2014 documents are not uploaded
		//List<Map<String,Object>> list=baseRequest.getOldReportPromoters(startDate,endDate,region,project);
		List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
		List<ReportPromoter> buf=baseRequest.getReportPromotersForGraphic(startDate,endDate,region,project);
		list.addAll(ReportPromoterToMap(buf,field));
		Map<LocalDate,Map<String,Object>> result=new HashMap<LocalDate,Map<String,Object>>();
		DateTimeFormatter dtf=DateTimeFormatter.ISO_DATE;
		for(Map<String,Object> map:list){
			LocalDate ld=LocalDate.parse((String)map.get("date"),dtf);
			Map<String,Object> day=result.get(ld);
			if(day==null){
				map.put("localDate", ld);
				result.put(ld, map);
			} else {
				sumProperty(day,map,"yValue");
				//TODO other sums for statistics				
			}
			
			for(;startDate.compareTo(endDate)<=0;startDate=startDate.plusDays(1)){
				if(result.get(startDate)==null){
					Map<String,Object> emptyDay=new HashMap<String,Object>();
					emptyDay.put("yValue", 0);
					emptyDay.put("date", startDate.toString());
					emptyDay.put("localDate",startDate);
					result.put(startDate, emptyDay);
				}
			}
		}
		List<Map<String,Object>> resultList=new ArrayList<Map<String,Object>>(result.values());
		resultList.sort(new Comparator<Map<String,Object>>(){

			@Override
			public int compare(Map<String,Object> o1, Map<String,Object> o2) {
				LocalDate date1=(LocalDate)o1.get("localDate");
				LocalDate date2=(LocalDate)o2.get("localDate");
				return date1.compareTo(date2);
			}
			
		});
		return resultList;
	}
	private List<Map<String,Object>> ReportPromoterToMap(
			List<ReportPromoter> list,String field) {
		switch(field){
		case "commonCountSell":
			return transferRPCommonCountSell(list);
		case "commonSumPrice":
			return transferRPCommonSumPrice(list);
		case "notebookCountSell":
			return transferRPNotebookCountSell(list);
		case "notebookSumPrice":
			return transferRPNotebookSumPrice(list);
		case "printerCountSell":
			return transferRPPrinterCountSell(list);
		case "printerSumPrice":
			return transferRPPrinterSumPrice(list);		
		default:
			throw new IllegalArgumentException("Illegal field value in getting graphic data - "+field);
		}

	}
	public List<Map<String, Object>> getBonusData(LocalDate startDate,
			LocalDate endDate, int region, String field) {
		List<SaleHasPromoStockDTO> data=baseRequest.getSaleHasPromoStockDTO(startDate,endDate,region);
		Map<LocalDate,Map<String,Object>> result=new HashMap<LocalDate,Map<String,Object>>();
		for(SaleHasPromoStockDTO bonusSale : data){
			LocalDate ld=LocalDate.parse(bonusSale.getDate(),DateTimeFormatter.ISO_DATE);
			Map<String,Object> day=result.get(ld);
			if(day==null){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("localDate",ld);
				map.put("yValue",field.equals("bonusCountSell")?1:bonusSale.getBonus());
				map.put("date", bonusSale.getDate());
				result.put(ld,map);
			} else {
				int yValue=(int)day.get("yValue");
				day.replace("yValue", yValue+(field.equals("bonusCountSell")?1:bonusSale.getBonus()));
			}
			
		}
		for(;startDate.compareTo(endDate)<=0;startDate=startDate.plusDays(1)){
			if(result.get(startDate)==null){
				Map<String,Object> emptyDay=new HashMap<String,Object>();
				emptyDay.put("yValue", 0);
				emptyDay.put("date", startDate.toString());
				emptyDay.put("localDate",startDate);
				result.put(startDate, emptyDay);
			}
		}
		List<Map<String,Object>> resultList=new ArrayList<Map<String,Object>>(result.values());
		resultList.sort(new Comparator<Map<String,Object>>(){

			@Override
			public int compare(Map<String,Object> o1, Map<String,Object> o2) {
				LocalDate date1=(LocalDate)o1.get("localDate");
				LocalDate date2=(LocalDate)o2.get("localDate");
				return date1.compareTo(date2);
			}
			
		});
		return resultList;
	}
	private void sumProperty(Map<String,Object> result,Map<String,Object> sum,String property){
		try{
		int cur=(int)result.get(property);

		cur+=(int)sum.get(property);
		result.replace(property, cur);
		} catch (NullPointerException e){
			e.printStackTrace();
		}
	}
	private List<Map<String,Object>> transferRPNotebookCountSell(List<ReportPromoter> list) {
		List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
		
		for(ReportPromoter rp:list){
			Map<String,Object> row=new HashMap<String,Object>();
			int value=Integer.parseInt(rp.getNotebookCountSell());
			row.put("yValue", value);
			row.put("date", rp.getDate());
			result.add(row);
		}
		return result;
	}
	private List<Map<String,Object>> transferRPPrinterCountSell(List<ReportPromoter> list) {
		List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
		
		for(ReportPromoter rp:list){
			Map<String,Object> row=new HashMap<String,Object>();
			int value=Integer.parseInt(rp.getPrinterCountSell());
			row.put("yValue", value);
			row.put("date", rp.getDate());
			result.add(row);
		}
		return result;
	}
	private List<Map<String,Object>> transferRPCommonCountSell(List<ReportPromoter> list) {
		List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
		
		for(ReportPromoter rp:list){
			Map<String,Object> row=new HashMap<String,Object>();
			int value=Integer.parseInt(rp.getPrinterCountSell())+Integer.parseInt(rp.getNotebookCountSell());
			row.put("yValue", value);
			row.put("date", rp.getDate());
			result.add(row);
		}
		return result;
	}
	private List<Map<String,Object>> transferRPCommonSumPrice(List<ReportPromoter> list) {
		List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
		
		for(ReportPromoter rp:list){
			Map<String,Object> row=new HashMap<String,Object>();
			int value=Integer.parseInt(rp.getPrinterSumPrice())+Integer.parseInt(rp.getNotebookSumPrice());
			row.put("yValue", value);
			row.put("date", rp.getDate());
			result.add(row);
		}
		return result;
	}
	private List<Map<String,Object>> transferRPNotebookSumPrice(List<ReportPromoter> list) {
		List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
		
		for(ReportPromoter rp:list){
			Map<String,Object> row=new HashMap<String,Object>();
			int value=Integer.parseInt(rp.getNotebookSumPrice());
			row.put("yValue", value);
			row.put("date", rp.getDate());
			result.add(row);
		}
		return result;
	}
	private List<Map<String,Object>> transferRPPrinterSumPrice(List<ReportPromoter> list) {
		List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
		
		for(ReportPromoter rp:list){
			Map<String,Object> row=new HashMap<String,Object>();
			int value=Integer.parseInt(rp.getPrinterSumPrice());
			row.put("yValue", value);
			row.put("date", rp.getDate());
			result.add(row);
		}
		return result;
	}

	
	

}

