package ch.epfl.lis.gnwgui;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/** This class is used to handle open/save dialogs.
 * 
 * Primarily, this class has been designed to be able to switch easily
 * between Swing and AWT dialogs. If the GUI of GNW makes use of Swing
 * in order to have the same GUI on all platforms, the possibility to
 * release GNW as a Mac application is studying because Mac offer nice
 * features to its applications such as drag&drop files on the application
 * icon to open them. What was intended with the implementation of this
 * class was to benefit from the nice, native open/save Mac dialogs.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class IODialog {
	
	private String title_;
	private Frame parent_;
	private Byte mode_;
	private String path_;
	private String selection_;
	
	public static final Byte LOAD = 0;
	public static final Byte SAVE = 1;
	
	public static final Byte AWT = 0;
	public static final Byte SWING = 1;
	
	private FileDialog fd_;
	private JFileChooser jfc_;
	
	private ArrayList<FileFilter> filters_;
	
    /** Logger for this class */
	private static Logger log = Logger.getLogger(IODialog.class.getName());
	
	
	public IODialog(Frame parent, String title, String path, Byte mode, Byte type) {
		parent_ = parent;
		title_ = title;
		path_ = path;
		
		try {
			setMode(mode);
			createDialog(type);
		} catch (IllegalArgumentException iae) {
			log.warning(iae.getMessage());
		} catch (Exception e) {
			log.warning(e.getMessage());
		}
		
		filters_ = new ArrayList<FileFilter>();
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Create LOAD dialog.
	 */
	private void createDialog(Byte type) {
		
		GnwGuiSettings settings = GnwGuiSettings.getInstance();
		boolean awtDialog = settings.allowAwt();
		
		if (awtDialog && (type == null || type == IODialog.AWT)) {
			if (mode_ == IODialog.LOAD)
				fd_ = new FileDialog(parent_, title_, FileDialog.LOAD);
			else if (mode_ == IODialog.SAVE)
				fd_ = new FileDialog(parent_, title_, FileDialog.SAVE);
			jfc_ = null;
		} else if (type == null || type == IODialog.SWING) {
			jfc_ = new JFileChooser(path_);
			fd_ = null;
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Display the dialog.
	 */
	public void display() {
		
		selection_ = null;
		
		if (fd_ != null) {
			fd_.setVisible(true);
			if (fd_.getFile() != null)
				selection_ = fd_.getDirectory() + fd_.getFile();
		}
		else if (jfc_ != null) {
			int returnVal = jfc_.showDialog(parent_, title_);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				selection_ = jfc_.getSelectedFile().getAbsolutePath();
			}
		}
		else
			log.warning("Display failed due to bad initialisation (mode) !");
	}
	
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Add one file filter. To use with Swing dialog.
	 * Native look & feel: no filter selected initially (Why?!)
	 */
	public void addFilter(final FileFilter filter) {
		
		if (jfc_ != null) {
			jfc_.addChoosableFileFilter(filter);
			jfc_.setFileFilter(filter);
		}
		else if (fd_ != null) {
			// save the new filter
			filters_.add(filter);
			FilenameFilter f = new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			    	for (int i=0; i < filters_.size(); i++)
			    		if (filters_.get(i).accept(new File(dir + name)));
			    			return true;
			    }
			};
			fd_.setFilenameFilter(f);
		}
	}
	
	
	/**
	 * Set file filter. To use with AWT dialog.
	 */
	public void setFilter(FilenameFilter filter) {
		if (fd_ != null)
			fd_.setFilenameFilter(filter);
	}
	
	
	// ----------------------------------------------------------------------------
	
	public FileFilter getSelectedFilter() {
		if (jfc_ != null)
			return jfc_.getFileFilter();
		else
			return null;
	}
	
	
	// ----------------------------------------------------------------------------
	
	public String getDirectory() {
		if (fd_ != null)
			return fd_.getDirectory();
		else if (jfc_ != null)
			return jfc_.getSelectedFile().getParentFile().getAbsolutePath();
		else
			return null;
	}
	
	// ----------------------------------------------------------------------------
	

	public void setMode(Byte mode) throws IllegalArgumentException {
		if (mode == IODialog.LOAD || mode == IODialog.SAVE)
			mode_ = mode;
		else {
			mode_ = null;
			throw new IllegalArgumentException("Modes other than {LOAD,SAVE} are invalid !");
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	public void selectOnlyFolder(boolean b) {
		if (jfc_ != null) {
			if (b)
				jfc_.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			else
				;//jfc_.setFileSelectionMode(JFileChooser.FILES_ONLY);
		} else if (fd_ != null) {
			// clear all filters
			filters_.clear();
			FilenameFilter f = new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			    	return (new File(dir + name)).isDirectory();
			    }
			};
			fd_.setFilenameFilter(f);
		}
	}
	
	
	// ----------------------------------------------------------------------------
	
	public void setAcceptAllFileFilterUsed(final boolean b) {
		if (jfc_ != null)
			jfc_.setAcceptAllFileFilterUsed(b);
	}
	
	public void setSelection(String filename) {
		if (jfc_ != null){
			File f = new File(filename);
			jfc_.setSelectedFile(f);
		}
		else {
			fd_.setFile(filename);
			System.out.println("Using AWT dialog: verify that IODialog:setSelection() works");
		}
	}

	// ----------------------------------------------------------------------------
	
	public String getSelection() { return selection_; }
}
