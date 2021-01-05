package data;

import javafx.animation.AnimationTimer;
import obj.base.AppObject;
import obj.base.MovingObject;
import obj.network.*;
import obj.vehicle.aircraft.*;
import obj.vehicle.ship.*;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Database {
    public enum ObjectAssignment { AIR, WATER }
    public enum ObjectType { AP, JU, TR, CA, MA, CS, AC }

    private static final HashMap<String, AppObject> appObjects = new HashMap<>();
    private static final HashMap<String, Thread> threads = new HashMap<>();
    private static AnimationTimer animationTimer = new AnimationTimer() { @Override public void handle(long l) { }};
    private static final HashSet<String> aircrafts = new HashSet<>();
    private static final HashSet<String> ships = new HashSet<>();
    private static final HashSet<String> junctions = new HashSet<>();
    private static final HashSet<String> tracks = new HashSet<>();
    private static final HashSet<String> airports = new HashSet<>();
    private static Graph airGraph;
    private static Graph waterGraph;
    public static boolean isInit = false;

    public static void init(String[] filenames, String[][] keys) {
        for (int i = 0; i < filenames.length; i++) {
            for (int j = 0; j < keys[i].length; j++) {
                try {
                    JSONObject mainObj = new JSONObject(new String(Files.readAllBytes(Paths.get(filenames[i]))));
                    JSONArray contents = mainObj.getJSONArray(keys[i][j]);
                    initAppObjects(contents);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        isInit = true;
        airGraph = new Graph(airports, junctions, ObjectAssignment.AIR);
        waterGraph = new Graph(airports, junctions, ObjectAssignment.WATER);
        for (String id : appObjects.keySet())
            if (appObjects.get(id) instanceof MovingObject)
                ((MovingObject) appObjects.get(id)).initRoute();
        createThreads();
    }

    private static void initAppObjects(JSONArray contents) {
        for (int i = 0; i < contents.length(); i++) {
            JSONObject obj = contents.getJSONObject(i);
            Utility.StringInfo.init(obj.getString("id"));
            switch (ObjectType.valueOf(Utility.StringInfo.getObjectType())) {
                case AP -> {
                    appObjects.put(obj.getString("id"), new Airport(obj.toString()));
                    airports.add(obj.getString("id"));
                }
                case JU -> {
                    appObjects.put(obj.getString("id"), new Junction(obj.toString()));
                    junctions.add(obj.getString("id"));
                }
                case TR -> {
                    appObjects.put(obj.getString("id"), new Track(obj.toString()));
                    tracks.add(obj.getString("id"));
                }
                case CA -> {
                    appObjects.put(obj.getString("id"), new CivilAircraft(obj.toString()));
                    aircrafts.add(obj.getString("id"));
                }
                case MA -> {
                    appObjects.put(obj.getString("id"), new MilitaryAircraft(obj.toString()));
                    aircrafts.add(obj.getString("id"));
                }
                case CS -> {
                    appObjects.put(obj.getString("id"), new CruiseShip(obj.toString()));
                    ships.add(obj.getString("id"));
                }
                case AC -> {
                    appObjects.put(obj.getString("id"), new AircraftCarrier(obj.toString()));
                    ships.add(obj.getString("id"));
                }
                default -> System.out.println("Wrong Type: " + obj.getString("id"));
            }
        }
    }

    private static void createThreads() {
        for (String aircraft : aircrafts)
            threads.put(aircraft, new Thread((Runnable) appObjects.get(aircraft)));
        for (String ship : ships)
            threads.put(ship, new Thread((Runnable) appObjects.get(ship)));
        for (String threadKey : threads.keySet())
            threads.get(threadKey).start();
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                for (String obj: appObjects.keySet())
                    appObjects.get(obj).update();
            }
        };
        animationTimer.start();
    }

    public static void startStopThreads() {
        for (String threadKey: threads.keySet())
            ((MovingObject) appObjects.get(threadKey)).startStop();
    }

    public static void endThread(String id) {
        if (threads.containsKey(id)) {
            try {
                ((MovingObject) appObjects.get(id)).end();
                threads.get(id).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            threads.remove(id);
        }
    }

    public static void endThreads() {
        if (threads.size() != 0) {
            for (String threadKey : threads.keySet()) {
                try {
                    ((MovingObject) appObjects.get(threadKey)).end();
                    threads.get(threadKey).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            threads.clear();
        }
    }

    public static void clear() {
        endThreads();
        animationTimer.stop();
        appObjects.clear();
        aircrafts.clear();
        ships.clear();
        junctions.clear();
        tracks.clear();
        airports.clear();
        isInit = false;
    }

    private static Double getTracksWeight(String p1, String p2) {
        for (String trackId : tracks) {
            String[] points = ((Track) appObjects.get(trackId)).getPoints();
            if ((points[0].equals(p1) && points[1].equals(p2)) || (points[1].equals(p1) && points[0].equals(p2)))
                return ((Track) appObjects.get(trackId)).getLen();
        }
        return null;
    }

    public static LinkedList<String> createRoute(String startId, String endId, ObjectAssignment assignment) {
        Graph graph;
        if (assignment == ObjectAssignment.AIR) graph = airGraph;
        else graph = waterGraph;
        LinkedList<String> route = new LinkedList<>();

        HashMap<String, String> con = new HashMap<>();
        for (String nodeId : graph.getNodes().keySet())
            con.put(nodeId, null);
        HashSet<String> visited = new HashSet<>();
        LinkedList<String> points = new LinkedList<>();
        points.add(startId);
        graph.resetNodes();
        graph.getNodes().get(startId).currWeight = 0d;

        while (!points.isEmpty()) {
            String p = points.remove();

            for (String c : graph.getNodes().get(p).connections) {
                Double cw = getTracksWeight(p, c);
                if (cw == null)
                    System.out.println("Database.createRoute: track between " + p + " and " + c + " not found");
                else {
                    double w = graph.getNodes().get(p).currWeight + cw;
                    if (graph.getNodes().get(c).currWeight > w) {
                        graph.getNodes().get(c).currWeight = w;
                        con.put(c, p);
                    }
                    if (!visited.contains(c)) {
                        points.add(c);
                        visited.add(c);
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

    public static HashMap<String, AppObject> getAppObjects() { return appObjects; }

    public static HashSet<String> getAircrafts() { return aircrafts; }

    public static HashSet<String> getShips() { return ships; }

    public static HashSet<String> getJunctions() { return junctions; }

    public static HashSet<String> getTracks() { return tracks; }

    public static HashSet<String> getAirports() { return airports; }

    public static boolean isInit() { return isInit; }

    public static String toStringStatic() {
        StringBuilder text = new StringBuilder();
        for (String key: appObjects.keySet()) {
            text.append(appObjects.get(key).toString());
        }
        return text.toString();
    }
}
