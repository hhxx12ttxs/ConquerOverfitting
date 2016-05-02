/*
 * GWT Portlets Framework (http://code.google.com/p/gwtportlets/)
 * Copyright 2010 Business Systems Group (Africa)
 *
 * This file is part of GWT Portlets.
 *
 * GWT Portlets is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GWT Portlets is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GWT Portlets.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwtportlets.portlet.smartgwt.client;

import com.google.gwt.user.client.Window;
import com.smartgwt.client.data.*;
import com.smartgwt.client.util.JSOHelper;
import org.gwtportlets.portlet.client.ui.Portlet;
import org.gwtportlets.portlet.client.ui.PortletFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * The portlet factory which must be used in Smart Portlets.
 *
 * @param <T> The portlet class.
 * @author Carl Crous
 */
public abstract class SmartPortletFactory<T extends SmartPortlet> extends PortletFactory<T> {
    /** The operation type for a fetch request. */
    public static final int OP_FETCH = 1;
    /** The operation type for an add request. */
    public static final int OP_ADD = 2;
    /** The operation type for an update request. */
    public static final int OP_UPDATE = 3;
    /** The operation type for a remove request. */
    public static final int OP_REMOVE = 4;

    /** The unique identifier for the current request which this portlet factory is handling. */
    protected String requestId;
    /** The operation type for the current request. */
    protected int operationType;
    /** The requested starting row (including) for a FETCH request. */
    protected int startRow = -1;
    /** The requested ending row (excluding) for a FETCH request. */
    protected int endRow = -1;
    /**
     * The total number of rows which can be queried.
     * This must be set by the Portlet Data Provider in a FETCH response.
     */
    protected int totalRows = 0;
    /** The ID of the data source which initiated the request */
    protected String dataSourceId;
    /** The ID of the component which initiated the request */
    protected String componentId;
    /** The sort specifiers which must be used to sort the data for a FETCH request. */
    protected SortSpecifierDto sort[];
    /** The advanced filtering criteria. This can be if client side filtering is also performed. */
    protected CriteriaDto criteria;

    /** Data which is returned from the server to the client or
     * the single entry which is transferred from the client to the server. */
    protected List<DataTransferObject> data;

    protected SmartPortletFactory() {
        super();
    }

    protected SmartPortletFactory(Portlet p) {
        super(p);
    }

    /**
     * Applies the limits provided by the factory to a list and sets the TotalRows of the factory.
     * @param list The list to limit.
     * @param <T> The DTO type of the list.
     * @return The limited list.
     */
    public <T extends DataTransferObject> List<T> limitList(List<T> list) {
        if (startRow < 0 || endRow < 0) {
            return list;
        }
        totalRows = list.size();
        return new ArrayList<T>(list.subList(
                Math.min(startRow, list.size()),
                Math.min(endRow, list.size())));
    }

    /**
     * Adds the given dto to a list and returns the list
     * @param dto The dto to add to the list.
     * @param <T> The type of the dto.
     * @return The list with the dto as its only element.
     */
    public <T extends DataTransferObject> List<T> singleResultList(T dto) {
        List<T> list = new ArrayList<T>(1);
        list.add(dto);
        return list;
    }

    /**
     * Initialises the data in this factory for a FETCH request.
     * @param p The portlet which generated the request.
     * @param request The request details.
     */
    public void executeFetch(SmartPortletDataSource dataSource, DSRequest request) {
        operationType = OP_FETCH;

        Integer startRow = request.getStartRow();
        this.startRow = startRow == null ? -1 : startRow;
        Integer endRow = request.getEndRow();
        this.endRow = endRow == null ? -1 : endRow;
        this.sort = SortSpecifierUtil.createSortSpecifierDtoArray(request);
        Criteria criteria = request.getCriteria();
        if (criteria != null) {
            try {
                this.criteria = CriteriaUtil.createCriteriaDto(dataSource, criteria.getJsObj());
            } catch (CriteriaUtil.CriteriaException e) {
                Window.alert("Could not get criteria: " + e.getMessage());
            }
        }
    }

    /**
     * Initialises the data in this factory for an ADD request.
     * @param dataSource The data source which the request is for.
     * @param request The request details.
     */
    public void executeAdd(SmartPortletDataSource dataSource, DSRequest request) {
        operationType = OP_ADD;
        setDataFromRequest(request);
    }

    /**
     * Initialises the data in this factory for an UPDATE request.
     * @param dataSource The data source which the request is for.
     * @param request The request details.
     */
    public void executeUpdate(SmartPortletDataSource dataSource, DSRequest request) {
        operationType = OP_UPDATE;
        setDataFromRequest(request);
    }

    /**
     * Initialises the data in this factory for a REMOVE request.
     * @param dataSource The data source which the request is for.
     * @param request The request details.
     */
    public void executeRemove(SmartPortletDataSource dataSource, DSRequest request) {
        operationType = OP_REMOVE;
        setDataFromRequest(request);
    }

    /**
     * Create a dto for the factory.
     *
     * This method uses the data factory id specified in this factory to decide on
     * the type of the dto to create.
     * @return The new dto for the factory's data soruce.
     */
    public abstract DataTransferObject createDto();

    /**
     * Sets the data for a data source request.
     * @param request The request to set the data for.
     */
    public void setDataFromRequest(DSRequest request) {
        Record rec = new Record();
        Record old = request.getOldValues();
        JSOHelper.apply(old.getJsObj(), rec.getJsObj());
        JSOHelper.apply(request.getData(), rec.getJsObj());

        DataTransferObject dto = createDto(), oldDto = createDto();
        if (dto == null || oldDto == null) {
            throw new RuntimeException("Could not create a DTO for data source " + dataSourceId);
        }
        dto.copyFromRecord(rec);
        oldDto.copyFromRecord(old);
        data = new ArrayList<DataTransferObject>(2);
        data.add(dto);
        data.add(oldDto);
    }

    @Override
    public void refresh(T w) {
        super.refresh(w);
        DSRequest request = w.removeRequest(this);
        if (request == null) {
            return;
        }
        DSResponse response = SmartPortlet.createResponse(request, this);
        response.setData(SmartPortlet.getRecordList(this.data));
        DataSource ds = w.getDataSource(dataSourceId);
        if (ds == null) {
            throw new RuntimeException("Error: Could not find data source with id " + dataSourceId);
        }
        ds.processResponse(request.getRequestId(), response);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getOperationType() {
        return operationType;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getEndRow() {
        return endRow;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public SortSpecifierDto[] getSort() {
        return sort;
    }

    public void setSort(SortSpecifierDto[] sort) {
        this.sort = sort;
    }

    public CriteriaDto getCriteria() {
        return criteria;
    }

    public void setCriteria(CriteriaDto criteria) {
        this.criteria = criteria;
    }

    public List<DataTransferObject> getData() {
        return data;
    }

    public void setData(List<DataTransferObject> data) {
        this.data = data;
    }

    public DataTransferObject getSingleDto() {
        return (data != null && data.size() > 0) ? data.get(0) : null;
    }

    public void setSingleDto(DataTransferObject dto) {
        data = new ArrayList<DataTransferObject>(1);
        data.add(dto);
    }
}

