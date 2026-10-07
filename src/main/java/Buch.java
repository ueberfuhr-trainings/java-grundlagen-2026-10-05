import java.util.Objects;

public class Buch {

  String isbn;
  String titel;
  String autor;

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
