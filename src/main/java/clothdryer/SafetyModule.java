package clothdryer;

public class SafetyModule {
    private final DryerState dryerState;
    private static final double OVERHEAT_THRESHOLD = 100.0;
    private static final double SAFE_DOOR_TEMPERATURE = 40.0;

    public SafetyModule(DryerState dryerState) {
        this.dryerState = dryerState;
    }

    /**
     * Prüft, ob der Trockner sicher betrieben werden kann.
     * @return true, wenn der Betrieb sicher ist, sonst false
     */
    public boolean isOperationAllowed() {
        boolean allowed = dryerState.isDoorClosed();
        if (!allowed) {
            dryerState.logEvent(DryerState.EventType.WARNING, "Operation not allowed: Door is open");
        }
        return allowed;
    }

    /**
     * Verriegelt die Tür, wenn ein Programm läuft.
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
     * Versucht die Tür zu öffnen.
     * @return true, wenn die Tür geöffnet werden konnte, sonst false
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

    public void closeDoor() {
        dryerState.setDoorClosed(true);
        if (dryerState.getStatus() == DryerState.ProgramStatus.DOOR_OPEN) {
            dryerState.setStatus(DryerState.ProgramStatus.IDLE);
        }
        dryerState.logEvent(DryerState.EventType.INFO, "Door closed");
    }

    public boolean isSafeToOpen() {
        return dryerState.getTemperature() < SAFE_DOOR_TEMPERATURE;
    }

    public boolean isOverheating() {
        boolean isOverheating = dryerState.getTemperature() >= OVERHEAT_THRESHOLD;
        if (isOverheating) {
            dryerState.setStatus(DryerState.ProgramStatus.ERROR);
            dryerState.setError("Overheating detected! Temperature: " + dryerState.getTemperature());
        }
        return isOverheating;
    }
}