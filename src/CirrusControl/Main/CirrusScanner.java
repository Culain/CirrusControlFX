package CirrusControl.Main;

import javafx.beans.property.IntegerProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class CirrusScanner {

    private int multicomserver = 0;
    private int port = 20001;
    private String ipAddress = "127.0.0.1";

    public CirrusScanner() {
        assert true;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String value) {
        try {
            InetAddress.getByName(value);
            this.ipAddress = value;
        } catch (UnknownHostException e) {
            //ignore
        }
    }

    public Response sendCommand(String command) {
        return sendCommand(command, 0);
    }

    public Response sendCommand(String command, int Master) {
        Response response = new Response();
        String socket_ip = this.ipAddress;
        int socket_port = port + Master + multicomserver * 30000;

//        try {
//            Socket socket = new Socket(socket_ip, socket_port); //add 30000 for multi command server usage
//            PrintWriter out = new PrintWriter(socket.getOutputStream(),
//                    true);
//            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//            System.out.println(in.toString());
//        } catch (UnknownHostException e) {
//            System.out.println("Unknown host: kq6py");
//            System.exit(1);
//        } catch (IOException e) {
//            System.out.println("No I/O");
//            System.exit(1);
//        }

        try (
                Socket socket = new Socket(socket_ip, socket_port);
                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));
                BufferedReader stdIn =
                        new BufferedReader(
                                new InputStreamReader(System.in))
        ) {
            while ((command = stdIn.readLine()) != null) {
                out.println(command);
                System.out.println("echo: " + in.readLine());
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + ipAddress);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    ipAddress);
            System.exit(1);
        }


        return response;
    }

}
