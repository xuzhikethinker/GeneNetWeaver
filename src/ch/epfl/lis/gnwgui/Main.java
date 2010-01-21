package ch.epfl.lis.gnwgui;

public class Main {

	/** Main class of GNW with GUI.
	 * 
	 * @author Thomas Schaffter (firstname.name@gmail.com)
	 * 
	 */
	public static void main(String[] args) {
		try {
			GnwGui.parse(args);
			GnwGui.setPackageLoggers();
			GnwGui.setPlatformPreferences();
			GnwGui.displaySplash(); // before the setLookAndFeel to benefit from native load bar
			//setLookAndFeel(GnwGuiSettings.getInstance().isMac() || 
			//			   GnwGuiSettings.getInstance().getNativeLookAndFeel());
			GnwGui.setLookAndFeel();
			
			GnwGui gui = new GnwGui();
			gui.getSplashScreen().setVisible(false);
			gui.getFrame().setVisible(true);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
