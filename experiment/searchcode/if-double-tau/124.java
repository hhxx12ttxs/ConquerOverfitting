import java.awt.*;
import java.awt.event.*;
import java.applet.*;

// -= ??????????? ?????? =- 
public class platon extends Applet{
  controls controls;   // The controls for marking and filling arcs
  figure canvas;       // The drawing area to display arcs
 // - ??????????? - ??????? ???????? ?????????? ? ??????? ????????? -
  public void init(){
    setLayout(new BorderLayout());
    canvas = new figure();
    canvas.InitData(1);
    add("Center", canvas);
    add("South", controls = new controls(canvas));
  }
 // - ?????????? - ?????????? ???????? ?????????? ? ??????? ????????? -
  public void destroy(){
    remove(controls);
    remove(canvas);
  }
 // - ????????? ??????? -
  public void start(){ controls.setEnabled(true); }
  public void stop(){ controls.setEnabled(false); } 
  public void processEvent(AWTEvent e){
    if (e.getID() == Event.WINDOW_DESTROY){ System.exit(0); }
  }
  public String getAppletInfo(){ return "Platon v1.0. Info :)"; }
 // - ???????? ????????? -
  public static void main(String args[]){
    Frame f = new Frame("Platon3D");
    platon Platon3D = new platon();
    Platon3D.init();
    Platon3D.start();
    f.add("Center",Platon3D);
    f.setSize(300,300);
    f.show();
  }
}

