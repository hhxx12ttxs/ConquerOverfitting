package stimulusgen;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

public class GazeHeatmap {


	ArrayList<Fixation> fixations;

	//ArrayList<String> users;	
	//ArrayList<String> allUsers;	
	//ArrayList<String> mouseEvents;
	//ArrayList<float[][]> heatmapSeries = new ArrayList<float[][]>();
	//ArrayList<Long> heatmapSeriesTimes  = new ArrayList<Long>();
//	long maxTime;
	//boolean normalize = false;
	//ArrayList<BufferedImage> stimulusSequence = null;
	//ArrayList<Long> stimulusSequenceTimes = null;
	
	
	Vector4[][] heatmap = null;
	float[][] spot = null;
	
	boolean heatmapChanged = false;
	
	int width;
	int height;
	
	class Vector4
	{
		double x = 0;
		double y = 0;
		double z = 0;
		double a = 0;
		
		public double length()
		{
			return Math.sqrt(x*x + y*y + z*z);
		}
	}


	

	
	public GazeHeatmap(int width, int height, ArrayList<Fixation> fix)
	{
		this.width = width;
		this.height = height;		
		this.fixations = fix;
	}
	
	public Vector4[][] initHeatmap(int width, int height)
	{
		Vector4[][] heatmap = new Vector4[width][];
		for (int i=0; i<width; i++)
		{
			heatmap[i] = new Vector4[height];
			for (int j=0; j<heatmap[i].length; j++)
				heatmap[i][j] = new Vector4();
		}	
		heatmapChanged = true;
		
		return heatmap;
	}
	
	public void initSpot(int r)
	{
		spot = new float[2*r][];
		for (int i=0; i<2*r; i++)
			spot[i] = new float[2*r];
		
		float sigma = r/3;
		
		float oneover2pi = 1/ (sigma * (float)Math.sqrt(2*Math.PI));
		
		
		for (int i=0; i<2*r; i++)
			for (int j=0; j<2*r; j++)
			{
				float distance = (float)((i-r)*(i-r) + (j-r)*(j-r));
				float f = oneover2pi * (float)Math.exp(-0.5 * distance / (sigma*sigma));
				
				//spot[i][j] = 1-(float)Math.min((float)distance/(r*r),1.);	
				spot[i][j] = f;	
			}
	}
	
	public void putSpotOnHeatmap(Vector4[][] heatmap, int x, int y, int r, double f, double rank)
	{
		if (spot == null || spot.length != 2*r)
			initSpot(r);
		
		for (int i=-r; i<r; i++)
		{
			int cx = x+i;
			if (cx < 0 || cx >= heatmap.length)
				continue;
			
			for (int j=-r; j<r; j++)
			{				
				int cy = y+j;
				if (cy < 0 || cy >= heatmap[cx].length)
					continue;
				
				Vector4 v = rankToColor(rank);
				v.a = f*spot[i+r][j+r];	
				v.x = v.x*spot[i+r][j+r];	
				v.y = v.y*spot[i+r][j+r];	
				v.z = v.z*spot[i+r][j+r];
				
				
				heatmap[cx][cy].x += v.x;
				heatmap[cx][cy].y += 0;
				heatmap[cx][cy].z += 0;
				heatmap[cx][cy].a += v.a;
			}
		}
		heatmapChanged = true;
	}
	
	public Vector4 rankToColor(double rank)
	{
		/*Vector4 v = new Vector4();
		
		if (rank < 0.5)
		{
			v.x = 0;
			v.y = rank;
			v.z = 1-rank;
		}
		else
		{
		v.x = rank;
		v.y = 1-rank;
		v.z = 0;
		}*/
		
		Vector4 v = new Vector4();
		v.x = 1; v.y = 0; v.z = 0; v.a = 0;
		
		return v;
	}
	
	public Vector4 maxValueOnHeatmap(Vector4[][] heatmap)
	{
		Vector4 mx=new Vector4();
		for (int i=0; i<heatmap.length; i++)
			for (int j=0; j<heatmap[i].length; j++)
			{
				if (heatmap[i][j].x > mx.x)
					mx.x = heatmap[i][j].x;
				if (heatmap[i][j].y > mx.y)
					mx.y = heatmap[i][j].y;
				if (heatmap[i][j].z > mx.z)
					mx.z = heatmap[i][j].z;
				if (heatmap[i][j].a > mx.a)
					mx.a = heatmap[i][j].a;
			}
		return mx;
	}
	
	public void normalizeHeatmap(Vector4[][] heatmap)
	{
		Vector4 mx = maxValueOnHeatmap(heatmap);
	
		for (int i=0; i<heatmap.length; i++)
			for (int j=0; j<heatmap[i].length; j++)
			{
				if (mx.x != 0)		heatmap[i][j].x /= mx.x;
				if (mx.y != 0)		heatmap[i][j].y /= mx.y;
				if (mx.z != 0)		heatmap[i][j].z /= mx.z;
				
				if (mx.a != 0)		heatmap[i][j].a /= mx.a;
			}
					
	}
	
