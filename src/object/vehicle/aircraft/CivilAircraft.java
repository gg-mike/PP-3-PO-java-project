package object.vehicle.aircraft;

import component.RouteComponent;
import data.Database;
import component.TableCellComponent;
import javafx.collections.ObservableList;
import object.network.Airport;
import util.Utility;

public final class CivilAircraft extends Aircraft {
    private final int maxPassengerN;
    private int currPassengerN;
    private final int delta = 50 / ((Double) (threadComponent.getFPS())).intValue();
    private int endN = 0;
    private boolean isFullStopIntermediateAirports = false;

    /**
     * Constructor
     * @param data json file string
     */
    public CivilAircraft(String data) {
        super(data);
        Utility.JSONInfo.init(data);
        maxPassengerN = (Integer) Utility.JSONInfo.get("maxPassengerN");
        currPassengerN = 0;
    }

    /**
     * @return true if all passengers deboarded
     */
    protected boolean deboarding() {
        if (currPassengerN > delta) { currPassengerN -= delta; return false; }
        else { currPassengerN = 0; return true; }
    }

    /**
     * @return true if all passengers boarded
     */
    protected boolean boarding() {
        if (currPassengerN + delta < endN) { currPassengerN += delta; return false; }
        else { currPassengerN = endN; return true; }
    }

    /**
     * Set of operations which need to be performed in case object is on the airport, like: DEBOARDING, BOARDING, REFUEL
     */
    @Override
    protected void airportActions() {
        if (airport_action == AIRPORT_ACTION.NONE && !(isFullStopIntermediateAirports || routeComponent.isMainRouteStop()))
            airport_action = AIRPORT_ACTION.REFUEL;
        switch (airport_action) {
            case NONE -> airport_action = AIRPORT_ACTION.DEBOARDING;
            case EMERGENCY -> {
                if (deboarding()) airport_action = AIRPORT_ACTION.REFUEL;
            }
            case REFUEL -> {
                if (refuel(20d / threadComponent.getFPS())) airport_action = AIRPORT_ACTION.READY;
            }
            case DEBOARDING -> {
                if (deboarding()) airport_action = AIRPORT_ACTION.SET_PASS_NUM;
            }
            case SET_PASS_NUM -> {
                endN = Utility.Math.randInt(maxPassengerN / 4, maxPassengerN);
                airport_action = AIRPORT_ACTION.BOARDING;
            }
            case BOARDING -> {
                if (boarding()) airport_action = AIRPORT_ACTION.REFUEL;
            }
            case READY -> {
                if (((Airport) Database.getAppObjects().get(routeComponent.getDest())).removeUsing(getId())) {
                    routeComponent.setState(RouteComponent.State.WAITING_TRACK);
                    airport_action = AIRPORT_ACTION.NONE;

                    movementComponent.setDest(routeComponent.setNewDest());
                    if (routeComponent.getDest() == null && !threadComponent.isExit()) generateNewRoute();
                } else
                    System.out.println("Removing from airport error");
            }
        }
        refuel(20d / threadComponent.getFPS());
    }

    /**
     * @param fullStopIntermediateAirports set if aircraft should make a full stop
     *                                     (deboard and board passengers or just refuel)
     */
    public void setFullStopIntermediateAirports(boolean fullStopIntermediateAirports) {
        isFullStopIntermediateAirports = fullStopIntermediateAirports;
    }

    @Override
    public String toString() {
        return  super.toString() +
                String.format("  passengers: %d/%d\n", currPassengerN, maxPassengerN);
    }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = super.getObjectInfo();
        objectInfos.add(new TableCellComponent("Passengers", String.format("%d/%d", currPassengerN, maxPassengerN)));
        return objectInfos;
    }
}
