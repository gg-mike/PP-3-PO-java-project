package util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Utility {

    public static class Convertors {
        public static LinkedList<String> array2linkedList(ArrayList<Object> array) {
            LinkedList<String> linkedList = new LinkedList<>();
            for (Object elem : array)
                linkedList.add((String) elem);
            return linkedList;
        }
    }

    public static class JSONInfo {
        private static JSONObject jsonObject;

        public static void init(String data) {
            jsonObject = new JSONObject(data);
        }

        public static Object get(String key) {
            return jsonObject.get(key);
        }

        public static ArrayList<Object> getArray(String key) {
            JSONArray jsonArray = jsonObject.getJSONArray(key);
            ArrayList<Object> objects = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++)
                objects.add(jsonArray.get(i));
            return objects;
        }
    }

    public static class StringInfo {
        private static String stringData;

        public static void init(String data) { stringData = data; }

        public static String getId() { return stringData; }

        public static double getX() { return 50d * Double.parseDouble(stringData.substring(3, 5)); }

        public static double getY() { return 50d * Double.parseDouble(stringData.substring(5, 7)); }

    }

    public static class Math {
        private static final Random random = new Random();

        public static Number clamp(Number val, Number min, Number max) {
            if (val.doubleValue() < min.doubleValue())
                return min;
            else if (val.doubleValue() > max.doubleValue())
                return max;
            return val;
        }

        public static double distance(double x1, double y1, double x2, double y2) {
            return java.lang.Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        }

        public static int randInt(int min, int max) {
            return random.nextInt((max - min) + 1) + min;
        }

        public static String intToString(int val, int width) {
            String valString = String.valueOf(val);
            while (valString.length() < width) valString = String.format("0%s", valString);
            return valString;
        }
    }
}
