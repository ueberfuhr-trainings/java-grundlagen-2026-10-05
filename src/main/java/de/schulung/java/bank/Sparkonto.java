package de.schulung.java.bank;

public class Sparkonto extends Konto {

  private double habenzins; // in Prozent p. a., stand >= 0

  public Sparkonto(Kunde inhaber) {
    super(inhaber);
  }

  public double getHabenzins() {
    return habenzins;
  }

  public void setHabenzins(double habenzins) {
    this.habenzins = habenzins;
  }
}
