package de.schulung.java.sonstiges;

public class StringDemo {

  public static void main(String[] args) {

    // String text1 = new String("text");
    // String text2 = new String("text");

    String text1 = "text";
    String text2 = "text";

    System.out.println(text1 == text2);
    System.out.println(text1.equals(text2));

    String userDecision = "yes";
    userDecision = new String("yes");
    System.out.println("user decided: " + isUserDecisionYes(userDecision));

    System.out.println("user decided: " + isUserDecisionYes(null));

  }


  static boolean isUserDecisionYes(String input) {
    // return input == "yes" || input == "y";
    // return input.equals("yes") || input.equals("y");

    // if(input == null) {
    //  return false;
    // } else {
    //  return input.equals("yes") || input.equals("y");
    // }

    // return input != null && (input.equals("yes") || input.equals("y"));

    return "yes".equals(input) || "y".equals(input);

  }

}
