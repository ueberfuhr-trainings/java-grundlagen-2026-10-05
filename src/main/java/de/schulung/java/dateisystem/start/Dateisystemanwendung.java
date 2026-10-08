package de.schulung.java.dateisystem.start;

import de.schulung.java.dateisystem.Datei;
import de.schulung.java.dateisystem.Dateisystem;
import de.schulung.java.dateisystem.DateisystemEintrag;
import de.schulung.java.dateisystem.Ordner;

public class Dateisystemanwendung {

  public static void main(String[] args) {

    Dateisystem dateisystem = Dateisystem.INSTANCE;
    Ordner wurzel = dateisystem.getWurzel();

    // Ordner

    Ordner home = new Ordner("home");
    Ordner anna = new Ordner("anna");
    Ordner dokumente = new Ordner("Dokumente");
    Ordner bilder = new Ordner("Bilder");
    Ordner urlaub = new Ordner("Urlaub");
    Ordner programme = new Ordner("programme");

    wurzel.hinzufügen(home);
    wurzel.hinzufügen(programme);
    home.hinzufügen(anna);
    anna.hinzufügen(dokumente);
    anna.hinzufügen(bilder);
    bilder.hinzufügen(urlaub);

    // Dateien

    Datei lebenslauf = new Datei("lebenslauf", "txt", "Anna Schmidt, geboren 1990 in Berlin".getBytes());
    Datei einkaufsliste = new Datei("einkaufsliste", "txt", "Milch, Brot, Äpfel".getBytes());
    Datei profilbild = new Datei("profilbild", "png", new byte[2048]);
    Datei strand = new Datei("strand", "jpg", new byte[4096]);
    Datei berge = new Datei("berge", "jpg", new byte[8192]);
    Datei editor = new Datei("editor", "exe", new byte[16384]);

    dokumente.hinzufügen(lebenslauf);
    dokumente.hinzufügen(einkaufsliste);
    bilder.hinzufügen(profilbild);
    urlaub.hinzufügen(strand);
    urlaub.hinzufügen(berge);
    programme.hinzufügen(editor);

    ausgeben(wurzel, "");

    // Zyklen werden verhindert

    System.out.println();
    System.out.println("Urlaub in sich selbst: " + urlaub.hinzufügen(urlaub));   // false
    System.out.println("anna in Urlaub: " + urlaub.hinzufügen(anna));            // false
    System.out.println("Wurzel in Bilder: " + bilder.hinzufügen(wurzel));        // false
    System.out.println("strand doppelt: " + bilder.hinzufügen(strand));          // false

    // Entfernen

    System.out.println();
    System.out.println("programme entfernen: " + wurzel.entfernen(programme));  // true
    System.out.println("programme erneut entfernen: " + wurzel.entfernen(programme)); // false
    System.out.println("Größe /: " + wurzel.getGröße() + " Bytes");

  }

  // gibt den Ordner mit allen Unterelementen als Baum aus
  private static void ausgeben(DateisystemEintrag eintrag, String einrückung) {
    System.out.println(einrückung + eintrag.getName() + " (" + eintrag.getGröße() + " Bytes)");
    if (eintrag instanceof Ordner) {
      Ordner ordner = (Ordner) eintrag;
      for (DateisystemEintrag unterelement : ordner.getEinträge()) {
        ausgeben(unterelement, einrückung + "  ");
      }
    }
  }

}
