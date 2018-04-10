package CirrusControl.Main;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.ArrayList;

@SuppressWarnings("ALL")
public class Response {

    private String rawData;

    private String sendCommand;
    private String command;
    private int status = -10;
    private int nbConfig;
    private int[] configID;
    private float avg;
    private float stdv;
    private float distance_deviation;
    private float angle_deviation;
    private int licenseTime;
    private int timeToSend;     //milliseconds

    public void setTimeToSend(int timeToSend) {
        this.timeToSend = timeToSend;
    }

    public void setTimeToSend(float timeToSend) {
        this.timeToSend = (int) timeToSend;
    }

    public Response() {
        assert true;
    }

    public Response(String rawData) {

        this.rawData = rawData;
        parse(rawData);
    }

    public Response(String rawData, String sendCommand) {

        this.rawData = rawData;
        this.sendCommand = sendCommand;
        parse(rawData);
    }

    public String toString(){
        return String.format("Received: %s\n" +
                "Command:\t%s\n" +
                "Status:\t\t%d\n" +
                "Time to Receive: %sms", rawData, command, status, NumberFormat.getNumberInstance().format(this.timeToSend));
    }

    private void parse(@NotNull String rawData) {
        if (rawData == null || rawData.isEmpty()) throw new IllegalArgumentException();

        if (rawData.startsWith("<")) {
            assert true;    //placeholder for parseXML(rawData);
        } else {
            parseEuler(rawData);
        }
    }

    private void parseEuler(@NotNull String rawData) {
        String[] split_data = rawData.split(" ");
        command = split_data[0];
        switch (command) {
            case "LOC":
                parseLOC(split_data[1]);
            case "LOCN":
                parseLOCN(split_data[1]);
        }
    }

    private void parseLOC(@NotNull String split_data) {        /* LOC status,avg,stdv,nbconfig,configID[] */
        String[] parameters = split_data.split(",");
        try {
            status = Integer.parseInt(parameters[0]);
            if (status != 0) {
                return;
            }                              // Break if there is an error status
            avg = Float.parseFloat(parameters[1]);
            stdv = Float.parseFloat(parameters[2]);
            nbConfig = Integer.parseInt(parameters[3]);
            configID = new int[nbConfig];
            for (int i = 0; i < nbConfig; i++) {
                configID[i] = Integer.parseInt(parameters[4 + i]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
           System.out.println("ERROR: " + e);
        }
    }

    private void parseLOCN(@NotNull String split_data) {        /* LOCN status,avg,stdv,nbconfig,configID[] */
        parseLOC(split_data);
    }

    private void parseSTS(@NotNull String split_data) {
        if (sendCommand == "STS 0") {
            status = Integer.parseInt(split_data);
        }
        if (sendCommand == "STS 1") {
            licenseTime = Integer.parseInt(split_data);
        }
    }

    public boolean isCommand(String command) {
        return commandList.contains(command);
    }

    private ArrayList<String> commandList = new ArrayList<>() {{
        add("LOC");
        add("MOD");
        add("STS");
    }};

}
