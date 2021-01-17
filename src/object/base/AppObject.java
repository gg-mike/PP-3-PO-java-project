package object.base;
import data.Database;
import component.GUIComponent;
import component.TableCellComponent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.shape.Shape;
import util.Utility;

public abstract class AppObject {
    protected GUIComponent guiComponent;

    public AppObject(String data) {
        Utility.JSONInfo.init(data);
        Utility.StringInfo.init(Utility.JSONInfo.get("id").toString());
        if (!Utility.StringInfo.getId().startsWith("TR"))
            guiComponent = new GUIComponent(Utility.StringInfo.getId(), Utility.StringInfo.getX(), Utility.StringInfo.getY());
    }

    public Shape getShape() { return guiComponent.getShape(); }

    public double getGUI_X() { return guiComponent.getCoords()[0]; }

    public double getGUI_Y() { return guiComponent.getCoords()[1]; }

    public String getId() {
        return guiComponent.getId();
    }

    public Database.ObjectType getType() {
        return Database.ObjectType.valueOf(getId().substring(0, 2));
    }

    public abstract void update();

    @Override
    public String toString() {
        return  String.format("Id: %s\n", getId()) +
                String.format("  coord: (%.1f | %.1f)\n", getGUI_X(), getGUI_Y());
    }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = FXCollections.observableArrayList();
        objectInfos.add(new TableCellComponent("Identification", getId()));
        objectInfos.add(new TableCellComponent("Coordinates", String.format("(%.1f | %.1f)", getGUI_X(), getGUI_Y())));
        return objectInfos;
    }
}
