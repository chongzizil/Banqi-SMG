package org.banqi.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;

public final class GameApi {
  public static final String ALL = "ALL";
  public static final String PLAYER_ID = "playerId";
  public static final String PLAYER_NAME = "playerName";
  public static final String PLAYER_TOKENS = "playerTokens";
  public static final String PLAYER_PROFILE_PIC_URL = "playerProfilePicUrl";

  /** playerId for the Artificial Intelligence (AI) player. */
  public static final int AI_PLAYER_ID = 0;
  /** playerId for a user viewing a match; a viewer can't make any moves in the game. */
  public static final int VIEWER_ID = -1;

  public static class VerifyMove extends HasEquality {
    protected final List<Map<String, Object>> playersInfo;
    protected final Map<String, Object> state;
    protected final Map<String, Object> lastState;

    /**
     * You should verify this lastMove is legal given lastState; some imperfect information
     * games will need to also examine state to determine if the lastMove was legal.
     */
    protected final List<Operation> lastMove;

    /**
     * lastMovePlayerId can either be the ID of a player in playersInfo,
     * or 0 for the Artificial Intelligence (AI) player.
     */
    protected final int lastMovePlayerId;

    /**
     * The number of tokens each player currently has in the pot (see {@link AttemptChangeTokens});
     * The sum of values is always non-negative (i.e., the total pot can NOT be negative).
     * If the game ends when the total pot is non-zero,
     * the pot is given to the player with the highest score (see {@link EndGame}).
     */
    protected final Map<Integer, Integer> playerIdToNumberOfTokensInPot;

    public VerifyMove(List<Map<String, Object>> playersInfo,
        Map<String, Object> state,
        Map<String, Object> lastState,
        List<Operation> lastMove,
        int lastMovePlayerId,
        Map<Integer, Integer> playerIdToNumberOfTokensInPot) {
      this.playersInfo = checkHasJsonSupportedType(playersInfo);
      this.state = checkHasJsonSupportedType(state);
      this.lastState = checkHasJsonSupportedType(lastState);
      this.lastMove = lastMove;
      this.lastMovePlayerId = checkHasJsonSupportedType(lastMovePlayerId);
      this.playerIdToNumberOfTokensInPot = playerIdToNumberOfTokensInPot;
    }

    @Override
    public String getClassName() {
      return "VerifyMove";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.<Object>asList(
          "playersInfo", playersInfo, "state", state,
          "lastState", lastState, "lastMove", lastMove, "lastMovePlayerId", lastMovePlayerId,
          "playerIdToNumberOfTokensInPot", playerIdToNumberOfTokensInPot);
    }

    public Map<Integer, Integer> getPlayerIdToNumberOfTokensInPot() {
      return playerIdToNumberOfTokensInPot;
    }

    public List<Map<String, Object>> getPlayersInfo() {
      return playersInfo;
    }

    public Map<String, Object> getState() {
      return state;
    }

    public List<Integer> getPlayerIds() {
      List<Integer> playerIds = new ArrayList<>();
      for (Map<String, Object> playerInfo : getPlayersInfo()) {
        playerIds.add((Integer) playerInfo.get(PLAYER_ID));
      }
      return playerIds;
    }

    public int getPlayerIndex(int playerId) {
      return getPlayerIds().indexOf(playerId);
    }

    public Map<String, Object> getPlayerInfo(int playerId) {
      for (Map<String, Object> playerInfo : getPlayersInfo()) {
        if (playerId == (Integer) playerInfo.get(PLAYER_ID)) {
          return playerInfo;
        }
      }
      return null;
    }

    public String getPlayerName(int playerId) {
      return String.valueOf(getPlayerInfo(playerId).get(PLAYER_NAME));
    }

    public int getPlayerTokens(int playerId) {
      return (Integer) (getPlayerInfo(playerId).get(PLAYER_TOKENS));
    }

    public String getPlayerProfilePicUrl(int playerId) {
      return String.valueOf(getPlayerInfo(playerId).get(PLAYER_PROFILE_PIC_URL));
    }

    public Map<String, Object> getLastState() {
      return lastState;
    }

    public List<Operation> getLastMove() {
      return lastMove;
    }

    public int getLastMovePlayerId() {
      return lastMovePlayerId;
    }
  }

