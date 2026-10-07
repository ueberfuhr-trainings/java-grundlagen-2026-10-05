package de.schulung.java.bank;

public class Adresse {

  private String straße;
  private String hausnummer;
  private String plz;
  private String ort;

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
}
