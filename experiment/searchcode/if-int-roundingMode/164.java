/**
 *  Product: Posterita Web-Based POS and Adempiere Plugin
 *  Copyright (C) 2007  Posterita Ltd
 *  This file is part of POSterita
 *  
 *  POSterita is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Created on May 9, 2006 by alok
 */

package org.posterita.businesslogic.performanceanalysis;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.ecs.XhtmlDocument;
import org.apache.ecs.xhtml.head;
import org.compiere.db.Database;
import org.compiere.model.I_AD_ReportView;
import org.compiere.model.Lookup;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MDocType;
import org.compiere.model.MInOut;
import org.compiere.model.MInvoice;
import org.compiere.model.MLocation;
import org.compiere.model.MOrder;
import org.compiere.model.MOrg;
import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessPara;
import org.compiere.model.MProduct;
import org.compiere.model.MQuery;
import org.compiere.model.MRole;
import org.compiere.model.MTable;
import org.compiere.model.MTransaction;
import org.compiere.model.MUOM;
import org.compiere.model.MUser;
import org.compiere.model.MWarehouse;
import org.compiere.model.PrintInfo;
import org.compiere.print.DataEngine;
import org.compiere.print.MPrintFormat;
import org.compiere.print.MPrintFormatItem;
import org.compiere.print.PrintData;
import org.compiere.print.PrintDataElement;
import org.compiere.print.ReportEngine;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.report.ReportStarter;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Language;
import org.compiere.util.Util;
import org.posterita.Constants;
import org.posterita.beans.BPartnerBean;
import org.posterita.beans.CloseTillBean;
import org.posterita.beans.POSHistoryBean;
import org.posterita.beans.POSReportBean;
import org.posterita.beans.ProductBean;
import org.posterita.beans.WebMinOutLineBean;
import org.posterita.beans.WebOrderLineBean;
import org.posterita.businesslogic.MinOutManager;
import org.posterita.businesslogic.OrganisationManager;
import org.posterita.businesslogic.POSManager;
import org.posterita.businesslogic.POSStockManager;
import org.posterita.businesslogic.POSTerminalManager;
import org.posterita.businesslogic.administration.BPartnerManager;
import org.posterita.businesslogic.administration.ProductManager;
import org.posterita.core.RandomStringGenerator;
import org.posterita.core.TimestampConvertor;
import org.posterita.exceptions.LogoException;
import org.posterita.exceptions.MandatoryException;
import org.posterita.exceptions.OperationException;
import org.posterita.exceptions.UnsupportedDatabaseException;
import org.posterita.lib.UdiConstants;
import org.posterita.order.UDIOrderTypes;
import org.posterita.util.PathInfo;
import org.posterita.util.TmkPrinterConstants;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class POSReportManager {
	
	private static final String ASCENDING_SORT = "asc";
	
	private static String getMaxMinSoldProductsSQL(Properties ctx,
			String reportType, Timestamp fromDate, Timestamp toDate) {
		String sql = "";
		if (DB.getDatabase().getName().equals(Database.DB_POSTGRESQL) || DB.getDatabase().getName().equals(Database.DB_ORACLE)) 
		{
			sql = "select qty,name,COALESCE(upc, ' ') as barcode,M_PRODUCT_ID from "
					+ "(select sum(ol.QTYENTERED) as qty,pr.NAME, pr.UPC,ol.M_PRODUCT_ID,"
					//+ "DENSE_RANK() OVER (ORDER BY sum(ol.QTYENTERED) "
					//+ reportType
					//+ " NULLS LAST) AS Drank"
					+ " max(ol.QTYENTERED) AS Drank"
					+ " from C_ORDERLINE ol,C_ORDER ord,M_product pr"
					+ " where ol.C_ORDER_ID=ord.c_order_id"
					+ " and ol.M_PRODUCT_ID=pr.M_PRODUCT_ID"
					+ " and ol.AD_ORG_ID="
					+ Env.getAD_Org_ID(ctx)
					+ " and ol.AD_CLIENT_ID="
					+ Env.getAD_Client_ID(ctx)
					+ " and ord.ISACTIVE='Y'"
					+ " and ol.CREATED between " + DB.TO_DATE(fromDate, false) 
					+ " and " + DB.TO_DATE(toDate, false) 
					+ " and ord.ORDERTYPE='"+ UDIOrderTypes.POS_ORDER.getOrderType() +"'"
					+ " and ord.DOCSTATUS='CO' group by pr.NAME,pr.UPC,ol.M_PRODUCT_ID) fastStockMovement"
					+ " where drank <26 order by qty " + reportType;
		}

		else
			throw new UnsupportedDatabaseException(
					"Operation GetMaxMinSoldProducts not supported on Database: "
							+ DB.getDatabase().getName());

		return sql;
	}

	public static ArrayList<ProductBean> getMaxMinSoldProducts(Properties ctx,
			String reportType, Timestamp fromDate, Timestamp todate)
			throws OperationException {
		String sql = getMaxMinSoldProductsSQL(ctx, reportType, fromDate, todate);

		PreparedStatement pstmt = DB.prepareStatement(sql, null);

		ProductBean bean = null;
		ArrayList<ProductBean> list = new ArrayList<ProductBean>();

		// TreeMap<Integer,ProductBean> tree = new
		// TreeMap<Integer,ProductBean>();
		try {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				bean = new ProductBean();
				BigDecimal qty = getQtyReturnedFromCustomer(ctx, rs.getInt(4),
						fromDate, todate);
				bean.setProductName(rs.getString(2).replaceAll("~", " "));
				bean.setBarCode(rs.getString(3));
				BigDecimal qty1 = rs.getBigDecimal(1);
				int productId = rs.getInt(4);
				MProduct product = new MProduct(ctx, productId, null);
				String uom = product.getUOMSymbol();
				bean.setUom(uom);
				if (qty1 == null)
				{
					qty1 = BigDecimal.ZERO;
				}
				if (qty == null)
				{
					qty = BigDecimal.ZERO;
				}
					
				BigDecimal quantity = qty1.subtract(qty);
				bean.setQuantity(quantity);

				list.add(bean);
			}
			rs.close();

			Collections.sort(list, getSortingComparator(reportType));
			return list;

		} catch (SQLException e) {
			throw new OperationException(e);
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {
			}

			pstmt = null;
		}

	}

	private static Comparator<ProductBean> getSortingComparator(
			final String order) {
		Comparator<ProductBean> comp = new Comparator<ProductBean>() {

			public int compare(ProductBean o1, ProductBean o2) {
				if (o1.getQuantity().intValue() < o2.getQuantity().intValue())
					return (order.equals(ASCENDING_SORT) ? -1 : 1);
				else if (o1.getQuantity().intValue() > o2.getQuantity()
						.intValue())
					return (order.equals(ASCENDING_SORT) ? 1 : -1);
				else
					return 0;
			}

		};
		return comp;
	}

	private static BigDecimal getQtyReturnedFromCustomer(Properties ctx,
			int productId, Timestamp fromDate, Timestamp toDate)
			throws OperationException {
		BigDecimal qty = Env.ZERO;

		String sql = "select" + " sum(ol.qtyordered)"
				+ " from C_ORDER ord,C_ORDERLINE ol"
				+ "  where ord.C_ORDER_ID=ol.c_order_id "
				+ " and ord.AD_CLIENT_ID=" + Env.getAD_Client_ID(ctx)
				+ " and ol.M_PRODUCT_ID=" + productId + " and ord.orderType='"
				+ UDIOrderTypes.CUSTOMER_RETURN_ORDER.getOrderType() + "'"
				+ " and ol.CREATED between "+ DB.TO_DATE(fromDate, false) + " and " + DB.TO_DATE(toDate, false);

		PreparedStatement pstmt = DB.prepareStatement(sql, null);

		try {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				qty = rs.getBigDecimal(1);
			}

			rs.close();
		} catch (SQLException e) {
			throw new OperationException(e);
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {
			}

			pstmt = null;
		}

		return qty;
	}

	public static ArrayList<POSReportBean> getStockMovementReport(
			Properties ctx, Timestamp fromDate, Timestamp toDate)
			throws OperationException {

		String sql = "select distinct v.m_product_id," + " pr.name"
				+ " from M_TRANSACTION_V v,m_product pr"
				+ " where v.m_product_id=pr.m_product_id"
				+ " and v.CREATED between "+DB.TO_DATE(fromDate, false)
				+ " and "+ DB.TO_DATE(toDate, false)
				+ " and v.AD_CLIENT_ID="
				+ Env.getAD_Client_ID(ctx) + " and v.AD_ORG_ID="
				+ Env.getAD_Org_ID(ctx) + " order by pr.name";

		MWarehouse warehouse = POSTerminalManager.getWarehouse(ctx);
		/*
		 * String sql = "select" + " distinct M_PRODUCT_ID " + //3 " from
		 * M_STORAGE st" + " where st.AD_CLIENT_ID="+Env.getAD_Client_ID(ctx)+ "
		 * and st.AD_ORG_ID=" +Env.getAD_Org_ID(ctx)+ " and st.M_LOCATOR_ID="
		 * +warehouse.getDefaultLocator().getID();
		 */

		// String whereClause = "AD_CLIENT_ID="+Env.getAD_Client_ID(ctx)+" and
		// AD_ORG_ID="+Env.getAD_Org_ID(ctx);

		POSReportBean bean;
		ArrayList<POSReportBean> list = new ArrayList<POSReportBean>();

		PreparedStatement pstmt = DB.prepareStatement(sql, null);

		ResultSet rs;
		try {
			rs = pstmt.executeQuery();
			while (rs.next()) {
				BigDecimal qtyOfSales;
				BigDecimal qtyOfReceipts;
				BigDecimal qtyOfReturn;
				BigDecimal qtyOfCustReturn;
				BigDecimal openingBal;
				BigDecimal closingBal;
				BigDecimal qtyofSalesByCredit;
				BigDecimal qtyInventoryIn;
				// int qtyInventoryOut;

				/*
				 * if(!isProductPresentInOrder(ctx,rs.getInt(1),fromDate,todate))
				 * continue;
				 */
				MProduct product = MProduct.get(ctx, rs.getInt(1));
				int uomPrecision = MUOM.getPrecision(ctx, product.getC_UOM_ID());
				String uom = product.getUOMSymbol();
				openingBal = getQtyOfOrders(ctx, null, rs.getInt(1), fromDate,
						toDate, "opening", warehouse).setScale(uomPrecision, RoundingMode.HALF_UP);
				// closingBal=getQtyOfOrders(ctx,null,rs.getInt(1),fromDate,todate,"closing",warehouse);
				qtyOfSales = getQtyOfOrders(ctx, UDIOrderTypes.POS_ORDER
						.getOrderType(), rs.getInt(1), fromDate, toDate,
						"none", warehouse).setScale(uomPrecision, RoundingMode.HALF_UP);
				qtyofSalesByCredit = getQtyOfOrders(ctx,
						UDIOrderTypes.CREDIT_ORDER.getOrderType(),
						rs.getInt(1), fromDate, toDate, "none", warehouse).setScale(uomPrecision, RoundingMode.HALF_UP);
				qtyOfReceipts = getQtyOfOrders(ctx,
						UDIOrderTypes.POS_GOODS_RECEIVE_NOTE.getOrderType(), rs
								.getInt(1), fromDate, toDate, "none", warehouse).setScale(uomPrecision, RoundingMode.HALF_UP);
				qtyOfReturn = getQtyOfOrders(ctx,
						UDIOrderTypes.POS_GOODS_RETURN_NOTE.getOrderType(), rs
								.getInt(1), fromDate, toDate, "none", warehouse).setScale(uomPrecision, RoundingMode.HALF_UP);
				qtyOfCustReturn = getQtyOfOrders(ctx,
						UDIOrderTypes.CUSTOMER_RETURN_ORDER.getOrderType(), rs
								.getInt(1), fromDate, toDate, "none", warehouse).setScale(uomPrecision, RoundingMode.HALF_UP);
				qtyInventoryIn = getQtyOfOrders(ctx, null, rs.getInt(1),
						fromDate, toDate, "inventortIn", warehouse).setScale(uomPrecision, RoundingMode.HALF_UP);
				// qtyInventoryOut
				// =getQtyOfOrders(ctx,null,rs.getInt(1),fromDate,todate,"inventoryOut",warehouse);
				closingBal = openingBal.subtract(
						(qtyOfSales.add(qtyOfReturn).add(qtyofSalesByCredit).
								add(qtyOfReceipts).add(qtyOfCustReturn).add(qtyInventoryIn)));
				bean = new POSReportBean();
				try 
				{
					bean.setProductName(ProductManager.getProductName(ctx, rs.getInt(1)));
					bean.setProductId(rs.getInt(1));
				} 
				
				catch (Exception e) 
				{
					throw new OperationException(e);
				}

				bean.setOpeningBalanceQty(openingBal);
				bean.setQtyOfGoodsSold(qtyOfSales.add(qtyofSalesByCredit));
				bean.setQtyOfGoodsReceived(qtyOfReceipts);
				bean.setQtyOfGoodsReturned(qtyOfReturn);
				bean.setCloseingBalanceQty(closingBal);
				bean.setQtyInventoryIn(qtyInventoryIn);
				// bean.setQtyInventoryOut(Integer.valueOf(qtyInventoryOut));
				bean.setQtyReturnedByCustomer(qtyOfCustReturn);
				bean.setUom(uom);
				list.add(bean);
			}

			rs.close();

		}

		catch (SQLException e) {
			throw new OperationException(e);
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {
			}

			pstmt = null;
		}

		return list;

	}

	public static ArrayList<Object[]> getStockMovementReportData(
			Properties ctx, Timestamp fromDate, Timestamp todate)
			throws OperationException {

		ArrayList<POSReportBean> list = getStockMovementReport(ctx, fromDate,
				todate);
		ArrayList<Object[]> reportData = new ArrayList<Object[]>();

		Object[] headers = new Object[] { "Product Name", "Uom","Opening Balance",
				"Inventory In / Out", "Qty Received", "Qty Sold",
				"Qty Returned to Supplier", "Qty Returned by Customer",
				"Closing Balance" };

		Object[] data = null;

		reportData.add(headers);

		for (POSReportBean bean : list) {
			data = new Object[9];

			data[0] = bean.getProductName();
			data[1] = bean.getUom();
			data[2] = bean.getOpeningBalanceQty();
			data[3] = bean.getQtyInventoryIn();
			data[4] = bean.getQtyOfGoodsReceived();
			data[5] = bean.getQtyOfGoodsSold();
			data[6] = bean.getQtyOfGoodsReturned();
			data[7] = bean.getQtyReturnedByCustomer();
			data[8] = bean.getCloseingBalanceQty();

			reportData.add(data);
		}

		return reportData;
	}

	private static BigDecimal getQtyOfOrders(Properties ctx, String orderType,
			int productId, Timestamp fromDate, Timestamp toDate, String queryType,
			MWarehouse warehouse) throws OperationException {

		String sql = null;
		String whereClause = "ol.AD_CLIENT_ID=" + Env.getAD_Client_ID(ctx)
				+ " and ol.AD_ORG_ID=" + Env.getAD_Org_ID(ctx);

		// SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		// String date = sdf.format(toDate);

		if (queryType.equalsIgnoreCase("none"))

			sql = "select sum(ol.qtyInvoiced)"
					+ " from c_orderLine ol,C_ORDER ord"
					+ " where ol.C_ORDER_ID=ord.C_ORDER_ID and " + whereClause
					+ " and M_PRODUCT_ID=" + productId
					+ " and ol.CREATED BETWEEN "+DB.TO_DATE(fromDate, false) 
					+ " and "+DB.TO_DATE(toDate, false)
					+ " and ord.ORDERTYPE='"
					+ orderType + "'" + " and ord.DOCSTATUS in ('CO','CL')"
					+ " and ord.M_WAREHOUSE_ID=" + warehouse.get_ID()
					+ " and ord.ISACTIVE='Y'";

		else if (queryType.equalsIgnoreCase("opening"))

			sql = "select sum(MOVEMENTQTY)" + " from M_TRANSACTION_V "
					+ " where M_PRODUCT_ID=" + productId + " and M_LOCATOR_ID="
					+ warehouse.getDefaultLocator().get_ID()
					+ " and AD_CLIENT_ID=" + Env.getAD_Client_ID(ctx)
					+ " and AD_ORG_ID=" + Env.getAD_Org_ID(ctx)
					+ " and created< "+DB.TO_DATE(fromDate, false);

		else if (queryType.equalsIgnoreCase("inventortIn"))
			sql = "select sum(MOVEMENTQTY)" + " from M_TRANSACTION_V "
					+ " where M_PRODUCT_ID=" + productId + " and M_LOCATOR_ID="
					+ warehouse.getDefaultLocator().get_ID()
					+ " and AD_CLIENT_ID=" + Env.getAD_Client_ID(ctx)
					+ " and AD_ORG_ID=" + Env.getAD_Org_ID(ctx)
					+ " and created> "+DB.TO_DATE(fromDate, false) + " and MOVEMENTTYPE='"
					+ MTransaction.MOVEMENTTYPE_InventoryIn + "'";

		else if (queryType.equalsIgnoreCase("inventoryOut"))

			sql = "select sum(MOVEMENTQTY)" + " from M_TRANSACTION_V "
					+ " where M_PRODUCT_ID=" + productId + " and M_LOCATOR_ID="
					+ warehouse.getDefaultLocator().get_ID()
					+ " and AD_CLIENT_ID=" + Env.getAD_Client_ID(ctx)
					+ " and AD_ORG_ID=" + Env.getAD_Org_ID(ctx)
					+ " and created> "+DB.TO_DATE(fromDate, false) + " and MOVEMENTTYPE='"
					+ MTransaction.MOVEMENTTYPE_InventoryOut + "'";

		else if (queryType.equalsIgnoreCase("closing"))

			sql = "select sum(MOVEMENTQTY)" + " from M_TRANSACTION_V "
					+ " where M_PRODUCT_ID=" + productId + " and M_LOCATOR_ID="
					+ warehouse.getDefaultLocator().get_ID()
					+ " and AD_CLIENT_ID=" + Env.getAD_Client_ID(ctx)
					+ " and AD_ORG_ID=" + Env.getAD_Org_ID(ctx)
					+ " and created< "+DB.TO_DATE(toDate, true);
		/*
		 * sql = "select" + " sum(st.QTYONHAND)" + //3 " from M_STORAGE st" + "
		 * where st.M_PRODUCT_ID=" +productId+ " and
		 * st.AD_CLIENT_ID="+Env.getAD_Client_ID(ctx)+ " and st.AD_ORG_ID="
		 * +Env.getAD_Org_ID(ctx)+ " and st.M_LOCATOR_ID="
		 * +warehouse.getDefaultLocator().getID()+ " and created<to_date('"+
		 * toDate+"','DD-MM-YYYY HH24:MI:SS')";
		 */

		PreparedStatement pstmt = DB.prepareStatement(sql, null);

		BigDecimal qty = BigDecimal.ZERO;

		try {
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				qty = rs.getBigDecimal(1);
			}

			rs.close();

		} catch (SQLException e) {
			throw new OperationException(e);
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {
			}

			pstmt = null;
		}
		
		if (qty != null)
		{
			return qty;
		}
		else
			return BigDecimal.ZERO;
	}

	public static ArrayList<POSHistoryBean> getOrderHistory(Properties ctx,
			String orderType, String docStatus, Integer month, Integer year,
			String paymentRule, String trxName) throws OperationException 
	{
	    StringBuffer payAmtSql = new StringBuffer();
	    payAmtSql.append(" (SELECT SUM(p.Amount) FROM");
	    payAmtSql.append(" ((SELECT payAmt as Amount FROM C_Payment p")
	    		 .append(" WHERE p.C_Invoice_ID=i.C_Invoice_ID AND p.DocStatus IN ('CO', 'CL'))");
	    payAmtSql.append(" UNION");
	    payAmtSql.append(" (SELECT Amount FROM C_CashLine cl")
	             .append(" WHERE (cl.c_Invoice_ID=i.C_Invoice_ID))) as p)");
	    
	    StringBuffer sqlStmt = new StringBuffer();
	    sqlStmt.append(" SELECT o.C_Order_ID, o.DateOrdered, o.DateAcct, o.DocumentNo, o.GrandTotal, o.OrderType, o.PaymentRule, o.DocStatus,");
	    sqlStmt.append(" bp.C_BPartner_ID, bp.Name, bp.IsCustomer,");
	    sqlStmt.append(" i.C_Invoice_ID, i.DocumentNo, (CASE WHEN i.C_Invoice_ID IS NULL THEN 0 ELSE ");
	    sqlStmt.append(payAmtSql.toString()).append(" END) as PayAmt");
	    sqlStmt.append(" FROM C_Order o");
	    sqlStmt.append(" INNER JOIN C_BPartner bp ON bp.C_BPartner_ID=o.C_BPartner_ID");
	    sqlStmt.append(" LEFT JOIN C_Invoice i ON (i.C_Order_ID=o.C_Order_ID AND i.DocStatus IN ('CO', 'CL'))");
	    
	    sqlStmt.append(" WHERE o.AD_Client_ID=?");
	    sqlStmt.append(" AND o.IsActive='Y'");
	    
	    if (orderType != null)
	    {
	        sqlStmt = sqlStmt.append(" AND o.OrderType='").append(orderType).append("'");
	    }

        if (paymentRule != null)
        {
            sqlStmt = sqlStmt.append(" AND o.PaymentRule='").append(paymentRule).append("'");
        }
  		
  		if (docStatus != null)
        {
            sqlStmt = sqlStmt.append(" AND o.DocStatus='").append(docStatus).append("'");
        }
        
        if (month != null) 
        {
            String mm = String.valueOf(month);
            if (mm.length() == 1) 
            {
                mm = "0" + mm;
            }

            sqlStmt = sqlStmt.append( " AND TO_CHAR(o.DateOrdered, 'mm')= '").append(mm).append("'");
        }

        if (year != null)
        {
            sqlStmt = sqlStmt.append(" AND TO_CHAR(o.DateOrdered, 'yyyy') ='").append(year).append("'");
        }
        sqlStmt = sqlStmt.append(" ORDER BY o.DateOrdered DESC");
            
	    
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<POSHistoryBean> list = new ArrayList<POSHistoryBean>();

		try 
		{
			pstmt = DB.prepareStatement(sqlStmt.toString(), trxName);
			pstmt.setInt(1, Env.getAD_Client_ID(ctx));
			rs = pstmt.executeQuery();

			while (rs.next()) 
			{
			    POSHistoryBean bean = new POSHistoryBean();
				bean.setOrderId(Integer.valueOf(rs.getInt(1)));
				bean.setDateOrdered(rs.getTimestamp(2));
				bean.setDateAcct(rs.getTimestamp(3));
				bean.setDocumentNo(rs.getString(4));
				bean.setOrderGrandTotal(rs.getBigDecimal(5));
				bean.setOrderType(rs.getString(6));
				bean.setPaymentRule(rs.getString(7));
				bean.setDocStatus(rs.getString(8));
				bean.setBpartnerId(rs.getInt(9));
				bean.setPartnerName(rs.getString(10));
				bean.setIsCustomer("Y".equals(rs.getString(11)) ? true : false);
				bean.setInvoiceDocumentNo(rs.getString(13));
				bean.setAmountPaid(rs.getBigDecimal(14));
				list.add(bean);
			}
		} 
		catch (SQLException e) 
		{
			throw new OperationException("Could not retrieve order history with sql: " + sqlStmt.toString(), e);
		} 
		finally 
		{
		    DB.close(rs, pstmt);
		    pstmt = null;
			pstmt = null;
		}

		return list;

	}

	public static ArrayList<POSHistoryBean> getDraftedOrderHistory(
			Properties ctx, String orderType, Integer month, Integer year)
			throws OperationException {
		String sql = "select ord.C_ORDER_ID," +
		// "inv.c_invoice_id," +
				"ord.created," + "ord.grandtotal," + "ord.DOCUMENTNO,"
				+ "bp.name" + " from c_order ord,C_BPARTNER bp"
				+ " where ord.C_BPARTNER_ID=bp.C_BPARTNER_ID"
				+ " and ord.DOCSTATUS = 'DR'" + " and ord.AD_ORG_ID="
				+ Env.getAD_Org_ID(ctx) + " and ord.AD_CLIENT_ID="
				+ Env.getAD_Client_ID(ctx);

		if (orderType != null)
			sql = sql + " and ord.ORDERTYPE='" + orderType + "'";

		if (month != null)
			sql = sql + " and to_char(ord.created, 'mm') = " + month;

		if (year != null)
			sql = sql + " and to_char(ord.created, 'yyyy') = " + year;

		sql = sql + " order by ord.created desc";

		System.out.println(sql);

		PreparedStatement pstmt = DB.prepareStatement(sql, null);

		POSHistoryBean bean = null;
		ArrayList<POSHistoryBean> list = new ArrayList<POSHistoryBean>();

		try {
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				bean = new POSHistoryBean();
				bean.setOrderId(Integer.valueOf(rs.getInt(1)));

				bean.setDateAcct(rs.getTimestamp(2));
				bean.setOrderGrandTotal(rs.getBigDecimal(3));
				bean.setDocumentNo(rs.getString(4));
				bean.setPartnerName(rs.getString(5));

				list.add(bean);

			}

			rs.close();
		} catch (SQLException e) {
			throw new OperationException(e);
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {
			}

			pstmt = null;
		}
		return list;

	}

	public static ArrayList<POSHistoryBean> getPartialOrderHistory(
			Properties ctx, String orderType, Integer month, Integer year)
			throws OperationException {
		String sql = "select ord.C_ORDER_ID," +
		// "inv.c_invoice_id," +
				"ord.created," + "ord.grandtotal," + "ord.DOCUMENTNO,"
				+ "bp.name" + " from c_order ord,C_BPARTNER bp"
				+ " where ord.C_BPARTNER_ID=bp.C_BPARTNER_ID"
				+ " and ord.DOCSTATUS = 'IP'" + " and ord.AD_ORG_ID="
				+ Env.getAD_Org_ID(ctx) + " and ord.AD_CLIENT_ID="
				+ Env.getAD_Client_ID(ctx);

		if (orderType != null)
			sql = sql + " and ord.ORDERTYPE='" + orderType + "'";

		if (month != null)
		{
			String mm = String.valueOf(month);
        	if (mm.length() == 1)
        	{
        		mm = "0" + mm;
        	}
			sql = sql + " and to_char(ord.created, 'mm') = '" + mm + "' " ;
		}

		if (year != null)
			sql = sql + " and to_char(ord.created, 'yyyy') = " + year;

		sql = sql + " order by ord.created desc";

		System.out.println(sql);

		PreparedStatement pstmt = DB.prepareStatement(sql, null);

		POSHistoryBean bean = null;
		ArrayList<POSHistoryBean> list = new ArrayList<POSHistoryBean>();

		try {
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				bean = new POSHistoryBean();
				bean.setOrderId(Integer.valueOf(rs.getInt(1)));

				bean.setDateAcct(rs.getTimestamp(2));
				bean.setOrderGrandTotal(rs.getBigDecimal(3));
				bean.setDocumentNo(rs.getString(4));
				bean.setPartnerName(rs.getString(5));

				list.add(bean);

			}

			rs.close();
		} catch (SQLException e) {
			throw new OperationException(e);
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {
			}

			pstmt = null;
		}
		return list;

	}

	public static ArrayList<POSHistoryBean> getAllOrderTypes(Properties ctx)
			throws OperationException {
		String sql = "select distinct orderType from c_order where AD_CLIENT_ID="
				+ Env.getAD_Client_ID(ctx)
				+ " and AD_ORG_ID="
				+ Env.getAD_Org_ID(ctx)
				+ " and isActive='Y' and ORDERTYPE is not null";

		PreparedStatement pstmt = DB.prepareStatement(sql, null);

		POSHistoryBean bean = null;
		ArrayList<POSHistoryBean> list = new ArrayList<POSHistoryBean>();

		try {
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				bean = new POSHistoryBean();
				bean.setOrderType(rs.getString(1));

				list.add(bean);

			}

			rs.close();
		} catch (SQLException e) {
			throw new OperationException(e);
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {
			}

			pstmt = null;
		}

		return list;

	}

	public static ArrayList<POSHistoryBean> getAllPaymentRule(Properties ctx)
			throws OperationException {
		String sql = "select distinct paymentRule from c_order where AD_CLIENT_ID="
				+ Env.getAD_Client_ID(ctx)
				+ " and AD_ORG_ID="
				+ Env.getAD_Org_ID(ctx) + " and isActive='Y'";

		PreparedStatement pstmt = DB.prepareStatement(sql, null);

		POSHistoryBean bean = null;
		ArrayList<POSHistoryBean> list = new ArrayList<POSHistoryBean>();

		try {
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				bean = new POSHistoryBean();
				bean.setPaymentRule(rs.getString(1));

				list.add(bean);

			}

			rs.close();
		} catch (SQLException e) {
			throw new OperationException(e);
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {
			}

			pstmt = null;
		}

		return list;

	}

	public static ArrayList<Object[]> getMaxMinSoldProductReportData(
			Properties ctx, String reportType, Timestamp fromDate, Timestamp todate)
			throws OperationException {
		ArrayList<ProductBean> list = getMaxMinSoldProducts(ctx, reportType,
				fromDate, todate);
		ArrayList<Object[]> reportData = new ArrayList<Object[]>();

		reportData.add(new Object[] { "Name", "Uom","Barcode", " Net Qty Sold" });

		String name = null;
		String barcode = null;
		BigDecimal qty = null;
		String uom = null;
		for (ProductBean bean : list) {
			name = bean.getProductName();
			name = name.replaceAll("~", " ");
			qty = bean.getQuantity();
			barcode = bean.getBarCode();
			uom = bean.getUom();
			reportData.add(new Object[] { name, uom, barcode, qty });
		}

		return reportData;
	}

	public static String getCompleteOrderPDFReport(Properties ctx, int orderId,
			String trxName) throws OperationException {
		String docStatus = null;
		String dateOrdered = null;
		String orderType = null;
		String orgName = null;
		String orgAddress = null;
		String salesRep = null;
		String paymentBy = null;
		String customerName = null;
		String customerAddress = null;
		String documentNo = null;
		String currency = "Rs ";
		NumberFormat formatter = new DecimalFormat("###,###,##0.00");

		currency = POSTerminalManager.getDefaultSalesCurrency(ctx)
				.getCurSymbol()
				+ " ";
		MOrder order = new MOrder(ctx, orderId, trxName);

		// getting payment info
		int[] invoiceIds = MInvoice.getAllIDs(MInvoice.Table_Name,
				"AD_CLIENT_ID=" + Env.getAD_Client_ID(ctx) + " and C_ORDER_ID="
						+ order.get_ID(), null);
		double paymentByCash = 0.0;
		double paymentByCard = 0.0;
		double paymentByCheque = 0.0;

		MInvoice invoice = null;
		String paymentRule = null;
		boolean isMixed = false;

		for (int i = 0; i < invoiceIds.length; i++) {
			invoice = new MInvoice(ctx, invoiceIds[i], trxName);

			if (i == 0) {
				paymentRule = invoice.getPaymentRule();
			} else {
				if (!paymentRule.equalsIgnoreCase(invoice.getPaymentRule())) {
					isMixed = true;
				}
			}

			if (invoice.getPaymentRule().equals(MOrder.PAYMENTRULE_Cash)) {
				paymentByCash += invoice.getGrandTotal().doubleValue();
				paymentBy = Constants.PAYMENT_RULE_CASH;
			}

			if (invoice.getPaymentRule().equals(MOrder.PAYMENTRULE_CreditCard)) {
				paymentByCard += invoice.getGrandTotal().doubleValue();
				paymentBy = Constants.PAYMENT_RULE_CARD;
			}

			if (invoice.getPaymentRule().equals(MOrder.PAYMENTRULE_DirectDebit)) {
				paymentByCard += invoice.getGrandTotal().doubleValue();
				paymentBy = Constants.PAYMENT_RULE_CARD;
			}

			if (invoice.getPaymentRule().equals(MOrder.PAYMENTRULE_Check)) {
				paymentByCheque += invoice.getGrandTotal().doubleValue();
				paymentBy = Constants.PAYMENT_RULE_CHEQUE;
			}

		}// for

		if (isMixed) {
			paymentBy = "Mixed (Cash:" + formatter.format(paymentByCash)
					+ " Card:" + formatter.format(paymentByCard) + " Cheque:"
					+ formatter.format(paymentByCheque) + ")";
		}

		// getting orgInfo
		MOrg org = new MOrg(ctx, order.getAD_Org_ID(), trxName);
		int location_id = org.getInfo().getC_Location_ID();
		MLocation location = new MLocation(ctx, location_id, trxName);

		orgName = org.getName();

		String address1 = (location.getAddress1() == null) ? " " : location
				.getAddress1();
		String address2 = (location.getAddress2() == null) ? " " : location
				.getAddress2();
		orgAddress = (address1 + " " + address2).trim();

		// getting order type
		orderType = order.getOrderType();

		// getting orderInfo
		docStatus = order.getDocStatusName();
		documentNo = order.getDocumentNo();

		Date d = new Date(order.getCreated().getTime());
		SimpleDateFormat s = new SimpleDateFormat(TimestampConvertor.DEFAULT_DATE_PATTERN1);
		dateOrdered = s.format(d);

		// getting salesrep
		int saleRep_id = order.getSalesRep_ID();
		MUser user = new MUser(ctx, saleRep_id, trxName);
		salesRep = user.getName();

		// getting customer info
		int bpartner_id = order.getBill_BPartner_ID();
		BPartnerBean bean = BPartnerManager.getBpartner(ctx, bpartner_id,
				trxName);

		String name1 = (bean.getPartnerName() == null) ? " " : bean
				.getPartnerName();
		String name2 = (bean.getName2() == null) ? " " : bean.getName2();
		customerName = (name1 + " " + name2).trim();

		address1 = (bean.getAddress1() == null) ? " " : bean.getAddress1();
		address2 = (bean.getAddress2() == null) ? " " : bean.getAddress2();
		customerAddress = (address1 + " " + address2).trim();

		ArrayList<WebOrderLineBean> orderLineList = POSManager
				.populateOrderLines(ctx, order);

		// ----------------------------------- generating pdf
		// --------------------------------------
		String reportName = RandomStringGenerator.randomstring() + ".pdf";
		String reportPath = ReportManager.getReportPath(reportName);

		Font titleFont = new Font(Font.TIMES_ROMAN, 18, Font.BOLD);
		Font subtitleFont = new Font(Font.TIMES_ROMAN, 14, Font.BOLD);

		Font headerFont = new Font(Font.TIMES_ROMAN, 11, Font.BOLD);
		Font simpleFont = new Font(Font.TIMES_ROMAN, 10);

		float cellBorderWidth = 0.0f;

		// step 1: creation of a document-object
		Document document = new Document(PageSize.A4, 30, 30, 20, 40);// l,r,t,b
		// document.getPageSize().set;

		System.out.println(document.leftMargin());

		try {
			// step 2:
			// we create a writer that listens to the document
			// and directs a PDF-stream to a file
			PdfWriter.getInstance(document, new FileOutputStream(reportPath));

			// step 3: we open the document
			document.open();
			// step 4: we add a paragraph to the document

			Image logo = null;

			String imageURI = PathInfo.PROJECT_HOME + "images/logo.gif";
			// "images/pos/openBLUE_POS_Logo.gif";

			try {
				byte logoData[] = OrganisationManager.getLogo(ctx, null);
				logo = Image.getInstance(logoData);
			} catch (LogoException ex) {
				logo = Image.getInstance(imageURI);
			}

			logo.setAbsolutePosition(document.left(), document.top()
					- logo.getHeight());
			document.add(logo);

			PdfPTable table = new PdfPTable(2);
			PdfPCell cell = null;

			//
			table.getDefaultCell().setPadding(5.0f);
			table.setWidthPercentage(100.0f);

			// header cell
			Paragraph title = new Paragraph();
			title.add(new Chunk(orgName, subtitleFont));
			title.add(new Chunk("\n"));
			title.add(new Chunk(orgAddress, subtitleFont));

			// cell = new PdfPCell(new Paragraph(new
			// Chunk("Title1",titleFont)));
			cell = new PdfPCell(title);

			cell.setColspan(2);
			cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
			cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
			cell.setFixedHeight(logo.getHeight());
			cell.setBorderWidth(cellBorderWidth);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(""));
			cell.setBorderWidth(cellBorderWidth);
			cell.setFixedHeight(10);
			cell.setColspan(2);
			table.addCell(cell);

			// doc type
			cell = new PdfPCell(new Paragraph(new Chunk(orderType, titleFont)));

			cell.setColspan(2);
			cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
			cell.setBorderWidth(cellBorderWidth);
			table.addCell(cell);

			// spacing
			cell = new PdfPCell(new Paragraph(""));
			cell.setBorderWidth(cellBorderWidth);
			cell.setFixedHeight(10);
			cell.setColspan(2);
			table.addCell(cell);

			// row 1
			cell = new PdfPCell(new Paragraph(new Chunk(customerName,
					headerFont)));
			cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
			cell.setBorderWidth(cellBorderWidth);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(new Chunk("Sales Rep: "
					+ salesRep, headerFont)));
			cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
			cell.setBorderWidth(cellBorderWidth);
			table.addCell(cell);

			// row 2
			cell = new PdfPCell(new Paragraph(new Chunk(customerAddress,
					headerFont)));
			cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
			cell.setBorderWidth(cellBorderWidth);
			table.addCell(cell);

			// spacing
			cell = new PdfPCell(new Paragraph(""));
			cell.setBorderWidth(cellBorderWidth);
			cell.setFixedHeight(10);
			cell.setColspan(2);
			table.addCell(cell);

			// row 3
			cell = new PdfPCell(new Paragraph(new Chunk(
					"Ref No: " + documentNo, headerFont)));
			cell.setColspan(2);
			cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
			cell.setBorderWidth(cellBorderWidth);
			table.addCell(cell);

			// row 4
			cell = new PdfPCell(new Paragraph(new Chunk("Doc Status: "
					+ docStatus, headerFont)));
			cell.setColspan(2);
			cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
			cell.setBorderWidth(cellBorderWidth);
			table.addCell(cell);

			// row 5
			cell = new PdfPCell(new Paragraph(new Chunk("Payment By: "
					+ paymentBy, headerFont)));
			cell.setColspan(2);
			cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
			cell.setBorderWidth(cellBorderWidth);
			table.addCell(cell);

			// row 6
			cell = new PdfPCell(new Paragraph(new Chunk("Date: " + dateOrdered,
					headerFont)));
			cell.setColspan(2);
			cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
			cell.setBorderWidth(cellBorderWidth);
			table.addCell(cell);

			// spacing
			cell = new PdfPCell(new Paragraph(""));
			cell.setColspan(2);
			cell.setFixedHeight(10);
			cell.setBorderWidth(cellBorderWidth);
			table.addCell(cell);

			// spacing
			cell = new PdfPCell(new Paragraph(""));
			cell.setColspan(2);
			cell.setFixedHeight(10);
			cell.setBorderWidth(cellBorderWidth);
			table.addCell(cell);

			// ------------------------------------------------------
			cell = new PdfPCell();
			cell.setColspan(2);
			cell.setBorderWidth(cellBorderWidth);

			PdfPTable t = new PdfPTable(6);
			t.getDefaultCell().setPadding(3.0f);
			t.setWidthPercentage(100.0f);

			int[] widths = { 1, 4, 1, 2, 2, 2 };
			t.setWidths(widths);

			// setting headers
			t.addCell(new Paragraph(new Chunk("SerNo", headerFont)));
			t.addCell(new Paragraph(new Chunk("Name", headerFont)));
			t.addCell(new Paragraph(new Chunk("Qty", headerFont)));
			t.addCell(new Paragraph(new Chunk("Price", headerFont)));
			t.addCell(new Paragraph(new Chunk("VAT", headerFont)));
			t.addCell(new Paragraph(new Chunk("Total", headerFont)));

			// setting table data
			// --------------------------------writing table
			// data------------------------------
			int serNo = 0;
			int totalQty = 0;
			double totalAmt = 0.0;
			double totalTaxAmt = 0.0;
			double grandTotal = 0.0;

			BigDecimal qty = null;
			BigDecimal lineAmt = null;
			BigDecimal taxAmt = null;
			BigDecimal lineTotalAmt = null;

			for (WebOrderLineBean orderlineBean : orderLineList) {
				serNo++;
				qty = orderlineBean.getQtyOrdered();
				lineAmt = orderlineBean.getLineNetAmt();
				taxAmt = orderlineBean.getTaxAmt();
				lineTotalAmt = orderlineBean.getLineTotalAmt();

				totalQty += qty.intValue();
				totalAmt += lineAmt.doubleValue();
				totalTaxAmt += taxAmt.doubleValue();
				grandTotal += lineTotalAmt.doubleValue();

				t.addCell(new Paragraph(new Chunk(serNo + "", simpleFont)));
				t.addCell(new Paragraph(new Chunk(orderlineBean
						.getProductName(), simpleFont)));
				t.addCell(new Paragraph(new Chunk(qty.intValue() + "",
						simpleFont)));
				t.addCell(new Paragraph(new Chunk(formatter.format(lineAmt
						.doubleValue()), simpleFont)));
				t.addCell(new Paragraph(new Chunk(formatter.format(taxAmt
						.doubleValue()), simpleFont)));
				t.addCell(new Paragraph(new Chunk(formatter.format(lineTotalAmt
						.doubleValue()), simpleFont)));
			}
			// -----------------------------------------------------------------------------------

			// setting table footer
			t.getDefaultCell().setBackgroundColor(new Color(240, 240, 240));

			PdfPCell c = new PdfPCell(new Paragraph(new Chunk("ORDER TOTAL",
					headerFont)));
			c.setColspan(2);
			c.setBackgroundColor(new Color(240, 240, 240));
			t.addCell(c);

			t.addCell(new Paragraph(new Chunk(totalQty + "", simpleFont)));
			t.addCell(new Paragraph(new Chunk(currency
					+ formatter.format(totalAmt), simpleFont)));
			t.addCell(new Paragraph(new Chunk(currency
					+ formatter.format(totalTaxAmt), simpleFont)));
			t.addCell(new Paragraph(new Chunk(currency
					+ formatter.format(grandTotal), simpleFont)));

			t.setSplitRows(true);
			cell.addElement(t);
			// ------------------------------------------------------

			// table.addCell(cell);
			table.setSplitRows(true);

			document.add(table);
			document.add(t);

		} catch (Exception e) {
			throw new OperationException(e);
		}

		// step 5: we close the document
		document.close();

		return reportName;
	}

	/*
	 * public static String getInvoiceFromOrderPDFReport(Properties ctx, int
	 * orderId, String trxName) throws OperationException { int invoiceIds[] =
	 * InvoiceManager.getInvoiceIdsForOrder(ctx, orderId, trxName);
	 * 
	 * if(invoiceIds.length == 0) throw new OperationException("No invoice found
	 * for Order with id: " + orderId); else if(invoiceIds.length == 1) return
	 * getInvoicePDFReport(ctx, invoiceIds[0], trxName); else return
	 * getCompleteOrderPDFReport(ctx, orderId, trxName); }
	 */

	/*
	 * public static String getInvoicePDFReport(Properties ctx, int invoiceId,
	 * String trxName) throws OperationException { String docStatus = null;
	 * String dateOrdered = null; String docType = null; String orgName = null;
	 * String orgAddress = null; String salesRep = null; String phone = " ";
	 * String fax = " ";
	 * 
	 * String customerName = null; String customerAddress = null; String
	 * documentNo = null; String currency = "Rs "; NumberFormat formatter = new
	 * DecimalFormat("###,###,##0.00");
	 * 
	 * currency =
	 * POSTerminalManager.getPOSDefaultSellCurrency(ctx).getCurSymbol()+ " ";
	 * 
	 * MInvoice invoice = InvoiceManager.loadInvoice(ctx, invoiceId, trxName);
	 * 
	 * //getting orgInfo MOrg org = new
	 * MOrg(ctx,invoice.getAD_Org_ID(),trxName); int location_id =
	 * org.getInfo().getC_Location_ID(); MLocation location = new
	 * MLocation(ctx,location_id,trxName); MBPartner orgPartner = new
	 * MBPartner(ctx, org.getLinkedC_BPartner_ID(), trxName); MBPartnerLocation
	 * meLocation[] = MBPartnerLocation.getForBPartner(ctx,orgPartner.getID());
	 * 
	 * if (meLocation.length != 1) throw new OperationException("Should have
	 * only 1 location for organisation business partner!!");
	 * 
	 * MBPartnerLocation orgLocation = meLocation[0];
	 * 
	 * if (orgLocation.getPhone() != null); phone = orgLocation.getPhone();
	 * 
	 * if (orgLocation.getFax() != null) fax = orgLocation.getFax();
	 * 
	 * orgName = org.getName();
	 * 
	 * String address1 = (location.getAddress1() == null)? " " :
	 * location.getAddress1(); String address2 = (location.getAddress2() ==
	 * null)? " " : location.getAddress2(); orgAddress = (address1 + " " +
	 * address2).trim();
	 * 
	 * //getting order type MDocType doctype = MDocType.get(ctx,
	 * invoice.getC_DocType_ID()); docType = doctype.getName();
	 * 
	 * //getting orderInfo docStatus = invoice.getDocStatusName(); documentNo =
	 * invoice.getDocumentNo();
	 * 
	 * Date d = new Date(invoice.getCreated().getTime()); SimpleDateFormat s =
	 * new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"); dateOrdered = s.format(d);
	 * 
	 * //getting salesrep int saleRep_id = invoice.getSalesRep_ID(); MUser user =
	 * new MUser(ctx,saleRep_id,trxName); salesRep = user.getName();
	 * 
	 * //getting customer info int bpartner_id = invoice.getC_BPartner_ID();
	 * BPartnerBean bean = BPartnerManager.getBpartner(ctx,bpartner_id,
	 * trxName);
	 * 
	 * String name1 = (bean.getPartnerName() == null)? " " :
	 * bean.getPartnerName(); String name2 = (bean.getName2() == null)? " " :
	 * bean.getName2(); customerName = (name1 + " " + name2).trim();
	 * 
	 * address1 = (bean.getAddress1() == null)? " " : bean.getAddress1();
	 * address2 = (bean.getAddress2() == null)? " " : bean.getAddress2();
	 * customerAddress = (address1 + " " + address2).trim();
	 * 
	 * 
	 * ArrayList<WebOrderLineBean> orderLineList =
	 * InvoiceManager.populateInvoiceLines(ctx,invoice, false);
	 * 
	 * //----------------------------------- generating pdf
	 * -------------------------------------- String reportName =
	 * RandomStringGenerator.randomstring() + ".pdf"; String reportPath =
	 * ReportManager.getReportPath(reportName);
	 * 
	 * 
	 * Font titleFont = new Font(Font.TIMES_ROMAN, 18,Font.BOLD); Font
	 * subtitleFont = new Font(Font.TIMES_ROMAN, 14,Font.BOLD);
	 * 
	 * Font headerFont = new Font(Font.TIMES_ROMAN, 11,Font.BOLD); Font
	 * simpleFont = new Font(Font.TIMES_ROMAN, 10);
	 * 
	 * float cellBorderWidth = 0.0f;
	 *  // step 1: creation of a document-object Document document = new
	 * Document(PageSize.A4,30,30,20,40);//l,r,t,b //document.getPageSize().set;
	 * 
	 * System.out.println(document.leftMargin());
	 * 
	 * try { // step 2: // we create a writer that listens to the document //
	 * and directs a PDF-stream to a file PdfWriter.getInstance(document,new
	 * FileOutputStream(reportPath));
	 *  // step 3: we open the document document.open(); // step 4: we add a
	 * paragraph to the document
	 * 
	 * Image logo = null;
	 * 
	 * //TODO: make this part dynamic <------------------------------ IMPORTANT
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!! String imageURI = PathInfo.PROJECT_HOME +
	 * "images/pos/openBLUE_POS_Logo.gif"; logo = Image.getInstance(imageURI);
	 * 
	 * //MAttachment attachment = new
	 * MAttachment(ctx,MOrg.Table_ID,org.getID(),null); //logo =
	 * Image.getInstance(attachment.getEntries()[0].getData());
	 * 
	 * try { byte logoData[] = OrganisationManager.getLogo(ctx, null); logo =
	 * Image.getInstance(logoData); } catch(LogoException ex) { logo =
	 * Image.getInstance(imageURI); }
	 * 
	 * logo.setAbsolutePosition(document.left(),document.top()-logo.height());
	 * document.add(logo);
	 * 
	 * PdfPTable table = new PdfPTable(2); PdfPCell cell = null;
	 *  // table.getDefaultCell().setPadding(5.0f);
	 * table.setWidthPercentage(100.0f);
	 * 
	 * //header cell Paragraph title = new Paragraph(); title.add(new
	 * Chunk(orgName,subtitleFont)); title.add(new Chunk("\n")); title.add(new
	 * Chunk(orgAddress,subtitleFont)); title.add(new Chunk("\n"));
	 * title.add(new Chunk("Phone: " + phone,subtitleFont)); title.add(new
	 * Chunk("\n")); title.add(new Chunk("Fax: " + fax,subtitleFont));
	 * 
	 * 
	 * //cell = new PdfPCell(new Paragraph(new Chunk("Title1",titleFont))); cell =
	 * new PdfPCell(title);
	 * 
	 * cell.setColspan(2); cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
	 * cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
	 * cell.setFixedHeight(logo.height()); cell.setBorderWidth(cellBorderWidth);
	 * table.addCell(cell);
	 * 
	 * cell = new PdfPCell(new Paragraph(""));
	 * cell.setBorderWidth(cellBorderWidth); cell.setFixedHeight(10);
	 * cell.setColspan(2); table.addCell(cell);
	 * 
	 * //doc type cell = new PdfPCell(new Paragraph(new
	 * Chunk(docType,titleFont)));
	 * 
	 * cell.setColspan(2); cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
	 * cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * //spacing cell = new PdfPCell(new Paragraph(""));
	 * cell.setBorderWidth(cellBorderWidth); cell.setFixedHeight(10);
	 * cell.setColspan(2); table.addCell(cell);
	 * 
	 * 
	 * //row 1 cell = new PdfPCell(new Paragraph(new
	 * Chunk(customerName,headerFont)));
	 * cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * 
	 * cell = new PdfPCell(new Paragraph(new Chunk("Sales Rep:
	 * "+salesRep,headerFont)));
	 * cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * //row 2 cell = new PdfPCell(new Paragraph(new
	 * Chunk(customerAddress,headerFont)));
	 * cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * //spacing cell = new PdfPCell(new Paragraph(""));
	 * cell.setBorderWidth(cellBorderWidth); cell.setFixedHeight(10);
	 * cell.setColspan(2); table.addCell(cell);
	 * 
	 * //row 3 cell = new PdfPCell(new Paragraph(new Chunk("No: " + documentNo,
	 * headerFont))); cell.setColspan(2);
	 * cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * //row 4 cell = new PdfPCell(new Paragraph(new Chunk("Doc Status:
	 * "+docStatus,headerFont))); cell.setColspan(2);
	 * cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * 
	 * //row 5 cell = new PdfPCell(new Paragraph(new Chunk("Payment By:
	 * "+paymentBy,headerFont))); cell.setColspan(2);
	 * cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * 
	 * //row 6 cell = new PdfPCell(new Paragraph(new Chunk("Date:
	 * "+dateOrdered,headerFont))); cell.setColspan(2);
	 * cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * //spacing cell = new PdfPCell(new Paragraph("")); cell.setColspan(2);
	 * cell.setFixedHeight(10); cell.setBorderWidth(cellBorderWidth);
	 * table.addCell(cell);
	 * 
	 * //spacing cell = new PdfPCell(new Paragraph("")); cell.setColspan(2);
	 * cell.setFixedHeight(10); cell.setBorderWidth(cellBorderWidth);
	 * table.addCell(cell);
	 * 
	 * 
	 * //------------------------------------------------------ cell = new
	 * PdfPCell(); cell.setColspan(2); cell.setBorderWidth(cellBorderWidth);
	 * 
	 * PdfPTable t = new PdfPTable(6); t.getDefaultCell().setPadding(3.0f);
	 * t.setWidthPercentage(100.0f);
	 * 
	 * int[] widths = {1,4,1,2,2,2}; t.setWidths(widths);
	 * 
	 * //setting headers t.addCell(new Paragraph(new
	 * Chunk("SerNo",headerFont))); t.addCell(new Paragraph(new
	 * Chunk("Name",headerFont))); t.addCell(new Paragraph(new
	 * Chunk("Qty",headerFont))); t.addCell(new Paragraph(new
	 * Chunk("Price",headerFont))); t.addCell(new Paragraph(new
	 * Chunk("VAT",headerFont))); t.addCell(new Paragraph(new
	 * Chunk("Total",headerFont)));
	 * 
	 * //setting table data //--------------------------------writing table
	 * data------------------------------ int serNo = 0; int totalQty = 0;
	 * double totalAmt = 0.0; double totalTaxAmt = 0.0; double grandTotal = 0.0;
	 * 
	 * BigDecimal qty = null; BigDecimal lineAmt = null; BigDecimal taxAmt =
	 * null; BigDecimal lineTotalAmt = null;
	 * 
	 * for (WebOrderLineBean orderlineBean : orderLineList) { serNo++; qty =
	 * orderlineBean.getQtyOrdered(); lineAmt = orderlineBean.getLineNetAmt();
	 * taxAmt = orderlineBean.getTaxAmt(); lineTotalAmt =
	 * orderlineBean.getLineTotalAmt();
	 * 
	 * totalQty += qty.intValue(); totalAmt += lineAmt.doubleValue();
	 * totalTaxAmt += taxAmt.doubleValue(); grandTotal +=
	 * lineTotalAmt.doubleValue();
	 * 
	 * t.addCell(new Paragraph(new Chunk(serNo+"",simpleFont))); t.addCell(new
	 * Paragraph(new Chunk(orderlineBean.getProductName(),simpleFont)));
	 * t.addCell(new Paragraph(new Chunk(qty.intValue()+"",simpleFont)));
	 * t.addCell(new Paragraph(new
	 * Chunk(formatter.format(lineAmt.doubleValue()),simpleFont)));
	 * t.addCell(new Paragraph(new
	 * Chunk(formatter.format(taxAmt.doubleValue()),simpleFont))); t.addCell(new
	 * Paragraph(new
	 * Chunk(formatter.format(lineTotalAmt.doubleValue()),simpleFont))); }
	 * //-----------------------------------------------------------------------------------
	 * 
	 * //setting table footer t.getDefaultCell().setBackgroundColor(new
	 * Color(240,240,240));
	 * 
	 * PdfPCell c = new PdfPCell(new Paragraph(new Chunk("ORDER
	 * TOTAL",headerFont))); c.setColspan(2); c.setBackgroundColor(new
	 * Color(240,240,240)); t.addCell(c);
	 * 
	 * t.addCell(new Paragraph(new Chunk(totalQty + "",simpleFont)));
	 * t.addCell(new Paragraph(new Chunk(currency +
	 * formatter.format(totalAmt),simpleFont))); t.addCell(new Paragraph(new
	 * Chunk(currency + formatter.format(totalTaxAmt),simpleFont)));
	 * t.addCell(new Paragraph(new Chunk(currency +
	 * formatter.format(grandTotal),simpleFont)));
	 * 
	 * t.setSplitRows(true); cell.addElement(t);
	 * //------------------------------------------------------
	 * 
	 * //table.addCell(cell); table.setSplitRows(true);
	 * 
	 * 
	 * document.add(table); document.add(t);
	 *  } catch (Exception e) { throw new OperationException(e); }
	 *  // step 5: we close the document document.close();
	 * 
	 * 
	 * return reportName; }
	 */

	/*
	 * public static String getPaymentPDFReport(Properties ctx, int paymentId,
	 * String trxName) throws OperationException { String docStatus = null;
	 * String dateOrdered = null; String docType = null; String orgName = null;
	 * String orgAddress = null; String salesRep = null; String phone = " ";
	 * String fax = " ";
	 * 
	 * String customerName = null; String customerAddress = null; String
	 * documentNo = null; String currency = "Rs "; NumberFormat formatter = new
	 * DecimalFormat("###,###,##0.00");
	 * 
	 * currency =
	 * POSTerminalManager.getPOSDefaultSellCurrency(ctx).getCurSymbol()+ " ";
	 * 
	 * MPayment payment = PaymentManager.loadPayment(ctx, paymentId, trxName);
	 * 
	 * //getting orgInfo MOrg org = new
	 * MOrg(ctx,payment.getAD_Org_ID(),trxName); int location_id =
	 * org.getInfo().getC_Location_ID(); MLocation location = new
	 * MLocation(ctx,location_id,trxName); MBPartner orgPartner = new
	 * MBPartner(ctx, org.getLinkedC_BPartner_ID(), trxName); MBPartnerLocation
	 * meLocation[] = MBPartnerLocation.getForBPartner(ctx,orgPartner.getID());
	 * 
	 * if (meLocation.length != 1) throw new OperationException("Should have
	 * only 1 location for organisation business partner!!");
	 * 
	 * MBPartnerLocation orgLocation = meLocation[0];
	 * 
	 * if (orgLocation.getPhone() != null); phone = orgLocation.getPhone();
	 * 
	 * if (orgLocation.getFax() != null) fax = orgLocation.getFax();
	 * 
	 * orgName = org.getName();
	 * 
	 * String address1 = (location.getAddress1() == null)? " " :
	 * location.getAddress1(); String address2 = (location.getAddress2() ==
	 * null)? " " : location.getAddress2(); orgAddress = (address1 + " " +
	 * address2).trim();
	 * 
	 * //getting order type MDocType doctype = MDocType.get(ctx,
	 * payment.getC_DocType_ID()); docType = doctype.getName();
	 * 
	 * //getting orderInfo docStatus = payment.getDocStatusName(); documentNo =
	 * payment.getDocumentNo();
	 * 
	 * Date d = new Date(payment.getCreated().getTime()); SimpleDateFormat s =
	 * new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"); dateOrdered = s.format(d);
	 * 
	 * //getting salesrep int saleRep_id = payment.getDoc_User_ID(); MUser user =
	 * new MUser(ctx,saleRep_id,trxName); salesRep = user.getName();
	 * 
	 * //getting customer info int bpartner_id = payment.getC_BPartner_ID();
	 * BPartnerBean bean = BPartnerManager.getBpartner(ctx,bpartner_id,
	 * trxName);
	 * 
	 * String name1 = (bean.getPartnerName() == null)? " " :
	 * bean.getPartnerName(); String name2 = (bean.getName2() == null)? " " :
	 * bean.getName2(); customerName = (name1 + " " + name2).trim();
	 * 
	 * address1 = (bean.getAddress1() == null)? " " : bean.getAddress1();
	 * address2 = (bean.getAddress2() == null)? " " : bean.getAddress2();
	 * customerAddress = (address1 + " " + address2).trim();
	 * 
	 * 
	 * ArrayList<WebDocumentBean> orderLineList =
	 * PaymentManager.getWebPaymentBean(ctx,payment);
	 * 
	 * //----------------------------------- generating pdf
	 * -------------------------------------- String reportName =
	 * RandomStringGenerator.randomstring() + ".pdf"; String reportPath =
	 * ReportManager.getReportPath(reportName);
	 * 
	 * 
	 * Font titleFont = new Font(Font.TIMES_ROMAN, 18,Font.BOLD); Font
	 * subtitleFont = new Font(Font.TIMES_ROMAN, 14,Font.BOLD);
	 * 
	 * Font headerFont = new Font(Font.TIMES_ROMAN, 11,Font.BOLD); Font
	 * simpleFont = new Font(Font.TIMES_ROMAN, 10);
	 * 
	 * float cellBorderWidth = 0.0f;
	 *  // step 1: creation of a document-object Document document = new
	 * Document(PageSize.A4,30,30,20,40);//l,r,t,b //document.getPageSize().set;
	 * 
	 * System.out.println(document.leftMargin());
	 * 
	 * try { // step 2: // we create a writer that listens to the document //
	 * and directs a PDF-stream to a file PdfWriter.getInstance(document,new
	 * FileOutputStream(reportPath));
	 *  // step 3: we open the document document.open(); // step 4: we add a
	 * paragraph to the document
	 * 
	 * Image logo = null;
	 * 
	 * //TODO: make this part dynamic <------------------------------ IMPORTANT
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!! String imageURI = PathInfo.PROJECT_HOME +
	 * "images/pos/openBLUE_POS_Logo.gif"; logo = Image.getInstance(imageURI);
	 * 
	 * //MAttachment attachment = new
	 * MAttachment(ctx,MOrg.Table_ID,org.getID(),null); //logo =
	 * Image.getInstance(attachment.getEntries()[0].getData());
	 * 
	 * try { byte logoData[] = OrganisationManager.getLogo(ctx, null); logo =
	 * Image.getInstance(logoData); } catch(LogoException ex) { logo =
	 * Image.getInstance(imageURI); }
	 * 
	 * logo.setAbsolutePosition(document.left(),document.top()-logo.height());
	 * document.add(logo);
	 * 
	 * PdfPTable table = new PdfPTable(2); PdfPCell cell = null;
	 *  // table.getDefaultCell().setPadding(5.0f);
	 * table.setWidthPercentage(100.0f);
	 * 
	 * //header cell Paragraph title = new Paragraph(); title.add(new
	 * Chunk(orgName,subtitleFont)); title.add(new Chunk("\n")); title.add(new
	 * Chunk(orgAddress,subtitleFont)); title.add(new Chunk("\n"));
	 * title.add(new Chunk("Phone: " + phone,subtitleFont)); title.add(new
	 * Chunk("\n")); title.add(new Chunk("Fax: " + fax,subtitleFont));
	 * 
	 * 
	 * //cell = new PdfPCell(new Paragraph(new Chunk("Title1",titleFont))); cell =
	 * new PdfPCell(title);
	 * 
	 * cell.setColspan(2); cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
	 * cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
	 * cell.setFixedHeight(logo.height()); cell.setBorderWidth(cellBorderWidth);
	 * table.addCell(cell);
	 * 
	 * cell = new PdfPCell(new Paragraph(""));
	 * cell.setBorderWidth(cellBorderWidth); cell.setFixedHeight(10);
	 * cell.setColspan(2); table.addCell(cell);
	 * 
	 * //doc type cell = new PdfPCell(new Paragraph(new
	 * Chunk(docType,titleFont)));
	 * 
	 * cell.setColspan(2); cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
	 * cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * //spacing cell = new PdfPCell(new Paragraph(""));
	 * cell.setBorderWidth(cellBorderWidth); cell.setFixedHeight(10);
	 * cell.setColspan(2); table.addCell(cell);
	 * 
	 * 
	 * //row 1 cell = new PdfPCell(new Paragraph(new
	 * Chunk(customerName,headerFont)));
	 * cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * 
	 * cell = new PdfPCell(new Paragraph(new Chunk("Sales Rep:
	 * "+salesRep,headerFont)));
	 * cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * //row 2 cell = new PdfPCell(new Paragraph(new
	 * Chunk(customerAddress,headerFont)));
	 * cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * //spacing cell = new PdfPCell(new Paragraph(""));
	 * cell.setBorderWidth(cellBorderWidth); cell.setFixedHeight(10);
	 * cell.setColspan(2); table.addCell(cell);
	 * 
	 * //row 3 cell = new PdfPCell(new Paragraph(new Chunk("No: " + documentNo,
	 * headerFont))); cell.setColspan(2);
	 * cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * //row 4 cell = new PdfPCell(new Paragraph(new Chunk("Doc Status:
	 * "+docStatus,headerFont))); cell.setColspan(2);
	 * cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * 
	 * //row 5 cell = new PdfPCell(new Paragraph(new Chunk("Payment By:
	 * "+paymentBy,headerFont))); cell.setColspan(2);
	 * cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * 
	 * //row 6 cell = new PdfPCell(new Paragraph(new Chunk("Date:
	 * "+dateOrdered,headerFont))); cell.setColspan(2);
	 * cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
	 * cell.setBorderWidth(cellBorderWidth); table.addCell(cell);
	 * 
	 * //spacing cell = new PdfPCell(new Paragraph("")); cell.setColspan(2);
	 * cell.setFixedHeight(10); cell.setBorderWidth(cellBorderWidth);
	 * table.addCell(cell);
	 * 
	 * //spacing cell = new PdfPCell(new Paragraph("")); cell.setColspan(2);
	 * cell.setFixedHeight(10); cell.setBorderWidth(cellBorderWidth);
	 * table.addCell(cell);
	 * 
	 * 
	 * //------------------------------------------------------ cell = new
	 * PdfPCell(); cell.setColspan(2); cell.setBorderWidth(cellBorderWidth);
	 * 
	 * PdfPTable t = new PdfPTable(6); t.getDefaultCell().setPadding(3.0f);
	 * t.setWidthPercentage(100.0f);
	 * 
	 * int[] widths = {1,4,1,2,2,2}; t.setWidths(widths);
	 * 
	 * //setting headers t.addCell(new Paragraph(new
	 * Chunk("SerNo",headerFont))); t.addCell(new Paragraph(new
	 * Chunk("Name",headerFont))); t.addCell(new Paragraph(new
	 * Chunk("Qty",headerFont))); t.addCell(new Paragraph(new
	 * Chunk("Price",headerFont))); t.addCell(new Paragraph(new
	 * Chunk("VAT",headerFont))); t.addCell(new Paragraph(new
	 * Chunk("Total",headerFont)));
	 * 
	 * //setting table data //--------------------------------writing table
	 * data------------------------------ int serNo = 0; int totalQty = 0;
	 * double totalAmt = 0.0; double totalTaxAmt = 0.0; double grandTotal = 0.0;
	 * 
	 * BigDecimal qty = null; BigDecimal lineAmt = null; BigDecimal taxAmt =
	 * null; BigDecimal lineTotalAmt = null;
	 * 
	 * for (WebOrderLineBean orderlineBean : orderLineList) { serNo++; qty =
	 * orderlineBean.getQtyOrdered(); lineAmt = orderlineBean.getLineNetAmt();
	 * taxAmt = orderlineBean.getTaxAmt(); lineTotalAmt =
	 * orderlineBean.getLineTotalAmt();
	 * 
	 * totalQty += qty.intValue(); totalAmt += lineAmt.doubleValue();
	 * totalTaxAmt += taxAmt.doubleValue(); grandTotal +=
	 * lineTotalAmt.doubleValue();
	 * 
	 * t.addCell(new Paragraph(new Chunk(serNo+"",simpleFont))); t.addCell(new
	 * Paragraph(new Chunk(orderlineBean.getProductName(),simpleFont)));
	 * t.addCell(new Paragraph(new Chunk(qty.intValue()+"",simpleFont)));
	 * t.addCell(new Paragraph(new
	 * Chunk(formatter.format(lineAmt.doubleValue()),simpleFont)));
	 * t.addCell(new Paragraph(new
	 * Chunk(formatter.format(taxAmt.doubleValue()),simpleFont))); t.addCell(new
	 * Paragraph(new
	 * Chunk(formatter.format(lineTotalAmt.doubleValue()),simpleFont))); }
	 * //-----------------------------------------------------------------------------------
	 * 
	 * //setting table footer t.getDefaultCell().setBackgroundColor(new
	 * Color(240,240,240));
	 * 
	 * PdfPCell c = new PdfPCell(new Paragraph(new Chunk("ORDER
	 * TOTAL",headerFont))); c.setColspan(2); c.setBackgroundColor(new
	 * Color(240,240,240)); t.addCell(c);
	 * 
	 * t.addCell(new Paragraph(new Chunk(totalQty + "",simpleFont)));
	 * t.addCell(new Paragraph(new Chunk(currency +
	 * formatter.format(totalAmt),simpleFont))); t.addCell(new Paragraph(new
	 * Chunk(currency + formatter.format(totalTaxAmt),simpleFont)));
	 * t.addCell(new Paragraph(new Chunk(currency +
	 * formatter.format(grandTotal),simpleFont)));
	 * 
	 * t.setSplitRows(true); cell.addElement(t);
	 * //------------------------------------------------------
	 * 
	 * //table.addCell(cell); table.setSplitRows(true);
	 * 
	 * 
	 * document.add(table); document.add(t);
	 *  } catch (Exception e) { throw new OperationException(e); }
	 *  // step 5: we close the document document.close();
	 * 
	 * 
	 * return reportName; }
	 */

	public static String getShipmentPDFReport(Properties ctx, int minoutId,
			String trxName) throws OperationException {
		String docStatus = null;
		String dateOrdered = null;
		String docType = null;
		String orgName = null;
		String orgAddress = null;
		String salesRep = null;
		String phone = "      ";
		String fax = "       ";

		String customerName = null;
		String customerAddress = null;
		String documentNo = null;

		MInOut minout = MinOutManager.loadMInOut(ctx, minoutId, trxName);

		// getting orgInfo
		MOrg org = new MOrg(ctx, minout.getAD_Org_ID(), trxName);
		int location_id = org.getInfo().getC_Location_ID();
		MLocation location = new MLocation(ctx, location_id, trxName);
		MBPartner orgPartner = new MBPartner(ctx, org.getLinkedC_BPartner_ID(trxName),
				trxName);
		MBPartnerLocation meLocation[] = MBPartnerLocation.getForBPartner(ctx,
				orgPartner.get_ID());

		if (meLocation.length != 1)
			throw new OperationException(
					"Should have only 1 location for organisation business partner!!");

		MBPartnerLocation orgLocation = meLocation[0];

		if (orgLocation.getPhone() != null)
			phone = orgLocation.getPhone();

		if (orgLocation.getFax() != null)
			fax = orgLocation.getFax();
		;

		orgName = org.getName();

		String address1 = (location.getAddress1() == null) ? " " : location
				.getAddress1();
		String address2 = (location.getAddress2() == null) ? " " : location
				.getAddress2();
		orgAddress = (address1 + " " + address2).trim();

		// getting order type
		MDocType doctype = MDocType.get(ctx, minout.getC_DocType_ID());
		docType = doctype.getName();

		// getting orderInfo
		docStatus = minout.getDocStatusName();
		documentNo = minout.getDocumentNo();

		Date d = new Date(minout.getCreated().getTime());
		SimpleDateFormat s = new SimpleDateFormat(TimestampConvertor.DEFAULT_DATE_PATTERN1);
		dateOrdered = s.format(d);

		// getting salesrep
		int saleRep_id = minout.getSalesRep_ID();
		MUser user = new MUser(ctx, saleRep_id, trxName);
		salesRep = user.getName();

		// getting customer info
		int bpartner_id = minout.getC_BPartner_ID();
		BPartnerBean bean = BPartnerManager.getBpartner(ctx, bpartner_id,
				trxName);

		String name1 = (bean.getPartnerName() == null) ? " " : bean
				.getPartnerName();
		String name2 = (bean.getName2() == null) ? " " : bean.getName2();
		customerName = (name1 + " " + name2).trim();

		address1 = (bean.getAddress1() == null) ? " " : bean.getAddress1();
		address2 = (bean.getAddress2() == null) ? " " : bean.getAddress2();
		customerAddress = (address1 + " " + address2).trim();

		ArrayList<WebMinOutLineBean> orderLineList = MinOutManager
				.getWebMinOutLines(ctx, minout);

		// ----------------------------------- generating pdf
		// --------------------------------------
		String reportName = RandomStringGenerator.randomstring() + ".pdf";
		String reportPath = ReportManager.getReportPath(reportName);

		Font titleFont = new Font(Font.TIMES_ROMAN, 18, Font.BOLD);
		Font subtitleFont = new Font(Font.TIMES_ROMAN, 14, Font.BOLD);

		Font headerFont = new Font(Font.TIM
