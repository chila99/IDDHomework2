package lucenex;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
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

    public static String indexPath = "../index";

    /**
     * Metodo che permette di effettuare query
     * @throws IOException
     * @throws ParseException
     */
    public static void main(String[] args) throws IOException, ParseException {
        boolean stop = false;
        while(!stop){
            System.out.println("Inserisci una query o scrivi 0 per terminare.");
            System.out.println("Formati disponibili:\n<campo>: <query> --> per effettuare una query su uno specifico campo." +
                    "\n<query> --> per effettuare una query su qualsiasi campo.");
            Scanner rispostaScanner = new Scanner(System.in);
            String risposta = rispostaScanner.nextLine();
            System.out.println(risposta);
            if(risposta.equals("0"))
                stop = true;
            else{
                Query query = elaborateQuery(risposta);
                runQuery(query);
            }
        }

    }

    /**
     * Metodo che elabora la stringa in input
     * @param inputString
     * @return
     * @throws IOException
     * @throws ParseException
     */
    private static Query elaborateQuery(String inputString) throws IOException, ParseException {
        String[] splittedInputString = inputString.split(":");
        QueryParser queryParser;
        Query query;
        if(splittedInputString[0].equals("titolo") || splittedInputString[0].equals("contenuto")){
            Analyzer analyzer = new StandardAnalyzer();
            //per fare in modo che le query per il titolo vengano capitalized
            if(splittedInputString[0].equals("titolo")){
                analyzer = CustomAnalyzer.builder()
                    .withTokenizer("standard")
                    .addTokenFilter("capitalization")
                    .build();
            }
            queryParser = new QueryParser(splittedInputString[0], analyzer);
            query = queryParser.parse(inputString);
        }else{
            queryParser = new MultiFieldQueryParser(new String[] {"titolo", "contenuto"}, new StandardAnalyzer());
            queryParser.setDefaultOperator(QueryParser.Operator.OR);
            query = queryParser.parse(inputString);
        }
        return query;
    }

    /**
     * Metodo che esegui il run della query parametro
     * @param query
     * @throws IOException
     */
    private static void runQuery(Query query) throws IOException {
        Path path = Paths.get(indexPath);
        IndexSearcher searcher;
        try (Directory directory = FSDirectory.open(path)) {
            try (IndexReader reader = DirectoryReader.open(directory)) {
                searcher = new IndexSearcher(reader);
                TopDocs docs = searcher.search(query, 10);
                System.out.println("Processando:" + query.toString() +"\n");
                for(int i = 0; i < docs.scoreDocs.length; i++){
                    ScoreDoc scoreDoc = docs.scoreDocs[i];
                    Document doc = searcher.doc(scoreDoc.doc);
                    System.out.println("doc"+scoreDoc.doc + ":"+ doc.get("titolo") + " (" + scoreDoc.score +")");
        }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                directory.close();
            }
        }
        System.out.println("\n");
    }
}
