package de.schulung.java.bibliothek;

import java.util.UUID;

// ein greifbares Exemplar eines Printmediums, das in einem Regal steht
public class PhysischesExemplar extends Exemplar {

  private final Printmedium printmedium;
  private Regal regal;

  // nicht public: Exemplare werden nur von der Bibliothek erstellt
  PhysischesExemplar(UUID inventarnummer, Printmedium printmedium) {
    super(inventarnummer);
    this.printmedium = printmedium;
  }

  @Override
  public Printmedium getMedium() {
    return printmedium;
  }

  public Regal getRegal() {
    return regal;
  }

  public void setRegal(Regal regal) {
    this.regal = regal;
  }
}
