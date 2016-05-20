/*
 * Copyright 2007 ATIKASOFT CIA. LTDA - ECUADOR
 * Licensed under the ATIKASOFT License, Version 1.0 (the "License"); you may not use this
 * file. You may obtain a copy of the License at http://www.atikasoft.com.ec Unless required
 * by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.taurus.web.gwt.client.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.taurus.web.gwt.client.graphics.LineGraphic;
import org.taurus.web.gwt.client.store.StoreProject;
import org.taurus.web.gwt.client.util.Util;
import org.taurus.web.gwt.client.util.enumerations.Month;
import org.taurus.web.gwt.client.vo.MilestoneMonthVO;
import org.taurus.web.gwt.client.vo.MilestoneVO;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.core.ExtElement;
import com.gwtext.client.core.TextAlign;
import com.gwtext.client.data.ArrayReader;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Toolbar;
import com.gwtext.client.widgets.ToolbarButton;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.FormPanel;
import com.gwtext.client.widgets.form.NumberField;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.grid.BaseColumnConfig;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.EditorGridPanel;
import com.gwtext.client.widgets.grid.GridEditor;
import com.gwtext.client.widgets.grid.GridPanel;
import com.gwtext.client.widgets.grid.Renderer;
import com.gwtext.client.widgets.grid.event.EditorGridListenerAdapter;
import com.gwtext.client.widgets.grid.event.GridRowListenerAdapter;

/**
 * Incluir aqui la descripcion
 * 
 * @author <a href="mailto:info@atikasoft.com.ec">atikasoft</a>
 * 
 * @version
 */
