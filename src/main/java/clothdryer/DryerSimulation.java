package clothdryer;

import clothdryer.DryerState.ProgramStatus;

/**
 * DryerSimulation simulates the drying process for different laundry programs.
 * It manages temperature, humidity, remaining time, and interacts with the SafetyModule
 * to ensure safe operation. The simulation updates the dryer state based on the selected
 * program and elapsed time.
 */
public class DryerSimulation {

    private static final double HUMIDITY_DECREASE_RATE_COTTON = 0.8;
    private static final double HUMIDITY_DECREASE_RATE_SYNTHETIC = 0.5;
    private static final double HUMIDITY_DECREASE_RATE_WOOL = 0.3;

    private static final double MAX_TEMP_COTTON = 75.0;
    private static final double MAX_TEMP_SYNTHETIC = 60.0;
    private static final double MAX_TEMP_WOOL = 45.0;

    private static final double TEMP_INCREASE_RATE = 2.0;
    private static final double TEMP_DECREASE_RATE = 0.8;
    private static final double TEMP_COOLING_RATE = 3.0; // Fast cooling

    private static final int TIME_COTTON = 3600;    // 60 minutes
    private static final int TIME_SYNTHETIC = 2700; // 45 minutes
    private static final int TIME_WOOL = 1800;      // 30 minutes

    private final DryerState dryerState;
    private final SafetyModule safetyModule;
    private boolean heatingActive = false;
    private double humidityDecreaseRate = 0.0;
    private double targetTemperature = 0.0;
    private int initialTimeForProgram = 0;

    private double previousHumidity;
    private double previousUpdateTime;
    private static final double TARGET_HUMIDITY = 5.0; // Program finishes when humidity reaches this level

    /**
     * Constructs a DryerSimulation with the given dryer state and safety module.
     * @param dryerState the state object representing the dryer's current state
     * @param safetyModule the safety module for door and overheating checks
     */
    public DryerSimulation(DryerState dryerState, SafetyModule safetyModule) {
        this.dryerState = dryerState;
        this.safetyModule = safetyModule;
    }

    /**
     * Starts a drying program by configuring parameters and setting the state to RUNNING.
     * @param programName the name of the selected program ("cotton", "synthetic", "wool")
     */
    public void startProgram(String programName) {
        if (!safetyModule.isOperationAllowed()) {
            throw new IllegalStateException("Operation not allowed: Door is open or locked.");
        }

        // Remove this line to keep the current temperature:
        // dryerState.setTemperature(0.0);

        // Configure program parameters to set the correct duration
        configureProgramParameters(programName);

        // Set status to RUNNING after configuration
        dryerState.setStatus(ProgramStatus.RUNNING);
        safetyModule.updateDoorLock();

        // Log program start
        dryerState.logEvent(DryerState.EventType.INFO, programName + " program started");
    }

    /**
     * Stops the current program, disables heating, and sets the status to IDLE.
     */
    public void stopProgram() {
        // Turn off heating
        heatingActive = false;
        
        // Set status to IDLE
        dryerState.setStatus(ProgramStatus.IDLE);

        dryerState.setRemainingSeconds(0);
        
        // Update door lock status
        safetyModule.updateDoorLock();
        
        dryerState.logEvent(DryerState.EventType.INFO, "Program stopped");
    }

    /**
     * Configures simulation parameters for the selected program.
     * @param programName the name of the program
     */
    private void configureProgramParameters(String programName) {
        switch (programName) {
            case "cotton" -> {
                humidityDecreaseRate = HUMIDITY_DECREASE_RATE_COTTON;
                targetTemperature = MAX_TEMP_COTTON;
                dryerState.setRemainingSeconds(TIME_COTTON);
                initialTimeForProgram = TIME_COTTON;
            }
            case "synthetic" -> {
                humidityDecreaseRate = HUMIDITY_DECREASE_RATE_SYNTHETIC;
                targetTemperature = MAX_TEMP_SYNTHETIC;
                dryerState.setRemainingSeconds(TIME_SYNTHETIC);
                initialTimeForProgram = TIME_SYNTHETIC;
            }
            case "wool" -> {
                humidityDecreaseRate = HUMIDITY_DECREASE_RATE_WOOL;
                targetTemperature = MAX_TEMP_WOOL;
                dryerState.setRemainingSeconds(TIME_WOOL);
                initialTimeForProgram = TIME_WOOL;
            }
        }
        
        // Make sure remaining seconds is set to program duration
        dryerState.setProgramName(programName);
        heatingActive = true;
    }
    
