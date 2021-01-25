package util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Functionality required by different object of the application
 */
public class Utility {

    public static class Convertors {

        /**
         * Converts ArrayList to LinkedList
         * Used for reading json files
         * @param array ArrayList object to be converted
         * @return LinkedList consisting of String
         */
        public static LinkedList<String> array2linkedList(ArrayList<Object> array) {
            LinkedList<String> linkedList = new LinkedList<>();
            for (Object elem : array)
                linkedList.add((String) elem);
            return linkedList;
        }
    }

    /**
     * Functionality needed for quick data extraction from json file
     */
    public static class JSONInfo {
        private static JSONObject jsonObject;

        /**
         * @param data json object string
         */
        public static void init(String data) {
            jsonObject = new JSONObject(data);
        }

        /**
         * @param key json key a single object
         * @return object specific to the given key
         */
        public static Object get(String key) {
            return jsonObject.get(key);
        }

        /**
         * @param key json key for an array
         * @return array specific to the given key
         */
        public static ArrayList<Object> getArray(String key) {
            JSONArray jsonArray = jsonObject.getJSONArray(key);
            ArrayList<Object> objects = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++)
                objects.add(jsonArray.get(i));
            return objects;
        }
    }

    /**
     * Extracts data from id of the object
     */
    public static class StringInfo {
        private static String stringData;

        public static void init(String data) { stringData = data; }

        public static String getId() { return stringData; }

        public static double getX() { return 50d * Double.parseDouble(stringData.substring(3, 5)); }

        public static double getY() { return 50d * Double.parseDouble(stringData.substring(5, 7)); }

    }

    /**
     * Math calculations needed in the application
     */
    public static class Math {
        private static final Random random = new Random();

        public static Number clamp(Number val, Number min, Number max) {
            if (val.doubleValue() < min.doubleValue())
                return min;
            else if (val.doubleValue() > max.doubleValue())
                return max;
            return val;
        }

        public static int closestMultiple(Number val, int multiple) {
            double multiplesInVal = val.doubleValue() / multiple;
            int low = (int) (multiple * java.lang.Math.floor(multiplesInVal));
            int high = (int) (multiple * java.lang.Math.ceil(multiplesInVal));
            if (val.doubleValue() - low <= high - val.doubleValue()) return low;
            else return high;
        }

        public static double dist(double x1, double y1, double x2, double y2) {
            return java.lang.Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        }

        public static String intToString(int val, int width) {
            String valString = String.valueOf(val);
            while (valString.length() < width) valString = String.format("0%s", valString);
            return valString;
        }

        public static int randInt(int min, int max) {
            return random.nextInt((max - min) + 1) + min;
        }

    }
}
