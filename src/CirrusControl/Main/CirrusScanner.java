package CirrusControl.Main;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

class CirrusScanner {

    private int multiCommandServer = 0;
    private int port = 20001;
    private String ipAddress = "127.0.0.1";

    CirrusScanner() {
        assert true;
    }

    String getIpAddress() {
        return this.ipAddress;
    }

    void setIpAddress(String value) {
        try {
            InetAddress.getByName(value);  //only try to parse. ignore value
            this.ipAddress = value;
        } catch (UnknownHostException e) {
            //ignore
        }
    }

    Response sendCommand(String command) {
        return sendCommand(command, 0);
    }

    private Response sendCommand(String command, int Master) {
        String socketIp = this.ipAddress;
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
        }
        return new Response("ERR");
    }

    private String sendData(String socket_ip, int socket_port, String data) throws IOException {

        Socket socket = new Socket(socket_ip, socket_port);
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
