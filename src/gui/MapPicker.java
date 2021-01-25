package gui;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Pair;

import java.util.LinkedList;

/**
 * Functionality connected with route picking
 */
public class MapPicker {

    /**
     * Functionality connected with single route token
     */
    protected static class Token {
        private int index, indexOnPoint;
        private final double x, y;
        private final Circle circle;
        private final Label label;

        public Token(int index, int indexOnPoint, double x, double y) {
            this.index = index;
            this.indexOnPoint = indexOnPoint;
            this.x = x;
            this.y = y;
            this.circle = new Circle();
            this.circle.setFill(Color.WHITE);
            this.circle.setRadius(7);
            this.circle.setStrokeWidth(0);
            this.label = new Label();
            this.label.setFont(Font.font("System", FontWeight.BLACK, 8));
            this.label.setPrefWidth(14);
            this.label.setPrefHeight(14);
            this.label.setAlignment(Pos.CENTER);
            updateCircle();
            updateLabel();
        }

        /**
         * Update circle id and coordinates
         */
        private void updateCircle() {
            this.circle.setId(Integer.toString(index));
            this.circle.setCenterX(x + 15 + 14.5 * indexOnPoint);
            this.circle.setCenterY(y - 15);
        }

        /**
         * Update label id, text and coordinates
         */
        private void updateLabel() {
            this.label.setId(Integer.toString(index));
            this.label.setText(Integer.toString(index));
            this.label.setLayoutX(x + 7.5 + 14.5 * indexOnPoint);
            this.label.setLayoutY(y - 22.5);
        }

        /**
         * Compare (x, y) coordinates with token's coordinates
         * @param x x coordinate
         * @param y y coordinate
         * @return this.x == x && this.y == y
         */
        public boolean compareCoord(double x, double y) {
            return this.x == x && this.y == y;
        }
    }

    public static final Group mainGroup = new Group();

    private static final Group circleGroup = new Group();
    private static final Group labelGroup = new Group();
    private static final LinkedList<Token> tokens = new LinkedList<>();

    static  {
        mainGroup.getChildren().setAll(circleGroup, labelGroup);
    }

    /**
     * Add new token with a given coordinates
     * @param x x coordinate
     * @param y y coordinate
     * @return new token's label (null if tokens.size() >= 10)
     */
    public static Label addToken(double x, double y) {
        if (tokens.size() < 10) {
            if (tokens.size() == 0 || !tokens.getLast().compareCoord(x, y)) {
                int indexOnPoint = 0;
                for (Token token : tokens) {
                    if (token.compareCoord(x, y))
                        indexOnPoint++;
                }
                if (indexOnPoint < 3) {
                    tokens.add(new Token(tokens.size(), indexOnPoint, x, y));
                    circleGroup.getChildren().add(tokens.getLast().circle);
                    labelGroup.getChildren().add(tokens.getLast().label);
                }
                return tokens.getLast().label;
            }
        }
        return null;
    }

    /**
     * Remove a token with a given index
     * @param index index of the token which is to be removed
     */
    public static void remove(int index) {
        if (tokens.size() > index) {
            Token removedToken = tokens.remove(index);
            Token removedToken2 = null;

            if (tokens.size() > index && index > 0 && tokens.get(index - 1).compareCoord(tokens.get(index).x, tokens.get(index).y))
                    removedToken2 = tokens.remove(index);

            for (int i = index; i < tokens.size(); i++)
                tokens.get(i).index = i;

            int indexOnPoint = 0, indexOnPoint2 = 0;
            for (Token token: tokens) {
                if (token.compareCoord(removedToken.x, removedToken.y)) {
                    token.indexOnPoint = indexOnPoint;
                    indexOnPoint++;
                }
                if (removedToken2 != null && token.compareCoord(removedToken2.x, removedToken2.y)) {
                    token.indexOnPoint = indexOnPoint2;
                    indexOnPoint2++;
                }
                token.updateCircle();
                token.updateLabel();
            }
            
            circleGroup.getChildren().removeAll(removedToken.circle, removedToken2 != null ? removedToken2.circle : null);
            labelGroup.getChildren().removeAll(removedToken.label, removedToken2 != null ? removedToken2.label : null);
        }
    }

    /**
     * Clear all tokens
     */
    public static void clear() {
        circleGroup.getChildren().clear();
        labelGroup.getChildren().clear();
        tokens.clear();
        mainGroup.getChildren().setAll(circleGroup, labelGroup);
    }

    /**
     * Get point coordinates of the tokens
     * @return points
     */
    public static LinkedList<Pair<Integer, Integer>> getPoints() {
        LinkedList<Pair<Integer, Integer>> points = new LinkedList<>();
        for (Token token: tokens)
            points.add(new Pair<>(((Double) token.x).intValue(), ((Double) token.y).intValue()));
        return points;
    }

    /**
     * Get size of tokens
     * @return size
     */
    public static int getTokensSize() { return tokens.size(); }
}
