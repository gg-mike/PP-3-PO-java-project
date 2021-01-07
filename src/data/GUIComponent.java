package data;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

/**
 * Unmovable GUI object
 */
public class GUIComponent {
    public enum ShapeType { LINE, CIRCLE, POLYGON }

    protected static class GUIPolygon {
        public static final Double[] civilAirplane = {
                0.0, 5.0,
                -0.4, 4.3,
                -2.0, 4.8,
                -1.9, 4.4,
                -0.6, 3.6,
                -0.7, 3.4,
                -0.7, 0.7,
                -5.0, 1.3,
                -4.8, 0.6,
                -2.7, -0.3,
                -2.8, -0.5,
                -2.8, -1.0,
                -2.7, -1.1,
                -2.1, -1.1,
                -2.0, -1.0,
                -2.0, -0.6,
                -0.7, -1.3,
                -0.7, -3.9,
                -0.6, -4.5,
                -0.5, -4.7,
                -0.3, -4.9,
                0.0, -5.0,
                0.3, -4.9,
                0.5, -4.7,
                0.6, -4.5,
                0.7, -3.9,
                0.7, -1.3,
                2.0, -0.6,
                2.0, -1.0,
                2.1, -1.1,
                2.7, -1.1,
                2.8, -1.0,
                2.8, -0.5,
                2.7, -0.3,
                4.8, 0.6,
                5.0, 1.3,
                0.7, 0.7,
                0.7, 3.4,
                0.6, 3.6,
                1.9, 4.4,
                2.0, 4.8,
                0.4, 4.3,
        };
        public static final Double[] militaryAirplane = {
                0.0, 5.0,
                -0.3, 4.2,
                -1.8, 4.4,
                -2.0, 3.6,
                -0.9, 3.0,
                -1.0, 2.6,
                -3.4, 3.4,
                -3.3, 2.5,
                -0.8, 0.0,
                -0.4, -4.0,
                0.0, -5.0,
                0.4, -4.0,
                0.8, 0.0,
                3.3, 2.5,
                3.4, 3.4,
                1.0, 2.6,
                0.9, 3.0,
                2.0, 3.6,
                1.8, 4.4,
                0.3, 4.2,
        };
        public static final Double[] cruiseShip = {
                0.0,  5.0,
                -1.4, 4.5,
                -1.7, 4.3,
                -1.7, -2.2,
                -1.4, -3.5,
                -1.0, -4.2,
                0.0, -5.0,
                1.0, -4.2,
                1.4, -3.5,
                1.7, -2.2,
                1.7, 4.3,
                1.4, 4.5,
        };
        public static final Double[] aircraftCarrier = {
                0.0, 5.0,
                -1.4, 5.0,
                -1.4, 4.8,
                -2.0, 4.7,
                -2.0, 4.1,
                -2.2, 3.9,
                -2.2, 0.4,
                -2.4, 0.2,
                -2.4, -1.1,
                -0.9, -2.5,
                -0.5, -5.0,
                0.5, -5.0,
                0.9, -2.5,
                2.4, -1.1,
                2.4, 4.7
        };

    }

    protected ShapeType shapeType;
    protected Shape shape = null;

    // INIT

    /**
     * Constructor
     * @param id id of the object
     * @param coords coordinates for the shape (4 for LINE [x1, y1, x2, y2], 2 for overs [x, y])
     */
    public GUIComponent(String id, Double... coords) { init(id, coords); }

    /**
     * Init shape
     * @param id id of the object
     * @param coords coordinates for the shape (4 for LINE [x1, y1, x2, y2], 2 for overs [x, y])
     */
    public void init(String id, Double... coords) {
        Color fill = null, stroke = null;
        Double size = null;
        Double[] points = null;
        switch(Database.ObjectType.valueOf(id.substring(0, 2))) {
            case AP -> {
                shapeType = ShapeType.CIRCLE;
                fill = Color.valueOf("#b4202a");
                if (id.charAt(2) == 'M') stroke = Color.BLACK;
                size = 10d;
            }
            case JU -> {
                shapeType = ShapeType.CIRCLE;
                fill = Color.valueOf("#9cdb43");
                size = 8d;
            }
            case TR -> {
                shapeType = ShapeType.LINE;
                stroke = Color.valueOf((id.charAt(2) == 'A') ? "#fa6a0a" : "#249fde");
                size = (id.charAt(4) == '2')? 4d : 2d;
            }
            case CA -> {
                shapeType = ShapeType.POLYGON;
                stroke = Color.BLACK;
                points = GUIPolygon.civilAirplane;
            }
            case MA -> {
                shapeType = ShapeType.POLYGON;
                stroke = Color.BLACK;
                points = GUIPolygon.militaryAirplane;
            }
            case CS -> {
                shapeType = ShapeType.POLYGON;
                stroke = Color.BLACK;
                points = GUIPolygon.cruiseShip;
            }
            case AC -> {
                shapeType = ShapeType.POLYGON;
                stroke = Color.BLACK;
                points = GUIPolygon.aircraftCarrier;
            }
        }

        switch (shapeType) {
            case LINE -> {
                shape = new Line();
                ((Line) shape).setStartX(coords[0]);
                ((Line) shape).setStartY(coords[1]);
                ((Line) shape).setEndX(coords[2]);
                ((Line) shape).setEndY(coords[3]);
                shape.setStroke(stroke);
                shape.setStrokeWidth(size);
            }
            case CIRCLE -> {
                shape = new Circle();
                ((Circle) shape).setCenterX(coords[0]);
                ((Circle) shape).setCenterY(coords[1]);
                ((Circle) shape).setRadius(size);
                shape.setFill(fill);
                if (stroke != null) {
                    shape.setStroke(stroke);
                    shape.setStrokeWidth(2);
                }
            }
            case POLYGON -> {
                shape = new Polygon();
                ((Polygon) shape).getPoints().addAll(points);
                shape.setLayoutX(coords[0]);
                shape.setLayoutY(coords[1]);
                shape.setScaleX(1.5);
                shape.setScaleY(1.5);
                shape.setStroke(stroke);
                shape.setStrokeWidth(2);
            }
        }

        shape.setId(id);
        shape.setUserData(id);
    }

    // FUNCTIONALITY

    /**
     * Update stroke color
     * @param stroke stroke color (null - reset to default)
     */
    public void update(Color stroke) {
        if (shapeType != ShapeType.LINE) {
            if (stroke != null) {
                shape.setStroke(stroke);
                shape.setStrokeWidth(2);
            } else if (getId().charAt(2) == 'M')
                shape.setStroke(Color.BLACK);
            else
                shape.setStrokeWidth(0);
        }
    }

    // GETTERS / SETTERS

    public String getId() { return shape.getId(); }

    public Double[] getCoords() {
        switch (shapeType) {
            case LINE -> { return new Double[] {
                    ((Line) shape).getStartX(), ((Line) shape).getEndX(),
                    ((Line) shape).getStartY(), ((Line) shape).getEndY()
            }; }
            case CIRCLE -> { return new Double[] {
                    ((Circle) shape).getCenterX(), ((Circle) shape).getCenterY()
            }; }
            case POLYGON -> { return new Double[] {
                    shape.getLayoutX(),  shape.getLayoutY()
            }; }
        }
        return null;
    }

    public Shape getShape() { return shape; }
}
