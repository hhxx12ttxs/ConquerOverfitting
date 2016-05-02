/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.coperius.gate.accounts;


import com.coperius.gate.abs.CouchWorker;
import com.coperius.gate.abs.SingletonCouchdb;
import com.coperius.gate.base.BankInfoBase;
import com.coperius.gate.exceptions.StatementReqException;
import com.coperius.gate.logging.couchLogger;
import com.coperius.gate.utils.Convert;
import com.coperius.gate.utils.dateUtils;
import com.coperius.gate.utils.gateFunctions;
import com.coperius.gate.utils.gateSettings;
import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.Session;
import com.fourspaces.couchdb.View;
import com.fourspaces.couchdb.ViewResults;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import ru.ftc.interpay.gate.datamodel.Amount;
//import ru.ftc.interpay.gate.datamodel.BankInfo;
import ru.ftc.interpay.gate.datamodel.BankInfo;
import ru.ftc.interpay.gate.datamodel.CashSymbol;
import ru.ftc.interpay.gate.datamodel.Employer;
import ru.ftc.interpay.gate.datamodel.Payment;
import ru.ftc.interpay.gate.datamodel.PaymentClient;
import ru.ftc.interpay.gate.datamodel.Period;
import ru.ftc.interpay.gate.datamodel.Record;
import ru.ftc.interpay.gate.datamodel.Saldo;
import ru.ftc.interpay.gate.datamodel.SaldoInOut;
import ru.ftc.interpay.gate.datamodel.Statement;
import ru.ftc.interpay.gate.utils.Currency;
import ru.ftc.interpay.gate.utils.DateUtils;
import ru.ftc.interpay.gate.utils.XmlBuilder;
import ru.ftc.interpay.gate.utils.XmlUtils;
import org.apache.log4j.Logger;


/**
 *
 * @author coperius
 */
public class AccountJur implements AccountIn{

    SingletonCouchdb ston;
    String iso="0";
    String Account="";
    String BIC ="040396717";
    Integer owner;
    String oper="0";
    BigDecimal rest = new BigDecimal(0);
    private static final Logger logger = Logger.getLogger(AccountJur.class);


    public void init(String acc) throws Exception
    {
      ston = SingletonCouchdb.getInstance();
      Account = acc;
      try {
            if (!initFromABS(acc)) throw new Exception("Невозможно инициализировать счет "+ acc);
        } catch (Exception ex) {
            logger.error(ex);
            throw ex;
        }
    }

     public void init(String account, String bic) throws Exception{
      init(account);
      BIC=bic;
    }

    public AccountEnum getType() {
        return AccountEnum.JURICAL;
    }

    public String getAccount() {
        return Account;
    }

    public String getBIC() {
        return BIC;
    }


 public Integer getOwner()
  {
   Integer result=0;
   Connection con = null;
   CallableStatement stm = null;
   try
   {
    con = DriverManager.getConnection("proxool.abs");
    String table = iso.equals("0")?"account_dbt":"account$_dbt";
    String query = "select Client from " + table + " where Account = ? ";
    stm = con.prepareCall(query);
    stm.setString(1, this.Account);
    stm.execute();
    ResultSet res = stm.executeQuery();
    if (res.next())
    {
     result=res.getInt("Client");
    }
   }
   catch(Exception e)
   {
   }
   finally
   {
    if (stm != null) try {stm.close();} catch (Exception ex) {}
    if (con != null) try {con.close();} catch (Exception ex) {}
   }
   return result;
  }

  public BigDecimal getRest()
  {
   BigDecimal result=new BigDecimal(0);
   Connection con = null;
   CallableStatement stm = null;
   try
   {
    con = DriverManager.getConnection("proxool.abs");
    String table = iso.equals("0")?"account_dbt":"account$_dbt";
    String query = "select R0 from " + table + " where Account = ? ";
    stm = con.prepareCall(query);
    stm.setString(1, this.Account);
    stm.execute();
    ResultSet res = stm.executeQuery();
    if (res.next())
    {
     result=res.getBigDecimal("R0");
               if (result==null) result=new BigDecimal(0);

    }
   }
   catch(Exception e)
   {
   }
   finally
   {
    if (stm != null) try {stm.close();} catch (Exception ex) {}
    if (con != null) try {con.close();} catch (Exception ex) {}
   }
   rest=result;
   return result;
  }


