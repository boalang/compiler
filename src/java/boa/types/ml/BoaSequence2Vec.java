/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer,
 *                 and Iowa State University of Science and Technology
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
package boa.types.ml;

import org.apache.hadoop.fs.FileStatus;
import org.deeplearning4j.models.sequencevectors.SequenceVectors;
import org.deeplearning4j.models.word2vec.VocabWord;

import boa.types.BoaType;

/**
 * A {@link BoaType} representing ML model of Word2Vec with attached types.
 * 
 * @author hyj
 */
public class BoaSequence2Vec extends BoaEnsemble {
	private SequenceVectors<VocabWord> seq2vec;

	/**
	 * Default BoaVote Constructor.
	 * 
	 */
	public BoaSequence2Vec() {
	}

	/**
	 * Construct a BoaVote.
	 * 
	 * @param t A {@link BoaType} containing the types attached with this model
	 *
	 */
	public BoaSequence2Vec(BoaType t) {
		this.t = t;
	}

	/**
	 * Construct a BoaVote.
	 * 
	 * @param w2v A {@link SequenceVectors} containing ML model
	 * 
	 * @param o   A {@link Object} containing type object
	 *
	 */
	public BoaSequence2Vec(SequenceVectors<VocabWord> seq2vec, Object o) {
		this.seq2vec = seq2vec;
		this.o = o;
	}

	public BoaSequence2Vec(FileStatus[] files, Object o) {
		this.files = files;
		this.o = o;
	}

	@Override
	public Kind getKind() {
		return Kind.VECTOR;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		if (!super.assigns(that))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		if (!super.assigns(that))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.ml.BoaSequence2Vec";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "boa.types.ml.BoaSequence2Vec" + "/" + this.t;
	}

	public SequenceVectors<VocabWord> getSeq2Vec() {
		return seq2vec;
	}
}