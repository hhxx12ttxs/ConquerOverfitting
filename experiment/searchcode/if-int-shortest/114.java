/**
 * 
 * @author Kaiven Zhou [kz2182]
 *
 */

import java.io.*;
import java.math.*;
import java.util.Scanner;
import java.util.LinkedList;

public class Graph 
{
	private City[] cities; //list of citys
	private City current; //the current city
	
	public Graph()
	{
		current = null;
		cities = new City[0];
	}
	
	/**
	 * Parse the file, and add the citys to the Graph
	 * @param filename
	 */
	public void load(String filename)
	{
		Scanner choice = new Scanner(System.in);
		String reset = "";

		while(!(reset.equals("Y") || reset.equals("N"))) //check whether or not to reset graph
		{
			System.out.println("Reset graph? Y/N: ");
			reset = choice.nextLine();
		}

		System.out.println("Creating...");
		try
		{
			BufferedReader in = new BufferedReader (new FileReader(filename));
			
			int numCities = Integer.parseInt(in.readLine());

			int startAppendingIndex = 0;
			if(reset.equals("Y"))
			{	
				cities = new City[numCities];
			}
			else //recreate the graph's array, copying over the previous graph's cities
			{
				int previousSize = cities.length;
				startAppendingIndex = previousSize;
				
				City[] temp = new City[numCities + previousSize];
				for(int i = 0; i < previousSize; i++)
				{
					temp[i] = cities[i];
				}
				cities = temp;		 
			}

			String[] cityState;
			double latitude, longitude;
			for(int i = startAppendingIndex; i < cities.length; i++) //add the Citys to the graph
			{
				cityState = parseName(in.readLine());;
				latitude = Double.parseDouble(in.readLine());
				longitude = Double.parseDouble(in.readLine());

				cities[i] = new City(i, cityState[0], cityState[1], latitude, longitude);
			}
			in.close();
			
			createConnections();
		}
		catch (IOException e)
		{
			System.out.println("File not found.");
		}
	}
	
	/**
	 * Parse the name and state of the city
	 * @param cityState an entry in the file, containing the city's name and state
	 * @return String[], where 0 is the city's name, and 1 is the city's state
	 */
	private String[] parseName(String cityState)
	{
		String[] answer = new String[2];
		answer[0] = cityState;
		answer[1] = cityState;
		
		for(int i = cityState.length() - 1; i >= 0; i--) //city is define as the largest substring before the last comma
		{
			if(cityState.charAt(i) == ',')
			{
				answer[0] = cityState.substring(0,i).trim();
				answer[1] = cityState.substring(i+1).trim();
				break;
			}
		}
		return answer; //if a unique state is not found, the state is named the same as the city
	}
	
	/**
	 * Randomly connect the citys in the graph, using the algorithm defined in the instructions
	 */
	private void createConnections()
	{
		int numConnections = 0;
		int randomId = 0;
		int randomWeight = 0;
		for(int i = 0; i < cities.length; i++)
		{
			numConnections = (int)(Math.random() * 7 + 2); //random number of connections from 2 to 8

			for(int j = 0; j < numConnections; j++)
			{
				randomId = (int)(Math.random() * cities.length);
				//ensure that the edges leaving current city do not point to itself, 
				//or to a city that is already connected to the current
				while(randomId == i || cities[i].hasCity(randomId))
					randomId = (int)(Math.random() * cities.length);
				
				randomWeight = (int)(Math.random() * 1901 + 100); //random weight from 100 to 2000

				cities[i].addCity(cities[randomId], randomWeight); //create directed edge from current city to random city
				cities[randomId].updateInOut(1); //new connection going into City randomId
				System.out.println("Created connection from " + i + " to " + randomId + " with weight=" + randomWeight);
			}
		}
		
		//check if any city doesn't have an incoming connection, and create one if it doesn't
		randomId = 0;
		for(int i=0; i < cities.length; i++)
		{
			if(!cities[i].hasIncoming())
			{
				randomId = (int)(Math.random() * cities.length);
				//ensure that the edges leaving current city do not point to itself, 
				//or to a city that is already connected to the current
				while(randomId == i || cities[i].hasCity(randomId))
					randomId = (int)(Math.random() * cities.length);
				
				randomWeight = (int)(Math.random() * 1901 + 100); //random weight from 100 to 2000

				cities[randomId].addCity(cities[i], randomWeight); //create directed edge from current city to random city
				cities[i].updateInOut(1); //new connection going into City i
				System.out.println("Created connection from " + randomId + " to " + i + " with weight=" + randomWeight);
			}
		}
	}
	
