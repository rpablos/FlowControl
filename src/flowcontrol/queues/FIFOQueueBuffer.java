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

/**
 *
 * @author Ronald
 */
public class FIFOQueueBuffer<T> implements Queue<T>{
    Object[] buffer;
    int head = 0,tail = -1;
    int size = 0;

    public FIFOQueueBuffer(int size) {
        buffer = new Object[size];
    }
    
    public int getCapacity() {
        return buffer.length;
    }
    public int getSize() {
        return size;
    }
    public boolean push(T t) {
        if (size < buffer.length) {
            tail = (tail+1) % buffer.length;
            buffer[tail] = t;
            size++;
            return true;
        }
        return false;
    }
    public T pop() {
        if (size > 0) {
            T result = (T) buffer [head];
            size--;
            buffer[head] = null; //libera
            head = (head+1) % buffer.length;
            return result;
        }
        return null;
    }
    public T peekHead() {
        if (size > 0) {
           return (T) buffer[head];
        }
        return null;
    }
    public T peekTail() {
        if (size > 0) {
           return (T) buffer[tail];
        }
        return null;
    }
}
