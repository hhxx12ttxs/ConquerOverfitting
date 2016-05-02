<<<<<<< HEAD
/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. OrbisGIS is
 * distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
 *
 *
 *  Team leader Erwan BOCHER, scientific researcher,
 *
 *  User support leader : Gwendall Petit, geomatic engineer.
 *
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * Copyright (C) 2010 Erwan BOCHER, Pierre-Yves FADET, Alexis GUEGANNO, Maxence LAURENT
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 *
 * or contact directly:
 * erwan.bocher _at_ ec-nantes.fr
 * gwendall.petit _at_ ec-nantes.fr
 */
package org.gdms.data.values;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import org.gdms.sql.strategies.IncompatibleTypesException;
import org.grap.model.GeoRaster;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Datatypes must implement this interface in order to the drivers to return
 * that datatype. The implementation can inherit from AbstractValue or must
 * implement equals and hashCode in the way explained at doEquals method javadoc
 */
public interface Value {
	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#and(com.hardcode.gdbms.engine.values.value)
	 *      ;
	 */
	public Value and(Value value) throws IncompatibleTypesException;

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#or(com.hardcode.gdbms.engine.values.value)
	 *      ;
	 */
	public Value or(Value value) throws IncompatibleTypesException;

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#producto(com.hardcode.gdbms.engine.values.value)
	 *      ;
	 */
	public Value producto(Value value) throws IncompatibleTypesException;

	/**
	 * @see com.hardcode.gdbms.engine.instruction.Operations#suma(com.hardcode.gdbms.engine.values.value)
	 *      ;
	 */
	public Value suma(Value value) throws IncompatibleTypesException;

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws IncompatibleTypesException
	 *             DOCUMENT ME!
	 */
	public Value inversa() throws IncompatibleTypesException;

	/**
	 * @see org.gdms.sql.instruction.Operations#equals(org.gdms.data.values.Value)
	 */
	public Value equals(Value value) throws IncompatibleTypesException;

	/**
	 * @see org.gdms.sql.instruction.Operations#notEquals(org.gdms.data.values.Value)
	 */
	public Value notEquals(Value value) throws IncompatibleTypesException;

	/**
	 * @see org.gdms.sql.instruction.Operations#greater(org.gdms.data.values.Value)
	 */
	public Value greater(Value value) throws IncompatibleTypesException;

	/**
	 * @see org.gdms.sql.instruction.Operations#less(org.gdms.data.values.Value)
	 */
	public Value less(Value value) throws IncompatibleTypesException;

	/**
	 * @see org.gdms.sql.instruction.Operations#greaterEqual(org.gdms.data.values.Value)
	 */
	public Value greaterEqual(Value value) throws IncompatibleTypesException;

	/**
	 * @see org.gdms.sql.instruction.Operations#lessEqual(org.gdms.data.values.Value)
	 */
	public Value lessEqual(Value value) throws IncompatibleTypesException;

	/**
	 * @see org.gdms.data.values.Operations#like(org.gdms.data.values.Value)
	 */
	public Value like(Value value) throws IncompatibleTypesException;

	/**
	 * In order to index the tables equals and hashCode must be defined.
	 * AbstractValue overrides these methods by calling doEquals and doHashCode.
	 * Any Value must inherit from abstract Value or override those methods in
	 * the same way.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean doEquals(Object obj);

	/**
	 * The hashCode implementation. Every value with the same semantic
	 * information must return the same int
	 * 
	 * @return integer
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int doHashCode();

	/**
	 * Gets the string representation of the value as it is defined in the
	 * specified ValueWriter
	 * 
	 * @param writer
	 *            Specifies the string representation for the values
	 * 
	 * @return String
	 */
	public String getStringValue(ValueWriter writer);

	/**
	 * Gets the type of the value
	 * 
	 * @return integer
	 */
	public int getType();

	/**
	 * Gets this value represented as an array of bytes
	 * 
	 * @return
	 */
	public byte[] getBytes();

	/**
	 * @return true if this value is null, false otherwise
	 */
	public boolean isNull();

	/**
	 * @return this value if it is a binary value or it can be converted
	 * 
	 * @throws IncompatibleTypesException
	 *             if the value is not of the required type or cannot be
	 *             converted
	 */
	public byte[] getAsBinary() throws IncompatibleTypesException;

