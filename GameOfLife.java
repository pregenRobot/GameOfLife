import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.awt.image.BufferedImage;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import java.io.IOException;

public class GameOfLife extends JFrame implements ActionListener {
    
    private Thread gameThread;
    private Gameboard board = new Gameboard();
    
    //Default cell size
    private int SIZE_OF_CELL;
    private int SIZE_OF_FILL;
    //Default thread sleep time
    private int millisecondsPerFrame = 100;
    //State of the game
    private boolean gameRunning = false;
    //Default cell color
    private Color liveCellColor = new Color(158, 24, 14);
    //Default background color
    private Color boardColor = Color.DARK_GRAY;
    //Default board grid line color
    private Color lineColor = Color.BLACK;
    //Default console text
    private JLabel consoleOutput = new JLabel("CONSOLE");
    //Default grid Dimensions
    private int gridXDimension = 50;
    private int gridYDimension = 50;

    private JPanel toolBarPanel;
    private JPanel consolePanel;
    private BoardPanel boardPanel;
    private JPanel mainPanel;
    private JSlider fps;
    private JPanel controlPanel;
    private JButton recordButton;
    private JButton playButton;
    
    //Stores snapshot of the board
    private ArrayList<boolean[][]> recordedFrames = new ArrayList<boolean[][]>();

