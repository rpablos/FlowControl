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

package flowcontrol.queues;

import flowcontrol.util.MovingAverage;
import java.util.Random;

/**
 *
 * @author Ronald
 */
public class RED<T> extends FIFOQueueBuffer<T>{
    int minThreshold, maxThreshold;
    double discardProbability;
    MovingAverage movingAverage;
    Random random = new Random();
    
    public RED(int size, int minThreshold, int maxThreshold, double discardProbability) {
        super(size);
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
        this.discardProbability = discardProbability;
        movingAverage = new MovingAverage();
    }

    @Override
    public boolean push(T t) {
        movingAverage.pushValue(getSize());
        int average = (int) movingAverage.getAverage();
        if (average > maxThreshold)
            return false;
        if ((average > minThreshold) && (random.nextDouble() <= discardProbability))
            return false;
        return super.push(t); 
    }
    
}
