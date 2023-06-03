package luceneLyrics;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Indexing {

     //path dell'indice
     public static String indexPath = "../index";
     //path della directory da indicizzare
     public static String documentPath = "../songs";

     public static void main(String[] args) {
         Path pathIndex = Paths.get(indexPath);
         try(Directory directory = FSDirectory.open(pathIndex)){
            index(directory, documentPath, null);
         } catch (IOException e) {
             System.out.println("Errore nell'apertura dell'index");
         }
     }

    /**
     * Metodo che indicizza i file all'interno di una directory
     * e se eventualmente inizializzato salva l'indice con un codec
     * @param index
     * @param directoryToBeIndexed
     * @param codec
     * @throws IOException
     */
    private static void index(Directory index, String directoryToBeIndexed, Codec codec) throws IOException {
         CharArraySet stopWords = new CharArraySet(Arrays.asList("di", "a", "da", "in", "con", "su", "per", "tra", "fra",
                 "il", "lo", "la", "i", "gli", "le", "an", "some", "the", "el", "los", "los", "unos", "unas", "de", "desde",
                 "en", "con", "para", "por", "hasta", "entre", "hacia", "tras", "sin", "contra", "según", "sobre", "ante", "bajo"), true);
         Analyzer defaultAnalyzer = new StandardAnalyzer();
         Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
         Analyzer titleAnalyzer = CustomAnalyzer.builder()
                    .withTokenizer("whitespace")
                    .addTokenFilter("capitalization")
                    .build();

         perFieldAnalyzers.put("titolo", titleAnalyzer);
         perFieldAnalyzers.put("contenuto", new StandardAnalyzer(stopWords));

         Analyzer analyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);

         IndexWriterConfig config = new IndexWriterConfig(analyzer);
         if (codec != null) {
            config.setCodec(codec);
        }

         IndexWriter writer = new IndexWriter(index, config);
         writer.deleteAll();

         List<Document> documents = getAllDocumentsFromDirectory(directoryToBeIndexed);
         long startTime = System.currentTimeMillis();
         writer.addDocuments(documents);
         long endTime = System.currentTimeMillis();
         long duration = (endTime - startTime);
         writer.commit();
         writer.close();
         System.out.println("Tempo per indicizzare: " + duration + " ms");
         }

    /**
     * Metodo che ritorna il contenuto di un file
     * sostituendo i caratteri '\n' con uno ' '
     * @return il contenuto del file
     * @throws FileNotFoundException
     */
    private static List<Document> getAllDocumentsFromDirectory(String directoryToBeIndexed) throws FileNotFoundException {
        File dir = new File(directoryToBeIndexed);
        File[] directoryListing = dir.listFiles();
        System.out.println("Indexing " + directoryListing.length + " txt files");
        List<Document> documents = new LinkedList<>();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                Document doc = new Document();
                doc.add(new TextField("titolo", child.getName().split("\\.")[0], Field.Store.YES));
                Scanner sc = new Scanner(child);
                StringBuilder sb = new StringBuilder();
                while(sc.hasNext()) {
                    sb.append(sc.next());
                    sb.append(' ');
                }
                sc.close();
                String text = sb.toString().replace("\n", "").replace("\r", "");
                doc.add(new TextField("contenuto", text, Field.Store.NO));
                documents.add(doc);
            }
        }
        return documents;
    }
}