    //State of board recording
    private boolean isRecording = false;
    public static void main(String[] args) {
        
        // Create the outer window (frame)
        JFrame frame = new GameOfLife();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);
        frame.setTitle("Conway's Game of Life Studio");
        frame.setVisible(true);
    }
    
    public GameOfLife() {

        //Resets the board when clicked
        JButton refreshButton = new JButton("RESET");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.clearBoard();
                repaint();
                if(isRecording){
                    consoleOutput.setText("Board reset. Board is still being recorded.");
                }else{
                    consoleOutput.setText("Board reset");
                }
            }
        });
        
        //Starts recording the grid
        recordButton = new JButton("RECORD");
        recordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GifCreator gifCreator = new GifCreator();
                Thread t = new Thread(gifCreator);
                t.start();
            }
        });

        //Toggle playing
        playButton = new JButton("▶");
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameRunning) {
                    gameRunning = true;
                    gameThread = new Thread(boardPanel);
                    gameThread.start();
                    playButton.setText("⏸");
                    consoleOutput.setText("Played");
                } else {
                    gameRunning = false;
                    gameThread.interrupt();
                    playButton.setText("▶");
                    consoleOutput.setText("Paused");
                }
            }
        });
        
        //Advances one step
        JButton nextButton = new JButton(">");
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameRunning) {
                    board.deathAndBirth();
                    repaint();
                    consoleOutput.setText("Next Frame");
                    if (isRecording) {
                        recordedFrames.add(board.boardSnapshot());
                    }
                }
            }
        });
        
        //Adds a slider to control playback speed
        fps = new JSlider(JSlider.HORIZONTAL, 0, 30, 10);
        fps.addChangeListener(e -> {
            try {
                millisecondsPerFrame = (int) Math.ceil(1000 / fps.getValue());
            } catch (ArithmeticException error) {
                // If division by 0 divide by 1
                millisecondsPerFrame = (int) Math.ceil(1000 / 1);
            }
            if(isRecording){
                consoleOutput.setText("Changed framerate: " + fps.getValue() + "\n Only new frame rate will be used for generating recording");
            }else{
                consoleOutput.setText("Changed framerate: " + fps.getValue());
            }
        });
        //Formatting slider
        fps.setMajorTickSpacing(10);
        fps.setMinorTickSpacing(1);
        fps.setPaintTicks(true);
        fps.setPaintLabels(true);
        fps.setBackground(boardColor);
        fps.setForeground(Color.WHITE);
        fps.setSnapToTicks(true);
        
        //Creates and formats panel containing the refresh and record buttons
        toolBarPanel = new JPanel();
        toolBarPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        toolBarPanel.setBackground(boardColor);
        toolBarPanel.setLayout(new BoxLayout(toolBarPanel, BoxLayout.X_AXIS));
        BoxLayout toolbarLayout = new BoxLayout(toolBarPanel, BoxLayout.X_AXIS);
        toolBarPanel.setLayout(toolbarLayout);
        toolBarPanel.add(refreshButton);
        toolBarPanel.add(recordButton);
        toolBarPanel.setPreferredSize(new Dimension(1000, 50));
        
        JPanel hwWrapper = new JPanel();
        hwWrapper.setBorder(new EmptyBorder(0, 0, 10, 10));
        
        //Creates panel which contains the play/pause controls and the playback speed slider
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.setBackground(boardColor);
        controlPanel.add(playButton);
        controlPanel.add(nextButton);
        controlPanel.add(fps);
        controlPanel.setPreferredSize(new Dimension(1000, 100));
        
        //Creates panel with text which displays messages to user
        consolePanel = new JPanel();
        consolePanel.setLayout(new BoxLayout(consolePanel, BoxLayout.X_AXIS));
        consolePanel.setBackground(boardColor);
        consoleOutput.setBackground(Color.GRAY);
        consoleOutput.setForeground(Color.WHITE);
        consolePanel.setPreferredSize(new Dimension(1000, 50));
        consolePanel.add(consoleOutput);
        
        //Creates panel of the actual game board
        boardPanel = new BoardPanel();
        boardPanel.setBackground(boardColor);
        
        //Creates a panel which contains all panels created above 
        mainPanel = new JPanel();
        BoxLayout layoutParams = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
        mainPanel.setLayout(layoutParams);
        mainPanel.setBackground(boardColor);
        mainPanel.add(toolBarPanel);
        mainPanel.add(boardPanel);
        mainPanel.add(controlPanel);
        mainPanel.add(consolePanel);

        createMenuBar();
        //adds all panels to the Frame and sets visible
        add(mainPanel);
        setVisible(true);
        
    }
    private int modulo(int a, int b){
        int r = a % b;
        if(r<0){
            r += b;
        }
        return r;
    }
    
    public void changeBackGroundColor(Color c){
        //Changes all components background colour to c 
        consoleOutput.setBackground(c);
        toolBarPanel.setBackground(c);
        consolePanel.setBackground(c);
        boardPanel.setBackground(c);
        mainPanel.setBackground(c);
        fps.setBackground(c);
        controlPanel.setBackground(c);
        boardColor = c;
        
        int redComponent = c.getRed();
        int greenComponent = c.getGreen();
        int blueComponent = c.getBlue();
        
        //Changes console color
        if((redComponent + greenComponent + blueComponent)/3 < 128){
            consoleOutput.setForeground(Color.WHITE);
        }else{
            consoleOutput.setForeground(Color.BLACK);
        }
        
        //Computes the most ideal grid line color relative to background
        redComponent = modulo(((int)(redComponent*1.5)-1),255);
        greenComponent = modulo(((int)(greenComponent*1.5)-1),255);
        blueComponent = modulo(((int)(blueComponent*1.5)-1),255);
        lineColor = new Color(redComponent,greenComponent,blueComponent);
        repaint();
        
        if(isRecording){
            consoleOutput.setText("Changed background color to: RGB(" + redComponent + "," + greenComponent + "," + blueComponent + "). Final color of the recorded gif will be the current color.");
        }else{
            consoleOutput.setText("Changed background color to: RGB(" + redComponent + "," + greenComponent + "," + blueComponent + ")");
        }
        
    }
    
    public void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        //Creates the two main menus for MenuBar
        JMenu fileMenu = new JMenu("File");
        JMenu optionsMenu = new JMenu("Options");
        
        //Adds menu items for saving/loading or changing settings
        JMenuItem loadFileMenuItem = new JMenuItem("Load");
        loadFileMenuItem.addActionListener(this);
        fileMenu.add(loadFileMenuItem);
        
        JMenuItem saveFileMenuItem = new JMenuItem("Save");
        saveFileMenuItem.addActionListener(this);
        fileMenu.add(saveFileMenuItem);
        
        JMenuItem dimensionsOptionsMenuItem = new JMenuItem("Dimensions");
        dimensionsOptionsMenuItem.addActionListener(this);
        optionsMenu.add(dimensionsOptionsMenuItem);
        
        JMenuItem rulesOptionsMenuItem = new JMenuItem("Rules");
        rulesOptionsMenuItem.addActionListener(this);
        optionsMenu.add(rulesOptionsMenuItem);
        
        JMenu colorOptionsMenu = new JMenu("Cell Color");

        //Creating submenu options for colors
        JMenuItem blueColorMenuItem = new JMenuItem("Blue");
        blueColorMenuItem.addActionListener(this);
        JMenuItem redColorMenuItem = new JMenuItem("Red");
        redColorMenuItem.addActionListener(this);
        JMenuItem orangeColorMenuItem = new JMenuItem("Orange");
        orangeColorMenuItem.addActionListener(this);
        JMenuItem magentaColorMenuItem = new JMenuItem("Magenta");
        magentaColorMenuItem.addActionListener(this);
        JMenuItem customColorMenuItem = new JMenuItem("Custom...");
        customColorMenuItem.addActionListener(this);
        
        colorOptionsMenu.add(blueColorMenuItem);
        colorOptionsMenu.add(redColorMenuItem);
        colorOptionsMenu.add(orangeColorMenuItem);
        colorOptionsMenu.add(magentaColorMenuItem);
        colorOptionsMenu.add(customColorMenuItem);
        
        optionsMenu.add(colorOptionsMenu);
        
        JMenu backColorOptionsMenu = new JMenu("Background Color");
        
        //Adds action listeners to Background color menu items
        JMenuItem darkGrayBackMenuItem = new JMenuItem("Dark Gray");
        darkGrayBackMenuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                changeBackGroundColor(Color.DARK_GRAY);
            }
        });
        JMenuItem whiteBackMenuitem = new JMenuItem("White");
        whiteBackMenuitem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                changeBackGroundColor(Color.WHITE);
            }
        });
        JMenuItem blackBackMenuItem = new JMenuItem("Black");
        blackBackMenuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                changeBackGroundColor(Color.BLACK);
            }
        });
        
        JMenuItem cyanBackMenuItem = new JMenuItem("Cyan");
        cyanBackMenuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                changeBackGroundColor(Color.CYAN);
            }
        });
        JMenuItem customBackMenuItem = new JMenuItem("Custom...");
        customBackMenuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                openColorPicker(false);
            }
        });
        
        
        backColorOptionsMenu.add(darkGrayBackMenuItem);
        backColorOptionsMenu.add(whiteBackMenuitem);
        backColorOptionsMenu.add(blackBackMenuItem);
        backColorOptionsMenu.add(cyanBackMenuItem);
        backColorOptionsMenu.add(customBackMenuItem);
        
        optionsMenu.add(backColorOptionsMenu);
        
        menuBar.add(fileMenu);
        menuBar.add(optionsMenu);
        
        //Sets the MenuBar of the Fram to the created menuBar
        setJMenuBar(menuBar);
        
    }

    // Handles menu bar clicks
    @Override
    public void actionPerformed(ActionEvent e){
        String action = e.getActionCommand();
        switch (action){
            case "Dimensions":
            openDimensionFrame();
            break;
            
            case "Rules":
            openRulesFrame();
            break;
            
            case "Load":
            
            //if save folder doesn't exist it is created
            File saveFolder = new File("./SaveFiles");
            saveFolder.mkdir();
            
            // Configures FileChooser only accept valid file types
            JFileChooser loadFileChooser = new JFileChooser();
            loadFileChooser.setCurrentDirectory(saveFolder);
            loadFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            loadFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Game Files", "gol", "rle"));
            loadFileChooser.setAcceptAllFileFilterUsed(false);

            int loadResult = loadFileChooser.showOpenDialog(null);

            if(loadResult == JFileChooser.APPROVE_OPTION){
                //If file selected then load into game
                if(!board.loadFile(loadFileChooser.getSelectedFile())){
                    consoleOutput.setText("Invlaid File!");
                }else{
                    gridXDimension = board.getWidth();
                    gridYDimension = board.getHeight();
                }  
            }
            break;
            
            case "Save":
            JFileChooser saveFileChooser = new JFileChooser();
            saveFileChooser.setCurrentDirectory(new File("./SaveFiles"));
            saveFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int saveResult = saveFileChooser.showSaveDialog(null);

            if(saveResult == JFileChooser.APPROVE_OPTION){
                //If valid file entered then save to file
                File selectedFile = saveFileChooser.getSelectedFile();
                String abspath = selectedFile.getAbsolutePath();
                board.saveFile(abspath);
            }
            break;

            case "Blue":
            liveCellColor = Color.BLUE;
            if(isRecording){
                consoleOutput.setText("Changed cell color to: RGB(" + liveCellColor.getRed() + "," + liveCellColor.getGreen() + "," + liveCellColor.getBlue() + "). Final color of the recorded gif will be the current color.");
            }else{
                consoleOutput.setText("Changed cell color to: RGB(" + liveCellColor.getRed() + "," + liveCellColor.getGreen() + "," + liveCellColor.getBlue() + ")");
            }
            break;

            case "Red":
            liveCellColor = new Color(158,24,14);
            if(isRecording){
                consoleOutput.setText("Changed cell color to: RGB(" + liveCellColor.getRed() + "," + liveCellColor.getGreen() + "," + liveCellColor.getBlue() + "). Final color of the recorded gif will be the current color.");
            }else{
                consoleOutput.setText("Changed cell color to: RGB(" + liveCellColor.getRed() + "," + liveCellColor.getGreen() + "," + liveCellColor.getBlue() + ")");
            }
            break;

            case "Orange":
            liveCellColor = new Color(255,123,0);
            if(isRecording){
                consoleOutput.setText("Changed cell color to: RGB(" + liveCellColor.getRed() + "," + liveCellColor.getGreen() + "," + liveCellColor.getBlue() + "). Final color of the recorded gif will be the current color.");
            }else{
                consoleOutput.setText("Changed cell color to: RGB(" + liveCellColor.getRed() + "," + liveCellColor.getGreen() + "," + liveCellColor.getBlue() + ")");

            }
            break;

            case "Magenta":
            liveCellColor = Color.MAGENTA;
            if(isRecording){
                consoleOutput.setText("Changed cell color to: RGB(" + liveCellColor.getRed() + "," + liveCellColor.getGreen() + "," + liveCellColor.getBlue() + "). Final color of the recorded gif will be the current color.");
            }else{
                consoleOutput.setText("Changed cell color to: RGB(" + liveCellColor.getRed() + "," + liveCellColor.getGreen() + "," + liveCellColor.getBlue() + ")");
            }
            break;

            case "Custom...":
            openColorPicker(true);
            
        }
        repaint();
    }
    
    //Frame for changing dimensions of grid
    public void openDimensionFrame(){
        JFrame dimensionsFrame = new JFrame();
        dimensionsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dimensionsFrame.setSize(200, 100);
        dimensionsFrame.setTitle("Dimensions Settings");
        dimensionsFrame.setLayout(new BoxLayout(dimensionsFrame.getContentPane(), BoxLayout.Y_AXIS));
        
        JPanel entryPanel = new JPanel();
        entryPanel.setLayout(new GridLayout(2,2));
        
        JLabel widthLabel = new JLabel("Width: ");
        JLabel heightLabel = new JLabel("Height: ");
        
        JTextField widthField = new JTextField("50");
        JTextField heightField = new JTextField("50");
        entryPanel.add(widthLabel);
        entryPanel.add(widthField);
        entryPanel.add(heightLabel);
        entryPanel.add(heightField);
        
        
        JButton okButton = new JButton("Enter");
        okButton.setAlignmentX(CENTER_ALIGNMENT);
        //Changes the size of the board and closes window 
        okButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    board.resizeBoard(Integer.parseInt(widthField.getText()),
                    Integer.parseInt(heightField.getText()));
                    dimensionsFrame.dispatchEvent(new WindowEvent(dimensionsFrame, WindowEvent.WINDOW_CLOSING));
                    repaint();
                    gridXDimension = Integer.parseInt(widthField.getText());
                    gridYDimension = Integer.parseInt(heightField.getText());
                    
                    if(isRecording){
                        consoleOutput.setText("Stopped recording and changed dimensions to:" + gridXDimension + gridYDimension);
                    }else{
                        consoleOutput.setText("Changed dimensions to:" + gridXDimension + gridYDimension);
                    }
                    
                } catch (NumberFormatException error) {
                    widthField.setText("50");
                    heightField.setText("50");
                }
            }
        });
        
        //Adds the components and sets visible
        dimensionsFrame.add(entryPanel);
        dimensionsFrame.add(okButton);
        dimensionsFrame.setVisible(true);
    }
    
    
    public void openColorPicker(boolean cellColor){
        //Creates and formats Frame to contain color picker
        JFrame colorFrame = new JFrame();
        colorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        colorFrame.setSize(500,500);
        colorFrame.setTitle("Custom Color Picker");

        JColorChooser colorChooser = new JColorChooser(boardColor);
        
        if(!cellColor){
            // modifying background color
            colorChooser.getSelectionModel().addChangeListener(e->{
                changeBackGroundColor(colorChooser.getColor());
            });    
        }else{
            //modifying cell color
            colorChooser.getSelectionModel().addChangeListener(e->{
                liveCellColor = colorChooser.getColor();
                repaint();
                if(isRecording){
                    consoleOutput.setText("Changed cell color to: RGB(" + liveCellColor.getRed() + "," + liveCellColor.getGreen() + "," + liveCellColor.getBlue() + "). Final color of the recorded gif will be the current color.");
                }else{
                    consoleOutput.setText("Changed cell color to: RGB(" + liveCellColor.getRed() + "," + liveCellColor.getGreen() + "," + liveCellColor.getBlue() + ")");

                }
            });
        }

        //Adds ColorChooser to frame and sets frame visible
        colorFrame.add(colorChooser);
        colorFrame.setVisible(true);

    }
    
    public void openRulesFrame(){
        //Creates Frame to edit rules of game
        JFrame rulesFrame = new JFrame();
        rulesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        rulesFrame.setSize(200, 150);
        rulesFrame.setTitle("Rule Settings");
        rulesFrame.setLayout(new BoxLayout(rulesFrame.getContentPane(), BoxLayout.Y_AXIS));
        
        JPanel entryPanel = new JPanel();
        entryPanel.setLayout(new GridLayout(3,2));
        
        JLabel xLabel = new JLabel("X: ");
        JLabel yLabel = new JLabel("Y: ");
        JLabel zLabel = new JLabel("Z: ");
        
        //Creates spinners that can only select plausible values
        SpinnerModel xSpinnerModel = new SpinnerNumberModel(board.getXVal(),0,7,1);
        SpinnerModel ySpinnerModel = new SpinnerNumberModel(board.getYVal(),1,8,1);
        SpinnerModel zSpinnerModel = new SpinnerNumberModel(board.getZVal(),0,8,1);
        
        JSpinner xSpinner = new JSpinner(xSpinnerModel);
        JSpinner ySpinner = new JSpinner(ySpinnerModel);
        JSpinner zSpinner = new JSpinner(zSpinnerModel);
        
        // Stops user from typing into spinner
        JFormattedTextField tfx = ((JSpinner.DefaultEditor) xSpinner.getEditor()).getTextField();
        tfx.setEditable(false);
        
        JFormattedTextField tfy = ((JSpinner.DefaultEditor) ySpinner.getEditor()).getTextField();
        tfy.setEditable(false);
        
        JFormattedTextField tfz = ((JSpinner.DefaultEditor) zSpinner.getEditor()).getTextField();
        tfz.setEditable(false);
        
        //Adds all components to panel 
        entryPanel.add(xLabel);
        entryPanel.add(xSpinner);
        entryPanel.add(yLabel);
        entryPanel.add(ySpinner);
        entryPanel.add(zLabel);
        entryPanel.add(zSpinner);
        
        JButton okButton = new JButton("Enter");
        okButton.setAlignmentX(CENTER_ALIGNMENT);
        
        // If values are valid then change the rules of the game
        okButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                int x = (int)xSpinner.getValue();
                int y = (int)ySpinner.getValue();
                int z = (int)zSpinner.getValue();
                
                if(x<y){
                    board.changeRules(x , y, z);
                    consoleOutput.setText("Changed rules to X:" + xSpinner.getValue() + " Y:" + ySpinner.getValue() + " Z:" + zSpinner.getValue());
                    rulesFrame.dispatchEvent(new WindowEvent(rulesFrame, WindowEvent.WINDOW_CLOSING));
                }else{
                    consoleOutput.setText("Invalid input! x>y");
                }
                
            }
        });
        
        rulesFrame.add(entryPanel);
        rulesFrame.add(okButton);
        rulesFrame.setVisible(true);
    }
    
    
    //Panel which contaiains the UI for interacting with the grid 
    public class BoardPanel extends JPanel implements MouseListener, Runnable {
        private int gridx1;
        private int gridy1;

        public BoardPanel(){ 
            repaint();
            //Attaches a listener to the board
            addMouseListener(this);
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            // Calculates the cell which has been clicked 
            int x = (e.getX()- gridx1)/SIZE_OF_CELL;
            int y = (e.getY()-gridy1)/SIZE_OF_CELL;
            // makes sure that the mouse click is on the board 
            if(board.isOnBoard(x,y)){
                try{
                    board.ToggleCell(x, y);
                    repaint();
                }catch(ArrayIndexOutOfBoundsException err){
                    //Do not update anything in this case.
                }
            }
        }
        public void mouseEntered(MouseEvent e) {
        }
        public void mouseExited(MouseEvent e) {
        }
        public void mousePressed(MouseEvent e) {  
        }
        public void mouseReleased(MouseEvent e) {
        }
        
        // Draws the grid and live cells
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // finds value to maximize grid size and center grid
            if(getWidth() / board.getWidth() < getHeight() / board.getHeight()){
                SIZE_OF_CELL = getWidth() / board.getWidth();
            }else{
                SIZE_OF_CELL = getHeight() / board.getHeight();
            }
            //Size of fill 1 smaller so that grid is still displayed
            SIZE_OF_FILL = SIZE_OF_CELL - 1;
            
            //Calculates the coordiante of the top left of the grid
            gridx1 = ( getWidth() - (board.getWidth()*SIZE_OF_CELL) ) / 2;
            gridy1 = ( getHeight() - (board.getHeight()*SIZE_OF_CELL) ) / 2;
            
            
            g.setColor(lineColor);
            
            // Draw Vertical lines of grid
            for(int i=0; i <= board.getWidth(); i++){
                g.drawLine(gridx1 + SIZE_OF_CELL * i , gridy1, gridx1 + SIZE_OF_CELL * i, gridy1 + SIZE_OF_CELL * board.getHeight());
            }
            // draw horizontal lines of grid
            for(int j=0; j<= board.getHeight(); j++){
                g.drawLine(gridx1 , gridy1 + SIZE_OF_CELL *j, gridx1 + SIZE_OF_CELL * board.getWidth(), gridy1 + SIZE_OF_CELL *j);
            }
            

            g.setColor(liveCellColor);

            //drawing live cells 
            for(int i=0 ; i< board.getWidth(); i++){
                for(int j=0 ; j< board.getHeight(); j++){
                    if(board.isCellAlive(i,j)){
                        g.fillRect(gridx1 + i*SIZE_OF_CELL + 1, gridy1 + j*SIZE_OF_CELL + 1, SIZE_OF_FILL, SIZE_OF_FILL);
                    }
                }
            }
            
        }
        
        //refreshes the board and redraws it
        @Override
        public void run(){
            while(gameRunning){
                //Calculates live and dead cells for next board state
                board.deathAndBirth();
                //Helps keep game smooth 
                Toolkit.getDefaultToolkit().sync();
                repaint();
                if(isRecording){
                    recordedFrames.add(board.boardSnapshot());
                }
                try{
                    Thread.sleep(millisecondsPerFrame);
                }catch(InterruptedException e){

                }
            }
            
        }
        
    }
    
    public class GifCreator implements Runnable{
        // Method for handeling gif generation on a seperate thread
        @Override
        public void run() {
            if (isRecording) {
                int pixelSize = (int)Math.ceil(10.0/((double)Math.max(gridXDimension,gridYDimension)/50.0));
                System.out.println("Generating gif with per pixel size: " + pixelSize);
                isRecording = false;
                consoleOutput.setText("Generating Image please wait!");
                recordButton.setText("RECORD");
                int capturedFrameCount = recordedFrames.size();
                System.out.println("Captured " + capturedFrameCount + " frames");
                //Calculates how many frames equate to single loading bar
                int perLoadingBar = (int)Math.ceil((double)capturedFrameCount/20.0);
                
                ArrayList<BufferedImage> generatedFrames = new ArrayList<BufferedImage>();
                String loadingText = "|————————————————————|";
                
                int currentIndex = 0;
                int currentLoadingIndex = 0;
                for (boolean[][] snapshot : recordedFrames) {
                    //For each snapshot of board recorded a buffered image is generated
                    BufferedImage bfImage = new BufferedImage(snapshot.length * pixelSize,
                    snapshot[0].length * pixelSize, BufferedImage.TYPE_INT_RGB);
                    Graphics g = bfImage.getGraphics();
                    g.setColor(boardColor);
                    g.fillRect(0,0,snapshot.length * pixelSize, snapshot[0].length * pixelSize);
                    
                    g.setColor(liveCellColor);
                    
                    //Draws the live cells on the buffered image
                    for (int i = 0; i < snapshot.length; i++) {
                        for (int ii = 0; ii < snapshot[i].length; ii++) {
                            if (snapshot[i][ii]) {
                                g.fillRect(i * pixelSize, ii * pixelSize, pixelSize, pixelSize);
                            }
                        }
                    }
                    generatedFrames.add(bfImage);
                    g.dispose();                 
                    
                }
                ImageOutputStream output;
                
                gameRunning = false;
                gameThread.interrupt();
                playButton.setText("▶");
                
                try {
                    JFileChooser recordSaver = new JFileChooser();
                    recordSaver.setCurrentDirectory(new File("~/Pictures"));
                    recordSaver.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    int recordSaverResult = recordSaver.showSaveDialog(null);
                    if(recordSaverResult == JFileChooser.APPROVE_OPTION){
                        //Retrives the users prefered location for where to save the gif
                        File selectedFile = recordSaver.getSelectedFile();
                        String abspath = selectedFile.getAbsolutePath();
                        output = new FileImageOutputStream(new File(abspath+".gif"));
                        
                        //Creates a new object to stitch multiple BufferedImage frames into a single gif
                        GifSequenceWriter writer = new GifSequenceWriter(output, generatedFrames.get(0).getType(),millisecondsPerFrame, true);
                        
                        currentIndex = 0;
                        for(BufferedImage frame: generatedFrames){
                            //Updates the loading bar
                            writer.writeToSequence(frame);
                            
                            if(currentIndex%(perLoadingBar)==0 && currentLoadingIndex!=20){
                                loadingText = loadingText.substring(0,1+currentLoadingIndex) + "=" + loadingText.substring(2+currentLoadingIndex);
                                consoleOutput.setText("Generating: " + loadingText);
                                currentLoadingIndex++;
                            }
                            currentIndex++;
                            
                        }               
                        writer.close();
                        output.close(); // finished generating the gif
                        consoleOutput.setText("Finished generating GIF file. Saved to: " + abspath);
                    }
                } catch (FileNotFoundException e2) {
                    e2.printStackTrace();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                
            }else{
                // if the game isn't being recorded start a new recording session
                isRecording = true;
                recordedFrames = new ArrayList<boolean[][]>();
                recordButton.setText("STOP");
                consoleOutput.setText("Recording...");
            }
            
        }
    }
    
    
}