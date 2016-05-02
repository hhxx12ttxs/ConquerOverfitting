package com.lbslocal.cc.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Pattern;



import com.lbslocal.cc.common.Functions;
import com.lbslocal.cc.common.dataBase;
import com.lbslocal.cc.objects.v0.common.Extent;
import com.lbslocal.cc.objects.v0.common.Pagination;
import com.lbslocal.cc.objects.v0.common.Point;
import com.lbslocal.cc.objects.v3.proximity.ProximityInfo;
import com.lbslocal.cc.objects.v3.proximity.ProximityOptions;
import com.sun.rowset.CachedRowSetImpl;

public class CommonProximity
{
	public CommonProximity()
	{
		
	}
	
	public static int _findClosestFromAddress = 45;
    public static int _findClosestFromXY = 46;
    public static int _findFromExtent = 47;
    public static int _findRadiusFromAddress = 48;
    public static int _findRadiusFromXY = 49;
	
	public ProximityInfo findFromExtent(Extent ext, ProximityOptions po, int idLicenca) throws Exception
	{
		ProximityInfo pi = new ProximityInfo();
		
		try
		{
			String[] tb;
			String[] whereClause;
			
			tb = po.getPoiDataSource().split(Pattern.quote("|"));
			
			if (Functions.IsNullOrEmpty(po.getWhereClause()))
				po.setWhereClause("");
			
			whereClause = po.getWhereClause().split(Pattern.quote("|"));
			
			// verifica se o n?mero de tabelas ? igual ao n?mero de clausulas
			// where
			if ((tb.length != whereClause.length) || (po.getFieldList().length() == 0))
				throw (new Exception("Numero de poiDataSource e diferente do numero de whereClause ou fieldList e esta em branco."));
			
			Boolean validos = validarPoiDataSource(tb, po.getFieldList(), po.getWhereClause(), idLicenca);
			
			if (!validos)
				throw (new Exception("poiDatasource ou fieldList invalidos."));
			
			int i = 0;
			String strSelect = "";
			String fields = po.getFieldList();
			String orderBy = "";
			
			fields = fields.replace("|", ",");
			
			Point point = new Point();
			
			point.setX(ext.getXMin() + (ext.getXMax() - ext.getXMin()) / 2);
			point.setY(ext.getYMin() + (ext.getYMax() - ext.getYMin()) / 2);
			
			if (fields.split(Pattern.quote(",")).length > 0)
				orderBy = " , " + fields.split(Pattern.quote(","))[0];
			
			if (whereClause[0] != "")
				whereClause[0] = "and " + whereClause[0];
			
			strSelect = "select count(1) as total from dados_clientes." + tb[i] + " where x<>0 and ((x>=" + ext.getXMin() + " and x<=" + ext.getXMax() + ") and (y>=" + ext.getYMin() + " and y<="
					+ ext.getYMax() + "))" + whereClause[0];
			
			Connection con = dataBase.getConnection("DADOS_WEBSERVICES");
			
			Statement st = con.createStatement();
			ResultSet rr = st.executeQuery(strSelect.toUpperCase());
			
			rr.next();
			int nrReg = rr.getInt("total");
			
			rr.close();
			st.close();
			con.close();
			
			if (po.getResultRange().getRecordsPerPage() > 50)
				po.getResultRange().setRecordsPerPage(50);
			
			if (po.getResultRange().getRecordsPerPage() < 1)
				po.getResultRange().setRecordsPerPage(5);
			
			Pagination pg = Functions.definePage(nrReg, po.getResultRange().getRecordsPerPage(), po.getResultRange().getPageIndex());
			
			strSelect = "select tabela, x, y, 102960 *  SQRT(POWER(x - " + point.getX() + ",2) + POWER(y - " + point.getY() + ",2)) as distancia, " + fields + " from dados_clientes." + tb[i]
					+ " where x<>0 and ((x>=" + ext.getXMin() + " and x<=" + ext.getXMax() + ") and (y>=" + ext.getYMin() + " and y<=" + ext.getYMax() + "))" + whereClause[0] + " order by distancia "
					+ orderBy + " LIMIT " + (pg.getRecordsInitial() - 1) + "," + (pg.getRecordsFinal());
			
			con = dataBase.getConnection("DADOS_WEBSERVICES");
			
			st = con.createStatement();
			rr = st.executeQuery(strSelect.toUpperCase());
			
			int columnCount = rr.getMetaData().getColumnCount();
			String[][] resultSet = new String[Math.abs((pg.getRecordsInitial() - 1) - pg.getRecordsFinal())][columnCount];
			
			while (rr.next())
			{
				int w = rr.getRow();
				
				for (int j = 1; j < columnCount + 1; j++)
				{
					if (j == 4)
						resultSet[w - 1][j - 1] = String.valueOf(rr.getObject(j)).substring(0, String.valueOf(rr.getObject(j)).indexOf("."));
					else
						resultSet[w - 1][j - 1] = String.valueOf(rr.getObject(j));
				}
			}
			
			pi.setResultSet(resultSet);
			pi.setRecordCount(nrReg);
			
			if (po.getResultRange().getRecordsPerPage() != 0)
			{
				pi.setPageCount(nrReg / po.getResultRange().getRecordsPerPage());
				
				if (pi.getRecordCount() % po.getResultRange().getRecordsPerPage() > 0)
					pi.setPageCount(pi.getPageCount() + 1);
			}
			else
				pi.setPageCount(1);
			
			rr.close();
			
			st.close();
			con.close();
		}
		catch (Exception ex)
		{
			throw (ex);
		}
		
		return pi;
	}
	