  public static class UpdateUI extends VerifyMove {
    /**
     * yourPlayerId can either be the ID of a player in playersInfo,
     * or 0 for the Artificial Intelligence (AI) player,
     * or -1 to represent that you're VIEWING a match (i.e., you're not one of the players and
     * therefore you cannot make moves).
     */
    protected final int yourPlayerId;

    public UpdateUI(int yourPlayerId, List<Map<String, Object>> playersInfo,
        Map<String, Object> state,
        Map<String, Object> lastState,
        List<Operation> lastMove,
        int lastMovePlayerId,
        Map<Integer, Integer> playerIdToNumberOfTokensInPot) {
      super(playersInfo, state, lastState, lastMove, lastMovePlayerId,
          playerIdToNumberOfTokensInPot);
      this.yourPlayerId = yourPlayerId;
    }

    @Override
    public String getClassName() {
      return "UpdateUI";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.<Object>asList(
          "yourPlayerId", yourPlayerId, "playersInfo", playersInfo, "state", state,
          "lastState", lastState, "lastMove", lastMove, "lastMovePlayerId", lastMovePlayerId,
          "playerIdToNumberOfTokensInPot", playerIdToNumberOfTokensInPot);
    }

    public int getYourPlayerId() {
      return yourPlayerId;
    }

    public boolean isAiPlayer() {
      return yourPlayerId == AI_PLAYER_ID;
    }

    public boolean isViewer() {
      return yourPlayerId == VIEWER_ID;
    }

    public int getYourPlayerIndex() {
      return getPlayerIds().indexOf(yourPlayerId);
    }
  }

  public abstract static class Operation extends HasEquality { }

  public static class EndGame extends Operation {
    private final Map<Integer, Integer> playerIdToScore;

    public EndGame(Map<Integer, Integer> playerIdToScore) {
      this.playerIdToScore = ImmutableMap.copyOf(playerIdToScore);
    }

    public EndGame(int winnerPlayerId) {
      Map<Integer, Integer> strPlayerIdToScore = new HashMap<>();
      strPlayerIdToScore.put(winnerPlayerId, 1);
      this.playerIdToScore = ImmutableMap.copyOf(strPlayerIdToScore);
    }

    @Override
    public String getClassName() {
      return "EndGame";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.<Object>asList("playerIdToScore", playerIdToScore);
    }

    public Map<Integer, Integer> getPlayerIdToScore() {
      return playerIdToScore;
    }
  }

  public static class Set extends Operation {
    private final String key;
    private final Object value;
    private final Object visibleToPlayerIds;

    public Set(String key, Object value) {
      this(key, value, ALL);
    }

    public Set(String key, Object value, List<Integer> visibleToPlayerIds) {
      this(key, value, (Object) visibleToPlayerIds);
    }

    private Set(String key, Object value, Object visibleToPlayerIds) {
      this.key = key;
      this.value = checkHasJsonSupportedType(value);
      this.visibleToPlayerIds = checkHasJsonSupportedType(visibleToPlayerIds);
    }

    @Override
    public String getClassName() {
      return "Set";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.<Object>asList(
          "key", key, "value", value, "visibleToPlayerIds", visibleToPlayerIds);
    }

    public String getKey() {
      return key;
    }

    public Object getValue() {
      return value;
    }

    public Object getVisibleToPlayerIds() {
      return visibleToPlayerIds;
    }
  }

  /**
   * An operation to set a random integer in the range [from,to),
   * so from {@code from} (inclusive) until {@code to} (exclusive).
   */
  public static class SetRandomInteger extends Operation {
    private final String key;
    private final int from;
    private final int to;

    public SetRandomInteger(String key, int from, int to) {
      this.key = key;
      this.from = from;
      this.to = to;
    }

    @Override
    public String getClassName() {
      return "SetRandomInteger";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.<Object>asList("key", key, "from", from, "to", to);
    }

    public String getKey() {
      return key;
    }

    public int getFrom() {
      return from;
    }

    public int getTo() {
      return to;
    }
  }

  public static class SetVisibility extends Operation {
    private final String key;
    private final Object visibleToPlayerIds;

    public SetVisibility(String key) {
      this(key, ALL);
    }

    public SetVisibility(String key, List<Integer> visibleToPlayerIds) {
      this(key, (Object) visibleToPlayerIds);
    }

    private SetVisibility(String key, Object visibleToPlayerIds) {
      this.key = key;
      this.visibleToPlayerIds = checkHasJsonSupportedType(visibleToPlayerIds);
    }

