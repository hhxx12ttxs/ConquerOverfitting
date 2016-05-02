package logic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jooq.InsertQuery;
import org.jooq.InsertValuesStep;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;
import org.jooq.UpdateQuery;
import org.jooq.impl.Factory;
import org.jooq.util.*;


import tables.tables.records.AccessRecord;
import tables.tables.records.ClassRecord;
import tables.tables.records.ClientRecord;
import tables.tables.records.ControlRecord;
import tables.tables.records.ObjectRecord;
import tables.tables.records.SeriesRecord;
import dataService.ServiceClass;
import datatypes.vhomeAccess;
import datatypes.vhomeClass;
import datatypes.vhomeClient;
import datatypes.vhomeObject;
import datatypes.vhomeTimeSeriesElement;
import db.vhomeDb;

public class vhomeAPI {

	private static Logger log = Logger.getLogger(ServiceClass.class.getName());
	private vhomeDb db=null; 
	public vhomeAPI() {
		db = new vhomeDb();
	}
	
	
	public int init() {
		int i = 0;
		if(db.equals(null)) return -3; 
		i = this.db.init();
		if(i==1) 
			return 1;
		else
			return i; 
	}
	public int close() 
	{
		int i=0;
		i = this.db.close();
		return i;
	}
	
	
	
