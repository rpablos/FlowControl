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

import java.util.List;

/**
 *
 * @author rpablos
 */
public class DispatcherReaderAggregator<T> implements DispatcherReader<T>{
    DispatcherReader<T>[] dispatchers;
    protected int nextDispatcherIndex = 0;
    long minPeriod;
    double rate;
    
    public DispatcherReaderAggregator(DispatcherReader<T>[] dispatchers) {
        if (dispatchers.length == 0)
            throw new IllegalArgumentException("Number of dispatchers must be greater than zero");
        this.dispatchers = dispatchers;
        minPeriod = findMinPeriod(this.dispatchers);
        rate = sumRates(this.dispatchers);
    }

    public DispatcherReaderAggregator(List<DispatcherReader<T>> dispatchers) {
        Object[] array = dispatchers.toArray();
        if (array.length == 0)
            throw new IllegalArgumentException("Number of dispatchers must be greater than zero");
        this.dispatchers = new DispatcherReader[array.length];
        for (int i = 0; i < array.length; i++)
            this.dispatchers[i] = (DispatcherReader<T>) array[i];
        minPeriod = findMinPeriod(this.dispatchers);
        rate = sumRates(this.dispatchers);
    }
    
    
    @Override
    public T get() {
        for (int i = 0; i < dispatchers.length; i++) {
            int index = getModuleIndex(nextDispatcherIndex+i);
            T t = dispatchers[index].get();
            if (t != null) {
                nextDispatcherIndex = getModuleIndex(index+1);
                return t;
            }
        }
        return null;
    }

    @Override
    public T getBlocking() throws InterruptedException {
        T result;
        while ((result = get()) == null)
            Thread.sleep(minPeriod);
        return result;
    }
    public T getBlocking(long timeout) throws InterruptedException {
        if (timeout == 0)
            return getBlocking();
        T result;
        long initTime = System.currentTimeMillis();
        long sleepTime = Math.min(minPeriod, timeout);
        long currentTime;
        while (((result = get()) == null) && ((currentTime=System.currentTimeMillis())-initTime)<timeout) {
            Thread.sleep(sleepTime);
            sleepTime = Math.min(minPeriod,timeout-(currentTime-initTime));
        }
        return result;
    }
    
    @Override
    public double getOutputRate() {
        return rate;
    }
    
    private int getModuleIndex(int index){
        return (index) % dispatchers.length;
    }

    private long findMinPeriod(DispatcherReader<T>[] dispatchers) {
        double result = 1000.0/dispatchers[0].getOutputRate();
        for (int i = 1; i < dispatchers.length; i++) {
             double period = 1000.0/dispatchers[i].getOutputRate();
             if (period < result)
                 result = period;
        }
        return (long) result;
    }
    private double sumRates(DispatcherReader<T>[] dispatchers) {
        double result = 0;
        for (DispatcherReader<T> dispatcher:dispatchers)
            result += dispatcher.getOutputRate();
        return result;
    }

    
}
