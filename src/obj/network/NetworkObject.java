package obj.network;

import data.Database;
import data.TableCellComponent;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import obj.base.AppObject;
import util.Utility;

import java.util.HashSet;

public abstract class NetworkObject extends AppObject {
    protected volatile HashSet<String> using;
    protected volatile boolean isOpened;
    protected final Database.ObjectAssignment assignment;

    public NetworkObject(String data) {
        super(data);
        using = new HashSet<>();
        isOpened = true;
        if (Utility.StringInfo.getObjectAssignment() == 'A')
            assignment = Database.ObjectAssignment.AIR;
        else
            assignment = Database.ObjectAssignment.WATER;
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

    public Database.ObjectAssignment getAssignment() {
        return assignment;
    }

    public void update() {
        if (objectType != Database.ObjectType.TR)
            if (!isOpened) {
                shape.setStrokeWidth(2);
                shape.setStroke(Color.RED);
            }
            else
                shape.setStrokeWidth(0);
    }

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
