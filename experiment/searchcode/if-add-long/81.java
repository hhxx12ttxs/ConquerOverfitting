/*
 * This file is part of Taurus
 *
 * Taurus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * Taurus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Melenti.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.taurus.web.gwt.server;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.taurus.dao.generic.excepciones.LocationGeographyNotFoundException;
import org.taurus.dao.generic.excepciones.RiskNotFoundException;
import org.taurus.domain.AdminReportData;
import org.taurus.domain.AdministrationMileStone;
import org.taurus.domain.AdministrationMileStoneMonth;
import org.taurus.domain.Beneficiary;
import org.taurus.domain.BudgetReportData;
import org.taurus.domain.Employee;
import org.taurus.domain.LinePlanning;
import org.taurus.domain.Location;
import org.taurus.domain.LocationGeography;
import org.taurus.domain.MileStone;
import org.taurus.domain.MileStoneMonth;
import org.taurus.domain.Mitigation;
import org.taurus.domain.Project;
import org.taurus.domain.ProjectHistory;
import org.taurus.domain.Risk;
import org.taurus.domain.UnitOrganizational;
import org.taurus.domain.UnitTime;
import org.taurus.domain.User;
import org.taurus.domain.base.ModelBasic;
import org.taurus.domain.composite.ReportParameter;
import org.taurus.enumerations.Month;
import org.taurus.enumerations.MonthNotFoundException;
import org.taurus.enumerations.ProjectStatusEnum;
import org.taurus.enumerations.SumaryDescription;
import org.taurus.exceptions.MileStoneNotFoundException;
import org.taurus.exceptions.NoFoundProjectException;
import org.taurus.exceptions.NoFoundUnitOrganizationalException;
import org.taurus.exceptions.NotFoundLocationException;
import org.taurus.exceptions.NotFoundObjectException;
import org.taurus.exceptions.ServiceException;
import org.taurus.util.StringUtil;
import org.taurus.web.gwt.client.ServiceWebAdministration;
import org.taurus.web.gwt.client.exceptions.GWTException;
import org.taurus.web.gwt.client.exceptions.GWTNoFoundException;
import org.taurus.web.gwt.client.vo.LocationGeographicVO;
import org.taurus.web.gwt.client.vo.MilestoneMonthVO;
import org.taurus.web.gwt.client.vo.MilestoneVO;
import org.taurus.web.gwt.client.vo.MitigacionVO;
import org.taurus.web.gwt.client.vo.RiskVO;
import org.taurus.web.gwt.composite.LocationComposite;
import org.taurus.web.gwt.composite.ProjectComposite;
import org.taurus.web.gwt.composite.SimpleComposite;
import org.taurus.web.gwt.composite.UnitOrganizationalComposite;
import org.taurus.web.gwt.server.composite.HitoComposite;
import org.taurus.web.gwt.server.composite.NumericalHitoComposite;
import org.taurus.web.gwt.server.composite.ProjectStatusComposite;
import org.taurus.web.gwt.server.composite.SumaryComposite;
import org.taurus.web.gwt.server.report.ConfiguradorReporte;
import org.taurus.web.gwt.server.reports.AdminitrationMilestoneReportConfig;
import org.taurus.web.gwt.server.reports.AnualAdminReportConfig;
import org.taurus.web.gwt.server.reports.AnualBudgetReportConfig;
import org.taurus.web.gwt.server.reports.BudgetMilestoneReportConfig;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Incluir aquí la descripcion de la clase.
 * 
 * @version $Revision: 1.0 $ [17/07/2008]
 * @author henry molina <a href="mailto:henrymolinanoboa@gmail.com">taurus</a> *
 * @web.servlet name="ServiceWebAdministrationImpl"
 * @web.servlet-mapping url-pattern="/ServiceWebAdministrationImpl"
 */
@RemoteServiceRelativePath("ServiceWebAdministrationImpl")
public class ServiceWebAdministrationImpl extends BaseServiceRemote implements ServiceWebAdministration {

	private static Logger log = Logger.getLogger(ServiceWebAdministrationImpl.class);

	private static final long serialVersionUID = -5852744954696622631L;

	/**
	 * 
	 */

	public ServiceWebAdministrationImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#buildTreeLocation()
	 */
	public String beforeMilestone(String currentYear, List<MilestoneVO> milestoneVOList, String prefijo)
			throws GWTException {

		this.storeMilestoneSession(milestoneVOList, currentYear, prefijo);

		int beforeYear = Integer.parseInt(currentYear) - 1;

		String[] meses = new String[] { "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto",
				"septiembre", "octubre", "noviembre", "diciembre" };

		return this.getListMilestone(String.valueOf(beforeYear), meses, prefijo);

	}

	/*
	 * (non-Javadoc) @seeorg.taurus.web.gwt.client.ServiceWebAdministration# findAllUnitOrganizationalActivate()
	 */
	private void borrarAnios() {
		for (int i = 1000; i < 3000; i++) {
			this.removeAttributeSessionHttp("hitos" + i);
			this.removeAttributeSessionHttp("hitoAdm" + i);
		}
	}

