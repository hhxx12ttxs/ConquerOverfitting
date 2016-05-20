    package cnslab.cnsnetwork;

    import java.awt.BasicStroke;
    import java.awt.Color;
    import java.awt.Font;
    import java.awt.Rectangle;
    import java.awt.event.ActionEvent;
    import java.awt.event.ActionListener;
    import java.awt.geom.Rectangle2D;
    import java.io.File;
    import java.util.ArrayList;
    import java.util.Properties;

    import javax.swing.JButton;
    import javax.swing.JCheckBox;
    import javax.swing.JPanel;

    import org.freehep.graphics2d.VectorGraphics;
    import org.freehep.graphicsio.ps.PSGraphics2D;
    import org.jfree.chart.ChartFactory;
    import org.jfree.chart.ChartPanel;
    import org.jfree.chart.JFreeChart;
    import org.jfree.chart.axis.AxisLocation;
    import org.jfree.chart.axis.CategoryAxis;
    import org.jfree.chart.axis.NumberAxis;
    import org.jfree.chart.block.BlockBorder;
    import org.jfree.chart.plot.CategoryPlot;
    import org.jfree.chart.plot.CombinedDomainXYPlot;
    import org.jfree.chart.plot.PlotOrientation;
    import org.jfree.chart.plot.XYPlot;
    import org.jfree.chart.renderer.AbstractRenderer;
    import org.jfree.chart.renderer.GrayPaintScale;
    import org.jfree.chart.renderer.category.ScatterRenderer;
    import org.jfree.chart.renderer.xy.VectorRenderer;
    import org.jfree.chart.renderer.xy.XYBlockRenderer;
    import org.jfree.chart.renderer.xy.XYErrorRenderer;
    import org.jfree.chart.renderer.xy.XYItemRenderer;
    import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
    import org.jfree.chart.title.PaintScaleLegend;
    import org.jfree.data.Range;
    import org.jfree.data.statistics.DefaultMultiValueCategoryDataset;
    import org.jfree.data.statistics.MultiValueCategoryDataset;
    import org.jfree.data.xy.DefaultXYZDataset;
    import org.jfree.data.xy.VectorSeries;
    import org.jfree.data.xy.VectorSeriesCollection;
    import org.jfree.data.xy.XYDataset;
    import org.jfree.data.xy.XYSeries;
    import org.jfree.data.xy.XYSeriesCollection;
    import org.jfree.data.xy.YIntervalSeries;
    import org.jfree.data.xy.YIntervalSeriesCollection;
    import org.jfree.ui.ApplicationFrame;
    import org.jfree.ui.RectangleEdge;
    import org.jfree.ui.RectangleInsets;
    import org.jfree.ui.RefineryUtilities;

    import ucar.ma2.ArrayChar;
    import ucar.ma2.ArrayDouble;
    import ucar.nc2.NetcdfFile;
    import ucar.nc2.Variable;
    
    // Remarked out when switched for Log4j to Logback for logging.
    // This log4j log file might have been initialized but not used.      
    // import org.apache.log4j.Layout;
    // import org.apache.log4j.FileAppender;
    // import org.apache.log4j.SimpleLayout;
    // import org.apache.log4j.net.SyslogAppender;

    /***********************************************************************
    * Class to plot the recorded data by using Java Free chart.
    *
    * @version
    *   $Date: 2012-08-21 23:46:51 +0200 (Tue, 21 Aug 2012) $
    *   $Rev: 125 $
    *   $Author: jmcohen27 $
    * @author
    *   Yi Dong
    * @author
    *   David Wallace Croft, M.Sc.
    * @author
    *   Jeremy Cohen
    ***********************************************************************/
    public class  PlotResult
      extends ApplicationFrame
      implements ActionListener
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    {
    
    private static final long  serialVersionUID = 1L;
    
    private static final String SEPARATOR = "_"; // See RecWriter.
    
    public int nowValue=-1;

    /**
     * number of single unit ele
     */
    public int suSeries; //number of single unit ele
//  public  static int muSeries; //number of mu unit ele 

    /**
     * number of sub exps
     */
    public  static int numExp; //number of sub exps
    /**
     * number of field electrode
     */
    public  static int numField; //number of field electrode 
    /**
     * number of vector electrode 
     */
    public  static int numVector; //number of vector electrode 
    /**
     * number of intracellular electrodes
     */
    public static int numIntra;
    /**
     * xEdge length
     */
    public  static int xEdge; //xEdge length;
    /**
     * yEdge length;
     */
    public  static int yEdge; //yEdge length;
    /**
     * the names of subexperiments 
     */
    public  static ArrayChar.D2 subexpNames;

    public int picture=0;
    public int currExp;
    public int currField;
    public int currIntra;

    /**
     * single unit names;
     */
    public  ArrayChar.D2 suNames; //single unit names;
    /**
     *  field electrodes names
     */
    public  ArrayChar.D2 fieldNames; //single unit names;
    /**
     *  vector electrodes names
     */
    public  ArrayChar.D2 vectorNames; //single unit names;
    /**
     * intracellular electrodes names
     */
    public ArrayChar.D2 intraNames;

    /**
     * bin, x, y
     */
    public double dataField [][][]; // bin, x, y
    public DefaultXYZDataset show;
    public double fieldMax;
    public int currFrame=0;
    public double fieldBinSize;
    public AbstractRenderer suRender;
    JFreeChart chartsave;
    public String titleField;
    public double xUnit=10.0;
    public double yUnit=10.0;


    // vector plots
    private VectorSeries [] vectorDataField ;
    private double vectorBinSize;
    VectorSeriesCollection vectorxydataset;
    
    // intra plots
    private double intraBinSize;
    private XYSeriesCollection intraVoltageData;
    private XYSeriesCollection intraCurrentData;
    
    public static NetcdfFile ncfile;

//  private AbstractRenderer muRender;

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////

    public static void main(String [] args) throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
      if(args.length <1)
      {
        init();
      }
      else
      {
        init(args[0]);
      }
//    System.out.println(numField);
//    ncfile.close();
//    new PlotResult().suPlot();  
//    new PlotResult().muPlot();  
//    new PlotResult().exportFile();  
      suPlot();
      muPlot();
      fieldPlot();
      vectorPlot();
      intraPlot();
//    exportFile();
      stop();

    }

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    
    /**
     * {@inheritDoc}
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    ////////////////////////////////////////////////////////////////////////
    {
      if(e.getActionCommand().equals("F_next"))
      {
        show.removeSeries(new Integer(currFrame));
        currFrame++;
        if(currFrame == dataField.length) currFrame=0;
        chartsave.setTitle(titleField+" "+String.format("%.0f",(fieldBinSize*currFrame*1000.0))+"-"+String.format("%.0f",(fieldBinSize*(currFrame+1)*1000.0))+" ms");
        show.addSeries(new Integer(currFrame),dataField[currFrame]);
      }
      else if (e.getActionCommand().equals("F_pre"))
      {
        show.removeSeries(new Integer(currFrame));
        currFrame--;
        if(currFrame == -1) currFrame=dataField.length-1;
        chartsave.setTitle(titleField+" "+String.format("%.0f",(fieldBinSize*currFrame*1000.0))+"-"+String.format("%.0f",(fieldBinSize*(currFrame+1)*1000.0))+" ms");
        show.addSeries(new Integer(currFrame),dataField[currFrame]);
      }
      else if (e.getActionCommand().equals("V_pre"))
      {
        vectorxydataset.removeSeries(vectorDataField[currFrame]);
        currFrame--;
        if(currFrame == -1) currFrame=vectorDataField.length-1;
        chartsave.setTitle(titleField+" "+String.format("%.0f",(vectorBinSize*currFrame*1000.0))+"-"+String.format("%.0f",(vectorBinSize*(currFrame+1)*1000.0))+" ms");
        vectorxydataset.addSeries(vectorDataField[currFrame]);
      }
      else if (e.getActionCommand().equals("V_next"))
      {
        vectorxydataset.removeSeries(vectorDataField[currFrame]);
        currFrame++;
        if(currFrame == vectorDataField.length) currFrame = 0;
        chartsave.setTitle(titleField+" "+String.format("%.0f",(vectorBinSize*currFrame*1000.0))+"-"+String.format("%.0f",(vectorBinSize*(currFrame+1)*1000.0))+" ms");
        vectorxydataset.addSeries(vectorDataField[currFrame]);
      }
      else if (e.getActionCommand().equals("SUsave"))
      {
        Properties p = new Properties();
        p.setProperty("PageSize","A5");
        try{
          VectorGraphics g = new PSGraphics2D(new File("pics/SU_"+subexpNames.getString(currExp)+"_Pic"+picture+".eps"), new java.awt.Dimension(800,800)); 
          g.setProperties(p); 
          g.startExport(); 
          //jpanel.print(g); 
          Rectangle2D r2D = new Rectangle2D.Double(0,0, 800,800);
          chartsave.draw(g, r2D);
          g.endExport();
          picture++;
        }catch(Exception x)
        {
          x.printStackTrace();
          System.exit(-1);
        }
      }
      else if (e.getActionCommand().equals("MUsave"))
      {
        Properties p = new Properties();
        p.setProperty("PageSize","A5");
        try{
          VectorGraphics g = new PSGraphics2D(new File("pics/MU_"+subexpNames.getString(currExp)+"_Pic"+picture+".eps"), new java.awt.Dimension(800,800)); 
          g.setProperties(p); 
          g.startExport(); 
          //jpanel.print(g); 
          Rectangle2D r2D = new Rectangle2D.Double(0,0, 800,800);
          chartsave.draw(g, r2D);
          g.endExport();
          picture++;
        }catch(Exception x)
        {
          x.printStackTrace();
          System.exit(-1);
        }
      }
      else if (e.getActionCommand().equals("Fieldsave"))
      {
        Properties p = new Properties();
        p.setProperty("PageSize","A5");
        try{
          VectorGraphics g = new PSGraphics2D(new File("pics/Field_"+subexpNames.getString(currExp)+"_ID"+currField+"_Pic"+picture+".eps"), new java.awt.Dimension(800,800)); 
          g.setProperties(p); 
          g.startExport(); 
          //jpanel.print(g); 
          Rectangle2D r2D = new Rectangle2D.Double(0,0, 800,800);
          chartsave.draw(g, r2D);
          g.endExport();
          picture++;
        }catch(Exception x)
        {
          x.printStackTrace();
          System.exit(-1);
        }
      }
      else if (e.getActionCommand().equals("Vectorsave"))
      {
        Properties p = new Properties();
        p.setProperty("PageSize","A5");
        try{
          VectorGraphics g = new PSGraphics2D(new File("pics/Vector_"+subexpNames.getString(currExp)+"_ID"+currField+"_Pic"+picture+".eps"), new java.awt.Dimension(800,800)); 
          g.setProperties(p); 
          g.startExport(); 
          //jpanel.print(g); 
          Rectangle2D r2D = new Rectangle2D.Double(0,0, 800,800);
          chartsave.draw(g, r2D);
          g.endExport();
          picture++;
        }catch(Exception x)
        {
          x.printStackTrace();
          System.exit(-1);
        }
      }
      else if (e.getActionCommand().equals("Intrasave"))
      {
          Properties p = new Properties();
          p.setProperty("PageSize","A5");
          try{
            VectorGraphics g = new PSGraphics2D(new File("pics/Intracellular_"+subexpNames.getString(currExp)+"_ID"+currIntra+"_Pic"+picture+".eps"), new java.awt.Dimension(800,800)); 
            g.setProperties(p); 
            g.startExport(); 
            Rectangle2D r2D = new Rectangle2D.Double(0,0, 800,800);
            chartsave.draw(g, r2D);
            g.endExport();
            picture++;
          }catch(Exception x)
          {
            x.printStackTrace();
            System.exit(-1);
          }
      }

      int byte0 = -1;
      
      for(int iter=0 ; iter< suSeries; iter++)
      {
        if(e.getActionCommand().equals(new Integer(iter).toString())) byte0 = iter;
      }
      
      if(byte0 >= 0)
      {
        boolean flag = suRender.getItemVisible(byte0, 0);
        suRender.setSeriesVisible(byte0, new Boolean(!flag));
      }
    }

    /**
     * {@inheritDoc}
     * @see javax.swing.JFrame#PlotResult()
     */
    public PlotResult() throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
      super("result");
      init();
