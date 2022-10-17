package lucenex;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
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
            System.out.println("Inserisci la query che vuoi processare: ");
            Scanner scanner = new Scanner(System.in);
            String inputString = scanner.nextLine();
            System.out.println("Searching for: " + inputString);

            elaborateQuery(inputString);

            System.out.println("Desideri che ti si torni un nuovo documento? Qualsiasi stringa-->Continua; 0-->fine");
            Scanner rispostaScanner = new Scanner(System.in);
            String risposta = rispostaScanner.nextLine();
            if(risposta.equals("0"))
                stop = true;
        }
    }

    private static void elaborateQuery(String inputString) throws IOException, ParseException {

        Path path = Paths.get(indexPath);

        QueryParser parser = new MultiFieldQueryParser(new String[] {"contenuto", "titolo"}, new WhitespaceAnalyzer());
        Query query = parser.parse(inputString);

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
        System.out.println("Processando:" + query.toString());
        for(int i = 0; i < docs.scoreDocs.length; i++){
            ScoreDoc scoreDoc = docs.scoreDocs[i];
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println("doc"+scoreDoc.doc + ":"+ doc.get("titolo") + " (" + scoreDoc.score +")");
        }
    }
    }
