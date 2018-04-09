package CirrusControl.Main;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.input.InputMethodEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public Spinner<Integer> Spinner_Scanner;

    public ProgressBar progressBarBottom;
    public TextField textfield_ipaddr;
    public Spinner<Integer> Spinner_Model;
    private CirrusScanner scanner = new CirrusScanner();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Setup Model Spinner
        SpinnerValueFactory<Integer> modelValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1024, 1);
        Spinner_Model.setValueFactory(modelValueFactory);

        //Setup ScannerID Spinner
        SpinnerValueFactory<Integer> scannerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9, 0);
        Spinner_Scanner.setValueFactory(scannerValueFactory);
    }

    //Events
    public void onTextchangedIpaddr(InputMethodEvent inputMethodEvent) {
        scanner.setIpAddress(textfield_ipaddr.getText());
    }

    public void sendModCommand(ActionEvent actionEvent) {
        scanner.sendCommand(String.format("MOD %d", Integer.parseInt(Spinner_Model.getValue().toString())));
    }

    public void sendLocCommand(ActionEvent actionEvent) {
        scanner.sendCommand("LOC 1");
    }

    public void sendLocnCommand(ActionEvent actionEvent) {
        scanner.sendCommand("LOCN 1");
    }

    public void sendLocgCommand(ActionEvent actionEvent) {
        scanner.sendCommand("LOCG 1");
    }


}


