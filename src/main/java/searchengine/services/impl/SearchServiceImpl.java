package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchResponse;
import searchengine.model.Site;
import searchengine.services.*;
import searchengine.utils.RelevanceCalc;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final SiteService siteService;
    private final LemmaService lemmaService;
    private final PageService pageService;
    private final IndexService indexService;

    @Override
    public SearchResponse search(String query, String site, Integer offset, Integer limit) {
        SearchResponse response = new SearchResponse();

        if (query.isEmpty()) {
            response.setResult(false);
            response.setError("Задан пустой поисковый запрос");
            return response;
        }

        if (site != null && siteService.getSiteByUrl(site) == null) {
            response.setResult(false);
            response.setError("Сайт не проиндексирован");
            return response;
        }

        List<Site> listSite = new ArrayList<>();

        if (site == null)
            listSite = siteService.findAll();
        else
            listSite.add(siteService.getSiteByUrl(site));

        List<SearchData> listSearchData = new ArrayList<>();
        for (Site siteModel: listSite) {
            RelevanceCalc relevanceCalc = new RelevanceCalc(lemmaService, pageService, indexService);
            listSearchData.addAll(relevanceCalc.getSearchData(siteModel, query));
        }

        response.setCount(listSearchData.size());
        listSearchData.sort((o1, o2) -> o2.getRelevance().compareTo(o1.getRelevance()));
        response.setData(listSearchData);
        response.setResult(true);

        return response;
    }
}
