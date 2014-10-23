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

package flowcontrol.connector;

import flowcontrol.DispatcherReader;
import flowcontrol.DispatcherWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author rpablos
 * @param <T>
 */
public class ConnectorManager<T> {
    final List<Connector<T>> connectors = new ArrayList<>();
    long minPeriod = 1000;
    Thread connectorManagerThread = null;
    ConnectorManagerTask connectorManagerRunnable = null;

    
    public Connector<T> createConnector(DispatcherReader<T> dr, DispatcherWriter<T> dw) {
        Connector_impl result = new Connector_impl(dr, dw);
        synchronized(connectors){
            connectors.add(result);
        }
        minPeriod = findMinPeriod();
        return result;
    }
    public void deleteConnector(Connector<T> connector){
        synchronized(connectors){
            connectors.remove(connector);
            if (connectors.isEmpty() && connectorManagerThread.isAlive()){ 
                connectorManagerThread = null;
                connectorManagerRunnable.terminate();
            }
            
        }
        minPeriod = findMinPeriod();
        
    }
    public List<Connector<T>> getConnectors(){
        return Collections.unmodifiableList(connectors);
    }

    private long findMinPeriod() {
        synchronized (connectors) {
            if (connectors.isEmpty())
                return 1000; // 1seg
            Iterator<Connector<T>> iterator = connectors.iterator();
            double result = 1000.0/iterator.next().getDispatcherReader().getOutputRate();
            while (iterator.hasNext()) {
                 double period = 1000.0/iterator.next().getDispatcherReader().getOutputRate();
                 if (period < result)
                     result = period;
            }
            return (long) result;
        }
    }
    private class Connector_impl implements Connector<T>{
        DispatcherReader<T> dr;
        DispatcherWriter<T> dw;
        boolean stopped = true;
        public Connector_impl(DispatcherReader<T> dr, DispatcherWriter<T> dw) {
            this.dr = dr;
            this.dw = dw;
        }

        @Override
        public DispatcherReader<T> getDispatcherReader() {
            return dr;
        }

        @Override
        public DispatcherWriter<T> getDispatcherWriter() {
            return dw;
        }

        @Override
        public void start() {
            stopped = false;
            synchronized (connectors) {
                if (connectorManagerThread == null || !connectorManagerThread.isAlive()){
                    connectorManagerRunnable = new ConnectorManagerTask();
                    connectorManagerThread = new Thread(connectorManagerRunnable);
                    connectorManagerThread.start();
                }
            }
        }

        @Override
        public void stop() {
            stopped = true;
        }
        public boolean isStopped() {
            return stopped;
        }
        
    }
    private class ConnectorManagerTask implements Runnable {
        volatile boolean fin = false;
        T t;
        @Override
        public void run() {
            try {
                while (!fin) {
                    synchronized (connectors){
                        for (Connector<T> connector: connectors) {
                            if (!connector.isStopped()) {
                                while ((t = connector.getDispatcherReader().get()) != null){
                                    connector.getDispatcherWriter().put(t);
                                }
                            }
                        }
                    }
                    Thread.sleep(minPeriod);
                }
            } catch (InterruptedException e) {}
        }
        public void terminate() {
            fin = true;
        }
    }
}
