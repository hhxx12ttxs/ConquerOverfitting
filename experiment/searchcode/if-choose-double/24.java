package fridayTea;

/**
 *
 * @author AwzumCawt
 */
import java.util.ArrayList;
import java.util.Random;

public class Fight {

    static Random r = new Random();
    static Random r2 = new Random();
    static Random r3 = new Random();
    static Random r4 = new Random();
    static Random r5 = new Random();
    static Random r6 = new Random();
    static int damage;
    static int damageDealt;
    static int defence;
    static int defencebymonster;
    static int mdefencebymonster;
    static double spellmatk;
    static double attackatk;
    static boolean firsttime = true;
    static boolean attackchosen = false;
    static boolean magicattack = false;
    static boolean attacking = false;
    static boolean enemyattacking = false;
    static boolean spellchosen = false;
    static boolean atkchosen = false;
    static String enemyattack = "scratch";
    static String attack = "";

    static int damageDealt(Enemy enemy) {
        return (int) (r.nextInt((PlayerPos.maxAttack - PlayerPos.minAttack) + 1)
                + PlayerPos.minAttack + PlayerPos.aatk * attackatk);
    }

    static int magicDealt(Enemy enemy) {
        return (int) (r.nextInt((PlayerPos.maxMaAttack - PlayerPos.minMaAttack) + 1)
                + PlayerPos.minMaAttack + PlayerPos.amatk * spellmatk);
    }

    static int damageBlocked(Enemy enemy) {
        return (r2.nextInt((PlayerPos.maxDefence - PlayerPos.minDefence) + 1)
                + PlayerPos.minDefence + PlayerPos.adef);
    }

    static int magicBlocked(Enemy enemy) {
        return (r3.nextInt((PlayerPos.maxmDefence - PlayerPos.minmDefence) + 1)
                + PlayerPos.minDefence + PlayerPos.amdef);
    }

    static int damageDealtbymonster(Enemy enemy) {
        return (r4.nextInt((enemy.maxAttack - enemy.minAttack) + 1) + enemy.minAttack);
    }

    static int damageBlockedbymonster(Enemy enemy) {
        return (r5.nextInt((enemy.maxDefence - enemy.minDefence) + 1) + enemy.minDefence);
    }

    static int magicBlockedbymonster(Enemy enemy) {
        return (r6.nextInt((enemy.maxmDefence - enemy.minmDefence) + 1) + enemy.minmDefence);
    }