    @Override
    public String getClassName() {
      return "SetVisibility";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.<Object>asList("key", key, "visibleToPlayerIds", visibleToPlayerIds);
    }

    public String getKey() {
      return key;
    }

    public Object getVisibleToPlayerIds() {
      return visibleToPlayerIds;
    }
  }

  public static class SetTurn extends Operation {
    private final int playerId;
    /** The number of seconds playerId will have to send MakeMove;
     * if it is 0 then the container will decide on the time limit
     * (or the container may decide that there is no time limit).
     */
    private final int numberOfSecondsForTurn;

    public SetTurn(int playerId) {
      this(playerId, 0);
    }

    public SetTurn(int playerId, int numberOfSecondsForTurn) {
      this.playerId = playerId;
      this.numberOfSecondsForTurn = numberOfSecondsForTurn;
    }

    @Override
    public String getClassName() {
      return "SetTurn";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.<Object>asList("playerId", playerId,
          "numberOfSecondsForTurn", numberOfSecondsForTurn);
    }

    public int getPlayerId() {
      return playerId;
    }

    public int getNumberOfSecondsForTurn() {
      return numberOfSecondsForTurn;
    }
  }

  public static class Delete extends Operation {
    private final String key;

    public Delete(String key) {
      this.key = key;
    }

    @Override
    public String getClassName() {
      return "Delete";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.<Object>asList("key", key);
    }

    public String getKey() {
      return key;
    }
  }

  public static class AttemptChangeTokens extends Operation {
    /**
     * Map each playerId to the number of tokens that should be increased/decreased.
     * The server will verify that the total change in tokens (in playerIdToTokenChange)
     * is equal to minus the total change in the pot (in playerIdToNumberOfTokensInPot).
     *
     * For example, suppose the total pot is initially empty, i.e.,
     * playerIdToNumberOfTokensInPot={} (see {@link VerifyMove})
     * Then you do the operation:
     * AttemptChangeTokens({42: -3000, 43: -2000}, {42: 3000, 43: 2000})
     * If playerId=42 indeed has at least 3000 tokens and playerId=43 has at least 2000 tokens
     * then the operation will succeed and the total pot will have 5000 tokens and you will have
     * in {@link VerifyMove}:
     * playerIdToNumberOfTokensInPot={42: 3000, 43: 2000}
     * If one of the players does not have sufficient token then the operation will fail, and
     * playerIdToNumberOfTokensInPot={}
     *
     * Assume the operation succeeded. As the game continues, playerId=43 might risk more money:
     * AttemptChangeTokens({43: -3000}, {42: 3000, 43: 5000})
     * and if he has enough tokens then the total pot will increase to 8000:
     * playerIdToNumberOfTokensInPot={42: 3000, 43: 5000}
     * When the game ends you should distribute the pot, e.g., if the game ends in a tie you could
     * call:
     * AttemptChangeTokens({42: 4000, 43: 4000}, {42: 0, 43:0})
     * and then the total pot will be 0.
     * If the game ends when the total pot is non-zero,
     * the pot is given to the player with the highest score (see {@link EndGame}).
     */
    private final Map<Integer, Integer> playerIdToTokenChange;

    /**
     * The number of tokens each player currently has in the pot;
     * The sum of values is always non-negative (i.e., the total pot can NOT be negative).
     * When the game ends, the pot is given to the player with the highest score.
     */
    protected final Map<Integer, Integer> playerIdToNumberOfTokensInPot;

    public AttemptChangeTokens(Map<Integer, Integer> playerIdToTokenChange,
        Map<Integer, Integer> playerIdToNumberOfTokensInPot) {
      this.playerIdToTokenChange = ImmutableMap.copyOf(playerIdToTokenChange);
      this.playerIdToNumberOfTokensInPot = ImmutableMap.copyOf(playerIdToNumberOfTokensInPot);
    }

    @Override
    public String getClassName() {
      return "AttemptChangeTokens";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.<Object>asList("playerIdToTokenChange", playerIdToTokenChange,
          "playerIdToNumberOfTokensInPot", playerIdToNumberOfTokensInPot);
    }

    public Map<Integer, Integer> getPlayerIdToTokenChange() {
      return playerIdToTokenChange;
    }

