package org.banqi.client;

import java.util.Arrays;

public class Piece extends Equality {
  /**
   * There're totally 9 kinds which 7 of them are normal kinds of pieces and 1 kind:
   * EMPTY refers to nonexists piece in a cell.
   */
  public enum Kind {
    GENERAL,  //GEN
    ADVISOR,  //ADV
    ELEPHANT, //ELE
    CHARIOT,  //CHA
    HORSE,    //HOR
    CANNON,   //CAN
    SOLDIER,  //SOL
    EMPTY;    //EMP
    
    private static final Kind[] VALUES = values();
    
    public static Kind fromFirstThreeLetterLowerCase(String firstThreeLetterLowerCase) {
      for (Kind kind : VALUES) {
        if (kind.getFirstThreeLetterLowerCase().equals(firstThreeLetterLowerCase)) {
          return kind;
        }
      }
      throw new IllegalArgumentException("Did not find firstThreeLetterLowerCase="
          + firstThreeLetterLowerCase);
    } 
  
    public String getFirstThreeLetterLowerCase() {
      return name().substring(0, 3).toLowerCase();
    }
  }

  /**
   * Since the game only supports two players, so there're only two colors.
   */
  public enum PieceColor {
    RED,
    BLACK,
    EMPTY;
    
    private static final PieceColor[] VALUES = values();
    
    public static PieceColor fromFirstLetterLowerCase(String firstLetterLowerCase) {
      for (PieceColor pieceColor : VALUES) {
        if (pieceColor.getFirstLetterLowerCase().equals(firstLetterLowerCase)) {
          return pieceColor;
        }
      }
      throw new IllegalArgumentException("Did not find firstLetterLowerCase="
        + firstLetterLowerCase);
    }
    
    public String getFirstLetterLowerCase() {
      return name().substring(0, 1).toLowerCase();
    }
  }
  
  private Kind kindValue;
  private PieceColor pieceColorValue;
  
  /**
   * Creates a piece.
   *
   * @param kind the kind value of this piece.
   * @param pieceColor the color value of this piece.
   */
  public Piece(Kind kind, PieceColor pieceColor) {
    kindValue = kind;
    pieceColorValue = pieceColor;
  }
  
  /**
   * Creates a piece.
   *
   * @param kind the kind value of this piece.
   */
  public Piece(Kind kind) {
    kindValue = kind;
    pieceColorValue = PieceColor.EMPTY;
  }

  /**
   * Returns the Kind of the piece.
   *
   * @return a Kind constant representing the kind value of the piece.
   */
  public Kind getKind() {
    return kindValue;
  }
  
  /**
   * Returns the PieceColor of the piece.
   *
   * @return a PieceColor constant representing the color value of the piece.
   */
  public PieceColor getPieceColor() {
    return pieceColorValue;
  }
  
  /**
   * Returns the four letters string of the piece.
   *
   * @return a string representing the value of the piece.
   */
  public String getPieceFourLetterString() {
    return pieceColorValue.getFirstLetterLowerCase() + kindValue.getFirstThreeLetterLowerCase();
  }
  
  /**
   * Returns a description of this piece.
   *
   * @return the name of the piece.
   */
  @Override
  public String toString() {
    return pieceColorValue.toString() + " " + kindValue.toString();
  }

  @Override
  public Object getId() {
    return Arrays.asList(getPieceColor(), getKind());
  }
}
