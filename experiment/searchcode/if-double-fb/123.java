import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class BiomeTerrain
{
  private static List<BiomeTerrainBaseMod> mods = new ArrayList();
  private static HashMap<String, String> settings = new HashMap();
  private static boolean createGlobalSettingsFiles;
  private static boolean createWorldSettingsFiles;
  private static boolean createNewGlobalSettings;
  public static File minecraftFolder;
  public static File worldSaveFolder;
  public static Random rand;
  public static et world;
  public static jj currentBiome;
  public static jj[] biomeList;
  public static boolean dungeonDefault;
  public static boolean clayDefault;
  public static boolean dirtDefault;
  public static boolean gravelDefault;
  public static boolean coalDefault;
  public static boolean ironDefault;
  public static boolean goldDefault;
  public static boolean redstoneDefault;
  public static boolean diamondDefault;
  public static boolean lapislazuliDefault;
  public static boolean flowerDefault;
  public static boolean roseDefault;
  public static boolean brownMushroomDefault;
  public static boolean redMushroomDefault;
  public static boolean reedDefault;
  public static boolean pumpkinDefault;
  public static boolean cactusDefault;
  public static boolean waterSourceDefault;
  public static boolean lavaSourceDefault;
  public static boolean rainforestDefault;
  public static boolean seasonalforestDefault;
  public static boolean forestDefault;
  public static boolean taigaDefault;
  public static boolean desertDefault;
  public static boolean plainsDefault;
  public static boolean tundraDefault;
  public static boolean lavaSourceHellDefault;
  public static boolean fireHellDefault;
  public static boolean lightstoneHellDefault1;
  public static boolean lightstoneHellDefault2;
  public static boolean brownMushroomHellDefault;
  public static boolean redMushroomHellDefault;
  public static String[] supportedModList = { "BiomeTerrain_" };

  static
  {
    try
    {
      File source = new File(BiomeTerrainBaseMod.class
        .getProtectionDomain().getCodeSource().getLocation()
        .toURI());
      JarInputStream jar = null;

      if ((source.isFile()) && (source.getName().endsWith(".jar"))) {
        jar = new JarInputStream(new FileInputStream(source));
        Object localObject = null;
      }while (true) {
        JarEntry entry = jar.getNextJarEntry();
        if (entry == null)
          break;
        String name = entry.getName();
        boolean supportedMod = false;
        for (int i = 0; (!supportedMod) && 
          (i < supportedModList.length); )
        {
          if (name.startsWith(supportedModList[i]))
            supportedMod = true;
          i++;
        }

        if ((entry.isDirectory()) || (!supportedMod) || 
          (!name.endsWith(".class"))) continue;
        addMod(name.substring(0, name.length() - 6));

        if (source.isDirectory()) {
          File[] files = source.listFiles();
          if (files == null) break;
          for (int i = 0; i < files.length; i++) {
            name = files[i].getName();
            supportedMod = false;
            for (int j = 0; (!supportedMod) && 
              (j < supportedModList.length); )
            {
              if (name.startsWith(supportedModList[j]))
                supportedMod = true;
              j++;
            }

            if ((!files[i].isFile()) || (!supportedMod) || 
              (!name.endsWith(".class"))) continue;
            addMod(name.substring(0, name.length() - 6));
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void initialize(et _world, long randSeed)
  {
    world = _world;
    rand = new Random();
    rand.setSeed(randSeed);
    String worldname = world.s.j();
    minecraftFolder = new File(BiomeTerrain.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    minecraftFolder = minecraftFolder.getParentFile();
    minecraftFolder = new File(minecraftFolder.getParentFile().toString().replace("%20", " "));
    worldSaveFolder = new File(minecraftFolder.toString().replace("%20", " ") + "/saves/" + worldname);

    createNewGlobalSettings = false;

    readSettings();
    fixSettingsValues();
    writeSettings();
    globalSettingsCorrector();
  }

  private static void addMod(String name)
  {
    try
    {
      Class localClass = BiomeTerrainBaseMod.class.getClassLoader()
        .loadClass(name);
      if (localClass.getSuperclass() != BiomeTerrainBaseMod.class)
        return;
      if (mods.add((BiomeTerrainBaseMod)localClass.newInstance()))
        System.out.println("Loaded: " + name);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static boolean isModLoaded(String mod) {
    try {
      for (int i = 0; i < mods.size(); i++)
        if (Class.forName(mod).isInstance(mods.get(i)))
          return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
    return false;
  }

  public static void updateRandom(Random r)
  {
    rand = r;
  }

  public static void updateWorld(et _world) {
    world = _world;
  }

  public static String readSettings(String settingsFile, String settingsName, String defaultValue)
  {
    BufferedReader br = null;
    createWorldSettingsFiles = BiomeTerrain.createGlobalSettingsFiles = true;
    	
    try {
        File f = new File(worldSaveFolder, settingsFile);
        if (!f.exists())
      	  f = new File(minecraftFolder, settingsFile);
        if (!f.exists())
        {
        	f.createNewFile();
        }
      br = new BufferedReader(new FileReader(f));
      String thisLine;
      while ((thisLine = br.readLine()) != null)
      {
        if ((thisLine.toLowerCase().contains(settingsName.toLowerCase())) && (!thisLine.toLowerCase().contains("withlava"))) {
          settings.put(thisLine.split(":")[0].trim(), 
            thisLine.split(":")[1].trim());
          return thisLine.split(":")[1].trim();
        }
      }
    } catch (FileNotFoundException e) {
      createNewGlobalSettings = true;

      if (br != null) {
        try {
          br.close();
        }
        catch (IOException localIOException2)
        {
        }

      }

      if (br != null) {
        try {
          br.close();
        }
        catch (IOException localIOException3)
        {
        }

      }

      if (br != null)
        try {
          br.close();
        }
        catch (IOException localIOException4)
        {
        }
    }
    catch (IOException e)
    {
      e.printStackTrace();

      if (br != null) {
        try {
          br.close();
        }
        catch (IOException localIOException5)
        {
        }

      }

      if (br != null) {
        try {
          br.close();
        }
        catch (IOException localIOException6)
        {
        }

      }

      if (br != null)
        try {
          br.close();
        }
        catch (IOException localIOException7)
        {
        }
    }
    finally
    {
      if (br != null)
        try {
          br.close();
        } catch (IOException localIOException8) {
        }
    }
    return defaultValue;
  }

  public static int readSettings(String settingsFile, String settingsName, int defaultValue)
  {
    return Integer.valueOf(readSettings(settingsFile, settingsName, 
      Integer.toString(defaultValue))).intValue();
  }

  public static double readSettings(String settingsFile, String settingsName, double defaultValue)
  {
    return Double.valueOf(readSettings(settingsFile, settingsName, 
      Double.toString(defaultValue))).doubleValue();
  }

  public static Boolean readSettings(String settingsFile, String settingsName, Boolean defaultValue)
  {
    return Boolean.valueOf(readSettings(settingsFile, settingsName, 
      Boolean.toString(defaultValue.booleanValue())));
  }

  public static void writeSettingsTitle(String settingsFile, String titleName)
  {
    BufferedWriter bw = null;
    File[] f = { new File(minecraftFolder, settingsFile), 
      new File(worldSaveFolder, settingsFile) };
    for (int i = 0; i < f.length; i++) {
      if (((i != 0) || (!createNewGlobalSettings)) && (i != 1)) continue;
      try {
        if ((i == 0) && (createGlobalSettingsFiles)) {
          createGlobalSettingsFiles = false;
          bw = new BufferedWriter(new FileWriter(f[i], false));
        } else if ((i == 1) && (createWorldSettingsFiles)) {
          createWorldSettingsFiles = false;
          bw = new BufferedWriter(new FileWriter(f[i], false));
        } else {
          bw = new BufferedWriter(new FileWriter(f[i], true));
        }if (f[i].length() > 0L)
          bw.newLine();
        bw.write("<" + titleName + ">");
        bw.newLine();
        bw.flush();
      } catch (IOException e) {
        e.printStackTrace();

        if (bw == null)
        {
          if (bw == null) continue;
        }
        else
        {
          try
          {
            bw.close();
          }
          catch (IOException localIOException2) {
          }

          try {
            bw.close();
          }
          catch (IOException localIOException3)
          {
          }

          try {
            bw.close();
          }
          catch (IOException localIOException4)
          {
          }
        }
      }
      finally
      {
        if (bw != null)
          try {
            bw.close();
          }
          catch (IOException localIOException5)
          {
          }
      }
    }
  }

  public static void writeSettings(String settingsFile, String settingsName, String settingsValue) {
    BufferedWriter bw = null;
    File[] f = { new File(minecraftFolder, settingsFile), 
      new File(worldSaveFolder, settingsFile) };
    for (int i = 0; i < f.length; i++) {
      if (((i != 0) || (!createNewGlobalSettings)) && (i != 1)) continue;
      try {
        bw = new BufferedWriter(new FileWriter(f[i], true));
        if (settings.containsKey(settingsName))
          bw.write(settingsName + ":" + 
            (String)settings.get(settingsName));
        else
          bw.write(settingsName + ":" + settingsValue);
        bw.newLine();
        bw.flush();
      } catch (IOException e) {
        e.printStackTrace();

        if (bw == null)
        {
        }
        else
        {
          try
          {
            bw.close();
          }
          catch (IOException localIOException2) {
          }

          try {
            bw.close();
          }
          catch (IOException localIOException3)
          {
          }

          try {
            bw.close();
          }
          catch (IOException localIOException4)
          {
          }
        }
      }
      finally
      {
        if (bw != null)
          try {
            bw.close();
          }
          catch (IOException localIOException5)
          {
          }
      }
    }
  }

  public static void writeSettings(String settingsFile, String settingsName, int settingsValue) {
    writeSettings(settingsFile, settingsName, 
      Integer.toString(settingsValue));
  }

  public static void writeSettings(String settingsFile, String settingsName, double settingsValue)
  {
    writeSettings(settingsFile, settingsName, 
      Double.toString(settingsValue));
  }

  public static void writeSettings(String settingsFile, String settingsName, Boolean settingsValue)
  {
    writeSettings(settingsFile, settingsName, 
      Boolean.toString(settingsValue.booleanValue()));
  }

  private static void globalSettingsCorrector()
  {
    if (getCustomObjects())
    {
      try
      {
        File BOBFolder = new File(minecraftFolder, "BOBPlugins");
        if (!BOBFolder.exists())
        {
          BOBFolder.mkdir();
        }
        String[] BOBFolderArray = BOBFolder.list();
        int i = 0;
        while (i < BOBFolderArray.length)
        {
          File BOBFile = new File(BOBFolder, BOBFolderArray[i]);
          for (int sizer = 0; sizer < mods.size(); sizer++)
            ((BiomeTerrainBaseMod)mods.get(sizer)).RegisterPlugins(BOBFile);
          i++;
        }
      }
      catch (Exception e)
      {
        System.out.println("BOB Plugin system encountered an error, aborting!");
      }
    }
    BufferedReader br = null;
    BufferedWriter bw = null;
    String[] globalMapOrder = new String[1024];
    String[] worldMapOrder = new String[1024];
    HashMap globalSettings = new HashMap();
    try
    {
      try {
        br = new BufferedReader(
          new FileReader(
          new File(worldSaveFolder, 
          BiomeTerrainValues.biomeTerrainSettingsName.stringValue())));
      }
      catch (FileNotFoundException e1)
      {
        try {
        	File f = new File(minecraftFolder, BiomeTerrainValues.biomeTerrainSettingsName.stringValue());
        	if (!f.exists())
        	{
        		f.createNewFile();
        	}
          br = new BufferedReader(
            new FileReader(f));
        }
        catch (FileNotFoundException localFileNotFoundException1)
        {
        }
      }
      int i = 0;
      String thisLine;
      while ((thisLine = br.readLine()) != null)
      {
        if (thisLine.contains(":")) {
          globalMapOrder[(i++)] = thisLine.split(":")[0];
          globalSettings.put(thisLine.split(":")[0], 
            thisLine.split(":")[1]);
        } else {
          globalMapOrder[(i++)] = thisLine;
          globalSettings.put(thisLine, "");
        }
      }
      br.close();
      settings = new HashMap();
      try
      {
        br = new BufferedReader(
          new FileReader(
          new File(worldSaveFolder, 
          BiomeTerrainValues.biomeTerrainSettingsName.stringValue())));
      }
      catch (FileNotFoundException e1)
      {
        try {
          System.out.println("Failed to find a save-specific settings file, using globals!");
          br = new BufferedReader(
            new FileReader(
            new File(minecraftFolder, 
            BiomeTerrainValues.biomeTerrainSettingsName.stringValue())));
        }
        catch (FileNotFoundException e2) {
          System.out.println("Failed to find a global settings file, using defaults!");
        }
      }

      i = 0;
      while ((thisLine = br.readLine()) != null) {
        if (thisLine.contains(":")) {
          worldMapOrder[(i++)] = thisLine.split(":")[0];
          settings.put(thisLine.split(":")[0], thisLine.split(":")[1]);
        } else {
          worldMapOrder[(i++)] = thisLine;
          settings.put(thisLine, "");
        }
      }
      br.close();
      bw = new BufferedWriter(
        new FileWriter(
        new File(worldSaveFolder, 
        BiomeTerrainValues.biomeTerrainSettingsName.stringValue()), 
        false));
      for (i = 0; i < worldMapOrder.length; i++) {
        String key = worldMapOrder[i];
        if (key == null)
          break;
        if (globalSettings.containsKey(key)) {
          if (globalSettings.get(key) != "")
            bw.write(key + ":" + (String)globalSettings.get(key));
          else
            bw.write(key);
        }
        else if (settings.get(key) != "")
          bw.write(key + ":" + (String)settings.get(key));
        else {
          bw.write(key);
        }
        bw.newLine();
        bw.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();

      if (br != null)
        try {
          br.close();
        } catch (IOException localIOException1) {
        }
      if (bw != null) {
        try {
          bw.close();
        }
        catch (IOException localIOException2)
        {
        }

      }

      if (br != null)
        try {
          br.close();
        } catch (IOException localIOException3) {
        }
      if (bw != null) {
        try {
          bw.close();
        }
        catch (IOException localIOException4)
        {
        }

      }

      if (br != null)
        try {
          br.close();
        } catch (IOException localIOException5) {
        }
      if (bw != null)
        try {
          bw.close();
        }
        catch (IOException localIOException6)
        {
        }
    }
    finally
    {
      if (br != null)
        try {
          br.close();
        } catch (IOException localIOException7) {
        }
      if (bw != null)
        try {
          bw.close();
        }
        catch (IOException localIOException8)
        {
        }
    }
  }

  public static void processDepositMaterial(int _x, int _z, int rarity, int frequency, int minAltitude, int maxAltitude, int size, int type, boolean evenDistribution)
  {
    int xyPosMod = (type == BiomeTerrainValues.flower.byteValue().byteValue()) || 
      (type == BiomeTerrainValues.rose.byteValue().byteValue()) || 
      (type == BiomeTerrainValues.brownmushroom.byteValue().byteValue()) || 
      (type == BiomeTerrainValues.redmushroom.byteValue().byteValue()) || 
      (type == BiomeTerrainValues.water.byteValue().byteValue()) || 
      (type == BiomeTerrainValues.lava.byteValue().byteValue()) || 
      (type == BiomeTerrainValues.fire.byteValue().byteValue()) || 
      (type == BiomeTerrainValues.cactus.byteValue().byteValue()) || 
      (type == BiomeTerrainValues.mobspawner.byteValue().byteValue()) || 
      (type == BiomeTerrainValues.reeds.byteValue().byteValue()) || 
      (type == BiomeTerrainValues.pumpkin.byteValue().byteValue()) || 
      (type == BiomeTerrainValues.lightstone
      .byteValue().byteValue()) ? 
      8 : 0;

    if ((type == BiomeTerrainValues.fire.byteValue().byteValue()) && (!evenDistribution))
      frequency = rand.nextInt(rand.nextInt(frequency) + 1) + 1;
    else if ((type == BiomeTerrainValues.lightstone.byteValue().byteValue()) && (size == -1) && 
      (!evenDistribution)) {
      frequency = rand.nextInt(rand.nextInt(frequency) + 1);
    }
    for (int i = 0; i < frequency; i++)
      if (rand.nextInt(100) < rarity) {
        int x = _x + rand.nextInt(16) + xyPosMod;
        int z = _z + rand.nextInt(16) + xyPosMod;
        int y = rand.nextInt(maxAltitude - minAltitude) + minAltitude;
        if (currentBiome == BiomeTerrainValues.Hell.biomeValue()) {
          if (type == BiomeTerrainValues.lava.byteValue().byteValue()) {
            new pd(type).a(world, rand, x, y, z);
          } else if (type == BiomeTerrainValues.fire.byteValue().byteValue()) {
            new wi().a(world, rand, x, y, z);
          } else if ((type == BiomeTerrainValues.lightstone.byteValue().byteValue()) && 
            (size == -1)) {
            new rc().a(world, rand, x, y, z);
          } else if ((type == BiomeTerrainValues.lightstone.byteValue().byteValue()) && 
            (size == -2)) {
            new fs().a(world, rand, x, y, z); } else {
            if ((type != BiomeTerrainValues.brownmushroom
              .byteValue().byteValue()) && 
              (type != BiomeTerrainValues.redmushroom
              .byteValue().byteValue())) continue;
            new ay(type).a(world, rand, x, y, z);
          }
        } else {
          if ((type == BiomeTerrainValues.water.byteValue().byteValue()) && 
            (!evenDistribution))
            y = rand.nextInt(rand
              .nextInt(maxAltitude - minAltitude) + 
              minAltitude);
          else if ((type == BiomeTerrainValues.lava.byteValue().byteValue()) && 
            (!evenDistribution))
            y = rand.nextInt(rand.nextInt(rand.nextInt(maxAltitude - 
              minAltitude * 2) + 
              minAltitude) + 
              minAltitude);
          if ((type == BiomeTerrainValues.flower.byteValue().byteValue()) || 
            (type == BiomeTerrainValues.rose.byteValue().byteValue()) || 
            (type == BiomeTerrainValues.brownmushroom
            .byteValue().byteValue()) || 
            (type == BiomeTerrainValues.redmushroom
            .byteValue().byteValue()))
            new ay(type).a(world, rand, x, y, z);
          else if (type == BiomeTerrainValues.cactus.byteValue().byteValue())
            new fm().a(world, rand, x, y, z);
          else if (type == BiomeTerrainValues.reeds.byteValue().byteValue())
            new ic().a(world, rand, x, y, z);
          else if (type == BiomeTerrainValues.pumpkin.byteValue().byteValue())
            new vo().a(world, rand, x, y, z);
          else if (type == BiomeTerrainValues.clay.byteValue().byteValue())
            new lv(size).a(world, rand, x, y, z);
          else if ((type == BiomeTerrainValues.water.byteValue().byteValue()) || 
            (type == BiomeTerrainValues.lava.byteValue().byteValue()))
            new we(type).a(world, rand, x, y, z);
          else if (type == BiomeTerrainValues.mobspawner.byteValue().byteValue())
            new eh().a(world, rand, x, y, z);
          else
            new fb(type, size).a(world, rand, x, y, z);
        }
      }
  }

  public static void setBlock(int x, int y, int z, int block)
  {
    world.a(x, y, z, block);
  }

  public static int getBlock(int x, int y, int z) {
    return world.a(x, y, z);
  }

  public static int getBlockElevation(int x, int z) {
    return world.d(x, z);
  }

  public static jj getBiomeType(int x, int z) {
    return world.a().a(x + 16, z + 16);
  }

  public static float sin(float n) {
    return hy.a(n);
  }

  public static float cos(float n) {
    return hy.b(n);
  }

  private static void processOriginalUndergroundDeposits(int x, int z)
  {
    if (dungeonDefault)
      for (int i = 0; i < 8; i++) {
        int xD = x + rand.nextInt(16) + 8;
        int yD = rand.nextInt(128);
        int zD = z + rand.nextInt(16) + 8;
        new eh().a(world, rand, xD, yD, zD);
      }
    if (clayDefault)
      for (int i = 0; i < 10; i++) {
        int xD = x + rand.nextInt(16);
        int yD = rand.nextInt(128);
        int zD = z + rand.nextInt(16);
        new lv(32).a(world, rand, xD, yD, zD);
      }
    if (dirtDefault)
      for (int i = 0; i < 20; i++) {
        int xD = x + rand.nextInt(16);
        int yD = rand.nextInt(128);
        int zD = z + rand.nextInt(16);
        new fb(BiomeTerrainValues.dirt.byteValue().byteValue(), 32).a(world, rand, xD, yD, zD);
      }
    if (gravelDefault)
      for (int i = 0; i < 10; i++) {
        int xD = x + rand.nextInt(16);
        int yD = rand.nextInt(128);
        int zD = z + rand.nextInt(16);
        new fb(BiomeTerrainValues.gravel.byteValue().byteValue(), 32).a(
          world, rand, xD, yD, zD);
      }
    if (coalDefault)
      for (int i = 0; i < 20; i++) {
        int xD = x + rand.nextInt(16);
        int yD = rand.nextInt(128);
        int zD = z + rand.nextInt(16);
        new fb(BiomeTerrainValues.coalore.byteValue().byteValue(), 16).a(
          world, rand, xD, yD, zD);
      }
    if (ironDefault)
      for (int i = 0; i < 20; i++) {
        int xD = x + rand.nextInt(16);
        int yD = rand.nextInt(64);
        int zD = z + rand.nextInt(16);
        new fb(BiomeTerrainValues.ironore.byteValue().byteValue(), 8).a(
          world, rand, xD, yD, zD);
      }
    if (goldDefault)
      for (int i = 0; i < 2; i++) {
        int xD = x + rand.nextInt(16);
        int yD = rand.nextInt(32);
        int zD = z + rand.nextInt(16);
        new fb(BiomeTerrainValues.goldore.byteValue().byteValue(), 8).a(
          world, rand, xD, yD, zD);
      }
    if (redstoneDefault)
      for (int i = 0; i < 8; i++) {
        int xD = x + rand.nextInt(16);
        int yD = rand.nextInt(16);
        int zD = z + rand.nextInt(16);
        new fb(BiomeTerrainValues.redstoneore.byteValue().byteValue(), 7).a(
          world, rand, xD, yD, zD);
      }
    if (diamondDefault)
      for (int i = 0; i < 1; i++) {
        int xD = x + rand.nextInt(16);
        int yD = rand.nextInt(16);
        int zD = z + rand.nextInt(16);
        new fb(BiomeTerrainValues.diamondore.byteValue().byteValue(), 7).a(
          world, rand, xD, yD, zD);
      }
    if (lapislazuliDefault)
      for (int i = 0; i < 1; i++) {
        int xD = x + rand.nextInt(16);
        int yD = rand.nextInt(16);
        int zD = z + rand.nextInt(16);
        new fb(BiomeTerrainValues.lapislazuliore.byteValue().byteValue(), 7).a(
          world, rand, xD, yD, zD);
      }
  }

  private static void processOriginalAboveGroundMaterials(int x, int z) {
    if (flowerDefault)
      for (int i = 0; i < 2; i++) {
        int xD = x + rand.nextInt(16) + 8;
        int yD = rand.nextInt(128);
        int zD = z + rand.nextInt(16) + 8;
        new ay(BiomeTerrainValues.flower.byteValue().byteValue()).a(
          world, rand, xD, yD, zD);
      }
    if ((roseDefault) && (rand.nextInt(2) == 0)) {
      int xD = x + rand.nextInt(16) + 8;
      int yD = rand.nextInt(128);
      int zD = z + rand.nextInt(16) + 8;
      new ay(BiomeTerrainValues.rose.byteValue().byteValue()).a(world, 
        rand, xD, yD, zD);
    }
    if ((brownMushroomDefault) && 
      (rand.nextInt(4) == 0)) {
      int xD = x + rand.nextInt(16) + 8;
      int yD = rand.nextInt(128);
      int zD = z + rand.nextInt(16) + 8;
      new ay(BiomeTerrainValues.brownmushroom.byteValue().byteValue()).a(
        world, rand, xD, yD, zD);
    }
    if ((redMushroomDefault) && 
      (rand.nextInt(8) == 0)) {
      int xD = x + rand.nextInt(16) + 8;
      int yD = rand.nextInt(128);
      int zD = z + rand.nextInt(16) + 8;
      new ay(BiomeTerrainValues.redmushroom.byteValue().byteValue()).a(
        world, rand, xD, yD, zD);
    }
    if (reedDefault)
      for (int i = 0; i < 10; i++) {
        int xD = x + rand.nextInt(16) + 8;
        int yD = rand.nextInt(128);
        int zD = z + rand.nextInt(16) + 8;
        new ic().a(world, rand, xD, yD, zD);
      }
    if ((pumpkinDefault) && (rand.nextInt(32) == 0)) {
      int xD = x + rand.nextInt(16) + 8;
      int yD = rand.nextInt(128);
      int zD = z + rand.nextInt(16) + 8;
      new vo().a(world, rand, xD, yD, zD);
    }
    int d = 0;
    if ((cactusDefault) && 
      (currentBiome == BiomeTerrainValues.Desert.biomeValue()))
      d += 10;
    for (int i = 0; i < d; i++) {
      int xD = x + rand.nextInt(16) + 8;
      int yD = rand.nextInt(128);
      int zD = z + rand.nextInt(16) + 8;
      new fm().a(world, rand, xD, yD, zD);
    }
    if (waterSourceDefault)
      for (int i = 0; i < 50; i++) {
        int xD = x + rand.nextInt(16) + 8;
        int yD = rand.nextInt(rand
          .nextInt(120) + 8);
        int zD = z + rand.nextInt(16) + 8;
        new we(BiomeTerrainValues.water.byteValue().byteValue()).a(
          world, rand, xD, yD, zD);
      }
    if (lavaSourceDefault)
      for (int i = 0; i < 20; i++) {
        int xD = x + rand.nextInt(16) + 8;
        int yD = rand.nextInt(rand
          .nextInt(rand.nextInt(112) + 8) + 8);
        int zD = z + rand.nextInt(16) + 8;
        new we(BiomeTerrainValues.lava.byteValue().byteValue()).a(
          world, rand, xD, yD, zD);
      }
  }

  private static void processOriginalHellMaterials(int x, int z) {
    if (lavaSourceHellDefault) {
      for (int i = 0; i < 8; i++) {
        int xD = x + rand.nextInt(16) + 8;
        int yD = rand.nextInt(120) + 4;
        int zD = z + rand.nextInt(16) + 8;
        new pd(BiomeTerrainValues.stationarylava.byteValue().byteValue()).a(
          world, rand, xD, yD, zD);
      }
    }
    int d = rand.nextInt(rand.nextInt(10) + 1) + 1;
    if (fireHellDefault) {
      for (int i = 0; i < d; i++) {
        int xD = x + rand.nextInt(16) + 8;
        int yD = rand.nextInt(120) + 4;
        int zD = z + rand.nextInt(16) + 8;
        new wi().a(world, rand, xD, yD, zD);
      }
    }
    d = rand.nextInt(rand.nextInt(10) + 1);
    if (lightstoneHellDefault1)
      for (int i = 0; i < d; i++) {
        int xD = x + rand.nextInt(16) + 8;
        int yD = rand.nextInt(120) + 4;
        int zD = z + rand.nextInt(16) + 8;
        new rc().a(world, rand, xD, yD, zD);
      }
    if (lightstoneHellDefault2) {
      for (int i = 0; i < 10; i++) {
        int xD = x + rand.nextInt(16) + 8;
        int yD = rand.nextInt(128);
        int zD = z + rand.nextInt(16) + 8;
        new fs().a(world, rand, xD, yD, zD);
      }
    }
    if ((brownMushroomHellDefault) && (rand.nextInt(1) == 0)) {
      int xD = x + rand.nextInt(16) + 8;
      int yD = rand.nextInt(128);
      int zD = z + rand.nextInt(16) + 8;
      new ay(BiomeTerrainValues.brownmushroom.byteValue().byteValue()).a(
        world, rand, xD, yD, zD);
    }

    if ((redMushroomHellDefault) && (rand.nextInt(1) == 0)) {
      int xD = x + rand.nextInt(16) + 8;
      int yD = rand.nextInt(128);
      int zD = z + rand.nextInt(16) + 8;
      new ay(BiomeTerrainValues.redmushroom.byteValue().byteValue()).a(
        world, rand, xD, yD, zD);
    }
  }

  private static int processOriginalTrees(int treeDensity, int treeDensityVariation)
  {
    if ((rainforestDefault) && 
      (currentBiome == BiomeTerrainValues.Rainforest
      .biomeValue()))
      treeDensity += treeDensityVariation + 5;
    if ((seasonalforestDefault) && 
      (currentBiome == BiomeTerrainValues.SeasonalForest
      .biomeValue()))
      treeDensity += treeDensityVariation + 2;
    if ((forestDefault) && 
      (currentBiome == BiomeTerrainValues.Forest
      .biomeValue()))
      treeDensity += treeDensityVariation + 5;
    if ((taigaDefault) && 
      (currentBiome == BiomeTerrainValues.Taiga
      .biomeValue()))
      treeDensity += treeDensityVariation + 5;
    if ((desertDefault) && 
      (currentBiome == BiomeTerrainValues.Desert
      .biomeValue()))
      treeDensity -= 20;
    if ((plainsDefault) && 
      (currentBiome == BiomeTerrainValues.Plains
      .biomeValue()))
      treeDensity -= 20;
    if ((tundraDefault) && 
      (currentBiome == BiomeTerrainValues.Tundra
      .biomeValue()))
      treeDensity -= 20;
    return treeDensity;
  }

  private static void readSettings()
  {
    for (int i = 0; i < mods.size(); i++)
      ((BiomeTerrainBaseMod)mods.get(i)).readSettings();
  }

  private static void fixSettingsValues() {
    for (int i = 0; i < mods.size(); i++)
      ((BiomeTerrainBaseMod)mods.get(i)).fixSettingsValues();
  }

  private static void writeSettings() {
    for (int i = 0; i < mods.size(); i++)
      ((BiomeTerrainBaseMod)mods.get(i)).writeSettings();
  }

  public static void processChunkBlocks(byte[] blocks, jj[] biomes)
  {
    biomeList = biomes;
    for (int i = 0; i < mods.size(); i++)
      ((BiomeTerrainBaseMod)mods.get(i)).processChunkBlocks(blocks);
  }

  public static void processUndergroundDeposits(int x, int z)
  {
    dungeonDefault = BiomeTerrain.clayDefault = BiomeTerrain.dirtDefault = BiomeTerrain.gravelDefault = BiomeTerrain.coalDefault = BiomeTerrain.ironDefault = BiomeTerrain.goldDefault = BiomeTerrain.redstoneDefault = BiomeTerrain.diamondDefault = true;
    for (int i = 0; i < mods.size(); i++)
      ((BiomeTerrainBaseMod)mods.get(i))
        .processUndergroundDeposits(x, z);
    processOriginalUndergroundDeposits(x, z);
  }

  public static void processAboveGroundMaterials(int x, int z) {
    flowerDefault = BiomeTerrain.roseDefault = BiomeTerrain.brownMushroomDefault = BiomeTerrain.redMushroomDefault = BiomeTerrain.reedDefault = BiomeTerrain.pumpkinDefault = BiomeTerrain.cactusDefault = BiomeTerrain.waterSourceDefault = BiomeTerrain.lavaSourceDefault = true;
    currentBiome = world.a().a(x + 16, z + 16);
    for (int i = 0; i < mods.size(); i++)
      ((BiomeTerrainBaseMod)mods.get(i)).processAboveGroundMaterials(x, 
        z);
    processOriginalAboveGroundMaterials(x, z);
  }

  public static void processHellDeposits(int x, int z) {
    lavaSourceHellDefault = BiomeTerrain.fireHellDefault = BiomeTerrain.lightstoneHellDefault1 = BiomeTerrain.lightstoneHellDefault2 = BiomeTerrain.brownMushroomHellDefault = BiomeTerrain.redMushroomHellDefault = true;
    currentBiome = BiomeTerrainValues.Hell.biomeValue();
    for (int i = 0; i < mods.size(); i++)
      ((BiomeTerrainBaseMod)mods.get(i)).processHellMaterials(x, z);
    processOriginalHellMaterials(x, z);
  }
  public static boolean getOldGen()
	  {
		    boolean result = BiomeTerrainValues.oldGen.booleanValue();
		    for (int i = 0; (i < mods.size()) && 
		      (result == BiomeTerrainValues.oldGen.booleanValue()); )
		    {
		      result = ((BiomeTerrainBaseMod)mods.get(i)).getOldGen();

		      i++;
		    }

		    return result;
		  }
  public static double getBiomeSize()
  {
    double result = BiomeTerrainValues.biomeSize.doubleValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.biomeSize.doubleValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getBiomeSize();

      i++;
    }

    return result;
  }

  public static double getMinimumTemperature() {
    double result = BiomeTerrainValues.minTemperature.doubleValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.minTemperature.doubleValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i))
        .getMinimumTemperature();

      i++;
    }

    return result;
  }

  public static double getMaximumTemperature() {
    double result = BiomeTerrainValues.maxTemperature.doubleValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.maxTemperature.doubleValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i))
        .getMaximumTemperature();

      i++;
    }

    return result;
  }

  public static double getMinimumMoisture() {
    double result = BiomeTerrainValues.minMoisture.doubleValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.minMoisture.doubleValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getMinimumMoisture();

      i++;
    }

    return result;
  }

  public static double getMaximumMoisture() {
    double result = BiomeTerrainValues.maxMoisture.doubleValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.maxMoisture.doubleValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getMaximumMoisture();

      i++;
    }

    return result;
  }

  public static double getSnowThreshold() {
    double result = BiomeTerrainValues.snowThreshold.doubleValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.snowThreshold.doubleValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getSnowThreshold();

      i++;
    }

    return result;
  }

  public static double getIceThreshold() {
    double result = BiomeTerrainValues.iceThreshold.doubleValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.iceThreshold.doubleValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getIceThreshold();

      i++;
    }

    return result;
  }

  public static int getWaterLevel()
  {
    int result = BiomeTerrainValues.waterLevel.intValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.waterLevel.intValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getWaterLevel();

      i++;
    }

    return result;
  }

  public static double getMaxAverageHeight() {
    double result = BiomeTerrainValues.maxAverageHeight.doubleValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.maxAverageHeight.doubleValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getMaxAverageHeight();

      i++;
    }

    return result;
  }

  public static double getMaxAverageDepth() {
    double result = BiomeTerrainValues.maxAverageDepth.doubleValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.maxAverageDepth.doubleValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getMaxAverageDepth();

      i++;
    }

    return result;
  }

  public static double getFractureHorizontal() {
    double result = BiomeTerrainValues.fractureHorizontal.doubleValue() + 1.0D;
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.fractureHorizontal
      .doubleValue() + 1.0D); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i))
        .getFractureHorizontal();

      i++;
    }

    return result;
  }

  public static double getFractureVertical() {
    double result = BiomeTerrainValues.fractureVertical.doubleValue() + 1.0D;
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.fractureVertical.doubleValue() + 1.0D); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getFractureVertical();

      i++;
    }

    return result;
  }

  public static double getVolatility1() {
    double result = BiomeTerrainValues.volatility1.doubleValue() + 1.0D;
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.volatility1.doubleValue() + 1.0D); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getVolatility1();

      i++;
    }

    return result;
  }

  public static double getVolatility2() {
    double result = BiomeTerrainValues.volatility2.doubleValue() + 1.0D;
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.volatility2.doubleValue() + 1.0D); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getVolatility2();

      i++;
    }

    return result;
  }

  public static double getVolatilityWeight1() {
    double result = (BiomeTerrainValues.volatilityWeight1.doubleValue() - 0.5D) * 24.0D;
    for (int i = 0; (i < mods.size()) && 
      (result == 
      (BiomeTerrainValues.volatilityWeight1
      .doubleValue() - 0.5D) * 24.0D); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getVolatilityWeight1();

      i++;
    }

    return result;
  }

  public static double getVolatilityWeight2() {
    double result = (0.5D - BiomeTerrainValues.volatilityWeight2
      .doubleValue()) * 24.0D;
    for (int i = 0; (i < mods.size()) && 
      (result == 
      (0.5D - BiomeTerrainValues.volatilityWeight2
      .doubleValue()) * 24.0D); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getVolatilityWeight2();

      i++;
    }

    return result;
  }

  public static boolean createadminium(int y) {
    for (int i = 0; i < mods.size(); i++) {
      boolean d = ((BiomeTerrainBaseMod)mods.get(i)).createadminium(y);
      if (!d)
        return d;
    }
    return true;
  }

  public static byte getadminium() {
    byte result = BiomeTerrainValues.adminium.byteValue().byteValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.adminium.byteValue().byteValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getadminium();

      i++;
    }

    return result;
  }

  public static boolean getCustomObjects() {
    for (int i = 0; i < mods.size(); i++) {
      boolean d = ((BiomeTerrainBaseMod)mods.get(i)).getCustomObjects();
      if (d)
        return d;
    }
    return false;
  }

  public static boolean getNotchBiomeTrees() {
    for (int i = 0; i < mods.size(); i++) {
      boolean d = ((BiomeTerrainBaseMod)mods.get(i)).getNotchBiomeTrees();
      if (d)
        return d;
    }
    return false;
  }

  public static int getObjectSpawnRatio() {
    for (int i = 0; i < mods.size(); i++) {
      int d = ((BiomeTerrainBaseMod)mods.get(i)).getObjectSpawnRatio();
      if (d != 1)
        return d;
    }
    return 1;
  }
  public static boolean getDisableNotchPonds() {
    for (int i = 0; i < mods.size(); i++) {
      boolean d = ((BiomeTerrainBaseMod)mods.get(i)).getDisableNotchPonds();
      if (d)
        return d;
    }
    return false;
  }

  public static int processTrees(jj biome, int treeDensity, int treeDensityVariation)
  {
    rainforestDefault = BiomeTerrain.seasonalforestDefault = BiomeTerrain.forestDefault = BiomeTerrain.taigaDefault = BiomeTerrain.desertDefault = BiomeTerrain.plainsDefault = BiomeTerrain.tundraDefault = true;
    int result = treeDensity;
    currentBiome = biome;
    for (int i = 0; (i < mods.size()) && 
      (result == treeDensity); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).processTrees(
        treeDensity, treeDensityVariation);

      i++;
    }

    processOriginalTrees(treeDensity, treeDensityVariation);
    return result;
  }

  public static int getCaveRarity()
  {
    int result = BiomeTerrainValues.caveRarity.intValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.caveRarity.intValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getCaveRarity();

      i++;
    }

    return result;
  }

  public static int getCaveFrequency() {
    int result = BiomeTerrainValues.caveFrequency.intValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.caveFrequency.intValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getCaveFrequency();

      i++;
    }

    return result;
  }

  public static int getCaveMinAltitude() {
    int result = BiomeTerrainValues.caveMinAltitude.intValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.caveMinAltitude.intValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getCaveMinAltitude();

      i++;
    }

    return result;
  }

  public static int getCaveMaxAltitude() {
    int result = BiomeTerrainValues.caveMaxAltitude.intValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.caveMaxAltitude.intValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getCaveMaxAltitude();

      i++;
    }

    return result;
  }

  public static int getIndividualCaveRarity() {
    int result = BiomeTerrainValues.individualCaveRarity.intValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.individualCaveRarity.intValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getIndividualCaveRarity();

      i++;
    }

    return result;
  }

  public static int getCaveSystemFrequency() {
    int result = BiomeTerrainValues.caveSystemFrequency.intValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.caveSystemFrequency.intValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getCaveSystemFrequency();

      i++;
    }

    return result;
  }

  public static int getCaveSystemPocketChance() {
    int result = BiomeTerrainValues.caveSystemPocketChance.intValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.caveSystemPocketChance.intValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getCaveSystemPocketChance();

      i++;
    }

    return result;
  }

  public static int getCaveSystemPocketMinSize() {
    int result = BiomeTerrainValues.caveSystemPocketMinSize.intValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.caveSystemPocketMinSize.intValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getCaveSystemPocketMinSize();

      i++;
    }

    return result;
  }

  public static int getCaveSystemPocketMaxSize() {
    int result = BiomeTerrainValues.caveSystemPocketMaxSize.intValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.caveSystemPocketMaxSize.intValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getCaveSystemPocketMaxSize();

      i++;
    }

    return result;
  }

  public static boolean getEvenCaveDistribution() {
    boolean result = BiomeTerrainValues.evenCaveDistribution.booleanValue().booleanValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.evenCaveDistribution.booleanValue().booleanValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getEvenCaveDistribution();

      i++;
    }

    return result;
  }
  public static int getLavaLevelMin() {
    int result = BiomeTerrainValues.lavaLevelMin.intValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.lavaLevelMin.intValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getLavaLevelMin();

      i++;
    }

    return result;
  }

  public static int getLavaLevelMax() {
    int result = BiomeTerrainValues.lavaLevelMax.intValue();
    for (int i = 0; (i < mods.size()) && 
      (result == BiomeTerrainValues.lavaLevelMax.intValue()); )
    {
      result = ((BiomeTerrainBaseMod)mods.get(i)).getLavaLevelMax();

      i++;
    }

    return result;
  }

  public static void processUndergroundLakes(int x, int z)
  {
    for (int i = 0; i < mods.size(); i++)
      ((BiomeTerrainBaseMod)mods.get(i)).processUndergroundLakes(x, z);
  }
}
