/*
*
*
* Copyright (C) 2011-2012 Jorge Luis Martinez Ramirez
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
* Author: Jorge Luis Martinez Ramirez
* Email: jorgemfk1@gmail.com
*/
package mx.dr.forms.view.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mx.dr.forms.constants.FormActions;
import mx.dr.forms.dto.DRComponentDefinition;
import mx.dr.forms.dto.DRFieldDto;
import mx.dr.forms.dto.DRSupportViewDTO;
import mx.dr.forms.dto.DRValidatorDefinition;
import mx.dr.forms.dto.GenericDtoIN;
import mx.dr.forms.view.DRFellowLink;
import mx.dr.forms.view.DRField;
import mx.dr.forms.view.DRRender;
import mx.dr.forms.view.component.DRGrid;
import mx.dr.forms.view.component.DRGroupBox;
import mx.dr.forms.view.component.DRLabel;
import mx.dr.forms.view.component.DRTabBox;
import mx.dr.forms.view.component.DRTabPanel;
import mx.dr.forms.view.render.DRLabelRender;
import mx.dr.forms.view.render.DRListBoxRender;
import mx.dr.forms.view.render.IDRRendereable;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.ext.Constrainted;

/**
 *
 * <br/>
 * Encargada de construir la vista procesando lo definido por el desarrollador.
 * @author Jorge Luis Martinez Ramirez
 * @version 1.0
 * @since 13/08/2011
 * @since v0.5
 */
public class DRFormBuilder {
    /**
	* mapa de atributos definidos como accion CREATE.
	**/
    private static Map<String, List<DRFieldDto>> ADD_MAP = new HashMap<String, List<DRFieldDto>>();
	/**
	* comparador para ordenar los atributos definidos como accion CREATE.
	*/
    private static Comparator COMPARATOR = new DRFieldComparator();
	