    public Map<Integer, Integer> getPlayerIdToNumberOfTokensInPot() {
      return playerIdToNumberOfTokensInPot;
    }
  }

  public static class Shuffle extends Operation {
    private final List<String> keys;

    public Shuffle(List<String> keys) {
      this.keys = checkHasJsonSupportedType(keys);
    }

    @Override
    public String getClassName() {
      return "Shuffle";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.<Object>asList("keys", keys);
    }

    public List<String> getKeys() {
      return keys;
    }
  }

  public static class GameReady extends HasEquality {
    @Override
    public String getClassName() {
      return "GameReady";
    }
  }

  public static class MakeMove extends HasEquality {
    private final List<Operation> operations;

    public MakeMove(List<Operation> operations) {
      this.operations = operations;
    }

    @Override
    public String getClassName() {
      return "MakeMove";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.<Object>asList("operations", operations);
    }

    public List<Operation> getOperations() {
      return operations;
    }
  }

  public static class VerifyMoveDone extends HasEquality {
    private final int hackerPlayerId;
    private final String message;

    /** Move is verified, i.e., no hacker found. */
    public VerifyMoveDone() {
      this(0, null);
    }

    /** Hacker found! */
    public VerifyMoveDone(int hackerPlayerId, String message) {
      this.hackerPlayerId = hackerPlayerId;
      this.message = message;
    }

    @Override
    public String getClassName() {
      return "VerifyMoveDone";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.<Object>asList("hackerPlayerId", hackerPlayerId, "message", message);
    }

    public int getHackerPlayerId() {
      return hackerPlayerId;
    }

    public String getMessage() {
      return message;
    }
  }

  public static class RequestManipulator extends HasEquality {
    @Override
    public String getClassName() {
      return "RequestManipulator";
    }
  }

  public static class ManipulateState extends HasEquality {
    private final Map<String, Object> state;

    public ManipulateState(Map<String, Object> state) {
      this.state = checkHasJsonSupportedType(state);
    }

    @Override
    public String getClassName() {
      return "ManipulateState";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.asList("state", state);
    }

    public Map<String, Object> getOperations() {
      return state;
    }
  }

  public static class ManipulationDone extends HasEquality {
    private final List<Operation> operations;

    public ManipulationDone(List<Operation> operations) {
      this.operations = operations;
    }

    @Override
    public String getClassName() {
      return "ManipulationDone";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.asList("operations", operations);
    }

    public List<Operation> getOperations() {
      return operations;
    }
  }

  public abstract static class HasEquality {
    public abstract String getClassName();

    public List<Object> getFieldsNameAndValue() {
      return Arrays.asList();
    }

