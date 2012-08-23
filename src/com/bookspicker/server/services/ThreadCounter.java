package com.bookspicker.server.services;

/**
 * A safe thread counter. Used to count how many services are still being inquired. 
 * @author Jonathan
 *
 */
public class ThreadCounter {
    
    private int counter;
    
    public ThreadCounter(int c) {
        counter = c;
    }
    
    public synchronized void decreaseCounter() {
        this.counter--;
    }
    
    public synchronized void increaseCounter() {
        this.counter++;
    }
    
    public synchronized int getCount() {
        return counter;
    }
}
