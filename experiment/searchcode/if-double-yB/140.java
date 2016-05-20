package pangeo.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Map;

import org.newdawn.slick.*;

//import org.lwjgl.Sys;

@SuppressWarnings("unused")
public class World 
{
<<<<<<< HEAD
    public static int vegTypes = 11;
    public static Random rand = new Random();
	public static String currentWorld = "PangeoTest";

    
    class Tuple<X,Y>{
        X a;
        Y b;
        Tuple(X x,Y y){a=x;b=y;}
        public int hashCode() {
            return a.hashCode()+b.hashCode();
        }
        @SuppressWarnings("unchecked")
		public boolean equals(Object obj) {
            Tuple<X,Y> k=(Tuple<X,Y>)obj;
            return this.a.equals(k.a)&&this.b.equals(k.b);
        }
        public String toString(){
            String retur="("+a+","+b+")";
            return retur;
        }
    }

    final static int const_loaded_MAX_X=50, const_loaded_MAX_Y=50;

=======
    final static int const_loaded_MAX_X=500, const_loaded_MAX_Y=2400;  // 50  50

   
>>>>>>> origin/Justp03_testDirtyCacheAgain
    public class WorldChunk {
        
        WorldBlock blocks[][]=new WorldBlock[const_loaded_MAX_X][const_loaded_MAX_Y];//100 200
        int xBig,yBig;
        int loaded=0; //0 to 100
        float lastPulse=0; //0 to 1  ,  1=very hurry, 0 = do nothing
        java.io.ObjectInputStream in;
        boolean generateCalled=false;
        WorldChunk(int cxbig,int cybig){
            xBig=cxbig; yBig=cybig;
            for(int xb=0;xb<const_loaded_MAX_X;xb++){
                for(int yb=0;yb<const_loaded_MAX_Y;yb++){
                    blocks[xb][yb]=new WorldBlock();
                }
            }
        }
        String saveFolder()
        {

        	return "saves/worlds/" + World.currentWorld + "/";
        }
        String chunkFolder()
        {
            return saveFolder() + "chunks/";

        }
        String fileName(){
            return chunkFolder() + "pangeoChunk"+xBig+"_"+yBig+".chunk";
        }
        public void write (){
            try{
                new File(chunkFolder()).mkdirs();
                java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(new java.io.FileOutputStream(fileName()));

                for(int x=0;x< const_loaded_MAX_X;x++){
                    for(int y=0;y< const_loaded_MAX_Y;y++){
                   
                        //out.writeObject(blocks[x][y]);
                        out.writeObject(blocks[x][y]);
                    }
                } 
                out.flush();
                out.close();
                //Sys.alert("Path", new File(fileName()).getAbsolutePath());/
                Util.print("World#1207" + (int)(Math.random()*100)+ "chunk writeFile big=("+xBig+","+yBig+")");
            }catch(IOException e){ 
                e.printStackTrace();       
                }
        }

        public void read (){
            boolean fact1=  loaded==const_loaded_MAX_X*const_loaded_MAX_Y;
            boolean fact2=  generateCalled;
            if(fact1||fact2)return; 
            Util.print("WorldChunk#6145 ("+xBig+","+yBig+")");
            if(xBig==3&&yBig==4){
                
                //int asdfads=0;
            }
            try { 
                new File(chunkFolder()).mkdirs();
                FileInputStream fileIO=null;
                if(loaded==0 && in==null){
                    fileIO= new java.io.FileInputStream(fileName());
                    in = new java.io.ObjectInputStream(fileIO);
                    Util.print("JustP info#6790 chunk readFile big=("+xBig+","+yBig+")");

                    Util.print("WorldChunk#6790 chunk readFile big=("+xBig+","+yBig+")");
                }
=======
}
>>>>>>> origin/Justp03_testDirtyCacheAgain
                int indexStart=loaded;
                int indexEnd=(int)(loaded+lastPulse*const_loaded_MAX_X*const_loaded_MAX_Y);
                if(indexEnd>=const_loaded_MAX_X*const_loaded_MAX_Y){
                    indexEnd=const_loaded_MAX_X*const_loaded_MAX_Y;
                    Util.print("JustP info#3164 chunk finish big=("+xBig+","+yBig+")");
                }
                loaded=indexEnd;
                for(int n=indexStart;n<indexEnd;n++){
                    try{
                        Object kk=in.readObject();
                        WorldBlock wb=(WorldBlock)kk;
                        blocks[n/const_loaded_MAX_Y][n%const_loaded_MAX_Y]=wb;
                    }catch(IOException eee){
                        eee.printStackTrace();
                    }
                }
                if(indexEnd==const_loaded_MAX_X*const_loaded_MAX_Y)//in.close();
                if(fileIO!=null)fileIO.close();
            } catch (FileNotFoundException e) {
                //if(!generateCalled){
                Util.print("JustP info#4142 chunk findNotFound called big=("+xBig+","+yBig+")  ");
                generateCalled=true;
                Util.print("JustP info#4125 chunk generateNew big=("+xBig+","+yBig+")  start");
                rangerOfTheWest__epicMapGenerator(xBig*const_loaded_MAX_X,yBig*const_loaded_MAX_Y,(xBig+1)*const_loaded_MAX_X,(yBig+1)*const_loaded_MAX_Y);

               
                //rangerOfTheWest_dummyMapGenerator(xBig*const_loaded_MAX_X,yBig*const_loaded_MAX_Y,(xBig+1)*const_loaded_MAX_X,(yBig+1)*const_loaded_MAX_Y);

                Util.print("JustP info#4148 chunk generateNew big=("+xBig+","+yBig+")  finish");
                loaded=const_loaded_MAX_X*const_loaded_MAX_Y;
                //}
                
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        void receivePULSE(float strength){
            lastPulse=Math.max(lastPulse, strength);
        }
        void timestep_update(){
            read(); lastPulse=0;
        }
        //loop of the game is like  :-
        //    receivePULSE() and update other objects --> delete block "lastPulse"==0 --> timestep_update()  
    }
    
