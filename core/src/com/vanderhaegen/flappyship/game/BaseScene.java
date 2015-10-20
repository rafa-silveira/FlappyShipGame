package com.vanderhaegen.flappyship.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public abstract class BaseScene extends ScreenAdapter {
    protected com.vanderhaegen.flappyship.FlappyShip game;
    protected Stage stage;
    protected Skin skin;
    protected AssetManager manager;
    protected BitmapFont font;
    protected TextureAtlas atlas;
    protected Dialog exitDia, scorDia;
    protected GameState gameState;
    protected Music music;
    protected Sound tapSound, crashSound, spawnSound, pickupSound;
    protected String strName;
    protected TextField textField;

    private int finalScore, lowScore;

    private boolean keyHandled;

    public BaseScene(com.vanderhaegen.flappyship.FlappyShip flappyShip) {
        game = flappyShip;
        stage = new Stage(game.viewport, game.batch);
        skin = game.skin;
        manager = game.manager;
        font = game.font;

        atlas = game.atlas;
        music = game.music;
        music.setVolume(game.soundVolume);
        music.setLooping(true);
        tapSound = game.tapSound;
        crashSound = game.crashSound;
        spawnSound = game.spawnSound;
        pickupSound = game.pickupSound;
        textField = new TextField("RBS", skin);
        strName = "RBS";

        keyHandled = false;
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchMenuKey(true);

        exitDia = new Dialog("PAUSED GAME", game.skin) {
            {
                button("Continue", "cont");
                button("Menu", "menu");
                button("Exit", "exit");
            }

            @Override
            protected void result(Object object) {
                if (object == "menu") {
                    game.setScreen(new MenuScene(game));
                } else if (object == "exit") {
                    Gdx.app.exit();
                } else {
                    resume();
                }
            }
        };

        scorDia = new Dialog("Save Score", game.skin) {
            {
                getContentTable().add(textField).row();
                button("Save", "save");
            }

            @Override
            protected void result(Object object) {
                if (object == "save") {
                    strName = textField.getText().length() > 2 ?
                            textField.getText().substring(0, 3) : textField.getText();
                    scorDia.setVisible(false);
                    scorDia.remove();
                    Gdx.input.setInputProcessor(null);
                    upHighScore();
                }
            }
        };
        scorDia.setVisible(false);

    }

    protected void checkStoreScore(int finalScore) {
        this.finalScore = finalScore;
        lowScore = game.prefs.getInteger("scorP6");
        if (finalScore > lowScore) {
            scorDia.show(stage);
            scorDia.setVisible(true);
            Gdx.input.setInputProcessor(stage);
        }
    }

    private void upHighScore() {
        String finalName;

        int[] scores = new int[6];
        String[] names = new String[6];
        for (int i = 1; i <= 6; i++) {
            scores[i - 1] = game.prefs.getInteger("scorP" + i);
            names[i - 1] = game.prefs.getString("scorN" + i);
        }
        scores[5] = finalScore;
        names[5] = strName;
        for (int i = 5; i > 0; i--) {
            if (scores[i] > scores[i - 1]) {
                finalScore = scores[i - 1];
                finalName = names[i - 1];
                scores[i - 1] = scores[i];
                names[i - 1] = names[i];
                scores[i] = finalScore;
                names[i] = finalName;
            } else {
                break;
            }
        }
        for (int i = 1; i <= 6; i++) {
            game.prefs.putInteger("scorP" + i, scores[i - 1]).flush();
            game.prefs.putString("scorN" + i, names[i - 1]).flush();
        }

    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (Gdx.input.isKeyPressed(Input.Keys.BACK) || Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            if (keyHandled) {
                return;
            }
            handleBackPress();
            keyHandled = true;
        } else {
            keyHandled = false;
        }
    }

    @Override
    public void hide() {
        music.stop();
    }

    @Override
    public void pause() {
        gameState = GameState.PAUSE;
        exitDia.show(stage);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resume() {
        gameState = GameState.ACTION;
        exitDia.remove();
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    protected abstract void handleBackPress();

    protected enum GameState {
        INIT, ACTION, GAME_OVER, PAUSE
    }
}