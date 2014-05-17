// Base codes copied from: http://bit.ly/1kGOZh2

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import org.banqi.client.BanqiState;
import org.banqi.client.Color;
import org.banqi.client.Move;
import org.banqi.client.Piece;
import org.banqi.client.Position;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

/**
 * http://en.wikipedia.org/wiki/Alpha-beta_pruning<br>
 * This algorithm performs both A* and alpha-beta pruning.<br>
 * The set of possible moves is maintained ordered by the current heuristic
 * value of each move. We first use depth=1, and update the heuristic value of
 * each move, then use depth=2, and so on until we get a timeout or reach
 * maximum depth. <br>
 * If a state has {@link TurnBasedState#whoseTurn} null (which happens in
 * backgammon when we should roll the dice), then I treat all the possible moves
 * with equal probabilities. <br>
 * 
 * @author yzibin@google.com (Yoav Zibin)
 */
public class AlphaBetaPruning {
  // A full state where all facedown pieces are revealed by shuffle.
  // The full state is not the real one, so even for the AI, it has 
  // to "guess"...
  private BanqiState fullState;
  private BanqiState state;
  private Heuristic heuristic;
  private static int turnCount = 0;
  private static int moveCount = 0;
  private static int winCaseCount = 0;
  private static int loseCaseCount = 0;

