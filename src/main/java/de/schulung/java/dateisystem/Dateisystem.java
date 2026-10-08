package de.schulung.java.dateisystem;

public class Dateisystem {

  public static final Dateisystem INSTANCE = new Dateisystem();

  private final Ordner wurzel = new Ordner("/");

  private Dateisystem() {
  }

  public Ordner getWurzel() {
    return wurzel;
  }

}
