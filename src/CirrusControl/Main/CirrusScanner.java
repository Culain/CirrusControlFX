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

    private int multicomserver = 0;
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
        String socket_ip = this.ipAddress;
        int socket_port = port + Master + multicomserver * 30000; // add 30000 if the MultiCommandServer is used

        try {
            StringBuilder stringbuilder = new StringBuilder();
            Socket socket = new Socket(socket_ip, socket_port);
            Scanner sc = new Scanner(socket.getInputStream());
            PrintStream p = new PrintStream(socket.getOutputStream());

            Instant starts = Instant.now();
//            float startTime = System.currentTimeMillis();
            p.println(command);
            while (sc.hasNext()){
                stringbuilder.append(sc.next());
                stringbuilder.append(" ");      //add whitespace for later parsing
            }
            Instant ends = Instant.now();
//            float stopTime = System.currentTimeMillis();
//            float elapsed_time = stopTime - startTime;
            socket.close();

            Response response = new Response(stringbuilder.toString());
            response.setTimeToSend(Duration.between(starts, ends).toMillis());
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Response("ERR");
    }
}
