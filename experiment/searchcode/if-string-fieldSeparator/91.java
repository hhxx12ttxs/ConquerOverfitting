/* gvSIG. Geographic Information System of the Valencian Government
 *
 * Copyright (C) 2007-2008 Infrastructures and Transports Department
 * of the Valencian Government (CIT)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 */

/*
 * AUTHORS (In addition to CIT):
 * 2008 Prodevelop S.L. main development
 */

package org.gvsig.normalization.gui;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.gvsig.normalization.operations.JoinedTableNormalization;
import org.gvsig.normalization.operations.NormAlgorithm;
import org.gvsig.normalization.operations.Normalization;
import org.gvsig.normalization.operations.NormalizationNewTable;
import org.gvsig.normalization.operations.StringListNormalization;
import org.gvsig.normalization.operations.TableNormalization;
import org.gvsig.normalization.patterns.Element;
import org.gvsig.normalization.patterns.Fieldseparator;
import org.gvsig.normalization.patterns.Fieldtype;
import org.gvsig.normalization.patterns.Infieldseparators;
import org.gvsig.normalization.patterns.NormalizationPattern;
import org.gvsig.normalization.patterns.Stringvalue;

import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.edition.EditableAdapter;
import com.iver.cit.gvsig.fmap.edition.IEditableSource;
import com.iver.cit.gvsig.fmap.edition.IRowEdited;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.project.Project;
import com.iver.cit.gvsig.project.ProjectFactory;
import com.iver.cit.gvsig.project.documents.ProjectDocumentFactory;
import com.iver.cit.gvsig.project.documents.table.ProjectTable;
import com.iver.cit.gvsig.project.documents.table.ProjectTableFactory;
import com.iver.cit.gvsig.project.documents.table.gui.Table;
import com.iver.utiles.GenericFileFilter;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.extensionPoints.ExtensionPoints;
import com.iver.utiles.extensionPoints.ExtensionPointsSingleton;

/**
 * Model of the Normalization panel
 * 
 * @author <a href="mailto:jsanz@prodevelop.es"> Jorge Gaspar Sanz Salinas</a>
 * @author <a href="mailto:vsanjaime@prodevelop.es"> Vicent Sanjaime Calvet</a>
 */

public class NormPanelModel implements INormPanelModel {

	private static final Logger log = PluginServices.getLogger();

	private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
	private String fieldToNormalize;
	private String[] samples;
	private NormalizationPattern pattern;
	private Table tab;
	private boolean inNewTable;
	private int contador = 1;
	private String[] relateNames;
	private List<String> fileChains;
	private boolean isFile;
	private String nameFile;

	/* SINGLETON DEFINITION */
	private volatile static INormPanelModel uniqueInstance;

	/**
	 * Get the instance of panel model
	 * 
	 * @return panel model
	 */
	public static INormPanelModel getInstance() {
		if (uniqueInstance == null) {
			synchronized (NormPanelModel.class) {
				if (uniqueInstance == null) {
					uniqueInstance = new NormPanelModel();
				}
			}
		}
		return uniqueInstance;
	}

	/**
	 * Constructor
	 */
	private NormPanelModel() {
		initPattern();
	}

	/**
	 * This method fills the GUI sample table with the samples
	 * 
	 * @return Object[][]
	 */
	public Object[][] normalizeSamples() {

		int numFields = pattern.getElements().size();
		Object[][] results = new Object[NormalizationPanel.SAMPLES][numFields];

		NormAlgorithm na = new NormAlgorithm(this.pattern);
		List<String> chains;
		for (int i = 0; i < NormalizationPanel.SAMPLES; i++) {
			chains = na.splitChain(samples[i]);
			numFields = chains.size();
			for (int j = 0; j < numFields; j++) {
				results[i][j] = chains.get(j);
			}
		}
		return results;
	}

