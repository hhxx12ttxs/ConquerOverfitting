package org.itx.jbalance.equeue.gwt.client.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.itx.jbalance.equeue.gwt.client.ActionCellWithTooltip;
import org.itx.jbalance.equeue.gwt.client.ContentWidget;
import org.itx.jbalance.equeue.gwt.client.DateUtils;
import org.itx.jbalance.equeue.gwt.client.EQueueNavigation;
import org.itx.jbalance.equeue.gwt.client.EnumRenderer;
import org.itx.jbalance.equeue.gwt.client.request.ChooseDouPanel.SelectDouListener;
import org.itx.jbalance.equeue.gwt.client.services.DouRequestsSearchResultsWrapper;
import org.itx.jbalance.equeue.gwt.client.services.SerializerService;
import org.itx.jbalance.equeue.gwt.client.services.SerializerServiceAsync;
import org.itx.jbalance.l0.h.HRegister;
import org.itx.jbalance.l0.h.RecordColor;
import org.itx.jbalance.l0.o.Contractor;
import org.itx.jbalance.l0.o.Dou;
import org.itx.jbalance.l0.o.Region;
import org.itx.jbalance.l0.s.SDouRequest;
import org.itx.jbalance.l0.s.SRegister;
import org.itx.jbalance.l1.api.DouRequestsSearchParams;
import org.itx.jbalance.l1.api.SortedColumnInfo;
import org.itx.jbalance.l2_api.dto.equeue.DouRequestDTO;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DisclosurePanelImages;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.HasDirectionalHtml;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

public class DouRequestListWidget extends ContentWidget implements DouRequestListPresenter.Display {

	interface Binder extends UiBinder<Widget, DouRequestListWidget> {

	}

	 class RecordColorWrapper{
		 boolean withoutColor = false;
		 RecordColor recordColor;
	 }
//	public static DateTimeFormat dateFormat=DateTimeFormat.getFormat("dd.MM.yy");
	
	@UiField(provided = false)
	CaptionPanel requestDetailsFieldset;

	
	@UiField(provided = true)
	CellTable<DouRequestDTO> requestsTable = new CellTable<DouRequestDTO>(DouRequestListPresenter.PAGE_SIZE, MyCellTableResources.INSTANCE);

	
	
	@UiField(provided = true)
	CellTable<SRegister>	sRegTable=new CellTable<SRegister>(DouRequestListPresenter.PAGE_SIZE,MyCellTableResources.INSTANCE);
//
//	@UiField(provided = false)
//	SimplePanel listTabPanel;
//	
//	@UiField(provided = false)
//	SimplePanel editTabPanel;
	
	@UiField(provided = false)
	TextBox searchField;
	
	
	DisclosurePanelHeader douSearchHeaderWidget;
//	@UiField(provided = false)
//	Button searchButton;
//	@UiField(provided = false)
//	Button cleanButton;
	
//	@UiField(provided = false)
//	TextBox ageFromField;
	
	@UiField(provided = true)
	CellList<SDouRequest> douList=new CellList<SDouRequest>(new SDouRequestCell());
	
	
	@UiField(provided=false)
	TextBox ageFrom;
	
	@UiField(provided=false)
	TextBox ageTo;
	
	@UiField(provided=false)
	DateBox regDateFrom;
	
	@UiField(provided=false)
	DateBox regDateTo;
	
	@UiField(provided=false)
	DateBox sverkDateFrom;
	
	@UiField(provided=false)
	DateBox sverkDateTo;
	
	@UiField(provided = false)
	HTML searchResLabel;
//	EnumRenderer<PrivilegesSearchItems> requesterTypeRenderer = new EnumRenderer<PrivilegesSearchItems>();
	
	@UiField(provided = false)
	HTML commentsHtml;
	
	
	@UiField(provided = false)
	HTML birthdaySertificate;
	
//	@UiField(provided = false)
//	HTML parrents;
	
	
	@UiField(provided = false)
	HTML address;
	
	AsyncDataProvider<DouRequestDTO> provider;

	ChooseDouPanel chooseDouPanel;
	
	
	Renderer<Integer> intRenderer=  new AbstractRenderer<Integer>() {
    	@Override
		public String render(Integer object) {
    		if(object==null)
    			return "--";
			return object+"";
		}
	};
	
	
	@UiField(provided=false)
	DisclosurePanel searchDouPanel;
	
	
	final DisclosurePanelImages images = (DisclosurePanelImages)GWT.create(DisclosurePanelImages.class);

	class DisclosurePanelHeader extends HorizontalPanel {
	HTML html = new HTML();
	
	public DisclosurePanelHeader(boolean isOpen, String html){
	add(isOpen ? images.disclosurePanelOpen().createImage()
          : images.disclosurePanelClosed().createImage());
    this.html .setHTML(html);
    add(this.html);
}

	public HTML getHtml() {
		return html;
	}
}


	
	@UiField(provided = false)
	DateBox bithdayFrom;
	
	@UiField(provided = false)
	DateBox bithdayTo;
	
	 
	

	
	@UiField(provided = true)
	ValueListBox<Integer> plainYearListBox;
	
