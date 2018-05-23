package CirrusControl.Main;

import jdk.jshell.spi.ExecutionControl;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

@SuppressWarnings("ALL")
public class Response {

    public int port;
    public String ipaddress;
    private String rawData;
    private String sendCommand;  //Command that was send to the Scanner
    private String command;      //Command that was received from the Scanner
    private int status = -20;    //Status -20: Unknown Status
    private int nbConfig;
    private int[] configID;
    private float avg;
    private float stdv;
    private float distance_deviation;
    private float angle_deviation;
    private int licenseTime;
    private int timeToSend;     //milliseconds
    private Date creationTime;
    private Position pos;
    private int Locg_IndexConfigID;
    private int locg_ConfigID;
    private Offset offset;
    private int Loco_IndexConfigID;
    private int loco_ConfigID;


    public Response() {
        this("ERR", null);
    }

    private ArrayList<String> commandList = new ArrayList<>() {{
        add("STS ");
        add("MOD ");
        add("LOC ");
        add("LOCN ");
        add("LOCG ");
        add("LOCO ");
        add("CALP ");
        add("CALC ");
    }};

    public String getCommand() {
        return command;
    }

    public void setTimeToSend(int timeToSend) {
        this.timeToSend = timeToSend;
    }

    public void setTimeToSend(float timeToSend) {
        this.timeToSend = (int) timeToSend;
    }

    public Response(String rawData) {
        this(rawData, null);
    }

    public Response(String rawData, String sendCommand) {
        this.sendCommand = sendCommand;
        this.rawData = rawData;
        parse(rawData);
        this.creationTime = new Date();
    }

    private String statusMessage;

    public int getStatus() {
        return status;
    }

    public String toString() {
        return String.format("Received: %s\n" +
                "Time to Receive: %sms", rawData, NumberFormat.getNumberInstance().format(this.timeToSend));
    }

    HashMap<Integer, String> statusList = new HashMap<Integer, String>() {{
        put(-30, "Connection Timeout");
        put(-21, "CirrusControlFX: Error parsing XML");
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
        put(90009, "RPL3D Demo Licence");
    }};

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public String getCreationTime() {
        return sdf.format(creationTime);
    }

    public String toListEntry() {
        switch (this.command) {
            case "LOCG":
                return String.format(">>> Received: \"%s\"\n" +
                        "\t\tStatus:\t%s\n" +
                        "\t\tCoordinates:\n" +
                        "%s\n" +
                        "\t\tTime to Receive: %sms", rawData, statusMessage, pos.toListEntry(), NumberFormat.getNumberInstance().format(this.timeToSend));
            case "LOCO":
                return String.format(">>> Received: \"%s\"\n" +
                        "\t\tStatus:\t%s\n" +
                        "\t\tOffset:\n" +
                        "%s\n" +
                        "\t\tTime to Receive: %sms", rawData, statusMessage, offset.toListEntry(), NumberFormat.getNumberInstance().format(this.timeToSend));
            default:
                return String.format(">>> Received: \"%s\"\n" +
                        "\t\tStatus:\t%s\n" +
                        "\t\tTime to Receive: %sms", rawData, statusMessage, NumberFormat.getNumberInstance().format(this.timeToSend));
        }
    }