//    org.apache.log4j.BasicConfigurator.configure();
//    org.apache.log4j.BasicConfigurator.configure(new FileAppender(new SimpleLayout(),"log/ncfile.log"));
//
//    String filename = "results/simExperiment.nc";
//    NetcdfFile ncfile = NetcdfFile.open(filename);
//    Variable exp = ncfile.findVariable("subExpNum");
//    numExp = exp.readScalarInt();
//    ncfile.close();
    }

    /**
     * {@inheritDoc}
     * @see ApplicationFrame#PlotResult(String)
     */
    public PlotResult(String st) throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
      super(st);
//    org.apache.log4j.BasicConfigurator.configure(new FileAppender(new SimpleLayout(),"log/ncfile.log"));
      //org.apache.log4j.BasicConfigurator.configure();
//    String filename = "results/simExperiment.nc";
//    NetcdfFile ncfile = NetcdfFile.open(filename);
//    Variable exp = ncfile.findVariable("subExpNum");
//    numExp = exp.readScalarInt();
//    ncfile.close();
    }

    /**
     * 
     * 
     * @return single unit data structure
     * 
     * @throws Exception
     */
    public  DefaultMultiValueCategoryDataset readSu() throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
//    String filename = "results/simExperiment.nc";
//    NetcdfFile ncfile = NetcdfFile.open(filename);
      Variable v = ncfile.findVariable("suNames");
      ArrayChar.D2 names = (ArrayChar.D2) v.read();
//    System.out.println(names.getString(0));
      int [] varShape = v.getShape();
      suSeries=varShape[0];
//    System.out.println(varShape[0]+" "+varShape[1]);
      DefaultMultiValueCategoryDataset data = new DefaultMultiValueCategoryDataset();
      Variable exp = ncfile.findVariable("subExpNum");
      int numExp = exp.readScalarInt();
      for( int i = 0 ; i < numExp; i ++)
      {
        Variable trial = ncfile.findVariable("SubExp" + i + SEPARATOR + "trialNum");
        int numTri = trial.readScalarInt();
        for(int j = 0 ; j < numTri ; j++)
        {
          for(int eleId=0; eleId < varShape[0]; eleId++)
          {
            Variable spike = ncfile.findVariable("SubExp" + i + SEPARATOR + "trial" + j + SEPARATOR + names.getString(eleId));
            int [] spikeShape = spike.getShape();
            ArrayDouble.D1 spiData = (ArrayDouble.D1) spike.read();

            ArrayList arrlist = new ArrayList();
            for( int temp=0 ; temp< spikeShape[0]; temp++)
            {
              arrlist.add(spiData.get(temp)*1000.0);
            }
            data.add(arrlist,names.getString(eleId),"Sub"+i+" Trial"+j);
          }
        }
      }
//    ncfile.close();
      return data;
    }

    /**
     * 
     * 
     * @param expId
     * @return single unit data structure
     * 
     * @throws Exception
     */
    public  DefaultMultiValueCategoryDataset readSu(int expId) throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
