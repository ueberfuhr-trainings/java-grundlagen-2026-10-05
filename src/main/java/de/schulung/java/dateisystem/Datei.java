package de.schulung.java.dateisystem;

public class Datei extends VerknüpfbarerEintrag {

  private String endung;
  private byte[] inhalt;

  public Datei(String name, String endung, byte[] inhalt) {
    super(name);
    this.endung = endung;
    this.inhalt = inhalt;
  }

  public String getEndung() {
    return endung;
  }

  public void setEndung(String endung) {
    this.endung = endung;
  }

  public byte[] getInhalt() {
    return inhalt;
  }

  public void setInhalt(byte[] inhalt) {
    this.inhalt = inhalt;
  }

  @Override
  public long getGröße() {
    return inhalt.length;
  }

}