	@SuppressWarnings("unchecked")
	public List<LocationComposite> buildTreeLocation() throws GWTNoFoundException, GWTException {
		if (log.isDebugEnabled()) {
			log.debug(".");
		}
		try {
			List<LocationComposite> list = null;
			this.setAttributeSessionHttp("LOCATION_LIST", null);
			if (this.getAttributeSessionHttp("LOCATION_LIST") == null) {
				list = new ArrayList<LocationComposite>();
				List<Location> locations = serviceAdministrator.findAllLocation();
				for (Location lo : locations) {
					if (lo.getLocationParent() == null) {
						this.fillTreeLocation(lo, list);
					}
				}
				this.setAttributeSessionHttp("LOCATION_LIST", list);
			} else {
				list = (List<LocationComposite>) this.getAttributeSessionHttp("LOCATION_LIST");
			}
			return list;
		} catch (NotFoundLocationException e) {
			throw new GWTNoFoundException(StringUtil.createChain("fail:", e.getMessage()), e);
		} catch (ServiceException e) {
			throw new GWTException(StringUtil.createChain("fail general:", e.getMessage()), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#findProject(java.lang .String)
	 */

	@SuppressWarnings("unchecked")
	public List<UnitOrganizationalComposite> buildTreeUnitOrganizational() throws GWTNoFoundException, GWTException {
		if (log.isDebugEnabled()) {
			log.debug(".");
		}
		try {
			List<UnitOrganizationalComposite> list = null;
			this.setAttributeSessionHttp("UNIT_ORGANIZATIONAL_LIST", null);
			if (this.getAttributeSessionHttp("UNIT_ORGANIZATIONAL_LIST") == null) {
				list = new ArrayList<UnitOrganizationalComposite>();
				List<UnitOrganizational> units = serviceAdministrator.findAllRootUnitOrganizations();
				for (UnitOrganizational uo : units) {
					if (uo.getUnitOrganizationalParent() == null) {
						this.fillTreeUnitOrganizational(uo, list);
					}
				}
				this.setAttributeSessionHttp("UNIT_ORGANIZATIONAL_LIST", list);
			} else {
				list = (List<UnitOrganizationalComposite>) this.getAttributeSessionHttp("UNIT_ORGANIZATIONAL_LIST");
			}
			return list;
		} catch (NoFoundUnitOrganizationalException e) {
			throw new GWTNoFoundException(StringUtil.createChain("fail:", e.getMessage()), e);
		} catch (ServiceException e) {
			throw new GWTException(StringUtil.createChain("fail general:", e.getMessage()), e);
		}
	}

	/**
	 * Incluir aquí la descripcion del metodo.
	 * 
	 * @param lo
	 * @param list
	 */

	private void fillTreeLocation(Location lo, List<LocationComposite> list) {
		if (lo != null) {
			LocationComposite loc = new LocationComposite();
			loc.setId(lo.getId().toString());
			loc.setName(lo.getName());
			list.add(loc);
			if (!lo.getLocations().isEmpty()) {
				loc.setLocations(new ArrayList<LocationComposite>());
				for (Location l : lo.getLocations()) {
					this.fillTreeLocation(l, loc.getLocations());
				}
			}
		}
	}

	/*
	 * (non-Javadoc) @seeorg.taurus.web.gwt.client.ServiceWebAdministration#
	 * findProjectByUnitOrganization(java.lang.String)
	 */

	/**
	 * 
	 * 
	 * @param uo
	 * @param list
	 */

	private int fillTreeUnitOrganizational(UnitOrganizational uo, List<UnitOrganizationalComposite> list) {
		int numeroProyectos = 0;
		if (uo != null) {
			UnitOrganizationalComposite uoc = new UnitOrganizationalComposite();
			uoc.setId(uo.getId().toString());
			uoc.setName(uo.getName());
			uoc.setResponsibleName(uo.getResponsibleName());
			uoc.setDescription(uo.getDescription());
			uoc.setPath(uo.getPathUnitOrganization());
			if (uo.getUnitOrganizationalParent() != null) {
				UnitOrganizationalComposite uocParent = new UnitOrganizationalComposite();
				uocParent.setId(uo.getUnitOrganizationalParent().getId().toString());
				uocParent.setName(uo.getUnitOrganizationalParent().getName());
				uocParent.setResponsibleName(uo.getUnitOrganizationalParent().getResponsibleName());
				uocParent.setDescription(uo.getUnitOrganizationalParent().getDescription());
				uoc.setParent(uocParent);
			}
			list.add(uoc);
			if (!uo.getUnits().isEmpty()) {
				uoc.setUnits(new ArrayList<UnitOrganizationalComposite>());
				for (UnitOrganizational u : uo.getUnits()) {
					numeroProyectos += this.fillTreeUnitOrganizational(u, uoc.getUnits());
				}
				uoc.setNumeroProyectos(numeroProyectos + "");

			} else {
				// registrar los proyectos asignados a la UO
				List<ProjectComposite> projects = new ArrayList<ProjectComposite>();
				for (Project p : uo.getProjects()) {
					projects.add(new ProjectComposite(p.getId().toString(), p.getName(), p.getProjectPath()));
					numeroProyectos++;
				}
				uoc.setProjects(projects);
				if (projects.size() > 0) {
					uoc.setHasProject(true);
				}
				uoc.setNumeroProyectos(numeroProyectos + "");

			}
		}
		return numeroProyectos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#findAllActiveEmployee ()
	 */
	public String findAllActiveEmployee() throws GWTException {
		List<Employee> employees;
		try {
			employees = serviceProject.findAllActiveEmployee();
			return this.toJson2(employees);
		} catch (ServiceException e) {
			e.printStackTrace();
			throw new GWTException(e.getMessage());
		} catch (NotFoundObjectException e) {
			e.printStackTrace();
			throw new GWTException(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new GWTException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#findAllActiveLinePlanning ()
	 */
	public String findAllActiveLinePlanning() throws GWTException {

		try {
			List<LinePlanning> linePlanning = serviceProject.findAllActiveLinePlanning();
			return this.toJson2(linePlanning);
		} catch (ServiceException e) {
			e.printStackTrace();
			throw new GWTException();
		} catch (NotFoundObjectException e) {
			e.printStackTrace();
			throw new GWTException();
		} catch (Exception e) {
			e.printStackTrace();
			throw new GWTException();
		}
	}

	public String findAllBeneficiaries() throws GWTException {
		try {
			List<Beneficiary> allBeneficiaries = serviceAdministrator.findAllBeneficiary();
			return this.toJson2(allBeneficiaries);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e.getMessage(), e);
		}
	}

	/**
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#findAllLocationGeographyByProject(java.lang.Long)
	 */
	public String findAllLocationGeographyByProject(Long projectId) throws GWTException {
		List<LocationGeography> locationList = null;
		String json = null;
		try {
			locationList = serviceAdministrator.findAllLocationGeographyByProject(projectId);
			json = this.toJson2(locationList);
		} catch (ServiceException e) {
			log.error(e, e);
			throw new GWTException(e);
		} catch (LocationGeographyNotFoundException e) {
			log.info(e);
			locationList = new ArrayList<LocationGeography>();
			json = this.toJson2(locationList);
		}

		return json;
	}

	public String findAllMilestoneMonthByProject(Long projectId) throws GWTException {
		String json = "[]";
		try {
			List<MileStoneMonth> allMileStoneMonth = serviceAdministrator.findAllMilestoneMonthByProject(projectId);
			List<MilestoneVO> resultados = this.getFromMilestoneMonthList(allMileStoneMonth);

			List<SumaryComposite> sumaryCompositeList = this.toSumaryComposite(resultados);
			json = this.toJson2(sumaryCompositeList);

		} catch (ServiceException e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e);
		} catch (MonthNotFoundException e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e);
		}
		return json;
	}

	/**
	 * 
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#findAllProjectsInTree(org.taurus.web.gwt.composite.UnitOrganizationalComposite)
	 */
	public String findAllProjectsInTree(UnitOrganizationalComposite actualUnitOrganization) throws GWTException {
		String json = "[]";
		try {
			List<Integer> meses = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
			List<Long> organizationsIds = new ArrayList<Long>();
			this.recolectAllUnitOrganizationIds(actualUnitOrganization, organizationsIds);
			List<MileStone> allMileStone = serviceAdministrator.findAllMileStoneByUnitOrganization(organizationsIds);
			List<MilestoneVO> resultados = new ArrayList<MilestoneVO>();
			for (Integer mes : meses) {
				String nombreMes = Month.getMonthName(mes);
				BigDecimal planificado = BigDecimal.ZERO;
				BigDecimal ejecutado = BigDecimal.ZERO;
				BigDecimal devengado = BigDecimal.ZERO;
				for (MileStone m : allMileStone) {
					for (MileStoneMonth mm : m.getMileStoneMonthList()) {
						if (mm.getMonthNumber().equals(mes)) {
							planificado = planificado.add(mm.getPlanedValue());
							ejecutado = ejecutado.add(mm.getExcecutedValue());
							devengado = devengado.add(mm.getEarnedValue());
						}
					}
				}
				MilestoneVO planificadoVo = new MilestoneVO();
				planificadoVo.setAnio("1");
				planificadoVo.setDescripcion(SumaryDescription.COMPROMETIDO.getValue());
				planificadoVo.setEjecutado(planificado.toString());
				planificadoVo.setPlaneado(planificado.toString());
				planificadoVo.setDevengado(planificado.toString());
				planificadoVo.setMes(nombreMes);

				MilestoneVO ejecutadoVO = new MilestoneVO();
				ejecutadoVO.setAnio("1");
				ejecutadoVO.setDescripcion(SumaryDescription.EJECUTADO.getValue());
				ejecutadoVO.setEjecutado(ejecutado.toString());
				ejecutadoVO.setPlaneado(ejecutado.toString());
				ejecutadoVO.setDevengado(ejecutado.toString());
				ejecutadoVO.setMes(nombreMes);

				MilestoneVO devengadoVO = new MilestoneVO();
				devengadoVO.setAnio("1");
				devengadoVO.setDescripcion(SumaryDescription.DEVENGADO.getValue());
				devengadoVO.setEjecutado(devengado.toString());
				devengadoVO.setPlaneado(devengado.toString());
				devengadoVO.setDevengado(devengado.toString());
				devengadoVO.setMes(nombreMes);

				resultados.add(planificadoVo);
				resultados.add(ejecutadoVO);
				resultados.add(devengadoVO);
			}

			List<SumaryComposite> sumaryCompositeList = this.toSumaryComposite(resultados);
			json = this.toJson2(sumaryCompositeList);

		} catch (MileStoneNotFoundException e) {
			log.info("No hay hitos");
		} catch (ServiceException e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e);
		} catch (MonthNotFoundException e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e);
		}
		return json;
	}

	public String findAllProjectsInTreeByYear(UnitOrganizationalComposite actualUnitOrganization, Integer year)
			throws GWTException {
		String json = "[]";
		try {
			List<Long> organizationsIds = new ArrayList<Long>();
			this.recolectAllUnitOrganizationIds(actualUnitOrganization, organizationsIds);
			List<MileStoneMonth> allMileStoneMonth = serviceAdministrator.findMileStoneMontsByOrganizationAndYear(
					organizationsIds, year);
			List<MilestoneVO> resultados = this.getFromMilestoneMonthList(allMileStoneMonth);

			List<SumaryComposite> sumaryCompositeList = this.toSumaryComposite(resultados);
			json = this.toJson2(sumaryCompositeList);

		} catch (ServiceException e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e);
		} catch (MonthNotFoundException e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e);
		}
		return json;
	}