  static class TimeoutException extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }

  @SuppressWarnings("hiding")
  static class MoveScore<Move> implements Comparable<MoveScore<Move>> {
    Move move;
    int score;

    @Override
    public int compareTo(MoveScore<Move> o) {
      return o.score - score; // sort DESC (best score first)
    }
  }

  public AlphaBetaPruning(Heuristic heuristic, BanqiState banqiState) {
    this.heuristic = heuristic;
    this.state = banqiState.copy();
  }

  public Move findBestMove(int depth, Timer timer) {
    boolean isBlack = state.getTurn().isBlack();    
    
    // Do iterative deepening (A*), and slow get better heuristic values for the
    // states.
    List<MoveScore<Move>> scores = Lists.newArrayList();

    Iterable<Move> possibleMoves = heuristic.getOrderedMoves(state);
    for (Move move : possibleMoves) {
      MoveScore<Move> score = new MoveScore<Move>();
      score.move = move;
      score.score = Integer.MIN_VALUE;
      scores.add(score);
    }

    try {
//      long startTime = System.currentTimeMillis();
      for (int i = 0; i < depth; i++) {
        // Get the fullState    
        this.fullState = getFullState(state.copy());
//        console("Depth " + i + " start's at " + (System.currentTimeMillis() - startTime));
        for (int j = 0; j < scores.size(); j++) {
          Move move = null;
          MoveScore<Move> moveScore = scores.get(j);
          move = moveScore.move;
          // Initial the turnCount.
          turnCount = 0;
          int score = findMoveScore(makeMove(state, move),
              i, Integer.MIN_VALUE, Integer.MAX_VALUE, timer);
          if (!isBlack) {
            // the scores are from the point of view of the black, so for white
            // we need to switch.
            score = -score;
          }
          moveScore.score = score;
        }
        // This will give better pruning on the next iteration.
        Collections.sort(scores); 
      }
    } catch (TimeoutException e) {
      // OK, it should happen
    }

    Collections.sort(scores);
    
    Move bestMove = scores.get(0).move;
    
//    console("Total move count: " + moveCount);
//    console("Win case count: " + winCaseCount);
//    console("Lose case count: " + loseCaseCount);
    
    return bestMove;
  }

  /**
   * If we get a timeout, then the score is invalid.
   */
  private int findMoveScore(final BanqiState banqiState,
      int depth, int alpha, int beta, Timer timer) throws TimeoutException {
    BanqiState state = banqiState.copy();
    
    if (timer.didTimeout()) {
      throw new TimeoutException();
    }

    if (depth == 0 || state.getWinner() != Color.N) {
      if (state.getWinner() == Color.R) {
        loseCaseCount++;
      } else if (state.getWinner() == Color.B) {
        winCaseCount++;
      }
      
      return (int) (heuristic.getStateValue(state) * (Math.pow(0.98, turnCount)));
    }

    Color color = state.getTurn();
    int scoreSum = 0;
    int count = 0;
    Iterable<Move> possibleMoves = heuristic.getOrderedMoves(state);
    for (Move move : possibleMoves) {
      count++;
      int childScore = findMoveScore(makeMove(state, move), depth - 1, alpha, beta, timer);
      if (color == null) {
        scoreSum += childScore;
      } else if (color.isBlack()) {
        alpha = Math.max(alpha, childScore);
        if (beta <= alpha) {
          break;
        }
      } else {
        beta = Math.min(beta, childScore);
        if (beta <= alpha) {
          break;
        }
      }
    }
    return color == null ? scoreSum / count : color.isBlack() ? alpha : beta;
  }

  /**
   * Get the full state by shuffle all face down pieces and reveal them all.
   * 
   * @param banqiState The current state.
   * @return state The full state.
   */
  public BanqiState getFullState(BanqiState banqiState) {
    BanqiState state = banqiState.copy();
    List<Optional<Piece>> cells = state.getCells();

    // All face down pieces waiting to be shuffled
    List<Piece> faceDownPieces = new ArrayList<Piece>();
    // All face down piece indexes
    Queue<Integer> faceDownPieceIndexes = new LinkedList<Integer>();
    

    // Get all face down piece indexes
    for (int i = 0; i < cells.size(); i++) {
      Optional<Piece> cell = cells.get(i);
      if (!cell.isPresent()) {
        faceDownPieceIndexes.offer(i);
      }
    }
    
    // Number of each face down piece
    Map<String, Integer> faceDownPiecesNum = heuristic
        .getFaceDownPiecesNum(state);

    // Get all face down pieces
    for (Map.Entry<String, Integer> entry : faceDownPiecesNum.entrySet()) {
      switch(entry.getKey()) {
        case "rsolNum": 
          for (int i = 0; i < entry.getValue(); i++) {
            faceDownPieces
                .add(new Piece(Piece.Kind.SOLDIER, Piece.PieceColor.RED));
          }
          break;
        case "bsolNum": 
          for (int i = 0; i < entry.getValue(); i++) {
            faceDownPieces
                .add(new Piece(Piece.Kind.SOLDIER, Piece.PieceColor.BLACK));
          }
          break;
        case "rcanNum": 
          for (int i = 0; i < entry.getValue(); i++) {
            faceDownPieces
                .add(new Piece(Piece.Kind.CANNON, Piece.PieceColor.RED));
          }
          break;
        case "bcanNum": 
          for (int i = 0; i < entry.getValue(); i++) {
            faceDownPieces
                .add(new Piece(Piece.Kind.CANNON, Piece.PieceColor.BLACK));
          }
          break;
        case "rhorNum": 
          for (int i = 0; i < entry.getValue(); i++) {
            faceDownPieces
                .add(new Piece(Piece.Kind.HORSE, Piece.PieceColor.RED));
          }
          break;
        case "bhorNum": 
          for (int i = 0; i < entry.getValue(); i++) {
            faceDownPieces
                .add(new Piece(Piece.Kind.HORSE, Piece.PieceColor.BLACK));
          }
          break;
        case "rchaNum": 
          for (int i = 0; i < entry.getValue(); i++) {
            faceDownPieces
                .add(new Piece(Piece.Kind.CHARIOT, Piece.PieceColor.RED));
          }
          break;
        case "bchaNum": 
          for (int i = 0; i < entry.getValue(); i++) {
            faceDownPieces
                .add(new Piece(Piece.Kind.CHARIOT, Piece.PieceColor.BLACK));
          }
          break;
        case "releNum": 
          for (int i = 0; i < entry.getValue(); i++) {
            faceDownPieces
                .add(new Piece(Piece.Kind.ELEPHANT, Piece.PieceColor.RED));
          }
          break;
        case "beleNum": 
          for (int i = 0; i < entry.getValue(); i++) {
            faceDownPieces
                .add(new Piece(Piece.Kind.ELEPHANT, Piece.PieceColor.BLACK));
          }
          break;
        case "radvNum": 
          for (int i = 0; i < entry.getValue(); i++) {
            faceDownPieces
                .add(new Piece(Piece.Kind.ADVISOR, Piece.PieceColor.RED));
          }
          break;
        case "badvNum": 
          for (int i = 0; i < entry.getValue(); i++) {
            faceDownPieces
                .add(new Piece(Piece.Kind.ADVISOR, Piece.PieceColor.BLACK));
          }
          break;
        case "rgenNum": 
          for (int i = 0; i < entry.getValue(); i++) {
            faceDownPieces
                .add(new Piece(Piece.Kind.GENERAL, Piece.PieceColor.RED));
          }
          break;
        case "bgenNum": 
          for (int i = 0; i < entry.getValue(); i++) {
            faceDownPieces
                .add(new Piece(Piece.Kind.GENERAL, Piece.PieceColor.BLACK));
          }
          break;
        default:  
          for (int i = 0; i < entry.getValue(); i++) {
            faceDownPieces
                .add(new Piece(Piece.Kind.SOLDIER, Piece.PieceColor.RED));
          }
          break;
      }
    }

    // Shuffle the face down pieces.
    Random rnd = new Random();
    List<Piece> shuffledFaceDownPieces = Lists.newArrayList();
    while (!faceDownPieces.isEmpty()) {
      int index = rnd.nextInt(faceDownPieces.size());
      shuffledFaceDownPieces.add(faceDownPieces.remove(index));
    }
    
    // Reveal all face down pieces.
    // Warning: It's not the real full state
    for (Piece piece : shuffledFaceDownPieces) {
      int index = faceDownPieceIndexes.remove();
      cells.set(index, Optional.fromNullable(piece));
    }

    state.setCells(cells);

    return state;
  }

  /**
   * Make the move and return the new state.
   * 
   * @param banqiState The state before the move.
   * @param move
   * @return state The state after the move.
   */
  public BanqiState makeMove(final BanqiState banqiState, Move move) {
    
    moveCount++;
    
    BanqiState state = banqiState.copy();
    
    List<Optional<Piece>> cells = state.getCells();
    List<Piece> capturedPieces = state.getCapturedPieces();
    Position from = move.getFrom();
    Position to = move.getTo();

    // Change the state according to the move and "fullState".
    if (move.getType() == Move.Type.CAPTURE) {
      int fromIndex = convertToIndex(from.getRow(), from.getCol());
      int toIndex = convertToIndex(to.getRow(), to.getCol());
      Optional<Piece> fromPiece = cells.get(fromIndex);
      Optional<Piece> toPiece = cells.get(toIndex);
      Optional<Piece> emptyCell = Optional.fromNullable(new Piece(
          Piece.Kind.EMPTY, Piece.PieceColor.EMPTY));
      // Set the new state
      cells.set(fromIndex, emptyCell);
      cells.set(toIndex, fromPiece);
      state.setCells(cells);
      // Set the captured pieces list
      capturedPieces.add(toPiece.get());
      state.setCapturedPieces(capturedPieces);
    } else if (move.getType() == Move.Type.TURN) {
      turnCount++;
      int index = convertToIndex(from.getRow(), from.getCol());
      List<Optional<Piece>> fullCells = fullState.getCells();
      
      // Get the piece from the full state
      Piece turnPiece = fullCells.get(index).get();
      cells.set(index, Optional.fromNullable(turnPiece));
      state.setCells(cells);
    } else {
      int fromIndex = convertToIndex(from.getRow(), from.getCol());
      int toIndex = convertToIndex(to.getRow(), to.getCol());
      Optional<Piece> fromPiece = cells.get(fromIndex);
      Optional<Piece> toPiece = cells.get(toIndex);

      cells.set(fromIndex, toPiece);
      cells.set(toIndex, fromPiece);
      state.setCells(cells);
    }
    
    // Set the turn to the next player
    state.setNextTurn();
    
    return state;
  };

  // Convert the row(1-4)/col(1-8) coordinate to gameApi coodinate(0-31)
  public int convertToIndex(int row, int col) {
    return ((row - 1) * 8 + col) - 1;
  }
  
  /** Print debug info in the console. */
  public static native void console(String text)
  /*-{
      console.log(text);
  }-*/;
}
