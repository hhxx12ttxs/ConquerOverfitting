
package ProjectVaria;

import java.util.Random;
import java.util.Scanner;

/**
 * <p>
 * Controls the world. Only one will be created at a time.
 * Also holds the player and the town arrays.
 * </p>
 * 
 * @author Morgan Wynne
 * @version 0.0
 */
public class World {
        
    //Other classes used for their methods. Nothing special.
    public static Random rand = new Random();
    public static Scanner scan = new Scanner(System.in);
    
    /**
     * <p>
     * The maximum amount of countries allowed in this world.
     * </p>
     */
    static final byte MAXIMUM_COUNTRIES = 4;
    
    /**
     * <p>
     * Records the number of residences in this world.
     * </p>
     */
    private int numberOfResidences;
    
    /**
     * <p>
     * The players character. Able to be accessed through a getter command.
     * </p>
     */
    private Person player;

    /**
     * <p>
     * Holds all the cities in this world.
     * [Number of the country][Number of the countries town]
     * </p>
     */
    private Town[][] countryArrays;
    
    /**
     * <p>
     * Lists all of the countries names. Cooperate with the first dimension of
     * the countryArrays array.
     * </p>
     */
    private Country[] countries;
    
    private Person[] residences;
    
    /**
     * <p>
     * Constructor. Creates the world, the country arrays, and the towns
     * that reside in the specific countries. Towns are specifically
     * generated within their own constructors.
     * </p>
     */
    public World() {
        
        //[Number of the country][Number of the countries town]
        countryArrays = new Town[rand.nextInt(MAXIMUM_COUNTRIES - 2) + 2][];
        //[A name assigned to the same number of the country]
        countries = new Country[countryArrays.length];
        
        residences = new Person[32767];
        
        Town targetTown;
        int targetCountry;
        int loopsRun;
        
        //Generates the amount of countries in the countryArrays array.
        for(int i = 0; i < countryArrays.length; i++) {
            countryArrays[i] = new Town[rand.nextInt(2) + 2];
        }
        
        for(int i = 0; i < (countries.length * rand.nextInt((6) + 5)); i++) {
            
        }
        
        //Generates random country names for each countryArray coordinate.
        for(int i = 0; i < countries.length; i++) {
            countries[i] = new Country(i);
            do { //Checks for duplicates
                countries[i].setName(Manipulator.generateCountryName());
            } while(!Manipulator.checkDuplicateNames(countries, i));
        }
        
        //Assigns a town and a country name to all countryrray.
        boolean noRepeats;
        for(int i = 0; i < countryArrays.length; i++) {
            for(int u = 0; u < (countryArrays[i].length); u++) {
                
                countryArrays[i][u] = new Town(u);
                countryArrays[i][u].setCountryName(countries[i].getName());
                countryArrays[i][u].setName(Manipulator.generateTownName());
                do {
                    noRepeats = true;
                    for(int x = 0; x < i; x++) {
                        for(int y = 0; y < countryArrays[x].length; y++) {
                            if(countryArrays[i][u].getName()
                                    .equals(countryArrays[x][y].getName())) {
                                noRepeats = false;
                            }
                        }
                    }
                    for(int y = 0; y < u; y++) {
                            if(countryArrays[i][u].getName()
                                    .equals(countryArrays[i][y].getName())) {
                                noRepeats = false;
                            }
                    }
                    if(!noRepeats) {
                        countryArrays[i][u]
                                .setName(Manipulator.generateTownName());
                    }
                } while(!noRepeats);
            }
        }   

        for(int i = 0; i < countryArrays.length; i++) {
            for(int u = 0; u < (countryArrays[i].length); u++) {
                //Assigns random towns to the roads[] variable in each town.
                loopsRun = 0;
                if (countryArrays[i][u].getNumberOfRoads() 
                        < countryArrays[i][u].getRoads().length) {
                    do {
                        noRepeats = true;
                        loopsRun++;
                        targetCountry = rand.nextInt(countryArrays.length);
                        targetTown = countryArrays[targetCountry]
                                [rand.nextInt(countryArrays[targetCountry]
                                .length)];
                        for(int x = 0; x < countryArrays[i][u]
                                .getNumberOfRoads(); x++){
                            if (countryArrays[i][u].getSpecificRoad(x) 
                                    == targetTown) {
                                noRepeats = (countryArrays[i][u]
                                        .getSpecificRoad(x) != targetTown);
                            }
                        }
                        if(noRepeats && targetTown != countryArrays[i][u]
                                && targetTown.getNumberOfRoads() 
                                < targetTown.getRoads().length) {
                            countryArrays[i][u].setRoads(targetTown);
                            countryArrays[i][u].incrementNumberOfRoads();
                            targetTown.setRoads(countryArrays[i][u]);
                            targetTown.incrementNumberOfRoads();
                        }
                        if(loopsRun > 16) {
                            loopsRun = 0;
                            countryArrays[i][u].shrinkRoadsArray();
                        }
                    } while(countryArrays[i][u].getNumberOfRoads() 
                            < countryArrays[i][u].getRoads().length);
                }
                
            }
        }
        
        createPlayer();
    }
    
    /**
     * <p>
     * Used to generate what each NPC and town does each turn. Events should
     * be few for the amount that can happen, but meaningful. We'll get to that
     * in the future.
     * </p>
     */
    public void runWorld() {
        
        //System.out.println("In the World.runWorld function");
    }
    
    /**
     * <p>
     * Getter for the players alive status.
     * </p>
     * @return if the player is alive. 
     */
    public boolean isPlayerAlive() {
        
        return player.getAlive();
    }
    
    /**
     * <p>
     * Creates a new player character for the player to control..
     * </p>
     */
    public final void createPlayer() {
        
        player = new Person();
        
        System.out.println("Enter in a name for your character:");
        player.setName(scan.nextLine());
        System.out.println("Enter in your profession: ");
        player.setProfession(scan.nextLine().toLowerCase());
        
        //Displays a list of the countries in this world and prompts 
        //the player to choose one. Will re-run if falsely input.
        boolean correctCountry = false;
        
        do {
            System.out.println("Enter in the country you reside in: ");
            //Prints out all the countries names.
            for(int i = 0; i < countryArrays.length - 1; i++) {
                System.out.print(countries[i].getName() 
                        + (countryArrays.length > 2 ? ", " : " "));
            }
            System.out.println("or " 
                    + countries[countryArrays.length - 1].getName());
            //Gets the input from the player.
            String newName = scan.nextLine();
            for(int i = 0; i < countryArrays.length; i++) {
                if(newName.toUpperCase()
                        .equals(countries[i].getName().toUpperCase())) {
                    player.setCountry(countries[i].getNumber());
                    correctCountry = true;
                }
            }
            if (!correctCountry) {
                System.out.println("The selected country does not exist.");
            }
        } while (!correctCountry);
        
        player.setTown(countryArrays[player.getCountry()]
                [rand.nextInt(countryArrays[player.getCountry()].length)]);
        player.setBuilding(player.getTown().getSpecificBuilding(0));
    }
    
    /**
     * <p>
     * Getter for the player.
     * </p>
     * @return the player him/herself.
     */
    public Person getPlayer() {
        
        return player;
    }
    
    public Person[] getResidences() {
        
        return residences;
    }
    
    public void incrementResidences() {
        
        numberOfResidences++;
    }
    
}

