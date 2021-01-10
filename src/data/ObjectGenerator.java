package data;

import javafx.util.Pair;
import util.Utility;

import java.util.HashSet;
import java.util.LinkedList;

public class ObjectGenerator {

    public static String generateJSON(int type, Integer speed, LinkedList<Pair<Integer, Integer>> route, int routeType,
                                Integer stuffN, Integer maxPassN, Double maxFuel, String weaponType, String company) {
        return "{ " + getId(type) + ", \"speed\": "+ speed + ", \n" +
                getOtherParams(stuffN, maxPassN, maxFuel, weaponType, company) + "\n" +
                getRoute(route, routeType) + " }";
    }

    private static String getId(int type) {
        String id;
        HashSet<String> vehSet;
        switch (type) {
            case 0 -> {
                id = "CA";
                vehSet = Database.getAircrafts();
            }
            case 1 -> {
                id = "MA";
                vehSet = Database.getAircrafts();
            }
            case 2 -> {
                id = "CS";
                vehSet = Database.getShips();
            }
            default -> {
                id = "AC";
                vehSet = Database.getShips();
            }
        }
        id += '-';
        do {
            id = id.substring(0, 3);
            id += Utility.Math.intToString(Utility.Math.randInt(1, 9999), 4);
        } while (vehSet.contains(id));
        id = "\"" + id + "\"";
        return "\"id\": " + id;
    }

    private static String getRoute(LinkedList<Pair<Integer, Integer>> points, int type) {
        StringBuilder routeSB = new StringBuilder("[");

        for (Pair<Integer, Integer> point: points) {
            String pointId = Database.getNetworkObject(point.getKey(), point.getValue());
            if (pointId == null)
                System.out.println("Id not found for x=" + point.getKey() + ", y=" + point.getValue());
            else
                routeSB.append("\"").append(pointId).append("\", ");
        }
        String routeType;
        switch (type) {
            case 0 -> routeType = "\"ONCE\"";
            case 1 -> routeType = "\"CIRCLES\"";
            default -> routeType = "\"THERE_AND_BACK\"";
        }

        return "\"route\": " + routeSB.substring(0, routeSB.length() - 2) + "] , \"routeType\": " + routeType;
    }

    private static String getOtherParams(Integer stuffN, Integer maxPassN, Double maxFuel, String weaponType, String company) {
        String other = "";
        if (stuffN != null)
            other += "\"stuffN\": " + stuffN.toString() + ", ";
        if (maxPassN != null)
            other += "\"maxPassengerN\": " + maxPassN.toString() + ", ";
        if (maxFuel != null)
            other += "\"fuelState\": [" + maxFuel.toString() + ", " + maxFuel.toString() + "], ";
        if (weaponType != null)
            other += "\"weaponType\": \"" + weaponType + "\", ";
        if (company != null)
            other += "\"company\": \"" + company + "\", ";
        return other;
    }
}
