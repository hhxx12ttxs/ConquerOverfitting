package by.q64.promo.utils.form;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import by.q64.promo.dao.BaseRequest;
import by.q64.promo.data.GlobalSettings;
import by.q64.promo.domain.AppUser;
import by.q64.promo.domain.Project;
import by.q64.promo.domain.Region;
import by.q64.promo.domain.TrastPromoShedule;
import by.q64.promo.domain.UnitRegion;
import by.q64.promo.utils.form.domain.SchedulleCell;

@Controller
public class Exel {

	@Autowired
	private BaseRequest baseRequest;

	public static final short yellow = 1;
	public static final short blue = 100;
	public static final short vinous = 10000;
	
	private Logger logger = LoggerFactory.getLogger(getClass().getName());

	//private XSSFWorkbook workbook = null;
	private StylesTable styletable = null;
	
	@Transactional
	@RequestMapping(value = "api/schedule/get/excel")
	public void getSchedule(Principal principal, int city, int project,
			String date, HttpServletResponse response) {
		long timeStart = System.currentTimeMillis();
		String folderFiles = GlobalSettings.folderFiles;// System.getProperty("java.io.tmpdir");
		
		//TODO fix me
		//if (System.getProperty("os.name") == "Linux") {
			folderFiles += GlobalSettings.fileSeparator;
		//}
		
		String filePath = folderFiles + "workbook" + timeStart + ".xlsx";
		String fileTemplate = folderFiles + "Consolidated_schedule_work_PPS_template.xlsx";
		
		
		if (copyFile(new File(fileTemplate), new File(filePath))) {
			FileOutputStream out = null;
			try {
				FileInputStream fileInputStream = new FileInputStream(new File(
						fileTemplate));
				XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
				styletable = workbook.getStylesSource();
				Sheet sheet = workbook.getSheetAt(0);
				LocalDate localDate = LocalDate.now();
				
				try {
					setSchedule(city, project, principal.getName(), localDate, sheet);
				} catch (Exception e) {
					e.printStackTrace();
				}
				

				// the code never gets to this point because of OutOfMemoryError
				out = new FileOutputStream(filePath);
				workbook.write(out);

				response.setHeader("Content-Disposition",
						"attachment;filename=report.xlsx");
				InputStream is = new FileInputStream(filePath);

				IOUtils.copy(is, response.getOutputStream());
				response.flushBuffer();

			} catch (FileNotFoundException ex) {
//				Logger logger = LoggerFactory.getLogger(getClass());
//				logger.error("Файла на этом компьютере больше нет. " + filePath);
				ex.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		} else {
			logger.error("FILE NOT COPIED");
			logger.info("FROM : " + fileTemplate);
			logger.info("TO : " +  filePath);
			
		}
		//workbook = null;
		styletable = null;
	}
	
	private void setSchedule(int regionId, int projectId, String email,LocalDate localDate, Sheet sheet) {
		LocalDate monday = getCurrentMonday(localDate);
		
		Region region = baseRequest.getEntity(Region.class, regionId);
		AppUser appUser = baseRequest.getUserFromEmail(email);
		List<TrastPromoShedule> trastPromoShedules = getTrastPromoSheduleSEVENDAYSForUser(regionId,monday,projectId,appUser);
		List<AppUser> supervisors = baseRequest.getRegionAppUsers(regionId,AppUser.SUPERVISOR);
		List<AppUser> promoters = baseRequest.getRegionAppUsers(regionId,AppUser.PROMOTER);
		List<UnitRegion> unitRegions = region.getUnitRegions();
		Project project = baseRequest.getEntity(Project.class, projectId);
		setData(trastPromoShedules,unitRegions,monday,appUser.getRole(),supervisors, sheet,promoters,project);
	}

	
	
	private void setData(List<TrastPromoShedule> trastPromoShedules,
			List<UnitRegion> unitRegions, LocalDate monday, int role,
			List<AppUser> supervisors, Sheet sheet, List<AppUser> promoters, Project project) {
		int numberRow = 10;
		int numberShop = 1;
		setHead(sheet,monday,project.getProjecName(),project.getClients().get(0).getName());		
		
		
		Map<Integer, List<UnitRegion>> shopsByUnit = sortUnit(unitRegions);
//		Set<Integer> shopsNetworkIds = shopsByUnit.keySet();
//		shopsNetworkIds = shopsByUnit.keySet();
		List<Integer> unitsId = new ArrayList<Integer>();
//		unitsId.addAll(shopsNetworkIds);
//		Collections.sort(unitsId);
		unitsId.add(3);
		unitsId.add(1);
		unitsId.add(4);
		unitsId.add(5);
		
		for (Integer integer : unitsId) {
			List<UnitRegion> unitRegionsUnit = shopsByUnit.get(integer);
			
			for (UnitRegion unitRegion : unitRegionsUnit) {				
				List<SchedulleCell> sc = new ArrayList<SchedulleCell>();
				for (int i = 0; i < 7; i++) {
					TrastPromoShedule promoShedule = getTPS(monday,i,trastPromoShedules,unitRegion);
					if (promoShedule == null) {
						SchedulleCell schedulleCell = SchedulleCell.empty();
						sc.add(schedulleCell);
					} else {
						int promoterId = promoShedule.getPromoter();
						AppUser promoter = findAppUser(promoters, promoterId);
						Integer cInt = 0;
						try {
							cInt =Integer.valueOf(promoShedule.getColour().getValue().toLowerCase().substring(1,7),16);
						} catch (Exception ex){
							ex.printStackTrace();
						}
						SchedulleCell schedulleCell = new SchedulleCell(
								promoter.getFullName(), 
								promoShedule.getStart().toLocalDateTime(), 
								promoShedule.getStop().toLocalDateTime(),
								cInt
								);
						sc.add(schedulleCell);
					}
				}
				String supervisorName = "";
				try {
					int supervisorId = unitRegion.getSupervisorId();
					AppUser supervisorUser = baseRequest.getEntity(AppUser.class, supervisorId);
					supervisorName = supervisorUser.getFullName();
				} catch (Exception ex) {
				}
				int  rowNumber= numberRow+numberShop*2;
				setRow(sheet,rowNumber ,numberShop, unitRegion.getName(), sc ,supervisorName, unitRegion.getUnit().getUnitName(), unitRegion.getSubway());
				numberShop++;
			}
			numberRow++;
		}
		
	}

	public void setHead(Sheet sheet,LocalDate localDate, String project, String client) {
		Row rowDate = sheet.getRow(9);
		for (int i = 0; i<8; i++) {
			Cell cellDayDate = rowDate.getCell(i+3);
			cellDayDate.setCellValue(localDate.plusDays((long) (i)).toString());
		}
		
		//Client
		Row rowClient = sheet.getRow(1);
		Cell cellClient = rowClient.getCell(2);
		cellClient.setCellValue(client);

		//Project
		Row rowProject = sheet.getRow(2);
		Cell cellProject = rowProject.getCell(2);
		cellProject.setCellValue(project);

		//Data
		Row rowData = sheet.getRow(4);
		Cell cellData = rowData.getCell(2);
		cellData.setCellValue(LocalDate.now().toString());
	}
	
	
	
	private void setRow(Sheet sheet,int rowNumber, final int numberShop, String shopName, List<SchedulleCell> sc,String supervisor, String shopNetworkName,String subWay) {
		Row rowPromoter = sheet.getRow(rowNumber-1);
		Row rowTime = sheet.getRow(rowNumber);

		Cell cellNumber = rowPromoter.getCell(0);
		cellNumber.setCellValue(numberShop);

		Cell cellAddres = rowPromoter.getCell(1);
		cellAddres.setCellValue(shopName);
		
		Cell cellSubWay = rowPromoter.getCell(2);
		cellSubWay.setCellValue(subWay);
		
		//sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber+1, 1, 1));
//		Cell cellShop = rowTime.createCell(1);
//		cellShop.setCellValue(shopNetworkName);
		
		int column = 3;
		for( ; column < 10; column++) {
			setSchedulleCell(column, rowPromoter, rowTime, sc);
		}
		Cell cellSupervisor = rowPromoter.getCell(column);
		cellSupervisor.setCellValue(supervisor);
		
	}
	
	private void setSchedulleCell(int column, Row rowPromoter, Row rowTime,List<SchedulleCell> sc) {
		try {
			SchedulleCell schedulleCell = sc.get(column-3);
			
		
			Cell cellDayPromoter = rowPromoter.getCell(column); 
			cellDayPromoter.setCellValue(schedulleCell.getPromoterName());
			
			
			Cell cellDayTime = rowTime.getCell(column); 
			cellDayTime.setCellValue(schedulleCell.getPeriod());
			
			
			
			if (schedulleCell.getColour() != 0) {
				Color myColor = new Color(schedulleCell.getColour());
				logger.info(myColor.toString());
				XSSFCellStyle style = new XSSFCellStyle(styletable);
				XSSFColor color = new XSSFColor(myColor);
				style.setFillBackgroundColor(color);
				style.setFillForegroundColor(color);
				cellDayPromoter.setCellStyle(style);
				cellDayTime.setCellStyle(style);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	private Map<Integer, List<UnitRegion>> sortUnit(List<UnitRegion> unitRegions) {
		Map<Integer, List<UnitRegion>> map = new HashMap<Integer, List<UnitRegion>>();
		for (UnitRegion unitRegion : unitRegions) {
			List<UnitRegion> unitRegionsUnit = null;
			int unitId = unitRegion.getUnit().getId();
			try {
				unitRegionsUnit = map.get(unitId);
				unitRegionsUnit.add(unitRegion);
				map.put(unitId, unitRegionsUnit);
			} catch (NullPointerException ex) {
				unitRegionsUnit = new ArrayList<UnitRegion>();
				unitRegionsUnit.add(unitRegion);
				map.put(unitId, unitRegionsUnit);
			}
		}
		return map;
	}

	public LocalDate getCurrentMonday(LocalDate localDate) {
		int dayValue = localDate.getDayOfWeek().getValue();
		return localDate.plusDays(1-dayValue);
	}
	
	private List<TrastPromoShedule> getTrastPromoSheduleSEVENDAYSForUser(
			int regionId, LocalDate monday, int project, AppUser appUser) {
		int role = appUser.getRole();
		List<TrastPromoShedule> trastPromoShedules = null;
		switch (role) {
		case AppUser.PROMOTER:
			trastPromoShedules = baseRequest.getTrastPromoSheduleSEVENDAYS(regionId,monday,project,appUser.getId(),null);
			break;

		case AppUser.SUPERVISOR:
			trastPromoShedules = baseRequest.getTrastPromoSheduleSEVENDAYS(regionId, monday, project, null, appUser.getId());
			break;

		case AppUser.MANAGER:
			trastPromoShedules = baseRequest.getTrastPromoSheduleSEVENDAYS(regionId,monday,project,null,null);
			break;
			
		default:
			trastPromoShedules = new ArrayList<TrastPromoShedule>();
			break;
		}
		
		return trastPromoShedules;
	}
	
	private TrastPromoShedule getTPS(LocalDate monday, int i,
			List<TrastPromoShedule> trastPromoShedules, UnitRegion unitRegion) {
		if (trastPromoShedules == null) {
			return null; 
		}
		LocalDateTime date = monday.atStartOfDay().plusDays(i);
				
		LocalDateTime date2 = date.plusDays(1);
		
		int s = trastPromoShedules.size();
		TrastPromoShedule promoSheduleReturn = null;
		for (int j = 0; j < s; j++) {
			TrastPromoShedule promoShedule = trastPromoShedules.get(j);
			if (promoShedule.getShop() == unitRegion.getId()) {
				LocalDateTime pst = promoShedule.getStart().toLocalDateTime();
				//if ((dl < pst) && (dl2> pst)) {
				if ( (pst.compareTo(date) > 0) && (date2.compareTo(pst) > 0) ) {
					//trastPromoShedules.remove(j);
					return promoShedule;
				}
			}
		}
		return promoSheduleReturn;
	}
	
	private AppUser findAppUser(List<AppUser> users, int userId) {
		for (AppUser appUser : users) {
			if (appUser.getId() == userId) {
				return appUser;
			}
		}
		logger.error("I can not find supervisor id = " + userId);
		for (AppUser appUser : users) {
			logger.error(appUser.toString());
		}
		return new AppUser().setFirstname("").setSurname("");
	}
	
	/**
	 * Copies file from source to dest
	 *
	 * @param source - source file
	 * @param dest - dest file
	 * @author Andrei
	 * @return True only if file copied without Exceptions
	 */
	public static Boolean copyFile(File source, File dest) {
		FileInputStream is = null;
		FileOutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			int nLength;
			byte[] buf = new byte[8000];
			while (true) {
				nLength = is.read(buf);
				if (nLength < 0) {
					break;
				}
				os.write(buf, 0, nLength);
			}
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return false;
	}
	
	
}

