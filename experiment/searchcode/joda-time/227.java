/**
 * 
 */
package de.fzi.hiwitool.db.hibernateImp.services.business.contract;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.RollbackException;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;







import de.fzi.hiwitool.business.contract.IContractBusiness;
import de.fzi.hiwitool.business.contract.util.ContractUtil;
import de.fzi.hiwitool.business.log.MyLoggerFactory;
import de.fzi.hiwitool.business.timesheet.util.TimesheetUtil;
import de.fzi.hiwitool.db.entity.IProject;
import de.fzi.hiwitool.db.entity.IUser;
import de.fzi.hiwitool.db.hibernateImp.tables.contract.Contract;
import de.fzi.hiwitool.db.hibernateImp.tables.contract.dao.IContractDAO;
import de.fzi.hiwitool.db.hibernateImp.tables.extension.Extension;
import de.fzi.hiwitool.db.hibernateImp.tables.extension.ExtensionState;
import de.fzi.hiwitool.db.hibernateImp.tables.extension.dao.IExtensionDAO;
import de.fzi.hiwitool.db.hibernateImp.tables.newcontract.NewContract;
import de.fzi.hiwitool.db.hibernateImp.tables.newcontract.NewContractState;
import de.fzi.hiwitool.db.hibernateImp.tables.newcontract.dao.INewContractDAO;
import de.fzi.hiwitool.db.hibernateImp.tables.project.Project;
import de.fzi.hiwitool.db.hibernateImp.tables.project.dao.IProjectDAO;
import de.fzi.hiwitool.db.hibernateImp.tables.timesheet.WorkAttest;
import de.fzi.hiwitool.db.hibernateImp.tables.timesheet.WorkRecord;
import de.fzi.hiwitool.db.hibernateImp.tables.timesheet.dao.IWorkAttestDAO;
import de.fzi.hiwitool.db.hibernateImp.tables.user.Admin;
import de.fzi.hiwitool.db.hibernateImp.tables.user.Adviser;
import de.fzi.hiwitool.db.hibernateImp.tables.user.HiWi;
import de.fzi.hiwitool.db.hibernateImp.tables.user.User;
import de.fzi.hiwitool.db.hibernateImp.tables.user.dao.IUserDAO;
import de.fzi.hiwitool.db.hibernateImp.tables.workingfield.dao.IWorkingFieldDAO;
import de.fzi.hiwitool.util.DateUtil;

/**
 * @author Ningyuan
 *
 */
@Service
@Scope("singleton")
public class ContractBusinessImp implements IContractBusiness {
	
	private static final Logger	logger	= MyLoggerFactory.getLogger(ContractBusinessImp.class);
	
	private final String WORKING_FIELD_FREETIME = "Urlaub";
	
	private final String WORKING_FIELD_CARRY = "Uebertrag";
	
	@Autowired
	private IUserDAO _userDAO;
	
	@Autowired
	private IWorkAttestDAO	_waDAO;
	
	@Autowired
	private IWorkingFieldDAO	_wfDAO;
	
	@Autowired
	private IContractDAO	_conDAO;
	
	@Autowired
	private INewContractDAO _ncDAO;
	
	@Autowired
	private IExtensionDAO	_extDAO;
	
	@Autowired
	private IProjectDAO proDAO;
	
	/*
	 * Force to recalculate all info, except hiwi, block and print date.
	 * 
	 * Used to correct data, when system has wrong data before.
	 */
	private int splitContract(Contract con, Date sEnd, Date sBegin){
		logger.debug("splitContract() - Split Contract");
		
		Date begin = con.getBegin();
		Date End = con.getEnd();
		int hours = con.getHours();
		
		List<Date> con1 = ContractUtil.calculateTimesheetMonth(begin, sEnd);
		List<Date> con2 = ContractUtil.calculateTimesheetMonth(sBegin, End);
		List<WorkAttest> was = con.getAttests();
		TimesheetUtil.sortTimesheet(was, true);
		
		
		Date month;
		WorkAttest wa = null;
		// every month in 1st contract
		int i = 0;
		for(; i < con1.size(); i++){
			month = con1.get(i);
			wa = was.get(i);
			
			if(wa ==null || !DateUtil.isSameMonthAndYear(month, wa.getMonth())){
				// monat of timesheet is corrupt
				logger.error("splitContract() - TIMESHEET MONTH ERROR "+wa.getMonth()+" OF CONTRACT "+con.getId());
				return -1;
			}
			
			wa.setMonth(month);
			logger.debug("splitContract() - 1. contract: "+wa.getMonth());
		}
		
		
		
		int j = 0;
		int [] sb = DateUtil.getDayMonthYearInInt(sBegin);
		List<WorkAttest> was2 = new ArrayList<WorkAttest>();
		/*
		 * last timesheet in 1st contract changed to half month,
		 * first timesheet in 2nd contract changed to half month
		 */
		if(sb[0] == 15){
			/*
			 * last timesheet in 1st contract
			 */
			double freetime = ContractUtil.calculateFreeTime(hours, true);
			wa.setFreeTime(freetime);
			wa.setHoursTodo(Double.valueOf(hours/2));
			
			boolean done = false;
			List<WorkRecord> lwr = wa.getRecords();
			for(int k = 0; k < lwr.size(); k++){
				WorkRecord w = lwr.get(k);
				if(w.getWorkingField().getDescription().equals(WORKING_FIELD_FREETIME)){
					w.setHours(freetime);
					done = true;
					break;
				}
			}
			if(!done){
				WorkRecord wr = new WorkRecord();
				wr.setDate(wa.getMonth());
				wr.setHours(freetime);
				wr.setWorkingField(_wfDAO.getByDescription(WORKING_FIELD_FREETIME));
				lwr.add(wr);
			}
			
			/*
			 * first timesheet in 2nd contract
			 */
			WorkAttest firstWA = new WorkAttest();
			firstWA.setMonth(con2.get(j));
			firstWA.setHiwi(con.getHiwi());
			firstWA.setBlocked(wa.getBlocked());
			firstWA.setPrintDate(wa.getPrintDate());
			firstWA.setHoursTodo(Double.valueOf(hours/2));
			firstWA.setFreeTime(freetime);
			firstWA.setArea(wa.getArea());
			firstWA.setCostunit(wa.getCostunit());
			lwr = new ArrayList<WorkRecord> ();
			firstWA.setRecords(lwr);
			
			WorkRecord wr = new WorkRecord();
			wr.setDate(con2.get(j));
			wr.setHours(freetime);
			wr.setWorkingField(_wfDAO.getByDescription(WORKING_FIELD_FREETIME));
			lwr.add(wr);
			
			was2.add(firstWA);
			
			logger.debug("splitContract() - 2. contract: "+firstWA.getMonth());
			
			j++;
		}
		
		for(; j < con2.size(); j++){
			month = con2.get(j);
			wa = was.get(i++);
				
			if(wa == null || !DateUtil.isSameMonthAndYear(month, wa.getMonth())){
				// monat of timesheet is corrupt
				logger.error("splitContract() - TIMESHEET MONTH ERROR "+wa.getMonth()+" OF CONTRACT "+con.getId());
				return -1;
			}
			
			wa.setMonth(month);
			was2.add(wa);
			logger.debug("splitContract() - 2. contract: "+wa.getMonth());
		}
		
		// remove timesheets of 2nd contract from 1st contract
		while(was.size() > con1.size()){
			was.remove(was.size()-1);
		}
		
		// update con1
		con.setEnd(sEnd);
		
		_conDAO.update(con);
		
		// save con2
		Contract newCon = new Contract();
		newCon.setBegin(sBegin);
		newCon.setEnd(End);
		newCon.setHiwi(con.getHiwi());
		newCon.setHours(hours);
		newCon.setAttests(was2);
		
		_conDAO.save(newCon);
		return 0;
	}
	
