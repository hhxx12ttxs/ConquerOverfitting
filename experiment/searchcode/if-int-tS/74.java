    package cnslab.cnsnetwork;
    
    import java.io.*;
    
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    
    import cnslab.cnsmath.*;
    import edu.jhu.mb.ernst.model.Synapse;
    import edu.jhu.mb.ernst.util.slot.Slot;
    
    /***********************************************************************
    * Implemented simple integrate and fire neuron. Only two channels are
    * allowed, one excitatory and one inhibitory, and the decay of
    * inhibitory channel should be longer than the excitatory one.
    * Look up table is used for quick spike test.
    * 
    * @version
    *   $Date: 2012-08-04 20:43:22 +0200 (Sat, 04 Aug 2012) $
    *   $Rev: 104 $
    *   $Author: croft $
    * @author
    *   Yi Dong
    * @author
    *   David Wallace Croft
    ***********************************************************************/
    public class  SIFNeuron
      implements Neuron, Serializable
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    {

    private static final long  serialVersionUID = 0L;
    
    public static double  maxWeight = 0;

    /** For table size */
    public static int TABLESIZE_o = 40;
    
    /** For table size */
    public static int TABLESIZE_c = 500;
    
    /** For table min boundary  */
    public static double  memMin = -0.10; // min -100 mV membrane potential
    
    /** For table  max boundary */
    public static double  memMax = -0.04; // max -40 mV membrane potential
    
    /** For table min boundary */
    public static double [ ]
      currMin = new double [ ] { 0, -maxWeight }; // minimum Current
    
    /** For table max boundary */
    public static double [ ]
      currMax = new double [ ] { maxWeight, 0 };
    
    /** For table step size */
    public static double  memStep = ( memMax - memMin ) / TABLESIZE_o;
    
    /** For table step size */
    public static double currStep = ( maxWeight ) / TABLESIZE_c;

    // absoulte refractory period    
    // public static final double para.ABSREF = 0.000;

    // voltage from threshold for which the neuron is considered to fire
    // public static final double para.EPSV = 1E-14;
    
    // used for absoulte refractory period    
    // public double timeOffset=0.0;

    /** long imposes a constraint on the maximum number of hosts to be 64 */
    public long  tHost;
    
    // private static variables
    
    private static final Class<SIFNeuron>
      CLASS = SIFNeuron.class;
  
    private static final Logger
      LOGGER = LoggerFactory.getLogger ( CLASS );
    
    // private final variables

    private final SIFNeuronPara  para;
    
    // private non-final variables

    private double [ ]  curr;
    
    /** true then this neuron will be recorded */
    private boolean  record;

    /** the neuron membrane potential */
    private double  memV;

    /** time of the neuron's next fire */
    private double  timeOfNextFire = -1;
    
    /** time of the neuron's last update */
    private double  timeOfLastUpdate;
    
    ////////////////////////////////////////////////////////////////////////
    // static methods
    ////////////////////////////////////////////////////////////////////////
    
/*
    public static double maxWeight()
    ////////////////////////////////////////////////////////////////////////
    {
      return para.CAP*(para.THRESHOLD-para.VRESET)/para.MINRISETIME;
    }
*/
    
    ////////////////////////////////////////////////////////////////////////
    // constructor methods
    ////////////////////////////////////////////////////////////////////////

    public  SIFNeuron ( final SIFNeuronPara  para )
    ////////////////////////////////////////////////////////////////////////
    {
      if ( maxWeight
        < para.CAP * ( para.THRESHOLD - para.VRESET ) / para.MINRISETIME )
      {
        maxWeight
          = para.CAP * ( para.THRESHOLD - para.VRESET ) / para.MINRISETIME;
        
        currStep = maxWeight / TABLESIZE_c;
      }

      this.para = para;
      
      this.curr = new double [ 2 ];
      
      curr[0]=0.0;
      
      curr[1]=0.0;

      if ( !para.GlobalInit )
      {
        para.FIRETABLE = new boolean
          [ TABLESIZE_o + 1 ]
          [ TABLESIZE_c + 1 ]
          [ TABLESIZE_c + 1 ];

        for ( int  i = 0; i < TABLESIZE_o + 1; i++ )
        {
          for ( int  k = 0; k < TABLESIZE_c + 1; k++ )
          {
            for ( int  m = 0; m < TABLESIZE_c + 1; m++ )
            {
              this.memV = i * memStep + memMin;
              
              this.curr = new double [ ] {
                k * currStep + currMin [ 0 ],
                m * currStep + currMin [ 1 ] };
              
              if ( this.memV >= para.THRESHOLD )
              {
                para.FIRETABLE [ i ] [ k ] [ m ] = true;
              }
              else
              {
                if ( doesFire ( ) )
                {
                  para.FIRETABLE [ i ] [ k ] [ m ] = true;
                }
                else
                {
                  para.FIRETABLE [ i ] [ k ] [ m ] = false;
                }
              }
            }
          }
        }
        
        para.GlobalInit = true;
      }
    }

    ////////////////////////////////////////////////////////////////////////
    // interface Neuron accessor methods
    ////////////////////////////////////////////////////////////////////////
    
/*
    public Axon getAxon()
    ////////////////////////////////////////////////////////////////////////
    {
      return axon;
    }
*/

    @Override
    public double [ ]  getCurr ( double  currTime )
    ////////////////////////////////////////////////////////////////////////
    {
      return new double [ ] {
        curr [ 0 ] * Math.exp (
          -para.DECAY_E * ( currTime - timeOfLastUpdate ) ),
        curr [ 1 ] * Math.exp (
          -para.DECAY_I * ( currTime - timeOfLastUpdate ) ) };
    }

    @Override
    public double  getMemV ( final double  currTime )
    ////////////////////////////////////////////////////////////////////////
    {
      // if input comes inside the refractory period, memV not changed
      
      if ( currTime < timeOfLastUpdate )
      {
        return this.memV; 
      }
      
      return voltageFunOfTime ( currTime - timeOfLastUpdate );
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

    /***********************************************************************
    * get the value of timeOfNextFire
    * 
    * @return
    *   the value of timeOfNextFire
    ***********************************************************************/
    @Override
    public synchronized double  getTimeOfNextFire ( )
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
    public boolean realFire()
    ////////////////////////////////////////////////////////////////////////
    {
      return true;
    }
    
    ////////////////////////////////////////////////////////////////////////
    // additional accessor methods
    ////////////////////////////////////////////////////////////////////////

    /***********************************************************************
    * It looks this was originally a method in the Neuron interface which
    * has since been removed.  Probably can be deleted.    
    ***********************************************************************/
    public double getSensoryWeight ( )
    ////////////////////////////////////////////////////////////////////////
    {
      throw new RuntimeException("LIF neurons can't run this function");
    }
    
    /***********************************************************************
    * get the value of timeOfLastUpdate
    * 
    * @return
    *   the value of timeOfLastUpdate
    ***********************************************************************/
    public double  getTimeOfLastUpdate ( )
    ////////////////////////////////////////////////////////////////////////
    {
      return this.timeOfLastUpdate;
    }
    
    ////////////////////////////////////////////////////////////////////////
    // interface Neuron mutator methods
    ////////////////////////////////////////////////////////////////////////
    
    /***********************************************************************
    * set a new value to memV
    * 
    * @param memV
    *   the new value to be used
    ***********************************************************************/
    public void  setMemV ( final double  memV )
    ////////////////////////////////////////////////////////////////////////
    {
      this.memV = memV;
    }

    /***********************************************************************
    * set the neuron record or not.
    ***********************************************************************/
    @Override
    public void setRecord ( final boolean  record )
    ////////////////////////////////////////////////////////////////////////
    {
      this.record = record;
    }

    /***********************************************************************
    * set the target host id //0 means none , 1 means yes 
    ***********************************************************************/
    @Override
    public void setTHost(long id)
    ////////////////////////////////////////////////////////////////////////
    {
      this.tHost=id;
    }

    /***********************************************************************
    * set a new value to timeOfNextFire
    * 
    * @param timeOfNextFire
    *   the new value to be used
    ***********************************************************************/
    @Override
    public synchronized void  setTimeOfNextFire (
      final double  timeOfNextFire )
    ////////////////////////////////////////////////////////////////////////
    {
      this.timeOfNextFire = timeOfNextFire;
    }

    ////////////////////////////////////////////////////////////////////////
    // additional mutator methods
    ////////////////////////////////////////////////////////////////////////
    
    /***********************************************************************
    * set a new value to timeOfLastUpdate
    * 
    * @param timeOfLastUpdate
    *   the new value to be used
    ***********************************************************************/
    public void  setTimeOfLastUpdate ( final double  timeOfLastUpdate )
    ////////////////////////////////////////////////////////////////////////
    {
      this.timeOfLastUpdate = timeOfLastUpdate;
    }

    ////////////////////////////////////////////////////////////////////////
    // interface Neuron lifecycle methods
    ////////////////////////////////////////////////////////////////////////
    
    @Override
    public void  init (
      int expid,
      int trialid,
      Seed idum,
      Network net,
      int id)
    ////////////////////////////////////////////////////////////////////////
    {
      this.timeOfLastUpdate=0.0;
      
      this.timeOfNextFire=-1;
      
      this.memV = para.ini_mem+para.ini_memVar*Cnsran.ran2(idum);
      
      this.curr[0]=0.0;
      
      this.curr[1]=0.0;
      
      double fireTime;
      
      // net.p.println("My name is"+net.getClass().getName());
      
      if ( doesFire ( ) )
      {
        final Slot<FireEvent>  fireEventSlot = net.getFireEventSlot ( );
        
        if ( net.getClass ( ).getName ( ).equals (
          "cnslab.cnsnetwork.ANetwork" ) )
        {
          fireEventSlot.offer (
            new AFireEvent (
              id,
              fireTime = timeOfFire ( 0 ),
              net.info.idIndex,
              ( ( ANetwork ) net ).aData.getId ( ( ANetwork ) net ) ) );
        }
        else if ( net.getClass ( ).getName ( ).equals (
          "cnslab.cnsnetwork.Network" ) )
        {
          fireEventSlot.offer (
            new FireEvent (
              id,
              fireTime = timeOfFire ( 0 ) ) );
        }
        else
        {
          throw new RuntimeException (
            "Other Network Class doesn't exist" );
        }
        
        timeOfNextFire = fireTime;
      }
    }

    @Override
    public double  updateFire ( )
    ////////////////////////////////////////////////////////////////////////
    {
      // LOGGER.trace ( "updateFire() entered" );
      
      memV = para.VRESET;
      
      // LOGGER.debug ( "time of next fire:  {}", timeOfNextFire );
      
      // LOGGER.debug ( "time of last update:  {}", timeOfLastUpdate );
      
      // LOGGER.debug ( "excitatory current:  {}", curr [ 0 ] );
      
      curr [ 0 ]
        = curr [ 0 ]
        * Math.exp (
          -para.DECAY_E * ( timeOfNextFire - timeOfLastUpdate ) )
        - para.REFCURR;
      
      // LOGGER.debug ( "excitatory current:  {}", curr [ 0 ] );
      
      // LOGGER.debug ( "inhibitory current:  {}", curr [ 1 ] );
      
      curr [ 1 ] *= Math.exp (
        -para.DECAY_I * ( timeOfNextFire - timeOfLastUpdate ) );
      
      // LOGGER.debug ( "inhibitory current:  {}", curr [ 1 ] );
      
      timeOfLastUpdate = timeOfNextFire + para.ABSREF;

      curr [ 0 ] *= Math.exp ( -para.DECAY_E * para.ABSREF );
      
      // LOGGER.debug ( "excitatory current:  {}", curr [ 0 ] );
      
      curr [ 1 ] *= Math.exp ( -para.DECAY_I * para.ABSREF );

      // LOGGER.debug ( "inhibitory current:  {}", curr [ 1 ] );
      
      //curr[a]=para.IRATIO[a]*curr[a]*Math.exp(
      //  -para.DECAY[a]*(timeOfNextFire-timeOfLastUpdate+para.ABSREF))
      //  +para.IADD[a]*Math.exp(-para.DECAY[a]*para.ABSREF);

      // for excitatory neuron
      //    curr[0] =0;

      final int
        memI = ( int ) ( Math.floor ( ( memV - memMin ) / memStep ) );
      
      final int [ ]  curI = new int [ curr.length ];

      for ( int  i = 0; i < curr.length; i++ )
      {
        curI [ i ] = ( int )
          ( Math.floor ( ( curr [ i ] - currMin [ i ] ) / currStep ) );
      }

      int [ ]  memList = null;
      
      final int [ ] [ ]  currList = new int [ curr.length ] [ ];
      
      boolean  fullcheck = false;

      if ( memI < 0)
      {  
        fullcheck = true;
        //      memList = new int [] {0} ;
      }
      else if ( memI >= TABLESIZE_o )
      {
        fullcheck = true;
        //      memList = new int [] {TABLESIZE_o} ;
      }
      else
      {
        memList = new int [ ] { memI, memI + 1 };
      }

      for ( int  i = 0; i < curr.length; i++ )
      {
        if ( curI [ i ] < 0 )
        {  
          fullcheck = true;
          //        currList[i] = new int [] {0} ;
        }
        else if ( curI [ i ] == TABLESIZE_c )
        {
          if ( i == 0 )
          {
            fullcheck = true; //excitatory currents needs to be checked
          }
          else
          {
            // inhibitory currents is fine
            
            currList [ i ] = new int [ ] { TABLESIZE_c };
          }
        }
        else if ( curI [ i ] > TABLESIZE_c )
        {
          fullcheck = true;
        }
        else
        {
          currList [ i ] = new int [ ] { curI [ i ], curI [ i ] + 1 };
        }
      }
      
      if ( fullcheck )
      {
        if ( doesFire ( ) )
        {
          return timeOfFire ( timeOfNextFire );
        }
        
        return -1.0;
      }
      
      final boolean  fireS = para.FIRETABLE
        [ memList [ 0 ] ]
        [ currList [ 0 ] [ 0 ] ]
        [ currList [ 1 ] [ 0 ] ];

      for ( int  ii = 0; ii < memList.length; ii++ )
      {
        for ( int  kk = 0; kk < currList [ 0 ].length; kk++ )
        {
          for ( int  mm = 0; mm < currList [ 1 ].length; mm++ )
          {
            if ( !( ii == 0
                 && kk == 0
                 && mm == 0 ) )
            {
              if ( ( para.FIRETABLE
                [ memList [ ii ] ]
                [ currList [ 0 ] [ kk ] ]
                [ currList [ 1 ] [ mm ] ] ^ fireS ) )
              {
                // ambiguous firing case, do a full check
                
                if ( doesFire ( ) )
                {
                  return timeOfFire ( timeOfNextFire );
                }
                
                return -1.0;
              }
            }
          }
        }
      }
      
      // pass through this, it is non ambiguous now.
      
      if ( fireS ) //do fire
      {
        return timeOfFire(timeOfNextFire);
      }
      
      // not fire
      
      return -1.0;
    }              

    @Override
    public double  updateInput (
      final double   time,
      final Synapse  inputSynapse )                             
    ////////////////////////////////////////////////////////////////////////
    {
      // if input comes inside the refractory period only update current to
      // the last update time.
      
      if ( time < timeOfLastUpdate )
      {
        switch ( inputSynapse.getType ( ) )
        {
          case 0:
            
            curr [ 0 ] = curr [ 0 ]
              + Math.exp ( -para.DECAY_E * ( timeOfLastUpdate - time ) )
              * inputSynapse.getWeight ( );
            
            // if(timeOfLastUpdate - time <=0 )
            // throw new RuntimeException(
            // "reversed time at "+time+"lastUPdate "+timeOfLastUpdate
            // +" input:"+input.toString());
            
            // if( curr[0]>1.0 )
            // throw new RuntimeException(
            // "two high activity time at "+time+"lastUPdate "
            // +timeOfLastUpdate+" input:"+input.toString());
            
            break;
            
          case 1:
            
            curr [ 1 ] = curr [ 1 ]
              + Math.exp ( -para.DECAY_I * ( timeOfLastUpdate - time ) )
              * inputSynapse.getWeight ( );
            
            break;
            
          default:
            
            System.out.println("no other types available");
            
            break;
        }
      }
      else
      {
        memV = voltageFunOfTime(time - timeOfLastUpdate);
        
        if(memV-para.THRESHOLD>0)
        {
          System.out.println(
            "current time:"+time+" last update: "+timeOfLastUpdate
            +" time of next firing: "+timeOfNextFire);
          
          System.out.println("time diff:"+(time - timeOfLastUpdate));
          
          System.out.println("curr0:"+curr[0]+" curr1:"+curr[1]);
          
          System.out.println("Membrane voltage: "+memV);
          
          throw new RuntimeException (
            "current time:"+time
            +" last update: "+timeOfLastUpdate
            +" time of next firing: "+timeOfNextFire + "\n"
            +"time diff:"+(time - timeOfLastUpdate)+"\n"
            +"curr0:"+curr[0]+" curr1:"+curr[1]+"\n"
            +"Membrane voltage: "+memV+"\n"
            +"already fires in updateinput" );
        }
        
        switch ( inputSynapse.getType ( ) )
        {
          case 0:
            
            curr[0]=curr[0]
              *Math.exp(-para.DECAY_E*(time - timeOfLastUpdate))
              +inputSynapse.getWeight ( );
            
            curr[1]=curr[1]
              *Math.exp(-para.DECAY_I*(time - timeOfLastUpdate));
            
            break;
            
          case 1:
            
            curr[0] = curr[0]
              * Math.exp(-para.DECAY_E*(time - timeOfLastUpdate));
            
            curr[1] = curr[1]
              * Math.exp(-para.DECAY_I*(time - timeOfLastUpdate))
              + inputSynapse.getWeight ( );
            
            break;
            
          default:
            
            System.out.println("no other types available");
            
            break;
        }
        
        timeOfLastUpdate = time;
      }

      int memI = (int)(Math.floor((memV-memMin)/memStep));
      
      int [] curI = new int [curr.length];

      for(int i=0; i < curr.length; i++)
      {
        curI[i] = (int)(Math.floor((curr[i]-currMin[i])/currStep));
      }

      int [] memList=null;
      
      int [][] currList = new int [curr.length][];
      
      boolean fullcheck = false;

      if(memI<0)
      {  
        fullcheck = true;
        
        // memList = new int [] {0} ;
      }
      else if( memI >= TABLESIZE_o )
      {
        fullcheck = true;
        
        // memList = new int [] {TABLESIZE_o} ;
      }
      else
      {
        memList = new int [] {memI, memI+1} ;
      }

      for(int i=0; i < curr.length; i++)
      {
        if(curI[i]<0)
        {  
          fullcheck = true;
          
          // currList[i] = new int [] {0} ;
        }
        else if( curI[i] == TABLESIZE_c )
        {
          if(i==0)
          {
            fullcheck = true; //excitatory currents needs to be checked
          }
          else
          {
            // inhibitory currents is fine
            currList[i] = new int [] {TABLESIZE_c} ;
          }
        }
        else if( curI[i] > TABLESIZE_c )
        {
          fullcheck = true;
        }
        else
        {
          currList[i] = new int [] {curI[i],curI[i]+1} ;
        }
      }
      
      if(fullcheck)
      {
        if(doesFire())
        {
          return timeOfFire(time);
        }
        return -1.0;
      }
      
      final boolean  fireS = para.FIRETABLE
        [ memList [ 0 ] ]
        [ currList [ 0 ] [ 0 ] ]
        [ currList [ 1 ] [ 0 ] ];

      for(int ii=0; ii < memList.length; ii ++)
      {
        for(int kk=0; kk< currList[0].length; kk++)
        {
          for(int mm=0; mm< currList[1].length; mm++)
          {
            if(!(ii==0 && kk==0 && mm==0))
            {
              if ( ( para.FIRETABLE
                [ memList [ ii ] ]
                [ currList [ 0 ] [ kk ] ]
                [ currList [ 1 ] [ mm ] ] ^ fireS ) )                
              {
                // ambiguous firing case, do a full check
                
                if(doesFire())
                {
                  return timeOfFire(time);
                }
                
                return -1.0;
              }
            }
          }
        }
      }
      
      //pass through this, it is non ambiguous now.
      
      if(fireS) //do fire
      {
        return timeOfFire(time);
      }
      
      // not fire
      
      return -1.0;
    }

    ////////////////////////////////////////////////////////////////////////
    // additional methods
    ////////////////////////////////////////////////////////////////////////
    
    /***********************************************************************
    * @return
    *   true if the neuron will fire in future
    ***********************************************************************/
    public boolean  doesFire ( )
    ////////////////////////////////////////////////////////////////////////
    {
      double ts = 0.0;

      double dt, tsmax;
      
      int  i = 0, imax;

      // maximum time to look for firing
      
      tsmax = 3.0 * ( para.DECAY_E > para.CAP / para.GL
        ? para.DECAY_E : para.CAP / para.GL );
      
      // tsmax=2*(para.CAP/para.GL);//maximum time to look for firing
      
      // max number of steps to look for firing
      
      imax = 100000;

// TODO:  Should this be >= instead of >?  memV >= para.THRESHOLD
      
      if ( memV - para.THRESHOLD > 0 )
      {
        System.out.println (
          "last update "
          + timeOfLastUpdate
          + "time of next firing "
          + timeOfNextFire );
        
        System.out.println ( "Membrane voltage: " + memV );
        
        throw new RuntimeException ( "already fires in doesfire" );
      }
      
      // if(derivvoltageFunOfTime(ts)<0)return false;
      
      while ( ts < tsmax )
      {
        if ( deriVoltageFunOfTime ( ts ) <= 0 )
        {
          return false;
        }
        
        dt = ( para.THRESHOLD - voltageFunOfTime ( ts ) )
          / deriVoltageFunOfTime ( ts );
        
        if ( voltageFunOfTime ( ts + 2 * dt ) > para.THRESHOLD )
        {
          // guess=ts+2*dt;
          
          return true;
        }
        
        ts += dt;
        
        if ( voltageFunOfTime ( ts ) > para.THRESHOLD )
        {
          // guess = ts;
          
          return true;
        }
        
        i++;
        
        if ( i > imax )
        {
          throw new RuntimeException (
            "max steps reached in doesfire, memV="
            + memV + " curr=" + curr [ 0 ] + " " + curr [ 1 ] );
        }
      }
      
      return false;
    }

    /***********************************************************************
    * @return
    *   the time when the neuron fires
    ***********************************************************************/
    public double  timeOfFire ( final double  currTime )
    ////////////////////////////////////////////////////////////////////////
    {
      double  ts = 0;
      
      // double  dt;
      
      int
        i = 0,
        imax = 100000;

      while ( i < imax )
      {
        ts += ( para.THRESHOLD - voltageFunOfTime ( ts ) )
          / deriVoltageFunOfTime ( ts );
        
        if ( Math.abs ( 1 - ( voltageFunOfTime ( ts ) / para.THRESHOLD ) )
          < para.EPSV )
        {
          return ts + ( ( timeOfLastUpdate - currTime ) < 0 ? 0.0
            : ( timeOfLastUpdate - currTime ) );
        }
        
        i++;
      }

      throw new RuntimeException (
        "Too many iteratations were encounted in timeOfFire currtime:"
        + currTime
        + " timeOfLastUpdate:"
        + timeOfLastUpdate
        + " curr[0]:"
        + curr [ 0 ]
        + " curr[1]:"
        + curr [ 1 ]
        + " memV:"
        + memV
        + " timeOfnextFire:"
        + timeOfNextFire );
    }

    ////////////////////////////////////////////////////////////////////////
    // overridden Object methods
    ////////////////////////////////////////////////////////////////////////
    
    @Override
    public String  toString ( )
    ////////////////////////////////////////////////////////////////////////
    {
      String  tmp = "";
      
      tmp = tmp + "MemVol:" + memV + "\n";
      
      tmp = tmp + "Current:" + "\n";
      
      for ( int  i = 0; i < curr.length; i++)
      {
        tmp = tmp + "       curr" + i + " " + curr [ 0 ] + "\n";
      }
      
      // tmp = tmp + axon;
      
      return tmp;
    }

    ////////////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////////////
    
    /***********************************************************************
    * @param _diff
    *   time difference between current time and future time
    * @return
    *   derivative of membrane voltage with respect to time
    ***********************************************************************/
    private double  deriVoltageFunOfTime ( final double  _diff )
    ////////////////////////////////////////////////////////////////////////
    {
      return (
        ( curr [ 0 ] * para.DECAY_E )
          / ( Math.exp ( para.DECAY_E * _diff )
          * ( para.CAP * para.DECAY_E - para.GL ) )
        + ( curr [ 1 ] * para.DECAY_I )
          / ( Math.exp ( para.DECAY_I * _diff )
          * ( para.CAP * para.DECAY_I - para.GL ) )
        - ( para.GL
          * ( curr [ 0 ] / ( para.CAP * para.DECAY_E - para.GL )
          + curr [ 1 ] / ( para.CAP * para.DECAY_I - para.GL )
          + memV
          - para.VREST ) )
          / ( para.CAP * Math.exp ( ( para.GL * _diff ) / para.CAP ) ) );
    }

    /***********************************************************************
    * @param _diff
    *   time difference between current time and future time
    * @return
    *   membrane voltage
    ***********************************************************************/
    private double  voltageFunOfTime ( double  _diff )
    ////////////////////////////////////////////////////////////////////////
    {                       
      return (
        -curr [ 0 ]
          / ( Math.exp ( para.DECAY_E * _diff )
            * ( para.CAP * para.DECAY_E - para.GL ) )
        - curr [ 1 ]
          / ( Math.exp ( para.DECAY_I * _diff )
            * ( para.CAP * para.DECAY_I - para.GL ) )
        + ( curr [ 0 ] / ( para.CAP * para.DECAY_E - para.GL )
          + curr [ 1 ] / ( para.CAP * para.DECAY_I - para.GL )
          + memV - para.VREST )
          / Math.exp ( ( para.GL * _diff ) / para.CAP )
        + para.VREST );
    }    

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    }
