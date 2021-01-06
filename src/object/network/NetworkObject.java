package object.network;

import data.TableCellComponent;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import object.base.AppObject;

import java.util.HashSet;

public abstract class NetworkObject extends AppObject {
    protected volatile HashSet<String> using;
    protected volatile boolean isOpened;

    public NetworkObject(String data) {
        super(data);
        using = new HashSet<>();
        isOpened = true;
    }

    public HashSet<String> getUsing() {
        return using;
    }

    public synchronized boolean removeUsing(String vehicleId) {
        if (using.contains(vehicleId)) {
            using.remove(vehicleId);
            isOpened = true;
            return true;
        }
        else
            return false;
    }

    public synchronized boolean addUsing(String vehicleId) {
        if (isOpened) {
            using.add(vehicleId);
            isOpened = false;
            return true;
        }
        else
            return false;
    }

    public char getAssignment() {
        return getId().charAt(2);
    }

    public void update() { guiComponent.update((isOpened)? null : Color.RED); }

    @Override
    public String toString() {
        return  super.toString() +
                String.format("  using: %s\n", using) +
                String.format("  isOpened: %b\n", isOpened);
    }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = super.getObjectInfo();
        objectInfos.add(new TableCellComponent("using", using.toString()));
        objectInfos.add(new TableCellComponent("isOpened", Boolean.toString(isOpened)));
        return objectInfos;
    }
}
