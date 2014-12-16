// GPars - Groovy Parallel Systems
//
// Copyright © 2014  The original author or authors
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

package groovyx.gpars.remote.netty.discovery

import com.google.common.net.InetAddresses
import groovyx.gpars.remote.RemotingContextWithUrls
import spock.lang.Specification
import spock.lang.Timeout

class DiscoveryServiceTest extends Specification {
    @Timeout(5)
    def "can use discovery mechanism"() {
        setup:
        def serverSocketAddress = new InetSocketAddress(InetAddresses.forString("192.168.1.123"), 1234)
        def remotingContext = { true } as RemotingContextWithUrls
        def discoveryServer = new DiscoveryServer(11345, serverSocketAddress, remotingContext)
        discoveryServer.start()
        def discoveryClient = new DiscoveryClient(11345)
        discoveryClient.start()


        when:
        def promise = discoveryClient.ask "test-actor"

        then:
        def receivedServerSocketAddress = promise.get()
        receivedServerSocketAddress.getPort() == 1234

        cleanup:
        [discoveryServer, discoveryClient]*.stop()
    }
}
