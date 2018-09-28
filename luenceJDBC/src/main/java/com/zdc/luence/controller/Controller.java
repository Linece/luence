package com.zdc.luence.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zdc.luence.model.Book;
import com.zdc.luence.service.DataSerchService;

@RestController
public class Controller {

	@Autowired
	private DataSerchService dataSerchService;

	@RequestMapping(value = "/getBook")
	public List<Book> createIndex() throws IOException {
		List<Book> books = this.dataSerchService.search();
		List<Document> documents = new ArrayList<Document>();
		Document document = null;
		for(Book book : books) {
			document = new Document();
			// 参数：1、域的名称；2、域的值；3、是否存储；
			// Field idIndex = new TextField("name", file_name, Store.YES);
			Field idIndex = new IntPoint("idIndex", book.getId());
			Field idString = new StoredField("idString", book.getId().toString());
			// TextField：分词、索引、存储
			Field name = new TextField("name", book.getName(), Store.YES);
			// FloatDocValuesField：不分词、索引、不存储，可用于排序，与DoubleDocValuesField、NumericDocValuesField、SortedNumericDocValuesField一样
			Field priceIndex = new FloatDocValuesField("priceIndex", book.getPrice());
			Field priceString = new StoredField("priceString", String.valueOf(book.getPrice()));
			// StoredField：不分词、不索引、存储
			Field pic = new StoredField("pic", book.getPic());
			// TextField：分词、索引、不存储
			Field description = new TextField("description", book.getDescription(), Store.YES);

			document.add(idIndex);
			document.add(idString);
			document.add(name);
			document.add(priceIndex);
			document.add(priceString);
			document.add(pic);
			document.add(description);

			documents.add(document);
		}

		File indexrepository_file = new File("E:\\book\\index");
		Directory directory = null;
		try {
			directory = FSDirectory.open(indexrepository_file.toPath());
			// 新建分析器对象
			Analyzer analyzer = new StandardAnalyzer();
			// analyzer
			// 新建配置对象
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			// 创建一个IndexWriter对象（参数一个索引库，一个配置）
			IndexWriter indexWriter = new IndexWriter(directory, config);
			for(Document doc : documents) {
				indexWriter.addDocument(doc);
			}
			indexWriter.close();
			directory.close();
		} catch(IOException e) {
			// TODO Auto-generated catch block
			directory.close();
			e.printStackTrace();

		}

		return books;
	}

	public List<String> testSearch(Query query) {
		Directory directory = null;
		IndexReader reader = null;
		List<String> strList = new ArrayList<String>();
		try {
			// Directory directory = FSDirectory.open(Paths.get("E:\\666"));

			directory = FSDirectory.open(FileSystems.getDefault().getPath("E:\\book\\index"));

			reader = DirectoryReader.open(directory);
			// reader.close();
			IndexSearcher searcher = new IndexSearcher(reader);

			TopDocs topDocs = searcher.search(query, 100000);
			ScoreDoc[] hits = topDocs.scoreDocs;
			int count = (int) topDocs.totalHits;

			System.out.println("匹配出的记录总数:" + count);
			System.out.println("匹配出的记录总数hits:" + hits.length);
			System.out.println("开始时间:" + new Date());
			for(ScoreDoc scoreDoc : hits) {
				Document doc = searcher.doc(scoreDoc.doc);
				// System.out.println("商品ID：" +
				// Integer.parseInt(doc.get("idString")));
				// System.out.println("商品名称：" + doc.get("name"));
				// System.out.println("商品价格：" +
				// Float.parseFloat(doc.get("priceString")));
				// System.out.println("商品图片地址：" + doc.get("pic"));
				// System.out.println("==========================");
				strList.add("商品ID：" + Integer.parseInt(doc.get("idString")) + ";商品名称：" + doc.get("name") + ";商品价格："
						+ Float.parseFloat(doc.get("priceString")) + ";商品图片地址：" + doc.get("pic") + ";商品描述："
						+ doc.get("description"));
			}

			reader.close();
		} catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				directory.close();
				reader.close();
			} catch(IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
		System.out.println("结束时间:" + new Date());
		return strList;
	}

	@RequestMapping(value = "/search")
	public List<String> indexSearch(String val) throws Exception {
		// 创建query对象
		// 使用QueryParser搜索时，需要指定分词器，搜索时的分词器要和索引时的分词器一致
		// 第一个参数：默认搜索的域的名称
		QueryParser parser = new QueryParser("name", new StandardAnalyzer());

		// 通过queryparser来创建query对象
		// 参数：输入的lucene的查询语句(关键字一定要大写)
		Query query = parser.parse(String.format("name:%s", val));
		// Query query = parser.parse("java OR name:lucene");
		// Query query = parser.parse("java NOT name:lucene");
		// Query query = new TermQuery(new Term("description", val));
		// 创建浮点数排序字段，默认为false:升序;true:降序
		// SortField sortField = new SortField("priceIndex", Type.FLOAT, false);
		// Sort sort = new Sort();
		// sort.setSort(sortField);

		return testSearch(query);
	}

	@RequestMapping(value = "/delete")
	public String deleteIndex() throws Exception {
		// 创建分词器，标准分词器
		Analyzer analyzer = new StandardAnalyzer();

		// 创建IndexWriter
		IndexWriterConfig cfg = new IndexWriterConfig(analyzer);
		Directory directory = FSDirectory.open(FileSystems.getDefault().getPath("E:\\book\\index"));
		// 创建IndexWriter
		IndexWriter writer = new IndexWriter(directory, cfg);

		// Terms
		// writer.deleteDocuments(new Term("idIndex", "1"));

		// 删除全部（慎用）
		writer.deleteAll();

		writer.close();
		return "delete success";
	}

	@RequestMapping(value = "/save")
	public String saveData() {
		this.dataSerchService.insetData();
		return "1";
	}

	public static void main(String[] args) {
		System.out.println(String.format("name:%s", 12));

	}
}
