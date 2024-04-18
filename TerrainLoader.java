package tides;

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

/**
 * This class loads the terrain from a provided .terrain file.
 * 
 * @author Original Creator Keith Scharz (NIFTY STANFORD)
 */
public class TerrainLoader {
    private TerrainLoader() {
    }

    /* Interface for receiving progress updates. */
    public interface DownloadNotifier {
        public void onProgress(int bytesRead, int bytesTotal);
    }

    /*
     * Progress monitor channel type. Adapted from this Stack Overflow post:
     * https://stackoverflow.com/a/59667209
     */
    private static final class ReadableConsumerByteChannel implements ReadableByteChannel {
        private final ReadableByteChannel rbc;
        private final DownloadNotifier onRead;

        private int bytesRead;
        private final int totalBytes;
        private int lastPercent = -1;

        public ReadableConsumerByteChannel(ReadableByteChannel rbc, int totalBytes, DownloadNotifier onBytesRead) {
            this.rbc = rbc;
            this.onRead = onBytesRead;
            this.totalBytes = totalBytes == 0 ? 1 : totalBytes;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            int nRead = rbc.read(dst);
            notifyBytesRead(nRead);
            return nRead;
        }

        protected void notifyBytesRead(int nRead) {
            if (nRead <= 0) {
                return;
            }
            bytesRead += nRead;

            int newPct = (int) (100.0 * bytesRead / totalBytes);
            if (newPct != lastPercent) {
                onRead.onProgress(bytesRead, totalBytes);
                lastPercent = newPct;
            }
        }

        @Override
        public boolean isOpen() {
            return rbc.isOpen();
        }

        @Override
        public void close() throws IOException {
            rbc.close();
        }
    }

    public static Terrain loadTerrain(File filename, DownloadNotifier downloadNotifier) throws IOException {
        try (var br = new FileInputStream(filename)) {
            return loadTerrain(br, downloadNotifier);
        }
    }

    /*
     * Reads another line from the Scanner, throwing an IOException if we didn't get
     * enough data.
     */
    private static String nextLine(Scanner s) throws IOException {
        if (!s.hasNextLine())
            throw new IOException("Unexpected end of file.");
        return s.nextLine();
    }

    private static int nextInt(Scanner s) throws IOException {
        if (!s.hasNextInt())
            throw new IOException("Malformed file.");
        return s.nextInt();
    }

    private static double nextDouble(Scanner s) throws IOException {
        if (!s.hasNextDouble())
            throw new IOException("Malformed file.");
        return s.nextDouble();
    }

    private static Terrain loadTerrain(InputStream stream, DownloadNotifier downloadNotifier) throws IOException {
        try (var input = new Scanner(stream)) {
            /* Determine whether this is a local file or whether it's remote. */
            var source = nextLine(input);
            if (!source.equals("local")) {
                return loadWebTerrain(source, downloadNotifier);
            }

            /* Read the terrain size. */
            int numRows = nextInt(input);
            int numCols = nextInt(input);
            var heights = new double[numRows][numCols];

            /* Read the water sources. */
            int numSources = nextInt(input);
            var sources = new GridLocation[numSources];
            for (int i = 0; i < numSources; i++) {
                int row = nextInt(input);
                int col = nextInt(input);
                sources[i] = new GridLocation(row, col);
            }

            /* Read the height data. */
            for (int row = 0; row < numRows; row++) {
                for (int col = 0; col < numCols; col++) {
                    heights[row][col] = nextDouble(input);
                }
            }

            return new Terrain(heights, sources);
        } catch (RuntimeException e) {
            throw new IOException("Error reading terrain.", e);
        }
    }

    /* Sees whether the given key file is the key for the given URL. */
    private static boolean isKeyFor(File keyFile, String source) throws IOException {
        /* Check if the file contents are the URL. */
        BufferedReader url = new BufferedReader(new FileReader(keyFile));
        boolean key = url.readLine().equals(source);
        url.close();
        return key;
    }

    /* Loads the terrain from the given URL. */
    private static Terrain loadWebTerrain(String source, DownloadNotifier downloadNotifier) throws IOException {
        /* Key file: Name is hash, contents are URL. */
        File keyFile = new File("DownloadCache/" + source.hashCode() + ".key");

        /* Data file: Name is hash, contents are actual contents. */
        File dataFile = new File("DownloadCache/" + source.hashCode() + ".data");


        /* If this isn't cached, go cache it. */
        if (!keyFile.exists() || !dataFile.exists() || !isKeyFor(keyFile, source)) {
            /*
             * Otherwise, we need to download this file. These next lines are adapted from
             * https://stackoverflow.com/questions/30405695/java-nio-filechannels-track-
             * progress
             */
            URLConnection connection = new URL(source).openConnection();
            ReadableByteChannel rbc = Channels.newChannel(new URL(source).openStream());
            ReadableConsumerByteChannel rcbc = new ReadableConsumerByteChannel(rbc, connection.getContentLength(),
                    downloadNotifier);
            FileOutputStream fos = new FileOutputStream(dataFile);
            fos.getChannel().transferFrom(rcbc, 0, Long.MAX_VALUE);
            fos.close();

            /* Now, make the key file. */
            try (var pw = new PrintWriter(keyFile)) {
                pw.println(source);
            }
        }

        /* It's now cached, so load it. */
        return loadTerrain(dataFile, downloadNotifier);
    }
}
