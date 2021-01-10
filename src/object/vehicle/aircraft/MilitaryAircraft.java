package object.vehicle.aircraft;

import data.Database;
import component.TableCellComponent;
import javafx.collections.ObservableList;
import object.network.Airport;
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
            case NONE, EMERGENCY, DEBOARDING, SET_PASS_NUM, BOARDING -> airport_action = AIRPORT_ACTION.REFUEL;
            case REFUEL -> {
                if (refuel(20d / fps)) airport_action = AIRPORT_ACTION.READY;
            }
            case READY -> {
                if (((Airport) Database.getAppObjects().get(destId)).removeUsing(getId())) {
                    state = State.WAITING_TRACK;
                    airport_action = AIRPORT_ACTION.NONE;
                    setNewDestID();
                    if (destId == null) generateNewRoute();
                } else
                    System.out.println("Removing from airport error");
            }
        }
        refuel(20d / fps);
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
