package core;

public class Station implements Comparable<Station> {

  private Line line;
  private String nameStation;

  public Station(Line line, String nameStation) {
    this.line = line;
    this.nameStation = nameStation;
  }

  public Line getLine() {
    return line;
  }

  public String getNameStation() {
    return nameStation;
  }

  public String getInfoStation() {
    return line.getNumberLine() + " " + nameStation;
  }

  @Override
  public int compareTo(Station o) {
    return getInfoStation().compareTo(o.getInfoStation());
  }
}
