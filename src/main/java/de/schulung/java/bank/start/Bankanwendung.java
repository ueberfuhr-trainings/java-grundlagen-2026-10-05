package de.schulung.java.bank.start;

import de.schulung.java.bank.Adresse;
import de.schulung.java.bank.Bank;
import de.schulung.java.bank.Festgeldkonto;
import de.schulung.java.bank.Girokonto;
import de.schulung.java.bank.Konto;
import de.schulung.java.bank.Kunde;
import de.schulung.java.bank.Sparkonto;

public class Bankanwendung {

  public static void main(String[] args) {

    Bank bank = Bank.INSTANCE;
    Bank bank2 = Bank.INSTANCE; // !!!!
    System.out.println(bank == bank2); // false: verschiedene Objekte

    // Kunden

    Kunde kunde1 = new Kunde();
    kunde1.setName("Anna Schmidt");
    kunde1.setWohnort(new Adresse());
    kunde1.getWohnort().setStraße("Hauptstraße");
    kunde1.getWohnort().setHausnummer("12");
    kunde1.getWohnort().setPlz("10115");
    kunde1.getWohnort().setOrt("Berlin");

    Kunde kunde2 = new Kunde();
    kunde2.setName("Bernd Müller");
    kunde2.setWohnort(new Adresse());
    kunde2.getWohnort().setStraße("Gartenweg");
    kunde2.getWohnort().setHausnummer("3a");
    kunde2.getWohnort().setPlz("80331");
    kunde2.getWohnort().setOrt("München");

    Kunde kunde3 = new Kunde();
    kunde3.setName("Clara Weiß");
    kunde3.setWohnort(new Adresse());
    kunde3.getWohnort().setStraße("Am Markt");
    kunde3.getWohnort().setHausnummer("7");
    kunde3.getWohnort().setPlz("20095");
    kunde3.getWohnort().setOrt("Hamburg");

    bank.kundeAnlegen(kunde1);
    bank.kundeAnlegen(kunde1);
    bank.kundeAnlegen(kunde2);
    bank.kundeAnlegen(kunde3);

    System.out.println(kunde1.getNummer());

    // Konten

    Girokonto konto1 = new Girokonto(kunde1);
    konto1.setDispolimit(100000); // 1.000,00 €
    konto1.einzahlen(150000); // 1.500,00 €

    Sparkonto konto2 = new Sparkonto(kunde1);
    konto2.setHabenzins(2.5); // 2,5 % p. a.
    konto2.einzahlen(25000); // 250,00 €

    Girokonto konto3 = new Girokonto(kunde2);
    konto3.setDispolimit(50000); // 500,00 €
    konto3.einzahlen(320000); // 3.200,00 €

    Sparkonto konto4 = new Sparkonto(kunde3);
    konto4.setHabenzins(3.0); // 3,0 % p. a.
    konto4.einzahlen(1000000); // 10.000,00 €

    Festgeldkonto konto5 = new Festgeldkonto(kunde2, 3.5); // 3,5 % p. a.
    konto5.setLaufzeit(24); // 2 Jahre
    konto5.einzahlen(2000000); // 20.000,00 €

    System.out.println(konto1.getDispolimit());
    System.out.println(konto2.getHabenzins());
    System.out.println(konto5.getLaufzeit() + " Monate, " + konto5.getZinssatz() + " %");

    System.out.println(konto1.getStand());
    System.out.println(konto2.getStand());

    bank.kontoAnlegen(konto1);
    bank.kontoAnlegen(konto2);
    bank.kontoAnlegen(konto3);
    bank.kontoAnlegen(konto4);
    bank.kontoAnlegen(konto5);

    konto1.einzahlen(4000);
    konto1.auszahlen(500000000);
    // konto1.setStand(konto1.getStand() - 500000000);

    // Mindeststand

    System.out.println(konto1.auszahlen(200000)); // Girokonto: -46.000 ct, innerhalb des Dispos
    System.out.println(konto1.auszahlen(100000)); // Girokonto: Fehler, Dispo überschritten
    System.out.println(konto2.auszahlen(30000));  // Sparkonto: Fehler, kein Minus erlaubt
    System.out.println(konto5.auszahlen(2000000)); // Festgeldkonto: 0 ct, genau auf 0

    // Vergleich

    Kunde kunde1Kopie = new Kunde();
    kunde1Kopie.setNummer(kunde1.getNummer());
    kunde1Kopie.setName("Anna Schmidt");

    System.out.println(kunde1 == kunde1Kopie);      // false: verschiedene Objekte
    System.out.println(kunde1.equals(kunde1Kopie)); // true: gleiche Kundennummer
    System.out.println(kunde1.equals(kunde2));

    Girokonto konto1Kopie = new Girokonto(konto1.getInhaber());
    konto1Kopie.setIban(konto1.getIban());

    System.out.println(konto1 == konto1Kopie);      // false: verschiedene Objekte
    System.out.println(konto1.equals(konto1Kopie)); // true: gleiche IBAN
    System.out.println(konto1.equals(konto2));

    // Ausgabe

    for (Kunde kunde : bank.getKunden()) {
      System.out.println(kunde.getNummer() + " " + kunde.getName() + " (" + kunde.getWohnort().getOrt() + ")");
      for (Konto konto : bank.getKonten()) {
        if (konto.getInhaber() == kunde) {
          System.out.println("  " + konto.getIban() + ": " + konto.getStand() + " ct");
        }
      }
    }

    System.out.println("Konten von de.schulung.java.bank.Kunde 1:");
    System.out.println(bank.findeKontenNachKunde(kunde1));
    System.out.println("Konten von de.schulung.java.bank.Kunde 2:");
    System.out.println(bank.findeKontenNachKunde(kunde2));

  }

}
