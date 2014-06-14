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

package flowcontrol;

import flowcontrol.util.MovingAverage;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author rpablos
 */
public class DispatcherReaderClassificator<T> implements DispatcherReader<T>{
    DispatcherReader<T>[] dispatchers;
    MovingAverage movingAverage;
    double period; // inverse of output objects per ms
    long lastPopTimeStamp = System.currentTimeMillis();
    
    public DispatcherReaderClassificator(double outputRate, DispatcherReader<T>[] dispatchers) {
        this(outputRate,Arrays.asList(dispatchers));
    }

    public DispatcherReaderClassificator(double outputRate,List<DispatcherReader<T>> dispatchers) {
        Object[] array = dispatchers.toArray();
        if (array.length == 0)
            throw new IllegalArgumentException("Number of dispatchers must be greater than zero");
        this.dispatchers = new DispatcherReader[array.length];
        for (int i = 0; i < array.length; i++)
            this.dispatchers[i] = (DispatcherReader<T>) array[i];
        period = 1000.0/outputRate;
        movingAverage = new MovingAverage(5, (int) period);
    }
    
    private T getFromDispatcherReaders() {
        for (int i = 0; i < dispatchers.length; i++) {
            T t = dispatchers[i].get();
            if (t != null) {
                return t;
            }
        }
        return null;
    }
    @Override
    public T get() {
        long timeToWait = TimeToWait();
        long currentTime = System.currentTimeMillis();
        if ((currentTime-lastPopTimeStamp) < timeToWait) 
            return null;
        T result = getFromDispatcherReaders();
        if (result == null)
            return null;
        currentTime = System.currentTimeMillis();
        movingAverage.pushValue(currentTime-lastPopTimeStamp);
        lastPopTimeStamp = currentTime;
        return result;
        
    }

    @Override
    public T getBlocking() throws InterruptedException {
        T result;
        while ((result = get()) == null)
            Thread.sleep((long) period);
        return result;
    }

    @Override
    public T getBlocking(long timeout) throws InterruptedException {
        if (timeout == 0)
            return getBlocking();
        T result;
        long initTime = System.currentTimeMillis();
        long sleepTime = Math.min((long)period, timeout);
        long currentTime;
        while (((result = get()) == null) && ((currentTime=System.currentTimeMillis())-initTime)<timeout) {
            Thread.sleep(sleepTime);
            sleepTime = Math.min((long)period,timeout-(currentTime-initTime));
        }
        return result;
    }

    @Override
    public double getOutputRate() {
        return 1000/period;
    }
    private long TimeToWait() {
        return (long) (period*2-movingAverage.getAverage());
    }
}
