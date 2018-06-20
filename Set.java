package hw9;
// Set

// The world's best card game turned digital
// Kai DeLorenzo & Teal Witter
// CS 201A Final Project

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*; // for Stack

@SuppressWarnings("deprecation")
public class Set extends Applet implements KeyListener, Runnable {
	private static final long serialVersionUID = 1L; // avoid warning

	// instance variables
	protected SetCanvas c;
	protected TimerCanvas t;
	protected Label title, userScore, compScore;
	protected Vector<Integer> setEntry;
	protected Thread thread;
	protected boolean isOver;

	public void init() {
		// initializes instance variables and creates layout

		// nice, readable font
		setFont(new Font("BellMT", Font.PLAIN, 20));
		userScore = new Label("USER: 0");
		compScore = new Label("COMP: 0");
		title = new Label("      Set      ", Label.CENTER);
		setEntry = new Vector<Integer>(); // store user's guesses
		isOver = false;

		setLayout(new BorderLayout());
		add("North", makeScorePanel()); // add at top
		add("South", makeTimePanel()); // add at bottom
		add("Center", makeSetCanvas()); // add middle
	}

	private Component makeSetCanvas() {
		// creates center canvas with shapes
		c = new SetCanvas(this);
		c.addKeyListener(this); // key listener

		return c;
	}

	private Component makeTimePanel() {
		// panel for countdown timer
		int timeHeight = 12;
		Dimension d = new Dimension(getSize().width, timeHeight);
		t = new TimerCanvas();

		Panel timePanel = new Panel();
		timePanel.setPreferredSize(d);
		timePanel.setLayout(new BorderLayout());
		timePanel.setBackground(Color.white);
		timePanel.add("Center", t);

		return timePanel;
	}

	private Component makeScorePanel() {
		// score panel with title and credits
		Panel scorePanel = new Panel();
		scorePanel.setForeground(Color.white);
		scorePanel.setBackground(Colors.dlave);
		scorePanel.setLayout(new GridBagLayout());

		GridBagConstraints contraints = new GridBagConstraints();

		changeGridBagConstraints(contraints, 0.5, 1, 1, 0, 0);
		scorePanel.add(userScore, contraints);

		changeGridBagConstraints(contraints, 0.5, 1, 1, 0, 1);
		scorePanel.add(compScore, contraints);

		changeGridBagConstraints(contraints, 0.5, 2, 2, 1, 0);
		Panel titlePanel = new Panel();
		titlePanel.setFont(new Font("BellMT", Font.PLAIN, 31));
		titlePanel.add(title);
		scorePanel.add(titlePanel, contraints);

		changeGridBagConstraints(contraints, 0.5, 1, 1, 3, 1);
		// Label namesLabel = new Label("By Kai & Teal ", Label.RIGHT);
		scorePanel.add((new Label("By Kai & Teal ", Label.RIGHT)), contraints);

		return scorePanel;
	}

	public void changeGridBagConstraints(GridBagConstraints c, double weightX,
			int gridWidth, int gridHeight, int gridX, int gridY) {
		// Changes GridBagConstraints
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = weightX;
		c.gridwidth = gridWidth;
		c.gridheight = gridHeight;
		c.gridx = gridX;
		c.gridy = gridY;
	}

	public void start() {
		// initializes thread
		thread = new Thread(this);
		thread.start();
	}

	public void stop() {
		// quits thread
		thread = null;
	}

	public void run() {
		Thread currentThread = Thread.currentThread();
		boolean timeToPaint = true;
		while (currentThread == thread) {
			try {
				Thread.sleep(10); // wait .1 seconds
			} catch (InterruptedException e) {
				// do nothing
			}
			if (t.timeLeft < 0) {
				computerTurn();
			}
			t.timeLeft -= 0.01;
			if (timeToPaint) {
				t.repaint();
				timeToPaint = false;
			} else {
				timeToPaint = true;
			}
			if (isOver) {
				title.setText("Game Over");
				t.timeLeft = 0;
				// System.out.println(c.deckOrder);
				break;
			}
		}
	}

