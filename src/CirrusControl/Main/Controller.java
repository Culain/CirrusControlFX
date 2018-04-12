package CirrusControl.Main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {


    private CirrusScanner scanner = new CirrusScanner();

    public ProgressBar progressBarBottom;
    public TextField TextField_IpAddress;
    public Spinner<Integer> Spinner_Model;
    public Spinner<Integer> Spinner_Scanner;
    public ListView<Response> guiConsole;
//    public TextArea textArea_Console = new TextArea();
private ObservableList<Response> responseList = FXCollections.observableArrayList();


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

//        guiConsole.setCellFactory(param -> new ListCell<Response>() {
//            @Override
//            protected void updateItem(Response item, boolean empty) {
//                super.updateItem(item, empty);
//
//                if (empty || item == null || item.toString() == null) {
//                    setText(null);
//                } else {
//                    setText(item.toString());
//                }
//            }
//        });
    }

    //Events

    private void sendGuiCommand(String command) {
        System.out.println(String.format("<<< Sending %s to %s", command, scanner.ipAddress.getValue()));
        Response response = scanner.sendCommand(command);
        System.out.println(String.format(">>> %s",response.toString()));
        responseList.add(response);
    }

    public void sendModCommand(ActionEvent actionEvent) {
//        Response response = scanner.sendCommand(String.format("MOD %d", Integer.parseInt(Spinner_Model.getValue().toString())));
        sendGuiCommand("MOD " + Spinner_Model.getValue());
//        textArea_Console.setText(response.toString());
    }

    public void sendLocCommand(ActionEvent actionEvent) {
        Response response = scanner.sendCommand("LOC 1");
//        textArea_Console.setText(response.toString());
    }

    public void sendLocnCommand(ActionEvent actionEvent) {
        Response response = scanner.sendCommand("LOCN 1");
//        textArea_Console.setText(response.toString());
    }

    public void sendLocgCommand(ActionEvent actionEvent) {
        Response response = scanner.sendCommand("LOCG 1");
//        textArea_Console.setText(response.toString());
    }

}


