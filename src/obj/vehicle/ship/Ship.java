package obj.vehicle.ship;

import javafx.scene.shape.Circle;
import obj.base.MovingObject;
import util.Utility;

public abstract class Ship extends MovingObject {

    public Ship(String data) {
        super(data);
        Utility.JSONInfo.init(data);
        ((Circle) shape).setRadius(6);
    }

    @Override
    protected void airportActions() {
        currState = State.MOVING;
    }

    @Override
    protected void moveActions() {}
}
