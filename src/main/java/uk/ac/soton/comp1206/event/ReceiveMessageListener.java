package uk.ac.soton.comp1206.event;

/**
 * The ReceiveMessageListener interface provides a method that will handle the change of the text
 * element in a scene based off of the received message from the server
 */
public interface ReceiveMessageListener {

  /**
   * Handles the way the chat message from the server is displayed
   *
   * @param message message to display in the UI
   */
  void receiveMessage(String message);
}
