package tides;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;

/**
 * This class is designed to run student implemented methods from the 
 * RisingTides class in an interactive GUI.
 * 
 * @author Original Creator Keith Scharz (NIFTY STANFORD) 
 * @author Vian Miranda (Rutgers University)
 */
public class Driver implements MouseMotionListener {

    /**
     * NOTE TO STUDENTS: This is the folder from which the Driver reads all of 
     * the terrain files. Feel free to change the path to your choice, but 
     * please ensure the path is within the RisingTides directory! This cannot 
     * access files outside of the RisingTides directory.
     */
    private static final File TERRAIN_DIRECTORY = new File("terrains");

    private RisingTidesVisualizer display;

    private JComboBox<File> fileSelector;

    private JFrame window;

    private JTextField heightInput;

    private JTextField newHeightInput;    
    
    private JTextField glRowInput;

    private JTextField glColInput;

    private JLabel coordinates;

    private JLabel elevationExtremaLine;

    private JLabel isFloodedLine;

    private JLabel heightAboveWaterLine;

    private JLabel totalLandLine;

    private JLabel landLostLine;

    private JLabel numOfIslandsLine;

    private JLabel statusLine;

    private JPanel controlPanel;

    private File lastFile = null;

    private Terrain terrain = null;

    /* Returns a sorted list of all the terrain files we know. */
    private File[] terrainFilesIn(File directory) {
        var results = directory.listFiles((File dir, String name) -> name.endsWith(".terrain"));
        Arrays.sort(results, (File one, File two) -> one.getName().compareTo(two.getName()));
        return results;
    }

    /* Makes the drop-down file selector. */
    private JComboBox<File> makeFileSelector() {
        var result = new JComboBox<File>();
        for (var file : terrainFilesIn(TERRAIN_DIRECTORY)) {
            result.addItem(file);
        }
        return result;
    }

    /* Makes the "Load" button. */
    private JButton makeLoadButton() {
        var result = new JButton("Load");
        result.addActionListener((ActionEvent e) -> {
            heightInput.setText("0.0");
            newHeightInput.setText("0.0");
            glRowInput.setText("0");
            glColInput.setText("0");
            runSimulation((File) fileSelector.getSelectedItem());
        });
        return result;
    }

    /* Makes the "Go!" button that makes the magic happen. */
    private JButton makeGoButton() {
        var result = new JButton("Go!");
        result.addActionListener((ActionEvent e) -> {
            runSimulation((File) fileSelector.getSelectedItem());
        });
        return result;
    }

    /* Builds the control panel. */
    private JPanel makeControlPanel() {
        JPanel container = new JPanel();
        container.setLayout(new GridLayout(9, 3));

        /* The main control panel. */
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        fileSelector = makeFileSelector();
        panel.add(fileSelector);

        var loadButton = makeLoadButton();
        loadButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.add(loadButton);

        /* Spacer. */
        panel.add(new JLabel("          "));

        heightInput = new JTextField("0.0", 8);
        panel.add(new JLabel("Water Height: "));
        panel.add(heightInput);

        /* Spacer. */
        panel.add(new JLabel("     "));

        newHeightInput = new JTextField("0.0", 8);
        panel.add(new JLabel("Future Water Height: "));
        panel.add(newHeightInput);

        var goButton = makeGoButton();
        goButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.add(goButton);

        container.add(panel);

        /* isFlooded() inputs */
        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());

        glColInput = new JTextField("0", 6);
        panel2.add(new JLabel("<html>Is cell at coordinate &nbsp x (column): </html>"));
        panel2.add(glColInput);

        glRowInput = new JTextField("0", 6);
        panel2.add(new JLabel(" y (row): "));
        panel2.add(glRowInput);
        
        panel2.add(new JLabel("<html> flooded? &emsp &emsp</html>"));

        coordinates = new JLabel("(0, 0)");
        panel2.add(coordinates);

        container.add(panel2);

        /* Information from the elevationExtrema method. */
        JPanel elevationExtrema = new JPanel();
        elevationExtremaLine = new JLabel("");
        elevationExtrema.setLayout(new BorderLayout());
        elevationExtrema.add(elevationExtremaLine, BorderLayout.WEST);
        container.add(elevationExtrema);

        /* Information from the isFlooded method. */
        JPanel isFlooded = new JPanel();
        isFloodedLine = new JLabel("");
        isFlooded.setLayout(new BorderLayout());
        isFlooded.add(isFloodedLine, BorderLayout.WEST);
        container.add(isFlooded);

        /* Information from the isFlooded method. */
        JPanel heightAboveWater = new JPanel();
        heightAboveWaterLine = new JLabel("");
        heightAboveWater.setLayout(new BorderLayout());
        heightAboveWater.add(heightAboveWaterLine, BorderLayout.WEST);
        container.add(heightAboveWater);

        /* Information from the totalLand method. */
        JPanel totalLand = new JPanel();
        totalLandLine = new JLabel("");
        totalLand.setLayout(new BorderLayout());
        totalLand.add(totalLandLine, BorderLayout.WEST);
        container.add(totalLand);

