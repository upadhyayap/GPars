//  GParallelizer
//
//  Copyright � 2008-9  The original author or authors
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.gparallelizer.dataflow;

import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Set;

/**
 * Data flow expression which invokes method of object
 *
 * @author Alex Tkachman
 */
public class DataFlowInvocationExpression extends DataFlowComplexExpression{
    private Object receiver;
    private final String methodName;

    public DataFlowInvocationExpression(Object receiver, String methodName, Object [] args) {
        super(args);
        this.receiver = receiver;
        this.methodName = methodName;
        init ();
    }

    protected Object evaluate() {
        if (receiver instanceof DataFlowExpression)
            receiver = ((DataFlowExpression)receiver).value;

        for (int i = 0; i != args.length; ++i)
            if (args[i] instanceof DataFlowExpression)
                args[i] = ((DataFlowExpression) args[i]).value;

        return InvokerHelper.invokeMethod(receiver, methodName, args);
    }

    protected void collectDataFlowExpressions(Set collection) {
        if (receiver instanceof DataFlowExpression)
            collection.add(receiver);

        super.collectDataFlowExpressions(collection);
    }
}
