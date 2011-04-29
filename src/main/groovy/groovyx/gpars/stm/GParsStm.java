// GPars - Groovy Parallel Systems
//
// Copyright © 2008-11  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.stm;

import groovy.lang.Closure;
import org.multiverse.api.StmUtils;

/**
 * @author Vaclav Pech
 */
public abstract class GParsStm {
    static final String THE_CODE_FOR_AN_ATOMIC_BLOCK_MUST_NOT_BE_NULL = "The code for an atomic block must not be null.";

    public static <T> T atomic(final Closure code) {
        return StmUtils.execute(new AtomicBlock<T>(code));
    }

    public static int atomicWithInt(final Closure code) {
        return StmUtils.execute(new AtomicIntBlock(code));
    }

    public static long atomicWithLong(final Closure code) {
        return StmUtils.execute(new AtomicLongBlock(code));
    }

    public static boolean atomicWithBoolean(final Closure code) {
        return StmUtils.execute(new AtomicBooleanBlock(code));
    }

    public static double atomicWithDouble(final Closure code) {
        return StmUtils.execute(new AtomicDoubleBlock(code));
    }

    public static void atomicWithVoid(final Closure code) {
        StmUtils.execute(new AtomicVoidBlock(code));
    }
}
