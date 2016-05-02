package edu.caltech.csn.geocell;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import edu.caltech.csn.geocell.converter.Converter;
import edu.caltech.csn.geocell.converter.GeocellLongConv;
import edu.caltech.csn.geocell.converter.GeostringConv;

/**
 * Library function implementing the derivation and manipulation of geocells.
 * <p>
 * Based off of work on
 * <a href="http://code.google.com/p/geomodel/">GeoModel</a>, which uses
 * strings to perform the same style of hash.
 * <p>
 * This implementation uses longs, providing the ability to use more
 * resolutions.  GeoModel specifies a coordinate's location on a 4x4 grid for
 * each character specified.  This library allows each bit to independently
 * signal whether a North/South or East/West choice was made, giving four
 * times the number of possible resolutions.
 * <p>
 * If preserving aspect ratio is a goal, then the step size should be set to
 * a multiple to 2 to ensure that all stored and returned geocells will be of
 * the same aspect ratio.  If preserving space is a goal, the
 * {MIN,MAX}_USEFUL_RESOLUTION constants can be set, along with the
 * step size, to confine the number of resolutions stored.  The storage medium
 * used can also have an impact; have a look at the various conversion
 * libraries as well as <a href="http://goo.gl/YNnG1">the spreadsheet</a> which
 * details the amount of space used for each algorithm at every possible
 * resolution.
 * <p>
 * This library class is intended to be separate from object implementations.
 * Object implementations for the GWT client and the GAE backend are
 * necessarily distinct, given different aims and limitations, but they should
 * share a common code base in how geocells are determined and manipulated.
 * This separation also allows long values to be easily manipulated without
 * any overhead.
 * <p>
 * This class is that common code base.  Only static methods should be
 * implemented here to be called from the implementations that then layer
 * additional functionality on top of these bare functions.  The
 * GeocellContainer class provides basic object functionality, and also
 * serves as the state maintenance class for the library in functions where
 * bounds play a key role (such as interpolation).  In such cases, Converter
 * arguments are available to specified the desired return type.
 * <p>
 * The library functions provided here can calculate a geocell from a given
 * point, and can then be used to shift that geocell in any of the four
 * cardinal directions.  Additional helper functions could be added to return
 * sets of adjacent cells.
 * <p>
 * Resolution directly specifies the depth of the decision tree.  Each bit
 * represents an east/west (longitude) choice for odd parity bits, and a
 * north/south (latitude) choice for even parity bits.
 * <p>
 * Structure of a geocell long: {up to 58 decision bits}{6 resolution bits}
 *
 * @author Michael Olson <michael.olson@gmail.com>
 */
public final class GeocellLibrary {

	private static final Logger LOG = Logger.getLogger(GeocellLibrary.class.getName());

	/** Minimum resolution that can be created by the library. */
	public static final int MIN_RESOLUTION = 1;
	/** Maximum resolution that can be created by the library. */
	public static final int MAX_RESOLUTION = 58;

	/** The maximum number of queries we are willing to make in one call. */
	public static final int MAX_QUERIES = 30;

	/**
	 * Determines the lower bound for resolutions returned by set functions.
	 * <p>
	 * Together with MAX_USEFUL_RESOLUTION and RESOLUTION_STEP_SIZE,
	 * determines what resolutions between min and max are returned in geocell
	 * groups and, consequently, what resolutions are available for querying.
	 * This forms a range according to the for loop in the getGeocellSet
	 * function:
	 * <pre><code>for (int i  = MIN_USEFUL_RESOLUTION;
	 *         i <= MAX_USEFUL_RESOLUTION;
	 *         i += RESOLUTION_STEP_SIZE)</code></pre>
	 * <code>getGeocellSet</code>, <code>getQueryCells</code>, and
	 * <code>interpolation</code> will only return cells from this range.
	 */
	public static final int MIN_USEFUL_RESOLUTION = 7;
	/** Determines the upper bound for resolutions returned by set functions. */
	public static final int MAX_USEFUL_RESOLUTION = 35;
	/** Determines the step size for resolutions returned by set functions. */
	public static final int RESOLUTION_STEP_SIZE = 1;

	/** The starting points for geocell calculation. */
	public static final Bounds WORLD_BOUNDS =
			new Bounds(-90d, -180d, 90d, 180d);

	// Caches a converter instance for the default conversion type.
	private static final GeocellLongConv DEFAULT_CONVERTER = new GeocellLongConv();

	// Convenience constant for bit manipulation.
	// 63L is the resolution width in all 1s.
	private static final long RESOLUTION_MASK = 63L;

	// Controls how may resolutions will be checked, at most, past an
	// increasing cost function in the case that the disparity between the
	// real bounded size and the cell bounded size is large.
	private static final int MAX_EXPLORE = 10;

	/** Utility classes should not have a public or default constructor. */
	private GeocellLibrary() {
	}

	public static long getGeocell(final double latitude, final double longitude) {
		return getGeocell(latitude, longitude, MAX_RESOLUTION);
	}

	public static long getGeocell(final double latitude, final double longitude,
			final int resolution) {
		final GeocellContainer geocellContainer = makeGeocell(latitude, longitude);
		increaseResolution(geocellContainer, resolution);
		return geocellContainer.geocell;
	}

	public static long getGeocell(final Point pt) {
		return getGeocell(pt, MAX_RESOLUTION);
	}