	public ProximityInfo findClosestFromXY(Point point, ProximityOptions po, int idLicenca) throws Exception
	{
		ProximityInfo pi = new ProximityInfo();
		
		try
		{
			String[] tb;
			String[] whereClause;
			
			tb = po.getPoiDataSource().split(Pattern.quote("|"));
			
			if (Functions.IsNullOrEmpty(po.getWhereClause()))
				po.setWhereClause("");
			
			whereClause = po.getWhereClause().split(Pattern.quote("|"));
			
			// verifica se o n?mero de tabelas ? igual ao n?mero de clausulas
			// where
			if ((tb.length != whereClause.length) || (po.getFieldList().length() == 0))
				throw (new Exception("Numero de poiDataSource e diferente do numero de whereClause ou fieldList e esta em branco."));
			
			Boolean validos = validarPoiDataSource(tb, po.getFieldList(), po.getWhereClause(), idLicenca);
			
			if (!validos)
				throw (new Exception("poiDatasource ou fieldList invalidos."));
			
			int i = 0;
			String strSelect = "";
			String fields = po.getFieldList();
			String orderBy = "";
			
			fields = fields.replace("|", ",");
			
			if (fields.split(Pattern.quote(",")).length > 0)
				orderBy = " , " + fields.split(Pattern.quote(","))[0];
			
			if (whereClause[0] != "")
				whereClause[0] = "and " + whereClause[0];
			
			strSelect = "select count(1) as total from dados_clientes." + tb[i] + " where x<>0 " + whereClause[0];
			
			Connection con = dataBase.getConnection("DADOS_WEBSERVICES");
			
			Statement st = con.createStatement();
			ResultSet rr = st.executeQuery(strSelect.toUpperCase());
			
			rr.next();
			int nrReg = rr.getInt("total");
			
			rr.close();
			st.close();
			con.close();
			
			if (po.getResultRange().getRecordsPerPage() > 50)
				po.getResultRange().setRecordsPerPage(50);
			
			if (po.getResultRange().getRecordsPerPage() < 1)
				po.getResultRange().setRecordsPerPage(5);
			
			Pagination pg = Functions.definePage(nrReg, po.getResultRange().getRecordsPerPage(), po.getResultRange().getPageIndex());
			
			strSelect = "select tabela, x, y, 102960 *  SQRT(POWER(x - " + point.getX() + ",2) + POWER(y - " + point.getY() + ",2)) as distancia, " + fields + " from dados_clientes." + tb[i]
					+ " where x<>0 " + whereClause[0] + " order by distancia " + orderBy + " LIMIT " + (pg.getRecordsInitial() - 1) + "," + (pg.getRecordsFinal());
			
			con = dataBase.getConnection("DADOS_WEBSERVICES");
			
			st = con.createStatement();
			rr = st.executeQuery(strSelect.toUpperCase());
			
			int columnCount = rr.getMetaData().getColumnCount();
			String[][] resultSet = new String[Math.abs((pg.getRecordsInitial() - 1) - pg.getRecordsFinal())][columnCount];
			
			while (rr.next())
			{
				int w = rr.getRow();
				
				for (int j = 1; j < columnCount + 1; j++)
				{
					if (j == 4)
						resultSet[w - 1][j - 1] = String.valueOf(rr.getObject(j)).substring(0, String.valueOf(rr.getObject(j)).indexOf("."));
					else
						resultSet[w - 1][j - 1] = String.valueOf(rr.getObject(j));
				}
			}
			
			pi.setResultSet(resultSet);
			pi.setRecordCount(nrReg);
			
			if (po.getResultRange().getRecordsPerPage() != 0)
			{
				pi.setPageCount(nrReg / po.getResultRange().getRecordsPerPage());
				
				if (pi.getRecordCount() % po.getResultRange().getRecordsPerPage() > 0)
					pi.setPageCount(pi.getPageCount() + 1);
			}
			else
				pi.setPageCount(1);
			
			rr.close();
			
			st.close();
			con.close();
		}
		catch (Exception ex)
		{
			throw (ex);
		}
		
		return pi;
	}
	