        /* Information from the landLost method. */
        JPanel landLost = new JPanel();
        landLostLine = new JLabel("");
        landLost.setLayout(new BorderLayout());
        landLost.add(landLostLine, BorderLayout.WEST);
        container.add(landLost);

        /* Information from the numOfIslands method. */
        JPanel numOfIslands = new JPanel();
        numOfIslandsLine = new JLabel("");
        numOfIslands.setLayout(new BorderLayout());
        numOfIslands.add(numOfIslandsLine, BorderLayout.WEST);
        container.add(numOfIslands);

        /* The status line. */
        JPanel statusBox = new JPanel();
        statusLine = new JLabel("");
        statusBox.add(statusLine);
        container.add(statusBox);

        return container;
    }

    private Driver() {
        /* Main window. */
        window = new JFrame();
        window.setLayout(new BorderLayout());
        window.setTitle("Rising Tides");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /* Main display. */
        display = new RisingTidesVisualizer();
        display.addMouseMotionListener(this);
        display.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        window.add(display, BorderLayout.CENTER);

        /* Control panel. */
        controlPanel = makeControlPanel();
        window.add(controlPanel, BorderLayout.SOUTH);

        window.pack();
        window.setVisible(true);
    }

    /* Finds coordinates for each cell. Converts pixel locations to cell 
     * locations using aspect ratios. 
     */
    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        try {
            PointerInfo a = MouseInfo.getPointerInfo();
            Point point = a.getLocation();
            SwingUtilities.convertPointFromScreen(point, display);
            int xScreenLocation = (int) (point.getX()/display.getWidthAspect());
            int yScreenLocation = (int) (point.getY()/display.getHeightAspect());

            String coords = "(" + xScreenLocation + ", " + yScreenLocation +")";

            double error = Math.abs((display.getDisplayAspectRatio() - display.getTerrainAspectRatio())/display.getTerrainAspectRatio());
            if (error > 0.01)
                setStatusLine(String.format("Please resize the window till there are no purple borders for better accuracy (coordinates %.2f%% inaccurate).", error*100));
            else 
                setStatusLine("");

            setCoordinates(coords);
        } catch (NullPointerException n) {}     
    }

    /* Methods to update text in main control panel if changes are made. */
    private void setCoordinates(final String text) {
        SwingUtilities.invokeLater(() -> {
            coordinates.setText(text);
        });
    }

    private void setElevationExtrema(final String text) {
        SwingUtilities.invokeLater(() -> {
            elevationExtremaLine.setText(text);
        });
    }

    private void setIsFlooded(final String text) {
        SwingUtilities.invokeLater(() -> {
            isFloodedLine.setText(text);
        });
    }

    private void setHeightAboveWater(final String text) {
        SwingUtilities.invokeLater(() -> {
            heightAboveWaterLine.setText(text);
        });
    }

    private void setTotalLand(final String text) {
        SwingUtilities.invokeLater(() -> {
            totalLandLine.setText(text);
        });
    }

    private void setLandLost(final String text) {
        SwingUtilities.invokeLater(() -> {
            landLostLine.setText(text);
        });
    }

    private void setNumOfIslands(final String text) {
        SwingUtilities.invokeLater(() -> {
            numOfIslandsLine.setText(text);
        });
    }

    private void setStatusLine(final String text) {
        SwingUtilities.invokeLater(() -> {
            statusLine.setText(text);
        });
    }

    /*
     * Disables/enables all components in the given container. Taken from
     * https://stackoverflow.com/questions/10985734/java-swing-enabling-disabling-
     * all-components-in-jpanel
     */
    private void setEnabled(Container container, boolean enabled) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setEnabled(enabled);
            if (component instanceof Container) {
                setEnabled((Container) component, enabled);
            }
        }
    }

    /* Fires off the simulation based on the configuration. */
    private void runSimulation(File terrainFile) {
        // Ensure there are values for the water heights
        double waterHeight, newWaterHeight;
        try {
            waterHeight = Double.parseDouble(heightInput.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(window, "Please enter a number for the water height.", "Water Height",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            newWaterHeight = Double.parseDouble(newHeightInput.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(window, "Please enter a number for the future water height.", "Future Water Height",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int isFloodedRow, isFloodedCol;
        try {
            isFloodedRow = Integer.parseInt(glRowInput.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(window, "Please enter a valid integer for the row (y) number.", "Row Is Flooded",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            isFloodedCol = Integer.parseInt(glColInput.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(window, "Please enter a valid integer for the column (x) number.", "Column Is Flooded",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        setEnabled(controlPanel, false);
        new Thread() {
            public void run() {
                try {
                    /* Did the terrain change? */
                    if (!terrainFile.equals(lastFile)) {
                        setStatusLine("Loading the Terrain...");
                        terrain = TerrainLoader.loadTerrain(terrainFile, (int bytes, int total) -> {
                            int percent = (int) (100.0 * bytes / total);
                            int totalMB = total / (1 << 20);
                            setStatusLine("Downloading Terrain " + " (" + percent + "% of " + totalMB + " MB)");
                        });
                        display.setTerrain(terrain.heights);
                        lastFile = terrainFile;
                    }

                    
                    // Results
                    setStatusLine("Watering the World... (running your code)");
                    RisingTides rt = new RisingTides(terrain);
                    try {
                        var flooded = rt.floodedRegionsIn(waterHeight);

                        display.setFlooding(flooded);

                        try {
                            SwingUtilities.invokeAndWait(() -> display.repaint());
                        } catch (InterruptedException e) {
                            SwingUtilities.invokeLater(() -> display.repaint());
                        } catch (InvocationTargetException e) {
                            throw new IOException(e);
                        }
                        setStatusLine("");
                    } catch (Throwable e) {
                        setStatusLine("floodedRegionsIn() error! " + e.getMessage());
                    }

                    try {
                        double[] elevationExtrema = rt.elevationExtrema();
                        String spacing = " &emsp &emsp ";
                        setElevationExtrema("<html><b> &nbsp Elevation Extrema:</b>" + spacing 
                            + "Lowest Point: <font color = 'red'>" + elevationExtrema[0] 
                            + "</font> meters &nbsp / &nbsp Highest Point: <font color = 'red'>" 
                            + elevationExtrema[1] + "</font> meters</html>");
                    } catch (Throwable e) {
                        setElevationExtrema("<html><b> &nbsp Elevation Extrema:</b> Error! "  
                            + e.getMessage() + "</html>");
                    }

                    try {
                        if (isFloodedRow < 0 || isFloodedRow >= terrain.heights.length 
                            || isFloodedCol < 0 || isFloodedCol >= terrain.heights[0].length)
                            throw new NumberFormatException();

                        boolean isFlooded = rt.isFlooded(waterHeight, new GridLocation(isFloodedRow, isFloodedCol));
                        String res = isFlooded ? "True" : "False";
                        String spacing = " &emsp &emsp &ensp &nbsp ";
                        setIsFlooded("<html><b> &nbsp Is (" + isFloodedCol + ", " + isFloodedRow 
                            + ") Flooded:</b><font color = 'red'>" + spacing + res + "</font></html>");
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(window, 
                            "Please enter a valid integer for the column/row number (column between 0 and " 
                            + (terrain.heights[0].length - 1) + ", row between 0 and " 
                            + (terrain.heights.length - 1) +").", "Is Flooded",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    } catch (Throwable e) {
                        setIsFlooded("<html><b> &nbsp Is (x, y) Flooded:</b> Error! "  
                            + e.getMessage() + "</html>");
                    } 
                    
                    try {
                        double heightAboveWater = rt.heightAboveWater(waterHeight, new GridLocation(isFloodedRow, isFloodedCol));
                        String outputHAW = heightAboveWater < 0 ? "meters below" : "meters above";
                        heightAboveWater = Math.abs(heightAboveWater);

                        String spacing = " &emsp &emsp &ensp &nbsp ";
                        setHeightAboveWater("<html><b> &nbsp Height at (" + isFloodedCol + ", " + isFloodedRow 
                            + "):</b><font color = 'red'>" + spacing + heightAboveWater + "</font> " + outputHAW + " sea level</html>");
                    } catch (Throwable e) {
                        setHeightAboveWater("<html><b> &nbsp Height at (x, y):</b> Error! "  
                            + e.getMessage() + "</html>");
                    } 

                    try {
                        int totalLand = rt.totalVisibleLand(waterHeight);
                        String spacing = " &emsp &emsp &emsp &emsp &emsp ";
                        setTotalLand("<html><b> &nbsp Total Land:</b><font color = 'red'>" + spacing 
                            + totalLand + "</font> cells of land above water</html>");
                    } catch (Throwable e) {
                        setTotalLand("<html><b> &nbsp Total Land:</b> Error! "  
                            + e.getMessage() + "</html>");
                    }

                    try {
                        int landLost = rt.landLost(waterHeight, newWaterHeight);
                        String outputLL = landLost < 0 ? "Will gain" : "Will lose";
                        landLost = Math.abs(landLost);

                        String spacing = " &emsp &emsp &emsp &emsp &ensp &nbsp &nbsp ";
                        setLandLost("<html><b> &nbsp Land Lost:</b>" + spacing 
                           + outputLL + "<font color = 'red'> " + landLost + "</font> cells of land</html>");
                    } catch (Throwable e) {
                        setLandLost("<html><b> &nbsp Land Lost:</b> Error! "  
                            + e.getMessage() + "</html>");
                    }

                    try {
                        int numOfIslands = rt.numOfIslands(waterHeight);
                        String spacing = " &emsp &emsp ";
                        setNumOfIslands("<html><b> &nbsp Number of Islands:</b><font color = 'red'>" + spacing 
                            + numOfIslands + "</font> islands</html>");
                    } catch (Throwable e) {
                        setNumOfIslands("<html><b> &nbsp Number of Islands:</b> Error! "  
                            + e.getMessage() + "</html>");
                    }


                } catch (IOException e) {
                    setStatusLine("Error: " + e.getMessage());
                    throw new RuntimeException(e);
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        setEnabled(controlPanel, true);
                    });
                }
            }
        }.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Cannot set look and feel; falling back on default.");
            }
            new Driver();
        });
    }
}