    public EnumWorldGenerator genType = EnumWorldGenerator.NORMAL;
    public int size_x = 500; //23500   //27000  //the whole world
    public int size_y = 2400; //2400
   // private WorldBlock[][] block=new WorldBlock[size_x][size_y];//[size_x][size_y];
    Hashtable<Tuple<Integer,Integer>, WorldChunk> chunks=new Hashtable<Tuple<Integer,Integer>, WorldChunk>();
    WorldBlock chunks_multithreadSupported[][]=new WorldBlock[size_x][size_y];
    boolean chunks_multithread_check_isCallMapGenerator=false;
    public WorldBlock getWorldBlock(int xSmall,int ySmall){
        if(!chunks_multithread_check_isCallMapGenerator){
            chunks_multithread_check_isCallMapGenerator=true;
            rangerOfTheWest__epicMapGenerator(0,0,size_x,size_y);
            
        }
        if(xSmall<0 || xSmall>=const_loaded_MAX_X ||ySmall<0 || ySmall>=const_loaded_MAX_Y ){
            Util.pn("World#7721 error out of bound access");
        }
        if(chunks_multithreadSupported[xSmall][ySmall]==null){
            chunks_multithreadSupported[xSmall][ySmall]=new WorldBlock();
        }
        return chunks_multithreadSupported[xSmall][ySmall];
            
//        if(false){    
//   
//            int xBig=(int)Math.floor( ( (float)xSmall)/const_loaded_MAX_X);
//            int yBig=(int)Math.floor( ( (float)ySmall)/const_loaded_MAX_Y);
//            WorldChunk chunk=PULSE_soft( xBig   , yBig,1.0000f);
//            chunk.read();//force hard pulse
//            return chunk.blocks[xSmall-xBig*const_loaded_MAX_X][ySmall-yBig*const_loaded_MAX_Y];
//        }
    }
    
    public void  world (int x,int y,int value){
        getWorldBlock(x, y).world=(short)value; //short
    }
    public void  destruction (int x,int y,int value){
        getWorldBlock( x, y).destruction=(short)value;
    }
    public void  xSlot (int x,int y,int value){
        getWorldBlock( x, y).xSlot=(byte)value;
    }
    public void  ySlot (int x,int y,int value){
        getWorldBlock( x, y).ySlot=(byte)value;
    }
    public void  wire (int x,int y,int value){
        getWorldBlock( x, y).wire=(short)value;
    }
    public void  direction (int x,int y,int value){
        getWorldBlock( x, y).direction=(byte)value;
    }
    public void  slopeStyle (int x,int y,int value){
        getWorldBlock( x, y).slopeStyle=(byte)value;
    }
    public void  wall (int x,int y,int value){
        getWorldBlock( x, y).wall=(short)value;
    }
    public void  liquid (int x,int y,int value){
        getWorldBlock( x, y).liquid=(short)value;
    }    
    public void  metaId (int x,int y,int value){
        getWorldBlock( x, y).metaId=(short)value;
    }
    public void  lighting (int x,int y,int value){
        getWorldBlock( x, y).lighting=(byte)value;
    }    
    public void  lightR (int x,int y,int value){
        getWorldBlock( x, y).lightR=(byte)value;
    }
    public void  lightG (int x,int y,int value){
        getWorldBlock( x, y).lightG=(byte)value;
    }
    public void  lightB (int x,int y,int value){
        getWorldBlock( x, y).lightB=(byte)value;
    }
    public void  paintR (int x,int y,int value){
        getWorldBlock( x, y).paintR=(byte)value;
    }
    public void  paintG (int x,int y,int value){
        getWorldBlock( x, y).paintG=(byte)value;
    }
    public void  paintB (int x,int y,int value){
        getWorldBlock( x, y).paintB=(byte)value;
    }
    public void  foreground (int x,int y,int value){
        getWorldBlock( x, y).foreground=(short)value;
    }
    public void  spriteX (int x,int y,int value){
        getWorldBlock( x, y).spriteX=(byte)value;
    }
    public void  spriteY (int x,int y,int value){
        getWorldBlock( x, y).spriteY=(byte)value;
    }
    public void  paintColor (int x,int y,Color value){
        getWorldBlock( x, y).paintColor=value;
    }

=======
	public static int vegTypes = 11;
	public static Random rand = new Random();
	public static String group = "testWorld";
	public static int worldID = 0;
	
	public int size_x = 17000; //27000
	public int size_y = 2400;
	
	public short[][] world = new short[size_x][size_y];
	public short[][] destruction = new short[size_x][size_y];
	public byte[][] xSlot = new byte[size_x][size_y];
	public byte[][] ySlot = new byte[size_x][size_y];
	public short[][] wire = new short[size_x][size_y];
	public byte[][] direction = new byte[size_x][size_y];
	public byte[][] slopeStyle = new byte[size_x][size_y];
	public short[][] wall = new short[size_x][size_y];
	public short[][] liquid = new short[size_x][size_y];
	public byte[][] liquidDepth = new byte[size_x][size_y];
	public int[][] metaId = new int[size_x][size_y];
	public double[][] altMetaId = new double[size_x][size_y];
	public byte[][] lighting = new byte[size_x][size_y];
	public byte[][] lightR = new byte[size_x][size_y];
	public byte[][] lightG = new byte[size_x][size_y];
	public byte[][] lightB = new byte[size_x][size_y];
	public byte[][] paintR = new byte[size_x][size_y];
	public byte[][] paintG = new byte[size_x][size_y];
	public byte[][] paintB = new byte[size_x][size_y];
	public short[][] foreground = new short[size_x][size_y];
	public short[][] spriteX = new short[size_x][size_y];
	public short[][] spriteY = new short[size_x][size_y];
	public short[][] biome = new short[size_x][size_y];
	public byte[][] altSprite = new byte[size_x][size_y];
	public byte[][] animationFrame = new byte[size_x][size_y];
	public Color[][] paintColor = new Color[size_x][size_y];
	
