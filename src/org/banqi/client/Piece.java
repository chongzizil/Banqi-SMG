package org.banqi.client;

import java.util.Arrays;

public class Piece extends Equality {
  public enum Kind {
    GENERAL,  //GEN
    ADVISOR,  //ADV
    ELEPHANT, //ELE
    CHARIOT,  //CHA
    HORSE,    //HOR
    CANNON,   //CAN
    SOLDIER;  //SOL
    
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

  public enum PieceColor {
    RED,
    BLACK;
    
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
  
  private PieceColor colorValue;
  private Kind kindValue;

  
  /**
   * Creates a new playing piece.
   *
   * @param kind the kind value of this card.
   * @param color the rank value of this card.
   */
  public Piece(PieceColor color, Kind kind) {
    kindValue = kind;
    colorValue = color;
  }

  /**
   * Returns the color of the piece.
   *
   * @return a PieceColor constant representing the color value of the piece.
   */
  public PieceColor getColor() {
    return colorValue;
  }
  
  /**
   * Returns the kind of the piece.
   *
   * @return a Kind constant representing the kind value of the piece.
   */
  public Kind getKind() {
    return kindValue;
  }
  
  /**
   * Returns a description of this piece.
   *
   * @return the name of the piece.
   */
  @Override
  public String toString() {
    return colorValue.toString() + " " + kindValue.toString();
  }

  @Override
  public Object getId() {
    return Arrays.asList(getColor(), getKind());
  }
}
