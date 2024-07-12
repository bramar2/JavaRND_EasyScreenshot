package bramar.easyscreenshot;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

public class Main implements NativeKeyListener, NativeMouseListener {
	private File lastScreenshot;
	private File lastScreenshotBefore;
	private long lastMS = 0;
	private File directory;
	private File mainDirectory;
	private boolean cropped = false;
	private Location loc1, loc2; // Crop locations
	private int selectionListening = 0; // 0 | 1 getting loc1 | 2 getting loc2
	private WindowsNotification notif;
	private Logs log = new Logs();
//	private Timer timer = new Timer(true);
	private Main() throws Exception {
		GlobalScreen.registerNativeHook();
		GlobalScreen.addNativeKeyListener(this);
		GlobalScreen.addNativeMouseListener(this);
		mainDirectory = new File("screenshots");
		if(!mainDirectory.exists()) mainDirectory.mkdirs();
		notif = new WindowsNotification();
		// Registering
	}
	public synchronized BufferedImage takeScreenshotImage() throws AWTException {
		return new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
	}
	public synchronized void takeScreenshot() throws IOException, AWTException {
		File folder = (directory == null) ? mainDirectory : directory;
		if(!folder.exists()) folder.mkdirs();
		String time = getTime();
		File file = new File(folder, time + ".png");
		boolean hasNumber = false;
		while(file.exists()) {
			if(hasNumber) {
				try {
					String name = file.getName();
					String[] split = name.substring(0, name.lastIndexOf('.')).split("_");
					int n = Integer.parseInt(split[split.length-1]);
					file = new File(folder, time + "_" + n + ".png");
				}catch(Exception e1) {
					file = new File(folder, time + "_1.png");					
				}
			}else file = new File(folder, time + "_1.png");
		}
		BufferedImage img = takeScreenshotImage();
		if(cropped) img = img.getSubimage(loc1.getX(), loc1.getY(), loc1.distanceX(loc2), loc1.distanceY(loc2));
		ImageIO.write(img, "png", file);
		lastScreenshot = file;
		lastScreenshotBefore = null;
	}
	public String getTime() {
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.HOUR_OF_DAY) + "." + c.get(Calendar.MINUTE) + "." + c.get(Calendar.SECOND);
	}
	

	private final long delay = 500;
	
	private boolean f2Pressed = false;
	private boolean fPressed = false;
	private boolean sPressed = false;
	private boolean dPressed = false;
	private boolean zPressed = false;
	public int dialog(String title, String message, int messageType) {
		return JOptionPane.showOptionDialog(null, message, title, 0, messageType, null, null, null);
	}
	public static void main(String[] args) throws Exception {
		new Main();
	}
	@Override
	public void nativeMouseClicked(NativeMouseEvent e) {
		
	}
	@Override
	public void nativeMousePressed(NativeMouseEvent e) {
		if(selectionListening == 1) {
			cropped = false;
			loc1 = new Location(e.getX(), e.getY());
			selectionListening = 2;
		}else if(selectionListening == 2) {
			loc2 = new Location(e.getX(), e.getY());
			selectionListening = 0;
			cropped = true;
			notif.displayInfo("EasyScreenshot", "Crop positions set to " + loc1 + " and " + loc2);
		}
	}
	@Override
	public void nativeMouseReleased(NativeMouseEvent e) {
		
	}
	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		if(e.getKeyCode() == NativeKeyEvent.VC_PRINTSCREEN) {
			log.print("Printscreen");
			if(System.currentTimeMillis() - lastMS < delay) { // delay
				log.print("On a " + delay + " ms delay! " + ((lastMS + delay) - System.currentTimeMillis()) + "ms left");
				return;
			}
			try {
				takeScreenshot();
				lastMS = System.currentTimeMillis();
				notif.displayNormal("EasyScreenshot", "Took screenshot successfully");
				log.print("Took screenshot");
			}catch(Exception e1) {
				log.print(e1);
			}
		}
		if(e.getKeyCode() == NativeKeyEvent.VC_F2) f2Pressed = true;
		if(e.getKeyCode() == NativeKeyEvent.VC_F) fPressed = true;
		if(e.getKeyCode() == NativeKeyEvent.VC_S) sPressed = true;
		if(e.getKeyCode() == NativeKeyEvent.VC_D) dPressed = true;
		if(e.getKeyCode() == NativeKeyEvent.VC_Z) zPressed = true;
		if(f2Pressed && fPressed) {
			notif.displayInfo("EasyScreenshot", "Input box has been shown. EasyScreenshot have been paused until it is closed/entered. The input box may be behind apps currently selected.");
			String output = JOptionPane.showInputDialog(null, "Enter new directory");
			StringBuilder builder = new StringBuilder(output);
			for(int i = 0; i < 20; i++) builder.append('\n').append(output);
			log.print(builder);
			if(output != null) try {
				if(directory != null && directory.getName().equalsIgnoreCase(output)) return;
				File folder = new File(mainDirectory, output);
				if(!folder.exists()) if(!folder.mkdirs()) throw new IOException("Failed to create directory: " + output);
				directory = folder;
				if(lastScreenshot != null) {
					File file = new File(folder, lastScreenshot.getName());
					Files.move(lastScreenshot.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
					lastScreenshotBefore = lastScreenshot;
					lastScreenshot = file;
				}
			}catch(Exception e1) {
				log.print(e1);
			}
		}else if(f2Pressed && sPressed) {
			selectionListening = 1;
			notif.displayInfo("EasyScreenshot", "Use mouse to select 2 positions (by clicking 2x on 2 positions)");
		}else if(f2Pressed && dPressed && lastScreenshot != null && lastScreenshot.exists()) {
			if(lastScreenshot.delete())
				notif.displayNormal("EasyScreenshot", "Deleted screenshot");
		}else if(f2Pressed && zPressed && lastScreenshot != null && lastScreenshotBefore != null) {
			try {
				Files.move(lastScreenshot.toPath(), lastScreenshotBefore.toPath(), StandardCopyOption.REPLACE_EXISTING);
				lastScreenshot = lastScreenshotBefore;
				lastScreenshotBefore = null;
			}catch(IOException e1) {
				log.print(e1);
			}
			
		}
		if(e.getKeyCode() == NativeKeyEvent.VC_ESCAPE && selectionListening != 0) {
			if(selectionListening == 1) selectionListening = 0;
			else {
				cropped = false;
				selectionListening = 0;
			}
		}
	}
	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		if(e.getKeyCode() == NativeKeyEvent.VC_F2) f2Pressed = false;
		if(e.getKeyCode() == NativeKeyEvent.VC_F) fPressed = false;
		if(e.getKeyCode() == NativeKeyEvent.VC_S) sPressed = false;
		if(e.getKeyCode() == NativeKeyEvent.VC_D) dPressed = false;
		if(e.getKeyCode() == NativeKeyEvent.VC_Z) zPressed = false;
	}
	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {
		
	}
}
