package obj.vehicle.aircraft;

import data.Database;
import data.TableCellComponent;
import javafx.collections.ObservableList;
import obj.network.Airport;
import util.Utility;

public final class MilitaryAircraft extends Aircraft {
    private final String weaponType;

    public MilitaryAircraft(String data) {
        super(data);
        Utility.JSONInfo.init(data);
        weaponType = (String) Utility.JSONInfo.get("weaponType");
    }

    public void connectToClosestTrack() {
        // TODO: Find closest track and connect to it
    }

    @Override
    protected void airportActions() {
        switch (airport_action) {
            case NONE, DEBOARDING, SET_PASS_NUM, BOARDING -> airport_action = AIRPORT_ACTION.READY;
            case READY -> {
                if (refuel(20)) {
                    if (((Airport) Database.getAppObjects().get(destId)).removeUsing(getId())) {
                        currState = State.WAITING_TRACK;
                        airport_action = AIRPORT_ACTION.NONE;
                        if (setNewDestID()) generateNewRoute();
                    }
                    else
                        System.out.println("Removing from airport error");
                }
            }
        }
        refuel(20);
    }

    @Override
    public String toString() {
        return  super.toString() +
                String.format("  weaponType: %s\n", weaponType);
    }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = super.getObjectInfo();
        objectInfos.add(new TableCellComponent("weaponType", weaponType));
        return objectInfos;
    }
}
