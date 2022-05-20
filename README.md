CONTENTS:
This directory contains a software implementation of the game, Line Of Action.

"Lines of Action is a board game invented by Claude Soucie. It is played on a checkerboard with ordinary checkers pieces. The two players take turns, each moving a piece, and possibly capturing an opposing piece. The goal of the game is to get all of one’s pieces into one group of pieces that are connected."

	
	Makefile		A makefile that will compile your
				files and run tests.  You must turn in a Makefile,
				'make' must compile all your files, and 
				'make check' must perform all your tests.  
				Currently, this makefile is set up to do just 
				that with our skeleton files.  Be sure to keep 
				it up to date.

	loa/			Directory containing the Lines of Action package.

	    Makefile		A convenience Makefile so that you can issue 
					compilation commands from the game directory.

	    Piece.java		An enumeration type describing the kinds of pieces.

	    Board.java		Represents a game board.  Contains much of the
					machinery for checking or generating possible moves.

	    Square.java         	Represents a position on a Board.

	    Move.java		Represents a single move.

	    Game.java           	Controls play of the game.  Calls on Players to
					provide moves, executes other commands,
				and maintains a current Board.

	    Player.java		Supertype representing common characteristics of
					players.

	    HumanPlayer.java	A kind of Player that reads moves from the standard
					input (i.e., presumably from a human player).

	    MachinePlayer.java	A kind of Player that chooses its moves automatically.

	    Reporter.java		The supertype of "reporters", which announce errors,
					moves, and other notes to the user.

	    TextReporter.java	A type of Reporter that uses the standard output
					(generally the terminal) for output.

	    View.java		An interface for things that display the Board on
					each move.

	    NullView.java		A View that does nothing.

	    Utils.java          	Assorted utility functions for debugging messages and
					error reporting.

	    UnitTests.java      	Class that coordinates unit testing of the loa package.

	    BoardTest.java      	Class containing unit tests of the Board class.

	    HelpText.txt        	Contains a brief description of the commands (intended
					for printing when help requested).

	    GUI.java            	A class that represents a graphical user interface
					(GUI) for the Loa game.

	    BoardWidget.java    	Used by the GUI class to display the board.

	    GUIPlayer.java      	A type of manual Player that takes move from the GUI.

	    About.html           
	    Help.html           	Files displayable by the GUI containing various
					documentation.
