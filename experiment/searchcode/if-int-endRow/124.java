package com.gft.larozanam.client.componentes.dados;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gft.larozanam.client.componentes.util.ApplicationConstants;
import com.gft.larozanam.shared.exceptions.ArquiteturaException;
import com.gft.larozanam.shared.exceptions.ExceptionManager;
import com.gft.larozanam.shared.util.RPCParametros;
import com.gft.larozanam.shared.util.RPCResultado;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceDateField;
import com.smartgwt.client.data.fields.DataSourceFloatField;
import com.smartgwt.client.data.fields.DataSourceImageField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.rpc.RPCResponse;
import com.smartgwt.client.types.DSDataFormat;
import com.smartgwt.client.types.DSProtocol;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public abstract class GridDataSource extends DataSource {

	public static final String	       CAMPO_ALTERAR	            = "pencil.gif", CAMPO_INATIVAR = "trash.gif",
	        CAMPO_VISUALIZAR = "lookup.gif";

	protected DataSourceImageField	   DATA_SOURCE_FIELD_VISUALIZAR	= new DataSourceImageField(CAMPO_VISUALIZAR, "Vis.", 30),
	        DATA_SOURCE_FIELD_ALTERAR = new DataSourceImageField(CAMPO_ALTERAR, "Alt.", 30),
	        DATA_SOURCE_FIELD_REMOVER = new DataSourceImageField(CAMPO_INATIVAR, "Rem.", 30);

	private final DataSourceField[]	   dataSourceFields;
	private final Map<String, Integer>	dataSourceFieldsIndexes	    = new HashMap<String, Integer>();
	private String	                   servico;
	private RPCParametros	           parametros	                = new RPCParametros();
	private int	                       totalRows	                = -1;
	private IFetchExecutor	           fetchExecutor;
	private FetchType	               fetchState;
	private List<String[]>	           localData;
	private ExceptionManager	       exceptionManager	            = new ExceptionManager();

	public GridDataSource(FetchType fetchState, DataSourceField[] fields) {
		if (fields == null) this.dataSourceFields = this.createColumns();
		else
			this.dataSourceFields = fields;

		int index = 1;
		for (DataSourceField field : dataSourceFields) {
			addField(field);
			dataSourceFieldsIndexes.put(field.getName(), index);
			index++;
		}
		this.setDataProtocol(DSProtocol.CLIENTCUSTOM);
		this.setDataFormat(DSDataFormat.CUSTOM);
		this.setClientOnly(false);
		this.setAutoCacheAllData(false);
		this.setCacheAllData(false);
		this.servico = this.injetarServico();
		this.setFetchType(fetchState);
	}

	public GridDataSource() {
		this(FetchType.DATABASE);
	}

	public GridDataSource(FetchType fetchType) {
		this(fetchType, null);
	}

	public DataSourceField[] getDataSourceFields() {
		return dataSourceFields;
	}

	@Override
	protected Object transformRequest(DSRequest request) {
		String requestId = request.getRequestId();
		DSResponse response = new DSResponse();
		response.setAttribute("clientContext", request.getAttributeAsObject("clientContext"));
		response.setStatus(0);
		switch (request.getOperationType()) {
			case FETCH:
				executeFetch(requestId, request, response);
				break;
			default:
				throw new ArquiteturaException("Opcao " + request.getOperationId() + " nao implementada");
		}
		return request.getData();
	}

	protected void executeFetch(final String requestId, final DSRequest request, final DSResponse response) {
		this.fetchExecutor.executeFetch(requestId, request, response);
	}

	protected abstract DataSourceField[] createColumns();

	protected void copyValues(String[] from, ListGridRecord to) {
		DateTimeFormat dateFormat = ApplicationConstants.GRID_DATE_FORMAT;
		int i = 0;
		for (DataSourceField field : this.dataSourceFields) {
			if (i == from.length) {// se atingirmos o fim dos dados retornados
				                   // do servidor
				break; // paramos o processamento
			}
			if (from[i] == null || from[i].length() == 0) {
				Object obj = null;
				to.setAttribute(field.getName(), obj);

			} else if (field instanceof DataSourceFloatField) {
				Double value = new Double(from[i]);
				to.setAttribute(field.getName(), value);

			} else if (field instanceof DataSourceDateField) {
				Date d = dateFormat.parse(from[i]);
				to.setAttribute(field.getName(), d);

			} else if (field instanceof DataSourceIntegerField) {
				int integer = Integer.parseInt(from[i]);
				to.setAttribute(field.getName(), integer);

			} else {
				String value = from[i];
				to.setAttribute(field.getName(), value);
			}
			i++;
		}
	}

	public void setServico(String servico) {
		this.servico = servico;
	}

	public String getServico() {
		return servico;
	}

	public RPCParametros getParametros() {
		return parametros;
	}

	public void setParametros(RPCParametros parameters) {
		this.parametros = parameters;
	}

	protected abstract String injetarServico();

	public void setFetchType(FetchType fetchState) {
		if (this.fetchState != fetchState) {
			this.fetchState = fetchState;
			this.fetchExecutor = fetchState.createFetchExecutor(this);
		}
	}

	public FetchType getFetchType() {
		return fetchState;
	}

	public void setLocalData(List<String[]> localData) {
		this.localData = localData;
		this.setFetchType(FetchType.LOCAL_DATA);
	}

	public void addLocalData(String[] data) {
		this.setFetchType(FetchType.LOCAL_DATA);
		this.localData.add(data);
	}

	public List<String[]> getLocalData() {
		return localData;
	}

	private interface IFetchExecutor {
		void executeFetch(final String requestId, final DSRequest request, final DSResponse response);
	}

	private class DatabaseFetchExecutor implements IFetchExecutor {
		private final GridRPCAsync	service	= GWT.create(GridRPC.class);

		@Override
		public void executeFetch(final String requestId, final DSRequest request, final DSResponse response) {
			if (GridDataSource.this.totalRows != -1 && GridDataSource.this.totalRows <= (request.getEndRow() + 1)) {
				request.setEndRow(GridDataSource.this.totalRows);
			}

			service.invocarServico(GridDataSource.this.servico,
								   GridDataSource.this.parametros,
								   request.getStartRow(),
								   request.getEndRow(),
								   getSortField(request),
								   new AsyncCallback<RPCResultado>() {

				        public void onFailure(Throwable caught) {
					        exceptionManager.tratarExcecao(caught);
					        response.setStatus(RPCResponse.STATUS_FAILURE);
					        processResponse(requestId, response);
				        }

				        @Override
				        public void onSuccess(RPCResultado result) {
					        ListGridRecord[] list = new ListGridRecord[result.getDados().size()];
					        for (int i = 0; i < list.length; i++) {
						        ListGridRecord record = new ListGridRecord();
						        copyValues(result.getDados().get(i), record);
						        list[i] = record;
					        }
					        GridDataSource.this.totalRows = result.getTotalDados();
					        response.setStartRow(request.getStartRow());
					        response.setTotalRows(result.getTotalDados());
					        int endRow = (request.getEndRow() > result.getTotalDados()) ? result.getTotalDados() : request.getEndRow();
					        response.setEndRow(endRow);
					        response.setData(list);
					        processResponse(requestId, response);
				        }
			        });
		}

		private String[] getSortField(DSRequest request) {
			String[] sort = request.getAttribute("sortBy").split(",");
			if (sort != null && sort.length != 0) {
				int i = 0;
				for (String s : sort) {
					boolean ascending = true;
					if (s.startsWith("-")) {
						s = s.replace("-", "");
						ascending = false;
					}
					sort[i] = String.valueOf(dataSourceFieldsIndexes.get(s));
					if (!ascending) {
						sort[i] = "-" + sort[i];
					}
					i++;
				}
			}
			return sort;
		}
	}

	private class LocalDataFetchExecutor implements IFetchExecutor {
		{
			localData = localData == null ? new ArrayList<String[]>() : localData;
		}

		@Override
		public void executeFetch(String requestId, DSRequest request, DSResponse response) {
			int endRow = (request.getEndRow() > localData.size()) ? localData.size() : request.getEndRow();
			int startRow = request.getStartRow();
			ListGridRecord[] list = new ListGridRecord[(endRow - startRow)];
			for (int i = startRow, row = 0; i < endRow; i++, row++) {
				ListGridRecord record = new ListGridRecord();
				copyValues(localData.get(i), record);
				list[row] = record;
			}
			response.setStartRow(startRow);
			response.setTotalRows(localData.size());
			response.setEndRow(endRow);
			response.setData(list);
			processResponse(requestId, response);
		}
	}

	public enum FetchType {
		DATABASE {
			@Override
			public IFetchExecutor createFetchExecutor(GridDataSource datasource) {
				return datasource.new DatabaseFetchExecutor();
			}
		},
		LOCAL_DATA {
			@Override
			public IFetchExecutor createFetchExecutor(GridDataSource datasource) {
				return datasource.new LocalDataFetchExecutor();
			}
		};
		public abstract IFetchExecutor createFetchExecutor(GridDataSource datasource);

	}
}