//    String filename = "results/simExperiment.nc";
//    NetcdfFile ncfile = NetcdfFile.open(filename);
      Variable v = ncfile.findVariable("suNames");
      suNames = (ArrayChar.D2) v.read();

      //    System.out.println(names.getString(0));
      int [] varShape = v.getShape();
      suSeries=varShape[0];
      //    System.out.println(varShape[0]+" "+varShape[1]);
      DefaultMultiValueCategoryDataset data = new DefaultMultiValueCategoryDataset();
      Variable trial = ncfile.findVariable("SubExp" + expId + SEPARATOR + "trialNum");
      int numTri = trial.readScalarInt();
      for(int j = 0 ; j < numTri ; j++)
      {
        for(int eleId=0; eleId < varShape[0]; eleId++)
        {
          Variable spike = ncfile.findVariable("SubExp" + expId + SEPARATOR + "trial" + j
        		  + SEPARATOR + suNames.getString(eleId));
          if(spike!=null)
          {
            int [] spikeShape = spike.getShape();
            ArrayDouble.D1 spiData = (ArrayDouble.D1) spike.read();

            ArrayList arrlist = new ArrayList();
            for( int temp=0 ; temp< spikeShape[0]; temp++)
            {
              arrlist.add(spiData.get(temp)*1000.0);
            }
            data.add(arrlist,suNames.getString(eleId),"Sub"+expId+" Trial"+j);
          }
        }
      }
//    ncfile.close();
      return data;
    }

    /**
     * 
     *  populate the field electrode data structure.
     * @param expId
     * @param fieldId
     * 
     * @throws Exception
     */
    public  void readField(int expId, int fieldId) throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
//    String filename = "results/simExperiment.nc";
//    NetcdfFile ncfile = NetcdfFile.open(filename);
      Variable v = ncfile.findVariable("fieldNames");
      fieldNames = (ArrayChar.D2) v.read();

      Variable bin = ncfile.findVariable("fieldBinSize");
      fieldBinSize = bin.readScalarDouble();


      Variable ratedata = ncfile.findVariable("SubExp" + expId + SEPARATOR + fieldNames.getString(fieldId));
      int [] rShape= ratedata.getShape(); //x,y,bins
//    System.out.println(rShape[2]+" "+rShape[0]+" "+rShape[1]);
      fieldMax=0.0;

      int xStep,yStep,xReso,yReso;
      double xMul,yMul;

      if(xEdge < rShape[0])
      {
        xStep = 1;
        xMul = (double)xEdge / (double)rShape[0] ;
        xReso = rShape[0];
        xUnit=1.0;
      }
      else
      {
        xStep = xEdge / rShape[0];
        xReso = xEdge;
        xMul = 1.0;
        xUnit=(double)xStep;
      }

      if(yEdge < rShape[1])
      {
        yStep = 1;
        yMul = (double)yEdge / (double)rShape[1] ;
        yReso = rShape[1];
        yUnit=1.0;
      }
      else
      {
        yStep = yEdge / rShape[1];
        yReso = yEdge;
        yMul = 1.0;
        yUnit=(double)yStep;
      }


      int factorx=xEdge/rShape[0];
      int factory=yEdge/rShape[1];
      if(factorx <1) factorx=1;
      if(factory <1) factory=1;

//    dataField = new double [rShape[2]][3][rShape[0]*rShape[1]];
      dataField = new double [rShape[2]][3][xReso*yReso];

      ArrayDouble.D3 rate = (ArrayDouble.D3) ratedata.read();

      for(int x=0; x < xReso; x++)
      {
        for(int y=0; y< yReso;y++)
        {
          for(int t=0; t< rShape[2]; t++)
          {
            dataField[t][0][x*yReso+y]=(double)x*(double)xMul- (double)(xStep-1)/2.0;
            dataField[t][1][x*yReso+y]=(double)y*(double)yMul- (double)(yStep-1)/2.0;
//          dataField[t][0][x*yReso+y]=(double)x*(double)xMul;
//          dataField[t][1][x*yReso+y]=(double)y*(double)yMul;
            dataField[t][2][x*yReso+y]=rate.get(x/xStep,y/yStep,t)/fieldBinSize;
            if(dataField[t][2][x*yReso+y]>fieldMax) fieldMax=dataField[t][2][x*yReso+y];
          }
        }
      }

      /*
    for(int x=0; x < rShape[0]; x++)
    {
      for(int y=0; y< rShape[1]; y++)
      {
        for(int t=0; t< rShape[2]; t++)
        {
          dataField[t][0][x*rShape[1]+y]=(double)x;
          dataField[t][1][x*rShape[1]+y]=(double)y;
          dataField[t][2][x*rShape[1]+y]=rate.get(x,y,t)/fieldBinSize;
          if(dataField[t][2][x*rShape[1]+y]>fieldMax) fieldMax=dataField[t][2][x*rShape[1]+y];
        }
      }
    }
       */
//    ncfile.close();
    }

    /**
     * 
     *  populate the vector electrode data structure.
     * @param expId
     * @param fieldId
     * 
     * @throws Exception
     */
    public  void readVector(int expId, int fieldId) throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
      Variable v = ncfile.findVariable("vectorNames");
      vectorNames = (ArrayChar.D2) v.read();

      Variable bin = ncfile.findVariable("vectorBinSize");
      vectorBinSize = bin.readScalarDouble();


      Variable rateX = ncfile.findVariable("SubExp" + expId + SEPARATOR + vectorNames.getString(fieldId) + SEPARATOR + "X");
      Variable rateY = ncfile.findVariable("SubExp" + expId + SEPARATOR + vectorNames.getString(fieldId) + SEPARATOR + "Y");

      ArrayDouble.D3 ratedataX = (ArrayDouble.D3) rateX.read();
      ArrayDouble.D3 ratedataY = (ArrayDouble.D3) rateY.read();

      int [] rShape= ratedataX.getShape(); //x,y,bins



      vectorDataField = new VectorSeries [rShape[2]];

      for(int t=0; t<rShape[2]; t++)
      {
        vectorDataField[t] = new VectorSeries("S"+t);
      }

      int xStep,yStep,xReso,yReso;
      double xMul,yMul;

      if(xEdge < rShape[0])
      {
        xStep = 1;
        xMul = (double)xEdge / (double)rShape[0] ;
        xReso = rShape[0];
        xUnit=1.0;
      }
      else
      {
        xStep = xEdge / rShape[0];
        xReso = xEdge;
        xMul = 1.0;
        xUnit=(double)xStep;
      }

      if(yEdge < rShape[1])
      {
        yStep = 1;
        yMul = (double)yEdge / (double)rShape[1] ;
        yReso = rShape[1];
        yUnit=1.0;
      }
      else
      {
        yStep = yEdge / rShape[1];
        yReso = yEdge;
        yMul = 1.0;
        yUnit=(double)yStep;
      }



      fieldMax=0.0;
      for(int x=0; x < rShape[0]; x++)
      {
        for(int y=0; y< rShape[1]; y++)
        {
          for(int t=0; t<rShape[2]; t++)
          {
            double currL = Math.sqrt(ratedataX.get(x,y,t)*ratedataX.get(x,y,t) + ratedataY.get(x,y,t)*ratedataY.get(x,y,t));
            if( currL >fieldMax ) fieldMax=currL;
          }

        }
      }

      for(int t=0; t<rShape[2]; t++)
      {
        for(int x=0; x < rShape[0]; x++)
        {
          for(int y=0; y< rShape[1]; y++)
          {
            vectorDataField[t].add(x*xStep,y*yStep, ratedataX.get(x,y,t) ,ratedataY.get(x,y,t));
          }
        }
      }

    }


    /**
     * 
     * 
     * @param expId
     * @return multiunit electrod data
     * 
     * @throws Exception
     */
    public  XYDataset readMU(int expId) throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
