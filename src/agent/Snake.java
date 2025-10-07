package agent;

import java.io.Serializable;
import java.util.ArrayList;

import model.SnakeGame;
import strategy.Strategy;
import utils.AgentAction;
import utils.ColorSnake;

import utils.Position;

// Class representing a Snake agent in the game
public class Snake implements Serializable{

	// List of positions occupied by the snake (head is first)
	ArrayList<Position> positions;

	// Last action performed by the snake
	private AgentAction lastMove;

	// Timers for special states
	private int invincibleTimer;
	private int sickTimer;

	// Strategy used by the snake (Q-learning, human, etc.)
	transient Strategy strategy;

	// Used to grow the snake after eating
	int oldTailX = -1;
	int oldTailY = -1;

	// Unique identifier for the snake
	private int id;
	
	// True if the snake is dead
	boolean isDead;

	// Color of the snake (for display)
	transient ColorSnake colorSnake;


	/**
	 * Constructor: initializes snake at given position, with color and id
	 */
	public Snake(Position position, AgentAction lastMove,  int id, ColorSnake colorSnake) {

		this.positions = new ArrayList<Position>();
		
		this.positions.add(position);
		
		
		this.setId(id);


		this.setInvincibleTimer(-1);
		this.setSickTimer(-1);
		
		this.isDead = false;
		
		this.colorSnake = colorSnake;
		
		this.setLastMove(lastMove);

	}


	/**
	 * Chooses an action for the snake using its strategy
	 */
	public AgentAction play(SnakeGame game) {
		return strategy.chooseAction(this.id, game);
	}
	

	/**
	 * Moves the snake in the given direction, updating its body and head
	 */
	public void move(AgentAction action, SnakeGame game) {
		Position head = this.positions.get(0);
		// Store old tail position for growth
		this.oldTailX = this.positions.get(positions.size() - 1).getX();
		this.oldTailY = this.positions.get(positions.size() - 1).getY();
		// Move body segments
		if(this.positions.size() > 1) {
			for(int i = 1; i < this.positions.size(); i++) {    
				positions.get(positions.size() - i).setX(positions.get(positions.size() - i - 1).getX());
				positions.get(positions.size() - i).setY(positions.get(positions.size() - i - 1).getY());
			}
		}    
		// Move head according to action
		switch (action) {
		case MOVE_UP:
			int y = positions.get(0).getY();
			if(y > 0) {
				head.setY(positions.get(0).getY()-1);
			} else {
				head.setY(game.getSizeY()-1);
			}
			break;
		case MOVE_DOWN:
			head.setY((positions.get(0).getY()+1)%game.getSizeY());
			break;
		case MOVE_RIGHT:
			head.setX((positions.get(0).getX()+1)%game.getSizeX());
			break;        
		case MOVE_LEFT:
			int x = positions.get(0).getX();
			if(x > 0) {
				head.setX(positions.get(0).getX()-1);
			} else {
				head.setX(game.getSizeX()-1);
			}
			break;
		default:
			break;
		}
		this.setLastMove(action);
	}

	
	/**
	 * Updates the strategy (Q-table) if in training mode
	 */
	public void update(SnakeGame state, AgentAction action, SnakeGame nextState, int reward ) {
		if(this.strategy.isModeTrain()) {
			this.strategy.update(this.id, state, action, nextState, reward, isDead );
		}
	}
	
	

	/**
	 * Increases the size of the snake by adding a new segment at the old tail position
	 */
	public void sizeIncrease() {
		this.positions.add(new Position(this.oldTailX, this.oldTailY));
	}

	/**
	 * Returns the current size of the snake
	 */
	public int getSize() {
		return this.positions.size();
	}

	/**
	 * Returns the list of positions occupied by the snake
	 */
	public ArrayList<Position> getPositions() {
		return positions;
	}

	/**
	 * Sets the positions of the snake
	 */
	public void setPositions(ArrayList<Position> positions) {
		this.positions = positions;
	}

	/**
	 * Returns the strategy used by the snake
	 */
	public Strategy getStrategy() {
		return strategy;
	}

	/**
	 * Sets the strategy for the snake
	 */
	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	/**
	 * Returns the X coordinate of the snake's head
	 */
	public int getX() {
		return this.positions.get(0).getX();
	}

	/**
	 * Returns the Y coordinate of the snake's head
	 */
	public int getY() {
		return this.positions.get(0).getY();
	}

	/**
	 * Returns the unique identifier of the snake
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the unique identifier for the snake
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns true if the snake is dead
	 */
	public boolean isDead() {
		return isDead;
	}


	/**
	 * Sets the dead status of the snake
	 */
	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}

	/**
	 * Returns the color of the snake
	 */
	public ColorSnake getColorSnake() {
		return colorSnake;
	}

	/**
	 * Sets the color of the snake
	 */
	public void setColorSnake(ColorSnake colorSnake) {
		this.colorSnake = colorSnake;
	}


	/**
	 * Returns the invincibility timer
	 */
	public int getInvincibleTimer() {
		return invincibleTimer;
	}


	/**
	 * Sets the invincibility timer
	 */
	public void setInvincibleTimer(int invincibleTimer) {
		this.invincibleTimer = invincibleTimer;
	}


	/**
	 * Returns the sick timer
	 */
	public int getSickTimer() {
		return sickTimer;
	}


	/**
	 * Sets the sick timer
	 */
	public void setSickTimer(int sickTimer) {
		this.sickTimer = sickTimer;
	}


	/**
	 * Returns the last action performed by the snake
	 */
	public AgentAction getLastMove() {
		return lastMove;
	}


	/**
	 * Sets the last action performed by the snake
	 */
	public void setLastMove(AgentAction lastMove) {
		this.lastMove = lastMove;
	}

	public int getOldTailX() {
		return oldTailX;
	}


	public int getOldTailY() {
		return oldTailY;
	}

}
