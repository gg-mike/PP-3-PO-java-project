package obj.base;

import data.Database;
import data.MovementComponent;
import data.TableCellComponent;
import data.ThreadComponent;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import obj.network.Airport;
import obj.network.Junction;
import obj.network.Track;
import obj.vehicle.Movable;
import util.Utility;

import java.util.*;

public abstract class MovingObject extends AppObject implements Movable, Runnable {
    public enum State { MOVING, WAITING_JUNCTION, WAITING_AIRPORT, JUNCTION, AIRPORT, WAITING_TRACK, STOP }
    public final int fps = 10;

    protected volatile ThreadComponent threadInfo = new ThreadComponent(false, fps);
    protected volatile MovementComponent movementInfo;
    protected final MovementType movementType;
    protected LinkedList<String> destRoute;
    protected LinkedList<String> intermediateRoute;
    protected String destId = "";
    protected State currState;
    private String currTrackUsed = null;
    private final double speed;

    public MovingObject(String data) {
        super(data);
        destRoute = Utility.Convertors.array2linkedList(Utility.JSONInfo.getArray("route"));
        shape.setFill(Color.BLACK);
        speed = ((Number) Utility.JSONInfo.get("speed")).doubleValue();
        movementType = MovementType.valueOf(((String) Utility.JSONInfo.get("movementType")));
        if (movementType == MovementType.CIRCLES)
            destRoute.add(destRoute.getFirst());
        ((Circle) shape).setRadius(5);
    }

    public void initRoute() {
        intermediateRoute = buildIntermediateRoute();
        if (checkRoute()) {
            destId = intermediateRoute.remove();
            ((Circle) shape).setCenterX(Database.getAppObjects().get(destId).getX());
            ((Circle) shape).setCenterY(Database.getAppObjects().get(destId).getY());
            if (Database.getAppObjects().get(destId).objectType == Database.ObjectType.AP)
                currState = State.WAITING_AIRPORT;
            else
                currState = State.WAITING_JUNCTION;
            if (intermediateRoute.size() != 0) {
                String nextDestId = intermediateRoute.getFirst();
                double destX = Database.getAppObjects().get(nextDestId).getX();
                double destY = Database.getAppObjects().get(nextDestId).getY();
                movementInfo = new MovementComponent(speed / fps, getX(), getY(), destX, destY, 10);
                movementInfo.setDestFirstTime(getX(), getY());
            }
            else
                movementInfo = new MovementComponent(speed / fps, getX(), getY(), 10);
        }
    }

    private boolean checkRoute() {
        for (String elem: intermediateRoute)
            if (!Database.getAirports().contains(elem) && !Database.getJunctions().contains(elem)) {
                System.out.println("MovingObject.checkRoute(): Invalid element in route, elem=" + intermediateRoute);
                return false;
            }
        return true;
    }

    protected LinkedList<String> buildIntermediateRoute() {
        LinkedList<String> ir = new LinkedList<>();
        Database.ObjectAssignment assignment;
        if (objectType == Database.ObjectType.CA || objectType == Database.ObjectType.MA)
            assignment = Database.ObjectAssignment.AIR;
        else
            assignment = Database.ObjectAssignment.WATER;
        for (int i = 0; i < destRoute.size() - 1; i++) {
            ir.addAll(Database.createRoute(destRoute.get(i), destRoute.get(i+1), assignment));
            ir.removeLast();
        }
        ir.add(destRoute.getLast());
        return ir;
    }

    protected boolean setNewDestID() {
        if (intermediateRoute.size() != 0) {
            destId = intermediateRoute.remove();
            double destX = Database.getAppObjects().get(destId).getX();
            double destY = Database.getAppObjects().get(destId).getY();
            if (!movementInfo.setDest(destX, destY))
                System.out.println("MovingObject.setNewDestID: False in MovementInfo.setDest(...)");
            return false;
        }
        else {
            destId = "";
            return true;
        }
    }

    public void update() {
        setX(movementInfo.getX());
        setY(movementInfo.getY());
        //shape.setVisible(currState == State.MOVING);
    }

    protected abstract void moveActions();

    protected abstract void airportActions();

