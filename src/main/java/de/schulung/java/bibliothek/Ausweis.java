package de.schulung.java.bibliothek;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Ausweis {

  private final UUID nummer;
  private LocalDate gültigBis;
  private Leser leser;

  // nicht public: Ausweise werden nur von der Bibliothek erstellt
  Ausweis(UUID nummer, Leser leser, LocalDate gültigBis) {
    this.nummer = nummer;
    this.leser = leser;
    this.gültigBis = gültigBis;
  }

  public UUID getNummer() {
    return nummer;
  }

  public LocalDate getGültigBis() {
    return gültigBis;
  }

  // nicht public: das Verlängern erfolgt über die Bibliothek
  void setGültigBis(LocalDate gültigBis) {
    this.gültigBis = gültigBis;
  }

  public boolean isGültigAm(LocalDate datum) {
    return !datum.isAfter(gültigBis);
  }

  public Leser getLeser() {
    return leser;
  }

  public void setLeser(Leser leser) {
    this.leser = leser;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Ausweis ausweis = (Ausweis) o;
    return Objects.equals(nummer, ausweis.nummer);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(nummer);
  }
}