	public ProximityInfo findRadiusFromXY(Point point, int radius, ProximityOptions po, int idLicenca) throws Exception
	{
		ProximityInfo pi = new ProximityInfo();
		
		try
		{
			String[] tb;
			String[] whereClause;
			
			tb = po.getPoiDataSource().split(Pattern.quote("|"));
			
			if (Functions.IsNullOrEmpty(po.getWhereClause()))
				po.setWhereClause("");
			
			whereClause = po.getWhereClause().split(Pattern.quote("|"));
			
			// verifica se o n?mero de tabelas ? igual ao n?mero de clausulas
			// where
			if ((tb.length != whereClause.length) || (po.getFieldList().length() == 0))
				throw (new Exception("Numero de poiDataSource e diferente do numero de whereClause ou fieldList e esta em branco."));
			
			Boolean validos = validarPoiDataSource(tb, po.getFieldList(), po.getWhereClause(), idLicenca);
			
			if (!validos)
				throw (new Exception("poiDatasource ou fieldList invalidos."));
			
			int i = 0;
			String strSelect = "";
			String fields = po.getFieldList();
			String orderBy = "";
			
			fields = fields.replace("|", ",");
			
			Extent ext = new Extent();
			
			double raio = radius * 9.7125097125097125097125097125097e-6;
			
			ext.setXMin(point.getX() - raio);
			ext.setXMax(point.getX() + raio);
			ext.setYMin(point.getY() + raio);
			ext.setYMax(point.getY() - raio);
			
			if (fields.split(Pattern.quote(",")).length > 0)
				orderBy = " , " + fields.split(Pattern.quote(","))[0];
			
			if (whereClause[0] != "")
				whereClause[0] = "and " + whereClause[0];
			
			strSelect = "select count(1) as total from dados_clientes." + tb[i] + " where x<>0 and ((x>=" + ext.getXMin() + " and x<=" + ext.getXMax() + ") and (y>=" + ext.getYMin() + " and y<="
					+ ext.getYMax() + "))" + whereClause[0];
			
			Connection con = dataBase.getConnection("DADOS_WEBSERVICES");
			
			Statement st = con.createStatement();
			ResultSet rr = st.executeQuery(strSelect.toUpperCase());
			
			rr.next();
			int nrReg = rr.getInt("total");
			
			rr.close();
			st.close();
			con.close();
			
			if (po.getResultRange().getRecordsPerPage() > 50)
				po.getResultRange().setRecordsPerPage(50);
			
			if (po.getResultRange().getRecordsPerPage() < 1)
				po.getResultRange().setRecordsPerPage(5);
			
			Pagination pg = Functions.definePage(nrReg, po.getResultRange().getRecordsPerPage(), po.getResultRange().getPageIndex());
			
			strSelect = "select tabela, x, y, 102960 *  SQRT(POWER(x - " + point.getX() + ",2) + POWER(y - " + point.getY() + ",2)) as distancia, " + fields + " from dados_clientes." + tb[i]
					+ " where x<>0 and ((x>=" + ext.getXMin() + " and x<=" + ext.getXMax() + ") and (y>=" + ext.getYMin() + " and y<=" + ext.getYMax() + "))" + whereClause[0] + " order by distancia "
					+ orderBy + " LIMIT " + (pg.getRecordsInitial() - 1) + "," + (pg.getRecordsFinal());
			
			con = dataBase.getConnection("DADOS_WEBSERVICES");
			
			st = con.createStatement();
			rr = st.executeQuery(strSelect.toUpperCase());
			
			int columnCount = rr.getMetaData().getColumnCount();
			String[][] resultSet = new String[Math.abs((pg.getRecordsInitial() - 1) - pg.getRecordsFinal())][columnCount];
			
			while (rr.next())
			{
				int w = rr.getRow();
				
				for (int j = 1; j < columnCount + 1; j++)
				{
					if (j == 4)
						resultSet[w - 1][j - 1] = String.valueOf(rr.getObject(j)).substring(0, String.valueOf(rr.getObject(j)).indexOf("."));
					else
						resultSet[w - 1][j - 1] = String.valueOf(rr.getObject(j));
				}
			}
			
			pi.setResultSet(resultSet);
			pi.setRecordCount(nrReg);
			
			if (po.getResultRange().getRecordsPerPage() != 0)
			{
				pi.setPageCount(nrReg / po.getResultRange().getRecordsPerPage());
				
				if (pi.getRecordCount() % po.getResultRange().getRecordsPerPage() > 0)
					pi.setPageCount(pi.getPageCount() + 1);
			}
			else
				pi.setPageCount(1);
			
			rr.close();
			
			st.close();
			con.close();
		}
		catch (Exception ex)
		{
			throw (ex);
		}
		
		return pi;
	}
	