	/**
	 * @return this value if it is a boolean value or it can be converted
	 * 
	 * @throws IncompatibleTypesException
	 *             if the value is not of the required type or cannot be
	 *             converted
	 */
	public boolean getAsBoolean() throws IncompatibleTypesException;

	/**
	 * @return this value if it is a date value or it can be converted
	 * 
	 * @throws IncompatibleTypesException
	 *             if the value is not of the required type or cannot be
	 *             converted
	 */
	public Date getAsDate() throws IncompatibleTypesException;

	/**
	 * @return this value if it is a geometry value or it can be converted
	 * 
	 * @throws IncompatibleTypesException
	 *             if the value is not of the required type or cannot be
	 *             converted
	 */
	public Geometry getAsGeometry() throws IncompatibleTypesException;

	/**
	 * @return this value if it is a raster value or it can be converted
	 * 
	 * @throws IncompatibleTypesException
	 *             if the value is not of the required type or cannot be
	 *             converted
	 */
	public GeoRaster getAsRaster();

	/**
	 * @return this value if it is a numeric value or it can be converted
	 * 
	 * @throws IncompatibleTypesException
	 *             if the value is not of the required type or cannot be
	 *             converted
	 */
	public double getAsDouble() throws IncompatibleTypesException;

	/**
	 * @return this value if it is a numeric value or it can be converted
	 * 
	 * @throws IncompatibleTypesException
	 *             if the value is not of the required type or cannot be
	 *             converted
	 */
	public float getAsFloat() throws IncompatibleTypesException;

	/**
	 * @return this value if it is a numeric value or it can be converted
	 * 
	 * @throws IncompatibleTypesException
	 *             if the value is not of the required type or cannot be
	 *             converted
	 */
	public long getAsLong() throws IncompatibleTypesException;

	/**
	 * @return this value if it is a numeric value or it can be converted
	 * 
	 * @throws IncompatibleTypesException
	 *             if the value is not of the required type or cannot be
	 *             converted
	 */
	public byte getAsByte() throws IncompatibleTypesException;

	/**
	 * @return this value if it is a numeric value or it can be converted
	 * 
	 * @throws IncompatibleTypesException
	 *             if the value is not of the required type or cannot be
	 *             converted
	 */
	public short getAsShort() throws IncompatibleTypesException;

	/**
	 * @return this value if it is a numeric value or it can be converted
	 * 
	 * @throws IncompatibleTypesException
	 *             if the value is not of the required type or cannot be
	 *             converted
	 */
	public int getAsInt() throws IncompatibleTypesException;

	/**
	 * @return this value if it is a string value or it can be converted
	 * 
	 * @throws IncompatibleTypesException
	 *             if the value is not of the required type or cannot be
	 *             converted
	 */
	public String getAsString() throws IncompatibleTypesException;

	/**
	 * @return this value if it is a timestamp value or it can be converted
	 * 
	 * @throws IncompatibleTypesException
	 *             if the value is not of the required type or cannot be
	 *             converted
	 */
	public Timestamp getAsTimestamp() throws IncompatibleTypesException;

	/**
	 * @return this value if it is a time value or it can be converted
	 * 
	 * @throws IncompatibleTypesException
	 *             if the value is not of the required type or cannot be
	 *             converted
	 */
	public Time getAsTime() throws IncompatibleTypesException;

	/**
	 * @return this value if it is a value collection
	 * 
	 * @throws IncompatibleTypesException
	 *             if the value is not of the required type or cannot be
	 *             converted
	 */
	public ValueCollection getAsValueCollection()
			throws IncompatibleTypesException;

	/**
	 * Tries to make a conversion to the specified type.
	 * 
	 * @param typeCode
	 * @return The converted type
	 * @throws IncompatibleTypesException
	 *             If the value cannot be converted
	 */
	public Value toType(int typeCode) throws IncompatibleTypesException;

}
=======
package ncsa.d2k.modules.projects.pgroves.geostat;

import ncsa.d2k.modules.core.datatype.table.*;
import ncsa.d2k.modules.core.datatype.table.util.*;
import ncsa.d2k.modules.core.datatype.table.basic.*;
import java.util.*;