	public void loadBin()
	{
		try
		{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("mygraph.bin"));
			cities = (City[]) in.readObject(); //throws error for some reason. i dont know why (classNotFoundException)
			
		}
		catch(IOException e)
		{
			System.out.println("Error loading. Try again.");
		}
	}
	
	public void save()
	{
		try
		{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("mygraph.bin"));
			out.writeObject(cities);
			out.close();
		}
		catch(IOException e)
		{
			System.out.println("Error saving. Try again.");
		}
	}
	
	/**
	 * Finds and prints out all citys that have a state with the given name
	 * @param name the name of the state
	 */
	public void searchState(String name)
	{
		City tempCity;
		for(int i=0; i < cities.length; i++)
		{
			tempCity = cities[i];
			if(tempCity.getState().equals(name))
			{
				System.out.println(tempCity);
			}
		}
	}
	/**
	 * Finds and prints out all citys that have a city with the given name
	 * @param name the name of the city
	 */
	public void searchCity(String name)
	{
		for(int i=0; i < cities.length; i++)
		{
			if(cities[i].getName().equals(name))
			{
				System.out.println(cities[i].toString());
			}
		}
	}
	private void setRandomCurrent()
	{
		if(cities.length == 0)
			System.out.println("Nothing in the graph. Please load a file.");
		else
		{
			System.out.println("Random current city: ");
			current = cities[(int) Math.random() * cities.length];
		}
			
	}
	public void setCurrent(int id) //city id is its id in the cities array
	{
		try
		{
			current = cities[id];
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println("Not a valid id. Choosing randomly...");
			setRandomCurrent();
			showCurrent();
		}
	}
	
	public void showCurrent()
	{
		if(current != null)
			System.out.println(current.toString());
		else
		{
			System.out.println("No current set. Choosing randomly...");
			setRandomCurrent();
			showCurrent();
		}
	}
	
	/**
	 * Insertion sorts based on gps distances
	 * slightly modified from the book's (pg 273)
	 * @param n how many cities to return
	 */
	public void gpsClosest(int n)
	{
		if(current == null) //ensure that there is a current
			setRandomCurrent();
		
		City[] tempCities = new City[cities.length - 1]; //create a temporary array, to be sorted
		int j=0;
		for(int i = 0 ; i < cities.length; i++)
		{
			if(!cities[i].equals(current)) //ignore the current city
			{
				tempCities[j] = cities[i];
				j++;
			}
		}
		
		City temp;
		j = 0;
		for(int i = 1; i < tempCities.length; i++) //insertion sort citys w/respect to gps distance to the current city
		{
			temp = tempCities[i];
			for(j = i; j > 0 && gpsDistance(current, temp) < gpsDistance(current, tempCities[j-1]); j--)
			{
				tempCities[j] = tempCities[j-1];
			}
				
			tempCities[j] = temp;
		}
		
		for(int i = 0; i < n; i++) //print out the n closest cities using GPS distance
		{
			System.out.println(tempCities[i].toString());
		}
	}

	/**
	 * Since the link to the given distance calculator is broken, I am using the following links to calculate gps distance
	 * - http://comminfo.rutgers.edu/~jacekg/devel/mobile/android/Distance%20based%20on%20lat_long.pdf
	 * - https://sites.google.com/a/cs.usfca.edu/cs-107-03-2012-spring/tutorials/distancecalculator
	 * 
	 */
	private double gpsDistance(City a, City b)
	{
		double x = 69.1*(b.getLatitude() - a.getLatitude());
		double y = 69.1*(b.getLongitude() - a.getLongitude()) * Math.cos(a.getLatitude() / 57.3);
		return Math.sqrt( Math.pow(x, 2.0) + Math.pow(y, 2.0) );
	}
	/**
	 * Finds all the Citys with edge distance less than the threshold
	 * @param threshold
	 */
	public void closest(int threshold)
	{
		if(current == null) //ensure that there is a current
			setRandomCurrent();
		
		int[] edgeDistance = edgeDistances(true); //edgeDistance[i] gives the distance from the current City to City i


		City[] tempCities = new City[cities.length - 1]; //create a temporary array, to be sorted
		int j=0;
		for(int i = 0 ; i < cities.length; i++)
		{
			if(!cities[i].equals(current)) //ignore current city
			{
				tempCities[j] = cities[i];
				j++;
			}
		}
		
		City temp;
		j = 0;
		for(int i = 1; i < tempCities.length; i++) //insertion sort the Citys based on their distance w/respect to the current city
		{
			temp = tempCities[i];
			for(j = i; j > 0 && edgeDistance[temp.getId()] < edgeDistance[tempCities[j-1].getId()]; j--)
			{
				tempCities[j] = tempCities[j-1];
			}
				
			tempCities[j] = temp;
		}
		for(int i=0; i<tempCities.length; i++) //go through the sorted list of Citys, printing those that have distance < threshold
		{
			if(edgeDistance[tempCities[i].getId()] < threshold)
				System.out.println(tempCities[i]);
			else
				break;
		}
	}
	
	/**
	 * Finds and outputs the shortest path from the current city to the user's specified
	 * @param end the id of the destination city
	 */
	public void shortest(int end)
	{
		if(current == null) //ensure that there is a current
			setRandomCurrent();
		
		int[] revSequence = edgeDistances(false); //revSequence[i] is the id of the city which points to City i in the shortest path
		
		LinkedList<Integer> path = new LinkedList<Integer>();
		int id = end;
		while(id != current.getId())
		{
			path.add(0,id);
			id = revSequence[id];
		}
		path.add(0,current.getId());
		for(Integer i: path)
		{
			System.out.println(cities[i]);
		}
	}
	
	/**
	 * Implementation of Dijkstra's Algorithm, loosely based off the code on pg 379,
	 * that finds the edge distance from the current City to every other city.
	 * @param wantDistances when true, returns a list of distances. When false, returns a list of relative orders
	 * @return list of distances, or list of relative orders
	 */
	private int[] edgeDistances(boolean wantDistances)
	{
		int[] dist = new int[cities.length]; //shortest known distance to the current City
		int[] prev = new int[cities.length]; //prev[i] is the id of the City that points to City i in the shortest path
		boolean[] known = new boolean[cities.length]; //known[i] == True means City i is known
		
		for(int i=0; i< dist.length; i++)
		{
			dist[i] = Integer.MAX_VALUE;
			known[i] = false;
		}
		dist[current.getId()] = 0;
		
		int next,neighborId,weightToNeighbor;
		int candidateDist = Integer.MAX_VALUE;
		for(int i=0; i<dist.length; i++)
		{
			next = nextClosest(dist, known); //the next closest node to what's in the path so far
			known[next] = true;
			for(Edge connection: cities[next].getAdj() ) //check the neighbors of the next closest node, and update known distances
			{
				neighborId = connection.getEnd().getId();
				weightToNeighbor = connection.getWeight();
				candidateDist = dist[next] + weightToNeighbor;
				if(candidateDist < dist[neighborId])
				{
					dist[neighborId] = candidateDist;
					prev[neighborId] = next;
				}
			}
		}	
		if(wantDistances)
			return dist;
		else
			return prev;
	}
	/**
	 * Find's the closest city that is not already part of the path
	 * @param dist list of known distances
	 * @param known representation of a list of known Cities
	 * @return id of the next closest city
	 */
	private int nextClosest(int[] dist, boolean[] known)
	{
		int closestDist = Integer.MAX_VALUE;
		int nextClosest = -1;
		for(int i=0; i<dist.length; i++)
		{
			if(!known[i] && dist[i] < closestDist)
			{
				nextClosest = i;
				closestDist = dist[i];
			}
		}
		return nextClosest;
	}

}

