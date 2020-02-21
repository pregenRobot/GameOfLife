# Conway's Game of Life Studio

## Supported Functionalities

### 1. Loading Files

This program can load `.rle` (the standard Conway's Game of Life save file format) as well as the custom `.gol` files.

1. Click the "File" toolbar menu.
2. Click the "Load" menu item to open the Load prompt window.
3. Choose file to load

### 2. Saving Files

Supports saving files as `.rle` format as well as the `.gol` format.

1. Click the "File" toolbar menu
2. Click the "Save" menu item and open the Save prompt window.
3. Type the file name, followed by the extension. (The supported ones are `.rle` and `.gol` files)
4. Save file

### 3. Changing Board Dimensions

Program allows users to change the baord gird dimensions to accommodate for Game of Life instances that require a lot of space

1. Click the "Options" toolbar menu
2. Click the "Dimensions" menu item.
3. Input the number of horizontal cells you want in the "Width" field.
4. Input the number of vertical cells you want in the "Height" field.
5. Press Enter

### 4. Changing Board Dimensions

Program allows users to change the baord gird dimensions to accommodate for Game of Life instances that require a lot of space

1. Click the "Options" toolbar menu
2. Click the "Dimensions" menu item.
3. Input the number of horizontal cells you want in the "Width" field.
4. Input the number of vertical cells you want in the "Height" field.
5. Press Enter

### 5. Change Game of Life Rules

Program allows users to change the rules (X,Y,Z values) that determine the dead and alive cells. Here is the logic:

a) If current cell is alive, the cell will stay alive if the neighbors (including diagonals) is in the inclusive range between X and Y values.

b) If current cell is dead, the cell will come alive if it has exactly Z alive neighbors (including diagonals)

To change the rules:

1. Click the "Options" toolbar menu
2. Click the "Rules" menu item.
3. Input the X value in the "X" field
4. Input the Y value in the "Y" field
5. Input the Z value in the "Z" field
6. Press Enter

Please note that the supported ranges (inclusive) are

|Attributes| X | Y | Z |
|:---------|--:|--:|--:|
|Minimum|0|1|0|
|Maximum|7|8|8|

The default values are X: 2, Y: 3, Z: 3 (According to Conway's original Game of Life)

### 6. Change Cell Color

This program allows users to change the color of alive cells.

1. Click "Options" toolbar menu
2. Click "Cell Color" menu item
3. Pick a color
   1. Click on a color if you are happy with pre-defined colors
   2. Click "Custom" menu item to choose a custom color

### 7. Change Background Color

Users can change the background color of the entire board, as well.

1. Click "Options" toolbar menu
2. Click "Cell Color" menu item
3. Pick a color
   1. Click on a color if you are happy with pre-defined colors
   2. Click "Custom" menu item to choose a custom color

### 8. Running the game

This game can continuously generate frames and displaying the next "generation" of live and dead cells

1. Press the "▶️" to start running the game and "⏸️" to pause when needed

### 9. Displaying next frame

In addition to continuously generating frames, the game can also generate a single frame and displaying it

1. Press the ">" button to calcualte the next generation of live and dead cells

### 10. Change framerate

You can also change the current frame rate (rate at which the next generation of live and dead cells are displayed on screen)

1. Drag the slider in the bottom to change the frame rate

### 11. Resetting the board space

Program can clear all activate cells from the board.

1. Press the "RESET" button

### 12. Recording the board

The main purpose of this program is to be a studio suite for Generating COnway's Game of Life GIFs. This means the program can record the gameboard and generate custom GIFs to share on any media.

1. Press the "RECORD" button. This can be done when the game is paused, or when its running as well.
2. Press "▶️" (play button) to keep on capturing frames or ">" (next step button) to capture a single frame.
3. After you are content with the captured frames, press "STOP" to stop the recording.
4. Wait for the Gif saving window to be prompted
5. Choose a file name to save the gif as
6. Wait for the program to generate the Gif file to be stored at the specified location.

**NOTE1**: This program will only capture frames while it is running. In other words, you can use "⏸️" to pause the "game-running" and fine tune the frames you capture.

**NOTE2**: Because the gif generation is running on a separate thread, you are free to continue carrying out other tasks in the program (including capturing another gif file)

**NOTE3**: The generated Gif's background and foreground (live cell colors) will be determined by the final state of the background color and live cell colors. This means that if you change the background mid-recording, only the most updated colors will be used to generate the gifs

**NOTE4**: The generated Gif's framerate will be determined by the final frame-rate of the game. If you change the framerate mid-recording, only the last frame rate will be applied on the gif.

### 13. Console Output

A console was added to give some information to the user on what has currently changed. The messages will be dispalyed in the bottom of the screen.
