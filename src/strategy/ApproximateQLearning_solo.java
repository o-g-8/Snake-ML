
// ApproximateQLearning_solo: Implements linear function approximation Q-learning for Snake.
// The agent uses a feature vector to represent the state-action pair and learns weights for each feature.
// Action selection is epsilon-greedy, and weights are updated using the TD error.
// Features include proximity to items, distance to items, and self-collision avoidance.
// All movement simulations are performed on copies to avoid modifying the real game state.
package strategy;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import agent.Snake;
import item.Item;
import model.SnakeGame;
import utils.AgentAction;
import utils.ItemType;
import utils.Position;

public class ApproximateQLearning_solo extends Strategy {
    // Q-learning with linear function approximation for Snake
    
    // Weights for each feature (learned parameters)
    private double[] weights;
    // Number of features in the feature vector
    private int NUM_FEATURES_MAX;
    
    /**
     * Constructor: initializes Q-learning parameters and random weights
     * @param nbActions Number of possible actions
     * @param epsilon Exploration rate
     * @param gamma Discount factor
     * @param alpha Learning rate
     */
    public ApproximateQLearning_solo(int nbActions, double epsilon, double gamma, double alpha) {    
        super(nbActions, epsilon, gamma, alpha);
        NUM_FEATURES_MAX = 4;
        weights = new double[NUM_FEATURES_MAX];
        Random rand = new Random();
        for (int i = 0; i < NUM_FEATURES_MAX; i++) {
            weights[i] = rand.nextDouble();
        }
    }
    
    /**
     * Computes the feature vector for a given state and action.
     * Features:
     * 0: Bias (always 1)
     * 1: Is next to item
     * 2: Distance to closest item (normalized)
     * 3: Is not next to own body
     */
    private double[] getFeatures(int idxSnake, SnakeGame state, AgentAction moveAction) {
        double[] features = new double[NUM_FEATURES_MAX];
        Snake snake = state.getSnakes().get(idxSnake);
        Position head = snake.getPositions().get(0);
        ArrayList<Double> featuresList = new ArrayList<>();
        ArrayList<Item> items = state.getItems();

        Position nextHead = getNextPosition(idxSnake, moveAction, state);
        // Copy the snake's body to simulate movement without affecting the real game state
        ArrayList<Position> nextSnakeBody = new ArrayList<>();
        for (Position p : snake.getPositions()) {
            nextSnakeBody.add(new Position(p.getX(), p.getY()));
        }

        // Simulate movement: update the body and head positions
        boolean onApple = false;
        for (Item item : items) {
            if (item.getItemType() == ItemType.APPLE) {
                if(item.getX() == nextHead.getX() && item.getY() == nextHead.getY()) {
                    onApple = true;
                    break;
                }
            }
        }
        if(!onApple) {
            // Move body segments
            for (int i=0;i<nextSnakeBody.size()-1;i++) {
                nextSnakeBody.get(nextSnakeBody.size()-1-i).setX(nextSnakeBody.get(nextSnakeBody.size()-2-i).getX());
                nextSnakeBody.get(nextSnakeBody.size()-1-i).setY(nextSnakeBody.get(nextSnakeBody.size()-2-i).getY());
            }
        }
        // Move head
        nextSnakeBody.get(0).setX(nextHead.getX());
        nextSnakeBody.get(0).setY(nextHead.getY());

        // Feature 0: Bias (always 1)
        features[0] = 1.0; 
        // Feature 1: Is next to item
        for (Item item : items) {
            Position itemPo = new Position(item.getX(), item.getY());
            if(isNextTo(nextHead,itemPo, state)) {
                features[1] += 1.0;
            }
        }

        // Feature 2: Distance to closest item (normalized)
        double minDist = Double.MAX_VALUE;
        for (Item item : items) {
            double dist = distanceSnakeItem(nextHead, item, state);
            minDist = Math.min(minDist, dist);
        }
        features[2] = items.isEmpty() ? 0.0 : (1.0 - Math.min(1.0, minDist / (state.getSizeX() + state.getSizeY())));
        // Feature 3: Is not next to own body
        boolean nextToBody = false;
        if (snake.getSize() > 2) {
            for (int i=2;i<nextSnakeBody.size();i++) {
                if(isNextTo(nextHead,nextSnakeBody.get(i), state)) {
                    nextToBody = true;
                    break;
                }
            }
        }
        features[3] = nextToBody ? 0.0 : 1.0;

        return features;
    }

    /**
     * Returns the next head position for a given action
     */
    public Position getNextPosition(int idxSnake, AgentAction action, SnakeGame state) {
        Position snake = state.getSnakes().get(idxSnake).getPositions().get(0);
        int x = snake.getX();
        int y = snake.getY();

        switch(action) {
            case MOVE_UP:
                return new Position(x, y - 1);
            case MOVE_DOWN:
                return new Position(x, y + 1);
            case MOVE_LEFT:
                return new Position(x - 1, y);
            case MOVE_RIGHT:
                return new Position(x + 1, y);
            default:
                return new Position(x, y);

        }
    }

