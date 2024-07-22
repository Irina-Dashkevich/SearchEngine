package searchengine.services;

import searchengine.dto.indexing.IndexingResponse;

public interface IndexingService {
    Boolean isStopped();
    IndexingResponse startIndexing();
    IndexingResponse stopIndexing();
    IndexingResponse indexingPage(String url);
}