	public void normalizeHeatmap(Vector4[][] heatmap, Vector4 mx)
	{
		for (int i=0; i<heatmap.length; i++)
			for (int j=0; j<heatmap[i].length; j++)
			{
				if (mx.x != 0)		heatmap[i][j].x /= mx.x;
				if (mx.y != 0)		heatmap[i][j].y /= mx.y;
				if (mx.z != 0)		heatmap[i][j].z /= mx.z;
				
				if (mx.a != 0)		heatmap[i][j].a /= mx.a;
			}					
	}
	
	public BufferedImage drawHeatmap(int mode, double transp)
	{
		if (heatmap == null) return null;
		
		BufferedImage image = new BufferedImage(heatmap.length, heatmap[0].length, BufferedImage.TYPE_INT_ARGB);
	
        WritableRaster raster = (WritableRaster) image.getData();
        for (int i=0; i<heatmap.length; i++)
        	for (int j=0; j<heatmap[i].length; j++)
        	{
        		int a = (int)(255*heatmap[i][j].a);
        		if (a > 255) a=255;
        		a = (int)(a * transp); 
        		
        		int r = (int)(255*heatmap[i][j].x);
        		if (r > 255) r=255;
        		int g = (int)(255*heatmap[i][j].y);
        		if (g > 255) g=255;
        		int b = (int)(255*heatmap[i][j].z);
        		if (b > 255) b=255;
        		
        		Color c = null;
        		if (mode == 0)
        		{
        			//if (i % 2 == 0)
        				c= new Color(r,g,b,a);
        			//else 
        			//	c= new Color(255,255,0,r);
        		}
        		else if (mode == 1)
        			c= new Color(0,0,0,255-a);
        		
        		image.setRGB(i, j, c.getRGB());
        	}
     
       return image;
	}
	
	public void computeHeatmap(int r)
	{
		heatmap = initHeatmap(this.width, this.height);
		 for (int i=0; i<fixations.size(); i++)
			{
			// if (mouseEvents.get(i).length() != 0)
				// continue;
			 
				double x = fixations.get(i).x;
				double y = fixations.get(i).y;
				
				long time = fixations.get(i).duration;	
				if (time < 0) time = 0;					
		
				double rank = fixations.get(i).rank / (double)fixations.size();
				
				this.putSpotOnHeatmap(this.heatmap, (int)x, (int)y, r, time, rank);
			}
		 normalizeHeatmap(this.heatmap);
	}
	
	double colorLength(Color c)
	{
		return Math.sqrt(c.getRed()*c.getRed() + c.getGreen()*c.getGreen() + c.getBlue()*c.getBlue());
	}
	
	/*public void computeHeatmapSeries(int r)
	{
		heatmapSeries = new ArrayList<float[][]>();
		heatmapSeriesTimes  = new ArrayList<Long>();
		
		int[] m = new int[users.size()];
		for (int i=0; i<m.length; i++)
			m[i] = 0;
		
		//int rr = (Integer)this.getProperty("Radius").getValue();
		
		long prevcmt = -1;
		for (int k=0; k<m.length*2; k++ )
		{
			long cmt = Long.MAX_VALUE;
			int index = 0;
			for (int i=0; i<users.size(); i++)
			{
				if (mouseEvents.get(i).length() != 0)
					continue;
				
				if (m[i] == 0 && startTimes.get(i) < cmt)
				{
					cmt = startTimes.get(i);
					index = i;
				}
				else if (m[i] == 1 && endTimes.get(i) < cmt)
				{
					cmt = endTimes.get(i);
					index = -i;
				}
			}
			
			m[Math.abs(index)]++;
			
			float[][] ph, ch; 
			if (heatmapSeries.size() == 0)
			{
				if (stimulusSequence != null)
					ch = initHeatmap(stimulusSequence.get(0).getWidth(), stimulusSequence.get(0).getHeight());
				else
					ch = initHeatmap(stimulusImage.getWidth(), stimulusImage.getHeight());
				
						
			}
			else
			{
				ph = heatmapSeries.get(heatmapSeries.size()-1);
				if (prevcmt != cmt)
				{
					if (stimulusSequence != null)
						ch = initHeatmap(stimulusSequence.get(0).getWidth(), stimulusSequence.get(0).getHeight());
					else
						ch = initHeatmap(stimulusImage.getWidth(), stimulusImage.getHeight());
				for (int x=0; x<ph.length; x++)
					for (int y=0; y<ph[x].length; y++)
						ch[x][y] = ph[x][y];
				}
				else ch = ph;
			}
			

			
			long dur = duration.get(Math.abs(index));	
			if (dur < 0) dur = 0;	
			if (index > 0)
			this.putSpotOnHeatmap(ch, (int)this.fixations.get(Math.abs(index)).getX(), (int)fixations.get(Math.abs(index)).getY(), r, (float)(dur * Math.signum(index)));
			
			if (prevcmt != cmt)
			{
			heatmapSeries.add(ch);
			heatmapSeriesTimes.add(cmt);
			}
			
			prevcmt = cmt;
			
		}
		//maxTime = (long)Math.log(maxTime);
		double maxf = Double.MIN_NORMAL;
		for (int i=0; i<heatmapSeries.size(); i++)
		{
			double v = this.maxValueOnHeatmap(heatmapSeries.get(i));
			if (v > maxf) maxf = v;
		}
		for (int i=0; i<heatmapSeries.size(); i++)
			this.normalizeHeatmap(heatmapSeries.get(i),maxf);
	}
	*/
	
	

	
	
	

	
}

