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

package groovyx.gpars

import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Vaclav Pech
 * Date: Aug 25, 2010
 */
public class SpeculationTest extends GroovyTestCase {

    final neverReachedFlag = new AtomicBoolean(false)

    final alternatives = [
            {
                Thread.sleep 30000
                neverReachedFlag.set(true)
                10
            },

            {
                Thread.sleep 1000
                20
            },

            {
                Thread.sleep 10
                throw new RuntimeException('test')
            },

            {
                Thread.sleep 1000
                40
            },

            {
                Thread.sleep 25000
                50
            }]
    final failedAlternatives = [
            {
                Thread.sleep 300
                throw new RuntimeException('test')
            },

            {
                Thread.sleep 100
                throw new RuntimeException('test')
            },

            {
                Thread.sleep 10
                throw new RuntimeException('test')
            },

            {
                Thread.sleep 1000
                throw new RuntimeException('test')
            },

            {
                Thread.sleep 100
                throw new RuntimeException('test')
            }]

    public void testGParsPoolSpeculation() {
        GParsPool.withPool(5) {
            assert GParsPool.speculate(alternatives) in [20, 40]
        }
        GParsPool.withPool(10) {
            assert GParsPool.speculate(alternatives) in [20, 40]
        }
        assert !neverReachedFlag.get()
    }

    public void testGParsExecutorsPoolSpeculation() {
        GParsExecutorsPool.withPool(5) {
            assert GParsExecutorsPool.speculate(alternatives) in [20, 40]
        }
        GParsExecutorsPool.withPool(10) {
            assert GParsExecutorsPool.speculate(alternatives) in [20, 40]
        }
        assert !neverReachedFlag.get()
    }

    public void testGParsPoolSpeculationWithAllBranchesFailed() {
        GParsPool.withPool(5) {
            shouldFail(IllegalStateException) {
                GParsPool.speculate(failedAlternatives)
            }
        }
        assert !neverReachedFlag.get()
    }

    public void testGParsExecutorsPoolSpeculationWithAllBranchesFailed() {
        GParsExecutorsPool.withPool(5) {
            shouldFail(IllegalStateException) {
                GParsExecutorsPool.speculate(failedAlternatives)
            }
        }
        assert !neverReachedFlag.get()
    }
}
