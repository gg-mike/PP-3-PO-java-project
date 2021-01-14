package object.network;

import data.Database;
import component.GUIComponent;
import component.MovementComponent;
import component.TableCellComponent;
import javafx.collections.ObservableList;
import util.Utility;

import java.util.ArrayList;

public class Track extends NetworkObject {
    private int direction;
    private final double len;
    private final String[] points;

    public Track(String data) {
        super(data);
        direction = (Utility.StringInfo.getX() == 0 || Utility.StringInfo.getX() == 500) ? 0 : 2;
        ArrayList<Object> objects = Utility.JSONInfo.getArray("points");
        points = new String[]{(String) objects.get(0), (String) objects.get(1)};
        guiComponent = new GUIComponent(Utility.StringInfo.getId(), getPointsCoord());
        addTracksToJunctions();
        len = Utility.Math.dist(getStartGUI_X(), getStartGUI_Y(), getEndGUI_X(), getEndGUI_Y());
    }

    public int getDirection() { return direction; }

    public void setDirection(int direction) { this.direction = direction; }

    public String[] getPoints() { return points; }

    private Double[] getPointsCoord() {
        if ((Database.getJunctions().contains(points[0]) || Database.getAirports().contains(points[0])) &&
                (Database.getJunctions().contains(points[1]) || Database.getAirports().contains(points[1]))) {
            return new Double[]{
                    Database.getAppObjects().get(points[0]).getGUI_X(), Database.getAppObjects().get(points[0]).getGUI_Y(),
                    Database.getAppObjects().get(points[1]).getGUI_X(), Database.getAppObjects().get(points[1]).getGUI_Y() };
        }
        else
            return null;
    }

    private void addTracksToJunctions() {
        double diffX = getStartGUI_X() - getEndGUI_X();
        double diffY = getStartGUI_Y() - getEndGUI_Y();
        if (diffX == 0 && diffY < 0) {
            ((Junction) Database.getAppObjects().get(points[0])).addTrack(MovementComponent.Heading.S, getId());
            ((Junction) Database.getAppObjects().get(points[1])).addTrack(MovementComponent.Heading.N, getId());
        }
        else if (diffY == 0 && diffX < 0) {
            ((Junction) Database.getAppObjects().get(points[0])).addTrack(MovementComponent.Heading.E, getId());
            ((Junction) Database.getAppObjects().get(points[1])).addTrack(MovementComponent.Heading.W, getId());
        }
    }

    @Override
    public synchronized boolean removeUsing(String vehicleId) {
        if (using.contains(vehicleId)) {
            using.remove(vehicleId);
            return true;
        }
        else
            return false;
    }

    @Override
    public synchronized boolean addUsing(String vehicleId) {
        using.add(vehicleId);
        return true;
    }

    public double getStartGUI_X() { return guiComponent.getCoords()[0]; }
    public double getEndGUI_X() { return guiComponent.getCoords()[1]; }
    public double getStartGUI_Y() { return guiComponent.getCoords()[2]; }
    public double getEndGUI_Y() { return guiComponent.getCoords()[3]; }

    @Deprecated public double getGUI_X() { return 0; }
    @Deprecated public double getGUI_Y() { return 0; }

    public double getLen() { return len; }

    @Override
    public String toString() {
        return  super.toString() +
                String.format("  direction: %d\n", direction) +
                String.format("  length: %.0f\n", len) +
                String.format("  points: %s, %s\n", points[0], points[1]);
    }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = super.getObjectInfo();
        objectInfos.remove(1);
        objectInfos.remove(2);
        objectInfos.add(new TableCellComponent("direction", Integer.toString(direction)));
        objectInfos.add(new TableCellComponent("length", String.format("%.0f", len)));
        objectInfos.add(new TableCellComponent("points", points[0] + ", " + points[1]));
        return objectInfos;
    }
}
