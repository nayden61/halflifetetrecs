package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer
 * values arranged in a 2D arrow, with rows and columns.
 * <p>
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display
 * of the contents of the grid.
 * <p>
 * The Grid contains functions related to modifying the model, for example, placing a piece inside
 * the grid.
 * <p>
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

  private static final Logger logger = LogManager.getLogger(Game.class);

  /**
   * The number of columns in this grid
   */
  private final int cols;

  /**
   * The number of rows in this grid
   */
  private final int rows;

  /**
   * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
   */
  private final SimpleIntegerProperty[][] grid;

  /**
   * Create a new Grid with the specified number of columns and rows and initialise them
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Grid(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    //Create the grid itself
    grid = new SimpleIntegerProperty[cols][rows];

    //Add a SimpleIntegerProperty to every block in the grid
    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        grid[x][y] = new SimpleIntegerProperty(0);
      }
    }
  }

  /**
   * Get the Integer property contained inside the grid at a given row and column index. Can be used
   * for binding.
   *
   * @param x column
   * @param y row
   * @return the IntegerProperty at the given x and y in this grid
   */
  public IntegerProperty getGridProperty(int x, int y) {
    return grid[x][y];
  }

  /**
   * Update the value at the given x and y index within the grid
   *
   * @param x     column
   * @param y     row
   * @param value the new value
   */
  public void set(int x, int y, int value) {
    grid[x][y].set(value);
  }

  /**
   * Get the value represented at the given x and y index within the grid
   *
   * @param x column
   * @param y row
   * @return the value
   */
  public int get(int x, int y) {
    try {
      //Get the value held in the property at the x and y index provided
      return grid[x][y].get();
    } catch (ArrayIndexOutOfBoundsException e) {
      //No such index
      return -1;
    }
  }

  /**
   * Get the number of columns in this game
   *
   * @return number of columns
   */
  public int getCols() {
    return cols;
  }

  /**
   * Get the number of rows in this game
   *
   * @return number of rows
   */
  public int getRows() {
    return rows;
  }

  /**
   * Check if a piece can be placed at the given coordinates x and y
   *
   * @param piece the piece to be placed
   * @param posX  the x coordinate to place at
   * @param posY  the y coordinate to place at
   * @return if the piece can be played or not
   */
  public boolean canPlayPiece(GamePiece piece, int posX, int posY) {

    logger.info("Checking if piece {} can be played at the position {} {}", piece, posX, posY);
    int[][] layout = piece.getBlocks();

    var topX = posX - 1;
    var topY = posY - 1;

    for (var blockX = 0; blockX < layout.length; blockX++) {
      for (var blockY = 0; blockY < layout.length; blockY++) {

        var blockValue = layout[blockX][blockY];
        if (blockValue > 0) {

          var gridValue = get(blockX + topX, blockY + topY);
          if (gridValue != 0) {
            logger.info("Unable to play piece {} at {} {}", piece, posX, posY);
            Multimedia.playAudio("cannotplace.wav");
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * Places a piece on the grid if possible, using the x and y coordinates
   *
   * @param piece the piece to be placed
   * @param posX  the x coordinate to place at
   * @param posY  the y coordinate to place at
   */
  public void playPiece(GamePiece piece, int posX, int posY) {

    logger.info("Playing piece {} at position {} {}", piece, posX, posY);

    var colour = piece.getValue();
    int[][] layout = piece.getBlocks();

    var topX = posX - 1;
    var topY = posY - 1;

    for (var blockX = 0; blockX < layout.length; blockX++) {
      for (var blockY = 0; blockY < layout.length; blockY++) {

        var blockValue = layout[blockX][blockY];
        if (blockValue > 0) {
          set(blockX + topX, blockY + topY, colour);
        }
      }
    }
  }
}
