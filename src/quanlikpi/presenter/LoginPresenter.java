package quanlikpi.presenter;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import quanlikpi.dao.UserDAO;
import quanlikpi.model.User;

public class LoginPresenter {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMessage;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Please enter both username and password.");
            return;
        }

        User user = userDAO.login(username, password);
        if (user != null) {
            navigateToDashboard(user);
        } else {
            lblMessage.setText("Invalid username or password.");
        }
    }

    private void navigateToDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quanlikpi/view/Dashboard.fxml"));
            Parent root = loader.load();

            // Get controller and pass data
            DashboardPresenter dashboardPresenter = loader.getController();
            dashboardPresenter.initData(user);

            // Switch scene
            Stage stage = (Stage) lblMessage.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("KPI Management Dashboard - " + user.getFullName());
        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Error loading dashboard.");
        }
    }
}
