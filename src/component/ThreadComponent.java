package component;

/**
 * All the necessary functionality required for proper thread running
 */
public class ThreadComponent {
    private boolean running;
    private boolean exit = false;
    private final double fps;
    private double prevTime;
    private double deltaTime;

    /**
     * Constructor
     * @param running set initial state of thread
     * @param fps frames per second
     */
    public ThreadComponent(boolean running, double fps) {
        this.running = running;
        this.fps = fps;
        this.prevTime = System.currentTimeMillis();
        this.deltaTime = System.currentTimeMillis();
    }

    /**
     * Evaluates if enough time passed from the last frame
     * @return true if is frame
     */
    public boolean isFrame() {
        boolean go = running && (deltaTime - prevTime >= 1 / fps * 1000);
        if (go) {
            prevTime = System.currentTimeMillis();
            deltaTime = System.currentTimeMillis();
            return true;
        }
        else {
            deltaTime = System.currentTimeMillis();
            return false;
        }
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
}
