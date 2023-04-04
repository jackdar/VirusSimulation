# Virus Simulation

This is a virus simulation game created in Java for COMP610 at AUT. Feel free to clone this repo and use fairly. If you are currently studying COMP610, **DO NOT plagiarise this work**.

# Installation

To run the game, simply download [`VirusSimulation.jar`](https://github.com/jackdar/VirusSimulation/releases) from releases.

*For cloning the repo the relevant packages are named:*
- `com.jackdarlington.question1`
- `com.jackdarlington.question2`

# Game Instructions

If a phone does not make it to the Repair Shop before all it's health runs out, it will
die and be removed from the screen.

## Phone States

Phones can be in three different states: `NORMAL`, `GOING TO REPAIR`, or `INFECTED`.

## GUI

The simulation features multiple GUI windows each for different uses and displaying different information.

### Scoreboard Tab

The most important information for the simulation is inside the Scoreboard tab (bottom left corner).

This tab stores information regarding ***all current phones*** such as:
- Phones Currently Alive
- Phones Currently Infected
- Repairs Taken Place
- Phones Dead
- The current phone being repaired/going to the Repair Shop

### Information Tab

You can click on any alive phone to see more information about it in the Phone info tab (bottom right corner).

This tab stores information regarding the ***selected phone*** such as:
- Phone Health
- If the phone is infected
- How many repairs the phone has had
- Current state: Alive or Dead (Phone tab will display when your selected phone has died)

## Controls

The game has many controls to change various aspects of the simulation.

### Main Controls

- Use <kbd>space</kbd> to add a new phone
- Use <kbd>i</kbd> to infect the selected phone
- Use <kbd>r</kbd> to infect a random phone
- Use <kbd>c</kbd> to kill all phones

### Adjustable Variables

* **Health** - Use the <kbd>up arrow</kbd> and <kbd>down arrow</kbd> keys to increase or decrease the health of all currently alive phones
* **Spread Radius** - Use the <kbd>page up</kbd> and <kbd>page down</kbd> keys to increase or decrease the spread radius of the virus

### Time Controls

- Click the `PAUSE` button or <kbd>escape</kbd> to pause the simulation
- Click the `PLAY` button to play at regular speed
- Click the `FAST FORWARD` button to play at two times the regular speed (Repair Shop speed will also increase accordingly)

<br>

***Have fun!***

