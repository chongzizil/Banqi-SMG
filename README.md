# Banqi/Half Chess/半棋/暗棋

## Overview

This game is a Social Multiplayer Gaming project which mainly use GWT. <br/>
The description and rules of this game can be found at [wikipedia] (http://en.wikipedia.org/wiki/Banqi).

![ScreenShot](/sample.jpg)

## Versions

### [1.2.0] (http://1-2-0.banqi-smg.appspot.com/)
- HW4 revised (HW4 version is not deployed anymore)
- Added graphics


### [1.7.0] (http://1-7-0.banqi-smg.appspot.com/)
- HW7
- Added animation
- Added drag and drop

### [2.0.1] (http://2-0-1-dot-banqi-smg.appspot.com/)
- HW7 refactored
- Added animation for the opponent
- Changed all the images
- Improved performance
- Fixed bugs

### [2.1] (http://2-1-dot-banqi-smg.appspot.com/)
- HW10 first deploy
- Changed to mobile UI
- Standalone, no need to use emulator for this version
- Need to play horizontally, tested in iPhone 5s.
- Known bugs:
  - No sound
  - Popup error during the first move and drag and drop

### [2.9] (http://2-9-dot-banqi-smg.appspot.com/)
- HW9 and HW10
- For testing or playing without emulator : [2.8] (http://2-8-dot-banqi-smg.appspot.com/)
- Since there's really no string for i18n, so i18n is not implemented...
- Auto resize to fit the browser.
- Auto change to landscape if the browser's width is less than the game's, in another word it will only effects in mobile phone. (Tested in iPhone 5s...)
- Change the sound of capture piece to a more suitable one, except for the cannon which still booooms...
- Known bugs:
  - Due to the bug of gwt-dnd, drag and drop won't work... (Yet it is not deleted)
  - No sound in mobile device.
  - Popup error during the first move and drag and drop (possiblely caused by the sound...), yet the move is valid and everything back to normal after...


## Instruction

### How to play

For the version later than 1.2.0, you need either the emulator or the container to start play.

#### Use the emulator
- [GWT-Emulator] (http://smg-gwt-emulator.appspot.com/GwtEmulator.html)
- [AngularJS-Emulator] (http://4.smg-angularjs-emulator.appspot.com/)

Warning: The animation can not be properly performed in the latest version (V4 & V3) of GWT-Emulator.

#### Use the container
- [GWT-Container] (http://5-dot-smg-gwt-container.appspot.com/demo.html)
- [AngularJS-Container] (http://smg-angularjs-container.appspot.com/#/) (My SMG team project, check [here] (https://github.com/chongzizil/SMG-AngularJS-Container) :) )

## Thanks
- Professor Yoav
- Grader ashishmanral
- Joyce Huang (Thank you for all the images you created for this game, my love :) )