	@UiField(provided = true)
	ValueListBox<Boolean> privilegeListBox;

	@UiField(provided = true)
	ValueListBox<Boolean> phoneListBox;
	
	
	@UiField(provided = true)
	ValueListBox<DouRequestsSearchParams.OrderStatus> permitListBox = new ValueListBox<DouRequestsSearchParams.OrderStatus>(new EnumRenderer<DouRequestsSearchParams.OrderStatus>());
	{
		ArrayList <DouRequestsSearchParams.OrderStatus>vals = new ArrayList<DouRequestsSearchParams.OrderStatus>(4);
		vals.add(DouRequestsSearchParams.OrderStatus.ALL);
		vals.add(DouRequestsSearchParams.OrderStatus.ACTIVE_QUEUE);
		vals.add(DouRequestsSearchParams.OrderStatus.OUT_OF_QUEUE);
		vals.add(DouRequestsSearchParams.OrderStatus.REJECT);
		permitListBox.setValue(DouRequestsSearchParams.OrderStatus.ACTIVE_QUEUE);
		permitListBox.setAcceptableValues(vals);
//		permitListBox.set
		
	}
	
	
//	@UiField(provided = true)
//	ValueListBox<Dou> searchDouListBox= new ValueListBox<Dou>(new DouRenderer());
	
	@UiField(provided = true)
	ValueListBox<RecordColorWrapper> recordColorListBox;
	
	
	@UiField(provided = true)
	SimplePager simplePager=new SimplePager();
	@UiField(provided = false)
	Button addButton;
	
	@UiField(provided = false)
	Button generateRegisterButton;
	
	
	@UiField(provided = false)
	Button cancelButton;
	
//	@UiField(provided = false)
//	Button goToSelectModeButton;

	@UiField(provided = false)
	Button printListButton;
	
	
	
	public DouRequestListWidget() {
		super();
	}
	ProvidesKey<DouRequestDTO> keyProvider;
	@Override
	public void initialize() {
		plainYearListBox=new ValueListBox<Integer>(intRenderer);
		Date date = new Date();
		Collection<Integer> years=new ArrayList<Integer>();
		int currentYear = date.getYear();
		for(int y=currentYear+1900;y<currentYear+1900+10;y++){
			years.add(y);
		}
		plainYearListBox.setAcceptableValues(years);
		plainYearListBox.setValue(null);
		
		privilegeListBox = new ValueListBox<Boolean>(new BooleanRenderer("???","?????? ????","????? ???"));
			{
				ArrayList <Boolean>vals = new ArrayList<Boolean>(3);
				vals.add(null);
				vals.add(true);
				vals.add(false);
				privilegeListBox.setAcceptableValues(vals);
				
			}
			
			phoneListBox = new ValueListBox<Boolean>(new BooleanRenderer("???","C ?????????","??? ?????????"));
			{
				ArrayList <Boolean>vals = new ArrayList<Boolean>(3);
				vals.add(null);
				vals.add(true);
				vals.add(false);
				phoneListBox.setAcceptableValues(vals);
				
			}	
			
			final RecordColor colors[] = RecordColor.values();
			final RecordColorWrapper values [] = new  RecordColorWrapper[colors.length+1];
			
			RecordColorWrapper withoutColor = new RecordColorWrapper();
			values[0]=withoutColor;
			withoutColor.withoutColor=true;
			
			for (int i=0;i<colors.length;i++) {
				RecordColorWrapper recordColorWrapper = new RecordColorWrapper();
				recordColorWrapper.recordColor=colors[i];
				values[i+1]=recordColorWrapper;
			}
			
			
		recordColorListBox	= new ValueListBox<RecordColorWrapper>(new RecordColorRenderer());
		recordColorListBox.setAcceptableValues(Arrays.asList(values));
		recordColorListBox.addValueChangeHandler(new ValueChangeHandler<RecordColorWrapper>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<RecordColorWrapper> event) {
				for(RecordColor rc:colors)
					recordColorListBox.removeStyleName(rc.name());
				RecordColorWrapper value = event.getValue();
				if(value!=null && !value.withoutColor && value.recordColor!=null)
					recordColorListBox.setStyleName(value.recordColor.name());
				
				
			}
		});
		
		Binder uiBinder = GWT.create(Binder.class);
		initWidget(uiBinder.createAndBindUi(this));
		initTableColumns();
		initTableStyles();

		initRegTableColumns();
		sRegTable.setRowCount(0);
		
		keyProvider=new ProvidesKey<DouRequestDTO>() {

			@Override
			public Object getKey(DouRequestDTO item) {
				return item.getUId();
			}
		};
		final SingleSelectionModel<DouRequestDTO> selectionModel=new SingleSelectionModel<DouRequestDTO>(keyProvider);
		
		requestsTable.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
