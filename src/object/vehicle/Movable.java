package object.vehicle;

public interface Movable {
    void move();
    void generateNewRoute();
    void start();
    void switchRunning();
    void stop();
    void end();
}
