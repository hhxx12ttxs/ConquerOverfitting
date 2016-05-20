package pt.ips.estsetubal.mig.academicCloud.client.components.blocks;

import java.util.ArrayList;
import java.util.List;

import pt.ips.estsetubal.mig.academicCloud.client.components.WidgetSize;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.ComboField;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.DateTextField;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.FieldSeparator;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.IntegerTextField;
import pt.ips.estsetubal.mig.academicCloud.client.components.fields.StringTextField;
import pt.ips.estsetubal.mig.academicCloud.client.components.utils.WidgetUtils;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.param.CountryDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.user.PersonDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.user.UserDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.user.embeddable.NonPortugueseAddressContactDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.dto.domain.user.embeddable.PortugueseAddressContactDTO;
import pt.ips.estsetubal.mig.academicCloud.shared.enums.user.IdDocumentType;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

/**
 * This component manages the user personal data.
 * 
 * @author Ant?nio Casqueiro
 */
public class UserDataComponent extends AbstractComponent {

	// [start] - Components
	private TabPanel tabPanel;

	// ---------------------------------------------------------------
	private StringTextField tfGivenNames = null;
	private StringTextField tfFamilyNames = null;
	private StringTextField tfNickname = null;

	private StringTextField tfIdDocumentNumber = null;
	private ComboField cbIdDocumentType = null;
	private IntegerTextField tfSocialSecurityNumber = null;

	private DateTextField tfDateOfBirth = null;
	private StringTextField tfTownOfBirth = null;

	private ComboField cbCountryOfBirth = null;
	private ComboField cbCountryOfResidence = null;

	private StringTextField tfFatherName = null;
	private StringTextField tfMotherName = null;

	private StringTextField tfStreet = null;
	private StringTextField tfPostalCodeMain = null;
	private StringTextField tfPostalCodeArea = null;
	private StringTextField tfArea = null;

	private StringTextField tfAddressLine1 = null;
	private StringTextField tfAddressLine2 = null;
	private StringTextField tfAddressLine3 = null;

	private StringTextField tfPrivateEmailContact = null;
	private StringTextField tfInstitutionalEmailContact = null;

	// [end] - Components

	// [start] - Set data
	public void init(List<CountryDTO> data) {
		// Fill combo boxes
		WidgetUtils
				.fill(cbIdDocumentType, IdDocumentType.class, enumTranslator);

		{
			List<String> values = new ArrayList<String>();
			List<String> labels = new ArrayList<String>();

			for (CountryDTO country : data) {
				labels.add(countryTranslator.getString(country.getCode()));
				values.add(country.getCode());
			}

			cbCountryOfBirth.fill(labels, values);
		}

		{
			List<String> values = new ArrayList<String>();
			List<String> labels = new ArrayList<String>();

			for (CountryDTO country : data) {
				labels.add(countryTranslator.getString(country.getCode()));
				values.add(country.getCode());
			}

			cbCountryOfResidence.fill(labels, values);
		}

		tabPanel.selectTab(0);
	}

	public void setData(UserDTO data) {
		// Set values
		if (data == null) {
			data = UserDTO.create();
		}
		final PersonDTO person = data.getPerson();

		setContacts(person);
		setIdentification(person);
		setNonPortugueseAddress(person);
		setParents(person);
		setPortugueseAddress(person);
	}

	private void setContacts(PersonDTO data) {
		WidgetUtils.setString(tfPrivateEmailContact,
				data.getPrivateEmailContact());
		WidgetUtils.setString(tfInstitutionalEmailContact,
				data.getInstitutionalEmailContact());
	}

	private void setIdentification(PersonDTO data) {
		WidgetUtils.setString(tfGivenNames, data.getGivenNames());
		WidgetUtils.setString(tfFamilyNames, data.getFamilyNames());
		WidgetUtils.setString(tfNickname, data.getNickname());

		WidgetUtils.setString(tfIdDocumentNumber, data.getIdDocumentNumber());
		WidgetUtils.setString(cbIdDocumentType, data.getIdDocumentType());
		WidgetUtils.setInteger(tfSocialSecurityNumber,
				data.getSocialSecurityNumber());

		WidgetUtils.setDate(tfDateOfBirth, data.getBirthData().getBirthDate());
		WidgetUtils.setString(tfTownOfBirth, data.getBirthData().getTown());

		WidgetUtils.setString(cbCountryOfBirth, data.getBirthData()
				.getCountryFCK());
		WidgetUtils.setString(cbCountryOfResidence,
				data.getCountryOfResidenceFCK());
	}

