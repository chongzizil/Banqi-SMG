package org.banqi.sounds;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;

public interface GameSounds extends ClientBundle {

        @Source("org/banqi/sounds/pieceCaptured.mp3")
        DataResource pieceCapturedMp3();

        @Source("org/banqi/sounds/pieceCaptured.wav")
        DataResource pieceCapturedWav();

        @Source("org/banqi/sounds/pieceDown.mp3")
        DataResource pieceDownMp3();

        @Source("org/banqi/sounds/pieceDown.wav")
        DataResource pieceDownWav();
        
}