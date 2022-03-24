package at.kurumi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A log file tailer that notifies registered listeners about new content in the tailed fail.
 */
public class Tailer implements Runnable {

    /**
     * Defines whether the log file tailer should include the entire contents
     * of the exising log file or tail from the end of the file when the tailer starts
     */
    private static final boolean START_AT_BEGINNING = false;
    /**
     * File read interval in milliseconds.
     */
    private static final long INTERVAL = 1000;

    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    private static final Map<String, Tailer> tailers = new HashMap<>();
    private static final Map<String, ScheduledFuture<?>> tailerFutures = new HashMap<>();

    private final Set<TailListener> listeners = new HashSet<>();
    private final File logfile;
    private final Logger LOG;

    private RandomAccessFile randomAccessFile = null;
    private long tailPosition;

    public Tailer(File file) {
        this.logfile = file;
        tailPosition = START_AT_BEGINNING ? 0 : logfile.length();
        LOG = LogManager.getLogger("Tailer<" + file.getName() + ">");


    }

    public static void startTail(File file, TailListener listener) {
        new Tailer(file).addListener(listener);
    }

    public static void stopTail(File file, TailListener listener) {
        final var fileName = file.getName();
        final var tailer = tailers.get(fileName);
        if(tailer != null) {
            tailer.removeListener(listener);
        }
    }

    /**
     * Registers a new listener. If this is the first listener, the Tailer will attempt to open the file. If opening
     * fails, the listener is not added.
     *
     * @param listener a new listener
     */
    private void addListener(TailListener listener) {
        if(listeners.isEmpty()) {
            try {
                randomAccessFile = new RandomAccessFile(logfile, "r");
                listeners.add(listener);

                final var fileName = logfile.getName();
                tailers.put(fileName, this);

                final var future = EXECUTOR_SERVICE
                        .scheduleAtFixedRate(this, 0, INTERVAL, TimeUnit.MILLISECONDS);
                tailerFutures.put(fileName, future);
            } catch (FileNotFoundException e) {
                LOG.error("Failed to find target file");
                LOG.debug(e.getMessage());
            }
        } else {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener. If this is the only remaining listener, the Tailer will attempt to close the file.
     *
     * @param listener a listener
     */
    private void removeListener(TailListener listener) {
        listeners.remove(listener);
        if(listeners.isEmpty()) {
            close();
        }
    }

    /**
     * Close the file and stop scheduled execution.
     */
    private void close() {
        try {
            randomAccessFile.close();
            final var fileName = logfile.getName();
            tailerFutures.remove(fileName).cancel(true);
            tailers.remove(fileName);
        } catch (IOException e) {
            LOG.error("Failed to close file");
            LOG.debug(e.getMessage());
        }
    }

    private void publish(String content) {
        listeners.forEach(listener -> listener.receive(content));
    }

    /**
     * If a file is currently opened, new content will be read and published once.
     */
    public void run() {
        try {
            // Compare the length of the file to the file pointer
            final long fileLength = this.logfile.length();

            if (fileLength < tailPosition) {
                LOG.info("Log file appears to have been rotated or deleted. Reopening.");
                randomAccessFile = new RandomAccessFile(logfile, "r");
                tailPosition = 0;
                LOG.info("Log file reopened.");
            }

            if (fileLength > tailPosition) {
                // There is data to read
                randomAccessFile.seek(tailPosition);
                var line = randomAccessFile.readLine();
                while (line != null) {
                    publish(line);
                    line = randomAccessFile.readLine();
                }
                tailPosition = randomAccessFile.getFilePointer();
            }
        } catch (FileNotFoundException e) {
            LOG.error("Failed to find target file after automatic re-open.");
            LOG.debug(e.getMessage());
        } catch (IOException e) {
            LOG.error("Failed to jump to position in file");
            LOG.debug(e.getMessage());
        }
    }
}
