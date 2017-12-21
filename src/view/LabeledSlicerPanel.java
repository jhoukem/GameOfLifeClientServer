package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.util.Dictionary;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

/**
 * This class allow me to have a scalable way of creating slider.
 * 
 * @author Jean-Hugo
 *
 */
public class LabeledSlicerPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// The label that will be displayed on top of the sliders.
	private JLabel label;
	// used to store the sliders in line.
	private Panel slicerPanel;
	// The listener to add to every label created.s
	private ChangeListener sliderListener;

	public LabeledSlicerPanel(String label, ChangeListener changeListener) {
		this.label = new JLabel(label);
		this.sliderListener = changeListener;
		initGraphics();
	}


	private void initGraphics() {
		slicerPanel = new Panel();
		slicerPanel.setLayout(new FlowLayout());

		this.setLayout(new BorderLayout());
		this.add(label, BorderLayout.NORTH);
		this.add(slicerPanel, BorderLayout.CENTER);
	}


	public void addSlicer(int slicerMinValue, int slicerMaxValue){
		addSlicer(null, slicerMinValue, slicerMaxValue, null, 0, 0);
	}

	public void addSlicer(int slicerMinValue, int slicerMaxValue, Dictionary<Integer, JLabel> labelsTable,
			int majorSpacing, int minorSpacing){
		addSlicer(null, slicerMinValue, slicerMaxValue, labelsTable, majorSpacing, minorSpacing);
	}

	public void addSlicer(String label, int slicerMinValue, int slicerMaxValue, Dictionary<Integer, JLabel> labelTable,
			int majorSpacing, int minorSpacing){


		Panel panel = new Panel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		if(label != null){
			JLabel slicerLabel = new JLabel(label);
			panel.add(slicerLabel);
		}

		JSlider newSlider = new JSlider(slicerMinValue, slicerMaxValue);
		newSlider.setLabelTable(labelTable);
		newSlider.setPaintTicks(true);
		newSlider.setPaintLabels(true);
		newSlider.setMajorTickSpacing(majorSpacing);
		newSlider.setMinorTickSpacing(minorSpacing);

		if(sliderListener != null){
			newSlider.addChangeListener(sliderListener);
		}

		newSlider.setSize(200, 50);
		panel.add(newSlider);

		slicerPanel.add(panel);
	}

	public boolean isSliderAdjusting(int slicerIndex){
		return getSlider(slicerIndex).getValueIsAdjusting();
	}

	public void setSliderValue(int slicerIndex, int value){
		getSlider(slicerIndex).setValue(value);
	}

	public int getSliderValue(int slicerIndex){
		return getSlider(slicerIndex).getValue();
	}

	public JSlider getSlider(int slicerIndex){
		Panel panel = (Panel) slicerPanel.getComponent(slicerIndex);
		// If the panel 2 component the first one is the label so we take the second here.
		JSlider slider = (JSlider) panel.getComponent(panel.getComponentCount() > 1 ? 1 : 0);
		return slider;
	}

}
