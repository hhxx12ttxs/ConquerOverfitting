package model.MARK_II;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Provides implementation for running the spatial learning algorithm on a
 * Region. This class provides methods that simulate brain activity within
 * Regions of the Neocortex.
 *
 * @author Quinn Liu (quinnliu@vt.edu)
 * @version July 29, 2013
 */
public class SpatialPooler extends Pooler {
    private Set<Column> activeColumns;
    private Set<ColumnPosition> activeColumnPositions;

    public static float MINIMUM_COLUMN_FIRING_RATE = 0.01f;

    public SpatialPooler(Region region) {
	if (region == null) {
	    throw new IllegalArgumentException(
		    "region in SpatialPooler class constructor cannot be null");
	}
	super.region = region;
	this.activeColumns = new HashSet<Column>();
	this.activeColumnPositions = new HashSet<ColumnPosition>();
    }

    /**
     * This method recomputes all Column states within this functional Region.
     * Through local inhibition, only a sparse set of Columns become active to
     * represent the current 2D array of sensory data or a lower Region's
     * output.
     *
     * @return A sparse set of active Columns within this Region.
     */
    public Set<Column> performSpatialPoolingOnRegion() {
	Column[][] columns = this.region.getColumns();
	for (int x = 0; x < columns.length; x++) {
	    for (int y = 0; y < columns[0].length; y++) {
		this.computeColumnOverlapScore(columns[x][y]);
	    }
	}

	// a sparse set of Columns become active after local inhibition
	this.computeActiveColumnsOfRegion();

	// // simulate learning by boosting specific Synapses
	this.regionLearnOneTimeStep();

	return this.activeColumns;
    }

    public Set<Column> performSpatialPoolingOnRegionWithoutInhibitionRadiusUpdate() {
	Column[][] columns = this.region.getColumns();
	for (int x = 0; x < columns.length; x++) {
	    for (int y = 0; y < columns[0].length; y++) {
		this.computeColumnOverlapScore(columns[x][y]);
	    }
	}

	// a sparse set of Columns become active after local inhibition
	this.computeActiveColumnsOfRegion();

	// // simulate learning by boosting specific Synapses
	this.regionLearnOneTimeStepWithoutInhitionRadiusUpdate();

	return this.activeColumns;
    }

    /**
     * If only the column positions computed by spatial pooling are needed use
     * this method to return a set of just the column positions that were active
     * in the most recent iteration of spatial pooling. For example instead of
     * using:
     *
     * Set<Column> columnActivity =
     * spatialPooler.performSpatialPoolingOnRegion();
     *
     * Now use:
     *
     * spatialPooler.performSpatialPoolingOnRegion();
     * Set<ColumnPosition> columnActivity = this.spatialPooler.getActiveColumnPositions();
     */
    public Set<ColumnPosition> getActiveColumnPositions() {
	return this.activeColumnPositions;
    }

    public Set<Column> getActiveColumns() {
	return this.activeColumns;
    }

    /**
     * The overlapScore for each Column is the number of Synapses connected to
     * Cells with active inputs multiplied by that Columns's boostValue. If a
     * Column's overlapScore is below minOverlap, that Column's overlapScore is
     * set to 0.
     */
    void computeColumnOverlapScore(Column column) {
	if (column == null) {
	    throw new IllegalArgumentException(
		    "the Column in SpatialPooler method computeColumnOverlapScore cannot be null");
	}
	int newOverlapScore = column.getProximalSegment()
		.getNumberOfActiveSynapses();

	// compute minimumOverlapScore assuming all proximalSegments are
	// connected to the same number of synapses
	Column[][] columns = this.region.getColumns();
	int regionMinimumOverlapScore = this.region.getMinimumOverlapScore();
	if (newOverlapScore < regionMinimumOverlapScore) {
	    newOverlapScore = 0;
	} else {
	    newOverlapScore = (int) (newOverlapScore * column.getBoostValue());
	}
	column.setOverlapScore(newOverlapScore);
    }

