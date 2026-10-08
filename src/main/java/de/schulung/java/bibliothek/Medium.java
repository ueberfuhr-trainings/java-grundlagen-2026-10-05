package de.schulung.java.bibliothek;

// abstrakt: ausgeliehen werden nur konkrete Medien (Buch, Zeitschrift, Hörbuch)
// kein equals/hashCode: die Identität legt jede Unterklasse selbst fest
public abstract class Medium {

  private String titel;
  private int erscheinungsjahr;

  protected Medium() {
  }

  protected Medium(String titel, int erscheinungsjahr) {
    this.titel = titel;
    this.erscheinungsjahr = erscheinungsjahr;
  }

  public String getTitel() {
    return titel;
  }

  public void setTitel(String titel) {
    this.titel = titel;
  }

  public int getErscheinungsjahr() {
    return erscheinungsjahr;
  }

  public void setErscheinungsjahr(int erscheinungsjahr) {
    this.erscheinungsjahr = erscheinungsjahr;
  }
}
