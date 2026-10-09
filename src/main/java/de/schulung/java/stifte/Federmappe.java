package de.schulung.java.stifte;

import java.util.ArrayList;
import java.util.List;

public class Federmappe {

  private final List<Stift> stifte = new ArrayList<>();

  public void addStift(Stift stift) {
    this.stifte.add(stift);
  }

  public void alleSchreiben() {
    for (Stift stift : this.stifte) {
      stift.schreiben();
    }
  }

  public void alleAuffüllen() {
    for (Stift stift : this.stifte) {
      stift.auffüllen();
    }
  }


}
