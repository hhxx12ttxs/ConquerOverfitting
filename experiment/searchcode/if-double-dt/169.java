    package cnslab.cnsnetwork;

    import java.util.*;

    import cnslab.cnsmath.*;
    import edu.jhu.mb.ernst.model.Synapse;
    import edu.jhu.mb.ernst.util.slot.Slot;
    
    /***********************************************************************
    * MN model with STDP implemented.
    * 
    * See Mihalas and Niebur 2009 paper.
    * 
    * @version
    *   $Date: 2012-08-04 20:43:22 +0200 (Sat, 04 Aug 2012) $
    *   $Rev: 104 $
    *   $Author: croft $
    * @author
    *   Yi Dong
    * @author
    *   David Wallace Croft
    * @author
    *   Jeremy Cohen
    ***********************************************************************/
    public final class  VSICLIFNeuronV2
      implements Neuron
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    {
    
    //  public Axon axon;
    
    //  private double[] curr;//0 is exc current, 1 is inhib current
    
    //  private double memV;
    
    private double  timeOfNextFire;
    
    private double  timeOfLastUpdate;
    
    /** whether the neuron is recordable or not */
    private boolean  record;

    /** table which stores presynaptic event time and weight */
    private Map<Synapse, TimeWeight>  histTable;
    
    //private double Alpha_LTD=1.0;//weight of strenght of LTD
    //private double Alpha_LTP=1.0;//weight of strenght of LTP
    //private double K_LTD=1/0.020; //LTD inverse time constant
    //private double K_LTP=1/0.005; //LTP inverse time constant
    //parameters were moved to VSICLIFNouronParaV2

    //  public static PrintStream p;
    //  public static boolean print=false;

    /** minimum rising time for neuron to fire */
    @Deprecated
    public static double  MINRISETIME = 1e-4;
    
    /** time accuracy */
    public static double  tEPS = 1e-12;
    
    /** the criteria for removing synapses, if a synaptic channel does not
    get an input for a long time such that its current decays below rEPS,
    it is removed from computations of the voltage and future updates up
    until it gets an input */
    public static double  rEPS = 1e-22;

    /** initialization of the maxWeight which was used in a previous method
    in doesfire */
    public static double  maxWeight = 0;

    /** the cost associated with neuron update,see D'hane's paper */
    public static double  cost_Update = 10;
    
    /** the cost associated with queue schedule. see D'hane's paper */
    public static double  cost_Schedule = 1;
    
    /** total cost, the costs associated to putting an temporary spike time
    in */
    public static double  cost;

    /** average time interval a neuron received spikes */
    public double  tAvg;
    
    /** last time the neuron received a spike */
    public double  lastInputTime;
    
    /** last time the neuron fired, negative means no last spike */
    public double  lastFireTime = -1.0;
    
    /** clamp the membrane voltage during absolute refractory period */
    public double  clamp_v;
    
    /** clamp the threshold during absolute refractory period */
    public double  clamp_t;
    
    /** judge whether the neuron fires or not */
    public boolean  fire; 

    /** long imposes a constraint on the maximum number of hosts to be 64 */
    public long  tHost;
    
    /** Neuron parameters */
    public VSICLIFNeuronParaV2  para;

    /** linked list for state variables */
    public TreeSet<State>  state;
    
    /** pointer to the state variables; the first two states correspond to
    the g and b terms respectively, followed by the spike-induced current
    terms */
    public State [ ]  sta_p;

    ////////////////////////////////////////////////////////////////////////
    // inner classes
    ////////////////////////////////////////////////////////////////////////
    
    public final class  State
      implements Comparable<State>
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    {
  
    public double  time;
  
    public double  value;

    public byte    id;

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////

    public State (
      final double  time,
      final double  value,
      final int     id )
    ////////////////////////////////////////////////////////////////////////
    {
      this.time  = time;

      this.value = value;

      this.id    = ( byte ) id;
    }

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////

    @Override
    public int  compareTo ( final State  arg0 )
    ////////////////////////////////////////////////////////////////////////
    {
      if ( para.allDecays [ this.id ] < para.allDecays [ arg0.id ] )
      {
        return 1;
      }
      else if ( para.allDecays [ this.id ] > para.allDecays [ arg0.id ] )
      {
        return -1;
      }
      else
      {
        if ( this.id < arg0.id )
        {
          return -1;
        }
        else if ( this.id > arg0.id )
        {
          return 1;
        }
        else
        {
          return 0;
        }
      }
    }

    @Override
    public String  toString ( )
    ////////////////////////////////////////////////////////////////////////
    {
      return
        "time:"   + time
        + " value:" + value
        + " id:"    + id
        + " decay:" + para.allDecays [ id ];
    }

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    }

    ////////////////////////////////////////////////////////////////////////
    // constructor methods
    ////////////////////////////////////////////////////////////////////////
    
    public  VSICLIFNeuronV2 ( final VSICLIFNeuronParaV2  para )
    ////////////////////////////////////////////////////////////////////////
    {
      this.para = para;
      
      if ( maxWeight
        < para.CAP * ( para.THRESHOLD - para.VRESET ) / para.MINRISETIME )
      {
        // not used
        
        maxWeight
          = para.CAP * ( para.THRESHOLD - para.VRESET ) / para.MINRISETIME;
      }
    }

    ////////////////////////////////////////////////////////////////////////
    // interface Neuron accessor methods
    ////////////////////////////////////////////////////////////////////////
    
    /*
    public Axon  getAxon()
    {
      return this.axon;
    }
    */

    @Override
    public double [ ]  getCurr ( final double  currTime )
    ////////////////////////////////////////////////////////////////////////
    {
      final double [ ]  out = new double [ para.DECAY.length ];
      
      for ( int  a = 0; a < para.DECAY.length; a++ )
      {
        if ( sta_p [ a + 2 ] == null )
        {
          out [ a ] = 0;
        }
        else
        {
          out [ a ] = sta_p [ a + 2 ].value * Math.exp (
            -( currTime - sta_p [ a + 2 ].time ) * para.DECAY [ a ] )
            * para.GLCAPDECAY [ a ] / ( 1 - para.ABDECAY [ a ] );
        }
      }
      
      return out;
    }    
    
    @Override
    public double  getMemV ( final double  currTime )
    ////////////////////////////////////////////////////////////////////////
    {
      if ( currTime < timeOfLastUpdate )
      {
        // if input comes inside the refractory period, memV not changed
        
        return memVoltage ( timeOfLastUpdate );
      }
      else
      {       
        return memVoltage (currTime);
      }
    }

    @Override
    public boolean  getRecord ( )
    ////////////////////////////////////////////////////////////////////////
    {
      return this.record;
    }

    @Override
    public long  getTHost ( )
    ////////////////////////////////////////////////////////////////////////
    {
      return tHost;
    }

    @Override
    public double  getTimeOfNextFire ( )
    ////////////////////////////////////////////////////////////////////////
    {
      return this.timeOfNextFire;
    }

    @Override
    public boolean  isSensory ( )
    ////////////////////////////////////////////////////////////////////////
    {
      return false;
    }

    @Override
    public boolean  realFire ( )
    ////////////////////////////////////////////////////////////////////////
    {
      //checks if the inserted spike is true,
      // or if it a temporary placeholder
      
      return fire;
    }

    ////////////////////////////////////////////////////////////////////////
    // interface Neuron mutator methods
    ////////////////////////////////////////////////////////////////////////
    
    @Override
    public void  setRecord ( final boolean  record )
    ////////////////////////////////////////////////////////////////////////
    {
      this.record = record;
    }

    @Override
    public void  setTHost ( final long  id )
    ////////////////////////////////////////////////////////////////////////
    {
      this.tHost = id;
    }

    @Override
    public void  setTimeOfNextFire ( final double  timeOfNextFire )
    ////////////////////////////////////////////////////////////////////////
    {
      this.timeOfNextFire = timeOfNextFire;
    }

    ////////////////////////////////////////////////////////////////////////
    // interface Neuron lifecycle methods
    ////////////////////////////////////////////////////////////////////////
    
    @Override
    public void  init (
      final int      expid,
      final int      trialid,
      final Seed     idum,
      final Network  net,
      final int      id )
    ////////////////////////////////////////////////////////////////////////
    {
      // initialization all the initial parameters.
      
      cost = Math.log ( cost_Schedule / cost_Update + 1.0 );
      
      this.tAvg = 0.005; // initial value is 5 ms;
      
      this.lastInputTime = 0.0;
      
      this.timeOfLastUpdate = 0.0;
      
      this.timeOfNextFire = -1;
      
      double  memV = para.ini_mem + para.ini_memVar * Cnsran.ran2 ( idum );
      
      double  instantaneousThreshold = para.ini_threshold;

      double [ ]  curr = new double [ para.ini_curr.length ];
      
      for ( int  a = 0; a < curr.length; a++ )
        curr [ a ] = para.ini_curr [ a ];

      para.CON_v = para.IEXT / para.GL;
      
      para.CON = para.IEXT / para.GL - ( para.THRESHOLD - para.VREST
        + para.A / para.B * para.IEXT / para.GL );

      // table history presynaptic event time and relative weight
      
      histTable = new HashMap<Synapse,TimeWeight> ( );
      
      // last time the neuron fired
      
      this.lastFireTime = -1.0;

      //state variables setup

      //create linked table
      
      state = new TreeSet<State> ( );
      
      sta_p = new State [ para.DECAY.length + 2 ];
      
      // First state is Ng term;
      
      sta_p [ 0 ] = new State ( 0, getNG ( memV, curr ), 0 );
      
      state.add ( sta_p [ 0 ] );
      
      sta_p [ 1 ] = new State (
        0,
        getNB ( instantaneousThreshold, memV, curr ),
        1 );
      
      state.add ( sta_p [ 1 ] );

      for ( int  i = 2; i < para.DECAY.length + 2; i++ )
      {
        sta_p [ i ] = new State ( 0, getCurr ( i - 2, curr ), i );
        
        state.add ( sta_p [ i ] );
      }
      
      /*
    //System.out.println("starting");
    Iterator<State> iter= state.iterator();
    while(iter.hasNext())
    {
      //System.out.println(iter.next());
    }
    //System.out.println("ending");
       */

      double  dt, vc;
      
      double  de = safeD ( 0.0 );
      
      double  nextTime = ( -( memV - instantaneousThreshold ) / de );
      
      dt = nextTime;
      
      // if(nowV-nowT>0)return -1.0;
      
      while ( de > 0 && nextTime < 1.0
        && !( dt > 0 ? dt < tEPS : -dt < tEPS ) ) 
      {
        de = safeD ( nextTime );
        
        vc = memStheta ( nextTime );
        
        dt = ( -vc / de );
        
        // //System.out.print("n:"+nextTime+" s:"+(-vc)+"/"+de+"="
        // //  +dt+" Etime:"+(time+nextTime));
        
        nextTime += dt; 
      }

      if ( de <0 || nextTime > 1.0 )
      {
        fire = false;
        
        nextTime = -1.0;
      }
      else if ( ( dt > 0 ? dt < tEPS : -dt < tEPS ) )
      {
        fire = true;
      }
      else
      {
        //System.out.println("error condition in init");
      }

      if ( nextTime < 0 )
      {
        // neuron will not fire at all
      }
      else
      {
        final Slot<FireEvent>  fireEventSlot = net.getFireEventSlot ( );
        
        // nextTime = (timeOfLastUpdate>0.0)
        // ?(timeOfLastUpdate-0.0-Math.log(nextTime)/para.K)
        // :(-1.0*Math.log(nextTime)/para.K);
        
        if ( net.getClass ( ).getName().equals (
          "cnslab.cnsnetwork.ANetwork" ) )
        {
          fireEventSlot.offer (
            new AFireEvent (
              id,
              nextTime,
              net.info.idIndex,
              ( ( ANetwork ) net ).aData.getId ( ( ANetwork ) net ) ) );
        }
        else if ( net.getClass ( ).getName ( ).equals (
          "cnslab.cnsnetwork.Network" ) )
        {
          fireEventSlot.offer ( new FireEvent ( id, nextTime ) );
        }
        else
        {
          throw new RuntimeException (
            "Other Network Class doesn't exist" );
        }
        
        timeOfNextFire = nextTime;
      }
    }

    @Override
    public double  updateFire ( )
    ////////////////////////////////////////////////////////////////////////
    {
      // System.out.println("fire at:"+timeOfNextFire);
      
      //update state variables containing membrane voltage and threshold
      //because of the resetting.
      
      double  baseline;
      
      double  nowV, nowT;
      
      if ( fire ) // for real fire
      {
        //Iterator<Synapse,WeightTime> it = histTable.iterator();
        
        for ( final Map.Entry<Synapse,TimeWeight>  entry
          : histTable.entrySet ( ) )
        {
          final Synapse  syn = entry.getKey ( );
          
          // get the relative weight
          
          final TimeWeight  tw = entry.getValue();

          // channel 0 has STDP
          
          if ( syn.getType ( ) == 0 )
          {
            // LTP only for close spikes
            
            if ( lastFireTime < tw.time )
            {
              //update the weight
              
              tw.weight = tw.weight
                * ( 1 + para.Alpha_LTP
                  * Math.exp ( -para.K_LTP
                    * ( timeOfNextFire-tw.time ) ) );
            }
          }
        }

        // Store the old neuron fire time.
        
        lastFireTime = timeOfNextFire;
        
        // backup current threshold
        
        final double  backCT
          = memVoltage ( timeOfNextFire ) - memStheta ( timeOfNextFire );
        
        // G term update
        
        sta_p [ 0 ].value *= Math.exp (
          -( timeOfNextFire - sta_p [ 0 ].time + para.ABSREF )
          * para.allDecays [ 0 ] );
        
        sta_p [ 0 ].time = timeOfNextFire + para.ABSREF;
        
        // B term update
        
        sta_p [ 1 ].value *= Math.exp (
          -( timeOfNextFire-sta_p [ 1 ].time + para.ABSREF )
          * para.allDecays [ 1 ] );
        
        sta_p [ 1 ].time = timeOfNextFire + para.ABSREF;

        //membrane voltage reset
        
        //future memVoltage
        
        final double
          cVoltage = memVoltage ( timeOfNextFire + para.ABSREF );
        
        // change memStheta(timeOfNextFire) to 0 later on may be beneficial
        
        // double cThreshold = cVoltage - memStheta(timeOfNextFire);
        
        // change memStheta(timeOfNextFire) to 0 later on may be beneficial
        
        // double  cThreshold
        //   = memVoltage(timeOfNextFire) - memStheta(timeOfNextFire);
        
        // System.out.println (
        //   "abT:"+backCT+" memV:"+memVoltage(timeOfNextFire));
        
        // System.out.println ( "abT2:"
        //   +(memVoltage(timeOfNextFire) - memStheta(timeOfNextFire)));

        // membrane voltage offset
        
        final double  vOffset = para.VRESET - cVoltage;
        
        // threshold in future
        
        final double
          cdiff = ( cVoltage - memStheta ( timeOfNextFire + para.ABSREF ) );
        
        // threshold offset
        
        final double  tOffset
          = Math.max (
            para.RRESET,
            backCT + para.THRESHOLDADD )
          - cdiff;

        sta_p [ 0 ].value += ( 1 - para.ABGLCAP ) * vOffset;
        
        sta_p [ 1 ].value += para.ABGLCAP * vOffset - tOffset; 

        // System.out.println("after fire voltage:"
        //   +memVoltage(timeOfNextFire+para.ABSREF)
        //   +" v-t:"+memStheta(timeOfNextFire+para.ABSREF));
        
        //    if(clamp_v==Double.MAX_VALUE) //if clamp_v is not set;

        clamp_v = para.VRESET;
        
        clamp_t = Math.max ( para.RRESET, backCT + para.THRESHOLDADD );

        // this section is for spike triggered current
        
        for ( int  a = 0; a < para.DECAY.length; a++ )
        {
          // update the currents if spike trigger current is non zero
          
          if ( para.IRATIO [ a ] != 1.0 || para.IADD [ a ] != 0 )
          {
            // System.out.println("added");
            
            // update current to fire time;
            
            if ( sta_p [ a + 2 ] == null ) //activate it
            {
              //System.out.println("act");
              
              sta_p [ a + 2 ] = new State (
                timeOfNextFire + para.ABSREF,
                0,
                a + 2 );
              
              state.add ( sta_p [ a + 2 ] );
            }
            else
            {
              sta_p [ a + 2 ].value *= Math.exp (
                -( timeOfNextFire - sta_p [ a + 2 ].time + para.ABSREF )
                * para.allDecays [ a + 2 ] );
              
              sta_p [ a + 2 ].time = timeOfNextFire + para.ABSREF;
            }

            // current at timeofnextfire
            
            final double  currNow = sta_p [ a + 2 ].value
              * para.GLCAPDECAY [ a ]
              / ( 1 - para.ABDECAY [ a ] );
            
            final double  currAfter = ( para.IRATIO [ a ] * currNow
              * Math.exp ( para.ABSREF * para.allDecays [ a + 2 ] )
              + para.IADD [ a ] )
              * Math.exp ( -para.ABSREF * para.allDecays [ a + 2 ] );
            
            final double  changeCurr = currAfter - currNow;
            
            // System.out.print("after:"+ currAfter+" now:"+currNow+" ");

            sta_p[0].value
              += changeCurr / para.GLCAPDECAY [ a ] * ( para.ABGLCAP - 1 );
            
            sta_p[1].value
              -= changeCurr / para.BCAPDECAY [ a ] * para.ABGLCAP;
            
            sta_p[a+2].value
              += ( changeCurr / para.GLCAPDECAY [ a ] )
              * ( 1 - para.ABDECAY [ a ] );
            
            /*
            sta_p[0].value
              += input.weight/para.GLCAPDECAY[a]*(para.ABGLCAP-1);
               
            sta_p[1].value
              -= input.weight/para.BCAPDECAY[a]*para.ABGLCAP;
               
            sta_p[a+2].value
              += (changeCurr/para.GLCAPDECAY[a])*(1 - para.ABDECAY[a]);
            */
            
            // System.out.print ( " bycalcu:" + sta_p [ a + 2 ].value
            //   * para.GLCAPDECAY [ a ] / ( 1 - para.ABDECAY [ a ] )
            //   + " " );
          }
        }

        // System.out.println ( "after fire curr:"
        //   + getCurr ( timeOfNextFire + para.ABSREF ) [ 0 ]
        //   +" "+getCurr(timeOfNextFire+para.ABSREF)[1]);

        timeOfLastUpdate = timeOfNextFire+para.ABSREF;
        
        baseline  = timeOfNextFire+para.ABSREF;
        
        nowV = para.VRESET;
        
        nowT = Math.max ( para.RRESET, backCT + para.THRESHOLDADD );
      }
      else //neuron is partial updated.
      {
        baseline = timeOfNextFire;
        
        nowV = memStheta(baseline);
        
        nowT = 0;
      }

      double  dt, vc;
      
      double  de = safeD ( baseline );
      
      double  nextTime = ( -( nowV - nowT ) ) / de;
      
      dt = nextTime;

      while ( de > 0 && nextTime < cost * tAvg ) 
      {
        de = safeD ( baseline + nextTime );
        
        vc = memStheta ( baseline + nextTime );
        
        dt = ( -vc / de );
        
        if ( dt > 0 ? dt < tEPS : -dt < tEPS )
        {
          fire = true;
          
          //no spike within absolute refractory period.
          
          //if(nextTime < para.ABSREF) return -1.0;
          
          //System.out.println(
          //  "Again:"+(nextTime+baseline - timeOfNextFire));
          
          return nextTime + baseline - timeOfNextFire;
        }
        
        nextTime += dt; 
      }

      if ( de < 0 || nextTime > 1.0 )
      {
        fire = false;
        
        return -1.0;
      }
      else
      {
        fire = false;
        
        return  nextTime + baseline - timeOfNextFire;
      }
    }

    @Override
    public double  updateInput (
      final double   time,
      final Synapse  input )
    ////////////////////////////////////////////////////////////////////////
    {
      if ( histTable.containsKey ( input ) )
      {
        // if this synapse is already in the table, just update its value
        
        TimeWeight  tw = histTable.get ( input );
        
        if ( input.getType ( ) == 0 ) //channel 0 has STDP
        {
          if ( lastFireTime > tw.time ) // LTD only for close spikes
          {
            // update weight if neuron fired before
            
            tw.weight = tw.weight * ( 1 - para.Alpha_LTD * Math.exp (
              -para.K_LTD * ( time - lastFireTime ) ) );
            
            // System.out.println("LTD:"+tw.weight);
          }
        }
        
        tw.time = time; // update the time
      }
      else // if this synapse did not fire before, add it to the table
      {
        TimeWeight  tw = new TimeWeight ( time, 1.0 );
        
        if ( input.getType ( ) == 0 ) //channel 0 has STDP
        {
          if ( lastFireTime > 0.0 )
          {
            // update weight if neuron fired before
            
            tw.weight = tw.weight * ( 1 - para.Alpha_LTD * Math.exp (
              -para.K_LTD * ( time - lastFireTime ) ) );
          }
        }
        
        // put the default weight into the history table
        
        histTable.put ( input, tw );
      }
      
      // System.out.println ( "get input at:" + time + " syn:" + input
      //   + " membrane voltage is:" + memVoltage ( time ) );
      
      // System.out.println ( "beg: t:" + time + " s:" + input
      //   + " mV:" + memVoltage ( time ) + " mtV:" + memStheta ( time ) );

      //update the tAvg; look for 5 points in the past
      
      tAvg = tAvg * 0.8 + ( time - lastInputTime ) *0.2;
      
      lastInputTime = time;
      
      // System.out.println ( "before updateInput:  memV: " + memV
      //   + " instantThresh: " + instantaneousThreshold + " curr0: "
      //   + curr[0] + " curr1: " + curr[1]);
      //
      // p.println("updateInput"+time);
      // p.flush();
      
      /*
      double [] oldcurr  = new double [curr.length];
      oldcurr[0] = curr[0];
      oldcurr[1] = curr[1];
      double oldMem=memV ;
      double VACC = 1E-9; // accuracy in voltage for spike to be generated
      */
      
      // System.out.println ( "0t:" + time + " s:" + input
      //   + " mV:" + memVoltage ( time ) + " mtV:" + memStheta ( time )
      //   + " th:" + ( memVoltage ( time ) - memStheta ( time ) ) );

      // update state 0,1 to current time;
      
      sta_p [ 0 ].value *= Math.exp (
        -( time - sta_p [ 0 ].time ) * para.allDecays [ 0 ] );
      
      // System.out.println ( "ng decay:" + Math.exp (
      //   -( time - sta_p [ 0 ].time ) * para.allDecays [ 0 ] ) );

      sta_p [ 0 ].time = time;
      
      sta_p [ 1 ].value *= Math.exp (
        -( time - sta_p [ 1 ].time ) * para.allDecays [ 1 ] );
      
      sta_p [ 1 ].time = time;

      // System.out.println ( "mid1: t:" + time + " s:" + input + " mV:"
      //   + memVoltage ( time ) + " mtV:" + memStheta ( time ) );

      //check whether the input is deleted
      
      if ( sta_p [ input.getType ( ) + 2 ] == null ) //activate it
      {
        //System.out.println("act");
        
        sta_p [ input.getType ( ) + 2 ] = new State ( time, 0, input.getType ( ) + 2 );
        
        state.add ( sta_p [ input.getType ( ) + 2 ] );
      }
      else
      {
        sta_p [ input.getType ( ) + 2 ].value *= Math.exp ( -( time - sta_p [
          input.getType ( ) + 2].time ) * para.allDecays [ input.getType ( ) + 2 ] );
        
        sta_p[input.getType ( )+2].time = time;
      }

      // System.out.println ( "mid2: t:" + time + " s:" + input + " mV:"
      //   + memVoltage ( time ) + " mtV:" + memStheta ( time ) );

      // add the extra state values (0,2,this synapse) by getting input
      // spike.
      
      sta_p [ 0 ].value += input.getWeight ( ) * ( histTable.get ( input ).weight )
        / para.GLCAPDECAY [ input.getType ( ) ] * ( para.ABGLCAP - 1 );
      
      sta_p [ 1 ].value -= input.getWeight ( ) * ( histTable.get ( input ).weight )
        / para.BCAPDECAY  [ input.getType ( ) ] * para.ABGLCAP;
      
      sta_p [ input.getType ( ) + 2 ].value
        += ( input.getWeight ( ) * ( histTable.get ( input ).weight )
          / para.GLCAPDECAY [ input.getType ( ) ] )
          * ( 1 - para.ABDECAY [ input.getType ( ) ] );

      double nextTime;
      
      double baseline;
      
      double nowV = memVoltage(time);
      
      double nowT = nowV - memStheta(time);
      
      if ( time < timeOfLastUpdate )
      {
        // set the offset of the membraneVoltage to make sure at the
        // timeOfLastUpdate the membraneVoltage is not changed at all
        
        // CON_v = CON_v - (memStheta(timeOfLastUpdate) - nowMem);
        
        /*
        if(clamp_v==Double.MAX_VALUE) //if clamp_v is not set;
        {
          clamp_v= nowV;
          clamp_t= nowT;
        }
        */
        
        double vFuture = memVoltage ( timeOfLastUpdate );
        
        double voltageOffset = ( vFuture - clamp_v );
        
        double tOffset = vFuture - memStheta ( timeOfLastUpdate ) - clamp_t;
        
        sta_p [ 0 ].value += -( 1 - para.ABGLCAP ) * voltageOffset
          * Math.exp ( ( timeOfLastUpdate - time ) * para.allDecays [ 0 ] );
        
        sta_p [ 1 ].value += -( para.ABGLCAP * voltageOffset - tOffset )
          * Math.exp ( ( timeOfLastUpdate - time ) * para.allDecays [ 1 ] );
        
        // System.out.println ( "nowV:" + nowV
        //   + " futV:" + memVoltage ( timeOfLastUpdate )
        //   + " clamp_v:" + clamp_v );
        
        // System.out.println ( "nowT:" + nowT
        //   + " futT:" + ( memVoltage ( timeOfLastUpdate )
        //     - memStheta ( timeOfLastUpdate ) )
        //   + " clamp_6:" + clamp_t );
        
        //  sta_p[0].value += (1-para.ABGLCAP)*vOffset;        
        //  sta_p[1].value += para.ABGLCAP*vOffset - tOffset;
        
        nowV = clamp_v;
        
        nowT = clamp_t;
        
        baseline = timeOfLastUpdate;
      }
      else
      {
        clamp_v=Double.MAX_VALUE;
        
        clamp_t=Double.MAX_VALUE;
        
        //if(input.to==1)
        {
          //System.out.println("t:"+time+ " s:"+input+ " mV:"
          //+ memVoltage(time) +" mtV:"+memStheta(time)+ " th:"
          //+(memVoltage(time)-memStheta(time))+ " last:"+timeOfLastUpdate);
          
          // //System.out.println("t:"+time+ " s:"+input+ " mV:"+memV
          // +" mtV:"+ (memV- instantaneousThreshold)+ " th:"
          // +instantaneousThreshold);
          //    double [] out= getCurr(time);
          //System.out.println("c0:"+out[0]+ " c1:"+out[1]);
        }
        
        timeOfLastUpdate=time;
        
        baseline = time;
      }

      double  dt, vc;
      
      double  de = safeD ( baseline );
      
      nextTime = ( -( nowV - nowT ) / de );
      
      dt = nextTime;
      
      //  if(nowV-nowT>0)return -1.0;
              
      while ( de > 0 && nextTime < cost * tAvg ) 
      {
    	  de = safeD ( baseline + nextTime );

    	  vc = memStheta ( baseline + nextTime );

    	  dt = ( -vc / de );

    	  // //System.out.print("n:"+nextTime+" s:"+(-vc)+"/"+de+"="+dt
    	  // +" Etime:"+(time+nextTime));

    	  if ( dt > 0 ? dt < tEPS : -dt < tEPS )
    	  {
    		  fire = true;

    		  // //System.out.println();
    		  // if(nextTime < timeOfLastUpdate - time)
    		  // {
    		  //   //System.out.println("fail to fire nextTime:"+nextTime
    		  //   //+" smaller than:"+(timeOfLastUpdate - time));
    		  //   return -1.0; //no spike within absolute refractory period.
    		  // }
    		  //System.out.println("N :"+input.to+" fire:"
    		  //+(time>timeOfLastUpdate?nextTime
    		  //:nextTime+timeOfLastUpdate-time)
    		  //+ " memV:"+memStheta(baseline+nextTime));

    		  return ( time > timeOfLastUpdate
    				  ? nextTime : nextTime + timeOfLastUpdate - time );
    	  }

    	  nextTime += dt; 
      }

      if( de < 0 || nextTime > 1.0 )
      {
        // input is not strong enough to make neuron fire
        
        fire = false;
        
        return -1.0;
      }
      else
      {
        // neuron dot fire yet, but eventually will fire in future,
        // do a partial update.
        
        fire = false;
        
        return ( time > timeOfLastUpdate
          ? nextTime : nextTime + timeOfLastUpdate - time );
      }
      // //System.out.println();
      // //System.out.println("fail to fire dt:"+dt+ " nextTime:"
      // //  +nextTime + " test:"+memStheta(time+0.10316241162745285)
      // //  + " deri:"+de+ " safeD:"+safeD(time+0.10316241162745285));
      //return -1.0;
    }

    ////////////////////////////////////////////////////////////////////////
    // accessor methods
    ////////////////////////////////////////////////////////////////////////
    
    /***********************************************************************
    * get the current which is the last updated current
    ***********************************************************************/
    public double [ ]  getCurr ( )
    ////////////////////////////////////////////////////////////////////////
    {
      final double [ ]  out = new double [ para.DECAY.length ];
      
      for ( int  a = 0; a < para.DECAY.length; a++ )
      {
        if ( sta_p [ a + 2 ] == null )
        {
          out [ a ] = 0;
        }
        else
        {
          out [ a ] = sta_p [ a + 2 ].value * para.GLCAPDECAY [ a ]
            / ( 1 - para.ABDECAY [ a ] );
        }
      }
      
      return out;
    }

    /***********************************************************************
    * Nj terms:
    ***********************************************************************/
    public double  getCurr (
      final int   a,
      double [ ]  curr )
    ////////////////////////////////////////////////////////////////////////
    {
      //Nj terms:
      
      return ( curr [ a ] / para.GLCAPDECAY [ a ] )
        * ( 1 - para.ABDECAY [ a ] );
    }

    /***********************************************************************
    * return nb term;
    ***********************************************************************/
    public double  getNB (
      final double      instantaneousThreshold,
      final double      memV,
      final double [ ]  curr ) //return nb term;
    ////////////////////////////////////////////////////////////////////////
    {
      double  nbTerm
        = para.VREST - memV + para.IEXT / ( para.B * para.CAP );
      
      for ( int  a = 0; a < curr.length; a++ )
        nbTerm += curr [ a ] / para.BCAPDECAY [ a ];
        // replace b*cap for para.GL
      
      nbTerm *= para.ABGLCAP;
      
      nbTerm += instantaneousThreshold - para.THRESHOLD;
      
      return -nbTerm;
    }

    /***********************************************************************
    * Get ng term  ( membrane voltage - ABGLCAP*membranevoltageNg )
    ***********************************************************************/
    public double  getNG (
      final double      memV,
      final double [ ]  curr ) 
    ////////////////////////////////////////////////////////////////////////
    {
      // return ng term ( membrane voltage - ABGLCAP*membranevoltageNg );
      
      double  ngTerm1 = memV - para.VREST - para.IEXT / para.GL;
      
      for ( int  a = 0; a < curr.length; a++ )
        ngTerm1 -= curr [ a ] / para.GLCAPDECAY [ a ];
      
      return ngTerm1 - ngTerm1 * para.ABGLCAP;
    }

    public double  getSensoryWeight ( )
    ////////////////////////////////////////////////////////////////////////
    {
      throw new RuntimeException ( "This neuron type doesn't use the "
        + "Sensory Weight Functions!" );
    }

    public double  getTimeOfLastUpdate ( )
    ////////////////////////////////////////////////////////////////////////
    {
      return this.timeOfLastUpdate;
    }

    /***********************************************************************
    * get the membrane voltage
    * 
    * @param t
    *   absolute time t
    ***********************************************************************/ 
    public double  memVoltage ( final double  t )
    ////////////////////////////////////////////////////////////////////////
    {
      /*
    double V=0.0;
    Iterator<State> iter = state.iterator();
    while(iter.hasNext())
    {
      State tmpState = iter.next();
      if((int)tmpState.id != 1 ) // nb term will be ignored.
      {
        if(t-tmpState.time!=0)
        {
          if((int)tmpState.id == 0 ) // ng term
          {
            V += Math.exp(-(t-tmpState.time)
            *para.allDecays[(int)tmpState.id])
            **tmpState.value/(1-para.ABGLCAP);
            // //System.out.print("ng:"+(tmpState.value/(1-para.ABGLCAP))
            // //+ " de:"+Math.exp(-(t-tmpState.time)
            // //*para.allDecays[(int)tmpState.id]));
          }
          else  //current terms
          {
            V += Math.exp(-(t-tmpState.time)*para.allDecays[
              (int)tmpState.id])*tmpState.value/(1 - para.ABDECAY[
              (int)tmpState.id-2]);
              
            // //System.out.print(" c:"+(tmpState.id-2)+" "+(
            // //tmpState.value/(1 - para.ABDECAY[(int)tmpState.id-2]))
            // //+ " de:"+Math.exp(-(t-tmpState.time)*para.allDecays[
            // //(int)tmpState.id]));
          }
        }
        else
        {
          if((int)tmpState.id == 0 ) // ng term
          {
            V += tmpState.value/(1-para.ABGLCAP);
            // //System.out.print(" ng:"+(tmpState.value/(1-para.ABGLCAP))
            // //+ " de:0");
          }
          else  //current terms
          {
            V += tmpState.value/(1 - para.ABDECAY[(int)tmpState.id-2]);
            // //System.out.print(" c:"+(tmpState.id-2)+" "
            // //+(tmpState.value/(1 - para.ABDECAY[(int)tmpState.id-2]))
            // //+" de:0");
          }
        }
      }
    }
    // //System.out.print("V:"+V+" memV:"+(V+para.CON_v+para.VREST)+"\n");
    // //System.out.println("V:"+V+" memV:"+(V+para.CON_v+para.VREST));
    //System.out.println((V+para.CON_v+para.VREST));
       */

      double  V = 0;
      
      double  constant = 0;
      
      Iterator<State>  iter = state.iterator ( );
      
      State  first = iter.next ( );
      
      State  second;
      
      double  firstV, secondV;

      if ( first.id == 1 ) //ignore nb term
      {
        if ( iter.hasNext ( ) )
        {
          first = iter.next ( );
        }
        else
        {
          return V + constant + para.CON_v + para.VREST;
        }
      }

      if ( first.time == t ) // ignore no time change term
      {
        if ( first.id == 0 )
        {
          constant += first.value / ( 1 - para.ABGLCAP );
        }
        else
        {
          constant
            += first.value / ( 1 - para.ABDECAY [ ( int ) first.id - 2 ] );
        }
        
        if ( iter.hasNext ( ) )
        {
          first = iter.next ( );
        }
        else
        {
          return V + constant + para.CON_v + para.VREST;
        }
      }

      if ( first.id == 1 ) //ignore nb term
      {
        if ( iter.hasNext ( ) )
        {
          first = iter.next ( );
        }
        else
        {
          return V + constant + para.CON_v + para.VREST;
        }
      }

      if ( first.id == 0 )
      {
        firstV = first.value / ( 1 - para.ABGLCAP );
      }
      else
      {
        //System.out.println(first.id);
        
        firstV
          = first.value / ( 1 - para.ABDECAY [ ( int ) first.id - 2 ] );
      }

      V = firstV;
      
      while ( iter.hasNext ( ) )
      {
        second = iter.next ( );
        
        secondV = 0.0;

        if ( second.time == t && second.id != 1 )
        {
          if ( second.id == 0 )
          {
            constant += second.value / ( 1 - para.ABGLCAP );
          }
          else
          {
            constant += second.value
              / ( 1 - para.ABDECAY [ ( int ) second.id - 2 ] );
          }
          
          if ( iter.hasNext ( ) )
          {
            second = iter.next ( );
          }
          else
          {
            break;
          }
        }

        if ( second.id == 1 )
        {
          if ( iter.hasNext ( ) )
          {
            second = iter.next ( );
          }
          else
          {
            break;
          }
        }

        if ( second.id == 0 )
        {
          secondV = second.value / ( 1 - para.ABGLCAP );
        }
        else
        {
          secondV = second.value
            / ( 1 - para.ABDECAY [ ( int ) second.id - 2 ] );
        }

        V = Math.exp ( -( ( para.allDecays [ ( int ) first.id ]
          - para.allDecays [ ( int ) second.id ] ) * t
          - ( para.allDecays [ ( int ) first.id ] * first.time
          - para.allDecays [ ( int ) second.id ] * second.time ) ) ) * V
          + secondV;
        
        first = second;
      }

      V = Math.exp ( -( para.allDecays [ ( int ) first.id ] * t
        - para.allDecays [ ( int ) first.id ] * first.time ) ) * V;
      
      //// System.out.println("V:"+(V+para.CON_v+para.VREST));
      
      return V + para.CON_v + para.VREST + constant;
    }

    ////////////////////////////////////////////////////////////////////////
    // mutator methods
    ////////////////////////////////////////////////////////////////////////
    
    /***********************************************************************
    * set the membrane voltage
    ***********************************************************************/
    public void  setMemV ( double  memV )
    ////////////////////////////////////////////////////////////////////////
    {
      return;
    }
    
    public void  setTimeOfLastUpdate ( final double  timeOfLastUpdate )
    ////////////////////////////////////////////////////////////////////////
    {
      this.timeOfLastUpdate = timeOfLastUpdate;
    }

    ////////////////////////////////////////////////////////////////////////
    // overridden Object methods
    ////////////////////////////////////////////////////////////////////////
    
    @Override
    public String  toString ( )
    ////////////////////////////////////////////////////////////////////////
    {
      String  tmp="";
      
      // tmp = tmp + "MemVol:" + memV + "\n";
      
      tmp=tmp+"Current:"+"\n";
      
      //    for(int i=0;i<curr.length;i++)
      //    {
      //      tmp=tmp+"       curr"+i+" "+curr[i]+"\n";
      //    }
      //    tmp=tmp+axon;
      
      return tmp;
    }

    ////////////////////////////////////////////////////////////////////////
    // miscellaneous methods
    ////////////////////////////////////////////////////////////////////////
    
    /***********************************************************************
    *  Membrane voltage computate mem = S+\theta
    *  
    *  @param t
    *    absolute time t
    ***********************************************************************/
    public double  memStheta ( double  t )
    ////////////////////////////////////////////////////////////////////////
    {
      /*
    double V=0.0;
    Iterator<State> iter = state.iterator();
    while(iter.hasNext())
    {
      State tmpState = iter.next();
      if(t-tmpState.time!=0)
      {
        V += Math.exp(-(t-tmpState.time)*para.allDecays[
          (int)tmpState.id])*tmpState.value;
      }
      else
      {
        V += tmpState.value;
      }
    }
    return V+para.CON;
       */
      double  V = 0, constant = 0.0;
      
      Iterator<State>  iter = state.iterator ( );
      
      State  first = iter.next ( );
      
      State  second;
      
      double  firstV, secondV;

      if ( Math.abs ( first.value ) < rEPS && ( int ) first.id > 1 ) 
      {
        // delete the synapse if it is small
        
        iter.remove ( );
        
        sta_p [ ( int ) first.id ] = null;
      }
      
      // ignore no time change term
      
      if ( first.time == t )
      {
        constant += first.value;
        
        if ( iter.hasNext ( ) )
        {
          first = iter.next ( );
        }
        else
        {
          return V + constant + para.CON;
        }
      }

      firstV = first.value;
      
      V = firstV;
      
      while ( iter.hasNext ( ) )
      {
        second = iter.next ( );
        
        if ( Math.abs ( second.value ) < rEPS && ( int ) second.id > 1 )
        {
          // delete the synapse if it is small
          iter.remove ( );
          
          sta_p [ ( int ) second.id ] = null;
        }
        
        secondV = 0.0;
        
        // ignore no time change term
        
        if ( second.time == t )
        {
          constant += second.value;
          
          if ( iter.hasNext ( ) )
          {
            second = iter.next ( );
          }
          else
          {
            break;
          }
        }

        secondV = second.value;
        
        V = Math.exp ( -( ( para.allDecays [ ( int ) first.id ]
          - para.allDecays [ ( int ) second.id ] ) * t
          - ( para.allDecays [ ( int ) first.id ] * first.time
          - para.allDecays [ ( int ) second.id ] * second.time ) ) )
          * V + secondV;
        
        first = second;
      }

      V = Math.exp ( -( para.allDecays [ ( int ) first.id ] * t
        - para.allDecays [ ( int ) first.id ] * first.time ) ) * V;
      
      ////System.out.println("V:"+(V+para.CON_v+para.VREST));
      
      return V + para.CON + constant;
    }

    /***********************************************************************
    * Safe derivative, good for Newton method, see D'hane paper
    * 
    * @param t
    *   absolute time t
    ***********************************************************************/
    public double  safeD ( double  t )
    ////////////////////////////////////////////////////////////////////////
    {
      // the smallest inverse decay of all negative state variables
      
      double  tauSafe = Double.MAX_VALUE;
      
      //find the tauSafe
      
      Iterator<State> iter = state.descendingIterator();
      
      while(iter.hasNext())
      {
        State tmpState = iter.next();
        
        // //System.out.println("value:"
        // //  +tmpState.value+" delay:"+para.allDecays[(int)tmpState.id]
        // //  + " id:"+(int)tmpState.id+ " time:"+tmpState.time);
        
        if ( tmpState.value < 0 )
        {
          tauSafe = para.allDecays [ ( int ) tmpState.id ];
          
          break;
        }
      }
      
      ////System.out.println("
      //    //System.out.println("safe:"+tauSafe);
      //calculate the dV
      //
      
      double  constant = 0;
      
      double  V = 0;
      
      iter = state.iterator ( );
      
      State  first = iter.next ( );
      
      State  second;
      
      double  firstV, secondV;

      if ( first.time == t )
      {
        // ignore no time change term
        
        if ( first.value > 0 )
        {
          final double  maxDecay = Math.min (
            para.allDecays [ ( int ) first.id ],
            tauSafe );
          
          constant += -first.value * maxDecay;
        }
        else
        {
          constant += -first.value * para.allDecays [ ( int ) first.id ];
        }

        if ( iter.hasNext ( ) )
        {
          first = iter.next ( );
        }
        else
        {
          return V + constant;
        }
      }

      if ( first.value > 0 )
      {
        final double  maxDecay = Math.min (
          para.allDecays [ ( int ) first.id ],
          tauSafe );
        
        firstV = -first.value * maxDecay;
      }
      else
      {
        firstV = -first.value * para.allDecays [ ( int ) first.id ];
      }

      V = firstV;
      
      while ( iter.hasNext ( ) )
      {
        second = iter.next ( );
        
        secondV = 0.0;

        if( second.time == t)
        {
          if ( second.value > 0 )
          {
            final double  maxDecay = Math.min (
              para.allDecays [ ( int ) second.id ],
              tauSafe );
            
            constant += -second.value * maxDecay;
          }
          else
          {
            constant
              += -second.value
              * para.allDecays [ ( int ) second.id ];
          }

          if ( iter.hasNext ( ) )
          {
            second = iter.next ( );
          }
          else
          {
            break;
          }
        }

        if ( second.value > 0 )
        {
          final double  maxDecay = Math.min (
            para.allDecays [ ( int ) second.id ],
            tauSafe );
          
          secondV = -second.value * maxDecay;
        }
        else
        {
          secondV = -second.value * para.allDecays [ ( int ) second.id ];
        }

        V = Math.exp ( -( ( para.allDecays [ ( int ) first.id ]
          - para.allDecays [ ( int ) second.id ] ) * t
          - ( para.allDecays [ ( int ) first.id ] * first.time
          - para.allDecays [ ( int ) second.id ] * second.time ) ) ) * V
          + secondV;
        
        first = second;
      }

      V = Math.exp ( -( para.allDecays [ ( int ) first.id ] * t
        - para.allDecays [ ( int ) first.id ] * first.time ) ) * V;
      
      /*
    double dV=0;
    iter = state.iterator();
    while(iter.hasNext())
    {
      State tmpState = iter.next();
      if(tmpState.value>0)
      {
        double maxDecay = Math.min(
          para.allDecays[(int)tmpState.id],tauSafe);
        if(t-tmpState.time!=0)
        {
          // dV -= Math.exp(-(t-tmpState.time)*maxDecay)
          //   * tmpState.value*maxDecay;
          
          dV -= Math.exp(-(t-tmpState.time)*para.allDecays[
            (int)tmpState.id])*tmpState.value*maxDecay;
        }
        else
        {
          dV -= tmpState.value*maxDecay;
        }
      }
      else
      {
        if(t-tmpState.time!=0)
        {
          dV -= Math.exp(-(t-tmpState.time)*para.allDecays[
            (int)tmpState.id])*tmpState.value*para.allDecays[
            (int)tmpState.id];
        }
        else
        {
          dV -= tmpState.value*para.allDecays[(int)tmpState.id];
        }
      }
    }
       */
      return V + constant;
    }

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    }
