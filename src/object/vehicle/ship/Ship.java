package object.vehicle.ship;

import component.RouteComponent;
import object.base.MovingObject;

public abstract class Ship extends MovingObject {

    public Ship(String data) { super(data); }

    @Override
    protected void airportActions() {
        routeComponent.setState(RouteComponent.State.MOVING);
    }

    @Override
    protected void moveActions() {}
}