/**
 * a transformation that maps any type of column's data into 
 * rankings based on that data type's default comparison scheme.
 * The generated rankings are presented as type double instead of
 * int so that applying the transformation to other tables and
 * also the untransformation can handle values that are not 
 * exactly the same as those of the data set an instance is
 * constructed from. Such cases will be handled by linear interpolation
 * between the nearest value above and nearest value below in
 * the source data set for numeric columns. This does not
 * exactly adhere to the principle of replacing the magnitude
 * of a value with its ordering, but is considered the lesser
 * of evils because it retains variability during the 
 * reverse transformation process.
 *
 * <p>Other column types
 * will be resolved by always mapping to the 'lesser' value
 * (eg. the nearest string in alphabetical order that is less
 * than the string in question, when the native data type
 * is String).
 *
 * <p>In the case of a tie in the forward transformation 
 * (eg there are multiple raw data values of zero), the largest
 * rank (furthest from zero) will be returned.
 *
 * @author pgroves
 * @date 03/29/04
 */

public class RankingTransformation implements ReversibleTransformation,
	Cloneable, java.io.Serializable{

	/**
	 * This table contains the sorted values of each of columns
	 * that are being transformed. 	 */
	MutableTableImpl sortedColumns;

	/**
	 * This holds the sorted order of the original table.
	 * So, if origOrder[i][k] == j, then the value at
	 * column 'i', row 'j' in the original table will
	 * be at position 'k' in the appropriate column when sorted
	 */
	int[][] origOrder;
	
	/**
	 * the columns of the original input table that were transformed
	 * (and are present in <code>sortedColumns</code>
	 */
	int[] transCols;

	/**
	 * Initiallizes the transformation using the sourceTable to generate
	 * rankings of only those columns specified in transformColumns.
	 *
	 * @param sourceTable a table to base the transformation on
	 * @param transformColumns the column indices of those columns
	 * 	that should be transformed
	 */
	public RankingTransformation(Table sourceTable, int[] transformColumns){

		transCols = transformColumns;
		this.initSourceData(sourceTable);
	}

	/**
	 * sets up <code>sortedColumns</code>
	 */
	protected void initSourceData(Table sourceTable){
		int i, j, k;
		int numTransCols = this.transCols.length;
		//we're going to put the smallest and largest
		//possible values in each column, as well
		int numRows = sourceTable.getNumRows();

		Column[] cols = new Column[numTransCols];
		this.sortedColumns = new MutableTableImpl(cols);				

		int[] columnsToSortBy = new int[1];
		this.origOrder = new int[numTransCols][];
		for(i = 0; i < numTransCols; i++){
			//System.out.println("SortedColumn:"+i);
			cols[i] = ColumnUtilities.metaColumnCopy(sourceTable, 
					this.transCols[i], numRows);
			columnsToSortBy[0] = this.transCols[i];
			origOrder[i] = TableUtilities.multiSortIndex(sourceTable, 
					columnsToSortBy);
			for(j = 0; j < numRows; j++){
				
				TableUtilities.setValue(sourceTable, origOrder[i][j], 
						this.transCols[i], this.sortedColumns, j, i);
				//cols[i].setDouble(sourceTable.getDouble(j, this.transCols[i]), 
				//		origOrder[i][j]);
			}
			/*
			for(j = 0; j < numRows; j++){
				System.out.print(sortedColumns.getString(j, i) + ", ");
			}
			System.out.println();
			*/
		}
		
		
	}

	
	public boolean transform(MutableTable table){
		
		int i, j, k;
		int numTransCols = this.transCols.length;
		int numRows = table.getNumRows();

		double rank;
		
		for(i = 0; i < numTransCols; i++){
			for(j = 0; j < numRows; j++){
				try{
					rank = mapForward(table, j, i);
					table.setDouble(rank, j, this.transCols[i]);
				}catch(Exception e){
					e.printStackTrace();
					System.out.println("RankingTransformation Failure");
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * finds the 'rank' value of a value in a table, or the forward
	 * transformation. this class's forward transformation is to
	 * do a binary search of the values that an instance of
	 * this class was constructed with (passed into the constructor),
	 * and to return the smallest rank/orderIndex that is equivalent.
	 * If the exact value is not found, a pseudo-ranking (not a whole
	 * number) will be returned that is a linear interpolation of
	 * the rankings of the values immediately above and below it. If
	 * this is not possible (b/c the data is not a numeric type), the
	 * largest rank for a value that is not greater than the value being
	 * looked up is used.
	 *
	 * <p> The rank of the lowest value in the source table is 1.
	 *
	 * <p> If the value being looked up is less than the smallest
	 * value in the source table, a rank of zero will always
	 * be returned (no interpolation under any circumstances)</p>
	 *
	 * @param tbl the table that contains the value to look up
	 * @param row the row in the table where the value is
	 * @param transColIdx the index into the <code>transCol</code>
	 * set of columns that holds the value
	 *
	 * @return a ranking as a double. this value may not be a whole
	 * number if the exact value if not found in the source data table
	 * and interpolation is possible
	 */
	public double mapForward(Table tbl, int row, int transColIdx){  
		
		//an index of the largest value that does not exceed the 
		//index of the value being looked up, holds the answer to 
		//the binary search
		int largestNotExceeding;
		
		int numSrcRows = this.sortedColumns.getNumRows();
		int leftend = 0;
		int rightend = numSrcRows;
		largestNotExceeding = binarySearch(leftend, rightend,
				tbl, row, transColIdx);
		
		if(largestNotExceeding == -1){
			return 0.0;
		}
		if(largestNotExceeding >= (numSrcRows - 1)){
			return (double)(largestNotExceeding + 1);
		}
		if(!tbl.isColumnNumeric(this.transCols[transColIdx])){
			return (double)largestNotExceeding + 1.0;
		}else{
			int justLargerIdx = largestNotExceeding + 1;
			double justLargerVal = this.sortedColumns.getDouble(justLargerIdx, 
					transColIdx);
			double justSmallerVal = this.sortedColumns.getDouble(
					largestNotExceeding, transColIdx);
			double lookupVal = tbl.getDouble(row, this.transCols[transColIdx]);
			double fracDiff = (lookupVal - justSmallerVal) / (justLargerVal -
					justSmallerVal);
			double pRank = ((double)largestNotExceeding) + fracDiff + 1;
			return pRank;
		}
	}

	/**
	 * finds the index of the largest value that the value of the ranking is
	 * not greater than the value being looked up. only searches
	 * for the value between the given 'ends'.
	 * */
	protected int binarySearch(int leftEnd, int rightEnd, Table tbl, int row,
			int transColIdx){
		//i don't have time to test a binary search right now, so
		//it's gonna have to be a linear lookup for the time being.
		//the untested binary search code is at the end of this method
		//if you're dying for the speed increase and want to try it
		int rank = leftEnd;
		int comparison = -1;
		/*for(rank = leftEnd; rank < rightEnd; rank++){
			comparison = TableUtilities.compareValues(tbl, row, 
				this.transCols[transColIdx], sortedColumns, rank, 
				transColIdx);
			System.out.println("rank: " + rank + ", lookupVal: " + 
					tbl.getString(row, 
						transCols[transColIdx]) + ", searchVal: " +
						sortedColumns.getString(rank, transColIdx) +
						", comp = " + comparison);
			if(comparison < 0){
				break;
			}
		}
		*/
		while(true){
			comparison = TableUtilities.compareValues(tbl, row, 
				this.transCols[transColIdx], sortedColumns, rank, 
				transColIdx);
			/*System.out.println("rank: " + rank + ", lookupVal: " + 
					tbl.getString(row, 
						transCols[transColIdx]) + ", searchVal: " +
						sortedColumns.getString(rank, transColIdx) +
						", comp = " + comparison);
						*/
			if((comparison < 0) || (rank >= (rightEnd - 1))){
				rank--;
				break;
			}else{
				rank++;
			}
		}

		if((rank == -1) && (comparison < 0)){
			System.out.println("Below Zero:" + tbl.getString(row, 
						transCols[transColIdx]));
			rank = -1;
		}
		if(rank == (rightEnd - 2)){
			if(comparison > 0){
				System.out.println("Past End:" + tbl.getString(row, 
						transCols[transColIdx]));

				rank = rightEnd;
			}else if(comparison == 0){
				rank = rightEnd -1;
			}else{
				rank = rightEnd - 2;
			}
		}
		return rank;
		
		/*int midpoint = (int) (((double)(rightEnd - leftEnd)) / 2.0);
		midpoint += leftEnd;
		
		int comparison = TableUtilities.compareValues(tbl, row, 
				this.transCols[transColIdx], sortedColumns, midpoint, 
				transColIdx);
		
		if(comparison > 0){
			if(midpoint == (sortedColumns.getNumRows() - 1)){
				//got to the end without finding it
				return (rightEnd + 1);
			}
			comparison = TableUtilities.compareValues(tbl, row, 
				this.transCols[transColIdx], sortedColumns, midpoint + 1, 
				transColIdx);

			if(comparison < 0){
				//the value is not in the lookup table, return what we have
				return midpoint;
			}
					
			return binarySearch(midpoint, rightEnd, tbl, row, transColIdx);
			
		}else if(comparison < 0){
			if(0 == midpoint){
			//we're at the very beginning and the value is still too large
				return -1;
			}
			return binarySearch(leftEnd, midpoint, tbl, row, transColIdx);
		}

		//comparison equals zero, which means we found it. now find
		//the largest rank for which it is still true. 
		int rank = midpoint;
		while(0 == TableUtilities.compareValues(tbl, row, 
				this.transCols[transColIdx], sortedColumns, rank, 
				transColIdx){
			rank++;
		}
		rank--;
		return rank;
		*/
	}

	
	public boolean untransform(MutableTable table){
		int numTransCols = this.transCols.length;
		int numSortedRows = this.sortedColumns.getNumRows();
		int numRows = table.getNumRows();
		
		int i, j, k;
		double rank, val;
		for(i = 0; i < numTransCols; i++){
			try{
				untransformCol(table, this.transCols[i], i);
			}catch(Exception e){
				e.printStackTrace();
				System.out.println("RankingTransformation: Reverse " +
						"Transform failed at column "+ transCols[i]);
				System.out.println(e);
				return false;
			}
		}					
		if(table instanceof PredictionTable){
			PredictionTable pt = (PredictionTable)table;
			int[] predSet = pt.getPredictionSet();
			if(predSet == null){
				//no predictions, nothing to do
				return true;
			}
			int outCol;
			int predCol;
			int transCol = 0;
			for(i = 0; i < predSet.length; i++){
				predCol = predSet[i];
				outCol = pt.getOutputFeatures()[i];
				
				for(j = 0; j < this.transCols.length; j++){
					if(this.transCols[j] == outCol){
							transCol = j;
						}
				}
				try{
					untransformCol(table, predCol, transCol);
				}catch(Exception e){
					System.out.println("Reverse Transformation of Predictions" +
						" Failed. Continuing Anyway (RankingTransformation)");
				}
			}
			
		}
		return true;
	}

	/**
	 * does the reverse transform on a single column.
	 * 
	 * @param table the table that contains a column to transform
	 * @param tblCol which column to transform
	 * @param transCol which of the transformation columns (in 
	 * 	sortedColumns) to base the reverse transformation on
	 */
	private void untransformCol(MutableTable table, int tblCol, int transCol){
		double rank, val;
		int numRows = table.getNumRows();
		for(int j = 0; j < numRows; j++){
			rank = table.getDouble(j, tblCol);
			if(this.sortedColumns.isColumnNumeric(transCol)){
				val = reverseMap(rank, transCol);
				table.setDouble(val, j, tblCol);
			}else{
				//if not numeric, just return the value at the
				//index that is the floor of the rank
				int index = (int)rank - 1;

				//set ranks outside of the range to the max and min
				if(index < 0)
					index = 0;
				if(index >= sortedColumns.getNumRows())
					index = sortedColumns.getNumRows() - 1;
				
				TableUtilities.setValue(sortedColumns, index, transCol,
					table, j, tblCol);
			}
		}
	}

	/**
	 * given a rank, returns a value corresponding to the original
	 * data set. only works when the original value was numeric,
	 * as an interpolation will be done if the rank is not a whole
	 * number
	 */
	protected double reverseMap(double rank, int transColIdx){
		int justUnderIdx = (int)(rank - 1.0);
		int justOverIdx = justUnderIdx + 1;

		int maxIdx = this.sortedColumns.getNumRows() - 1;
		if(justUnderIdx < 0){
			return this.sortedColumns.getDouble(0, transColIdx);
		}
		if(justUnderIdx >= maxIdx){
			return this.sortedColumns.getDouble(maxIdx, transColIdx);
		}
		double justUnderVal = this.sortedColumns.getDouble(justUnderIdx, 
				transColIdx);
		double justOverVal = this.sortedColumns.getDouble(justOverIdx, 
				transColIdx);
		double diffFrac = rank - ((double)justUnderIdx) - 1;
		double val = justUnderVal + diffFrac * (justOverVal - justUnderVal);
		return val;
	}

}



>>>>>>> 76aa07461566a5976980e6696204781271955163
