package lucenex;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.queryparser.flexible.standard.nodes.intervalfn.Phrase;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Searching {

    public static String indexPath = "/Users/davidemolitierno/Desktop/UNI/Ingegneria dei Dati/Homeworks/Homework2/index";

    public static void main(String[] args) throws IOException, ParseException {
        search();
    }

    private static void search() throws IOException, ParseException {

        boolean stop = false;

        while(!stop){
            System.out.println("Inserisci una query o scrivi 0 per terminare.");
            Scanner rispostaScanner = new Scanner(System.in);
            String risposta = rispostaScanner.nextLine();
            System.out.println(risposta);
            if(risposta.equals("0"))
                stop = true;
            else elaborateQuery(risposta);
        }

    }

    private static void elaborateQuery(String inputString) throws IOException, ParseException {

        Path path = Paths.get(indexPath);

        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(new String[] {"titolo", "contenuto"}, new StandardAnalyzer());
        queryParser.setDefaultOperator(QueryParser.Operator.OR);
        Query query = queryParser.parse(inputString);
        try (Directory directory = FSDirectory.open(path)) {
            try (IndexReader reader = DirectoryReader.open(directory)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                runQuery(searcher, query);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                directory.close();
            }
    }
}

    private static void runQuery(IndexSearcher searcher, Query query) throws IOException {
        TopDocs docs = searcher.search(query, 10);
        System.out.println("Processando:" + query.toString() +"\n");
        for(int i = 0; i < docs.scoreDocs.length; i++){
            ScoreDoc scoreDoc = docs.scoreDocs[i];
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println("doc"+scoreDoc.doc + ":"+ doc.get("titolo") + " (" + scoreDoc.score +")");
        }
    }
    }
