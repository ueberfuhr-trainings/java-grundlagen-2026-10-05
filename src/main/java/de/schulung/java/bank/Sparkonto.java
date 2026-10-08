package de.schulung.java.bank;

public class Sparkonto extends Anlagekonto {

  private double habenzins; // in Prozent p. a.

  public Sparkonto(Kunde inhaber) {
    super(inhaber);
  }

  public double getHabenzins() {
    return habenzins;
  }

  public void setHabenzins(double habenzins) {
    this.habenzins = habenzins;
  }

  @Override
  public double getZinssatz() {
    return habenzins;
  }
}
