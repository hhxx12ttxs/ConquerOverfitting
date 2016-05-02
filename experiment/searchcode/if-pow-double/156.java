package newExamples;

import gaia.cu1.mdb.cu3.localplanecoords.dm.FovTransitInfo;
import gaia.cu1.mdb.cu3.localplanecoords.dm.LpcCentroid;
import gaia.cu1.params.GaiaParam;
import gaia.cu1.params.MissingParams;
import gaia.cu1.tools.dal.gbin.GbinReaderV2;
import gaia.cu1.tools.dm.GaiaRoot;
import gaia.cu1.tools.exception.GaiaDataAccessException;
import gaia.cu1.tools.exception.GaiaException;
import gaia.cu1.tools.numeric.algebra.GVector2d;
import gaiasimu.GaiaSimuTime;
import gaiasimu.SimuException;
import gaiasimu.GaiaSimuEnums.SimuTimeScale;
import gaiasimu.gaia.payload.psf.CachePSFManager;
import gaiasimu.universe.outputfile.WriteMDBICD;
import gaiasimu.universe.source.stellar.OrbitalParams;
import gaiasimu.universe.source.stellar.Star;
import gaiasimu.universe.source.stellar.StellarSource;
import gaiasimu.utils.cache.CacheManager;
import gog.GogGlobals;
import gog.GogRun;
import gog.configuration.GogConfigurationMgr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import newExamples.gaia.cu4.du438.current_cycle.DmsSimuDU437;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;

/**
 * The Class ModelFunctionGOG.
 * models epoch astrometry for star system using GOG
 */
public class ModelFunctionGOG implements DifferentiableMultivariateVectorialFunction {
	
	// GOG files
	/** The GOG configuration file. */
	private final String[] gogConfig = { "newDataInput/gogconfigExoplanetInclinationNoiseless.xml" };
	
	/** The umStellar temporal file directory. */
	private final String umStellarFileDir = "newGogTemp/";
	
	/** The umStellar temporal file. */	
	private final String umStellarFile    = umStellarFileDir + "UMStellar.gbin";
	
	/** The GOG temporal files output directory. */
	private final String gogOutputDir     = "newDataOutput/";
	
	/** The GOG temporal output files. */
	private final String[] gogOutputFiles   = {
			gogOutputDir + "gog7beta_combinedAstrometric.gbin",
			gogOutputDir + "gog7beta_combinedAstroPhysical.gbin",
			gogOutputDir + "gog7beta_combinedPhotometric.gbin",
			gogOutputDir + "gog7beta_combinedRVS.gbin",
			gogOutputDir + "gog7beta_epochAstrometric.gbin",
			gogOutputDir + "gog7beta_epochPhotometric.gbin",
			gogOutputDir + "gog7beta_epochRVS.gbin",
			gogOutputDir + "gog7beta_sourceRVS.gbin" };
	
	/** The GOG epoch astrometry file. */
	private final String GOG_EPOCH_ASTROMETRY_FILE = gogOutputDir + "gog7beta_epochAstrometric.gbin";
	
	
	// Stellar system data
	
	/** The primary star. */
	private Star star;
	
	/** The exoplanet mass (Jupiter masses). */
	private double massPlanet;
	
	/** The orbital period (days). */
	private double period;
	
	/** The periastron time (days). */
	private double timePeriastron;
	
	/** The orbit eccentricity. */
	private double eccentricity;
	
	/** The true inclination */
	private double inclination;
	
	/** The argument of periastron (rad). */
	private double omega2;
	
	/** The position angle of the line of nodes (rad). */
	private double nodeAngle;	
	
	/** The number of transits. */
	private int nTransits;
	
	/** The angle increment for derivatives calculation (rad). */
	private final double angleIncrement = 1e-5;
	
	/** The years included in the subcatalogue. */
	private double subCatalogue;
	
	/** The component to use in the astrometry file. */
	private final int component = 0;
	
	/** The use AC data boolean switch. */
	private boolean useAc;
	
