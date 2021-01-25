package data;

import javafx.util.Pair;
import util.Utility;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Generation of the object's json
 */
public class ObjectGenerator {

    /**
     * Generate json object
     * @param type vehicle type (0 -> CA, 1 -> MA, 2 -> CS, else -> AC)
     * @param speed speed of the vehicle
     * @param route main route points
     * @param routeType type of route (0 -> ONCE, 1 -> CIRCLES, else -> THERE_END_BACK)
     * @param stuffN stuff number (needed for CA, MA)
     * @param maxPassN max passenger number (needed for CA, CS)
     * @param maxFuel max fuel (needed for CA, MA)
     * @param weaponType weapon type (needed for MA, AC)
     * @param company company name (needed for MS)
     * @return generated json object
     */
    public static String generateJSON(int type, Integer speed, LinkedList<Pair<Integer, Integer>> route, int routeType,
                                Integer stuffN, Integer maxPassN, Double maxFuel, String weaponType, String company) {
        return "{ " + getId(type) + ", \"speed\": "+ speed + ", \n" +
                getOtherParams(stuffN, maxPassN, maxFuel, weaponType, company) + "\n" +
                getRoute(route, routeType) + " }";
    }

    /**
     * Generate vehicle id
     * @param type vehicle type (0 -> CA, 1 -> MA, 2 -> CS, else -> AC)
     * @return vehicle id
     */
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

    /**
     * Generate route
     * @param points main route points
     * @param type route type
     * @return route
     */
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

    /**
     * Generate other parameters values
     * @param stuffN stuff number (needed for CA, MA)
     * @param maxPassN max passenger number (needed for CA, CS)
     * @param maxFuel max fuel (needed for CA, MA)
     * @param weaponType weapon type (needed for MA, AC)
     * @param company company name (needed for MS)
     * @return other parameters values
     */
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