public class BudgetInfoForm extends Form {
	private final NumberFormat nf = NumberFormat.getFormat("#,##0.00");
	private final Renderer renderer = new Renderer() {

		public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum,
				Store store) {
			if (value != null) {
				// //////////////////////////////////////
				double valor = BudgetInfoForm.this.nf.parse(value.toString());
				if (valor != 0) {
					return "<font color='blue'>" + BudgetInfoForm.this.nf.format(valor) + "</font>";
				} else {
					return BudgetInfoForm.this.nf.format(valor);
				}
				// //////////////////////////////////////

			}
			return null;
		}
	};

	private final Renderer uppercaseRenderer = new Renderer() {
		public String render(Object value, CellMetadata cellMetadata, Record record, int rowIndex, int colNum,
				Store store) {
			if (value != null) {
				return value.toString().toUpperCase();
			}
			return null;
		}
	};
	BudgetInfoForm instance = null;
	final FormPanel backPanel = new FormPanel();
	private Panel graphPanel;

	double totalComprometido;
	double totalejecutado;
	double totaldevengado;
	double totalInv;

	int anioSeleccion = 2009;
	int pos = 0;

	double valorAcumuladoMesCom = 0;
	double valorAcumuladoMesEje = 0;
	double valorAcumuladoMesDev = 0;
	double enceraAcumuladoMesPla = 0;
	double enceraAcumuladoMesEje = 0;

	Toolbar bottomToolbarinicio = new Toolbar();
	Toolbar bottomToolbarmedio = new Toolbar();

	Panel panel = new Panel();
	Record recordSelected;
	Label anio = new Label("A?? FISCAL   " + this.anioSeleccion);// /
	final GridPanel gridPresu = new GridPanel();

	private String backPanelId = "";
	RecordDef recordDef1 = new RecordDef(new FieldDef[] { new StringFieldDef("test"), new StringFieldDef("total"),
			new StringFieldDef("enero"), new StringFieldDef("febrero"), new StringFieldDef("marzo"),
			new StringFieldDef("abril"), new StringFieldDef("mayo"), new StringFieldDef("junio"),
			new StringFieldDef("julio"), new StringFieldDef("agosto"), new StringFieldDef("septiembre"),
			new StringFieldDef("octubre"), new StringFieldDef("noviembre"), new StringFieldDef("diciembre") });

	Object[][] data1 = this.getCompanyData();
	MemoryProxy proxy1 = new MemoryProxy(this.data1);
	ArrayReader reader1 = new ArrayReader(this.recordDef1);
	final Store store1 = new Store(this.proxy1, this.reader1);

	final EditorGridPanel grid = new EditorGridPanel();

	final RecordDef recordDef = StoreProject.createRecordDefMilestone();

	private final Store store = StoreProject.createStore(null, StoreProject.simpleRecordMilestone());
	private final Store totalProjectStore = StoreProject.createStore(null, StoreProject.simpleRecordSumaryComposite());

	final GridPanel gridTotales = new GridPanel();

	public BudgetInfoForm() {

	}

	public BudgetInfoForm(String datosHitos, String anio, String idProject, boolean tieneAcceso) {

		this.anioSeleccion = Integer.parseInt(anio);
		final VerticalPanel vp = new VerticalPanel();
		this.init(vp, datosHitos, idProject, tieneAcceso);
	}

	private void actualizarDatosGridTotales(String idProject) {
		AsyncCallback<String> gridTotalesCallBack = new AsyncCallback<String>() {

			public void onFailure(Throwable caught) {
				MessageBox.alert("No se han podido recuperar los datos para presentar el grid de totales del proyecto");
				GWT.log("No se han podido recuperar los datos para presentar el grid de totales del proyecto", caught);
			}

			public void onSuccess(String result) {
				BudgetInfoForm.this.totalProjectStore.commitChanges();
				BudgetInfoForm.this.totalProjectStore.removeAll();
				BudgetInfoForm.this.totalProjectStore.commitChanges();
				BudgetInfoForm.this.totalProjectStore.loadJsonData(result, false);
				BudgetInfoForm.this.totalProjectStore.commitChanges();
				BudgetInfoForm.this.gridTotales.getView().refresh();

			}
		};
		this.serviceWebAdministration.findAllMilestoneMonthByProject(Long.valueOf(idProject), gridTotalesCallBack);
	}

	/**
	 * before year of milestone
	 * 
	 * @param recordHitos
	 * @param currentYear
	 * @param meses
	 */
	private void beforeMilestone(Record[] recordHitos, String currentYear, String[] meses) {

		List<MilestoneVO> hitos = new ArrayList<MilestoneVO>();
		MilestoneVO milestoneVO = null;
		final ExtElement element = Ext.get(this.backPanelId);
		element.mask("Cargando..", true);

		for (Record recordHito : recordHitos) {

			milestoneVO = new MilestoneVO();
			milestoneVO.setAnio(currentYear);
			if (recordHito.getAsString("common") == null) {
				MessageBox.alert("La descripcion de los hitos es obliglatoria, o eliminelos");
				return;
			}
			milestoneVO.setDescripcion(recordHito.getAsString("common").trim());

			List<MilestoneMonthVO> monthsMilestone = new ArrayList<MilestoneMonthVO>();

			for (String mes : meses) {

				String valorPlaneado = recordHito.getAsString(mes + "P");
				if (valorPlaneado != null && valorPlaneado.trim().length() > 0) {
					valorPlaneado = valorPlaneado.trim();
				} else {
					valorPlaneado = "0";
				}

				String valorEjecutado = recordHito.getAsString(mes + "E");
				if (valorEjecutado != null && valorEjecutado.trim().length() > 0) {
					valorEjecutado = valorEjecutado.trim();
				} else {
					valorEjecutado = "0";
				}

				String valorDevengado = recordHito.getAsString(mes + "D");
				if (valorDevengado != null && valorDevengado.trim().length() > 0) {
					valorDevengado = valorDevengado.trim();
				} else {
					valorDevengado = "0";
				}
				if (!valorPlaneado.equals("0") || !valorEjecutado.equals("0") || !valorDevengado.equals("0")) {

					MilestoneMonthVO milestoneMonthVO = new MilestoneMonthVO();
					milestoneMonthVO.setAnio(currentYear);
					milestoneMonthVO.setEjecutado(valorEjecutado);
					milestoneMonthVO.setPlaneado(valorPlaneado);
					milestoneMonthVO.setDevengado(valorDevengado);
					milestoneMonthVO.setMes(mes);
					monthsMilestone.add(milestoneMonthVO);
				}
			}
			milestoneVO.setMonthsMileStoneVO(monthsMilestone);
			hitos.add(milestoneVO);
		}

		this.store.commitChanges();
		this.store.removeAll();
		this.store.commitChanges();

		AsyncCallback<String> beforeAsync = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				element.unmask();
				MessageBox.alert("No se pudo cargar de la pantalla anterior: ");
				GWT.log("mensaje", caught);
			}

			public void onSuccess(String result) {

				element.unmask();
				BudgetInfoForm.this.anioSeleccion = BudgetInfoForm.this.anioSeleccion - 1;

				BudgetInfoForm.this.panel.clear();
				Label anio = new Label("A?? FISCAL  " + BudgetInfoForm.this.anioSeleccion);
				BudgetInfoForm.this.backPanel.setTitle("INFORMACI&#211N PRESEPUESTARIA" + anio);
				BudgetInfoForm.this.gridPresu.setTitle("TOTAL COMPROMETIDO, DEVENGADO, EJECUTADO A\u00d1O "
						+ BudgetInfoForm.this.anioSeleccion);
				BudgetInfoForm.this.panel.add(anio);
				BudgetInfoForm.this.panel.doLayout();

				BudgetInfoForm.this.store.loadJsonData(result, false);
				BudgetInfoForm.this.store.commitChanges();
				BudgetInfoForm.this.grid.doLayout();
				BudgetInfoForm.this.grid.getView().refresh();

				BudgetInfoForm.this.gridPresu.doLayout();
				BudgetInfoForm.this.gridPresu.getView().refresh();
			}

		};
		this.serviceWebAdministration.beforeMilestone(currentYear, hitos, "hitos", beforeAsync);
	}

	private List<Object[]> calcularValoresPorMes(String[] meses, Record[] records, Record[] totales) {

		List<Object[]> acumulados = new ArrayList<Object[]>();

		for (String mes : meses) {
			this.valorAcumuladoMesCom = 0;
			this.valorAcumuladoMesEje = 0;
			this.valorAcumuladoMesDev = 0;
			for (Record record : records) {
				try {

					String p = record.getAsString(mes + "P");
					String e = record.getAsString(mes + "E");
					String d = record.getAsString(mes + "D");

					if (p == null || p.trim().length() == 0) {
						p = "0";
					}
					if (e == null || e.trim().length() == 0) {
						e = "0";
					}

					if (d == null || d.trim().length() == 0) {
						d = "0";
					}
					this.valorAcumuladoMesCom = this.valorAcumuladoMesCom + Double.parseDouble(p);
					this.valorAcumuladoMesEje = this.valorAcumuladoMesEje + Double.parseDouble(e);
					this.valorAcumuladoMesDev = this.valorAcumuladoMesDev + Double.parseDouble(d);

				} catch (NumberFormatException e) {
					MessageBox.alert("el valor introducido no es numero");
					return null;
				}
			}

			this.totalComprometido = this.totalComprometido + this.valorAcumuladoMesCom;
			this.totalejecutado = this.totalejecutado + this.valorAcumuladoMesEje;
			this.totaldevengado = this.totaldevengado + this.valorAcumuladoMesDev;

			totales[0].set("total", this.totalComprometido);
			totales[1].set("total", this.totaldevengado);
			totales[2].set("total", this.totalejecutado);

			totales[0].set(mes, this.valorAcumuladoMesCom);
			totales[1].set(mes, this.valorAcumuladoMesDev);
			totales[2].set(mes, this.valorAcumuladoMesEje);

			acumulados.add(new Object[] { mes, Double.valueOf(this.valorAcumuladoMesCom),
					Double.valueOf(this.valorAcumuladoMesEje), Double.valueOf(this.valorAcumuladoMesDev) });
		}
		return acumulados;
	}

	private List<Object[]> determinarValoresEjecutadosPorMes(List<String> meses, Record[] records) {
		List<Object[]> result = new ArrayList<Object[]>();
		for (String mes : meses) {
			for (Record record : records) {
				if (record.getAsString(mes + "E") != null && record.getAsString(mes + "E").trim() != "") {
					result.add(new Object[] { mes, Month.getMonthNumber(mes), record.getAsString("common"),
							Double.valueOf(record.getAsString(mes + "E")) });
				}
			}
		}

		return result;
	}

	private List<Object[]> determinarValoresPlaneadosPorMes(List<String> meses, Record[] records) {
		List<Object[]> result = new ArrayList<Object[]>();
		for (String mes : meses) {
			for (Record record : records) {
				if (record.getAsString(mes + "P") != null && record.getAsString(mes + "P").trim() != "") {
					result.add(new Object[] { mes, Month.getMonthNumber(mes), record.getAsString("common"),
							Double.valueOf(record.getAsString(mes + "P")) });
				}
			}
		}

		return result;
	}

	private void encerarValor() {
		this.totalComprometido = 0;
		this.totalejecutado = 0;
		this.totaldevengado = 0;
		this.valorAcumuladoMesCom = 0;
		this.valorAcumuladoMesEje = 0;
	}

	private final NumberField getColumnNumberField() {
		NumberField numberField = new NumberField();
		numberField.setAllowDecimals(true);
		numberField.setAllowNegative(false);
		return numberField;
	}

	private Object[][] getCompanyData() {
		return new Object[][] {
				new Object[] { "<b>COMPROMETIDO</b>", "", "", "", "", "", "", "", "", "", "", "", "", "" },
				new Object[] { "<b>DEVENGADO</b>", "", "", "", "", "", "", "", "", "", "", "", "", "" },
				new Object[] { "<b>EJECUTADO</b>", "", "", "", "", "", "", "", "", "", "", "", "", "" } };
	}

	private void init(VerticalPanel vp, String json, final String idProject, final boolean tieneAcceso) {

		vp.clear();
		this.instance = this;
		this.backPanelId = Util.generateRandomId("panelBudge");
		this.backPanel.setId(this.backPanelId);
		this.backPanel.setWidth(890);
		this.backPanel.setHeight(700);
		this.backPanel.setPaddings(15);
		this.backPanel.setBorder(false);
		this.backPanel.setFrame(true);
		this.backPanel.setAutoScroll(true);
		this.backPanel.setTitle("INFORMACI&#211N PRESEPUESTARIA " + this.anioSeleccion);
		this.graphPanel = new Panel();

		this.store.loadJsonData(json, false);
		this.store.commitChanges();

		this.pos = this.store.getCount();

		ColumnConfig commonCol = new ColumnConfig("", "common", 200, false, null, "common");
		TextField commonText = new TextField();
		commonText.setAllowBlank(false);
		commonText.setBlankText("Debe ingresar la descripci\u00f3n");
		commonText.setInvalidText("La descripci\u00f3n es obligatoria y puede tener una longitud m\u00e1xima de 512");
		commonText.setValue("NUEVO HITO");
		commonText.setMaxLength(512);
		commonText.addStyleName("uppercase-text");
		commonCol.setEditor(new GridEditor(commonText));
		commonCol.setHeader("<b>Hitos</b>");
		commonCol.setAlign(TextAlign.LEFT);
		commonCol.setTooltip("Descripci\u00f3n del Hito");
		commonCol.setRenderer(this.uppercaseRenderer);

		ColumnConfig colEneP = new ColumnConfig("", "eneroP", 80, false, null, "eneroP");
		colEneP.setEditor(new GridEditor(this.getColumnNumberField()));
		colEneP.setHeader("<b><font color='#fbd609'>PLA </font>ENE</b>");
		colEneP.setAlign(TextAlign.RIGHT);
		colEneP.setTooltip("Comprometido Enero");
		colEneP.setRenderer(this.renderer);
		ColumnConfig colEneE = new ColumnConfig("", "eneroE", 80, false, null, "eneroE");
		colEneE.setEditor(new GridEditor(this.getColumnNumberField()));
		colEneE.setHeader("<b><font color='#fb5900'>EJE </font>ENE</b>");
		colEneE.setAlign(TextAlign.RIGHT);
		colEneE.setTooltip("Ejecutado Enero");
		colEneE.setRenderer(this.renderer);
		ColumnConfig colEneroD = new ColumnConfig("", "eneroD", 80, false, null, "eneroD");
		colEneroD.setEditor(new GridEditor(this.getColumnNumberField()));
		colEneroD.setHeader("<b><font color='#d26225'>DEV </font>ENE</b>");
		colEneroD.setAlign(TextAlign.RIGHT);
		colEneroD.setTooltip("Devengado Enero");
		colEneroD.setRenderer(this.renderer);

		ColumnConfig colFebP = new ColumnConfig("", "febreroP", 80, false, null, "febreroP");
		colFebP.setEditor(new GridEditor(this.getColumnNumberField()));
		colFebP.setHeader("<b><font color='#fbd609'>PLA </font>FEB</b>");
		colFebP.setAlign(TextAlign.RIGHT);
		colFebP.setTooltip("Comprometido Febrero");
		colFebP.setRenderer(this.renderer);
		ColumnConfig colFebE = new ColumnConfig("", "febreroE", 80, false, null, "febreroE");
		colFebE.setEditor(new GridEditor(this.getColumnNumberField()));
		colFebE.setHeader("<b><font color='#fb5900'>EJE </font>FEB</b>");
		colFebE.setAlign(TextAlign.RIGHT);
		colFebE.setTooltip("Ejecutado Febrero");
		colFebE.setRenderer(this.renderer);
		ColumnConfig colFebD = new ColumnConfig("", "febreroD", 80, false, null, "febreroD");
		colFebD.setEditor(new GridEditor(this.getColumnNumberField()));
		colFebD.setHeader("<b><font color='#d26225'>DEV </font>FEB</b>");
		colFebD.setAlign(TextAlign.RIGHT);
		colFebD.setTooltip("Devengado Febrero");
		colFebD.setRenderer(this.renderer);

		ColumnConfig colMarP = new ColumnConfig("", "marzoP", 80, false, null, "marzoP");
		colMarP.setEditor(new GridEditor(this.getColumnNumberField()));
		colMarP.setHeader("<b><font color='#fbd609'>PLA </font>MAR</b>");
		colMarP.setAlign(TextAlign.RIGHT);
		colMarP.setRenderer(this.renderer);
		colMarP.setTooltip("Comprometido Marzo");
		ColumnConfig colMarE = new ColumnConfig("", "marzoE", 80, false, null, "marzoE");
		colMarE.setEditor(new GridEditor(this.getColumnNumberField()));
		colMarE.setHeader("<b><font color='#fb5900'>EJE </font>MAR</b>");
		colMarE.setAlign(TextAlign.RIGHT);
		colMarE.setTooltip("Ejecutado Marzo");
		colMarE.setRenderer(this.renderer);
		ColumnConfig colMarD = new ColumnConfig("", "marzoD", 80, false, null, "marzoD");
		colMarD.setEditor(new GridEditor(this.getColumnNumberField()));
		colMarD.setHeader("<b><font color='#d26225'>DEV </font>MAR</b>");
		colMarD.setAlign(TextAlign.RIGHT);
		colMarD.setTooltip("Devengado Marzo");
		colMarD.setRenderer(this.renderer);

		ColumnConfig colAbrP = new ColumnConfig("", "abrilP", 80, false, null, "abrilP");
		colAbrP.setEditor(new GridEditor(this.getColumnNumberField()));
		colAbrP.setHeader("<b><font color='#fbd609'>PLA </font>ABR</b>");
		colAbrP.setAlign(TextAlign.RIGHT);
		colAbrP.setTooltip("Comprometido Abril");
		colAbrP.setRenderer(this.renderer);
		ColumnConfig colAbrE = new ColumnConfig("", "abrilE", 80, false, null, "abrilE");
		colAbrE.setEditor(new GridEditor(this.getColumnNumberField()));
		colAbrE.setHeader("<b><font color='#fb5900'>EJE </font>ABR</b>");
		colAbrE.setAlign(TextAlign.RIGHT);
		colAbrE.setTooltip("Comprometido Abril");
		colAbrE.setRenderer(this.renderer);
		ColumnConfig colAbrD = new ColumnConfig("", "abrilD", 80, false, null, "abrilD");
		colAbrD.setEditor(new GridEditor(this.getColumnNumberField()));
		colAbrD.setHeader("<b><font color='#d26225'>DEV </font>ABR</b>");
		colAbrD.setAlign(TextAlign.RIGHT);
		colAbrD.setTooltip("Devengado Abril");
		colAbrD.setRenderer(this.renderer);
		ColumnConfig colMayP = new ColumnConfig("", "mayoP", 80, false, null, "mayoP");
		colMayP.setEditor(new GridEditor(this.getColumnNumberField()));
		colMayP.setHeader("<b><font color='#fbd609'>PLA </font>MAY</b>");
		colMayP.setAlign(TextAlign.RIGHT);
		colMayP.setTooltip("Comprometido Mayo");
		colMayP.setRenderer(this.renderer);
		ColumnConfig colMayE = new ColumnConfig("", "mayoE", 80, false, null, "mayoE");
		colMayE.setEditor(new GridEditor(this.getColumnNumberField()));
		colMayE.setHeader("<b><font color='#fb5900'>EJE </font>MAY</b>");
		colMayE.setAlign(TextAlign.RIGHT);
		colMayE.setTooltip("Ejecutado Mayo");
		colMayE.setRenderer(this.renderer);
		ColumnConfig colMayD = new ColumnConfig("", "mayoD", 80, false, null, "mayoD");
		colMayD.setEditor(new GridEditor(this.getColumnNumberField()));
		colMayD.setHeader("<b><font color='#d26225'>DEV </font>MAY</b>");
		colMayD.setAlign(TextAlign.RIGHT);
		colMayD.setTooltip("Devengado Mayo");
		colMayD.setRenderer(this.renderer);

		ColumnConfig colJunP = new ColumnConfig("", "junioP", 80, false, null, "junioP");
		colJunP.setEditor(new GridEditor(this.getColumnNumberField()));
		colJunP.setHeader("<b><font color='#fbd609'>PLA </font>JUN</b>");
		colJunP.setAlign(TextAlign.RIGHT);
		colJunP.setTooltip("Comprometido Junio");
		colJunP.setRenderer(this.renderer);
		ColumnConfig colJunE = new ColumnConfig("", "junioE", 80, false, null, "junioE");
		colJunE.setEditor(new GridEditor(this.getColumnNumberField()));
		colJunE.setHeader("<b><font color='#fb5900'>EJE </font>JUN</b>");
		colJunE.setAlign(TextAlign.RIGHT);
		colJunE.setTooltip("Ejecutado Junio");
		colJunE.setRenderer(this.renderer);
		ColumnConfig colJunD = new ColumnConfig("", "junioD", 80, false, null, "junioD");
		colJunD.setEditor(new GridEditor(this.getColumnNumberField()));
		colJunD.setHeader("<b><font color='#d26225'>DEV </font>JUN</b>");
		colJunD.setAlign(TextAlign.RIGHT);
		colJunD.setTooltip("Devengado Junio");
		colJunD.setRenderer(this.renderer);
		ColumnConfig colJulP = new ColumnConfig("", "julioP", 80, false, null, "julioP");
		colJulP.setEditor(new GridEditor(this.getColumnNumberField()));
		colJulP.setHeader("<b><font color='#fbd609'>PLA </font>JUL</b>");
		colJulP.setAlign(TextAlign.RIGHT);
		colJulP.setTooltip("Comprometido Julio");
		colJulP.setRenderer(this.renderer);
		ColumnConfig colJulE = new ColumnConfig("", "julioE", 80, false, null, "julioE");
		colJulE.setEditor(new GridEditor(this.getColumnNumberField()));
		colJulE.setHeader("<b><font color='#fb5900'>EJE </font>JUL</b>");
		colJulE.setAlign(TextAlign.RIGHT);
		colJulE.setTooltip("Ejecutado Julio");
		colJulE.setRenderer(this.renderer);
		ColumnConfig colJulD = new ColumnConfig("", "julioD", 80, false, null, "julioD");
		colJulD.setEditor(new GridEditor(this.getColumnNumberField()));
		colJulD.setHeader("<b><font color='#d26225'>DEV </font>JUL</b>");
		colJulD.setAlign(TextAlign.RIGHT);
		colJulD.setTooltip("Devengado Julio");
		colJulD.setRenderer(this.renderer);
		ColumnConfig colAgoP = new ColumnConfig("", "agostoP", 80, false, null, "agostoP");
		colAgoP.setEditor(new GridEditor(this.getColumnNumberField()));
		colAgoP.setHeader("<b><font color='#fbd609'>PLA </font>AGO</b>");
		colAgoP.setAlign(TextAlign.RIGHT);
		colAgoP.setTooltip("Comprometido Agosto");
		colAgoP.setRenderer(this.renderer);
		ColumnConfig colAgoE = new ColumnConfig("", "agostoE", 80, false, null, "agostoE");
		colAgoE.setEditor(new GridEditor(this.getColumnNumberField()));
		colAgoE.setHeader("<b><font color='#fb5900'>EJE </font>AGO</b>");
		colAgoE.setAlign(TextAlign.RIGHT);
		colAgoE.setTooltip("Ejecutado Agosto");
		colAbrE.setRenderer(this.renderer);
		ColumnConfig colAgoD = new ColumnConfig("", "agostoD", 80, false, null, "agostoD");
		colAgoD.setEditor(new GridEditor(this.getColumnNumberField()));
		colAgoD.setHeader("<b><font color='#d26225'>DEV </font>AGO</b>");
		colAgoD.setAlign(TextAlign.RIGHT);
		colAgoD.setTooltip("Devengado Agosto");
		colAgoD.setRenderer(this.renderer);
		ColumnConfig colSepP = new ColumnConfig("", "septiembreP", 80, false, null, "septiembreP");
		colSepP.setEditor(new GridEditor(this.getColumnNumberField()));
		colSepP.setHeader("<b><font color='#fbd609'>PLA </font>SEP</b>");
		colSepP.setAlign(TextAlign.RIGHT);
		colSepP.setTooltip("Comprometido Septiembre");
		colSepP.setRenderer(this.renderer);
		ColumnConfig colSepE = new ColumnConfig("", "septiembreE", 80, false, null, "septiembreE");
		colSepE.setEditor(new GridEditor(this.getColumnNumberField()));
		colSepE.setHeader("<b><font color='#fb5900'>EJE </font>SEP</b>");
		colSepE.setAlign(TextAlign.RIGHT);
		colSepE.setTooltip("Ejecutado Septiembre");
		colSepE.setRenderer(this.renderer);
		ColumnConfig colSepD = new ColumnConfig("", "septiembreD", 80, false, null, "septiembreD");
		colSepD.setEditor(new GridEditor(this.getColumnNumberField()));
		colSepD.setHeader("<b><font color='#d26225'>DEV </font>SEP</b>");
		colSepD.setAlign(TextAlign.RIGHT);
		colSepD.setTooltip("Devengado Septiembre");
		colSepD.setRenderer(this.renderer);
		ColumnConfig colOctP = new ColumnConfig("", "octubreP", 80, false, null, "octubreP");
		colOctP.setEditor(new GridEditor(this.getColumnNumberField()));
		colOctP.setHeader("<b><font color='#fbd609'>PLA </font>OCT</b>");
		colOctP.setAlign(TextAlign.RIGHT);
		colOctP.setTooltip("Comprometido Octubre");
		colOctP.setRenderer(this.renderer);
		ColumnConfig colOctE = new ColumnConfig("", "octubreE", 80, false, null, "octubreE");
		colOctE.setEditor(new GridEditor(this.getColumnNumberField()));
		colOctE.setHeader("<b><font color='#fb5900'>EJE </font>OCT</b>");
		colOctE.setAlign(TextAlign.RIGHT);
		colOctE.setTooltip("Ejecutado Octubre");
		colOctE.setRenderer(this.renderer);
		ColumnConfig colOctD = new ColumnConfig("", "octubreD", 80, false, null, "octubreD");
		colOctD.setEditor(new GridEditor(this.getColumnNumberField()));
		colOctD.setHeader("<b><font color='#d26225'>DEV </font>OCT</b>");
		colOctD.setAlign(TextAlign.RIGHT);
		colOctD.setTooltip("Devengado Octubre");
		colOctD.setRenderer(this.renderer);
		ColumnConfig colNovP = new ColumnConfig("", "noviembreP", 80, false, null, "noviembreP");
		colNovP.setEditor(new GridEditor(this.getColumnNumberField()));
		colNovP.setHeader("<b><font color='#fbd609'>PLA </font>NOV</b>");
		colNovP.setAlign(TextAlign.RIGHT);
		colNovP.setTooltip("Comprometido Noviembre");
		colNovP.setRenderer(this.renderer);
		ColumnConfig colNovE = new ColumnConfig("", "noviembreE", 80, false, null, "noviembreE");
		colNovE.setEditor(new GridEditor(this.getColumnNumberField()));
		colNovE.setHeader("<b><font color='#fb5900'>EJE </font>NOV</b>");
		colNovE.setAlign(TextAlign.RIGHT);
		colNovE.setTooltip("Ejecutado Noviembre");
		colNovE.setRenderer(this.renderer);
		ColumnConfig colNovD = new ColumnConfig("", "noviembreD", 80, false, null, "noviembreD");
		colNovD.setEditor(new GridEditor(this.getColumnNumberField()));
		colNovD.setHeader("<b><font color='#d26225'>DEV </font>NOV</b>");
		colNovD.setAlign(TextAlign.RIGHT);
		colNovD.setTooltip("Devengado Noviembre");
		colNovD.setRenderer(this.renderer);

		ColumnConfig colDicP = new ColumnConfig("", "diciembreP", 80, false, null, "diciembreP");
		colDicP.setEditor(new GridEditor(this.getColumnNumberField()));
		colDicP.setHeader("<b><font color='#fbd609'>PLA </font>DIC</b>");
		colDicP.setAlign(TextAlign.RIGHT);
		colDicP.setTooltip("Comprometido Diciembre");
		colDicP.setRenderer(this.renderer);
		ColumnConfig colDicE = new ColumnConfig("", "diciembreE", 80, false, null, "diciembreE");
		colDicE.setEditor(new GridEditor(this.getColumnNumberField()));
		colDicE.setHeader("<b><font color='#fb5900'>EJE </font>DIC</b>");
		colDicE.setAlign(TextAlign.RIGHT);
		colDicE.setTooltip("Ejecutado Diciembre");
		colDicE.setRenderer(this.renderer);
		ColumnConfig colDicD = new ColumnConfig("", "diciembreD", 80, false, null, "diciembreD");
		colDicD.setEditor(new GridEditor(this.getColumnNumberField()));
		colDicD.setHeader("<b><font color='#d26225'>DEV </font>DIC</b>");
		colDicD.setAlign(TextAlign.RIGHT);
		colDicD.setTooltip("Devengado Diciembre");
		colDicD.setRenderer(this.renderer);

		ColumnConfig[] columnConfigs = { commonCol, colEneP, colEneroD, colEneE, colFebP, colFebD, colFebE, colMarP,
				colMarD, colMarE, colAbrP, colAbrD, colAbrE, colMayP, colMayD, colMayE, colJunP, colJunD, colJunE,
				colJulP, colJulD, colJulE, colAgoP, colAgoD, colAgoE, colSepP, colSepD, colSepE, colOctP, colOctD,
				colOctE, colNovP, colNovD, colNovE, colDicP, colDicD, colDicE };

		ColumnModel columnModel = new ColumnModel(columnConfigs);
		columnModel.setDefaultSortable(false);

		// ////////////////metodo listener para obtener dato de click dentro del grid/////////
		this.grid.addGridRowListener(new GridRowListenerAdapter() {
			@Override
			public void onRowClick(final GridPanel grid, final int rowIndex, final EventObject e) {
				BudgetInfoForm.this.recordSelected = BudgetInfoForm.this.store.getAt(rowIndex);
			}
		});

		this.grid.addEditorGridListener(new EditorGridListenerAdapter() {
			@Override
			public boolean doBeforeEdit(GridPanel grid, Record record, String field, Object value, int rowIndex,
					int colIndex) {
				return tieneAcceso;
			}
		});

		// ///////////////panel de botones////////////////////
		Toolbar toolbar = new Toolbar();
		ToolbarButton button = new ToolbarButton("Agregar", new ButtonListenerAdapter() {
			@Override
			public void onClick(Button button, EventObject e) {
				BudgetInfoForm.this.pos = BudgetInfoForm.this.pos + 1;
				Record plant = BudgetInfoForm.this.recordDef.createRecord(new Object[] { "", "", "", "", "", "", "",
						"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
						"", "", "", "", "", "" });
				BudgetInfoForm.this.grid.stopEditing();
				BudgetInfoForm.this.store.commitChanges();
				BudgetInfoForm.this.store.add(plant);
				BudgetInfoForm.this.store.commitChanges();
				BudgetInfoForm.this.grid.startEditing(BudgetInfoForm.this.pos, 0);
				BudgetInfoForm.this.grid.doLayout();
				BudgetInfoForm.this.grid.getView().refresh();
			}
		});
		button.setDisabled(!tieneAcceso);
		toolbar.addButton(button);
		toolbar.addSeparator();

		ToolbarButton button1 = new ToolbarButton("Eliminar", new ButtonListenerAdapter() {
			@Override
			public void onClick(Button button1, EventObject e) {
				BudgetInfoForm.this.store.remove(BudgetInfoForm.this.recordSelected);
				BudgetInfoForm.this.store.commitChanges();
				BudgetInfoForm.this.recordSelected = null;

			}
		});
		button1.setDisabled(!tieneAcceso);
		toolbar.addButton(button1);
		toolbar.addSeparator();

		ToolbarButton button2 = new ToolbarButton("Guardar", new ButtonListenerAdapter() {
			@Override
			public void onClick(Button button1, EventObject e) {
				String[] meses = new String[] { "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
						"agosto", "septiembre", "octubre", "noviembre", "diciembre" };

				List<MilestoneVO> hitos = new ArrayList<MilestoneVO>();
				MilestoneVO milestoneVO = null;

				for (Record recordHito : BudgetInfoForm.this.store.getRecords()) {
					boolean tieneValores = false;

					milestoneVO = new MilestoneVO();
					milestoneVO.setAnio(BudgetInfoForm.this.anioSeleccion + "");
					if (recordHito.getAsString("common") == null) {
						MessageBox
								.alert("La descripci\u00f3n de los hitos es obliglatoria, ingr\u00e9sela o elimine los hitos que no tiene descripci\u00f3n");
						return;
					}
					milestoneVO.setDescripcion(recordHito.getAsString("common").trim());

					List<MilestoneMonthVO> monthsMilestone = new ArrayList<MilestoneMonthVO>();
					for (String mes : meses) {
						String valorPlaneado = recordHito.getAsString(mes + "P");
						if (valorPlaneado != null && valorPlaneado.trim().length() > 0) {
							valorPlaneado = valorPlaneado.trim();
						}

						String valorEjecutado = recordHito.getAsString(mes + "E");
						if (valorEjecutado != null && valorEjecutado.trim().length() > 0) {
							valorEjecutado = valorEjecutado.trim();
						}

						String valorDevengado = recordHito.getAsString(mes + "D");
						if (valorDevengado != null && valorDevengado.trim().length() > 0) {
							valorDevengado = valorDevengado.trim();
						}
						if (valorPlaneado != null || valorEjecutado != null || valorDevengado != null) {

							MilestoneMonthVO milestoneMonthVO = new MilestoneMonthVO();
							milestoneMonthVO.setAnio(BudgetInfoForm.this.anioSeleccion + "");
							milestoneMonthVO.setEjecutado(valorEjecutado != null ? valorEjecutado : "0");
							milestoneMonthVO.setPlaneado(valorPlaneado != null ? valorPlaneado : "0");
							milestoneMonthVO.setDevengado(valorDevengado != null ? valorDevengado : "0");
							milestoneMonthVO.setMes(mes);
							monthsMilestone.add(milestoneMonthVO);
							tieneValores = true;
						}

					}

					if (!tieneValores) {
						MessageBox
								.alert("Debe ingresar por lo menos el valor Comprometido, no se permite ingresar hitos que tengan \u00fanicamente descripci\u00f3n. Una alternativa es remover los hitos que tengan solamente descripci\u00f3n y volver a grabar");
						return;
					}
					milestoneVO.setMonthsMileStoneVO(monthsMilestone);
					hitos.add(milestoneVO);
				}

				AsyncCallback<Void> storeSessionAsync = new AsyncCallback<Void>() {

					public void onFailure(Throwable caught) {
						MessageBox.alert("No se pudo registrar los hitos en la session ");
						GWT.log("mensaje", caught);
					}

					public void onSuccess(Void result) {

						AsyncCallback<Void> storeMilestoneAsync = new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								MessageBox.alert("No se pudo almacenar los hitos ");
								GWT.log("mensaje", caught);
							}

							public void onSuccess(Void result) {

								MessageBox.alert("Hitos Almacenados");
								BudgetInfoForm.this.actualizarDatosGridTotales(idProject);

							}

						};
						BudgetInfoForm.this.serviceWebAdministration.storeProjectMileStones(null, idProject,
								storeMilestoneAsync);
					}
				};
				BudgetInfoForm.this.serviceWebAdministration.storeMilestoneSession(hitos,
						BudgetInfoForm.this.anioSeleccion + "", "hitos", storeSessionAsync);
			}
		});
		button2.setDisabled(!tieneAcceso);
		toolbar.addButton(button2);
		toolbar.addSeparator();

		this.bottomToolbarmedio.addFill();
		this.bottomToolbarmedio.addSeparator();
		toolbar.addButton(new ToolbarButton("<< Anterior", new ButtonListenerAdapter() {
			@Override
			public void onClick(Button button, EventObject e) {
				for (Record recordHito : BudgetInfoForm.this.store.getRecords()) {
					if (recordHito.getAsString("common") == null) {
						MessageBox
								.alert("La descripcion de los hitos es obliglatoria, ingrese descripcion o elimine los hitos sin descripcion");
						return;
					}
				}
				String[] meses = new String[] { "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
						"agosto", "septiembre", "octubre", "noviembre", "diciembre" };

				BudgetInfoForm.this.beforeMilestone(BudgetInfoForm.this.store.getRecords(),
						BudgetInfoForm.this.anioSeleccion + "", meses);
				// ////////////////para encerar y limpiar el grafico////////////////////////////////
				BudgetInfoForm.this.encerarValor();
				Record[] hitos = BudgetInfoForm.this.store.getRecords();
				String[] meses2 = new String[] { "total", "enero", "febrero", "marzo", "abril", "mayo", "junio",
						"julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre" };
				BudgetInfoForm.this.limpiarGrid2(meses2, hitos, BudgetInfoForm.this.store1.getRecords());
				BudgetInfoForm.this.graphPanel.clear();
				// ///////////////////////////////////////////////
			}
		}));
		toolbar.addSeparator();
		toolbar.addButton(new ToolbarButton("Siguiente >>", new ButtonListenerAdapter() {
			@Override
			public void onClick(Button button, EventObject e) {

				String[] meses = new String[] { "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
						"agosto", "septiembre", "octubre", "noviembre", "diciembre" };
				for (Record recordHito : BudgetInfoForm.this.store.getRecords()) {
					if (recordHito.getAsString("common") == null) {
						MessageBox
								.alert("La descripcion de los hitos es obliglatoria, ingrese descripcion o elimine los hitos sin descripcion");
						return;
					}
				}
				BudgetInfoForm.this.nextMilestone(BudgetInfoForm.this.store.getRecords(),
						BudgetInfoForm.this.anioSeleccion + "", meses);
				// ////////////////para encerar y limpiar el grafico////////////////////////////////
				BudgetInfoForm.this.encerarValor();
				Record[] hitos = BudgetInfoForm.this.store.getRecords();
				String[] meses2 = new String[] { "total", "enero", "febrero", "marzo", "abril", "mayo", "junio",
						"julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre" };
				BudgetInfoForm.this.limpiarGrid2(meses2, hitos, BudgetInfoForm.this.store1.getRecords());
				BudgetInfoForm.this.graphPanel.clear();
				// ///////////////////////////////////////////////
			}
		}));

		this.grid.setStore(this.store);
		this.grid.setColumnModel(columnModel);
		// grid.setBottomToolbar(bottomToolbarmedio);
		this.grid.setWidth(850);
		this.grid.setHeight(300);
		this.grid.setAutoScroll(true);
		this.grid.setAutoExpandColumn("diciembreD");
		this.grid.setFrame(false);
		this.grid.setClicksToEdit(1);
		this.grid.setTopToolbar(toolbar);

		// ///////////////////////SEGUNDO GRID///////////////////
		Panel panelFondo = new Panel();
		panelFondo.setBorder(false);
		panelFondo.setPaddings(15);
		this.store1.load();
		this.gridPresu.setStore(this.store1);
		this.gridPresu.setTitle("TOTAL COMPROMETIDO, DEVENGADO, EJECUTADO A\u00d1O "
				+ BudgetInfoForm.this.anioSeleccion);
		// ///////////////definiendo 2 grid con propiedaes de formato left right///////////////
		ColumnConfig columTest = new ColumnConfig("", "test", 120, false, null, "test");
		columTest.setAlign(TextAlign.LEFT);
		ColumnConfig columTotal = new ColumnConfig("<b>TOTAL</b>", "total", 90, false, null, "total");
		columTotal.setAlign(TextAlign.RIGHT);
		columTotal.setRenderer(this.renderer);
		ColumnConfig columEnero = new ColumnConfig("<b>ENE</b>", "enero", 70, false, null, "enero");
		columEnero.setAlign(TextAlign.RIGHT);
		columEnero.setRenderer(this.renderer);
		ColumnConfig columFebrero = new ColumnConfig("<b>FEB</b>", "febrero", 70, false, null, "febrero");
		columFebrero.setAlign(TextAlign.RIGHT);
		columFebrero.setRenderer(this.renderer);
		ColumnConfig columMarzo = new ColumnConfig("<b>MAR</b>", "marzo", 70, false, null, "marzo");
		columMarzo.setAlign(TextAlign.RIGHT);
		columMarzo.setRenderer(this.renderer);
		ColumnConfig columAbril = new ColumnConfig("<b>ABR</b>", "abril", 70, false, null, "abril");
		columAbril.setAlign(TextAlign.RIGHT);
		columAbril.setRenderer(this.renderer);
		ColumnConfig columMayo = new ColumnConfig("<b>MAY</b>", "mayo", 70, false, null, "mayo");
		columMayo.setAlign(TextAlign.RIGHT);
		columMayo.setRenderer(this.renderer);
		ColumnConfig columJunio = new ColumnConfig("<b>JUN</b>", "junio", 70, false, null, "junio");
		columJunio.setAlign(TextAlign.RIGHT);
		columJunio.setRenderer(this.renderer);
		ColumnConfig columJulio = new ColumnConfig("<b>JUL</b>", "julio", 70, false, null, "julio");
		columJulio.setAlign(TextAlign.RIGHT);
		columJulio.setRenderer(this.renderer);
		ColumnConfig columAgosto = new ColumnConfig("<b>AGO</b>", "agosto", 70, false, null, "agosto");
		columAgosto.setAlign(TextAlign.RIGHT);
		columAgosto.setRenderer(this.renderer);
		ColumnConfig columSeptiembre = new ColumnConfig("<b>SEP</b>", "septiembre", 70, false, null, "septiembre");
		columSeptiembre.setAlign(TextAlign.RIGHT);
		columSeptiembre.setRenderer(this.renderer);
		ColumnConfig columOctubre = new ColumnConfig("<b>OCT</b>", "octubre", 70, false, null, "octubre");
		columOctubre.setAlign(TextAlign.RIGHT);
		columOctubre.setRenderer(this.renderer);
		ColumnConfig columNoviembre = new ColumnConfig("<b>NOV</b>", "noviembre", 70, false, null, "noviembre");
		columNoviembre.setAlign(TextAlign.RIGHT);
		columNoviembre.setRenderer(this.renderer);
		ColumnConfig columDiciembre = new ColumnConfig("<b>DIC</b>", "diciembre", 70, false, null, "diciembre");
		columDiciembre.setAlign(TextAlign.RIGHT);
		columDiciembre.setRenderer(this.renderer);

		ColumnConfig[] columns1 = { columTest, columTotal, columEnero, columFebrero, columMarzo, columAbril, columMayo,
				columJunio, columJulio, columAgosto, columSeptiembre, columOctubre, columNoviembre, columDiciembre };

		ColumnModel columnModel1 = new ColumnModel(columns1);
		// //////////////////////////////////////////////////////////////////////////////////////////////////////
		this.gridPresu.setColumnModel(columnModel1);
		this.gridPresu.setFrame(false);
		this.gridPresu.setStripeRows(true);
		this.gridPresu.setAutoExpandColumn("enero");
		this.gridPresu.setHeight(200);
		this.gridPresu.setWidth(850);

		Toolbar bottomToolbar1 = new Toolbar();
		this.bottomToolbarmedio.addSeparator();
		toolbar.addButton(new ToolbarButton("Calcular/Ver Grafico", new ButtonListenerAdapter() {
			@Override
			public void onClick(Button button, EventObject e) {

				Record[] hitos = BudgetInfoForm.this.store.getRecords();
				String[] meses = new String[] { "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
						"agosto", "septiembre", "octubre", "noviembre", "diciembre" };
				BudgetInfoForm.this.calcularValoresPorMes(meses, hitos, BudgetInfoForm.this.store1.getRecords());
				BudgetInfoForm.this.gridPresu.doLayout();
				BudgetInfoForm.this.gridPresu.getView().refresh();
				List<Object[]> planeados = BudgetInfoForm.this.determinarValoresPlaneadosPorMes(Arrays.asList(meses),
						hitos);
				List<Object[]> ejecutados = BudgetInfoForm.this.determinarValoresEjecutadosPorMes(Arrays.asList(meses),
						hitos);
				BudgetInfoForm.this.showBudgetInfoGraph(planeados, ejecutados);

				BudgetInfoForm.this.encerarValor();
			}
		}));
		this.gridPresu.setTopToolbar(bottomToolbar1);

		// ///////////////////////////////////////////////////////////////////////
		// Se arma el grid de totales del proyecto
		ColumnConfig descripcion = new ColumnConfig("", "descripcion", 120, false, null, "descripcion");
		ColumnConfig enero = new ColumnConfig("<B>ENE</B>", "enero", 80, false, null, "enero");
		enero.setAlign(TextAlign.RIGHT);
		enero.setRenderer(BudgetInfoForm.this.renderer);
		ColumnConfig febrero = new ColumnConfig("<B>FEB</B>", "febrero", 80, false, null, "febrero");
		febrero.setAlign(TextAlign.RIGHT);
		febrero.setRenderer(BudgetInfoForm.this.renderer);
		ColumnConfig marzo = new ColumnConfig("<B>MAR</B>", "marzo", 80, false, null, "marzo");
		marzo.setAlign(TextAlign.RIGHT);
		marzo.setRenderer(BudgetInfoForm.this.renderer);
		ColumnConfig abril = new ColumnConfig("<B>ABR</B>", "abril", 80, false, null, "abril");
		abril.setAlign(TextAlign.RIGHT);
		abril.setRenderer(BudgetInfoForm.this.renderer);
		ColumnConfig mayo = new ColumnConfig("<B>MAY</B>", "mayo", 80, false, null, "mayo");
		mayo.setAlign(TextAlign.RIGHT);
		mayo.setRenderer(BudgetInfoForm.this.renderer);
		ColumnConfig junio = new ColumnConfig("<B>JUN</B>", "junio", 80, false, null, "junio");
		junio.setAlign(TextAlign.RIGHT);
		junio.setRenderer(BudgetInfoForm.this.renderer);
		ColumnConfig julio = new ColumnConfig("<B>JUL</B>", "julio", 80, false, null, "julio");
		julio.setAlign(TextAlign.RIGHT);
		julio.setRenderer(BudgetInfoForm.this.renderer);
		ColumnConfig agosto = new ColumnConfig("<B>AGO</B>", "agosto", 80, false, null, "agosto");
		agosto.setAlign(TextAlign.RIGHT);
		agosto.setRenderer(BudgetInfoForm.this.renderer);
		ColumnConfig septiembre = new ColumnConfig("<B>SEP</B>", "septiembre", 80, false, null, "septiembre");
		septiembre.setAlign(TextAlign.RIGHT);
		septiembre.setRenderer(BudgetInfoForm.this.renderer);
		ColumnConfig octubre = new ColumnConfig("<B>OCT</B>", "octubre", 80, false, null, "octubre");
		octubre.setAlign(TextAlign.RIGHT);
		octubre.setRenderer(BudgetInfoForm.this.renderer);
		ColumnConfig noviembre = new ColumnConfig("<B>NOV</B>", "noviembre", 80, false, null, "noviembre");
		noviembre.setAlign(TextAlign.RIGHT);
		noviembre.setRenderer(BudgetInfoForm.this.renderer);
		ColumnConfig diciembre = new ColumnConfig("<B>DIC</B>", "diciembre", 80, false, null, "diciembre");
		diciembre.setAlign(TextAlign.RIGHT);
		diciembre.setRenderer(BudgetInfoForm.this.renderer);
		ColumnConfig total = new ColumnConfig("<B>TOTAL</B>", "total", 100, false, null, "total");
		total.setCss("bold");
		total.setAlign(TextAlign.RIGHT);
		total.setRenderer(BudgetInfoForm.this.renderer);

		ColumnModel columnModelTotalProyecto = new ColumnModel(new BaseColumnConfig[] { descripcion, total, enero,
				febrero, marzo, abril, mayo, junio, julio, agosto, septiembre, octubre, noviembre, diciembre });

		columnModel.setDefaultSortable(false);
		this.gridTotales.setStore(this.totalProjectStore);
		this.gridTotales.setColumnModel(columnModelTotalProyecto);

		this.gridTotales.setWidth(850);
		this.gridTotales.setHeight(150);
		this.gridTotales.setTitle("CONSOLIDADO DE TODOS LOS A\u00d1OS");
		this.gridTotales.setFrame(true);
		this.gridTotales.doLayout();
		this.actualizarDatosGridTotales(idProject);
		// ////////////////////////////////////////////
		this.backPanel.add(this.grid);
		this.backPanel.add(this.gridPresu);
		this.backPanel.add(this.gridTotales);
		this.backPanel.add(this.graphPanel);
		vp.add(this.backPanel);
		this.backPanel.doLayout();
		this.initWidget(vp);
	}

	private void limpiarGrid2(String[] meses, Record[] records, Record[] totales) {
		for (String mes : meses) {
			totales[0].set("total", "0");
			totales[1].set("total", "0");
			totales[2].set("total", "0");

			totales[0].set(mes, "0");
			totales[1].set(mes, "0");
			totales[2].set(mes, "0");
		}
	}

	/**
	 * Next year of milestone
	 */
	private void nextMilestone(Record[] recordHitos, String currentYear, String[] meses) {

		List<MilestoneVO> hitos = new ArrayList<MilestoneVO>();
		MilestoneVO milestoneVO = null;

		final ExtElement element = Ext.get(this.backPanelId);
		element.mask("Cargando..", true);

		for (Record recordHito : recordHitos) {

			milestoneVO = new MilestoneVO();
			milestoneVO.setAnio(currentYear);
			if (recordHito.getAsString("common") == null) {
				MessageBox.alert("La descripcion de los hitos es obliglatoria, o eliminelos");
				return;
			}
			milestoneVO.setDescripcion(recordHito.getAsString("common").trim());

			List<MilestoneMonthVO> monthsMilestone = new ArrayList<MilestoneMonthVO>();

			for (String mes : meses) {

				String valorPlaneado = recordHito.getAsString(mes + "P");
				if (valorPlaneado != null && valorPlaneado.trim().length() > 0) {
					valorPlaneado = valorPlaneado.trim();
				} else {
					valorPlaneado = "0";
				}

				String valorEjecutado = recordHito.getAsString(mes + "E");
				if (valorEjecutado != null && valorEjecutado.trim().length() > 0) {
					valorEjecutado = valorEjecutado.trim();
				} else {
					valorEjecutado = "0";
				}

				String valorDevengado = recordHito.getAsString(mes + "D");
				if (valorDevengado != null && valorDevengado.trim().length() > 0) {
					valorDevengado = valorDevengado.trim();
				} else {
					valorDevengado = "0";
				}
				if (!valorPlaneado.equals("0") || !valorEjecutado.equals("0") || !valorDevengado.equals("0")) {

					MilestoneMonthVO milestoneMonthVO = new MilestoneMonthVO();
					milestoneMonthVO.setAnio(currentYear);
					milestoneMonthVO.setEjecutado(valorEjecutado);
					milestoneMonthVO.setPlaneado(valorPlaneado);
					milestoneMonthVO.setDevengado(valorDevengado);
					milestoneMonthVO.setMes(mes);
					monthsMilestone.add(milestoneMonthVO);
				}
			}
			milestoneVO.setMonthsMileStoneVO(monthsMilestone);
			hitos.add(milestoneVO);
		}

		this.store.commitChanges();
		this.store.removeAll();
		this.store.commitChanges();

		AsyncCallback<String> nextAsync = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				element.unmask();
				MessageBox.alert("No se pudo obtener los datos actuales ");
				GWT.log("mensaje", caught);
			}

			public void onSuccess(String result) {
				element.unmask();
				BudgetInfoForm.this.anioSeleccion = BudgetInfoForm.this.anioSeleccion + 1;
				BudgetInfoForm.this.panel.clear();
				Label anio = new Label("A?? FISCAL " + BudgetInfoForm.this.anioSeleccion);
				BudgetInfoForm.this.backPanel.setTitle("INFORMACI&#211N PRESEPUESTARIA" + anio);
				BudgetInfoForm.this.gridPresu.setTitle("TOTAL COMPROMETIDO, DEVENGADO, EJECUTADO A\u00d1O "
						+ BudgetInfoForm.this.anioSeleccion);
				BudgetInfoForm.this.panel.add(anio);
				BudgetInfoForm.this.panel.doLayout();

				BudgetInfoForm.this.store.loadJsonData(result, false);
				BudgetInfoForm.this.store.commitChanges();
				BudgetInfoForm.this.grid.doLayout();
				BudgetInfoForm.this.grid.getView().refresh();

				BudgetInfoForm.this.gridPresu.doLayout();
				BudgetInfoForm.this.gridPresu.getView().refresh();

			}
		};
		this.serviceWebAdministration.nextMilestone(currentYear, hitos, "hitos", nextAsync);
	}

	protected void showBudgetInfoGraph(final List<Object[]> planeados, List<Object[]> ejecutados) {

		Double scale = Double.valueOf(0);
		for (Object[] o : planeados) {
			Double value = (Double) o[3];
			if (value.doubleValue() > scale.doubleValue()) {
				scale = value;
			}
		}

		for (Object[] o : ejecutados) {
			Double value = (Double) o[3];
			if (value.doubleValue() > scale.doubleValue()) {
				scale = value;
			}
		}

		if (scale.intValue() == 0) {
			return;
		}
		Double steps = Double.valueOf(scale.doubleValue() / 10);
		Panel graph = LineGraphic.drawComparitionBudgetLine(planeados, ejecutados, "Informaci\u00f3n Presupuestaria",
				"Comprometido", "#00ff00", "Ejecutado", "#ff0000", 12, scale, steps);
		this.graphPanel.clear();
		this.graphPanel.add(graph);
		this.backPanel.doLayout();
	}
}

