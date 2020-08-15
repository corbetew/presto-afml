/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.corbetew.presto.afml;
import io.prestosql.spi.function.AccumulatorState;

public interface ImbalanceState
        extends AccumulatorState
{
    double getEb();

    void setEb(double value);

    double getET();

    void setET(double value);

    double getLastPrice();

    void setLastPrice(double value);

    double getImbalance();

    void setImbalance(double value);

    double getExpImbalance();

    void setExpImbalance(double value);

    long getTicks();

    void setTicks(long value);

    int getB();

    void setB(int value);

    boolean getInitialized();

    void setInitialized(boolean value);

    long getBarStart();

    void setBarStart(long value);
}
