package data;

import object.network.Junction;
import object.network.NetworkObject;
import object.network.Track;

import java.util.*;

/**
 * Holds all the data about nodes and connections between them
 */
class Graph {
    /**
     * Single node in the graph, which holds ids of its connections
     */
    public static class Node {
        public String id;
        public double currWeight = Double.MAX_VALUE;
        public HashSet<String> connections = new HashSet<>();

        public Node(String id) {
            this.id = id;
        }
    }

    private final HashMap<String, Node> nodes = new HashMap<>();

    /**
     * Constructor
     * @param nodesId_a airports
     * @param nodesId_j junctions
     * @param assignment A - air, W - water
     */
    public Graph(HashSet<String> nodesId_a, HashSet<String> nodesId_j, char assignment) {
        if (assignment == 'A') {
            for (String nodeId : nodesId_a)
            nodes.put(nodeId, new Node(nodeId));
            for (String nodeId : nodesId_j)
                if (((NetworkObject) Database.getAppObjects().get(nodeId)).getAssignment() == 'A')
                    nodes.put(nodeId, new Node(nodeId));
        } else {
            for (String nodeId : nodesId_j)
                if (((NetworkObject) Database.getAppObjects().get(nodeId)).getAssignment() == 'W')
                    nodes.put(nodeId, new Node(nodeId));
        }
       createConnections();
    }

    /**
     * Generate connections for nodes
     */
    private void createConnections() {
        for (String nodeId : nodes.keySet()) {
            for (String trackId : ((Junction) Database.getAppObjects().get(nodeId)).getTracks().values()) {
                String[] points = ((Track) Database.getAppObjects().get(trackId)).getPoints();
                if (points[0].equals(nodeId))
                    nodes.get(nodeId).connections.add(points[1]);
                else
                    nodes.get(nodeId).connections.add(points[0]);
            }
        }
    }

    /**
     * Reset nodes weight to 0
     */
    private void resetNodes() {
        for (String nodeId : nodes.keySet())
            nodes.get(nodeId).currWeight = Double.MAX_VALUE;
    }

    /**
     * Generate route
     * @param startId start point id
     * @param endId end point id
     * @param isCivil should algorithm consider military airports in path-finding
     * @return route
     */
    public LinkedList<String> createRoute(String startId, String endId, boolean isCivil) {
        LinkedList<String> route = new LinkedList<>();

        HashMap<String, String> con = new HashMap<>();
        for (String nodeId : nodes.keySet())
            con.put(nodeId, null);
        HashSet<String> visited = new HashSet<>();
        LinkedList<String> points = new LinkedList<>();
        points.add(startId);
        resetNodes();
        nodes.get(startId).currWeight = 0d;

        while (!points.isEmpty()) {
            String p = points.remove();

            for (String c : nodes.get(p).connections) {
                if (!isCivil || !c.startsWith("APM")) {
                    Double cw = Database.getTracksWeight(p, c);
                    if (cw == null)
                        System.out.println("Database.createRoute: track between " + p + " and " + c + " not found");
                    else {
                        double w = nodes.get(p).currWeight + cw;
                        if (nodes.get(c).currWeight > w) {
                            nodes.get(c).currWeight = w;
                            con.put(c, p);
                        }
                        if (!visited.contains(c)) {
                            points.add(c);
                            visited.add(c);
                        }
                    }
                }
            }
        }

        if (con.get(endId) != null) {
            String id = endId;
            while (id != null) {
                route.add(id);
                id = con.get(id);
            }
            Collections.reverse(route);
        }
        return route;
    }

    /**
     * Calculate route length
     * @param route route points
     * @return length of the route
     */
    private double calculateLength(LinkedList<String> route) {
        double len = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            Double l = Database.getTracksWeight(route.get(i), route.get(i+1));
            if (l != null) len += l;
            else System.out.println(route.get(i) + " " + route.get(i+1));
        }
        return len;
    }

    /**
     * Find the closest airport and generate route to this point
     * @param startId start point id
     * @param isCivil should algorithm consider military airports in path-finding
     * @return route
     */
    public LinkedList<String> getClosestAirport(String startId, boolean isCivil) {
        ArrayList<LinkedList<String>> possibleRoutes = new ArrayList<>();
        ArrayList<Double> routesLens = new ArrayList<>();
        for (String airport : Database.getAirports()) {
            if (!isCivil || airport.charAt(2) == 'C') {
                possibleRoutes.add(createRoute(startId, airport, isCivil));
                routesLens.add(calculateLength(possibleRoutes.get(possibleRoutes.size() - 1)));
            }
        }
        return possibleRoutes.get(routesLens.indexOf(Collections.min(routesLens)));
    }

}