package object.vehicle.ship;

import component.RouteComponent;
import object.base.MovingObject;

/**
 * Base class for ships
 */
public abstract class Ship extends MovingObject {

    /**
     * Constructor
     * @param data json file string
     */
    public Ship(String data) { super(data); }

    /**
     * Set of operations which need to be performed in case object is on the airport
     */
    @Override
    protected void airportActions() {
        routeComponent.setState(RouteComponent.State.MOVING);
    }

    /**
     * Set of operations which need to be performed in case object is moving
     * FOR SHIPS NOTHING HAPPENS
     */
    @Override
    protected void moveActions() {}
}
