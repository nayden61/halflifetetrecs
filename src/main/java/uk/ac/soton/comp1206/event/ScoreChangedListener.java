package uk.ac.soton.comp1206.event;

/**
 * The ScoreChangedListener listens to when the score of the player has changed and carries out the
 * needed checks relating to the highs-core
 */
public interface ScoreChangedListener {

  /**
   * Carries out checks that compare the current score and the highs-core
   */
  void scoreChanged();
}