    public static int fajt() {
        Enemy enemy = PlayerPos.map[PlayerPos.x][PlayerPos.y].enemies.get((int) (PlayerPos.map[PlayerPos.x][PlayerPos.y].enemies.size() * Math.random()));
        // Inventory inventory = new Inventory();
        while (true) {
            Main.gui.println("******************");
            if (firsttime == true) {
                attack = "";
                System.out.println(attack);
                GUI.item = new FightGFX("fightbg.png", "player.png", enemy.name + ".png", attack + ".png");
                Main.gui.jpanel.repaint();
                Main.gui.println("You entered a battle with a " + enemy.name
                        + "!");
                firsttime = false;
            } else {
                magicattack = false;
                attackchosen = true;
                boolean active = true;
                Main.gui.update();
                Main.gui.jpanel.repaint();
                if (active == true) {
                    GUI.item = new FightGFX("fightbg.png", "player.png", enemy.name + ".png", attack + ".png");
                    Main.gui.jpanel.repaint();
                    enemyattacking = false;
                    attackchosen = false;
                    spellchosen = false;
                    atkchosen = false;
                    Main.gui.println("What do you do?");
                    Main.gui.println("[1] Attack");
                    Main.gui.println("[2] Use item");
                    Main.gui.println("[3] Run away");
                    int answer = Main.gui.readInt();
                    if (answer == 1) {
                        boolean goback = false;
                        if (goback == false) {
                            while (attackchosen == false) {
                                Main.gui.println("******************");
                                attack = "";

                                GUI.item = new FightGFX("fightbg.png", "player.png", enemy.name + ".png", attack + ".png");
                                Main.gui.jpanel.repaint();
                                Main.gui.println("[1] Physical attack");
                                Main.gui.println("[2] Magic attack");
                                Main.gui.println("[b] Go back");
                                String attacktype2 = Main.gui.readString();
                                int attacktype = 0;
                                try {
                                    attacktype = Integer.parseInt(attacktype2);
                                } catch (Exception e) {
                                }
                                if (attacktype == 1) {
                                    magicattack = false;
                                    attackchosen = true;
                                } else if (attacktype == 2) {
                                    magicattack = true;
                                    attackchosen = true;
                                } else if (attacktype2.equals("b")) {
                                    attackchosen = true;
                                    goback = true;
                                } else {
                                    Main.gui.println("Wrong input");
                                }
                            }
                        }

                        // Main.gui.println(damage + " - " + defencebymonster +
                        // " = " + damageDealt);

                        if (magicattack == false && attackchosen == true && goback == false) {
                            while (atkchosen == false) {
                                Main.gui.println("******************");
                                Main.gui.println("[1]Scratch");
                                if (PlayerPos._str > 120) {
                                    Main.gui.println("[2]Double Scratch");
                                }
                                if (PlayerPos._str > 140) {
                                    Main.gui.println("[3]Tripple Scratch");
                                }
                                Main.gui.println("[b]Go back");
                                String atk2 = Main.gui.readString();
                                int atk = 0;
                                try {
                                    atk = Integer.parseInt(atk2);
                                } catch (Exception e) {
                                }
                                if (atk == 1) {
                                    attackchosen = true;
                                    attack = "scratch";
                                    attackatk = 1.1;
                                    break;
                                } else if (atk == 2 && (PlayerPos._str > 120)) {
                                    attackchosen = true;
                                    attack = "dblscratch";
                                    attackatk = 1.3;
                                    break;
                                } else if (atk == 3 && (PlayerPos._str > 140)) {
                                    attackchosen = true;
                                    attack = "trpscratch";
                                    attackatk = 1.5;
                                    break;
                                } else if (atk2.equals("b")) {
                                    atkchosen = true;
                                    goback = true;
                                } else {
                                    Main.gui.println("Wrong input");
                                }
                            }
                            damage = damageDealt(enemy);
                            defencebymonster = damageBlockedbymonster(enemy);
                            damageDealt = damage - defencebymonster;
                        } else if (magicattack == true && attackchosen == true && goback == false) {
                            while (spellchosen == false) {
                                Main.gui.println("******************");
                                Main.gui.println("You have " + PlayerPos.currentMana + "Mana left.");
                                Main.gui.println("[1]Fireball [10mp]");
                                if (PlayerPos._int > 30) {
                                    Main.gui.println("[2]Waterstomp [20mp]");
                                }
                                if (PlayerPos._int > 50) {
                                    Main.gui.println("[3]Huge Fireball [50mp]");
                                }
                                Main.gui.println("[b]Go back");
                                String spell2 = Main.gui.readString();
                                int spell = 0;
                                try {
                                    spell = Integer.parseInt(spell2);
                                } catch (Exception e) {
                                }
                                if (spell == 1) {
                                    if (PlayerPos.currentMana >= 10) {
                                        attackchosen = true;
                                        attack = "fireball";
                                        spellmatk = 1.15;
                                        PlayerPos.currentMana -= 10;
                                        break;
                                    } else {
                                        Main.gui.println("You dont have enough mana for this spell");
                                    }
                                } else if (spell == 2 && (PlayerPos._int > 30)) {
                                    if (PlayerPos.currentMana >= 20) {
                                        attackchosen = true;
                                        attack = "waterstomp";
                                        spellmatk = 1.45;
                                        PlayerPos.currentMana -= 20;
                                        break;
                                    } else {
                                        Main.gui.println("You dont have enough mana for this spell");
                                    }
                                } else if (spell == 3 && (PlayerPos._int > 50)) {
                                    if (PlayerPos.currentMana >= 50) {
                                        attackchosen = true;
                                        attack = "hugefireball";
                                        spellmatk = 1.75;
                                        PlayerPos.currentMana -= 50;
                                        break;
                                    } else {
                                        Main.gui.println("You dont have enough mana for this spell");
                                    }
                                } else if (spell2.equals("b")) {
                                    spellchosen = true;
                                    goback = true;
                                } else {
                                    Main.gui.println("Wrong input");
                                }
                            }
                            damage = magicDealt(enemy);
                            defencebymonster = magicBlockedbymonster(enemy);
                            damageDealt = damage - mdefencebymonster;
                        } else {
                            answer = 0;
                        }
                        if (attackchosen == true && goback == false) {
                            double CharacterHitChance = Math.random();
                            double monsterHitChance = Math.random();
                            if (CharacterHitChance > enemy.accuracy) {
                                if (damageDealt > 0) {
                                    attacking = true;
                                    enemy.health -= damageDealt;
                                    Main.gui.println("******************");
                                    Main.gui.println("You dealt " + damageDealt
                                            + " damage to the " + enemy.name + ".");
                                    if (enemy.health < 1) {
                                        Main.gui.println("The " + enemy.name
                                                + " has 0HP left.");
                                    } else {
                                        Main.gui.println("The " + enemy.name + " has "
                                                + enemy.health + "HP left.");
                                    }
                                } else {
                                    Main.gui.println("******************");
                                    Main.gui.println("Your attack got blocked.");
                                    attacking = true;
                                    attack = "block";
                                    Main.gui.jpanel.repaint();
                                }
                            } else {
                                Main.gui.println("******************");
                                Main.gui.println("You missed.");
                                attacking = true;
                                attack = "miss";
                                Main.gui.jpanel.repaint();
                            }
                            if (enemy.health < 1) {
                                Main.gui.println("Nice, you just pwned an "
                                        + enemy.name + "!");
                                Main.gui.println("******************");
                                Main.gui.println("You gained " + enemy.exp
                                        + " exp and " + enemy.coins + " coins!");
                                PlayerPos.coins += enemy.coins;
                                PlayerPos.exp += enemy.exp;
                                Main.gui.update();
                                PlayerPos.map[PlayerPos.x][PlayerPos.y].enemies.remove(enemy);
                                if (enemy.respawnable) {
                                    try {
                                        PlayerPos.map[PlayerPos.x][PlayerPos.y].enemies.add(enemy.getClass().newInstance());
                                    } catch (InstantiationException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (PlayerPos.exp > (PlayerPos.level * 10)) {
                                    PlayerPos.levelUp();
                                    Main.gui.update();
                                } else {
                                    Main.gui.println("You have "
                                            + PlayerPos.exp
                                            + " exp! You will need "
                                            + ((PlayerPos.level * 10) - PlayerPos.exp)
                                            + " more experience points to level up to level "
                                            + (PlayerPos.level + 1) + "!");
                                    Main.gui.println("******************");
                                }
                                firsttime = true;
                                return 1;
                            }

                            if (enemy.health < 1) {
                                enemy.health = 0;
                                Main.gui.update();
                            }

                            if (PlayerPos.currentHealth < 1) {
                                PlayerPos.currentHealth = 0;
                                Main.gui.update();
                            }

                            if (monsterHitChance > PlayerPos.accuracy) {
                                int damagebymonster = damageDealtbymonster(enemy);
                                defence = damageBlocked(enemy);
                                int damageDealtbymonster = damagebymonster - defence;
                                // Main.gui.println(damagebymonster + " - " + defence +
                                // " = " + damageDealtbymonster);
                                if (damageDealtbymonster > 0) {
                                    /*enemyattacking = true;
                                    Main.gui.jpanel.repaint();
                                    FightGFX._scratch1 = true;
                                    Main.gui.jpanel.repaint();
                                    FightGFX._scratch2 = true;
                                    Main.gui.jpanel.repaint();
                                    FightGFX._scratch3 = true;*/
                                    Main.gui.println("Ouch, you got hit by "
                                            + damageDealtbymonster + "HP!");
                                    PlayerPos.currentHealth -= damageDealtbymonster;
                                    Main.gui.update();
                                    if (PlayerPos.currentHealth < 1) {
                                        Main.gui.println("You have 0HP left.");
                                    } else {
                                        Main.gui.println("You have "
                                                + PlayerPos.currentHealth + "HP left.");
                                    }
                                } else {
                                    Main.gui.println("You blocked the " + enemy.name
                                            + "'s attack.");
                                }
                            } else {
                                Main.gui.println("The " + enemy.name + " missed.");
                            }

                            if (PlayerPos.currentHealth < 1) {
                                Main.gui.println("You died.");
                                return 2; // returnera 2 tillbax till Main
                            }
                        }
                    } else if (answer == 2) {
                        ArrayList<Item> items = new ArrayList<Item>();
                        for (Item item : PlayerPos.inventory) {
                            if (item != null) {
                                if (item.usabelinfight) {
                                    items.add(item);
                                }
                            }
                        }
                        if (!items.isEmpty()) {
                            Main.gui.println("******************");
                            Main.gui.println("Choose item to use");
                            Main.gui.println("******************");
                            for (Item item : items) {
                                Main.gui.println("["
                                        + (items.indexOf(item) + 1)
                                        + "] "
                                        + item.name
                                        + (item.stackable ? " (x" + item.amount
                                        + ")" : ""));
                            }
                            Main.gui.println("[b] Go back");
                            while (true) {
                                String choose = Main.gui.readString();
                                int chose = -1;
                                try {
                                    chose = Integer.parseInt(choose);
                                    chose--;
                                } catch (Exception e) {
                                }
                                if (chose >= 0 && chose < items.size()) {
                                    if (items.get(chose).use()) {
                                        if (items.get(chose).consumable) {
                                            items.get(chose).amount -= 1;
                                            if (items.get(chose).amount <= 0) {
                                                PlayerPos.remove(items.get(chose));
                                            }
                                        }
                                    }
                                    break;
                                } else if (choose.equals("b")) {
                                    break;
                                } else {
                                    Main.gui.println("Wrong input");
                                }
                            }
                        } else {
                            Main.gui.println("No usable items in inventory.");
                        }
                    } else if (answer == 3) { // ifall man flyr
                        firsttime = true; // resetta firsttime
                        Main.gui.update();
                        return 3; // returnera 3 tillbax till Main
                    }
                }
            }
        }
    }
}

