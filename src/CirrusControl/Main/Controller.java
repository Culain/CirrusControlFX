package CirrusControl.Main;

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
//    public TextArea textArea_Console = new TextArea();


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

        //Tooltips
        TextField_IpAddress.setTooltip(new Tooltip("Select Scanner IP Address"));
        Spinner_Scanner.setTooltip(new Tooltip("0=Master, 1-9=Slave"));
        Spinner_Model.setTooltip(new Tooltip("Select Model Number"));
    }

    //Events

    public void sendModCommand(ActionEvent actionEvent) {
        Response response = scanner.sendCommand(String.format("MOD %d", Integer.parseInt(Spinner_Model.getValue().toString())));
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

//    public void changeScannerIpAddress(ActionEvent actionEvent) {
//        scanner.setIpAddress(TextField_IpAddress.getText());
////        textArea_Console.setText(String.format("IP Changed to: %s", scanner.getIpAddress()));
//    }
//
//    public void onChangedScannerIpAddress(InputMethodEvent inputMethodEvent) {
//        scanner.setIpAddress(TextField_IpAddress.getText());
////        textArea_Console.setText(String.format("IP Changed to: %s", scanner.getIpAddress()));
//    }
}


