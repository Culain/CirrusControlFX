package CirrusControl.Main;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

public class CirrusScanner {

    private int multicomserver = 0;
    private int port = 20001;
    private String ipAddress = "127.0.0.1";

    CirrusScanner() {
        assert true;
    }

    public String getIpAddress() {
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
//        try (
//                Socket socket = new Socket(socket_ip, socket_port);
//                PrintWriter out =
//                        new PrintWriter(socket.getOutputStream(), true);
//                BufferedReader in =
//                        new BufferedReader(
//                                new InputStreamReader(socket.getInputStream()));
//                BufferedReader stdIn =
//                        new BufferedReader(
//                                new InputStreamReader(System.in))
//        ) {
//            while ((command = stdIn.readLine()) != null) {
//                out.println(command);
//                System.out.println("echo: " + in.readLine());
//            }
//        } catch (UnknownHostException e) {
//            System.err.println("Don't know about host " + ipAddress);
//            System.exit(1);
//        } catch (IOException e) {
//            System.err.println("Couldn't get I/O for the connection to " +
//                    ipAddress);
//            System.exit(1);
//        }

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
            response.setTimeToSend(Duration.between(starts, ends).toMillisPart());
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Response("ERR");
    }
}