	public static long getGeocell(final Point pt, final int resolution) {
		return getGeocell(pt.getLatitude(), pt.getLongitude(), resolution);
	}

	/**
	 * Initialize GeocellContainer object for state maintenance in some methods.
	 */
	private static GeocellContainer makeGeocell(final double latitude, final double longitude) {
		final GeocellContainer geocellContainer = new GeocellContainer();
		geocellContainer.latitude = latitude;
		geocellContainer.longitude = longitude;
		geocellContainer.point = true;
		geocellContainer.bounds = new Bounds(WORLD_BOUNDS);
		return geocellContainer;
	}

	/**
	 * Increase a geocell's resolution.
	 * <p>
	 * Also the means by which a new geocell is created from a point.
	 *
	 * @param cellContainer
	 * @param res
	 */
	protected static void increaseResolution(final GeocellContainer cellContainer, final int res) {
		final int currRes = getResolution(cellContainer.geocell);

		// With the current storage mechanism, a resolution greater than 58 is
		// not possible with the number of available bits.
		final int resolution = Math.min(res, MAX_RESOLUTION);

		// Compute number of loop iterations and initial geocell value.
		// We only operate on right aligned values and only store left aligned.
		final int maxdepth = resolution - currRes;
		cellContainer.geocell = cellContainer.geocell >>> shiftDistance(currRes);
		final int oddOrEven = currRes % 2;

		for (int i = 0; i < maxdepth; i++) {
			cellContainer.geocell = cellContainer.geocell << 1;

			if (i % 2 == oddOrEven) {
				final double lonMid = (cellContainer.bounds.swLon + cellContainer.bounds.neLon) / 2;
				if (cellContainer.longitude < lonMid) {
					cellContainer.bounds.neLon = lonMid;
				} else {
					cellContainer.bounds.swLon = lonMid;
					cellContainer.geocell += 1;
				}
			} else {
				final double latMid = (cellContainer.bounds.swLat + cellContainer.bounds.neLat) / 2;
				if (cellContainer.latitude < latMid) {
					cellContainer.bounds.neLat = latMid;
				} else {
					cellContainer.bounds.swLat = latMid;
					cellContainer.geocell += 1;
				}
			}
		}

		// Align with standard geocell bit start location and add
		// trailing zeros to compensate for a resolution below 58.
		// Then add resolution to least significant bits.
		cellContainer.geocell = cellContainer.geocell << shiftDistance(res);
		cellContainer.geocell += resolution;
	}

	/**
	 * Function to decrease the resolution of a geocell without its bounds.
	 *
	 * @param geocell the geocell to decrease the resolution of
	 * @param newRes the resolution to decrease to
	 * @return the long of the decreased resolution geocell
	 */
	public static long decreaseResolution(final long geocell, final int newRes) {
		return ((geocell >>> shiftDistance(newRes)) << shiftDistance(newRes)) + newRes;
	}

	/**
	 * Modifies the resolution of the provided container to be newRes.
	 * <p>
	 * Bounds cannot be efficiently recalculated when decreasing the resolution
	 * because of rounding errors introduced by doubles.  The bounds must
	 * always be recalculated so that their calculation is consistent.
	 */
	protected static void decreaseResolution(final GeocellContainer geocellContainer,
			final int newRes) {
		final boolean hadPoint = geocellContainer.point;
		if (!geocellContainer.point) {
			geocellContainer.latitude = (geocellContainer.bounds.swLat
					+ geocellContainer.bounds.neLat) / 2;
			geocellContainer.longitude = (geocellContainer.bounds.swLon
					+ geocellContainer.bounds.neLon) / 2;
			geocellContainer.point = true;
		}
		geocellContainer.geocell = 0L;
		geocellContainer.bounds = new Bounds(WORLD_BOUNDS);
		increaseResolution(geocellContainer, newRes);
		if (!hadPoint) {
			geocellContainer.point = false;
		}
	}

	/**
	 * Helper function to centralize calculation of bit shift distances.
	 * <p>
	 * This makes it easier to change and harder to get wrong.  The parameter
	 * is named resolution since this is most frequently used to take a
	 * right-aligned geocell and shift it left so it lines up with where bits
	 * in a geocell should be, or to reverse the same operation.  However,
	 * that fact also makes it easy to shift a particular set of bits to the
	 * position of the bits for a particular resolution for modification.
	 * <p>
	 * For instance, a resolution 1 geocell would have to be shifted 63 bits to
	 * the right to be right aligned, while a resolution 58 geocell would have
	 * to be shifted 6 bits to the right (the 6 bits reserved for resolution)
	 * to be right aligned.
	 * <p>
	 * This formula is used because geocells are always right aligned while
	 * being manipulated so that addition can be used to set bits (the
	 * alternative being zeroing the bits of the resolution to set and then
	 * setting the appropriate bit pattern after left shifting it).  Since
	 * adding and left shift is two operations and left shifting a bit pattern
	 * and anding it is two operations, it seemed, performance wise, that bit
	 * shifting patterns was not strikingly better and that keeping geocells
	 * right aligned made their manipulation more intuitive.  For this reason,
	 * after manipulation, geocells must be left shifted the number of bits
	 * returned by the formula below before setting the resolution, and, before
	 * manipulation, must be right shifted the number of bits returned by the
	 * formula below.
	 *
	 * @param resolution the resolution to shift to or adjust using
	 * @return the number of bits to shift
	 */
	public static int shiftDistance(final int resolution) {
		return Long.SIZE - resolution;
	}

