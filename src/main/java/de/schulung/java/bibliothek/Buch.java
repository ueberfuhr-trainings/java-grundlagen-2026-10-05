package de.schulung.java.bibliothek;

import java.util.Objects;

public class Buch {

  private final String isbn;
  private String titel;
  private String autor;

  public Buch(String isbn) {
    this.isbn = isbn;
  }

  public Buch(String isbn, String titel, String autor) {
    this.isbn = isbn;
    this.titel = titel;
    this.autor = autor;
  }

  public String getIsbn() {
    return isbn;
  }

  public String getTitel() {
    return titel;
  }

  public void setTitel(String titel) {
    this.titel = titel;
  }

  public String getAutor() {
    return autor;
  }

  public void setAutor(String autor) {
    this.autor = autor;
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