//    String filename = "results/simExperiment.nc";
//    NetcdfFile ncfile = NetcdfFile.open(filename);
      Variable v = ncfile.findVariable("muNames");
      suNames = (ArrayChar.D2) v.read();
//    System.out.println(names.getString(0));
      int [] varShape = v.getShape();
      suSeries=varShape[0];
//    System.out.println(varShape[0]+" "+varShape[1]);
      YIntervalSeries [] eles = new YIntervalSeries[suSeries];
      for(int i=0; i < suSeries; i++)
      {
        eles[i] = new YIntervalSeries(suNames.getString(i));
      }


      Variable bin = ncfile.findVariable("muBinSize");
      double binSize = bin.readScalarDouble();


      for(int eleId=0; eleId < suSeries; eleId++)
      {
        Variable ratedata = ncfile.findVariable("SubExp" + expId + SEPARATOR + suNames.getString(eleId)+ SEPARATOR + "ALL");
        int [] rShape= ratedata.getShape();
        ArrayDouble.D2 rate = (ArrayDouble.D2) ratedata.read();
        double [] mean = new double [rShape[1]];
        double [] sd = new double [rShape[1]];
        for(int y=0; y< rShape[1]; y++)
        {
          double [] tmp = new double[rShape[0]];
          for(int x=0; x< rShape[0]; x++)
          {
            tmp[x]=rate.get(x,y)/binSize;
          }
          mean[y]=FunUtil.mean(tmp);
          sd[y] = FunUtil.sd(tmp);
          eles[eleId].add((y+1)*binSize*1000.0,mean[y],mean[y]-sd[y],mean[y]+sd[y]);
        }
      }