	/**
	 * Run the process of normalization
	 * 
	 * @return process ok
	 */
	public boolean runModel(ChangeListener listener) {
		this.registerListener(listener);
		Normalization normAction = null;
		// REQUEST FILE TO OUPUT THE DBF
		File thefile = null;
		if (this.isFile || (!this.isFile && this.inNewTable)) {
			thefile = getDBFFile();
			if (thefile == null) {
				update("INFO.endnormalizing");
				return false;
			}
			if (thefile.exists()) {
				update("INFO.fileexists");
				update("INFO.endnormalizing");
				return false;
			}
		}

		// FILE NORMALIZATION
		if (this.isFile) {
			normAction = new StringListNormalization(pattern, fileChains,
					thefile);
		} else {
			IEditableSource source = tab.getModel().getModelo();
			int index = tab.getSelectedFieldIndices().nextSetBit(0);

			if (!this.inNewTable) {
				// ONE TABLE
				normAction = new TableNormalization(source, index, pattern);
			} else {
				// JOINED TABLE
				normAction = new JoinedTableNormalization(source, index,
						pattern, relateNames, thefile);
			}
		}

		normAction.registerListener(listener);
		boolean pre = normAction.preProcess();

		if (pre) {
			PluginServices.cancelableBackgroundExecution(new NormalizationTask(
					normAction, this));
		} else {
			log.error("Error preprocessing tables");
			return false;
		}

		if (normAction instanceof NormalizationNewTable) {
			NormalizationNewTable normTable = (NormalizationNewTable) normAction;
			this.loadTable(normTable);
		}
		return true;
	}

	/**
	 * This method up the selected element one position in the list of Elements
	 * 
	 * @param pos
	 */
	public void fieldUp(int pos) {

		int nu = pattern.getElements().size();

		if (pos > 0 && nu > 1) {
			int newpos = pos - 1;
			Element[] ad = pattern.getArrayElements();
			Element ele21 = ad[pos];
			Element ele12 = ad[newpos];

			ad[newpos] = ele21;
			ad[pos] = ele12;
			List<Element> elems = new ArrayList<Element>();
			for (int i = 0; i < ad.length; i++) {
				elems.add(ad[i]);
			}
			pattern.setElements(elems);
		}
	}

	/**
	 * This method down the selected element one position in the list of
	 * Elements
	 * 
	 * @param pos
	 */
	public void fieldDown(int pos) {
		int nu = pattern.getElements().size();

		if (pos != (nu - 1) && nu > 1) {
			int newpos = pos + 1;
			Element[] ad = pattern.getArrayElements();
			Element ele21 = ad[pos];
			Element ele12 = ad[newpos];

			ad[newpos] = ele21;
			ad[pos] = ele12;
			List<Element> elems = new ArrayList<Element>();
			for (int i = 0; i < ad.length; i++) {
				elems.add(ad[i]);
			}
			pattern.setElements(elems);
		}
	}

	/**
	 * This method adds a new element to the pattern
	 */

	public void addField() {

		contador++;
		int tam = pattern.getElements().size();
		Element eleme = new Element();

		Fieldseparator fsep = new Fieldseparator();
		fsep.setSemicolonsep(true);
		fsep.setJoinsep(false);
		fsep.setColonsep(false);
		fsep.setSpacesep(false);
		fsep.setTabsep(false);

		String nam = "";
		boolean isOkName = true;
		do {
			isOkName = true;
			nam = "NewField" + contador;
			for (int i = 0; i < tam; i++) {
				String napat = ((Element) pattern.getElements().get(i))
						.getFieldname();
				if (napat.compareToIgnoreCase(nam) == 0) {
					isOkName = false;
					break;
				}
			}
			if (!isOkName) {
				contador++;
			}

		} while (!isOkName);

		// validate the new field name
		//String vname = validateFieldName(nam);
		eleme.setFieldname(nam);
		eleme.setFieldseparator(fsep);
		eleme.setInfieldseparators(getDefaultInfieldseparators());
		eleme.setFieldtype(getDefaultNewFieldType());
		eleme.setFieldwidth(0);
		eleme.setImportfield(true);

		List<Element> elems = pattern.getElements();
		elems.add(tam, eleme);
	}

