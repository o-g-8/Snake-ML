package controller;
import model.SimpleGame;
import view.ViewCommand;
import view.ViewSimpleGame;


// Controller for the simple version of the game.
// Initializes the SimpleGame model and sets up the view.
public class ControllerSimpleGame extends AbstractController {

	/**
	 * Default constructor: initializes simple game and views.
	 */
	public ControllerSimpleGame() {
		super();
		this.game = new SimpleGame(100);
		this.game.init();
		ViewSimpleGame viewSimpleGame = new ViewSimpleGame(this.game);
		ViewCommand viewCommand = new ViewCommand(this, this.game);
	}
}