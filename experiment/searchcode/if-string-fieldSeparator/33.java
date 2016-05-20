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
 //import ru.ftc.interpay.gate.datamodel.BankInfo;
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
import com.coperius.gate.base.CouchdbGateBase;
import com.coperius.gate.base.PostgresGateBase;
import com.coperius.gate.utils.Convert;
import com.coperius.gate.utils.dateUtils;
import com.coperius.gate.utils.gateFunctions;
import com.coperius.gate.utils.gateSettings;
import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Session;
import com.fourspaces.couchdb.View;
import com.fourspaces.couchdb.ViewResults;

 import javax.management.*;
 import javax.management.remote.*;

 import java.lang.management.*;
import java.sql.DriverManager;

 import org.logicalcobwebs.proxool.configuration.PropertyConfigurator;
import ru.ftc.interpay.gate.client.ProcessSignException;

//Log_Tr("Изменилась инфо по счетам" + AcR.Account + " было " + bfAc.R0 + " стало " + AcR.R0 );
//Log_("Изменен статус документа с GlobalID " + RsGetNode("PaymentStatus\\globalID"));
// ошибки при проверке дока


 public class RSBankConnectionCouchdb extends AbstractABSConnectionFactory
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
   private SingletonCouchdb rsFun;
   private MBeanServer mbs = null;

   private Logger gLogger= Logger.getLogger("docs");
   
   public RSBankConnectionCouchdb()  throws ProxoolException
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
     this.rsFun = SingletonCouchdb.INSTANCE;
     logger.setLevel(Level.ALL);
     String curDir = new File(".").getAbsolutePath();
     PropertyConfigurator.configure(curDir + "/conf/dbconn.properties");
     
   }

 //   public RSFunctions getRsFun() {
 //       return rsFun;
 //   }

   public SingletonCouchdb getRsFun() {
        return rsFun;
        //DefaultMBeanServerInterceptor m = new DefaultMBeanServerInterceptor();

    }

   

