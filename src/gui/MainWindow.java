package gui;

import data.Database;
import component.GUIComponent;
import component.TableCellComponent;
import data.ObjectGenerator;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import object.base.MovingObject;
import object.network.NetworkObject;
import object.vehicle.aircraft.Aircraft;
import object.vehicle.aircraft.CivilAircraft;
import object.vehicle.ship.AircraftCarrier;
import org.json.JSONObject;
import util.Utility;

public class MainWindow {

    // Toolbar Objects
    public Button toolbar_databaseContentButton, toolbar_switchRunningButton, toolbar_resetButton;
    public CheckMenuItem display_labelCheckbox, display_vehicleAtJunctionsCheckbox, display_vehicleWaitingAtJunctionsCheckbox, simulation_fullStopCheckbox;
    public MenuButton toolbar_simulationMenuButton, toolbar_displayMenuButton;
    public Slider toolbar_simulationSpeedSlider;

    // "Map" Tab Objects
    private ContextMenu map_tokenContextMenu;
    public Group map_group;
    public Rectangle map_rect;
    private Double mouseInitPosX = 0d, mouseInitPosY = 0d, scaleFactor = 1.0;
    private Group objectsGroup;
    private String objectChosenId;

    // "Help" Tab Objects

    // "Info" Tab Objects
    public Button info_option1Button, info_option2Button;
    public Label info_nameLabel;
    public Pane info_Pane;
    public Tab info_Tab;
    public TableView<TableCellComponent> info_TableView;
    public TableColumn<TableCellComponent, String> info_paramTableColumn;
    public TableColumn<TableCellComponent, String> info_valueTableColumn;
    private AnimationTimer tableRefresher;

    // "Add" Tab Objects
    public Button add_pickRouteButton, add_discardButton, add_saveButton, add_generateButton, add_cancelButton, add_addButton;
    public ComboBox<String> add_vehicleTypeComboBox, add_routeTypeComboBox;
    public Label add_property1Label, add_property2Label, add_property3Label, add_idLabel, add_jsonLabel;
    public Spinner<Integer> add_speedSpinner, add_fuelSpinner, add_stuffNSpinner, add_passNCASpinner, add_passNCSSpinner;
    public Tab add_Tab;
    public TextField add_weaponTypeMATextField, add_companyTextField, add_weaponTypeACTextField;
    private AnimationTimer routeSizeCondition;
    private boolean isMapPick = false, isDeployMA = false;
    private String acId;

    // Other Objects
    public TabPane rightTabPane;

    // Toolbar Functionality

    /**
     * Init toolbar section
     */
    private void toolbar_init() {
        toolbar_switchRunningButton.setDisable(false);
        toolbar_displayMenuButton.setDisable(false);
        toolbar_simulationMenuButton.setDisable(false);
        display_labelCheckbox.setSelected(true);
        display_vehicleAtJunctionsCheckbox.setSelected(false);
        display_vehicleWaitingAtJunctionsCheckbox.setSelected(false);
        simulation_fullStopCheckbox.setSelected(true);
        toolbar_simulationSpeedSlider.setDisable(false);
        toolbar_simulationSpeedSlider.valueProperty().addListener((o, oldValue, newValue) -> Database.changeSimulationSpeed(newValue.doubleValue()));
    }

    /**
     * Reset toolbar section
     */
    private void toolbar_reset() {
        toolbar_switchRunningButton.setDisable(true);
        toolbar_displayMenuButton.setDisable(true);
        toolbar_simulationMenuButton.setDisable(true);
        toolbar_simulationSpeedSlider.setDisable(true);
        toolbar_simulationSpeedSlider.setValue(1d);
    }

    /**
     * Init/Reset database
     */
    public void toolbar_databaseContentController() {
        if (Database.isInit()) {
            resetDatabase();
            toolbar_databaseContentButton.setText("Init Database");
        }
        else {
            initDatabase();
            toolbar_databaseContentButton.setText("Reset Database");
        }
    }

    /**
     * Init database with json files
     */
    private void initDatabase() {
        Database.init(new String[]{"./data/net.json", "./data/veh.json"}, new String[][] {
                {"junctions", "airports", "tracks"},
                {"aircrafts", "ships"}});
        toolbar_init();
        map_init();
        add_init();
        tableRefresher = new AnimationTimer() {
            @Override
            public void handle(long l) {
                info_TableView.setItems(Database.getAppObjects().get(objectChosenId).getObjectInfo());
            }
        };
    }

