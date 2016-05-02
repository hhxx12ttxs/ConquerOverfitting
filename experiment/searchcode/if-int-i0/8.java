/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg30dungeon;

import java.awt.Color;
import java.awt.Graphics;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Akos
 */
public class World implements Drawable {
    
    private TileMap map;
  //  private List<Entity> beasts;
    
    private CreatureCollection creatueCollection;
    private ItemCollection itemCollection;
    private TaskManager taskManager;
    
    private CreatureFactory creatureFactory;
    private TaskFactory taskFactory; 
    private ItemFactory itemFactory;
    
    private PathFinder pathfinder;
    
    private Random gen;
    
    private int money;
    
    World()
    { 
       creatureFactory = new CreatureFactory();
       taskFactory = new TaskFactory();
       itemFactory = new ItemFactory();
       
        gen = new Random();
       
       map = new TileMap();
       
       map.getTile(0, 0).switchType(3);
       map.exploreMap(0, 0, true);
     //  map.getTile(0, 1).setWalkable(true);
       
       pathfinder = new PathFinder(map);
       
       creatueCollection = new CreatureCollection();
       itemCollection = new ItemCollection();
       taskManager = new TaskManager(this);
       
       money = 1000;
       
    //   addEntity(0, 0, AIenum.gangmember);
    //    addEntity(0, 2, AIenum.gangmember);
        addEntity(0, 3, AIenum.gangmember);
    }

    
    public void addItem(int y, int x, String name)
    {
    
      //  if(getTile(y,x).isExplored()==false || getTile(y,x).isWalkable()==false)
      //      return;
        
        if(getTile(y, x).getItem().isEmpty())
        {
        int nid = getItemfactory().getNextid();
        Item item = ItemFactory.newItem(y, x, getTile(y,x), name); 
        if(item==null) return;
        
        this.getItemcollection().things.put(nid,item);
        
        getMap().getTile(y, x).getItem().add(nid);
        }
        
    }
    
    public void addRandomEntity(AIenum type)
    {
        int ty=0;
        int tx=0;
        if(type==AIenum.gangmember)
        do{
         ty = getGen().nextInt(getMap().getHeight());
         tx = getGen().nextInt(getMap().getWidth());
        }
        while(getMap().getTile(ty, tx).getTiletype()!=3);
        
        if(type==AIenum.annoybug)
        do{
         ty = getGen().nextInt(getMap().getHeight());
         tx = getGen().nextInt(getMap().getWidth());
        }
        while(getMap().getTile(ty, tx).getTiletype()!=2);
        
        
       addEntity(ty,tx, type);
       
    }
    
    void addEntity(int ty, int tx, AIenum type)
    {
      //beasts.add(entfactory.newEntity(ty, tx));
        
        int nid = getEntfactory().getNextid();
        Creature c =  CreatureFactory.newEntity(ty, tx, type); 
        getBeastcollection().beasts.put(nid,c);
        
        getMap().getTile(ty, tx).getCreature().add(nid);
    }
    
    
    
