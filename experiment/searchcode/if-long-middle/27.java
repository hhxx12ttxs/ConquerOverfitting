package org.jeffkubina.graph.generate.random;

import java.util.Random;

/**
 * The class ComponentSizes randomly generates the number of vertices and edges
 * in the components of a graph given the total number of components, total
 * vertices, and a random seed. It calculates the vertex and edge sizes of a
 * component in lg(TotalComponents) steps using lg(TotalComponents) memory.
 */
public class ComponentSizes
{

  /**
   * Instantiates a new component sizes.
   * 
   * @param TotalComponents
   *          The total components in the graph to be generated; force to be
   *          positive.
   * @param TotalVertices
   *          The total vertices in the graph to be generated; force to be
   *          greater than one.
   * @param TotalEdges
   *          The total edges in the graph to be generated; force to be positive
   *          and at least half of TotalVertices.
   * @param Directed
   *          True if the graph is to be directed.
   * @param RandomSeed
   *          The seed for the random number generator.
   * @param Skew
   *          The Skew is a positive number less than one used to skew the size
   *          of the components. Smaller values will generate a graph with a few
   *          large components and many small components.
   */
  public ComponentSizes(long TotalComponents, long TotalVertices,
      long TotalEdges, boolean Directed, long RandomSeed, double Skew)
  {
    /* force TotalVertices to be non-negative. */
    totalVertices = Math.abs(TotalVertices);

    /* force TotalComponents to be positive. */
    totalComponents = Math.max(1, Math.abs(TotalComponents));

    /* make sure totalVertices >= 2 * totalComponents. */
    if (TotalVertices < 2 * totalComponents)
      totalVertices = 2 * totalComponents;

    /* store the total edges for the graph. */
    totalEdges = TotalEdges;

    /* store the type of the graph, affects max size computations. */
    directed = Directed;

    /* set the random seed and create the random number generator. */
    randomSeed = RandomSeed;
    randomGenerator = new Random();

    /* get the factor to scale the random numbers by. */
    skew = Math.min(Math.abs(Skew), 1.0);
    if (skew == 0.0)
      skew = 1.0;

    /*
     * compute some preprocessing values to calculate the number of edges
     * somewhat quickly.
     */
    preprocessEdgeSizes();
  }

  /**
   * Returns the number of vertices in the component with index ComponentIndex.
   * 
   * @param ComponentIndex
   *          ComponentIndex holds the index of the component whose vertex size
   *          is computed; it is force to be in the range from zero to
   *          TotalComponents - 1.
   * @return Returns the number of vertices in component with index
   *         ComponentIndex.
   */
  public long getComponentSize(long ComponentIndex)
  {
    /* always reset the random generator for consistency. */
    randomGenerator.setSeed(randomSeed);

    /* push ComponentIndex into a valid range if necessary. */
    if (ComponentIndex < 0)
      ComponentIndex = 0;
    if (ComponentIndex >= totalComponents)
      ComponentIndex = totalComponents - 1;

    /*
     * call recursive function to get size; note each component will have at
     * least two vertices, so that it has at least one edge.
     */
    return 2 + _getComponentSize(ComponentIndex, totalVertices - 2
        * totalComponents, totalComponents);
  }

  /**
   * _get component size.
   * 
   * @param ComponentIndex
   *          ComponentIndex holds the index of the component whose edge size is
   *          computed; it must be in the range from zero to TotalComponents -
   *          1.
   * @param TotalVertices
   *          Holds the total number of vertices remaining to distribute.
   * @param TotalComponents
   *          Holds the total number of components to comprise the vertices.
   * @return Returns the size of the component as a long.
   */
  private long _getComponentSize(long ComponentIndex, long TotalVertices,
      long TotalComponents)
  {
    /* if number of components left is one, return the vertices left. */
    if (TotalComponents < 2)
      return TotalVertices;

    /* if the number of vertices left is zero, return zero. */
    if (TotalVertices < 1)
      return TotalVertices;

    /* get the middle index for the total components. */
    long middle = TotalComponents / 2;

    /* get the number of vertices for the left half of the divided vertices. */
    long leftNodes = (long) (TotalVertices * randomGenerator.nextDouble() * skew);

    if (ComponentIndex < middle)
    {
      /* get the number of components for the left half. */
      return _getComponentSize(ComponentIndex, leftNodes, middle);
    } else
    {
      /* get the number of components for the right half. */
      return _getComponentSize(ComponentIndex - middle, TotalVertices
          - leftNodes, TotalComponents - middle);
    }
  }

