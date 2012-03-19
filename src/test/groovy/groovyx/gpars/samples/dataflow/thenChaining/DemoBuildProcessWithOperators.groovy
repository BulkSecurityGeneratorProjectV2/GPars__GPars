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

package groovyx.gpars.samples.dataflow.thenChaining

import groovyx.gpars.dataflow.DataflowBroadcast
import groovyx.gpars.dataflow.DataflowQueue
import groovyx.gpars.dataflow.operator.PoisonPill
import static groovyx.gpars.dataflow.Dataflow.operator

/**
 * Illustrates the use of dataflow variables and tasks to orchestrate a build process
 *
 * @author Vaclav Pech
 */

//Mock-up definitions of build steps
def createABuildStep = {name -> {param -> println "Starting $name"; sleep 3000; println "Finished $name"; true}}
def createASlowArgBuildStep = {name -> {param -> println "Starting $name"; sleep 9000; println "Finished $name"; true}}
def createAThreeArgBuildStep = {name -> {a, b, c -> println "Starting $name"; sleep 3000; println "Finished $name"; true}}
def checkout = createASlowArgBuildStep 'Checkout Sources'
def compileSources = createABuildStep 'Compile Sources'
def generateAPIDoc = createABuildStep 'Generate API Doc'
def generateUserDocumentation = createABuildStep 'Generate User Documentation'
def packageProject = createAThreeArgBuildStep 'Package Sources'
def deploy = createABuildStep 'Deploy'

/* We need channels to wire active elements together */

def urls = new DataflowQueue()
def checkedOutProjects = new DataflowBroadcast()
def compiledProjects = new DataflowQueue()
def apiDocs = new DataflowQueue()
def userDocs = new DataflowQueue()
def packages = new DataflowQueue()
def done = new DataflowQueue()

/* Here's the composition of individual build steps into a process */

operator(inputs: [urls], outputs: [checkedOutProjects], maxForks: 3) {url ->
    bindAllOutputs checkout(url)
}

operator([checkedOutProjects.createReadChannel()], [compiledProjects]) {projectRoot ->
    bindOutput compileSources(projectRoot)
}

operator(checkedOutProjects.createReadChannel(), apiDocs) {projectRoot ->
    bindOutput generateAPIDoc(projectRoot)
}

operator(checkedOutProjects.createReadChannel(), userDocs) {projectRoot ->
    bindOutput generateUserDocumentation(projectRoot)
}

operator([compiledProjects, apiDocs, userDocs], [packages]) {classes, api, guide ->
    bindOutput packageProject(classes, api, guide)
}

def deployer = operator(packages, done) {packagedProject ->
    if (deploy(packagedProject) == 'success') bindOutput true
    else bindOutput false
}

/* Now we're setup and can wait for the build to finish */

println "Starting the build process. This line is quite likely to be printed first ..."

/* Feed in the requested source repositories. We might well be reading these from a socket instead. */

urls << 'git@github.com:vaclav/GPars.git branch:master'
urls << 'git@github.com:vaclav/GPars.git branch:secret_new_stuff'
urls << 'git@github.com:gpars/GPars.git branch:master'
urls << 'git@github.com:russel/GPars.git'

urls << PoisonPill.instance  //Request to shutdown
deployer.join()  //Wait for the last operator in the network to finish