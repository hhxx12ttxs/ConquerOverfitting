package com.abssoft.constructor.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;
import oracle.sql.CLOB;

import com.abssoft.constructor.common.ActionStatus;
import com.abssoft.constructor.common.Attribute;
import com.abssoft.constructor.common.ExportData;
import com.abssoft.constructor.common.FormColumnsArr;
import com.abssoft.constructor.common.Row;
import com.abssoft.constructor.common.RowsArr;
import com.abssoft.constructor.common.metadata.FormColumnMD;

public class FormInstance {
	private FormColumnsArr columnsArr = new FormColumnsArr();
	private Form form;
	private ResultSet rs;
	private OraclePreparedStatement statement;
	private String currentSortBy;
	private Map<?, ?> currentFilterValues;
	private int currentEndRow = -1;
	private RowsArr resultData = new RowsArr();
	private HashMap<Integer, CLOB> ClobHM = new HashMap<Integer, CLOB>();
	private HashMap<Integer, ExportData> exportDatHM = new HashMap<Integer, ExportData>();
	private Session session;

	public FormInstance(Form form) throws SQLException {
		this.form = form;
		this.columnsArr = form.getFormMetaData().getColumns();
		this.session = form.getSession();
	}

	public void closeForm() {
		session.debug("FormInstance. before close...");
		try {
			rs.close();
		} catch (java.sql.SQLException e) {
			session.printErrorStackTrace(e);
		} catch (Exception e) {
			session.printErrorStackTrace(e);
		}
		session.debug("FormInstance. Resultset closed.");
		try {

			statement.close();
		} catch (java.sql.SQLException e) {
			session.printErrorStackTrace(e);
		} catch (Exception e) {
			session.printErrorStackTrace(e);
		}
		session.debug("FormInstance. Statement closed.");
		resultData.clear();
		// resultData = null;
		session.debug("FormInstance. resultData (RowsArr) cleared.");
		ClobHM.clear();
		// ClobHM = null;
		session.debug("FormInstance. ClobHM cleared.");
		session.debug("FormInstance closed...");
		// showReferencedObjects(this);
	}

