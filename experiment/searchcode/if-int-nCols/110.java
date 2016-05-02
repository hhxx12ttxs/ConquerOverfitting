package vplot;


import java.util.logging.Level;
import java.util.logging.Logger;
import  visad.*;
import visad.java2d.DisplayImplJ2D;
import visad.java3d.DisplayImplJ3D;
import java.rmi.RemoteException;
import java.awt.*;
import javax.swing.*;
import scalaSci.Vec;

// this class implements Matlab-like plotting routines by wrapping them to VISAD 

public class vPlot  extends JPanel {
  RealType  x, y, z;   // the quantities to be displayed in x-, y- axis and z-axis
  FunctionType  func_x_y;  // the function y = f(x), represented by (x -> y)
  Set   x_set;  // the Data values for x represented by the set
  FlatField vals_ff;  // the Data class FlatField which will hold time and height data.  x data are implicitly given by Set x_set
  DataReferenceImpl data_ref;   //  the DataReference from the data to display
  // the 2D display, and its maps
  ScalarMap  xMap, yMap, zMap, yRGBMap;
  int   figid;   // the id of this plot object
  /*   
 var t=inc(0, 0.01, 10); var x = sin(3.4*t).getv;
 var vf = new vPlot(1)
 vf.plot(x, 6)
 vf.show
 */     

