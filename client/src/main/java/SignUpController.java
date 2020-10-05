import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;


public class SignUpController {
    public TextField nick;
    public TextField login;
    public PasswordField password;
    public Button signUpBtn;


    public void signUpAction(ActionEvent actionEvent) {
        Network.getInstance().sendMessage(new CommandMessage(Command.SIGN_UP, nick.getText(), login.getText(), password.getText()));
        Platform.runLater(() -> {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass()
                    .getResource("main.fxml"));
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();
        });
        signUpBtn.getScene().getWindow().hide();
    }

}
