package com.artezio.arttime.services;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.exceptions.SaveApprovedHoursException;
import com.artezio.arttime.services.repositories.HoursRepository;

@RunWith(EasyMockRunner.class)
public class HoursServiceTest {
	@Mock
	private HoursRepository hoursRepository;
	private HoursService hoursService;
	private static EntityManagerFactory entityManagerFactory;
	 
	 static {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("javax.persistence.validation.mode", "none");	
		entityManagerFactory = Persistence.createEntityManagerFactory("test", properties);
	}
	 
	 @Test
	 public void testSaveManagedHours_ifHoursNotPersisted() throws Exception {
		 hoursService = new HoursService();
		 setField(hoursService, "hoursRepository", hoursRepository);
		 Employee employee = new Employee();
		 Project project = new Project();
		 HourType hourType = new HourType();
		 Date date = new Date();
		 Hours hours = new Hours(project, date, employee, hourType);
		 hoursRepository.lock(employee);
		 expect(hoursRepository.findHours(date, employee, project, hourType)).andReturn(null);
		 expect(hoursRepository.create(hours)).andReturn(hours);
		 replay(hoursRepository);
		 
		 hoursService.saveManagedHours(Arrays.asList(hours));
		 
		 verify(hoursRepository);
	 }
	 
	 @Test
	 public void testSaveManagedHours_ifHoursPersisted() throws Exception {
		 hoursService = new HoursService();
		 setField(hoursService, "hoursRepository", hoursRepository);
		 Employee employee = new Employee();
		 Project project = new Project();
		 HourType hourType = new HourType();
		 Date date = new Date();
		 Hours hours = new Hours(project, date, employee, hourType);
		 hoursRepository.lock(employee);
		 expect(hoursRepository.findHours(date, employee, project, hourType)).andReturn(hours);
		 expect(hoursRepository.update(hours)).andReturn(hours);
		 replay(hoursRepository);
		 
		 hoursService.saveManagedHours(Arrays.asList(hours));
		 
		 verify(hoursRepository);
	 }
	 
	 @Test
	 public void testSaveTimesheet_ifHoursNotPersisted() throws Exception {
		 hoursService = new HoursService();
		 setField(hoursService, "hoursRepository", hoursRepository);
		 Employee employee = new Employee();
		 Project project = new Project();
		 HourType hourType = new HourType();
		 Date date = new Date();
		 Hours hours = new Hours(project, date, employee, hourType);
		 hoursRepository.lock(employee);
		 expect(hoursRepository.findHours(date, employee, project, hourType)).andReturn(null);
		 expect(hoursRepository.create(hours)).andReturn(hours);
		 replay(hoursRepository);
		 
		 hoursService.saveReportTime(Arrays.asList(hours));
		 
		 verify(hoursRepository);
	 }
	 
	 @Test
	 public void testSaveTimesheet_ifHoursPersisted() throws Exception {
		 hoursService = new HoursService();
		 setField(hoursService, "hoursRepository", hoursRepository);
		 Employee employee = new Employee();
		 Project project = new Project();
		 HourType hourType = new HourType();
		 Date date = new Date();
		 Hours hours = new Hours(project, date, employee, hourType);
		 hoursRepository.lock(employee);
		 expect(hoursRepository.findHours(date, employee, project, hourType)).andReturn(hours);
		 expect(hoursRepository.update(hours)).andReturn(hours);
		 replay(hoursRepository);
		 
		 hoursService.saveReportTime(Arrays.asList(hours));
		 
		 verify(hoursRepository);
	 }
	 
	 @Test(expected = SaveApprovedHoursException.class)
	 public void testSaveTimesheet_ifHoursPersistedAndApproved() throws Exception {
		 hoursService = new HoursService();
		 setField(hoursService, "hoursRepository", hoursRepository);
		 Employee employee = new Employee();
		 Project project = new Project();
		 HourType hourType = new HourType();
		 Date date = new Date();
		 Hours hours = new Hours(project, date, employee, hourType);
		 hours.setApproved(true);
		 hoursRepository.lock(employee);
		 expect(hoursRepository.findHours(date, employee, project, hourType)).andReturn(hours);
		 expect(hoursRepository.update(hours)).andReturn(hours);
		 replay(hoursRepository);
		 
		 hoursService.saveReportTime(Arrays.asList(hours));
		 
		 verify(hoursRepository);
	 }
	 
	 @Test
	 public void testSaveManagedHours() throws Exception {
		 Employee employee1 = new Employee("employee1");
		 Employee employee2 = new Employee("employee2");
		 Project project1 = new Project();
		 Project project2 = new Project();
		 HourType hourType = new HourType();
		 Date date = new Date();
		 EntityManager entityManager = entityManagerFactory.createEntityManager();
		 entityManager.getTransaction().begin();
		 entityManager.persist(project1);
		 entityManager.persist(project2);
		 entityManager.persist(employee1);
		 entityManager.persist(employee2);
		 entityManager.persist(hourType);
		 entityManager.flush();
		 entityManager.clear();
		 entityManager.getTransaction().commit();
		 Hours hours11 = new Hours(project1, date, employee1, hourType);
		 Hours hours12 = new Hours(project1, date, employee2, hourType);
		 Hours hours21 = new Hours(project2, date, employee2, hourType);
		 Hours hours22 = new Hours(project2, date, employee1, hourType);
		 		 
		 List<Hours> collection1 = Arrays.asList(hours11, hours12);
		 List<Hours> collection2 = Arrays.asList(hours21, hours22);
		 
		 SaveManagedHoursImitator imitator1 = new SaveManagedHoursImitator(collection1);
		 SaveManagedHoursImitator imitator2 = new SaveManagedHoursImitator(collection2);
		 
		 new Thread(imitator1).start();
		 new Thread(imitator2).start();
		 
		 Thread.sleep(1000);		 
		 List<Hours> hours = entityManager
				 .createQuery("SELECT h FROM Hours h", Hours.class)
				 .getResultList();
		 System.out.println(hours.size());
		 entityManagerFactory.close();
	 }	
	
	class SaveManagedHoursImitator implements Runnable {
		private HoursService hoursService;
		private EntityManager entityManager;		
		private List<Hours> hoursCollection;				
		
		public SaveManagedHoursImitator(List<Hours> hoursCollection) throws Exception {
			this.hoursCollection = hoursCollection;
			initService();
		}				

		@Override
		public void run() {
			try {
				entityManager.getTransaction().begin();
				saveManagedHours();
				entityManager.getTransaction().commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void saveManagedHours() throws ReflectiveOperationException {
			hoursService.saveManagedHours(hoursCollection);
		}		
		
		private void initService() throws Exception {
			hoursService = new HoursService();
			hoursRepository = new HoursRepository();
			entityManager = entityManagerFactory.createEntityManager();
			setField(hoursService, "hoursRepository", hoursRepository);		 			
			setField(hoursRepository, "entityManager", entityManager);
			
		}				
	}
}

