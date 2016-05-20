package pt.ips.estsetubal.mig.academicCloud.client.components.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.ips.estsetubal.mig.academicCloud.client.components.WidgetSize;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.ComboField;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.DateTextField;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.DecimalTextField;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.FieldSeparator;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.MultiLineStringField;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.StringTextField;
import pt.ips.estsetubal.mig.academicCloud.client.components.utils.StringUtils;
import pt.ips.estsetubal.mig.academicCloud.client.components.utils.WidgetUtils;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.degree.CompetenceCourseDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.degree.CompetenceCourseInformationDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.degree.DepartmentDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.degree.embeddable.BibliographyDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.degree.embeddable.CourseDetailDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.degree.embeddable.CourseLoadDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.enums.degree.CurricularPeriodType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

/**
 * This component manages the a competence course data.
 * 
 * @author Ant?nio Casqueiro
 */
public class CompetenceCourseDataComponent extends AbstractComponent implements
		IComponentActionEvents {

	// [start] - Components
	private StringTextField tfCode;
	private StringTextField tfName;
	private ComboField cbDepartment;
	// private StringTextField tfDepartment;
	private DateTextField tfEndDate;
	private DateTextField tfBeginDate;

	private TabPanel tabPanel;

	private MultiLineStringField tfPrerequisites;
	private MultiLineStringField tfObjectives;
	private MultiLineStringField tfProgram;
	private MultiLineStringField tfEvaluationMethod;
	private MultiLineStringField tfTeachingMethod;
	private MultiLineStringField tfNotes;
	private DecimalTextField tfEcts;
	private ComboField cbCurricularPeriodType;
	// private StringTextField tfCurricularPeriodType;

	private DecimalTextField tfTheoretical;
	private DecimalTextField tfProblem;
	private DecimalTextField tfLaboratorial;
	private DecimalTextField tfTraining;
	private DecimalTextField tfTutorialOrientation;
	private DecimalTextField tfAutonomousWork;
	private DecimalTextField tfFieldWork;
	private DecimalTextField tfOthers;

	private MultiLineStringField tfBasicBibliography;
	private MultiLineStringField tfExtraBibliography;

	private HorizontalPanel panelNavigation;
	private Button btnPrevious = new Button();
	private Button btnNext = new Button();

	private HorizontalPanel panelActions;
	private Button btnNew = new Button();
	private Button btnDelete = new Button();
	private Button btnEdit = new Button();
	private Button btnSave = new Button();
	private Button btnCancel = new Button();

	// [end] - Components

	// [start] - Get components
	@Override
	public HasClickHandlers getSaveButton() {
		return btnSave;
	}

	@Override
	public HasClickHandlers getDeleteButton() {
		return btnDelete;
	}

	// [end] - Get components

	// [start] - Attributes
	private boolean listenersInitialized = false;
	private CompetenceCourseDTO model = null;
	private int selectedIndex = -1;
	private int maxVersions = 0;
	private ComponentMode mode;

	// [end] - Attributes

	// [start] - Set data
	public void init(CompetenceCourseDTO data) {
		init(Arrays.asList(data.getDepartment()));
	}

	public void init(List<DepartmentDTO> data) {
		// Fill combo boxes
		WidgetUtils.fill(cbCurricularPeriodType, CurricularPeriodType.class,
				enumTranslator);

		{
			List<String> values = new ArrayList<String>();
			List<String> labels = new ArrayList<String>();

			for (DepartmentDTO elem : data) {
				labels.add(elem.getName());
				values.add(StringUtils.getString(elem.getPk()));
			}

			cbDepartment.fill(labels, values);
		}

		tabPanel.selectTab(0);
		initListeners();
	}

	private void initListeners() {
		synchronized (this) {
			if (!listenersInitialized) {
				btnPrevious.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						previousAction();
					}
				});

				btnNext.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						nextAction();
					}
				});

				btnEdit.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						editAction();
					}
				});

				btnNew.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						newAction();
					}
				});
				//
				// btnDelete.addClickHandler(new ClickHandler() {
				// public void onClick(ClickEvent event) {
				// deleteAction();
				// }
				// });
				//
				// btnSave.addClickHandler(new ClickHandler() {
				// public void onClick(ClickEvent event) {
				// saveAction();
				// }
				// });

				btnCancel.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						cancelAction();
					}
				});

				listenersInitialized = true;
			}
		}
	}

	public void setData(CompetenceCourseDTO data) {
		// Set values
		if (data == null) {
			data = CompetenceCourseDTO.create();
		}
		model = data;

		if (data.getVersions() != null && data.getVersions().size() > 0) {
			maxVersions = data.getVersions().size();
			selectedIndex = 0;
		} else {
			CompetenceCourseInformationDTO competenceCourseInformation;
			competenceCourseInformation = CompetenceCourseInformationDTO
					.create();
			competenceCourseInformation.setCompetenceCourse(data);
			data.getVersions().add(competenceCourseInformation);
			selectedIndex = 0;
		}

		WidgetUtils.setString(cbDepartment, data.getDepartment().getPk());
		setIdentification(data);
		setCompetenceCourseValidity(data);
		refreshCompetenceCourseInformation();
	}

	private void refreshCompetenceCourseInformation() {
		if (model != null) {
			// Model is null when this method is called by the view after
			// creating an instance of this component
			CompetenceCourseInformationDTO competenceCourseInformation = model
					.getVersions().get(selectedIndex);
			refreshCompetenceCourseInformation(competenceCourseInformation);
		}
	}

	private void refreshCompetenceCourseInformation(
			CompetenceCourseInformationDTO competenceCourseInformation) {
		if (competenceCourseInformation == null) {
			competenceCourseInformation = CompetenceCourseInformationDTO
					.create();
		}

		setBibliography(competenceCourseInformation.getBibliography());
		setCourseDetail(competenceCourseInformation.getCourseDetail());
		setCourseLoad(competenceCourseInformation.getCourseLoad());
		setVersionValidity(competenceCourseInformation);

		refreshNavigationButtons();
	}

	private void setIdentification(CompetenceCourseDTO data) {
		WidgetUtils.setString(tfCode, data.getCode());
		WidgetUtils.setString(tfName, data.getName());
	}

	private void setBibliography(BibliographyDTO data) {
		WidgetUtils.setString(tfBasicBibliography, data.getBasic());
		WidgetUtils.setString(tfExtraBibliography, data.getExtra());
	}

	private void setCourseDetail(CourseDetailDTO data) {
		WidgetUtils.setString(tfPrerequisites, data.getPrerequisites());
		WidgetUtils.setString(tfObjectives, data.getObjectives());
		WidgetUtils.setString(tfProgram, data.getProgram());
		WidgetUtils.setString(tfEvaluationMethod, data.getEvaluationMethod());
		WidgetUtils.setString(tfTeachingMethod, data.getTeachingMethod());
		WidgetUtils.setString(tfNotes, data.getNotes());
		WidgetUtils.setDecimal(tfEcts, data.getEcts());
		WidgetUtils.setString(cbCurricularPeriodType,
				data.getCurricularPeriodType());
	}

	private void setCourseLoad(CourseLoadDTO data) {
		WidgetUtils.setDecimal(tfTheoretical, data.getTheoretical());
		WidgetUtils.setDecimal(tfProblem, data.getProblem());
		WidgetUtils.setDecimal(tfLaboratorial, data.getLaboratorial());
		WidgetUtils.setDecimal(tfTraining, data.getTraining());
		WidgetUtils.setDecimal(tfTutorialOrientation,
				data.getTutorialOrientation());
		WidgetUtils.setDecimal(tfAutonomousWork, data.getAutonomousWork());
		WidgetUtils.setDecimal(tfFieldWork, data.getFieldWork());
		WidgetUtils.setDecimal(tfOthers, data.getOthers());
	}

	private void setVersionValidity(CompetenceCourseInformationDTO data) {
		WidgetUtils.setDate(tfBeginDate, data.getBeginDate());
	}

	private void setCompetenceCourseValidity(CompetenceCourseDTO data) {
		WidgetUtils.setDate(tfEndDate, data.getEndDate());
	}

	// [end] - Set data

	// [start] - Get data
	public CompetenceCourseDTO getData() {
		// Build DTOs
		CompetenceCourseDTO competenceCourse = CompetenceCourseDTO.create();
		CompetenceCourseInformationDTO competenceCourseInformation = CompetenceCourseInformationDTO
				.create();
		competenceCourse.getVersions().add(competenceCourseInformation);

		// Retrieve data
		competenceCourse.getDepartment().setPk(
				WidgetUtils.getString(cbDepartment));
		buildIdentification(competenceCourse);
		buildCompetenceCourseValidity(competenceCourse);
		buildBibliography(competenceCourseInformation.getBibliography());
		buildCourseDetail(competenceCourseInformation.getCourseDetail());
		buildCourseLoad(competenceCourseInformation.getCourseLoad());
		buildVersionValidity(competenceCourseInformation);

		return competenceCourse;
	}

	public CompetenceCourseDTO getCompetenceCourseData() {
		// Build DTOs
		CompetenceCourseDTO competenceCourse = CompetenceCourseDTO.create();
		CompetenceCourseInformationDTO competenceCourseInformation = CompetenceCourseInformationDTO
				.create();
		competenceCourse.getVersions().add(competenceCourseInformation);

		// Retrieve data
		competenceCourse.getDepartment().setPk(
				WidgetUtils.getString(cbDepartment));
		buildIdentification(competenceCourse);

		return competenceCourse;
	}

	public CompetenceCourseInformationDTO getCompetenceCourseInformationData() {
		// Build DTOs
		CompetenceCourseDTO competenceCourse = getData();
		CompetenceCourseInformationDTO competenceCourseInformation = competenceCourse
				.getVersions().get(0);

		if (mode != ComponentMode.NEW) {
			// Get PK of the selected competence course information
			competenceCourseInformation.setPk(model.getVersions()
					.get(selectedIndex).getPk());
		}

		return competenceCourseInformation;
	}

	private void buildIdentification(CompetenceCourseDTO data) {
		data.setCode(WidgetUtils.getString(tfCode));
		data.setName(WidgetUtils.getString(tfName));
	}

	private void buildBibliography(BibliographyDTO data) {
		data.setBasic(WidgetUtils.getString(tfBasicBibliography));
		data.setExtra(WidgetUtils.getString(tfExtraBibliography));
	}

	private void buildCourseDetail(CourseDetailDTO data) {
		data.setPrerequisites(WidgetUtils.getString(tfPrerequisites));
		data.setObjectives(WidgetUtils.getString(tfObjectives));
		data.setProgram(WidgetUtils.getString(tfProgram));
		data.setEvaluationMethod(WidgetUtils.getString(tfEvaluationMethod));
		data.setTeachingMethod(WidgetUtils.getString(tfTeachingMethod));
		data.setNotes(WidgetUtils.getString(tfNotes));
		data.setEcts(WidgetUtils.getDecimal(tfEcts));
		data.setCurricularPeriodType(WidgetUtils.getString(
				CurricularPeriodType.class, cbCurricularPeriodType));
	}

	private void buildCourseLoad(CourseLoadDTO data) {
		data.setTheoretical(WidgetUtils.getDecimal(tfTheoretical));
		data.setProblem(WidgetUtils.getDecimal(tfProblem));
		data.setLaboratorial(WidgetUtils.getDecimal(tfLaboratorial));
		data.setTraining(WidgetUtils.getDecimal(tfTraining));
		data.setTutorialOrientation(WidgetUtils
				.getDecimal(tfTutorialOrientation));
		data.setAutonomousWork(WidgetUtils.getDecimal(tfAutonomousWork));
		data.setFieldWork(WidgetUtils.getDecimal(tfFieldWork));
		data.setOthers(WidgetUtils.getDecimal(tfOthers));
	}

	private void buildCompetenceCourseValidity(CompetenceCourseDTO data) {
		data.setEndDate(WidgetUtils.getDate(tfEndDate));
	}

	private void buildVersionValidity(CompetenceCourseInformationDTO data) {
		data.setBeginDate(WidgetUtils.getDate(tfBeginDate));
	}

	// [end] - Get data

	// [start] - Create view
	public CompetenceCourseDataComponent() {
		super();

		// Header
		VerticalPanel headerPanel = new VerticalPanel();
		headerPanel.setWidth("100%");
		addHeader(headerPanel);
		contentPanel.add(headerPanel);

		// Body
		tabPanel = new TabPanel();
		contentPanel.add(tabPanel);
		tabPanel.setWidth("100%");

		// ---------------------
		VerticalPanel verticalCourseDetail1of3 = new VerticalPanel();
		tabPanel.add(verticalCourseDetail1of3,
				globalConstants.tabCourseDetail1of3_text(), false);
		addCourseDetail1of3(verticalCourseDetail1of3);

		VerticalPanel verticalCourseDetail2of3 = new VerticalPanel();
		tabPanel.add(verticalCourseDetail2of3,
				globalConstants.tabCourseDetail2of3_text(), false);
		addCourseDetail2of3(verticalCourseDetail2of3);

		VerticalPanel verticalCourseDetail3of3 = new VerticalPanel();
		tabPanel.add(verticalCourseDetail3of3,
				globalConstants.tabCourseDetail3of3_text(), false);
		addCourseDetail3of3(verticalCourseDetail3of3);

		VerticalPanel verticalPanelCourseLoad = new VerticalPanel();
		tabPanel.add(verticalPanelCourseLoad,
				globalConstants.tabCourseLoad_text(), false);
		addCourseLoadComponents(verticalPanelCourseLoad);

		VerticalPanel verticalPanelBibliography = new VerticalPanel();
		tabPanel.add(verticalPanelBibliography,
				globalConstants.tabBibliography_text(), false);
		addBibliographyComponents(verticalPanelBibliography);

		// Select 1st tab
		tabPanel.selectTab(0);
		// ---------------------

		// Footer
		HorizontalPanel panelFooter = new HorizontalPanel();
		contentPanel.add(panelFooter);
		panelFooter.setSpacing(0);

		panelNavigation = new HorizontalPanel();
		panelFooter.add(panelNavigation);
		panelNavigation.setSpacing(5);

		panelNavigation.add(btnPrevious);
		btnPrevious.setText(buttonConstants.btnPrevious_text());
		btnPrevious.setStyleName(WidgetUtils.BUTTON_STYLE);
		btnPrevious.addStyleName(WidgetUtils.ICON_PREVIOUS_STYLE);

		panelNavigation.add(btnNext);
		btnNext.setText(buttonConstants.btnNext_text());
		btnNext.setStyleName(WidgetUtils.BUTTON_STYLE);
		btnNext.addStyleName(WidgetUtils.ICON_NEXT_STYLE);

		panelNavigation.add(new Label("  "));

		panelActions = new HorizontalPanel();
		panelFooter.add(panelActions);
		panelActions.setSpacing(5);

		panelActions.add(btnNew);
		btnNew.setText(buttonConstants.btnCreate_text());
		btnNew.setStyleName(WidgetUtils.BUTTON_STYLE);
		btnNew.addStyleName(WidgetUtils.ICON_NEW_STYLE);

		panelActions.add(btnDelete);
		btnDelete.setText(buttonConstants.btnDelete_text());
		btnDelete.setStyleName(WidgetUtils.BUTTON_STYLE);
		btnDelete.addStyleName(WidgetUtils.ICON_DELETE_STYLE);

		panelActions.add(btnEdit);
		btnEdit.setText(buttonConstants.btnEdit_text());
		btnEdit.setStyleName(WidgetUtils.BUTTON_STYLE);
		btnEdit.addStyleName(WidgetUtils.ICON_EDIT_STYLE);

		panelActions.add(btnSave);
		btnSave.setText(buttonConstants.btnSave_text());
		btnSave.setStyleName(WidgetUtils.BUTTON_STYLE);
		btnSave.addStyleName(WidgetUtils.ICON_CONFIRM_STYLE);

		panelActions.add(btnCancel);
		btnCancel.setText(buttonConstants.btnAbort_text());
		btnCancel.setStyleName(WidgetUtils.BUTTON_STYLE);
		btnCancel.addStyleName(WidgetUtils.ICON_ABORT_STYLE);
	}

	private void addHeader(VerticalPanel verticalPanel) {
		// Row 1
		tfCode = new StringTextField(globalConstants.lblCode_text());
		tfCode.setRequired(true);

		tfName = new StringTextField(globalConstants.lblName_text());
		tfName.setRequired(true);

		HorizontalPanel row1 = new HorizontalPanel();
		row1.add(tfCode);
		row1.add(new FieldSeparator());
		row1.add(tfName);

		// Row 2
		cbDepartment = new ComboField(globalConstants.lblDepartment_text());
		cbDepartment.setRequired(true);

		HorizontalPanel row2 = new HorizontalPanel();
		row2.add(cbDepartment);

		// Row 3
		tfBeginDate = new DateTextField(globalConstants.lblBeginDate_text());
		tfBeginDate.setRequired(true);
		tfBeginDate.setFormat(new DefaultFormat(DateTimeFormat
				.getFormat(WidgetUtils.DATE_FORMAT)));

		tfEndDate = new DateTextField(globalConstants.lblEndDate_text());
		tfEndDate.setRequired(false);
		tfEndDate.setFormat(new DefaultFormat(DateTimeFormat
				.getFormat(WidgetUtils.DATE_FORMAT)));

		HorizontalPanel row3 = new HorizontalPanel();
		row3.add(tfBeginDate);
		row3.add(new FieldSeparator());
		row3.add(tfEndDate);

		// Add rows
		verticalPanel.add(row1);
		verticalPanel.add(row2);
		verticalPanel.add(row3);
	}

	private void addCourseDetail1of3(VerticalPanel verticalPanel) {
		// Row 1
		tfEcts = new DecimalTextField(globalConstants.lblEcts_text());
		tfEcts.setRequired(true);
		setDigits(tfEcts);

		cbCurricularPeriodType = new ComboField(
				globalConstants.lblCurricularPeriodType_text());
		cbCurricularPeriodType.setRequired(true);

		HorizontalPanel row1 = new HorizontalPanel();
		row1.add(tfEcts);
		row1.add(new FieldSeparator());
		row1.add(cbCurricularPeriodType);

		// Row 2
		tfPrerequisites = new MultiLineStringField(
				globalConstants.lblPrerequisites_text());
		tfPrerequisites.setRequired(true);
		tfPrerequisites.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row2 = new HorizontalPanel();
		row2.add(tfPrerequisites);

		// Row 3
		tfNotes = new MultiLineStringField(globalConstants.lblNotes_text());
		tfNotes.setRequired(false);
		tfNotes.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row3 = new HorizontalPanel();
		row3.add(tfNotes);

		// Add rows
		verticalPanel.add(row1);
		verticalPanel.add(row2);
		verticalPanel.add(row3);
	}

	private void addCourseDetail2of3(VerticalPanel verticalPanel) {
		// Row 1
		tfObjectives = new MultiLineStringField(
				globalConstants.lblObjectives_text());
		tfObjectives.setRequired(true);
		tfObjectives.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row1 = new HorizontalPanel();
		row1.add(tfObjectives);

		// Row 2
		tfProgram = new MultiLineStringField(globalConstants.lblProgram_text());
		tfProgram.setRequired(true);
		tfProgram.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row2 = new HorizontalPanel();
		row2.add(tfProgram);

		// Add rows
		verticalPanel.add(row1);
		verticalPanel.add(row2);
	}

	private void addCourseDetail3of3(VerticalPanel verticalPanel) {
		// Row 1
		tfEvaluationMethod = new MultiLineStringField(
				globalConstants.lblEvaluationMethod_text());
		tfEvaluationMethod.setRequired(true);
		tfEvaluationMethod.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row1 = new HorizontalPanel();
		row1.add(tfEvaluationMethod);

		// Row 2
		tfTeachingMethod = new MultiLineStringField(
				globalConstants.lblTeachingMethod_text());
		tfTeachingMethod.setRequired(true);
		tfTeachingMethod.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row2 = new HorizontalPanel();
		row2.add(tfTeachingMethod);

		// Add rows
		verticalPanel.add(row1);
		verticalPanel.add(row2);
	}

	private void addCourseLoadComponents(VerticalPanel verticalPanel) {
		// Row 1
		tfTheoretical = new DecimalTextField(
				globalConstants.lblTheoretical_text());
		tfTheoretical.setRequired(true);
		setDigits(tfTheoretical);

		tfProblem = new DecimalTextField(globalConstants.lblProblem_text());
		tfProblem.setRequired(true);
		setDigits(tfProblem);

		HorizontalPanel row1 = new HorizontalPanel();
		row1.add(tfTheoretical);
		row1.add(new FieldSeparator());
		row1.add(tfProblem);

		// Row 2
		tfLaboratorial = new DecimalTextField(
				globalConstants.lblLaboratorial_text());
		tfLaboratorial.setRequired(true);
		setDigits(tfLaboratorial);

		tfTraining = new DecimalTextField(globalConstants.lblTraining_text());
		tfTraining.setRequired(true);
		setDigits(tfTraining);

		HorizontalPanel row2 = new HorizontalPanel();
		row2.add(tfLaboratorial);
		row2.add(new FieldSeparator());
		row2.add(tfTraining);

		// Row 3
		tfTutorialOrientation = new DecimalTextField(
				globalConstants.lblTutorialOrientation_text());
		tfTutorialOrientation.setRequired(true);
		setDigits(tfTutorialOrientation);

		tfAutonomousWork = new DecimalTextField(
				globalConstants.lblAutonomousWork_text());
		tfAutonomousWork.setRequired(true);
		setDigits(tfAutonomousWork);

		HorizontalPanel row3 = new HorizontalPanel();
		row3.add(tfTutorialOrientation);
		row3.add(new FieldSeparator());
		row3.add(tfAutonomousWork);

		// Row 4
		tfFieldWork = new DecimalTextField(globalConstants.lblFieldWork_text());
		tfFieldWork.setRequired(true);
		setDigits(tfFieldWork);

		tfOthers = new DecimalTextField(globalConstants.lblOthers_text());
		tfOthers.setRequired(true);
		setDigits(tfOthers);

		HorizontalPanel row4 = new HorizontalPanel();
		row4.add(tfFieldWork);
		row4.add(new FieldSeparator());
		row4.add(tfOthers);

		// Add rows
		verticalPanel.add(row1);
		verticalPanel.add(row2);
		verticalPanel.add(row3);
		verticalPanel.add(row4);
	}

	private void addBibliographyComponents(VerticalPanel verticalPanel) {
		// Row 1
		tfBasicBibliography = new MultiLineStringField(
				globalConstants.lblBasicBibliography_text());
		tfBasicBibliography.setRequired(true);
		tfBasicBibliography.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row1 = new HorizontalPanel();
		row1.add(tfBasicBibliography);

		// Row 2
		tfExtraBibliography = new MultiLineStringField(
				globalConstants.lblExtraBibliography_text());
		tfExtraBibliography.setRequired(false);
		tfExtraBibliography.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row2 = new HorizontalPanel();
		row2.add(tfExtraBibliography);

		// Add rows
		verticalPanel.add(row1);
		verticalPanel.add(row2);
	}

	public void setReadOnly(boolean readOnly) {
		setReadOnlyIdentification(readOnly);
		setReadOnlyCompetenceCourseValidity(readOnly);
		setReadOnlyCompetenceCourseInformation(readOnly);
	}

	public void setReadOnlyForEditCompetenceCourse() {
		setReadOnlyIdentification(false);
		cbDepartment.setReadOnly(true);
		setReadOnlyCompetenceCourseValidity(false);
		setReadOnlyCompetenceCourseInformation(true);
	}

	private void setReadOnlyCompetenceCourseInformation(boolean readOnly) {
		setReadOnlyVersionValidity(readOnly);
		setReadOnlyTabBibliography(readOnly);
		setReadOnlyTabCourseDetail(readOnly);
		setReadOnlyTabCourseLoad(readOnly);
	}

	// private void setReadOnlyHeader(boolean readOnly) {
	// setReadOnlyIdentification(readOnly);
	// setReadOnlyValidity(readOnly);
	// }

	private void setReadOnlyIdentification(boolean readOnly) {
		tfCode.setReadOnly(readOnly);
		tfName.setReadOnly(readOnly);
		cbDepartment.setReadOnly(readOnly);
	}

	private void setReadOnlyCompetenceCourseValidity(boolean readOnly) {
		tfEndDate.setReadOnly(readOnly);
	}

	private void setReadOnlyVersionValidity(boolean readOnly) {
		tfBeginDate.setReadOnly(readOnly);
	}

	private void setReadOnlyTabBibliography(boolean readOnly) {
		tfBasicBibliography.setReadOnly(readOnly);
		tfExtraBibliography.setReadOnly(readOnly);
	}

	private void setReadOnlyTabCourseDetail(boolean readOnly) {
		tfPrerequisites.setReadOnly(readOnly);
		tfObjectives.setReadOnly(readOnly);
		tfProgram.setReadOnly(readOnly);
		tfEvaluationMethod.setReadOnly(readOnly);
		tfTeachingMethod.setReadOnly(readOnly);
		tfNotes.setReadOnly(readOnly);
		tfEcts.setReadOnly(readOnly);
		cbCurricularPeriodType.setReadOnly(readOnly);
	}

	private void setReadOnlyTabCourseLoad(boolean readOnly) {
		tfTheoretical.setReadOnly(readOnly);
		tfProblem.setReadOnly(readOnly);
		tfLaboratorial.setReadOnly(readOnly);
		tfTraining.setReadOnly(readOnly);
		tfTutorialOrientation.setReadOnly(readOnly);
		tfAutonomousWork.setReadOnly(readOnly);
		tfFieldWork.setReadOnly(readOnly);
		tfOthers.setReadOnly(readOnly);
	}

	private void setDigits(DecimalTextField field) {
		field.setIntegerDigits(3);
		field.setDecimalDigits(1);
	}

	// [end] - Component methods

	// [start] - Individual read only
	// Not required yet
	// [end] - Individual read only

	// [start] - Events
	private void previousAction() {
		if (selectedIndex > 0) {
			selectedIndex--;
		}

		refreshCompetenceCourseInformation();
	}

	private void nextAction() {
		if (selectedIndex < maxVersions - 1) {
			selectedIndex++;
		}

		refreshCompetenceCourseInformation();
	}

	private void editAction() {
		changeMode(ComponentMode.EDIT);
	}

	private void newAction() {
		changeMode(ComponentMode.NEW);
	}

	private void cancelAction() {
		changeMode(ComponentMode.READONLY);
	}

	// [end] - Events

	private void refreshNavigationButtons() {
		{
			boolean enable = selectedIndex < maxVersions - 1;
			btnNext.setEnabled(enable);
		}
		{
			boolean enable = selectedIndex > 0;
			btnPrevious.setEnabled(enable);
		}
	}

	public void changeMode(ComponentMode mode) {
		this.mode = mode;

		switch (mode) {
		case EDIT: {
			setReadOnlyCompetenceCourseInformation(false);

			btnPrevious.setEnabled(false);
			btnNext.setEnabled(false);
			btnSave.setEnabled(true);
			btnCancel.setEnabled(true);
			btnDelete.setEnabled(false);
			btnNew.setEnabled(false);
		}
			;
			break;

		case NAVIGATION: {
			panelActions.setVisible(false);
		}
			;
			break;

		case NEW: {
			setReadOnlyCompetenceCourseInformation(false);
			refreshCompetenceCourseInformation(null);

			btnPrevious.setEnabled(false);
			btnNext.setEnabled(false);
			btnSave.setEnabled(true);
			btnCancel.setEnabled(true);
			btnDelete.setEnabled(false);
			btnEdit.setEnabled(false);
		}
			;
			break;

		case READONLY: {
			panelActions.setVisible(true);
			setReadOnlyCompetenceCourseInformation(true);
			refreshCompetenceCourseInformation();

			btnSave.setEnabled(false);
			btnCancel.setEnabled(false);
			btnDelete.setEnabled(true);
			btnEdit.setEnabled(true);
			btnNew.setEnabled(true);
		}
			;
			break;

		}
	}
}