	public Map<String, WorldTask> oreTable = new HashMap<String, WorldTask>(); //This table contains all the world generator tasks
	
	
	public void save(String name)
	{
		try 
		{
			OutputStream file = new FileOutputStream(name + ".wld");
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			output.writeObject(world);
			output.writeObject(destruction);
			output.writeObject(xSlot);
			output.writeObject(ySlot);
			output.writeObject(wire);
			output.writeObject(direction);
			output.writeObject(slopeStyle);
			output.writeObject(wall);
			output.writeObject(liquid);
			output.writeObject(liquidDepth);
			output.writeObject(metaId);
			output.writeObject(altMetaId);
			output.writeObject(lighting);
			output.writeObject(lightR);
			output.writeObject(lightG);
			output.writeObject(lightB);
			output.writeObject(paintR);
			output.writeObject(paintG);
			output.writeObject(paintB);
			output.writeObject(foreground);
			output.writeObject(spriteX);
			output.writeObject(spriteY);
			output.writeObject(biome);
			output.writeObject(altSprite);
			output.writeObject(animationFrame);
			output.writeObject(paintColor);
			output.close();
		}
		catch(IOException ex){
		}
>>>>>>> origin/rangerofthewest

		//deserialize the quarks.ser file
	}
	
	public static boolean isEmpty(int x, int y, World w, boolean pos, boolean left, boolean right, boolean top, boolean bottom)
	{
		return ((w.world[x][y] == 0) == pos && (w.world[x - 1][y] == 0) == left && (w.world[x + 1][y] == 0) == right && (w.world[x][y - 1] == 0) == top && (w.world[x][y + 1] == 0) == bottom);
	}
	
	public static RenderTile calcRender(int x, int y, World w)
	{
		if (isEmpty(x, y, w, false, true, true, true, true)) { return RenderTile.allBorder; }
		else if (isEmpty(x, y, w, false, true, false, false, false)) { return RenderTile.leftBorder; }
		else if (isEmpty(x, y, w, false, false, true, false, false)) { return RenderTile.rightBorder; }
		else if (isEmpty(x, y, w, false, false, false, true, false)) { return RenderTile.topBorder; }
		else if (isEmpty(x, y, w, false, false, false, false, true)) { return RenderTile.bottomBorder; }
		else if (isEmpty(x, y, w, false, true, false, true, false)) { return RenderTile.topLeftBorder; }
		else if (isEmpty(x, y, w, false, false, true, true, false)) { return RenderTile.topRightBorder; }
		else if (isEmpty(x, y, w, false, true, false, false, true)) { return RenderTile.bottomLeftBorder; }
		else if (isEmpty(x, y, w, false, false, true, false, true)) { return RenderTile.bottomRightBorder; }
		else { return RenderTile.noBorder; }
	}
	
