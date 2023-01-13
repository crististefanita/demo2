package com.web.action.basic;

public class WaitAction {
    public static void delayFor(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
