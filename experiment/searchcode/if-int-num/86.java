import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class synodos03 extends PApplet {

int ROW = 3;
int COL = 3;
int NUM = ROW * COL;
GenLogo gen[] = new GenLogo[NUM];
PImage frameImg;

public void setup() {
  size(600, 600, OPENGL);
  frameRate(15);
  colorMode(HSB, 360, 100, 100, 100);
  for (int j = 0; j < ROW; j++) {
    for (int i = 0; i < COL; i++) {
      gen[j * ROW + i] = new GenLogo(i * width/PApplet.parseFloat(COL), j * height/PApplet.parseFloat(ROW), width/PApplet.parseFloat(COL), height/PApplet.parseFloat(ROW));
    }
  }
}

public void draw() {
  background(0, 0, 100, 100);
  lights();
  ortho();
  for (int i = 0; i < NUM; i++) {
    gen[i].draw();
  }
}

public void mouseReleased() {
  for (int i = 0; i < NUM; i++) {
    gen[i].mouseReleased();
  }
}

class GenLogo {
  int NUM = 12;
  float left, top;
  float w, h;
  float[] rotX = new float[NUM];
  float[] rotY = new float[NUM];
  float[] rotZ = new float[NUM];
  float[] centorX = new float[NUM];
  float[] centorY = new float[NUM];
  float[] centorZ = new float[NUM];
  int[] col = new int[NUM];
  int baseHue;

  GenLogo(float _left, float _top, float _width, float _height) {
    left = _left;
    top = _top;
    w = _width;
    h = _height;
    setParams();
  }

  public void draw() {
    pushMatrix();
    translate(left + w/2.0f, top + w/2.0f);
    for(int i = 0; i < NUM; i++){
      pushMatrix();
      rotateX(rotX[i]);
      rotateY(rotY[i]);
      rotateZ(rotZ[i]);
      translate(centorX[i], centorY[i], centorZ[i]);
      fill(col[i]);
      //stroke(0,0,100,50);
      noStroke();
      //sphere(w/5 * pow(0.95, i));
      box(w/5 * pow(0.95f, i));
      popMatrix();
    }
    popMatrix();
  }

  public void mouseReleased() {
    setParams();
  }

  public void setParams() {
    baseHue = PApplet.parseInt(random(6)) * 60;
    for(int i = 0; i < NUM; i++){
      rotX[i] = radians(random(360));
      rotY[i] = radians(random(360));
      rotZ[i] = radians(random(360));
      centorX[i] = random(-w/6.0f, w/6.0f);
      centorY[i] = random(-w/6.0f, w/6.0f);
      centorZ[i] = random(-w/6.0f, w/6.0f);
      col[i] = color(baseHue + random(-40,40), 50, 100, 100);
    }
  }

};
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "synodos03" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

