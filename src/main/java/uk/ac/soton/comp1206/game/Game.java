package uk.ac.soton.comp1206.game;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameEndListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.event.ScoreChangedListener;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to
 * manipulate the game state and to handle actions made by the player should take place inside this
 * class.
 */
public class Game {

  private static final Logger logger = LogManager.getLogger(Game.class);

  /**
   * The milliseconds that the timer will run for
   */
  Integer timeLeft;

  /**
   * Number of rows
   */
  protected final int rows;

  /**
   * Number of columns
   */
  protected final int cols;

  /**
   * The grid model linked to the game
   */
  protected final Grid grid;

  /**
   * Keep track of the current piece that is selected
   */
  protected GamePiece currentPiece;

  /**
   * Keeps track of the piece that is after the current piece
   */
  protected GamePiece followingPiece;

  /**
   * Random number generator
   */
  private final Random rnd = new Random();

  /**
   * The timer that will keep track of the time that the player has to place a piece
   */
  private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  /**
   * Used to cancel the timers execution
   */
  protected ScheduledFuture<?> future;

  /**
   * Used to get the future of the timer
   * @return the future to use to cancel
   */
  public ScheduledFuture<?> getFuture() {
    return future;
  }

  /**
   * The listener that is called when a new piece is spawned
   */
  protected NextPieceListener nextPieceListener;

  /**
   * The listener that is called when a line is to be cleared
   */
  private LineClearedListener clearedListener;

  /**
   * The listener that is called when the timer is started
   */
  private GameLoopListener gameLoopListener;

  /**
   * The listener that is called when the game ends
   */
  private GameEndListener gameEndListener;

  /**
   * The listener that is called when the score changes
   */
  private ScoreChangedListener scoreChangedListener;

  /**
   * Keeps track of the score
   */
  SimpleIntegerProperty score = new SimpleIntegerProperty(0);

  /**
   * Keeps track of the level
   */
  SimpleIntegerProperty level = new SimpleIntegerProperty(0);

  /**
   * Keeps track of the lives left
   */
  SimpleIntegerProperty lives = new SimpleIntegerProperty(3);

  /**
   * Keeps track of the multiplier
   */
  SimpleIntegerProperty multiplier = new SimpleIntegerProperty(1);

  /**
   * Sets the score
   *
   * @param score the new score
   */
  public void setScore(int score) {
    logger.info("Score is set to {}", score);
    this.score.set(score);
  }

  /**
   * Retrieves the score
   *
   * @return the score
   */
  public int getScore() {
    return score.get();
  }

  public StringBinding scoreProperty() {
    return score.asString();
  }

  /**
   * Sets the level
   *
   * @param level the new level
   */
  public void setLevel(int level) {
    logger.info("Level is set to {}", level);
    this.level.set(level);
  }

  /**
   * Retrieves the level
   *
   * @return the level
   */
  public int getLevel() {
    return level.get();
  }

  public StringBinding levelProperty() {
    return level.asString();
  }

  /**
   * Sets the live(s)
   *
   * @param lives the new live(s)
   */
  public void setLives(int lives) {
    logger.info("Lives are set to {}", lives);
    this.lives.set(lives);
  }

  /**
   * Retrieves the lives
   *
   * @return the lives
   */
  public int getLives() {
    return lives.get();
  }

  public StringBinding livesProperty() {
    return lives.asString();
  }

  /**
   * Sets the multiplier
   *
   * @param multiplier the new multiplier
   */
  public void setMultiplier(int multiplier) {
    logger.info("Multiplier is set to {}", multiplier);
    this.multiplier.set(multiplier);
  }

  /**
   * Retrieves the multiplier
   *
   * @return the multiplier
   */
  public int getMultiplier() {
    return multiplier.get();
  }

  public StringBinding multiplierProperty() {
    return multiplier.asString();
  }

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Game(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    //Create a new grid model to represent the game state
    this.grid = new Grid(cols, rows);
  }

  /**
   * Start the game
   */
  public void start() {
    logger.info("Starting game");
    startTimer();
    initialiseGame();
  }

  /**
   * Initialise a new game and set up anything that needs to be done at the start
   */
  public void initialiseGame() {
    logger.info("Initialising game");

    //Call nextPiece() to get the current piece
    nextPiece();
  }

