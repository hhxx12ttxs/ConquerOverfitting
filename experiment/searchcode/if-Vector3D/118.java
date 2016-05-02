package brain;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import perspectives.base.DataSource;
import perspectives.base.Property;
import perspectives.base.Task;
import perspectives.properties.PFileInput;
import perspectives.properties.PInteger;
import perspectives.three_d.Vector3D;
import perspectives.base.PropertyType;


public class BrainData extends DataSource {
	
	Vector3D segments[][];
	
	boolean loaded = false;

	public BrainData(String name) {
		super(name);
		
		try {
			
			final BrainData thisf = this;
			Property<PFileInput> p = new Property<PFileInput>("Load", new PFileInput())
					{
					@Override
						public boolean updating(PFileInput newvalue)
						{
							final PFileInput newvaluef = newvalue;
							
							Task t = new Task("Loading...")
							{	
							
								@Override
								public void task() {
									fromFile(((PFileInput)newvaluef).path);
									thisf.removeProperty("Load");
									
									Property<PInteger> p = new Property<PInteger>("#tubes", new PInteger(thisf.segments.length));
									p.setReadOnly(true);
									try {
										thisf.addProperty(p);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									loaded = true;
									done();
								}
							};
							t.indeterminate = true;
							t.blocking = true;
							thisf.startTask(t);
							
							return true;
						}
					};
			this.addProperty(p);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isLoaded()
	{
		return loaded;
	}
	
	
	public void fromFile(String fileName)
	{
		System.out.println("loading tubes...");
		
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(new File(fileName));
		
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String s;
		
			int n = Integer.parseInt(br.readLine());
		
		//tubes = new Triangle[n][];
			segments = new Vector3D[n][];
			
			Vector3D min = new Vector3D(99999,99999,99999);
			Vector3D max = new Vector3D(-99999,-99999,-99999);
			
		
			for (int k=0; k<n; k++)
			{
				int m = Integer.parseInt(br.readLine());
			
				segments[k] = new Vector3D[m];
				for (int j=0; j<m; j++)
				{
					String[] xyzs = br.readLine().split(" ");				
					
					double x = Double.parseDouble(xyzs[0]);
					double y = Double.parseDouble(xyzs[1]);
					double z = Double.parseDouble(xyzs[2]);	
					
					if (x < min.x) min.x = (float)x;
					if (y < min.y) min.y = (float)y;
					if (z < min.z) min.z = (float)z;
					
					if (x > max.x) max.x = (float)x;
					if (y > max.y) max.y = (float)y;
					if (z > max.z) max.z = (float)z;
					
					Vector3D pp = new Vector3D((float)x,(float)y,(float)z);
					segments[k][j] = pp;
				}
			}
			
			Vector3D center = new Vector3D(min.x+ (max.x-min.x)/2, min.y+(max.y-min.y)/2, min.z+(max.z- min.z)/2);
			//Vector3D center = new Vector3D(min.x+100, min.y, min.z);
			for (int i=0; i<segments.length; i++)
				for (int j=0; j<segments[i].length; j++)
				{
					segments[i][j] = segments[i][j].minus(center).div(max.minus(min).length()).times(8);
				}
			
	
			System.out.println("done loading tubes");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	
    public double getTubeLength(int tubeId)
    {
        Vector3D[] segments = this.segments[tubeId];
        double length =0;
        for(int i=1;i<segments.length;i++)
        {
            Vector3D v1 = segments[i-1];
            Vector3D v2 = segments[i];
            length += v1.minus(v2).magnitude();
        }
        return length;
    }	
	

}

