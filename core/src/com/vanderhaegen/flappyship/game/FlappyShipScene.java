package com.vanderhaegen.flappyship.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.vanderhaegen.flappyship.FlappyShip;


public class FlappyShipScene extends BaseScene {
    private static final Vector2 DAMPING = new Vector2(0.99f, 0.99f);
    private static final int TOUCH_IMPULSE = 370;
    private static final float TAP_DRAW_TIME_MAX = 0.25f;
    private static final int METEOR_SPEED = 20;

    private SpriteBatch batch;
    private OrthographicCamera camera;

    private Array<Vector2> pillars;
    private TextureRegion bgRegion, plane, terrainBelow, terrainAbove, tapIndicator, getReady,
            gameOver, pillarUp, pillarDown, selectedMeteorTexture;
    private Texture fuelIndicator;
    private float terrainOffset, planeAnimTime, tapDrawTime, deltaPosition, nextMeteorIn,
            shieldCount, fuelCount, score;

    private Vector2 planeVelocity, planePosition, planeDefaultPosition, gravity, scrollVelocity,
            tmpVector, lastPillarPosition, meteorPosition, meteorVelocity;
    private Vector3 touchPosition, pickupTiming;
    private Animation plane2, shield;

    private Rectangle planeRect, obstacleRect;
    private Array<AtlasRegion> meteorTextures;
    private boolean meteorInScene;
    private Pickup tempPickup;
    private Array<Pickup> pickupsInScene;

    private int starCount, fuelPercent;

    private ParticleEffect smoke, explosion;
    private ParticleEffectPool bombPool;
    private Array<ParticleEffectPool.PooledEffect> effects;

    public FlappyShipScene(FlappyShip flappyShip) {
        super(flappyShip);

        batch = game.batch;
        camera = game.camera;

        bgRegion = atlas.findRegion("background");
        planeVelocity = new Vector2();
        planePosition = new Vector2();
        planeDefaultPosition = new Vector2();
        gravity = new Vector2();
        scrollVelocity = new Vector2();
        tmpVector = new Vector2();
        lastPillarPosition = new Vector2();
        meteorPosition = new Vector2();
        meteorVelocity = new Vector2();
        touchPosition = new Vector3();
        pickupTiming = new Vector3();
        nextMeteorIn = 5;
        planeRect = new Rectangle();
        obstacleRect = new Rectangle();
        meteorInScene = true;

        pillars = new Array<Vector2>();
        pickupsInScene = new Array<Pickup>();
        effects = new Array<ParticleEffectPool.PooledEffect>();

        terrainBelow = atlas.findRegion("groundIce");
        terrainAbove = new TextureRegion(terrainBelow);
        terrainAbove.flip(true, true);

        tapIndicator = atlas.findRegion("tap2");
        getReady = atlas.findRegion("tapLeft");
        gameOver = atlas.findRegion("textGameOver");

        pillarUp = atlas.findRegion("pillarUp");
        pillarDown = atlas.findRegion("pillarDown");

        plane2 = new Animation(0.06f, atlas.findRegion("fire1"), atlas.findRegion("fire2"),
                atlas.findRegion("fire3"), atlas.findRegion("fire4"));
        plane = atlas.findRegion("playerShip");

        meteorTextures = new Array<AtlasRegion>();
        meteorTextures.add(atlas.findRegion("meteor1"));
        meteorTextures.add(atlas.findRegion("meteor2"));
        meteorTextures.add(atlas.findRegion("meteor3"));

        gameState = GameState.INIT;
        fuelIndicator = manager.get("fuelIndicator.png", Texture.class);
        shield = new Animation(0.06f, atlas.findRegion("shield1"), atlas.findRegion("shield2"), atlas.findRegion("shield3"));

        smoke = manager.get("particles/Smoke", ParticleEffect.class);
        explosion = manager.get("particles/Explosion", ParticleEffect.class);
        bombPool = new ParticleEffectPool(explosion, 1, 3);

        resetScene();
    }