	public List<vhomeTimeSeriesElement> getSeries(HashMap<String, String> attrib)
	{
		long timestamp ; Double value ;
		List<vhomeTimeSeriesElement> retVals = new ArrayList<vhomeTimeSeriesElement>(); 
		Result<Record> result = null ; 
		try {
		// A general, dialect-unspecific factory
		Factory create = new Factory(this.db.getConn(), Config.sqldialect);
		SelectQuery q = create.selectQuery();
		q.addFrom(tables.Tables.SERIES);
		for (Map.Entry<String, String> e : attrib.entrySet()) {
				if(e.getKey().equals("value_low"))
				{q.addConditions(tables.Tables.SERIES.VALUE.greaterOrEqual(Double.parseDouble(e.getValue())));}
				else if(e.getKey().equals("value_high"))
				{q.addConditions(tables.Tables.SERIES.VALUE.lessOrEqual((Double.parseDouble(e.getValue()))));}
				if(e.getKey().equals("timestamp_low"))
				{q.addConditions(tables.Tables.SERIES.TIMESTAMP.greaterOrEqual(Long.parseLong(e.getValue())));}
				else if(e.getKey().equals("timestamp_high"))
				{q.addConditions(tables.Tables.SERIES.TIMESTAMP.lessOrEqual((Long.parseLong(e.getValue()))));}
				else if(e.getKey().equals("cid"))
				{q.addConditions(tables.Tables.SERIES.CID.equal(Integer.parseInt(e.getValue())));}
				else if(e.getKey().equals("oid"))
				{q.addConditions(tables.Tables.SERIES.OID.equal(Integer.parseInt(e.getValue())));}
				
		}
		
		result = q.fetch();
		
		for (Record r : result) {
            
			timestamp = r.getValue(tables.Tables.SERIES.TIMESTAMP);
			value = r.getValue(tables.Tables.SERIES.VALUE);
			vhomeTimeSeriesElement v = new vhomeTimeSeriesElement(timestamp, value);
            retVals.add(v);
		}
		
		return retVals;
		}
		catch (Exception e) 
		{
			log.warn("vhomeAPI getSeries Exception "+e.getStackTrace());
			// TODO : add more meaningful return value here
			return retVals;
		}
	
		
	}
	public List<vhomeObject> getObject(HashMap<String, String> attrib1 , HashMap<String, String> attrib2 )
	{
		int cid, oid,granularity; String oname, descriptor;
		String symbol ;
		List<vhomeObject> retVals = new ArrayList<vhomeObject>(); 
		Result<Record> result = null ; 
		try {
		// A general, dialect-unspecific factory
		Factory create = new Factory(this.db.getConn(), Config.sqldialect);
		SelectQuery q = create.selectQuery();
		q.addFrom(tables.Tables.OBJECT);
		q.addJoin(tables.Tables.CLASS, tables.Tables.OBJECT.CID.equal(tables.Tables.CLASS.CID));
		
		for (Map.Entry<String, String> e : attrib1.entrySet()) {
				if(e.getKey().equals("rating_low"))
				{q.addConditions(tables.Tables.CLASS.RATING.greaterOrEqual(Double.parseDouble((e.getValue()))));}
				else if(e.getKey().equals("cid"))
				{q.addConditions(tables.Tables.CLASS.CID.equal(Integer.parseInt(e.getValue())));}
				else if(e.getKey().equals("cname"))
				{q.addConditions(tables.Tables.CLASS.CNAME.equal(e.getValue()));}
				else if(e.getKey().equals("rating_high"))
				{q.addConditions(tables.Tables.CLASS.RATING.lessOrEqual((Double.parseDouble((e.getValue())))));}
				
		}
		
		for (Map.Entry<String, String> e : attrib2.entrySet()) {
			if(e.getKey().equals("granularity_low"))
			{q.addConditions(tables.Tables.OBJECT.GRANULARITY.greaterOrEqual(Integer.parseInt((e.getValue()))));}
			else if(e.getKey().equals("oid"))
			{q.addConditions(tables.Tables.OBJECT.OID.equal(Integer.parseInt(e.getValue())));}
			else if(e.getKey().equals("oname"))
			{q.addConditions(tables.Tables.OBJECT.ONAME.equal(e.getValue()));}
			else if(e.getKey().equals("granularity_high"))
			{q.addConditions(tables.Tables.OBJECT.GRANULARITY.lessOrEqual((Integer.parseInt((e.getValue())))));}
			
	    }
		
		result = q.fetch();
		
		
		for (Record r : result) {
            cid = r.getValue(tables.Tables.OBJECT.CID);
            oid = r.getValue(tables.Tables.OBJECT.OID);
            oname = r.getValue(tables.Tables.OBJECT.ONAME);
            descriptor = tables.Tables.OBJECT.DESCRIPTOR ==null ? "": r.getValue(tables.Tables.OBJECT.DESCRIPTOR);
            granularity = r.getValue(tables.Tables.OBJECT.GRANULARITY);           
            symbol = tables.Tables.OBJECT.SYMBOL ==null ? "": r.getValue(tables.Tables.OBJECT.SYMBOL);
            
            vhomeObject v = new vhomeObject(cid, oid, oname, descriptor, granularity, symbol);
            retVals.add(v);
		}
		
		return retVals;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			log.warn("vhomeAPI getObject Exception "+e.getStackTrace());
			// TODO : add more meaningful return value here, if something goes wrong
			return retVals;
		}
		
	}
	public List<vhomeClass> getClass(HashMap<String, String> attrib)
	{
		int cid; String cname, descriptor; Double rating ;
		List<vhomeClass> retVals = new ArrayList<vhomeClass>(); 
		Result<Record> result = null ; 
		try {
		// A general, dialect-unspecific factory
		Factory create = new Factory(this.db.getConn(), Config.sqldialect);
		SelectQuery q = create.selectQuery();
		q.addFrom(tables.Tables.CLASS);
		
		for (Map.Entry<String, String> e : attrib.entrySet()) {
				if(e.getKey().equals("rating_low"))
				{q.addConditions(tables.Tables.CLASS.RATING.greaterOrEqual(Double.parseDouble((e.getValue()))));}
				else if(e.getKey().equals("cid"))
				{q.addConditions(tables.Tables.CLASS.CID.equal(Integer.parseInt(e.getValue())));}
				else if(e.getKey().equals("cname"))
				{q.addConditions(tables.Tables.CLASS.CNAME.equal(e.getValue()));}
				else if(e.getKey().equals("rating_high"))
				{q.addConditions(tables.Tables.CLASS.RATING.lessOrEqual((Double.parseDouble((e.getValue())))));}
				
		}
		
		result = q.fetch();
		
		
		for (Record r : result) {
            cid = r.getValue(tables.Tables.CLASS.CID);
            cname = r.getValue(tables.Tables.CLASS.CNAME);
            descriptor = r.getValue(tables.Tables.CLASS.DESCRIPTOR);
            rating = r.getValue(tables.Tables.CLASS.RATING);           
            vhomeClass v = new vhomeClass(cid, cname, descriptor, rating);
            retVals.add(v);
		}
		
		return retVals;
		}
		catch (Exception e) 
		{
			log.warn("vhomeAPI getClass Exception "+e.getStackTrace());
			// TODO : add more meaningful return value here
			return retVals;
		}
	}

