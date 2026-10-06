public class Bibliotheksanwendung {

  public static void main(String[] args) {

    Buch buch1 = new Buch();
    buch1.isbn = "978-3-86680-100-0";
    buch1.titel = "Java für Einsteiger";
    buch1.autor = "Peter";

    Buch buch2 = new Buch();
    buch2.isbn = "978-3-86680-101-7";
    buch2.titel = "Python für Einsteiger";
    buch2.autor = "Max";

    System.out.println(buch1.autor);
    System.out.println(buch2.titel);

    Exemplar exemplar1 = new Exemplar();
    exemplar1.inventarnummer = "12345";
    exemplar1.buch = buch1;

    Exemplar exemplar2 = new Exemplar();
    exemplar2.inventarnummer = "67890";
    exemplar2.buch = buch2;

    Exemplar exemplar3 = new Exemplar();
    exemplar3.inventarnummer = "54321";
    exemplar3.buch = buch1;

    System.out.println(exemplar1.buch.titel);

    exemplar1.buch.titel = "Java für Fortgeschrittene";
    System.out.println(buch1.titel);

    ändere(exemplar2.buch); // buch2
    System.out.println(buch1.autor); // Peter?
    System.out.println(buch2.autor); // Max?

    buch1 = null;
    exemplar1 = null;
    exemplar3.buch = null;


  }

  static void ändere(Buch buch) {
    buch.autor = "Ralf";
  }

}
