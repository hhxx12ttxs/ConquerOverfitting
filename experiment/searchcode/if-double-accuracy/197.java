package fridayTea;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import fridayTea.enemies.Pacman;
import fridayTea.item.consumable.SmallPotion;
import fridayTea.item.equipment.armors.BloodArmor;
import fridayTea.item.equipment.helmets.AfroHelmet;
import fridayTea.item.equipment.helmets.GoldHelmet;
import fridayTea.item.equipment.helmets.LeatherHelmet;
import fridayTea.item.equipment.helmets.MasterChiefHelmet;
import fridayTea.item.equipment.helmets.StormTrooperHelmet;
import fridayTea.item.equipment.shoes.BloodShoes;
import fridayTea.item.equipment.weapons.BloodSword;
import fridayTea.item.equipment.weapons.DarkSword;
import fridayTea.item.equipment.weapons.IchigoUnr;
import fridayTea.item.equipment.weapons.RedStaff;
import fridayTea.item.equipment.weapons.WGiantDagger;

/**
 * 
 * @author MrOpposite
 */
public class PlayerPos {

    // --------------------//
    // Old Character.java //
    // --------------------//
    public static String name;
    public static int maxHealth = 50;
    public static int currentHealth = 50;
    public static int maxMana = 50;
    public static int currentMana = 50;
    public static int minDefence = 1;
    public static int maxDefence = 2;
    public static int minmDefence = 1;
    public static int maxmDefence = 2;
    public static double accuracy = 0.2;
    public static int level = 1;
    public static int exp = 0;
    public static int adef = 0;
    public static int amdef = 0;
    public static int aatk = 0;
    public static int amatk = 0;
    public static int minMaAttack = 2;
    public static int maxMaAttack = 4;
    public static int minAttack = 1;
    public static int maxAttack = 4;
    public static int _str = 100;
    public static int _int = 10;
    public static int skillpoints = 10;
    public static BufferedImage playerImg;
    public static BufferedImage playerImgInv;
    public static Equipment helm = null;
    public static Equipment rhand = null;
    public static Equipment lhand = null;
    public static Equipment armor = null;
    public static Equipment shoes = null;
    public static BufferedImage hair = null;
    public static BufferedImage hairinv = null;
    public static int coins = 20;
    public static int weight = 0;
    public static ArrayList<Item> inventory = new ArrayList<Item>() {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public boolean add(Item e) {
            if (e.stackable) {
                for (int i = 0; i < size(); i++) {
                    if (get(i).getClass() == e.getClass()) {
                        get(i).amount++;
                        return true;
                    }
                }
            }
            if (size() < 30) {
                super.add(e);
                return true;
            }
            return false;
        }
    };

    public static void allocateSkillpoints() {
        boolean active = true;
        while (skillpoints > 0 && active == true) {
            Main.gui.println("******************");
            Main.gui.println("You have " + skillpoints + " skillpoints left. \nPick a selection:");
            Main.gui.println("[1]Help\n[2]Increase Strenght\n[3]Increase Inteligence\n[b]Go back");
            String answer = Main.gui.readString();
            int choise = -1;
            try {
                choise = Integer.parseInt(answer);
            } catch (Exception e) {
            }
            if (choise == 1) {
                skillHelp();
            } else if (choise == 2) {
                while (true) {
                    Main.gui.println("******************");
                    Main.gui.println("Do you want to increase your strenght by 1? [y/n]");
                    String answer2 = Main.gui.readString();
                    if (answer2.equals("y")) {
                        skillpoints--;
                        _str++;
                        Main.gui.update();
                        Main.gui.println("You increased your strenght, you now have " + _str + " strenght.");
                        break;
                    } else if (answer2.equals("n")) {
                        break;
                    } else {
                        Main.gui.println("Wrong input");
                    }
                }
            } else if (choise == 3) {
                while (true) {
                    Main.gui.println("******************");
                    Main.gui.println("Do you want to increase your inteligence by 1? [y/n]");
                    String answer2 = Main.gui.readString();
                    if (answer2.equals("y")) {
                        skillpoints--;
                        _int++;
                        Main.gui.update();
                        Main.gui.println("You increased your inteligence, you now have " + _int + " inteligence.");
                        break;
                    } else if (answer2.equals("n")) {
                        break;
                    } else {
                        Main.gui.println("Wrong input");
                    }
                }
            } else if (answer.equals("b")) {
                break;
            } else {
                Main.gui.println("Wrong input");
            }
        }
    }

