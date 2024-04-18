package tides;

import java.util.*;

/**
 * DO NOT EDIT!
 * This class contains the heights of the terrain and water sources.
 * 
 * @author Original Creator Keith Scharz (NIFTY STANFORD) 
 */
public class Terrain {
    public double[][] heights;
    public GridLocation[] sources;

    public Terrain(double[][] heights, GridLocation[] sources) {
        this.heights = heights;
        this.sources = sources;
    }

    @Override
    public String toString() {
        return Arrays.deepToString(heights) + Arrays.deepToString(sources);
    }
}
