package com.vanderhaegen.flappyship.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.vanderhaegen.flappyship.FlappyShip;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;


public class MenuScene extends BaseScene {
    private Image screenBg, title, tuto1, tuto2, starHT, shieldHT, fuelHT, meteorHT, indicatorHT;
    private Table table, options, rankTbl, howToTbl, howToTbl2;
    private TextButton playButton, optionsButton, exitButton, backButton, rankBtn,
            backRank, howToBtn, backhowTo, backhowTo2;
    private CheckBox muteCheckBox;
    private Slider volumeSlider;
    private Label soundTitle, volumTitle, helpTip, rankTtl, score, starTitle, shieldT, fuelT, meteorT, indiT, tutoT;
    private Array<String> helpT;
    private Drawable backG;
    private Stage stageMe;
    private OrthographicCamera camera;

    public MenuScene(FlappyShip flappyShip) {
        super(flappyShip);

        backG = game.backG;
        stageMe = new Stage(game.viewport, game.batch);
        camera = game.camera;

        screenBg = new Image(game.atlas.findRegion("background"));
        title = new Image(game.atlas.findRegion("flappylogo"));
        title.setSize(stageMe.getWidth() / 2, stageMe.getHeight() / 2 - 50);
        helpT = new Array<String>();
        helpT.add(" 'To see a World in a Grain of sand' ");
        helpT.add(" 'Education is not the learning of facts' ");
        helpT.add(" 'If the Doors of perception were clensed' ");
        helpTip = new Label(helpT.random(), skin);
        helpTip.setColor(Color.GOLD);
        helpTip.setFontScale(0.6f);
        helpTip.setPosition(stageMe.getWidth() / 2 - helpTip.getWidth() / 3.5f, 10);

        tuto1 = new Image(game.atlas.findRegion("tuto1"));
        tuto1.setScale(1f);
        tuto2 = new Image(game.atlas.findRegion("tuto2"));
        tuto2.setScale(1f);
        starHT = new Image(game.atlas.findRegion("star_pickup"));
        starHT.setScale(0.6f);
        shieldHT = new Image(game.atlas.findRegion("shield_pickup"));
        shieldHT.setScale(0.6f);
        fuelHT = new Image(game.atlas.findRegion("fuel_pickup"));
        fuelHT.setScale(0.3f);
        meteorHT = new Image(game.atlas.findRegion("meteor2"));
        meteorHT.setScale(0.7f);
        indicatorHT = new Image(game.atlas.findRegion("fuelIndicator"));
        indicatorHT.setScale(0.7f);

        table = new Table();
        playButton = new TextButton("PLAY GAME", skin);
        table.add(playButton).padBottom(10).colspan(3).row();
        howToBtn = new TextButton("HOW TO PLAY", skin);
        table.add(howToBtn).padBottom(10).colspan(3).row();

        rankBtn = new TextButton("HIGHSCORE", skin);
        table.add(rankBtn).padBottom(10).align(Align.left);
        optionsButton = new TextButton("SOUND", skin);
        table.add(optionsButton).padBottom(10).colspan(2).row();

        exitButton = new TextButton("EXIT GAME", skin);
        table.add(exitButton).colspan(3);
        table.setPosition(300, -600);

        options = new Table();
        soundTitle = new Label("SOUND AND SAFE", skin);
        volumTitle = new Label("VOLUME", skin);
        soundTitle.setColor(Color.GOLD);
        options.add(soundTitle).pad(30).colspan(2).row();
        options.add(volumTitle).pad(10);
        volumeSlider = new Slider(0, 2, 0.2f, false, skin);
        options.add(volumeSlider).pad(10).growX().row();
        muteCheckBox = new CheckBox(" MUTE ALL", skin);
        muteCheckBox.getImageCell().size(19);
        options.add(muteCheckBox).padBottom(10).colspan(2).row();
        backButton = new TextButton("BACK", skin);
        options.add(backButton).colspan(2).padTop(20);
        options.setBackground(backG);
        options.pack();
        options.setPosition(400, -600);
        muteCheckBox.setChecked(!game.soundEnabled);
        volumeSlider.setValue(game.soundVolume);

        rankTbl = new Table();
        rankTtl = new Label("TOP 6", skin);
        rankTtl.setColor(Color.GOLD);
        backRank = new TextButton("BACK", skin);
        rankTbl.add(rankTtl).padBottom(25).colspan(2).row();
        showScore();
        rankTbl.add(backRank).colspan(2).padTop(20);
        rankTbl.setBackground(backG);
        rankTbl.pack();
        rankTbl.setPosition(400, -600);

        howToTbl = new Table();
        tutoT = new Label("Touch's distance and position \n controls the ship movement", skin);
        tutoT.setFontScale(0.5f);
        howToTbl.add(tuto1).pad(5).colspan(1);
        howToTbl.add(tuto2).pad(5).colspan(3).row();
        howToTbl.add(tutoT).pad(5).colspan(5).row();

        backhowTo = new TextButton("NEXT", skin);
        howToTbl.add(backhowTo).colspan(5);
        howToTbl.setBackground(backG);
        howToTbl.pack();
        howToTbl.setPosition(400, -600);

        howToTbl2 = new Table();
        starTitle = new Label("extra points; ", skin);
        starTitle.setFontScale(0.5f);
        shieldT = new Label("Protection Shield; ", skin);
        shieldT.setFontScale(0.5f);
        fuelT = new Label("Restores Fuel; ", skin);
        fuelT.setFontScale(0.5f);
        meteorT = new Label("Kills; ", skin);
        meteorT.setFontScale(0.5f);
        indiT = new Label("Indicates Fuel; ", skin);
        indiT.setFontScale(0.5f);
        howToTbl2.add(starHT).colspan(1);
        howToTbl2.add(shieldHT).colspan(2);
        howToTbl2.add(fuelHT).colspan(3).row();
        howToTbl2.add(starTitle).pad(7).colspan(1);
        howToTbl2.add(shieldT).pad(7).colspan(2);
        howToTbl2.add(fuelT).pad(7).colspan(3).row();

        howToTbl2.add(meteorHT).colspan(2);
        howToTbl2.add(indicatorHT).colspan(3).row();
        howToTbl2.add(meteorT).pad(7).colspan(2);
        howToTbl2.add(indiT).pad(7).colspan(3).row();

        backhowTo2 = new TextButton("BACK", skin);
        howToTbl2.add(backhowTo2).colspan(5);
        howToTbl2.setBackground(backG);
        howToTbl2.pack();
        howToTbl2.setPosition(400, -600);

        stageMe.addActor(screenBg);
        stageMe.addActor(title);
        stageMe.addActor(table);
        stageMe.addActor(helpTip);
        stageMe.addActor(options);
        stageMe.addActor(rankTbl);
        stageMe.addActor(howToTbl);
        stageMe.addActor(howToTbl2);

    }

