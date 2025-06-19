package clothdryer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testklasse für die DryerSimulation
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

    @Test
    void testInitialState() {
        assertEquals(DryerState.ProgramStatus.IDLE, dryerState.getStatus(), "Initialer Status sollte IDLE sein");
        assertEquals(0, dryerState.getRemainingSeconds(), "Initiale Restzeit sollte 0 sein");
        assertEquals(0, dryerState.getTemperature(), "Initiale Temperatur sollte 0 sein");
        assertEquals(100.0, dryerState.getHumidity(), "Initiale Feuchtigkeit sollte 100 sein");
    }

    @Test
    void testStartProgram() {
        // Programm starten
        // Programm starten
        simulation.startProgram("cotton");

        // Überprüfen, ob der Status auf RUNNING gesetzt wurde
        assertEquals(DryerState.ProgramStatus.RUNNING, dryerState.getStatus(), "Status sollte nach Start RUNNING sein");

        // Überprüfen, ob die Restzeit für Baumwolle korrekt gesetzt wurde
        assertTrue(dryerState.getRemainingSeconds() > 0, "Restzeit sollte nach Start größer als 0 sein");

        // Überprüfen, ob die initiale Feuchtigkeit gesetzt wurde
        assertTrue(dryerState.getHumidity() > 0, "Feuchtigkeit sollte nach Start größer als 0 sein");
    }

    @Test
    void testStopProgram() {
        // Programm starten und dann stoppen
        simulation.startProgram("cotton");
        simulation.stopProgram();

        assertEquals(DryerState.ProgramStatus.IDLE, dryerState.getStatus(), "Status sollte nach Stop IDLE sein");

        int remainingSeconds = dryerState.getRemainingSeconds();
        // Überprüfen, ob die Restzeit zurückgesetzt wurde
        assertEquals(0, remainingSeconds, "Restzeit sollte nach Stop 0 sein");
    }

    @Test
    void testUpdateState() {
        // Programm starten
        dryerState.setProgramName("cotton");
        simulation.startProgram("cotton");

        // Ursprüngliche Werte speichern
        double initialHumidity = dryerState.getHumidity();
        int initialRemainingSeconds = dryerState.getRemainingSeconds();

        // Simulation für 10 Sekunden laufen lassen
        simulation.updateState(10000); // 10000 ms = 10 s

        // Überprüfen, ob die Restzeit verringert wurde
        assertTrue(dryerState.getRemainingSeconds() < initialRemainingSeconds,
                "Restzeit sollte nach Update verringert sein");

        // Überprüfen, ob die Feuchtigkeit verringert wurde
        assertTrue(dryerState.getHumidity() < initialHumidity,
                "Feuchtigkeit sollte nach Update verringert sein");
    }

    @Test
    void testOverheatingProtection() {
        // Überprüfe, ob der Sicherheitsmechanismus den Trockner bei Überhitzung stoppt
        // Programm starten
        simulation.startProgram("cotton");

        // Temperatur manuell auf einen kritischen Wert setzen
        dryerState.setTemperature(100.0); // Überhitzungsschwelle überschreiten

        // Simulation aktualisieren, um die Sicherheitsprüfung auszulösen
        simulation.updateState(2000);

        // Überprüfen, ob das Programm gestoppt wurde
        assertEquals(DryerState.ProgramStatus.ERROR, dryerState.getStatus(),
                "Status sollte bei Überhitzung ERROR sein");
        assertTrue(dryerState.getError().contains("Temperature"),
                "Fehlermeldung sollte einen Hinweis auf Überhitzung enthalten");
    }

    @Test
    void testDifferentPrograms() {
        // Test für Baumwolle
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

    @Test
    void testProgramCompletion() {
        // Programm mit sehr kurzer Laufzeit starten
        dryerState.setProgramName("wool"); // Wollprogramm hat typischerweise die kürzeste Laufzeit
        simulation.startProgram("wool");

        // Restzeit manuell auf einen sehr niedrigen Wert setzen, um das Programmende zu
        // simulieren
        // Wollprogramm hat typischerweise die kürzeste Laufzeit
        simulation.startProgram("wool");

        // Restzeit manuell auf einen sehr niedrigen Wert setzen, um das Programmende zu
        // simulieren
        dryerState.setRemainingSeconds(1);
        // Feuchtigkeit auf fast 0 setzen, um Trockenheit zu simulieren
        dryerState.setHumidity(0.1);

        // Simulation aktualisieren, um das Programmende auszulösen
        simulation.updateState(2000);

        // Überprüfen, ob das Programm beendet wurde
        assertTrue(DryerState.ProgramStatus.IDLE.equals(dryerState.getStatus())
                || DryerState.ProgramStatus.COOLING.equals(dryerState.getStatus()),
                "Status sollte nach Programmende IDLE oder COOLING sein");
        assertEquals(0, dryerState.getRemainingSeconds(),
                "Restzeit sollte nach Programmende 0 sein");
    }

    @Test
    void testHumiditySensorAffectsRemainingTime() {
        // Setup: Start mit hoher Feuchtigkeit
        dryerState.setHumidity(80.0);
        dryerState.setProgramName("cotton");
        simulation.startProgram("cotton");

        // Action: Simulation mit schneller Trocknung
        simulation.setHumidityDecreaseRate(5.0); // Schnellere Trocknung simulieren
        simulation.updateState(10000); // 10 Sekunden simulieren

        // Speichere die Restzeit
        int remainingAfterFastDrying = dryerState.getRemainingSeconds();

        simulation.stopProgram();

        // Reset für Vergleich
        dryerState.setHumidity(80.0);
        dryerState.setProgramName("cotton");
        simulation.startProgram("cotton");
        dryerState.setRemainingSeconds(3600);

        // Action: Simulation mit langsamer Trocknung
        simulation.setHumidityDecreaseRate(0.5); // Langsamere Trocknung simulieren
        simulation.updateState(10000); // 10 Sekunden simulieren

        int remainingAfterSlowDrying = dryerState.getRemainingSeconds();

        // Verification: Bei schnellerer Trocknung sollte die Restzeit kürzer sein
        assertTrue(remainingAfterFastDrying < remainingAfterSlowDrying,
                "Schnellere Trocknung sollte zu kürzerer Restzeit führen");
    }

    @Test
    void testResetSimulationValuesAfterProgramCompletion() {
        // Step 1: Start a program
        simulation.startProgram("cotton");

        // Step 2: Simulate program running and completing
        // Modify simulation values to simulate running state
        dryerState.setTemperature(65.0);
        dryerState.setHumidity(30.0);

        // Fast-forward to program completion
        dryerState.setRemainingSeconds(1);
        simulation.updateState(2000); // Simulate enough time to complete

        // Verify program completed
        assertTrue(DryerState.ProgramStatus.COOLING.equals(dryerState.getStatus())
        || DryerState.ProgramStatus.IDLE.equals(dryerState.getStatus()),
                "Program should be COOLING or IDLE after completion");

        // Step 3: Start a new program
        simulation.startProgram("synthetic");

        // Step 4: Verify simulation values
        assertEquals(DryerState.ProgramStatus.RUNNING, dryerState.getStatus(),
                "Status should be RUNNING for new program");
        assertTrue(dryerState.getRemainingSeconds() > 0,
                "Remaining time should be properly set for new program");
    }
}