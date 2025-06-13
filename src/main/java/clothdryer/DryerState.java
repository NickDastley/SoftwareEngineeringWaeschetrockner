package clothdryer;

public class DryerState {

    private String programName = "None";
    public enum ProgramStatus {
        IDLE, RUNNING, FINISHED, ERROR, DOOR_OPEN
    }
    private ProgramStatus status;
    private int remainingSeconds = 0;
    private double temperature = 0.0;
    private double humidity = 100.0;
    private boolean doorClosed = true;
    private boolean doorLocked = false;

    public DryerState() {
        status = ProgramStatus.IDLE;
    }

    public synchronized String getProgramName() {
        return programName;
    }

    public synchronized void setProgramName(String programName) {
        this.programName = programName;
    }

    public synchronized ProgramStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(ProgramStatus status) {
        this.status = status;
    }

    public synchronized int getRemainingSeconds() {
        return remainingSeconds;
    }

    public synchronized void setRemainingSeconds(int seconds) {
        this.remainingSeconds = seconds;
    }

    public synchronized double getTemperature() {
        return temperature;
    }

    public synchronized void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public synchronized double getHumidity() {
        return humidity;
    }

    public synchronized void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public synchronized boolean isDoorClosed() {
        return doorClosed;
    }

    public synchronized void setDoorClosed(boolean doorClosed) {
        this.doorClosed = doorClosed;
    }

    public synchronized void setDoorLocked(boolean doorLocked) {
        this.doorLocked = doorLocked;
    }

    public synchronized boolean isDoorLocked() {
        return doorLocked;
    }
}