	public int addClass(HashMap<String, String> attrib){
	try {
	
		Factory create = new Factory(this.db.getConn(), Config.sqldialect);
		Result<Record> result  = create.select(tables.Tables.CLASS.CID).from(tables.Tables.CLASS).orderBy(tables.Tables.CLASS.CID.desc()).limit(1).fetch();
		
		int cid; 
		cid = result.size() ==0  ? 0 : result.get(0).getValue(tables.Tables.CLASS.CID);
		
		cid++;
		attrib.put("cid", cid+"");
		create = new Factory(this.db.getConn(), Config.sqldialect);
		InsertQuery<ClassRecord> q = create.insertQuery(tables.Tables.CLASS);

		for (Map.Entry<String, String> e : attrib.entrySet()) {
			if(e.getKey().equals("descriptor"))
			{q.addValue(tables.Tables.CLASS.DESCRIPTOR, e.getValue());}
			else if(e.getKey().equals("cname"))
			{q.addValue(tables.Tables.CLASS.CNAME, e.getValue());}		
			else if(e.getKey().equals("rating"))
			{q.addValue(tables.Tables.CLASS.RATING , Double.parseDouble((e.getValue())));}
			else if(e.getKey().equals("cid"))
			{q.addValue(tables.Tables.CLASS.CID, Integer.parseInt(e.getValue()));}		
			
		}
	

		//create.insertInto(tables.Tables.CLASS) ; .set(tables.Tables.CLASS.CNAME, attrib.get("cname")).
		//set(tables.Tables.CLASS.DESCRIPTOR, attrib.get("descriptor")).
	//	set(tables.Tables.CLASS.RATING, Double.parseDouble(attrib.get("rating")))
		if(q.execute()==0) return 0;
		else return cid;
		
		}
		catch (Exception e)
		{
		log.warn("vhomeAPI addClass Exception "+e.getStackTrace());
		e.printStackTrace();
		return 0;
		
		}
	
	}
	
	public int addClient(HashMap<String, String> attrib){
		try {
			
			Factory create = new Factory(this.db.getConn(), Config.sqldialect);
			InsertQuery<ClientRecord> q = create.insertQuery(tables.Tables.CLIENT);

			for (Map.Entry<String, String> e : attrib.entrySet()) {
				if(e.getKey().equals("name"))
				{q.addValue(tables.Tables.CLIENT.NAME, e.getValue());}
				else if(e.getKey().equals("url"))
				{q.addValue(tables.Tables.CLIENT.URL, e.getValue());}		
				else if(e.getKey().equals("description"))
				{q.addValue(tables.Tables.CLIENT.DESCRIPTION , e.getValue());}
				else if(e.getKey().equals("redirecturi"))
				{q.addValue(tables.Tables.CLIENT.REDIRECTURI , e.getValue());}
				else if(e.getKey().equals("clientid"))
				{q.addValue(tables.Tables.CLIENT.CLIENTID , e.getValue());}
				else if(e.getKey().equals("issuedat"))
				{q.addValue(tables.Tables.CLIENT.ISSUEDAT , Timestamp.valueOf(e.getValue()));}
				
			}
		

			//create.insertInto(tables.Tables.CLASS) ; .set(tables.Tables.CLASS.CNAME, attrib.get("cname")).
			//set(tables.Tables.CLASS.DESCRIPTOR, attrib.get("descriptor")).
		//	set(tables.Tables.CLASS.RATING, Double.parseDouble(attrib.get("rating")))
			return q.execute();
			
			}
			catch (Exception e)
			{
				log.warn("vhomeAPI addClient Exception "+e.getStackTrace());
			e.printStackTrace();
			return 0;
			
			}
		
		}
	
