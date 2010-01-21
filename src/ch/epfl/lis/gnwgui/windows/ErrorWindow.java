package ch.epfl.lis.gnwgui.windows;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;

import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnwgui.GnwGuiSettings;

public class ErrorWindow extends GenericWindow {
	
	protected JButton cancelButton;
	protected JPanel actionPanel;
	protected JPanel centerPanel_;

	public ErrorWindow(Frame aFrame, boolean modal, String errorShortDescription) {
		super(aFrame, modal);
		setDescription(errorShortDescription);
		setTitle("ERROR");
		setTitle2("Error");
		
		setSize(580, 180);
		setLocationRelativeTo(GnwGuiSettings.getInstance().getGnwGui().getFrame());
		setResizable(false);
		
		centerPanel_ = new JPanel();
		centerPanel_.setBackground(Color.WHITE);
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {7,0,7};
		gridBagLayout.rowHeights = new int[] {7,7,7,0,0,0,7,7};
		centerPanel_.setLayout(gridBagLayout);
		getContentPane().add(centerPanel_);
		
		actionPanel = new JPanel();
		actionPanel.setBackground(Color.WHITE);
		final GridBagConstraints gridBagConstraints_9 = new GridBagConstraints();
		gridBagConstraints_9.gridy = 3;
		gridBagConstraints_9.gridx = 1;
		centerPanel_.add(actionPanel, gridBagConstraints_9);
		
		cancelButton = new JButton();
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.setBackground(UIManager.getColor("Button.background"));
		cancelButton.setFont(new Font("Sans", Font.PLAIN, 12));
		cancelButton.setText("Cancel");
		actionPanel.add(cancelButton);
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				GnwSettings.getInstance().stopBenchmarkGeneration(true);
				escapeAction();
			}
		});
		
		
	}
	
	/**
	 * 
	 * @param errorShortDescription - A very brief description of the error (less than 65 Characters long). 
	 */
	public void setDescription(String errorShortDescription){
		if(errorShortDescription.length() > 66)
			throw new RuntimeException("The string provided to setDescription() in ErrorWindow is longer than 65 characters.");
		setGeneralInformation(errorShortDescription);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 9127589973518920105L;

}
