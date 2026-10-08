package de.schulung.java.bibliothek;

// Printmedien gibt es als physische Exemplare, die in einem Regal stehen
public abstract class Printmedium extends Medium {

  private int seitenzahl;

  protected Printmedium() {
  }

  protected Printmedium(String titel, int erscheinungsjahr, int seitenzahl) {
    super(titel, erscheinungsjahr);
    this.seitenzahl = seitenzahl;
  }

  public int getSeitenzahl() {
    return seitenzahl;
  }

  public void setSeitenzahl(int seitenzahl) {
    this.seitenzahl = seitenzahl;
  }
}
