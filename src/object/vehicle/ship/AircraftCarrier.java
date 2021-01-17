package object.vehicle.ship;

import component.TableCellComponent;
import javafx.collections.ObservableList;
import util.Utility;

public final class AircraftCarrier extends Ship {
    private final String weaponType;

    public AircraftCarrier(String data) {
        super(data);
        Utility.JSONInfo.init(data);
        weaponType = (String) Utility.JSONInfo.get("weaponType");
    }

    @Override
    public String toString() {
        return  super.toString() +
                String.format("  weaponType: %s\n", weaponType);
    }

    public String getWeaponType() { return weaponType; }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = super.getObjectInfo();
        objectInfos.add(new TableCellComponent("Weapon type", weaponType));
        return objectInfos;
    }

}
