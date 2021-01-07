package gui;

import data.Database;
import data.TableCellComponent;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import object.base.MovingObject;
import object.vehicle.aircraft.Aircraft;
import object.vehicle.aircraft.CivilAircraft;
import object.vehicle.ship.AircraftCarrier;
import util.Utility;

public class MainWindow {

    public Button databaseContentButton, startStopButton, objectButton1, objectButton2, resetMapButton;
    public Group objectsGroup;
    public Label nameLabel;
    public Pane map;
    public ScrollPane infoTabScrollPane;
    public Tab mapTab, infoTab;
    public CheckMenuItem displayLabelCheckbox, displayVehicleAtJunctionsCheckbox, displayVehicleWaitingAtJunctionsCheckbox, simulationFullStopCheckbox;
    private AnimationTimer tableRefresher;
    private String objectChosenId;
    public TableView<TableCellComponent> objectDataTable;
    public TableColumn<TableCellComponent, String> paramColumn;
    public TableColumn<TableCellComponent, String> valueColumn;
    public TabPane mainTabPane;
    public VBox objectDataVBox;
    private double mouseInitPosX = 0, mouseInitPosY = 0, scaleFactor = 1.0;

    public void databaseContentController() {
        if (Database.isInit()) {
            reset();
            databaseContentButton.setText("Init Database");
        }
        else {
            init();
            databaseContentButton.setText("Reset Database");
        }
    }

    private void init() {
        Database.init(new String[]{"./data/net.json", "./data/veh.json"}, new String[][] {
                {"junctions", "airports", "tracks"},
                {"aircrafts", "ships"}});
        objectsGroup = new Group();
        initNodes();
        map.getChildren().add(objectsGroup);
        startStopButton.setDisable(false);
        tableRefresher = new AnimationTimer() {
            @Override
            public void handle(long l) {
                objectDataTable.setItems(Database.getAppObjects().get(objectChosenId).getObjectInfo());
            }
        };
    }

    private void initNodes() {
        for (String id : Database.getIds()) {
            Database.getAppObjects().get(id).getShape().setOnMouseClicked(this::objectChosen);
            objectsGroup.getChildren().add((Database.getAppObjects().get(id)).getShape());
            if (Database.getAppObjects().get(id) instanceof MovingObject)
                objectsGroup.getChildren().add(((MovingObject) (Database.getAppObjects().get(id))).getLabel());
        }
    }

    private void reset() {
        if (Database.isInit()) {
            tableRefresher.stop();
            Database.clear();
            objectsGroup.getChildren().clear();
            startStopButton.setDisable(true);
            infoTabScrollPane.setContent(new Text(""));
            map.getTransforms().clear();
        }
        displayLabelCheckbox.setSelected(true);
        displayVehicleAtJunctionsCheckbox.setSelected(false);
        displayVehicleWaitingAtJunctionsCheckbox.setSelected(false);
        simulationFullStopCheckbox.setSelected(true);
    }

    public void startStop() { Database.startStopThreads(); }

    public void objectChosen(MouseEvent event) {
        if (Database.isInit()) {
            nameLabel.setText("Name: " + ((Node)event.getSource()).getUserData());
            objectDataVBox.setDisable(false);
            objectChosenId = ((Node)event.getSource()).getId();
            objectDataTable.setItems(Database.getAppObjects().get(objectChosenId).getObjectInfo());
            switch (Database.getAppObjects().get(((Node)event.getSource()).getId()).getType()) {
                case CA, MA -> {
                    objectButtonSet(objectButton1, ((Node)event.getSource()).getId(), "Delete",
                            true, false, this::removeObject);
                    objectButtonSet(objectButton2, ((Node)event.getSource()).getId(), "Force Emergency Stop",
                            true, false, event1 -> ((Aircraft) Database.getAppObjects().get(((Node) event.getSource()).getId())).emergencyStop());
                }
                case AC -> {
                    objectButtonSet(objectButton1, ((Node)event.getSource()).getId(), "Delete",
                            true, false, this::removeObject);
                    objectButtonSet(objectButton2, ((Node)event.getSource()).getId(), "Deploy Military Aircraft",
                            true, false, ((AircraftCarrier) Database.getAppObjects().get(((Node)event.getSource()).getId()))::deployMA);
                }
                case CS -> {
                    objectButtonSet(objectButton1, ((Node)event.getSource()).getId(), "Delete",
                            true, false, this::removeObject);
                    objectButtonSet(objectButton2, null, null, false, true, null);
                }
                default -> {
                    objectButtonSet(objectButton1, null, null, false, true, null);
                    objectButtonSet(objectButton2, null, null, false, true, null);
                }
            }
            tableRefresher.start();
        }
    }

