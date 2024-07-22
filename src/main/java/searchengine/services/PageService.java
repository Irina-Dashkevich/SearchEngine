package searchengine.services;

import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.utils.Lemmatizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@AllArgsConstructor
public class PageService {
    private final PageRepository pageRepository;
    private final LemmaService lemmaService;
    private final IndexService indexService;

    public Page pageSave(Page page) {
        pageRepository.save(page);
        return page;
    }

    public Page getPageByPath(Site site, String path) {

            ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("path", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("site_id", ExampleMatcher.GenericPropertyMatchers.exact());

            Page pageExample = new Page();
            pageExample.setPath(path);
            pageExample.setSite(site);
            org.springframework.data.domain.Example<Page> example = org.springframework.data.domain.Example.of(pageExample, matcher); // (pageExample, ignoringExampleMatcher);
            List<Page> pageList = null;
            pageList = pageRepository.findAll(example);

            if (pageList.size() == 0)
                return null;
            else
                return pageList.get(0);
    }

    public void deleteAllForSite(Site site) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("site_id", ExampleMatcher.GenericPropertyMatchers.exact());

        Page pageExample = new Page();
        pageExample.setSite(site);
        org.springframework.data.domain.Example<Page> example = org.springframework.data.domain.Example.of(pageExample, matcher);
        List<Page> pageList = null;
        pageList = pageRepository.findAll(example);
        for (Page page: pageList) {
            indexService.deleteAllForPage(page);
            pageRepository.delete(page);
        }
    }

    public void pageIndexing(Page page) {
        Lemmatizer lemmatizer = new Lemmatizer();
        HashMap<String, Integer> hashMapLemmas = lemmatizer.listLemmaCount(Jsoup.parse(page.getContent()).text());
        for(String strLemma: hashMapLemmas.keySet()) {
            Lemma lemma = new Lemma();
            lemma.setSite(page.getSite());
            lemma.setLemma(strLemma);
            lemma = lemmaService.lemmaSave(lemma);
            Index index = new Index();
            index.setPage(page);
            index.setLemma(lemma);
            index.setRank(Float.valueOf(hashMapLemmas.get(strLemma).toString()));
            indexService.indexSave(index);
        }
    }

    public void delete(Page page) {
        indexService.deleteAllForPage(page);
        pageRepository.delete(page);
    }

    public List<Page> listPageWithLemma(Lemma lemma) {

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("lemma_id", ExampleMatcher.GenericPropertyMatchers.exact());

        Index indexExample = new Index();
        indexExample.setLemma(lemma);
        org.springframework.data.domain.Example<Index> example = org.springframework.data.domain.Example.of(indexExample, matcher);
        List<Index> indexList = null;
        indexList = indexService.findAll(example);

        List<Page> pageList = new ArrayList<>();
        for (Index index: indexList)
            if (!pageList.contains(pageRepository.getReferenceById(index.getPage().getId())))
                pageList.add(pageRepository.getReferenceById(index.getPage().getId()));

        return pageList;
    }

    public Float absRelevance(Page page, List<Lemma> lemmaList) {
        Float absRelevance = 0F;
        List<Index> indexList = indexService.findAllForPageListLemma(page, lemmaList);
        for (Index index: indexList)
            absRelevance =+ index.getRank();
        return absRelevance;
    }
}