//   protected RSBankGateBase getRSBankGateBase() {
//     return (RSBankGateBase)getGateBase();
//
//   }


   protected CouchdbGateBase getRSBankGateBase() {
     return (CouchdbGateBase)getGateBase();

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
     //this.rsFun.getCalendar();
     this.logger.debug(this.rsFun.getOperDate());
     //this.logger.debug(gateFunctions.getPackGate("40702810200000003765"));
     //this.logger.debug(gateFunctions.getPackGate("40502810200000003765"));
     
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
     //startABSSync();
     this.refreshDocumentStateTimeout=20000;
     //startDocumentToABSRefreshProcess();
     //CouchWorker cw = new CouchWorker(this);

    // gateSettings.setValue("pervasive", "jdbc:pervasive://10.211.55.3:1583/RS31;encoding=cp866");
     //cw.syncABSDocs_doc_rub();
     //cw.syncABSDocs_arh_rub(30);
//cw.syncRetDocs(30);
     //cw.getStatements3(true);
     //cw.getStatements3(false);
     //cw.sendStatements();
     startWorkers0();
     startWorkers();
     startWorkers2();
     startWorkers3();
     startWorkers4();
     startWorkers5();

     startWorkers6();
     startWorkers7();

     startWorkers8();

    // processABSSync();
   }
   private void startWorkers0()
   {
     Runnable documentStatusRefresher = new Runnable() {
       public void run() {
         RSBankConnectionCouchdb.this.logger.debug("Запущено задание обработки документов. Период обновления: " + RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
         while (!Thread.currentThread().isInterrupted())
           try {
             synchronized (this) {
               Thread.sleep(RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
               RSBankConnectionCouchdb.this.processWorkers0();
             }
           } catch (Exception e) {
             RSBankConnectionCouchdb.this.logger.debug("Выполнение обработки документов прервано ошибкой: ", e);
           }
       }
     };
     Thread refresher = new Thread(documentStatusRefresher);
     refresher.setDaemon(true);
     refresher.start();
   }
   private void startWorkers()
   {
     Runnable documentStatusRefresher = new Runnable() {
       public void run() {
         RSBankConnectionCouchdb.this.logger.debug("Запущено задание обработки документов. Период обновления: " + RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
         while (!Thread.currentThread().isInterrupted())
           try {
             synchronized (this) {
               Thread.sleep(RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
               RSBankConnectionCouchdb.this.processWorkers();
             }
           } catch (Exception e) {
             RSBankConnectionCouchdb.this.logger.debug("Выполнение обработки документов прервано ошибкой: ", e);
           }
       }
     };
     Thread refresher = new Thread(documentStatusRefresher);
     refresher.setDaemon(true);
     refresher.start();
   }


    private void startWorkers2()
   {
     Runnable documentStatusRefresher = new Runnable() {
       public void run() {
         RSBankConnectionCouchdb.this.logger.debug("Запущено задание обработки документов. Период обновления: " + RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
         while (!Thread.currentThread().isInterrupted())
           try {
             synchronized (this) {
               Thread.sleep(RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
               RSBankConnectionCouchdb.this.processWorkers2();
             }
           } catch (Exception e) {
             RSBankConnectionCouchdb.this.logger.debug("Выполнение обработки документов прервано ошибкой: ", e);
           }
       }
     };
     Thread refresher = new Thread(documentStatusRefresher);
     refresher.setDaemon(true);
     refresher.start();
   }

      private void startWorkers3()
   {
     Runnable documentStatusRefresher = new Runnable() {
       public void run() {
         RSBankConnectionCouchdb.this.logger.debug("Запущено задание обработки документов. Период обновления: " + RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
         while (!Thread.currentThread().isInterrupted())
           try {
             synchronized (this) {
               Thread.sleep(RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
               RSBankConnectionCouchdb.this.processWorkers3();
             }
           } catch (Exception e) {
             RSBankConnectionCouchdb.this.logger.debug("Выполнение обработки документов прервано ошибкой: ", e);
           }
       }
     };
     Thread refresher = new Thread(documentStatusRefresher);
     refresher.setDaemon(true);
     refresher.start();
   }

        private void startWorkers4()
   {
     Runnable documentStatusRefresher = new Runnable() {
       public void run() {
         RSBankConnectionCouchdb.this.logger.debug("Запущено задание обработки документов. Период обновления: " + RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
         while (!Thread.currentThread().isInterrupted())
           try {
             synchronized (this) {
               Thread.sleep(RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
               RSBankConnectionCouchdb.this.processWorkers4();
             }
           } catch (Exception e) {
             RSBankConnectionCouchdb.this.logger.debug("Выполнение обработки документов прервано ошибкой: ", e);
           }
       }
     };
     Thread refresher = new Thread(documentStatusRefresher);
     refresher.setDaemon(true);
     refresher.start();
   }

        private void startWorkers5()
   {
     Runnable documentStatusRefresher = new Runnable() {
       public void run() {
         RSBankConnectionCouchdb.this.logger.debug("Запущено задание обработки документов. Период обновления: " + RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
         while (!Thread.currentThread().isInterrupted())
           try {
             synchronized (this) {
               Thread.sleep(RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
               RSBankConnectionCouchdb.this.processWorkers5();
             }
           } catch (Exception e) {
             RSBankConnectionCouchdb.this.logger.debug("Выполнение обработки документов прервано ошибкой: ", e);
           }
       }
     };
     Thread refresher = new Thread(documentStatusRefresher);
     refresher.setDaemon(true);
     refresher.start();
   }

        private void startWorkers6()
   {
     Runnable documentStatusRefresher = new Runnable() {
       public void run() {
         RSBankConnectionCouchdb.this.logger.debug("Запущено задание обработки документов. Период обновления: " + RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
         while (!Thread.currentThread().isInterrupted())
           try {
             synchronized (this) {
               Thread.sleep(RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
               RSBankConnectionCouchdb.this.processWorkers6();
             }
           } catch (Exception e) {
             RSBankConnectionCouchdb.this.logger.debug("Выполнение обработки документов прервано ошибкой: ", e);
           }
       }
     };
     Thread refresher = new Thread(documentStatusRefresher);
     refresher.setDaemon(true);
     refresher.start();
   }

        private void startWorkers7()
   {
     Runnable documentStatusRefresher = new Runnable() {
       public void run() {
         RSBankConnectionCouchdb.this.logger.debug("Запущено задание обработки документов. Период обновления: " + RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
         while (!Thread.currentThread().isInterrupted())
           try {
             synchronized (this) {
               Thread.sleep(RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
               RSBankConnectionCouchdb.this.processWorkers7();
             }
           } catch (Exception e) {
             RSBankConnectionCouchdb.this.logger.debug("Выполнение обработки документов прервано ошибкой: ", e);
           }
       }
     };
     Thread refresher = new Thread(documentStatusRefresher);
     refresher.setDaemon(true);
     refresher.start();
   }
 private void startWorkers8()
   {
     Runnable documentStatusRefresher = new Runnable() {
       public void run() {
         RSBankConnectionCouchdb.this.logger.debug("Запущено задание обработки документов. Период обновления: " + RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
         while (!Thread.currentThread().isInterrupted())
           try {
             synchronized (this) {
               Thread.sleep(RSBankConnectionCouchdb.this.refreshDocumentStateTimeout);
               RSBankConnectionCouchdb.this.processWorkers8();
             }
           } catch (Exception e) {
             RSBankConnectionCouchdb.this.logger.debug("Выполнение обработки документов прервано ошибкой: ", e);
           }
       }
     };
     Thread refresher = new Thread(documentStatusRefresher);
     refresher.setDaemon(true);
     refresher.start();
   }

protected void processWorkers0() throws Exception
     {
     CouchWorker cw = new CouchWorker(this);
     logger.info("------------------COUCHWORKER---------------------");
     //cw.updateIsOnline();
     //if (!gateFunctions.isOnline()) return;
     cw.updateOperDate();
     cw.getStatements3(false);
//     cw.sendStatements();


     //cw.test();
}

protected void processWorkers() throws Exception
     {
     CouchWorker cw = new CouchWorker(this);
     logger.info("------------------COUCHWORKER---------------------");
     //cw.updateIsOnline();
     //if (!gateFunctions.isOnline()) return;
     //cw.updateOperDate();
     cw.getStatements3(true);
//     cw.sendStatements();
     
     
     //cw.test();
}
protected void processWorkers2() throws Exception
     {
     CouchWorker cw = new CouchWorker(this);
     logger.info("------------------COUCHWORKER222---------------------");
     // сначала загрузим платежки, а потом обновим статусы
     cw.sendPayments2ABS();
     cw.jur_scan();
}

protected void processWorkers3() throws Exception
     {
     CouchWorker cw = new CouchWorker(this);
     logger.info("------------------COUCHWORKER333---------------------");
     cw.sendStatuses();
}

protected void processWorkers4() throws Exception
     {
     CouchWorker cw = new CouchWorker(this);
     logger.info("------------------COUCHWORKER444---------------------");

}
 
protected void processWorkers5() throws Exception
     {
     CouchWorker cw = new CouchWorker(this);
     logger.info("------------------COUCHWORKER555---------------------");
     cw.sendStatements();
     }


protected void processWorkers6() throws Exception
     {
     CouchWorker cw = new CouchWorker(this);
     logger.info("------------------COUCHWORKER666---------------------");
     //cw.updateIsOnline();
     //if (!gateFunctions.isOnline()) return;
     cw.syncABSDocs_arh_rub(Integer.parseInt(gateSettings.getValue("syncdays")));
     //cw.syncABSDocs_arh_cur(Integer.parseInt(gateSettings.getValue("syncdays")));
logger.info("------------------COUCHWORKER666---END---------------");

     //cw.test();
}

protected void processWorkers7() throws Exception
     {
     CouchWorker cw = new CouchWorker(this);
     logger.info("------------------COUCHWORKER777---------------------");
     //cw.updateIsOnline();
     //if (!gateFunctions.isOnline()) return;
     //cw.syncABSDocs_doc_cur();
     cw.syncABSDocs_doc_rub();
logger.info("------------------COUCHWORKER777------END------------");

     //cw.test();
}
protected void processWorkers8() throws Exception
     {
     CouchWorker cw = new CouchWorker(this);
     logger.info("------------------COUCHWORKER888---------------------");
     //cw.updateIsOnline();
     //if (!gateFunctions.isOnline()) return;
     //cw.syncABSDocs_doc_cur();
     cw.syncRetDocs(Integer.parseInt(gateSettings.getValue("syncdays")));
logger.info("------------------COUCHWORKER888------END------------");

     //cw.test();
}

   protected PaymentInfo getPaymentState(String globalID, PaymentType paymentType)
     throws Exception
   {
     this.logger.debug("Получение статуса документа globalID=" + globalID);
     List fields = new ArrayList(1);
     fields.add(new Field("globalID", globalID));
 
     PaymentInfo info = new PaymentInfo();
     info = (paymentType != null) ? getRSBankGateBase().getPaymentInfo(globalID, paymentType) : getRSBankGateBase().getPaymentInfo(globalID, PaymentType.PAYMENT);
//
//     if (info == null) info = getRSBankGateBase().getPaymentInfo(fields, PaymentType.CURRENCY_PAYMENT);
//     if (info == null) info = getRSBankGateBase().getPaymentInfo(fields, PaymentType.CURRENCY_EXCHANGE_ORDER);
//     if (info == null) info = getRSBankGateBase().getPaymentInfo(fields, PaymentType.FORCED_CURRENCY_SALE_ORDER);

     return info;
   }
 
   protected void createRegularStatements() throws Exception {
     this.logger.info("Плановое создание выписок по времени...");
     BankInfo bankInfo = this.documentWorker.getDefaultBankInfo();


   }






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
     this.createStatement(record);

     this.logger.debug("Запрос на выписку по счету " + record.accountNumber + " создан успешно [" + "]");

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
 

   public void createStatement(Record rec) throws Exception
   {

     logger.info("------------------------TEST--STATEMENT--BEGIN--------------------------------");
     try
     {
     
     Session s = new Session("localhost",5984);
     Database db = s.getDatabase("gate");
     com.fourspaces.couchdb.Document doc = new com.fourspaces.couchdb.Document();
     doc.put("type", "st_req");
     doc.put("account", rec.accountNumber);
     String from= dateUtils.date2string(rec.getPeriod().getFromDate());
     doc.put("date_from", from);
     String to = dateUtils.date2string(rec.getPeriod().getToDate());
     doc.put("date_to", to);
     String today = dateUtils.date2string(new java.util.Date());
     doc.put("time", today);
     doc.put("status", "0");
     doc.put("bic",rec.getBIC());
     doc.put("operationinfo", rec.getOperatingInfo());
     doc.put("reqid", rec.requestId);
     db.saveDocument(doc);

     }
     catch (Exception e)
     {
         logger.error("create statement request error");
         throw(e);
     }
     logger.info("------------------------TEST--STATEMENT--END----------------------------------");
   }




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
       //con = DriverManager.getConnection("proxool.gate");
       //con.setAutoCommit(false);

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
 //    String xmlSource = XmlUtils.toXML(absDocument.getRoot(), "windows-1251");
     String sgnSource = XmlUtils.toSGN(absDocument.getSigners());
     

     // TODO сделать реквестайди из коуча
     String requestId = Utils.formatLong(getGateBase().getNextRequestID(), this.requestIdFormat);
   //  String requestName = "pla" + requestId;
 
     checkOrders(absDocument, payment);

     //TODO Обработка вложенных платежей (комиссия и т.п.)
     List payments = payment.listPayments();
     Payment p;
//     for (Iterator i$ = payments.iterator(); i$.hasNext(); p.setGateID(getGateBase().getNextGlobalID()))
//       p = (Payment)i$.next();
 
//     Connection con = null;
     try {
          Session s = new Session("localhost",5984);
          Database db = s.getDatabase("gate");
          com.fourspaces.couchdb.Document doc = new com.fourspaces.couchdb.Document();



//       for (int i = 0; i < payments.size(); ++i)
//       {
//         Payment payment = (Payment)payments.get(i);
         doc.put("globalid", payment.getGlobalID());
         doc.setId("fdoc_"+payment.getGlobalID());
         doc.put("gateid","fdoc_"+payment.getGlobalID());
         doc.put("type", "fdoc");

     logger.info(payment);
     logger.info(payment.getPayer().getAccountNumber());
     switch(gateFunctions.getAccountType(payment.getPayer().getAccountNumber()).ordinal())
     {
      case 0: //    External
        break;
      case 1: //    Jurical
       doc.put("type2","jurrub");
       break;
      case 2: //    PhizicDepozit
       doc.put("type2","phizdepozitrub");
       break;
      case 3: //    PhizicCard
       doc.put("type2","phizcardrub");
       break;
      case 4: //    PhizicCredit
       doc.put("type2","phizcreditrub");
       break;
      default:
       //TODO сгенерировать ошибку нахождения типа счета

      }


         
         doc.put("appkey","");
         doc.put("appkind","");
         //doc.put("currency","0");
         doc.put("currency",Currency.RUR.toString());
         //doc.put("currency",payment.getCurrency().toString());
         //doc.put("gateid", payment.getGateID()); //doc._id
         //doc.put(3, requestId);
         doc.put("tagname", payment.getTagName());
         doc.put("status", DocState.DELIVERED.name().toLowerCase());
         doc.put("docnum", payment.getDocNum());
         doc.put("docdate", dateUtils.date2string(payment.getDocDate()));
         doc.put("operationtype", payment.getOperationType());
         doc.put("operationdate", dateUtils.date2string(payment.getOperationDate()));
         doc.put("amount", payment.getAmount());
         doc.put("amountnat", payment.getAmountNat()==null?0:payment.getAmountNat()==null);
         doc.put("queue", payment.getQueue());
         doc.put("paypurpose", payment.getPayPurpose());
         doc.put("paydeliverytype", payment.getPayDeliveryType());

         doc.put("payername", payment.getPayer().getName());
         doc.put("payerinn", payment.getPayer().getInn());
         doc.put("payeraccountnumber", payment.getPayer().getAccountNumber());
         doc.put("payerbankbic", payment.getPayer().getBankBic());
         doc.put("payerbankbictype", "bic");//payment.getPayer().getBankBicType());
         doc.put("payerbankname", payment.getPayer().getBankName());
         doc.put("payerbankaccount", payment.getPayer().getBankAccount());
         doc.put("payerbankcity", payment.getPayer().getBankCity());
         doc.put("payerkpp", payment.getPayer().getKpp());

         doc.put("payeename", payment.getPayee().getName());
         doc.put("payeeinn", payment.getPayee().getInn());
         doc.put("payeeaccountnumber", payment.getPayee().getAccountNumber());
         doc.put("payeebankbic", payment.getPayee().getBankBic());
         doc.put("payeebankbictype", "bic");//payment.getPayee().getBankBicType());
         doc.put("payeebankname", payment.getPayee().getBankName());
         doc.put("payeebankaccount", payment.getPayee().getBankAccount());
         doc.put("payeebankcity", payment.getPayee().getBankCity());
         doc.put("payeekpp", payment.getPayee().getKpp());

         doc.put("taxpayertype", payment.getTaxPayerType());
         doc.put("taxbudgetcode", payment.getTaxBudgetCode());
         doc.put("taxregioncode", payment.getTaxRegionCode());
         doc.put("taxpaymentreason", payment.getTaxPaymentReason()); // longvarchar в нужной кодировке получим
         //stm.setString(31, payment.getTaxPaymentReason());
         doc.put("taxperiod", payment.getTaxPeriod());
         doc.put("taxdocnumber", payment.getTaxDocNumber());
         doc.put("taxdocdate",payment.getTaxDocDate());
         doc.put("taxpaymenttype", payment.getTaxPaymentType());

         doc.put("tagname", payment.getTagName());
         doc.put("relatedpayments", "");// TODO обработка вложенных платежей от фактуры
         doc.put("isdebet", payment.isDebet());// всегда правда
         doc.put("bankreceiptdate",dateUtils.date2string(payment.getBankReceiptDate()));
         doc.put("chargeoffdate", dateUtils.date2string(payment.getChargeOffDate()));
         doc.put("banknotes", payment.getBankNotes()==null?"":payment.getBankNotes());
         doc.put("vbcommissions", payment.getVbCommission()==null?"":payment.getVbCommission());

         List<CashSymbol> cs=payment.getCashSymbols();
         String Scs="[";
         if ((cs!=null) && (cs.size()>0))
         {
          for(int i=0;i<cs.size();i++)
          {
           if (i>0) Scs=Scs.concat(",");
           Scs=Scs.concat(Convert.cashSymbol2String(cs.get(i)) + ",");
          }
         }
        Scs=Scs.concat("]");
        doc.put("cashsymbols", Scs);

        doc.put("auxinfo", "[]");
         
//         doc.put(36, (i == 0) ? xmlSource : "");
        doc.put("signs", sgnSource );

        doc.put("cdate", dateUtils.date2string(new java.util.Date()));
        
        db.saveDocument(doc);

       this.logger.debug("Запрос на добавление документа создан успешно [ ]");
     }
     catch (Exception e)
     {

       throw e;
     } finally {
       }
     
   }


  @Override protected void afterProcessPayment(PayDocument payDocument) throws Exception
   {
     //this.logger.debug("Изменение состояния полученного документа [" + payDocument.getGlobalID() + ", " + DocState.RECEIVED + "](+)");
     //sendDocState(new PaymentInfo(PaymentType.PAYMENT, payDocument.getGlobalID(), DocState.RECEIVED, null));
     //this.logger.debug("Изменение состояния полученного документа [" + payDocument.getGlobalID() + ", " + DocState.RECEIVED + "](-)");
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

  public DocumentWorker getDW()
     {
      return this.documentWorker;
  }

  public PersistentGateRequestQueue getRQ()
     {
      return this.requestQueue;
  }

 }

/* Location:           J:\sochi\gate\lib\fxgate-1.11.6.jar
 * Qualified Name:     ru.ftc.interpay.gate.abs.BankirConnectionFactory
 * JD-Core Version:    0.5.4
 */
