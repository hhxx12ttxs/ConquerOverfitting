import processing.core.*; 
import processing.xml.*; 

import ddf.minim.*; 
import ddf.minim.signals.*; 
import ddf.minim.analysis.*; 
import ddf.minim.effects.*; 
import processing.serial.*; 
import controlP5.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class LightController extends PApplet {








final int HEAD_R = 0x55;
final int HEAD_G = 0x56;
final int HEAD_B = 0x57;

final int HEAD_ROW = 0x58;
final int HEAD_COL = 0x59;
final int HEAD_ALL = 0x60;
final int HEAD_DIA1 = 0x61;
final int HEAD_DIA2 = 0x62;
final int HEAD_MATRIX = 0x63;

final int H = 700;
final int W = 600;

final int nRows = 4;
final int nCols = 4;

final int nSlots = 16;
final int nRacks= 8;
final int slotPadd = 10;
final int slotHeight = 40;
final int slotWidth = ((W-(slotPadd*nSlots))  / nSlots);
int currentSlot = 0;
int activeSlot = 0;
int currentRack = 0;
int activeRack = 0;
Slot[][] slots = new Slot[nRacks][nSlots];

int[] matrix = new int[nRows*nCols];
Button[][] matrixPads = new Button[nRows][nCols];
PadListener padListener = new PadListener();
KnobListener knobListener = new KnobListener();

//Pitch 
boolean playing = false;
boolean audioSync = false;
final int framerate = 60;
int pitch = 180; 
int beatCounter = 0;


boolean[] keys = new boolean[526];

Serial myPort;  // Create object from Serial class
int val;        // Data received from the serial port
PImage bg;

Minim minim;    //Minim instance
BeatDetect beat; //Beatdetection algorithms
AudioInput in;  //Input from mic
FFT fft;        //Fast furier transform

ControlP5 controlP5;

// COLOR SETTER AREA Knobs
Knob knobR;
Knob knobG;
Knob knobB;
Textlabel lblKnobR;
Textlabel lblKnobG;
Textlabel lblKnobB;
Button resetR_btn;
Button resetG_btn;
Button resetB_btn;

// ALL Triggers
int allTrigger0_col = 0;
int allTrigger1_col = 0;
int allTrigger2_col = 0;
int allTrigger3_col = 0;
Button allTrigger0_btn;
Button allTrigger1_btn;
Button allTrigger2_btn;
Button allTrigger3_btn;


// ROW/COL AREA Knobs
Knob knobRow;
Knob knobCol;
Button knobRow_btn;
Button knobCol_btn;
int row = 0;
int col = 0;
Textlabel knobRow_lbl;
Textlabel knobCol_lbl;
int knobRow_col = 0;
int knobCol_col = 0;

// DIAGONAL AREA Knobs
Knob knobDia1;
Knob knobDia2;
Button knobDia1_btn;
Button knobDia2_btn;
int dia1_col = 0;
int dia2_col = 0;
Textlabel knobDia1_lbl;
Textlabel knobDia2_lbl;
int dia1 = 0;
int dia2 = 0;

Button btnMode0;
Button btnMode1;
Button btnMode2;
Button btnMode3;
Button btnManual;
Button btnAudio;

Button playBtn;
Button stopBtn;
Button audioSyncBtn;
Button activateRackBtn;

RadioButton racks;

float sliderR = 0;
float sliderG = 0;
float sliderB = 0;

public void setup() 
{
  frameRate(framerate);
  bg = loadImage("bg.jpg");
  size(W,H);
  smooth();
 
  //Minim init
  minim = new Minim(this);
  beat = new BeatDetect();
  
  //minim.debugOn();
  in = minim.getLineIn(Minim.STEREO,512);
  fft = new FFT(in.bufferSize(), in.sampleRate());
  
  //Open serial communication
  String portName = Serial.list()[1];
  myPort = new Serial(this, portName, 115200);
  
  controlP5 = new ControlP5(this);
  //controlP5.setControlFont(new ControlFont(createFont("Georgia",20), 20));
 
  // COLOR SETTER AREA
  knobR = controlP5.addKnob("knobR",0,1023,0,20,20,70);
  knobR.showTickMarks(true);
  lblKnobR = controlP5.addTextlabel("lblKnobR","RED",40,100);
  //resetR_btn = controlP5.addButton("ResetR",1,20,110,40,20);
  controlP5.controller("knobR").addListener(knobListener);
  
  knobG = controlP5.addKnob("knobG",0,1023,0,115,20,70);
  knobG.showTickMarks(true);
  lblKnobG = controlP5.addTextlabel("lblKnobG","GREEN",135,100);
  //resetG_btn = controlP5.addButton("ResetG",1,80,110,40,20);
  controlP5.controller("knobG").addListener(knobListener);
  
  knobB = controlP5.addKnob("knobB",0,1023,0,210,20,70);
  knobB.showTickMarks(true);
  lblKnobB = controlP5.addTextlabel("lblKnobB","BLUE",235,100);
  //resetB_btn = controlP5.addButton("ResetB",1,140,110,40,20);
  controlP5.controller("knobB").addListener(knobListener);
  
  // ALL TRIGGER AREA
  allTrigger0_btn = controlP5.addButton("All0",1,305,120,64,40);
  allTrigger1_btn = controlP5.addButton("All1",1,375,120,64,40);
  allTrigger2_btn = controlP5.addButton("All2",1,445,120,64,40);
  allTrigger3_btn = controlP5.addButton("All3",1,515,120,64,40);
  
  // COL/ROW AREA
  knobRow = controlP5.addKnob("knobRow",0,nRows-1,0,310,180,50);
  knobRow.showTickMarks(true);
  knobRow_btn = controlP5.addButton("Row",1,304,240,64,20);
  knobRow_lbl = controlP5.addTextlabel("knobRow_lbl","0",350,246);
  
  knobCol = controlP5.addKnob("knobCol",0,nCols-1,0,380,180,50);
  knobCol.showTickMarks(true);
  knobCol_btn = controlP5.addButton("Col",1,374,240,64,20);
  knobCol_lbl = controlP5.addTextlabel("knobCol_lbl","0",420,246);
  
  // DIAGONAL AREA
  knobDia1 = controlP5.addKnob("knobDia1",0,fact(nRows),0,450,180,50);
  knobDia1.showTickMarks(true);
  knobDia1_btn = controlP5.addButton("Dia1",1,444,240,64,20);
  knobDia1_lbl = controlP5.addTextlabel("knobDia1_lbl","0",490,246);
  
  knobDia2 = controlP5.addKnob("knobDia2",0,fact(nCols),0,520,180,50);
  knobDia2.showTickMarks(true);
  knobDia2_btn = controlP5.addButton("Dia2",1,514,240,64,20);
  knobDia2_lbl = controlP5.addTextlabel("knobDia2_lbl","0",570,246);
  
  // MATRIX SINGLES
   //Initialize slots
  //ControlGroup pads = new ControlGroup()
  for (int j=0;j<nCols;j++){
    for (int i=0;i<nRows;i++){
     if(j%2==1) matrixPads[j][i] = controlP5.addButton("pad"+((j*nRows)+i),(j*nRows)+i,305+(i*70),270+(j*70),64,64);
     else matrixPads[j][i] = controlP5.addButton("pad"+((j*nRows)+i),(j*nRows)+i,305+(nRows*70)-((i+1)*70),270+(j*70),64,64); 
     controlP5.controller("pad"+((j*nRows)+i)).addListener(padListener);
    }  
  }
  
  //Initialize slots
  for (int j=0;j<nRacks;j++){
    for (int i=0;i<nSlots;i++){
     slots[j][i] = new Slot(0,HEAD_ALL,null);
    }  
  }
  
  //Pitch control
  controlP5.addSlider("pitch",60,600,120,slotPadd,H - 2*slotPadd - slotHeight -20,100,20);
  playBtn = controlP5.addButton("play",0,180,H - 2*slotPadd - slotHeight -20,40,20);
  stopBtn = controlP5.addButton("stop",0,230,H - 2*slotPadd - slotHeight -20,40,20);
  audioSyncBtn = controlP5.addButton("sync",0,280,H - 2*slotPadd - slotHeight -20,40,20);
  audioSyncBtn.setSwitch(true);
  
  //racks
  racks = controlP5.addRadioButton("racks",330,H - 2*slotPadd - slotHeight -20);
  racks.setColorForeground(color(120));
  racks.setColorActive(color(255));
  racks.setColorLabel(color(255));
  racks.setItemsPerRow(nRacks);
  racks.setSpacingColumn(12);
  racks.setItemWidth(10);
  racks.setItemHeight(20);
  
  //Add racks
  for (int i=0;i<nRacks;i++){
    addToRadioButton(racks,Integer.toString(i),i);
  }
  
  racks.activate(0);
  
  activateRackBtn = controlP5.addButton("setRack",0,540,H - 2*slotPadd - slotHeight -20,40,20);
 
}

public void draw() {
  background(bg);
  
  //Draw color indicator PICKER
  fill(color(sb2rgb(sliderR),sb2rgb(sliderG),sb2rgb(sliderB)));
  rect(20,120,256,40);
  
  stroke(255);  
  //Draw color indicator ALL TRIGGERS
  /*fill(allTrigger0_col);
  rect(410,40,20,20);
  fill(allTrigger1_col);
  rect(410,70,20,20);
  fill(allTrigger2_col);
  rect(410,100,20,20);
  fill(allTrigger3_col);
  rect(410,130,20,20);
  
  //Draw color indicator CROW/COL 
  fill(knobRow_col);
  rect(70,210,20,20);
  fill(knobCol_col);
  rect(150,210,20,20);
  
  //Draw color indicator DIAGONAL
  fill(dia1_col);
  rect(70,300,20,20);
  fill(dia2_col);
  rect(150,300,20,20);*/
  
  stroke(255);
  
  // Audio snc area
  fft.forward(in.mix);
  beat.detect(in.mix);
  if (beat.isOnset()) stroke(255,0,0);
  else stroke(255);
  fill(0);
  rect(20,340,fft.specSize(),200);
  for (int i = 0; i < fft.specSize(); i++)
  {
     // draw the line for frequency band i, 
     // scaling it by 4 so we can see it a bit better
     line(20+i, 540, 20+i, 540 - fft.getBand(i));
  } 
  float lowA = fft.getFreq(400); //400Hz    
  float highA = fft.getFreq(2000); //2000Hz
  //println("LOW: "+lowA+" HIGH: "+highA);  
  
  
  //Draw slots
  for (int i=0;i<nSlots;i++){
    //Draw slots
    //println(currentRack+" "+i+slots[currentRack][i]);
    //if (slots[currentRack][i]!=null){
      fill(slots[currentRack][i].col);
      if (i == currentSlot) strokeWeight(4);    
      if (i == activeSlot) stroke(255,0,0);
      rect(slotPadd + i*(slotPadd + slotWidth),H - slotPadd - slotHeight,slotWidth,slotHeight);
      strokeWeight(1);
      stroke(255);  
   // }
    
  }
  
  //if playing than loop
  if (playing){
    beatCounter++;
    //println(beatCounter+"/"+(framerate / (pitch/framerate)));
    if (beatCounter >= (framerate / (pitch/framerate))){
      activeSlot = ((activeSlot+1) % (nSlots));
      beatCounter = 0;
      slots[activeRack][activeSlot].trigger();
    }  
  }
  
  if (playing && audioSync && beat.isOnset()){
    activeSlot = ((activeSlot+1) % (nSlots));
    beatCounter = 0;
    slots[activeRack][activeSlot].trigger();
  }
  
}

// COLOR PICKER

public void ResetR(int theValue) {
  knobR(0);
}

public void ResetG(int theValue) {
  knobG(0);
}

public void ResetB(int theValue) {
  knobB(0);
}

public void knobR(float r) {
  sliderR = r;
  //knobR.setValue(r);
}

public void knobG(float g) {
  sliderG = g;
  //knobG.setValue(g);
}

public void knobB(float b) {
  sliderB = b;
  //knobB.setValue(b);
}

public void pitch(float value) {
  pitch = (int)value;
}

public void play(int theValue){
  playing = true;
}

public void stop(int theValue){
  playing = false;
}

public void sync(int theValue){
  audioSync = !audioSyncBtn.booleanValue();
  playing = true;
}

public void addToRadioButton(RadioButton theRadioButton, String theName, int theValue ) {
  Toggle t = theRadioButton.addItem(theName,theValue);
  t.captionLabel().setColorBackground(color(80));
  t.captionLabel().style().movePadding(2,0,-1,2);
  t.captionLabel().style().moveMargin(-2,0,0,-3);
  t.captionLabel().style().backgroundWidth = 10;
  t.captionLabel().style().backgroundHeight = 20;
}

public void setRack(int theValue){
  activeRack = currentRack;
}

public void controlEvent(ControlEvent theEvent) {
  if (theEvent.group()!=null){
     if (theEvent.group().name().equals("racks")){
        currentRack = (int)theEvent.group().value();
     }
     //println(theEvent.id());
  }
  
}

// ALL SETTERS
public void All0(int theValue) {
  if(keyPressed) {
    if (checkKey(KeyEvent.VK_SPACE)) {
      allTrigger0_col = color(sb2rgb(sliderR),sb2rgb(sliderG),sb2rgb(sliderB));
      allTrigger0_btn.setColorBackground(allTrigger0_col);
    }else if (checkKey(KeyEvent.VK_S)) {
      slots[currentRack][currentSlot] = new Slot(allTrigger0_col,HEAD_ALL,null);
      currentSlot = ((currentSlot+1) % (nSlots));
    }
  }else{
    //transferAllChannelsColor(0);    
    transferAllChannelsColor(allTrigger0_col);    
  }
}

public void All1(int theValue) {
  if(keyPressed) {
    if (key == ' ') {
      allTrigger1_col = color(sb2rgb(sliderR),sb2rgb(sliderG),sb2rgb(sliderB));
      allTrigger1_btn.setColorBackground(allTrigger1_col);
    }else if (checkKey(KeyEvent.VK_S)) {
      slots[currentRack][currentSlot] = new Slot(allTrigger1_col,HEAD_ALL,null);
      currentSlot = ((currentSlot+1) % (nSlots));
    }
  }else{
    //transferAllChannelsColor(0);
    transferAllChannelsColor(allTrigger1_col);
  }
}

public void All2(int theValue) {
  if(keyPressed) {
    if (key == ' ') {
      allTrigger2_col = color(sb2rgb(sliderR),sb2rgb(sliderG),sb2rgb(sliderB));
      allTrigger2_btn.setColorBackground(allTrigger2_col);
    }else if (checkKey(KeyEvent.VK_S)) {
      slots[currentRack][currentSlot] = new Slot(allTrigger2_col,HEAD_ALL,null);
      currentSlot = ((currentSlot+1) % (nSlots));
    }
  }else{
    //transferAllChannelsColor(0);
    transferAllChannelsColor(allTrigger2_col);    
  }
}

public void All3(int theValue) {
  if(keyPressed) {
    if (key == ' ') {
      allTrigger3_col = color(sb2rgb(sliderR),sb2rgb(sliderG),sb2rgb(sliderB));
      allTrigger3_btn.setColorBackground(allTrigger3_col);
    }else if (checkKey(KeyEvent.VK_S)) {
      slots[currentRack][currentSlot] = new Slot(allTrigger3_col,HEAD_ALL,null);
      currentSlot = ((currentSlot+1) % (nSlots));
    }
  }else{
    transferAllChannelsColor(0);
    transferAllChannelsColor(allTrigger3_col);    
  }
}

// ROW/COL AREA
public void knobRow(float r) {
  row = (int)r;
  knobRow_lbl.setValue(Integer.toString(row));
  transferRowColor(row,knobRow_col);
}

public void knobCol(float c) {
  col = (int)c;
  knobCol_lbl.setValue(Integer.toString(col));
  transferColColor(col,knobCol_col);
}

public void Row(int theValue) {
  if(keyPressed) {
    if (key == ' ') {
      knobRow_col = color(sb2rgb(sliderR),sb2rgb(sliderG),sb2rgb(sliderB));
      knobRow_btn.setColorBackground(knobRow_col);
    }else if (checkKey(KeyEvent.VK_S)) {
      ArrayList params = new ArrayList();
      params.add(row);
      slots[currentRack][currentSlot] = new Slot(knobRow_col,HEAD_ROW,params);
      currentSlot = ((currentSlot+1) % (nSlots));
    }
  }else{
    //transferAllChannelsColor(0);
    transferRowColor(row,knobRow_col);
  }
}

public void Col(int theValue) {
  if(keyPressed) {
    if (key == ' ') {
      knobCol_col = color(sb2rgb(sliderR),sb2rgb(sliderG),sb2rgb(sliderB));
      knobCol_btn.setColorBackground(knobCol_col);
    }else if (checkKey(KeyEvent.VK_S)) {
      ArrayList params = new ArrayList();
      params.add(col);
      slots[currentRack][currentSlot] = new Slot(knobCol_col,HEAD_COL,params);
      currentSlot = ((currentSlot+1) % (nSlots));
    }
  }else{
    //transferAllChannelsColor(0);
    transferColColor(col,knobCol_col);
  }
}


// DIAGONAL AREA
public void knobDia1(float r) {
  dia1 = (int)r;
  knobDia1_lbl.setValue(Integer.toString(dia1));
}

public void knobDia2(float c) {
  dia2 = (int)c;
  knobDia2_lbl.setValue(Integer.toString(dia2));
}

public void Dia1(int theValue) {
  if(keyPressed) {
    if (key == ' ') {
      dia1_col = color(sb2rgb(sliderR),sb2rgb(sliderG),sb2rgb(sliderB));
    }else if (checkKey(KeyEvent.VK_S)) {
      slots[currentRack][currentSlot] = new Slot(dia1_col,HEAD_DIA1,null);
      currentSlot = ((currentSlot+1) % (nSlots));
    }
  }else{
    transferAllChannelsColor(0);
    //Transfer Row
  }
}

public void Dia2(int theValue) {
  if(keyPressed) {
    if (key == ' ') {
      dia2_col = color(sb2rgb(sliderR),sb2rgb(sliderG),sb2rgb(sliderB));
    }else if (checkKey(KeyEvent.VK_S)) {
      slots[currentRack][currentSlot] = new Slot(dia2_col,HEAD_DIA2,null);
      currentSlot = ((currentSlot+1) % (nSlots));
    }
  }else{
    transferAllChannelsColor(0);
    //Transfer Row
  }
}

public int sb2rgb(float v){
  float d = v/1023;
  return PApplet.parseInt(255*d);
}



// FUNCTIONS TO TRANSFER VALUES TO MATRIX
public void transferColor(int head){
  myPort.write(head);
  if (head==HEAD_R){
     myPort.write(sb2rgb(sliderR)); 
  }else if (head==HEAD_G){
     myPort.write(sb2rgb(sliderG));    
  }else if (head==HEAD_B){
     myPort.write(sb2rgb(sliderB));    
  }
  println("R "+sb2rgb(sliderR)+" G "+sb2rgb(sliderG)+" B "+sb2rgb(sliderB));
}

// Transfer a color 
public void transferAllChannelsColor(int colorToTransfer){
  //RED
  myPort.write(HEAD_R);
  myPort.write(sb2rgb( (colorToTransfer >> 16) & 0xFF )); 
  myPort.write(HEAD_G);
  myPort.write(sb2rgb( (colorToTransfer >> 8) & 0xFF )); 
  myPort.write(HEAD_B);
  myPort.write(sb2rgb( (colorToTransfer) & 0xFF )); 
  myPort.write(HEAD_ALL);
}

// Transfer a row 
public void transferRowColor(int row,int colorToTransfer){
  //RED
  myPort.write(HEAD_R);
  myPort.write(sb2rgb( (colorToTransfer >> 16) & 0xFF )); 
  myPort.write(HEAD_G);
  myPort.write(sb2rgb( (colorToTransfer >> 8) & 0xFF )); 
  myPort.write(HEAD_B);
  myPort.write(sb2rgb( (colorToTransfer) & 0xFF )); 
  myPort.write(HEAD_ROW);
  myPort.write(row); 
}

// Transfer a col 
public void transferColColor(int col,int colorToTransfer){
  //RED
  myPort.write(HEAD_R);
  myPort.write(sb2rgb( (colorToTransfer >> 16) & 0xFF )); 
  myPort.write(HEAD_G);
  myPort.write(sb2rgb( (colorToTransfer >> 8) & 0xFF )); 
  myPort.write(HEAD_B);
  myPort.write(sb2rgb( (colorToTransfer) & 0xFF )); 
  myPort.write(HEAD_COL);
  myPort.write(col); 
}

// Transfer the matrix
public void transferMatrix(){
  //RED
  myPort.write(HEAD_R);
  myPort.write(0); 
  myPort.write(HEAD_G);
  myPort.write(0); 
  myPort.write(HEAD_B);
  myPort.write(0); 
  myPort.write(HEAD_MATRIX);
  for (int i=0;i<matrix.length;i++){
    int col = matrix[i];
    myPort.write(sb2rgb( (col >> 16) & 0xFF )); 
    myPort.write(sb2rgb( (col >> 8) & 0xFF ));
    myPort.write(sb2rgb( (col) & 0xFF )); 
  }
  
}

// KEY HANDLING

public boolean checkKey(int k)
{
  if (keys.length >= k) {
    return keys[k];  
  }
  return false;
}

public void keyPressed()
{ 
  keys[keyCode] = true;
  println(KeyEvent.getKeyText(keyCode));
  if (checkKey(KeyEvent.VK_RIGHT) && currentSlot < nSlots) currentSlot++;
  else if (checkKey(KeyEvent.VK_LEFT) && currentSlot > 0) currentSlot--;
  else if (checkKey(KeyEvent.VK_UP) && currentRack < nRacks-1){
     currentRack++;
     racks.activate(currentRack);
  }
  else if (checkKey(KeyEvent.VK_DOWN) && currentRack > 0){
    currentRack--;
    racks.activate(currentRack);
  }else if (checkKey(KeyEvent.VK_G)){
    storeRackInfo();
  }else if (checkKey(KeyEvent.VK_H)){
    loadRackInfo();
  }
  
}
 
public void keyReleased()
{ 
  keys[keyCode] = false; 
}

// UTILITY METHODS
public int fact(int n) {
 if (n <= 1) {
   return 1;
 }
 else {
   return n * fact(n-1);
 }
} 

//Store rack information to XML

public void storeRackInfo(){
  
  XMLElement toStore= new XMLElement();
  toStore.setName("racks");
  toStore.setAttribute( "nCols",Integer.toString(nCols));
  toStore.setAttribute( "nRows",Integer.toString(nRows));  
  //toStore.setContent("inhalt");
  
  for (int i=0;i<nRacks;i++){
    
    XMLElement rack = new XMLElement();
    rack.setName("rack");
    rack.setAttribute( "id",Integer.toString(i));
    
    for (int j=0;j<nSlots;j++){
      
      Slot s = slots[i][j];
      
      XMLElement item = new XMLElement();
      item.setName("item");
      item.setAttribute( "col",Integer.toString(s.col));
      item.setAttribute( "type",Integer.toString(s.type));
      
      String params = "";
      if (s.params!=null){
        for (int k=0;k<s.params.size();k++){
          params += Integer.toString((Integer)s.params.get(k));
          params += ",";
          item.setAttribute( "params",params);
        }  
      }
      
      rack.addChild(item);
    
    }
    
    toStore.addChild(rack);
    
  }
  
  PrintWriter xmlfile;
  xmlfile = createWriter("racks.xml");
  
  //xml schreiben
  try
  {
    XMLWriter schreibXML = new XMLWriter(xmlfile) ;
  
    schreibXML.write(toStore);
  
    xmlfile.flush();
    xmlfile.close();
  }
  catch (IOException e)
  {
    e.printStackTrace();
  } 
}

public void loadRackInfo(){
  
  XMLElement toLoad= new XMLElement(this,"racks.xml");
  int nCols = toLoad.getIntAttribute("nCols");
  int nRows = toLoad.getIntAttribute("nRows");

  XMLElement[] racks = toLoad.getChildren();
  
  for (int i=0;i<racks.length;i++){
    
    XMLElement rack = racks[i];
    int id = rack.getIntAttribute("id");
    
    XMLElement[] items = rack.getChildren();
    
    for (int j=0;j<items.length;j++){
      
      XMLElement item = items[j];
      
      int col = item.getIntAttribute("col");
      int type = item.getIntAttribute("type");
      ArrayList params = new ArrayList();   
      String pAttr = item.getStringAttribute("params");
      
      if (pAttr!=null){
        String[] l = split(pAttr, ',');
      
        for (int k=0;k<l.length;k++){
          params.add(PApplet.parseInt(l[k]));       
        }  
      }      
      
      slots[i][j] = new Slot(col,type,params);    
    }
  }
}

class PadListener implements ControlListener {
  int index;
  public void controlEvent(ControlEvent theEvent) {
    println("i got an event from mySlider, " +
            "changing background color to "+
            theEvent.controller().value());
    index = (int)theEvent.controller().value();
    if(keyPressed) {
      if (key == ' ') {
        matrix[index] = color(sb2rgb(sliderR),sb2rgb(sliderG),sb2rgb(sliderB));
        theEvent.controller().setColorBackground(matrix[index]);
        transferMatrix();    
      }else if (checkKey(KeyEvent.VK_S)) {
        ArrayList params = new ArrayList(); 
        for (int i=0;i<matrix.length;i++){
          params.add(matrix[i]);
        }
        slots[currentRack][currentSlot] = new Slot(matrix[index],HEAD_MATRIX,params);
        currentSlot = ((currentSlot+1) % (nSlots));
      }else if (checkKey(KeyEvent.VK_R)) {
        matrix[index] = 0;
        theEvent.controller().setColorBackground(color(004762));
        transferMatrix();    
      }
    }else{
      transferMatrix();    
    }
  }
}

class KnobListener implements ControlListener {
  int index;
  public void controlEvent(ControlEvent theEvent) {
    if(keyPressed) {
      if (checkKey(KeyEvent.VK_R)) {
        if (theEvent.controller().name().equals("knobR")){
           knobR.setValue(0);
           sliderR = 0;
        }else if (theEvent.controller().name().equals("knobG")){
           knobG.setValue(0);
           sliderG = 0;
        }else if (theEvent.controller().name().equals("knobB")){
           knobB.setValue(0);
           sliderB = 0;
        }
      }
    }
  }
}

//Slot

/*
 * Class used to store the different operations that the slots will contain.
 */
class Slot { 
  
  int col;
  int type;
  ArrayList params;
  
  Slot (int c,int t,ArrayList p) {  
    col = c;
    type = t;
    params = p;
  } 
  
  public void trigger() { 
    switch (type){
      //all
      case HEAD_ALL:  
      transferAllChannelsColor(col);  
      break;
      //col
      case HEAD_COL:
      transferColColor((Integer)params.get(0),col);
      break;
      //row
      case HEAD_ROW:  
      transferRowColor((Integer)params.get(0),col);
      break;
      //diag1
      case HEAD_DIA1:  
      break;
      //diag2
      case HEAD_DIA2:  
      break;
      //matrix
      case HEAD_MATRIX: 
      for (int i=0;i<nCols*nRows-1;i++){
        matrix[i] = (Integer)params.get(i);
      }
      transferMatrix(); 
      break;
    }
  } 
}




  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#FFFFFF", "LightController" });
  }
}

