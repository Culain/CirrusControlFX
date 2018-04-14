package CirrusControl.Main;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@SuppressWarnings("ALL")
public class Response {

    private String rawData;
    private String sendCommand;  //Command that was send to the Scanner
    private String command;      //Command that was received from the Scanner
    private int status = -20;    //Status -20: Unknown Status
    HashMap<Integer, String> statusList = new HashMap<Integer, String>() {{
        put(-30, "Connection Timeout");
        put(-20, "CirrusControlFX: No Status Message found");
        put(-10, "CirrusControlFX: Error Message from Scanner");
        put(-2, "RPL3D command failed (Not ok)");
        put(-1, "RPL3D Unknown");
        put(0, "RPL3D Command successful (All ok)");
        put(1, "RPL3D Communication error");
        put(2, "RPL3D Internal error 2");
        put(3, "RPL3D Invalid model configuration file path (.cfg)");
        put(4, "RPL3D Invalid input points");
        put(5, "RPL3D Unknown model ID");
        put(6, "RPL3D Corrupted model file");
        put(7, "RPL3D Unloaded Model");
        put(8, "RPL3D Invalid model file path (.3xx)");
        put(9, "RPL3D Internal error 9");
        put(10, "RPL3D Timeout - Calculation time exceeded");
        put(11, "RPL3D Unsupported function");
        put(100, "RPL3D Internal error 100");
        put(101, "RPL3D Invalid input scan ID");
        put(102, "RPL3D Internal error 102");
        put(103, "RPL3D Internal error 103");
        put(201, "RPL3D Script compilation error");
        put(202, "RPL3D Sript execution error (Please check RPL3D console)");
        put(1000, "RPL3D No localization : not enough points");
        put(1001, "RPL3D No localization : not localizable part");
        put(1002, "RPL3D No localization : localization out of tolerance");
        put(1003, "RPL3D No localization : converging localization");
        put(1004, "RPL3D No localization : orientation contraints not ok");
        put(1005, "RPL3D No localization : volumic estimation not ok");
        put(1006, "RPL3D No localization : surfacic estimation not ok");
        put(2000, "RPL3D All parts overlapped");
        put(3000, "RPL3D Zero gripper above part");
        put(3001, "RPL3D Gripper collision with lower plane");
        put(3002, "RPL3D Gripper collision");
        put(90001, "Bridge Communication error with the HardwareInterface");
        put(90002, "Bridge No cloud of points receive");
        put(90003, "Status Unknown");
        put(90004, "RPL3D Invalid license");
        put(90005, "RPL3D Invalid Gripper Id");
    }};
    private int nbConfig;
    private int[] configID;
    private float avg;
    private float stdv;
    private float distance_deviation;
    private float angle_deviation;
    private int licenseTime;
    private int timeToSend;     //milliseconds
    private Date creationTime;


    public Response() {
        this("ERR", null);
    }

    public void setTimeToSend(int timeToSend) {
        this.timeToSend = timeToSend;
    }

    public void setTimeToSend(float timeToSend) {
        this.timeToSend = (int) timeToSend;
    }

    public Response(String rawData) {
        this.sendCommand = null;
        this.rawData = rawData;
        parse(rawData);
        this.creationTime = new Date();
    }

    public Response(String rawData, String sendCommand) {
        this.sendCommand = sendCommand;
        this.rawData = rawData;
        parse(rawData);
        this.creationTime = new Date();
    }

    private String statusMessage;

    public String toString(){
        return String.format("Received: %s\n" +
                "Time to Receive: %sms", rawData, NumberFormat.getNumberInstance().format(this.timeToSend));
    }

    private ArrayList<String> commandList = new ArrayList<>() {{
        add("STS");
        add("MOD");
        add("LOC");
        add("LOCN ");
        add("LOCG ");
        add("CALP ");
        add("CALC");
    }};

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public String getCreationTime() {
        return sdf.format(creationTime);
    }

    public String toListEntry() {
        return String.format("Received: %s\n" +
                "\t\tStatus:\t%s\n" +
                "\t\tTime to Receive: %sms", rawData, statusMessage, NumberFormat.getNumberInstance().format(this.timeToSend));
    }

    private void parse(@NotNull String rawData) {
        if (rawData == null || rawData.isEmpty()) throw new IllegalArgumentException();

        if (rawData.startsWith("<")) {
            assert true;    //placeholder for parseXML(rawData);
        } else {
            parseEuler(rawData);
        }
        parseStatusMessage();
    }

    private void parseMOD(@NotNull String split_data) {
        status = Integer.parseInt(split_data);
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

    private void parseStatusMessage() {
        statusMessage = statusList.get(this.status);
    }

    public boolean isCommand(String command) {
        return commandList.contains(command);
    }

    private void parseEuler(@NotNull String rawData) {
        String[] split_data = rawData.split(" ");
        command = split_data[0];
        switch (command) {
            case "LOC":
                parseLOC(split_data[1]);
                break;
            case "LOCN":
                parseLOCN(split_data[1]);
                break;
            case "MOD":
                parseMOD(split_data[1]);
                break;
            case "TIMEOUT":
                parseTIMEOUT();
            default:
                return;
        }
    }

    private void parseTIMEOUT() {
        statusMessage = "Timeout during Connection";
        status = -30;
    }
}
