package de.schulung.java.stifte;

public interface Beleerbar {

  default void leeren() {
    leeren(0);
  }

  void leeren(int bisStand);

}