  public String getStatement(Date from, Date to) throws Exception
  {
    try
    {

     //logger.debug("888888888888888888888888888888888888888888888888888888888888");
     //logger.debug("From:"+from);
     //logger.debug("To:"+to);

     boolean useNewSt = false;
     useNewSt = Boolean.parseBoolean(gateSettings.getValue("usenewst"));
     
     String res="";
     Date operDate = dateUtils.string2date(gateSettings.getValue("operdate"));
     //logger.debug("Operdate:"+operDate);
     //if (from.after(to)) throw new StatementReqException("Дата начала периода не может быть больше даты окончания периода");
     //if (from.after(operDate)) throw new StatementReqException("Дата начала периода не может быть больше даты текущего опердня");
     int deep = Integer.parseInt(gateSettings.getValue("statementdeep"));
     if (from.before(dateUtils.dateAddDays(operDate, -deep))) from=dateUtils.dateAddDays(operDate, -deep);//throw new StatementReqException("На данный момент глубина выписки не должна превышать 15 дней.");
     if (to.after(operDate)) to = operDate;
     operDate=from;
     //logger.debug("From new:"+from);
     //logger.debug("To new:"+to);
     //logger.debug("Operdate new:"+operDate);

     System.out.println("RES:"+res);
     while (operDate.before(to))
     {
       res=res.concat(Convert.st2gate(getStatementSt(operDate)).concat(","));
       System.out.println("RES:"+res);
       //logger.debug("Iter operdate:"+operDate);
       operDate = dateUtils.dateAddDays(operDate, 1);
     }
     res=res.concat(Convert.st2gate(getStatementSt(to)));
     System.out.println("RESEND:"+res);

     //   return Convert.st2gate(getStatementSt(from, to));
     return res;
    }
    catch(StatementReqException e)
    {
      couchLogger.error("AccountJur.getStatement", e.getMessage());
      System.out.println("RESERROR1:"+e.getMessage());
      e.printStackTrace();
      throw(e);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      couchLogger.error("AccountJur.getStatement", e.getMessage());
      System.out.println("RESERROR2:"+e.getMessage());
      return "";
    }
   finally {
      //logger.debug("-8888888888888888888888888888888888888888888888888888888888");
   }
  }

    public BigDecimal RestA(String account, java.util.Date rDate) throws Exception
    {
      Connection con = null;
      PreparedStatement stm = null;
      //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      BigDecimal result = new BigDecimal(0);
      String query;
      String field;
      Date operDate;

      try
      {
      operDate = dateUtils.string2date(gateSettings.getValue("operdate"));
      con = DriverManager.getConnection("proxool.abs");
      if (rDate.after(operDate) || ( rDate.compareTo(operDate)==0)  )
      {
      query ="select  * from account_dbt where account = ?";
      stm= con.prepareStatement(query);
      stm.setString(1, account);
      field="R0";
      }
      else
      {
      query ="select top 1 * from restdate_dbt where account = ?";
      query += " and date_carry <= ? ";
      query += " order by date_carry desc ";
      stm= con.prepareStatement(query);
      stm.setString(1, account);
      stm.setDate(2, new java.sql.Date(rDate.getTime()) );
      field="Rest";
      }


      ResultSet res = stm.executeQuery();
      if (res.next()) {
          result = res.getBigDecimal(field);
      }
      res.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();//logger.error(e);
      }
      finally
      {
       if (stm != null) stm.close();
       if (con != null) con.close();
      }
        return result;
    }

    public BigDecimal getRestA(java.util.Date rDate) throws Exception
    {
      Connection con = null;
      PreparedStatement stm = null;
      BigDecimal result = new BigDecimal(0);
      String query;
      String field;
      Date operDate;
      String table= this.iso.equals("0")?"restdate_dbt":"restdat$_dbt";
      String table2= this.iso.equals("0")?"account_dbt":"account$_dbt";
      try
      {
      operDate = dateUtils.string2date(gateSettings.getValue("operdate"));
      con = DriverManager.getConnection("proxool.abs");
      if (rDate.after(operDate) || ( rDate.compareTo(operDate)==0)  )
      {
      query ="select  * from "+table2+" where account = ?";
      stm= con.prepareStatement(query);
      stm.setString(1, this.Account);
      field="R0";
      }
      else
      {
      query ="select top 1 * from "+table+" where account = ?";
      query += " and date_carry <= ? ";
      query += " order by date_carry desc ";
      stm= con.prepareStatement(query);
      stm.setString(1, this.Account);
      stm.setDate(2, new java.sql.Date(rDate.getTime()) );
      field="Rest";
      }
//System.out.println(this.Account+":"+new java.sql.Date(rDate.getTime())+":"+field+":"+query);

      ResultSet res = stm.executeQuery();

      if (res.next()) {
          result = res.getBigDecimal(field);
          if (result==null) result=new BigDecimal(0);
          //System.out.println(field+":"+result);
      }
      res.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();//logger.error(e);
      }
      finally
      {
       if (stm != null) stm.close();
       if (con != null) con.close();
      }
      //System.out.println("result is" + result);
      return result;
    }


