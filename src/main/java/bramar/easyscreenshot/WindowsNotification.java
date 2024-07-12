package bramar.easyscreenshot;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;

public class WindowsNotification {
	private boolean supported = false;
	private TrayIcon tray;
	public WindowsNotification() {
		if(SystemTray.isSupported()) {
			supported = true;
			display();
		}
	}
	public boolean isSupported() {
		return supported;
	}
	private void display() {
		final SystemTray tray = SystemTray.getSystemTray();
		Image image = Toolkit.getDefaultToolkit().createImage(Main.class.getResource("/java.png"));
//		Image image = Toolkit.getDefaultToolkit().createImage("java.png");
		this.tray = new TrayIcon(image, "EasyScreenshot");
		this.tray.setImageAutoSize(true);
		this.tray.setToolTip("EasyScreenshot Tray");
		try {
			tray.add(this.tray);
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				tray.remove(WindowsNotification.this.tray);
			}));
		}catch(AWTException e) {
			e.printStackTrace();
			supported = false;
			this.tray = null;
		}
	}
	public void displayInfo(String title, String message) {
		if(supported) tray.displayMessage(title, message, MessageType.INFO);
	}
	public void displayError(String title, String message) {
		if(supported) tray.displayMessage(title, message, MessageType.ERROR);
	}
	public void displayNormal(String title, String message) {
		if(supported) tray.displayMessage(title, message, MessageType.NONE);
	}
	public void displayWarning(String title, String message) {
		if(supported) tray.displayMessage(title, message, MessageType.WARNING);
	}
	
}
