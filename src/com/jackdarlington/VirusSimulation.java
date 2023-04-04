/*
 * Created by Jack Darlington | 2023
 */

package com.jackdarlington;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * @author Jack Darlington
 * Student ID: 19082592
 * Date: 18/03/2023
 */

public class VirusSimulation {

    JFrame frame;
    Panel panel;
    
    public VirusSimulation() {
        this.frame = new JFrame("Virus Simulation");
        
        this.frame.setIconImage(new ImageIcon(getClass().getClassLoader().getResource("icons/phone-infected.png")).getImage());
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.panel = new Panel();
        
        this.frame.getContentPane().add(this.panel);
        this.frame.setSize(1000, 1000);
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        new VirusSimulation();
    }
    
}