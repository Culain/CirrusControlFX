package CirrusControl.Main;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class Controller implements Initializable {

    private final CirrusScanner scanner = new CirrusScanner();
    //    public TextArea textArea_Console = new TextArea();
    private final ObservableList<ConsoleElement> responseList = FXCollections.observableArrayList();

    //<editor-fold desc="Command Tab Elements">
    public ProgressBar progressBarBottom;
    public TextField TextField_IpAddress;
    public TextField TextField_SendCommand;
    public Spinner<Integer> Spinner_Model;
    public Spinner<Integer> Spinner_Scanner;
    public ListView<ConsoleElement> guiConsole;
    public TabPane tabPaneCommands;
    public CheckBox Checkbox_MultiComServer;
    //</editor-fold>
    public Spinner Spinner_Model_calib;
    public Spinner Spinner_Scanner_calib;
    public TextField TextField_IpAddress_calib;
    public CheckBox Checkbox_MultiComServer_calib;
    public ListView guiConsole_calib;
    public TextField tf_calib_pos1_x;
    public TextField tf_calib_pos1_y;
    public TextField tf_calib_pos1_z;
    public TextField tf_calib_pos2_x;
    public TextField tf_calib_pos2_y;
    public TextField tf_calib_pos2_z;
    public TextField tf_calib_pos3_x;
    public TextField tf_calib_pos3_y;
    public TextField tf_calib_pos3_z;
    public TextField tf_calib_pos4_x;
    public TextField tf_calib_pos4_y;
    public TextField tf_calib_pos4_z;
    public Label label_calib_pos1;
    public Label label_calib_pos2;
    public Label label_calib_pos3;
    public Label label_calib_pos4;
    //<editor-fold desc="Calibration Tab Elements">
    private SimpleStringProperty calib_pos1_x = new SimpleStringProperty();
    private SimpleStringProperty calib_pos1_y = new SimpleStringProperty();
    private SimpleStringProperty calib_pos1_z = new SimpleStringProperty();
    private SimpleStringProperty calib_pos2_x = new SimpleStringProperty();
    private SimpleStringProperty calib_pos2_y = new SimpleStringProperty();
    private SimpleStringProperty calib_pos2_z = new SimpleStringProperty();
    private SimpleStringProperty calib_pos3_x = new SimpleStringProperty();
    private SimpleStringProperty calib_pos3_y = new SimpleStringProperty();
    private SimpleStringProperty calib_pos3_z = new SimpleStringProperty();
    private SimpleStringProperty calib_pos4_x = new SimpleStringProperty();
    private SimpleStringProperty calib_pos4_y = new SimpleStringProperty();
    private SimpleStringProperty calib_pos4_z = new SimpleStringProperty();
    private SimpleStringProperty calib_pos1_text = new SimpleStringProperty();
    private SimpleStringProperty calib_pos2_text = new SimpleStringProperty();
    private SimpleStringProperty calib_pos3_text = new SimpleStringProperty();
    private SimpleStringProperty calib_pos4_text = new SimpleStringProperty();
    //</editor-fold>

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Bindings
        TextField_IpAddress.textProperty().bindBidirectional(scanner.ipAddress);
        Checkbox_MultiComServer.selectedProperty().bindBidirectional(scanner.multiCommandServer);
        scanner.selectedModel.bind(Spinner_Model.valueProperty());
        scanner.selectedScanner.bind(Spinner_Scanner.valueProperty());

        calib_pos1_text.bindBidirectional(label_calib_pos1.textProperty());
        calib_pos2_text.bindBidirectional(label_calib_pos2.textProperty());
        calib_pos3_text.bindBidirectional(label_calib_pos3.textProperty());
        calib_pos4_text.bindBidirectional(label_calib_pos4.textProperty());

        calib_pos1_x.bindBidirectional(tf_calib_pos1_x.textProperty());
        calib_pos1_y.bindBidirectional(tf_calib_pos1_y.textProperty());
        calib_pos1_z.bindBidirectional(tf_calib_pos1_z.textProperty());

        calib_pos2_x.bindBidirectional(tf_calib_pos2_x.textProperty());
        calib_pos2_y.bindBidirectional(tf_calib_pos2_y.textProperty());
        calib_pos2_z.bindBidirectional(tf_calib_pos2_z.textProperty());

        calib_pos3_x.bindBidirectional(tf_calib_pos3_x.textProperty());
        calib_pos3_y.bindBidirectional(tf_calib_pos3_y.textProperty());
        calib_pos3_z.bindBidirectional(tf_calib_pos3_z.textProperty());

        calib_pos4_x.bindBidirectional(tf_calib_pos4_x.textProperty());
        calib_pos4_y.bindBidirectional(tf_calib_pos4_y.textProperty());
        calib_pos4_z.bindBidirectional(tf_calib_pos4_z.textProperty());


        //Bindings Calibration
        TextField_IpAddress_calib.textProperty().bindBidirectional(scanner.ipAddress);
        Checkbox_MultiComServer_calib.selectedProperty().bindBidirectional(scanner.multiCommandServer);

//        calib_pos1_text.set("test");
//        calib_pos1_text.bind(Bindings.format("test %s", calib_pos1_x.getValue()));
//        label_calib_pos1.textProperty().bind(calib_pos1_text);

        //Setup Model Spinner
        SpinnerValueFactory<Integer> modelValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1024, 1);
        Spinner_Model.setValueFactory(modelValueFactory);
        Spinner_Model_calib.setValueFactory(modelValueFactory);

        //Setup ScannerID Spinner
        SpinnerValueFactory<Integer> scannerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9, 0);
        Spinner_Scanner.setValueFactory(scannerValueFactory);
        Spinner_Scanner_calib.setValueFactory(scannerValueFactory);

        //Setup guiConsole
        guiConsole.setItems(responseList);
        guiConsole_calib.setItems(responseList);

        //Tooltips
        Spinner_Scanner.setTooltip(new Tooltip("0=Master, 1-9=Slave"));
        Spinner_Model.setTooltip(new Tooltip("Select Model Number"));
        Spinner_Scanner_calib.setTooltip(new Tooltip("0=Master, 1-9=Slave"));
        Spinner_Model_calib.setTooltip(new Tooltip("Select Model Number"));

        //Set own IP address
        try {
            scanner.setIp(InetAddress.getLocalHost().getHostAddress());
            TextField_IpAddress.setTooltip(new Tooltip("Computer IP: " + InetAddress.getLocalHost().getHostAddress()));
            TextField_IpAddress_calib.setTooltip(new Tooltip("Computer IP: " + InetAddress.getLocalHost().getHostAddress()));
        } catch (UnknownHostException e) {
            scanner.setIp("127.0.0.1");
        }

        //CellFactory for listView
        guiConsole.setCellFactory(lv -> {

            ListCell<ConsoleElement> cell = new ListCell<>() {
                @Override
                protected void updateItem(ConsoleElement item, boolean empty) {
                    try {
                        super.updateItem(item, empty);

                        if (empty || item == null || item.toString() == null || item.toListEntry() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item.toListEntry());
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        setText(String.format("%s\t%s", new SimpleDateFormat("HH:mm:ss").format(new Date()), e.toString()));
                    }
                }
            };

            ContextMenu contextMenu = new ContextMenu();

            //TODO: add (set address) context menu item only for address cells
            MenuItem setAddress = new MenuItem();
            setAddress.textProperty().bind(Bindings.format("Set IP Address"));
            setAddress.setOnAction(event -> scanner.setIp(cell.getItem().getip()));
            contextMenu.getItems().add(setAddress);

            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete Item"));
            deleteItem.setOnAction(event -> guiConsole.getItems().remove(cell.getItem()));
            contextMenu.getItems().add(deleteItem);

            MenuItem deleteAllItems = new MenuItem();
            deleteAllItems.textProperty().bind(Bindings.format("Delete All Item"));
            deleteAllItems.setOnAction(event -> guiConsole.getItems().clear());
            contextMenu.getItems().add(deleteAllItems);

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });
    }

    //<editor-fold desc="Events Control Tab">

    private void sendGuiCommand(String command) {
        sendGuiCommand(command, true);
    }

    private void sendGuiCommand(String command, boolean output) {
        new Thread(() -> {
            Platform.runLater(() -> tabPaneCommands.setDisable(true));
            int sendPort = 20001 + Spinner_Scanner.getValue() + (scanner.multiCommandServer.getValue() ? 30000 : 0);    //standart port + master + multicomserver
            if (output) {
                Platform.runLater(() -> responseList.add(new ConsoleElement(ConsoleElement.elementType.sendMessage, command, scanner.ipAddress.getValue(), sendPort)));
                System.out.println(String.format("<<< Sending %s to %s:%d", command, scanner.ipAddress.getValue(), sendPort));
            }

            Response response = scanner.sendCommand(command, Spinner_Scanner.getValue());

            if (output) {
                Platform.runLater(() -> responseList.add(new ConsoleElement(response)));
                System.out.println(String.format(">>> %s", response.toString()));
            }
            Platform.runLater(() -> guiConsole.scrollTo(responseList.size() - 1));    //Scroll to last Entry
            Platform.runLater(() -> tabPaneCommands.setDisable(false));
        }).start();
    }

    public void sendModCommand(ActionEvent actionEvent) {
        sendGuiCommand("MOD " + Spinner_Model.getValue());
    }

    public void sendLocCommand(ActionEvent actionEvent) {
        sendGuiCommand("LOC 1");
    }

    public void sendLocnCommand(ActionEvent actionEvent) {
        sendGuiCommand("LOCN 1");
    }

    public void sendLocgCommand(ActionEvent actionEvent) {
        sendGuiCommand("LOCG 1");
    }

    public void sendStringCommand(ActionEvent actionEvent) {
        sendGuiCommand(TextField_SendCommand.getText());
    }

    public void sendCheckCommand(ActionEvent actionEvent) {
        sendGuiCommand("STS 0");
    }

    public void findScannerCommand(ActionEvent actionEvent) {
        new Thread(this::findScannerCommandAction).start();
    }

    private void findScannerCommandAction() {

        Platform.runLater(() -> tabPaneCommands.setDisable(true));
        Platform.runLater(() -> responseList.add(new ConsoleElement(String.format("v v v Looking for Scanner in Subnet %s.###", scanner.ipAddress.getValue().substring(0, scanner.ipAddress.getValue().lastIndexOf('.'))))));

        String tempVal = scanner.ipAddress.getValue();
        String subnet = scanner.ipAddress.getValue().substring(0, scanner.ipAddress.getValue().lastIndexOf('.'));

        ExecutorService taskExecutor = Executors.newFixedThreadPool(128);
        Set<Future<String>> set = new HashSet<>();

        for (int i = 1; i <= 255; i++) {
            String host = subnet + "." + i;
            Callable<String> callable = new PingThread(host);
            Future<String> future = taskExecutor.submit(callable);
            set.add(future);
        }
        taskExecutor.shutdown();
        try {
            taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        }

        for (Future future : set) {
            try {
                if (future.get() != null) {
                    Platform.runLater(() -> {
                        try {
                            responseList.add(new ConsoleElement(ConsoleElement.elementType.address, (String) future.get()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        scanner.ipAddress.set(tempVal);

        Platform.runLater(() -> tabPaneCommands.setDisable(false));
        Platform.runLater(() -> responseList.add(new ConsoleElement("^^^ Done")));
    }
    //</editor-fold>

    public void updateCalibLabel_1() {
        float posX, posY, posZ;

        try {
            posX = Float.parseFloat(calib_pos1_x.getValue().replace(",", "."));
        } catch (Exception e1) {
            posX = 0;
        }

        try {
            posY = Float.parseFloat(calib_pos1_y.getValue().replace(",", "."));
        } catch (Exception e2) {
            posY = 0;
        }

        try {
            posZ = Float.parseFloat(calib_pos1_z.getValue().replace(",", "."));
        } catch (Exception e3) {
            posZ = 0;
        }

        calib_pos1_text.set(String.format(Locale.US, "CALP %d,1,%.3f,%.3f,%.3f,0,0,0", scanner.selectedModel.getValue(), posX, posY, posZ));
    }

    public void updateCalibLabel_2() {
        float posX, posY, posZ;

        try {
            posX = Float.parseFloat(calib_pos2_x.getValue().replace(",", "."));
        } catch (Exception e1) {
            posX = 0;
        }

        try {
            posY = Float.parseFloat(calib_pos2_y.getValue().replace(",", "."));
        } catch (Exception e2) {
            posY = 0;
        }

        try {
            posZ = Float.parseFloat(calib_pos2_z.getValue().replace(",", "."));
        } catch (Exception e3) {
            posZ = 0;
        }

        calib_pos2_text.set(String.format(Locale.US, "CALP %d,1,%.3f,%.3f,%.3f,0,0,0", scanner.selectedModel.getValue(), posX, posY, posZ));
    }

    public void updateCalibLabel_3() {
        float posX, posY, posZ;

        try {
            posX = Float.parseFloat(calib_pos3_x.getValue().replace(",", "."));
        } catch (Exception e1) {
            posX = 0;
        }

        try {
            posY = Float.parseFloat(calib_pos3_y.getValue().replace(",", "."));
        } catch (Exception e2) {
            posY = 0;
        }

        try {
            posZ = Float.parseFloat(calib_pos3_z.getValue().replace(",", "."));
        } catch (Exception e3) {
            posZ = 0;
        }

        calib_pos3_text.set(String.format(Locale.US, "CALP %d,1,%.3f,%.3f,%.3f,0,0,0", scanner.selectedModel.getValue(), posX, posY, posZ));
    }

    public void updateCalibLabel_4() {
        float posX, posY, posZ;

        try {
            posX = Float.parseFloat(calib_pos4_x.getValue().replace(",", "."));
        } catch (Exception e1) {
            posX = 0;
        }

        try {
            posY = Float.parseFloat(calib_pos4_y.getValue().replace(",", "."));
        } catch (Exception e2) {
            posY = 0;
        }

        try {
            posZ = Float.parseFloat(calib_pos4_z.getValue().replace(",", "."));
        } catch (Exception e3) {
            posZ = 0;
        }

        calib_pos4_text.set(String.format(Locale.US, "CALP %d,1,%.3f,%.3f,%.3f,0,0,0", scanner.selectedModel.getValue(), posX, posY, posZ));
    }

    public void calib_copyP1(ActionEvent actionEvent) {
        calib_pos4_x.setValue(calib_pos1_x.getValue());
        calib_pos4_y.setValue(calib_pos1_y.getValue());
        calib_pos4_z.setValue(calib_pos1_z.getValue());
        updateCalibLabel_4();
    }

    public void calib_copyP2(ActionEvent actionEvent) {
        calib_pos4_x.setValue(calib_pos2_x.getValue());
        calib_pos4_y.setValue(calib_pos2_y.getValue());
        calib_pos4_z.setValue(calib_pos2_z.getValue());
        updateCalibLabel_4();
    }

    public void calib_copyP3(ActionEvent actionEvent) {

        calib_pos4_x.setValue(calib_pos3_x.getValue());
        calib_pos4_y.setValue(calib_pos3_y.getValue());
        calib_pos4_z.setValue(calib_pos3_z.getValue());
        updateCalibLabel_4();
    }
}


//<editor-fold desc="Console Element">

class ConsoleElement {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private int port = 20001;
    private String message;
    private elementType type = elementType.standard;
    private final Date creationTime = new Date();
    private Response response;
    private String ipaddress;
    private Object command;

    ConsoleElement(String message) {
        this.message = message;
    }

    ConsoleElement(Response response) {
        this.type = elementType.response;
        this.response = response;
    }

    ConsoleElement(elementType type, String address) {
        this.type = type;
        if (type == elementType.address) {
            this.ipaddress = address;
        } else {
            this.message = address;
        }
    }

    ConsoleElement(elementType type, String command, String address) {
        this(type, command, address, 20001);
    }

    ConsoleElement(elementType type, String command, String address, int port) {
        this.type = type;
        if (type == elementType.sendMessage) {
            this.command = command;
            this.ipaddress = address;
            this.port = port;
        }
    }

    public elementType getType() {
        return this.type;
    }

    private String printTime() {
        return sdf.format(creationTime);
    }

    public String toString() {
        return "";
    }

    String toListEntry() {
        switch (this.type) {
            case standard:
                return String.format("%s\t%s", printTime(), message);
            case control:
                return String.format("%s\t%s", printTime(), message);
            case response:
                return String.format("%s\t%s", printTime(), response.toListEntry());
            case address:
                return String.format("%s\tScanner online at [%s]", printTime(), this.ipaddress);
            case sendMessage:
                return String.format("%s\t<<< Sending \"%s\" to [%s:%d]", printTime(), this.command, this.ipaddress, this.port);
        }
        throw new NullPointerException("what");
    }

    String getip() {
        return this.ipaddress;
    }

    public enum elementType {
        standard, response, address, control, sendMessage
    }
}
//</editor-fold>


//<editor-fold desc="Helper class for pinging multiple Scanner">

class PingThread implements Callable<String> {
    private final String host;

    PingThread(String ipAddress) {
        this.host = ipAddress;
    }

    @Override
    public String call() {
        {
            try {
//            System.out.println("Thread " + currentThread().getId() + " is running");
                if (InetAddress.getByName(host).isReachable(1000)) {
                    System.out.println(host + " is pingable");
                    CirrusScanner scanner = new CirrusScanner();
                    scanner.ipAddress.set(host);
                    Response response = scanner.sendCommand("STS 0");
                    if (response.getStatus() == 0) {
                        return host;
                    }
                }
//            System.out.println("Thread " + currentThread().getId() + " is finished");
            } catch (Exception e) {
                // Throwing an exception
                System.out.println("Exception is caught");
                System.out.println(e.toString());
            }
            return null;
        }
    }
}
//</editor-fold>
