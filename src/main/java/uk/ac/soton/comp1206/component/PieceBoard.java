package uk.ac.soton.comp1206.component;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;

/**
 * A PieceBoard is used to display a piece
 */
public class PieceBoard extends GameBoard {

  /**
   * The logger
   */
  protected static final Logger logger = LogManager.getLogger(PieceBoard.class);

  /**
   * Creating a pieceBoard based on the columns, rows, width and height
   *
   * @param cols   number of columns to have
   * @param rows   number of rows to have
   * @param width  the visual width
   * @param height the visual height
   */
  public PieceBoard(int cols, int rows, double width, double height) {
    super(cols, rows, width, height);

    //Add a mouse handler to call clicked when PieceBoard is clicked
    this.setOnMouseClicked(this::clicked);
  }

  /**
   * Setting a specific piece to display
   *
   * @param piece the piece to set
   */
  public void setPiece(GamePiece piece) {

    logger.info("Clearing piece");
    for (int x = 0; x < grid.getCols(); x++) {
      for (int y = 0; y < grid.getRows(); y++) {
        grid.set(x, y, 0);
      }
    }
    grid.playPiece(piece, 1, 1);
  }

  /**
   * Checks if a left click has happened and calls the attached listener
   *
   * @param event mouse event
   */
  public void clicked(MouseEvent event) {
    logger.info("Piece Board has been clicked");
    if (rightClickedListener != null && event.getButton() == MouseButton.PRIMARY) {
      rightClickedListener.blockRightClicked();
    }
  }

  /**
   * Overridden here so the pieceboards don't display a highlight
   *
   * @param x the x coordinate of the aim
   * @param y the y coordinate of the aim
   */
  @Override
  public void aimChange(int x, int y) {
  }

  /**
   * Overridden here so the pieceboards don't display a highlight
   *
   * @param block the block to retrieve the x and y coordinates from
   */
  @Override
  public void aimChange(GameBlock block) {
  }

  /**
   * Overridden here so the pieceboards don't clear a highlight
   *
   * @param block the block to clear the highlight of
   */
  @Override
  public void exitedBlock(GameBlock block) {
  }
}
