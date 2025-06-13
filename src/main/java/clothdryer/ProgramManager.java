package clothdryer;

public class ProgramManager implements Runnable {

    private final DryerState state;
    private final DryerSimulation simulation;
    private long lastUpdateTime;
    private final SafetyModule safetyModule;

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

    private void refreshState() {
        long currentTime = System.currentTimeMillis();
        int elapsedMilliseconds = (int) (currentTime - lastUpdateTime);

        simulation.updateState(elapsedMilliseconds);
        lastUpdateTime = currentTime;
    }

    public boolean tryOpenDoor() {
        return simulation.tryOpenDoor();
    }

    public void closeDoor() {
        simulation.closeDoor();
    }

    public boolean isDoorLocked() {
        return simulation.isDoorLocked();
    }

    public boolean isDoorClosed() {
        return simulation.isDoorClosed();
    }
}

