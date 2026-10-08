package de.schulung.java.bibliothek;

import java.util.Objects;
import java.util.UUID;

public class Exemplar {

  private final UUID inventarnummer;
  private Buch buch;
  private Regal regal;

  // nicht public: Exemplare werden nur von der Bibliothek erstellt
  Exemplar(UUID inventarnummer, Buch buch) {
    this.inventarnummer = inventarnummer;
    this.buch = buch;
  }

  public UUID getInventarnummer() {
    return inventarnummer;
  }

  public Buch getBuch() {
    return buch;
  }

  public void setBuch(Buch buch) {
    this.buch = buch;
  }

  public Regal getRegal() {
    return regal;
  }

  public void setRegal(Regal regal) {
    this.regal = regal;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Exemplar exemplar = (Exemplar) o;
    return Objects.equals(inventarnummer, exemplar.inventarnummer);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(inventarnummer);
  }
}
