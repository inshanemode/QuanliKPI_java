package quanlikpi.presenter;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import quanlikpi.dao.DepartmentDAO;
import quanlikpi.dao.EmployeeKPIDAO;
import quanlikpi.dao.UserDAO;
import quanlikpi.model.Department;
import quanlikpi.model.EmployeeKPI;
import quanlikpi.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class KPIFormPresenter {

    @FXML
    private Label lblTitle;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtTarget;
    @FXML
    private TextField txtActual;
    @FXML
    private TextField txtUnit;
    @FXML
    private TextField txtWeight;
    @FXML
    private TextField txtScore;
    @FXML
    private ComboBox<String> cmbStatus;
    @FXML
    private RadioButton rbIndividual;
    @FXML
    private RadioButton rbDepartment;
    @FXML
    private ToggleGroup tgAssignment;
    @FXML
    private ComboBox<Object> cmbAssignee;

    private EmployeeKPI currentKPI;
    private User currentUser;
    private boolean saveClicked = false;

    private UserDAO userDAO = new UserDAO();
    private DepartmentDAO deptDAO = new DepartmentDAO();
    private EmployeeKPIDAO kpiDAO = new EmployeeKPIDAO();
    private List<User> availableUsers;
    private List<Department> availableDepartments;

    @FXML
    public void initialize() {
        cmbStatus.setItems(FXCollections.observableArrayList("PENDING", "APPROVED", "REJECTED", "COMPLETED"));
        cmbStatus.setValue("PENDING");

        // Auto-complete status when Actual >= Target
        javafx.beans.value.ChangeListener<String> progressListener = (obs, oldVal, newVal) -> {
            try {
                if (!txtActual.getText().isEmpty() && !txtTarget.getText().isEmpty()) {
                    double actual = Double.parseDouble(txtActual.getText());
                    double target = Double.parseDouble(txtTarget.getText());
                    if (actual >= target) {
                        cmbStatus.setValue("COMPLETED");
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore transient parsing errors while typing
            }
        };
        txtActual.textProperty().addListener(progressListener);
        txtTarget.textProperty().addListener(progressListener);
    }

    public void setKPI(EmployeeKPI kpi, User user) {
        this.currentKPI = kpi;
        this.currentUser = user;

        loadAssignees();

        tgAssignment.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            updateAssigneeComboBox();
        });

        if (kpi != null && kpi.getId() > 0) {
            lblTitle.setText("Edit KPI");
            txtName.setText(kpi.getKpiName());
            txtTarget.setText(String.valueOf(kpi.getTargetValue()));
            txtActual.setText(kpi.getActualValue() != null ? String.valueOf(kpi.getActualValue()) : "");
            txtUnit.setText(kpi.getUnit());
            txtWeight.setText(String.valueOf(kpi.getWeight()));
            txtScore.setText(kpi.getScore() != null ? String.valueOf(kpi.getScore()) : "");
            cmbStatus.setValue(kpi.getStatus());

            if (kpi.getUserId() != null) {
                rbIndividual.setSelected(true);
                updateAssigneeComboBox();
                for (User u : availableUsers) {
                    if (u.getId() == kpi.getUserId()) {
                        cmbAssignee.getSelectionModel().select(u);
                        break;
                    }
                }
            } else if (kpi.getDepartmentId() != null) {
                rbDepartment.setSelected(true);
                updateAssigneeComboBox();
                for (Department d : availableDepartments) {
                    if (d.getId() == kpi.getDepartmentId()) {
                        cmbAssignee.getSelectionModel().select(d);
                        break;
                    }
                }
            }
        } else {
            lblTitle.setText("Add New KPI");
            updateAssigneeComboBox();
        }

        // Restrict fields for Employees so they can only update progress
        if (currentUser.getRoleId() == 3) {
            lblTitle.setText("Update KPI Progress");
            txtName.setDisable(true);
            txtTarget.setDisable(true);
            txtUnit.setDisable(true);
            txtWeight.setDisable(true);
            txtScore.setDisable(true);
            cmbAssignee.setDisable(true);
            rbIndividual.setDisable(true);
            rbDepartment.setDisable(true);
        }
    }

    private void loadAssignees() {
        if (currentUser.getRoleId() == 1) {
            // Admin sees all
            availableUsers = userDAO.getAllUsers();
            availableDepartments = deptDAO.getAllDepartments();
        } else if (currentUser.getRoleId() == 2) {
            // Manager sees their own dept only
            availableDepartments = deptDAO.getAllDepartments().stream()
                    .filter(d -> d.getId() == currentUser.getDepartmentId())
                    .collect(Collectors.toList());
            availableUsers = userDAO.getAllUsers().stream()
                    .filter(u -> u.getDepartmentId() != null && u.getDepartmentId() == currentUser.getDepartmentId())
                    .collect(Collectors.toList());
        } else {
            // Employee only self and own dept
            availableDepartments = deptDAO.getAllDepartments().stream()
                    .filter(d -> d.getId() == currentUser.getDepartmentId())
                    .collect(Collectors.toList());
            availableUsers = userDAO.getAllUsers().stream()
                    .filter(u -> u.getId() == currentUser.getId())
                    .collect(Collectors.toList());
        }
    }

    private void updateAssigneeComboBox() {
        if (rbIndividual.isSelected()) {
            cmbAssignee.setItems(FXCollections.observableArrayList(availableUsers));
        } else {
            cmbAssignee.setItems(FXCollections.observableArrayList(availableDepartments));
        }
        if (!cmbAssignee.getItems().isEmpty()) {
            cmbAssignee.getSelectionModel().selectFirst();
        }
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    @FXML
    public void handleSave() {
        if (currentKPI == null) {
            currentKPI = new EmployeeKPI();
            currentKPI.setCycleId(1); // Default cycle for MVP
            currentKPI.setManagerId(
                    currentUser.getManagerId() != null ? currentUser.getManagerId() : currentUser.getId());
        }

        Object selectedAssignee = cmbAssignee.getSelectionModel().getSelectedItem();

        if (rbIndividual.isSelected() && selectedAssignee instanceof User) {
            currentKPI.setUserId(((User) selectedAssignee).getId());
            currentKPI.setDepartmentId(null);
        } else if (rbDepartment.isSelected() && selectedAssignee instanceof Department) {
            currentKPI.setUserId(null);
            currentKPI.setDepartmentId(((Department) selectedAssignee).getId());
        }

        try {
            double newWeight = Double.parseDouble(txtWeight.getText());

            // Validate Weight limits (cannot exceed 100%)
            // Only validate limit if taking action by Admin/Manager
            if (currentUser.getRoleId() != 3) {
                double currentTotal = kpiDAO.getTotalWeightForAssignee(
                        currentKPI.getUserId(),
                        currentKPI.getDepartmentId(),
                        currentKPI.getId() > 0 ? currentKPI.getId() : -1,
                        currentKPI.getCycleId());

                if (currentTotal + newWeight > 100.0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Trọng số không hợp lệ");
                    alert.setHeaderText("Tổng trọng số (Weight) vượt quá 100%");
                    alert.setContentText("Hiện tại nhân sự/phòng ban này đã có " + currentTotal + "% trọng số. " +
                            "Bạn chỉ có thể nhập tối đa " + (100.0 - currentTotal) + "% cho KPI này.");
                    alert.showAndWait();
                    return; // abort save
                }
            }

            currentKPI.setKpiName(txtName.getText());
            currentKPI.setTargetValue(Double.parseDouble(txtTarget.getText()));
            currentKPI.setActualValue(txtActual.getText().isEmpty() ? null : Double.parseDouble(txtActual.getText()));
            currentKPI.setUnit(txtUnit.getText());
            currentKPI.setWeight(newWeight);
            currentKPI.setScore(txtScore.getText().isEmpty() ? null : Double.parseDouble(txtScore.getText()));
            currentKPI.setStatus(cmbStatus.getValue());

            saveClicked = true;
            closeStage();
        } catch (NumberFormatException e) {
            // Show alert or handle error
            System.err.println("Invalid numeric input");
        }
    }

    @FXML
    public void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) lblTitle.getScene().getWindow();
        stage.close();
    }

    public EmployeeKPI getKPI() {
        return currentKPI;
    }
}
