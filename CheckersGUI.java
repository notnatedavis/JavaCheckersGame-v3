package ui;

import java.util.ArrayList;

// Imports (separated for convenience)
import core.Checkers;
import javafx.application.Application;

import javafx.stage.Stage;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;

import javafx.geometry.Pos; 

/**
 * This is a CheckersTextConsole class within the ui package making use 
 * of Checkers class within the core package.
 * 
 * @author Nathaniel Davis-Perez
 * @version Build Feb 6, 2024
 * package ui
 */
public class CheckersGUI extends Application {
	
	/**
	 * Declarations of Stages, Scenes and Checkers
	 */
	private Stage primaryStage;
	private Scene mainScene, chooseScene, testScene, scene;
	private Checkers checkers;
	
	/**
	 * Instantiates CheckersBoard board
	 */
	CheckersBoard board;
	
	/**
	 * Main method used to launch application
	 */
	public static void main(String[] args) { //main launch method
		launch(args);
	}

	/**
	 * Start method for Checkers GUI Application
	 * 
	 * @param primaryStage
	 * @Override
	 */
	@Override
	public void start(Stage primaryStage) { // Main GUI Method (whole game should fit in here)

		Pane root = new Pane();
		
		// UI Elements + their actions
		Button pVp = new Button("Player vs Player");
		Button pVc = new Button("Player vs Computer");
		
		// Loads in board from within CheckersBoard class
		board = new CheckersBoard();
		board.printBoard();
	
		// Add board to Pane
		root.getChildren().addAll(board);
		
		// Container for game mode Selection
		VBox buttonContainer = new VBox(10); // Spacing between buttons
		buttonContainer.getChildren().addAll(pVp, pVc);
		buttonContainer.setAlignment(Pos.CENTER);
		
		// Initial scene for game mode collection
		chooseScene = new Scene(buttonContainer, 300, 300);
		
		// Main Checkers scene
		Scene checkerScene = new Scene(root,320,320);
		primaryStage.setResizable(false);
		
		// Setting the Player vs Player button to initialize a new game
		pVp.setOnAction(e->{
			primaryStage.setScene(checkerScene); // set to checkerScene
			board.newGame();
		});
		// Setting the Player vs Computer button to initialize a new game
		pVc.setOnAction(e->{
			primaryStage.setScene(checkerScene); // set to checkerScene
			board.newGame();
		});
		// Setting the mouseAction to read value of event
		board.setOnMousePressed(e->{
			board.mousePressed(e);
		});
	
		primaryStage.setTitle("Checkers Game");
		primaryStage.setScene(chooseScene);
        primaryStage.show();
	}
	
	/**
	 * This class exists to constitute a valid move within a Checkers game.
	 * Stores information on source Row & Column as well as destination Row 
	 * & Column. Only issue is the failure to guarantee valid moves within
	 * this class which is why there are fail-safes.
	 */
	private static class CheckersMove {
		// Integer placeholders for source and destinations
		int fromR, fromC;
		int toR, toC;
		
		/**
		 * This method exists to pass itself back values
		 */
		CheckersMove(int rx, int cx, int ry, int cy) {
			fromR = rx;
			fromC = cx;
			toR = ry;
			toC = cy;
		}
		/**
		 * Simple boolean method to verify the distance of a jump
		 */
		boolean validJump() {
			return (Math.abs(fromR - toR) == 2);
		}
	}
	
	/**
	 * This class begins by providing a display of a 320x320 pixel Checkers 
	 * board, and contains methods to handle the start of a newGame, mouseEvents 
	 * by the user, moveAttempts, and printBoard to repeatedly update the GUI
	 * with an updated board.
	 */
	private class CheckersBoard extends Canvas {
		
		// Holds the info for a valid board including validity of moves
		CheckersInfo board;
		
		// Is gameRunning is the gameOver condition (true when true and false when false)
		boolean gameRunning;
		
		// Either RED or WHITE (corresponding piece colors)
		int currentPlayer;
		int selectRow, selectCol;
		
		// Contains all valid moves for respective player
		CheckersMove[] validMoves;
		
