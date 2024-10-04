package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 * <p>
 * Extends Canvas and is responsible for drawing itself.
 * <p>
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 * <p>
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

  private static final Logger logger = LogManager.getLogger(GameBlock.class);

  /**
   * The set of colours for different pieces
   */
  public static final Color[] COLOURS = {
      Color.TRANSPARENT,
      Color.DEEPPINK,
      Color.RED,
      Color.ORANGE,
      Color.YELLOW,
      Color.YELLOWGREEN,
      Color.LIME,
      Color.GREEN,
      Color.DARKGREEN,
      Color.DARKTURQUOISE,
      Color.DEEPSKYBLUE,
      Color.AQUA,
      Color.AQUAMARINE,
      Color.BLUE,
      Color.MEDIUMPURPLE,
      Color.PURPLE
  };

  private final GameBoard gameBoard;
  private final double width;
  private final double height;

  /**
   * The column this block exists as in the grid
   */
  private final int x;

  /**
   * The row this block exists as in the grid
   */
  private final int y;

  /**
   * The value of this block (0 = empty, otherwise specifies the colour to render as)
   */
  private final IntegerProperty value = new SimpleIntegerProperty(0);

  /**
   * Get the column of this block
   *
   * @return column number
   */
  public int getX() {
    return x;
  }

  /**
   * Get the row of this block
   *
   * @return row number
   */
  public int getY() {
    return y;
  }

  /**
   * Get the current value held by this block, representing it's colour
   *
   * @return value
   */
  public int getValue() {
    return this.value.get();
  }

  /**
   * Bind the value of this block to another property. Used to link the visual block to a
   * corresponding block in the Grid.
   *
   * @param input property to bind the value to
   */
  public void bind(ObservableValue<? extends Number> input) {
    value.bind(input);
  }

  /**
   * A more descriptive toString method for when data is needed about a block
   *
   * @return returns the coordinates and value of the block
   */
  @Override
  public String toString() {
    return "GameBlock{" +
        "x=" + x +
        ", y=" + y +
        ", value=" + value.get() +
        '}';
  }

  /**
   * Create a new single Game Block
   *
   * @param gameBoard the board this block belongs to
   * @param x         the column the block exists in
   * @param y         the row the block exists in
   * @param width     the width of the canvas to render
   * @param height    the height of the canvas to render
   */
  public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
    this.gameBoard = gameBoard;
    this.width = width;
    this.height = height;
    this.x = x;
    this.y = y;

    //A canvas needs a fixed width and height
    setWidth(width);
    setHeight(height);

    //Do an initial paint
    paint();

    //When the value property is updated, call the internal updateValue method
    value.addListener(this::updateValue);
  }

  /**
   * When the value of this block is updated,
   *
   * @param observable what was updated
   * @param oldValue   the old value
   * @param newValue   the new value
   */
  private void updateValue(ObservableValue<? extends Number> observable, Number oldValue,
      Number newValue) {
    paint();
  }

  /**
   * Handle painting of the block canvas
   */
  public void paint() {
    //If the block is empty, paint as empty
    if (value.get() == 0) {
      paintEmpty();
    } else {
      //If the block is not empty, paint with the colour represented by the value
      paintColor(COLOURS[value.get()]);
    }
  }

  /**
   * Paint this canvas empty
   */
  private void paintEmpty() {
    var gc = getGraphicsContext2D();

    //Clear
    gc.clearRect(0, 0, width, height);

    //Fill
    gc.setFill(Color.BLACK);
    gc.setGlobalAlpha(0.6);
    gc.fillRect(0, 0, width, height);

    //Border
    gc.setStroke(Color.DARKORANGE);
    gc.strokeRect(0, 0, width, height);
  }

  /**
   * Paint this canvas with the given colour
   *
   * @param colour the colour to paint
   */
  private void paintColor(Paint colour) {
    var gc = getGraphicsContext2D();

    String path = null;
    //Clear
    gc.clearRect(0, 0, width, height);

    //Image fill
    switch (colour.toString()) {
      case "0xff1493ff":
        path = getClass().getResource("/images/42.png").toExternalForm();
        break;
      case "0xff0000ff":
        path = getClass().getResource("/images/43.png").toExternalForm();
        break;
      case "0xffa500ff":
        path = getClass().getResource("/images/44.png").toExternalForm();
        break;
      case "0xffff00ff" :
        path = getClass().getResource("/images/45.png").toExternalForm();
        break;
      case "0x9acd32ff":
        path = getClass().getResource("/images/46.png").toExternalForm();
        break;
      case "0x00ff00ff":
        path = getClass().getResource("/images/47.png").toExternalForm();
        break;
      case "0x008000ff":
        path = getClass().getResource("/images/48.png").toExternalForm();
        break;
      case "0x006400ff":
        path = getClass().getResource("/images/49.png").toExternalForm();
        break;
      case "0x00ced1ff":
        path = getClass().getResource("/images/50.png").toExternalForm();
        break;
      case "0x00bfffff":
        path = getClass().getResource("/images/51.png").toExternalForm();
        break;
      case "0x00ffffff":
        path = getClass().getResource("/images/52.png").toExternalForm();
        break;
      case "0x7fffd4ff":
        path = getClass().getResource("/images/53.png").toExternalForm();
        break;
      case "0x0000ffff":
        path = getClass().getResource("/images/54.png").toExternalForm();
        break;
      case "0x9370dbff":
        path = getClass().getResource("/images/55.png").toExternalForm();
        break;
      case "0x800080ff":
        path = getClass().getResource("/images/56.png").toExternalForm();
        break;
    }

    var image = new Image(path);
    gc.setFill(new ImagePattern(image));

    //gc.setFill(colour);
    gc.setGlobalAlpha(1);
    gc.fillRect(0, 0, width, height);

    //Border
    gc.setStroke(Color.DARKORANGE);
    gc.strokeRect(0, 0, width, height);
  }

  /**
   * Draws a circle on the currently selected block (used in the current piece, piece board)
   */
  public void drawIndicator() {
    logger.info("Drawing indicator at {}", toString());

    var gc = getGraphicsContext2D();
    gc.setFill(Color.ORANGE);

    gc.fillOval(4, 4, 40, 40);
  }

  /**
   * Draws a transparent rectangle on the currently selected block
   */
  public void drawHighlight() {
    logger.info("Drawing highlight at {}", toString());

    var gc = getGraphicsContext2D();
    gc.setFill(Color.ORANGE);
    gc.setGlobalAlpha(0.3);
    gc.fillRect(0, 0, width, height);
  }

  /**
   * Clears the current block and repaints it without the transparent rectangle highlight
   */
  public void cleanHighlight() {
    logger.info("Cleaning highlight at {}", toString());

    var gc = getGraphicsContext2D();

    //Clears the whole block
    gc.clearRect(0, 0, width, height);

    //Then repaints it without the highlight
    paint();
  }

  /**
   * Plays an animation that fades out the currently selected block
   */
  public void fadeOut() {
    logger.info("Fading out {}", toString());

    var gc = getGraphicsContext2D();

    var timer = new AnimationTimer() {
      private double transparency = 1;

      @Override
      public void handle(long l) {

        //Starts off by painting the block empty
        paintEmpty();

        gc.setFill(Color.GREEN);
        gc.setGlobalAlpha(transparency);

        //Then fills it with green
        gc.fillRect(0, 0, width, height);

        //Which gets more transparent as time passes
        transparency = transparency - 0.1;

        //Stop the animation when the block is invisible
        if (transparency <= -0.1) {
          stop();
        }
      }
    };
    timer.start();
  }
}
