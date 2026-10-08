package de.schulung.java.stifte;

public abstract class Stift implements Beleerbar {

  private String farbe;
  private int füllstand;

  public Stift(String farbe, int füllstand) {
    this.farbe = farbe;
    this.füllstand = füllstand;
  }

  @Override
  public void leeren(int bisStand) {
    this.füllstand = bisStand;
  }

  public String getFarbe() {
    return farbe;
  }

  public void setFarbe(String farbe) {
    this.farbe = farbe;
  }

  public int getFüllstand() {
    return füllstand;
  }

  protected void setFüllstand(int füllstand) {
    this.füllstand = füllstand;
  }

  public abstract void schreiben();

  public abstract void auffüllen();

}