    public BigDecimal getRest(Date dt)
    {
     BigDecimal result = new BigDecimal(0);
     Connection con = null;
     CallableStatement stm = null;

     if (gateFunctions.isOperDate(dt)) return getRest();

      String query;
       String table= this.iso.equals("0")?"restdate_dbt":"restdat$_dbt";

     try
      {
      con = DriverManager.getConnection("proxool.abs");
      query = "select top 1 Rest  from " + table;
      query += " where Account = ? and Date_Carry <= ? and Code_Currency= ? ";
      query += " order by Date_Carry desc";
      //logger.info(query);
      stm = con.prepareCall(query);
      stm.setString(1, Account);
      stm.setDate(2, dateUtils.date2sql(dt));
      stm.setString(3, this.iso);
      stm.execute();
      ResultSet res = stm.executeQuery();
      if (res.next()) {
       result = res.getBigDecimal("Rest");
          if (result==null) result=new BigDecimal(0);

      }
      else
      {
       couchLogger.info("AccountJur.getRest","нет информации об остатках по счету "+Account);
      }
      res.close();
      }
      catch (Exception e)
      {
        couchLogger.error("AccountJur.getRest",e.getMessage());
        //throw(e);
      }
      finally
      {
       if (stm != null) try{stm.close();}catch(Exception e){}
       if (con != null) try{con.close();}catch(Exception e){}
      }

      return result;

    }

//public Statement getStatementSt(Date dateFrom, Date dateTo) throws Exception
// {
//  Connection con = null;
//  CallableStatement stm = null;
//  Date operDate;
//  //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//  Statement st = new Statement();
//  try
//  {
//   operDate = dateUtils.string2date(gateSettings.getValue("operdate"));
//   if (dateFrom.after(dateTo)) throw new StatementReqException("Дата начала периода не может быть больше даты окончания периода");
//   if (dateFrom.after(operDate)) throw new StatementReqException("Дата начала периода не может быть больше даты текущего опердня");
//   if (dateTo.after(operDate)) dateTo = operDate;
//   // добавить проверку на закрытие счета
//   Payment pm;
//   double debetTurnOver=0.0d;
//   double creditTurnOver=0.0d;
//   java.util.Date lastOperDate = null;
//   st.setAuxFields(null);
//   st.setBic(""); // тогда получим инфу о банке из настроек
//   st.setOperatingInfo(true);// по дефолту
//   SaldoInOut sIO;
//   Saldo inS, outS;
//   if (this.iso.equals("0"))
//   {
//    inS= new Saldo(dateFrom,
//      ston.RestA(this.Account, dateUtils.dateAddDays(dateFrom,-1)).doubleValue() ,
//      0.0d);
//    outS= new Saldo(dateTo,
//       ston.RestA(this.Account, dateTo).doubleValue(),0.0d);
//   }
//   else
//   {
//    // добавить нацвалюту?
//    inS= new Saldo(dateFrom,
//      ston.RestAV(this.Account, dateUtils.dateAddDays(dateFrom,-1)).doubleValue() ,
//      0.0d);
//    outS= new Saldo(dateTo,ston.RestAV(this.Account, dateTo).doubleValue(),0.0d);
//   }
//   sIO = new SaldoInOut(inS,outS);
//   st.addAllSaldo(sIO);
//   String query;
//   String table="arhdoc_dbt", table2= "document_dbt";
//   //if (this.iso.equals("0") && this.chapter!=1) {table="obdocar_dbt";  table2="obdocum_dbt";}
//   if (!this.iso.equals("0")){table="arhdoc$_dbt";  table2="documnt$_dbt";}
//   //if (!this.iso.equals("0") && this.chapter!=1){table="obdocar$_dbt"; table2="obdocum$_dbt";}
//   con = DriverManager.getConnection("proxool.abs");
//   //if (con==null) logger.info("null con!!!");
//   query = "select * from " + table;
//   query += " where (Real_Receiver = ? ) and (Date_Carry between ? and ?) and Result_Carry<>23 order by Date_Carry ";
//   stm = con.prepareCall(query);
//   stm.setString(1, this.Account);
//   //stm.setString(2, this.Account);
//   stm.setDate(2, dateUtils.date2sql(dateFrom));
//   stm.setDate(3, dateUtils.date2sql(dateTo));
//   stm.execute();
//   ResultSet res = stm.executeQuery();
//   while (res.next())
//   {
//    pm = RS2Payment(res);
//    if (lastOperDate==null) lastOperDate = pm.getDocDate(); //pm.getOperationDate()
//    if (lastOperDate.before(pm.getDocDate())) lastOperDate = pm.getDocDate();
//    if (pm.isDebet()) debetTurnOver+=pm.getAmount();
//    else creditTurnOver+=pm.getAmount();
//    st.addPayment(pm);
//   }
//   res.close();
//   query = "select * from " + table;
//   query += " where (Real_Payer = ? ) and (Date_Carry between ? and ?) and Result_Carry<>23 order by Date_Carry ";
//   stm = con.prepareCall(query);
//   stm.setString(1, this.Account);
//   //stm.setString(2, this.Account);
//   stm.setDate(2, dateUtils.date2sql(dateFrom));
//   stm.setDate(3, dateUtils.date2sql(dateTo));
//   stm.execute();
//   res = stm.executeQuery();
//   while (res.next())
//   {
//    pm = RS2Payment(res);
//    if (lastOperDate==null) lastOperDate = pm.getDocDate(); //pm.getOperationDate()
//    if (lastOperDate.before(pm.getDocDate())) lastOperDate = pm.getDocDate();
//    if (pm.isDebet()) debetTurnOver+=pm.getAmount();
//    else creditTurnOver+=pm.getAmount();
//    st.addPayment(pm);
//   }
//   res.close();
//   if (dateTo.equals(operDate)) // возьмем документы опердня
//   {
//    // поиск и разбор документов опердня
//    query = "select * from " + table2;
//    query += " where (Real_Receiver = ? or Real_Payer = ? ) and (Date_Document between ? and ?)  and Result_Carry<>23 ";
//    stm = con.prepareCall(query);
//    stm.setString(1, this.Account);
//    stm.setString(2, this.Account);
//    stm.setDate(3, dateUtils.date2sql(dateFrom));
//    stm.setDate(4, dateUtils.date2sql(dateTo));
//    stm.execute();
//    res = stm.executeQuery();
//    while (res.next())
//    {
//     pm = RS2Payment(res);
//     if (lastOperDate==null) lastOperDate = pm.getDocDate(); //pm.getOperationDate()
//     if (lastOperDate.before(pm.getDocDate())) lastOperDate = pm.getDocDate();
//     if (pm.isDebet()) debetTurnOver+=pm.getAmount();
//     else creditTurnOver+=pm.getAmount();
//     st.addPayment(pm);
//    }
//    res.close();
//   }
//   st.setCreditTurnOver(creditTurnOver);
//   st.setDebetTurnOver(debetTurnOver);
//   st.setLastOperationDate(lastOperDate);
//  }
//  catch (Exception e)
//  {
//   couchLogger.error("AccountJur.getStatementSt", e.getMessage());
//   throw(e);
//  }
//  finally
//  {
//   if (stm != null) stm.close();
//   if (con != null) con.close();
//  }
//  return st;
// }



public Statement getStatementSt(Date dt) throws Exception
    {
    return getStatementSt(dt,true);
}

public Statement getStatementSt(Date dt,boolean checkSaldo) throws Exception
 {
  System.out.println("!!!!!!!!!!!!!!DT:"+dt.toString());

  Date operDate;
  //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
  Statement st = new Statement();
  try
  {
   operDate = dateUtils.string2date(gateSettings.getValue("operdate"));
   // TODO обработать без ошибок, когда запрашивают будущие выписки
   //if (dt.after(operDate)) throw new StatementReqException("Дата начала периода не может быть больше даты текущего опердня");
   // добавить проверку на закрытие счета
   Payment pm;
   double debetTurnOver=0.0d;
   double creditTurnOver=0.0d;
   java.util.Date lastOperDate = null;
   st.setAuxFields(null);
   st.setBic(""); // тогда получим инфу о банке из настроек
   st.setOperatingInfo(true);// по дефолту
   SaldoInOut sIO;
   Saldo inS, outS;
   if (this.iso.equals("0"))
   {
    inS= new Saldo(dt,
      getRestA(dateUtils.dateAddDays(dt,-1)).doubleValue() ,
      0.0d);
   // logger.debug("DT is "+dt);
    //logger.debug("getRestA is "+getRestA(dt));
    //System.out.println("DT is "+dt);
    //System.out.println("getRestA is "+getRestA(dt));
    outS= new Saldo(dt,
       getRestA(dt).doubleValue(),0.0d);
   }
   else
   {
    // добавить нацвалюту?
    inS= new Saldo(dt,
      getRestA(dateUtils.dateAddDays(dt,-1)).doubleValue() ,
      0.0d);
    outS= new Saldo(dt,getRestA(dt).doubleValue(),0.0d);
   }
   sIO = new SaldoInOut(inS,outS);
   st.addAllSaldo(sIO);

     //Date to = dateUtils.string2date(gateSettings.getValue("operdate"));
     //Date from = dateUtils.dateAddDays(to, -daysago);
     Session s = new Session("localhost",5984);
     Database db = s.getDatabase("gate");
     View v=null;
     if (this.iso.equals("0"))
     {
         v = new View("gate_views/jurdoc_rub");
     }
     else
     {
         v = new View("gate_views/jurdoc_cur");
      }
     v.setStartKey(java.net.URLEncoder.encode("[\""+this.Account+"\",\""+dateUtils.date2string(dt)+"\"]"));
     v.setEndKey(java.net.URLEncoder.encode("[\""+this.Account+"\",\""+dateUtils.date2string(dt)+"\"]"));
     ViewResults vr = db.view(v);
     if (vr==null || vr.isEmpty()) ; /////////////////--------------
     if (vr.getResults()==null || vr.getResults().isEmpty()) ;
     List<Document> ld = vr.getResults();
     for (int i=0;i<ld.size();i++)
     {
    pm = Convert.couchdb2payment(ld.get(i).getId());
    if (lastOperDate==null) lastOperDate = pm.getDocDate(); //pm.getOperationDate()
    if (lastOperDate.before(pm.getDocDate())) lastOperDate = pm.getDocDate();
    if (pm.isDebet()) debetTurnOver+=pm.getAmount();
    else creditTurnOver+=pm.getAmount();
    st.addPayment(pm);

     }

   st.setCreditTurnOver(creditTurnOver);
   st.setDebetTurnOver(debetTurnOver);
   st.setLastOperationDate(lastOperDate);

   if ( (checkSaldo) && (gateFunctions.round2(inS.getSaldo()+creditTurnOver
           -debetTurnOver)!=gateFunctions.round2(outS.getSaldo())))
   {
     System.out.println(gateFunctions.round2(inS.getSaldo()+creditTurnOver-debetTurnOver));
    //перегрузить данные за день

     System.out.println("1:"+inS.getSaldo()+":"+st.getCreditTurnOver()+":"+st.getDebetTurnOver()+":"+outS.getSaldo())  ;
     reload(dt);
     st=this.getStatementSt(dt,false);
     if (gateFunctions.round2(inS.getSaldo()+st.getCreditTurnOver()
             -st.getDebetTurnOver())!=gateFunctions.round2(outS.getSaldo()))
     {
       System.out.println("2:"+inS.getSaldo()+":"+st.getCreditTurnOver()+":"+st.getDebetTurnOver()+":"+outS.getSaldo())  ;
       throw (new Exception("Невозможно устранить расхождение баланса за дату "+ dt.toString()+ " для счета "+this.Account));
     }
   }

  }
  catch (Exception e)
  {
   couchLogger.error("AccountJur.getStatementSt_couchdb", e.getMessage());
   e.printStackTrace();
   throw(e);
  }

  return st;
 }

public void reload(Date dt) throws Exception
{
     Session s = new Session("localhost",5984);
     Database db = s.getDatabase("gate");
     View v = new View("gate_views/jurdoc_rub");
     v.setStartKey(java.net.URLEncoder.encode("[\""+this.Account+"\",\""+dateUtils.date2string(dt)+"\"]"));
     v.setEndKey(java.net.URLEncoder.encode("[\""+this.Account+"\",\""+dateUtils.date2string(dt)+"\"]"));
     ViewResults vr = db.view(v);
     if (vr==null || vr.isEmpty()) ; /////////////////--------------
     if (vr.getResults()==null || vr.getResults().isEmpty()) ;
     List<Document> ld = vr.getResults();
     for (int i=0;i<ld.size();i++)
     {
      db.deleteDocument(db.getDocument(ld.get(i).getId()));
      System.out.println("---"+ld.get(i).getId()+" (Remove)");
     }

  // загрузим платежки за указанный день в шлюз
  Connection con = null;
  CallableStatement stm = null;
  Date operDate;
  //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
  try
  {
   operDate = dateUtils.string2date(gateSettings.getValue("operdate"));
   Payment pm;
   String query;
   String table="arhdoc_dbt", table2= "document_dbt";
   //if (this.iso.equals("0") && this.chapter!=1) {table="obdocar_dbt";  table2="obdocum_dbt";}
   if (!this.iso.equals("0")){table="arhdoc$_dbt";  table2="documnt$_dbt";}
   //if (!this.iso.equals("0") && this.chapter!=1){table="obdocar$_dbt"; table2="obdocum$_dbt";}
   con = DriverManager.getConnection("proxool.abs");
   //if (con==null) logger.info("null con!!!");
   query = "select * from " + table;
   query += " where (Real_Receiver = ? ) and (Date_Carry = ?) and Result_Carry<>23 order by Date_Carry ";
   stm = con.prepareCall(query);
   stm.setString(1, this.Account);
   //stm.setString(2, this.Account);
   stm.setDate(2, dateUtils.date2sql(dt));
   stm.execute();
   ResultSet res = stm.executeQuery();
   while (res.next())
   {
     Convert.payment2Couchdb(Convert.RS2Payment(res, this.Account, this.iso.equals("0")?"rub":"cur"), "jurdoc");
   }
   res.close();
   query = "select * from " + table;
   query += " where (Real_Payer = ? ) and (Date_Carry = ?) and Result_Carry<>23 order by Date_Carry ";
   stm = con.prepareCall(query);
   stm.setString(1, this.Account);
   //stm.setString(2, this.Account);
   stm.setDate(2, dateUtils.date2sql(dt));
   stm.execute();
   res = stm.executeQuery();
   while (res.next())
   {
    Convert.payment2Couchdb(Convert.RS2Payment(res, this.Account, this.iso.equals("0")?"rub":"cur"), "jurdoc");
   }
   res.close();
   if (dt.equals(operDate)) // возьмем документы опердня
   {
    // поиск и разбор документов опердня
    query = "select * from " + table2;
    query += " where (Real_Receiver = ? or Real_Payer = ? ) and (Date_Document = ?)  and Result_Carry<>23 ";
    stm = con.prepareCall(query);
    stm.setString(1, this.Account);
    stm.setString(2, this.Account);
    stm.setDate(3, dateUtils.date2sql(dt));
    stm.execute();
    res = stm.executeQuery();
    while (res.next())
    {
     Convert.payment2Couchdb(Convert.RS2Payment(res, this.Account, this.iso.equals("0")?"rub":"cur"), "jurdoc");
    }
    res.close();
   }
  }
  catch (Exception e)
  {
   throw(e);
  }
  finally
  {
   if (stm != null) stm.close();
   if (con != null) con.close();
  }
     
    
}

public Payment RS2Payment(ResultSet res) throws Exception
{

       Payment pm;
       pm = new Payment();
       try
       {
        BankInfoBase bib = new BankInfoBase();
        //bib.setDataSource(this.absS);
        BankInfo bi = new BankInfo();
        BankInfo bi2 = new BankInfo();
        bi = bib.getBankInfo(res.getInt("Code_Currency")==0?res.getString("MFO_Payer"):res.getString("Code_Payer"));
        bi2 = bib.getBankInfo(res.getInt("Code_Currency")==0?res.getString("MFO_Receiver"):res.getString("Code_Receiver"));
        PaymentClient em = new PaymentClient();
        em.setInn(res.getString("OKPO_Payer")); //
        em.setKpp(res.getString("KPP_Payer"));
        em.setName(res.getString("Payer"));
        em.setAccountNumber(res.getString("Account_Payer"));
        em.setBankAccount(bi.getAccount());
        em.setBankBic(bi.getBic());
        em.setBankBicType(res.getInt("Code_Currency")==0?"ru":"swift"); // ru/swift
        em.setBankCity(bi.getCity());
        //em.setBankCountryCode(res.getString(""));
        em.setBankName(res.getString("Bank_Payer"));

        PaymentClient payee = new PaymentClient();
        payee.setInn(res.getString("OKPO_Receiver"));
        payee.setKpp(res.getString("KPP_Receiver"));
        payee.setName(res.getString("Receiver"));
        payee.setAccountNumber(res.getString("Account_Receiver"));
        payee.setBankAccount(bi2.getAccount());
        payee.setBankBic(bi2.getBic());
        payee.setBankBicType(res.getInt("Code_Currency")==0?"ru":"swift"); // ru/swift
        payee.setBankCity(bi2.getCity());
        //payee.setBankCountryCode(res.getString(""));
        payee.setBankName(res.getString("Bank_Receiver"));


        pm.setAmount(res.getDouble("Sum")); // как поведет себя double с BigDecimal (numeric)?//pm.setAmount(0.0);
        pm.setAmountNat(0.0); // если рублевый, то равен 0, иначе получить из документа покрытия

        //pm.setAuxInfo();//
        pm.setBankNotes("");//Отметки банка.Заполняется банком (через документ pay-document-state и приходит в приложении к выписке – cashless-document)
        //pm.setBankReceiptDate();// поступило в банк-плательщика
        // Заполняется банком (через документ pay-document-state и приходит в приложении к выписке – cashless-document)

        pm.setCashSymbols(getCashSymbols(res));//
        //pm.setChargeOffDate();//WriteOffDate - дата списания
        // Списано со счета плательщика. Указывается дата списания денежных средств со счета плательщика.
        //Заполняется банком (через документ pay-document-state и приходит в приложении к выписке – cashless-document)
        pm.setCurrency(res.getInt("Code_Currency")==0?Currency.getInstance("810"):Currency.getInstance(res.getString("Code_Currency")));//
        pm.setDebet(res.getString("Account_Payer").equals(this.Account)?true:false);
        pm.setDocDate(new java.util.Date(res.getDate("Date_Document").getTime()));
        pm.setDocNum(res.getString("Numb_Document"));
        pm.setGateID("0"); // добавить поиск по таблице шлюза и вернуть айди , если нашел
        pm.setGlobalID("0");
        // или валюта в опердне
        if (  "document_dbt".equals(res.getMetaData().getTableName(1)) == true)
        {
            pm.setOperationDate(dateUtils.string2date( gateSettings.getValue("operdate")));
        }
        else
        {
            pm.setOperationDate(dateUtils.sql2date(res.getDate("Date_Carry")));
        }
        pm.setOperationType(res.getString("Shifr_Oper"));
        pm.setPayDeliveryType(gateFunctions.getDispatchDescr(res.getInt("Dispatch"))); // получить из dispatch_dbt по полю Dispatch
        pm.setPayPurpose(res.getString("Ground"));
        pm.setPayee(payee);//Реквизиты получателя
        pm.setPayer(em);//Реквизиты плательщика
        pm.setQueue(res.getString("Payment"));
        pm.setRelatedPayments(null);// «Связанные платежи». Используется для взимания комиссий.
        pm.setTagName("");
        if (!res.getString("ComposerStatus").equals("")) // здесь проверка на налоговый платеж
        {
          pm.setTaxBudgetCode(res.getString("BudjClassifCode"));
          pm.setTaxDocDate(res.getString("TaxDate"));
          pm.setTaxDocNumber(res.getString("TaxNumber"));
          pm.setTaxPayerType(res.getString("ComposerStatus")); //
          pm.setTaxPeriod(res.getString("TaxPeriod"));
          pm.setTaxPaymentReason(res.getString("TaxGround"));
          pm.setTaxPaymentType(res.getString("TaxPaymentType"));
          pm.setTaxRegionCode(res.getString("OKATO")); // ОКАТО либо 0
        }
        pm.setVbCommission("");
}
catch (Exception e)
{
    couchLogger.error("AccountJur.RS2Payment",e.getMessage());
    throw(e);
}
      return pm;

  }


