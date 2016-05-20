package pt.ips.estsetubal.mig.academicCloud.client.components.blocks;

import java.util.ArrayList;
import java.util.List;

import pt.ips.estsetubal.mig.academicCloud.client.components.WidgetSize;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.ComboField;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.DateTextField;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.FieldSeparator;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.IntegerTextField;
import pt.ips.estsetubal.mig.academicCloud.client.components.utils.StringUtils;
import pt.ips.estsetubal.mig.academicCloud.client.components.utils.WidgetUtils;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.degree.CompetenceCourseDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.degree.CurricularCourseDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.degree.DepartmentDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.enums.degree.CurricularCourseType;

import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

/**
 * This component manages the curricular course identification data.
 * 
 * @author Ant?nio Casqueiro
 */
public class CurricularCourseIdentificationDataComponent extends
		AbstractComponent {

	// [start] - Components
	private ComboField cbDepartment = null;

	private ComboField cbName = null;

	private IntegerTextField tfCurricularYear = null;
	private IntegerTextField tfCurricularSemester = null;

	private DateTextField tfBeginDate = null;
	private DateTextField tfEndDate = null;

	private ComboField cbCurricularCourseType = null;

	// [end] - Components

	// [start] - Get components
	public HasChangeHandlers getDepartmentCombo() {
		return cbDepartment;
	}

	// [end] - Get components

	// [start] - Set data
	public void init(List<DepartmentDTO> data) {
		// Fill combo boxes
		WidgetUtils.fill(cbCurricularCourseType, CurricularCourseType.class,
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
	}

	public void setDataUpdateCompetenceCourses(DepartmentDTO data) {
		{
			List<String> values = new ArrayList<String>();
			List<String> labels = new ArrayList<String>();

			for (CompetenceCourseDTO elem : data.getCompetenceCourses()) {
				labels.add(elem.getName() + " - [" + elem.getCode() + "]");
				values.add(StringUtils.getString(elem.getPk()));
			}

			cbName.fill(labels, values);
		}
	}

	public void setData(CurricularCourseDTO data) {
		// Set values
		if (data == null) {
			data = new CurricularCourseDTO();
		}

		if (data.getCompetenceCourse() != null) {
			WidgetUtils.setString(cbDepartment, data.getCompetenceCourse()
					.getDepartment().getPk());

			WidgetUtils.setString(cbName, data.getCompetenceCourse().getPk());
		} else {
			WidgetUtils.setString(cbDepartment, "");
			WidgetUtils.setString(cbName, "");
		}

		WidgetUtils.setInteger(tfCurricularYear, data.getCurricularYear());
		WidgetUtils
				.setInteger(tfCurricularSemester, data.getCurricularPeriod());

		WidgetUtils.setDate(tfBeginDate, data.getBeginDate());
		WidgetUtils.setDate(tfEndDate, data.getEndDate());

		WidgetUtils.setString(cbCurricularCourseType,
				data.getCurricularCourseType());
	}

	// [end] - Set data

	// [start] - Get data
	public CurricularCourseDTO getData() {
		// Build DTOs
		CurricularCourseDTO data = new CurricularCourseDTO();

		CompetenceCourseDTO competenceCourse = new CompetenceCourseDTO();
		competenceCourse.setPk(WidgetUtils.getString(cbName));

		// Retrieve data
		data.setCompetenceCourse(competenceCourse);

		data.setCurricularYear(WidgetUtils.getInteger(tfCurricularYear));
		data.setCurricularPeriod(WidgetUtils.getInteger(tfCurricularSemester));

		data.setBeginDate(WidgetUtils.getDate(tfBeginDate));
		data.setEndDate(WidgetUtils.getDate(tfEndDate));

		data.setCurricularCourseType(WidgetUtils.getString(
				CurricularCourseType.class, cbCurricularCourseType));

		return data;
	}

	public DepartmentDTO getSelectedDepartment() {
		DepartmentDTO data = new DepartmentDTO();

		data.setPk(WidgetUtils.getString(cbDepartment));

		return data;
	}

	// [end] - Get data

	// [start] - Create view
	public CurricularCourseIdentificationDataComponent() {
		super();

		// Header
		// None yet

		// Body
		VerticalPanel verticalPanelIdentification = new VerticalPanel();
		contentPanel.add(verticalPanelIdentification);
		addIdentificationComponents(verticalPanelIdentification);

		// Footer
		// None yet
	}

	private void addIdentificationComponents(VerticalPanel verticalPanel) {
		// Row 1
		cbDepartment = new ComboField(globalConstants.lblDepartment_text());
		cbDepartment.setRequired(false);
		cbDepartment.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row1 = new HorizontalPanel();
		row1.add(cbDepartment);

		// Row 2
		cbName = new ComboField(globalConstants.lblName_text());
		cbName.setRequired(true);
		cbName.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row2 = new HorizontalPanel();
		row2.add(cbName);

		// Row 3
		tfCurricularYear = new IntegerTextField(
				globalConstants.lblCurricularYear_text());
		tfCurricularYear.setRequired(true);

		tfCurricularSemester = new IntegerTextField(
				globalConstants.lblCurricularPeriod_text());
		tfCurricularSemester.setRequired(true);

		HorizontalPanel row3 = new HorizontalPanel();
		row3.add(tfCurricularYear);
		row3.add(new FieldSeparator());
		row3.add(tfCurricularSemester);

		// Row 4
		tfBeginDate = new DateTextField(globalConstants.lblBeginDate_text());
		tfBeginDate.setRequired(true);
		tfBeginDate.setFormat(new DefaultFormat(DateTimeFormat
				.getFormat(WidgetUtils.DATE_FORMAT)));

		tfEndDate = new DateTextField(globalConstants.lblEndDate_text());
		tfEndDate.setRequired(false);
		tfEndDate.setFormat(new DefaultFormat(DateTimeFormat
				.getFormat(WidgetUtils.DATE_FORMAT)));

		HorizontalPanel row4 = new HorizontalPanel();
		row4.add(tfBeginDate);
		row4.add(new FieldSeparator());
		row4.add(tfEndDate);

		// Row 5
		cbCurricularCourseType = new ComboField(globalConstants.lblCurricularCourseType_text());
		cbCurricularCourseType.setRequired(true);

		HorizontalPanel row5 = new HorizontalPanel();
		row5.add(cbCurricularCourseType);

		// Add rows
		verticalPanel.add(row1);
		verticalPanel.add(row2);
		verticalPanel.add(row3);
		verticalPanel.add(row4);
		verticalPanel.add(row5);
	}

	public void setReadOnly(boolean readOnly) {
		cbDepartment.setReadOnly(readOnly);

		cbName.setReadOnly(readOnly);

		tfCurricularYear.setReadOnly(readOnly);
		tfCurricularSemester.setReadOnly(readOnly);

		tfBeginDate.setReadOnly(readOnly);
		tfEndDate.setReadOnly(readOnly);
		
		cbCurricularCourseType.setReadOnly(readOnly);
	}

	public void setReadOnlyForEdit() {
		setReadOnly(false);

		cbDepartment.setReadOnly(true);

		cbName.setReadOnly(true);
	}

	// [end] - Component methods

	// [start] - Individual read only
	// Not required yet
	// [end] - Individual read only

}

