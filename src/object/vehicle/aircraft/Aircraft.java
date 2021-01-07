package object.vehicle.aircraft;

import data.Database;
import data.TableCellComponent;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import object.base.MovingObject;
import util.Utility;

import java.util.ArrayList;

public abstract class Aircraft extends MovingObject {
    public enum AIRPORT_ACTION { NONE, EMERGENCY, REFUEL, DEBOARDING, SET_PASS_NUM, BOARDING, READY }

    protected int stuffN;
    protected Pair<Double, Double> fuelState;
    protected AIRPORT_ACTION airport_action = AIRPORT_ACTION.NONE;

    public Aircraft(String data) {
        super(data);
        stuffN = (Integer) Utility.JSONInfo.get("stuffN");
        ArrayList<Object> objects = Utility.JSONInfo.getArray("fuelState");
        fuelState = new Pair<>(((Integer) objects.get(0)).doubleValue(), ((Integer) objects.get(1)).doubleValue());
    }

    public void burnFuel(double delta) {
        if (fuelState.getKey() > delta)
            fuelState = new Pair<>(fuelState.getKey() - delta, fuelState.getValue());
        else
            fuelState = new Pair<>(0d, fuelState.getValue());
    }

    public boolean refuel(double delta) {
        if (fuelState.getKey() + delta < fuelState.getValue()) {
            fuelState = new Pair<>(fuelState.getKey() + delta, fuelState.getValue());
            return false;
        }
        else {
            fuelState = new Pair<>(fuelState.getValue(), fuelState.getValue());
            return true;
        }
    }

    public void emergencyStop() {
        initialMovementType = movementType;
        initialMainRoute = mainRoute;
        mainRoute = Database.getClosestAirport(destId, (getId().startsWith("CA")? 'C' : 'A'));
        emergencyDestId = mainRoute.getLast();
        intermediateRoute = mainRoute;
        intermediateRoute.removeFirst();
        movementType = MovementType.EMERGENCY;
        airport_action = AIRPORT_ACTION.EMERGENCY;
    }

    @Override
    protected void moveActions() {
        burnFuel(5 / threadInfo.getFps());
    }

    @Override
    public String toString() {
        return  super.toString() +
                String.format("  stuffN: %d\n", stuffN) +
                String.format("  fuelState: %.1f/%.1f\n", fuelState.getKey(), fuelState.getValue()) +
                String.format("  airport action: %s\n", airport_action);
    }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = super.getObjectInfo();
        objectInfos.add(new TableCellComponent("stuffN", Integer.toString(stuffN)));
        objectInfos.add(new TableCellComponent("fuelState", String.format("%.1f/%.1f", fuelState.getKey(), fuelState.getValue())));
        objectInfos.add(new TableCellComponent("airport action", airport_action.toString()));
        return objectInfos;
    }
}
