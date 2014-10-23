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

import flowcontrol.connector.Connector;
import flowcontrol.connector.ConnectorManager;
import flowcontrol.dispatchers.DefaultDispatcher;
import flowcontrol.dispatchers.Dispatcher;
import flowcontrol.dispatchers.GaussianDispatcher;
import flowcontrol.events.DispatcherLossListener;
import java.util.Random;

/**
 *
 * @author Ronald
 */
public class ConnectorExample {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException  {

        final Dispatcher<String> dispatcher = new DefaultDispatcher<>(1);
        final Dispatcher<String> dispatcher2 = new DefaultDispatcher<>(.5);
        ConnectorManager<String> cm = new ConnectorManager<>();
        Connector<String> connector = cm.createConnector(dispatcher, dispatcher2);
        dispatcher.addDispatcherLossListener(new DispatcherLossListener<String>() {

            @Override
            public void onLoss(Dispatcher<String> dw, String t) {
                System.out.println("Pérdida en el dispatcher 1: "+t);
            }
        });
        dispatcher2.addDispatcherLossListener(new DispatcherLossListener<String>() {
            @Override
            public void onLoss(Dispatcher<String> dw, String t) {
                System.out.println("Pérdida en el dispatcher 2: "+t);
            }
        });
        System.out.println("Esquema: Generador-->dispatcher1-->conector-->dispatcher2-->Consumidor");
        System.out.println("Dispatcher1: "+dispatcher.getOutputRate()+ " objetos/sec. Cola FIFO de "+dispatcher.getQueue().getCapacity()+" elementos.");
        System.out.println("Dispatcher2: "+dispatcher2.getOutputRate()+ " objetos/sec. Cola FIFO de "+dispatcher2.getQueue().getCapacity()+" elementos.");
        connector.start();
        System.out.println("Comienzo del conector entre dispatcher1 y dispatcher2");
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Random random = new Random();
                    Thread.sleep(3000);
                    long infimo=250,rango=1000;
                    System.out.println("Comienzo del generador. Generación aleatoria de objetos entre "+infimo+" ms y "+(rango+infimo)+" ms");
                    for (int i = 0; i < 1000; i++) {

                        dispatcher.put(""+i);

                        Thread.sleep((long) (random.nextDouble()*rango+infimo));
                    }
                } catch (InterruptedException ex){}
            
            }
        }).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println("Comienzo del consumidor.");

                try {
                for (int i = 0; i < 1000; i++) {
                    
                    System.out.print(dispatcher2.getBlocking(5000));
                    System.out.println(": "+System.currentTimeMillis());
                }
                } catch(InterruptedException ex) {}
            }
        }).start();
//        Thread.sleep(30000);
//        System.out.println("Parando conector durante 10 seg");
//        connector.stop();
//        Thread.sleep(10000);
//        connector.start();
    }
    
}