    private static Map<String, List<DRFieldDto>> EDIT_MAP = new HashMap<String, List<DRFieldDto>>();
    //private static Comparator EDIT_COMPARATOR = new DRFieldComparator(FormActions.EDIT);
    private static Map<String, List<DRFieldDto>> SEARCH_MAP = new HashMap<String, List<DRFieldDto>>();
    //private static Comparator SEARCH_COMPARATOR = new DRFieldComparator(FormActions.SEARCH);
    private static Map<String, List<DRFieldDto>> READ_MAP = new HashMap<String, List<DRFieldDto>>();
    /**
	* constructor que previene de ser instanciado
	**/
    private DRFormBuilder() {
    }
    /**
	* construye y valua los atributos del objeto modelo de vista definido.
	* @param action que invoca el usuario.
	* @param dtoClass clase del modelo de vista.
	* @param self componente padre.
	* @return objeto valuado con los valores introducidos por el usuario.
	**/
    public static Object fillDTO(String action, Class dtoClass, Component self) throws Exception {
        System.out.println(self);

        Object dto = dtoClass.newInstance();


        List<DRFieldDto> fields = null;
        if (action.equals(FormActions.ADD.name())) {
            fields = getDRFields(dtoClass, FormActions.ADD);
        } else if (action.equals(FormActions.EDIT.name())) {
            fields = getDRFields(dtoClass, FormActions.EDIT);
        } else if (action.equals(FormActions.SEARCH.name())) {
            fields = getDRFields(dtoClass, FormActions.SEARCH);
        }
        Component comp;
        Field field = null;
        Annotation drComponent = null;
        Class<? extends IDRRendereable> renderable = null;
        DRComponentDefinition definition;
        Object value;
        for (DRFieldDto fieldDto : fields) {
            field = fieldDto.getField();
            field.setAccessible(true);

            definition = DRComponentDefinition.define(field);
            renderable = definition.getRenderable();
            drComponent = definition.getDrTag();
            DRLabelRender lrend = new DRLabelRender();
            field.setAccessible(true);

            if (renderable != null) {
                comp = self.getFellow(field.getName());
                value = renderable.newInstance().value(drComponent, comp, field.getType());
                if (value != null) {
                    field.set(dto, value);
                }

            } else {
                field.set(dto, fillDTO(action, field.getType(), self));
            }


        }

        return dto;
    }
    /**
	* construye la vista basandose en el modelo definido.
	* @param comp componente dentro del cual se construira la vista.
	* @param dtoClass clase que define el modelo de la vista.
	* @param dtoValue objeto valuado que define los valores iniciales de los componentes.
	* @param action accion que define el comportamiento de la vista.
	* @param origin componente en el que definio el controlador.
	* @throws Exception si ocurre cualquier error en cualquier punto.
	*
	**/
    public static void buildForm(Component comp, Class dtoClass, Object dtoValue, String action, Component origin) throws Exception {

        DRSupportViewDTO dtoSup;

        DRGrid drGrid;
        DRTabBox drTabBox;
        Grid grid = null;
        Row arow = null;
        Tabbox tabbox = null;
        Field field = null;
        if (action.equals(FormActions.ADD.name())) {
            DRField addField = null;
            drGrid = (DRGrid) dtoClass.getAnnotation(DRGrid.class);
            drTabBox = (DRTabBox) dtoClass.getAnnotation(DRTabBox.class);

            int cols = 0;
            int itCols = 1;

            if (drGrid != null) {
                grid = new Grid();
                grid.setId(drGrid.id());
                grid.setWidth(drGrid.width());
                cols = drGrid.cols();
                arow = new Row();
                grid.getChildren().add(new Rows());
                comp.getChildren().add(0, grid);

            } else if (drTabBox != null) {
                tabbox = new Tabbox();
                tabbox.setWidth(drTabBox.width());
                tabbox.getChildren().add(new Tabs());
                tabbox.getChildren().add(new Tabpanels());
                comp.getChildren().add(0, tabbox);
            } else if (drTabBox == null && drGrid == null && comp instanceof Grid) {
                grid = (Grid) comp;
                cols = drGrid.cols();
                arow = new Row();
            }

            for (DRFieldDto drFieldDto : getDRFields(dtoClass, FormActions.ADD)) {
                field = drFieldDto.getField();
                field.setAccessible(true);

                addField = drFieldDto.getDrField();
                dtoSup = buildOneElement(arow, cols, itCols, field, comp, dtoValue, action, origin, grid, tabbox, addField);
                itCols = dtoSup.getCont();
                arow = dtoSup.getArow();
            }
        } else if (action.equals(FormActions.EDIT.name())) {
            DRField editField = null;
            drGrid = (DRGrid) dtoClass.getAnnotation(DRGrid.class);
            drTabBox = (DRTabBox) dtoClass.getAnnotation(DRTabBox.class);

            int cols = 0;
            int itCols = 1;

            if (drGrid != null) {
                grid = new Grid();
                grid.setId(drGrid.id());
                grid.setWidth(drGrid.width());
                cols = drGrid.cols();
                arow = new Row();
                grid.getChildren().add(new Rows());
                comp.getChildren().add(0, grid);

            } else if (drTabBox != null) {
                tabbox = new Tabbox();
                tabbox.setWidth(drTabBox.width());
                tabbox.getChildren().add(new Tabs());
                tabbox.getChildren().add(new Tabpanels());
                comp.getChildren().add(0, tabbox);
            } else if (drTabBox == null && drGrid == null && comp instanceof Grid) {
                grid = (Grid) comp;
                cols = drGrid.cols();
                arow = new Row();
            }

            for (DRFieldDto drFieldDto : getDRFields(dtoClass, FormActions.EDIT)) {
                field = drFieldDto.getField();
                field.setAccessible(true);

                editField = drFieldDto.getDrField();
                dtoSup = buildOneElement(arow, cols, itCols, field, comp, dtoValue, action, origin, grid, tabbox, editField);
                itCols = dtoSup.getCont();
                arow = dtoSup.getArow();
            }
        } else if (action.equals(FormActions.SEARCH.name())) {
            DRField searchField = null;
            drGrid = (DRGrid) dtoClass.getAnnotation(DRGrid.class);
            drTabBox = (DRTabBox) dtoClass.getAnnotation(DRTabBox.class);

            int cols = 0;
            int itCols = 1;

            if (drGrid != null) {
                grid = new Grid();
                grid.setId(drGrid.id());
                grid.setWidth(drGrid.width());
                cols = drGrid.cols();
                arow = new Row();
                grid.getChildren().add(new Rows());
                comp.getChildren().add(0, grid);

            } else if (drTabBox != null) {
                tabbox = new Tabbox();
                tabbox.setWidth(drTabBox.width());
                tabbox.getChildren().add(new Tabs());
                tabbox.getChildren().add(new Tabpanels());
                comp.getChildren().add(0, tabbox);
            } else if (drTabBox == null && drGrid == null && comp instanceof Grid) {
                grid = (Grid) comp;
                cols = 2;
                arow = new Row();
            }

            for (DRFieldDto drFieldDto : getDRFields(dtoClass, FormActions.SEARCH)) {
                field = drFieldDto.getField();
                field.setAccessible(true);

                searchField = drFieldDto.getDrField();
                dtoSup = buildOneElement(arow, cols, itCols, field, comp, dtoValue, action, origin, grid, tabbox, searchField);
                itCols = dtoSup.getCont();
                arow = dtoSup.getArow();
            }
            DRFellowLink drSearchAction = DRGeneralViewUtils.readAnnotation(dtoClass, FormActions.SEARCH);
            if (drSearchAction != null) {
                DRListBoxRender lrender = new DRListBoxRender();
                Listbox alist = null;
                Component compresult = lrender.render(drSearchAction.resultsComponent(), null, null, dtoValue);
                origin.appendChild(compresult);
                alist = compresult instanceof Listbox ? (Listbox) compresult : (Listbox) compresult.getFellow(drSearchAction.resultsComponent().id());

                if (drSearchAction.loadOnInit()) {
                    GenericDtoIN dtoIn = new GenericDtoIN();
                    dtoIn.setSessionParams(DRGeneralViewUtils.buildMapSessionParams(drSearchAction.sessionParams(),
                            origin.getDesktop().getSession().getAttributes()));
                    dtoIn.setViewDTO(dtoClass.newInstance());
                    System.out.println("val: "+ dtoIn.getViewDTO());
                    DRGeneralViewUtils.fillListModel(drSearchAction, alist, dtoIn, origin);
                }
            }
        } else if (action.equals(FormActions.READ.name())) {
            DRField drReadField;
            System.out.println("redaction");
            Component parent = null;
            DRLabelRender lrend = new DRLabelRender();
            DRRender drRender;
            Annotation drComponent = null;
            Class<? extends IDRRendereable> renderable = null;
            DRComponentDefinition definition;
            for (DRFieldDto drFieldDto : getDRFields(dtoClass, FormActions.READ)) {
                field = drFieldDto.getField();
                field.setAccessible(true);
                definition = DRComponentDefinition.define(field);
                renderable = definition.getRenderable();
                drComponent = definition.getDrTag();
                System.out.println(field.getName());
                drReadField = drFieldDto.getDrField();
                parent = origin.getFellow(drReadField.readParent());
                if (!drReadField.label().key().equals(DRLabel.NO_LABEL)) {
                    parent.appendChild(lrend.render(drReadField.label(), null, null, null));
                }

                if (renderable != null) {
                	System.out.println(((Annotation)drComponent).annotationType());
                    parent.appendChild(renderable.newInstance().render((Annotation)drComponent, field.getName(), dtoValue == null ? null : field.get(dtoValue), dtoValue));
                } else {
                    buildForm(comp, field.getType(), dtoValue == null ? null : field.get(dtoValue), action, origin);
                }

            }

        }
    }
    