	private void setNonPortugueseAddress(PersonDTO data) {
		// if (!"pt".equals(data.getCountryOfResidenceFCK())) {
		final NonPortugueseAddressContactDTO address = data
				.getNonPortugueseAddressContact();
		WidgetUtils.setString(tfAddressLine1, address.getAddressLine1());
		WidgetUtils.setString(tfAddressLine2, address.getAddressLine2());
		WidgetUtils.setString(tfAddressLine3, address.getAddressLine3());
		// }
	}

	private void setParents(PersonDTO data) {
		WidgetUtils
				.setString(tfFatherName, data.getFiliation().getFatherName());
		WidgetUtils
				.setString(tfMotherName, data.getFiliation().getMotherName());
	}

	private void setPortugueseAddress(PersonDTO data) {
		// if ("pt".equals(data.getCountryOfResidenceFCK())) {
		final PortugueseAddressContactDTO address = data
				.getPortugueseAddressContact();
		WidgetUtils.setString(tfStreet, address.getStreet());
		WidgetUtils.setString(tfPostalCodeMain, address.getPostalCodeMain());
		WidgetUtils.setString(tfPostalCodeArea, address.getPostalCodeArea());
		WidgetUtils.setString(tfArea, address.getArea());
		// }
	}

	// [end] - Set data

	// [start] - Get data
	public UserDTO getData() {
		// Build DTOs
		UserDTO user = UserDTO.create();
		PersonDTO data = user.getPerson();

		// Retrieve data
		buildContacts(data);
		buildIdentification(data);
		buildNonPortugueseAddress(data);
		buildParents(data);
		buildPortugueseAddress(data);

		return user;
	}

	private void buildContacts(PersonDTO data) {
		data.setPrivateEmailContact(WidgetUtils
				.getString(tfPrivateEmailContact));
		data.setInstitutionalEmailContact(WidgetUtils
				.getString(tfInstitutionalEmailContact));
	}

	private void buildIdentification(PersonDTO data) {
		data.setGivenNames(WidgetUtils.getString(tfGivenNames));
		data.setFamilyNames(WidgetUtils.getString(tfFamilyNames));
		data.setNickname(WidgetUtils.getString(tfNickname));

		data.setIdDocumentNumber(WidgetUtils.getString(tfIdDocumentNumber));
		data.setIdDocumentType(WidgetUtils.getString(IdDocumentType.class,
				cbIdDocumentType));
		data.setSocialSecurityNumber(WidgetUtils
				.getInteger(tfSocialSecurityNumber));

		data.getBirthData().setBirthDate(WidgetUtils.getDate(tfDateOfBirth));
		data.getBirthData().setTown(WidgetUtils.getString(tfTownOfBirth));

		data.getBirthData().setCountryFCK(
				WidgetUtils.getString(cbCountryOfBirth));
		data.setCountryOfResidenceFCK(WidgetUtils
				.getString(cbCountryOfResidence));
	}

	private void buildNonPortugueseAddress(PersonDTO data) {
		String country = WidgetUtils.getString(cbCountryOfResidence);
		if (country != null && !"pt".equals(country)) {
			final NonPortugueseAddressContactDTO address = data
					.getNonPortugueseAddressContact();
			address.setAddressLine1(WidgetUtils.getString(tfAddressLine1));
			address.setAddressLine2(WidgetUtils.getString(tfAddressLine2));
			address.setAddressLine3(WidgetUtils.getString(tfAddressLine3));
		}
	}

	private void buildParents(PersonDTO data) {
		data.getFiliation().setFatherName(WidgetUtils.getString(tfFatherName));
		data.getFiliation().setMotherName(WidgetUtils.getString(tfMotherName));
	}