	/*
	 * Force to recalculate all info, except hiwi, block and print date.
	 * 
	 * Used to correct data, when system has wrong data before.
	 */
	private int changeContract(Contract con, int hours, int cu, String area){
		
		logger.debug("changeContract() - Change Contract");
		
		Date end = con.getEnd();
		List<WorkAttest> was = con.getAttests();
		TimesheetUtil.sortTimesheet(was, true);
		List<Date> months = ContractUtil.calculateTimesheetMonth(con.getBegin(), end);
		
		
		for(int i = 0; i < was.size(); i++){
			Date month = months.get(i);
			WorkAttest wa = was.get(i);
			
			if(wa ==null || !DateUtil.isSameMonthAndYear(month, wa.getMonth())){
				// monat of timesheet is corrupt
				logger.error("changeContract() - TIMESHEET MONTH ERROR"+wa.getMonth()+" OF CONTRACT "+con.getId());
				return -1;
			}
			
			double freetime;
			int waHours = hours;
			
			
			// the first month of contract
			if(i == 0){
			
				String bd = DateUtil.getDayMonthYear(month)[0];
				// contract begins at 15th
				if(bd.equalsIgnoreCase("15")){
					freetime = ContractUtil.calculateFreeTime(hours, true);
					waHours = waHours/2;
				}
				// contract begings at 1st but contains only one month
				else if(was.size() == 1){
					String ed = DateUtil.getDayMonthYear(end)[0];
					// contract ends at 14th
					if(ed.equalsIgnoreCase("14")){
						freetime = ContractUtil.calculateFreeTime(hours, true);
						waHours = waHours/2;
					}
					// contract ends at end of month
					else{
						freetime = ContractUtil.calculateFreeTime(hours, false);
					}
				}
				// contract begins at 1st and contains many month
				else{
					freetime = ContractUtil.calculateFreeTime(hours, false);
				}
			}
			// the last month of contract
			else if(i == (was.size()-1)){
				String ed = DateUtil.getDayMonthYear(end)[0];
				// contract ends at 14th
				if(ed.equalsIgnoreCase("14")){
					freetime = ContractUtil.calculateFreeTime(hours, true);
					waHours = waHours/2;
				}
				// contract ends at end of month
				else{
					freetime = ContractUtil.calculateFreeTime(hours, false);
				}
			}
			// other months of contract
			else{
				freetime = ContractUtil.calculateFreeTime(hours, false);
			}
			
			// modify timesheet
			logger.debug("changeContract() - Modify Timesheet: " + wa.getMonth());
			wa.setMonth(month);
			logger.debug("changeContract() - month: "+wa.getMonth());
			wa.setArea(area);
			wa.setCostunit(cu);
			wa.setFreeTime(freetime);
			logger.debug("changeContract() - freetime: "+wa.getFreeTime());
			wa.setHoursTodo(Double.valueOf(waHours));
			logger.debug("changeContract() - hours: "+wa.getHoursTodo());
			
			boolean done = false;
			List<WorkRecord> lwr = wa.getRecords();
			for(int j = 0; j < lwr.size(); j++){
				WorkRecord wr = lwr.get(j);
				if(wr.getWorkingField().getDescription().equals(WORKING_FIELD_FREETIME)){
					logger.debug("changeContract() - Modify Workrecord freetime: "+ freetime);
					wr.setHours(freetime);
					done = true;
					break;
				}
			}
			if(!done){
				logger.debug("changeContract() - Create Workrecord freetime: "+ freetime);
				WorkRecord wr = new WorkRecord();
				wr.setDate(wa.getMonth());
				wr.setHours(freetime);
				wr.setWorkingField(_wfDAO.getByDescription(WORKING_FIELD_FREETIME));
				lwr.add(wr);
			}
			
		}
		
		// modify contract
		con.setHours(hours);
	
		
		// database write operation
		_conDAO.update(con);
		
		return 0;
	}
	
	/*
	 * Recalculate data only when it is nessearay
	 * 
	 * Used when system has not wrong data before.
	 */
	private int changeContract2(Contract con, int hours, int cu, String area){
		
		logger.debug("changeContract() - Change Contract 2");
		
		List<WorkAttest> was = con.getAttests();
		/*
		 * hours is changed
		 */
		if(con.getHours() != hours){
			TimesheetUtil.sortTimesheet(was, true);
			Date end = con.getEnd();
			
			for(int i = 0; i < was.size(); i++){
				double freetime;
				int waHours = hours;
				WorkAttest wa = was.get(i);
				
				// the first month of contract
				if(i == 0){
				
					Date d = was.get(i).getMonth();
					String bd = DateUtil.getDayMonthYear(d)[0];
					// contract begins at 15th
					if(bd.equalsIgnoreCase("15")){
						freetime = ContractUtil.calculateFreeTime(hours, true);
						waHours = waHours/2;
					}
					// contract begings at 1st but contains only one month
					else if(was.size() == 1){
						String ed = DateUtil.getDayMonthYear(end)[0];
						// contract ends at 14th
						if(ed.equalsIgnoreCase("14")){
							freetime = ContractUtil.calculateFreeTime(hours, true);
							waHours = waHours/2;
						}
						// contract ends at end of month
						else{
							freetime = ContractUtil.calculateFreeTime(hours, false);
						}
					}
					// contract begins at 1st and contains many month
					else{
						freetime = ContractUtil.calculateFreeTime(hours, false);
					}
				}
				// the last month of contract
				else if(i == (was.size()-1)){
					String ed = DateUtil.getDayMonthYear(end)[0];
					// contract ends at 14th
					if(ed.equalsIgnoreCase("14")){
						freetime = ContractUtil.calculateFreeTime(hours, true);
						waHours = waHours/2;
					}
					// contract ends at end of month
					else{
						freetime = ContractUtil.calculateFreeTime(hours, false);
					}
				}
				// other months of contract
				else{
					freetime = ContractUtil.calculateFreeTime(hours, false);
				}
				
				// modify timesheet
				logger.debug("changeContract() - Modify Timesheet: " + wa.getMonth());
				wa.setArea(area);
				wa.setCostunit(cu);
				wa.setFreeTime(freetime);
				logger.debug("changeContract() - freetime: "+wa.getFreeTime());
				wa.setHoursTodo(Double.valueOf(waHours));
				logger.debug("changeContract() - hours: "+wa.getHoursTodo());
				
				boolean done = false;
				List<WorkRecord> lwr = wa.getRecords();
				for(int j = 0; j < lwr.size(); j++){
					WorkRecord wr = lwr.get(j);
					if(wr.getWorkingField().getDescription().equals(WORKING_FIELD_FREETIME)){
						logger.debug("changeContract() - Modify Workrecord freetime: "+ freetime);
						wr.setHours(freetime);
						done = true;
						break;
					}
				}
				if(!done){
					logger.debug("changeContract() - Create Workrecord freetime: "+ freetime);
					WorkRecord wr = new WorkRecord();
					wr.setDate(wa.getMonth());
					wr.setHours(freetime);
					wr.setWorkingField(_wfDAO.getByDescription(WORKING_FIELD_FREETIME));
					lwr.add(wr);
				}
			}
		}
		else {
			for(int i = 0; i < was.size(); i++){
				WorkAttest wa = was.get(i);
				
				// modify timesheet
				logger.debug("changeContract() - Modify Timesheet: " + wa.getMonth());
				wa.setArea(area);
				wa.setCostunit(cu);
				
			}
		}
		
		// modify contract
		con.setHours(hours);
	
		
		// database write operation
		_conDAO.update(con);
		
		return 0;
	}
	
