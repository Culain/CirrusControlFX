package CirrusControl.Main;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.*;

public class Controller implements Initializable {

    private final CirrusScanner scanner = new CirrusScanner();
    //    public TextArea textArea_Console = new TextArea();
    private final ObservableList<ConsoleElement> responseList = FXCollections.observableArrayList();


    @FXML
    private ProgressBar progressBarBottom;
    @FXML
    private TextField TextField_IpAddress;
    @FXML
    private TextField TextField_SendCommand;
    @FXML
    private Spinner<Integer> Spinner_Model;
    @FXML
    private Spinner<Integer> Spinner_Scanner;
    @FXML
    private ListView<ConsoleElement> guiConsole;
    @FXML
    private TabPane tabPaneCommands;
    @FXML
    private CheckBox Checkbox_MultiComServer;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Bindings
        TextField_IpAddress.textProperty().bindBidirectional(scanner.ipAddress);
        Checkbox_MultiComServer.selectedProperty().bindBidirectional(scanner.multiCommandServer);


        //Setup Model Spinner
        SpinnerValueFactory<Integer> modelValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1024, 1);
        Spinner_Model.setValueFactory(modelValueFactory);

        //Setup ScannerID Spinner
        SpinnerValueFactory<Integer> scannerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9, 0);
        Spinner_Scanner.setValueFactory(scannerValueFactory);

        //Setup guiConsole
        guiConsole.setItems(responseList);

        //Tooltips
//        TextField_IpAddress.setTooltip(new Tooltip ("Select Scanner IP Address"));  //will be set with ip address (see below)
        Spinner_Scanner.setTooltip(new Tooltip("0=Master, 1-9=Slave"));
        Spinner_Model.setTooltip(new Tooltip("Select Model Number"));

        //Set own IP address
        try {
            scanner.setIp(InetAddress.getLocalHost().getHostAddress());
            TextField_IpAddress.setTooltip(new Tooltip("Computer IP: " + InetAddress.getLocalHost().getHostAddress()));
        } catch (UnknownHostException e) {
            scanner.setIp("127.0.0.1");
        }

        //CellFactory for listView
        guiConsole.setCellFactory(lv -> {

            ListCell<ConsoleElement> cell = new ListCell<>() {
                @Override
                protected void updateItem(ConsoleElement item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null || item.toString() == null || item.toListEntry() == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.toListEntry());
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

    //region Events Control Tab

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
    //endregion

}

//region Control Elements

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

// Helper class for pinging multiple Scanner
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
//endregion