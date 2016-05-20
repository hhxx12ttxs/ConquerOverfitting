package gnukhata.controllers;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import gnukhata.globals;
import gnukhata.views.AccountReport;
import gnukhata.views.LedgerRecon;
import gnukhata.views.ViewBalanceSheetReport;
import gnukhata.views.ViewCashFlowReport;
import gnukhata.views.ViewDualLedgr;
import gnukhata.views.ViewLedgerReport;
import gnukhata.views.ViewSourcesOfFundBalanceSheet;
import gnukhata.views.ViewUnclearedAccounts;
import gnukhata.views.getUnclearedTransactions;
import gnukhata.views.updateBankRecon;
import gnukhata.views.viewBalanceSheet;
import gnukhata.views.viewCashflow;
import gnukhata.views.viewProfitAndLossReport;
import gnukhata.views.viewProjectStatementReport;
import gnukhata.views.viewReconciliation;
import gnukhata.views.viewTrialBalReport;
import gnukhata.views.viewextendedtrialbalreport;
import gnukhata.views.viewgrosstrialbalreport;

public class reportController {
	
	private static final String ProjectName = null;

	public static void showBalanceSheet (Composite grandParent, String endDate, String tbType)
	{
		List<Object> serverParams = new ArrayList<Object>();
		List<Object> serverParams2 = new ArrayList<Object>();
		serverParams2.add(new Object[]{globals.session[2],globals.session[2],endDate});
		serverParams2.add(globals.session[0]);
		serverParams.add(new Object[]{globals.session[2], globals.session[2] ,endDate});
		serverParams.add(globals.session[0]);
		try {
			Object[] profitloss = (Object[]) globals.client.execute("reports.getProfitLoss", serverParams2);
			Object[] result = (Object[]) globals.client.execute("reports.getBalancesheet" , serverParams);
			if(tbType.equals("Conventional Balance Sheet"))
			{
			
				ViewBalanceSheetReport bsr = new ViewBalanceSheetReport(grandParent, SWT.NONE, endDate, result ,profitloss);
				bsr.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
			}
			if(tbType.equals("Sources & Application of fund"))
			{
				ViewSourcesOfFundBalanceSheet fbsr = new ViewSourcesOfFundBalanceSheet(grandParent, SWT.NONE, endDate, result, profitloss);
				fbsr.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
			}
		

		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		
		
	}

	public static void showTrialBalance (Composite grandParent, String endDate, String tbType)
	{
		List<Object> serverParams = new ArrayList<Object>();
		serverParams.add(new Object[]{globals.session[2], globals.session[2], endDate});
		serverParams.add(globals.session[0]);
		try {
			if(tbType.equals("Net Trial Balance"))
			{
				
				Object[] tbData = (Object[]) globals.client.execute("reports.getTrialBalance", serverParams);
				viewTrialBalReport tbr = new viewTrialBalReport(grandParent, endDate, SWT.NONE , tbData);
				tbr.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
			}
			if(tbType.equals("Gross Trial Balance"))
			{
				
				Object[] tbData1 = (Object[]) globals.client.execute("reports.getGrossTrialBalance", serverParams);
				viewgrosstrialbalreport tbr = new viewgrosstrialbalreport(grandParent,endDate, SWT.NONE , tbData1);
				tbr.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
			}
			if(tbType.equals("Extended Trial Balance"))
			{
				
				Object[] tbData2 = (Object[]) globals.client.execute("reports.getExtendedTrialBalance", serverParams);
				viewextendedtrialbalreport tbr = new viewextendedtrialbalreport(grandParent,endDate, SWT.NONE , tbData2);
				tbr.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
			}
			

		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		
	}
	
	
	public static void showProfitAndLoss(Composite grandParent,String toDate)
	{
		List<Object> serverParams = new ArrayList<Object>();
		serverParams.add(new Object[]{globals.session[2], globals.session[2],toDate});
		
		
		serverParams.add(globals.session[0]);
		try {
			Object[] result = (Object[]) globals.client.execute("reports.getProfitLoss" , serverParams);
			viewProfitAndLossReport vplr=new viewProfitAndLossReport(grandParent, SWT.NONE, toDate, result);
			vplr.setSize(grandParent.getClientArea().width,grandParent.getClientArea().height);
		} 
		catch (XmlRpcException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void showProjectStatement(Composite grandParent,String toDate, String selectproject)
	{
		List<Object> serverParams = new ArrayList<Object>();
		serverParams.add(new Object[]{selectproject,globals.session[2], globals.session[2], toDate});
		
		
		serverParams.add(globals.session[0]);
		try {
			Object[] result = (Object[]) globals.client.execute("reports.getProjectStatement" , serverParams);
			viewProjectStatementReport vprs = new viewProjectStatementReport(grandParent, toDate, SWT.NONE, result, selectproject);
			vprs.setSize(grandParent.getClientArea().width,grandParent.getClientArea().height);
		} 
		catch (XmlRpcException e)
		{
			e.printStackTrace();
		}
	}
	public static void showLedger(Composite grandParent, String accountName,String fromDate,String toDate,String ProjectName, boolean narrationFlag, boolean tbDrillDown,boolean psdrilldown, String tbType,String selectproject)
	{
		
					
		ArrayList<Object> serverParams = new ArrayList<Object>();
		//code for sending project name back to ledger report
		serverParams.add(new Object[]{accountName,fromDate,toDate,globals.session[2],ProjectName});
		serverParams.add(globals.session[0]);
			
			try {
					Object[] result_f = (Object[]) globals.client.execute("reports.getLedger", serverParams);
					String oldselectproject = null;
					String oldenddate = null;
					String oldprojectname = null;
					boolean narration =narrationFlag;
					String oldaccname = null;
					String oldfromdate = null ;
					boolean dualledgerflag=false;
					ViewLedgerReport ledger = new ViewLedgerReport(grandParent, SWT.None, result_f,ProjectName,oldprojectname, narrationFlag,narration, accountName,oldaccname,fromDate,oldfromdate,toDate,oldenddate,tbDrillDown,psdrilldown,tbType,selectproject,oldselectproject,dualledgerflag);
			
					System.out.print("Project name is :"+ProjectName);
			//now make an instance of the ledgerReport which is a composite.
			//in the constructor pass this result as a parameter.
			// in the function of constructor of that composite, create a table and run the loop for this grid (result ) which you took as a parameter.
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
	}
		
		
		}
	
	
	
	public static void showDualLedger(Composite grandParent, String accountName,String oldaccName,String fromDate,String oldfromdate,String toDate,String oldenddate,String ProjectName,String oldprojectName, boolean narrationFlag,boolean narration, boolean tbDrillDown,boolean tbflag,boolean psdrilldown,boolean projectflag, String tbType,String tb,String selectproject,String oldselectproject,boolean dualledgerflag,boolean dualflag)
	{
		
		
		ArrayList<Object> serverParams = new ArrayList<Object>();
		//code for sending project name back to ledger report
		serverParams.add(new Object[]{accountName,fromDate,toDate,globals.session[2],ProjectName});
		serverParams.add(globals.session[0]);
		ArrayList<Object> serverParams1 = new ArrayList<Object>();
		//code for sending project name back to ledger report
		serverParams1.add(new Object[]{oldaccName,oldfromdate,oldenddate,globals.session[2],oldprojectName});
		serverParams1.add(globals.session[0]);
		
		try {
				Object[] result_t1 = (Object[]) globals.client.execute("reports.getLedger", serverParams);
				Object[] result_t2 = (Object[]) globals.client.execute("reports.getLedger", serverParams1);
				
				ViewDualLedgr ledger_t1 = new ViewDualLedgr(grandParent, SWT.None, result_t1,result_t2,ProjectName,oldprojectName, narrationFlag,narration, accountName,oldaccName,fromDate,oldfromdate,toDate,oldenddate,tbDrillDown,tbflag,psdrilldown,projectflag,tbType,tb,selectproject,oldselectproject,dualledgerflag,dualflag);
						
				System.out.print("Project name is :"+ProjectName);
		//now make an instance of the ledgerReport which is a composite.
		//in the constructor pass this result as a parameter.
		// in the function of constructor of that composite, create a table and run the loop for this grid (result ) which you took as a parameter.
	} catch (XmlRpcException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
		
		
		
	}

			
		public static void showCashFlow(Composite grandParent, String fromDate, String toDate, String financialFrom)
		{
			ArrayList<Object> serverParams = new ArrayList<Object>();
			serverParams.add(new Object[]{fromDate,toDate,financialFrom});
			serverParams.add(globals.session[0]);
			try {
				Object[] result = (Object[]) globals.client.execute("reports.getCashFlow" , serverParams);
				ViewCashFlowReport CashFlow = new ViewCashFlowReport(grandParent, SWT.NONE, result, fromDate,toDate, financialFrom);
				
			} catch (XmlRpcException e)
			{
				e.printStackTrace();
			}
		}
		
		public static void getAccountReport(Composite grandParent)
		{
			try
			{
				Object[] result=(Object[])globals.client.execute("account.getAccountReport",new Object[]{globals.session[0]});
				AccountReport ar=new AccountReport(grandParent, SWT.NONE, result);
				
			}
			catch (XmlRpcException e)
			{
				e.printStackTrace();
			}
			
		}
	
		public static String[] getBankList()
		{
			try
			{
				Object[] result = (Object[]) globals.client.execute("reports.getBankList",new Object[]{globals.session[0]});
				String[] accounts = new String[result.length];
				for(int i = 0; i<result.length; i++)
				{

					accounts[i] = result[i].toString();
				}
				return accounts;
			}
			catch(Exception e)
			{
				e.getMessage();
				return new String[]{};
			}
		}

		public static void setReconcile(Composite grandParent, Object[][] reconData, String selectaccountname, String FromDate, String Todate, String fromYear, String projectname,Boolean narration )
		{
			ArrayList<Object> serverParams = new ArrayList<Object>();
			serverParams.add(reconData);
			serverParams.add(globals.session[0]);
			
			System.out.println(selectaccountname + FromDate + Todate + projectname);
			System.out.println( globals.session[2]);
			try {
				Object result = globals.client.execute("reports.setBankRecon", serverParams );
				System.out.println( result.toString());
				Boolean successflag = Boolean.valueOf(result.toString());
				if(successflag)
				{
					serverParams.clear();
					serverParams.add(new Object[]{selectaccountname,FromDate,Todate,globals.session[2],projectname});
					serverParams.add(globals.session[0]);
					Object[] result_f = (Object[]) globals.client.execute("reports.updateBankRecon", serverParams);
				
					System.out.println("selectbank:"+selectaccountname+"fromdate"+FromDate+"todate"+Todate+"fin:"+globals.session[2]+"proj:"+projectname);
					updateBankRecon ubr=new updateBankRecon(grandParent, SWT.NONE, selectaccountname,FromDate, Todate,fromYear,projectname,narration, result_f);
				}
			} catch (XmlRpcException e) {
				// TODO Auto-generated catch bloc
				System.out.println("some thing is wrong, chamari");
				e.getMessage();
			}
			
		}
		
		
		public static void showledgerRecon(Composite grandParent, String bankname,String fromDate,String toDate, String projname,boolean narrationFlag)
		{
			
						
			ArrayList<Object> serverParams = new ArrayList<Object>();
			//code for sending project name back to ledger report
			serverParams.add(new Object[]{bankname,fromDate,toDate,globals.session[2],"No Project"});
			
			serverParams.add(globals.session[0]);
			//boolean narration =narrationFlag;
			//String ProjName = projname;
				
				try {
						Object[] result_f = (Object[]) globals.client.execute("reports.updateBankRecon", serverParams);
						LedgerRecon lr = new LedgerRecon(grandParent,SWT.None,bankname,fromDate,toDate,  narrationFlag,projname,result_f);
					
				//now make an instance of the ledgerReport which is a composite.
				//in the constructor pass this result as a parameter.
				// in the function of constructor of that composite, create a table and run the loop for this grid (result ) which you took as a parameter.
			} catch (XmlRpcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
		}
			
			
		}
		
		public static void getClearedUnclearedTransactions (Composite grandParent, String bankname,String fromDate,String toDate,String projname,boolean narration_Flag) 
		{
			Vector<Object> reconResult = new Vector<Object>();
			ArrayList<Object> serverParams = new ArrayList<Object>();
			//code for sending project name back to ledger report
			serverParams.add(new Object[]{bankname,fromDate,toDate,globals.session[2],"No Project"});
			serverParams.add(globals.session[0]);
			System.out.println("getuncleared");
			//System.out.println(bankname + fromDate + toDate + projname+cleared_Flag);	
				try 
				{
					
					/*if(cleared_Flag==true)
					{
						*/Object[] result_cleared=(Object[]) globals.client.execute("reports.getReconciledTransactions",serverParams);
						reconResult.add(result_cleared);
						Object[] result_Uncleared = (Object[]) globals.client.execute("reports.updateBankRecon",serverParams);
						reconResult.add(result_Uncleared);
						//System.out.println("selectbank:"+bankname+"fromdate"+fromDate+"todate"+toDate+"proj:"+projname+"narration"+narration_Flag+"clear:"+cleared_Flag);

						ViewUnclearedAccounts vuca = new ViewUnclearedAccounts(grandParent, SWT.NONE, bankname, fromDate, toDate, narration_Flag,projname,reconResult  );
						
					/*}
					else
					{
						Object[] result_Uncleared = (Object[]) globals.client.execute("reports.updateBankRecon", serverParams);
						reconResult.add(result_Uncleared);
						//LedgerRecon lr = new LedgerRecon(grandParent,SWT.None,bankname,fromDate,toDate, narration_Flag,projname, result);
						ViewUnclearedAccounts vuca = new ViewUnclearedAccounts(grandParent, SWT.NONE, bankname, fromDate, toDate, narration_Flag,projname,cleared_Flag ,reconResult  );

					
					}*/
				//now make an instance of the ledgerReport which is a composite.
				//in the constructor pass this result as a parameter.
				// in the function of constructor of that composite, create a table and run the loop for this grid (result ) which you took as a parameter.
			} catch (XmlRpcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		public static boolean deleteClearedRecon(String accountName,int vouchercode,String clearedDate)
		{
			ArrayList<Object> serverParams = new ArrayList<Object>();
			try {
				serverParams.add(new Object[]{accountName,vouchercode,clearedDate});
				serverParams.add(globals.session[0]);
				
				Object success = globals.client.execute("reports.deleteClearedRecon", serverParams);
				return true;
			} catch (XmlRpcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		
		public static void showUpdateRecon(Composite grandParent, String bankname,String fromDate,String toDate, String projname,boolean narrationFlag)
		{
						
			ArrayList<Object> serverParams = new ArrayList<Object>();
			//code for sending project name back to ledger report
			serverParams.add(new Object[]{bankname,fromDate,toDate,globals.session[2],projname});
			serverParams.add(globals.session[0]);
			boolean narration =false;
				
				try {
						Object[] result_f = (Object[]) globals.client.execute("reports.updateBankRecon", serverParams);
						
						//LedgerRecon lr = new LedgerRecon(grandParent,bankname,fromDate,toDate, SWT.None, result_f,narrationFlag);
				
						//System.out.print("Project name is :"+ProjectName);
				//now make an instance of the ledgerReport which is a composite.
				//in the constructor pass this result as a parameter.
				// in the function of constructor of that composite, create a table and run the loop for this grid (result ) which you took as a parameter.
			} catch (XmlRpcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
		}
			
			
		}
				
		

}

