package obj.network;

import data.Database;
import data.MovementComponent;
import data.TableCellComponent;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import util.Utility;

import java.util.ArrayList;

public class Track extends NetworkObject {
    private int direction;
    private double len = 0;
    private final String[] points;

    public Track(String data) {
        super(data);
        if (Utility.StringInfo.getX() == 0 || Utility.StringInfo.getX() == 500)
            direction = 0;
        else
            direction = 2;
        ArrayList<Object> objects = Utility.JSONInfo.getArray("points");
        points = new String[]{(String) objects.get(0), (String) objects.get(1)};
        Line line = new Line();
        line.setId(shape.getId());
        line.setUserData(shape.getUserData());
        line.setStrokeWidth((direction == 2)? 4 : 2);
        if (setPointsCoord(line)) {
            setPointsCoord(line);
            shape = line;
            if (assignment == Database.ObjectAssignment.AIR)
                shape.strokeProperty().set(Color.valueOf("#fa6a0a"));
            else
                shape.strokeProperty().set(Color.valueOf("#249fde"));
            len = Utility.Math.distance(getStartX(), getStartY(), getEndX(), getEndY());
        }
        else
            System.out.println("Does not exists: " + points[0] + ", " + points[1]);
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
        shape.setStrokeWidth((direction == 2)? 4 : 2);
    }

    public String[] getPoints() {
        return points;
    }

    private boolean setPointsCoord(Line line) {
        if ((Database.getJunctions().contains(points[0]) || Database.getAirports().contains(points[0])) &&
                (Database.getJunctions().contains(points[1]) || Database.getAirports().contains(points[1]))) {
            line.setStartX(Database.getAppObjects().get(points[0]).getX());
            line.setStartY(Database.getAppObjects().get(points[0]).getY());
            line.setEndX(Database.getAppObjects().get(points[1]).getX());
            line.setEndY(Database.getAppObjects().get(points[1]).getY());
            addTracksToJunctions(line);
            return true;
        }
        else
            return false;
    }

    private void addTracksToJunctions(Line line) {
        double diffX = line.getStartX() - line.getEndX();
        double diffY = line.getStartY() - line.getEndY();
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

    public double getStartX() { return ((Line) shape).getStartX(); }
    public double getStartY() { return ((Line) shape).getStartY(); }
    public double getEndX() { return ((Line) shape).getEndX(); }
    public double getEndY() { return ((Line) shape).getEndY(); }

    @Deprecated public double getX() { return 0; }
    @Deprecated public double getY() { return 0; }
    @Deprecated public void setX(double x) { }
    @Deprecated public void setY(double y) { }

    public double getLen() {
        return len;
    }

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
