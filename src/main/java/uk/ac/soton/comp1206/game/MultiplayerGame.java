package uk.ac.soton.comp1206.game;

import java.util.LinkedList;
import java.util.Queue;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.MultiplayerGameEndListener;
import uk.ac.soton.comp1206.event.ReceiveMessageListener;
import uk.ac.soton.comp1206.network.Communicator;

/**
 * The MultiplayerGame class handles the main logic of the multiplayer part of the game. It extends
 * Game so the gameplay is similar with some changes.
 */
public class MultiplayerGame extends Game {

  private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

  /**
   * The communicator used to communicate with the server
   */
  private final Communicator communicator;

  /**
   * The queue that is used to store the game pieces that are received from the server
   */
  private Queue<String> pieceQueue;

  /**
   * The listener that is called when the game ends
   */
  private MultiplayerGameEndListener multiplayerGameEndListener;

  /**
   * The listener that is called when a chat message is received from the server
   */
  private ReceiveMessageListener receiveMessageListener;

  /**
   * The property that stores the scores of the players
   */
  public final SimpleListProperty<String[]> scoresProperty = new SimpleListProperty<>(
      FXCollections.observableArrayList());

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   * @param communicator passes the instance of the communicator
   */
  public MultiplayerGame(int cols, int rows, Communicator communicator) {
    super(cols, rows);
    this.communicator = communicator;
  }

  /**
   * Initialise a new game and set up anything that needs to be done at the start
   */
  @Override
  public void initialiseGame() {
    logger.info("Initialising game");

    pieceQueue = new LinkedList<>();

    getScores();

    //Request 5 pieces so the program does not have to wait for the server later on
    for(int i = 0; i < 6; i++)
    {
      requestPiece();
    }

    //Handles all the communicators messages
    communicator.addListener((message) -> {

      //Handles received messages
      if (message.startsWith("MSG")) {
        receiveMessage(message);
      }

      //Handles the next piece
      if (message.startsWith("PIECE")) {
        pieceQueue.add(message);
        logger.info(pieceQueue);
        if (currentPiece == null || followingPiece == null) {
          nextPiece(pieceQueue.remove());
        }
      }

      //Handles received scores
      if (message.startsWith("SCORES")) {
        receiveScores(message);
      }

      //Handles received errors
      if (message.startsWith("ERROR")) {
        logger.error(message);
      }
    });
  }

  /**
   * Handle what should happen when a particular block is clicked
   *
   * @param gameBlock the block that was clicked
   */
  @Override
  public void blockClicked(GameBlock gameBlock) {

    StringBuilder values = new StringBuilder();

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

      //Request next piece
      requestPiece();

      //Gets the next piece from the queue
      nextPiece(pieceQueue.remove());
    }

    for (int i = 0; i < getCols(); i++) {
      for (int j = 0; j < getRows(); j++) {
        values.append(grid.get(i, j)).append(" ");
      }
    }

