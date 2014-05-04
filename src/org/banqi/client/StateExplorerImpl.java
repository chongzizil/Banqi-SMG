package org.banqi.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;

public class StateExplorerImpl implements StateExplorer {
  BanqiLogic banqiLogic = new BanqiLogic();

  @Override
  public Set<Move> getPossibleMoves(BanqiState state) {
    Set<Move> possibleMoves = new HashSet<Move>();
    Set<Position> possibleStartMoves = new HashSet<Position>();

    possibleStartMoves = getPossibleStartPositions(state);
    for (Position startPostion : possibleStartMoves) {
      Set<Move> possibleMovesOfP = getPossibleMovesFromPosition(state,
          startPostion);
      for (Move move : possibleMovesOfP) {
        possibleMoves.add(move);
      }
    }
    return possibleMoves;
  }

  @Override
  public Set<Move> getPossibleMovesFromPosition(BanqiState state, Position start) {
    Set<Move> moves = new HashSet<Move>();
    Set<Position> possibleMoveFromPosition = new HashSet<Position>();
    List<Optional<Piece>> cells = state.getCells();
    int startRow = start.getRow();
    int startCol = start.getCol();
    int endRow = 0;
    int endCol = 0;
    // If the piece is facing down, then there's only one move for that piece.
    if (!cells.get(convertToIndex(startRow, startCol)).isPresent()) {
      Move move = new Move(new Position(startRow, startCol));
      moves.add(move);
      return moves;
    }

    Piece piece = cells.get(convertToIndex(startRow, startCol)).get();
    /*
     * Get all possible end position for the piece can verify each of them. If
     * the piece is a cannon, then its possible end position is any square of
     * the same row or same column, otherwise is any square next to its position
     */
    if (piece.getKind() == Piece.Kind.CANNON) {
      for (int i = 1; i <= 4; i++) {
        if (i != startRow) {
          endRow = i;
          endCol = startCol;
          Position endPos = new Position(endRow, endCol);

          // If the piece is not facing down
          if (cells.get(convertToIndex(endRow, endCol)).isPresent()) {
            // If the piece is in the cell and facing up
            if (cells.get(convertToIndex(endRow, endCol)).get().getKind() != Piece.Kind.EMPTY) {
              if (!cells.get(convertToIndex(endRow, endCol)).get()
                  .getPieceColor().name().equals(piece.getPieceColor().name())
                  && banqiLogic.canCapture(cells,
                      (convertToIndex(startRow, startCol)),
                      (convertToIndex(endRow, endCol)))) {
                possibleMoveFromPosition.add(endPos);
              }
            }
          }
        }
      }
      for (int j = 1; j <= 8; j++) {
        if (j != startCol) {
          endRow = startRow;
          endCol = j;
          Position endPos = new Position(endRow, endCol);

          // If the piece is not facing down
          if (cells.get(convertToIndex(endRow, endCol)).isPresent()) {
            // If the piece is in the cell and facing up
            if (cells.get(convertToIndex(endRow, endCol)).get().getKind() != Piece.Kind.EMPTY) {
              if (!cells.get(convertToIndex(endRow, endCol)).get()
                  .getPieceColor().name().equals(piece.getPieceColor().name())
                  && banqiLogic.canCapture(cells,
                      (convertToIndex(startRow, startCol)),
                      (convertToIndex(endRow, endCol)))) {
                possibleMoveFromPosition.add(endPos);
              }
            }
          }
        }
      }
    }
    endRow = 0;
    endCol = 0;
    if (startRow != 1) {
      endRow = startRow - 1;
      endCol = startCol;
      Position up = new Position(endRow, endCol);

      // If the piece is not facing down
      if (cells.get(convertToIndex(endRow, endCol)).isPresent()) {
        // If the piece is in the cell and facing up
        if (cells.get(convertToIndex(endRow, endCol)).get().getKind() != Piece.Kind.EMPTY) {
          if (!cells.get(convertToIndex(endRow, endCol)).get().getPieceColor()
              .name().equals(piece.getPieceColor().name())
              && banqiLogic.canCapture(cells,
                  (convertToIndex(startRow, startCol)),
                  (convertToIndex(endRow, endCol)))) {
            possibleMoveFromPosition.add(up);
          }
        } else {
          possibleMoveFromPosition.add(up);
        }
      }
    }
    if (startRow != 4) {
      endRow = startRow + 1;
      endCol = startCol;
      Position down = new Position(endRow, endCol);

      // If the piece is not facing down
      if (cells.get(convertToIndex(endRow, endCol)).isPresent()) {
        // If the piece is in the cell and facing up
        if (cells.get(convertToIndex(endRow, endCol)).get().getKind() != Piece.Kind.EMPTY) {
          if (!cells.get(convertToIndex(endRow, endCol)).get().getPieceColor()
              .name().equals(piece.getPieceColor().name())
              && banqiLogic.canCapture(cells,
                  (convertToIndex(startRow, startCol)),
                  (convertToIndex(endRow, endCol)))) {
            possibleMoveFromPosition.add(down);
          }
        } else {
          possibleMoveFromPosition.add(down);
        }
      }
    }
    if (startCol != 1) {
      endRow = startRow;
      endCol = startCol - 1;
      Position left = new Position(endRow, endCol);

      // If the piece is not facing down
      if (cells.get(convertToIndex(endRow, endCol)).isPresent()) {
        // If the piece is in the cell and facing up
        if (cells.get(convertToIndex(endRow, endCol)).get().getKind() != Piece.Kind.EMPTY) {
          if (!cells.get(convertToIndex(endRow, endCol)).get().getPieceColor()
              .name().equals(piece.getPieceColor().name())
              && banqiLogic.canCapture(cells,
                  (convertToIndex(startRow, startCol)),
                  (convertToIndex(endRow, endCol)))) {
            possibleMoveFromPosition.add(left);
          }
        } else {
          possibleMoveFromPosition.add(left);
        }
      }
    }
    if (startCol != 8) {
      endRow = startRow;
      endCol = startCol + 1;
      Position right = new Position(endRow, endCol);

      // If the piece is not facing down
      if (cells.get(convertToIndex(endRow, endCol)).isPresent()) {
        // If the piece is in the cell and facing up
        if (cells.get(convertToIndex(endRow, endCol)).get().getKind() != Piece.Kind.EMPTY) {
          if (!cells.get(convertToIndex(endRow, endCol)).get().getPieceColor()
              .name().equals(piece.getPieceColor().name())
              && banqiLogic.canCapture(cells,
                  (convertToIndex(startRow, startCol)),
                  (convertToIndex(endRow, endCol)))) {
            possibleMoveFromPosition.add(right);
          }
        } else {
          possibleMoveFromPosition.add(right);
        }
      }
    }
    for (Position pos : possibleMoveFromPosition) {
      Move move = null;
      Optional<Piece> targetPiece = state.getCells().get(
          convertToIndex(pos.getRow(), pos.getCol()));
      if (targetPiece.isPresent()
          && targetPiece.get().getKind() != Piece.Kind.EMPTY) {
        move = new Move(new Position(startRow, startCol), new Position(
            pos.getRow(), pos.getCol()), Move.Type.CAPTURE);
      } else if (targetPiece.isPresent()
          && targetPiece.get().getKind() == Piece.Kind.EMPTY) {
        move = new Move(new Position(startRow, startCol), new Position(
            pos.getRow(), pos.getCol()), Move.Type.MOVE);
      }
      moves.add(move);
    }
    return moves;
  }

