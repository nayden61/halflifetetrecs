package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the
 * game.
 */
public class ChallengeScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
  private Game game;

  Label titleLbl;

  Label score;

  /**
   * The GameBoard where the game takes place
   */
  protected GameBoard board;

  /**
   * A label that displays the current highscore
   */
  private Label highscoreLbl;

  /**
   * A label that displays the text "UPCOMING"
   */
  protected Label upcomingLbl;

  /**
   * A PieceBoard to display the current piece
   */
  protected PieceBoard currentPieceBoard;

  /**
   * A PieceBoard to display the following piece
   */
  protected PieceBoard followingPieceBoard;

  /**
   * A label that becomes more visible as the timer goes down to indicate urgency
   */
  protected Label warningLbl;

  /**
   * A rectangle that acts as a visual way to represent a timer
   */
  protected Rectangle rectTimer;


  /**
   * Keeps track of the highscore from the Scores.txt file
   */
  private int highScoreValue;

  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public ChallengeScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Challenge Scene");
  }

  /**
   * Build the Challenge window
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    setupGame();

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("challenge-background");
    root.getChildren().add(challengePane);

    var mainPane = new BorderPane();
    challengePane.getChildren().add(mainPane);

    //Challenge screen title
    titleLbl = new Label("STORY MODE");
    titleLbl.getStyleClass().add("challengeTitle");
    challengePane.getChildren().add(titleLbl);
    StackPane.setAlignment(titleLbl, Pos.TOP_LEFT);

    //Score label (HEV Power)
    score = new Label();
    score.getStyleClass().add("score");
    score.textProperty().bind(Bindings.concat("HEV POWER:", game.scoreProperty()));
    challengePane.getChildren().add(score);
    StackPane.setAlignment(score, Pos.TOP_CENTER);

    //Main board where the majority of the gameplay takes place
    board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
    board.getStyleClass().add("board");
    mainPane.setCenter(board);

    //Highscore label
    highscoreLbl = new Label();
    highscoreLbl.getStyleClass().add("highscore");
    challengePane.getChildren().add(highscoreLbl);
    StackPane.setAlignment(highscoreLbl, Pos.TOP_RIGHT);

    //Upcoming label
    upcomingLbl = new Label("UPCOMING");
    upcomingLbl.getStyleClass().add("upcoming");
    challengePane.getChildren().add(upcomingLbl);
    StackPane.setAlignment(upcomingLbl, Pos.TOP_RIGHT);

    //The main big piece board that represents the current piece
    currentPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 5.5,
        gameWindow.getWidth() / 5.5);
    currentPieceBoard.getStyleClass().add("currentPieceBoard");
    challengePane.getChildren().add(currentPieceBoard);
    StackPane.setAlignment(currentPieceBoard, Pos.TOP_RIGHT);
    currentPieceBoard.setTranslateY(100);

    //The secondary piece board that represents the following piece
    followingPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 5.5,
        gameWindow.getWidth() / 5.5);
    followingPieceBoard.getStyleClass().add("followingPieceBoard");
    challengePane.getChildren().add(followingPieceBoard);
    StackPane.setAlignment(followingPieceBoard, Pos.CENTER_RIGHT);
    followingPieceBoard.setTranslateY(80);
    followingPieceBoard.setTranslateX(-15);

    //Lives icon
    var livesRgn = new Region();
    livesRgn.getStyleClass().add("livesIco");
    challengePane.getChildren().add(livesRgn);
    StackPane.setAlignment(livesRgn, Pos.BOTTOM_LEFT);

    //Lives
    var lives = new Label();
    lives.getStyleClass().add("lives");
    lives.textProperty().bind(game.livesProperty());
    challengePane.getChildren().add(lives);
    StackPane.setAlignment(lives, Pos.BOTTOM_LEFT);

    //Lives label
    var livesLbl = new Label("LIVES");
    livesLbl.getStyleClass().add("livesLbl");
    challengePane.getChildren().add(livesLbl);
    StackPane.setAlignment(livesLbl, Pos.BOTTOM_LEFT);

    //Separator
    var separatorRect = new Rectangle(5, 50);
    separatorRect.getStyleClass().add("separator");
    challengePane.getChildren().add(separatorRect);
    StackPane.setAlignment(separatorRect, Pos.BOTTOM_LEFT);

    //Level icon
    var levelRgn = new Region();
    levelRgn.getStyleClass().add("levelIco");
    challengePane.getChildren().add(levelRgn);
    StackPane.setAlignment(levelRgn, Pos.BOTTOM_LEFT);

    //Level
    var level = new Label();
    level.getStyleClass().add("level");
    level.textProperty().bind(game.levelProperty());
    challengePane.getChildren().add(level);
    StackPane.setAlignment(level, Pos.BOTTOM_LEFT);

    //Level label
    var levelLbl = new Label("HEV LEVEL");
    levelLbl.getStyleClass().add("levelLbl");
    challengePane.getChildren().add(levelLbl);
    StackPane.setAlignment(levelLbl, Pos.BOTTOM_LEFT);

    //Warning label
    warningLbl = new Label("WARNING");
    warningLbl.setOpacity(0.0);
    warningLbl.getStyleClass().add("warning");
    challengePane.getChildren().add(warningLbl);
    StackPane.setAlignment(warningLbl, Pos.BOTTOM_CENTER);

    //Multiplier icon
    var multiplierRgn = new Region();
    multiplierRgn.getStyleClass().add("multiplierIco");
    challengePane.getChildren().add(multiplierRgn);
    StackPane.setAlignment(multiplierRgn, Pos.BOTTOM_RIGHT);

    //Multiplier
    var multiplier = new Label();
    multiplier.getStyleClass().add("multiplier");
    multiplier.textProperty().bind(Bindings.concat("x", game.multiplierProperty()));
    challengePane.getChildren().add(multiplier);
    StackPane.setAlignment(multiplier, Pos.BOTTOM_RIGHT);

    //Multiplier label
    var multiplierLbl = new Label("MULTIPLIER");
    multiplierLbl.getStyleClass().add("multiplierLbl");
    challengePane.getChildren().add(multiplierLbl);
    StackPane.setAlignment(multiplierLbl, Pos.BOTTOM_RIGHT);

    //Timer rectangle
    rectTimer = new Rectangle();
    rectTimer.setWidth(gameWindow.getWidth());
    rectTimer.setHeight(25);
    rectTimer.setFill(Color.ORANGE);
    challengePane.getChildren().add(rectTimer);
    StackPane.setAlignment(rectTimer, Pos.BOTTOM_CENTER);

    //Play music
    Multimedia.playMusic("game.mp3");

    //Handle block on gameboard grid being clicked
    board.setOnBlockClick(this::blockClicked);

    //Handles the board being right-clicked
    board.setOnRightClick(this::rightClicked);

    //Handles the current piece pieceboard being left-clicked
    currentPieceBoard.setOnRightClick(this::mainBoardClicked);

    //Handles the following piece pieceboard being clicked
    followingPieceBoard.setOnMouseClicked((e) -> swap());

    //Handles when a next piece is created and swapped
    game.setNextPieceListener(this::nextPiece);

    //Handles when a line has been cleared
    game.setClearedListener(this::lineCleared);

    //Handles when the timer runs out and the game loops
    game.setGameLoopListener(this::timerAnimation);

    //Handles when the game ends
    game.setGameEndListener(this::endGame);

    //Handles when the score is changed
    game.setScoreChangedListener(this::checkHighScore);

    //Gets the highscore from the Scores.txt file at the beginning
    getHighScore();
  }

  /**
   * Setup the game object and model
   */
  public void setupGame() {
    logger.info("Starting a new challenge");

    //Start new game
    game = new Game(5, 5);
  }

  /**
   * Initialise the scene and start the game
   */
  @Override
  public void initialise() {
    logger.info("Initialising Challenge");
    game.start();

    //Handles all the keyboard controls
    scene.setOnKeyPressed(keyEvent -> {

      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        Stage stage = (Stage) root.getScene().getWindow();
        logger.info("Closing stage {}", stage);
        stopGame();
        gameWindow.startMenu();

      } else if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.X) {
        game.blockClicked(board.getAimX(), board.getAimY());

      } else if (keyEvent.getCode() == KeyCode.SPACE || keyEvent.getCode() == KeyCode.R) {
        game.swapCurrentPiece();

      } else if (keyEvent.getCode() == KeyCode.Q || keyEvent.getCode() == KeyCode.Z
          || keyEvent.getCode() == KeyCode.OPEN_BRACKET) {
        game.rotateCurrentPiece(3);

      } else if (keyEvent.getCode() == KeyCode.E || keyEvent.getCode() == KeyCode.C
          || keyEvent.getCode() == KeyCode.CLOSE_BRACKET) {
        game.rotateCurrentPiece(1);

      } else if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
        board.aimChange(0, -1);

      } else if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
        board.aimChange(-1, 0);

      } else if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
        board.aimChange(0, 1);

      } else if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
        board.aimChange(1, 0);
      }
    });
  }

  /**
   * Handle when a block is clicked
   *
   * @param gameBlock the Game Block that was clocked
   */
  void blockClicked(GameBlock gameBlock) {
    game.blockClicked(gameBlock);
  }

  /**
   * Handles when a new piece is created and updates the PieceBoards
   *
   * @param piece          the current piece to be displayed
   * @param followingPiece the following piece to be displayed
   */
  void nextPiece(GamePiece piece, GamePiece followingPiece) {
    currentPieceBoard.setPiece(piece);
    followingPieceBoard.setPiece(followingPiece);
    currentPieceBoard.getBlock(1, 1).drawIndicator();
  }

  /**
   * Calls the fade out method of the game board
   *
   * @param blocks the set of coordinates of blocks to be faded
   */
  void lineCleared(Set<GameBlockCoordinate> blocks) {
    board.fadeOut(blocks);
  }

  /**
   * Handles when the main GameBoard is right-clicked
   */
  void rightClicked() {
    game.rotateCurrentPiece(1);
  }

  /**
   * Handles when the main PieceBoard is left-clicked
   */
  void mainBoardClicked() {
    game.rotateCurrentPiece(1);
  }

  void swap() {
    game.swapCurrentPiece();
  }

  /**
   * Manages the rectangle timer's animations including the size and color changes
   *
   * @param delay how long the keyframes should last
   */
  void timerAnimation(int delay) {
    logger.info("Start timer animation");

    rectTimer.setWidth(gameWindow.getWidth());
    rectTimer.setFill(Color.ORANGE);
    warningLbl.setOpacity(0.0);

    Timeline timeline = new Timeline();
    timeline.getKeyFrames().addAll(
        new KeyFrame(Duration.millis(delay), new KeyValue(rectTimer.widthProperty(), 0)),
        new KeyFrame(Duration.millis(delay), new KeyValue(rectTimer.fillProperty(), Color.RED)),
        new KeyFrame(Duration.millis(delay + 3000), new KeyValue(warningLbl.opacityProperty(), 1))
    );
    timeline.play();
  }

  /**
   * Stops the game and opens the scores stage
   *
   * @param game the game instance
   */
  void endGame(Game game) {
    Stage stage = (Stage) root.getScene().getWindow();
    logger.info("Closing stage {}", stage);

    stopGame();
    Platform.runLater(() -> gameWindow.startScores(game.getScore()));
  }

  /**
   * Gets the highscore from the Scores.txt file
   */
  public void getHighScore() {
    try {

      //Use a buffered reader to only read the first line
      BufferedReader bufferedReader = new BufferedReader(new FileReader("Scores.txt"));
      var highScore = bufferedReader.readLine();

      //Split the name and the score
      String[] splitter = highScore.split(":");

      //Save the value of the score
      highScoreValue = Integer.parseInt(splitter[1]);

      logger.debug("HIGH SCORE: {}", highScore);

      //Display the highscore with the name included
      highscoreLbl.setText("HIGHSCORE \n" + highScore);

    } catch (IOException e) {
      logger.error("Cannot read from scores file");
      e.printStackTrace();
    }
  }

  /**
   * Checks if the user's current score is higher than the highscore from the Scores.txt file. Keep
   * updating the label with the current score if it is.
   */
  public void checkHighScore() {
    if (game.getScore() > highScoreValue) {
      highscoreLbl.textProperty().bind(Bindings.concat("HIGHSCORE \nYou:", game.scoreProperty()));
    }
  }

  /**
   * Closes the game and ends all ongoing processes tied to it
   */
  public void stopGame() {
    game.getFuture().cancel(false);
    Multimedia.stopAudio();
  }
}