//				requestDetailsFieldset.setAttribute("visible", "true");
				DouRequestDTO selectedRequest = selectionModel.getSelectedObject();
				if(selectedRequest != null){
					getPresenter().populateDetails(selectedRequest);
					commentsHtml.setHTML(selectedRequest.getComments());
					requestDetailsFieldset.setCaptionText("?????? ?????? "+selectedRequest.getRegNumber());
					birthdaySertificate.setHTML(selectedRequest.getBirthdaySertificateSeria()+" "+ selectedRequest.getBirthdaySertificateNumber());
	//				parrents.setHTML(selectedRequest.getBirthSertificate().getGuardian1()+" <br/> "+ selectedRequest.getBirthSertificate().getGuardian2());
					requestDetailsFieldset.getElement(). getStyle().setProperty("visibility", "");
					address.setText(selectedRequest.getAddress());	
				}else{
					requestDetailsFieldset.getElement(). getStyle().setProperty("visibility", "hidden;");
				}
			}
		});
		
		requestsTable.setPageSize(DouRequestListPresenter.PAGE_SIZE);
		simplePager.setDisplay(requestsTable);

	    // Add a ColumnSortEvent.AsyncHandler to connect sorting to the
	    // AsyncDataPRrovider.
	    AsyncHandler columnSortHandler = new AsyncHandler(requestsTable);
	    requestsTable.addColumnSortHandler(columnSortHandler);

		
		requestDetailsFieldset.getElement(). getStyle().setProperty("visibility", "hidden");
		
		if(State.SELECT_REQUESTS == getPresenter().getState()){
			addButton.getElement(). getStyle().setProperty("visibility", "hidden");
			addButton.getElement(). getStyle().setProperty("width", "0px");
			
			printListButton.getElement(). getStyle().setProperty("visibility", "hidden");
			printListButton.getElement(). getStyle().setProperty("width", "0px");
//			goToSelectModeButton.getElement(). getStyle().setProperty("visibility", "hidden");
		}else{
//			cancelButton.getElement(). getStyle().setProperty("visibility", "hidden");
			generateRegisterButton.getElement(). getStyle().setProperty("visibility", "hidden");
			generateRegisterButton.getElement(). getStyle().setProperty("width", "0px");
			
			cancelButton.getElement(). getStyle().setProperty("visibility", "hidden");
			cancelButton.getElement(). getStyle().setProperty("width", "0px");
		}
	
		
		
