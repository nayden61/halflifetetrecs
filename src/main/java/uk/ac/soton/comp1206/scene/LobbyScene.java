package uk.ac.soton.comp1206.scene;

import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The lobby scene is used to show available multiplayer channels which can be joined or created. There is also a chat features.
 */
public class LobbyScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(LobbyScene.class);

  /**
   * The vertical box that will contain the available channels
   */
  VBox channelList;

  /**
   * The button that will be used to create a channel
   */
  Button createChannel;

  /**
   * The StackPane that will contain everything about the channel itself
   */
  StackPane channelPane;

  /**
   * Used to stop the timer
   */
  protected ScheduledFuture<?> future;

  /**
   * Name of the channel to join
   */
  String channelToJoin;

  /**
   * The vertical box that will contain the names of the users in a channel
   */
  VBox userList;

  /**
   * The TextFlow that will display the messages in the chat of a channel
   */
  TextFlow messages;

  /**
   * Used to send messages in the channel chat
   */
  Button messageButton;

  /**
   * The TextField where messages will be typed iin
   */
  TextField messageField;

  /**
   * A boolean to keep track if the user is a host or not
   */
  Boolean isHost = false;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //The main StackPane
    var lobbyPane = new StackPane();
    lobbyPane.setMaxWidth(gameWindow.getWidth());
    lobbyPane.setMaxHeight(gameWindow.getHeight());
    lobbyPane.getStyleClass().add("menu-background");
    root.getChildren().add(lobbyPane);

    var lobbyTitleLbl = new Label("FIND SERVERS");
    lobbyTitleLbl.getStyleClass().add("lobbyTitle");
    lobbyPane.getChildren().add(lobbyTitleLbl);
    StackPane.setAlignment(lobbyTitleLbl, Pos.TOP_LEFT);

    //ChannelList Vbox
    channelList = new VBox();
    channelList.getStyleClass().add("channelList");
    lobbyPane.getChildren().add(channelList);
    StackPane.setAlignment(channelList, Pos.CENTER_LEFT);

    //ChannelPane StackPane
    channelPane = new StackPane();
    channelPane.getStyleClass().add("channelPane");
    channelPane.setOpacity(0.0);
    lobbyPane.getChildren().add(channelPane);
    StackPane.setAlignment(channelPane, Pos.CENTER_RIGHT);

    //Create channel button
    createChannel = new Button();
    createChannel.getStyleClass().add("createChannel");
    lobbyPane.getChildren().add(createChannel);
    createChannel.setAlignment(Pos.BOTTOM_CENTER);

    //Handles when the createChannel button is clicked
    createChannel.setOnMouseClicked((e) ->{
      TextInputDialog nameDialog = new TextInputDialog();
      nameDialog.setHeaderText("Enter channel name");
      nameDialog.showAndWait();
      createChannel(nameDialog.getEditor().getText());
      Multimedia.playAudio("buttonclick.wav");
    });

    //Join channel button
    var joinChannel = new Button();
    joinChannel.getStyleClass().add("joinChannel");
    lobbyPane.getChildren().add(joinChannel);
    joinChannel.setAlignment(Pos.BOTTOM_CENTER);

    joinChannel.setOnMouseClicked((e) -> {
      joinChannel(channelToJoin);
    Multimedia.playAudio("buttonclick.wav");
    });
  }

  /**
   * Initialise the scene and handle keyboard and communicator events
   */
  @Override
  public void initialise() {
    var executor = Executors.newSingleThreadScheduledExecutor();

    Multimedia.playMusic("lobby.mp3");

    var alert = new Alert(AlertType.ERROR);
    alert.setTitle("An Error Has Occurred");
    alert.setHeaderText("There was an issue with your request");

    //Ask the server for currently available channels
    future = executor.scheduleAtFixedRate(() -> {
      logger.info("Refreshing the list");
      gameWindow.getCommunicator().send("LIST");
    }, 0, 5000, TimeUnit.MILLISECONDS);

    //Handles all the communicators messages
    gameWindow.getCommunicator().addListener((message) -> {

      //Handles received messages
      if (message.startsWith("MSG")) {
        Multimedia.playAudio("message.wav");
        receiveMessage(message);
      }

      //Handles the event of the game starting
      if (message.startsWith("START")) {
        isHost = false;
        stopScene();
        Platform.runLater(gameWindow::startMultiplayer);
      }

      //Handles received errors
      if (message.startsWith("ERROR")) {
        logger.error(message);
        alert.setContentText(message);

        alert.showAndWait();
      }

      //Handles the received list of currently active channels
      if (message.startsWith("CHANNELS")) {
        displayChannels(message);
      }

      //Handles the received list of users that are in the current channel
      if (message.startsWith("USERS")) {
        displayUsers(message);
      }
    });

    //Handle keyboard events
    scene.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        Stage stage = (Stage) root.getScene().getWindow();
        logger.info("Closing stage {}", stage);
        stopScene();
        gameWindow.startMenu();
      }
    });
  }

  /**
   * Display the channels received by the server
   *
   * @param message the channels
   */
  public void displayChannels(String message) {

    //Split the string into separate strings
    String[] channelsSplitter = message.split("\n");

    //Get rid of the message start e.g. CHANNELS
    channelsSplitter[0] = channelsSplitter[0].substring(channelsSplitter[0].indexOf(" ") + 1);

    //Clean the previously displayed channels
    cleanChannels();

    for (String name : channelsSplitter) {
      addChannel(name);
    }
  }

  /**
   * Add the channels received from the server
   * @param channelName the name of the channel to be added
   */
  public void addChannel(String channelName) {
    logger.info("Adding channel {}", channelName);

    Platform.runLater(() -> {
      Button channel = new Button(channelName);
      channel.getStyleClass().add("channel");
      channelList.getChildren().add(channel);
      channel.setOnMouseClicked((e) -> {
        Multimedia.playAudio("buttonclick.wav");
        channelToJoin = channelName;
      });
    });
  }

  /**
   * Clean the previous channels, so they don't keep getting added
   */
  public void cleanChannels() {
    Platform.runLater(() -> channelList.getChildren().clear());
  }

  /**
   * Join the selected channel
   *
   * @param channelToJoin name of the selected channel
   */
  public void joinChannel(String channelToJoin) {
    logger.info("Joining {}", channelToJoin);
    gameWindow.getCommunicator().send(String.format("JOIN %s", channelToJoin));
    isHost = false;
    displayChannel(channelToJoin);
  }

  /**
   * Create a channel with the given name
   *
   * @param channelName the name of the channel
   */
  public void createChannel(String channelName) {
    if(channelName.trim().equals(""))
      return;
    gameWindow.getCommunicator().send(String.format("CREATE %s", channelName));
    isHost = true;
    displayChannel(channelName);
  }

  /**
   * Displays a channel when it is joined or created
   *
   * @param channeName the name of the joined/created channel
   */
  public void displayChannel(String channeName) {
    logger.info("Displaying channel {}", channeName);
    channelPane.getChildren().clear();

    //Make the channelPane visible
    channelPane.setOpacity(1.0);

    //Add the channel title
    var channelTitle = new Label(channeName);
    channelTitle.getStyleClass().add("channelTitle");
    channelPane.getChildren().add(channelTitle);
    StackPane.setAlignment(channelTitle, Pos.TOP_LEFT);

    //Make a list for the users in the channel
    userList = new VBox();
    userList.getStyleClass().add("userList");
    channelPane.getChildren().add(userList);
    StackPane.setAlignment(userList, Pos.CENTER_RIGHT);

    //Retrieve the users that are in the channel
    gameWindow.getCommunicator().send("USERS");

    //Make the channel chat
    displayChat();
  }

  /**
   * Display the users that are in the current channel
   *
   * @param users that are in the current channel
   */
  public void displayUsers(String users) {
    logger.info("Displaying users");

    String[] usersSplitter = users.split("\n");

    //Get rid of the message start e.g. USERS
    usersSplitter[0] = usersSplitter[0].substring(usersSplitter[0].indexOf(" ") + 1);

    Platform.runLater(() -> {

      //Clear the userList of any previous text
      userList.getChildren().clear();
      for (String user : usersSplitter) {
        logger.debug("Add user {}", user);
        var name = new Label(user);
        userList.getChildren().add(name);
      }
    });
  }

  /**
   * Display the chat of the current channel
   */
  public void displayChat() {
    logger.debug("Displaying chat");

    //The HBox that takes care of the elements that are using in chatting
    var messageBar = new HBox();
    messageBar.getStyleClass().add("messageBar");

    messageField = new TextField();
    messageField.getStyleClass().add("messageField");
    messageField.setPromptText("type \\nick Name to change nickname");
    StackPane.setAlignment(messageField, Pos.BOTTOM_LEFT);

    messageButton = new Button("");
    messageButton.getStyleClass().add("messageButton");

    messageBar.getChildren().add(messageField);
    messageBar.getChildren().add(messageButton);

    channelPane.getChildren().add(messageBar);
    StackPane.setAlignment(messageBar, Pos.BOTTOM_CENTER);

    messages = new TextFlow();
    messages.getStyleClass().add("messages");
    channelPane.getChildren().add(messages);
    StackPane.setAlignment(messages, Pos.TOP_LEFT);

    var message = new Text("Welcome!" + "\n");
    messages.getChildren().add(message);

    //Send a message whenever the button is pressed
    sendMessage();

    //Check if user is the host and if they can start the game
    if (isHost) {
      startGame();
    }

    //Leave the channel if the corresponding button is pressed
    leaveChannel();
  }

  /**
   * Send a message to the channel
   */
  public void sendMessage() {
    messageButton.setOnMouseClicked((e) -> {

      if (messageField.getText().startsWith("\\nick")) {
        gameWindow.getCommunicator()
            .send(String.format("NICK %s", messageField.getText().substring(5)));
        messageField.setText("");
        return;
      }
      Multimedia.playAudio("buttonclick.wav");
      gameWindow.getCommunicator().send(String.format("MSG %s", messageField.getText()));
      messageField.setText("");
    });
  }

  /**
   * Handles the way a chat message is received
   *
   * @param message a chat message
   */
  public void receiveMessage(String message) {

    String[] messageSplitter = message.split(":");

    //Get rid of the message start e.g. MSG
    messageSplitter[0] = messageSplitter[0].substring(messageSplitter[0].indexOf(" ") + 1);

    Platform.runLater(() -> messages.getChildren()
        .add(new Text(messageSplitter[0] + ":" + messageSplitter[1] + "\n")));

  }

  /**
   * Starts the game
   */
  public void startGame() {
    logger.info("Starting game");
    var startButton = new Button("");
    startButton.getStyleClass().add("start");

    channelPane.getChildren().add(startButton);
    StackPane.setAlignment(startButton, Pos.BOTTOM_RIGHT);

    startButton.setOnMouseClicked((e) -> gameWindow.getCommunicator().send("START"));
  }

  /**
   * Leave the current channel
   */
  public void leaveChannel() {
    logger.info("Leaving channel");
    var leaveButton = new Button();
    leaveButton.getStyleClass().add("leave");

    leaveButton.setOnMouseClicked((e) -> {
      gameWindow.getCommunicator().send("PART");
      isHost = false;
      channelToJoin = null;
      channelPane.setOpacity(0.0);
      Multimedia.playAudio("buttonclick.wav");
    });
    channelPane.getChildren().add(leaveButton);
    StackPane.setAlignment(leaveButton, Pos.TOP_RIGHT);
  }

  /**
   * Shuts off the scene and its components
   */
  public void stopScene() {
    future.cancel(false);
    Multimedia.stopAudio();
  }
}
