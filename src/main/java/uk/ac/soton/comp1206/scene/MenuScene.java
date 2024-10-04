package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * The VBox that will store the menu buttons
   */
  VBox menu;

  /**
   * Button that starts the game
   */
  Button newGameBttn;

  /**
   * Button that opens the multiplayer lobby
   */
  Button findServersBttn;

  /**
   * Button that opens the instructions menu
   */
  Button instructionsBttn;

  /**
   * Button that closes the game
   */
  Button quitBttn;

  /**
   * The title of the game
   */
  Label titleLbl;

  /**
   * The ECSGames logo
   */
  Region logoRgn;

  /**
   * Create a new menu scene
   *
   * @param gameWindow the Game Window this will be displayed in
   */
  public MenuScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Menu Scene");
  }

  /**
   * Build the menu layout
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("menu-background");
    root.getChildren().add(menuPane);

    var mainPane = new BorderPane();
    menuPane.getChildren().add(mainPane);

    //Menu Vbox to store the buttons
    menu = new VBox();
    menu.getStyleClass().add("menu");
    menuPane.getChildren().add(menu);
    StackPane.setAlignment(menu, Pos.BOTTOM_LEFT);

    //Button to start the game
    newGameBttn = new Button("New Game");
    newGameBttn.getStyleClass().add("menuItem");
    menu.getChildren().add(newGameBttn);

    //Button to start the multiplayer lobby
    findServersBttn = new Button("Find Servers");
    findServersBttn.getStyleClass().add("menuItem");
    menu.getChildren().add(findServersBttn);

    //Button to start instructions
    instructionsBttn = new Button("Instructions");
    instructionsBttn.getStyleClass().add("menuItem");
    menu.getChildren().add(instructionsBttn);

    //Button to quit the game
    quitBttn = new Button("Quit");
    quitBttn.getStyleClass().add("menuItem");
    menu.getChildren().add(quitBttn);

    //Game title
    titleLbl = new Label("T E T R - E C S");
    titleLbl.getStyleClass().add("menuTitle");
    menuPane.getChildren().add(titleLbl);
    StackPane.setAlignment(titleLbl, Pos.BOTTOM_LEFT);

    //Display logo (It's a region not image because for some reason images do not want to work properly)
    logoRgn = new Region();
    logoRgn.getStyleClass().add("logo");
    menuPane.getChildren().add(logoRgn);
    StackPane.setAlignment(logoRgn, Pos.BOTTOM_RIGHT);

    //Call the reveal animation
    reveal();
  }

  /**
   * Initialise the menu and handle keyboard events
   */
  @Override
  public void initialise() {

    //Play music
    Multimedia.playMusic("menu.mp3");

    //Handles all the buttons on action and mouse entered events
    newGameBttn.setOnAction((e) -> startGame());
    newGameBttn.setOnMouseEntered((e) -> Multimedia.playAudio("buttonrollover.wav"));

    instructionsBttn.setOnAction((e) -> showInstructions());
    instructionsBttn.setOnMouseEntered((e) -> Multimedia.playAudio("buttonrollover.wav"));

    findServersBttn.setOnAction((e) -> showMultiplayer());
    findServersBttn.setOnMouseEntered((e) -> Multimedia.playAudio("buttonrollover.wav"));

    quitBttn.setOnAction((e) -> quitGame());
    quitBttn.setOnMouseEntered((e) -> Multimedia.playAudio("buttonrollover.wav"));

    titleLbl.setOnMouseClicked((e) -> Multimedia.playAudio("buttonclickrelease.wav"));
    titleLbl.setOnMouseEntered((e) -> Multimedia.playAudio("buttonrollover.wav"));

    //Will close the application if escape has been pressed
    scene.setOnKeyPressed((keyEvent) -> {
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        quitGame();
      }
    });
  }

  /**
   * Plays an animation that reveals each element on the scene one by one
   */
  public void reveal() {
    logger.debug("Playing reveal animation");

    //Make a sequential transition to reveal each menu option one by one
    var sequentialTransition = new SequentialTransition();

    for (int i = 0; i < menu.getChildren().size(); i++) {

      //Make the buttons invisible
      menu.getChildren().get(i).setOpacity(0.0);

      //Give the individual button a fade animation
      FadeTransition fade = new FadeTransition(new Duration(500), menu.getChildren().get(i));
      fade.setToValue(1.0);

      //Add the fades to the sequential animation
      sequentialTransition.getChildren().add(fade);
    }

    //Same with the title
    titleLbl.opacityProperty().set(0.0);
    FadeTransition titleFade = new FadeTransition(new Duration(1000), titleLbl);
    titleFade.setToValue(1.0);

    //And logo
    logoRgn.opacityProperty().set(0.0);
    FadeTransition logoFade = new FadeTransition(new Duration(1000), logoRgn);
    logoFade.setToValue(1.0);

    //Add the title and logo animations to the sequential transition
    sequentialTransition.getChildren().addAll(titleFade, logoFade);

    //Play the sequential animation at the end
    sequentialTransition.play();
  }

  /**
   * Handle when the Start Game button is pressed
   */
  private void startGame() {
    Multimedia.stopAudio();
    gameWindow.startChallenge();
  }

  /**
   * Handle when the Instructions button is pressed
   */
  private void showInstructions() {
    Multimedia.stopAudio();
    gameWindow.startInstructions();
  }

  /**
   * Handle when the Multiplayer button is pressed
   */
  private void showMultiplayer() {
    Multimedia.stopAudio();
    gameWindow.startMultiplayerLobby();
  }

  /**
   * Handles the way the game is closed
   */
  private void quitGame() {
    logger.info("Closing application");
    gameWindow.getCommunicator().send("QUIT");
    Platform.exit();
  }
}
