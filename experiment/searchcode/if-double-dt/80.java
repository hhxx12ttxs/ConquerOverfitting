package jaseimov.client.controlcarB.adaptative;

import jaseimov.lib.devices.DeviceException;
import info.monitorenter.gui.chart.ITrace2D;
import jaseimov.lib.devices.Encoder;
import jaseimov.lib.devices.MotorControl;
import jaseimov.lib.devices.ServoControl;
import jaseimov.lib.devices.Spatial;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;

public class Sequencer
{
  //timer vars
  Timer timer = new Timer();
  private MyTask tarea = new MyTask(); // timer generic task, iside we put the real task
  private boolean frozen; // indicates if the sequencer timer is paused or not
  public boolean scheduled = false; //indicates if the timer has a task already
  // global vars
  private double t0 = System.currentTimeMillis();//initial time
  private double t = 0.0; //current time
  private double tact = 0.0; // currect relative time (relative to t0)
  private double vact = 0.0; //actual speed in %
  private double vord = 0.0; //ordered speed in %
  private double vact_real = 0.0; //actual speed (filtred speed) in m/s
  private double vord_real = 0.0; //ordered speed in m/s
  private double maxspeed = 100.0; //maximum speed for the car in m/s
  private double dt = 0.0; //sampling time in seconds.
  private int is_counter = 0; //intelistop counter
  private int rec_counter = 0;
  //Arrays for data export
  ArrayList column = new ArrayList();
  ArrayList col_names = new ArrayList();
  //enables
  boolean AutomaticControl = false;
  boolean enableFiltering = false;
  boolean enableAccelerationSpeed = true;
  boolean SequencerAvailable = false;
  boolean intelistop = false;
  double intelistoptime = 2.0; // 2000 ms, 2 seconds by default
  boolean RecEnable = false;
  boolean ScriptEnable = false;
  // Emergency Stop
  boolean EMERGENCYSTOP = false;
  boolean AutomaticControlState = false;
  int encoder_zeros = 0;
  double Speed_sign = 0;
  double breakval=0.0;
  // tracers
  public ITrace2D[] trace;
  // Modules
  public Curves crv;
  private NextStep nxst;
  // devices
  MotorControl motorcontrol;
  ServoControl servo;
  Spatial spatial;
  Encoder encoder;
  // encoder info
  double encoder_velocity = 0.0;
  double encoder_velocity_filtered=0.0;
  double encoder_tics = 0.0;
  double encoder_tics2rad = 0.0;
  double encoder_tics2m = 0.0;
  // spatial info
  double[][] spatial_info =
  {
    {
      0.0, 0.0, 0.0
    },
    {
      0.0, 0.0, 0.0
    },
    {
      0.0, 0.0, 0.0
    }
  };
  // Script model
  DefaultListModel scriptmodel;
  int scriptindex = 0;
  double script_time_offset=0.0;

  // constructor
  public Sequencer(MotorControl val1,
          ServoControl val2,
          Spatial val3,
          Encoder val4,
          Curves val5,
          NextStep val6,
          ITrace2D[] val7,
          DefaultListModel val8,
          double period)
  {
    //asign devices
    motorcontrol = val1;
    servo = val2;
    spatial = val3;
    encoder = val4;
    crv = val5;
    nxst = val6;
    trace = val7;
    scriptmodel = val8;
    dt = period;

    col_names.add("time");
    col_names.add("ordered_speed");
    col_names.add("Adjusted_order");
    col_names.add("Encoder_tics");
    col_names.add("encoder_velocity");
    col_names.add("encoder_velocity_filtered");
    col_names.add("Accelaration x");
    col_names.add("Acceleration y");
    col_names.add("Acceleration z");
    col_names.add("Magnetic field x");
    col_names.add("Magnetic field y");
    col_names.add("Magnetic field z");
    col_names.add("Angular momentum x");
    col_names.add("Angular momentum y");
    col_names.add("Angular momentum z");
    col_names.add("Servo_position");
    col_names.add("Motor_percent");

    double[] info = getEncoderInfo();
    encoder_tics2m = info[0]; // metros por cada tic
    encoder_tics2rad = info[1]; // radianes por tic

    System.out.println("tics2m: " + encoder_tics2m + ", tics2rad:" + encoder_tics2rad);

    // set an arbitrary maximum real speed
    maxspeed = crv.getMaxVal(crv.getacfw());

  } // constructor

