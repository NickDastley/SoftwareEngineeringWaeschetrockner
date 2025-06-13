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
        return dryerState.isDoorClosed();
    }

    /**
     * Verriegelt die Tür, wenn ein Programm läuft.
     */
    public void updateDoorLock() {
        boolean shouldBeLocked = dryerState.getStatus() == DryerState.ProgramStatus.RUNNING;
        dryerState.setDoorLocked(shouldBeLocked);
    }

    /**
     * Versucht die Tür zu öffnen.
     * @return true, wenn die Tür geöffnet werden konnte, sonst false
     */
    public boolean tryOpenDoor() {
        if (dryerState.isDoorLocked()) {
            return false;
        }

        dryerState.setDoorClosed(false);
        if (dryerState.getStatus() != DryerState.ProgramStatus.ERROR) {
            dryerState.setStatus(DryerState.ProgramStatus.DOOR_OPEN);
        }
        return true;
    }

    public void closeDoor() {
        dryerState.setDoorClosed(true);
        if (dryerState.getStatus() == DryerState.ProgramStatus.DOOR_OPEN) {
            dryerState.setStatus(DryerState.ProgramStatus.IDLE);
        }
    }

    public boolean isSafeToOpen() {
        return dryerState.getTemperature() < SAFE_DOOR_TEMPERATURE;
    }

    public boolean isOverheating() {
        return dryerState.getTemperature() > OVERHEAT_THRESHOLD;
    }
}