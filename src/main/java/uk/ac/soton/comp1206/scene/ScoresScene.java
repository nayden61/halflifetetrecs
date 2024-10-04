package uk.ac.soton.comp1206.scene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;
import java.util.Stack;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * This scene displays the locally saved scores and the current score if it is higher than the
 * locally stored ones
 */
public class ScoresScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(ScoresScene.class);

  /**
   * The arraylist that stores the scores
   */
  private final ArrayList<Pair<String, Integer>> localScores = new ArrayList<>();

  /**
   * The observable list property that keeps track of the scores and updates the scores for the local
   * ScoreList component
   */
  private final SimpleListProperty<Pair<String, Integer>> localScoresProperty = new SimpleListProperty<>(
      FXCollections.observableArrayList(localScores));

  /**
   * The observable list property that keeps track of the remote scores and updates the scores for the remote
   * ScoreList component
   */
  private final SimpleListProperty<Pair<String, Integer>> remoteScoresProperty = new SimpleListProperty<>(
      FXCollections.observableArrayList());

  /**
   * Stores the scores of all users from the multiplayer game instance
   */
  private SimpleListProperty<String[]> currentMultiplayerScore;

  /**
   * A prompt that makes the user enter their username
   */
  TextInputDialog nameDialog;

  /**
   * The score that was gotten in the game before this scene was shown
   */
  Integer currentScore;

  /**
   * Stores the name of the player
   */
  String playerName;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in and the singleplayer score
   *
   * @param gameWindow the game window
   * @param score singleplayer score
   */
  public ScoresScene(GameWindow gameWindow, int score) {
    super(gameWindow);
    currentMultiplayerScore = null;
    currentScore = score;
  }

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in and the multiplayer scores
   * @param gameWindow the game window
   * @param scoresProperty multiplayer scores
   */
  public ScoresScene(GameWindow gameWindow, SimpleListProperty<String[]> scoresProperty) {
    super(gameWindow);
    currentMultiplayerScore = scoresProperty;
  }

  /**
   * Builds the layout of the score scene
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var scorePane = new StackPane();
    scorePane.setMaxWidth(gameWindow.getWidth());
    scorePane.setMaxHeight(gameWindow.getHeight());
    scorePane.getStyleClass().add("score-background");
    root.getChildren().add(scorePane);

    nameDialog = new TextInputDialog();
    nameDialog.setHeaderText("Enter name");

    //Title
    var title = new Label("TETR-ECS");
    title.getStyleClass().add("scoreTitle");
    scorePane.getChildren().add(title);
    StackPane.setAlignment(title, Pos.TOP_CENTER);

    //Subtitle
    var subtitle = new Label("Well Done Doctor!");
    subtitle.getStyleClass().add("scoreSubtitle");
    scorePane.getChildren().add(subtitle);
    StackPane.setAlignment(subtitle, Pos.TOP_CENTER);

    //Local scores lbl
    var localScoresListLbl = new Label("Local HEV Power");
    localScoresListLbl.getStyleClass().add("localScoresLbl");
    scorePane.getChildren().add(localScoresListLbl);
    StackPane.setAlignment(localScoresListLbl, Pos.CENTER);

    //Local scores ScoreList
    var localScoresList = new ScoresList();
    localScoresList.getStyleClass().add("localScores");

    //Bind the list of the ScoresList to the list of the scene
    Bindings.bindContent(localScoresList.getScoresProperty(), localScoresProperty);
    localScoresList.getStyleClass().add("localScores");
    scorePane.getChildren().add(localScoresList);
    StackPane.setAlignment(localScoresList, Pos.TOP_CENTER);

    //Remote scores lbl
    var remoteScoresListLbl = new Label("Remote HEV Power");
    remoteScoresListLbl.getStyleClass().add("remoteScoresLbl");
    scorePane.getChildren().add(remoteScoresListLbl);
    StackPane.setAlignment(remoteScoresListLbl, Pos.CENTER);

    //Remote scores ScoreList
    var remoteScoresList = new ScoresList();
    remoteScoresList.getStyleClass().add("remoteScores");

    //Bind the list of the ScoresList to the remote list of the scene
    Bindings.bindContent(remoteScoresList.getScoresProperty(), remoteScoresProperty);
    scorePane.getChildren().add(remoteScoresList);

    if (currentMultiplayerScore != null) {
      addMultiplayerScores();
      return;
    }

    //Firstly load the scores from the file and order them
    loadScores("Scores.txt");

    logger.debug(
        "The contents of localScoresList in the scene are {} and the component ones are {}",
        localScoresProperty.get(), localScoresList.getScoresProperty());

    //Then compare the scores
    compareScores();

    Multimedia.playMusic("scores.mp3");
    Multimedia.playAudio("lose.wav");
  }

  /**
   * Initialise the menu and handle keyboard events
   */
  @Override
  public void initialise() {
    gameWindow.getCommunicator().send("HISCORES");

    gameWindow.getCommunicator().addListener((message) -> {
      if (!message.startsWith("HISCORES")) {
        return;
      }
      loadOnlineScores(message);
      compareOnlineScores();
    });

    scene.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        Multimedia.stopAudio();
        Stage stage = (Stage) root.getScene().getWindow();
        logger.info("Closing stage {}", stage);
        gameWindow.startMenu();
      }
    });
  }

  /**
   * Adds a score as a Pair
   *
   * @param score to add
   * @param list the list to add the Pair to
   */
  public void addScore(Pair<String, Integer> score, SimpleListProperty list) {
    logger.info("Adding score {} to {}", score, list);
    list.add(score);
    logger.debug(list);
    if (list == remoteScoresProperty) {
      return;
    }
    orderScores();
  }

  /**
   * Adds the multiplayer scores to the localScoresProperty
   */
  public void addMultiplayerScores() {
    logger.info("Adding multiplayer scores");
    for (String[] strings : currentMultiplayerScore) {
      localScoresProperty.add(new Pair<>(strings[0],
          Integer.parseInt(strings[1])));
    }
  }

  /**
   * Orders the scores and writes them to a file
   */
  public void orderScores() {
    logger.info("Ordering scores");
    logger.debug("Not in order {}", localScoresProperty);

    //Sorts the list in descending order
    localScoresProperty.sort(Comparator.comparing(p -> -p.getValue()));

    //Rewrite the scores to the file in the new correct order
    writeScores("Scores.txt");
  }

  /**
   * Compares the score that the user just got to the saved scores. Opens a textfield if the score
   * is bigger than any score.
   */
  public void compareScores() {
    for (Pair<String, Integer> score : localScoresProperty) {
      if (currentScore > score.getValue()) {
        logger.info("User score is bigger than at least one saved score");

        nameDialog.showAndWait();
        playerName = nameDialog.getEditor().getText();
        addScore(new Pair<>(playerName, currentScore), localScoresProperty);
        return;
      }
    }
  }

  /**
   * Check if the current singleplayer score is bigger than any online score
   */
  public void compareOnlineScores() {
    for (Pair<String, Integer> score : remoteScoresProperty) {
      if (currentScore > score.getValue()) {
        writeOnlineScore();
        return;
      }
    }
  }

  /**
   * Loads the scores from the given file
   *
   * @param file where the scores are stored
   */
  public void loadScores(String file) {
    logger.info("Loading scores");
    ArrayList<String> loadedScores = new ArrayList<>();

    int i = 0;

    //Use a scanner to go through each line in the given file
    try {
      Scanner scanner = new Scanner(new File(file));
      while (scanner.hasNextLine()) {

        //Store each line in an array list
        loadedScores.add(scanner.nextLine());
      }
    } catch (FileNotFoundException e) {
      logger.error("File not found");
    }

    //Then split each line into two and add it as a pair to the scoreListProperty
    for (String lines : loadedScores) {
      if(i >= 10) {
        return;
      }

      String[] splitter = lines.split(":");
      logger.debug(splitter);
      addScore(new Pair(splitter[0], Integer.parseInt(splitter[1])), localScoresProperty);
      i++;
    }

    //Order the scores in case they aren't
    orderScores();
  }

  /**
   * Cleans the online scores received from the server and adds them to the remoteScores list property
   * @param message the online scores
   **/
  public void loadOnlineScores(String message) {

    logger.debug("THIS");

    //Split the string into separate strings
    String[] scoresSplitter = message.split("\n");

    //Get rid of the message start e.g. HISCORE
    scoresSplitter[0] = scoresSplitter[0].substring(scoresSplitter[0].indexOf(" ") + 1);

    for (String lines : scoresSplitter) {
      String[] splitter = lines.split(":");
      logger.debug("Online scores: {} {}", splitter);
      addScore(new Pair(splitter[0], Integer.parseInt(splitter[1])), remoteScoresProperty);
      logger.debug("remote list is {}", remoteScoresProperty);
    }
  }

  /**
   * Writes the scores to a file. Creates a file and writes default scores if there is not file.
   *
   * @param file to write the scores in
   */
  public void writeScores(String file) {
    logger.info("Writing scores to file");
    File file1 = new File(file);

    //Check if it exists
    if (!file1.exists()) {
      try {
        FileWriter myWriter = new FileWriter(file);

        myWriter.write("Default1:1000\n");
        myWriter.write("Default2:4000\n");
        myWriter.write("Default3:500\n");
        myWriter.write("Default4:3500\n");
        myWriter.write("Default5:3000\n");

        myWriter.close();
      } catch (IOException e) {
        logger.error("Unable to write to file");
      }
    } else {
      try {
        FileWriter myWriter = new FileWriter(file);
        for (Pair score : localScoresProperty) {
          myWriter.write(String.format("%s:%s\n", score.getKey(), score.getValue()));
        }

        myWriter.close();
      } catch (IOException e) {
        logger.error("Unable to write to file");
      }
    }
  }

  /**
   * Send a message to the communicator to write the current score
   */
  public void writeOnlineScore() {
    gameWindow.getCommunicator().send(String.format("HISCORE %s:%s", playerName, currentScore));

    //Write to the remote scores
    remoteScoresProperty.add(new Pair<>(playerName, currentScore));
    orderOnlineScores();
  }

  /**
   * Orders the online scores after they have been updated
   */
  public void orderOnlineScores() {
    //Sorts the list in descending order
    remoteScoresProperty.sort(Comparator.comparing(p -> -p.getValue()));
  }
}
