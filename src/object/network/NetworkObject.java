package object.network;

import component.TableCellComponent;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import object.base.AppObject;

import java.util.HashSet;

/**
 * Base class for all network objects
 */
public abstract class NetworkObject extends AppObject {
    protected volatile HashSet<String> using;
    protected volatile boolean isOpened;

    /**
     * Constructor
     * @param data json file string
     */
    public NetworkObject(String data) {
        super(data);
        using = new HashSet<>();
        isOpened = true;
    }

    /**
     * @return set of ids for all vehicle which use this object
     */
    public HashSet<String> getUsing() {
        return using;
    }

    /**
     * @param vehicleId id of the vehicle to be removed
     * @return true if successfully removed
     */
    public synchronized boolean removeUsing(String vehicleId) {
        if (using.contains(vehicleId)) {
            using.remove(vehicleId);
            isOpened = true;
            return true;
        }
        else
            return false;
    }

    /**
     * @param vehicleId id of the vehicle to be added
     * @return true if successfully added
     */
    public synchronized boolean addUsing(String vehicleId) {
        if (isOpened) {
            using.add(vehicleId);
            isOpened = false;
            return true;
        }
        else
            return false;
    }

    /**
     * @return assignment of the object
     */
    public char getAssignment() {
        return getId().charAt(2);
    }

    /**
     * Set of operations which need to be performed every frame (GUI)
     */
    public void update() { guiComponent.update((isOpened)? null : Color.RED); }

    @Override
    public String toString() {
        return  super.toString() +
                String.format("  using: %s\n", using) +
                String.format("  isOpened: %b\n", isOpened);
    }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = super.getObjectInfo();
        objectInfos.add(new TableCellComponent("Vehicles using", using.toString()));
        objectInfos.add(new TableCellComponent("Is open?", Boolean.toString(isOpened)));
        return objectInfos;
    }
}
