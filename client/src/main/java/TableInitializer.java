import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;

public class TableInitializer {

    public static void initialize(TableView<FileManager> table, String name) {
        TableColumn<FileManager, String> columnName = new TableColumn<>(name);
        columnName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        columnName.setPrefWidth(200);
        TableColumn<FileManager, String> columnType = new TableColumn<>("Type");
        columnType.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getType()));
        columnType.setPrefWidth(60);
        TableColumn<FileManager, Long> columnSize = new TableColumn<>("Size");
        columnSize.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        columnSize.setPrefWidth(140);
        columnSize.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d bytes", item);
                    if (item == -1L) {
                        text = "[DIR]";
                    }
                    setText(text);
                }
            }
        });
        table.getSortOrder().add(columnType);
        table.getColumns().addAll(columnName, columnType, columnSize);
    }

}
