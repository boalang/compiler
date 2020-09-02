package boa.aggregators.ml;

import java.io.IOException;
import java.util.LinkedList;

import org.deeplearning4j.models.embeddings.learning.impl.elements.SkipGram;
import org.deeplearning4j.models.embeddings.loader.VectorsConfiguration;
import org.deeplearning4j.models.sequencevectors.SequenceVectors;
import org.deeplearning4j.models.sequencevectors.iterators.AbstractSequenceIterator;
import org.deeplearning4j.models.sequencevectors.transformers.impl.SentenceTransformer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.stopwords.StopWords;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import boa.aggregators.AggregatorSpec;
import boa.runtime.Tuple;
import weka.core.Utils;

@AggregatorSpec(name = "seq2vec", formalParameters = { "string" }, canCombine = true)
public class Sequence2VectorAggregator extends MLAggregator {
	LinkedList<String> train;

	public Sequence2VectorAggregator() {
		train = new LinkedList<String>();
	}

	public Sequence2VectorAggregator(final String s) {
		this();
		try {
			options = Utils.splitOptions(s);
			// enable multiple training mode
			for (int i = 0; i < options.length; i++)
				if (options[i].equals("-M")) {
					this.trainWithCombiner = true;
					break;
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void aggregate(String[] data, String metadata) throws IOException, InterruptedException {
		if (metadata != null && metadata.equals("single") && data.length == 1) {
			aggregate(data[0], null);
		} else {
			StringBuilder sb = new StringBuilder();
			for (String s : data)
				sb.append(s).append(' ');
			train.add(sb.toString());
		}
	}

	@Override
	public void aggregate(final Tuple data, final String metadata) throws IOException, InterruptedException {
	}

	@Override
	public void aggregate(String data, String metadata) throws IOException, InterruptedException {
		train.add(data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finish() throws IOException, InterruptedException {
		try {
			// First we build iterator
			SentenceIterator itr = new CollectionSentenceIterator(train);

			// seq2vec model builder
			SequenceVectors.Builder<VocabWord> svb = new SequenceVectors.Builder<VocabWord>(new VectorsConfiguration());

			// tokenization
			TokenizerFactory t = new DefaultTokenizerFactory();

			// update options
			updateOptions(svb, t);

			// sequence transformer
			SentenceTransformer transformer = new SentenceTransformer.Builder()
					.iterator(itr)
					.tokenizerFactory(t)
					.build();

			// sequence iterator
			AbstractSequenceIterator<VocabWord> seqItr = new AbstractSequenceIterator
					.Builder<VocabWord>(transformer)
					.build();

			// build a seq2vec model
			SequenceVectors<VocabWord> sv = svb
					.iterate(seqItr)
					.resetModel(true)
					.trainElementsRepresentation(true)
					.trainSequencesRepresentation(false)
					.elementsLearningAlgorithm(new SkipGram<VocabWord>())
					.build();

			String info = "";
			// train model
			sv.fit();
			info += "\n=== Model is trained ===\n";
			// save model
			saveModel(sv);
			info += "\n=== Model is Saved ===\n";
			info += "\n=== Model Configuration ===\n" + sv.getConfiguration().toString();
			collect(info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateOptions(SequenceVectors.Builder<VocabWord> sb, TokenizerFactory t) {
		if (options == null)
			return;
		for (int i = 0; i < options.length; i++) {
			String cur = options[i];
			if (cur.equals("-s"))
				trainingPerc = Integer.parseInt(options[++i]);
			else if (cur.equals("-f")) {
				sb.minWordFrequency(Integer.parseInt(options[++i]));
			} else if (cur.equals("-lr")) {
				sb.minLearningRate(Double.parseDouble(options[++i]));
			} else if (cur.equals("-ls")) {
				sb.layerSize(Integer.parseInt(options[++i]));
			} else if (cur.equals("-ws")) {
				sb.windowSize(Integer.parseInt(options[++i]));
			} else if (cur.equals("-i"))
				sb.iterations(Integer.parseInt(options[++i]));
			else if (cur.equals("-b"))
				sb.batchSize(Integer.parseInt(options[++i]));
			else if (cur.equals("-stop"))
				sb.stopWords(StopWords.getStopWords());
			else if (cur.equals("-tp"))
				t.setTokenPreProcessor(new CommonPreprocessor());
		}
	}
}