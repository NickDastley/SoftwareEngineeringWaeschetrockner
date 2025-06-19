package clothdryer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testklasse für die DryerSimulation
 * 
 * Traceability:
 * - TC-001: Programmauswahl
 * - TC-002: Start- und Stop-Logik
 * - TC-003: Überhitzungsschutz
 * - TC-004: Anzeige Restlaufzeit
 * - TC-005: Feuchtesensorintegration
 * - TC-007: Neue Wäsche einlegen
 * - TC-009: Abkühlfunktion
 */
public class DryerSimulationTest {

    private DryerSimulation simulation;
    private DryerState dryerState;
    private SafetyModule safetyModule;

    @BeforeEach
    void setUp() {
        dryerState = new DryerState();
        safetyModule = new SafetyModule(dryerState);
        simulation = new DryerSimulation(dryerState, safetyModule);
    }

    @Test // TC-001, TC-002
    void testInitialState() {
        assertEquals(DryerState.ProgramStatus.IDLE, dryerState.getStatus(), "Initialer Status sollte IDLE sein");
        assertEquals(0, dryerState.getRemainingSeconds(), "Initiale Restzeit sollte 0 sein");
        assertEquals(0, dryerState.getTemperature(), "Initiale Temperatur sollte 0 sein");
        assertEquals(100.0, dryerState.getHumidity(), "Initiale Feuchtigkeit sollte 100 sein");
    }

    @Test // TC-001, TC-002
    void testStartProgram() {
        simulation.startProgram("cotton");
        assertEquals(DryerState.ProgramStatus.RUNNING, dryerState.getStatus(), "Status sollte nach Start RUNNING sein");
        assertTrue(dryerState.getRemainingSeconds() > 0, "Restzeit sollte nach Start größer als 0 sein");
        assertTrue(dryerState.getHumidity() > 0, "Feuchtigkeit sollte nach Start größer als 0 sein");
    }

    @Test // TC-002
    void testStopProgram() {
        simulation.startProgram("cotton");
        simulation.stopProgram();
        assertEquals(DryerState.ProgramStatus.IDLE, dryerState.getStatus(), "Status sollte nach Stop IDLE sein");
        int remainingSeconds = dryerState.getRemainingSeconds();
        assertEquals(0, remainingSeconds, "Restzeit sollte nach Stop 0 sein");
    }

    @Test // TC-004, TC-005
    void testUpdateState() {
        dryerState.setProgramName("cotton");
        simulation.startProgram("cotton");
        double initialHumidity = dryerState.getHumidity();
        int initialRemainingSeconds = dryerState.getRemainingSeconds();
        simulation.updateState(10000); // 10 s
        assertTrue(dryerState.getRemainingSeconds() < initialRemainingSeconds, "Restzeit sollte nach Update verringert sein");
        assertTrue(dryerState.getHumidity() < initialHumidity, "Feuchtigkeit sollte nach Update verringert sein");
    }

    @Test // TC-003
    void testOverheatingProtection() {
        simulation.startProgram("cotton");
        dryerState.setTemperature(100.0); // Überhitzungsschwelle überschreiten
        simulation.updateState(2000);
        assertEquals(DryerState.ProgramStatus.ERROR, dryerState.getStatus(), "Status sollte bei Überhitzung ERROR sein");
        assertTrue(dryerState.getError().contains("Temperature"), "Fehlermeldung sollte einen Hinweis auf Überhitzung enthalten");
    }

    @Test // TC-001
    void testDifferentPrograms() {
        dryerState.setProgramName("cotton");
        simulation.startProgram("cotton");
        int syntheticTime;
        int woolTime;
        simulation.startProgram("cotton");
        int cottonTime = dryerState.getRemainingSeconds();
        simulation.stopProgram();
        simulation.startProgram("synthetic");
        syntheticTime = dryerState.getRemainingSeconds();
        simulation.stopProgram();
        simulation.startProgram("wool");
        woolTime = dryerState.getRemainingSeconds();
        assertNotEquals(cottonTime, woolTime, "Laufzeit für Baumwolle und Wolle sollte unterschiedlich sein");
        assertNotEquals(syntheticTime, woolTime, "Laufzeit für Synthetik und Wolle sollte unterschiedlich sein");
    }

    @Test // TC-009
    void testProgramCompletion() {
        dryerState.setProgramName("wool");
        simulation.startProgram("wool");
        dryerState.setRemainingSeconds(1);
        dryerState.setHumidity(0.1);
        simulation.updateState(2000);
        assertTrue(DryerState.ProgramStatus.IDLE.equals(dryerState.getStatus())
                || DryerState.ProgramStatus.COOLING.equals(dryerState.getStatus()),
                "Status sollte nach Programmende IDLE oder COOLING sein");
        assertEquals(0, dryerState.getRemainingSeconds(), "Restzeit sollte nach Programmende 0 sein");
    }

    @Test // TC-005
    void testHumiditySensorAffectsRemainingTime() {
        dryerState.setHumidity(80.0);
        dryerState.setProgramName("cotton");
        simulation.startProgram("cotton");
        simulation.setHumidityDecreaseRate(5.0); // Schnellere Trocknung simulieren
        simulation.updateState(10000); // 10 Sekunden simulieren
        int remainingAfterFastDrying = dryerState.getRemainingSeconds();
        simulation.stopProgram();
        dryerState.setHumidity(80.0);
        dryerState.setProgramName("cotton");
        simulation.startProgram("cotton");
        dryerState.setRemainingSeconds(3600);
        simulation.setHumidityDecreaseRate(0.5); // Langsamere Trocknung simulieren
        simulation.updateState(10000); // 10 Sekunden simulieren
        int remainingAfterSlowDrying = dryerState.getRemainingSeconds();
        assertTrue(remainingAfterFastDrying < remainingAfterSlowDrying, "Schnellere Trocknung sollte zu kürzerer Restzeit führen");
    }

    @Test // TC-007
    void testResetSimulationValuesAfterProgramCompletion() {
        simulation.startProgram("cotton");
        dryerState.setTemperature(65.0);
        dryerState.setHumidity(30.0);
        dryerState.setRemainingSeconds(1);
        simulation.updateState(2000); // Simulate enough time to complete
        assertTrue(DryerState.ProgramStatus.COOLING.equals(dryerState.getStatus())
        || DryerState.ProgramStatus.IDLE.equals(dryerState.getStatus()),
                "Program should be COOLING or IDLE after completion");
        simulation.startProgram("synthetic");
        assertEquals(DryerState.ProgramStatus.RUNNING, dryerState.getStatus(),
                "Status should be RUNNING for new program");
        assertTrue(dryerState.getRemainingSeconds() > 0,
                "Remaining time should be properly set for new program");
    }
}