package object.base;

import component.*;
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
    public final int fps = 10;

    protected volatile ThreadComponent threadComponent = new ThreadComponent(false, fps);
    protected volatile MovementComponent movementComponent;
    protected volatile RouteComponent routeComponent;

    public MovingObject(String data) {
        super(data);
        guiComponent = new GUIMovableComponent(guiComponent);
        movementComponent = new MovementComponent(((Number) Utility.JSONInfo.get("speed")).doubleValue() / fps, 10);
        routeComponent = new RouteComponent(Utility.Convertors.array2linkedList(Utility.JSONInfo.getArray("route")),
                RouteComponent.RouteType.valueOf(((String) Utility.JSONInfo.get("routeType"))), Database.ObjectType.valueOf(getId().substring(0, 2)));
    }

    public void initRouteComponent(boolean firstTime) {
        movementComponent.setDest(routeComponent.initRoute(firstTime));
    }

    public Label getLabel() { return ((GUIMovableComponent) guiComponent).getLabel(); }

    public void setLabelVisible(boolean labelVisible) { ((GUIMovableComponent) guiComponent).setVisibleLabel(labelVisible); }

    public void setVisibleAtJunction(boolean visibleAtJunction) { ((GUIMovableComponent) guiComponent).setShapeVisibleAtJunctions(visibleAtJunction); }

    public void setVisibleWaitingAtJunction(boolean visibleWaitingAtJunction) { ((GUIMovableComponent) guiComponent).setShapeVisibleWaitingAtJunctions(visibleWaitingAtJunction); }

    public void update() {
        ((GUIMovableComponent) guiComponent).update(null, movementComponent.getPositionData(),
                routeComponent.isMoving(), routeComponent.isWaiting());
    }

    protected abstract void moveActions();

    protected abstract void airportActions();

    @Override
    public synchronized void move() {
        switch (routeComponent.getState()) {
            case CONNECTING_TO_TRAFFIC_X -> {
                if (movementComponent.arrived()) {
                    movementComponent.setDest(new ArrayList<>(Arrays.asList(getGUI_X(), getGUI_Y(), getGUI_X(), Database.getAppObjects().get(routeComponent.getTmpDest()).getGUI_Y())));
                    routeComponent.setState(RouteComponent.State.CONNECTING_TO_TRAFFIC_Y);
                }
                else {
                    movementComponent.move();
                    moveActions();
                }
            }
            case CONNECTING_TO_TRAFFIC_Y -> {
                if (movementComponent.arrived()) {
                    if (Database.getAppObjects().get(routeComponent.getTmpDest()).getId().startsWith("AP"))
                        routeComponent.setState(RouteComponent.State.WAITING_AIRPORT);
                    else if (Database.getAppObjects().get(routeComponent.getTmpDest()).getId().startsWith("JU"))
                        routeComponent.setState(RouteComponent.State.CONNECTING_TO_TRAFFIC_Y);
                    routeComponent.setRouteType(RouteComponent.RouteType.CONNECTING);
                    generateNewRoute();
                }
                else {
                    movementComponent.move();
                    moveActions();
                }
            }
            case MOVING -> {
                if (movementComponent.arrived()) {
                    if (Database.getAppObjects().get(routeComponent.getDest()).getId().startsWith("AP"))
                        routeComponent.setState(RouteComponent.State.WAITING_AIRPORT);
                    else if (Database.getAppObjects().get(routeComponent.getDest()).getId().startsWith("JU"))
                        routeComponent.setState(RouteComponent.State.WAITING_JUNCTION);
                    if (routeComponent.getUsedTrack() != null)
                        if (!((Track) Database.getAppObjects().get(routeComponent.getUsedTrack())).removeUsing(getId()))
                            System.out.println("Track " + routeComponent.getUsedTrack() + " doesn't contain vehicle with this id " + getId());
                }
                else {
                    movementComponent.move();
                    moveActions();
                }
            }
            case WAITING_JUNCTION -> {
                if (((Junction) Database.getAppObjects().get(routeComponent.getDest())).addUsing(getId()))
                    routeComponent.setState(RouteComponent.State.JUNCTION);
            }
            case WAITING_AIRPORT -> {
                if (((Airport) Database.getAppObjects().get(routeComponent.getDest())).addUsing(getId()))
                    routeComponent.setState(RouteComponent.State.AIRPORT);
            }
            case JUNCTION -> {
                if (((Junction) Database.getAppObjects().get(routeComponent.getDest())).removeUsing(getId())) {
                    routeComponent.setState(RouteComponent.State.WAITING_TRACK);
                    movementComponent.setDest(routeComponent.setNewDest());
                    if (routeComponent.getDest() == null) generateNewRoute();
                }
                else
                    System.out.println("Removing from junction error");
            }
            case AIRPORT -> airportActions();
            case WAITING_TRACK -> {
                routeComponent.setUsedTrack(((Junction) Database.getAppObjects().get(routeComponent.getDest())).getTrack(movementComponent.getHeading(), true));
                if (routeComponent.getUsedTrack() != null) {
                    Track track = (Track) Database.getAppObjects().get(routeComponent.getUsedTrack());
                    if (track.getDirection() == 2) {
                        track.addUsing(getId());
                        routeComponent.setState(RouteComponent.State.MOVING);
                    }
                    else if (track.getDirection() == 0) {
                        if (track.getPoints()[1].equals(routeComponent.getDest())) {
                            track.addUsing(getId());
                            routeComponent.setState(RouteComponent.State.MOVING);
                        }
                        else if (track.getUsing().size() == 0) {
                            track.setDirection(1);
                            track.addUsing(getId());
                            routeComponent.setState(RouteComponent.State.MOVING);
                        }
                    }
                    else if (track.getDirection() == 1) {
                        if (track.getPoints()[0].equals(routeComponent.getDest())) {
                            track.addUsing(getId());
                            routeComponent.setState(RouteComponent.State.MOVING);
                        }
                        else if (track.getUsing().size() == 0) {
                            track.setDirection(0);
                            track.addUsing(getId());
                            routeComponent.setState(RouteComponent.State.MOVING);
                        }
                    }
                }
                else
                    System.out.println("MovingObject.move: Error in retrieving trackId, destId=" + routeComponent.getDest() +
                            ", heading=" + movementComponent.getHeading() + ", invertHeading=" + true +
                            ", tracks=" + ((Junction) Database.getAppObjects().get(routeComponent.getDest())).getTracks());
            }
        }
    }

    @Override
    public synchronized void generateNewRoute() {
        movementComponent.setDest(routeComponent.generateNewRoute());
        threadComponent.setExit(routeComponent.getState() == RouteComponent.State.STOP);
    }

    @Override
    public void start() { threadComponent.setRunning(true); }

    @Override
    public synchronized void switchRunning() { threadComponent.switchRunning(); }

    @Override
    public void stop() { threadComponent.setRunning(false); }

    @Override
    public synchronized void end() { threadComponent.setExit(true); }

    @Override
    public void run() {
        while (!threadComponent.isExit()) {
            if (threadComponent.isFrame())
                move();
        }
    }

    @Override
    public String toString() {
        return  super.toString() +
                String.format("  state: %s\n", routeComponent.getState()) +
                String.format("  speed: %.2f\n", movementComponent.getSpeed()) +
                String.format("  heading: %s\n", movementComponent.getHeading()) +
                String.format("  route: %s\n", routeComponent.getMainRoute()) +
                String.format("  destId: %s\n", routeComponent.getDest());
    }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = super.getObjectInfo();
        objectInfos.add(new TableCellComponent("state", routeComponent.getState().toString()));
        objectInfos.add(new TableCellComponent("speed", Double.toString(movementComponent.getSpeed())));
        objectInfos.add(new TableCellComponent("heading", movementComponent.getHeading().toString()));
        objectInfos.add(new TableCellComponent("route", routeComponent.getMainRoute().toString()));
        objectInfos.add(new TableCellComponent("destId", routeComponent.getDest()));
        return objectInfos;
    }
}
