/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib??ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
package com.iver.cit.gvsig.project.documents.table;

import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;

import org.gvsig.tools.file.PathGenerator;

import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.data.NoSuchTableException;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.data.driver.ObjectDriver;
import com.hardcode.gdbms.engine.instruction.FieldNotFoundException;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.exceptions.layers.LegendLayerException;
import com.iver.cit.gvsig.fmap.edition.EditableAdapter;
import com.iver.cit.gvsig.fmap.edition.IEditableSource;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.iver.cit.gvsig.fmap.layers.layerOperations.SingleLayer;
import com.iver.cit.gvsig.fmap.rendering.IClassifiedVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.LegendFactory;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.AttrInTableLabelingStrategy;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.ILabelingStrategy;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelingFactory;
import com.iver.cit.gvsig.project.Project;
import com.iver.cit.gvsig.project.documents.ProjectDocument;
import com.iver.cit.gvsig.project.documents.exceptions.OpenException;
import com.iver.cit.gvsig.project.documents.exceptions.SaveException;
import com.iver.cit.gvsig.project.documents.table.gui.TableProperties;
import com.iver.cit.gvsig.project.documents.table.gui.tablemodel.Column;
import com.iver.cit.gvsig.project.documents.table.gui.tablemodel.Columns;
import com.iver.cit.gvsig.project.documents.view.ProjectView;
import com.iver.cit.gvsig.project.documents.view.ProjectViewFactory;
import com.iver.utiles.XMLEntity;

/**
 * Tabla del proyecto
 * 
 * @author Fernando Gonz?lez Cort?s
 */
public class ProjectTable extends ProjectDocument {
	// public static int numTables = 0;

	private IEditableSource esModel;

	private IEditableSource original;

	private String linkTable;

	private String[] joinedTables;

	private String field1;

	private String field2;

	/* No es necesaria para operar, s?lo para guardar el proyecto */
	private AlphanumericData associatedTable;

	private int[] mapping;

	private String[] alias = null;

	private Columns columns = new Columns();

	private long[] orderIndexes = null;

	private XMLEntity backupXMLEntity = null;

	
	
	/**
	 * Establece a true el bit index-?simo del bitmap de campos visibles. Los
	 * campos cuyos bits est?n a true se considerar?n visibles y viceversa
	 * 
	 * @param index
	 *            indice del bit que se quiere establecer a true
	 */
	public void set(int index) {
		// TODO implementar bien
		// modelo.set(index);

		change.firePropertyChange("visibles", true, true);
	}

	/**
	 * Obtiene el valor del bit index-?simo del bitmap de campos visibles
	 * 
	 * @param index
	 *            indice del bit que se quiere obtener
	 * 
	 * @return devuelve true si el campo index-?simo es visible y false en caso
	 *         contrario
	 */
	public boolean get(int index) {
		// TODO implementar bien
		// return modelo.get(index);
		return false;
	}

	/**
	 * Obtiene el modelo de la Tabla. Es decir, una clase con capacidad para
	 * leer la informaci?n de la tabla
	 * 
	 * @return
	 */
	public IEditableSource getModelo() {
		return esModel;
	}

	/**
	 * Establece el valor del bit index-?simo al valor 'value'
	 * 
	 * @param bitIndex
	 *            indice del bit que se quiere tocar
	 * @param value
	 *            valor que se quiere establecer en el bit indicado
	 */
	public void set(int bitIndex, boolean value) {
		// TODO implementar bien
		// modelo.set(bitIndex, value);
		change.firePropertyChange("visibles", value, value);
	}

	public void createAlias() throws ReadDriverException {
		SelectableDataSource sds = esModel.getRecordset();
		int fieldCount = sds.getFieldCount();
		mapping = new int[fieldCount];
		alias = new String[fieldCount];
		for (int i = 0; i < fieldCount; i++) {
			mapping[i] = i;
			alias[i] = sds.getFieldName(i);
		}
		recalculateColumnsFromAliases();

	}

