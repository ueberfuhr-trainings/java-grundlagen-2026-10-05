package de.schulung.java.bibliothek;

import java.util.Objects;

public class Adresse {

  private String straße;
  private String hausnummer;
  private String plz;
  private String ort;

  public Adresse() {
  }

  public Adresse(String straße, String hausnummer, String plz, String ort) {
    this.straße = straße;
    this.hausnummer = hausnummer;
    this.plz = plz;
    this.ort = ort;
  }

  public String getStraße() {
    return straße;
  }

  public void setStraße(String straße) {
    this.straße = straße;
  }

  public String getHausnummer() {
    return hausnummer;
  }

  public void setHausnummer(String hausnummer) {
    this.hausnummer = hausnummer;
  }

  public String getPlz() {
    return plz;
  }

  public void setPlz(String plz) {
    this.plz = plz;
  }

  public String getOrt() {
    return ort;
  }

  public void setOrt(String ort) {
    this.ort = ort;
  }

  // Wertobjekt: zwei Adressen sind gleich, wenn alle Felder gleich sind
  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Adresse adresse = (Adresse) o;
    return Objects.equals(straße, adresse.straße)
      && Objects.equals(hausnummer, adresse.hausnummer)
      && Objects.equals(plz, adresse.plz)
      && Objects.equals(ort, adresse.ort);
  }

  @Override
  public int hashCode() {
    return Objects.hash(straße, hausnummer, plz, ort);
  }
}
