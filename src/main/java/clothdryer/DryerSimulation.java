package clothdryer;

import clothdryer.DryerState.ProgramStatus;

public class DryerSimulation {

    private static final double HUMIDITY_DECREASE_RATE_COTTON = 0.8;
    private static final double HUMIDITY_DECREASE_RATE_SYNTHETIC = 0.5;
    private static final double HUMIDITY_DECREASE_RATE_WOOL = 0.3;

    private static final double MAX_TEMP_COTTON = 75.0;
    private static final double MAX_TEMP_SYNTHETIC = 60.0;
    private static final double MAX_TEMP_WOOL = 45.0;

    private static final double TEMP_INCREASE_RATE = 2.0;
    private static final double TEMP_DECREASE_RATE = 0.8;

    private static final int TIME_COTTON = 3600;    // 60 Minuten
    private static final int TIME_SYNTHETIC = 2700; // 45 Minuten
    private static final int TIME_WOOL = 1800;      // 30 Minuten

    private final DryerState dryerState;
    private final SafetyModule safetyModule;
    private boolean heatingActive = false;
    private double humidityDecreaseRate = 0.0;
    private double targetTemperature = 0.0;
    private int initialTimeForProgram = 0;

    public DryerSimulation(DryerState dryerState, SafetyModule safetyModule) {
        this.dryerState = dryerState;
        this.safetyModule = safetyModule;
    }

    public void startProgram(String programName) {
        if (!safetyModule.isOperationAllowed()) {
            throw new IllegalStateException("Operation not allowed: Door is open or locked.");
        }
        
        // Complete reset of all state variables
        dryerState.setStatus(ProgramStatus.IDLE);
        dryerState.setTemperature(0.0);
        
        // Always start with 100% humidity regardless of "Load New Laundry" button
        dryerState.setHumidity(100.0);
        
        // Configure program parameters to set the correct duration
        configureProgramParameters(programName);
        
        // Set status to RUNNING after configuration
        dryerState.setStatus(ProgramStatus.RUNNING);
        safetyModule.updateDoorLock();
        
        // Log program start
        dryerState.logEvent(DryerState.EventType.INFO, programName + " program started");
    }

    public void stopProgram() {
        // Heizung ausschalten
        heatingActive = false;
        
        // Status auf FINISHED setzen (oder IDLE, je nach Anforderung)
        dryerState.setStatus(ProgramStatus.IDLE);

        dryerState.setRemainingSeconds(0);
        
        // Türverriegelung aktualisieren
        safetyModule.updateDoorLock();
        
        dryerState.logEvent(DryerState.EventType.INFO, "Program stopped");
    }
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
    

    public void updateState(int elapsedTimeMs) {
        // Wenn das Programm läuft oder fertig ist, Werte aktualisieren
        if (dryerState.getStatus() == ProgramStatus.RUNNING || 
            dryerState.getStatus() == ProgramStatus.IDLE) {
            
            // Bei IDLE keine Heizung mehr
            if (dryerState.getStatus() == ProgramStatus.IDLE) {
                heatingActive = false;
            }
            
            updateTemperature(elapsedTimeMs / 1000.0);
            
            // Nur bei laufendem Programm Feuchtigkeit und Restzeit aktualisieren
            if (dryerState.getStatus() == ProgramStatus.RUNNING) {
                updateHumidity(elapsedTimeMs / 1000.0);
                updateRemainingTime(elapsedTimeMs / 1000.0);
                
                if (safetyModule.isOverheating()) {
                    heatingActive = false;
                    return;
                }
                
                checkProgramFinished();
            }
        }
        
        // Türverriegelung in jedem Fall aktualisieren
        safetyModule.updateDoorLock();
    }

    private void updateTemperature(double elapsedTimeSec) {
        double currentTemp = dryerState.getTemperature();

        if (heatingActive) {
            if (currentTemp < targetTemperature) {
                double newTemp = currentTemp + (TEMP_INCREASE_RATE * elapsedTimeSec);
                dryerState.setTemperature(Math.min(newTemp, targetTemperature));
            }
        } else {
            if (currentTemp > 0) {
                double newTemp = currentTemp - (TEMP_DECREASE_RATE * elapsedTimeSec);
                dryerState.setTemperature(Math.max(0, newTemp));
            }
        }
    }

    private void updateHumidity(double elapsedTimeSec) {
        double currentHumidity = dryerState.getHumidity();

        if (heatingActive && currentHumidity > 0) {
            double newHumidity = currentHumidity - (humidityDecreaseRate * elapsedTimeSec);
            dryerState.setHumidity(Math.max(0, newHumidity));
        }
    }

    private void updateRemainingTime(double elapsedTimeSec) {
        int remainingTime = dryerState.getRemainingSeconds();

        if (remainingTime > 0) {
            double humidityFactor = dryerState.getHumidity() / 100.0;
            int calculatedRemainingTime = (int)(initialTimeForProgram * humidityFactor);
            
            int newRemainingSeconds = Math.min(
                Math.max(0, (int)(remainingTime - elapsedTimeSec)),
                calculatedRemainingTime
            );
            
            dryerState.setRemainingSeconds(newRemainingSeconds);
        }
    }

    private void checkProgramFinished() {
        if (dryerState.getHumidity() <= 5.0 || dryerState.getRemainingSeconds() <= 0) {
            dryerState.setStatus(ProgramStatus.IDLE);
            heatingActive = false;
            safetyModule.updateDoorLock();
        }
    }

    private void adjustRemainingTime(double factor) {
        int currentTime = dryerState.getRemainingSeconds();
        dryerState.setRemainingSeconds((int)(currentTime * factor));
    }

    public boolean tryOpenDoor() {
        return safetyModule.tryOpenDoor();
    }

    public void closeDoor() {
        safetyModule.closeDoor();
    }

    public boolean isDoorLocked() {
        return dryerState.isDoorLocked();
    }

    public boolean isDoorClosed() {
        return dryerState.isDoorClosed();
    }

    public boolean isSafeToOpen() {
        return safetyModule.isSafeToOpen();
    }

        public boolean isHeatingActive() {
        return heatingActive;
    }

    public void setHeatingActive(boolean active) {
        this.heatingActive = active;
    }

    public void setHumidityDecreaseRate(double rate) {
        this.humidityDecreaseRate = rate;
    }

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