	/**
	* construye un componente, cualquiera definido.
	* @param arow fila del grid donde se construira el elemento.
	* @param cols numero total de columas permisibles en el grid.
	* @param itCols numero acual de columnas del grid.
	* @param field atributo que sera representado por un componente.
	* @param comp componente dentro del cual se construira la vista.
	* @param dtoValue objeto valuado que define los valores iniciales de los componentes.
	* @param action accion que define el comportamiento de la vista.
	* @param origin componente en el que definio el controlador.
	* @param grid grid en que se van a crear los componentes.
    * @param tabbox componente tab si aplica.
	* @param drField definicion del comportamiento del atributo como componente.
	* @return objeto que encapsula la nueva fila y columna actual.
	*/
    private static DRSupportViewDTO buildOneElement(Row arow, final int cols, int itCols, final Field field, final Component comp, final Object dtoValue, final String action, final Component origin, final Grid grid, final Tabbox tabbox, final DRField drField) throws Exception {

        DRTabPanel drTabPanel;
        DRGroupBox drGroupBox;
        drTabPanel = field.getAnnotation(DRTabPanel.class);
        drGroupBox = field.getAnnotation(DRGroupBox.class);

        Annotation drComponent = null;
        Class<? extends IDRRendereable> renderable = null;
        DRComponentDefinition definition;
        definition = DRComponentDefinition.define(field);
        renderable = definition.getRenderable();
        drComponent = definition.getDrTag();
        DRLabelRender lrend = new DRLabelRender();
        field.setAccessible(true);
        Component mychild;
        DRLiveConstraint constraint;
        if (renderable != null) {

            if (drField != null) {
                arow.appendChild(lrend.render(drField.label(), null, null, null));

            }
            arow = getRow(arow, grid.getRows(), itCols, cols);
            itCols = getCols(itCols, cols);
            mychild=renderable.newInstance().render(drComponent, field.getName(), dtoValue == null ? null : field.get(dtoValue), dtoValue);
            arow.appendChild(mychild);
            if(drField.liveValidate()){
                constraint = new DRLiveConstraint(DRValidatorDefinition.define(field));
                constraint.setDto(dtoValue);
                constraint.setLabelKey(drField.label().key());
                constraint.setAction(FormActions.getValue(action));
                ((Constrainted)mychild).setConstraint(constraint);
            }
            
            arow = getRow(arow, grid.getRows(), itCols, cols);
            itCols = getCols(itCols, cols);

        } else if (drTabPanel != null) {
            if (drField != null) {
                tabbox.getTabs().appendChild(new Tab(Labels.getLabel(drField.label().key())));
            }
            Tabpanel atab = new Tabpanel();
            if (drGroupBox != null) {
                addGroupBox(drGroupBox, atab, field.getType(), action, origin, dtoValue == null ? null : field.get(dtoValue),drField.label());
            } else {
                buildForm(atab, field.getType(), dtoValue == null ? null : field.get(dtoValue), action, origin);
            }
            tabbox.getTabpanels().appendChild(atab);
        } else if (drGroupBox != null) {
            if (grid != null) {
                addGroupBox(drGroupBox, arow, field.getType(), action, origin, dtoValue == null ? null : field.get(dtoValue),drField.label());
                arow = getRow(arow, grid.getRows(), itCols, cols);
                itCols = getCols(itCols, cols);
            } else {
                addGroupBox(drGroupBox, comp, field.getType(), action, origin, dtoValue == null ? null : field.get(dtoValue),drField.label());
            }
        } else {
            buildForm(grid, field.getType(), dtoValue == null ? null : field.get(dtoValue), action, origin);
        }
        return new DRSupportViewDTO(arow, itCols);
    }
    /**
	* obtiente los atributos ordenados aplicables a una accion.
	* @param dtoClass clase que define el modelo de vista.
	* @param action accion aplicable.
	* @param lista ordenada de las definiciones de cada atributo aplicable a la accion.
	**/
    public static List<DRFieldDto> getDRFields(Class dtoClass, FormActions action) {
        Map<String, List<DRFieldDto>> map = null;
        Comparator<DRFieldDto> comparator = COMPARATOR;
        switch (action) {
            case ADD:
                map = ADD_MAP;
                //comparator = ADD_COMPARATOR;
                break;
            case EDIT:
                map = EDIT_MAP;
                //comparator = EDIT_COMPARATOR;
                break;
            case SEARCH:
                map = SEARCH_MAP;
                //comparator = SEARCH_COMPARATOR;
                break;
            case READ:
                map = READ_MAP;
                //comparator = SEARCH_COMPARATOR;
                break;    
        }
        List<DRFieldDto> results = map.get(dtoClass.getName());
        if (results == null) {
            DRField drField;
            results = new ArrayList<DRFieldDto>();
            for (Field field : dtoClass.getDeclaredFields()) {
                field.setAccessible(true);
                drField = DRGeneralViewUtils.readAnnotation(field, action);
                if (drField != null) {
                    results.add(new DRFieldDto(field, drField));
                }
            }
            Collections.sort(results, comparator);
            map.put(dtoClass.getName(), results);
        }
        return results;
    }
    /**
	* @param arow siguiente fila.
	* @param parent componente padre de la fila actual.
	* @param current numero de columa actual.
	* @param limit numero maximo de columnas.
	*@return fila acual aplicable.
	*/
    private static Row getRow(Row arow, Component parent, int current, int limit) {
        parent.appendChild(arow);
        return current == limit ? new Row() : arow;
    }
    /**
	* @param current numero de columa actual.
	* @param limit numero maximo de columnas.	 
	* @return numero de columnas para la fila actual.
	**/
    private static int getCols(int current, int limit) {
        return current == limit ? 1 : ++current;
    }
    /**
	*
	*
	 * @param drGroupBox definicion del comportamiento del groupbox
	 * @param parent componente en el que se creara el groupbox.
	 * @param dtoClass clase que define el modelo de vista.
	 * @param comp componente dentro del cual se construira la vista.
	 * @param dtoValue objeto valuado que define los valores iniciales de los componentes.
	 * @param action accion que define el comportamiento de la vista.
	 * @param label etiqueta
	 */
    private static void addGroupBox(DRGroupBox drGroupBox, Component parent, Class dtoClass, String action, Component comp, Object dtoValue,DRLabel label) throws Exception {
        Groupbox group = new Groupbox();
        group.setMold(drGroupBox.mold().name().toLowerCase().replaceAll("_", ""));
        group.setWidth(drGroupBox.width());
        group.setOpen(drGroupBox.open());
        buildForm(group, dtoClass, dtoValue, action, comp);

        if (!label.key().equals(DRLabel.NO_LABEL)) {
            group.getChildren().add(0, new Caption(Labels.getLabel(label.key())));
        }

        parent.getChildren().add(0, group);
    }
}

