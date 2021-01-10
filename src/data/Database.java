package data;

import javafx.animation.AnimationTimer;
import object.base.AppObject;
import object.base.MovingObject;
import object.network.*;
import object.vehicle.aircraft.*;
import object.vehicle.ship.*;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Database {
    public enum ObjectType { AP, JU, TR, CA, MA, CS, AC }

    private static final HashMap<String, AppObject> appObjects = new HashMap<>();
    private static final HashMap<String, Thread> threads = new HashMap<>();
    private static AnimationTimer animationTimer = new AnimationTimer() { @Override public void handle(long l) { }};
    private static final HashSet<String> aircrafts = new HashSet<>();
    private static final HashSet<String> ships = new HashSet<>();
    private static final HashSet<String> junctions = new HashSet<>();
    private static final HashSet<String> tracks = new HashSet<>();
    private static final HashSet<String> airports = new HashSet<>();
    private static Graph airGraph, waterGraph;
    public static boolean isInit = false;


    /**
     * Init database with json files
     * @param filenames list of all json files in which data is stored
     * @param keys list of all keys for a given json file
     */
    public static void init(String [] filenames, String[][] keys) {
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

        airGraph = new Graph(airports, junctions, 'A');
        waterGraph = new Graph(airports, junctions, 'W');
        for (String id : appObjects.keySet())
            if (appObjects.get(id) instanceof MovingObject)
                ((MovingObject) appObjects.get(id)).initRoute(true);
        createThreads();
    }

    private static void initAppObjects(JSONArray contents) {
        for (int i = 0; i < contents.length(); i++)
            initAppObject(contents.getJSONObject(i), true);
    }

    public static void initAppObject(JSONObject obj, boolean isStartUp) {
        switch (ObjectType.valueOf(obj.getString("id").substring(0, 2))) {
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
        if (!isStartUp) {
            ((MovingObject) Database.getAppObjects().get(obj.getString("id"))).initRoute(true);
            threads.put(obj.getString("id"), new Thread((Runnable) appObjects.get(obj.getString("id"))));
            threads.get(obj.getString("id")).start();
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

    public static void startThreads() {
        for (String threadKey: threads.keySet())
            ((MovingObject) appObjects.get(threadKey)).start();
    }

    public static void switchRunningThreads() {
        for (String threadKey: threads.keySet())
            ((MovingObject) appObjects.get(threadKey)).switchRunning();
    }

    public static void stopThreads() {
        for (String threadKey: threads.keySet())
            ((MovingObject) appObjects.get(threadKey)).stop();
    }

    /**
     * End specific thread and remove it from threads HashMap
     * @param id id of MovingObject which is to be ended
     */
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

    /**
     * End all running threads and clear threads HashMap
     */
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

    /**
     * Clear database and end all running threads
     */
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

    public static LinkedList<String> createRoute(String startId, String endId, char assignment) {
        if (assignment == 'A') return airGraph.createRoute(startId, endId);
        else return waterGraph.createRoute(startId, endId);
    }

    public static LinkedList<String> getClosestAirport(String startId, char assignment) {
        return airGraph.getClosestAirport(startId, assignment);
    }

    public static Double getTracksWeight(String p1, String p2) {
        for (String trackId : tracks) {
            String[] points = ((Track) appObjects.get(trackId)).getPoints();
            if ((points[0].equals(p1) && points[1].equals(p2)) || (points[1].equals(p1) && points[0].equals(p2)))
                return ((Track) appObjects.get(trackId)).getLen();
        }
        return null;
    }

    public static HashMap<String, AppObject> getAppObjects() { return appObjects; }

    public static HashSet<String> getAircrafts() { return aircrafts; }

    public static HashSet<String> getShips() { return ships; }

    public static HashSet<String> getJunctions() { return junctions; }

    public static HashSet<String> getTracks() { return tracks; }

    public static HashSet<String> getAirports() { return airports; }

    public static String getNetworkObject(int x, int y) {
        String pointCoord = Utility.Math.intToString(x / 50, 2) + Utility.Math.intToString(y / 50, 2);
        for (String junction: junctions) {
            if (junction.contains(pointCoord)) return junction;
        }
        for (String airport: airports) {
            if (airport.contains(pointCoord)) return airport;
        }
        return null;
    }

    public static boolean isInit() { return isInit; }

}
