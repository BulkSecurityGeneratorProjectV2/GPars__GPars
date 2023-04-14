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

package groovyx.gpars.dataflow.stream

import groovyx.gpars.dataflow.DataflowQueue
import groovyx.gpars.dataflow.Select
import static groovyx.gpars.dataflow.Dataflow.operator
import static groovyx.gpars.dataflow.Dataflow.select
import static groovyx.gpars.dataflow.Dataflow.selector
import static groovyx.gpars.dataflow.Dataflow.task

class DataflowStreamOperatorTest extends groovy.test.GroovyTestCase {
    public void testOperatorCommunication() {
        final DataflowStream a = new DataflowStream()
        final DataflowStream b = new DataflowStream()
        def aw = new DataflowStreamWriteAdapter(a)
        def bw = new DataflowStreamWriteAdapter(b)
        def ar = new DataflowStreamReadAdapter(a)
        def br = new DataflowStreamReadAdapter(b)

        def result = new DataflowQueue()

        def op1 = operator(ar, bw) {
            bindOutput it
        }
        def op2 = selector([br], [result]) {
            result << it
        }

        aw << 1
        aw << 2
        aw << 3
        assert ([1, 2, 3] == [result.val, result.val, result.val])
        op1.terminate()
        op2.terminate()
        op1.join()
        op2.join()
    }

    public void testSelect() {
        final DataflowStream a = new DataflowStream()
        final DataflowStream b = new DataflowStream()
        def aw = new DataflowStreamWriteAdapter(a)
        def bw = new DataflowStreamWriteAdapter(b)
        def ar = new DataflowStreamReadAdapter(a)
        def br = new DataflowStreamReadAdapter(b)

        final Select<?> select = select(ar, br)
        task {
            aw << 1
            aw << 2
            aw << 3
        }
        assert 1 == select().value
        assert 2 == select().value
        assert 3 == select().value
        task {
            bw << 4
            aw << 5
            bw << 6
        }
        def result = (1..3).collect {select()}.sort {it.value}
        assert result*.value == [4, 5, 6]
        assert result*.index == [1, 0, 1]
    }
}