	public Boolean validarPoiDataSource(String[] tb, String fieldList, String whereClause, int idLicenca)
	{
		try
		{
			int nrReg = 0;
			int i = 0;
			String where = "";
			
			// valida fieldList: n?o pode ter * no select, somente os campos
			// desejados
			if (fieldList.indexOf("*") != -1)
				return false;
			
			// valida where
			whereClause = whereClause.toLowerCase();
			
			if (whereClause.indexOf("drop") != -1)
				return false;
			if (whereClause.indexOf("delete") != -1)
				return false;
			if (whereClause.indexOf("update") != -1)
				return false;
			if (whereClause.indexOf("insert") != -1)
				return false;
			if (whereClause.indexOf("grant") != -1)
				return false;
			if (whereClause.indexOf("revoke") != -1)
				return false;
			if (whereClause.indexOf("create") != -1)
				return false;
			if (whereClause.indexOf("truncate") != -1)
				return false;
			if (whereClause.indexOf("select") != -1)
				return false;
			if (whereClause.indexOf(";") != -1)
				return false;
			if (whereClause.indexOf("--") != -1)
				return false;
			if (whereClause.indexOf("/*") != -1)
				return false;
			
			Connection con = dataBase.getConnection("DADOS_CLIENTES");
			
			Statement st = con.createStatement();
			ResultSet rr = st.executeQuery("SHOW TABLES LIKE '" + tb[0].toUpperCase() + "'");
			
			CachedRowSetImpl crs = new CachedRowSetImpl();
			crs.populate(rr);
			
			nrReg = crs.size();
			
			rr.close();
			st.close();
			crs.close();
			
			// se n?o existe alguma tabela
			if (nrReg != tb.length)
				return false;
			
			// valida campos
			String[] fields;
			fields = fieldList.split(Pattern.quote("|"));
			where = "";
			
			for (i = 0; i < fields.length; i++)
				where = where + " or field = '" + fields[i] + "'";
			
			where = where.substring(4);
			
			con = dataBase.getConnection("DADOS_CLIENTES");
			
			st = con.createStatement();
			
			rr = st.executeQuery("SHOW COLUMNS FROM DADOS_CLIENTES." + tb[0].toUpperCase() + " WHERE " + where);
			
			crs = new CachedRowSetImpl();
			crs.populate(rr);
			
			nrReg = crs.size();
			
			rr.close();
			st.close();
			crs.close();
			
			if (nrReg != fields.length)
				return false;
			
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
}

