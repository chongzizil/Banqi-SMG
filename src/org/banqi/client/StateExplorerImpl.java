package org.banqi.client;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class StateExplorerImpl implements StateExplorer {
  BanqiLogic banqiLogic = new BanqiLogic();
  
  @Override
  public Set<MovePiece> getPossibleMoves(State state) {
    Set<MovePiece> possibleMoves = new HashSet<MovePiece>();
    Set<Position> possibleStartMoves = new HashSet<Position>();
    possibleStartMoves = getPossibleStartPositions(state);
    for (Position p: possibleStartMoves) {
      Set<MovePiece> possibleMovesOfP = getPossibleMovesFromPosition(state, p);
      for (MovePiece move: possibleMovesOfP) {
        possibleMoves.add(move);
      }
    }
    return possibleMoves;
  }

  @Override
  public Set<MovePiece> getPossibleMovesFromPosition(State state, Position start) {
    Set<MovePiece> moves = new HashSet<MovePiece>();
    Set<Position> possibleMoveFromPosition = new HashSet<Position>();
    ImmutableList<Optional<Piece>> pieces = state.getPieces();
    ImmutableList<Optional<Integer>> squares = state.getSquares();
    int startRow = start.getRow();
    int startCol = start.getCol();
    int endRow = 0;
    int endCol = 0;
    Piece piece = pieces.get(squares.get(convertCoord(startRow, startCol)).get()).get();
    /* 
     * Get all possible end position for the piece can verify each of them.
     * If the piece is a cannon, then its possible end position is any square
     * of the same row or same column, otherwise is any square next to its position
     */
    if (piece.getKind().name().equals("CANNON")) {
      for (int i = 1; i <= 4; i++) {
        if (i != startRow) {
          endRow = i;
          endCol = startCol;
          Position endPos = new Position(endRow, endCol);
          // If there's a piece in the end position
          if (squares.get(convertCoord(endRow, endCol)).isPresent()) {
            // If the piece is facing up, check if the capture can be perform
            if (pieces.get(squares.get(convertCoord(endRow, endCol)).get()).isPresent()) {
              if (banqiLogic.canCapture(pieces, squares,
                  (convertCoord(startRow, startCol)), (convertCoord(endRow, endCol)))) {
                possibleMoveFromPosition.add(endPos);
              }
            }
          } else if (endRow == startRow - 1 || endRow == startRow + 1) {
            possibleMoveFromPosition.add(endPos);
          }
        }
      }
      for (int j = 1; j <= 8; j++) {
        if (j != startCol) {
        endRow = startRow;
        endCol = j;
        Position endPos = new Position(endRow, endCol);
        // If there's a piece in the end position
        if (squares.get(convertCoord(endRow, endCol)).isPresent()) {
          // If the piece is facing up, check if the capture can be perform
          if (pieces.get(squares.get(convertCoord(endRow, endCol)).get()).isPresent()) {
            if (banqiLogic.canCapture(pieces, squares,
                (convertCoord(startRow, startCol)), (convertCoord(endRow, endCol)))) {
              possibleMoveFromPosition.add(endPos);
            }
          }
        } else if (endCol == startCol - 1 || endCol == startCol + 1) {
          possibleMoveFromPosition.add(endPos);
          }
        }
      }
    } else {
      endRow = 0;
      endCol = 0;
      if (startRow != 1) {
        endRow = startRow - 1;
        endCol = startCol;
        Position up = new Position(endRow, endCol);
        // If there's a piece in the end position
        if (squares.get(convertCoord(endRow, endCol)).isPresent()) {
          // If the piece is facing up, check if the capture can be perform
          if (pieces.get(squares.get(convertCoord(endRow, endCol)).get()).isPresent()) {
            if (banqiLogic.canCapture(pieces, squares,
                (convertCoord(startRow, startCol)), (convertCoord(endRow, endCol)))) {
              possibleMoveFromPosition.add(up);
            }
          }
        } else {
          possibleMoveFromPosition.add(up);
        }
      }
      if (startRow != 4) {
        endRow = startRow + 1;
        endCol = startCol;
        Position down = new Position(endRow, endCol);
        // If there's a piece in the end position
        if (squares.get(convertCoord(endRow, endCol)).isPresent()) {
          // If the piece is facing up, check if the capture can be perform
          if (pieces.get(squares.get(convertCoord(endRow, endCol)).get()).isPresent()) {
            if (banqiLogic.canCapture(pieces, squares,
                (convertCoord(startRow, startCol)), (convertCoord(endRow, endCol)))) {
              possibleMoveFromPosition.add(down);
            }
          }
        } else {
          possibleMoveFromPosition.add(down);
        }
      }
      if (startCol != 1) {
        endRow = startRow;
        endCol = startCol - 1;
        Position left = new Position(endRow, endCol);
        // If there's a piece in the end position
        if (squares.get(convertCoord(endRow, endCol)).isPresent()) {
          // If the piece is facing up, check if the capture can be perform
          if (pieces.get(squares.get(convertCoord(endRow, endCol)).get()).isPresent()) {
            if (banqiLogic.canCapture(pieces, squares,
                (convertCoord(startRow, startCol)), (convertCoord(endRow, endCol)))) {
              possibleMoveFromPosition.add(left);
            }
          }
        } else {
          possibleMoveFromPosition.add(left);
        }
      }
      if (startCol != 8) {
        endRow = startRow;
        endCol = startCol + 1;
        Position right = new Position(endRow, endCol);
        // If there's a piece in the end position
        if (squares.get(convertCoord(endRow, endCol)).isPresent()) {
          // If the piece is facing up, check if the capture can be perform
          if (pieces.get(squares.get(convertCoord(endRow, endCol)).get()).isPresent()) {
            if (banqiLogic.canCapture(pieces, squares,
                (convertCoord(startRow, startCol)), (convertCoord(endRow, endCol)))) {
              possibleMoveFromPosition.add(right);
            }
          }
        } else {
          possibleMoveFromPosition.add(right);
        }
      } 
    }
    for (Position pos: possibleMoveFromPosition) {
      MovePiece move = new MovePiece(convertCoord(startRow, startCol),
          convertCoord(pos.getRow(), pos.getCol()));
      moves.add(move);
    }
    return moves;
  }
  
  @Override
  public Set<Position> getPossibleStartPositions(State state) {
    Set<Position> startPositions = new HashSet<Position>();
    ImmutableList<Optional<Piece>> pieces = state.getPieces();
    ImmutableList<Optional<Integer>> squares = state.getSquares();
    Color turnOfColor = state.getTurn();
    // Check all pieces on the board
    for (Optional<Piece> piece: pieces) {
      // If a piece is on board and the color is same as the turn of
      // color, than continue to check if it can make move
      if (piece.isPresent() && piece.get().getColor().name().substring(0, 1).
          equals(turnOfColor.toString())) {
        int piecePos = squares.get(pieces.indexOf(piece)).get();
        int row = (piecePos / 8) + 1;
        int col = (piecePos % 8) + 1;
        Position currentPos = new Position(row, col);
        if (!getPossibleMovesFromPosition(state, currentPos).isEmpty()) {
          startPositions.add(currentPos); 
        }
      }
    }
    return startPositions;
  }
  
  // Convert the row(1-4)/col(1-8) coordinate to gameApi coodinate(0-31)
  public int convertCoord(int row, int col) {
    return ((row - 1) * 8 + col) - 1;
  }
}
