package org.banqi.graphics;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface BanqiImages extends ClientBundle {
  
  // Back Piece images
  @Source("images/pieces/back.png")
  ImageResource back();
  
  // Normal Piece images
  @Source("images/pieces/black_general.png")
  ImageResource bgen();

  @Source("images/pieces/red_general.png")
  ImageResource rgen();

  @Source("images/pieces/black_advisor.png")
  ImageResource badv();

  @Source("images/pieces/red_advisor.png")
  ImageResource radv();

  @Source("images/pieces/black_elephant.png")
  ImageResource bele();

  @Source("images/pieces/red_elephant.png")
  ImageResource rele();

  @Source("images/pieces/black_chariot.png")
  ImageResource bcha();

  @Source("images/pieces/red_chariot.png")
  ImageResource rcha();

  @Source("images/pieces/black_horse.png")
  ImageResource bhor();

  @Source("images/pieces/red_horse.png")
  ImageResource rhor();

  @Source("images/pieces/black_cannon.png")
  ImageResource bcan();

  @Source("images/pieces/red_cannon.png")
  ImageResource rcan();

  @Source("images/pieces/black_soldier.png")
  ImageResource bsol();

  @Source("images/pieces/red_soldier.png")
  ImageResource rsol();
  
  // HL Piece images
  @Source("images/pieces/black_general_hl.png")
  ImageResource bgenHL();

  @Source("images/pieces/red_general_hl.png")
  ImageResource rgenHL();

  @Source("images/pieces/black_advisor_hl.png")
  ImageResource badvHL();

  @Source("images/pieces/red_advisor_hl.png")
  ImageResource radvHL();

  @Source("images/pieces/black_elephant_hl.png")
  ImageResource beleHL();

  @Source("images/pieces/red_elephant_hl.png")
  ImageResource releHL();

  @Source("images/pieces/black_chariot_hl.png")
  ImageResource bchaHL();

  @Source("images/pieces/red_chariot_hl.png")
  ImageResource rchaHL();

  @Source("images/pieces/black_horse_hl.png")
  ImageResource bhorHL();

  @Source("images/pieces/red_horse_hl.png")
  ImageResource rhorHL();

  @Source("images/pieces/black_cannon_hl.png")
  ImageResource bcanHL();

  @Source("images/pieces/red_cannon_hl.png")
  ImageResource rcanHL();

  @Source("images/pieces/black_soldier_hl.png")
  ImageResource bsolHL();

  @Source("images/pieces/red_soldier_hl.png")
  ImageResource rsolHL();
  
  // Target HL Piece images
  @Source("images/pieces/black_general_target_hl.png")
  ImageResource bgenTargetHL();

  @Source("images/pieces/red_general_target_hl.png")
  ImageResource rgenTargetHL();

  @Source("images/pieces/black_advisor_target_hl.png")
  ImageResource badvTargetHL();

  @Source("images/pieces/red_advisor_target_hl.png")
  ImageResource radvTargetHL();

  @Source("images/pieces/black_elephant_target_hl.png")
  ImageResource beleTargetHL();

  @Source("images/pieces/red_elephant_target_hl.png")
  ImageResource releTargetHL();

  @Source("images/pieces/black_chariot_target_hl.png")
  ImageResource bchaTargetHL();

  @Source("images/pieces/red_chariot_target_hl.png")
  ImageResource rchaTargetHL();

  @Source("images/pieces/black_horse_target_hl.png")
  ImageResource bhorTargetHL();

  @Source("images/pieces/red_horse_target_hl.png")
  ImageResource rhorTargetHL();

  @Source("images/pieces/black_cannon_target_hl.png")
  ImageResource bcanTargetHL();

  @Source("images/pieces/red_cannon_target_hl.png")
  ImageResource rcanTargetHL();

  @Source("images/pieces/black_soldier_target_hl.png")
  ImageResource bsolTargetHL();

  @Source("images/pieces/red_soldier_target_hl.png")
  ImageResource rsolTargetHL();
  
  // Empty cell
  @Source("images/other/empty_cell.gif")
  ImageResource eemp();
  
  @Source("images/other/empty_cell_target_hl.gif")
  ImageResource eempTargetHL();
  
  // Banqi board
  @Source("images/other/board.gif")
  ImageResource board();
}
