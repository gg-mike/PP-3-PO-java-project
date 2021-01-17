package component;

/**
 * All the necessary functionality required for proper thread running
 */
public class ThreadComponent {
    private boolean running;
    private boolean exit = false;
    private final double fps, deltaTime;
    private double prevTime, simulationSpeed = 1d;

    /**
     * Constructor
     * @param running set initial state of thread
     * @param fps frames per second
     */
    public ThreadComponent(boolean running, double fps) {
        this.running = running;
        this.fps = fps;
        this.deltaTime = 1d / fps * 1000d;
    }

    /**
     * Start clock (prevTime = now)
     */
    public void startClock() {
        prevTime = System.currentTimeMillis();
    }

    /**
     * End clock and force thread to sleep remaining time
     */
    public void endClock() {
        double end = System.currentTimeMillis();
        try {
            long sleepTime = (long) ((deltaTime / simulationSpeed - (end - prevTime)));
            Thread.sleep((sleepTime < 0)? 0 : sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return running (false if thread is paused)
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @param running set the value of running
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Change the value of running to the opposite
     */
    public void switchRunning() {
        running = !running;
    }

    /**
     * @return exit (end of thread)
     */
    public boolean isExit() {
        return exit;
    }

    /**
     * @param exit set the value of exit
     */
    public void setExit(boolean exit) {
        this.exit = exit;
    }

    /**
     * @return fps (frames per second)
     */
    public double getFPS() {
        return fps;
    }

    public void setSimulationSpeed(double simulationSpeed) {
        this.simulationSpeed = simulationSpeed;
    }
}