//    ncfile.close();
      YIntervalSeriesCollection data = new YIntervalSeriesCollection();

      for(int i=0; i < suSeries; i++)
      {
        data.addSeries(eles[i]);
      }
      return data;
    }
    
    /**
     * 
     * 
     * @param expId
     * @return intracellular recorder data
     * 
     * @throws Exception
     */
    public  void readIntra ( int expId, int intraId ) throws Exception
    ////////////////////////////////////////////////////////////////////////
    {	
      Variable intraNamesVar = ncfile.findVariable("intraNames");
      intraNames = (ArrayChar.D2) intraNamesVar.read();

      Variable intraBinSizeVar = ncfile.findVariable("intraBinSize");
      intraBinSize = intraBinSizeVar.readScalarDouble();
      
      Variable numTrialsVar = 
          ncfile.findVariable("SubExp" + expId + SEPARATOR + "trialNum");
      int numTrials = numTrialsVar.readScalarInt();
      
      intraVoltageData = new XYSeriesCollection();
      intraCurrentData = new XYSeriesCollection();
      
      for (int trialId = 0; trialId < numTrials; trialId ++)
      {

     	  Variable voltageVar = ncfile.findVariable(
     	      "SubExp" + expId + SEPARATOR + 
     	      "trial" + trialId + SEPARATOR + 
     	      intraNames.getString(intraId) + SEPARATOR + 
     	      "V");
     	  Variable currentVar = ncfile.findVariable(
     	      "SubExp" + expId + SEPARATOR + 
     	      "trial" + trialId + SEPARATOR + 
     	      intraNames.getString(intraId) + SEPARATOR + 
     	      "C");
   	  
        ArrayDouble.D1 voltageData = (ArrayDouble.D1) voltageVar.read();
        ArrayDouble.D2 currentData = (ArrayDouble.D2) currentVar.read();
      
        int numVoltageBins = voltageData.getShape()[0];
        int numCurrentBins = currentData.getShape()[1];
        int numCurrentChannels = currentData.getShape()[0];
                        
        XYSeries voltage = new XYSeries("Voltage (trial " + trialId + ")");
        XYSeries[] current = new XYSeries[numCurrentChannels];
      
        for (int i = 0; i < numVoltageBins; i++)
        {
    	    double time = intraBinSize * i;
    	    voltage.add(time, voltageData.get(i));
        }
         
        for (int channel = 0; channel < numCurrentChannels; channel++)
        {
      	  current[channel] = new XYSeries("Current" + channel + 
      	      " (trial " + trialId + ")");
    	    for (int i = 0; i < numCurrentBins; i++)
    	    {
        	  double time = intraBinSize * i;
        	  current[channel].add(time, currentData.get(channel, i) * 10E9);
    	    }
        }
           
      intraVoltageData.addSeries (voltage);
      
      for (int i = 0; i  < current.length; i++)
    	  intraCurrentData.addSeries(current[i]);
      }
    }

    /**
     * 
     * 
     * @param i
     * @return Panel for singlue unit
     * 
     * @throws Exception
     */
    public  JPanel createSuPanel(int i) throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
      JFreeChart jfreechart = createSUChart(readSu(i));
      return new ChartPanel(jfreechart);
    }

    /**
     * 
     * 
     * @param multivaluecategorydataset
     * @return Charte for single unit electrode
     */
    private  JFreeChart createSUChart(MultiValueCategoryDataset multivaluecategorydataset)
    ////////////////////////////////////////////////////////////////////////
    {
      ScatterRenderer shape = new ScatterRenderer();
      suRender = shape;
      for(int i=0; i < suSeries; i++)
      {
        if(i==nowValue)
        {
          suRender.setSeriesVisible(i, new Boolean(true));
        }
        else
        {
          suRender.setSeriesVisible(i, new Boolean(false));
        }
        shape.setSeriesShape(i, new Rectangle(2, 4));
        shape.setSeriesShape(i, new Rectangle(2, 4));
      }

      CategoryPlot categoryplot = new CategoryPlot(multivaluecategorydataset, new CategoryAxis("Trial"), new NumberAxis("Time"), shape);
      categoryplot.setBackgroundPaint(Color.lightGray);
      categoryplot.setOrientation(PlotOrientation.HORIZONTAL);
      categoryplot.setDomainGridlinePaint(Color.white);
      categoryplot.setRangeGridlinePaint(Color.white);
      categoryplot.setAxisOffset(new RectangleInsets(4D, 4D, 4D, 4D));
      JFreeChart jfreechart = new JFreeChart(categoryplot);
      jfreechart.setBackgroundPaint(Color.white);
      chartsave = jfreechart;
      return jfreechart;
    }

    /**
     * 
     * 
     * @return chart for field electrode
     */
    private JFreeChart createFieldChart()
    ////////////////////////////////////////////////////////////////////////
    {
      show = new DefaultXYZDataset();
      show.addSeries(new Integer(currFrame),dataField[currFrame]);
//    System.out.println(fieldMax);

      NumberAxis numberaxis = new NumberAxis("X");
//    numberaxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
      numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      //numberaxis.setTickUnit( new NumberTickUnit(xUnit*5)); 
      numberaxis.setLowerMargin(0.0D);
      numberaxis.setUpperMargin(0.0D);
      numberaxis.setAxisLinePaint(Color.white);
      numberaxis.setTickMarkPaint(Color.white);
      NumberAxis numberaxis1 = new NumberAxis("Y");
      numberaxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//    numberaxis1.setStandardTickUnits(NumberAxis.createStandardTickUnits());
      //numberaxis1.setTickUnit( new NumberTickUnit(yUnit*5)); 
      numberaxis1.setLowerMargin(0.0D);
      numberaxis1.setUpperMargin(0.0D);
      numberaxis1.setAxisLinePaint(Color.white);
      numberaxis1.setTickMarkPaint(Color.white);

      XYBlockRenderer xyblockrenderer = new XYBlockRenderer();
//    System.out.println("fieldMax="+fieldMax);
//    System.out.println("fieldMax="+fieldMax);
      if(fieldMax==0.0)fieldMax=1.0;

      GrayPaintScale graypaintscale = new GrayPaintScale(0, fieldMax);
      xyblockrenderer.setPaintScale(graypaintscale);
      XYPlot xyplot = new XYPlot(show, numberaxis, numberaxis1, xyblockrenderer);

      xyplot.setBackgroundPaint(Color.lightGray);
      xyplot.setDomainGridlinesVisible(false);
      xyplot.setRangeGridlinePaint(Color.white);
      xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
      xyplot.setOutlinePaint(Color.blue);
      JFreeChart jfreechart = new JFreeChart(titleField+" "+String.format("%.0f",(fieldBinSize*currFrame*1000.0))+"-"+String.format("%.0f",(fieldBinSize*(currFrame+1)*1000.0))+" ms", xyplot);
      chartsave = jfreechart;
//    jfreechart.setTitle("Field Plot "+(fieldBinSize*currFrame*1000.0)+"-"+(fieldBinSize*(currFrame+1)*1000.0));
      jfreechart.removeLegend();

      NumberAxis numberaxis2 = new NumberAxis("Scale");
      numberaxis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      numberaxis2.setRange(new Range(0,fieldMax));
      numberaxis2.setAxisLinePaint(Color.white);
      numberaxis2.setTickMarkPaint(Color.white);
      numberaxis2.setTickLabelFont(new Font("Dialog", 0, 7));
      PaintScaleLegend paintscalelegend = new PaintScaleLegend(graypaintscale, numberaxis2);
      paintscalelegend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
      paintscalelegend.setAxisOffset(5D);
      paintscalelegend.setMargin(new RectangleInsets(5D, 5D, 5D, 5D));
      paintscalelegend.setFrame(new BlockBorder(Color.red));
      paintscalelegend.setPadding(new RectangleInsets(10D, 10D, 10D, 10D));
      paintscalelegend.setStripWidth(10D);
      paintscalelegend.setPosition(RectangleEdge.RIGHT);
      paintscalelegend.setBackgroundPaint(new Color(120, 120, 180));
      jfreechart.addSubtitle(paintscalelegend);
      jfreechart.setBackgroundPaint(new Color(180, 180, 250));
      return jfreechart;
    }

    /**
     * 
     * 
     * @return Chart for vector electrode
     */
    private JFreeChart createVectorChart()
    ////////////////////////////////////////////////////////////////////////
    {
      vectorxydataset = new VectorSeriesCollection();
      vectorxydataset.addSeries(vectorDataField[currFrame]);

      NumberAxis numberaxis = new NumberAxis("X");
      numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      numberaxis.setLowerMargin(0.01D);
      numberaxis.setUpperMargin(0.01D);
      numberaxis.setAutoRangeIncludesZero(false);
      NumberAxis numberaxis1 = new NumberAxis("Y");
      numberaxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      numberaxis1.setLowerMargin(0.01D);
      numberaxis1.setUpperMargin(0.01D);
      numberaxis1.setAutoRangeIncludesZero(false);
      VectorRenderer vectorrenderer = new VectorRenderer();
      vectorrenderer.setSeriesPaint(0, Color.blue);
      XYPlot xyplot = new XYPlot(vectorxydataset , numberaxis, numberaxis1, vectorrenderer);
      xyplot.setBackgroundPaint(Color.lightGray);
      xyplot.setDomainGridlinePaint(Color.white);
      xyplot.setRangeGridlinePaint(Color.white);
      xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
      xyplot.setOutlinePaint(Color.black);
      JFreeChart jfreechart = new JFreeChart(titleField+" "+String.format("%.0f",(vectorBinSize*currFrame*1000.0))+"-"+String.format("%.0f",(vectorBinSize*(currFrame+1)*1000.0))+" ms", xyplot);                                            
      jfreechart.setBackgroundPaint(Color.white);
      chartsave = jfreechart;
      return jfreechart;
    }

    /**
     * 
     * 
     * @param xydataset
     * @param i
     * @return chart for multi unit electrode
     */
    private  JFreeChart createMUChart(XYDataset xydataset,int i)
    ////////////////////////////////////////////////////////////////////////
    {
      JFreeChart jfreechart = ChartFactory.createScatterPlot("Multi Unit SubExp "+subexpNames.getString(i), "Time (ms)", "Rate (Hz)", xydataset, PlotOrientation.VERTICAL, true, true, false);
      jfreechart.setBackgroundPaint(Color.white);
      XYPlot xyplot = (XYPlot)jfreechart.getPlot();
      xyplot.setBackgroundPaint(Color.lightGray);
      xyplot.setDomainGridlinePaint(Color.white);
      xyplot.setRangeGridlinePaint(Color.white);
      xyplot.setAxisOffset(new RectangleInsets(4D, 4D, 4D, 4D));
      XYErrorRenderer render = new XYErrorRenderer();
      suRender = render;
      //  YIntervalRenderer render = new YIntervalRenderer();
/*
    render.setSeriesStroke(0, new BasicStroke(3F, 1, 1));                                                 
    render.setSeriesStroke(1, new BasicStroke(3F, 1, 1));                                                 
    render.setSeriesLinesVisible(0,true);
    render.setSeriesLinesVisible(1,true);
 */
      //render.setSeriesShapesVisible(0,true);
      //
      for(int ii=0; ii < suSeries; ii++)
      {
        render.setSeriesStroke(ii, new BasicStroke(3F, 1, 1));                                                 
        render.setSeriesLinesVisible(ii,true);
        if(ii==nowValue)
        {
          suRender.setSeriesVisible(ii, new Boolean(true));
        }
        else
        {
          suRender.setSeriesVisible(ii, new Boolean(false));

        }
      }


      xyplot.setRenderer(render);
      chartsave = jfreechart;
      return jfreechart;
      /*
    JFreeChart jfreechart = ChartFactory.createXYLineChart("Multi Unit", "Time (ms)", "Rate (Hz)", xydataset, PlotOrientation.VERTICAL, true, true, false);
    jfreechart.setBackgroundPaint(Color.white);
    XYPlot xyplot = (XYPlot)jfreechart.getPlot();
    xyplot.setBackgroundPaint(Color.lightGray);
    xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
    xyplot.setDomainGridlinePaint(Color.white);
    xyplot.setRangeGridlinePaint(Color.white);
    //  DeviationRenderer deviationrenderer = new DeviationRenderer(true, false);
    //  deviationrenderer.setSeriesStroke(0, new BasicStroke(3F, 1, 1));                                                 
    //  deviationrenderer.setSeriesStroke(1, new BasicStroke(3F, 1, 1));                                                 
    //  deviationrenderer.setSeriesFillPaint(0, new Color(255, 200, 200));
    //  deviationrenderer.setSeriesFillPaint(1, new Color(200, 200, 255));
    xyplot.setRenderer(deviationrenderer);
    NumberAxis numberaxis = (NumberAxis)xyplot.getRangeAxis();
    numberaxis.setAutoRangeIncludesZero(false);
    numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    return jfreechart;
       */
    }
    
    /**
     * 
     * 
     * @return Chart for intracellular electrodes
     */
    private JFreeChart createIntraChart()
    ////////////////////////////////////////////////////////////////////////
    {   	
    	NumberAxis timeAxis = new NumberAxis("Time (s)");
    	CombinedDomainXYPlot plot = new CombinedDomainXYPlot(timeAxis);
    	
    	XYItemRenderer voltageRenderer = new XYLineAndShapeRenderer(true, false);
    	NumberAxis voltageAxis = new NumberAxis("Voltage (v)");
    	XYPlot voltagePlot = new XYPlot(intraVoltageData, timeAxis, voltageAxis, voltageRenderer);
    	voltagePlot.setBackgroundPaint(Color.lightGray);
        voltagePlot.setDomainGridlinePaint(Color.white);
        voltagePlot.setRangeGridlinePaint(Color.white);
        voltagePlot.setAxisOffset(new RectangleInsets(4D, 4D, 4D, 4D));
        plot.add(voltagePlot);

    	XYItemRenderer currentRenderer = new XYLineAndShapeRenderer(true, false);
    	NumberAxis currentAxis = new NumberAxis("Current (nA)");
    	XYPlot currentPlot = new XYPlot(intraCurrentData, timeAxis, currentAxis, currentRenderer);
    	currentPlot.setBackgroundPaint(Color.lightGray);
        currentPlot.setDomainGridlinePaint(Color.white);
        currentPlot.setRangeGridlinePaint(Color.white);
        currentPlot.setAxisOffset(new RectangleInsets(4D, 4D, 4D, 4D));
        plot.add(currentPlot);

        JFreeChart jfreechart = new JFreeChart("SubExp " + subexpNames.getString(currExp) + " Intracellular Unit " + intraNames.getString(currIntra), plot);

        chartsave = jfreechart; // For SaveEPS
        
        return jfreechart;
    }
    

    /**
     * 
     *  create the panel
     * @param i
     * @return
     * 
     * @throws Exception
     */
    public  JPanel createMUPanel(int i) throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
      JFreeChart jfreechart = createMUChart(readMU(i),i);
      return new ChartPanel(jfreechart);
    }

    /**
     * 
     * create the panel
     * @return
     * 
     * @throws Exception
     */
    public  JPanel createFieldPanel() throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
      JFreeChart jfreechart = createFieldChart();
      return new ChartPanel(jfreechart);
    }
    
    /**
     * 
     * create the intracellular recorder panel
     * @return
     * 
     * @throws Exception
     */
    public  JPanel createIntraPanel() throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
      JFreeChart jfreechart = createIntraChart();
      return new ChartPanel(jfreechart);
    }

    /**
     * create the panel
     * 
     * @return
     * 
     * @throws Exception
     */
    public  JPanel createVectorPanel() throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
      JFreeChart jfreechart = createVectorChart();
      return new ChartPanel(jfreechart);
    }
    public static void suPlot() throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
