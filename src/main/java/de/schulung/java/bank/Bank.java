package de.schulung.java.bank;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Bank {

  public static final Bank INSTANCE = new Bank();

  private final Set<Kunde> kunden = new HashSet<>();
  // Konten nach IBAN, damit die Suche nach IBAN ohne Schleife auskommt
  private final Map<String, Konto> konten = new HashMap<>();

  private Bank() {
  }

  // nicht veränderbar: Kunden und Konten werden nur über die Bank angelegt
  public Set<Kunde> getKunden() {
    return Collections.unmodifiableSet(kunden);
  }

  public Collection<Konto> getKonten() {
    return Collections.unmodifiableCollection(konten.values());
  }

  public void kundeAnlegen(Kunde kunde) {

    // Prüfen, ob de.schulung.java.bank.Kunde bereits vorhanden ist
    if (kunden.contains(kunde)) {
      System.out.println("Fehler: de.schulung.java.bank.Kunde ist bereits vorhanden.");
      return;
    }

    // Kundennummer generieren – vor dem Hinzufügen, weil der Hashcode von der Nummer abhängt;
    // contains() vergleicht über die Nummer, also neu würfeln, solange sie schon vergeben ist
    do {
      kunde.setNummer(UUID.randomUUID());
    } while (kunden.contains(kunde));

    kunden.add(kunde);

  }

  public void kontoAnlegen(Konto konto) {

    // Prüfen, ob de.schulung.java.bank.Konto bereits vorhanden ist
    if (konten.containsValue(konto)) {
      System.out.println("Fehler: de.schulung.java.bank.Konto ist bereits vorhanden.");
      return;
    }

    // Kontonummer generieren, solange sie schon vergeben ist
    String iban;
    do {
      iban = randomIban();
    } while (konten.containsKey(iban));
    konto.setIban(iban);

    konten.put(konto.getIban(), konto);

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

  public Konto findeKontoNachIban(String iban) {
    return konten.get(iban); // null, wenn das Konto nicht gefunden wurde
  }

  public List<Konto> findeKontenNachKunde(Kunde kunde) {
    List<Konto> gefundeneKonten = new ArrayList<>();
    for (Konto konto : konten.values()) {
      if (konto.getInhaber().getNummer().equals(kunde.getNummer())) {
        gefundeneKonten.add(konto);
      }
    }
    return gefundeneKonten;
  }

}
