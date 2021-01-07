package object.base;

import data.*;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import object.network.Airport;
import object.network.Junction;
import object.network.Track;
import object.vehicle.Movable;
import util.Utility;

import java.util.*;

public abstract class MovingObject extends AppObject implements Movable, Runnable {
    public enum State { MOVING, WAITING_JUNCTION, WAITING_AIRPORT, JUNCTION, AIRPORT, WAITING_TRACK, STOP }
    public final int fps = 10;

    protected volatile ThreadComponent threadInfo = new ThreadComponent(false, fps);
    protected MovementType movementType;
    protected MovementType initialMovementType = null;
    protected LinkedList<String> mainRoute;
    protected LinkedList<Integer> mainRouteIndexInIntermediate;
    protected LinkedList<String> intermediateRoute;
    protected LinkedList<String> initialMainRoute = null;
    protected String destId = null;
    protected String emergencyDestId = null;
    protected State state;
    private String currTrackUsed = null;

    public MovingObject(String data) {
        super(data);
        mainRoute = Utility.Convertors.array2linkedList(Utility.JSONInfo.getArray("route"));
        checkRoute(mainRoute);
        movementType = MovementType.valueOf(((String) Utility.JSONInfo.get("movementType")));
        if (movementType == MovementType.CIRCLES)
            mainRoute.add(mainRoute.getFirst());

        guiComponent = new GUIMovableComponent(guiComponent,
                ((Number) Utility.JSONInfo.get("speed")).doubleValue() / fps, 10);
    }

    public void initRoute(boolean firstTime) {
        intermediateRoute = buildIntermediateRoute();
        destId = intermediateRoute.remove();
        if (firstTime) {
            double destX = Database.getAppObjects().get(destId).getX();
            double destY = Database.getAppObjects().get(destId).getY();
            state = (Database.getAppObjects().get(destId).getId().startsWith("AP")) ? State.WAITING_AIRPORT : State.WAITING_JUNCTION;
            if (intermediateRoute.size() != 0) {
                String nextDestId = intermediateRoute.getFirst();
                double nextDestX = Database.getAppObjects().get(nextDestId).getX();
                double nextDestY = Database.getAppObjects().get(nextDestId).getY();
                ((GUIMovableComponent) guiComponent).init(destX, destY, nextDestX, nextDestY);
            }
        }
        else setNewDestID();
    }