//    org.apache.log4j.BasicConfigurator.configure();
//    org.apache.log4j.BasicConfigurator.configure(new FileAppender(new SimpleLayout(),"log/ncfile.log"));
      ucar.nc2.Dimension exp = ncfile.findDimension("suEleId");
      if(exp!=null)
      {
        for(int i = 0 ; i< numExp; i++)
        {
          PlotResult frame = new PlotResult("SU Raster "+subexpNames.getString(i));
          frame.currExp = i;
          JPanel jpanel = frame.createSuPanel(i);
          JPanel command = new JPanel();
          for(int j = 0 ; j< frame.suSeries; j++)
          {
            JCheckBox check = new JCheckBox(frame.suNames.getString(j));
            check.setActionCommand(new Integer(j).toString());
            check.addActionListener(frame);
            check.setSelected(false);
            command.add(check);
          }

          JButton pre = new JButton("SaveEPS");
          pre.setActionCommand("SUsave");
          pre.addActionListener(frame);
          command.add(pre);

          jpanel.setPreferredSize(new java.awt.Dimension(xEdge+50,yEdge+50));
          //frame.setContentPane(jpanel);
          frame.add(jpanel);
          frame.add(command,"South");
          frame.pack();
          RefineryUtilities.centerFrameOnScreen(frame);
          frame.setVisible(true);
        }
      }
    }

    public static void muPlot() throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
//    org.apache.log4j.BasicConfigurator.configure(new FileAppender(new SimpleLayout(),"log/ncfile.log"));
      ucar.nc2.Dimension exp = ncfile.findDimension("muEleId");
      if(exp!=null)
      {
        for(int i = 0 ; i< numExp; i++)
        {
          PlotResult frame = new PlotResult("MU Rateplot "+subexpNames.getString(i));
          frame.currExp = i;
          JPanel jpanel = frame.createMUPanel(i);

          JPanel command = new JPanel();
          for(int j = 0 ; j< frame.suSeries; j++)
          {
            JCheckBox check = new JCheckBox(frame.suNames.getString(j));
            check.setActionCommand(new Integer(j).toString());
            check.addActionListener(frame);
            check.setSelected(false);
            command.add(check);
          }

          JButton pre = new JButton("SaveEPS");
          pre.setActionCommand("MUsave");
          pre.addActionListener(frame);
          command.add(pre);



          jpanel.setPreferredSize(new java.awt.Dimension(xEdge+50,yEdge+50));
//    frame.setContentPane(jpanel);
          frame.add(jpanel);
          frame.add(command,"South");
          frame.pack();
          RefineryUtilities.centerFrameOnScreen(frame);
          frame.setVisible(true);
        }
      }
    }

    public static void fieldPlot() throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
