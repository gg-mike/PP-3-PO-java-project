package object.network;

import component.TableCellComponent;
import javafx.collections.ObservableList;
import util.Utility;

public class Airport extends Junction {
    private final int capacity;

    /**
     * Constructor
     * @param data json file string
     */
    public Airport(String data) {
        super(data);
        capacity = (Integer) Utility.JSONInfo.get("capacity");
    }

    /**
     * @return set of ids for all vehicle which use this object
     */
    @Override
    public synchronized boolean addUsing(String vehicleId) {
        if (isOpened && using.size() < capacity) {
            using.add(vehicleId);
            isOpened = using.size() < capacity;
            return true;
        }
        else
            return false;
    }

    @Override
    public String toString() {
        return  super.toString() +
                String.format("  isCivil: %b\n", getId().charAt(2) != 'M') +
                String.format("  capacity: %d\n", capacity);
    }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = super.getObjectInfo();
        objectInfos.add(new TableCellComponent("Is civilian airport?", Boolean.toString(getId().charAt(2) != 'M')));
        objectInfos.add(new TableCellComponent("Capacity", Integer.toString(capacity)));
        return objectInfos;
    }
}
