package hw9;
//Set

//Kai DeLorenzo & Teal Witter
//CS 201 A HW 9

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*; // for Stack

@SuppressWarnings("deprecation")
public class Set extends Applet implements KeyListener, Runnable {

	private static final long serialVersionUID = 1L; // avoid warning

	// instance variables
	protected int time;
	protected SetCanvas c;
	protected TimerCanvas t;
	protected Label userScore, compScore;
	protected Vector<Integer> userEntry;
	protected Thread thread;

	public void init() {
		// initializes instance variables
		// nice, readable font
		setFont(new Font("BellMT", Font.PLAIN, 16));
		userScore = new Label("USER: 0");
		compScore = new Label("COMP: 0");
		// store user's guesses
		userEntry = new Vector<Integer>();

		setLayout(new BorderLayout());
		add("North", makeScorePanel()); // add at top
		add("South", makeTimePanel()); // add at bottom
		add("Center", makeSetCanvas()); // add middle
	}

	private Component makeSetCanvas() {
		// center canvas with cards
		c = new SetCanvas(this);
		c.addKeyListener(this); // key listener

		return c;
	}

	private Component makeTimePanel() {
		// panel for displaying time
		int timeHeight = 12;
		Dimension d = new Dimension(getSize().width, timeHeight);
		t = new TimerCanvas();

		Panel timePanel = new Panel();
		timePanel.setPreferredSize(d);
		timePanel.setLayout(new BorderLayout());
		timePanel.add("Center", t);

		return timePanel;
	}

	private Component makeScorePanel() {
		// score panel with title and credits
		Panel scorePanel = new Panel();

		scorePanel.setForeground(Color.white);
		scorePanel.setBackground(Colors.dlave); // access color from canvas class
		scorePanel.setLayout(new GridBagLayout());
		GridBagConstraints d = new GridBagConstraints();

		changeD(d, 0.5, 1, 1, 0, 0);
		scorePanel.add(userScore, d);

		changeD(d, 0.5, 1, 1, 0, 1);
		scorePanel.add(compScore, d);

		changeD(d, 0.5, 2, 2, 1, 0);
		Panel titlePanel = new Panel();
		titlePanel.setFont(new Font("TimesRoman", Font.PLAIN, 31));
		titlePanel.add(new Label("Set", Label.CENTER));
		scorePanel.add(titlePanel, d);

		changeD(d, 0.5, 1, 1, 3, 1);
		Label namesLabel = new Label("By Kai & Teal ", Label.RIGHT);
		scorePanel.add((namesLabel), d);

		return scorePanel;
	}

	public void changeD(GridBagConstraints d, double weightX, int gridWidth,
			int gridHeight, int gridX, int gridY) {
		// manipulates words so that appear correctly
		d.fill = GridBagConstraints.HORIZONTAL;
		d.weightx = weightX;
		d.gridwidth = gridWidth;
		d.gridheight = gridHeight;
		d.gridx = gridX;
		d.gridy = gridY;
	}

	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	public void stop() {
		thread = null;
	}

	public void run() {
		Thread currentThread = Thread.currentThread();
		// t.starttime = System.currentTimeMillis();
		while (currentThread == thread) {
			try {
				Thread.sleep(10); // wait .1 seconds
			} catch (InterruptedException e) {
				// do nothing
			}

			if (t.timeLeft < 0) {
				computerTurn();
				t.resetTime(getScore(userScore), getScore(compScore));
			}
			System.out.println(t.timeLeft);
			t.timeLeft -= 0.01;
			t.repaint();
		}
	}

	public void changeScore(Label score, int change) {
		// changes score in a label
		String[] sArray = score.getText().split(" "); // accesses current score
		int value = Math.max(0, Integer.parseInt(sArray[1]) + change); // updates
																		// score
		score.setText(sArray[0] + " " + Integer.toString(value)); // puts label
																	// back
																	// together
	}

	public int getScore(Label score) {
		return Integer.parseInt(score.getText().split(" ")[1]);
	}

	public void computerTurn() {
		Vector<Integer[]> sets = c.findSets(c.onDisplay);
		if (sets.size() == 0) {
			changeScore(compScore, 1);
			c.onDisplay.add(c.deckOrder.pop());
		} else {
			changeScore(compScore, 3);
			c.onDisplay.remove(sets.get(0)[0]);
			c.onDisplay.remove(sets.get(0)[1]);
			c.onDisplay.remove(sets.get(0)[2]);
		}
		c.repaint();
	}

