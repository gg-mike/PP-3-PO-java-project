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

/**
 * Base class for all moving objects
 */
public abstract class MovingObject extends AppObject implements Movable, Runnable {
    public final int fps = 10;

    protected volatile ThreadComponent threadComponent = new ThreadComponent(false, fps);
    protected volatile MovementComponent movementComponent;
    protected volatile RouteComponent routeComponent;

    /**
     * Constructor
     * @param data json file string
     */
    public MovingObject(String data) {
        super(data);
        guiComponent = new GUIMovableComponent(guiComponent);
        movementComponent = new MovementComponent(((Number) Utility.JSONInfo.get("speed")).doubleValue() / fps, 10);
        routeComponent = new RouteComponent(Utility.Convertors.array2linkedList(Utility.JSONInfo.getArray("route")),
                RouteComponent.RouteType.valueOf(((String) Utility.JSONInfo.get("routeType"))), Database.ObjectType.valueOf(getId().substring(0, 2)));
    }

    /**
     * Init RouteComponent and set destination coordinates for MovementComponent
     * @param firstTime true if method is called after creating new object
     */
    public void initRouteComponent(boolean firstTime) {
        movementComponent.setDest(routeComponent.initRoute(firstTime));
    }

    /**
     * @return label of the object
     */
    public Label getLabel() { return ((GUIMovableComponent) guiComponent).getLabel(); }

    /**
     * @param labelVisible set visibility of the label
     */
    public void setLabelVisible(boolean labelVisible) { ((GUIMovableComponent) guiComponent).setVisibleLabel(labelVisible); }

    /**
     * @param visibleAtJunction set visibility of the shape at junctions (and airports)
     */
    public void setVisibleAtJunction(boolean visibleAtJunction) { ((GUIMovableComponent) guiComponent).setShapeVisibleAtJunctions(visibleAtJunction); }

    /**
     * @param visibleWaitingAtJunction set visibility of the shape waiting at junctions (and airports)
     */
    public void setVisibleWaitingAtJunction(boolean visibleWaitingAtJunction) { ((GUIMovableComponent) guiComponent).setShapeVisibleWaitingAtJunctions(visibleWaitingAtJunction); }

    /**
     * Set of operations which need to be performed every frame (GUI)
     */
    public void update() {
        ((GUIMovableComponent) guiComponent).update(null, movementComponent.getPositionData(),
                routeComponent.isMoving(), routeComponent.isWaiting());
    }

    /**
     * Set of operations which need to be performed in case object is moving
     */
    protected abstract void moveActions();

    /**
     * Set of operations which need to be performed in case object is on the airport
     */
    protected abstract void airportActions();

    /**
     * Set of operations which need to be performed every frame (MovementComponent, RouteComponent)
     */
    @Override
    public synchronized void move() {
        switch (routeComponent.getState()) {
            case CONNECTING_TO_TRAFFIC_X -> {
                if (movementComponent.arrived()) {
                    double destX = Database.getAppObjects().get(routeComponent.getTmpDest()).getGUI_X();
                    double destY = Database.getAppObjects().get(routeComponent.getTmpDest()).getGUI_Y();
                    movementComponent.setDest(new ArrayList<>(Arrays.asList(destX, destY)));
                    routeComponent.setState(RouteComponent.State.CONNECTING_TO_TRAFFIC_Y);
                }
                else {
                    movementComponent.move();
                    moveActions();
                }
            }
            case CONNECTING_TO_TRAFFIC_Y -> {
                if (movementComponent.arrived()) {
                    routeComponent.setDest(routeComponent.getTmpDest());
                    routeComponent.setState();
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
                    routeComponent.setState();
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

    /**
     * New route generation (RouteType == ONCE then end)
     */
    @Override
    public synchronized void generateNewRoute() {
        movementComponent.setDest(routeComponent.generateNewRoute());
        threadComponent.setExit(routeComponent.getState() == RouteComponent.State.STOP);
    }

    /**
     * Start thread
     */
    @Override
    public void start() { threadComponent.setRunning(true); }

    /**
     * Change thread running to the opposite
     */
    @Override
    public synchronized void switchRunning() { threadComponent.switchRunning(); }

    /**
     * Stop thread
     */
    @Override
    public void stop() { threadComponent.setRunning(false); }

    /**
     * End thread
     */
    @Override
    public synchronized void end() { threadComponent.setExit(true); }

    /**
     * @param newSimulationSpeed speed of the simulation (1 - normal)
     */
    @Override
    public void changeSimulationSpeed(double newSimulationSpeed) {
        threadComponent.setSimulationSpeed(newSimulationSpeed);
    }

    /**
     * Run method (from Runnable)
     */
    @Override
    public void run() {
        while (!threadComponent.isExit()) {
            threadComponent.startClock();
            if (threadComponent.isRunning())
                move();
            threadComponent.endClock();
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
        objectInfos.add(new TableCellComponent("State", routeComponent.getState().toString()));
        objectInfos.add(new TableCellComponent("Speed", Double.toString(movementComponent.getSpeed())));
        objectInfos.add(new TableCellComponent("Heading", movementComponent.getHeading().toString()));
        objectInfos.add(new TableCellComponent("Main route", routeComponent.getMainRoute().toString()));
        objectInfos.add(new TableCellComponent("Destination", routeComponent.getDest()));
        return objectInfos;
    }
}