  /**
   * Handle what should happen when a particular block is clicked
   *
   * @param gameBlock the block that was clicked
   */
  public void blockClicked(GameBlock gameBlock) {

    //Get the position of this block
    int x = gameBlock.getX();
    int y = gameBlock.getY();

    if (grid.canPlayPiece(currentPiece, x, y)) {
      grid.playPiece(currentPiece, x, y);

      //Restart the timer when a block is placed
      restartTimer();

      Multimedia.playAudio("place.wav");

      //Clean lines and spawn another piece
      afterPiece();
      nextPiece();
    }
  }

  /**
   * Handles what happens when a block is clicked and only needs x and y coordinates (mainly for
   * keyboard)
   *
   * @param x the x coordinate of the clicked block
   * @param y the y coordinate of the clicked block
   */
  public void blockClicked(int x, int y) {

    if (grid.canPlayPiece(currentPiece, x, y)) {
      grid.playPiece(currentPiece, x, y);

      //Restart the timer when a block is placed
      restartTimer();

      Multimedia.playAudio("place.wav");

      //Clean lines and spawn another piece
      afterPiece();
      nextPiece();
    }
  }

  /**
   * Sets the listener
   *
   * @param listener listener to set
   */
  public void setNextPieceListener(NextPieceListener listener) {
    this.nextPieceListener = listener;
  }

  /**
   * Sets the listener
   *
   * @param listener listener to set
   */
  public void setClearedListener(LineClearedListener listener) {
    this.clearedListener = listener;
  }

  /**
   * Sets the listener
   *
   * @param listener listener to set
   */
  public void setGameLoopListener(GameLoopListener listener) {
    this.gameLoopListener = listener;
  }

  /**
   * Sets the listener
   *
   * @param listener listener to set
   */
  public void setGameEndListener(GameEndListener listener) {
    this.gameEndListener = listener;
  }

  /**
   * Sets the listener
   *
   * @param listener listener to set
   */
  public void setScoreChangedListener(ScoreChangedListener listener) {
    this.scoreChangedListener = listener;
  }