//		/* init Date Filter*/
//		years=new ArrayList<Integer>();
//		for(int y=currentYear+1900;y>currentYear+1900-10;y--){
//			years.add(y);
//		}
//		yearFrom.setAcceptableValues(years);
//		yearFrom.setValue(null);
//		
//		yearTo.setAcceptableValues(years);
//		yearTo.setValue(null);
		
		
		DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("dd.MM.yy");
		 DateBox.Format format=new DateBox.DefaultFormat(dateTimeFormat); 
		 bithdayFrom.setFormat(format); 
		 bithdayTo.setFormat(format); 
		 regDateFrom.setFormat(format); 
		 regDateTo.setFormat(format); 
		 sverkDateFrom.setFormat(format); 
		 sverkDateTo.setFormat(format); 

		 douSearchHeaderWidget = new DisclosurePanelHeader(false,"?????");
		 
		 searchDouPanel.setHeader(douSearchHeaderWidget);
		 searchDouPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
		 searchDouPanel.getElement().getStyle().setBackgroundColor("white");
		 
		 /**
		  * #121
		  * ????? ??? ??????? Enter ? ???? ?????
		  */
		 KeyPressHandler searchHandler = new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					getPresenter().search();
			    }
			}
		};
		searchField.addKeyPressHandler(searchHandler);
		bithdayFrom.getTextBox().addKeyPressHandler(searchHandler);
		bithdayTo.getTextBox().addKeyPressHandler(searchHandler);
		sverkDateFrom.getTextBox().addKeyPressHandler(searchHandler);
		sverkDateTo.getTextBox().addKeyPressHandler(searchHandler);
		regDateFrom.getTextBox().addKeyPressHandler(searchHandler);
		regDateTo.getTextBox().addKeyPressHandler(searchHandler);
		ageFrom.addKeyPressHandler(searchHandler);
		ageTo.addKeyPressHandler(searchHandler);
		
	}

	
	private void initRegTableColumns() {
		
		
		/*********     number     ***********/
		Column<SRegister, String> idColumn = new Column<SRegister, String>(
				new TextCell()) {
			@Override
			public String getValue(SRegister object) {
				return object.getSeqNumber()+"";
			}
		};

		sRegTable.addColumn(idColumn, SafeHtmlUtils.fromSafeConstant("#"));
		
		
		
		/*********     ? ?????????     ***********/
		Column<SRegister, String> registerNumberColumn = new Column<SRegister, String>(
				new TextCell()) {
			@Override
			public String getValue(SRegister object) {
				if(object.getDou()!=null)
					return object.getHUId().getDnumber()+"";
				else
					return "";
			}
		};
		
		sRegTable.addColumn(registerNumberColumn, SafeHtmlUtils.fromSafeConstant("? ?????????"));
		

		
		/*********     ???? ?????????     ***********/
		Column<SRegister, String> registerDateColumn = new Column<SRegister, String>(
				new TextCell()) {
			@Override
			public String getValue(SRegister object) {
				if(object.getDou()!=null)
					return DateUtils.format( ((HRegister)object.getHUId()).getRegisterDate());
				else
					return "";
			}
		};
		
		sRegTable.addColumn(registerDateColumn, SafeHtmlUtils.fromSafeConstant("???? ?????????"));
		

		
		/*********     ????     ***********/
		Column<SRegister, String> douColumn = new Column<SRegister, String>(
				new TextCell()) {
			@Override
			public String getValue(SRegister object) {
				if(object.getDou()!=null)
					return object.getDou().getIdDou()+"  ("+object.getDou().getName()+")";
				else
					return "";
			}
		};
		
		sRegTable.addColumn(douColumn, SafeHtmlUtils.fromSafeConstant("????"));

		
		/*********     ????????     ***********/
		Column<SRegister, String> actionColumn = new Column<SRegister, String>(
				new TextCell()) {
			@Override
			public String getValue(SRegister object) {
				return object.getRnAction().getDescription();
			}
		};

		sRegTable.addColumn(actionColumn , SafeHtmlUtils.fromSafeConstant("????????"));
	
	}


	private void initTableStyles() {
		requestsTable.setRowStyles(new RowStyles<DouRequestDTO>() {
			
			@Override
			public String getStyleNames(DouRequestDTO row, int rowIndex) {
				if(row!=null && row.getRecordColor()!=null)
					return row.getRecordColor().name();
				return null;
			}
		});
		
	}


	@UiHandler("searchButton")
	public void runSearch(ClickEvent event){
		getPresenter().search();
	}

	@UiHandler("cleanButton")
	public void cleanSearch(ClickEvent event){
		getPrivilege().setValue(null);

		getPlainYear().setValue(null);
		getAgeFrom().setText("");
		getAgeTo().setText("");
		getRegDateFrom().setValue(null);
		getRegDateTo().setValue(null);
		getSverkDateFrom().setValue(null);
		getSverkDateTo().setValue(null);
		getSearchField().setText("");
		getBirthdayFromDateBox().setValue(null);
		getBirthdayToDateBox().setValue(null);
		getRecordColor().setValue(null);
//		fix styles
		for(RecordColor rc:RecordColor.values())
			recordColorListBox.removeStyleName(rc.name());
		getPhone().setValue(null);
		getPresenter().getSearchParams().firstname=null;
		getPresenter().getSearchParams().lastname=null;
		getPresenter().getSearchParams().middlename=null;
		
		chooseDouPanel.clearSelection();
		
		updateDouSearchPanelHeader();
		
		getPresenter().search();
	}
	
	@UiHandler("printListButton")
	public void print(ClickEvent event){
		SerializerServiceAsync serializerServiceAsync= GWT.create(SerializerService.class);
		serializerServiceAsync.serializeToBase64(getPresenter().getSearchParams(), new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				String urlStr = "equeue/report?type=requestsList";
				urlStr+="&searchParams="+result;
				 Window.open(urlStr, "blank_", null);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				showExceptionNotification(caught);
			}
		});
		

	}
	
	
	@UiHandler("addButton")
	public void clickNew(ClickEvent event){
		EQueueNavigation.instance().goToDouRequestEditPage(null);
	}
	
//	@UiHandler("goToSelectModeButton")
//	public void goToSelectMode(ClickEvent event){
//		getShell().ACTION_SELECT_RN.execute();
//	}
	
	@UiHandler("cancelButton")
	public void goToListMode(ClickEvent event){
		HashMap<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("registerEditPresenter", getPresenter().getRegisterPresenter());
		params.put("regNums", (Serializable) getPresenter().getPrewSelectedNums());
		
		EQueueNavigation.instance().goToRegisterEditPage(params);
	}
	
	@UiHandler("generateRegisterButton")
	public void generateRegister(ClickEvent event){
		HashMap<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("registerEditPresenter", getPresenter().getRegisterPresenter());
		params.put("regNums", (Serializable) getPresenter().getSelectedNums());
		
		EQueueNavigation.instance().goToRegisterEditPage(params);
	}
//	

	
	/**
	 * ?????? ????? ??? ???????
	 */
	public void setDouList(Map<Region, List<Dou>> dous){
		searchDouPanel.clear();
//		cb2douid.clear();
//		douid2douNumber.clear();
		SelectDouListener listener = new SelectDouListener() {
			@Override
			public void selectionChanged(List<Dou> selected) {
				updateDouSearchPanelHeader();
				
			}
		};
		chooseDouPanel = new ChooseDouPanel(dous, listener);
		searchDouPanel.add(chooseDouPanel);
	}
	
	private void updateDouSearchPanelHeader(){
		String title="";
		List<Dou> selectedDou = chooseDouPanel.getSelectedDou();
		if(selectedDou==null || selectedDou.isEmpty())
			title = "?????";
		else{
			for (int i=0;i<  selectedDou.size() ; i++) {
				Dou dou = selectedDou.get(i);
				title += dou.getIdDou() ;
				if(i!=selectedDou.size()-1)
					title += ", ";
			}
		}
		douSearchHeaderWidget.html.setHTML(title);
	}
	
	
	DouRequestListPresenter presenter;

	public DouRequestListPresenter getPresenter() {
		return presenter;
	}


	public void setPresenter(DouRequestListPresenter presenter) {
		this.presenter=presenter;
	}

	
	public void updateRowData(int start,List<DouRequestDTO>data){
		provider.updateRowData(start, data);
	}
	int start=0;
