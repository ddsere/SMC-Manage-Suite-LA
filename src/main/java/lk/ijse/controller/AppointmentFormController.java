package lk.ijse.controller;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import lk.ijse.bo.BOFactory;
import lk.ijse.bo.custom.*;
import lk.ijse.dto.*;
import lk.ijse.entity.*;
import lk.ijse.util.Regex;
import lk.ijse.util.TextFields;
import lk.ijse.veiw.tdm.AppointmentTm;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentFormController {

    @FXML
    private JFXComboBox<String> cmbEmpId;

    @FXML
    private JFXComboBox<String> cmbServId;

    @FXML
    private JFXComboBox<String> cmbTimeSlot;

    @FXML
    private TableColumn<?, ?> colAppId;

    @FXML
    private TableColumn<?, ?> colCusName;

    @FXML
    private TableColumn<?, ?> colDate;

    @FXML
    private TableColumn<?, ?> colEmpName;

    @FXML
    private TableColumn<?, ?> colServName;

    @FXML
    private TableColumn<?, ?> colTimeSlot;

    @FXML
    private Label lblCusName;

    @FXML
    private Label lblEmpName;

    @FXML
    private Label lblServiceName;

    @FXML
    private TableView<AppointmentTmDTO> tblAppointment;

    @FXML
    private JFXTextField txtAppId;

    @FXML
    private JFXTextField txtCusPhone;

    @FXML
    private DatePicker txtDate;

    private List<AppointmentDetailsDTO> appointmentList = new ArrayList<>();
    AppointmentBO appointmentBO = (AppointmentBO) BOFactory.getBoFactory().getBO(BOFactory.BOType.APPOINTMENT);
    AppointmentDetailsBO appointmentDetailsBO = (AppointmentDetailsBO) BOFactory.getBoFactory().getBO(BOFactory.BOType.APPOINTMENT_DETAIL);
    EmployeeBO employeeBO = (EmployeeBO) BOFactory.getBoFactory().getBO(BOFactory.BOType.EMPLOYEE);
    ServiceBO serviceBO = (ServiceBO) BOFactory.getBoFactory().getBO(BOFactory.BOType.SERVICE);
    ChangeAppointmentBO changeAppointmentBO = (ChangeAppointmentBO) BOFactory.getBoFactory().getBO(BOFactory.BOType.CHANGE_APPOINTMENT);
    OrderBO orderBO = (OrderBO) BOFactory.getBoFactory().getBO(BOFactory.BOType.ORDER);
    CustomerBO customerBO = (CustomerBO) BOFactory.getBoFactory().getBO(BOFactory.BOType.CUSTOMER);
    private double price;

    public void initialize() {
        this.appointmentList = getAllItems();
        setCellValueFactory();
        loadAppTable();
        getTimeSlot();
        getServiceId();
        getEmployeeId();
    }


    private List<AppointmentDetailsDTO> getAllItems() {
        List<AppointmentDetailsDTO> appointmentList = null;
        try {
            appointmentList = appointmentDetailsBO.getAll();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return appointmentList;
    }

    private void setCellValueFactory() {
        colAppId.setCellValueFactory(new PropertyValueFactory<>("appId"));
        colCusName.setCellValueFactory(new PropertyValueFactory<>("cusName"));
        colServName.setCellValueFactory(new PropertyValueFactory<>("sName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTimeSlot.setCellValueFactory(new PropertyValueFactory<>("timeSlot"));
        colEmpName.setCellValueFactory(new PropertyValueFactory<>("empName"));
    }

    private void loadAppTable() {
        ObservableList<AppointmentTmDTO> tmList = FXCollections.observableArrayList();

        for (AppointmentDetailsDTO appointmentDetails : appointmentList) {
            AppointmentTmDTO appointmentTmDTO = new AppointmentTmDTO(
                    appointmentDetails.getAppId(),
                    appointmentDetails.getCusName(),
                    appointmentDetails.getSName(),
                    appointmentDetails.getDate(),
                    appointmentDetails.getTimeSlot(),
                    appointmentDetails.getEmpName()
            );
            tmList.add(appointmentTmDTO);
        }
        tblAppointment.setItems(tmList);
    }

    private void getTimeSlot() {
        ObservableList<String> obList = FXCollections.observableArrayList();
        try {

            obList.add("9:00am - 10:00am");
            obList.add("10:00am - 11:00am");
            obList.add("11:00am - 12:00pm");
            obList.add("1:00pm - 2:00pm");
            obList.add("2:00pm - 3:00pm");
            cmbTimeSlot.setItems(obList);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void getEmployeeId() {
        ObservableList<String> obList = FXCollections.observableArrayList();
        try {
            List<String> idList = employeeBO.getCodes();
            for (String id : idList) {
                obList.add(id);
            }

            cmbEmpId.setItems(obList);

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void getServiceId() {
        ObservableList<String> obList = FXCollections.observableArrayList();
        try {
            List<String> idList = serviceBO.getIds();
            for (String id : idList) {
                obList.add(id);
            }

            cmbServId.setItems(obList);

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void clearFields() {
        txtAppId.setText("");
        txtDate.setValue(null);
        txtCusPhone.setText("");
        lblServiceName.setText("");
        lblEmpName.setText("");
        lblCusName.setText("");
        cmbEmpId.getSelectionModel().clearSelection();
        cmbServId.getSelectionModel().clearSelection();
        cmbTimeSlot.getSelectionModel().clearSelection();
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        clearFields();
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        if (isValid()) {
            String appId = txtAppId.getText();
            LocalDate date = txtDate.getValue();
            String cusPhone = txtCusPhone.getText();
            String servId = cmbServId.getValue();
            String empId = cmbEmpId.getValue();
            String ts = cmbTimeSlot.getValue();


            try {
                price = serviceBO.getPrice(servId);
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            AppointmentDTO appointment = new AppointmentDTO(appId, date, cusPhone, servId, empId, ts, price);

            try {
                boolean isSaved = appointmentBO.save(appointment);
                if (isSaved) {
                    new Alert(Alert.AlertType.CONFIRMATION, "Appointment Saved!").show();
                }
            } catch (SQLException | ClassNotFoundException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            }
            initialize();
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        String id = txtAppId.getText();

        try {
            boolean isDeleted = appointmentBO.delete(id);
            if (isDeleted) {
                new Alert(Alert.AlertType.CONFIRMATION, "A Deleted!").show();
            }
        } catch (SQLException | ClassNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
        clearFields();
        initialize();
    }

    @FXML
    void btnUpdateOnAction(ActionEvent event) {
        if (isValid()) {
            String appId = txtAppId.getText();
            LocalDate date = txtDate.getValue();
            String cusPhone = txtCusPhone.getText();
            String servId = cmbServId.getValue();
            String empId = cmbEmpId.getValue();
            String ts = cmbTimeSlot.getValue();

            AppointmentDTO appointment = new AppointmentDTO(appId, date, cusPhone, servId, empId, ts, price);

            try {
                boolean isUpdated = appointmentBO.update(appointment);
                if (isUpdated) {
                    new Alert(Alert.AlertType.CONFIRMATION, "Appointment Updated!").show();
                }
            } catch (SQLException | ClassNotFoundException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            }
            initialize();
        }
    }

    public void btnCompleteOnAction(ActionEvent actionEvent) {
        if (isValid()) {
            String appId = txtAppId.getText();
            String servId = cmbServId.getValue();
            double price;

            try {
                price = serviceBO.getPrice(servId);
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            String cusPhone = txtCusPhone.getText();
            String cusName = lblCusName.getText();
            Date date = Date.valueOf(LocalDate.now());

            String orderId = loadNextOrderId();
            var order = new OrderDTO(orderId, date, price, cusPhone, cusName);

            String status = "Completed";
            AppointmentStatusDTO appointmentStatus = new AppointmentStatusDTO(status, appId);

            ChangeAppointmentDTO po = new ChangeAppointmentDTO(order, appointmentStatus);
            System.out.println(po.toString());
            try {
                boolean isPlaced = changeAppointmentBO.changeAppointment(po);
                System.out.println(isPlaced);
                if (isPlaced) {
                    new Alert(Alert.AlertType.CONFIRMATION, "order placed!").show();
                } else {
                    new Alert(Alert.AlertType.WARNING, "order not placed!").show();
                }
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            }
        }
    }

    private String loadNextOrderId() {
        try {
            String currentId = orderBO.currentId();
            String nextId = nextId(currentId);

            String orderId = (nextId);
            return orderId;
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String nextId(String currentId) {
        if (currentId != null) {
            String[] split = currentId.split("D");
            int id = Integer.parseInt(split[1]);
            id++;

            // Format the ID with leading zeros using String.format
            return "D" + String.format("%03d", id);
        } else {
            return "D001";
        }
    }

    @FXML
    void cmbEmpIdOnAction(ActionEvent event) {
        String empId = cmbEmpId.getValue();

        try {
            EmployeeDTO employee = employeeBO.searchById(empId);
            if (employee != null) {
                lblEmpName.setText(employee.getName());
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void cmbServIdOnAction(ActionEvent event) {
        String sId = cmbServId.getValue();

        try {
            ServiceDTO service = serviceBO.searchById(sId);
            if (service != null) {
                lblServiceName.setText(service.getDescription());
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void txtSearchOnAction(ActionEvent actionEvent) {
        String cusPhone = txtCusPhone.getText();

        try {
            CustomerDTO customer = customerBO.searchById(cusPhone);
            if (customer != null) {
                lblCusName.setText(customer.getName());
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            AppointmentSearchDTO appointmentSearch = appointmentDetailsBO.searchById(cusPhone);

            if (appointmentSearch != null) {
                txtAppId.setText(appointmentSearch.getAppId());
                txtDate.setValue(appointmentSearch.getDate());
                cmbServId.setValue(appointmentSearch.getSId());
                cmbEmpId.setValue(appointmentSearch.getEmpId());
                cmbTimeSlot.setValue(appointmentSearch.getTs());
                lblCusName.setText(appointmentSearch.getCusName());
                lblEmpName.setText(appointmentSearch.getEmpName());
                lblServiceName.setText(appointmentSearch.getSName());

            }
        } catch (SQLException | ClassNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    public boolean isValid(){
        if (!Regex.setTextColor(TextFields.ID,txtAppId)) return false;
        if (!Regex.setTextColor(TextFields.PHONE,txtCusPhone)) return false;
        return true;
    }

    public void txtIdCheckOnAction(KeyEvent keyEvent) {
        Regex.setTextColor(TextFields.ID,txtAppId);
    }

    public void txtPhoneCheckOnAction(KeyEvent keyEvent) {
        Regex.setTextColor(TextFields.PHONE,txtCusPhone);
    }
}