    /**
     * Updates the simulation state based on elapsed time.
     * @param elapsedTimeMs elapsed time in milliseconds since last update
     */
    public void updateState(int elapsedTimeMs) {
        // If the program is running or idle, update values
        if (dryerState.getStatus() == ProgramStatus.RUNNING || 
            dryerState.getStatus() == ProgramStatus.IDLE) {
            
            // No heating when idle
            if (dryerState.getStatus() == ProgramStatus.IDLE) {
                heatingActive = false;
            }
            
            // Always update temperature, even when idle, to allow cooling
            updateTemperature(elapsedTimeMs / 1000.0);

            if (dryerState.getStatus() == ProgramStatus.RUNNING) {
                updateHumidity(elapsedTimeMs / 1000.0);
                updateRemainingTime(elapsedTimeMs / 1000.0);
                
                if (safetyModule.isOverheating()) {
                    heatingActive = false;
                    return;
                }
                
                boolean programFinished = checkProgramFinished();
                if (programFinished) {
                    dryerState.setStatus(ProgramStatus.COOLING);
                    heatingActive = false;
                    safetyModule.updateDoorLock();
                }
            } else if (dryerState.getStatus() == ProgramStatus.COOLING) {
                if (dryerState.getTemperature() > SafetyModule.SAFE_DOOR_TEMPERATURE) {
                    dryerState.setDoorLocked(true);
                } else {
                    dryerState.setDoorLocked(false);
                    dryerState.setStatus(ProgramStatus.IDLE);
                }
            }
        }
        
        // Always update door lock status
        safetyModule.updateDoorLock();
    }

    /**
     * Updates the temperature based on heating state and elapsed time.
     * @param elapsedTimeSec elapsed time in seconds
     */
    private void updateTemperature(double elapsedTimeSec) {
        double currentTemp = dryerState.getTemperature();

        if (heatingActive) {
            if (currentTemp < targetTemperature) {
                double newTemp = currentTemp + (TEMP_INCREASE_RATE * elapsedTimeSec);
                dryerState.setTemperature(Math.min(newTemp, targetTemperature));
            }
        } else {
            if (currentTemp > 0) {
                double coolingRate = dryerState.getStatus() == DryerState.ProgramStatus.COOLING
                    ? TEMP_COOLING_RATE
                    : TEMP_DECREASE_RATE;
                double newTemp = currentTemp - (coolingRate * elapsedTimeSec);
                dryerState.setTemperature(Math.max(0, newTemp));
            }
        }
    }

    /**
     * Updates the humidity based on heating state and elapsed time.
     * @param elapsedTimeSec elapsed time in seconds
     */
    private void updateHumidity(double elapsedTimeSec) {
        double currentHumidity = dryerState.getHumidity();

        if (heatingActive && currentHumidity > 0) {
            double newHumidity = currentHumidity - (humidityDecreaseRate * elapsedTimeSec);
            dryerState.setHumidity(Math.max(0, newHumidity));
        }
    }

