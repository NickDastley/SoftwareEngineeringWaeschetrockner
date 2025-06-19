package clothdryer;

/**
 * SafetyModule handles all safety-related checks and actions for the dryer.
 * <p>
 * It manages door locking/unlocking, checks if operation is allowed,
 * ensures the door can only be opened at safe temperatures, and detects overheating.
 */
public class SafetyModule {
    private final DryerState dryerState;
    private static final double OVERHEAT_THRESHOLD = 100.0;
    public static final double SAFE_DOOR_TEMPERATURE = 40.0;

    /**
     * Constructs a SafetyModule for the given dryer state.
     * @param dryerState the DryerState instance to monitor and control
     */
    public SafetyModule(DryerState dryerState) {
        this.dryerState = dryerState;
    }

    /**
     * Checks if the dryer can be safely operated (e.g., door must be closed).
     * Logs a warning if not allowed.
     * @return true if operation is allowed, false otherwise
     */
    public boolean isOperationAllowed() {
        boolean allowed = dryerState.isDoorClosed();
        if (!allowed) {
            dryerState.logEvent(DryerState.EventType.WARNING, "Operation not allowed: Door is open");
        }
        return allowed;
    }

    /**
     * Locks the door when a program is running, unlocks it otherwise.
     * Logs the locking/unlocking events.
     */
    public void updateDoorLock() {
        boolean shouldBeLocked = dryerState.getStatus() == DryerState.ProgramStatus.RUNNING;
        boolean wasLocked = dryerState.isDoorLocked();
        dryerState.setDoorLocked(shouldBeLocked);
        
        if (shouldBeLocked && !wasLocked) {
            dryerState.logEvent(DryerState.EventType.INFO, "Door locked for program execution");
        } else if (!shouldBeLocked && wasLocked) {
            dryerState.logEvent(DryerState.EventType.INFO, "Door unlocked");
        }
    }

    /**
     * Attempts to open the door.
     * Only possible if the door is not locked and the temperature is safe.
     * Logs the result.
     * @return true if the door was opened, false otherwise
     */
    public boolean tryOpenDoor() {
        if (dryerState.isDoorLocked()) {
            dryerState.logEvent(DryerState.EventType.WARNING, "Cannot open door: Door is locked");
            return false;
        }

        if (!isSafeToOpen()) {
            dryerState.logEvent(DryerState.EventType.WARNING, "Cannot open door: Temperature too high (" + 
                dryerState.getTemperature() + "°C)");
            return false;
        }

        dryerState.setDoorClosed(false);
        if (dryerState.getStatus() != DryerState.ProgramStatus.ERROR) {
            dryerState.setStatus(DryerState.ProgramStatus.DOOR_OPEN);
        }
        dryerState.logEvent(DryerState.EventType.INFO, "Door opened");
        return true;
    }

    /**
     * Closes the door and sets the status to IDLE if it was DOOR_OPEN.
     * Logs the event.
     */
    public void closeDoor() {
        dryerState.setDoorClosed(true);
        if (dryerState.getStatus() == DryerState.ProgramStatus.DOOR_OPEN) {
            dryerState.setStatus(DryerState.ProgramStatus.IDLE);
        }
        dryerState.logEvent(DryerState.EventType.INFO, "Door closed");
    }

    /**
     * Checks if it is safe to open the door (temperature below threshold).
     * @return true if temperature is safe, false otherwise
     */
    public boolean isSafeToOpen() {
        return dryerState.getTemperature() < SAFE_DOOR_TEMPERATURE;
    }

    /**
     * Checks if the dryer is overheating.
     * If overheating, sets status to ERROR and logs the error.
     * @return true if overheating, false otherwise
     */
    public boolean isOverheating() {
        boolean isOverheating = dryerState.getTemperature() >= OVERHEAT_THRESHOLD;
        if (isOverheating) {
            dryerState.setStatus(DryerState.ProgramStatus.ERROR);
            dryerState.setError("Overheating detected! Temperature: " + dryerState.getTemperature());
        }
        return isOverheating;
    }
}