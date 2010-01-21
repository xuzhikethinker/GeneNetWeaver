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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import ch.epfl.lis.gnwgui.GnwGuiSettings;
import ch.epfl.lis.gnwgui.HyperText;
import ch.epfl.lis.gnwgui.NetworkManager;


/** Window for ch.epfl.lis.gnwgui.GnwGui
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class GnwGuiWindow {

	/** Header of the application */
	protected Header header_ = new Header();
	/** Defines the header of the main frame of the application. */
	protected JPanel headerPanel_;
	
	/** Panel presents in the main part of the application. */
	protected JPanel displayPanel_;
	/** Layout of displayPanel_ */
	protected CardLayout mainDisplayLayout_;
	/** About panel */
	protected JPanel aboutPanel_;
	/** Network manager panel */
	protected JPanel networkPanel_;
	/** Settings panel */
	protected JPanel settingsPanel_;
	/** Tutorial panel */
	protected JPanel tutorialPanel_;
	/** Help panel */
	protected JPanel helpPanel_;
	
	/** MENU: Group the section buttons */
	protected ButtonGroup sectionButtonGroup_ = new ButtonGroup();
	/** MENU: About section */
	protected JToggleButton about_;
	/** MENU: Network Manager */
	protected JToggleButton networkManager_;
	/** MENU: Settings */
	protected JToggleButton settings_;
	/** MENU: Help section button */
	protected JToggleButton tutorial_;
	/** MENU: Documentation button */
	protected JToggleButton help_;
	/** MENU: Open the console */
	protected JButton consoleToggleButton;
	/** MENU: Exit button */
	protected JButton exit_;
	
	/** Apply the new settings */
	protected JButton applySettingsButton_;
	/** Reload the last settings file opened  */
	protected JButton reloadSettingsButton_;
	/** Load a settings file */
	protected JButton openSettingsButton_;
	/** Export (Save As) the current settings to a file */
	protected JButton exportSettingsButton_;
	
	/** iDesktop of the Network Manager */
	protected NetworkManager networkDesktop_ = null;
	
	/** Frame of the application. */
	protected JFrame frame;
	
	/** HyperText link to Thomas e-mail */
	protected HyperText thomasEmail_;
	/** HyperText link to Daniel e-mail */
	protected HyperText danielEmail_;
	
	/** Settings file content */
	protected JTextPane settingsTextPane_;
	
    /** Logger for this class */
	private static Logger log = Logger.getLogger(GnwGuiWindow.class.getName());

    
	// ============================================================================
	// PUBLIC METHODS
    
	/**
	 * Create the application
	 */
	public GnwGuiWindow() {
		initialize();
	}
	
	
	// ============================================================================
	// PRIVATE METHODS

	/**
	 * Initialize the contents of the frame
	 */
	@SuppressWarnings("serial")
	private void initialize() {
		
		GnwGuiSettings settings = GnwGuiSettings.getInstance();
		
		frame = new JFrame();
		frame.getContentPane().setFocusCycleRoot(true);
		frame.setSize(new Dimension(0, 0));
		frame.getContentPane().setBackground(Color.WHITE);
		frame.setTitle("GeneNetWeaver");
		frame.setForeground(Color.BLACK);
		frame.setFont(new Font("Sans", Font.PLAIN, 11));
		frame.setBounds(100, 100, 870, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);		
		frame.setLocationRelativeTo(null);
		// Don't set a custom icon for the application, reason: don't affect "open" and "save as" dialog... :-(
		// I don't want a mix
		try {
			frame.setIconImage(new ImageIcon(GnwGuiSettings.getInstance().getGnwIcon()).getImage());
		} catch (NoSuchMethodError e) {
			// setIconImage() doesn't exist in Mac OS Java implementation.
		}

		headerPanel_ = new JPanel();
		final CardLayout cards_ = new CardLayout();
		headerPanel_.setLayout(cards_);
		frame.getContentPane().add(headerPanel_, BorderLayout.NORTH);
		
		header_.getTitle().setFont(new Font("Sans", Font.BOLD, 30));
		header_.setPreferredSize(new Dimension(0, 80));
		headerPanel_.add(header_, "header");

		final JPanel controlPanel = new JPanel();
		controlPanel.setFocusCycleRoot(true);
		final FlowLayout flowLayout = new FlowLayout();
		flowLayout.setVgap(10);
		controlPanel.setLayout(flowLayout);
		controlPanel.setSize(100, 496);
		controlPanel.setFont(new Font("Sans", Font.PLAIN, 11));
		controlPanel.setBackground(Color.WHITE);
		controlPanel.setPreferredSize(new Dimension(100, 0));
		frame.getContentPane().add(controlPanel, BorderLayout.WEST);

		final Component component_1 = Box.createRigidArea(new Dimension(50, 10));
		controlPanel.add(component_1);
		about_ = new JToggleButton(new ImageIcon(settings.getMenuAboutImage()));
		about_.setVerticalTextPosition(AbstractButton.BOTTOM);
		about_.setHorizontalTextPosition(AbstractButton.CENTER);
		about_.setFont(new Font("Sans", Font.PLAIN, 11));
		about_.setPreferredSize(new Dimension(65, 60));
		about_.setText("About");
		controlPanel.add(about_);
		sectionButtonGroup_.add(about_);

		networkManager_ = new JToggleButton(new ImageIcon(settings.getMenuNetworkImage()));
		networkManager_.setFocusTraversalPolicyProvider(true);
		networkManager_.setFont(new Font("Sans", Font.PLAIN, 11));
		networkManager_.setPreferredSize(new Dimension(65, 60));
		networkManager_.setVerticalTextPosition(AbstractButton.BOTTOM);
		networkManager_.setHorizontalTextPosition(AbstractButton.CENTER);
		networkManager_.setText("Networks");
		controlPanel.add(networkManager_);
		sectionButtonGroup_.add(networkManager_);

		settings_ = new JToggleButton(new ImageIcon(settings.getMenuSettingsImage()));
		settings_.setFocusTraversalPolicyProvider(true);
		settings_.setFont(new Font("Sans", Font.PLAIN, 11));
		settings_.setPreferredSize(new Dimension(65, 60));
		settings_.setVerticalTextPosition(AbstractButton.BOTTOM);
		settings_.setHorizontalTextPosition(AbstractButton.CENTER);
		settings_.setText("Settings");
		controlPanel.add(settings_);
		sectionButtonGroup_.add(settings_);

		tutorial_ = new JToggleButton(new ImageIcon(settings.getMenuTutorialImage()));
		tutorial_.setFont(new Font("Sans", Font.PLAIN, 11));
		tutorial_.setPreferredSize(new Dimension(65, 60));
		tutorial_.setVerticalTextPosition(AbstractButton.BOTTOM);
		tutorial_.setHorizontalTextPosition(AbstractButton.CENTER);
		tutorial_.setText("Tutorial");
		controlPanel.add(tutorial_);
		sectionButtonGroup_.add(tutorial_);

		help_ = new JToggleButton(new ImageIcon(settings.getMenuHelpImage()));
		help_.setPreferredSize(new Dimension(65, 60));
		help_.setFont(new Font("Dialog", Font.PLAIN, 11));
		help_.setVerticalTextPosition(AbstractButton.BOTTOM);
		help_.setHorizontalTextPosition(AbstractButton.CENTER);
		help_.setText("Help");
		controlPanel.add(help_);
		sectionButtonGroup_.add(help_);
		
		consoleToggleButton = new JButton(new ImageIcon(settings.getMenuConsoleImage()));
		consoleToggleButton.setFont(new Font("Sans", Font.PLAIN, 11));
		consoleToggleButton.setPreferredSize(new Dimension(65, 60));
		consoleToggleButton.setText("Console");
		consoleToggleButton.setVerticalTextPosition(AbstractButton.BOTTOM);
		consoleToggleButton.setHorizontalTextPosition(AbstractButton.CENTER);
		controlPanel.add(consoleToggleButton);
		
		exit_ = new JButton(new ImageIcon(settings.getMenuExitImage()));
		exit_.setPreferredSize(new Dimension(65, 60));
		exit_.setFont(new Font("Dialog", Font.PLAIN, 11));
		exit_.setText("Exit");
		exit_.setVerticalTextPosition(AbstractButton.BOTTOM);
		exit_.setHorizontalTextPosition(AbstractButton.CENTER);
		controlPanel.add(exit_);

		networkDesktop_ = new NetworkManager("networkDesktop");
		settings.setNetworkDesktop(networkDesktop_);

		displayPanel_ = new JPanel();
		mainDisplayLayout_ = new CardLayout();
		displayPanel_.setLayout(mainDisplayLayout_);
		displayPanel_.setBackground(Color.WHITE);
		frame.getContentPane().add(displayPanel_, BorderLayout.CENTER);

		aboutPanel_ = new JPanel();
		aboutPanel_.setBackground(Color.WHITE);
		aboutPanel_.setLayout(new BorderLayout());
		aboutPanel_.setName("panel1");
		displayPanel_.add(aboutPanel_, aboutPanel_.getName());

		final JPanel aboutContentPanel = new JPanel();
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {7,7,7,7,7,7,7,7,7,7,7,7,7,7,7};
		aboutContentPanel.setLayout(gridBagLayout);
		aboutContentPanel.setBackground(Color.WHITE);
		aboutPanel_.add(aboutContentPanel, BorderLayout.CENTER);

		final Component component_7 = Box.createVerticalStrut(25);
		final GridBagConstraints gridBagConstraints_13 = new GridBagConstraints();
		gridBagConstraints_13.gridx = 0;
		gridBagConstraints_13.gridy = 0;
		aboutContentPanel.add(component_7, gridBagConstraints_13);

		final JLabel logo = new JLabel(new ImageIcon(GnwGuiSettings.getInstance().getAboutImagePath()));
		final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.gridy = 1;
		gridBagConstraints_5.gridx = 0;
		aboutContentPanel.add(logo, gridBagConstraints_5);

		final JLabel thomasSchaffterLabel = new JLabel();
		thomasSchaffterLabel.setFont(new Font("Sans", Font.BOLD, 12));
//		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridy = 3;
//		gridBagConstraints.gridx = 0;
//		aboutContentPanel.add(thomasSchaffterLabel, gridBagConstraints);
//		thomasSchaffterLabel.setText("<html><center>Laboratory of Intelligent Systems</center></html>");

//		final HyperText lisWebPage = new HyperText("http://lis.epfl.ch") {
//			@Override
//			public void action() {
//				openDefaultBrowserClient("http://lis.epfl.ch");
//			}
//		};
//		final GridBagConstraints gridBagConstraints_10 = new GridBagConstraints();
//		gridBagConstraints_10.gridy = 4;
//		gridBagConstraints_10.gridx = 0;
//		aboutContentPanel.add(lisWebPage, gridBagConstraints_10);

		final JLabel programmersLabel = new JLabel();
		programmersLabel.setFont(new Font("Sans", Font.BOLD, 12));
		programmersLabel.setText("Developers");
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.gridy = 4;
		gridBagConstraints_3.gridx = 0;
		aboutContentPanel.add(programmersLabel, gridBagConstraints_3);

		final Component component = Box.createVerticalStrut(10);
		final GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
		gridBagConstraints_6.gridy = 5;
		gridBagConstraints_6.gridx = 0;
		aboutContentPanel.add(component, gridBagConstraints_6);

		final JLabel thomasName = new JLabel();
		thomasName.setText("Thomas Schaffter (1)");
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.gridy = 6;
		gridBagConstraints_2.gridx = 0;
		aboutContentPanel.add(thomasName, gridBagConstraints_2);
		
		thomasEmail_ = new HyperText("thomas.schaffter@gmail.com") {
			@Override
			public void action() {
				openDefaultMailClient("thomas.schaffter@gmail.com");
			}
		};
		final GridBagConstraints gridBagConstraints_7 = new GridBagConstraints();
		gridBagConstraints_7.gridy = 7;
		gridBagConstraints_7.gridx = 0;
		aboutContentPanel.add(thomasEmail_, gridBagConstraints_7);

		final Component component_3 = Box.createVerticalStrut(5);
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.gridy = 8;
		gridBagConstraints_1.gridx = 0;
		aboutContentPanel.add(component_3, gridBagConstraints_1);

		final JPanel bottomPanel = new JPanel();
		bottomPanel.setBackground(Color.WHITE);
		bottomPanel.setLayout(new BorderLayout());
		final GridBagConstraints gridBagConstraints_14 = new GridBagConstraints();
		gridBagConstraints_14.gridy = 9;
		gridBagConstraints_14.gridx = 0;
		aboutContentPanel.add(bottomPanel, gridBagConstraints_14);

		final JPanel textLabPanel = new JPanel();
		textLabPanel.setBackground(Color.WHITE);
		final GridBagLayout gridBagLayout_1 = new GridBagLayout();
		gridBagLayout_1.columnWidths = new int[] {7,0,7};
		gridBagLayout_1.rowHeights = new int[] {7,7,7,7};
		textLabPanel.setLayout(gridBagLayout_1);
		bottomPanel.add(textLabPanel, BorderLayout.CENTER);

		final JLabel danielMarbachLabel = new JLabel();
		final GridBagConstraints gridBagConstraints_8 = new GridBagConstraints();
		gridBagConstraints_8.gridy = 0;
		gridBagConstraints_8.gridx = 1;
		textLabPanel.add(danielMarbachLabel, gridBagConstraints_8);
		danielMarbachLabel.setText("Daniel Marbach (1,2)");

		danielEmail_ = new HyperText("daniel.marbach@gmail.com") {
			@Override
			public void action() {
				openDefaultMailClient("daniel.marbach@gmail.com");
			}
		};
		final GridBagConstraints gridBagConstraints_9 = new GridBagConstraints();
		gridBagConstraints_9.gridy = 1;
		gridBagConstraints_9.gridx = 1;
		textLabPanel.add(danielEmail_, gridBagConstraints_9);

		final Component component_12 = Box.createVerticalStrut(20);
		final GridBagConstraints gridBagConstraints_21 = new GridBagConstraints();
		gridBagConstraints_21.gridy = 2;
		gridBagConstraints_21.gridx = 1;
		textLabPanel.add(component_12, gridBagConstraints_21);

		
		final HyperText lisWebPage = new HyperText("Laboratory of Intelligent Systems", "(1) ") {
			@Override
			public void action() {
				openDefaultBrowserClient("http://lis.epfl.ch");
			}
		};
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridx = 1;
		textLabPanel.add(lisWebPage, gridBagConstraints);

		final Component component_10 = Box.createHorizontalStrut(20);
		final GridBagConstraints gridBagConstraints_19 = new GridBagConstraints();
		gridBagConstraints_19.gridy = 4;
		gridBagConstraints_19.gridx = 0;
		textLabPanel.add(component_10, gridBagConstraints_19);

		final JLabel swissFederalInstituteLabel = new JLabel();
		final GridBagConstraints gridBagConstraints_11 = new GridBagConstraints();
		gridBagConstraints_11.gridx = 1;
		gridBagConstraints_11.gridy = 4;
		gridBagConstraints_11.insets = new Insets(0, 0, 15, 0);
		textLabPanel.add(swissFederalInstituteLabel, gridBagConstraints_11);
		swissFederalInstituteLabel.setText("<html><center>Swiss Federal Institute of Technology in Lausanne (EPFL)<br>1015 Lausanne, Switzerland</center></html>");

		final HyperText webpage2 = new HyperText("MIT Computational Biology Group", "(2) ") {
			@Override
			public void action() {
				openDefaultBrowserClient("http://compbio.mit.edu");
			}
		};
		final GridBagConstraints gridBagConstraints100 = new GridBagConstraints();
		gridBagConstraints100.gridy = 5;
		gridBagConstraints100.gridx = 1;
		textLabPanel.add(webpage2, gridBagConstraints100);

		final JLabel swissFederalInstituteLabel_1 = new JLabel();
		swissFederalInstituteLabel_1.setText("<html><center>Massachusetts Institute of Technology<br>Cambridge, MA 02138, USA</center></html>");
		final GridBagConstraints gridBagConstraints_10 = new GridBagConstraints();
		gridBagConstraints_10.gridy = 6;
		gridBagConstraints_10.gridx = 1;
		textLabPanel.add(swissFederalInstituteLabel_1, gridBagConstraints_10);
		


		final Component component_11 = Box.createHorizontalStrut(20);
		final GridBagConstraints gridBagConstraints_20 = new GridBagConstraints();
		gridBagConstraints_20.gridy = 4;
		gridBagConstraints_20.gridx = 2;
		textLabPanel.add(component_11, gridBagConstraints_20);

		final JPanel epflLogoPanel = new JPanel();
		epflLogoPanel.setBackground(Color.WHITE);
		final GridBagLayout gridBagLayout_2 = new GridBagLayout();
		gridBagLayout_2.rowHeights = new int[] {7,7};
		epflLogoPanel.setLayout(gridBagLayout_2);
		bottomPanel.add(epflLogoPanel, BorderLayout.WEST);

		final Component component_8 = Box.createHorizontalStrut(150);
		final GridBagConstraints gridBagConstraints_16 = new GridBagConstraints();
		gridBagConstraints_16.gridx = 0;
		gridBagConstraints_16.gridy = 0;
		epflLogoPanel.add(component_8, gridBagConstraints_16);

		final JLabel epflLogo = new JLabel(new ImageIcon(GnwGuiSettings.getInstance().getEpflImage()));
		final GridBagConstraints gridBagConstraints_15 = new GridBagConstraints();
		gridBagConstraints_15.gridy = 1;
		gridBagConstraints_15.gridx = 0;
		epflLogoPanel.add(epflLogo, gridBagConstraints_15);
		epflLogo.setBackground(Color.WHITE);

		final Component component_2 = Box.createVerticalStrut(40);
		epflLogoPanel.add(component_2, new GridBagConstraints());

		final JPanel lisLogoPanel = new JPanel();
		lisLogoPanel.setBackground(Color.WHITE);
		final GridBagLayout gridBagLayout_3 = new GridBagLayout();
		gridBagLayout_3.rowHeights = new int[] {7};
		lisLogoPanel.setLayout(gridBagLayout_3);
		bottomPanel.add(lisLogoPanel, BorderLayout.EAST);

		final Component component_4 = Box.createVerticalStrut(40);
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.gridx = 0;
		gridBagConstraints_4.gridy = 0;
		lisLogoPanel.add(component_4, gridBagConstraints_4);

		final Component component_9 = Box.createHorizontalStrut(150);
		final GridBagConstraints gridBagConstraints_17 = new GridBagConstraints();
		gridBagConstraints_17.gridx = 0;
		gridBagConstraints_17.gridy = 1;
		lisLogoPanel.add(component_9, gridBagConstraints_17);

		//final JLabel label = new JLabel(new ImageIcon(GnwGuiSettings.getInstance().getLisImage()));
		final JLabel label = new JLabel(new ImageIcon(GnwGuiSettings.getInstance().getMitImage()));
		final GridBagConstraints gridBagConstraints_18 = new GridBagConstraints();
		gridBagConstraints_18.gridy = 2;
		lisLogoPanel.add(label, gridBagConstraints_18);

		networkPanel_ = new JPanel();
		networkPanel_.setRequestFocusEnabled(false);
		networkPanel_.setPreferredSize(new Dimension(0, 0));
		networkPanel_.setBackground(Color.WHITE);
		networkPanel_.setLayout(new BorderLayout());
		networkPanel_.setName("panel");
		displayPanel_.add(networkPanel_, networkPanel_.getName());
		networkPanel_.add(networkDesktop_.getDesktopPane(), BorderLayout.CENTER);
		
		settingsPanel_ = new JPanel();
		settingsPanel_.setBackground(Color.WHITE);
		settingsPanel_.setLayout(new BorderLayout());
		settingsPanel_.setName("panel_1");
		displayPanel_.add(settingsPanel_, settingsPanel_.getName());

		final JPanel settingsBPanel = new JPanel();
		settingsBPanel.setLayout(new BorderLayout());
		settingsBPanel.setBackground(Color.WHITE);
		settingsPanel_.add(settingsBPanel, BorderLayout.SOUTH);

		final JPanel panel_2 = new JPanel();
		panel_2.setBackground(Color.WHITE);
		final FlowLayout flowLayout_1 = new FlowLayout();
		flowLayout_1.setVgap(10);
		flowLayout_1.setAlignment(FlowLayout.RIGHT);
		panel_2.setLayout(flowLayout_1);
		settingsBPanel.add(panel_2);

		applySettingsButton_ = new JButton();
		applySettingsButton_.setText("Apply");
		panel_2.add(applySettingsButton_);

		reloadSettingsButton_ = new JButton();
		reloadSettingsButton_.setText("Reload");
		panel_2.add(reloadSettingsButton_);

		final Component component_13_1 = Box.createHorizontalStrut(10);
		panel_2.add(component_13_1);

		openSettingsButton_ = new JButton();
		openSettingsButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
			}
		});
		openSettingsButton_.setText("Open");
		panel_2.add(openSettingsButton_);

		exportSettingsButton_ = new JButton();
		exportSettingsButton_.setText("Export");
		panel_2.add(exportSettingsButton_);

		final JPanel panel_3 = new JPanel();
		final FlowLayout flowLayout_2 = new FlowLayout();
		flowLayout_2.setVgap(8);
		flowLayout_2.setHgap(0);
		panel_3.setLayout(flowLayout_2);
		panel_3.setBackground(Color.WHITE);
		settingsBPanel.add(panel_3, BorderLayout.WEST);

		final JLabel settingsApplyNote = new JLabel();
		settingsApplyNote.setFont(new Font("Sans", Font.PLAIN, 12));
		settingsApplyNote.setBackground(Color.WHITE);
		settingsApplyNote.setText("<html>Press on <b>Apply</b> to reassign all the settings.<br>Press on <b>Reload</b> to load the last settings file opened.</html>");
		panel_3.add(settingsApplyNote);

		final Component component_15 = Box.createVerticalStrut(5);
		settingsBPanel.add(component_15, BorderLayout.NORTH);

		final JScrollPane settingsScrollPane = new JScrollPane();
		settingsPanel_.add(settingsScrollPane, BorderLayout.CENTER);

		settingsTextPane_ = new JTextPane();
		settingsScrollPane.setViewportView(settingsTextPane_);

		final Component component_14 = Box.createVerticalStrut(29);
		settingsPanel_.add(component_14, BorderLayout.NORTH);
		displayPanel_.setBackground(Color.WHITE);

		tutorialPanel_ = new JPanel();
		tutorialPanel_.setBackground(Color.WHITE);
		tutorialPanel_.setLayout(new BorderLayout());
		tutorialPanel_.setName("panel_2");
		
		// TUTORIAL LAYOUT AND CONTENT
		JEditorPane editorPane = new JEditorPane();
		editorPane.setBackground(Color.WHITE);
		editorPane.setContentType("text/html");
		editorPane.setEditable(false);
		
		// Set up the JEditorPane to handle clicks on hyperlinks
		editorPane.addHyperlinkListener(new HyperlinkListener() {

			public void hyperlinkUpdate(HyperlinkEvent arg0) {
				// Handle clicks; ignore mouseovers and other link-related events
				if (arg0.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					openDefaultBrowserClient(arg0.getDescription());
				}
				
			}
	    });
		
		java.net.URL tutorialURL = GnwGuiSettings.getInstance().getHtmlTutorial();
		if (tutorialURL != null) {
		    try {
		        editorPane.setPage(tutorialURL);
		    } catch (IOException e) {
		    	log.warning("Attempted to read a bad URL: " + tutorialURL);
		    }
		} else {
			log.warning("Couldn't find file: " + tutorialURL);
		}

		//Put the editor pane in a scroll pane.
		JScrollPane editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane.setBackground(Color.WHITE);
		editorScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		displayPanel_.add(editorScrollPane, tutorialPanel_.getName());

		// Tutorial -> left hmargin
		final JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		editorScrollPane.setRowHeaderView(panel);

		final Component component_5 = Box.createHorizontalStrut(5);
		panel.add(component_5);

		// Tutorial -> top vmargin
		final JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.WHITE);
		editorScrollPane.setColumnHeaderView(panel_1);

		final Component component_6 = Box.createVerticalStrut(15);
		panel_1.add(component_6);
		
		
		helpPanel_ = new JPanel();
		helpPanel_.setBackground(Color.WHITE);
		helpPanel_.setLayout(new BorderLayout());
		helpPanel_.setName("helpPanel");
		
		// HELP LAYOUT AND CONTENT
		JEditorPane editorPane2 = new JEditorPane();
		editorPane2.setBackground(Color.WHITE);
		editorPane2.setContentType("text/html");
		editorPane2.setEditable(false);
		
		// Set up the JEditorPane to handle clicks on hyperlinks
		editorPane2.addHyperlinkListener(new HyperlinkListener() {

			public void hyperlinkUpdate(HyperlinkEvent arg0) {
				// Handle clicks; ignore mouseovers and other link-related events
				if (arg0.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					openDefaultBrowserClient(arg0.getDescription());
				}
				
			}
	    });
		
		java.net.URL helpURL = GnwGuiSettings.getInstance().getHtmlHelp();
		if (helpURL != null) {
		    try {
		        editorPane2.setPage(helpURL);
		    } catch (IOException e) {
		    	log.warning("Attempted to read a bad URL: " + helpURL);
		    }
		} else {
			log.warning("Couldn't find file: " + helpURL);
		}

		//Put the editor pane in a scroll pane.
		JScrollPane editorScrollPane2 = new JScrollPane(editorPane2);
		editorScrollPane2.setBackground(Color.WHITE);
		editorScrollPane2.setBorder(new EmptyBorder(0, 0, 0, 0));
		editorScrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		displayPanel_.add(editorScrollPane2, helpPanel_.getName());
		
		// Help -> left hmargin
		final JPanel panel100 = new JPanel();
		panel100.setBackground(Color.WHITE);
		editorScrollPane2.setRowHeaderView(panel100);

		final Component component101 = Box.createHorizontalStrut(5);
		panel100.add(component101);

		// Help -> top vmargin
		final JPanel panel102 = new JPanel();
		panel102.setBackground(Color.WHITE);
		editorScrollPane2.setColumnHeaderView(panel102);

		final Component component103 = Box.createVerticalStrut(15);
		panel102.add(component103);


	}
	
	
	
	public boolean canUseDefaultMailClient() {
		try {
			if (!java.awt.Desktop.isDesktopSupported()) {
				String error = "Java.awt.Desktop is not supported!";
				dialogJavaAwtDesktopError(error);
				return false;
			}
			java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
			
	        if(!desktop.isSupported(java.awt.Desktop.Action.MAIL)) {
	        	String error = "Desktop doesn't support the mail action!";
	        	dialogJavaAwtDesktopError(error);
	        	return false;
	        }
	        return true;
		} catch (NoClassDefFoundError e) {
			//log.warning(e.getMessage());
		}
		return false;
	}
	
	
	public boolean canUseDefaultBrowser() {
		try {
			if (!java.awt.Desktop.isDesktopSupported()) {
				String error = "Java.awt.Desktop is not supported!";
				dialogJavaAwtDesktopError(error);
				return false;
			}
			java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
			
	        if(!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
				String error = "Desktop doesn't support the browser action!";
				dialogJavaAwtDesktopError(error);
				return false;
	        }
	        return true;
		} catch (NoClassDefFoundError e) {
			//log.warning(e.getMessage());
		}
		return false;
	}

		
	public void openDefaultMailClient(String email) {

		GnwGuiSettings settings = GnwGuiSettings.getInstance();
		
        try {
        	if (canUseDefaultMailClient()) { // for Unix and Windows
        		java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        		String cmd = "mailto:" + email;
                java.net.URI uri = new java.net.URI(cmd);
                desktop.mail(uri);
        	} else if (settings.isMac()) { // for Mac OS
        		String cmd = "open mailto:" + email;
        		Runtime.getRuntime().exec(cmd);
        	}
        }
        catch (Exception e) {
        	log.warning(e.getMessage());
        }
	}
	
	
	public void openDefaultBrowserClient(String url) {
		
		GnwGuiSettings settings = GnwGuiSettings.getInstance();
		
        try {
        	if (canUseDefaultBrowser()) { // for Unix and Windows
        		java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                java.net.URI uri = new java.net.URI(url);
                desktop.browse(uri);
        	} else if (settings.isMac()) {
        		String cmd = "open " + url;
        		Runtime.getRuntime().exec(cmd);
        	}
        }
        catch (Exception e) {
        	log.warning(e.getMessage());
        }
	}
	
	
	public void dialogJavaAwtDesktopError(String error) {
		log.warning(error);
		JOptionPane.showMessageDialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), error, "GNW Message", JOptionPane.WARNING_MESSAGE);
	}
	
	// ============================================================================
	// GETTERS AND SETTERS
	
	public JFrame getFrame() { return frame; }
}