  /**
   * Get the grid model inside this game representing the game state of the board
   *
   * @return game grid model
   */
  public Grid getGrid() {
    return grid;
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
   * Spawns a random piece
   *
   * @return the random piece
   */
  public GamePiece spawnPiece() {

    logger.info("Spawning new piece");

    var pieceNum = GamePiece.PIECES;
    var pieceId = rnd.nextInt(pieceNum);

    return GamePiece.createPiece(pieceId);
  }

  /**
   * Sets the current piece to the following piece and replaces the following piece
   */
  public void nextPiece() {

    if (followingPiece == null) {
      followingPiece = spawnPiece();
    }

    currentPiece = followingPiece;
    logger.info("Current piece is {}", currentPiece);

    followingPiece = spawnPiece();
    logger.info("The following piece is {}", followingPiece);

    nextPieceListener.nextPiece(currentPiece, followingPiece);
  }

  /**
   * Checks if there are any lines that can be cleared and keeps track of them
   */
  public void afterPiece() {
    logger.info("Checking if any lines can be cleared");
    var linesToClear = 0;
    HashSet<GameBlockCoordinate> blocksToClear = new HashSet<>();

    //Checking if any column lines can be cleared
    for (var x = 0; x < cols; x++) {
      var counter = 0;

      for (var y = 0; y < rows; y++) {
        if (grid.get(x, y) == 0) {
          break;
        }
        counter++;
      }
      if (counter == rows) {
        linesToClear++;

        //Add all the blocks that are on the current column to the HashSet
        for (int coordY = 0; coordY < rows; coordY++) {
          blocksToClear.add(new GameBlockCoordinate(x, coordY));
        }
      }
    }

    //Checking if any row lines can be cleared
    for (var y = 0; y < rows; y++) {
      var counter = 0;

      for (var x = 0; x < cols; x++) {
        if (grid.get(x, y) == 0) {
          break;
        }
        counter++;
      }
      if (counter == cols) {
        linesToClear++;

        //Add all the blocks that are on the current row to the HashSet
        for (int coordX = 0; coordX < cols; coordX++) {
          blocksToClear.add(new GameBlockCoordinate(coordX, y));
        }
      }
    }

    logger.debug("Blocks to clear {}", blocksToClear);
    logger.info("There are {} lines and {} blocks to be cleared", linesToClear,
        blocksToClear.size());

    clearLines(blocksToClear);
    score(linesToClear, blocksToClear.size());
  }

  /**
   * Clears the blocks that have formed a line
   *
   * @param blocksToClear the blocks that are a part of a line(s)
   */
  public void clearLines(HashSet<GameBlockCoordinate> blocksToClear) {
    for (GameBlockCoordinate block : blocksToClear) {
      grid.set(block.getX(), block.getY(), 0);
    }

    //Calls the listener
    clearedListener.lineClear(blocksToClear);
  }

  /**
   * Calculates and sets the score based off of the lines to clear, blocks to clear and multiplier
   *
   * @param lineCount  the number of lines to clear
   * @param blockCount the number of blocks to clear
   */
  public void score(int lineCount, int blockCount) {
    var scoreToAdd = (lineCount * blockCount * 10 * getMultiplier());
    logger.info("Attempting to add score {}", scoreToAdd);
    setScore(scoreToAdd + getScore());
    scoreChangedListener.scoreChanged();
    multiplier(lineCount);
    level();
  }

  /**
   * Changes the multiplier based of the number of lines that are cleared
   *
   * @param lineCount the number of lines to clear
   */
  public void multiplier(int lineCount) {
    logger.info("Attempting to change multiplier");
    if (lineCount > 0) {
      setMultiplier(getMultiplier() + 1);
    } else {
      setMultiplier(1);
    }
  }

  /**
   * Sets the level depending on the score
   */
  public void level() {
    logger.info("Checking if user can level up");
    setLevel(getScore() / 1000);
  }

  /**
   * Will rotate the current piece by the given rotations
   * @param rotations the number of rotations clockwise
   */
  public void rotateCurrentPiece(int rotations) {
    logger.info("Rotating current piece");
    currentPiece.rotate(rotations);
    nextPieceListener.nextPiece(currentPiece, followingPiece);
    Multimedia.playAudio("rotate.wav");
  }

  /**
   * Swaps the current and following pieces
   */
  public void swapCurrentPiece() {
    logger.info("Swapping current and following piece");
    var tempPiece = currentPiece;

    currentPiece = followingPiece;
    followingPiece = tempPiece;

    nextPieceListener.nextPiece(currentPiece, followingPiece);
    Multimedia.playAudio("swap.wav");
  }

  /**
   * Gets the amount of time the player has to play a piece
   *
   * @return the time in milliseconds
   */
  public int getTimerDelay() {
    if (getLevel() <= 19) {
      return 12000 - 500 * getLevel();
    } else {
      return 2500;
    }
  }

  /**
   * Starts the game timer
   */
  public void startTimer() {

    //Set the new delay each time the timer is started
    timeLeft = getTimerDelay();

    gameLoopListener.loop(timeLeft);

    //Set the pool for the executor
    executor = Executors.newScheduledThreadPool(1);

    //Use the future to later cancel the execution
    future = executor.scheduleAtFixedRate(() -> {

      //Deduct half a second every half a second
      timeLeft -= 500;

      logger.info("Timer started {}", timeLeft);

      //If the time has run out, shutdown the executor and call gameLoop()
      if (timeLeft.equals(0)) {
        logger.info("Ran out of time!");
        executor.shutdown();
        gameLoop();
      }
    }, 0, 500, TimeUnit.MILLISECONDS);
  }

  /**
   * Restarts the timer by cancelling its execution and starting it again
   */
  public void restartTimer() {
    logger.info("Timer is reset");
    future.cancel(false);
    startTimer();
  }

  /**
   * Handles the way the game acts when a piece has not been played for the given time duration
   */
  public void gameLoop() {

    if (getLives() == 0) {
      gameEndListener.endGame(this);
      return;
    }

    //Lives reduced by one
    Platform.runLater(() -> setLives(getLives() - 1));

    //Multiplier reset to 1
    Platform.runLater(() -> setMultiplier(1));

    //Discard current piece
    nextPiece();

    //Restart timer
    startTimer();
  }
}