        public List<CashSymbol> getCashSymbols(ResultSet res) throws Exception
  {
      List<CashSymbol> result = new ArrayList();

      CashSymbol cs = new CashSymbol();
      Amount am = null;
      Connection con = null;
      CallableStatement stm = null;
try {
      if (res.getInt("Symbol_Cach")!=0) // символ есть и он один
      {
       cs.setSymbol(res.getString("Symbol_Cach"));
       cs.setAmount(new Amount(res.getDouble("Sum"),res.getInt("Code_Currency")==0?Currency.getInstance("810"):Currency.getInstance(res.getString("Code_Currency"))));
       result.add(cs);
      }
      else // или символа нет, или их несколько
      {
       Currency cur = res.getInt("Code_Currency")==0?Currency.getInstance("810"):Currency.getInstance(res.getString("Code_Currency"));


      con = DriverManager.getConnection("proxool.abs");
      stm = con.prepareCall("select * from " + (cur.code().equals("810")?"symbcash":"symbcshc") +"_dbt where Kind=1 and iApplicationKind=? and ApplicationKey=?");
      stm.setString(1, res.getString("iApplicationKind"));
      stm.setString(2, res.getString("ApplicationKey"));
      stm.execute();
      ResultSet res1 = stm.executeQuery();
      while (res1.next()) {
       cs = new CashSymbol();
       cs.setSymbol(res1.getString("Symbol"));
       cs.setAmount(new Amount(res1.getDouble("Sum"),cur));
       result.add(cs);
      }
      res1.close();
      }
}
catch(Exception e)
{
  throw(e);
}
finally
{
       if (stm != null) stm.close();
       if (con != null) con.close();

}

      return result;
  }

