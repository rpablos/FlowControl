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

import flowcontrol.dispatchers.DefaultDispatcher;
import flowcontrol.dispatchers.Dispatcher;
import flowcontrol.connector.Connector;
import flowcontrol.connector.ConnectorManager;
import flowcontrol.events.DispatcherLossListener;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Ronald
 */
public class ClassificatorExample {
    static Dispatcher<Integer> goldDispatcher = new DefaultDispatcher<Integer>(1);
    static Dispatcher<Integer> silverDispacher = new DefaultDispatcher<Integer>(1); 
    static Dispatcher<Integer> bronzeDispatcher = new DefaultDispatcher<Integer>(1);
  
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException  {
        final DispatcherWriterClassificator<Integer> dwc = new DispatcherWriterClassificator<>(new MyClassificator());
        final DispatcherReaderClassificator<Integer> drc = 
                new DispatcherReaderClassificator<Integer>(1, new DispatcherReader[]{goldDispatcher,silverDispacher,bronzeDispatcher});
        DispatcherLossListener<Integer> mylosslistener = new myLossListener();
        goldDispatcher.addDispatcherLossListener(mylosslistener);
        bronzeDispatcher.addDispatcherLossListener(mylosslistener);
        silverDispacher.addDispatcherLossListener(mylosslistener);
        
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Random random = new Random();
                    Thread.sleep(3000);
                    System.out.println("Comienzo del generador");
                    for (int i = 0; i < 1000; i++) {
                        //System.out.println("push "+i);
                        dwc.put(random.nextInt(1000));
                           
                        Thread.sleep((long) (random.nextDouble()*1000+250));
//                            Thread.sleep(500);
                    }
                } catch (InterruptedException ex){}
            
            }
        }).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println("comienzo del consumidor");

                try {
                for (int i = 0; i < 1000; i++) {
                    System.out.println(drc.getBlocking(5000)+" :"+System.currentTimeMillis());
                }
                } catch(InterruptedException ex) {}
            }
        }).start();
    }
    
    
    private static class MyClassificator implements Classificator<Integer>{

        public MyClassificator() {
        }

        @Override
        public DispatcherWriter<Integer> getDispatcherWriter(Integer t) {
            if (t.intValue() < 500)
                return goldDispatcher;
            if (t.intValue() < 750)
                return silverDispacher;
            return bronzeDispatcher;
                
        }
    }

    private static class myLossListener implements DispatcherLossListener<Integer> {
        int goldLosses, silverLosses,bronzeLosses;
        public myLossListener() {
        }

        @Override
        public void onLoss(Dispatcher<Integer> dw, Integer t) {
            System.out.print("pérdida en dispatcher \"");
            if (dw== goldDispatcher)
                goldLosses++;
            else if (dw == silverDispacher)
                silverLosses++;
            else
                bronzeLosses++;
            System.out.print((dw==goldDispatcher)?"gold":((dw==silverDispacher)?"silver":"bronze"));
            System.out.print("\": "+t);
            System.out.println("\t Estadística: "+goldLosses+" "+silverLosses+" "+bronzeLosses);
        }
    }
    
}
