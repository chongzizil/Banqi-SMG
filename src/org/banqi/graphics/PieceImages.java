// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// //////////////////////////////////////////////////////////////////////////////

package org.banqi.graphics;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface PieceImages extends ClientBundle {
  @Source("images/pieces/back.gif")
  ImageResource back();
  
  @Source("images/pieces/black_general.gif")
  ImageResource bgen();

  @Source("images/pieces/red_general.gif")
  ImageResource rgen();

  @Source("images/pieces/black_advisor.gif")
  ImageResource badv();

  @Source("images/pieces/red_advisor.gif")
  ImageResource radv();

  @Source("images/pieces/black_elephant.gif")
  ImageResource bele();

  @Source("images/pieces/red_elephant.gif")
  ImageResource rele();

  @Source("images/pieces/black_chariot.gif")
  ImageResource bcha();

  @Source("images/pieces/red_chariot.gif")
  ImageResource rcha();

  @Source("images/pieces/black_horse.gif")
  ImageResource bhor();

  @Source("images/pieces/red_horse.gif")
  ImageResource rhor();

  @Source("images/pieces/black_cannon.gif")
  ImageResource bcan();

  @Source("images/pieces/red_cannon.gif")
  ImageResource rcan();

  @Source("images/pieces/black_soldier.gif")
  ImageResource bsol();

  @Source("images/pieces/red_soldier.gif")
  ImageResource rsol();
}
