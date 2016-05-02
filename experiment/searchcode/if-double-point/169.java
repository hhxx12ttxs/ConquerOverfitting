<<<<<<< HEAD
package ncsa.d2k.modules.core.optimize.random;

import ncsa.d2k.modules.core.datatype.parameter.*;
import ncsa.d2k.modules.core.datatype.parameter.impl.*;
import ncsa.d2k.modules.core.datatype.table.basic.*;

//import ncsa.d2k.modules.core.datatype.table.continuous.*;
import ncsa.d2k.modules.core.datatype.table.*;
import java.util.Random;
import ncsa.d2k.core.modules.ComputeModule;
import ncsa.d2k.core.modules.PropertyDescription;
import java.beans.PropertyVetoException;

public class UniformSampling
    extends ComputeModule
    implements java.io.Serializable {

  public PropertyDescription[] getPropertiesDescriptions() {

    PropertyDescription[] pds = new PropertyDescription[6];

    pds[0] = new PropertyDescription(
        "objectiveScoreOutputFeatureNumber",
        "Objective Score Output Feature Number",
        "Selects which example output feature is used to denote the objective score of the Parameter Point.  ");

    pds[1] = new PropertyDescription(
        "objectiveScoreDirection",
        "Objective Score Direction",
        "Determines whether the objective score is to be minimized (-1) or maximized (1).  ");

    pds[2] = new PropertyDescription(
        "stopObjectiveScoreThreshold",
        "Stop Utility Threshold",
        "Optimization halts when an example is generated with an objective score which is greater or less than threshold depending on Objective Score Direction.  ");

    pds[3] = new PropertyDescription(
        "maxNumIterations",
        "Maximum Number of Iterations",
        "Optimization halts when this limit on the number of iterations is exceeded.  ");

    pds[4] = new PropertyDescription(
        "randomSeed",
        "Random Number Generator Initial Seed",
        "This integer is use to seed the random number generator which is used to select points in parameter space.  ");

    pds[5] = new PropertyDescription(
        "trace",
        "Trace",
        "Report extra information during execution to trace the modules execution.  ");

    return pds;
  }

  private int ObjectiveScoreOutputFeatureNumber = 1;
  public void setObjectiveScoreOutputFeatureNumber(int value) throws
      PropertyVetoException {
    if (value < 1) {
      throw new PropertyVetoException(" < 1", null);
    }
    this.ObjectiveScoreOutputFeatureNumber = value;
  }

  public int getObjectiveScoreOutputFeatureNumber() {
    return this.ObjectiveScoreOutputFeatureNumber;
  }

  private int ObjectiveScoreDirection = -1;
  public void setObjectiveScoreDirection(int value) throws
      PropertyVetoException {
    if (! ( (value == -1) || (value == 1))) {
      throw new PropertyVetoException(" must be -1 or 1", null);
    }
    this.ObjectiveScoreDirection = value;
  }

  public int getObjectiveScoreDirection() {
    return this.ObjectiveScoreDirection;
  }

  private double StopObjectiveScoreThreshold = 0.0;
  public void setStopObjectiveScoreThreshold(double value) {
    this.StopObjectiveScoreThreshold = value;
  }

  public double getStopObjectiveScoreThreshold() {
    return this.StopObjectiveScoreThreshold;
  }

  private int MaxNumIterations = 10;
  public void setMaxNumIterations(int value) throws PropertyVetoException {
    if (value < 1) {
      throw new PropertyVetoException(" < 1", null);
    }
    this.MaxNumIterations = value;
  }

  public int getMaxNumIterations() {
    return this.MaxNumIterations;
  }

  private int RandomSeed = 123;
  public void setRandomSeed(int value) {
    this.RandomSeed = value;
  }

  public int getRandomSeed() {
    return this.RandomSeed;
  }

  private boolean Trace = false;
  public void setTrace(boolean value) {
    this.Trace = value;
  }

  public boolean getTrace() {
    return this.Trace;
  }

  public String getModuleName() {
    return "Random Optimizer";
  }

  public String getModuleInfo() {
    return "This module implements a simple random sampling optimizer which selects points according to a uniform " +
           "distribution over the parameter space.  Every point in the space has equal likelihood of being selected.  ";

  }

  public String getInputName(int i) {
    switch (i) {
      case 0:
        return "Control Parameter Space";
      case 1:
        return "Example";
    }
    return "";
  }

  public String getInputInfo(int i) {
    switch (i) {
      case 0:
        return "The Control Parameter Space to search";
      case 1:
        return
            "The Example created by combining the Parameter Point and the objective scores";
    }
    return "";
  }

  public String[] getInputTypes() {
    String[] in = {
        "ncsa.d2k.modules.core.datatype.parameter.ParameterSpace",
        "ncsa.d2k.modules.core.datatype.table.Example"
    };
    return in;
  }

  public String getOutputName(int i) {
    switch (i) {
      case 0:
        return "Parameter Point";
      case 1:
        return "Optimal Example Table";
      case 2:
        return "Complete Example Table";
    }
    return "";
  }

  public String getOutputInfo(int i) {
    switch (i) {
      case 0:
        return "The next Parameter Point selected for evaluation";
      case 1:
        return "An example table consisting of only the Optimal Example(s)";
      case 2:
        return
            "An example table consisting of all Examples generated during optimization";
    }
    return "";
  }

  public String[] getOutputTypes() {
    String[] out = {
        "ncsa.d2k.modules.core.datatype.parameter.ParameterPoint",
        "ncsa.d2k.modules.core.datatype.table.ExampleTable",
        "ncsa.d2k.modules.core.datatype.table.ExampleTable"
    };
    return out;
  }

  private boolean InitialExecution = true;
  private Random randomNumberGenerator = null;

  public void beginExecution() {

    InitialExecution = true;
    ExampleData = null;
    NumExamples = 0;

    if (ObjectiveScoreDirection == 1) {
      BestUtility = Double.NEGATIVE_INFINITY;
    }
    else {
      BestUtility = Double.POSITIVE_INFINITY;
    }
    BestExampleIndex = Integer.MIN_VALUE;
    randomNumberGenerator = new Random(RandomSeed);
  }

  public boolean isReady() {
    boolean value = false;

    if (InitialExecution) {
      value = (this.getFlags()[0] > 0);
    }
    else {
      value = (this.getFlags()[1] > 0);
    }

    return value;
  }

  int NumExperimentsCompleted = 0;

  ParameterSpace BiasSpace;
  //String []     BiasSpaceDimensionNames;
  double[] Bias;
  //int           BiasSpaceNumDimensions;
  double[][][] InitialExampleSet;
  int InitialNumExamples;
  //ContinuousDoubleExampleTable ExampleSet;
  //ExampleTable ExampleSet;

  int NumExamples;
  double[][] ExampleData;
  int [] inputs;
  int [] outputs;
  String [] inputNames;
  String [] outputNames;

  double BestUtility = 0;
  int BestExampleIndex = Integer.MIN_VALUE;

  public void doit() {

    if (InitialExecution) {
      BiasSpace = (ParameterSpace)this.pullInput(0);
      InitialExecution = false;
    }
    else {

      Example example = (Example)this.pullInput(1);
      if (ExampleData == null) {
	    NumExamples = 0;
		ExampleData = new double [BiasSpace.getNumParameters()+((ExampleTable)example.getTable()).getNumOutputFeatures()][MaxNumIterations];
		inputs = new int [BiasSpace.getNumParameters()];
		outputs = new int [((ExampleTable)example.getTable()).getNumOutputFeatures()];
		int index = 0;
		for (; index < inputs.length ; index++) inputs[index] = index;
		for (int i = 0 ; i < outputs.length ; index++, i++) outputs[i] = index;

		inputNames = new String[BiasSpace.getNumParameters()];
		for (int i = 0; i < BiasSpace.getNumParameters(); i++) {
		  inputNames[i] = BiasSpace.getName(i);
		}

		outputNames = new String[((ExampleTable)example.getTable()).getNumOutputFeatures()];
		for (int i = 0; i < ((ExampleTable)example.getTable()).getNumOutputFeatures(); i++) {
		  outputNames[i] = ((ExampleTable)example.getTable()).getOutputName(i);
		}
      }

      // add example to set
      int index = 0;
      for (int i = 0; i < ((ExampleTable)example.getTable()).getNumInputFeatures(); i++) {
        ExampleData[index++][NumExamples] = example.getInputDouble(i);
      }
      for (int i = 0; i < ((ExampleTable)example.getTable()).getNumOutputFeatures(); i++) {
        ExampleData[index++][NumExamples] = example.getOutputDouble(i);
      }
      NumExamples++;

      // update best solution so far
	  int outputFeature2Score = inputs.length + (ObjectiveScoreOutputFeatureNumber-1);
      for (int e = NumExamples - 1; e < NumExamples; e++) {
		double utility = ExampleData [outputFeature2Score][e];
        if (ObjectiveScoreDirection == 1) {
          if (utility > BestUtility) {
            BestUtility = utility;
            BestExampleIndex = e;
          }
        }
        else {
          if (utility < BestUtility) {
            BestUtility = utility;
            BestExampleIndex = e;
          }
        }
      }

    }

    ////////////////////////////
    // test stopping criteria //
    ////////////////////////////

    boolean stop = false;

    if (NumExamples > 0) {
      if ( (ObjectiveScoreDirection == 1) &&
          (BestUtility >= StopObjectiveScoreThreshold))
        stop = true;
      if ( (ObjectiveScoreDirection == -1) &&
          (BestUtility <= StopObjectiveScoreThreshold))
        stop = true;
      if (NumExamples >= MaxNumIterations)
        stop = true;
      if (BiasSpace.getNumParameters() == 0) {
        System.out.println(
            "Halting execution of optimizer after on iteration because numParameters = 0.  ");
        stop = true;
      }
    }

    /////////////////////////////////////////
    // quit when necessary and push result //
    /////////////////////////////////////////
    if (stop) {

      if (Trace) {

        System.out.println("Optimization Completed");
        System.out.println("  Number of Experiments = " + NumExamples);

        System.out.println("NumExamples............ " + NumExamples);
        System.out.println("ObjectiveScoreDirection....... " +
                           ObjectiveScoreDirection);
        System.out.println("BestUtility............ " + BestUtility);
        System.out.println("BestExampleNumber...... " + (BestExampleIndex + 1));
      }

      // add example to set
	  double[][] data = new double[ExampleData.length][1];
	  int index = 0;
	  for (int i = 0; i < ExampleData.length; i++) {
		data[index++][0] = ExampleData[i][BestExampleIndex];
	  }
	  //ANCA: was this.getTable()
	  ExampleTable optimalExampleSet = getTable(data, inputNames, outputNames,
			  inputs, outputs, 1);
	  ExampleTable exampleSet = getTable(ExampleData, inputNames, outputNames,
			  inputs, outputs, NumExamples);
      this.pushOutput(optimalExampleSet, 1);
      this.pushOutput(exampleSet, 2);
      beginExecution();
      return;
    }

    //////////////////////////////////////////////
    // generate next point in bias space to try //
    //////////////////////////////////////////////

    double[] point = new double[BiasSpace.getNumParameters()];

    // use uniform random sampling to constuct point
    for (int d = 0; d < BiasSpace.getNumParameters(); d++) {
      double range = BiasSpace.getMaxValue(d) - BiasSpace.getMinValue(d);

      switch (BiasSpace.getType(d)) {
        case ColumnTypes.DOUBLE:
          point[d] = BiasSpace.getMinValue(d) + range * randomNumberGenerator.nextDouble();
          break;
        case ColumnTypes.FLOAT:
          point[d] = BiasSpace.getMinValue(d) + range * randomNumberGenerator.nextFloat();
          break;
        case ColumnTypes.INTEGER:
          if ( (int) range == 0) {
            point[d] = BiasSpace.getMinValue(d);
          }
          else {
            point[d] = BiasSpace.getMinValue(d) + randomNumberGenerator.nextInt( (int) (range + 1));
          }
          break;
        case ColumnTypes.BOOLEAN:
          if ( (int) range == 0) {
            point[d] = BiasSpace.getMinValue(d);
          }
          else {
            point[d] = BiasSpace.getMinValue(d) + randomNumberGenerator.nextInt( (int) (range + 1));
          }
          break;

      }
    }
	String[] names = new String[BiasSpace.getNumParameters()];
	for (int i = 0; i < BiasSpace.getNumParameters(); i++) {
	  names[i] = BiasSpace.getName(i);
	}
	ParameterPoint parameterPoint = ParameterPointImpl.getParameterPoint(names, point);
    this.pushOutput(parameterPoint, 0);

  }

  /**
   * Given a two d array of doubles, create a table.
   * @param data
   * @return
   */
  static public ExampleTable getTable (double[][] data, String [] inputNames,
			String [] outputNames, int [] inputs, int [] outputs, int count) {
    Column [] cols = new Column[data.length];
	int index = 0;
    for (int i = 0 ; i < inputs.length; i++, index++) {
	  if (data.length != count) {
	    double [] tmp = new double [count];
		System.arraycopy(data[index], 0, tmp, 0, count);
		data[index] = tmp;
	  }
	  cols[index] = new DoubleColumn(data[index]);
	  cols[index].setLabel(inputNames[i]);
	}
	for (int i = 0 ; i < outputs.length; i++, index++) {
		if (data.length != count) {
		  double [] tmp = new double [count];
		  System.arraycopy(data[index], 0, tmp, 0, count);
		  data[index] = tmp;
		}
	  cols[index] = new DoubleColumn(data[index]);
	  cols[index].setLabel(outputNames[i]);
	}
	MutableTable mt = new MutableTableImpl(cols);
	ExampleTable et = mt.toExampleTable();
	et.setInputFeatures(inputs);
	et.setOutputFeatures(outputs);
	return et;
  }
}