	/**
	 * Method for accessing geocell's resolution.
	 * <p>
	 * Resolution is currently stored in the six least significant bits of
	 * the long.
	 *
	 * @param geocell the geocell to retrieve the resolution of
	 * @return the current resolution of the geocell
	 */
	public static int getResolution(final long geocell) {
		return (int) (geocell & RESOLUTION_MASK);
	}

	/**
	 * This function returns the bit from the geocell at the specified res.
	 * <p>
	 * Assumes an already left aligned geocell.
	 *
	 * @param geocell the geocell string to extract a bit from
	 * @param resolution the resolution depth to extract the bit at
	 * @return the bit that represents the choice made at a
	 *   particular resolution
	 */
	public static long getBit(final long geocell, final int resolution) {
		return (geocell >>> shiftDistance(resolution)) & 1;
	}

	/**
	 * Retrieves the geocell one geocell east of the provided geocell.
	 *
	 * @param geocell the geocell we want the eastern neighbor for
	 * @return the long representing the geocell's eastern neighbor
	 */
	public static long getEast(final long geocell) {
		return getEast(geocell, getResolution(geocell));
	}
	private static long getEast(final long geocell, final int resolution) {
		final int latShift, lonShift;
		final long geochar, latBit, lonBit, modifier;
		if ((resolution & 1) == 1) {
			latShift = resolution - 1;
			lonShift = resolution;
			latBit = getBit(geocell, latShift);
			lonBit = getBit(geocell, lonShift);
			geochar = latBit << 1 | lonBit;
			modifier = 2L;
		} else {
			latShift = resolution;
			lonShift = resolution - 1;
			latBit = getBit(geocell, latShift);
			lonBit = getBit(geocell, lonShift);
			geochar = lonBit << 1 | latBit;
			modifier = 1L;
		}
		long nextGeocell = 0;

		// Western half of any immediate 2x2 grid have their least significant
		// bit as 0, so adding 1 will put it in the eastern half.
		if (lonBit == 0) {
			nextGeocell = geocell + (1L << shiftDistance(lonShift));
		// We know we're in the eastern half of the 2x2 grid, so let's pop out
		// to the next higher grid.
		} else {
			// For anything except the highest resolution, we need to recurse.
			// We could have to shift multiple levels of parents to find the
			// right cell.
			if (resolution > 2) {
				// Step size is 2 because we have to get to the next set of
				// longitude bits, not the next set of latitude bits.
				nextGeocell = getEast(geocell, resolution - 2);
			// At the highest resolution, wrap around the world.
			} else {
				nextGeocell = geocell;
			}
			// This operation zeros out the geochar we're working with
			// and replaces it with a version in which the bits have been
			// &ed with 10 to move from the easternmost edge of the
			// original parent to the westernmost edge of the new parent.
			final int shift = shiftDistance(resolution);
			nextGeocell = nextGeocell & (~(3L << shift));
			nextGeocell += (geochar & modifier) << shift;
		}

		return nextGeocell;
	}

	/**
	 * Retrieves the geocell one geocell west of the provided geocell.
	 *
	 * @param geocell the geocell we want the western neighbor for
	 * @return the long representing the geocell's western neighbor
	 */
	public static long getWest(final long geocell) {
		return getWest(geocell, getResolution(geocell));
	}
	private static long getWest(final long geocell, final int resolution) {
		final int latShift, lonShift;
		final long geochar, latBit, lonBit, modifier;
		if ((resolution & 1) == 1) {
			latShift = resolution - 1;
			lonShift = resolution;
			latBit = getBit(geocell, latShift);
			lonBit = getBit(geocell, lonShift);
			geochar = latBit << 1 | lonBit;
			modifier = 1L;
		} else {
			latShift = resolution;
			lonShift = resolution - 1;
			latBit = getBit(geocell, latShift);
			lonBit = getBit(geocell, lonShift);
			geochar = lonBit << 1 | latBit;
			modifier = 2L;
		}
		long nextGeocell = 0;

		// Eastern half of any immediate 2x2 grid have their least significant
		// bit as 1, so subtracting 1 will put it in the western half.
		if (lonBit == 1) {
			nextGeocell = geocell - (1L << (shiftDistance(lonShift)));
		// We know we're in the eastern half of the 2x2 grid, so let's pop out
		// to the next higher grid.
		} else {
			// For anything except the highest resolution, we need to recurse.
			// We could have to shift multiple levels of parents to find the
			// right cell.
			if (resolution > 2) {
				// Step size is 2 because we have to get to the next set of
				// longitude bits, not the next set of latitude bits.
				nextGeocell = getWest(geocell, resolution - 2);
			// At the highest resolution, wrap around the world.
			} else {
				nextGeocell = geocell;
			}
			// This operation zeros out the geochar we're working with
			// and replaces it with a version in which the bits have been
			// |ed with 01 to move from the westernmost edge of the
			// original parent to the easternmost edge of the new parent.
			final int shift = shiftDistance(resolution);
			nextGeocell = nextGeocell & (~(3L << shift));
			nextGeocell += (geochar | modifier) << shift;
		}

		return nextGeocell;
	}

