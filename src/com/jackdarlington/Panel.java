/*
 * Created by Jack Darlington | 2023
 */

package com.jackdarlington;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * @author Jack Darlington
 * Student ID: 19082592
 * Date: 18/03/2023
 */

public class Panel extends JPanel implements KeyListener, ComponentListener, MouseListener {

    static final Object lock = new Object();
    
    static int setPhoneHealth = 300;
    static boolean isPaused = true;
    
    Phone[] phones;
    Thread[] threads;
    
    RepairShop repairShop;
    
    int width, height;
    
    int totalInfectedPhones;
    int totalDeadPhones;
    int phoneIcons;
    int infectionRadius;
    int deathFontSize;
    int programDelay;
    
    float backgroundOpacity;
    
    Image normal, goingToRepair, infected, glow;
    Image play, playD, pause, pauseD, twoX, twoXD;
    
    Phone selectedPhone;
    
    public Panel()
    {
        // Panel values
        this.width = 1000;
        this.height = 1000;
        
        // Set game time specific values
        this.resetValues();
        
        // Import image 
        try {
            this.normal = new ImageIcon(getClass().getClassLoader().getResource("icons/phone-normal.png")).getImage();
            this.goingToRepair = new ImageIcon(getClass().getClassLoader().getResource("icons/phone-toShop.png")).getImage();
            this.infected = new ImageIcon(getClass().getClassLoader().getResource("icons/phone-infected.png")).getImage();
            this.glow = new ImageIcon(getClass().getClassLoader().getResource("icons/phone-glow.png")).getImage();
            this.play = new ImageIcon(getClass().getClassLoader().getResource("icons/play-active.png")).getImage();
            this.playD = new ImageIcon(getClass().getClassLoader().getResource("icons/play-deactive.png")).getImage();
            this.pause = new ImageIcon(getClass().getClassLoader().getResource("icons/pause-active.png")).getImage();
            this.pauseD = new ImageIcon(getClass().getClassLoader().getResource("icons/pause-deactive.png")).getImage();
            this.twoX = new ImageIcon(getClass().getClassLoader().getResource("icons/2x-active.png")).getImage();
            this.twoXD = new ImageIcon(getClass().getClassLoader().getResource("icons/2x-deactive.png")).getImage();
        } catch (Exception e) {
            System.out.println("Class: Panel, Method: Panel(), Lines 72-81 - Exception thrown");
        }
        
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addComponentListener(this);
        this.setFocusable(true);
    }
    
    // Reset game time specific values
    private void resetValues() {
        Phone.phonesRepaired = 0;
        Phone.nextIDNumber = 1;
        Panel.setPhoneHealth = 300;
        
        this.repairShop = new RepairShop(this.width, this.height);
        
        this.phones = new Phone[1];
        this.threads = new Thread[1];
        this.totalInfectedPhones = 0;
        this.totalDeadPhones = 0;
        this.phoneIcons = 1;
        this.infectionRadius = 25;
        this.deathFontSize = 9000;
        this.programDelay = 12;
        this.backgroundOpacity = 0.0f;
        this.selectedPhone = null;
        
        for (int i = 0; i < this.phones.length; i++) {
            this.phones[i] = new Phone(this.phoneIcons, this.repairShop, this.width, this.height);
            (this.threads[i] = new Thread(this.phones[i])).start();
        }
    }
    
    // Adds a new phone to the phones[] array
    private Phone[] addPhone(Phone[] phones) {
        Phone[] newPhones = new Phone[phones.length + 1];
        System.arraycopy(phones, 0, newPhones, 0, phones.length);
        newPhones[newPhones.length - 1] = new Phone(++this.phoneIcons, this.repairShop, this.width, this.height);
        newPhones[newPhones.length - 1].width = this.width;
        newPhones[newPhones.length - 1].height = this.height;
        return newPhones;
    }
    