	/**
	 * Instantiates a new exoplanet inclination differentiable function.
	 *
	 * @param star the primary star
	 * @param massPlanet the exoplanet mass (Jupiter masses)
	 * @param period the orbital period (days)
	 * @param timePeriastron the periastron time (days)
	 * @param eccentricity the orbit eccentricity
	 * @param omega2 the argument of periastron (rad)
	 * @param nodeAngle the node angle
	 * @param subCatalogue the years included in the sub catalogue
	 * @param useAc the use AC data boolean switch
	 * @throws SimuException the simu exception
	 * @throws GaiaException the gaia exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */	
	public ModelFunctionGOG(
			Star    star,
			double  massPlanet,
			double  period,
			double  timePeriastron,
			double  eccentricity,
			double	inclination,
			double  omega2,
			double  nodeAngle,
			double  subCatalogue,
			boolean useAc)
	throws SimuException, GaiaException, IOException {
		
		// Parse stellar system parameters
		this.star           = star;
		this.massPlanet     = massPlanet;
		this.period         = period;
		this.timePeriastron = timePeriastron;
		this.eccentricity   = eccentricity;
		this.inclination	= inclination;
		this.omega2         = omega2;
		this.nodeAngle      = nodeAngle;
		this.subCatalogue   = subCatalogue;
		this.useAc          = useAc;
		
		// Get number of transits in subcatalogue
		this.nTransits = getNTransits();
		
	}
	
	
	/* (non-Javadoc)
	 * @see org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction#jacobian()
	 */
	@Override
	public MultivariateMatrixFunction jacobian() {		
        return new MultivariateMatrixFunction() {
            public double[][] value(double[] inclination) {
        		
        		try {
        			// Create new stellar source with input inclination angle
        			StellarSource source0 = getStellarSource(
        					star, massPlanet, period, timePeriastron, eccentricity, omega2, nodeAngle, inclination[0] );
        			// Run GOG for this source
        			runGog( source0 );        			
        			// Read epoch astrometry vector
        			double[] vector0 = readEpochAstrometryArray();
        			
        			// Create new stellar source with input inclination angle + epsilon
        			StellarSource source1 = getStellarSource(
        					star, massPlanet, period, timePeriastron, eccentricity, omega2, nodeAngle, inclination[0] + angleIncrement );
        			// Run GOG for this source
        			runGog( source1 );
        			// Read epoch astrometry vector
        			double[] vector1 = readEpochAstrometryArray();
        			
            		// Jacobian definition
               		double[][] jacobianMatrix = new double [vector0.length][1];
               		for (int i = 0; i < vector0.length; i++) {
               			jacobianMatrix[i][0] = ( vector1[i] - vector0[i] ) / angleIncrement;
               		}

        			return jacobianMatrix;
        			
        		} // Exceptions cannot be handled, null returned
        		  catch (SimuException e) {
        			e.printStackTrace();
        			return null;
        		} catch (GaiaException e) {
        			e.printStackTrace();
        			return null;        			
        		} catch (IOException e) {
					e.printStackTrace();
				}
        		
        		System.out.printf("XXXXXXXXX Some exception happened. XXXXXXXXXX\n");
        		return null;
       		
            }
            
        };
        
	}
	
	
	/* (non-Javadoc)
	 * @see org.apache.commons.math.analysis.MultivariateVectorialFunction#value(double[])
	 */
	@Override
	public double[] value(double[] inclination) throws FunctionEvaluationException,
			IllegalArgumentException {
		
		System.out.printf(" --- Evaluation for inc = %e \n", inclination[0]);
		
		try {
			// Create new stellar source with input inclination angle
			StellarSource source = getStellarSource(star, massPlanet, period, timePeriastron, eccentricity, omega2, nodeAngle, inclination[0]);
			// Run GOG for this source
			runGog( source );
			// Read epoch astrometry vector
			return readEpochAstrometryArray();
			
		} // Exceptions cannot be handled, null returned
		  catch (SimuException e) {
			e.printStackTrace();
		} catch (GaiaException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.err.println("XXXXXXXXX Some exception happened. XXXXXXXXXX");
		return null;
		
	}
	
	
	/**
	 * Gets a stellar source including a primary star and a single exoplanet.
	 *
	 * @param star the primary star
	 * @param massSinIncl the exoplanet projected mass, i.e. m sin i (Jupiter masses)
	 * @param period the orbital period (days)
	 * @param timePeriastron the periastron time (days)
	 * @param eccentricity the orbit eccentricity
	 * @param omega2 the argument of periastron (rad)
	 * @param nodeAngle the node angle
	 * @param inclination the inclination angle (rad)
	 * @return the stellar source
	 * @throws SimuException the simu exception
	 */
	public static StellarSource getStellarSource(
			Star   star,
			double massSinIncl,
			double period,
			double timePeriastron,
			double eccentricity,
			double omega2,
			double nodeAngle,
			double inclination) throws SimuException{
		
		// Create new stellar source with different inclination angles
		// Create new factory
		DmsSimuDU437 dmsSimuDU437 = new DmsSimuDU437();
		
		// Generate new orbital parameters
		OrbitalParams orbitalParams = dmsSimuDU437.generateOrbitalParams(
				star, massSinIncl / Math.sin(inclination), period, timePeriastron, eccentricity, omega2, nodeAngle, inclination);
		
		// Generate new stellar source
		StellarSource source = dmsSimuDU437.generateSystem( star, orbitalParams );
		
		return source;
		
	}
	
	
	/**
	 * Runs GOG to obtain astrometry.
	 *
	 * @param source the stellar source
	 * @return the int
	 * @throws GaiaException the gaia exception
	 * @throws SimuException the simu exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private int runGog( StellarSource source ) throws GaiaException, SimuException, IOException {
		
		// Write sources to UMStellar file
		ArrayList<gaiasimu.universe.source.AstroSource> objList =
			new ArrayList<gaiasimu.universe.source.AstroSource>();
		objList.add( source );
		
		// Erase old UMStellar file, if available, and write the new one
		( new File ( umStellarFile ) ).delete();
		WriteMDBICD writer = new WriteMDBICD( umStellarFileDir );
		writer.launchWrite( objList, false );
		
		// Erase old GOG output files, if available
		( new File ( GOG_EPOCH_ASTROMETRY_FILE) ).delete();
		for (String gogOutputFile : gogOutputFiles){
			( new File ( gogOutputFile ) ).delete();			
		};
		
		// Run GOG
		// GogRun.main( gogConfig );
		GogConfigurationMgr.getConfig( gogConfig[0], Long.toString(System.currentTimeMillis()));
        CachePSFManager.setPsfDir(GogGlobals.getPsfPath());
        CacheManager.getInstance("gaiasimu-cache.xml");
        GogRun gr = new GogRun();
        gr.start();
        
        return 0;
		
	}
	
	
	/**
	 * Reads the epoch astrometry array from GOG output.
	 *
	 * @return the epoch astrometry array
	 * @throws GaiaDataAccessException the gaia data access exception
	 */
	private double[] readEpochAstrometryArray() throws GaiaDataAccessException {
		
		// Read first element from epoch astrometry GOG file
		ArrayList<GaiaRoot> epochAstrometryArrayList = new ArrayList<GaiaRoot>();		
		GbinReaderV2 epochAstrometryReader = new GbinReaderV2 ( GOG_EPOCH_ASTROMETRY_FILE );		
		epochAstrometryReader.readAllToList( epochAstrometryArrayList );
		epochAstrometryReader.close();
		LpcCentroid epochAstrometry = (LpcCentroid) epochAstrometryArrayList.get( component );
		
		// Create epoch astrometry vector in pairs (AL, AC)		
		FovTransitInfo[] transitInfo = epochAstrometry.getTransits();
		double[] vector;
		if (useAc){
			
			//double[] vector = new double[ 2 * transitInfo.length ];
			//for (int i = 0; i < transitInfo.length; i++){
			vector = new double[ 2 * nTransits ];
			for (int i = 0; i < nTransits; i++){
				vector[ 2 * i     ] = transitInfo[i].getCentroidPosAl();
				vector[ 2 * i + 1 ] = transitInfo[i].getCentroidPosAc();
			}
			
		} else {
			
			vector = new double[ nTransits ];
			for (int i = 0; i < nTransits; i++){
				vector[i] = transitInfo[i].getCentroidPosAl();
			}
						
		}
		
		return vector;
				
	}
	
	
	/**
	 * Gets the epoch astrometry array from GOG output.
	 * Gaussian noise can be added for each measurement.
	 *
	 * @param addNoise the add noise boolean switch
	 * @param noise the noise array (mas)
	 * @return the epoch astrometry array
	 * @throws SimuException the simu exception
	 * @throws GaiaException the gaia exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double[] getEpochAstrometry( boolean addNoise, double[] noise )
	throws SimuException, GaiaException, IOException {
		
		FovTransitInfo[] transitInfo = getTransitInfo ( massPlanet, inclination );
		FovTransitInfo[] transitInfoZero = getTransitInfo ( 1e-8, inclination );
		
		double[] vector;
		if (useAc){
			
			vector = new double[ 2 * nTransits ];
			for ( int i = 0; i < nTransits; i++ ) {
				vector[ 2 * i     ] = transitInfo[i].getCentroidPosAl();
				vector[ 2 * i + 1 ] = transitInfo[i].getCentroidPosAc();
				
				// Some debugging output checking conversion functions
				double scanAngle = transitInfo[i].getScanPosAngle()*GaiaParam.Nature.DEGREE_RADIAN; // NOTE: Documentation wrong, scan angle is returned in deg
				GVector2d lsc = new GVector2d(transitInfo[i].getCentroidPosAl(), transitInfo[i].getCentroidPosAc());
				GVector2d lpc = Conversion.fromLSCtoLPC ( lsc, scanAngle ); 
				//StellarAstrometry ast = ((StellarAstrometry) star.getAstrometry());
				//GVector3d pos = Conversion.fromLSCtoCoMRS ( lsc, ast.getAlpha(), ast.getDelta(), scanAngle );
				GaiaSimuTime gst = new GaiaSimuTime(transitInfo[i].getObsTime(), SimuTimeScale.UM_TIME);
				
				//GVector3d comrsRef = ast.getCoMRSPosition(gst, false);
				//GVector2d lpcRef = Conversion.fromCoMRStoLPC(comrsRef, ast.getAlpha(), ast.getDelta());
				
				GVector2d lscZero = new GVector2d(transitInfoZero[i].getCentroidPosAl(), transitInfoZero[i].getCentroidPosAc());
				GVector2d lpcZero = Conversion.fromLSCtoLPC(lscZero, transitInfoZero[i].getScanPosAngle()*GaiaParam.Nature.DEGREE_RADIAN);
				
				System.out.printf("%20.14e %20.14e %20.14e %20.14e %20.14e %20.14e %20.14e %20.14e %20.14e %20.14e\n",
						gst.getJD(),
						lpc.getX(), lpc.getY(),
						lpcZero.getX(), lpcZero.getY(),
						lsc.getX(), lsc.getY(),
						lscZero.getX(), lscZero.getY(),
						scanAngle);
				//*/
			}
			
		} else {
			
			vector = new double[ nTransits ];			
			for (int i = 0; i < nTransits; i++){
				vector[i] = transitInfo[i].getCentroidPosAl();
			}
			
		}
		
		// Add gaussian noise
		if ( addNoise ){
			Random random = new Random();
			for (int i = 0; i < vector.length; i++){
				vector[i] += random.nextGaussian() * noise[i];
			}
		}
		
		return vector;
				
	}
	
	
	/**
	 * Gets the epoch astrometry errors array from GOG output.
	 * DEPRECATED
	 *
	 * @return the epoch astrometry array
	 * @throws SimuException the simu exception
	 * @throws GaiaException the gaia exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double[] getEpochAstrometryErrorsGOG() throws SimuException, GaiaException, IOException{
		
		FovTransitInfo[] transitInfo = getTransitInfo(massPlanet, inclination);
		
		double[] vector;
		
		if (useAc) {
			
			//double[] vector = new double[ 2 * transitInfo.length ];
			//for (int i = 0; i < transitInfo.length; i++){
			vector = new double[ 2 * nTransits ];
			for (int i = 0; i < nTransits; i++){
				vector[ 2 * i     ] = transitInfo[i].getCentroidPosErrorAl();
				vector[ 2 * i + 1 ] = transitInfo[i].getCentroidPosErrorAc();
			}
			
		} else {
			
			//double[] vector = new double[ 2 * transitInfo.length ];
			//for (int i = 0; i < transitInfo.length; i++){
			vector = new double[ nTransits ];
			for (int i = 0; i < nTransits; i++){
				vector[i] = transitInfo[i].getCentroidPosErrorAl();
			}
			
		}
		
		return vector;
				
	}
	
	
	/**
	 * Cut-off function required for error estimate
	 * 
	 * @param magG
	 * @return z as described in Gaia astrometric performance
	 */
	private double cutOffFunction (double magG) {
		final double CUTOFF_MAG = 12;
		final double CUTOFF_ERR = Math.pow(10, 0.4*(CUTOFF_MAG-15));
		double err = Math.pow(10, 0.4*(magG-15));
		return Math.max(err, CUTOFF_ERR);
	}
	
	
	/**
	 * Gets parallax error estimate for given magnitude in uas
	 * 
	 * @param magG Gaia magnitude of object
	 * @param magVI V-I color index
	 * @return Estimate for parallax error
	 */
	private double parallaxError(double magG, double magVI) {
		double colorCorrection = 0.986 + (1-0.986) * magVI;
		double z = cutOffFunction(magG);
		return 1.0e-3 * Math.pow(9.3 + 658.1 * z + 4.568 * Math.pow(z, 2), 0.5) * colorCorrection;
	}
	
	
	/**
	 * Gets the epoch astrometry errors array as derived from model calculations.
	 *
	 * @return the epoch astrometry array
	 * @throws SimuException the simu exception
	 * @throws GaiaException the gaia exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double[] getEpochAstrometryErrors() {
		
		final double NOISE_SCALE = 1.0;							// Scaling factor allowing for pessimistic/optimistic view
		final double ALPHA_AL = 0.869712;						// Fitted error ratio, determined by model fit as 0.869712 +/- 0.000711
		final double ALPHA_AC = 0.163119;						// Fitted error ratio, determined by model fit as 0.163119 +/- 0.000340
		double magVI = star.getPhotometry().getMeanVI();		// V-I color index
		double magG = star.getPhotometry().getMeanAbsoluteMv();	// Absolute magnitude (hopefully G band)
		double sigPar = parallaxError(magG, magVI);				// Parallax error in mas
		double sigTraAl = sigPar / ALPHA_AL;					// Transit error along scan in mas
		double sigTraAc = sigPar / ALPHA_AC;					// Transit error across scan in mas
		
		// Create epoch astrometry error vector in pairs (AL, AC)		
		double[] vector;
		if (useAc){
			vector = new double[ 2 * nTransits ];
			for (int i = 0; i < nTransits; i++){
				vector[ 2 * i     ] = sigTraAl * NOISE_SCALE;
				vector[ 2 * i + 1 ] = sigTraAc * NOISE_SCALE;
			}
		} else {
			vector = new double[ nTransits ];
			for (int i = 0; i < nTransits; i++){
				vector[i] = sigTraAl * NOISE_SCALE;
			}
		}
		
		return vector;
				
	}
	
	
	/**
	 * Creates stellar source and runs GOG on it to obtain transit information
	 *  
	 * @param massSinIncl	Projected mass of planet, i.e. m sin i [M_Jup]
	 * @param inc			Inclination of planetary orbit [rad]
	 * @return				Array of fov transit information
	 * @throws GaiaException
	 * @throws SimuException
	 * @throws IOException
	 */
	public FovTransitInfo[] getTransitInfo( double massSinIncl, double inc ) throws GaiaException, SimuException, IOException {
		
		// Create new stellar source with input inclination angle
		StellarSource source = getStellarSource( star, massSinIncl, period, timePeriastron, eccentricity, omega2, nodeAngle, inc );
		
		// Run GOG for this source
		runGog( source );
		
		// Read first element from epoch astrometry GOG file
		ArrayList<GaiaRoot> epochAstrometryArrayList = new ArrayList<GaiaRoot>();		
		GbinReaderV2 epochAstrometryReader = new GbinReaderV2 ( GOG_EPOCH_ASTROMETRY_FILE );		
		epochAstrometryReader.readAllToList( epochAstrometryArrayList );
		epochAstrometryReader.close();
		LpcCentroid epochAstrometry = (LpcCentroid) epochAstrometryArrayList.get( component );
		
		// Create epoch astrometry vector in pairs (AL, AC)		
		return epochAstrometry.getTransits();
		
	}
	
	
	/**
	 * Gets the epoch astrometry time array from GOG output.
	 *
	 * @return the epoch astrometry array
	 * @throws SimuException the simu exception
	 * @throws GaiaException the gaia exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double[] getEpochAstrometryTime()
	throws SimuException, GaiaException, IOException{
		
		FovTransitInfo[] transitInfo = getTransitInfo(massPlanet, inclination);
		
		double[] vector;
		
		if( useAc ) {
			vector = new double[ transitInfo.length*2 ];
			for (int i = 0; i < transitInfo.length; i++){
				vector[2*i] = transitInfo[i].getObsTime();
				vector[2*i+1] = transitInfo[i].getObsTime();
			}
		} else {
			vector = new double[ transitInfo.length ];
			for (int i = 0; i < transitInfo.length; i++){
				/* Conversion to JD
				 * getObsTime returns time in ns starting from Jan 1, 2010, 0:00 (JD 2455197.5)
				 */
				vector[i] = ((double) transitInfo[i].getObsTime()) / (8.6400e13) + 2455197.5;
			}
		}
		
		return vector;
				
	}
	
	
	/**
	 * Gets the number of transits in the subcatalogue.
	 *
	 * @return the number of transits in the subcatalogue
	 * @throws SimuException the simu exception
	 * @throws GaiaException the gaia exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public int getNTransits()
	throws SimuException, GaiaException, IOException{
		
		// Compute subcatalogue end time (ns)
		double missionStart = MissingParams.MISSION_START * GaiaParam.Nature.JULIANYEAR_DAY * GaiaParam.Nature.DAY_SECOND * 1e9;
		double timeSpan     = subCatalogue                * GaiaParam.Nature.JULIANYEAR_DAY * GaiaParam.Nature.DAY_SECOND * 1e9;
		double subCatalogueTimeEnd = missionStart + timeSpan;
		
		// Get epoch astrometry times
		double[] epochAstrometryTime = getEpochAstrometryTime();
		
		// Get effective number of transits
		int nTransits = 0;
		for (double time : epochAstrometryTime){
			if ( time <= subCatalogueTimeEnd ){
				nTransits++;
			}
		}
		
		if (useAc)
			nTransits /= 2;
		
		return nTransits;
	}


	/**
	 * Checks if is use AC data boolean switch.
	 *
	 * @return true, if AC data is used
	 */
	public boolean isAcUsed() {
		return useAc;
	}
	
	
	/**
	 * Sets the use AC data boolean switch.
	 *
	 * @param useAc the new use AC boolean switch
	 */
	public void setUseAc(boolean useAc) {
		this.useAc = useAc;
	}
	
}