	public List<vhomeClient> getClient(HashMap<String, String> attrib){
		List<vhomeClient> retVals = new ArrayList<vhomeClient>(); 
		Result<Record> result = null ; 
		try {
		// A general, dialect-unspecific factory
		Factory create = new Factory(this.db.getConn(), Config.sqldialect);
		SelectQuery q = create.selectQuery();
		q.addFrom(tables.Tables.CLIENT);
		
		for (Map.Entry<String, String> e : attrib.entrySet()) {
				if(e.getKey().equals("clientid"))
				{q.addConditions(tables.Tables.CLIENT.CLIENTID.equal(e.getValue()));}

		}
		
		result = q.fetch();
		
		String name, url, desc, reduri, clientid, dat;
		for (Record r : result) {
            name=r.getValue(tables.Tables.CLIENT.NAME);
            url=r.getValue(tables.Tables.CLIENT.URL);
            desc = r.getValue(tables.Tables.CLIENT.DESCRIPTION);
            reduri = r.getValue(tables.Tables.CLIENT.REDIRECTURI);
            clientid = r.getValue(tables.Tables.CLIENT.CLIENTID);
            dat= r.getValue(tables.Tables.CLIENT.ISSUEDAT) == null ? null : r.getValue(tables.Tables.CLIENT.ISSUEDAT).toString();
            vhomeClient c = new vhomeClient(name, url, desc, reduri, clientid, dat);
            retVals.add(c);
		}
		
		return retVals;
		}
		catch (Exception e) 
		{
			log.warn("vhomeAPI getClient Exception "+e.getStackTrace());
			// TODO : add more meaningful return value here
			return retVals;
		}
		
	}
/*
	
	public List<vhomeAccessCode> getCode(HashMap<String, String> attrib){
		List<vhomeAccessCode> retVals = new ArrayList<vhomeAccessCode>(); 
		Result<Record> result = null ; 
		try {
		// A general, dialect-unspecific factory
		Factory create = new Factory(this.db.getConn(), Config.sqldialect);
		SelectQuery q = create.selectQuery();
		q.addFrom(tables.Tables.ACCESSCODE);
		
		for (Map.Entry<String, String> e : attrib.entrySet()) {
				if(e.getKey().equals("clientid"))
				{q.addConditions(tables.Tables.ACCESSCODE.CLIENTID.equal(e.getValue()));}
				
				else if(e.getKey().equals("code"))
				{q.addConditions(tables.Tables.ACCESSCODE.CODE.equal(e.getValue()));}
				else if(e.getKey().equals("scope"))
				{q.addConditions(tables.Tables.ACCESSCODE.SCOPE.equal(e.getValue()));}
				else if(e.getKey().equals("valid"))
				{q.addConditions(tables.Tables.ACCESSCODE.VALID.equal(Integer.parseInt(e.getValue())));}
				else if(e.getKey().equals("expires"))
				{q.addConditions(tables.Tables.ACCESSCODE.EXPIRES.equal(Integer.parseInt(e.getValue())));}


		}
		
		result = q.fetch();
		
		String  clientid, dat, scope, code; int valid, expires;
		for (Record r : result) {
            expires = r.getValue(tables.Tables.ACCESSCODE.EXPIRES);
            valid=r.getValue(tables.Tables.ACCESSCODE.VALID);
            scope = r.getValue(tables.Tables.ACCESSCODE.SCOPE);
            code = r.getValue(tables.Tables.ACCESSCODE.CODE);
            clientid = r.getValue(tables.Tables.ACCESSCODE.CLIENTID);
            dat=r.getValue(tables.Tables.ACCESSCODE.ISSUEDAT).toString();
            vhomeAccessCode c = new vhomeAccessCode(clientid ,dat, code , scope, expires, valid );
            retVals.add(c);
		}
		
		return retVals;
		}
		catch (Exception e) 
		{
			// TODO : add more meaningful return value here
			return retVals;
		}
		
	}
*/
	/*
	public int addCode(HashMap<String, String> attrib){
		try {
			
			Factory create = new Factory(this.db.getConn(), Config.sqldialect);
			InsertQuery<ClientRecord> q = create.insertQuery(tables.Tables.CLIENT);

			for (Map.Entry<String, String> e : attrib.entrySet()) {
				if(e.getKey().equals("valid"))
				{q.addValue(tables.Tables.ACCESSCODE.VALID, Integer.parseInt(e.getValue()));}
				else if(e.getKey().equals("expires"))
				{q.addValue(tables.Tables.ACCESSCODE.EXPIRES, Integer.parseInt(e.getValue()));}		
				else if(e.getKey().equals("scope"))
				{q.addValue(tables.Tables.ACCESSCODE.SCOPE , e.getValue());}
				else if(e.getKey().equals("code"))
				{q.addValue(tables.Tables.ACCESSCODE.CODE , e.getValue());}
				else if(e.getKey().equals("clientid"))
				{q.addValue(tables.Tables.ACCESSCODE.CLIENTID , e.getValue());}
				
				
			}
		

			//create.insertInto(tables.Tables.CLASS) ; .set(tables.Tables.CLASS.CNAME, attrib.get("cname")).
			//set(tables.Tables.CLASS.DESCRIPTOR, attrib.get("descriptor")).
		//	set(tables.Tables.CLASS.RATING, Double.parseDouble(attrib.get("rating")))
			return q.execute();
			
			}
			catch (Exception e)
			{
			e.printStackTrace();
			return 0;
			
			} 
		
		}
 */
	public int addAccess(HashMap<String, String> attrib){
		try {
			
			Factory create = new Factory(this.db.getConn(), Config.sqldialect);
			InsertQuery<AccessRecord> q = create.insertQuery(tables.Tables.ACCESS);

			for (Map.Entry<String, String> e : attrib.entrySet()) {
				if(e.getKey().equals("expiresToken"))
				{q.addValue(tables.Tables.ACCESS.EXPIRESTOKEN, Integer.parseInt(e.getValue()));}
				if(e.getKey().equals("expiresCode"))
				{q.addValue(tables.Tables.ACCESS.EXPIRESCODE, Integer.parseInt(e.getValue()));}
				else if(e.getKey().equals("scope"))
				{q.addValue(tables.Tables.ACCESS.SCOPE, e.getValue());}		
				else if(e.getKey().equals("refreshtoken"))
				{q.addValue(tables.Tables.ACCESS.REFRESHTOKEN, e.getValue());}		
				else if(e.getKey().equals("accesstoken"))
				{q.addValue(tables.Tables.ACCESS.ACCESSTOKEN , e.getValue());}
				else if(e.getKey().equals("code"))
				{q.addValue(tables.Tables.ACCESS.CODE , e.getValue());}
				else if(e.getKey().equals("clientid"))
				{q.addValue(tables.Tables.ACCESS.CLIENTID , e.getValue());}
				else if(e.getKey().equals("issuedatCode"))
				{q.addValue(tables.Tables.ACCESS.ISSUEDATCODE , Timestamp.valueOf(e.getValue()));}
				else if(e.getKey().equals("issuedatToken"))
				{q.addValue(tables.Tables.ACCESS.ISSUEDATTOKEN , Timestamp.valueOf(e.getValue()));}
				else if(e.getKey().equals("valid"))
				{q.addValue(tables.Tables.ACCESS.VALID, Integer.parseInt(e.getValue()));}
			}
		

			//create.insertInto(tables.Tables.CLASS) ; .set(tables.Tables.CLASS.CNAME, attrib.get("cname")).
			//set(tables.Tables.CLASS.DESCRIPTOR, attrib.get("descriptor")).
		//	set(tables.Tables.CLASS.RATING, Double.parseDouble(attrib.get("rating")))
			return q.execute();
			
			}
			catch (Exception e)
			{
				log.warn("vhomeAPI addAcccess Exception "+e.getStackTrace());
			e.printStackTrace();
			return 0;
			
			} 
		
		}
	public List<vhomeAccess> getAccess(HashMap<String, String> attrib){
		List<vhomeAccess> retVals = new ArrayList<vhomeAccess>(); 
		Result<Record> result = null ; 
		try {
		// A general, dialect-unspecific factory
		Factory create = new Factory(this.db.getConn(), Config.sqldialect);
		SelectQuery q = create.selectQuery();
		q.addFrom(tables.Tables.ACCESS);
		
		for (Map.Entry<String, String> e : attrib.entrySet()) {
				if(e.getKey().equals("clientid"))
				{q.addConditions(tables.Tables.ACCESS.CLIENTID.equal(e.getValue()));}
				/*if(e.getKey().equals("issuedat"))
				{q.addConditions(tables.Tables.ACCESSCODE.ISSUEDAT.equal(Timestamp.parse(e.getValue()));}*/
				else if(e.getKey().equals("code"))
				{q.addConditions(tables.Tables.ACCESS.CODE.equal(e.getValue()));}
				else if(e.getKey().equals("accesstoken"))
				{q.addConditions(tables.Tables.ACCESS.ACCESSTOKEN.equal(e.getValue()));}
				else if(e.getKey().equals("refreshtoken"))
				{q.addConditions(tables.Tables.ACCESS.REFRESHTOKEN.equal(e.getValue()));}
				else if(e.getKey().equals("scope"))
				{q.addConditions(tables.Tables.ACCESS.SCOPE.equal(e.getValue()));}
				else if(e.getKey().equals("valid"))
				{q.addConditions(tables.Tables.ACCESS.VALID.equal(Integer.parseInt(e.getValue())));}

		}
		
		result = q.fetch();
		
		String  clientid, datCode, datTok,  code, at, rt, scope; int expiresCode, expiresToken, valid;
		for (Record r : result) {
            expiresCode = r.getValue(tables.Tables.ACCESS.EXPIRESCODE);
            expiresToken = r.getValue(tables.Tables.ACCESS.EXPIRESTOKEN);
            code = r.getValue(tables.Tables.ACCESS.CODE);
            clientid = r.getValue(tables.Tables.ACCESS.CLIENTID);
            datCode= r.getValue(tables.Tables.ACCESS.ISSUEDATCODE)==null ? null : r.getValue(tables.Tables.ACCESS.ISSUEDATCODE).toString();
            datTok= r.getValueAsTimestamp(tables.Tables.ACCESS.ISSUEDATTOKEN) == null ? null : r.getValueAsTimestamp(tables.Tables.ACCESS.ISSUEDATTOKEN).toString();
            at = r.getValue(tables.Tables.ACCESS.ACCESSTOKEN);
            rt = r.getValue(tables.Tables.ACCESS.REFRESHTOKEN);
            valid = r.getValue(tables.Tables.ACCESS.VALID);
            scope = r.getValue(tables.Tables.ACCESS.SCOPE);
            vhomeAccess c = new vhomeAccess(code, clientid, at, rt, datCode, datTok, expiresCode, expiresToken, valid, scope);
            retVals.add(c);
		}
		
		return retVals;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			// TODO : add more meaningful return value here
			return retVals;
		}
		
	}
	public int updateAccess(HashMap<String, String> values, HashMap<String, String> conditions){
		try {
			
			Factory create = new Factory(this.db.getConn(), Config.sqldialect);
			UpdateQuery<AccessRecord> q = create.updateQuery(tables.Tables.ACCESS);

			for (Map.Entry<String, String> e : values.entrySet()) {
				if(e.getKey().equals("expiresCode"))
				{q.addValue(tables.Tables.ACCESS.EXPIRESCODE, Integer.parseInt(e.getValue()));}
				if(e.getKey().equals("expiresToken"))
				{q.addValue(tables.Tables.ACCESS.EXPIRESTOKEN, Integer.parseInt(e.getValue()));}
				else if(e.getKey().equals("refreshtoken"))
				{q.addValue(tables.Tables.ACCESS.REFRESHTOKEN, e.getValue());}		
				else if(e.getKey().equals("accesstoken"))
				{q.addValue(tables.Tables.ACCESS.ACCESSTOKEN , e.getValue());}
				else if(e.getKey().equals("code"))
				{q.addValue(tables.Tables.ACCESS.CODE , e.getValue());}
				else if(e.getKey().equals("clientid"))
				{q.addValue(tables.Tables.ACCESS.CLIENTID , e.getValue());}
				else if(e.getKey().equals("issuedatCode"))
				{q.addValue(tables.Tables.ACCESS.ISSUEDATCODE , Timestamp.valueOf(e.getValue()));}
				else if(e.getKey().equals("issuedatToken"))
				{q.addValue(tables.Tables.ACCESS.ISSUEDATTOKEN , Timestamp.valueOf(e.getValue()));}
				else if(e.getKey().equals("valid"))
				{q.addValue(tables.Tables.ACCESS.VALID , Integer.parseInt(e.getValue()));}
				else if(e.getKey().equals("scope"))
				{q.addValue(tables.Tables.ACCESS.SCOPE , e.getValue());}
				
			}
		
			for (Map.Entry<String, String> e : conditions.entrySet()) {
				if(e.getKey().equals("expiresCode"))
				{q.addConditions(tables.Tables.ACCESS.EXPIRESCODE.equal(Integer.parseInt(e.getValue())));}
				if(e.getKey().equals("expiresToken"))
				{q.addConditions(tables.Tables.ACCESS.EXPIRESTOKEN.equal(Integer.parseInt(e.getValue())));}
				else if(e.getKey().equals("refreshtoken"))
				{q.addConditions((tables.Tables.ACCESS.REFRESHTOKEN.equal(e.getValue())));}		
				else if(e.getKey().equals("accesstoken"))
				{q.addConditions(tables.Tables.ACCESS.ACCESSTOKEN.equal(e.getValue()));}
				else if(e.getKey().equals("code"))
				{q.addConditions(tables.Tables.ACCESS.CODE.equal(e.getValue()));}
				else if(e.getKey().equals("clientid"))
				{q.addConditions(tables.Tables.ACCESS.CLIENTID .equal(e.getValue()));}
				else if(e.getKey().equals("issuedatCode"))
				{q.addConditions(tables.Tables.ACCESS.ISSUEDATCODE.equal(Timestamp.valueOf(e.getValue())));}
				else if(e.getKey().equals("issuedatToken"))
				{q.addConditions(tables.Tables.ACCESS.ISSUEDATTOKEN.equal(Timestamp.valueOf(e.getValue())));}
				else if(e.getKey().equals("valid"))
				{q.addConditions(tables.Tables.ACCESS.VALID.equal(Integer.parseInt(e.getValue())));}
				else if(e.getKey().equals("scope"))
				{q.addConditions(tables.Tables.ACCESS.SCOPE.equal(e.getValue()));}
			}
			//create.insertInto(tables.Tables.CLASS) ; .set(tables.Tables.CLASS.CNAME, attrib.get("cname")).
			//set(tables.Tables.CLASS.DESCRIPTOR, attrib.get("descriptor")).
		//	set(tables.Tables.CLASS.RATING, Double.parseDouble(attrib.get("rating")))
			return q.execute();
			
			}
			catch (Exception e)
			{
				log.warn("vhomeAPI updateAccess Exception "+e.getStackTrace());
			e.printStackTrace();
			return 0;
			
			} 
		
		}


	
	public int addObject(HashMap<String, String> attrib){
		
			
			Result<Record> result = null ;
			int maxOid =-1; 
			Factory create = new Factory(this.db.getConn(), Config.sqldialect);
		try {
	
			result = create.
			select(tables.Tables.CLASS.CID, Factory.max(tables.Tables.OBJECT.OID).as("max")).
			from(tables.Tables.CLASS, tables.Tables.OBJECT). 
			where(tables.Tables.CLASS.CID.equal(tables.Tables.OBJECT.CID)) . 
			and(tables.Tables.CLASS.CID.equal(Integer.parseInt(attrib.get("cid")))). 
			groupBy(tables.Tables.CLASS.CID).
			fetch();
			
			//SelectQuery q = create.selectQuery(tables.Tables.CLASS.CID , MAX(tables.Tables.OBJECT.OID).as("max") );
			//q.addFrom(tables.Tables.CLASS, tables.Tables.OBJECT);
			//q.addConditions(tables.Tables.CLASS.CID.equal(Integer.parseInt(attrib.get("cid"))));
			//result = q.fetch();
			if(result.size()>1) {return 0;}
			if(result.size()==0) {
				result = create.
						select(tables.Tables.CLASS.CID).
						from(tables.Tables.CLASS).
						where(tables.Tables.CLASS.CID.equal(Integer.parseInt(attrib.get("cid")))).fetch();
				if(result.size() == 1)// class ID is valid, but no object yet for this object
					{maxOid = 0;}
				else return 0; // Class ID was invalid, hence return 0
			}
			else {
				for (Record r : result) {
					maxOid = Integer.parseInt(r.getValueAsString("max")); break;
			}
			}
			
		
			
			create = new Factory(this.db.getConn(), Config.sqldialect);
			InsertQuery<ObjectRecord> q = create.insertQuery(tables.Tables.OBJECT);
			InsertQuery<ControlRecord> q1 = create.insertQuery(tables.Tables.CONTROL);
			
			q.addValue(tables.Tables.OBJECT.OID, maxOid + 1);
			q1.addValue(tables.Tables.CONTROL.OID, maxOid + 1);
			
			for (Map.Entry<String, String> e : attrib.entrySet()) {
				if(e.getKey().equals("cid"))
				{
					q1.addValue(tables.Tables.CONTROL.CID, Integer.parseInt(e.getValue()));
					q.addValue(tables.Tables.OBJECT.CID, Integer.parseInt(e.getValue()));
				}
			
				if(e.getKey().equals("descriptor"))
				{q.addValue(tables.Tables.OBJECT.DESCRIPTOR, e.getValue());}
				
				else if(e.getKey().equals("oname"))
				{q.addValue(tables.Tables.OBJECT.ONAME, e.getValue());}		
				
				else if(e.getKey().equals("symbol"))
				{q.addValue(tables.Tables.OBJECT.SYMBOL, e.getValue());}
				
				else if(e.getKey().equals("granularity"))
				{q.addValue(tables.Tables.OBJECT.GRANULARITY,Integer.parseInt(e.getValue()));}
				
			}
		
			q1.execute();
			//create.insertInto(tables.Tables.CLASS) ; .set(tables.Tables.CLASS.CNAME, attrib.get("cname")).
			//set(tables.Tables.CLASS.DESCRIPTOR, attrib.get("descriptor")).
		//	set(tables.Tables.CLASS.RATING, Double.parseDouble(attrib.get("rating")))
			if(q.execute()==0) return 0;
			else return maxOid+1;
		}
		catch (Exception e)
		{
			log.warn("vhomeAPI addObject Exception "+e.getStackTrace());
		e.printStackTrace();
		return 0;
		
		}
			
			
		
		}
	public int addSeries(Long[] attrib1 , Double[] attrib2, HashMap<String, Integer> properties){
		
		int cid = properties.get("cid");
		int oid = properties.get("oid");
		int j ;
		
		try 
		{
			
		Factory create = new Factory(this.db.getConn(), Config.sqldialect);
		 InsertValuesStep<SeriesRecord> q = create.insertInto(tables.Tables.SERIES, tables.Tables.SERIES.CID, 
				tables.Tables.SERIES.OID,tables.Tables.SERIES.TIMESTAMP,tables.Tables.SERIES.VALUE);
		

		for (j=0 ; j <attrib1.length ; j ++)
		{
			q.values(cid, oid, attrib1[j], attrib2[j]);
		}
	
		
		return q.execute();
		
		}
		catch (Exception e)
		{
		log.warn("vhomeAPI addSeries Exception "+e.getStackTrace());
			// TODO: add better return values for duplication cases
		e.printStackTrace();
		return 0;
		
		}
		
	}
}

