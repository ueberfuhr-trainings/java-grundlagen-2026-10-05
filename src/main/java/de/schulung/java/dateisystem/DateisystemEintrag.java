package de.schulung.java.dateisystem;

public abstract class DateisystemEintrag {

  private String name;

  public DateisystemEintrag(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public abstract long getGröße();

}
