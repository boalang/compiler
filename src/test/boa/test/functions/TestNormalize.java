/*
 * Copyright 2018, Robert Dyer, Mohd Arafat, Che Shian Hung
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
 * @author cheshianhung
 */
@RunWith(Parameterized.class)
public class TestNormalize {
    @Parameters(name = "{index}][{0} = {1}")
    public static Collection<String[]> expressions() {
        return Arrays.asList(new String[][]{
                {"temp - $RECEIVER$.charAt(i) - '0' / 10 == ans", "$RECEIVER$.charAt(i) + 48 / 10 + ans - temp == 0"},

                {"$RECEIVER$.length() / 2 == $RECEIVER$.length() / 2.0", "0 == 0"},

                {"a != 0xFFFFFFFFFFFFFFFFL", "a != 0xFFFFFFFFFFFFFFFFL"},

                {"a > 1", "a > 1"},
                {"a > +1", "a > 1"},
                {"1 > a", "a < 1"},
                {"-a >= -1", "a <= 1"},
                {"a > b", "a - b > 0"},
                {"++a >= b", "++a - b >= 0"},
                {"a[1] == true", "a[1] == true"},
                {"a[1] != ++b", "++b - a[1] != 0"},
                {"a-- <= func()", "a-- - func() <= 0"},
                {"a + 1 > 0", "a > -1"},
                {"-a + 1 > 0", "a < 1"},
                {"-a + 1 > 3", "a < -2"},
                {"-a + 3 > 3", "a < 0"},
                {"a / 3 <= 3", "a <= 9"},
                {"-a + 1 > b", "a + b < 1"},
                {"0 > b + a", "a + b < 0"},
                {"rcv$.charAt(0) == 0xfeff", "rcv$.charAt(0) == 65279"},
                {"b[1] + 5 == --a", "--a - b[1] == 5"},
                {"-a + c <= b", "a + b - c >= 0"},
                {"-a + c <= -func()", "a - c - func() >= 0"},
                {"a++ - func() >= b", "a++ - b - func() >= 0"},
                {"--b - a[1] <= func()", "--b - a[1] - func() <= 0"},
                {"1 - b[1]*a++ > 3", "a++ * b[1] < -2"},
                {"b * c * a > 1", "a * b * c > 1"},
                {"c * b * -a > 1", "a * b * c < -1"},
                {"a * -b + c > 1", "a * b - c < -1"},
                {"3 > 5 + a * -b", "a * b > 2"},
                {"a * -b  +  c > func()", "a * b - c + func() < 0"},
                {"b * -func()  +  a > 3 ", "a - b * func() > 3"},
                {"3 * 10 / -2 > -a ", "a > 15"},
                {"3 * -10 / -2 > -a ", "a > -15"},

                {"a > 0", "a > 0"},
                {"a < 0", "a < 0"},
                {"a == 0", "a == 0"},
                {"a >= 0", "a >= 0"},
                {"a <= 0", "a <= 0"},
                {"0 < a", "a > 0"},
                {"0 > a", "a < 0"},
                {"0 == a", "a == 0"},
                {"0 <= a", "a >= 0"},
                {"0 >= a", "a <= 0"},

                {"a + 1 > 0", "a > -1"},
                {"a + 1 < 0", "a < -1"},
                {"a + 1 == 0", "a == -1"},
                {"a + 1 >= 0", "a >= -1"},
                {"a + 1 <= 0", "a <= -1"},
                {"a - 1 > 0", "a > 1"},
                {"a - 1 < 0", "a < 1"},
                {"a - 1 == 0", "a == 1"},
                {"a - 1 >= 0", "a >= 1"},
                {"a - 1 <= 0", "a <= 1"},
                {"a > 1", "a > 1"},
                {"a < 1", "a < 1"},
                {"a == 1", "a == 1"},
                {"a >= 1", "a >= 1"},
                {"a <= 1", "a <= 1"},
                {"a > -1", "a > -1"},
                {"a < -1", "a < -1"},
                {"a == -1", "a == -1"},
                {"a >= -1", "a >= -1"},
                {"a <= -1", "a <= -1"},

                {"-a + 1 > 0", "a < 1"},
                {"-a + 1 < 0", "a > 1"},
                {"-a + 1 == 0", "a == 1"},
                {"-a + 1 >= 0", "a <= 1"},
                {"-a + 1 <= 0", "a >= 1"},
                {"-a - 1 > 0", "a < -1"},
                {"-a - 1 < 0", "a > -1"},
                {"-a - 1 == 0", "a == -1"},
                {"-a - 1 >= 0", "a <= -1"},
                {"-a - 1 <= 0", "a >= -1"},
                {"-a > 1", "a < -1"},
                {"-a < 1", "a > -1"},
                {"-a == 1", "a == -1"},
                {"-a >= 1", "a <= -1"},
                {"-a <= 1", "a >= -1"},
                {"-a > -1", "a < 1"},
                {"-a < -1", "a > 1"},
                {"-a == -1", "a == 1"},
                {"-a >= -1", "a <= 1"},
                {"-a <= -1", "a >= 1"},

                {"0 < -a + 1", "a < 1"},
                {"0 > -a + 1", "a > 1"},
                {"0 == -a + 1", "a == 1"},
                {"0 <= -a + 1", "a <= 1"},
                {"0 >= -a + 1", "a >= 1"},
                {"0 < -a - 1", "a < -1"},
                {"0 > -a - 1", "a > -1"},
                {"0 == -a - 1", "a == -1"},
                {"0 <= -a - 1", "a <= -1"},
                {"0 >= -a - 1", "a >= -1"},
                {"1 < -a", "a < -1"},
                {"1 > -a", "a > -1"},
                {"1 == -a", "a == -1"},
                {"1 <= -a", "a <= -1"},
                {"1 >= -a", "a >= -1"},
                {"-1 < -a", "a < 1"},
                {"-1 > -a", "a > 1"},
                {"-1 == -a", "a == 1"},
                {"-1 <= -a", "a <= 1"},
                {"-1 >= -a", "a >= 1"},

                {"a + b + c < 0", "a + b + c < 0"},
                {"a + b < -c", "a + b + c < 0"},
                {"a + c < -b", "a + b + c < 0"},
                {"a < -b - c", "a + b + c < 0"},
                {"b + c < -a", "a + b + c < 0"},
                {"c < -a - b", "a + b + c < 0"},
                {"b < -a - c", "a + b + c < 0"},
                {"0 < -a - c - b", "a + b + c < 0"},

                {"c + 3 - b + 10 + a < c + c", "a - b - c < -13"},
                {"c + 3 - b + 10 + a < 2 * c", "a - b - c < -13"},
                {"c + 3 - b + 10 + a < c * 2", "a - b - c < -13"},
                {"c + 3 - b + 10 + a < c * c", "a - b + c - c * c < -13"},

                {"a / 1 > 2", "a > 2"},
                {"a / -1 > 2", "a < -2"},
                {"-a / 1 > 2", "a < -2"},
                {"a / 1 > -2", "a > -2"},
                {"a / 1 > -2", "a > -2"},

                {"2 * x / 3 < 2", "x < 3"},

                {"2 * a + 3 * b - a + 2 * b + 2 * a < 0", "a + 5 * b / 3 < 0"},

                {"a * b < a", "a - a * b > 0"},
                {"a * b - a < 0", "a - a * b > 0"},
                {"x * z * y / x < 2 * z", "y * z - 2 * z < 0"},
                {"a * b + b * a * c + z * a < a", "a - a * b - a * b * c - a * z > 0"},
                {"a * b + z * a < a", "a - a * b - a * z > 0"},
                {"a > a * b", "a - a * b > 0"},
                {"z * 2 > x * z * y / x", "y * z - 2 * z < 0"},
                {"a > a * b + b * a * c + z * a", "a - a * b - a * b * c - a * z > 0"},
                {"a > a * b + z * a", "a - a * b - a * z > 0"},

                {"x * y / x < 2", "y < 2"},
                {"x * y * x / y < 2", "x * x < 2"},
                {"x * y * (a + b) / (x * (a + b)) < 2", "y < 2"},

                {"2 * a + 3 * (d + a - 3 - 2 * (c + d + a - 2)) < 2 * (a + b + 3 + c)", "a + 2 * b / 3 + 8 * c / 3 + d > -1"},
                {"2 * a + 3 * (d + a - 3) < 2 * (a + b + 3 + c)", "a + d - 2 * b / 3 - 2 * c / 3 < 5"},
                {"2 * a + 3 * (d + a - 3) < 0", "a + 3 * d / 5 < 9 / 5"},
                {"2 * a + 3 * b - a + 2 * (b + a) < 0", "a + 5 * b / 3 < 0"},
                {"2 * (b + a) < 2", "a + b < 1"},

                {"1 / a > 2", "1 / a > 2"},
                {"-1 / a > 2", "1 / a < -2"},
                {"1 / -a > 2", "1 / a < -2"},
                {"2 < 1 / a", "1 / a > 2"},
                {"2 < -1 / a", "1 / a < -2"},
                {"2 < 1 / -a", "1 / a < -2"},
                {"a / b > -2", "a / b > -2"},
                {"a / -b + c > func()", "a / b - c + func() < 0"},
                {"a / -b + c > 1", "a / b - c < -1"},
                {"b / a > 5", "b / a > 5"},
                {"b / -func() * a > 3 ", "a * b / func() < -3"},

                {"a * 10 / -2 > 3", "a < -3 / 5"},
                {"a / -2 * 10 > 3", "a < -3 / 5"},
                {"a * 10 / -3 > 3", "a < -9 / 10"},
                {"3 / a / -2 * 1 > 3", "1 / a < -2"},
                {"a / -2 * 1 > 3", "a < -6"},
                {"3 / a > 3", "1 / a > 1"},
                {"a * 3 <= 3", "a <= 1"},
                {"a * 3 <= 3 + z", "a - z / 3 <= 1"},
                {"a * 3 / z <= 3 + z", "a / z - z / 3 <= 1"},
                {"a * 6 / z / 2 <= 3 + z", "a / z - z / 3 <= 1"},
                {"a / z / 2 <= 3 + z", "a / z - 2 * z <= 6"},
                {"-a + 3 * b - c >= 2 * a", "a + c / 3 - b <= 0"},
                {"-a + 3 * b * x - c >= 2 * a", "a + c / 3 - b * x <= 0"},
                {"2 * a < 2", "a < 1"},
                {"2 < 2 * a", "a > 1"},
                {"b * 2 * a < 2", "a * b < 1"},
                {"a / 2 < 3", "a < 6"},

                {"3 / b * 2 > -a ", "a + 6 / b > 0"},
                {"b / a > 5", "b / a > 5"},

                {"1 * a < 2", "a < 2"},
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
