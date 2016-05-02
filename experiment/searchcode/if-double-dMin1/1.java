import java.awt.*;
import com.java4less.rchart.*;
import java.awt.event.*;


public class CharApp extends Frame implements java.awt.event.ActionListener, ChartListener,MouseMotionListener
{
	static CharApp app= new CharApp();


	Panel ToolBar= new Panel(null);
	Panel ChartArea= new Panel();
	Chart globalChart;
	String[] lbls={"June","July","Aug.","Sept.","Oct.","Nov.","Dec."};


	public static void main(String[] args) {
          app.setTitle("RChart demo");
	      app.run();
	}


	public void run() {

	  initFrame();

	  app.show();

      Example1();

	  app.show();


	}

public void Example9() {

	   // data
	  double[] d1={1,1,3,3.5,5,4,2};
      double[] d2={2,2,5,6,5.4,3.5,3.1};

          // series
	  LineDataSerie data1= new LineDataSerie(d1,new LineStyle(0.2f,java.awt.Color.blue,LineStyle.LINE_NORMAL));
	  data1.drawPoint=true;
	  data1.valueFont=new Font("Arial",Font.BOLD,11);
      //data1.fillStyle=new FillStyle(java.awt.Color.blue);
	  LineDataSerie data2= new LineDataSerie(d2,new LineStyle(0.2f,java.awt.Color.green,LineStyle.LINE_NORMAL));
	  data2.drawPoint=true;
	  data2.valueFont=new Font("Arial",Font.BOLD,11);
      data2.fillStyle=new FillStyle(java.awt.Color.green);

      // legend
	  Legend l=new Legend();
	  //l.background=new FillStyle(java.awt.Color.lightGray);
	  //l.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
	  l.addItem("Products",new FillStyle(java.awt.Color.blue));
	  l.addItem("Services",new FillStyle(java.awt.Color.green));

	  // create title
	  com.java4less.rchart.Title title=new Title("Sales (thousands $)");
	  // create axis
	  com.java4less.rchart.Axis  XAxis=new Axis(Axis.HORIZONTAL,new Scale());
	  com.java4less.rchart.Axis  YAxis=new Axis(Axis.VERTICAL,new Scale());
      XAxis.tickAtBase=true; // draw also first tick
	  XAxis.scale.min=0;
	  YAxis.scale.min=0;
	  YAxis.scaleTickInterval=1;
	  XAxis.scaleTickInterval=1;
      YAxis.bigTickInterval=2;
      YAxis.gridStyle=new LineStyle(0.2f,java.awt.Color.gray,LineStyle.LINE_NORMAL);
	  String[] lbls={"June","July","Aug.","Sept.","Oct.","Nov.","Dec."};
	  XAxis.tickLabels=lbls;


      com.java4less.rchart.HAxisLabel XLabel= new HAxisLabel("",java.awt.Color.blue,new Font("Arial",Font.BOLD,14));
	  com.java4less.rchart.VAxisLabel YLabel= new VAxisLabel("Brutto",java.awt.Color.black,new Font("Arial",Font.BOLD,14));

	  // plotter
	  com.java4less.rchart.LinePlotter3D plot=new LinePlotter3D();

	  // create report
	  com.java4less.rchart.Chart chart=new Chart(title,plot,XAxis,YAxis);
	  plot.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
	  plot.back=new FillStyle(java.awt.Color.white);
          chart.back=new FillStyle(java.awt.Color.yellow);
          chart.back.colorFrom=java.awt.Color.yellow;
          chart.back.colorUntil=java.awt.Color.white;
          chart.back.gradientType=FillStyle.GRADIENT_HORIZONTAL;
	  chart.XLabel=XLabel;
	  chart.YLabel=YLabel;
	  chart.legend=l;
	  chart.addSerie(data2);
	  chart.addSerie(data1);

	  ChartArea.add("Center",chart);



	}


public void Example8() {

	 // data
	  double[] d1={1,2,1,4,5,4,2};
	  BarDataSerie data1= new BarDataSerie(d1,new FillStyle(java.awt.Color.orange));
	  //data1.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
          data1.borderType=BarDataSerie.BORDER_RAISED;
	  data1.valueFont=new Font("Arial",Font.BOLD,10);

	  double[] d2={-1,3,4,4.2,6.4,4.5,6.1};
	  BarDataSerie data2= new BarDataSerie(d2,new FillStyle(java.awt.Color.green));
	  data2.valueFont=new Font("Arial",Font.BOLD,10);
	  //data2.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
          data2.borderType=BarDataSerie.BORDER_RAISED;
          data2.negativeStyle=new FillStyle(java.awt.Color.red);

          // if jdk 1.2, fill partner
          //String v=java.lang.System.getProperty("java.version");
          //if (v.indexOf("1.2")>=0) {
          //   java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(5, 5,
          //                      java.awt.image.BufferedImage.TYPE_INT_RGB);
          //  java.awt.Graphics2D big = bi.createGraphics();
          //  big.setColor(Color.orange);
           // big.fillRect(0, 0, 5, 5);
          //  big.setColor(Color.red);
          //  big.fillOval(0, 0, 5, 5);
          //  java.awt.Rectangle r = new java.awt.Rectangle(0,0,5,5);
          //  data1.style.fillPatern=(new java.awt.TexturePaint(bi, r));
         //}


	  Legend l=new Legend();
	  l.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
	  l.addItem("Company A",new FillStyle(java.awt.Color.orange));
	  l.addItem("Company B",new FillStyle(java.awt.Color.green));
          l.background=new FillStyle(java.awt.Color.lightGray);

	  // create title
	  com.java4less.rchart.Title title=new Title("Benefits companies A & B");
	  // create axis
	  com.java4less.rchart.Axis  XAxis=new Axis(Axis.HORIZONTAL,new Scale());
	  com.java4less.rchart.Axis  YAxis=new Axis(Axis.VERTICAL,new Scale());
	  com.java4less.rchart.Axis  Y2Axis=new Axis(Axis.VERTICAL,new Scale());

          XAxis.tickAtBase=true; // draw also first tick
	  XAxis.scale.min=0;
	  XAxis.scale.max=7;
	  YAxis.scale.min=-2;
	  YAxis.scale.max=7;
	  YAxis.IntegerScale=true;

	  Y2Axis.scale.min=-2;
	  Y2Axis.scale.max=7;
	  Y2Axis.IntegerScale=true;
	  Y2Axis.scaleTickInterval=1;

	  YAxis.scaleTickInterval=1;
	  XAxis.scaleTickInterval=1;
	  XAxis.gridStyle=new LineStyle(0.2f,java.awt.Color.lightGray,LineStyle.LINE_DOTS);
      YAxis.gridStyle=new LineStyle(0.2f,java.awt.Color.lightGray,LineStyle.LINE_DOTS);
	  String[] lbls={"1992","1993","1994","1995","1996","1997","1998","1999"};
	  XAxis.tickLabels=lbls;

          com.java4less.rchart.HAxisLabel XLabel= new HAxisLabel("Year",java.awt.Color.black,new Font("Arial",Font.BOLD,12));
	  com.java4less.rchart.VAxisLabel YLabel= new VAxisLabel("million $",java.awt.Color.black,new Font("Arial",Font.BOLD,12));

	  // plotter
	  com.java4less.rchart.BarPlotter3D plot=new BarPlotter3D();
	  // create report
	  com.java4less.rchart.Chart chart=new Chart(title,plot,XAxis,YAxis);
      plot.interBarSpace=1;
	  plot.back=new FillStyle(java.awt.Color.white);
	  plot.depth=20;
	  plot.fullDepth=true;
	  //chart.setY2Scale(Y2Axis);
	  chart.XLabel=XLabel;
	  chart.YLabel=YLabel;
	  chart.legend=l;
	  chart.addSerie(data2);
	  chart.addSerie(data1);
	  chart.legendMargin=0.25;
      chart.back=new FillStyle(java.awt.Color.yellow);



           // chart background
          java.awt.Image im2=null;
         try{
          java.awt.MediaTracker mt2 = new java.awt.MediaTracker(this);
          im2=Toolkit.getDefaultToolkit().getImage ("back13.gif");
          mt2.addImage(im2,0); mt2.waitForID(0);
          int WidthIm = im2.getWidth(null);
          } catch (Exception e2) {}

          chart.backImage=im2;

	 ChartArea.add("Center",chart);


	}

