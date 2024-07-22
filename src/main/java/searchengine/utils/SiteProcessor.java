package searchengine.utils;

import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.ExampleMatcher;
import searchengine.config.ConnectionParametersJsoup;
import searchengine.model.EnumStatus;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.services.PageService;
import searchengine.services.SiteService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.RecursiveTask;
@AllArgsConstructor
public class SiteProcessor extends RecursiveTask<Integer> {
    private String url;
    private String name;
    private final ConnectionParametersJsoup connectionParametersJsoup;
    private final IndexingService indexingService;
    private final SiteService siteService;
    private final PageService pageService;

    @Override
    protected Integer compute() {
        Site site = new Site();
        site.setName(name);
        site.setUrl(url.endsWith("/")? url.substring(0, url.length() - 1): url);
        site.setStatus(EnumStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        site.setLastError("");
        siteService.siteSave(site);
        addPageInDB(site, "/", false);
        site.setStatus(EnumStatus.INDEXED);
        siteService.siteSave(site);
        return 0;
    }

    public void addPageInDB(Site site, String path, Boolean isOnlyCurrent) {
        if (indexingService.isStopped())
            return;

        if (pageService.getPageByPath(site, path) != null) {
            return;
        }

        try {
            Thread.sleep(150);
            org.jsoup.Connection connectionUrl =
                    Jsoup.connect(site.getUrl() + path)
                            .userAgent(connectionParametersJsoup.getUserAgent())
                            .referrer(connectionParametersJsoup.getReferrer())
                            .timeout(100000)
                            .ignoreHttpErrors(true);

            org.jsoup.Connection.Response response = connectionUrl.execute();
            Document document = connectionUrl.post();
            String content = document.toString().replace((char) 39, '"');
            Page page = new Page();
            page.setSite(site);
            page.setPath(path);
            page.setCode(response.statusCode());
            page.setContent(content);
            pageService.pageSave(page);
            if (page.getCode() < 400)
                pageService.pageIndexing(page);

            site.setStatusTime(LocalDateTime.now());
            siteService.siteSave(site);

            if (isOnlyCurrent)
                return;

            Elements elements = document.select("a[href^=" + path + "]");
            for (Element element : elements) {
                String path_ = element.attr("href");

                // проверяем, является ли адрес ссылкой на страницу
                if ((!path_.isEmpty() && path_.substring(path_.length() - 1).equals("#")) || !path_.endsWith("/"))
                    continue;
                addPageInDB(site, path_, false);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
