package gnukhata.controllers;

import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.sun.media.sound.SoftAbstractResampler;

import sun.org.mozilla.javascript.ast.CatchClause;
import gnukhata.globals;
import gnukhata.controllers.reportmodels.ProfitAndLossReport;
import gnukhata.controllers.reportmodels.accountReport;
import gnukhata.controllers.reportmodels.cashflowReport;
import gnukhata.controllers.reportmodels.conventionalbalancesheet;
import gnukhata.controllers.reportmodels.conventionalbalancesheet;
import gnukhata.controllers.reportmodels.extendedTrialBalance;
import gnukhata.controllers.reportmodels.grossTrialBalance;
import gnukhata.controllers.reportmodels.netTrialBalance;
import gnukhata.controllers.reportmodels.projectstatement;
import gnukhata.controllers.reportmodels.sourcesandapplicationoffundsbalancesheet;
import gnukhata.controllers.reportmodels.transaction;
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
import gnukhata.views.viewProfitAndLoss;
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
			String strOrgType;
			strOrgType = globals.session[4].toString();
			if(tbType.equals("Conventional Balance Sheet") | tbType.equals("Conventional Statement of Affairs"))
			{	
			ArrayList<conventionalbalancesheet> convbaldata_liabilities = new ArrayList<conventionalbalancesheet>();
			ArrayList<conventionalbalancesheet> convbaldata_asset = new ArrayList<conventionalbalancesheet>();
			String rowFlag = "";
			Integer rows=0;
			Double pnlDr = 0.00;
			
			String CapitalLiabilities;
			String Amount1;
			String Amount2;
			String PropertyAssets;
			String Amount3;
			String Amount4;
			
			Integer netflag = profitloss.length - 4;
			Integer netTotalIndex = profitloss.length - 3;
			//Integer netTotal = profitloss.length - 3;
			Integer ballength=result.length - 13; 
			Integer tol_capital = result.length - 4;
			Integer tol_reserves = result.length - 3;
			Integer tol_loanlia = result.length - 5;
			Integer tol_currlia = result.length - 6;
			Integer tol_fixesAsset = result.length -8;
			Integer tol_miscellaneous = result.length - 7;
			Integer tol_investment = result.length - 11;
			Integer tol_loansasset = result.length - 10;
			Integer tol_currentasset = result.length -9;
			Integer assSrNo = Integer.parseInt(result[result.length-13].toString());
			Integer liaSrNo = Integer.parseInt(result[result.length-12].toString());
			Integer lialength = result.length - 1;
			Integer asslength = result.length - 2;
			Double TotalDr =Double.parseDouble(result[lialength].toString());
			Double TotalCr = Double.parseDouble(result[asslength].toString());
			Double netTotal = Double.parseDouble(profitloss[netTotalIndex].toString());
			pnlDr = netTotal + TotalDr;
			Double pnlCr = Double.parseDouble(profitloss[netTotalIndex].toString()) + TotalCr;
			Double difamount = 0.00;
			Double balancingTotal = 0.00;
			
			if(profitloss[netflag].equals("netProfit"))
			{
				if(TotalDr > pnlCr)
				{
					difamount = TotalDr - pnlCr;
				}
				else
				{
					difamount = pnlCr - TotalDr;
				}
				
			}
			else
			{
				if(TotalCr>pnlDr)
				{
					difamount = TotalCr - pnlDr;
				}
				else
				{
					difamount = pnlDr - TotalCr;
				}

			}
			
			if (assSrNo > liaSrNo)
			{
				rowFlag = "liabilities";
				rows = assSrNo - liaSrNo;
			}
			
			
			if(assSrNo < liaSrNo)
			{
				rowFlag = "asset";
				rows = liaSrNo - assSrNo;
			}

			
			int grpcode1=0;
			int grpcode12=0;
			int grpcode11=0;
			int grpcode3=0;
			int grpcode6=0;
			int grpcode2=0;
			int grpcode10=0;
			int grpcode9=0;
			int grpcode13=0;
			
			// For loop to get the length of the accounts of the respective groups from the result 
			for(int cnt =0; cnt < ballength; cnt++)
			{
				Object[] len = (Object[]) result[cnt];
				if(len[1].equals(1))
				{
					 grpcode1++;
				}
				if(len[1].equals(12))
				{
					 grpcode12++;
				}
				if(len[1].equals(11))
				{
					 grpcode11++;
				}
				if(len[1].equals(3))
				{
					 grpcode3++;
				}
				if(len[1].equals(6))
				{
					grpcode6++;
				
				}
				if(len[1].equals(9))
				{
					grpcode9++;
				
				}if(len[1].equals(2))
				{
					grpcode2++;
				
				}
				if(len[1].equals(10))
				{
					grpcode10++;
				
				}
				if(len[1].equals(13))
				{
					grpcode13++;
				
				}
			}
			
			grpcode3 = grpcode1 + grpcode3;
			grpcode11 = grpcode3 + grpcode11;
			grpcode12 = grpcode11 + grpcode12;
			grpcode6 = grpcode12 + grpcode6;
			grpcode2 = grpcode6 +  grpcode2;
			grpcode9 = grpcode2 + grpcode9;
			grpcode10 = grpcode10 + grpcode9;
			grpcode13 = grpcode9 + grpcode13;
			
			
			/* Code to display the accounts of group Capital,Reserves,Loans,Current Liabilities in the
			Liabilities side of the Balance Sheet */
			if(strOrgType.equals("ngo"))
			{	CapitalLiabilities = "CORPUS";
			Amount1 = "";
			Amount2 = result[tol_capital].toString();
			PropertyAssets ="";
			Amount3 = "";
			Amount4 = "";
			conventionalbalancesheet cnblsht1 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
			convbaldata_liabilities.add(cnblsht1);
			}
			if(strOrgType.equals("profit making"))
			{
				CapitalLiabilities = "CAPITAL";
				Amount1 = "";
				Amount2 = result[tol_capital].toString();
				PropertyAssets ="";
				Amount3 = "";
				Amount4 = "";
				conventionalbalancesheet cnblsht1 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
				convbaldata_liabilities.add(cnblsht1);
			}
			
			
			
			for(int rowcounter =0; rowcounter < grpcode1; rowcounter ++)
			{
				Object[] baldata = (Object[]) result[rowcounter];
				
				if(baldata[1].equals(1))
				{
					CapitalLiabilities= "\t" + baldata[2].toString();
					Amount1= "\t" + baldata[3].toString();
					Amount2 = "";
					Amount3= "";
					Amount4 = "";
					PropertyAssets ="";
					conventionalbalancesheet cnblsht3 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
					convbaldata_liabilities.add(cnblsht3);
				}
				
			}
			
			CapitalLiabilities = "RESERVES";
			Amount2 = result[tol_reserves].toString();
			Amount1 = "";
			PropertyAssets ="";
			Amount3 = "";
			Amount4 = "";
			conventionalbalancesheet cnblsht4 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
			convbaldata_liabilities.add(cnblsht4);
			
		
			for(int rowcounter =grpcode11; rowcounter < grpcode12; rowcounter ++)
			{
				Object[] baldata2 = (Object[]) result[rowcounter];
				
				if(baldata2[1].equals(12))
				{
					CapitalLiabilities= "\t" + baldata2[2].toString();
					Amount1= "\t" + baldata2[3].toString();
					Amount2 = "";
					Amount3= "";
					Amount4 = "";
					PropertyAssets ="";
					conventionalbalancesheet cnblsht5 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
					convbaldata_liabilities.add(cnblsht5);
				}
			}

			
			CapitalLiabilities="LOANS";
			Amount2=result[tol_loanlia].toString();
			Amount1="";
			Amount3 = "";
			Amount4="";
			PropertyAssets ="";
			conventionalbalancesheet cnblsht6 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
			convbaldata_liabilities.add(cnblsht6);
			
			for(int rowcounter =grpcode3; rowcounter < grpcode11; rowcounter ++)
			{
				
				Object[] baldata3 = (Object[]) result[rowcounter];
				
				if(baldata3[1].equals(11))
				{
					CapitalLiabilities =  "\t" + baldata3[2].toString();
					Amount1="\t" + baldata3[3].toString();
					Amount2="";
					Amount3 = "";
					Amount4="";
					PropertyAssets ="";
					conventionalbalancesheet cnblsht7 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
					convbaldata_liabilities.add(cnblsht7);
				}
			}

			CapitalLiabilities="CURRENT LIABILITIES";
			Amount2= result[tol_currlia].toString();
			Amount1="";
			Amount3 = "";
			Amount4="";
			PropertyAssets ="";
			conventionalbalancesheet cnblsht8 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
			convbaldata_liabilities.add(cnblsht8);
			
			for(int rowcounter =grpcode1; rowcounter < grpcode3; rowcounter ++)
			{
				Object[] baldata4 = (Object[]) result[rowcounter];
				
				if(baldata4[1].equals(3))
				{
					CapitalLiabilities= "\t" + baldata4[2].toString();
					Amount1= "\t" + baldata4[3].toString();
					Amount2="";
					Amount3 = "";
					Amount4="";
					PropertyAssets ="";
					conventionalbalancesheet cnblsht9 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
					convbaldata_liabilities.add(cnblsht9);

				}
			}
			
			if(profitloss[netflag].equals("netProfit"))
			{
				if(strOrgType.equals("profit making"))
				{
					CapitalLiabilities= "NET PROFIT";
					Amount1="";
					Amount3 = "";
					Amount4="";
					PropertyAssets ="";
					Amount2= profitloss[netTotalIndex].toString();
					conventionalbalancesheet cnblsht9 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
					convbaldata_liabilities.add(cnblsht9);
				}
				else
				{
					CapitalLiabilities= "NET SURPLUS";
					Amount1="";
					Amount3 = "";
					Amount4="";
					PropertyAssets ="";
					Amount2= profitloss[netTotalIndex].toString();
					conventionalbalancesheet cnblsht9 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
					convbaldata_liabilities.add(cnblsht9);
				}
				
			}

			//for loop add empty row in Liabilities table in order to have as much rows as in the Assets table 
			if(rowFlag.equals("liabilities"))
			{
				for(int i = 0; i <= rows;i++)
				{
					CapitalLiabilities= "";
					Amount1="";
					Amount2="";
					Amount3 = "";
					Amount4="";
					PropertyAssets ="";
					
					conventionalbalancesheet cnblsht10 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
					convbaldata_liabilities.add(cnblsht10);
				}
			}
			
			/* Code to display the accounts of groups Fixed Assets,Investment,Current Assets,Assets Loans,
			Miscellaneous Expenses on the Assets side of the Balance Sheet */
			
			CapitalLiabilities="";
			Amount1="";
			Amount2="";
			PropertyAssets="FIXED ASSETS";
			Amount4= result[tol_fixesAsset].toString();
			Amount3="";
			conventionalbalancesheet cnblsht11 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
			convbaldata_asset.add(cnblsht11);
			
			for(int rowcounter =grpcode12; rowcounter < grpcode6; rowcounter ++)
			{
				Object[] Asset_baldata = (Object[]) result[rowcounter];
					
				if(Asset_baldata[1].equals(6))
				{	CapitalLiabilities="";
					Amount1="";
					Amount2="";
					PropertyAssets= "\t" + Asset_baldata[2].toString();
					Amount3= "\t" + Asset_baldata[3].toString();
					Amount4="";
					conventionalbalancesheet cnblsht12 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
					convbaldata_asset.add(cnblsht12);
				}
				
			}
			
			CapitalLiabilities="";
			Amount1="";
			Amount2="";
			PropertyAssets= "INVESTMENTS";
			Amount4= result[tol_investment].toString();
			Amount3="";
			conventionalbalancesheet cnblsht13 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
			convbaldata_asset.add(cnblsht13);
			
			for(int rowcounter =grpcode2; rowcounter < grpcode9; rowcounter ++)
			{
				Object[] invest_baldata = (Object[]) result[rowcounter];
					
				if(invest_baldata[1].equals(9))
				{
					CapitalLiabilities="";
					Amount1="";
					Amount2="";
					PropertyAssets = "\t" + invest_baldata[2].toString();
					Amount3="\t" + invest_baldata[3].toString();
					Amount4="";
					conventionalbalancesheet cnblsht14 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
					convbaldata_asset.add(cnblsht14);
				}
				
			}	
				CapitalLiabilities="";
				Amount1="";
				Amount2="";
				PropertyAssets="CURRENT ASSETS";
				Amount4= result[tol_currentasset].toString();
				Amount3="";
				conventionalbalancesheet cnblsht15 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
				convbaldata_asset.add(cnblsht15);
				
				for(int rowcounter1 =grpcode6; rowcounter1 < grpcode2; rowcounter1 ++)
				{
					Object[] currasset_baldata = (Object[]) result[rowcounter1];
						
					if(currasset_baldata[1].equals(2))
					{
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets= "\t" + currasset_baldata[2].toString();
						Amount3= "\t" + currasset_baldata[3].toString();
						Amount4="";
						conventionalbalancesheet cnblsht16 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht16);
					}
				}
				
				CapitalLiabilities="";
				Amount1="";
				Amount2="";
				PropertyAssets="ASSET LOANS";
				Amount4= result[tol_loansasset].toString();
				Amount3="";
				conventionalbalancesheet cnblsht17 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
				convbaldata_asset.add(cnblsht17);
				
				for(int rowcounter3 = grpcode9; rowcounter3 < grpcode10;rowcounter3++)
				{
					Object[] loansAssetsdata = (Object[]) result[rowcounter3];
					if(loansAssetsdata[1].equals(10))
					{
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets= "\t" + loansAssetsdata[2].toString();
						Amount3="\t" + loansAssetsdata[3].toString();
						conventionalbalancesheet cnblsht18 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht18);
					}
					
				}
				
				CapitalLiabilities="";
				Amount1="";
				Amount2="";
				PropertyAssets="MISCELLANEOUS EXPENSE";
				Amount4 =result[tol_miscellaneous].toString();
				Amount3="";
				conventionalbalancesheet cnblsht19 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
				convbaldata_asset.add(cnblsht19);
				
				for(int rowcounter4 =grpcode10; rowcounter4 < grpcode13; rowcounter4 ++)
				{
					Object[] misexp_baldata = (Object[]) result[rowcounter4];
						
					if(misexp_baldata[1].equals(13))
					{
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets= "\t" + misexp_baldata[2].toString();
						Amount3= "\t" + misexp_baldata[3].toString();
						Amount4="";
						conventionalbalancesheet cnblsht20 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht20);

					}
				}

				if(profitloss[netflag].equals("netLoss"))
				{
					if(strOrgType.equals("profit making"))
					{
						PropertyAssets="NET LOSS";
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						Amount3= "";
						Amount4= profitloss[netTotalIndex].toString();
						conventionalbalancesheet cnblsht21 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht21);
					}
					else
					{
						PropertyAssets="NET DEFICIT";
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						Amount3= "";
						Amount4= profitloss[netTotalIndex].toString();
						conventionalbalancesheet cnblsht21 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht21);
					}
					
				}
				else
				{
					CapitalLiabilities="";
					Amount1="";
					Amount2="";
					PropertyAssets="";
					Amount3="";
					Amount4="";
					conventionalbalancesheet cnblsht21 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
					convbaldata_asset.add(cnblsht21);
				}
				
				//for loop add empty row in Assets table in order to have as much rows as in the Liabilities table
				if(rowFlag.equals("asset"))
				{
					for(int i = 0; i < rows;i++)
					{
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets="";
						Amount3="";
						Amount4="";
						conventionalbalancesheet cnblsht22 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht22);
					}
				}
				if(profitloss[netflag].equals("netLoss"))
				{
					if(pnlDr > TotalCr)
					{
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets="";
						Amount3="";
						Amount4="";
						conventionalbalancesheet cnblsht23 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht23);
						
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets="";
						Amount3="";
						Amount4="";
						conventionalbalancesheet cnblsht24 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht24);
					}
					
					
					if(difamount != 0.00)
					{
						Amount4=pnlDr.toString();
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets="Total";
						Amount3="";
						conventionalbalancesheet cnblsht25 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht25);
					}
					else
					{
						Amount4=pnlDr.toString();
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets="Total";
						Amount3="";
						conventionalbalancesheet cnblsht25 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht25);
					}
					
					
					
					if(pnlDr < TotalCr)
					{
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets="";
						Amount3="";
						Amount4="";
						conventionalbalancesheet cnblsht28 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_liabilities.add(cnblsht28);
						
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets="";
						Amount3="";
						Amount4="";
						conventionalbalancesheet cnblsht29 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_liabilities.add(cnblsht29);
					}
					
					

					if(difamount != 0.00)
					{	
						Amount2=TotalCr.toString();
						CapitalLiabilities="Total";
						Amount1="";
						Amount4="";
						PropertyAssets="";
						Amount3="";
						conventionalbalancesheet cnblsht30 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_liabilities.add(cnblsht30);
					}
					else
					{
						Amount2=TotalCr.toString();
						CapitalLiabilities="Total";
						Amount1="";
						Amount4="";
						PropertyAssets="";
						Amount3="";
						conventionalbalancesheet cnblsht30 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_liabilities.add(cnblsht30);
					}
					
					
					if(difamount != 0.00)
					{	

					if(pnlDr > TotalCr)
					{	
						CapitalLiabilities="Difference In Opening Balance";
						Amount1="";
						Amount2=difamount.toString();
						PropertyAssets="";
						Amount3="";
						Amount4="";
						conventionalbalancesheet cnblsht33 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_liabilities.add(cnblsht33);
						
						
						balancingTotal = difamount + TotalCr;
						System.out.println("bal total" + balancingTotal);
						
						CapitalLiabilities="Total";
						Amount1="";
						Amount2=balancingTotal.toString();
						PropertyAssets="";
						Amount3="";
						Amount4="";
						conventionalbalancesheet cnblsht34 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_liabilities.add(cnblsht34);
						
					}
				
				
						if(pnlDr < TotalCr)
						{
							CapitalLiabilities="";
							Amount1="";
							Amount2="";
							PropertyAssets="Difference In Opening Balance";
							Amount3="";
							Amount4=difamount.toString();
							conventionalbalancesheet cnblsht35 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
							convbaldata_asset.add(cnblsht35);
							
							balancingTotal = difamount + pnlDr;
							System.out.println("bal total" + balancingTotal);
							
							CapitalLiabilities="";
							Amount1="";
							Amount2="";
							PropertyAssets="Total";
							Amount3="";
							Amount4=balancingTotal.toString();
							conventionalbalancesheet cnblsht31 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
							convbaldata_asset.add(cnblsht31);
							
						}
						
						
					}
					
				}
				else
				{
					if(pnlCr < TotalDr)
					{
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets="";
						Amount3="";
						Amount4="";
						conventionalbalancesheet cnblsht32 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht32);
						
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets="";
						Amount3="";
						Amount4="";
						conventionalbalancesheet cnblsht33 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht33);
					}
					
				  
					if(difamount != 0.00)
					{	
						Amount4=TotalDr.toString();
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets="Total";
						Amount3="";
						conventionalbalancesheet cnblsht34 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht34);					
					}
					else
					{

						Amount4=TotalDr.toString();
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets="Total";
						Amount3="";
						conventionalbalancesheet cnblsht34 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht34);
					}
					
					
					if(pnlCr > TotalDr)
					{
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets="";
						Amount3="";
						Amount4="";
						conventionalbalancesheet cnblsht37 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_liabilities.add(cnblsht37);
						
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets="";
						Amount3="";
						Amount4="";
						conventionalbalancesheet cnblsht38 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_liabilities.add(cnblsht38);
					}
					
					if(difamount != 0.00)
					{
						Amount2=pnlCr.toString();
						CapitalLiabilities="Total";
						Amount1="";
						PropertyAssets="";
						Amount3="";
						Amount4="";
						conventionalbalancesheet cnblsht39 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_liabilities.add(cnblsht39);
					}
					else
					{
						Amount2=pnlCr.toString();
						CapitalLiabilities="Total";
						Amount1="";
						PropertyAssets="";
						Amount3="";
						Amount4="";
						conventionalbalancesheet cnblsht39 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_liabilities.add(cnblsht39);
					}

					
					if(difamount != 0.00)
					{	
					if(TotalDr > pnlCr)
					{
						CapitalLiabilities="Difference In Opening Balance";
						Amount1="";
						Amount2=difamount.toString();
						PropertyAssets="";
						Amount3="";
						Amount4="";
						conventionalbalancesheet cnblsht40 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_liabilities.add(cnblsht40);
						
						
						balancingTotal = difamount + pnlCr;
						System.out.println("bal total" + balancingTotal);
						
						CapitalLiabilities="Total";
						Amount1="";
						Amount2=balancingTotal.toString();
						PropertyAssets="";
						Amount3="";
						Amount4="";
						conventionalbalancesheet cnblsht41 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_liabilities.add(cnblsht41);
					}

					if(TotalDr < pnlCr)
					{

						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets="Difference In Opening Balance";
						Amount3="";
						Amount4=difamount.toString();
						conventionalbalancesheet cnblsht42 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht42);
						
						balancingTotal = difamount + TotalDr;
						System.out.println("bal total" + balancingTotal);
						
						CapitalLiabilities="";
						Amount1="";
						Amount2="";
						PropertyAssets="Total";
						Amount3="";
						Amount4=balancingTotal.toString();
						conventionalbalancesheet cnblsht43 = new conventionalbalancesheet(CapitalLiabilities, Amount1, Amount2, PropertyAssets, Amount3, Amount4);
						convbaldata_asset.add(cnblsht43);
					}
				}
			}	
			
			ViewBalanceSheetReport bsr = new ViewBalanceSheetReport(grandParent, SWT.NONE, endDate, convbaldata_asset,convbaldata_liabilities);
			bsr.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
			}
			if(tbType.equals("Sources & Application of fund") | tbType.equals("Sources & Application of Funds"))
			{
				ArrayList<sourcesandapplicationoffundsbalancesheet> soafbal = new ArrayList<sourcesandapplicationoffundsbalancesheet>();
				String groupName;
				String amount1;
				String amount2;
				
				String rowFlag = "";
				Integer rows=0;
				Double pnlDr = 0.00;
				
				Integer netflag = profitloss.length - 4;
				Integer netTotalIndex = profitloss.length - 3;
				//Integer netTotal = profitloss.length - 3;
				Integer ballength=result.length - 13; 
				
				Integer tol_loanlia = result.length - 5;
				Integer tol_currlia = result.length - 6;
				Integer tol_fixesAsset = result.length -8;
				Integer tol_miscellaneous = result.length - 7;
				Integer tol_investment = result.length - 11;
				Integer tol_loansasset = result.length - 10;
				Integer tol_currentasset = result.length -9;
				Integer assSrNo = Integer.parseInt(result[result.length-13].toString());
				Integer liaSrNo = Integer.parseInt(result[result.length-12].toString());
				Integer lialength = result.length - 1;
				Integer asslength = result.length - 2;
				
				Double 	tol_capital = Double.parseDouble(result[result.length - 4].toString());
				Double  tol_reserves =Double.parseDouble(result[result.length - 3].toString());
				Double tol_capitalAndreserves = tol_capital + tol_reserves;
				
				Double TotalDr =Double.parseDouble(result[lialength].toString());
				Double TotalCr = Double.parseDouble(result[asslength].toString());
				Double netTotal = Double.parseDouble(profitloss[netTotalIndex].toString());
				pnlDr = netTotal + TotalDr;
				Double pnlCr = Float.parseFloat(profitloss[netTotalIndex].toString()) + TotalCr;
				Double difamount = 0.00;
				Double balancingTotal = 0.00;
				
				
				if (assSrNo > liaSrNo)
				{
					rowFlag = "liabilities";
					rows = assSrNo - liaSrNo;
				}
				
				
				if(assSrNo < liaSrNo)
				{
					rowFlag = "asset";
					rows = liaSrNo - assSrNo;
				}

				if(profitloss[netflag].equals("netProfit"))
				{
					if(TotalDr > pnlCr)
					{
						difamount = TotalDr - pnlCr;
					}
					else
					{
						difamount = pnlCr - TotalDr;
					}
				}
				else
				{
					if(TotalCr>pnlDr)
					{
						difamount = TotalCr - pnlDr;
					}
					else
					{
						difamount = pnlDr - TotalCr;
					}
				}
				
				int grpcode1=0;
				int grpcode12=0;
				int grpcode11=0;
				int grpcode3=0;
				int grpcode6=0;
				int grpcode2=0;
				int grpcode10=0;
				int grpcode9=0;
				int grpcode13=0;
				
				// For loop to get the length of the accounts of the respective groups from the result 
				for(int cnt =0; cnt < ballength; cnt++)
				{
					Object[] len = (Object[]) result[cnt];
					if(len[1].equals(1))
					{
						 grpcode1++;
					}
					if(len[1].equals(12))
					{
						 grpcode12++;
					}
					if(len[1].equals(11))
					{
						 grpcode11++;
					}
					if(len[1].equals(3))
					{
						 grpcode3++;
					}
					if(len[1].equals(6))
					{
						grpcode6++;
					
					}
					if(len[1].equals(9))
					{
						grpcode9++;
					
					}if(len[1].equals(2))
					{
						grpcode2++;
					
					}
					if(len[1].equals(10))
					{
						grpcode10++;
					
					}
					if(len[1].equals(13))
					{
						grpcode13++;
					
					}
				}
				

				grpcode3 = grpcode1 + grpcode3;
				grpcode11 = grpcode3 + grpcode11;
				grpcode12 = grpcode11 + grpcode12;
				grpcode6 = grpcode12 + grpcode6;
				grpcode2 = grpcode6 +  grpcode2;
				grpcode10 = grpcode2 + grpcode10;
				grpcode9 = grpcode10 + grpcode9;
				grpcode13 = grpcode9 + grpcode13;
				
				/* Code to display the accounts of group Capital,Reserves,Loans,Current Liabilities in the
				Liabilities side of the Balance Sheet */
				
				
				groupName= "SOURCES";
				amount1="";
				amount2="";
				sourcesandapplicationoffundsbalancesheet sof1 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
				soafbal.add(sof1);
				
				
				/*if(strOrgType == "profit making")
				{					
					groupName="\t\t"+"CAPITAL AND LIABILITIES"+"\t";
					amount1="AMOUNT"+"\t";
					amount2="AMOUNT"+"\t";
					sourcesandapplicationoffundsbalancesheet sof2 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
					soafbal.add(sof2);
					
				}
				else
				{
					groupName="\t\t"+"CORPUS AND LIABILITIES"+"\t";
					amount1="AMOUNT"+"\t";
					amount2="AMOUNT"+"\t";
					sourcesandapplicationoffundsbalancesheet sof2 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
					soafbal.add(sof2);
				}*/
				
				if(strOrgType.equals("ngo"))
				{
					groupName="CORPUS";
					amount1="";
					amount2=tol_capitalAndreserves.toString();
					sourcesandapplicationoffundsbalancesheet sof3 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
					soafbal.add(sof3);
					
				}
				if(strOrgType.equals("profit making"))
				{
					groupName="CAPITAL";
					amount1="";
					amount2=tol_capitalAndreserves.toString();
					sourcesandapplicationoffundsbalancesheet sof3 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
					soafbal.add(sof3);
					
				}
				
				for(int rowcounter =0; rowcounter < grpcode1; rowcounter ++)
				{
					Object[] baldata = (Object[]) result[rowcounter];
					
					if(baldata[1].equals(1))
					{
						groupName="\t" + baldata[2].toString();
						amount1="\t" + baldata[3].toString();
						amount2="";
						sourcesandapplicationoffundsbalancesheet sof4 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof4);

					}
					
					
				}

				
				for(int rowcounter =grpcode11; rowcounter < grpcode12; rowcounter ++)
				{
					Object[] baldata2 = (Object[]) result[rowcounter];
					
					if(baldata2[1].equals(12))
					{
						groupName= "\t" + baldata2[2].toString();
						amount1="\t" + baldata2[3].toString();
						amount2="";
						sourcesandapplicationoffundsbalancesheet sof5 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof5);

					}
				}

				groupName="LOANS";
				amount1="";
				amount2= result[tol_loanlia].toString();
				sourcesandapplicationoffundsbalancesheet sof6 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
				soafbal.add(sof6);
				
				for(int rowcounter =grpcode3; rowcounter < grpcode11; rowcounter ++)
				{
					Object[] baldata3 = (Object[]) result[rowcounter];
					
					if(baldata3[1].equals(11))
					{
						groupName="\t" + baldata3[2].toString();
						amount1="\t" + baldata3[3].toString();
						amount2="";
						sourcesandapplicationoffundsbalancesheet sof7 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof7);
					}
				}

				groupName="CURRENT LIABILITIES";
				amount1="";
				amount2=result[tol_currlia].toString();
				sourcesandapplicationoffundsbalancesheet sof8 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
				soafbal.add(sof8);
				
				for(int rowcounter =grpcode1; rowcounter < grpcode3; rowcounter ++)
				{
					Object[] baldata4 = (Object[]) result[rowcounter];
					
					if(baldata4[1].equals(3))
					{
						groupName="\t" + baldata4[2].toString();
						amount1="\t"+ baldata4[3].toString();
						amount2="";
						sourcesandapplicationoffundsbalancesheet sof9 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof9);
					}
				}
				
				if(profitloss[netflag].equals("netProfit"))
				{
					if(strOrgType.equals("profit making"))
					{
						groupName="NET PROFIT";
						
					}
					else
					{
						groupName="NET SURPLUS";
						
					}
					amount1="";
					amount2=profitloss[netTotalIndex].toString();
					sourcesandapplicationoffundsbalancesheet sof10 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
					soafbal.add(sof10);
				}
				
				if(profitloss[netflag].equals("netLoss"))
				{
					groupName="TOTAL";
					amount1="";
					if(difamount != 0.00)
					{
						amount2=TotalCr.toString();						
					}
					else
					{
						amount2=TotalCr.toString();
					}
					sourcesandapplicationoffundsbalancesheet sof11 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
					soafbal.add(sof11);
					

					
					if(pnlDr > TotalCr)
					{
						if(difamount != 0.00)
						{	
						
						groupName="Difference In Opening Balance";
						amount1="";
						amount2=difamount.toString();
						sourcesandapplicationoffundsbalancesheet sof12 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof12);

						balancingTotal = difamount + TotalCr;
						System.out.println("bal total" + balancingTotal);
						
						groupName="TOTAL";
						amount1="";
						amount2=balancingTotal.toString();
						sourcesandapplicationoffundsbalancesheet sof13 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof13);
						}

					}
				}
				else
				{		
					groupName="TOTAL";
					amount1="";
						if(difamount != 0.00)
						{
							amount2=pnlCr.toString();
							
						}
						else
						{
							amount2=pnlCr.toString();
							
						}
						sourcesandapplicationoffundsbalancesheet sof14 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof14);


						if(TotalDr > pnlCr)
						{
							if(difamount != 0.00)
							{	
							groupName="Difference In Opening Balance";
							amount1="";
							amount2=difamount.toString();
							sourcesandapplicationoffundsbalancesheet sof15 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
							soafbal.add(sof15);
							
							balancingTotal = difamount + pnlCr;
							System.out.println("bal total" + balancingTotal);
							
							groupName="TOTAL";
							amount1="";
							amount2=balancingTotal.toString();
							sourcesandapplicationoffundsbalancesheet sof16 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
							soafbal.add(sof16);
							}
					
						}
				}
				
				//Code to show the Difference in Opening Balance in Liabilities side
				groupName="";
				amount1="";
				amount2="";
				sourcesandapplicationoffundsbalancesheet sof17 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
				soafbal.add(sof17);
				
				/* Code to display the accounts of groups Fixed Assets,Investment,Current Assets,Assets Loans,
				Miscellaneous Expenses on the Assets side of the Balance Sheet */
				
				groupName="APPLICATIONS";
				amount1="";
				amount2="";
				sourcesandapplicationoffundsbalancesheet sof18 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
				soafbal.add(sof18);
				
				
				/*groupName="\t\t"+"PROPERTY AND ASSETS"+"\t";
				amount1="AMOUNT"+"\t";
				amount2="AMOUNT"+"\t";
				sourcesandapplicationoffundsbalancesheet sof19 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
				soafbal.add(sof19);*/
				
				groupName="FIXED ASSETS";
				amount1="";
				amount2=result[tol_fixesAsset].toString();
				sourcesandapplicationoffundsbalancesheet sof20 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
				soafbal.add(sof20);

				
				for(int rowcounter =grpcode12; rowcounter < grpcode6; rowcounter ++)
				{
					Object[] Asset_baldata = (Object[]) result[rowcounter];
						
					if(Asset_baldata[1].equals(6))
					{
						groupName= "\t" + Asset_baldata[2].toString();
						amount1="\t" + Asset_baldata[3].toString();
						amount2="";
						sourcesandapplicationoffundsbalancesheet sof21 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof21);

					}
				}

				groupName="INVESTMENTS";
				amount1="";
				amount2= result[tol_investment].toString();
				sourcesandapplicationoffundsbalancesheet sof22 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
				soafbal.add(sof22);

				for(int rowcounter =grpcode2; rowcounter < grpcode9; rowcounter ++)
				{
					Object[] invest_baldata = (Object[]) result[rowcounter];
						
					if(invest_baldata[1].equals(9))
					{
						groupName="\t" + invest_baldata[2].toString();
						amount1="\t" + invest_baldata[3].toString();
						amount2="";
						sourcesandapplicationoffundsbalancesheet sof23 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof23);
					}
				}
				
				groupName="CURRENT ASSETS";
				amount1="";
				amount2=result[tol_currentasset].toString();
				sourcesandapplicationoffundsbalancesheet sof24 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
				soafbal.add(sof24);
				
				for(int rowcounter =grpcode6; rowcounter < grpcode2; rowcounter ++)
				{
					Object[] currasset_baldata = (Object[]) result[rowcounter];
						
					if(currasset_baldata[1].equals(2))
					{
						groupName="\t" + currasset_baldata[2].toString();
						amount1="\t" + currasset_baldata[3].toString();
						amount2="";
						sourcesandapplicationoffundsbalancesheet sof25 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof25);
						
					}
				}
				
				groupName="ASSET LOANS";
				amount1="";
				amount2=result[tol_loansasset].toString();
				sourcesandapplicationoffundsbalancesheet sof26 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
				soafbal.add(sof26);
				
				for(int rowcounter = grpcode2; rowcounter < grpcode10;rowcounter++)
				{
					Object[] loansAssetsdata = (Object[]) result[rowcounter];
					if(loansAssetsdata[1].equals(10))
					{

						groupName="\t" + loansAssetsdata[2].toString();
						amount1="\t" + loansAssetsdata[3].toString();
						amount2="";
						sourcesandapplicationoffundsbalancesheet sof27 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof27);
					}
					
				}
				
				groupName="MISCELLANEOUS EXPENSE";
				amount1="";
				amount2=result[tol_miscellaneous].toString();
				sourcesandapplicationoffundsbalancesheet sof28 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
				soafbal.add(sof28);
				
				
				for(int rowcounter =grpcode9; rowcounter < grpcode13; rowcounter ++)
				{
					Object[] misexp_baldata = (Object[]) result[rowcounter];
						
					if(misexp_baldata[1].equals(13))
					{
						groupName="\t" + misexp_baldata[2].toString();
						amount1="\t" + misexp_baldata[3].toString();
						amount2="";
						sourcesandapplicationoffundsbalancesheet sof29 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof29);

					}
				}
				
				if(profitloss[netflag].equals("netLoss"))
				{
					if(strOrgType.equals("profit making"))
					{
						groupName="NET LOSS";
						
					}
					else
					{
						groupName="NET DEFICIT";
						
					}
					amount1="";
					amount2=profitloss[netTotalIndex].toString();
					sourcesandapplicationoffundsbalancesheet sof30 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
					soafbal.add(sof30);
				}
				else
				{
					groupName="";
					amount1="";
					amount2="";
					sourcesandapplicationoffundsbalancesheet sof31 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
					soafbal.add(sof31);
				}
				
				if(profitloss[netflag].equals("netLoss"))
				{
					groupName="TOTAL";
					amount1="";
					
					System.out.println("Total" + difamount);
					if(difamount != 0.00)
					{
						amount2=pnlDr.toString();
						
					}
					else
					{
						amount2=pnlDr.toString();
					}
					sourcesandapplicationoffundsbalancesheet sof32 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
					soafbal.add(sof32);
					
					if(pnlDr < TotalCr)
					{
						if(difamount != 0.00)
						{	

						groupName= "Difference In Opening balance";
						amount1="";
						amount2=difamount.toString();
						sourcesandapplicationoffundsbalancesheet sof33 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof33);
						
						balancingTotal = difamount + pnlDr;
						System.out.println("bal total" + balancingTotal);
						groupName="TOTAL";
						amount1="";
						amount2=balancingTotal.toString();
						sourcesandapplicationoffundsbalancesheet sof34 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof34);
					}
					}
					
					
				}
				else
				{
					groupName="TOTAL";
					amount1="";
					if(difamount != 0.00)
					{
						amount2=TotalDr.toString();
						
					}
					else {

						amount2=TotalDr.toString();
						
					}
					sourcesandapplicationoffundsbalancesheet sof35 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
					soafbal.add(sof35);
					if(TotalDr < pnlCr)
					{
						if(difamount != 0.00)
						{	

						groupName= "Difference In Opening balance";
						amount1="";
						amount2=difamount.toString();
						sourcesandapplicationoffundsbalancesheet sof36 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof36);
						
						balancingTotal = difamount + TotalDr;
						System.out.println("bal total" + balancingTotal);
						
						groupName="TOTAL";
						amount1="";
						amount2=balancingTotal.toString();
						sourcesandapplicationoffundsbalancesheet sof37 = new sourcesandapplicationoffundsbalancesheet(groupName, amount1, amount2);
						soafbal.add(sof37);
					}
					}

				}

				ViewSourcesOfFundBalanceSheet fbsr = new ViewSourcesOfFundBalanceSheet(grandParent, SWT.NONE, endDate, soafbal);
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
				ArrayList<netTrialBalance> netData = new ArrayList<netTrialBalance>();
				for(int tbcounter = 0; tbcounter < tbData.length; tbcounter ++ )
				{
					Object[] tbRow = (Object[]) tbData[tbcounter];
					if(tbcounter < tbData.length -1 )
					{
						String srNo = tbRow[0].toString();
						System.out.println(srNo);
						String accountName = tbRow[1].toString();
						System.out.println(accountName);
						String groupName = tbRow[2].toString();
						System.out.println(groupName);
						String drBal = tbRow[3].toString();
						System.out.println(drBal);
						String crBal = tbRow[4].toString();
						System.out.println(crBal);
						netTrialBalance ntb = new netTrialBalance(srNo, accountName, groupName, drBal, crBal);
						netData.add(ntb);
						System.out.println("now from the list of instances");
						System.out.println(netData.get(tbcounter).getSrNo() + ", " + netData.get(tbcounter).getAccountName() );
					}
					else
					{
						String srNo = "";
						String accountName ="Total";
						String groupName = "";
						String drBal =  tbRow[0].toString();
						String crBal = tbRow[1].toString();
						netTrialBalance ntb = new netTrialBalance(srNo, accountName, groupName, drBal, crBal);
						netData.add(ntb);
						
					}
					
				}
				viewTrialBalReport tbr = new viewTrialBalReport(grandParent, endDate, SWT.NONE , netData);
				tbr.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
			}
			if(tbType.equals("Gross Trial Balance"))
			{
				
				Object[] tbData1 = (Object[]) globals.client.execute("reports.getGrossTrialBalance", serverParams);
				ArrayList<grossTrialBalance> grossdata = new ArrayList<grossTrialBalance>();
				String tdr = "";
				String tcr= "";
				double diffbal=0.00;
				double dr=0.00;
				double cr=0.00;
				for(int tbcounter = 0; tbcounter< tbData1.length; tbcounter ++)
				{
					
					Object[] tbRow = (Object[]) tbData1[tbcounter];
					if(tbcounter < tbData1.length-1)
					{
						String srNo = tbRow[0].toString();
						System.out.println(srNo);
						String accountName = tbRow[1].toString();
						System.out.println(accountName);
						String groupName = tbRow[2].toString();
						System.out.println(groupName);
						String totaldr = tbRow[3].toString();
						System.out.println(totaldr);
						String totalcr = tbRow[4].toString();
						System.out.println(totalcr);
						grossTrialBalance gtb = new grossTrialBalance(srNo, accountName, groupName, totaldr, totalcr);
						grossdata.add(gtb);
						System.out.println("now from the list of instances");
						System.out.println(grossdata.get(tbcounter).getSrNo() + ", " + grossdata.get(tbcounter).getAccountName() );
					}
					else
					{
						String srNo = "";
						String accountName ="Total";
						String groupName = "";
						String totaldr =  tbRow[0].toString();
						String totalcr = tbRow[1].toString();
						grossTrialBalance gtb = new grossTrialBalance(srNo, accountName, groupName, totaldr, totalcr);
						grossdata.add(gtb);
						
					}
					
					
				}
				
				/*TableItem closingRow = new TableItem(tblgrosstrialbal , SWT.NONE);
				//closingRow.setFont(new Font(display, "Times New Roman", 10, SWT.NORMAL));
				closingRow.setFont(new Font(display, "Times New Roman",10,SWT.BOLD));
				*///now get the Total Dr and Total Cr.
				//both can be got from the last row of tbdata.
				//get the last row (tbdata.len -1)
				//then access row[0] for TotalDr and row[1] for totalCr.
				//if dr is greater than cr then it is a dr balance.
				//substract the cr amount from dr to get the diff.
				//do exactly the other way round for cr > dr.
				
				/*nf = NumberFormat.getInstance();
				nf.setGroupingUsed(false);
				nf.setMaximumFractionDigits(2);
				nf.setMinimumFractionDigits(2);*/
				
				Object[] lastRow = (Object[]) tbData1[tbData1.length-1 ];
				dr= Double.parseDouble(lastRow[0].toString());
				cr= Double.parseDouble(lastRow[1].toString());
				
				if(dr > cr)
				{
					diffbal = dr - cr;
					String srNo = "";
					String accountName ="Difference In Trial Balance";
					String groupName = "";
					String totaldr =  "";
					String totalcr = Double.toString(diffbal);
					grossTrialBalance gtb = new grossTrialBalance(srNo, accountName, groupName, totaldr, totalcr);
					grossdata.add(gtb);

				}
				if(cr > dr)
				{
					diffbal = cr - dr;
					String srNo = "";
					String accountName ="Difference In Trial Balance";
					String groupName = "";
					String totaldr = Double.toString(diffbal);
					String totalcr = "";
					grossTrialBalance gtb = new grossTrialBalance(srNo, accountName, groupName, totaldr, totalcr);
					grossdata.add(gtb);
				}
				
				/*TableItem totaldrcr = new TableItem(tblgrosstrialbal , SWT.NONE|SWT.SEPARATOR|SWT.BOLD);
				//totaldrcr.setFont(new Font(display, "Times New Roman", 10, SWT.NORMAL));
				totaldrcr.setFont(new Font(display, "Times New Roman",10,SWT.BOLD|SWT.CENTER));*/
				if(dr>cr)
				{
					String srNo = "";
					String accountName ="";
					String groupName = "";
					String totaldr = Double.toString(dr);
					String totalcr = Double.toString(dr);
					grossTrialBalance gtb = new grossTrialBalance(srNo, accountName, groupName, totaldr, totalcr);
					grossdata.add(gtb);
				}
				if(cr>dr)
				{
					String srNo = "";
					String accountName ="";
					String groupName = "";
					String totaldr = Double.toString(cr);
					String totalcr = Double.toString(cr);
					grossTrialBalance gtb = new grossTrialBalance(srNo, accountName, groupName, totaldr, totalcr);
					grossdata.add(gtb);
				}

								
				viewgrosstrialbalreport tbr = new viewgrosstrialbalreport(grandParent,endDate, SWT.NONE , grossdata);
				tbr.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
			}
			if(tbType.equals("Extended Trial Balance"))
			{
				
				Object[] tbData2 = (Object[]) globals.client.execute("reports.getExtendedTrialBalance", serverParams);
				ArrayList<extendedTrialBalance> extendedData = new ArrayList<extendedTrialBalance>();
				String tdr1 = "";
				String tcr1= "";
				double diffbal1=0.00;
				double dr1=0.00;
				double cr1=0.00;
				
				for(int extendedcounter = 0; extendedcounter < tbData2.length;extendedcounter ++)
				{
										
						Object[] tbRow = (Object[]) tbData2[extendedcounter];
						if(extendedcounter < tbData2.length-1)
						{
							String srNo = tbRow[0].toString();
							System.out.println(srNo);
							String accountName = tbRow[1].toString();
							System.out.println(accountName);
							String groupName = tbRow[2].toString();
							System.out.println(groupName);
							String openingBalance = tbRow[3].toString();
							System.out.println(openingBalance);
							String totalDrTransactions = tbRow[4].toString();
							System.out.println(totalDrTransactions);
							String totalCrTransactions = tbRow[5].toString();
							System.out.println(totalCrTransactions);
							String drBalance = tbRow[6].toString();
							System.out.println(drBalance);
							String crBalance = tbRow[7].toString();
							System.out.println(crBalance);
							extendedTrialBalance etb = new extendedTrialBalance(srNo, accountName, groupName, openingBalance, totalDrTransactions, totalCrTransactions, drBalance, crBalance);
							extendedData.add(etb);
							System.out.println("now from the list of instances");
							System.out.println(extendedData.get(extendedcounter).getSrNo() + ", " + extendedData.get(extendedcounter).getAccountName() );
						}
						else
						{
							String srNo = "";
							
							String accountName = "";
							
							String groupName = "";
							
							String openingBalance = "Total";
							
							String totalDrTransactions = tbRow[2].toString();
							
							String totalCrTransactions = tbRow[3].toString();
							
							String drBalance = tbRow[0].toString();
							
							String crBalance = tbRow[1].toString();
							
							extendedTrialBalance etb = new extendedTrialBalance(srNo, accountName, groupName, openingBalance, totalDrTransactions, totalCrTransactions, drBalance, crBalance);
							extendedData.add(etb);

							
						}
						
						
					}
					
					/*TableItem closingRow = new TableItem(tblgrosstrialbal , SWT.NONE);
					//closingRow.setFont(new Font(display, "Times New Roman", 10, SWT.NORMAL));
					closingRow.setFont(new Font(display, "Times New Roman",10,SWT.BOLD));
					*///now get the Total Dr and Total Cr.
					//both can be got from the last row of tbdata.
					//get the last row (tbdata.len -1)
					//then access row[0] for TotalDr and row[1] for totalCr.
					//if dr is greater than cr then it is a dr balance.
					//substract the cr amount from dr to get the diff.
					//do exactly the other way round for cr > dr.
					
					/*nf = NumberFormat.getInstance();
					nf.setGroupingUsed(false);
					nf.setMaximumFractionDigits(2);
					nf.setMinimumFractionDigits(2);*/
					
					Object[] lastRow = (Object[]) tbData2[tbData2.length-1 ];
					dr1= Double.parseDouble(lastRow[0].toString());
					cr1= Double.parseDouble(lastRow[1].toString());
					
					if(dr1 > cr1)
					{
						diffbal1 = dr1 - cr1;
						String srNo = "";
						
						String accountName = "Difference in Trial Balance";
						
						String groupName = "";
						
						String openingBalance = "";
						
						String totalDrTransactions = "";
						
						String totalCrTransactions = "";
						
						String drBalance = "";
						
						String crBalance = Double.toString(diffbal1);
						
						extendedTrialBalance etb = new extendedTrialBalance(srNo, accountName, groupName, openingBalance, totalDrTransactions, totalCrTransactions, drBalance, crBalance);
						extendedData.add(etb);

					}
					if(cr1 > dr1)
					{
						diffbal1 = cr1 - dr1;
						String srNo = "";
						
						String accountName = "Difference in Trial Balance";
						
						String groupName = "";
						
						String openingBalance = "";
						
						String totalDrTransactions = "";
						
						String totalCrTransactions = "";
						
						String drBalance = Double.toString(diffbal1);
						
						String crBalance = "";
						
						extendedTrialBalance etb = new extendedTrialBalance(srNo, accountName, groupName, openingBalance, totalDrTransactions, totalCrTransactions, drBalance, crBalance);
						extendedData.add(etb);

					}
					
					/*TableItem totaldrcr = new TableItem(tblgrosstrialbal , SWT.NONE|SWT.SEPARATOR|SWT.BOLD);
					//totaldrcr.setFont(new Font(display, "Times New Roman", 10, SWT.NORMAL));
					totaldrcr.setFont(new Font(display, "Times New Roman",10,SWT.BOLD|SWT.CENTER));*/
					if(dr1>cr1)
					{
						String srNo = "";
						
						String accountName = "";
						
						String groupName = "";
						
						String openingBalance = "";
						
						String totalDrTransactions = "";
						
						String totalCrTransactions = "";
						
						String drBalance = Double.toString(dr1);
						
						String crBalance = Double.toString(dr1);
						
						extendedTrialBalance etb = new extendedTrialBalance(srNo, accountName, groupName, openingBalance, totalDrTransactions, totalCrTransactions, drBalance, crBalance);
						extendedData.add(etb);

					}
					if(cr1>dr1)
					{
						String srNo = "";
						
						String accountName = "";
						
						String groupName = "";
						
						String openingBalance = "";
						
						String totalDrTransactions = "";
						
						String totalCrTransactions = "";
						
						String drBalance = Double.toString(cr1);
						
						String crBalance = Double.toString(cr1);
						
						extendedTrialBalance etb = new extendedTrialBalance(srNo, accountName, groupName, openingBalance, totalDrTransactions, totalCrTransactions, drBalance, crBalance);
						extendedData.add(etb);
					}
	
					
				
				viewextendedtrialbalreport tbr = new viewextendedtrialbalreport(grandParent,endDate, SWT.NONE , extendedData);
				tbr.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
			}
			
			
		}
		catch(XmlRpcException e)
		{
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
					ArrayList<ProfitAndLossReport> pandlData = new ArrayList<ProfitAndLossReport>();
					
					
					 Integer trialdata = result.length;
					  Integer balllength= result.length -10;

					 Integer grandTotal =result.length -1; 
					 Integer netTotal = result.length -2; 
					 Integer dirincm = result.length -10; 
					 Integer direxp = result.length -9; 
					 Integer indirincm = result.length -8; 
					 Integer indirexp = result.length -7; 
					 Integer grossFlag =result.length -6; 
					 Integer grossProfitloss = result.length -5; 
					 Integer netFlag = result.length -4; 
					 Integer netProfitloss = result.length -3; 
					 

					 int grpcode4=0;
						int grpcode5=0;
						int grpcode7=0;
						int grpcode8=0;
								
						for(int cnt =0; cnt < balllength; cnt++)
						{
							Object[] len = (Object[]) result[cnt];
							if(len[1].equals(4))
							{
								 grpcode4++;
							}
							if(len[1].equals(5))
							{
								 grpcode5++;
							}
							if(len[1].equals(7))
							{
								 grpcode7++;
							}
							if(len[1].equals(8))
							{
								 grpcode8++;
							}
						}
						

						grpcode5 = grpcode4 + grpcode5;
						//grpcode6= grpcode5+ grpcode6;
						grpcode7 = grpcode5 + grpcode7;
						grpcode8 = grpcode7 + grpcode8;
						
						String toheading = "";
						System.out.println(toheading);	
						String accountheading = "DIRECT EXPENDITURE";
						System.out.println(accountheading);
						String amountheading = "";
						System.out.println(amountheading);
						String byheading="";
						System.out.println(byheading);
						String account1heading="DIRECT INCOME";
						System.out.println(account1heading);
						String amount1heading="";
						System.out.println(amount1heading);
						
						ProfitAndLossReport pnlrheading = new ProfitAndLossReport(toheading,accountheading,amountheading,byheading,account1heading,amount1heading);
						pandlData.add(pnlrheading);
						
						 for(int plcounter = 0; plcounter< grpcode5; plcounter ++) 
						 { 		 
							  Object[] plRow = (Object[]) result[plcounter]; 	 
							 

		                   if(plRow[1].equals(4)) 
							{ 
								if(plRow[4].equals("Dr"))
								{
									
									String to = "To,";
									System.out.println(to);	
									String account = plRow[2].toString();
									System.out.println(account);
									String amount = plRow[3].toString();
									System.out.println(amount);
									String by="";
									System.out.println(by);
									String account1="";
									System.out.println(account1);
									String amount1="";
									System.out.println(amount1);
									
									ProfitAndLossReport pnlr = new ProfitAndLossReport(to,account,amount,by,account1,amount1);
									pandlData.add(pnlr);
								
										
									}
								
								if(plRow[4].equals("Cr"))
								{
										
									String to = "";
									System.out.println(to);	
									String account = "";
									System.out.println(account);
									String amount = "";
									System.out.println(amount);
									String by = "By,";
									System.out.println(by);	
									String account1 = plRow[2].toString();
									System.out.println(account1);
									String amount1= plRow[3].toString();
									System.out.println(amount1);
									
									ProfitAndLossReport pnlr = new ProfitAndLossReport(to,account,amount,by,account1,amount1);
									pandlData.add(pnlr);
								   
									}
								
								}
					
						 
		                 if(plRow[1].equals(5) ) 
							{ 
								if(plRow[4].equals("Dr"))
								{
									
									String to = "To,";
									System.out.println(to);	
									String account = plRow[2].toString();
									System.out.println(account);
									String amount = plRow[3].toString();
									System.out.println(amount);
									String by="";
									System.out.println(by);
									String account1="";
									System.out.println(account1);
									String amount1="";
									System.out.println(amount1);
									
									ProfitAndLossReport pnlr = new ProfitAndLossReport(to,account,amount,by,account1,amount1);
									pandlData.add(pnlr);
								
										
									}
								
								if(plRow[4].equals("Cr"))
								{
										
									String to = "";
									System.out.println(to);	
									String account = "";
									System.out.println(account);
									String amount = "";
									System.out.println(amount);
									String by = "By,";
									System.out.println(by);	
									String account1 = plRow[2].toString();
									System.out.println(account1);
									String amount1= plRow[3].toString();
									System.out.println(amount1);
									
									ProfitAndLossReport pnlr = new ProfitAndLossReport(to,account,amount,by,account1,amount1);
									pandlData.add(pnlr);
								   
									}
								
										}
						 }
						 
						 
						 if(result[grossFlag].toString().equals("grossProfit")) 
						 	{ 
						 		if(globals.session[4].equals("profit making")) 
						 		{
						 			
						 			String to = "To,";
									System.out.println(to);	
									String account = "Gross Profit C/F";
									System.out.println(account);
									String amount = result[grossProfitloss].toString();
									System.out.println(amount);
									String by="";
									System.out.println(by);
									String account1="";
									System.out.println(account1);
									String amount1="";
									System.out.println(amount1);
									
									ProfitAndLossReport pnlr = new ProfitAndLossReport(to,account,amount,by,account1,amount1);
									pandlData.add(pnlr);
						 			
						 		
						 	
							   
						 		}
											 
						 		if(globals.session[4].equals("ngo")) 
						 		{ 
						 			String to = "To,";
									System.out.println(to);	
									String account = "Gross Surplus C/F";
									System.out.println(account);
									String amount = result[grossProfitloss].toString();
									System.out.println(amount);
									String by="";
									System.out.println(by);
									String account1="";
									System.out.println(account1);
									String amount1="";
									System.out.println(amount1);
									
									ProfitAndLossReport pnlr = new ProfitAndLossReport(to,account,amount,by,account1,amount1);
									pandlData.add(pnlr);
						 			
							   
						 		} 
						 
						 	}
							
							 if(result[grossFlag].toString().equals("grossLoss")) 
							  { 
							   if(globals.session[4].equals("profit making")) 
							  { 
								   
									String to = "";
									System.out.println(to);	
									String account = "";
									System.out.println(account);
									String amount = "";
									System.out.println(amount);
									String by="By,";
									System.out.println(by);
									String account1="Gross Loss C/F";
									System.out.println(account1);
									String amount1=result[grossProfitloss].toString();
									System.out.println(amount1);
									
									ProfitAndLossReport pnlr = new ProfitAndLossReport(to,account,amount,by,account1,amount1);
									pandlData.add(pnlr);
							 
							  }
							  					 
							   if(globals.session[4].equals("ngo")) 
							  { 
								   
									String to = "";
									System.out.println(to);	
									String account = "";
									System.out.println(account);
									String amount = "";
									System.out.println(amount);
									String by="By,";
									System.out.println(by);
									String account1="Gross Deficit C/F";
									System.out.println(account1);
									String amount1=result[grossProfitloss].toString();
									System.out.println(amount1);
									
									ProfitAndLossReport pnlr = new ProfitAndLossReport(to,account,amount,by,account1,amount1);
									pandlData.add(pnlr);   
							  
							  
							  } 
							   
							  }

		                 if(result[grossFlag].toString().equals("grossProfit")) 
							{ 
								
								String to = "";
								System.out.println(to);	
								String account = "Total Of Amounts";
								System.out.println(account);
								String amount = result[dirincm].toString();
								System.out.println(amount);
								String by="";
								System.out.println(by);
								String account1="Total Of Amounts";
								System.out.println(account1);
								String amount1=result[dirincm].toString();
								System.out.println(amount1);
								
								ProfitAndLossReport pnlr = new ProfitAndLossReport(to,account,amount,by,account1,amount1);
								pandlData.add(pnlr);   	
							
								
							 
											        			 
							} 
							if(result[grossFlag].toString().equals("grossLoss")) 
							{ 
								String to = "";
								System.out.println(to);	
								String account = "Total Of Amounts";
								System.out.println(account);
								String amount = result[direxp].toString();
								System.out.println(amount);
								String by="";
								System.out.println(by);
								String account1="Total Of Amounts";
								System.out.println(account1);
								String amount1=result[direxp].toString();
								System.out.println(amount1);
								
								ProfitAndLossReport pnlr = new ProfitAndLossReport(to,account,amount,by,account1,amount1);
								pandlData.add(pnlr); 	
							
								
											        			 
							} 

				 	
										 
									 
										    
									
					
					String to = "";
					System.out.println(to);	
					String account = "INDIRECT EXPENDITURE";
					System.out.println(account);
					String amount = "";
					System.out.println(amount);
					String by="";
					System.out.println(by);
					String account1="INDIRECT INCOME";
					System.out.println(account1);
					String amount1="";
					System.out.println(amount1);
					
					ProfitAndLossReport pnlr = new ProfitAndLossReport(to,account,amount,by,account1,amount1);
					pandlData.add(pnlr); 	
					
			
					   
						  if(result[grossFlag].toString().equals("grossLoss")) 
						  { 
						   if(globals.session[4].equals("profit making")) 
						  { 
							    String to11 = "To,";
								System.out.println(to11);	
								String account11 = "Gross Loss B/F";
								System.out.println(account11);
								String amount11 = result[grossProfitloss].toString();
								System.out.println(amount11);
								String by12="";
								System.out.println(by12);
								String account12="";
								System.out.println(account12);
								String amount12="";
								System.out.println(amount12);
								
								ProfitAndLossReport pnlr1 = new ProfitAndLossReport(to11,account11,amount11,by12,account12,amount12);
								pandlData.add(pnlr1);    
						  
						
						  }
						  					 
						   if(globals.session[4].equals("ngo")) 
						  { 
						 
							        String to11 = "To,";
									System.out.println(to11);	
									String account11 = "Gross Deficit B/F";
									System.out.println(account11);
									String amount11 = result[grossProfitloss].toString();
									System.out.println(amount11);
									String by12="";
									System.out.println(by12);
									String account12="";
									System.out.println(account12);
									String amount12="";
									System.out.println(amount12);
									
									ProfitAndLossReport pnlr1 = new ProfitAndLossReport(to11,account11,amount11,by12,account12,amount12);
									pandlData.add(pnlr1); 
						  } 
						   
						  }
						  
						  if(result[grossFlag].toString().equals("grossProfit")) 
						  { 
						   if(globals.session[4].equals("profit making")) 
						  { 
						

						        String to11 = "";
								System.out.println(to11);	
								String account11 = "";
								System.out.println(account11);
								String amount11 = "";
								System.out.println(amount11);
								String by12="By,";
								System.out.println(by12);
								String account12="Gross Profit B/F";
								System.out.println(account12);
								String amount12=result[grossProfitloss].toString();
								System.out.println(amount12);
								
								ProfitAndLossReport pnlr1 = new ProfitAndLossReport(to11,account11,amount11,by12,account12,amount12);
								pandlData.add(pnlr1); 
						  }
						  					 
						   if(globals.session[4].equals("ngo")) 
						  { 
						  
							    String to11 = "";
								System.out.println(to11);	
								String account11 = "";
								System.out.println(account11);
								String amount11 = "";
								System.out.println(amount11);
								String by12="By,";
								System.out.println(by12);
								String account12="Gross Surplus B/F";
								System.out.println(account12);
								String amount12=result[grossProfitloss].toString();
								System.out.println(amount12);
								
								ProfitAndLossReport pnlr1 = new ProfitAndLossReport(to11,account11,amount11,by12,account12,amount12);
								pandlData.add(pnlr1); 
						  } 
						   
						  }

						     
						  for(int plcounter = grpcode5; plcounter< grpcode8; plcounter ++) 
							 { 		 
								  Object[] plRow= (Object[]) result[plcounter]; 	 
								 

			                 if(plRow[1].equals(7)) 
								{ 
									if(plRow[4].equals("Dr"))
									{
										
										String To = "To,";
										System.out.println(To);	
										String Account = plRow[2].toString();
										System.out.println(Account);
										String Amount = plRow[3].toString();
										System.out.println(Amount);
										String By="";
										System.out.println(By);
										String Account1="";
										System.out.println(Account1);
										String Amount1="";
										System.out.println(Amount1);
										
										ProfitAndLossReport pnlr2 = new ProfitAndLossReport(To,Account,Amount,By,Account1,Amount1);
										pandlData.add(pnlr2);
									
											
										}
									
									if(plRow[4].equals("Cr"))
									{
											
										String To = "";
										System.out.println(To);	
										String Account = "";
										System.out.println(Account);
										String Amount = "";
										System.out.println(Amount);
										String By = "By,";
										System.out.println(By);	
										String Account1 = plRow[2].toString();
										System.out.println(Account1);
										String Amount1= plRow[3].toString();
										System.out.println(Amount1);
										
										ProfitAndLossReport pnlr2 = new ProfitAndLossReport(To,Account,Amount,By,Account1,Amount1);
										pandlData.add(pnlr2);
									   
										}
									
											}
						
							 
			                 if(plRow[1].equals(8)) 
								{ 
									if(plRow[4].equals("Dr"))
									{
										
										String To = "To,";
										System.out.println(To);	
										String Account = plRow[2].toString();
										System.out.println(Account);
										String Amount = plRow[3].toString();
										System.out.println(Amount);
										String By="";
										System.out.println(By);
										String Account1="";
										System.out.println(Account1);
										String Amount1="";
										System.out.println(Amount1);
										
										ProfitAndLossReport pnlr2 = new ProfitAndLossReport(To,Account,Amount,By,Account1,Amount1);
										pandlData.add(pnlr2);
									
											
										}
									
									if(plRow[4].equals("Cr"))
									{
										String To = "";
										System.out.println(To);	
										String Account = "";
										System.out.println(Account);
										String Amount = "";
										System.out.println(Amount);
										String By = "By,";
										System.out.println(By);	
										String Account1 = plRow[2].toString();
										System.out.println(Account1);
										String Amount1= plRow[3].toString();
										System.out.println(Amount1);
										
										ProfitAndLossReport pnlr2 = new ProfitAndLossReport(To,Account,Amount,By,Account1,Amount1);
										pandlData.add(pnlr2);
									   
									   
										}
									
											}
							 }
						 if((result[grossFlag].toString().equals("grossProfit")) && (result[netFlag].toString().equals("netProfit"))) 
						  { 
						   if(globals.session[4].equals("profit making")) 
						  { 
						
							   
							    String To ="To,";
								System.out.println(To);	
								String Account = "Net Profit";
								System.out.println(Account);
								String Amount = result[netProfitloss].toString();
								System.out.println(Amount);
								String By = "";
								System.out.println(By);	
								String Account1 = "";
								System.out.println(Account1);
								String Amount1= "";
								System.out.println(Amount1);
								
								ProfitAndLossReport pnlr2 = new ProfitAndLossReport(To,Account,Amount,By,Account1,Amount1);
								pandlData.add(pnlr2);
							   
					   
						  }
						  					 
						   if(globals.session[4].equals("ngo")) 
						  { 
						 
							   String To = "To,";
								System.out.println(To);	
								String Account = "Net Surplus";
								System.out.println(Account);
								String Amount = result[netProfitloss].toString();
								System.out.println(Amount);
								String By = "";
								System.out.println(By);	
								String Account1 = "";
								System.out.println(Account1);
								String Amount1= "";
								System.out.println(Amount1);
								
								ProfitAndLossReport pnlr2 = new ProfitAndLossReport(To,Account,Amount,By,Account1,Amount1);
								pandlData.add(pnlr2);
						  } 
						   
						  }
						  
						 
						   if((result[grossFlag].toString().equals("grossLoss")) && (result[netFlag].toString().equals("netProfit"))) 
							  { 
							   
							   
							   if(globals.session[4].equals("profit making")) 
							  { 
							
								    String To = "To,";
									System.out.println(To);	
									String Account = "Net Profit";
									System.out.println(Account);
									String Amount = result[netProfitloss].toString();
									System.out.println(Amount);
									String By = "";
									System.out.println(By);	
									String Account1 = "";
									System.out.println(Account1);
									String Amount1= "";
									System.out.println(Amount1);
									
									ProfitAndLossReport pnlr2 = new ProfitAndLossReport(To,Account,Amount,By,Account1,Amount1);
									pandlData.add(pnlr2);
						   
							  }
							  					 
							   if(globals.session[4].equals("ngo")) 
							  { 
							
								    String To = "To,";
									System.out.println(To);	
									String Account = "Net Surplus";
									System.out.println(Account);
									String Amount = result[netProfitloss].toString();
									System.out.println(Amount);
									String By = "";
									System.out.println(By);	
									String Account1 = "";
									System.out.println(Account1);
									String Amount1= "";
									System.out.println(Amount1);
									
									ProfitAndLossReport pnlr2 = new ProfitAndLossReport(To,Account,Amount,By,Account1,Amount1);
									pandlData.add(pnlr2);
						   
							  } 
							  
							  } 
								  if((result[grossFlag].toString().equals("grossProfit")) && (result[netFlag].toString().equals("netLoss"))) 
								  { 
								   if(globals.session[4].equals("profit making")) 
								  { 
								
									   String To = "";
										System.out.println(To);	
										String Account = "";
										System.out.println(Account);
										String Amount = "";
										System.out.println(Amount);
										String By = "By,";
										System.out.println(By);	
										String Account1 = "Net Loss";
										System.out.println(Account1);
										String Amount1= result[netProfitloss].toString();
										System.out.println(Amount1);
										
										ProfitAndLossReport pnlr2 = new ProfitAndLossReport(To,Account,Amount,By,Account1,Amount1);
										pandlData.add(pnlr2);
							   
							   
								  }
								  					 
								   if(globals.session[4].equals("ngo")) 
								   {
									   String To = "";
										System.out.println(To);	
										String Account = "";
										System.out.println(Account);
										String Amount = "";
										System.out.println(Amount);
										String By = "By,";
										System.out.println(By);	
										String Account1 = "Net Deficit";
										System.out.println(Account1);
										String Amount1= result[netProfitloss].toString();
										System.out.println(Amount1);
										
										ProfitAndLossReport pnlr2 = new ProfitAndLossReport(To,Account,Amount,By,Account1,Amount1);
										pandlData.add(pnlr2);
							   
							   
								  } 
								   
								  }
								  					 
								 				  
								  if((result[grossFlag].toString().equals("grossLoss")) && (result[netFlag].toString().equals("netLoss"))) 
								  { 
								   if(globals.session[4].equals("profit making")) 
								  { 
								
									   String To = "";
										System.out.println(To);	
										String Account = "";
										System.out.println(Account);
										String Amount = "";
										System.out.println(Amount);
										String By = "By,";
										System.out.println(By);	
										String Account1 = "Net Loss";
										System.out.println(Account1);
										String Amount1= result[netProfitloss].toString();
										System.out.println(Amount1);
										
										ProfitAndLossReport pnlr2 = new ProfitAndLossReport(To,Account,Amount,By,Account1,Amount1);
										pandlData.add(pnlr2);
							   
								  }
								  					 
								   if(globals.session[4].equals("ngo")) 
								  { 
								
									   String To = "";
										System.out.println(To);	
										String Account = "";
										System.out.println(Account);
										String Amount = "";
										System.out.println(Amount);
										String By = "By,";
										System.out.println(By);	
										String Account1 = "Net Deficit";
										System.out.println(Account1);
										String Amount1= result[netProfitloss].toString();
										System.out.println(Amount1);
										
										ProfitAndLossReport pnlr2 = new ProfitAndLossReport(To,Account,Amount,By,Account1,Amount1);
										pandlData.add(pnlr2);
							   
								  
								  } 
								   
								  }
								  					 

						  					 
							   if(result[netFlag].toString().equals("netLoss")) 
								{ 
								    String To = "";
									System.out.println(To);	
									String Account = "Total Of Amounts";
									System.out.println(Account);
									String Amount = result[netTotal].toString();
									System.out.println(Amount);
									String By = "";
									System.out.println(By);	
									String Account1 = "Total Of Amounts";
									System.out.println(Account1);
									String Amount1= result[netTotal].toString();
									System.out.println(Amount1);
									
									ProfitAndLossReport pnlr2 = new ProfitAndLossReport(To,Account,Amount,By,Account1,Amount1);
									pandlData.add(pnlr2);
								 	
												        			 
								} 
								if(result[netFlag].toString().equals("netProfit")) 
								{ 
									
									  String To = "";
										System.out.println(To);	
										String Account = "Total Of Amounts";
										System.out.println(Account);
										String Amount = result[grandTotal].toString();
										System.out.println(Amount);
										String By = "";
										System.out.println(By);	
										String Account1 = "Total Of Amounts";
										System.out.println(Account1);
										String Amount1= result[grandTotal].toString();
										System.out.println(Amount1);
										
										ProfitAndLossReport pnlr2 = new ProfitAndLossReport(To,Account,Amount,By,Account1,Amount1);
										pandlData.add(pnlr2);
									 	
								}
							
							 
						  viewProfitAndLossReport vplr=new viewProfitAndLossReport(grandParent, SWT.NONE, toDate,pandlData);
							vplr.setSize(grandParent.getClientArea().width,grandParent.getClientArea().height);
			}
							catch(XmlRpcException e)
							{
								e.printStackTrace();
							}
						} 
			
	
						
							
							
							 /*
								if(plrecord[4].equals("Cr"))
								{
										tbrow.setText(3,"By,"); 
										tbrow.setText(4,tbrecord[2].toString()); 
										tbrow.setText(5,tbrecord[3].toString()); 
										Object[] printableRow = new Object[]{"","","","By,",tbrecord[2].toString(),tbrecord[3].toString()};
									    printPl.add(printableRow);
								   
									}
								
								 if(plrecord[3].equals("0.00"))
								 {
									
									 plrow.dispose();
								 }

						 
										}
					
						 
						
					for(int tbcounter = 0; tbcounter < tbData.length; tbcounter ++ )
					{
						Object[] tbRow = (Object[]) tbData[tbcounter];
						if(tbcounter < tbData.length -1 )
						{
							String srNo = tbRow[0].toString();
							System.out.println(srNo);
							String accountName = tbRow[1].toString();
							System.out.println(accountName);
							String groupName = tbRow[2].toString();
							System.out.println(groupName);
							String drBal = tbRow[3].toString();
							System.out.println(drBal);
							String crBal = tbRow[4].toString();
							System.out.println(crBal);
							netTrialBalance ntb = new netTrialBalance(srNo, accountName, groupName, drBal, crBal);
							netData.add(ntb);
							System.out.println("now from the list of instances");
							System.out.println(netData.get(tbcounter).getSrNo() + ", " + netData.get(tbcounter).getAccountName() );
						}
						else
						{
							String srNo = "";
							String accountName ="Total";
							String groupName = "";
							String drBal =  tbRow[0].toString();
							String crBal = tbRow[1].toString();
							netTrialBalance ntb = new netTrialBalance(srNo, accountName, groupName, drBal, crBal);
							netData.add(ntb);
							
						}
						
					}
					viewTrialBalReport tbr = new viewTrialBalReport(grandParent, endDate, SWT.NONE , netData);
					tbr.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
				}*/
		/*List<Object> serverParams = new ArrayList<Object>();
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
		}*/

	
	public static void showProjectStatement(Composite grandParent,String toDate, String selectproject)
	{
		List<Object> serverParams = new ArrayList<Object>();
		serverParams.add(new Object[]{selectproject,globals.session[2], globals.session[2], toDate});
		
		
		serverParams.add(globals.session[0]);
		try {
			
			Object[] result = (Object[]) globals.client.execute("reports.getProjectStatement" , serverParams);
			ArrayList<projectstatement> prjstmt = new ArrayList<projectstatement>();
			Double total_out=0.00;
			Double total_in=0.00;
			for (int tbcounter = 0; tbcounter < result.length; tbcounter ++) 
			{
				Object [] tbRow = (Object[]) result[tbcounter];
				if (tbcounter<result.length-1)
				{
				String srNo = tbRow[0].toString();
				String accountName = tbRow[1].toString();
				String groupName = tbRow[2].toString();
				String totalOutgoing = tbRow[3].toString();
				total_out=total_out+Double.parseDouble(totalOutgoing);
				String totalIncoming  = tbRow[4].toString();
				total_in=total_in+Double.parseDouble(totalIncoming);
				projectstatement pjstmt = new projectstatement(srNo, accountName, groupName, totalOutgoing, totalIncoming);
				prjstmt.add(pjstmt);
				}
				else
				{
					String srNo = "";
					String accountName = "";
					String groupName = "Total";
					String totalOutgoing = total_out.toString();
					System.out.println("this is the out total"+totalOutgoing);
					String totalIncoming  = total_in.toString();
					System.out.println("this is the in total"+totalIncoming);
					projectstatement pjstmt = new projectstatement(srNo, accountName, groupName, totalOutgoing, totalIncoming);
					prjstmt.add(pjstmt);
				}
				
			}
			
			viewProjectStatementReport vprs = new viewProjectStatementReport(grandParent, toDate, SWT.NONE, prjstmt, selectproject);
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
					ArrayList<transaction> lstLedger = new ArrayList<transaction>();
			for(int ledgercounter =0; ledgercounter < result_f.length; ledgercounter++)
			{
				String voucherdate;
				String  particulars = null;
				String voucherno;
				String dr;
				String cr;
				String narration1;
				String voucherCode =  "";
				Object[] ledgerRow = (Object[]) result_f[ledgercounter];
				Object[] p_list =(Object[])ledgerRow[1] ;
				voucherdate = ledgerRow[0].toString();
				for(int p =0; p<p_list.length; p++)
				{
					particulars= p_list[p].toString()+"\n";
				}
				try {
					particulars = particulars.substring(0, particulars.length()-1 );
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				voucherno = ledgerRow[2].toString();
				dr = ledgerRow[3].toString();
				cr =  ledgerRow[4].toString();
				narration1  = ledgerRow[5].toString();
				voucherCode= ledgerRow[6].toString();
				transaction t = new transaction(voucherdate, particulars, voucherno, dr, cr, narration1,voucherCode );
				lstLedger.add(t);
				
			}
					ViewLedgerReport ledger = new ViewLedgerReport(grandParent, SWT.None,lstLedger ,ProjectName,oldprojectname, narrationFlag,narration, accountName,oldaccname,fromDate,oldfromdate,toDate,oldenddate,tbDrillDown,psdrilldown,tbType,selectproject,oldselectproject,dualledgerflag);
					ledger.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
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
				
				ArrayList<transaction> lstLedger1 = new ArrayList<transaction>();
				for(int ledgercounter =0; ledgercounter < result_t1.length; ledgercounter++)
				{
					String voucherdate;
					String  particulars = null;
					String voucherno;
					String dr;
					String cr;
					String narration1;
					String voucherCode =  "";
					Object[] ledgerRow = (Object[]) result_t1[ledgercounter];
					Object[] p_list1 =(Object[])ledgerRow[1] ;
					voucherdate = ledgerRow[0].toString();
					for(int p =0; p<p_list1.length; p++)
					{
						particulars= p_list1[p].toString()+"\n";
					}
					try {
						particulars = particulars.substring(0, particulars.length()-1 );
					} catch (NullPointerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					voucherno = ledgerRow[2].toString();
					System.out.println("voucher no: "+ voucherno);
					dr = ledgerRow[3].toString();
					System.out.println("dr: "+ dr);
					cr =  ledgerRow[4].toString();
					System.out.println("cr: "+cr);
					narration1  = ledgerRow[5].toString();
					System.out.println("narration1: "+narration1);
					voucherCode= ledgerRow[6].toString();
					transaction t = new transaction(voucherdate, particulars, voucherno, dr, cr, narration1,voucherCode );
					lstLedger1.add(t);
					
				}
				ArrayList<transaction> lstLedger2 = new ArrayList<transaction>();
				for(int ledgercounter =0; ledgercounter < result_t2.length; ledgercounter++)
				{
					String voucherdate;
					String particulars = null;
					String voucherno;
					String dr;
					String cr;
					String narration2;
					String voucherCode =  "";
					Object[] ledgerRow = (Object[]) result_t2[ledgercounter];
					Object[] p_list =(Object[])ledgerRow[1] ;
					voucherdate = ledgerRow[0].toString();
					for(int p =0; p<p_list.length; p++)
					{
						particulars= p_list[p].toString()+"\n";
					}
					try {
						particulars = particulars.substring(0, particulars.length()-1 );
					} catch (NullPointerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					voucherno = ledgerRow[2].toString();
					System.out.println("voucher no: "+ voucherno);
					dr = ledgerRow[3].toString();
					System.out.println("dr: "+ dr);
					cr =  ledgerRow[4].toString();
					System.out.println("cr: "+cr);
					narration2  = ledgerRow[5].toString();
					System.out.println("narration1: "+narration2);
					voucherCode= ledgerRow[6].toString();
					transaction t = new transaction(voucherdate, particulars, voucherno, dr, cr, narration2,voucherCode );
					lstLedger2.add(t);
					
				}
				
				
				ViewDualLedgr ledger_t1 = new ViewDualLedgr(grandParent, SWT.None, lstLedger1,lstLedger2,ProjectName,oldprojectName, narrationFlag,narration, accountName,oldaccName,fromDate,oldfromdate,toDate,oldenddate,tbDrillDown,tbflag,psdrilldown,projectflag,tbType,tb,selectproject,oldselectproject,dualledgerflag,dualflag);
				ledger_t1.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);		
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
				ArrayList<cashflowReport>cashreport = new ArrayList<cashflowReport>();
				Object[] rlist = (Object[]) result[0];
				Object plist[] = (Object[]) result[1];
				Integer difflen = 0;
				if(rlist.length > plist.length)
				{
					difflen =  rlist.length;
				}
				else
				{
					difflen = plist.length;
				}
				for (int cashcounter = 0; cashcounter < difflen; cashcounter++) 
				{
					Object[] receipts = (Object[]) rlist[cashcounter]; 
					Object[] payments = (Object[]) plist[cashcounter];
					String accName;
					String amounts;
					String accName1;
					String amounts1;
					//Code to display records of receipts side
					if(receipts[0].toString().equals("ob"))
					{
						accName= "\t"+receipts[1].toString();
						amounts= receipts[2].toString();
					}
					else
					{
						accName= receipts[0].toString();
						amounts= receipts[1].toString();
					}
					//Code to display records of payment side
					if(payments[0].toString().equals("cb"))
					{
						accName1="\t"+ payments[1].toString();
						amounts1= payments[2].toString();
					}
					else
					{
						accName1= payments[0].toString();
						amounts1= payments[1].toString();
					}
					System.out.println("this is first accname :"+accName);
					System.out.println("this is first amounts :"+amounts);
					System.out.println("this is second accname :"+accName1);
					System.out.println("this is second amounts :"+amounts1);
					cashflowReport cfw = new cashflowReport(accName, amounts, accName1, amounts1);
					cashreport.add(cfw);
				}
				ViewCashFlowReport CashFlow = new ViewCashFlowReport(grandParent, SWT.NONE, cashreport, fromDate,toDate, financialFrom);
				
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
				ArrayList<accountReport> accdata = new ArrayList<accountReport>();
				for (int tbcounter = 0; tbcounter < result.length; tbcounter++)
				{
					Object[] tbRow = (Object[]) result[tbcounter];
					String srNo = tbRow[0].toString();
					System.out.println(srNo);
					String accountName = tbRow[1].toString();
					System.out.println(accountName);
					String groupName = tbRow[2].toString();
					System.out.println(groupName);
					String subgroupName = tbRow[3].toString();
					if(subgroupName.equals("None"))
					{
						subgroupName="";
					}
					System.out.println(subgroupName);
					accountReport accreport =new accountReport(srNo, accountName, groupName, subgroupName);
					accdata.add(accreport);
										
				}
				AccountReport ar=new AccountReport(grandParent, SWT.NONE,accdata);
				ar.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
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

