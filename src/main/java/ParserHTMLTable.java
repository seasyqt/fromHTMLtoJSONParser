import core.*;
import java.io.IOException;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ParserHTMLTable {

  private final String URL = "https://ru.wikipedia.org/wiki/%D0%A1%D0%BF%D0%B8%D1%81%D0%BE%D0%BA_%D1%81%D1%82%D0%B0%D0%BD%D1%86%D0%B8%D0%B9_%D0%9C%D0%BE%D1%81%D0%BA%D0%BE%D0%B2%D1%81%D0%BA%D0%BE%D0%B3%D0%BE_%D0%BC%D0%B5%D1%82%D1%80%D0%BE%D0%BF%D0%BE%D0%BB%D0%B8%D1%82%D0%B5%D0%BD%D0%B0";
  private TreeMap<Line, ArrayList<Station>> linesWithStations = new TreeMap<>();
  private LinkedHashMap<Station, ArrayList<Station>> connectionsStations = new LinkedHashMap<>();
  private static Document wikiMosMetro;


  public ParserHTMLTable() throws IOException {
    wikiMosMetro = Jsoup.connect(URL).get();
    parseLines();
    connectionsStations = connectionUpdate();
    connectionsStations = deleteDuplicate();
  }

  public TreeMap<Line, ArrayList<Station>> getLinesWithStations() {
    return linesWithStations;
  }


  public LinkedHashMap<Station, ArrayList<Station>> getConnectionsStations() {
    return connectionsStations;
  }

  private void parseLines() {
    Elements rows = wikiMosMetro.select("table[class~=standard sortable]").select("tr:has(td)");
    rows.forEach(row -> {
      Elements cols = row.select("td:lt(4)");
      int endIndexSubstring = (cols.get(0).text().length() - 2);
      String numberLine = cols.get(0).text().substring(0, endIndexSubstring);
      String nameLine = cols.get(0).select("span").attr("title");
      String nameStation = cols.get(1).select("a").get(0).text();

      if (numberLine.length() > 4) {
        String[] someStationsInOneLine = numberLine.split("\\s");
        for (int i = 0; i < someStationsInOneLine.length; i++) {
          if (cols.get(3).hasText()) {
            addConnections(cols, someStationsInOneLine[i], nameStation);
          }
          linesWithStations
              .putIfAbsent(new Line(someStationsInOneLine[i], nameLine), new ArrayList<>());
          int finalI = i;
          linesWithStations.forEach((l, k) -> {
            if (l.getNumberLine().equals(someStationsInOneLine[finalI])) {
              linesWithStations.get(l).add(new Station(l, nameStation));
            }
          });
        }
      } else {
        if (cols.get(3).hasText()) {
          addConnections(cols, numberLine, nameStation);
        }
        linesWithStations.putIfAbsent(new Line(numberLine, nameLine), new ArrayList<>());
        linesWithStations.forEach((l, k) -> {
          if (l.getNumberLine().equals(numberLine)) {
            linesWithStations.get(l).add(new Station(l, nameStation));
          }
        });
      }
    });
  }

  private void addConnections(Elements cols, String sourceLine, String sourceNameStation) {
    cols.get(3).select("td:has(span)").forEach(col -> {
      Elements el = col.select("span");
      Station source = new Station(new Line(sourceLine), sourceNameStation);
      connectionsStations.putIfAbsent(source, new ArrayList<>());
      for (int i = 0, j = 1; i < el.size(); i = i + 2, j = j + 2) {
        String connectionLine = el.get(i).text();
        String connectionStation = el.get(j).attr("title").replaceAll("Переход на станцию", "");
        Station target = new Station(new Line(connectionLine), connectionStation);
        connectionsStations.get(source).add(target);
      }
    });

  }

  private LinkedHashMap<Station, ArrayList<Station>> connectionUpdate() {
    LinkedHashMap<Station, ArrayList<Station>> connectStations = new LinkedHashMap<>();
    connectionsStations.forEach((k, v) -> {
      connectStations.putIfAbsent(k, new ArrayList<>());
      v.forEach(station -> {
        linesWithStations.get(station.getLine()).forEach(stationGoodName -> {
          if (station.getNameStation().contains(stationGoodName.getNameStation())) {
            connectStations.get(k).add(stationGoodName);
          }
        });
      });
    });
    return connectStations;
  }

  private LinkedHashMap<Station, ArrayList<Station>> deleteDuplicate() {
    LinkedHashMap<Station, ArrayList<Station>> connectStationsWithOutDuplicate = new LinkedHashMap<>();
    Set<Station> duplicate = new TreeSet<>();
    connectionsStations.forEach((k, v) -> {
      duplicate.add(k);
      connectStationsWithOutDuplicate.putIfAbsent(k, new ArrayList<>());
      v.forEach(station -> {
        var ref = new Object() {
          boolean isDuplicate;
        };
        duplicate.forEach(key -> {
          if (key.getInfoStation().equals(station.getInfoStation())) {
            ref.isDuplicate = true;
          }
        });
        duplicate.add(station);
        if (!ref.isDuplicate) {
          connectStationsWithOutDuplicate.get(k).add(station);
        } else {
          connectStationsWithOutDuplicate.remove(k);
        }

      });

    });

    return connectStationsWithOutDuplicate;
  }

}
