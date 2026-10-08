package de.schulung.java.bibliothek;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

/**
 * Eine Bibliothek verwaltet Exemplare, Ausweise und Ausleihen.
 * Alle Fehlerfälle werden auf der Konsole ausgegeben; Methoden mit Rückgabewert liefern dann {@code null}.
 *
 * <h2>Kalender</h2>
 * <ul>
 *   <li>Alle Prüfungen beziehen sich auf das {@linkplain #getAktuellesDatum() aktuelle Datum} der Bibliothek.
 *       Es ist anfangs das heutige Datum und kann gesetzt werden.</li>
 * </ul>
 *
 * <h2>Medien, Regale und Exemplare</h2>
 * <ul>
 *   <li>Die Bibliothek verleiht Printmedien (Bücher, Zeitschriften) und Hörbücher.</li>
 *   <li>Beim Erstellen der Bibliothek werden 10 Regale angelegt (A1, A2, B1, B2, …, E2).</li>
 *   <li>Ein neues Exemplar erhält eine zufällige Inventarnummer.</li>
 *   <li>Ein Printmedium erhält ein physisches Exemplar, das in ein Regal gestellt wird;
 *       die Regale werden der Reihe nach befüllt.</li>
 *   <li>Ein Hörbuch erhält ein virtuelles Exemplar – eine Lizenz – ohne Regal.
 *       Ein Hörbuch kann also so oft gleichzeitig ausgeliehen werden, wie es Exemplare gibt.</li>
 *   <li>Ein Exemplar ist verfügbar, wenn zu ihm keine offene Ausleihe existiert.</li>
 *   <li>Herunterladen ist nur für offene Ausleihen von Hörbüchern möglich.</li>
 * </ul>
 *
 * <h2>Ausweise</h2>
 * <ul>
 *   <li>Ein Leser erhält beim Anmelden einen Ausweis, der {@value #AUSWEIS_GÜLTIGKEIT_JAHRE} Jahr gültig ist –
 *       aber nur, wenn er noch keinen gültigen Ausweis hat.</li>
 *   <li>Ein Ausweis kann frühestens {@value #AUSWEIS_VERLÄNGERUNG_FRÜHESTENS_TAGE_VORHER} Tage vor Ablauf
 *       verlängert werden, jeweils um {@value #AUSWEIS_GÜLTIGKEIT_JAHRE} Jahr ab dem bisherigen Ablaufdatum.</li>
 *   <li>Ein abgelaufener Ausweis kann nicht verlängert werden, wenn der Leser inzwischen
 *       einen anderen gültigen Ausweis hat.</li>
 * </ul>
 *
 * <h2>Ausleihen</h2>
 * <ul>
 *   <li>Eine Ausleihe ist offen, solange das Exemplar nicht zurückgegeben wurde.</li>
 *   <li>Eine Ausleihe ist überfällig, wenn sie offen ist und ihre letzte Ausleihperiode
 *       in der Vergangenheit liegt.</li>
 *   <li>Die Dauer einer Ausleihperiode beträgt 1 bis {@value #MAXIMALE_DAUER} Tage,
 *       Standard sind {@value #STANDARD_DAUER} Tage. Der erste Tag zählt mit.</li>
 *   <li>Ausleihen ist nur möglich, wenn
 *     <ul>
 *       <li>das Exemplar verfügbar ist,</li>
 *       <li>der Leser keine überfälligen Ausleihen hat,</li>
 *       <li>der Leser weniger als {@value #MAXIMALE_OFFENE_AUSLEIHEN} offene Ausleihen hat und</li>
 *       <li>der Ausweis mindestens bis zum Ende der Ausleihperiode gültig ist.</li>
 *     </ul>
 *   </li>
 *   <li>Verlängern ist nur möglich, wenn
 *     <ul>
 *       <li>die Ausleihe noch nicht zurückgegeben wurde,</li>
 *       <li>der Leser keine <em>anderen</em> überfälligen Ausleihen hat
 *           (ist nur diese Ausleihe überfällig, darf sie verlängert werden),</li>
 *       <li>die letzte Ausleihperiode vergangen ist oder höchstens
 *           {@value #VERLÄNGERUNG_FRÜHESTENS_TAGE_VORHER} Tage in der Zukunft endet und</li>
 *       <li>der Ausweis mindestens bis zum Ende der neuen Ausleihperiode gültig ist.</li>
 *     </ul>
 *     Die neue Ausleihperiode beginnt einen Tag nach dem Ende der letzten Ausleihperiode.
 *   </li>
 *   <li>Zurückgeben ist nur möglich, wenn die Ausleihe noch nicht zurückgegeben wurde.</li>
 * </ul>
 */