	public void changeScore(Label score, int change) {
		// changes one of the score labels
		String[] sArray = score.getText().split(" "); // accesses current score
		// updates score
		int value = Math.max(0, Integer.parseInt(sArray[1]) + change);
		// puts label back together
		score.setText(sArray[0] + " " + Integer.toString(value));
	}

	public int getScore(Label score) {
		// gets score from score label
		return Integer.parseInt(score.getText().split(" ")[1]);
	}

	public void addCards(int num) {
		for (int i = 0; i < num; i++) {
			c.onDisplay.add(c.deckOrder.pop());
		}
	}

	public void computerTurn() {
		// gives computer points and changes board if the timer runs out
		Vector<Integer[]> sets = c.findSets(c.onDisplay);
		if (sets.size() == 0) {
			changeScore(compScore, 1);
			isOver = c.deckOrder.size() < 3;
			if (!isOver) {
				addCards(3);
			} else {
				c.removeKeyListener(c.getKeyListeners()[0]);
			}
		} else {
			changeScore(compScore, 3);
			// show the computer's set for 2 seconds
			setEntry = new Vector<Integer>(Arrays.asList(sets.get(0)));
			c.repaint();
			try {
				Thread.sleep(2000); // wait 2 seconds
			} catch (InterruptedException e) {
				// do nothing
			}
			for (int i = 0; i < 3; i++) {
				c.onDisplay.remove(sets.get(0)[i]);
			}
		}
		setEntry.clear();
		c.repaint();
		t.resetTime(getScore(userScore), getScore(compScore));
	}

	public void keyPressed(KeyEvent e) {
		// handles key presses
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_BACK_SPACE && setEntry.size() != 0) {
			// removes most recent element in setEntry
			setEntry.remove(setEntry.size() - 1);
		} else if (keyCode == KeyEvent.VK_ESCAPE) {
			// user indicated they think there are no sets

			if (c.findSets(c.onDisplay).size() == 0) {
				// if there are no sets, give one point and add three cards
				changeScore(userScore, 1);
				isOver = c.deckOrder.size() < 3;
				if (!isOver) {
					addCards(3);
				} else {
					c.removeKeyListener(c.getKeyListeners()[0]);
				}
				t.resetTime(getScore(userScore), getScore(compScore));
			} else {
				// otherwise, minus one point!
				changeScore(userScore, -1);
			}
		} else if (keyCode == KeyEvent.VK_ENTER && setEntry.size() == 3) {
			// enters current guesses
			// convert to userArray in order to sort for easy comparison
			Integer[] userArray = new Integer[3];
			setEntry.toArray(userArray);
			Arrays.sort(userArray);
			// finds all the actual sets
			Vector<Integer[]> sets = c.findSets(c.onDisplay);
			// checks for whether the guessed set is there
			boolean contained = false;
			for (int i = 0; i < sets.size(); i++) {
				if (Arrays.equals(userArray, sets.get(i)))
					contained = true;
			}
			if (contained) {
				// if contained, updates score and removes cards
				changeScore(userScore, 3);
				for (int i = 0; i < 3; i++) {
					c.onDisplay.remove(setEntry.get(i));
				}
				t.resetTime(getScore(userScore), getScore(compScore));
			} else {
				// else, minus 3!
				changeScore(userScore, -3);
			}
			setEntry.clear();
		} else if (65 <= keyCode && keyCode < c.onDisplay.size() + 65
				&& setEntry.contains(c.onDisplay.get(keyCode - 65))) {
			// if existing card typed again, then it is removed
			setEntry.remove(c.onDisplay.get(keyCode - 65));

		} else if (65 <= keyCode && keyCode < c.onDisplay.size() + 65
				&& setEntry.size() < 3) {
			// if valid card is typed, then it is added
			setEntry.add(c.onDisplay.get(keyCode - 65));
		}
		c.repaint();
	}

	public void keyReleased(KeyEvent e) {
		// placeholder
	}

	public void keyTyped(KeyEvent e) {
		// placeholder
	}
}

class SetCanvas extends Canvas {
	// main shapes canvas
	private static final long serialVersionUID = 1L;

	// instance variables
	Set parent;
	protected int[][] deckMaster; // 0,1,2,3 => color, filling, shape, number
	// random, non-repeated indices to deckMaster
	protected Stack<Integer> deckOrder;
	protected Vector<Integer> onDisplay; // cards on display

