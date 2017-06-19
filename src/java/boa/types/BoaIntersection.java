/*
 * Copyright 2017, Robert Dyer,
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
package boa.types;

import java.util.List;
import java.util.ArrayList;

import boa.compiler.ast.Identifier;

/**
 * Base class for a intersection of several tuple types.
 *
 * @author rdyer
 */
public class BoaIntersection extends BoaTuple {
    private List<BoaTuple> types = new ArrayList<BoaTuple>();
    private BoaTuple t;

    /**
     * Construct a new BoaIntersection.
     *
     * @param types
     * @param t
     */
    public BoaIntersection(final List<BoaTuple> types, final BoaType t) {
        this.types = types;
        this.t = (BoaTuple)t;
    }

    /** {@inheritDoc} */
    @Override
	public boolean hasMember(final String member) {
        if (!t.hasMember(member))
            return false;
        final BoaType type = getMember(member);

        for (final BoaTuple tup : types) {
            if (!tup.hasMember(member))
                return false;
            if (!type.assigns(tup.getMember(member)))
                return false;
        }

		return true;
	}

    /** {@inheritDoc} */
    @Override
	public BoaType getMember(final int index) {
		return t.getMember(index);
	}

    /** {@inheritDoc} */
    @Override
	public BoaType getMember(final String member) {
		return t.getMember(member);
	}

    /** {@inheritDoc} */
    @Override
	public int getMemberIndex(final String member) {
		return t.getMemberIndex(member);
	}

    /** {@inheritDoc} */
    @Override
	public String getMemberName(final String member) {
		return t.getMemberName(member);
	}

    /** {@inheritDoc} */
    @Override
	public List<BoaType> getTypes() {
		return t.getTypes();
	}

    /** {@inheritDoc} */
    @Override
    public String toString() {
        String s = null;
        for (final BoaType t : types) {
            if (s != null)
                s += ", ";
            else
                s = "";
            s += t;
        }
        return s;
    }
}