  @Override
  public Set<Position> getPossibleStartPositions(BanqiState state) {
    Set<Position> startPositions = new HashSet<Position>();
    List<Optional<Piece>> cells = state.getCells();
    Color turnOfColor = state.getTurn();
    // Check all pieces on the board
    for (int i = 0; i < 32; i++) {
      int row = (i / 8) + 1;
      int col = (i % 8) + 1;
      Position currentPos = new Position(row, col);
      Optional<Piece> cell = cells.get(i);

      if (cell.isPresent() && cell.get().getKind() != Piece.Kind.EMPTY) {
        Piece piece = cell.get();
        if (piece.getPieceColor().name().substring(0, 1)
            .equals(turnOfColor.toString())) {
          if (!getPossibleMovesFromPosition(state, currentPos).isEmpty()) {
            startPositions.add(currentPos);
          }
        }
      } else if (!cell.isPresent()) {
        startPositions.add(currentPos);
      }
    }

    return startPositions;
  }

  // Convert the gameApi coodinate(0-31) to row(1-4)/col(1-8) coordinate
  public Position convertToCoord(int cellIndex) {
    int row = cellIndex / 8 + 1;
    int col = cellIndex % 8 + 1;
    return new Position(row, col);
  }

  // Convert the row(1-4)/col(1-8) coordinate to gameApi coodinate(0-31)
  public int convertToIndex(int row, int col) {
    return ((row - 1) * 8 + col) - 1;
  }

  public List<Integer> convertFromPosToIndex(
      Set<Position> possibleStartPositions) {
    List<Integer> possibleStartIndexOfCells = new ArrayList<Integer>();
    for (Position pos : possibleStartPositions) {
      int row = pos.getRow();
      int col = pos.getCol();
      int index = convertToIndex(row, col);
      possibleStartIndexOfCells.add(index);
    }
    return possibleStartIndexOfCells;
  }
}
