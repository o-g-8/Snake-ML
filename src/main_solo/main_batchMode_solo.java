package main_solo;


import java.util.ArrayList;

import controller.ControllerSnakeGame;
import model.Game;
import model.InputMap;
import model.SnakeGame;
import strategy.ApproximateQLearning_solo;
import strategy.Strategy;
import strategy.TabularQLearning_solo;
import utils.AgentAction;
import view.PanelSnakeGame;
import view.ViewCommand;
import view.ViewSnakeGame;



// Main class for running batch simulations of Snake in solo mode
public class main_batchMode_solo {

    /**
     * Entry point for batch training and evaluation of Snake agent
     */
    public static void main(String[] args) {
        // Q-learning parameters
        double gamma = 0.95;      // Discount factor
        double epsilon = 0.3;     // Exploration rate
        double alpha = 0.01;      // Learning rate

        // Whether to place the first apple randomly
        boolean randomFirstApple = true;    

        // Map layout file
        String layoutName = "layouts/alone/smallNoWall_alone.lay";

        // Load map and initial positions
        InputMap inputMap = null;
        try {
            inputMap = new InputMap(layoutName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ask user for strategy choice
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.println("Choose strategy: 1 = TabularQLearning, 2 = ApproximateQLearning");
        int strategyChoice = 0;
        while (strategyChoice != 1 && strategyChoice != 2) {
            System.out.print("Enter 1 or 2: ");
            try {
                strategyChoice = Integer.parseInt(scanner.nextLine());
            } catch (Exception e) {
                strategyChoice = 0;
            }
        }

        // Create strategy array for each snake
        Strategy[] arrayStrategies = new Strategy[inputMap.getStart_snakes().size()];
        if (strategyChoice == 1) {
            arrayStrategies[0] = new TabularQLearning_solo(AgentAction.values().length, epsilon, gamma, alpha);
            System.out.println("TabularQLearning selected.");
        } else {
            arrayStrategies[0] = new ApproximateQLearning_solo(AgentAction.values().length, epsilon, gamma, alpha);
            System.out.println("ApproximateQLearning selected.");
        }

        // Number of sequential simulations for average reward in training mode
        int Ntrain = 100;
        // Number of parallel simulations for average reward in test mode
        int Ntest = 100;
        // Maximum number of turns per Snake game
        int maxTurnSnakeGame = 300;

        // Main training loop: alternate test and train simulations
        for(int cpt = 0; cpt < 10000000; cpt++) {
            System.out.println("Compute score in test mode");
            launchParallelGames(Ntest, maxTurnSnakeGame, inputMap, arrayStrategies, false, randomFirstApple);
            // Uncomment to visualize every 10 cycles
            if(cpt%100 == 0) {
                System.out.println("Visualization mode");
                vizualize(maxTurnSnakeGame, inputMap, arrayStrategies, false, randomFirstApple);
            }
            System.out.println("Play and collect examples - train mode");
            launchParallelGames(Ntrain, maxTurnSnakeGame, inputMap, arrayStrategies, true, randomFirstApple);
        }
    }

    /**
     * Runs multiple Snake games in parallel and computes average scores for each strategy
     * @param nbGames Number of games to run
     * @param maxTurnSnakeGame Maximum turns per game
     * @param inputMap Map and initial positions
     * @param arrayStrats Array of strategies for each snake
     * @param modeTrain True for training mode, false for test mode
     * @param randomFirstApple Whether to place first apple randomly
     */
    public static void launchParallelGames(int nbGames, int maxTurnSnakeGame, InputMap inputMap, Strategy[] arrayStrats, boolean modeTrain, boolean randomFirstApple) {
        double[] scoreStrats = new double[arrayStrats.length];
        ArrayList<SnakeGame> snakeGames = new ArrayList<SnakeGame>();

        // Initialize and start all games
        for(int i = 0; i < nbGames; i++ ) {
            for(int j =0; j < arrayStrats.length; j++) {
                arrayStrats[j].setModeTrain(modeTrain);
            }
            SnakeGame snakeGame = new SnakeGame(maxTurnSnakeGame, inputMap, randomFirstApple);
            snakeGame.setStrategies(arrayStrats);
            snakeGame.init();
            snakeGame.setTime(0);
            snakeGames.add(snakeGame);
        }

        // Launch all games
        for(int i = 0; i < nbGames; i++ ) {
            snakeGames.get(i).launch();
        }

        // Wait for all games to finish and collect scores
        for(int i = 0; i < nbGames; i++ ) {
            try {
                ((Game) snakeGames.get(i)).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int j =0; j < arrayStrats.length; j++) {
                scoreStrats[j] += snakeGames.get(i).getTabTotalScoreSnakes()[j];
            }
        }

        // Print average scores for each strategy
        for(int j =0; j < arrayStrats.length; j++) {
            if(modeTrain) {
                System.out.println("Train - agent " + j + " - strategy " + arrayStrats[j] + " average global score : " + scoreStrats[j]/nbGames);
            } else {
                System.out.println("Test - agent " + j + " - strategy " + arrayStrats[j] + " average global score : " + scoreStrats[j]/nbGames);
            }
        }
    }

    /**
     * Visualizes a single Snake game in a graphical window
     * @param maxTurnSnakeGame Maximum turns per game
     * @param inputMap Map and initial positions
     * @param arrayStrats Array of strategies for each snake
     * @param modeTrain True for training mode, false for test mode
     * @param randomFirstApple Whether to place first apple randomly
     */
    private static void vizualize(int maxTurnSnakeGame, InputMap inputMap, Strategy[] arrayStrats, boolean modeTrain, boolean randomFirstApple) {
        SnakeGame snakeGame = new SnakeGame(maxTurnSnakeGame, inputMap, randomFirstApple);
        for(int j =0; j < arrayStrats.length; j++) {
            arrayStrats[j].setModeTrain(modeTrain);
        }
        snakeGame.setStrategies(arrayStrats);
        snakeGame.init();
        snakeGame.setTime(10);

        ControllerSnakeGame controllerSnakeGame = new ControllerSnakeGame(snakeGame);
        PanelSnakeGame panelSnakeGame = new PanelSnakeGame(inputMap.getSizeX(), inputMap.getSizeY(), inputMap.get_walls(), inputMap.getStart_snakes(), inputMap.getStart_items());
        ViewSnakeGame viewSnakeGame = new ViewSnakeGame(controllerSnakeGame, snakeGame, panelSnakeGame);
        ViewCommand viewCommand = new ViewCommand(controllerSnakeGame, snakeGame);

        controllerSnakeGame.play();
        viewCommand.getState().clickPlay();
    }
}
