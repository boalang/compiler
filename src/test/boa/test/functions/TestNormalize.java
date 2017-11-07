/*
 * Copyright 2017, Robert Dyer, Mohd Arafat
 *                 and Bowling Green State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.test.functions;

import static boa.functions.BoaAstIntrinsics.prettyprint;
import static org.junit.Assert.assertEquals;
import static boa.functions.BoaAstIntrinsics.parseexpression;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import boa.functions.BoaNormalFormIntrinsics;
import boa.types.Ast.Expression;

/**
 * Test expression normalization
 *
 * @author marafat
 */
@RunWith(Parameterized.class)
public class TestNormalize {
    @Parameters
    public static Collection<String[]> expressions() {
        return Arrays.asList(new String[][]{
                //size = 1
                {"1", "1"},
                {"arg0", "arg0"},
                {"-arg0", "-arg0"},
                {"arg0.length()", "arg0.length()"},
                //size = 2
                {"arg1.length() + arg0", "arg0 + arg1.length()"},
                {"arg1.length() - arg0", "-arg0 + arg1.length()"},
                {"arg0 + 1", "arg0 + 1"},
                {"arg0 > 1", "arg0 > 1"},
                {"arg0 > +1", "arg0 > 1"},
                {"1 > arg0", "arg0 < 1"},
                {"-arg0 >= -1", "arg0 <= 1"},
                {"arg0 > arg1", "arg0 - arg1 > 0"},
                //size = 3
                {"arg1.length() - arg0 - 3", "-arg0 + arg1.length() - 3"},
                {"arg0 + 1 > 0", "arg0 > -1"},
                {"-arg0 + 1 > 0", "arg0 < 1"},
                {"-arg0 + 1 > 3", "arg0 < -2"},
                {"-arg0 + 3 > 3", "arg0 < 0"},
                {"arg0 * 3 <= 3", "3 * arg0 <= 3"},
                {"arg0 / 3 <= 3", "arg0 / 3 <= 3"},
                {"-arg0 + 1 > arg1", "arg0 + arg1 < 1"},
                {"arg1 * arg0 > 5", "arg1 * arg0 > 5"},
                {"arg1 / arg0 > 5", "arg1 / arg0 > 5"},  
                {"-arg0 + arg2 <= arg1", "arg0 + arg1 - arg2 >= 0"},
                {"-arg0 + arg2 <= -length()", "arg0 - arg2 - length() >= 0"},

        });
    }

    private Expression e;
    private String normalized;

    public TestNormalize(final String e, final String normalized) {
        this.e = parseexpression(e);
        this.normalized = normalized;
    }

    @Test()
    public void testNormalize() throws Exception {
        assertEquals(normalized, prettyprint(BoaNormalFormIntrinsics.normalize(e)));
    }
}