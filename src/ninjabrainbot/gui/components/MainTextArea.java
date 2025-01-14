package ninjabrainbot.gui.components;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import ninjabrainbot.Main;
import ninjabrainbot.calculator.*;
import ninjabrainbot.gui.ColumnLayout;
import ninjabrainbot.gui.GUI;
import ninjabrainbot.gui.SizePreference;
import ninjabrainbot.gui.Theme;
import ninjabrainbot.io.NinjabrainBotPreferences;
import ninjabrainbot.util.I18n;
import ninjabrainbot.util.Pair;

public class MainTextArea extends JPanel {

	private static final long serialVersionUID = 5680882946230890993L;
	
	private final String BLIND = "BLIND", DIVINE = "DIVINE", TRIANGULATION = "TRI", TRIANGULATION_DETAILED = "DET";
	
	BasicTriangulationPanel basicTriangulation;
	DetailedTriangulationPanel detailedTriangulation;
	BlindPanel blind;
	DivinePanel divine;
	
	boolean idle;
	CardLayout layout;
	
	public MainTextArea(GUI gui) {
		layout = new CardLayout();
		idle = true;
		setLayout(layout);
		setAlignmentX(0);
		basicTriangulation = new BasicTriangulationPanel(gui);
		detailedTriangulation = new DetailedTriangulationPanel(gui);
		blind = new BlindPanel(gui);
		divine = new DivinePanel(gui);
		add(basicTriangulation, TRIANGULATION);
		add(detailedTriangulation, TRIANGULATION_DETAILED);
		add(blind, BLIND);
		add(divine, DIVINE);
		setOpaque(false);
		if (Main.preferences.view.get() == NinjabrainBotPreferences.BASIC) {
			layout.show(this, TRIANGULATION);
		} else {
			layout.show(this, TRIANGULATION_DETAILED);
		}
	}
	
	public void setResult(CalculatorResult result, GUI gui) {
		if (result == null && blind.isVisible()) {
			return;
		}
		if (Main.preferences.view.get() == NinjabrainBotPreferences.BASIC) {
			basicTriangulation.setResult(result);
			basicTriangulation.updateColors(gui);
			layout.show(this, TRIANGULATION);
		} else {
			if (result != null && !result.success()) {
				basicTriangulation.setResult(result);
				basicTriangulation.updateColors(gui);
				layout.show(this, TRIANGULATION);
			} else {
				detailedTriangulation.setResult(result);
				detailedTriangulation.updateColors(gui);
				layout.show(this, TRIANGULATION_DETAILED);
			}
		}
		idle = result == null;
	}
	
	public void setResult(BlindResult result, GUI gui) {
		blind.setResult(result);
		blind.updateColors(gui);
		layout.show(this, BLIND);
		idle = false;
	}
	
	public void setResult(DivineResult result, GUI gui) {
		divine.setResult(result);
		divine.updateColors(gui);
		layout.show(this, DIVINE);
		idle = false;
	}

	public void setNetherCoordsEnabled(boolean b) {
		basicTriangulation.netherLabel.setVisible(b);
	}

	public void setAngleUpdatesEnabled(boolean b) {
		basicTriangulation.setAngleUpdatesEnabled(b);
		detailedTriangulation.setAngleUpdatesEnabled(b);
	}

	public void updateColors(GUI gui) {
		basicTriangulation.updateColors(gui);
		detailedTriangulation.updateColors(gui);
		blind.updateColors(gui);
	}
	
	public void onReset() {
		if (Main.preferences.view.get() == NinjabrainBotPreferences.BASIC) {
			layout.show(this, TRIANGULATION);
		} else {
			layout.show(this, TRIANGULATION_DETAILED);
		}
	}
	
	@Override
	public Dimension getPreferredSize() {
		if (Main.preferences.view.get() == NinjabrainBotPreferences.BASIC) {
			return basicTriangulation.getPreferredSize();
		} else {
			return detailedTriangulation.getPreferredSize();
		}
	}

	public boolean isIdle() {
		return idle;
	}

}
class BasicTriangulationPanel extends ThemedPanel {

	private static final long serialVersionUID = 5784318732643211103L;

	public JLabel maintextLabel;
	public ColorMapLabel certaintyPanel;
	public JLabel netherLabel;
	public ColorMapLabel currentAngleLabel;