    /**
     * Updates the remaining time estimate based on humidity decrease and elapsed time.
     * @param elapsedTimeSec elapsed time in seconds
     */
    private void updateRemainingTime(double elapsedTimeSec) {
        double currentTime = System.currentTimeMillis() / 1000.0;
        double currentHumidity = dryerState.getHumidity();
        
        // Initialize values if needed
        if (previousUpdateTime == 0) {
            previousUpdateTime = currentTime;
            previousHumidity = currentHumidity;
            
            // For first call, just decrease time linearly
            int remainingTime = dryerState.getRemainingSeconds();
            dryerState.setRemainingSeconds(Math.max(0, (int)(remainingTime - elapsedTimeSec)));
        }
        
        double timeDelta = currentTime - previousUpdateTime;
        double humidityDelta = previousHumidity - currentHumidity;
        
        // Calculate drying rate (humidity decrease per second)
        double dryingRate = (timeDelta > 0) ? humidityDelta / timeDelta : 0;
        
        // Only update estimate if we have a positive drying rate
        if (dryingRate > 0) {
            // Calculate remaining time based on current drying rate
            double remainingHumidityToRemove = currentHumidity - TARGET_HUMIDITY;
            int estimatedRemainingSeconds = (int)(remainingHumidityToRemove / dryingRate);
            
            // Set a floor and ceiling to avoid extreme values
            estimatedRemainingSeconds = Math.max(5, estimatedRemainingSeconds);
            estimatedRemainingSeconds = Math.min(initialTimeForProgram, estimatedRemainingSeconds);
            
            dryerState.setRemainingSeconds(estimatedRemainingSeconds);
        } else {
            // If no drying progress, just decrease time linearly
            int remainingTime = dryerState.getRemainingSeconds();
            dryerState.setRemainingSeconds(Math.max(0, (int)(remainingTime - elapsedTimeSec)));
        }
        
        // Update values for next calculation
        previousUpdateTime = currentTime;
        previousHumidity = currentHumidity;
        
        // Check if program should finish based on humidity
        if (currentHumidity <= TARGET_HUMIDITY) {
            checkProgramFinished();
        }
    }

    /**
     * Checks if the program should finish based on humidity or time.
     * Sets status to IDLE and disables heating if finished.
     */
    private boolean checkProgramFinished() {
        if (dryerState.getHumidity() <= TARGET_HUMIDITY || dryerState.getRemainingSeconds() <= 0) {
            dryerState.setStatus(ProgramStatus.IDLE);
            heatingActive = false;
            safetyModule.updateDoorLock();
            return true;
        }
        return false;
    }

    public boolean tryOpenDoor() {
        return safetyModule.tryOpenDoor();
    }

    /**
     * Closes the door using the safety module.
     */
    public void closeDoor() {
        safetyModule.closeDoor();
    }

    /**
     * @return true if the door is locked
     */
    public boolean isDoorLocked() {
        return dryerState.isDoorLocked();
    }

    /**
     * @return true if the door is closed
     */
    public boolean isDoorClosed() {
        return dryerState.isDoorClosed();
    }

    /**
     * @return true if it is safe to open the door
     */
    public boolean isSafeToOpen() {
        return safetyModule.isSafeToOpen();
    }

    /**
     * @return true if heating is currently active
     */
    public boolean isHeatingActive() {
        return heatingActive;
    }

    /**
     * Sets the heating active state.
     * @param active true to activate heating, false to deactivate
     */
    public void setHeatingActive(boolean active) {
        this.heatingActive = active;
    }

    /**
     * Sets the humidity decrease rate for the simulation.
     * @param rate the new humidity decrease rate
     */
    public void setHumidityDecreaseRate(double rate) {
        this.humidityDecreaseRate = rate;
    }

    /**
     * Loads new laundry if the door is open, resetting humidity to 100%.
     * Logs an event if successful or if the door is closed.
     */
    public void loadNewLaundry() {
        if (!dryerState.isDoorClosed()) {
            // Reset humidity to 100% for a new load of clothes
            dryerState.setHumidity(100.0);
            dryerState.logEvent(DryerState.EventType.INFO, "New laundry loaded");
        } else {
            dryerState.logEvent(DryerState.EventType.WARNING, "Cannot load new laundry while door is closed");
        }
    }
}