package de.schulung.java.bibliothek;

import java.util.Objects;

public class Zeitschrift extends Printmedium {

  // eine Zeitschrift wird erst durch ISSN und Ausgabennummer eindeutig
  private final String issn;
  private final int ausgabennummer;

  public Zeitschrift(String issn, int ausgabennummer) {
    this.issn = issn;
    this.ausgabennummer = ausgabennummer;
  }

  public Zeitschrift(String issn, int ausgabennummer, String titel, int erscheinungsjahr, int seitenzahl) {
    super(titel, erscheinungsjahr, seitenzahl);
    this.issn = issn;
    this.ausgabennummer = ausgabennummer;
  }

  public String getIssn() {
    return issn;
  }

  public int getAusgabennummer() {
    return ausgabennummer;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Zeitschrift zeitschrift = (Zeitschrift) o;
    return Objects.equals(issn, zeitschrift.issn) && ausgabennummer == zeitschrift.ausgabennummer;
  }

  @Override
  public int hashCode() {
    return Objects.hash(issn, ausgabennummer);
  }
}
