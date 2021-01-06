package object.vehicle;

public interface Movable {
    enum MovementType { ONCE, CIRCLES, THERE_AND_BACK }

    void move();
    void generateNewRoute();
    void startStop();
    void end();
}