//	@Override
	public void showDouRequests(DouRequestsSearchResultsWrapper searchRes) {
		
		if(provider==null){
		provider = new AsyncDataProvider<DouRequestDTO>(keyProvider) {
		      @Override
		      protected void onRangeChanged(HasData<DouRequestDTO> display) {
		        start = display.getVisibleRange().getStart();
		        int end = start + display.getVisibleRange().getLength();
		        ColumnSortList columnSortList = requestsTable.getColumnSortList();
		        
		        List<SortedColumnInfo>sortedColumns=new LinkedList<SortedColumnInfo>();
//		        for(int i=0;i<columnSortList.size();i++){
		        if(columnSortList.size()>0)	
		        	sortedColumns.add(new SortedColumnInfo("rn", columnSortList.get(0).isAscending()));
		        else
		        	sortedColumns.add(new SortedColumnInfo("rn", false));
//		        	 columnSortList.get(i).getColumn().get
//		        }
		       
		        getPresenter().loadData(start,end,sortedColumns);
		        
		      }
		    };
		    provider.addDataDisplay(requestsTable);
		}else{
		
		}
		    
		 provider.updateRowCount(searchRes.getSearchCount(), true);
		 provider.updateRowData(0, searchRes.getList());
		 
//		requestsTable.setRowData(searchRes.getList());
		searchResLabel.setHTML("??????? ?? ??????? <b>"+searchRes.getSearchCount() +"</b>.  ???????? ??????? <b>"+searchRes.getActiveCount() +"</b>.   ????? ??????? ? ??????? <b>"+searchRes.getAllCount()+"</b>.");
		
	}

	public HasText getSearchField() {
		return searchField;
	}

	public void setSearchField(TextBox searchField) {
		this.searchField = searchField;
	}
	
	public HasConstrainedValue<Boolean>getPrivilege(){
		return privilegeListBox;
	}
	
	
	public HasConstrainedValue<DouRequestsSearchParams.OrderStatus>getPermit(){
		return permitListBox;
	}

	
	static class SDouRequestCell extends AbstractCell<SDouRequest> {


	    public SDouRequestCell() {
	    }

	    @Override
	    public void render(Context context, SDouRequest value, SafeHtmlBuilder sb) {
	      if (value == null||  value.getArticleUnit()==null) {
	        return;
	      }

	      Contractor dou = value.getArticleUnit();
	      sb.appendHtmlConstant(dou.getName());
	    }
	  }


	public void populateDousList(List<SDouRequest> result) {
		douList.setRowData(result);
	}
	
	
	
	