	private void buildPortugueseAddress(PersonDTO data) {
		String country = WidgetUtils.getString(cbCountryOfResidence);
		if (country != null && "pt".equals(country)) {
			final PortugueseAddressContactDTO address = data
					.getPortugueseAddressContact();
			address.setStreet(WidgetUtils.getString(tfStreet));
			address.setPostalCodeMain(WidgetUtils.getString(tfPostalCodeMain));
			address.setPostalCodeArea(WidgetUtils.getString(tfPostalCodeArea));
			address.setArea(WidgetUtils.getString(tfArea));
		}
	}

	// [end] - Get data

	// [start] - Create view
	public UserDataComponent() {
		super();

		// Header
		// None yet

		// Body
		tabPanel = new TabPanel();
		contentPanel.add(tabPanel);
		tabPanel.setWidth("100%");

		// ---------------------
		VerticalPanel verticalPanelIdentification = new VerticalPanel();
		tabPanel.add(verticalPanelIdentification,
				globalConstants.tabIdentification_text(), false);
		addIdentificationComponents(verticalPanelIdentification);

		// ---------------------
		VerticalPanel verticalPanelPortugueseAddress = new VerticalPanel();
		tabPanel.add(verticalPanelPortugueseAddress,
				globalConstants.tabPortugueseAddress_text(), false);
		addPortugueseAddressComponents(verticalPanelPortugueseAddress);

		VerticalPanel verticalPanelNonPortugueseAddress = new VerticalPanel();
		tabPanel.add(verticalPanelNonPortugueseAddress,
				globalConstants.tabNonPortugueseAddress_text(), false);
		addNonPortugueseAddressComponents(verticalPanelNonPortugueseAddress);

		VerticalPanel verticalPanelContacts = new VerticalPanel();
		tabPanel.add(verticalPanelContacts, globalConstants.tabContacts_text(),
				false);
		addContactsComponents(verticalPanelContacts);

		VerticalPanel verticalPanelParents = new VerticalPanel();
		tabPanel.add(verticalPanelParents, globalConstants.tabParents_text(),
				false);
		addParentComponents(verticalPanelParents);

		// Select 1st tab
		tabPanel.selectTab(0);

		// Footer
		// None yet
	}

	private void addIdentificationComponents(VerticalPanel verticalPanel) {
		// Row 1
		tfGivenNames = new StringTextField(globalConstants.lblGivenNames_text());
		tfGivenNames.setRequired(true);

		tfFamilyNames = new StringTextField(
				globalConstants.lblFamilyNames_text());
		tfFamilyNames.setRequired(true);

		HorizontalPanel row1 = new HorizontalPanel();
		row1.add(tfGivenNames);
		row1.add(new FieldSeparator());
		row1.add(tfFamilyNames);

		// Row 2
		tfNickname = new StringTextField(globalConstants.lblNickname_text());

		HorizontalPanel row2 = new HorizontalPanel();
		row2.add(tfNickname);

		// Row 3
		tfIdDocumentNumber = new StringTextField(
				globalConstants.lblIdDocumentNumber_text());
		tfIdDocumentNumber.setRequired(true);

		cbIdDocumentType = new ComboField(
				globalConstants.lblIdDocumentType_text());
		cbIdDocumentType.setRequired(true);

		HorizontalPanel row3 = new HorizontalPanel();
		row3.add(tfIdDocumentNumber);
		row3.add(new FieldSeparator());
		row3.add(cbIdDocumentType);

		// Row 4
		tfSocialSecurityNumber = new IntegerTextField(
				globalConstants.lblSocialSecurityNumber_text());

		HorizontalPanel row4 = new HorizontalPanel();
		row4.add(tfSocialSecurityNumber);

		// Row 5
		tfDateOfBirth = new DateTextField(globalConstants.lblDateOfBirth_text());
		tfDateOfBirth.setFormat(new DefaultFormat(DateTimeFormat
				.getFormat(WidgetUtils.DATE_FORMAT)));

		tfTownOfBirth = new StringTextField(
				globalConstants.lblTownOfBirth_text());

		HorizontalPanel row5 = new HorizontalPanel();
		row5.add(tfDateOfBirth);
		row5.add(new FieldSeparator());
		row5.add(tfTownOfBirth);

		// Row 6
		cbCountryOfBirth = new ComboField(
				globalConstants.lblCountryOfBirth_text());

		cbCountryOfResidence = new ComboField(
				globalConstants.lblCountryOfResidence_text());

		HorizontalPanel row6 = new HorizontalPanel();
		row6.add(cbCountryOfBirth);
		row6.add(new FieldSeparator());
		row6.add(cbCountryOfResidence);

		// Add rows
		verticalPanel.add(row1);
		verticalPanel.add(row2);
		verticalPanel.add(row3);
		verticalPanel.add(row4);
		verticalPanel.add(row5);
		verticalPanel.add(row6);
	}