    /*
     * Paints the Level
     */
    @Override
     public void draw(Graphics g, Viewport vport) 
    {
        int tilesize = vport.getTilesize();
        
        int tiley = vport.getHeight()/tilesize;
        int tilex = vport.getWidth()/tilesize;
        
        if(tiley>getMap().getHeight()) tiley = getMap().getHeight();
         if(tilex>getMap().getWidth()) tilex = getMap().getWidth();
        
       for(int i = 0; i<tiley; i++)
       {
           for(int j = 0; j<tilex; j++)
           {
             int offi = i + vport.getOffy(); 
             int offj = j  + vport.getOffx(); 
             
            if(offi>getMap().getHeight()) continue;
            if(offj>getMap().getWidth()) continue;
               
        /*  switch(  getMap().getTile(offi, offj).getTiletype())
           {
              case 0: g.setColor(Color.black); break;
              case 1: g.setColor(Color.DARK_GRAY); break;
              case 2: g.setColor(Color.gray); break;
              case 3: g.setColor(Color.GREEN); break;
           }*/
          
          if(getMap().getTile(offi, offj).isExplored()==false)
          { 
              g.setColor(Color.black);
               g.fillRect(j*tilesize, i*tilesize, tilesize, tilesize);
          }
          else
          {
              String s;
          switch(  getMap().getTile(offi, offj).getTiletype())
           {
              case 0:  s="rockwall";  
              g.drawImage(ImageRegistry.getImage(s), j*tilesize, i*tilesize, (j+1)*tilesize, (i+1)*tilesize, 0, 0, 50, 50, null); 
              break;
              case 1:  s="woodwall";
              g.drawImage(ImageRegistry.getImage(s), j*tilesize, i*tilesize, (j+1)*tilesize, (i+1)*tilesize, 0, 0, 50, 50, null); 
              break;
              case 2:  s="floor"; 
              g.drawImage(ImageRegistry.getImage(s), j*tilesize, i*tilesize, tilesize, tilesize, null);
              break;
              case 3:  s="grass"; 
              g.drawImage(ImageRegistry.getImage(s), j*tilesize, i*tilesize, tilesize, tilesize, null);
              break;
           }
          
           
          }
          
          if(getMap().getTile(offi, offj).getItem().isEmpty()==false)
          {
       //   g.drawImage(ImageRegistry.getImage("comfybed"), j*tilesize, i*tilesize, tilesize, tilesize, null); 
              for(int k = 0; k<getMap().getTile(offi, offj).getItem().size(); k++)
                  this.itemCollection.getKeyID(getMap().getTile(offi, offj).getItem().get(k)).draw(g, vport);
          }
          
           if(getMap().getTile(offi, offj).getDesignate()!=null)
          switch( getMap().getTile(offi, offj).getDesignate())
          { 
              case digging:
             g.drawImage(ImageRegistry.getImage("digdesig"), j*tilesize, i*tilesize, tilesize, tilesize, null); 
               break;
              case walling:  
               g.drawImage(ImageRegistry.getImage("walldesig"), j*tilesize, i*tilesize, tilesize, tilesize, null); 
              break;
           }
          
          
           
           
           
           }
       }
       
       
         for(Creature beast : getBeastcollection().beasts.values())
             {
              if(getTile(beast.getCy(), beast.getCx()).isExplored()==true)
               beast.draw(g, vport);
             }  
       g.setColor(Color.yellow);  
       g.drawString("MONEY: " + this.getMoney() + "@", 0, 20); 
    }
    
      /*
       * Checks if it is a space the entity can move into
       */
      boolean Checkdir(int y, int x, Directions dir)
      {
      
      switch(dir)
      {
          case N: 
              if((y-1)<0 || isBlocked(y-1, x)) return false; 
              break;
          case NE: 
              if((x+1)>(this.getWidth()-1) || (y-1)<0 || isBlocked(y-1, x+1)) return false; 
              break;
          case E: 
              if((x+1)>(this.getWidth()-1) || isBlocked(y, x+1) ) return false; 
              break;
          case SE:
              if((x+1)>(this.getWidth()-1) || (y+1)>(this.getHeight()-1) || isBlocked(y+1, x+1)) return false; 
              break;
          case S:
              if((y+1)>(this.getHeight()-1) || isBlocked(y+1, x)) return false; 
              break;
          case SW:
              if((y+1)>(this.getHeight()-1) || (x-1)<0 || isBlocked(y+1, x-1)) return false; 
              break;
          case W:
              if((x-1)<0 || isBlocked(y, x-1) ) return false;  
              break;
          case NW:
              if((y-1)<0 || (x-1)<0 || isBlocked(y-1, x-1)) return false;  
              break;
      }
      
    return true;
      
      
      }
     
