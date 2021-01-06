package object.vehicle.ship;

import data.TableCellComponent;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import util.Utility;

public final class AircraftCarrier extends Ship {
    private final String weaponType;

    public AircraftCarrier(String data) {
        super(data);
        Utility.JSONInfo.init(data);
        weaponType = (String) Utility.JSONInfo.get("weaponType");
    }

    public void deployMA(MouseEvent event) {

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
