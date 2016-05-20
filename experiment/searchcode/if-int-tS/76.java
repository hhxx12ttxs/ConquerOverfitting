package fracdraw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

/**
 * Trida pro praci s zelvi grafikou
 * Umoznuje vykreslovani, obarvovani a zoomovani L-Systemu
 *
 * @author craw
 */
public class TurtleMachine
{
  /* Ovladaci znaky zelvy */
  public char TURTLE_LEFT = '-';
  public char TURTLE_RIGHT = '+';
  public char TURTLE_STACKPUSH = '[';
  public char TURTLE_STACKPOP = ']';
  public char TURTLE_RANDOM = '?';
  public char TURTLE_TURNAROUND = '|';
  /* ******************* */

  /**
   * Kresli konce bodu
   */
  public boolean drawLinePoints = false;
  /**
   * Generuje barevne cary
   */
  public boolean drawRandomColors = false;
  /**
   * Vykresli obdelnik kolem fraktalu
   */
  public boolean drawOutSideSquare = true;

  /**
   * Pokud je true, vykresli fraktal barevne, podle Craw Color Algorithm(R)
   * Musi byt zapnuto uz pri buildu fraktalu
   */
  public boolean drawPalleteColor = false;
  /**
   * Pokud je <i>drawPalleteColor</i> zapnuto, zalezi na tehle promene jakym zpusobem se bude obarvovat
   */
  public boolean useSmartAlgorithm = false;
  /**
   * Pokud je true, je pouzita linearni interpolace
   */
  public boolean useLinearInterpolation = false;


  private Random rGen = new Random();

  /**
   * Tato promenna obsahuje velikost vygenerovaneho fraktalu
   * Tedy Minimalni a maximalni hodnotu sirky a vysky
   */
  private TurtleImage timage = new TurtleImage();

  /**
   * Uchova posledni krok zelvy pri stavbe fraktalu
   */
  private TurtleStep  lastStep = new TurtleStep();

  /**
   *  Obsahuje verzi vykreslovaciho jadra TurtleMachine<br>
   *  Format verze je:<br>
   *  <br>
   *  <i>float int string</i>
   *  <br>
   *  kde <b>float</b> znamena cislo verze<br>
   *  <b>int</b> dodatecne cislo (revize)<br>
   *  <b>string</b> jmeno jadra<br>
   *  <br>
   *  @see getVersionName()
   *  @see getVersionNumber()
   *
   */
  public final static String TURTLE_VERSION = "0.8 5 DragonTurtle";
  /**
   * Urcuje velikost kroku zelvy v px, zmena v nastaveni
   */
  public int stepSize = 400;

  public int lineWidth = 1;
  /**
   * Urcuje pocatecni rotaci vsech "kroku" zelvy, zmena v nastaveni
   */
  public double initialRotation = 0;

  protected Color backColor = Color.BLACK;
  protected Color lineColor = Color.WHITE;
  protected Color linePointColor = Color.ORANGE;
  protected Color startPointColor = Color.GREEN;
  protected Color squareColor = new Color(25,25,25);

  /**
   * Real Zoomer , konstanta pro realne zoomovani (1:1) atp.
   */
  protected float zoomConst = (float) 0.01;

  /**
   * ArrayList obsahujici pozice X a Y pro vykreslovani car L-Systemu
   *
   * @see buildFractal(String lSystem)
   */
  private ArrayList<TurtleStep> fraktal = new ArrayList<TurtleStep>();

  /**
   * Obsahuje barvy urcene k obarveni fraktalu
   * 
   * @see drawPalleteColor
   */
  protected ColorStack cs = new ColorStack();

  public TurtleMachine()
  {
    //loadColorStack(3);
    //cs.loadFromFile("lesni_smes.txt");
  }

  /**
   * Vrati jmeno Zelviho vykreslovaciho enginu
   * @return jmeno soucasne verze TurtleMachine
   */
  public static String getVersionName()
  {
    String[] a = TURTLE_VERSION.split(" ");
    return a[2];
  }

