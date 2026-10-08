package de.schulung.java.bibliothek.start;

import de.schulung.java.bibliothek.Adresse;
import de.schulung.java.bibliothek.Ausleihe;
import de.schulung.java.bibliothek.Ausleihperiode;
import de.schulung.java.bibliothek.Ausweis;
import de.schulung.java.bibliothek.Bibliothek;
import de.schulung.java.bibliothek.Buch;
import de.schulung.java.bibliothek.Exemplar;
import de.schulung.java.bibliothek.Hörbuch;
import de.schulung.java.bibliothek.Leser;
import de.schulung.java.bibliothek.PhysischesExemplar;
import de.schulung.java.bibliothek.Zeitschrift;

import java.time.Duration;

public class Bibliotheksanwendung {

  public static void main(String[] args) {

    Bibliothek bibliothek = new Bibliothek();
    System.out.println("=== Heute ist der " + bibliothek.getAktuellesDatum());

    // Bücher

    Buch buch1 = new Buch("978-3-86680-100-0", "Java für Einsteiger", "Peter");
    Buch buch2 = new Buch("978-3-86680-101-7", "Python für Einsteiger", "Max");
    Buch buch3 = new Buch("978-3-86680-102-4", "Kotlin für Einsteiger", "Lena");
    Buch buch1Kopie = new Buch("978-3-86680-100-0", "Java für Einsteiger", "Peter");

    System.out.println(buch1 == buch1Kopie);      // false: verschiedene Objekte
    System.out.println(buch1.equals(buch1Kopie)); // true: gleiche ISBN

    // weitere Medien

    Zeitschrift zeitschrift = new Zeitschrift("0038-7452", 41, "Der Spiegel", 2026, 140);
    Hörbuch hörbuch = new Hörbuch("978-3-86680-200-7", "Java zum Hören", 2025,
      Duration.ofHours(8).plusMinutes(30), 300_000_000);

    // Exemplare

    Exemplar exemplar1 = bibliothek.erstelleExemplar(buch1);
    Exemplar exemplar2 = bibliothek.erstelleExemplar(buch1);
    Exemplar exemplar3 = bibliothek.erstelleExemplar(buch2);
    Exemplar exemplar4 = bibliothek.erstelleExemplar(buch2);
    Exemplar[] kotlinExemplare = new Exemplar[11];
    for (int i = 0; i < kotlinExemplare.length; i++) {
      kotlinExemplare[i] = bibliothek.erstelleExemplar(buch3);
    }
    bibliothek.erstelleExemplar(zeitschrift);
    Exemplar hörbuchLizenz1 = bibliothek.erstelleExemplar(hörbuch); // virtuell: kein Regal
    Exemplar hörbuchLizenz2 = bibliothek.erstelleExemplar(hörbuch);

    System.out.println("Exemplare:");
    for (Exemplar exemplar : bibliothek.getExemplare()) {
      String standort = exemplar instanceof PhysischesExemplar physischesExemplar
        ? "Regal " + physischesExemplar.getRegal().getNummer()
        : "Lizenz";
      System.out.println("  " + exemplar.getInventarnummer() + " "
        + exemplar.getMedium().getTitel() + " (" + standort + ")");
    }

    // Leser und Ausweise

    Leser leser1 = new Leser("Anna Schmidt", new Adresse("Hauptstraße", "12", "10115", "Berlin"));
    Leser leser2 = new Leser("Bernd Müller", new Adresse("Gartenweg", "3a", "80331", "München"));
    Leser leser3 = new Leser("Clara Weiß", new Adresse("Am Markt", "7", "20095", "Hamburg"));

    Ausweis ausweis1 = bibliothek.anmelden(leser1);
    Ausweis ausweis2 = bibliothek.anmelden(leser2);
    Ausweis ausweis3 = bibliothek.anmelden(leser3);
    System.out.println(bibliothek.anmelden(leser1)); // Fehler, null

    System.out.println("Ausweise:");
    for (Ausweis ausweis : bibliothek.getAusweise()) {
      System.out.println("  " + ausweis.getNummer() + " " + ausweis.getLeser().getName()
        + " (gültig bis " + ausweis.getGültigBis() + ")");
    }

    // Ausleihen

    Ausleihe ausleihe1 = bibliothek.ausleihen(exemplar1, ausweis1);
    Ausleihe ausleihe2 = bibliothek.ausleihen(exemplar3, ausweis1);
    bibliothek.ausleihen(exemplar1, ausweis2);     // Fehler: Exemplar ist ausgeliehen
    bibliothek.ausleihen(exemplar2, ausweis2, 30); // Fehler: Dauer zu lang
    Ausleihe ausleihe3 = bibliothek.ausleihen(exemplar2, ausweis2, 2);

    for (Exemplar exemplar : kotlinExemplare) {
      bibliothek.ausleihen(exemplar, ausweis3); // das 11. Exemplar: Fehler, maximal 10 offene Ausleihen
    }

    System.out.println("Verfügbare Exemplare von „" + buch1.getTitel() + "“: "
      + bibliothek.findeVerfügbareExemplare(buch1).length);
    System.out.println("Verfügbare Exemplare von „" + buch2.getTitel() + "“: "
      + bibliothek.findeVerfügbareExemplare(buch2).length);
    System.out.println("Verfügbare Exemplare von „" + buch3.getTitel() + "“: "
      + bibliothek.findeVerfügbareExemplare(buch3).length);

    // Hörbücher: zwei Lizenzen, also zwei gleichzeitige Ausleihen

    Ausleihe hörbuchAusleihe1 = bibliothek.ausleihen(hörbuchLizenz1, ausweis2);
    Ausleihe hörbuchAusleihe2 = bibliothek.ausleihen(hörbuchLizenz2, ausweis2);
    System.out.println("Verfügbare Exemplare von „" + hörbuch.getTitel() + "“: "
      + bibliothek.findeVerfügbareExemplare(hörbuch).length);
    byte[] datei = bibliothek.herunterladen(hörbuchAusleihe1);
    System.out.println("„" + hörbuch.getTitel() + "“ heruntergeladen: " + datei.length + " Bytes");
    bibliothek.herunterladen(ausleihe1); // Fehler: kein Hörbuch
    bibliothek.zurückgeben(hörbuchAusleihe1);
    bibliothek.zurückgeben(hörbuchAusleihe2);
    bibliothek.herunterladen(hörbuchAusleihe1); // Fehler: bereits zurückgegeben

    // Verlängern

    bibliothek.verlängern(ausleihe1, 7);  // Fehler: zu früh, Ende liegt 13 Tage in der Zukunft
    bibliothek.verlängern(ausleihe3, 29); // Fehler: Dauer zu lang
    bibliothek.verlängern(ausleihe3, 7);  // okay: Ende liegt nur 1 Tag in der Zukunft
    gibAusleihperiodenAus(ausleihe3);

    // 20 Tage später: Annas Ausleihen sind überfällig

    bibliothek.setAktuellesDatum(bibliothek.getAktuellesDatum().plusDays(20));
    System.out.println("=== 20 Tage später: " + bibliothek.getAktuellesDatum());

    System.out.println("Überfällige Ausleihen von " + leser1.getName() + ": "
      + bibliothek.findeÜberfälligeAusleihen(ausweis1).length);
    bibliothek.ausleihen(exemplar4, ausweis1); // Fehler: überfällige Ausleihen
    bibliothek.verlängern(ausleihe1, 7);       // Fehler: ausleihe2 ist ebenfalls überfällig

    bibliothek.zurückgeben(ausleihe2);
    bibliothek.zurückgeben(ausleihe2);   // Fehler: bereits zurückgegeben
    bibliothek.verlängern(ausleihe2, 7); // Fehler: bereits zurückgegeben
    bibliothek.verlängern(ausleihe1, 7); // okay: nur noch diese Ausleihe ist überfällig
    gibAusleihperiodenAus(ausleihe1);

    Ausleihe ausleihe5 = bibliothek.ausleihen(exemplar4, ausweis1); // jetzt okay

    // 10 Tage vor Ablauf von Annas Ausweis

    bibliothek.setAktuellesDatum(ausweis1.getGültigBis().minusDays(10));
    System.out.println("=== 10 Tage vor Ablauf von Annas Ausweis: " + bibliothek.getAktuellesDatum());

    bibliothek.zurückgeben(ausleihe1);
    bibliothek.zurückgeben(ausleihe5);
    Ausleihe ausleihe4 = bibliothek.ausleihen(exemplar1, ausweis1, 14); // Fehler: Ausweis läuft vorher ab
    ausleihe4 = bibliothek.ausleihen(exemplar1, ausweis1, 8);           // okay: endet 3 Tage vor Ablauf
    bibliothek.verlängern(ausweis1); // Fehler: zu früh

    bibliothek.setAktuellesDatum(bibliothek.getAktuellesDatum().plusDays(5));
    System.out.println("=== 5 Tage später: " + bibliothek.getAktuellesDatum());

    bibliothek.verlängern(ausleihe4, 14); // Fehler: Ausweis läuft vorher ab
    System.out.println("Ausweis von " + leser1.getName() + " gültig bis " + bibliothek.verlängern(ausweis1));
    bibliothek.verlängern(ausleihe4, 14); // jetzt okay
    gibAusleihperiodenAus(ausleihe4);

    // Bernds Ausweis ist abgelaufen

    bibliothek.setAktuellesDatum(ausweis2.getGültigBis().plusDays(1));
    System.out.println("=== Bernds Ausweis ist abgelaufen: " + bibliothek.getAktuellesDatum());

    System.out.println(bibliothek.findeGültigenAusweis(leser2)); // null
    Ausweis ausweis2Neu = bibliothek.anmelden(leser2);
    System.out.println("Neuer Ausweis von " + leser2.getName() + " gültig bis " + ausweis2Neu.getGültigBis());
    bibliothek.verlängern(ausweis2); // Fehler: es gibt bereits einen anderen gültigen Ausweis

    // Übersicht

    System.out.println("Ausleihen:");
    for (Ausleihe ausleihe : bibliothek.getAusleihen()) {
      System.out.println("  " + ausleihe.getExemplar().getMedium().getTitel()
        + " an " + ausleihe.getAusweis().getLeser().getName()
        + " bis " + ausleihe.getLetzteAusleihperiode().getEndetAm()
        + (ausleihe.isOffen() ? " (offen)" : " (zurückgegeben am " + ausleihe.getRückgabeErfolgtAm() + ")"));
    }

  }

  static void gibAusleihperiodenAus(Ausleihe ausleihe) {
    System.out.println("Ausleihperioden von " + ausleihe.getAusweis().getLeser().getName()
      + " für „" + ausleihe.getExemplar().getMedium().getTitel() + "“:");
    for (Ausleihperiode ausleihperiode : ausleihe.getAusleihperioden()) {
      System.out.println("  " + ausleihperiode.getBeginntAm() + " bis " + ausleihperiode.getEndetAm());
    }
  }

}