    // Removes a specified phone at index from the phones[] array
    private Phone[] removePhone(Phone[] phones, int index) {
        Phone[] newPhones = new Phone[phones.length - 1];
        phones[index] = phones[phones.length - 1];
        System.arraycopy(phones, 0, newPhones, 0, newPhones.length);
        this.totalDeadPhones++;
        return newPhones;
    }
    
    // Resets phones[] array - kills all remaining phones
    private void removeAllPhones() {
        this.phones = new Phone[0];
    }
    
    // Starts a new runnable Phone object
    private void addPhoneIcon() {
        this.phones = this.addPhone(this.phones);
        this.threads = this.addThread(this.threads);
        (this.threads[this.threads.length - 1] = new Thread(this.phones[this.phones.length - 1])).start();
    }
    
    // Simple method to update all phones health to current set health unless infected
    private void updatePhoneHealth() {
        for (Phone p : phones) {
            if (!p.isPhoneInfected) {
                p.phoneHealth = Panel.setPhoneHealth;
            }
        }
    }
    
    // Adds a new thread to the threads[] array
    private Thread[] addThread(Thread[] threads) {
        Thread[] newThreads = new Thread[threads.length + 1];
        System.arraycopy(threads, 0, newThreads, 0, threads.length);
        return newThreads;
    }
    
