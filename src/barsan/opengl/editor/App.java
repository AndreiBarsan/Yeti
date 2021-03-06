package barsan.opengl.editor;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import barsan.opengl.Yeti;
import barsan.opengl.platform.SwingFactory;
import barsan.opengl.rendering.Scene;
import barsan.opengl.util.GLHelp;
import barsan.opengl.util.TextUtil;

public class App {

	private JFrame frmYeti;
	private Yeti engine;
	
	private JSlider anisotropicSlider;
	private JLabel anisotropicLabel;
	JToolBar toolBar;
	
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
		
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		frmYeti.getContentPane().add(toolBar);
		
		anisotropicSlider = new JSlider(1, 1);
		anisotropicLabel = new JLabel("Anisotropic filtering:");
		toolBar.add(anisotropicLabel);
		toolBar.add(anisotropicSlider);
		
		JLabel lblPressEscapeTo = new JLabel(" Press Escape to release the mouse. ");
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
		
		engine.startApplicationLoop(this, frmYeti, frmYeti.getContentPane(), new SwingFactory());		
	}
	
	public void generateGLKnobs(final Yeti yeti) {
		GL gl = yeti.gl;
		
		anisotropicSlider.setMaximum( (int) GLHelp.get1f(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT));
		anisotropicSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				Yeti.get().settings.anisotropySamples = source.getValue();
				anisotropicLabel.setText("Anisotropic filtering: " + source.getValue());
			}
		});
		anisotropicSlider.setValue(Yeti.get().settings.anisotropySamples);	
		
		JMenu menu = new JMenu("Scene");
		for(int i = 0; i < Yeti.getAvailableScenes().length; i++) {
			final Class<?> s = Yeti.getAvailableScenes()[i];
			@SuppressWarnings("serial")
			JMenuItem item = new JMenuItem(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						yeti.loadScene((Scene)s.newInstance());
					} catch (InstantiationException e1) {
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						e1.printStackTrace();
					}
				}
			});
			item.setText(TextUtil.splitCamelCase(s.getSimpleName()));
			menu.add(item);
		}
		
		frmYeti.getJMenuBar().add(menu);
	}
}