	/**
	 * Retrieves the geocell one geocell north of the provided geocell.
	 *
	 * @param geocell the geocell we want the northern neighbor for
	 * @return the long representing the geocell's northern neighbor
	 */
	public static long getNorth(final long geocell) {
		return getNorth(geocell, getResolution(geocell));
	}
	private static long getNorth(final long geocell, final int resolution) {
		final int latShift, lonShift;
		final long geochar, latBit, lonBit, modifier;
		if ((resolution & 1) == 1) {
			latShift = resolution - 1;
			lonShift = resolution;
			latBit = getBit(geocell, latShift);
			lonBit = getBit(geocell, lonShift);
			geochar = latBit << 1 | lonBit;
			modifier = 1L;
		} else {
			latShift = resolution;
			lonShift = resolution - 1;
			latBit = getBit(geocell, latShift);
			lonBit = getBit(geocell, lonShift);
			geochar = lonBit << 1 | latBit;
			modifier = 2L;
		}
		long nextGeocell = 0;

		// Southern half of any immediate 2x2 grid have their 1st
		// bit as 0, so adding 1 will put it in the northern half.
		if (latBit == 0) {
			nextGeocell = geocell + (1L << (shiftDistance(latShift)));
		// We know we're in the northern half of the 2x2 grid, so let's pop
		// out to the next higher grid.
		} else {
			// For anything except the highest resolution, we need to
			// recurse (we could have to shift multiple levels of
			// parents to find the right cell).
			if (resolution > 2) {
				// Step size is 2 because we have to get to the next set of
				// latitude bits, not the next set of longitude bits.
				nextGeocell = getNorth(geocell, resolution - 2);
			// At the highest resolution, wrap around the world.
			// TODO: This should probably error out since you can't wrap n -> s.
			} else {
				nextGeocell = geocell;
			}
			// This operation zeros out the geochar we're working with
			// and replaces it with a version in which the bits have been
			// &ed with 01 to move from the northernmost edge of the
			// original parent to the southernmost edge of the new parent.
			final int shift = shiftDistance(resolution);
			nextGeocell = nextGeocell & (~(3L << shift));
			nextGeocell += (geochar & modifier) << shift;
		}

		return nextGeocell;
	}

	/**
	 * Retrieves the geocell one geocell south of the provided geocell.
	 *
	 * @param geocell the geocell we want the northern neighbor for
	 * @return the long representing the geocell's northern neighbor
	 */
	public static long getSouth(final long geocell) {
		return getSouth(geocell, getResolution(geocell));
	}
	private static long getSouth(final long geocell, final int resolution) {
		final int latShift, lonShift;
		final long geochar, latBit, lonBit, modifier;
		if ((resolution & 1) == 1) {
			latShift = resolution - 1;
			lonShift = resolution;
			latBit = getBit(geocell, latShift);
			lonBit = getBit(geocell, lonShift);
			geochar = latBit << 1 | lonBit;
			modifier = 2L;
		} else {
			latShift = resolution;
			lonShift = resolution - 1;
			latBit = getBit(geocell, latShift);
			lonBit = getBit(geocell, lonShift);
			geochar = lonBit << 1 | latBit;
			modifier = 1L;
		}
		long nextGeocell = 0;

		// Northern half of any immediate 2x2 grid have their 1st
		// bit as 1, so subtracting 1 will put it in the southern half.
		if (latBit == 1) {
			nextGeocell = geocell - (1L << (shiftDistance(latShift)));
		// We know we're in the southern half of the 2x2 grid, so let's pop
		// out to the next higher grid.
		} else {
			// For anything except the highest resolution, we need to
			// recurse (we could have to shift multiple levels of
			// parents to find the right cell).
			if (resolution > 2) {
				// Step size is 2 because we have to get to the next set of
				// latitude bits, not the next set of longitude bits.
				nextGeocell = getSouth(geocell, resolution - 2);
			// At the highest resolution, wrap around the world.
			// TODO: This should probably error out since you can't wrap n -> s.
			} else {
				nextGeocell = geocell;
			}
			// This operation zeros out the geochar we're working with
			// and replaces it with a version in which the bits have been
			// |ed with 10 to move from the southernmost edge of the
			// original parent to the northernmost edge of the new parent.
			final int shift = shiftDistance(resolution);
			nextGeocell = nextGeocell & (~(3L << shift));
			nextGeocell += (geochar | modifier) << shift;
		}

		return nextGeocell;
	}

	/**
	 * Returns the set of geocells in the useful resolution range.
	 * <p>
	 * Returns the full set of geocells to be stored based on the constant
	 * minimum and maximum useful resolutions specified.  This set is then
	 * stored as a set with data so that membership of a specified geocell
	 * can be determined.
	 *
	 * @param latitude the latitude of the point
	 * @param longitude the longitude of the point
	 * @return the set of geocells at resolutions between and including
	 *   MIN_USEFUL_RESOLUTION and MAX_USEFUL_RESOLUTION with
	 *   RESOLUTION_STEP_SIZE increments
	 */
	public static Set<Long> getGeocellSet(final double latitude, final double longitude) {
		return getGeocellSet(latitude, longitude, DEFAULT_CONVERTER);
	}
	public static <T> Set<T> getGeocellSet(final double latitude, final double longitude,
			final Converter<T> converter) {
		final Set<T> result = new HashSet<T>();
		final GeocellContainer geocellContainer = makeGeocell(latitude, longitude);

		for (int i = MIN_USEFUL_RESOLUTION;
				i <= MAX_USEFUL_RESOLUTION; i += RESOLUTION_STEP_SIZE) {
			increaseResolution(geocellContainer, i);
			result.add(converter.fromGeocellContainer(geocellContainer));
		}

		return result;
	}

