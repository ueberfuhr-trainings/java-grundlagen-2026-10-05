package de.schulung.java.bank;

import java.util.Objects;
import java.util.UUID;

public class Kunde {

  private UUID nummer;
  private String name;
  private Adresse wohnort;

  public UUID getNummer() {
    return nummer;
  }

  public void setNummer(UUID nummer) {
    this.nummer = nummer;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Adresse getWohnort() {
    return wohnort;
  }

  public void setWohnort(Adresse wohnort) {
    this.wohnort = wohnort;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Kunde kunde = (Kunde) o;
    return Objects.equals(nummer, kunde.nummer);
  }

}