    /**
     * Simulates the move and checks for self-collision
     * Returns true if the move is legal (no collision with body)
     */
    public boolean isLegalMove(AgentAction action,  Snake snake, SnakeGame game) {
        ArrayList<Position> snakePositions = new ArrayList<>();
        for (Position p : snake.getPositions()) {
            snakePositions.add(new Position(p.getX(), p.getY()));
        }
        int oldTailX = snake.getOldTailX();
        int oldTailY = snake.getOldTailY();

		Position head = snakePositions.get(0);
		// Store old tail position for growth
		oldTailX = snakePositions.get(snakePositions.size() - 1).getX();
		oldTailY = snakePositions.get(snakePositions.size() - 1).getY();
		// Move body segments
		if(snakePositions.size() > 1) {
			for(int i = 1; i < snakePositions.size(); i++) {    
				snakePositions.get(snakePositions.size() - i).setX(snakePositions.get(snakePositions.size() - i - 1).getX());
				snakePositions.get(snakePositions.size() - i).setY(snakePositions.get(snakePositions.size() - i - 1).getY());
			}
		}    
		// Move head according to action
		switch (action) {
		case MOVE_UP:
			int y = snakePositions.get(0).getY();
			if(y > 0) {
				head.setY(snakePositions.get(0).getY()-1);
			} else {
				head.setY(game.getSizeY()-1);
			}
			break;
		case MOVE_DOWN:
			head.setY((snakePositions.get(0).getY()+1)%game.getSizeY());
			break;
		case MOVE_RIGHT:
			head.setX((snakePositions.get(0).getX()+1)%game.getSizeX());
			break;        
		case MOVE_LEFT:
			int x = snakePositions.get(0).getX();
			if(x > 0) {
				head.setX(snakePositions.get(0).getX()-1);
			} else {
				head.setX(game.getSizeX()-1);
			}
			break;
		default:
			break;
		}

        // Check for self-collision
        for (int i = 1; i < snakePositions.size(); i++) {
            if (head.getX() == snakePositions.get(i).getX() && head.getY() == snakePositions.get(i).getY()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Chooses an action using epsilon-greedy policy:
     * - With probability epsilon, selects a random legal action
     * - Otherwise, selects the action with the highest Q-value
     */
    @Override
    public AgentAction chooseAction(int idxSnake, SnakeGame state) {
        Random rand = new Random();
        Snake snake = state.getSnakes().get(idxSnake);
        // Epsilon-greedy action selection
        if (rand.nextDouble() < epsilon) {
            ArrayList<AgentAction> legalActions = new ArrayList<>();
            for (AgentAction action : AgentAction.values()) {
                if (state.isLegalMove(snake, action)) {
                    legalActions.add(action);
                }
            }
            if (legalActions.isEmpty()) return AgentAction.MOVE_UP; // fallback
            return legalActions.get(rand.nextInt(legalActions.size()));
        }
        // Choose action with highest Q-value
        double maxQ = Double.NEGATIVE_INFINITY;
        AgentAction bestAction = AgentAction.MOVE_UP;
        for (AgentAction action : AgentAction.values()) {
            if (!state.isLegalMove(snake, action)) continue;
            if (!isLegalMove(action, snake, state)) continue;
            double[] features = getFeatures(idxSnake, state, action);
            double q = 0.0;
            for (int i = 0; i < weights.length; i++) q += weights[i] * features[i];
            if (q > maxQ) {
                maxQ = q;
                bestAction = action;
            }
        }
        return bestAction;
    }
    
    // Check if a position is next to another position (taking into account grid wrapping)
    /**
     * Checks if two positions are adjacent, considering grid wrapping
     */
    public boolean isNextTo(Position p1, Position p2, SnakeGame game) {
        int sizeX = game.getSizeX();
        int sizeY = game.getSizeY();

        int dx = Math.abs(p1.getX() - p2.getX());
        int dy = Math.abs(p1.getY() - p2.getY());
        dx = Math.min(dx, sizeX - dx);
        dy = Math.min(dy, sizeY - dy);
        return (dx + dy) == 1;
    }

    // Calculate the Manhattan distance between two positions (considering grid wrapping and ignoring snake body) 
    /**
     * Computes the Manhattan distance between two positions, considering grid wrapping
     */
    int distanceSnakeItem(Position snake, Item item, SnakeGame game) {
        Position itemPo = new Position(item.getX(), item.getY());
        int dx = Math.abs(snake.getX() - itemPo.getX());
        int dy = Math.abs(snake.getY() - itemPo.getY());
        dx = Math.min(dx, game.getSizeX() - dx);
        dy = Math.min(dy, game.getSizeY() - dy);
        return dx + dy;
    }

    // Update weights based on observed transition
    /**
     * Updates the weights using the Q-learning update rule with linear function approximation
     */
    @Override
    public void update(int idx, SnakeGame state, AgentAction moveAction, SnakeGame nextState, int reward, boolean isFinalState) {
        double[] features = getFeatures(idx, state, moveAction);

        double qCurrent = 0.0;
        for (int i = 0; i < weights.length; i++) qCurrent += weights[i] * features[i];

        double qNextMax = 0.0;
        if (!isFinalState) {
            double maxQ = Double.NEGATIVE_INFINITY;
            for (AgentAction action : AgentAction.values()) {
                if (!nextState.isLegalMove(nextState.getSnakes().get(idx), action)) continue;
                double[] nextFeatures = getFeatures(idx, nextState, action);
                double q = 0.0;
                for (int i = 0; i < weights.length; i++) q += weights[i] * nextFeatures[i];
                if (q > maxQ) maxQ = q;
            }
            qNextMax = maxQ;
        }

        double target = reward + (isFinalState ? 0.0 : gamma * qNextMax);
        double tdError = target - qCurrent;

        for (int i = 0; i < weights.length; i++) {
            weights[i] += alpha * tdError * features[i];
        }
    }
}
    
