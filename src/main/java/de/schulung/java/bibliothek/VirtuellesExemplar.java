package de.schulung.java.bibliothek;

import java.util.UUID;

// eine Lizenz für ein Hörbuch: so viele gleichzeitige Ausleihen, wie es Exemplare gibt
public class VirtuellesExemplar extends Exemplar {

  private final Hörbuch hörbuch;

  // nicht public: Exemplare werden nur von der Bibliothek erstellt
  VirtuellesExemplar(UUID inventarnummer, Hörbuch hörbuch) {
    super(inventarnummer);
    this.hörbuch = hörbuch;
  }

  @Override
  public Hörbuch getMedium() {
    return hörbuch;
  }
}