    /**
     * Init all application nodes
     */
    private void initNodes() {
        initNetNodes();
        initVehNodes();
    }

    /**
     * Init network nodes
     */
    private void initNetNodes() {
        for (String id: Database.getTracks()) {
            Database.getAppObjects().get(id).getShape().setOnMouseClicked(this::map_objectChosenController);
            objectsGroup.getChildren().add((Database.getAppObjects().get(id)).getShape());
        }
        for (String id: Database.getJunctions()) {
            Database.getAppObjects().get(id).getShape().setOnMouseClicked(this::map_objectChosenController);
            objectsGroup.getChildren().add((Database.getAppObjects().get(id)).getShape());
        }
        for (String id: Database.getAirports()) {
            Database.getAppObjects().get(id).getShape().setOnMouseClicked(this::map_objectChosenController);
            objectsGroup.getChildren().add((Database.getAppObjects().get(id)).getShape());
        }
    }

    /**
     * Init vehicle nodes
     */
    private void initVehNodes() {
        for (String id: Database.getShips()) {
            Database.getAppObjects().get(id).getShape().setOnMouseClicked(this::map_objectChosenController);
            objectsGroup.getChildren().add((Database.getAppObjects().get(id)).getShape());
            objectsGroup.getChildren().add(((MovingObject) (Database.getAppObjects().get(id))).getLabel());
        }
        for (String id: Database.getAircrafts()) {
            Database.getAppObjects().get(id).getShape().setOnMouseClicked(this::map_objectChosenController);
            objectsGroup.getChildren().add((Database.getAppObjects().get(id)).getShape());
            objectsGroup.getChildren().add(((MovingObject) (Database.getAppObjects().get(id))).getLabel());
        }
    }

    /**
     * Clear the content of the database
     */
    private void resetDatabase() {
        if (Database.isInit()) {
            Database.clear();
            toolbar_reset();
            map_reset();
            info_reset();
            add_reset();
        }
    }

    /**
     * Switch running of the threads (Database.switchRunningThreads())
     */
    public void toolbar_switchRunningController() { Database.switchRunningThreads(); }

    /**
     * Reset map to its original position and reset if necessary
     */
    public void toolbar_resetMapController() {
        map_group.getTransforms().clear();
        scaleFactor = 1.0;
        double widthScale = ((Pane) map_group.getParent()).getWidth() / map_rect.getWidth();
        double heightScale = ((Pane) map_group.getParent()).getHeight() / map_rect.getHeight();
        map_group.setScaleX(Math.min(widthScale, heightScale));
        map_group.setScaleY(Math.min(widthScale, heightScale));
        map_group.setLayoutX((((Pane) map_group.getParent()).getWidth() - map_rect.getWidth()) / 2);
        map_group.setLayoutY((((Pane) map_group.getParent()).getHeight() - map_rect.getHeight()) / 2);
    }

    /**
     * Set MovingObject label visibility
     */
    public void display_labelController() {
        for (String id : Database.getAppObjects().keySet())
            if (Database.getAppObjects().get(id) instanceof MovingObject)
                ((MovingObject) Database.getAppObjects().get(id)).setLabelVisible(display_labelCheckbox.isSelected());
    }

    /**
     * Set MovingObject visibility at junctions
     */
    public void display_vehicleAtJunctionsController() {
        for (String id : Database.getAppObjects().keySet())
            if (Database.getAppObjects().get(id) instanceof MovingObject)
                ((MovingObject) Database.getAppObjects().get(id)).setVisibleAtJunction(display_vehicleAtJunctionsCheckbox.isSelected());
    }

    /**
     * Set MovingObject visibility waiting at junctions
     */
    public void display_vehicleWaitingAtJunctionsController() {
        for (String id : Database.getAppObjects().keySet())
            if (Database.getAppObjects().get(id) instanceof MovingObject)
                ((MovingObject) Database.getAppObjects().get(id)).setVisibleWaitingAtJunction(display_vehicleWaitingAtJunctionsCheckbox.isSelected());
    }

