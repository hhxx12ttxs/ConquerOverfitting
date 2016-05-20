 package com.coperius.gate.abs;
 
import com.coperius.gate.exceptions.PaymentDocException;
import org.logicalcobwebs.proxool.ProxoolException;
import ru.ftc.interpay.gate.abs.*;
 import java.io.File;
 import java.sql.Connection;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import javax.sql.DataSource;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import ru.cft.web.fxgate.abs.AbsDocument;
 import ru.cft.web.fxgate.utils.FileUtils;
 import ru.cft.web.util.alerter.Alerter;
 import ru.ftc.interpay.gate.base.Field;
 import ru.ftc.interpay.gate.base.GateBase;
 import ru.ftc.interpay.gate.base.MSAccessGateBase;
 import ru.ftc.interpay.gate.client.GateClient;
 import ru.ftc.interpay.gate.datamodel.Amount;
 import ru.ftc.interpay.gate.datamodel.AuxField;
 import ru.ftc.interpay.gate.datamodel.BankInfo;
 import ru.ftc.interpay.gate.datamodel.Client;
 import ru.ftc.interpay.gate.datamodel.CurrencyPayment;
 import ru.ftc.interpay.gate.datamodel.DocState;
 import ru.ftc.interpay.gate.datamodel.Employer;
 import ru.ftc.interpay.gate.datamodel.PayDocument;
 import ru.ftc.interpay.gate.datamodel.Payment;
 import ru.ftc.interpay.gate.datamodel.PaymentAccount;
 import ru.ftc.interpay.gate.datamodel.PaymentClient;
 import ru.ftc.interpay.gate.datamodel.PaymentInfo;
 import ru.ftc.interpay.gate.datamodel.PaymentType;
 import ru.ftc.interpay.gate.datamodel.Period;
 import ru.ftc.interpay.gate.datamodel.Record;
 import ru.ftc.interpay.gate.datamodel.Saldo;
 import ru.ftc.interpay.gate.datamodel.SaldoInOut;
 import ru.ftc.interpay.gate.datamodel.Statement;
 import ru.ftc.interpay.gate.documentworker.DocumentWorker;
 import ru.ftc.interpay.gate.documentworker.ReplyException;
 import ru.ftc.interpay.gate.queue.PersistentGateRequestQueue;
 import ru.ftc.interpay.gate.utils.Currency;
 import ru.ftc.interpay.gate.utils.DateConverter;
 import ru.ftc.interpay.gate.utils.DateUtils;
 import ru.ftc.interpay.gate.utils.Utils;
 import ru.ftc.interpay.gate.utils.XmlBuilder;
 import ru.ftc.interpay.gate.utils.XmlUtils;
 import com.coperius.gate.base.RSBankGateBase;
 import org.apache.log4j.Level;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.CallableStatement;
 import java.sql.ResultSet;
 import java.sql.PreparedStatement;
 import ru.ftc.interpay.gate.base.AbstractDAO;
 
 import ru.ftc.interpay.gate.base.BankInfoBase;
 import java.sql.Date;
 import java.util.GregorianCalendar;
 import ru.ftc.interpay.gate.datamodel.*;
 import com.coperius.gate.abs.checks.*;
 import com.coperius.gate.client.PostgresGateClient;

 import ru.ftc.interpay.gate.utils.DateConverter;
 import ru.ftc.interpay.gate.utils.DateUtils;

 import java.util.regex.*;

 import com.coperius.gate.abs.Singleton;
import com.coperius.gate.base.PostgresGateBase;

 import javax.management.*;
 import javax.management.remote.*;

 import java.lang.management.*;
import java.sql.DriverManager;

 import org.logicalcobwebs.proxool.configuration.PropertyConfigurator;

