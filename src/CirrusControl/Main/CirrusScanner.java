package CirrusControl.Main;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

final class CirrusScanner {

    private final int port = 20001;

    SimpleStringProperty ipAddress = new SimpleStringProperty("127.0.0.1");
    SimpleIntegerProperty selectedModel = new SimpleIntegerProperty(1);
    SimpleIntegerProperty selectedScanner = new SimpleIntegerProperty(0);
    public SimpleBooleanProperty multiCommandServer = new SimpleBooleanProperty(false);

    CirrusScanner() {
        assert true;        //no special constructor
    }

    /* REGEX FOR IP-ADDRESSES
    \A(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}
  (?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\z
     */


    Response sendCommand(String command) {
        return sendCommand(command, 0);
    }

    Response sendCommand(String command, int Master) {
        String socketIp = ipAddress.getValue();
        int socketPort = port + Master;
        if (multiCommandServer.getValue()) {
            socketPort += 30000;    // add 30000 if the MultiCommandServer is used
        }

        try {
            if (command.startsWith("MOD")){     //clear Memory on Scanner
                sendData(socketIp, socketPort, "MOD 0");
            }

            Instant starts = Instant.now();
            String responseData = sendData(socketIp, socketPort, command);
            Instant ends = Instant.now();

            Response response = new Response(responseData, command);
            response.setTimeToSend(Duration.between(starts, ends).toMillis());
            response.port = socketPort;
            response.ipaddress = socketIp;
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return new Response("ERR");
        }
    }

    private String sendData(String socket_ip, int socket_port, String data) throws IOException {

        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(socket_ip, socket_port), 1000);
        } catch (SocketTimeoutException e) {
            return "TIMEOUT";
        }

        Scanner sc = new Scanner(socket.getInputStream());
        PrintStream p = new PrintStream(socket.getOutputStream());
        StringBuilder stringbuilder = new StringBuilder();

        p.print(data);
        socket.shutdownOutput();

        while (sc.hasNext()) {
            stringbuilder.append(sc.next());
            stringbuilder.append(" ");      //add whitespace for later parsing
        }

        socket.close();
        return stringbuilder.toString();
    }

    void setIp(String value) {
        ipAddress.setValue(value);
    }
}
