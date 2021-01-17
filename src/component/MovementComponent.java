package component;

import util.Utility;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Responsible for proper movement of the vehicle
 */
public class MovementComponent {
    public enum Heading { N, E, S, W }

    private Heading heading = Heading.N;
    private final double speed, offset;
    private double x, y, destX, destY;

    /**
     * Constructor
     * @param speed speed of the object (pixels per frame)
     * @param offset how far can the object be from the destination to be considered "arrived"
     */
    public MovementComponent(double speed, double offset) {
        this.speed = speed;
        this.offset = offset;
    }

    /**
     * Init positions
     * @param position_destData positional data of the object (and destination - 4 arguments required)
     */
    public void init(ArrayList<Double> position_destData) {
        x = position_destData.get(0);
        y = position_destData.get(1);
        if (position_destData.size() == 4) {
            destX = position_destData.get(2);
            destY = position_destData.get(3);
        }
        else {
            destX = x;
            destY = y;
        }
        setHeading();
    }

    /**
     * Move object according to its speed
     */
    public void move() {
        x += getSpeedX();
        y += getSpeedY();
    }

    /**
     * Check if object arrived to the destination
     * @return true if arrived
     */
    public boolean arrived() {
        if (Utility.Math.dist(x, y, destX, destY) < offset) {
            x = destX;
            y = destY;
            return true;
        }
        return false;
    }

    /**
     * Get positional data used for updating guiComponent
     * @return x, y, angle
     */
    public ArrayList<Double> getPositionData() {
        return new ArrayList<>(Arrays.asList(x, y, getAngle()));
    }

    /**
     * Get angle based on the object's heading
     * @return angle
     */
    private double getAngle() {
        return switch (heading) {
            case N -> 0d;
            case E -> 90d;
            case S -> 180d;
            default -> 270d;
        };
    }

    /**
     * Get heading of the object
     * @return current heading
     */
    public Heading getHeading() { return heading; }

    /**
     * Set heading of the object
     */
    private void setHeading() {
        double angle = Math.toDegrees(Math.atan2(destY - y, destX - x) - Math.atan2(-100, 0));
        int roundAngle = Utility.Math.closestMultiple(angle, 90);
        if (roundAngle < 0) roundAngle += 360;
        switch (roundAngle) {
           case 0 -> heading = Heading.N;
           case 90 -> heading = Heading.E;
           case 180 -> heading = Heading.S;
           default -> heading = Heading.W;
        }
    }

    /**
     * Get speed of the object
     * @return speed
     */
    public double getSpeed() { return speed; }

    /**
     * Get x component of the object's speed
     * @return speed x component
     */
    private double getSpeedX() {
        return switch (heading) {
            case N, S -> 0;
            case E -> speed;
            case W -> -speed;
        };
    }

    /**
     * Get y component of the object's speed
     * @return speed y component
     */
    private double getSpeedY() {
        return switch (heading) {
            case E, W -> 0;
            case N -> -speed;
            case S -> speed;
        };
    }

    /**
     * Set new destination for the object
     * @param destData positional data of the destination
     *                 (and next destination - 4 arguments required), if null function does nothing
     */
    public void setDest(ArrayList<Double> destData) {
        if (destData == null) return;
        if (destData.size() == 2)
            setDest(destData.get(0), destData.get(1));
        else {
            x = destData.get(0);
            y = destData.get(1);
            destX = destData.get(0);
            destY = destData.get(1);
            setDest(destData.get(2), destData.get(3));
            x = destData.get(0);
            y = destData.get(1);
            destX = destData.get(0);
            destY = destData.get(1);
        }
    }

    /**
     * Set new destination, calculate new heading and correct the position of the object
     * @param destX x coordinate of the destination
     * @param destY y coordinate of the destination
     */
    private void setDest(double destX, double destY) {
        double prevDestX = this.destX;
        double prevDestY = this.destY;
        this.destX = destX;
        this.destY = destY;
        setHeading();
        switch (heading) {
            case N -> {
                x = destX;
                y = prevDestY - offset;
            }
            case E -> {
                y = destY;
                x = prevDestX + offset;
            }
            case S -> {
                x = destX;
                y = prevDestY + offset;
            }
            case W -> {
                y = destY;
                x = prevDestX - offset;
            }
        }
    }

    @Override
    public String toString() {
        return "MovementComponent{" +
                "heading=" + heading +
                ", speed=" + speed +
                ", offset=" + offset +
                ", x=" + x +
                ", y=" + y +
                ", destX=" + destX +
                ", destY=" + destY +
                '}';
    }
}
