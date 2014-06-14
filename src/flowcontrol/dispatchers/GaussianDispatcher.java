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

import flowcontrol.queues.Queue;
import java.util.Random;

/**
 *
 * @author rpablos
 */
public class GaussianDispatcher<T> extends DefaultDispatcher<T> {
    Random random = new Random();
    double factor;

    public GaussianDispatcher(double outputRate) {
        this(0.5,outputRate);
    }
    
    public GaussianDispatcher(double factorStdDev,double outputRate) {
        super(outputRate);
        factor = factorStdDev;
    }

    public GaussianDispatcher(double factorStdDev,double outputRate, int bufferLength) {
        super(outputRate, bufferLength);
        factor = factorStdDev;
    }
    public GaussianDispatcher(double factorStdDev,double outputRate, Queue queue) {
        super(outputRate,queue);
        factor = factorStdDev;
    }

    @Override
    protected long TimeToWait() {
        return Math.max(0L, (long) (period*2-movingAverage.getAverage())+(long)(factor*random.nextGaussian()*period));
    }
    
}