	// constructors
	public SetCanvas(Set s) {
		parent = s; // allows access to parent instance variables
		deckMaster = new int[81][4];
		deckOrder = new Stack<Integer>();
		onDisplay = new Vector<Integer>(26);

		// initialize deckMaster with all 81 permutations of {0,1,2}^3
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 3; k++) {
					for (int l = 0; l < 3; l++) {
						int index = l + 3 * k + 9 * j + 27 * i;
						// unique index accessible from each element
						deckMaster[index][0] = i;
						deckMaster[index][1] = j;
						deckMaster[index][2] = k;
						deckMaster[index][3] = l;
					}
				}
			}
		}

		// initialize deckOrder
		int counter = 0;
		int randint;
		Random randInt = new Random();
		while (counter < 81) {
			randint = Math.abs(randInt.nextInt() % 81);
			// checks to see if randInt already in deckOrder
			// expected O(n) time
			if (deckOrder.search(randint) < 0) {
				deckOrder.push(randint);
				counter++;
			}
		}

		// initialize onDisplay
		for (int i = 0; i < 12; i++)
			onDisplay.add(deckOrder.pop());
	}

	public Vector<Integer[]> findSets(Vector<Integer> displayed) {
		// finds all sets in displayed and returns all unique sets
		Vector<Integer[]> sets = new Vector<Integer[]>();
		// vector because unknown number of sets
		for (int i = 0; i < displayed.size(); i++) {
			for (int j = i + 1; j < displayed.size(); j++) {
				// based on two cards, determines the 'ideal' card that
				// completes the set
				int[] ideal = new int[4];
				for (int k = 0; k < 4; k++) {
					int val1 = deckMaster[displayed.get(i)][k];
					int val2 = deckMaster[displayed.get(j)][k];
					// if value the same, then ideal has the same
					if (val1 == val2)
						ideal[k] = val1;
					// if value not the same, then ideal has the one neither had
					else
						ideal[k] = 3 - (val1 + val2); // generates not present
														// value
				}
				// use unique index based off of card
				int index = ideal[3] + 3 * ideal[2] + 9 * ideal[1]
						+ 27 * ideal[0];
				// checks whether ideal card in onDisplay
				if (displayed.contains(index)) {
					// creates potential addition to sets
					Integer[] potential = new Integer[] { displayed.get(i),
							displayed.get(j), index };
					// sorts so that containment checks can work
					Arrays.sort(potential);
					boolean contained = false;
					for (int l = 0; l < sets.size(); l++) {
						if (Arrays.equals(sets.get(l), potential)) {
							contained = true;
						}
					}
					// adds potential if not already in sets
					if (!contained)
						sets.add(potential);
				}
			}
		}
		return sets;
	}

	public void paint(Graphics g) {
		// paints canvas
		// got the next two commands from:
		// https://stackoverflow.com/questions/28477330/java-resize-double-buffer
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Image offscreen = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration()
				.createCompatibleVolatileImage(d.width, d.height);
		// offscreen image for double buffering
		Graphics g2 = offscreen.getGraphics();
		g2.setFont(new Font("BellMT", Font.PLAIN, 25));
		for (int i = 0; i < onDisplay.size(); i++) {
			// check to see if in setEntry in order
			if (parent.setEntry.contains(onDisplay.get(i))) {
				// draws bigger card to indicate it is displayed
				drawCard(g2, i, 1.1);
			} else {
				drawCard(g2, i, .8);
			}
		}
		g.drawImage(offscreen, 0, 0, null);
	}

	public Color lighten(Color color) {
		// lightens darker color to lighter color
		Color lighter;
		if (color == Colors.dteal)
			lighter = Colors.lteal;
		else if (color == Colors.dlave)
			lighter = Colors.llave;
		else // == dsalm
			lighter = Colors.lsalm;
		return lighter;
	}

	public Color getColor(int i) {
		// gets color based off of i
		return new Color[] { Colors.dteal, Colors.dlave, Colors.dsalm }[i];
	}

	public Color getFilling(Color color, int i) {
		// determines correct filling style depending on i
		Color filling;
		if (i == 0)
			filling = Color.white;
		else if (i == 1)
			filling = lighten(color);
		else // i == 2
			filling = color;
		return filling;
	}

	public void drawCard(Graphics g, int index, double multiply) {
		// draws each individual card
		Dimension d = getSize();
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(3)); // makes lines bolder
		int[] features = deckMaster[onDisplay.get(index)];
		// height and width of cards
		int h = d.height / 3;
		int w = d.width / ((onDisplay.size() + 2) / 3);
		// height and width of shapes
		int sh = (int) (multiply * 3 * h / 5);
		int sw = (int) (multiply * 1 * w / 4);
		// x and y of each space
		int y = h / 2 + h * ((index) % 3);
		int x = w / 2 + w * ((index) / 3);
		// x and y of each shape within the space
		int sy;
		int sx = 0;
		sy = y - 1 * sh / 2;
		// updates sx based on the number of shapes in each card
		switch (features[3]) {
		case 0:
			sx = x - 2 * sw / 4;
			break;
		case 1:
			sx = x - 5 * sw / 4;
			break;
		case 2:
			sx = x - 7 * sw / 4;
			break;
		}
		Color color = getColor(features[0]);
		Color filling = getFilling(color, features[1]);
		if (multiply < 1) {
			// draws letter only if not big
			g.setColor(color);
			g.drawString(Character.toString((char) (index + 97)),
					x - 4 * w / 11, y - 4 * h / 11);
		}
		for (int j = 0; j <= features[3]; j++) {
			// draws each shape within the card
			g.setColor(filling);
			// draws filling in shape based on feature value
			switch (features[2]) {
			case 0:
				g.fillOval(sx, sy, sw, sh);
				break;
			case 1:
				g.fillRect(sx, sy, sw, sh);
				break;
			case 2:
				g.fillPolygon(
						new int[] { sx + sw / 2, sx + sw, sx + sw / 2, sx },
						new int[] { sy, sy + sh / 2, sy + sh, sy + sh / 2 }, 4);
				break;
			}
			g.setColor(color);
			// draws shape based on feature value
			switch (features[2]) {
			case 0:
				g2.drawOval(sx, sy, sw, sh);
				break;
			case 1:
				g2.drawRect(sx, sy, sw, sh);
				break;
			case 2:
				g2.drawPolygon(
						new int[] { sx + sw / 2, sx + sw, sx + sw / 2, sx },
						new int[] { sy, sy + sh / 2, sy + sh, sy + sh / 2 }, 4);
				break;
			}
			sx += sw + 1 * sw / 4; // updates sx for each shape
		}

	}
}

