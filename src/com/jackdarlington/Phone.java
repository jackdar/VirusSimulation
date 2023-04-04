/*
 * Created by Jack Darlington | 2023
 */

package com.jackdarlington;

/**
 * @author Jack Darlington
 * Student ID: 19082592
 * Date: 18/03/2023
 */

public class Phone implements Runnable {
    
    static final int MAX_SPEED = 3;
    
    static int phonesRepaired = 0;
    static int nextIDNumber = 1;
    
    int id;
    
    int x, y;
    int velocityX, velocityY;
    
    int width, height;
    
    int delay;
    int time;
    int phoneHealth;
    int repairs;
    
    boolean isPhoneInfected;
    boolean goToRepairShop;
    boolean isSelected;
    
    RepairShop repairShop;
    
    public Phone(int id, RepairShop repairShop, int panelAreaX, int panelAreaY) {
        this.id = nextIDNumber++;
        
        this.width = panelAreaX;
        this.height = panelAreaY;
        
        this.relocate(this.width, this.height);
        
        this.velocityX = (int) ((Math.random() * 2.0 - 1.0) * 5.0);
        this.velocityY = (int) ((Math.random() * 2.0 - 1.0) * 5.0);
        
        this.delay = 12;
        this.time = 0;
        this.phoneHealth = Panel.setPhoneHealth;
        this.repairs = 0;
        
        this.isPhoneInfected = false;
        this.goToRepairShop = false;
        this.isSelected = false;
        
        this.repairShop = repairShop;
    }
    
    @Override
    public void run() {
        // While Phone is Alive
        while (this.phoneHealth > 0) {
            
            try {
                // Simulation logic delay, time controls act on this.delay
                Thread.sleep(this.delay);
            } catch (InterruptedException ex) {
                System.out.println("Simulation Paused!");
            }
            
            // If the simulation is paused wait for notify from Panel.lock
            if (Panel.isPaused) {
                synchronized (Panel.lock) {
                    try {
                        Panel.lock.wait();
                    } catch (InterruptedException ex) {
                        System.out.println("Simulation Halted!");
                    }
                }
            }
            
            // Phone logic when Infected
            if (this.isPhoneInfected) {
                --this.phoneHealth;
                synchronized (this.repairShop) {
                    if (this.repairShop.isEmpty) {
                        this.repairShop.isEmpty = false;
                        this.goToRepairShop = true;
                    }
                }
                if (this.goToRepairShop) {
                    if (this.goingToRepairShop()) {
                        continue;
                    }
                    this.goToRepairShop = false;
                    this.getRepair();
                } else {
                    this.move();
                }
            } else {
                this.move();
            }
        }
    }
    
    // Move method implemented using random angle and speed generation
    public void move() {
        // Set the bounds of the phone's movement
        if (this.x > this.width - 16 || this.x < 16) {
            this.velocityX *= -1;
        }
        if (this.y > this.height - 16 || this.y < 16) {
            this.velocityY *= -1;
        }
        
        // Generate phone angle and speed, and calculate velocity x and y
        if (++this.time % 50 == 0) {
            double angle = Math.random() * 2.0 * Math.PI;
            double speed = Math.random() * MAX_SPEED;
            this.velocityX = (int) Math.round(speed * Math.cos(angle));
            this.velocityY = (int) Math.round(speed * Math.sin(angle));
        }

        // Apply the velocity to the phone's location
        double magnitude = Math.sqrt(this.velocityX * this.velocityX + this.velocityY * this.velocityY);
        if (magnitude > 0.0) {
            double factor = MAX_SPEED / magnitude;
            this.x += (int) Math.round(factor * this.velocityX);
            this.y += (int) Math.round(factor * this.velocityY);
        }
    }
    
    // Sets the phone's x and y location to a random location inside the new panel area
    public void relocate(int panelAreaX, int panelAreaY) {
        this.x = (int) ((Math.random() * ((double) panelAreaX - 16.0)) + 16);
        this.y = (int) ((Math.random() * ((double) panelAreaY - 16.0)) + 16);
    }
    
    // Method to move the phone in the most direct route to the repair shop
    private boolean goingToRepairShop() {
        double dx = this.repairShop.x + 60 - this.x;
        double dy = this.repairShop.y - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // If the distance is less than 5, the phone has arrived at the repair shop
        if (distance < 5) {
            return false;
        }

        double speed = Math.min(distance, 3);
        this.velocityX = (int) Math.round(speed * dx / distance);
        this.velocityY = (int) Math.round(speed * dy / distance);

        this.x += this.velocityX;
        this.y += this.velocityY;

        return true;
    }
    
    // Method to have the phone wait in the repair shop,
    // dependent on what time control is currently being used
    private void getRepair() {
        try {
            Thread.sleep(200 + (this.delay * 100));
        } catch (InterruptedException ex) {
            System.out.println("Class: Phone, Method: getRepair(), Line 166 - InterruptedException thrown");
        }
        
        ++phonesRepaired;
        ++this.repairs;
        
        this.isPhoneInfected = false;
        this.phoneHealth = Panel.setPhoneHealth;
        this.repairShop.isEmpty = true;
    }

}