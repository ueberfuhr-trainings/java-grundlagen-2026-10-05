package de.schulung.java.bank;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;

public class Bank {

  Kunde[] kunden = new Kunde[0];
  Konto[] konten = new Konto[0];


  public void kundeAnlegen(Kunde kunde) {

    // Prüfen, ob de.schulung.java.bank.Kunde bereits vorhanden ist
    for (Kunde existing : kunden) {
      if (existing == kunde) {
        System.out.println("Fehler: de.schulung.java.bank.Kunde ist bereits vorhanden.");
        return;
      }
    }

    // Kundennummer generieren
    kunde.nummer = UUID.randomUUID();
    // TODO: existiert UUID schon im Array?

    // de.schulung.java.bank.Kunde an Array anhängen
    kunden = Arrays.copyOf(kunden, kunden.length + 1);
    kunden[kunden.length - 1] = kunde;

  }

  void kontoAnlegen(Konto konto) {

    // Prüfen, ob de.schulung.java.bank.Konto bereits vorhanden ist
    for (Konto existing : konten) {
      if (existing == konto) {
        System.out.println("Fehler: de.schulung.java.bank.Konto ist bereits vorhanden.");
        return;
      }
    }

    // Kontonummer generieren
    konto.iban = randomIban();
    // TODO: existiert IBAN schon im Array?

    // de.schulung.java.bank.Konto an Array anhängen
    konten = Arrays.copyOf(konten, konten.length + 1);
    konten[konten.length - 1] = konto;

  }


  // Aufbau: DE + 2 Prüfziffern + 8-stellige BLZ + 10-stellige Kontonummer
  static String randomIban() {
    String blz = "37040044";
    String kontonummer = String.format("%010d", (long) (Math.random() * 10_000_000_000L));
    String bban = blz + kontonummer;
    // Prüfziffern nach ISO 7064 (Modulo 97): "DE00" ans Ende, D=13, E=14
    int prüfziffern = 98 - new BigInteger(bban + "131400").mod(BigInteger.valueOf(97)).intValue();
    return "DE" + String.format("%02d", prüfziffern) + bban;
  }

  Konto findeKontoNachIban(String iban) {
    for (Konto konto : konten) {
      if (konto.iban.equals(iban)) {
        return konto;
      }
    }
    return null; // de.schulung.java.bank.Konto nicht gefunden
  }

  Konto[] findeKontenNachKunde(Kunde kunde) {
    Konto[] gefundeneKonten = new Konto[konten.length];
    int gefunden = 0;
    for (Konto konto : konten) {
      if (konto.inhaber.nummer.equals(kunde.nummer)) {
        gefundeneKonten[gefunden] = konto;
        gefunden++;
      }
    }
    return Arrays.copyOf(gefundeneKonten, gefunden);
  }

}