	/**
	 * Get set of cells to query given a bounding box.
	 * <p>
	 * This function uses a naive cost function to attempt to determine the
	 * resolution that should be used to generate a geocell grid that
	 * efficiently covers the space specified by the pair of points provided.
	 * The generated grid is merged where possible (where merged cells would
	 * belong to the set returned by getGeocellSet) by the interpolation
	 * function.
	 * <p>
	 * This function is currently biased toward small collections of
	 * geocells, even if they cover large regions outside the desired area.
	 * In reality, the cost function should be determined by the density of
	 * the objects being queried.  Querying a larger space is less expensive in
	 * a sparse network, while executing many queries is more efficient in a
	 * dense network.
	 *
	 * @param swLat the southwest point of the space to be covered
	 * @param swLon
	 * @param neLat the northeast point of the space to be covered
	 * @param neLon
	 * @return the set of geocells that covers that space at a resolution
	 *   determined by the built-in cost function
	 */
	public static Set<Long> getQueryCells(final double swLat, final double swLon,
			final double neLat, final double neLon) {
		return getQueryCells(swLat, swLon, neLat, neLon, DEFAULT_CONVERTER);
	}
	public static <T> Set<T> getQueryCells(final double swLat, final double swLon,
			final double neLat, final double neLon, final Converter<T> converter) {
		Set<T> result = null;

		// Calculate the full geocell for the boundary corners.
		final GeocellContainer southWest = new GeocellContainer(swLat, swLon);
		final GeocellContainer northEast = new GeocellContainer(neLat, neLon);

		// If the southwest and northeast cells are the same at the maximum
		// resolution, then that is the best we can do (because there is no
		// available higher resolution to work with).
		if (southWest.equals(northEast)) {
			result = new HashSet<T>();
			result.add(converter.fromGeocellContainer(southWest));
			return result;
		}

		// Calculate the center of the northwest and northeast bounding points.
		// This is used to fix problems with wrapping around the world at
		// large resolutions.
		final Bounds bounds = new Bounds(swLat, swLon, neLat, neLon);
		final Point centerTop = new Point(neLat, bounds.getCenter().getLongitude());

		// Initialize the starting value of the best set found so far.
		double best = Double.MAX_VALUE;

		// Calculate the common prefix between the southwest and northeast
		// cells to use as a starting point for determining which geocells
		// should be used.
		int minRes = 0;
		for (int i = 1; i <= MAX_USEFUL_RESOLUTION; i++) {
			if (getBit(southWest.geocell, i) == getBit(northEast.geocell, i)) {
				minRes++;
			} else {
				break;
			}
		}
		LOG.fine("Maximum prefix of " + southWest + " and " + northEast + " is " + minRes);
		if (minRes < MIN_USEFUL_RESOLUTION) {
			minRes = MIN_USEFUL_RESOLUTION;
		} else {
			// Align with a multiple of step size.
			// This is a no-op with a step size of one.
			minRes -= (minRes - MIN_USEFUL_RESOLUTION) % RESOLUTION_STEP_SIZE;
		}

		// We calculate the rough land area bounded as one of the parameters of
		// the cost function which determines what resolution to use for the
		// smallest boxes.
		final double boundedArea = bounds.getArea();
		LOG.fine("Bounded area of map is roughly " + boundedArea + " miles");

		decreaseResolution(southWest, minRes);
		decreaseResolution(northEast, minRes);
		final GeocellContainer northWest = new GeocellContainer(neLat, swLon, minRes);
		int explore = 0;

		// Loop from the minimum resolution to the maximum and try each one.
		for (int i = minRes; i <= MAX_USEFUL_RESOLUTION; i += RESOLUTION_STEP_SIZE) {
			increaseResolution(southWest, i);
			increaseResolution(northEast, i);
			increaseResolution(northWest, i);

			// Get the boxes created by this set of points.
			// TODO: This call could be slow to iterate through
			final Set<T> boxes;
			if (northWest.geocell == northEast.geocell
					&& !northWest.bounds.contains(centerTop)) {
				// Fix world wrapping.
				final GeocellContainer newNe = new GeocellContainer(northEast);
				newNe.moveWest();
				boxes = interpolation(southWest, newNe, northWest, converter);
			} else {
				boxes = interpolation(southWest, northEast, northWest, converter);
			}

			final int numCells = boxes.size();

			// Determine rough land area occupied by cell grid.
			final double cellArea = southWest.bounds.getArea() * numCells;
			LOG.finer("Bounded area of cells at resolution "
					+ i + " is roughly " + cellArea + " miles");
			LOG.finer("Number of cells required to cover bounded area at resolution "
					+ i + " is " + numCells);

			// TODO: What cost function might be better?
			// Determine the cost of this resolution level
			final double newBest =
//				( Math.sqrt((cellArea - boundedArea))
				(cellArea - boundedArea) / 2
				+ Math.pow(numCells, 2);
			LOG.finer("Cost function is thus: " + newBest);

			// If the cost function is increasing, we should stop.  Also,
			// if the number of cells exceeds the number we're willing to
			// query, it doesn't really matter what the cost function is.
			if (newBest > best || numCells > MAX_QUERIES) {
				// Area is a funny thing at low resolutions, and a simple
				// split can cause the cell area to go up; this condition
				// promotes exploring higher resolutions when the area
				// occupied by the "solution" is obviously still too much.
				// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINE.
				if (cellArea > boundedArea * 10) {
					explore += RESOLUTION_STEP_SIZE;
					if (explore < MAX_EXPLORE) {
						continue;
					}
				}
				explore = 0;
				LOG.finer("Cost function increasing; stopping");
				// Too many cells to query in first iteration.
				// TODO: Should this throw an exception?
				if (result == null) {
					LOG.warning("Function exited without viable result.");
					result = new HashSet<T>();
				}
				break;
			} else {
				best = newBest;
				result = boxes;
			}
		}

		return result;
	}

