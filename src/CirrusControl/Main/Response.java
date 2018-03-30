package CirrusControl.Main;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;

@SuppressWarnings("ALL")
public class Response {

    private String rawData;

    private String command;
    private int status;
    private int nbConfig;
    private int[] configID;
    private float avg;
    private float stdv;
    private float distance_deviation;
    private float angle_deviation;



    public Response(String rawData){

        this.rawData = rawData;
        parse(rawData);

    }

    private void parse(@NotNull String rawData){
        if (rawData == null || rawData.isEmpty()) throw new IllegalArgumentException();

        if (rawData.startsWith("<")){
            assert true;    //placeholder for parseXML(rawData);
        } else {
            parseEuler(rawData);
        }
    }

    private void parseEuler(@NotNull String rawData) {
        String[] split_data = rawData.split(" ");
        command = split_data[0];
        switch (command){
            case "LOC":
                parseLOC(split_data[1]);
        }

    }


    private void parseLOC(@NotNull String split_data){        /* LOC status,avg,stdv,nbconfig,configID[] */
        String[] parameters = split_data.split(",");
        status = Integer.parseInt(parameters[0]);
        avg = Float.parseFloat(parameters[1]);
        stdv = Float.parseFloat(parameters[2]);
        nbConfig = Integer.parseInt(parameters[3]);
        configID = new int[nbConfig];
        for (int i = 0; i < nbConfig; i++){
            configID[i] = Integer.parseInt(parameters[4+i]);
        }
    }

    public boolean isCommand(String command){
        return commandList.contains(command);
    }

    private ArrayList<String> commandList = new ArrayList<>() {{
        add("LOC");
        add("MOD");
        add("STS");
    }};

}