public class Bibliothek {

  public static final int MAXIMALE_DAUER = 28;
  public static final int STANDARD_DAUER = 14;
  public static final int MAXIMALE_OFFENE_AUSLEIHEN = 10;
  // so viele Tage vor dem Ende der letzten Ausleihperiode darf frühestens verlängert werden
  public static final int VERLÄNGERUNG_FRÜHESTENS_TAGE_VORHER = 3;
  public static final int AUSWEIS_GÜLTIGKEIT_JAHRE = 1;
  // so viele Tage vor dem Ablauf des Ausweises darf frühestens verlängert werden
  public static final int AUSWEIS_VERLÄNGERUNG_FRÜHESTENS_TAGE_VORHER = 7;

  private LocalDate aktuellesDatum = LocalDate.now();
  private final Regal[] regale = new Regal[10];
  private Exemplar[] exemplare = new Exemplar[0];
  private Ausweis[] ausweise = new Ausweis[0];
  private Ausleihe[] ausleihen = new Ausleihe[0];

  public Bibliothek() {
    // 10 Regale: A1, A2, B1, B2, ..., E1, E2
    for (int i = 0; i < regale.length; i++) {
      char buchstabe = (char) ('A' + i / 2);
      int ziffer = i % 2 + 1;
      regale[i] = new Regal("" + buchstabe + ziffer);
    }
  }

  public LocalDate getAktuellesDatum() {
    return aktuellesDatum;
  }

  public void setAktuellesDatum(LocalDate aktuellesDatum) {
    this.aktuellesDatum = aktuellesDatum;
  }

  public Regal[] getRegale() {
    return regale;
  }

  public Exemplar[] getExemplare() {
    return exemplare;
  }

  public Ausweis[] getAusweise() {
    return ausweise;
  }

  public Ausleihe[] getAusleihen() {
    return ausleihen;
  }

  // Exemplare

  public Exemplar erstelleExemplar(Medium medium) {
    Exemplar exemplar;
    if (medium instanceof Printmedium printmedium) {
      PhysischesExemplar physischesExemplar = new PhysischesExemplar(UUID.randomUUID(), printmedium);
      // Regale der Reihe nach befüllen
      physischesExemplar.setRegal(regale[zählePhysischeExemplare() % regale.length]);
      exemplar = physischesExemplar;
    } else if (medium instanceof Hörbuch hörbuch) {
      exemplar = new VirtuellesExemplar(UUID.randomUUID(), hörbuch);
    } else {
      System.out.println("Fehler: Für dieses Medium können keine Exemplare erstellt werden.");
      return null;
    }

    exemplare = Arrays.copyOf(exemplare, exemplare.length + 1);
    exemplare[exemplare.length - 1] = exemplar;
    return exemplar;
  }

  public Exemplar[] findeVerfügbareExemplare(Medium medium) {
    Exemplar[] gefundeneExemplare = new Exemplar[exemplare.length];
    int gefunden = 0;
    for (Exemplar exemplar : exemplare) {
      if (exemplar.getMedium().equals(medium) && findeOffeneAusleihe(exemplar) == null) {
        gefundeneExemplare[gefunden] = exemplar;
        gefunden++;
      }
    }
    return Arrays.copyOf(gefundeneExemplare, gefunden);
  }

  // Ausweise

  public Ausweis findeGültigenAusweis(Leser leser) {
    for (Ausweis ausweis : ausweise) {
      if (ausweis.getLeser() == leser && ausweis.isGültigAm(aktuellesDatum)) {
        return ausweis;
      }
    }
    return null; // kein gültiger Ausweis vorhanden
  }

