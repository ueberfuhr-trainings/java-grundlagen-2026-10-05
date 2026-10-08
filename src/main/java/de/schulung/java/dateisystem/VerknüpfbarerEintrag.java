package de.schulung.java.dateisystem;

// Ziel einer Verknüpfung – nur Datei und Ordner erben davon, Verknüpfung nicht
public abstract class VerknüpfbarerEintrag extends DateisystemEintrag {

  public VerknüpfbarerEintrag(String name) {
    super(name);
  }

}