//	public HasConstrainedValue<Dou> getSearchedDou(){
//		return searchDouListBox;
//	}

	
	private void initTableColumns() {
//		ColumnSortList columnSortList = requestsTable.getColumnSortList();
		
		
		/* Dynamic number */
		Column<DouRequestDTO, String> dynNumColumn = new Column<DouRequestDTO, String>(
				new TextCell()) {
			@Override
			public String getValue(DouRequestDTO object) {
				
				 return (requestsTable.getVisibleItems().indexOf(object)+1+start)+"";
			}
		};

		requestsTable.addColumn(dynNumColumn, SafeHtmlUtils.fromSafeConstant("#"));
		requestsTable.setColumnWidth(dynNumColumn,"10px");
		
		
		
		
		if(State.SELECT_REQUESTS == getPresenter().getState()){
			/* select request */
			Column<DouRequestDTO, Boolean> checkColumn = new Column<DouRequestDTO, Boolean>(
					new CheckboxCell()) {
				@Override
				public Boolean getValue(DouRequestDTO object) {
					return getPresenter().isSelected(object.getUId());
				}
			};
			checkColumn.setFieldUpdater(new FieldUpdater<DouRequestDTO, Boolean>() {
				@Override
				public void update(int index, DouRequestDTO object, Boolean value) {
					if(value){
						getPresenter().select(object.getUId());
					}else{
						getPresenter().deselect(object.getUId());
					}
					
				}
			});
			
			requestsTable.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("???????? ? ????????"));
		}
		
		
		/* reg num */
		Column<DouRequestDTO, String> idColumn = new Column<DouRequestDTO, String>(
				new TextCell()) {
			@Override
			public String getValue(DouRequestDTO object) {
				return object.getRegNumber();
			}
		};

		idColumn.setSortable(true);
		requestsTable.addColumn(idColumn, SafeHtmlUtils.fromSafeConstant("???. ?????"));

		/* iss64:  init sorting */
		if(getPresenter().getState() == State.SELECT_REQUESTS){
			ColumnSortInfo sortedColumnInfo = new ColumnSortInfo(idColumn,true);
			requestsTable.getColumnSortList().push(sortedColumnInfo);
		}
		
		/* old reg num */
		Column<DouRequestDTO, String> oldRegNumColumn = new Column<DouRequestDTO, String>(
				new TextCell()) {
			@Override
			public String getValue(DouRequestDTO object) {
				return object.getOldNumber();
			}
		};

		requestsTable.addColumn(oldRegNumColumn, SafeHtmlUtils.fromSafeConstant("?????? ??"));
		
		
		/* reg date */
		Column<DouRequestDTO, String> regDateColumn = new Column<DouRequestDTO, String>(
				new TextCell()) {
			@Override
			public String getValue(DouRequestDTO object) {
				return DateUtils.format(object.getRegDate());
			}
		};

		requestsTable.addColumn(regDateColumn, SafeHtmlUtils.fromSafeConstant("???? ???."));
		requestsTable.setColumnWidth(regDateColumn, "130px");

		
		
		
		
		/* Sverk date */
		Column<DouRequestDTO, String> sverkDateColumn = new Column<DouRequestDTO, String>(
				new TextCell()) {
			@Override
			public String getValue(DouRequestDTO object) {
				return DateUtils.format(object.getSverkDate());
			}
		};

		requestsTable.addColumn(sverkDateColumn, SafeHtmlUtils.fromSafeConstant("???? ??????"));
		requestsTable.setColumnWidth(sverkDateColumn, "130px");

				
		
		/* name */
	/*	Column<DouRequestDTO, String> nameColumn = new Column<DouRequestDTO, String>(
//				new EditTextCell()) {
				new TextCell()) {
			@Override
			public String getValue(DouRequestDTO object) {
				if(object.getChild()!=null){
					StringBuffer res=new StringBuffer(); 
					res.append(object.getChild().getSurname());
					res.append(" ");
					res.append(object.getChild().getName());
					res.append(" ");
					res.append(object.getChild().getPatronymic());
					return  res.toString();
				}else
					return null;
			}
		};

		requestsTable.addColumn(nameColumn,
				SafeHtmlUtils.fromSafeConstant("???"));
		
		*/
		
		
		
		
		
		
		
		
	Column<DouRequestDTO, String> nameColumn = new Column<DouRequestDTO, String>(
		new TextCell()) {
	@Override
	public String getValue(DouRequestDTO object) {
		return object.getSurname();
	}
};

requestsTable.addColumn(nameColumn,
		SafeHtmlUtils.fromSafeConstant("???????"));
requestsTable.setColumnWidth(nameColumn, "150px");
//requestsTable.

Column<DouRequestDTO, String> name2Column = new Column<DouRequestDTO, String>(
		new TextCell()) {
	@Override
	public String getValue(DouRequestDTO object) {
		return object.getRealName();
	}
};

requestsTable.addColumn(name2Column,
		SafeHtmlUtils.fromSafeConstant("???"));
requestsTable.setColumnWidth(name2Column, "50px");




Column<DouRequestDTO, String> name3Column = new Column<DouRequestDTO, String>(
		new TextCell()) {
	@Override
	public String getValue(DouRequestDTO object) {
		return object.getPatronymic();
	}
};

requestsTable.addColumn(name3Column,
		SafeHtmlUtils.fromSafeConstant("????????"));

		


/* dous */
Column<DouRequestDTO, String> dous = new Column<DouRequestDTO, String>(
		new TextCell()) {
	@Override
	public String getValue(DouRequestDTO object) {
		return object.getDous();
	}
};

requestsTable.addColumn(dous,
		SafeHtmlUtils.fromSafeConstant("????"));
		/* age */
		Column<DouRequestDTO, String> ageColumn = new Column<DouRequestDTO, String>(
				new TextCell()) {

			@Override
			public String getValue(DouRequestDTO object) {
				if(object.getAge()!=null){
					 return  object.getAge()+""; 
				}else
					return null;
			}
		};

		requestsTable.addColumn(ageColumn,
				SafeHtmlUtils.fromSafeConstant("????."));
		
		
		
		
		/* birthday */
		
		Column<DouRequestDTO, String> birthdayColumn = new Column<DouRequestDTO, String>(new TextCell()){

			@Override
			public String getValue(DouRequestDTO object) {
				return object.getBirthdayFormated();
			}};	
		requestsTable.addColumn(birthdayColumn,
				SafeHtmlUtils.fromSafeConstant("???? ????."));
		
		
		
