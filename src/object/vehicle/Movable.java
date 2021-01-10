package object.vehicle;

public interface Movable {
    enum MovementType { ONCE, EMERGENCY, AFTER_EMERGENCY, CIRCLES, THERE_AND_BACK }

    void move();
    void generateNewRoute();
    void start();
    void switchRunning();
    void stop();
    void end();
}
