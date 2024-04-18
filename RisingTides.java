package tides;

import java.util.*;

/**
 * This class contains methods that provide information about select terrains 
 * using 2D arrays. Uses floodfill to flood given maps and uses that 
 * information to understand the potential impacts. 
 * Instance Variables:
 *  - a double array for all the heights for each cell
 *  - a GridLocation array for the sources of water on empty terrain 
 * 
 * @author Original Creator Keith Scharz (NIFTY STANFORD) 
 * @author Vian Miranda (Rutgers University)
 * 
 * 
 * @name  Ethan Wang
 * @date  December 22, 2023
 * @class Period 3
 * 
 */
public class RisingTides {

    // Instance variables
    private double[][] terrain;     // an array for all the heights for each cell
    private GridLocation[] sources; // an array for the sources of water on empty terrain 

    /**
     * DO NOT EDIT!
     * Constructor for RisingTides.
     * @param terrain passes in the selected terrain 
     */
    public RisingTides(Terrain terrain) {
        this.terrain = terrain.heights;
        this.sources = terrain.sources;
    }

    /** 5 points
     * 
     * Find the lowest and highest point of the terrain and output it.
     * 
     * @return double[], with index 0 and index 1 being the lowest and 
     * highest points of the terrain, respectively
     */
    public double[] elevationExtrema() {

        /* WRITE YOUR CODE BELOW */
    	double extremaMax = terrain[0][0];
    	double extremaMin = terrain[0][0];
    	double subject;

    	for (int i=0; i<terrain.length; i++) {
    		for (int j=0; j<terrain[i].length; j++) {
    			subject = terrain[i][j];
    			if (subject<extremaMin) extremaMin=subject;
    			if (subject>extremaMax) extremaMax=subject;
    		}
    	}
    	double[] info = {extremaMin, extremaMax};
        return info; // substitute this line. It is provided so that the code compiles.
    }

    /** 10 points
     * 
     * Implement the floodfill algorithm using the provided terrain and sources.
     * 
     * All water originates from the source GridLocation. If the height of the 
     * water is greater than that of the neighboring terrain, flood the cells. 
     * Repeat iteratively till the neighboring terrain is higher than the water 
     * height.
     * 
     * 
     * @param height of the water
     * @return boolean[][], where flooded cells are true, otherwise false
     */
    public boolean[][] floodedRegionsIn(double height) {
        
        /* WRITE YOUR CODE BELOW */
    	boolean[][] result = new boolean[terrain.length][terrain[0].length];

    	ArrayList<GridLocation> GridLocations = new ArrayList<GridLocation>();
    	for (int i=0; i<sources.length ;i++) {
    		GridLocations.add(sources[i]);
    	}
    	while (GridLocations.size() > 0) {
    		int r = GridLocations.get(0).row;
    		int c = GridLocations.get(0).col;
    		if (terrain[r][c]<height) result[r][c]=true;
    		GridLocations.remove(0);

    		if ((r<terrain.length-1)&&(terrain[r+1][c]<=height)&&result[r+1][c]==false) { 
    			result[r+1][c] = true;
    			GridLocation s = new GridLocation(r+1,c);
    			GridLocations.add(s);
    		}
    		if ((r>0)&&(terrain[r-1][c]<=height)&&result[r-1][c]==false) { 
    			result[r-1][c] = true;
    			GridLocation n = new GridLocation(r-1,c);
    			GridLocations.add(n);
    		}
    		if ((c<terrain[r].length-1)&&(terrain[r][c+1]<=height)&&result[r][c+1]==false) { 
    			result[r][c+1] = true;
    			GridLocation e = new GridLocation(r,c+1);
    			GridLocations.add(e);
    		}
    		if ((c>0)&&(terrain[r][c-1]<=height)&&result[r][c-1]==false) { 
    			result[r][c-1] = true;
    			GridLocation w = new GridLocation(r,c-1);
    			GridLocations.add(w);
    		}
    	}
        return result; // substitute this line. It is provided so that the code compiles.
    }

    /** 5 points
     * 
     * Checks if a given cell is flooded at a certain water height.
     * 
     * @param height of the water
     * @param cell location 
     * @return boolean, true if cell is flooded, otherwise false
     */
    public boolean isFlooded(double height, GridLocation cell) {
        
        /* WRITE YOUR CODE BELOW */
    	boolean[][] floodResults = floodedRegionsIn(height);
    	return floodResults[cell.row][cell.col]; // substitute this line. It is provided so that the code compiles.
    }

