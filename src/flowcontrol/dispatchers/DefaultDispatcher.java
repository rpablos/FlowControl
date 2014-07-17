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
import flowcontrol.queues.FIFOQueueBuffer;
import flowcontrol.queues.Queue;
import flowcontrol.util.MovingAverage;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Ronald
 * @param <T>
 */
public class DefaultDispatcher<T> implements Dispatcher<T> {
    final Queue<T> buffer;
    MovingAverage movingAverage;
    double period; // inverse of output objects per ms
    long lastPopTimeStamp = System.currentTimeMillis();
    public DefaultDispatcher(double outputRate) {
        this(outputRate,8);
    }
    
    public DefaultDispatcher(double outputRate, int bufferLength) {
        this (outputRate,new FIFOQueueBuffer<T>(bufferLength));
    }
    
    public DefaultDispatcher(double outputRate, Queue queue) {
        buffer = queue;
        period = 1000.0/outputRate;
        movingAverage = new MovingAverage((long) period);
    }
    public boolean put(T t) {
        synchronized (buffer) {
            if (buffer.push(t)) {
                buffer.notify();
                return true;
            }
        }
        notifyListeners(t);
        return false;
    }
    
    public T get() {
        T head = buffer.peekHead();
        if (head == null)
            return null;
        long timeToWait = TimeToWait();
        long currentTime = System.currentTimeMillis();
        if ((currentTime-lastPopTimeStamp) < timeToWait) 
            return null;
        T result = buffer.pop();
        currentTime = System.currentTimeMillis();
        movingAverage.pushValue(currentTime-lastPopTimeStamp);
        lastPopTimeStamp = currentTime;
        return result;
    }
    
    public T getBlocking() throws InterruptedException {
        T head;
        synchronized (buffer) {
            while ((head = buffer.peekHead()) == null)
                buffer.wait();       
        }
        long timeToWait = TimeToWait();
        long currentTime;
        while (((currentTime=System.currentTimeMillis())-lastPopTimeStamp) < timeToWait)  {
            Thread.sleep(timeToWait- (currentTime-lastPopTimeStamp));
        }
        T result = buffer.pop();
        currentTime = System.currentTimeMillis();
        movingAverage.pushValue(currentTime-lastPopTimeStamp);
        lastPopTimeStamp = currentTime;
        return result;

        
    }
    @Override
    public T getBlocking(long timeout) throws InterruptedException {
        if (timeout == 0)
            return getBlocking();
        T head;
        long initTime = System.currentTimeMillis(),tempTime;

        synchronized (buffer) {
            while (((head = buffer.peekHead()) == null) && ((tempTime=System.currentTimeMillis())-initTime)<timeout)
                buffer.wait(timeout-(tempTime-initTime));       
        }
        if (head == null)
            return null;
        long timeToWait = TimeToWait();

        long currentTime;
        while (( (currentTime=System.currentTimeMillis())-lastPopTimeStamp) < timeToWait) {
            if ((timeout-(currentTime-initTime)) < (timeToWait- (currentTime-lastPopTimeStamp))) {
                Thread.sleep(Math.max(0,timeout-(currentTime-initTime)));
                return null;
            }
            else
                Thread.sleep(timeToWait- (currentTime-lastPopTimeStamp));
        }
        
        T result = buffer.pop();
        currentTime = System.currentTimeMillis();
        movingAverage.pushValue(currentTime-lastPopTimeStamp);
        lastPopTimeStamp = currentTime;
        return result;

        
    }
    
    protected long TimeToWait() {
        return Math.max(0L,(long) _TimeToWait());
    }
    protected double _TimeToWait() {
        return period*2-movingAverage.getAverage();
    }
    @Override
    public double getOutputRate(){
        return 1000.0/period;
    }
    Set<DispatcherLossListener<T>> listeners = null;
    @Override
    public void addDispatcherLossListener(DispatcherLossListener<T> listener) {
        if (listener == null)
            return;
        if (listeners == null)
            listeners = new LinkedHashSet<>();
        listeners.add(listener);
    }

    @Override
    public void removeDispatcherLossListener(DispatcherLossListener<T> listener) {
        if (listeners != null)
            listeners.remove(listener);
    }
    
    private void notifyListeners(T t) {
        if (listeners != null) {
            for (DispatcherLossListener<T> listener: listeners) {
                listener.onLoss(this, t);
            }
        }
    }
}