  public Ausweis anmelden(Leser leser) {
    if (findeGültigenAusweis(leser) != null) {
      System.out.println("Fehler: " + leser.getName() + " hat bereits einen gültigen Ausweis.");
      return null;
    }

    Ausweis ausweis = new Ausweis(UUID.randomUUID(), leser, aktuellesDatum.plusYears(AUSWEIS_GÜLTIGKEIT_JAHRE));
    ausweise = Arrays.copyOf(ausweise, ausweise.length + 1);
    ausweise[ausweise.length - 1] = ausweis;
    return ausweis;
  }

  public LocalDate verlängern(Ausweis ausweis) {
    if (ausweis.getGültigBis().isAfter(aktuellesDatum.plusDays(AUSWEIS_VERLÄNGERUNG_FRÜHESTENS_TAGE_VORHER))) {
      System.out.println("Fehler: Der Ausweis kann frühestens "
        + AUSWEIS_VERLÄNGERUNG_FRÜHESTENS_TAGE_VORHER + " Tage vor Ablauf verlängert werden.");
      return null;
    }
    Ausweis gültigerAusweis = findeGültigenAusweis(ausweis.getLeser());
    if (gültigerAusweis != null && gültigerAusweis != ausweis) {
      System.out.println("Fehler: " + ausweis.getLeser().getName() + " hat bereits einen anderen gültigen Ausweis.");
      return null;
    }

    ausweis.setGültigBis(ausweis.getGültigBis().plusYears(AUSWEIS_GÜLTIGKEIT_JAHRE));
    return ausweis.getGültigBis();
  }

  // Ausleihen

  public Ausleihe findeOffeneAusleihe(Exemplar exemplar) {
    for (Ausleihe ausleihe : ausleihen) {
      if (ausleihe.getExemplar().equals(exemplar) && ausleihe.isOffen()) {
        return ausleihe;
      }
    }
    return null; // Exemplar ist verfügbar
  }

  public Ausleihe[] findeÜberfälligeAusleihen(Ausweis ausweis) {
    Ausleihe[] gefundeneAusleihen = new Ausleihe[ausleihen.length];
    int gefunden = 0;
    for (Ausleihe ausleihe : ausleihen) {
      if (ausleihe.getAusweis().equals(ausweis) && istÜberfällig(ausleihe)) {
        gefundeneAusleihen[gefunden] = ausleihe;
        gefunden++;
      }
    }
    return Arrays.copyOf(gefundeneAusleihen, gefunden);
  }

  public Ausleihe ausleihen(Exemplar exemplar, Ausweis ausweis) {
    return ausleihen(exemplar, ausweis, STANDARD_DAUER);
  }

  public Ausleihe ausleihen(Exemplar exemplar, Ausweis ausweis, int dauer) {
    if (!istDauerZulässig(dauer)) {
      return null;
    }
    if (findeÜberfälligeAusleihen(ausweis).length > 0) {
      System.out.println("Fehler: Der Leser hat überfällige Ausleihen.");
      return null;
    }
    if (zähleOffeneAusleihen(ausweis) >= MAXIMALE_OFFENE_AUSLEIHEN) {
      System.out.println("Fehler: Der Leser hat bereits " + MAXIMALE_OFFENE_AUSLEIHEN + " offene Ausleihen.");
      return null;
    }
    if (findeOffeneAusleihe(exemplar) != null) {
      System.out.println("Fehler: Das Exemplar ist bereits ausgeliehen.");
      return null;
    }
    LocalDate beginntAm = aktuellesDatum;
    LocalDate endetAm = berechneEnde(beginntAm, dauer);
    if (!istAusweisGültigBis(ausweis, endetAm)) {
      return null;
    }

    Ausleihe ausleihe = new Ausleihe(ausweis, exemplar);
    ausleihe.anhängen(new Ausleihperiode(aktuellesDatum, beginntAm, endetAm));

    ausleihen = Arrays.copyOf(ausleihen, ausleihen.length + 1);
    ausleihen[ausleihen.length - 1] = ausleihe;
    return ausleihe;
  }