  /**
   * Vrati verzi zelviho vykreslovaciho enginu
   * @return verze soucasneho TurtleMachine
   */
  public static float getVersionNumber()
  {
    String[] a = TURTLE_VERSION.split(" ");
    return Float.valueOf(a[0]);
  }

  /**
   * Vrati pocet car ve fraktalu
   */
  public int getLinesNumber()
  {
    return fraktal.size();
  }

  /**
   * Metoda prida polozku do ArrayListu fraktal
   *
   * @param X
   * @param Y
   * @param angle Soucasny uhel natoceni zelvy
   */
  public void addLine(double sX, double sY, double eX, double eY, double angle)
  {
    fraktal.add(new TurtleStep(sX, sY, eX, eY, angle));
  }

  /**
   * Obarvi fraktal metodou moji :)
   */
  public void colorizeFractal()
  {
    int actuallColor = 0;
    int colorStep = getColorStepValue();

    for (int i = 1; i <= fraktal.size(); i++) {
      TurtleStep tts = fraktal.get(i-1);

      tts.color = actuallColor;


      if ((i) % (colorStep) == 0 && (actuallColor+1) < cs.barvy.size())
      {
        actuallColor++;
      }

    }
  }

  public synchronized ArrayList<TurtleStep> getFractalPositions()
  {
    return fraktal;
  }

  /**
   * Obarvi fraktal metodou linearni interpolace :)
   */
  public void colorizeFractalUsingInterpolation()
  {
    int actuallColor = 1;
    int colorStep = getColorStepValue();

    if (cs.barvy.size() < 2)
      return;

    Color c1 = cs.barvy.get(1), c2 = cs.barvy.get(0);

    for (int i = 1; i <= fraktal.size(); i++) {
      TurtleStep tts;
      tts = fraktal.get(i - 1);

      tts.color = linearInterpolation(c1, c2, (double)(i % colorStep*2)/(colorStep * 2));

      if ((i) % (colorStep*2) == 0 && (actuallColor+1) < cs.barvy.size())
      {
        actuallColor++;
          c1 = cs.barvy.get(actuallColor-1);
          c2 = cs.barvy.get(actuallColor);
      }

    }
  }

  /**
   * Obarvi fraktal nahodne
   */
  public void randomlyColorizeFractal()
  {
    for (int i = 1; i <= fraktal.size(); i++) {
      TurtleStep tts = fraktal.get(i-1);
      tts.color = rGen.nextInt(cs.barvy.size());
    }
  }

  /**
   * Provede linearni interpolaci mezi dvemi barvami
   * @param c1 Barva 1
   * @param c2 Barva 2
   * @param step Procentualni hodnota interpolace
   * @return RGB interpolovane barvy (alpha je odstranena)
   */
  public int linearInterpolation(Color c1, Color c2, double step)
  {

    Color gradient;
    int newR = (int) (c1.getRed() + step * (c2.getRed() - c1.getRed()));
    int newG = (int) (c1.getGreen() + step * (c2.getGreen() - c1.getGreen()));
    int newB = (int) (c1.getBlue() + step * (c2.getBlue() - c1.getBlue()));

    //System.out.println(newR + ";" + newG + ";" + newB);

    gradient = new Color(newR, newG, newB);
    
    return gradient.getRGB();
  }

  public TurtleStep getLastCoordinates()
  {
    return lastStep;
  }

  /**
   * Vrati cislo urcujici kolik primek se ma obarvit jednou barvou
   *
   * @return pocet primek pro jednotlive barvy ze seznamu
   */
  private int getColorStepValue()
  {
    if (cs.barvy.size() <= 1)
    {
      return fraktal.size() + 1;
    }
    else
    {
      int p = (int) (fraktal.size() / (float) (cs.barvy.size()) );
      if (p <= 0)
        return fraktal.size();
      else
        return p;
    }
  }

  /**
   * Vrati tImage jako ctyrrozmerne pole intu
   *
   * [0] = minWidth
   * [1] = maxWidth
   * [2] = minHeight
   * [3] = maxHeight
   *
   * @return hodnoty obsazene v tImage
   * @see TurtleImage
   */
  public int[] getFractalSize()
  {
    int[] p = new int[4];

    p[0] = timage.minWidth;
    p[1] = timage.maxWidth;
    p[2] = timage.minHeight;
    p[3] = timage.maxHeight;

    return p;
  }

