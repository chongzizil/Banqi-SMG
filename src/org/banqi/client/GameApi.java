package org.banqi.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class GameApi {
  public static final String ALL = "ALL";
  public static final String PLAYER_ID = "playerId";
  public static final String PLAYER_NAME = "playerName";
  public static final String PLAYER_PROFILE_PIC_URL = "playerProfilePicUrl";

  /** playerId for the Artificial Intelligence (AI) player. */
  public static final int AI_PLAYER_ID = 0;

  public static class UpdateUI extends HasEquality {
    protected final int yourPlayerId;
    protected final List<Map<String, Object>> playersInfo;
    protected final Map<String, Object> state;
    private final Map<String, Object> lastState;
    private final List<Operation> lastMove;
    private final int lastMovePlayerId;

    public UpdateUI(int yourPlayerId, List<Map<String, Object>> playersInfo,
        Map<String, Object> state,
        Map<String, Object> lastState,
        List<Operation> lastMove,
        int lastMovePlayerId) {
      this.yourPlayerId = yourPlayerId;
      this.playersInfo = checkHasJsonSupportedType(playersInfo);
      this.state = checkHasJsonSupportedType(state);
      this.lastState = checkHasJsonSupportedType(lastState);
      this.lastMove = lastMove;
      this.lastMovePlayerId = checkHasJsonSupportedType(lastMovePlayerId);
    }

    @Override
    public String getClassName() {
      return "UpdateUI";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.<Object>asList(
          "yourPlayerId", yourPlayerId, "playersInfo", playersInfo, "state", state,
          "lastState", lastState, "lastMove", lastMove, "lastMovePlayerId", lastMovePlayerId);
    }

    public int getYourPlayerId() {
      return yourPlayerId;
    }

    public boolean isAiPlayer() {
      return yourPlayerId == AI_PLAYER_ID;
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

    public int getYourPlayerIndex() {
      return getPlayerIds().indexOf(yourPlayerId);
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

  public static class VerifyMove extends UpdateUI {

    public VerifyMove(int yourPlayerId, List<Map<String, Object>> playersInfo,
        Map<String, Object> state,
        Map<String, Object> lastState,
        List<Operation> lastMove,
        int lastMovePlayerId) {
      super(yourPlayerId, playersInfo, state, lastState, lastMove, lastMovePlayerId);
    }

    @Override
    public String getClassName() {
      return "VerifyMove";
    }


  }

  public abstract static class Operation extends HasEquality { }

  public static class EndGame extends Operation {
    private final Map<String, Integer> playerIdToScore;

    public EndGame(Map<String, Integer> playerIdToScore) {
      this.playerIdToScore = checkHasJsonSupportedType(playerIdToScore);
    }

    @Override
    public String getClassName() {
      return "EndGame";
    }

    @Override
    public List<Object> getFieldsNameAndValue() {
      return Arrays.<Object>asList("playerIdToScore", playerIdToScore);
    }

    public EndGame(int winnerPlayerId) {
      playerIdToScore = new HashMap<>();
      playerIdToScore.put(String.valueOf(winnerPlayerId), 1);
    }

    public Map<String, Integer> getPlayerIdToScore() {
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
              (Integer) message.get("lastMovePlayerId"));

        case "VerifyMove":
          return new VerifyMove(
              (Integer) message.get("yourPlayerId"),
              (List<Map<String, Object>>) message.get("playersInfo"),
              (Map<String, Object>) message.get("state"),
              (Map<String, Object>) message.get("lastState"),
              messageToOperationList(message.get("lastMove")),
              (Integer) message.get("lastMovePlayerId"));

        case "EndGame":
          return new EndGame((Map<String, Integer>) message.get("playerIdToScore"));

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

        case "Delete":
          return new Delete((String) message.get("key"));

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