package component;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * Movable GUI object (used for airplanes and ships)
 */
public class GUIMovableComponent extends GUIComponent {
    private final MovementComponent movementComponent;
    private boolean isShapeVisibleAtJunctions = false;
    private boolean isShapeVisibleWaitingAtJunctions = false;
    protected Label label;
    protected Boolean isLabelVisible = true;

    // INIT

    /**
     * Constructor
     * @param prevGUIComponent previous GUIComponent
     * @param speed vehicle speed
     * @param offset allowable offset to the destination
     */
    public GUIMovableComponent(GUIComponent prevGUIComponent, double speed, double offset) {
        super(prevGUIComponent.getId(), prevGUIComponent.getCoords());
        movementComponent = new MovementComponent(speed, getCoords()[0], getCoords()[1], offset);
        label = new Label(getId());
        label.setLayoutX(getCoords()[0] + 10);
        label.setLayoutY(getCoords()[1] - 15);
        label.setStyle("-fx-font-weight: bold;");
        label.setVisible(isLabelVisible);
    }


    /**
     * Init used after the intermediate route was created
     * @param destX starting destination x pos
     * @param destY starting destination y pos
     * @param nextDestX next destination x pos
     * @param nextDestY next destination y pos
     */
    public void init(double destX, double destY, double nextDestX, double nextDestY) {
        movementComponent.setDest(destX, destY, nextDestX, nextDestY);
    }

    // FUNCTIONALITY

    /**
     * Update stroke color
     * @param stroke stroke color (null - reset to default)
     * @param moving state == MOVE
     * @param waiting state == WAITING_*
     */
    public void update(Color stroke, boolean moving, boolean waiting) {
        super.update(stroke);
        shape.setLayoutX(movementComponent.getX());
        shape.setLayoutY(movementComponent.getY());
        rotate();
        shape.setVisible((isShapeVisibleAtJunctions && !waiting) || moving || (isShapeVisibleWaitingAtJunctions && waiting));
        label.setLayoutX(movementComponent.getX() + 10);
        label.setLayoutY(movementComponent.getY() - 15);
        label.setVisible(isLabelVisible && shape.isVisible());
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

    public void setShapeVisibleAtJunctions(boolean shapeVisibleAtJunctions) {
        isShapeVisibleAtJunctions = shapeVisibleAtJunctions;
    }

    public void setShapeVisibleWaitingAtJunctions(boolean shapeVisibleWaitingAtJunctions) {
        isShapeVisibleWaitingAtJunctions = shapeVisibleWaitingAtJunctions;
    }

    public Label getLabel() { return label; }

    public void setVisibleLabel(boolean visibleLabel) { isLabelVisible = visibleLabel; }
}