  public synchronized void clearFractal()
  {
    fraktal.clear();
  }

  /**
   * Metoda nejdrive vymaze ArrayList fraktal a pak jej naplni novymi pozicemi pro kresleni car.
   * Take nastavi promenou timage, ktera obsahuje maximalni a minimalni vysku a sirku vykreslovaneho fraktalu -
   * to je vhodne pro vypocitani zoomu na cely fraktal.
   *
   * @param lSystem L-System ze ktereho chceme zjistit pozice, jmeno, etc..
   *
   * @see fraktal
   * @see TurtleImage
   */
  public synchronized void buildFractal(LSystem lSystem)
  {
    if (lSystem == null)
    {
      return;
    }

    long startTimeL = 0;
    Stack<TurtleStep> tStack = new Stack<TurtleStep>();
    clearFractal();
    timage = new TurtleImage();

    Main.debugOut("Building of fractal has been started. L-System lenght: " + lSystem.buffer.length() + " characters.");

    startTimeL = System.nanoTime();


    double angle = initialRotation;
    double X=0, Y=0;
    double X2=-1, Y2=-1;

    for (int i=0; i < lSystem.buffer.length(); i++)
    {
      char c = lSystem.buffer.charAt(i);

      if (c == TURTLE_LEFT)
      {
        angle = normalizeAngle(angle + lSystem.buildAngleLeft);
      }
      else if (c == TURTLE_RIGHT)
      {
        angle = normalizeAngle(angle - lSystem.buildAngleRight);
      }
      else if (c == TURTLE_TURNAROUND)
      {
        angle = normalizeAngle(angle + 180);
      }
      else if (c == TURTLE_STACKPUSH)
      {
        tStack.push(new TurtleStep(X, Y, -1, -1, angle));
      }
      else if (c == TURTLE_STACKPOP)
      {
        if(!tStack.isEmpty())
          {
            TurtleStep ts = tStack.pop();
            X = ts.sX;
            Y = ts.sY;
            angle = ts.angle;
          }

      }
      else if (lSystem.moveChars.containsKey(new Character(c)))
      {
        X2 = Y2 = -1;

        switch(lSystem.moveChars.get(new Character(c)))
        {
          case LSystem.TURTLE_FORWARD:

            X2 = X + stepSize * Math.cos(Math.toRadians(angle));
            Y2 = Y - stepSize * Math.sin(Math.toRadians(angle));

            addLine(X, Y, X2, Y2, angle);

            break;
          case LSystem.TURTLE_BACK:

            X2 = X + stepSize * Math.cos(Math.toRadians(angle+180));
            Y2 = Y - stepSize * Math.sin(Math.toRadians(angle+180));

            addLine(X, Y, X2, Y2, angle);

            break;
          case LSystem.TURTLE_HALT:
            X2 = X;
            Y2 = Y;
            break;

          case LSystem.TURTLE_STEP:
            X2 = X + stepSize * Math.cos(Math.toRadians(angle));
            Y2 = Y - stepSize * Math.sin(Math.toRadians(angle));
            break;
        }

        X = X2;
        Y = Y2;

        if (X > timage.maxWidth)
          timage.maxWidth = (int)X;
        else if (X < timage.minWidth)
          timage.minWidth = (int)X;
        if (Y > timage.maxHeight)
          timage.maxHeight = (int)Y;
        else if (Y < timage.minHeight)
          timage.minHeight = (int)Y;
      }

    }

    lastStep = new TurtleStep(X, Y, X, Y, angle);

    /*
     * Nakonec obarvit
     */
    if(drawPalleteColor)
    {
      if(useLinearInterpolation)
        colorizeFractalUsingInterpolation();
      else if(useSmartAlgorithm)
        colorizeFractal();
      else
        randomlyColorizeFractal();
    }

    //System.out.println(fraktal);

    Main.debugOut("Fractal successfully builded! \n Total time: " + 
      ((System.nanoTime() - startTimeL) / (float)1000000000) + " sec \n Total lines: " + String.valueOf(fraktal.size())
       + "\n Iteration Level: " + String.valueOf(lSystem.iLevel));
  }
  
