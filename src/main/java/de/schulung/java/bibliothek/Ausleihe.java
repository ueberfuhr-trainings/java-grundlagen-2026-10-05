package de.schulung.java.bibliothek;

import java.time.LocalDate;
import java.util.Arrays;

public class Ausleihe {

  private final Ausweis ausweis;
  private final Exemplar exemplar;
  private LocalDate rückgabeErfolgtAm;
  private Ausleihperiode[] ausleihperioden = new Ausleihperiode[0];

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

  public Ausleihperiode[] getAusleihperioden() {
    return ausleihperioden;
  }

  public Ausleihperiode getLetzteAusleihperiode() {
    if (ausleihperioden.length == 0) {
      return null;
    }
    return ausleihperioden[ausleihperioden.length - 1];
  }

  // nicht public: Ausleihperioden werden nur von der Bibliothek angehängt
  void anhängen(Ausleihperiode ausleihperiode) {
    ausleihperioden = Arrays.copyOf(ausleihperioden, ausleihperioden.length + 1);
    ausleihperioden[ausleihperioden.length - 1] = ausleihperiode;
  }

  public boolean isOffen() {
    return rückgabeErfolgtAm == null;
  }
}