		/**
		 * CheckerBoard creates a canvas, and CheckersInfo to contain contents
		 * of board followed by starting Checkers Game
		 */
		CheckersBoard() {
			super(320,320); // Canvas is 320x320 pixels
			board = new CheckersInfo();
			newGame();
		}
		/**
		 * This method starts a new game and is only called when first created
		 * and called upon by the pVp button or pVc button.
		 */
		void newGame() {
			// Establishes placement of pieces
			board.setUpGame();
			
			// Sets currentPlayer
			currentPlayer = CheckersInfo.RED;
			
			// Searches for any validMoves Red has
			validMoves = board.getValidMoves(CheckersInfo.RED);
			
			// Set selects to a non valid input
			selectRow = -2; 
			selectCol = -2;
			
			gameRunning = true;
			
			// Update board
			printBoard();
		}
		/**
		 * This method is fetched by mousePressed() within the start application method, 
		 * and only stores the value of clicked cell that has also has a valid move, 
		 * in respective selectRow and selectCol which will then pass on to next method
		 * to attempt to make a validMove with those stored values.
		 * 
		 * @param row
		 * @param col
		 */
		void tryClickCell(int row, int col) {
			for (int a = 0; a < validMoves.length; a++) {
				if (validMoves[a].fromR == row && validMoves[a].fromC == col) {
					selectRow = row;
					selectCol = col;
					printBoard();
					return;
				}
			}
			// For loop for attempting validMoves
			for (int a = 0; a < validMoves.length; a++) {
				if (validMoves[a].fromR == selectRow && validMoves[a].fromC == selectCol && validMoves[a].toR == row && validMoves[a].toC == col) {
					tryMakeMove(validMoves[a]);
					return;
				}
			}
		}
		
		/**
		 * This method is called once player has chosen a valid piece with a valid move.
		 * This method will make the move and continue the next turn.
		 * 
		 * @param move
		 */
		void tryMakeMove(CheckersMove move) {
			board.makeMove(move);
			
			//
			if (move.validJump()) {
				validMoves = board.getValidJumps(currentPlayer, move.toR, move.toC);
				if (validMoves != null) {
					selectRow = move.toR;
					selectCol = move.toC;
					printBoard();
					return;
				}
			}
			// Checks in advance whether or not the next players turn has any valid moves
			// if not game is over.
			if (currentPlayer == CheckersInfo.RED) {
				currentPlayer = CheckersInfo.WHITE;
				validMoves = board.getValidMoves(currentPlayer);
				if (validMoves == null) {
					gameRunning = false;
				}
			} else {
				currentPlayer = CheckersInfo.RED;
				validMoves = board.getValidMoves(currentPlayer);
				if (validMoves == null) {
					gameRunning = false;
				}
			}
			// Set selects to a non valid input
			selectRow = -2;
			selectCol = -2;
			
			// Update board
			printBoard();
		}
		
		/**
		 * This method exists to print out the current state of the board.
		 */
		public void printBoard() {
			GraphicsContext gC = getGraphicsContext2D();
			
			// Draw squares of Checker board
			for (int row = 0; row < 8; row++) {
				for (int col = 0; col < 8; col++) {
					
					// Check condition of background cells and color accordingly
					if ((row + col) % 2 == 0) {
						gC.setFill(Color.DARKKHAKI);
					} else {
						gC.setFill(Color.FORESTGREEN);
					}
					gC.fillRect(col*40,row*40,40,40);
					
					// Switch statement for cases of presence of pieces (switch allows for expansion of future pieces)
					switch (board.pieceAt(row, col)) {
					case CheckersInfo.WHITE:
						gC.setFill(Color.WHITE);
						gC.fillOval(col*40,row*40,38,38);
						break;
					case CheckersInfo.RED:
						gC.setFill(Color.RED);
						gC.fillOval(col*40,row*40,38,38);
						break;
					}
				}
			}
		}
		/**
		 * This method exists to read the react to a users click on the
		 * board, and executes tryClickCell on passed click.
		 */
		public void mousePressed(MouseEvent e) {
			if (gameRunning == false) {
				return;
			} else {
				int col = (int)((e.getX())/40);
				int row = (int)((e.getY())/40);
				if (col >= 0 || col < 8 && row >= 0 && row < 8) {
					tryClickCell(row,col);
				}
			}
		}
	}
	
	/**
	 * This static class exists to hold the data about a Checkers game, 
	 * including respective game rules that apply to respective pieces.
	 */
	private static class CheckersInfo {
		
		// Static Finals of 3 possible states of spaces on a board
		static final int EMPTY = 0;
		static final int RED = 1;
		static final int WHITE = 2;
		
		// board[row][column]
		int[][] board; 
		