  /**
   * Metoda vykresli fraktal do grafickeho kontextu g predaneho zvnejsku
   *
   * @param g Graficky kontext do ktereho budeme vykreslovat
   * @param cX Pozice X stredu vykreslovani
   * @param cY Pozice Y stredu vykreslovani
   * @param zoom Urcuje nasobitel velikosti
   * @param lSysName jmeno L-Systemu
   * @return Graficky kontext s vykreslenym fraktalem
   */
  public void drawFractal(Graphics go, int cX, int cY, int areaW, int areaH, float zoom, String lSysName)
  {
    /* Prepnem do pokrocilejsiho vykreslovaciho modu, zatim pouze pro zmenu tloustky cary */
    Graphics2D g = (Graphics2D)go;

    g.setBackground(backColor);
    g.clearRect(0, 0, areaW, areaH);

    //Main.debugOut("Drawing fractal \n X: " + String.valueOf(cX) + "\n Y:" + String.valueOf(cY));

    if(drawOutSideSquare)
    {
      g.setColor(squareColor);
      g.drawRect ( (int) (cX + ((zoom * zoomConst) * timage.minWidth)), (int)(cY + ((zoom * zoomConst) * timage.minHeight)), (int)((zoom * zoomConst) * (timage.maxWidth - timage.minWidth)), (int)((zoom * zoomConst) * (timage.maxHeight - timage.minHeight)));
      g.drawString(lSysName, cX + ((zoom * zoomConst) * timage.minWidth), (cY + ((zoom * zoomConst) * timage.minHeight)));
    }

    for (int i = 0; i < fraktal.size(); i++)
    {
      TurtleStep ts = fraktal.get(i);

      if(drawRandomColors)
        g.setColor(getRandomColor());
      else if (useLinearInterpolation)
      {
        g.setColor(new Color(ts.color));
      }
      else if (drawPalleteColor)
      {
        g.setColor(cs.barvy.get(ts.color));  
      }
      else
        g.setColor(lineColor);



      //g.drawLine( Math.round((pX * (zoom * zoomConst)) + cX), Math.round((pY * (zoom * zoomConst)) + cY), Math.round(cX + ((int)ts.X * (zoom * zoomConst))), Math.round(cY + ((int)ts.Y * (zoom * zoomConst))));
      g.setStroke(new BasicStroke(lineWidth));
      g.drawLine( Math.round(((int)ts.sX * (zoom * zoomConst))) + cX, Math.round(((int)ts.sY * (zoom * zoomConst))) + cY, Math.round(((int)ts.X * (zoom * zoomConst))) + cX, Math.round(((int)ts.Y * (zoom * zoomConst))) + cY );
      g.setStroke(new BasicStroke(1));

      if(drawLinePoints)
      {
        g.setColor(linePointColor);
        g.drawOval( (int)(cX+((zoom * zoomConst) * ts.X) - 2), (int)(cY+((zoom * zoomConst) * ts.Y) - 2), 4, 4);
      }

    }

    g.setColor(startPointColor);
    g.drawOval(cX-4, cY-4, 8, 8);

    

    

    //return g;
  }

  /**
   * Vygeneruje nahodnou barvu v rozsahu vsech moznych barev
   *
   * @return Nahodna barva
   * @see boolean <b>drawRandomColors</b>
   */
  public Color getRandomColor()
  {
    return new Color(rGen.nextInt(255), rGen.nextInt(255), rGen.nextInt(255));
  }

  public void loadColorStack(int count)
  {
    cs.clear();
    for (int i = 0; i < count; i++)
    {
      cs.addColor(getRandomColor());
    }
  }

