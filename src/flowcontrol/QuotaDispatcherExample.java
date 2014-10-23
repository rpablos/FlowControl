/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flowcontrol;

import flowcontrol.dispatchers.DefaultDispatcher;
import flowcontrol.dispatchers.Dispatcher;
import flowcontrol.dispatchers.QuotaDispatcher;
import flowcontrol.events.DispatcherLossListener;
import flowcontrol.events.DispatcherQuotaExhaustedListener;
import flowcontrol.events.DispatcherQuotaRenewalListener;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author ronald
 */
public class QuotaDispatcherExample {
    public static void main(String[] args)   {
        System.out.println("Qouta dispatcher config: 10 units of quota in 30 seconds. Queue size: 10.");
        final QuotaDispatcher<String> dispatcher = new QuotaDispatcher<>(10,new Date(),30,TimeUnit.SECONDS,10);
        dispatcher.addDispatcherLossListener(new DispatcherLossListener<String>() {
            @Override
            public void onLoss(Dispatcher<String> dw, String t) {
                System.out.println("Loss in dispatcher: "+t);
            }
        });
        dispatcher.addDispatcherQuotaExhaustedListener(new DispatcherQuotaExhaustedListener() {
            boolean firstTime = true;
            @Override
            public void onQuotaExhausted(QuotaDispatcher dispatcher) {
                System.out.println("Quota Exhausted: "+dispatcher.getQuota());
                if (firstTime) {
                    firstTime = false;
                    System.out.println("First Time. Increase the quota a little bit");
                    dispatcher.setQuota(dispatcher.getQuota()+10);
                } else {
                    dispatcher.setQuota(10);
                }
            }
        });
        dispatcher.addDispatcherQuotaRenewalListener(new DispatcherQuotaRenewalListener() {
            boolean firstTime = true;
            @Override
            public void onQuotaRenewal(QuotaDispatcher dispatcher) {
                System.out.println("Quota renewal: "+dispatcher.getQuota());
                if (firstTime) {
                    firstTime = false;
                    System.out.println("First Time Renewal. Change to 20 seconds the period.");
                    dispatcher.changeQuotaPeriodRenewal(new Date(),20,TimeUnit.SECONDS);
                }
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    
                    System.out.println("Start of generator.");
                    for (int i = 0; i < 1000; i++) {
                        dispatcher.put(""+i);
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException ex){}
            
            }
        }).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println("Start of consumer.");

                try {
                for (int i = 0; i < 1000; i++) {
                    
                    System.out.print(dispatcher.getBlocking());
                    System.out.println(": "+System.currentTimeMillis());
                }
                } catch(InterruptedException ex) {}
            }
        }).start();
    }
}
