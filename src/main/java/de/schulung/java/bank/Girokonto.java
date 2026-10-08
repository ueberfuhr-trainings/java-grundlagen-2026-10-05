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
    this.dispolimit = dispolimit;
  }
}