    /**
     * This method is called by performSpatialPooling and computes the
     * activeColumns(t)-the list of Columns that win due to the bottom-up input
     * at time t.
     */
    void computeActiveColumnsOfRegion() {
	// remove old active columns from last time spatial pooling was called
	this.activeColumns.clear();
	this.activeColumnPositions.clear();

	Column[][] columns = this.region.getColumns();
	for (int x = 0; x < columns.length; x++) {
	    for (int y = 0; y < columns[0].length; y++) {
		columns[x][y].setActiveState(false);
		this.updateNeighborColumns(x, y);

		// necessary for calculating kthScoreOfColumns
		List<ColumnPosition> neighborColumnPositions = new ArrayList<ColumnPosition>();
		neighborColumnPositions = columns[x][y].getNeighborColumns();
		List<Column> neighborColumns = new ArrayList<Column>();
		for (ColumnPosition columnPosition : neighborColumnPositions) {
		    neighborColumns
			    .add(columns[columnPosition.getX()][columnPosition
				    .getY()]);
		}

		int minimumLocalOverlapScore = this.kthScoreOfColumns(
			neighborColumns, this.region.getDesiredLocalActivity());

		// more than (this.region.desiredLocalActivity) number of
		// columns can become active since it is applied to each
		// Column object's neighborColumns
		if (columns[x][y].getOverlapScore() > 0
			&& columns[x][y].getOverlapScore() >= minimumLocalOverlapScore) {
		    columns[x][y].setActiveState(true);

		    this.addActiveColumn(columns[x][y]);
		    this.activeColumnPositions.add(new ColumnPosition(x, y));
		}
	    }
	}
    }

    /**
     * This method models spike-timing dependent plasticity. This is also known
     * as Hebb's Rule. The inhibitionRadius for the Region is also computed and
     * updated here.
     */
    void regionLearnOneTimeStep() {
	this.modelLongTermPotentiationAndDepression();

	this.boostSynapsesBasedOnActiveAndOverlapDutyCycle();

	this.region
		.setInhibitionRadius((int) averageReceptiveFieldSizeOfRegion());
    }

    void regionLearnOneTimeStepWithoutInhitionRadiusUpdate() {
	this.modelLongTermPotentiationAndDepression();

	this.boostSynapsesBasedOnActiveAndOverlapDutyCycle();
    }

    void modelLongTermPotentiationAndDepression() {
	Column[][] columns = this.region.getColumns();

	if (super.getLearningState()) {
	    for (int x = 0; x < columns.length; x++) {
		for (int y = 0; y < columns[0].length; y++) {
		    if (columns[x][y].getActiveState()) {
			// increase and decrease of proximal segment synapses
			// based on each Synapses's activeState
			Set<Synapse<Cell>> synapses = columns[x][y]
				.getProximalSegment().getSynapses();
			for (Synapse<Cell> synapse : synapses) {
			    if (synapse.getConnectedCell() != null
				    && synapse.getConnectedCell()
					    .getActiveState()) {
				// model long term potentiation
				synapse.increasePermanence();
			    } else {
				// model long term depression
				synapse.decreasePermanence();
			    }
			}
		    }
		}
	    }
	}
    }

    void boostSynapsesBasedOnActiveAndOverlapDutyCycle() {
	Column[][] columns = this.region.getColumns();

	for (int x = 0; x < columns.length; x++) {
	    for (int y = 0; y < columns[0].length; y++) {
		if (columns[x][y].getActiveState()) {
		    // increase and decrease of proximal Segment Synapses based
		    // on each Synapses's activeState
		    // columns[x][y].performBoosting();

		    // 2 methods to help a Column's proximal Segment
		    // Synapses learn connections:
		    // 1) If activeDutyCycle(measures winning rate) is too low.
		    // The overall boost value of the Columns is increased.
		    // 2) If overlapDutyCycle(measures connected Synapses with
		    // inputs) is too low, the permanence values of the
		    // Column's Synapses are boosted.

		    // neighborColumns are already up to date.
		    List<ColumnPosition> neighborColumnPositions = columns[x][y]
			    .getNeighborColumns();

		    List<Column> neighborColumns = new ArrayList<Column>();
		    for (ColumnPosition columnPosition : neighborColumnPositions) {
			// add the Column object to neighborColumns
			neighborColumns
				.add(columns[columnPosition.getX()][columnPosition
					.getY()]);
		    }

		    float maximumActiveDutyCycle = this.region
			    .maximumActiveDutyCycle(neighborColumns);
		    if (maximumActiveDutyCycle == 0) {
			maximumActiveDutyCycle = 0.1f;
		    }

		    // neighborColumns are no longer necessary for calculations
		    // in this time step
		    columns[x][y].clearNeighborColumns();

		    // minDutyCycle represents the minimum desired firing rate
		    // for a Column(number of times it becomes active over some
		    // number of iterations).
		    // If a Column's firing rate falls below this value, it will
		    // be boosted.
		    float minimumActiveDutyCycle = this.MINIMUM_COLUMN_FIRING_RATE
			    * maximumActiveDutyCycle;

		    // 1) boost if activeDutyCycle is too low
		    columns[x][y].updateActiveDutyCycle();

		    columns[x][y].setBoostValue(columns[x][y]
			    .boostFunction(minimumActiveDutyCycle));

		    // 2) boost if overlapDutyCycle is too low
		    this.updateOverlapDutyCycle(x, y);
		    if (columns[x][y].getOverlapDutyCycle() < minimumActiveDutyCycle
			    && this.getLearningState()) {
			columns[x][y]
				.increaseProximalSegmentSynapsePermanences(1);
		    }
		}
	    }
	}
    }