  public void show() {
      JFrame vf = new JFrame("Figure+ "+figid);
      vf.setSize(500, 500);
      vf.add(vfigure.currentDisplay.getComponent());
      vf.setVisible(true);
      
  }
        
// TODO: vPlot  is needed ??
  public vPlot(int figId) {
      figid = figId;
        try {
            // create Display and its maps
            vfigure.currentDisplay  = new DisplayImplJ2D("display "+figId);
        } catch (VisADException ex) {
            Logger.getLogger(vPlot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(vPlot.class.getName()).log(Level.SEVERE, null, ex);
        }
  }

  
  /*
var t=inc(0, 0.01, 10); var x = sin(3.4*t).getv;
 
vfigure(2); 
  vplot(x, 20)
 vsubplot(1, 1, 1)
vplot(x)
   
 
*/
  
  public  JPanel  plot(Vec  vec)  {
      return plot(vec.getv());
  }
  
  public  JPanel  plot(double [] vals )  {
      return plot(vals, "x", "y");
  }
  
    
  public  JPanel  plot(double [] vals, String xtitle, String ytitle)  {
      int N = vals.length;
       try {
      x =  RealType.getRealType(xtitle); 
      y = RealType.getRealType(ytitle);
      
   // create a functionType, that is the class which represents our function
   //  This is the MathType( x -> y)
   
      //   Use FunctionType(MathType domain, MathType range)
      func_x_y = new FunctionType(x, y);
      
      // Create the x_set, with  N integer values, ranging from 0 to N-1
      // That means, that there should be N values for y
      // Use Integer1DSet(MathType type, int length)
      x_set = new Integer1DSet(x, N);
      
      // the actual values
      double [][]  actVals = new double[1][N];
      for (int k=0; k<N; k++)
          actVals[0][k] = vals[k];
      
      // create a FlatField, that is the class for the samples
      vals_ff = new FlatField(func_x_y, x_set);
  
      // and put the act_Vals in it
      vals_ff.setSamples(actVals);
  
      // create the ScalarMaps: quantity x is to be displaye along x-axis
      // and vals along y-axis
      // use ScalarMap(ScalarType scalar, DisplayRealType display_scalar)
      xMap = new ScalarMap(x, Display.XAxis);
      yMap = new ScalarMap(y,  Display.YAxis);
      
      // add maps to display
      vfigure.currentDisplay.addMap( xMap );
      vfigure.currentDisplay.addMap( yMap );
      
      GraphicsModeControl dispGMC = (GraphicsModeControl) vfigure.currentDisplay.getGraphicsModeControl();
      dispGMC.setScaleEnable(true);
      
      // create a data reference and set the FlatField as our data
      data_ref = new DataReferenceImpl("data_ref");
      data_ref.setData(vals_ff);
      
      // add reference to display
      vfigure.currentDisplay.addReference( data_ref );
      setLayout(new BorderLayout());
       
      add(vfigure.currentDisplay.getComponent());
     
       }
       catch (Exception e)  { 
        System.out.println("Error adding component");
        System.out.println("Exception:   "+e.getMessage());
       }
  
      return this;
  }
  
  /*
   var vals= Rand(30, 60)
   vimage(vals)
   */
  public  JPanel  image(double [][] vals, String xtitle, String ytitle, String ztitle)  {
      int N = vals.length;
       try {
      x =  RealType.getRealType(xtitle); 
      y = RealType.getRealType(ytitle);
      z = RealType.getRealType(ztitle);
      
      RealTupleType domain_tuple = new RealTupleType(x,  y);
      
      func_x_y = new FunctionType(domain_tuple, z);
    
      int NRows = vals.length; 
      int NCols = vals[0].length;
      Set domain_set = new Linear2DSet(domain_tuple, 0, NRows, NRows, 0, NCols, NCols);
      
      float [][] flat_samples = new  float[1][NRows*NCols];
      
      for (int c=0; c<NCols; c++)
          for (int r=0; r<NRows; r++)
               flat_samples[0][c*NRows+r] = (float) vals[r][c];
      
       FlatField vals_ff = new FlatField(func_x_y, domain_set);
       vals_ff.setSamples(flat_samples);
       
       vfigure.currentDisplay = new DisplayImplJ2D("imageDisplay");   // create a new display
       
      GraphicsModeControl dispGMC = (GraphicsModeControl) vfigure.currentDisplay.getGraphicsModeControl();
      dispGMC.setScaleEnable(true);
       
      // create the ScalarMaps: quantity x is to be displaye along x-axis
      // and vals along y-axis
      // use ScalarMap(ScalarType scalar, DisplayRealType display_scalar)
      xMap = new ScalarMap(x, Display.XAxis);
      yMap = new ScalarMap(y,  Display.YAxis);
      ScalarMap rgbMap  = new ScalarMap(z, Display.RGB);
      // add maps to display
      vfigure.currentDisplay.addMap( xMap );
      vfigure.currentDisplay.addMap( yMap );
      vfigure.currentDisplay.addMap( rgbMap );
      
      
      // create a data reference and set the FlatField as our data
      data_ref = new DataReferenceImpl("data_ref");
      data_ref.setData(vals_ff);
      
      // add reference to display
      vfigure.currentDisplay.addReference( data_ref );
      setLayout(new BorderLayout());
       
      add(vfigure.currentDisplay.getComponent());
     
       }
       catch (Exception e)  { 
        System.out.println("Error adding component");
        System.out.println("Exception:   "+e.getMessage());
       }
  
      return this;
  }
  
public  JPanel  plot3(double [] vals, String xtitle, String ytitle)  {
      int N = vals.length;
       try {
      x =  RealType.getRealType(xtitle); 
      y = RealType.getRealType(ytitle);
      
   // create a functionType, that is the class which represents our function
   //  This is the MathType( x -> y)
   
      //   Use FunctionType(MathType domain, MathType range)
      func_x_y = new FunctionType(x, y);
      
      // Create the x_set, with  N integer values, ranging from 0 to N-1
      // That means, that there should be N values for y
      // Use Integer1DSet(MathType type, int length)
      x_set = new Integer1DSet(x, N);
      
      // the actual values
      double [][]  actVals = new double[1][N];
      for (int k=0; k<N; k++)
          actVals[0][k] = vals[k];
      
      // create a FlatField, that is the class for the samples
      vals_ff = new FlatField(func_x_y, x_set);
  
      // and put the act_Vals in it
      vals_ff.setSamples(actVals);
  
      // create the ScalarMaps: quantity x is to be displaye along x-axis
      // and vals along y-axis
      // use ScalarMap(ScalarType scalar, DisplayRealType display_scalar)
      xMap = new ScalarMap(x, Display.XAxis);
      yMap = new ScalarMap(y,  Display.YAxis);
      zMap = new ScalarMap(y, Display.ZAxis);
      
      vfigure.currentDisplay = new DisplayImplJ3D("3-d");
      // add maps to display
      vfigure.currentDisplay.addMap( xMap );
      vfigure.currentDisplay.addMap( yMap );
      vfigure.currentDisplay.addMap( zMap );
      
      GraphicsModeControl dispGMC = (GraphicsModeControl) vfigure.currentDisplay.getGraphicsModeControl();
      dispGMC.setScaleEnable(true);
      
      // create a data reference and set the FlatField as our data
      data_ref = new DataReferenceImpl("data_ref");
      data_ref.setData(vals_ff);
      
      // add reference to display
      vfigure.currentDisplay.addReference( data_ref );
      setLayout(new BorderLayout());
       
      add(vfigure.currentDisplay.getComponent());
     
       }
       catch (Exception e)  { 
        System.out.println("Error adding component");
        System.out.println("Exception:   "+e.getMessage());
       }
  
      return this;
  }
  
  public JPanel plot(Vec vec, int pointSize) {
      return plot(vec, "x", "y", pointSize);
  }
  
  public JPanel plot(double [] vals, int pointSize) {
      return plot(vals, "x", "y", pointSize);
  }
  
  public  JPanel plot(Vec vec, String xtitle, String ytitle, int pointSize)  {
      return plot(vec.getv(), xtitle, ytitle,  pointSize);
  }

   public  JPanel addplot(float [] vals)  {
     return addplot(vals, "x",  "y", 1);
   }
   
   public  JPanel addplot(double [] vals)  {
     return addplot(vals, "x",  "y", 1);
   }
   
    public  JPanel addplot(Vec vals)  {
     return addplot(vals, "x", "y", 1);
   }
   
  public  JPanel addplot(float [] vals, String xtitle, String ytitle, int pointSize)  {
      double [] dvals = new double[vals.length];
      for (int k=0; k<vals.length; k++)
          dvals[k] = vals[k];
      return addplot(dvals, xtitle, ytitle, pointSize);
  }
  
  public  JPanel addplot(Vec vals, String xtitle, String ytitle, int pointSize)  {
    return addplot(vals.getv(), xtitle, ytitle, pointSize);
  }
  
    //  this routine adds one plot over existing ones at the current DisplayImpl context
  public  JPanel addplot(double [] vals, String xtitle, String ytitle, int pointSize)  {
      int N = vals.length;
       try {
     x =  RealType.getRealType(xtitle); 
     y = RealType.getRealType(ytitle);
     // create a functionType, that is the class which represents our function
   //  This is the MathType( x -> y)
   
      ConstantMap [] pointsMap = { new ConstantMap(1.0f, Display.Red), new ConstantMap(0.0f, Display.Green),
        new ConstantMap(0.0f, Display.Blue), new ConstantMap(pointSize, Display.LineWidth)};
      
      
              
      //   Use FunctionType(MathType domain, MathType range)
      func_x_y = new FunctionType(x, y);
      
      // Create the x_set, with  N integer values, ranging from 0 to N-1
      // That means, that there should be N values for y
      // Use Integer1DSet(MathType type, int length)
      x_set = new Integer1DSet(x, N);
      
      // the actual values
      double [][]  actVals = new double[1][];
      actVals[0] = vals;
      
      // create a FlatField, that is the class for the samples
      vals_ff = new FlatField(func_x_y, x_set);
  
      // and put the act_Vals in it
      vals_ff.setSamples(actVals);
  
      // create the ScalarMaps: quantity x is to be displayed along x-axis
      // and vals along y-axis
      // use ScalarMap(ScalarType scalar, DisplayRealType display_scalar)
      xMap = new ScalarMap(x, Display.XAxis);
      yMap = new ScalarMap(y,  Display.YAxis);
      
      // add maps to display
      //vfigure.currentDisplay.addMap( xMap );
     // vfigure.currentDisplay.addMap( yMap );
      
      // create a data reference and set the FlatField as our data
      data_ref = new DataReferenceImpl("data_ref");
      data_ref.setData(vals_ff);
      
      // add reference to display
      vfigure.currentDisplay.addReference( data_ref, pointsMap );
      setLayout(new BorderLayout());
      add(vfigure.currentDisplay.getComponent());
      
       }
       catch (Exception e)  { }
  
      return this;
  }
  
  
  public  JPanel plot(float [] vals, String xtitle, String ytitle, int pointSize)  {
    double [] dvals = new double[vals.length];
    for (int k=0; k<dvals.length; k++)
        dvals[k] = vals[k];
    return plot(dvals, xtitle, ytitle, pointSize);
  }
  
  public  JPanel plot(double [] vals, String xtitle, String ytitle, int pointSize)  {
      int N = vals.length;
       try {
     x =  RealType.getRealType(xtitle); 
     y = RealType.getRealType(ytitle);
     // create a functionType, that is the class which represents our function
   //  This is the MathType( x -> y)
   
      ConstantMap [] pointsMap = { new ConstantMap(1.0f, Display.Red), new ConstantMap(0.0f, Display.Green),
        new ConstantMap(0.0f, Display.Blue), new ConstantMap(pointSize, Display.LineWidth)};
      
      
              
      //   Use FunctionType(MathType domain, MathType range)
      func_x_y = new FunctionType(x, y);
      
      // Create the x_set, with  N integer values, ranging from 0 to N-1
      // That means, that there should be N values for y
      // Use Integer1DSet(MathType type, int length)
      x_set = new Integer1DSet(x, N);
      
      // the actual values
      double [][]  actVals = new double[1][];
      actVals[0] = vals;
      
      // create a FlatField, that is the class for the samples
      vals_ff = new FlatField(func_x_y, x_set);
  
      // and put the act_Vals in it
      vals_ff.setSamples(actVals);
  
      // create the ScalarMaps: quantity x is to be displaye along x-axis
      // and vals along y-axis
      // use ScalarMap(ScalarType scalar, DisplayRealType display_scalar)
      xMap = new ScalarMap(x, Display.XAxis);
      yMap = new ScalarMap(y,  Display.YAxis);
      
      // add maps to display
      vfigure.currentDisplay.addMap( xMap );
      vfigure.currentDisplay.addMap( yMap );
      
      // create a data reference and set the FlatField as our data
      data_ref = new DataReferenceImpl("data_ref");
      data_ref.setData(vals_ff);
      
      // add reference to display
      vfigure.currentDisplay.addReference( data_ref, pointsMap );
      setLayout(new BorderLayout());
      add(vfigure.currentDisplay.getComponent());
      
       }
       catch (Exception e)  { }
  
      return this;
  }
  
  
  public  JPanel plotXPoint(double  [] vals, String xtitle, String ytitle, int pointSize)  {
      float [] fvals = new float[vals.length];
      for (int k=0; k<vals.length; k++)
          fvals[k] = (float) vals[k];
      return plotXPoint(fvals, xtitle, ytitle, pointSize);
  }
      
  public  JPanel plotXPoint(float  [] vals, String xtitle, String ytitle, int pointSize)  {
      int N = vals.length;
      RealTupleType t_x_tuple;
      try {
     x =  RealType.getRealType(xtitle); 
     y = RealType.getRealType(ytitle);
     // create a functionType, that is the class which represents our function
   //  This is the MathType( x -> y)
   
     t_x_tuple = new RealTupleType(x, y);
     RealType index = RealType.getRealType("index");
             
      //   Use FunctionType( index -> (time, height)
   FunctionType func_i_tuple = new FunctionType( index, t_x_tuple);
     
     Set index_set = new Integer1DSet(index, N);
     
     ConstantMap [] pointsMap = { new ConstantMap(1.0f, Display.Red), new ConstantMap(0.0f, Display.Green),
        new ConstantMap(0.0f, Display.Blue), new ConstantMap(pointSize, Display.PointSize)};
      
    
      float [][] xsamples= index_set.getSamples();
      
      // the actual values
      float [][]  actVals = new float [2][];
      for (int k=0; k<N; k++) {
          actVals[0] = xsamples[0];
          actVals[1] = vals;
      }
      
      // create a FlatField, that is the class for the samples
      vals_ff = new FlatField(func_i_tuple, index_set);
  
      // and put the act_Vals in it
      vals_ff.setSamples(actVals);
  
      // create the ScalarMaps: quantity x is to be displaye along x-axis
      // and vals along y-axis
      // use ScalarMap(ScalarType scalar, DisplayRealType display_scalar)
      xMap = new ScalarMap(x, Display.XAxis);
      yMap = new ScalarMap(y,  Display.YAxis);
      
      // add maps to display
      vfigure.currentDisplay.addMap( xMap );
      vfigure.currentDisplay.addMap( yMap );
      
      // create a data reference and set the FlatField as our data
      data_ref = new DataReferenceImpl("data_ref");
      data_ref.setData(vals_ff);
      
      // add reference to display
      vfigure.currentDisplay.addReference( data_ref, pointsMap );
      setLayout(new BorderLayout());
      add(vfigure.currentDisplay.getComponent());
      
       }
       catch (Exception e)  { }
  
      return this;
  }
  
  public  JPanel plotXYPoint(double [] xvals, double [] yvals,  String xtitle, String ytitle, int pointSize)  {
      int N = xvals.length;
      RealTupleType t_x_tuple;
      try {
     x =  RealType.getRealType(xtitle); 
     y = RealType.getRealType(ytitle);
     // create a functionType, that is the class which represents our function
   //  This is the MathType( x -> y)
   
     t_x_tuple = new RealTupleType(x, y);
     RealType index = RealType.getRealType("index");
             
      //   Use FunctionType( index -> (time, height)
   FunctionType func_i_tuple = new FunctionType( index, t_x_tuple);
     
     Set index_set = new Integer1DSet(index, N);
     
     ConstantMap [] pointsMap = { new ConstantMap(1.0f, Display.Red), new ConstantMap(0.0f, Display.Green),
        new ConstantMap(0.0f, Display.Blue), new ConstantMap(pointSize, Display.PointSize)};
      
    
      float [][] xsamples= index_set.getSamples();
      
      // the actual values
      double [][]  actVals = new double[2][];
      for (int k=0; k<N; k++) {
          actVals[0] = xvals;
          actVals[1] = yvals;
      }
      
      // create a FlatField, that is the class for the samples
      vals_ff = new FlatField(func_i_tuple, index_set);
  
      // and put the act_Vals in it
      vals_ff.setSamples(actVals);
  
      // create the ScalarMaps: quantity x is to be displaye along x-axis
      // and vals along y-axis
      // use ScalarMap(ScalarType scalar, DisplayRealType display_scalar)
      xMap = new ScalarMap(x, Display.XAxis);
      yMap = new ScalarMap(y,  Display.YAxis);
      
      // add maps to display
      vfigure.currentDisplay.addMap( xMap );
      vfigure.currentDisplay.addMap( yMap );
      
      // create a data reference and set the FlatField as our data
      data_ref = new DataReferenceImpl("data_ref");
      data_ref.setData(vals_ff);
      
      // add reference to display
      vfigure.currentDisplay.addReference( data_ref, pointsMap );
      setLayout(new BorderLayout());
      add(vfigure.currentDisplay.getComponent());  
      
       }
       catch (Exception e)  { }
  
      return this;
  }
  
  public  JPanel plot3() { //double [] valsX, double[] valsY, double [] valsZ)  {
      //int N = valsX.length;
       try {
  RealType longitude, latitude;
  RealType altitude, temperature,  precipitation;
            
  RealTupleType domain_tuple, range_tuple;


  // The function (domain_tuple -> range_tuple )

     FunctionType func_domain_range;


   // Our Data values for the domain are represented by the Set

      Set domain_set;

      
  // The Data class FlatField

  FlatField lvals_ff;

  // The DataReference from data to display

  DataReferenceImpl ldata_ref;

  // The 2D display, and its the maps

   DisplayImpl ldisplay;
   ScalarMap latMap, lonMap;
   ScalarMap altZMap, tempRGBMap;
   ScalarMap tempZMap, altRGBMap;
   
   // Create the quantities
    // Use RealType(String name);

    latitude = RealType.getRealType("latitude",SI.meter,null);
    longitude = RealType.getRealType("longitude",SI.meter,null);

    domain_tuple = new RealTupleType(latitude, longitude);

    temperature = RealType.getRealType("temperature",SI.kelvin,null);
    altitude = RealType.getRealType("altitude",SI.meter,null);

    // Create the range tuple ( altitude, temperature )
    // Use RealTupleType( RealType[] )

    range_tuple = new RealTupleType( altitude, temperature  );


    // Create a FunctionType (domain_tuple -> range_tuple )
    // Use FunctionType(MathType domain, MathType range)

    func_domain_range = new FunctionType( domain_tuple, range_tuple);

        // Create the domain Set
    // Use LinearDSet(MathType type, double first1, double last1, int lengthX,
    //				     double first2, double last2, int lengthY)

    int NCOLS = 50;
    int NROWS = NCOLS;

    domain_set = new Linear2DSet(domain_tuple, -Math.PI, Math.PI, NROWS,
    					       -Math.PI, Math.PI, NCOLS);




    // Get the Set samples to facilitate the calculations

    float[][] set_samples = domain_set.getSamples( true );


    // We create another array, with the same number of elements of
    // altitude and temperature, but organized as
    // float[2][ number_of_samples ]

    float[][] flat_samples = new float[2][NCOLS * NROWS];

    // ...and then we fill our 'flat' array with the generated values
    // by looping over NCOLS and NROWS

    for(int c = 0; c < NCOLS; c++)

      for(int r = 0; r < NROWS; r++){

	// ...altitude
	flat_samples[0][ c * NROWS + r ] = (float) Math.cos(1.0f / (float)( (set_samples[0][ c * NROWS + r ] *
						     set_samples[0][ c * NROWS + r ]) +
						     (set_samples[1][ c * NROWS + r ] *
						     set_samples[1][ c * NROWS + r ]) + 1.0f ));

	// ...temperature
	flat_samples[1][ c * NROWS + r ] = (float)( (Math.sin( 0.50*(double) set_samples[0][ c * NROWS + r ])  ) * Math.cos( (double) set_samples[1][ c * NROWS + r ] ) ) ;


    }


    // Create a FlatField
    // Use FlatField(FunctionType type, Set domain_set)

    vals_ff = new FlatField( func_domain_range, domain_set);

    // ...and put the values above into it

    // Note the argument false, meaning that the array won't be copied

    vals_ff.setSamples( flat_samples , false );

    // Create Display and its maps

    // A 2D display

    vfigure.currentDisplay = new DisplayImplJ3D("display1");

    // Get display's graphics mode control and draw scales

    GraphicsModeControl dispGMC = (GraphicsModeControl)  vfigure.currentDisplay.getGraphicsModeControl();
    dispGMC.setScaleEnable(true);


    // Create the ScalarMaps: latitude to YAxis, longitude to XAxis and
    // altitude to ZAxis and temperature to RGB
    // Use ScalarMap(ScalarType scalar, DisplayRealType display_scalar)

    latMap = new ScalarMap( latitude,    Display.YAxis );
    lonMap = new ScalarMap( longitude, Display.XAxis );

    // Add maps to display

    vfigure.currentDisplay.addMap( latMap );
    vfigure.currentDisplay.addMap( lonMap );

     // altitude to z-axis and temperature to color

    altZMap = new ScalarMap( altitude,  Display.ZAxis );
    tempRGBMap = new ScalarMap( temperature,  Display.RGB );
    // Add maps to display
    vfigure.currentDisplay.addMap( altZMap );
    vfigure.currentDisplay.addMap( tempRGBMap );


    // Uncomment following lines to have different data depiction
    // temperature to z-axis and altitude to color

    //altRGBMap = new ScalarMap( altitude,  Display.RGB );
    //tempZMap = new ScalarMap( temperature,  Display.ZAxis );
    //display.addMap( altRGBMap );
    //display.addMap( tempZMap );


    // Create a data reference and set the FlatField as our data

    data_ref = new DataReferenceImpl("data_ref");

    data_ref.setData( vals_ff );

    // Add reference to display

    vfigure.currentDisplay.addReference( data_ref );
    setLayout(new BorderLayout());
    add(vfigure.currentDisplay.getComponent());
      
       }
       catch (Exception e)  { }
  
      return this;
  }

   /*
   public JPanel plot(double [] xv, double [] yv, double [] zv)  {
    
    RealType xl = RealType.getRealType("x");
    RealType yl = RealType.getRealType("y");
    RealType zl = RealType.getRealType("z");
    RealType vl = RealType.getRealType("v");
    FunctionType function;
    GriddedSet set, f_set;
    int size = 20;
    int dim = 3;
    int Nx = xv.length;
    int Ny = yv.length;
    
    RealTupleType domain_tuple = new RealTupleType(xl, yl);
           
          
    // compute samples
    int Nall = Nx*Ny;
    float[][] f_samples = new float[dim][Nall];
    int nz=0;
     for (int nx=0; nx<Nx; nx++) {
         for (int ny=0; ny<Ny; ny++) {
            int cidx = ny+nx*Ny;
            f_samples[0][cidx] = (float)zv[nz++];
            f_samples[1][cidx] = (float)yv[nz++];
            f_samples[2][cidx] = (float)zv[nz++];
      }
    }
    Gridded3DSet gd3 = new Gridded3DSet(domain_tuple, f_samples, Nx, Ny);
   */ 
   
   
  /*
public  JPanel plot3(double [] domX, double [] domY, double [] rZ, double [] rC)    {
    RealType   xDomRealType, yDomRealType;   // the domain types
    RealType   zRangeRealType, colorRangeRealType;   // the range 
    
      // two tuples: one to pack domX and domY together, as the domain
      // and the other for the range rZ, rC
    RealTupleType domain_tuple, range_tuple;
      // the function (domainTuple -> rangeTuple)
    FunctionType func_domain_range;
      // our data values for the domain are represented by the Set
    Set domain_set;
     // the data class FlatField
    FlatField  vals_ff;
     // the DataReference from data to display
    DataReferenceImpl data_ref;
    // the 2D display and its maps
    DisplayImpl  display;
    ScalarMap  xMap, yMap;
    ScalarMap  zMap, cRGBMap;
    
    xDomRealType = RealType.getRealType("X");
    yDomRealType = RealType.getRealType("Y");
    domain_tuple = new RealTupleType(xDomRealType, yDomRealType);
    
    zRangeRealType = RealType.getRealType("Z");
    colorRangeRealType = RealType.getRealType("C");
    range_tuple = new RealTupleType(zRangeRealType, colorRangeRealType);
    
    // create a FunctionType( domain_tuple -> range_tuple )
    func_domain_range = new FunctionType(domain_tuple, range_tuple);
    
    int lx = domX.length;
    int ly = domY.length;
    double [][] set_samples = new double[2][lx*ly];
    
    
    
 
    
}
    */

   public JPanel plot(Vec xvals, double [] yvals, String xtitle, String ytitle, int pointSize) {
       return plot(xvals.getv(), yvals, xtitle, ytitle, pointSize);
   }

    public JPanel plot(Vec xvals, double [] yvals) {
       return plot(xvals.getv(), yvals, "x",  "y", 1);
   }
 
   public JPanel plot(double [] xvals, Vec yvals, String xtitle, String ytitle, int pointSize) {
       return plot(xvals, yvals.getv(), xtitle, ytitle, pointSize);
   }
   
   public JPanel plot(double [] xvals, Vec yvals) {
       return plot(xvals, yvals.getv(), "x", "y", 1);
   }
   
   public JPanel plot(Vec xvals, Vec yvals, String xtitle, String ytitle, int pointSize) {
       return plot(xvals.getv(), yvals.getv(), xtitle, ytitle, pointSize);
   }
   
   public JPanel plot(double [] xvals, double [] yvals) {
       return plot(xvals, yvals, "x", "y", 1);
   }
   
   public  JPanel plot(double [] xvals, double [] yvals, String xttile, String ytitle, int pointSize)  {
      int N = xvals.length;
      if (N != yvals.length) {
          JOptionPane.showMessageDialog(null, "Arrays in plot(xvals, yvals) should be of the same size");
          return this;
      }
      
       try {
      x = RealType.getRealType(xttile);
      y = RealType.getRealType(ytitle);
   // create a functionType, that is the class which represents our function
   //  This is the MathType( x -> y)
   
      //   Use FunctionType(MathType domain, MathType range)
      func_x_y = new FunctionType(x, y);

      // the Gridded1DDoubleSet routine requires the elements to be sorted, so sort them according to xvals
      double [] ryvals = new double[N];
      utils.quickSort.sortXYusingX(xvals, yvals, ryvals);
      
      // the actual values
      double [][]  x_Vals = new double[1][];
      x_Vals[0]= xvals;
      double [][]  y_Vals = new double[1][];
      y_Vals[0]= ryvals;
      
      
      // Create the x_set, with  N integer values, ranging from 0 to N-1
      // That means, that there should be N values for y
      // Use Integer1DSet(MathType type, int length)
      x_set = new Gridded1DDoubleSet(x, x_Vals,  N);
      
      
      // create a FlatField, that is the class for the samples
      vals_ff = new FlatField(func_x_y, x_set);
  
      // and put the act_Vals in it
      vals_ff.setSamples(y_Vals);
  
      vfigure.currentDisplay = new DisplayImplJ2D("display1");
      GraphicsModeControl dispGMC = (GraphicsModeControl)  vfigure.currentDisplay.getGraphicsModeControl();
      dispGMC.setScaleEnable(true);
              
      // create the ScalarMaps: quantity x is to be displaye along x-axis
      // and vals along y-axis
      // use ScalarMap(ScalarType scalar, DisplayRealType display_scalar)
      xMap = new ScalarMap(x, Display.XAxis);
      yMap = new ScalarMap(y,  Display.YAxis);
      yRGBMap = new ScalarMap(y, Display.RGB);
      
      // add maps to display
      vfigure.currentDisplay.addMap( xMap );
      vfigure.currentDisplay.addMap( yMap );
      vfigure.currentDisplay.addMap(yRGBMap);
      
      // create a data reference and set the FlatField as our data
      data_ref = new DataReferenceImpl("data_ref");
      data_ref.setData(vals_ff);
      
      // add reference to display
      vfigure.currentDisplay.addReference( data_ref );
      setLayout(new BorderLayout());
      add(vfigure.currentDisplay.getComponent());
       }
       catch (Exception e)  { }
      return this;
  
  }
  
  public  JPanel plot(double [] xvals, double [] yvals, double [] zvals, String xttile, String ytitle, String ztitle, int pointSize)  {
      int Nx = xvals.length; int Ny = yvals.length; int Nz = zvals.length;
      if (Nx != Ny || Nx != Nz) {
          JOptionPane.showMessageDialog(null, "Arrays in plot(xvals, yvals, zvals) should be of the same size");
          return this;
      }
      
       try {
      x = RealType.getRealType(xttile);
      y = RealType.getRealType(ytitle);
      z = RealType.getRealType(ztitle);
   // create a functionType, that is the class which represents our function
   //  This is the MathType( x -> y)
   
      RealTupleType domain_tuple  = new RealTupleType(x, y);
      //   Use FunctionType(MathType domain, MathType range)
      FunctionType func_x_y_z = new FunctionType(domain_tuple, z);

  
      double  [][] flat_samples = new double[1][Nz];
      for  (int c=0; c<Nz; c++)
            flat_samples[0][c] = zvals[c];
      
      float [][] set_samples = new float[2][Nz];
      for (int c=0; c<Nz; c++) {
          set_samples[0][c] = (float)xvals[c];
          set_samples[1][c] = (float)yvals[c];
      }
       x_set = new Irregular2DSet(domain_tuple, set_samples);
              
      // create a FlatField, that is the class for the samples
      vals_ff = new FlatField(func_x_y_z, x_set);
  
      // and put the act_Vals in it
      vals_ff.setSamples(flat_samples, false);
  
      vfigure.currentDisplay = new DisplayImplJ3D("display1");
      GraphicsModeControl dispGMC = (GraphicsModeControl)  vfigure.currentDisplay.getGraphicsModeControl();
      dispGMC.setScaleEnable(true);
      
      // create the ScalarMaps: quantity x is to be displaye along x-axis
      // and vals along y-axis
      // use ScalarMap(ScalarType scalar, DisplayRealType display_scalar)
      xMap = new ScalarMap(x, Display.XAxis);
      yMap = new ScalarMap(y,  Display.YAxis);
      zMap = new ScalarMap(z, Display.ZAxis);
      yRGBMap = new ScalarMap(y, Display.RGB);
      
      // add maps to display
      vfigure.currentDisplay.addMap( xMap );
      vfigure.currentDisplay.addMap( yMap );
      vfigure.currentDisplay.addMap( zMap );
      vfigure.currentDisplay.addMap(yRGBMap);
      
              
      // create a data reference and set the FlatField as our data
      data_ref = new DataReferenceImpl("data_ref");
      data_ref.setData(vals_ff);
      
      // add reference to display
      vfigure.currentDisplay.addReference( data_ref );
      setLayout(new BorderLayout());
      add(vfigure.currentDisplay.getComponent());
       }
       catch (Exception e)  { }
      return this;
  
  }
  
  
  public  JPanel plotXYPoints(double [] xvals, double [] yvals, String xttile, String ytitle, int pointSize)  {
        RealType index; //  for points plot
        FunctionType func_i_tuple;
        RealTupleType x_y_tuple;
        // our data values indexed are now indexedby the Set
        Set index_set;
        
       int N = xvals.length;
      if (N != yvals.length) {
          JOptionPane.showMessageDialog(null, "Arrays in plot(xvals, yvals) should be of the same size");
          return this;
      }
      
       try {
      x = RealType.getRealType(xttile);
      y = RealType.getRealType(ytitle);
    
      // organize x and y in a Tuple
      x_y_tuple = new RealTupleType(x, y);
      
      index = RealType.getRealType("index");
      
      func_i_tuple = new FunctionType(index, x_y_tuple);
      
      index_set = new Integer1DSet(index, N);
      
   // create a functionType, that is the class which represents our function
   //  This is the MathType( x -> y)
   
      // the Gridded1DDoubleSet routine requires the elements to be sorted, so sort them according to xvals
      double [] ryvals = new double[N];
      utils.quickSort.sortXYusingX(xvals, yvals, ryvals);
      
     
      
      double [][] point_vals = new double[2][];
      point_vals[0] = xvals;
      point_vals[1] = ryvals;
      
      // create a FlatField, that is the class for the samples
      vals_ff = new FlatField(func_i_tuple, index_set);
  
      // and put the act_Vals in it
      vals_ff.setSamples(point_vals);
  
      vfigure.currentDisplay = new DisplayImplJ2D("display1");
      GraphicsModeControl dispGMC = (GraphicsModeControl)  vfigure.currentDisplay.getGraphicsModeControl();
      dispGMC.setScaleEnable(true);
              
      // create the ScalarMaps: quantity x is to be displaye along x-axis
      // and vals along y-axis
      // use ScalarMap(ScalarType scalar, DisplayRealType display_scalar)
      xMap = new ScalarMap(x, Display.XAxis);
      yMap = new ScalarMap(y,  Display.YAxis);
      yRGBMap = new ScalarMap(y, Display.RGB);
      
      // add maps to display
      vfigure.currentDisplay.addMap( xMap );
      vfigure.currentDisplay.addMap( yMap );
      vfigure.currentDisplay.addMap(yRGBMap);
      
      // create a data reference and set the FlatField as our data
      data_ref = new DataReferenceImpl("data_ref");
      data_ref.setData(vals_ff);
      
      // add reference to display
      vfigure.currentDisplay.addReference( data_ref );
      setLayout(new BorderLayout());
      add(vfigure.currentDisplay.getComponent());
       }
       catch (Exception e)  { }
      return this;
  
  }
  
  // add plot points without erasing the other plots, i.e. "hold on" mode
   public  JPanel addPlotXYPoints(double [] xvals, double [] yvals, String xttile, String ytitle, int pointSize)  {
        RealType index; //  for points plot
        FunctionType func_i_tuple;
        RealTupleType x_y_tuple;
        // our data values indexed are now indexed by the Set
        Set index_set;
        
       int N = xvals.length;
      if (N != yvals.length) {
          JOptionPane.showMessageDialog(null, "Arrays in plot(xvals, yvals) should be of the same size");
          return this;
      }
      
       try {
      x = RealType.getRealType(xttile);
      y = RealType.getRealType(ytitle);
    
      // organize x and y in a Tuple
      x_y_tuple = new RealTupleType(x, y);
      
      index = RealType.getRealType("index");
      
      func_i_tuple = new FunctionType(index, x_y_tuple);
      
      index_set = new Integer1DSet(index, N);
      
   // create a functionType, that is the class which represents our function
   //  This is the MathType( x -> y)
   
      // the Gridded1DDoubleSet routine requires the elements to be sorted, so sort them according to xvals
      double [] ryvals = new double[N];
      utils.quickSort.sortXYusingX(xvals, yvals, ryvals);
      
     
      double [][] point_vals = new double[2][];
      point_vals[0] = xvals;
      point_vals[1] = ryvals;
      
      // create a FlatField, that is the class for the samples
      vals_ff = new FlatField(func_i_tuple, index_set);
  
      // and put the act_Vals in it
      vals_ff.setSamples(point_vals); 
  
      GraphicsModeControl dispGMC = (GraphicsModeControl)  vfigure.currentDisplay.getGraphicsModeControl();
      dispGMC.setScaleEnable(true);
              
      // create the ScalarMaps: quantity x is to be displaye along x-axis
      // and vals along y-axis
      // use ScalarMap(ScalarType scalar, DisplayRealType display_scalar)
      xMap = new ScalarMap(x, Display.XAxis);
      yMap = new ScalarMap(y,  Display.YAxis);
      yRGBMap = new ScalarMap(y, Display.RGB);
      
      // add maps to display
      vfigure.currentDisplay.addMap( xMap );
      vfigure.currentDisplay.addMap( yMap );
      vfigure.currentDisplay.addMap(yRGBMap);
      
      // create a data reference and set the FlatField as our data
      data_ref = new DataReferenceImpl("data_ref");
      data_ref.setData(vals_ff);
      
      // add reference to display
      vfigure.currentDisplay.addReference( data_ref );
      setLayout(new BorderLayout());
      add(vfigure.currentDisplay.getComponent());
       }
       catch (Exception e)  { }
      return this;
  
  }
  
  
   
   
   public  static void main(String [] args) {
       
       int N=1000;
       double [] x = new double[N];
       double [] y = new double[N];
       double [] z = new double[N];
       
       for (int k=0; k<N; k++)  {
           x[k] = Math.sin(0.03*k);
           y[k] = Math.sin(0.08*k);
           z[k] = Math.cos(7*x[k]+y[k]);
       }
   vfigure.vplot(x, y, z);
     //vfigure.vplotXYPoints(x, y, "x", "y", 9);
   
   }
}
       //vfigure.vpl
      /*int N=20; int M = 30;
       double [][] vals = new double[N][M];
       for (int n=0; n<N; n++)
           for (int m=0; m<M; m++)
               vals[n][m] = Math.sin(n*m);
       
           
     vfigure.vfigure();
     vfigure.vsubplot(2, 2, 3);
     vfigure.vimage(vals);
      vfigure.vsubplot(2, 2, 2);
     vfigure.vimage(vals);
  */

