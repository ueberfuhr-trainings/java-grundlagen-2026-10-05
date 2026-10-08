package de.schulung.java.bank;

import java.util.Objects;

public abstract class Konto {

  private String iban;
  private long stand; // in Cent
  private final Kunde inhaber;

  public Konto(Kunde inhaber) {
    this.inhaber = inhaber;
  }

  public String getIban() {
    return iban;
  }

  public void setIban(String iban) {
    this.iban = iban;
  }

  public Kunde getInhaber() {
    return inhaber;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Konto konto = (Konto) o;
    return Objects.equals(iban, konto.iban);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(iban);
  }

  public long getStand() {
    return this.stand;
  }

  public long einzahlen(long betrag) {
    this.stand = this.stand + betrag;
    return this.stand;
  }

  public long auszahlen(long betrag) {
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
