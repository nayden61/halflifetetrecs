package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The instructions scene will display all the pieces as they are dynamically generated and show the controls for the game.
 */
public class InstructionsScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public InstructionsScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating instructions scene");
  }

  /**
   * Builds the layout of the instruction scene
   */
  @Override
  public void build() {
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var instructionPane = new StackPane();
    instructionPane.setMaxWidth(gameWindow.getWidth());
    instructionPane.setMaxHeight(gameWindow.getHeight());
    instructionPane.getStyleClass().add("instructions-background");
    root.getChildren().add(instructionPane);

    var instructionsTitle = new Label("INSTRUCTIONS");
    instructionsTitle.getStyleClass().add("instructionsTitle");
    instructionPane.getChildren().add(instructionsTitle);
    StackPane.setAlignment(instructionsTitle, Pos.TOP_LEFT);

    var instructionsRgn = new Region();
    instructionsRgn.getStyleClass().add("instructionsImage");
    instructionPane.getChildren().add(instructionsRgn);
    StackPane.setAlignment(instructionsRgn, Pos.CENTER_RIGHT);

    var piecesTitle = new Label("PIECES");
    piecesTitle.getStyleClass().add("piecesTitle");
    instructionPane.getChildren().add(piecesTitle);
    StackPane.setAlignment(piecesTitle, Pos.TOP_LEFT);

    var piecesPane = new GridPane();
    piecesPane.getStyleClass().add("piecesPane");
    piecesPane.setVgap(10);
    piecesPane.setHgap(10);
    piecesPane.setMaxSize(gameWindow.getWidth(), gameWindow.getHeight() / 2);
    instructionPane.getChildren().add(piecesPane);
    StackPane.setAlignment(piecesPane, Pos.TOP_CENTER);

    //Creating and adding each pieceBoard to the piecePane
    for (int i = 0; i < 15; i++) {
      var pieceBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 8, gameWindow.getHeight() / 7);
      pieceBoard.setPiece(GamePiece.createPiece(i));

      if (i < 3) {
        piecesPane.add(pieceBoard, i, 0);
      } else if(i < 6) {
        piecesPane.add(pieceBoard, i - 3, 1);
      } else if(i < 9) {
        piecesPane.add(pieceBoard, i - 6, 2);
      }else if(i < 12) {
        piecesPane.add(pieceBoard, i - 9, 3);
      } else {
        piecesPane.add(pieceBoard, i - 12, 4);
      }
    }
  }

  /**
   * Initialise the menu and handle keyboard events
   */
  @Override
  public void initialise() {

    Multimedia.playMusic("instructions.mp3");

    //Will show the start menu if the escape has been pressed
    scene.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        Multimedia.stopAudio();
        Stage stage = (Stage) root.getScene().getWindow();
        logger.info("Closing stage {}", stage);
        gameWindow.startMenu();
      }
    });
  }
}
