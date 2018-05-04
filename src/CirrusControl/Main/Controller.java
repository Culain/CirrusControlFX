package CirrusControl.Main;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.*;

public class Controller implements Initializable {

    public ProgressBar progressBarBottom;
    public TextField TextField_IpAddress;
    public TextField TextField_SendCommand;
    public Spinner<Integer> Spinner_Model;
    public Spinner<Integer> Spinner_Scanner;
    public ListView<ConsoleElement> guiConsole;
    public TabPane tabPaneCommands;
    private CirrusScanner scanner = new CirrusScanner();
    //    public TextArea textArea_Console = new TextArea();
    private ObservableList<ConsoleElement> responseList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Bindings
        TextField_IpAddress.textProperty().bindBidirectional(scanner.ipAddress);

        //Setup Model Spinner
        SpinnerValueFactory<Integer> modelValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1024, 1);
        Spinner_Model.setValueFactory(modelValueFactory);

        //Setup ScannerID Spinner
        SpinnerValueFactory<Integer> scannerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9, 0);
        Spinner_Scanner.setValueFactory(scannerValueFactory);

        //Setup guiConsole
        guiConsole.setItems(responseList);

        //Tooltips
        TextField_IpAddress.setTooltip(new Tooltip("Select Scanner IP Address"));
        Spinner_Scanner.setTooltip(new Tooltip("0=Master, 1-9=Slave"));
        Spinner_Model.setTooltip(new Tooltip("Select Model Number"));

/*        guiConsole.setCellFactory(lv -> {
 *//*           ListCell cell = new ListCell<>() {
                @Override
                protected void updateItem(ConsoleElement item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null || item.toString() == null) {
                        setText(null);
                    } else {
                        setText(item.toListEntry());
                    }
                }
            };*//*
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();

//            MenuItem editItem = new MenuItem();
//            editItem.textProperty().bind(Binding.format("E"))
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
            deleteItem.setOnAction(event -> guiConsole.getItems().remove(cell.getItem()));
            contextMenu.getItems().addAll(*//*editItem,*//* deleteItem);
        });*/
        guiConsole.setCellFactory(lv -> {

            ListCell<ConsoleElement> cell = new ListCell<>() {
                @Override
                protected void updateItem(ConsoleElement item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null || item.toString() == null) {
                        setText(null);
                    } else {
                        setText(item.toListEntry());
                    }
                }
            };

            ContextMenu contextMenu = new ContextMenu();


//            MenuItem editItem = new MenuItem();
//            editItem.textProperty().bind(Bindings.format("Edit \"%s\"", cell.itemProperty()));
//            editItem.setOnAction(event -> {
//                String item = cell.getItem();
//                // code to edit item...
//            });
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete Item"));
            deleteItem.setOnAction(event -> guiConsole.getItems().remove(cell.getItem()));
            contextMenu.getItems().add(deleteItem);

            if (cell.getClass().equals(ConsoleAddressElement.class)){
                MenuItem setAddress = new MenuItem();
                setAddress.textProperty().bind(Bindings.format("set Address"));
                setAddress.setOnAction(event -> scanner.ipAddress.setValue(cell.getItem().toString()));
                contextMenu.getItems().add(setAddress);
            }

//            cell.textProperty().bind(cell.itemProperty());

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

    //Events

    private void sendGuiCommand(String command) {
        sendGuiCommand(command, true);
    }

    private void sendGuiCommand(String command, boolean output) {
        if (output) {
            System.out.println(String.format("<<< Sending %s to %s", command, scanner.ipAddress.getValue()));
            responseList.add(new ConsoleControlElement(String.format("<<< Sending \"%s\" to [%s]", command, scanner.ipAddress.getValue())));
        }
        Response response = scanner.sendCommand(command);

        if (output) {
            System.out.println(String.format(">>> %s", response.toString()));
            responseList.add(new ConsoleResponseElement(response));
        }
        guiConsole.scrollTo(responseList.size() - 1);     //Scroll to last Entry
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
        tabPaneCommands.setDisable(true);
        String tempVal = scanner.ipAddress.getValue();
        String subnet = scanner.ipAddress.getValue().substring(0, scanner.ipAddress.getValue().lastIndexOf('.'));

        ExecutorService taskExecutor = Executors.newFixedThreadPool(128);
        Set<Future<String>> set = new HashSet<Future<String>>();

        for (int i = 1; i < 2; i++) {
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
                    responseList.add(new ConsoleAddressElement((String) future.get()));
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        scanner.ipAddress.set(tempVal);
        tabPaneCommands.setDisable(false);
    }
}

// Console Elements

abstract class ConsoleElement {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    String message;
    private Date creationTime = new Date();

    String printTime() {
        return sdf.format(creationTime);
    }

    @Override
    public String toString() {
        return "";
    }

    public String toListEntry() {
        return String.format("%s\t%s", printTime(), message);
    }
}

class ConsoleResponseElement extends ConsoleElement {
    private Response response;


    ConsoleResponseElement(Response response) {
        this.response = response;
    }

    @Override
    public String toListEntry() {
        return String.format("%s\t%s", printTime(), response.toListEntry());
    }
}

class ConsoleControlElement extends ConsoleElement {

    ConsoleControlElement(String message) {
        this.message = message;
    }

    @Override
    public String toListEntry() {
        return String.format("%s\t%s", this.printTime(), message);
    }
}

class ConsoleAddressElement extends ConsoleElement {
    private String ipaddress;

    ConsoleAddressElement(String address) {
        this.ipaddress = address;
    }

    @Override
    public String toListEntry() {
        return String.format("Scanner [%s] is online", this.ipaddress);
    }

    @Override
    public String toString() {
        return this.ipaddress;
    }
}

// Helperclass for pinging multiple Scanner

class PingThread implements Callable<String> {
    private String host;

    PingThread(String ipaddress) {
        this.host = ipaddress;
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