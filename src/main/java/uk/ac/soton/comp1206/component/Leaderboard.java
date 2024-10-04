package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This component is used to display players scores in the multiplayer part of the game
 */
public class Leaderboard extends ScoresList {

  private static final Logger logger = LogManager.getLogger(Leaderboard.class);

  /**
   * The observable list property that keeps track of the scores
   */
  private SimpleListProperty<String[]> scoresProperty = new SimpleListProperty<>(
      FXCollections.observableArrayList());

  /**
   * Returns the scoresProperty list property
   *
   * @return the list property
   */
  public SimpleListProperty<String[]> getMultiplayerScoresProperty() {
    return scoresProperty;
  }

  /**
   * The constructor handles the display of the scores whenever the list property changes
   */
  public Leaderboard() {

    //When the list gets changed display the newest version
    scoresProperty.addListener(
        (ListChangeListener<String[]>) change -> displayScores());
  }

  /**
   * Displays the scores in a visual way
   */
  public void displayScores() {
    Platform.runLater(() -> {

          //Clear any previous scores
          getChildren().clear();

          //Display each score as text
          for (String[] score : scoresProperty) {
            logger.info("Displaying {}", score);
            var leaderboardScore = new Label(String.format("%s:%s:%s", score[0], score[1], score[2]));
            leaderboardScore.getStyleClass().add("leaderboardScore");
            getChildren().add(leaderboardScore);
          }

          //Play the animation every time the scores are displayed
          reveal();
        }
    );
  }

  /**
   * Plays an animation whenever the scores are displayed
   */
  @Override
  public void reveal() {
    logger.debug("Playing reveal animation");

    //Make a sequential transition to reveal each score one by one
    SequentialTransition sequentialTransition = new SequentialTransition();

    for (int i = 0; i < getChildren().size(); i++) {

      //Make the scores invisible
      getChildren().get(i).setOpacity(0.0);

      //Give the individual score a fade animation
      FadeTransition fade = new FadeTransition(new Duration(500), getChildren().get(i));
      fade.setToValue(1.0);

      //Add it to the sequential animation
      sequentialTransition.getChildren().add(fade);
    }

    //Play the sequential animation at the end
    sequentialTransition.play();
  }
}