	public void keyTyped(KeyEvent e) {
		// placeholder
	}

	public void keyPressed(KeyEvent e) {
		// handles key presses
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_BACK_SPACE && userEntry.size() != 0) {
			// removes most recent element in userEntry
			userEntry.remove(userEntry.size() - 1);
		} else if (keyCode == KeyEvent.VK_ESCAPE) {
			// user indicated they think there are no sets
			if (c.findSets(c.onDisplay).size() == 0) {
				// if there are no sets, give one point and add a card
				changeScore(userScore, 1);
				c.onDisplay.add(c.deckOrder.pop());
				t.resetTime(getScore(userScore), getScore(compScore));
			} else {
				// otherwise, minus one point!
				changeScore(userScore, -1);
			}
		} else if (keyCode == KeyEvent.VK_ENTER && userEntry.size() == 3) {
			// enters current guesses
			// convert to userArray in order to sort for easy comparison
			Integer[] userArray = new Integer[3];
			userEntry.toArray(userArray);
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
				c.onDisplay.remove(userEntry.get(0));
				c.onDisplay.remove(userEntry.get(1));
				c.onDisplay.remove(userEntry.get(2));
				t.resetTime(getScore(userScore), getScore(compScore));
			} else {
				// else, minus 3!
				changeScore(userScore, -3);
			}
			userEntry.clear();
		} else if (65 <= keyCode && keyCode < c.onDisplay.size() + 65
				&& userEntry.contains(c.onDisplay.get(keyCode - 65))) {
			// if existing card typed again, then it is removed
			userEntry.remove(c.onDisplay.get(keyCode - 65));

		} else if (65 <= keyCode && keyCode < c.onDisplay.size() + 65
				&& userEntry.size() < 3) {
			// if valid card is typed, then it is added
			userEntry.add(c.onDisplay.get(keyCode - 65));
		}
		// repaints
		c.repaint();

	}

	public void keyReleased(KeyEvent e) {
		// placeholder
	}

}

class SetCanvas extends Canvas {

	private static final long serialVersionUID = 1L;

	// instance variables
	Set parent;
	protected int[][] deckMaster; // 0,1,2,3 => color, filling, shape, number
	protected Stack<Integer> deckOrder; // random, non-repeated indices to
										// deckMaster
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
		// got the next two commands from:
		// https://stackoverflow.com/questions/28477330/java-resize-double-buffer
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Image offscreen = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration()
				.createCompatibleVolatileImage(d.width, d.height);
		// offscreen image for double buffering
		Graphics g2 = offscreen.getGraphics();
		// carries over font to g2
		g2.setFont(getFont());
		for (int i = 0; i < onDisplay.size(); i++) {
			// check to see if in userEntry in order
			if (parent.userEntry.contains(onDisplay.get(i))) {
				// draws bigger card to indicate it is displayed
				drawCard(g2, i, 1.1);
			} else {
				drawCard(g2, i, .8);
			}
		}
		g.drawImage(offscreen, 0, 0, null);
	}

	public Color lighten(Color color) {
		// lightens darker color to ligher color
		if (color == Colors.dteal)
			return Colors.lteal;
		if (color == Colors.dlave)
			return Colors.llave;
		else
			return Colors.lsalm;
	}

	public Color getColor(int i) {
		// gets color based off of i
		return new Color[] { Colors.dteal, Colors.dlave, Colors.dsalm }[i];
	}

	public Color getFilling(Color color, int i) {
		// determines correct filling style depending on i
		Color filling = null;
		if (i == 0)
			filling = Color.white;
		if (i == 1)
			filling = lighten(color);
		if (i == 2)
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
			parent.setFont(new Font("BellMT", Font.PLAIN, 20));
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
		timeAllotted = (double) (45 * (compScore + 1) / (userScore + 1));
		timeLeft = timeAllotted;
		starttime = System.currentTimeMillis();
	}

	// height
	public void paint(Graphics g) {
		Dimension d = getSize();
		int rectHeight = d.height;
		int rectWidth = (int) ((d.width * timeLeft / timeAllotted));
		// g.setColor(new Color(255, 102, 102));
		g.setColor(Colors.dlave);
		g.fillRoundRect(0, 0, rectWidth, rectHeight, 3, 10);
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