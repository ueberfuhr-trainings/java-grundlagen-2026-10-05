import java.util.Objects;
import java.util.UUID;

public class Kunde {

  UUID nummer;
  String name;
  Adresse wohnort;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Kunde kunde = (Kunde) o;
    return Objects.equals(nummer, kunde.nummer);
  }

}