    // Method to check pause state and interrupt the simulation threads if true
    private void checkPause() {
        if (isPaused) {
            synchronized (lock) {
                for (Thread t : this.threads) {
                    t.interrupt();
                }
            }
        } else {
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }
    
    // Calculate distance between two phone objects
    private float distance(Phone p1, Phone p2) {
        return (float) Math.sqrt(Math.pow(p1.x - p2.x, 2.0) + Math.pow(p1.y - p2.y, 2.0));
    }
    
    @Override
    public void paint(final Graphics g) {
        super.paintComponent(g);
        
        // Graphics2D instance with active anti-aliasing
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Graphics2D instance with anti-aliasing disabled
        Graphics2D g2dNoAA = (Graphics2D) g2d.create();
        g2dNoAA.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Creating required fonts
        Font headingF = new Font(Font.SANS_SERIF, Font.BOLD, 20);
        Font heading2F = new Font(Font.SANS_SERIF, Font.BOLD, 18);
        Font textF = new Font(Font.SANS_SERIF, Font.PLAIN, 17);
        Font dataF = new Font(Font.SANS_SERIF, Font.BOLD, 17);
        
        this.totalInfectedPhones = 0;
        Phone currentlyRepairing = null;
        
        g2d.setFont(headingF);
        g2d.drawString("Repair Shop", this.repairShop.x, this.repairShop.y);
                
        for (int i = 0; i < this.phones.length; i++) {
            this.phones[i].delay = this.programDelay;
            if (this.phones[i].phoneHealth > 0) {
                BufferedImage composite = new BufferedImage(this.glow.getWidth(null), this.glow.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2dIcons = composite.createGraphics();
                g2dIcons.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                
                if (this.phones[i].isSelected) {
                    g2dIcons.drawImage(this.glow, 0, 0, null);
                }
                
                if (this.phones[i].isPhoneInfected && !this.phones[i].goToRepairShop) {
                    g2dIcons.drawImage(this.infected, 10, 10, null);
                    ++this.totalInfectedPhones;
                } else if (this.phones[i].isPhoneInfected && this.phones[i].goToRepairShop) {
                    g2dIcons.drawImage(this.goingToRepair, 10, 10, null);
                    currentlyRepairing = this.phones[i];
                } else {
                    g2dIcons.drawImage(this.normal, 10, 10, null);
                }
                if (this.phones[i].isPhoneInfected) {
                    for (int j = 0; j < this.phones.length; ++j) {
                        if (this.distance(this.phones[i], this.phones[j]) < this.infectionRadius) {
                            this.phones[j].isPhoneInfected = true;
                        }
                    }
                    this.phones[i].repairShop = this.repairShop;
                }
                
                g2dNoAA.drawImage(composite, this.phones[i].x - 26, this.phones[i].y - 26, null);
            } else {
                if (this.phones[i].goToRepairShop) {
                    this.repairShop.isEmpty = true;
                    currentlyRepairing = null;
                }
                this.phones = this.removePhone(this.phones, i);
            }
        }
        
        // Drawing Scoreboard GUI
        g2d.setColor(new Color(0, 0, 0, 0.8f));
        g2d.fillRoundRect(10, this.height - 210, 220, 200, 20, 20);
        
        // Scoreboard Text, Color, Font
        g2d.setColor(Color.WHITE);
        g2d.setFont(headingF);
        g2d.drawString("Scoreboard", 60, this.height - 180);
        
        // Draw Underline with no AA
        g2dNoAA.setColor(Color.WHITE);
        g2dNoAA.drawLine(40, this.height - 170, 200, this.height - 170);
        
        // Draw Scoreboard Data Text
        g2d.setColor(Color.WHITE);
        g2d.setFont(textF);
        g2d.drawString("Phones Alive", 30, this.height - 140);
        g2d.setColor(Color.GREEN);
        g2d.setFont(dataF);
        g2d.drawString(String.valueOf(this.phones.length), 180, this.height - 140);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(textF);
        g2d.drawString("Infected Phones", 30, this.height - 115);
        g2d.setColor(Color.RED);
        g2d.setFont(dataF);
        g2d.drawString(String.valueOf(this.totalInfectedPhones), 180, this.height - 115);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(textF);
        g2d.drawString("Phones Repairs", 30, this.height - 90);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(dataF);
        g2d.drawString(String.valueOf(Phone.phonesRepaired), 180, this.height - 90);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(textF);
        g2d.drawString("Phones Dead", 30, this.height - 65);
        g2d.setColor(Color.GRAY);
        g2d.setFont(dataF);
        g2d.drawString(String.valueOf(this.totalDeadPhones), 180, this.height - 65);
        
        if (currentlyRepairing == null) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            g2d.drawString("No phone being repaired", 30, this.height - 40);
        } else {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            g2d.drawString("Phone " + currentlyRepairing.id, 30, this.height - 40);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            g2d.drawString("being repaired", currentlyRepairing.id > 9 ? (currentlyRepairing.id > 100 ? 112 : 104) : 94, this.height - 40);
        }
        
        
        // Drawing phone variable GUI
        g2d.setColor(new Color(0, 0, 0, 0.8f));
        g2d.fillRoundRect(this.width - 10 - 220, this.height - 210, 220, 200, 20, 20);
        
        // Phone Variable Text, Color, Font
        g2d.setColor(Color.WHITE);
        if (this.selectedPhone == null) {
            g2d.setFont(headingF);
            g2d.drawString("No Phone", this.width - 167, this.height - 180);
            g2d.setFont(textF);
            g2d.drawString("Click a phone to", this.width - 185, this.height - 140);
            g2d.drawString("show its variables...", this.width - 190, this.height - 120);
        } else {
            g2d.setFont(headingF);
            g2d.setColor(this.selectedPhone.phoneHealth == 0 ? Color.GRAY : Color.WHITE);
            g2d.drawString("Phone " + this.selectedPhone.id, this.selectedPhone.id > 9 ? (this.selectedPhone.id > 99 ? this.width - 170 : this.width - 164) : this.width - 158, this.height - 180);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("Health", this.width - 200, this.height - 140);
            g2d.setColor(this.selectedPhone.isPhoneInfected ? (this.selectedPhone.phoneHealth == 0 ? Color.GRAY : Color.YELLOW) : Color.GREEN);
            g2d.setFont(dataF);
            g2d.drawString(String.valueOf(this.selectedPhone.phoneHealth), this.width - 80, this.height - 140);

            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("Infected", this.width - 200, this.height - 115);
            g2d.setColor(this.selectedPhone.isPhoneInfected ? Color.RED : Color.WHITE);
            g2d.setFont(dataF);
            g2d.drawString((this.selectedPhone.isPhoneInfected ? "Yes" : "No"), this.width - 80, this.height - 115);

            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("Repairs", this.width - 200, this.height - 90);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(dataF);
            g2d.drawString(String.valueOf(this.selectedPhone.repairs), this.width - 80, this.height - 90);

            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("State", this.width - 200, this.height - 65);
            g2d.setColor(this.selectedPhone.phoneHealth > 0 ? Color.GREEN : Color.RED);
            g2d.setFont(dataF);
            g2d.drawString((this.selectedPhone.phoneHealth > 0 ? "Alive" : "Dead"), this.width - 80, this.height - 65);
        }
        
        // Draw Underline with no AA
        g2dNoAA.setColor(Color.WHITE);
        g2dNoAA.drawLine(this.width - 200, this.height - 170, this.width - 40, this.height - 170);
        
        
        
        // Drawing Adjustable Variable Slider GUI
        g2d.setColor(new Color(0, 0, 0, 0.8f));
        g2d.fillRoundRect(this.width - 280, this.height - 210, 40, 200, 20, 20);
        
        // Health Slider
        g2d.setPaint(new GradientPaint(this.width - 220, this.height - 10, Color.YELLOW, this.width - 220, this.height - 210, Color.GREEN));
        g2d.fillRoundRect(this.width - 275, this.height - (setPhoneHealth / 3) - 5, 10, (setPhoneHealth / 3) - 10, 10, 10);
        
        // Spread Radius Slider
        g2d.setPaint(new GradientPaint(this.width - 200, this.height - 10, Color.YELLOW, this.width - 200, this.height - 210, Color.RED));
        g2d.fillRoundRect(this.width - 255, this.height - this.infectionRadius * 4 - 5, 10, this.infectionRadius * 4 - 10, 10, 10);
        
        
        // Drawing Time Controls GUI
        g2d.setColor(new Color(0, 0, 0, 0.8f));
        g2d.fillRoundRect((this.width - 160) / 2 , this.height - 55, 160, 45, 20, 20);
        
        // Drawing button images, and image logic
        g2d.drawImage(isPaused == true ? this.pause : this.pauseD, (this.width / 2) - 60, this.height - 49, this);
        g2d.drawImage(this.programDelay == 12 && !isPaused ? this.play : this.playD, (this.width / 2) - 15, this.height - 49, this);
        g2d.drawImage(this.programDelay == 6 && !isPaused ? this.twoX : twoXD, (this.width / 2) + 30, this.height - 49, this);
        
        
        // Pause Menu GUI and Logic
        if (isPaused) {
            
            // Set Dimensions
            final int pauseMenuX = 720;
            final int pauseMenuY = 640;
            
            // Draw Main Window
            g2d.setColor(new Color(0, 0, 0, 0.8f));
            int roundRectX = (this.width - pauseMenuX) / 2;
            int roundRectY = (this.height - pauseMenuY) / 2 - 100;
            g2d.fillRoundRect(roundRectX, roundRectY, pauseMenuX, pauseMenuY, 20, 20);
            
            
            // Pause Menu Body
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
            FontMetrics fm = g2d.getFontMetrics();
            String title = "Virus Simulation";
            int titleWidth = fm.stringWidth(title);
            int titleHeight = fm.getHeight();
            int x = roundRectX + (pauseMenuX - titleWidth) / 2;
            int y = roundRectY + ((60 - titleHeight) / 2) + fm.getAscent();
            g2d.drawString(title, x, y);
            
            g2d.setFont(textF);
            g2d.drawString("Welcome to the Mobile Phone Virus Simulation by Jack Darlington.",roundRectX + 110, roundRectY + 80);
            g2d.drawString("This game simulates the passing of viruses from one mobile device to another,",roundRectX + 75, roundRectY + 115);
            g2d.drawString("demonstrated by the changing screens on the phones...",roundRectX + 150, roundRectY + 135);
            
            g2d.setFont(dataF);
            g2d.drawImage(this.normal, roundRectX + 210, roundRectY + 170, null);
            g2d.drawString("Normal", roundRectX + 200, roundRectY + 225);
            g2d.drawImage(this.goingToRepair, roundRectX + 340, roundRectY + 170, null);
            g2d.drawString("Going to Repair", roundRectX + 295, roundRectY + 225);
            g2d.drawImage(this.infected, roundRectX + 470, roundRectY + 170, null);
            g2d.drawString("Infected", roundRectX + 456, roundRectY + 225);
            
            g2d.setFont(textF);
            g2d.drawString("If a phone does not make it to the repair shop in time it will die and be taken off the screen.",roundRectX + 30, roundRectY + 280);
            
            g2d.setFont(heading2F);
            g2d.drawString("Controls", roundRectX + 30, roundRectY + 320);
            
            g2d.setFont(textF);
            g2d.drawString("Add New Phone", roundRectX + 50, roundRectY + 350);
            g2d.fillRoundRect(roundRectX + 230, roundRectY + 332, 80, 25, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.setFont(dataF);
            g2d.drawString("Space", roundRectX + 245, roundRectY + 350);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("Infect Selected Phone", roundRectX + 50, roundRectY + 385);
            g2d.fillRoundRect(roundRectX + 230, roundRectY + 367, 80, 25, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.setFont(dataF);
            g2d.drawString("I", roundRectX + 267, roundRectY + 385);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("Infect Random Phone", roundRectX + 50, roundRectY + 420);
            g2d.fillRoundRect(roundRectX + 230, roundRectY + 402, 80, 25, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.setFont(dataF);
            g2d.drawString("R", roundRectX + 264, roundRectY + 420);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("Kill Phones", roundRectX + 50, roundRectY + 455);
            g2d.fillRoundRect(roundRectX + 230, roundRectY + 437, 80, 25, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.setFont(dataF);
            g2d.drawString("C", roundRectX + 264, roundRectY + 455);
            
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("Increase Health", roundRectX + 380, roundRectY + 350);
            g2d.fillRoundRect(roundRectX + 590, roundRectY + 332, 80, 25, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.setFont(dataF);
            g2d.drawString("Up", roundRectX + 619, roundRectY + 350);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("Decrease Health", roundRectX + 380, roundRectY + 385);
            g2d.fillRoundRect(roundRectX + 590, roundRectY + 367, 80, 25, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.setFont(dataF);
            g2d.drawString("Down", roundRectX + 608, roundRectY + 385);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("Increase Spread Radius", roundRectX + 380, roundRectY + 420);
            g2d.fillRoundRect(roundRectX + 590, roundRectY + 402, 80, 25, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.setFont(dataF);
            g2d.drawString("Pg-Up", roundRectX + 606, roundRectY + 420);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("Decrease Spread Radius", roundRectX + 380, roundRectY + 455);
            g2d.fillRoundRect(roundRectX + 590, roundRectY + 437, 80, 25, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.setFont(dataF);
            g2d.drawString("Pg-Down", roundRectX + 594, roundRectY + 455);
            
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(heading2F);
            g2d.drawString("Adjustable Variables", roundRectX + 30, roundRectY + 500);
            
            g2d.setPaint(new GradientPaint(roundRectX + 50, roundRectY + 530, Color.YELLOW, roundRectX + 50, roundRectY + 520, Color.GREEN));
            g2d.fillOval(roundRectX + 50, roundRectY + 520, 10, 10);
            g2d.setFont(textF);
            g2d.drawString("Health", roundRectX + 70, roundRectY + 530);
            
            g2d.setPaint(new GradientPaint(roundRectX + 50, roundRectY + 562, Color.YELLOW, roundRectX + 50, roundRectY + 552, Color.RED));
            g2d.fillOval(roundRectX + 50, roundRectY + 552, 10, 10);
            g2d.drawString("Spread Radius", roundRectX + 70, roundRectY + 562);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(heading2F);
            g2d.drawString("Time Controls", roundRectX + 360, roundRectY + 500);
            
            g2d.drawImage(this.pause, roundRectX + 380, roundRectY + 508, null);
            g2d.setFont(textF);
            g2d.drawString("or", roundRectX + 423, roundRectY + 530);
            g2d.fillRoundRect(roundRectX + 455, roundRectY + 512, 80, 25, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.setFont(dataF);
            g2d.drawString("ESC", roundRectX + 478, roundRectY + 530);
            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("to Pause", roundRectX + 555, roundRectY + 530);
            
            g2d.drawImage(this.play, roundRectX + 380, roundRectY + 540, null);
            g2d.drawString("to Play", roundRectX + 423, roundRectY + 562);
            
            g2d.drawImage(this.twoX, roundRectX + 530, roundRectY + 540, null);
            g2d.drawString("2x Speed", roundRectX + 575, roundRectY + 562);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("Press", roundRectX + 230, roundRectY + 610);         
            g2d.setFont(dataF);
            g2d.drawString("ESCAPE", roundRectX + 280, roundRectY + 610);
            g2d.setFont(textF);
            g2d.drawString("to exit this menu...", roundRectX + 355, roundRectY + 610);
        }
        
        // Death Screen
        // All phones dead (this.phones.length == 0)
        // This code iterates on this.deathFontSize to achieve a text animation, along with the background opacity
        if (this.phones.length == 0) {
            
            // Background colors and fonts
            Color backgroundC = new Color(0, 0, 0, this.backgroundOpacity);
            Font deathF = new Font(Font.SERIF, Font.PLAIN, this.deathFontSize / 100);
            
            // Creating background rectangles
            g2d.setColor(backgroundC);
            g2d.fillRect(0, 0, this.width, this.height);
            
            g2d.setColor(backgroundC);
            g2d.fillRect(0, (this.height / 2) - (this.height / 10), this.width, this.height / 5);
            
            // "You Died" string variables
            String youDied = "You Died";
            g2d.setFont(deathF);
            g2d.setColor(Color.RED);
            
            FontMetrics fm2 = g2d.getFontMetrics();
            int stringX = ((this.width - fm2.stringWidth(youDied)) / 2);
            int stringY = ((this.height - fm2.getHeight()) / 2) + fm2.getAscent() - (this.height / 160);
            
            g2d.drawString(youDied, stringX, stringY);
            
            // Iterate on the font size
            if (this.deathFontSize < 13000) {
                ++this.deathFontSize;
            }
            
            // After some time, display "Press SPACE to start again..."
            if (this.deathFontSize > 10000) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(textF);
                g2d.drawString("Press", (this.width / 2) - 100, (this.height / 2) + 150);
                g2d.setFont(dataF);
                g2d.drawString("SPACE", (this.width / 2) - 50, (this.height / 2) + 150);
                g2d.setFont(textF);
                g2d.drawString("to start again...", (this.width / 2) + 15, (this.height / 2) + 150);
            }
            
            // Iterate on the background opacity
            if (this.backgroundOpacity < 0.8f) {
                this.backgroundOpacity += 0.0001f;
            }
        }
        this.repaint();
    }
    
    @Override
    public void keyPressed(KeyEvent ke) {
        
        // If game is paused, do not listen for most key events
        if (!isPaused) {
            
            // KEY: SPACE - Add a new phone icon to the screen and restart the simulation
            if (ke.getKeyCode() == KeyEvent.VK_SPACE) {
                if (this.phones.length == 0) {
                    this.resetValues();
                } else {
                    this.addPhoneIcon();
                }
            }
            
            // KEY: R - Infect a phone at random
            if (ke.getKeyCode() == KeyEvent.VK_R) {
                this.phones[(int) (Math.random() * this.phones.length)].isPhoneInfected = true;
            }
            
            // KEY: I - Infect the currently selected phone
            if (ke.getKeyCode() == KeyEvent.VK_I) {
                if (this.selectedPhone != null) {
                    this.selectedPhone.isPhoneInfected = true;
                }
            }
            
            // KEY: PAGE UP - Increase the infection radius in increments of 5
            if (ke.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                if (this.infectionRadius % 5 == 0 && this.infectionRadius <= 45) {
                    this.infectionRadius += 5;
                }
            }
            
            // KEY: PAGE DOWN - Decrease the infection radius in decrements of 5
            if (ke.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                if (this.infectionRadius >= 5) {
                    this.infectionRadius -= 5;
                }
            }
            
            // KEY: UP ARROW - Increase the health for all phones in increments of 30
            if (ke.getKeyCode() == KeyEvent.VK_UP) {
                this.updatePhoneHealth();
                for (Phone p : phones) {
                    if (p.phoneHealth == Panel.setPhoneHealth && p.phoneHealth < 600) {
                        p.phoneHealth += 30;
                        Panel.setPhoneHealth = p.phoneHealth;
                    }
                }
                this.updatePhoneHealth();
            }
            
            // KEY: DOWN ARROW - Decrease the health for all phones in decrements of 30
            if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
                this.updatePhoneHealth();
                for (Phone p : phones) {
                    if (p.phoneHealth == Panel.setPhoneHealth && Panel.setPhoneHealth > 30) {
                        p.phoneHealth -= 30;
                        Panel.setPhoneHealth = p.phoneHealth;
                    } 
                }
                this.updatePhoneHealth();
            }
            
            // KEY: C - Clear all phones from the phones[] array and restart the simulation
            if (ke.getKeyCode() == KeyEvent.VK_C) {
                this.removeAllPhones();
            }
        }
        
        // KEY: ESCAPE - Show to pause menu unless simulation over
        if (ke.getKeyCode() == KeyEvent.VK_ESCAPE && this.phones.length > 0) {
            isPaused = !isPaused;
            this.checkPause();
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        
        // Get mouse event location
        int x = e.getX();
        int y = e.getY();
        
        // Check for click on time controls
        Rectangle pauseRect = new Rectangle((this.width / 2) - 60, this.height - 49, this.pause.getWidth(this), this.pause.getHeight(this));
        Rectangle playRect = new Rectangle((this.width / 2) - 15, this.height - 49, this.play.getWidth(this), this.play.getHeight(this));
        Rectangle twoXRect = new Rectangle((this.width / 2) + 30, this.height - 49, this.twoX.getWidth(this), this.twoX.getHeight(this));
        
        if (pauseRect.contains(x, y)) {
            isPaused = true;
        }
        if (playRect.contains(x, y)) {
            programDelay = 12;
            isPaused = false;
        }
        if (twoXRect.contains(x, y)) {
            programDelay = 6;
            isPaused = false;
        }
        this.checkPause();
        
        
        // Check for click on phone icon
        this.selectedPhone = null;
        for (Phone p : phones) {
            p.isSelected = false;
        }
        for (Phone p : phones) {
            
            if (((x < p.x + 50) && (x > p.x - 50)) && ((y < p.y + 50) && (y > p.y - 50))) {
                p.isSelected = true;
                this.selectedPhone = p;
                break;
            }
        }
    }
    
    // When the user resizes the window, resize the available space for the phones to use
    @Override
    public void componentResized(ComponentEvent ce) {
        this.width = this.getWidth();
        this.height = this.getHeight();
        
        for (Phone p : this.phones) {
            if (p.x > this.width || p.y > this.height) {
                p.relocate(this.width, this.height);
            }
            p.width = this.width;
            p.height = this.height;
        }
        
        this.repairShop.relocate(this.width, this.height);
    }
    
    // Unused overriden methods
    @Override public void keyReleased(KeyEvent ke) {}    
    @Override public void keyTyped(KeyEvent ke) {}
    @Override public void componentMoved(ComponentEvent ce) {}
    @Override public void componentShown(ComponentEvent ce) {}
    @Override public void componentHidden(ComponentEvent ce) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    
}