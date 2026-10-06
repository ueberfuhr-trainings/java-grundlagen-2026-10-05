public class WetterStation {

  public static void main(String[] args) {
    int[] woche = {17, 24, 18, 22, 25, 18, 21};
    int[] keineMessung = {};

    System.out.println("Mittelwert:   " + mittelwert(17, 24));
    System.out.println("Durchschnitt: " + durchschnitt(woche));
    System.out.println("Durchschnitt: " + durchschnitt(keineMessung));
    System.out.println("Durchschnitt: " + durchschnitt(keineMessung, -99.0));

    var wert = durchschnitt(keineMessung);
    System.out.println(wert * 2);

    System.out.println(0 / 0);

  }

  static double mittelwert(int a, long b) {
    return (double) (a + b) / 2;
  }

  static double durchschnitt(int[] messwerte) {
    return durchschnitt(messwerte, 0.0);
  }

  // Gleicher Name, andere Parameterliste – das ist Überladen.
  // Die Berechnung wird nicht kopiert, sondern aufgerufen.
  static double durchschnitt(int[] messwerte, double standardwert) {
    if (messwerte.length == 0) {
      return standardwert;
    }

    int summe = 0;
    for (int wert : messwerte) {
      summe += wert;
    }

    return (double) summe / messwerte.length;

  }
}

// Ausgabe: Mittelwert:   20.5
//          Durchschnitt: 20.857142857142858
//          Durchschnitt: 0.0
//          Durchschnitt: -99.0
