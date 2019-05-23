# MDP_Group_05_Android
## Overview
The Android Module written for the Android Application required for CZ3004 Multi Disciplinary Project (MDP).
The Android Application (AA) acts as a wireless remote controller for the team's robotic system. Using the AA, we can issue various commands to our robot to begin various manoeuvres in the maze during the leader board challenge. Communication with the raspberry-Pi and PC will be done solely via Bluetooth.
The AA's Graphical User Interface (GUI) provides the visualisation of the current status of the maze through a graphical 2-D display of the maze as well as the current status of the robot.
 
## Android Application's Core Functionalities
### Messaging Function
User can send and receive text strings to and from the AA to any devices connected via Bluetooth serial communication link.

### Bluetooth Connection Function
User can initiate the scanning of nearby Bluetooth device and then initiate connection with any devices selected from the list of nearby detected devices. 
 
### Control of Robot Movement Function
User can manually control the robot's movement via the directional buttons on the GUI. Pressing on the "Up Arrow", "Left Arrow", "Right Arrow" will move the robot's forward, rotate left and rotate right respectively.

### Robot's Status Display Function
When any of the directional button is pressed, the Robot will move/rotate. The 'robotStatus' on the GUI will be updated to reflect the current status of the robot.

### Setting Waypoint & Robot Start Coordinates Function
Robot's start point can be set by clicking on a cell on the Arena Map in the GUI. The cell selected for the starting location will be for the center of the Robot. As such, user are not allowed to choose the border cells as the starting point. Similarly, the Waypoint can also be set. Since the Robot has to be over the placed Waypoint during fastest path, user are not allow to choose the border cells as the Waypoint.

### 2-D Display of  the Arena Map & Robot's Location
A 15x20 grid maze is created by drawing 300 square-shaped cells and drawing lines in between the cells. Each cell is colored differently based on what is the status of that particular cell. The Robot is drawn within a 3x3 of the maze grid which has a small blue square as the indication of the robot head.

### Toggle between Manual or Auto updating of 2-D Arena Map
The GUI allows for two methods of updating: Manual and Automatic (Default mode). When Automatic mode is toggled, the AA updates the Arena Map as soon as possible when an update message is received. When the Manual mode is toggled, the AA only updates the Arena Map when the 'Manual' button is pressed

### Sending of Persistent User Reconfigurable String Commands 
Both buttons F1 and F2 contain a string value that can be reconfigured to a different value by user. The string values are saved upon change and will remain as such even after quitting the application. Pressing either of the buttons will send the string value saved in association with the button to the connected device via Bluetooth.

### Robust connectivity with Bluetooth device
The AA provides robust Bluetooth connectivity with other devices upon successful connection. Should the connection between the two devices are lost during data transmission, the AA will attempt to re-establish the Bluetooth connection with the disconnected device.

### Displaying Arrow Blocks on the 2-D Arena Map
When the camera on the robot detects an arrow image, information will be sent to the computer. After which, the PC will then relay the message to the AA.

### Tilt Control Functions
To enable tilt controls, the user will need to hold down the button named 'Motion' on the GUI. When activated, the robot will move according to how the tablet is tilted. For example, when tilting forward, the app will send a command for the robot to move forward.