    /**
     * Adds all Columns within inhitionRadius of the parameter Column to the
     * neighborColumns field within the parameter Column.
     *
     * @param columnXAxis
     *            position of Column within Region along x-axis
     * @param columnYAxis
     *            position of Column within Region along y-axis
     */
    void updateNeighborColumns(int columnXAxis, int columnYAxis) {
	if (columnXAxis < 0 || columnXAxis > this.region.getXAxisLength()
		|| columnYAxis < 0
		|| columnYAxis > this.region.getYAxisLength()) {
	    throw new IllegalArgumentException(
		    "the Column being updated by the updateNeighborColumns method"
			    + "in SpatialPooler class does not exist within the Region");
	}
	int localInhibitionRadius = this.region.getInhibitionRadius();
	assert (localInhibitionRadius >= 0);

	// forced inhibition of adjacent Columns
	int xInitial = Math.max(0, columnXAxis - localInhibitionRadius);
	int yInitial = Math.max(0, columnYAxis - localInhibitionRadius);

	// System.out.println("xInitial, yInitial: " + xInitial + ", " +
	// yInitial);
	int xFinal = Math.min(this.region.getXAxisLength(), columnXAxis
		+ localInhibitionRadius);
	int yFinal = Math.min(this.region.getYAxisLength(), columnYAxis
		+ localInhibitionRadius);

	// to allow double for loop to reach end portion of this.allColumns
	xFinal = Math.min(this.region.getXAxisLength(), xFinal + 1);
	yFinal = Math.min(this.region.getYAxisLength(), yFinal + 1);
	// System.out.println("xFinal, yFinal: " + xFinal + ", " + yFinal);

	Column[][] columns = this.region.getColumns();

	if (columns[columnXAxis][columnYAxis].getNeighborColumns().size() != 0) {
	    // remove neighbors of Column computed with old inhibitionRadius
	    columns[columnXAxis][columnYAxis].clearNeighborColumns();
	}

	for (int columnIndex = xInitial; columnIndex < xFinal; columnIndex++) {
	    for (int rowIndex = yInitial; rowIndex < yFinal; rowIndex++) {
		if (columnIndex == columnXAxis && rowIndex == columnYAxis) {
		} else {
		    Column newColumn = columns[columnIndex][rowIndex];
		    if (newColumn != null) {
			columns[columnXAxis][columnYAxis].addNeighborColumns(new ColumnPosition(columnIndex, rowIndex));
		    }
		}
	    }
	}
    }

    /**
     * @param neighborColumns
     * @param desiredLocalActivity
     * @return the kth highest overlapScore value of a Column object within the
     *         neighborColumns list.
     */
    int kthScoreOfColumns(List<Column> neighborColumns,
	    int desiredLocalActivity) {
	if (neighborColumns == null) {
	    throw new IllegalArgumentException(
		    "neighborColumns in SpatialPooler method kthScoreOfColumns cannot be null");
	}
	// TreeSet data structures' elements are automatically sorted.
	Set<Integer> overlapScores = new TreeSet<Integer>();
	for (Column column : neighborColumns) {
	    overlapScores.add(column.getOverlapScore());
	}

	// if invalid or no local activity is desired, it is changed so that the
	// highest overlapScore is returned.
	if (desiredLocalActivity <= 0) {
	    throw new IllegalStateException(
		    "desiredLocalActivity cannot be <= 0");
	}

	// k is the index of the overlapScore to be returned. The overlapScore
	// is the score at position k(counting from the top) of all
	// overlapScores when arranged from smallest to greatest.
	int k = Math.max(0, overlapScores.size() - desiredLocalActivity);
    if(overlapScores.size() > k)
	    return (Integer) overlapScores.toArray()[k];
    else
        return 0;
    }

