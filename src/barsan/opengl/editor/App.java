package barsan.opengl.editor;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import barsan.opengl.Yeti;

public class App {

	private JFrame frmYeti;
	private Yeti engine;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					App window = new App();
					window.frmYeti.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public App() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		System.out.println("Initializing YETI-edit");
		frmYeti = new JFrame();
		frmYeti.setResizable(false);
		engine = Yeti.get();
		
		frmYeti.setTitle("Hosted inside SWING");
		//frmYeti.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmYeti.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Yeti.quit();
			}
		});
		
		
		frmYeti.getContentPane().setLayout(new BoxLayout(frmYeti.getContentPane(), BoxLayout.Y_AXIS));
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		frmYeti.getContentPane().add(toolBar);
		
		JButton btnOption = new JButton("Option 1");
		toolBar.add(btnOption);
		
		JButton btnOption_1 = new JButton("Option 2");
		toolBar.add(btnOption_1);
		
		JLabel lblPressEscapeTo = new JLabel("Press Escape to release the mouse");
		lblPressEscapeTo.setHorizontalAlignment(SwingConstants.RIGHT);
		toolBar.add(lblPressEscapeTo);
		
		JMenuBar menuBar = new JMenuBar();
		
		frmYeti.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		
		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Yeti.quit();
			}
		});
		mnFile.add(mntmQuit);
		
		engine.hackStartLoop(frmYeti, frmYeti.getContentPane());
		
	}
}
