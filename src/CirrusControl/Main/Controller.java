package CirrusControl.Main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

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