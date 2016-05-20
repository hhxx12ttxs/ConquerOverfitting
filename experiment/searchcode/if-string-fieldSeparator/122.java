package pt.ips.estsetubal.mig.academicCloud.client.components.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.ips.estsetubal.mig.academicCloud.client.components.WidgetSize;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.ComboField;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.FieldSeparator;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.StringTextField;
import pt.ips.estsetubal.mig.academicCloud.client.components.utils.StringUtils;
import pt.ips.estsetubal.mig.academicCloud.client.components.utils.WidgetUtils;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.degree.DegreeDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.degree.SchoolDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.enums.degree.DegreeType;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This component manages the degree identification data.
 * 
 * @author Ant?nio Casqueiro
 */
public class DegreeIdentificationDataComponent extends AbstractComponent {

	// [start] - Components
	private ComboField cbSchool = null;

	private ComboField cbType = null;
	private StringTextField tfCode = null;

	private StringTextField tfName = null;

	private StringTextField tfAcronym = null;

	// [end] - Components

	// [start] - Set data
	public void init(DegreeDTO data) {
		init(Arrays.asList(data.getSchool()));
	}

	public void init(List<SchoolDTO> data) {
		// Fill combo boxes
		WidgetUtils.fill(cbType, DegreeType.class, enumTranslator);

		{
			List<String> values = new ArrayList<String>();
			List<String> labels = new ArrayList<String>();

			for (SchoolDTO school : data) {
				labels.add(school.getName());
				values.add(StringUtils.getString(school.getPk()));
			}

			cbSchool.fill(labels, values);
		}
	}

	public void setData(DegreeDTO data) {
		// Set values
		if (data == null) {
			data = DegreeDTO.create();
		}

		WidgetUtils.setString(cbSchool, data.getSchool().getPk());

		WidgetUtils.setString(cbType, data.getDegreeType());
		WidgetUtils.setString(tfCode, data.getCode());

		WidgetUtils.setString(tfName, data.getName());

		WidgetUtils.setString(tfAcronym, data.getAcronym());
	}

	// [end] - Set data

	// [start] - Get data
	public DegreeDTO getData() {
		// Build DTOs
		DegreeDTO data = DegreeDTO.create();

		// Retrieve data
		data.getSchool().setPk(WidgetUtils.getString(cbSchool));

		data.setDegreeType(WidgetUtils.getString(DegreeType.class, cbType));
		data.setCode(WidgetUtils.getString(tfCode));
		data.setName(WidgetUtils.getString(tfName));
		data.setAcronym(WidgetUtils.getString(tfAcronym));

		return data;
	}

	// [end] - Get data

	// [start] - Create view
	public DegreeIdentificationDataComponent() {
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
		cbSchool = new ComboField(globalConstants.lblSchool_text());
		cbSchool.setRequired(true);
		cbSchool.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row1 = new HorizontalPanel();
		row1.add(cbSchool);

		// Row 2
		cbType = new ComboField(globalConstants.lblDegreeType_text());
		cbType.setRequired(true);

		tfCode = new StringTextField(globalConstants.lblCode_text());
		tfCode.setRequired(true);

		HorizontalPanel row2 = new HorizontalPanel();
		row2.add(cbType);
		row2.add(new FieldSeparator());
		row2.add(tfCode);

		// Row 3
		tfName = new StringTextField(globalConstants.lblDegreeName_text());
		tfName.setRequired(true);
		tfName.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row3 = new HorizontalPanel();
		row3.add(tfName);

		// Row 4
		tfAcronym = new StringTextField(globalConstants.lblAcronym_text());
		tfAcronym.setRequired(true);

		HorizontalPanel row4 = new HorizontalPanel();
		row4.add(tfAcronym);

		// Add rows
		verticalPanel.add(row1);
		verticalPanel.add(row2);
		verticalPanel.add(row3);
		verticalPanel.add(row4);
	}

	public void setReadOnly(boolean readOnly) {
		tfAcronym.setReadOnly(readOnly);
		tfCode.setReadOnly(readOnly);
		tfName.setReadOnly(readOnly);
		cbType.setReadOnly(readOnly);
		cbSchool.setReadOnly(readOnly);
	}

	public void setReadOnlyForEdit() {
		setReadOnly(false);
		cbSchool.setReadOnly(true);
	}
	// [end] - Component methods

	// [start] - Individual read only
	// Not required yet
	// [end] - Individual read only

}

