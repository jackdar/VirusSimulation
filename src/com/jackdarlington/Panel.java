/*
 * Created by Jack Darlington | 2023
 */

package com.jackdarlington;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * @author Jack Darlington
 * Student ID: 19082592
 * Date: 18/03/2023
 */

public class Panel extends JPanel implements KeyListener, ComponentListener, MouseListener {
    
    static GameComponent gC;
    
    static final Object lock = new Object();
    
    public static int setPhoneHealth = 300;
    public static boolean isPaused = true;
    
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
        
        gC = new GameComponent(this);
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
        this.deathFontSize = 900;
        this.programDelay = 12;
        this.backgroundOpacity = 0.0f;
        this.selectedPhone = null;
        
        for (int i = 0; i < this.phones.length; i++) {
            this.phones[i] = new Phone(this.phoneIcons, this.repairShop, this.width, this.height);
            (this.threads[i] = new Thread(this.phones[i])).start();
        }
    }
    
    // Adds a new phone to the phones[] array
    public Phone[] addPhone(Phone[] phones) {
        Phone[] newPhones = new Phone[phones.length + 1];
        System.arraycopy(phones, 0, newPhones, 0, phones.length);
        newPhones[newPhones.length - 1] = new Phone(++this.phoneIcons, this.repairShop, this.width, this.height);
        newPhones[newPhones.length - 1].width = this.width;
        newPhones[newPhones.length - 1].height = this.height;
        return newPhones;
    }
    
    // Removes a specified phone at index from the phones[] array
    public Phone[] removePhone(Phone[] phones, int index) {
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
    public float distance(Phone p1, Phone p2) {
        return (float) Math.sqrt(Math.pow(p1.x - p2.x, 2.0) + Math.pow(p1.y - p2.y, 2.0));
    }
    
    @Override
    public void paint(Graphics g) {
        super.paintComponent(g);
        
        // REFACTORED: Moved main game logic to separate class
        // Paint main game component, including game logic
        gC.paintComponent(g);
        
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