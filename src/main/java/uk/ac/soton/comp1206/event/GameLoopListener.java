package uk.ac.soton.comp1206.event;

/**
 * The GameLoopListener listens to when the game has to loop (when the timer runs out) and syncs the
 * UI timer to the game timer
 */
public interface GameLoopListener {

  /**
   * Set the timer to the value given in the game class
   * @param delay the duration of the timer
   */
  void loop(int delay);
}