//Log_Tr("Изменилась инфо по счетам" + AcR.Account + " было " + bfAc.R0 + " стало " + AcR.R0 );
//Log_("Изменен статус документа с GlobalID " + RsGetNode("PaymentStatus\\globalID"));
// ошибки при проверке дока


 public class RSBankConnectionFactory extends AbstractABSConnectionFactory
 {
   private String encoding;
   private String toAbsDir;
   private String fromAbsDir;
   private String requestIdFormat;
   private boolean backupABSAnswers;
   private DateFormat statementRequestDateFormat;
   private DateFormat paymentDateFormat;
   private String statementFilePattern = "sta.*$";
   private String documentStateFilePattern = "pds.*$";
   private String fieldSeparator = "\\^";
   private SimpleDateFormat simpleDateFormat;
   private long refreshDocumentStateTimeout;
   private BankInfoBase bankInfo;
   //private Connection con;
   //private RSFunctions rsFun;
   private Singleton rsFun;
   private MBeanServer mbs = null;

   private Logger gLogger= Logger.getLogger("docs");
   
   public RSBankConnectionFactory() throws ProxoolException
   {
     this.encoding = "cp866";
 
     this.backupABSAnswers = false;
     this.statementRequestDateFormat = new SimpleDateFormat("yyyy-MM-dd");
     this.paymentDateFormat = new SimpleDateFormat("yyyyMMdd");
     this.statementFilePattern = "sta.*$";
     this.documentStateFilePattern = "pds.*$";
     this.fieldSeparator = "\\^";
     this.simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
     this.bankInfo = null; //
     //this.rsFun = new RSFunctions();
     this.rsFun = Singleton.INSTANCE;
     logger.setLevel(Level.ALL);
     String curDir = new File(".").getAbsolutePath();
     PropertyConfigurator.configure(curDir + "\\conf\\dbconn.properties");
     
   }

 //   public RSFunctions getRsFun() {
 //       return rsFun;
 //   }

   public Singleton getRsFun() {
        return rsFun;
        //DefaultMBeanServerInterceptor m = new DefaultMBeanServerInterceptor();

    }

   

//   protected RSBankGateBase getRSBankGateBase() {
//     return (RSBankGateBase)getGateBase();
//
//   }


   protected PostgresGateBase getRSBankGateBase() {
     return (PostgresGateBase)getGateBase();

   }

//   protected PostgresGateClient getRSGateBase()
//   {
//     return (PostgresGateClient)this.getGateClient();
//   }

   public long getRefreshDocumentStateTimeout() {
     return this.refreshDocumentStateTimeout;
   }
 
   public void setRefreshDocumentStateTimeout(long refreshDocumentStateTimeout) {
     this.refreshDocumentStateTimeout = refreshDocumentStateTimeout;
   }
 
   public void setBackupABSAnswers(boolean backupABSAnswers) {
     this.backupABSAnswers = backupABSAnswers;
   }
 
   public String getToAbsDir() {
     return this.toAbsDir;
   }
 
   public void setToAbsDir(String toAbsDir) {
     this.toAbsDir = toAbsDir;
     File dir = new File(toAbsDir);
     dir.mkdirs();
   }
 
   public String getFromAbsDir() {
     return this.fromAbsDir;
   }
 
   public void setFromAbsDir(String fromAbsDir) {
     this.fromAbsDir = fromAbsDir;
     File dir = new File(fromAbsDir);
     dir.mkdirs();
   }
 
   public String getEncoding() {
     return this.encoding;
   }
 
   public void setEncoding(String encoding) {
     this.encoding = encoding;
   }
 
   public String getRequestIdFormat() {
     return this.requestIdFormat;
   }
 
   public void setRequestIdFormat(String requestIdFormat) {
     this.requestIdFormat = requestIdFormat;
   }
 
   protected void initialize() throws Exception
   {
     if (this.logger.isDebugEnabled()) {
       this.logger.debug("Сохранять копии ответов абс: " + this.backupABSAnswers);
       this.logger.debug("Каталог входящих в АБС документов: " + this.toAbsDir);
       this.logger.debug("Каталог исходящих из АБС документов: " + this.fromAbsDir);
       this.logger.debug("Запросы абс обрабатываются в кодировке: " + this.encoding);
       this.logger.debug("Максимальное количество документов в одной выписке: " + this.maxPaymentsInStatement);
     }

     gLogger.info("Start gate Logger");
     

     this.rsFun.init();
////////////////////////////////////////////////////////////////////
//     con = DriverManager.getConnection("proxool.gate");         //
////////////////////////////////////////////////////////////////////

     //this.rsFun.setAbsS(this.getGateBase().getDataSource());
     //this.rsFun.setGateS(this.getRSGateBase().getDataSource()); //this.getGateBase().getDataS()
     this.rsFun.setcF(this);
     this.rsFun.setDocW((RSDocumentWorker)this.documentWorker);
     this.rsFun.setrQueue(requestQueue);
     this.rsFun.getCalendar();
     this.logger.debug(this.rsFun.getOperDate());
     
     //this.getRSBankGateBase().setDataS(this.getGateBase().getDataSource());
     //this.getRSBankGateBase().setDataSource(this.getRSGateBase().getDataSource());

//----------создаем mbean для управления----------------------------------------
     // Получить экземпляр MBeanServer
//     mbs = ManagementFactory.getPlatformMBeanServer();
//     // Создаем наш MBean
//     ObjectName beanName = null;
//     try {
//     // И регистрируем его на платформе MBeanServer
//         beanName = new ObjectName("RSBankConnectionFactory:name=rsfun");
//         mbs.registerMBean(rsFun, beanName);
//        } catch(Exception e) {
//            e.printStackTrace();
//        }

//------------------------------------------------------------------------------
     // здесь можно будет наделать сервисов
     if (this.refreshDocumentStateTimeout <= 0L) return; 
     startABSSync();
     startDocumentToABSRefreshProcess();
   }

      private void startDocumentToABSRefreshProcess()
   {
     Runnable documentToABSRefresher = new Runnable() {
       public void run() {
         RSBankConnectionFactory.this.logger.debug("Запущено задание отправки документов в АБС. Период обновления: " + RSBankConnectionFactory.this.refreshDocumentStateTimeout);
         while (!Thread.currentThread().isInterrupted())
           try {
             synchronized (this) {
               Thread.sleep(RSBankConnectionFactory.this.refreshDocumentStateTimeout);
               RSBankConnectionFactory.this.sendPaymentsToABS();
             }
           } catch (Exception e) {
             RSBankConnectionFactory.this.logger.debug("Выполнение синхронизации документов с АБС прервано ошибкой: ", e);
           }
       }
     };
     Thread refresher = new Thread(documentToABSRefresher);
     refresher.setDaemon(true);
     refresher.start();
   }


   private void startABSSync()
   {
     Runnable documentStatusRefresher = new Runnable() {
       public void run() {
         RSBankConnectionFactory.this.logger.debug("Запущено задание обновления статусов документов. Период обновления: " + RSBankConnectionFactory.this.refreshDocumentStateTimeout);
         while (!Thread.currentThread().isInterrupted())
           try {
             synchronized (this) {
               Thread.sleep(RSBankConnectionFactory.this.refreshDocumentStateTimeout);
               RSBankConnectionFactory.this.processABSSync();
             }
           } catch (Exception e) {
             RSBankConnectionFactory.this.logger.debug("Выполнение синхронизации статусов документов прервано ошибкой: ", e);
           }
       }
     };
     Thread refresher = new Thread(documentStatusRefresher);
     refresher.setDaemon(true);
     refresher.start();
   }
 
   protected void processABSSync()
   {
     this.logger.info("Проверка изменения статусов документов в АБС ...");

     try {
        logger.debug("try");
        java.util.Date d1,d2;
        ru.ftc.interpay.gate.utils.DateConverter dc = new DateConverter("yyyy-MM-dd");
        d1=dc.parse(this.rsFun.getOperDate());
        d2=ru.ftc.interpay.gate.utils.DateUtils.add(d1, -4); // срок синхронизации назад от текущего опердня
        
        long t = System.currentTimeMillis();
        //this.rsFun.syncDocs(d2, d1);
        //t = System.currentTimeMillis() - t;
        //logger.debug("Time1: " + t/1000);
        //t = System.currentTimeMillis();
        this.rsFun.syncDocs2(d2, d1);
        t = System.currentTimeMillis() - t;
        logger.debug("Time2: " + t/1000);
        logger.debug("syncDocs OK");
        t = System.currentTimeMillis();
        this.rsFun.SyncRests2(d2, d1);
        t = System.currentTimeMillis() - t;
        logger.debug("Time_rests: " + t/1000);
        logger.debug("SyncRests OK");
        this.checkABSDocumentsState();
        logger.debug("checkState OK");

     }
     catch (Throwable e) {
       String subj = "Ошибка при обновлении статусов документов";
       this.logger.error(subj, e);
       if (e instanceof Exception)
         this.alerter.alert(subj, "Исключение при обновлении статусов документов", e);
       else {
         this.alerter.alert(subj, "Исключение при обновлении статусов документов: " + e.getMessage());
       }
     }
     this.logger.debug("Завершено создание запросов на статусы документов");
   }
 

 
   protected PaymentInfo getPaymentState(String globalID, PaymentType paymentType)
     throws Exception
   {
     this.logger.debug("Получение статуса документа globalID=" + globalID);
     List fields = new ArrayList(1);
     fields.add(new Field("globalID", globalID));
 
     PaymentInfo info = new PaymentInfo();
     info = (paymentType != null) ? getRSBankGateBase().getPaymentInfo(fields, paymentType) : getRSBankGateBase().getPaymentInfo(fields, PaymentType.PAYMENT);
 
     if (info == null) info = getRSBankGateBase().getPaymentInfo(fields, PaymentType.CURRENCY_PAYMENT);
     if (info == null) info = getRSBankGateBase().getPaymentInfo(fields, PaymentType.CURRENCY_EXCHANGE_ORDER);
     if (info == null) info = getRSBankGateBase().getPaymentInfo(fields, PaymentType.FORCED_CURRENCY_SALE_ORDER);

     return info;
   }
 
   protected void createRegularStatements() throws Exception {
     this.logger.info("Плановое создание выписок по времени...");
     BankInfo bankInfo = this.documentWorker.getDefaultBankInfo();

/*     List<PaymentAccount> accounts = getGateClient().getRecordAccounts();
     if ((accounts == null) || (accounts.size() == 0)) return;
     java.util.Date now = DateUtils.today();
     for (PaymentAccount account : accounts)
       try {
         this.logger.debug("Плановое создание выписки по счету " + account.getAccountNumber());
         Record record = new Record();
         record.accountNumber = account.getAccountNumber();
         record.bankBic = bankInfo.getBic();
         record.bankBic = account.getBankBic();
         record.period = new Period(now, now);
         record.isAuto = true;
         XmlBuilder xb = new XmlBuilder(XmlUtils.createDocument());
         xb.tag("statement-request");
         xb.attr("document_type", "a");
         xb.attr("date-from", DateUtils.W3C.format(record.period.getFromDate()));
         xb.attr("date-to", DateUtils.W3C.format(record.period.getToDate()));
         xb.tag("account");
         xb.attr("number", account.getAccountNumber());
         xb.tag("bank");
         xb.attr("name", bankInfo.getName());
         xb.attr("bic", bankInfo.getBic());
         xb.attr("account-number", bankInfo.getAccount());
         xb.end();
         xb.end();
         xb.end();
         record.root = xb.getDocument().getDocumentElement();
         processStatementRequest(account.getClient(), record);
       } catch (Exception e) {
         this.logger.warn("Ошибка при плановом создании выписки по счету " + account.getAccountNumber(), e);
       }
  */ }

  /**
   * обработка запроса выписки из Системы
   * @param client
   * @param record
   * @throws Exception
   */
   protected void processStatementRequest(Client client, Record record)
     throws Exception
   {
     if (record.requestId == 0L) record.requestId = getGateBase().getNextRequestID();
     this.logger.info("Создание запроса на выписку для абс, клиент [" + client.name + ", " + record.period + ", " + record.getAccountNumber() + "]");

     // заменить на создание запроса в базе шлюза, обрабатывать его потом отдельным потоком
     this.createStatement(record.accountNumber, record.period.getFromDate(), record.period.getToDate());

     this.logger.debug("Запрос на выписку по счету " + record.accountNumber + " создан успешно [" + "]");

   }

 /**
  * Обработка запроса статуса документа от Системы
  * @param record
  * @throws java.lang.Exception
  */
   private void processDocumentStatusRequestToAbsFormat(Record record)
     throws Exception
   {
 /*    String requestName = "dsr" + Utils.formatLong(record.requestId, this.requestIdFormat);
 
     String fromDate = this.statementRequestDateFormat.format(record.period.getFromDate());
     String toDate = this.statementRequestDateFormat.format(record.period.getToDate());
 
     String body = "003/00000001/" + record.accountNumber + "/" + fromDate + "/" + toDate + "/::\r\n";
 
     File result = writeAbsFile(requestName, body);
     this.logger.debug("Запрос на статусы документов по счету " + record.accountNumber + " создан успешно [" + result.getName() + "]");
  */ }


   private void sendPaymentsToABS() throws Exception
   {
     /*
      *  1. сканируем базу доков шлюза на наличие ПП со статусом помещен в шлюз
      *  2. перебираем в цикле эти доки
      *  3. для каждого выполняем проверки
      *  4. добавляем в АБС
      *  5. меняем статус док в шлюзе
      *  6. отправляем Системе сообщение о смене статуса
      */  
      logger.debug("sendPaymentsToABS()");
      Connection con = null;
      CallableStatement stm = null;
      ResultSet res = null;
      try
      {
      con = DriverManager.getConnection("proxool.gate");
      String query = "SELECT " +
	" payments.amount,payments.\"appendDate\",payments.\"gateID\",payments.\"globalId\"," +
	" payments.id,payments.\"ipayType\",payments.note,payments.\"operationType\"," +
	" payments.\"payDocumentDate\",payments.\"payDocumentNumber\",payments.\"payeeAccountNumber\"," +
	" payments.\"payeeBankAccount\",payments.\"payeeBankCity\",payments.\"payeeBankName\"," +
	" payments.\"payeeBIC\",payments.\"payeeINN\",payments.\"payeeKPP\",payments.\"payeeName\"," +
	" payments.\"payerAccountNumber\",payments.\"payerBankAccount\",payments.\"payerBankCity\"," +
	" payments.\"payerBankName\",payments.\"payerBIC\",payments.\"payerINN\",payments.\"payerKPP\"," +
	" payments.\"payerName\",payments.\"payPurpose\",payments.queue,payments.\"requestID\"," +
	" payments.state,payments.\"strPerSign\",payments.\"taxBudgetCode\",payments.\"taxDocDate\"," +
	" payments.\"taxDocNumber\",payments.\"taxPayerType\",payments.\"taxPaymentReason\"," +
	" payments.\"taxPaymentType\",payments.\"taxPeriod\",payments.\"taxRegionCode\",payments.\"sgnSource\"" +
        " FROM payments ,\"f_gdocs\" " +
        " WHERE " +
        " payments.\"globalId\" = \"f_gdocs\".\"global_id\" AND \"f_gdocs\".\"type\" = 2 AND \"f_gdocs\".status = 6";

      logger.debug(query);
      
      stm = con.prepareCall(query);
      stm.execute();
      res = stm.executeQuery();

      while (res.next()) {
        /**
         * 3. вызов проверок
         * если не прошел, то смена статуса у дока в шлюзе и отправка сообщения системе
         * 4. добавить в АБС
         * для совместимости со старой версией учитывать...
         * rRec.UserField2 = "FakturaGID:" + GlobalID;
         * rRec.UserField3 = Sign1; //3 используется также для других целей!!!
         *
         */
        
        logger.info("Обработка документа...");
        logger.info("----------------------------------------------------------");
        logger.info("gateID:" + res.getObject("gateID"));
        try
        {
        PaymentChecks pc = new PaymentChecks();
        pc.setOurBank(this.documentWorker.getDefaultBankInfo());
        pc.doPaymentChecks(res);
        logger.info("Отправляем в АБС");
        this.sendPaymentToABS(res);
        PaymentInfo pi = new PaymentInfo(PaymentType.PAYMENT, res.getString("globalID") , DocState.RECEIVED, null);
        pi.gateID = res.getString("gateID");
        logger.info("меняем статус в шлюзе");
        this.rsFun.changePaymentState(pi, false);
        //logger.info("создаем документ статуса");
        //Document doc = this.documentWorker.createPayDocumentStateXML(pi);
        //logger.warn(this.rsFun.Dom2String(doc));
        //logger.info("ставим статус в очередь");
        //this.requestQueue.appendRequest(doc);
        logger.info("закончили");
        }
        catch (PaymentDocException e1)
        {
          logger.info("Ошибки в обработке платежки");
          PaymentInfo pi1 = new PaymentInfo(PaymentType.PAYMENT, res.getString("globalID") , DocState.RETURNED, e1.getMessage());
          pi1.gateID = res.getString("gateID");
          this.rsFun.changePaymentState(pi1, false);
          Document doc1 = this.documentWorker.createPayDocumentStateXML(pi1);
          logger.warn(this.rsFun.Dom2String(doc1));
          this.requestQueue.appendRequest(doc1);
          //sendDocState(pi1);
        }
      
      }
       res.close();
     }

      catch (Exception e)
      {
        logger.info("sendPaymentsToABS error");
        throw(e);
      }
      finally
      {
       if (res != null) res.close();
       if (stm != null) stm.close();
       if (con != null) con.close();
      }


   }


/*
 * непосредственно отправка платежки в postdoc (пока только рублевая)
 *    
 */
private void sendPaymentToABS(ResultSet res) throws Exception
{
Connection con = null;
Connection gate = null;
CallableStatement stm = null;
CallableStatement stm2 = null;
String appkey="";
logger.debug("try sendPaymentsToABS");
try
{
 con = DriverManager.getConnection("proxool.abs");
 gate = DriverManager.getConnection("proxool.gate");
 con.setAutoCommit(false);
 gate.setAutoCommit(false);


 String query = "INSERT INTO  " + " postdoc_dbt " +
             "(" +
             "Numb_Document,Date_Document,Shifr_Oper,\"Sum\",Ground," +
             "Dispatch,Payment,Payer,OKPO_Payer,KPP_Payer,Account_Payer," +
             "Real_Payer,Oper,Code_Currency,MFO_Receiver,MFO_Payer," +
             "Account_Receiver,Receiver,OKPO_Receiver,KPP_Receiver," +
             "iApplicationKind,ApplicationKey,Kind_Oper,Result_Carry," +
             "Number_Pack,Symbol_Cach,SymbNotBal,Origin," +
             "Bank_Payer,Bank_Receiver,Real_Receiver,CorSchemKred," +
             "CorAcc_Receiver,Date_Value," +
             "ComposerStatus,BudjClassifCode,OKATO,TaxGround," +
             "TaxPeriod,TaxNumber,TaxDate,TaxpaymentType," +
             "UserField2,UserField3" +
             ")" +
             " Values (" +
             "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?" +
             ")" ;


 stm = con.prepareCall(query);


 // * Numb_Document payDocumentNumber
 stm.setObject(1, res.getObject("payDocumentNumber"));
 // * Date_Document payDocumentDate
 stm.setObject(2, res.getObject("payDocumentDate"));
 // * Shifr_Oper operationType
 stm.setObject(3, res.getObject("operationType"));
 // * Sum amount
 stm.setObject(4, res.getDouble("amount")/100);
 // * _Ground payPurpose
 String ground="";
 ground=(String)res.getObject("payPurpose");
 ground = ground.replaceAll("\r", " ");
 ground = ground.replaceAll("\n", " ");
 stm.setObject(5, ground);
 // * Dispatch DeliveryType // convert
 stm.setObject(6, this.rsFun.getDispatchNum("электронно")); // приходят только электронно, шлюз это поле в таблицу не отправляет
 // * Payment queue
 stm.setObject(7, res.getObject("queue"));
 // * sTMP /Payer/name
 // * rRec.Payer = Substr(sTMP,1,160);
 stm.setObject(8, res.getObject("payerName"));
 // * OKPO_Payer /Payer/INN
 stm.setObject(9, res.getObject("payerINN"));
 // * KPP_Payer /Payer/kpp
 stm.setObject(10, res.getObject("payerKPP"));
 // * sTMP /Payer/Account/accountNumber
 // *  rRec.Account_Payer = sTMP;
 stm.setObject(11, res.getObject("payerAccountNumber"));
 // *  rRec.Real_Payer = sTMP;
 stm.setObject(12, res.getObject("payerAccountNumber"));
 // *  rRec.Oper = _Oper;
 stm.setObject(13, "9997"); // сделать из настроек
 // *  rRec.Code_Currency = Val;
 if (res.getString("payerAccountNumber").substring(5, 8).equals("810")) stm.setObject(14, "0");
 else stm.setObject(14,res.getString("payerAccountNumber").substring(5, 8)) ;
     
 // * MFO_Receiver /Payee/Account/BIC
 stm.setObject(15, res.getObject("payeeBIC"));
 if (this.documentWorker.getDefaultBankInfo().getBic().equals(res.getString("payeeBIC")))
   stm.setString(16, "");
 stm.setString(17, res.getString("payeeAccountNumber"));
 // *   If (rRec.MFO_Receiver == {MFO_Bank})
 // *    rRec.MFO_Payer = "";
 // *    If (Счет_Проверен(sTMP, sError, False))
 // *       rRec.Account_Receiver = sTMP;
 // *    Else
 // *       sError= "Неверный номер счета получателя ";
 // *    End;
 // * Else
 // *    If (БИК_Проверен(rRec.MFO_Receiver, sError, sBName))
 // *        If (RsDomError == 1 ) sError = "ошибка чтения тега"; End;
 // *        rRec.Account_Receiver = sTMP;
 // *    Else
 // *      sError = "Неверно указан БИК банка получателя ";
 // *    End;
 // * End;
 // *
 // * Receiver /Payee/name
 stm.setObject(18, res.getObject("payeeName"));
 // * OKPO_Receiver /Payee/INN
 stm.setObject(19, res.getObject("payeeINN"));
 // * KPP_Receiver /Payee/kpp
 stm.setObject(20, res.getObject("payeeKPP"));
 // * iApplicationKind 2
 stm.setObject(21, "2");
 // * ApplicationKey GetApplicationKey()
 appkey=this.rsFun.getApplicationKey();
 stm.setObject(22, appkey);
 // * _Ground Убираем перевод строк и возврат каретки
 //    сделать это выше. отсеивать длиннее 210 символов. возвращать ошибку
 //    и отклонять платежку (узнать где и как в АБС используется дополнительное основание)
 /// *   If (rRec.Shifr_Oper == "01")
 // *   rRec.Kind_Oper = " 1";
 // * Elif (rRec.Shifr_Oper == "09")
 // *   rRec.Kind_Oper = " 9";
 if (res.getString("operationType").equals("01")) stm.setObject(23, " 1");
 if (res.getString("operationType").equals("09")) stm.setObject(23, " 9");
 // *   rRec.Result_Carry = cResCarry;
 stm.setObject(24, 9); // брать из настроек
 // * rRec.Number_Pack = cNumPack;
 stm.setObject(25,444); // брать из настроек
 // * rRec.Symbol_Cach = "  0";
 stm.setObject(26, "  0");
 // * rRec.SymbNotBal  = "  0";
 stm.setObject(27, "0");
 // * rRec.Origin = "f";
 stm.setObject(28, "f");
 if (this.documentWorker.getDefaultBankInfo().getBic().equals(res.getString("payeeBIC")))
  {
   stm.setObject(16, "");
   stm.setObject(15, "");
   stm.setObject(29, this.documentWorker.getDefaultBankInfo().getName());
   stm.setObject(30, this.documentWorker.getDefaultBankInfo().getName());
   stm.setObject(31, res.getObject("payeeAccountNumber"));
   stm.setObject(6, 10); // сделать настройкой
   // *   If (rRec.MFO_Receiver == {MFO_Bank}) // получатель - наш клиент
   //    rRec.MFO_Payer = "";
   //    rRec.MFO_Receiver = "";
   //    rRec.Bank_Payer = {Name_Bank};
   //    rRec.Bank_Receiver = {Name_Bank};
   //    rRec.Real_Receiver = rRec.Account_Receiver;
   //    rRec.Dispatch = 10;// поставить свое значение кода Отправки для внутреннего платежа
   
  }
 else
  {
   //  Else  // получатель - внешний клиент
   //    rRec.Bank_Payer = {Name_Bank};
   stm.setObject(29, this.documentWorker.getDefaultBankInfo().getName());
   //      rRec.Bank_Receiver = Trim(BDprt.Name_Depart);
   stm.setObject(30, res.getString("payeeBankName")); // проверить получили мы его из своего справочника или
   //// 03.03.2007 Шапиро Е.И.      rRec.CorSchemKred = BDprt.Schem;
   //      rRec.CorSchemKred = 1;
   stm.setObject(32, 1);
   //
   //      rRec.CorAcc_Receiver = BDprt.Corr_Acc;
   stm.setObject(33, res.getString("payeeBankAccount"));
   //      Rewind(Schem);
   //      Schem.Code_Currency = 0; нет
   //
   //// 03/03/2007 Шапиро Е.И.      Schem.Number = BDprt.Schem;
   //      Schem.Number = 1;
   //
   //      If (GetEQ(Schem))
   //        rRec.Real_Receiver = Schem.FKreditCAD;
   stm.setObject(31, this.rsFun.getCorSchemeFCredit(1,0)); // !!!!!

   //      End;
   //    End; // GetGE
   ///** 08.03.2007 Шапиро Е.И.
   ///*******Для заполнения поля отправки в зависимости от региона***********
   ///* заместо 04630000 и 046399999 надо поставить свое значение границ региона
   ///* и поставить свое значение для данного диапозона

   if ((Long.parseLong(res.getString("payeeBIC"))>=46300000) && (Long.parseLong(res.getString("PayeeBIC"))<= 46399999))
    {
      stm.setObject(6, 4);
    }
   else stm.setObject(6, 5);
     
  }
 // * If (rRec.Date_Value == Date(0,0,0))
 //    rRec.Date_Value = {curdate};
 //  End;
 stm.setObject(34, DateUtils.sqlDate(new java.util.Date()));
 // *
 // * Sign1 = "ЭЦП: "+ ФИО_подписи (1 + 2)
 //  TaxInfo();
 // * ComposerStatus payerType
 stm.setObject(35, res.getObject("taxPayerType"));
 // * BudjClassifCode budgetCode
 stm.setObject(36, res.getObject("taxBudgetCode"));
 // * OKATO regionCode
 stm.setObject(37, res.getObject("taxRegionCode"));
 // * TaxGround paymentReason
 stm.setObject(38, res.getObject("taxPaymentReason"));
 // * TaxPeriod taxPeriod
 stm.setObject(39, res.getObject("taxPeriod"));
 // * TaxNumber docNumber
 stm.setObject(40, res.getObject("taxDocNumber"));
 // * TaxDate docDate
 stm.setObject(41, res.getObject("taxDocDate"));
 // * TaxpaymentType paymentType
 stm.setObject(42, res.getObject("taxPaymentType"));
 //rRec.UserField2 = "FakturaGID:" + GlobalID
 stm.setObject(43, "FakturaGID:" + res.getObject("globalID"));
 //rRec.UserField3 = Sign1;
logger.debug("000000000000000000000000000");
 String sign="";
 Pattern p = Pattern.compile(" O=([^,]*),");
 Matcher matcher = p.matcher(res.getString("sgnSource"));
 boolean isFirst = true;
 while (matcher.find())
 {
  logger.debug("AAA:"+matcher.group(0));
  if (isFirst) isFirst=false;
  else sign=sign+matcher.group(1)+"\n";
 }
 
 stm.setObject(44, sign); // изменить на получение правильной подписи
 //stm.setObject(44, res.getObject("sgnSource")); // изменить на получение правильной подписи
 
 if (stm.executeUpdate()==0)
  {
   logger.info("вставка не удалась...");
   //return;
   // вставка не удалась... возможно сделать перехват ошибки
  }
 else
 {
  query="update payments set appkey=? where id=?";
  stm2=gate.prepareCall(query);     
  stm2.setString(1, appkey);
  stm2.setLong(2, res.getLong("id"));
  if (stm2.executeUpdate()==0)
  {
    logger.debug("Не удалось добавить значение ключа АБС.Будет произведен откат.");
    con.rollback();
    gate.rollback();
  }
 }


con.commit();
gate.commit();


 }
 catch (Exception e)
  {
   logger.debug(e);
   con.rollback();
   gate.rollback();
   throw(e);
  }
 finally
  {
   if (stm != null) stm.close();
   if (con != null) con.close();
   if (stm2 != null) stm2.close();
   if (gate != null) gate.close();
   logger.debug("end paymetsoABS");
  }
}



   private File writeAbsFile(String requestName, String body)
     throws Exception
   {
     File tempDir = new File(this.toAbsDir, "temp");
     if (!tempDir.exists()) tempDir.mkdirs();
     String reqestTempName = requestName + ".tmp";
     File resultTemp = FileUtils.createFile(tempDir.getPath(), reqestTempName, body, this.encoding);
     File result = new File(this.toAbsDir, requestName);
     if (result.exists()) result.delete();
     resultTemp.renameTo(result);
     return result;
   }

   /**
    * обработка сообщений из абс
    * поменяем на получение данных из таблицы АБС
    * @throws Exception
    */
   protected void handleABSFiles()
     throws Exception
   {
     this.logger.info("Обработка файлов от АБС в каталоге " + this.fromAbsDir);
 
         try {
           handleABSFile();
         } catch (Exception e) {
           this.logger.error("Ошибка обработки файла АБС ", e);
           this.alerter.alert("Ошибка обработки файла АБС", "При обработке файла  возникла ошибка!\n" + "Файл сохранен в .\n", e);
         }
       }
   
 
   private void handleABSFile() throws Exception
   {
  //   String filename = file.getName();
  //   if (filename.matches("sta.*$")) { handleABSStatement(file); } else {
//       if (!filename.matches("pds.*$")) return; handleABSDocumentState(file);
   //  }
   }
 
  /**
   * Проверка изменения статусов документов в АБС
   * @throws java.lang.Exception
   */ 
   private void checkABSDocumentsState() throws Exception
   {
     // запустить отдельным сервисом проверку статусов

 //    public void syncStatuses()  throws Exception
 // {
  /**
   *  1. Выбрать всех работающих клиентов
   *  2. Выбрать все действующие счета
   *  3. Сделать выборку из таблиц всех доков и доков клиентов(из Системы)
   * по разности статусов документов (или лучше за период???)
   *  4. По полученной выборке сформировать уведомления клиентам
   */
    Connection con = null;
    PreparedStatement stm = null;
    ResultSet res = null;

    String query;
    try
    {
      con =DriverManager.getConnection("proxool.gate");
      query="SELECT " +
              "public.payments.id," +
              "payments_abs.\"gate_state\"," +
              "payments.\"globalId\"," +
              "public.payments_abs.id," +
              "\"gateID\"" +
              "FROM" +
              "  public.payments_abs" +
              "  RIGHT OUTER JOIN public.payments ON (public.payments_abs.\"appKey\" = public.payments.appkey)" +
              "WHERE" +
              "  (payments_abs.\"appKind\" = 2 and payments_abs.gate_state<>payments.state) OR" +
              "  (payments_abs.\"appKind\" IS NULL and payments.state<>'canceled' and payments.state<>'delivered'" +
              "    and payments.last_change< (select max(last_change) from payments_abs)) " ;
      
      stm= con.prepareStatement(query);
      res = stm.executeQuery();
      while (res.next())
      {
        PaymentInfo pi = new PaymentInfo(PaymentType.PAYMENT, res.getString("globalId") , null, null);
        if (res.getObject(2)==null) pi.state = DocState.CANCELED;
        else    pi.state = DocState.getInstance(res.getString(2));
        pi.gateID = res.getString("gateID");
        logger.info("меняем статус в шлюзе");
        this.changeDocumentStatus(pi);
      }
    }
    catch(Exception e)
    {
       throw(e);
    }
    finally
    {
        if (res!=null) res.close();
        if (stm!=null) stm.close();
        if (con!=null) con.close();
    }
  /*
SELECT
  public.payments.id,
  public.payments_abs.id
FROM
  public.payments_abs
  RIGHT OUTER JOIN public.payments ON (public.payments_abs."appKey" = public.payments.appkey)
WHERE
  (payments_abs."appKind" = 2 and payments_abs.gate_state<>payments.state) OR
  payments_abs."appKind" IS NULL
  */


   }
   
   
   /**
    * Обработка статуса документа в АБС
    * @param res
    * @throws java.lang.Exception
    */
    private void handleABSDocumentState(ResultSet res)
     throws Exception
   {
 
     this.logger.info("Обработка статуса документа из АБС");
 
       PaymentInfo info = new PaymentInfo();
       // получим глобалАйДи (сделать отдельным полем??? в текущей версии в юзеринфо3?)
       info.globalID = res.getString("iApplicationKind")+res.getString("ApplicationKey");//response[0];
       info.operDate = this.simpleDateFormat.parse(res.getString("operDate"));
       info.state = DocState.valueOf("RECEIVED");
       info.comment = "";//response[3];
       info.bankNotes = "";//response[4];

       // есть ли в базе шлюза такой документ
       PaymentInfo paymentInfo = getPaymentState(info.globalID, null);
       if (paymentInfo == null) {
         this.logger.warn("Документ [" + info.globalID + "] не найден в БД шлюза");
       }
       else if (paymentInfo.state != info.state) {
         info.type = paymentInfo.type;
         changeDocumentStatus(info);
       } else {
         this.logger.info("Документ [" + info.globalID + "] уже находится в статусе " + info.state + ". Отправка статуса на сервер не требуется.");
       }
     
   }
 /**
  * Изменение статуса документа в базе шлюза и отправка сообщения об этом Системе
  * @param info
  * @throws java.lang.Exception
  */
   protected void changeDocumentStatus(PaymentInfo info)
     throws Exception
   {
     try
     {
     this.rsFun.changePaymentState(info, false);
     Document sDoc = this.documentWorker.createPayDocumentStateXML(info);
     this.requestQueue.appendRequest(sDoc);
     if (this.logger.isDebugEnabled())
       {this.gLogger.debug("-------------------------------------------");
        this.gLogger.debug("Изменен статус документа " + info.globalID + " на " + info.state);
        this.gLogger.debug(this.rsFun.getDocInfo(info.globalID));
       }
     }
     catch(Exception e)
     {
       throw(e);
     }
     finally
     {
         
     }
   }


   /*
   private void handleABSStatement1(File file)
     throws Exception
   {
     this.logger.info("Обработка выписки из файла " + file.getName());
     Statement statement = new Statement();
     String fileContent = FileUtils.readFile(file, this.encoding);
     Saldo inSaldo = null; Saldo outSaldo = null;
     Employer employer = null;
     Period period = null;
     Record record = null;
     List auxFields = statement.getAuxFields();
     List payments = new ArrayList();
 
     for (String statementString : fileContent.split("\r\n")) {
       String stringType = statementString.substring(0, statementString.trim().indexOf(':'));
       String contentString = statementString.substring(statementString.indexOf(':') + 1, statementString.length());
 
       if ("statement".equals(stringType)) {
         String[] header = contentString.split("\\^", -1);
         if (header.length < 11) throw new IncomingFileFormatException("Формат заголовка выписки неверный");
         employer = new Employer(header[4], header[6]);
         record = createRecord(header);
         createStatementHeader(statement, header);
         period = record.period;
         statement.setOperatingInfo(false);
       }
       else if ("payment".equals(stringType)) { payments.add(createStatementPayment(contentString.split("\\^", -1), record.getAccountNumber()));
       } else if ("saldo-incoming".equals(stringType)) { inSaldo = createSaldo(contentString.split("\\^", -1));
       } else if ("saldo-outgoing".equals(stringType)) { outSaldo = createSaldo(contentString.split("\\^", -1));
       } else if ("turnover-debit".equals(stringType)) { statement.setDebetTurnOver(Double.valueOf(contentString).doubleValue());
       } else if ("turnover-credit".equals(stringType)) { statement.setCreditTurnOver(Double.valueOf(contentString).doubleValue());
       } else if ("aux-info".equals(stringType)) { auxFields.add(createAuxInfo(contentString.split("\\^", -1))); } else {
         throw new IncomingFileFormatException("Файл выписки [" + file.getName() + "] имеет некорректный формат");
       }
     }
 
     Collections.sort(payments, new Comparator<Payment>() {
       public int compare(Payment payment, Payment payment1) {
         return payment.getOperationDate().compareTo(payment1.getOperationDate());
       }
     });
     calcSaldos(record, statement, payments, null, inSaldo, outSaldo);
 
     Document statementDoc = this.documentWorker.createStatementXML(period, record, employer, statement, true);
     this.requestQueue.appendRequest(statementDoc);
 
     Document accountStateDoc = this.documentWorker.createAccountStateXML(record, statement);
     this.requestQueue.appendRequest(accountStateDoc);
 
     this.logger.info("Выписка по счету" + record.getAccountNumber() + " из файла [" + file.getName() + "] успешно сформирована");
   }
*/

   public void createStatement(String account,java.util.Date fromDate,java.util.Date toDate) throws Exception
   {
     Period period;
     Record record;
     Employer employer;
     Statement statement;

     logger.info("------------------------TEST--STATEMENT--BEGIN--------------------------------");
     try
     {
     RSAccount acc = new RSAccount();
     acc.initFromABS(account);
     statement = acc.getStatement_new(fromDate, toDate);
     period = new Period(fromDate, toDate);
     record = new Record();
     record.setAccountNumber(account);
     record.setPeriod(period);
     record.setBIC(statement.getBic());
     record.setOperatingInfo(statement.isOperatingInfo());

     employer = acc.getAbsClient(acc.getOwner());

     Document statementDoc = this.documentWorker.createStatementXML(period, record, employer, statement, true);
     this.requestQueue.appendRequest(statementDoc);

     Document accountStateDoc = this.documentWorker.createAccountStateXML(record, statement);
     this.requestQueue.appendRequest(accountStateDoc);
     }
     catch (Exception e)
     {
         logger.error("createStatement error");
         throw(e);
     }
     logger.info("------------------------TEST--STATEMENT--END----------------------------------");
   }



/*
   private Saldo createSaldo(String[] contentString) throws Exception
   {
     if ((!Utils.isEmpty(contentString[0])) || (!Utils.isEmpty(contentString[1])) || (!Utils.isEmpty(contentString[2]))) {
       return new Saldo(this.simpleDateFormat.parse(contentString[0]), Double.valueOf(contentString[1]).doubleValue(), (Utils.isEmpty(contentString[2])) ? null : new Double(Double.valueOf(contentString[2]).doubleValue()));
     }
 
     throw new IncomingFileFormatException("Неверный формат сальдо");
   }
 
   private AuxField createAuxInfo(String[] auxString) throws RSBankConnectionFactory.IncomingFileFormatException {
     if (auxString.length == 3) {
       this.logger.debug("Добавлено дополнительное поле (aux-info): " + auxString[0]);
       return new AuxField(auxString[0], auxString[1], Double.valueOf(auxString[2]));
     }
 
     throw new IncomingFileFormatException("aux-info некорректно сформирован");
   }
 
   private Payment createStatementPayment(String[] paymentString, String statementAccountNumber) throws Exception {
     if (paymentString.length != 40) {
       throw new IncomingFileFormatException("Некорректно сформирована строка платежного поручения в выписке. Количество полей: " + paymentString.length + ", ожидается 40");
     }
     for (int i = 0; i < paymentString.length; ++i) {
       paymentString[i] = paymentString[i].trim();
     }
 
     Payment payment = new Payment();
 
     payment.setDocNum(paymentString[0]);
     payment.setDocDate(this.simpleDateFormat.parse(paymentString[1]));
     payment.setOperationType(paymentString[2]);
     payment.setBankReceiptDate(this.simpleDateFormat.parse(paymentString[5]));
     payment.setChargeOffDate(this.simpleDateFormat.parse(paymentString[6]));
     payment.setOperationDate(payment.getChargeOffDate());
     payment.setPayDeliveryType(paymentString[7]);
     payment.setQueue(paymentString[9]);
     payment.setAmount(Double.valueOf(paymentString[10]).doubleValue());
 
     PaymentClient payer = new PaymentClient();
     payer.setName(paymentString[11]);
     payer.setInn(paymentString[12]);
     payer.setKpp(paymentString[13]);
     payer.setAccountNumber(paymentString[14]);
     payer.setBankBic(paymentString[15]);
     payer.setBankName(paymentString[16]);
     payer.setBankAccount(paymentString[17]);
     payment.setPayer(payer);
 
     PaymentClient payee = new PaymentClient();
     payee.setName(paymentString[23]);
     payee.setInn(paymentString[24]);
     payee.setKpp(paymentString[25]);
     payee.setAccountNumber(paymentString[26]);
     payee.setBankBic(paymentString[27]);
     payee.setBankName(paymentString[28]);
     payee.setBankAccount(paymentString[29]);
     payment.setPayee(payee);
 
     payment.setPayPurpose(paymentString[30]);
 
     payment.setTaxPayerType(paymentString[31]);
     payment.setTaxBudgetCode(paymentString[32]);
     payment.setTaxDocDate(paymentString[33]);
     payment.setTaxDocNumber(paymentString[34]);
     payment.setTaxPaymentReason(paymentString[35]);
     payment.setTaxPaymentType(paymentString[36]);
     payment.setTaxRegionCode(paymentString[37]);
     payment.setTaxPeriod(paymentString[38]);
     payment.setBankNotes(paymentString[39]);
 
     payment.setDebet(statementAccountNumber.equals(payment.getPayer().getAccountNumber()));
 
     if (this.logger.isDebugEnabled()) this.logger.debug("Добавление платежа в выписку: " + payment.toString());
     return payment;
   }
 
   private Record createRecord(String[] header)
     throws Exception
   {
     Record record = new Record();
 
     record.period = new Period(this.simpleDateFormat.parse(header[1]), this.simpleDateFormat.parse(header[2]));
     record.setAccountNumber(header[5]);
     return record;
   }
 
   private void createStatementHeader(Statement statement, String[] header)
     throws Exception
   {
     statement.setBic(header[8]);
     statement.setLastOperationDate(this.simpleDateFormat.parse(header[3]));
   }
*/

 /**
  * обработка платежки из Системы
  * @param absDocument
  * @param client
  * @param payDocument
  * @throws Exception
  */
   public void processPayment(AbsDocument absDocument, Client client, PayDocument payDocument)
     throws Exception
   {
     if (payDocument instanceof Payment) { // рублевое пп
       processPayment(absDocument, (Payment)payDocument);
     }
     else if (payDocument instanceof CurrencyPayment) {//валютное пп
       processPayment(absDocument, (CurrencyPayment)payDocument);
     }
     else {
       String returnMessage = "Неподдерживаемый вид документа";
       Document rDoc = this.documentWorker.createRejectDocumentXML(absDocument, returnMessage);
       this.requestQueue.appendRequest(rDoc);
       throw new ReplyException(returnMessage);
     }
   }

   /**
    * обработка поступившего из Системы валютного пп
    * @param absDocument
    * @param payment
    * @throws Exception
    */
   private void processPayment(AbsDocument absDocument, CurrencyPayment payment)
     throws Exception
   {
     String xmlSource = XmlUtils.toXML(absDocument.getRoot(), "windows-1251");
     String sgnSource = XmlUtils.toSGN(absDocument.getSigners());
 
     String requestId = Utils.formatLong(getGateBase().getNextRequestID(), this.requestIdFormat);
  //   String requestName = "plv" + requestId;
 
     Connection con = null;
     try {
       con = DriverManager.getConnection("proxool.gate");
       con.setAutoCommit(false);

 // добавить информацию в базу шлюза и пп в АБС      
 //       getRSBankGateBase().appendCurrencyPayment(con, requestId, payment, xmlSource, sgnSource);
//       File result = writeAbsFile(requestName, processPaymentToAbsFormat(payment));
       this.logger.debug("Запрос на добавление документа создан успешно [ ]");
     }
     catch (Exception e)
     {
       throw e;
     } finally {
       if (con != null) {
         con.setAutoCommit(true);
         con.close();
       }
     }
   }

   /**
    * обработка поступившего из Системы рублевого пп
    * @param absDocument
    * @param payment
    * @throws Exception
    */
   private void processPayment(AbsDocument absDocument, Payment payment)
     throws Exception
   {
     String xmlSource = XmlUtils.toXML(absDocument.getRoot(), "windows-1251");
     String sgnSource = XmlUtils.toSGN(absDocument.getSigners());
 
     String requestId = Utils.formatLong(getGateBase().getNextRequestID(), this.requestIdFormat);
   //  String requestName = "pla" + requestId;
 
     checkOrders(absDocument, payment);
 
     List payments = payment.listPayments();
     Payment p;
     for (Iterator i$ = payments.iterator(); i$.hasNext(); p.setGateID(getGateBase().getNextGlobalID()))
       p = (Payment)i$.next();
 
     Connection con = null;
     try {
       con = DriverManager.getConnection("proxool.gate"); //getRSBankGateBase().getDataSource().getConnection();
       con.setAutoCommit(false);
 // добавить информацию в базу шлюза и пп в АБС
       getRSBankGateBase().appendPayments(con, requestId, payments, xmlSource, sgnSource);


       //    File result = writeAbsFile(requestName, processPaymentToAbsFormat(payment));
       con.commit();
       this.logger.debug("Запрос на добавление документа создан успешно [ ]");
     }
     catch (Exception e)
     {
       con.rollback();
       throw e;
     } finally {
       if (con != null) {
         con.setAutoCommit(true);
         con.close();
       }
     }
   }


  @Override protected void afterProcessPayment(PayDocument payDocument) throws Exception
   {
     //this.logger.debug("Изменение состояния полученного документа [" + payDocument.getGlobalID() + ", " + DocState.RECEIVED + "](+)");
     //sendDocState(new PaymentInfo(PaymentType.PAYMENT, payDocument.getGlobalID(), DocState.RECEIVED, null));
     //this.logger.debug("Изменение состояния полученного документа [" + payDocument.getGlobalID() + ", " + DocState.RECEIVED + "](-)");
   }


   private String processPaymentToAbsFormat(CurrencyPayment payment)
   {
     if (payment == null) return null;
 
     PaymentStringBuilder result = new PaymentStringBuilder("^");
 
     boolean isInternalPayment = (payment.payer != null) && (payment.payee != null) && (payment.payer.getBankBic() != null) && (payment.payee.getBankBic() != null) && (payment.payer.getBankBic().equals(payment.payee.getBankBic()));
 
     String payerBankAccountNumber = (Utils.isEmpty(payment.payer.getBankAccount())) ? this.documentWorker.getDefaultBankInfo().getAccount() : payment.payer.getBankAccount();
 
     result.append(this.paymentDateFormat.format(payment.getDocDate())).append(payment.getDocNum()).append("01").append("В").appendEmpty(5).append("4").append(payment.getAmount().getCurrency().code()).append(Double.toString(payment.getAmount().getValue())).append(Double.toString(payment.getAmount().getValue())).appendEmpty(5).append(payment.payer.getAccountNumber()).append(payment.payer.getBankBic()).append(payerBankAccountNumber).appendEmpty().append(payment.payer.getBankBic()).appendEmpty().append(payment.payee.getAccountNumber()).append(payment.payee.getBankBic()).append(payment.payee.getBankAccount()).appendEmpty().append(payment.payee.getBankBic()).appendEmpty();
 
     String payPurpose = payment.getPaymentInfo().replaceAll("\n", " ").replaceAll("\r", " ");
     result.append(payPurpose).appendEmpty(2).append(payment.payer.getName()).append(payment.payee.getName()).appendEmpty(12).append("e").append(payment.payer.getAccountNumber()).appendEmpty(5);
 
     if (isInternalPayment)
       result.append(payment.payee.getAccountNumber());
     else {
       result.append(payerBankAccountNumber);
     }
     result.appendEmpty(12).append("CUR" + payment.getGlobalID()).appendEmpty(5).append(payment.payer.getInn()).append(payment.payee.getInn()).appendEmpty(6);
 
     result.endDocument();
     return result.toString();
   }
 
   private String processPaymentToAbsFormat(Payment payment)
   {
     PaymentStringBuilder result = new PaymentStringBuilder("^");
     boolean isInternalPayment = payment.getPayer().getBankBic().equals(payment.getPayee().getBankBic());
 
     result.append(this.paymentDateFormat.format(payment.getDocDate())).append(payment.getDocNum()).append("01").append("В").appendEmpty(4);
 
     if ("почтой".equals(payment.getPayDeliveryType()))
       result.append("8022");
     else if ("телеграфом".equals(payment.getPayDeliveryType()))
       result.append("8023");
     else result.appendEmpty();
 
     result.append("4").append("810").append(Double.toString(payment.getAmount())).append(Double.toString(payment.getAmount())).appendEmpty().append(payment.getQueue()).appendEmpty(3).append(payment.getPayer().getAccountNumber()).append(payment.getPayer().getBankBic()).append(payment.getPayer().getBankAccount()).appendEmpty().append(payment.getPayer().getBankBic()).appendEmpty().append(payment.getPayee().getAccountNumber()).append(payment.getPayee().getBankBic()).append(payment.getPayee().getBankAccount()).appendEmpty().append(payment.getPayee().getBankBic()).appendEmpty();
 
     String payPurpose = payment.getPayPurpose().replaceAll("\n", " ").replaceAll("\r", " ");
     result.append(payPurpose).appendEmpty(2).append(payment.getPayer().getName()).append(payment.getPayee().getName()).appendEmpty(12).append("e").append(payment.getPayer().getAccountNumber()).appendEmpty().append(payment.getPayDeliveryType().substring(0, 1).toUpperCase()).appendEmpty();
 
     if (isInternalPayment)
       result.append("900");
     else {
       result.append("901");
     }
     result.append("e");
 
     if (isInternalPayment)
       result.append(payment.getPayee().getAccountNumber());
     else {
       result.append(payment.getPayer().getBankAccount());
     }
     result.appendEmpty().append(this.paymentDateFormat.format(payment.getDocDate())).appendEmpty(10).append("RUR" + payment.getGlobalID()).appendEmpty(6).append(payment.getPayer().getInn()).append(payment.getPayee().getInn()).appendEmpty(15).append(payment.getPayer().getKpp()).append(payment.getPayee().getKpp()).append(payment.getTaxPayerType()).append(payment.getTaxBudgetCode()).append(payment.getTaxRegionCode()).append(payment.getTaxPaymentReason()).append(payment.getTaxPeriod()).append(payment.getTaxDocNumber()).append(payment.getTaxDocDate()).append(payment.getTaxPaymentType()).append(this.paymentDateFormat.format(new java.util.Date())).appendEmpty();
 
     result.endDocument();
 
     return result.toString();
   }
 
   protected void calcSaldos(Record record, Statement statement, List<Payment> payments, Double currencyRate, Saldo incoming, Saldo outgoing)
   {
     for (Payment payment : payments) {
       java.util.Date operDate = payment.getOperationDate();
       if (record.period.contains(operDate)) statement.addPayment(payment);
       if ((payment.getAmountNat() == null) && (currencyRate != null)) payment.setAmountNat(Double.valueOf(payment.getAmount() * currencyRate.doubleValue()));
       statement.addAllSaldo(new SaldoInOut(new Saldo(incoming.getDate(), incoming.getSaldo(), incoming.getSaldoNat()), new Saldo(outgoing.getDate(), outgoing.getSaldo(), outgoing.getSaldoNat())));
 
       if (incoming.getDate().equals(operDate)) {
         outgoing.setSaldo(incoming.getSaldo());
       }
       else {
         incoming.setSaldo(outgoing.getSaldo());
         incoming.setSaldoNat(outgoing.getSaldoNat());
       }
       outgoing.setDate(operDate);
       incoming.setDate(operDate);
       outgoing.setSaldo(outgoing.getSaldo() + ((payment.isDebet()) ? -payment.getAmount() : payment.getAmount()));
       if (payment.isDebet()) outgoing.decSaldoNat(payment.getAmountNat()); else {
         outgoing.incSaldoNat(payment.getAmountNat());
       }
     }
     statement.addAllSaldo(new SaldoInOut(new Saldo(incoming.getDate(), incoming.getSaldo(), incoming.getSaldoNat()), new Saldo(outgoing.getDate(), outgoing.getSaldo(), outgoing.getSaldoNat())));
   }
 
   private class IncomingFileFormatException extends Exception
   {
     public IncomingFileFormatException(String s)
     {
       super(s);
     }
   }
 
   private class PaymentStringBuilder
   {
     private StringBuilder result;
     private String fieldSeparator;
 
     PaymentStringBuilder(String fieldSeparator)
     {
       this.fieldSeparator = fieldSeparator;
       this.result = new StringBuilder();
     }
 
     public PaymentStringBuilder append(String s)
     {
       if (Utils.isEmpty(s)) appendEmpty();
       else
         this.result.append(s.replaceAll(this.fieldSeparator, "")).append(this.fieldSeparator);
       return this;
     }
 
     public PaymentStringBuilder append(String s, int times)
     {
       while (times-- > 0)
         append(s);
       return this;
     }
 
//@Override
     public String toString() {
       return this.result.toString();
     }
 
     public PaymentStringBuilder appendEmpty()
     {
       this.result.append(this.fieldSeparator);
       return this;
     }
 
     public PaymentStringBuilder appendEmpty(int times)
     {
       while (times-- > 0)
         appendEmpty();
       return this;
     }
 
     public PaymentStringBuilder endDocument()
     {
       this.result.delete(this.result.length() - 1, this.result.length()).append("\r\n");
 
       return this;
     }
   }
   
   /**
    * Обработка писем от клиентов
    * @param absDocument
    * @param client
    * @param eMessage
    * @throws java.lang.Exception
    */
   @Override public void processEMessage(AbsDocument absDocument, Client client, EMessage eMessage)
     throws Exception
   {
     logger.info("Path:" + eMessage.getAttachment().getAbsolutePath());
     
 
     //logger.error("processEMessage is not implemented");
   }
   
  private void sendMessages() throws Exception
  {
     EMessage eMess = new EMessage();
     Employee emp = new Employee("$$$bank-clients","","");
     //emp.setName("$$$bank-clients");
     eMess.addRecipient(emp);
     eMess.setSubject("очень важное письмо");
     eMess.setBody("просто тело письма");
     //eMess.setSender("Иванов Иван Иванович");
     eMess.setSender("Зенит Сочи Администратор");
     eMess.setGlobalID("123456");

     Document eMessDoc = this.documentWorker.createEMessageXML(eMess);
     this.requestQueue.appendRequest(eMessDoc);
     logger.info("Письмо отправлено"); 
  }
   
 }

/* Location:           J:\sochi\gate\lib\fxgate-1.11.6.jar
 * Qualified Name:     ru.ftc.interpay.gate.abs.BankirConnectionFactory
 * JD-Core Version:    0.5.4
 */
