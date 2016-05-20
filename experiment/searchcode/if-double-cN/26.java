package cn.mmbook.platform.service.data.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javacommon.base.BaseManager;
import javacommon.base.EntityDao;
import javacommon.systemparameters.SParametersHelper;
import javacommon.util.StringTool;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cn.mmbook.platform.dao.data.TbusDataDao;
import cn.mmbook.platform.model.data.ExcelTable;
import cn.mmbook.platform.model.data.TbusData;
import cn.mmbook.platform.service.data.ExcelTableManager;
import cn.mmbook.platform.service.data.TbusDataManager;
import cn.org.rapid_framework.page.Page;
import cn.org.rapid_framework.page.PageRequest;
/**
 * <p> TbusData server?,????,????<br>
 * <p>   <br>
 * @author admin,
 * @version 1.0. 2010-07-08
 *
 */

@Component("tbusDataManager")
@Transactional
public class TbusDataManagerImpl extends BaseManager<TbusData,java.lang.Integer> 
					implements TbusDataManager {

	private TbusDataDao tbusDataDao;
	private ExcelTableManager excelTableManager;
	
	
	
	/**??setXXXX()??,spring?????autowire????????*/
	public void setTbusDataDao(TbusDataDao dao) {
		this.tbusDataDao = dao;
	}
	public void setExcelTableManager(ExcelTableManager manager) {
		this.excelTableManager = manager;
	}
	
	/**??DAO*/
	public EntityDao getEntityDao() {
		return this.tbusDataDao;
	}
	/**??????*/
	@Transactional(readOnly=true)
	public Page findByPageRequest(PageRequest pr) {
		return tbusDataDao.findByPageRequest(pr);
	}
	
	
	public List getList(TbusData u){
		return tbusDataDao.getList(u);
	}
	/**??????*/
	@Transactional(readOnly=true)
	public Page listPageAnyTable(PageRequest pr) {
		return tbusDataDao.listPageAnyTable(pr);
	}
	public void saveNew(TbusData u){
		String questionnaireNumber = u.getQuestionnaireNumber();
		String line = questionnaireNumber.substring(2,questionnaireNumber.length()-3);
		u.setLine(line);
		if(StringTool.isNull(u.getPlateNumber())&&!u.getPlateNumber().startsWith("?A")){
			u.setPlateNumber("?A"+u.getPlateNumber());
		}
		String upTime1 = u.getUpTime();
		String downTime1 = u.getDownTime();
		upTime1 = upTime1.replaceAll("-", ":");
		downTime1 = downTime1.replaceAll("-", ":");
		String upTime = "",downTime="";
		if(null!=upTime1&&upTime1.indexOf(":")<0){
			upTime = upTime1.substring(0,2)+":"+upTime1.substring(2,4);
		}
		if(null!=downTime1&&downTime1.indexOf(":")<0){
			downTime = downTime1.substring(0,2)+":"+downTime1.substring(2,4);
		}
		u.setUpTime(upTime);
		u.setDownTime(downTime);
		
		u.setDeduction1(getDeduction(u.getSubject1()));
		u.setSubject1(   getSubject(u.getSubject1()));
		
		u.setDeduction2(getDeduction(u.getSubject2()));
		u.setSubject2(   getSubject(u.getSubject2()));
		
		u.setDeduction3(getDeduction(u.getSubject3()));
		u.setSubject3(   getSubject(u.getSubject3()));
		
//		u.setDeduction3(getDeduction(u.getSubject3()));
//		u.setSubject3(   getSubject(u.getSubject3()));
		
		u.setDeduction4(getDeduction(u.getSubject4()));
		u.setSubject4(   getSubject(u.getSubject4()));
		
		u.setDeduction5(getDeduction(u.getSubject5()));
		u.setSubject5(   getSubject(u.getSubject5()));
		
//		u.setDeduction5(getDeduction(u.getSubject5()));
//		u.setSubject5(   getSubject(u.getSubject5()));
		
		u.setDeduction6(getDeduction(u.getSubject6()));
		u.setSubject6(   getSubject(u.getSubject6()));
		
		u.setDeduction7(getDeduction(u.getSubject7()));
		u.setSubject7(   getSubject(u.getSubject7()));
		
		u.setDeduction8(getDeduction(u.getSubject8()));
		u.setSubject8(   getSubject(u.getSubject8()));
		
		u.setDeduction9(getDeduction(u.getSubject9()));
		u.setSubject9(   getSubject(u.getSubject9()));
		
		u.setDeduction10(getDeduction(u.getSubject10()));
		u.setSubject10(   getSubject(u.getSubject10()));
		
		u.setDeduction11(getDeduction(u.getSubject11()));
		u.setSubject11(   getSubject(u.getSubject11()));
		
		u.setDeduction12(getDeduction(u.getSubject12()));
		u.setSubject12(   getSubject(u.getSubject12()));
		
		u.setDeduction13(getDeduction(u.getSubject13()));
		u.setSubject13(   getSubject(u.getSubject13()));
		
		u.setDeduction14(getDeduction(u.getSubject14()));
		u.setSubject14(   getSubject(u.getSubject14()));
		
		u.setDeduction15(getDeduction(u.getSubject15()));
		u.setSubject15(   getSubject(u.getSubject15()));
		
		u.setDeduction16(getDeduction(u.getSubject16()));
		u.setSubject16(   getSubject(u.getSubject16()));
		
		u.setDeduction17(getDeduction(u.getSubject17()));
		u.setSubject17 (  getSubject(u.getSubject17()));
		
		u.setDeduction18(getDeduction(u.getSubject18()));
		u.setSubject18(   getSubject(u.getSubject18()));
		
		u.setDeduction19(getDeduction(u.getSubject19()));
		u.setSubject19(   getSubject(u.getSubject19()));
		
		u.setDeduction20(getDeduction(u.getSubject20()));
		u.setSubject20(   getSubject(u.getSubject20()));
		
		u.setDeduction21(getDeduction(u.getSubject21()));
		u.setSubject21(   getSubject(u.getSubject21()));
		
		u.setDeduction22(getDeduction(u.getSubject22()));
		u.setSubject22(   getSubject(u.getSubject22()));

		u.setDeduction23(getDeduction(u.getSubject23()));
		u.setSubject23(   getSubject(u.getSubject23()));
		
		u.setDeduction24(getDeduction(u.getSubject24()));
		u.setSubject24(   getSubject(u.getSubject24()));
		
		u.setDeduction25(getDeduction(u.getSubject25()));
		u.setSubject25(   getSubject(u.getSubject25()));
		
		u.setDeduction26(getDeduction(u.getSubject26()));
		u.setSubject26(   getSubject(u.getSubject26()));
		
		u.setDeduction27(getDeduction(u.getSubject27()));
		u.setSubject27(   getSubject(u.getSubject27()));
		
		u.setDeduction28(getDeduction(u.getSubject28()));
		u.setSubject28(   getSubject(u.getSubject28()));
		
		u.setDeduction29(getDeduction(u.getSubject29()));
		u.setSubject29(   getSubject(u.getSubject29()));
		
		u.setDeduction30(getDeduction(u.getSubject30()));
		u.setSubject30(   getSubject(u.getSubject30()));
		
		u.setDeduction31(getDeduction(u.getSubject31()));
		u.setSubject31(   getSubject(u.getSubject31()));
		
		
		save(u);
	}
	
	private String getSubject(String str){
		str = getCommonStr(str);
		String number = StringTool.getQuantity(str);
		String strx = StringTool.getStrNumber(str);
		if(StringTool.isNull(number)){
			return strx;
		}else if(StringTool.isNull(number)==false&&str.length()>1)  {
			return strx;
		}else{
			return "";
		}
	}
	/**
	 * ????
	 * @param str
	 * @return
	 */
	private String getDeduction(String str){
		str = getCommonStr(str);
		String number = StringTool.getQuantity(str);
		if(StringTool.isNull(number)){
			return "-"+number;
		}else if(StringTool.isNull(number)==false&&str.length()>1)  {
			return "-2";
		}else{
			return "";
		}
			
	}
	
	public String getCommonStr(String str){
		if(StringTool.isNull(str)==false) {
			return "";
		}else{
			str = str.replaceAll("-", "");
			str = str.replaceAll(" ", "");
			return str;
		}
	}
	/**
	 * ????????
	 * 
	 */
	public int export(String beginTime,String endTime,String tableStr){
		//System.out.println("????????" + dayStr);
		/**????????????*/
		Map map_excel_param = new HashMap();
		map_excel_param.put("linkExcel", tableStr);
		List<ExcelTable> list_excel = excelTableManager.getListByMap(map_excel_param);
		/**?????????Map?*/
		Map map_param = new HashMap();
		map_param.put("beginTime",beginTime);
		map_param.put("endTime",endTime);
		 
		List list_bus =   tbusDataDao.getListByDate(map_param);
		String tomcatPath=SParametersHelper.getValue("TOMCATPATH");
		File f = new File(tomcatPath+"/webapps/Bus/file/bus.xls"); 
        WritableWorkbook wwb = null;
        // ??Excel???
        WritableSheet ws = null; 
        try {
            f.createNewFile();
            wwb = Workbook.createWorkbook(f); 
            ws = wwb.createSheet("Sheet1", 0);// ??sheet
            
            for(int i=0;null!=list_excel&&i<list_excel.size();i++){
            	ExcelTable  excelTable =(ExcelTable)list_excel.get(i);
            	Label lable = new Label((excelTable.getLineValue().intValue())-1, 0, excelTable.getLineExplain());
            	//ws.setRowView(4,111);
            	//WritableFont fontNormal= new   WritableFont(WritableFont.TIMES,11,WritableFont.BOLD,true);  //????
            	WritableFont fontNormal= new   WritableFont(WritableFont.ARIAL,11);  //????
            	WritableCellFormat cellFormat=new WritableCellFormat(fontNormal);
            	ws.setColumnView(i, 14);//????????? 14            	
            	 
            	cellFormat.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN); 
            	
            	cellFormat.setAlignment(jxl.format.Alignment.CENTRE);//?????
            	cellFormat.setWrap(true); //??????
            	lable.setCellFormat(cellFormat);
            	
            	ws.addCell(lable);
                
            }
        } catch (WriteException e) {
        	
        } catch (IOException e) { 
            e.printStackTrace();
        }
        
		for(int i=0;null!=list_bus&&i<list_bus.size();i++){
			TbusData tbusData = (TbusData)list_bus.get(i);
			try {
				WritableFont fontNormal= new   WritableFont(WritableFont.ARIAL,9);  //????
				 
				jxl.write.WritableCellFormat wcsB = new jxl.write.WritableCellFormat(fontNormal);
				wcsB.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);
				wcsB.setWrap(true); //??????
				wcsB.setAlignment(jxl.format.Alignment.CENTRE);//?????
				jxl.write.WritableCellFormat wcfN = new jxl.write.WritableCellFormat(NumberFormats.INTEGER);
				
				wcfN.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);
				wcfN.setWrap(true); //??????
					// ws.setColumnView(i, 5);//?????????4
					//ws.setRowView(i, 12,true);
	                ws.addCell(new  Label(0, i+1, tbusData.getQuestionnaireNumber(),wcsB)); 
	                ws.addCell(new	Label(1,	i+1,	tbusData.getLine(),wcsB)); 
	                ws.addCell(new	Label(2,	i+1,	tbusData.getLineType(),wcsB)); 
	                ws.addCell(new	Label(3,	i+1,	tbusData.getPlateNumber(),wcsB)); 
	                ws.addCell(new	Label(4,	i+1,	tbusData.getUsrserName(),wcsB)); 
	                ws.addCell(new	Label(5,	i+1,	tbusData.getDaysString(),wcsB)); 
	                ws.addCell(new	Label(6,	i+1,	tbusData.getUpAddress(),wcsB)); 
	                ws.addCell(new	Label(7,	i+1,	tbusData.getUpTime(),wcsB)); 
	                ws.addCell(new	Label(8,	i+1,	tbusData.getDownAddress(),wcsB)); 
	                ws.addCell(new	Label(9,	i+1,	tbusData.getDownTime(),wcsB)); 
	                ws.addCell(new	Label(10,	i+1,	tbusData.getDeduction1(),wcfN)); 
	                ws.addCell(new	Label(11,	i+1,	tbusData.getSubject1(),wcsB)); 
	                ws.addCell(new	Label(12,	i+1,	tbusData.getDeduction2(),wcfN)); 
	                ws.addCell(new	Label(13,	i+1,	tbusData.getSubject2(),wcsB)); 
	                ws.addCell(new	Label(14,	i+1,	tbusData.getDeduction3(),wcfN)); 
	                ws.addCell(new	Label(15,	i+1,	tbusData.getSubject3 (),wcsB)); 
	                ws.addCell(new	Label(16,	i+1,	tbusData.getDeduction4(),wcfN)); 
	                ws.addCell(new	Label(17,	i+1,	tbusData.getSubject4 (),wcsB)); 
	                ws.addCell(new	Label(18,	i+1,	tbusData.getDeduction5(),wcfN)); 
	                ws.addCell(new	Label(19,	i+1,	tbusData.getSubject5 (),wcsB)); 
	                ws.addCell(new	Label(20,	i+1,	tbusData.getDeduction6(),wcfN)); 
	                ws.addCell(new	Label(21,	i+1,	tbusData.getSubject6  (),wcsB)); 
	                ws.addCell(new	Label(22,	i+1,	tbusData.getDeduction7(),wcfN)); 
	                ws.addCell(new	Label(23,	i+1,	tbusData.getSubject7  (),wcsB)); 
	                ws.addCell(new	Label(24,	i+1,	tbusData.getDeduction8(),wcfN)); 
	                ws.addCell(new	Label(25,	i+1,	tbusData.getSubject8   (),wcsB)); 
	                ws.addCell(new	Label(26,	i+1,	tbusData.getDeduction9 (),wcfN)); 
	                ws.addCell(new	Label(27,	i+1,	tbusData.getSubject9   (),wcsB)); 
	                ws.addCell(new	Label(28,	i+1,	tbusData.getDeduction10 (),wcfN)); 
	                ws.addCell(new	Label(29,	i+1,	tbusData.getSubject10   (),wcsB)); 
	                ws.addCell(new	Label(30,	i+1,	tbusData.getDeduction11 (),wcfN)); 
	                ws.addCell(new	Label(31,	i+1,	tbusData.getSubject11   (),wcsB)); 
	                ws.addCell(new	Label(32,	i+1,	tbusData.getDeduction12  (),wcfN)); 
	                ws.addCell(new	Label(33,	i+1,	tbusData.getSubject12   (),wcsB)); 
	                ws.addCell(new	Label(34,	i+1,	tbusData.getDeduction13   (),wcfN)); 
	                ws.addCell(new	Label(35,	i+1,	tbusData.getSubject13     (),wcsB)); 
	                ws.addCell(new	Label(36,	i+1,	tbusData.getDeduction14   (),wcfN)); 
	                ws.addCell(new	Label(37,	i+1,	tbusData.getSubject14     (),wcsB)); 
	                ws.addCell(new	Label(38,	i+1,	tbusData.getDeduction15   (),wcfN)); 
	                ws.addCell(new	Label(39,	i+1,	tbusData.getSubject15     (),wcsB)); 
	                ws.addCell(new	Label(40,	i+1,	tbusData.getDeduction16   (),wcfN)); 
	                ws.addCell(new	Label(41,	i+1,	tbusData.getSubject16     (),wcsB)); 
	                ws.addCell(new	Label(42,	i+1,	tbusData.getDeduction17   (),wcfN)); 
	                ws.addCell(new	Label(43,	i+1,	tbusData.getSubject17      (),wcsB)); 
	                ws.addCell(new	Label(44,	i+1,	tbusData.getDeduction18   (),wcfN)); 
	                ws.addCell(new	Label(45,	i+1,	tbusData.getSubject18     (),wcsB)); 
	                ws.addCell(new	Label(46,	i+1,	tbusData.getDeduction19   (),wcfN)); 
	                ws.addCell(new	Label(47,	i+1,	tbusData.getSubject19     (),wcsB)); 
	                ws.addCell(new	Label(48,	i+1,	tbusData.getDeduction20    (),wcfN)); 
	                ws.addCell(new	Label(49,	i+1,	tbusData.getSubject20      (),wcsB)); 
	                ws.addCell(new	Label(50,	i+1,	tbusData.getDeduction21   (),wcfN)); 
	                ws.addCell(new	Label(51,	i+1,	tbusData.getSubject21      (),wcsB)); 
	                ws.addCell(new	Label(52,	i+1,	tbusData.getDeduction22    (),wcfN)); 
	                ws.addCell(new	Label(53,	i+1,	tbusData.getSubject22      (),wcsB)); 
	                ws.addCell(new	Label(54,	i+1,	tbusData.getDeduction23 (),wcfN)); 
	                ws.addCell(new	Label(55,	i+1,	tbusData.getSubject23 (),wcsB)); 
	                ws.addCell(new	Label(56,	i+1,	tbusData.getDeduction24(),wcfN)); 
	                ws.addCell(new	Label(57,	i+1,	tbusData.getSubject24(),wcsB)); 
	                ws.addCell(new	Label(58,	i+1,	tbusData.getDeduction25 (),wcfN)); 
	                ws.addCell(new	Label(59,	i+1,	tbusData.getSubject25    (),wcsB)); 
	                ws.addCell(new	Label(60,	i+1,	tbusData.getDeduction26  (),wcfN)); 
	                ws.addCell(new	Label(61,	i+1,	tbusData.getSubject26    (),wcsB)); 
	                ws.addCell(new	Label(62,	i+1,	tbusData.getDeduction27  (),wcfN)); 
	                ws.addCell(new	Label(63,	i+1,	tbusData.getSubject27     (),wcsB)); 
	                ws.addCell(new	Label(64,	i+1,	tbusData.getDeduction28    (),wcfN)); 
	                ws.addCell(new	Label(65,	i+1,	tbusData.getSubject28    (),wcsB)); 
	                ws.addCell(new	Label(66,	i+1,	tbusData.getDeduction29   (),wcfN)); 
	                ws.addCell(new	Label(67,	i+1,	tbusData.getSubject29     (),wcsB)); 
	                ws.addCell(new	Label(68,	i+1,	tbusData.getDeduction30   (),wcfN)); 
	                ws.addCell(new	Label(69,	i+1,	tbusData.getSubject30   (),wcsB)); 
	                ws.addCell(new	Label(70,	i+1,	tbusData.getDeduction31 (),wcfN)); 
	                ws.addCell(new	Label(71,	i+1,	tbusData.getSubject31   (),wcsB)); 
	                ws.addCell(new	Label(72,	i+1,	tbusData.getTicketBus  (),wcfN)); 

					 
					
					tbusData = getFloat(tbusData); 
		            ws.addCell(new jxl.write.Number(73,i+1,Double.parseDouble(tbusData.getDuplNumber()),wcfN));
		            ws.addCell(new jxl.write.Number(74,i+1,Double.parseDouble(getSX(tbusData.getDuplNumber())),wcfN));
	                
	        } catch (WriteException e) {
	            e.printStackTrace();
	        }
		}
		
        try {
            // ???
            wwb.write();
            // ???
            wwb.close();
            // ?????
            //Runtime.getRuntime().exec("cmd.exe /c start  D:\\test\\");
        } catch (WriteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
		 
		return 0;
	}
	
	
	/**
	 * ????????
	 */
	public int stat(String beginTime,String endTime,String tableStr){
		//System.out.println("????????" + dayStr);
		/**????????????*/
		 
//		/**?????????Map?*/
//		Map map_param = new HashMap();
//		map_param.put("beginTime",beginTime);
//		map_param.put("endTime",endTime);
//		List list_bus =   tbusDataDao.getListByDate(map_param);
//		String tomcatPath=SParametersHelper.getValue("TOMCATPATH");
//		File f = new File(tomcatPath+"/webapps/Bus/file/stat_bus.xls"); 
//        WritableWorkbook wwb = null;
//        // ??Excel???
//        WritableSheet ws = null; 
//        try {
//            f.createNewFile();
//            wwb = Workbook.createWorkbook(f); 
//            ws = wwb.createSheet("??????????", 0);// ??sheet
//             
//            	Label lable1 = new Label(0, 0, "??");
//            	Label lable2 = new Label(1, 0, "??");
//            	Label lable3 = new Label(2, 0, "????");
//            	Label lable4 = new Label(3, 0, "????");
//            	ws.addCell(lable1);
//            	ws.addCell(lable2);
//            	ws.addCell(lable3);
//            	ws.addCell(lable4);
//            //}
//        } catch (WriteException e) {
//        	
//        } catch (IOException e) { 
//            e.printStackTrace();
//        }
//        
//		for(int i=0;null!=list_bus&&i<list_bus.size();i++){
//			TbusData tbusData = (TbusData)list_bus.get(i);
//			try {
//				WritableFont fontNormal= new   WritableFont(WritableFont.ARIAL,9);  //????
//				
//
//	            
//	            
//	        } catch (WriteException e) {
//	            e.printStackTrace();
//	        }
//		}
//		
//        try {
//            // ???
//            wwb.write();
//            // ???
//            wwb.close();
//            // ?????
//            //Runtime.getRuntime().exec("cmd.exe /c start  D:\\test\\");
//        } catch (WriteException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        
		 
		return 0;
	}
	/**
	 * @param tbusData
	 * @return
	 */
	private static TbusData   getFloat(TbusData tbusData){
		int count=0;
		count = count	+	myParseInt((null==tbusData.getDeduction1  () 	||	tbusData.getDeduction1  () .length()<1		)	 ?	"0"	:		tbusData.getDeduction1  () 		);
		count = count	+	myParseInt((null==tbusData.getDeduction2  () 	||	tbusData.getDeduction2  () .length()<1		)	 ?	"0"	:		tbusData.getDeduction2  () 		);
		count = count	+	myParseInt((null==tbusData.getDeduction3  () 	||	tbusData.getDeduction3  () .length()<1		)	 ?	"0"	:		tbusData.getDeduction3  () 		);
		count = count	+	myParseInt((null==tbusData.getDeduction4  () 	||	tbusData.getDeduction4  () .length()<1		)	 ?	"0"	:		tbusData.getDeduction4  () 		);
		count = count	+	myParseInt((null==tbusData.getDeduction5  () 	||	tbusData.getDeduction5  () .length()<1		)	 ?	"0"	:		tbusData.getDeduction5  () 		);
		count = count	+	myParseInt((null==tbusData.getDeduction6  () 	||	tbusData.getDeduction6  () .length()<1		)	 ?	"0"	:		tbusData.getDeduction6  () 		);
		count = count	+	myParseInt((null==tbusData.getDeduction7  () 	||	tbusData.getDeduction7  () .length()<1		)	 ?	"0"	:		tbusData.getDeduction7  () 		);
		count = count	+	myParseInt((null==tbusData.getDeduction8  () 	||	tbusData.getDeduction8  () .length()<1		)	 ?	"0"	:		tbusData.getDeduction8  () 		);
		count = count	+	myParseInt((null==tbusData.getDeduction9  () 	||	tbusData.getDeduction9  () .length()<1		)	 ?	"0"	:		tbusData.getDeduction9  () 		);
		count = count	+	myParseInt((null==tbusData.getDeduction10 () 	||	tbusData.getDeduction10 () .length()<1		)	 ?	"0"	:		tbusData.getDeduction10 () 		);
		count = count	+	myParseInt((null==tbusData.getDeduction11 () 	||	tbusData.getDeduction11 () .length()<1		)	 ?	"0"	:		tbusData.getDeduction11 () 		);
		count = count	+	myParseInt((null==tbusData.getDeduction12  ()	||	tbusData.getDeduction12  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction12  ()		);
		count = count	+	myParseInt((null==tbusData.getDeduction13  ()	||	tbusData.getDeduction13  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction13  ()		);
		count = count	+	myParseInt((null==tbusData.getDeduction14  ()	||	tbusData.getDeduction14  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction14  ()		);
		count = count	+	myParseInt((null==tbusData.getDeduction15 () 	||	tbusData.getDeduction15 () .length()<1		)	 ?	"0"	:		tbusData.getDeduction15 () 		);
		count = count	+	myParseInt((null==tbusData.getDeduction16 () 	||	tbusData.getDeduction16 () .length()<1		)	 ?	"0"	:		tbusData.getDeduction16 () 		); 
		count = count	+	myParseInt((null==tbusData.getDeduction17  ()	||	tbusData.getDeduction17  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction17  ()		);
		count = count	+	myParseInt((null==tbusData.getDeduction18 () 	||	tbusData.getDeduction18 () .length()<1		)	 ?	"0"	:		tbusData.getDeduction18 () 		);
		count = count	+	myParseInt((null==tbusData.getDeduction19 () 	||	tbusData.getDeduction19 () .length()<1		)	 ?	"0"	:		tbusData.getDeduction19 () 		);
		count = count	+	myParseInt((null==tbusData.getDeduction20  ()	||	tbusData.getDeduction20  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction20  ()		); 
		count = count	+	myParseInt((null==tbusData.getDeduction21  ()	||	tbusData.getDeduction21  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction21  ()		); 
		count = count	+	myParseInt((null==tbusData.getDeduction22  ()	||	tbusData.getDeduction22  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction22  ()		);
		count = count	+	myParseInt((null==tbusData.getDeduction23  ()	||	tbusData.getDeduction23  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction23  ()		);
		count = count	+	myParseInt((null==tbusData.getDeduction24  ()	||	tbusData.getDeduction24  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction24  ()		);
		count = count	+	myParseInt((null==tbusData.getDeduction25  ()	||	tbusData.getDeduction25  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction25  ()		);
		count = count	+	myParseInt((null==tbusData.getDeduction26  ()	||	tbusData.getDeduction26  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction26  ()		);
		count = count	+	myParseInt((null==tbusData.getDeduction27  ()	||	tbusData.getDeduction27  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction27  ()		); 
		count = count	+	myParseInt((null==tbusData.getDeduction28  ()	||	tbusData.getDeduction28  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction28  ()		); 
		count = count	+	myParseInt((null==tbusData.getDeduction29  ()	||	tbusData.getDeduction29  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction29  ()		);  
		count = count	+	myParseInt((null==tbusData.getDeduction30  ()	||	tbusData.getDeduction30  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction30  ()		); 
		count = count	+	myParseInt((null==tbusData.getDeduction31  ()	||	tbusData.getDeduction31  ().length()<1		)	 ?	"0"	:		tbusData.getDeduction31  ()		); 
		tbusData.setDuplNumber(String.valueOf(count));
		return tbusData;
	}
	
	private static int myParseInt(String value){
		value = value.replaceAll("?", "");
		value = value.replaceAll(" ", "");
		if(null!=value&&value.length()>0){
			return Integer.parseInt(value);
		}else{
			return 0;
		}
	}
	private static String getSX(String value){
		value = value.replaceAll(" ", "");
		int count=0;
		if(null!=value&&value.length()>0){
			count = 100+ Integer.parseInt(value);
		} 
		return String.valueOf(count);
	}
	public static void main(String[] as){
		 int a=-22;
		 int b=100;
		 
		System.out.println(b+a);
	}
}


