package CirrusControl.Main;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.InputMethodEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private CirrusScanner scanner = new CirrusScanner();

    public ProgressBar progressBarBottom;
    public TextField textfield_ipaddr;
    public Spinner<Integer> Spinner_Model;
    public Spinner<Integer> Spinner_Scanner;
    public TextArea textArea_Console;



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
        Response response = scanner.sendCommand("LOC 1");
        textArea_Console.setText(response.toString());
    }

    public void sendLocnCommand(ActionEvent actionEvent) {
        scanner.sendCommand("LOCN 1");
    }

    public void sendLocgCommand(ActionEvent actionEvent) {
        scanner.sendCommand("LOCG 1");
    }


}


