package de.schulung.java.bibliothek;

import java.util.Objects;

public class Regal {

  private String nummer;

  public Regal(String nummer) {
    this.nummer = nummer;
  }

  public String getNummer() {
    return nummer;
  }

  public void setNummer(String nummer) {
    this.nummer = nummer;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Regal regal = (Regal) o;
    return Objects.equals(nummer, regal.nummer);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(nummer);
  }
}
