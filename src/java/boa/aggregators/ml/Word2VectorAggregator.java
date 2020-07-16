package boa.aggregators.ml;

import java.io.IOException;
import java.util.LinkedList;

import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.Word2Vec.Builder;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.stopwords.StopWords;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import boa.aggregators.AggregatorSpec;
import boa.runtime.Tuple;
import weka.core.Utils;

@AggregatorSpec(name = "word2vec", formalParameters = { "string" })
public class Word2VectorAggregator extends MLAggregator {
	LinkedList<String> train;

	public Word2VectorAggregator() {
		train = new LinkedList<String>();
	}

	public Word2VectorAggregator(final String s) {
		this();
		try {
			options = Utils.splitOptions(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void aggregate(String[] data, String metadata) throws IOException, InterruptedException {
		if (metadata != null && metadata.equals("single") && data.length == 1) {
			aggregate(data[0], null);
		} else {
			for (String s : data)
				train.add(s);
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
			// load train dataset
			SentenceIterator iter = new CollectionSentenceIterator(train);
			iter.setPreProcessor(new SentencePreProcessor() {
				private static final long serialVersionUID = 1L;

				@Override
				public String preProcess(String sentence) {
					return sentence.toLowerCase();
				}
			});
			// word2vec model builder
			Word2Vec.Builder wb = new Word2Vec.Builder();
			// tokenization
			TokenizerFactory t = new DefaultTokenizerFactory();
			updateOptions(wb, t);
			t.setTokenPreProcessor(new CommonPreprocessor());
			// build word2vec model
			Word2Vec vec = wb.iterate(iter).tokenizerFactory(t).build();
			// train model
			vec.fit();
			// save model
			saveModel(vec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateOptions(Builder wb, TokenizerFactory t) {
		if (options == null)
			return;
		for (int i = 0; i < options.length; i++) {
			String cur = options[i];
			if (cur.equals("-s"))
				trainingPerc = Integer.parseInt(options[++i]);
			else if (cur.equals("-f"))
				wb.minWordFrequency(Integer.parseInt(options[++i]));
			else if (cur.equals("-lr"))
				wb.minLearningRate(Double.parseDouble(options[++i]));
			else if (cur.equals("-ls"))
				wb.layerSize(Integer.parseInt(options[++i]));
			else if (cur.equals("-ws"))
				wb.windowSize(Integer.parseInt(options[++i]));
			else if (cur.equals("-stop"))
				wb.stopWords(StopWords.getStopWords());
			else if (cur.equals("-tp"))
				t.setTokenPreProcessor(new CommonPreprocessor());
		}
	}

}
