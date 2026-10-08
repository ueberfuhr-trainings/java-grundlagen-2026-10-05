package de.schulung.java.dateisystem;

import java.nio.file.Path;

public abstract class DateisystemEintrag {

  private String name;
  private Ordner parent;

  public DateisystemEintrag(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Ordner getParent() {
    return parent;
  }

  // nur Ordner.hinzufügen() und Ordner.entfernen() setzen den Parent
  void setParent(Ordner parent) {
    this.parent = parent;
  }

  /**
   * Liefert den Pfad vom obersten Ordner bis zu diesem Eintrag.
   * Die einzelnen Bestandteile liefern getNameCount() und getName(index).
   */
  public Path getPath() {
    if (parent == null) {
      return Path.of(name);
    }
    return parent.getPath().resolve(name);
  }

  public abstract long getGröße();

}
