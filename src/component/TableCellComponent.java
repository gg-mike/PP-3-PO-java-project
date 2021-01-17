package component;

/**
 * Parameter - Value table cell
 */
public class TableCellComponent {
    String param, value;

    /**
     * Constructor
     * @param param parameter name
     * @param value parameter value
     */
    public TableCellComponent(String param, String value) {
        this.param = param;
        this.value = value;
    }

    public String getParam() { return param; }

    public void setParam(String param) { this.param = param; }

    public String getValue() { return value; }

    public void setValue(String value) { this.value = value; }
}