    private void checkRoute(LinkedList<String> route) {
        try {
            for (String elem : route)
                if (!Database.getAirports().contains(elem) && !Database.getJunctions().contains(elem)) {
                    System.out.println("MovingObject.checkRoute(): Invalid element in route, elem=" + route);
                    throw new Exception();
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected LinkedList<String> buildIntermediateRoute() {
        LinkedList<String> ir = new LinkedList<>();
        mainRouteIndexInIntermediate = new LinkedList<>();
        char assignment = (getId().startsWith("CA") || getId().startsWith("MA"))? 'A' : 'W';
        for (int i = 0; i < mainRoute.size() - 1; i++) {
            ir.addAll(Database.createRoute(mainRoute.get(i), mainRoute.get(i+1), assignment));
            ir.removeLast();
        }
        ir.add(mainRoute.getLast());

        for (String mainStop : mainRoute)
            mainRouteIndexInIntermediate.add(ir.size() - ir.indexOf(mainStop));

        return ir;
    }

    protected void setNewDestID() {
        if (intermediateRoute.size() != 0) {
            destId = intermediateRoute.remove();
            double destX = Database.getAppObjects().get(destId).getX();
            double destY = Database.getAppObjects().get(destId).getY();
            ((GUIMovableComponent) guiComponent).getMovementComponent().setDest(destX, destY);
        }
        else
            destId = null;
    }

    protected boolean isMainRouteStop() {
        return mainRouteIndexInIntermediate.contains(intermediateRoute.size() + 1);
    }

    public Label getLabel() { return ((GUIMovableComponent) guiComponent).getLabel(); }

    public void setLabelVisible(boolean labelVisible) { ((GUIMovableComponent) guiComponent).setVisibleLabel(labelVisible); }

    public void setVisibleAtJunction(boolean visibleAtJunction) { ((GUIMovableComponent) guiComponent).setShapeVisibleAtJunctions(visibleAtJunction); }

    public void setVisibleWaitingAtJunction(boolean visibleWaitingAtJunction) { ((GUIMovableComponent) guiComponent).setShapeVisibleWaitingAtJunctions(visibleWaitingAtJunction); }

    public void update() {
        ((GUIMovableComponent) guiComponent).update(null, state == State.MOVING,
                state == State.WAITING_AIRPORT || state == State.WAITING_JUNCTION || state == State.WAITING_TRACK);
    }

    protected abstract void moveActions();

    protected abstract void airportActions();

    @Override
    public synchronized void move() {
        switch (state) {
            case MOVING -> {
                if (((GUIMovableComponent) guiComponent).getMovementComponent().arrived()) {
                    if (Database.getAppObjects().get(destId).getId().startsWith("AP"))
                        state = State.WAITING_AIRPORT;
                    else if (Database.getAppObjects().get(destId).getId().startsWith("JU"))
                        state = State.WAITING_JUNCTION;
                    if (currTrackUsed != null)
                        if (!((Track) Database.getAppObjects().get(currTrackUsed)).removeUsing(getId()))
                            System.out.println("Track " + currTrackUsed + " doesn't contain vehicle with this id " + getId());
                }
                else {
                    ((GUIMovableComponent) guiComponent).getMovementComponent().move();
                    moveActions();
                }
            }
            case WAITING_JUNCTION -> {
                if (((Junction) Database.getAppObjects().get(destId)).addUsing(getId()))
                    state = State.JUNCTION;
            }
            case WAITING_AIRPORT -> {
                if (((Airport) Database.getAppObjects().get(destId)).addUsing(getId()))
                    state = State.AIRPORT;
            }
            case JUNCTION -> {
                if (((Junction) Database.getAppObjects().get(destId)).removeUsing(getId())) {
                    state = State.WAITING_TRACK;
                    setNewDestID();
                    if (destId == null) generateNewRoute();
                }
                else
                    System.out.println("Removing from junction error");
            }
            case AIRPORT -> airportActions();
            case WAITING_TRACK -> {
                currTrackUsed = ((Junction) Database.getAppObjects().get(destId)).getTrack(((GUIMovableComponent) guiComponent).getMovementComponent().getHeading(), true);
                if (currTrackUsed != null) {
                    Track track = (Track) Database.getAppObjects().get(currTrackUsed);
                    if (track.getDirection() == 2) {
                        track.addUsing(getId());
                        state = State.MOVING;
                    }
                    else if (track.getDirection() == 0) {
                        if (track.getPoints()[1].equals(destId)) {
                            track.addUsing(getId());
                            state = State.MOVING;
                        }
                        else if (track.getUsing().size() == 0) {
                            track.setDirection(1);
                            track.addUsing(getId());
                            state = State.MOVING;
                        }
                    }
                    else if (track.getDirection() == 1) {
                        if (track.getPoints()[0].equals(destId)) {
                            track.addUsing(getId());
                            state = State.MOVING;
                        }
                        else if (track.getUsing().size() == 0) {
                            track.setDirection(0);
                            track.addUsing(getId());
                            state = State.MOVING;
                        }
                    }
                }
                else
                    System.out.println("MovingObject.move: Error in retrieving trackId, destId=" + destId +
                            ", heading=" + ((GUIMovableComponent) guiComponent).getMovementComponent().getHeading() + ", invertHeading=" + true +
                            ", tracks=" + ((Junction) Database.getAppObjects().get(destId)).getTracks());
            }
        }
    }

    @Override
    public synchronized void generateNewRoute() {
        switch (movementType) {
            case ONCE -> {
                state = State.STOP;
                threadInfo.setExit(true);
            }
            case EMERGENCY -> {
                if (initialMovementType != null && initialMainRoute != null && emergencyDestId != null) {
                    movementType = MovementType.AFTER_EMERGENCY;
                    mainRoute.clear();
                    mainRoute.add(emergencyDestId);
                    mainRoute.add(initialMainRoute.getFirst());
                    initRoute(false);
                }
                else {
                    state = State.STOP;
                    threadInfo.setExit(true);
                }
            }
            case AFTER_EMERGENCY -> {
                movementType = initialMovementType;
                mainRoute = initialMainRoute;
                initialMovementType = null;
                initialMainRoute = null;
                emergencyDestId = null;
                initRoute(false);
            }
            case CIRCLES ->
                initRoute(false);

            case THERE_AND_BACK -> {
                Collections.reverse(mainRoute);
                initRoute(false);
            }
        }
    }

    @Override
    public synchronized void startStop() { threadInfo.switchRunning(); }

    @Override
    public synchronized void end() { threadInfo.setExit(true); }

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
                String.format("  state: %s\n", state) +
                String.format("  speed: %.2f\n", ((GUIMovableComponent) guiComponent).getMovementComponent().getSpeed()) +
                String.format("  heading: %s\n", ((GUIMovableComponent) guiComponent).getMovementComponent().getHeading()) +
                String.format("  route: %s\n", mainRoute) +
                String.format("  destId: %s\n", destId);
    }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = super.getObjectInfo();
        objectInfos.add(new TableCellComponent("state", state.toString()));
        objectInfos.add(new TableCellComponent("speed", Double.toString(((GUIMovableComponent) guiComponent).getMovementComponent().getSpeed())));
        objectInfos.add(new TableCellComponent("heading", ((GUIMovableComponent) guiComponent).getMovementComponent().getHeading().toString()));
        objectInfos.add(new TableCellComponent("route", mainRoute.toString()));
        objectInfos.add(new TableCellComponent("destId", destId));
        return objectInfos;
    }
}
