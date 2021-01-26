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

    /**
     * Constructor
     * @param data json file string
     */
    public Track(String data) {
        super(data);
        direction = (Utility.StringInfo.getX() == 0 || Utility.StringInfo.getX() == 500) ? 0 : 2;
        ArrayList<Object> objects = Utility.JSONInfo.getArray("points");
        points = new String[]{(String) objects.get(0), (String) objects.get(1)};
        guiComponent = new GUIComponent(Utility.StringInfo.getId(), getPointsCoord());
        addTracksToJunctions();
        len = Utility.Math.dist(getStartGUI_X(), getStartGUI_Y(), getEndGUI_X(), getEndGUI_Y());
    }

    /**
     * @return direction of the track (2 - two-way, 0/1 - one-way)
     */
    public int getDirection() { return direction; }

    /**
     * @param direction new direction for a track (2 - two-way, 0/1 - one-way)
     */
    public void setDirection(int direction) { this.direction = direction; }

    /**
     * @return start and end points ids
     */
    public String[] getPoints() { return points; }

    /**
     * @return start and end points coordinates
     */
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

    /**
     * Add this track to specific junctions
     */
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

    /**
     * @param vehicleId id of the vehicle to be removed
     * @return true if successfully removed
     */
    @Override
    public synchronized boolean removeUsing(String vehicleId) {
        if (using.contains(vehicleId)) {
            using.remove(vehicleId);
            return true;
        }
        else
            return false;
    }

    /**
     * @param vehicleId id of the vehicle to be added
     * @return true if successfully added
     */
    @Override
    public synchronized boolean addUsing(String vehicleId) {
        using.add(vehicleId);
        return true;
    }

    /**
     * @return x coordinate of the start point of the line
     */
    public double getStartGUI_X() { return guiComponent.getCoords()[0]; }

    /**
     * @return x coordinate of the end point of the line
     */
    public double getEndGUI_X() { return guiComponent.getCoords()[1]; }

    /**
     * @return y coordinate of the start point of the line
     */
    public double getStartGUI_Y() { return guiComponent.getCoords()[2]; }

    /**
     * @return y coordinate of the end point of the line
     */
    public double getEndGUI_Y() { return guiComponent.getCoords()[3]; }

    @Deprecated public double getGUI_X() { return 0; }
    @Deprecated public double getGUI_Y() { return 0; }

    /**
     * @return length of the track
     */
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
        objectInfos.add(new TableCellComponent("Direction", Integer.toString(direction)));
        objectInfos.add(new TableCellComponent("Length", String.format("%.0f", len)));
        objectInfos.add(new TableCellComponent("Points", points[0] + ", " + points[1]));
        return objectInfos;
    }
}