//    org.apache.log4j.BasicConfigurator.configure(new FileAppender(new SimpleLayout(),"log/ncfile.log"));
      for(int i = 0 ; i< numExp; i++)
      {
        for(int j=0; j< numField; j++)
        {
          PlotResult frame = new PlotResult("FieldPlot "+subexpNames.getString(i)+" Id "+j);
          frame.currExp = i;
          frame.currField = j;
          frame.readField(i,j);
          frame.titleField= frame.fieldNames.getString(j);
          JPanel jpanel = frame.createFieldPanel();

          JPanel command = new JPanel();

          JButton pre = new JButton("<-");
          pre.setActionCommand("F_pre");
          pre.addActionListener(frame);
          command.add(pre);
          JButton next = new JButton("->");
          next.setActionCommand("F_next");
          next.addActionListener(frame);
          command.add(next);

          pre = new JButton("SaveEPS");
          pre.setActionCommand("Fieldsave");
          pre.addActionListener(frame);
          command.add(pre);



          jpanel.setPreferredSize(new java.awt.Dimension(xEdge+50,yEdge+50));
//        frame.setContentPane(jpanel);
          frame.add(jpanel);
          frame.add(command,"South");
          frame.pack();
          RefineryUtilities.centerFrameOnScreen(frame);
          frame.setVisible(true);
        }
      }
    }

    public static void vectorPlot() throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
      for(int i = 0 ; i< numExp; i++)
      {
        for(int j=0; j< numVector; j++)
        {
          PlotResult frame = new PlotResult("VectorPlot "+subexpNames.getString(i)+" Id "+j);
          frame.currExp = i;
          frame.currField = j;
          frame.readVector(i,j);
          frame.titleField= frame.vectorNames.getString(j);
          JPanel jpanel = frame.createVectorPanel();

          JPanel command = new JPanel();

          JButton pre = new JButton("<-");
          pre.setActionCommand("V_pre");
          pre.addActionListener(frame);
          command.add(pre);
          JButton next = new JButton("->");
          next.setActionCommand("V_next");
          next.addActionListener(frame);
          command.add(next);

          pre = new JButton("SaveEPS");
          pre.setActionCommand("Vectorsave");
          pre.addActionListener(frame);
          command.add(pre);





          jpanel.setPreferredSize(new java.awt.Dimension(xEdge+50,yEdge+50));
//        frame.setContentPane(jpanel);
          frame.add(jpanel);
          frame.add(command,"South");
          frame.pack();
          RefineryUtilities.centerFrameOnScreen(frame);
          frame.setVisible(true);
        }
      }
    }

    public static void intraPlot() throws Exception
    {
    	for (int i = 0; i < numExp; i++)
    	{
    		for (int j = 0; j < numIntra; j++)
    		{
    	    	PlotResult frame = new PlotResult ( "Intracellular Plot " + subexpNames.getString( i ) + " Id "+ j );
    	    	frame.currExp = i;
    	    	frame.currIntra = j;
    	    	frame.readIntra( i , j );
    	    	frame.titleField = frame.intraNames.getString( j );
    	    	
    	    	JPanel intraPanel = frame.createIntraPanel();
    	    	intraPanel.setPreferredSize ( new java.awt.Dimension( xEdge + 50, yEdge + 50 ));
    	    	
    	    	JPanel commandPanel = new JPanel();
    	    	
    	    	JButton saveButton = new JButton( "SaveEPS" );
    	    	saveButton.setActionCommand("Intrasave");
    	    	saveButton.addActionListener(frame);
    	    	
    	    	commandPanel.add( saveButton );
    	    	
    	    	frame.add( intraPanel );
    	    	frame.add( commandPanel , "South" );
    	    	frame.pack( );
    	        RefineryUtilities.centerFrameOnScreen(frame);
    	        frame.setVisible( true );
    		}
    	}
    }


    /**
     * 
     *  generate the EPS files
     * 
     * @throws Exception
     */
    public  static void exportFile() throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
      Properties p = new Properties();
      p.setProperty("PageSize","A5");

      //JPanel jpanel = createMUPanel(0);
      for(int i = 0 ; i < numExp; i++)
      {
        PlotResult frame = new PlotResult("SU Raster SubExp "+i);
        JFreeChart jfreechart = frame.createSUChart(frame.readSu(i));
        VectorGraphics g = new PSGraphics2D(new File("pics/SU_sub"+i+".eps"), new java.awt.Dimension(xEdge+50,yEdge+50)); 
        g.setProperties(p); 
        g.startExport(); 
        //jpanel.print(g); 
        Rectangle2D r2D = new Rectangle2D.Double(0,0, xEdge+50,yEdge+50);
        jfreechart.draw(g, r2D);
        g.endExport();
      }

      for(int i = 0 ; i < numExp; i++)
      {
        PlotResult frame = new PlotResult("MU Rateplot SubExp "+i);
        JFreeChart jfreechart = frame.createMUChart(frame.readMU(i),i);
        VectorGraphics g = new PSGraphics2D(new File("pics/MU_sub"+i+".eps"), new java.awt.Dimension(xEdge+50,yEdge+50)); 
        g.setProperties(p); 
        g.startExport(); 
        //jpanel.print(g); 
        Rectangle2D r2D = new Rectangle2D.Double(0,0, xEdge+50,yEdge+50);
        jfreechart.draw(g, r2D);
        g.endExport();
      }

      for(int i = 0 ; i< numExp; i++)
      {
        for(int j=0; j< numField; j++)
        {
          PlotResult frame = new PlotResult("FieldPlot SubExp "+i+" Id "+j);
          frame.readField(i,j);
          frame.titleField= frame.fieldNames.getString(j);

          for(int k=0; k< frame.dataField.length; k++)
          {
            frame.currFrame = k;
            JFreeChart jfreechart = frame.createFieldChart();
            VectorGraphics g = new PSGraphics2D(new File("pics/field_sub"+i+"_id"+j+"_t"+k+".eps"), new java.awt.Dimension(xEdge+50,yEdge+50)); 
            g.setProperties(p); 
            g.startExport(); 
            //jpanel.print(g); 
            Rectangle2D r2D = new Rectangle2D.Double(0,0, xEdge+50,yEdge+50);
            jfreechart.draw(g, r2D);
            g.endExport();
          }
        }
      }


      for(int i = 0 ; i< numExp; i++)
      {
        for(int j=0; j< numVector; j++)
        {
          PlotResult frame = new PlotResult("VectorPlot SubExp "+i+" Id "+j);
          frame.readVector(i,j);

          frame.titleField= frame.vectorNames.getString(j);

          for(int k=0; k< frame.vectorDataField.length; k++)
          {
            frame.currFrame = k;
            JFreeChart jfreechart = frame.createVectorChart();
            VectorGraphics g = new PSGraphics2D(new File("pics/vector_sub"+i+"_id"+j+"_t"+k+".eps"), new java.awt.Dimension(xEdge+50,yEdge+50)); 
            g.setProperties(p); 
            g.startExport(); 
            //jpanel.print(g); 
            Rectangle2D r2D = new Rectangle2D.Double(0,0, xEdge+50,yEdge+50);
            jfreechart.draw(g, r2D);
            g.endExport();
          }
        }
      }
      
      for(int i = 0 ; i< numExp; i++)
      {
    	  for(int j=0; j< numIntra; j++)
    	  {
    		  PlotResult frame = new PlotResult("IntraPlot SubExp "+i+" Id "+j);
    		  frame.readIntra(i,j);

    		  frame.titleField= frame.intraNames.getString(j);

    		  JFreeChart jfreechart = frame.createIntraChart();
    		  VectorGraphics g = new PSGraphics2D(new File("pics/intra_sub"+i+"_id"+j+".eps"), new java.awt.Dimension(xEdge+50,yEdge+50)); 
    		  g.setProperties(p); 
    		  g.startExport(); 
    		  //jpanel.print(g); 
    		  Rectangle2D r2D = new Rectangle2D.Double(0,0, xEdge+50,yEdge+50);
    		  jfreechart.draw(g, r2D);
    		  g.endExport();
    	  }
      }


    }

    /**
     *  initialize the plotting class
     * 
     * 
     * @throws Exception
     */
    public static void init() throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
// Remarked out when switched for Log4j to Logback for logging.
// This log4j log file might have been initialized but not used.      
//      org.apache.log4j.BasicConfigurator.configure(new FileAppender(new SimpleLayout(),"log/ncfile.log"));
      String filename = "results/simExperiment.nc";
      ncfile = NetcdfFile.open(filename);
      Variable exp = ncfile.findVariable("subExpNum");
      numExp = exp.readScalarInt();
      int [] varShape;
      exp = ncfile.findVariable("fieldNames");
      if(exp!=null)
      {
        ArrayChar.D2 fieldNames   = (ArrayChar.D2) exp.read();
        varShape = exp.getShape();
        numField = varShape[0];
      }
      else
      {
        numField=0;
      }

      exp = ncfile.findVariable("vectorNames");
      if(exp!=null)
      {
        ArrayChar.D2 vectorNames   = (ArrayChar.D2) exp.read();
        varShape = exp.getShape();
        numVector = varShape[0];
      }
      else
      {
        numVector=0;
      }
      
      exp = ncfile.findVariable("intraNames");
      if (exp != null)
      {
    	  varShape = exp.getShape();
    	  numIntra = varShape[0];
      }
      else
      {
    	  numIntra = 0;
      }

      exp = ncfile.findVariable("subexpNames");
      subexpNames = (ArrayChar.D2) exp.read();
      exp = ncfile.findVariable("xEdgeLen");
      xEdge = exp.readScalarInt();

      exp = ncfile.findVariable("yEdgeLen");
      yEdge = exp.readScalarInt();

    }

    public static void init(String fileName) throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
      // Remarked out when switched for Log4j to Logback for logging.
      // This log4j log file might have been initialized but not used.      
      // org.apache.log4j.BasicConfigurator.configure(new FileAppender(new SimpleLayout(),"log/ncfile.log"));
      