  public Ausleihperiode verlängern(Ausleihe ausleihe, int dauer) {
    if (!istDauerZulässig(dauer)) {
      return null;
    }
    if (!ausleihe.isOffen()) {
      System.out.println("Fehler: Die Ausleihe wurde bereits zurückgegeben.");
      return null;
    }
    // nur andere überfällige Ausleihen verhindern die Verlängerung
    for (Ausleihe überfällig : findeÜberfälligeAusleihen(ausleihe.getAusweis())) {
      if (überfällig != ausleihe) {
        System.out.println("Fehler: Der Leser hat andere überfällige Ausleihen.");
        return null;
      }
    }
    LocalDate letztesEnde = ausleihe.getLetzteAusleihperiode().getEndetAm();
    if (letztesEnde.isAfter(aktuellesDatum.plusDays(VERLÄNGERUNG_FRÜHESTENS_TAGE_VORHER))) {
      System.out.println("Fehler: Verlängern ist frühestens "
        + VERLÄNGERUNG_FRÜHESTENS_TAGE_VORHER + " Tage vor Ablauf möglich.");
      return null;
    }
    LocalDate beginntAm = letztesEnde.plusDays(1);
    LocalDate endetAm = berechneEnde(beginntAm, dauer);
    if (!istAusweisGültigBis(ausleihe.getAusweis(), endetAm)) {
      return null;
    }

    Ausleihperiode ausleihperiode = new Ausleihperiode(aktuellesDatum, beginntAm, endetAm);
    ausleihe.anhängen(ausleihperiode);
    return ausleihperiode;
  }

  public void zurückgeben(Ausleihe ausleihe) {
    if (!ausleihe.isOffen()) {
      System.out.println("Fehler: Die Ausleihe wurde bereits zurückgegeben.");
      return;
    }
    ausleihe.setRückgabeErfolgtAm(aktuellesDatum);
  }

  // Herunterladen

  public byte[] herunterladen(Ausleihe ausleihe) {
    if (!ausleihe.isOffen()) {
      System.out.println("Fehler: Die Ausleihe wurde bereits zurückgegeben.");
      return null;
    }
    if (!(ausleihe.getExemplar() instanceof VirtuellesExemplar virtuellesExemplar)) {
      System.out.println("Fehler: Nur Hörbücher können heruntergeladen werden.");
      return null;
    }
    // vereinfacht: statt der echten Hörbuchdatei nur leere Bytes in der Dateigröße
    return new byte[(int) virtuellesExemplar.getMedium().getDateigröße()];
  }

  // Hilfsmethoden

  private int zählePhysischeExemplare() {
    int anzahl = 0;
    for (Exemplar exemplar : exemplare) {
      if (exemplar instanceof PhysischesExemplar) {
        anzahl++;
      }
    }
    return anzahl;
  }

  private boolean istÜberfällig(Ausleihe ausleihe) {
    return ausleihe.isOffen()
      && ausleihe.getLetzteAusleihperiode().getEndetAm().isBefore(aktuellesDatum);
  }

  private int zähleOffeneAusleihen(Ausweis ausweis) {
    int anzahl = 0;
    for (Ausleihe ausleihe : ausleihen) {
      if (ausleihe.getAusweis().equals(ausweis) && ausleihe.isOffen()) {
        anzahl++;
      }
    }
    return anzahl;
  }

  private boolean istDauerZulässig(int dauer) {
    if (dauer < 1 || dauer > MAXIMALE_DAUER) {
      System.out.println("Fehler: Die Dauer muss zwischen 1 und " + MAXIMALE_DAUER + " Tagen liegen.");
      return false;
    }
    return true;
  }

  private boolean istAusweisGültigBis(Ausweis ausweis, LocalDate datum) {
    if (!ausweis.isGültigAm(datum)) {
      System.out.println("Fehler: Der Ausweis ist nur bis " + ausweis.getGültigBis() + " gültig.");
      return false;
    }
    return true;
  }

  // Ende inklusive: eine Dauer von 14 Tagen ab dem 1. endet am 14.
  private static LocalDate berechneEnde(LocalDate beginntAm, int dauer) {
    return beginntAm.plusDays(dauer - 1);
  }

}
