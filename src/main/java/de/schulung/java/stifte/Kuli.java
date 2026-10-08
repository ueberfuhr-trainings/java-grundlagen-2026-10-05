package de.schulung.java.stifte;

public class Kuli extends Stift {

  private Mine mine;

  public Kuli(String farbe, int füllstand, Mine mine) {
    super(farbe, füllstand);
    this.mine = mine;
  }

  public void schreiben() {
    System.out.println("Ich bin ein Kuli!");
  }

  public Mine getMine() {
    return mine;
  }

  public void setMine(Mine mine) {
    this.mine = mine;
  }

  @Override
  public void auffüllen() {
    setMine(new Mine());
  }
}
