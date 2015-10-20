package com.vanderhaegen.flappyship;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Rafael Silveira on 3/31/2015.
 */
public class FlappyShip extends Game {

    private static final int SCREEN_WIDTH;
    private static final int SCREEN_HEIGHT;

    static {
        SCREEN_WIDTH = 800;
        SCREEN_HEIGHT = 480;
    }

    public SpriteBatch batch;
    public OrthographicCamera camera;
    public TextureAtlas atlas;
    public Viewport viewport;
    public BitmapFont font;
    public Skin skin;
    public float soundVolume;
    public boolean soundEnabled;
    public AssetManager manager;
    public Music music;
    public Sound tapSound, crashSound, spawnSound, pickupSound;
    public Preferences prefs;
    public Pixmap pixmap;
    public Drawable backG;

    @Override
    public void create() {
        batch = new SpriteBatch();
        prefs = Gdx.app.getPreferences("flappyship prefs");
        camera = new OrthographicCamera();
        camera.position.set(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0);
        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);

        pixmap = new Pixmap(1, 1, Pixmap.Format.RGB888);
        pixmap.setColor(Color.CORAL);
        pixmap.fill();
        backG = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));

        manager = new AssetManager();
        soundVolume = prefs.getFloat("sound vol", 1.0f);
        soundEnabled = prefs.getBoolean("sound stat", true);

        setScreen(new com.vanderhaegen.flappyship.loads.LoadScreen(this));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        pixmap.dispose();
        batch.dispose();
        atlas.dispose();
        manager.dispose();
    }
}