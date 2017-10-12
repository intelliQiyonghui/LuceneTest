package com.atguigu.search.lucene.test;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;

public class LuceneTest {

    @Test
    public void query() throws IOException, ParseException {
       File indexPath = new File("D:\\my_index");
       FSDirectory directory = FSDirectory.open(indexPath);
       //读取文件索引库,分析器也要一致
        DirectoryReader reader = DirectoryReader.open(directory);
        StandardAnalyzer analyzer = new StandardAnalyzer();
        //创建查询解析器对象
        QueryParser parser = new QueryParser("song_name",analyzer);
        // 解析关键字,并创建IndexSearch对象
        Query query = parser.parse("My Love Maps");
        IndexSearcher searcher = new IndexSearcher(reader);
        //执行查询,并获取查询的数量
        TopDocs docs = searcher.search(query, 10);
        int totalHits = docs.totalHits;
        System.out.println(totalHits);

        ScoreDoc[] scoreDocs = docs.scoreDocs;
        for(ScoreDoc scoreDoc:scoreDocs){
            //文档的id
            int docId = scoreDoc.doc;
            System.out.println("文档的id:"+docId);

            //文档的匹配度,由分数的高低体现
            float score = scoreDoc.score;
            System.out.println("文档的匹配得分:"+score);

            //获取文档的各个属性值
            Document document = searcher.doc(docId);
            IndexableField song_id = document.getField("song_id");
            System.out.println("songId="+song_id.stringValue());

            IndexableField hometown = document.getField("home_town");
            System.out.println("hometown="+hometown.stringValue());

            IndexableField singer = document.getField("singer");
            System.out.println("singer="+singer);

            IndexableField lyric = document.getField("lyric");
            System.out.println("lyric="+lyric);
        }
    }

    @Test
    public void tokenize() throws IOException {
      //创建分词器对象
      Analyzer analyzer1 = new StandardAnalyzer();
      Analyzer analyzer2 = new CJKAnalyzer();
      Analyzer analyzer3 = new SmartChineseAnalyzer();
      Analyzer analyzer4 = new IKAnalyzer();

      //执行分词操作
        TokenStream tokenStream = analyzer4.tokenStream("good", "I love you tom!Let's go to sea.你好，小明，" +
                "吃早饭了吗？我请客你掏钱行不行？饭后我们去开黑，你主攻我打野。");


        //3.重置指针位置
        tokenStream.reset();

        //4.创建指针位置偏移量对象
        OffsetAttribute offset = tokenStream.addAttribute(OffsetAttribute.class);

        //5.创建分词对象
        CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);

        //6.遍历分词结果
        while (tokenStream.incrementToken()) {
            int startOffset = offset.startOffset();
            int endOffset = offset.endOffset();
            System.out.println(startOffset+"["+term+"]"+endOffset);
        }

        analyzer4.close();
    }

    @Test
    public void createIndex() throws IOException {
        //使用FSDirectory类的open()方法打开索引库位置
       File indexPath = new File("D:\\my_index");
       FSDirectory directory = FSDirectory.open(indexPath);
       //创建文档对象,document为一条记录
        Document document1 = new Document();
        Document document2 = new Document();
        Document document3 = new Document();
        //TextField与StringField都表示字符串,但是TextField可分词
        IntField songId = new IntField("song_id",22, Field.Store.YES);
        TextField songName = new TextField("song_name","My Love Maps", Field.Store.YES);
        TextField singer = new TextField("singer","James Allen", Field.Store.YES);
        StringField hometown = new StringField("home_town","Houston NewYork", Field.Store.YES);
        TextField lyric = new TextField("lyric","Oh,My Love is Bigger,World is day by day", Field.Store.NO);
        //把字段域加入文档,构成一行数据
        document1.add(songId);
        document1.add(songName);
        document1.add(singer);
        document1.add(hometown);
        document1.add(lyric);

         songId = new IntField("song_id",24, Field.Store.YES);
         songName = new TextField("song_name","Keep Love Moving", Field.Store.YES);
         singer = new TextField("singer","James Brown", Field.Store.YES);
         hometown = new StringField("home_town","French Noton", Field.Store.YES);
         lyric = new TextField("lyric","BiggerWorld is day by day My Love is for you", Field.Store.NO);
        //把字段域加入文档,构成一行数据
        document2.add(songId);
        document2.add(songName);
        document2.add(singer);
        document2.add(hometown);
        document2.add(lyric);

        songId = new IntField("song_id",12, Field.Store.YES);
        songName = new TextField("song_name","Love is beautiful ", Field.Store.YES);
        singer = new TextField("singer","Brown Belly", Field.Store.YES);
        hometown = new StringField("home_town","Chelsea London", Field.Store.YES);
        lyric = new TextField("lyric","Good good Study day day up", Field.Store.NO);
        //把字段域加入文档,构成一行数据
        document3.add(songId);
        document3.add(songName);
        document3.add(singer);
        document3.add(hometown);
        document3.add(lyric);

        //创建分词器对象
        //创建索引库和查询索引库时使用的分词器必须一致
        Analyzer analyzer = new StandardAnalyzer();
        //封装写入器对象需要的配置信息并创建写入器对象
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3,analyzer);
        IndexWriter writer = new IndexWriter(directory,config);

        writer.addDocument(document1);
        writer.addDocument(document2);
        writer.addDocument(document3);

        //提交并关闭
        writer.commit();
        writer.close();
    }


}
