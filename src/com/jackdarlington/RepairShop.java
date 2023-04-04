/*
 * Created by Jack Darlington | 2023
 */

package com.jackdarlington;

/**
 * @author Jack Darlington
 * Student ID: 19082592
 * Date: 18/03/2023
 */

public final class RepairShop {
    
    public int x, y;
    public boolean isEmpty;
    
    public RepairShop(int x, int y) {
        this.relocate(x, y);
        
        this.isEmpty = true;
    }
    
    public void relocate(int x, int y) {
        this.x = (x - 120) / 2;
        this.y = 35;
    }
    
    @Override
    public String toString() {
        return "Repair Shop";
    }
    
}