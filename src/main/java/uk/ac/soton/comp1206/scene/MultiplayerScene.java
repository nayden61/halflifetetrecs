package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The MultiplayerScene functions similar to the ChallengeScene but has some slight changes to make
 * it more suitable for multiplayer playing.
 */
public class MultiplayerScene extends ChallengeScene {

  private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);
  /**
   * Instance of the logic for the multiplayer part
   */
  private MultiplayerGame multiplayerGame;
  /**
   * The textfield that is used for the ingame chat
   */
  TextField messageField;

  /**
   * Message to send
   */
  Text message;

  /**
   * Create a new MultiPlayer challenge scene
   *
   * @param gameWindow the Game Window
   */
  public MultiplayerScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  /**
   * Build the MultiplayerScene window
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    setupGame();

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var multiplayerPane = new StackPane();
    multiplayerPane.setMaxWidth(gameWindow.getWidth());
    multiplayerPane.setMaxHeight(gameWindow.getHeight());
    multiplayerPane.getStyleClass().add("menu-background");
    root.getChildren().add(multiplayerPane);

    var mainPane = new BorderPane();
    multiplayerPane.getChildren().add(mainPane);

    //Challenge screen title
    titleLbl = new Label("VERSUS MODE");
    titleLbl.getStyleClass().add("challengeTitle");
    multiplayerPane.getChildren().add(titleLbl);
    StackPane.setAlignment(titleLbl, Pos.TOP_LEFT);

    //Score label (HEV Power)
    score = new Label();
    score.getStyleClass().add("score");
    score.textProperty().bind(Bindings.concat("HEV POWER:", multiplayerGame.scoreProperty()));
    multiplayerPane.getChildren().add(score);
    StackPane.setAlignment(score, Pos.TOP_CENTER);

    //Main board where the majority of the gameplay takes place
    board = new GameBoard(multiplayerGame.getGrid(), gameWindow.getWidth() / 2,
        gameWindow.getWidth() / 2);
    board.getStyleClass().add("board");
    mainPane.setCenter(board);

    //Upcoming label
    upcomingLbl = new Label("UPCOMING");
    upcomingLbl.getStyleClass().add("upcoming");
    multiplayerPane.getChildren().add(upcomingLbl);
    StackPane.setAlignment(upcomingLbl, Pos.TOP_RIGHT);

    //The main big piece board that represents the current piece
    currentPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 5.5,
        gameWindow.getWidth() / 5.5);
    currentPieceBoard.getStyleClass().add("currentPieceBoard");
    multiplayerPane.getChildren().add(currentPieceBoard);
    StackPane.setAlignment(currentPieceBoard, Pos.TOP_RIGHT);
    currentPieceBoard.setTranslateY(100);

    //The secondary piece board that represents the following piece
    followingPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 5.5,
        gameWindow.getWidth() / 5.5);
    followingPieceBoard.getStyleClass().add("followingPieceBoard");
    multiplayerPane.getChildren().add(followingPieceBoard);
    StackPane.setAlignment(followingPieceBoard, Pos.CENTER_RIGHT);
    followingPieceBoard.setTranslateY(80);
    followingPieceBoard.setTranslateX(-15);

    //Lives icon
    var livesRgn = new Region();
    livesRgn.getStyleClass().add("livesIco");
    multiplayerPane.getChildren().add(livesRgn);
    StackPane.setAlignment(livesRgn, Pos.BOTTOM_LEFT);

    //Lives
    var lives = new Label();
    lives.getStyleClass().add("lives");
    lives.textProperty().bind(multiplayerGame.livesProperty());
    multiplayerPane.getChildren().add(lives);
    StackPane.setAlignment(lives, Pos.BOTTOM_LEFT);

    //Lives label
    var livesLbl = new Label("LIVES");
    livesLbl.getStyleClass().add("livesLbl");
    multiplayerPane.getChildren().add(livesLbl);
    StackPane.setAlignment(livesLbl, Pos.BOTTOM_LEFT);

    //Warning label
    warningLbl = new Label("WARNING");
    warningLbl.setOpacity(0.0);
    warningLbl.getStyleClass().add("warning");
    multiplayerPane.getChildren().add(warningLbl);
    StackPane.setAlignment(warningLbl, Pos.BOTTOM_CENTER);

    //Multiplier icon
    var multiplierRgn = new Region();
    multiplierRgn.getStyleClass().add("multiplierIco");
    multiplayerPane.getChildren().add(multiplierRgn);
    StackPane.setAlignment(multiplierRgn, Pos.BOTTOM_RIGHT);

    //Multiplier
    var multiplier = new Label();
    multiplier.getStyleClass().add("multiplier");
    multiplier.textProperty().bind(Bindings.concat("x", multiplayerGame.multiplierProperty()));
    multiplayerPane.getChildren().add(multiplier);
    StackPane.setAlignment(multiplier, Pos.BOTTOM_RIGHT);

    //Multiplier label
    var multiplierLbl = new Label("MULTIPLIER");
    multiplierLbl.getStyleClass().add("multiplierLbl");
    multiplayerPane.getChildren().add(multiplierLbl);
    StackPane.setAlignment(multiplierLbl, Pos.BOTTOM_RIGHT);

    //Timer rectangle
    rectTimer = new Rectangle();
    rectTimer.setWidth(gameWindow.getWidth());
    rectTimer.setHeight(25);
    rectTimer.setFill(Color.ORANGE);
    multiplayerPane.getChildren().add(rectTimer);
    StackPane.setAlignment(rectTimer, Pos.BOTTOM_CENTER);

    //Leaderboard
    var leaderBoard = new Leaderboard();
    leaderBoard.getStyleClass().add("leaderboard");

    //Bind the leaderboard to the game
    Bindings.bindContent(leaderBoard.getMultiplayerScoresProperty(),
        multiplayerGame.scoresProperty);

    multiplayerPane.getChildren().add(leaderBoard);
    StackPane.setAlignment(leaderBoard, Pos.TOP_RIGHT);

    //Add chat
    var messageBar = new VBox();
    messageBar.getStyleClass().add("gameMessageBar");
    StackPane.setAlignment(messageBar, Pos.TOP_RIGHT);

    message = new Text();
    message.getStyleClass().add("message");

    messageField = new TextField();
    messageField.setPromptText("Use T to type");

    messageBar.getChildren().add(message);
    messageBar.getChildren().add(messageField);

    messageField.setDisable(true);

    multiplayerPane.getChildren().add(messageBar);

    //Play music
    Multimedia.playMusic("game.mp3");

    //Handle block on gameboard grid being clicked
    board.setOnBlockClick(this::blockClicked);

    //Handles the board being right-clicked
    board.setOnRightClick(this::rightClicked);

    //Handles the current piece pieceboard being left-clicked
    currentPieceBoard.setOnRightClick(this::mainBoardClicked);

    followingPieceBoard.setOnMouseClicked((e) -> swap());

    //Handles when a next piece is created and swapped
    multiplayerGame.setNextPieceListener(this::nextPiece);

    //Handles when a line has been cleared
    multiplayerGame.setClearedListener(this::lineCleared);

    //Handles when the timer runs out and the game loops
    multiplayerGame.setGameLoopListener(this::timerAnimation);

    //Handles when the game ends
    multiplayerGame.setGameEndListener(this::endMultiplayerGame);

    //Handles receiving messages
    multiplayerGame.setReceiveMessageListener(this::receiveMessage);
  }

  /**
   * Setup the game object and model
   */
  @Override
  public void setupGame() {
    logger.info("Starting a new challenge");

    //Start new game
    multiplayerGame = new MultiplayerGame(5, 5, gameWindow.getCommunicator());
  }

  /**
   * Initialise the scene and start the game
   */
  public void initialise() {
    logger.info("Initialising Challenge");
    multiplayerGame.start();

    //Handles all the keyboard controls
    scene.setOnKeyPressed(keyEvent -> {

      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        Stage stage = (Stage) root.getScene().getWindow();
        logger.info("Closing stage {}", stage);
        gameWindow.getCommunicator().send("DIE");
        stopGame();
        gameWindow.startMenu();

      } else if (keyEvent.getCode() == KeyCode.ENTER && !messageField.isDisabled()) {
        sendMessage();

      } else if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.X) {
        multiplayerGame.blockClicked(board.getAimX(), board.getAimY());

      } else if ((keyEvent.getCode() == KeyCode.SPACE || keyEvent.getCode() == KeyCode.R)
          && messageField.isDisabled()) {
        multiplayerGame.swapCurrentPiece();

      } else if ((keyEvent.getCode() == KeyCode.Q || keyEvent.getCode() == KeyCode.Z
          || keyEvent.getCode() == KeyCode.OPEN_BRACKET) && messageField.isDisabled()) {
        multiplayerGame.rotateCurrentPiece(3);

      } else if ((keyEvent.getCode() == KeyCode.E || keyEvent.getCode() == KeyCode.C
          || keyEvent.getCode() == KeyCode.CLOSE_BRACKET) && messageField.isDisabled()) {
        multiplayerGame.rotateCurrentPiece(1);

      } else if ((keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W)
          && messageField.isDisabled()) {
        board.aimChange(0, -1);

      } else if ((keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A)
          && messageField.isDisabled()) {
        board.aimChange(-1, 0);

      } else if ((keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S)
          && messageField.isDisabled()) {
        board.aimChange(0, 1);

      } else if ((keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D)
          && messageField.isDisabled()) {
        board.aimChange(1, 0);

      } else if (keyEvent.getCode() == KeyCode.T) {
        messageField.setDisable(false);
      }
    });
  }

  /**
   * Handle when a block is clicked
   *
   * @param gameBlock the Game Block that was clocked
   */
  @Override
  void blockClicked(GameBlock gameBlock) {
    multiplayerGame.blockClicked(gameBlock);
  }

  /**
   * Handles when the main GameBoard is right-clicked
   */
  @Override
  public void rightClicked() {
    multiplayerGame.rotateCurrentPiece(1);
  }

  /**
   * Handles when the main PieceBoard is left-clicked
   */
  @Override
  void mainBoardClicked() {
    multiplayerGame.rotateCurrentPiece(1);
  }

  /**
   * Send a chat message to the communicator
   */
  public void sendMessage() {
    gameWindow.getCommunicator()
        .send(String.format("MSG %s", messageField.getText()));

    messageField.setText("");
    messageField.setDisable(true);
  }

  /**
   * Display the chat message received from the server
   *
   * @param message received from the server
   */
  public void receiveMessage(String message) {
    this.message.setText(message);
  }

  /**
   * Stops the game and opens the scores stage
   *
   * @param multiplayerGame the multiplayer game instance
   */
  void endMultiplayerGame(MultiplayerGame multiplayerGame) {
    Stage stage = (Stage) root.getScene().getWindow();
    logger.info("Closing stage {}", stage);

    stopGame();
    Platform.runLater(() -> gameWindow.startScores(multiplayerGame.scoresProperty));
  }

  /**
   * Closes the game and ends all ongoing processes tied to it
   */
  public void stopGame() {
    multiplayerGame.getFuture().cancel(false);
    Multimedia.stopAudio();
  }
}
