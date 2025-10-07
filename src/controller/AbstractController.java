
// Abstract base class for game controllers in the MVC architecture.
// Provides common control methods for managing the game state.
package controller;

import model.Game;



public abstract class AbstractController {

	// Reference to the game model
	Game game;

	/**
	 * Restarts the game by pausing and reinitializing.
	 */
	public void restart() {
		this.game.pause();
		this.game.init();
	}

	/**
	 * Advances the game by one step.
	 */
	public void step() {
		this.game.step();
	}

	/**
	 * Starts or resumes the game.
	 */
	public void play() {
		this.game.launch();
	}

	/**
	 * Pauses the game.
	 */
	public void pause() {
		this.game.pause();
	}

	/**
	 * Sets the game speed (in steps per second).
	 * @param speed Number of steps per second
	 */
	public void setSpeed(double speed) {
		long time = (long) (1000/speed);
		this.game.setTime(time);
	}
}
