package de.schulung.java.bibliothek;

import java.time.Duration;
import java.util.Objects;

// Hörbücher gibt es nur als virtuelle Exemplare (Lizenzen), die heruntergeladen werden
public class Hörbuch extends Medium {

  private final String isbn;
  private Duration spieldauer;
  private long dateigröße; // in Bytes

  public Hörbuch(String isbn) {
    this.isbn = isbn;
  }

  public Hörbuch(String isbn, String titel, int erscheinungsjahr, Duration spieldauer, long dateigröße) {
    super(titel, erscheinungsjahr);
    this.isbn = isbn;
    this.spieldauer = spieldauer;
    this.dateigröße = dateigröße;
  }

  public String getIsbn() {
    return isbn;
  }

  public Duration getSpieldauer() {
    return spieldauer;
  }

  public void setSpieldauer(Duration spieldauer) {
    this.spieldauer = spieldauer;
  }

  public long getDateigröße() {
    return dateigröße;
  }

  public void setDateigröße(long dateigröße) {
    this.dateigröße = dateigröße;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Hörbuch hörbuch = (Hörbuch) o;
    return Objects.equals(isbn, hörbuch.isbn);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(isbn);
  }
}