  /**
   * Nastavi konstatu pro zoomovani.
   * S touto konstantou bude vzdy fraktal zazoomovany tzv. pres celou oblast ktera je dana (areaW x areaH)
   *
   * @param areaW Sirka oblasti
   * @param areaH Vyska oblasti
   */
  public void setZoomConst(int areaW, int areaH, int margin)
  {
    if (-timage.minHeight + timage.maxHeight == 0 || -timage.minWidth + timage.maxWidth == 0)
    {
      zoomConst = 1;
      return;
    }
      

    float zH = ((float)areaH - margin) / (-timage.minHeight + timage.maxHeight);
    float zW = ((float)areaW - margin) / (-timage.minWidth + timage.maxWidth);

    if (zH < zW)
      zoomConst = zH;
    else
      zoomConst = zW;
  }

  /**
   * Vycentruje fraktal doprostred obrazovky
   * tedy nastavi cX a cY tak aby se fraktal vykreslil do oblasti (areaW x areaH)
   * @return pole
   */
  /*public int[] centerFractal(int areaW, int areaH)
  {
    int x, y;
    float pomer;
    int sirkaF = Math.abs(-timage.minWidth + timage.maxWidth);
    int vyskaF = Math.abs(-timage.minHeight + timage.maxHeight);
    
    if (sirkaF > vyskaF)
    {
      pomer = (float)sirkaF / (float)areaW;

      if (pomer == 0)
        return new int[] {areaW/2,areaH/2};

      if (areaW > areaH)
      {
        x = (int) Math.abs(Math.floor(timage.minWidth / pomer));
        y = (int) Math.abs(Math.floor(timage.minHeight / pomer)) + (int)(areaH - (vyskaF / pomer)) / 2;
      }
      else
      {
        x = (int) Math.abs(Math.floor(timage.minWidth / pomer));
        y = (int) Math.abs(Math.floor(timage.minHeight / pomer)) + (int)(areaH - (vyskaF / pomer)) / 2;
      }
    }
    else
    {
      pomer = (float)vyskaF / (float)areaH;

      if (areaW > areaH)
      {
        x = (int) Math.abs(Math.floor(timage.minWidth / pomer)) + (int)(areaW - (sirkaF / pomer)) / 2;
        y = (int) Math.abs(Math.floor(timage.minHeight / pomer));
      }
      else
      {
        x = (int) Math.abs(Math.floor(timage.minWidth / pomer)) + (int)(areaW - (sirkaF / pomer)) / 2;
        y = (int) Math.abs(Math.floor(timage.minHeight / pomer));
      }
    }
    return new int[] {x, y};
  }*/

  public int[] centerFractal(int areaW, int areaH)
  {
    int x, y;
    float pomer;
    int sirkaF = Math.abs(-timage.minWidth + timage.maxWidth);
    int vyskaF = Math.abs(-timage.minHeight + timage.maxHeight);

    if (sirkaF > vyskaF)
    {
      pomer = (float)sirkaF / (float)areaW;

      x = (int) Math.abs(Math.floor(timage.minWidth / pomer));
      y = (int) Math.abs(Math.floor(timage.minHeight / pomer));

      return new int[] {x, y};
    }
    else
    {
      pomer = (float)vyskaF / (float)areaH;

      x = (int) Math.abs(Math.floor(timage.minWidth / pomer));
      y = (int) Math.abs(Math.floor(timage.minHeight / pomer));

      return new int[] {x, y};
    }


  }


  /**
   * Zmensi hodnotu uhlu na nejmensi pouzitelnou hodnotu
   * v intervalu <0; 360> stupnu
   *
   * @param angle Uhel ktery chceme normalizovat
   * @return Normalizovany uhel
   */
  public double normalizeAngle(double angle)
  {
    angle = angle % 360;
    return angle;
  }

  /**
   * Trida pro uchovani rozmeru Zelviho obrazku
   * Uchovava maximalni a minimalni vysku a sirku
   *
   * Tyto udaje jsou dulezite pro spravne zazoomovani na cely fraktal,
   * jelikoz musime znat rozmery fraktalu pro vypocitani zoomovaci konstanty
   */
  class TurtleImage
  {
    protected int maxWidth = 0, minWidth = 0, maxHeight = 0, minHeight = 0;
  }


}

