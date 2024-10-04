package uk.ac.soton.comp1206;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;

/**
 * The multimedia class handles multimedia files such as audio files and their execution.
 */
public class Multimedia {

  private static final Logger logger = LogManager.getLogger(Game.class);

  /**
   * Used to turn the audio on or off
   */
  private static boolean audioEnabled = true;

  /**
   * A media player that will be used to play audio
   */
  private static MediaPlayer audioPlayer;

  /**
   * A media player that will be used to play music
   */
  private static MediaPlayer musicPlayer;

  /**
   * Will play the given short audio
   *
   * @param audio audio to play
   */
  public static void playAudio(String audio) {
    if (!audioEnabled) {
      return;
    }

    String toPlay = Multimedia.class.getResource("/sounds/" + audio).toExternalForm();
    logger.info("Playing audio {}", toPlay);

    try {
      Media play = new Media(toPlay);
      audioPlayer = new MediaPlayer(play);
      audioPlayer.play();

    } catch (Exception e) {
      audioEnabled = false;
      logger.error("Cannot play audio file, disabling audio");
    }
  }

  /**
   * Will play the given background music to play
   *
   * @param music background music to play
   */
  public static void playMusic(String music) {
    if (!audioEnabled) {
      return;
    }

    String toPlay = Multimedia.class.getResource("/music/" + music).toExternalForm();
    logger.info("Playing background music {}", toPlay);

    try {
      Media play = new Media(toPlay);
      musicPlayer = new MediaPlayer(play);
      musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
      musicPlayer.play();

    } catch (Exception e) {
      audioEnabled = false;
      logger.error("Cannot play music file, disabling audio");
    }
  }

  /**
   * Stop all of the audio that is currently playing
   */
  public static void stopAudio() {

    logger.info("Stopping all audio");
    if (audioPlayer != null) {
      audioPlayer.stop();
    }
    if (musicPlayer != null) {
      musicPlayer.stop();
    }
  }
}
