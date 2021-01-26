package object.vehicle.ship;

import component.TableCellComponent;
import javafx.collections.ObservableList;
import util.Utility;

public final class CruiseShip extends Ship {
    private final int currPassengerN;
    private final int maxPassengerN;
    private final String company;

    /**
     * Constructor
     * @param data json file string
     */
    public CruiseShip(String data) {
        super(data);
        Utility.JSONInfo.init(data);
        maxPassengerN = (Integer) Utility.JSONInfo.get("maxPassengerN");
        currPassengerN = Utility.Math.randInt(maxPassengerN / 3, maxPassengerN);
        company = (String) Utility.JSONInfo.get("company");
    }

    @Override
    public String toString() {
        return  super.toString() +
                String.format("  passengers: %d/%d\n", currPassengerN, maxPassengerN) +
                String.format("  company: %s\n", company);
    }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = super.getObjectInfo();
        objectInfos.add(new TableCellComponent("Passengers", String.format("%d/%d", currPassengerN, maxPassengerN)));
        objectInfos.add(new TableCellComponent("Company", company));
        return objectInfos;
    }
}
