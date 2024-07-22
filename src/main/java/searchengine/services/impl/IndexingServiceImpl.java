package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.ConnectionParametersJsoup;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Page;
import searchengine.services.PageService;
import searchengine.services.SiteService;
import searchengine.utils.Lemmatizer;
import searchengine.utils.SiteProcessor;
import searchengine.services.IndexingService;
import searchengine.utils.Util;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sites;
    private final ConnectionParametersJsoup connectionParametersJsoup;
    private final SiteService siteService;
    private final PageService pageService;

    private boolean isStopped = false;
    IndexingService indexingService = this;

    @Override
    public Boolean isStopped() {
        return isStopped;
    }

    @Override
    public IndexingResponse startIndexing() {
        isStopped = false;
        IndexingResponse response = new IndexingResponse();

        List<Site> sitesList = sites.getSites();
        boolean isStopped = false;

        for (int i = 0; i < sitesList.size() && !isStopped; i++) {
            String url = sitesList.get(i).getUrl();
            String name = sitesList.get(i).getName();
            siteService.delete(url);
            ForkJoinPool poolSite = new ForkJoinPool();
            SiteProcessor siteProcessor = new SiteProcessor(url, name, connectionParametersJsoup, indexingService, siteService, pageService);
            poolSite.execute(siteProcessor);
            poolSite.shutdown();
        }

        response.setError("");
        response.setResult(true);
        return response;
    }

    @Override
    public IndexingResponse stopIndexing() {
        isStopped = true;
        IndexingResponse response = new IndexingResponse();
        response.setError("");
        response.setResult(true);
        return response;
    }

    @Override
    public IndexingResponse indexingPage(String url) {
        IndexingResponse response = new IndexingResponse();
        searchengine.model.Site site = siteService.getSiteByUrl(Util.getUrlSiteByUrlPage(url));

        if (site == null) {
            response.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
            response.setResult(false);
            return response;
        }

        Page page = pageService.getPageByPath(site, Util.getRelativePath(url));
        if (page != null)
            pageService.delete(page);

        isStopped = false;

        SiteProcessor siteProcessor = new SiteProcessor(null, null, connectionParametersJsoup, indexingService, siteService, pageService);
        siteProcessor.addPageInDB(site, Util.getRelativePath(url),true);

        response.setError("");
        response.setResult(true);
        return response;
    }



}