    /**
     * Returns the radius of the average connected receptive field size of all
     * the Columns. The connected receptive field size of a Column includes only
     * the Column's connected Synapses.
     *
     * @return The average connected receptive field size.
     */
    double averageReceptiveFieldSizeOfRegion() {
	double regionAverageReceptiveField = 0.0;

	// for each column
	Column[][] columns = this.region.getColumns();
	for (int x = 0; x < columns.length; x++) {
	    for (int y = 0; y < columns[0].length; y++) {

		// get the set of connected synapses
		Set<Synapse<Cell>> connectedSynapes = columns[x][y]
			.getProximalSegment().getConnectedSynapses();

		Dimension bottomLayerDimensions = this.region
			.getBottomLayerXYAxisLength();

		// get the column position relative to the input layer
		double xRatio = bottomLayerDimensions.width
			/ this.region.getXAxisLength();
		double columnX = xRatio / 2 + x * xRatio;
		double yRatio = bottomLayerDimensions.height
			/ this.region.getYAxisLength();
		double columnY = yRatio / 2 + y * yRatio;

		double totalSynapseDistanceFromOriginColumn = 0.0;
		// iterates over every connected Synapses and sums the
		// distances from it's origin Column to determine the
		// average receptive field for this Column
		for (Synapse<?> connectedSynapse : connectedSynapes) {

		    double dx = Math.abs(columnX
			    - connectedSynapse.getCellXPosition());
		    double dy = Math.abs(columnY
			    - connectedSynapse.getCellYPosition());
		    double connectedSynapseDistance = Math.sqrt(dx * dx + dy
			    * dy);
		    totalSynapseDistanceFromOriginColumn += connectedSynapseDistance;
		}

		double columnAverageReceptiveField = totalSynapseDistanceFromOriginColumn
			/ connectedSynapes.size();
		regionAverageReceptiveField += columnAverageReceptiveField;
	    }
	}

	regionAverageReceptiveField /= this.region.getNumberOfColumns();

	return regionAverageReceptiveField;
    }

    void addActiveColumn(Column activeColumn) {
	if (activeColumn == null) {
	    throw new IllegalArgumentException(
		    "activeColumn in SpatialPooler class method addActiveColumn cannot be null");
	}
	this.activeColumns.add(activeColumn);
    }

    /**
     * Compute a moving average of how often this Column has overlap greater
     * than minimumDutyOverlap. Exponential Moving Average(EMA): St = a * Yt +
     * (1 - a) * St - 1.
     */
    void updateOverlapDutyCycle(int columnXAxis, int columnYAxis) {
	if (columnXAxis < 0 || columnXAxis > this.region.getXAxisLength()
		|| columnYAxis < 0
		|| columnYAxis > this.region.getYAxisLength()) {
	    throw new IllegalArgumentException(
		    "the Column being updated by the updateOverlapDutyCycle method"
			    + "in SpatialPooler does not exist within the Region");
	}
	// Note whenever updateOverlapDutyCycle() is called, the
	// overlapDutyCycle
	// is always decremented less and less but only incremented if the
	// Column's
	// overlapScore was greater than the Region's minimumOverlapScore.
	// Furthermore, the increment applied to overlapDutyCycle is a constant
	// representing the maximum decrement of overlapDutyCycle from initial
	// value 1. Because of this a Column's overlapDutyCycle has a upper
	// bound of 1.
	Column[][] columns = this.region.getColumns();
	float newOverlapDutyCycle = (1.0f - Column.EXPONENTIAL_MOVING_AVERAGE_AlPHA)
		* columns[columnXAxis][columnYAxis].getOverlapDutyCycle();

	if (columns[columnXAxis][columnYAxis].getOverlapScore() > this.region
		.getMinimumOverlapScore()) {
	    newOverlapDutyCycle += Column.EXPONENTIAL_MOVING_AVERAGE_AlPHA;
	}
	columns[columnXAxis][columnYAxis]
		.setOverlapDutyCycle(newOverlapDutyCycle);
    }

    public Region getRegion() {
	return this.region;
    }

    /**
     * @return Return the list of active column positions in the following
     * format: ((0, 0), (1, 1), (2, 2), (2,0))
     */
    public String getActiveColumnPositionsAsString() {
	String listOfActiveColumns = "(";

	int numberOfActiveColumns = this.activeColumnPositions.size();
	for (ColumnPosition columnPosition : this.activeColumnPositions) {
	    listOfActiveColumns += "(" + columnPosition.getX() + ", " +
		    columnPosition.getY() + ")";
	    --numberOfActiveColumns;

	    if (numberOfActiveColumns == 0) {
		// this is the last active column so don't print ", "
	    } else {
		listOfActiveColumns += ", ";
	    }
	}

	listOfActiveColumns += ")";
	return listOfActiveColumns;
    }

    @Override
    public String toString() {
	StringBuilder stringBuilder = new StringBuilder();
	stringBuilder.append("\n===============================");
	stringBuilder.append("\n---SpatialPooler Information---");
	stringBuilder.append("\n     biological region name: ");
	stringBuilder.append(this.region.getBiologicalName());
	stringBuilder.append("\n# of activeColumns produced: ");
	stringBuilder.append(this.activeColumnPositions.size());
	stringBuilder.append("\n===============================");
	String spatialPoolerInformation = stringBuilder.toString();
	return spatialPoolerInformation;
    }
}