	/**
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#findAllRiksByProject(java.lang.Long)
	 */
	public String findAllRiksByProject(Long projectId) throws GWTException {
		List<Risk> risks = null;
		String json = null;
		try {
			risks = serviceAdministrator.findAllRiksByProject(projectId);
			json = this.toJson2(risks);
		} catch (ServiceException e) {
			log.error(e, e);
			throw new GWTException(e);
		} catch (RiskNotFoundException e) {
			log.info(e);
			risks = new ArrayList<Risk>();
			json = this.toJson2(risks);
		}
		return json;
	}

	@SuppressWarnings("unchecked")
	public String findAllUnitTime() throws GWTException {
		try {
			List<UnitTime> allUnitTimes;
			if (this.getAttributeContextHttp("UNIT_TIME_LIST") == null) {
				allUnitTimes = serviceAdministrator.findAllUnitTime();
				this.setAttributeContextHttp("UNIT_TIME_LIST", allUnitTimes);
			} else {
				allUnitTimes = (List<UnitTime>) this.getAttributeContextHttp("UNIT_TIME_LIST");
			}

			return this.toJson2(allUnitTimes);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#findProjectHistory (java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	public String findProject(String id) throws GWTNoFoundException, GWTException {
		if (log.isDebugEnabled()) {
			log.debug("retrive project id=" + id);
		}
		try {

			Set<String> aniosGuardar = (Set<String>) this.getAttributeSessionHttp("aniosGuardarhitos");

			Set<String> aniosGuardarAdm = (Set<String>) this.getAttributeSessionHttp("aniosGuardarhitoAdm");

			Project project = serviceAdministrator.findByIdProject(Long.valueOf(id));

			// Colocamos en sesión los hitos que tenga el proyecto
			// Determinamos los ańos en los que hay informacion
			List<Integer> anios = serviceAdministrator.findYearsInBudgetMileStonesByProject(Long.valueOf(id));

			for (Integer year : anios) {
				// Elimino las variables de session anteriores
				this.removeAttributeSessionHttp("hitos" + year);
				ArrayList<MilestoneVO> listaSesion = new ArrayList<MilestoneVO>();
				for (MileStone m : project.getMileStones()) {
					int i = 0;
					List<MilestoneMonthVO> mmvos = new ArrayList<MilestoneMonthVO>();
					MilestoneVO mvo = new MilestoneVO();
					for (MileStoneMonth mm : m.getMileStoneMonthList()) {
						if (mm.getYear().equals(year)) {
							if (i++ == 0) {
								mvo.setDescripcion(m.getDescription());
							}
							MilestoneMonthVO mmvo = new MilestoneMonthVO();
							mmvo.setAnio(mm.getYear().toString());
							mmvo.setMes(mm.getMonthName());
							mmvo.setDevengado(mm.getEarnedValue() != null ? mm.getEarnedValue().toString() : "");
							mmvo.setEjecutado(mm.getExcecutedValue() != null ? mm.getExcecutedValue().toString() : "");
							mmvo.setPlaneado(mm.getPlanedValue() != null ? mm.getPlanedValue().toString() : "");
							mmvos.add(mmvo);

						}
					}
					if (!mmvos.isEmpty()) {
						mvo.setMonthsMileStoneVO(mmvos);
						listaSesion.add(mvo);
					}
				}
				this.setAttributeSessionHttp("hitos" + year, listaSesion);

				if (aniosGuardar != null) {
					aniosGuardar.add(year.toString());
				}
			}

			anios = serviceAdministrator.findYearsInAdministrationMileStoneByProject(Long.valueOf(id));
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

			for (Integer year : anios) {
				// Elimino las variables de session anteriores
				this.removeAttributeSessionHttp("hitoAdm" + year);
				ArrayList<MilestoneVO> listaSesion = new ArrayList<MilestoneVO>();
				for (AdministrationMileStone m : project.getAdministrationMileStoneList()) {
					int i = 0;
					List<MilestoneMonthVO> mmvos = new ArrayList<MilestoneMonthVO>();
					MilestoneVO mvo = new MilestoneVO();
					for (AdministrationMileStoneMonth aammmm : m.getAdministrationMilestoneList()) {
						if (aammmm.getMilestoneYear().equals(year)) {
							if (i++ == 0) {
								mvo.setDescripcion(m.getMileStoneDescription());
							}
							MilestoneMonthVO mmvo = new MilestoneMonthVO();
							mmvo.setAnio(aammmm.getMilestoneYear().toString());
							mmvo.setEjecutado(aammmm.getExcecutedDate() != null ? sdf.format(aammmm.getExcecutedDate())
									: "");
							mmvo.setMes(aammmm.getMonthName());
							mmvo.setPlaneado(aammmm.getPlanedDate() != null ? sdf.format(aammmm.getPlanedDate()) : "");
							mmvos.add(mmvo);
						}
					}
					if (!mmvos.isEmpty()) {
						mvo.setMonthsMileStoneVO(mmvos);
						listaSesion.add(mvo);
					}
				}

				if (aniosGuardarAdm != null) {
					aniosGuardarAdm.add(year.toString());
				}
				this.setAttributeSessionHttp("hitoAdm" + year, listaSesion);
			}

			return this.toJson2(project);
		} catch (NoFoundProjectException e) {
			throw new GWTNoFoundException(e.getMessage(), e);
		} catch (Exception e) {
			throw new GWTException(e.getMessage(), e);
		}
	}

	public String findProjectByUnitOrganization(String unitOrganizationId) throws GWTException {
		if (log.isDebugEnabled()) {
			log.debug("unitOrganizationId=" + unitOrganizationId);
		}
		try {
			User user = (User) this.getAttributeSessionHttp(User.class);
			List<Project> projects = serviceAdministrator.findProjectsByUnitOrganization(Long
					.valueOf(unitOrganizationId), user.getId());
			for (Project p : projects) {
				if (p.getUnitOrganizationalResponsible() == null) {
					p.setUnitOrganizationalResponsible(new UnitOrganizational());
				}
			}
			return this.toJson2(projects);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e.getMessage(), e);

		}
	}