      void Update()
      {
          
           for(Creature beast : getBeastcollection().beasts.values())
             {
              if(beast.isCanwork())
              this.getTaskmanager().assignTasks(beast);
              beast.update(this);
             }
      }
    
      
      void saveWorld()
      {
      List<String> sLine = new ArrayList<>();
      sLine.add(""+this.money);
      sLine.add("#ENDBLOCK#");
      this.map.saveMap(sLine);
      this.taskManager.saveTasks(sLine);
      this.creatueCollection.saveCreatures(sLine);
      this.itemCollection.saveItems(sLine);
      
              String   outpath = "Data//save.txt";
        try (PrintWriter out = new PrintWriter(new FileWriter(outpath))) {
            for(int j=0; j<sLine.size(); j++)
                        {
                out.println(sLine.get(j));
                        }
            
         out.close();
            
        }
        catch(Exception exc){  System.out.println("WRONG FILE while saving map");System.exit(0);} 
       
        
      }
      
      
      void loadWorld()
      {
      
           try{
         
     String inpath ="Data//save.txt";
            try (FileInputStream fstream = new FileInputStream(inpath)) {
                DataInputStream in = new DataInputStream(fstream);
                List<String> worldLines;
                List<String> mapLines;
                List<String> taskLines;
                List<String> creatureLines;
                List<String> itemLines;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                    List<String> readLines = new ArrayList<>();
                    String sLine;
                    while( (sLine = br.readLine()) !=null )
                  {   
                     readLines.add(sLine);
                  } worldLines = new ArrayList<>();
                    mapLines = new ArrayList<>();
                    taskLines = new ArrayList<>();
                    creatureLines = new ArrayList<>();
                    itemLines = new ArrayList<>();
                    int readcount = 0;
                    for(int i=0; i<readLines.size(); i++)
                    {
                    if("#ENDBLOCK#".equals(readLines.get(i)))
                    { 
                        readcount++; 
                        continue;
                    }
                    
                    switch(readcount)
                        {
                        case 0: worldLines.add(readLines.get(i)); break;
                        case 1: mapLines.add(readLines.get(i)); break;
                        case 2: taskLines.add(readLines.get(i)); break; 
                        case 3: creatureLines.add(readLines.get(i)); break;
                        case 4: itemLines.add(readLines.get(i)); break;    
                        }
                    
                    }
                }
                  
              this.money = Integer.valueOf(worldLines.get(0));
              
              map = null;
              map = new TileMap(mapLines); 
            
              this.taskManager = null;
              this.taskFactory = null;
              this.taskFactory = new TaskFactory();
              this.taskManager = new TaskManager(this);
              
              taskManager.loadTasks(taskLines);
              
              
              this.creatueCollection = null;
              creatueCollection = new CreatureCollection();
              this.creatureFactory = null;
              creatureFactory = new CreatureFactory();
              creatueCollection.loadCreatures(creatureLines, taskManager);
              
              
              this.itemFactory = null;
              this.itemFactory = new ItemFactory();
              this.itemCollection = new ItemCollection();
              itemCollection.loadItems(itemLines);
            }
          
     }catch(IOException | NumberFormatException e){System.out.println("Error while loading"); System.exit(0);
        }
          
          
          
      
      }
      
      
    
    /**
     * @return the width
     */
    public int getWidth() {
        return getMap().getWidth();
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        getMap().setWidth(width);
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return getMap().getHeight();
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
         getMap().setHeight(height);
    }

   public Tile getTile(int y, int x)
   {
       
   try{
   return   getMap().getTile(y, x);
   }catch(Exception e){System.out.println("Nullpointer exception in getting a tile"); System.exit(0);}
   
   return null;
   }
   
 
    int getDistance(int sy, int sx, int ty, int tx)
    {
    return Math.abs(sx-tx)+Math.abs(sy-ty);
    
    
    }

   public boolean isBlocked(int y, int x)
   { 
    return getMap().isBlocked(y, x);
   }

    /**
     * @return the map
     */
     TileMap getMap() {
        return map;
    }

    /**
     * @param map the map to set
     */
    private void setMap(TileMap map) {
        this.map = map;
    }

    /*todo: Collapse these two*/
    void designateTask(int i, int i0, TaskEnum type) {
        if(this.getTile(i, i0).getDesignate()==type)
        {
           this.getTile(i, i0).setDesignate(null);
            this.getTaskmanager().undesignateTask(this.getMap().getTile(i, i0).getActualTask(), this.getBeastcollection());
           return;
           
        }
        
        switch(type)
        {
            case digging:
       this.getTile(i, i0).designateMine();
       break;
            case walling: 
       this.getTile(i, i0).designateWall();  
        }
        
       if(this.getTile(i, i0).getDesignate()==type)//if successfully designated
       {
           
           if(this.getTile(i, i0).getDesignate()==TaskEnum.digging && this.getTile(i, i0).isWalkable()) return;
           
            if(this.getTile(i, i0).getDesignate()==TaskEnum.walling && this.getTile(i, i0).isExplored()==false) return;
           
           Task ntask = TaskFactory.newCompositeTask(-1, i, i0, type);
           this.getMap().getTile(i, i0).setActualTask(ntask.getTaskid());
            this.getTaskmanager().addTask(ntask);
       }
      } 

  
  //returns the status and description of all jobs
      String getJobs() {
       return getTaskmanager().getJobs(); 
    }

    /**
     * @return the beastcollection
     */
    public CreatureCollection getBeastcollection() {
        return creatueCollection;
    }

    /**
     * @param beastcollection the beastcollection to set
     */
    public void setBeastcollection(CreatureCollection beastcollection) {
        this.creatueCollection = beastcollection;
    }

    /**
     * @return the taskmanager
     */
    public TaskManager getTaskmanager() {
        return taskManager;
    }

    /**
     * @param taskmanager the taskmanager to set
     */
    public void setTaskmanager(TaskManager taskmanager) {
        this.taskManager = taskmanager;
    }

    /**
     * @return the entfactory
     */
    public CreatureFactory getEntfactory() {
        return creatureFactory;
    }

    /**
     * @param entfactory the entfactory to set
     */
    public void setEntfactory(CreatureFactory entfactory) {
        this.creatureFactory = entfactory;
    }

    /**
     * @return the taskfactory
     */
    public TaskFactory getTaskfactory() {
        return taskFactory;
    }

    /**
     * @param taskfactory the taskfactory to set
     */
    public void setTaskfactory(TaskFactory taskfactory) {
        this.taskFactory = taskfactory;
    }

    /**
     * @return the itemfactory
     */
    public ItemFactory getItemfactory() {
        return itemFactory;
    }

    /**
     * @param itemfactory the itemfactory to set
     */
    public void setItemfactory(ItemFactory itemfactory) {
        this.itemFactory = itemfactory;
    }

    /**
     * @return the itemcollection
     */
    public ItemCollection getItemcollection() {
        return itemCollection;
    }

    /**
     * @param itemcollection the itemcollection to set
     */
    public void setItemcollection(ItemCollection itemcollection) {
        this.itemCollection = itemcollection;
    }

    /**
     * @return the money
     */
    public int getMoney() {
        return money;
    }

    /**
     * @param money the money to set
     */
    public void setMoney(int money) {
        this.money = money;
    }

    @Override
    public void drawThing(Graphics g) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the pathfinder
     */
    public PathFinder getPathfinder() {
        return pathfinder;
    }

    /**
     * @param pathfinder the pathfinder to set
     */
    public void setPathfinder(PathFinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    /**
     * @return the gen
     */
    public Random getGen() {
        return gen;
    }

    /**
     * @param gen the gen to set
     */
    public void setGen(Random gen) {
        this.gen = gen;
    }

    

    
}