// -= ?????????????? ?????? =- 
class figure extends Canvas{
  int Complex,x0,y0;
  static final double pi = 3.14159265;
  int MaxDimSpace = 6;
  int MaxDimNods = 96;
  int MaxDimLoops = 200;
  String Mask = "123456789ABCDEFGHIJKLMNOPQRSTVUWXYZ";
  double Dx,Dy;
 // Geometry Definitions
  double DimAngle = 300;
  int DimSpace;
  int DimNods;
  int DimLoops;
  int DimPlanes;
  double W[][] = new double[MaxDimNods][MaxDimSpace];
  int Loops[][] = new int[MaxDimLoops][2];
  double Rotate[][] = new double[MaxDimSpace][MaxDimSpace];
 // Geometry Data
  int DS[] = { 4, 3, 3, 5, 4, 4, 4, 5,  6, 3}; // ?????p?????
  int DN[] = { 4, 8, 6, 5, 8,24,16,32, 64,12}; // ????? ??p???
  int DL[] = { 6,12,12,10,24,96,32,80,192,30}; // ????? p???p
  int DP[] = { 4, 6, 8, 4, 8, 8, 6, 6,  6,20}; // ????? ?p????
  int DD[] = { 3, 4, 3, 3, 3, 3, 4, 4,  4, 3}; // ?????p????? ???????????????
 // ???????
  int Disp[] = { 50,100,170,40,180,120, 80, 80, 80, 10};
 // H???????? ?p??????
  int XT[][] =
   {{1,2,3,2},{1,2,1,2},{1,2,1,2},
    {1,2,3,1},{1,2,1,2},{1,2,1,2},
    {1,2,3,2},{1,1,2,1},{1,2,3,1},
    {1,2,3,1}};
  int YT[][] =
   {{4,4,4,3},{3,3,2,3},{3,3,2,3},
	{4,4,4,3},{4,4,2,3},{4,4,3,3},
	{4,4,4,3},{5,4,5,3},{6,5,4,3},
	{6,5,4,3}};
  double RT[][] =
   {{pi/6,pi/7,-pi/8,0},
    {pi/6,pi/7,-pi/9,pi/8},
    {pi/6,pi/7,-pi/9,0},
    {pi/6,pi/7,-pi/9,pi/7},
    {pi/6,pi/7,-pi/9,0},
    {pi/6,pi/7,-pi/9,0},
    {pi/6,pi/7,-pi/9,0},
    {pi/6,pi/7,-pi/9,0},
    {pi/6,pi/7,-pi/9,pi/7},
    {pi/6,pi/7,-pi/9,pi/7}};
 // ??????? ?p??????
  int TX[][] =
   {{1,2,3,2},{1,2,3,2},{1,2,1,2},
	{1,2,3,4},{1,2,3,2},{1,2,1,2},
	{1,2,3,2},{1,2,3,4},{1,2,1,2},
	{1,2,1,2}};
  int TY[][] =
   {{4,4,4,3},{2,3,1,3},{2,3,3,3},
	{5,5,5,5},{4,4,1,3},{4,3,2,2},
	{4,4,4,3},{5,5,5,5},{6,4,5,3},
	{6,4,5,3}};
  double TR[][] =
   {{1/DimAngle,-1/DimAngle,1/DimAngle,0},
	{1/DimAngle,-1/DimAngle,1/DimAngle,0},
	{1/DimAngle,-1/DimAngle,1/DimAngle,0},
	{1/DimAngle,-1/DimAngle,1/DimAngle,0},
	{1/DimAngle,-1/DimAngle,1/DimAngle,1/DimAngle},
	{1/DimAngle,-1/DimAngle,1/DimAngle,1/DimAngle},
	{1/DimAngle,-1/DimAngle,1/DimAngle,1/DimAngle},
	{1/DimAngle,-1/DimAngle,1/DimAngle,0},
	{1/DimAngle,-1/DimAngle,1/DimAngle,1/DimAngle},
	{1/DimAngle,-1/DimAngle,1/DimAngle,1/DimAngle}};
 // * ??p?????-3 *
  int T3[][] =
   {{ 1, 1, 1,-3},
	{ 1, 1,-3, 1},
	{ 1,-3, 1, 1},
	{-3, 1, 1, 1}};
  int LT3[][] = {{1,2},{1,3},{1,4},{2,3},{2,4},{3,4}};
  int PT3[][] = {{1,2,3},{1,3,4},{1,4,2},{2,4,3}};
 // * ?????-3 *
  int C3[][] =
   {{-1,-1,-1},{-1,-1, 1},
    {-1, 1,-1},{-1, 1, 1},
    { 1,-1,-1},{ 1,-1, 1},
    { 1, 1,-1},{ 1, 1, 1}};
  int LC3[][] =
   {{1,2},{1,3},{2,4},{3,4},
    {1,5},{2,6},{3,7},{4,8},
    {5,6},{5,7},{6,8},{7,8}};
  int PC3[][] =
   {{1,2,4,3},{1,5,6,2},{1,3,7,5},
    {3,4,8,7},{2,6,8,4},{5,7,8,6}};
 // * ??????p-3 *
  int O3[][] =
   {{ 1, 0, 0},{-1, 0, 0},
    { 0, 1, 0},{-0,-1, 0},
    { 0, 0, 1},{-0, 0,-1}};
  int LO3[][] =
   {{ 1, 3},{ 1, 4},{ 1, 5},{ 1, 6},
    { 2, 3},{ 2, 4},{ 2, 5},{ 2, 6},
    { 3, 5},{ 3, 6},{ 4, 5},{ 4, 6}};
  int PO3[][] =
   {{1,6,4},{1,4,5},{1,5,3},{2,5,4},
    {3,5,2},{2,4,6},{1,3,6},{2,6,3}};
 // * ??p?????-4 *
  int T4[][] =
   {{ 1, 1, 1, 1,-4},
    { 1, 1, 1,-4, 1},
    { 1, 1,-4, 1, 1},
    { 1,-4, 1, 1, 1},
    {-4, 1, 1, 1, 1}};
  int LT4[][] =
   {{1,2},{1,3},{2,3},{2,4},{3,4},
	{3,5},{4,5},{4,1},{5,1},{5,2}};
  int PT4[][] = {{1,2,3},{1,3,4},{1,4,2},{2,4,3}};
 // * ??????p-4 *
  int O4[][] =
   {{ 1, 0, 0, 0},{-1, 0, 0, 0},
    { 0, 1, 0, 0},{ 0,-1, 0, 0},
    { 0, 0, 1, 0},{ 0, 0,-1, 0},
    { 0, 0, 0, 1},{ 0, 0, 0,-1}};
  int LO4[][] =
   {{ 1, 3},{ 1, 4},{ 1, 5},{ 1, 6},{ 1, 7},{ 1, 8},
    { 2, 3},{ 2, 4},{ 2, 5},{ 2, 6},{ 2, 7},{ 2, 8},
    { 3, 5},{ 3, 6},{ 3, 7},{ 3, 8},{ 4, 5},{ 7, 6},
    { 4, 6},{ 4, 7},{ 4, 8},{ 5, 7},{ 5, 8},{ 6, 8}};
  int PO4[][] =
   {{1,6,4},{1,4,5},{1,5,3},{2,5,4},
    {3,5,2},{2,4,6},{1,3,6},{2,6,3}};
 // * 24-?????? *
  int L4[][] =
   {{ 1, 1, 0, 0},{ 1,-1, 0, 0},
	{ 1, 0, 1, 0},{ 1, 0,-1, 0},
	{ 1, 0, 0, 1},{ 1, 0, 0,-1},
	{-1, 1, 0, 0},{-1,-1, 0, 0},
	{-1, 0, 1, 0},{-1, 0,-1, 0},
	{-1, 0, 0, 1},{-1, 0, 0,-1},
	{ 0, 1, 1, 0},{ 0, 1,-1, 0},
	{ 0, 1, 0, 1},{ 0, 1, 0,-1},
	{ 0, 0, 1, 1},{ 0, 0, 1,-1},
	{ 0,-1, 1, 0},{ 0,-1,-1, 0},
	{ 0,-1, 0, 1},{ 0,-1, 0,-1},
	{ 0, 0,-1, 1},{ 0, 0,-1,-1}};
  int LL4[][] =
   {{ 1, 3},{ 1, 4},{ 1, 5},{ 1, 6},{ 1,13},
	{ 1,14},{ 1,15},{ 1,16},{ 2, 3},{ 2, 4},
	{ 2, 5},{ 2, 6},{ 2,19},{ 2,20},{ 2,21},
	{ 2,22},{ 3, 5},{ 3, 6},{ 3,13},{ 3,17},
	{ 3,18},{ 3,19},{ 4, 5},{ 4, 6},{ 4,14},
	{ 4,20},{ 4,23},{ 4,24},{ 5,15},{ 5,17},
	{ 5,21},{ 5,23},{ 6,16},{ 6,18},{ 6,22},
	{ 6,24},{ 7, 9},{ 7,10},{ 7,11},{ 7,12},
	{ 7,13},{ 7,14},{ 7,15},{ 7,16},{ 8, 9},
	{ 8,10},{ 8,11},{ 8,12},{ 8,19},{ 8,20},
	{ 8,21},{ 8,22},{ 9,11},{ 9,12},{ 9,13},
	{ 9,17},{ 9,18},{ 9,19},{10,11},{10,12},
	{10,14},{10,20},{10,23},{10,24},{11,15},
	{11,17},{11,21},{11,23},{12,16},{12,18},
	{12,22},{12,24},{13,15},{13,16},{13,17},
	{13,18},{14,15},{14,16},{14,23},{14,24},
	{15,17},{15,23},{16,18},{16,24},{17,19},
	{17,21},{18,19},{18,22},{19,21},{19,22},
	{20,21},{20,22},{20,23},{20,24},{21,23},
	{22,24}};
  int PL4[][] =
   {{1,6,4},{1,4,5},{1,5,3},{2,5,4},
	{3,5,2},{2,4,6},{1,3,6},{2,6,3}};
 // * ?????-4 *
  int C4[][] =
   {{-1,-1,-1,-1},{-1,-1, 1,-1},
	{-1, 1,-1,-1},{-1, 1, 1,-1},
	{ 1,-1,-1,-1},{ 1,-1, 1,-1},
	{ 1, 1,-1,-1},{ 1, 1, 1,-1},
	{-1,-1,-1, 1},{-1,-1, 1, 1},
	{-1, 1,-1, 1},{-1, 1, 1, 1},
	{ 1,-1,-1, 1},{ 1,-1, 1, 1},
	{ 1, 1,-1, 1},{ 1, 1, 1, 1}};
  int LC4[][] =
   {{ 1, 2},{ 1, 3},{ 2, 4},{ 3, 4},
	{ 1, 5},{ 2, 6},{ 3, 7},{ 4, 8},
	{ 5, 6},{ 5, 7},{ 6, 8},{ 7, 8},
	{ 1, 9},{ 2,10},{ 3,11},{ 4,12},
	{ 5,13},{ 6,14},{ 7,15},{ 8,16},
	{ 9,10},{ 9,11},{10,12},{11,12},
	{ 9,13},{10,14},{11,15},{12,16},
	{13,14},{13,15},{14,16},{15,16}};
  int PC4[][] =
   {{1,2,4,3},{1,5,6,2},{1,3,7,5},
    {3,4,8,7},{4,2,6,8},{5,7,8,6}};
 // * ?????-5 *
  int C5[][] =
   {{-1,-1,-1,-1,-1},{-1,-1,-1,-1, 1},
	{-1,-1,-1, 1,-1},{-1,-1,-1, 1, 1},
	{-1,-1, 1,-1,-1},{-1,-1, 1,-1, 1},
	{-1,-1, 1, 1,-1},{-1,-1, 1, 1, 1},
	{-1, 1,-1,-1,-1},{-1, 1,-1,-1, 1},
	{-1, 1,-1, 1,-1},{-1, 1,-1, 1, 1},
	{-1, 1, 1,-1,-1},{-1, 1, 1,-1, 1},
	{-1, 1, 1, 1,-1},{-1, 1, 1, 1, 1},
	{ 1,-1,-1,-1,-1},{ 1,-1,-1,-1, 1},
	{ 1,-1,-1, 1,-1},{ 1,-1,-1, 1, 1},
	{ 1,-1, 1,-1,-1},{ 1,-1, 1,-1, 1},
	{ 1,-1, 1, 1,-1},{ 1,-1, 1, 1, 1},
	{ 1, 1,-1,-1,-1},{ 1, 1,-1,-1, 1},
	{ 1, 1,-1, 1,-1},{ 1, 1,-1, 1, 1},
	{ 1, 1, 1,-1,-1},{ 1, 1, 1,-1, 1},
	{ 1, 1, 1, 1,-1},{ 1, 1, 1, 1, 1}};
  int LC5[][] =
   {{ 1, 2},{ 1, 3},{ 1, 5},{ 1, 9},{ 1,17},
	{ 2, 4},{ 2, 6},{ 2,10},{ 2,18},{ 3, 4},
	{ 3, 7},{ 3,11},{ 3,19},{ 4, 8},{ 4,12},
	{ 4,20},{ 5, 6},{ 5, 7},{ 5,13},{ 5,21},
	{ 6, 8},{ 6,14},{ 6,22},{ 7, 8},{ 7,15},
	{ 7,23},{ 8,16},{ 8,24},{ 9,10},{ 9,11},
	{ 9,13},{ 9,25},{10,12},{10,14},{10,26},
	{11,12},{11,15},{11,27},{12,16},{12,28},
	{13,14},{13,15},{13,29},{14,16},{14,30},
	{15,16},{15,31},{16,32},{17,18},{17,19},
	{17,21},{17,25},{18,20},{18,22},{18,26},
	{19,20},{19,23},{19,27},{20,24},{20,28},
	{21,22},{21,23},{21,29},{22,24},{22,30},
	{23,24},{23,31},{24,32},{25,26},{25,27},
	{25,29},{26,28},{26,30},{27,28},{27,31},
	{28,32},{29,30},{29,31},{30,32},{31,32}};
  int PC5[][] =
   {{1,2,4,3},{1,5,6,2},{1,3,7,5},
	{3,4,8,7},{2,6,8,4},{5,7,8,6}};
 // * ?????-6 *
  int C6[][] =
   {{-1,-1,-1,-1,-1,-1},{-1,-1,-1,-1,-1, 1},
	{-1,-1,-1,-1, 1,-1},{-1,-1,-1,-1, 1, 1},
	{-1,-1,-1, 1,-1,-1},{-1,-1,-1, 1,-1, 1},
	{-1,-1,-1, 1, 1,-1},{-1,-1,-1, 1, 1, 1},
	{-1,-1, 1,-1,-1,-1},{-1,-1, 1,-1,-1, 1},
	{-1,-1, 1,-1, 1,-1},{-1,-1, 1,-1, 1, 1},
	{-1,-1, 1, 1,-1,-1},{-1,-1, 1, 1,-1, 1},
	{-1,-1, 1, 1, 1,-1},{-1,-1, 1, 1, 1, 1},
	{-1, 1,-1,-1,-1,-1},{-1, 1,-1,-1,-1, 1},
	{-1, 1,-1,-1, 1,-1},{-1, 1,-1,-1, 1, 1},
	{-1, 1,-1, 1,-1,-1},{-1, 1,-1, 1,-1, 1},
	{-1, 1,-1, 1, 1,-1},{-1, 1,-1, 1, 1, 1},
	{-1, 1, 1,-1,-1,-1},{-1, 1, 1,-1,-1, 1},
	{-1, 1, 1,-1, 1,-1},{-1, 1, 1,-1, 1, 1},
	{-1, 1, 1, 1,-1,-1},{-1, 1, 1, 1,-1, 1},
	{-1, 1, 1, 1, 1,-1},{-1, 1, 1, 1, 1, 1},
	{ 1,-1,-1,-1,-1,-1},{ 1,-1,-1,-1,-1, 1},
	{ 1,-1,-1,-1, 1,-1},{ 1,-1,-1,-1, 1, 1},
	{ 1,-1,-1, 1,-1,-1},{ 1,-1,-1, 1,-1, 1},
	{ 1,-1,-1, 1, 1,-1},{ 1,-1,-1, 1, 1, 1},
	{ 1,-1, 1,-1,-1,-1},{ 1,-1, 1,-1,-1, 1},
	{ 1,-1, 1,-1, 1,-1},{ 1,-1, 1,-1, 1, 1},
	{ 1,-1, 1, 1,-1,-1},{ 1,-1, 1, 1,-1, 1},
	{ 1,-1, 1, 1, 1,-1},{ 1,-1, 1, 1, 1, 1},
	{ 1, 1,-1,-1,-1,-1},{ 1, 1,-1,-1,-1, 1},
	{ 1, 1,-1,-1, 1,-1},{ 1, 1,-1,-1, 1, 1},
	{ 1, 1,-1, 1,-1,-1},{ 1, 1,-1, 1,-1, 1},
	{ 1, 1,-1, 1, 1,-1},{ 1, 1,-1, 1, 1, 1},
	{ 1, 1, 1,-1,-1,-1},{ 1, 1, 1,-1,-1, 1},
	{ 1, 1, 1,-1, 1,-1},{ 1, 1, 1,-1, 1, 1},
	{ 1, 1, 1, 1,-1,-1},{ 1, 1, 1, 1,-1, 1},
	{ 1, 1, 1, 1, 1,-1},{ 1, 1, 1, 1, 1, 1}};
  int LC6[][] =
   {{ 1, 2},{ 1, 3},{ 1, 5},{ 1, 9},{ 1,17},{ 1,33},
	{ 2, 4},{ 2, 6},{ 2,10},{ 2,18},{ 2,34},{ 3, 4},
	{ 3, 7},{ 3,11},{ 3,19},{ 3,35},{ 4, 8},{ 4,12},
	{ 4,20},{ 4,36},{ 5, 6},{ 5, 7},{ 5,13},{ 5,21},
	{ 5,37},{ 6, 8},{ 6,14},{ 6,22},{ 6,38},{ 7, 8},
	{ 7,15},{ 7,23},{ 7,39},{ 8,16},{ 8,24},{ 8,40},
	{ 9,10},{ 9,11},{ 9,13},{ 9,25},{ 9,41},{10,12},
	{10,14},{10,26},{10,42},{11,12},{11,15},{11,27},
	{11,43},{12,16},{12,28},{12,44},{13,14},{13,15},
	{13,29},{13,45},{14,16},{14,30},{14,46},{15,16},
	{15,31},{15,47},{16,32},{16,48},{17,18},{17,19},
	{17,21},{17,25},{17,49},{18,20},{18,22},{18,26},
	{18,50},{19,20},{19,23},{19,27},{19,51},{20,24},
	{20,28},{20,52},{21,22},{21,23},{21,29},{21,53},
	{22,24},{22,30},{22,54},{23,24},{23,31},{23,55},
	{24,32},{24,56},{25,26},{25,27},{25,29},{25,57},
	{26,28},{26,30},{26,58},{27,28},{27,31},{27,59},
	{28,32},{28,60},{29,30},{29,31},{29,61},{30,32},
	{30,62},{31,32},{31,63},{32,64},{33,34},{33,35},
	{33,37},{33,41},{33,49},{34,36},{34,38},{34,42},
	{34,50},{35,36},{35,39},{35,43},{35,51},{36,40},
	{36,44},{36,52},{37,38},{37,39},{37,45},{37,53},
	{38,40},{38,46},{38,54},{39,40},{39,47},{39,55},
	{40,48},{40,56},{41,42},{41,43},{41,45},{41,57},
	{42,44},{42,46},{42,58},{43,44},{43,47},{43,59},
	{44,48},{44,60},{45,46},{45,47},{45,61},{46,48},
	{46,62},{47,48},{47,63},{48,64},{49,50},{49,51},
	{49,53},{49,57},{50,52},{50,54},{50,58},{51,52},
	{51,55},{51,59},{52,56},{52,60},{53,54},{53,55},
	{53,61},{54,56},{54,62},{55,56},{55,63},{56,64},
	{57,58},{57,59},{57,61},{58,60},{58,62},{59,60},
	{59,63},{60,64},{61,62},{61,63},{62,64},{63,64}};
  int PC6[][] =
   {{1,2,4,3},{1,5,6,2},{1,3,7,5},
	{8,7,3,4},{2,6,8,4},{5,7,8,6}};
 // * ???????? - 3 *
  int I3[][] = new int[20][3];
  int LI3[][] =
   {{ 1, 2},{ 1, 3},{ 1, 4},{ 1, 5},{ 1, 6},
	{ 6,11},{ 2,11},{ 2, 7},{ 7, 3},{ 3, 8},
	{ 8, 4},{ 4, 9},{ 9, 5},{ 5,10},{10, 6},
	{ 2, 3},{ 3, 4},{ 4, 5},{ 5, 6},{ 6, 2},
	{ 7, 8},{ 8, 9},{ 9,10},{10,11},{11, 7},
	{12, 7},{12, 8},{12, 9},{12,10},{12,11}};
  int PI3[][] =
   {{ 3, 2, 1},{ 4, 3, 1},{ 5, 4, 1},{ 6, 5, 1},{ 2, 6, 1},
	{ 6, 2,11},{11, 2, 7},{ 3, 7, 2},{ 7, 3, 8},{ 4, 8, 3},
	{ 8, 4, 9},{ 5, 9, 4},{ 9, 5,10},{ 6,10, 5},{10, 6,11},
	{12, 7, 8},{12, 8, 9},{12, 9,10},{12,10,11},{12,11, 7}};
 // ?????????? ????????? 3D
  void BuildI3(){
    int i; double Tau,r;
    Tau=(1+Math.sqrt(5))/2;
    r=Tau-0.5;
    for (i=0;i<5;i++){
      I3[i+1][0] = (int)(20*Math.cos(i*pi/2.5));
      I3[i+1][1] = (int)(20*Math.sin(i*pi/2.5));
      I3[i+1][2] = +10;
      I3[i+6][0] = (int)(20*Math.cos((i+0.5)*pi/2.5));
      I3[i+6][1] = (int)(20*Math.sin((i+0.5)*pi/2.5));
      I3[i+6][2] = -10;
    };
    I3[0][0]  = 0; I3[0][1]  = 0; I3[0][2] = (int)(20*r);
    I3[11][0] = 0; I3[11][1] = 0; I3[11][2] = -(int)(20*r);
  };
 // ??????? ???????
  void Rotate_Vect( int num ){
    int i,j;
    double Y[] = new double[MaxDimSpace];
    for (i=0;i<MaxDimSpace;i++) Y[i]=0;
      for (i=0;i<DimSpace;i++)
        for (j=0;j<DimSpace;j++)
     	  Y[i]=Y[i] + Rotate[i][j] * W[num][j];
    for (i=0;i<DimSpace;i++) W[num][i]=Y[i];
  }
 // ??????? ?????? ???????????? ???
  void Rotate_Comp( int n, int m, double a ){
/*    double ca,sa;
    int j,i;
    for (i=0;i<MaxDimSpace;i++)
      for (j=0;j<MaxDimSpace;j++)
  	Rotate[i][j]=0;
    for(i=0;i<DimSpace;i++) Rotate[i][i]=1;
    sa=Math.sin(2*pi*a); ca=Math.cos(2*pi*a);
    Rotate[n-1][n-1] = ca; Rotate[n-1][m-1] = -sa;
    Rotate[m-1][n-1] = sa; Rotate[m-1][m-1] = ca;
    for (i=0;i<DimNods;i++) Rotate_Vect(i);*/
  }
 // ???????? ??????????
  int XC( double x ){ return (int)(x0+Dx*x); };
  int YC( double y ){ return (int)(y0+Dy*y); };
 // ------
  int	   startAngle = 0;
  int	   endAngle = 45;
  boolean  filled = false;
  Font	   font; 
 // ??????? ?????
  void ShowLoops( int Ind,Graphics g ){
    int i,j;
    for (i=0;i<DimLoops;i++)
      for (j=0;j<2;j++)
  	switch (Ind) {
 	  case 0: Loops[i][j]=LT3[i][j]; break;
	  case 1: Loops[i][j]=LC3[i][j]; break;
	  case 2: Loops[i][j]=LO3[i][j]; break;
	  case 3: Loops[i][j]=LT4[i][j]; break;
	  case 4: Loops[i][j]=LO4[i][j]; break;
	  case 5: Loops[i][j]=LL4[i][j]; break;
	  case 6: Loops[i][j]=LC4[i][j]; break;
	  case 7: Loops[i][j]=LC5[i][j]; break;
	  case 8: Loops[i][j]=LC6[i][j]; break;
	  case 9: Loops[i][j]=LI3[i][j]; break;
        };
     g.setColor(Color.red);
     for (i=0;i<DimLoops;i++)
       g.drawLine( XC(W[Loops[i][0]-1][0]),YC(W[Loops[i][0]-1][1]),
                   XC(W[Loops[i][1]-1][0]),YC(W[Loops[i][1]-1][1]));
    Rectangle r = getBounds();
    int hlines = r.height / 10;
    int vlines = r.width / 10;
    g.setColor(Color.pink);
    for(i=1; i<=hlines; i++) g.drawLine(0, i * 10, r.width, i * 10);
    for(i=1; i<=vlines; i++) g.drawLine(i * 10, 0, i * 10, r.height);
    g.setColor(Color.red);
    if (filled){
      g.fillArc(0, 0, r.width - 1, r.height - 1, startAngle, endAngle);
    }else{
      g.drawArc(0, 0, r.width - 1, r.height - 1, startAngle, endAngle);
    }
    g.setColor(Color.black);
    g.setFont(font);
    g.drawLine(0, r.height / 2, r.width, r.height / 2);
    g.drawLine(r.width / 2, 0, r.width / 2, r.height);
    g.drawLine(0, 0, r.width, r.height);
    g.drawLine(r.width, 0, 0, r.height);
    int sx = 10;
    int sy = r.height - 28;
    g.drawString("S = " + startAngle, sx, sy); 
    g.drawString("E = " + endAngle, sx, sy + 14); 
  }
 // ??????? ?????
  void ShowNamb(Graphics g){
//	String st = "1";
//	for (int i=0;i<DimNods;i++){
//	  st[0]=Mask[i];
//	  outtextxy( XC(W[i][0])-10,YC(W[i][1])-8,st);
//	};
  }; /*
 // ????? - ??????????
  double Minor2( int p1,int p2 ){
    return W[p1-1][0]*W[p2-1][1]-W[p2-1][0]*W[p1-1][1];
  }
  double Area( int p1,int p2,int p3 ){
    return (Minor2(p2,p3)+Minor2(p3,p1)+Minor2(p1,p2))/2;
  }
 // ????? ????????????
  void ShowTr( int p1,int p2,int p3,Graphics g ){
	int Triangle[] =
	  { XC(W[p1-1][0]), YC(W[p1-1][1]),
	    XC(W[p2-1][0]), YC(W[p2-1][1]),
	    XC(W[p3-1][0]), YC(W[p3-1][1]) };
//	fillpoly(sizeof(Triangle)/sizeof(pointtype),Triangle);
  };
 // ????? ????????????????
  void ShowSq( int p1,int p2,int p3,int p4,Graphics g ){
	int Square[] =
	  { XC(W[p1-1][0]), YC(W[p1-1][1]), XC(W[p2-1][0]), YC(W[p2-1][1]),
     	    XC(W[p3-1][0]), YC(W[p3-1][1]), XC(W[p4-1][0]), YC(W[p4-1][1]) };
//	fillpoly(sizeof(Square)/sizeof(pointtype),Square);
  };*/
 // ????? ??????
  void ShowPlanes( int Ind,Graphics g ){
/*	int i,k; double r=0; double P[]=new double[MaxDimSpace];
	for (k=0;k<DimPlanes;k++){
	  switch (Ind){
		case 0: r=Area(PT3[k][0],PT3[k][1],PT3[k][2]); break;
		case 1: r=Area(PC3[k][0],PC3[k][1],PC3[k][2]); break;
		case 2: r=Area(PO3[k][0],PO3[k][1],PO3[k][2]); break;
		case 3: r=Area(PT4[k][0],PT4[k][1],PT4[k][2]); break;
		case 4: r=Area(PO4[k][0],PO4[k][1],PO4[k][2]); break;
		case 5: r=Area(PL4[k][0],PL4[k][1],PL4[k][2]); break;
		case 6: r=Area(PC4[k][0],PC4[k][1],PC4[k][2]); break;
		case 7: r=Area(PC5[k][0],PC5[k][1],PC5[k][2]); break;
		case 8: r=Area(PC6[k][0],PC6[k][1],PC6[k][2]); break;
		case 9: r=Area(PI3[k][0],PI3[k][1],PI3[k][2]); break;
	  };
	g.setColor(new Color(k*10,k*10,k*10));
	//  setfillstyle(1,(k%6)+1);
	  if (r>0)
		switch (Ind){
		  case 0: ShowTr(PT3[k][0],PT3[k][1],PT3[k][2],g); break;
		  case 1: ShowSq(PC3[k][0],PC3[k][1],PC3[k][2],PC3[k][3],g); break;
		  case 2: ShowTr(PO3[k][0],PO3[k][1],PO3[k][2],g); break;
		  case 3: ShowTr(PT4[k][0],PT4[k][1],PT4[k][2],g); break;
		  case 4: ShowTr(PO4[k][0],PO4[k][1],PO4[k][2],g); break;
		  case 5: ShowTr(PL4[k][0],PL4[k][1],PL4[k][2],g); break;
		  case 6: ShowSq(PC4[k][0],PC4[k][1],PC4[k][2],PC4[k][3],g); break;
		  case 7: ShowSq(PC5[k][0],PC5[k][1],PC5[k][2],PC5[k][3],g); break;
		  case 8: ShowSq(PC6[k][0],PC6[k][1],PC6[k][2],PC6[k][3],g); break;
		  case 9: ShowTr(PI3[k][0],PI3[k][1],PI3[k][2],g);
		};
	};*/
  };
 // ????????????? ?????? ??????
  void InitData( int Ind ){
    int i,j,Xasp,Yasp;
    Complex = Ind;
  // getaspectratio(&Xasp,&Yasp);
    Xasp = 100; Yasp = 100;
    DimSpace  = DS[Complex];
    DimNods   = DN[Complex];
    DimLoops  = DL[Complex];
    DimPlanes = DP[Complex];
    if(Complex==9) BuildI3();
    for(i=0;i<MaxDimNods;i++) for(j=0;j<MaxDimSpace;j++) W[i][j]=0;
    for (j=0;j<DimSpace;j++)
      for (i=0;i<DimNods;i++)
        switch (Complex){
          case 0: W[i][j]=T3[i][j]; break;
          case 1: W[i][j]=C3[i][j]; break;
          case 2: W[i][j]=O3[i][j]; break;
          case 3: W[i][j]=T4[i][j]; break;
          case 4: W[i][j]=O4[i][j]; break;
          case 5: W[i][j]=L4[i][j]; break;
          case 6: W[i][j]=C4[i][j]; break;
          case 7: W[i][j]=C5[i][j]; break;
          case 8: W[i][j]=C6[i][j]; break;
          case 9: W[i][j]=I3[i][j]; break;
        };
     Dx=Disp[Complex];
     Dy=((double)Xasp*Disp[Complex])/Yasp;
     for(i=0;i<4;i++) Rotate_Comp(XT[Complex][i],YT[Complex][i],RT[Complex][i]);
  };
/* // ????????
  void MakeMoveStep( int Complex,Graphics g ){
   //int Page; char Ch;
   // double Angle = 3/DimAngle;
   //Page=0; SetPages(Page,1-Page); while (!kbhit()){
    for (int n=0;n<4;n++)
      Rotate_Comp(TX[Complex][n],TY[Complex][n],TR[Complex][n]);
   //  clearviewport();
    ShowLoops(Complex,g); ShowPlanes(Complex,g); ShowNamb(g);
   //  Page=1-Page; SetPages(Page,1-Page);
   //};while(kbhit()) getch();
   //clearviewport();
  };*/
 // ???????? - ???? ???
  public void paint(Graphics g){
    for (int n=0;n<4;n++)
      Rotate_Comp(TX[Complex][n],TY[Complex][n],TR[Complex][n]);
    ShowLoops(Complex,g); ShowPlanes(Complex,g); ShowNamb(g);
  }

