package finalproject;

/**
 * Name: Eric Kilmer Section: 002 Program: finalproject Date: Feb 6, 2013
 * Description: Abstract class to contain all necessary data members and methods
 * that are shared across all of the types of media content that extend this
 * class. There are methods available to aid in the interaction between these
 * objects and a buyer and/or administrator.
 *
 */
import java.sql.*;
import java.util.Comparator;
import java.util.Objects;

/**
 * LibraryMedia is an abstract class that contains all necessary information
 * that will be shared across each type of media content that is present in the
 * library.
 *
 * @author Eric
 * @version 1.0, Feb 6, 2013
 */
public abstract class LibraryMedia implements Comparable<LibraryMedia> {

    /** Total number of library media sold. */
    private static int totalSold = 0;
    /** The sum of all prices that were bought. */
    private static double totalSales = 0;

    /** Main artist, director, author, etc. */
    protected String creator;
    /** Main title of the media. */
    protected String title;
    /** Genre of the media file. */
    protected String genre;
    /** Length/duration of the media in seconds. */
    protected double length;
    /** The price of the media in dollars. */
    protected double price;
    /** Average ranking is determined by comparing the amount sold. */
    protected int averageRanking = 0;
    /** The number of times people have bought this particular media object. */
    protected int numberSold = 0;
    /** The average star rank for this media object. */
    protected double starRank = 0;
    /** The number of times this media has been rated. */
    protected int numberRated;
    /** True if user has starred the media object, false otherwise. */
    protected boolean rated = false;
    /** Connection for the SQL Database */
    private Connection mediaStoreDatabase;
    /** Statement field */
    private Statement stmt;
    /** String for query */
    private String query;
    /** Result set from query */
    private ResultSet resultQuery;