	public static <T> Set<T> interpolation(final GeocellContainer southWest,
			final GeocellContainer northEast, final GeocellContainer northWest,
			final Converter<T> converter) {
		// Calculate the width and height of the grid created by the three
		// corners.
		final int[] gridSize = gridDimensions(
				southWest.geocell, northEast.geocell, northWest.geocell);
		final int width = gridSize[0];
		final int height = gridSize[1];
		final Set<T> result = new HashSet<T>();

		// Used to flag cells that do not need to be checked.  This is done
		// when an earlier cell is expanded; the cells that it expands to
		// cover do not need to be checked.
		final boolean[][] grid = new boolean[height][width];
		final Bounds bounds = new Bounds(southWest.latitude, southWest.longitude,
				northEast.latitude, northEast.longitude);

		final GeocellContainer row = new GeocellContainer(northWest);

		for (int i = 0; i < height; i++) {
			final GeocellContainer curr = new GeocellContainer(row);

			for (int j = 0; j < width; j++) {

				// If this space hasn't been previously excluded, check it.
				if (!grid[i][j]) {
					result.add(mergeNeighborsShrinkCorners(curr, grid, i, j,
							height, width, bounds, converter));
				}

				curr.moveEast();
			}

			row.moveSouth();
		}

		return result;
	}

	// CHECKSTYLE IGNORE ParameterNumber FOR NEXT 1 LINE.
	private static <T> T mergeNeighborsShrinkCorners(final GeocellContainer curr,
			final boolean[][] grid, final int i, final int j, final int height,
			final int width, final Bounds bounds, final Converter<T> converter) {
		// This tracks what the geocell of the expanded cell will be.
		long bigCell = curr.geocell;
		int resolution = getResolution(bigCell);

		// Tracks the number of cells across or down that must
		// be available in order to decrease the resolution of
		// the geocell by RESOLUTION_STEP_SIZE.
		int decResWidth = 1;
		int decResHeight = 1;
		for (int k = 0; k < RESOLUTION_STEP_SIZE; k++) {
			if (((resolution - k) & 1) == 1) {
				decResWidth *= 2;
			} else {
				decResHeight *= 2;
			}
		}

		// As long as we remain in a northern/western cell (depending on the
		// resolution and step size) and there is room to expand, that is, all
		// of the cells that form the next step size lower resolution are
		// available, then we can keep expanding.  Each time, reduce the
		// resolution by RESOLUTION_STEP_SIZE.
		// Note: if we enter a cell that is not a northern/western cell, then
		// it should be obvious we can't expand further.  If we
		// could, then we would have already done so because
		// the current cell would have been a member of a
		// previously encountered northern/western cell's neighbors.
		while (isNOrWCell(bigCell, resolution)
				&& resolution > MIN_USEFUL_RESOLUTION
				&& decResWidth - 1 + j < width
				&& decResHeight - 1 + i < height) {
			// Reduce the cell resolution.
			resolution -= RESOLUTION_STEP_SIZE;
			bigCell = decreaseResolution(bigCell, resolution);

			// Force the next loop iteration to check for the
			// availability of more cells.
			for (int k = 0; k < RESOLUTION_STEP_SIZE; k++) {
				if (((resolution - k) & 1) == 1) {
					decResWidth *= 2;
				} else {
					decResHeight *= 2;
				}
			}
		}

		// If bigCell changed, we successfully merged cells.
		if (bigCell != curr.geocell) {
			// The last while loop check failed, so we need to back out
			// the height/width size changes.
			for (int k = 0; k < RESOLUTION_STEP_SIZE; k++) {
				if (((resolution - k) & 1) == 1) {
					decResWidth /= 2;
				} else {
					decResHeight /= 2;
				}
			}

			// Mark off cells that are part of the larger cell so
			// that they are not checked.
			for (int k = 0; k < decResHeight; k++) {
				for (int l = 0; l < decResWidth; l++) {
					grid[i + k][j + l] = true;
				}
			}

			// Return the merged cell.
			return converter.fromGeocell(bigCell);

		// Attempt to shrink corner cells if possible.  In order to shrink a
		// cell, the resulting increased resolution cell must exist at the
		// opposite position of the corner to be shrunk.  That is, if
		// shrinking the northwest corner by longitude, the increased
		// resolution cell must be an eastern cell.  Likewise, if shrinking
		// the same cell by latitude, the increased resolution cell must be
		// a southern cell.
		} else if ((i == 0 || i == height - 1)
				&& (j == 0 || j == width - 1)) {
			GeocellContainer shrunk = new GeocellContainer(curr);
			resolution = shrunk.getResolution();

			// Identify the appropriate direction of movement based on whether
			// the resolution is odd (longitude bits) or even (latitude bits).
			long seekingOdd;
			long seekingEven;
			if (i == 0) {
				// NW corner, seeking southern/eastern.
				if (j == 0) {
					shrunk.latitude = bounds.neLat;
					shrunk.longitude = bounds.swLon;
					seekingOdd = 1;
					seekingEven = 0;
				// NE corner, seeking southern/western.
				} else {
					shrunk.latitude = bounds.neLat;
					shrunk.longitude = bounds.neLon;
					seekingOdd = 0;
					seekingEven = 0;
				}
			} else {
				// SW corner, seeking northern/eastern.
				if (j == 0) {
					shrunk.latitude = bounds.swLat;
					shrunk.longitude = bounds.swLon;
					seekingOdd = 1;
					seekingEven = 1;
				// SE corner, seeking northern/western.
				} else {
					shrunk.latitude = bounds.swLat;
					shrunk.longitude = bounds.neLon;
					seekingOdd = 0;
					seekingEven = 1;
				}
			}
			outer: while (resolution + RESOLUTION_STEP_SIZE
					< MAX_USEFUL_RESOLUTION) {
				final GeocellContainer test = new GeocellContainer(shrunk);
				resolution += RESOLUTION_STEP_SIZE;
				increaseResolution(test, resolution);

				// Check that cell lies in desired direction.
				for (int k = 0; k < RESOLUTION_STEP_SIZE; k++) {
					final int testing = resolution - k;
					if ((testing & 1) == 1) {
						if (getBit(test.geocell, testing) != seekingOdd) {
							break outer;
						}
					} else {
						if (getBit(test.geocell, testing) != seekingEven) {
							break outer;
						}
					}
				}

				shrunk = test;
			}
			return converter.fromGeocellContainer(shrunk);
		// No shrinking or merging, just return.
		} else {
			return converter.fromGeocellContainer(curr);
		}
	}

