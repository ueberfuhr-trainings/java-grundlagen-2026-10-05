package de.schulung.java.dateisystem;

import java.util.Arrays;

public class Ordner extends DateisystemEintrag {

  private DateisystemEintrag[] einträge = new DateisystemEintrag[0];

  public Ordner(String name) {
    super(name);
  }

  public int getAnzahlEinträge() {
    return einträge.length;
  }

  /**
   * Liefert eine Kopie, damit Einträge nur über hinzufügen() und entfernen()
   * geändert werden können (sonst ließe sich die Zyklusprüfung umgehen).
   */
  public DateisystemEintrag[] getEinträge() {
    return Arrays.copyOf(einträge, einträge.length);
  }

  /**
   * Fügt einen Eintrag hinzu.
   *
   * @return false, wenn der Eintrag fehlt, schon enthalten ist oder einen Zyklus erzeugen würde
   */
  public boolean hinzufügen(DateisystemEintrag eintrag) {
    if (eintrag == null || eintrag == this || enthält(eintrag)) {
      return false;
    }
    // Zyklus: der neue Ordner enthält (rekursiv) bereits diesen Ordner
    if (eintrag instanceof Ordner ordner && ordner.enthält(this)) {
      return false;
    }
    einträge = Arrays.copyOf(einträge, einträge.length + 1);
    einträge[einträge.length - 1] = eintrag;
    return true;
  }

  /**
   * Entfernt einen direkt enthaltenen Eintrag.
   *
   * @return false, wenn der Eintrag nicht direkt in diesem Ordner liegt
   */
  public boolean entfernen(DateisystemEintrag eintrag) {
    for (int i = 0; i < einträge.length; i++) {
      if (einträge[i] == eintrag) {
        // letzten Eintrag an die frei gewordene Stelle setzen und Array verkürzen
        einträge[i] = einträge[einträge.length - 1];
        einträge = Arrays.copyOf(einträge, einträge.length - 1);
        return true;
      }
    }
    return false;
  }

  /**
   * Prüft, ob der Eintrag in diesem Ordner oder einem seiner Unterordner liegt.
   */
  public boolean enthält(DateisystemEintrag eintrag) {
    for (DateisystemEintrag e : einträge) {
      if (e == eintrag) {
        return true;
      }
      if (e instanceof Ordner unterordner && unterordner.enthält(eintrag)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public long getGröße() {
    long summe = 0;
    for (DateisystemEintrag e : einträge) {
      summe += e.getGröße();
    }
    return summe;
  }

}