    /**
     * Set CA full stop on intermediate airports
     */
    public void simulation_fullStopController() {
        for (String id : Database.getAircrafts())
            if (Database.getAppObjects().get(id) instanceof CivilAircraft)
                ((CivilAircraft) Database.getAppObjects().get(id)).setFullStopIntermediateAirports(simulation_fullStopCheckbox.isSelected());
    }

    // "Map" Tab Functionality

    /**
     * Init map section
     */
    private void map_init() {
        objectsGroup = new Group();
        initNodes();
        map_group.getChildren().add(objectsGroup);
        map_tokenContextMenu = new ContextMenu();
        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(this::add_deleteController);
        map_tokenContextMenu.getItems().add(delete);
    }

    /**
     * Reset map section
     */
    private void map_reset() {
        objectsGroup.getChildren().clear();
        map_group.getTransforms().clear();
    }

    /**
     * Rescale map with a given pivot
     * @param event ScrollEvent
     */
    public void map_zoomController(ScrollEvent event) {
        Scale scale = new Scale();
        double prevScaleFactor = scaleFactor;
        scaleFactor = Utility.Math.clamp(scaleFactor * (1 + event.getDeltaY() / 80), 1, 10).doubleValue();
        scale.setX(scaleFactor / prevScaleFactor);
        scale.setY(scaleFactor / prevScaleFactor);
        scale.setPivotX(event.getX());
        scale.setPivotY(event.getY());
        map_group.getTransforms().add(scale);
    }

    /**
     * Drag map
     * @param event MouseEvent
     */
    public void map_dragController(MouseEvent event) {
        Translate translate = new Translate();
        translate.setX(map_group.getTranslateX() + (event.getX() - mouseInitPosX));
        translate.setY(map_group.getTranslateY() + (event.getY() - mouseInitPosY));
        map_group.getTransforms().add(translate);
    }

    /**
     * Set starting coordinates for map dragging
     * @param dragEvent MouseEvent
     */
    public void map_startDragController(MouseEvent dragEvent) {
        mouseInitPosX = dragEvent.getX();
        mouseInitPosY = dragEvent.getY();
    }

    /**
     * If isMapPick == true then try to add the token else init info section
     * @param event MouseEvent
     */
    public void map_objectChosenController(MouseEvent event) {
        if (isMapPick) {
            NetworkObject networkObject = (NetworkObject) Database.getAppObjects().get(((Node)event.getSource()).getId());
            if (networkObject != null) {
                boolean isAdd = false;
                switch (add_vehicleTypeComboBox.getSelectionModel().getSelectedIndex()) {
                    case 0 -> isAdd = networkObject.getId().startsWith("APC") || networkObject.getId().startsWith("JUA");
                    case 1 -> isAdd = networkObject.getId().startsWith("AP") || networkObject.getId().startsWith("JUA");
                    case 2, 3 -> isAdd = networkObject.getId().startsWith("JUW");
                }
                if (isAdd) {
                    Label label = MapPicker.addToken(networkObject.getGUI_X(), networkObject.getGUI_Y());
                    if (label != null)
                        label.setOnContextMenuRequested(contextMenuEvent -> map_tokenContextMenu.show(label, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY()));
                }
            }
        }
        else info_init(((Node)event.getSource()).getId(), (String) ((Node)event.getSource()).getUserData());
    }

    // "Info" Tab Functionality

    /**
     * Init info section
     * @param id object's id
     * @param userData userData of the event
     */
    private void info_init(String id, String userData) {
        if (Database.isInit()) {
            info_Tab.setDisable(false);
            info_nameLabel.setText(userData);
            objectChosenId = id;
            info_TableView.setItems(Database.getAppObjects().get(objectChosenId).getObjectInfo());
            switch (Database.getAppObjects().get(id).getType()) {
                case CA, MA -> {
                    info_optionButtonSet(info_option1Button, id, "Delete",
                            true, false, this::removeVehicleEventController);
                    info_optionButtonSet(info_option2Button, id, "Force Emergency Stop",
                            true, false, event1 -> ((Aircraft) Database.getAppObjects().get(id)).emergencyStop());
                }
                case AC -> {
                    info_optionButtonSet(info_option1Button, id, "Delete",
                            true, false, this::removeVehicleEventController);
                    info_optionButtonSet(info_option2Button, id, "Deploy Military Aircraft",
                            true, false, this::add_initDeployMA);
                }
                case CS -> {
                    info_optionButtonSet(info_option1Button, id, "Delete",
                            true, false, this::removeVehicleEventController);
                    info_optionButtonSet(info_option2Button, null, null, false, true, null);
                }
                default -> {
                    info_optionButtonSet(info_option1Button, null, null, false, true, null);
                    info_optionButtonSet(info_option2Button, null, null, false, true, null);
                }
            }
            info_PaneSet(id);
            tableRefresher.start();
        }
    }

