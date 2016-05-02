package brain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

import perspectives.base.DataSource;
import perspectives.base.Property;
import perspectives.base.Task;
import perspectives.properties.PFileInput;
import perspectives.properties.PFileOutput;
import perspectives.properties.PInteger;
import perspectives.three_d.Vector3D;

public class BrainDataModifier extends DataSource {
	public static final String PROPERTY_LOAD ="Load";
	public static final String PROPERTY_SAVE ="Save modified";
	
	private static final long serialVersionUID = -537835664618242030L;

	Vector3D segments[][];
	ArrayList<ArrayList<Vector3D>> outSegments;
	
	boolean loaded = false;

	public BrainDataModifier(String name) {
		super(name);
		
		try {
			
			final BrainDataModifier thisf = this;
			Property<PFileInput> p = new Property<PFileInput>(PROPERTY_LOAD, new PFileInput())
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
									thisf.removeProperty(PROPERTY_LOAD);
									
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
			
			Property<PFileOutput> p1 = new Property<PFileOutput>(PROPERTY_SAVE, new PFileOutput())
			{
					@Override
					protected boolean updating(PFileOutput newvalue) {
						final PFileOutput newvaluef = newvalue;
						
						Task t = new Task("Saving...")
						{	
						
							@Override
							public void task() {
								thisf.toFile(newvaluef.path);
								thisf.removeProperty(PROPERTY_SAVE);
								
								Property<PInteger> p = new Property<PInteger>("#modified tubes", new PInteger(thisf.outSegments.size()));
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
			this.addProperty(p1);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isLoaded()
	{
		return loaded;
	}
	
	public void toFile(String fileName)
	{
		try {
			File f = new File(fileName);
			FileWriter fstream = new FileWriter(f);
			BufferedWriter out = new BufferedWriter(fstream);
			
			out.write(this.outSegments.size()+"\r\n");
			for(ArrayList<Vector3D> tube: this.outSegments)
			{
				out.write(tube.size()+"\r\n");
				for(Vector3D v:tube)
				{
					out.write(v.x+" "+v.y+" "+v.z+"\r\n");
				}
			}
			
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
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
		
			segments = new Vector3D[n][];
			double maxTubeLength = 0;
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
					
					Vector3D pp = new Vector3D((float)x,(float)y,(float)z);
					segments[k][j] = pp;
					
				}
				double tubeLength = this.getTubeLength(segments[k]);
				if(tubeLength > maxTubeLength)
				{
					maxTubeLength = tubeLength;
				}
			}
			this.outSegments = new ArrayList<ArrayList<Vector3D>>();
			for (int k=0; k<this.segments.length; k++)
			{
			
				double tubeLength = this.getTubeLength(this.segments[k]);
				double probability = this.getProbability(tubeLength, maxTubeLength);
				double score = Math.random();
				if(score > probability)
				{
					continue;
				}
				ArrayList<Vector3D> tube = new ArrayList<Vector3D>();
				for (int j=0; j<this.segments[k].length; j++)
				{
					tube.add(this.segments[k][j]);
			
				}
				this.outSegments.add(tube);
			}
			System.out.println("done loading tubes");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	private double getTubeLength(Vector3D[] tubeSegment)
    {
        double length =0;
        for(int i=1;i<tubeSegment.length;i++)
        {
            Vector3D v1 = tubeSegment[i-1];
            Vector3D v2 = tubeSegment[i];
            length += v1.minus(v2).magnitude();
        }
        return length;
    }
	public double getProbability(double tubeLength, double maxTubeLength)
	{
		return 0.6 - 0.3 *Math.min(1,tubeLength/ maxTubeLength);
	}
}