    private void resetScene() {
        terrainOffset = 0;
        planeAnimTime = 0;
        planeVelocity.set(50, 0);
        gravity.set(0, -1.3f);
        scrollVelocity.set(2, 0);
        planeDefaultPosition.set(250 - 88 / 2, 240 - 73 / 2);
        planePosition.set(planeDefaultPosition.x, planeDefaultPosition.y);

        pillars.clear();
        addPillar();
        meteorInScene = false;
        nextMeteorIn = (float) (Math.random() * 5);
        music.stop();
        if (game.soundEnabled) {
            music.play();
        } else {
            game.soundVolume = 0f;
        }

        pickupTiming.set(1, 3, 10);
        fuelPercent = 91;
        fuelCount = 100;
        shieldCount = 15;
        starCount = 0;
        score = 0;
        smoke.reset();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render(delta);

        updateScene(delta);
        drawScene(delta);
    }

    private void updateScene(float delta) {
        if (Gdx.input.justTouched()) {
            if (gameState == GameState.INIT) {
                gameState = GameState.ACTION;
                return;
            }
            if (gameState == GameState.GAME_OVER && !scorDia.isVisible()) {
                gameState = GameState.INIT;
                resetScene();
                return;
            }
            if (fuelCount < 0 || gameState == GameState.PAUSE) {
                return;
            }
            touchPosition.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPosition);
            tmpVector.set(planePosition.x, planePosition.y);
            tmpVector.sub(touchPosition.x, touchPosition.y).nor();
            planeVelocity.mulAdd(tmpVector, TOUCH_IMPULSE - MathUtils.clamp(
                    Vector2.dst(touchPosition.x, touchPosition.y, planePosition.x, planePosition.y), 0, TOUCH_IMPULSE));
            tapDrawTime = TAP_DRAW_TIME_MAX;
            tapSound.play(game.soundVolume);
        }
        tapDrawTime -= delta;
        planeAnimTime += delta;

        if (gameState == GameState.INIT || gameState == GameState.PAUSE) {
            return;
        }

        planeVelocity.scl(DAMPING);
        planeVelocity.add(gravity);
        planeVelocity.add(scrollVelocity);
        planePosition.mulAdd(planeVelocity, delta);

        if (gameState == GameState.GAME_OVER) {
            return;
        }

        deltaPosition = planePosition.x - planeDefaultPosition.x;

//        Teto, movimento e colisao
        terrainOffset -= planePosition.x - planeDefaultPosition.x;
        planePosition.x = planeDefaultPosition.x;

