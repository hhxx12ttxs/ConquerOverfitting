package org.ben;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.math.distribution.BetaDistribution;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import umontreal.iro.lecuyer.probdist.BetaDist;

//import umontreal.iro.lecuyer.probdist.BetaDist;






/**
 * This is the model implementation of FunctionPredictor.
 * 
 *
 * @author 
 */
public class FunctionPredictorNodeModel extends NodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger
	.getLogger(FunctionPredictorNodeModel.class);



	public static final String CFG_CUTOFF = "cutoff";


	public static final String CFG_COL_1 = "col 1";



	public static final String CFG_COL_2 = "col_2";


	public static final String CFG_USE_NEG = "use_neg";



	public static final String CFG_COL_TGT = "tgts";



	public static final String CFG_ERROR = "errors";



	public static final String CFG_COL_4 = "col 4";



	public static final String CFG_ADDS = "adds";



	public static final String CFG_USE_BETA = "beta";


	private final SettingsModelString m_col_1 = 
		new SettingsModelString(CFG_COL_1, null);

	private final SettingsModelString m_col_2 = 
		new SettingsModelString(CFG_COL_2, null);

	private final SettingsModelString m_col_4 = 
		new SettingsModelString(CFG_COL_4, null);

	private final SettingsModelString m_col_tgt = 
		new SettingsModelString(CFG_COL_TGT, null);

	private final SettingsModelBoolean use_neg =
		new SettingsModelBoolean(CFG_USE_NEG, false);

	private SettingsModelBoolean use_beta = new SettingsModelBoolean(
			CFG_USE_BETA, false);

	private SettingsModelDouble m_error = new SettingsModelDouble(
			CFG_ERROR, 0.01);

	private SettingsModelDouble m_adds = new SettingsModelDouble(
			CFG_ADDS, 0.01);

	/**
	 * Constructor for the node model.
	 */
	protected FunctionPredictorNodeModel() {

		// TODO one incoming port and one outgoing port is assumed
		super(3, 1);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		try{

			Date newdate = new Date();
			long starttime = newdate.getTime();

			int rows = inData[0].getRowCount();
			int poscount = inData[1].getRowCount();
			int negcount = inData[2].getRowCount();

			ArrayList<Double> compounds = new ArrayList<Double>(rows);
			HashSet<Double> positive = new HashSet<Double>(poscount);
			HashSet<Double> negative = new HashSet<Double>(negcount);

			//HashMap<Double,double[]> fparray = new HashMap<Double,double[]>(rows);



			int net_col_1 = inData[0].getDataTableSpec().findColumnIndex(
					m_col_1.getStringValue());
			int net_col_2 = inData[0].getDataTableSpec().findColumnIndex(
					m_col_2.getStringValue());
			int net_col_4 = inData[0].getDataTableSpec().findColumnIndex(
					m_col_4.getStringValue());
			int tgt_col_3 = inData[1].getDataTableSpec().findColumnIndex(
					m_col_tgt.getStringValue());

			System.out.println("Column int id are "+net_col_2+" and "+net_col_4);
			int noofprot =net_col_4-net_col_2;


			for(DataRow tgtrow : inData[1])
			{
				double s1 = Double.parseDouble(tgtrow.getCell(tgt_col_3).toString());
				positive.add(s1);
			}
			int tgtcount= positive.size();
			double[][] fparray1 = new double[tgtcount][noofprot];

			double[][] fparray2=null;
			if(use_neg.getBooleanValue())
			{
				for(DataRow tgtrow : inData[2])
				{
					double s1 = Double.parseDouble(tgtrow.getCell(tgt_col_3).toString());
					negative.add(s1);
				}
				int ngtcount= negative.size();
				fparray2 = new double[ngtcount][noofprot];
			}



			int tgtcounter=0;
			for(DataRow rgnRow : inData[0])
			{
				Double c1 = Double.parseDouble(rgnRow.getCell(net_col_1).toString());
				compounds.add(c1);

				//double[] fp = new double[noofprot];

				if(positive.contains(c1))
				{
					for(int i=0;i<noofprot;++i)
					{
						//fp[i]=Double.parseDouble(rgnRow.getCell(net_col_2+i).toString());
						fparray1[tgtcounter][i]=Double.parseDouble(rgnRow.getCell(net_col_2+i).toString());
					}
					//fparray.put(c1,fp);
				}
				if(use_neg.getBooleanValue()){


					if(negative.contains(c1))
					{
						for(int i=0;i<noofprot;++i)
						{
							//fp[i]=Double.parseDouble(rgnRow.getCell(net_col_2+i).toString());
							fparray2[tgtcounter][i]=Double.parseDouble(rgnRow.getCell(net_col_2+i).toString());
						}
					}
				}
			}





			int initcount= compounds.size();

			System.out.println("strings "+initcount+" , targets "+tgtcount);

			int cutoffcount =0;



			Integer[] goodcount = new Integer[initcount];
			Arrays.fill(goodcount,0);

			//calculate prediction function


			double[] mean = new double[noofprot];
			double[] sigma = new double[noofprot];

			double[] meann = new double[noofprot];
			double[] sigman = new double[noofprot];

			boolean[] use = new boolean[noofprot];

			double[] aa = new double[noofprot];
			double[] bb = new double[noofprot];
			double[] betaf = new double[noofprot];
			//BetaDist[] betadists = new BetaDist[noofprot];
			//BetaDist[] betadists1 = new BetaDist[noofprot];
			
			
			

			double eta= Math.max(0.000000000001,m_error.getDoubleValue());
			int adds = (int)Math.round(positive.size()*m_adds.getDoubleValue());
			int adds1 = (int)Math.round(negative.size()*m_adds.getDoubleValue());

			double[][] values = new double[noofprot][positive.size()+adds];
			double[][] values1 = new double[noofprot][negative.size()+adds1];
			
			System.out.println("Calculating statistics with eta = "+eta+" and adding "+adds);

			for(int i=0;i<noofprot;++i)
			{
				
				double meanc=0;
				double sqrc =0;
				double meancn=0;
				double sqrcn =0;
				use[i]=true;

				if(i==0)System.out.println(i);

				for(int j=0;j<positive.size();++j)
				{

					//double[] fp1 =new double[noofprot];

					//fp1=fparray.get(positive.get(j));

					values[i][j]=fparray1[j][i]+eta*(0.5-fparray1[j][i])*2;
					meanc=meanc+values[i][j]	;
					sqrc=sqrc+ Math.pow((values[i][j]),2);
					//System.out.println(fp1[i]);
				}

				if(use_neg.getBooleanValue())
				{
					for(int j=0;j<negative.size();++j)
					{

						//double[] fp1 =new double[noofprot];

						//fp1=fparray.get(positive.get(j));

						values1[i][j]=fparray2[j][i]+eta*(0.5-fparray2[j][i])*2;
						meancn=meancn+values1[i][j]	;
						sqrcn=sqrcn+ Math.pow((values1[i][j]),2);

						//System.out.println(fp1[i]);
					}



					for(int j=0;j<adds1;++j)
					{

						values1[i][j+negative.size()]=0.5;

						//System.out.println(fp1[i]);
					}

				}

				for(int j=0;j<adds;++j)
				{

					values[i][j+positive.size()]=0.5;

					//System.out.println(fp1[i]);
				}
				
				
				

				double meanf= meanc/positive.size();
				
				double sqrf = Math.sqrt(sqrc/positive.size()-meanf*meanf/(positive.size()*positive.size()));

				double meanfn= meancn/negative.size();
				double sqrfn = Math.sqrt(sqrcn/negative.size()-meanfn*meanfn/(negative.size()*negative.size()));

				if(use_neg.getBooleanValue())
				{
					if((meanf-meanfn)*(meanf-meanfn)<(sqrf*sqrfn))
					{
						use[i]=false;
						System.out.print("No distinction between sets");
					}
				}
				//mean[i]= Math.max(meanf,eta);
				//sigma[i]= Math.max(eta,sqrf);

				mean[i]= meanf;
				sigma[i]= sqrf;

				meann[i]= meanfn;
				sigman[i]= sqrfn;

				System.out.print("mean "+i+" is "+mean[i]);
				System.out.print(" stddev "+i+" is "+sigma[i]);

				//aa[i]=mean[i]*(((mean[i]*(1-mean[i]))/sigma[i])-1);
				//bb[i]=(1-mean[i])*(((mean[i]*(1-mean[i]))/sigma[i])-1);




				//BetaDist betadists = BetaDist.getInstanceFromMLE(values[i], positive.size()+adds);

				if(use_neg.getBooleanValue())
				{
					//BetaDist betadists1= BetaDist.getInstanceFromMLE(values1[i], negative.size()+adds1);
				}

				//aa[i]=betadists.getAlpha();
				//bb[i]=betadists.getBeta();
				//betaf[i] = cern.jet.stat.Gamma.beta(aa[i],bb[i]);

				
				
				
				System.out.print(" alpha "+i+" is "+aa[i]);
				System.out.print(" beta "+i+" is "+bb[i]);
				System.out.println(" betafn "+i+" is "+betaf[i]);


			}









			System.out.println();



			Date newdate3 = new Date();
			long time3 = newdate3.getTime();
			long delta3 = (time3 - starttime)/1000;

			System.out.println("Data Time was " + delta3+" s");






			DataColumnSpec[] allColSpecs = new DataColumnSpec[4];
			allColSpecs[0] = 
				new DataColumnSpecCreator("Score", DoubleCell.TYPE).createSpec();

			allColSpecs[1] = 
				new DataColumnSpecCreator("Cmpd", StringCell.TYPE).createSpec();

			allColSpecs[2] = 
				new DataColumnSpecCreator("Positive", StringCell.TYPE).createSpec();

			allColSpecs[3] = 
				new DataColumnSpecCreator("Negative", StringCell.TYPE).createSpec();

			DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
			// the execution context will provide us with storage capacity, in this
			// case a data container to which we will add rows sequentially
			// Note, this container can also handle arbitrary big data tables, it
			// will buffer to disc if necessary.
			BufferedDataContainer container = exec.createDataContainer(outputSpec);
			// let's add m_count rows to it

			System.out.println("output table created");

			System.out.println("Calc loop ");



			int count1 =0;
			int rowcount=0;
			for (DataRow rgnRow : inData[0]) {

				++count1;
				if(count1>(initcount/10)){
					System.out.print(".");
					count1=0;
				}

				String pos = "no";
				String neg = "no";
				if(positive.contains(compounds.get(rowcount))) pos ="yes";

				if(negative.contains(compounds.get(rowcount))) neg ="yes";

				//if(i==0)System.out.println("mean "+mean[i]+" , stdev "+sigma[i]);


				double score =0.0;

				//double[] fp2 = fparray.get(compounds.get(i));

				if(rowcount==0) System.out.println(use_beta.getBooleanValue()+ " , ");

				for(int j=0;j<noofprot;++j)
				{
					if(use[j]){
						//assuming a beta distribution	
						double si = Double.parseDouble(rgnRow.getCell(net_col_2+j).toString());	

						double sinorm = si+ eta*(0.5-si)*2;

						//BetaDist betadists = BetaDist.getInstanceFromMLE(values[j], positive.size()+adds);
						
						//double prob1 = (1/betaf[j])*Math.pow(sinorm, aa[j]-1)*Math.pow(1-sinorm, bb[j]-1);

						//double prob= betadists.density(sinorm);
						double probn=0;

						double err = (si-mean[j])*(si-mean[j])/sigma[j];
						double nerr = (si-meann[j])*(si-meann[j])/sigman[j];

						//if(rowcount==0) System.out.println(j+" , "+err +" , "+nerr);

						if(use_beta.getBooleanValue())
						{
							//score=score+ Math.log(prob);
							if(use_neg.getBooleanValue())
							{
								
								//BetaDist betadists1= BetaDist.getInstanceFromMLE(values1[j], negative.size()+adds1);
																
								//probn= betadists1.density(sinorm);
								score=score+ Math.log(probn);
							}
						}
						else
						{
							score=score+ err;
							if(use_neg.getBooleanValue())
							{

								score=score- nerr;
							}
						}





						//if((rowcount==0)||(rowcount==5)||(rowcount==41)||(rowcount==89))System.out.println(rowcount+" , "+j+" , "+si+" , "+sinorm+" , "+mean[j]+" , "+aa[j]+" , "+bb[j]+" , "+betaf[j]+" , "+prob1+" , "+prob+" , "+score);	
					}
				}


				RowKey key = new RowKey("Row " + rowcount);

				DataCell[] cells = new DataCell[4];
				cells[0] =  new DoubleCell(score); 

				cells[1] = new StringCell(compounds.get(rowcount).toString());

				cells[2] = new StringCell(pos);
				cells[3] = new StringCell(neg);

				DataRow row = new DefaultRow(key, cells);
				container.addRowToTable(row);

				// check if the execution monitor was canceled
				exec.checkCanceled();
				++rowcount;
			}
			// once we are done, we close the container and return its table
			container.close();
			BufferedDataTable out = container.getTable();

			System.out.println();
			System.out.println("No of cutoff compounds was "+cutoffcount);

			Date newdate1 = new Date();
			long time1 = newdate1.getTime();
			long delta1 = (time1 - starttime)/1000;

			System.out.println();
			System.out.println("Final Time was " + delta1+" s");


			return new BufferedDataTable[]{out};



		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}



	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// TODO Code executed on reset.
		// Models build during execute are cleared here.
		// Also data handled in load/saveInternals will be erased here.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
	throws InvalidSettingsException {

		// TODO: check if user settings are available, fit to the incoming
		// table structure, and the incoming types are feasible for the node
		// to execute. If the node can execute in its current state return
		// the spec of its output data table(s) (if you can, otherwise an array
		// with null elements), or throw an exception with a useful user message

		return new DataTableSpec[]{null};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		// TODO save user settings to the config object.



		m_col_1.saveSettingsTo(settings);
		m_col_2.saveSettingsTo(settings);
		m_col_tgt.saveSettingsTo(settings);
		use_neg.saveSettingsTo(settings);
		m_error.saveSettingsTo(settings);
		m_col_4.saveSettingsTo(settings);
		m_adds.saveSettingsTo(settings);
		use_beta.saveSettingsTo(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
	throws InvalidSettingsException {

		// TODO load (valid) settings from the config object.
		// It can be safely assumed that the settings are valided by the 
		// method below.



		m_col_1.loadSettingsFrom(settings);
		m_col_2.loadSettingsFrom(settings);
		m_col_tgt.loadSettingsFrom(settings);
		use_neg.loadSettingsFrom(settings);
		m_error.loadSettingsFrom(settings);
		m_col_4.loadSettingsFrom(settings);
		m_adds.loadSettingsFrom(settings);
		use_beta.loadSettingsFrom(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
	throws InvalidSettingsException {

		// TODO check if the settings could be applied to our model
		// e.g. if the count is in a certain range (which is ensured by the
		// SettingsModel).
		// Do not actually set any values of any member variables.



		m_col_1.validateSettings(settings);
		m_col_2.validateSettings(settings);
		m_col_tgt.validateSettings(settings);
		use_neg.validateSettings(settings);
		m_error.validateSettings(settings);
		m_col_4.validateSettings(settings);
		m_adds.validateSettings(settings);
		use_beta.validateSettings(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		// TODO load internal data. 
		// Everything handed to output ports is loaded automatically (data
		// returned by the execute method, models loaded in loadModelContent,
		// and user settings set through loadSettingsFrom - is all taken care 
		// of). Load here only the other internals that need to be restored
		// (e.g. data used by the views).

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

		// TODO save internal models. 
		// Everything written to output ports is saved automatically (data
		// returned by the execute method, models saved in the saveModelContent,
		// and user settings saved through saveSettingsTo - is all taken care 
		// of). Save here only the other internals that need to be preserved
		// (e.g. data used by the views).

	}

}

