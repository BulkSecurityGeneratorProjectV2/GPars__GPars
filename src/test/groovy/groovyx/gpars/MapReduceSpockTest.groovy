// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10  The original author or authors
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

package groovyx.gpars

import spock.lang.Specification
import spock.lang.Unroll

import static groovyx.gpars.GParsPool.withPool

class MapReduceSpockTest extends Specification {
    @Unroll
    def "min of #nums is #minValue"() {
        expect:
        withPool() {
            nums.parallel.reduce { a, b -> Math.min(a, b) } == minValue
        }
        where:
        nums             | minValue
        [1, 2, 3, 4, 5] || 1
        [4, 2, 3, 1, 5] || 1
        [4, 5, 3, 2, 1] || 1
        [2, 2, 3, 4, 5] || 2
    }
}
