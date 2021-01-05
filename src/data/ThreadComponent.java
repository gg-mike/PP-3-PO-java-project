package data;

public class ThreadComponent {
    private boolean running;
    private boolean exit = false;
    private final double fps;
    private double prevTime;
    private double deltaTime;

    public ThreadComponent(boolean running, double fps) {
        this.running = running;
        this.fps = fps;
        this.prevTime = System.currentTimeMillis();
        this.deltaTime = System.currentTimeMillis();
    }

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

    public void switchRunning() {
        running = !running;
    }

    public boolean isExit() {
        return exit;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    public double getFps() {
        return fps;
    }
}
