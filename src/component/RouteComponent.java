package component;

import data.Database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Responsible for proper route management (creating intermediate route, setting destinations, emergency stops)
 */
public class RouteComponent {
    public enum RouteType { ONCE, CONNECTING, AFTER_CONNECTING, CIRCLES, THERE_AND_BACK }
    public enum State { CONNECTING_TO_TRAFFIC_X,  CONNECTING_TO_TRAFFIC_Y, MOVING, WAITING_JUNCTION, WAITING_AIRPORT, JUNCTION, AIRPORT, WAITING_TRACK, STOP }

    private final Database.ObjectType objectType;
    private State state;
    private LinkedList<String> mainRoute;
    private final LinkedList<String> initialMainRoute;
    private RouteType routeType;
    private final RouteType initialRouteType;

    private LinkedList<String> intermediateRoute;
    private LinkedList<Integer> mainInIntermediate;

    private String dest, tmpDest, usedTrack = null;

    /**
     * Constructor
     * @param mainRoute array of stop's ids
     * @param routeType route type (used for generating new routes)
     * @param objectType object type required for proper intermediate route generation
     */
    public RouteComponent(LinkedList<String> mainRoute, RouteType routeType, Database.ObjectType objectType) {
        checkRoute(mainRoute);

        if (routeType == RouteType.CIRCLES && !mainRoute.getFirst().equals(mainRoute.getLast()))
            mainRoute.add(mainRoute.getFirst());
        this.objectType = objectType;
        this.initialMainRoute = mainRoute;
        this.mainRoute = mainRoute;
        this.initialRouteType = routeType;
        this.routeType = routeType;
    }

    /**
     * New route creation and destination position retrieval for MovementComponent
     * @param firstTime determines if objects heading needs to be adjusted for a new route
     *                  (should be true only for a newly created object)
     * @return array of positional coordinates of the destination (4 - if firstTime == true, else 2)
     */
    public ArrayList<Double> initRoute(boolean firstTime) {
        initIntermediateRoute();
        dest = intermediateRoute.remove();
        if (firstTime) {
            double destX = Database.getAppObjects().get(dest).getGUI_X();
            double destY = Database.getAppObjects().get(dest).getGUI_Y();
            state = (Database.getAppObjects().get(dest).getId().startsWith("AP")) ? State.WAITING_AIRPORT : State.WAITING_JUNCTION;
            if (intermediateRoute.size() != 0) {
                String nextDestId = intermediateRoute.getFirst();
                double nextDestX = Database.getAppObjects().get(nextDestId).getGUI_X();
                double nextDestY = Database.getAppObjects().get(nextDestId).getGUI_Y();
                return new ArrayList<>(Arrays.asList(destX, destY, nextDestX, nextDestY));
            }
            return new ArrayList<>(Arrays.asList(destX, destY));
        }
        else
            return setNewDest();
    }

    /**
     * Check if given route has valid ids (should be used only for checking validity of the route from json file,
     * preferable should be deleted in final version)
     * @param route route to be checked
     */
    private void checkRoute(LinkedList<String> route) {
        try {
            for (String elem : route)
                if (!Database.getAirports().contains(elem) && !Database.getJunctions().contains(elem)) {
                    System.out.println("RouteComponent.checkRoute(): Invalid element in route, elem=" + route);
                    throw new Exception();
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the shortest path between subsequent stops
     */
    private void initIntermediateRoute() {
        intermediateRoute = new LinkedList<>();
        mainInIntermediate = new LinkedList<>();
        char assignment = (objectType == Database.ObjectType.CA || objectType == Database.ObjectType.MA)? 'A' : 'W';
        boolean isCivil = objectType == Database.ObjectType.CA || objectType == Database.ObjectType.CS;
        for (int i = 0; i < mainRoute.size() - 1; i++) {
            intermediateRoute.addAll(Database.createRoute(mainRoute.get(i), mainRoute.get(i+1), assignment, isCivil));
            intermediateRoute.removeLast();
        }
        intermediateRoute.add(mainRoute.getLast());

        for (String mainStop : mainRoute)
            mainInIntermediate.add(intermediateRoute.size() - intermediateRoute.indexOf(mainStop));
    }

    /**
     * Retrieve position of the next destination
     * @return position of the new destination (return null if intermediateRoute.size() == 0)
     */
    public ArrayList<Double> setNewDest() {
        if (intermediateRoute.size() != 0) {
            dest = intermediateRoute.remove();
            double destX = Database.getAppObjects().get(dest).getGUI_X();
            double destY = Database.getAppObjects().get(dest).getGUI_Y();
            return new ArrayList<>(Arrays.asList(destX, destY));
        }
        dest = null;
        return null;
    }

    /**
     * Generates new route and return the position of the first destination
     * @return array of positional coordinates of the destination (return null if route == ONCE)
     */
    public ArrayList<Double> generateNewRoute() {
        switch (routeType) {
            case ONCE -> state = State.STOP;
            case CONNECTING -> {
                routeType = RouteType.AFTER_CONNECTING;
                mainRoute.clear();
                mainRoute.add(tmpDest);
                mainRoute.add(initialMainRoute.getFirst());
                return initRoute(false);
            }
            case AFTER_CONNECTING -> {
                routeType = initialRouteType;
                mainRoute = initialMainRoute;
                return initRoute(false);
            }
            case CIRCLES -> {
                return initRoute(false);
            }
            case THERE_AND_BACK -> {
                Collections.reverse(mainRoute);
                return initRoute(false);
            }
        }
        return null;
    }

    /**
     * Perform necessary actions in case of the emergency (find closest airport and fly there)
     */
    public void emergency() {
        mainRoute = Database.getClosestAirport(dest, objectType == Database.ObjectType.CA);
        tmpDest = mainRoute.getLast();
        intermediateRoute = mainRoute;
        intermediateRoute.removeFirst();
        routeType = RouteType.CONNECTING;
    }

    /**
     * Checks if current stop is a part of the main route
     * @return true if mainRoute stop
     */
    public boolean isMainRouteStop() { return mainInIntermediate.contains(intermediateRoute.size() + 1); }

    /**
     * @return state == MOVING
     */
    public boolean isMoving() { return state == State.MOVING; }

    /**
     * @return state == WAITING_*
     */
    public boolean isWaiting() {
        return  state == State.WAITING_AIRPORT ||
                state == State.WAITING_JUNCTION ||
                state == State.WAITING_TRACK;
    }

    /**
     * Get state of the object
     * @return state
     */
    public State getState() { return state; }

    /**
     * Set new state for the object
     * @param state new state
     */
    public void setState(State state) { this.state = state; }

    /**
     * Get main route
     * @return mainRoute
     */
    public LinkedList<String> getMainRoute() { return mainRoute; }

    /**
     * Set new route type for the object
     * @param routeType new routeType
     */
    public void setRouteType(RouteType routeType) { this.routeType = routeType; }

    /**
     * Get current destination id
     * @return dest
     */
    public String getDest() { return dest; }

    /**
     * Get temporary destination id (used by emergency and connecting to the route)
     * @return tmpDest
     */
    public String getTmpDest() { return tmpDest; }

    /**
     * Set new temporary destination for the object
     * @param tmpDest new tmpDest
     */
    public void setTmpDest(String tmpDest) { this.tmpDest = tmpDest; }

    /**
     * Get currently used track id
     * @return usedTrack
     */
    public String getUsedTrack() { return usedTrack; }

    /**
     * Set new used track id
     * @param usedTrack new usedTrack
     */
    public void setUsedTrack(String usedTrack) { this.usedTrack = usedTrack; }
}