	public static final String CERTAINTY_TEXT = I18n.get("certainty");
	
	public BasicTriangulationPanel(GUI gui) {
		super(gui);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setAlignmentX(0);
		maintextLabel = new ThemedLabel(gui, I18n.get("waiting_f3c"));
		certaintyPanel = new ColorMapLabel(gui, true);
		netherLabel = new ThemedLabel(gui, "");
		netherLabel.setVisible(Main.preferences.showNetherCoords.get());
		currentAngleLabel = new ColorMapLabel(gui, true);
		maintextLabel.setAlignmentX(0);
		netherLabel.setAlignmentX(0);
		certaintyPanel.setAlignmentX(0);
		currentAngleLabel.setAlignmentX(0);
		add(maintextLabel);
		add(certaintyPanel);
		add(netherLabel);
		add(currentAngleLabel);
		setAngleUpdatesEnabled(Main.preferences.showAngleUpdates.get());
	}
	
	public void setResult(CalculatorResult result) {
		if (result != null) {
			if (result.success()) {
				ChunkPrediction prediction = result.getBestPrediction();
				maintextLabel.setText(prediction.format());
				certaintyPanel.setText(CERTAINTY_TEXT);
				certaintyPanel.setColoredText(String.format(Locale.US, "%.1f%%", prediction.weight*100.0), (float) prediction.weight);
				netherLabel.setText(I18n.get("nether_coordinates", prediction.x*2, prediction.z*2, prediction.getDistance()/8));
				currentAngleLabel.setText(prediction.formatTravelAngle(true));
				currentAngleLabel.setColoredText(prediction.formatTravelAngleDiff(), prediction.getTravelAngleDiffColor());
			} else {
				maintextLabel.setText(I18n.get("could_not_determine"));
				certaintyPanel.setText(I18n.get("you_probably_misread"));
				certaintyPanel.setColoredText("", 0);
				netherLabel.setText("");
				currentAngleLabel.setText("");
				currentAngleLabel.setColoredText("", 0);
			}
		} else {
			maintextLabel.setText(I18n.get("waiting_f3c"));
			certaintyPanel.setText("");
			certaintyPanel.setColoredText("", 0);
			netherLabel.setText("");
			currentAngleLabel.setText("");
			currentAngleLabel.setColoredText("", 0);
		}
	}

	public void setAngleUpdatesEnabled(boolean b) {
		currentAngleLabel.setVisible(b);
	}
	
	@Override
	public void updateColors(GUI gui) {
		super.updateColors(gui);
		certaintyPanel.updateColor(gui);
		currentAngleLabel.updateColor(gui);
	}
	
	@Override
	public Color getBackgroundColor(Theme theme) {
		return theme.COLOR_NEUTRAL;
	}
	
	@Override
	public void updateSize(GUI gui) {
		int numLabels = currentAngleLabel.isVisible() ? 4 : 3;
		setPreferredSize(new Dimension(0, numLabels * (gui.size.PADDING + gui.size.TEXT_SIZE_MEDIUM) + (numLabels - 1) * gui.size.PADDING_THIN));
		setBorder(new EmptyBorder(gui.size.PADDING_THIN, gui.size.PADDING, gui.size.PADDING_THIN, gui.size.PADDING));
		super.updateSize(gui);
	}
	
}
class DetailedTriangulationPanel extends ThemedPanel {

	private static final long serialVersionUID = -9022636765337872342L;
	
	private ChunkPanelHeader header;
	private List<ChunkPanel> panels;
	
	public DetailedTriangulationPanel(GUI gui) {
		super(gui);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setAlignmentX(0);
		header = new ChunkPanelHeader(gui);
		add(header);
		panels = new ArrayList<ChunkPanel>();
		for (int i = 0; i < SizePreference.NUM_DETAILED_PANELS; i++) {
			ChunkPanel panel = new ChunkPanel(gui);
			panels.add(panel);
			add(panel);
		}
	}
	
