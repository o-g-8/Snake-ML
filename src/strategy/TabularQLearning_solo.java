package strategy;


import java.util.ArrayList;
import java.util.HashMap;

import java.util.Random;
import java.util.Vector;

import agent.Snake;
import item.Item;
import model.SnakeGame;
import utils.AgentAction;
import utils.ItemType;
import utils.Position;



// Tabular Q-Learning strategy for Snake agent
public class TabularQLearning_solo extends Strategy {
	// Q-table: maps state (String) to an array of Q-values for each action
	private HashMap<String, double[]> Q;
	// Number of possible actions
	private int nbActions;
	// Exploration rate
	private double epsilon;
	// Discount factor
	private double gamma;
	// Learning rate
	private double alpha;

	/**
	 * Constructor: initializes Q-table and parameters
	 * @param nbActions Number of possible actions
	 * @param epsilon Exploration rate
	 * @param gamma Discount factor
	 * @param alpha Learning rate
	 */
	public TabularQLearning_solo(int nbActions, double epsilon, double gamma, double alpha) {
		super(nbActions, epsilon, gamma, alpha);
		this.Q = new HashMap<>();
		this.nbActions = nbActions;
		this.epsilon = epsilon;
		this.gamma = gamma;
		this.alpha = alpha;
	}

	/**
	 * Encodes the current state of the game as a String for Q-table lookup
	 * @param idxSnake Index of the snake agent
	 * @param snakeGame Current game state
	 * @return Encoded state as String
	 */
	public String encodeState(int idxSnake, SnakeGame snakeGame) {
		String state = "";
		String[][] t = new String[snakeGame.getSizeX()][snakeGame.getSizeY()];
		Snake snake = snakeGame.getSnakes().get(idxSnake);

		// Fill grid with 'V' for empty
		for(int i=0 ; i<snakeGame.getSizeX() ; i++) {
			for(int j=0 ; j<snakeGame.getSizeY() ; j++) {
				t[i][j] = "V";
			}
		}

		// Mark walls
		boolean[][] walls = snakeGame.getWalls();
		for(int i=0 ; i<snakeGame.getSizeX() ; i++) {
			for(int j=0 ; j<snakeGame.getSizeY() ; j++) {
				if(walls[i][j]) {
					t[i][j] = "W";
				}
			}
		}

		// Mark snake body
		int n = 0;
		for(Position p : snake.getPositions()) {
			//t[p.getX()][p.getY()] = "B";
			t[p.getX()][p.getY()] = "B"+n;
			n++;
		}

		// Mark snake head
		t[snake.getX()][snake.getY()] = "H";

		// Mark items
		for(Item i : snakeGame.getItems()) {
			switch(i.getItemType()) {
				case APPLE:
					t[i.getX()][i.getY()] = "A";
					break;
				case BOX:
					t[i.getX()][i.getY()] = "B";
					break;
				case INVINCIBILITY_BALL:
					t[i.getX()][i.getY()] = "I";
					break;
				case SICK_BALL:
					t[i.getX()][i.getY()] = "S";
					break;
				default:
					break;
			}
		}

		// Concatenate grid to a single string
		for(String[] i : t) {
			for(String j : i) {
				state += j;
			}
		}

		return state;
	}
    
   
    
	/**
	 * Chooses an action for the snake using epsilon-greedy policy
	 * @param idxSnake Index of the snake agent
	 * @param snakeGame Current game state
	 * @return Chosen AgentAction
	 */
	@Override
	public synchronized AgentAction chooseAction(int idxSnake, SnakeGame snakeGame) {
		String state = encodeState(idxSnake, snakeGame);
		double[] qValues = Q.get(state);

		// Initialize Q-values for unseen state
		if (qValues == null) {
			qValues = new double[nbActions];
			for (int i = 0; i < nbActions; i++) qValues[i] = 0.0;
			Q.put(state, qValues);
		}

		Random rand = new Random();
		// Exploration: random action with probability epsilon
		if (rand.nextDouble() < epsilon) {
			int randomActionId = rand.nextInt(nbActions);
			return AgentAction.values()[randomActionId];
		} else {
			// Exploitation: choose action with highest Q-value
			int BestActionId = 0;
			for (int i = 1; i < nbActions; i++) {
				if (qValues[i] > qValues[BestActionId]) BestActionId = i;
			}
			return AgentAction.values()[BestActionId];
		}
	}


	/**
	 * Updates the Q-table using the Q-learning update rule
	 * @param idxSnake Index of the snake agent
	 * @param state Current state
	 * @param action Action taken
	 * @param nextState State after action
	 * @param reward Reward received
	 * @param isFinalState True if nextState is terminal
	 */
	@Override
	public synchronized void update(int idxSnake, SnakeGame state, AgentAction action, SnakeGame nextState, int reward, boolean isFinalState) {
		String currentState = encodeState(idxSnake, state);
		String nextStateStr = encodeState(idxSnake, nextState);

		// Get or initialize Q-values for current state
		double[] qValues = Q.get(currentState);
		if (qValues == null) {
			qValues = new double[nbActions];
			for (int i = 0; i < nbActions; i++) qValues[i] = 0.0;
			Q.put(currentState, qValues);
		}

		// Get or initialize Q-values for next state
		double[] qValuesNext = Q.get(nextStateStr);
		if (qValuesNext == null) {
			qValuesNext = new double[nbActions];
			for (int i = 0; i < nbActions; i++) qValuesNext[i] = 0.0;
			Q.put(nextStateStr, qValuesNext);
		}

		int ActionId = action.ordinal();

		// Find max Q-value for next state (for Q-learning update)
		double maxQNext = 0.0;
		if (!isFinalState) {
			maxQNext = qValuesNext[0];
			for (int i = 1; i < nbActions; i++) {
				if (qValuesNext[i] > maxQNext) maxQNext = qValuesNext[i];
			}
		}

		// Q-learning update rule
		qValues[ActionId] += alpha * (reward + gamma * maxQNext - qValues[ActionId]);
	}
}
