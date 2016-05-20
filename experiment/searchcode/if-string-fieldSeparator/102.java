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

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.event.ChangeListener;

import org.gvsig.normalization.patterns.Element;
import org.gvsig.normalization.patterns.Fieldseparator;
import org.gvsig.normalization.patterns.NormalizationPattern;

import com.iver.cit.gvsig.project.documents.table.gui.Table;

/**
 * Interface of panel model
 * 
 * @author <a href="mailto:jsanz@prodevelop.es"> Jorge Gaspar Sanz Salinas</a>
 * @author <a href="mailto:vsanjaime@prodevelop.es"> Vicent Sanjaime Calvet</a>
 */
public interface INormPanelModel {

	/**
	 * Normalize the samples and return a 2D array of objects (Strings) to put
	 * on the output table
	 * 
	 * @return matrix with the strings of sample
	 */
	public Object[][] normalizeSamples();

	/**
	 * Return the name of the field to normalize
	 * 
	 * @return name
	 */
	public String getFieldToNormalize();

	/**
	 * Set the name of the field to normalize
	 * 
	 * @param fieldToNormalize
	 *            name of field
	 */
	public void setFieldToNormalize(String fieldToNormalize);

	/**
	 * Get samples from the input table
	 * 
	 * @return array with strings from the table
	 */
	public String[] getSamplesTable();

	/**
	 * Get samples from the input file
	 * 
	 * @return array with strings from the text file
	 */
	public String[] getSamplesFile();

	/**
	 * Get the first or random samples from data source (FILE)
	 */
	public void getSamplesFromFile(int numNoRows);

	/**
	 * Get the default Fieldseparator object for new Fields
	 * 
	 * @return
	 */
	public Fieldseparator getDefaultFieldseparators();

	/**
	 * Delete the address element selected
	 * 
	 * @param column
	 *            number
	 */
	public void deleteField(int column);

	/**
	 * Add one address element
	 */
	public void addField();

	/**
	 * Return the names of the new fields
	 * 
	 * @return list names
	 */
	public String[] getNewFieldNames();

	/**
	 * Main method to run the model
	 * 
	 * @param normalizationPanel
	 * 
	 * @return process ok
	 */
	public boolean runModel(ChangeListener normalizationPanel);

	/**
	 * This method up one place in the list of the new fields
	 * 
	 * @param pos
	 */
	public void fieldUp(int pos);

	/**
	 * This method down one place in the list of the new fields
	 * 
	 * @param pos
	 */
	public void fieldDown(int pos);

	/**
	 * Set the pattern
	 * 
	 * @param pat
	 *            pattern
	 */
	public void setPattern(NormalizationPattern pat);

	/**
	 * @return the pattern
	 */
	public NormalizationPattern getPattern();

	/**
	 * Load the pattern from xml file
	 * 
	 * @return pattern
	 */
	public NormalizationPattern loadPatternXML();

	/**
	 * Make the normalization process in a new table (true) or in the original
	 * table (false)
	 * 
	 * @param inNewTable
	 */
	public void setInNewTable(boolean inNewTable);

	/**
	 * Return the selected Element in the list of the new Fields
	 * 
	 * @param index
	 * @return
	 */
	public Element getElement(int index);

	/**
	 * Save the pattern in a XML file
	 * 
	 * @return
	 */
	public void savePatternXML();

	/**
	 * Get all the fields names of the original table
	 * 
	 * @return list model
	 */
	public DefaultListModel getAllOriginalFields();

	/**
	 * Set the relates fields names
	 * 
	 * @param array
	 *            with relate names
	 */
	public void setNameRelateFields(String[] names);

	/**
	 * get the field names of the original table
	 * 
	 * @return fields of the original table
	 */
	public String[] getFieldNamesMainTable();

	/**
	 * Get the text File name
	 * 
	 * @return name file
	 */
	public String getFileName();

	/**
	 * Set the attribute Don't normalize the first row
	 * 
	 * @param first
	 */
	public void setFirstRows(int first);

	/**
	 * Get Normalize the first row
	 * 
	 * @return first row ok
	 */
	public int getFirstRows();

	/**
	 * Get the table
	 * 
	 * @return the tab
	 */
	public Table getTab();

	/**
	 * Set the table
	 * 
	 * @param tab
	 *            the tab to set
	 */
	public void setTab(Table tab);

	/**
	 * Get the file strings
	 * 
	 * @return list of strings
	 */
	public List<String> getFileChains();

	/**
	 * Set the list of strngs from file
	 * 
	 * @param fileChains
	 *            the fileChains to set
	 */
	public void setFileChains(List<String> fileChains);

	/**
	 * Get file name
	 * 
	 * @return the nameFile
	 */
	public String getNameFile();

	/**
	 * Set file name
	 * 
	 * @param nameFile
	 *            the nameFile to set
	 */
	public void setNameFile(String nameFile);

	/**
	 * Initialize the pattern
	 */
	public void initPattern();

	/**
	 * Set the file type
	 * 
	 * @param file
	 *            ok
	 */
	public void isFile(boolean file);

	/**
	 * This method registers the listeners
	 * 
	 * @param l
	 *            listener
	 */
	public void registerListener(ChangeListener l);

}

