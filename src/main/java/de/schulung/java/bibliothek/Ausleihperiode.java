package de.schulung.java.bibliothek;

import java.time.LocalDate;

public class Ausleihperiode {

  private final LocalDate erstelltAm;
  private final LocalDate beginntAm;
  private final LocalDate endetAm;

  // nicht public: Ausleihperioden werden nur von der Bibliothek erstellt
  Ausleihperiode(LocalDate erstelltAm, LocalDate beginntAm, LocalDate endetAm) {
    this.erstelltAm = erstelltAm;
    this.beginntAm = beginntAm;
    this.endetAm = endetAm;
  }

  public LocalDate getErstelltAm() {
    return erstelltAm;
  }

  public LocalDate getBeginntAm() {
    return beginntAm;
  }

  public LocalDate getEndetAm() {
    return endetAm;
  }
}
