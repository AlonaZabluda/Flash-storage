import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainController implements Initializable {

    public VBox auth;
    public TextField login;
    public PasswordField password;
    public Button signUp;
    public VBox storage;
    public TableView<FileManager> clientTable;
    public TextField pathFieldLeft;
    public TableView<FileManager> storageTable;
    public TextField pathFieldRight;

    private static final Logger log = LogManager.getLogger(MainController.class);
    private Path path = Paths.get("client", "ClientStorage");
    private final String PATH = path.toString();
    private ContextMenu contextMenu = new ContextMenu();
    private String currentUserNick;
    private boolean isAuthorized;


    private void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
        if (!isAuthorized) {
            auth.setVisible(true);
            auth.setManaged(true);
            storage.setVisible(false);
            storage.setManaged(false);
        } else {
            auth.setVisible(false);
            auth.setManaged(false);
            storage.setVisible(true);
            storage.setManaged(true);
        }
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        Network.getInstance();
        setAuthorized(false);
        TableInitializer.initialize(clientTable, "Client files");
        TableInitializer.initialize(storageTable, "Storage files");
        updateClientDirectory();
        requestToUpdateStorageDirectory();
        updateClientFilesList(Paths.get(PATH));
        initializeDeleteFile();
        initializeRenameFile();
        new Thread(() -> {
            Network.getInstance().getClientHandler().setCallback(o -> {
                if(o instanceof ListMessage){
                    Platform.runLater(() -> {
                        ListMessage lm = (ListMessage) o;
                        storageTable.getItems().clear();
                        List<FileManager> list = lm.getFilesList();
                        list.forEach(f -> storageTable.getItems().add(f));
                        list.forEach(s -> pathFieldRight.setText(s.getPath()));
                        log.info("Storage files list updated.");
                    });
                }
                if (o instanceof FileMessage) {
                    try {
                        FileMessage fm = (FileMessage) o;
                        Files.write(Paths.get(PATH, fm.getName()), fm.getData(), StandardOpenOption.CREATE);
                        updateClientFilesList(Paths.get(PATH));
                        log.info(String.format("File downloaded from the server: name - %s, size - %s bytes.", fm.getName(), fm.getData().length));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (o instanceof CommandMessage) {
                    CommandMessage cm = (CommandMessage) o;
                    log.info(String.format("Command was received from the server: %s.", cm.getCommand()));
                    switch (cm.getCommand()) {
                        case AUTH:
                            setAuthorized(true);
                            currentUserNick = ((CommandMessage) o).getParam();
                            requestToUpdateStorageFilesList();
                            break;
                        case WHOLE_FILES_LIST:
                            requestToUpdateStorageFilesList();
                            break;
                        case DIRECTORY_FILES_LIST:
                            requestToUpdateStorageDirectoryList();
                            break;
                    }
                }
            });
        }).start();
    }


    private void updateClientFilesList(Path path){
        try {
            pathFieldLeft.setText(path.normalize().toAbsolutePath().toString());
            clientTable.getItems().clear();
            clientTable.getItems().addAll(Files.list(path).map(FileManager::new)
                    .collect(Collectors.toList()));
            clientTable.sort();
        } catch (IOException e) {
            log.warn("Unable to update client`s list of files.");
            Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to update list of files.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void requestToUpdateStorageFilesList(){
        Network.getInstance().sendMessage(new CommandMessage(Command.WHOLE_FILES_LIST));
    }

    private void requestToUpdateStorageDirectoryList() {
        Network.getInstance().sendMessage(new CommandMessage(Command.DIRECTORY_FILES_LIST));
    }

    private void updateClientDirectory(){
        clientTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Path path = Paths.get(pathFieldLeft.getText()).resolve(clientTable.getSelectionModel()
                        .getSelectedItem().getFileName());
                if (Files.isDirectory(path)) {
                    updateClientFilesList(path);
                }
            }
        });
    }

    private String getSelectedStorageDirectoryName() {
        if (!storageTable.isFocused()) {
            return null;
        }
        return storageTable.getSelectionModel().getSelectedItem().getDirectoryName();
    }


    private void requestToUpdateStorageDirectory(){
        storageTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (getSelectedStorageDirectoryName() != null) {
                    Network.getInstance().sendMessage(new CommandMessage(Command.FILE_DIR, getSelectedStorageDirectoryName()));
                }
            }
        });
    }

    private String getSelectedFileName(TableView<FileManager> table) {
        if (!table.isFocused()) {
            return null;
        }
        return table.getSelectionModel().getSelectedItem().getFileName();
    }


    private String getCurrentPath(TextField pathField) {
        return pathField.getText();
    }


    public void uploadFile(ActionEvent actionEvent) throws IOException {
        if(getSelectedFileName(clientTable) != null){
            Network.getInstance().sendMessage(new FileMessage(Paths.get(getCurrentPath(pathFieldLeft), getSelectedFileName(clientTable))));
        }else {
            log.warn("No client`s file selected.");
            Alert alert = new Alert(Alert.AlertType.WARNING, "No file selected", ButtonType.OK);
            alert.showAndWait();
        }
    }


    public void downloadFile(ActionEvent actionEvent) {
        if (getSelectedFileName(storageTable) != null) {
            Network.getInstance().sendMessage(new CommandMessage(Command.FILE_REQUEST, getCurrentPath(pathFieldRight), getSelectedFileName(storageTable)));
        }else {
            log.warn("No storage`s file selected.");
            Alert alert = new Alert(Alert.AlertType.WARNING, "No file selected", ButtonType.OK);
            alert.showAndWait();
        }
        updateClientFilesList(Paths.get(getCurrentPath(pathFieldLeft)));
    }


    private void initializeDeleteFile(){
        MenuItem deleteItem = new MenuItem("Delete file");
        deleteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (getSelectedFileName(storageTable) != null) {
                    String[] str = getCurrentPath(pathFieldRight).split("/", 4);
                    if (currentUserNick.equals(str[2])) {
                        Network.getInstance().sendMessage(new CommandMessage(Command.FILE_DELETE, getSelectedFileName(storageTable)));
                    } else {
                        log.info("The user does not have sufficient rights to delete file.");
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Not enough rights to delete file.", ButtonType.OK);
                        alert.showAndWait();
                    }
                }
                if (getSelectedFileName(clientTable) != null) {
                    Path path = Paths.get(getCurrentPath(pathFieldLeft), getSelectedFileName(clientTable));
                    try {
                        Files.delete(path);
                        updateClientFilesList(Paths.get(getCurrentPath(pathFieldLeft)));
                    } catch (IOException e) {
                        log.warn(String.format("Failed to delete file due to the reason: %s(%s).", e.getClass().getSimpleName(), e.getMessage()));
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Failed to delete file.", ButtonType.OK);
                        alert.showAndWait();
                    }
                }
            }
        });
        contextMenu.getItems().add(deleteItem);
        storageTable.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                contextMenu.show(storageTable, event.getScreenX(), event.getScreenY());
            }
        });
        clientTable.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                contextMenu.show(clientTable, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private void initializeRenameFile() {
        MenuItem renameItem = new MenuItem("Rename file");
        renameItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (getSelectedFileName(storageTable) != null) {
                    String[] str = getCurrentPath(pathFieldRight).split("/", 4);
                    if(currentUserNick.equals(str[2])) {
                        TextInputDialog dialog = new TextInputDialog(getSelectedFileName(storageTable));
                        dialog.setTitle("Rename file");
                        dialog.setContentText("New file name: ");
                        Optional<String> result = dialog.showAndWait();
                        if (result.isPresent()) {
                            Network.getInstance().sendMessage(new CommandMessage(Command.FILE_RENAME, getSelectedFileName(storageTable), result.get()));
                        }
                    } else {
                        log.info("The user does not have sufficient rights to rename file.");
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Not enough rights to rename file", ButtonType.OK);
                        alert.showAndWait();
                    }
                }
                if (getSelectedFileName(clientTable) != null) {
                    TextInputDialog dialog = new TextInputDialog(getSelectedFileName(clientTable));
                    dialog.setTitle("Rename file");
                    dialog.setContentText("New file name: ");
                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        try {
                            renameFile(Paths.get((getCurrentPath(pathFieldLeft) + "/" + getSelectedFileName(clientTable))), result.get());
                            updateClientFilesList(Paths.get(getCurrentPath(pathFieldLeft)));
                        } catch (IOException e) {
                            log.warn(String.format("Failed to rename file due to the reason: %s (%s).", e.getClass().getSimpleName(), e.getMessage()));
                            Alert alert = new Alert(Alert.AlertType.WARNING, "Failed to rename file.", ButtonType.OK);
                            alert.showAndWait();
                        }
                    }

                }
            }
        });
        contextMenu.getItems().add(renameItem);
        storageTable.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                contextMenu.show(storageTable, event.getScreenX(), event.getScreenY());
            }
        });
        clientTable.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                contextMenu.show(clientTable, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private void renameFile(Path path, String newFileName) throws IOException {
        Path newName = path.resolveSibling(newFileName);
        Files.move(path, newName, StandardCopyOption.REPLACE_EXISTING);
    }

    public void buttonExit() {
        Network.getInstance().close();
        setAuthorized(false);
    }

    public void clientButtonUp(ActionEvent actionEvent) {
        Path path = Paths.get(pathFieldLeft.getText()).getParent();
        if (path != null) {
            updateClientFilesList(path);
        }
    }

    public void storageButtonUp(ActionEvent actionEvent) {
        Path path = Paths.get(pathFieldRight.getText()).getParent();
        if (path != null) {
            requestToUpdateStorageFilesList();
        }
    }

    public void logInAction(ActionEvent actionEvent) {
        Network.getInstance().sendMessage(new CommandMessage(Command.AUTH, login.getText(), password.getText()));
        login.clear();
        password.clear();
    }

    public void setSignUp(ActionEvent actionEvent) {
            signUp.getScene().getWindow().hide();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().
                    getResource("signUp.fxml"));
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();
    }

    public void openAccess(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Open access");
        dialog.setHeaderText("Want to share your server storage with other users?");
        dialog.setContentText("Provide username: ");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            Network.getInstance().sendMessage(new CommandMessage(Command.OPEN_ACCESS, result.get()));
        }
    }
}