    private void showScore() {
        for (int i = 1; i <= 6; i++) {
            score = new Label(game.prefs.getInteger("scorP" + i, 0) + "- "
                    + game.prefs.getString("scorN" + i, "RBS"), skin);
            rankTbl.add(score).pad(0, 39, 16, 39).row();
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stageMe);

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stageMe.addAction(sequence(fadeOut(0.7f, Interpolation.bounceIn), run(new Runnable() {
                    @Override
                    public void run() {
                        game.setScreen(new com.vanderhaegen.flappyship.game.FlappyShipScene(game));
                    }
                })));
            }
        });

        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMenu(options, table);
            }
        });

        rankBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMenu(rankTbl, table);
            }
        });

        howToBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMenu(howToTbl, table);
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.soundVolume = volumeSlider.getValue();
            }
        });

        muteCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.soundEnabled = !muteCheckBox.isChecked();
            }
        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMenu(table, options);
                game.prefs.putFloat("sound vol", game.soundVolume)
                        .putBoolean("sound stat", game.soundEnabled).flush();
            }
        });

        backRank.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMenu(table, rankTbl);
            }
        });

        backhowTo.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMenu(howToTbl2, howToTbl);
            }
        });

        backhowTo2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMenu(table, howToTbl2);
            }
        });

        stageMe.addAction(sequence(alpha(0), fadeIn(0.3f)));
        title.addAction(moveTo(camera.viewportWidth / 2 - title.getWidth() / 2,
                camera.viewportHeight - 10 - title.getHeight(), 10, Interpolation.elasticOut));

        showMenu(table, options);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render(delta);

        stageMe.act(delta);
        stageMe.draw();
    }

    private void showMenu(Table tab1, Table tab2) {
        tab1.addAction(moveTo(camera.viewportWidth / 2 - tab1.getWidth() / 2,
                tab1.getHeight() < camera.viewportHeight / 2 ? camera.viewportHeight / 3 :
                        camera.viewportHeight / 2 - tab1.getHeight() / 2, 1.5f, Interpolation.swing));
        tab2.addAction(moveTo(camera.viewportWidth / 2, -600, 1, Interpolation.swingIn));
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    protected void handleBackPress() {
        game.setScreen(new MenuScene(game));
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        stageMe.dispose();
        skin.dispose();
        super.dispose();
    }
}
