import java.awt.Color;


/*<pre>
   This is a simple example to show how to use the MISApplet
   to make your own pixel-by-pixel framebuffer.

   Two methods you can override from the MISApplet base
   class are initFrame and setPixel.
 */

public class MyMISApplet extends MISApplet {

	//----- THESE TWO METHODS OVERRIDE METHODS IN THE BASE CLASS

	double t = 0;
	private Geometry cube = null;
	
	@Override
	public void init(){
		super.init();
		
	}

	public void initFrame(double time) { // INITIALIZE ONE ANIMATION FRAME

		//       REPLACE THIS CODE WITH YOUR OWN TIME-DEPENDENT COMPUTATIONS, IF ANY.

		t = 10 * time;
//		this.setBackground(Color.WHITE);

	}

	int rgbBak[];
	double centerX = 200;
	double centerY = 200;
	double  cwidth= 10;
	double space = 30;
	double r=0;
	double R =0;
	public void setPixel(int x, int y, int rgb[]) { // SET ONE PIXEL'S COLOR
		if(x>=0 && x<W && y>=0 && y<H){
			pix[x+y*W] = pack(rgb[0],rgb[1],rgb[2]);
		}
		
	}
	


	
	private int[] rgb_WHITE = {255,255,255};
	
	@Override
	public void computeImage(double time){
		initFrame(time); 
		
		for(int i=0;i<W;i++){
			for(int j=0;j<H;j++)
				setPixel(i,j,rgb_WHITE);
		}
		
		cube = Geometry.CubeFactory();
//		cube = Geometry.OctahedronFactory();
		cube.identity();
		cube.scale(150);
		cube.rotateX(t/20);
		cube.rotateY(0.3+t/20);
		cube.rotateZ(0.3+t/20);
		cube.matrix.translate(200, 200, 0);

		
		drawGeometry(cube,rgb_WHITE);

	} 
	
	private double[][] oneFaceVertices = null;;
	private double[][] geoVertices = null;
	private double[][] dst = null;;
	public void drawGeometry(Geometry geo, int[] rgb){
		geoVertices = geo.getVertices();

		for(int[] f: geo.getFace()){
			if(oneFaceVertices == null || oneFaceVertices.length != f.length){
				oneFaceVertices = new double[f.length][3];
				dst = new double[f.length][3];
			}
			
			for(int i=0;i<f.length;i++){
				oneFaceVertices[i] = geoVertices[f[i]];
				geo.matrix.transform(oneFaceVertices[i], dst[i]);
			}
			drawPolygon(dst, new int[] {255,0,0});
					
		}

			
		
	}
	
	public void drawPolygon(double vertices[][], int rgb[]){
		for(int i=1;i<vertices.length-1;i++){
			drawTrangle(vertices[0],vertices[i],vertices[i+1],rgb);

		}
	}
	
	
	private void drawTrangle(double[] a, double[] b, double[] c, int[] rgb){
		v= new double[3][3];
		v[0] = a;
		v[1] = b;
		v[2] = c;
		drawTrangle(v, rgb);
	}
	
	private double epsilon = 1;
	private double tmp[];
	private double v[][];
	private double area;
	private double[] d= new double[2];
	private double tt = 0;
	private double XL = 0;
	private double XR = 0;
	private double XLT = 0;
	private double XRT = 0;
	private double XLB = 0;
	private double XRB = 0;
	private double YT = 0;
	private double YB = 0;
	private int[] rgb_BLACK = {0,0,0};
	
	private void drawTrangle(double vertics[][], int rgb[]){
		area = 0;
		area += (vertics[0][0] - vertics[1][0]) * (vertics[1][1]+vertics[0][1]) / 2;
		area += (vertics[1][0] - vertics[2][0]) * (vertics[2][1]+vertics[1][1]) / 2;
		area += (vertics[2][0] - vertics[0][0]) * (vertics[0][1]+vertics[2][1]) / 2;

		if(area > 0){
			return;
		}
		
		v = vertics.clone();
		for(int i=0;i<3;i++)
			for(int j=i;j<3;j++){
				if (v[i][1] > v[j][1]){
					tmp = v[j];
					v[j] = v[i];
					v[i] = tmp;
				}
			}

		if(v[2][1] - v[0][1] > epsilon){
			tt = (v[1][1] - v[0][1]) / (v[2][1] - v[0][1]);
			d[0] = v[0][0] + tt*(v[2][0]-v[0][0]);
			d[1] = v[1][1];
		}else{
			d[0] = v[2][0];
			d[1] = v[1][1];
		}
		
		
		YT = v[0][1];
		YB = d[1];
		XLT =XRT = v[0][0];
		if(d[0]<= v[1][0]){
			XLB = d[0];
			XRB = v[1][0];
		}else{
			XLB = v[1][0];
			XRB = d[0];
		}
		
		for(int y = (int)Math.ceil(YT); y< YB; y++){
			tt = (y-YT) / (YB-YT);
			XL = XLT + tt* (XLB-XLT);
			XR = XRT + tt* (XRB-XRT);
			for(int x = (int)XL; x< XR; x++){
				
				setPixel(x,y,rgb);
			}

		}

		YB = v[2][1];
		YT = d[1];
		if(d[0]<= v[1][0]){
			XLT = d[0];
			XRT = v[1][0];
		}else{
			XLT = v[1][0];
			XRT = d[0];
		}
		XLB = XRB = v[2][0];
		for(int y = (int)Math.ceil(YT); y< YB; y++){
			tt = (y-YT) / (YB-YT);
			XL = XLT + tt* (XLB-XLT);
			XR = XRT + tt* (XRB - XRT);
			for(int x = (int)XL; x< XR; x++){
				setPixel(x,y,rgb);
			}
			
		}	
	}
	
	
	
	double square(double a){
		return a*a;
	}

	int getRed(int rgb) { return rgb >> 16 & 255; }
	int getGrn(int rgb) { return rgb >>  8 & 255; }
	int getBlu(int rgb) { return rgb       & 255; }

	int packRGB(int r, int g, int b) {
		return 255 << 24 | r << 16 | g << 8 | b;
	}
}

