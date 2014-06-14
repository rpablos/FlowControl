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

/**
 *
 * @author Ronald
 */
public class DispatcherWriterClassificator<T> implements DispatcherWriter<T> {
    Classificator<T> classificator;

    public DispatcherWriterClassificator(Classificator<T> classificator) {
        this.classificator = classificator;
    }
    
    @Override
    public boolean put(T t) {
        return classificator.getDispatcherWriter(t).put(t);
    }
    
}