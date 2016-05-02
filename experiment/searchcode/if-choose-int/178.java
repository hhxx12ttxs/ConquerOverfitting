package planty;

import java.util.Random;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * The UI to handle a single plant.
 *
 * @author Nereare
 * @version 2.6.22r03
 */
public class Panel {

    // <editor-fold defaultstate="collapsed" desc="game variables">
    /**
     * If the program must keep on.
     */
    private boolean keep_on = true;
    /**
     * Whether the program should use the short version of the menu or not.
     */
    private boolean shorty = false;
    /**
     * If there is a plant created.
     */
    private boolean plant_exists = false;
    /**
     * If the user can harvest yet.
     * <br />
     * Fixes the multiple harvest issue.
     */
    private boolean can_harvest = true;
    /**
     * The plant.
     */
    private Sprouling plant;
    /**
     * The weather.
     */
    private Weather weather = new Weather(true);
    /**
     * The pseudorandom number generator.
     */
    private Random rnd = new Random( (int) (Math.random() * 1000000000) );
    /**
     * The absolute day of the game.
     */
    private int abs_day = 0;
    /**
     * The current day of the current month (ranges from 0 to days_by_month).
     */
    private int day = 0;
    /**
     * The current month (ranges from 0 to season.length).
     */
    private int month = 0;
    /**
     * The current year (starts from 0).
     */
    private int year = 0;
    /**
     * The number of days in each month.
     */
    private static final int days_by_month = 15;
    /**
     * Seasons names.
     */
    private static final String season[] = {"Spring", "Summer", "Autumn", "Winter"};
    /**
     * The number of fruits in your possession.
     */
    private int inventory = 0;
    /**
     * The state of watering of the plant for the day.
     */
    private boolean watering = false;
    /**
     * When it reaches <code>1d</code>, the plant's health heals by one.
     */
    private double talk_bonus = 0d;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="echoes and reads">
    /**
     * Executes a line-reading of the default <code>InputStream</code>.
     *
     * @return  either the data inputed to the command-line or <code>null</code> in case of error.
     */
    private String read() {
        try {
            return (new BufferedReader(new InputStreamReader(System.in))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Runs the <code>read()</code> method and converts the input to <code>int</code> format.
     *
     * @return  the user input as a integer number.
     */
    private int readInt() {
        int returnable = 0;
        boolean ok = false;

        while (!ok) {
            try {
                returnable = Integer.parseInt( read() );
                ok = true;
            }
            catch (NumberFormatException e) {
                echoErr("USE NUMBERS ONLY!");
            }
        }

        return returnable;
    }

    /**
     * Reades the default <code>InputStream</code> for an &quot;yes&quot; or &quot;no&quot;.
     *
     * @return  <code>false</code> if the user input "n", <code>true</code> otherwise.
     */
    private boolean yes() {
        if ( !read().equalsIgnoreCase("n") ) return true;
        else return false;
    }

    /**
     * Prints an empty line in the default <code>PrintStream</code>.
     */
    private void echo() {
        System.out.println();
    }

    /**
     * Prints the given content.
     *
     * @param o something to print.
     */
    private void echo(Object o) {
        System.out.println(o);
    }

    /**
     * Prints something to the default error <code>PrintStream</code>.
     *
     * @param o the thing to print.
     */
    private void echoErr(Object o) {
        System.err.println(o);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="game features">
    /**
     * Procedure to create a plant.
     */
    private void createPlant() {
        String name, species, desc;
        int minFruit, maxFruit, fruitTime, maxAge;

        echo("What will be your plant\'s name?");
        name = read();
        echo("And which will be " + name + "\'s species? (no need to go scietistic, ok? xD)");
        species = read();
        echo("Describe " + name + " for us.");
        desc = read();

        echo("How many fruits can " + name + " bear at most? (numbers, please.)");
        maxFruit = readInt();
        echo("Which is the time for its fruits to be ripe?");
        fruitTime = readInt();
        echo("And what is its life span?");
        maxAge = readInt();
        echo("Will you want to determine the minimum of fruit bearing? [Y/n]");
        if (yes()) {
            echo("Then what's its minimum?");
            minFruit = readInt();
            plant = new Sprouling(name, species, desc, minFruit, maxFruit, fruitTime, maxAge);
        } else {
            echo("Then it will be zero, like normal plants! ;D");
            plant = new Sprouling(name, species, desc, maxFruit, fruitTime, maxAge);
        }
        echo("Congratulations! Your plant, " + name + ", was created!");
        plant.renew();
    }

    /**
     * Procedure to load a plant either from a list of possible precreated plants or from a file.
     */
    private void loadPlant() {
        boolean choose = true;
        while (choose) {
            echo("Load from list? [Y/n]");
            if (yes()) { plant = PlantList.loadFromList(); choose = false; }
            else if ( (plant = FileHandler.load()) != null ) choose = false;
        }
        plant.renew();
    }

    /**
     * Procedure to run when plant dies.
     */
    private void plantDied() {
        echo("Oh my! My deepest sympathies, it seems your beloved " +
                plant.getName() + " has died.");
        echo("Do you wish to ease your pain by getting a new one? [Y/n]");
        if (yes()) {
            echo("Load a plant? [Y/n]");
            if (yes()) loadPlant();
            else createPlant();
        }
        else {
            keep_on = false;
        }
    }

    /**
     * Sets the date variables to a new day, based only on the game's <code>abs_day</code>.
     */
    private void setDate() {
        day = ( abs_day % days_by_month );
        month = ( ((int) abs_day / 15) % season.length );
        year = (int) ( ((int) abs_day / 15) / season.length );
    }

    /**
     * Formats the options to display.
     *
     * @param col_num   number of columns of the formatation.
     * @return          these options formatted in a <code>n</code> columns aspect.
     */
    private String options(int col_num) {
        // REMEMBER, WHEN ADDING NEW FIELDS: add enogh leading spaces to fill 30 of length for all!
        String options[] = new String[9];
        options[0] = "See plant details (D)         ";
        options[1] = "Go to next day (G)            ";
        options[2] = "Check inventory (I)           ";
        options[3] = "Water it(W)                   ";
        options[4] = "Talk to it (T)                ";
        // These options are in separated strings for both they are sazonal or,
        // in the exit case, they come at last.
        final String emergency_option = "Apply medicine (M)            "; // Only if health <= 2.
        final String harvest_option = "Harverst fruits (H)           "; // Only on harvest days, of course.
        final String save_option = "Save (S)                      "; // Allways before exit.
        final String exit_option = "Exit (X)                      "; // Allways the last, and hence in separate.
        // Fixes the "null" appearence issue, when array is not full.
        int length = options.length;
        // Initializing return string, only to avoid NullPointer issues.
        String returnable = "";
        
        int k = 5;
        // Adds the emergency option if health is too low.
        if ( plant.getHealth() < 3 ) { options[k] = emergency_option; k++; }
        else length--;
        // Adds the harvest option if it is harvest time.
        if ( plant.isFruitTime() && can_harvest ) { options[k] = harvest_option; k++; }
        else length--;
        // Adds the save option, before exit.
        options[k] = save_option; k++;
        // Adds the exit option at last.
        options[k] = exit_option;

        int i = 0;
        while ( i < length ) {
            for ( int i2 = 0; i2 < col_num; i2++ ) {
                returnable += options[i];
                i++;
                if ( i == length ) break;
            }
            returnable += "\n";
        }

        return returnable;
    }

    /**
     * Formats the options to display with only the "hotletters".
     *
     * @return  the "hotletters" of the options avaiable, in a single line.
     */
    private String simple_options() {
        String returnable = "(D), (G), (I), (W), (T)";
        if ( plant.getHealth() < 3 ) returnable += ", (M)";
        if ( plant.isFruitTime() & can_harvest ) returnable += ", (H)";
        returnable += ", (S), (X)";
        return returnable;
    }

    /**
     * Checks the choice from the options menu.
     * <br />
     * <strong>Must be used <em>only</em> after calling options() or simple_options()!</strong>
     */
    private void check_choice() {
        // Possible hotletters: S(ee details), G(o to), I(nventory), W(ater), T(alk),
        //                      M(edicine), H(arvest) and e(X)it.
        int index = 0;

        String choice = read().toLowerCase();
        if ( choice.length() > 1 ) {
            if ( choice.contains("cheat") ) {
                if ( choice.equals("cheat_kill") ) index = -3;
                else {
                    if ( choice.equals("cheat_motherlode") ) index = -2;
                    else if ( choice.equals("cheat_total_heal") ) index = -1;
                }
            }
            else {
                if ( choice.contains("detail") || choice.contains("see") ) index = 1;
                else {
                    if ( choice.contains("next") || choice.contains("day") ) index = 2;
                    else {
                        if ( choice.contains("inventory") ) index = 3;
                        else {
                            if ( choice.contains("water") ) index = 4;
                            else {
                                if ( choice.contains("talk") ) index = 5;
                                else {
                                    if ( plant.getHealth() < 3 && (choice.contains("medicine")
                                            || choice.contains("heal")) ) index = 6;
                                    else {
                                        if ( plant.isFruitTime() && can_harvest && choice.contains("harvest") )
                                            index = 7;
                                        else {
                                            if ( choice.contains("save") ) index = 8;
                                            else if ( choice.contains("exit") || choice.contains("quit") ) index = 9;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            if ( choice.equals("d") ) index = 1;
            else {
                if ( choice.equals("g") ) index = 2;
                else {
                    if ( choice.equals("i") ) index = 3;
                    else {
                        if ( choice.equals("w") ) index = 4;
                        else {
                            if ( choice.equals("t") ) index = 5;
                            else {
                                if ( plant.getHealth() < 3 && choice.equals("m") )
                                    index = 6;
                                else {
                                    if ( plant.isFruitTime() && can_harvest && choice.equals("h") )
                                        index = 7;
                                    else {
                                        if ( choice.equals("s") ) index = 8;
                                        else if ( choice.equals("x") ) index = 9;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        switch (index) {
            case -3:
                cheat_KILL();
                break;
            case -2:
                cheat_MOTHERLODE();
                break;
            case -1:
                cheat_TOTAL_HEAL();
                break;
            case 1:
                see_details();
                break;
            case 2:
                next_day();
                break;
            case 3:
                inventory();
                break;
            case 4:
                water();
                break;
            case 5:
                talk();
                break;
            case 6:
                medicine();
                break;
            case 7:
                harvest();
                break;
            case 8:
                save();
                break;
            case 9:
                exit();
                break;
            default:
                ops();
                break;
        }
    }

    /**
     * The game's full main menu.
     */
    private void menu() {
        echo("\n\nToday is day " + (day + 1) + " of " + season[month] + ", year " + (year + 1) + ". It is "
                + weather.getTemperatureName() + " and " + weather.getWeatherName() + ".");
        echo("You have " + inventory + " fruits in your inventory.");
        echo(plant.getName() + " is " + plant.getAge() + " days old and its health " +
                "level is " + plant.getHealth(plant.getHealth()) + ".");
        if ( plant.isLastDay() ) echo("Today is " + plant.getName() + "\'s last day. Remember to bid your farewell");
        if ( plant.getHealth() < 3 ) echo("WARNING! Your plant is in very low health! Heal it!");
        if ( plant.isFruitTime() && can_harvest ) echo("Today " + plant.getName() + " is bearing juicy fruits! " +
                "Be aware that they will wither away if you do not harvest them!");
        echo("You may:\n" + options(3) );
        check_choice();
    }

    /**
     * The game's short main menu.
     */
    private void short_menu() {
        echo("Day " + (day + 1) + " of " + season[month] + ", year " + (year + 1) + " | " + weather.getTemperatureName() +
                " and " + weather.getWeatherName() + " | " + inventory + " fruits");
        echo(plant.getName() + ", " + plant.getAge() + " d.o., " + plant.getHealth(plant.getHealth()));
        if ( plant.isLastDay() ) echo(plant.getName() + "\'s last day.");
        if ( plant.getHealth() < 3 ) echo("WARNING: Low health!");
        if ( plant.isFruitTime() && can_harvest ) echo("There\'s fruits to harvest.");
        echo( simple_options() );
        check_choice();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="option methods">
    /**
     * See plant details (S) choice.
     * <br />
     * Shows the full data for the plant and leads back the menu's short form.
     */
    private void see_details() {
        echo(plant.getName() + ", a " + plant.getHealth(plant.getHealth()) + " " + plant.getSp());
        echo("About: " + plant.getDescription());
        echo("Age: " + plant.getAge() + " (of " + plant.getMaxAge() +
                " of its lifespan) days old");
        echo("Fruit taxes: " + plant.getMinFruit() + " (min) / " + plant.getMaxFruit() + " (max)");
        echo("Waterings since last harvest: " + plant.getWaterings() + "/" + plant.getFruitTime());
        echo("Days until next harvest: " + ( plant.getFruitTime() - day % plant.getFruitTime() ));
        shorty = true;
        read();
    }

    /**
     * Go to next day (G) option.
     * <br />
     * Checks plant watering, adds a day and ages the plant.
     */
    private void next_day() {
        if (!watering) plant.harm();
        else { if ( talk_bonus >= 0.1d ) plant.heal(); }
        watering = false;
        if ( talk_bonus >= 1d ) plant.heal();
        talk_bonus = 0d;

        abs_day++;
        plant.age();
        setDate();

        echo("You have gone to bed with a fulfilling feeling that you\'ve done " +
                "your job for today and took a good care of " + plant.getName() +
                ".\nA new day was born and you wake up merrily to another day of " +
                "work.");

        can_harvest = true;
        shorty = false;
        read();
    }

    /**
     * Check inventory (I) option.
     * <br />
     * Still doesn't do much.
     */
    private void inventory() {
        echo("You have " + inventory + " fruits in your inventory. Nothing else. xD");

        shorty = false;
        read();
    }

    /**
     * Water (W) choice.
     * <br />
     * Do the watering in the system vars and in the plant.
     */
    private void water() {
        if (!watering) {
            plant.water();
            watering = true;
            echo(plant.getName() + " was watered.");
        } else {
            echo("You\'ve already watered the plant today.");
        }
        shorty = false;
        read();
    }

    /**
     * Talk to it (T) choice.
     * <br />
     * There's no tip ingame, but if you talk to the plant 20 times in a day, it will heal by one.
     */
    private void talk() {
        String pre_msg = "You talked to " + plant.getName() + " for some time.\n";
        String post_msg[] = {
            "\'Twas not so responsive, of course, but you feel as if it had heard you the whole time.",
            "You don\'t know how, but you've heard a sweet slow voice whispering, almost soudlessly, the words \"Thank you for talking to me.\""
        };
        int i = (int) rnd.nextInt(33) / 32;

        echo(pre_msg + post_msg[i]);
        talk_bonus += 0.05d; // 20 talks needed to improve it's health.

        shorty = false;
        read();
    }

    /**
     * Apply medicine (M) option.
     * <br />
     * It charges 3 fruits in charge of one medicine. Each medicine heal de plant by one.
     */
    private void medicine() {
        if ( plant.getHealth() < 3 ) {
            echo("Medicine is not free, you see? There's a price.");
            if ( inventory < 3 ) {
                if ( inventory == 0 ) echo("It's 3 fruits per medicine, you have none.");
                else echo("It's 3 fruits per medicine, you only have " + inventory + ".");
            }
            else {
                echo("It's 3 fruits per medicine and you have " + inventory + ".\n" +
                        "Want to buy one? (A medicine only improves one health!) [Y/n]");
                if (yes()) {
                    int before = plant.getHealth();
                    plant.heal();
                    echo("Here\'s your medicine, it healed " + plant.getName() +
                            " from " + plant.getHealth(before) + " to " +
                            plant.getHealth(plant.getHealth()) + ".");
                }
                else echo("Ok, then.");
            }
        }
        else echo("How the hell have you got in here, for crying out loud?! O_O");
        
        shorty = false;
        read();
    }

    /**
     * Harvest crops (H) choice.
     * <br />
     * Does just the obvious... xD
     */
    private void harvest() {
        int fruits = plant.harvest();
        inventory += fruits;
        echo("Your plant was bearing " + fruits + " juicy delicious fruits, " +
                "you harvested them all and now have " + inventory +
                " mouth-watering fruits in stock.");

        can_harvest = false;
        shorty = false;
        read();
    }

    /**
     * Save (S) choice.
     * <br />
     * Just as it sounds...
     */
    private void save() {
        if (FileHandler.save(plant)) echo(plant.getName() + " saved.");
        shorty = true;
        read();
    }

    /**
     * Exit (X) choice.
     * <br />
     * Just exits the execution loop, which leads to its termination.
     */
    private void exit() {
        echo("Want to save " + plant.getName() + " before exiting? [Y/n]");
        if (yes()) save();
        keep_on = false;
    }

    /**
     * Error choice -when no other choice fits-.
     * <br />
     * Just leads back to the menu, but in its short form.
     */
    private void ops() {
        shorty = true;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="cheats">
    /**
     * Cheat option, avaiable only with certain key commands.
     * <br />
     * Kills the plant instantly.
     */
    private void cheat_KILL() {
        plant.kill();
        echo("You, mercylessly, approached your once beloved " + plant.getName() + " and set it on fire," +
                " laughing at its excruciating pain and despair, in face of its iminent death!");
        shorty = false;
        read();
    }

    /**
     * Cheat option, avaiable only with certain key commands.
     * <br />
     * Adds <code>50000</code> fruits to the inventory.
     */
    private void cheat_MOTHERLODE() {
        inventory += 50000;
        echo("You started reciting some ancient magical words, whilst you wrote runic symbols all over " + plant.getName() +
                "\'s wooden skin.\nAs soon as you finished the incantation, the symbols were magically absorbed by your plant\'s" +
                " trunk, it began glowing a powerful mystical greenish light.\nThousands of fruits began to grow and become ripe " +
                "in " + plant.getName() + ", as they fell to earth and you harvested them!");
        shorty = false;
        read();
    }

    /**
     * Cheat option, avaiable only with certain key commands.
     * <br />
     * Heals the plant back to its Perfect health.
     */
    private void cheat_TOTAL_HEAL() {
        if (plant.isAlive()) while (plant.canHeal()) plant.heal();
        echo("You drew a star of David around " + plant.getName() + "\'s base, sat right outside of it and began praying to the lord above.\n" +
                "After some minutes of deep meditation a tower of light descended from the Heavens, over " + plant.getName() + ", and healed" +
                " it back to its perfect health!");
        shorty = false;
        read();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="constructor and main()">
    /**
     * Constructor method.
     */
    public Panel() {
        setDate();
        echo("Welcome to Virtual Plant.");
        echo();

        while ( !plant_exists && keep_on ) {
            echo("You have no plant. Load one? [Y/n]");
            if ( yes() ) {
                loadPlant(); plant_exists = true;
            }
            else {
                echo("Then shall we create a new one? [Y/n]");
                if ( yes() ) {
                    echo("Right, then! 8D\nLet\'s create a new plant, oui?"); echo();
                    createPlant(); plant_exists = true;
                }
                else { echo("Shame. :x"); keep_on = false; }
            }
        }

        while (keep_on) {
            if (plant.isAlive()) {
                if (!shorty) menu();
                else short_menu();
            }
            else {
                plantDied();
            }
        }

        echo("Have a nice day~\nBai bai! ;D");
        read();
    }

    /**
     * The execution method.
     *
     * @param args  the parameters given in the command-line call: <code>java -jar [this file] [parameters]</code>
     */
    public static void main(String args[]) {
        new Panel();
    }
    // </editor-fold>
    
}