    public static void skillHelp() {
        while (true) {
            Main.gui.println("******************");
            Main.gui.println("~~~Help~~~");
            Main.gui.println("******************");
            Main.gui.println("[1]Strenght\n[2]Int\n[3]Physical Attacks\n[4]Spellbook\n[b]Go back");
            String answer2 = Main.gui.readString();
            int choise2 = -1;
            try {
                choise2 = Integer.parseInt(answer2);
            } catch (Exception e) {
            }
            if (choise2 == 1) {
                Main.gui.println("*");
                Main.gui.println("Strenght will increase your weight-capacity, your physicaldamage \nand also unlock new physical-attacks");
                Main.gui.println("Strenght type of build will highly increase your defence capabilities \nbecause of added weight-capacity so you can wear heavy armors.");
            } else if (choise2 == 2) {
                Main.gui.println("*");
                Main.gui.println("Inteligence will increase your magicdamage and unlock new spells");
                Main.gui.println("Inteligence type of build will highly increase your damage \nbut will also leave you very vulnerable to opponent damage.");
            } else if (choise2 == 3) {
                boolean active2 = true;
                while (active2 == true) {
                    Main.gui.println("******************");
                    Main.gui.println("~~~Physical Attacks~~~");
                    Main.gui.println("******************");
                    Main.gui.println("[1]Scratch\n[2]Double Scratch\n[3]Tripple Scratch\n[b]Go back");
                    String answer3 = Main.gui.readString();
                    int choise3 = -1;
                    try {
                        choise3 = Integer.parseInt(answer3);
                    } catch (Exception e) {
                    }
                    if (choise3 == 1) {
                        Main.gui.println("******************");
                        Main.gui.println("Scratch: \nAttack +10%");
                    } else if (choise3 == 2) {
                        Main.gui.println("******************");
                        Main.gui.println("Double Scratch: \nAttack +30% \nRequires: minimum of 120 Strenght");
                    } else if (choise3 == 3) {
                        Main.gui.println("******************");
                        Main.gui.println("Tripple Scratch: \nAttack +50% \nRequires: minimum of 140 Strenght");
                    } else if (answer3.equals("b")) {
                        break;
                    } else {
                        Main.gui.println("Wrong input.");
                    }
                }
            } else if (choise2 == 4) {
                boolean active3 = true;
                while (active3 == true) {
                    Main.gui.println("******************");
                    Main.gui.println("~~~Spellbook~~~");
                    Main.gui.println("******************");
                    Main.gui.println("[1]Fireball\n[2]Waterstomp\n[3]Huge Fireball\n[b]Go back");
                    String answer3 = Main.gui.readString();
                    int choise3 = -1;
                    try {
                        choise3 = Integer.parseInt(answer3);
                    } catch (Exception e) {
                    }
                    if (choise3 == 1) {
                        Main.gui.println("******************");
                        Main.gui.println("Fireball: \nMagic Attack +15%");
                    } else if (choise3 == 2) {
                        Main.gui.println("******************");
                        Main.gui.println("Waterstomp: \nMagic Attack +45% \nRequires: minimum of 30 Inteligence");
                    } else if (choise3 == 3) {
                        Main.gui.println("******************");
                        Main.gui.println("Huge Fireball: \nAttack +75% \nRequires: minimum of 50 Inteligence");
                    } else if (answer3.endsWith("b")) {
                        active3 = false;
                    } else {
                        Main.gui.println("Wrong input.");
                    }
                }
            } else if (answer2.equals("b")) {
                break;
            } else {
                Main.gui.println("Wrong input.");
            }
        }
    }

    static int levelUp() {
        Main.gui.println("Gratz! You leveled up! :D You are now level "
                + (level + 1) + "!!");
        Main.gui.println("You feel much stronger now, and your attributes have increased!");
        minAttack += 1;
        maxAttack += 3;
        accuracy *= 1.2;
        maxHealth += 10;
        currentHealth = maxHealth;
        maxMana += 10;
        currentMana = maxMana;
        minDefence += 1;
        maxDefence += 3;
        exp = 0;
        return level += 1;
    }

    public static void leetMode() {
        Main.gui.println("leet mode activated ^_^");
        minAttack = 1337;
        minMaAttack = 1337;
        maxAttack = 1337;
        maxMaAttack = 1337;
        maxHealth = 1337;
        maxMana = 1337;
        currentMana = 1337;
        minDefence = 1337;
        maxDefence = 1337;
        currentHealth = 1337;
        level = 1337;
        coins = 1337;
        _int = 1337;
        _str = 1337;
        skillpoints = 1337;
        Main.gui.update();
    }