  public void redraw(boolean filled, int start, int end) {
    this.filled = filled;
    this.startAngle = start;
    this.endAngle = end; 
    repaint();
  }
}

// -= ?????? ?????????? =-
class controls extends Panel
                 implements ActionListener/*,
                            ItemListener*/{
  TextField s;
  TextField e;
  figure canvas;

  public controls(figure canvas) {
    this.canvas = canvas;
    add(s = new TextField("0", 4));
    add(e = new TextField("45", 4));
    Button b = null;
    b = new Button("Fill");
    b.addActionListener(this);
    add(b);
    b = new Button("Draw");
    b.addActionListener(this);
    add(b);
/*    Choice c = new Choice();
    c.addItem("one");
    c.addItem("two");
    c.addItem("three");
    c.addItem("four");
    c.addItem("five");
    c.addItem("six");
    c.addItemListener(this);
    add(c); */
  }

/*  public void itemStateChanged(ItemEvent e) {
    ((CardLayout)this.getLayout()).show(this,
                                         (String)(e.getItem()));
  } */

  public void actionPerformed(ActionEvent ev) {
    String label = ev.getActionCommand();
    canvas.redraw(label.equals("Fill"),
	          Integer.parseInt(s.getText().trim()),
	          Integer.parseInt(e.getText().trim()));
  }
}
	

