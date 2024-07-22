package searchengine.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;

public interface SearchService {
    SearchResponse search(String query, String site, Integer offset, Integer limit);

}