   //  vfigure.vfigure().plot3(x, "txtitle", "tytitle");
     //vfigure.vplotXYPoints(x, y, "x", "y", 9);
     //vfigure.vfigure();
     //vfigure.vplotPoint(x);
     //vfigure.vplot( y);
     //vfigure.vaddplot(z);
     //vfigure.vplot(z);
     
     /* 
      vfigure(1);
      vsubplot(2, 1, 1);
      vplot(x);
      vsubplot(2, 1, 2); 
      vplot(y);
      */
       //
       /*vfigure.vfigure(1);
       vfigure.vsubplot(2, 1, 1);
       vfigure.vplot3();
       vfigure.vsubplot(2, 1, 2);
       vfigure.vplot(x);
       
        vfigure.vfigure(2);
       vfigure.vsubplot(2, 2, 2);
       vfigure.vplot3();
       vfigure.vsubplot(2, 2, 3);
       vfigure.vplot(y);
    */

        /* 
      
       
        vsubplot(2,2,4)
        vplot3
      
        
      */
       //vPlot pObj = new vPlot(0);
      // pObj.plot3(); //lot(x);
      // JFrame jf = new JFrame("test");
      // jf.add(pObj);
      // jf.setSize(400, 400);
      // jf.setVisible(true);
       
   //}

