// Base codes copied from http://bit.ly/1hnmalY

//Copyright 2012 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
////////////////////////////////////////////////////////////////////////////////

package org.banqi.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.banqi.client.BanqiState;
import org.banqi.client.Color;
import org.banqi.client.Move;
import org.banqi.client.Piece;
import org.banqi.client.StateExplorerImpl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class Heuristic {
  private final StateExplorerImpl stateExplorer = new StateExplorerImpl();
  // Basic value of each kind of piece, note that although cannon is vunlunrable to most pieces,
  // it also can capture all kinds of pieces, so it has a relatively high value.
  private static final int VALUEOFSOLDIER = 1;
  private static final int VALUEOFHORSE = 6;
  private static final int VALUEOFCHARIOT = 13;
  private static final int VALUEOFELEPHANT = 27;
  private static final int VALUEOFCANNON = 55;
  private static final int VALUEOFADVISOR = 55;
  private static final int VALUEOFGENERAL = 111;
  private static final Map<String, Integer> FACEDOWNPIECESNUMBER =
      ImmutableMap.<String, Integer>builder()
      .put("rsolNum", 5)
      .put("bsolNum", 5)
      .put("rcanNum", 2)
      .put("bcanNum", 2)
      .put("rhorNum", 2)
      .put("bhorNum", 2)
      .put("rchaNum", 2)
      .put("bchaNum", 2)
      .put("releNum", 2)
      .put("beleNum", 2)
      .put("radvNum", 2)
      .put("badvNum", 2)
      .put("rgenNum", 1)
      .put("bgenNum", 1)
      .build();
      
  public Heuristic() {
  }
  
  /**
   * Get the value of the current state.
   * 
   * @param banqiState The state for evaluation.
   * @return stateValue The value of the state.
   */
  public int getStateValue(final BanqiState state) {
    int faceDownPiecesValue = 0;
    int faceUpPiecesValue = 0;
    
    // The game is over
    Color winnerColor = state.getWinner();
    if (winnerColor == Color.R) {
      return Integer.MIN_VALUE;
    } else if (winnerColor == Color.B) {
      return Integer.MAX_VALUE;
    }
    
    // Get the total value of all face down pieces
    faceDownPiecesValue = getFaceDownPieceValue(state);
    
    // Get the total value of all face up pieces
    for (Optional<Piece> piece : state.getCells()) {
      if (piece.isPresent() && piece.get().getKind() != Piece.Kind.EMPTY) {
        faceUpPiecesValue += getFaceUpPieceValue(piece.get());
      }
    }
    
    return faceUpPiecesValue + faceDownPiecesValue;
  }

  /**
   * Get all possible moves and reorder them.
   * Priority 1: Capture. Since capture is the most effective way to change the state value.
   * Priority 2: Turn. If no piece can be captured, turn up a piece may be the next
   * move which can change the state value.
   * Priority 3: Move.
   * 
   * @param banqiState The current state.
   * @return orderedMoves The ordered all possible moves.
   */
  public Iterable<Move> getOrderedMoves(final BanqiState banqiState) {    
    List<Move> orderedMoves = new ArrayList<Move>();
    List<Move> captureMoves = new ArrayList<Move>();
    List<Move> moveMoves = new ArrayList<Move>();
    List<Move> turnMoves = new ArrayList<Move>();
    
    // Get all possible moves
    Set<Move> allPossibleMoves = stateExplorer.getPossibleMoves(banqiState);
    
    for (Move move : allPossibleMoves) {
      switch(move.getType()) {
        case CAPTURE: captureMoves.add(move); break;
        case TURN: turnMoves.add(move); break;
        case MOVE: moveMoves.add(move); break;
        default: captureMoves.add(move); break;
      }
    }
    
    // According to the type of the move, reorder all the moves
    orderedMoves.addAll(captureMoves);
    orderedMoves.addAll(moveMoves);
    orderedMoves.addAll(turnMoves);
    
    return orderedMoves;
  }
  
  /**
   * Get the number of each piece of each kind and color.
   * 
   * @param state The current state.
   * @return faceDownPiecesNum The number of each piece of each kind and color.
   */
  public Map<String, Integer> getFaceDownPiecesNum(BanqiState state) {
    List<Optional<Piece>> cells = state.getCells();
    List<Piece> capturedPieces = state.getCapturedPieces();
    Map<String, Integer> faceDownPiecesNum = Maps.newHashMap();
    faceDownPiecesNum.putAll(FACEDOWNPIECESNUMBER);

    // Caculate all face up pieces of the state to determine the number of each
    // pieces facing down of the state.
    for (Optional<Piece> piece : cells) {
      if (piece.isPresent() && piece.get().getKind() != Piece.Kind.EMPTY) {
        Piece.Kind kind = piece.get().getKind();
        Piece.PieceColor pieceColor = piece.get().getPieceColor();
        String key = "";
        
        switch(kind) {
          case SOLDIER: key = "solNum"; break;
          case CANNON: key = "canNum"; break;
          case HORSE: key = "horNum"; break;
          case CHARIOT: key = "chaNum"; break;
          case ELEPHANT: key = "eleNum"; break;
          case ADVISOR: key = "advNum"; break;
          case GENERAL: key = "genNum"; break;
          default: key = "solNum"; break;
        }
        
        switch(pieceColor) {
          case RED: key = "r" + key; break;
          case BLACK: key = "b" + key; break;
          default: key = "r" + key; break;
        }
        
        faceDownPiecesNum.put(key, faceDownPiecesNum.get(key) - 1);
      }
    }
    
    // Caculate all captured pieces of the state.
    for (Piece piece : capturedPieces) {
      if (piece.getKind() != Piece.Kind.EMPTY) {
        Piece.Kind kind = piece.getKind();
        Piece.PieceColor pieceColor = piece.getPieceColor();
        String key = "";
        
        switch(kind) {
          case SOLDIER: key = "solNum"; break;
          case CANNON: key = "canNum"; break;
          case HORSE: key = "horNum"; break;
          case CHARIOT: key = "chaNum"; break;
          case ELEPHANT: key = "eleNum"; break;
          case ADVISOR: key = "advNum"; break;
          case GENERAL: key = "genNum"; break;
          default: key = "solNum"; break;
        }
      
        switch(pieceColor) {
          case RED: key = "r" + key; break;
          case BLACK: key = "b" + key; break;
          default: key = "r" + key; break;
        }
        
        faceDownPiecesNum.put(key, faceDownPiecesNum.get(key) - 1);
      }
    }
    
    return faceDownPiecesNum;
  }
  
  /**
   * Get the value of all face down pieces.
   * 
   * @param state
   * @return valueOfFaceDownPiece The value of all face down pieces.
   */
  public int getFaceDownPieceValue(BanqiState state) {
    Map<String, Integer> faceDownPiecesNum = getFaceDownPiecesNum(state);
    
    int valueOfFaceDownPiece =
        (faceDownPiecesNum.get("bsolNum") - faceDownPiecesNum.get("rsolNum")) * VALUEOFSOLDIER
        + (faceDownPiecesNum.get("bhorNum") - faceDownPiecesNum.get("rhorNum")) * VALUEOFHORSE
        + (faceDownPiecesNum.get("bchaNum") - faceDownPiecesNum.get("rchaNum")) * VALUEOFCHARIOT
        + (faceDownPiecesNum.get("beleNum") - faceDownPiecesNum.get("releNum")) * VALUEOFELEPHANT
        + (faceDownPiecesNum.get("bcanNum") - faceDownPiecesNum.get("rcanNum")) * VALUEOFCANNON
        + (faceDownPiecesNum.get("badvNum") - faceDownPiecesNum.get("radvNum")) * VALUEOFADVISOR
        + (faceDownPiecesNum.get("bgenNum") - faceDownPiecesNum.get("rgenNum")) * VALUEOFGENERAL;
    
    return valueOfFaceDownPiece;
  }
  
  /** 
   * Return the value of a face up piece.
   * 
   * @param piece
   * @return value The value of a face up piece.
   */
  public int getFaceUpPieceValue(Piece piece) {
    int value = 0;
    
    switch(piece.getKind()) {
      case SOLDIER: value = VALUEOFSOLDIER; break;
      case HORSE: value = VALUEOFHORSE; break;
      case CHARIOT: value = VALUEOFCHARIOT; break;
      case ELEPHANT: value = VALUEOFELEPHANT; break;
      case CANNON: value = VALUEOFCANNON; break;
      case ADVISOR: value = VALUEOFADVISOR; break;
      case GENERAL: value = VALUEOFGENERAL; break;
      default: value = VALUEOFSOLDIER; break;
    }

    return piece.getPieceColor() == Piece.PieceColor.RED ? -value : value;
  }
}