	public void Example13() {

	  // first

	   // data
	  double[] d1={2,1 ,2 ,3 ,4 ,5 ,4 ,3};
	  double[] dmax1={0.5,0 ,0.2 ,0.1 ,0.5 ,0,1 ,0};
	  MaxMinDataSerie data1= new MaxMinDataSerie(d1,null);
	  data1.bubbleChart=true;
	  data1.fillBubble=true;
	  data1.drawPoint=true;
      data1.pointColor=java.awt.Color.green;
	  data1.setMaxMinValues(dmax1,null);

	  //data1.valueFont=new Font("Arial",Font.BOLD,10);

	  double[] d2={2,1 ,2 ,3 ,4 ,5 ,4 ,3};
	  LineDataSerie data2= new LineDataSerie(d2,new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_DOTS));
      data2.valueFont=new Font("Arial",Font.PLAIN,10);
      data2.valueColor=java.awt.Color.yellow;
	  data2.valueFont=new Font("Arial",Font.BOLD,10);

	  // second

	  double[] d3={1,2 ,3 ,2 ,3 ,4 ,1 ,2};
	  double[] dmax3={0,0.2 ,0.2 ,0 ,0.2 ,0,0.3 ,0};
	  MaxMinDataSerie data3= new MaxMinDataSerie(d3,null);
	  data3.bubbleChart=true;
	  data3.fillBubble=false;
	  data3.drawPoint=true;
      data3.pointColor=java.awt.Color.white;
	  data3.setMaxMinValues(dmax3,null);

