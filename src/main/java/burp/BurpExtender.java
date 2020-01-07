package burp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.ArrayUtils;
import org.fife.ui.rsyntaxtextarea.FileLocation;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.fife.ui.rtextarea.RTextScrollPane;

import burp.CustomPlugin.CustomPluginExecuteValues;
import burp.CustomPlugin.CustomPluginFunctionOutputValues;
import burp.CustomPlugin.CustomPluginParameterValues;
import net.razorvine.pyro.*;

public class BurpExtender implements IBurpExtender, ITab, ActionListener, IContextMenuFactory, MouseListener, IExtensionStateListener {
	
	public static final int PLATFORM_ANDROID = 0;
	public static final int PLATFORM_IOS = 1;
	public static final int PLATFORM_GENERIC = 2;

	public IBurpExtenderCallbacks callbacks;
	public IExtensionHelpers helpers;
    
    public PrintWriter stdout;
    public PrintWriter stderr;
    
    private JPanel mainPanel;
    
    private PyroProxy pyroBridaService;
    private Process pyroServerProcess;
    	
	private JTextField pythonPath;
	private String pythonScript;
	public JTextField pyroHost;
	public JTextField pyroPort;
	private JTextPane serverStatus;
	private JTextPane applicationStatus;
	private JTextField fridaPath;
    private JTextField applicationId;
    
    private JRadioButton remoteRadioButton;
    private JRadioButton localRadioButton;
    	
	private Style redStyle;
	private Style greenStyle;
	DefaultStyledDocument documentServerStatus;
	DefaultStyledDocument documentApplicationStatus;
	
	DefaultStyledDocument documentServerStatusButtons;
	DefaultStyledDocument documentApplicationStatusButtons;
    private JTextPane serverStatusButtons;
    private JTextPane applicationStatusButtons;
	
	private JTextField executeMethodName;
	private JTextField executeMethodArgument;
	private DefaultListModel executeMethodInsertedArgumentList;
	private JList executeMethodInsertedArgument;
	
	public boolean serverStarted;
	public boolean applicationSpawned;
	public boolean customPluginEnabled;
	
	public IContextMenuInvocation currentInvocation;
	
	private ITextEditor stubTextEditor;
    
    private JButton executeMethodButton;
    private JButton saveSettingsToFileButton;
    private JButton loadSettingsFromFileButton;
    private JButton generateJavaStubButton;
    private JButton generatePythonStubButton;    
    private JButton loadJSFileButton;
    private JButton saveJSFileButton; 
    private JButton loadTreeButton;
    private JButton detachAllButton;
    private JButton clearConsoleButton;
    private JButton enableCustomPluginButton;
    
    private JEditorPane pluginConsoleTextArea;
    
    private TextEditorPane jsEditorTextArea;
	
    private Thread stdoutThread;
    private Thread stderrThread;
    
    private JTextField findTextField;
    
    private JTree tree;
    
    private JTable trapTable;
    
    private boolean lastPrintIsJS;
    
    private int platform;
    
    private List<DefaultHook> defaultHooks;
    
    private JPanel customPluginToolsPanel;
    private JPanel customPluginScopePanel;
    private JPanel customPluginButtonTypePanel;
    private JPanel customPluginButtonPlatformPanel;
    private JPanel customPluginExecuteWhenPanel;
    private JPanel customPluginParametersPanel;
    private JPanel customPluginParameterEncodingPanel;
    private JPanel customPluginOutputDecodingPanel;
    private JPanel customPluginOutputEncodingPanel;
    private JPanel customPluginMessageEditorModifiedEncodeInputPanel;
    private JPanel customPluginMessageEditorModifiedDecodingOutputPanel;
    private JPanel customPluginMessageEditorModifiedFridaFunctioPanel;
    private JPanel customPluginMessageEditorModifiedOutputEncodingPanel;
    private JPanel customPluginMessageEditorModifiedOutputLocationPanel;
    private JTextField customPluginNameText;
    private JComboBox<String> customPluginTypePluginOptions;
    private JLabel customPluginTypePluginDescription;
    private JTextField customPluginExportNameText;
    private JRadioButton customPluginExecuteOnRadioRequest;
    private JRadioButton customPluginExecuteOnRadioResponse;
    private JRadioButton customPluginExecuteOnRadioAll;
    private JRadioButton customPluginExecuteOnRadioContext;
    private JRadioButton customPluginExecuteOnRadioButton;
    private JTextField customPluginExecuteOnStringParameter;
    private JRadioButton customPluginButtonTypeRadioFunction;
    private JRadioButton customPluginButtonTypeRadioHook;
    private JRadioButton customPluginButtonTypeRadioIos;
    private JRadioButton customPluginButtonTypeRadioAndroid;
    private JRadioButton customPluginButtonTypeRadioGeneric;
    private JRadioButton customPluginMessageEditorModifiedEncodingInputFridaRadioNone;
    private JRadioButton customPluginMessageEditorModifiedEncodingInputFridaRadioBase64;
    private JRadioButton customPluginMessageEditorModifiedEncodingInputFridaRadioAsciiHex;
    private JRadioButton customPluginOutputMessageEditorModifiedDecodingRadioNone;
    private JRadioButton customPluginOutputMessageEditorModifiedDecodingRadioBase64;
    private JRadioButton customPluginOutputMessageEditorModifiedDecodingRadioAsciiHex;
    private JTextField customPluginMessageEditorModifiedFridaExportNameText;
    private JRadioButton customPluginMessageEditorModifiedOutputEncodingRadioNone;
    private JRadioButton customPluginMessageEditorModifiedOutputEncodingRadioBase64;
    private JRadioButton customPluginMessageEditorModifiedOutputEncodingRadioAsciiHex;
    
    private JCheckBox customPluginToolsRepeater;
    private JCheckBox customPluginToolsProxy;
    private JCheckBox customPluginToolsScanner;
    private JCheckBox customPluginToolsIntruder;
    private JCheckBox customPluginToolsExtender;
    private JCheckBox customPluginToolsSequencer;
    private JCheckBox customPluginToolsSpider;
    private JCheckBox customPluginScopeCheckBox;
    private JComboBox<String> customPluginExecuteWhenOptions;
    private JTextField customPluginExecuteWhenText;
    private JComboBox<String> customPluginParametersOptions;
    private JTextField customPluginParametersText;
    private JRadioButton customPluginParameterEncodingRadioPlain;
    private JRadioButton customPluginParameterEncodingRadioBase64;
    private JRadioButton customPluginParameterEncodingRadioAsciiHex;
    private JRadioButton customPluginOutputDecodingRadioNone;
    private JRadioButton customPluginOutputDecodingRadioBase64;
    private JRadioButton customPluginOutputDecodingRadioAsciiHex;
    private JComboBox<String> customPluginOutputOptions;
    private JTextField customPluginOutputText;
    private JRadioButton customPluginOutputEncodingRadioNone;
    private JRadioButton customPluginOutputEncodingRadioBase64;
    private JRadioButton customPluginOutputEncodingRadioAsciiHex;
    
    private JComboBox<String> customPluginMessageEditorModifiedOutputLocationOptions;
    private JTextField customPluginMessageEditorModifiedOutputLocationText;
    
    private JPanel androidHooksPanel;
    private JPanel iOSHooksPanel;
    private JPanel genericHooksPanel;
    
    private JTable customPluginsTable;
    		
    /*
     * TODO
     * - Migrate from ASCII HEX to Base64 for defautl hooks?
     * - Android hooks keychain/touchID
     * - Swift demangle?
     * - "Execute method" -> "Run export"
     * - Merge commits
     * - Add hooks/functions
     * - Fix char Python
     * - Search in HEAP
     * - Tab with helps on Brid and on Frida
     * - GUI restyle
     * - Code restyle
     * - Bugfixes
     * - Check Burp 2
     * - Add references to README and update README
     * - Add base address to main view?
     * - Trap by name/address (addressing base address issues)?
     * - Add tab with Frida hooks that can be enabled/disabled (pinning, etc.)
     * - Add addresses to tree view (export and iOS)
     * - Trap/edit return value of custom methods
     * - Organize better JS file (maybe divide custom one from Brida one)
     */
    
    
    public void initializeDefaultHooks() {
    	
    	// Default Android hooks
    	addButtonToHooksAndFunctions(new DefaultHook("SSL Pinning bypass with CA certificate, more reliable (requires CA public certificate in /data/local/tmp/cert-der.crt)",BurpExtender.PLATFORM_ANDROID,"androidpinningwithca1",true,new String[] {},null,false));
    	addButtonToHooksAndFunctions(new DefaultHook("SSL Pinning bypass without CA certificate, less reliable",BurpExtender.PLATFORM_ANDROID,"androidpinningwithoutca1",true,new String[] {},null,false));
    	addButtonToHooksAndFunctions(new DefaultHook("Rooting check bypass",BurpExtender.PLATFORM_ANDROID,"androidrooting1",true,new String[] {},null,false));
    	addButtonToHooksAndFunctions(new DefaultHook("Print keystores when they are opened",BurpExtender.PLATFORM_ANDROID,"androiddumpkeystore1",true,new String[] {},null,false));
    	    	
    	// Custom Android hooks
    	addButtonToHooksAndFunctions(new DefaultHook("Custom Android hook 1",BurpExtender.PLATFORM_ANDROID,"customandroidhook1",true,new String[] {},null,false));
    	addButtonToHooksAndFunctions(new DefaultHook("Custom Android hook 2",BurpExtender.PLATFORM_ANDROID,"customandroidhook2",true,new String[] {},null,false));
    	addButtonToHooksAndFunctions(new DefaultHook("Custom Android hook 3",BurpExtender.PLATFORM_ANDROID,"customandroidhook3",true,new String[] {},null,false));
    	
    	// Default iOS hooks
    	addButtonToHooksAndFunctions(new DefaultHook("SSL Pinning bypass (iOS 10) *",BurpExtender.PLATFORM_IOS,"ios10pinning",true,new String[] {},null,false));
    	addButtonToHooksAndFunctions(new DefaultHook("SSL Pinning bypass (iOS 11) *",BurpExtender.PLATFORM_IOS,"ios11pinning",true,new String[] {},null,false));
    	addButtonToHooksAndFunctions(new DefaultHook("SSL Pinning bypass (iOS 12) *",BurpExtender.PLATFORM_IOS,"ios12pinning",true,new String[] {},null,false));
    	addButtonToHooksAndFunctions(new DefaultHook("Jailbreaking check bypass **",BurpExtender.PLATFORM_IOS,"iosjailbreak",true,new String[] {},null,false));
    	addButtonToHooksAndFunctions(new DefaultHook("Bypass TouchID (click \"Cancel\" when TouchID windows pops up)",BurpExtender.PLATFORM_IOS,"iosbypasstouchid",true,new String[] {},null,false));   	
    	
    	// Custom iOS hooks
    	addButtonToHooksAndFunctions(new DefaultHook("Custom iOS hook 1",BurpExtender.PLATFORM_IOS,"customioshook1",true,new String[] {},null,false));
    	addButtonToHooksAndFunctions(new DefaultHook("Custom iOS hook 2",BurpExtender.PLATFORM_IOS,"customioshook2",true,new String[] {},null,false));
    	addButtonToHooksAndFunctions(new DefaultHook("Custom iOS hook 3",BurpExtender.PLATFORM_IOS,"customioshook3",true,new String[] {},null,false));
    	
    	// Custom generic hooks
    	addButtonToHooksAndFunctions(new DefaultHook("Custom Generic hook 1",BurpExtender.PLATFORM_GENERIC,"customgenenrichook1",true,new String[] {},null,false));
    	addButtonToHooksAndFunctions(new DefaultHook("Custom Generic hook 2",BurpExtender.PLATFORM_GENERIC,"customgenenrichook2",true,new String[] {},null,false));
    	addButtonToHooksAndFunctions(new DefaultHook("Custom Generic hook 3",BurpExtender.PLATFORM_GENERIC,"customgenenrichook3",true,new String[] {},null,false));
    	
    	// Default iOS functions
    	addButtonToHooksAndFunctions(new DefaultHook("Dump keychain",BurpExtender.PLATFORM_IOS,"iosdumpkeychain",false,new String[] {},null,false));
    	addButtonToHooksAndFunctions(new DefaultHook("List files with Data Protection keys",BurpExtender.PLATFORM_IOS,"iosdataprotectionkeys",false,new String[] {},null,false));
    	addButtonToHooksAndFunctions(new DefaultHook("Dump current ENCRYPTED app (downloaded from App Store)",BurpExtender.PLATFORM_IOS,"iosdumpcurrentencryptedapp",false,new String[] {},null,false));
    	    	
    }
    
