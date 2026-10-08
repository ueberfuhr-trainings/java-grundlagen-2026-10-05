package de.schulung.java.dateisystem;

public class Verknüpfung extends DateisystemEintrag {

  private VerknüpfbarerEintrag ziel;

  public Verknüpfung(String name, VerknüpfbarerEintrag ziel) {
    super(name);
    this.ziel = ziel;
  }

  public VerknüpfbarerEintrag getZiel() {
    return ziel;
  }

  public void setZiel(VerknüpfbarerEintrag ziel) {
    this.ziel = ziel;
  }

  @Override
  public long getGröße() {
    return 1;
  }

}