    /** 5 points
     * 
     * Given the water height and a GridLocation find the difference between 
     * the chosen cells height and the water height.
     * 
     * If the return value is negative, the Driver will display "meters below"
     * If the return value is positive, the Driver will display "meters above"
     * The value displayed will be positive.
     * 
     * @param height of the water
     * @param cell location
     * @return double, representing how high/deep a cell is above/below water
     */
    public double heightAboveWater(double height, GridLocation cell) {
        
        /* WRITE YOUR CODE BELOW */
        return terrain[cell.row][cell.col]-height; // substitute this line. It is provided so that the code compiles.
    }

    /** 5 points
     * 
     * Total land available (not underwater) given a certain water height.
     * 
     * @param height of the water
     * @return int, representing every cell above water
     */
    public int totalVisibleLand(double height) {
        
        /* WRITE YOUR CODE BELOW */
    	boolean[][] floodResults = floodedRegionsIn(height);
    	int count = 0;
    	for (int i=0; i<floodResults.length; i++) {
    		for (int j=0; j<floodResults[i].length; j++) {
    			if (floodResults[i][j]==false) count++;
    		}
    	}
        return count; // substitute this line. It is provided so that the code compiles.
    } 


    /** 5 points
     * 
     * Given 2 heights, find the difference in land available at each height. 
     * 
     * If the return value is negative, the Driver will display "Will gain"
     * If the return value is positive, the Driver will display "Will lose"
     * The value displayed will be positive.
     * 
     * @param height of the water
     * @param newHeight the future height of the water
     * @return int, representing the amount of land lost or gained
     */
    public int landLost(double height, double newHeight) {
        
        /* WRITE YOUR CODE BELOW */
        return totalVisibleLand(height)- totalVisibleLand(newHeight); // substitute this line. It is provided so that the code compiles.
    }

    /** 10 points
     * 
     * Count the total number of islands on the flooded terrain.
     * 
     * Parts of the terrain are considered "islands" if they are completely 
     * surround by water in all 8-directions. Should there be a direction (ie. 
     * left corner) where a certain piece of land is connected to another 
     * landmass, this should be considered as one island. A better example 
     * would be if there were two landmasses connected by one cell. Although 
     * seemingly two islands, after further inspection it should be realized 
     * this is one single island. Only if this connection were to be removed 
     * (height of water increased) should these two landmasses be considered 
     * two separate islands.
     * 
     * @param height of the water
     * @return int, representing the total number of islands
     */
    public int numOfIslands(double height) {
        
        /* WRITE YOUR CODE BELOW */
    	boolean[][] floodResults = floodedRegionsIn(height);
    	WeightedQuickUnionUF result = new WeightedQuickUnionUF(terrain.length,terrain[0].length);
    	
    	for (int r=0; r<terrain.length; r++) {
    		for (int c=0; c<terrain[r].length; c++) {
    			GridLocation og = new GridLocation(r,c);
    			if(!floodResults[r][c]) {
	
	    			if ((r<terrain.length-1)&&!(floodResults[r+1][c])) { //south
		    			GridLocation s = new GridLocation(r+1,c);
		    			result.union(og,s);
		    		}
	    			if ((r<terrain.length-1)&&(c<terrain[r].length-1)&&!(floodResults[r+1][c+1])) { //south east
		    			GridLocation se = new GridLocation(r+1,c+1);
		    			result.union(og,se);
		    		}
	    			if ((r<terrain.length-1)&&(c>0)&&!(floodResults[r+1][c-1])) { //south west
		    			GridLocation sw = new GridLocation(r+1,c-1);
		    			result.union(og,sw);
		    		}
		    		if ((r>0)&&!(floodResults[r-1][c])) { //north
		    			GridLocation n = new GridLocation(r-1,c);
		    			result.union(og,n);
		    		}
		    		if ((r>0)&&(c<terrain[r].length-1)&&!(floodResults[r-1][c+1])) { //north east
		    			GridLocation ne = new GridLocation(r-1,c+1);
		    			result.union(og,ne);
		    		}
		    		if ((r>0)&&(c>0)&&!(floodResults[r-1][c-1])) { //north west
		    			GridLocation nw = new GridLocation(r-1,c-1);
		    			result.union(og,nw);
		    		}
		    		if ((c<terrain[r].length-1)&&!(floodResults[r][c+1])) { //east
		    			GridLocation e = new GridLocation(r,c+1);
		    			result.union(og,e);
		    		}
		    		if ((c>0)&&!(floodResults[r][c-1])) { //west
		    			GridLocation w = new GridLocation(r,c-1);
		    			result.union(og,w);
		    		}
    			}
    		}
    	}
    	int count = 0;
    	for (int ro=0; ro<terrain.length; ro++) {
    		for (int co=0; co<terrain[ro].length; co++) {
    			if ((floodResults[ro][co]==false)&&(result.find(new GridLocation(ro,co)).equals(new GridLocation(ro,co)))) {
    				count++;
    			}
    		}
    	}

        return count; // substitute this line. It is provided so that the code compiles.
    }
}
