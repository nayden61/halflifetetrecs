package uk.ac.soton.comp1206.component;

import java.util.Set;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.RightClickedListener;
import uk.ac.soton.comp1206.game.Grid;

/**
 * A GameBoard is a visual component to represent the visual GameBoard. It extends a GridPane to
 * hold a grid of GameBlocks.
 * <p>
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming
 * block. It also be linked to an external grid, for the main game board.
 * <p>
 * The GameBoard is only a visual representation and should not contain game logic or model logic in
 * it, which should take place in the Grid.
 */
public class GameBoard extends GridPane {

  /**
   * Logger
   */
  protected static final Logger logger = LogManager.getLogger(GameBoard.class);

  /**
   * Number of columns in the board
   */
  private final int cols;

  /**
   * Number of rows in the board
   */
  private final int rows;

  /**
   * The visual width of the board - has to be specified due to being a Canvas
   */
  private final double width;

  /**
   * The visual height of the board - has to be specified due to being a Canvas
   */
  private final double height;

  /**
   * The x coordinate of the aim
   */
  private int aimX = 2;

  /**
   * The y coordinate of the aim
   */
  private int aimY = 2;

  /**
   * The grid this GameBoard represents
   */
  final Grid grid;

  /**
   * The blocks inside the grid
   */
  GameBlock[][] blocks;

  /**
   * The listener to call when a specific block is clicked
   */
  private BlockClickedListener blockClickedListener;

  /**
   * The listener to call when a right click has been performed
   */
  protected RightClickedListener rightClickedListener;

  /**
   * Create a new GameBoard, based off a given grid, with a visual width and height.
   *
   * @param grid   linked grid
   * @param width  the visual width
   * @param height the visual height
   */
  public GameBoard(Grid grid, double width, double height) {
    this.cols = grid.getCols();
    this.rows = grid.getRows();
    this.width = width;
    this.height = height;
    this.grid = grid;

    //Build the GameBoard
    build();
  }

  /**
   * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows,
   * along with the visual width and height.
   *
   * @param cols   number of columns for internal grid
   * @param rows   number of rows for internal grid
   * @param width  the visual width
   * @param height the visual height
   */
  public GameBoard(int cols, int rows, double width, double height) {
    this.cols = cols;
    this.rows = rows;
    this.width = width;
    this.height = height;
    this.grid = new Grid(cols, rows);

    //Build the GameBoard
    build();
  }

  /**
   * Return the x coordinate of the aim
   *
   * @return x coordinate
   */
  public int getAimX() {
    return aimX;
  }

  /**
   * Return the y coordinate of the aim
   *
   * @return y coordinate
   */
  public int getAimY() {
    return aimY;
  }

  /**
   * Sets the x coordinate of the aim
   *
   * @param aimX the new x coordinate to set
   */
  public void setAimX(int aimX) {
    this.aimX = aimX;
  }

  /**
   * Sets the y coordinate of the aim
   *
   * @param aimY the new y coordinate to set
   */
  public void setAimY(int aimY) {
    this.aimY = aimY;
  }

  /**
   * Get a specific block from the GameBoard, specified by it's row and column
   *
   * @param x column
   * @param y row
   * @return game block at the given column and row
   */
  public GameBlock getBlock(int x, int y) {
    return blocks[x][y];
  }

  /**
   * Build the GameBoard by creating a block at every x and y column and row
   */
  protected void build() {
    logger.info("Building grid: {} x {}", cols, rows);

    setMaxWidth(width);
    setMaxHeight(height);

    setGridLinesVisible(true);

    blocks = new GameBlock[cols][rows];

    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        createBlock(x, y);
      }
    }

    //Add a mouse click handler to the GameBoard to call the rightClicked method
    this.setOnMouseClicked(this::rightClicked);
  }

  /**
   * Create a block at the given x and y position in the GameBoard
   *
   * @param x column
   * @param y row
   */
  protected GameBlock createBlock(int x, int y) {
    var blockWidth = width / cols;
    var blockHeight = height / rows;

    //Create a new GameBlock UI component
    GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

    //Add to the GridPane
    add(block, x, y);

    //Add to our block directory
    blocks[x][y] = block;

    //Link the GameBlock component to the corresponding value in the Grid
    block.bind(grid.getGridProperty(x, y));

    //Add a mouse click handler to the block to trigger GameBoard blockClicked method
    block.setOnMouseClicked((e) -> blockClicked(e, block));

    //Add a mouse entered handler to trigger the aimchange method
    block.setOnMouseEntered((e) -> aimChange(block));

    //Add a mouse exited handler to trigger the exited block method
    block.setOnMouseExited((e) -> exitedBlock(block));

    return block;
  }

  /**
   * Manages the x and y coordinates of the aim (keyboard)
   *
   * @param x the x coordinate of the aim
   * @param y the y coordinate of the aim
   */
  public void aimChange(int x, int y) {

    //To clean the previous block that was selected as the keyboard does not have a visual way of changing the aim
    getBlock(aimX, aimY).cleanHighlight();

    if (getAimX() + x >= 0 && getAimX() + x < cols) {
      setAimX(getAimX() + x);
    }

    if (getAimY() + y >= 0 && getAimY() + y < rows) {
      setAimY(getAimY() + y);
    }

    logger.info("Aim is x:{} y:{}", aimX, aimY);

    //Draw a highlight on the block with the given coordinates
    getBlock(aimX, aimY).drawHighlight();
  }

  /**
   * Manages the x and y coordinates of the aim (mouse)
   *
   * @param block the block to retrieve the x and y coordinates from
   */
  public void aimChange(GameBlock block) {

    //To clean the last block that was aimed at so when the keyboard is used it does not create multiple highlights
    getBlock(aimX, aimY).cleanHighlight();

    setAimX(block.getX());
    setAimY(block.getY());

    logger.info("Aim is x:{} y:{}", aimX, aimY);

    block.drawHighlight();
  }

  /**
   * Cleans the highlight of a previously selected block
   *
   * @param block the block to clear the highlight of
   */
  public void exitedBlock(GameBlock block) {
    block.cleanHighlight();
  }

  /**
   * Calls the fadeOut method of all the given blocks
   *
   * @param blocks the coordinates of the blocks to be faded out
   */
  public void fadeOut(Set<GameBlockCoordinate> blocks) {
    for (GameBlockCoordinate block : blocks) {
      getBlock(block.getX(), block.getY()).fadeOut();
    }
  }

  /**
   * Set the listener to handle an event when a block is clicked
   *
   * @param listener listener to add
   */
  public void setOnBlockClick(BlockClickedListener listener) {
    this.blockClickedListener = listener;
  }

  /**
   * Triggered when a block is clicked. Call the attached listener.
   *
   * @param event mouse event
   * @param block block clicked on
   */
  private void blockClicked(MouseEvent event, GameBlock block) {
    logger.info("Block clicked: {}", block);

    if (blockClickedListener != null && event.getButton() == MouseButton.PRIMARY) {
      blockClickedListener.blockClicked(block);
    }
  }

  /**
   * Set the listener that will handle when the board is right-clicked
   *
   * @param listener listener to add
   */
  public void setOnRightClick(RightClickedListener listener) {
    this.rightClickedListener = listener;
  }

  /**
   * Checks if a right click has happened and calls the rightClicked listener
   *
   * @param event mouse event
   */
  private void rightClicked(MouseEvent event) {
    if (rightClickedListener != null && event.getButton() == MouseButton.SECONDARY) {
      logger.info("Right clicked");
      rightClickedListener.blockRightClicked();
    }
  }
}
