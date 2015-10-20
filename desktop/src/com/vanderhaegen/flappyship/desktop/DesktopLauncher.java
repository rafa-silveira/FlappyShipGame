package com.vanderhaegen.flappyship.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.vanderhaegen.flappyship.FlappyShip;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Flappy SpaceShip v2.0";
		config.width = 800;
		config.height = 480;
		new LwjglApplication(new FlappyShip(), config);
	}
}
