package de.schulung.java.bank;

import java.util.Objects;

public class Konto {

  String iban;
  long stand; // in Cent, stand >= 0
  Kunde inhaber;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Konto konto = (Konto) o;
    return Objects.equals(iban, konto.iban);
  }

  long getStand() {
    return this.stand;
  }

  long einzahlen(long betrag) {
    this.stand = this.stand + betrag;
    return this.stand;
  }

  long auszahlen(long betrag) {
    if (this.stand < betrag) {
      System.out.println("Fehler: Kontostand zu niedrig. Auszahlung nicht möglich.");
      return this.stand;
    }
    this.stand = this.stand - betrag;
    return this.stand;
  }

  @Override
  public String toString() {
    return "de.schulung.java.bank.Konto{" +
      "iban='" + iban + '\'' +
      ", stand=" + stand +
      '}';
  }
}
