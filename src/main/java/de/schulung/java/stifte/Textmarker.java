package de.schulung.java.stifte;

public class Textmarker extends Stift {

  private double strichDicke;

  public Textmarker(String farbe, int füllstand, double strichDicke) {
    super(farbe, füllstand);
    this.strichDicke = strichDicke;
  }

  public void schreiben() {
    System.out.println("Ich bin ein Textmarker!");
  }

  public void tinteEinfüllen(int volumen) {
    this.setFüllstand(this.getFüllstand() + volumen);
  }

  public double getStrichDicke() {
    return strichDicke;
  }

  public void setStrichDicke(double strichDicke) {
    this.strichDicke = strichDicke;
  }

  @Override
  public void auffüllen() {
    tinteEinfüllen(100);
  }
}
