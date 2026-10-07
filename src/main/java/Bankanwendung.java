public class Bankanwendung {

  public static void main(String[] args) {

    Bank bank = new Bank();

    // Kunden

    Kunde kunde1 = new Kunde();
    kunde1.name = "Anna Schmidt";
    kunde1.wohnort = new Adresse();
    kunde1.wohnort.straße = "Hauptstraße";
    kunde1.wohnort.hausnummer = "12";
    kunde1.wohnort.plz = "10115";
    kunde1.wohnort.ort = "Berlin";

    Kunde kunde2 = new Kunde();
    kunde2.name = "Bernd Müller";
    kunde2.wohnort = new Adresse();
    kunde2.wohnort.straße = "Gartenweg";
    kunde2.wohnort.hausnummer = "3a";
    kunde2.wohnort.plz = "80331";
    kunde2.wohnort.ort = "München";

    Kunde kunde3 = new Kunde();
    kunde3.name = "Clara Weiß";
    kunde3.wohnort = new Adresse();
    kunde3.wohnort.straße = "Am Markt";
    kunde3.wohnort.hausnummer = "7";
    kunde3.wohnort.plz = "20095";
    kunde3.wohnort.ort = "Hamburg";

    // bank.kundeAnlegen(kunde1);
    // bank.kundeAnlegen(kunde2);
    // bank.kundeAnlegen(kunde3);

    // Konten

    Konto konto1 = new Konto();
    konto1.stand = 1500.0;
    konto1.inhaber = kunde1;

    Konto konto2 = new Konto();
    konto2.stand = 250.0;
    konto2.inhaber = kunde1;

    Konto konto3 = new Konto();
    konto3.stand = 3200.0;
    konto3.inhaber = kunde2;

    // bank.kontoAnlegen(konto1);
    // bank.kontoAnlegen(konto2);
    // bank.kontoAnlegen(konto3);

    // Ausgabe

    for (Kunde kunde : bank.kunden) {
      System.out.println(kunde.nummer + " " + kunde.name + " (" + kunde.wohnort.ort + ")");
      for (Konto konto : bank.konten) {
        if (konto.inhaber == kunde) {
          System.out.println("  " + konto.iban + ": " + konto.stand + " €");
        }
      }
    }

  }

}
