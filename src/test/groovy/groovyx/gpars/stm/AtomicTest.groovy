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

package groovyx.gpars.stm

import org.multiverse.api.references.IntRef
import static org.multiverse.api.StmUtils.newIntRef

/**
 * @author Vaclav Pech
 */
class AtomicTest extends GroovyTestCase {
    public void testAtomicBlock() {
        final Account account = new Account()
        account.transfer(10)
        def t1 = Thread.start {
            account.transfer(100)
        }
        def t2 = Thread.start {
            account.transfer(20)
        }

        [t1, t2]*.join()
        assert 260 == account.currentAmount
    }

    public void testAtomicBlockException() {
        final Account account = new Account()
        account.transfer(10)
        shouldFail(IllegalArgumentException) {
            account.transfer(-1)
        }
        account.transfer(10)

        def t1 = Thread.start {
            account.transfer(100)
            shouldFail(IllegalArgumentException) {
                account.transfer(-1)
            }
            account.transfer(100)
        }
        def t2 = Thread.start {
            account.transfer(20)
        }

        [t1, t2]*.join()
        assert 480 == account.currentAmount
    }
}

public class Account {
    private final IntRef amount = newIntRef(0);

    public void transfer(final int a) {
        GParsStm.atomic {
            amount.increment(a);
            sleep 300
            if (a == -1) throw new IllegalArgumentException('test')
            amount.increment(a);
        }
    }

    public int getCurrentAmount() {
        GParsStm.atomicWithInt {
            amount.get();
        }
    }
}
