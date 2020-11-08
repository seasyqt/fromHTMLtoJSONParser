import core.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;


public class JsonCreator {

  private static final String PATH_JSON_FILE = "files/moscow.json";

  public static void main(String[] args)  {
    try {
      createJsonFile();
      countStationsInLine();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  //  /*
  public static void createJsonFile() throws IOException {
    JSONObject jsonFile = new JSONObject();
    ParserHTMLTable moscowMetro = new ParserHTMLTable();

    JSONObject stations = addLineWithStations(moscowMetro);
    JSONArray lines = addNameLines(moscowMetro);
    JSONArray connections = addConnectionStations(moscowMetro);

    jsonFile.put("stations", stations);
    jsonFile.put("lines", lines);
    jsonFile.put("connections", connections);
    try (FileWriter file = new FileWriter(PATH_JSON_FILE)) {
      file.write(jsonFile.toJSONString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static JSONObject addLineWithStations(ParserHTMLTable parserHTMLTable) {
    JSONObject stations = new JSONObject();
    TreeMap<Line, ArrayList<Station>> lines = parserHTMLTable.getLinesWithStations();
    lines.forEach((k, v) -> {
      JSONArray array = new JSONArray();
      v.forEach(station -> array.add(station.getNameStation()));
      stations.put(k.getNumberLine(), array);
    });

    return stations;
  }

  private static JSONArray addNameLines(ParserHTMLTable parserHTMLTable) {
    JSONArray nameLines = new JSONArray();

    Set<Line> arrayLine = parserHTMLTable.getLinesWithStations().keySet();
    arrayLine.forEach(l -> {
      JSONObject line = new JSONObject();
      line.put("number", l.getNumberLine());
      line.put("name", l.getName());
      nameLines.add(line);
    });
    return nameLines;
  }

  private static JSONArray addConnectionStations(ParserHTMLTable parserHTMLTable) {
    JSONArray connectionNodes = new JSONArray();

    parserHTMLTable
        .getConnectionsStations()
        .entrySet()
        .stream()
        .forEach(key -> {
          JSONArray connectionNode = new JSONArray();
          JSONObject stationNodeFirst = new JSONObject();
          String nameLine = key.getKey().getLine().getNumberLine();
          String nameStation = key.getKey().getNameStation();
          stationNodeFirst.put("line", nameLine);
          stationNodeFirst.put("station", nameStation);
          connectionNode.add(stationNodeFirst);
          key.getValue().forEach(keyArray -> {
            JSONObject stationNodeOther = new JSONObject();
            String nameLineOtherStation = keyArray.getLine().getNumberLine();
            String nameOtherStation = keyArray.getNameStation();
            stationNodeOther.put("line", nameLineOtherStation);
            stationNodeOther.put("station", nameOtherStation);
            connectionNode.add(stationNodeOther);
          });
          connectionNodes.add(connectionNode);
        });
    return connectionNodes;
  }

  public static void countStationsInLine() throws ParseException {
    JSONParser jsonParser = new JSONParser();
    JSONObject jsonFileObject = (JSONObject) jsonParser.parse(readJsonFile());

    JSONObject stationObjects = (JSONObject) jsonFileObject.get("stations");
    stationObjects.keySet().forEach(line -> {
      JSONArray stations = (JSONArray) stationObjects.get(line);
      System.out
          .println("Номер станции - " + line.toString() + "\tКол-во станций - " + stations.size());
    });

  }

  private static String readJsonFile() {
    StringBuilder builder = new StringBuilder();
    try {
      List<String> list = Files.readAllLines(Path.of(PATH_JSON_FILE));
      list.forEach(line -> builder.append(line));
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return builder.toString();
  }
}
