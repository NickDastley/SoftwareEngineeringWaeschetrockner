package clothdryer;

/**
 * ProgramManager coordinates the simulation and state of the dryer.
 * <p>
 * It manages the main simulation loop in a background thread, handles
 * starting and stopping drying programs, door operations, and provides
 * access to the current dryer state for the GUI.
 */
public class ProgramManager implements Runnable {

    private final DryerState state;
    private final DryerSimulation simulation;
    private long lastUpdateTime;
    private final SafetyModule safetyModule;

    /**
     * Constructs a new ProgramManager, initializing the state, safety module, and simulation.
     */
    public ProgramManager() {
        this.state = new DryerState();
        this.safetyModule = new SafetyModule(state);
        this.simulation = new DryerSimulation(state, safetyModule);
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Starts a drying program based on the selected program name.
     *
     * @param programName The name of the drying program to start.
     */
    public void startProgram(String programName) {
        if (state.getStatus() == DryerState.ProgramStatus.IDLE) {
            simulation.startProgram(programName);
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    /**
     * Stops the currently running drying program.
     */
    public void stopProgram() {
        if (state.getStatus() == DryerState.ProgramStatus.RUNNING) {
            simulation.stopProgram();
        }
    }

    /**
     * Returns the current state of the dryer.
     *
     * @return The current DryerState object.
     */
    public DryerState getState() {
        return state;
    }

    /**
     * Main loop for the simulation thread.
     * Periodically updates the simulation state.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            refreshState();
            try {
                Thread.sleep(1000); // refresh every second
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Updates the simulation state based on elapsed time.
     */
    private void refreshState() {
        long currentTime = System.currentTimeMillis();
        int elapsedMilliseconds = (int) (currentTime - lastUpdateTime);

        simulation.updateState(elapsedMilliseconds);
        lastUpdateTime = currentTime;
    }

    /**
     * Attempts to open the dryer door.
     * @return true if the door was opened, false otherwise
     */
    public boolean tryOpenDoor() {
        return simulation.tryOpenDoor();
    }

    /**
     * Closes the dryer door.
     */
    public void closeDoor() {
        simulation.closeDoor();
    }

    /**
     * @return true if the door is locked
     */
    public boolean isDoorLocked() {
        return simulation.isDoorLocked();
    }

    /**
     * @return true if the door is closed
     */
    public boolean isDoorClosed() {
        return simulation.isDoorClosed();
    }

    /**
     * Simulates loading new laundry, resetting humidity values.
     * @return true if successful, false if door is closed
     */
    public boolean loadNewLaundry() {
        if (!isDoorClosed()) {
            simulation.loadNewLaundry();
            return true;
        }
        return false;
    }
}

