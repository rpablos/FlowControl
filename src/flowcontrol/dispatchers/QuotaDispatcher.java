/*
 * Copyright 2014 Ronald Pablos.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package flowcontrol.dispatchers;

import flowcontrol.events.DispatcherLossListener;
import flowcontrol.events.DispatcherQuotaExhaustedListener;
import flowcontrol.events.DispatcherQuotaRenewalListener;
import flowcontrol.queues.FIFOQueueBuffer;
import flowcontrol.queues.Queue;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Ronald
 * @param <T>
 */
public class QuotaDispatcher<T> implements Dispatcher<T> {
    static final Timer timer = new Timer("QuotaDispatcherTimer", true);;
    final Queue<T> queue;
    
    int consumedUnits = 0;
    int quota;
    long period;
    TimerTask timerTask;

    public QuotaDispatcher(int quota, Date firstTime, long time, TimeUnit unit,int queuesize) {
        this.queue = new FIFOQueueBuffer<>(queuesize);
        this.quota = quota;
        period = unit.toMillis(time);
        timer.schedule(timerTask=new QuotaTimerTask(), firstTime, period);
    }
    
    @Override
    public Queue<T> getQueue() {
        return queue;
    }
    public void setQuota(int quota) {
        this.quota = quota;
        
    }
    public int getQuota() {
        return quota;
    }
    public int getConsumed() {
        return consumedUnits;
    }
    public boolean isQuotaExhausted() {
        return getConsumed() >= getQuota();
    }
    public void changeQuotaPeriodRenewal(Date firstTime,long time, TimeUnit unit) {
        timerTask.cancel();
        period = unit.toMillis(time);
        timer.schedule(timerTask=new QuotaTimerTask(), firstTime, period);
    }
    
    @Override
    public T get() {
        synchronized (queue) {
            return queue.pop();
        }
    }

    @Override
    public T getBlocking() throws InterruptedException {
        T head;
        synchronized (queue) {
            while ((head = queue.peekHead()) == null)
                queue.wait();   
            return queue.pop();
        }
    }

    @Override
    public T getBlocking(long timeout) throws InterruptedException {
        if (timeout == 0)
            return getBlocking();
        T head;
        long initTime = System.currentTimeMillis(),tempTime;

        synchronized (queue) {
            while (((head = queue.peekHead()) == null) && ((tempTime=System.currentTimeMillis())-initTime)<timeout)
                queue.wait(timeout-(tempTime-initTime));       
            return queue.pop();
        }
        
    }

    @Override
    public double getOutputRate() {
        return quota*1000d/period;
    }

    /** Put an element into dispatcher.
     * 
     * This action will trigger the quota exhaustion event and loss events through 
     * the listeners.
     *
     * @param t
     * @return
     */
    @Override
    public boolean put(T t) {
        if (isQuotaExhausted()) {
            notifyQuotaExhausted();
        }
        synchronized (queue) {
            if ((consumedUnits < quota)  && (queue.push(t))) {
                consumedUnits++;
                queue.notify();
                return true;
            }
        }
        notifyLossListeners(t);
        return false;
    }
    
    Set<DispatcherLossListener<T>> lossListeners = null;
    @Override
    public void addDispatcherLossListener(DispatcherLossListener<T> listener) {
        if (listener == null)
            return;
        if (lossListeners == null)
            lossListeners = new LinkedHashSet<>();
        lossListeners.add(listener);
    }

    @Override
    public void removeDispatcherLossListener(DispatcherLossListener<T> listener) {
        if (lossListeners != null)
            lossListeners.remove(listener);
    }
    
    private void notifyLossListeners(T t) {
        if (lossListeners != null) {
            for (DispatcherLossListener<T> listener: lossListeners) {
                listener.onLoss(this, t);
            }
        }
    }
    
    Set<DispatcherQuotaExhaustedListener> quotaExhaustedListeners = null;

    /** Add a listener for the Quota Exhaustion event.
     * <p>
     *  One can modify the quota inside the listener code, for example for adding a bonus.
     *  
     *
     * @param listener
     */
    public void addDispatcherQuotaExhaustedListener(DispatcherQuotaExhaustedListener listener) {
        if (listener == null)
            return;
        if (quotaExhaustedListeners == null)
            quotaExhaustedListeners = new LinkedHashSet<>();
        quotaExhaustedListeners.add(listener);
    }

    public void removeDispatcherQuotaExhaustedListener(DispatcherQuotaExhaustedListener listener) {
        if (quotaExhaustedListeners != null)
            quotaExhaustedListeners.remove(listener);
    }
    private void notifyQuotaExhausted() {
        if (quotaExhaustedListeners != null) {
            for (DispatcherQuotaExhaustedListener listener: quotaExhaustedListeners) {
                listener.onQuotaExhausted(this);
            }
        }
    }
    
    Set<DispatcherQuotaRenewalListener> quotaRenewalListeners = null;

    /** Add a listener for the quota renewal event.
     * <p>
     *  One can modify the period quota renewal, for example.
     *
     * @param listener
     */
    public void addDispatcherQuotaRenewalListener(DispatcherQuotaRenewalListener listener) {
        if (listener == null)
            return;
        if (quotaRenewalListeners == null)
            quotaRenewalListeners = new LinkedHashSet<>();
        quotaRenewalListeners.add(listener);
    }

    public void removeDispatcherQuotaRenewalListener(DispatcherQuotaRenewalListener listener) {
        if (quotaRenewalListeners != null)
            quotaRenewalListeners.remove(listener);
    }
    private void notifyQuotaRenewal() {
        if (quotaRenewalListeners != null) {
            for (DispatcherQuotaRenewalListener listener: quotaRenewalListeners) {
                listener.onQuotaRenewal(this);
            }
        }
    }
    
    private class QuotaTimerTask extends TimerTask {

        @Override
        public void run() {
            synchronized (queue) {
                consumedUnits = 0;
            }
            notifyQuotaRenewal();
        }
        
    }
}