    private void parse(@NotNull String rawData) {
        if (rawData == null || rawData.isEmpty()) throw new IllegalArgumentException();

        try {
            if (rawData.startsWith("<")) {
                parseXML(rawData);
            } else {
                parseEuler(rawData);
            }
            parseStatusMessage();
        } catch (ExecutionControl.NotImplementedException e) {
            e.printStackTrace();
        }
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
            e.printStackTrace();
        }
    }

    private void parseLOCG(String split_data) {
        String[] parameters = split_data.split(",");
        try {
            status = Integer.parseInt(parameters[0]);
            if (status != 0) {
                return;
            }
            Locg_IndexConfigID = Integer.parseInt(parameters[1]);
            locg_ConfigID = Integer.parseInt(parameters[2]);
            this.pos = new Position(
                    Float.parseFloat(parameters[3]),
                    Float.parseFloat(parameters[4]),
                    Float.parseFloat(parameters[5]),
                    Float.parseFloat(parameters[6]),
                    Float.parseFloat(parameters[7]),
                    Float.parseFloat(parameters[8])
            );
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void parseLOCO(String split_data) {
        String[] parameters = split_data.split(",");
        try {
            status = Integer.parseInt(parameters[0]);
            if (status != 0) {
                return;
            }
            Loco_IndexConfigID = Integer.parseInt(parameters[1]);
            loco_ConfigID = Integer.parseInt(parameters[2]);
            this.offset = new Offset(
                    Float.parseFloat(parameters[3]),
                    Float.parseFloat(parameters[4]),
                    Float.parseFloat(parameters[5]),
                    Float.parseFloat(parameters[6]),
                    Float.parseFloat(parameters[7]),
                    Float.parseFloat(parameters[8])
            );
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
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
            status = 0;
        }
    }

    private void parseCALC(@NotNull String split_data) {
        String[] parameters = split_data.split(",");
        status = Integer.parseInt(parameters[0]);
    }

    private void parseCALP(@NotNull String split_data) {
        String[] parameters = split_data.split(",");
        status = Integer.parseInt(parameters[0]);
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
            case "LOCG":
                parseLOCG(split_data[1]);
                break;
            case "LOCO":
                parseLOCO(split_data[1]);
                break;
            case "MOD":
                parseMOD(split_data[1]);
                break;
            case "STS":
                parseSTS(split_data[1]);
                break;
            case "CALP":
                parseCALP(split_data[1]);
                break;
            case "CALC":
                parseCALC(split_data[1]);
                break;
            case "TIMEOUT":
                parseTIMEOUT();
            default:
                return;
        }
    }

    private static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        Document document = builder.parse(is);
        document.getDocumentElement().normalize();
        return document;
    }

    // XML Parser

    private static String trimXML(String xml) {
        String temp = xml;
        temp = temp.replace("</COM_VN><COM_VN>", "");
        temp = temp.replace("</COM_VN> <COM_VN>", "");
        return temp;
    }

    private void parseTIMEOUT() {
        statusMessage = "Timeout during Connection";
        status = -30;
    }

    private void parseXML(String rawData) throws ExecutionControl.NotImplementedException {
        try {
            Document doc = loadXMLFromString(trimXML(rawData));
            NodeList nodeList = doc.getElementsByTagName("*");
            Node node = nodeList.item(1);
            StringBuilder eulerString = new StringBuilder();

            eulerString.append(node.getTextContent());
            eulerString.append(" ");

            for (int i = 2; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (node.getNodeName().startsWith("VN_Frame")) {  //Kuka Frame has to be handled differently
                        eulerString.append(parseXMLFrame(node));
                    } else {
                        eulerString.append(node.getTextContent());
                    }
                    if (i != nodeList.getLength() - 1) {
                        eulerString.append(",");
                    }
                }
            }
            System.out.println(String.format("XML->Euler: \"" + eulerString.toString() + "\""));
            parseEuler(eulerString.toString());

        } catch (Exception e) {
            status = -21;
            e.printStackTrace();
            throw new ExecutionControl.NotImplementedException("Error in parseXML");
        }
    }

    private String parseXMLFrame(Node node) {
        System.out.println(node.getAttributes().getLength());

        NamedNodeMap nodeList = node.getAttributes();
        ArrayList<String> frame = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            System.out.println(nodeList.item(i).getTextContent());
            frame.add(nodeList.item(i).getTextContent());
        }

        for (int i = 0; i < 3; i++) {              //A,B,C,X,Y,Z -> X,Y,Z,A,B,C
            Collections.swap(frame, i, i + 3);
        }
        Collections.swap(frame, 3, 5);      //convert KUKA ABC -> Euler WPR

        StringBuilder returnString = new StringBuilder();
        for (int i = 0; i < frame.size(); i++) {
            returnString.append(frame.get(i));
            if (i < frame.size() - 1) {
                returnString.append(",");
            }
        }
        return returnString.toString();
    }

}