package data;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * Movable GUI object (used for airplanes and ships)
 */
public class GUIMovableComponent extends GUIComponent {
    private final MovementComponent movementComponent;
    private boolean isVisibleShape = false;
    protected Label label;
    protected Boolean isVisibleLabel = true;

    // INIT

    public GUIMovableComponent(GUIComponent prevGUIComponent, double speed, double offset) {
        super(prevGUIComponent.getId(), prevGUIComponent.getCoords());
        movementComponent = new MovementComponent(speed, getCoords()[0], getCoords()[1], offset);
        label = new Label(getId());
        label.setLayoutX(getCoords()[0] + 10);
        label.setLayoutY(getCoords()[1] - 15);
        label.setStyle("-fx-font-weight: bold;");
        label.setVisible(isVisibleLabel);
    }

    public void init(double destX, double destY, double nextDestX, double nextDestY) {
        movementComponent.setDest(destX, destY, nextDestX, nextDestY);
    }

    // FUNCTIONALITY

    public void update(Color stroke, boolean moving) {
        super.update(stroke);
        shape.setLayoutX(movementComponent.getX());
        shape.setLayoutY(movementComponent.getY());
        rotate();
        shape.setVisible(isVisibleShape || moving);
        label.setLayoutX(movementComponent.getX() + 10);
        label.setLayoutY(movementComponent.getY() - 15);
        label.setVisible(isVisibleLabel && (moving || isVisibleShape));
    }

    /**
     * Calculate the rotation angle and rotate the shape by it
     */
    private void rotate() {
        switch (movementComponent.getHeading()) {
            case N -> shape.setRotate(0);
            case E -> shape.setRotate(90);
            case S -> shape.setRotate(180);
            case W -> shape.setRotate(270);
        }
    }

    // GETTERS / SETTERS

    public MovementComponent getMovementComponent() {
        return movementComponent;
    }

    public void setVisibleShape(boolean visibleShape) { isVisibleShape = visibleShape; }

    public Label getLabel() { return label; }

    public void setVisibleLabel(boolean visibleLabel) { isVisibleLabel = visibleLabel; }
}
