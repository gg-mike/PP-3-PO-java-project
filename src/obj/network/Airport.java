package obj.network;

import data.TableCellComponent;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import util.Utility;

public class Airport extends Junction {
    private final boolean isCivil;
    private final int capacity;

    public Airport(String data) {
        super(data);
        shape.setUserData(Utility.JSONInfo.get("name"));
        shape.setFill(Color.valueOf("#b4202a"));
        ((Circle) shape).setRadius(10);
        isCivil = (Boolean) Utility.JSONInfo.get("isCivil");
        if (!isCivil) {
            shape.setStrokeWidth(2);
            shape.setStroke(Color.BLUE);
        }
        capacity = (Integer) Utility.JSONInfo.get("capacity");
    }

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
    public void update() {
        if (!isOpened) {
            shape.setStrokeWidth(2);
            shape.setStroke(Color.RED);
        } else if (isCivil) shape.setStrokeWidth(0);
        else shape.setStroke(Color.BLACK);
    }


    @Override
    public String toString() {
        return  super.toString() +
                String.format("  isCivil: %b\n", isCivil) +
                String.format("  capacity: %d\n", capacity);
    }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = super.getObjectInfo();
        objectInfos.add(new TableCellComponent("isCivil", Boolean.toString(isCivil)));
        objectInfos.add(new TableCellComponent("capacity", Integer.toString(capacity)));
        return objectInfos;
    }
}
