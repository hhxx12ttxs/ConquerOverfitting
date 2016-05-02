package ib;
import com.ib.client.*;


public class Main implements EWrapper {

   EClientSocket m_client = new EClientSocket(this);
   int id = 1;

   public Main(String[] args) {
      m_client.eConnect("", 7496, 6);

      if (!m_client.isConnected()) {
         System.out.println("Unable to connect to IB");
         System.exit(-1);
      }
      System.out.println("Sample TWS API Client Java code to print market data");
      System.out.println("getting quotes for BAC STK");
      System.out.println("Press CTRL-C to quit");
      Contract bacContract = new Contract();
      bacContract.m_symbol = "BAC";
      bacContract.m_secType = "STK";
      bacContract.m_exchange = "SMART";
      bacContract.m_currency = "USD";
      String tickTypes = "" + TickType.BID + "," + TickType.ASK + "," + TickType.LAST;
      tickTypes = "100,101,104,105,106,107,165,221,225,233,236,258";
      m_client.reqMktData(id, bacContract, "", false);
      m_client.reqAccountUpdates(true, "U746502");
      m_client.reqContractDetails(2, bacContract);
      
      Thread.yield();
   }

   public static void main(String[] args) {
      Main main = new Main(args);
   }

   public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
      switch (field) {
         case 1:  //bid
            System.out.println("Bid Price = " + String.valueOf(price));
            break;
         case 2:  //ask
            System.out.println("Ask Price = " + String.valueOf(price));
            break;
         case 4:  //last
            System.out.println("Last Price = " + String.valueOf(price));
            break;
         case 6:  //high
            System.out.println("High Price = " + String.valueOf(price));
            break;
         case 7:  //low
            System.out.println("Low Price = " + String.valueOf(price));
            break;
         case 9:  //close
            System.out.println("Close Price = " + String.valueOf(price));
            break;
      }
   }

   public void tickSize(int tickerId, int field, int size) {
      switch (field) {
         case 0:   //bid
            System.out.println("Bid Size = " + String.valueOf(size));
            break;
         case 3:   //ask
            System.out.println("Ask Size = " + String.valueOf(size));
            break;
         case 5:   //last
            System.out.println("Last Size = " + String.valueOf(size));
            break;
         case 8:   //volume
            System.out.println("Volume = " + String.valueOf(size));
            break;
      }
   }

   public void orderStatus(int orderId, String status, int filled, int remaining,
           double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId) {
   }

   public void openOrder(int orderId, Contract contract, Order order) {
   }

   public void error(String str) {
   }

   public void connectionClosed() {
      System.out.println("IB Connection Closed");
   }

   public void nextValidId(int orderId) {
      id = orderId;
      System.out.println(orderId);
   }

   public void error(int id, int errorCode, String errorMsg) {
      System.out.println("Error id = " + String.valueOf(id) + " Error Code = " + String.valueOf(errorCode) + " " + errorMsg);
   }

   public void accountDownloadEnd(String accountName) {
      System.out.println("accountDownloadEnd: " + accountName);
   }

   public void bondContractDetails(int reqId, ContractDetails contractDetails) {
      System.out.println("bondContractDetails: " + reqId + contractDetails);
   }

   public void contractDetails(int reqId, ContractDetails contractDetails) {
      System.out.println("contractDetails: " + reqId + "month" + contractDetails.m_contractMonth + "" + contractDetails.m_marketName + ":");
   }

   public void contractDetailsEnd(int reqId) {
      System.out.println("contractDetailsEnd: " + reqId);
   }

   public void currentTime(long time) {
      System.out.println("currentTime: " + time);
   }

   public void fundamentalData(int reqId, String data) {
      System.out.println("fundamentalData ...");
   }

   public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
      System.out.println("historicalData ...");
   }

   public void managedAccounts(String accountsList) {
      System.out.println("managedAccounts: " + accountsList);
   }

   public void realtimeBar(int reqId, long time, double open, double high,
           double low, double close, long volume, double wap, int count) {
      System.out.println("realtimeBar ...");
   }

   public void receiveFA(int faDataType, String xml) {
      System.out.println("receiveFA ...");
   }

   public void scannerData(int reqId, int rank,
           ContractDetails contractDetails, String distance, String benchmark,
           String projection, String legsStr) {
      System.out.println("scannerData ...");
   }

   public void scannerDataEnd(int reqId) {
      System.out.println("scannerDataEnd ...");
   }

   public void scannerParameters(String xml) {
      System.out.println("scannerParameters ...");
   }

   public void tickEFP(int tickerId, int tickType, double basisPoints,
           String formattedBasisPoints, double impliedFuture, int holdDays,
           String futureExpiry, double dividendImpact, double dividendsToExpiry) {
      System.out.println("tickEFP ...");
   }

   public void tickGeneric(int tickerId, int tickType, double value) {
      System.out.println("tickGeneric ...tickerId" + tickerId);
      System.out.println("tickGeneric ...tickerType" + tickType);
      System.out.println("tickGeneric ...value" + value);
   }

   public void tickOptionComputation(int tickerId, int field,
           double impliedVol, double delta, double modelPrice,
           double pvDividend) {
      System.out.println("tickOptionComputation ...");
   }

   public void tickString(int tickerId, int tickType, String value) {
      System.out.println("tickString ...");
   }

   public void tickSnapshotEnd(int tickerId) {
      System.out.println("tickSnapshotEnd ...");
   }

   public void updateAccountTime(String timeStamp) {
      System.out.println("updateAccountTime: " + timeStamp);
   }

   public void updateAccountValue(String key, String value, String currency, String accountName) {
      System.out.println("updateAccountValue..." + "key" + key + "value" + value + "currency" + currency + "accountName" + accountName);
   }

   public void updateMktDepth(int tickerId, int position, int operation,
           int side, double price, int size) {
      System.out.println("updateMktDepth...");
   }

   public void updateMktDepthL2(int tickerId, int position,
           String marketMaker, int operation, int side, double price, int size) {
      System.out.println("updateMktDepthL2...");
   }

   public void updateNewsBulletin(int msgId, int msgType, String message,
           String origExchange) {
      System.out.println("updateNewsBulletin...");
   }

   public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
      System.out.println(contract.m_primaryExch+":"+contract.m_multiplier+":"+contract.m_symbol+":"+contract.m_expiry+":"+contract.m_strike+""+":"+position+":"+marketPrice+":"+marketValue+":"+accountName);
      new Account().addPosition(contract,position,marketPrice,marketValue,averageCost,unrealizedPNL, realizedPNL,accountName);
   }

   public void error(Exception e) {
      System.out.println("error: " + e);
   }

   /* *********************************************************************************************
    *                                  important for placing orders
    **********************************************************************************************/
   public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
      //System.out.println("openOrder: "+orderId+", "+contract+", "+order+", "+orderState);
   }

   public void openOrderEnd() {
      //System.out.println("openOrderEnd:");
   }

   public void orderStatus(int orderId, String status, int filled,
           int remaining, double avgFillPrice, int permId, int parentId,
           double lastFillPrice, int clientId, String whyHeld) {

      System.out.println("orderStatus: " + orderId + ", " + status + ", " + filled + ", " + remaining);

   }

   public void deltaNeutralValidation(int reqId, UnderComp underComp) {
      System.out.println("deltaNeutralValidation: " + reqId + ", " + underComp);
   }

   public void execDetails(int reqId, Contract contract, Execution execution) {
      System.out.println("execDetails: " + reqId + ", " + contract + ", " + execution);
   }

   public void execDetailsEnd(int reqId) {
      System.out.println("execDetailsEnd: " + reqId);
   }
}