	/**
	 * This method erases a selected element to the list
	 * 
	 * @param pos
	 *            position
	 */
	public void deleteField(int pos) {
		int conta = pattern.getElements().size();
		if (conta > 1) {
			pattern.getElements().remove(pos);
		}
	}

	/**
	 * This method saves a Normalization pattern to XML file *
	 */
	public void savePatternXML() {

		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle(PluginServices.getText(this, "save_norm_pattern"));
		String[] extensions = { "xml" };
		jfc.setCurrentDirectory(new File(getFolderPattern()));
		jfc.addChoosableFileFilter(new GenericFileFilter(extensions,
				PluginServices.getText(this, "pattern_norm_file")));
		int returnval = jfc.showSaveDialog((Component) PluginServices
				.getMainFrame());

		if (returnval == JFileChooser.APPROVE_OPTION) {
			File thefile = jfc.getSelectedFile();
			// Check if the file has extension
			if (!(thefile.getPath().toLowerCase().endsWith(".xml"))) {
				thefile = new File(thefile.getPath() + ".xml");
			}

			if (thefile.exists()) {

				int n = JOptionPane.showConfirmDialog(null, PluginServices
						.getText(null, "file_exists"), PluginServices.getText(
						null, "save_norm_pattern"), JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION) {
					writeSaveFile(thefile);
				} else if (n == JOptionPane.NO_OPTION) {
					update("INFO.filenotoverwrite");
				} else {
					update("INFO.filenotsave");
				}
			} else {
				writeSaveFile(thefile);
			}
		} else {
			update("INFO.nosave");
		}
	}

	/**
	 * This method loads a Normalization pattern from a XML file and return the
	 * pattern and the String info
	 * 
	 * @return pattern
	 */
	public NormalizationPattern loadPatternXML() {

		NormalizationPattern pat = new NormalizationPattern();
		File thefile = null;

		// Show the FileChooser to select a pattern
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File(getFolderPattern()));
		jfc.setDialogTitle(PluginServices.getText(this, "load_norm_pattern"));
		String[] extensions = { "xml" };
		jfc.addChoosableFileFilter(new GenericFileFilter(extensions,
				PluginServices.getText(this, "pattern_norm_file")));

		int returnval = jfc.showOpenDialog((Component) PluginServices
				.getMainFrame());

		if (returnval == JFileChooser.APPROVE_OPTION) {
			thefile = jfc.getSelectedFile();
		} else {
			update("INFO.noload");
			return null;
		}

		try {
			pat.loadFromXML(thefile);
			update("INFO.loadsuccess");
		} catch (FileNotFoundException e) {
			pat = null;
			update("ERROR.filenotfound");
			log.error("File not found", e);
		} catch (Exception e) {
			pat = null;
			update("ERROR.parsingpatternxml");
			update("INFO.noformatok");
			update("INFO.noload");
			log.error("ERROR Parsing xml", e);
		}