	public RowsArr fetch(String sortBy, int startRow, int endRow, Map<?, ?> filterValues, boolean forceFetch) {
		// TODO ????????? ???????? ??????? ???????? - ?? ????????? ?????? ? TotalRows

		RowsArr currentData = new RowsArr();
		// /////////////////////////
		if (null != sortBy) {
			String[] sortByArr = sortBy.split(",");
			sortBy = "";
			for (String ss : sortByArr) {
				// System.out.println("######>" + ss);
				sortBy = sortBy + ("".equals(sortBy) ? "" : ", ") + ss.replaceAll("-", "") + (ss.contains("-") ? " desc" : "");
			}
			sortBy = "\n" + "order by " + sortBy;
			session.debug("sortBy:" + sortBy);
		}

		// ////////////////////////
		String sqlText = null;
		boolean isStmntClosedErr = false;
		try {
			// ?????? ????? DataSource ??? ????????? ??????????, ????????, ? ????? ?????????????
			if (forceFetch || -1 == currentEndRow || currentSortBy != sortBy || !currentFilterValues.equals(filterValues)) {
				OracleConnection connection = form.getConnection();
				session.debug("Erase ResultSetData....");
				currentEndRow = -1;
				resultData = new RowsArr();
				ClobHM.clear();
				sqlText = form.getFormSQLText() + ((sortBy != null) ? sortBy : "");
				{
					String totalRowsSqlText = "select count(*) cnt from (" + sqlText + "\n)";
					OraclePreparedStatement rowCntStmnt = (OraclePreparedStatement) connection.prepareStatement(totalRowsSqlText); // statement
					Utils.setFilterValues(session, rowCntStmnt, filterValues);
					session.debug("totalRowsSqlText:\n" + totalRowsSqlText);
					ResultSet rowCntRS = rowCntStmnt.executeQuery();
					rowCntRS.next();
					int totalRows = rowCntRS.getInt("CNT");
					session.debug("totalRows:" + totalRows);
					resultData.setTotalRows(totalRows);
					rowCntRS.close();
					rowCntStmnt.close();
				}
				statement = (OraclePreparedStatement) connection.prepareStatement(sqlText);
				Utils.setFilterValues(session, statement, filterValues);
				session.debug("sqlText:\n" + sqlText);
				// 20100928
				// if (statement.isClosed()) {
				// Utils.debug("FormInstance. statement closed: ");
				// } else {
				isStmntClosedErr = true;
				rs = statement.executeQuery();
				isStmntClosedErr = false;
				// }
				currentSortBy = sortBy;
				currentFilterValues = filterValues;

			}
			session.debug("currentEndRow before: " + currentEndRow + "; Cashe: " + resultData.size() + "; startRow: " + startRow
					+ "; endRow: " + endRow + "; gridHashCode: " + "; sortBy: " + sortBy + "; forceFetch: " + forceFetch);
			// ????????? ?? ResultSet ?? ????????? ??????, ????????? ??? ?????????? ?????? (currentEndRow),
			// ?? ????? ???????? ????????? (endRow) ??? ?? ?????? ResultSet. ???? currentEndRow>endRow -
			// ???? ?? ??????????? - ?????? ??? ???????.
			for (int rowNum = currentEndRow + 1; rowNum <= endRow; rowNum++) {
				boolean isRSclosed = false;
				try {
					if (!rs.next() // !rs.isAfterLast() //!rs.isClosed() &&
					) {
						session.debug("FormInstance. ResultSet ended. rowNum:" + rowNum);
						rs.close();
						statement.close();
						isRSclosed = true;
					}
				} catch (Exception e) {
					session.debug("Error on close resultset/statement: " + e);
					// e.printStackTrace();
					isRSclosed = true;
				}
				if (isRSclosed)
					break;
				Row r = new Row();
				for (int colNum = 0; colNum < columnsArr.size(); colNum++) {
					try {
						FormColumnMD cmd = columnsArr.get(colNum);
						String colName = cmd.getName();
						String formColDataType = cmd.getDataType();
						r.put(colNum, Utils.getAttribute(colName, formColDataType, rs, this));
					} catch (Exception e) {
						r.put(colNum, new Attribute(e.getMessage()));
						session.debug("Error on column: " + columnsArr.get(colNum).getName());
						session.printErrorStackTrace(e);
					}

				}
				resultData.put(rowNum, r);
				currentEndRow = rowNum;
			}

			session.debug("currentEndRow after:" + currentEndRow);
			// ????????? ?? resultData ? ?????????? ?????? ?????? ? ????????? ?????????: startRow ? endRow.
			// ??????? ????? ???????????? ?????? ?? resultData ??? ???????? ??????. ???????? ?????? ??????,
			// ????????? ?? ??????? rs, ?? ???????????????? ?????.

			for (int i = 0; i <= Math.min(endRow, currentEndRow) - startRow; i++) {
				Row r = resultData.get(i + startRow);
				currentData.put(i, r);
				// resultData.remove(i + startRow);
				// System.out.println("col:" + i + "; data1:" + r.get(0));
			}
			currentData.setTotalRows(resultData.getTotalRows());
			session.debug("currentData.TotalRows setted" + resultData.getTotalRows());
		} catch (Exception e) {

			String errText = Utils.getExceptionStackIntoString(e) + "\n";
			errText = errText + "Form:" + form.getFormCode() + "\n";
			errText = errText + "SQL:" + sqlText + "\n";
			session.debug(errText);
			if (!isStmntClosedErr) {
				currentData.setStatus(new ActionStatus(errText, ActionStatus.StatusType.ERROR));
			} else {
				currentData.setStatus(new ActionStatus(errText, ActionStatus.StatusType.SUCCESS));
			}
		}
		currentSortBy = sortBy;
		session.debug("FormInstance.fetch... return.." + this.form.getFormCode());
		return currentData;
	}

	public void setClobHM(HashMap<Integer, CLOB> clobHM) {
		ClobHM = clobHM;
	}

	public HashMap<Integer, CLOB> getClobHM() {
		return ClobHM;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	public Form getForm() {
		return form;
	}

	public HashMap<Integer, ExportData> getExportDatHM() {
		return exportDatHM;
	}

	public Integer setExportData(ExportData exportData) {
		int clobID = exportData.hashCode();
		exportDatHM.put(clobID, exportData);
		return clobID;
	}
}