//		/* birth sertificate number */
//		Column<DouRequestDTO, String> birthSertNumColumn = new Column<DouRequestDTO, String>(
////				new EditTextCell()) {
//				new TextCell()) {
//			@Override
//			public String getValue(DouRequestDTO object) {
//				if(object.getChild()!=null && object.getBirthSertificate()!=null){
//					return object.getBirthSertificate().getSeria()+" "+ object.getBirthSertificate().getNumber();
//				}else
//					return null;
//			}
//		};
//
//		requestsTable.addColumn(birthSertNumColumn,
//				SafeHtmlUtils.fromSafeConstant("? ?????????????"));
		
		
		/* phone number */
		Column<DouRequestDTO, String> phoneNumColumn = new Column<DouRequestDTO, String>(
				new TextCell()) {
			@Override
			public String getValue(DouRequestDTO object) {
				return object.getPhone();
			}
		};

		requestsTable.addColumn(phoneNumColumn,
				SafeHtmlUtils.fromSafeConstant("???."));
		
		/* privilege */
		Column<DouRequestDTO, String> privilegeColumn = new Column<DouRequestDTO, String>(
//				new EditTextCell()) {
				new TextCell()) {
			
			
			@Override
			public String getValue(DouRequestDTO object) {
				if(object.getPrivilege()!=null){
					return object.getPrivilege().getName();
				}else
					return null;
			}
		};
		
		requestsTable.addColumn(privilegeColumn,
				SafeHtmlUtils.fromSafeConstant("??????"));
		
		
		
		/* privilege */
		Column<DouRequestDTO, String> yearColumn = new Column<DouRequestDTO, String>(
//				new EditTextCell()) {
				new TextCell()) {
			
			
			@Override
			public String getValue(DouRequestDTO object) {
				if(object.getYear()!=null){
					return object.getYear()+"";
				}else
					return null;
			}
		};
		
		requestsTable.addColumn(yearColumn,
				SafeHtmlUtils.fromSafeConstant("??? ??????."));
		
		
//		/*********     ??????     ***********/
//		Column<SRegister, String> statusColumn = new Column<SRegister, String>(
//				new TextCell()) {
//			@Override
//			public String getValue(SRegister object) {
//				return object.getDouRequest().getStatus().getDescription();
//			}
//		};
//
//		requestsTable.addColumn(statusColumn , SafeHtmlUtils.fromSafeConstant("??????"));
		