    /**
     * Reset info section
     */
    private void info_reset() {
        tableRefresher.stop();
        info_nameLabel.setText("");
        info_Pane.getChildren().clear();
        info_optionButtonSet(info_option1Button, null, null, false, true, null);
        info_optionButtonSet(info_option2Button, null, null, false, true, null);
        info_TableView.setItems(FXCollections.observableArrayList());
        info_Tab.setDisable(true);
    }

    /**
     * Init object's shape in the info section
     * @param id object's id
     */
    private void info_PaneSet(String id) {
        if (id.startsWith("TR"))
            info_Pane.getChildren().setAll(GUIComponent.getShapeBase(id,
                    info_Pane.getWidth() / 4, info_Pane.getHeight() * 3 / 4,
                    info_Pane.getWidth() * 3 / 4, info_Pane.getHeight() / 4));
        else {
            Shape shape = GUIComponent.getShapeBase(id, info_Pane.getWidth() / 2, info_Pane.getHeight() / 2);
            if (shape instanceof Polygon) {
                shape.setScaleX(4);
                shape.setScaleY(4);
                shape.setRotate(45);
            }
            info_Pane.getChildren().setAll(shape);
        }
    }

    /**
     * Setup for info section button
     * @param button button reference
     * @param id button id
     * @param text button text
     * @param isVisible visibility of the button
     * @param isDisable whether the button is disabled
     * @param eventHandler functionality of the button
     */
    private void info_optionButtonSet(Button button, String id, String text, boolean isVisible, boolean isDisable,
                                      javafx.event.EventHandler<? super javafx.scene.input.MouseEvent> eventHandler) {
        button.setId(id);
        button.setText(text);
        button.setVisible(isVisible);
        button.setDisable(isDisable);
        button.setOnMouseClicked(eventHandler);
    }

    // "Add"  Tab Functionality

    /**
     * Init add section
     */
    private void add_init() {
        add_vehicleTypeComboBox.getItems().setAll("Civil Aircraft", "Military Aircraft", "Cruise Ship", "Aircraft Carrier");
        add_vehicleTypeComboBox.getSelectionModel().selectFirst();
        add_routeTypeComboBox.getItems().setAll("Once", "Circles", "There and back");
        add_routeTypeComboBox.getSelectionModel().selectFirst();
        routeSizeCondition = new AnimationTimer() {
            @Override
            public void handle(long l) {
                add_saveButton.setDisable(MapPicker.getTokensSize() <= 1);
            }
        };
        add_Tab.setDisable(false);
    }

    /**
     * Init add section for MA deployment
     * @param event MouseEvent
     */
    private void add_initDeployMA(MouseEvent event) {
        acId = ((Node) event.getSource()).getId();
        add_resetDeployMA(((AircraftCarrier) Database.getAppObjects().get(acId)).getWeaponType());
    }

    /**
     * Reset add section
     */
    private void add_reset() {
        add_resetContent();
        add_interfaceAC();
        add_pickRouteButton.setVisible(true);
        add_discardButton.setVisible(false);
        add_saveButton.setVisible(false);
        add_Tab.setDisable(true);
    }

    /**
     * Reset add section for MA deployment
     * @param weaponType weapon type of the AC
     */
    private void add_resetDeployMA(String weaponType) {
        isDeployMA = true;
        rightTabPane.getSelectionModel().select(add_Tab);
        add_vehicleTypeComboBox.getSelectionModel().select(1);
        add_vehicleTypeComboBox.setDisable(true);
        add_resetContent();
        add_interfaceMA();
        add_weaponTypeMATextField.setText(weaponType);
        add_weaponTypeMATextField.setDisable(true);
    }

    /**
     * Choose the add section interface setup
     */
    public void add_vehicleTypeChangeController() {
        add_resetContent();
        switch (add_vehicleTypeComboBox.getSelectionModel().getSelectedIndex()) {
            case 0 -> add_interfaceCA();
            case 1 -> add_interfaceMA();
            case 2 -> add_interfaceCS();
            case 3 -> add_interfaceAC();
        }
    }