    public void displayLabelController() {
        for (String id : Database.getAppObjects().keySet())
            if (Database.getAppObjects().get(id) instanceof MovingObject)
                ((MovingObject) Database.getAppObjects().get(id)).setLabelVisible(displayLabelCheckbox.isSelected());
    }

    public void displayVehicleAtJunctionsController() {
        for (String id : Database.getAppObjects().keySet())
            if (Database.getAppObjects().get(id) instanceof MovingObject)
                ((MovingObject) Database.getAppObjects().get(id)).setVisibleAtJunction(displayVehicleAtJunctionsCheckbox.isSelected());
    }

    public void displayVehicleWaitingAtJunctionsController() {
        for (String id : Database.getAppObjects().keySet())
            if (Database.getAppObjects().get(id) instanceof MovingObject)
                ((MovingObject) Database.getAppObjects().get(id)).setVisibleWaitingAtJunction(displayVehicleWaitingAtJunctionsCheckbox.isSelected());
    }

    public void simulationFullStopController() {
        for (String id : Database.getAircrafts())
            if (Database.getAppObjects().get(id) instanceof CivilAircraft)
                ((CivilAircraft) Database.getAppObjects().get(id)).setFullStopIntermediateAirports(simulationFullStopCheckbox.isSelected());
    }

    private void objectButtonSet(Button objButton, String id, String text, boolean isVisible, boolean isDisable,
                                 javafx.event.EventHandler<? super javafx.scene.input.MouseEvent> eventHandler) {
        objButton.setId(id);
        objButton.setText(text);
        objButton.setVisible(isVisible);
        objButton.setDisable(isDisable);
        objButton.setOnMouseClicked(eventHandler);
    }

    private void removeObject(MouseEvent event) {
        String key = ((Node) event.getSource()).getId();
        if (Database.getAppObjects().containsKey(key)) {
            objectsGroup.getChildren().remove(Database.getAppObjects().get(key).getShape());
            switch (Database.getAppObjects().get(key).getType()) {
                case CA, MA -> Database.getAircrafts().remove(key);
                case CS, AC -> Database.getShips().remove(key);
            }
            tableRefresher.stop();
            Database.endThread(key);
            Database.getAppObjects().remove(key);
            nameLabel.setText("");
            objectButtonSet(objectButton1, null, null, false, true, null);
            objectButtonSet(objectButton2, null, null, false, true, null);
            objectDataTable.setItems(FXCollections.observableArrayList());
            objectDataVBox.setDisable(false);
        }
    }

    // ---
    // Map
    // ---

    public void zoomMap(ScrollEvent event) {
        Scale scale = new Scale();
        double prevScaleFactor = scaleFactor;
        scaleFactor = Utility.Math.clamp(scaleFactor * (1 + event.getDeltaY() / 80), 1, 10).doubleValue();
        scale.setX(scaleFactor / prevScaleFactor);
        scale.setY(scaleFactor / prevScaleFactor);
        scale.setPivotX(event.getX());
        scale.setPivotY(event.getY());
        map.getTransforms().add(scale);
    }

    public void dragMap(MouseEvent event) {
        Translate translate = new Translate();
        translate.setX(map.getTranslateX() + (event.getX() - mouseInitPosX));
        translate.setY(map.getTranslateY() + (event.getY() - mouseInitPosY));
        map.getTransforms().add(translate);
    }

    public void resetMap() {
        map.getTransforms().clear();
        scaleFactor = 1.0;
    }

    public void startDragMap(MouseEvent dragEvent) {
        mouseInitPosX = dragEvent.getX();
        mouseInitPosY = dragEvent.getY();
    }
}

