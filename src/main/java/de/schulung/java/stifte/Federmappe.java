package de.schulung.java.stifte;

import java.util.Arrays;

public class Federmappe {

  private Stift[] stifte = new Stift[0];

  public void addStift(Stift stift) {
    this.stifte = Arrays.copyOf(this.stifte, this.stifte.length + 1);
    this.stifte[this.stifte.length - 1] = stift;
  }

}
