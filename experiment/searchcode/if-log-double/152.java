package newExamples;

import gaiasimu.gaia.Gaia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


/**
 * Class hosting a simulation to fit simulated exoplanet inclinations.
 * 
 * @author ebopp
 */
public class Simulation implements Logging {

	/** Array list of planetary systems to be simulated */
	private ArrayList<SimulatedSystem> systems;
	
	/** Time period of Gaia subcatalogue to be simulated */
	private double subCatalogue;

	/** Instance of Gaia */ 
	private GaiaWrapper gaiaWrapper;
	
	/** Error model to be used */
	private boolean faintCutOff;
	
	/** Base path for simulation output */
	private String basepath;
	
	/** Randomized orbital geometries flag; causes time of periapsis, node angle and argument of periapsis to be varied randomly */
	private boolean randGeo;
	
	/**
	 * Constructor
	 */
	public Simulation(int ngaias) {
				
		systems = new ArrayList<SimulatedSystem>();
		subCatalogue = 20.0;	// use whole Gaia data; early science would be 2.5
		randGeo = false;
		faintCutOff = false;
		
		// Initialize GaiaWrapper
		gaiaWrapper = new GaiaWrapper(ngaias);
		
	}

	
	/**
	 * Fits every system in the simulation
	 * @param NSIMS	Number of simulations
	 * @param NINCS Number of inclinations
	 */
	public void fitSystems(int NSIMS, double[] incs) {
		
		for ( SimulatedSystem sys: systems ) {
			sys.fitInclinations(NSIMS, incs);
		}
		
	}
	
	
	/**
	 * Loads systems from file with a certain format.
	 * 
	 * @param filename	Path to file from which systems should be loaded
	 */
	public void loadSystemsFromFile(String filename) {
		
		File file = new File(filename);
		
		try {
			
			BufferedReader in = new BufferedReader(new FileReader(file));
			String str = "";
			SystemTemplate lastSys = null;
			ArrayList<SystemTemplate> templates = new ArrayList<SystemTemplate>();
			
			// Read first line
			str = in.readLine();

			while (str != null) {
				// Parsing of data
				String[] values = str.split(",", -1);
				
				for (int i=0; i<values.length; i++) {
					if (values[i].isEmpty())
						values[i] = "0";
				}
				
				if(values[0].equals("S")) {
					
					// Read data from line
					String name = values[1].trim();
					double mass = Double.valueOf(values[2]);
					String spType = "F9V";//values[3]; TODO: parse correctly somehow
					double feH = Double.valueOf(values[4]);
					double alpha = Double.valueOf(values[5]) * 15.0; // hrs > deg
					double delta = Double.valueOf(values[6]);
					double muAlpha = Double.valueOf(values[7]);
					double muDelta = Double.valueOf(values[8]);
					double parallax = Double.valueOf(values[9]);
					double magV = Double.valueOf(values[10]);
					double magBV = Double.valueOf(values[11]);
					double radVel = Double.valueOf(values[12]);
					
					// Calculate additional data
					// TODO: check if ok
					double magB = magBV + magV;
					double magG = Conversion.gaiaMag(magB, magV);
					double magVI = Conversion.colorBVtoVI(magBV);
					double magBol = magV + Conversion.colorBVtoBC(magBV);
					double absV = 0.0;
					double alphaE = 0.0;
					int pop = 6;
					
					// Info output
					synchronized(logger) {
						
						logger.log("-- Adding system --");
						logger.log("\tName .................... "+name);
						logger.log("\tStellar mass [M_Sun] .... "+mass);
						logger.log("\tRight ascension [deg] ... "+alpha);
						logger.log("\tDeclination [deg] ....... "+delta);
						logger.log("\tRA p.m. [mas/yr] ........ "+muAlpha);
						logger.log("\tDec p.m. [mas/yr] ....... "+muDelta);
						logger.log("\tParallax [mas] .......... "+parallax);
						logger.log("\tV band magnitude ........ "+magV);
						logger.log("\tB-V color index ......... "+magBV);
						logger.log("\tRadial velocity [km/s] .. "+radVel);
						
					}
					
					// Build templates
					StarTemplate newStar = new StarTemplate(magG, magVI, absV, feH, alphaE, mass, spType, magBol, pop, name);
					AstrometryTemplate newAstro = new AstrometryTemplate(alpha, delta, parallax, muAlpha, muDelta, radVel);
					SystemTemplate newSys = new SystemTemplate();
					newSys.provide(newStar);
					newSys.provide(newAstro);
					lastSys = newSys;
					templates.add(newSys);
					
				} else if(values[0].equals("P")) {
					
					// Read data from line
					String name = values[1].trim();
					double msini = Double.valueOf(values[2]);
					double period = Double.valueOf(values[4]);
					double ecc = Double.valueOf(values[6]);
					double timeperi = Double.valueOf(values[8]);
					double argperi = Double.valueOf(values[10])/180.0*Math.PI; // deg > rad
					
					// Set default data for other values
					double nodeangle = 0.0;
					double inclination = Math.PI/2;
					
					// Info output
					synchronized(logger) {
						
						logger.log("-- Adding planet --");
						logger.log("\tName .................... "+name);
						logger.log("\tProjected mass [M_Jup] .. "+msini);
						logger.log("\tPeriod [d] .............. "+period);
						logger.log("\tEccentricity ............ "+ecc);
						logger.log("\tTime of peri. [JD] ...... "+timeperi);
						logger.log("\tArg. of peri. [rad] ..... "+argperi);
						
					}
					
					// Build planet template and provide to last added system template
					PlanetTemplate newPlanet = new PlanetTemplate(msini, period, ecc, inclination, nodeangle, argperi, timeperi, name);
					lastSys.provide(newPlanet);
					
				}
				
				// Read another line
				str = in.readLine();
				
			}

			for(SystemTemplate sys: templates) {
				this.addSystem(sys);
			}

		} catch (FileNotFoundException e) {
			// Print error msg
			System.err.println("File not found: " + file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Add a system based on a model template
	 * 
	 * @param sysTemp
	 * @return
	 */
	public SimulatedSystem addSystem(SystemTemplate sysTemp) {
		
		SimulatedSystem sys = this.addSystem(new SimulatedSystem(sysTemp.getStarTemplate().getName(), this));
		sys.setModelTemplate(sysTemp);
		sys.generateData(true);
		
		return sys;
		
	}
	
	
	/**
	 * Adds a system to the list of systems to be simulated.
	 * 
	 * @param system System to be added
	 * @return System for further processing
	 */
	public SimulatedSystem addSystem(SimulatedSystem system) {
		
		system.setOutputDir ( basepath + system.getName() );
		systems.add(system);
		return system;
		
	}

	
	/**
	 * Adds a system to the list of systems to be simulated
	 * 
	 * @param system System to be added
	 * @return System for further processing
	 */
	public SimulatedSystem addSystem(String name) {
		return addSystem(new SimulatedSystem(name, this));
	}
	
	
	/**
	 * Setter for systems array
	 * 
	 * @param systems New array of systems
	 */
	public void setSystems(ArrayList<SimulatedSystem> systems) {
		this.systems = systems;
	}


	/**
	 * Getter for systems array
	 * 
	 * @return Array of systems
	 */
	public ArrayList<SimulatedSystem> getSystems() {
		return systems;
	}

	
	/**
	 * Getter for Gaia instance
	 * 
	 * @return Gaia instance
	 */
	public Gaia getGaia() {
		return gaiaWrapper.getInstance();
	}
	
	
	/**
	 * Setter for base path for simulation output.
	 * 
	 * @param basepath	The new path to be set.
	 */
	public void setBasepath(String basepath) {
		
		// Check for trailing slash
		if ( ! basepath.endsWith("/") )
			basepath += "/";
		
		if ((new File(basepath)).exists())
			logger.log("Basepath "+basepath+" already exists");
		else
			if ((new File(basepath)).mkdirs())
				logger.log("Created basepath "+basepath);
			else
				logger.log("Failed to create basepath "+basepath);
		
		this.basepath = basepath;
	}
	
	
	/**
	 * Getter for base path for simulation output.
	 * 
	 * @return	The base path.
	 */
	public String getBasepath() {
		return basepath;
	}

	
	/**
	 * Setter for sub-catalogue, i.e. the mission time in years to be included in the simulation.
	 * 
	 * @param subCatalogue The time interval in years used as sub-catalogue
	 */
	public void setSubCatalogue(double subCatalogue) {
		this.subCatalogue = subCatalogue;
	}


	/**
	 * Getter for sub-catalogue, i.e. the mission time in years to be included in the simulation.
	 * 
	 * @return The time interval in years used as sub-catalogue
	 */
	public double getSubCatalogue() {
		return subCatalogue;
	}


	public void setRandGeo(boolean randGeo) {
		this.randGeo = randGeo;
	}


	public boolean isRandGeo() {
		return randGeo;
	}


	public void setFaintCutOff(boolean faintCutOff) {
		this.faintCutOff = faintCutOff;
	}


	public boolean isFaintCutOff() {
		return faintCutOff;
	}
	
}

