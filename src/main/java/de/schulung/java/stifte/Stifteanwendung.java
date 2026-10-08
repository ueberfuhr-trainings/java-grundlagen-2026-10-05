package de.schulung.java.stifte;

public class Stifteanwendung {

  public static void main(String[] args) {

    Federmappe federmappe = new Federmappe();

    Kuli kuli = new Kuli("blau", 100, new Mine());
    federmappe.addStift(kuli);

    Textmarker textmarker = new Textmarker("gelb", 100, 5.5);
    federmappe.addStift(textmarker);

    Stift stift = new Textmarker("rot", 100, 5.5);
    federmappe.addStift(stift);

    // geht nicht, weil Stift eine abstrakte Klasse ist
    // Stift stift2 = new Stift("grün", 100);
    // federmappe.addStift(stift2);

    federmappe.alleSchreiben();

    Object o = new Object();

  }

}
