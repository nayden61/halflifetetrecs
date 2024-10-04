package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The NextPieceListener handles the event when a new piece needs to be displayed on a PieceBoard
 */
public interface NextPieceListener {

  /**
   * Handles a nextPiece event
   *
   * @param nextPiece      the next piece to be played
   * @param followingPiece the piece after the next piece
   */
  void nextPiece(GamePiece nextPiece, GamePiece followingPiece);
}
