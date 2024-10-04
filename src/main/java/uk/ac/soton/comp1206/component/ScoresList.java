package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This component will display a list of scores
 */
public class ScoresList extends VBox {

  private static final Logger logger = LogManager.getLogger(ScoresList.class);

  /**
   * The observable list property that keeps track of the scores that it is given
   */
  private SimpleListProperty<Pair<String, Integer>> scoresProperty = new SimpleListProperty<Pair<String, Integer>>(
      FXCollections.observableArrayList());

  /**
   * Returns the scoresProperty list property
   * @return the list property
   */
  public SimpleListProperty<Pair<String, Integer>> getScoresProperty() {
    return scoresProperty;
  }

  /**
   * The constructor handles the display of the scores whenever the list property changes
   */
  public ScoresList() {

    //When the list gets changed display the newest version
    scoresProperty.addListener(
        (ListChangeListener<Pair<String, Integer>>) change -> displayScores());
  }

  /**
   * Displays the scores in a visual way
   */
  public void displayScores() {
    Platform.runLater(() -> {

      //Clear any previous scores
      getChildren().clear();

      //Display each score as text
      for (Pair<String, Integer> score : scoresProperty) {
        logger.info("Displaying {}", score);
        var individualScore = new Label(String.format("%s:%s", score.getKey(), score.getValue()));
        individualScore.getStyleClass().add("individualScore");
        getChildren().add(individualScore);
      }

      //Play the animation every time the scores are displayed
      reveal();}
    );
  }

  /**
   * Plays an animation whenever the scores are displayed
   */
  public void reveal() {
    logger.debug("Playing reveal animation");

    //Make a sequential transition to reveal each score one by one
    SequentialTransition sequentialTransition = new SequentialTransition();

    for (int i = 0; i < getChildren().size(); i++) {

      //Make the scores invisible
      getChildren().get(i).setOpacity(0.0);

      //Give the individual score a fade animation
      FadeTransition fade = new FadeTransition(new Duration(2000), getChildren().get(i));
      fade.setToValue(1.0);

      //Add it to the sequential animation
      sequentialTransition.getChildren().add(fade);
    }

    //Play the sequential animation at the end
    sequentialTransition.play();
  }
}
