package de.schulung.java.stifte;

public class Druckerpatrone implements Beleerbar {

  private long tintenstand = 100;

  public long getTintenstand() {
    return tintenstand;
  }

  public void setTintenstand(long tintenstand) {
    this.tintenstand = tintenstand;
  }

  @Override
  public void leeren(int bisStand) {
    this.tintenstand = bisStand;
  }


}
