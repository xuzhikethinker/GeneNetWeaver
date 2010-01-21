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

package ch.epfl.lis.gnwgui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnwgui.filefilters.FilenameUtilities;
import ch.epfl.lis.gnwgui.windows.GnwGuiWindow;
import ch.epfl.lis.imod.LoggerManager;
import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.networks.HierarchicalScaleFreeNetwork;
import ch.epfl.lis.networks.ios.ParseException;

import com.jgoodies.looks.plastic.PlasticLookAndFeel; 
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
/** Silver, LightGray, (DarkStar) */
import com.jgoodies.looks.plastic.theme.Silver;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;

import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJOpenApplicationHandler;
import com.apple.mrj.MRJOpenDocumentHandler;
import com.apple.mrj.MRJQuitHandler;


/** Main class of the GeneNetWeaver GUI application.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * 
 */
public class GnwGui extends GnwGuiWindow implements MRJOpenApplicationHandler, MRJOpenDocumentHandler,
MRJQuitHandler{
	
    /** Splash Screen */
    private static SplashScreen splashScreen;
    
    /** Packages to set up for logging. */
    private static String[] packages2log = {"ch.epfl.lis.gnw", 
    										 "ch.epfl.lis.gnwgui", 
    										 "ch.epfl.lis.gnwgui.filefilters", 
    										 "ch.epfl.lis.gnwgui.idesktop", 
    										 "ch.epfl.lis.gnwgui.jungtransformers", 
    										 "ch.epfl.lis.gnwgui.windows", 
    										 "ch.epfl.lis.imod", 
    										 "ch.epfl.lis.networks", 
    										 "ch.epfl.lis.networks.ios",
    										 "ch.epfl.lis.sde"};
    
	/** StyledDocument for the content of the console */
	private StyledDocument doc_ = null;
	/** Style for the content of the console */
	private Style style_ = null;
    
    /** Logger for this class */
    private static Logger log = Logger.getLogger(GnwGui.class.getName());

    
	// ============================================================================
	// PUBLIC METHODS
    

	/**
	 * Constructor
	 */
	public GnwGui() {
	   try {
			initialize();
	   } catch (Exception e) {
		   log.log(Level.WARNING, "GNW initialization failed!");
	   }
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * This function defines which are the command line arguments that can be
	 * specified. Make use of the JSAP library.
	 * 
	 * JSAP - Java Simple Argument Parser
	 * http://martiansoftware.com/jsap/doc/
	 */
	public static void parse(String args[]) throws Exception {
		// Java Simple Argument Parser 
		JSAP jsap = new JSAP();
		
		// Settings file argument
        FlaggedOption gnwSettings = new FlaggedOption("settingsFile")
        							.setStringParser(JSAP.STRING_PARSER)
        							.setDefault("local")
        							.setRequired(false)
        							.setShortFlag('s') 
        							.setLongFlag("settings");
        
        // Allow AWT window			DEPRECATED
        Switch allowAwt = new Switch("allowAwt")
									.setLongFlag("allowAwt");
        allowAwt.setHelp("Allow GNW to use strict AWT components");
        
        // Use native look and feel
        Switch useSwingNativeLookAndFeel = new Switch("useSwingNativeLookAndFeel") 
									.setLongFlag("swingNativeLAF");
        useSwingNativeLookAndFeel.setHelp("Use the Swing native look & feel");
        
        // Run GNW in "test" mode
        Switch test = new Switch("test")
									.setLongFlag("test");
        test.setHelp("Test the implementation of GNW");
        
        try {
			jsap.registerParameter(gnwSettings);
			jsap.registerParameter(allowAwt);
			jsap.registerParameter(useSwingNativeLookAndFeel);
			jsap.registerParameter(test);
			JSAPResult settings = jsap.parse(args);
			
        	try {
        		if (settings.userSpecified("settingsFile")) // load command-line settings file
        			GnwSettings.getInstance().loadInitialConfiguration(settings.getString("settingsFile"));
        		else
        			GnwSettings.getInstance().loadInitialConfiguration("");
        			
        	} catch (Exception e) {
        		log.warning("Loading settings file: " + e.getMessage());
        	}
	        	
	        if (settings.userSpecified("allowAwt"))
	        	GnwGuiSettings.getInstance().allowAwt(settings.getBoolean("allowAwt"));
	        if (settings.userSpecified("useSwingNativeLookAndFeel"))
	        	GnwGuiSettings.getInstance().useSwingNativeLookAndFeel(settings.getBoolean("useSwingNativeLookAndFeel"));
	        if (settings.userSpecified("test")) {
	        	test();
	        	System.exit(0);
	        }
		} catch (JSAPException e) {
			log.warning(e.getMessage());
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	public static void test() {
		
		System.out.println("TEST starts...");
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Initialization
	 */
	private void initialize() {
		
		splashScreen.setTaskInfo("Initialization");
		GnwGuiSettings settings = GnwGuiSettings.getInstance();
		settings.setGnwGui(this);
		setGnwConsole();
		
		
		if (GnwGuiSettings.getInstance().isMac()) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "GeneNetWeaver");
		}

		// Console welcome
		String text = "Welcome to GeneNetWeaver " + GnwSettings.getInstance().getGnwVersion() + "\n";
		log.info(text);
		
		
		// Define the actions of the components of the window
		// Add a listener for the close event
	    frame.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent evt) {
	        	if (!shouldExit())
	        		return;
	        	//frame.setVisible(false);
	        	//frame.dispose();
				closeAction();
	        }
	    });
	    
		about_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				mainDisplayLayout_.show(displayPanel_, aboutPanel_.getName());
				header_.setTitle("GeneNetWeaver");
				header_.setInfo(GnwSettings.getInstance().getGnwVersion());
			}
		});
		
		networkManager_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				mainDisplayLayout_.show(displayPanel_, networkPanel_.getName());
				header_.setTitle("Network Manager");
				header_.setInfo("Click on networks for options (blue = network " +
				"structures / orange = dynamical " +
				"network models)");
			}
		});
		
		settings_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				mainDisplayLayout_.show(displayPanel_, settingsPanel_.getName());
				header_.setTitle("Settings");
				header_.setInfo("Load/save and edit GNW settings");
			}
		});
		
		tutorial_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				mainDisplayLayout_.show(displayPanel_, tutorialPanel_.getName());
				header_.setTitle("Tutorial");
				header_.setInfo("Generating a Benchmark with GNW");
			}
		});
		
		help_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				mainDisplayLayout_.show(displayPanel_, helpPanel_.getName());
				header_.setTitle("Help");
				header_.setInfo("GNW documentation");
			}
		});
		
		exit_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (shouldExit())
					closeAction();
			}
		});
		
		applySettingsButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				applySettings();
			}
		});
		
		reloadSettingsButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				reloadSettings();
			}
		});
		
		openSettingsButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				openSettings();
			}
		});
		
		exportSettingsButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				exportSettings();
			}
		});
		
		
		consoleToggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				GnwConsoleHandler.getInstance().displayConsoleWindow(true);
			}
		});
		
		// Display the Network manager after launching
		networkManager_.doClick();
		
		splashScreen.stepDone();
		

		// Create an instance of hierarchical scale-free network (Ravasz and al.)
		splashScreen.setTaskInfo("Loading Example");
		HierarchicalScaleFreeNetwork scaleFree = new HierarchicalScaleFreeNetwork(3, "G");
		scaleFree.setId("Example");
		StructureElement scaleFreeItem = new StructureElement(scaleFree.getId(), settings.getNetworkDesktop());
		scaleFreeItem.setNetwork(new ImodNetwork(scaleFree));
		settings.getNetworkDesktop().addItemOnDesktop(scaleFreeItem);
		IONetwork.printOpeningInfo(scaleFreeItem);
		splashScreen.stepDone();
		
		loadInitialNetworks();
		
		scaleFreeItem.setToolTipText(
				"<html>Hierarchical scale-free network model: 64 nodes, 207 edges.<br>" +
				"Has a scale-free topology with embedded modularity similar to many<br>" +
				"biological networks (Ravasz et al. 2002. <i>Science</i>, 297:1551-55).</html>");
		networkDesktop_.getIElementFromLabel("Ecoli").setToolTipText(
				"<html>E.coli transcriptional regulatory network: 1502 nodes, 3587 edges.<br>" +
				"Corresponds to the TF-gene interactions of RegulonDB release 6.2.<br>" +
				"(Gama-Castro et al. 2008. <i>Nucleic Acids Res</i>, 36:D120-4).</html>");
		networkDesktop_.getIElementFromLabel("Yeast").setToolTipText(
				"<html>Yeast transcriptional regulatory network: 4441 nodes, 12873 edges.<br>" +
				"As described in: Balaji et al. 2006. <i>J Mol Biol</i>, 360:213-27.</html>");
		
		// Link the escape action with anyone of the components that are always present in the main
		// application window.
		keyboardExit(header_);
		
		doc_ = (StyledDocument)settingsTextPane_.getDocument();
		style_ = doc_.addStyle("settingsStyle", null);
		displaySettingsContent();
		
		MRJApplicationUtils.registerOpenApplicationHandler(this);
		MRJApplicationUtils.registerOpenDocumentHandler(this);
		MRJApplicationUtils.registerQuitHandler(this);
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Display a dialog that asks the user if he/she really want to leave the application.
	 * @return Return true if the user answer is "Yes" to the question "Exit GeneNetWeaver ?".
	 */
	public boolean shouldExit() {
		
		ImageIcon icon = new ImageIcon(GnwGuiSettings.getInstance().getMenuExitImage());
		
			int n = JOptionPane.showConfirmDialog(
					frame,
				    "Exit GeneNetWeaver ?",
				    "GNW message",
				    JOptionPane.YES_NO_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    icon);

			if (n == JOptionPane.YES_OPTION)
				return true; // If the user selected YES
			else
				return false;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Associated the logger to the console handler.
	 */
	public void setGnwConsole() {
		Handler ch = GnwConsoleHandler.getInstance();
		for (int i=0; i < packages2log.length; i++)
			Logger.getLogger(packages2log[i]).addHandler(ch);
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	/**
	 * @throws IOException, Exception 
	 * 
	 */
	public String loadSettingsContent() throws IOException, Exception {
	
		// Create a URL for the desired page
		URL url = GnwSettings.getInstance().getLastSettingsURL();
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String str = "";
		String tmp = in.readLine();
		
		while (tmp != null) {
			str += tmp + "\n";
			tmp = in.readLine();
		}
		in.close();
		
		return str;
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	/**
	 * Display the content of the last settings file loaded.
	 */
	public void displaySettingsContent() {
		try {
			printSettingsContent(loadSettingsContent());
			settingsTextPane_.setCaretPosition(0); // set the cursor on the first line
		} catch (BadLocationException e) {
			printSettingsContent("Unable to display settings file content, see console for details.");
			log.warning("Unable to display settings file content (BadLocationException): " + e.getMessage());
		} catch (IOException e) {
			printSettingsContent("Unable to display settings file content, see console for details.");
			log.warning("Unable to display settings file content (IOException): " + e.getMessage());
		} catch (Exception e) {
			printSettingsContent("Unable to display settings file content, see console for details.");;
			log.warning("Unable to display settings file content (Exception): " + e.getMessage());
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	/**
	 * Apply the new settings that have been edited by the user inside GNW.
	 */
	public void applySettings() {
		
		try {
			// get the content of the settings pane
			String data = doc_.getText(0, doc_.getLength());
			InputStream is = new ByteArrayInputStream(data.getBytes("UTF-8"));
			GnwSettings.getInstance().loadSettingsFromStream(is);
			
			log.info("The new settings are successfully applied!");
		
		} catch (BadLocationException e) {
			JOptionPane.showMessageDialog(this.getFrame(), "Unable to apply the new settings, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log.warning("Unable to apply the new settings (BadLocationException): " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			JOptionPane.showMessageDialog(this.getFrame(), "Unable to apply the new settings, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log.warning("Unable to apply the new settings (UnsupportedEncodingException): " + e.getMessage());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this.getFrame(), "Unable to apply the new settings, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log.warning("Unable to apply the new settings (Exception): " + e.getMessage());
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	public void reloadSettings() {
		
		try {
			GnwSettings.getInstance().loadLastSettingsOpened();
			displaySettingsContent();
			
			log.info("The settings file " + FilenameUtilities.getFilenameWithoutPath(GnwSettings.getInstance().getLastSettingsURL().getPath()) + " is successfully loaded!");
			
    	} catch (IOException e) {
    		JOptionPane.showMessageDialog(this.getFrame(), "Unable to reload the settings file, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log.warning("Unable to reload the settings file (IOException): " + e.getMessage());
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(this.getFrame(), "Unable to reload the settings file, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log.warning("Unable to reload the settings file (Exception): " + e.getMessage());
    	}
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	public void openSettings() {
		
		IODialog dialog = new IODialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Open Settings",
				GnwSettings.getInstance().getOutputDirectory(), IODialog.LOAD, null);
		
		dialog.setAcceptAllFileFilterUsed(true);
		dialog.display();
		
    	try {
    		if (dialog.getSelection() != null) {
    			GnwSettings.getInstance().loadSettingsFromURL(new URL("file:///" + dialog.getSelection()));
    			displaySettingsContent();
    			log.info("The settings file " + FilenameUtilities.getFilenameWithoutPath(dialog.getSelection()) + " is successfully loaded!");
    			
				// Save the current directory as default path
				String dir = FilenameUtilities.getDirectory(dialog.getSelection());
				GnwSettings.getInstance().setOutputDirectory(dir);
    		}
    		
    	} catch (IOException e) {
    		JOptionPane.showMessageDialog(this.getFrame(), "Unable to load the settings file, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log.warning("Unable to load the settings file (IOException): " + e.getMessage());
    	} catch (Exception e) {
       		JOptionPane.showMessageDialog(this.getFrame(), "Unable to load the settings file, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log.warning("Unable to load the settings file (Exception): " + e.getMessage());
    	}
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	public void exportSettings() {
		
		IODialog dialog = new IODialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Export Settings",
				GnwSettings.getInstance().getOutputDirectory(), IODialog.SAVE, null);
		
		dialog.setAcceptAllFileFilterUsed(true);
		dialog.display();
		
		try {
			if (dialog.getSelection() != null) {
				OutputStream output = new FileOutputStream(dialog.getSelection()); 
				output.write(doc_.getText(0, doc_.getLength()).getBytes("UTF-8"));
				output.close();
				log.info("The settings file " + FilenameUtilities.getFilenameWithoutPath(dialog.getSelection()) + " is successfully saved!");
				
				// Save the current directory as default path
				String dir = FilenameUtilities.getDirectory(dialog.getSelection());
				GnwSettings.getInstance().setOutputDirectory(dir);
			}

    	} catch (IOException e) {
    		JOptionPane.showMessageDialog(this.getFrame(), "Unable to save the settings, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log.warning("Unable to save the settings (IOException): " + e.getMessage());
    	} catch (BadLocationException e) {
    		JOptionPane.showMessageDialog(this.getFrame(), "Unable to save the settings, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log.warning("Unable to save the settings (BadLocationException): " + e.getMessage());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this.getFrame(), "Unable to save the settings, see console for details.", "GNW message", JOptionPane.WARNING_MESSAGE);
			log.warning("Unable to save the settings (Exception): " + e.getMessage());
     	}
	}
	
	
	// ----------------------------------------------------------------------------

	
	/**
	 * Use this function to print the settings content
	 * @param data Text
	 * @throws BadLocationException 
	 */
	public void printSettingsContent(String data) {
			try {
				doc_.remove(0, doc_.getLength()); // clean the content of the settings window
				doc_.insertString(0, data, style_); // insert the content of the settings file loaded
			} catch (BadLocationException e) {
				log.warning("Unable to print the settings file content: " + e.getMessage());
			} 
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	/**
	 * Load initial networks from the content of GnwGuiSettings.initialNetworksToLoad_.
	 * These networks (structures and dynamical models) will be loaded at the end of
	 * the initialization of the program.
	 */
	@SuppressWarnings("unchecked")
	public void loadInitialNetworks() {
		
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		Map<String, URL> list = global.getInitialNetworksToLoad();
		Iterator<?> it = list.entrySet().iterator();
		
	    while (it.hasNext()) {
	        try {
		        Map.Entry pairs = (Map.Entry)it.next();
		        splashScreen.setTaskInfo("Loading " + (String)pairs.getKey());
				IONetwork.loadItem((String)pairs.getKey(), (URL) pairs.getValue(), null);
			} catch (FileNotFoundException e) {
				log.warning("Unable to load initial networks (FileNotFoundException): " + e.getMessage());
			} catch (ParseException e) {
				log.warning("Unable to load initial networks (ParseException): " + e.getMessage());
			} catch (Exception e) {
				log.warning("Unable to load initial networks (Exception): " + e.getMessage());
			}
			splashScreen.stepDone();
	    }
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	/**
	 * Start the exit process when user press on ESCAPE.
	 */
	@SuppressWarnings("serial")
	public void keyboardExit(JComponent jp) {
	   KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	   jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "EXIT");
	   jp.getActionMap().put("EXIT", new AbstractAction() {
		   public void actionPerformed(ActionEvent arg0) {
				if (shouldExit())
					closeAction();
		   }
	   });
	}

	
	// ----------------------------------------------------------------------------
	
	
	/**
	 * Called to close the application.
	 */
	public void closeAction() {
		log.info("ByeBye!");
		System.exit(0);
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	/**
	 * 
	 */
	public static void setLookAndFeel() {
		
		boolean defautlLAF = GnwGuiSettings.getInstance().useSwingNativeLookAndFeel();
		
		try {
			if (defautlLAF) {
				// use the native look and feel
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			else {
				PlasticLookAndFeel.setPlasticTheme(new Silver());
				UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
				PlasticXPLookAndFeel lookXP = new PlasticXPLookAndFeel();
				UIManager.setLookAndFeel(lookXP);
			}
		} catch (ClassNotFoundException e) {
			log.warning("Unable to set Java Look and Feel (ClassNotFoundException): " + e.getMessage());
		} catch (InstantiationException e) {
			log.warning("Unable to set Java Look and Feel (InstantiationException): " + e.getMessage());
		} catch (IllegalAccessException e) {
			log.warning("Unable to set Java Look and Feel (IllegalAccessException): " + e.getMessage());
		} catch (UnsupportedLookAndFeelException e) {
			log.warning("Unable to set Java Look and Feel (UnsupportedLookAndFeelException): " + e.getMessage());
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	/**
	 * Display the application splash screen
	 */
	public static void displaySplash() {
		splashScreen = new SplashScreen(new Frame(), GnwGuiSettings.getInstance().getSplashScreenImage(),
				   SplashScreen.NORMAL, true, true, false);
		
		splashScreen.setVisible(true);
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	/**
	 * Set preferences according to the running OS.
	 */
	public static void setPlatformPreferences() {
		if (GnwGuiSettings.getInstance().isMac()) {
			// combine application menu bar with Mac OS menu bar
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("apple.awt.brushMetalLook", "true");
			System.setProperty("apple.awt.antialiasing", "on");
			System.setProperty("apple.awt.textantialiasing", "on");
			System.setProperty("apple.awt.rendering", "quality"); // speed or quality
			System.setProperty("apple.awt.interpolation", "nearestneighbor, "); // nearestneighbor, bilinear, or bicubic
			System.setProperty("apple.awt.graphics.UseQuartz", "true, ");
			System.setProperty("apple.awt.graphics.EnableQ2DX", "true, "); // should not be enough to be activated
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	/**
	 * 
	 */
	public static void setPackageLoggers() {
		// TODO: put the file outside the jar
		InputStream stream = GnwGui.class.getResourceAsStream("gnwguiLogSettings.txt");
		LoggerManager manager = new LoggerManager(stream); // load and apply the log settings files
		for (int i=0; i < GnwGui.packages2log.length; i++)
			manager.setLoggerPolicyForPackage(packages2log[i]);
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	/**
	 * Mac OS X - Handle dock-dropped file
	 */
	public void handleOpenFile(final File file) {
		IONetwork.open(file.getAbsolutePath(), null);
		networkManager_.doClick(); // focus on network manager
	}
	
	public void handleQuit() {
		closeAction();
	}

	public void handleOpenApplication() {
		
	}
	
	
	// ----------------------------------------------------------------------------
	
	
	public SplashScreen getSplashScreen() { return splashScreen; }
}