    @Override
    public synchronized void move() {
        switch (currState) {
            case MOVING -> {
                if (movementInfo.arrived()) {
                    if (Database.getAppObjects().get(destId).objectType == Database.ObjectType.AP)
                        currState = State.WAITING_AIRPORT;
                    else if (Database.getAppObjects().get(destId).objectType == Database.ObjectType.JU)
                        currState = State.WAITING_JUNCTION;
                    if (currTrackUsed != null)
                        if (!((Track) Database.getAppObjects().get(currTrackUsed)).removeUsing(getId()))
                            System.out.println("Track " + currTrackUsed + " doesn't contain vehicle with this id " + getId());
                }
                else {
                    movementInfo.move();
                    moveActions();
                }
            }
            case WAITING_JUNCTION -> {
                if (((Junction) Database.getAppObjects().get(destId)).addUsing(getId()))
                    currState = State.JUNCTION;
            }
            case WAITING_AIRPORT -> {
                if (((Airport) Database.getAppObjects().get(destId)).addUsing(getId()))
                    currState = State.AIRPORT;
            }
            case JUNCTION -> {
                if (((Junction) Database.getAppObjects().get(destId)).removeUsing(getId())) {
                    currState = State.WAITING_TRACK;
                    if (setNewDestID()) generateNewRoute();
                }
                else
                    System.out.println("Removing from junction error");
            }
            case AIRPORT -> airportActions();
            case WAITING_TRACK -> {
                currTrackUsed = ((Junction) Database.getAppObjects().get(destId)).getTrack(movementInfo.getHeading(), true);
                if (currTrackUsed != null) {
                    Track track = (Track) Database.getAppObjects().get(currTrackUsed);
                    if (track.getDirection() == 2) {
                        track.addUsing(getId());
                        currState = State.MOVING;
                    }
                    else if (track.getDirection() == 0) {
                        if (track.getPoints()[1].equals(destId)) {
                            track.addUsing(getId());
                            currState = State.MOVING;
                        }
                        else if (track.getUsing().size() == 0) {
                            track.setDirection(1);
                            track.addUsing(getId());
                            currState = State.MOVING;
                        }
                    }
                    else if (track.getDirection() == 1) {
                        if (track.getPoints()[0].equals(destId)) {
                            track.addUsing(getId());
                            currState = State.MOVING;
                        }
                        else if (track.getUsing().size() == 0) {
                            track.setDirection(0);
                            track.addUsing(getId());
                            currState = State.MOVING;
                        }
                    }
                }
                else
                    System.out.println("MovingObject.move: Error in retrieving trackId, destId=" + destId +
                            ", heading=" + movementInfo.getHeading() + ", invertHeading=" + true +
                            ", tracks=" + ((Junction) Database.getAppObjects().get(destId)).getTracks());
            }
        }
    }

    @Override
    public synchronized void generateNewRoute() {
        switch (movementType) {
            case ONCE -> {
                currState = State.STOP;
                threadInfo.setExit(true);
            }
            case CIRCLES ->
                initRoute();

            case THERE_AND_BACK -> {
                Collections.reverse(destRoute);
                initRoute();
            }
        }
    }

    @Override
    public synchronized void startStop() {
        threadInfo.switchRunning();
    }

    @Override
    public synchronized void end() {
        threadInfo.setExit(true);
    }

    @Override
    public void run() {
        while (!threadInfo.isExit()) {
            if (threadInfo.isFrame())
                move();
        }
    }

    @Override
    public String toString() {
        return  super.toString() +
                String.format("  state: %s\n", currState) +
                String.format("  speed: %.2f\n", movementInfo.getSpeed()) +
                String.format("  heading: %s\n", movementInfo.getHeading()) +
                String.format("  route: %s\n", destRoute) +
                String.format("  destId: %s\n", destId);
    }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = super.getObjectInfo();
        objectInfos.add(new TableCellComponent("state", currState.toString()));
        objectInfos.add(new TableCellComponent("speed", Double.toString(movementInfo.getSpeed())));
        objectInfos.add(new TableCellComponent("heading", movementInfo.getHeading().toString()));
        objectInfos.add(new TableCellComponent("route", destRoute.toString()));
        objectInfos.add(new TableCellComponent("destId", destId));
        return objectInfos;
    }
}
