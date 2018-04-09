package CirrusControl.Main;

import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.InputMethodEvent;

public class Controller {
    public ProgressBar progressBarBottom;
    public TextField textfield_ipaddr;

    CirrusScanner scanner = new CirrusScanner();

    public void onTextchangedIpaddr(InputMethodEvent inputMethodEvent) {
        scanner.setIPAddr(textfield_ipaddr.getText());
    }


}


