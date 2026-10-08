package de.schulung.java.bank;

public class Girokonto extends Konto {

  private long dispolimit; // in Cent, stand >= -dispolimit

  public Girokonto(Kunde inhaber) {
    super(inhaber);
  }

  public long getDispolimit() {
    return dispolimit;
  }

  public void setDispolimit(long dispolimit) {
    if (getStand() < -dispolimit) {
      System.out.println("Fehler: Kontostand liegt unter dem neuen Dispolimit. Änderung nicht möglich.");
      return;
    }
    this.dispolimit = dispolimit;
  }

  @Override
  protected boolean isAuszahlenErlaubt(long betrag) {
    return getStand() - betrag >= -dispolimit;
  }
}