	public void setResult(CalculatorResult result) {
		header.updateHeaderText();
		if (result == null) {
			for (ChunkPanel p : panels) {
				p.setPrediciton(null);
			}
			return;
		}
		List<ChunkPrediction> predictions = result.getTopPredictions(SizePreference.NUM_DETAILED_PANELS);
		for (int i = 0; i < SizePreference.NUM_DETAILED_PANELS; i++) {
			ChunkPanel p = panels.get(i);
			p.setPrediciton(predictions.get(i));
		}
	}

	public void setAngleUpdatesEnabled(boolean b) {
		header.setAngleUpdatesEnabled(b);
		panels.forEach(p -> p.setAngleUpdatesEnabled(b));
	}
	
	@Override
	public void updateColors(GUI gui) {
		super.updateColors(gui);
		for (int i = 0; i < SizePreference.NUM_DETAILED_PANELS; i++) {
			ChunkPanel p = panels.get(i);
			p.updateColors(gui);
		}
	}
	
	@Override
	public Color getBackgroundColor(Theme theme) {
		return theme.COLOR_NEUTRAL;
	}
	
	@Override
	public void updateSize(GUI gui) {
		setPreferredSize(new Dimension(0, (1 + SizePreference.NUM_DETAILED_PANELS) * (gui.size.PADDING + gui.size.TEXT_SIZE_MEDIUM)));
		super.updateSize(gui);
	}
	
}
class BlindPanel extends ThemedPanel {

	private static final long serialVersionUID = 5784318732643211103L;

	public ColorMapLabel evalLabel;
	public ColorMapLabel certaintyPanel;
	public JLabel distanceLabel;
	
	public BlindPanel(GUI gui) {
		super(gui);
		setLayout(new GridBagLayout());
		setAlignmentX(0);
		evalLabel = new ColorMapLabel(gui, true);
		distanceLabel = new ThemedLabel(gui, "");
		certaintyPanel = new ColorMapLabel(gui, false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.weightx = 1;
		add(evalLabel, gbc);
		add(certaintyPanel, gbc);
		add(distanceLabel, gbc);
		gbc.weighty = 1;
		add(Box.createGlue(), gbc);
	}
	
	public void setResult(BlindResult result) {
		Pair<Float, String> eval = result.evaluation();
		evalLabel.setText(I18n.get("blind_coords", result.x, result.z));
		evalLabel.setColoredText(I18n.get(eval.snd), eval.fst);
		certaintyPanel.setText(I18n.get("chance_of", (int) result.highrollThreshold));
		certaintyPanel.setColoredText(String.format(Locale.US, "%.1f%% ", result.highrollProbability*100), (float) (result.highrollProbability / result.optHighrollProb));
//		distanceLabel.setText(I18n.get("average_distance_to", result.avgDistance));
		if (eval != BlindResult.EXCELLENT) {
			distanceLabel.setText(I18n.get("blind_direction", result.improveDirection * 180.0 / Math.PI, result.improveDistance));
		} else {
			distanceLabel.setText("");
		}
	}
	
	@Override
	public void updateColors(GUI gui) {
		super.updateColors(gui);
		certaintyPanel.updateColor(gui);
		evalLabel.updateColor(gui);
	}
	
	@Override
	public Color getBackgroundColor(Theme theme) {
		return theme.COLOR_NEUTRAL;
	}
	
	@Override
	public void updateSize(GUI gui) {
		setPreferredSize(new Dimension(0, 3 * (gui.size.PADDING + gui.size.TEXT_SIZE_MEDIUM) + 2 * gui.size.PADDING_THIN));
		setBorder(new EmptyBorder(gui.size.PADDING_THIN, gui.size.PADDING, gui.size.PADDING_THIN, gui.size.PADDING));
		super.updateSize(gui);
	}
	
}
class DivinePanel extends ThemedPanel {

	private static final long serialVersionUID = 8846911396318732368L;
	
	private JPanel panels[];
	public JLabel fossilLabel;
	public JLabel safeLabels[];
	public JLabel highrollLabels[];
	
