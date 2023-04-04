# Virus Simulation
This is a virus simulation game created in Java for COMP610 at AUT. Feel free to clone this repo and use fairly. If you are currently studying COMP610, **DO NOT plagiarise this work**. Feel free to build on or use as an example.

# Installation
To run the game, simply download [`VirusSimulation.jar`](https://github.com/jackdar/VirusSimulation/releases) from releases.

*For cloning the repo the relevant packages are named:*
- `com.jackdarlington.question1`
- `com.jackdarlington.question2`

# Game Instructions
Phones can be in three different states: **NORMAL**, **GOING TO REPAIR**, **INFECTED**.

If a phone does not make it to the Repair Shop before all it's health runs out, it will
die and be removed from the screen.

The most important information for the simulation is inside the Scoreboard tab (bottom left corner).
- This tab stores information regarding:
  - Phones Currently Alive
  - Phones Currently Infected
  - Repairs Taken Place
  - Phones Dead
  - The current phone being repaired/going to the Repair Shop

You can click on any alive phone to see more information about it in the Phone info tab (bottom right corner).
- This tab stores information regarding the selected phone such as:
  - Phone Health
  - If the phone is infected
  - How many repairs the phone has had
  - Current state: Alive or Dead (Phone tab will display when your selected phone has died)

Controls:
- Add a New Phone			            `SPACE`
- Infect the Selected Phone	      `I`
- Infect a Random Phone		        `R`
- Kill all Phones 		            `C`
- Increase Health of all Phones	  `UP ARROW`
- Decrease Health of all Phones	  `DOWN ARROW`
- Increase the Spread Radius		  `PAGE UP`
- Decrease the Spread Radius		  `PAGE DOWN`

Adjustable Variables:
* Health - Use the UP and DOWN arrow keys to increase or decrease the health of all currently alive phones.
* Spread Radius - Use the PAGE UP and PAGE DOWN keys to increase or decrease the spread radius of the virus.

Time Controls:
- PAUSE button or ESC to pause the simulation
- PLAY to play at regular speed
- FAST FORWARD to play at two times the regular speed (Repair Shop speed will also increase accordingly)

Have fun!

