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

package flowcontrol.util;

import java.util.Arrays;

/**
 *
 * @author rpablos
 */
public class MovingAverage {
    long[] values;
    int pos = 0;
    public MovingAverage() {
        this(0);
    }
    
    
    public MovingAverage(long initialValue) {
        this(8,initialValue);
    }

    
    public MovingAverage(int size, long initialValue) {
        if (size < 1)
            throw new IllegalArgumentException("Size must be greater than zero");
        values = new long[size];
        Arrays.fill(values,initialValue);
    }
    
    public void pushValue(long value) {
        values[pos] = value;
        pos = (pos+1) % values.length;
    }
    public long getAverage() {
        long result = 0;
        for (int i = 0; i < values.length; i++) {
            result = (int) (result*((i)/(i+1.0))+values[i]/(i+1.0));
        }
        return result;
    }
}