	public void init()
	{
		//Set up the ore tables
		Util.print("Calculating world gen tasks...");
		
		oreTable.put("GenCopperSurface", new WorldTask(3, 9, 4, 800, 1230, 250, this));
		oreTable.put("GenStone", new WorldTask(6, 18, 3, 800, 1730, 850, this));
		oreTable.put("GenStoneMore", new WorldTask(6, 20, 3, 800, 1100, 750, this));
		oreTable.put("GenDirtDeep", new WorldTask(6, 24, 1, 1100, 2200, 755, this));
		oreTable.put("GenAirPocket", new WorldTask(10, 30, 0, 850, 2200, 2055, this));
		oreTable.put("GenSmallCave", new WorldTask(70, 270, 0, 825, 2200, 3255, this));
		oreTable.put("GenMediumCave", new WorldTask(200, 670, 0, 825, 2200, 4555, this));
		oreTable.put("GenLargeCave", new WorldTask(700, 1470, 0, 825, 2200, 7555, this));
		oreTable.put("GenExtraLargeCave", new WorldTask(1700, 2470, 0, 800, 2200, 8555, this));
		oreTable.put("GenUltraLargeCave", new WorldTask(3500, 6770, 0, 800, 2200, 16555, this));
		oreTable.put("GenIron", new WorldTask(4, 9, 5, 810, 1290, 975, this));
		oreTable.put("GenCoal", new WorldTask(3, 16, 35, 810, 1590, 775, this));
		oreTable.put("GenSilver", new WorldTask(3, 10, 6, 860, 1350, 1280, this));
		oreTable.put("GenMegaIron", new WorldTask(8, 19, 5, 840, 1330, 1415, this));
		oreTable.put("GenMegaSilver", new WorldTask(7, 20, 6, 880, 1410, 1935, this));
		oreTable.put("GenGold", new WorldTask(5, 28, 7, 860, 1990, 1960, this));
		oreTable.put("GenNaktiite", new WorldTask(1, 4, 67, 1500, 2250, 7555, this));
		
		try 
		{
			genWorld();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void placeTile(Tile t, int x, int y)
	{
		t.onPlaceTile(x, y, this);
	}
	public static List<Vector2> wizardTowerList = new ArrayList<Vector2>();
	public static List<Vector2> naktiiteCave = new ArrayList<Vector2>();
	
	public static int defaultLight = 0;
	public static int deviationLight = 5;
	//Now we do the world generator task
	public static short currBiome = Biome.Forest;
	public static int biomeSize = 0;
	public void genWorld() throws FileNotFoundException, UnsupportedEncodingException
	{
		for (int x = 0; x < size_x; x++)
		{
			for (int y = 0; y < size_y; y++)
			{
				short oldBiome = currBiome;
				slopeStyle[x][y] = 0; //Typical
				xSlot[x][y] = 0; 
				ySlot[x][y] = 0;
				wire[x][y] = -1; //No wire
				destruction[x][y] = 0;
				wall[x][y] = -1;
				liquid[x][y] = -1;
				liquidDepth[x][y] = 0;
				direction[x][y] = 0; 
				metaId[x][y] = -1;
				altMetaId[x][y] = -1;
				lighting[x][y] = (byte) defaultLight;
				lightR[x][y] = (byte) 255;
				lightG[x][y] = (byte) 255;
				lightB[x][y] = (byte) 255;
				paintR[x][y] = (byte) 255;
				paintG[x][y] = (byte) 255;
				paintB[x][y] = (byte) 255;
				foreground[x][y] = 0;
				spriteX[x][y] = 0;
				spriteY[x][y] = 0;
				biome[x][y] = currBiome;
				altSprite[x][y] = 0;
				animationFrame[x][y] = 0;
				if (y <= size_y / 3)
				{
					world[x][y] = 0; //Air
				}
				if (currBiome == Biome.Forest)
				{
					if (y > size_y / 3 && y <= (size_y / 3) + (size_y / 8))
					{
						world[x][y] = 1; //Dirt
					}
					if (y > (size_y / 3) + (size_y / 8))
					{
						world[x][y] = 2; //Stone
						biome[x][y] = Biome.Underground;
					}
				}
				if (currBiome == Biome.Snow)
				{
					if (y > size_y / 3 && y <= (size_y / 3) + (size_y / 8))
					{
						if (rand.nextInt(5) == 0)
						{
							world[x][y] = 161;
						}
						else
						{
							world[x][y] = 162;
						}
					}
					if (y > (size_y / 3) + (size_y / 8))
					{
						world[x][y] = 2; //Stone
						biome[x][y] = Biome.Underground;
					}
				}
				if (currBiome == Biome.Desert)
				{
					if (y > size_y / 3 && y <= (size_y / 3) + (size_y / 8))
					{
						if (rand.nextInt(5) == 0)
						{
							world[x][y] = 163;
						}
						else
						{
							world[x][y] = 30;
						}
					}
					if (y > (size_y / 3) + (size_y / 8))
					{
						world[x][y] = 2; //Stone
						biome[x][y] = Biome.Underground;
					}
				}
				if (rand.nextInt(size_x / 8) == 0 && biomeSize > size_x / 25)
				{
					while (!Biome.isBiomeAllowed(currBiome))
					{
						currBiome = (short) rand.nextInt(Biome.maxBiomes);	
					}
				}
				if (currBiome != oldBiome)
				{
					biomeSize = 0;
				}
			}
			biomeSize++;
			Util.print("Initializing world: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		//Now we add grass
		for (int x = 0; x < size_x; x++)
		{
			//world[x][size_y / 3] = 3; //Grass
			if (currBiome == Biome.Forest)
			{
				Util.print("Grassifying: " + ((((float)x + 1) / size_x) * 100) + "%");
			}
		}
		//Holes are next, adding some texture to the terrain
		for (int x = 0; x < size_x; x++)
		{
			if (rand.nextInt(6) == 0)
			{
				if (rand.nextBoolean())
				{
					world[x][(size_y / 3) + 1] = 0; 
				}
				else
				{
					//world[x][(size_y / 3) - 1] = 3;
					world[x][(size_y / 3)] = world[x][(size_y / 3) + 1]; 
				}
			}
			Util.print("Making divots and bumps: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		//Now we create some raised portions
		boolean flagElevate = false;
		for (int x = 0; x < size_x; x++)
		{
			if (rand.nextInt(6) == 0)
			{
				flagElevate = !flagElevate;
			}
			if (flagElevate)
			{
				world[x][(size_y / 3) - 1] = world[x][(size_y / 3)];
				world[x][(size_y / 3)] = world[x][(size_y / 3) + 1]; 
			}
			Util.print("Making raised terrain: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		//Generate some blobs of stone on surface
		for (int x = 0; x < size_x; x++)
		{
			if (rand.nextInt(27) == 0)
			{
				new WorldBlob(8, 27, 2, -1).gen(this, x, (size_y / 3));
			}
			Util.print("Making stone blobs: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		//After generating stone blobs, do the ore tables!
		boolean executed = false;
		for (int x = 0; x < size_x; x++)
		{
			for (int y = 0; y < size_y; y++)
			{
				for(Entry<String, WorldTask> e : oreTable.entrySet()) 
				{
			        WorldTask value = e.getValue();
			        if (y >= value.min_depth && y <= value.max_depth && rand.nextInt(value.weight) == 0 && !executed)
			        {
			        	//Util.print("Executing task " + e.getKey() + "!");
			        	value.execute(x, y);
			        	executed = true;
			        }
			        if (executed)
			        {
			        	break;
			        }
			    }
				executed = false;
			}
			Util.print("WorldGen Tasks: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		for (int x = 0; x < size_x; x++)
		{
			for (int y = 0; y < 20; y++)
			{
				lighting[x][795 + y] = (byte) (100 - (y * deviationLight));
				lighting[x][795 - y] = (byte) (100 - (y * deviationLight));
			}
		}
		for (int x = 1; x < size_x - 1; x++)
		{
			for (int y = 810; y < size_y - 275; y++)
			{
				if ((world[x + 1][y] == 0 && world[x - 1][y] == 0 && world[x][y + 1] == 0 && world[x][y - 1] == 0) || (rand.nextInt(24) == 0 && (world[x + 1][y] == 0 || world[x - 1][y] == 0 || world[x][y + 1] == 0 || world[x][y - 1] == 0)))
				{
					world[x][y] = 0;
				}
			}
			Util.print("Smoothing underworld: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		for (int x = 1; x < size_x - 1; x++)
		{
			for (int y = 810; y < size_y - 275; y++)
			{
				if (world[x + 1][y] == 0 && world[x - 1][y] == 0 && world[x][y + 1] == 0 && world[x][y - 1] == 0)
				{
					world[x][y] = 0;
				}
			}
			Util.print("Smoothing underworld again: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		//Now that we did the world gen tasks, add some terrain variance... for real
		for (int x = 0; x < size_x; x++)
		{
			for (int y = 0; y < size_y; y++)
			{
				if (y < 805 && y > 801)
				{
					if (rand.nextInt(4) == 0)
					{
						if (rand.nextInt(2) == 0)
						{
							world[x][y] = 0;
						}
						else
						{
							world[x][y] = 1;
						}
					}
				}
			}
			Util.print("Scrambling terrain: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		//All world generators here
		
		new WorldHell(2250).generate(this); //Start generating hell at 2250 Y
		for (int x = 0; x < size_x; x++)
		{
			int y = 900;
			while (world[x][y] != 0 && y > 730)
			{
				y--;
			}
			world[x][y + 1] = 3;
			if (world[x][y + 1] == 1 || world[x][y + 1] == 3)
			{
				world[x][y] = (short) (15 + rand.nextInt(World.vegTypes));
			}
			Util.print("Adding vegetation: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		for (int x = 0; x < size_x; x++)
		{
			for (int y = 800; y < size_y; y++)
			{
				if (rand.nextFloat() <= 1.0f)
				{
					setSlope(x, y);
				}
			}
			Util.print("Adding slopes: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		for (int x = 0; x < size_x; x++)
		{
			for (int y = 800; y < 2250; y++)
			{
				if (y >= 820)
				{
					if (y > (size_y / 3) + (size_y / 8))
					{
						wall[x][y] = 2;
					}
					else
					{
						if (biome[x][y] == Biome.Forest)
						{
							wall[x][y] = 1;
						}
						else if (biome[x][y] == Biome.Snow)
						{
							if (rand.nextInt(5) == 0)
							{
								wall[x][y] = 161;
							}
							else
							{
								wall[x][y] = 162;
							}
						}
						else if (biome[x][y] == Biome.Desert)
						{
							wall[x][y] = 30;
						}
					}
				}
				else
				{
					if (world[x][y] == 0 || slopeStyle[x][y] > 0)
					{
						if (y > (size_y / 3) + (size_y / 8))
						{
							wall[x][y] = 2;
						}	
						else
						{
							if (biome[x][y] == Biome.Forest)
							{
								wall[x][y] = 1;
							}
							else if (biome[x][y] == Biome.Snow)
							{
								if (rand.nextInt(5) == 0)
								{
									wall[x][y] = 161;
								}
								else
								{
									wall[x][y] = 162;
								}
							}
							else if (biome[x][y] == Biome.Desert)
							{
								wall[x][y] = 30;
							}
						}
					}
				}
			}
			Util.print("Adding backwalls: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		for (int x = 0; x < size_x - 5; x++)
		{
			for (int y = 750; y < 1100; y++)
			{
				if (rand.nextFloat() <= 0.00005f)
				{
					new WorldGemAltar((byte)rand.nextInt(8), new Vector2(x, y)).generate(this);
				}
			}
			Util.print("Adding gem altars: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		for (int x = 1; x < size_x - 20; x++)
		{
			for (int y = 0; y < 1100; y++) //above rock layer
			{
				if (rand.nextBoolean() || y > 805)
				{
					if (isValidPot(x, y + 1) && rand.nextFloat() < 0.65f)
					{
						if (rand.nextBoolean())
						{
							Tile.tiles[32].onPlaceTile(x, y + 1, this);
						}
						else
						{
							Tile.tiles[33].onPlaceTile(x, y + 1, this);
						}
					}
				}
				else
				{
					if (isValidDecoration(x, y) && rand.nextFloat() < 0.925f && Tile.tiles[world[x][y + 1]].solid && noPot(x, y))
					{
						genTree(x, y);
					}
				}
			}
			Util.print("Adding breakables: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		for (int x = 0; x < size_x; x++)
		{
			for (int y = 0; y < size_y; y++)
			{
				try
				{
					if ((world[x][y - 1] == 128 || world[x][y] == 128 || foreground[x][y - 1] == 130) && world[x][y] == 0 && world[x][y + 1] == 0 && rand.nextFloat() <= 0.94f)
					{
						foreground[x][y] = 130;
					}
				}
				catch (Exception e)
				{
					
				}
			}
			Util.print("Adding vines: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		{
			int y = 900;
			for (int x = 1; x < size_x - 20; x++)
			{
				while (world[x][y] != 0 && y > 730)
				{
					y--;
				}
				if (rand.nextInt(25000) == 0 && wizardTowerList.size() < 3)
				{
					new WorldWizardTower((byte)rand.nextInt(8), new Vector2(x, y)).generate(this);
					//Sys.alert("Wizard Tower", "A wizard tower spawned at " + x + "/" + y);
					wizardTowerList.add(new Vector2(x, y));
				}
				Util.print("Adding wizard towers: " + ((((float)x + 1) / size_x) * 100) + "%");
			}	
		}
		new WorldSteampunk(15000, 800).gen(this);
		while (wizardTowerList.size() == 0)
		{
			int y = 900;
			Util.print("Wizard towers not added!");
			for (int x = 1; x < size_x - 20; x++)
			{
				while (world[x][y] != 0 && y > 730)
				{
					y--;
				}
				if (rand.nextInt(25000) == 0 && wizardTowerList.size() < 3)
				{
					new WorldWizardTower((byte)rand.nextInt(8), new Vector2(x, y)).generate(this);
					//Sys.alert("Wizard Tower", "A wizard tower spawned at " + x + "/" + y);
					wizardTowerList.add(new Vector2(x, y));
				}
				Util.print("Re-adding wizard towers: " + ((((float)x + 1) / size_x) * 100) + "%");
			}
		}
		for (int x = 0; x < size_x - 30; x++)
		{
			for (int y = 0; y < size_y - 900; y++)
			{
				if (rand.nextInt(1000000) == 0)
				{
					//new WorldNaktiiteCave(x, y).generate(this);
					//naktiiteCave.add(new Vector2(x, y));
				}
			}
			Util.print("Adding naktireen hives: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		new WorldNaktiiteCave(15000, 800).generate(this);
		for (int x = 1; x < size_x - 20; x++)
		{
			for (int y = 2250; y < size_y - 5; y++) //in hell
			{
				if (isValidPot(x, y + 1) && rand.nextFloat() < 0.15f)
				{
					Tile.tiles[34].onPlaceTile(x, y + 1, this);
				}
			}
			Util.print("Adding hell breakables: " + ((((float)x + 1) / size_x) * 100) + "%");
		}
		for (int x = 0; x < size_x; x++)
		{
			for (int y = 0; y < size_y; y++)
			{
				if (world[x][y] >= 15 && world[x][y] <= 15 + (World.vegTypes - 1))
				{
					if (world[x][y + 1] != 1 && world[x][y + 1] != 3)
					{
						world[x][y] = 0;
					}
				}
				if (world[x][y] == 3 && Tile.tiles[world[x][y - 1]].solid)
				{
					world[x][y] = 1;
				}
			}
			Util.print("Trimming vegetation: " + ((((float)x + 1) / size_x) * 100) + "%");
			
		}
		//new WorldDungeon(15000, 800).generate(this);
		for (int a = 0; a < wizardTowerList.toArray().length; a++)
		{
			Util.print(wizardTowerList.get(a).X + "/" + wizardTowerList.get(a).Y + " (Wizard Tower)");
		}
		Util.print(wizardTowerList.toArray().length + " wizard tower(s) spawned");
		//Tile.tiles[123].onPlaceTile(15000, 500, this);
		//Util.print(naktiiteCave.toArray().length + " naktiite hive(s) spawned");
		
		//Tree gen
		//Now for saving
		//saveWorld("world1.dat");
	}
	
	public boolean isPot(int x, int y)
	{
		return world[x][y] == 32 || world[x][y] == 33 || world[x][y] == 34;
	}
	
	public boolean noPot(int x, int y)
	{
		return !(isPot(x - xSlot[x][y], y - ySlot[x][y]) || isPot(x - xSlot[x][y] + 1, y - ySlot[x][y]) || isPot(x - xSlot[x][y] + 1, y - ySlot[x][y] + 1) || isPot(x - xSlot[x][y], y - ySlot[x][y] + 1));
	}
	
	public void explode(int x, int y, double radius)
	{
		for (int i = (x - (int)(radius / 2)); i < x + radius; i++)
		{
			for (int j = (y - (int)(radius / 2)); j < y + radius; j++)
			{
				if (Math.sqrt((double)Math.pow((x - i), 2) + (double)Math.pow((y - j), 2)) <= radius)
				{
					Tile.tiles[world[i][j]].onKillTile(i, j, this);
					if (rand.nextBoolean())
					{
						wall[i][j] = -1;
					}
				}
			}
		}
	}
	
	public boolean isValidPot(int x, int y)
	{
		//x and y signify top left corner
		return world[x][y + 1] != 128 && world[x + 1][y + 1] != 128 && isValidDecorationIgnoreVeg(x, y + 2) && isValidDecorationIgnoreVeg(x + 1, y + 2) && world[x][y] == 0 && world[x + 1][y] == 0 && world[x][y + 1] == 0 && world[x + 1][y + 1] == 0 && world[x - 1][y] == 0 && world[x - 1][y + 1] == 0 && world[x + 2][y + 1] == 0 && world[x + 2][y] == 0 && liquid[x][y] == -1 && liquid[x + 1][y] == -1 && liquid[x][y + 1] == -1 && liquid[x + 1][y + 1] == -1 && liquid[x - 1][y] == -1 && liquid[x - 1][y + 1] == -1 && liquid[x + 2][y + 1] == -1 && liquid[x + 2][y] == -1;
	}
	
	public boolean isDecoration(int x, int y)
	{
		return (world[x][y] >= 26 && world[x][y] < 26 + Tile.decTypes);
	
	}
	
	public boolean isValidDecorationIgnoreVeg(int x, int y)
	{
		return (world[x][y] != 128 && Tile.tiles[world[x][y]].solid && slopeStyle[x][y] != 3 && slopeStyle[x][y] != 4);
	}
	
	//measures if the space above is a valid spot for a decoration
	public boolean isValidDecoration(int x, int y)
	{
		return world[x][y] != 128 && (Tile.tiles[world[x][y]].solid && world[x][y - 1] == 0 && !isVegetation(x, y)  && !isDecoration(x, y) && slopeStyle[x][y] != 3 && slopeStyle[x][y] != 4);
	}
	
	public boolean isVegetation(int x, int y)
	{
		return (world[x][y] >= 15 && world[x][y] < 15 + vegTypes);
	}
	
	public void setSlope(int x, int y)
	{
		try
		{
			if (rand.nextInt(1) == 0 && !isVegetation(x, y) && Tile.tiles[world[x][y]].canSlope)
			{
				if (rand.nextFloat() < 0.99f)
				{
					if (world[x][y - 1] != 0 && world[x - 1][y] == 0 && world[x + 1][y] != 0 && world[x][y + 1] == 0)
					{
						slopeStyle[x][y] = 1;
					}
					if (world[x][y - 1] != 0 && world[x - 1][y] != 0 && world[x + 1][y] == 0 && world[x][y + 1] == 0)
					{
						slopeStyle[x][y] = 2;
					}
				}
				else
				{
					if (world[x + 1][y] == 0 || world[x - 1][y] == 0)
					{
						//slopeStyle[x][y] = 5;
					}	
				}
				//the up-slopes are only if there is no "vegetation"
				if (!isVegetation(x, y - 1))
				{
					if (rand.nextFloat() < 0.99f)
					{
						if (world[x][y - 1] == 0 && world[x - 1][y] != 0 && world[x + 1][y] == 0 && world[x][y + 1] != 0)
						{
							slopeStyle[x][y] = 4;
						}
						if (world[x][y - 1] == 0 && world[x - 1][y] == 0 && world[x + 1][y] != 0 && world[x][y + 1] != 0)
						{
							slopeStyle[x][y] = 3;
						}
					}
					else
					{
						if (world[x + 1][y] == 0 || world[x - 1][y] == 0)
						{
							//slopeStyle[x][y] = 6;
						}	
					}
				}
			}
		}
		catch (Exception e)
		{
			slopeStyle[x][y] = 0;
		}
	}
	
	public void genTree(int x, int y)
	{
		int h = rand.nextInt(16) + 8;
		boolean mega = rand.nextFloat() <= 0.01f;
		boolean megaWide = false;
		byte layer = (byte)rand.nextInt(3);
		
		if (mega)
		{
			h *= rand.nextInt(10) + rand.nextInt(6) + 8;
			megaWide = rand.nextBoolean();
		}
		if (layer == 0)
		{
			for (int a = 0; a < h; a++)
			{
				world[x][y - a] = 127;
				slopeStyle[x][y - a] = 0;
				if (mega)
				{
					if (a < h * 0.5)
					{
						world[x + 1][y - a] = 127;
						world[x - 1][y - a] = 127;
						if (a < h * 0.33 && megaWide)
						{
							world[x + 2][y - a] = 127;
							world[x - 2][y - a] = 127;
							if (a < h * 0.25)
							{
								world[x + 3][y - a] = 127;
								world[x - 3][y - a] = 127;
							}
						}
					}
				}
			}
			int i = x;
			int j = y - h;  
			try
			{
				world[i][j] = 128;
				world[i + 1][j] = 128;
				world[i + 2][j] = 128;
				world[i - 1][j] = 128;
				world[i - 2][j] = 128;
				world[i][j - 1] = 128;
				world[i][j - 2] = 128;
				world[i + 1][j - 1] = 128;
				world[i + 2][j - 1] = 128;
				world[i - 1][j - 1] = 128;
				world[i - 2][j - 1] = 128;
				world[i + 1][j - 2] = 128;
				world[i + 2][j - 2] = 128;
				world[i - 1][j - 2] = 128;
				world[i - 2][j - 2] = 128;
				
			}
			catch (Exception e)
			{
				
			}
		}
		
		if (layer == 1)
		{
			for (int a = 0; a < h; a++)
			{
				wall[x][y - a] = 127;
				slopeStyle[x][y - a] = 0;
				if (mega)
				{
					if (a < h * 0.5)
					{
						wall[x + 1][y - a] = 127;
						wall[x - 1][y - a] = 127;
						if (a < h * 0.33 && megaWide)
						{
							wall[x + 2][y - a] = 127;
							wall[x - 2][y - a] = 127;
							if (a < h * 0.25)
							{
								wall[x + 3][y - a] = 127;
								wall[x - 3][y - a] = 127;
							}
						}
					}
				}
			}
			int i = x;
			int j = y - h;  
			try
			{
				wall[i][j] = 128;
				wall[i + 1][j] = 128;
				wall[i + 2][j] = 128;
				wall[i - 1][j] = 128;
				wall[i - 2][j] = 128;
				wall[i][j - 1] = 128;
				wall[i][j - 2] = 128;
				wall[i + 1][j - 1] = 128;
				wall[i + 2][j - 1] = 128;
				wall[i - 1][j - 1] = 128;
				wall[i - 2][j - 1] = 128;
				wall[i + 1][j - 2] = 128;
				wall[i + 2][j - 2] = 128;
				wall[i - 1][j - 2] = 128;
				wall[i - 2][j - 2] = 128;
				
			}
			catch (Exception e)
			{
				
			}
		}
		
		if (layer == 2)
		{
			for (int a = 0; a < h; a++)
			{
				foreground[x][y - a] = 127;
				slopeStyle[x][y - a] = 0;
				if (mega)
				{
					if (a < h * 0.5)
					{
						foreground[x + 1][y - a] = 127;
						foreground[x - 1][y - a] = 127;
						if (a < h * 0.33 && megaWide)
						{
							foreground[x + 2][y - a] = 127;
							foreground[x - 2][y - a] = 127;
							if (a < h * 0.25)
							{
								foreground[x + 3][y - a] = 127;
								foreground[x - 3][y - a] = 127;
							}
						}
					}
				}
			}
			int i = x;
			int j = y - h;  
			try
			{
				foreground[i][j] = 128;
				foreground[i + 1][j] = 128;
				foreground[i + 2][j] = 128;
				foreground[i - 1][j] = 128;
				foreground[i - 2][j] = 128;
				foreground[i][j - 1] = 128;
				foreground[i][j - 2] = 128;
				foreground[i + 1][j - 1] = 128;
				foreground[i + 2][j - 1] = 128;
				foreground[i - 1][j - 1] = 128;
				foreground[i - 2][j - 1] = 128;
				foreground[i + 1][j - 2] = 128;
				foreground[i + 2][j - 2] = 128;
				foreground[i - 1][j - 2] = 128;
				foreground[i - 2][j - 2] = 128;
				
			}
			catch (Exception e)
			{
				
			}
		}
	}
	public boolean isCalcLighting(int i, int j)
	{
		if (j >= 795 && j <= 815)
		{
			return !(lighting[i][j] == (byte) (Math.abs(815 - j) * deviationLight));
		}
		else if (j >= 775 && j <= 795)
		{
			return !(lighting[i][j] == (byte) (Math.abs(795 - j) * deviationLight));
		}
		else
		{
			return !(lighting[i][j] == 0);
		}
	}
	//@SuppressWarnings("unused")
	
	int sand_counter = 0;
	int fire_counter = 0;
	int animation_counter = 0;
	public void checkChunks(int x, int y)
	{
		sand_counter++;
		fire_counter++;
		animation_counter++;
		for (int i = x - 100; i < x + 100; i++)
		{
			for (int j = y + 100; j >= y - 100; j--)
			{ 
				try
				{
					//Update animations first
					if (animation_counter % Tile.tiles[world[i][j]].animationLength == 0 && Tile.tiles[world[i][j]].animationFrames > 0)
					{
						animationFrame[i][j]++;
						if (animationFrame[i][j] > Tile.tiles[world[i][j]].animationFrames - 1)
						{
							animationFrame[i][j] = 0;
						}
					}
					if (animation_counter > 10000)
					{
						animation_counter = 0;
					}
					
					//Now the rest of the stuff
					if ((liquid[i][j] >= 0 && liquidDepth[i][j] < 1) || liquid[i][j] == -1)
					{
						liquid[i][j] = -1;
						liquidDepth[i][j] = 0;
					}
					if (liquid[i][j] >= 0)
					{
						Liquid.calcPhysics(i, j, this);
					}
					if (world[i][j] == 157 && metaId[i][j] > -1 && Main.player.survival)
					{
						if (fire_counter > 45)
						{
							fire_counter = 0;
							altMetaId[i][j] -= 0.03;
						}
						metaId[i][j]--;
						if (metaId[i][j] == 0 || altMetaId[i][j] < TileCampfire.baseTemp / 2)
						{
							Tile.tiles[158].onOverwriteTile(i, j, this);
						}
					}
					if (world[i][j] == 127 && !(Tile.tiles[world[i][j + 1]].validTree || world[i][j + 1] == 127 || world[i][j + 1] == 128 || !isPot(i, j)))
					{
						Tile.tiles[world[i][j]].onKillTile(i, j, this);
					}
					if (wall[i][j] == 127 && !(Tile.tiles[world[i][j + 1]].solid || wall[i][j + 1] == 127 || wall[i][j + 1] == 128))
					{
						Tile.tiles[wall[i][j]].onKillWall(i, j, this);
					}
					if (foreground[i][j] == 127 && !(Tile.tiles[world[i][j + 1]].solid || foreground[i][j + 1] == 127 || foreground[i][j + 1] == 128))
					{
						Tile.tiles[foreground[i][j]].onKillFore(i, j, this);
					}
					if (i >= 0 && i < size_x && j >= 0 && j < size_y)
					{
						if (Tile.tiles[world[i][j]].gravity && !Tile.tiles[world[i][j + 1]].solid && sand_counter > 4)
						{
							world[i][j + 1] = world[i][j];
							world[i][j] = 0;
						}
						if (isDecoration(i, j) && !isValidDecoration(i, j + 1))
						{
							world[i][j] = 0;
						}	
						if (Math.sqrt(Math.pow(i - Main.player.pos.X, 2) + Math.pow(j - Main.player.pos.Y, 2)) < 1.4)
						{
							//Tile.tiles[world[i][j]].onCollide(i, j);
							if (world[i][j] == 31)
							{
								world[i][j] = 0;
								this.explode(i, j, rand.nextInt(4) + 5);
							}
						}
					}
				}
				catch (Exception e)
				{
					
				}
			}
		}
		sand_counter = 0;
	}
	
	
	public void saveWorld(String name)
	{
		File dir = new File(group);
		dir.mkdir();
		File save = new File(group + "/" + World.worldID + name);
		try {
			save.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter writer = null;
		PrintWriter writer2 = null;
		//PrintWriter writer3 = null;
		try {
			writer = new PrintWriter(name + "/" + name, "UTF-8");
			//writer2 = new PrintWriter(name + "/" + "thick_" + name, "UTF-8");
			writer2 = new PrintWriter(name + "/" + "wire_" + name, "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int y = 0; y < size_y; y++)
		{
			for (int x = 0; x < size_x; x++)
			{
				writer.print(world[x][y] + "\n");
				//writer2.print(thickness[x][y] + "\n");
				writer2.print(wire[x][y] + "\n");
			}
			//\n
			writer.print("\n");
			Util.print("Saving World: " + ((((float)y + 1) / size_y) * 100) + "%");
		}
		Util.print("RAM Used: " + (((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())) / 1024 / 1024) + " MB");
		writer.close();
		writer2.close();
	}
}