		return pat;
	}

	/* GETTERS Y SETTERS */

	/**
	 * Return the name of the field to normalize
	 * 
	 * @return field name
	 */
	public String getFieldToNormalize() {
		return fieldToNormalize;
	}

	/**
	 * Set the name of the field to normalize
	 * 
	 * @param fieldToNormalize
	 */
	public void setFieldToNormalize(String fieldToNormalize) {

		this.fieldToNormalize = fieldToNormalize;

		/* Get samples to put in the GUI table */
		getSamplesFromTable();
	}

	/**
	 * This method returns the names of the fields from the pattern
	 * 
	 * @return new fields names
	 */
	public String[] getNewFieldNames() {

		int numFields = pattern.getElements().size();
		String[] res = new String[numFields];

		for (int i = 0; i < numFields; i++) {
			res[i] = ((Element) pattern.getElements().get(i)).getFieldname();
		}
		return res;
	}

	/**
	 * Get the first or random samples from data source (FILE)
	 */
	public void getSamplesFromFile(int numNoRows) {

		int num = NormalizationPanel.SAMPLES;
		String[] res = new String[num];
		for (int i = 0; i < num; i++) {
			try {
				res[i] = (String) fileChains.get(i + numNoRows);
			} catch (Exception e) {
				res[i] = "";
			}
		}
		this.samples = res;
	}

	/**
	 * Return the samples Table
	 * 
	 * @return samples from table
	 */
	public String[] getSamplesTable() {
		return samples;
	}

	/**
	 * Return the samples File
	 * 
	 * @return samples from file
	 */
	public String[] getSamplesFile() {
		return samples;
	}

	/**
	 * This method sets the original table to the model
	 * 
	 * @param _table
	 */
	public void setTable(Table _table) {
		tab = _table;
	}

	/**
	 * This method says if the process will be in a new table (true) or in the
	 * original table (false)
	 * 
	 * @return
	 */
	public boolean isInNewTable() {
		return inNewTable;
	}

	/**
	 * Assign value the flag
	 * 
	 * @param inNewTable
	 */
	public void setInNewTable(boolean inNewTable) {
		this.inNewTable = inNewTable;
	}

	/**
	 * This method returns a Element of one position
	 * 
	 * @param index
	 * @return
	 */
	public Element getElement(int index) {
		Element[] adrs = pattern.getArrayElements();
		return adrs[index];
	}

	/**
	 * This method returns the fields names of the original table
	 * 
	 * @return list model
	 */
	public DefaultListModel getAllOriginalFields() {

		FieldDescription[] fields = tab.getModel().getModelo()
				.getFieldsDescription();
		int num = fields.length;
		String names = "";
		DefaultListModel dlm = new DefaultListModel();
		dlm.add(0, PluginServices.getText(this, "none_field"));
		dlm.add(1, PluginServices.getText(this, "all_fields"));
		for (int i = 0; i < num; i++) {
			names = fields[i].getFieldName();
			dlm.addElement(names);
		}
		return dlm;
	}

	/**
	 * This method generates and returns field separators
	 * 
	 * @return default separators between fields
	 */
	public Fieldseparator getDefaultFieldseparators() {

		Fieldseparator filsep = new Fieldseparator();
		filsep.setSemicolonsep(true);
		filsep.setJoinsep(false);
		filsep.setColonsep(false);
		filsep.setSpacesep(false);
		filsep.setTabsep(false);

		return filsep;
	}

	/**
	 * Set the names of the relate Fields
	 * 
	 * @param list
	 *            of the names
	 */

	public void setNameRelateFields(String[] names) {
		relateNames = names;
	}

	/**
	 * This method returns the fields names of the original table
	 * 
	 * @return fields names of the main table
	 */
	public String[] getFieldNamesMainTable() {
		FieldDescription[] fds = tab.getModel().getModelo()
				.getFieldsDescription();
		String[] na = new String[fds.length];
		for (int i = 0; i < fds.length; i++) {
			na[i] = fds[i].getFieldName();
		}
		return na;
	}

	/**
	 * This method sets the pattern
	 * 
	 * @pat pattern
	 */
	public void setPattern(NormalizationPattern pat) {
		pattern = pat;
	}

	// /**
	// * This method returns the debug info
	// *
	// * @return info about the process
	// */
	// public String getInfo() {
	// return infob.toString();
	// }
	//
	// /**
	// * This method adds info about the process
	// *
	// * @param chain
	// * message
	// */
	// public void addInfo(String chain) {
	// infob.append(chain);
	// }

	/**
	 * This method returns the File name to normalize
	 * 
	 * @return file name
	 */
	public String getFileName() {
		return nameFile;
	}

	/**
	 * set the number of first rows that not normalize
	 * 
	 * @param first
	 *            select the first row
	 */
	public void setFirstRows(int first) {
		pattern.setNofirstrows(first);

	}

	/**
	 * Set the table
	 * 
	 * @param tab
	 *            the tab to set
	 */
	public void setTab(Table tab) {
		this.tab = tab;
	}

	/**
	 * Set the file strings
	 * 
	 * @param fileChains
	 *            the fileChains to set
	 */
	public void setFileChains(List<String> fileChains) {
		this.fileChains = fileChains;
		this.isFile = true;
		getSamplesFromFile(getFirstRows());
	}

	/**
	 * Get the file strings
	 * 
	 * @return the file strings
	 */
	public List<String> getFileChains() {
		return fileChains;
	}

	/**
	 * Get the file name
	 * 
	 * @return the nameFile
	 */
	public String getNameFile() {
		return nameFile;
	}

	/**
	 * Get the table
	 * 
	 * @return the tab
	 */
	public Table getTab() {
		return tab;
	}

	/**
	 * Set the file name
	 * 
	 * @param nameFile
	 *            the nameFile to set
	 */
	public void setNameFile(String nameFile) {
		this.nameFile = nameFile;
	}

	/**
	 * get Normalize the first row
	 * 
	 * @return normalize first row
	 */
	public int getFirstRows() {
		return pattern.getNofirstrows();

	}

	/**
	 * Set the file type
	 * 
	 * @param file
	 */
	public void isFile(boolean file) {
		this.isFile = file;
	}

	// /**
	// * This method cleans the information about the process
	// */
	// public void clearConsole() {
	// int con = infob.length() > 0 ? infob.length() - 1 : 0;
	// if (con > 0) {
	// infob.delete(0, con);
	// }
	// }

	/**
	 * This method creates the default Normalization pattern
	 */
	public void initPattern() {

		NormalizationPattern pat = new NormalizationPattern();

		pat.setPatternname("defaultPattern");
		pat.setNofirstrows(0);

		/* Create the first Address Element */
		Element elem = new Element();

		elem.setFieldname("NewField");
		elem.setFieldseparator(getDefaultFieldseparators());
		elem.setInfieldseparators(getDefaultInfieldseparators());
		elem.setFieldtype(getDefaultNewFieldType());
		elem.setFieldwidth(0);
		elem.setImportfield(true);

		List<Element> elems = new ArrayList<Element>();
		elems.add(elem);

		pat.setElements(elems);

		this.pattern = pat;
	}

	/* METHODS PRIVATES */

	/**
	 * This method loads the new table
	 * 
	 * @param normTable
	 */
	private void loadTable(NormalizationNewTable normTable) {
		String name = normTable.getOuputFile().getName();

//		LayerFactory.getDataSourceFactory().addDataSource(
//				normTable.getDriver(), name);
		
		LayerFactory.getDataSourceFactory().addFileDataSource("gdbms dbf driver",
				name, normTable.getOuputFile().getAbsolutePath());

		EditableAdapter editAdapterSecond = null;

		DataSource dtSecond;
		try {
			
			
			
			dtSecond = LayerFactory.getDataSourceFactory()
					.createRandomDataSource(name,
							DataSourceFactory.AUTOMATIC_OPENING);			

			SelectableDataSource sel = new SelectableDataSource(dtSecond);
			
			editAdapterSecond = new EditableAdapter();
			editAdapterSecond.setOriginalDataSource(sel);

			// add to doc factory
			ProjectTable ptsec = ProjectFactory.createTable(name,
					editAdapterSecond);
			ExtensionPoints extensionPoints = ExtensionPointsSingleton
					.getInstance();

			extensionPoints.get("Documents");

			ProjectDocumentFactory documentFactory = null;

			documentFactory = new ProjectTableFactory();

			ptsec.setProjectDocumentFactory(documentFactory);
			Table secondTable = new Table();
			secondTable.setModel(ptsec);
			PluginServices.getMDIManager().addWindow(secondTable);
			ProjectExtension pe = (ProjectExtension) PluginServices
					.getExtension(ProjectExtension.class);
			Project theProject = pe.getProject();
			theProject.addDocument(ptsec);
			pe.getProjectFrame().refreshControls();
			pe.getProjectFrame().repaint();
		} catch (Exception e) {
			log.error("Error loading the new table", e);
		}
	}

	/**
	 * Get the first or random samples from data source (TABLE)
	 */
	private void getSamplesFromTable() {
		int num = NormalizationPanel.SAMPLES;
		String[] res = new String[num];

		ProjectTable pt = tab.getModel();
		IEditableSource ies = pt.getModelo();
		try {

			long tamTab = ies.getRowCount();
			if (tamTab > num)
				tamTab = num;

			for (int i = 0; i < (int) tamTab; i++) {
				IRowEdited rowVals = ies.getRow(i);

				res[i] = rowVals.getAttribute(
						tab.getSelectedFieldIndices().nextSetBit(0)).toString()
						.trim();
			}
		} catch (Exception e) {
			log.error("ERROR al obtener el recorset de la tabla", e);

		}

		this.samples = res;
	}

	/**
	 * This method returns the dbf file
	 * 
	 * @return dbf file
	 */
	private File getDBFFile() {
		File thefile = null;
		JFileChooser jfc = new JFileChooser();
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.addChoosableFileFilter(new GenericFileFilter("dbf", PluginServices
				.getText(this, "gdbms dbf drver")));
		jfc.setDialogTitle(PluginServices.getText(this, "new_table"));
		int returnval = jfc.showSaveDialog((Component) PluginServices
				.getMainFrame());
		if (returnval == JFileChooser.APPROVE_OPTION) {
			thefile = jfc.getSelectedFile();
			if (!(thefile.getPath().toLowerCase().endsWith(".dbf"))) {
				thefile = new File(thefile.getPath() + ".dbf");
			}
		}
		return thefile;
	}

	/**
	 * This method generates and returns in field separators
	 * 
	 * @return special characters within one string
	 */

	private Infieldseparators getDefaultInfieldseparators() {

		/* create the default in-field separators */
		Locale loc = Locale.getDefault();
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(loc);
		Infieldseparators infilsep = new Infieldseparators();
		infilsep.setThousandseparator(Character.toString(dfs
				.getGroupingSeparator()));
		infilsep.setDecimalseparator(Character.toString(dfs
				.getDecimalSeparator()));
		infilsep.setTextseparator("\"");

		return infilsep;
	}

	/**
	 * This method generates and returns a new field type of type Stringvalue
	 * 
	 * @return field type
	 */
	private Fieldtype getDefaultNewFieldType() {

		Fieldtype newtype = new Fieldtype();
		Stringvalue strval = new Stringvalue();
		strval.setStringvaluewidth(50);
		newtype.setStringvalue(strval);

		return newtype;
	}

	/**
	 * This method write the save xml file
	 * 
	 * @param file
	 */
	private void writeSaveFile(File thefile) {

		pattern.setPatternname(thefile.getName());

		try {
			pattern.saveToXML(thefile);
			update("INFO.savesuccess");

		} catch (Exception e) {
			log.error("Error al parsear el patron", e);
			update("ERROR.savingpat");
		}
	}

	/**
	 * This method return the folder where gvSIG stores the patterns
	 * 
	 * @return
	 */
	private String getFolderPattern() {
		XMLEntity xml = PluginServices.getPluginServices(this)
				.getPersistentXML();
		String pathFolder = String.valueOf(xml
				.getStringProperty("Normalization_pattern_folder"));
		return pathFolder;
	}

	/**
	 * This method registers the listeners
	 * 
	 * @param l
	 *            listener
	 */
	public void registerListener(ChangeListener l) {
		this.listeners.add(l);
	}

	/**
	 * This method remove the listeners registred
	 * 
	 * @param l
	 *            listener
	 */
	public void removeListener(ChangeListener l) {
		this.listeners.remove(l);
	}

	/**
	 * This method removes all listeners
	 */
	public void removeAllListeners() {
		this.listeners.clear();
	}

	/**
	 * 
	 * @param evt
	 *            event
	 */
	public void update(ChangeEvent evt) {

		for (int i = 0; i < listeners.size(); i++) {
			((ChangeListener) listeners.get(i)).stateChanged(evt);
		}
	}

	/**
	 * Add message
	 * 
	 * @param message
	 */
	public void update(String message) {
		ChangeEvent evt = new ChangeEvent(message);
		update(evt);
	}

	/**
	 * @return the pattern
	 */
	public NormalizationPattern getPattern() {
		return pattern;
	}

}

