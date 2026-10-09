package de.schulung.java.sonstiges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

public class CollectionsDemo {

  public static void main(String[] args) {

    int x = 4;
    Integer o = Integer.valueOf(4);
    int y = o.intValue();

    Integer o2 = x; // Integer.valueOf(x); -> Autoboxing
    int y2 = o2; // o2.intValue(); -> Autounboxing

    o2 = null;
    // y2 = o2; // NullPointerException

    System.out.print("LinkedList: ");
    testeCollection(new LinkedList<>());
    System.out.print("ArrayList: ");
    testeCollection(new ArrayList<>());
    System.out.print("HashSet: ");
    testeCollection(new HashSet<>());
    System.out.print("TreeSet: ");
    testeCollection(new TreeSet<>());

    // new TreeSet().add(new Girokonto(new Kunde()));


  }

  static void testeCollection(Collection<Integer> collection) {
    collection.add(Integer.valueOf(4));
    collection.add(3);
    collection.add(9);
    collection.add(4);
    collection.add(5);
    collection.add(-9);
    // collection.add("Hello World");

    System.out.println(collection);

    for (Integer element : collection) {
      System.out.println(element);
    }
    for (Iterator<Integer> it = collection.iterator(); it.hasNext(); ) {
      System.out.println(it.next());
    }

  }

}