	public String findProjectHistory(Long projectId) throws GWTException {
		try {
			Project p = serviceAdministrator.findByIdProject(projectId);
			List<ProjectHistory> histories = new ArrayList<ProjectHistory>(p.getProjectHistories());
			Collections.sort(histories, new Comparator<ProjectHistory>() {

				public int compare(ProjectHistory o1, ProjectHistory o2) {
					return o1.getId().compareTo(o2.getId());
				}
			});
			return this.toJson2(histories);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e.getMessage(), e);
		}
	}

	/**
	 * Se encarga de generar los datos necesarios para el reporte de datos administrativos
	 * 
	 * @param parameter
	 * @return
	 * @throws GWTException
	 */
	private List<HitoComposite> getAdminMilestoneDataReport(ReportParameter parameter) throws GWTException {
		try {
			List<AdministrationMileStone> administrationMilestoneList = serviceAdministrator
					.findAllAdministrationMilestoneByReportParameter(parameter);
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			ArrayList<MilestoneVO> listaMilestoneVO = new ArrayList<MilestoneVO>();
			for (AdministrationMileStone m : administrationMilestoneList) {
				int i = 0;
				List<MilestoneMonthVO> mmvos = new ArrayList<MilestoneMonthVO>();
				MilestoneVO mvo = new MilestoneVO();
				for (AdministrationMileStoneMonth aammmm : m.getAdministrationMilestoneList()) {
					if (i++ == 0) {
						mvo.setDescripcion(m.getMileStoneDescription());
						mvo.setProjectId(m.getProject().getId());
						mvo.setProjectName(m.getProject().getName());
						mvo.setUnitOrganizationId(m.getProject().getUnitOrganizational().getId());
						mvo.setUnitOrganizationName(m.getProject().getUnitOrganizational().getName());
					}
					MilestoneMonthVO mmvo = new MilestoneMonthVO();
					mmvo.setAnio(aammmm.getMilestoneYear().toString());
					mmvo.setEjecutado(aammmm.getExcecutedDate() != null ? sdf.format(aammmm.getExcecutedDate()) : "");
					mmvo.setMes(aammmm.getMonthName());
					mmvo.setPlaneado(aammmm.getPlanedDate() != null ? sdf.format(aammmm.getPlanedDate()) : "");
					mmvos.add(mmvo);
				}
				mvo.setMonthsMileStoneVO(mmvos);
				listaMilestoneVO.add(mvo);
			}
			return this.pasarComposite(listaMilestoneVO);
		} catch (ServiceException e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e.getMessage(), e);
		}
	}

	public String[][] getAnios() throws GWTException {
		try {
			List<Integer> anios = serviceAdministrator.getYearsInMilestones();
			String[][] result = new String[anios.size() + 1][2];
			result[0] = new String[] { "TODOS", "TODOS" };
			for (int i = 0; i < anios.size(); i++) {
				String[] strings = new String[2];
				Integer anio = anios.get(i);
				strings[0] = anio.toString();
				strings[1] = anio.toString();
				result[i + 1] = strings;
			}
			return result;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e.getMessage(), e);
		}
	}

	/**
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#getAvilableProjectStatus()
	 */
	public String getAvilableProjectStatus() throws GWTException {
		List<ProjectStatusComposite> listaEstados = new ArrayList<ProjectStatusComposite>();
		for (ProjectStatusEnum pee : ProjectStatusEnum.values()) {
			listaEstados.add(new ProjectStatusComposite(pee.getStatus(), pee.getDescription()));
		}
		return this.toJson2(listaEstados);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#getLogedUser()
	 */

	private List<NumericalHitoComposite> getBudgetMilestoneDataReport(ReportParameter parameter) throws GWTException,
			SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		try {
			List<MileStone> mileStoneList = serviceAdministrator.findAllMilestoneByReportParameter(parameter);

			ArrayList<MilestoneVO> listaMilestoneVO = new ArrayList<MilestoneVO>();
			for (MileStone m : mileStoneList) {
				int i = 0;
				List<MilestoneMonthVO> mmvos = new ArrayList<MilestoneMonthVO>();
				MilestoneVO mvo = new MilestoneVO();
				for (MileStoneMonth mm : m.getMileStoneMonthList()) {
					if (i++ == 0) {
						mvo.setDescripcion(m.getDescription());
						mvo.setProjectId(m.getProject().getId());
						mvo.setProjectName(m.getProject().getName());
						mvo.setUnitOrganizationId(m.getProject().getUnitOrganizational().getId());
						mvo.setUnitOrganizationName(m.getProject().getUnitOrganizational().getName());
					}
					MilestoneMonthVO mmvo = new MilestoneMonthVO();
					mmvo.setAnio(mm.getYear().toString());
					mmvo.setMes(mm.getMonthName());
					mmvo.setDevengado(mm.getEarnedValue() != null ? mm.getEarnedValue().toString() : "0");
					mmvo.setEjecutado(mm.getExcecutedValue() != null ? mm.getExcecutedValue().toString() : "0");
					mmvo.setPlaneado(mm.getPlanedValue() != null ? mm.getPlanedValue().toString() : "0");
					mmvos.add(mmvo);

				}
				mvo.setMonthsMileStoneVO(mmvos);
				listaMilestoneVO.add(mvo);
			}
			List<HitoComposite> listaHitoComposite = this.pasarComposite(listaMilestoneVO);
			List<NumericalHitoComposite> listaNumericalHitoComposite = new ArrayList<NumericalHitoComposite>();
			for (HitoComposite hc : listaHitoComposite) {
				listaNumericalHitoComposite.add(new NumericalHitoComposite(hc));
			}
			return listaNumericalHitoComposite;

		} catch (ServiceException e) {
			throw new GWTException(e);
		}
	}

	private List<MilestoneVO> getFromMilestoneMonthList(List<MileStoneMonth> allMileStoneMonth)
			throws MonthNotFoundException {
		List<Integer> meses = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
		List<MilestoneVO> resultados = new ArrayList<MilestoneVO>();
		for (Integer mes : meses) {
			String nombreMes = Month.getMonthName(mes);
			BigDecimal planificado = BigDecimal.ZERO;
			BigDecimal ejecutado = BigDecimal.ZERO;
			BigDecimal devengado = BigDecimal.ZERO;
			for (MileStoneMonth mm : allMileStoneMonth) {
				if (mm.getMonthNumber().equals(mes)) {
					planificado = planificado.add(mm.getPlanedValue());
					ejecutado = ejecutado.add(mm.getExcecutedValue());
					devengado = devengado.add(mm.getEarnedValue());
				}
			}
			MilestoneVO planificadoVo = new MilestoneVO();
			planificadoVo.setAnio("1");
			planificadoVo.setDescripcion(SumaryDescription.COMPROMETIDO.getValue());
			planificadoVo.setEjecutado(planificado.toString());
			planificadoVo.setPlaneado(planificado.toString());
			planificadoVo.setDevengado(planificado.toString());
			planificadoVo.setMes(nombreMes);

			MilestoneVO ejecutadoVO = new MilestoneVO();
			ejecutadoVO.setAnio("1");
			ejecutadoVO.setDescripcion(SumaryDescription.EJECUTADO.getValue());
			ejecutadoVO.setEjecutado(ejecutado.toString());
			ejecutadoVO.setPlaneado(ejecutado.toString());
			ejecutadoVO.setDevengado(ejecutado.toString());
			ejecutadoVO.setMes(nombreMes);

			MilestoneVO devengadoVO = new MilestoneVO();
			devengadoVO.setAnio("1");
			devengadoVO.setDescripcion(SumaryDescription.DEVENGADO.getValue());
			devengadoVO.setEjecutado(devengado.toString());
			devengadoVO.setPlaneado(devengado.toString());
			devengadoVO.setDevengado(devengado.toString());
			devengadoVO.setMes(nombreMes);

			resultados.add(planificadoVo);
			resultados.add(ejecutadoVO);
			resultados.add(devengadoVO);
		}
		return resultados;
	}

	/**
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#getListMilestone(java.lang.String, java.lang.String[],
	 *      java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public String getListMilestone(String anio, String[] meses, String prefijo) throws GWTException {

		List<MilestoneVO> mileStones = (List<MilestoneVO>) this.getAttributeSessionHttp(prefijo + anio);
		return this.toJson2(this.pasarComposite(mileStones));

	}

	public String getLogedUser() throws GWTException {
		try {
			List<User> users = new ArrayList<User>();
			User u = (User) this.getAttributeSessionHttp(User.class);
			if (u != null) {
				users.add(u);
			}
			return this.toJson2(users);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e.getMessage(), e);
		}
	}

	public String getProjects(Long unitOrganizationId) throws GWTException {
		try {
			List<Project> list = serviceAdministrator.findAllProjectsInUnitOrganization(unitOrganizationId);
			return this.toJson2(list);
		} catch (ServiceException e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e.getMessage(), e);
		}
	}

	public Map<String, List<SimpleComposite>> getStores() throws GWTException {
		try {
			Map<String, List<SimpleComposite>> result = new HashMap<String, List<SimpleComposite>>();
			List<UnitTime> unitTimes = serviceAdministrator.findAllUnitTime();

			List<SimpleComposite> unitTimeListSimpleComposite = new ArrayList<SimpleComposite>();
			for (UnitTime ut : unitTimes) {
				unitTimeListSimpleComposite.add(new SimpleComposite(ut.getId().toString(), ut.getDescription()));
			}

			List<Beneficiary> beneficiaries = serviceAdministrator.findAllBeneficiary();
			List<SimpleComposite> beneficiariesListSimpleComposite = new ArrayList<SimpleComposite>();
			for (Beneficiary ben : beneficiaries) {
				beneficiariesListSimpleComposite.add(new SimpleComposite(ben.getId().toString(), ben.getName()));
			}

			result.put("unitTimes", unitTimeListSimpleComposite);
			result.put("beneficiaries", beneficiariesListSimpleComposite);
			return result;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e.getMessage(), e);
		}
	}

	public String[][] getUnitOrganization() throws GWTException {
		try {
			List<UnitOrganizational> list = serviceAdministrator.findAllThatHaveProjects();
			String result[][] = new String[list.size()][2];
			for (int i = 0; i < list.size(); i++) {
				String[] strings = new String[2];
				UnitOrganizational uo = list.get(i);
				strings[0] = uo.getId().toString();
				strings[1] = uo.getName();
				result[i] = strings;
			}
			return result;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e.getMessage(), e);
		}
	}

	public String[] iniciarHitosPrimerAnioProyecto(String projectId) throws GWTException {
		if (log.isDebugEnabled()) {
			log.debug("retrive project id=" + projectId);
		}

		String[] result = new String[] { "", "", "" };

		this.borrarAnios();

		try {
			Project project = serviceAdministrator.findByIdProject(Long.valueOf(projectId));
			Calendar calProyecto = new GregorianCalendar();
			calProyecto.setTime(project.getFirstDate());
			int anio = calProyecto.get(Calendar.YEAR);

			List<Integer> anios = serviceAdministrator.findYearsInBudgetMileStonesByProject(Long.valueOf(projectId));
			if (anios.contains(anio)) {
				ArrayList<MilestoneVO> listaSesion = new ArrayList<MilestoneVO>();
				for (MileStone m : project.getMileStones()) {
					int i = 0;
					MilestoneVO mvo = new MilestoneVO();
					mvo.setDescripcion(m.getDescription());
					List<MilestoneMonthVO> mmvos = new ArrayList<MilestoneMonthVO>();
					for (MileStoneMonth mm : m.getMileStoneMonthList()) {
						if (mm.getYear().equals(Integer.valueOf(anio))) {
							MilestoneMonthVO mmvo = new MilestoneMonthVO();
							mmvo.setAnio(mm.getYear().toString());
							mmvo.setDevengado(mm.getEarnedValue() != null ? mm.getEarnedValue().toString() : "");
							mmvo.setEjecutado(mm.getExcecutedValue() != null ? mm.getExcecutedValue().toString() : "");
							mmvo.setPlaneado(mm.getPlanedValue() != null ? mm.getPlanedValue().toString() : "");
							mmvo.setMes(mm.getMonthName());
							mmvos.add(mmvo);
						}
					}
					if (!mmvos.isEmpty()) {
						mvo.setMonthsMileStoneVO(mmvos);
						listaSesion.add(mvo);
					}
				}
				this.setAttributeSessionHttp("hitos" + anio, listaSesion);
				result[0] = this.toJson2(this.pasarComposite(listaSesion));

				Set<String> aniosGuardarInicio = new TreeSet<String>();
				aniosGuardarInicio.add(String.valueOf(anio));
				this.setAttributeSessionHttp("aniosGuardarhitos", aniosGuardarInicio);

			} else {
				this.setAttributeSessionHttp("hitos" + anio, new ArrayList<MilestoneVO>());
				result[0] = this.toJson2(this.pasarComposite(new ArrayList<MilestoneVO>()));

				Set<String> aniosGuardarInicio = new TreeSet<String>();
				aniosGuardarInicio.add(String.valueOf(anio));
				this.setAttributeSessionHttp("aniosGuardarhitos", aniosGuardarInicio);

			}

			anios = serviceAdministrator.findYearsInAdministrationMileStoneByProject(Long.valueOf(projectId));

			if (anios.contains(anio)) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				ArrayList<MilestoneVO> listaSesion = new ArrayList<MilestoneVO>();
				for (AdministrationMileStone m : project.getAdministrationMileStoneList()) {
					int i = 0;
					List<MilestoneMonthVO> mmavos = new ArrayList<MilestoneMonthVO>();
					MilestoneVO admmvo = new MilestoneVO();
					for (AdministrationMileStoneMonth aammmm : m.getAdministrationMilestoneList()) {
						if (aammmm.getMilestoneYear().equals(Integer.valueOf(anio))) {
							if (i++ == 0) {
								admmvo.setDescripcion(m.getMileStoneDescription());
							}
							MilestoneMonthVO mmvo = new MilestoneMonthVO();
							mmvo.setAnio(aammmm.getMilestoneYear().toString());
							mmvo.setEjecutado(aammmm.getExcecutedDate() != null ? sdf.format(aammmm.getExcecutedDate())
									: "");
							mmvo.setMes(aammmm.getMonthName());
							mmvo.setPlaneado(aammmm.getPlanedDate() != null ? sdf.format(aammmm.getPlanedDate()) : "");
							mmavos.add(mmvo);
						}

					}
					if (!mmavos.isEmpty()) {
						admmvo.setMonthsMileStoneVO(mmavos);
						listaSesion.add(admmvo);
					}
				}

				result[2] = this.toJson2(this.pasarComposite(listaSesion));
				this.setAttributeSessionHttp("hitoAdm" + anio, listaSesion);

				Set<String> aniosGuardarInicio = new TreeSet<String>();
				aniosGuardarInicio.add(String.valueOf(anio));
				this.setAttributeSessionHttp("aniosGuardarhitoAdm", aniosGuardarInicio);

			} else {
				this.setAttributeSessionHttp("hitoAdm" + anio, new ArrayList<MilestoneVO>());
				Set<String> aniosGuardarInicio = new TreeSet<String>();
				aniosGuardarInicio.add(String.valueOf(anio));
				this.setAttributeSessionHttp("aniosGuardarhitoAdm", aniosGuardarInicio);
				result[2] = this.toJson2(this.pasarComposite(null));
			}
			this.setAttributeSessionHttp("anioRegistro", anio);
			result[1] = String.valueOf(anio);
			return result;

		} catch (Exception e) {
			log.error(e, e);
			throw new GWTException(e);
		}
	}

	private String initReportParamAnio(String[] parameter) {
		String anio = null;
		if (parameter[0] != null && parameter[0].length() != 0 && parameter[1] != null && parameter[1].length() != 0) {
			if (parameter[0].equalsIgnoreCase("TODOS") || parameter[1].equalsIgnoreCase("TODOS")) {
				anio = "TODOS";
			} else {
				anio = "Desde: " + parameter[0] + " Hasta: " + parameter[1];
			}
		} else if (parameter[0] != null && parameter[0].length() != 0 && !parameter[0].equalsIgnoreCase("TODOS")) {
			anio = parameter[0];
		} else {
			anio = "TODOS";
		}
		return anio;
	}

	private String initReportParamMes(String[] parameter) {
		String mes = null;
		if (parameter[2] != null && parameter[3] != null && parameter[2].length() > 0 && parameter[3].length() > 0) {
			if (parameter[2].equalsIgnoreCase("TODOS") || parameter[3].equalsIgnoreCase("TODOS")) {
				mes = "TODOS";
			} else {
				mes = "Desde: " + parameter[6] + " Hasta: " + parameter[7];
			}
		} else if (parameter[2] != null && parameter[2].length() > 0 && !parameter[2].equalsIgnoreCase("TODOS")) {
			mes = parameter[2];
		}
		return mes;
	}

	private String initReportParamProyecto(String[] parameter) {
		String unidad = null;
		if (parameter[5] != null) {
			if (parameter[5].equalsIgnoreCase("TODOS")) {
				unidad = "TODAS";
			} else {
				unidad = parameter[9];
			}
		}
		return unidad;
	}

	private String initReportParamUnidadOrganizacional(String[] parameter) {
		String unidad = null;
		if (parameter[4] != null) {
			if (parameter[4].equalsIgnoreCase("TODOS")) {
				unidad = "TODAS";
			} else {
				unidad = parameter[8];
			}
		}
		return unidad;
	}

	public void logout() throws GWTException {
		this.invalidateSessionUser();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#modifyProject(java .lang.String, java.util.Map)
	 */
	public Long modifyProject(String jsonProject, Map<String, String> riskAndMitigation, String idEmployee)
			throws GWTException {

		if (log.isDebugEnabled()) {
			log.debug("jsonProject=" + jsonProject);
		}
		System.out.println(" JSON 555 " + jsonProject);
		System.out.println(" MAP " + riskAndMitigation.toString());
		try {
			Project project = (Project) this.fromJson(jsonProject, Project.class);
			Project tempProject;
			project.setStatus("1");
			User user = (User) this.getAttributeSessionHttp(User.class);
			project.setUser(user);

			// Employee employee = new Employee();
			// employee.setId(Long.valueOf(idEmployee));
			// project.setEmployee(employee);

			List<Risk> risks = new ArrayList<Risk>();
			List<Mitigation> mitigations = null;

			Set<String> descriptionRisks = riskAndMitigation.keySet();
			for (String description : descriptionRisks) {

				Risk risk = new Risk();
				risk.setDescription(description);
				risk.setStatus("1");
				risk.setUser(user);
				risk.setProject(project);
				risks.add(risk);
				String descriptionMitigation = riskAndMitigation.get(description);
				Mitigation mitigation = new Mitigation();
				mitigation.setDescription(descriptionMitigation);
				mitigation.setUser(user);
				mitigation.setStatus("1");
				mitigation.setRisk(risk);
				mitigations = new ArrayList<Mitigation>();
				mitigations.add(mitigation);
				risk.setMitigations(mitigations);

			}
			project.setRisks(risks);
			System.out.println("Antes de modificar");
			// modificar
			tempProject = serviceAdministrator.modifyProject(project);
			System.out.println("Despues de modificar");
			this.setAttributeSessionHttp("UNIT_ORGANIZATIONAL_LIST", null);
			this.buildTreeUnitOrganizational();
			return tempProject.getId();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("  " + e.getMessage());
			throw new GWTException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#nextMilestone(java.lang.String, java.util.List)
	 */
	public String nextMilestone(String currentYear, List<MilestoneVO> milestoneVOList, String prefijo)
			throws GWTException {

		this.storeMilestoneSession(milestoneVOList, currentYear, prefijo);

		int nextYear = Integer.parseInt(currentYear) + 1;

		// Set<String> aniosGuardar = (Set<String>) getAttributeSessionHttp("aniosGuardar" + prefijo);
		// if (aniosGuardar != null) {
		// aniosGuardar.add(currentYear);
		// } else {
		// aniosGuardar = new TreeSet<String>();
		// aniosGuardar.add(String.valueOf(currentYear));
		// setAttributeSessionHttp("aniosGuardar" + prefijo, aniosGuardar);
		// }

		String[] meses = new String[] { "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto",
				"septiembre", "octubre", "noviembre", "diciembre" };

		return this.getListMilestone(String.valueOf(nextYear), meses, prefijo);

	}

	private List<HitoComposite> pasarComposite(List<MilestoneVO> mileStones) throws GWTException {

		List<HitoComposite> result = new ArrayList<HitoComposite>();
		try {

			HitoComposite hitoComposite = null;

			if (!(mileStones != null && !mileStones.isEmpty())) {
				return result;
			}

			for (MilestoneVO milestoneVO : mileStones) {

				hitoComposite = new HitoComposite();
				hitoComposite.setCommon(milestoneVO.getDescripcion());
				hitoComposite.setProjectId(milestoneVO.getProjectId());
				hitoComposite.setProjectName(milestoneVO.getProjectName());
				hitoComposite.setUnitOrganizationId(milestoneVO.getUnitOrganizationId());
				hitoComposite.setUnitOrganizationName(milestoneVO.getUnitOrganizationName());

				List<MilestoneMonthVO> hitosMesesVO = milestoneVO.getMonthsMileStoneVO();

				if (hitosMesesVO == null) {
					result.add(hitoComposite);
					continue;
				}

				for (MilestoneMonthVO milestoneMonthVO : hitosMesesVO) {

					char[] inicial = new char[] { milestoneMonthVO.getMes().charAt(0) };
					String mes = new String(inicial).toUpperCase()
							+ milestoneMonthVO.getMes().toLowerCase().substring(1);

					Method metodoP = HitoComposite.class.getMethod("set" + mes + "P", String.class);
					metodoP.invoke(hitoComposite, milestoneMonthVO.getPlaneado());

					Method metodoE = HitoComposite.class.getMethod("set" + mes + "E", String.class);
					metodoE.invoke(hitoComposite, milestoneMonthVO.getEjecutado());

					Method metodoD = HitoComposite.class.getMethod("set" + mes + "D", String.class);
					metodoD.invoke(hitoComposite, milestoneMonthVO.getDevengado());
					hitoComposite.setYear(Integer.valueOf(milestoneMonthVO.getAnio()));

				}
				result.add(hitoComposite);

			}

			return result;

		} catch (SecurityException e) {
			e.printStackTrace();
			throw new GWTException(e.getMessage());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new GWTException(e.getMessage());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new GWTException(e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new GWTException(e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new GWTException(e.getMessage());
		}

	}

	private void recolectAllUnitOrganizationIds(UnitOrganizationalComposite uo, List<Long> ids) {
		ids.add(Long.valueOf(uo.getId()));
		if (uo.getUnits() != null) {
			for (UnitOrganizationalComposite hijo : uo.getUnits()) {
				this.recolectAllUnitOrganizationIds(hijo, ids);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#storeMilestoneSession(java.util.List, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public void storeMilestoneSession(List<MilestoneVO> milestoneVOList, String anio, String prefijo)
			throws GWTException {

		Set<String> aniosGuardar = (Set<String>) this.getAttributeSessionHttp("aniosGuardar" + prefijo);
		if (aniosGuardar != null) {
			aniosGuardar.add(anio);
		} else {
			aniosGuardar = new TreeSet<String>();
			aniosGuardar.add(String.valueOf(anio));
			this.setAttributeSessionHttp("aniosGuardar" + prefijo, aniosGuardar);
		}

		List<MilestoneVO> hitosExistentes = (List<MilestoneVO>) this.getAttributeSessionHttp(prefijo + anio);
		if (hitosExistentes != null && hitosExistentes.size() != 0) {
			this.removeAttributeSessionHttp(prefijo + anio);
		}
		this.setAttributeSessionHttp(prefijo + anio, milestoneVOList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#storeProject(java. lang.String)
	 */
	public Long storeProject(String jsonProject) throws GWTException {
		if (log.isDebugEnabled()) {
			log.debug("jsonProject=" + jsonProject);
		}
		System.out.println("storeProject 8888");
		try {
			Project project = (Project) this.fromJson(jsonProject, Project.class);
			Project tempProject;
			project.setStatus("1");
			User user = (User) this.getAttributeSessionHttp(User.class);
			project.setUser(user);

			if (project.getId() == null) {
				// nuevo
				tempProject = serviceAdministrator.createProject(project);
			} else {
				// modificar
				System.out.println("ServiceWebAdministrationImpl.storeProject() " + project.getStatus());
				tempProject = serviceAdministrator.modifyProject(project);
			}
			this.setAttributeSessionHttp("UNIT_ORGANIZATIONAL_LIST", null);
			this.buildTreeUnitOrganizational();
			return tempProject.getId();
		} catch (final Exception e) {
			e.printStackTrace();
			System.out.println("ServiceWebAdministrationImpl.storeProject() " + e.getMessage());
			log.error(e.getMessage(), e);
			throw new GWTException(e.getMessage(), e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#storeProject(java. lang.String, java.util.List)
	 */
	public Long storeProject(String jsonProject, List<RiskVO> riesgosMitigacion, List<LocationGeographicVO> locGeo)
			throws GWTException {

		if (log.isDebugEnabled()) {
			log.debug("jsonProject=" + jsonProject);
		}
		try {
			Project project = (Project) this.fromJson(jsonProject, Project.class);
			this.toUppercase(project);

			if (project.getStatus() == null) {
				project.setStatus("1");
			}
			Project tempProject;
			User user = (User) this.getAttributeSessionHttp(User.class);
			project.setUser(user);
			List<Risk> risks = new ArrayList<Risk>();
			List<Mitigation> mitigations = null;

			for (RiskVO riskVO : riesgosMitigacion) {
				Risk risk = new Risk();
				risk.setDescription(riskVO.getDescripcion());
				risk.setStatus("1");
				risk.setUser(user);
				risk.setProject(project);
				mitigations = new ArrayList<Mitigation>();
				MitigacionVO mitigacionVO = riskVO.getMitigacionVO();
				Mitigation mitigation = new Mitigation();
				mitigation.setDescription(mitigacionVO.getDescription());
				mitigation.setStatus("1");
				mitigation.setUser(user);
				mitigation.setRisk(risk);
				mitigations.add(mitigation);
				risk.setMitigations(mitigations);
				risks.add(risk);
			}
			project.setRisks(risks);

			List<LocationGeography> locaGeolist = new ArrayList<LocationGeography>();
			for (LocationGeographicVO locaGeofVO : locGeo) {
				LocationGeography locaGeof = new LocationGeography();
				locaGeof.setUser(user);
				locaGeof.setStatus("1");
				locaGeof.setBeneficiary(new Beneficiary(locaGeofVO.getIdBeneficiario()));
				locaGeof.setLocation(new Location(locaGeofVO.getIdLocalidad()));
				locaGeof.setProject(project);
				locaGeolist.add(locaGeof);
			}
			project.setLocationGeographycs(locaGeolist);

			if (project.getId() == null) {
				// nuevo
				tempProject = serviceAdministrator.createProject(project);
			} else {
				// modificar
				tempProject = serviceAdministrator.modifyProject(project);
			}
			this.setAttributeSessionHttp("UNIT_ORGANIZATIONAL_LIST", null);
			this.buildTreeUnitOrganizational();
			return tempProject.getId();
		} catch (final Exception e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public void storeProjectAdministrationMileStones(List<String> years, String projectId) throws GWTException {
		try {

			// aniosGuardar
			Set<String> aniosGuardar = (Set<String>) this.getAttributeSessionHttp("aniosGuardarhitoAdm");
			if (aniosGuardar == null) {
				aniosGuardar = new TreeSet<String>();
				String anioRegistro = ((Integer) this.getAttributeSessionHttp("anioRegistro")).toString();
				aniosGuardar.add(anioRegistro);
			}

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			List<List<AdministrationMileStone>> listaPersistir = new ArrayList<List<AdministrationMileStone>>();
			for (String year : aniosGuardar) {
				List<MilestoneVO> vos = (List<MilestoneVO>) this.getAttributeSessionHttp("hitoAdm" + year);
				List<AdministrationMileStone> ams = new ArrayList<AdministrationMileStone>();
				if (vos == null) {
					throw new ServiceException("No se ha encontrado la variable de sesion hito" + year
							+ " que se quiere persisitir");
				}

				for (MilestoneVO vo : vos) {
					AdministrationMileStone am = new AdministrationMileStone();
					am.setRegisterDate(new Date());
					am.setProject(new Project(Long.valueOf(projectId)));
					am.setMileStoneDescription(vo.getDescripcion());
					am.setStatus("1");
					am.setUser((User) this.getAttributeSessionHttp(User.class));

					List<MilestoneMonthVO> msmVO = vo.getMonthsMileStoneVO();
					List<AdministrationMileStoneMonth> amms = new ArrayList<AdministrationMileStoneMonth>();
					for (MilestoneMonthVO milestoneMonthVO : msmVO) {
						AdministrationMileStoneMonth amm = new AdministrationMileStoneMonth();
						amm.setRegisterDate(new Date());
						amm.setMonthNumber(Month.getMonthNumber(milestoneMonthVO.getMes().toLowerCase()));
						amm.setMonthName(milestoneMonthVO.getMes());
						amm.setMilestoneYear(Integer.valueOf(milestoneMonthVO.getAnio()));
						amm.setPlanedDate(milestoneMonthVO.getPlaneado() != null
								&& !milestoneMonthVO.getPlaneado().equals("") ? sdf.parse(milestoneMonthVO
								.getPlaneado()) : null);
						amm.setExcecutedDate(milestoneMonthVO.getEjecutado() != null
								&& !milestoneMonthVO.getEjecutado().equals("") ? sdf.parse(milestoneMonthVO
								.getEjecutado()) : null);
						amm.setStatus("1");
						amm.setUser((User) this.getAttributeSessionHttp(User.class));
						this.toUppercase(amm);
						amms.add(amm);
					}
					am.setAdministrationMilestoneList(amms);
					this.toUppercase(am);
					ams.add(am);
				}
				listaPersistir.add(ams);
			}
			serviceAdministrator.createAllAdministrationMileStone(listaPersistir, Long.valueOf(projectId));
		} catch (Exception e) {
			log.error(e, e);
			throw new GWTException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public void storeProjectMileStones(List<String> years, String projectId) throws GWTException {
		try {

			// aniosGuardar
			Set<String> aniosGuardar = (Set<String>) this.getAttributeSessionHttp("aniosGuardarhitos");
			if (aniosGuardar == null) {
				aniosGuardar = new TreeSet<String>();
				String anioRegistro = ((Integer) this.getAttributeSessionHttp("anioRegistro")).toString();
				aniosGuardar.add(anioRegistro);
			}

			List<List<MileStone>> listaPersistir = new ArrayList<List<MileStone>>();
			for (String year : aniosGuardar) {
				List<MilestoneVO> vos = (List<MilestoneVO>) this.getAttributeSessionHttp("hitos" + year);
				List<MileStone> ams = new ArrayList<MileStone>();
				if (vos == null) {
					throw new ServiceException("No se ha encontrado la variable de sesion hitos" + year
							+ " que se quiere persisitir");
				}

				for (MilestoneVO vo : vos) {
					MileStone m = new MileStone();
					m.setDate(new Date());
					m.setProject(new Project(Long.valueOf(projectId)));
					// m.setMonthNumber(Month.getMonthNumber(vo.getMes()));
					// m.setMonthName(vo.getMes());
					// m.setYear(Integer.valueOf(vo.getAnio()));
					// m.setPlanedValue(new BigDecimal(vo.getPlaneado()));
					// m.setExcecutedValue((new BigDecimal(vo.getEjecutado())));
					// m.setEarnedValue(new BigDecimal(vo.getDevengado()));
					m.setDescription(vo.getDescripcion());
					m.setStatus("1");
					m.setUser((User) this.getAttributeSessionHttp(User.class));

					List<MilestoneMonthVO> milestonesMonthVO = vo.getMonthsMileStoneVO();
					List<MileStoneMonth> milestonesMonth = new ArrayList<MileStoneMonth>();
					for (MilestoneMonthVO milestoneMonthVO : milestonesMonthVO) {
						MileStoneMonth msm = new MileStoneMonth();
						msm.setDate(new Date());
						msm.setYear(Integer.valueOf(milestoneMonthVO.getAnio()));
						msm.setEarnedValue(new BigDecimal(milestoneMonthVO.getDevengado()));
						msm.setExcecutedValue((new BigDecimal(milestoneMonthVO.getEjecutado())));
						msm.setMileStone(m);
						msm.setMonthName(milestoneMonthVO.getMes());
						msm.setMonthNumber(Month.getMonthNumber(milestoneMonthVO.getMes().toLowerCase()));
						msm.setPlanedValue(new BigDecimal(milestoneMonthVO.getPlaneado()));
						msm.setStatus("1");
						msm.setUser((User) this.getAttributeSessionHttp(User.class));
						this.toUppercase(msm);
						milestonesMonth.add(msm);
					}
					m.setMileStoneMonthList(milestonesMonth);
					this.toUppercase(m);
					ams.add(m);
				}
				listaPersistir.add(ams);
			}
			serviceAdministrator.createAllMileStone(listaPersistir, Long.valueOf(projectId));
		} catch (Exception e) {
			log.error(e, e);
			throw new GWTException(e);
		}
	}

	private List<SumaryComposite> toSumaryComposite(List<MilestoneVO> vos) throws GWTException {
		try {

			List<SumaryComposite> result = new ArrayList<SumaryComposite>();
			List<SumaryDescription> descriptions = Arrays.asList(SumaryDescription.COMPROMETIDO,
					SumaryDescription.DEVENGADO, SumaryDescription.EJECUTADO);
			for (SumaryDescription sd : descriptions) {
				SumaryComposite sc = new SumaryComposite();
				sc.setDescripcion(sd.getValue());
				BigDecimal total = BigDecimal.ZERO;
				for (MilestoneVO mvo : vos) {
					if (mvo.getDescripcion().equals(sd.getValue())) {
						total = total.add(new BigDecimal(mvo.getEjecutado()));
						char[] inicial = new char[] { mvo.getMes().charAt(0) };
						String mes = new String(inicial).toUpperCase() + mvo.getMes().substring(1);

						Method setDescripcion = SumaryComposite.class.getMethod("setDescripcion", String.class);
						setDescripcion.invoke(sc, "<b>" + mvo.getDescripcion() + "</b>");

						Method set = SumaryComposite.class.getMethod("set" + mes, String.class);
						set.invoke(sc, mvo.getPlaneado());
					}
				}
				sc.setTotal(total.toString());
				result.add(sc);
			}
			return result;
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e);
		} catch (IllegalAccessException e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e);
		} catch (InvocationTargetException e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e);
		} catch (SecurityException e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e);
		} catch (NoSuchMethodException e) {
			log.error(e.getMessage(), e);
			throw new GWTException(e);
		}
	}

	/**
	 * 
	 * Se encarga de cambiar a mayusculas todos los strings de una entidad que extienda de model basic
	 * 
	 * @param <T>
	 * @param t
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private <T extends ModelBasic> void toUppercase(T t) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		for (Field field : t.getClass().getDeclaredFields()) {
			if (field.getType().getSimpleName().equals(String.class.getSimpleName())) {
				char[] inicial = new char[] { field.getName().charAt(0) };
				StringBuffer getterMethodName = new StringBuffer();
				getterMethodName.append("get").append(new String(inicial).toUpperCase()).append(
						field.getName().substring(1));
				Method getterMethod = t.getClass().getMethod(getterMethodName.toString(), new Class[] {});

				StringBuffer setterMethodName = new StringBuffer();
				setterMethodName.append("set").append(new String(inicial).toUpperCase()).append(
						field.getName().substring(1));
				Method setterMethod = t.getClass().getMethod(setterMethodName.toString(), String.class);

				String result = (String) getterMethod.invoke(t, new Object[] {});

				if (result != null) {
					setterMethod.invoke(t, result.toUpperCase());
				}

			}
		}

	}

	/**
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#viewAdministrationMilestoneReport(java.lang.String[])
	 */
	public String viewAdministrationMilestoneReport(String[] param) throws GWTException {
		ReportParameter reportParameter = new ReportParameter();
		reportParameter.setAnioFiscalDesde(param[0]);
		reportParameter.setAnioFiscalHasta(param[1]);
		reportParameter.setMesDesde(param[2]);
		reportParameter.setMesHasta(param[3]);
		reportParameter.setUnidadOrganizacional(param[4]);
		reportParameter.setProyecto(param[5]);

		List<HitoComposite> listHitoComposite = this.getAdminMilestoneDataReport(reportParameter);
		AdminitrationMilestoneReportConfig amrc = new AdminitrationMilestoneReportConfig(listHitoComposite);

		final ConfiguradorReporte cr = amrc.obtenerConfiguracion();
		final String confReportId = cr.obtenerId();
		cr.agregarParametro("FILTRO_ANIOS", this.initReportParamAnio(param));
		cr.agregarParametro("FILTRO_MESES", this.initReportParamMes(param));
		cr.agregarParametro("NOMBRE_UNIDAD_ORGANIZACIONAL", this.initReportParamUnidadOrganizacional(param));
		cr.agregarParametro("NOMBRE_PROYECTO", this.initReportParamProyecto(param));
		cr.agregarParametro("TABLE_TITLE", "AŃO FISCAL:" + this.initReportParamAnio(param));

		this.setAttributeSessionHttp(confReportId, cr);

		return "ueeReporte?confReportId=" + confReportId;
	}

	/**
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#viewAnualAdminReport(java.lang.String[])
	 */
	public String viewAnualAdminReport(String[] param) throws GWTException {
		try {
			ReportParameter reportParameter = new ReportParameter();
			reportParameter.setAnioFiscalDesde(param[0]);
			reportParameter.setAnioFiscalHasta(param[1]);
			reportParameter.setMesDesde(param[2]);
			reportParameter.setMesHasta(param[3]);
			reportParameter.setUnidadOrganizacional(param[4]);
			reportParameter.setProyecto(param[5]);

			AdminReportData adminReportData = serviceAdministrator.getYearlyAdminReportData(reportParameter);

			adminReportData.setReportTitle("AVANCE DE PROYECTOS");
			adminReportData.setTableTitle("AŃO FISCAL:" + this.initReportParamAnio(param));
			adminReportData.setNombreUnidadOrganizacional(this.initReportParamUnidadOrganizacional(param));
			AnualAdminReportConfig aarc = new AnualAdminReportConfig(adminReportData);
			final ConfiguradorReporte cr = aarc.obtenerConfiguracion();
			cr.agregarParametro("FILTRO_ANIOS", this.initReportParamAnio(param));
			cr.agregarParametro("FILTRO_MESES", this.initReportParamMes(param));
			cr.agregarParametro("NOMBRE_UNIDAD_ORGANIZACIONAL", this.initReportParamUnidadOrganizacional(param));
			cr.agregarParametro("NOMBRE_PROYECTO", this.initReportParamProyecto(param));
			cr.agregarParametro("TABLE_TITLE", "AŃO FISCAL:" + this.initReportParamAnio(param));

			final String confReportId = cr.obtenerId();
			this.setAttributeSessionHttp(confReportId, cr);
			return "ueeReporte?confReportId=" + confReportId;
		} catch (Exception e) {
			log.error(e, e);
			throw new GWTException(e);
		}
	}

	/**
	 * @see org.taurus.web.gwt.client.ServiceWebAdministration#viewAnualReport(java.lang.Integer)
	 */
	public String viewAnualReport(String[] param) throws GWTException {
		try {
			ReportParameter reportParameter = new ReportParameter();
			reportParameter.setAnioFiscalDesde(param[0]);
			reportParameter.setAnioFiscalHasta(param[1]);
			reportParameter.setMesDesde(param[2]);
			reportParameter.setMesHasta(param[3]);
			reportParameter.setUnidadOrganizacional(param[4]);
			reportParameter.setProyecto(param[5]);
			BudgetReportData budgetReportData = serviceAdministrator.getYearlyReport(reportParameter);
			budgetReportData.setReportTitle("PRESUPUESTO DE PROYECTOS");
			String anioFiscalLabel = this.initReportParamAnio(param);
			budgetReportData.setTableTitle("AŃO FISCAL: " + (anioFiscalLabel != null ? anioFiscalLabel : "TODOS"));
			budgetReportData.setNombreUnidadOrganizacional(this.initReportParamUnidadOrganizacional(param));

			AnualBudgetReportConfig arc = new AnualBudgetReportConfig(budgetReportData);
			final ConfiguradorReporte cr = arc.obtenerConfiguracion();
			cr.agregarParametro("FILTRO_ANIOS", this.initReportParamAnio(param));
			cr.agregarParametro("FILTRO_MESES", this.initReportParamMes(param));
			cr.agregarParametro("NOMBRE_UNIDAD_ORGANIZACIONAL", this.initReportParamUnidadOrganizacional(param));
			cr.agregarParametro("NOMBRE_PROYECTO", this.initReportParamProyecto(param));
			final String confReportId = cr.obtenerId();
			this.setAttributeSessionHttp(confReportId, cr);
			return "ueeReporte?confReportId=" + confReportId;
		} catch (Exception e) {
			log.error(e, e);
			throw new GWTException(e);
		}
	}

	public String viewBudgetMilestoneReport(String[] param) throws GWTException {
		try {
			ReportParameter reportParameter = new ReportParameter();
			reportParameter.setAnioFiscalDesde(param[0]);
			reportParameter.setAnioFiscalHasta(param[1]);
			reportParameter.setMesDesde(param[2]);
			reportParameter.setMesHasta(param[3]);
			reportParameter.setUnidadOrganizacional(param[4]);
			reportParameter.setProyecto(param[5]);
			List<NumericalHitoComposite> hitos;
			hitos = this.getBudgetMilestoneDataReport(reportParameter);
			BudgetMilestoneReportConfig brc = new BudgetMilestoneReportConfig(hitos);
			final ConfiguradorReporte cr = brc.obtenerConfiguracion();
			final String confReportId = cr.obtenerId();

			cr.agregarParametro("FILTRO_ANIOS", this.initReportParamAnio(param));
			cr.agregarParametro("FILTRO_MESES", this.initReportParamMes(param));
			cr.agregarParametro("NOMBRE_UNIDAD_ORGANIZACIONAL", this.initReportParamUnidadOrganizacional(param));
			cr.agregarParametro("NOMBRE_PROYECTO", this.initReportParamProyecto(param));
			cr.agregarParametro("TABLE_TITLE", "AŃO FISCAL:" + this.initReportParamAnio(param));

			this.setAttributeSessionHttp(confReportId, cr);

			return "ueeReporte?confReportId=" + confReportId;
		} catch (SecurityException e) {
			log.error(e, e);
			throw new GWTException(e);
		} catch (IllegalArgumentException e) {
			log.error(e, e);
			throw new GWTException(e);
		} catch (NoSuchMethodException e) {
			log.error(e, e);
			th
