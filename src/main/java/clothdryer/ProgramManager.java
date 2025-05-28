package clothdryer;

public class ProgramManager implements Runnable {

    private final DryerState state;

    public ProgramManager() {
        this.state = new DryerState();
    }

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

    }
}
