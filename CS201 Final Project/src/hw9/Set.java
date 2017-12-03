package hw9;
//Set
// germgerkkng
//Kai Delorenzo & Teal Witter
//CS 201 A HW 9

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*; // for Stack

public class Set extends Applet implements KeyListener {

	private static final long serialVersionUID = 1L; // avoid warning

	// instance variables
	protected int time;
	protected SetCanvas c;
	protected Label userScore, compScore;
	protected Vector<Integer> userEntry;

	public void init() {
		// Font to use in Applet
		setFont(new Font("TimesRoman", Font.PLAIN, 16));
		userScore = new Label("USER: 0");
		compScore = new Label("COMP: 0");
		userEntry = new Vector<Integer>();

		setLayout(new BorderLayout());
		add("North", makeScorePanel()); // add at top
		add("South", makeTimePanel()); // add at bottom
		add("Center", makeSetPanell()); // add in remaining area (fills space)
	}

	private Component makeSetPanell() {
		c = new SetCanvas(this);
		c.addKeyListener(this);
		Panel setPanel = new Panel();
		setPanel.setBackground(Color.white);
		setPanel.setLayout(new BorderLayout());
		setPanel.add("Center", c);

		return setPanel;
	}

	private Component makeTimePanel() {
		Panel timePanel = new Panel();
		timePanel.setBackground(Color.black);

		return timePanel;
	}

	private Component makeScorePanel() {
		Panel scorePanel = new Panel();

		scorePanel.setForeground(Color.white);
		scorePanel.setBackground(c.dlave);
		scorePanel.setLayout(new GridBagLayout());
		GridBagConstraints d = new GridBagConstraints();

		d.fill = GridBagConstraints.HORIZONTAL;
		d.weightx = 0.5;
		d.gridwidth = 1;
		d.gridheight = 1;
		d.gridx = 0;
		d.gridy = 0;

		scorePanel.add(userScore, d);

		d.fill = GridBagConstraints.HORIZONTAL;
		d.weightx = 0.5;
		d.gridwidth = 1;
		d.gridheight = 1;
		d.gridx = 0;
		d.gridy = 1;

		scorePanel.add(compScore, d);

		d.fill = GridBagConstraints.HORIZONTAL;

		d.weightx = 0.5;
		d.gridwidth = 2;
		d.gridheight = 2;
		d.gridx = 1;
		d.gridy = 0;
		Panel titlePanel = new Panel();
		titlePanel.setFont(new Font("TimesRoman", Font.PLAIN, 31));

		titlePanel.add(new Label("Set", Label.CENTER));
		scorePanel.add(titlePanel, d);

		d.fill = GridBagConstraints.HORIZONTAL;
		d.weightx = 0.5;
		d.gridwidth = 1;
		d.gridheight = 1;
		d.gridx = 3;
		d.gridy = 1;
		d.anchor = GridBagConstraints.LAST_LINE_END;

		Label namesLabel = new Label("By Kai & Teal ", Label.RIGHT);

		scorePanel.add((namesLabel), d);
		return scorePanel;
	}

	public void changeScore(Label score, int change) {
		String[] sArray = score.getText().split(" ");
		int value = Integer.parseInt(sArray[1]) + change;
		score.setText(sArray[0] + " " + Integer.toString(value));
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		System.out.println(e.getKeyCode());
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_BACK_SPACE && userEntry.size() != 0) {
			System.out.println("You pressed delete!");
			userEntry.remove(userEntry.size() - 1);
		} else if (keyCode == KeyEvent.VK_ESCAPE) {
			if (!c.containsSet) {
				changeScore(userScore, 1);
				c.onDisplay.add(c.deckOrder.pop());
			} else {
				changeScore(userScore, -1);
			}
		} else if (keyCode == KeyEvent.VK_ENTER && userEntry.size() == 3) {
			System.out.println("You pressed enter!");
			Integer[] userArray = new Integer[3];
			userEntry.toArray(userArray);
			Arrays.sort(userArray);
			Vector<Integer[]> sets = c.findSets(c.onDisplay);
			boolean contained = false;
			for (int i = 0; i < sets.size(); i++) {
				if (Arrays.equals(userArray, sets.get(i)))
					contained = true;
			}
			if (contained) {
				System.out.println("You got a set!");
				changeScore(userScore, 3);
				c.onDisplay.remove(userEntry.get(0));
				c.onDisplay.remove(userEntry.get(1));
				c.onDisplay.remove(userEntry.get(2));
			} else {
				changeScore(userScore, -3);
			}
			userEntry.clear();
		} else if (65 <= keyCode && keyCode < c.onDisplay.size() + 65
				&& userEntry.contains(c.onDisplay.get(keyCode - 65))) {
			userEntry.remove(c.onDisplay.get(keyCode - 65));

		} else if (65 <= keyCode && keyCode < c.onDisplay.size() + 65
				&& userEntry.size() < 3) {

			userEntry.add(c.onDisplay.get(keyCode - 65));
			System.out.println("You entered a letter!");
		}
		c.repaint();
		c.containsSet = c.findSets(c.onDisplay).size() != 0;
	}

	public void keyReleased(KeyEvent e) {
	}

}

