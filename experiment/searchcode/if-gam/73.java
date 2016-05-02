class U5Data {

  // Like in U4Data, following codes are <y,x> tile-coords

  public static final short TILE_ARROW_SLIT = 0x4A;
  public static final short TILE_BLACK = 0xFF;
  public static final short TILE_BRICK = 0x44;
  public static final short TILE_BRIDGE_NS = 0x6B;
  public static final short TILE_CIRCLE = 0x100; // circle of dots
  public static final short TILE_CHEST = 0x101;
  public static final short TILE_DESERT = 0x07;
  public static final short TILE_DOOR = 0xB8;
  public static final short TILE_DOT = 0x2A; // (lighthouse)
  public static final short TILE_FIELD_ENERGY = 0x1EB;
  public static final short TILE_FIELD_FIRE = 0x1EA;
  public static final short TILE_FIELD_POISON = 0x1E8;
  public static final short TILE_FIELD_SLEEP = 0x1E9;
  public static final short TILE_FOUNTAIN1 = 0xD8;
  public static final short TILE_FOUNTAIN2 = 0xD9;
  public static final short TILE_FOUNTAIN3 = 0xDA;
  public static final short TILE_HEX = 0x45;
  public static final short TILE_HIT = 0x00;
  public static final short TILE_INVISIBLE = 0x11D;
  public static final short TILE_LADDER_DOWN = 0xC9;
  public static final short TILE_LADDER_UP = 0xC8;
  public static final short TILE_LOOSE_BRICK = 0x8C;
  public static final short TILE_MAGIC_LOCKED_DOOR = 0x97;
  //public static final short TILE_ORB = 0x29;
  public static final short TILE_PLANKS_HORIZ = 0x40;
  public static final short TILE_PLANKS_VERT = 0x48;
  public static final short TILE_RING = 0x10A;
  public static final short TILE_SECRET_DOOR = 0x4E;
  public static final short TILE_SHADOW_GAP_HORIZ = 0x7A;
  public static final short TILE_SHADOW_GAP_VERT = 0x7B;
  public static final short TILE_SHOALS = 0x03;
  public static final short TILE_WALL = 0x4F;
  public static final short TILE_WALL_SIGN = 0xF8;
  //public static final short TILE_WALL_STONE = 0x4D;
  public static final short TILE_WHITE = 0xFE;

    
  public static final short FONT_0 = 0x30;
  public static final short FONT_1 = 0x31;
  public static final short FONT_2 = 0x32;
  public static final short FONT_3 = 0x33;
  //public static final short FONT_A = 0x41;
  public static final short FONT_B = 0x42;
  public static final short FONT_C = 0x43;
  //public static final short FONT_H = 0x48;
  //public static final short FONT_P = 0x50;
  public static final short FONT_S = 0x53;
  public static final short FONT_ARROW_BOTH = 0x12;
  public static final short FONT_ARROW_DOWN = 0x19;
  public static final short FONT_ARROW_UP = 0x18;
  public static final short FONT_TRIANGLE_DOWN = 0x1F;


  public static final String[] combatFieldNames = {
    "Camp",
    "Swamp",
    "Grass",
    "Scrub",
    "Desert",
    "Forest",
    "Hill",
    "Bridge",
    "Brick",
    "Hall",
    "Shadow",
    "Ship",
    "ShoreShip",
    "ShipShore",
    "ShipShip",
    "Coast"
  };

  // Used in SIGNS.DAT and SAVED.GAM, at least
  public final static String[] locationNames = {
    // Britannia/Underworld
    "The World",
    // Towns
    "Moonglow",
    "Britain",
    "Jhelom",
    "Yew",
    "Minoc",
    "Trinsic",
    "Skara Brae",
    "New Magincia",
    // Lighthouses
    "Fogsbane",
    "Stormcrow",
    "Greyhaven",
    "Waveguide",
    // Huts
    "Iolo's Hut",
    "Sutek's Hut",
    "Sin'Vraal's Hut",
    "Grendel's Hut",
    // Castles
    "Castle British",
    "Castle Blackthorn",
    // Villages
    "West Britanny",
    "North Britanny",
    "East Britanny",
    "Paws",
    "Cove",
    "Buccaneer's Den", // "'s" or "s'"?
    // Keeps
    "Ararat",
    "Bordermarch",
    "Farthing",
    "Windemere", // +e or -e?
    "Stonegate",
    // Castles of the Principles
    "The Lycaeum",
    "Empath Abbey",
    "Serpent's Hold" };

  // ** use some sort of indices into the above to return values for below?

  //  Thanks to www.gamefaqs.com for pngs which helped identify following names
  public static final String[] keepNames = {
    "Ararat1",       "Ararat2",       // Drydocked
    "Bordermarch1",  "Bordermarch2",  // Has a moat
    "Farthing",                       // Has a well
    "Windmere",                       // Has an atrium
    "Stonegate",                      // Has a pit
    "Lycaeum1",      "Lycaeum2",      "Lycaeum3",        // True, true
    "EmpathAbbey1",  "EmpathAbbey2",  "EmpathAbbey3",    // I love being a turtle
    "SerpentsHold0", "SerpentsHold1", "SerpentsHold2" }; // I dare not joke

  public static final String[] towneNames = {
    "Moonglow1",   "Moonglow2",
    "Britain1",    "Britain2",
    "Jhelom1",     "Jhelom2",
    "Yew0",        "Yew1",
    "Minoc1",      "Minoc2",
    "Trinsic1",    "Trinsic2",
    "SkaraBrae1",  "SkaraBrae2",
    "NewMagincia1","NewMagincia2" };

  public static final String[] castleNames = {
    "LBCastle0", "LBCastle1", "LBCastle2", "LBCastle3", "LBCastle4",
    "BTCastle0", "BTCastle1", "BTCastle2", "BTCastle3", "BTCastle4",
    "WestBrittany", "NorthBrittany", "EastBrittany",
    "Paws",
    "Cove",
    "BuccaneersDen" }; // Buccaneers' Den

  public static final String[] dwellingNames = {
    "Fogsbane1",   "Fogsbane2",   "Fogsbane3",
    "Stormcrow1",  "Stormcrow2",  "Stormcrow3",  // lothspell I name you
    "Greyhaven1",  "Greyhaven2",  "Greyhaven3",  // paging cirdan
    "Waveguide1",  "Waveguide2",  "Waveguide3",
    "IolosHut",    // Iolo's Hut
    "Suteks",      // Sutek's
    "SinVraal",    // Sin'Vraal
    "Grendels" };  // Grendel's

  public static final String[] dungeonNames = {
    "Deceit",    // [GRR]
    "Despise",   // [Cave]
    "Destard",   // [Cave]
    "Wrong",     // [GRR]
    "Covetous",  // [GRR]
    "Shame",     // [Mine]
    "Hythloth",  // [Mine]
    "Doom" };    // [Cave]

  public static String getDungeonName(final int dungeonIndex) {
    return dungeonNames[dungeonIndex];
  }

  public static String getTowneName(final String dataName, final int mapIndex) {
    if (dataName.equals("DWELLING"))
      return dwellingNames[mapIndex];
    else if (dataName.equals("CASTLE"))
      return castleNames[mapIndex];
    else if (dataName.equals("KEEP"))
      return keepNames[mapIndex];
    else if (dataName.equals("TOWNE"))
      return towneNames[mapIndex];
    //** complain
    return dataName;
  }


  // of questionable artistic merit--
  // (might look better with different walls too...)
  public static short getDungeonFloorTile(final int dungeonIndex) {
    // 3 different styles of dungeon, so try drawing different floors
    if (dungeonIndex == 1 || dungeonIndex == 2 || dungeonIndex == 7)
      return TILE_DESERT; // Cavern style
    else if (dungeonIndex == 5 || dungeonIndex == 6)
      return TILE_PLANKS_HORIZ; // Mine style
    else
      return TILE_HEX;    // "Proper" dungeon style
  }

  public static short getDungeonTile(final int dungeonData) {
    // There are special codes in dungeon maps that go back to map tiles
    // Thanks to www.ultima-universe and www.gamefaqs.com for their excellent maps
    if (dungeonData == 0x00)      // floor (default style)
      return TILE_HEX;
    else if (dungeonData == 0x08) // ceiling hole (explicit)
      return TILE_CIRCLE;
    else if (dungeonData == 0x10)
      return TILE_LADDER_UP;
    else if (dungeonData == 0x20)
      return TILE_LADDER_DOWN;
    else if (dungeonData == 0x30) // ladder/both ways
      return TILE_BRIDGE_NS;
    else if (dungeonData == 0x41)
      return TILE_CHEST;
    else if (dungeonData == 0x51) // the healing kind? (appears on both spoiler maps)
      return TILE_FOUNTAIN1;
    else if (dungeonData == 0x52) // looks blank on some maps -- bad/useless fountain?
      return TILE_FOUNTAIN2;
    else if (dungeonData == 0x53) // looks blank on some maps -- bad/useless fountain?
      return TILE_FOUNTAIN3;
    else if (dungeonData == 0x60) // floor hole
      return TILE_CIRCLE;
    else if (dungeonData == 0x61) // pit trap
      return TILE_LOOSE_BRICK;
    else if (dungeonData == 0x62) // bomb trap
      return TILE_HIT;
    else if (dungeonData == 0x68) // up/down pit
      return TILE_CIRCLE;
    else if (dungeonData == 0x69) // pit trap + ceiling hole (???)
      return TILE_LOOSE_BRICK;
    else if (dungeonData == 0x80)
      return TILE_FIELD_POISON;
    else if (dungeonData == 0x81)
      return TILE_FIELD_SLEEP;
    else if (dungeonData == 0x82)
      return TILE_FIELD_ENERGY;
    else if (dungeonData == 0x83)
      return TILE_FIELD_FIRE;
    else if (dungeonData == 0xB0)
      return TILE_WALL;
    else if (dungeonData == 0xB1) // all Bx > B0 different signs?
      return TILE_WALL_SIGN;
    else if (dungeonData == 0xB2 || // if so, more magic codes...
             dungeonData == 0xB3 ||
             dungeonData == 0xB4)
      return TILE_WALL_SIGN;
    else if (dungeonData == 0xC0) // wall decor (skeleton, stalactite, collapse)
      return TILE_ARROW_SLIT;
    else if (dungeonData == 0xD0)
      return TILE_SECRET_DOOR;
    else if (dungeonData == 0xE0)
      return TILE_DOOR;
    else if (dungeonData == 0xF0) // room (thru FF)
      return TILE_BRICK;
    return -1;

    // Often the ladders down seem to be in rooms -
    // but that data comes out of a different file.

    // Interesting to note that the above floor having a pit (and thus allowing klimb up) is not indicated on a given floor, so the "up arrows" on a spoiler map won't appear here
  }
  
  public static short getDungeonMark(int dungeonData) {
    // Look up the map tile that the special dungeon value corresponds to.
    if (dungeonData == 0x08 ||
        dungeonData == 0x10)      // ladder/hole up
	    return FONT_ARROW_UP;
    else if (dungeonData == 0x20 ||
             dungeonData == 0x60) // ladder/hole down
	    return FONT_ARROW_DOWN;
    else if (dungeonData == 0x30 ||
             dungeonData == 0x68 ||
             dungeonData == 0x69) // ladder/hole both
	    return FONT_ARROW_BOTH;
    else if (dungeonData == 0x51) // fountain (unknown type)
      return FONT_1;
    else if (dungeonData == 0x52) // fountain (unknown type)
      return FONT_2;
    else if (dungeonData == 0x53) // fountain (unknown type)
      return FONT_3;
    else if (dungeonData == 0x61) // pit trap
      return FONT_TRIANGLE_DOWN;
    else if (dungeonData == 0xB3) // sign 2
      return FONT_B;
    else if (dungeonData == 0xB4) // sign 3
      return FONT_C;
    else if (dungeonData == 0xD0) // secret door
      return FONT_S;
    else if (dungeonData == 0xF0) // room - special detection (thru FF)
	    return -1;
    return -1;
  }
}