	/**
	 * Helper function to ascertain if a geocell remains to the north and west.
	 * <p>
	 * When iterating through cells for merging, we know that a cell can only
	 * be merged if all of its constituent cells at the next lower resolution
	 * are available.  This is done by always merging from the north or west
	 * and checking on the availability of the remaining cells.  Once we come
	 * to any cell which does not meet the below condition, we know that if
	 * its neighbor cells in a lower resolution existed in the current grid,
	 * they would have already been checked when the cell that met the below
	 * conditions was encountered.
	 * <p>
	 * For any transition by RESOLUTION_STEP_SIZE from one resolution to the
	 * next, only one cell from the next lower resolution should be able to
	 * meet the below criteria, so the ability to merge should only be checked
	 * once, if at all, for each set of merging candidate cells.
	 *
	 * @param geocell geocell to test whether it is a northwest cell or not
	 * @return true if the cell is in the northwest quadrant for its
	 *   resolution or false otherwise
	 */
	private static boolean isNOrWCell(final long geocell, final int resolution) {
		for (int i = 0; i < RESOLUTION_STEP_SIZE; i++) {
			final int check = resolution - i;
			final long bit = getBit(geocell, check);
			if ((check & 1) == 1) {
				if (bit == 1) {
					return false;
				}
			} else {
				if (bit != 1) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Calculate the grid dimensions of a grid specified by the three corners.
	 * <p>
	 * While two corners is sufficient, three corners leads to a more efficient
	 * algorithm.
	 *
	 * @param sw southwest corner of geocell grid
	 * @param ne northeast corner of geocell grid
	 * @param nw northwest corner of geocell grid
	 * @return length two array containing the width and height of the grid
	 */
	private static int[] gridDimensions(final long sw, final long ne, final long nw) {
		int width = 1;
		int height = 1;
		long curr = nw;

		// Broken if nw/ne wrap around world, which is why getQueryCells passes
		// a modified northeast cell to interpolation.
		while (curr != ne) {
			curr = getEast(curr);
			width++;
		}
		curr = nw;
		while (curr != sw) {
			curr = getSouth(curr);
			height++;
		}

		return new int[] {width, height};
	}

	/**
	 * Creates an unmerged grid of Geocells at a fixed resolution.
	 * <p>
	 * Performs the same general work as getQueryCells/interpolation, but not
	 * designed to test for a good resolution like getQueryCells and not
	 * designed to alter the resolution of discovered geocells like
	 * interpolation.  Instead, used to get a grid covering a specified Bounds
	 * at a specified resolution.
	 *
	 * @param bnds the bounds that the returned grid should cover
	 * @param resolution the resolution to return the geocells at
	 * @param converter the converter object to get the desired return type
	 * @return
	 */
	public static <T> Set<T> fixedGrid(final Bounds bnds, final int resolution,
			final Converter<T> converter) {
		final Set<T> result = new HashSet<T>();
		final GeocellContainer northWest = new GeocellContainer(
				bnds.getNorthWest(), resolution);
		long ne = getGeocell(bnds.getNorthEast(), resolution);
		final long sw = getGeocell(bnds.getSouthWest(), resolution);
		final Point centerTop = new Point(bnds.neLat,
				bnds.getCenter().getLongitude());
		if (northWest.geocell == ne && !northWest.bounds.contains(centerTop)) {
			ne = getWest(ne);
		}

		final int[] gridSize = gridDimensions(sw, ne, northWest.geocell);
		final int width = gridSize[0];
		final int height = gridSize[1];
		final GeocellContainer row = new GeocellContainer(northWest);

		for (int i = 0; i < height; i++) {
			final GeocellContainer curr = new GeocellContainer(row);
			for (int j = 0; j < width; j++) {
				result.add(converter.fromGeocellContainer(curr));
				curr.moveEast();
			}
			row.moveSouth();
		}
		return result;
	}

	/**
	 * Calculate bounds for a given geocell.
	 * <p>
	 * This function is used to calculate the southWest and northEast bounds
	 * for a geocell whose long representation has already been initialized.
	 * The function simply iterates through the bit values in the long to
	 * determine what the bounding box of the geocell should be.
	 * <p>
	 * Users of this function may want to consider using the GeocellContainer
	 * object class so that redetermining the bounds is not necessary.
	 */
	public static Bounds recalculateBounds(final long geocell) {
		final int resolution = getResolution(geocell);

		// Initialize starting latitudes and longitudes
		final Bounds bounds = new Bounds(WORLD_BOUNDS);

		for (int i = 1; i <= resolution; i++) {
			final long bit = getBit(geocell, i);
			if (i % 2 == 0) {
				final double latMid = (bounds.swLat + bounds.neLat) / 2;
				if (bit == 1) {
					bounds.swLat = latMid;
				} else {
					bounds.neLat = latMid;
				}
			} else {
				final double lonMid = (bounds.swLon + bounds.neLon) / 2;
				if (bit == 1) {
					bounds.swLon = lonMid;
				} else {
					bounds.neLon = lonMid;
				}
			}
		}

		return bounds;
	}

	/**
	 * Determines if a point lies inside a given geocell.
	 * <p>
	 * If using GeocellContainer, container.getBounds().contains() will
	 * perform the same function with less computation.
	 *
	 * @param geocell the geocell to check if a point lies within it
	 * @param pt the point to determine containment
	 * @return true if the geocell contains the point, false otherwise
	 */
	public static boolean contains(final long geocell, final Point pt) {
		return geocell == getGeocell(pt, getResolution(geocell));
	}

	/**
	 * Return this geocell's immediate ancestor.
	 * <p>
	 * This function is just an alias to decreaseResolution.  If moving
	 * multiple steps it is more efficient to just call decreaseResolution
	 * directly on the geocell than to call ancestor multiple times.
	 *
	 * @param geocell the geocell to return the ancestor of
	 * @return the immediate ancestor of geocell
	 */
	public static long ancestor(final long geocell) {
		return decreaseResolution(geocell,
				getResolution(geocell) - RESOLUTION_STEP_SIZE);
	}

	/**
	 * Return whether or not parent is an ancestor of child.
	 *
	 * @param parent the geocell to determine ancestor status for
	 * @param child the geocell to determine ancestry of
	 * @return true if child is a higher resolution than and a descendant of
	 *   parent
	 */
	public static boolean isAncestorOf(final long parent, final long child) {
		final int parentRes = getResolution(parent);
		if (getResolution(child) <= parentRes) {
			return false;
		}

		return decreaseResolution(child, parentRes) == parent;
	}

	/**
	 * Calculates all of the children of geocell at the next resolution step.
	 * <p>
	 * This function will return cells outside of MAX_USEFUL_RESOLUTION
	 * if asked to do so.  It is primarily used by the childrenAtResolution
	 * function.
	 *
	 * @param geocell the geocell to calculate the immediate children of
	 * @return all children of the geocell at the next resolution step size
	 *   provided the next step does not exceed MAX_RESOLUTION
	 */
	public static Set<Long> children(final long geocell) {
		int resolution = getResolution(geocell);
		if (resolution + RESOLUTION_STEP_SIZE > MAX_RESOLUTION) {
			return null;
		}

		final HashSet<Long> result = new HashSet<Long>();
		final long shifted = (geocell >>> shiftDistance(resolution)) << RESOLUTION_STEP_SIZE;
		resolution += RESOLUTION_STEP_SIZE;

		for (int i = 0; i < 2 << RESOLUTION_STEP_SIZE; i++) {
			final long child = (shifted + i) << shiftDistance(resolution);
			result.add(child + resolution);
		}
		return result;
	}

	/**
	 * Calculates all of the children of the geocell at the given resolution.
	 *
	 * @param geocell the geocell to calculat the children of
	 * @param childRes the resolution to find the children of the geocell at
	 * @return all of the children of geocell at resolution childRes
	 */
	public static Set<Long> childrenAtResolution(final long geocell, final int childRes) {
		final int initResolution = getResolution(geocell);
		if (childRes > MAX_RESOLUTION
				// This is a no-op with a step size of one.
				|| childRes - initResolution % RESOLUTION_STEP_SIZE != 0) {
			return null;
		}

		HashSet<Long> nextParents = new HashSet<Long>();
		nextParents.add(Long.valueOf(geocell));
		for (int i = initResolution; i < childRes; i += RESOLUTION_STEP_SIZE) {
			final HashSet<Long> parents = nextParents;
			nextParents = new HashSet<Long>();
			for (Long parentCell : parents) {
				nextParents.addAll(children(parentCell));
			}
		}

		return nextParents;
	}

	/**
	 * Return the B64Geocell string that represents this long.
	 *
	 * @param geocell
	 * @return
	 */
	public static String toString(final long geocell) {
		return GeostringConv.toGeostring(geocell);
	}

	/**
	 * Return the corresponding long that a B64Geocell string represents.
	 *
	 * @param geostring
	 * @return
	 */
	public static long fromString(final String geostring) {
		return GeostringConv.fromGeostring(geostring);
	}
}