	/*
	 * pre condition: begin must before end
	 * 				  begin (1 or 15)
	 * 				  end   (14 or end)  
	 */
	@SuppressWarnings("unchecked")
	private int changeContract(Contract con, Date begin, Date end, int hours, int cu, String area){
		try {
			logger.debug("changeContract() - Change Contract");
			logger.debug("changeContract() - begin: "+begin);
			logger.debug("changeContract() - end: "+end);
			
			Object dates [] = ContractUtil.calculateChangedContract(con.getBegin(), con.getEnd(), begin, end);
			List<WorkAttest> was = con.getAttests();
			
			double carry = 0.0;
			double freetime;
			int waHours;
			
			/*
			 * timesheets to be deleted
			 */
			List<Date> del = (List<Date>) dates[0];
			
			for(int i = 0; i < del.size(); i++){
				Date dd = del.get(i);
				
				WorkAttest dwa = getTimesheetWithSameDate(was, dd);
				if(dwa != null){
					List<WorkRecord> wrs = dwa.getRecords();
					logger.debug("changeContract() - Delete Timesheet: "+dwa.getMonth());
					
					if(wrs.size() < 1){
						// delete timesheet, when it contains no info
						was.remove(dwa);
						_waDAO.deleteWorkAttest(dwa);
					}
					else if(wrs.size() < 3){
						for(int j = 0; j < wrs.size(); j++){
							WorkRecord wr = wrs.get(j);
							
							if(wr.getWorkingField().getDescription().equals(WORKING_FIELD_CARRY)){
								//
								carry = wr.getHours();
								logger.debug("changeContract() - Get carry from del timesheet: "+carry);
								
							}
							else if(wr.getWorkingField().getDescription().equals(WORKING_FIELD_FREETIME)){
								continue;
							}
							else{
								// can not delete timesheet
								logger.debug("changeContract() - CAN NOT DELETE WROTEN TIMESHEET: "+dwa.getMonth());
								return -2;
							}
						}
						
						// delete timesheet, when it contains only urlaub info and carry info
						was.remove(dwa);
						_waDAO.deleteWorkAttest(dwa);
					}
					else{
						// can not delete timesheet
						logger.debug("changeContract() - CAN NOT DELETE WROTEN TIMESHEET: "+dwa.getMonth());
						return -2;
					}
				}
				else{
					logger.error("changeContract() - CAN NOT FIND TIMESHEET "+dd+" OF CONTRACT "+con.getId());
				}
			}
			
			
			/*
			 * timesheets to be modified
			 */
			List<Date> mod = (List<Date>) dates[1];
			// every timesheet in old contract to delete
			for(int i = 0; i < mod.size(); i++){
				Date md = mod.get(i);
				int [] mdmy = DateUtil.getDayMonthYearInInt(md);
				WorkAttest mwa = getTimesheetWithSameDate(was, md);
				waHours = hours;
				
				/*
				 * modified timesheet with date dose not exist in old contract.
				 * 
				 * old contract has a timesheet of the same month
				 */
				if(mwa == null){
					mwa = getTimesheetWithSameMonth(was, md);
					
					if(mwa != null){
						logger.debug("changeContract() - Modify 2 Timesheet: " + mwa.getMonth());
						
						/*
						 * this month is contract's new begin
						 * contract's begin delayed to 15th
						 */
						if(mdmy[0] == 15){
							freetime = ContractUtil.calculateFreeTime(hours, true);
							waHours = waHours/2;
							
							// move carry info to the new 1st timesheet
							if(carry != 0){
								logger.debug("changeContract() - Set carry to mod timesheet: "+carry);
								WorkRecord wr = new WorkRecord();
								wr.setDate(md);
								wr.setWorkingField(_wfDAO.getByDescription(WORKING_FIELD_CARRY));
								wr.setHours(carry);
									
								carry = 0.0;
								mwa.getRecords().add(wr);
							}
						}
						/*
						 * this month is contract's old begin
						 * and contract's old begin starts from 15th
						 */
						else{
							freetime = ContractUtil.calculateFreeTime(hours, false);
							
							/*
							 *  try to get carry from contract's old begin,
							 *  if this is not the contract's new begin 
							 */
							if(DateUtil.dayCompareTo(md, begin) != 0){
								List<WorkRecord> wrs = mwa.getRecords();
								for(int j = 0 ; j < wrs.size(); j++){
									WorkRecord wr = wrs.get(j);
									if(wr.getWorkingField().getDescription().equals(WORKING_FIELD_CARRY)){
										
										carry = wr.getHours();
										logger.debug("changeContract() - Get carry from mod timesheet: "+carry);
										wrs.remove(wr);
										_waDAO.deleteWorkRecord(wr);
										break;
									}
								}
							}
						}
						
					}
					else{
						// modify timesheet dose not exist in old contract
						logger.error("changeContract() - NO MOD TIMESHEET: "+ md);
						return -3;
					}
				}
				/*
				 * modified timesheet with date exist in old contract.
				 * 
				 * overlap months of old and new contract.
				 */
				else{
					logger.debug("changeContract() - Modify 1 Timesheet: " + mwa.getMonth());
					/*
					 *  the first overlap month
					 *  this may be the old begin or the new begin
					 */
					if(i == 0){
						/*
						 * contract's begin is not changed
						 * started from 15th
						 */
						if(mdmy[0] == 15){
							freetime = ContractUtil.calculateFreeTime(hours, true);
							waHours = waHours/2;
						}
						/*
						 * this month started from 1st and
						 * contract ends in this month
						 */
						else if(DateUtil.isSameMonthAndYear(md, end)){
							int [] edmy = DateUtil.getDayMonthYearInInt(end);
							
							if(edmy[0] == 14){
								freetime = ContractUtil.calculateFreeTime(hours, true);
								waHours = waHours/2;
							}
							else{
								freetime = ContractUtil.calculateFreeTime(hours, false);
							}
									
							/*
							 *  if this is not the contract's new begin,
							 *  try to get carry from the old 1st timesheet 
							 */
							if(DateUtil.dayCompareTo(md, begin) != 0){
								List<WorkRecord> wrs = mwa.getRecords();
								for(int j = 0 ; j < wrs.size(); j++){
									WorkRecord wr = wrs.get(j);
									if(wr.getWorkingField().getDescription().equals(WORKING_FIELD_CARRY)){
										
										carry = wr.getHours();
										logger.debug("changeContract() - Get carry from mod timesheet: "+carry);
										wrs.remove(wr);
										_waDAO.deleteWorkRecord(wr);
										break;
									}
								}
							}
							/*
							 *  if this is the contract's new begin,
							 *  try to set carry to the new 1st timesheet
							 */
							else{
								if(carry != 0){
									logger.debug("changeContract() - Set carry to mod timesheet: "+carry);
									WorkRecord wr = new WorkRecord();
									wr.setDate(md);
									wr.setWorkingField(_wfDAO.getByDescription(WORKING_FIELD_CARRY));
									wr.setHours(carry);
										
									carry = 0.0;
									mwa.getRecords().add(wr);
								}
							}
						}
						/*
						 * this month started from 1st and
						 * contract dose not end in this month
						 */
						else{
							freetime = ContractUtil.calculateFreeTime(hours, false);
							
							/*
							 *  if this is not the contract's new begin, 
							 *  try to get carry from the old 1st timesheet
							 */
							if(DateUtil.dayCompareTo(md, begin) != 0){
								List<WorkRecord> wrs = mwa.getRecords();
								for(int j = 0 ; j < wrs.size(); j++){
									WorkRecord wr = wrs.get(j);
									if(wr.getWorkingField().getDescription().equals(WORKING_FIELD_CARRY)){
										
										carry = wr.getHours();
										logger.debug("changeContract() - Get carry from mod timesheet: "+carry);
										wrs.remove(wr);
										_waDAO.deleteWorkRecord(wr);
										break;
									}
								}
							}
							/*
							 *  if this is the contract's new begin,
							 *  try to set carry to the new 1st timesheet
							 */
							else{
								if(carry != 0){
									logger.debug("changeContract() - Set carry from mod timesheet: "+carry);
									WorkRecord wr = new WorkRecord();
									wr.setDate(md);
									wr.setWorkingField(_wfDAO.getByDescription(WORKING_FIELD_CARRY));
									wr.setHours(carry);
										
									carry = 0.0;
									mwa.getRecords().add(wr);
								}
							}
						}
					}
					/*
					 *  the last overlap month and not the first.
					 *  it can not start from 15th and can not be
					 *  old and new begin.
					 */
					else if(i == mod.size()-1){
						/*
						 * this month started from 1st and
						 * contract ends in this month
						 */
						if(DateUtil.isSameMonthAndYear(md, end)){
							int [] edmy = DateUtil.getDayMonthYearInInt(end);
							
							if(edmy[0] == 14){
								freetime = ContractUtil.calculateFreeTime(hours, true);
								waHours = waHours/2;
							}
							else{
								freetime = ContractUtil.calculateFreeTime(hours, false);
							}
						}
						/*
						 * this month started from 1st and
						 * contract dose not end in this month
						 */
						else{
							freetime = ContractUtil.calculateFreeTime(hours, false);
						}
					}
					/*
					 *  other overlap months
					 *  it can not be old and new begin.
					 *  
					 */
					else{
						freetime = ContractUtil.calculateFreeTime(hours, false);	
					}
				}
				
				// modify timesheet
				
				mwa.setMonth(md);
				logger.debug("changeContract() - month: "+mwa.getMonth());
				mwa.setFreeTime(freetime);
				logger.debug("changeContract() - freetime: "+mwa.getFreeTime());
				mwa.setHoursTodo(Double.valueOf(waHours));
				logger.debug("changeContract() - hours: "+mwa.getHoursTodo());
				mwa.setArea(area);
				mwa.setCostunit(cu);
				logger.debug("changeContract() - costunit: "+mwa.getCostunit());
				
				// modify freetime work record
				boolean done = false;
				List<WorkRecord> lwr = mwa.getRecords();
				for(int j = 0; j < lwr.size(); j++){
					WorkRecord w = lwr.get(j);
					if(w.getWorkingField().getDescription().equals(WORKING_FIELD_FREETIME)){
						logger.debug("changeContract() - Set Workrecord ");
						w.setHours(freetime);
						logger.debug("changeContract() - freetime: "+freetime);
						done = true;
						break;
					}
				}
				if(!done){
					logger.debug("changeContract() - Create Workrecord");
					WorkRecord wr = new WorkRecord();
					wr.setDate(mwa.getMonth());
					wr.setHours(freetime);
					logger.debug("changeContract() - freetime: "+freetime);
					wr.setWorkingField(_wfDAO.getByDescription(WORKING_FIELD_FREETIME));
					lwr.add(wr);
				}
			}
			
			
			/*
			 * timesheets to be added
			 */
			List<Date> add = (List<Date>) dates[2];
			//every timesheet to be added
			for(int i = 0; i < add.size(); i++){
				Date ad = add.get(i);
				int [] mdmy = DateUtil.getDayMonthYearInInt(ad);
				waHours = hours;
				
				WorkAttest awa = new WorkAttest();
				List<WorkRecord> wrs = new ArrayList<WorkRecord>();
				WorkRecord wr;
				
				logger.debug("changeContract() - Add Timesheet: " + ad);
				
				/*
				 *  the first add month
				 *  this may be the new begin
				 */
				if(i == 0){
					/*
					 * contract's new begin
					 * started from 15th
					 */
					if(mdmy[0] == 15){
						
						freetime = ContractUtil.calculateFreeTime(hours, true);
						waHours = waHours/2;
						
						// move carry info to the new 1st timesheet
						if(carry != 0){
							logger.debug("changeContract() - Set carry to add timesheet: "+carry);
							wr = new WorkRecord();
							wr.setDate(ad);
							wr.setWorkingField(_wfDAO.getByDescription(WORKING_FIELD_CARRY));
							wr.setHours(carry);
								
							
							carry = 0.0;
							wrs.add(wr);
						}
					}
					/*
					 * this month started from 1st and
					 * contract ends in this month
					 */
					else if(DateUtil.isSameMonthAndYear(ad, end)){
						int [] edmy = DateUtil.getDayMonthYearInInt(end);
						
						if(edmy[0] == 14){
							freetime = ContractUtil.calculateFreeTime(hours, true);
							waHours = waHours/2;
						}
						else{
							freetime = ContractUtil.calculateFreeTime(hours, false);
						}
						
						// try to move carry info to the new 1st timesheet
						if(DateUtil.dayCompareTo(ad, begin) == 0){
							if(carry != 0){
								logger.debug("changeContract() - Set carry to add timesheet: "+carry);
								wr = new WorkRecord();
								wr.setDate(ad);
								wr.setWorkingField(_wfDAO.getByDescription(WORKING_FIELD_CARRY));
								wr.setHours(carry);
									
								
								carry = 0.0;
								wrs.add(wr);
							}
						}
					}
					/*
					 * this month started from 1st and
					 * contract dose not ends in this month
					 */
					else{
						freetime = ContractUtil.calculateFreeTime(hours, false);
						
						// try to move carry info to the new 1st timesheet
						if(DateUtil.dayCompareTo(ad, begin) == 0){
							if(carry != 0){
								logger.debug("changeContract() - Set carry to add timesheet: "+carry);
								wr = new WorkRecord();
									
								wr.setDate(ad);
									
								wr.setWorkingField(_wfDAO.getByDescription(WORKING_FIELD_CARRY));
									
								wr.setHours(carry);
									
								
								carry = 0.0;
								wrs.add(wr);
							}
						}
					}
				}
				/*
				 *  the last add month and not the first.
				 *  it could not be new begin.
				 */
				else if(i == add.size()-1){
					/*
					 * this month started from 1st and
					 * contract ends in this month
					 */
					if(DateUtil.isSameMonthAndYear(ad, end)){
						int [] edmy = DateUtil.getDayMonthYearInInt(end);
						
						if(edmy[0] == 14){
							freetime = ContractUtil.calculateFreeTime(hours, true);
							waHours = waHours/2;
						}
						else{
							freetime = ContractUtil.calculateFreeTime(hours, false);
						}
					}
					/*
					 * this month started from 1st and
					 * contract dose not end in this month
					 */
					else{
						freetime = ContractUtil.calculateFreeTime(hours, false);
					}
				}
				/*
				 * other add months
				 */
				else{
					freetime = ContractUtil.calculateFreeTime(hours, false);
				}
				
				
				// add timesheets
				wr = new WorkRecord();
					logger.debug("changeContract() - Create WorkRecord");
				wr.setDate(ad);
					logger.debug("changeContract() - date: "+wr.getDate());
				wr.setWorkingField(_wfDAO.getByDescription(WORKING_FIELD_FREETIME));
					logger.debug("changeContract() - workingfield: "+wr.getWorkingField().getDescription());
			    wr.setHours(freetime);
				    logger.debug("changeContract() - hours: "+wr.getHours());
			
				wrs.add(wr);
				
				// create work attest
					logger.debug("changeContract() - Create Timesheet");
				awa.setMonth(ad);
					logger.debug("changeContract() - date: "+awa.getMonth());
				awa.setArea(area);
				awa.setBlocked(false);
				awa.setCostunit(cu);
					logger.debug("changeContract() - costunit: "+awa.getCostunit());
				awa.setFreeTime(freetime);
					logger.debug("changeContract() - freetime: "+awa.getFreeTime());
				awa.setHiwi(con.getHiwi());
				awa.setHoursTodo(Double.valueOf(waHours));
					logger.debug("changeContract() - hours: "+awa.getHoursTodo());
				awa.setRecords(wrs);
				
				was.add(awa);
			}
			
			// modify contract
			
			con.setBegin(begin);
			con.setEnd(end);
			con.setHours(hours);
			
			_conDAO.update(con);
			return 0;
			
		} catch (ParseException e) {
			return -1;
		}
	}
	