  public Employer getAbsClient () 
  {
      Employer result = null;
      Connection con = null;
      CallableStatement stm = null;
      CallableStatement stm1 = null;
      try
      {
      con = DriverManager.getConnection("proxool.abs");
      stm = con.prepareCall("select * from client_dbt where Client=?");
      stm.setInt(1, this.getOwner());
      stm.execute();
      ResultSet res = stm.executeQuery();
      if (res.next()) {
       stm1 = con.prepareCall("select * from regdoc_dbt where Client=? and RDKind=100 and IsMain='X'");
       stm1.setInt(1, this.getOwner());
       stm1.execute();
       ResultSet res1 = stm1.executeQuery();
       if (res1.next()) {
        result = new Employer(res.getString("Name_Client"),res1.getString("RegNum"));
        }
       else
       {
        result = new Employer(res.getString("Name_Client"),"");
       }
       res1.close();
      }
      res.close();

      }
      catch (Exception e)
      {
        couchLogger.error("AccountJur.getAbsClient",e.getMessage());
      }
      finally
      {
       if (stm != null) try{stm.close();} catch(Exception e) {};
       if (stm1 != null)try{stm.close();} catch(Exception e) {};
       if (con != null) try{con.close();} catch(Exception e) {};
      }
      return result;
  }