class TimerCanvas extends Canvas {
	// bottom timer canvas
	private static final long serialVersionUID = 1L;

	// instance variables
	long starttime;
	double timeAllotted;
	double timeLeft;

	// constructor
	public TimerCanvas() {
		resetTime(0, 0);
	}

	public void resetTime(int userScore, int compScore) {
		// sets time
		int timeFactor = 18;
		timeAllotted = (double) (timeFactor * (compScore + 1)
				/ (userScore + 1));
		timeLeft = timeAllotted;
		starttime = System.currentTimeMillis();
	}

	public void paint(Graphics g) {
		// draw canvas
		double factor = timeLeft / timeAllotted;
		Dimension d = getSize();
		int rectHeight = d.height;
		int rectWidth = (int) (d.width * factor);
		g.setColor(getColor(factor));
		g.fillRoundRect(0, 0, rectWidth, rectHeight, 3, 6);
	}

	public Color getColor(double power) {
		// Creates changing color from
		// https://stackoverflow.com/questions/340209/generate-colors-between-red-and-green-for-a-power-meter
		double H = power * 0.4; // Hue
		double S = 0.9; // Saturation
		double B = 0.9; // Brightness
		return Color.getHSBColor((float) H, (float) S, (float) B);
	}
}

final class Colors {
	// pretty colors :)
	static final Color dteal = new Color(0, 153, 153);
	static final Color lteal = new Color(153, 255, 255);
	static final Color dlave = new Color(153, 51, 255);
	static final Color llave = new Color(204, 153, 255);
	static final Color dsalm = new Color(255, 102, 102);
	static final Color lsalm = new Color(255, 179, 179);
}