  public void Execute_Step() throws DeviceException
  {
    t = System.currentTimeMillis();//set current time to compare it with t0
    tact = (t - t0) / 1000.0; // in seconds   

    //////////////////////////////////////////////////////////////////////
    //check script
    if (ScriptEnable == true)
    {
      followScript(tact);
    }

    //System.out.println("Vact:"+vact);
    //////////////////////////////////////////////////////////////////////
    /*main filtering process*/
    //vord is a value from -100 to 100, it is modified later inside setMotorControlSpeed
    if (enableFiltering == true)
    {
      vord_real = vord * maxspeed / 100.0;
      vact_real = nxst.next_step(vact_real, vord_real); // in m/s
      vact = vact_real / maxspeed * 100.0; //in %
    }
    else
    { //if not filtering, send the raw signal
      vact = vord;
      vact_real = vact * maxspeed / 100.0;
    }

    
    
    ///////////////////////////////////////////////////////////////
    //send order to motor to revise emergency state
    setMotorDesiredOrder(vord);

    //////////////////////////////////////////////////////////
    // get encoder speed in m/s
    encoder_velocity = getEncoderSpeed();
    encoder_velocity_filtered=getEncoderSpeedFiltered();   

    /////////////////////////////////////////////////////////
    // get spatial_info
    spatial_info = getSpatialInfo();

    ////////////////////////////////////////////////////////////
    // InteliStop */
    if (intelistop == true)
    {
      if (vact == vord)
      {
        is_counter++;
      }
      //if its 'intelistoptime' miliseconds in the same position, then stop the clock
      if (is_counter > intelistoptime / dt)
      {
        Pause();
        is_counter = 0;
      }
    }

    //////////////////////////////////////////////////////////////////////
    // add points to the tracers
    trace[0].addPoint(tact, vord_real);
    trace[1].addPoint(tact, vact_real);
    trace[2].addPoint(tact, encoder_velocity);
    trace[3].addPoint(tact, encoder_velocity_filtered);

    ////////////////////////////////////////////////////////////////
    //rec
    ////////////////////////////////////////////////////////////////
    if (RecEnable == true)
    {
      //save data in the array
      ArrayList row = new ArrayList();
      row.add(0, tact);
      row.add(1, vord_real);
      row.add(2, vact_real);
      row.add(3, encoder_tics);
      row.add(4, encoder_velocity);
      row.add(5, encoder_velocity_filtered);

      //acceleration x, y, z
      row.add(6, spatial_info[0][0]);
      row.add(7, spatial_info[0][1]);
      row.add(8, spatial_info[0][2]);
      // magnetic field x,y,z
      row.add(9, spatial_info[1][0]);
      row.add(10, spatial_info[1][1]);
      row.add(11, spatial_info[1][2]);
      // angular momentum x,y,z
      row.add(12, spatial_info[2][0]);
      row.add(13, spatial_info[2][1]);
      row.add(14, spatial_info[2][2]);
      // servo pos
      row.add(15,getServoPosition());
      //Motorcontrol percent
      row.add(16,getMotorPercent());

      column.add(rec_counter, row);
      rec_counter++;
      //System.out.println(row);
    }

    //send speed to car
    SendOrderToMotorControl(vact_real);

  }// end of main process

  public void setScriptEnable(boolean val)
  {
    Start();
    ScriptEnable = val;
    scriptindex=0;
    script_time_offset=tact-dt; //reset the time to ba able to use relative time scripts
    
    System.out.println("[Sequencer]:Filter enabled:" + val);
  }

  private void followScript(double t)
  {   
   String line=scriptmodel.get(scriptindex).toString();
   String vals[]=line.split(";");
   /*
      * vals[0]=time
      * vals[1]=order
      * vals[2]=value
      */
   double time=Double.parseDouble(vals[0])+script_time_offset;
   String order=vals[1];
   double val=Double.parseDouble(vals[2]);
   double t1=time-(dt/2);
   double t2=time+(dt/2);

  
   System.out.println("[Script]: scriptindex:"+scriptindex
            +", line:"+line
            + ", t:" + t
            + ", time:"+time
            + ", [" + t1
            + ", "+t2 +"]"
            + ", offset:"+script_time_offset);  
    

   if (t >= t1 && t < t2){ // if t is in the recovering of time:
     
      scriptindex++;

      if (scriptindex>=scriptmodel.size()){
        scriptindex=0;
      }

    
    
      System.out.println("[Script]:"
            + line
            + ", found at: "+ scriptindex
            + ", t:"+t
            + ", time:"+time
            + ", [" + t1
            + ", "+t2 +"]"
            + ", order:"+order
            + ", val:"+val);


      if (order.trim().equals("v")){
        setVord(val);
        System.out.println("[script]Vact:"+vact);
      }
      if(order.trim().equals("p")){
        setServoPosition(val);
      }
      if(order.trim().equals("stop")){
        Pause();
      }
      if(order.trim().equals("start")){
        Start();
      }
          
   }

  }