	private void addPortugueseAddressComponents(VerticalPanel verticalPanel) {
		// Row 1
		tfStreet = new StringTextField(globalConstants.lblStreet_text());
		tfStreet.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row1 = new HorizontalPanel();
		row1.add(tfStreet);

		// Row 2
		tfPostalCodeMain = new StringTextField(
				globalConstants.lblPostalCodeMain_text());

		tfPostalCodeArea = new StringTextField(
				globalConstants.lblPostalCodeArea_text());

		HorizontalPanel row2 = new HorizontalPanel();
		row2.add(tfPostalCodeMain);
		row2.add(new FieldSeparator());
		row2.add(tfPostalCodeArea);

		// Row 3
		tfArea = new StringTextField(globalConstants.lblArea_text());
		tfArea.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row3 = new HorizontalPanel();
		row3.add(tfArea);

		// Add rows
		verticalPanel.add(row1);
		verticalPanel.add(row2);
		verticalPanel.add(row3);
	}

	private void addNonPortugueseAddressComponents(VerticalPanel verticalPanel) {
		// Row 1
		tfAddressLine1 = new StringTextField(
				globalConstants.lblAddressLine1_text());
		tfAddressLine1.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row1 = new HorizontalPanel();
		row1.add(tfAddressLine1);

		// Row 2
		tfAddressLine2 = new StringTextField(
				globalConstants.lblAddressLine2_text());
		tfAddressLine2.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row2 = new HorizontalPanel();
		row2.add(tfAddressLine2);

		// Row 3
		tfAddressLine3 = new StringTextField(
				globalConstants.lblAddressLine3_text());
		tfAddressLine3.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row3 = new HorizontalPanel();
		row3.add(tfAddressLine3);

		// Add rows
		verticalPanel.add(row1);
		verticalPanel.add(row2);
		verticalPanel.add(row3);
	}

	private void addContactsComponents(VerticalPanel verticalPanel) {
		// Row 1
		tfPrivateEmailContact = new StringTextField(
				globalConstants.lblPrivateEmailContact_text());
		tfPrivateEmailContact.setInputFormat(".+@.+\\.[a-z]+");

		tfInstitutionalEmailContact = new StringTextField(
				globalConstants.lblInstitutionalEmailContact_text());
		tfInstitutionalEmailContact.setInputFormat(".+@.+\\.[a-z]+");

		HorizontalPanel row1 = new HorizontalPanel();
		row1.add(tfPrivateEmailContact);
		row1.add(new FieldSeparator());
		row1.add(tfInstitutionalEmailContact);

		// Add rows
		verticalPanel.add(row1);
	}

	private void addParentComponents(VerticalPanel verticalPanel) {
		// Row 1
		tfFatherName = new StringTextField(globalConstants.lblFatherName_text());
		tfFatherName.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row1 = new HorizontalPanel();
		row1.add(tfFatherName);

		// Row 2
		tfMotherName = new StringTextField(globalConstants.lblMotherName_text());
		tfMotherName.setWidgetSize(WidgetSize.BIG);

		HorizontalPanel row2 = new HorizontalPanel();
		row2.add(tfMotherName);

		// Add rows
		verticalPanel.add(row1);
		verticalPanel.add(row2);
	}

	// [end] - Create view

	// [start] - Component methods
	public void setReadOnly(boolean readOnly) {
		setReadOnlyIdentification(readOnly, readOnly, readOnly, readOnly,
				readOnly, readOnly, readOnly, readOnly, readOnly, readOnly);
		setReadOnlyPortugueseAddress(readOnly, readOnly, readOnly, readOnly);
		setReadOnlyNonPortugueseAddress(readOnly, readOnly, readOnly);
		setReadOnlyContacts(readOnly, readOnly);
		setReadOnlyParents(readOnly, readOnly);
	}

