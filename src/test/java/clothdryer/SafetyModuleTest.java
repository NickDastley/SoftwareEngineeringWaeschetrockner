package clothdryer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testklasse für das SafetyModule
 *
 * Traceability:
 * - TC-002: Start- und Stop-Logik
 * - TC-003: Überhitzungsschutz
 * - TC-009: Abkühlfunktion
 */
public class SafetyModuleTest {
    
    private DryerState dryerState;
    private SafetyModule safetyModule;
    
    @BeforeEach
    public void setUp() {
        dryerState = new DryerState();
        safetyModule = new SafetyModule(dryerState);
    }
    
    @Test // TC-002
    void testDoorLocksWhenProgramIsRunning() {
        // Setup: Door closed, program not running
        dryerState.setDoorClosed(true);
        dryerState.setDoorLocked(false);
        dryerState.setStatus(DryerState.ProgramStatus.IDLE);
        
        // Action: Start program and update door lock
        dryerState.setStatus(DryerState.ProgramStatus.RUNNING);
        safetyModule.updateDoorLock();
        
        // Verification: Door should be locked
        assertTrue(dryerState.isDoorLocked(), "Door should be locked when program is running");
    }

    @Test // TC-002
    void testDoorCannotBeOpenedWhenLocked() {
        // Setup: Door closed and locked
        dryerState.setDoorClosed(true);
        dryerState.setDoorLocked(true);
        
        // Action: Attempt to open the door
        boolean doorOpened = safetyModule.tryOpenDoor();
        
        // Verification: Door cannot be opened and remains closed
        assertFalse(doorOpened, "Opening the door should fail when it is locked");
        assertTrue(dryerState.isDoorClosed(), "Door should remain closed when it is locked");
    }

    @Test // TC-009
    void testDoorCanBeOpenedWhenTemperatureBelowThreshold() {
        // Setup: Door closed, not locked, temperature below threshold
        dryerState.setDoorClosed(true);
        dryerState.setDoorLocked(false);
        dryerState.setTemperature(35.0); // Below threshold of 40.0
        
        // Verification: Door can be safely opened
        assertTrue(safetyModule.isSafeToOpen(), "Door should be safe to open when temperature is below 40°C");
        
        // Action: Open door
        boolean doorOpened = safetyModule.tryOpenDoor();
        
        // Verification: Door was opened
        assertTrue(doorOpened, "Door should be able to open");
        assertFalse(dryerState.isDoorClosed(), "Door should not be closed after opening");
    }

    @Test // TC-003
    void testOverheatingDetection() {
        // Setup: Temperature below overheating threshold
        dryerState.setTemperature(90.0);
        
        // Verification: No overheating
        assertFalse(safetyModule.isOverheating(), "At 90°C, no overheating should be detected");
        
        // Action: Temperature above overheating threshold
        dryerState.setTemperature(105.0);
        
        // Verification: Overheating detected
        assertTrue(safetyModule.isOverheating(), "At 105°C, overheating should be detected");
        assertEquals(DryerState.ProgramStatus.ERROR, dryerState.getStatus(), "Status should be ERROR after overheating");
    }
    
    @Test // TC-002
    void testOperationAllowedOnlyWithClosedDoor() {
        // Setup: Door open
        dryerState.setDoorClosed(false);
        
        // Verification: Operation not allowed
        assertFalse(safetyModule.isOperationAllowed(), "Operation should not be allowed when door is open");
        
        // Action: Close door
        safetyModule.closeDoor();
        
        // Verification: Operation allowed
        assertTrue(safetyModule.isOperationAllowed(), "Operation should be allowed when door is closed");
    }
    
    @Test // TC-002
    void testDoorStatusChangeUpdatesProgram() {
        // Setup: Door closed, status IDLE
        dryerState.setDoorClosed(true);
        dryerState.setStatus(DryerState.ProgramStatus.IDLE);
        
        // Action: Open door
        safetyModule.tryOpenDoor();
        
        // Verification: Status should be DOOR_OPEN
        assertEquals(DryerState.ProgramStatus.DOOR_OPEN, dryerState.getStatus(),
                "Status should change to DOOR_OPEN when door is opened");
        
        // Action: Close door again
        safetyModule.closeDoor();
        
        // Verification: Status should return to IDLE
        assertEquals(DryerState.ProgramStatus.IDLE, dryerState.getStatus(),
                "Status should return to IDLE when door is closed");
    }
}