		// Constructs board and sets up a new game
		CheckersInfo() {
			board = new int[8][8];
			setUpGame();
		}
		
		/**
		 * This method exists to set up board in original state 
		 * [white on top and red on the bottom].
		 */
		void setUpGame() {
			for (int row = 0; row < 8; row++) {
				for (int col = 0; col < 8; col++) {
					if ((row + col) % 2 == 0) {
						board[row][col] = EMPTY;
					} else {
						if (row < 3) {
							board[row][col] = WHITE;
						} else if (row > 4) {
							board[row][col] = RED;
						} else {
							board[row][col] = EMPTY;
						}
					}
				}
			}
		}
		
		/**
		 * This method exists to return the integer value associated
		 * with respective piece at a give row & column.
		 * 
		 * @param row
		 * @param col
		 * 
		 * @return board[row][col]
		 */
		int pieceAt(int row, int col) {
			return board[row][col];
		}
		
		/**
		 * This method exists to execute a move (under the presumption
		 * that the move is valid).
		 * 
		 * @param move
		 */
		void makeMove(CheckersMove move) {
			makeMove(move.fromR, move.fromC, move.toR, move.toC);
		}
		
		/**
		 * This method exists to 'make move' from selected position on
		 * board to selected destination on board. Takes into account
		 * capturing mechanic.
		 */
		void makeMove(int fromR, int fromC, int toR, int toC) {
			board[toR][toC] = board[fromR][fromC];
			board[fromR][fromC] = EMPTY;
			
			// If statement checks if distance 2 and if the capturing
			// piece is different from source piece
			if (Math.abs(fromR - toR) == 2 && pieceAt(fromR, fromC) != pieceAt((fromR+toR)/2, (fromC+toC)/2)) {
				// move is a jump over a piece that is not the same as itself
				// Remove jumped piece from board
				int captureRow = (fromR+toR)/2;
				int captureCol = (fromC+toC)/2;
				board[captureRow][captureCol] = EMPTY;
				
				// If adding Kinging, add here
			}
		}
		/**
		 * This method exists to return an array of all possible valid
		 * moves for the current passed player.
		 * 
		 * @param player
		 * @return validMoveArray
		 * @return null; if no valid moves
		 */
		CheckersMove[] getValidMoves(int player) {
			if (player != WHITE && player != RED) {
				return null; // Who even are you ?
			}
			
			// Store within a reachable array
			ArrayList<CheckersMove> gotMoves = new ArrayList<CheckersMove>();
			
			/*
			 * For loop iterates through any possible moves, then iterates
			 * through any possible jumps storing the valid moves into
			 * the reachable array.
			 */
			for (int row = 0; row < 8; row++) {
				for (int col = 0; col < 8; col++) {
					if (board[row][col] == player) {
						if (checkMove(player, row, col, row + 1, col + 1)) {
							gotMoves.add(new CheckersMove(row, col, row + 1, col + 1));
						}
						if (checkMove(player, row, col, row - 1, col - 1)) {
							gotMoves.add(new CheckersMove(row, col, row - 1, col - 1));
						}
						if (checkMove(player, row, col, row - 1, col + 1)) {
							gotMoves.add(new CheckersMove(row, col, row - 1, col + 1));
						}
						if (checkMove(player, row, col, row + 1, col - 1)) {
							gotMoves.add(new CheckersMove(row, col, row + 1, col - 1));
						}
					}
				}
			}
			for (int row = 0; row < 8; row++) {
				for (int col = 0; col < 8; col++) {
					if (board[row][col] == player) {
						if (checkJump(player, row, col, row + 1, col + 1, row + 2, col + 2)) {
							gotMoves.add(new CheckersMove(row, col, row + 2, col + 2));
						}
						if (checkJump(player, row, col, row - 1, col - 1, row - 2, col - 2)) {
							gotMoves.add(new CheckersMove(row, col, row - 2, col - 2));
						}
						if (checkJump(player, row, col, row - 1, col + 1, row - 2, col + 2)) {
							gotMoves.add(new CheckersMove(row, col, row - 2, col + 2));
						}
						if (checkJump(player, row, col, row + 1, col - 1, row + 2, col - 2)) {
							gotMoves.add(new CheckersMove(row, col, row + 2, col - 2));
						}
					}
				}
			}
			if (gotMoves.size() == 0) { // If after all of this still no valid moves , return null
				return null;
			} else {
				// If size not 0
				CheckersMove[] validMoveArray = new CheckersMove[gotMoves.size()];
				for (int a = 0; a < gotMoves.size(); a++) {
					validMoveArray[a] = gotMoves.get(a);
				}
				return validMoveArray;
			}
		}
		/**
		 * This method exists to return an ArrayList of valid jumps of the
		 * current player for a given Row & Column.
		 * 
		 * @param player
		 * @param row
		 * @param col
		 * 
		 * @return validMoveArray
		 * @return null; if no valid moves
		 */
		CheckersMove[] getValidJumps(int player, int row, int col) {
			if (player != WHITE && player != RED) {
				return null; // Who even are you ?
			}
			
			// Store within a reachable array
			ArrayList<CheckersMove> gotMoves = new ArrayList<CheckersMove>();
			if (board[row][col] == player) {
				if (checkJump(player, row, col, row + 1, col + 1, row + 2, col + 2)) {
					gotMoves.add(new CheckersMove(row, col, row + 2, col + 2));
				}
				if (checkJump(player, row, col, row + 1, col + 1, row - 2, col - 2)) {
					gotMoves.add(new CheckersMove(row, col, row - 2, col - 2));
				}
				if (checkJump(player, row, col, row + 1, col + 1, row - 2, col + 2)) {
					gotMoves.add(new CheckersMove(row, col, row - 2, col + 2));
				}
				if (checkJump(player, row, col, row + 1, col + 1, row + 2, col - 2)) {
					gotMoves.add(new CheckersMove(row, col, row + 2, col - 2));
				}
			}
			if (gotMoves.size() == 0) {
				return null;
			} else {
				// If size not 0, pass array of valid moves
				CheckersMove[] validMoveArray = new CheckersMove[gotMoves.size()];
				for (int a = 0; a < gotMoves.size(); a++) {
					validMoveArray[a] = gotMoves.get(a);
				}
				return validMoveArray;
			}
		}
		/**
		 * This method exists to validate the execution of a jump and
		 * whether or not it is allowed for current board.
		 * 
		 * @param player
		 * @param rx: source row
		 * @param cx: source column
		 * @param ry: capture row
		 * @param cy: capture column
		 * @param rz: destination row
		 * @param cz: destination column
		 * 
		 * @return true; valid jump
		 * @return false; invalid jump
		 */
		private boolean checkJump(int player, int rx, int cx, int ry, int cy, int rz, int cz) {
			// Check if jump destination is within bounds of board
			if (rz < 0 || rz >= 8 || cz < 0 || cz >= 8) {
				return false;
			}
			// Check if jump destination is open
			if (board[rz][cz] != EMPTY) {
				return false;
			}
			// Red condition checks
			if (player == RED) {
				// Checks if Red is moving in the correct direction
				if (board[rx][cx] == RED && rz > rx) {
					return false;
				}
				// Checks the piece Red is jumping is not Red
				if (board[ry][cy] != WHITE && board[ry][cy] != EMPTY) {
					return false;
				}
				return true; // Valid jump
			}
			// White condition checks
			if (player == WHITE) {
				// Checks if White is moving in the correct direction
				if (board[rx][cx] == WHITE && rz < rx) {
					return false;
				}
				// Checks the piece White is jumping is not White
				if (board[ry][cy] != RED && board[ry][cy] != EMPTY) {
					return false;
				}
				return true; // Valid jump
			}
			return false;
		}
		/**
		 * This method exists to verify if current player is able to move
		 * piece from source to destination.
		 * 
		 * @param player
		 * @param rx: source row
		 * @param cx: source column
		 * @param ry: destination row
		 * @param cy: destination column
		 * 
		 * @return true: valid move
		 * @return false: invalid move
		 */
		private boolean checkMove(int player, int rx, int cx, int ry, int cy) {
			// Check if move destination is within bounds of board
			if (ry < 0 || ry >= 8 || cy < 0 || cy >= 8) {
				return false;
			}
			// Check if move destination is open
			if (board[ry][cy] != EMPTY) {
				return false;
			}
			// Red condition checks
			if (player == RED) {
				// Checks if Red is moving in the correct direction
				if (board[rx][cx] == RED && ry > rx) {
					return false;
				}
				return true; // Valid move
			}
			// White condition checks
			if (player == WHITE) {
				// Checks if White is moving in the correct direction
				if (board[rx][cx] == WHITE && ry < rx) {
					return false;
				}
				return true; // Valid move
			}
			return false;
		}
	}
}