  // internal class that represents a task
  class MyTask extends TimerTask
  {
    public void run()
    {

      if (frozen == false)
      { //code to do at intervals
        try
        {
          //code to do at intervals
          Execute_Step();
        }
        catch (DeviceException ex)
        {
          Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

    }// end run()
  }

  public void Start()
  { //schedules and starts the timer, so the sequencer
    frozen = false;
    // we assign a task to the timer
    if (SequencerAvailable == true)
    {
      if (scheduled == false)
      {
        double period = dt * 1000.0; // the period must be in ms, but dt is in seconds...
        timer.schedule(tarea, 0, Math.round(period)); //we need to set the clock frequency to dt (period)
        nxst.setdt(dt);
        maxspeed = crv.getMaxVal(crv.getacfw());
        System.out.println("[Sequencer]:maxspeed:" + maxspeed + " m/s");
        System.out.println("[Sequencer]:Clock Started, frequency:" + Math.round(period) + " ms");
        scheduled = true;
      }
    }
  }// end Start

  public void Pause()
  { //pauses the timer
    System.out.println("[Sequencer]:Sequencer Clock Paused");
    frozen = true;
  }// end Stop

  public void TimerKill()
  {
    timer.purge();
    timer.cancel();
  }

  public void setAvailable(boolean val)
  {
    SequencerAvailable = val;
    System.out.println("[Sequencer]:Sequencer available:" + val);
  }

  public boolean isAvailable()
  {
    return SequencerAvailable;
  }

  public double getSequencertime()
  {
    return System.currentTimeMillis() - t0;
  }

  public void reset()
  { //delete all data, and reset time
    clearTraces();
    t0 = System.currentTimeMillis();
    column.clear();
  }

  public void clearTraces()
  {
    for (int i = 0; i < trace.length; i++)
    {
      trace[i].removeAllPoints();
    }
  }

  public void setMotorDesiredOrder(double v){
    try
    {
      motorcontrol.setDesiredOrder(v);
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public boolean getAutomaticControl(){
    try
    {
      return motorcontrol.isAutoControlled();
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public void setAutomaticControl(boolean val)
  {
    AutomaticControl = val;
    try
    {
      if (val==true){
        if (motorcontrol.isAutoControlled()==false){
          motorcontrol.setAutoControlled(true);
        }
      } else {
        if (motorcontrol.isAutoControlled()==true){
          motorcontrol.setAutoControlled(false);
        }
      }
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void stopMotor(){
    try
    {
      Pause();
      motorcontrol.setUseVelocityLimits(false);
      motorcontrol.setVelocity(0.0);
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public boolean isAutomaticControl()
  {
    return AutomaticControl;
  }

  public void setVord(double val)
  {
    vord = val;
    System.out.println("[Sequencer]:New order: " + vord + "%");
    //t = System.currentTimeMillis();
  }

  public double getServoPosition(){
    try
    {
      return servo.getPosition();
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    return 0.0;
  }

  public double getMotorPercent(){
    try
    {
      return motorcontrol.getVelocity();
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    return 0.0;
  }


  private void EmergencyStopRoutine()
  {
    try
    {
      if (motorcontrol.getVelocity()>0){
        motorcontrol.setVelocity(-1*motorcontrol.getLowerLimit());
      } else{
        motorcontrol.setVelocity(motorcontrol.getLowerLimit());
      }
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }

  }
  
  public void SendOrderToMotorControl(double val)
  {
    try
    {

      if (AutomaticControl == true)
      {
        motorcontrol.setAutoControlVelocity(val);
      }
      else
      {
        motorcontrol.setVelocity(val);
      }
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  private void sendtomotorcontrol()
  {
    try
    {
      motorcontrol.setAcceleration(100.0);
      if (enableFiltering == true)
      {
        if (AutomaticControl == true)
        {
          motorcontrol.setAutoControlVelocity(vact_real);
        }
        else
        {
          motorcontrol.setVelocity(vact);
        }
      }
      else
      { //filtering off
        if (AutomaticControl == true)
        {
          motorcontrol.setAutoControlVelocity(vord * maxspeed / 100.0);
        }
        else
        {
          motorcontrol.setVelocity(vord);
        }
      }
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public double getMotorControlVelocity()
  {
    double v = 0.0;
    try
    {
      v = motorcontrol.getVelocity();
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }

    return v;
  }



  public double getVact()
  {
    return vact;
  }

  public double getVord()
  {
    return vord;
  }

  public double getVact_real()
  {
    return vact_real;
  }

  public double getVord_real()
  {
    return vord_real;
  }

  public void set_t0(double val)
  {
    t0 = val;
  }

  public void setRecEnable(boolean val)
  {
    RecEnable = val;
    System.out.println("[Sequencer]: Rec: " + RecEnable);
  }

  public void clearRecData(){
    rec_counter=0;
    column.clear();
    System.out.println("[Sequencer]: Rec data erased");
    //column = new ArrayList();
  }

  public double get_t0()
  {
    return t0;
  }

  public double getMaxSpeed()
  {
    return maxspeed;
  }

  public double getEncoderSpeed()
  {
    double v = -1000000.0;
    try
    {
      if (motorcontrol.isAutoControlled()==false){
        v = motorcontrol.getAutoControlRawVelocity();
      } else {
        v=motorcontrol.getAutoControlRawVelocity();
      }
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }

    return v;
  }

    public double getEncoderSpeedFiltered()
  {
    double v = -1000000.0;
    try
    {
      if (motorcontrol.isAutoControlled()==false){
        v = encoder.getTics()* encoder_tics2m / dt;
      } else {
        v=motorcontrol.getAutoControlFilteredVelocity();
      }
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }

    return v;
  }

  private double[] getEncoderInfo()
  {

    double[] val =
    {
      0.0, 0.0
    };
    try
    {
      val[0] = encoder.getCmPerTic(); // metros por cada tic
      val[1] = encoder.getRadPerTic(); // radianes por tic

    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }

    return val;
  }

  public double[][] getSpatialInfo()
  {
    double[][] val =
    {
      {
        0.0, 0.0, 0.0
      },
      {
        0.0, 0.0, 0.0
      },
      {
        0.0, 0.0, 0.0
      }
    };
    try
    {
      val = spatial.getSpatialValue();
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    return val;
  }

  ;

  public void setServoPosition(double val)
  {
    try
    {
      System.out.println("Pos: " + val);
      servo.setPosition(val);
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  public void setFilterEnable(boolean val)
  {
    enableFiltering = val;
    if (enableFiltering==true){
      maxspeed = crv.getMaxVal(crv.getacfw()); // n m/s, ej: 1.53 m/s
    }else{
      maxspeed= 10; // 10 m/s
    }
    System.out.println("[Sequencer]:Filter enabled:" + val + ", maxspeed: "+maxspeed);
  }

  

  public boolean isFilterEnable()
  {
    return enableFiltering;
  }

  public boolean getFilterEnable()
  {
        return enableFiltering;
  }

  public void setAccelerationProcessEnable(boolean val)
  {
    enableAccelerationSpeed = val;
    System.out.println("[Sequencer]:Acceleration process enabled:" + val);
  }

  public void setInteliStopEnable(boolean val)
  {
    intelistop = val;
    if (val == false)
    {
      Start();
    }
    else
    {
      is_counter = 0;
    }
    System.out.println("[Sequencer]:InteliStop enabled:" + val);
  }

  public void setIntelistopTime(int val)
  {
    intelistoptime = val;
  }

  public void setSamplingTime(int val)
  {
    dt = val;
  }

  public double getSamplingTime()
  {
    return dt;
  }

  public double getIntelistopTime()
  {
    return intelistoptime;
  }

  public void SaveData(File file)
  {

    System.out.println("[Sequencer]:Saving data in: " + file.getName());

    try
    {
      BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
      Pause();
      //save headers
      for (int k = 0; k < col_names.size(); k++)
      {
        bw.write(col_names.get(k).toString() + ";");
      }
      bw.write("\n");

      // save data
      for (int i = 0; i < column.size(); i++)
      {
        ArrayList fila = (ArrayList) column.get(i);

        //System.out.println(i + ", " + column.get(i));

        for (int j = 0; j < fila.size(); j++)
        {
          bw.write(fila.get(j).toString() + ";");
        }
        bw.write("\n");
        //System.out.println(arr.get(i));
      }

      bw.close();
    }
    catch (IOException ex)
    {
    }
    System.out.println("[Sequencer]:End of data save");
  }

  public void WaitTime(double time)
  {
    double t1 = System.currentTimeMillis();
    double t2 = System.currentTimeMillis();

    while (t2 - t1 < time)
    {
      t2 = System.currentTimeMillis();
    }
    //System.out.println(time/1000.0 + "seconds elapsed");
  }

  public double getCurrentTime(){
    return tact;
  }

  public double getHitAcceleration(){
    try
    {
      return motorcontrol.getHitAccelerationMaximum();
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    return 0.0;
  }

  public void setHitAcceleration(double val){
    try
    {
      motorcontrol.setHitAccelerationMaximum(val);
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (DeviceException ex)
    {
      Logger.getLogger(Sequencer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}

