package component;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import java.util.ArrayList;

/**
 * Movable GUI object (used for vehicles)
 */
public class GUIMovableComponent extends GUIComponent {
    private boolean isShapeVisibleAtJunctions = false;
    private boolean isShapeVisibleWaitingAtJunctions = false;
    private final Label label;
    private Boolean isLabelVisible = true;

    /**
     * Constructor
     * @param prevGUIComponent previous GUIComponent
     */
    public GUIMovableComponent(GUIComponent prevGUIComponent) {
        super(prevGUIComponent.getId(), prevGUIComponent.getCoords());
        label = new Label(getId());
        label.setLayoutX(getCoords()[0] + 10);
        label.setLayoutY(getCoords()[1] - 15);
        label.setStyle("-fx-font-weight: bold;");
        label.setVisible(isLabelVisible);
    }

    /**
     * Update stroke color
     * @param stroke stroke color (null - reset to default)
     * @param positionData positionData from MovementComponent (x, y, angle)
     * @param moving state == MOVE
     * @param waiting state == WAITING_*
     */
    public void update(Color stroke, ArrayList<Double> positionData, boolean moving, boolean waiting) {
        super.update(stroke);
        shape.setLayoutX(positionData.get(0));
        shape.setLayoutY(positionData.get(1));
        shape.setRotate(positionData.get(2));
        shape.setVisible((isShapeVisibleAtJunctions && !waiting) || moving || (isShapeVisibleWaitingAtJunctions && waiting));
        label.setLayoutX(positionData.get(0) + 10);
        label.setLayoutY(positionData.get(1) - 15);
        label.setVisible(isLabelVisible && shape.isVisible());
    }

    /**
     * @param shapeVisibleAtJunctions set visibility of the shape at junctions (and airports)
     */
    public void setShapeVisibleAtJunctions(boolean shapeVisibleAtJunctions) {
        isShapeVisibleAtJunctions = shapeVisibleAtJunctions;
    }

    /**
     * @param shapeVisibleWaitingAtJunctions set visibility of the shape waiting at junctions (and airports)
     */
    public void setShapeVisibleWaitingAtJunctions(boolean shapeVisibleWaitingAtJunctions) {
        isShapeVisibleWaitingAtJunctions = shapeVisibleWaitingAtJunctions;
    }

    /**
     * @return label of the object
     */
    public Label getLabel() { return label; }

    /**
     * @param visibleLabel set visibility of the label
     */
    public void setVisibleLabel(boolean visibleLabel) { isLabelVisible = visibleLabel; }
}
