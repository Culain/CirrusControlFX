package CirrusControl.Main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Controller implements Initializable {


    private CirrusScanner scanner = new CirrusScanner();

    public ProgressBar progressBarBottom;
    public TextField TextField_IpAddress;
    public TextField TextField_SendCommand;
    public Spinner<Integer> Spinner_Model;
    public Spinner<Integer> Spinner_Scanner;
    public ListView<ConsoleElement> guiConsole;
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

        guiConsole.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ConsoleElement item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.toString() == null) {
                    setText(null);
                } else {
                    setText(item.toListEntry());
                }
            }
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
//        System.out.println(String.format("<<< Sending \"%s\" to %s", "STS 0",scanner.ipAddress.getValue()));
//        responseList.add(new ConsoleControlElement(String.format("<<< Sending \"%s\" to [%s]", "STS 0", scanner.ipAddress.getValue())));
//
//        Response response = scanner.sendCommand("STS 0");
//        if (response.getCommand() == "STS"){
//            responseList.add(new ConsoleControlElement("Scanner online with IP: %s"));
//
//        }
        sendGuiCommand("STS 0");
    }

    public void findScannerCommand(ActionEvent actionEvent) {
        //todo: ping all addresses in subnet and find Scanner online
//        int timeout=1000;
        String subnet = scanner.ipAddress.getValue().substring(0, scanner.ipAddress.getValue().lastIndexOf('.'));

        ExecutorService taskExecutor = Executors.newFixedThreadPool(256);

        for (int i=1;i<255;i++) {
            String host = subnet + "." + i;
//            PingerThread object = new PingerThread(host);
//            object.start();
            taskExecutor.execute(new PingerThread(host));
        }
        taskExecutor.shutdown();
        try {
            taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }
}

abstract class ConsoleElement {
    String message;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private Date creationTime = new Date();

    String printTime(){
        return sdf.format(creationTime);
    }

    @Override
    public String toString(){
        return "";
    }

    public String toListEntry(){
        return String.format("%s\t%s", printTime(), message);
    }
}

class ConsoleResponseElement extends ConsoleElement {
    private Response response;


    ConsoleResponseElement(Response response){
        this.response = response;
//        this.creationTime = new Date();
    }

    @Override
    public String toListEntry(){
        return String.format("%s\t%s", printTime(), response.toListEntry());
    }
}

class ConsoleControlElement extends ConsoleElement {

    ConsoleControlElement(String message){
        this.message = message;
    }

    @Override
    public String toListEntry(){
        return String.format("%s\t%s", this.printTime(), message);
    }
}

class ConsoleAddressElement extends ConsoleElement {
    String ipaddress = "";
    ConsoleAddressElement(String address){
        this.ipaddress = address;
    }

    @Override
    public String toListEntry() {
        return String.format("%s", this.ipaddress);
    }

    @Override
    public String toString() {
        return this.ipaddress;
    }
}

class PingerThread extends Thread implements Callable<String>
{
    String host ;

    PingerThread(String ipaddress) {
        this.host = ipaddress;
    }
    public void run()
    {
        try
        {
//            System.out.println("Thread " + currentThread().getId() + " is running");
            if (InetAddress.getByName(host).isReachable(1000)) {
                System.out.println(host + " is reachable");
            }
//            System.out.println("Thread " + currentThread().getId() + " is finished");
        }
        catch (Exception e)
        {
            // Throwing an exception
            System.out.println ("Exception is caught");
            System.out.println(e);
        }
    }

    @Override
    public String call() throws Exception {
        return null;
    }
}