    /**
     * Map picking start
     */
    public void add_pickRouteController() {
        isMapPick = true;
        routeSizeCondition.start();
        Database.stopThreads();
        objectsGroup.getChildren().clear();
        initNetNodes();
        info_reset();
        objectsGroup.getChildren().add(MapPicker.mainGroup);

        toolbar_databaseContentButton.setDisable(true);
        toolbar_switchRunningButton.setDisable(true);

        add_vehicleTypeComboBox.setDisable(true);
        add_pickRouteButton.setVisible(false);
        add_discardButton.setVisible(true);
        add_saveButton.setVisible(true);
    }

    /**
     * Discard route in MapPicker
     */
    public void add_discardController() {
        MapPicker.clear();
        add_generateButton.setDisable(true);
        if (!isDeployMA)
            add_vehicleTypeComboBox.setDisable(false);
        add_endPicking();
    }

    /**
     * Save route from MapPicker
     */
    public void add_saveController() {
        add_endPicking();
        add_generateButton.setDisable(false);
    }

    /**
     * End map picking
     */
    private void add_endPicking() {
        routeSizeCondition.stop();
        objectsGroup.getChildren().clear();
        initNetNodes();
        initVehNodes();
        isMapPick = false;

        add_discardButton.setVisible(false);
        add_saveButton.setVisible(false);
        add_pickRouteButton.setVisible(true);

        toolbar_databaseContentButton.setDisable(false);
        toolbar_switchRunningButton.setDisable(false);
    }

    /**
     * Remove token from route
     * @param actionEvent ActionEvent
     */
    public void add_deleteController(ActionEvent actionEvent) {
        MapPicker.remove(Integer.parseInt(((MenuItem) actionEvent.getSource()).getParentPopup().getOwnerNode().getId()));
    }

    /**
     * Reset add section
     */
    public void add_resetController() {
        MapPicker.clear();
        add_generateButton.setDisable(true);
        if (!isDeployMA) {
            add_vehicleTypeComboBox.setDisable(false);
            add_vehicleTypeChangeController();
        }
        else
            add_resetDeployMA(add_weaponTypeMATextField.getText());
    }

    /**
     * Generate new vehicle's json
     */
    public void add_generateController() {
        switch (add_vehicleTypeComboBox.getSelectionModel().getSelectedIndex()) {
            case 0 -> add_jsonLabel.setText(ObjectGenerator.generateJSON(0, add_speedSpinner.getValue(), MapPicker.getPoints(),
                    add_routeTypeComboBox.getSelectionModel().getSelectedIndex(),
                    add_stuffNSpinner.getValue(), add_passNCASpinner.getValue(), add_fuelSpinner.getValue().doubleValue(), null, null));

            case 1 -> add_jsonLabel.setText(ObjectGenerator.generateJSON(1, add_speedSpinner.getValue(), MapPicker.getPoints(),
                    add_routeTypeComboBox.getSelectionModel().getSelectedIndex(),
                    add_stuffNSpinner.getValue(), add_passNCASpinner.getValue(), add_fuelSpinner.getValue().doubleValue(), add_weaponTypeMATextField.getText(), null));

            case 2 -> add_jsonLabel.setText(ObjectGenerator.generateJSON(2, add_speedSpinner.getValue(), MapPicker.getPoints(),
                    add_routeTypeComboBox.getSelectionModel().getSelectedIndex(),
                    null, add_passNCASpinner.getValue(), null, null, add_companyTextField.getText()));

            case 3 -> add_jsonLabel.setText(ObjectGenerator.generateJSON(3, add_speedSpinner.getValue(), MapPicker.getPoints(),
                    add_routeTypeComboBox.getSelectionModel().getSelectedIndex(),
                    null, null, null, add_weaponTypeACTextField.getText(), null));
        }

        add_idLabel.setText(add_jsonLabel.getText().substring(9, 16));
        add_addButton.setDisable(false);
    }

    /**
     * Cancel progress in adding new vehicle
     */
    public void add_cancelController() {
        acId = null;
        isDeployMA = false;
        add_resetContent();
    }

