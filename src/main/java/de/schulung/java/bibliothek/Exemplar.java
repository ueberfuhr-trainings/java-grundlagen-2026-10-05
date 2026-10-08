package de.schulung.java.bibliothek;

import java.util.Objects;
import java.util.UUID;

// abstrakt: ein Exemplar ist entweder physisch (Printmedium) oder virtuell (Hörbuch)
public abstract class Exemplar {

  private final UUID inventarnummer;

  // nicht public: Exemplare werden nur von der Bibliothek erstellt
  Exemplar(UUID inventarnummer) {
    this.inventarnummer = inventarnummer;
  }

  public UUID getInventarnummer() {
    return inventarnummer;
  }

  // die Unterklassen liefern einen spezielleren Rückgabetyp
  public abstract Medium getMedium();

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
