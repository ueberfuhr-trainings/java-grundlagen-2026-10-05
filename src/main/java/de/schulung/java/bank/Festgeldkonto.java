package de.schulung.java.bank;

public class Festgeldkonto extends Anlagekonto {

  private int laufzeit; // in Monaten
  private final double zinssatz; // in Prozent p. a.

  public Festgeldkonto(Kunde inhaber, double zinssatz) {
    super(inhaber);
    this.zinssatz = zinssatz;
  }

  public int getLaufzeit() {
    return laufzeit;
  }

  public void setLaufzeit(int laufzeit) {
    this.laufzeit = laufzeit;
  }

  @Override
  public double getZinssatz() {
    return zinssatz;
  }
}
