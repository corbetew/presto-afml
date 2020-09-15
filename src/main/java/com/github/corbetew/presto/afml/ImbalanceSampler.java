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

import io.prestosql.spi.block.BlockBuilder;
import io.prestosql.spi.function.AggregationFunction;
import io.prestosql.spi.function.CombineFunction;
import io.prestosql.spi.function.InputFunction;
import io.prestosql.spi.function.OutputFunction;
import io.prestosql.spi.function.SqlType;
import io.prestosql.spi.type.StandardTypes;

import static io.prestosql.spi.type.DoubleType.DOUBLE;
import static java.lang.Math.abs;

@AggregationFunction("imbalance_sampler")
public class ImbalanceSampler
{
    private ImbalanceSampler()
    {}

    @InputFunction
    public static void input(
            ImbalanceState state,
            @SqlType(StandardTypes.BIGINT) long tradeId,
            @SqlType(StandardTypes.DOUBLE) double price,
            @SqlType(StandardTypes.DOUBLE) double criterion,
            @SqlType(StandardTypes.DOUBLE) double initET,
            @SqlType(StandardTypes.DOUBLE) double initEb,
            @SqlType(StandardTypes.DOUBLE) double alpha)
    {
        if (!state.getInitialized()) {
            state.setBarStart(tradeId);
            state.setET(initET);
            state.setEb(initEb);
            state.setExpImbalance(initET * abs(initEb));
            state.setLastPrice(price);
            state.setInitialized(true);
        }
        else {
            state.setB(calculateB(price, state));
            state.setLastPrice(price);
            state.setImbalance(state.getImbalance() + state.getB() * criterion);
            state.setTicks(state.getTicks() + 1);
            // ewm of the Eb
            state.setEb(state.getEb() + alpha * (state.getB() * criterion - state.getEb()));
        }

        if (abs(state.getImbalance()) >= state.getExpImbalance()) {
            // ewm of expected ticks
            state.setET(state.getET() + alpha * (state.getTicks() - state.getET()));
            state.setImbalance(0.);
            state.setTicks(0);
            state.setExpImbalance(state.getET() * abs(state.getEb()));
            state.setBarStart(tradeId);
        }
    }

    @CombineFunction
    public static void combine(
            ImbalanceState state,
            ImbalanceState otherState)
    {
        // do nothing
        throw new UnsupportedOperationException("Imbalance Sampler should use the OVER clause, NOT GROUP BY");
    }

    @OutputFunction(StandardTypes.DOUBLE)
    public static void output(ImbalanceState state, BlockBuilder out)
    {
        long barStart = state.getBarStart();
        DOUBLE.writeDouble(out, barStart);
    }

    public static int calculateB(double price, ImbalanceState state)
    {
        double newB = (price - state.getLastPrice());

        if (newB != 0.) {
            return (int) (abs(newB) / newB);
        }
        else {
            return state.getB();
        }
    }
}