    /** Title sort comparator */
    public static final Comparator<LibraryMedia> TITLE_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the title of two media objects ignoring case in 
         * lexicographic order.
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * greater than, equal to, or less than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return o1.getTitle().compareToIgnoreCase(o2.getTitle());
        }
    };
    /** Reverse Title sort comparator */
    public static final Comparator<LibraryMedia> REV_TITLE_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the title of two media objects ignoring case in reverse 
         * lexicographic order.
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * less than, equal to, or greater than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return o2.getTitle().compareToIgnoreCase(o1.getTitle());
        }
    };
    /** Creator sort comparator */
    public static final Comparator<LibraryMedia> CREATOR_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the creator of two media objects ignoring case in 
         * lexicographic order.
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * greater than, equal to, or less than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return o1.getCreator().compareToIgnoreCase(o2.getCreator());
        }
    };
    /** Reverse Creator sort comparator */
    public static final Comparator<LibraryMedia> REV_CREATOR_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the creator of two media objects ignoring case in reverse 
         * lexicographic order.
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * less than, equal to, or greater than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return o2.getCreator().compareToIgnoreCase(o1.getCreator());
        }
    };
    /** Genre sort comparator */
    public static final Comparator<LibraryMedia> GENRE_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the genre of two media objects ignoring case in 
         * lexicographic order.
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * greater than, equal to, or less than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return o1.getGenre().compareToIgnoreCase(o2.getGenre());
        }
    };
    /** Reverse Genre sort comparator */
    public static final Comparator<LibraryMedia> REV_GENRE_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the genre of two media objects ignoring case in reverse 
         * lexicographic order.
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * less than, equal to, or greater than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return o2.getGenre().compareToIgnoreCase(o1.getGenre());
        }
    };
    /** Length sort comparator */
    public static final Comparator<LibraryMedia> LENGTH_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the length of two media
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * greater than, equal to, or less than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return Double.compare(o2.getLength(), o1.getLength());
        }
    };
    /** Reverse Length sort comparator */
    public static final Comparator<LibraryMedia> REV_LENGTH_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the length of two media objects 
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * less than, equal to, or greater than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return Double.compare(o1.getLength(), o2.getLength());
        }
    };
    /** Price sort comparator */
    public static final Comparator<LibraryMedia> PRICE_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the price of two media
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * greater than, equal to, or less than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return Double.compare(o2.getPrice(), o1.getPrice());
        }
    };
    /** Reverse Price sort comparator */
    public static final Comparator<LibraryMedia> REV_PRICE_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the price of two media objects 
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * less than, equal to, or greater than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return Double.compare(o1.getPrice(), o2.getPrice());
        }
    };
    /** Rank sort comparator */
    public static final Comparator<LibraryMedia> RANK_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the rank of two media
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * greater than, equal to, or less than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return Double.compare(o1.getAverageRanking(), o2.getAverageRanking());
        }
    };
    /** Reverse Rank sort comparator */
    public static final Comparator<LibraryMedia> REV_RANK_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the rank of two media objects 
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * less than, equal to, or greater than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return Double.compare(o2.getAverageRanking(), o1.getAverageRanking());
        }
    };
    /** Number Sold sort comparator */
    public static final Comparator<LibraryMedia> NUM_SOLD_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the number sold of two media
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * greater than, equal to, or less than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return o2.getNumberSold() - o1.getNumberSold();
        }
    };
    /** Reverse Number Sold sort comparator */
    public static final Comparator<LibraryMedia> REV_NUM_SOLD_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the number sold of two media objects 
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * less than, equal to, or greater than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return o1.getNumberSold() - o2.getNumberSold();
        }
    };
    /** Stars sort comparator */
    public static final Comparator<LibraryMedia> STARS_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the star rank of two media
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * greater than, equal to, or less than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return Double.compare(o2.getStarRank(), o1.getAverageRanking());
        }
    };
    /** Reverse Stars sort comparator */
    public static final Comparator<LibraryMedia> REV_STARS_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the star rank of two media objects 
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * less than, equal to, or greater than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return Double.compare(o1.getStarRank(), o2.getAverageRanking());
        }
    };
    /** Number Rated sort comparator */
    public static final Comparator<LibraryMedia> NUM_RATED_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the rank of two media
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * greater than, equal to, or less than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return o2.getNumberRated() - o1.getNumberRated();
        }
    };
    /** Reverse Number Rated sort comparator */
    public static final Comparator<LibraryMedia> REV_NUM_RATED_SORT = 
                                     new Comparator<LibraryMedia>() {
        /**
         * Compares the rank of two media objects 
         * 
         * @param o1 The main object
         * @param o2 The compared object
         * @return A negative integer, zero, or a positive integer according to
         * less than, equal to, or greater than.
         */
        @Override
        public int compare(LibraryMedia o1, LibraryMedia o2) {
            return o1.getNumberRated() - o2.getNumberRated();
        }
    };

    /**
     * Default constructor to create a null media file.
     */
    public LibraryMedia() {
        this(null, null, null, 0.0, 0.0);
    }

    /**
     * Initializing constructor.
     *
     * @param creator Author, artist, director of the media.
     * @param title Title of the media work.
     * @param genre Genre of the media.
     * @param length Length of the media in seconds.
     * @param price Price of the media in dollars. (Should not be more than two
     * decimals long)
     */
    public LibraryMedia(String creator, String title, String genre,
            double length, double price) {
        this.creator = creator;
        this.title = title;
        this.genre = genre;
        this.length = length;
        this.price = price;
        
        //connect to database
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance(); 
            mediaStoreDatabase = DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/media_store", "root", "root");    
        }
        catch(SQLException | InstantiationException | ClassNotFoundException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Each subclass must implement their own toString method which lists all
     * member variables and their values.
     *
     * @return
     */
    @Override
    public abstract String toString();

    /**
     * Required method to get the name of the class that the object is.
     * 
     * @return A string of the object's class name.
     */
    public abstract String getType();

    /**
     * Purchase the item.
     *
     * @return The price of the item.
     */
    public double buy() {
        // Increase appropriate counters
        totalSold++;
        numberSold++;
        totalSales += price;
        
        query = String.format("update media set num_sold = %d "
                + "where media_id = %d", numberSold, getID());
        try {
            stmt = mediaStoreDatabase.createStatement();
            stmt.execute(query);
        } catch(SQLException e) {
            System.out.println(query);
            e.printStackTrace();
        }
        
        return price;
    }

    /**
     * Accessor method to get the creator's name. This method is a failsafe in
     * case any subclass does not implement a similar method.
     *
     * @return The name of the creator of the media.
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Accessor method for reading the title of the media.
     *
     * @return The title of the media.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Accessor method for reading the genre of the media.
     *
     * @return The genre of the media.
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Accessor method for the length or duration of the media.
     *
     * @return The length/duration of the media.
     */
    public double getLength() {
        return length;
    }

    /**
     * Accessor method for getting the price of the media.
     *
     * @return The price of the media
     */
    public double getPrice() {
        return price;
    }

    /**
     * Accessor method to obtain the number of copies sold.
     *
     * @return The number of copies sold.
     */
    public int getNumberSold() {
        //POST: Returns the number of items sold
        return numberSold;
    }

    /**
     * Accessor method for getting the average ranking of the media.
     *
     * @return The average ranking of the particular media.
     */
    public int getAverageRanking() {
        return averageRanking;
    }

    /**
     * Accessor method to obtain the total number of media sold.
     *
     * @return The total number of media sold.
     */
    public static int getTotalSold() {
        return totalSold;
    }

    /**
     * Accessor method to get the total sales money from all media.
     *
     * @return The total sales in dollars.
     */
    public static double getTotalSales() {
        return totalSales;
    }

    /**
     * Mutator method to set the creator
     * 
     * @param creator New creator.
     */
    public void setCreator(String creator) {
        query = String.format("update media set creator = '%s' "
                + "where creator = '%s'", creator, this.creator);
        try {
            stmt = mediaStoreDatabase.createStatement();
            stmt.execute(query);
            this.creator = creator;
        } catch(SQLException e) {
            System.out.println(query);
            e.printStackTrace();
        }
    }

    /**
     * Mutator method to change the title of the media.
     *
     * @param titleName New title name.
     */
    public void setTitle(String titleName) {
        query = String.format("update media set title = '%s' "
                + "where title = '%s'", titleName, title);
        try {
            stmt = mediaStoreDatabase.createStatement();
            stmt.execute(query);
            title = titleName;
        } catch(SQLException e) {
            System.out.println(query);
            e.printStackTrace();
        }
    }

    /**
     * Mutator method to change genre.
     *
     * @param genre New genre name.
     */
    public void setGenre(String genre) {
        query = String.format("update media set genre = '%s' "
                + "where genre = '%s'", genre, this.genre);
        try {
            stmt = mediaStoreDatabase.createStatement();
            stmt.execute(query);
            this.genre = genre;
        } catch(SQLException e) {
            System.out.println(query);
            e.printStackTrace();
        }
    }

    /**
     * Mutator method to change the length of the media.
     *
     * @param length New length of the media.
     */
    public void setLength(double length) {
        query = String.format("update media set length = %5.2f "
                + "where genre = %5.2f", length, this.length);
        try {
            stmt = mediaStoreDatabase.createStatement();
            stmt.execute(query);
            this.length = length;
        } catch(SQLException e) {
            System.out.println(query);
            e.printStackTrace();
        }
    }

	/**
	 * Sets the number sold.
	 * @param numSold The number of times you want this object to have been
	 * sold.
	 */
	public void setNumberSold(int numSold) {
		this.numberSold = numSold;
	}

    /**
     * Mutator method to change the price of the media.
     *
     * @param price New price of the media.
     */
    public void setPrice(double price) {
        query = String.format("update media set price = %5.2f "
                + "where price = %5.2f", price, this.price);
        try {
            stmt = mediaStoreDatabase.createStatement();
            stmt.execute(query);
            this.price = price;
        } catch(SQLException e) {
            System.out.println(query);
            e.printStackTrace();
        }
    }

    public void setAverageRanking(int rank) {
        query = String.format("update media set length = %d "
                + "where genre = %d", rank, averageRanking);
        try {
            stmt = mediaStoreDatabase.createStatement();
            stmt.execute(query);
            averageRanking = rank;
        } catch(SQLException e) {
            System.out.println(query);
            e.printStackTrace();
        }
    }

    /**
     * Overriding equals method to compare two media objects. Only the important
     * parts of the media are compared. <br/><br/> Undefined results may occur
     * if this method is not called with a check to verify that compare is of 
     * the same class as the calling object.
     *
     * @param compare The media object to which should be compared.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object compare) {
        if (compare == null) {
            return false;
        }
        if (compare == this) {
            return true;
        }
        if (!(compare instanceof LibraryMedia)) {
            return false;
        }

        LibraryMedia test = (LibraryMedia) compare;
        return this.creator.equalsIgnoreCase(test.getCreator())
                && this.genre.equalsIgnoreCase(test.getGenre())
                && this.title.equalsIgnoreCase(test.getTitle())
                && this.length == test.getLength();
    }

    /**
     * Required hashCode method implementation override.
     *
     * @return The hashCode of the object.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.creator.toLowerCase());
        hash = 13 * hash + Objects.hashCode(this.title.toLowerCase());
        hash = 13 * hash + Objects.hashCode(this.genre.toLowerCase());
		hash = 13 * hash + Objects.hashCode(getType().toLowerCase());
        hash = 13 * hash + (int) (Double.doubleToLongBits(this.length) ^ (Double.doubleToLongBits(this.length) >>> 32));
        return hash;
    }

	/**
	 * Returns a unique id that has been hashed to identify this object.
	 * @return a unique id number.
	 */
	public int getID() {
		return hashCode();
	}

    /**
     * Custom compareTo method to allow sorting based on number sold.
     *
     * @param other The object that this is being compared to.
     * @return The difference between this number sold and other's number sold.
     */
    @Override
    public int compareTo(LibraryMedia other) {
        return other.getNumberSold() - numberSold;
    }


    /**
     * Accessor to see if this media file has been rated.
     *
     * @return True if it has been rated already, false otherwise.
     */
    public boolean getRated() {
        return rated;
    }

    /**
     * Accessor method to get the number of people who have rated the object.
     * 
     * @return The number of people who rated this particular media.
     */
    public int getNumberRated() {
        return numberRated;
    }

    /**
     * Sets a average new star rank for this media object.
     *
     * @param stars The number of stars that should be rated. The stars should
     * be between 0 and 5.
     */
    public void setStarRank(double stars) {
        // Check if already rated.
        /*if (rated) {
         System.out.println("Already rated. Cannot rate again.");
         return;
         }*/

        // Check to make sure stars is between 0 and 5.
        if (stars < 0 || stars > 5) {
            System.out.println("Star rating is invalid. Must be between"
                    + " 0 and 5.");
            return;
        }

        numberRated++;    // Increase rate number
        query = String.format("update media set num_rated = %d "
                + "where media_id = %d", numberRated, getID());
        try {
            stmt = mediaStoreDatabase.createStatement();
            stmt.execute(query);
        } catch(SQLException e) {
            System.out.println(query);
            e.printStackTrace();
        }

        // Adjust average stars.
        starRank = (double) (starRank + stars) / numberRated;    
        query = String.format("update media set avg_stars = %5.2f "
                + "where media_id = %d", starRank, getID());
        try {
            stmt = mediaStoreDatabase.createStatement();
            stmt.execute(query);
        } catch(SQLException e) {
            System.out.println(query);
            e.printStackTrace();
        }

        rated = true;
    }

    /**
     * Accessor method for the average star rating.
     *
     * @return The average star rating as a number between 0 and 5.
     */
    public double getStarRank() {
        return starRank;
    }
}

