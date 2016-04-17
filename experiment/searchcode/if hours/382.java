package main.creational.factory.video2brain.withFactoryMethod;

import java.util.Date;

/**
 * Class containing the factory method
 */
public class Cantine {

  public static Meal getMeal(){
    Date now = new Date();
    int hours = now.getHours();
    Meal meal;
    if (6 <= hours && hours < 11) {
      meal = new Breakfast();
    } else if (11 <= hours && hours < 16) {
      meal = new Lunch();
    } else if (16 <= hours && hours < 22) {
      meal = new Dinner();
    } else {
      meal = new NoMeal();
    }
    return meal;
  }
}

