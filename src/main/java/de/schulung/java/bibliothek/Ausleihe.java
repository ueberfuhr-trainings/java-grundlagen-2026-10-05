package de.schulung.java.bibliothek;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ausleihe {

  private final Ausweis ausweis;
  private final Exemplar exemplar;
  private LocalDate rückgabeErfolgtAm;
  private final List<Ausleihperiode> ausleihperioden = new ArrayList<>();

  public Ausleihe(Ausweis ausweis, Exemplar exemplar) {
    this.ausweis = ausweis;
    this.exemplar = exemplar;
  }

  public Ausweis getAusweis() {
    return ausweis;
  }

  public Exemplar getExemplar() {
    return exemplar;
  }

  public LocalDate getRückgabeErfolgtAm() {
    return rückgabeErfolgtAm;
  }

  // nicht public: die Rückgabe erfolgt über die Bibliothek
  void setRückgabeErfolgtAm(LocalDate rückgabeErfolgtAm) {
    this.rückgabeErfolgtAm = rückgabeErfolgtAm;
  }

  // nicht veränderbar: Ausleihperioden werden nur über anhängen() hinzugefügt
  public List<Ausleihperiode> getAusleihperioden() {
    return Collections.unmodifiableList(ausleihperioden);
  }

  public Ausleihperiode getLetzteAusleihperiode() {
    if (ausleihperioden.isEmpty()) {
      return null;
    }
    return ausleihperioden.getLast();
  }

  // nicht public: Ausleihperioden werden nur von der Bibliothek angehängt
  void anhängen(Ausleihperiode ausleihperiode) {
    ausleihperioden.add(ausleihperiode);
  }

  public boolean isOffen() {
    return rückgabeErfolgtAm == null;
  }
}