	/*
	 * Get timesheet with the same date
	 */
	private WorkAttest getTimesheetWithSameDate(List<WorkAttest> was, Date date){
		
		for(WorkAttest wa : was){
			if(DateUtil.dayCompareTo(wa.getMonth(), date) == 0){
				return wa;
			}
		}
		return null;
	}
	
	/*
	 * Get timesheet with the same month
	 */
	private WorkAttest getTimesheetWithSameMonth(List<WorkAttest> was, Date month){
		
		for(WorkAttest wa : was){
			if(DateUtil.isSameMonthAndYear(wa.getMonth(), month)){
				return wa;
			}
		}
		return null;
	}
	
	private int createContract(Date begin, Date end, HiWi hiwi, String area,
			int costUnit, int hours, double carry){
		
			List<WorkAttest> was = new ArrayList<WorkAttest>();
			
			List<Date> waMonths = ContractUtil.calculateTimesheetMonth(begin, end);
			for(int i=0; i < waMonths.size(); i++){
				WorkAttest w = _waDAO.getByHiwiAndMonth(hiwi, waMonths.get(i));
				if( w != null){
					// duplicated workattest
					logger.debug("createContract(): Duplicated Timesheet "+w.getMonth());
					return -2;
				}
			}
			
			for(int i = 0; i < waMonths.size(); i++){
				List<WorkRecord> wrs = new ArrayList<WorkRecord>();
				
				double freetime;
				int waHours = hours;
				
				// the first month of contract
				if(i == 0){
					// carry work record should be saved only in the first timesheet
					if(carry != 0){
						WorkRecord wr = new WorkRecord();
							logger.debug("Create new work record");
						wr.setDate(waMonths.get(i));
							logger.debug("Date: "+wr.getDate());
						wr.setWorkingField(_wfDAO.getByDescription(WORKING_FIELD_CARRY));
							logger.debug("Workingfield: "+wr.getWorkingField().getDescription());
						wr.setHours(carry);
							logger.debug("Carry: "+wr.getHours());
						wrs.add(wr);
					}
					
					Date d = waMonths.get(i);
					String bd = DateUtil.getDayMonthYear(d)[0];
					// contract begins at 15th
					if(bd.equalsIgnoreCase("15")){
						freetime = ContractUtil.calculateFreeTime(hours, true);
						waHours = waHours/2;
					}
					// contract begings at 1st but contains only one month
					else if(waMonths.size() == 1){
						String ed = DateUtil.getDayMonthYear(end)[0];
						// contract ends at 14th
						if(ed.equalsIgnoreCase("14")){
							freetime = ContractUtil.calculateFreeTime(hours, true);
							waHours = waHours/2;
						}
						// contract ends at end of month
						else{
							freetime = ContractUtil.calculateFreeTime(hours, false);
						}
					}
					// contract begins at 1st and contains many month
					else{
						freetime = ContractUtil.calculateFreeTime(hours, false);
					}
				}
				// the last month of contract
				else if(i == (waMonths.size()-1)){
					String ed = DateUtil.getDayMonthYear(end)[0];
					// contract ends at 14th
					if(ed.equalsIgnoreCase("14")){
						freetime = ContractUtil.calculateFreeTime(hours, true);
						waHours = waHours/2;
					}
					// contract ends at end of month
					else{
						freetime = ContractUtil.calculateFreeTime(hours, false);
					}
				}
				// other months of contract
				else{
					freetime = ContractUtil.calculateFreeTime(hours, false);
				}
				
				
				
				// create free time work record
				WorkRecord wr = new WorkRecord();
					logger.debug("Create new work record");
				wr.setDate(waMonths.get(i));
					logger.debug("Date: "+wr.getDate());
				wr.setWorkingField(_wfDAO.getByDescription(WORKING_FIELD_FREETIME));
					logger.debug("Workingfield: "+wr.getWorkingField().getDescription());
				wr.setHours(freetime);
					logger.debug("Hours: "+wr.getHours());
				
				wrs.add(wr);
			
				// create work attest
				WorkAttest wa = new WorkAttest();
					logger.debug("Create new work attest");
				wa.setMonth(waMonths.get(i));
					logger.debug("Date: "+wa.getMonth());
				wa.setArea(area);
				wa.setBlocked(false);
				wa.setCostunit(costUnit);
				wa.setFreeTime(freetime);
				wa.setHiwi(hiwi);
				wa.setHoursTodo(Double.valueOf(waHours));
					logger.debug("Hours: "+wa.getHoursTodo());
				wa.setRecords(wrs);
				was.add(wa);
			}
			
			// create contract
			Contract con = new Contract();
				logger.debug("Create new contract");
			con.setBegin(begin);
				logger.debug("Begin: "+con.getBegin());
			con.setEnd(end);
				logger.debug("End: "+con.getEnd());
			con.setHiwi(hiwi);
				logger.debug("Hiwi: "+con.getHiwi().getLastName());
			con.setHours(hours);
				logger.debug("Hours: "+con.getHours());
			con.setAttests(was);
			
			// database write operation
			_conDAO.save(con);
			
			return 0;
		
	}

	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {RuntimeException.class, RollbackException.class})
	public void allowNewContract(long actorID, long hiwiID, long ncID) throws RollbackException {
		try{
		// only admin use this function
		Admin actor = _userDAO.getAdminByIDAndActive(actorID, true);
		
		if(actor != null){
			HiWi hiwi = _userDAO.getHiWiByID(hiwiID);
			
			if(hiwi != null && hiwi.getActive()){
				//XXX orphan new contract without hiwi???
				NewContract nc = getNewContractWithHiwiAndId(hiwi, ncID);
				
				if(nc != null && nc.getState() == NewContractState.WAITING){
					
					// allow new contract
					List<Contract> contracts = _conDAO.getForHiwi(hiwi);
					Date begin = nc.getBegin();
					Date end = nc.getEnd();
					
					if(contracts != null && contracts.size() > 0){
						int ret = ContractUtil.checkDateInContracts(contracts, begin, end);
						switch (ret){
							case -1: {
								// new contract begin after end
								throw new RollbackException("-5");
							}
							case 0: {
								
								break;
							}
							default: {
								// overlapping 
								throw new RollbackException(String.valueOf(ret));
							}
						}
					}
					
					switch(createContract(begin, end, hiwi, nc.getArea(), nc.getCostunit(), nc.getHours(), nc.getCarry())){
						
						case -2:{
							// duplicated timesheetmonth
							throw new RollbackException("-4");
						}
						default:{
							_ncDAO.delete(nc);
							// succeed
							return;
						}
					}
				}
				else{
					// wrong new contract
					throw new RollbackException("-3");
				}
			}
			else{
				// no hiwi
				throw new RollbackException("-2");
			}
		}
		else{
			// no actor
			throw new RollbackException("-1");
		}
		}
		catch(HibernateException he){
			logger.error(he.getMessage());
			throw new RollbackException("-8");
		}
	}
	
	@Override
	@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = {RuntimeException.class, RollbackException.class})
	public void deleteNewContract(long actorID, long hiwiID, long ncID)
			throws RollbackException {
		try{
		// only admin use this function
		Admin actor = _userDAO.getAdminByID(actorID);
		
		if(actor != null && actor.getActive()){
			HiWi hiwi = _userDAO.getHiWiByID(hiwiID);
			
			if(hiwi != null){
				//XXX orphan new contract without hiwi???
				NewContract nc = getNewContractWithHiwiAndId(hiwi, ncID);
				
				if(nc != null){
					// deletion
					_ncDAO.delete(nc);
					return;
				}
				else{
					// wrong new contract
					throw new RollbackException("-3");
				}
			}
			else{
				// wrong hiwi
				throw new RollbackException("-2");
			}
		}
		else{
			// wrong actor
			throw new RollbackException("-1");
		}
		}
		catch(HibernateException he){
			logger.error(he.getMessage());
			throw new RollbackException("-8");
		}
	}
	
	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {RuntimeException.class, RollbackException.class})
	public int allowContractExtension(long actorID, long hiwiID, long extID)
			throws RollbackException {
		try{
		// only admin use this function
		Admin actor = _userDAO.getAdminByIDAndActive(actorID, true);
		if(actor != null){
			
			Extension ext = _extDAO.getByID(extID);
				
			if(ext != null && ext.getState() == ExtensionState.REQUESTED){
				HiWi hiwi = ext.getHiwi();
					
				if(hiwi != null){
					if(hiwi.getActive() && hiwi.getId() == hiwiID){
							// allow extension
							Date begin = ext.getBegin();
							Date end = ext.getEnd();
							int ret = ContractUtil.checkDateInContracts(_conDAO.getForHiwi(hiwi), begin, end);
							
							switch(ret){
								case -1 :{
									// begin after end
									throw new RollbackException("-5");
								}
					
								case 0 :{
									
									switch(createContract(begin, end, hiwi, ext.getArea(), ext.getCostunit(), ext.getHours(), 0)){
										
										case -2:{
											// duplicated timesheet
											throw new RollbackException("-6");
										}
										default : {
											_extDAO.delete(ext);
											// succeed
											return 0;
										}
									}
								}
								default :{
									// overlapping 
									throw new RollbackException(String.valueOf(ret));
								}
							}
						}
						else{
							// wrong hiwi
							throw new RollbackException("-2");
						}
					}
				else{
					//delete orphan extension (without restriction)
					_extDAO.delete(ext);
					return 1;
				}
			}
			else{
				// wrong extension
				throw new RollbackException("-4");
			}
		}
		else{
			// wrong actor
			throw new RollbackException("-1");
		}
		}
		catch(HibernateException he){
			logger.error(he.getMessage());
			throw new RollbackException("-8");
		}
		
	}
	
	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {RuntimeException.class, RollbackException.class})
	public int changeContract(long actorID, long hiwiID, long cID, Date begin,
			Date end, int hours, int costunit, String area)
			throws RollbackException {
		try{
			Admin actor = _userDAO.getAdminByIDAndActive(actorID, true);
			
			if(actor != null){
				HiWi hiwi = _userDAO.getHiWiByID(hiwiID);
				
				if(hiwi != null){
					Contract con = getContractWithHiwiAndID(hiwi, cID);
					
					if(con != null){
						switch(ContractUtil.checkChangedContractDate(_conDAO.getForHiwi(hiwi), con, begin, end)){
						case -2:{
							// date is not changed
							switch(changeContract(con, hours, costunit, area)){
								case -1:{
									// timesheet error
									throw new RollbackException("-3");
								}
							}
							return 0;
						}
						case -1:{
							// begin after end
							return -5;
						}
						case 0:{
							// change contract
							switch(changeContract(con, begin, end, hours, costunit, area)){
							case -1:{
								// parse exeption
								return -6;
							}
							case -2:{
								// can not delete timesheet
								throw new RollbackException("-2");
							}
							case -3:{
								// timesheet error
								throw new RollbackException("-3");
							}
							}
							return 0;
						}
						default:{
							// overlapping
							return -4;
						}
						}
					}
					else{
						// wrong contract
						return -3;
					}
				}
				else{
					// wrong hiwi
					return -2;
				}
			}
			else{
				// wrong actor
				return -1;
			}
		}
		catch(HibernateException he){
			logger.error(he.getMessage());
			throw new RollbackException("-8");
		}
	}
	
	@Override
	@Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = {RuntimeException.class, RollbackException.class})
	public int splitContract(long actorID, long hiwiID, long cID, Date sBegin)
			throws RollbackException {
		try{
			Admin actor = _userDAO.getAdminByIDAndActive(actorID, true);
			
			if(actor != null){
				HiWi hiwi = _userDAO.getHiWiByID(hiwiID);
				
				if(hiwi != null){
					Contract con = getContractWithHiwiAndID(hiwi, cID);
					
					if(con != null){
						Date sEnd = ContractUtil.checkSplitContractDate(con, sBegin);
						if(sEnd != null){
							// split contract
							switch(splitContract(con, sEnd, sBegin)){
							case -1: {
								// timesheet error
								throw new RollbackException("-3");
							}
							}
							//succeed
							return 0;
						}
						else{
							// split date error
							return -4;
						}
					}
					else{
						// wrong contract
						return -3;
					}
				}
				else{
					// wrong hiwi
					return -2;
				}
			}
			else{
				// wrong actor
				return -1;
			}
		}
		catch(HibernateException he){
			logger.error(he.getMessage());
			throw new RollbackException("-8");
		}
	}
	
	@Override
	@Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = {RuntimeException.class, RollbackException.class})
	public void requestExtension(long actorID, long extID) throws RollbackException {
		try{
		// only hiwi can request extension
		HiWi actor = _userDAO.getHiWiByID(actorID);
		
		if(actor != null && actor.getActive()){
			Extension ext = _extDAO.getByID(extID);
			if(ext != null){
				HiWi hiwi = ext.getHiwi();
				
				if(hiwi != null 
						&& hiwi.getActive()
						&& actor.getId().longValue() == hiwi.getId().longValue()){
					if(ext.getState() == ExtensionState.FREE){
						ext.setState(ExtensionState.REQUESTED);
						_extDAO.update(ext);
						return;
					}
					else if(ext.getState() == ExtensionState.REQUESTED){
						return;
					}
					else{
						// extension in invalid state
						throw new RollbackException("-4");
					}
				}
				else{
					// no right
					throw new RollbackException("-3");
				}
			}
			else{
				// no extension
				throw new RollbackException("-2");
			}
		}
		else{
			// no actor
			throw new RollbackException("-1");
		}	
		}
		catch(HibernateException he){
			logger.error(he.getMessage());
			throw new RollbackException("-8");
		}
	}

	@Override
	@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = {
			RuntimeException.class, RollbackException.class})
	public void deleteRedudantExtensions(long actorID, long hiwiID) throws RollbackException {
		try{
		// only adviser use this function
		Adviser actor = _userDAO.getAdviserByID(actorID);
		
		if(actor != null && actor.getActive()){
			HiWi hiwi = _userDAO.getHiWiByID(hiwiID);
			//TODO hiwi.getAdviser check null
			if(hiwi != null 
				&& hiwi.getActive()
				&& actor.getId().longValue() == hiwi.getAdviser().getId().longValue()
			){
				
				List<Extension> exts = getExtensionsWithHiwi(hiwi);
				for(int i = 0; i < exts.size(); i++){
					Extension ext = exts.get(i);
					if(ext.getState() != ExtensionState.REQUESTED){
						_extDAO.delete(ext);
					}
				}
				// success
				return;
			}
			else{
				// no right
				throw new RollbackException("-3");
			}
		}
		else{
			// no actor
			throw new RollbackException("-1");
		}
		}
		catch(HibernateException he){
			logger.error(he.getMessage());
			throw new RollbackException("-8");
		}
	}
	
	@Override
	@Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = {
			RuntimeException.class, RollbackException.class})
	public void deleteExtension(long actorID, long extID)
			throws RollbackException {
		try{
		User actor = _userDAO.getByID(actorID);
		
		if(actor != null && actor.getActive()){
			Extension ext = _extDAO.getByID(extID);
			
			if(ext != null){
				if(!(actor instanceof Admin)){
					if(actor instanceof Adviser){
						HiWi hiwi = ext.getHiwi();
						// hiwi == null must be checked, because delete hiwi dose not
						// cascade extension
						if(hiwi != null){
							IUser adviser = hiwi.getAdviser();
							// adviser == null must be checked, because delete adviser dose not
							// cascade hiwi
							if(adviser == null 
								|| actor.getId().longValue() != adviser.getId().longValue()
								|| ext.getState() == ExtensionState.REQUESTED
							){
								// no right
								throw new RollbackException("-3");
							}
						}
					}
					else{
						// no right
						throw new RollbackException("-3");
					}
				}
				
				// deletion
				_extDAO.delete(ext);
				return;
			}
			else{
				// extension dose not exist
				throw new RollbackException("-2");
			}
		}
		else{
			// actor dose not exist
			throw new RollbackException("-1");
		}
		}
		catch(HibernateException he){
			logger.error(he.getMessage());
			throw new RollbackException("-8");
		}
	}
	
	@Override
	@Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = {
			RuntimeException.class, RollbackException.class })
	public int freeExtension(long actorID, Date bDate, Date eDate, int hours, int costunit, String area,
			long hiwiID, long conID) throws RollbackException {
		try{
		// Only adviser can free extension
		Adviser actor = _userDAO.getAdviserByID(actorID);
		
		if(actor != null && actor.getActive()){
			HiWi hiwi = _userDAO.getHiWiByID(hiwiID);
			
			if(hiwi != null && hiwi.getActive()){
				IUser adviser = hiwi.getAdviser();
				if(adviser != null &&
					actor.getId().longValue() == adviser.getId().longValue()
				){
					List<Extension> exts = getExtensionsWithHiwi(hiwi);
					
					if(exts == null || exts.size() == 0){
						List<Contract> contracts = _conDAO.getForHiwi(hiwi);
						Contract con = null;
						for(int i = 0; i < contracts.size(); i++){
							if(contracts.get(i).getId().longValue() == conID){
								con = contracts.get(i);
								break;
							}
						}
						
						if(con != null){
							// create extension
							int ret = ContractUtil.checkDateInContracts(contracts, bDate, eDate);
							switch (ret){
								case -1: {
									// begin after end
									throw new RollbackException("-4");
								}
								case 0: {
									Project project = proDAO.getByCostUnit(costunit);
									if(project != null){
										if(eDate.after(project.getEnd()))
											// end after project end
											throw new RollbackException("-7");
									}
									
									Extension ext = new Extension();
									ext.setBegin(bDate);
									ext.setEnd(eDate);
									ext.setAdviser((Adviser)hiwi.getAdviser());
									ext.setContract(con);
									ext.setHiwi(hiwi);
									ext.setHours(hours);
									ext.setState(ExtensionState.FREE);
									ext.setArea(area);
									ext.setCostunit(costunit);
											
									_extDAO.save(ext);
										logger.info("New extension. id: "+ext.getId());
									
									// succeed
									return 0;
								}
								default :{
									// overlapping
									throw new RollbackException(String.valueOf(ret));
								}
							}
						}
						else{
							// contract dose not exist
							throw new RollbackException("-6");
						}
					}
					else{
						// a extension exists already
						throw new RollbackException("-5");
					}
				}
				else{
					// no right
					throw new RollbackException("-3");
				}
			}
			else{
				// hiwi dose not exist or inactive
				throw new RollbackException("-2");
			}
		}
		else{
			// actor dose not exist
			throw new RollbackException("-1");
		}
		}
		catch(HibernateException he){
			logger.error(he.getMessage());
			throw new RollbackException("-8");
		}
	}
	
	@Override
	@Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = {
			RuntimeException.class, RollbackException.class })
	public int updateExtension(long actorID, long hiwiID, long extID, Date bDate, Date eDate,
			int hours, int costunit, String area)
			throws RollbackException {
		try{
		// Only adviser can update extension
		Adviser actor = _userDAO.getAdviserByID(actorID);
		
		if(actor != null && actor.getActive()){
			HiWi hiwi = _userDAO.getHiWiByID(hiwiID);
			
			if(hiwi != null && hiwi.getActive()){
				IUser adviser = hiwi.getAdviser();
				if(adviser != null &&
					actor.getId().longValue() == adviser.getId().longValue()
				){
					List<Extension> exts = getExtensionsWithHiwi(hiwi);
					
					if(exts != null && exts.size() == 1){
						Extension ext = exts.get(0);
						
						if(ext.getId().longValue() == extID && ext.getState() == ExtensionState.FREE){
							Contract con = ext.getContract();
							if(con != null){
								List<Contract> contracts = _conDAO.getForHiwi(hiwi);
								// update extension
								int ret = ContractUtil.checkDateInContracts(contracts, bDate, eDate);
								switch (ret){
									case -1: {
										// begin afte end
										throw new RollbackException("-4");
									}
									case 0 : {
										Project project = proDAO.getByCostUnit(costunit);
										if(project != null){
											if(eDate.after(project.getEnd()))
												// end after project end
												throw new RollbackException("-9");
										}
										
										ext.setBegin(bDate);
										ext.setEnd(eDate);
										ext.setHours(hours);
										ext.setCostunit(costunit);
										ext.setAdviser((Adviser)hiwi.getAdviser());
										ext.setArea(area);
										ext.setState(ExtensionState.FREE);
												
										_extDAO.update(ext);
											logger.info("Update extension. id: "+ext.getId());
										
										//succeed	
										return 0;
									}
									default :{
										// overlapping
										throw new RollbackException(String.valueOf(ret));
									}
								}
							}
							else{
								// contract dose not exist
								throw new RollbackException("-6");
							}
						}
						else{
							// wrong extension
							throw new RollbackException("-7");
						}
					}
					else{
						// many extensions or no extension
						throw new RollbackException("-5");
					}
				}
				else{
					// no right
					throw new RollbackException("-3");
				}
			}
			else{
				// hiwi dose not exist or inactive
				throw new RollbackException("-2");
			}
		}
		else{
			// actor dose not exist
			throw new RollbackException("-1");
		}
		}
		catch(HibernateException he){
			logger.error(he.getMessage());
			throw new RollbackException("-8");
		}
	}
	
	
	@Override
	@Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = {
			RuntimeException.class, RollbackException.class })
	public void freeExtensionWithoutCheck(long actorID, Date bDate, Date eDate,
			int hours, int costunit, String area, long hiwiID, long conID)
			throws RollbackException {
		try{
			// Only adviser can free extension
			Adviser actor = _userDAO.getAdviserByID(actorID);
			
			if(actor != null && actor.getActive()){
				HiWi hiwi = _userDAO.getHiWiByID(hiwiID);
				
				if(hiwi != null && hiwi.getActive()){
					IUser adviser = hiwi.getAdviser();
					if(adviser != null &&
						actor.getId().longValue() == adviser.getId().longValue()
					){
						List<Extension> exts = getExtensionsWithHiwi(hiwi);
						
						if(exts == null || exts.size() == 0){
							List<Contract> contracts = _conDAO.getForHiwi(hiwi);
							Contract con = null;
							for(int i = 0; i < contracts.size(); i++){
								if(contracts.get(i).getId().longValue() == conID){
									con = contracts.get(i);
									break;
								}
							}
							
							if(con != null){
								// create extension
								int ret = ContractUtil.checkDateInContracts(contracts, bDate, eDate);
								switch (ret){
									case -1: {
										// begin after end
										throw new RollbackException("-4");
									}
									case 0: {
										Extension ext = new Extension();
										ext.setBegin(bDate);
										ext.setEnd(eDate);
										ext.setAdviser((Adviser)hiwi.getAdviser());
										ext.setContract(con);
										ext.setHiwi(hiwi);
										ext.setHours(hours);
										ext.setState(ExtensionState.FREE);
										ext.setArea(area);
										ext.setCostunit(costunit);
												
										_extDAO.save(ext);
											logger.info("New extension. id: "+ext.getId());
										return;
									}
									default :{
										// overlapping
										throw new RollbackException(String.valueOf(ret));
									}
								}
							}
							else{
								// contract dose not exist
								throw new RollbackException("-6");
							}
						}
						else{
							// a extension exists already
							throw new RollbackException("-5");
						}
					}
					else{
						// no right
						throw new RollbackException("-3");
					}
				}
				else{
					// hiwi dose not exist or inactive
					throw new RollbackException("-2");
				}
			}
			else{
				// actor dose not exist
				throw new RollbackException("-1");
			}
			}
			catch(HibernateException he){
				logger.error(he.getMessage());
				throw new RollbackException("-8");
			}
	}

	@Override
	@Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = {
			RuntimeException.class, RollbackException.class })
	public void updateExtensionWithoutCheck(long actorID, long hiwiID,
			long extID, Date bDate, Date eDate, int hours, int costunit,
			String area) throws RollbackException {
		try{
			// Only adviser can update extension
			Adviser actor = _userDAO.getAdviserByID(actorID);
			
			if(actor != null && actor.getActive()){
				HiWi hiwi = _userDAO.getHiWiByID(hiwiID);
				
				if(hiwi != null && hiwi.getActive()){
					IUser adviser = hiwi.getAdviser();
					if(adviser != null &&
						actor.getId().longValue() == adviser.getId().longValue()
					){
						List<Extension> exts = getExtensionsWithHiwi(hiwi);
						
						if(exts != null && exts.size() == 1){
							Extension ext = exts.get(0);
							
							if(ext.getId().longValue() == extID && ext.getState() == ExtensionState.FREE){
								Contract con = ext.getContract();
								if(con != null){
									List<Contract> contracts = _conDAO.getForHiwi(hiwi);
									// update extension
									int ret = ContractUtil.checkDateInContracts(contracts, bDate, eDate);
									switch (ret){
										case -1: {
											// begin afte end
											throw new RollbackException("-4");
										}
										case 0 : {
											
											ext.setBegin(bDate);
											ext.setEnd(eDate);
											ext.setHours(hours);
											ext.setCostunit(costunit);
											ext.setAdviser((Adviser)hiwi.getAdviser());
											ext.setArea(area);
											ext.setState(ExtensionState.FREE);
													
											_extDAO.update(ext);
												logger.info("Update extension. id: "+ext.getId());
											return;
										}
										default :{
											// overlapping
											throw new RollbackException(String.valueOf(ret));
										}
									}
								}
								else{
									// contract dose not exist
									throw new RollbackException("-6");
								}
							}
							else{
								// wrong extension
								throw new RollbackException("-7");
							}
						}
						else{
							// many extensions or no extension
							throw new RollbackException("-5");
						}
					}
					else{
						// no right
						throw new RollbackException("-3");
					}
				}
				else{
					// hiwi dose not exist or inactive
					throw new RollbackException("-2");
				}
			}
			else{
				// actor dose not exist
				throw new RollbackException("-1");
			}
			}
			catch(HibernateException he){
				logger.error(he.getMessage());
				throw new RollbackException("-8");
			}
	}
	
	
	@Override
	@Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = {
			RuntimeException.class, RollbackException.class })
	public void createNewContract(long actorID, long hiwiID, Date bDate, Date eDate, double carry,
			int hours, int costunit, String area)
			throws RollbackException {
		try{
		// only admin use this function
		Admin actor = _userDAO.getAdminByID(actorID);
		
		if(actor != null && actor.getActive()){
			HiWi hiwi = _userDAO.getHiWiByID(hiwiID);
			
			if(hiwi != null && hiwi.getActive()){
				
				int ret = ContractUtil.checkDateInContracts(_conDAO.getForHiwi(hiwi), bDate, eDate);
				switch (ret){
					case -1: {
						// begin after end
						throw new RollbackException("-3");
					}
					case 0 : {
						// create new contract
						NewContract nc = new NewContract();
						nc.setHiwi(hiwi);
						nc.setBegin(bDate);
						nc.setEnd(eDate);
						nc.setCarry(carry);
						nc.setCostunit(costunit);
						nc.setHours(hours);
						nc.setArea(area);
						nc.setState(NewContractState.WAITING);
						
						_ncDAO.save(nc);
						
							logger.debug("New new contract. id: "+nc.getId());
						return;
					}
					default :{
						// overlapping
						throw new RollbackException(String.valueOf(ret));
					}
				}
			}
			else{
				// no hiwi
				throw new RollbackException("-2");
			}
		}
		else{
			// actor dose not exist
			throw new RollbackException("-1");
		}
		}
		catch(HibernateException he){
			logger.error(he.getMessage());
			throw new RollbackException("-8");
		}
	}
	
	private Contract getContractWithHiwiAndID(HiWi hiwi, long id){
		Criterion [] crits = new Criterion[2];
		crits[0] = Restrictions.eq("hiwi", hiwi);
		crits[1] = Restrictions.eq("id", id);
		return _conDAO.getUniqueResult(crits);
	}
	
	private Extension getExtensionWithContractAndId(Contract con, long id) {
		Criterion [] crits = new Criterion[2];
		crits[0] = Restrictions.eq("contract", con);
		crits[1] = Restrictions.eq("id", id);
		return _extDAO.getUniqueResult(crits);
	}

	private List<Extension> getExtensionsWithHiwi(HiWi hiwi) {
		return _extDAO.getList(Restrictions.eq("hiwi", hiwi));
	}
	
	private NewContract getNewContractWithHiwiAndId(HiWi hiwi, long id) {
		Criterion [] crits = new Criterion[2];
		crits[0] = Restrictions.eq("hiwi", hiwi);
		crits[1] = Restrictions.eq("id", id);
		return _ncDAO.getUnique(crits);
	}

}

