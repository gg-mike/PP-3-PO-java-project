package obj.base;
import data.Database;
import data.TableCellComponent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import util.Utility;

public abstract class AppObject {
    protected volatile Shape shape;
    protected Database.ObjectType objectType;

    public AppObject(String data) {
        Utility.JSONInfo.init(data);
        Utility.StringInfo.init(Utility.JSONInfo.get("id").toString());
        shape = new Circle();
        shape.setId(Utility.StringInfo.getId());
        shape.setUserData(Utility.StringInfo.getId());
        ((Circle) shape).setCenterX(Utility.StringInfo.getX());
        ((Circle) shape).setCenterY(Utility.StringInfo.getY());
        objectType = Database.ObjectType.valueOf(Utility.StringInfo.getObjectType());
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public double getX() { return ((Circle) this.shape).getCenterX(); }

    public double getY() { return ((Circle) this.shape).getCenterY(); }

    public void setX(double x) { ((Circle) this.shape).setCenterX(x); }

    public void setY(double y) { ((Circle) this.shape).setCenterY(y); }

    public String getId() {
        return shape.getId();
    }

    public Database.ObjectType getType() {
        return objectType;
    }

    public abstract void update();

    @Override
    public String toString() {
        return  String.format("Id: %s\n", getId()) +
                String.format("  coord: (%.1f | %.1f)\n", getX(), getY());
    }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = FXCollections.observableArrayList();
        objectInfos.add(new TableCellComponent("ID", getId()));
        objectInfos.add(new TableCellComponent("coord", String.format("(%.1f | %.1f)", getX(), getY())));
        return objectInfos;
    }
}
