/*
Copyright (c) 2008-2010 Daniel Marbach & Thomas Schaffter

We release this software open source under an MIT license (see below). If this
software was useful for your scientific work, please cite our paper(s) listed
on http://gnw.sourceforge.net.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package ch.epfl.lis.gnwgui.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.epfl.lis.gnwgui.windows.GenericWindow;

/** This dialog is used to rename a network.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class RenameWindow extends GenericWindow {

	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** Cancel button */
	protected JButton cancelButton;
	/** Apply button */
	protected JButton applyButton;
	
	/** This text field must contain the new name of the network. */
	protected JTextField newName_;
	
    /** Logger for this class */
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(RenameWindow.class.getName());

	
	// ============================================================================
	// PUBLIC METHODS
	
    /**
     * Constructor
     * @param aFrame
     */
	public RenameWindow(Frame aFrame) {
		
		super(aFrame, true);
		setTitle2("Enter a new name");
		setTitle("Rename");
		setBounds(100, 100, 416, 275);

		final JPanel centerPanel = new JPanel();
		centerPanel.setBackground(Color.WHITE);
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {0,7,7};
		centerPanel.setLayout(gridBagLayout);
		getContentPane().add(centerPanel, BorderLayout.CENTER);

		newName_ = new JTextField();
		newName_.setRequestFocusEnabled(false);
		newName_.setColumns(26);
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 0;
		centerPanel.add(newName_, gridBagConstraints);

		setLocationRelativeTo(aFrame);

		final Component component = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.gridy = 1;
		gridBagConstraints_2.gridx = 0;
		centerPanel.add(component, gridBagConstraints_2);

		final JPanel actionPanel = new JPanel();
		actionPanel.setBackground(Color.WHITE);
		final FlowLayout flowLayout_1 = new FlowLayout();
		actionPanel.setLayout(flowLayout_1);
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.gridy = 2;
		gridBagConstraints_1.gridx = 0;
		centerPanel.add(actionPanel, gridBagConstraints_1);

		applyButton = new JButton();
		applyButton.setMnemonic(KeyEvent.VK_A);
		applyButton.setSelected(true);
		applyButton.setFont(new Font("Sans", Font.PLAIN, 12));
		applyButton.setText("Apply");
		actionPanel.add(applyButton);

		final Component component_1 = Box.createHorizontalStrut(2);
		actionPanel.add(component_1);

		cancelButton = new JButton();
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.setFont(new Font("Sans", Font.PLAIN, 12));
		cancelButton.setText("Cancel");
		actionPanel.add(cancelButton);
		
		
//		GnwGuiSettings settings = GnwGuiSettings.getInstance();
//		applyButton.setIcon(new ImageIcon(settings.getApplyIconPath()));
//		cancelButton.setIcon(new ImageIcon(settings.getCancelIconPath()));
	}
	
	
	// ============================================================================
	// GETTERS AND SETTERS

	public void setNewName(String text) { newName_.setText(text); }
	public String getNewName() { return newName_.getText(); }
}