        if (terrainOffset * -1 > terrainBelow.getRegionWidth()) {
            terrainOffset = 0;
        }
        if (terrainOffset > 0) {
            terrainOffset = -terrainBelow.getRegionWidth();
        }
        if (planePosition.y < terrainBelow.getRegionHeight() - 70 ||
                planePosition.y + 73 > 480 - terrainBelow.getRegionHeight() + 70) {
            endGame();
        }

//        Pilares, movimento, colisao e lancamento
        planeRect.set(planePosition.x + 30, planePosition.y, 35, 30);
        for (Vector2 vec : pillars) {
            vec.x -= deltaPosition;
            if (vec.x + pillarUp.getRegionWidth() < -10) {
                pillars.removeValue(vec, false);
            }
            if (vec.y == 1) {
                obstacleRect.set(vec.x + 50, 0, pillarUp.getRegionWidth() - 120, pillarUp.getRegionHeight() - 20);
            } else {
                obstacleRect.set(vec.x + 50, 480 - pillarDown.getRegionHeight() + 20,
                        pillarUp.getRegionWidth() - 120, pillarUp.getRegionHeight());
            }
            if (planeRect.overlaps(obstacleRect) && shieldCount <= 0) {
                endGame();
            }
        }
        if (lastPillarPosition.x < 400) {
            addPillar();
        }

//        Meteoros, movimento, colisao e lancamento
        if (meteorInScene) {
            meteorPosition.mulAdd(meteorVelocity, delta);
            meteorPosition.x -= deltaPosition;
            if (meteorPosition.x < -10) {
                meteorInScene = false;
            }
            obstacleRect.set(meteorPosition.x + 10, meteorPosition.y + 10, 10, 20);
            if (planeRect.overlaps(obstacleRect) && shieldCount <= 0) {
                endGame();
            }
        }
        nextMeteorIn -= delta;
        if (nextMeteorIn <= 0) {
            launchMeteor();
        }

//        Pickup, lancamento, movimento e colisao
        checkAndCreatePickup(delta);
        for (Pickup pic : pickupsInScene) {
            pic.pickupPosition.x -= deltaPosition;
            if (pic.pickupPosition.x < -20) {
                pickupsInScene.removeValue(pic, false);
            }
            obstacleRect.set(pic.pickupPosition.x + 10, pic.pickupPosition.y + 10, 50, 20);
            if (planeRect.overlaps(obstacleRect)) {
                pickItUp(pic);
            }
        }

//        Combustivel, porcentagem, escudo e pontos em controle
        fuelCount -= 2.9 * delta;
        fuelPercent = (int) (91 * fuelCount / 100);
        if (shieldCount > 0) {
            shieldCount -= 1.5 * delta;
        }
        score += 0.9f * delta;

    }

    private void drawScene(float delta) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.disableBlending();
        batch.draw(bgRegion, 0, 0);
        batch.enableBlending();

        for (Vector2 vec : pillars) {
            if (vec.y == 1) {
                batch.draw(pillarUp, vec.x, 0);
            } else {
                batch.draw(pillarDown, vec.x, 480 - pillarDown.getRegionHeight());
            }
        }

        for (Pickup pic : pickupsInScene) {
            batch.draw(pic.pickupTexture, pic.pickupPosition.x, pic.pickupPosition.y, 40, 35);
        }

        batch.draw(terrainBelow, terrainOffset, 0);
        batch.draw(terrainBelow, terrainOffset + terrainBelow.getRegionWidth(), 0);
        batch.draw(terrainAbove, terrainOffset, 480 - terrainAbove.getRegionHeight());
        batch.draw(terrainAbove, terrainOffset + terrainAbove.getRegionWidth(), 480 - terrainAbove.getRegionHeight());

        batch.setColor(Color.BLACK);
        batch.draw(fuelIndicator, 10, 350, 0, 0, 91, 91);
        batch.setColor(Color.WHITE);
        batch.draw(fuelIndicator, 10, 350, 0, 0, fuelPercent, 91);

        font.draw(batch, "" + (int) (starCount + score), camera.viewportWidth - 60, camera.viewportHeight - 30);

        if (shieldCount > 0) {
            font.draw(batch, "" + (int) shieldCount, camera.viewportWidth / 2, camera.viewportHeight - 30);
            batch.draw(shield.getKeyFrame(planeAnimTime, true), planePosition.x - 35, planePosition.y - 25);
        }

        if (tapDrawTime > -0.30f) {
            batch.draw(plane2.getKeyFrame(planeAnimTime, true), planePosition.x - 20, planePosition.y + 10);
        }
        batch.draw(plane, planePosition.x, planePosition.y, 70, 45);

        //        Particulas, desenhar
        for (ParticleEffectPool.PooledEffect ef : effects) {
            ef.setEmittersCleanUpBlendFunction(false);
            ef.draw(batch, delta);
            if (ef.isComplete()) {
                ef.free();
                effects.removeValue(ef, true);
            }
        }
        if (fuelPercent < 50) {
            smoke.setPosition(planePosition.x + 15, planePosition.y + 10);
            smoke.setEmittersCleanUpBlendFunction(false);
            smoke.draw(batch, delta);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (tapDrawTime > 0) {
            batch.draw(tapIndicator, touchPosition.x - 29.5f, touchPosition.y - 29.5f);
        }
        if (meteorInScene) {
            batch.draw(selectedMeteorTexture, meteorPosition.x, meteorPosition.y);
        }
        if (gameState == GameState.INIT) {
            batch.draw(getReady, camera.viewportWidth / 2, planePosition.y - 80);
        }
        if (gameState == GameState.GAME_OVER) {
            batch.draw(gameOver, camera.viewportWidth / 3.9f, camera.viewportHeight / 2);
        }
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    private void endGame() {
        if (gameState != GameState.GAME_OVER) {
            crashSound.play(game.soundVolume);
            gameState = GameState.GAME_OVER;
            ParticleEffectPool.PooledEffect eff = bombPool.obtain();
            eff.setPosition(planePosition.x + 40, planePosition.y + 40);
            effects.add(eff);
            eff.reset();
        }
        checkStoreScore((int) (starCount + score));
    }


    protected void addPillar() {
        Vector2 pillarPosition = new Vector2();
        if (pillars.size == 0) {
            pillarPosition.x = (float) (800 + Math.random() * 600);
        } else {
            pillarPosition.x = lastPillarPosition.x + (float) (600 + Math.random() * 600);
        }
        if (MathUtils.randomBoolean()) {
            pillarPosition.y = 1;
        } else {
            pillarPosition.y = -1;
        }
        lastPillarPosition = pillarPosition;
        pillars.add(pillarPosition);
    }

    protected void launchMeteor() {
        spawnSound.play(game.soundVolume);
        nextMeteorIn = (float) (1.5 + Math.random() * 5);
        if (meteorInScene) {
            return;
        }
        meteorInScene = true;
        int id = (int) (Math.random() * meteorTextures.size);
        selectedMeteorTexture = meteorTextures.get(id);
        meteorPosition.x = 810;
        meteorPosition.y = (float) (80 + Math.random() * 320);
        Vector2 destination = new Vector2();
        destination.x = -10;
        destination.y = (float) (Math.random() * 320);
        destination.sub(meteorPosition).nor();
        meteorVelocity.mulAdd(destination, METEOR_SPEED);
    }

    protected void checkAndCreatePickup(float delta) {
        pickupTiming.sub(delta);
        if (pickupTiming.x <= 0) {
            pickupTiming.x = (float) (0.5 + Math.random() * 0.5);
            if (addPickup(Pickup.STAR)) {
                pickupTiming.x = 1 + (float) (Math.random() * 2);
            }
        }
        if (pickupTiming.y <= 0) {
            pickupTiming.y = (float) (0.5 + Math.random() * 0.5);
            if (addPickup(Pickup.FUEL))
                pickupTiming.y = 3 + (float) (Math.random() * 2);
        }
        if (pickupTiming.z <= 0) {
            pickupTiming.z = (float) (0.5 + Math.random() * 0.5);
            if (addPickup(Pickup.SHIELD))
                pickupTiming.z = 10 + (float) (Math.random() * 3);
        }
    }

    protected boolean addPickup(int pickupType) {
        Vector2 randomPosition = new Vector2();
        randomPosition.x = 820;
        randomPosition.y = (float) (80 + Math.random() * 320);
        for (Vector2 vec : pillars) {
            if (vec.y == 1) {
                obstacleRect.set(vec.x, 0, pillarUp.getRegionWidth(), pillarUp.getRegionHeight());
            } else {
                obstacleRect.set(vec.x, 480 - pillarDown.getRegionHeight(),
                        pillarUp.getRegionWidth(), pillarUp.getRegionHeight());
            }
            if (obstacleRect.contains(randomPosition)) {
                return false;
            }
        }
        tempPickup = new Pickup(pickupType, atlas);
        tempPickup.pickupPosition.set(randomPosition);
        pickupsInScene.add(tempPickup);
        return true;
    }

    protected void pickItUp(Pickup pickup) {
        pickupSound.play(game.soundVolume);
        switch (pickup.pickupType) {
            case Pickup.STAR:
                starCount += pickup.pickupValue;
                break;
            case Pickup.SHIELD:
                shieldCount = pickup.pickupValue;
                break;
            case Pickup.FUEL:
                fuelCount = pickup.pickupValue;
                break;
        }
        pickupsInScene.removeValue(pickup, false);
    }

    @Override
    protected void handleBackPress() {
        if (gameState == GameState.ACTION) {
            pause();
            return;
        }
        if (gameState == GameState.PAUSE) {
            resume();
        }
    }

    @Override
    public void dispose() {
        pillars.clear();
        meteorTextures.clear();
        pickupsInScene.clear();
        batch.dispose();
        super.dispose();
    }

}