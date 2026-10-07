import java.util.Objects;

public class Konto {

  String iban;
  long stand; // in Cent, stand >= 0
  Kunde inhaber;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Konto konto = (Konto) o;
    return Objects.equals(iban, konto.iban);
  }

}