    @Override
    public int hashCode() {
      return getFieldsNameAndValue().hashCode() ^ getClassName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof HasEquality)) {
        return false;
      }
      HasEquality other = (HasEquality) obj;
      return Objects.equals(other.getFieldsNameAndValue(), getFieldsNameAndValue())
          && Objects.equals(other.getClassName(), getClassName());
    }

    @Override
    public String toString() {
      return toMessage().toString();
    }

    private List<?> listToMessage(List<?> values) {
      if (values.isEmpty() || !(values.get(0) instanceof HasEquality)) {
        return values;
      }
      List<Object> messages = new ArrayList<>();
      for (Object operation : values) {
        messages.add(((HasEquality) operation).toMessage());
      }
      return messages;
    }

    public Map<String, Object> toMessage() {
      Map<String, Object> message = new HashMap<>();
      message.put("type", getClassName());
      List<Object> fieldsNameAndValue = getFieldsNameAndValue();
      for (int i = 0; i < fieldsNameAndValue.size() / 2; i++) {
        String fieldName = (String) fieldsNameAndValue.get(2 * i);
        Object fieldValue = fieldsNameAndValue.get(2 * i + 1);
        // If the field value is a list of operations (lastMove/operations),
        // then we need to convert each operation to a message
        if (fieldValue instanceof List) {
          fieldValue = listToMessage((List<?>) fieldValue);
        }
        message.put(fieldName, fieldValue);
      }
      return message;
    }

    @SuppressWarnings("unchecked")
    private static List<Operation> messageToOperationList(Object operationMessagesObj) {
      List<?> operationMessages = (List<?>) operationMessagesObj;
      List<Operation> operations = new ArrayList<>();
      for (Object operationMessage : operationMessages) {
        operations.add((Operation) messageToHasEquality((Map<String, Object>) operationMessage));
      }
      return operations;
    }

    @SuppressWarnings("unchecked")
    public static HasEquality messageToHasEquality(Map<String, Object> message) {
      String type = (String) message.get("type");
      switch (type) {
        case "UpdateUI":
          return new UpdateUI(
              (Integer) message.get("yourPlayerId"),
              (List<Map<String, Object>>) message.get("playersInfo"),
              (Map<String, Object>) message.get("state"),
              (Map<String, Object>) message.get("lastState"),
              messageToOperationList(message.get("lastMove")),
              (Integer) message.get("lastMovePlayerId"),
              toIntegerMap(message.get("playerIdToNumberOfTokensInPot")));

        case "VerifyMove":
          return new VerifyMove(
              (List<Map<String, Object>>) message.get("playersInfo"),
              (Map<String, Object>) message.get("state"),
              (Map<String, Object>) message.get("lastState"),
              messageToOperationList(message.get("lastMove")),
              (Integer) message.get("lastMovePlayerId"),
              toIntegerMap(message.get("playerIdToNumberOfTokensInPot")));

        case "EndGame":
          return new EndGame(toIntegerMap(message.get("playerIdToScore")));

        case "Set":
          return new Set((String) message.get("key"),
              message.get("value"),
              message.get("visibleToPlayerIds"));

        case "SetRandomInteger":
          return new SetRandomInteger(
              (String) message.get("key"),
              (Integer) message.get("from"),
              (Integer) message.get("to"));

        case "SetVisibility":
          return new SetVisibility(
              (String) message.get("key"),
              message.get("visibleToPlayerIds"));

        case "SetTurn":
          return new SetTurn((Integer) message.get("playerId"),
              (Integer) message.get("numberOfSecondsForTurn"));

        case "Delete":
          return new Delete((String) message.get("key"));

        case "AttemptChangeTokens":
          return new AttemptChangeTokens(toIntegerMap(message.get("playerIdToTokenChange")),
              toIntegerMap(message.get("playerIdToNumberOfTokensInPot")));

        case "Shuffle":
          return new Shuffle((List<String>) message.get("keys"));

        case "GameReady":
          return new GameReady();

        case "MakeMove":
          return new MakeMove(messageToOperationList(message.get("operations")));

        case "VerifyMoveDone":
          return new VerifyMoveDone(
              (Integer) message.get("hackerPlayerId"),
              (String) message.get("message"));

        case "RequestManipulator":
          return new RequestManipulator();

        case "ManipulateState":
          return new ManipulateState((Map<String, Object>) message.get("state"));

        case "ManipulationDone":
          return new ManipulationDone(messageToOperationList(message.get("operations")));

        default:
          return null;
      }
    }
  }

  static Map<Integer, Integer> toIntegerMap(Object objMap) {
    Map<?, ?> map = (Map<?, ?>) objMap;
    Map<Integer, Integer> result = new HashMap<>();
    for (Object key : map.keySet()) {
      Object value = map.get(key);
      result.put(key instanceof Integer ? (Integer) key : Integer.parseInt(key.toString()),
          value instanceof Integer ? (Integer) value : Integer.parseInt(value.toString()));
    }
    return result;
  }



  /**
   * Checks the object has a JSON-supported data type, i.e.,
   * the object is either a primitive (String, Integer, Double, Boolean, null)
   * or the object is a List and every element in the list has a JSON-supported data type,
   * or the object is a Map and the keys are String and the values have JSON-supported data types.
   * @return the given object.
   */
  static <T> T checkHasJsonSupportedType(T object) {
    if (object == null) {
      return object;
    }
    if (object instanceof Integer || object instanceof Double
        || object instanceof String || object instanceof Boolean) {
      return object;
    }
    if (object instanceof List) {
      List<?> list = (List<?>) object;
      for (Object element : list) {
        checkHasJsonSupportedType(element);
      }
      return object;
    }
    if (object instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) object;
      for (Object key : map.keySet()) {
        if (!(key instanceof String)) {
          throw new IllegalArgumentException("Keys in a map must be String! key=" + key);
        }
      }
      for (Object value : map.values()) {
        checkHasJsonSupportedType(value);
      }
      return object;
    }
    throw new IllegalArgumentException(
        "The object doesn't have a JSON-supported data type! object=" + object);
  }

  private GameApi() { }

}