//		nameColumn.setFieldUpdater(new FieldUpdater<Privilege, String>() {
//
//			@Override
//			public void update(int index, Privilege object, String value) {
//				object.setName(value);
//				getPresenter().update(object);
//
//			}
//
//		});
//
//		/* description */
//		Column<Privilege, String> descriptionColumn = new Column<Privilege, String>(
//				new EditTextCell()) {
//			@Override
//			public String getValue(Privilege object) {
//				return object.getDescription() + "";
//			}
//		};
//
//		requestsTable.addColumn(descriptionColumn,
//				SafeHtmlUtils.fromSafeConstant("????????"));
//		descriptionColumn
//				.setFieldUpdater(new FieldUpdater<Privilege, String>() {
//
//					@Override
//					public void update(int index, Privilege object, String value) {
//						object.setDescription(value);
//
//					}
//
//				});
//	
//	
//		
		
		if(State.VIEW_REQUESTS == getPresenter().getState()){ 
			/* actions */
			List<HasCell<DouRequestDTO, ?>>  hasCells = new ArrayList<HasCell<DouRequestDTO, ?>>();
			
			
			/* edit */ 
			ActionCell<DouRequestDTO> editCell = new ActionCellWithTooltip<DouRequestDTO>(
					SafeHtmlUtils.fromSafeConstant("<img src='images/edit.png' alt='?????????????' />"),new Delegate<DouRequestDTO>() {
			
				@Override
				public void execute(DouRequestDTO p) {
					HashMap<String, Serializable> params = new HashMap<String, Serializable>();
					params.put("objectUId", p.getUId());
					EQueueNavigation.instance().goToDouRequestEditPage(params);
				}
			},"????????????? ??????");
			
			
			Column <DouRequestDTO,DouRequestDTO>editColumn =new Column<DouRequestDTO,DouRequestDTO>(
					editCell){
					@Override
					public DouRequestDTO getValue(DouRequestDTO object) {
						return object;
					}}; 
			hasCells.add(editColumn);		
			
			/* delete */ 
			Delegate<DouRequestDTO> delDelegate = new Delegate<DouRequestDTO>() {

				@Override
				public void execute(DouRequestDTO p) {
					if(Window.confirm("??????? ?????????????")){
							getPresenter().delete(p.getUId());
					}
				}
			};
			ActionCell<DouRequestDTO> delCell = new ActionCellWithTooltip<DouRequestDTO>(
					SafeHtmlUtils.fromSafeConstant("<img src='images/delete.png' alt='???????' title='??????? ?????? ??? ??????????? ??????????????' />"),delDelegate,
					"??????? ?????? ??? ??????????? ??????????????.");
		
			
			Column <DouRequestDTO,DouRequestDTO>delColumn =new Column<DouRequestDTO,DouRequestDTO>(
				delCell){
					@Override
					public DouRequestDTO getValue(DouRequestDTO object) {
						return object;
					}
			}; 

			hasCells.add(delColumn);		
					
					
			
//			/* permit */ 
//			ActionCell<DouRequestDTO> exportCell
//			= new ActionCell<DouRequestDTO>(SafeHtmlUtils.fromSafeConstant("<img src='images/export.png' alt='???????'/>"),new Delegate<DouRequestDTO>() {
//				@Override
//				public void execute(DouRequestDTO p) {
//					Window.al1ert("???????? ? ????????? ??????");
//				}
//			});
//			
//			
//			Column <DouRequestDTO,DouRequestDTO>exportColumn =new Column<DouRequestDTO,DouRequestDTO>(
//					exportCell){
//					@Override
//					public DouRequestDTO getValue(DouRequestDTO object) {
//						return object;
//					}}; 
//					
//			hasCells.add(exportColumn);
			
			/*  */ 
			ActionCell<DouRequestDTO> printCell = new ActionCellWithTooltip<DouRequestDTO>(
					SafeHtmlUtils.fromSafeConstant("<img src='images/export.png' alt='???????'/>"),new Delegate<DouRequestDTO>() {
				@Override
				public void execute(DouRequestDTO p) {
					String urlStr = "equeue/report?type=registrationNotification";
					urlStr+="&hDouRequestUidParam="+p.getUId().toString();
					Window.open(urlStr, "blank_", null);
				}
			},"??????? ?? ?????? ??????????? ? ?????????? ? ???????");
			
			
			Column <DouRequestDTO,DouRequestDTO>printColumn =new Column<DouRequestDTO,DouRequestDTO>(
					printCell){
					@Override
					public DouRequestDTO getValue(DouRequestDTO object) {
						return object;
					}}; 
					
			hasCells.add(printColumn);
			
			
			CompositeCell<DouRequestDTO> cell=new CompositeCell<DouRequestDTO>(
				hasCells );
			
			
			Column <DouRequestDTO,DouRequestDTO>actionColumn =new Column<DouRequestDTO,DouRequestDTO>(
				cell){
			
					@Override
					public DouRequestDTO getValue(DouRequestDTO object) {
						return object;
					}}; 
					
			requestsTable.addColumn(actionColumn, SafeHtmlUtils.fromSafeConstant("????????"));
			requestsTable.setColumnWidth(actionColumn, "90px");
		}	else {
			
		}
		
	}
	
	
	public HasDirectionalHtml getComments(){
		return commentsHtml;
	}
	
	
	
	
	
	public HasConstrainedValue<RecordColorWrapper>getRecordColor(){
		return recordColorListBox;
	}
	
	public HasText getAgeFrom(){
		return ageFrom;
	}
	
	public HasText getAgeTo(){
		return ageTo;
	}
	
	
	public  HasValue<Date> getRegDateFrom(){
		return regDateFrom;
	}
	
	public  HasValue<Date> getRegDateTo(){
		return regDateTo;
	}
	
	public  HasValue<Date> getSverkDateFrom(){
		return sverkDateFrom;
	}
	
	public  HasValue<Date> getSverkDateTo(){
		return sverkDateTo;
	}
	
	
	public HasValue<Integer>getPlainYear(){
		return plainYearListBox;
	}
	
	
	public HasValue<Boolean>getPhone(){
		return phoneListBox;
	}


	public HasValue<Date> getBirthdayFromDateBox() {
		return bithdayFrom;
	}
	
	public HasValue<Date> getBirthdayToDateBox() {
		return bithdayTo;
	}
	
	
	
	public void populateSRegTable(List<SRegister> result) {
		sRegTable.setRowData(result);
		
	}
	
	
	public HasHTML getAddress() {
		return address;
	}
	
	
	 class RecordColorRenderer extends AbstractRenderer<RecordColorWrapper> {
			private String nullString="???";
			
			public RecordColorRenderer() {
				super();
			}
			@Override
			public String render(RecordColorWrapper o) {
				if(o==null){
					return nullString;
				}else if(o.withoutColor){
					return "??? ?????";
				} else {
					return o.recordColor.getColorName();
				}
			}
	 }


	public List<Long> getSearchedDou() {
		if(chooseDouPanel==null || chooseDouPanel.isAllSelected() || chooseDouPanel.isAllUnselected())
			return null;
		List<Dou> selectedDou = chooseDouPanel.getSelectedDou();
		List<Long> res= new ArrayList<Long>();
		for (Dou dou : selectedDou) {
			res.add(dou.getUId());
		}
		return res;
	}
	 
	
	public  void setSearchedDou(List<Long> doueId) {
		if(chooseDouPanel==null)
			return ;
		chooseDouPanel.selectByUids(doueId, true);
	}
	
	
	 
	 
	
	 
//	 
//	 private Widget createAdvancedForm() {
//		    
//
//		    // Add advanced options to form in a disclosure panel
//		    DisclosurePanel advancedDisclosure = new DisclosurePanel();
//		    advancedDisclosure.setAnimationEnabled(true);
//		    advancedDisclosure.ensureDebugId("cwDisclosurePanel");
//		    advancedDisclosure.setContent(advancedOptions);
//		    layout.setWidget(3, 0, advancedDisclosure);
//		    cellFormatter.setColSpan(3, 0, 2);
//
//		    // Wrap the contents in a DecoratorPanel
//		    DecoratorPanel decPanel = new DecoratorPanel();
//		    decPanel.setWidget(layout);
//		    return decPanel;
//		  }

	 
}

