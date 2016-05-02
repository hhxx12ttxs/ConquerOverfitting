package org.sampottinger.cityscraper.connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.phineas.contrib.PhineasLocation;
import org.phineas.core.PhineasBoundable;
import org.phineas.core.PhineasLocateable;
import org.sampottinger.cityscraper.workspace.WorkspaceManager;

/**
 * Adapter to the DijkstraPathFinder that integrates the class with the rest of
 * CityScraper
 * @author Sam Pottinger
 */
public class NodePathFinder
{
	private static NodePathFinder instance;
	
	/**
	 * Get a shared instance of the NodePathFinder singleton.
	 * @return Shared global instance of the NodePathFinder.
	 */
	public static NodePathFinder getInstance()
	{
		if(instance == null)
			instance = new NodePathFinder();
		return instance;
	}
	
	private NodePathFinder() {}
	
	/**
	 * Find the shortest path available between nodes.
	 * @param startNode The node to start the path on.
	 * @param endNode The node to end the path on.
	 * @param workspace The workspace where the nodes reside.
	 * @return NodePathFinderResult The result of the path finding operation.
	 */
	public NodePathFinderResult findPath(PhineasBoundable startNode,
			PhineasBoundable endNode, WorkspaceManager workspace)
	{
		boolean shouldAcceptPath;
		boolean isEnd;
		boolean isStart;
		PhineasLocateable location;
		Collection<PhineasLocation> endingLocations;
		Collection<PhineasLocation> startingLocations;
		List<DijkstraSpace<PhineasLocateable>> startingSpaces;
		DijkstraSpace<PhineasLocateable> newSpace;
		DijkstraPathFinderResult<PhineasLocateable> possiblePath;

		DijkstraPathFinderResult<PhineasLocateable> shortestPath = null;
		DijkstraSpace<PhineasLocateable> shortestPathStartingSpace = null;

		DijkstraPathFinder<PhineasLocateable> pathFinder = 
				new DijkstraPathFinder<PhineasLocateable>();
		
		// Collect information about workspace
		PhineasBoundable gridBounds = workspace.getBounds();
		PhineasBoundable stepBounds = workspace.getStepBounds();
		int minX = gridBounds.getX();
		int minY = gridBounds.getY();
		int maxX = gridBounds.getWidth() + minX;
		int maxY = gridBounds.getHeight() + minY;
		int xStep = stepBounds.getWidth();
		int yStep = stepBounds.getHeight();
		int numXSpaces = (maxX - minX) / xStep;
		int numYSpaces = (maxY - minY) / yStep;
		
		// Find possible ending points
		endingLocations = workspace.getSpacesOccupiedBy(endNode);

		// Find possible starting points
		startingLocations = workspace.getSpacesOccupiedBy(startNode);
		startingSpaces = new ArrayList<DijkstraSpace<PhineasLocateable>>();
		
		// Create spaces graph
		List<DijkstraSpace<PhineasLocateable>> spaces =
				new ArrayList<DijkstraSpace<PhineasLocateable>>();
		for(int y=0; y<numYSpaces; y++)
		{
			for(int x=0; x<numXSpaces; x++)
			{
				location = new PhineasLocation(x * xStep, y * yStep);
				isStart = startingLocations.contains(location);
				isEnd = endingLocations.contains(location);
				if(isStart || isEnd ||
						workspace.isFree(location.getX(), location.getY()))
				{
					newSpace = new DijkstraSpace<PhineasLocateable>(
							location,
							isEnd
					);
					spaces.add(newSpace);
					
					// Check for starting space
					if(isStart)
						startingSpaces.add(newSpace);

					// Connect to previous spaces
					connectBehind(newSpace, x, y, spaces, numXSpaces);
				}
				else
					spaces.add(null);
			}
		}
		
		// Find shortest path
		for(DijkstraSpace<PhineasLocateable> startingSpace : startingSpaces)
		{
			// Reset spaces
			for(DijkstraSpace<PhineasLocateable> space: spaces)
			{
				if(space != null)
					space.reset();
			}

			// Try starting space and find possible path
			try {
				possiblePath = pathFinder.findPath(startingSpace);
			} catch (SpaceNotFoundException e) {
				throw new RuntimeException(
						"Unexpected program state. (%s)",
						e
				);
			}
			
			// Determine appropriate action for path
			if(possiblePath.wasSuccessful())
			{
				// Accept if no prior path found
				shouldAcceptPath = shortestPath == null;
				
				// Accept if shorter path found
				shouldAcceptPath = shouldAcceptPath || 
						shortestPath.getLength() > possiblePath.getLength();
			}
			else
				shouldAcceptPath = false;
			
			// Accept path
			if(shouldAcceptPath)
			{
				// Determine if the path is better than the previous one
				shortestPath = possiblePath;
				shortestPathStartingSpace = startingSpace;
			}
		}
		
		if(shortestPath == null)
			return null;
		
		// Peel out the shortest path 
		NodePathIterable decoratedIterable = new NodePathIterable(
				shortestPath.getPath(),
				shortestPathStartingSpace
		);
		
		return new NodePathFinderResult(
				decoratedIterable,
				shortestPath.getLength()
		);
				
	}

	/**
	 * Connect spaces to indicate how paths can go through them.
	 * 
	 * Connects a new DijkstraSpace to the existing spaces to build a graph that
	 * the path finder can later traverse.
	 * 
	 * @param newSpace The space to make connections for.
	 * @param x How many spaces to the left this space is (x coordinate in
	 * 		spaces list).
	 * @param y How many spaces down this space is (y coordinate in the spaces
	 * 		list).
	 * @param spaces List of spaces already created for the path finder.
	 * @param numXSpaces How wide in spaces the area for path planning is.
	 */
	private void connectBehind(DijkstraSpace<PhineasLocateable> newSpace,
			int x, int y, List<DijkstraSpace<PhineasLocateable>> spaces,
			int numXSpaces)
	{
		DijkstraSpace<PhineasLocateable> leftSpace;
		DijkstraSpace<PhineasLocateable> aboveSpace;
		DijkstraConnection<PhineasLocateable> newConnection;

		// Connect left
		if(x > 0)
		{
			leftSpace = spaces.get(y * numXSpaces + x - 1);
			if(leftSpace != null)
			{
				newConnection = new DijkstraConnection<PhineasLocateable>(
						leftSpace,
						newSpace,
						1,
						true
				);
				newSpace.addConnection(newConnection);
				leftSpace.addConnection(newConnection);
			}
		}
		
		// Connect above
		if(y > 0)
		{
			aboveSpace = spaces.get((y - 1) * numXSpaces + x);
			if(aboveSpace != null)
			{
				newConnection = new DijkstraConnection<PhineasLocateable>(
						aboveSpace,
						newSpace,
						1,
						true
				);
				newSpace.addConnection(newConnection);
				aboveSpace.addConnection(newConnection);
			}
		}
	}
}

