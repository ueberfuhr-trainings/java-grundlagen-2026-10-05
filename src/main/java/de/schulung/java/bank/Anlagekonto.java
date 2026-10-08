package de.schulung.java.bank;

// stand >= 0
public abstract class Anlagekonto extends Konto {

  public Anlagekonto(Kunde inhaber) {
    super(inhaber);
  }

  public abstract double getZinssatz(); // in Prozent p. a.

  @Override
  protected boolean isAuszahlenErlaubt(long betrag) {
    return getStand() - betrag >= 0;
  }
}
