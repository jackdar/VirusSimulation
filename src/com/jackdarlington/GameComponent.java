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
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * @author Jack Darlington
 * Student ID: 19082592
 * Date: 05/04/2023
 */

public class GameComponent extends JComponent {
    
    Panel p;
    
    public GameComponent(Panel p) {
        this.p = p;
    }
    
    @Override
    public void paintComponent(Graphics g) {
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
        
        p.totalInfectedPhones = 0;
        Phone currentlyRepairing = null;
        
        g2d.setFont(headingF);
        g2d.drawString("Repair Shop", p.repairShop.x, p.repairShop.y);
                
        for (int i = 0; i < p.phones.length; i++) {
            p.phones[i].delay = p.programDelay;
            if (p.phones[i].phoneHealth > 0) {
                BufferedImage composite = new BufferedImage(p.glow.getWidth(null), p.glow.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2dIcons = composite.createGraphics();
                g2dIcons.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                
                if (p.phones[i].isSelected) {
                    g2dIcons.drawImage(p.glow, 0, 0, null);
                }
                
                if (p.phones[i].isPhoneInfected && !p.phones[i].goToRepairShop) {
                    g2dIcons.drawImage(p.infected, 10, 10, null);
                    ++p.totalInfectedPhones;
                } else if (p.phones[i].isPhoneInfected && p.phones[i].goToRepairShop) {
                    g2dIcons.drawImage(p.goingToRepair, 10, 10, null);
                    currentlyRepairing = p.phones[i];
                } else {
                    g2dIcons.drawImage(p.normal, 10, 10, null);
                }
                if (p.phones[i].isPhoneInfected) {
                    for (int j = 0; j < p.phones.length; ++j) {
                        if (p.distance(p.phones[i], p.phones[j]) < p.infectionRadius) {
                            p.phones[j].isPhoneInfected = true;
                        }
                    }
                    p.phones[i].repairShop = p.repairShop;
                }
                
                g2dNoAA.drawImage(composite, p.phones[i].x - 26, p.phones[i].y - 26, null);
            } else {
                if (p.phones[i].goToRepairShop) {
                    p.repairShop.isEmpty = true;
                    currentlyRepairing = null;
                }
                p.phones = p.removePhone(p.phones, i);
            }
        }
        
        // Drawing Scoreboard GUI
        g2d.setColor(new Color(0, 0, 0, 0.8f));
        g2d.fillRoundRect(10, p.height - 210, 220, 200, 20, 20);
        
        // Scoreboard Text, Color, Font
        g2d.setColor(Color.WHITE);
        g2d.setFont(headingF);
        g2d.drawString("Scoreboard", 60, p.height - 180);
        
        // Draw Underline with no AA
        g2dNoAA.setColor(Color.WHITE);
        g2dNoAA.drawLine(40, p.height - 170, 200, p.height - 170);
        
        // Draw Scoreboard Data Text
        g2d.setColor(Color.WHITE);
        g2d.setFont(textF);
        g2d.drawString("Phones Alive", 30, p.height - 140);
        g2d.setColor(Color.GREEN);
        g2d.setFont(dataF);
        g2d.drawString(String.valueOf(p.phones.length), 180, p.height - 140);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(textF);
        g2d.drawString("Infected Phones", 30, p.height - 115);
        g2d.setColor(Color.RED);
        g2d.setFont(dataF);
        g2d.drawString(String.valueOf(p.totalInfectedPhones), 180, p.height - 115);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(textF);
        g2d.drawString("Phones Repairs", 30, p.height - 90);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(dataF);
        g2d.drawString(String.valueOf(Phone.phonesRepaired), 180, p.height - 90);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(textF);
        g2d.drawString("Phones Dead", 30, p.height - 65);
        g2d.setColor(Color.GRAY);
        g2d.setFont(dataF);
        g2d.drawString(String.valueOf(p.totalDeadPhones), 180, p.height - 65);
        
        if (currentlyRepairing == null) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            g2d.drawString("No phone being repaired", 30, p.height - 40);
        } else {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            g2d.drawString("Phone " + currentlyRepairing.id, 30, p.height - 40);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            g2d.drawString("being repaired", currentlyRepairing.id > 9 ? (currentlyRepairing.id > 100 ? 112 : 104) : 94, p.height - 40);
        }
        
        // Drawing phone variable GUI
        g2d.setColor(new Color(0, 0, 0, 0.8f));
        g2d.fillRoundRect(p.width - 10 - 220, p.height - 210, 220, 200, 20, 20);
        
        // Phone Variable Text, Color, Font
        g2d.setColor(Color.WHITE);
        if (p.selectedPhone == null) {
            g2d.setFont(headingF);
            g2d.drawString("No Phone", p.width - 167, p.height - 180);
            g2d.setFont(textF);
            g2d.drawString("Click a phone to", p.width - 185, p.height - 140);
            g2d.drawString("show its variables...", p.width - 190, p.height - 120);
        } else {
            g2d.setFont(headingF);
            g2d.setColor(p.selectedPhone.phoneHealth == 0 ? Color.GRAY : Color.WHITE);
            g2d.drawString("Phone " + p.selectedPhone.id, p.selectedPhone.id > 9 ? (p.selectedPhone.id > 99 ? p.width - 170 : p.width - 164) : p.width - 158, p.height - 180);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("Health", p.width - 200, p.height - 140);
            g2d.setColor(p.selectedPhone.isPhoneInfected ? (p.selectedPhone.phoneHealth == 0 ? Color.GRAY : Color.YELLOW) : Color.GREEN);
            g2d.setFont(dataF);
            g2d.drawString(String.valueOf(p.selectedPhone.phoneHealth), p.width - 80, p.height - 140);

            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("Infected", p.width - 200, p.height - 115);
            g2d.setColor(p.selectedPhone.isPhoneInfected ? Color.RED : Color.WHITE);
            g2d.setFont(dataF);
            g2d.drawString((p.selectedPhone.isPhoneInfected ? "Yes" : "No"), p.width - 80, p.height - 115);

            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("Repairs", p.width - 200, p.height - 90);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(dataF);
            g2d.drawString(String.valueOf(p.selectedPhone.repairs), p.width - 80, p.height - 90);

            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("State", p.width - 200, p.height - 65);
            g2d.setColor(p.selectedPhone.phoneHealth > 0 ? Color.GREEN : Color.RED);
            g2d.setFont(dataF);
            g2d.drawString((p.selectedPhone.phoneHealth > 0 ? "Alive" : "Dead"), p.width - 80, p.height - 65);
        }
        
        // Draw Underline with no AA
        g2dNoAA.setColor(Color.WHITE);
        g2dNoAA.drawLine(p.width - 200, p.height - 170, p.width - 40, p.height - 170);
        
        // Drawing Adjustable Variable Slider GUI
        g2d.setColor(new Color(0, 0, 0, 0.8f));
        g2d.fillRoundRect(p.width - 280, p.height - 210, 40, 200, 20, 20);
        
        // Health Slider
        g2d.setPaint(new GradientPaint(p.width - 220, p.height - 10, Color.YELLOW, p.width - 220, p.height - 210, Color.GREEN));
        g2d.fillRoundRect(p.width - 275, p.height - (Panel.setPhoneHealth / 3) - 5, 10, (Panel.setPhoneHealth / 3) - 10, 10, 10);
        
        // Spread Radius Slider
        g2d.setPaint(new GradientPaint(p.width - 200, p.height - 10, Color.YELLOW, p.width - 200, p.height - 210, Color.RED));
        g2d.fillRoundRect(p.width - 255, p.height - p.infectionRadius * 4 - 5, 10, p.infectionRadius * 4 - 10, 10, 10);
        
        // Drawing Time Controls GUI
        g2d.setColor(new Color(0, 0, 0, 0.8f));
        g2d.fillRoundRect((p.width - 160) / 2 , p.height - 55, 160, 45, 20, 20);
        
        // Drawing button images, and image logic
        g2d.drawImage(Panel.isPaused == true ? p.pause : p.pauseD, (p.width / 2) - 60, p.height - 49, p);
        g2d.drawImage(p.programDelay == 12 && !Panel.isPaused ? p.play : p.playD, (p.width / 2) - 15, p.height - 49, p);
        g2d.drawImage(p.programDelay == 6 && !Panel.isPaused ? p.twoX : p.twoXD, (p.width / 2) + 30, p.height - 49, p);
        
        // Pause Menu GUI and Logic
        if (Panel.isPaused) {
            
            // Set Dimensions
            final int pauseMenuX = 720;
            final int pauseMenuY = 640;
            
            // Draw Main Window
            g2d.setColor(new Color(0, 0, 0, 0.8f));
            int roundRectX = (p.width - pauseMenuX) / 2;
            int roundRectY = (p.height - pauseMenuY) / 2 - 100;
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
            g2d.drawImage(p.normal, roundRectX + 210, roundRectY + 170, null);
            g2d.drawString("Normal", roundRectX + 200, roundRectY + 225);
            g2d.drawImage(p.goingToRepair, roundRectX + 340, roundRectY + 170, null);
            g2d.drawString("Going to Repair", roundRectX + 295, roundRectY + 225);
            g2d.drawImage(p.infected, roundRectX + 470, roundRectY + 170, null);
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
            
            g2d.drawImage(p.pause, roundRectX + 380, roundRectY + 508, null);
            g2d.setFont(textF);
            g2d.drawString("or", roundRectX + 423, roundRectY + 530);
            g2d.fillRoundRect(roundRectX + 455, roundRectY + 512, 80, 25, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.setFont(dataF);
            g2d.drawString("ESC", roundRectX + 478, roundRectY + 530);
            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("to Pause", roundRectX + 555, roundRectY + 530);
            
            g2d.drawImage(p.play, roundRectX + 380, roundRectY + 540, null);
            g2d.drawString("to Play", roundRectX + 423, roundRectY + 562);
            
            g2d.drawImage(p.twoX, roundRectX + 530, roundRectY + 540, null);
            g2d.drawString("2x Speed", roundRectX + 575, roundRectY + 562);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(textF);
            g2d.drawString("Press", roundRectX + 230, roundRectY + 610);         
            g2d.setFont(dataF);
            g2d.drawString("ESCAPE", roundRectX + 280, roundRectY + 610);
            g2d.setFont(textF);
            g2d.drawString("to exit p menu...", roundRectX + 355, roundRectY + 610);
        }
        
        // REFACTORED: Sped up opacity increase and font size enlargement
        // Death Screen
        // All phones dead (p.phones.length == 0)
        // This code iterates on p.deathFontSize to achieve a text animation, along with the background opacity
        if (p.phones.length == 0) {
            
            // Background colors and fonts
            Color backgroundC = new Color(0, 0, 0, p.backgroundOpacity);
            Font deathF = new Font(Font.SERIF, Font.PLAIN, p.deathFontSize / 10);
            
            // Creating background rectangles
            g2d.setColor(backgroundC);
            g2d.fillRect(0, 0, p.width, p.height);
            
            g2d.setColor(backgroundC);
            g2d.fillRect(0, (p.height / 2) - (p.height / 10), p.width, p.height / 5);
            
            // "You Died" string variables
            String youDied = "You Died";
            g2d.setFont(deathF);
            g2d.setColor(Color.RED);
            
            FontMetrics fm2 = g2d.getFontMetrics();
            int stringX = ((p.width - fm2.stringWidth(youDied)) / 2);
            int stringY = ((p.height - fm2.getHeight()) / 2) + fm2.getAscent() - (p.height / 160);
            
            g2d.drawString(youDied, stringX, stringY);
            
            // Iterate on the font size
            if (p.deathFontSize < 1300) {
                ++p.deathFontSize;
            }
            
            // After some time, display "Press SPACE to start again..."
            if (p.deathFontSize > 800) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(textF);
                g2d.drawString("Press", (p.width / 2) - 100, (p.height / 2) + 150);
                g2d.setFont(dataF);
                g2d.drawString("SPACE", (p.width / 2) - 50, (p.height / 2) + 150);
                g2d.setFont(textF);
                g2d.drawString("to start again...", (p.width / 2) + 15, (p.height / 2) + 150);
            }
            
            // Iterate on the background opacity
            if (p.backgroundOpacity < 0.8f) {
                p.backgroundOpacity += 0.001f;
            }
        }
    }

}
