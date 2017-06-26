package com.springboot.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExtractKeyWords {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private List<String> stopwords = new ArrayList<String>();
	/*private WordVectors wordVectors = null;*/

	public ExtractKeyWords() {
		// load stop words
		/*try {
			ClassLoader classLoader = getClass().getClassLoader();
			String tempFilePath = classLoader.getResource("stopword.txt").getPath();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(tempFilePath))));
			String lineTxt = null;
			while ((lineTxt = br.readLine()) != null) {
				stopwords.add(lineTxt);
			}
			br.close();
		} catch (FileNotFoundException e) {
			logger.info("stop words not found in ExtractKeywords construction");
		} catch (IOException e) {
			logger.info("read stop words error in ExtractKeywords construction");
		}
		// load word2vector model
		ClassLoader classLoader = getClass().getClassLoader();
		String tempFilePath = classLoader.getResource("glove.6B.50d.txt").getPath();
		wordVectors = WordVectorSerializer.readWord2VecModel(new File(tempFilePath));*/
	}
/*
	@PreDestroy
	public void destory() {
		// upload index file to predix blob service
		BlobServiceUtil util = new BlobServiceUtil();
		if (util.uploadIndex()) {
			logger.info("upload index successfully");
		}
	}

	*//**
	 * 
	 * @param question
	 * @param answer
	 * @param parameterId
	 * @param indexDir
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 *//*
	public String updateIndex(String question, String answer, String parameterId, String indexDir) {
		// get the directory
		Directory directory = null;
		try {
			directory = FSDirectory.open(Paths.get(indexDir));
		} catch (IOException e) {
			logger.info("There's no directory find in ExtractKeyWords in updateIndex:" + e.getMessage());
		}
		// get the new question
		String[] words = question.split(" ");
		List<String> wordsList = new ArrayList<String>();
		for (String qWords : words) {
			wordsList.add(qWords);
		}
		wordsList.removeAll(stopwords);
		List<String> relatedWords = new ArrayList<String>();
		logger.info(wordsList.toString());
		// get related words by word2vector
		if (wordVectors != null) {
			for (String word : wordsList) {
				relatedWords.addAll(wordVectors.wordsNearestSum(word, 3));
			}
		}
		// add key word append to original question
		StringBuilder newQuestion = new StringBuilder(question);
		for (String relatedWord : relatedWords) {
			newQuestion.append(" " + relatedWord);
		}
		// update the index
		Article article = new Article(question, answer, parameterId, newQuestion.toString());
		Analyzer analyzer = new WhitespaceAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		Document doc = new Document();
		doc.add(new TextField("parameterId", article.getParameterId(), Store.YES));
		doc.add(new TextField("question", article.getQuestion(), Store.YES));
		doc.add(new TextField("answer", article.getAnswer(), Store.YES));
		doc.add(new TextField("newQuestion", article.getNewQuestion(), Store.YES));
		IndexWriter indexWriter = null;
		try {
			indexWriter = new IndexWriter(directory, iwc);
			indexWriter.updateDocument(new Term("parameterId", article.getParameterId()), doc);
		} catch (IOException e) {
			logger.info("There's no directory find in ExtractKeyWords in updateIndex:" + e.getMessage());
		} finally {
			if (indexWriter != null) {
				try {
					indexWriter.close();
				} catch (IOException e) {
					logger.info("Index writer close fail:" + e.getMessage());
				}
			}
		}
		return newQuestion.toString();
	}

	*//**
	 * 
	 * @param question
	 * @param answer
	 * @param parameterId
	 * @param indexDir
	 * @return
	 * @throws IOException
	 *//*
	public String createIndex(String question, String answer, String parameterId, String indexDir) {
		String[] words = question.split(" ");
		List<String> wordsList = new ArrayList<String>();
		for (String qWords : words) {
			wordsList.add(qWords);
		}
		wordsList.removeAll(stopwords);
		List<String> relatedWords = new ArrayList<String>();
		logger.info(wordsList.toString());
		// get related words by word2vector
		if (wordVectors != null) {
			for (String word : wordsList) {
				if (wordVectors.hasWord(word)) {
					List<String> tempWords = (List<String>) wordVectors.wordsNearestSum(word, 3);
					tempWords.remove(word);
					relatedWords.addAll(tempWords);
				}
			}
		}
		// add key word append to original question
		StringBuilder newQuestion = new StringBuilder(question);
		for (String relatedWord : relatedWords) {
			newQuestion.append(" " + relatedWord);
		}
		Article article = new Article(question, answer, parameterId, newQuestion.toString());
		Directory saveDirectory = null;
		try {
			saveDirectory = FSDirectory.open(Paths.get(indexDir));
		} catch (IOException e) {
			logger.info("There's no directory find in ExtractKeyWords in createIndex:" + e.getMessage());
		}
		Analyzer analyzer = new WhitespaceAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		Document doc = new Document();
		doc.add(new TextField("parameterId", article.getParameterId(), Store.YES));
		doc.add(new TextField("question", article.getQuestion(), Store.YES));
		doc.add(new TextField("answer", article.getAnswer(), Store.YES));
		doc.add(new TextField("newQuestion", article.getNewQuestion(), Store.YES));
		IndexWriter indexWriter = null;
		try {
			indexWriter = new IndexWriter(saveDirectory, iwc);
			indexWriter.addDocument(doc);
		} catch (IOException e) {
			logger.info("There's no directory find in ExtractKeyWords in createIndex:" + e.getMessage());
		} finally {
			if (indexWriter != null) {
				try {
					indexWriter.close();
				} catch (IOException e) {
					logger.info("indexWriter close error in ExtractKeyWords in createIndex" + e.getMessage());
				}
			}
		}
		return newQuestion.toString();
	}

	*//**
	 * 
	 * @param queryCondition
	 * @param indexDir
	 * @return
	 *//*
	public Map<String, Object> search(String queryCondition, String indexDir) {
		Map<String, Object> result = new HashMap<String, Object>();
		Directory directory;
		Article article = null;
		try {
			String tempFilePath = indexDir;
			directory = FSDirectory.open(Paths.get(tempFilePath));
			Analyzer analyzer = new WhitespaceAnalyzer();
			QueryParser queryParser = new QueryParser("newQuestion", analyzer);
			Query query = queryParser.parse(queryCondition);
			IndexReader indexReader = DirectoryReader.open(directory);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			TopDocs topDocs = indexSearcher.search(query, 1);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			if (scoreDocs.length > 0) {
				ScoreDoc scoreDoc = scoreDocs[0];
				int docId = scoreDoc.doc;
				Document doc = indexSearcher.doc(docId);
				article = new Article(doc.get("question"), doc.get("answer"), doc.get("parameterId"),
						doc.get("newQuestion"));
			}
			indexSearcher.getIndexReader().close();
		} catch (IOException e) {
			logger.info("There's no index directory to find in ExtractKeyWords.search");
		} catch (ParseException e) {
			logger.info("Parser query condition error in ExtractKeyWords.search");
		}
		if (article != null) {
			result.put("answer", article.getAnswer());
			result.put("parameterId", article.getParameterId());
			return result;
		} else {
			result.put("errormessage", "Please correct your input");
			return result;
		}
	}

	*//**
	 * @param parameterId
	 *            if fail delResult=-1
	 * @param indexDir
	 * @return delResult
	 *//*
	public long deleteIndex(String parameterId, String indexDir) {
		// get the directory
		long delResult = 0;
		Directory directory = null;
		try {
			directory = FSDirectory.open(Paths.get(indexDir));
		} catch (IOException e) {
			logger.info("There's no directory find in ExtractKeyWords in updateIndex:" + e.getMessage());
		}
		Analyzer analyzer = new WhitespaceAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		IndexWriter indexWriter = null;
		try {
			indexWriter = new IndexWriter(directory, iwc);
			delResult = indexWriter.deleteDocuments(new Term("parameterId", parameterId));
		} catch (IOException e) {
			logger.info("There's no directory find in ExtractKeyWords in createIndex:" + e.getMessage());
		} finally {
			if (indexWriter != null) {
				try {
					indexWriter.close();
				} catch (IOException e) {
					logger.info("indexWriter close error in ExtractKeyWords in createIndex" + e.getMessage());
				}
			}
		}
		return delResult;
	}
*/
}
