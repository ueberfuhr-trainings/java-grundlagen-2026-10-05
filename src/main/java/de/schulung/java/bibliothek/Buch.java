package de.schulung.java.bibliothek;

import java.util.Objects;

public class Buch extends Printmedium {

  private final String isbn;
  private String autor;
  private int auflage;

  public Buch(String isbn) {
    this.isbn = isbn;
  }

  public Buch(String isbn, String titel, String autor) {
    this.isbn = isbn;
    setTitel(titel);
    this.autor = autor;
  }

  public Buch(String isbn, String titel, String autor, int erscheinungsjahr, int seitenzahl, int auflage) {
    super(titel, erscheinungsjahr, seitenzahl);
    this.isbn = isbn;
    this.autor = autor;
    this.auflage = auflage;
  }

  public String getIsbn() {
    return isbn;
  }

  public String getAutor() {
    return autor;
  }

  public void setAutor(String autor) {
    this.autor = autor;
  }

  public int getAuflage() {
    return auflage;
  }

  public void setAuflage(int auflage) {
    this.auflage = auflage;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Buch buch = (Buch) o;
    return Objects.equals(isbn, buch.isbn);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(isbn);
  }
}