class SetCanvas extends Canvas {

	// instance variables
	Set parent;
	protected int[][] deckMaster;
	// 0,1,2,3 => color, filling, shape, number
	protected Stack<Integer> deckOrder;
	protected Vector<Integer> onDisplay;
	protected boolean containsSet;

	// colors
	static final Color dteal = new Color(0, 153, 153);
	static final Color lteal = new Color(153, 255, 255);
	static final Color dlave = new Color(153, 51, 255);
	static final Color llave = new Color(204, 153, 255);
	static final Color dsalm = new Color(255, 102, 102);
	static final Color lsalm = new Color(255, 179, 179);

	// constructors
	public SetCanvas(Set s) {
		parent = s;
		deckMaster = new int[81][4];
		deckOrder = new Stack<Integer>();
		onDisplay = new Vector<Integer>(26);

		// initialize deckMaster
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 3; k++) {
					for (int l = 0; l < 3; l++) {
						int index = l + 3 * k + 9 * j + 27 * i;
						deckMaster[index][0] = i;
						deckMaster[index][1] = j;
						deckMaster[index][2] = k;
						deckMaster[index][3] = l;
						// System.out.println(Arrays.toString(deckMaster[index]));
						// System.out.println(index);
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
			if (deckOrder.search(randint) < 0) {
				deckOrder.push(randint);
				counter++;
				// System.out.println(randint);
			}
		}

		// initialize onDisplay
		for (int i = 0; i < 12; i++)
			onDisplay.add(deckOrder.pop());
		// System.out.println(onDisplay);
		Vector<Integer[]> sets = findSets(onDisplay);
		containsSet = sets.size() != 0;
		System.out.println(sets.size());
		for (int k = 0; k < sets.size(); k++) {
			System.out.println(Arrays.toString(sets.get(k)));
		}

	}

	public Vector<Integer[]> findSets(Vector<Integer> displayed) {
		Vector<Integer[]> sets = new Vector<Integer[]>();
		for (int i = 0; i < displayed.size(); i++) {
			for (int j = i + 1; j < displayed.size(); j++) {
				int[] ideal = new int[4];
				for (int k = 0; k < 4; k++) {
					int val1 = deckMaster[displayed.get(i)][k];
					int val2 = deckMaster[displayed.get(j)][k];
					if (val1 == val2)
						ideal[k] = val1;
					else
						ideal[k] = 3 - (val1 + val2);
				}
				int index = ideal[3] + 3 * ideal[2] + 9 * ideal[1]
						+ 27 * ideal[0];
				if (displayed.contains(index)) {
					// System.out.println(Arrays.toString(deckMaster[displayed.get(i)]));
					// System.out.println(Arrays.toString(deckMaster[displayed.get(j)]));
					// System.out.println(Arrays.toString(deckMaster[index]));
					Integer[] potential = new Integer[] { displayed.get(i),
							displayed.get(j), index };
					Arrays.sort(potential);
					boolean contained = false;
					for (int l = 0; l < sets.size(); l++) {
						if (Arrays.equals(sets.get(l), potential)) {
							contained = true;
						}
					}
					if (!contained)
						sets.add(potential);
				}
			}
		}
		return sets;
	}

	public void paint(Graphics g) {
		for (int i = 0; i < onDisplay.size(); i++) {
			if (parent.userEntry.contains(onDisplay.get(i))) {
				drawCard(g, i, 1.1);
			} else {
				drawCard(g, i, .8);
			}
		}
	}

	public Color lighten(Color color) {
		if (color == dteal)
			return lteal;
		if (color == dlave)
			return llave;
		else
			return lsalm;
	}

	public Color getColor(int i) {
		return new Color[] { dteal, dlave, dsalm }[i];
	}

	public Color getFilling(Color color, int i) {
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
		Dimension d = getSize();
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(3));
		int[] features = deckMaster[onDisplay.get(index)];
		// height and width of cards
		int h = d.height / 3;
		int w = d.width / ((onDisplay.size() + 2) / 3);
		// height and width of shapes
		int sh = (int) (multiply * 3 * h / 5);
		int sw = (int) (multiply * 1 * w / 4);
		// x and y of cards
		int y = h / 2 + h * ((index) % 3);
		int x = w / 2 + w * ((index) / 3);
		// x and y of shape
		int sy;
		int sx = 0;
		sy = y - 1 * sh / 2;
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
			g.setColor(color);
			g.drawString(Character.toString((char) (index + 97)),
					x - 4 * w / 11, y - 4 * h / 11);
		}
		for (int j = 0; j <= features[3]; j++) {
			g.setColor(filling);
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
			sx += sw + 1 * sw / 4;
		}

	}

}