	public void recalculateColumnsFromAliases() {
		ArrayList columnsAux = new ArrayList();
		columnsAux.addAll(columns);
		columns.clear();
		int aliasLength = getAliases().length;
		for (int i = 0; i < aliasLength; i++) {
			if (columnsAux.size() > i) {
				columns.add(columnsAux.get(i));
			} else {
				addColumn(new Column());
			}
		}

	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param sds
	 *            DOCUMENT ME!
	 * @throws DriverLoadException
	 * @throws ReadDriverException
	 */
	public void setDataSource(IEditableSource es) throws DriverLoadException,
			ReadDriverException {
		setModel(es);

		setName(esModel.getRecordset().getName());
		setCreationDate(DateFormat.getInstance().format(new Date()));
		change.firePropertyChange("model", esModel, esModel);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param ds
	 *            DOCUMENT ME!
	 * @throws ReadDriverException
	 * @throws com.hardcode.gdbms.engine.data.driver.DriverException
	 */
	public void replaceDataSource(IEditableSource es)
			throws ReadDriverException {
		if (original == null) {
			original = esModel;
		}
		setModel(es);
		es.getRecordset().setSelectionSupport(
				(original.getRecordset()).getSelectionSupport());

		createAlias();
		// FJP:
		// Si la tabla proviene de un layer, cambiamos su recordset
		if (associatedTable != null) {
			if (associatedTable instanceof FLyrVect) {
				// ((EditableAdapter)((FLyrVect)
				// associatedTable).getSource()).setRecordSet((SelectableDataSource)es.getRecordset());
				FLyrVect lyrVect = (FLyrVect) associatedTable;
				lyrVect.setRecordset(es.getRecordset());
				((FLyrVect) associatedTable).setIsJoined(true);
			}
		}

		change.firePropertyChange("model", original, esModel);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @throws com.hardcode.gdbms.engine.data.driver.DriverException
	 * @throws DriverLoadException
	 */
	public void restoreDataSource() throws ReadDriverException,
			DriverLoadException {
		// FJP:
		// Si la tabla proviene de un layer, cambiamos su recordset
		if (associatedTable != null) {
			if (associatedTable instanceof FLyrVect) {
				// Miramos si la leyenda que est? usando es una
				// leyenda basada en un campo de los de la uni?n.
				// Si lo es, avisamos al usuario de que ponemos una leyenda por
				// defecto.
				// Si los campos son de los originales, les ponemos el nombre
				// correcto en la leyenda

				FLyrVect lyr = ((FLyrVect) associatedTable);
				SelectableDataSource sdsOrig = original.getRecordset();
				SelectableDataSource sdsJoined = lyr.getRecordset();

				if (lyr.getLegend() instanceof IClassifiedVectorLegend) {
					IClassifiedVectorLegend legend = (IClassifiedVectorLegend) lyr
							.getLegend();
					String[] legendFields = legend.getClassifyingFieldNames();
					boolean bUsingJoinedField = false;
					for (int i = 0; i < legendFields.length; i++) {
						int idField = sdsJoined
								.getFieldIndexByName(legendFields[i]);
						String fieldName = sdsJoined.getFieldName(idField);
						int idOriginal = sdsOrig.getFieldIndexByName(fieldName);
						if (idOriginal == -1) {
							bUsingJoinedField = true;
							break;
						} else {
							legendFields[i] = fieldName;
						}
					}

					if (bUsingJoinedField) {
						JOptionPane.showMessageDialog(null, PluginServices.getText(null,
								"legend_using_joined_field"));
						try {
							lyr.setLegend(LegendFactory
									.createSingleSymbolLegend(lyr
											.getShapeType()));
						} catch (LegendLayerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							NotificationManager
									.addWarning(
											"Error in com.iver.cit.gvsig.project.documents.table.ProjectTable.restoreDataSource()",
											null);
							return;
						}
					} else {
						legend.setClassifyingFieldNames(legendFields);
						try {
							legend.setDataSource(original.getRecordset());
						} catch (FieldNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							NotificationManager
									.addWarning(
											"Error in com.iver.cit.gvsig.project.documents.table.ProjectTable.restoreDataSource()",
											null);
						}
							
					}
				} // ClassifiedVectorLegend
				checkLabelling(lyr, sdsOrig, sdsJoined);

				lyr.setRecordset(original.getRecordset());

				lyr.setIsJoined(false);
			}
		}

		setModel(original);
		original = null;
		joinedTables = null;
		createAlias();

		change.firePropertyChange("model", original, esModel);
	}

	private void checkLabelling(FLyrVect lyr, SelectableDataSource sdsOrig,
			SelectableDataSource sdsJoined) throws ReadDriverException {
		boolean bLabelingWithJoin = false;
		if (lyr.getLabelingStrategy() != null) {
			if (lyr.getLabelingStrategy() instanceof AttrInTableLabelingStrategy) {
				AttrInTableLabelingStrategy labelStrategy = (AttrInTableLabelingStrategy) lyr
						.getLabelingStrategy();
				String alias = labelStrategy.getTextField();
				if (alias != null) {
					int idField = sdsJoined.getFieldIndexByName(alias);
					String fieldName = sdsJoined.getFieldName(idField);
					int idOriginal = sdsOrig.getFieldIndexByName(fieldName);
					if (idOriginal == -1) {
						bLabelingWithJoin = true;
					} else {
						labelStrategy.setTextField(fieldName);
					}
				}

				alias = labelStrategy.getRotationField();
				if (alias != null) {
					int idField = sdsJoined.getFieldIndexByName(alias);
					String fieldName = sdsJoined.getFieldName(idField);
					int idOriginal = sdsOrig.getFieldIndexByName(fieldName);
					if (idOriginal == -1) {
						bLabelingWithJoin = true;
					} else {
						labelStrategy.setRotationField(fieldName);
					}
				}

				if (alias != null) {
					alias = labelStrategy.getColorField();
					int idField = sdsJoined.getFieldIndexByName(alias);
					String fieldName = sdsJoined.getFieldName(idField);
					int idOriginal = sdsOrig.getFieldIndexByName(fieldName);
					if (idOriginal == -1) {
						bLabelingWithJoin = true;
					} else {
						labelStrategy.setColorField(fieldName);
					}
				}

				if (alias != null) {
					alias = labelStrategy.getHeightField();
					int idField = sdsJoined.getFieldIndexByName(alias);
					String fieldName = sdsJoined.getFieldName(idField);
					int idOriginal = sdsOrig.getFieldIndexByName(fieldName);
					if (idOriginal == -1) {
						bLabelingWithJoin = true;
					} else {
						labelStrategy.setHeightField(fieldName);
					}
				}
			}
		}

		if (bLabelingWithJoin) {
			JOptionPane.showMessageDialog(null, PluginServices.getText(null,
					"labelling_using_joined_field"));
			
			lyr.setLabelingStrategy(LabelingFactory.createDefaultStrategy(lyr));
			lyr.setIsLabeled(false);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * @throws SaveException
	 * 
	 * @throws XMLException
	 */
	public XMLEntity getXMLEntity() throws SaveException {
		XMLEntity xml = super.getXMLEntity();
		try {
			// xml.putProperty("nameClass", this.getClass().getName());
			int numTables = ((Integer) ProjectDocument.NUMS
					.get(ProjectTableFactory.registerName)).intValue();

			xml.putProperty("numTables", numTables);

			if (this.getModelo() == null) {
				if (this.backupXMLEntity == null) {
					return xml;
				} else {
					return this.backupXMLEntity;
				}
			}

			if (getLinkTable() != null) {
				xml.putProperty("linkTable", linkTable);
				xml.putProperty("field1", field1);
				xml.putProperty("field2", field2);
			}

			if (getJoinedTables() != null) {
				xml.putProperty("joinedTableNames", getJoinedTables());
			}

			if (getOriginal() != null) {
				xml.addChild(getOriginal().getRecordset().getXMLEntity());
			}
			xml.addChild(esModel.getRecordset().getXMLEntity());

			// Object di = LayerFactory.getDataSourceFactory().getDriverInfo(
			// esModel.getRecordset().getName());

			if (associatedTable != null) {
				xml.putProperty("layerName", ((FLayer) associatedTable)
						.getName());
				xml.putProperty("viewName", project
						.getView((FLayer) associatedTable));
			}

			xml.putProperty("mapping", mapping);
			xml.putProperty("aliases", getAliases());
		} catch (Exception e) {
			throw new SaveException(e, this.getClass().getName());
		}

		// for (int i=0;i<columns.size();i++){
		// Column column=(Column)columns.get(i);
		// xml.addChild(column.getXMLEntity());
		// }
		xml.addChild(columns.getXMLEntity());
		xml.putProperty("columns", true);
		return xml;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param xml
	 *            DOCUMENT ME!
	 * @param p
	 *            DOCUMENT ME!
	 * 
	 * @throws XMLException
	 * @throws ReadDriverException
	 * @throws DriverException
	 * 
	 * @see com.iver.cit.gvsig.project.documents.ProjectDocument#setXMLEntity(com.iver.utiles.XMLEntity)
	 */
	public void setXMLEntity03(XMLEntity xml) throws XMLException,
			ReadDriverException {
		super.setXMLEntity03(xml);
		int numTables = xml.getIntProperty("numTables");
		ProjectDocument.NUMS.put(ProjectTableFactory.registerName, new Integer(
				numTables));

		if (xml.getStringProperty("type").equals("otherDriverFile")) {
			LayerFactory.getDataSourceFactory().addFileDataSource(
					xml.getStringProperty("driverName"),
					xml.getStringProperty("gdbmsname"),
					xml.getStringProperty("file"));

			setSelectableDataSource03(xml);
		} else if (xml.getStringProperty("type").equals("sameDriverFile")) {
			String layerName = xml.getStringProperty("layerName");

			ProjectView vista = (ProjectView) project.getProjectDocumentByName(
					xml.getStringProperty("viewName"),
					ProjectViewFactory.registerName);
			FLayer layer = vista.getMapContext().getLayers()
					.getLayer(layerName);

			setTheModel((VectorialEditableAdapter) ((FLyrVect) layer)
					.getSource());
			associatedTable = (AlphanumericData) layer;

			LayerFactory.getDataSourceFactory().addDataSource(
					(ObjectDriver) ((SingleLayer) layer).getSource()
							.getDriver(), xml.getStringProperty("gdbmsname"));
		} else if (xml.getStringProperty("type").equals("db")) {
			LayerFactory.getDataSourceFactory().addDBDataSourceByTable(
					xml.getStringProperty("gdbmsname"),
					xml.getStringProperty("host"), xml.getIntProperty("port"),
					xml.getStringProperty("user"),
					xml.getStringProperty("password"),
					xml.getStringProperty("dbName"),
					xml.getStringProperty("tableName"),
					xml.getStringProperty("driverInfo"));

			setSelectableDataSource03(xml);
		}

		setName(xml.getStringProperty("name"));
	}

	private void fillAsEmpty() {
		this.esModel = null;
		mapping = new int[0];
		alias = new String[0];
		recalculateColumnsFromAliases();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param xml
	 *            DOCUMENT ME!
	 * @param p
	 *            DOCUMENT ME!
	 * 
	 * @throws XMLException
	 * @throws DriverException
	 * @throws OpenException
	 * 
	 * @see com.iver.cit.gvsig.project.documents.ProjectDocument#setXMLEntity(com.iver.utiles.XMLEntity)
	 */
	public void setXMLEntity(XMLEntity xml) throws XMLException,
			ReadDriverException, OpenException {
		try {
			super.setXMLEntity(xml);
			backupXMLEntity = xml;

			setName(xml.getStringProperty("name"));
			int numTables = xml.getIntProperty("numTables");
			ProjectDocument.NUMS.put(ProjectTableFactory.registerName,
					new Integer(numTables));

			if (xml.getChildrenCount() == 0) {
				fillAsEmpty();
				return;

			}
			try {
				setSelectableDataSource(xml);
			} catch (ReadDriverException e) {
				fillAsEmpty();
				throw e;
			}

			/*
			 * if (xml.getStringProperty("type").equals("otherDriverFile")) { }
			 * else if (xml.getStringProperty("type").equals("sameDriverFile")) {
			 * String layerName = xml.getStringProperty("layerName");
			 * ProjectView vista = project.getViewByName(xml.getStringProperty(
			 * "viewName")); FLayer layer =
			 * vista.getMapContext().getLayers().getLayer(layerName);
			 * 
			 * modelo = ((AlphanumericData) layer).getRecordset();
			 * associatedTable = (AlphanumericData) layer; } else if
			 * (xml.getStringProperty("type").equals("db")) {
			 * setSelectableDataSource(xml); }
			 */
			setName(xml.getStringProperty("name"));

			if (xml.contains("linkTable")) {
				setLinkTable(xml.getStringProperty("linkTable"), xml
						.getStringProperty("field1"), xml
						.getStringProperty("field2"));
			}

			if (xml.contains("joinedTableNames")) {
				setJoinTable(xml.getStringArrayProperty("joinedTableNames"));
			}

			if (xml.contains("mapping")) {
				mapping = xml.getIntArrayProperty("mapping");
				alias = xml.getStringArrayProperty("aliases");
				// we check if all fields are real there (may be some external
				// program has changed them.
				// If we detect any change, we discard all mapping and aliases.
				SelectableDataSource sds = getModelo().getRecordset();
				if (mapping.length != sds.getFieldCount()) {
					createAlias();
					// columns.clear();
					// for (int i = 0; i <
					// esModel.getRecordset().getFieldCount(); i++) {
					// addColumn(new Column());
					// }
					return;

				} else {
					for (int i = 0; i < alias.length; i++) {
						sds.setFieldAlias(i, alias[i]);
					}
				}

			} else {
				try {
					createAlias();
				} catch (ReadDriverException e) {
					throw new XMLException(e);
				}
			}
		} catch (Exception e) {
			throw new OpenException(e, this.getClass().getName());
		}

		// for (int i=1;i<xml.getNumChild();i++){
		// columns.add(Column.createColumn(xml.getChild(i)));
		// }
		if (xml.contains("columns")) {
			columns.clear();
			columns = Columns.createColumns(xml
					.getChild(xml.getChildrenCount() - 1));
		}

	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param xml
	 *            DOCUMENT ME!
	 * 
	 * @throws XMLException
	 *             DOCUMENT ME!
	 * @throws DriverException
	 *             DOCUMENT ME!
	 */
	private void setSelectableDataSource03(XMLEntity xml) throws XMLException {
		String layerName = null;

		if (xml.contains("layerName")) {
			layerName = xml.getStringProperty("layerName");
		}

		if (layerName == null) {
			DataSource dataSource;

			try {
				dataSource = LayerFactory.getDataSourceFactory()
						.createRandomDataSource(
								xml.getStringProperty("gdbmsname"),
								DataSourceFactory.AUTOMATIC_OPENING);

				SelectableDataSource sds = new SelectableDataSource(dataSource);

				sds.setXMLEntity03(xml.getChild(0));
				EditableAdapter auxea = new EditableAdapter();
				auxea.setOriginalDataSource(sds);
				setDataSource(auxea);
			} catch (NoSuchTableException e) {
				throw new XMLException(e);
			} catch (DriverLoadException e) {
				throw new XMLException(e);
			} catch (ReadDriverException e) {
				throw new XMLException(e);
			}

		} else {
			ProjectView vista = (ProjectView) project.getProjectDocumentByName(
					xml.getStringProperty("viewName"),
					ProjectViewFactory.registerName);
			FLayer layer = vista.getMapContext().getLayers()
					.getLayer(layerName);

			setTheModel((VectorialEditableAdapter) ((FLyrVect) layer)
					.getSource());
			associatedTable = (AlphanumericData) layer;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param xml
	 *            DOCUMENT ME!
	 * 
	 * @throws XMLException
	 *             DOCUMENT ME!
	 * @throws DriverException
	 *             DOCUMENT ME!
	 */
	private void setSelectableDataSource(XMLEntity xml)
			throws ReadDriverException {
		String layerName = null;
		boolean bNeedToReplace = false;
		XMLEntity xmlAux = null;

		try {
			EditableAdapter es;

			if (xml.contains("layerName")) {
				layerName = xml.getStringProperty("layerName");

				ProjectView vista = (ProjectView) project
						.getProjectDocumentByName(xml
								.getStringProperty("viewName"),
								ProjectViewFactory.registerName);
				FLayer layer = getLayer(vista.getMapContext().getLayers(),
						layerName);
				EditableAdapter ea = new EditableAdapter();
				SelectableDataSource sds = ((FLyrVect) layer).getRecordset();
				// sds.setSelectionSupport(((FLyrVect)
				// layer).getSelectionSupport());
				ea.setOriginalDataSource(sds);
				associatedTable = (AlphanumericData) layer;

				es = ea;
			} else {
				es = new EditableAdapter();
				es.setOriginalDataSource(SelectableDataSource
						.createSelectableDataSource(xml.getChild(0)));
			}

			setDataSource(es);

			if (xml.getChildrenCount() == 2
					&& !(xml.contains("columns"))
					|| (xml.contains("columns") && (xml.getChildrenCount() == 3))) {
				bNeedToReplace = true;
				xmlAux = xml.getChild(1);
				es = new EditableAdapter();
				// es.setRecordSet(SelectableDataSource.createSelectableDataSource(xmlAux));

				// replaceDataSource(SelectableDataSource.createSelectableDataSource(xml.getChild(1)));
			}

			if (bNeedToReplace) {
				if (layerName != null) {
					ProjectView vista = (ProjectView) project
							.getProjectDocumentByName(xml
									.getStringProperty("viewName"),
									ProjectViewFactory.registerName);
					FLayer layer = getLayer(vista.getMapContext().getLayers(),
							layerName);

					// modelo = ((AlphanumericData) layer).getRecordset();
					associatedTable = (AlphanumericData) layer;
				}

				EditableAdapter auxea = new EditableAdapter();
				auxea.setOriginalDataSource(SelectableDataSource
						.createSelectableDataSource(xmlAux));
				replaceDataSource(auxea);
			}
		} catch (DriverLoadException e) {
			throw new ReadDriverException(getName(), e);
		} catch (XMLException e) {
			throw new ReadDriverException(getName(), e);
		}
	}

	private FLayer getLayer(FLayers layers, String name) {
		FLayer aux;
		for (int i = 0; i < layers.getLayersCount(); i++) {
			if (layers.getLayer(i) instanceof FLayers) {
				aux = getLayer((FLayers) layers.getLayer(i), name);
				if (aux != null) {
					return aux;
				}
			} else if (layers.getLayer(i).getName().equals(name)) {
				return layers.getLayer(i);
			}
		}
		return null;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public AlphanumericData getAssociatedTable() {
		return associatedTable;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param associatedTable
	 *            DOCUMENT ME!
	 */
	public void setAssociatedTable(AlphanumericData associatedTable) {
		this.associatedTable = associatedTable;
	}

	/**
	 * Obtiene la fuente de datos original de la tabla si se ha invocado
	 * replaceDataSource. Si no se invoc? este m?todo o se invoc? posteriormente
	 * restoreDataSource se devuelve null
	 * 
	 * @return Returns the original.
	 */
	public IEditableSource getOriginal() {
		return original;
	}

	/**
	 * Devuelve el identificador de la tabla que contiene el link.
	 * 
	 * @return identificador ?nico de la tabla.
	 */
	public String getLinkTable() {
		return linkTable;
	}

	/**
	 * Returns the name of the table(s) which have been joined to this table
	 * (using the JOIN operation from TableOperations).
	 * 
	 * @return The name of the joined table(s), if any, or null otherwise.
	 */
	public String[] getJoinedTables() {
		return joinedTables;
	}

	/**
	 * Sets the name of the table(s) which have been joined to this table (using
	 * the JOIN operation from TableOperations).
	 * 
	 * @return The name of the joined table(s), if any, or null otherwise.
	 */
	public void setJoinTable(String[] tables) {
		this.joinedTables = tables;
	}

	/**
	 * Devuelve el nombre del campo de la tabla a enlazar.
	 * 
	 * @return Nombre del campo de la tabla a enlazar.
	 */
	public String getField1() {
		return field1;
	}

	/**
	 * Devuelve el nombre del campo de la tabla enlazada.
	 * 
	 * @return Nombre del campo de la tabla enlazada.
	 */
	public String getField2() {
		return field2;
	}

	/**
	 * Inserta el identificador de la tabla, el campo de la primera tabla y el
	 * campo de la segunda tabla.
	 * 
	 * @param lt
	 *            identificado de la tabla.
	 * @param f1
	 *            nombre del campo de la primera tabla.
	 * @param f2
	 *            nombre del campo de la segunda tabla.
	 */
	public void setLinkTable(String lt, String f1, String f2) {
		linkTable = lt;
		field1 = f1;
		field2 = f2;
	}

	/**
	 * Borra el identificador de la tabla y elimina del array de listener los
	 * listener que sean del tipo: LinkSelectionListenr
	 */
	public void removeLinkTable() {
		linkTable = null;
		try {
			getModelo().getRecordset().removeLinksSelectionListener();
		} catch (ReadDriverException e) {
			e.printStackTrace();
		}
	}

	public String[] getAliases() {
		if (alias == null) {
			try {
				createAlias();
			} catch (ReadDriverException e) {
				PluginServices.getLogger().error(
						"Error creating the field aliases", e);
			}
		}
		return alias;
	}

	public void setAliases(String[] alias) {
		this.alias = alias;
	}

	public int[] getMapping() {
		if (mapping == null) {
			mapping = new int[getAliases().length];
			for (int i = 0; i < mapping.length; i++) {
				mapping[i] = i;
			}
		}
		return mapping;
	}

	public void setMapping(int[] mapping) {
		this.mapping = mapping;
	}

	public void setModel(IEditableSource ies) {
		setTheModel(ies);
		try {
			createAlias();
		} catch (ReadDriverException e) {
			e.printStackTrace();
			NotificationManager.addError(e);
		}
	}

	public Column getColumn(int i) {
		// if (i==0){
		// Column column=new Column();
		// column.setWidth(45);
		// return column;
		// }
		// i--;
		return (Column) columns.get(getMapping()[i]);
	}

	public void addColumn(Column column) {
		columns.add(column);
	}

	public int getColumnCount() {
		return columns.size();
	}

	public IWindow createWindow() {
		if (this.getModelo() == null)
			return null;
		com.iver.cit.gvsig.project.documents.table.gui.Table table = new com.iver.cit.gvsig.project.documents.table.gui.Table();
		table.setModel(this);
		callCreateWindow(table);
		return table;
	}

	public IWindow getProperties() {
		return new TableProperties(this);
	}

	public void afterRemove() {
		// TODO Auto-generated method stub

	}

	public void afterAdd() {
		// TODO Auto-generated method stub

	}

	private void setTheModel(IEditableSource es) {
		this.esModel = es;

	}

	public void exportToXML(XMLEntity root, Project project)
			throws SaveException {
		XMLEntity tableRoot = project.getExportXMLTypeRootNode(root,
				ProjectTableFactory.registerName);
		try {
			project.exportToXMLDataSource(root, this.getModelo().getRecordset()
					.getName());
		} catch (ReadDriverException e) {
			throw new SaveException();
		}
		tableRoot.addChild(this.getXMLEntity());
	}

	public void importFromXML(XMLEntity root, XMLEntity typeRoot,
			int elementIndex, Project project, boolean removeDocumentsFromRoot)
			throws XMLException, ReadDriverException, OpenException {
		XMLEntity element = typeRoot.getChild(elementIndex);
		this.setXMLEntity(element);
		if (removeDocumentsFromRoot) {
			typeRoot.removeChild(elementIndex);
		}
		project.addDocument(this);

	}

	public long[] getOrderIndexes() {
		return orderIndexes;
	}

	public void setOrderIndexes(long[] orderIndexes) {
		this.orderIndexes = orderIndexes;
	}

	// public int computeSignature() {
	// int result = 17;
	//
	// Class clazz = getClass();
	// Field[] fields = clazz.getDeclaredFields();
	// for (int i = 0; i < fields.length; i++) {
	// try {
	// String type = fields[i].getType().getName();
	// if (type.equals("boolean")) {
	// result += 37 + ((fields[i].getBoolean(this)) ? 1 : 0);
	// } else if (type.equals("java.lang.String")) {
	// Object v = fields[i].get(this);
	// if (v == null) {
	// result += 37;
	// continue;
	// }
	// char[] chars = ((String) v).toCharArray();
	// for (int j = 0; j < chars.length; j++) {
	// result += 37 + (int) chars[i];
	// }
	// } else if (type.equals("byte")) {
	// result += 37 + (int) fields[i].getByte(this);
	// } else if (type.equals("char")) {
	// result += 37 + (int) fields[i].getChar(this);
	// } else if (type.equals("short")) {
	// result += 37 + (int) fields[i].getShort(this);
	// } else if (type.equals("int")) {
	// result += 37 + fields[i].getInt(this);
	// } else if (type.equals("long")) {
	// long f = fields[i].getLong(this) ;
	// result += 37 + (f ^ (f >>> 32));
	// } else if (type.equals("float")) {
	// result += 37 + Float.floatToIntBits(fields[i].getFloat(this));
	// } else if (type.equals("double")) {
	// long f = Double.doubleToLongBits(fields[i].getDouble(this));
	// result += 37 + (f ^ (f >>> 32));
	// } else {
	// Object obj = fields[i].get(this);
	// result += 37 + ((obj != null)? obj.hashCode() : 0);
	// }
	// } catch (Exception e) { e.printStackTrace(); }
	//
	// }
	// return result;
	// }
}

