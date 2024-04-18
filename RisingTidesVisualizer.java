package tides;

import javax.swing.*;
import java.awt.*;

/**
 * This class creates a visualizer for .terrain files based on cell heights.
 * 
 * @author Original Creator Keith Scharz (NIFTY STANFORD) 
 */
public class RisingTidesVisualizer extends JPanel {
    /* Terrain data */
    private double[][] terrain;

    /* Which cells are flooded; can change. */
    private boolean[][] flooded;

    /* Min and max height; used to interpolate heights to colors. */
    private double minHeight, maxHeight;

    /* Color of flooded cells. */
    private static final Color UNDERWATER_COLOR = new Color(0, 49, 83); // Prussian blue

    /*
     * The rest of the map is colored according to the following scheme. The 
     * heights are mapped to real numbers between 0 and 1. Those real numbers 
     * are then used to interpolate between a fixed series of color points 
     * marked off at various intervals.
     *
     * The RGBPoint type represents a particular RGB color annotated with a
     * threshold value between 0 and 1 indicating where that color sits.
     */
    private static final class RGBPoint {
        public final int red, green, blue;
        public final double threshold;

        public RGBPoint(int r, int g, int b, double t) {
            red = r;
            green = g;
            blue = b;
            threshold = t;
        }
    }

    /* Background color */
    private static final Color BACKGROUND_COLOR = new Color(102, 2, 60); // Tyrian purple

    /*
     * The actual colors to use to draw the map, annotated with their threshold
     * values.
     */
    private static final RGBPoint[] COLORS = new RGBPoint[] {
            new RGBPoint(0, 102, 0, 0.0), // Pakistan green
            new RGBPoint(154, 205, 50, 0.1), // Chartreuse
            new RGBPoint(251, 236, 93, 0.25), // Maize
            new RGBPoint(212, 175, 55, 0.4), // Metallic gold
            new RGBPoint(166, 60, 20, 1.01) // Sienna. The 1.01 here is to ensure we cover rounding errors.
    };

    /* Initial dimensions. */
    private static final int DEFAULT_WIDTH = 740;
    private static final int DEFAULT_HEIGHT = 600;

    public RisingTidesVisualizer() {
        setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }

    public void setTerrain(double[][] terrain) {
        /* Stash the terrain. */
        this.terrain = terrain;

        /* Clear flooding, since the terrain has changed. */
        this.flooded = null;

        /* Recompute min and max heights. */
        minHeight = Double.POSITIVE_INFINITY;
        maxHeight = Double.NEGATIVE_INFINITY;

        for (int row = 0; row < terrain.length; row++) {
            for (int col = 0; col < terrain[0].length; col++) {
                if (terrain[row][col] < minHeight)
                    minHeight = terrain[row][col];
                if (terrain[row][col] > maxHeight)
                    maxHeight = terrain[row][col];
            }
        }
    }

    public void setFlooding(boolean[][] flooded) {
        this.flooded = flooded;
    }

    private static int interpolate(int value, int min, int max, int newMin, int newMax) {
        return (int) interpolate(value + 0.0, min + 0.0, max + 1.0, newMin + 0.0, newMax);
    }

    private static double interpolate(double value, double min, double max, double newMin, double newMax) {
        return (value - min) / (max - min + 0.000001) * (newMax - newMin) + newMin;
    }

    private Color colorFor(int row, int col) {
        /* Water always draws blue. */
        if (flooded[row][col])
            return UNDERWATER_COLOR;

        /* Everything else gets a nice shade based on height. */

        /* Map everything to a value in the interaval [0, 1) */
        double alpha = interpolate(terrain[row][col], minHeight, maxHeight, 0, 1);

        /* Figure out which points we're between. */
        for (int i = 1; i < COLORS.length; i++) {
            if (alpha <= COLORS[i].threshold) {
                /*
                 * Progress is measured by how far between the two points we are. 0.0 means
                 * "completely at the left end. 1.0 means "completely at the right end."
                 */
                double progress = (alpha - COLORS[i - 1].threshold) /
                        (COLORS[i].threshold - COLORS[i - 1].threshold);

                /* Interpolate between those color points to get our overall color. */
                int red = (int) interpolate(progress, 0, 1, COLORS[i - 1].red, COLORS[i].red);
                int green = (int) interpolate(progress, 0, 1, COLORS[i - 1].green, COLORS[i].green);
                int blue = (int) interpolate(progress, 0, 1, COLORS[i - 1].blue, COLORS[i].blue);

                return new Color(red, green, blue);
            }
        }

        throw new RuntimeException("Impossible.");
    }

    @Override
    public void paint(Graphics g) {
        /* Draw the background. */
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        /* Now draw the terrain - if there is anything to draw, that is. */
        if (terrain == null || flooded == null)
            return;

        /* Compute our aspect ratio. */
        double width = getWidth();
        double height = getHeight();

        double aspectRatio = (double) terrain[0].length / terrain.length;

        /* Aspect ratio is too wide. Bring the width down. */
        if (width / height > aspectRatio) {
            width = height * aspectRatio;
        }
        /* Aspect ratio is too tall. Bring the height down. */
        else {
            height = width / aspectRatio;
        }

        int baseX = (int) ((getWidth() - width) / 2.0);
        int baseY = (int) ((getHeight() - height) / 2.0);

        for (int x = baseX; x < baseX + width; x++) {
            for (int y = baseY; y < baseY + height; y++) {
                int col = interpolate(x, baseX, (int) (baseX + width), 0, terrain[0].length);
                int row = interpolate(y, baseY, (int) (baseY + height), 0, terrain.length);

                g.setColor(colorFor(row, col));
                g.fillRect(x, y, 1, 1);
            }
        }
    }

    public double getHeightAspect() {
        return (double) getHeight()/terrain.length;
    }

    public double getWidthAspect() {
        return (double) getWidth()/terrain[0].length;
    }

    public double getDisplayAspectRatio() {
        return (double) getHeight()/getWidth();
    }

    public double getTerrainAspectRatio() {
        return (double) terrain.length/terrain[0].length;
    }
}