	  double[] d4={1,2 ,3 ,2 ,3 ,4 ,1 ,2};
	  LineDataSerie data4= new LineDataSerie(d4,new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_DOTS));
      data4.valueFont=new Font("Arial",Font.PLAIN,10);
      data4.valueColor=java.awt.Color.yellow;
	  data4.valueFont=new Font("Arial",Font.BOLD,10);

	  // create title
	  com.java4less.rchart.Title title=new Title("Price");
          title.color=java.awt.Color.white;

	  // create axis
	  com.java4less.rchart.Axis  XAxis=new Axis(Axis.HORIZONTAL,new Scale());
	  com.java4less.rchart.Axis  YAxis=new Axis(Axis.VERTICAL,new Scale());
          XAxis.tickAtBase=true; // draw also first tick
	  XAxis.scale.min=-1;
	  XAxis.ceroAxis=XAxis.CEROAXIS_NO;
	  YAxis.ceroAxis=YAxis.CEROAXIS_NO;
	  YAxis.scale.min=0;
          YAxis.scale.max=6;
          YAxis.DescColor=java.awt.Color.white;
	  YAxis.scaleTickInterval=1;
	  XAxis.scaleTickInterval=1;
          XAxis.bigTickInterval=1;
          XAxis.DescColor=java.awt.Color.white;
 	  String[] lbls={"","8 Jan.","9 Jan.","10 Jan.","11 Jan.","12 Jan.","13 Jan.","14 Jan.","15 Jan."};
	  XAxis.tickLabels=lbls;
          XAxis.style =new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_NORMAL);
          YAxis.style =new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_NORMAL);

          com.java4less.rchart.HAxisLabel XLabel= new HAxisLabel("Week",java.awt.Color.white,new Font("Arial",Font.ITALIC,12));
	  com.java4less.rchart.VAxisLabel YLabel= new VAxisLabel("Value",java.awt.Color.white,new Font("Arial",Font.ITALIC,12));

	  // plotter
	  com.java4less.rchart.LinePlotter plot=new LinePlotter();

	  // create report
	  com.java4less.rchart.Chart chart=new Chart(title,plot,XAxis,YAxis);
	  chart.XLabel=XLabel;
	  chart.YLabel=YLabel;
	  chart.addSerie(data1);
	  chart.addSerie(data2);

	  chart.addSerie(data4);
	  chart.addSerie(data3);
          chart.back=new FillStyle(java.awt.Color.black);

          // chart background
          java.awt.Image im2=null;
         try{
          java.awt.MediaTracker mt2 = new java.awt.MediaTracker(this);
          im2=Toolkit.getDefaultToolkit().getImage ("back5.jpg");
          mt2.addImage(im2,0); mt2.waitForID(0);
          int WidthIm = im2.getWidth(null);
          } catch (Exception e2) {}

          chart.backImage=im2;

	  ChartArea.add("Center",chart);

	}


	public void Example3() {

	  // data
	  double[] d1={64,95,11,70};
          // style of the pie
	  FillStyle[] s1={new FillStyle(java.awt.Color.cyan),new FillStyle(java.awt.Color.blue),new FillStyle(java.awt.Color.green),new FillStyle(java.awt.Color.yellow)};
	  PieDataSerie data1= new PieDataSerie(d1,s1);
          data1.valueFont=new java.awt.Font("Arial",Font.PLAIN,14);

          // legend
	  Legend l=new Legend();
	  l.background=new FillStyle(java.awt.Color.lightGray);
	  l.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
	  l.addItem("Software",new FillStyle(java.awt.Color.blue));
	  l.addItem("Hardware",new FillStyle(java.awt.Color.green));
          l.addItem("Services",new FillStyle(java.awt.Color.yellow));
	  l.addItem("Other",new FillStyle(java.awt.Color.cyan));

	  // create title
	  com.java4less.rchart.Title title=new Title("Sales 1999");

	  // plotter
	  com.java4less.rchart.PiePlotter plot=new PiePlotter();

          // create chart
	  com.java4less.rchart.Chart chart=new Chart(title,plot,null,null);
          // chart background
          chart.back=new FillStyle(java.awt.Color.darkGray);
          chart.back.gradientType=FillStyle.GRADIENT_VERTICAL;
          chart.back.gradientCyclic=true;
          // add legend
	  chart.legend=l;
          // add data
	  chart.addSerie(data1);

          // chart background
          java.awt.Image im2=null;
         try{
          java.awt.MediaTracker mt2 = new java.awt.MediaTracker(this);
          im2=Toolkit.getDefaultToolkit().getImage ("back16.gif");
          mt2.addImage(im2,0); mt2.waitForID(0);
          int WidthIm = im2.getWidth(null);
          } catch (Exception e2) {}

          chart.backImage=im2;

	  ChartArea.add("Center",chart);

	}

	public void Example4() {
  // data
	  double[] d1={60,90,23,40};
	  boolean[] b1={true,false,true,true};
	  String[] labels={"Software","Hardware","Services","Other"};
          // style of the pie
	  FillStyle[] s1={new FillStyle(java.awt.Color.cyan),new FillStyle(java.awt.Color.blue),new FillStyle(java.awt.Color.green),new FillStyle(java.awt.Color.yellow)};
	  PieDataSerie data1= new PieDataSerie(d1,s1,b1,labels);
	  data1.valueFont=new Font("Arial",Font.BOLD,14);

          // legend
	  Legend l=new Legend();
	  l.background=new FillStyle(java.awt.Color.lightGray);
	  l.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
	  l.addItem(labels[0],new FillStyle(java.awt.Color.blue));
	  l.addItem(labels[1],new FillStyle(java.awt.Color.green));
      l.addItem(labels[2],new FillStyle(java.awt.Color.yellow));
	  l.addItem(labels[3],new FillStyle(java.awt.Color.cyan));

	  // create title
	  com.java4less.rchart.Title title=new Title("Sales 1999");

	  // plotter
	  com.java4less.rchart.PiePlotter plot=new PiePlotter();
          // 3D effect
          plot.effect3D=true;
		  plot.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);

          data1.textDistanceToCenter=0.3;
		  plot.labelFormat="#PERCENTAGE#";

          // create chart
	  com.java4less.rchart.Chart chart=new Chart(title,plot,null,null);
          // chart background
          chart.back=new FillStyle(java.awt.Color.darkGray);
          chart.back.gradientType=FillStyle.GRADIENT_VERTICAL;
          chart.back.gradientCyclic=true;
          // add legend
	  chart.legend=l;
          // legend position and layout
          chart.layout=Chart.LAYOUT_LEGEND_BOTTOM;
          chart.bottomMargin=0;
          chart.topMargin=0.2; // 20%
          l.verticalLayout=false;
          // add data
	  chart.addSerie(data1);

         // chart background
          java.awt.Image im2=null;
         try{
          java.awt.MediaTracker mt2 = new java.awt.MediaTracker(this);
          im2=Toolkit.getDefaultToolkit().getImage ("back16.gif");
          mt2.addImage(im2,0); mt2.waitForID(0);
          int WidthIm = im2.getWidth(null);
          } catch (Exception e2) {}

          chart.backImage=im2;

	  ChartArea.add("Center",chart);



	}

	public void Example2() {

	  // data
	  double[] d1={1,2,3,4,5,4,2};
	  BarDataSerie data1= new BarDataSerie(d1,new FillStyle(java.awt.Color.cyan));
	  data1.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
          //data1.borderType=BarDataSerie.BORDER_RAISED;
          data1.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
	  data1.valueFont=new Font("Arial",Font.BOLD,10);

	  double[] d2={-2,3,4,4.2,6.4,4.5,6.1};
	  BarDataSerie data2= new BarDataSerie(d2,new FillStyle(java.awt.Color.green));
	  data2.valueFont=new Font("Arial",Font.BOLD,10);
	  data2.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
          data2.negativeStyle=new FillStyle(java.awt.Color.red);
          //data2.borderType=BarDataSerie.BORDER_RAISED;

	  Legend l=new Legend();
	  l.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
	  l.addItem("Company A",new FillStyle(java.awt.Color.cyan));
	  l.addItem("Company B",new FillStyle(java.awt.Color.green));

	  // create title
	  com.java4less.rchart.Title title=new Title("Benefits companies A & B");
	  // create axis
	  com.java4less.rchart.Axis  XAxis=new Axis(Axis.HORIZONTAL,new Scale());
	  com.java4less.rchart.Axis  YAxis=new Axis(Axis.VERTICAL,new Scale());
          XAxis.tickAtBase=true; // draw also first tick
	  XAxis.scale.min=-3;
	  XAxis.scale.max=7;
	  YAxis.scale.min=0;
	  YAxis.scale.max=7;
	  YAxis.IntegerScale=true;
	  YAxis.scaleTickInterval=1;
	  XAxis.scaleTickInterval=1;
	  XAxis.gridStyle=new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_DOTS);
          YAxis.gridStyle=new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_DOTS);
	  String[] lbls={"1992","1993","1994","1995","1996","1997","1998","1999"};
	  YAxis.tickLabels=lbls;

          com.java4less.rchart.HAxisLabel XLabel= new HAxisLabel("million $",java.awt.Color.black,new Font("Arial",Font.BOLD,12));
	  com.java4less.rchart.VAxisLabel YLabel= new VAxisLabel("Year",java.awt.Color.black,new Font("Arial",Font.BOLD,12));

	  // plotter
	  com.java4less.rchart.BarPlotter plot=new BarPlotter();
	  plot.verticalBars=false;
          plot.interBarSpace=0;
	  // create report
	  com.java4less.rchart.Chart chart=new Chart(title,plot,XAxis,YAxis);
	  chart.XLabel=XLabel;
	  chart.YLabel=YLabel;
	  chart.legend=l;
	  chart.addSerie(data2);
	  chart.addSerie(data1);
          chart.back=new FillStyle(java.awt.Color.lightGray);


	  ChartArea.add("Center",chart);

	}

	public void Example1() {

	   // data
	  double[] d1={1,2,3,4,5,4,2};
          double[] d2={3,8,4,3,0,9,6.100002};

          // series
	  LineDataSerie data1= new LineDataSerie(d1,new LineStyle(2f,java.awt.Color.blue,LineStyle.LINE_NORMAL));
	  data1.drawPoint=true;
	  data1.valueFont=new Font("Arial",Font.BOLD,10);
	  LineDataSerie data2= new LineDataSerie(d2,new LineStyle(2f,java.awt.Color.green,LineStyle.LINE_NORMAL));
	  data2.drawPoint=true;
         data2.fillStyle=new FillStyle(java.awt.Color.green);
	  data2.valueFont=new Font("Arial",Font.BOLD,10);

          // legend
	  Legend l=new Legend();
	  l.background=new FillStyle(java.awt.Color.white);
	  l.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
	  l.addItem("Products",new LineStyle(0.2f,java.awt.Color.blue,LineStyle.LINE_NORMAL));
	  l.addItem("Services",new LineStyle(0.2f,java.awt.Color.green,LineStyle.LINE_NORMAL));
          //l.verticalLayout=false;

	  // create title
	  com.java4less.rchart.Title title=new Title("Sales (thousands $)");
	  // create axis
	  com.java4less.rchart.Axis  XAxis=new Axis(Axis.HORIZONTAL,new Scale());
	  com.java4less.rchart.Axis  YAxis=new Axis(Axis.VERTICAL,new Scale());
          XAxis.tickAtBase=true; // draw also first tick
	  XAxis.scale.min=0;
	  YAxis.scale.min=1;
          YAxis.scale.max=7;
	  YAxis.scaleTickInterval=1;
	  XAxis.scaleTickInterval=1;
   	  XAxis.gridStyle=new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_DOTS);
          YAxis.gridStyle=new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_DOTS);

	  XAxis.tickLabels=lbls;


          com.java4less.rchart.HAxisLabel XLabel= new HAxisLabel("Date",java.awt.Color.white,new Font("Arial",Font.BOLD,14));
	  com.java4less.rchart.VAxisLabel YLabel= new VAxisLabel("Brutto",java.awt.Color.white,new Font("Arial",Font.BOLD,14));
      YLabel.vertical=true;

	  // plotter
	  com.java4less.rchart.LinePlotter plot=new LinePlotter();

      plot.fixedLimits=true;

	  // create report
	  com.java4less.rchart.Chart chart=new Chart(title,plot,XAxis,YAxis);
      chart.back=new FillStyle(java.awt.Color.red);
      chart.back.gradientType=FillStyle.GRADIENT_HORIZONTAL;
	  chart.XLabel=XLabel;
	  chart.YLabel=YLabel;
	  chart.legend=l;
      //    chart.tmpImage=new java.awt.image.BufferedImage(300,300,java.awt.image.BufferedImage.TYPE_BYTE_INDEXED);
      //chart.layout=Chart.LAYOUT_LEGEND_BOTTOM;
	  chart.addSerie(data2);
	  chart.addSerie(data1);

	  chart.setChartListener(this);
	  chart.addMouseMotionListener(this);
	  globalChart=chart;

	  chart.virtualHeight=500;
	  chart.virtualWidth=800;

      iChart	 iC= new iChart(chart,true);
	  iC.zoomIncrement=10;
	  ChartArea.add("Center",iC);

	  this.doLayout();
	  ChartArea.doLayout();

	  iC.init();

       //chart.setSize(400,400);
       //chart.saveToFile("c:\\a.jpg","JPEG");

 }


 	public void Example12() {

	   // data
	  double[] d1={1,2,3,4,5};
      double[] d2={2,3,4,4.2,3};

          // series
	  LineDataSerie data1= new LineDataSerie(d1,new LineStyle(2f,java.awt.Color.blue,LineStyle.LINE_NORMAL));
	  data1.drawPoint=true;
	  data1.valueFont=new Font("Arial",Font.BOLD,10);
	  LineDataSerie data2= new LineDataSerie(d2,new LineStyle(2f,java.awt.Color.green,LineStyle.LINE_NORMAL));
	  data2.drawPoint=true;
	  data2.valueFont=new Font("Arial",Font.BOLD,10);
	  data2.fillStyle=new FillStyle(java.awt.Color.green);



          // legend
	  Legend l=new Legend();
	  l.background=new FillStyle(java.awt.Color.white);
	  l.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
	  l.addItem("Products",new LineStyle(0.2f,java.awt.Color.blue,LineStyle.LINE_NORMAL));
	  l.addItem("Services",new LineStyle(0.2f,java.awt.Color.green,LineStyle.LINE_NORMAL));
          //l.verticalLayout=false;

	  // create title
	  com.java4less.rchart.Title title=new Title("Sales (thousands $)");

	  // plotter
	  com.java4less.rchart.RadarPlotter plot=new RadarPlotter();

	  double[] fMaxs={6,6,6,6,6};
	  double[] fMins={0,0,0,0,0};
	  String[] faktors={"Faktor1","Faktor2","Faktor3","Faktor4","Faktor5","Faktor6"};

	  plot.faktorMaxs=fMaxs;
	  plot.faktorMins=fMins;
	  plot.faktorNames=faktors;
	  plot.backStyle=new FillStyle(java.awt.Color.yellow);

	  plot.gridStyle=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_DASHED);
	  plot.gridFont=new Font("Arial",Font.PLAIN,10);

	  // create report
	  com.java4less.rchart.Chart chart=new Chart(title,plot,null,null);
      chart.back=new FillStyle(java.awt.Color.red);
      chart.back.gradientType=FillStyle.GRADIENT_HORIZONTAL;
	  chart.legend=l;
          //chart.layout=Chart.LAYOUT_LEGEND_BOTTOM;
	  chart.addSerie(data2);
	  chart.addSerie(data1);

       ChartArea.add("Center",chart);

      // chart.setSize(400,400);
      // chart.saveToFile("c:\\a.jpg","JPEG");

 }



 	public void Example5() {

	   // data
	  double[] d1={1,2,3,4,5,4,2};
          double[] d2={2,3,4,6,6.4,4.5,6.1};

          // series
	  LineDataSerie data1= new LineDataSerie(d1,new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL));
	  data1.drawPoint=true;
	  data1.valueFont=new Font("Arial",Font.BOLD,10);
          data1.fillStyle=new FillStyle(java.awt.Color.blue);
	  LineDataSerie data2= new LineDataSerie(d2,new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL));
	  data2.drawPoint=true;
	  data2.valueFont=new Font("Arial",Font.BOLD,10);
          data2.fillStyle=new FillStyle(java.awt.Color.green);

          // legend
	  Legend l=new Legend();
	  //l.background=new FillStyle(java.awt.Color.lightGray);
	  //l.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
	  l.addItem("Products",new FillStyle(java.awt.Color.blue));
	  l.addItem("Services",new FillStyle(java.awt.Color.green));

	  // create title
	  com.java4less.rchart.Title title=new Title("Sales (thousands $)");
	  // create axis
	  com.java4less.rchart.Axis  XAxis=new Axis(Axis.HORIZONTAL,new Scale());
	  com.java4less.rchart.Axis  YAxis=new Axis(Axis.VERTICAL,new Scale());
          XAxis.tickAtBase=true; // draw also first tick
	  XAxis.scale.min=0;
	  YAxis.scale.min=0;
	  YAxis.scaleTickInterval=1;
	  XAxis.scaleTickInterval=1;
          YAxis.bigTickInterval=2;
          YAxis.gridStyle=new LineStyle(0.2f,java.awt.Color.gray,LineStyle.LINE_NORMAL);
	  String[] lbls={"June","July","Aug.","Sept.","Oct.","Nov.","Dec."};
	  XAxis.tickLabels=lbls;


          com.java4less.rchart.HAxisLabel XLabel= new HAxisLabel("",java.awt.Color.blue,new Font("Arial",Font.BOLD,14));
	  com.java4less.rchart.VAxisLabel YLabel= new VAxisLabel("Brutto",java.awt.Color.black,new Font("Arial",Font.BOLD,14));

	  // plotter
	  com.java4less.rchart.LinePlotter plot=new LinePlotter();

	  // create report
	  com.java4less.rchart.Chart chart=new Chart(title,plot,XAxis,YAxis);
          chart.back=new FillStyle(java.awt.Color.yellow);
          chart.back.colorFrom=java.awt.Color.yellow;
          chart.back.colorUntil=java.awt.Color.white;
          chart.back.gradientType=FillStyle.GRADIENT_HORIZONTAL;
	  chart.XLabel=XLabel;
	  chart.YLabel=YLabel;
	  chart.legend=l;
	  chart.addSerie(data2);
	  chart.addSerie(data1);

	  ChartArea.add("Center",chart);

	}


	public void Example6() {

	  // data
	  double[] d1={1,2,3,4,5,4,2};
	  BarDataSerie data1= new BarDataSerie(d1,new FillStyle(java.awt.Color.orange));
	  //data1.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
          data1.borderType=BarDataSerie.BORDER_RAISED;
	  data1.valueFont=new Font("Arial",Font.BOLD,10);

	  double[] d2={2,3,4,4.2,6.4,4.5,6.1};
	  BarDataSerie data2= new BarDataSerie(d2,new FillStyle(java.awt.Color.green));
	  data2.valueFont=new Font("Arial",Font.BOLD,10);
	  //data2.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
          data2.borderType=BarDataSerie.BORDER_RAISED;
          data2.negativeStyle=new FillStyle(java.awt.Color.red);

          // if jdk 1.2, fill partner
          //String v=java.lang.System.getProperty("java.version");
          //if (v.indexOf("1.2")>=0) {
          //   java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(5, 5,
          //                      java.awt.image.BufferedImage.TYPE_INT_RGB);
          //  java.awt.Graphics2D big = bi.createGraphics();
          //  big.setColor(Color.orange);
           // big.fillRect(0, 0, 5, 5);
          //  big.setColor(Color.red);
          //  big.fillOval(0, 0, 5, 5);
          //  java.awt.Rectangle r = new java.awt.Rectangle(0,0,5,5);
          //  data1.style.fillPatern=(new java.awt.TexturePaint(bi, r));
         //}


	  Legend l=new Legend();
	  l.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
	  l.addItem("Company A",new FillStyle(java.awt.Color.orange));
	  l.addItem("Company B",new FillStyle(java.awt.Color.green));
          l.background=new FillStyle(java.awt.Color.lightGray);

	  // create title
	  com.java4less.rchart.Title title=new Title("Benefits companies A & B");
	  // create axis
	  com.java4less.rchart.Axis  XAxis=new Axis(Axis.HORIZONTAL,new Scale());
	  com.java4less.rchart.Axis  YAxis=new Axis(Axis.VERTICAL,new Scale());
          XAxis.tickAtBase=true; // draw also first tick
	  XAxis.scale.min=0;
	  XAxis.scale.max=7;
	  YAxis.scale.min=0;
	  YAxis.scale.max=7;
	  YAxis.IntegerScale=true;
	  YAxis.scaleTickInterval=1;
	  XAxis.scaleTickInterval=1;
	  XAxis.gridStyle=new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_DOTS);
          YAxis.gridStyle=new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_DOTS);
	  String[] lbls={"1992","1993","1994","1995","1996","1997","1998","1999"};
	  XAxis.tickLabels=lbls;

          com.java4less.rchart.HAxisLabel XLabel= new HAxisLabel("Year",java.awt.Color.black,new Font("Arial",Font.BOLD,12));
	  com.java4less.rchart.VAxisLabel YLabel= new VAxisLabel("million $",java.awt.Color.black,new Font("Arial",Font.BOLD,12));

	  // plotter
	  com.java4less.rchart.BarPlotter plot=new BarPlotter();
	  // create report
	  com.java4less.rchart.Chart chart=new Chart(title,plot,XAxis,YAxis);
          plot.interBarSpace=1;
	  chart.XLabel=XLabel;
	  chart.YLabel=YLabel;
	  chart.legend=l;
	  chart.addSerie(data2);
	  chart.addSerie(data1);
          chart.back=new FillStyle(java.awt.Color.lightGray);

           // chart background
          java.awt.Image im2=null;
         try{
          java.awt.MediaTracker mt2 = new java.awt.MediaTracker(this);
          im2=Toolkit.getDefaultToolkit().getImage ("back13.gif");
          mt2.addImage(im2,0); mt2.waitForID(0);
          int WidthIm = im2.getWidth(null);
          } catch (Exception e2) {}

          chart.backImage=im2;

	  ChartArea.add("Center",chart);

	}

        public void Example7() {

	  // data
	  double[] d1={1,2,3,4,5,4,2};
	  BarDataSerie data1= new BarDataSerie(d1,new FillStyle(java.awt.Color.blue));
	  //data1.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
          data1.borderType=BarDataSerie.BORDER_RAISED;

	  double[] d2={2,3,4,1.2,1.4,1.5,3.1};
	  BarDataSerie data2= new BarDataSerie(d2,new FillStyle(java.awt.Color.red));
	  data2.valueFont=new Font("Arial",Font.BOLD,10);
	  //data2.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
          data2.borderType=BarDataSerie.BORDER_RAISED;


	  Legend l=new Legend();
	  l.addItem("Company A",new FillStyle(java.awt.Color.red));
	  l.addItem("Company B",new FillStyle(java.awt.Color.blue));

	  // create title
	  com.java4less.rchart.Title title=new Title("Benefits companies A + B");
	  // create axis
	  com.java4less.rchart.Axis  XAxis=new Axis(Axis.HORIZONTAL,new Scale());
	  com.java4less.rchart.Axis  YAxis=new Axis(Axis.VERTICAL,new Scale());
          XAxis.tickAtBase=true; // draw also first tick
	  XAxis.scale.min=0;
	  XAxis.scale.max=8;
	  YAxis.scale.min=0;
	  YAxis.scale.max=7;
	  YAxis.IntegerScale=true;
	  YAxis.scaleTickInterval=1;
	  XAxis.scaleTickInterval=1;
	  XAxis.style=new LineStyle(2f,java.awt.Color.lightGray,LineStyle.LINE_NORMAL);
          YAxis.style=new LineStyle(2f,java.awt.Color.lightGray,LineStyle.LINE_NORMAL);
	  XAxis.gridStyle=new LineStyle(0.2f,java.awt.Color.lightGray,LineStyle.LINE_DASHED);
          YAxis.gridStyle=new LineStyle(0.2f,java.awt.Color.lightGray,LineStyle.LINE_DASHED);
	  String[] lbls={"","1993","1994","1995","1996","1997","1998","1999",""};
	  XAxis.tickLabels=lbls;

          com.java4less.rchart.HAxisLabel XLabel= new HAxisLabel("Year",java.awt.Color.black,new Font("Arial",Font.BOLD,12));
	  com.java4less.rchart.VAxisLabel YLabel= new VAxisLabel("million $",java.awt.Color.black,new Font("Arial",Font.BOLD,12));

	  // plotter
	  com.java4less.rchart.BarPlotter plot=new BarPlotter();
	  // create report
	  com.java4less.rchart.Chart chart=new Chart(title,plot,XAxis,YAxis);
	  chart.XLabel=XLabel;
	  chart.YLabel=YLabel;
	  chart.legend=l;
	  chart.addSerie(data2);
	  chart.addSerie(data1);
          chart.back=new FillStyle(java.awt.Color.lightGray);
          plot.cumulative=true;
          plot.back=new FillStyle(java.awt.Color.white);

	  ChartArea.add("Center",chart);


	}

 public void Example10() {

	   // data
	  double[] d1={1,2,3,4,5,3,4};
          double[] d2={0,1,2,3,4,5,3,4};

          // series
	  LineDataSerie data2= new LineDataSerie(d2,new LineStyle(2f,java.awt.Color.blue,LineStyle.LINE_NORMAL));
	  data2.valueFont=new Font("Arial",Font.BOLD,10);
	  BarDataSerie data1= new BarDataSerie(d1,new FillStyle(java.awt.Color.orange));
	  data1.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);

          // legend
	  Legend l=new Legend();
	  l.background=new FillStyle(java.awt.Color.white);
	  l.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
	  l.addItem("Products",new LineStyle(0.2f,java.awt.Color.blue,LineStyle.LINE_NORMAL));
          l.verticalLayout=false;

	  // create title
	  com.java4less.rchart.Title title=new Title("Sales (thousands $)");
	  // create axis
	  com.java4less.rchart.Axis  XAxis=new Axis(Axis.HORIZONTAL,new Scale());
	  com.java4less.rchart.Axis  YAxis=new Axis(Axis.VERTICAL,new Scale());
          XAxis.tickAtBase=true; // draw also first tick
	  XAxis.scale.min=0;
	  YAxis.scale.min=0;
          YAxis.scale.max=7;
	  YAxis.scaleTickInterval=1;
	  XAxis.scaleTickInterval=1;
	  String[] lbls={"","June","July","Aug.","Sept.","Oct.","Nov.","Dec."};
	  XAxis.tickLabels=lbls;


          com.java4less.rchart.HAxisLabel XLabel= new HAxisLabel("",java.awt.Color.blue,new Font("Arial",Font.BOLD,14));
	  com.java4less.rchart.VAxisLabel YLabel= new VAxisLabel("Brutto",java.awt.Color.white,new Font("Arial",Font.BOLD,14));

	  // plotter
	  com.java4less.rchart.LinePlotter plot=new LinePlotter();

         // second plotter
          com.java4less.rchart.BarPlotter plot2=new BarPlotter();

	  // create report
	  com.java4less.rchart.Chart chart=new Chart(title,plot2,XAxis,YAxis);
          chart.addPlotter(plot);
          chart.back=new FillStyle(java.awt.Color.green);
          chart.back.colorUntil=java.awt.Color.green;
          chart.back.colorFrom=java.awt.Color.white;
          chart.back.gradientType=FillStyle.GRADIENT_VERTICAL;
	  chart.XLabel=XLabel;
	  chart.YLabel=YLabel;
	  chart.legend=l;
          chart.layout=Chart.LAYOUT_LEGEND_BOTTOM;
	  plot.addSerie(data2);
	  plot2.addSerie(data1);
          plot2.barWidth=20;

	  ChartArea.add("Center",chart);

	}



	public void Example14() {

	   // data
	  double[] d1={2,1 ,2 ,3 ,4 ,5 ,4 ,3};
	  double[] dmin1={1.5,0.85 ,1.5 ,2.8 ,3, 4.8 ,3.5,2.5 };
	  double[] dmax1={2.3,1.15 ,2.5 ,3.2 ,4.4 ,5.3,4.5 ,3.3};
	  MaxMinDataSerie data1= new MaxMinDataSerie(d1,null);
	  data1.drawPoint=true;
          data1.drawLineEnd=true;
          data1.pointColor=java.awt.Color.green;
	  data1.setMaxMinValues(dmax1,dmin1);
          data1.maxminStyle=new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_NORMAL);

	  //data1.valueFont=new Font("Arial",Font.BOLD,10);

	  double[] d2={2,1 ,2 ,3 ,4 ,5 ,4 ,3};
	  LineDataSerie data2= new LineDataSerie(d2,new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_DOTS));
          data2.valueFont=new Font("Arial",Font.PLAIN,10);
          data2.valueColor=java.awt.Color.green;

	  // create title
	  com.java4less.rchart.Title title=new Title("Price");
          title.color=java.awt.Color.white;

	  // create axis
	  com.java4less.rchart.Axis  XAxis=new Axis(Axis.HORIZONTAL,new Scale());
	  com.java4less.rchart.Axis  YAxis=new Axis(Axis.VERTICAL,new Scale());
          XAxis.tickAtBase=true; // draw also first tick
	  XAxis.scale.min=0;
	  YAxis.scale.min=0;
          YAxis.scale.max=6;
          YAxis.DescColor=java.awt.Color.white;
	  YAxis.scaleTickInterval=1;
	  XAxis.scaleTickInterval=1;
          XAxis.bigTickInterval=1;
          XAxis.DescColor=java.awt.Color.white;
 	  String[] lbls={"8 Jan.","9 Jan.","10 Jan.","11 Jan.","12 Jan.","13 Jan.","14 Jan.","15 Jan."};
	  XAxis.tickLabels=lbls;
          XAxis.style =new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_NORMAL);
          YAxis.style =new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_NORMAL);

          com.java4less.rchart.HAxisLabel XLabel= new HAxisLabel("Week",java.awt.Color.white,new Font("Arial",Font.ITALIC,12));
	  com.java4less.rchart.VAxisLabel YLabel= new VAxisLabel("Value",java.awt.Color.white,new Font("Arial",Font.ITALIC,12));

	  // plotter
	  com.java4less.rchart.LinePlotter plot=new LinePlotter();

	  // create report
	  com.java4less.rchart.Chart chart=new Chart(title,plot,XAxis,YAxis);
	  chart.XLabel=XLabel;
	  chart.YLabel=YLabel;
	  chart.addSerie(data2);
	  chart.addSerie(data1);
          chart.back=new FillStyle(java.awt.Color.black);

          // chart background
          java.awt.Image im2=null;
         try{
          java.awt.MediaTracker mt2 = new java.awt.MediaTracker(this);
          im2=Toolkit.getDefaultToolkit().getImage ("back5.jpg");
          mt2.addImage(im2,0); mt2.waitForID(0);
          int WidthIm = im2.getWidth(null);
          } catch (Exception e2) {}

          chart.backImage=im2;

	  ChartArea.add("Center",chart);

	}




  	public void Example15() {

          // load images
          int WidthIm=0;
         java.awt.Image im1=null;
         try{
          java.awt.MediaTracker mt1 = new java.awt.MediaTracker(this);
          im1=Toolkit.getDefaultToolkit().getImage ("point.gif");
          mt1.addImage(im1,0); mt1.waitForID(0);
          WidthIm= im1.getWidth(null);
          } catch (Exception e2) {}

         java.awt.Image im2=null;
         try{
          java.awt.MediaTracker mt2 = new java.awt.MediaTracker(this);
          im2=Toolkit.getDefaultToolkit().getImage ("point2.gif");
          mt2.addImage(im2,0); mt2.waitForID(0);
          WidthIm = im2.getWidth(null);
          } catch (Exception e2) {}

         // load backgorund

         java.awt.Image im3=null;
         try{
          java.awt.MediaTracker mt3 = new java.awt.MediaTracker(this);
          im3=Toolkit.getDefaultToolkit().getImage ("back19.jpg");
          mt3.addImage(im3,0); mt3.waitForID(0);
          WidthIm= im3.getWidth(null);
          } catch (Exception e2) {}

	   // data
	  double[] d1={1,4,3,4,5,4,2};
          double[] d2={2,3,4,6,6.4,4.5,6.1};

          // series
	  LineDataSerie data1= new LineDataSerie(d1,new LineStyle(0.2f,java.awt.Color.blue,LineStyle.LINE_NORMAL));
	  data1.drawPoint=true;
          data1.icon=im1;
          data1.lineType=LinePlotter.TYPE_CUBIC_NATURAL;


	  LineDataSerie data2= new LineDataSerie(d2,new LineStyle(2f,java.awt.Color.green,LineStyle.LINE_DASHED));
	  data2.drawPoint=true;
          data2.icon=im2;
          data2.lineType=LinePlotter.TYPE_LEAST_SQUARES_LINE;


          // legend
	  Legend l=new Legend();
	  //l.background=new FillStyle(java.awt.Color.lightGray);
	  //l.border=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
	  l.addItem("Products",im1);
	  l.addItem("Services",im2);

	  // create title
	  com.java4less.rchart.Title title=new Title("Sales (thousands $)");
	  // create axis
	  com.java4less.rchart.Axis  XAxis=new Axis(Axis.HORIZONTAL,new Scale());
	  com.java4less.rchart.Axis  YAxis=new Axis(Axis.VERTICAL,new Scale());
          XAxis.tickAtBase=true; // draw also first tick
	  XAxis.scale.min=0;
	  YAxis.scale.min=0;
	  YAxis.scaleTickInterval=1;
	  XAxis.scaleTickInterval=1;
          YAxis.bigTickInterval=2;
          YAxis.gridStyle=new LineStyle(0.2f,java.awt.Color.gray,LineStyle.LINE_NORMAL);
	  String[] lbls={"June","July","Aug.","Sept.","Oct.","Nov.","Dec."};
	  XAxis.tickLabels=lbls;


          com.java4less.rchart.HAxisLabel XLabel= new HAxisLabel("",java.awt.Color.blue,new Font("Arial",Font.BOLD,14));
	  com.java4less.rchart.VAxisLabel YLabel= new VAxisLabel("Brutto",java.awt.Color.black,new Font("Arial",Font.BOLD,14));

	  // plotter
	  com.java4less.rchart.CurvePlotter plot=new CurvePlotter();

	  // create report
	  com.java4less.rchart.Chart chart=new Chart(title,plot,XAxis,YAxis);
          chart.back=new FillStyle(java.awt.Color.yellow);
          chart.back.colorFrom=java.awt.Color.yellow;
          chart.back.colorUntil=java.awt.Color.white;
          chart.back.gradientType=FillStyle.GRADIENT_HORIZONTAL;
	  chart.XLabel=XLabel;
	  chart.YLabel=YLabel;
	  chart.legend=l;
	  chart.addSerie(data2);
	  chart.addSerie(data1);
          chart.backImage=im3;

	  ChartArea.add("Center",chart);

	}

       public void Example11() {

	   // data
	  double[] d1=      {2   ,1    ,2  ,3  ,4.3 ,5   ,4   ,3};
          double[] dclose1={ 2.3 , 1.15 ,2.5,2.8,4 ,5.1 ,3.5 ,3.2};
	  double[] dmin1={1.5    , 0.85,1.5,2.8,3 , 4.8,3.0 ,2.5 };
	  double[] dmax1={2.5    ,1.5 ,2.6,3.2, 4.4,5.3,4.5 ,3.3};
	  MaxMinDataSerie data1= new MaxMinDataSerie(d1,dclose1,dmin1,dmax1,null);
          data1.drawLineEnd=false;
	  data1.setMaxMinValues(dmax1,dmin1);
          data1.maxminStyle=new LineStyle(0.2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
          data1.drawPoint=false;
          data1.positiveValueColor=java.awt.Color.yellow;

	  // create title
	  com.java4less.rchart.Title title=new Title("Price");
          title.color=java.awt.Color.black;

	  // create axis
	  com.java4less.rchart.Axis  XAxis=new Axis(Axis.HORIZONTAL,new Scale());
	  com.java4less.rchart.Axis  YAxis=new Axis(Axis.VERTICAL,new Scale());
          XAxis.tickAtBase=false; // draw also first tick
          YAxis.ceroAxis=YAxis.CEROAXIS_NO;
          YAxis.gridStyle=new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_DASHED);
	  XAxis.scale.min=-1;
	  YAxis.scale.min=0;
          YAxis.scale.max=6;
          YAxis.DescColor=java.awt.Color.black;
	  YAxis.scaleTickInterval=1;
	  XAxis.scaleTickInterval=1;
          XAxis.bigTickInterval=1;
          XAxis.gridStyle=new LineStyle(0.2f,java.awt.Color.white,LineStyle.LINE_DASHED);
          XAxis.DescColor=java.awt.Color.black;
 	  String[] lbls={"8 Jan.","9 Jan.","10 Jan.","11 Jan.","12 Jan.","13 Jan.","14 Jan.","15 Jan."};
	  XAxis.tickLabels=lbls;
          XAxis.style =new LineStyle(2f,java.awt.Color.black,LineStyle.LINE_NORMAL);
          YAxis.style =new LineStyle(2f,java.awt.Color.black,LineStyle.LINE_NORMAL);

          com.java4less.rchart.HAxisLabel XLabel= new HAxisLabel("Week",java.awt.Color.black,new Font("Arial",Font.ITALIC,12));
	  com.java4less.rchart.VAxisLabel YLabel= new VAxisLabel("Value",java.awt.Color.black,new Font("Arial",Font.ITALIC,12));

	  // plotter
	  com.java4less.rchart.LinePlotter plot=new LinePlotter();
          plot.MaxMinType=plot.MM_CANDLESTICK;

	  // create report
	  com.java4less.rchart.Chart chart=new Chart(title,plot,XAxis,YAxis);
	  chart.XLabel=XLabel;
	  chart.YLabel=YLabel;
	  chart.addSerie(data1);
          chart.back=new FillStyle(java.awt.Color.black);

          // chart background
          java.awt.Image im2=null;
         try{
          java.awt.MediaTracker mt2 = new java.awt.MediaTracker(this);
          im2=Toolkit.getDefaultToolkit().getImage ("back13.gif");
          mt2.addImage(im2,0); mt2.waitForID(0);
          int WidthIm = im2.getWidth(null);
          } catch (Exception e2) {}

          chart.backImage=im2;

	  ChartArea.add("Center",chart);

	}




        Button btnExit= new Button("Exit");
        List list =new List(2,false);

	public void initFrame() {

		this.setLayout(new java.awt.BorderLayout());
		ChartArea.setLayout(new java.awt.BorderLayout());

		this.setSize(new Dimension(500,500));

		ToolBar.setSize(new Dimension(100,70));
		ToolBar.setBackground(java.awt.Color.lightGray);


                list.setBounds(4,2,120,60);
                list.add("1. Line chart");
                list.add("2. Bar Chart");
                list.add("3. Pie chart");
                list.add("4. Pie chart 3D");
                list.add("5. Area chart");
                list.add("6. Column chart");
                list.add("7. Stackbar chart");
                list.add("8. Column Chart 3D");
                list.add("9. Line chart 3D");
                list.add("10. Combined");
                list.add("11. Candlestick");
                list.add("12. Radar");
				list.add("13. BubbleChart");
                list.add("14. Max/Min chart");
                list.add("15. Images");
                list.addActionListener(this);

		btnExit.setBounds(128,15,50,30);
                btnExit.setActionCommand("EXIT");
                btnExit.addActionListener(this);
                ToolBar.add(list);
                ToolBar.add(btnExit);

		this.add("Center", ChartArea);
		this.add("North",ToolBar);

        this.doLayout();

	}

  public void actionPerformed(java.awt.event.ActionEvent a) {

     String c=a.getActionCommand();
     if (c.compareTo("EXIT")==0) {System.exit(0);}

     ChartArea.removeAll();

     if (c.substring(0,2).compareTo("1.")==0)  {Example1();}
     if (c.substring(0,2).compareTo("2.")==0)  {Example2();}
     if (c.substring(0,2).compareTo("3.")==0)  {Example3();}
     if (c.substring(0,2).compareTo("4.")==0)  {Example4();}
     if (c.substring(0,2).compareTo("5.")==0)  {Example5();}
     if (c.substring(0,2).compareTo("6.")==0)  {Example6();}
     if (c.substring(0,2).compareTo("7.")==0)  {Example7();}
     if (c.substring(0,2).compareTo("8.")==0)  {Example8();}
     if (c.substring(0,2).compareTo("9.")==0)  {Example9();}
     if (c.substring(0,2).compareTo("10")==0)  {Example10();}
     if (c.substring(0,2).compareTo("11")==0)  {Example11();}
     if (c.substring(0,2).compareTo("12")==0)  {Example12();}
	 if (c.substring(0,2).compareTo("13")==0)  {Example13();}
     if (c.substring(0,2).compareTo("14")==0)  {Example14();}
     if (c.substring(0,2).compareTo("15")==0)  {Example15();}


     this.paintAll(this.getGraphics());


  }


   // chart listener
 public void paintUserExit(Chart c,Graphics g) {
	 // paint values on screen, where the cursor is

	 if ((c.currentX>0) && (c.currentY>0)) {
		 g.setColor(java.awt.Color.yellow); // background
		 g.fillRect(c.currentX,c.currentY-15,40,15);
		 g.setColor(java.awt.Color.black); // border
		 g.drawRect(c.currentX,c.currentY-15,40,15);
		 // value
		 g.setFont(new Font("Serif",Font.PLAIN,10));
		 g.drawString(""+lbls[(int) c.currentValueX]+": "+(int) c.currentValueY,c.currentX+2,c.currentY-2);
	 }

 }


	   public void mouseMoved(MouseEvent e) {
		   // mouse moved in chart, repaint
		   //if ((globalChart.currentX>0) && (globalChart.currentY>0)) {
			   globalChart.paint(globalChart.getGraphics());
		   //}
	   }
       public void mouseExited(MouseEvent e) {}
	   public void mouseEntered(MouseEvent e) {}
	   public void mouseDragged(MouseEvent e) {}

}