  private boolean initFromABS(String acc) throws Exception
    {
      boolean result = false;
      ResultSet res = null;
      if (!gateFunctions.isOnline()) return result;

      String table ="account";
      if (! acc.substring(5, 8).equals("810")) table+="$";
      Connection con = null;
      CallableStatement stm = null;

      try
      {
      con = DriverManager.getConnection("proxool.abs");
      stm = con.prepareCall("select * from "+table+"_dbt where Account=?");
      stm.setString(1, acc);
      stm.execute();
      res = stm.executeQuery();
      if (res.next())
      {
       iso = res.getString("Code_Currency");
       owner = res.getInt("Client");
       rest = res.getBigDecimal("R0");
       oper = res.getString("Oper");
       result = true;
      }
      res.close();
      }
      catch (Exception e)
      {
       couchLogger.error("AccountJur.initFromABS",e.getMessage());
       e.printStackTrace();
      }
      finally
      {
       if (res != null) {res.close(); res=null;}
       if (stm != null) {stm.close();stm=null;}
       if (con != null) {con.close();con=null;}
      }

      return result;
    }

 public void getState(CouchWorker cw) throws Exception
    {
     Date operationDate = dateUtils.string2date(gateSettings.getValue("operdate"));

     Period period = new Period(operationDate,operationDate);
     Record record = new Record();
     record.setAccountNumber(this.Account);
     record.setPeriod(period);
     record.setBIC(this.BIC);
     record.setOperatingInfo(false);

     Statement st = getStatementSt(operationDate);
     org.w3c.dom.Document accountStateDoc = cw.getRs().getDW().createAccountStateXML(record, st);
     cw.getRs().getRQ().appendRequest(accountStateDoc);

 }

 private void initFromGate(String acc)
 {
     
 }

}