=======
/**
 Ported by David Turner from Visilibity, by Karl J. Obermeyer
   
 
 This port undoubtedly introduced a number of bugs (and removed some features).
 
 Bug reports should be directed to the OpenTripPlanner project, unless they 
 can be reproduced in the original VisiLibity.
  
 This program is free software: you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the
 Free Software Foundation, either version 3 of the License, or (at your
 option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.opentripplanner.visibility;

import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisibilityPolygon extends Polygon {
    private static Logger log = LoggerFactory.getLogger(VisibilityPolygon.class);
    Point observer;

    public boolean is_spike(Point observer, Point point1, Point point2, Point point3, double epsilon) {

        return (
        // Make sure observer not colocated with any of the points.
        observer.distance(point1) > epsilon && observer.distance(point2) > epsilon
                && observer.distance(point3) > epsilon
                // Test whether there is a spike with point2 as the tip
                && ((observer.distance(point2) >= observer.distance(point1) && observer
                        .distance(point2) >= observer.distance(point3)) || (observer
                        .distance(point2) <= observer.distance(point1) && observer.distance(point2) <= observer
                        .distance(point3)))
        // && the pike is sufficiently sharp,
        && Math.max(point2.distance(new Ray(observer, point1)),
                point2.distance(new Ray(observer, point3))) <= epsilon);
        // Formerly used
        // Math.abs( Polygon(point1, point2, point3).area() ) < epsilon
    }

    public void chop_spikes_at_back(Point observer, double epsilon) {
        // Eliminate "special case" vertices of the visibility polygon.
        // While the top three vertices form a spike.
        while (vertices.size() >= 3
                && is_spike(observer, vertices.get(vertices.size() - 3),
                        vertices.get(vertices.size() - 2), vertices.get(vertices.size() - 1),
                        epsilon)) {
            vertices.set(vertices.size() - 2, vertices.get(vertices.size() - 1));
            vertices.remove(vertices.size() - 1);
        }
    }

    void chop_spikes_at_wrap_around(Point observer, double epsilon) {
        // Eliminate "special case" vertices of the visibility polygon at
        // wrap-around. While the there's a spike at the wrap-around,
        while (vertices.size() >= 3
                && is_spike(observer, vertices.get(vertices.size() - 2),
                        vertices.get(vertices.size() - 1), vertices.get(0), epsilon)) {
            // Chop off the tip of the spike.
            vertices.remove(vertices.size() - 1);
        }
    }

    void chop_spikes(Point observer, double epsilon) {
        HashSet<Point> spike_tips = new HashSet<Point>();
        ArrayList<Point> vertices_temp = new ArrayList<Point>();
        // Middle point is potentially the tip of a spike
        for (int i = 0; i < vertices.size(); i++)
            if (get(i + 2).distance(new LineSegment(get(i), get(i + 1))) <= epsilon
                    || get(i).distance(new LineSegment(get(i + 1), get(i + 2))) <= epsilon)
                spike_tips.add(get(i + 1));

        for (int i = 0; i < vertices.size(); i++)
            if (!spike_tips.contains(vertices.get(i)))
                vertices_temp.add(vertices.get(i));
        vertices = vertices_temp;
    }

    public VisibilityPolygon(Point observer, Environment environment_temp, double epsilon) {
        this.observer = observer;
        // Visibility polygon algorithm for environments with holes
        // Radial line (AKA angular plane) sweep technique.
        //
        // Based on algorithms described in
        //
        // [1] "Automated Camera Layout to Satisfy Task-Specific and
        // Floorplan-Specific Coverage Requirements" by Ugur Murat Erdem
        // && Stan Scarloff, April 15, 2004
        // available at BUCS Technical Report Archive:
        // http://www.cs.bu.edu/techreports/pdf/2004-015-camera-layout.pdf
        //
        // [2] "Art Gallery Theorems && Algorithms" by Joseph O'Rourke
        //
        // [3] "Visibility Algorithms in the Plane" by Ghosh
        //

        // We define a k-point is a point seen on the other side of a
        // visibility occluding corner. This name is appropriate because
        // the vertical line in the letter "k" is like a line-of-sight past
        // the corner of the "k".

        //
        // Preconditions:
        // (1) the Environment is epsilon-valid,
        // (2) the Point observer is actually in the Environment
        // environment_temp,
        // (3) the guard has been epsilon-snapped to the boundary, followed
        // by vertices of the environment (the order of the snapping
        // is important).
        //
        // :WARNING:
        // For efficiency, the assertions corresponding to these
        // preconditions have been excluded.
        //
        assert (environment_temp.is_valid(epsilon));
        assert (environment_temp.is_in_standard_form());
        assert (observer.in(environment_temp, epsilon));

        // true => data printed to terminal
        // false => silent

        // The visibility polygon cannot have more vertices than the environment.
        vertices.ensureCapacity(environment_temp.n());

        //
        // --------PREPROCESSING--------
        //

        // construct a POLAR EDGE LIST from environment_temp's outer
        // boundary and holes. During this construction, those edges are
        // split which either (1) cross the ray emanating from the observer
        // parallel to the x-axis (of world coords), or (2) contain the
        // observer in their relative interior (w/in epsilon). Also, edges
        // having first vertex bearing >= second vertex bearing are
        // eliminated because they cannot possibly contribute to the
        // visibility polygon.

        final Angle ANGLE_PI = new Angle(Math.PI);
        final Angle ANGLE_ZERO = new Angle(0.0);
        ArrayList<PolarEdge> elp = new ArrayList<PolarEdge>();
        PolarPoint ppoint1, ppoint2;
        PolarPoint split_bottom, split_top;
        double t;
        // If the observer is standing on the Enviroment boundary with its
        // back to the wall, these will be the bearings of the next vertex
        // to the right && to the left, respectively.
        Angle right_wall_bearing = new Angle(0.0);
        Angle left_wall_bearing = new Angle(0.0);
        for (int i = 0; i <= environment_temp.h(); i++) {
            Polygon polygon = environment_temp.get(i);

            for (int j = 0; j < polygon.n(); j++) {
                ppoint1 = new PolarPoint(observer, polygon.get(j));
                ppoint2 = new PolarPoint(observer, polygon.get(j + 1));
                log.debug("contemplating " +  ppoint1 + " and " +  ppoint1);

                // If the observer is in the relative interior of the edge.
                if (observer.in_relative_interior_of(new LineSegment(ppoint1, ppoint2), epsilon)) {
                    log.debug("in relative interior");

                    // Split the edge at the observer && add the resulting two
                    // edges to elp (the polar edge list).
                    split_bottom = new PolarPoint(observer, observer);
                    split_top = new PolarPoint(observer, observer);

                    if (ppoint2.bearing.equals(ANGLE_ZERO))
                        ppoint2.set_bearing_to_2pi();

                    left_wall_bearing = ppoint1.bearing.clone();
                    right_wall_bearing = ppoint2.bearing.clone();

                    elp.add(new PolarEdge(ppoint1, split_bottom));
                    elp.add(new PolarEdge(split_top, ppoint2));
                    continue;
                }

                // Else if the observer is on first vertex of edge.
                else if (observer.distance(ppoint1) <= epsilon) {
                    log.debug("on first vertex");

                    if (ppoint2.bearing.equals(ANGLE_ZERO)) {
                        ppoint2.set_bearing_to_2pi();
                    }
                    // Get right wall bearing.
                    right_wall_bearing = ppoint2.bearing.clone();
                    elp.add(new PolarEdge(new PolarPoint(observer, observer), ppoint2));
                    continue;
                }
                // Else if the observer is on second vertex of edge.
                else if (observer.distance(ppoint2) <= epsilon) {
                    log.debug("on second vertex");

                    // Get left wall bearing.
                    left_wall_bearing = ppoint1.bearing.clone();
                    elp.add(new PolarEdge(ppoint1, new PolarPoint(observer, observer)));
                    continue;
                }

                // Otherwise the observer is not on the edge.

                // If edge not horizontal (w/in epsilon).
                else if (Math.abs(ppoint1.y - ppoint2.y) > epsilon) {
                    log.debug("off edge");

                    // Possible source of numerical instability?
                    t = (observer.y - ppoint2.y) / (ppoint1.y - ppoint2.y);
                    // If edge crosses the ray emanating horizontal && right of
                    // the observer.
                    if (0 < t && t < 1 && observer.x < t * ppoint1.x + (1 - t) * ppoint2.x) {
                        log.debug("crosses ray");

                        // If first point is above, omit edge because it runs
                        // 'against the grain'.
                        if (ppoint1.y > observer.y)
                            continue;
                        // Otherwise split the edge, making sure angles are assigned
                        // correctly on each side of the split point.
                        split_bottom = new PolarPoint(observer, new Point(t * ppoint1.x + (1 - t)
                                * ppoint2.x, observer.y));
                        split_top = new PolarPoint(observer, new Point(t * ppoint1.x + (1 - t)
                                * ppoint2.x, observer.y));
                        split_top.set_bearing(ANGLE_ZERO);
                        split_bottom.set_bearing_to_2pi();
                        elp.add(new PolarEdge(ppoint1, split_bottom));
                        elp.add(new PolarEdge(split_top, ppoint2));
                        continue;
                    } else {
                        if (ppoint1.bearing.compareTo(ppoint2.bearing) >= 0
                                && ppoint2.bearing.equals(ANGLE_ZERO)
                                && ppoint1.bearing.compareTo(ANGLE_PI) > 0) {
                            ppoint2.set_bearing_to_2pi();
                        // Filter out edges which run 'against the grain'.
                        } else if ((ppoint1.bearing.equals(ANGLE_ZERO) && 
                                ppoint2.bearing.compareTo(ANGLE_PI) > 0)
                                || ppoint1.bearing.compareTo(ppoint2.bearing) >= 0) {
                            continue;
                        }
                    }
                    elp.add(new PolarEdge(ppoint1, ppoint2));
                    continue;
                }
                // If edge is horizontal (w/in epsilon).
                else {
                    log.debug("epsilon horizontal");
                    // Filter out edges which run 'against the grain'.
                    if (ppoint1.bearing.compareTo(ppoint2.bearing) >= 0)
                        continue;
                    elp.add(new PolarEdge(ppoint1, ppoint2));
                }
            }
        }

        // construct a SORTED LIST, q1, OF VERTICES represented by
        // PolarPointWithEdgeInfo objects. A
        // PolarPointWithEdgeInfo is a derived class of PolarPoint
        // which includes (1) a pointer to the corresponding edge
        // (represented as a PolarEdge) in the polar edge list elp, and
        // (2) a boolean(is_first) which is true iff that vertex is the
        // first Point of the respective edge (is_first == false => it's
        // second Point). q1 is sorted according to lex. order of polar
        // coordinates just as PolarPoints are, but with the additional
        // requirement that if two vertices have equal polar coordinates,
        // the vertex which is the first point of its respective edge is
        // considered greater. q1 will serve as an event point queue for
        // the radial sweep.
        ArrayList<PolarPointWithEdgeInfo> q1 = new ArrayList<PolarPointWithEdgeInfo>();
        PolarPointWithEdgeInfo ppoint_wei1 = new PolarPointWithEdgeInfo(), ppoint_wei2 = new PolarPointWithEdgeInfo();
        Iterator<PolarEdge> elp_iterator = elp.iterator();
        while (elp_iterator.hasNext()) {
            PolarEdge edge = elp_iterator.next();
            ppoint_wei1.set_polar_point(edge.first);
            ppoint_wei1.incident_edge = edge;
            ppoint_wei1.is_first = true;
            ppoint_wei2.set_polar_point(edge.second);
            ppoint_wei2.incident_edge = edge;
            ppoint_wei2.is_first = false;
            // If edge contains the observer, then adjust the bearing of
            // the PolarPoint containing the observer.
            if (observer.distance(ppoint_wei1) <= epsilon) {
                if (right_wall_bearing.compareTo(left_wall_bearing) > 0) {
                    ppoint_wei1.set_bearing(right_wall_bearing);
                    edge.first.set_bearing(right_wall_bearing);
                } else {
                    ppoint_wei1.set_bearing(ANGLE_ZERO);
                    edge.first.set_bearing(ANGLE_ZERO);
                }
            } else if (observer.distance(ppoint_wei2) <= epsilon) {
                if (right_wall_bearing.compareTo(left_wall_bearing) > 0) {
                    ppoint_wei2.set_bearing(right_wall_bearing);
                    edge.second.set_bearing(right_wall_bearing);
                } else {
                    ppoint_wei2.set_bearing_to_2pi();
                    edge.second.set_bearing_to_2pi();
                }
            }
            q1.add(ppoint_wei1.clone());
            q1.add(ppoint_wei2.clone());
        }

        // Put event point in correct order.
        // Collections.sort is a stable sort.
        Collections.sort(q1);
        for (PolarPointWithEdgeInfo q : q1) {
            log.debug("q: " + q);
        }
        //
        // -------PREPARE FOR MAIN LOOP-------
        //

        // current_vertex is used to hold the event point (from q1)
        // considered at iteration of the main loop.

        //
        PolarPointWithEdgeInfo current_vertex = new PolarPointWithEdgeInfo();
        // Note active_edge and e are not actually edges themselves, but
        // iterators pointing to edges. active_edge keeps track of the
        // current edge visible during the sweep. e is an auxiliary
        // variable used in calculation of k-points
        PolarEdge active_edge, e;
        // More aux vars for computing k-points.
        PolarPoint k = new PolarPoint();
        double k_range;
        LineSegment xing;

        // Priority queue of edges, where lower (first) priority indicates closer
        // range to observer along current ray (of ray sweep).
        IncidentEdgeCompare my_iec = new IncidentEdgeCompare(observer, current_vertex, epsilon);
        PriorityQueue<PolarEdge> q2 = new PriorityQueue<PolarEdge>(elp.size(), my_iec);

        // Initialize main loop.
        current_vertex.set(q1.remove(0));
        active_edge = current_vertex.incident_edge;

        // Insert e into q2 as long as it doesn't contain the
        // observer.
        if (observer.distance(active_edge.first) > epsilon
                && observer.distance(active_edge.second) > epsilon) {

            q2.add(active_edge);
        }

        vertices.add(new Point(current_vertex));
        log.debug("adding: " + current_vertex + "\n--");

        // -------BEGIN MAIN LOOP-------//
        //
        // Perform radial sweep by sequentially considering each vertex
        // (event point) in q1.

        while (!q1.isEmpty()) {

            // Pop current_vertex from q1.
            current_vertex.set(q1.remove(0));
            log.debug("cv: " + current_vertex);

            // ---Handle Event Point---

            // TYPE 1: current_vertex is the _second_vertex_ of active_edge.
            if (current_vertex.incident_edge.equals(active_edge) && !current_vertex.is_first) {
                log.debug( "type 1");

                if (!q1.isEmpty()) {
                    // If the next vertex in q1 is contiguous.
                    if (current_vertex.distance(q1.get(0)) <= epsilon) {
                        continue;
                    }
                }

                // Push current_vertex onto visibility polygon
                vertices.add(new Point(current_vertex));
                log.debug("adding: " + current_vertex);

                chop_spikes_at_back(observer, epsilon);

                while (!q2.isEmpty()) {
                    e = q2.peek();
                    log.debug("q2: " + e);
                    // If the current_vertex bearing has not passed, in the
                    // lex. order sense, the bearing of the second point of the
                    // edge at the front of q2.
                    if ((current_vertex.bearing.get() <= e.second.bearing.get())
                    // For robustness.
                            && new Ray(observer, current_vertex.bearing).distance(e.second) >= epsilon
                    /*
                     * was && std::min( distance(Ray(observer, current_vertex.bearing), e.second),
                     * distance(Ray(observer, e.second.bearing), current_vertex) ) >= epsilon
                     */
                    ) {
                        // Find intersection point k of ray (through
                        // current_vertex) with edge e.
                        xing = new Ray(observer, current_vertex.bearing).intersection(
                                new LineSegment(e.first, e.second), epsilon);

                        // assert( xing.size() > 0 );

                        if (xing.size() > 0) {
                            k = new PolarPoint(observer, xing.first());
                        } else { // Error contingency.
                            k = current_vertex;
                            e = current_vertex.incident_edge;
                        }

                        // Push k onto the visibility polygon.
                        vertices.add(new Point(k));
                        log.debug("adding k1: " + k);
                        chop_spikes_at_back(observer, epsilon);
                        active_edge = e;
                        break;
                    }

                    q2.poll();
                }
            } // Close Type 1.

            // If current_vertex is the _first_vertex_ of its edge.
            if (current_vertex.is_first) {
                log.debug("is first");
                // Find intersection point k of ray (through current_vertex)
                // with active_edge.

                xing = new Ray(observer, current_vertex.bearing).intersection(new LineSegment(
                        active_edge.first, active_edge.second), epsilon);
                if (xing.size() == 0
                        || (active_edge.first.distance(observer) <= epsilon && active_edge.second.bearing
                                .compareTo(current_vertex.bearing) <= 0)
                        || active_edge.second.compareTo(current_vertex) < 0) {

                    k_range = Double.POSITIVE_INFINITY;
                } else {
                    k = new PolarPoint(observer, xing.first());
                    k_range = k.range;
                }

                // Incident edge of current_vertex.
                e = current_vertex.incident_edge;

                // Insert e into q2 as long as it doesn't contain the
                // observer.
                if (observer.distance(e.first) > epsilon && observer.distance(e.second) > epsilon) {

                    q2.add(e);
                }

                // TYPE 2: current_vertex is (1) a first vertex of some edge
                // other than active_edge, && (2) that edge should not become
                // the next active_edge. This happens, e.g., if that edge is
                // (rangewise) in back along the current bearing.
                if (k_range < current_vertex.range) {
                    // this is empty, in the original too -DMT
                    log.debug("type 2");
                } // Close Type 2.

                // TYPE 3: current_vertex is (1) the first vertex of some edge
                // other than active_edge, && (2) that edge should become the
                // next active_edge. This happens, e.g., if that edge is
                // (rangewise) in front along the current bearing.
                if (k_range >= current_vertex.range) {
                    // Push k onto the visibility polygon unless effectively
                    // contiguous with current_vertex.
                    log.debug("type 3");
                    if (xing.size() > 0 && k_range != Double.POSITIVE_INFINITY
                            && k.distance(current_vertex) > epsilon
                            && active_edge.first.distance(observer) > epsilon) {

                        // Push k-point onto the visibility polygon.
                        vertices.add(new Point(k));
                        log.debug("adding k2: " + k);
                        chop_spikes_at_back(observer, epsilon);
                    }

                    // Push current_vertex onto the visibility polygon.
                    vertices.add(new Point(current_vertex));
                    log.debug("adding: " + current_vertex);
                    chop_spikes_at_back(observer, epsilon);
                    // Set active_edge to edge of current_vertex.
                    active_edge = e;

                } // Close Type 3.
            }

        } //
          //
          // -------END MAIN LOOP-------//

        // The VisibilityPolygon should have a minimal representation
        chop_spikes_at_wrap_around(observer, epsilon);
        eliminate_redundant_vertices(epsilon);
        chop_spikes(observer, epsilon);
        enforce_standard_form();

    }

    VisibilityPolygon(Point observer, Polygon polygon_temp, double epsilon) {
        this(observer, new Environment(polygon_temp), epsilon);
    }

}
>>>>>>> 76aa07461566a5976980e6696204781271955163
