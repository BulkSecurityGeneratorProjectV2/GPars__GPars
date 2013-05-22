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

package groovyx.gpars.samples.stm

import groovyx.gpars.stm.GParsStm
import org.multiverse.api.AtomicBlock
import org.multiverse.api.PropagationLevel
import org.multiverse.api.references.IntRef
import static org.multiverse.api.StmUtils.newIntRef

public class CustomAccount {
    private final IntRef amount = newIntRef(0);

    private static AtomicBlock writeBlock = GParsStm.createTxnExecutor(familyName: 'Write', PropagationLevel: PropagationLevel.Requires)
    private static AtomicBlock readBlock = GParsStm.createTxnExecutor(readonly: true, PropagationLevel: PropagationLevel.RequiresNew, familyName: 'Read')

    public void transfer(final int a) {
        GParsStm.atomic(writeBlock) {
            amount.increment(a);
        }
    }

    public int getCurrentAmount() {
        GParsStm.atomicWithInt(readBlock) {
            amount.get();
        }
    }
}

final CustomAccount account = new CustomAccount()
account.transfer(10)
def t1 = Thread.start {
    account.transfer(2)
    account.transfer(3)
}
def t2 = Thread.start {
    account.transfer(20)
    account.transfer(30)
}

[t1, t2]*.join()
println account.currentAmount


