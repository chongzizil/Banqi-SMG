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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.banqi.client.Color;
import org.banqi.client.Move;
import org.banqi.client.BanqiState;
import org.banqi.client.Piece;
import org.banqi.client.StateExplorerImpl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class Heuristic {
  private final StateExplorerImpl stateExplorer = new StateExplorerImpl();
  // Basic value of each kind of piece, note that although cannon is vunlunrable to most pieces,
  // it also can capture all kinds of pieces, so it's value for now is 5
  private static final int VALUEOFSOLDIER = 1;
  private static final int VALUEOFHORSE = 6;
  private static final int VALUEOFCHARIOT = 13;
  private static final int VALUEOFELEPHANT = 27;
  private static final int VALUEOFCANNON = 27;
  private static final int VALUEOFADVISOR = 55;
  private static final int VALUEOFGENERAL = 111;
  
  public Heuristic() {
  }
  
  /**
   * Get the value of the current state.
   * 
   * @param banqiState The state for evaluation.
   * @return stateValue The value of the state.
   */
  public int getStateValue(final BanqiState banqiState) {
    BanqiState state = banqiState.copy();
    int faceDownPiecesValue = 0;
    int faceUpPiecesValue = 0;
    
    // The game is over
    if (state.hasGameEnded()) {
      if (state.hasRedOrBlackPieces(Color.R)) {
        return Integer.MIN_VALUE;
      } else {
        return Integer.MAX_VALUE;
      }
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
    List<Move> turnMoves = new ArrayList<Move>();
    List<Move> moveMoves = new ArrayList<Move>();
    List<Move> otherMoves = new ArrayList<Move>();
    
    // Get all possible moves
    Set<Move> allPossibleMoves = stateExplorer.getPossibleMoves(banqiState);
    
//    for (Move move : allPossibleMoves) {
//      if (move.getType() == Move.Type.CAPTURE) {
//        captureMoves.add(move);
//      } else if (move.getType() == Move.Type.TURN) {
//        turnMoves.add(move);
//      } else {
//        moveMoves.add(move);
//      }
//    }
    
    for (Move move : allPossibleMoves) {
      if (move.getType() == Move.Type.CAPTURE) {
        captureMoves.add(move);
      } else {
        otherMoves.add(move);
      }
    }
    
//    // Shuffle
//    Random rnd = new Random();
//    List<Move> shuffledOtherMoves = Lists.newArrayList();
//    while (!otherMoves.isEmpty()) {
//      int index = rnd.nextInt(otherMoves.size());
//      shuffledOtherMoves.add(otherMoves.remove(index));
//    }
    
    // According to the type of the move, reorder all the moves
    orderedMoves.addAll(captureMoves);
    orderedMoves.addAll(otherMoves);
//    orderedMoves.addAll(moveMoves);
//    orderedMoves.addAll(turnMoves);
//    orderedMoves.addAll(shuffledOtherMoves);
    return orderedMoves;
  }
  
  /**
   * Get the number of each piece of each kind and color.
   * 
   * @param state The current state.
   * @return faceDownPiecesNum The number of each piece of each kind and color.
   */
  public Map<String, Integer> getFaceDownPiecesNum(BanqiState state) {
    List<Piece> capturedPieces = state.getCapturedPieces();
    Map<String, Integer> faceDownPiecesNum = new HashMap<String, Integer>();
    
    // Initial number of each kind of pieces
    faceDownPiecesNum.put("rsolNum", 5);
    faceDownPiecesNum.put("bsolNum", 5);
    faceDownPiecesNum.put("rcanNum", 2);
    faceDownPiecesNum.put("bcanNum", 2);
    faceDownPiecesNum.put("rhorNum", 2);
    faceDownPiecesNum.put("bhorNum", 2);
    faceDownPiecesNum.put("rchaNum", 2);
    faceDownPiecesNum.put("bchaNum", 2);
    faceDownPiecesNum.put("releNum", 2);
    faceDownPiecesNum.put("beleNum", 2);
    faceDownPiecesNum.put("radvNum", 2);
    faceDownPiecesNum.put("badvNum", 2);
    faceDownPiecesNum.put("rgenNum", 1);
    faceDownPiecesNum.put("bgenNum", 1);
    
    // Caculate all face up pieces of the state to determine the number of each
    // pieces facing down of the state.
    for (Optional<Piece> piece : state.getCells()) {
      if (piece.isPresent() && piece.get().getKind() != Piece.Kind.EMPTY) {
        Piece.Kind kind = piece.get().getKind();
        Piece.PieceColor pieceColor = piece.get().getPieceColor();
        if (kind == Piece.Kind.SOLDIER) {
          if (pieceColor == Piece.PieceColor.RED) {
            faceDownPiecesNum.put("rsolNum", faceDownPiecesNum.get("rsolNum") - 1);
          } else if (pieceColor == Piece.PieceColor.BLACK) {
            faceDownPiecesNum.put("bsolNum", faceDownPiecesNum.get("bsolNum") - 1);
          }
        } else if (kind == Piece.Kind.CANNON) {
          if (pieceColor == Piece.PieceColor.RED) {
            faceDownPiecesNum.put("rcanNum", faceDownPiecesNum.get("rcanNum") - 1);
          } else if (pieceColor == Piece.PieceColor.BLACK) {
            faceDownPiecesNum.put("bcanNum", faceDownPiecesNum.get("bcanNum") - 1);
          }
        } else if (kind == Piece.Kind.HORSE) {
          if (pieceColor == Piece.PieceColor.RED) {
            faceDownPiecesNum.put("rhorNum", faceDownPiecesNum.get("rhorNum") - 1);
          } else if (pieceColor == Piece.PieceColor.BLACK) {
            faceDownPiecesNum.put("bhorNum", faceDownPiecesNum.get("bhorNum") - 1);
          }
        } else if (kind == Piece.Kind.CHARIOT) {
          if (pieceColor == Piece.PieceColor.RED) {
            faceDownPiecesNum.put("rchaNum", faceDownPiecesNum.get("rchaNum") - 1);
          } else if (pieceColor == Piece.PieceColor.BLACK) {
            faceDownPiecesNum.put("bchaNum", faceDownPiecesNum.get("bchaNum") - 1);
          }
        } else if (kind == Piece.Kind.ELEPHANT) {
          if (pieceColor == Piece.PieceColor.RED) {
            faceDownPiecesNum.put("releNum", faceDownPiecesNum.get("releNum") - 1);
          } else if (pieceColor == Piece.PieceColor.BLACK) {
            faceDownPiecesNum.put("beleNum", faceDownPiecesNum.get("beleNum") - 1);
          }
        } else if (kind == Piece.Kind.ADVISOR) {
          if (pieceColor == Piece.PieceColor.RED) {
            faceDownPiecesNum.put("radvNum", faceDownPiecesNum.get("radvNum") - 1);
          } else if (pieceColor == Piece.PieceColor.BLACK) {
            faceDownPiecesNum.put("badvNum", faceDownPiecesNum.get("badvNum") - 1);
          }
        } else if (kind == Piece.Kind.GENERAL) {
          if (pieceColor == Piece.PieceColor.RED) {
            faceDownPiecesNum.put("rgenNum", faceDownPiecesNum.get("rgenNum") - 1);
          } else if (pieceColor == Piece.PieceColor.BLACK) {
            faceDownPiecesNum.put("bgenNum", faceDownPiecesNum.get("bgenNum") - 1);
          }
        }
      }
    }
    
    // Caculate all captured pieces of the state.
    for (Piece piece : capturedPieces) {
      if (piece.getKind() != Piece.Kind.EMPTY) {
        Piece.Kind kind = piece.getKind();
        Piece.PieceColor pieceColor = piece.getPieceColor();
        if (kind == Piece.Kind.SOLDIER) {
          if (pieceColor == Piece.PieceColor.RED) {
            faceDownPiecesNum.put("rsolNum", faceDownPiecesNum.get("rsolNum") - 1);
          } else if (pieceColor == Piece.PieceColor.BLACK) {
            faceDownPiecesNum.put("bsolNum", faceDownPiecesNum.get("bsolNum") - 1);
          }
        } else if (kind == Piece.Kind.CANNON) {
          if (pieceColor == Piece.PieceColor.RED) {
            faceDownPiecesNum.put("rcanNum", faceDownPiecesNum.get("rcanNum") - 1);
          } else if (pieceColor == Piece.PieceColor.BLACK) {
            faceDownPiecesNum.put("bcanNum", faceDownPiecesNum.get("bcanNum") - 1);
          }
        } else if (kind == Piece.Kind.HORSE) {
          if (pieceColor == Piece.PieceColor.RED) {
            faceDownPiecesNum.put("rhorNum", faceDownPiecesNum.get("rhorNum") - 1);
          } else if (pieceColor == Piece.PieceColor.BLACK) {
            faceDownPiecesNum.put("bhorNum", faceDownPiecesNum.get("bhorNum") - 1);
          }
        } else if (kind == Piece.Kind.CHARIOT) {
          if (pieceColor == Piece.PieceColor.RED) {
            faceDownPiecesNum.put("rchaNum", faceDownPiecesNum.get("rchaNum") - 1);
          } else if (pieceColor == Piece.PieceColor.BLACK) {
            faceDownPiecesNum.put("bchaNum", faceDownPiecesNum.get("bchaNum") - 1);
          }
        } else if (kind == Piece.Kind.ELEPHANT) {
          if (pieceColor == Piece.PieceColor.RED) {
            faceDownPiecesNum.put("releNum", faceDownPiecesNum.get("releNum") - 1);
          } else if (pieceColor == Piece.PieceColor.BLACK) {
            faceDownPiecesNum.put("beleNum", faceDownPiecesNum.get("beleNum") - 1);
          }
        } else if (kind == Piece.Kind.ADVISOR) {
          if (pieceColor == Piece.PieceColor.RED) {
            faceDownPiecesNum.put("radvNum", faceDownPiecesNum.get("radvNum") - 1);
          } else if (pieceColor == Piece.PieceColor.BLACK) {
            faceDownPiecesNum.put("badvNum", faceDownPiecesNum.get("badvNum") - 1);
          }
        } else if (kind == Piece.Kind.GENERAL) {
          if (pieceColor == Piece.PieceColor.RED) {
            faceDownPiecesNum.put("rgenNum", faceDownPiecesNum.get("rgenNum") - 1);
          } else if (pieceColor == Piece.PieceColor.BLACK) {
            faceDownPiecesNum.put("bgenNum", faceDownPiecesNum.get("bgenNum") - 1);
          }
        }
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
        (faceDownPiecesNum.get("rsolNum") - faceDownPiecesNum.get("bsolNum")) * VALUEOFSOLDIER
        + (faceDownPiecesNum.get("rhorNum") - faceDownPiecesNum.get("bhorNum")) * VALUEOFHORSE
        + (faceDownPiecesNum.get("rchaNum") - faceDownPiecesNum.get("bchaNum")) * VALUEOFCHARIOT
        + (faceDownPiecesNum.get("releNum") - faceDownPiecesNum.get("beleNum")) * VALUEOFELEPHANT
        + (faceDownPiecesNum.get("rcanNum") - faceDownPiecesNum.get("bcanNum")) * VALUEOFCANNON
        + (faceDownPiecesNum.get("radvNum") - faceDownPiecesNum.get("badvNum")) * VALUEOFADVISOR
        + (faceDownPiecesNum.get("rgenNum") - faceDownPiecesNum.get("bgenNum")) * VALUEOFGENERAL;
    
    return valueOfFaceDownPiece;
  }
  
  /** 
   * Return the value of a face up piece.
   * 
   * @param piece
   * @return value The value of a face up piece.
   */
  public int getFaceUpPieceValue(Piece piece) {
    int value = piece.getKind() == Piece.Kind.SOLDIER ? VALUEOFSOLDIER
      : piece.getKind() == Piece.Kind.HORSE ? VALUEOFHORSE
      : piece.getKind() == Piece.Kind.CHARIOT ? VALUEOFCHARIOT
      : piece.getKind() == Piece.Kind.ELEPHANT ? VALUEOFELEPHANT
      : piece.getKind() == Piece.Kind.CANNON ? VALUEOFCANNON
      : piece.getKind() == Piece.Kind.ADVISOR ? VALUEOFADVISOR : VALUEOFGENERAL;
    return piece.getPieceColor() == Piece.PieceColor.RED ? value : -value;
  }
}