package clothdryer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * DryerState holds the current state of the dryer, including program name,
 * status, temperature, humidity, door state, error messages, and event history.
 * It provides synchronized access to all state variables and methods for
 * logging and retrieving events and errors.
 */
public class DryerState {

    private static final Logger LOGGER = Logger.getLogger(DryerState.class.getName());
    private static FileHandler fileHandler;

    static {
        try {
            // Create logs directory if it does not exist
            Files.createDirectories(Path.of("logs"));
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            fileHandler = new FileHandler("logs/dryerlog_" + timestamp + ".log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Could not initialize log file: " + e.getMessage());
        }
    }

    private String programName = "None";

    /**
     * Enum representing the possible program statuses.
     */
    public enum ProgramStatus {
        IDLE, RUNNING, COOLING, ERROR, DOOR_OPEN
    }
    
    /**
     * Enum representing the type of event for logging.
     */
    public enum EventType {
        INFO, WARNING, ERROR
    }
    
    private ProgramStatus status;
    private int remainingSeconds = 0;
    private double temperature = 0.0;
    private double humidity = 100.0;
    private boolean doorClosed = true;
    private boolean doorLocked = false;
    
    // Error and event handling
    private String currentError = null;
    private static final int MAX_EVENT_HISTORY = 100;
    private final List<DryerEvent> eventHistory = new ArrayList<>();
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructs a DryerState with default values (IDLE, 100% humidity, door closed).
     */
    public DryerState() {
        status = ProgramStatus.IDLE;
    }

    // Synchronized getters and setters for all state variables

    public synchronized String getProgramName() {
        return programName;
    }

    public synchronized void setProgramName(String programName) {
        this.programName = programName;
    }

    public synchronized ProgramStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(ProgramStatus status) {
        this.status = status;
    }

    public synchronized int getRemainingSeconds() {
        return remainingSeconds;
    }

    public synchronized void setRemainingSeconds(int seconds) {
        this.remainingSeconds = seconds;
    }

    public synchronized double getTemperature() {
        return temperature;
    }

    public synchronized void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public synchronized double getHumidity() {
        return humidity;
    }

    public synchronized void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public synchronized boolean isDoorClosed() {
        return doorClosed;
    }

    public synchronized void setDoorClosed(boolean doorClosed) {
        this.doorClosed = doorClosed;
    }

    public synchronized void setDoorLocked(boolean doorLocked) {
        this.doorLocked = doorLocked;
    }

    public synchronized boolean isDoorLocked() {
        return doorLocked;
    }
    
    // Error and event handling methods

    /**
     * Gets the current error message if there is one.
     * @return The current error message or null if no error
     */
    public synchronized String getError() {
        return currentError;
    }
    
    /**
     * Sets the current error and logs it to the event history.
     * Also sets the status to ERROR.
     * @param error The error message
     */
    public synchronized void setError(String error) {
        this.currentError = error;
        if (error != null) {
            logEvent(EventType.ERROR, error);
            setStatus(ProgramStatus.ERROR);
        }
    }
    
    /**
     * Clears the current error.
     */
    public synchronized void clearError() {
        this.currentError = null;
    }
    
    /**
     * Logs an event to the event history and to the log file.
     * @param type The type of event (INFO, WARNING, ERROR)
     * @param message The event message
     */
    public synchronized void logEvent(EventType type, String message) {
        DryerEvent event = new DryerEvent(type, message);
        eventHistory.add(event);

        // Limit the event history size
        if (eventHistory.size() > MAX_EVENT_HISTORY) {
            eventHistory.remove(0);
        }

        // Log to file
        switch (type) {
            case INFO -> LOGGER.info(message);
            case WARNING -> LOGGER.warning(message);
            case ERROR -> LOGGER.severe(message);
        }
    }
    
    /**
     * Gets the event history as an unmodifiable list.
     * @return The event history
     */
    public synchronized List<DryerEvent> getEventHistory() {
        return Collections.unmodifiableList(eventHistory);
    }
    
    /**
     * Gets the most recent events up to the specified count.
     * @param count The maximum number of events to return
     * @return The most recent events
     */
    public synchronized List<DryerEvent> getRecentEvents(int count) {
        int startIndex = Math.max(0, eventHistory.size() - count);
        return Collections.unmodifiableList(
            eventHistory.subList(startIndex, eventHistory.size())
        );
    }
    
    /**
     * Class representing a dryer event, including type, message, and timestamp.
     */
    public static class DryerEvent {
        private final EventType type;
        private final String message;
        private final LocalDateTime timestamp;
        
        public DryerEvent(EventType type, String message) {
            this.type = type;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }
        
        public EventType getType() {
            return type;
        }
        
        public String getMessage() {
            return message;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s: %s", 
                timestamp.format(timeFormatter),
                type.toString(), 
                message);
        }
    }
}
