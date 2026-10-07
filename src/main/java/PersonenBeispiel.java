public class PersonenBeispiel {


  public static void main(String[] args) {

    Person p1 = new Person("Anna");
    Person p2 = p1;
    System.out.println("1. " + p1.name + " und " + p2.name);

    changeName(p1);
    System.out.println("2. " + p1.name + " und " + p2.name);

    p1 = new Person("Clara");
    System.out.println("3. " + p1.name + " und " + p2.name);

    delete(p2);
    System.out.println("4. " + p1.name + " und " + p2.name);

    p2 = null;

    System.out.println("5. " + p1.name + " und " + p2.name);

  }

  static void changeName(Person person) {
    person.name = "Bernd";
    person = new Person("David");
  }

  static void delete(Person person) {
    person = null;
  }


}