	public DivinePanel(GUI gui) {
		super(gui);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setAlignmentX(0);
		int n = Ring.get(0).numStrongholds;
		JPanel panel0 = new ThemedPanel(gui);
		ColumnLayout layout0 = new ColumnLayout(0);
		panels = new JPanel[3];
		panel0.setLayout(layout0);
		panel0.setOpaque(false);
		panels[0] = panel0;
		fossilLabel = new ThemedLabel(gui, "");
		fossilLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		layout0.setRelativeWidth(fossilLabel, 0.9f);
		panel0.add(fossilLabel, 0);
		for (int i = 0; i < n; i++) {
			panel0.add(new ThemedLabel(gui, "s" + (i + 1), true, true));
		}
		JPanel panel1 = new ThemedPanel(gui);
		ColumnLayout layout1 = new ColumnLayout(0);
		panel1.setLayout(layout1);
		panel1.setOpaque(false);
		panels[1] = panel1;
		JLabel safeLabel = new ThemedLabel(gui, I18n.get("divine_safe"));
		safeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		layout1.setRelativeWidth(safeLabel, 0.9f);
		panel1.add(safeLabel);
		safeLabels = new ThemedLabel[n];
		for (int i = 0; i < n; i++) {
			JLabel s = new ThemedLabel(gui, "", false, true);
			safeLabels[i] = s;
			panel1.add(s);
		}
		JPanel panel2 = new ThemedPanel(gui);
		ColumnLayout layout2 = new ColumnLayout(0);
		panel2.setLayout(layout2);
		panel2.setOpaque(false);
		panels[2] = panel2;
		JLabel highrollLabel = new ThemedLabel(gui, I18n.get("divine_highroll"));
		highrollLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		layout2.setRelativeWidth(highrollLabel, 0.9f);
		panel2.add(highrollLabel);
		highrollLabels = new ThemedLabel[n];
		for (int i = 0; i < n; i++) {
			JLabel h = new ThemedLabel(gui, "", false, true);
			highrollLabels[i] = h;
			panel2.add(h);
		}
		add(panel0);
		add(panel1);
		add(panel2);
		add(Box.createGlue());
	}
	
	public void setResult(DivineResult result) {
		fossilLabel.setText(I18n.get("fossil_number", result.fossil.x));
		for (int i = 0; i < Ring.get(0).numStrongholds; i++) {
			safeLabels[i].setText(result.safe[i].toString());
			highrollLabels[i].setText(result.highroll[i].toString());
		}
	}
	
	@Override
	public Color getBackgroundColor(Theme theme) {
		return theme.COLOR_NEUTRAL;
	}
	
	@Override
	public void updateSize(GUI gui) {
		setPreferredSize(new Dimension(0, 3 * (gui.size.PADDING + gui.size.TEXT_SIZE_MEDIUM) + 2 * gui.size.PADDING_THIN));
		setBorder(new EmptyBorder(gui.size.PADDING_THIN, gui.size.PADDING, gui.size.PADDING_THIN, gui.size.PADDING));
		for (JPanel p : panels) {
			p.setMaximumSize(new Dimension(1000, gui.size.TEXT_SIZE_MEDIUM));
		}
		super.updateSize(gui);
	}
	
}
class ColorMapLabel extends JPanel implements ILabel {
	
	private static final long serialVersionUID = 8926205242557099213L;
	
	public JLabel textLabel;
	public JLabel coloredLabel;

	private double lastColor = 0.0;

	public ColorMapLabel(GUI gui, boolean textFirst) {
		this(gui, textFirst, false);
	}

	public ColorMapLabel(GUI gui, boolean textFirst, boolean centered) {
		textLabel = new ThemedLabel(gui, "");
		coloredLabel = new ThemedLabel(gui, "") {
			private static final long serialVersionUID = -6995689057641195351L;
			@Override
			public Color getForegroundColor(Theme theme) {
				return theme.CERTAINTY_COLOR_MAP.get(lastColor);
			}
		};
		setLayout(new FlowLayout(centered ? FlowLayout.CENTER : FlowLayout.LEFT, 0, 0));
		setOpaque(false);
		if (textFirst) {
			add(textLabel);
			add(coloredLabel);
		} else {
			add(coloredLabel);
			add(textLabel);
		}
		setAlignmentX(0);
	}
	
	void setColoredText(String text, float color) {
		lastColor = color;
		coloredLabel.setText(text);
	}
	
	public void setText(String text) {
		textLabel.setText(text);
	}
	
	void updateColor(GUI gui) {
		coloredLabel.setForeground(gui.theme.CERTAINTY_COLOR_MAP.get(lastColor));
	}
	
}
