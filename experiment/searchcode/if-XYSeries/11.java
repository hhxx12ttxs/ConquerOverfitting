package cnslab.cnsnetwork;
    
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;
import org.jfree.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;    
import ucar.ma2.*;
import ucar.nc2.*;

    /***********************************************************************
    * Write the recorded data into files.
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
    public final class  RecWriter
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    {
    
    private static final Class<RecWriter>
      CLASS = RecWriter.class;

    private static final Logger
      LOGGER = LoggerFactory.getLogger ( CLASS );
    
    
    // The character used to separate terms in variable names.
    private static final String SEPARATOR = "_";
    
    //
  
    /** info */
    public Experiment exp;

    /** XML parser */
    public SimulatorParser pas;

    /** The recorded info */
    public Map<String, LinkedList<Double>>  receiver;

    public LinkedList[]  intraReceiver;

    /** don't consider trials */
    public Map<String, Integer>  multiCounter;
    
    /** considering separate trials */
    public Map<String, Integer>  multiCounterAll; 

    /** different time section, neuron */
    public Map<String, Integer> fieldCounter; 

    /** different time section, neuron for X axis */
    public Map<String, Double>  vectorCounterX;
    
    /** different time section, neuron for Y axis */
    public Map<String, Double>  vectorCounterY;
    
    public ArrayList<ArrayList<Map<String, Integer>>>  avalancheCounter; 

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
        
    public  RecWriter (
      final Experiment       exp,
      final SimulatorParser  pas,
      final LinkedList [ ]   intraReceiver,
      final RecorderData     rdata )
    ////////////////////////////////////////////////////////////////////////
    {
      this.exp = exp;
      this.pas = pas;
      this.receiver = rdata.receiver;
      this.intraReceiver = intraReceiver;
      this.multiCounter = rdata.multiCounter;
      this.multiCounterAll =  rdata.multiCounterAll;
      this.fieldCounter = rdata.fieldCounter;
      this.vectorCounterX = rdata.vectorCounterX;
      this.vectorCounterY = rdata.vectorCounterY;
    }

    public  RecWriter (
      final Experiment        exp,
      final SimulatorParser   pas,
      final LinkedList [ ]    intraReceiver,
      final RecorderData      rdata,
      final AvalancheCounter  aData )
    ////////////////////////////////////////////////////////////////////////
    {
      this.exp = exp;
      this.pas = pas;
      this.receiver = rdata.receiver;
      this.intraReceiver = intraReceiver;
      this.multiCounter = rdata.multiCounter;
      this.multiCounterAll =  rdata.multiCounterAll;
      this.fieldCounter = rdata.fieldCounter;
      this.vectorCounterX = rdata.vectorCounterX;
      this.vectorCounterY = rdata.vectorCounterY;
      this.avalancheCounter = aData.avalancheCounter;
    }

    /***********************************************************************
    * Record the data into file fileOut
    *
    * @param fileOut
    * 
    * @throws Exception
    ***********************************************************************/
    public void  record ( final String  fileOut )
      throws Exception
    ////////////////////////////////////////////////////////////////////////
    {        
      //prepare the recorder's file
      String filename = "results/"+fileOut;
      NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(filename, false);
      ArrayList<Dimension> dims;
      Dimension eleId=null,mulEleId=null,fieldEleId=null,vectorEleId=null,intraEleId=null;
      Dimension [] vectorX=null,vectorY=null,intraCha=null,fieldX=null,fieldY=null;
      Dimension sLen = ncfile.addDimension("svarLen", 40); //length of each name is no longer than 40;
      if(exp.recorder.singleUnit.size()>0)
      {
        eleId = ncfile.addDimension("suEleId", exp.recorder.singleUnit.size());
        dims = new ArrayList<Dimension>();
        dims.add(eleId);
        dims.add(sLen);
        ncfile.addVariable("suNames", DataType.CHAR, dims);
      }

      if(exp.recorder.multiUnit.size()>0)
      {
        mulEleId = ncfile.addDimension("muEleId", exp.recorder.multiUnit.size());
        ncfile.addVariable("muBinSize", DataType.DOUBLE, new ArrayList());
        dims = new ArrayList<Dimension>();
        dims.add(mulEleId);
        dims.add(sLen);
        ncfile.addVariable("muNames", DataType.CHAR, dims);
      }

      if(exp.recorder.fieldNames.size()>0)
      {
        fieldEleId = ncfile.addDimension("fieldEleId", exp.recorder.fieldNames.size());
        fieldX =  new Dimension[exp.recorder.fieldEle.size()];
        fieldY =  new Dimension[exp.recorder.fieldEle.size()];
        ncfile.addVariable("fieldBinSize", DataType.DOUBLE, new ArrayList());
        dims = new ArrayList<Dimension>();
        dims.add(fieldEleId);
        dims.add(sLen);
        ncfile.addVariable("fieldNames", DataType.CHAR, dims);
      }

      if(exp.recorder.vectorNames.size()>0)
      {
        vectorEleId = ncfile.addDimension("vectorEleId", exp.recorder.vectorNames.size());
        vectorX =  new Dimension[exp.recorder.vectorUnit.size()];
        vectorY =  new Dimension[exp.recorder.vectorUnit.size()];
        ncfile.addVariable("vectorBinSize", DataType.DOUBLE, new ArrayList());
        dims = new ArrayList<Dimension>();
        dims.add(vectorEleId);
        dims.add(sLen);
        ncfile.addVariable("vectorNames", DataType.CHAR, dims);
      }

      if(exp.recorder.intraNames.size()>0)
      {
        intraEleId = ncfile.addDimension("intraEleId", exp.recorder.intraNames.size());
        intraCha =  new Dimension[exp.recorder.intraEle.size()];
        ncfile.addVariable("intraBinSize", DataType.DOUBLE, new ArrayList());
        dims = new ArrayList<Dimension>();
        dims.add(intraEleId);
        dims.add(sLen);
        ncfile.addVariable("intraNames", DataType.CHAR, dims);
      }
     
      Dimension trials[] = new Dimension[exp.subExp.length];

      Dimension multiBinLen[] = new Dimension[exp.subExp.length];
      Dimension fieldBinLen[] = new Dimension[exp.subExp.length];
      Dimension vectorBinLen[] = new Dimension[exp.subExp.length];
      Dimension intraBinLen[] = new Dimension[exp.subExp.length];


      for(int  i=0; i<exp.recorder.intraEle.size(); i++)
      {
        intraCha[i] = ncfile.addDimension("channel"+i, exp.recorder.currChannel.get(i).size());
      }

      for(int i=0; i<exp.recorder.fieldEle.size(); i++)
      {
        int xMul,yMul;
        xMul = exp.recorder.fieldEle.get(i).getMultiplierX();
        yMul = exp.recorder.fieldEle.get(i).getMultiplierY();

        if(xMul < 0 )
        {
          if(pas.xEdgeLength % xMul !=0 ) throw new RuntimeException(xMul+"is not a multiplier of x-edge length"+pas.xEdgeLength);
        }
        if(yMul < 0 )
        {
          if(pas.yEdgeLength % yMul !=0 ) throw new RuntimeException(yMul+"is not a multiplier of y-edge length"+pas.yEdgeLength);
        }

        fieldX[i] = ncfile.addDimension("fieldX"+i, (xMul >0 ? pas.xEdgeLength*xMul : pas.xEdgeLength/(-xMul)));
        fieldY[i] = ncfile.addDimension("fieldY"+i, (yMul >0 ? pas.yEdgeLength*yMul : pas.yEdgeLength/(-yMul)));
//      System.out.println("pas.xEdgeLength "+pas.xEdgeLength+" pas.yEdgeLength "+pas.yEdgeLength+ " xsize "+(xMul >0 ? pas.xEdgeLength*xMul : pas.xEdgeLength/(-xMul))+" ysize "+(yMul >0 ? pas.yEdgeLength*yMul : pas.yEdgeLength/(-yMul))+" xMul "+xMul+" yMul "+yMul);
      }

      for(int i=0; i<exp.recorder.vectorUnit.size(); i++)
      {
        int xMul,yMul;
        xMul = exp.recorder.vectorUnit.get(i).coms.get(0).layer.getMultiplierX();
        yMul = exp.recorder.vectorUnit.get(i).coms.get(0).layer.getMultiplierY();

        if(xMul < 0 )
        {
          if(pas.xEdgeLength % xMul !=0 ) throw new RuntimeException(xMul+"is not a multiplier of x-edge length"+pas.xEdgeLength);
        }
        if(yMul < 0 )
        {
          if(pas.yEdgeLength % yMul !=0 ) throw new RuntimeException(yMul+"is not a multiplier of y-edge length"+pas.yEdgeLength);
        }

        vectorX[i] = ncfile.addDimension("vectorX"+i, (xMul >0 ? pas.xEdgeLength*xMul : pas.xEdgeLength/(-xMul)));
        vectorY[i] = ncfile.addDimension("vectorY"+i, (yMul >0 ? pas.yEdgeLength*yMul : pas.yEdgeLength/(-yMul)));
//      System.out.println("pas.xEdgeLength "+pas.xEdgeLength+" pas.yEdgeLength "+pas.yEdgeLength+ " xsize "+(xMul >0 ? pas.xEdgeLength*xMul : pas.xEdgeLength/(-xMul))+" ysize "+(yMul >0 ? pas.yEdgeLength*yMul : pas.yEdgeLength/(-yMul))+" xMul "+xMul+" yMul "+yMul);
      }


      Dimension numSub = ncfile.addDimension("subexpId", exp.subExp.length);
      ncfile.addVariable("subExpNum", DataType.INT, new ArrayList());
      ncfile.addVariable("xEdgeLen", DataType.INT, new ArrayList());
      ncfile.addVariable("yEdgeLen", DataType.INT, new ArrayList());

      
      dims = new ArrayList<Dimension>();
      dims.add(numSub);
      dims.add(sLen);
      ncfile.addVariable("subexpNames", DataType.CHAR, dims);


      for(int expId=0; expId < exp.subExp.length; expId++)
      {       
        trials[expId] = ncfile.addDimension("subTrial"+expId, exp.subExp[expId].repetition);

        if(exp.recorder.multiUnit.size()>0)
        {
          multiBinLen[expId] = ncfile.addDimension("binLen"+expId, (int)(exp.subExp[expId].trialLength/exp.recorder.timeBinSize));
        }
        if(exp.recorder.fieldEle.size()>0)
        {
          fieldBinLen[expId] = ncfile.addDimension("fBinLen"+expId, (int)(exp.subExp[expId].trialLength/exp.recorder.fieldTimeBinSize));
        }
        if(exp.recorder.vectorUnit.size()>0)
        {
          vectorBinLen[expId] = ncfile.addDimension("vBinLen"+expId, (int)(exp.subExp[expId].trialLength/exp.recorder.vectorTimeBinSize));
        }
        if(exp.recorder.intraEle.size()>0)
        {
          intraBinLen[expId] = ncfile.addDimension("iBinLen"+expId, (int)(exp.subExp[expId].trialLength/exp.recorder.intraTimeBinSize));
        }

        String subExpPrefix = "SubExp" + expId + SEPARATOR;

        Variable trialNum = ncfile.addVariable(subExpPrefix + "trialNum", DataType.INT, new ArrayList());

        for(int tID=0; tID<exp.subExp[expId].repetition; tID++)
        {
          String trialPrefix = subExpPrefix + "trial" + tID + SEPARATOR;
        	
          Variable ele[] = new Variable[exp.recorder.singleUnit.size()];

          if(avalancheCounter!=null) //record only necessary
          {
            Dimension dim_ava = ncfile.addDimension("A_E"+expId+"T"+tID, avalancheCounter.get(expId).get(tID).size());                  
            dims = new ArrayList<Dimension>();
            dims.add(dim_ava);
            Variable avalan = ncfile.addVariable(trialPrefix + "ava" + tID, DataType.INT, dims);
          }

          // Create single-unit recorder variables.
          for(int sID = 0; sID < exp.recorder.singleUnit.size(); sID++)
          {       
            if(receiver.get("E"+expId+"T"+tID+"N"+exp.recorder.singleUnit.get(sID))!=null)
            {
              Dimension timeDim = ncfile.addDimension("tE"+expId+"T"+tID+"N"+exp.recorder.singleUnit.get(sID), receiver.get("E"+expId+"T"+tID+"N"+exp.recorder.singleUnit.get(sID)).size());
              dims = new ArrayList<Dimension>();
              dims.add(timeDim);
              ele[sID] = ncfile.addVariable(trialPrefix + exp.recorder.suNames.get(sID), DataType.DOUBLE, dims);
            }
          }
          
          // Create intracellular recorder variables.
          for(int iID = 0 ; iID < exp.recorder.intraEle.size(); iID++)
          {         
            dims = new ArrayList<Dimension>();
            dims.add(intraBinLen[expId]);
            // Average over electrodes
            Variable intraEleV = ncfile.addVariable(trialPrefix + exp.recorder.intraNames.get(iID) + SEPARATOR + "V", DataType.DOUBLE, dims);
            
            dims = new ArrayList<Dimension>();
            dims.add(intraCha[iID]);
            dims.add(intraBinLen[expId]);
            // Average over electrodes
            Variable intraEleC = ncfile.addVariable(trialPrefix + exp.recorder.intraNames.get(iID) + SEPARATOR + "C", DataType.DOUBLE, dims);          }
        } // END: for trial loop

        // Create multi-unit recorder variables.
        for(int mID = 0 ; mID < exp.recorder.multiUnit.size(); mID++)
        {
        	
          dims = new ArrayList<Dimension>();
          dims.add(multiBinLen[expId]);
          Variable multiEle = ncfile.addVariable(subExpPrefix + exp.recorder.multiNames.get(mID), DataType.DOUBLE, dims); //average over trials and electrodes;

          dims = new ArrayList<Dimension>();
          dims.add(trials[expId]);
          dims.add(multiBinLen[expId]);
          Variable multiEleAll = ncfile.addVariable(subExpPrefix + exp.recorder.multiNames.get(mID) + SEPARATOR + "ALL", DataType.DOUBLE, dims); // average over electrodes put trials as another dimension
        }

        // Create field recorder variables.s
        for(int fID = 0 ; fID < exp.recorder.fieldEle.size(); fID++)
        {        	
        	dims = new ArrayList<Dimension>();
        	dims.add(fieldX[fID]);
        	dims.add(fieldY[fID]);
        	dims.add(fieldBinLen[expId]);
        	Variable fieldEle = ncfile.addVariable(subExpPrefix + exp.recorder.fieldNames.get(fID), DataType.DOUBLE, dims); // average over trials and electrodes
        }

        // Create vector recorder variables.
        for(int vID = 0 ; vID < exp.recorder.vectorUnit.size(); vID++)
        {
        	dims = new ArrayList<Dimension>();
        	dims.add(vectorX[vID]);
        	dims.add(vectorY[vID]);
        	dims.add(vectorBinLen[expId]);
        	Variable vectorEleX = ncfile.addVariable(subExpPrefix + exp.recorder.vectorNames.get(vID) + SEPARATOR + "X", DataType.DOUBLE, dims);
        	Variable vectorEleY = ncfile.addVariable(subExpPrefix + exp.recorder.vectorNames.get(vID) + SEPARATOR + "Y", DataType.DOUBLE, dims);
        }

      }
      ncfile.create();
      //end of preparation


      //recording...now

      ArrayInt.D0 datas = new ArrayInt.D0();
      datas.set(exp.subExp.length);
      ncfile.write("subExpNum", datas);

      ArrayChar ac2;
      ArrayDouble.D0 binSize;

      if(exp.recorder.multiUnit.size()>0)
      {
        binSize = new ArrayDouble.D0();
        binSize.set(exp.recorder.timeBinSize);
        ncfile.write("muBinSize", binSize);
        ac2 = new ArrayChar.D2(exp.recorder.multiUnit.size(), 40);
        for( int sID= 0; sID < exp.recorder.multiUnit.size(); sID++) 
        {
          ac2.setString(sID,exp.recorder.multiNames.get(sID));
        }
        ncfile.write("muNames", ac2);
      }

      if(exp.recorder.fieldNames.size()>0)
      {

        binSize = new ArrayDouble.D0();
        binSize.set(exp.recorder.fieldTimeBinSize);
        ncfile.write("fieldBinSize", binSize);

        ac2 = new ArrayChar.D2(exp.recorder.fieldNames.size(), 40);
        for( int sID= 0; sID < exp.recorder.fieldNames.size(); sID++) 
        {
          ac2.setString(sID,exp.recorder.fieldNames.get(sID));
        }
        ncfile.write("fieldNames", ac2);
      }

      if(exp.recorder.vectorNames.size()>0)
      {
        binSize = new ArrayDouble.D0();
        binSize.set(exp.recorder.vectorTimeBinSize);
        ncfile.write("vectorBinSize", binSize);
        ac2 = new ArrayChar.D2(exp.recorder.vectorNames.size(), 40);
        for( int sID= 0; sID < exp.recorder.vectorNames.size(); sID++) 
        {
          ac2.setString(sID,exp.recorder.vectorNames.get(sID));
        }
        ncfile.write("vectorNames", ac2);
      }

      if(exp.recorder.intraNames.size()>0)
      {
        binSize = new ArrayDouble.D0();
        binSize.set(exp.recorder.intraTimeBinSize);
        ncfile.write("intraBinSize", binSize);
        ac2 = new ArrayChar.D2(exp.recorder.intraNames.size(), 40);
        for( int sID= 0; sID < exp.recorder.intraNames.size(); sID++) 
        {
          ac2.setString(sID,exp.recorder.intraNames.get(sID));
        }
        ncfile.write("intraNames", ac2);
      }

      datas = new ArrayInt.D0();
      datas.set(pas.xEdgeLength );
      ncfile.write("xEdgeLen", datas);

      datas = new ArrayInt.D0();
      datas.set(pas.yEdgeLength);
      ncfile.write("yEdgeLen", datas);

      ac2 = new ArrayChar.D2(exp.recorder.singleUnit.size(), 40);

      if(exp.recorder.singleUnit.size()>0)
      {

        for( int sID= 0; sID < exp.recorder.singleUnit.size(); sID++) 
        {
          ac2.setString(sID,exp.recorder.suNames.get(sID));
        }
        ncfile.write("suNames", ac2);
      }



      ac2 = new ArrayChar.D2(exp.subExp.length, 40);
      for( int sID= 0; sID < exp.subExp.length; sID++) 
      {
        ac2.setString(sID,exp.subExp[sID].name);
      }
      ncfile.write("subexpNames", ac2);




      for(int expId=0; expId < exp.subExp.length; expId++)
      {       
        ArrayDouble.D0 dtrial = new ArrayDouble.D0();
        dtrial.set(exp.subExp[expId].repetition);
        ncfile.write("SubExp" + expId + SEPARATOR + "trialNum", dtrial);

        for(int tID=0; tID<exp.subExp[expId].repetition; tID++)
        {
//      Map<String, Integer> mapdata = avalancheCounter.get(expId).get(tID);
          if(avalancheCounter!=null) //record when it is necessary
          {
            ArrayInt.D1 ava_input =  new ArrayInt.D1(avalancheCounter.get(expId).get(tID).size());  
            Iterator<Integer> values = avalancheCounter.get(expId).get(tID).values().iterator();
            int i_counter=0;
            while(values.hasNext())
            {
              ava_input.set(i_counter++, values.next());
            }
            int [] ava_origin = new int [] {0};
            ncfile.write("SubExp" + expId + SEPARATOR + "trial" + tID + SEPARATOR + "ava" + tID, ava_origin, ava_input);

          }
          //single unit
          for( int sID= 0; sID < exp.recorder.singleUnit.size(); sID++) 
          {
            LinkedList<Double> tmp = receiver.get("E"+expId+"T"+tID+"N"+exp.recorder.singleUnit.get(sID));
//        System.out.println("E"+expId+"T"+tID+"N"+exp.rec.singleUnit.get(sID));
            if(tmp!=null)
            {
              ArrayDouble.D1 input = new ArrayDouble.D1(tmp.size());
              //Index ima = input.getIndex();

              //double[] tmpDouble= new double[tmp.size()];
              //System.out.println("size"+tmp.size());
              int ii =0;
              for(double val : tmp)
              {
                input.set(ii++,val);
                //tmpDouble[ii++]= val;
              }

              int [] origin = new int [] {0};
              ncfile.write("SubExp" + expId + SEPARATOR + "trial" + tID + SEPARATOR + exp.recorder.suNames.get(sID), origin, input);
            }
          }
        } // END: for all trials in a subexp
        
        recordMultiUnit (
          exp,
          expId,
          multiCounter,
          multiCounterAll,
          ncfile );

        //recorder for the field ele
        for(int fID = 0 ; fID < exp.recorder.fieldEle.size(); fID++)
        {
          int xMul,yMul;
          xMul = exp.recorder.fieldEle.get(fID).getMultiplierX();
          yMul = exp.recorder.fieldEle.get(fID).getMultiplierY();
          String pre = exp.recorder.fieldEle.get(fID).getPrefix();
          String suf = exp.recorder.fieldEle.get(fID).getSuffix();

          int xstep,ystep;
          int xresosize,yresosize;

          if(xMul>0)
          {
            xstep=1;
            xresosize=xMul*pas.xEdgeLength;
          }
          else
          {
            xstep=-xMul;
            xresosize=pas.xEdgeLength;
          }
          if(yMul>0)
          {
            ystep=1;
            yresosize=yMul*pas.yEdgeLength;
          }
          else
          {
            ystep=-yMul;
            yresosize=pas.yEdgeLength;
          }

          ArrayDouble.D3 input = new ArrayDouble.D3(xresosize/xstep,yresosize/ystep, (int)(exp.subExp[expId].trialLength/exp.recorder.fieldTimeBinSize));
          for(int xx=0 ; xx < xresosize; xx+=xstep)
          {
            for(int yy=0; yy< yresosize; yy+=ystep)
            {
              for( int ii = 0 ; ii < (int)(exp.subExp[expId].trialLength/exp.recorder.fieldTimeBinSize) ; ii++)
              {

                Integer outInt=fieldCounter.get("E"+expId+","+pre+","+xx+","+yy+","+suf+","+"B"+ii);
                if(outInt==null) outInt= new Integer(0);
                input.set(xx/xstep,yy/ystep,ii,(double)outInt/(double)(exp.subExp[expId].repetition));
              }
            }
          }
          int [] origin = new int [] {0,0,0};
        ncfile.write("SubExp" + expId + SEPARATOR + exp.recorder.fieldNames.get(fID), origin, input);

        }
        
        //recorder for the vector ele
        for(int vID = 0 ; vID < exp.recorder.vectorUnit.size(); vID++)
        {
          int xMul,yMul;
          xMul = exp.recorder.vectorUnit.get(vID).coms.get(0).layer.getMultiplierX();
          yMul = exp.recorder.vectorUnit.get(vID).coms.get(0).layer.getMultiplierY();
          String pre = exp.recorder.vectorUnit.get(vID).coms.get(0).layer.getPrefix();
          String suf = exp.recorder.vectorUnit.get(vID).coms.get(0).layer.getSuffix();
          int xstep,ystep;
          int xresosize,yresosize;

          if(xMul>0)
          {
            xstep=1;
            xresosize=xMul*pas.xEdgeLength;
          }
          else
          {
            xstep=-xMul;
            xresosize=pas.xEdgeLength;
          }
          if(yMul>0)
          {
            ystep=1;
            yresosize=yMul*pas.yEdgeLength;
          }
          else
          {
            ystep=-yMul;
            yresosize=pas.yEdgeLength;
          }

          ArrayDouble.D3 inputX = new ArrayDouble.D3(xresosize/xstep,yresosize/ystep, (int)(exp.subExp[expId].trialLength/exp.recorder.vectorTimeBinSize));
          ArrayDouble.D3 inputY = new ArrayDouble.D3(xresosize/xstep,yresosize/ystep, (int)(exp.subExp[expId].trialLength/exp.recorder.vectorTimeBinSize));

          for(int xx=0 ; xx < xresosize; xx+=xstep)
          {
            for(int yy=0; yy< yresosize; yy+=ystep)
            {
              for( int ii = 0 ; ii < (int)(exp.subExp[expId].trialLength/exp.recorder.vectorTimeBinSize) ; ii++)
              {

                Double outDX = vectorCounterX.get("E"+expId+"V"+vID+"B"+ii+"C"+xx+","+yy);
                Double outDY = vectorCounterY.get("E"+expId+"V"+vID+"B"+ii+"C"+xx+","+yy);
                if(outDX==null) outDX= new Double(0.0);
                if(outDY==null) outDY= new Double(0.0);
                inputX.set(xx/xstep,yy/ystep,ii,(double)outDX/(double)(exp.subExp[expId].repetition));
                inputY.set(xx/xstep,yy/ystep,ii,(double)outDY/(double)(exp.subExp[expId].repetition));
              }
            }
          }
          int [] origin = new int [] {0,0,0};
          ncfile.write("SubExp" + expId + SEPARATOR + exp.recorder.vectorNames.get(vID) + SEPARATOR + "X", origin, inputX);
          ncfile.write("SubExp" + expId + SEPARATOR + exp.recorder.vectorNames.get(vID) + SEPARATOR + "Y", origin, inputY);
        }
        
        //recorder for the intra ele
        for ( int tID = 0; tID < exp.subExp [expId].repetition; tID ++)
        {
          for(int iID = 0 ; iID < exp.recorder.intraEle.size(); iID++)
          {
            ArrayDouble.D1 inputV = new ArrayDouble.D1((int)(exp.subExp[expId].trialLength/exp.recorder.intraTimeBinSize));
            ArrayDouble.D2 inputC = new ArrayDouble.D2( exp.recorder.currChannel.get(iID).size(), (int)(exp.subExp[expId].trialLength/exp.recorder.intraTimeBinSize));

            final int index = iID 
                              + tID * exp.recorder.intraEle.size ( )
                              + expId * exp.recorder.intraEle.size ( )
                                      * exp.subExp [expId].repetition;

            LinkedList<IntraInfo> 
              currList = (LinkedList<IntraInfo>) intraReceiver [index];
            Iterator<IntraInfo> intraData = currList.iterator();

            for(int xx=0 ; xx < (int)(exp.subExp[expId].trialLength/exp.recorder.intraTimeBinSize); xx++)
            {
              IntraInfo tmp = intraData.next();

              inputV.set(xx, tmp.memV);

              for(int yy=0 ; yy < exp.recorder.currChannel.get(iID).size(); yy++)
              {
                inputC.set(yy, xx, tmp.curr.get(yy));
              }
            }

            int [] origin = new int [] {0};
            ncfile.write("SubExp" + expId + SEPARATOR 
                       + "trial" + tID  + SEPARATOR 
                       + exp.recorder.intraNames.get(iID) + SEPARATOR
                       + "V",
                     origin, inputV);
          
            origin = new int [] {0,0};
            ncfile.write("SubExp" + expId + SEPARATOR 
                + "trial" + tID  + SEPARATOR 
                + exp.recorder.intraNames.get(iID) + SEPARATOR
                + "C",
              origin, inputC);         
            }
        }
      } // END: for subexp loop
//    receiver.clear();
      ncfile.close();
    }

    public void  plot ( )
    ////////////////////////////////////////////////////////////////////////
    {
      for(int expId=0; expId < exp.subExp.length; expId++)
      {       

        XYSeries xyseries[]= new XYSeries[exp.recorder.singleUnit.size()];

        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        for(int tID=0; tID<exp.subExp[expId].repetition; tID++)
        {


          //single unit
          for( int sID= 0; sID < exp.recorder.singleUnit.size(); sID++) 
          {
            LinkedList<Double> tmp = receiver.get("E"+expId+"T"+tID+"N"+exp.recorder.singleUnit.get(sID));
            if(tmp!=null)
            {
              int ii =0;
              for(double val : tmp)
              {
                if(xyseries[sID] == null) xyseries[sID] = new XYSeries(exp.recorder.suNames.get(sID));
                xyseries[sID].add(val,1.0/(double)exp.recorder.singleUnit.size()*(double)sID+(double)tID);

              }

            }
          }

        } // END: for all trials in a subexp
        for(int sID= 0; sID < exp.recorder.singleUnit.size(); sID++)
        {
          xyseriescollection.addSeries(xyseries[sID]);
        }

        JFreeChart jfreechart = ChartFactory.createScatterPlot("Subexp"+expId+" Raster Plot","time","Trials",xyseriescollection, PlotOrientation.VERTICAL, true,false,false);
        XYPlot xyplot = (XYPlot) jfreechart.getPlot();
        xyplot.setNoDataMessage("NO DATA");
        xyplot.setRangeZeroBaselineVisible(true);
        xyplot.setDomainTickBandPaint(new Color(0, 100, 0, 50));
        xyplot.setRangeTickBandPaint(new Color(0, 100, 0, 50));
        ChartPanel chartpanel = new ChartPanel(jfreechart);
        chartpanel.setPreferredSize(new java.awt.Dimension(500, 270));
        ApplicationFrame appFram =  new ApplicationFrame("Result");
        appFram.setContentPane(chartpanel);
        appFram.pack();
        RefineryUtilities.centerFrameOnScreen(appFram);
        appFram.setVisible(true);
      }
    }
    
    ////////////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////////////
    
    private static void  recordMultiUnit (
      final Experiment            exp,
      final int                   expId,
      final Map<String, Integer>  multiCounter,
      final Map<String, Integer>  multiCounterAll,
      final NetcdfFileWriteable   ncfile )
      throws InvalidRangeException, IOException
    ////////////////////////////////////////////////////////////////////////
    {
      // LOGGER.trace ( "recordMultiUnit entered" );
      
      final int  binCount = ( int )
        ( exp.subExp [ expId ].trialLength / exp.recorder.timeBinSize );
      
      // LOGGER.trace ( "binCount {}", binCount );
      
      final ArrayList<ArrayList<Integer>>
        multiUnit = exp.recorder.multiUnit;
      
      final int  multiUnitCount = multiUnit.size ( );
    
      for ( int  mID = 0; mID < multiUnitCount; mID++ )
      {
        final ArrayDouble.D1  input = new ArrayDouble.D1 ( binCount );
        
        for ( int  ii = 0; ii < binCount; ii++ )
        {
          final String  key = "E" + expId + "N" + mID + "B" + ii;
          
          Integer  outInt = multiCounter.get ( key );
          
          if ( outInt == null )
          {
            outInt = new Integer ( 0 );
          }

          input.set (
            ii,
            ( double ) outInt
              / ( double ) ( exp.subExp [ expId ].repetition )
              / ( double )
                ( exp.recorder.multiUnit.get ( mID ).size ( ) ) );
        }
        
        final int [ ]  origin = new int [ ] { 0 };
        
        ncfile.write("SubExp" + expId + SEPARATOR + exp.recorder.multiNames.get(mID), origin, input);

        final ArrayDouble.D2  input2 = new ArrayDouble.D2 (
          exp.subExp[expId].repetition,
          binCount );
        
        for ( int  tID = 0; tID < exp.subExp [ expId ].repetition; tID++ )
        {
          for ( int  ii = 0; ii < binCount; ii++ )
          {
            final String
              key = "E" + expId + "T" + tID + "N" + mID + "B" + ii;
            
            Integer  outInt = multiCounterAll.get ( key );
            
            if ( outInt == null )
            {
              outInt = new Integer ( 0 );
            }

            input2.set (
              tID,
              ii,
              ( double ) outInt / ( double )
                ( exp.recorder.multiUnit.get ( mID ).size ( ) ) );
          }
        }
        
        final int [ ]  origin2 = new int [ ] { 0, 0 };
    
        ncfile.write("SubExp" + expId + SEPARATOR + exp.recorder.multiNames.get(mID) + SEPARATOR + "ALL", origin2, input2);
      }
      
      // LOGGER.trace ( "recordMultiUnit exiting" );      
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    }


