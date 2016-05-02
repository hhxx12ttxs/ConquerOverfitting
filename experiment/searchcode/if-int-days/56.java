/**
 * JacobGen generated file --- do not edit
 *
 * (http://www.sourceforge.net/projects/jacob-project */
package com.jacobgen.microsoft.excel;

import com.jacob.com.*;

public class _Workbook extends Dispatch {

	public static final String componentName = "Excel._Workbook";

	public _Workbook() {
		super(componentName);
	}

	/**
	* This constructor is used instead of a case operation to
	* turn a Dispatch object into a wider object - it must exist
	* in every wrapper class whose instances may be returned from
	* method calls wrapped in VT_DISPATCH Variants.
	*/
	public _Workbook(Dispatch d) {
		// take over the IDispatch pointer
		m_pDispatch = d.m_pDispatch;
		// null out the input's pointer
		d.m_pDispatch = 0;
	}

	public _Workbook(String compName) {
		super(compName);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Application
	 */
	public Application getApplication() {
		return new Application(Dispatch.get(this, "Application").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type int
	 */
	public int getCreator() {
		return Dispatch.get(this, "Creator").changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Object
	 */
	public Object getParent() {
		return Dispatch.get(this, "Parent");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getAcceptLabelsInFormulas() {
		return Dispatch.get(this, "AcceptLabelsInFormulas").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param acceptLabelsInFormulas an input-parameter of type boolean
	 */
	public void setAcceptLabelsInFormulas(boolean acceptLabelsInFormulas) {
		Dispatch.put(this, "AcceptLabelsInFormulas", new Variant(acceptLabelsInFormulas));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void activate() {
		Dispatch.call(this, "Activate");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Chart
	 */
	public Chart getActiveChart() {
		return new Chart(Dispatch.get(this, "ActiveChart").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Object
	 */
	public Object getActiveSheet() {
		return Dispatch.get(this, "ActiveSheet");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getAuthor() {
		return Dispatch.get(this, "Author").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param author an input-parameter of type String
	 */
	public void setAuthor(String author) {
		Dispatch.put(this, "Author", author);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type int
	 */
	public int getAutoUpdateFrequency() {
		return Dispatch.get(this, "AutoUpdateFrequency").changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param autoUpdateFrequency an input-parameter of type int
	 */
	public void setAutoUpdateFrequency(int autoUpdateFrequency) {
		Dispatch.put(this, "AutoUpdateFrequency", new Variant(autoUpdateFrequency));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getAutoUpdateSaveChanges() {
		return Dispatch.get(this, "AutoUpdateSaveChanges").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param autoUpdateSaveChanges an input-parameter of type boolean
	 */
	public void setAutoUpdateSaveChanges(boolean autoUpdateSaveChanges) {
		Dispatch.put(this, "AutoUpdateSaveChanges", new Variant(autoUpdateSaveChanges));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type int
	 */
	public int getChangeHistoryDuration() {
		return Dispatch.get(this, "ChangeHistoryDuration").changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param changeHistoryDuration an input-parameter of type int
	 */
	public void setChangeHistoryDuration(int changeHistoryDuration) {
		Dispatch.put(this, "ChangeHistoryDuration", new Variant(changeHistoryDuration));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Object
	 */
	public Object getBuiltinDocumentProperties() {
		return Dispatch.get(this, "BuiltinDocumentProperties");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param mode an input-parameter of type int
	 * @param writePassword an input-parameter of type Variant
	 * @param notify an input-parameter of type Variant
	 */
	public void changeFileAccess(int mode, Variant writePassword, Variant notify) {
		Dispatch.call(this, "ChangeFileAccess", new Variant(mode), writePassword, notify);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param mode an input-parameter of type int
	 * @param writePassword an input-parameter of type Variant
	 */
	public void changeFileAccess(int mode, Variant writePassword) {
		Dispatch.call(this, "ChangeFileAccess", new Variant(mode), writePassword);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param mode an input-parameter of type int
	 */
	public void changeFileAccess(int mode) {
		Dispatch.call(this, "ChangeFileAccess", new Variant(mode));
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param mode an input-parameter of type int
	 * @param writePassword an input-parameter of type Variant
	 * @param notify an input-parameter of type Variant
	 */
	public void changeFileAccess(int mode, Variant writePassword, Variant notify) {
		Dispatch.call(this, "ChangeFileAccess", new Variant(mode), writePassword, notify);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param name an input-parameter of type String
	 * @param newName an input-parameter of type String
	 * @param type an input-parameter of type int
	 */
	public void changeLink(String name, String newName, int type) {
		Dispatch.call(this, "ChangeLink", name, newName, new Variant(type));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param name an input-parameter of type String
	 * @param newName an input-parameter of type String
	 */
	public void changeLink(String name, String newName) {
		Dispatch.call(this, "ChangeLink", name, newName);
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param name an input-parameter of type String
	 * @param newName an input-parameter of type String
	 * @param type an input-parameter of type int
	 */
	public void changeLink(String name, String newName, int type) {
		Dispatch.call(this, "ChangeLink", name, newName, new Variant(type));

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Sheets
	 */
	public Sheets getCharts() {
		return new Sheets(Dispatch.get(this, "Charts").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param saveChanges an input-parameter of type Variant
	 * @param filename an input-parameter of type Variant
	 * @param routeWorkbook an input-parameter of type Variant
	 */
	public void close(Variant saveChanges, Variant filename, Variant routeWorkbook) {
		Dispatch.call(this, "Close", saveChanges, filename, routeWorkbook);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param saveChanges an input-parameter of type Variant
	 * @param filename an input-parameter of type Variant
	 */
	public void close(Variant saveChanges, Variant filename) {
		Dispatch.call(this, "Close", saveChanges, filename);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param saveChanges an input-parameter of type Variant
	 */
	public void close(Variant saveChanges) {
		Dispatch.call(this, "Close", saveChanges);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void close() {
		Dispatch.call(this, "Close");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param saveChanges an input-parameter of type Variant
	 * @param filename an input-parameter of type Variant
	 * @param routeWorkbook an input-parameter of type Variant
	 */
	public void close(Variant saveChanges, Variant filename, Variant routeWorkbook) {
		Dispatch.call(this, "Close", saveChanges, filename, routeWorkbook);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getCodeName() {
		return Dispatch.get(this, "CodeName").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String get_CodeName() {
		return Dispatch.get(this, "_CodeName").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param _CodeName an input-parameter of type String
	 */
	public void set_CodeName(String _CodeName) {
		Dispatch.put(this, "_CodeName", _CodeName);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param index an input-parameter of type Variant
	 * @return the result is of type Variant
	 */
	public Variant getColors(Variant index) {
		return Dispatch.call(this, "Colors", index);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Variant
	 */
	public Variant getColors() {
		return Dispatch.get(this, "Colors");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param index an input-parameter of type Variant
	 * @return the result is of type Variant
	 */
	public Variant getColors(Variant index) {
		Variant result_of_Colors = Dispatch.call(this, "Colors", index);


		return result_of_Colors;
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param index an input-parameter of type Variant
	 */
	public void setColors(Variant index) {
		Dispatch.put(this, "Colors", index);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void setColors() {
		Dispatch.call(this, "Colors");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param index an input-parameter of type Variant
	 */
	public void setColors(Variant index) {
		Dispatch.put(this, "Colors", index);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type CommandBars
	 */
	public CommandBars getCommandBars() {
		return new CommandBars(Dispatch.get(this, "CommandBars").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getComments() {
		return Dispatch.get(this, "Comments").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param comments an input-parameter of type String
	 */
	public void setComments(String comments) {
		Dispatch.put(this, "Comments", comments);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type int
	 */
	public int getConflictResolution() {
		return Dispatch.get(this, "ConflictResolution").changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param conflictResolution an input-parameter of type int
	 */
	public void setConflictResolution(int conflictResolution) {
		Dispatch.put(this, "ConflictResolution", new Variant(conflictResolution));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Object
	 */
	public Object getContainer() {
		return Dispatch.get(this, "Container");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getCreateBackup() {
		return Dispatch.get(this, "CreateBackup").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Object
	 */
	public Object getCustomDocumentProperties() {
		return Dispatch.get(this, "CustomDocumentProperties");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getDate1904() {
		return Dispatch.get(this, "Date1904").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param date1904 an input-parameter of type boolean
	 */
	public void setDate1904(boolean date1904) {
		Dispatch.put(this, "Date1904", new Variant(date1904));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param numberFormat an input-parameter of type String
	 */
	public void deleteNumberFormat(String numberFormat) {
		Dispatch.call(this, "DeleteNumberFormat", numberFormat);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Sheets
	 */
	public Sheets getDialogSheets() {
		return new Sheets(Dispatch.get(this, "DialogSheets").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type int
	 */
	public int getDisplayDrawingObjects() {
		return Dispatch.get(this, "DisplayDrawingObjects").changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param displayDrawingObjects an input-parameter of type int
	 */
	public void setDisplayDrawingObjects(int displayDrawingObjects) {
		Dispatch.put(this, "DisplayDrawingObjects", new Variant(displayDrawingObjects));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean exclusiveAccess() {
		return Dispatch.call(this, "ExclusiveAccess").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type int
	 */
	public int getFileFormat() {
		return Dispatch.get(this, "FileFormat").changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void forwardMailer() {
		Dispatch.call(this, "ForwardMailer");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getFullName() {
		return Dispatch.get(this, "FullName").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getHasMailer() {
		return Dispatch.get(this, "HasMailer").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param hasMailer an input-parameter of type boolean
	 */
	public void setHasMailer(boolean hasMailer) {
		Dispatch.put(this, "HasMailer", new Variant(hasMailer));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getHasPassword() {
		return Dispatch.get(this, "HasPassword").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getHasRoutingSlip() {
		return Dispatch.get(this, "HasRoutingSlip").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param hasRoutingSlip an input-parameter of type boolean
	 */
	public void setHasRoutingSlip(boolean hasRoutingSlip) {
		Dispatch.put(this, "HasRoutingSlip", new Variant(hasRoutingSlip));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getIsAddin() {
		return Dispatch.get(this, "IsAddin").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param isAddin an input-parameter of type boolean
	 */
	public void setIsAddin(boolean isAddin) {
		Dispatch.put(this, "IsAddin", new Variant(isAddin));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getKeywords() {
		return Dispatch.get(this, "Keywords").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param keywords an input-parameter of type String
	 */
	public void setKeywords(String keywords) {
		Dispatch.put(this, "Keywords", keywords);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param name an input-parameter of type String
	 * @param linkInfo an input-parameter of type int
	 * @param type an input-parameter of type Variant
	 * @param editionRef an input-parameter of type Variant
	 * @return the result is of type Variant
	 */
	public Variant linkInfo(String name, int linkInfo, Variant type, Variant editionRef) {
		return Dispatch.call(this, "LinkInfo", name, new Variant(linkInfo), type, editionRef);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param name an input-parameter of type String
	 * @param linkInfo an input-parameter of type int
	 * @param type an input-parameter of type Variant
	 * @return the result is of type Variant
	 */
	public Variant linkInfo(String name, int linkInfo, Variant type) {
		return Dispatch.call(this, "LinkInfo", name, new Variant(linkInfo), type);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param name an input-parameter of type String
	 * @param linkInfo an input-parameter of type int
	 * @return the result is of type Variant
	 */
	public Variant linkInfo(String name, int linkInfo) {
		return Dispatch.call(this, "LinkInfo", name, new Variant(linkInfo));
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param name an input-parameter of type String
	 * @param linkInfo an input-parameter of type int
	 * @param type an input-parameter of type Variant
	 * @param editionRef an input-parameter of type Variant
	 * @return the result is of type Variant
	 */
	public Variant linkInfo(String name, int linkInfo, Variant type, Variant editionRef) {
		Variant result_of_LinkInfo = Dispatch.call(this, "LinkInfo", name, new Variant(linkInfo), type, editionRef);


		return result_of_LinkInfo;
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param type an input-parameter of type Variant
	 * @return the result is of type Variant
	 */
	public Variant linkSources(Variant type) {
		return Dispatch.call(this, "LinkSources", type);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Variant
	 */
	public Variant linkSources() {
		return Dispatch.call(this, "LinkSources");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param type an input-parameter of type Variant
	 * @return the result is of type Variant
	 */
	public Variant linkSources(Variant type) {
		Variant result_of_LinkSources = Dispatch.call(this, "LinkSources", type);


		return result_of_LinkSources;
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Mailer
	 */
	public Mailer getMailer() {
		return new Mailer(Dispatch.get(this, "Mailer").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 */
	public void mergeWorkbook(Variant filename) {
		Dispatch.call(this, "MergeWorkbook", filename);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Sheets
	 */
	public Sheets getModules() {
		return new Sheets(Dispatch.get(this, "Modules").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getMultiUserEditing() {
		return Dispatch.get(this, "MultiUserEditing").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getName() {
		return Dispatch.get(this, "Name").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Names
	 */
	public Names getNames() {
		return new Names(Dispatch.get(this, "Names").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Window
	 */
	public Window newWindow() {
		return new Window(Dispatch.call(this, "NewWindow").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getOnSave() {
		return Dispatch.get(this, "OnSave").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param onSave an input-parameter of type String
	 */
	public void setOnSave(String onSave) {
		Dispatch.put(this, "OnSave", onSave);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getOnSheetActivate() {
		return Dispatch.get(this, "OnSheetActivate").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param onSheetActivate an input-parameter of type String
	 */
	public void setOnSheetActivate(String onSheetActivate) {
		Dispatch.put(this, "OnSheetActivate", onSheetActivate);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getOnSheetDeactivate() {
		return Dispatch.get(this, "OnSheetDeactivate").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param onSheetDeactivate an input-parameter of type String
	 */
	public void setOnSheetDeactivate(String onSheetDeactivate) {
		Dispatch.put(this, "OnSheetDeactivate", onSheetDeactivate);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param name an input-parameter of type String
	 * @param readOnly an input-parameter of type Variant
	 * @param type an input-parameter of type Variant
	 */
	public void openLinks(String name, Variant readOnly, Variant type) {
		Dispatch.call(this, "OpenLinks", name, readOnly, type);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param name an input-parameter of type String
	 * @param readOnly an input-parameter of type Variant
	 */
	public void openLinks(String name, Variant readOnly) {
		Dispatch.call(this, "OpenLinks", name, readOnly);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param name an input-parameter of type String
	 */
	public void openLinks(String name) {
		Dispatch.call(this, "OpenLinks", name);
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param name an input-parameter of type String
	 * @param readOnly an input-parameter of type Variant
	 * @param type an input-parameter of type Variant
	 */
	public void openLinks(String name, Variant readOnly, Variant type) {
		Dispatch.call(this, "OpenLinks", name, readOnly, type);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getPath() {
		return Dispatch.get(this, "Path").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getPersonalViewListSettings() {
		return Dispatch.get(this, "PersonalViewListSettings").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param personalViewListSettings an input-parameter of type boolean
	 */
	public void setPersonalViewListSettings(boolean personalViewListSettings) {
		Dispatch.put(this, "PersonalViewListSettings", new Variant(personalViewListSettings));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getPersonalViewPrintSettings() {
		return Dispatch.get(this, "PersonalViewPrintSettings").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param personalViewPrintSettings an input-parameter of type boolean
	 */
	public void setPersonalViewPrintSettings(boolean personalViewPrintSettings) {
		Dispatch.put(this, "PersonalViewPrintSettings", new Variant(personalViewPrintSettings));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type PivotCaches
	 */
	public PivotCaches pivotCaches() {
		return new PivotCaches(Dispatch.call(this, "PivotCaches").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param destName an input-parameter of type Variant
	 */
	public void post(Variant destName) {
		Dispatch.call(this, "Post", destName);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void post() {
		Dispatch.call(this, "Post");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param destName an input-parameter of type Variant
	 */
	public void post(Variant destName) {
		Dispatch.call(this, "Post", destName);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getPrecisionAsDisplayed() {
		return Dispatch.get(this, "PrecisionAsDisplayed").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param precisionAsDisplayed an input-parameter of type boolean
	 */
	public void setPrecisionAsDisplayed(boolean precisionAsDisplayed) {
		Dispatch.put(this, "PrecisionAsDisplayed", new Variant(precisionAsDisplayed));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 * @param activePrinter an input-parameter of type Variant
	 * @param printToFile an input-parameter of type Variant
	 * @param collate an input-parameter of type Variant
	 */
	public void __PrintOut(Variant from, Variant to, Variant copies, Variant preview, Variant activePrinter, Variant printToFile, Variant collate) {
		Dispatch.call(this, "__PrintOut", from, to, copies, preview, activePrinter, printToFile, collate);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 * @param activePrinter an input-parameter of type Variant
	 * @param printToFile an input-parameter of type Variant
	 */
	public void __PrintOut(Variant from, Variant to, Variant copies, Variant preview, Variant activePrinter, Variant printToFile) {
		Dispatch.call(this, "__PrintOut", from, to, copies, preview, activePrinter, printToFile);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 * @param activePrinter an input-parameter of type Variant
	 */
	public void __PrintOut(Variant from, Variant to, Variant copies, Variant preview, Variant activePrinter) {
		Dispatch.call(this, "__PrintOut", from, to, copies, preview, activePrinter);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 */
	public void __PrintOut(Variant from, Variant to, Variant copies, Variant preview) {
		Dispatch.call(this, "__PrintOut", from, to, copies, preview);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 */
	public void __PrintOut(Variant from, Variant to, Variant copies) {
		Dispatch.call(this, "__PrintOut", from, to, copies);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 */
	public void __PrintOut(Variant from, Variant to) {
		Dispatch.call(this, "__PrintOut", from, to);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 */
	public void __PrintOut(Variant from) {
		Dispatch.call(this, "__PrintOut", from);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void __PrintOut() {
		Dispatch.call(this, "__PrintOut");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 * @param activePrinter an input-parameter of type Variant
	 * @param printToFile an input-parameter of type Variant
	 * @param collate an input-parameter of type Variant
	 */
	public void __PrintOut(Variant from, Variant to, Variant copies, Variant preview, Variant activePrinter, Variant printToFile, Variant collate) {
		Dispatch.call(this, "__PrintOut", from, to, copies, preview, activePrinter, printToFile, collate);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param enableChanges an input-parameter of type Variant
	 */
	public void printPreview(Variant enableChanges) {
		Dispatch.call(this, "PrintPreview", enableChanges);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void printPreview() {
		Dispatch.call(this, "PrintPreview");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param enableChanges an input-parameter of type Variant
	 */
	public void printPreview(Variant enableChanges) {
		Dispatch.call(this, "PrintPreview", enableChanges);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param password an input-parameter of type Variant
	 * @param structure an input-parameter of type Variant
	 * @param windows an input-parameter of type Variant
	 */
	public void _Protect(Variant password, Variant structure, Variant windows) {
		Dispatch.call(this, "_Protect", password, structure, windows);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param password an input-parameter of type Variant
	 * @param structure an input-parameter of type Variant
	 */
	public void _Protect(Variant password, Variant structure) {
		Dispatch.call(this, "_Protect", password, structure);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param password an input-parameter of type Variant
	 */
	public void _Protect(Variant password) {
		Dispatch.call(this, "_Protect", password);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void _Protect() {
		Dispatch.call(this, "_Protect");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param password an input-parameter of type Variant
	 * @param structure an input-parameter of type Variant
	 * @param windows an input-parameter of type Variant
	 */
	public void _Protect(Variant password, Variant structure, Variant windows) {
		Dispatch.call(this, "_Protect", password, structure, windows);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param sharingPassword an input-parameter of type Variant
	 */
	public void _ProtectSharing(Variant filename, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, Variant sharingPassword) {
		Dispatch.call(this, "_ProtectSharing", filename, password, writeResPassword, readOnlyRecommended, createBackup, sharingPassword);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 */
	public void _ProtectSharing(Variant filename, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup) {
		Dispatch.call(this, "_ProtectSharing", filename, password, writeResPassword, readOnlyRecommended, createBackup);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 */
	public void _ProtectSharing(Variant filename, Variant password, Variant writeResPassword, Variant readOnlyRecommended) {
		Dispatch.call(this, "_ProtectSharing", filename, password, writeResPassword, readOnlyRecommended);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 */
	public void _ProtectSharing(Variant filename, Variant password, Variant writeResPassword) {
		Dispatch.call(this, "_ProtectSharing", filename, password, writeResPassword);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 */
	public void _ProtectSharing(Variant filename, Variant password) {
		Dispatch.call(this, "_ProtectSharing", filename, password);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 */
	public void _ProtectSharing(Variant filename) {
		Dispatch.call(this, "_ProtectSharing", filename);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void _ProtectSharing() {
		Dispatch.call(this, "_ProtectSharing");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param sharingPassword an input-parameter of type Variant
	 */
	public void _ProtectSharing(Variant filename, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, Variant sharingPassword) {
		Dispatch.call(this, "_ProtectSharing", filename, password, writeResPassword, readOnlyRecommended, createBackup, sharingPassword);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getProtectStructure() {
		return Dispatch.get(this, "ProtectStructure").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getProtectWindows() {
		return Dispatch.get(this, "ProtectWindows").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getReadOnly() {
		return Dispatch.get(this, "ReadOnly").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean get_ReadOnlyRecommended() {
		return Dispatch.get(this, "_ReadOnlyRecommended").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void refreshAll() {
		Dispatch.call(this, "RefreshAll");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void reply() {
		Dispatch.call(this, "Reply");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void replyAll() {
		Dispatch.call(this, "ReplyAll");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param index an input-parameter of type int
	 */
	public void removeUser(int index) {
		Dispatch.call(this, "RemoveUser", new Variant(index));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type int
	 */
	public int getRevisionNumber() {
		return Dispatch.get(this, "RevisionNumber").changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void route() {
		Dispatch.call(this, "Route");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getRouted() {
		return Dispatch.get(this, "Routed").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type RoutingSlip
	 */
	public RoutingSlip getRoutingSlip() {
		return new RoutingSlip(Dispatch.get(this, "RoutingSlip").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param which an input-parameter of type int
	 */
	public void runAutoMacros(int which) {
		Dispatch.call(this, "RunAutoMacros", new Variant(which));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void save() {
		Dispatch.call(this, "Save");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param accessMode an input-parameter of type int
	 * @param conflictResolution an input-parameter of type Variant
	 * @param addToMru an input-parameter of type Variant
	 * @param textCodepage an input-parameter of type Variant
	 * @param textVisualLayout an input-parameter of type Variant
	 */
	public void _SaveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, int accessMode, Variant conflictResolution, Variant addToMru, Variant textCodepage, Variant textVisualLayout) {
		Dispatch.callN(this, "_SaveAs", new Object[] { filename, fileFormat, password, writeResPassword, readOnlyRecommended, createBackup, new Variant(accessMode), conflictResolution, addToMru, textCodepage, textVisualLayout});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param accessMode an input-parameter of type int
	 * @param conflictResolution an input-parameter of type Variant
	 * @param addToMru an input-parameter of type Variant
	 * @param textCodepage an input-parameter of type Variant
	 */
	public void _SaveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, int accessMode, Variant conflictResolution, Variant addToMru, Variant textCodepage) {
		Dispatch.callN(this, "_SaveAs", new Object[] { filename, fileFormat, password, writeResPassword, readOnlyRecommended, createBackup, new Variant(accessMode), conflictResolution, addToMru, textCodepage});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param accessMode an input-parameter of type int
	 * @param conflictResolution an input-parameter of type Variant
	 * @param addToMru an input-parameter of type Variant
	 */
	public void _SaveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, int accessMode, Variant conflictResolution, Variant addToMru) {
		Dispatch.callN(this, "_SaveAs", new Object[] { filename, fileFormat, password, writeResPassword, readOnlyRecommended, createBackup, new Variant(accessMode), conflictResolution, addToMru});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param accessMode an input-parameter of type int
	 * @param conflictResolution an input-parameter of type Variant
	 */
	public void _SaveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, int accessMode, Variant conflictResolution) {
		Dispatch.call(this, "_SaveAs", filename, fileFormat, password, writeResPassword, readOnlyRecommended, createBackup, new Variant(accessMode), conflictResolution);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param accessMode an input-parameter of type int
	 */
	public void _SaveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, int accessMode) {
		Dispatch.call(this, "_SaveAs", filename, fileFormat, password, writeResPassword, readOnlyRecommended, createBackup, new Variant(accessMode));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 */
	public void _SaveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup) {
		Dispatch.call(this, "_SaveAs", filename, fileFormat, password, writeResPassword, readOnlyRecommended, createBackup);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 */
	public void _SaveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended) {
		Dispatch.call(this, "_SaveAs", filename, fileFormat, password, writeResPassword, readOnlyRecommended);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 */
	public void _SaveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword) {
		Dispatch.call(this, "_SaveAs", filename, fileFormat, password, writeResPassword);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 */
	public void _SaveAs(Variant filename, Variant fileFormat, Variant password) {
		Dispatch.call(this, "_SaveAs", filename, fileFormat, password);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 */
	public void _SaveAs(Variant filename, Variant fileFormat) {
		Dispatch.call(this, "_SaveAs", filename, fileFormat);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 */
	public void _SaveAs(Variant filename) {
		Dispatch.call(this, "_SaveAs", filename);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void _SaveAs() {
		Dispatch.call(this, "_SaveAs");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param accessMode an input-parameter of type int
	 * @param conflictResolution an input-parameter of type Variant
	 * @param addToMru an input-parameter of type Variant
	 * @param textCodepage an input-parameter of type Variant
	 * @param textVisualLayout an input-parameter of type Variant
	 */
	public void _SaveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, int accessMode, Variant conflictResolution, Variant addToMru, Variant textCodepage, Variant textVisualLayout) {
		Dispatch.callN(this, "_SaveAs", new Object[] { filename, fileFormat, password, writeResPassword, readOnlyRecommended, createBackup, new Variant(accessMode), conflictResolution, addToMru, textCodepage, textVisualLayout});

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 */
	public void saveCopyAs(Variant filename) {
		Dispatch.call(this, "SaveCopyAs", filename);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void saveCopyAs() {
		Dispatch.call(this, "SaveCopyAs");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param filename an input-parameter of type Variant
	 */
	public void saveCopyAs(Variant filename) {
		Dispatch.call(this, "SaveCopyAs", filename);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getSaved() {
		return Dispatch.get(this, "Saved").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param saved an input-parameter of type boolean
	 */
	public void setSaved(boolean saved) {
		Dispatch.put(this, "Saved", new Variant(saved));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getSaveLinkValues() {
		return Dispatch.get(this, "SaveLinkValues").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param saveLinkValues an input-parameter of type boolean
	 */
	public void setSaveLinkValues(boolean saveLinkValues) {
		Dispatch.put(this, "SaveLinkValues", new Variant(saveLinkValues));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param recipients an input-parameter of type Variant
	 * @param subject an input-parameter of type Variant
	 * @param returnReceipt an input-parameter of type Variant
	 */
	public void sendMail(Variant recipients, Variant subject, Variant returnReceipt) {
		Dispatch.call(this, "SendMail", recipients, subject, returnReceipt);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param recipients an input-parameter of type Variant
	 * @param subject an input-parameter of type Variant
	 */
	public void sendMail(Variant recipients, Variant subject) {
		Dispatch.call(this, "SendMail", recipients, subject);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param recipients an input-parameter of type Variant
	 */
	public void sendMail(Variant recipients) {
		Dispatch.call(this, "SendMail", recipients);
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param recipients an input-parameter of type Variant
	 * @param subject an input-parameter of type Variant
	 * @param returnReceipt an input-parameter of type Variant
	 */
	public void sendMail(Variant recipients, Variant subject, Variant returnReceipt) {
		Dispatch.call(this, "SendMail", recipients, subject, returnReceipt);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param fileFormat an input-parameter of type Variant
	 * @param priority an input-parameter of type int
	 */
	public void sendMailer(Variant fileFormat, int priority) {
		Dispatch.call(this, "SendMailer", fileFormat, new Variant(priority));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param fileFormat an input-parameter of type Variant
	 */
	public void sendMailer(Variant fileFormat) {
		Dispatch.call(this, "SendMailer", fileFormat);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void sendMailer() {
		Dispatch.call(this, "SendMailer");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param fileFormat an input-parameter of type Variant
	 * @param priority an input-parameter of type int
	 */
	public void sendMailer(Variant fileFormat, int priority) {
		Dispatch.call(this, "SendMailer", fileFormat, new Variant(priority));

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param name an input-parameter of type String
	 * @param procedure an input-parameter of type Variant
	 */
	public void setLinkOnData(String name, Variant procedure) {
		Dispatch.call(this, "SetLinkOnData", name, procedure);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param name an input-parameter of type String
	 */
	public void setLinkOnData(String name) {
		Dispatch.call(this, "SetLinkOnData", name);
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param name an input-parameter of type String
	 * @param procedure an input-parameter of type Variant
	 */
	public void setLinkOnData(String name, Variant procedure) {
		Dispatch.call(this, "SetLinkOnData", name, procedure);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Sheets
	 */
	public Sheets getSheets() {
		return new Sheets(Dispatch.get(this, "Sheets").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getShowConflictHistory() {
		return Dispatch.get(this, "ShowConflictHistory").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param showConflictHistory an input-parameter of type boolean
	 */
	public void setShowConflictHistory(boolean showConflictHistory) {
		Dispatch.put(this, "ShowConflictHistory", new Variant(showConflictHistory));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Styles
	 */
	public Styles getStyles() {
		return new Styles(Dispatch.get(this, "Styles").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getSubject() {
		return Dispatch.get(this, "Subject").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param subject an input-parameter of type String
	 */
	public void setSubject(String subject) {
		Dispatch.put(this, "Subject", subject);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getTitle() {
		return Dispatch.get(this, "Title").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param title an input-parameter of type String
	 */
	public void setTitle(String title) {
		Dispatch.put(this, "Title", title);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param password an input-parameter of type Variant
	 */
	public void unprotect(Variant password) {
		Dispatch.call(this, "Unprotect", password);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void unprotect() {
		Dispatch.call(this, "Unprotect");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param password an input-parameter of type Variant
	 */
	public void unprotect(Variant password) {
		Dispatch.call(this, "Unprotect", password);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sharingPassword an input-parameter of type Variant
	 */
	public void unprotectSharing(Variant sharingPassword) {
		Dispatch.call(this, "UnprotectSharing", sharingPassword);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void unprotectSharing() {
		Dispatch.call(this, "UnprotectSharing");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param sharingPassword an input-parameter of type Variant
	 */
	public void unprotectSharing(Variant sharingPassword) {
		Dispatch.call(this, "UnprotectSharing", sharingPassword);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void updateFromFile() {
		Dispatch.call(this, "UpdateFromFile");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param name an input-parameter of type Variant
	 * @param type an input-parameter of type Variant
	 */
	public void updateLink(Variant name, Variant type) {
		Dispatch.call(this, "UpdateLink", name, type);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param name an input-parameter of type Variant
	 */
	public void updateLink(Variant name) {
		Dispatch.call(this, "UpdateLink", name);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void updateLink() {
		Dispatch.call(this, "UpdateLink");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param name an input-parameter of type Variant
	 * @param type an input-parameter of type Variant
	 */
	public void updateLink(Variant name, Variant type) {
		Dispatch.call(this, "UpdateLink", name, type);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getUpdateRemoteReferences() {
		return Dispatch.get(this, "UpdateRemoteReferences").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param updateRemoteReferences an input-parameter of type boolean
	 */
	public void setUpdateRemoteReferences(boolean updateRemoteReferences) {
		Dispatch.put(this, "UpdateRemoteReferences", new Variant(updateRemoteReferences));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getUserControl() {
		return Dispatch.get(this, "UserControl").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param userControl an input-parameter of type boolean
	 */
	public void setUserControl(boolean userControl) {
		Dispatch.put(this, "UserControl", new Variant(userControl));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Variant
	 */
	public Variant getUserStatus() {
		return Dispatch.get(this, "UserStatus");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type CustomViews
	 */
	public CustomViews getCustomViews() {
		return new CustomViews(Dispatch.get(this, "CustomViews").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Windows
	 */
	public Windows getWindows() {
		return new Windows(Dispatch.get(this, "Windows").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Sheets
	 */
	public Sheets getWorksheets() {
		return new Sheets(Dispatch.get(this, "Worksheets").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getWriteReserved() {
		return Dispatch.get(this, "WriteReserved").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getWriteReservedBy() {
		return Dispatch.get(this, "WriteReservedBy").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Sheets
	 */
	public Sheets getExcel4IntlMacroSheets() {
		return new Sheets(Dispatch.get(this, "Excel4IntlMacroSheets").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Sheets
	 */
	public Sheets getExcel4MacroSheets() {
		return new Sheets(Dispatch.get(this, "Excel4MacroSheets").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getTemplateRemoveExtData() {
		return Dispatch.get(this, "TemplateRemoveExtData").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param templateRemoveExtData an input-parameter of type boolean
	 */
	public void setTemplateRemoveExtData(boolean templateRemoveExtData) {
		Dispatch.put(this, "TemplateRemoveExtData", new Variant(templateRemoveExtData));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param when an input-parameter of type Variant
	 * @param who an input-parameter of type Variant
	 * @param where an input-parameter of type Variant
	 */
	public void highlightChangesOptions(Variant when, Variant who, Variant where) {
		Dispatch.call(this, "HighlightChangesOptions", when, who, where);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param when an input-parameter of type Variant
	 * @param who an input-parameter of type Variant
	 */
	public void highlightChangesOptions(Variant when, Variant who) {
		Dispatch.call(this, "HighlightChangesOptions", when, who);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param when an input-parameter of type Variant
	 */
	public void highlightChangesOptions(Variant when) {
		Dispatch.call(this, "HighlightChangesOptions", when);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void highlightChangesOptions() {
		Dispatch.call(this, "HighlightChangesOptions");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param when an input-parameter of type Variant
	 * @param who an input-parameter of type Variant
	 * @param where an input-parameter of type Variant
	 */
	public void highlightChangesOptions(Variant when, Variant who, Variant where) {
		Dispatch.call(this, "HighlightChangesOptions", when, who, where);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getHighlightChangesOnScreen() {
		return Dispatch.get(this, "HighlightChangesOnScreen").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param highlightChangesOnScreen an input-parameter of type boolean
	 */
	public void setHighlightChangesOnScreen(boolean highlightChangesOnScreen) {
		Dispatch.put(this, "HighlightChangesOnScreen", new Variant(highlightChangesOnScreen));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getKeepChangeHistory() {
		return Dispatch.get(this, "KeepChangeHistory").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param keepChangeHistory an input-parameter of type boolean
	 */
	public void setKeepChangeHistory(boolean keepChangeHistory) {
		Dispatch.put(this, "KeepChangeHistory", new Variant(keepChangeHistory));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getListChangesOnNewSheet() {
		return Dispatch.get(this, "ListChangesOnNewSheet").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param listChangesOnNewSheet an input-parameter of type boolean
	 */
	public void setListChangesOnNewSheet(boolean listChangesOnNewSheet) {
		Dispatch.put(this, "ListChangesOnNewSheet", new Variant(listChangesOnNewSheet));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param days an input-parameter of type int
	 * @param sharingPassword an input-parameter of type Variant
	 */
	public void purgeChangeHistoryNow(int days, Variant sharingPassword) {
		Dispatch.call(this, "PurgeChangeHistoryNow", new Variant(days), sharingPassword);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param days an input-parameter of type int
	 */
	public void purgeChangeHistoryNow(int days) {
		Dispatch.call(this, "PurgeChangeHistoryNow", new Variant(days));
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param days an input-parameter of type int
	 * @param sharingPassword an input-parameter of type Variant
	 */
	public void purgeChangeHistoryNow(int days, Variant sharingPassword) {
		Dispatch.call(this, "PurgeChangeHistoryNow", new Variant(days), sharingPassword);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param when an input-parameter of type Variant
	 * @param who an input-parameter of type Variant
	 * @param where an input-parameter of type Variant
	 */
	public void acceptAllChanges(Variant when, Variant who, Variant where) {
		Dispatch.call(this, "AcceptAllChanges", when, who, where);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param when an input-parameter of type Variant
	 * @param who an input-parameter of type Variant
	 */
	public void acceptAllChanges(Variant when, Variant who) {
		Dispatch.call(this, "AcceptAllChanges", when, who);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param when an input-parameter of type Variant
	 */
	public void acceptAllChanges(Variant when) {
		Dispatch.call(this, "AcceptAllChanges", when);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void acceptAllChanges() {
		Dispatch.call(this, "AcceptAllChanges");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param when an input-parameter of type Variant
	 * @param who an input-parameter of type Variant
	 * @param where an input-parameter of type Variant
	 */
	public void acceptAllChanges(Variant when, Variant who, Variant where) {
		Dispatch.call(this, "AcceptAllChanges", when, who, where);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param when an input-parameter of type Variant
	 * @param who an input-parameter of type Variant
	 * @param where an input-parameter of type Variant
	 */
	public void rejectAllChanges(Variant when, Variant who, Variant where) {
		Dispatch.call(this, "RejectAllChanges", when, who, where);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param when an input-parameter of type Variant
	 * @param who an input-parameter of type Variant
	 */
	public void rejectAllChanges(Variant when, Variant who) {
		Dispatch.call(this, "RejectAllChanges", when, who);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param when an input-parameter of type Variant
	 */
	public void rejectAllChanges(Variant when) {
		Dispatch.call(this, "RejectAllChanges", when);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void rejectAllChanges() {
		Dispatch.call(this, "RejectAllChanges");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param when an input-parameter of type Variant
	 * @param who an input-parameter of type Variant
	 * @param where an input-parameter of type Variant
	 */
	public void rejectAllChanges(Variant when, Variant who, Variant where) {
		Dispatch.call(this, "RejectAllChanges", when, who, where);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 * @param tableDestination an input-parameter of type Variant
	 * @param tableName an input-parameter of type Variant
	 * @param rowGrand an input-parameter of type Variant
	 * @param columnGrand an input-parameter of type Variant
	 * @param saveData an input-parameter of type Variant
	 * @param hasAutoFormat an input-parameter of type Variant
	 * @param autoPage an input-parameter of type Variant
	 * @param reserved an input-parameter of type Variant
	 * @param backgroundQuery an input-parameter of type Variant
	 * @param optimizeCache an input-parameter of type Variant
	 * @param pageFieldOrder an input-parameter of type Variant
	 * @param pageFieldWrapCount an input-parameter of type Variant
	 * @param readData an input-parameter of type Variant
	 * @param connection an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData, Variant tableDestination, Variant tableName, Variant rowGrand, Variant columnGrand, Variant saveData, Variant hasAutoFormat, Variant autoPage, Variant reserved, Variant backgroundQuery, Variant optimizeCache, Variant pageFieldOrder, Variant pageFieldWrapCount, Variant readData, Variant connection) {
		Dispatch.callN(this, "PivotTableWizard", new Object[] { sourceType, sourceData, tableDestination, tableName, rowGrand, columnGrand, saveData, hasAutoFormat, autoPage, reserved, backgroundQuery, optimizeCache, pageFieldOrder, pageFieldWrapCount, readData, connection});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 * @param tableDestination an input-parameter of type Variant
	 * @param tableName an input-parameter of type Variant
	 * @param rowGrand an input-parameter of type Variant
	 * @param columnGrand an input-parameter of type Variant
	 * @param saveData an input-parameter of type Variant
	 * @param hasAutoFormat an input-parameter of type Variant
	 * @param autoPage an input-parameter of type Variant
	 * @param reserved an input-parameter of type Variant
	 * @param backgroundQuery an input-parameter of type Variant
	 * @param optimizeCache an input-parameter of type Variant
	 * @param pageFieldOrder an input-parameter of type Variant
	 * @param pageFieldWrapCount an input-parameter of type Variant
	 * @param readData an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData, Variant tableDestination, Variant tableName, Variant rowGrand, Variant columnGrand, Variant saveData, Variant hasAutoFormat, Variant autoPage, Variant reserved, Variant backgroundQuery, Variant optimizeCache, Variant pageFieldOrder, Variant pageFieldWrapCount, Variant readData) {
		Dispatch.callN(this, "PivotTableWizard", new Object[] { sourceType, sourceData, tableDestination, tableName, rowGrand, columnGrand, saveData, hasAutoFormat, autoPage, reserved, backgroundQuery, optimizeCache, pageFieldOrder, pageFieldWrapCount, readData});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 * @param tableDestination an input-parameter of type Variant
	 * @param tableName an input-parameter of type Variant
	 * @param rowGrand an input-parameter of type Variant
	 * @param columnGrand an input-parameter of type Variant
	 * @param saveData an input-parameter of type Variant
	 * @param hasAutoFormat an input-parameter of type Variant
	 * @param autoPage an input-parameter of type Variant
	 * @param reserved an input-parameter of type Variant
	 * @param backgroundQuery an input-parameter of type Variant
	 * @param optimizeCache an input-parameter of type Variant
	 * @param pageFieldOrder an input-parameter of type Variant
	 * @param pageFieldWrapCount an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData, Variant tableDestination, Variant tableName, Variant rowGrand, Variant columnGrand, Variant saveData, Variant hasAutoFormat, Variant autoPage, Variant reserved, Variant backgroundQuery, Variant optimizeCache, Variant pageFieldOrder, Variant pageFieldWrapCount) {
		Dispatch.callN(this, "PivotTableWizard", new Object[] { sourceType, sourceData, tableDestination, tableName, rowGrand, columnGrand, saveData, hasAutoFormat, autoPage, reserved, backgroundQuery, optimizeCache, pageFieldOrder, pageFieldWrapCount});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 * @param tableDestination an input-parameter of type Variant
	 * @param tableName an input-parameter of type Variant
	 * @param rowGrand an input-parameter of type Variant
	 * @param columnGrand an input-parameter of type Variant
	 * @param saveData an input-parameter of type Variant
	 * @param hasAutoFormat an input-parameter of type Variant
	 * @param autoPage an input-parameter of type Variant
	 * @param reserved an input-parameter of type Variant
	 * @param backgroundQuery an input-parameter of type Variant
	 * @param optimizeCache an input-parameter of type Variant
	 * @param pageFieldOrder an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData, Variant tableDestination, Variant tableName, Variant rowGrand, Variant columnGrand, Variant saveData, Variant hasAutoFormat, Variant autoPage, Variant reserved, Variant backgroundQuery, Variant optimizeCache, Variant pageFieldOrder) {
		Dispatch.callN(this, "PivotTableWizard", new Object[] { sourceType, sourceData, tableDestination, tableName, rowGrand, columnGrand, saveData, hasAutoFormat, autoPage, reserved, backgroundQuery, optimizeCache, pageFieldOrder});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 * @param tableDestination an input-parameter of type Variant
	 * @param tableName an input-parameter of type Variant
	 * @param rowGrand an input-parameter of type Variant
	 * @param columnGrand an input-parameter of type Variant
	 * @param saveData an input-parameter of type Variant
	 * @param hasAutoFormat an input-parameter of type Variant
	 * @param autoPage an input-parameter of type Variant
	 * @param reserved an input-parameter of type Variant
	 * @param backgroundQuery an input-parameter of type Variant
	 * @param optimizeCache an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData, Variant tableDestination, Variant tableName, Variant rowGrand, Variant columnGrand, Variant saveData, Variant hasAutoFormat, Variant autoPage, Variant reserved, Variant backgroundQuery, Variant optimizeCache) {
		Dispatch.callN(this, "PivotTableWizard", new Object[] { sourceType, sourceData, tableDestination, tableName, rowGrand, columnGrand, saveData, hasAutoFormat, autoPage, reserved, backgroundQuery, optimizeCache});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 * @param tableDestination an input-parameter of type Variant
	 * @param tableName an input-parameter of type Variant
	 * @param rowGrand an input-parameter of type Variant
	 * @param columnGrand an input-parameter of type Variant
	 * @param saveData an input-parameter of type Variant
	 * @param hasAutoFormat an input-parameter of type Variant
	 * @param autoPage an input-parameter of type Variant
	 * @param reserved an input-parameter of type Variant
	 * @param backgroundQuery an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData, Variant tableDestination, Variant tableName, Variant rowGrand, Variant columnGrand, Variant saveData, Variant hasAutoFormat, Variant autoPage, Variant reserved, Variant backgroundQuery) {
		Dispatch.callN(this, "PivotTableWizard", new Object[] { sourceType, sourceData, tableDestination, tableName, rowGrand, columnGrand, saveData, hasAutoFormat, autoPage, reserved, backgroundQuery});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 * @param tableDestination an input-parameter of type Variant
	 * @param tableName an input-parameter of type Variant
	 * @param rowGrand an input-parameter of type Variant
	 * @param columnGrand an input-parameter of type Variant
	 * @param saveData an input-parameter of type Variant
	 * @param hasAutoFormat an input-parameter of type Variant
	 * @param autoPage an input-parameter of type Variant
	 * @param reserved an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData, Variant tableDestination, Variant tableName, Variant rowGrand, Variant columnGrand, Variant saveData, Variant hasAutoFormat, Variant autoPage, Variant reserved) {
		Dispatch.callN(this, "PivotTableWizard", new Object[] { sourceType, sourceData, tableDestination, tableName, rowGrand, columnGrand, saveData, hasAutoFormat, autoPage, reserved});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 * @param tableDestination an input-parameter of type Variant
	 * @param tableName an input-parameter of type Variant
	 * @param rowGrand an input-parameter of type Variant
	 * @param columnGrand an input-parameter of type Variant
	 * @param saveData an input-parameter of type Variant
	 * @param hasAutoFormat an input-parameter of type Variant
	 * @param autoPage an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData, Variant tableDestination, Variant tableName, Variant rowGrand, Variant columnGrand, Variant saveData, Variant hasAutoFormat, Variant autoPage) {
		Dispatch.callN(this, "PivotTableWizard", new Object[] { sourceType, sourceData, tableDestination, tableName, rowGrand, columnGrand, saveData, hasAutoFormat, autoPage});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 * @param tableDestination an input-parameter of type Variant
	 * @param tableName an input-parameter of type Variant
	 * @param rowGrand an input-parameter of type Variant
	 * @param columnGrand an input-parameter of type Variant
	 * @param saveData an input-parameter of type Variant
	 * @param hasAutoFormat an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData, Variant tableDestination, Variant tableName, Variant rowGrand, Variant columnGrand, Variant saveData, Variant hasAutoFormat) {
		Dispatch.call(this, "PivotTableWizard", sourceType, sourceData, tableDestination, tableName, rowGrand, columnGrand, saveData, hasAutoFormat);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 * @param tableDestination an input-parameter of type Variant
	 * @param tableName an input-parameter of type Variant
	 * @param rowGrand an input-parameter of type Variant
	 * @param columnGrand an input-parameter of type Variant
	 * @param saveData an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData, Variant tableDestination, Variant tableName, Variant rowGrand, Variant columnGrand, Variant saveData) {
		Dispatch.call(this, "PivotTableWizard", sourceType, sourceData, tableDestination, tableName, rowGrand, columnGrand, saveData);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 * @param tableDestination an input-parameter of type Variant
	 * @param tableName an input-parameter of type Variant
	 * @param rowGrand an input-parameter of type Variant
	 * @param columnGrand an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData, Variant tableDestination, Variant tableName, Variant rowGrand, Variant columnGrand) {
		Dispatch.call(this, "PivotTableWizard", sourceType, sourceData, tableDestination, tableName, rowGrand, columnGrand);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 * @param tableDestination an input-parameter of type Variant
	 * @param tableName an input-parameter of type Variant
	 * @param rowGrand an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData, Variant tableDestination, Variant tableName, Variant rowGrand) {
		Dispatch.call(this, "PivotTableWizard", sourceType, sourceData, tableDestination, tableName, rowGrand);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 * @param tableDestination an input-parameter of type Variant
	 * @param tableName an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData, Variant tableDestination, Variant tableName) {
		Dispatch.call(this, "PivotTableWizard", sourceType, sourceData, tableDestination, tableName);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 * @param tableDestination an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData, Variant tableDestination) {
		Dispatch.call(this, "PivotTableWizard", sourceType, sourceData, tableDestination);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData) {
		Dispatch.call(this, "PivotTableWizard", sourceType, sourceData);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType) {
		Dispatch.call(this, "PivotTableWizard", sourceType);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void pivotTableWizard() {
		Dispatch.call(this, "PivotTableWizard");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param sourceType an input-parameter of type Variant
	 * @param sourceData an input-parameter of type Variant
	 * @param tableDestination an input-parameter of type Variant
	 * @param tableName an input-parameter of type Variant
	 * @param rowGrand an input-parameter of type Variant
	 * @param columnGrand an input-parameter of type Variant
	 * @param saveData an input-parameter of type Variant
	 * @param hasAutoFormat an input-parameter of type Variant
	 * @param autoPage an input-parameter of type Variant
	 * @param reserved an input-parameter of type Variant
	 * @param backgroundQuery an input-parameter of type Variant
	 * @param optimizeCache an input-parameter of type Variant
	 * @param pageFieldOrder an input-parameter of type Variant
	 * @param pageFieldWrapCount an input-parameter of type Variant
	 * @param readData an input-parameter of type Variant
	 * @param connection an input-parameter of type Variant
	 */
	public void pivotTableWizard(Variant sourceType, Variant sourceData, Variant tableDestination, Variant tableName, Variant rowGrand, Variant columnGrand, Variant saveData, Variant hasAutoFormat, Variant autoPage, Variant reserved, Variant backgroundQuery, Variant optimizeCache, Variant pageFieldOrder, Variant pageFieldWrapCount, Variant readData, Variant connection) {
		Dispatch.callN(this, "PivotTableWizard", new Object[] { sourceType, sourceData, tableDestination, tableName, rowGrand, columnGrand, saveData, hasAutoFormat, autoPage, reserved, backgroundQuery, optimizeCache, pageFieldOrder, pageFieldWrapCount, readData, connection});

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void resetColors() {
		Dispatch.call(this, "ResetColors");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type VBProject
	 */
	public VBProject getVBProject() {
		return new VBProject(Dispatch.get(this, "VBProject").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param address an input-parameter of type String
	 * @param subAddress an input-parameter of type Variant
	 * @param newWindow an input-parameter of type Variant
	 * @param addHistory an input-parameter of type Variant
	 * @param extraInfo an input-parameter of type Variant
	 * @param method an input-parameter of type Variant
	 * @param headerInfo an input-parameter of type Variant
	 */
	public void followHyperlink(String address, Variant subAddress, Variant newWindow, Variant addHistory, Variant extraInfo, Variant method, Variant headerInfo) {
		Dispatch.call(this, "FollowHyperlink", address, subAddress, newWindow, addHistory, extraInfo, method, headerInfo);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param address an input-parameter of type String
	 * @param subAddress an input-parameter of type Variant
	 * @param newWindow an input-parameter of type Variant
	 * @param addHistory an input-parameter of type Variant
	 * @param extraInfo an input-parameter of type Variant
	 * @param method an input-parameter of type Variant
	 */
	public void followHyperlink(String address, Variant subAddress, Variant newWindow, Variant addHistory, Variant extraInfo, Variant method) {
		Dispatch.call(this, "FollowHyperlink", address, subAddress, newWindow, addHistory, extraInfo, method);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param address an input-parameter of type String
	 * @param subAddress an input-parameter of type Variant
	 * @param newWindow an input-parameter of type Variant
	 * @param addHistory an input-parameter of type Variant
	 * @param extraInfo an input-parameter of type Variant
	 */
	public void followHyperlink(String address, Variant subAddress, Variant newWindow, Variant addHistory, Variant extraInfo) {
		Dispatch.call(this, "FollowHyperlink", address, subAddress, newWindow, addHistory, extraInfo);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param address an input-parameter of type String
	 * @param subAddress an input-parameter of type Variant
	 * @param newWindow an input-parameter of type Variant
	 * @param addHistory an input-parameter of type Variant
	 */
	public void followHyperlink(String address, Variant subAddress, Variant newWindow, Variant addHistory) {
		Dispatch.call(this, "FollowHyperlink", address, subAddress, newWindow, addHistory);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param address an input-parameter of type String
	 * @param subAddress an input-parameter of type Variant
	 * @param newWindow an input-parameter of type Variant
	 */
	public void followHyperlink(String address, Variant subAddress, Variant newWindow) {
		Dispatch.call(this, "FollowHyperlink", address, subAddress, newWindow);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param address an input-parameter of type String
	 * @param subAddress an input-parameter of type Variant
	 */
	public void followHyperlink(String address, Variant subAddress) {
		Dispatch.call(this, "FollowHyperlink", address, subAddress);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param address an input-parameter of type String
	 */
	public void followHyperlink(String address) {
		Dispatch.call(this, "FollowHyperlink", address);
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param address an input-parameter of type String
	 * @param subAddress an input-parameter of type Variant
	 * @param newWindow an input-parameter of type Variant
	 * @param addHistory an input-parameter of type Variant
	 * @param extraInfo an input-parameter of type Variant
	 * @param method an input-parameter of type Variant
	 * @param headerInfo an input-parameter of type Variant
	 */
	public void followHyperlink(String address, Variant subAddress, Variant newWindow, Variant addHistory, Variant extraInfo, Variant method, Variant headerInfo) {
		Dispatch.call(this, "FollowHyperlink", address, subAddress, newWindow, addHistory, extraInfo, method, headerInfo);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void addToFavorites() {
		Dispatch.call(this, "AddToFavorites");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getIsInplace() {
		return Dispatch.get(this, "IsInplace").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 * @param activePrinter an input-parameter of type Variant
	 * @param printToFile an input-parameter of type Variant
	 * @param collate an input-parameter of type Variant
	 * @param prToFileName an input-parameter of type Variant
	 */
	public void _PrintOut(Variant from, Variant to, Variant copies, Variant preview, Variant activePrinter, Variant printToFile, Variant collate, Variant prToFileName) {
		Dispatch.call(this, "_PrintOut", from, to, copies, preview, activePrinter, printToFile, collate, prToFileName);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 * @param activePrinter an input-parameter of type Variant
	 * @param printToFile an input-parameter of type Variant
	 * @param collate an input-parameter of type Variant
	 */
	public void _PrintOut(Variant from, Variant to, Variant copies, Variant preview, Variant activePrinter, Variant printToFile, Variant collate) {
		Dispatch.call(this, "_PrintOut", from, to, copies, preview, activePrinter, printToFile, collate);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 * @param activePrinter an input-parameter of type Variant
	 * @param printToFile an input-parameter of type Variant
	 */
	public void _PrintOut(Variant from, Variant to, Variant copies, Variant preview, Variant activePrinter, Variant printToFile) {
		Dispatch.call(this, "_PrintOut", from, to, copies, preview, activePrinter, printToFile);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 * @param activePrinter an input-parameter of type Variant
	 */
	public void _PrintOut(Variant from, Variant to, Variant copies, Variant preview, Variant activePrinter) {
		Dispatch.call(this, "_PrintOut", from, to, copies, preview, activePrinter);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 */
	public void _PrintOut(Variant from, Variant to, Variant copies, Variant preview) {
		Dispatch.call(this, "_PrintOut", from, to, copies, preview);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 */
	public void _PrintOut(Variant from, Variant to, Variant copies) {
		Dispatch.call(this, "_PrintOut", from, to, copies);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 */
	public void _PrintOut(Variant from, Variant to) {
		Dispatch.call(this, "_PrintOut", from, to);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 */
	public void _PrintOut(Variant from) {
		Dispatch.call(this, "_PrintOut", from);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void _PrintOut() {
		Dispatch.call(this, "_PrintOut");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 * @param activePrinter an input-parameter of type Variant
	 * @param printToFile an input-parameter of type Variant
	 * @param collate an input-parameter of type Variant
	 * @param prToFileName an input-parameter of type Variant
	 */
	public void _PrintOut(Variant from, Variant to, Variant copies, Variant preview, Variant activePrinter, Variant printToFile, Variant collate, Variant prToFileName) {
		Dispatch.call(this, "_PrintOut", from, to, copies, preview, activePrinter, printToFile, collate, prToFileName);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void webPagePreview() {
		Dispatch.call(this, "WebPagePreview");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type PublishObjects
	 */
	public PublishObjects getPublishObjects() {
		return new PublishObjects(Dispatch.get(this, "PublishObjects").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type WebOptions
	 */
	public WebOptions getWebOptions() {
		return new WebOptions(Dispatch.get(this, "WebOptions").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param encoding an input-parameter of type MsoEncoding
	 */
	public void reloadAs(MsoEncoding encoding) {
		Dispatch.call(this, "ReloadAs", encoding);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type HTMLProject
	 */
	public HTMLProject getHTMLProject() {
		return new HTMLProject(Dispatch.get(this, "HTMLProject").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getEnvelopeVisible() {
		return Dispatch.get(this, "EnvelopeVisible").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param envelopeVisible an input-parameter of type boolean
	 */
	public void setEnvelopeVisible(boolean envelopeVisible) {
		Dispatch.put(this, "EnvelopeVisible", new Variant(envelopeVisible));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type int
	 */
	public int getCalculationVersion() {
		return Dispatch.get(this, "CalculationVersion").changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param calcid an input-parameter of type int
	 */
	public void dummy17(int calcid) {
		Dispatch.call(this, "Dummy17", new Variant(calcid));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param s an input-parameter of type String
	 */
	public void sblt(String s) {
		Dispatch.call(this, "sblt", s);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getVBASigned() {
		return Dispatch.get(this, "VBASigned").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getShowPivotTableFieldList() {
		return Dispatch.get(this, "ShowPivotTableFieldList").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param showPivotTableFieldList an input-parameter of type boolean
	 */
	public void setShowPivotTableFieldList(boolean showPivotTableFieldList) {
		Dispatch.put(this, "ShowPivotTableFieldList", new Variant(showPivotTableFieldList));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type int
	 */
	public int getUpdateLinks() {
		return Dispatch.get(this, "UpdateLinks").changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param updateLinks an input-parameter of type int
	 */
	public void setUpdateLinks(int updateLinks) {
		Dispatch.put(this, "UpdateLinks", new Variant(updateLinks));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param name an input-parameter of type String
	 * @param type an input-parameter of type int
	 */
	public void breakLink(String name, int type) {
		Dispatch.call(this, "BreakLink", name, new Variant(type));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void dummy16() {
		Dispatch.call(this, "Dummy16");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param accessMode an input-parameter of type int
	 * @param conflictResolution an input-parameter of type Variant
	 * @param addToMru an input-parameter of type Variant
	 * @param textCodepage an input-parameter of type Variant
	 * @param textVisualLayout an input-parameter of type Variant
	 * @param local an input-parameter of type Variant
	 */
	public void saveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, int accessMode, Variant conflictResolution, Variant addToMru, Variant textCodepage, Variant textVisualLayout, Variant local) {
		Dispatch.callN(this, "SaveAs", new Object[] { filename, fileFormat, password, writeResPassword, readOnlyRecommended, createBackup, new Variant(accessMode), conflictResolution, addToMru, textCodepage, textVisualLayout, local});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param accessMode an input-parameter of type int
	 * @param conflictResolution an input-parameter of type Variant
	 * @param addToMru an input-parameter of type Variant
	 * @param textCodepage an input-parameter of type Variant
	 * @param textVisualLayout an input-parameter of type Variant
	 */
	public void saveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, int accessMode, Variant conflictResolution, Variant addToMru, Variant textCodepage, Variant textVisualLayout) {
		Dispatch.callN(this, "SaveAs", new Object[] { filename, fileFormat, password, writeResPassword, readOnlyRecommended, createBackup, new Variant(accessMode), conflictResolution, addToMru, textCodepage, textVisualLayout});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param accessMode an input-parameter of type int
	 * @param conflictResolution an input-parameter of type Variant
	 * @param addToMru an input-parameter of type Variant
	 * @param textCodepage an input-parameter of type Variant
	 */
	public void saveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, int accessMode, Variant conflictResolution, Variant addToMru, Variant textCodepage) {
		Dispatch.callN(this, "SaveAs", new Object[] { filename, fileFormat, password, writeResPassword, readOnlyRecommended, createBackup, new Variant(accessMode), conflictResolution, addToMru, textCodepage});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param accessMode an input-parameter of type int
	 * @param conflictResolution an input-parameter of type Variant
	 * @param addToMru an input-parameter of type Variant
	 */
	public void saveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, int accessMode, Variant conflictResolution, Variant addToMru) {
		Dispatch.callN(this, "SaveAs", new Object[] { filename, fileFormat, password, writeResPassword, readOnlyRecommended, createBackup, new Variant(accessMode), conflictResolution, addToMru});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param accessMode an input-parameter of type int
	 * @param conflictResolution an input-parameter of type Variant
	 */
	public void saveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, int accessMode, Variant conflictResolution) {
		Dispatch.call(this, "SaveAs", filename, fileFormat, password, writeResPassword, readOnlyRecommended, createBackup, new Variant(accessMode), conflictResolution);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param accessMode an input-parameter of type int
	 */
	public void saveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, int accessMode) {
		Dispatch.call(this, "SaveAs", filename, fileFormat, password, writeResPassword, readOnlyRecommended, createBackup, new Variant(accessMode));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 */
	public void saveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup) {
		Dispatch.call(this, "SaveAs", filename, fileFormat, password, writeResPassword, readOnlyRecommended, createBackup);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 */
	public void saveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended) {
		Dispatch.call(this, "SaveAs", filename, fileFormat, password, writeResPassword, readOnlyRecommended);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 */
	public void saveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword) {
		Dispatch.call(this, "SaveAs", filename, fileFormat, password, writeResPassword);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 */
	public void saveAs(Variant filename, Variant fileFormat, Variant password) {
		Dispatch.call(this, "SaveAs", filename, fileFormat, password);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 */
	public void saveAs(Variant filename, Variant fileFormat) {
		Dispatch.call(this, "SaveAs", filename, fileFormat);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 */
	public void saveAs(Variant filename) {
		Dispatch.call(this, "SaveAs", filename);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void saveAs() {
		Dispatch.call(this, "SaveAs");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param accessMode an input-parameter of type int
	 * @param conflictResolution an input-parameter of type Variant
	 * @param addToMru an input-parameter of type Variant
	 * @param textCodepage an input-parameter of type Variant
	 * @param textVisualLayout an input-parameter of type Variant
	 * @param local an input-parameter of type Variant
	 */
	public void saveAs(Variant filename, Variant fileFormat, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, int accessMode, Variant conflictResolution, Variant addToMru, Variant textCodepage, Variant textVisualLayout, Variant local) {
		Dispatch.callN(this, "SaveAs", new Object[] { filename, fileFormat, password, writeResPassword, readOnlyRecommended, createBackup, new Variant(accessMode), conflictResolution, addToMru, textCodepage, textVisualLayout, local});

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getEnableAutoRecover() {
		return Dispatch.get(this, "EnableAutoRecover").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param enableAutoRecover an input-parameter of type boolean
	 */
	public void setEnableAutoRecover(boolean enableAutoRecover) {
		Dispatch.put(this, "EnableAutoRecover", new Variant(enableAutoRecover));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getRemovePersonalInformation() {
		return Dispatch.get(this, "RemovePersonalInformation").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param removePersonalInformation an input-parameter of type boolean
	 */
	public void setRemovePersonalInformation(boolean removePersonalInformation) {
		Dispatch.put(this, "RemovePersonalInformation", new Variant(removePersonalInformation));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getFullNameURLEncoded() {
		return Dispatch.get(this, "FullNameURLEncoded").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param saveChanges an input-parameter of type Variant
	 * @param comments an input-parameter of type Variant
	 * @param makePublic an input-parameter of type Variant
	 */
	public void checkIn(Variant saveChanges, Variant comments, Variant makePublic) {
		Dispatch.call(this, "CheckIn", saveChanges, comments, makePublic);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param saveChanges an input-parameter of type Variant
	 * @param comments an input-parameter of type Variant
	 */
	public void checkIn(Variant saveChanges, Variant comments) {
		Dispatch.call(this, "CheckIn", saveChanges, comments);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param saveChanges an input-parameter of type Variant
	 */
	public void checkIn(Variant saveChanges) {
		Dispatch.call(this, "CheckIn", saveChanges);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void checkIn() {
		Dispatch.call(this, "CheckIn");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param saveChanges an input-parameter of type Variant
	 * @param comments an input-parameter of type Variant
	 * @param makePublic an input-parameter of type Variant
	 */
	public void checkIn(Variant saveChanges, Variant comments, Variant makePublic) {
		Dispatch.call(this, "CheckIn", saveChanges, comments, makePublic);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean canCheckIn() {
		return Dispatch.call(this, "CanCheckIn").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param recipients an input-parameter of type Variant
	 * @param subject an input-parameter of type Variant
	 * @param showMessage an input-parameter of type Variant
	 * @param includeAttachment an input-parameter of type Variant
	 */
	public void sendForReview(Variant recipients, Variant subject, Variant showMessage, Variant includeAttachment) {
		Dispatch.call(this, "SendForReview", recipients, subject, showMessage, includeAttachment);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param recipients an input-parameter of type Variant
	 * @param subject an input-parameter of type Variant
	 * @param showMessage an input-parameter of type Variant
	 */
	public void sendForReview(Variant recipients, Variant subject, Variant showMessage) {
		Dispatch.call(this, "SendForReview", recipients, subject, showMessage);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param recipients an input-parameter of type Variant
	 * @param subject an input-parameter of type Variant
	 */
	public void sendForReview(Variant recipients, Variant subject) {
		Dispatch.call(this, "SendForReview", recipients, subject);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param recipients an input-parameter of type Variant
	 */
	public void sendForReview(Variant recipients) {
		Dispatch.call(this, "SendForReview", recipients);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void sendForReview() {
		Dispatch.call(this, "SendForReview");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param recipients an input-parameter of type Variant
	 * @param subject an input-parameter of type Variant
	 * @param showMessage an input-parameter of type Variant
	 * @param includeAttachment an input-parameter of type Variant
	 */
	public void sendForReview(Variant recipients, Variant subject, Variant showMessage, Variant includeAttachment) {
		Dispatch.call(this, "SendForReview", recipients, subject, showMessage, includeAttachment);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param showMessage an input-parameter of type Variant
	 */
	public void replyWithChanges(Variant showMessage) {
		Dispatch.call(this, "ReplyWithChanges", showMessage);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void replyWithChanges() {
		Dispatch.call(this, "ReplyWithChanges");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param showMessage an input-parameter of type Variant
	 */
	public void replyWithChanges(Variant showMessage) {
		Dispatch.call(this, "ReplyWithChanges", showMessage);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void endReview() {
		Dispatch.call(this, "EndReview");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getPassword() {
		return Dispatch.get(this, "Password").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param password an input-parameter of type String
	 */
	public void setPassword(String password) {
		Dispatch.put(this, "Password", password);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getWritePassword() {
		return Dispatch.get(this, "WritePassword").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param writePassword an input-parameter of type String
	 */
	public void setWritePassword(String writePassword) {
		Dispatch.put(this, "WritePassword", writePassword);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getPasswordEncryptionProvider() {
		return Dispatch.get(this, "PasswordEncryptionProvider").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getPasswordEncryptionAlgorithm() {
		return Dispatch.get(this, "PasswordEncryptionAlgorithm").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type int
	 */
	public int getPasswordEncryptionKeyLength() {
		return Dispatch.get(this, "PasswordEncryptionKeyLength").changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param passwordEncryptionProvider an input-parameter of type Variant
	 * @param passwordEncryptionAlgorithm an input-parameter of type Variant
	 * @param passwordEncryptionKeyLength an input-parameter of type Variant
	 * @param passwordEncryptionFileProperties an input-parameter of type Variant
	 */
	public void setPasswordEncryptionOptions(Variant passwordEncryptionProvider, Variant passwordEncryptionAlgorithm, Variant passwordEncryptionKeyLength, Variant passwordEncryptionFileProperties) {
		Dispatch.call(this, "SetPasswordEncryptionOptions", passwordEncryptionProvider, passwordEncryptionAlgorithm, passwordEncryptionKeyLength, passwordEncryptionFileProperties);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param passwordEncryptionProvider an input-parameter of type Variant
	 * @param passwordEncryptionAlgorithm an input-parameter of type Variant
	 * @param passwordEncryptionKeyLength an input-parameter of type Variant
	 */
	public void setPasswordEncryptionOptions(Variant passwordEncryptionProvider, Variant passwordEncryptionAlgorithm, Variant passwordEncryptionKeyLength) {
		Dispatch.call(this, "SetPasswordEncryptionOptions", passwordEncryptionProvider, passwordEncryptionAlgorithm, passwordEncryptionKeyLength);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param passwordEncryptionProvider an input-parameter of type Variant
	 * @param passwordEncryptionAlgorithm an input-parameter of type Variant
	 */
	public void setPasswordEncryptionOptions(Variant passwordEncryptionProvider, Variant passwordEncryptionAlgorithm) {
		Dispatch.call(this, "SetPasswordEncryptionOptions", passwordEncryptionProvider, passwordEncryptionAlgorithm);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param passwordEncryptionProvider an input-parameter of type Variant
	 */
	public void setPasswordEncryptionOptions(Variant passwordEncryptionProvider) {
		Dispatch.call(this, "SetPasswordEncryptionOptions", passwordEncryptionProvider);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void setPasswordEncryptionOptions() {
		Dispatch.call(this, "SetPasswordEncryptionOptions");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param passwordEncryptionProvider an input-parameter of type Variant
	 * @param passwordEncryptionAlgorithm an input-parameter of type Variant
	 * @param passwordEncryptionKeyLength an input-parameter of type Variant
	 * @param passwordEncryptionFileProperties an input-parameter of type Variant
	 */
	public void setPasswordEncryptionOptions(Variant passwordEncryptionProvider, Variant passwordEncryptionAlgorithm, Variant passwordEncryptionKeyLength, Variant passwordEncryptionFileProperties) {
		Dispatch.call(this, "SetPasswordEncryptionOptions", passwordEncryptionProvider, passwordEncryptionAlgorithm, passwordEncryptionKeyLength, passwordEncryptionFileProperties);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getPasswordEncryptionFileProperties() {
		return Dispatch.get(this, "PasswordEncryptionFileProperties").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getReadOnlyRecommended() {
		return Dispatch.get(this, "ReadOnlyRecommended").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param readOnlyRecommended an input-parameter of type boolean
	 */
	public void setReadOnlyRecommended(boolean readOnlyRecommended) {
		Dispatch.put(this, "ReadOnlyRecommended", new Variant(readOnlyRecommended));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param password an input-parameter of type Variant
	 * @param structure an input-parameter of type Variant
	 * @param windows an input-parameter of type Variant
	 */
	public void protect(Variant password, Variant structure, Variant windows) {
		Dispatch.call(this, "Protect", password, structure, windows);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param password an input-parameter of type Variant
	 * @param structure an input-parameter of type Variant
	 */
	public void protect(Variant password, Variant structure) {
		Dispatch.call(this, "Protect", password, structure);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param password an input-parameter of type Variant
	 */
	public void protect(Variant password) {
		Dispatch.call(this, "Protect", password);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void protect() {
		Dispatch.call(this, "Protect");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param password an input-parameter of type Variant
	 * @param structure an input-parameter of type Variant
	 * @param windows an input-parameter of type Variant
	 */
	public void protect(Variant password, Variant structure, Variant windows) {
		Dispatch.call(this, "Protect", password, structure, windows);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type SmartTagOptions
	 */
	public SmartTagOptions getSmartTagOptions() {
		return new SmartTagOptions(Dispatch.get(this, "SmartTagOptions").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void recheckSmartTags() {
		Dispatch.call(this, "RecheckSmartTags");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Permission
	 */
	public Permission getPermission() {
		return new Permission(Dispatch.get(this, "Permission").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type SharedWorkspace
	 */
	public SharedWorkspace getSharedWorkspace() {
		return new SharedWorkspace(Dispatch.get(this, "SharedWorkspace").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Sync
	 */
	public Sync getSync() {
		return new Sync(Dispatch.get(this, "Sync").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param recipients an input-parameter of type Variant
	 * @param subject an input-parameter of type Variant
	 * @param showMessage an input-parameter of type Variant
	 */
	public void sendFaxOverInternet(Variant recipients, Variant subject, Variant showMessage) {
		Dispatch.call(this, "SendFaxOverInternet", recipients, subject, showMessage);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param recipients an input-parameter of type Variant
	 * @param subject an input-parameter of type Variant
	 */
	public void sendFaxOverInternet(Variant recipients, Variant subject) {
		Dispatch.call(this, "SendFaxOverInternet", recipients, subject);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param recipients an input-parameter of type Variant
	 */
	public void sendFaxOverInternet(Variant recipients) {
		Dispatch.call(this, "SendFaxOverInternet", recipients);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void sendFaxOverInternet() {
		Dispatch.call(this, "SendFaxOverInternet");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param recipients an input-parameter of type Variant
	 * @param subject an input-parameter of type Variant
	 * @param showMessage an input-parameter of type Variant
	 */
	public void sendFaxOverInternet(Variant recipients, Variant subject, Variant showMessage) {
		Dispatch.call(this, "SendFaxOverInternet", recipients, subject, showMessage);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type XmlNamespaces
	 */
	public XmlNamespaces getXmlNamespaces() {
		return new XmlNamespaces(Dispatch.get(this, "XmlNamespaces").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type XmlMaps
	 */
	public XmlMaps getXmlMaps() {
		return new XmlMaps(Dispatch.get(this, "XmlMaps").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param url an input-parameter of type String
	 * @param importMap an input-parameter of type VT_PTR
	 * @param overwrite an input-parameter of type Variant
	 * @param destination an input-parameter of type Variant
	 * @return the result is of type int
	 */
	public int xmlImport(String url, VT_PTR importMap, Variant overwrite, Variant destination) {
		return Dispatch.call(this, "XmlImport", url, importMap, overwrite, destination).changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param url an input-parameter of type String
	 * @param importMap an input-parameter of type VT_PTR
	 * @param overwrite an input-parameter of type Variant
	 * @return the result is of type int
	 */
	public int xmlImport(String url, VT_PTR importMap, Variant overwrite) {
		return Dispatch.call(this, "XmlImport", url, importMap, overwrite).changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param url an input-parameter of type String
	 * @param importMap an input-parameter of type VT_PTR
	 * @return the result is of type int
	 */
	public int xmlImport(String url, VT_PTR importMap) {
		return Dispatch.call(this, "XmlImport", url, importMap).changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param url an input-parameter of type String
	 * @param importMap is an one-element array which sends the input-parameter
	 *                  to the ActiveX-Component and receives the output-parameter
	 * @param overwrite an input-parameter of type Variant
	 * @param destination an input-parameter of type Variant
	 * @return the result is of type int
	 */
	public int xmlImport(String url, VT_PTR[] importMap, Variant overwrite, Variant destination) {
		Variant vnt_importMap = new Variant();
		if( importMap == null || importMap.length == 0 )
			vnt_importMap.putNoParam();
		else
			vnt_importMap.putVT_PTRRef(importMap[0]);

		int result_of_XmlImport = Dispatch.call(this, "XmlImport", url, vnt_importMap, overwrite, destination).changeType(Variant.VariantInt).getInt();

		if( importMap != null && importMap.length > 0 )
			importMap[0] = vnt_importMap.toVT_PTR();

		return result_of_XmlImport;
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type SmartDocument
	 */
	public SmartDocument getSmartDocument() {
		return new SmartDocument(Dispatch.get(this, "SmartDocument").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type DocumentLibraryVersions
	 */
	public DocumentLibraryVersions getDocumentLibraryVersions() {
		return new DocumentLibraryVersions(Dispatch.get(this, "DocumentLibraryVersions").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getInactiveListBorderVisible() {
		return Dispatch.get(this, "InactiveListBorderVisible").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param inactiveListBorderVisible an input-parameter of type boolean
	 */
	public void setInactiveListBorderVisible(boolean inactiveListBorderVisible) {
		Dispatch.put(this, "InactiveListBorderVisible", new Variant(inactiveListBorderVisible));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getDisplayInkComments() {
		return Dispatch.get(this, "DisplayInkComments").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param displayInkComments an input-parameter of type boolean
	 */
	public void setDisplayInkComments(boolean displayInkComments) {
		Dispatch.put(this, "DisplayInkComments", new Variant(displayInkComments));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param data an input-parameter of type String
	 * @param importMap an input-parameter of type VT_PTR
	 * @param overwrite an input-parameter of type Variant
	 * @param destination an input-parameter of type Variant
	 * @return the result is of type int
	 */
	public int xmlImportXml(String data, VT_PTR importMap, Variant overwrite, Variant destination) {
		return Dispatch.call(this, "XmlImportXml", data, importMap, overwrite, destination).changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param data an input-parameter of type String
	 * @param importMap an input-parameter of type VT_PTR
	 * @param overwrite an input-parameter of type Variant
	 * @return the result is of type int
	 */
	public int xmlImportXml(String data, VT_PTR importMap, Variant overwrite) {
		return Dispatch.call(this, "XmlImportXml", data, importMap, overwrite).changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param data an input-parameter of type String
	 * @param importMap an input-parameter of type VT_PTR
	 * @return the result is of type int
	 */
	public int xmlImportXml(String data, VT_PTR importMap) {
		return Dispatch.call(this, "XmlImportXml", data, importMap).changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param data an input-parameter of type String
	 * @param importMap is an one-element array which sends the input-parameter
	 *                  to the ActiveX-Component and receives the output-parameter
	 * @param overwrite an input-parameter of type Variant
	 * @param destination an input-parameter of type Variant
	 * @return the result is of type int
	 */
	public int xmlImportXml(String data, VT_PTR[] importMap, Variant overwrite, Variant destination) {
		Variant vnt_importMap = new Variant();
		if( importMap == null || importMap.length == 0 )
			vnt_importMap.putNoParam();
		else
			vnt_importMap.putVT_PTRRef(importMap[0]);

		int result_of_XmlImportXml = Dispatch.call(this, "XmlImportXml", data, vnt_importMap, overwrite, destination).changeType(Variant.VariantInt).getInt();

		if( importMap != null && importMap.length > 0 )
			importMap[0] = vnt_importMap.toVT_PTR();

		return result_of_XmlImportXml;
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type String
	 * @param map an input-parameter of type XmlMap
	 */
	public void saveAsXMLData(String filename, XmlMap map) {
		Dispatch.call(this, "SaveAsXMLData", filename, map);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void toggleFormsDesign() {
		Dispatch.call(this, "ToggleFormsDesign");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type MetaProperties
	 */
	public MetaProperties getContentTypeProperties() {
		return new MetaProperties(Dispatch.get(this, "ContentTypeProperties").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Connections
	 */
	public Connections getConnections() {
		return new Connections(Dispatch.get(this, "Connections").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param removeDocInfoType an input-parameter of type int
	 */
	public void removeDocumentInformation(int removeDocInfoType) {
		Dispatch.call(this, "RemoveDocumentInformation", new Variant(removeDocInfoType));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type SignatureSet
	 */
	public SignatureSet getSignatures() {
		return new SignatureSet(Dispatch.get(this, "Signatures").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param saveChanges an input-parameter of type Variant
	 * @param comments an input-parameter of type Variant
	 * @param makePublic an input-parameter of type Variant
	 * @param versionType an input-parameter of type Variant
	 */
	public void checkInWithVersion(Variant saveChanges, Variant comments, Variant makePublic, Variant versionType) {
		Dispatch.call(this, "CheckInWithVersion", saveChanges, comments, makePublic, versionType);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param saveChanges an input-parameter of type Variant
	 * @param comments an input-parameter of type Variant
	 * @param makePublic an input-parameter of type Variant
	 */
	public void checkInWithVersion(Variant saveChanges, Variant comments, Variant makePublic) {
		Dispatch.call(this, "CheckInWithVersion", saveChanges, comments, makePublic);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param saveChanges an input-parameter of type Variant
	 * @param comments an input-parameter of type Variant
	 */
	public void checkInWithVersion(Variant saveChanges, Variant comments) {
		Dispatch.call(this, "CheckInWithVersion", saveChanges, comments);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param saveChanges an input-parameter of type Variant
	 */
	public void checkInWithVersion(Variant saveChanges) {
		Dispatch.call(this, "CheckInWithVersion", saveChanges);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void checkInWithVersion() {
		Dispatch.call(this, "CheckInWithVersion");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param saveChanges an input-parameter of type Variant
	 * @param comments an input-parameter of type Variant
	 * @param makePublic an input-parameter of type Variant
	 * @param versionType an input-parameter of type Variant
	 */
	public void checkInWithVersion(Variant saveChanges, Variant comments, Variant makePublic, Variant versionType) {
		Dispatch.call(this, "CheckInWithVersion", saveChanges, comments, makePublic, versionType);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type ServerPolicy
	 */
	public ServerPolicy getServerPolicy() {
		return new ServerPolicy(Dispatch.get(this, "ServerPolicy").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void lockServerFile() {
		Dispatch.call(this, "LockServerFile");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type DocumentInspectors
	 */
	public DocumentInspectors getDocumentInspectors() {
		return new DocumentInspectors(Dispatch.get(this, "DocumentInspectors").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type WorkflowTasks
	 */
	public WorkflowTasks getWorkflowTasks() {
		return new WorkflowTasks(Dispatch.call(this, "GetWorkflowTasks").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type WorkflowTemplates
	 */
	public WorkflowTemplates getWorkflowTemplates() {
		return new WorkflowTemplates(Dispatch.call(this, "GetWorkflowTemplates").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 * @param activePrinter an input-parameter of type Variant
	 * @param printToFile an input-parameter of type Variant
	 * @param collate an input-parameter of type Variant
	 * @param prToFileName an input-parameter of type Variant
	 * @param ignorePrintAreas an input-parameter of type Variant
	 */
	public void printOut(Variant from, Variant to, Variant copies, Variant preview, Variant activePrinter, Variant printToFile, Variant collate, Variant prToFileName, Variant ignorePrintAreas) {
		Dispatch.callN(this, "PrintOut", new Object[] { from, to, copies, preview, activePrinter, printToFile, collate, prToFileName, ignorePrintAreas});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 * @param activePrinter an input-parameter of type Variant
	 * @param printToFile an input-parameter of type Variant
	 * @param collate an input-parameter of type Variant
	 * @param prToFileName an input-parameter of type Variant
	 */
	public void printOut(Variant from, Variant to, Variant copies, Variant preview, Variant activePrinter, Variant printToFile, Variant collate, Variant prToFileName) {
		Dispatch.call(this, "PrintOut", from, to, copies, preview, activePrinter, printToFile, collate, prToFileName);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 * @param activePrinter an input-parameter of type Variant
	 * @param printToFile an input-parameter of type Variant
	 * @param collate an input-parameter of type Variant
	 */
	public void printOut(Variant from, Variant to, Variant copies, Variant preview, Variant activePrinter, Variant printToFile, Variant collate) {
		Dispatch.call(this, "PrintOut", from, to, copies, preview, activePrinter, printToFile, collate);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 * @param activePrinter an input-parameter of type Variant
	 * @param printToFile an input-parameter of type Variant
	 */
	public void printOut(Variant from, Variant to, Variant copies, Variant preview, Variant activePrinter, Variant printToFile) {
		Dispatch.call(this, "PrintOut", from, to, copies, preview, activePrinter, printToFile);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 * @param activePrinter an input-parameter of type Variant
	 */
	public void printOut(Variant from, Variant to, Variant copies, Variant preview, Variant activePrinter) {
		Dispatch.call(this, "PrintOut", from, to, copies, preview, activePrinter);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 */
	public void printOut(Variant from, Variant to, Variant copies, Variant preview) {
		Dispatch.call(this, "PrintOut", from, to, copies, preview);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 */
	public void printOut(Variant from, Variant to, Variant copies) {
		Dispatch.call(this, "PrintOut", from, to, copies);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 */
	public void printOut(Variant from, Variant to) {
		Dispatch.call(this, "PrintOut", from, to);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param from an input-parameter of type Variant
	 */
	public void printOut(Variant from) {
		Dispatch.call(this, "PrintOut", from);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void printOut() {
		Dispatch.call(this, "PrintOut");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param copies an input-parameter of type Variant
	 * @param preview an input-parameter of type Variant
	 * @param activePrinter an input-parameter of type Variant
	 * @param printToFile an input-parameter of type Variant
	 * @param collate an input-parameter of type Variant
	 * @param prToFileName an input-parameter of type Variant
	 * @param ignorePrintAreas an input-parameter of type Variant
	 */
	public void printOut(Variant from, Variant to, Variant copies, Variant preview, Variant activePrinter, Variant printToFile, Variant collate, Variant prToFileName, Variant ignorePrintAreas) {
		Dispatch.callN(this, "PrintOut", new Object[] { from, to, copies, preview, activePrinter, printToFile, collate, prToFileName, ignorePrintAreas});

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type ServerViewableItems
	 */
	public ServerViewableItems getServerViewableItems() {
		return new ServerViewableItems(Dispatch.get(this, "ServerViewableItems").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type TableStyles
	 */
	public TableStyles getTableStyles() {
		return new TableStyles(Dispatch.get(this, "TableStyles").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Variant
	 */
	public Variant getDefaultTableStyle() {
		return Dispatch.get(this, "DefaultTableStyle");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param defaultTableStyle an input-parameter of type Variant
	 */
	public void setDefaultTableStyle(Variant defaultTableStyle) {
		Dispatch.put(this, "DefaultTableStyle", defaultTableStyle);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Variant
	 */
	public Variant getDefaultPivotTableStyle() {
		return Dispatch.get(this, "DefaultPivotTableStyle");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param defaultPivotTableStyle an input-parameter of type Variant
	 */
	public void setDefaultPivotTableStyle(Variant defaultPivotTableStyle) {
		Dispatch.put(this, "DefaultPivotTableStyle", defaultPivotTableStyle);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getCheckCompatibility() {
		return Dispatch.get(this, "CheckCompatibility").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param checkCompatibility an input-parameter of type boolean
	 */
	public void setCheckCompatibility(boolean checkCompatibility) {
		Dispatch.put(this, "CheckCompatibility", new Variant(checkCompatibility));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getHasVBProject() {
		return Dispatch.get(this, "HasVBProject").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type CustomXMLParts
	 */
	public CustomXMLParts getCustomXMLParts() {
		return new CustomXMLParts(Dispatch.get(this, "CustomXMLParts").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getFinal() {
		return Dispatch.get(this, "Final").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param final an input-parameter of type boolean
	 */
	public void setFinal(boolean final) {
		Dispatch.put(this, "Final", new Variant(final));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Research
	 */
	public Research getResearch() {
		return new Research(Dispatch.get(this, "Research").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type OfficeTheme
	 */
	public OfficeTheme getTheme() {
		return new OfficeTheme(Dispatch.get(this, "Theme").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type String
	 */
	public void applyTheme(String filename) {
		Dispatch.call(this, "ApplyTheme", filename);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getExcel8CompatibilityMode() {
		return Dispatch.get(this, "Excel8CompatibilityMode").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getConnectionsDisabled() {
		return Dispatch.get(this, "ConnectionsDisabled").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void enableConnections() {
		Dispatch.call(this, "EnableConnections");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getShowPivotChartActiveFields() {
		return Dispatch.get(this, "ShowPivotChartActiveFields").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param showPivotChartActiveFields an input-parameter of type boolean
	 */
	public void setShowPivotChartActiveFields(boolean showPivotChartActiveFields) {
		Dispatch.put(this, "ShowPivotChartActiveFields", new Variant(showPivotChartActiveFields));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param type an input-parameter of type int
	 * @param filename an input-parameter of type Variant
	 * @param quality an input-parameter of type Variant
	 * @param includeDocProperties an input-parameter of type Variant
	 * @param ignorePrintAreas an input-parameter of type Variant
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param openAfterPublish an input-parameter of type Variant
	 * @param fixedFormatExtClassPtr an input-parameter of type Variant
	 */
	public void exportAsFixedFormat(int type, Variant filename, Variant quality, Variant includeDocProperties, Variant ignorePrintAreas, Variant from, Variant to, Variant openAfterPublish, Variant fixedFormatExtClassPtr) {
		Dispatch.callN(this, "ExportAsFixedFormat", new Object[] { new Variant(type), filename, quality, includeDocProperties, ignorePrintAreas, from, to, openAfterPublish, fixedFormatExtClassPtr});
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param type an input-parameter of type int
	 * @param filename an input-parameter of type Variant
	 * @param quality an input-parameter of type Variant
	 * @param includeDocProperties an input-parameter of type Variant
	 * @param ignorePrintAreas an input-parameter of type Variant
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param openAfterPublish an input-parameter of type Variant
	 */
	public void exportAsFixedFormat(int type, Variant filename, Variant quality, Variant includeDocProperties, Variant ignorePrintAreas, Variant from, Variant to, Variant openAfterPublish) {
		Dispatch.call(this, "ExportAsFixedFormat", new Variant(type), filename, quality, includeDocProperties, ignorePrintAreas, from, to, openAfterPublish);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param type an input-parameter of type int
	 * @param filename an input-parameter of type Variant
	 * @param quality an input-parameter of type Variant
	 * @param includeDocProperties an input-parameter of type Variant
	 * @param ignorePrintAreas an input-parameter of type Variant
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 */
	public void exportAsFixedFormat(int type, Variant filename, Variant quality, Variant includeDocProperties, Variant ignorePrintAreas, Variant from, Variant to) {
		Dispatch.call(this, "ExportAsFixedFormat", new Variant(type), filename, quality, includeDocProperties, ignorePrintAreas, from, to);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param type an input-parameter of type int
	 * @param filename an input-parameter of type Variant
	 * @param quality an input-parameter of type Variant
	 * @param includeDocProperties an input-parameter of type Variant
	 * @param ignorePrintAreas an input-parameter of type Variant
	 * @param from an input-parameter of type Variant
	 */
	public void exportAsFixedFormat(int type, Variant filename, Variant quality, Variant includeDocProperties, Variant ignorePrintAreas, Variant from) {
		Dispatch.call(this, "ExportAsFixedFormat", new Variant(type), filename, quality, includeDocProperties, ignorePrintAreas, from);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param type an input-parameter of type int
	 * @param filename an input-parameter of type Variant
	 * @param quality an input-parameter of type Variant
	 * @param includeDocProperties an input-parameter of type Variant
	 * @param ignorePrintAreas an input-parameter of type Variant
	 */
	public void exportAsFixedFormat(int type, Variant filename, Variant quality, Variant includeDocProperties, Variant ignorePrintAreas) {
		Dispatch.call(this, "ExportAsFixedFormat", new Variant(type), filename, quality, includeDocProperties, ignorePrintAreas);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param type an input-parameter of type int
	 * @param filename an input-parameter of type Variant
	 * @param quality an input-parameter of type Variant
	 * @param includeDocProperties an input-parameter of type Variant
	 */
	public void exportAsFixedFormat(int type, Variant filename, Variant quality, Variant includeDocProperties) {
		Dispatch.call(this, "ExportAsFixedFormat", new Variant(type), filename, quality, includeDocProperties);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param type an input-parameter of type int
	 * @param filename an input-parameter of type Variant
	 * @param quality an input-parameter of type Variant
	 */
	public void exportAsFixedFormat(int type, Variant filename, Variant quality) {
		Dispatch.call(this, "ExportAsFixedFormat", new Variant(type), filename, quality);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param type an input-parameter of type int
	 * @param filename an input-parameter of type Variant
	 */
	public void exportAsFixedFormat(int type, Variant filename) {
		Dispatch.call(this, "ExportAsFixedFormat", new Variant(type), filename);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param type an input-parameter of type int
	 */
	public void exportAsFixedFormat(int type) {
		Dispatch.call(this, "ExportAsFixedFormat", new Variant(type));
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param type an input-parameter of type int
	 * @param filename an input-parameter of type Variant
	 * @param quality an input-parameter of type Variant
	 * @param includeDocProperties an input-parameter of type Variant
	 * @param ignorePrintAreas an input-parameter of type Variant
	 * @param from an input-parameter of type Variant
	 * @param to an input-parameter of type Variant
	 * @param openAfterPublish an input-parameter of type Variant
	 * @param fixedFormatExtClassPtr an input-parameter of type Variant
	 */
	public void exportAsFixedFormat(int type, Variant filename, Variant quality, Variant includeDocProperties, Variant ignorePrintAreas, Variant from, Variant to, Variant openAfterPublish, Variant fixedFormatExtClassPtr) {
		Dispatch.callN(this, "ExportAsFixedFormat", new Object[] { new Variant(type), filename, quality, includeDocProperties, ignorePrintAreas, from, to, openAfterPublish, fixedFormatExtClassPtr});

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type IconSets
	 */
	public IconSets getIconSets() {
		return new IconSets(Dispatch.get(this, "IconSets").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type String
	 */
	public String getEncryptionProvider() {
		return Dispatch.get(this, "EncryptionProvider").toString();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param encryptionProvider an input-parameter of type String
	 */
	public void setEncryptionProvider(String encryptionProvider) {
		Dispatch.put(this, "EncryptionProvider", encryptionProvider);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getDoNotPromptForConvert() {
		return Dispatch.get(this, "DoNotPromptForConvert").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param doNotPromptForConvert an input-parameter of type boolean
	 */
	public void setDoNotPromptForConvert(boolean doNotPromptForConvert) {
		Dispatch.put(this, "DoNotPromptForConvert", new Variant(doNotPromptForConvert));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type boolean
	 */
	public boolean getForceFullCalculation() {
		return Dispatch.get(this, "ForceFullCalculation").changeType(Variant.VariantBoolean).getBoolean();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param forceFullCalculation an input-parameter of type boolean
	 */
	public void setForceFullCalculation(boolean forceFullCalculation) {
		Dispatch.put(this, "ForceFullCalculation", new Variant(forceFullCalculation));
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param sharingPassword an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 */
	public void protectSharing(Variant filename, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, Variant sharingPassword, Variant fileFormat) {
		Dispatch.call(this, "ProtectSharing", filename, password, writeResPassword, readOnlyRecommended, createBackup, sharingPassword, fileFormat);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param sharingPassword an input-parameter of type Variant
	 */
	public void protectSharing(Variant filename, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, Variant sharingPassword) {
		Dispatch.call(this, "ProtectSharing", filename, password, writeResPassword, readOnlyRecommended, createBackup, sharingPassword);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 */
	public void protectSharing(Variant filename, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup) {
		Dispatch.call(this, "ProtectSharing", filename, password, writeResPassword, readOnlyRecommended, createBackup);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 */
	public void protectSharing(Variant filename, Variant password, Variant writeResPassword, Variant readOnlyRecommended) {
		Dispatch.call(this, "ProtectSharing", filename, password, writeResPassword, readOnlyRecommended);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 */
	public void protectSharing(Variant filename, Variant password, Variant writeResPassword) {
		Dispatch.call(this, "ProtectSharing", filename, password, writeResPassword);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 */
	public void protectSharing(Variant filename, Variant password) {
		Dispatch.call(this, "ProtectSharing", filename, password);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param filename an input-parameter of type Variant
	 */
	public void protectSharing(Variant filename) {
		Dispatch.call(this, "ProtectSharing", filename);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void protectSharing() {
		Dispatch.call(this, "ProtectSharing");
	}

	/**
	 * Wrapper for calling the ActiveX-Method and receiving the output-parameter(s).
	 * @param filename an input-parameter of type Variant
	 * @param password an input-parameter of type Variant
	 * @param writeResPassword an input-parameter of type Variant
	 * @param readOnlyRecommended an input-parameter of type Variant
	 * @param createBackup an input-parameter of type Variant
	 * @param sharingPassword an input-parameter of type Variant
	 * @param fileFormat an input-parameter of type Variant
	 */
	public void protectSharing(Variant filename, Variant password, Variant writeResPassword, Variant readOnlyRecommended, Variant createBackup, Variant sharingPassword, Variant fileFormat) {
		Dispatch.call(this, "ProtectSharing", filename, password, writeResPassword, readOnlyRecommended, createBackup, sharingPassword, fileFormat);

	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type SlicerCaches
	 */
	public SlicerCaches getSlicerCaches() {
		return new SlicerCaches(Dispatch.get(this, "SlicerCaches").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Slicer
	 */
	public Slicer getActiveSlicer() {
		return new Slicer(Dispatch.get(this, "ActiveSlicer").toDispatch());
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type Variant
	 */
	public Variant getDefaultSlicerStyle() {
		return Dispatch.get(this, "DefaultSlicerStyle");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param defaultSlicerStyle an input-parameter of type Variant
	 */
	public void setDefaultSlicerStyle(Variant defaultSlicerStyle) {
		Dispatch.put(this, "DefaultSlicerStyle", defaultSlicerStyle);
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void dummy26() {
		Dispatch.call(this, "Dummy26");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 */
	public void dummy27() {
		Dispatch.call(this, "Dummy27");
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @return the result is of type int
	 */
	public int getAccuracyVersion() {
		return Dispatch.get(this, "AccuracyVersion").changeType(Variant.VariantInt).getInt();
	}

	/**
	 * Wrapper for calling the ActiveX-Method with input-parameter(s).
	 * @param accuracyVersion an input-parameter of type int
	 */
	public void setAccuracyVersion(int accuracyVersion) {
		Dispatch.put(this, "AccuracyVersion", new Variant(accuracyVersion));
	}

}

