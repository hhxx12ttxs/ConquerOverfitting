import java.util.Set;

public class SeedFabric {
private static SeedFabric instance;
private long	      lastSeed;
private Random	    random;
private Set<Long>	 seedSet;

public static SeedFabric getInstance() {

