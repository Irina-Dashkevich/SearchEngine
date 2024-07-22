package searchengine.services;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import java.util.List;

@Service
@AllArgsConstructor
public class SiteService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final PageService pageService;
    private final LemmaService lemmaService;

    public Site siteSave(Site site) {
        return siteRepository.save(site);
    }

    public Site getSiteById(Long siteId) {
        return siteRepository.getReferenceById(siteId);
    }

    public void delete(String url) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("url", ExampleMatcher.GenericPropertyMatchers.exact());

        Site siteExample = new Site();
        siteExample.setUrl(url);
        org.springframework.data.domain.Example<Site> example = org.springframework.data.domain.Example.of(siteExample, matcher);
        List<Site> siteList = null;
        siteList = siteRepository.findAll(example);
        for (Site site: siteList) {
            pageService.deleteAllForSite(site);
            lemmaService.deleteAllForSite(site);
            siteRepository.delete(site);
        }
    }

    public Site getSiteByUrl(String url) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("url", ExampleMatcher.GenericPropertyMatchers.exact());

        Site siteExample = new Site();
        siteExample.setUrl(url);
        org.springframework.data.domain.Example<Site> example = org.springframework.data.domain.Example.of(siteExample, matcher);
        List<Site> siteList = null;
        siteList = siteRepository.findAll(example);
        if (siteList.size() > 0)
            return siteList.get(0);
        else
            return null;
    }

    public Integer getCountPage(Site site) {
        if (site == null)
            return 0;
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("site_id", ExampleMatcher.GenericPropertyMatchers.exact());

        Page pageExample = new Page();
        pageExample.setSite(site);
        org.springframework.data.domain.Example<Page> example = org.springframework.data.domain.Example.of(pageExample, matcher);
        List<Page> pageList = pageRepository.findAll(example);
        return pageList.size();
    }

    public Integer getCountLemma(Site site) {
        if (site == null)
            return 0;
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("site_id", ExampleMatcher.GenericPropertyMatchers.exact());

        Lemma lemmaExample = new Lemma();
        lemmaExample.setSite(site);
        org.springframework.data.domain.Example<Lemma> example = org.springframework.data.domain.Example.of(lemmaExample, matcher);
        List<Lemma> lemmaList = lemmaRepository.findAll(example);
        return lemmaList.size();
    }

    public List<Site> findAll() {
        return siteRepository.findAll();
    }
}