    public static void restoreHealth() {
        currentHealth = maxHealth;
    }

    public static void restoreMana() {
        currentMana = maxMana;
    }

    // ------------------------//
    // End old Character.java //
    // ------------------------//
    public static void startRoom() {
        Main.gui.println(map[x][y].info);
        if (!map[x][y].items.isEmpty()) {
            Main.gui.println("\nYou see this here:");
            for (int i = 0; i < map[x][y].items.size(); i++) {
                Main.gui.println(map[x][y].items.get(i).name);
            }
        }
    }
    public static int x = 500;
    public static int y = 400;
    public static boolean facedRight = true;
    public static Map map[][] = new Map[1000][1000];
    public static PlayerPos player = null;
    public static String gender = "shemale";

    public static void init() {
        try {
            playerImg = ImageIO.read(Main.class.getClass().getResource(
                    "/fridayTea/images/playershemale.png"));
            playerImgInv = ImageIO.read(Main.class.getClass().getResource(
                    "/fridayTea/images/playershemale.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: remove when done debugging
        inventory.add(new SmallPotion());// 1
        inventory.add(new MasterChiefHelmet());// 2
        inventory.add(new DarkSword());// 3
        inventory.add(new BloodSword());// 4
        inventory.add(new StormTrooperHelmet());// 5
        inventory.add(new AfroHelmet());// 6
        inventory.add(new WGiantDagger());// 7
        inventory.add(new WGiantDagger());// 8
        inventory.add(new GoldHelmet());// 9
        inventory.add(new BloodShoes());// 10
        inventory.add(new BloodArmor());// 11
        inventory.add(new IchigoUnr());// 12
        inventory.add(new RedStaff());// 13

        map[497][397] = new Map("You entered a small place with a minimalistic shop.", "grass.png");
        map[496][397] = new Map("You see nothing but grass.", "grass.png");
        map[495][397] = new Map("You see nothing but grass.", "grass.png");
        map[495][396] = new Map("You see nothing but grass.", "grass.png");
        map[498][397] = new Map("You see something that moves in the bushes.", "grass.png");
        map[499][397] = new Map("You see nothing but grass.", "grass.png");
        map[499][398] = new Map("You see nothing but grass.", "grass.png");
        map[499][399] = new Map("You see nothing but grass.", "grass.png");
        map[498][399] = new Map("You see nothing but grass.", "grass.png");
        map[500][399] = new Map("You see nothing but grass.", "grass.png");
        map[500][400] = new Map("You see nothing but grass.", "grass.png");
        map[500][401] = new Map("You see nothing but grass.", "grass.png");



        map[497][397].examinate.put("shop", "A small shop which dosen't have many items for sale.");

        map[498][397].enemies.add(new Pacman());

        map[497][397].shoplist.add(new SmallPotion());
        map[497][397].shoplist.add(new LeatherHelmet());

        map[496][397].items.add(new SmallPotion());

        map[496][397].conEast = true;
        map[496][397].conWest = true;
        map[495][397].conEast = true;
        map[495][397].conNorth = true;
        map[495][396].conSouth = true;
        map[497][397].conWest = true;
        map[497][397].conEast = true;
        map[498][397].conEast = true;
        map[498][397].conWest = true;
        map[499][397].conSouth = true;
        map[499][397].conWest = true;
        map[499][398].conSouth = true;
        map[499][398].conNorth = true;
        map[499][399].conWest = true;
        map[499][399].conNorth = true;
        map[499][399].conEast = true;
        map[500][399].conSouth = true;
        map[500][399].conWest = true;
        map[500][400].conNorth = true;
        map[500][400].conSouth = true;
        map[500][401].conNorth = true;
        map[498][399].conEast = true;
    }

    public static boolean moveNorth() {
        if (map[x][y].conNorth) {
            y -= 1;
            MapGFX.north = true;
            MapGFX.east = false;
            MapGFX.south = false;
            MapGFX.west = false;
            Main.gui.println("Went north");
            startRoom();
            Main.gui.jpanel.repaint();
            return true;
        } else {
            Main.gui.println("You can not go there");
            return false;
        }
    }

    public static boolean moveSouth() {
        if (map[x][y].conSouth) {
            y += 1;
            MapGFX.north = false;
            MapGFX.east = false;
            MapGFX.south = true;
            MapGFX.west = false;
            Main.gui.println("Went south");
            Main.gui.jpanel.repaint();
            return true;
        } else {
            Main.gui.println("You can not go there");
            return false;
        }
    }

    public static boolean moveWest() {
        if (map[x][y].conWest) {
            x -= 1;
            MapGFX.north = false;
            MapGFX.east = false;
            MapGFX.south = false;
            MapGFX.west = true;
            Main.gui.println("Went west");
            facedRight = false;
            Main.gui.jpanel.repaint();
            return true;
        } else {
            Main.gui.println("You can not go there");
            return false;
        }
    }

    public static boolean moveEast() {
        if (map[x][y].conEast) {
            x += 1;
            MapGFX.north = false;
            MapGFX.east = true;
            MapGFX.south = false;
            MapGFX.west = false;
            Main.gui.println("Went east");
            facedRight = true;
            Main.gui.jpanel.repaint();
            return true;
        } else {
            Main.gui.println("You can not go there");
            return false;
        }
    }

    public static void remove(Item item) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i) == item) {
                inventory.remove(i);
            }
        }
    }

    public static boolean copy(Item item) {
        if (item.stackable) {
            for (int i = 0; i < inventory.size(); i++) {
                if (inventory.get(i).getClass() == item.getClass()) {
                    inventory.get(i).amount++;
                    return true;
                }
            }
        }
        try {
            return inventory.add(item.getClass().newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void drawPlayer(Graphics g, int x, int y, boolean inv) {
        if (!inv) {
            g.drawImage(playerImg, x - playerImg.getWidth() / 2,
                    y - playerImg.getHeight() / 2, null);
            g.drawImage(hair, x - playerImg.getWidth() / 2, y
                    - playerImg.getHeight() / 2, null);
            if (armor != null) {
                g.drawImage(armor.mapImg, x - playerImg.getWidth() / 2, y
                        - playerImg.getHeight() / 2, null);
            }
            if (helm != null) {
                g.drawImage(helm.mapImg, x - playerImg.getWidth() / 2, y
                        - playerImg.getHeight() / 2, null);
            }
            if (rhand != null) {
                g.drawImage(rhand.mapImg, x - playerImg.getWidth() / 2 + rhand.xoffset,
                        y - playerImg.getHeight() / 2 + rhand.yoffset, null);
            }
            if (lhand != null) {
                g.drawImage(lhand.mapImg, x - playerImg.getWidth() / 2, y
                        - playerImg.getHeight() / 2, null);
            }
            if (shoes != null) {
                g.drawImage(shoes.mapImg, x - playerImg.getWidth() / 2, y
                        - playerImg.getHeight() / 2, null);
            }
        } else {
            g.drawImage(playerImgInv, x - playerImgInv.getWidth() / 2 - 7, y
                    - playerImgInv.getHeight() / 2, null);
            g.drawImage(hairinv, x - playerImgInv.getWidth() / 2 - 7, y
                    - playerImgInv.getHeight() / 2, null);
            if (armor != null) {
                g.drawImage(armor.mapImgInv,
                        x - playerImgInv.getWidth() / 2 - 7 - (armor.mapImgInv.getWidth() - playerImgInv.getWidth()),
                        y - playerImgInv.getHeight() / 2, null);
            }
            if (helm != null) {
                g.drawImage(helm.mapImgInv, x - playerImgInv.getWidth() / 2 - 7 - (helm.mapImgInv.getWidth() - playerImgInv.getWidth()),
                        y - playerImgInv.getHeight() / 2, null);
            }
            if (rhand != null) {
                g.drawImage(rhand.mapImgInv, x - playerImgInv.getWidth() / 2 - 7 - (rhand.mapImgInv.getWidth() - playerImgInv.getWidth())
                        - rhand.xoffset, y - playerImgInv.getHeight() / 2 - rhand.yoffset, null);
            }
            if (lhand != null) {
                g.drawImage(lhand.mapImgInv, x - playerImgInv.getWidth() / 2 - 7 - (lhand.mapImgInv.getWidth() - playerImgInv.getWidth()),
                        y - playerImgInv.getHeight() / 2, null);
            }
            if (shoes != null) {
                g.drawImage(shoes.mapImgInv, x - playerImgInv.getWidth() / 2 - 7 - (shoes.mapImgInv.getWidth() - playerImgInv.getWidth()),
                        y - playerImgInv.getHeight() / 2, null);
            }
        }
    }
}