	public void registerExtenderCallbacks(IBurpExtenderCallbacks c) {
			
		
        // Keep a reference to our callbacks object
        this.callbacks = c;
        
        // Obtain an extension helpers object
        helpers = callbacks.getHelpers();
        
        // Set our extension name
        callbacks.setExtensionName("Brida");
        
        //register to produce options for the context menu
        callbacks.registerContextMenuFactory(this);
        
        // register to execute actions on unload
        callbacks.registerExtensionStateListener(this);
        
        // Initialize stdout and stderr
        stdout = new PrintWriter(callbacks.getStdout(), true);
        stderr = new PrintWriter(callbacks.getStderr(), true); 
        
        stdout.println("Welcome to Brida, the new bridge between Burp Suite and Frida!");
        stdout.println("Created by Piergiovanni Cipolloni and Federico Dotta");
        stdout.println("Contributors: Maurizio Agazzini");
        stdout.println("Version: 0.4");
        stdout.println("");
        stdout.println("Github: https://github.com/federicodotta/Brida");
        stdout.println("");
                
        serverStarted = false;
    	applicationSpawned = false;
    	
    	lastPrintIsJS = false;

    	defaultHooks = new ArrayList<DefaultHook>();
    			
		try {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream("res/bridaServicePyro.py");
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream ));
			File outputFile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "bridaServicePyro.py");
			
			FileWriter fr = new FileWriter(outputFile);
			BufferedWriter br  = new BufferedWriter(fr);
			
			String s;
			while ((s = reader.readLine())!=null) {
				
				br.write(s);
				br.newLine();
				
			}
			reader.close();
			br.close();
			
			pythonScript = outputFile.getAbsolutePath();
			
		} catch(Exception e) {
			
			printException(e,"Error copying Pyro Server file");
			
		}
		       
        SwingUtilities.invokeLater(new Runnable()  {
        	
            @Override
            public void run()  {   	
            	
            	mainPanel = new JPanel();
            	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            	
            	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            	
            	// **** Left panel (tabbed plus console)            	
            	JSplitPane consoleTabbedSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);            	
            	
            	// Tabbed Pabel            	
            	final JTabbedPane tabbedPanel = new JTabbedPane();
            	tabbedPanel.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                       
                        SwingUtilities.invokeLater(new Runnable() {
            				
            	            @Override
            	            public void run() {
            	            	
            	            	showHideButtons(tabbedPanel.getSelectedIndex());
            					
            	            }
            			});	
                        
                    }
                });
            	
            	// **** TABS

            	// **** CONFIGURATION PANEL
            	
            	JPanel configurationConfPanel = new JPanel();
                configurationConfPanel.setLayout(new BoxLayout(configurationConfPanel, BoxLayout.Y_AXIS));
                                
                // RED STYLE
                StyleContext styleContext = new StyleContext();
                redStyle = styleContext.addStyle("red", null);
                StyleConstants.setForeground(redStyle, Color.RED);
                // GREEN STYLE                
                greenStyle = styleContext.addStyle("green", null);
                StyleConstants.setForeground(greenStyle, Color.GREEN);
                                
                JPanel serverStatusPanel = new JPanel();
                serverStatusPanel.setLayout(new BoxLayout(serverStatusPanel, BoxLayout.X_AXIS));
                serverStatusPanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
                JLabel labelServerStatus = new JLabel("Server status: ");
                documentServerStatus = new DefaultStyledDocument();
                serverStatus = new JTextPane(documentServerStatus);                
                try {
                	documentServerStatus.insertString(0, "NOT running", redStyle);
				} catch (BadLocationException e) {
					printException(e,"Error setting labels");
				}
                serverStatus.setMaximumSize( serverStatus.getPreferredSize() );
                serverStatusPanel.add(labelServerStatus);
                serverStatusPanel.add(serverStatus);
                
                JPanel applicationStatusPanel = new JPanel();
                applicationStatusPanel.setLayout(new BoxLayout(applicationStatusPanel, BoxLayout.X_AXIS));
                applicationStatusPanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
                JLabel labelApplicationStatus = new JLabel("Application status: ");
                documentApplicationStatus = new DefaultStyledDocument();
                applicationStatus = new JTextPane(documentApplicationStatus);                      
                try {
                	documentApplicationStatus.insertString(0, "NOT spawned", redStyle);
				} catch (BadLocationException e) {
					printException(e,"Error setting labels");
				}
                applicationStatus.setMaximumSize( applicationStatus.getPreferredSize() );
                applicationStatusPanel.add(labelApplicationStatus);
                applicationStatusPanel.add(applicationStatus);
             
                JPanel pythonPathPanel = new JPanel();
                pythonPathPanel.setLayout(new BoxLayout(pythonPathPanel, BoxLayout.X_AXIS));
                pythonPathPanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
                JLabel labelPythonPath = new JLabel("Python binary path: ");
                pythonPath = new JTextField(200);                
                if(callbacks.loadExtensionSetting("pythonPath") != null)
                	pythonPath.setText(callbacks.loadExtensionSetting("pythonPath"));
                else {
                	if(System.getProperty("os.name").startsWith("Windows")) {
                		pythonPath.setText("C:\\python27\\python");
                	} else {
                		pythonPath.setText("/usr/bin/python");
                	}
                }
                pythonPath.setMaximumSize( pythonPath.getPreferredSize() );
                JButton pythonPathButton = new JButton("Select file");
                pythonPathButton.setActionCommand("pythonPathSelectFile");
                pythonPathButton.addActionListener(BurpExtender.this);
                pythonPathPanel.add(labelPythonPath);
                pythonPathPanel.add(pythonPath);
                pythonPathPanel.add(pythonPathButton);
                                
                JPanel pyroHostPanel = new JPanel();
                pyroHostPanel.setLayout(new BoxLayout(pyroHostPanel, BoxLayout.X_AXIS));
                pyroHostPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel labelPyroHost = new JLabel("Pyro host: ");
                pyroHost = new JTextField(200);                
                if(callbacks.loadExtensionSetting("pyroHost") != null)
                	pyroHost.setText(callbacks.loadExtensionSetting("pyroHost"));
                else
                	pyroHost.setText("localhost");
                pyroHost.setMaximumSize( pyroHost.getPreferredSize() );
                pyroHostPanel.add(labelPyroHost);
                pyroHostPanel.add(pyroHost);
                                
                JPanel pyroPortPanel = new JPanel();
                pyroPortPanel.setLayout(new BoxLayout(pyroPortPanel, BoxLayout.X_AXIS));
                pyroPortPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel labelPyroPort = new JLabel("Pyro port: ");
                pyroPort = new JTextField(200);                
                if(callbacks.loadExtensionSetting("pyroPort") != null)
                	pyroPort.setText(callbacks.loadExtensionSetting("pyroPort"));
                else
                	pyroPort.setText("9999");
                pyroPort.setMaximumSize( pyroPort.getPreferredSize() );
                pyroPortPanel.add(labelPyroPort);
                pyroPortPanel.add(pyroPort);
                                
                JPanel fridaPathPanel = new JPanel();
                fridaPathPanel.setLayout(new BoxLayout(fridaPathPanel, BoxLayout.X_AXIS));
                fridaPathPanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
                JLabel labelFridaPath = new JLabel("Frida JS file path: ");
                fridaPath = new JTextField(200);                
                if(callbacks.loadExtensionSetting("fridaPath") != null)
                	fridaPath.setText(callbacks.loadExtensionSetting("fridaPath"));
                else {                	
                	if(System.getProperty("os.name").startsWith("Windows")) {
                		fridaPath.setText("C:\\burp\\script.js");
                	} else {
                		fridaPath.setText("/opt/burp/script.js");
                	}
                }
                fridaPath.setMaximumSize( fridaPath.getPreferredSize() );
                JButton fridaPathButton = new JButton("Select file");
                fridaPathButton.setActionCommand("fridaPathSelectFile");
                fridaPathButton.addActionListener(BurpExtender.this);
                JButton fridaDefaultPathButton = new JButton("Load default JS file");
                fridaDefaultPathButton.setActionCommand("fridaPathSelectDefaultFile");
                fridaDefaultPathButton.addActionListener(BurpExtender.this);
                fridaPathPanel.add(labelFridaPath);
                fridaPathPanel.add(fridaPath);
                fridaPathPanel.add(fridaPathButton);
                fridaPathPanel.add(fridaDefaultPathButton);
                
                JPanel applicationIdPanel = new JPanel();
                applicationIdPanel.setLayout(new BoxLayout(applicationIdPanel, BoxLayout.X_AXIS));
                applicationIdPanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
                JLabel labelApplicationId = new JLabel("Application ID: ");
                applicationId = new JTextField(200);                
                if(callbacks.loadExtensionSetting("applicationId") != null)
                	applicationId.setText(callbacks.loadExtensionSetting("applicationId"));
                else
                	applicationId.setText("org.test.application");
                applicationId.setMaximumSize( applicationId.getPreferredSize() );
                applicationIdPanel.add(labelApplicationId);
                applicationIdPanel.add(applicationId);
                                
                JPanel localRemotePanel = new JPanel();
                localRemotePanel.setLayout(new BoxLayout(localRemotePanel, BoxLayout.X_AXIS));
                localRemotePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                remoteRadioButton = new JRadioButton("Frida Remote");
                localRadioButton = new JRadioButton("Frida Local");
                if(callbacks.loadExtensionSetting("remote") != null) {                	
                	if(callbacks.loadExtensionSetting("remote").equals("true"))
                		remoteRadioButton.setSelected(true);
                	else
                		localRadioButton.setSelected(true);                	
                } else {
                	remoteRadioButton.setSelected(true);
                }
                ButtonGroup localRemoteButtonGroup = new ButtonGroup();
                localRemoteButtonGroup.add(remoteRadioButton);
                localRemoteButtonGroup.add(localRadioButton);
                localRemotePanel.add(remoteRadioButton);
                localRemotePanel.add(localRadioButton);
            	  
                configurationConfPanel.add(serverStatusPanel);
                configurationConfPanel.add(applicationStatusPanel);
                configurationConfPanel.add(pythonPathPanel);
                configurationConfPanel.add(pyroHostPanel);
                configurationConfPanel.add(pyroPortPanel);
                configurationConfPanel.add(fridaPathPanel);
                configurationConfPanel.add(applicationIdPanel);  
                configurationConfPanel.add(localRemotePanel);
                
                // **** END CONFIGURATION PANEL
                
            	// **** JS EDITOR PANEL / CONSOLE
                jsEditorTextArea = new TextEditorPane();
                jsEditorTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
                jsEditorTextArea.setCodeFoldingEnabled(false);   
                RTextScrollPane sp = new RTextScrollPane(jsEditorTextArea);
                jsEditorTextArea.setFocusable(true);                
                // **** END JS EDITOR PANEL / CONSOLE    
                
                // 	*** TREE WITH CLASSES AND METHODS
                
                JPanel treeSearchPanel = new JPanel();
                treeSearchPanel.setLayout(new BorderLayout());  
                                
                JPanel treePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JScrollPane scrollTreeJPanel = new JScrollPane(treePanel);
                scrollTreeJPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                
                DefaultMutableTreeNode top = new DefaultMutableTreeNode("Binary");
                
                tree = new JTree(top);
                
                // Add mouse listener
                tree.addMouseListener(BurpExtender.this);
                
                treePanel.add(tree);
                                
                JPanel searchPanelBar = new JPanel();
                searchPanelBar.setLayout(new BoxLayout(searchPanelBar, BoxLayout.X_AXIS));
                                
                JLabel findLabel = new JLabel("Search:");
                //findTextField = new JTextField(60);       
                findTextField = new JTextField();
                JButton searchButton = new JButton("Search");
                searchButton.setActionCommand("searchAnalysis");
                searchButton.addActionListener(BurpExtender.this); 
                
                searchPanelBar.add(findLabel);
                searchPanelBar.add(findTextField);
                searchPanelBar.add(searchButton);
             
                treeSearchPanel.add(scrollTreeJPanel);
                treeSearchPanel.add(searchPanelBar,BorderLayout.SOUTH);
                
                // *** TREE WITH CLASSES AND METHODS                
                
            	// **** STUB GENERATION     
                                
                stubTextEditor = callbacks.createTextEditor();                
                stubTextEditor.setEditable(false);
                
            	// **** END STUB GENERATION  
                
                // **** EXECUTE METHOD TAB
                
                // Execute method
                JPanel executeMethodPanel = new JPanel();
                executeMethodPanel.setLayout(new BoxLayout(executeMethodPanel, BoxLayout.Y_AXIS));
                
                JPanel executeMethodNamePanel = new JPanel();
                executeMethodNamePanel.setLayout(new BoxLayout(executeMethodNamePanel, BoxLayout.X_AXIS));
                executeMethodNamePanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
                JLabel labelExecuteMethodName = new JLabel("Method name: ");
                executeMethodName = new JTextField(200);                
                if(callbacks.loadExtensionSetting("executeMethodName") != null)
                	executeMethodName.setText(callbacks.loadExtensionSetting("executeMethodName"));
                executeMethodName.setMaximumSize( executeMethodName.getPreferredSize() );
                executeMethodNamePanel.add(labelExecuteMethodName);
                executeMethodNamePanel.add(executeMethodName);

                JPanel executeMethodArgumentPanel = new JPanel();
                executeMethodArgumentPanel.setLayout(new BoxLayout(executeMethodArgumentPanel, BoxLayout.X_AXIS));
                executeMethodArgumentPanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
                JLabel labelExecuteMethodArgument = new JLabel("Argument: ");
                executeMethodArgument = new JTextField(200);                
                executeMethodArgument.setMaximumSize( executeMethodArgument.getPreferredSize() );
                JButton addExecuteMethodArgument = new JButton("Add");
                addExecuteMethodArgument.setActionCommand("addExecuteMethodArgument");
                addExecuteMethodArgument.addActionListener(BurpExtender.this);
                executeMethodArgumentPanel.add(labelExecuteMethodArgument);
                executeMethodArgumentPanel.add(executeMethodArgument);
                executeMethodArgumentPanel.add(addExecuteMethodArgument);
                            
                executeMethodInsertedArgumentList = new DefaultListModel();                
                JPanel executeMethodInsertedArgumentPanel = new JPanel();
                executeMethodInsertedArgumentPanel.setLayout(new BoxLayout(executeMethodInsertedArgumentPanel, BoxLayout.X_AXIS));
                executeMethodInsertedArgumentPanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
                JLabel labelExecuteMethodInsertedArgument = new JLabel("Argument list: ");
                executeMethodInsertedArgument = new JList(executeMethodInsertedArgumentList);    
                JScrollPane executeMethodInsertedArgumentScrollPane = new JScrollPane(executeMethodInsertedArgument);
                executeMethodInsertedArgumentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                executeMethodInsertedArgumentScrollPane.setBorder(new LineBorder(Color.BLACK));
                executeMethodInsertedArgumentScrollPane.setMaximumSize( executeMethodInsertedArgumentScrollPane.getPreferredSize() ); 
                if(callbacks.loadExtensionSetting("executeMethodSizeArguments") != null) {
                	int sizeArguments = Integer.parseInt(callbacks.loadExtensionSetting("executeMethodSizeArguments"));
                	for(int i=0;i<sizeArguments;i++) {
                		executeMethodInsertedArgumentList.addElement(callbacks.loadExtensionSetting("executeMethodArgument" + i));
                	}
                }
                               
                JPanel executeMethodInsertedArgumentButtonPanel = new JPanel();
                executeMethodInsertedArgumentButtonPanel.setLayout(new BoxLayout(executeMethodInsertedArgumentButtonPanel, BoxLayout.Y_AXIS));
                JButton removeExecuteMethodArgument = new JButton("Remove");
                removeExecuteMethodArgument.setActionCommand("removeExecuteMethodArgument");
                removeExecuteMethodArgument.addActionListener(BurpExtender.this);
                JButton modifyExecuteMethodArgument = new JButton("Modify");
                modifyExecuteMethodArgument.setActionCommand("modifyExecuteMethodArgument");
                modifyExecuteMethodArgument.addActionListener(BurpExtender.this);
                executeMethodInsertedArgumentButtonPanel.add(removeExecuteMethodArgument);
                executeMethodInsertedArgumentButtonPanel.add(modifyExecuteMethodArgument);                
                executeMethodInsertedArgumentPanel.add(labelExecuteMethodInsertedArgument);
                executeMethodInsertedArgumentPanel.add(executeMethodInsertedArgumentScrollPane);
                executeMethodInsertedArgumentPanel.add(executeMethodInsertedArgumentButtonPanel);
                
                executeMethodPanel.add(executeMethodNamePanel);
                executeMethodPanel.add(executeMethodArgumentPanel);
                executeMethodPanel.add(executeMethodInsertedArgumentPanel);
                
                // **** END EXECUTE METHOD TAB
                
                // **** BEGIN TRAPPING TAB
                
                trapTable = new JTable(new TrapTableModel());
                JScrollPane trapTableScrollPane = new JScrollPane(trapTable);
                trapTableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                trapTableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                trapTable.setAutoCreateRowSorter(true);
                
                // Center header
                ((DefaultTableCellRenderer)trapTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
                
                // Center columns 4 and 5
                DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                centerRenderer.setHorizontalAlignment( JLabel.CENTER );
                trapTable.getColumnModel().getColumn(4).setCellRenderer( centerRenderer );
                trapTable.getColumnModel().getColumn(5).setCellRenderer( centerRenderer );
                
                // **** END TRAPPING TAB
                
                
                // **** FRIDA DEFAULT HOOKS TAB
                final JTabbedPane tabbedPanelHooks = new JTabbedPane();
                
                androidHooksPanel = new JPanel();
                androidHooksPanel.setLayout(new BoxLayout(androidHooksPanel, BoxLayout.Y_AXIS));
                
                iOSHooksPanel = new JPanel();
                iOSHooksPanel.setLayout(new BoxLayout(iOSHooksPanel, BoxLayout.Y_AXIS));                
                
                genericHooksPanel = new JPanel();
                genericHooksPanel.setLayout(new BoxLayout(genericHooksPanel, BoxLayout.Y_AXIS));  
                
                // Initialize default hooks
                initializeDefaultHooks();
                
                // Add tips to iOS hooks tab
                JPanel iosTipsJPanel = new JPanel();
                iosTipsJPanel.setLayout(new BoxLayout(iosTipsJPanel, BoxLayout.Y_AXIS));
                JLabel iosTip1Label = new JLabel("* TIP: If SSL pinning escape does not work try \"SSL Kill Switch 2\" application!");
                JLabel iosTip2Label = new JLabel("** TIP: If Jailbreak escape does not work try \"TS Protector\" or \"Liberty Lite\" applications!");
                Font fontJLabel = iosTip1Label.getFont();
                iosTip1Label.setFont(fontJLabel.deriveFont(fontJLabel.getStyle() | Font.BOLD));
                iosTip2Label.setFont(fontJLabel.deriveFont(fontJLabel.getStyle() | Font.BOLD));
                iosTipsJPanel.add(iosTip1Label);
                iosTipsJPanel.add(iosTip2Label);                
                JPanel iOSHooksPanelWithTips = new JPanel();
                iOSHooksPanelWithTips.setLayout(new BorderLayout());
                iOSHooksPanelWithTips.add(iOSHooksPanel);
                iOSHooksPanelWithTips.add(iosTipsJPanel,BorderLayout.SOUTH);
               
                tabbedPanelHooks.add("Android",androidHooksPanel);
                tabbedPanelHooks.add("iOS",iOSHooksPanelWithTips);
                tabbedPanelHooks.add("Generic",genericHooksPanel);
                // **** END FRIDA DEFAULT HOOKS TAB    
                
                
                // **** BEGIN CUSTOM PLUGINS
                JPanel customPluginPanel = new JPanel();
                customPluginPanel.setLayout(new BoxLayout(customPluginPanel, BoxLayout.Y_AXIS));
                
                JPanel customPluginNamePanel = new JPanel();
                customPluginNamePanel.setLayout(new BoxLayout(customPluginNamePanel, BoxLayout.X_AXIS));
                customPluginNamePanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
                JLabel customPluginNameLabel = new JLabel("Plugin name: ");
                customPluginNameText = new JTextField(200);                
                customPluginNameText.setMaximumSize( customPluginNameText.getPreferredSize() );
                customPluginNamePanel.add(customPluginNameLabel);
                customPluginNamePanel.add(customPluginNameText);
                
                JPanel customPluginTypePluginPanel = new JPanel();
                customPluginTypePluginPanel.setLayout(new BoxLayout(customPluginTypePluginPanel, BoxLayout.X_AXIS));
                customPluginTypePluginPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginTypePluginLabel = new JLabel("Plugin Type ");                
                String[] customPluginTypePluginComboOptions = new String[] {"IHttpListener", "IMessageEditorTab","IContextMenu","JButton"};
                customPluginTypePluginOptions = new JComboBox<String>(customPluginTypePluginComboOptions);
                customPluginTypePluginOptions.setSelectedIndex(0);
                customPluginTypePluginOptions.setMaximumSize( customPluginTypePluginOptions.getPreferredSize() );  
                customPluginTypePluginOptions.addActionListener (new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                    	changeCustomPluginOptions(customPluginTypePluginOptions.getSelectedItem().toString());
                    }
                });
                customPluginTypePluginDescription = new JLabel("Plugin that dynamically process each requests and responses");                 
                customPluginTypePluginDescription.setMaximumSize( customPluginTypePluginDescription.getPreferredSize() );
                customPluginTypePluginPanel.add(customPluginTypePluginLabel);
                customPluginTypePluginPanel.add(customPluginTypePluginOptions);
                customPluginTypePluginPanel.add(customPluginTypePluginDescription);
                
                JPanel customPluginExportNamePanel = new JPanel();
                customPluginExportNamePanel.setLayout(new BoxLayout(customPluginExportNamePanel, BoxLayout.X_AXIS));
                customPluginExportNamePanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
                JLabel customPluginExportNameLabel = new JLabel("Name of the Frida exported function (*): ");
                customPluginExportNameText = new JTextField(200);                
                customPluginExportNameText.setMaximumSize( customPluginExportNameText.getPreferredSize() );
                customPluginExportNamePanel.add(customPluginExportNameLabel);
                customPluginExportNamePanel.add(customPluginExportNameText);
                
                JPanel customPluginExecuteOnPanel = new JPanel();
                customPluginExecuteOnPanel.setLayout(new BoxLayout(customPluginExecuteOnPanel, BoxLayout.X_AXIS));
                customPluginExecuteOnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginExecuteOnLabel = new JLabel("Execute on: ");                
                customPluginExecuteOnRadioRequest = new JRadioButton("Requests");
                customPluginExecuteOnRadioResponse = new JRadioButton("Responses");
                customPluginExecuteOnRadioAll = new JRadioButton("All");
                customPluginExecuteOnRadioContext = new JRadioButton("Context menu options named: ");
                customPluginExecuteOnRadioContext.setVisible(false);
                customPluginExecuteOnRadioButton = new JRadioButton("Button named: ");
                customPluginExecuteOnRadioButton.setVisible(false);
                customPluginExecuteOnRadioRequest.setSelected(true);     
                customPluginExecuteOnStringParameter = new JTextField(200);    
                customPluginExecuteOnStringParameter.setMaximumSize( customPluginExecuteOnStringParameter.getPreferredSize() );
                customPluginExecuteOnStringParameter.setVisible(false);
                ButtonGroup customPluginExecuteOnRadioButtonGroup = new ButtonGroup();
                customPluginExecuteOnRadioButtonGroup.add(customPluginExecuteOnRadioRequest);
                customPluginExecuteOnRadioButtonGroup.add(customPluginExecuteOnRadioResponse);
                customPluginExecuteOnRadioButtonGroup.add(customPluginExecuteOnRadioAll);
                customPluginExecuteOnRadioButtonGroup.add(customPluginExecuteOnRadioContext);
                customPluginExecuteOnRadioButtonGroup.add(customPluginExecuteOnRadioButton);
                customPluginExecuteOnPanel.add(customPluginExecuteOnLabel);
                customPluginExecuteOnPanel.add(customPluginExecuteOnRadioRequest);
                customPluginExecuteOnPanel.add(customPluginExecuteOnRadioResponse);
                customPluginExecuteOnPanel.add(customPluginExecuteOnRadioAll);
                customPluginExecuteOnPanel.add(customPluginExecuteOnRadioContext);
                customPluginExecuteOnPanel.add(customPluginExecuteOnRadioButton);
                customPluginExecuteOnPanel.add(customPluginExecuteOnStringParameter);
                
                customPluginButtonPlatformPanel = new JPanel();
                customPluginButtonPlatformPanel.setLayout(new BoxLayout(customPluginButtonPlatformPanel, BoxLayout.X_AXIS));
                customPluginButtonPlatformPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginButtonPlatformLabel = new JLabel("Platform: ");                
                customPluginButtonTypeRadioIos = new JRadioButton("iOS");
                customPluginButtonTypeRadioAndroid = new JRadioButton("Android");
                customPluginButtonTypeRadioGeneric = new JRadioButton("Generic");
                customPluginButtonTypeRadioIos.setSelected(true);     
                ButtonGroup customPluginButtonPlatformRadioButtonGroup = new ButtonGroup();
                customPluginButtonPlatformRadioButtonGroup.add(customPluginButtonTypeRadioIos);
                customPluginButtonPlatformRadioButtonGroup.add(customPluginButtonTypeRadioAndroid);
                customPluginButtonPlatformRadioButtonGroup.add(customPluginButtonTypeRadioGeneric);
                customPluginButtonPlatformPanel.add(customPluginButtonPlatformLabel);
                customPluginButtonPlatformPanel.add(customPluginButtonTypeRadioIos);
                customPluginButtonPlatformPanel.add(customPluginButtonTypeRadioAndroid);
                customPluginButtonPlatformPanel.add(customPluginButtonTypeRadioGeneric);
                customPluginButtonPlatformPanel.setVisible(false);
                
                customPluginButtonTypePanel = new JPanel();
                customPluginButtonTypePanel.setLayout(new BoxLayout(customPluginButtonTypePanel, BoxLayout.X_AXIS));
                customPluginButtonTypePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginButtonTypeLabel = new JLabel("Button type: ");                
                customPluginButtonTypeRadioFunction = new JRadioButton("Function");
                customPluginButtonTypeRadioHook = new JRadioButton("Hook");
                customPluginButtonTypeRadioFunction.setSelected(true);     
                ButtonGroup customPluginButtonTypeRadioButtonGroup = new ButtonGroup();
                customPluginButtonTypeRadioButtonGroup.add(customPluginButtonTypeRadioFunction);
                customPluginButtonTypeRadioButtonGroup.add(customPluginButtonTypeRadioHook);
                customPluginButtonTypeRadioFunction.addActionListener(new ActionListener() {
                	@Override
                    public void actionPerformed(ActionEvent e) {
                		SwingUtilities.invokeLater(new Runnable() {                			
                            @Override
                            public void run() {
		                		customPluginParametersPanel.setVisible(true);
		                		customPluginParameterEncodingPanel.setVisible(true);
                            }
                		});
                	}
                });
                customPluginButtonTypeRadioHook.addActionListener(new ActionListener() {
                	@Override
                    public void actionPerformed(ActionEvent e) {
                		SwingUtilities.invokeLater(new Runnable() {                			
                            @Override
                            public void run() {
		                		customPluginParametersPanel.setVisible(false);
		                		customPluginParameterEncodingPanel.setVisible(false);
                            }
                		});
                	}
                });
                customPluginButtonTypePanel.add(customPluginButtonTypeLabel);
                customPluginButtonTypePanel.add(customPluginButtonTypeRadioFunction);
                customPluginButtonTypePanel.add(customPluginButtonTypeRadioHook);
                customPluginButtonTypePanel.setVisible(false);
                
                customPluginToolsPanel = new JPanel();
                customPluginToolsPanel.setLayout(new BoxLayout(customPluginToolsPanel, BoxLayout.X_AXIS));
                customPluginToolsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginToolsLabel = new JLabel("Burp Suite Tools: ");                
                customPluginToolsRepeater = new JCheckBox("Repeater",true);
                customPluginToolsProxy = new JCheckBox("Proxy",false);
                customPluginToolsScanner = new JCheckBox("Scanner",false);
                customPluginToolsIntruder = new JCheckBox("Intruder",false);
                customPluginToolsExtender = new JCheckBox("Extender",false);
                customPluginToolsSequencer = new JCheckBox("Sequencer",false);
                customPluginToolsSpider = new JCheckBox("Spider",false);
                customPluginToolsPanel.add(customPluginToolsLabel);
                customPluginToolsPanel.add(customPluginToolsRepeater);
                customPluginToolsPanel.add(customPluginToolsProxy);
                customPluginToolsPanel.add(customPluginToolsScanner);
                customPluginToolsPanel.add(customPluginToolsIntruder);
                customPluginToolsPanel.add(customPluginToolsExtender);
                customPluginToolsPanel.add(customPluginToolsSequencer);
                customPluginToolsPanel.add(customPluginToolsSpider);
                
                customPluginScopePanel = new JPanel();
                customPluginScopePanel.setLayout(new BoxLayout(customPluginScopePanel, BoxLayout.X_AXIS));
                customPluginScopePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginScopeLabel = new JLabel("Process only in-scope requests/responses: ");                
                customPluginScopeCheckBox = new JCheckBox();
                customPluginScopePanel.add(customPluginScopeLabel);
                customPluginScopePanel.add(customPluginScopeCheckBox);
                                
                customPluginExecuteWhenPanel = new JPanel();
                customPluginExecuteWhenPanel.setLayout(new BoxLayout(customPluginExecuteWhenPanel, BoxLayout.X_AXIS));
                customPluginExecuteWhenPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginExecuteWhenLabel = new JLabel("Execute ");                
                String[] customPluginExecuteJComboOptions = new String[] {"always", "when request/response contains plaintext","when request/response contains regex"};
                customPluginExecuteWhenOptions = new JComboBox<String>(customPluginExecuteJComboOptions);
                customPluginExecuteWhenOptions.setSelectedIndex(0);
                customPluginExecuteWhenOptions.setMaximumSize( customPluginExecuteWhenOptions.getPreferredSize() );
                customPluginExecuteWhenText = new JTextField(200);                
                customPluginExecuteWhenText.setMaximumSize( customPluginExecuteWhenText.getPreferredSize() );
                customPluginExecuteWhenPanel.add(customPluginExecuteWhenLabel);
                customPluginExecuteWhenPanel.add(customPluginExecuteWhenOptions);
                customPluginExecuteWhenPanel.add(customPluginExecuteWhenText);
                
                customPluginParametersPanel = new JPanel();
                customPluginParametersPanel.setLayout(new BoxLayout(customPluginParametersPanel, BoxLayout.X_AXIS));
                customPluginParametersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginParametersLabel = new JLabel("Parameters: ");                
                String[] customPluginParametersComboOptions = new String[] {"none", "complete request/response","headers","body","regex (with parenthesis)","fixed (#,# as separator)","ask to user with popup (#,# as separator)"};
                customPluginParametersOptions = new JComboBox<String>(customPluginParametersComboOptions);
                customPluginParametersOptions.setSelectedIndex(0);
                customPluginParametersOptions.setMaximumSize( customPluginParametersOptions.getPreferredSize() );
                customPluginParametersText = new JTextField(200);                
                customPluginParametersText.setMaximumSize( customPluginParametersText.getPreferredSize() );
                customPluginParametersPanel.add(customPluginParametersLabel);
                customPluginParametersPanel.add(customPluginParametersOptions);
                customPluginParametersPanel.add(customPluginParametersText);
                
                customPluginParameterEncodingPanel = new JPanel();
                customPluginParameterEncodingPanel.setLayout(new BoxLayout(customPluginParameterEncodingPanel, BoxLayout.X_AXIS));
                customPluginParameterEncodingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginParameterEncodinglabel = new JLabel("Encode function parameters: ");   
                customPluginParameterEncodingRadioPlain = new JRadioButton("Plain (not advisable with non-ASCII parameters)");
                customPluginParameterEncodingRadioBase64 = new JRadioButton("Base64");
                customPluginParameterEncodingRadioAsciiHex = new JRadioButton("Ascii-Hex");
                customPluginParameterEncodingRadioPlain.setSelected(true);                
                ButtonGroup customPluginParameterEncodingRadioGroup = new ButtonGroup();
                customPluginParameterEncodingRadioGroup.add(customPluginParameterEncodingRadioPlain);
                customPluginParameterEncodingRadioGroup.add(customPluginParameterEncodingRadioBase64);
                customPluginParameterEncodingRadioGroup.add(customPluginParameterEncodingRadioAsciiHex);
                customPluginParameterEncodingPanel.add(customPluginParameterEncodinglabel);
                customPluginParameterEncodingPanel.add(customPluginParameterEncodingRadioPlain);
                customPluginParameterEncodingPanel.add(customPluginParameterEncodingRadioBase64);
                customPluginParameterEncodingPanel.add(customPluginParameterEncodingRadioAsciiHex);
                
                customPluginOutputDecodingPanel = new JPanel();
                customPluginOutputDecodingPanel.setLayout(new BoxLayout(customPluginOutputDecodingPanel, BoxLayout.X_AXIS));
                customPluginOutputDecodingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginOutputDecodingLabel = new JLabel("Decode function output: ");                
                customPluginOutputDecodingRadioNone = new JRadioButton("None");
                customPluginOutputDecodingRadioBase64 = new JRadioButton("Base64");
                customPluginOutputDecodingRadioAsciiHex = new JRadioButton("Ascii-Hex");
                customPluginOutputDecodingRadioNone.setSelected(true);                
                ButtonGroup customPluginOutputDecodingRadioGroup = new ButtonGroup();
                customPluginOutputDecodingRadioGroup.add(customPluginOutputDecodingRadioNone);
                customPluginOutputDecodingRadioGroup.add(customPluginOutputDecodingRadioBase64);
                customPluginOutputDecodingRadioGroup.add(customPluginOutputDecodingRadioAsciiHex);
                customPluginOutputDecodingPanel.add(customPluginOutputDecodingLabel);
                customPluginOutputDecodingPanel.add(customPluginOutputDecodingRadioNone);
                customPluginOutputDecodingPanel.add(customPluginOutputDecodingRadioBase64);
                customPluginOutputDecodingPanel.add(customPluginOutputDecodingRadioAsciiHex);
                
                JPanel customPluginOutputPanel = new JPanel();
                customPluginOutputPanel.setLayout(new BoxLayout(customPluginOutputPanel, BoxLayout.X_AXIS));
                customPluginOutputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginOutputLabel = new JLabel("Plugin output: ");                
                String[] customPluginOutputComboOptions = new String[] {"print in Brida console","replace in request/response with regex (with parenthesys)"};
                customPluginOutputOptions = new JComboBox<String>(customPluginOutputComboOptions);
                customPluginOutputOptions.setSelectedIndex(0);
                customPluginOutputOptions.setMaximumSize( customPluginOutputOptions.getPreferredSize() );
                customPluginOutputText = new JTextField(200);                
                customPluginOutputText.setMaximumSize( customPluginOutputText.getPreferredSize() );
                customPluginOutputPanel.add(customPluginOutputLabel);
                customPluginOutputPanel.add(customPluginOutputOptions);
                customPluginOutputPanel.add(customPluginOutputText);
                
                customPluginOutputEncodingPanel = new JPanel();
                customPluginOutputEncodingPanel.setLayout(new BoxLayout(customPluginOutputEncodingPanel, BoxLayout.X_AXIS));
                customPluginOutputEncodingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginOutputEncodingLabel = new JLabel("Plugin output encoding: ");                
                customPluginOutputEncodingRadioNone = new JRadioButton("None");
                customPluginOutputEncodingRadioBase64 = new JRadioButton("Base64");
                customPluginOutputEncodingRadioAsciiHex = new JRadioButton("Ascii-Hex");
                customPluginOutputEncodingRadioNone.setSelected(true);                
                ButtonGroup customPluginOutputEncodingRadioGroup = new ButtonGroup();
                customPluginOutputEncodingRadioGroup.add(customPluginOutputEncodingRadioNone);
                customPluginOutputEncodingRadioGroup.add(customPluginOutputEncodingRadioBase64);
                customPluginOutputEncodingRadioGroup.add(customPluginOutputEncodingRadioAsciiHex);
                customPluginOutputEncodingPanel.add(customPluginOutputEncodingLabel);
                customPluginOutputEncodingPanel.add(customPluginOutputEncodingRadioNone);
                customPluginOutputEncodingPanel.add(customPluginOutputEncodingRadioBase64);
                customPluginOutputEncodingPanel.add(customPluginOutputEncodingRadioAsciiHex); 
                
                customPluginMessageEditorModifiedFridaFunctioPanel = new JPanel();
                customPluginMessageEditorModifiedFridaFunctioPanel.setLayout(new BoxLayout(customPluginMessageEditorModifiedFridaFunctioPanel, BoxLayout.X_AXIS));
                customPluginMessageEditorModifiedFridaFunctioPanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
                JLabel customPluginMessageEditorModifiedFridaExportNameLabel = new JLabel("Name of the Frida exported function for the edited content (*): ");
                customPluginMessageEditorModifiedFridaExportNameText = new JTextField(200);                
                customPluginMessageEditorModifiedFridaExportNameText.setMaximumSize( customPluginMessageEditorModifiedFridaExportNameText.getPreferredSize() );
                customPluginMessageEditorModifiedFridaFunctioPanel.add(customPluginMessageEditorModifiedFridaExportNameLabel);
                customPluginMessageEditorModifiedFridaFunctioPanel.add(customPluginMessageEditorModifiedFridaExportNameText);
                customPluginMessageEditorModifiedFridaFunctioPanel.setVisible(false);
                
                customPluginMessageEditorModifiedEncodeInputPanel = new JPanel();
                customPluginMessageEditorModifiedEncodeInputPanel.setLayout(new BoxLayout(customPluginMessageEditorModifiedEncodeInputPanel, BoxLayout.X_AXIS));
                customPluginMessageEditorModifiedEncodeInputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginMessageEditorModifiedEncodeInputLabel = new JLabel("Encode input passed to Frida function executed on edited content: ");                
                customPluginMessageEditorModifiedEncodingInputFridaRadioNone = new JRadioButton("None");
                customPluginMessageEditorModifiedEncodingInputFridaRadioBase64 = new JRadioButton("Base64");
                customPluginMessageEditorModifiedEncodingInputFridaRadioAsciiHex = new JRadioButton("Ascii-Hex");
                customPluginMessageEditorModifiedEncodingInputFridaRadioNone.setSelected(true);                
                ButtonGroup customPluginMessageEditorModifiedEncodingInputFridaRadioGroup = new ButtonGroup();
                customPluginMessageEditorModifiedEncodingInputFridaRadioGroup.add(customPluginMessageEditorModifiedEncodingInputFridaRadioNone);
                customPluginMessageEditorModifiedEncodingInputFridaRadioGroup.add(customPluginMessageEditorModifiedEncodingInputFridaRadioBase64);
                customPluginMessageEditorModifiedEncodingInputFridaRadioGroup.add(customPluginMessageEditorModifiedEncodingInputFridaRadioAsciiHex);
                customPluginMessageEditorModifiedEncodeInputPanel.add(customPluginMessageEditorModifiedEncodeInputLabel);
                customPluginMessageEditorModifiedEncodeInputPanel.add(customPluginMessageEditorModifiedEncodingInputFridaRadioNone);
                customPluginMessageEditorModifiedEncodeInputPanel.add(customPluginMessageEditorModifiedEncodingInputFridaRadioBase64);
                customPluginMessageEditorModifiedEncodeInputPanel.add(customPluginMessageEditorModifiedEncodingInputFridaRadioAsciiHex);
                customPluginMessageEditorModifiedEncodeInputPanel.setVisible(false);
                
                customPluginMessageEditorModifiedDecodingOutputPanel = new JPanel();
                customPluginMessageEditorModifiedDecodingOutputPanel.setLayout(new BoxLayout(customPluginMessageEditorModifiedDecodingOutputPanel, BoxLayout.X_AXIS));
                customPluginMessageEditorModifiedDecodingOutputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginMessageEditorModifiedDecodingOutputLabel = new JLabel("Decode output of Frida function executed on edited content: ");                
                customPluginOutputMessageEditorModifiedDecodingRadioNone = new JRadioButton("None");
                customPluginOutputMessageEditorModifiedDecodingRadioBase64 = new JRadioButton("Base64");
                customPluginOutputMessageEditorModifiedDecodingRadioAsciiHex = new JRadioButton("Ascii-Hex");
                customPluginOutputMessageEditorModifiedDecodingRadioNone.setSelected(true);                
                ButtonGroup customPluginOutputMessageEditorModifiedDecodingRadioGroup = new ButtonGroup();
                customPluginOutputMessageEditorModifiedDecodingRadioGroup.add(customPluginOutputMessageEditorModifiedDecodingRadioNone);
                customPluginOutputMessageEditorModifiedDecodingRadioGroup.add(customPluginOutputMessageEditorModifiedDecodingRadioBase64);
                customPluginOutputMessageEditorModifiedDecodingRadioGroup.add(customPluginOutputMessageEditorModifiedDecodingRadioAsciiHex);
                customPluginMessageEditorModifiedDecodingOutputPanel.add(customPluginMessageEditorModifiedDecodingOutputLabel);
                customPluginMessageEditorModifiedDecodingOutputPanel.add(customPluginOutputMessageEditorModifiedDecodingRadioNone);
                customPluginMessageEditorModifiedDecodingOutputPanel.add(customPluginOutputMessageEditorModifiedDecodingRadioBase64);
                customPluginMessageEditorModifiedDecodingOutputPanel.add(customPluginOutputMessageEditorModifiedDecodingRadioAsciiHex);
                customPluginMessageEditorModifiedDecodingOutputPanel.setVisible(false);
                
                customPluginMessageEditorModifiedOutputLocationPanel = new JPanel();
                customPluginMessageEditorModifiedOutputLocationPanel.setLayout(new BoxLayout(customPluginMessageEditorModifiedOutputLocationPanel, BoxLayout.X_AXIS));
                customPluginMessageEditorModifiedOutputLocationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginMessageEditorModifiedOutputLocationLabel = new JLabel("Edited content location: ");                
                String[] customPluginMessageEditorModifiedOutputLocationComboOptions = new String[] {"Discard (view only mode)","Print in Brida console and return original request/response","Replace complete request/response","Replace request/response body","Regex (with parenthesys)"};
                customPluginMessageEditorModifiedOutputLocationOptions = new JComboBox<String>(customPluginMessageEditorModifiedOutputLocationComboOptions);
                customPluginMessageEditorModifiedOutputLocationOptions.setSelectedIndex(0);
                customPluginMessageEditorModifiedOutputLocationOptions.setMaximumSize( customPluginMessageEditorModifiedOutputLocationOptions.getPreferredSize() );
                customPluginMessageEditorModifiedOutputLocationText = new JTextField(200);                
                customPluginMessageEditorModifiedOutputLocationText.setMaximumSize( customPluginMessageEditorModifiedOutputLocationText.getPreferredSize() );
                customPluginMessageEditorModifiedOutputLocationPanel.add(customPluginMessageEditorModifiedOutputLocationLabel);
                customPluginMessageEditorModifiedOutputLocationPanel.add(customPluginMessageEditorModifiedOutputLocationOptions);
                customPluginMessageEditorModifiedOutputLocationPanel.add(customPluginMessageEditorModifiedOutputLocationText);
                customPluginMessageEditorModifiedOutputLocationPanel.setVisible(false);
                
                customPluginMessageEditorModifiedOutputEncodingPanel = new JPanel();
                customPluginMessageEditorModifiedOutputEncodingPanel.setLayout(new BoxLayout(customPluginMessageEditorModifiedOutputEncodingPanel, BoxLayout.X_AXIS));
                customPluginMessageEditorModifiedOutputEncodingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel customPluginMessageEditorModifiedOutputEncodingLabel = new JLabel("Encode output of edited content function: ");                
                customPluginMessageEditorModifiedOutputEncodingRadioNone = new JRadioButton("None");
                customPluginMessageEditorModifiedOutputEncodingRadioBase64 = new JRadioButton("Base64");
                customPluginMessageEditorModifiedOutputEncodingRadioAsciiHex = new JRadioButton("Ascii-Hex");
                customPluginMessageEditorModifiedOutputEncodingRadioNone.setSelected(true);                
                ButtonGroup customPluginMessageEditorModifiedOutputEncodingRadioGroup = new ButtonGroup();
                customPluginMessageEditorModifiedOutputEncodingRadioGroup.add(customPluginMessageEditorModifiedOutputEncodingRadioNone);
                customPluginMessageEditorModifiedOutputEncodingRadioGroup.add(customPluginMessageEditorModifiedOutputEncodingRadioBase64);
                customPluginMessageEditorModifiedOutputEncodingRadioGroup.add(customPluginMessageEditorModifiedOutputEncodingRadioAsciiHex);
                customPluginMessageEditorModifiedOutputEncodingPanel.add(customPluginMessageEditorModifiedOutputEncodingLabel);
                customPluginMessageEditorModifiedOutputEncodingPanel.add(customPluginMessageEditorModifiedOutputEncodingRadioNone);
                customPluginMessageEditorModifiedOutputEncodingPanel.add(customPluginMessageEditorModifiedOutputEncodingRadioBase64);
                customPluginMessageEditorModifiedOutputEncodingPanel.add(customPluginMessageEditorModifiedOutputEncodingRadioAsciiHex);     
                customPluginMessageEditorModifiedOutputEncodingPanel.setVisible(false);
                
                customPluginPanel.add(customPluginNamePanel);
                customPluginPanel.add(customPluginTypePluginPanel);
                customPluginPanel.add(customPluginExportNamePanel);
                customPluginPanel.add(customPluginExecuteOnPanel);
                customPluginPanel.add(customPluginButtonPlatformPanel);
                customPluginPanel.add(customPluginButtonTypePanel);
                customPluginPanel.add(customPluginToolsPanel);
                customPluginPanel.add(customPluginScopePanel);
                customPluginPanel.add(customPluginExecuteWhenPanel);
                customPluginPanel.add(customPluginParametersPanel);
                customPluginPanel.add(customPluginParameterEncodingPanel);
                customPluginPanel.add(customPluginOutputDecodingPanel);
                customPluginPanel.add(customPluginOutputPanel);
                customPluginPanel.add(customPluginOutputEncodingPanel);
                customPluginPanel.add(customPluginMessageEditorModifiedFridaFunctioPanel);
                customPluginPanel.add(customPluginMessageEditorModifiedEncodeInputPanel);
                customPluginPanel.add(customPluginMessageEditorModifiedDecodingOutputPanel);
                customPluginPanel.add(customPluginMessageEditorModifiedOutputLocationPanel);
                customPluginPanel.add(customPluginMessageEditorModifiedOutputEncodingPanel);
                                                
                customPluginsTable = new JTable(new CustomPluginsTableModel());
                JScrollPane customPluginsTableScrollPane = new JScrollPane(customPluginsTable);
                customPluginsTableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                customPluginsTableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                customPluginsTable.setAutoCreateRowSorter(true);
                
                // Handle buttons action in the table
                customPluginsTable.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent evt) {
                        int row = customPluginsTable.rowAtPoint(evt.getPoint());
                        int col = customPluginsTable.columnAtPoint(evt.getPoint());
                        if (row >= 0 && col >= 0) {
                        	List<CustomPlugin> customPlugins = ((CustomPluginsTableModel)(customPluginsTable.getModel())).getCustomPlugins();
                			CustomPlugin currentPlugin = customPlugins.get(row);
                        	switch(col) {
                        		// Enable/disable
                        		case 0:                        			
                        			if(currentPlugin.isOn()) {
                        				currentPlugin.disable(); 
                        			} else {
                        				currentPlugin.enable(); 
                        			}
                        			((CustomPluginsTableModel)(customPluginsTable.getModel())).fireTableCellUpdated(row, col);
                        			break;
                        		// Debug
                        		case 4:
                        			if(currentPlugin.getType() != CustomPlugin.CustomPluginType.JBUTTON) {
                        				currentPlugin.enableDebugToExternalFrame();
                        			}
                        			break;
                        		// Edit
                        		case 5:
                        			// TODO in future releases
                        			break;
                        		// Remove
                        		case 6:
                        			// If plugin is enabled, disable first
                            		if(currentPlugin.isOn())
                            			currentPlugin.disable();                              		
                            		// Double check because unload button hooks may fail if the application is running
                            		if(!currentPlugin.isOn()) {
            	                		synchronized(customPlugins) {                		
            	                			int currentPluginIndex = customPlugins.indexOf(currentPlugin);
            	                			customPlugins.remove(currentPlugin);
            	                			((CustomPluginsTableModel)(customPluginsTable.getModel())).fireTableRowsDeleted(currentPluginIndex, currentPluginIndex);
            	                		}
                            		}
                        			break;
                        		default:
                        			break;
                        	}
                        	
                        }
                    }
                });                
                
                // Center header
                ((DefaultTableCellRenderer)customPluginsTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
                               
                // Center columns 0, 4, 5 and 6
                customPluginsTable.getColumnModel().getColumn(0).setCellRenderer( centerRenderer );
                customPluginsTable.getColumnModel().getColumn(4).setCellRenderer( centerRenderer );
                customPluginsTable.getColumnModel().getColumn(5).setCellRenderer( centerRenderer );
                customPluginsTable.getColumnModel().getColumn(6).setCellRenderer( centerRenderer );
                
                JSplitPane customPluginsplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                customPluginsplitPane.setTopComponent(customPluginPanel);
                customPluginsplitPane.setBottomComponent(customPluginsTableScrollPane);                
                customPluginsplitPane.setResizeWeight(.7d);
                
                
                // **** END CUSTOM PLUGINS
                
            	tabbedPanel.add("Configurations",configurationConfPanel);
            	tabbedPanel.add("JS Editor",sp); 
            	tabbedPanel.add("Analyze binary",treeSearchPanel);
            	tabbedPanel.add("Generate stubs",stubTextEditor.getComponent());            	
            	tabbedPanel.add("Execute method",executeMethodPanel);
            	tabbedPanel.add("Trap methods",trapTableScrollPane);
            	tabbedPanel.add("Hooks and functions",tabbedPanelHooks);
            	tabbedPanel.add("Custom plugins",customPluginsplitPane);
            	            	
            	// *** CONSOLE            	
            	pluginConsoleTextArea = new JEditorPane("text/html", "<font color=\"green\"><b>*** Brida Console ***</b></font><br/><br/>");
                JScrollPane scrollPluginConsoleTextArea = new JScrollPane(pluginConsoleTextArea);
                scrollPluginConsoleTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                pluginConsoleTextArea.setEditable(false);
                                
                consoleTabbedSplitPane.setTopComponent(tabbedPanel);
                consoleTabbedSplitPane.setBottomComponent(scrollPluginConsoleTextArea);
                consoleTabbedSplitPane.setResizeWeight(.7d);
                            	
                // *** RIGHT - BUTTONS
            	
            	// RIGHT
                JPanel rightSplitPane = new JPanel();
                rightSplitPane.setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                
                documentServerStatusButtons = new DefaultStyledDocument();
                serverStatusButtons = new JTextPane(documentServerStatusButtons);                
                try {
                	documentServerStatusButtons.insertString(0, "Server stopped", redStyle);
				} catch (BadLocationException e) {
					printException(e,"Error setting labels");
				}
                serverStatusButtons.setMaximumSize( serverStatusButtons.getPreferredSize() );
                
                documentApplicationStatusButtons = new DefaultStyledDocument();
                applicationStatusButtons = new JTextPane(documentApplicationStatusButtons);                
                try {
                	documentApplicationStatusButtons.insertString(0, "App stopped", redStyle);
				} catch (BadLocationException e) {
					printException(e,"Error setting labels");
				}
                applicationStatusButtons.setMaximumSize( applicationStatusButtons.getPreferredSize() );
                                
            	JButton startServer = new JButton("Start server");
                startServer.setActionCommand("startServer");
                startServer.addActionListener(BurpExtender.this); 
                
                JButton killServer = new JButton("Kill server");
                killServer.setActionCommand("killServer");
                killServer.addActionListener(BurpExtender.this); 
            	
            	JButton spawnApplication = new JButton("Spawn application");
                spawnApplication.setActionCommand("spawnApplication");
                spawnApplication.addActionListener(BurpExtender.this);   
                
                JButton killApplication = new JButton("Kill application");
                killApplication.setActionCommand("killApplication");
                killApplication.addActionListener(BurpExtender.this);
                
                JButton reloadScript = new JButton("Reload JS");
                reloadScript.setActionCommand("reloadScript");
                reloadScript.addActionListener(BurpExtender.this); 
                
                clearConsoleButton = new JButton("Clear console");
                clearConsoleButton.setActionCommand("clearConsole");
                clearConsoleButton.addActionListener(BurpExtender.this);
                
                executeMethodButton = new JButton("Execute Method");
                executeMethodButton.setActionCommand("executeMethod");
                executeMethodButton.addActionListener(BurpExtender.this); 
                
                generateJavaStubButton = new JButton("Java Stub");
                generateJavaStubButton.setActionCommand("generateJavaStub");
                generateJavaStubButton.addActionListener(BurpExtender.this);    
                
                generatePythonStubButton = new JButton("Python Stub");
                generatePythonStubButton.setActionCommand("generatePythonStub");
                generatePythonStubButton.addActionListener(BurpExtender.this);
                
                saveSettingsToFileButton = new JButton("Save settings to file");
                saveSettingsToFileButton.setActionCommand("saveSettingsToFile");
                saveSettingsToFileButton.addActionListener(BurpExtender.this);  
                
                loadSettingsFromFileButton = new JButton("Load settings from file");
                loadSettingsFromFileButton.setActionCommand("loadSettingsFromFile");
                loadSettingsFromFileButton.addActionListener(BurpExtender.this);
                
                loadJSFileButton = new JButton("Load JS file");
                loadJSFileButton.setActionCommand("loadJsFile");
                loadJSFileButton.addActionListener(BurpExtender.this);  
                
                saveJSFileButton = new JButton("Save JS file");
                saveJSFileButton.setActionCommand("saveJsFile");
                saveJSFileButton.addActionListener(BurpExtender.this); 
                
                loadTreeButton = new JButton("Load tree");
                loadTreeButton.setActionCommand("loadTree");
                loadTreeButton.addActionListener(BurpExtender.this);
                                
                detachAllButton = new JButton("Detach all");
                detachAllButton.setActionCommand("detachAll");
                detachAllButton.addActionListener(BurpExtender.this); 
                
                enableCustomPluginButton = new JButton("Add plugin");
                enableCustomPluginButton.setActionCommand("enablePlugin");
                enableCustomPluginButton.addActionListener(BurpExtender.this); 
                           
                JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
                separator.setBorder(BorderFactory.createMatteBorder(3, 0, 3, 0, Color.ORANGE));

                rightSplitPane.add(serverStatusButtons,gbc);
                rightSplitPane.add(applicationStatusButtons,gbc);
                rightSplitPane.add(startServer,gbc);
                rightSplitPane.add(killServer,gbc);
                rightSplitPane.add(spawnApplication,gbc);
                rightSplitPane.add(killApplication,gbc);
                rightSplitPane.add(reloadScript,gbc);
                rightSplitPane.add(clearConsoleButton,gbc);

                rightSplitPane.add(separator,gbc);
                
                // TAB CONFIGURATIONS
                rightSplitPane.add(saveSettingsToFileButton,gbc);
                rightSplitPane.add(loadSettingsFromFileButton,gbc);
                
                // TAB JS EDITOR
                rightSplitPane.add(loadJSFileButton,gbc);
                rightSplitPane.add(saveJSFileButton,gbc);
                
                // TAB EXECUTE METHOD
                rightSplitPane.add(executeMethodButton,gbc);
                
                // TAB GENERATE STUBS
                rightSplitPane.add(generateJavaStubButton,gbc);
                rightSplitPane.add(generatePythonStubButton,gbc);
                
                // TREE ANALYSIS                
                rightSplitPane.add(loadTreeButton,gbc);     
                
                // TRAP METHODS
                rightSplitPane.add(detachAllButton,gbc);
                
                // CUSTOM PLUGINS
                rightSplitPane.add(enableCustomPluginButton,gbc);
                
                splitPane.setLeftComponent(consoleTabbedSplitPane);
                splitPane.setRightComponent(rightSplitPane);
                
                splitPane.setResizeWeight(.9d);

                mainPanel.add(splitPane);
                
                callbacks.customizeUiComponent(mainPanel);
                
                callbacks.addSuiteTab(BurpExtender.this);
                
            }
            
        });
		
	}
	
	private void changeCustomPluginOptions(String pluginType) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
            @Override
            public void run() {
            	
            	if(pluginType.equals("IHttpListener")) {
            		// Plugin description
                	customPluginTypePluginDescription.setText("Plugin that dynamically process each requests and responses");
                	// Execute on
                	customPluginExecuteOnRadioRequest.setVisible(true);
                	customPluginExecuteOnRadioResponse.setVisible(true);
                	customPluginExecuteOnRadioAll.setVisible(true);
                	customPluginExecuteOnRadioContext.setVisible(false);
                	customPluginExecuteOnRadioButton.setVisible(false);
                	customPluginExecuteOnStringParameter.setVisible(false);
                	customPluginExecuteOnRadioRequest.setSelected(true);
                	// Button platform
                	customPluginButtonPlatformPanel.setVisible(false);
                	// Button type
                	customPluginButtonTypePanel.setVisible(false);
                	// Burp Suite Tools
                	customPluginToolsPanel.setVisible(true);
                	// Only in scope?
                	customPluginScopePanel.setVisible(true);
                	// Execute
                	customPluginExecuteWhenPanel.setVisible(true);
                	// Parameter
                    customPluginParametersPanel.setVisible(true);
                	DefaultComboBoxModel<String> customPluginParametersModel = new DefaultComboBoxModel<String>(new String[] {"none", "complete request/response","headers","body","regex (with parenthesis)","fixed (#,# as separator)","ask to user with popup (#,# as separator)"});
                	customPluginParametersOptions.setModel(customPluginParametersModel);
                	// Parameter encoding
                	customPluginParameterEncodingPanel.setVisible(true);
                	// Plugin output
                	DefaultComboBoxModel<String> customPluginOutputModel = new DefaultComboBoxModel<String>(new String[] {"print in Brida console","replace in request/response with regex (with parenthesys)"});
                	customPluginOutputOptions.setModel(customPluginOutputModel);
                	customPluginOutputText.setVisible(true);
                	// Frida output decoding
                    customPluginOutputDecodingPanel.setVisible(true);
                    // Plugin output encoding
                    customPluginOutputEncodingPanel.setVisible(true);
                	// Message editor encode input to Frida function for edited content
                	customPluginMessageEditorModifiedEncodeInputPanel.setVisible(false);
                	// Message Editor Decoding Output
                	customPluginMessageEditorModifiedDecodingOutputPanel.setVisible(false);
                	// Message Editor Frida funtion for edited content
                	customPluginMessageEditorModifiedFridaFunctioPanel.setVisible(false);
                	// Message Editor Output encoding
                	customPluginMessageEditorModifiedOutputEncodingPanel.setVisible(false);
                	// Message Editor Output location
                	customPluginMessageEditorModifiedOutputLocationPanel.setVisible(false);
                } else if(pluginType.equals("IMessageEditorTab")) {
            		// Plugin description
                	customPluginTypePluginDescription.setText("Plugin that add a editable Message Editor Tab to all requests/responses");
                	// Execute on                	
                	customPluginExecuteOnRadioRequest.setVisible(true);
                	customPluginExecuteOnRadioResponse.setVisible(true);
                	customPluginExecuteOnRadioAll.setVisible(true);
                	customPluginExecuteOnRadioContext.setVisible(false);
                	customPluginExecuteOnRadioButton.setVisible(false);
                	customPluginExecuteOnStringParameter.setVisible(false);
                	customPluginExecuteOnRadioRequest.setSelected(true);
                	// Button platform
                	customPluginButtonPlatformPanel.setVisible(false);                	
                	// Button type
                	customPluginButtonTypePanel.setVisible(false);                	
                	// Burp Suite Tools
                	customPluginToolsPanel.setVisible(false);   
                	// Only in scope?
                	customPluginScopePanel.setVisible(false);                	
                	// Execute
                	customPluginExecuteWhenPanel.setVisible(true);  
                	// Parameter
                	customPluginParametersPanel.setVisible(true);
                	DefaultComboBoxModel<String> customPluginParametersModel = new DefaultComboBoxModel<String>(new String[] {"none", "complete request/response","headers","body","regex (with parenthesis)","fixed (#,# as separator)","ask to user with popup (#,# as separator)"});
                	customPluginParametersOptions.setModel(customPluginParametersModel);
                	// Parameter encoding
                	customPluginParameterEncodingPanel.setVisible(true);
                	// Plugin output
                	//DefaultComboBoxModel<String> customPluginOutputModel = new DefaultComboBoxModel<String>(new String[] {"print in Brida console","replace in request/response with regex (with parenthesys)"});
                	DefaultComboBoxModel<String> customPluginOutputModel = new DefaultComboBoxModel<String>(new String[] {"Print in Message Editor tab named"});
                	customPluginOutputOptions.setModel(customPluginOutputModel);
                	customPluginOutputText.setVisible(true);
                	// Frida output decoding
                    customPluginOutputDecodingPanel.setVisible(true);
                    // Plugin output encoding
                    customPluginOutputEncodingPanel.setVisible(true);                	
                	// Message editor encode input to Frida function for edited content
                	customPluginMessageEditorModifiedEncodeInputPanel.setVisible(true);                	
                	// Message Editor Decoding Output
                	customPluginMessageEditorModifiedDecodingOutputPanel.setVisible(true);
                	// Message Editor Frida funtion for edited content
                	customPluginMessageEditorModifiedFridaFunctioPanel.setVisible(true);
                	// Message Editor Output encoding
                	customPluginMessageEditorModifiedOutputEncodingPanel.setVisible(true); 
                	// Message Editor Output location
                	customPluginMessageEditorModifiedOutputLocationPanel.setVisible(true);
                } else if(pluginType.equals("IContextMenu")) {
            		// Plugin description
                	customPluginTypePluginDescription.setText("Plugin that add a context menu option to Burp Suite right-button menu");
                	// Execute on                	
                	customPluginExecuteOnRadioRequest.setVisible(false);
                	customPluginExecuteOnRadioResponse.setVisible(false);
                	customPluginExecuteOnRadioAll.setVisible(false);
                	customPluginExecuteOnRadioContext.setVisible(true);
                	customPluginExecuteOnRadioButton.setVisible(false);
                	customPluginExecuteOnStringParameter.setVisible(true);
                	customPluginExecuteOnRadioContext.setSelected(true);
                	// Button platform
                	customPluginButtonPlatformPanel.setVisible(false);                	
                	// Button type
                	customPluginButtonTypePanel.setVisible(false);                	
                	// Burp Suite Tools
                	customPluginToolsPanel.setVisible(false);    
                	// Only in scope?
                	customPluginScopePanel.setVisible(false);                	
                	// Execute
                	customPluginExecuteWhenPanel.setVisible(false); 
                	// Parameter
                	customPluginParametersPanel.setVisible(true);
                	DefaultComboBoxModel<String> customPluginParametersModel = new DefaultComboBoxModel<String>(new String[] {"none", "complete request/response","headers","body","regex (with parenthesis)","highlighted value in request/response","fixed (#,# as separator)","ask to user with popup (#,# as separator)"});
                	customPluginParametersOptions.setModel(customPluginParametersModel);
                	// Parameter encoding
                	customPluginParameterEncodingPanel.setVisible(true);
                	// Plugin output
                	DefaultComboBoxModel<String> customPluginOutputModel = new DefaultComboBoxModel<String>(new String[] {"print in Brida console","replace in request/response with regex (with parenthesys)","replace highlighted value in request/response"});
                	customPluginOutputOptions.setModel(customPluginOutputModel);
                	customPluginOutputText.setVisible(true);
                	// Frida output decoding
                    customPluginOutputDecodingPanel.setVisible(true);
                    // Plugin output encoding
                    customPluginOutputEncodingPanel.setVisible(true);                	
                	// Message editor encode input to Frida function for edited content
                	customPluginMessageEditorModifiedEncodeInputPanel.setVisible(false);                	
                	// Message Editor Decoding Output
                	customPluginMessageEditorModifiedDecodingOutputPanel.setVisible(false);
                	// Message Editor Frida funtion for edited content
                	customPluginMessageEditorModifiedFridaFunctioPanel.setVisible(false);
                	// Message Editor Output encoding
                	customPluginMessageEditorModifiedOutputEncodingPanel.setVisible(false);         
                	// Message Editor Output location
                	customPluginMessageEditorModifiedOutputLocationPanel.setVisible(false);
                } else {
            		// Plugin description
                	customPluginTypePluginDescription.setText("Plugin that add a button that enable a hook/call a function");
                	// Execute on                	
                	customPluginExecuteOnRadioRequest.setVisible(false);
                	customPluginExecuteOnRadioResponse.setVisible(false);
                	customPluginExecuteOnRadioAll.setVisible(false);
                	customPluginExecuteOnRadioContext.setVisible(false);
                	customPluginExecuteOnRadioButton.setVisible(true);
                	customPluginExecuteOnStringParameter.setVisible(true);
                	customPluginExecuteOnRadioButton.setSelected(true);
                	// Button platform
                	customPluginButtonPlatformPanel.setVisible(true);                	
                	// Button type
                	customPluginButtonTypePanel.setVisible(true);                	
                	// Burp Suite Tools
                	customPluginToolsPanel.setVisible(false);   
                	// Only in scope?
                	customPluginScopePanel.setVisible(false);                	
                	// Execute
                	customPluginExecuteWhenPanel.setVisible(false);  
                	// Parameter
                	if(customPluginButtonTypeRadioFunction.isSelected()) {
                		customPluginParametersPanel.setVisible(true);
                	} else {
                		customPluginParametersPanel.setVisible(false);
                	}
                	DefaultComboBoxModel<String> customPluginParametersModel = new DefaultComboBoxModel<String>(new String[] {"none", "fixed (#,# as separator)","ask to user with popup (#,# as separator)"});
                	customPluginParametersOptions.setModel(customPluginParametersModel);
                	// Parameter encoding
                	if(customPluginButtonTypeRadioFunction.isSelected()) {
                		customPluginParameterEncodingPanel.setVisible(true);
                	} else {
                		customPluginParameterEncodingPanel.setVisible(false);
                	}
                	// Plugin output
                	DefaultComboBoxModel<String> customPluginOutputModel = new DefaultComboBoxModel<String>(new String[] {"print in Brida console"});
                	customPluginOutputOptions.setModel(customPluginOutputModel);
                	customPluginOutputText.setVisible(false);
                	// Frida output decoding
                    customPluginOutputDecodingPanel.setVisible(false);
                    // Plugin output encoding
                    customPluginOutputEncodingPanel.setVisible(false);                	
                	// Message editor encode input to Frida function for edited content
                	customPluginMessageEditorModifiedEncodeInputPanel.setVisible(false);                	
                	// Message Editor Decoding Output
                	customPluginMessageEditorModifiedDecodingOutputPanel.setVisible(false);
                	// Message Editor Frida funtion for edited content
                	customPluginMessageEditorModifiedFridaFunctioPanel.setVisible(false);
                	// Message Editor Output encoding
                	customPluginMessageEditorModifiedOutputEncodingPanel.setVisible(false);    
                	// Message Editor Output location
                	customPluginMessageEditorModifiedOutputLocationPanel.setVisible(false);
                }
            	
            }
            
		});
		
	}
		
	private void showHideButtons(int indexTabbedPanel) {
		
		switch(indexTabbedPanel) {
		
			// CONFIGURATIONS
			case 0:
				
				SwingUtilities.invokeLater(new Runnable() {
					
		            @Override
		            public void run() {
				
						executeMethodButton.setVisible(false);
						saveSettingsToFileButton.setVisible(true);
						loadSettingsFromFileButton.setVisible(true);
						generateJavaStubButton.setVisible(false);
						generatePythonStubButton.setVisible(false);
						loadJSFileButton.setVisible(false);
						saveJSFileButton.setVisible(false);
						loadTreeButton.setVisible(false);
						detachAllButton.setVisible(false);
						enableCustomPluginButton.setVisible(false);

		            }
		            
				});
				
				break;
			
			// JS editor	
			case 1:
				
				SwingUtilities.invokeLater(new Runnable() {
					
		            @Override
		            public void run() {

		            	executeMethodButton.setVisible(false);
						saveSettingsToFileButton.setVisible(false);
						loadSettingsFromFileButton.setVisible(false);
						generateJavaStubButton.setVisible(false);
						generatePythonStubButton.setVisible(false);
						loadJSFileButton.setVisible(true);
						saveJSFileButton.setVisible(true);
						loadTreeButton.setVisible(false);
						detachAllButton.setVisible(false);
						enableCustomPluginButton.setVisible(false);

		            }
		            
				});
				
				break;	
				
			// Tree view	
			case 2:
								
				SwingUtilities.invokeLater(new Runnable() {
					
		            @Override
		            public void run() {

		            	executeMethodButton.setVisible(false);
						saveSettingsToFileButton.setVisible(false);
						loadSettingsFromFileButton.setVisible(false);
						generateJavaStubButton.setVisible(false);
						generatePythonStubButton.setVisible(false);
						loadJSFileButton.setVisible(false);
						saveJSFileButton.setVisible(false);
						loadTreeButton.setVisible(true);
						detachAllButton.setVisible(false);
						enableCustomPluginButton.setVisible(false);

		            }
		            
				});
				
				break;					
				
				
			// GENERATE STUBS	
			case 3:
				
				SwingUtilities.invokeLater(new Runnable() {
					
		            @Override
		            public void run() {

		            	executeMethodButton.setVisible(false);
						saveSettingsToFileButton.setVisible(false);
						loadSettingsFromFileButton.setVisible(false);
						generateJavaStubButton.setVisible(true);
						generatePythonStubButton.setVisible(true);
						loadJSFileButton.setVisible(false);
						saveJSFileButton.setVisible(false);
						loadTreeButton.setVisible(false);
						detachAllButton.setVisible(false);
						enableCustomPluginButton.setVisible(false);

		            }
		            
				});
				
				break;
			
			// EXECUTE METHODS	
			case 4:
				
				SwingUtilities.invokeLater(new Runnable() {
					
		            @Override
		            public void run() {
				
						executeMethodButton.setVisible(true);
						saveSettingsToFileButton.setVisible(false);
						loadSettingsFromFileButton.setVisible(false);
						generateJavaStubButton.setVisible(false);
						generatePythonStubButton.setVisible(false);
						loadJSFileButton.setVisible(false);
						saveJSFileButton.setVisible(false);
						loadTreeButton.setVisible(false);
						detachAllButton.setVisible(false);
						enableCustomPluginButton.setVisible(false);

		            }
		            
				});
				
				break;
				
			//TRAP METHODS	
			case 5:
				
				SwingUtilities.invokeLater(new Runnable() {
					
		            @Override
		            public void run() {
				
						executeMethodButton.setVisible(false);
						saveSettingsToFileButton.setVisible(false);
						loadSettingsFromFileButton.setVisible(false);
						generateJavaStubButton.setVisible(false);
						generatePythonStubButton.setVisible(false);
						loadJSFileButton.setVisible(false);
						saveJSFileButton.setVisible(false);
						loadTreeButton.setVisible(false);
						detachAllButton.setVisible(true);
						enableCustomPluginButton.setVisible(false);

		            }
		            
				});
				
				break;

				//DEFAULT HOOKS	
				case 6:
					
					SwingUtilities.invokeLater(new Runnable() {
						
			            @Override
			            public void run() {
					
							executeMethodButton.setVisible(false);
							saveSettingsToFileButton.setVisible(false);
							loadSettingsFromFileButton.setVisible(false);
							generateJavaStubButton.setVisible(false);
							generatePythonStubButton.setVisible(false);
							loadJSFileButton.setVisible(false);
							saveJSFileButton.setVisible(false);
							loadTreeButton.setVisible(false);
							detachAllButton.setVisible(true);
							enableCustomPluginButton.setVisible(false);
							
			            }
			            
					});
					
					break;	
					
				//CUSTOM PLUGIN	
				case 7:
					
					SwingUtilities.invokeLater(new Runnable() {
						
			            @Override
			            public void run() {
					
							executeMethodButton.setVisible(false);
							saveSettingsToFileButton.setVisible(false);
							loadSettingsFromFileButton.setVisible(false);
							generateJavaStubButton.setVisible(false);
							generatePythonStubButton.setVisible(false);
							loadJSFileButton.setVisible(false);
							saveJSFileButton.setVisible(false);
							loadTreeButton.setVisible(false);
							detachAllButton.setVisible(false);
			                enableCustomPluginButton.setVisible(true);

			            }
			            
					});
					
					break;						
			
			default:			
				printException(null,"ShowHideButtons: index not found");				
				break;	
		
		}
		
	}	
	
	private void launchPyroServer(String pythonPath, String pyroServicePath) {
		
		Runtime rt = Runtime.getRuntime();
		
		String[] startServerCommand = {pythonPath,"-i",pyroServicePath,pyroHost.getText().trim(),pyroPort.getText().trim()};
			
		try {
			pyroServerProcess = rt.exec(startServerCommand);
						
			final BufferedReader stdOutput = new BufferedReader(new InputStreamReader(pyroServerProcess.getInputStream()));
			final BufferedReader stdError = new BufferedReader(new InputStreamReader(pyroServerProcess.getErrorStream()));
		    
			// Initialize thread that will read stdout
			stdoutThread = new Thread() {
				
				public void run() {
					
						while(true) {
					
							try {
								
								final String line = stdOutput.readLine();
								
								// Only used to handle Pyro first message (when server start)
								if(line.equals("Ready.")) {
									        	
						        	pyroBridaService = new PyroProxy(new PyroURI("PYRO:BridaServicePyro@" + pyroHost.getText().trim() + ":" + pyroPort.getText().trim()));
						        	serverStarted = true;	 
						        	
						        	SwingUtilities.invokeLater(new Runnable() {
										
							            @Override
							            public void run() {
							            	
							            	serverStatus.setText("");
							            	serverStatusButtons.setText("");
							            	try {
							                	documentServerStatus.insertString(0, "running", greenStyle);
							                	documentServerStatusButtons.insertString(0, "Server running", greenStyle);
											} catch (BadLocationException e) {
												
												printException(e,"Exception setting labels");
	
											}
											
							            }
									});
						        	
						        	printSuccessMessage("Pyro server started correctly");
								
						        // Standard line	
								} else {
									
									printJSMessage(line);
									
								}
								
								
							} catch (IOException e) {
								printException(e,"Error reading Pyro stdout");
							}
							
						}
				}
				
			};			
			stdoutThread.start();
			
			// Initialize thread that will read stderr
			stderrThread = new Thread() {
				
				public void run() {
					
						while(true) {
												
							try {
								
								final String line = stdError.readLine();								
								printException(null,line);								
								
							} catch (IOException e) {
								
								printException(e,"Error reading Pyro stderr");
								
							}
							
						}
				}
				
			};			
			stderrThread.start();
			
		} catch (final Exception e1) {
			
			printException(e1,"Exception starting Pyro server");

		}
		
		
	}
	
	private String generateJavaStub() {
		
		String out = "";
		out += "import net.razorvine.pyro.*;\n";
		out += "\n";
		out += "String pyroUrl = \"PYRO:BridaServicePyro@" + pyroHost.getText().trim() + ":" + pyroPort.getText().trim() + "\";\n";
		out += "try {\n";
		out += "\tPyroProxy pp = new PyroProxy(new PyroURI(pyroUrl));\n";
		out += "\tString ret = (String)pp.call(\"callexportfunction\",\"METHOD_NAME\",new String[]{\"METHOD_ARG_1\",\"METHOD_ARG_2\",...});\n";
		out += "\tpp.close();\n";
		out += "} catch(IOException e) {\n";
		out += "\t// EXCEPTION HANDLING\n";
		out += "}\n";
		
		return out;
		
	}
	
	private String generatePythonStub() {
		
		String out = "";
		out += "import Pyro4\n";
		out += "\n";
		out += "uri = 'PYRO:BridaServicePyro@" + pyroHost.getText().trim() + ":" + pyroPort.getText().trim() + "'\n";
		out += "pp = Pyro4.Proxy(uri)\n";
		out += "args = []\n";
		out += "args.append(\"METHOD_ARG_1\")\n";
		out += "args.append(\"METHOD_ARG_2\")\n";
		out += "args.append(\"...\")\n";
		out += "ret = pp.callexportfunction('METHOD_NAME',args)\n";
		out += "pp._pyroRelease()\n";
		
		return out;
		
	}
	
	private void savePersistentSettings() {
		
		callbacks.saveExtensionSetting("pythonPath",pythonPath.getText().trim());
		callbacks.saveExtensionSetting("pyroHost",pyroHost.getText().trim());
		callbacks.saveExtensionSetting("pyroPort",pyroPort.getText().trim());
		callbacks.saveExtensionSetting("fridaPath",fridaPath.getText().trim());
		callbacks.saveExtensionSetting("applicationId",applicationId.getText().trim());
		if(remoteRadioButton.isSelected()) {
			callbacks.saveExtensionSetting("remote","true");
		} else {
			callbacks.saveExtensionSetting("remote","false");
		}
		callbacks.saveExtensionSetting("executeMethodName",executeMethodName.getText().trim());
		int sizeArguments = executeMethodInsertedArgumentList.getSize();
		callbacks.saveExtensionSetting("executeMethodSizeArguments",Integer.toString(sizeArguments));
		for(int i=0; i< sizeArguments; i++) {			
			callbacks.saveExtensionSetting("executeMethodArgument" + i,(String)executeMethodInsertedArgumentList.getElementAt(i));			
		}

	}
	
	private void exportConfigurationsToFile() {
		
		JFrame parentFrame = new JFrame();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Configuration output file");
		
		int userSelection = fileChooser.showSaveDialog(parentFrame);
		
		if(userSelection == JFileChooser.APPROVE_OPTION) {

			
			File outputFile = fileChooser.getSelectedFile();
			FileWriter fw;
			try {
				fw = new FileWriter(outputFile);
				
				fw.write("pythonPath:" + pythonPath.getText().trim() + "\n");
				fw.write("pyroHost:" + pyroHost.getText().trim() + "\n");
				fw.write("pyroPort:" + pyroPort.getText().trim() + "\n");
				fw.write("fridaPath:" + fridaPath.getText().trim() + "\n");
				fw.write("applicationId:" + applicationId.getText().trim() + "\n");
				if(remoteRadioButton.isSelected()) 
					fw.write("remote:true\n");
				else
					fw.write("remote:false\n");
				fw.write("executeMethodName:" + executeMethodName.getText().trim() + "\n");
				
				int sizeArguments = executeMethodInsertedArgumentList.getSize();
				fw.write("executeMethodSizeArguments:" + sizeArguments + "\n");
				for(int i=0; i< sizeArguments; i++) {			
					fw.write("executeMethodArgument" + i + ":" + ((String)executeMethodInsertedArgumentList.getElementAt(i)) + "\n");			
				}				
				
				fw.close();
				
				printSuccessMessage("Saving configurations to file executed correctly");
				
			} catch (final IOException e) {
				
				printException(e,"Exception exporting configurations to file");
				
				return;
			}			
				
		}
		
	}
	
	private void execute_startup_scripts() {
		
		DefaultHook currentHook;
		for(int i=0; i < defaultHooks.size();i++) {
			
			currentHook = defaultHooks.get(i);
			
			if(currentHook.isEnabled() && currentHook.getOs() == platform) {
				
				try {
					
					pyroBridaService.call("callexportfunction",currentHook.getFridaExportName(),new String[] {});
					
				} catch (Exception e) {
						
					 printException(e,"Exception running starting hook " + currentHook.getName());
						
				}
				
			}
			
		}
		
	}

	private void loadConfigurationsFromFile() {
		
		JFrame parentFrame = new JFrame();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Configuration input file");
		
		int userSelection = fileChooser.showOpenDialog(parentFrame);
		
		if(userSelection == JFileChooser.APPROVE_OPTION) {
			
			File inputFile = fileChooser.getSelectedFile();
						
			try {
				
				BufferedReader br = new BufferedReader(new FileReader(inputFile));
				
				String line;
				while ((line = br.readLine()) != null) {
					String[] lineParts = line.split(":",2);
					
					if(lineParts.length > 1) {
											
						switch(lineParts[0]) {
						case "pythonPath":
							pythonPath.setText(lineParts[1]);
							break;
						case "pyroHost":
							pyroHost.setText(lineParts[1]);
							break;
						case "pyroPort":
							pyroPort.setText(lineParts[1]);
							break;
						case "fridaPath":
							fridaPath.setText(lineParts[1]);
							break;
						case "applicationId":
							applicationId.setText(lineParts[1]);
							break;
						case "remote":
							if(lineParts[1].equals("true")) {
								remoteRadioButton.setSelected(true);
							} else {
								localRadioButton.setSelected(true);
							}
							break;
						case "executeMethodSizeArguments":
							executeMethodInsertedArgumentList.clear();
							break;
						case "executeMethodName":
							executeMethodName.setText(lineParts[1]);
							break;
						default:
							if(lineParts[0].startsWith("executeMethodArgument")) {
								executeMethodInsertedArgumentList.addElement(lineParts[1]);
							} else {
								printException(null,"Invalid option " + lineParts[0]);
							}							
						}
						
					} else {
						
						printException(null,"The line does not contain a valid option");
						
					}
					
				}
							 				
				br.close();
				
				printSuccessMessage("Loading configurations executed correctly");
				
			} catch (final Exception e) {
				
				printException(e,"Error loading configurations from file");
				return;
				
			}
			
			
		}
	}

	public String getTabCaption() {

		return "Brida";
	}

	public Component getUiComponent() {
		return mainPanel;
	}

	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		
		if (command.equals("addExecuteMethodArgument")) {
			
			SwingUtilities.invokeLater(new Runnable() {
				
	            @Override
	            public void run() {
	            	
	            	executeMethodInsertedArgumentList.addElement(executeMethodArgument.getText().trim());
	    			executeMethodArgument.setText("");
					
	            }
			});		
			
		} else  if (command.equals("removeExecuteMethodArgument")) {
			
			SwingUtilities.invokeLater(new Runnable() {
				
	            @Override
	            public void run() {
	            	
	            	int index = executeMethodInsertedArgument.getSelectedIndex();
	            	if(index != -1) {
	            		executeMethodInsertedArgumentList.remove(index);
	            	}
	            	
	            }
			});	
			
		} else  if (command.equals("modifyExecuteMethodArgument")) {
			
			SwingUtilities.invokeLater(new Runnable() {
				
	            @Override
	            public void run() {
	            	
	            	int index = executeMethodInsertedArgument.getSelectedIndex();
	            	if(index != -1) {
	            		executeMethodArgument.setText((String)executeMethodInsertedArgument.getSelectedValue());
	            		executeMethodInsertedArgumentList.remove(index);
	            	}
					
	            }
			});	
		
		
		} else if(command.equals("spawnApplication") && serverStarted) {
			
			try {
				
				pyroBridaService.call("spawn_application", applicationId.getText().trim(), fridaPath.getText().trim(),remoteRadioButton.isSelected());
				
				execute_startup_scripts();
				
				// Wait for 3 seconds in order to load hooks
				Thread.sleep(3000);
				
				pyroBridaService.call("resume_application");				
				
				applicationSpawned = true;
				
				SwingUtilities.invokeLater(new Runnable() {
					
		            @Override
		            public void run() {
		            	
		            	applicationStatus.setText("");
		            	applicationStatusButtons.setText("");
		            			            	
		            	// Empty trapping table
		            	List<TrapTableItem> trapEntries = ((TrapTableModel)(trapTable.getModel())).getTrappedMethods();
		            	synchronized(trapEntries) {
		            		int trapEntryOldSize = trapEntries.size();
		            		if(trapEntryOldSize > 0) {
		            			trapEntries.clear();
		            			((TrapTableModel)(trapTable.getModel())).fireTableRowsDeleted(0, trapEntryOldSize - 1);
		            		}
		                }
		            	
		            	try {
		                	documentApplicationStatus.insertString(0, "spawned", greenStyle);
		                	documentApplicationStatusButtons.insertString(0, "App running", greenStyle);
						} catch (BadLocationException e) {
							printException(e,"Exception with labels");
						}
						
		            }
				});
				
				printSuccessMessage("Application " + applicationId.getText().trim() + " spawned correctly");
				
				// GETTING PLAFORM INFO (ANDROID/IOS/GENERIC)			
				try {
					platform = (int)(pyroBridaService.call("callexportfunction","getplatform",new String[] {}));
					if(platform == BurpExtender.PLATFORM_ANDROID) {
						printSuccessMessage("Platform: Android");					
					} else if(platform == BurpExtender.PLATFORM_IOS) {
						printSuccessMessage("Platform: iOS");
					} else {
						printSuccessMessage("Platform: Generic");
					}
				} catch (Exception e) {				
					printException(e,"Exception with getting info Android/iOS");				
				}
				
			} catch (final Exception e) {
				
				printException(e,"Exception with spawn application");
				
			}		
			
		} else if(command.equals("reloadScript") && serverStarted && applicationSpawned) {
				
			try {
				
				pyroBridaService.call("reload_script");
				
				printSuccessMessage("Reloading script executed");
				
			} catch (final Exception e) {
								
				printException(e,"Exception reloading script");
				
			}
	
						
		} else if(command.equals("killApplication") && serverStarted && applicationSpawned) {
			
			try {
				pyroBridaService.call("disconnect_application");
				applicationSpawned = false;
				
				SwingUtilities.invokeLater(new Runnable() {
					
		            @Override
		            public void run() {
		            	
		            	applicationStatus.setText("");
		            	applicationStatusButtons.setText("");
		            	try {
		                	documentApplicationStatus.insertString(0, "NOT spawned", redStyle);
		                	documentApplicationStatusButtons.insertString(0, "App stopped", redStyle);
						} catch (BadLocationException e) {
							printException(e,"Exception setting labels");
						}
						
		            }
				});
				
				printSuccessMessage("Killing application executed");
				
			} catch (final Exception e) {
				
				printException(e,"Exception killing application");
				
			}
			
		} else if(command.equals("killServer") && serverStarted) {
			
			stdoutThread.stop();
			stderrThread.stop();
			
			try {
				pyroBridaService.call("shutdown");
				pyroServerProcess.destroy();
				pyroBridaService.close();
				serverStarted = false;
				
				SwingUtilities.invokeLater(new Runnable() {
					
		            @Override
		            public void run() {
		            	
		            	serverStatus.setText("");
		            	serverStatusButtons.setText("");
		            	try {
		                	documentServerStatus.insertString(0, "NOT running", redStyle);
		                	documentServerStatusButtons.insertString(0, "Server stopped", redStyle);
						} catch (BadLocationException e) {
							printException(e,"Exception setting labels");
						}
						
		            }
				});
				
				printSuccessMessage("Pyro server shutted down");
				
			} catch (final Exception e) {
				
				printException(e,"Exception shutting down Pyro server");
				
			}
		
			
		} else if(command.equals("startServer") && !serverStarted) {
			
			savePersistentSettings();
			
			try {
				
				launchPyroServer(pythonPath.getText().trim(),pythonScript);

			} catch (final Exception e) {
								
				printException(null,"Exception starting Pyro server");
								
			}
			
		} else if(command.equals("executeMethod")) {
			
			savePersistentSettings();
			
			try {
				
				String[] arguments = new String[executeMethodInsertedArgumentList.size()];
				for(int i=0;i<executeMethodInsertedArgumentList.size();i++) {	
					arguments[i] = (String)(executeMethodInsertedArgumentList.getElementAt(i));
				}
				
				final String s = (String)(pyroBridaService.call("callexportfunction",executeMethodName.getText().trim(),arguments));
								
				printJSMessage("*** Output " + executeMethodName.getText().trim() + ":");
				printJSMessage(s);
				
			} catch (Exception e) {
				
				printException(e,"Exception with execute method");
				
			}
			
		} else if(command.equals("generateJavaStub")) {
			
			SwingUtilities.invokeLater(new Runnable() {
				
	            @Override
	            public void run() {
	            	
	            	stubTextEditor.setText(generateJavaStub().getBytes());
	                
	            }
			});
	
		} else if(command.equals("generatePythonStub")) {
			
			SwingUtilities.invokeLater(new Runnable() {
				
	            @Override
	            public void run() {
	            	
	            	stubTextEditor.setText(generatePythonStub().getBytes());

	            }
			});
			
		} else if(command.equals("saveSettingsToFile")) {
			
			exportConfigurationsToFile();
			
		} else if(command.equals("loadSettingsFromFile")) {
			
			loadConfigurationsFromFile();			
			
		} else if(command.equals("loadJsFile")) {
			
			File jsFile = new File(fridaPath.getText().trim());
			final FileLocation fl = FileLocation.create(jsFile);
			
			SwingUtilities.invokeLater(new Runnable() {
				
	            @Override
	            public void run() {
	            			            	
	            	try {
						jsEditorTextArea.load(fl, null);
					} catch (IOException e) {
						printException(e,"Exception loading JS file");
					}

	            }
			});
						
		} else if(command.equals("saveJsFile")) {
		
			try {
				jsEditorTextArea.save();
			} catch (IOException e) {
				printException(e,"Error saving JS file");
			}
					
		} else if(command.equals("contextcustom1") || command.equals("contextcustom2")) {
			
			IHttpRequestResponse[] selectedItems = currentInvocation.getSelectedMessages();
			int[] selectedBounds = currentInvocation.getSelectionBounds();
			byte selectedInvocationContext = currentInvocation.getInvocationContext();
			
			try {
				
				byte[] selectedRequestOrResponse = null;
				if(selectedInvocationContext == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {
					selectedRequestOrResponse = selectedItems[0].getRequest();
				} else {
					selectedRequestOrResponse = selectedItems[0].getResponse();
				}
				
				byte[] preSelectedPortion = Arrays.copyOfRange(selectedRequestOrResponse, 0, selectedBounds[0]);
				byte[] selectedPortion = Arrays.copyOfRange(selectedRequestOrResponse, selectedBounds[0], selectedBounds[1]);
				byte[] postSelectedPortion = Arrays.copyOfRange(selectedRequestOrResponse, selectedBounds[1], selectedRequestOrResponse.length);
				
				String s = (String)(pyroBridaService.call("callexportfunction",command,new String[]{byteArrayToHexString(selectedPortion)}));
				
				byte[] newRequest = ArrayUtils.addAll(preSelectedPortion, hexStringToByteArray(s));
				newRequest = ArrayUtils.addAll(newRequest, postSelectedPortion);
				
				selectedItems[0].setRequest(newRequest);
			
			} catch (Exception e) {
				
				printException(e,"Exception with custom context application");
				
			}
				

		} else if(command.equals("loadTree")) {
			
			try {
				
				ArrayList<String> allClasses = (ArrayList<String>)(pyroBridaService.call("callexportfunction","getallclasses",new String[0]));
				HashMap<String, Integer> allModules = (HashMap<String,Integer>)(pyroBridaService.call("callexportfunction","getallmodules",new String[0]));
				
				// Sort classes
				Collections.sort(allClasses, new Comparator<String>() {
			        @Override
			        public int compare(String class1, String class2)
			        {

			            return  class1.compareToIgnoreCase(class2);
			        }
			    });	
				
				
				ArrayList<String> moduleNames = new ArrayList<String>(allModules.keySet());
				
				// Sort module names
				Collections.sort(moduleNames, new Comparator<String>() {
			        @Override
			        public int compare(String class1, String class2)
			        {

			            return  class1.compareToIgnoreCase(class2);
			        }
			    });	
								
				DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
				
				DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode("Binary");
				
				DefaultMutableTreeNode currentNode;
				
				// ONLY FOR IOS AND ANDROID
				if(platform == BurpExtender.PLATFORM_ANDROID || platform == BurpExtender.PLATFORM_IOS) {
				
					DefaultMutableTreeNode objNode = (platform == BurpExtender.PLATFORM_ANDROID ? new DefaultMutableTreeNode("Java") : new DefaultMutableTreeNode("Objective-C"));
				
					for(int i=0; i<allClasses.size(); i++) {
	
						currentNode = new DefaultMutableTreeNode(allClasses.get(i));
	
						objNode.add(currentNode);
						
					}
	
					newRoot.add(objNode);
					
				}
				
				DefaultMutableTreeNode modulesNode = new DefaultMutableTreeNode("Modules");
			
				for(int i=0; i<moduleNames.size(); i++) {

					currentNode = new DefaultMutableTreeNode(moduleNames.get(i));

					modulesNode.add(currentNode);
					
				}

				newRoot.add(modulesNode);				
				
				model.setRoot(newRoot);

			} catch (Exception e) {
								
				printException(e,"Exception with load tree");
				
			}

		} else if(command.equals("searchAnalysis")) {
		
			String toSearch = findTextField.getText().trim();
			
			HashMap<String, Integer> foundObjcMethods = null;
			if(platform == BurpExtender.PLATFORM_IOS) {
				try {
					foundObjcMethods = (HashMap<String,Integer>)(pyroBridaService.call("callexportfunction","findobjcmethods",new String[] {toSearch}));
				} catch (Exception e) {
					printException(e,"Exception searching OBJC methods");
					return;
				} 
			}
			
			HashMap<String, Integer> foundImports = null;
			try {
				foundImports = (HashMap<String,Integer>)(pyroBridaService.call("callexportfunction","findimports",new String[] {toSearch}));
			} catch (Exception e) {
				printException(e,"Exception searching imports");
				return;
			} 
			
			HashMap<String, Integer> foundExports = null;
			try {
				foundExports = (HashMap<String,Integer>)(pyroBridaService.call("callexportfunction","findexports",new String[] {toSearch}));
			} catch (Exception e) {
				printException(e,"Exception searching exports");
				return;
			} 
				
			printJSMessage("**** Result of the search of " + findTextField.getText().trim());
			
			if(foundObjcMethods != null) {
				
				ArrayList<String> objcMethodNames = new ArrayList<String>(foundObjcMethods.keySet());
				
				// Sort objc method names
				Collections.sort(objcMethodNames, new Comparator<String>() {
			        @Override
			        public int compare(String class1, String class2)
			        {

			            return  class1.compareToIgnoreCase(class2);
			        }
			    });	
			
				Iterator<String> currentClassMethodsIterator = objcMethodNames.iterator(); 
				
				String currentMethodName;
				
				while(currentClassMethodsIterator.hasNext()) {
					
					currentMethodName = currentClassMethodsIterator.next();
					printJSMessage("OBJC: " + currentMethodName);
					
				}
				
			}
			
			if(foundImports != null) {
				
				ArrayList<String> importNames = new ArrayList<String>(foundImports.keySet());
				
				// Sort import names
				Collections.sort(importNames, new Comparator<String>() {
			        @Override
			        public int compare(String class1, String class2)
			        {

			            return  class1.compareToIgnoreCase(class2);
			        }
			    });	
				
				Iterator<String> currentImportIterator = importNames.iterator(); 
				
				
				
				String currentImportName;
				
				while(currentImportIterator.hasNext()) {
					
					currentImportName = currentImportIterator.next();
					printJSMessage("IMPORT: " + currentImportName);
					
				}
				
			}
			
			if(foundExports != null) {
				
				ArrayList<String> exportNames = new ArrayList<String>(foundExports.keySet());
				
				// Sort export names
				Collections.sort(exportNames, new Comparator<String>() {
			        @Override
			        public int compare(String class1, String class2)
			        {

			            return  class1.compareToIgnoreCase(class2);
			        }
			    });	
				
				Iterator<String> exportIterator = exportNames.iterator(); 
				
				
				String currentExportName;
				
				while(exportIterator.hasNext()) {
					
					currentExportName = exportIterator.next();
					printJSMessage("EXPORT: " + currentExportName);
					
				}
				
			}
					
		} else if(command.equals("trap")) {	
			
			trap(false);
			
		} else if(command.equals("detachAll")) {	
			
			int dialogButton = JOptionPane.YES_NO_OPTION;
			int dialogResult = JOptionPane.showConfirmDialog(mainPanel, "Detach all will detach also custom interception methods defined in your JS file and hooks enabled in the hooks and functions section. Are you sure?", "Confirm detach all", dialogButton);
			if(dialogResult == 0) {
				try {
					pyroBridaService.call("callexportfunction","detachAll",new String[] {});
				} catch (Exception e) {					
					printException(e,"Exception detaching all");
					return;
				}
				
				// Empty trapping table
            	List<TrapTableItem> trapEntries = ((TrapTableModel)(trapTable.getModel())).getTrappedMethods();
            	synchronized(trapEntries) {
            		int trapEntryOldSize = trapEntries.size();
            		if(trapEntryOldSize > 0) {
            			trapEntries.clear();
            			((TrapTableModel)(trapTable.getModel())).fireTableRowsDeleted(0, trapEntryOldSize - 1);
            		}
                }
				
				printSuccessMessage("Detaching all successfully executed");
				
			} else {
				printSuccessMessage("Detaching all CANCELED as requested by the user");
			}			
			
		} else if(command.equals("trapBacktrace")) {	
			
			trap(true);	
			
		} else if(command.equals("contextcustom3") || command.equals("contextcustom4")) {
			
			IHttpRequestResponse[] selectedItems = currentInvocation.getSelectedMessages();
			int[] selectedBounds = currentInvocation.getSelectionBounds();
			byte selectedInvocationContext = currentInvocation.getInvocationContext();
			
			try {
				
				byte[] selectedRequestOrResponse = null;
				if(selectedInvocationContext == IContextMenuInvocation.CONTEXT_MESSAGE_VIEWER_REQUEST) {
					selectedRequestOrResponse = selectedItems[0].getRequest();
				} else {
					selectedRequestOrResponse = selectedItems[0].getResponse();
				}
				
				byte[] selectedPortion = Arrays.copyOfRange(selectedRequestOrResponse, selectedBounds[0], selectedBounds[1]);
				
				final String s = (String)(pyroBridaService.call("callexportfunction",command,new String[]{byteArrayToHexString(selectedPortion)}));
				
				SwingUtilities.invokeLater(new Runnable() {
					
		            @Override
		            public void run() {
		            	
		            	JTextArea ta = new JTextArea(10, 10);
		                ta.setText(new String(hexStringToByteArray(s)));
		                ta.setWrapStyleWord(true);
		                ta.setLineWrap(true);
		                ta.setCaretPosition(0);
		                ta.setEditable(false);

		                JOptionPane.showMessageDialog(null, new JScrollPane(ta), "Custom invocation response", JOptionPane.INFORMATION_MESSAGE);
					    
		            }
		            
				});
				
			
			} catch (Exception e) {

				printException(e,"Exception with custom context application");
				
			}
		

		} else if(command.equals("pythonPathSelectFile")) {
			
			JFrame parentFrame = new JFrame();
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Python Path");
			
			int userSelection = fileChooser.showOpenDialog(parentFrame);
			
			if(userSelection == JFileChooser.APPROVE_OPTION) {
				
				final File pythonPathFile = fileChooser.getSelectedFile();
				
				SwingUtilities.invokeLater(new Runnable() {
					
		            @Override
		            public void run() {
		            	pythonPath.setText(pythonPathFile.getAbsolutePath());
		            }
				
				});
				
			}				
			
		} else if(command.equals("fridaPathSelectFile")) {
			
			JFrame parentFrame = new JFrame();
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Frida JS Path");
			
			int userSelection = fileChooser.showOpenDialog(parentFrame);
			
			if(userSelection == JFileChooser.APPROVE_OPTION) {
				
				final File fridaPathFile = fileChooser.getSelectedFile();
				
				SwingUtilities.invokeLater(new Runnable() {
					
		            @Override
		            public void run() {
		            	fridaPath.setText(fridaPathFile.getAbsolutePath());
		            }
				
				});
				
			}
			
		} else if(command.equals("fridaPathSelectDefaultFile")) {
			
			JFrame parentFrame = new JFrame();
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Select location for Frida default JS file");
			
			int userSelection = fileChooser.showSaveDialog(parentFrame);
			
			if(userSelection == JFileChooser.APPROVE_OPTION) {
				
				final File fridaPathFile = fileChooser.getSelectedFile();
				
				try {
					InputStream inputStream = getClass().getClassLoader().getResourceAsStream("res/scriptBridaDefault.js");
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream ));
					File outputFile = fridaPathFile;
					
					FileWriter fr = new FileWriter(outputFile);
					BufferedWriter br  = new BufferedWriter(fr);
					
					String s;
					while ((s = reader.readLine())!=null) {
						
						br.write(s);
						br.newLine();
						
					}
					reader.close();
					br.close();
					
					SwingUtilities.invokeLater(new Runnable() {
						
			            @Override
			            public void run() {
			            	fridaPath.setText(fridaPathFile.getAbsolutePath());
			            }
					
					});
					
				} catch(Exception e) {
					
					printException(e,"Error copying Frida default JS file");
					
				}
				
			}
			
		} else if(command.startsWith("changeReturnValue")) {
			
        	Pattern p = Pattern.compile("^changeReturnValue(.*)$", Pattern.DOTALL);
    		Matcher m = p.matcher(command);
    		
    		String changeType = null;
    		if(m.find()) {
    			changeType = m.group(1);
    		}

    		if(changeType != null) {
    			
    			String dialogResult = JOptionPane.showInputDialog(mainPanel, "Insert the new " + changeType + " return value","Return value",JOptionPane.QUESTION_MESSAGE);
    			changeReturnValue(changeType,dialogResult);
    			
    		}
			
		} else if(command.startsWith("clearConsole")) {
		
			SwingUtilities.invokeLater(new Runnable() {
				
	            @Override
	            public void run() {
	            	String newConsoleText = "<font color=\"green\">";
	        		newConsoleText = newConsoleText + "<b>**** Console cleared successfully ****</b><br/>";
	        		newConsoleText = newConsoleText + "</font><br/>";		
	        		
	        		pluginConsoleTextArea.setText(newConsoleText);	        		 	
	            	
	            }
			
			});
			
		} else if(command.startsWith("enablePlugin")) {
			
			// Plugin type
			CustomPlugin.CustomPluginType pluginType = null;
			if(customPluginTypePluginOptions.getSelectedItem().toString().equals("IHttpListener")) {
				pluginType = CustomPlugin.CustomPluginType.IHTTPLISTENER;
			} else if(customPluginTypePluginOptions.getSelectedItem().toString().equals("IMessageEditorTab")) {
				pluginType = CustomPlugin.CustomPluginType.IMESSAGEEDITORTAB;
			} else if(customPluginTypePluginOptions.getSelectedItem().toString().equals("IContextMenu")) {
				pluginType = CustomPlugin.CustomPluginType.ICONTEXTMENU;
			} else {
				pluginType = CustomPlugin.CustomPluginType.JBUTTON;
			}
			
			// Execute on
			CustomPlugin.CustomPluginExecuteOnValues customPluginExecuteOn = null;			
			if(customPluginExecuteOnRadioRequest.isSelected()) {
				customPluginExecuteOn = CustomPlugin.CustomPluginExecuteOnValues.REQUESTS;
			} else if(customPluginExecuteOnRadioResponse.isSelected()) {
				customPluginExecuteOn = CustomPlugin.CustomPluginExecuteOnValues.RESPONSES;
			} else if(customPluginExecuteOnRadioAll.isSelected()) {
				customPluginExecuteOn = CustomPlugin.CustomPluginExecuteOnValues.ALL;
			} else if(customPluginExecuteOnRadioContext.isSelected()) {
				customPluginExecuteOn = CustomPlugin.CustomPluginExecuteOnValues.CONTEXT;
			} else {
				customPluginExecuteOn = CustomPlugin.CustomPluginExecuteOnValues.BUTTON;
			}
			
			// Burp Suite tools (IHttpListener plugins only)
			ArrayList<Integer> customPluginTools = new ArrayList<Integer>();
			if(pluginType == CustomPlugin.CustomPluginType.IHTTPLISTENER) {
				if(customPluginToolsRepeater.isSelected())	customPluginTools.add(IBurpExtenderCallbacks.TOOL_REPEATER);
				if(customPluginToolsProxy.isSelected())	customPluginTools.add(IBurpExtenderCallbacks.TOOL_PROXY);
				if(customPluginToolsScanner.isSelected())	customPluginTools.add(IBurpExtenderCallbacks.TOOL_SCANNER);
				if(customPluginToolsIntruder.isSelected())	customPluginTools.add(IBurpExtenderCallbacks.TOOL_INTRUDER);
				if(customPluginToolsExtender.isSelected())	customPluginTools.add(IBurpExtenderCallbacks.TOOL_EXTENDER);
				if(customPluginToolsSequencer.isSelected())	customPluginTools.add(IBurpExtenderCallbacks.TOOL_SEQUENCER);
				if(customPluginToolsSpider.isSelected())	customPluginTools.add(IBurpExtenderCallbacks.TOOL_SPIDER);
			}
			
			// Execute (IHttpListener and IMessageEditorTab only)
			CustomPlugin.CustomPluginExecuteValues customPluginExecute = null;
			if(pluginType == CustomPlugin.CustomPluginType.IHTTPLISTENER || pluginType == CustomPlugin.CustomPluginType.IMESSAGEEDITORTAB) {
				if(customPluginExecuteWhenOptions.getSelectedItem().toString().equals("always")) {
					customPluginExecute = CustomPluginExecuteValues.ALWAYS;
				} else if(customPluginExecuteWhenOptions.getSelectedItem().toString().equals("when request/response contains plaintext")) {
					customPluginExecute = CustomPluginExecuteValues.PLAINTEXT;
				} else {
					customPluginExecute = CustomPluginExecuteValues.REGEX;
				}
			}
			
			// Plugin platform (JButton only)
			int pluginPlatform = 0;
			if(pluginType == CustomPlugin.CustomPluginType.JBUTTON) {
				if(customPluginButtonTypeRadioIos.isSelected()) {
					pluginPlatform = PLATFORM_IOS;
				} else if(customPluginButtonTypeRadioAndroid.isSelected()) {
					pluginPlatform = PLATFORM_ANDROID;
				} else {
					pluginPlatform = PLATFORM_GENERIC;
				}
			}
			
			// Hook or function (JButton only)
			boolean hookOrFunction = false;
			if(pluginType == CustomPlugin.CustomPluginType.JBUTTON) {
				if(customPluginButtonTypeRadioFunction.isSelected()) {
					hookOrFunction = false;
				} else {
					hookOrFunction = true;
				}
			}
			
			// Parameters
			CustomPluginParameterValues customPluginParameter = null;
			if(customPluginParametersOptions.getSelectedItem().toString().equals("none")) {
				customPluginParameter = CustomPlugin.CustomPluginParameterValues.NONE;
			} else if(customPluginParametersOptions.getSelectedItem().toString().equals("complete request/response")) {
				customPluginParameter = CustomPlugin.CustomPluginParameterValues.COMPLETE;
			} else if(customPluginParametersOptions.getSelectedItem().toString().equals("headers")) {
				customPluginParameter = CustomPlugin.CustomPluginParameterValues.HEADERS;
			} else if(customPluginParametersOptions.getSelectedItem().toString().equals("body")) {
				customPluginParameter = CustomPlugin.CustomPluginParameterValues.BODY;
			} else if(customPluginParametersOptions.getSelectedItem().toString().equals("highlighted value in request/response")) {
				customPluginParameter = CustomPlugin.CustomPluginParameterValues.CONTEXT;
			} else if(customPluginParametersOptions.getSelectedItem().toString().equals("regex (with parenthesis)")) {
				customPluginParameter = CustomPlugin.CustomPluginParameterValues.REGEX;
			} else if(customPluginParametersOptions.getSelectedItem().toString().equals("fixed (#,# as separator)")) {
				customPluginParameter = CustomPlugin.CustomPluginParameterValues.FIXED;
			} else {
				customPluginParameter = CustomPlugin.CustomPluginParameterValues.POPUP;
			}
			
			// Parameter encoding
			CustomPlugin.CustomPluginEncodingValues customPluginParameterEncoding = null;
			if(customPluginParameterEncodingRadioPlain.isSelected()) {
				customPluginParameterEncoding = CustomPlugin.CustomPluginEncodingValues.PLAIN;
			} else if(customPluginParameterEncodingRadioBase64.isSelected()) {
				customPluginParameterEncoding = CustomPlugin.CustomPluginEncodingValues.BASE64;
			} else {
				customPluginParameterEncoding = CustomPlugin.CustomPluginEncodingValues.ASCII_HEX;
			}
			
			// Decode Frida output
			CustomPlugin.CustomPluginEncodingValues customPluginOutputDecoding = null;
			if(customPluginOutputDecodingRadioNone.isSelected()) {
				customPluginOutputDecoding = CustomPlugin.CustomPluginEncodingValues.PLAIN;
			} else if(customPluginOutputDecodingRadioBase64.isSelected()) {
				customPluginOutputDecoding = CustomPlugin.CustomPluginEncodingValues.BASE64;
			} else {
				customPluginOutputDecoding = CustomPlugin.CustomPluginEncodingValues.ASCII_HEX;
			}
			
			// Plugin output
			CustomPluginFunctionOutputValues customPluginFunctionOutput = null;
			if(customPluginOutputOptions.getSelectedItem().toString().equals("print in Brida console")) {
				customPluginFunctionOutput = CustomPlugin.CustomPluginFunctionOutputValues.BRIDA;
			} else if(customPluginOutputOptions.getSelectedItem().toString().equals("replace highlighted value in request/response")) {
				customPluginFunctionOutput = CustomPlugin.CustomPluginFunctionOutputValues.CONTEXT;
			} else if(customPluginOutputOptions.getSelectedItem().toString().equals("replace in request/response with regex (with parenthesys)")) {
				customPluginFunctionOutput = CustomPlugin.CustomPluginFunctionOutputValues.REGEX;
			} else {
				customPluginFunctionOutput = CustomPlugin.CustomPluginFunctionOutputValues.MESSAGE_EDITOR;
			}
			
			// Encode output
			CustomPlugin.CustomPluginEncodingValues customPluginOutputEncoding = null;
			if(customPluginOutputEncodingRadioNone.isSelected()) {
				customPluginOutputEncoding = CustomPlugin.CustomPluginEncodingValues.PLAIN;
			} else if(customPluginOutputEncodingRadioBase64.isSelected()) {
				customPluginOutputEncoding = CustomPlugin.CustomPluginEncodingValues.BASE64;
			} else {
				customPluginOutputEncoding = CustomPlugin.CustomPluginEncodingValues.ASCII_HEX;
			}
			
			// Encode value edited message before passing to Frida function executed on edited content (IMessageEditorTab only)
			CustomPlugin.CustomPluginEncodingValues customPluginFridaInputEncodingEditedContent = null;
			if(pluginType == CustomPlugin.CustomPluginType.IMESSAGEEDITORTAB) {
				if(customPluginMessageEditorModifiedEncodingInputFridaRadioNone.isSelected()) {
					customPluginFridaInputEncodingEditedContent = CustomPlugin.CustomPluginEncodingValues.PLAIN;
				} else if(customPluginMessageEditorModifiedEncodingInputFridaRadioBase64.isSelected()) {
					customPluginFridaInputEncodingEditedContent = CustomPlugin.CustomPluginEncodingValues.BASE64;
				} else {
					customPluginFridaInputEncodingEditedContent = CustomPlugin.CustomPluginEncodingValues.ASCII_HEX;
				}
			}
			
			// Decode output of Frida function executed on edited content (IMessageEditorTab only)
			CustomPlugin.CustomPluginEncodingValues customPluginOutputDecodingEditedContent = null;
			if(pluginType == CustomPlugin.CustomPluginType.IMESSAGEEDITORTAB) {
				if(customPluginOutputMessageEditorModifiedDecodingRadioNone.isSelected()) {
					customPluginOutputDecodingEditedContent = CustomPlugin.CustomPluginEncodingValues.PLAIN;
				} else if(customPluginOutputMessageEditorModifiedDecodingRadioBase64.isSelected()) {
					customPluginOutputDecodingEditedContent = CustomPlugin.CustomPluginEncodingValues.BASE64;
				} else {
					customPluginOutputDecodingEditedContent = CustomPlugin.CustomPluginEncodingValues.ASCII_HEX;
				}
			}
			
			// Message editor output location (IMessageEditorTab only)
			BridaMessageEditorPlugin.BridaMessageEditorPluginOutputLocation customPluginEditedMessageOutputLocation = null;
			if(pluginType == CustomPlugin.CustomPluginType.IMESSAGEEDITORTAB) {
				if(customPluginMessageEditorModifiedOutputLocationOptions.getSelectedItem().toString().equals("Discard (view only mode)")) {
					customPluginEditedMessageOutputLocation = BridaMessageEditorPlugin.BridaMessageEditorPluginOutputLocation.NONE;
				} else if(customPluginMessageEditorModifiedOutputLocationOptions.getSelectedItem().toString().equals("Print in Brida console and return original request/response")) {
					customPluginEditedMessageOutputLocation = BridaMessageEditorPlugin.BridaMessageEditorPluginOutputLocation.CONSOLE;
				} else if(customPluginMessageEditorModifiedOutputLocationOptions.getSelectedItem().toString().equals("Replace complete request/response")) {
					customPluginEditedMessageOutputLocation = BridaMessageEditorPlugin.BridaMessageEditorPluginOutputLocation.COMPLETE;
				} else if(customPluginMessageEditorModifiedOutputLocationOptions.getSelectedItem().toString().equals("Replace request/response body")) {
					customPluginEditedMessageOutputLocation = BridaMessageEditorPlugin.BridaMessageEditorPluginOutputLocation.BODY;
				} else {
					customPluginEditedMessageOutputLocation = BridaMessageEditorPlugin.BridaMessageEditorPluginOutputLocation.REGEX;
				}
			}
			
			// Encode output of Frida function executed on edited content (IMessageEditorTab only)
			CustomPlugin.CustomPluginEncodingValues customPluginEditedFunctionOutputEncoding = null;
			if(pluginType == CustomPlugin.CustomPluginType.IMESSAGEEDITORTAB) {
				if(customPluginMessageEditorModifiedOutputEncodingRadioNone.isSelected()) {
					customPluginEditedFunctionOutputEncoding = CustomPlugin.CustomPluginEncodingValues.PLAIN;
				} else if(customPluginMessageEditorModifiedOutputEncodingRadioBase64.isSelected()) {
					customPluginEditedFunctionOutputEncoding = CustomPlugin.CustomPluginEncodingValues.BASE64;
				} else {
					customPluginEditedFunctionOutputEncoding = CustomPlugin.CustomPluginEncodingValues.ASCII_HEX;
				}
			}
			
			CustomPlugin newCustomPlugin;
			if(pluginType == CustomPlugin.CustomPluginType.IHTTPLISTENER) {
				BridaHttpListenerPlugin newPlugin = new BridaHttpListenerPlugin(customPluginTools,
						customPluginScopeCheckBox.isSelected(),
						this,
						customPluginNameText.getText(),
						customPluginExportNameText.getText(),
						customPluginExecuteOn,
						customPluginExecuteOnStringParameter.getText(),
						customPluginExecute,
						customPluginExecuteWhenText.getText(),
						customPluginParameter,
						customPluginParametersText.getText(),
						customPluginParameterEncoding,
						customPluginFunctionOutput,
						customPluginOutputText.getText(),
						customPluginOutputEncoding,
						customPluginOutputDecoding);
				
				newCustomPlugin = newPlugin;
								
			} else if(pluginType == CustomPlugin.CustomPluginType.ICONTEXTMENU) {
				BridaContextMenuPlugin newPlugin = new BridaContextMenuPlugin(this,
						customPluginNameText.getText(),
						customPluginExportNameText.getText(),
						customPluginExecuteOn,
						customPluginExecuteOnStringParameter.getText(),
						customPluginParameter,
						customPluginParametersText.getText(),
						customPluginParameterEncoding,
						customPluginFunctionOutput,
						customPluginOutputText.getText(),
						customPluginOutputEncoding,
						customPluginOutputDecoding);
				
				newCustomPlugin = newPlugin;
								
			} else if(pluginType == CustomPlugin.CustomPluginType.JBUTTON) {
				BridaButtonPlugin newPlugin = new BridaButtonPlugin(pluginPlatform,
						hookOrFunction,
						this,
						customPluginNameText.getText(),
						customPluginExportNameText.getText(),
						customPluginExecuteOn,
						customPluginExecuteOnStringParameter.getText(),
						customPluginParameter,
						customPluginParametersText.getText(),
						customPluginParameterEncoding,
						customPluginFunctionOutput,
						customPluginOutputText.getText(),
						customPluginOutputEncoding,
						customPluginOutputDecoding);
				
				newCustomPlugin = newPlugin;
								
			} else {
				
				BridaMessageEditorPlugin newPlugin = new BridaMessageEditorPlugin(customPluginEditedMessageOutputLocation,
						customPluginMessageEditorModifiedOutputLocationText.getText(),
						customPluginFridaInputEncodingEditedContent,
						customPluginOutputDecodingEditedContent,
						customPluginMessageEditorModifiedFridaExportNameText.getText(),
						customPluginEditedFunctionOutputEncoding,
						this,
						customPluginNameText.getText(),
						customPluginExportNameText.getText(),
						customPluginExecuteOn,
						customPluginExecuteOnStringParameter.getText(),
						customPluginExecute,
						customPluginExecuteWhenText.getText(),
						customPluginParameter,
						customPluginParametersText.getText(),
						customPluginParameterEncoding,
						customPluginFunctionOutput,
						customPluginOutputText.getText(),
						customPluginOutputEncoding,
						customPluginOutputDecoding);
				
				newCustomPlugin = newPlugin;
								
			}
			
			List<CustomPlugin> customPlugins = ((CustomPluginsTableModel)(customPluginsTable.getModel())).getCustomPlugins();
			synchronized(customPlugins) {
				int customPluginsOldSize = customPlugins.size();
				customPlugins.add(newCustomPlugin);
				((CustomPluginsTableModel)(customPluginsTable.getModel())).fireTableRowsInserted(customPluginsOldSize, customPlugins.size() - 1);
			}			
						
		}
		
	}

	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
		
		if(invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST ||
		   invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_RESPONSE) {
		
			currentInvocation = invocation;
			
			List<JMenuItem> menu = new ArrayList<JMenuItem>();
			
			JMenuItem itemCustom1 = new JMenuItem("Brida Custom 1");
			itemCustom1.setActionCommand("contextcustom1");
			itemCustom1.addActionListener(this);
			
			JMenuItem itemCustom2 = new JMenuItem("Brida Custom 2");
			itemCustom2.setActionCommand("contextcustom2");
			itemCustom2.addActionListener(this);		
			
			menu.add(itemCustom1);
			menu.add(itemCustom2);
			
			return menu;
			
		} else if(invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_VIEWER_REQUEST ||
				  invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_VIEWER_RESPONSE) { 
			
			currentInvocation = invocation;
			
			List<JMenuItem> menu = new ArrayList<JMenuItem>();
			
			JMenuItem itemCustom3 = new JMenuItem("Brida Custom 3");
			itemCustom3.setActionCommand("contextcustom3");
			itemCustom3.addActionListener(this);
			
			JMenuItem itemCustom4 = new JMenuItem("Brida Custom 4");
			itemCustom4.setActionCommand("contextcustom4");
			itemCustom4.addActionListener(this);		
			
			menu.add(itemCustom3);
			menu.add(itemCustom4);
			
			return menu;
		
		
		} else {
		
			return null;
			
		}
		
	}
	
	public static String byteArrayToHexString(byte[] raw) {
        StringBuilder sb = new StringBuilder(2 + raw.length * 2);
        for (int i = 0; i < raw.length; i++) {
            sb.append(String.format("%02X", Integer.valueOf(raw[i] & 0xFF)));
        }
        return sb.toString();
    }
	
	public static byte[] hexStringToByteArray(String hex) {
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0; i < b.length; i++){
          int index = i * 2;
          int v = Integer.parseInt(hex.substring(index, index + 2), 16);
          b[i] = (byte)v;
        }
        return b;
   }
	
	private void retrieveClassMethods(DefaultMutableTreeNode clickedNode) {
		
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)clickedNode.getParent();
		
		if(parent != null) {
			
			String nodeContentParent = (String)parent.getUserObject();
			String nodeContent = (String)clickedNode.getUserObject();
			
			if(nodeContentParent.equals("Modules")) {
								
				HashMap<String, Integer> currentImports;
				try {
					currentImports = (HashMap<String,Integer>)(pyroBridaService.call("callexportfunction","getmoduleimports",new String[] {nodeContent}));
				} catch (Exception e) {
					printException(e,"Exception retrieving module imports");
					return;
				} 
				
				if(currentImports != null) {
					
					ArrayList<String> importNames = new ArrayList<String>(currentImports.keySet());
					
					// Sort import names
					Collections.sort(importNames, new Comparator<String>() {
				        @Override
				        public int compare(String class1, String class2)
				        {

				            return  class1.compareToIgnoreCase(class2);
				        }
				    });	
										
					DefaultMutableTreeNode importNode = new DefaultMutableTreeNode("Imports");
					
					Iterator<String> currentImportsIterator = importNames.iterator(); 
					
					String currentImportName;
					DefaultMutableTreeNode currentNodeImport;
					while(currentImportsIterator.hasNext()) {
						
						currentImportName = currentImportsIterator.next();
																
						currentNodeImport = new DefaultMutableTreeNode(currentImportName);
						
						importNode.add(currentNodeImport);
						
					}
					
					clickedNode.add(importNode);
					
				}
				
				HashMap<String, Integer> currentExports;
				try {
					currentExports = (HashMap<String,Integer>)(pyroBridaService.call("callexportfunction","getmoduleexports",new String[] {nodeContent}));
				} catch (Exception e) {
					printException(e,"Exception retrieving module exports");
					return;
				} 
				
				if(currentExports != null) {
					
					ArrayList<String> exportNames = new ArrayList<String>(currentExports.keySet());
					
					// Sort export names
					Collections.sort(exportNames, new Comparator<String>() {
				        @Override
				        public int compare(String class1, String class2)
				        {

				            return  class1.compareToIgnoreCase(class2);
				        }
				    });	
					
					DefaultMutableTreeNode exportNode = new DefaultMutableTreeNode("Exports");
					
					Iterator<String> currentExportsIterator = exportNames.iterator(); 
					
					String currentExportName;
					DefaultMutableTreeNode currentNodeExport;
					while(currentExportsIterator.hasNext()) {
						
						currentExportName = currentExportsIterator.next();
																
						currentNodeExport = new DefaultMutableTreeNode(currentExportName);
						
						exportNode.add(currentNodeExport);
						
					}
					
					clickedNode.add(exportNode);
					
				}
				
			} else if (nodeContentParent.equals("Objective-C") || nodeContentParent.equals("Java")) {
								
				HashMap<String, Integer> currentClassMethods = null;
				ArrayList<String> methodNames = null;

				try {
					currentClassMethods = (HashMap<String,Integer>)(pyroBridaService.call("callexportfunction","getclassmethods",new String[] {nodeContent}));
				} catch (Exception e) {
					printException(e,"Exception retrieving class methods");
					return;
				}
				
				if(currentClassMethods != null) {
					
					methodNames = new ArrayList<String>(currentClassMethods.keySet());
					
					if(platform == BurpExtender.PLATFORM_ANDROID) {
					
						// Sort Android method names
						Collections.sort(methodNames, new Comparator<String>() {
					        @Override
					        public int compare(String method1, String method2) {
	
					        	String[] splitMethod1 = method1.split("\\(")[0].split(" ");
					        	String[] splitMethod2 = method2.split("\\(")[0].split(" ");
					        	
					            return  splitMethod1[splitMethod1.length-1].compareToIgnoreCase(splitMethod2[splitMethod1.length-1]);
					            
					        }
					    });
						
					} else {
						
						// Sort iOS method names
						Collections.sort(methodNames, new Comparator<String>() {
					        @Override
					        public int compare(String class1, String class2)
					        {

					            return  class1.compareToIgnoreCase(class2);
					        }
					    });
						
					}
				
					Iterator<String> currentClassMethodsIterator = methodNames.iterator(); 
					
					String currentMethodName;
					DefaultMutableTreeNode currentNodeMethod;
					while(currentClassMethodsIterator.hasNext()) {
												
						currentMethodName = currentClassMethodsIterator.next();
										
						currentNodeMethod = new DefaultMutableTreeNode(currentMethodName);
						
						clickedNode.add(currentNodeMethod);
						
					}
					
				} 				
					
			}			
			
			DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
			model.reload(clickedNode);
			
			tree.expandPath(new TreePath(clickedNode.getPath()));
						
		}
		
	}

	public void trap(boolean withBacktrace) {		

		DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode)(tree.getSelectionPath().getLastPathComponent());
		
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)clickedNode.getParent();
		
		String type = null;	
		String pattern = null;
		
		// ROOT
		if(parentNode != null) {
		
			String parentNodeContent = (String)parentNode.getUserObject();			
			
			DefaultMutableTreeNode grandparentNode = null;
	
			switch(parentNodeContent) {
			
				// Clicked Java class
				case "Java":
					type = "java_class";
					pattern = (String)clickedNode.getUserObject();
					break;
					
				// Clicked a iOS class	
				case "Objective-C":
					type = "objc_class";
					pattern = (String)clickedNode.getUserObject();
					break;
					
				// Clicked an export (the same for iOS and Android)
				case "Exports":
					
					// Only functions can be trapped
					if(((String)clickedNode.getUserObject()).startsWith("function")) {
						
						type = "export";						
						grandparentNode = (DefaultMutableTreeNode)parentNode.getParent();
						pattern = (String)grandparentNode.getUserObject() + "!" + ((String)clickedNode.getUserObject()).replace("function: ", "");
												
					}
					
					break;
					
				default:
					
					grandparentNode = (DefaultMutableTreeNode)parentNode.getParent();
					
					if(grandparentNode != null) {
						
						String grandparentNodeContent = (String)grandparentNode.getUserObject();
						
						// Clicked a iOS method
						if(grandparentNodeContent.equals("Objective-C")) {
														
							type = "objc_method";
							
							pattern = (String)clickedNode.getUserObject();
						
						// Clicked a Java method 
						} else if(grandparentNodeContent.equals("Java")) {
							
							type = "java_method";						
							
							pattern = (String)clickedNode.getUserObject();
														
						}
						
					}				
					
					break;
			
			}
			
		}
		
		if(type != null) {
				
			try {
				
				pyroBridaService.call("callexportfunction","trace",new String[] {pattern,type,(withBacktrace ? "true" : "false")});
								
				List<TrapTableItem> trapEntries = ((TrapTableModel)(trapTable.getModel())).getTrappedMethods();
	
				HashMap<String,Integer> currentClassMethods = null;
				
				// Better outside synchronized block
				if(type.equals("objc_class") || type.equals("java_class")) {
	        		currentClassMethods = (HashMap<String,Integer>)(pyroBridaService.call("callexportfunction","getclassmethods",new String[] {pattern}));
				}
				
	            synchronized(trapEntries) {
	            	int trapEntryOldSize = trapEntries.size();
	            	if(type.equals("objc_class")  || type.equals("java_class")) {
	            		if(currentClassMethods != null) {    					
	    					ArrayList<String> methodNames = new ArrayList<String>(currentClassMethods.keySet());
	    					Iterator<String> currentClassMethodsIterator = methodNames.iterator();     					
	    					String currentMethodName;
	    					while(currentClassMethodsIterator.hasNext()) {    						
	    						currentMethodName = currentClassMethodsIterator.next();    										
	    						trapEntries.add(new TrapTableItem("Inspect",(platform == BurpExtender.PLATFORM_ANDROID ? "Java" : "OBJ-C"),currentMethodName, withBacktrace,"-","-"));    						
	    					}    					
	    				}            		
	            	} else if(type.equals("objc_method") || type.equals("java_method")) {
	            		trapEntries.add(new TrapTableItem("Inspect",(platform == BurpExtender.PLATFORM_ANDROID ? "Java" : "OBJ-C"),pattern, withBacktrace,"-","-"));
	            	} else {
	            		trapEntries.add(new TrapTableItem("Inspect","Export",pattern, withBacktrace,"-","-"));
	            	}
	            	((TrapTableModel)(trapTable.getModel())).fireTableRowsInserted(trapEntryOldSize, trapEntries.size() - 1);
	            } 
							
			} catch (Exception e) {
				
				printException(e,"Exception with trap");
				
			}
			
		}
		
	}
	
	public void changeReturnValue(String returnValueType, String dialogResult) {

		DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode)(tree.getSelectionPath().getLastPathComponent());
		
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)clickedNode.getParent();
		
		String type = null;	
		String pattern = null;
		
		// ROOT
		if(parentNode != null) {
		
			String parentNodeContent = (String)parentNode.getUserObject();			
			
			DefaultMutableTreeNode grandparentNode = null;
	
			switch(parentNodeContent) {
					
				// Clicked a export
				case "Exports":
					
					// Only functions can be trapped
					if(((String)clickedNode.getUserObject()).startsWith("function")) {
						
						type = "export";						
						grandparentNode = (DefaultMutableTreeNode)parentNode.getParent();
						pattern = (String)grandparentNode.getUserObject() + "!" + ((String)clickedNode.getUserObject()).replace("function: ", "");
												
					}
					
					break;
					
				default:
					
					grandparentNode = (DefaultMutableTreeNode)parentNode.getParent();
					
					if(grandparentNode != null) {
						
						String grandparentNodeContent = (String)grandparentNode.getUserObject();
						
						// Clicked a iOS method
						if(grandparentNodeContent.equals("Objective-C")) {
							
							type = "objc_method";
							pattern = (String)clickedNode.getUserObject();
						
						// Clicked a Java method
						} else if(grandparentNodeContent.equals("Java")) {
							
							type = "java_method";
							pattern = (String)clickedNode.getUserObject();
							
						}						
						
					}				
					
					break;
			
			}
			
		}
		
		if(type != null) {
				
			try {
				
				pyroBridaService.call("callexportfunction","changereturnvalue",new String[] {pattern,type,returnValueType,dialogResult});
								
				List<TrapTableItem> trapEntries = ((TrapTableModel)(trapTable.getModel())).getTrappedMethods();
					
	            synchronized(trapEntries) {
	            	int trapEntryOldSize = trapEntries.size();
	            	if(type.equals("objc_method")) {
	            		trapEntries.add(new TrapTableItem("Edit return","OBJ-C",pattern, false,returnValueType,dialogResult));
	            	} else if(type.equals("java_method")) {
	            		trapEntries.add(new TrapTableItem("Edit return","Java",pattern, false,returnValueType,dialogResult));
	            	} else {
	            		trapEntries.add(new TrapTableItem("Edit return","Export",pattern, false,returnValueType,dialogResult));
	            	}
	            	((TrapTableModel)(trapTable.getModel())).fireTableRowsInserted(trapEntryOldSize, trapEntries.size() - 1);
	            } 
					
				
			} catch (Exception e) {
				
				printException(e,"Exception with replace return value");
				
			}
			
		}
		
	}
	
	private void generatePopup(MouseEvent e){
		TreePopup menu = new TreePopup(this);
		menu.show(e.getComponent(), e.getX(), e.getY());
    }
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
		// Double click -> EXPAND
		if (e.getClickCount() == 2) {
								
			//stdout.println("CLICK: " + tree.getSelectionPath().getLastPathComponent().getClass());
			
			DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode)(tree.getSelectionPath().getLastPathComponent());
			
			retrieveClassMethods(clickedNode);

        }
			
	}

	@Override
	public void mousePressed(MouseEvent e) {

		if(e.isPopupTrigger()) {
			int row = tree.getClosestRowForLocation(e.getX(), e.getY());
            tree.setSelectionRow(row);
			generatePopup(e);
		}
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		if(e.isPopupTrigger()) {
			int row = tree.getClosestRowForLocation(e.getX(), e.getY());
            tree.setSelectionRow(row);
			generatePopup(e);
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
	
	public void printSuccessMessage(final String message) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
            @Override
            public void run() {
            	
            	String oldConsoleText = pluginConsoleTextArea.getText();
            	
        		Pattern p = Pattern.compile("^.*<body>(.*)</body>.*$", Pattern.DOTALL);
        		Matcher m = p.matcher(oldConsoleText);
        		
        		String newConsoleText = "";
        		if(m.find()) {
        			newConsoleText = m.group(1);
        		}        		
        		        		
        		if(lastPrintIsJS) {
        			newConsoleText = newConsoleText + "<br/>";
        		}
        		
        		newConsoleText = newConsoleText + "<font color=\"green\">";
        		newConsoleText = newConsoleText + "<b>" + message + "</b><br/>";
        		newConsoleText = newConsoleText + "</font><br/>";
        		
        		pluginConsoleTextArea.setText(newConsoleText);
            	
        		lastPrintIsJS = false;
            	
            }
		
		});
		
	}
	
	
	public void printJSMessage(final String message) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
            @Override
            public void run() {
        		
            	String oldConsoleText = pluginConsoleTextArea.getText();
            	Pattern p = Pattern.compile("^.*<body>(.*)</body>.*$", Pattern.DOTALL);
        		Matcher m = p.matcher(oldConsoleText);
        		
        		String newConsoleText = "";
        		if(m.find()) {
        			newConsoleText = m.group(1);
        		}           	
        		
        		newConsoleText = newConsoleText + "<font color=\"black\"><pre>";
        		//newConsoleText = newConsoleText + message + "<br/>";
        		newConsoleText = newConsoleText + message;
        		newConsoleText = newConsoleText + "</pre></font>";
        		
        		pluginConsoleTextArea.setText(newConsoleText);
        		
        		lastPrintIsJS = true;            	
            	
            }
		
		});
		
	}
	
	
	public void printException(final Exception e, final String message) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
            @Override
            public void run() {
        		
        		
            	String oldConsoleText = pluginConsoleTextArea.getText();
            	Pattern p = Pattern.compile("^.*<body>(.*)</body>.*$", Pattern.DOTALL);
        		Matcher m = p.matcher(oldConsoleText);
        		
        		String newConsoleText = "";
        		if(m.find()) {
        			newConsoleText = m.group(1);
        		}
        		        		
        		if(lastPrintIsJS) {
        			newConsoleText = newConsoleText + "<br/>";
        		}
        		
        		newConsoleText = newConsoleText + "<font color=\"red\">";
        		newConsoleText = newConsoleText + "<b>" + message + "</b><br/>";
        		
        		if(e != null) {        		
	        		newConsoleText = newConsoleText + e.toString() + "<br/>";
	        		//consoleText = consoleText + e.getMessage() + "<br/>";
	        		StackTraceElement[] exceptionElements = e.getStackTrace();
	        		for(int i=0; i< exceptionElements.length; i++) {
	        			newConsoleText = newConsoleText + exceptionElements[i].toString() + "<br/>";
	        		}		
        		}
        		
        		newConsoleText = newConsoleText + "</font><br/>";
        		
        		pluginConsoleTextArea.setText(newConsoleText);
        		
        		lastPrintIsJS = false;            	
            	
            }
		
		});
		
	}
	
	public int getPlatform() {
		return platform;
	}

	@Override
	public void extensionUnloaded() {

		if(serverStarted) {
		
			stdoutThread.stop();
			stderrThread.stop();
			
			try {
				pyroBridaService.call("shutdown");
				pyroServerProcess.destroy();
				pyroBridaService.close();
				
				printSuccessMessage("Pyro server shutted down");
				
			} catch (final Exception e) {
				
				printException(e,"Exception shutting down Pyro server");
				
			}
			
		}
		
	}
	
	public boolean removeButtonFromHooksAndFunctions(JPanel buttonPanelToRemove, DefaultHook dh) {
		
		// Disable the hook, if enabled and if possible
		if(dh.isEnabled() && dh.isInterceptorHook() && applicationSpawned) {
			printException(null,"Could not unload a single hook while application is running. Detach hook first by stopping application or by detatching all the hooks and then remove the button");
			return false;
		} else if(dh.isEnabled() && dh.isInterceptorHook()) {
			printSuccessMessage("Hook " + dh.getName() + " is enabled. It will be disabled.");
			dh.setEnabled(false);
		}
		
		// Removing the button
		if(dh.getOs() == BurpExtender.PLATFORM_ANDROID) {
			SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	            	androidHooksPanel.remove(buttonPanelToRemove);
	            	androidHooksPanel.revalidate();
	            	androidHooksPanel.repaint();
	            }
			});
		} else if(dh.getOs() == BurpExtender.PLATFORM_IOS) {
			SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	            	iOSHooksPanel.remove(buttonPanelToRemove);
	            	iOSHooksPanel.revalidate();
	            	iOSHooksPanel.repaint();
	            }
			});
		} else {
			SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	            	genericHooksPanel.remove(buttonPanelToRemove);
	            	genericHooksPanel.revalidate();
	            	genericHooksPanel.repaint();
	            }
			});
		}
		
		defaultHooks.remove(dh);
		return true;
		
	}	
	
	public JPanel addButtonToHooksAndFunctions(DefaultHook dh) {
    	
        JLabel tempHookLabel = new JLabel(dh.getName());                    
        
        JPanel lineJPanel = new JPanel();
        lineJPanel.setLayout(new BoxLayout(lineJPanel, BoxLayout.X_AXIS));
        lineJPanel.setAlignmentX(Component.LEFT_ALIGNMENT);     
        
        if(dh.isInterceptorHook()) {
        
            final JToggleButton tempHookToggleButton = new JToggleButton("Enable",false);
            tempHookToggleButton.addActionListener(new ActionListener() {
            	public void actionPerformed(ActionEvent actionEvent) {
            		
            		// Enabling hook
            		if(tempHookToggleButton.isSelected()) {
            			
            			if(applicationSpawned) {
            				            				
            				// Call hook
            				try {
            					pyroBridaService.call("callexportfunction",dh.getFridaExportName(),new String[0]);
                				printSuccessMessage("Hook " + dh.getName() + " ENABLED");
                				dh.setEnabled(true);
							} catch (Exception e) {
								printException(e,"Error while enabling hook " + dh.getName());
							} 
            			} else {
            				
            				printSuccessMessage("Hook " + dh.getName() + " ENABLED");
            				dh.setEnabled(true);
            				
            			}
            		
            		// Disabling hook	
            		} else {
            			
            			if(applicationSpawned) {
            			
            				printException(null,"It is not possible to detach a single hook while app is running (you can detach ALL the hooks with the \"Detach all\" button)");
                			tempHookToggleButton.setSelected(true);
                			
            			} else {
            				
                			printSuccessMessage("Hook " + dh.getName() + " DISABLED");
                			dh.setEnabled(false);
            				
            			}
                			
            		}
            	}
            });
            
            lineJPanel.add(tempHookToggleButton);
            
        } else {
        	
        	JButton tempHookButton = new JButton("Execute");
        	tempHookButton.addActionListener(new ActionListener() {
            	public void actionPerformed(ActionEvent actionEvent) {

            		if(applicationSpawned) {
	            		// Parameters
	    				String[] currentParameters;
	    				if(dh.isPopupParameters()) {
	    					String parametersPopup = JOptionPane.showInputDialog("Enter parameter(s), delimited by \"#,#\"");
	    					currentParameters = parametersPopup.split("#,#");
	    					for(int i=0;i<currentParameters.length;i++) {
        						currentParameters[i] = CustomPlugin.encodeCustomPluginValue(currentParameters[i].getBytes(),dh.getParametersEncoding());
        					}
	    				} else {
        					// For cases different from POPUP parameters are already encoded	    					
	    					currentParameters = dh.getParameters();
	    				}
	    				// Call exported function
	    				try {
	    					printJSMessage("*** Output " + dh.getName() + ":");
	    					String ret = (String)pyroBridaService.call("callexportfunction",dh.getFridaExportName(),currentParameters);
	    					printJSMessage("* Ret value: " + ret);
						} catch (Exception e) {
							printException(e,"Error while running function " + dh.getName());
						} 	  
            		} else {
            			
            			printException(null,"Error, start Pyro server and spawn application first.");
            			
            		}
            	}
            	
        	});
        	
            lineJPanel.add(tempHookButton);
        	
        }
        
        lineJPanel.add(tempHookLabel);                    
        
        if(dh.getOs() == BurpExtender.PLATFORM_ANDROID) {
        	androidHooksPanel.add(lineJPanel);
        } else if(dh.getOs() == BurpExtender.PLATFORM_IOS) {
        	iOSHooksPanel.add(lineJPanel);
        } else {
        	genericHooksPanel.add(lineJPanel);
        }
        
        defaultHooks.add(dh);
        
        return lineJPanel;
        
	}
	
}

		 