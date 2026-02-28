package quanlikpi.presenter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import quanlikpi.dao.EmployeeKPIDAO;
import quanlikpi.model.EmployeeKPI;
import quanlikpi.model.User;

import java.util.List;

public class DashboardPresenter {

    @FXML
    private Label lblWelcome;

    @FXML
    private Label lblTotalScore;

    @FXML
    private TableView<EmployeeKPI> tblKPIs;

    @FXML
    private TableColumn<EmployeeKPI, String> colName;

    @FXML
    private TableColumn<EmployeeKPI, Double> colTarget;

    @FXML
    private TableColumn<EmployeeKPI, Double> colActual;

    @FXML
    private TableColumn<EmployeeKPI, String> colUnit;

    @FXML
    private TableColumn<EmployeeKPI, Double> colWeight;

    @FXML
    private TableColumn<EmployeeKPI, Double> colScore;

    @FXML
    private TableColumn<EmployeeKPI, String> colAssignedTo;

    @FXML
    private TableColumn<EmployeeKPI, String> colStatus;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    private User currentUser;
    private EmployeeKPIDAO kpiDAO = new EmployeeKPIDAO();

    /**
     * Initializes the dashboard with user data.
     */
    public void initData(User user) {
        this.currentUser = user;
        lblWelcome.setText("Welcome, " + user.getFullName() + "!");

        // Hide Add/Delete buttons for Employees, re-purpose Edit to Update Progress
        if (user.getRoleId() == 3) {
            btnAdd.setVisible(false);
            btnDelete.setVisible(false);
            btnAdd.setManaged(false);
            btnDelete.setManaged(false);
            btnEdit.setText("Update Progress");
        }

        setupTable();
        loadKPIs();
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("kpiName"));
        colTarget.setCellValueFactory(new PropertyValueFactory<>("targetValue"));
        colActual.setCellValueFactory(new PropertyValueFactory<>("actualValue"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colAssignedTo.setCellValueFactory(cellData -> {
            EmployeeKPI kpi = cellData.getValue();
            if (kpi.getUserId() != null && kpi.getUserId() > 0) {
                String name = kpi.getUserName() != null ? kpi.getUserName() : "User ID: " + kpi.getUserId();
                return new javafx.beans.property.SimpleStringProperty("👤 " + name);
            } else if (kpi.getDepartmentId() != null && kpi.getDepartmentId() > 0) {
                String name = kpi.getDepartmentName() != null ? kpi.getDepartmentName()
                        : "Dept ID: " + kpi.getDepartmentId();
                return new javafx.beans.property.SimpleStringProperty("🏢 " + name);
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
    }

    private void loadKPIs() {
        List<EmployeeKPI> kpis = kpiDAO.getDashboardKPIs(currentUser);
        ObservableList<EmployeeKPI> observableList = FXCollections.observableArrayList(kpis);
        tblKPIs.setItems(observableList);

        // Update Total Score Label
        double totalScore = 0;
        int currentCycleId = 1; // Assuming cycle 1 for MVP

        if (currentUser.getRoleId() == 3) { // Employee
            totalScore = kpiDAO.getTotalScoreForAssignee(currentUser.getId(), null, currentCycleId);
            lblTotalScore.setText(String.format("My Total Score: %.2f", totalScore));
        } else if (currentUser.getRoleId() == 2) { // Manager
            totalScore = kpiDAO.getTotalScoreForAssignee(null, currentUser.getDepartmentId(), currentCycleId);
            lblTotalScore.setText(String.format("Department Total Score: %.2f", totalScore));
        } else { // Admin
            lblTotalScore.setText("Total Score: (Select User/Dept to view)");
            // In a real app, Admin would have a filter to select which user's score to view
        }
    }

    @FXML
    public void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/quanlikpi/view/Login.fxml"));
            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddKPI() {
        EmployeeKPI newKPI = showKPIForm(null);
        if (newKPI != null) {
            if (kpiDAO.addKPI(newKPI)) {
                loadKPIs();
            }
        }
    }

    @FXML
    public void handleEditKPI() {
        EmployeeKPI selectedKPI = tblKPIs.getSelectionModel().getSelectedItem();
        if (selectedKPI != null) {
            EmployeeKPI updatedKPI = showKPIForm(selectedKPI);
            if (updatedKPI != null) {
                if (kpiDAO.updateKPI(updatedKPI)) {
                    loadKPIs();
                }
            }
        }
    }

    @FXML
    public void handleDeleteKPI() {
        EmployeeKPI selectedKPI = tblKPIs.getSelectionModel().getSelectedItem();
        if (selectedKPI != null) {
            if (kpiDAO.deleteKPI(selectedKPI.getId())) {
                loadKPIs();
            }
        }
    }

    private EmployeeKPI showKPIForm(EmployeeKPI kpi) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quanlikpi/view/KPIForm.fxml"));
            Parent root = loader.load();

            KPIFormPresenter presenter = loader.getController();
            presenter.setKPI(kpi != null ? kpi : null, currentUser);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(kpi == null ? "Add KPI" : "Edit KPI");
            dialogStage.initOwner(lblWelcome.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            if (presenter.isSaveClicked()) {
                return presenter.getKPI();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