	public void setReadOnlyIdentification(boolean readOnlyGivenNames,
			boolean readOnlyFamilyNames, boolean readOnlyNickname,
			boolean readOnlyIdDocumentNumber, boolean readOnlyIdDocumentType,
			boolean readOnlySocialSecurityNumber, boolean readOnlyDateOfBirth,
			boolean readOnlyTownOfBirth, boolean readOnlyCountryOfBirth,
			boolean readOnlyCountryOfResidence) {
		tfGivenNames.setReadOnly(readOnlyGivenNames);
		tfFamilyNames.setReadOnly(readOnlyFamilyNames);
		tfNickname.setReadOnly(readOnlyNickname);

		tfIdDocumentNumber.setReadOnly(readOnlyIdDocumentNumber);
		cbIdDocumentType.setReadOnly(readOnlyIdDocumentType);
		tfSocialSecurityNumber.setReadOnly(readOnlySocialSecurityNumber);

		tfDateOfBirth.setReadOnly(readOnlyDateOfBirth);
		tfTownOfBirth.setReadOnly(readOnlyTownOfBirth);

		cbCountryOfBirth.setReadOnly(readOnlyCountryOfBirth);
		cbCountryOfResidence.setReadOnly(readOnlyCountryOfResidence);
	}

	public void setReadOnlyPortugueseAddress(boolean readOnlyStreet,
			boolean readOnlyPostalCodeMain, boolean readOnlyPostalCodeArea,
			boolean readOnlyArea) {
		tfStreet.setReadOnly(readOnlyStreet);
		tfPostalCodeMain.setReadOnly(readOnlyPostalCodeMain);
		tfPostalCodeArea.setReadOnly(readOnlyPostalCodeArea);
		tfArea.setReadOnly(readOnlyArea);
	}

	public void setReadOnlyNonPortugueseAddress(boolean readOnlyAddressLine1,
			boolean readOnlyAddressLine2, boolean readOnlyAddressLine3) {
		tfAddressLine1.setReadOnly(readOnlyAddressLine1);
		tfAddressLine2.setReadOnly(readOnlyAddressLine2);
		tfAddressLine3.setReadOnly(readOnlyAddressLine3);

	}

	public void setReadOnlyContacts(boolean readOnlyInstitutionalEmailContact,
			boolean readOnlyPrivateEmailContact) {
		tfInstitutionalEmailContact
				.setReadOnly(readOnlyInstitutionalEmailContact);
		tfPrivateEmailContact.setReadOnly(readOnlyPrivateEmailContact);
	}

	public void setReadOnlyParents(boolean readOnlyFatherName,
			boolean readOnlyMotherName) {
		tfFatherName.setReadOnly(readOnlyFatherName);
		tfMotherName.setReadOnly(readOnlyMotherName);
	}

	// [end] - Component methods

	// [start] - Individual read only
	public void setReadOnlyGivenNames(boolean readOnly) {
		tfGivenNames.setReadOnly(readOnly);
	}

	public void getReadOnlyGivenNames() {
		tfGivenNames.getReadOnly();
	}

	public void setReadOnlyFamilyNames(boolean readOnly) {
		tfFamilyNames.setReadOnly(readOnly);
	}

	public void getReadOnlyFamilyNames() {
		tfFamilyNames.getReadOnly();
	}

	public void setReadOnlyIdDocumentNumber(boolean readOnly) {
		tfIdDocumentNumber.setReadOnly(readOnly);
	}

	public void getReadOnlyIdDocumentNumber() {
		tfIdDocumentNumber.getReadOnly();
	}

	public void setReadOnlyIdDocumentType(boolean readOnly) {
		cbIdDocumentType.setReadOnly(readOnly);
	}

	public void getReadOnlyIdDocumentType() {
		cbIdDocumentType.getReadOnly();
	}

	//
	// Add more read only methods if needed
	//
	// public void setReadOnlyFatherName(boolean readOnly) {
	// tfFatherName.setReadOnly(readOnly);
	// }
	//
	// public void getReadOnlyFatherName() {
	// tfFatherName.getReadOnly();
	// }
	//
	// public void setReadOnlyMotherName(boolean readOnly) {
	// tfMotherName.setReadOnly(readOnly);
	// }
	//
	// public void getReadOnlyMotherName() {
	// tfMotherName.getReadOnly();
	// }
	// [end] - Individual read only

}

