import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;


public class Gameboard{
    // Sets the defualt grid size
    private final int DEFAULT_WIDTH = 50;
    private final int DEFAULT_HEIGHT = 50;

    private int gridWidth = DEFAULT_WIDTH;
    private int gridHeight = DEFAULT_HEIGHT;

    //Creates the 2D boolean array to store cell states (true = alive)
    private boolean[][] board = new boolean[gridWidth][gridHeight];
    private List<Point> alivePoints = new ArrayList<Point>();
    

    // Set default rule values 
    private int X_VALUE=2;
    private int Y_VALUE=3;
    private int Z_VALUE=3;


    public boolean[][] boardSnapshot(){
        return board;
    }

    public Gameboard(){
        this(50,50,2,3,3);
    }

    public Gameboard(int XX, int YY, int ZZ){
         this(50,50,XX,YY,ZZ);
    }

    public Gameboard(int height, int width){
        this(height,width,2,3,3);        
    }

    public Gameboard(int height, int width, int XX, int YY, int ZZ){
        this.gridWidth = width;
        this.gridHeight = height;
        this.board = new boolean[gridWidth][gridHeight];
        this.X_VALUE = XX;
        this.Y_VALUE = YY;
        this.Z_VALUE = ZZ;
    }

    public int getXVal(){
        return X_VALUE;
    }

    public int getYVal(){
        return Y_VALUE;
    }

    public int getZVal(){
        return Z_VALUE;
    }
    
    public void changeRules(int xVal, int yVal, int zVal){
        X_VALUE = xVal;
        Y_VALUE = yVal;
        Z_VALUE = zVal;
    }


    public void resizeBoard(int width, int height){
        this.gridWidth = width;
        this.gridHeight = height;
        board = new boolean[width][height];
    }

    //Toggles the state of the given cell
    public void ToggleCell(int x, int y){
        if(isOnBoard(x, y)){
            board[x][y] = !board[x][y];
            if(alivePoints.contains(new Point(x,y))){
                alivePoints.remove(new Point(x,y));
            }else{
                alivePoints.add(new Point(x,y));
            }
        }
    }

    public boolean isCellAlive(int x, int y){
        return board[x][y];
    }

    //returns true if x,y is on board
    public boolean isOnBoard(int x, int y){
        if(x>=0 && y>=0 && (getWidth() >= x && getHeight() >= y)){
            return true;
        }
        return false;
    }

    public int getWidth(){
        return gridWidth;
    }

    public int getHeight() {
        return gridHeight;
    }

    // loads in a grid of any size of type .gol as defined by specification
    public boolean loadGOLFile(File file){
        
        // First stores into ArrayList so that size can be determined
        List<boolean[]>  readGrid = new ArrayList<boolean[]>();
        try(BufferedReader reader = new BufferedReader( new FileReader(file))){
            String line = reader.readLine();
            while(line != null){
                boolean[] row = new boolean[line.length()];
                for(int x = 0; x<line.length(); x++){
                    if(line.charAt(x) == 'o'){
                        row[x] = true;
                    }
                }
                readGrid.add(row);
                line=reader.readLine();
            }
        }catch(IOException e){
            return false;
        }
        
        //Calculates the size of the grid and changes the board size
        int width = readGrid.get(0).length;
        int height = readGrid.size();
        resizeBoard(width, height);

        //Transfers from the temperary ArrayList into the board array
        for(int i=0; i <width; i++){
            for(int ii=0 ; ii<height ; ii++){
                if(readGrid.get(ii)[i]){
                    board[i][ii] = true;
                    alivePoints.add(new Point(i,ii));
                }
            }
        }
        return true;
    }