  /**
   * Pre-process information to compute the total edges per component quickly.
   */
  private void preprocessEdgeSizes()
  {
    /* compute the total possible edges in the graph from the component sizes. */
    totalPossibleEdges = 0;
    for (long componentIndex = 0; componentIndex < totalComponents; componentIndex++)
    {
      /* compute the maximum number of edges in the component. */
      double maxPossibleEdgesInComponent = getMaxEdgesOfGraphSize(getComponentSize(componentIndex));

      /* accumulate the total maximum edges possible in the graph. */
      totalPossibleEdges += maxPossibleEdgesInComponent;
    }

    long totalMinEdgesUsed = 0;
    for (long componentIndex = 0; componentIndex < totalComponents; componentIndex++)
    {
      /* get the total vertices in the component. */
      long totalVerticesInComponent = getComponentSize(componentIndex);

      /* compute the maximum number of edges in the component. */
      double maxPossibleEdgesInComponent = getMaxEdgesOfGraphSize(totalVerticesInComponent);

      /* compute the minimum number of edges for each component. */
      long edgesInComponent = (long) ((maxPossibleEdgesInComponent / totalPossibleEdges) * totalEdges);
      if (edgesInComponent < (long) totalVerticesInComponent)
        edgesInComponent = (long) totalVerticesInComponent - 1;

      /* accumulate the total minimum number of edges in the graph. */
      totalMinEdgesUsed += edgesInComponent;
    }

    /* compute the number of edges left to spread among the components. */
    long remainingEdges = totalEdges - totalMinEdgesUsed;
    if (remainingEdges < 0)
    {
      edgeQuotient = 0;
      edgeRemainder = 0;
    } else
    {
      edgeQuotient = remainingEdges / totalComponents;
      edgeRemainder = remainingEdges - edgeQuotient * totalComponents;
    }
  }

  /**
   * Returns the number of edges in the component with index ComponentIndex.
   * 
   * @param ComponentIndex
   *          ComponentIndex holds the index of the component whose edge size is
   *          to be computed; it is forced to be in the range from zero to
   *          TotalComponents - 1.
   * @return Returns the number of edges in the component with index
   *         ComponentIndex.
   */
  public long getEdgeSize(long ComponentIndex)
  {
    /* get the total vertices in the component. */
    long totalVerticesInComponent = getComponentSize(ComponentIndex);

    /* compute the maximum number of edges in the component. */
    double maxPossibleEdgesInComponent = getMaxEdgesOfGraphSize(totalVerticesInComponent);

    /* compute the minimum number of edges for each component. */
    long edgesInComponent = (long) ((maxPossibleEdgesInComponent / totalPossibleEdges) * totalEdges);
    if (edgesInComponent < totalVerticesInComponent)
      edgesInComponent = totalVerticesInComponent - 1;

    /* add remaining edges to the component. */
    edgesInComponent += edgeQuotient * totalComponents;
    if (ComponentIndex < edgeRemainder)
      ++edgesInComponent;

    return edgesInComponent;
  }

  /**
   * Returns the maximum possible number of edges in a graph with TotalVertices.
   * The value depends on if the graph is directed or not.
   * 
   * @param TotalVertices
   *          Holds the total number of vertices in the graph.
   * @return Returns the maximum possible number of edges in a graph with
   *         TotalVertices as a double.
   */
  private double getMaxEdgesOfGraphSize(long TotalVertices)
  {
    TotalVertices = Math.abs(TotalVertices);
    double totalVertices = (double) TotalVertices;
    if (directed)
      return totalVertices * totalVertices;
    if ((TotalVertices & 1L) == 1)
      return totalVertices * ((totalVertices - 1) / 2);
    else
      return (totalVertices / 2) * (totalVertices - 1);
  }

  /**
   * Runs the specified number of unit tests.
   * 
   * @param NumberOfTests
   *          Holds the number of unit tests to run.
   * @return Returns true if all tests successful, false otherwise.
   */
  public boolean runUnitTests(long NumberOfTests)
  {
    NumberOfTests = Math.abs(NumberOfTests);

    for (long testNo = 0; testNo < NumberOfTests; testNo++)
    {
      long totalComponents = testNo + 10;
      long totalVertices = 10 * totalComponents;
      long totalEdges = 100 * totalVertices;
      ComponentSizes componentSizes = new ComponentSizes(totalComponents,
          totalVertices, totalEdges, false, testNo, 1);

      long summedNodes = 0;
      for (long compIndex = 0; compIndex < totalComponents; compIndex++)
      {
        summedNodes += componentSizes.getComponentSize(compIndex);
      }
      if (summedNodes != totalVertices)
        return false;

      long summedEdges = 0;
      for (long compIndex = 0; compIndex < totalComponents; compIndex++)
      {
        summedEdges += componentSizes.getEdgeSize(compIndex);
      }
      if (summedEdges != totalEdges)
        return false;
    }

    return true;
  }

  /** The total components in the graph. */
  private long totalComponents;

  /** The total vertices in the graph. */
  private long totalVertices;

  /** The total edges in the graph. */
  private long totalEdges;

  /** directed holds true if the graph is directed. */
  boolean directed;

  /** The random seed used to generate the component sizes. */
  private long randomSeed;

  /** The random generator used to generate the component sizes. */
  private Random randomGenerator;

  /** The skew factor for the component sizes. */
  private double skew;

  /**
   * Holds the total possible edges the graph could have given the component
   * sizes.
   */
  private double totalPossibleEdges;

  /**
   * Holds the number of extra edges, by a factor, to distribute to each
   * component.
   */
  private long edgeQuotient;

  /** Holds the number of extra edges to distribute to each component. */
  private long edgeRemainder;
}

