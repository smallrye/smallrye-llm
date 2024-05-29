package io.jefrajames.booking;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;

import java.io.File;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.java.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;


@Log
@ApplicationScoped
public class DocRagIngestor {

    // Used by ContentRetriever
    @Produces
    private EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    // Used by ContentRetriever
    @Produces
    private EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

    @Inject
    @ConfigProperty(name = "app.docs-for-rag.dir")
    private File docs;

    private List<Document> loadDocs() {
        return loadDocuments(docs.getPath(), new TextDocumentParser());
    }
    
    public void ingest(@Observes @Initialized(ApplicationScoped.class) Object pointless) {

        long start = System.currentTimeMillis();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 30))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        List<Document> docs = loadDocs();
        ingestor.ingest(docs);

        log.info(String.format("DEMO %d documents ingested in %d msec", docs.size(),
        System.currentTimeMillis() - start));
    }

}
