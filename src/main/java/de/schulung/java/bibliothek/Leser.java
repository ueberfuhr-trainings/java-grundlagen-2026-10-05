package de.schulung.java.bibliothek;

public class Leser {

  private String name;
  private Adresse adresse;

  public Leser() {
  }

  public Leser(String name, Adresse adresse) {
    this.name = name;
    this.adresse = adresse;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Adresse getAdresse() {
    return adresse;
  }

  public void setAdresse(Adresse adresse) {
    this.adresse = adresse;
  }

  // kein equals/hashCode: Leser haben kein identifizierendes Merkmal,
  // daher gilt die Objektidentität (==)
}
