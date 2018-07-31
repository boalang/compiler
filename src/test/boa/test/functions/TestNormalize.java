/*
 * Copyright 2018, Robert Dyer, Mohd Arafat
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
 * @author rdyer
 */
@RunWith(Parameterized.class)
public class TestNormalize {
    @Parameters(name = "{index}][{0} = {1}")
    public static Collection<String[]> expressions() {
        return Arrays.asList(new String[][]{
                // size = 1
                {"a * 10 / -2 > 3 ", "5 * a < -3"},
                {"1", "1"},
                {"a", "a"},
                {"-a", "-a"},
                {"++a", "++a"},
                {"a[1]", "a[1]"},
                {"a.func()", "a.func()"},

                // size = 2
                {"b.func() + a", "a + b.func()"},
                {"b.func() - a", "-a + b.func()"},
                {"-(a-b)", "-a + b"},
                {"(-a-b)", "-a - b"},
                {"a + 1", "a + 1"},
                {"a > 1", "a > 1"},
                {"a > +1", "a > 1"},
                {"1 > a", "a < 1"},
                {"-a >= -1", "a <= 1"},
                {"a > b", "a - b > 0"},
                {"++a >= b", "++a - b >= 0"},
                {"a[1] == true", "a[1] == true"},
                {"a[1] != ++b", "++b - a[1] != 0"},
                {"a-- <= func()", "a-- - func() <= 0"},

                // size = 3
                {"b.func() - a - 3", "-a + b.func() - 3"},
                {"a + 1 > 0", "a > -1"},
                {"-a + 1 > 0", "a < 1"},
                {"-a + 1 > 3", "a < -2"},
                {"-a + 3 > 3", "a < 0"},
                {"a * 3 <= 3", "3 * a <= 3"},
                {"a / 3 <= 3", "1 * a / 3 <= 3"}, // FIXME
                {"-a + 1 > b", "a + b < 1"},
                {"0 > b + a", "a + b < 0"},
                {"b / a > 5", "1 * b / a > 5"}, // FIXME
                {"a / b / c", "a / (b * c)"},
                {"2 / (b + a)", "2 / (a + b)"},
                {"rcv$.charAt(0) == 0xfeff", "rcv$.charAt(0) == 65279"},
                {"2 * (b + a)", "2 * (a + b)"},
                {"b[1] + 5 == --a", "--a - b[1] == 5"},
                {"-a + c <= b", "a + b - c >= 0"},
                {"-a + c <= -func()", "a - c - func() >= 0"},
                {"a++ - func() >= b", "a++ - b - func() >= 0"},
                {"--b - a[1] <= func()", "--b - a[1] - func() <= 0"},

                // size = 4
                {"++c + b.func() - a-- - 3", "++c - a-- + b.func() - 3"},
                {"1 - b[1]*a++ > 3", "a++ * b[1] < -2"},
                {"b * c * a > 1", "a * b * c > 1"},
                {"c * b * -a > 1", "a * b * c < -1"},
                {"a * -b + c > 1", "a * b - c < -1"},
                {"a / -b + c > 1", "1 / b * a - c < -1"}, // FIXME
                {"3 > 5 + a * -b", "a * b > 2"},
                {"c * -2 / -(b - a)", "-2 * c / (a - b)"},
                {"a * -b  +  c > func()", "a * b - c + func() < 0"},
                {"a / -b  +  c > func()", "1 / b * a - c + func() < 0"}, // FIXME
                {"b * -func()  +  a > 3 ", "a - b * func() > 3"},
                {"b / -func() * a > 3 ", "1 / func() * a * b < -3"}, // FIXME
                {"a * 10 / -2 > 3 ", "5 * a < -3"},
                {"3 * 10 / -2 > -a ", "a > 15"},
                {"3 * -10 / -2 > -a ", "a > -15"},
                {"3 / b * 2 > -a ", "1 * 6 / b + a > 0"}, // FIXME

                // size = 5
                {"a * c / 2 * d * b", "a * b * c * d / 2"},
                {"a*2/(c*(b+c*(x+y/2)))", "2 * a / ((b + c * (x + y / 2)) * c)"},
                {"c*(d-b)/((a-b)*3)", "(-b + d) * c / (3 * (a - b))"},
                //{"-(b-a)*-3*(1+2)*(x[0]-func()*3)/((b+a)*-2*(x-y-3)+2+b*a)", " "},
                //{"-(b-a)*3*(1+2)*(-x[0]+func()*3)/((b+a)*-2*(x-y-3)+2+b*a)", " "}
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
