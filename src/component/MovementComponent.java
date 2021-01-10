package component;

public class MovementComponent {
    public enum Heading { N, E, S, W }

    private Heading heading;
    private final double speed;
    private double x, y, destX, destY;
    private final double offset;

    public MovementComponent(double speed, double x, double y, double destX, double destY, double offset) {
        this.speed = speed;
        this.x = x;
        this.y = y;
        this.destX = destX;
        this.destY = destY;
        this.offset = offset;
        setHeading();
    }

    public MovementComponent(double speed, double x, double y, double offset) {
        this(speed, x, y, x, y, offset);
    }

    public void move() {
        x += getSpeedX();
        y += getSpeedY();
    }

    public boolean arrived() {
        boolean arrived = false;
        switch (heading) {
            case N, S -> arrived = x == destX && Math.abs(destY - y) <= offset;
            case E, W -> arrived = y == destY && Math.abs(destX - x) <= offset;
        }
        if (arrived) {
            x = destX;
            y = destY;
        }
        return arrived;
    }

    public Heading getHeading() { return heading; }

    private void setHeading() {
        double addX=0, addY = 0;
        if (x % 50 != 0) {
            if (heading == Heading.E) addX = offset;
            else if (heading == Heading.W) addX = -offset;
        }
        if (y % 50 != 0) {
            if (heading == Heading.S) addY = offset;
            else if (heading == Heading.N) addY = -offset;
        }
        if (x + addX == destX) {
            if (y + addY < destY) heading = Heading.S;
            else heading = Heading.N;
        }
        else if (y + addY == destY) {
            if (x + addX > destX) heading = Heading.W;
            else heading = Heading.E;
        }
    }

    public double getSpeed() { return speed; }

    private double getSpeedX() {
        return switch (heading) {
            case N, S -> 0;
            case E -> speed;
            case W -> -speed;
        };
    }

    private double getSpeedY() {
        return switch (heading) {
            case E, W -> 0;
            case N -> -speed;
            case S -> speed;
        };
    }

    public double getX() { return x; }

    public double getY() { return y; }

    public void setDest(double destX, double destY, double nextDestX, double nextDestY) {
        this.destX = destX;
        this.destY = destY;
        setDest(nextDestX, nextDestY);
        this.x = destX;
        this.y = destY;
        this.destX = destX;
        this.destY = destY;
    }

    public void setDest(double destX, double destY) {
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
}