    //Send the board
    communicator.send("BOARD" + " " + values);
  }

  /**
   * Handles what happens when a block is clicked and only needs x and y coordinates (mainly for
   * keyboard)
   *
   * @param x the x coordinate of the clicked block
   * @param y the y coordinate of the clicked block
   */
  @Override
  public void blockClicked(int x, int y) {

    StringBuilder values = new StringBuilder();

    if (grid.canPlayPiece(currentPiece, x, y)) {
      grid.playPiece(currentPiece, x, y);

      //Restart the timer when a block is placed
      restartTimer();

      Multimedia.playAudio("place.wav");

      //Clean lines and spawn another piece
      afterPiece();
      nextPiece();
    }

    for (int i = 0; i < getCols(); i++) {
      for (int j = 0; j < getRows(); j++) {
        values.append(grid.get(i, j)).append(" ");
      }
    }

    //Send the board
    communicator.send("BOARD" + " " + values);
  }

  /**
   * Sets the listener
   *
   * @param listener listener to set
   */
  public void setGameEndListener(MultiplayerGameEndListener listener) {
    this.multiplayerGameEndListener = listener;
  }

  /**
   * Sets the listener
   *
   * @param receiveMessageListener listener to set
   */
  public void setReceiveMessageListener(
      ReceiveMessageListener receiveMessageListener) {
    this.receiveMessageListener = receiveMessageListener;
  }

  /**
   * Takes the piece that is at the beginning of the queue and sets the current piece to the
   * following piece, which is replaced by the given piece from the queue
   *
   * @param value the next piece gotten from the queue
   */
  public void nextPiece(String value) {

    //Get rid of the message start e.g. PIECE
    value = value.substring(value.indexOf(" ") + 1);

    if (followingPiece == null) {
      followingPiece = GamePiece.createPiece(Integer.parseInt(value));
      return;
    }

    currentPiece = followingPiece;
    logger.info("Current piece is {}", currentPiece);

    followingPiece = GamePiece.createPiece(Integer.parseInt(value));
    logger.info("The following piece is {}", followingPiece);

    nextPieceListener.nextPiece(currentPiece, followingPiece);
  }

  /**
   * Takes the chat message that has been received from the server and cleans it up, then calls the
   * given listener
   *
   * @param message chat message to clean and display
   */
  public void receiveMessage(String message) {
    String[] messageSplitter = message.split(":");

    //Get rid of the message start e.g. MSG
    messageSplitter[0] = messageSplitter[0].substring(messageSplitter[0].indexOf(" ") + 1);

    receiveMessageListener.receiveMessage(messageSplitter[0] + ":" + messageSplitter[1]);
  }

  /**
   * Calculates and sets the score based off of the lines to clear, blocks to clear and multiplier
   * then sends the new score to the server
   *
   * @param lineCount  the number of lines to clear
   * @param blockCount the number of blocks to clear
   */
  @Override
  public void score(int lineCount, int blockCount) {
    var scoreToAdd = (lineCount * blockCount * 10 * getMultiplier());
    logger.info("Attempting to add score {}", scoreToAdd);

    setScore(scoreToAdd + getScore());

    //If there is something to clear send an updated score to the server
    if (lineCount != 0) {
      communicator.send("SCORE" + " " + getScore());
    }

    multiplier(lineCount);
  }

  /**
   * Handles the way the game acts when a piece has not been played for the given time duration
   */
  @Override
  public void gameLoop() {

    //If there are no more lives send the DIE message to the server and show the scores screen
    if (getLives() == 0) {
      communicator.send("DIE");
      multiplayerGameEndListener.endGame(this);
      return;
    }

    //Lives reduced by one
    Platform.runLater(() -> setLives(getLives() - 1));

    //Send the new lives to the server
    communicator.send("LIVES" + " " + (getLives() - 1));

    //Multiplier reset to 1
    Platform.runLater(() -> setMultiplier(1));

    //Request a piece from the server
    requestPiece();

    //Discard current piece
    nextPiece(pieceQueue.remove());

    //Restart timer
    startTimer();
  }

  /**
   * Sends a PIECE message to the server
   */
  public void requestPiece() {
    communicator.send("PIECE");
  }

  /**
   * Cleans the received score from the server and puts them in a list property
   *
   * @param message the scores from the server
   */
  public void receiveScores(String message) {
    logger.info("Receiving scores");

    //Clear any previous scores
    scoresProperty.clear();

    String[] scoresSplitter = message.split("\n");

    //Get rid of the message start e.g. SCORES
    scoresSplitter[0] = scoresSplitter[0].substring(scoresSplitter[0].indexOf(" ") + 1);

    for (String score : scoresSplitter) {
      //Split the result into name, score, lives
      String[] splitter = score.split(":");
      logger.debug(splitter);

      addScore(new String[]{splitter[0], splitter[1], splitter[2]});
    }
  }

  /**
   * Adds the given scores to the main list property
   *
   * @param score score to add
   */
  public void addScore(String[] score) {
    scoresProperty.add(score);

    logger.debug(scoresProperty);
  }

  /**
   * Requests the scores from the server by sending a SCORES message
   */
  public void getScores() {
    communicator.send("SCORES");
  }
}
