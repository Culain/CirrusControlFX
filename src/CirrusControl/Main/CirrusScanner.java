package CirrusControl.Main;

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

    private int multiCommandServer = 0;
    private int port = 20001;
//    private String ipAddress = "127.0.0.1";
    SimpleStringProperty ipAddress = new SimpleStringProperty("192.168.0.100");
    SimpleIntegerProperty selectedModel = new SimpleIntegerProperty(1);
    SimpleIntegerProperty selectedScanner = new SimpleIntegerProperty(0);


    CirrusScanner() {
        assert true;
    }



//    String getIpAddress() { return ipAddress.getValue();}
//
//    void setIpAddress(String value){
//        try {
//            InetAddress.getByName(value);  //only try to parse. ignore value
//            ipAddress.set(value);
//        } catch (UnknownHostException e) {
//            //ignore
//        }
//    }

//    String getIpAddress() {
//        return this.ipAddress;
//    }

//    void setIpAddress(String value) {
//        try {
//            InetAddress.getByName(value);  //only try to parse. ignore value
//            this.ipAddress = value;
//        } catch (UnknownHostException e) {
//            //ignore
//        }
//    }


    Response sendCommand(String command) {
        return sendCommand(command, 0);
    }

    private Response sendCommand(String command, int Master) {
        String socketIp = ipAddress.getValue();
        int socketPort = port + Master + multiCommandServer * 30000; // add 30000 if the MultiCommandServer is used

        try {
            if (command.startsWith("MOD")){     //clear Memory on Scanner
                sendData(socketIp, socketPort, "MOD 0");
            }

            Instant starts = Instant.now();
            String responseData = sendData(socketIp, socketPort, command);
            Instant ends = Instant.now();

            Response response = new Response(responseData);
            response.setTimeToSend(Duration.between(starts, ends).toMillis());
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

        p.println(data);
        while (sc.hasNext()){
            stringbuilder.append(sc.next());
            stringbuilder.append(" ");      //add whitespace for later parsing
        }
        socket.close();
        return stringbuilder.toString();
    }
}