    /**
     * Add new vehicle to the database
     */
    public void add_addController() {
        JSONObject jsonObject = new JSONObject(add_jsonLabel.getText());
        if (isDeployMA)
            Database.initDeployMA(jsonObject, Database.getAppObjects().get(acId).getGUI_X(), Database.getAppObjects().get(acId).getGUI_Y());
        else
            Database.initAppObject(jsonObject, false);
        Database.getAppObjects().get(jsonObject.getString("id")).getShape().setOnMouseClicked(this::map_objectChosenController);
        objectsGroup.getChildren().add((Database.getAppObjects().get(jsonObject.getString("id"))).getShape());
        objectsGroup.getChildren().add(((MovingObject) (Database.getAppObjects().get(jsonObject.getString("id")))).getLabel());
        info_init(jsonObject.getString("id"), jsonObject.getString("id"));
        add_resetController();
        add_addButton.setDisable(true);
        add_generateButton.setDisable(true);
        add_saveButton.setDisable(true);
    }

    /**
     * Setup add section for CA
     */
    private void add_interfaceCA() {
        add_property1Label.setVisible(true);
        add_property2Label.setVisible(true);
        add_property3Label.setVisible(true);
        add_property1Label.setText("Max fuel");
        add_property2Label.setText("Stuff number");
        add_property3Label.setText("Max passenger number");
        add_fuelSpinner.setVisible(true);
        add_stuffNSpinner.setVisible(true);
        add_passNCASpinner.setVisible(true);
    }

    /**
     * Setup add section for MA
     */
    private void add_interfaceMA() {
        add_property1Label.setVisible(true);
        add_property2Label.setVisible(true);
        add_property3Label.setVisible(true);
        add_property1Label.setText("Max fuel");
        add_property2Label.setText("Stuff number");
        add_property3Label.setText("Weapon type");
        add_fuelSpinner.setVisible(true);
        add_stuffNSpinner.setVisible(true);
        add_weaponTypeMATextField.setVisible(true);
    }

    /**
     * Setup add section for CS
     */
    private void add_interfaceCS() {
        add_property1Label.setVisible(true);
        add_property2Label.setVisible(true);
        add_property1Label.setText("Max passenger number");
        add_property2Label.setText("Company name");
        add_passNCSSpinner.setVisible(true);
        add_companyTextField.setVisible(true);
    }

    /**
     * Setup add section for AC
     */
    private void add_interfaceAC() {
        add_property1Label.setVisible(true);
        add_property1Label.setText("Weapon type");
        add_weaponTypeACTextField.setVisible(true);
    }

    /**
     * Reset content of add section
     */
    private void add_resetContent() {
        add_property1Label.setVisible(false);
        add_property2Label.setVisible(false);
        add_property3Label.setVisible(false);

        add_speedSpinner.getValueFactory().setValue(50);

        add_fuelSpinner.setVisible(false);
        add_fuelSpinner.getValueFactory().setValue(300);

        add_stuffNSpinner.setVisible(false);
        add_stuffNSpinner.getValueFactory().setValue(5);

        add_passNCASpinner.setVisible(false);
        add_passNCASpinner.getValueFactory().setValue(100);

        add_weaponTypeMATextField.setVisible(false);
        add_weaponTypeMATextField.setText("");

        add_passNCSSpinner.setVisible(false);
        add_passNCSSpinner.getValueFactory().setValue(200);

        add_companyTextField.setVisible(false);
        add_companyTextField.setText("");

        add_weaponTypeACTextField.setVisible(false);
        add_weaponTypeACTextField.setText("");

        add_idLabel.setText("__-____");
        add_jsonLabel.setText("none");
    }

    // Others functionality

    /**
     * Remove picked vehicle
     * @param event MouseEvent
     */
    private void removeVehicleEventController(MouseEvent event) {
        String key = ((Node) event.getSource()).getId();
        if (Database.getAppObjects().containsKey(key)) {
            objectsGroup.getChildren().remove(Database.getAppObjects().get(key).getShape());
            objectsGroup.getChildren().remove(((MovingObject) Database.getAppObjects().get(key)).getLabel());
            switch (Database.getAppObjects().get(key).getType()) {
                case CA, MA -> Database.getAircrafts().remove(key);
                case CS, AC -> Database.getShips().remove(key);
            }
            Database.endThread(key);
            Database.getAppObjects().remove(key);
            info_reset();
        }
    }}
