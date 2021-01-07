package object.vehicle.ship;

import object.base.MovingObject;

public abstract class Ship extends MovingObject {

    public Ship(String data) { super(data); }

    @Override
    protected void airportActions() {
        state = State.MOVING;
    }

    @Override
    protected void moveActions() {}
}
