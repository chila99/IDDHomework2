package lucenex;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class indexing {

     //path dell'indice
     public static String indexPath = "../index";
     //path della directory da indicizzare
     public static String documentPath = "../songs";

     public static void main(String[] args) {
         Path path = Paths.get(indexPath);
         try(Directory directory = FSDirectory.open(path)){
            index(directory, null);
         } catch (IOException e) {
             System.out.println("Errore nell'apertura dell'index");
         }
     }

    /**
     * Metodo che indicizza i file all'interno di una directory
     * e se eventualmente inizializzato salva l'indice con un codec
     * @param directory
     * @param codec
     * @throws IOException
     */
    private static void index(Directory directory, Codec codec) throws IOException {

         Analyzer defaultAnalyzer = new StandardAnalyzer();
         Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
         perFieldAnalyzers.put("titolo", new WhitespaceAnalyzer());
         perFieldAnalyzers.put("contenuto", new StandardAnalyzer());

         Analyzer analyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);

         IndexWriterConfig config = new IndexWriterConfig(analyzer);
         if (codec != null) {
            config.setCodec(codec);
        }

         IndexWriter writer = new IndexWriter(directory, config);
         writer.deleteAll();
         File dir = new File(documentPath);
         File[] directoryListing = dir.listFiles();
         System.out.println("Indexing " + directoryListing.length + " txt files");

         long startTime = System.nanoTime();
         if (directoryListing != null) {
             for (File child : directoryListing) {
                 Document doc = new Document();
                 doc.add(new TextField("titolo", child.getName().split("\\.")[0], Field.Store.YES));
                 String text = getContent(child);
                 text = text.replace("\n", "").replace("\r", "");
                 doc.add(new TextField("contenuto", text, Field.Store.NO));

                 writer.addDocument(doc);
             }
             writer.commit();
             writer.close();
             long endTime = System.nanoTime();
             long duration = (endTime - startTime)/1000000;
             System.out.println("Tempo per indicizzare: " + duration + " ms");
         } else {
             writer.close();
         }
    }

    /**
     * Metodo che ritorna il contenuto di un file
     * sostituendo i caratteri '\n' con uno ' '
     * @param file
     * @return il contenuto del file
     * @throws FileNotFoundException
     */
    private static String getContent(File file) throws FileNotFoundException {
        Scanner sc = new Scanner(file);
        StringBuilder sb = new StringBuilder();
        while(sc.hasNext()) {
            sb.append(sc.next());
            sb.append(' ');
        }
        sc.close();
        return sb.toString();
    }
}
