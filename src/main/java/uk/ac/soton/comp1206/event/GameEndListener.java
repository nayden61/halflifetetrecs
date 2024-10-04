package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.Game;

/**
 * The GameEndListener listens to when the game has ended and handles what happens afterwards
 */
public interface GameEndListener {

  /**
   * Carries out the needed tasks when the game finishes
   *
   * @param game the game reference (mainly for the score)
   */
  void endGame(Game game);
}
