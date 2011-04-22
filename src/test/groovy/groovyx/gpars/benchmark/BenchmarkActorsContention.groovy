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

package groovyx.gpars.benchmark

import groovyx.gpars.actor.DefaultActor
import groovyx.gpars.actor.ReactiveActor

import groovyx.gpars.group.DefaultPGroup
import groovyx.gpars.scheduler.FJPool
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import groovyx.gpars.actor.impl.RunnableBackedBlockingActor

final def t1 = System.currentTimeMillis()

final def numOfAttackers = 100000
final def attackerConcurrencyLevel = 7
final def attackGroup = new DefaultPGroup(new FJPool(attackerConcurrencyLevel))
final def attackSignal = new CountDownLatch(1)
final def retreatSignal = new CountDownLatch(numOfAttackers)

final def defendGroup = new DefaultPGroup(new FJPool(1))
final def defender = new StressHandler()
defender.parallelGroup = defendGroup
defender.start()

int i = 0
while (i < numOfAttackers) {
    final def attacker = new AttackActor(attackSignal, retreatSignal, i, defender)
    attacker.parallelGroup = attackGroup
    attacker.start()
    i += 1
}

attackSignal.countDown()
retreatSignal.await(1000, TimeUnit.SECONDS)

attackGroup.shutdown()
defendGroup.shutdown()
final def t2 = System.currentTimeMillis()
println(t2 - t1)

final class AttackActor extends DefaultActor {

    final def attackSignal
    final def retreatSignal
    final def weapon
    final def defender

    def AttackActor(final attackSignal, final retreatSignal, final weapon, final defender) {
        this.attackSignal = attackSignal;
        this.retreatSignal = retreatSignal;
        this.weapon = weapon;
        this.defender = defender;
    }

    void act() {
        attackSignal.await()
        defender.send(weapon)
        react {
            retreatSignal.countDown()
        }
    }
}

final class RunnableAttackActor extends RunnableBackedBlockingActor {

    final def attackSignal
    final def retreatSignal
    final def weapon
    final def defender

    def RunnableAttackActor(final attackSignal, final retreatSignal, final weapon, final defender) {
        super(null)
        this.attackSignal = attackSignal;
        this.retreatSignal = retreatSignal;
        this.weapon = weapon;
        this.defender = defender;
        setAction this
    }

    public void run() {
        attackSignal.await()
        defender.send weapon
        this.receive {
            retreatSignal.countDown()
        }
    }
}
final class StressHandler extends ReactiveActor {

    def StressHandler() {
        super({
            2 * it
        })
//        makeFair()
    }
}