//    String filename = "results/"+fileName;
      String filename = fileName;
      ncfile = NetcdfFile.open(filename);
      Variable exp = ncfile.findVariable("subExpNum");
      numExp = exp.readScalarInt();
      int [] varShape;
      exp = ncfile.findVariable("fieldNames");
      if(exp!=null)
      {
        ArrayChar.D2 fieldNames   = (ArrayChar.D2) exp.read();
        varShape = exp.getShape();
        numField = varShape[0];
      }
      else
      {
        numField=0;
      }

      exp = ncfile.findVariable("vectorNames");
      if(exp!=null)
      {
        ArrayChar.D2 vectorNames   = (ArrayChar.D2) exp.read();
        varShape = exp.getShape();
        numVector = varShape[0];
      }
      else
      {
        numVector=0;
      }
      
      exp = ncfile.findVariable("intraNames");
      if (exp != null)
      {
    	  varShape = exp.getShape();
    	  numIntra = varShape[0];
      }
      else
      {
    	  numIntra = 0;
      }

      exp = ncfile.findVariable("subexpNames");
      subexpNames = (ArrayChar.D2) exp.read();

      exp = ncfile.findVariable("xEdgeLen");
      xEdge = exp.readScalarInt();

      exp = ncfile.findVariable("yEdgeLen");
      yEdge = exp.readScalarInt();

    }
    public static void stop() throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
      ncfile.close();
    }

    public void getFrame(int subExp,int recorderType, int recorderID) throws Exception
    ////////////////////////////////////////////////////////////////////////
    {
      if(recorderType==0) // Single-Unit
      {
        ucar.nc2.Dimension exp = ncfile.findDimension("suEleId");
        if(exp!=null)
        {
          PlotResult frame = new PlotResult("SU Raster "+subexpNames.getString(subExp));
          frame.nowValue=recorderID;
          frame.removeWindowListener(frame.getWindowListeners()[0]);

          frame.currExp = subExp;
          JPanel jpanel = frame.createSuPanel(subExp);
          JPanel command = new JPanel();
          for(int jj = 0 ; jj< frame.suSeries; jj++)
          {
            JCheckBox check = new JCheckBox(frame.suNames.getString(jj));
            check.setActionCommand(new Integer(jj).toString());
            check.addActionListener(frame);
            if(jj==recorderID)
            {
              check.setSelected(true);
            }
            else
            {
              check.setSelected(false);
            }
            command.add(check);
          }

          JButton pre = new JButton("SaveEPS");
          pre.setActionCommand("SUsave");
          pre.addActionListener(frame);
          command.add(pre);

          jpanel.setPreferredSize(new java.awt.Dimension(xEdge+50,yEdge+50));
          //frame.setContentPane(jpanel);
          frame.add(jpanel);
          frame.add(command,"South");
          frame.pack();
          RefineryUtilities.centerFrameOnScreen(frame);
          frame.setVisible(true);
        }

      }
      if(recorderType==1) // Multi-Unit
      {
        ucar.nc2.Dimension exp = ncfile.findDimension("muEleId");
        if(exp!=null)
        {
          PlotResult frame = new PlotResult("MU Rateplot "+subexpNames.getString(subExp));
          frame.nowValue=recorderID;
          frame.removeWindowListener(frame.getWindowListeners()[0]);
          frame.currExp = subExp;
          JPanel jpanel = frame.createMUPanel(subExp);

          JPanel command = new JPanel();
          for(int jj = 0 ; jj< frame.suSeries; jj++)
          {
            JCheckBox check = new JCheckBox(frame.suNames.getString(jj));
            check.setActionCommand(new Integer(jj).toString());
            check.addActionListener(frame);
            if(jj==recorderID)
            {
              check.setSelected(true);
            }
            else
            {
              check.setSelected(false);
            }

            command.add(check);
          }

          JButton pre = new JButton("SaveEPS");
          pre.setActionCommand("MUsave");
          pre.addActionListener(frame);
          command.add(pre);



          jpanel.setPreferredSize(new java.awt.Dimension(xEdge+50,yEdge+50));
          //    frame.setContentPane(jpanel);
          frame.add(jpanel);
          frame.add(command,"South");
          frame.pack();
          RefineryUtilities.centerFrameOnScreen(frame);
          frame.setVisible(true);
        }
      }
      if(recorderType==2) // Field
      {
        PlotResult frame = new PlotResult("FieldPlot "+subexpNames.getString(subExp)+" Id "+recorderID);

        //System.out.println(frame.getWindowListeners().length);
        frame.removeWindowListener(frame.getWindowListeners()[0]);
        frame.currExp = subExp;
        frame.currField = recorderID;
        frame.readField(subExp,recorderID);
        frame.titleField= frame.fieldNames.getString(recorderID);
        JPanel jpanel = frame.createFieldPanel();

        JPanel command = new JPanel();

        JButton pre = new JButton("<-");
        pre.setActionCommand("F_pre");
        pre.addActionListener(frame);
        command.add(pre);
        JButton next = new JButton("->");
        next.setActionCommand("F_next");
        next.addActionListener(frame);
        command.add(next);

        pre = new JButton("SaveEPS");
        pre.setActionCommand("Fieldsave");
        pre.addActionListener(frame);
        command.add(pre);



        jpanel.setPreferredSize(new java.awt.Dimension(xEdge+50,yEdge+50));
        //        frame.setContentPane(jpanel);
        frame.add(jpanel);
        frame.add(command,"South");
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
      }
      if(recorderType==3) // Vector
      {
        PlotResult frame = new PlotResult("VectorPlot "+subexpNames.getString(subExp)+" Id "+recorderID);
        frame.removeWindowListener(frame.getWindowListeners()[0]);
        frame.currExp = subExp;
        frame.currField = recorderID;
        frame.readVector(subExp,recorderID);
        frame.titleField= frame.vectorNames.getString(recorderID);
        JPanel jpanel = frame.createVectorPanel();

        JPanel command = new JPanel();

        JButton pre = new JButton("<-");
        pre.setActionCommand("V_pre");
        pre.addActionListener(frame);
        command.add(pre);
        JButton next = new JButton("->");
        next.setActionCommand("V_next");
        next.addActionListener(frame);
        command.add(next);

        pre = new JButton("SaveEPS");
        pre.setActionCommand("Vectorsave");
        pre.addActionListener(frame);
        command.add(pre);

        jpanel.setPreferredSize(new java.awt.Dimension(xEdge+50,yEdge+50));
//        frame.setContentPane(jpanel);
        frame.add(jpanel);
        frame.add(command,"South");
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
      }
      if(recorderType==4) // Intracellular
      {
    	  PlotResult frame = new PlotResult ( "Intracellular Plot " + subexpNames.getString( subExp ) + " Id "+ recorderID );
          frame.removeWindowListener(frame.getWindowListeners()[0]);
    	  frame.currExp = subExp;
    	  frame.currIntra = recorderID;
    	  frame.readIntra( subExp , recorderID );
    	  frame.titleField = frame.intraNames.getString( recorderID );

    	  JPanel intraPanel = frame.createIntraPanel();
    	  intraPanel.setPreferredSize ( new java.awt.Dimension( xEdge + 50, yEdge + 50 ));

    	  JPanel commandPanel = new JPanel();

    	  JButton saveButton = new JButton( "SaveEPS" );
    	  saveButton.setActionCommand("Intrasave");
    	  saveButton.addActionListener(frame);

    	  commandPanel.add( saveButton );

    	  frame.add( intraPanel );
    	  frame.add( commandPanel , "South" );
    	  frame.pack( );
    	  RefineryUtilities.centerFrameOnScreen(frame);
    	  frame.setVisible( true );
      }     
    }

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    }