    //Loads RLE file type as defined on https://www.conwaylife.com/wiki/Run_Length_Encoded
    public boolean loadRLEFile(File file){
        int padding = 2;
        try(BufferedReader reader = new BufferedReader( new FileReader(file))){
            String line = reader.readLine();
            try{
                //ignore all lines containing a #
                while(line.contains("#")){
                    line = reader.readLine();
                }
                // removes spacing of header line
                line = line.replaceAll("\\s+", "");

                // reads between 'x=' and the first ',' and stores the parsed integer value as width of grid and discards this part of string
                int width = Integer.parseInt(line.substring(line.indexOf("x=")+2 , line.indexOf(",")));
                line = line.substring(line.indexOf(",") + 1);

                // reads between 'x=' and the first ',' and stores the parsed integer value as width of grid and discards this part of string
                int height = Integer.parseInt(line.substring(line.indexOf("y=")+2 , line.indexOf(",")));
                line = line.substring(line.indexOf(",") + 1);
                resizeBoard(width+4, height+4);

                // if rules are specified then load the rule else set to default values
                if (line.contains("rule=")){
                    line = line.replaceAll("rule=" , "");
                    line = line.toUpperCase();
                    int zVal  = Integer.parseInt(line.substring(line.indexOf("B") + 1, line.indexOf("B") +2));
                    int xVal = Integer.parseInt(line.substring(line.indexOf("S") + 1 , line.indexOf("S") +2));
                    int yVal = Integer.parseInt(line.substring(line.indexOf("S") + 2 , line.indexOf("S") +3));
                    changeRules(xVal, yVal, zVal);                         
                }else{
                    changeRules(2, 3, 3);
                }
                line = reader.readLine();
                
                // adds padding only on rle so that other patterns do not break immediatly due to toroidal grid
                int currentRow = padding;
                int rowPosition = padding;
                //iterates through and reads all of the file
                while(line != null){
                    line = line.replaceAll("\\s+", "");
                    while(line.length() != 0 ){
                        String runCount = "";
                        int numOfCharacters=0;
                        char character = line.charAt(0);
                        // Once '!' reached rest of file is ignored 
                        if(character == '!'){
                            return true;
                        }
                        // move onto next line once '$' read
                        if(character == '$'){
                            currentRow += 1;
                            rowPosition = padding;
                            line = line.substring(1);
                            continue;
                            
                        }
                        //Gets the run count for the current character 
                        while(Character.isDigit(character)){
                            runCount += character;
                            line = line.substring(1);
                            character = line.charAt(0);
                        }
                        //If no run count then set to 1 else parse runCount to integer
                        if(runCount.isEmpty()){
                            numOfCharacters = 1;
                        }else{
                            numOfCharacters = Integer.parseInt(runCount);
                        }
                        
                        //Adds the run to the grid
                        for(int i=rowPosition; i< rowPosition + numOfCharacters; i++){
                            if(character=='o'){
                                board[i][currentRow] = true;
                                alivePoints.add(new Point(rowPosition, currentRow));
                            }
                        }

                        line = line.substring(1);
                        rowPosition += numOfCharacters;
                    }
                    line = reader.readLine();
                }
            }catch(NumberFormatException e){
                return false;
            }   
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    //Calls relevant funcation depending on file type. Returns true if successful loading of file 
    public boolean loadFile(File file){
        if (file.getName().contains(".gol")){
            return loadGOLFile(file);
        }else if(file.getName().contains(".rle")){
            return loadRLEFile(file);
        }
        return false;
    }


    // defining moulo as java % gives remainder so doesnt work as needed for negatives
    private int modulo(int a, int b){
        int r = a % b;
        if(r<0){
            r += b;
        }
        return r;
    }

    public void clearBoard(){
        alivePoints = new ArrayList<Point>();
        for(int i=0; i < board.length; i++){
            for(int ii = 0;ii<board[i].length;ii++){
                board[i][ii] = false;

            }
        }
    }

    //Finds which cells die and which live and which are born for next board state
    public void deathAndBirth(){
        boolean[][] newBoard = new boolean[gridWidth][gridHeight];
        Set<Point> check = new HashSet<Point>();

        for(Point cell : alivePoints){
            for(int xOffset = -1 ; xOffset <= 1 ; xOffset++){
                for (int yOffset = -1 ; yOffset <= 1 ; yOffset++){
                    // addds all points which need to be checked to set
                    check.add(new Point(modulo((cell.x + xOffset),gridWidth),modulo((cell.y+yOffset),gridHeight)));
                }
            }
        }
        for(Point cell : check){
            boolean currentCell = board[cell.x][cell.y];
            int aliveCells = 0;

            //If current cell alive set aliveCells to -1 to discount the cell itself 
            if(currentCell){
                aliveCells = -1;
            }
            
            // calculates the alive neighbors
            for(int xOffset = -1 ; xOffset <= 1 ; xOffset++){
                for (int yOffset = -1 ; yOffset <= 1 ; yOffset++){
                    if(board[modulo((cell.x + xOffset),gridWidth)][modulo((cell.y+yOffset),gridHeight)]){
                        aliveCells ++;
                    }
                }
            }

            if(currentCell){
                if(aliveCells >= X_VALUE && aliveCells <= Y_VALUE){
                    //if current cell is alive and the alive neighbors is in the inclusive range between X and Y value sets the cell alive on new board
                    newBoard[cell.x][cell.y] = true;
                }else{
                    //if cell dies  then remove from alivePoints ArrayList
                    alivePoints.remove(new Point(cell.x,cell.y));
                }
            }else{
                if(aliveCells == Z_VALUE){
                    //if cells should be born then add the point to the array list 
                    newBoard[cell.x][cell.y] = true;
                    alivePoints.add(new Point(cell.x,cell.y));

                }
            }
        }
        board = newBoard;
    }

    public void saveFile(String saveName){

        if(saveName.contains(".rle")){
            //saves board as rle file
            try(
                FileWriter fw = new FileWriter(saveName,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)

            ){
                //Adds header info to file
                out.println("x = " + gridWidth + ", y = " + gridHeight + ", rules = B" + Z_VALUE + "/S" + X_VALUE + Y_VALUE);
            
                String fileLine = "";
                int y = 0;
                int x = 0;

                while(y < gridHeight){
                    while(x < gridWidth){
                        boolean currentState = board[x][y];
                        String runString = "";
                        int runCount = 1;

                        //calculates the run length
                        while(x + 1 < gridWidth && board[x+1][y] == currentState){
                            runCount += 1;
                            x++;
                        }

                        //only adds run count if greater than 1
                        if(runCount >1){
                            runString += runCount;
                        }

                        //adds the correct character to the string based on current state
                        if(currentState){
                            runString += "o";
                        }else{
                            runString += "b";
                        }
                        
                        //if final run on line is dead then break as should not be included in file
                        if(x == gridWidth -1 && !currentState){
                            break;
                        }

                        //keeps line length under 70 characters
                        if(fileLine.length() + runString.length() < 70){
                            fileLine += runString;
                        }else{
                            out.println(fileLine);
                            fileLine = runString;
                        }
                        x++;
                    }
                    //adds end of line characer to string
                    fileLine += "$";
                    y++;
                    x = 0;
                }
                out.println(fileLine + "!");
            }catch(IOException e){}

        }else{
            //saves board as gol file 
            if(!saveName.contains(".gol")){
                saveName += ".gol";
            }
            try(
                FileWriter fw = new FileWriter(saveName,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)

            ){
                //iterates through every cell on every row and if cell is dead add 'o' to baseString and if dead add '.' to baseString
                for(int ii = 0; ii < gridHeight; ii++){
                    String baseString = "";
                    for(int i = 0; i < gridWidth;i++){
                        if(board[i][ii]){
                            baseString+="o";
                        }else{
                            baseString+=".";
                        }
                    }
                    out.println(baseString);
                }
            }catch(IOException e){
            
            }
        
        }
    }
}