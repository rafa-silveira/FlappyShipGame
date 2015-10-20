package com.vanderhaegen.flappyship.game;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Pickup {
    static final int STAR = 1;
    static final int SHIELD = 2;
    static final int FUEL = 3;

    TextureRegion pickupTexture;
    TextureAtlas atlas;
    Vector2 pickupPosition;
    int pickupType, pickupValue;


    public Pickup(int type, TextureAtlas atlas) {
        this.atlas = atlas;
        pickupPosition = new Vector2();
        pickupType = type;
        defType(pickupType);
    }

    private void defType(int type) {
        switch (type) {
            case STAR:
                pickupTexture = atlas.findRegion("star_pickup");
                pickupValue = 3;
                break;
            case SHIELD:
                pickupTexture = atlas.findRegion("shield_pickup");
                pickupValue = 16;
                break;
            case FUEL:
                pickupTexture = atlas.findRegion("fuel_pickup");
                pickupValue = 100;
                break;
        }

    }

}
