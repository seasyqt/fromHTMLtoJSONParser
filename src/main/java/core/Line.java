package core;

public class Line implements Comparable<Line> {

  private String numberLine;
  private String name;

  public Line(String numberLine) {
    this.numberLine = numberLine;
  }

  public Line(String numberLine, String name) {
    this.numberLine = numberLine;
    this.name = name;
  }

  public String getNumberLine() {
    return numberLine;
  }

  public String getName() {
    return name;
  }


  @Override
  public int compareTo(Line o) {
    return this.getNumberLine().compareTo(o.getNumberLine());
  }

}
