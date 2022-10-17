package lucenex;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;

public class indexing {

     public static String indexPath = "/Users/davidemolitierno/Desktop/UNI/Ingegneria dei Dati/Homeworks/Homework2/index";
     public static String documentPath = "/Users/davidemolitierno/Desktop/UNI/Ingegneria dei Dati/Homeworks/Homework2/songs";

     public static void main(String args[]) {
         Path path = Paths.get(indexPath);
         System.out.println(path);
         try(Directory directory = FSDirectory.open(path)){
            index(directory, new SimpleTextCodec());
         } catch (IOException e) {
         }
     }

    private static void index(Directory directory, Codec codec) throws IOException {
         CharArraySet englishStopWords = new CharArraySet(Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but",
                 "by", "or", "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their",
                 "then", "there", "these","they", "this", "to", "was", "will", "with"), true);
         CharArraySet italianStopWords = new CharArraySet(Arrays.asList("in", "dei", "di", "a", "da", "in", "con", "su",
                 "per", "tra", "fra", "il", "i", "gli", "le", "e", "o"), true);
         CharArraySet spanishStopWords = new CharArraySet(Arrays.asList("y", "o"), true);
         CharArraySet stopWords = new CharArraySet(englishStopWords, true);
         stopWords.add(italianStopWords);
         stopWords.add(spanishStopWords);

         Analyzer defaultAnalyzer = new StandardAnalyzer();
         Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
         perFieldAnalyzers.put("titolo", new WhitespaceAnalyzer());
         perFieldAnalyzers.put("contenuto", new StandardAnalyzer(stopWords));

         Analyzer analyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);

         IndexWriterConfig config = new IndexWriterConfig(analyzer);
         if (codec != null) {
            config.setCodec(codec);
        }

         IndexWriter writer = new IndexWriter(directory, config);
         writer.deleteAll();
         File dir = new File(documentPath);
         File[] directoryListing = dir.listFiles();

         if (directoryListing != null) {
             for (File child : directoryListing) {
                 Document doc = new Document();
                 doc.add(new TextField("titolo", child.getName(), Field.Store.YES));
                 String text = getContent(child);
                 doc.add(new TextField("Contenuto", text, Field.Store.NO));

                 writer.addDocument(doc);
             }
             writer.commit();
             writer.close();
         } else {
             writer.close();
             return ;
         }
    }

    private static String getContent(File file) throws FileNotFoundException {
        Scanner sc = new Scanner(file);
        String text = null;
        while(sc.hasNextLine()){
            text = sc.nextLine();
        }
        return text;
    }
}
