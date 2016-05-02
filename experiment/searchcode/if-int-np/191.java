import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.security.*;
import javax.swing.*;
import javax.imageio.*;

public class BrokenClayTileVis {
    final int[] dirx = {1, 1,  1,  0}, diry = {1, 0, -1, -1};	//directions of main axes
    final int[] dx = {1, 0, -1, 0}, dy = {0, 1, 0, -1};		//crumbling neighborhood

    SecureRandom r;
    int[] xs, ys;	//coordinates of the polygon vertices
    int S,H,N;		//size of square, half of size and number of pixels
    char[][] pattern;	//the original tile (along with its pattern)
    int[][] breaks;	//the way the tile is broken to pieces
    int np, npres;	//number of pieces (before and after removing)
    boolean[] present;	//is piece i present in the pieces given to the competitor
    String[] arg;	//the argument to be passed to the solution
    String[] ret;	//competitor's return
    // -----------------------------------------
    void generateTileContour() {
        //first, generate the contour of the tile in small scale (multiply by 10 later)
        //generate it as a set of points 
        //generate topmost right part and reflect it
        int t,x,y,dir1,dir2;
        boolean outer = (r.nextInt(3)>0), inner = (r.nextInt(3)==0);
        int np = 3 + (outer ? 2 : 0) + (inner ? 3 : 1);
        xs = new int[np];
        ys = new int[np];
        //generate the outer side of the shape
        //choose 2 points on the sides of the angle and up to 1 random point inside of it
        xs[0] = 0;
        ys[0] = (r.nextInt(7)+5)*2;		//will need divisible by 2 later
        int mind = ys[0];
        if (!outer)
        {   //no random points inside - choose the only direction left
            t = ys[0];
            t = (r.nextInt(2)==0 ? t : t/2);
            xs[1] = ys[1] = t;
            //reflect the points
            xs[2] = ys[0];
            ys[2] = xs[0];
            np=3;
        }
        else
        {   //one random point inside - choose directions of two segments
            dir1=0;
            dir2=0; 
            t = r.nextInt(7);
            switch (t) {
                case 0: dir1=0; dir2=1; break;
                case 1: dir1=0; dir2=2; break;
                case 2: dir1=0; dir2=3; break;
                case 3: dir1=1; dir2=2; break;
                case 4: dir1=1; dir2=3; break;
                case 5: dir1=2; dir2=1; break;
                case 6: dir1=2; dir2=3;
            }
            do { t = r.nextInt(ys[0]-1)+1;
                 x = xs[0] + t * dirx[dir1];
                 y = ys[0] + t * diry[dir1]; }
            while (x>=y);
            xs[1] = x;
            ys[1] = y;
            mind = Math.min(mind, x+y);
            //figure out the point where the line with direction dir2 intersects diagonal r=c
            t = (xs[1] - ys[1])/(diry[dir2] - dirx[dir2]);
            xs[2] = xs[1] + t * dirx[dir2];
            ys[2] = ys[1] + t * diry[dir2];
            mind = Math.min(mind, xs[2]+ys[2]);
            //reflect the points
            xs[3] = ys[1];
            ys[3] = xs[1];
            xs[4] = ys[0];
            ys[4] = xs[0];
            np=5;
        }
        //generate the inner side of the shape
        //either a single point (i.e., no hole), or single segment (a rectangular hole)
        if (!inner)
        {   //no hole
            xs[np] = ys[np] = 0;
        }
        else
        {   //single segment
            do { x = r.nextInt(mind*2/3)+mind/6; }
            while (2*x>=mind);
            if (r.nextInt(2)==0)
            {   //diagonal
                xs[np] = 2*x;
                ys[np+2] = 2*x;
            }
            else
            {   //right corner
                xs[np] = x;
                ys[np+2] = x;
            }
            ys[np] = 0;
            xs[np+1] = x;
            ys[np+1] = x;
            xs[np+2] = 0;
        }

        //scale the result
        S = 0;
        for (int i=0; i<xs.length; i++)
            S = Math.max(S, xs[i]);
        t=(r.nextInt(6)*10+150)/S;
        for (int i=0; i<ys.length; i++)
        {   xs[i] = t*(xs[i]+S);
            ys[i] = t*(ys[i]+S);
        }
        H = t*S;
        S = 2*H+1;
    }
    // -----------------------------------------
    boolean isInside(int x, int y) {
        //boundaries check
        int s,i;
        for (i=0; i<xs.length; i++)
        {   if (x>Math.max(xs[i], xs[(i+1)%xs.length])) continue;
            if (x<Math.min(xs[i], xs[(i+1)%xs.length])) continue;
            if (y>Math.max(ys[i], ys[(i+1)%xs.length])) continue;
            if (y<Math.min(ys[i], ys[(i+1)%xs.length])) continue;
            s = (xs[(i+1)%xs.length] - x) * (ys[i] - y) - (ys[(i+1)%xs.length] - y) * (xs[i] - x);
            if (s==0) return true;
        }
        //main inside check
        double a=0,da;
        for (i=0; i<xs.length; i++)
        {   da = Math.atan2(ys[(i+1)%xs.length]-y, xs[(i+1)%xs.length]-x) - Math.atan2(ys[i]-y, xs[i]-x);
            if (da >  Math.PI) 
                da-=2*Math.PI;
            if (da < -Math.PI) 
                da+=2*Math.PI;
            a+=da;
        }  
        if (a>-Math.PI && a<Math.PI)
            return false;
        return true;
    }
    // -----------------------------------------
    void fillTile() {
        //convert tile contour to the tile itself, including colors
        //currently we have a contour of 1/4 of the tile - fill it, and then rotate it to fill the rest
        //first fill insides with background, and outsides with '.'
        int i,j;
        char c1, c2;				//foreground and background, respectively
        if (r.nextInt(2)==0)
        {   c1='1';	c2='0';	}
        else 
        {   c1='0';	c2='1';	}
        pattern = new char[S][S];
        for (i=H; i<S; i++)
        for (j=H; j<S; j++)
            if (isInside(j,i))
                pattern[i][j] = c2;
           else pattern[i][j] = '.';

        //the actual patterning
        //style 2 - some random lines (not necessarily parallel to 4 main axes)
        //drawn on 1/8 of the pattern, and then reflected
        //generate these random lines
        int nl = r.nextInt(5)+2,l;
        double d;
        int[] jbeg = new int[nl], jend = new int[nl], width = new int[nl];
        //line k begins at (0,jbeg[k]) and ends at (jend[k],jend[k])
        //j>=i
        for (l=0; l<nl; l++)
        {   jbeg[l] = r.nextInt(H);
            jend[l] = r.nextInt(H);
            width[l] = r.nextInt(5)+2;
        }

        for (i=H; i<S; i++)
        for (j=i; j<S; j++)
            if (pattern[i][j]!='.')
            {   for (l=0; l<nl; l++)
                {   d = Math.abs((i-H)*(jend[l]-jbeg[l])-jend[l]*(j-H-jbeg[l]))/Math.sqrt(jend[l]*jend[l]+(jend[l]-jbeg[l])*(jend[l]-jbeg[l]));
                    if (d<width[l])
                    {   pattern[i][j]=c1;
                        break;
                    }
                }
            }

        //reflect
        for (i=H; i<S; i++)
        for (j=i+1; j<S; j++)
            pattern[j][i] = pattern[i][j];

        //reflecting the whole pattern
        for (i=0; i<=H; i++)
        for (j=0; j<=H; j++)
        {   pattern[H-j][H+i] = pattern[H+i][H+j];
            pattern[H-i][H-j] = pattern[H+i][H+j];
            pattern[H+j][H-i] = pattern[H+i][H+j];
        }
    }
    // -----------------------------------------
    void breakTile() {
        //build a "map" of pieces the tile breaks to
        int i,j,x,y,sz;
        N=0;		//number of pixels in the tile
        for (x=0; x<S; x++)
        for (y=0; y<S; y++)
            if (pattern[x][y]!='.')
                N++;

        breaks = new int[S][S];
        for (x=0; x<S; x++)
        for (y=0; y<S; y++)
            breaks[x][y] = (pattern[x][y]=='.' ? -2 : -1);
        //breaks = -2 means that we don't need to assign it to one of the pieces

        sz = r.nextInt(51)+250;//average size of a piece
        np = N/sz;		//number of pieces
        int[] xp = new int[np], yp = new int[np];
        for (i=0; i<np; i++)
        {   //pick a random point to mark this part
            do {x = r.nextInt(S);
                y = r.nextInt(S);
            }
            while (breaks[x][y] != -1);
            breaks[x][y] = i;
            xp[i] = x;
            yp[i] = y;
        }

        //build Voronoi diagram based on these points
        int mind, minc;
        for (x=0; x<S; x++)
        for (y=0; y<S; y++)
            if (breaks[x][y]==-1)
            {   mind = 4*S*S; 
                minc = -1;
                for (i=0; i<np; i++)
                    if ((xp[i]-x)*(xp[i]-x)+(yp[i]-y)*(yp[i]-y) < mind)
                    {   mind = (xp[i]-x)*(xp[i]-x)+(yp[i]-y)*(yp[i]-y);
                        minc = i;
                    }
                breaks[x][y]=minc;
            }

        //remove some of the pieces (randomly)
        present = new boolean[np];
        Arrays.fill(present, true);
        int nrem = r.nextInt(np/5-np/10) + np/10;	//number of pieces to be removed
        for (i=0; i<nrem; i++)
        {   do { j = r.nextInt(np); }
            while (!present[j]);
            present[j] = false;
        }
        for (x=0; x<S; x++)
        for (y=0; y<S; y++)
            if (breaks[x][y]>-1 && !present[breaks[x][y]])
                breaks[x][y] = -1;

        //crumble the sides of the pieces left
        int cprob = r.nextInt(10)+25;
        boolean tileb, pieceb, crumble;
        for (j=0; j<15; j++)
        for (x=0; x<S; x++)
        for (y=0; y<S; y++)
            if (breaks[x][y]>-1)
            {   //a solid pixel - check whether it is a border one
                tileb = false;
                pieceb = false;
                for (i=0; i<4; i++)
                {   if (x+dx[i]<0 || x+dx[i]==S || y+dy[i]<0 || y+dy[i]==S)
                        continue;
                    if (pattern[x+dx[i]][y+dy[i]] == '.')
                    {   tileb = true;
                        break;
                    }
                    if (breaks[x+dx[i]][y+dy[i]] != breaks[x][y])
                        pieceb = true;
                }
                crumble = !tileb && pieceb && (r.nextInt(cprob)==0);
                if (crumble)
                    breaks[x][y]=-1;
            }

	//smooth the edges of crumbled pieces
        int nc;
        for (j=0; j<2; j++)
	for (x=0; x<S; x++)
	for (y=0; y<S; y++)
        {   nc=0;
            for (i=0; i<4; i++)
            {   if (x+dx[i]<0 || x+dx[i]==S || y+dy[i]<0 || y+dy[i]==S)
                    continue;
                if (breaks[x+dx[i]][y+dy[i]]!=breaks[x][y])
                    nc++;
            }
            if (nc>2)
                breaks[x][y]=-1;
	}
    }
    // -----------------------------------------
    void generateParam() {
        //convert breaks and pattern to the actual parameter
        int i,x,y;
        npres=0;	//number of pieces present (in case some piece crumbled completely)
        int[] minx = new int[np], maxx = new int[np], miny = new int[np], maxy = new int[np];
        Arrays.fill(minx, S);
        Arrays.fill(miny, S);
        Arrays.fill(maxx, -1);
        Arrays.fill(maxy, -1);
        Arrays.fill(present, false);
        //get the positions and sizes of the pieces
        for (x=0; x<S; x++)
        for (y=0; y<S; y++)
            if (breaks[y][x] > -1)
            {   minx[breaks[y][x]] = Math.min(minx[breaks[y][x]], x);
                maxx[breaks[y][x]] = Math.max(maxx[breaks[y][x]], x);
                miny[breaks[y][x]] = Math.min(miny[breaks[y][x]], y);
                maxy[breaks[y][x]] = Math.max(maxy[breaks[y][x]], y);
                present[breaks[y][x]] = true;
            }
        for (i=0; i<np; i++)
            if (present[i])
                npres++;

        //fix rotations for each piece
        int[] rot = new int[np];
        for (i=0; i<np; i++)
            rot[i] = r.nextInt(4);

        //now calculate the total length of the parameter
        //as sum of delta-y (or delta-x) for each present piece + spaces ("-") between them
        int arglen = npres-1;			//for "-" delimiters
        for (i=0; i<np; i++)
            if (present[i])
                if (rot[i]%2==0)
                    arglen += (maxy[i]-miny[i]+1);
               else arglen += (maxx[i]-minx[i]+1);

        //generate the param itself
        arg = new String[arglen];
        char[][] piece = new char[1][1];	//to get the piece itself
        int szy, szx;
        int argi = 0;
        for (i=0; i<np; i++)
            if (present[i])
            {   //get this piece and rotate it
                szy = maxy[i] - miny[i] + 1;
                szx = maxx[i] - minx[i] + 1;
                if (rot[i]%2==0)
                    piece = new char[szy][szx];
               else piece = new char[szx][szy];
                for (y=0; y<piece.length; y++)
                for (x=0; x<piece[0].length; x++)
                    piece[y][x]='.';		//empty
                for (x=minx[i]; x<=maxx[i]; x++)
                for (y=miny[i]; y<=maxy[i]; y++)
                    if (breaks[y][x]==i)	//copy only the pattern of this piece
                    {   if (rot[i]==0)
                            piece[y-miny[i]][x-minx[i]] = pattern[y][x];
                        if (rot[i]==1)
                            piece[szx-1-(x-minx[i])][y-miny[i]] = pattern[y][x];
                        if (rot[i]==2)
                            piece[szy-1-(y-miny[i])][szx-1-(x-minx[i])] = pattern[y][x];
                        if (rot[i]==3)
                            piece[x-minx[i]][szy-1-(y-miny[i])] = pattern[y][x];
                    }
                //convert it to Strings and add to arg
                for (y=0; y<piece.length; y++)
                    arg[argi++] = new String(piece[y]);
                //add delimiter
                if (argi<arglen-1)
                    arg[argi++] = "-";
            }
    }
    // -----------------------------------------
    public double runTest(String seed) {
      try {
        r = SecureRandom.getInstance("SHA1PRNG");
        r.setSeed(Long.parseLong(seed));
        generateTileContour();
        fillTile();
        breakTile();
        generateParam();

addFatalError("S = "+S);
addFatalError("N = "+N);
addFatalError(np + " pieces generated.");
addFatalError(npres + " pieces passed to the solution.");

        //finally, get to the actual data exchange with solution
        ret = reconstruct();

        //add dimension checks in server solution
        if (ret == null)
            addFatalError("No return to process.");

        //and compare it to the original tile
        int x,y;
        double score=0;
        if (exec != null)
        for (y=0; y<S; y++)
        {   if (ret[y].length() != S)
            {   addFatalError("Wrong number of characters in element "+y+" of return.");
                return 0;
            }
            for (x=0; x<S; x++)
            {   if (ret[y].charAt(x) !='.' && ret[y].charAt(x) != '0' && ret[y].charAt(x) != '1')
                {   addFatalError("Invalid character in return: '"+ret[y].charAt(x)+"'.");
                    return 0;
                }
                if (ret[y].charAt(x) == pattern[y][x])
                    //both '.' or both of same color
                    score += 1;
               else if (ret[y].charAt(x) != '.' && pattern[y][x] != '.')
                        //both present but in different color
                        score += 0.5;
            }
        }

        //visualization part
        if (vis)
        {   if (ret != null)
                jf.setSize(2*S+10+8, 2*S+10+27);
           else jf.setSize(2*S+10+8, S+27);
            v.repaint();
            jf.setVisible(true);
        }

        return score/S/S;
      }
      catch (Exception e) { 
        System.err.println("An exception occurred while trying to get your program's results.");
        e.printStackTrace(); 
        return 0.0;
      }
    }
// ------------- server part -------------------
    public String checkData(String test) {
        return "";
    }
    // -----------------------------------------
    public String displayTestCase(String test) {
        StringBuffer sb = new StringBuffer();
        sb.append("seed = "+test);
        return sb.toString();
    }
    // -----------------------------------------
    public double[] score(double[][] sc) {
        double[] res = new double[sc.length];
        //absolute - just a sum
        for (int i=0; i<sc.length; i++)
        {   res[i]=0;
            for (int j=0; j<sc[0].length; j++)
                res[i]+=sc[i][j];
        }
        return res;
    }
// ------------- visualization part ------------
    Vis v;
    static String exec;
    static boolean vis, save, autoexit;
    static Process proc;
    static String seed1;
    InputStream is;
    OutputStream os;
    JFrame jf;
    final int bg = 0xAAAAAA;
    // -----------------------------------------
    String[] reconstruct() throws IOException {
        //pass the params and get the result
    	
    	/****************************************/
    	//if (exec == null)
    	//	return null;
    	/****************************************/

    	int i;
        StringBuffer sb = new StringBuffer();
        sb.append(S).append('\n');
        sb.append(N).append('\n');
        sb.append(arg.length).append('\n');
        for (i=0; i<arg.length; i++)
            sb.append(arg[i]).append('\n');

    	/****************************************/
        if (exec == null){
        	FileOutputStream fos = new FileOutputStream(new File("input" + seed1 + ".txt"));
        	fos.write(sb.toString().getBytes());
        	return null;

        }
    	/****************************************/
        
        
        
        os.write(sb.toString().getBytes());
        os.flush();
        //MUST get exactly S strings
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String[] ret = new String[S];
        for (i=0; i<S; i++)
            ret[i] = br.readLine();
        return ret;
    }
    // -----------------------------------------
    int color(char t) {
        //returns the color for this character
        if (t=='0') return 0xEEEEEE;
	if (t=='1') return 0x000099;
        return bg;
    }
    // -----------------------------------------
    public class Vis extends JPanel implements WindowListener {
        public void paint(Graphics g) {
          try {
            //do painting here
            BufferedImage bi = new BufferedImage(S,S,BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = (Graphics2D)bi.getGraphics();
            int i,j,k;

            //overall background
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0,0,2*S+11,2*S+11);

            //1 - the tile before breaking
            //tile
            for (i=0; i<S; i++)
            for (j=0; j<S; j++)
                bi.setRGB(j,i,color(pattern[i][j]));
            g.drawImage(bi,0,0,S,S,null);
            if (save)
                ImageIO.write(bi,"png",new File(seed1+"_original.png"));

            //2 - the tile after breaking (to show the missing pixels)
            //background
            g2.setColor(new Color(bg));
            g2.fillRect(0,0,S+1,S+1);
            //tile
            for (i=0; i<S; i++)
            for (j=0; j<S; j++)
                if (breaks[i][j]>-1)
                    bi.setRGB(j,i,color(pattern[i][j]));
            g.drawImage(bi,S+10,0,S,S,null);
            if (save)
                ImageIO.write(bi,"png",new File(seed1+"_broken.png"));

            if (ret == null)
                return;

            //3 - the competitor's return
            //tile
            for (i=0; i<S; i++)
            for (j=0; j<S; j++)
                bi.setRGB(j,i,color(ret[i].charAt(j)));
            g.drawImage(bi,0,S+10,S,S,null);
            if (save)
                ImageIO.write(bi,"png",new File(seed1+"_returned.png"));

            //4 - comparison results
            for (i=0; i<S; i++)
            for (j=0; j<S; j++)
                if (ret[i].charAt(j) == pattern[i][j])
                    //both '.' or both of same color
                    bi.setRGB(j,i,0x00BB00);		//green
               else if (ret[i].charAt(j) != '.' && pattern[i][j] != '.')
                        //both present but in different color
                        bi.setRGB(j,i,0x0000BB);
                   else bi.setRGB(j,i,0xBB0000);
            g.drawImage(bi,S+10,S+10,S,S,null);
            if (save)
                ImageIO.write(bi,"png",new File(seed1+"_compared.png"));

            if (save) save = false;	//so that it doesn't re-save each time

            if (autoexit) System.exit(0);
          }
          catch (Exception e) {}
        }
        public Vis() {
            jf.addWindowListener(this);
        }
        //WindowListener
        public void windowClosing(WindowEvent e){ 
            if(proc != null)
                try { proc.destroy(); } 
                catch (Exception ex) { ex.printStackTrace(); }
            System.exit(0); 
        }
        public void windowActivated(WindowEvent e) { }
        public void windowDeactivated(WindowEvent e) { }
        public void windowOpened(WindowEvent e) { }
        public void windowClosed(WindowEvent e) { }
        public void windowIconified(WindowEvent e) { }
        public void windowDeiconified(WindowEvent e) { }
    }
    // -----------------------------------------
    public BrokenClayTileVis(String seed) {
        //interface for runTest
        if (vis)
        {   jf = new JFrame();
            v = new Vis();
            jf.getContentPane().add(v);

        }
        if (exec != null) {
            try {
                Runtime rt = Runtime.getRuntime();
                proc = rt.exec(exec);
                os = proc.getOutputStream();
                is = proc.getInputStream();
                new ErrorReader(proc.getErrorStream()).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Score = "+runTest(seed));


        
        
    }
    // -----------------------------------------
    public static void main(String[] args) {
        String seed = "1";
        vis = true;
        save = false;
        autoexit = false;
        for (int i = 0; i<args.length; i++)
        {   if (args[i].equals("-seed"))
                seed = args[++i];
            if (args[i].equals("-exec"))
                exec = args[++i];
            if (args[i].equals("-novis")){
                vis = false;
                autoexit = true;
            }
            if (args[i].equals("-save"))
                save = true;
        }
        if (save)
            vis = true;
        seed1 = seed;
        BrokenClayTileVis f = new BrokenClayTileVis(seed);
    }
    // -----------------------------------------
    void addFatalError(String message) {
        System.out.println(message);
    }
}

class ErrorReader extends Thread{
    InputStream error;
    public ErrorReader(InputStream is) {
        error = is;
    }
    public void run() {
        try {
            byte[] ch = new byte[50000];
            int read;
            while ((read = error.read(ch)) > 0)
            {   String s = new String(ch,0,read);
                System.out.print(s);
                System.out.flush();
            }
        } catch(Exception e) { }
    }
}

