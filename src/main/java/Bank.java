import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;

public class Bank {

  Kunde[] kunden = new Kunde[0];
  Konto[] konten = new Konto[0];


  public void kundeAnlegen(Kunde kunde) {

    // Prüfen, ob Objekt bereits vorhanden ist
    for (Kunde existing : kunden) {
      if (existing == kunde) {
        System.out.println("Fehler: Kunde ist bereits vorhanden.");
        return;
      }
    }

    // Kundennummer generieren
    kunde.nummer = UUID.randomUUID();
    // TODO: existiert UUID schon im Array?

    // Kunde an Array anhängen
    kunden = Arrays.copyOf(kunden, kunden.length + 1);
    kunden[kunden.length - 1] = kunde;

  }

  //  TODO: Konto anlegen


  // Aufbau: DE + 2 Prüfziffern + 8-stellige BLZ + 10-stellige Kontonummer
  static String randomIban() {
    String blz = "37040044";
    String kontonummer = String.format("%010d", (long) (Math.random() * 10_000_000_000L));
    String bban = blz + kontonummer;
    // Prüfziffern nach ISO 7064 (Modulo 97): "DE00" ans Ende, D=13, E=14
    int prüfziffern = 98 - new BigInteger(bban + "131400").mod(BigInteger.valueOf(97)).intValue();
    return "DE" + String.format("%02d", prüfziffern) + bban;
  }

  // TODO: Konto finden nach IBAN
  // TODO: Konten eines Kunden ermitteln


}
