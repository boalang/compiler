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
		for (String s : data)
			train.add(s);
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

			// tokenization and preprocess
			TokenizerFactory t = new DefaultTokenizerFactory();
			t.setTokenPreProcessor(new CommonPreprocessor());

			// train model
			Word2Vec.Builder wb = new Word2Vec.Builder();
			updateOptions(wb);
			Word2Vec vec = wb.iterate(iter).tokenizerFactory(t).build();
			vec.fit();

			// save model
			saveModel(vec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateOptions(Builder wb) {
		for (int i = 0; i < options.length; i++) {
			String cur = options[i];
			if (cur.equals("-s"))
				trainingPerc = Integer.parseInt(options[++i]);
			else if (cur.equals("-f"))
				wb.minWordFrequency(Integer.parseInt(options[++i]));
			else if (cur.equals("-lr"))
				wb.minLearningRate(Integer.parseInt(options[++i]));
			else if (cur.equals("-ls"))
				wb.layerSize(Integer.parseInt(options[++i]));
			else if (cur.equals("-ws"))
				wb.windowSize(Integer.parseInt(options[++i]));
			else if (cur.equals("-stop"))
				wb.stopWords(StopWords.getStopWords());
		}
	}

}
