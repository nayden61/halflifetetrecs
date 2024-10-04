package uk.ac.soton.comp1206.event;

import java.util.Set;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * The LineClearedListener listens to when a line is cleared and handles what happens
 */
public interface LineClearedListener {

  /**
   * Handles the way a line cleared event is handled
   *
   * @param blocks the set of coordinates of the blocks that will be cleared
   */
  void lineClear(Set<GameBlockCoordinate> blocks);
}
