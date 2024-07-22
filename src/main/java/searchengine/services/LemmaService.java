package searchengine.services;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.LemmaRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class LemmaService {
    private final LemmaRepository lemmaRepository;

    public Lemma lemmaSave(Lemma lemma) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("lemma", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("site_id", ExampleMatcher.GenericPropertyMatchers.exact());

        Lemma lemmaExample = new Lemma();
        lemmaExample.setSite(lemma.getSite());
        lemmaExample.setLemma(lemma.getLemma());
        org.springframework.data.domain.Example<Lemma> example = org.springframework.data.domain.Example.of(lemmaExample, matcher);
        List<Lemma> lemmaList = null;
        lemmaList = lemmaRepository.findAll(example);
        if (lemmaList.size() > 0) {
            lemma = lemmaList.get(0);
            lemma.setFrequency(lemma.getFrequency() + 1);
        }
        else
            lemma.setFrequency(1);
        return lemmaRepository.save(lemma);
    }

    public void deleteAllForSite(Site site) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("site_id", ExampleMatcher.GenericPropertyMatchers.exact());

        Lemma lemmaExample = new Lemma();
        lemmaExample.setSite(site);
        org.springframework.data.domain.Example<Lemma> example = org.springframework.data.domain.Example.of(lemmaExample, matcher);
        List<Lemma> lemmaList = null;
        lemmaList = lemmaRepository.findAll(example);
        for (Lemma lemma: lemmaList) {
            lemmaRepository.delete(lemma);
        }
    }

    public void minusFrequency(Lemma lemma) {
        Integer frequency = lemma.getFrequency();
        if (frequency < 2) {
            lemmaRepository.delete(lemma);
        }
        else {
            lemma.setFrequency(frequency - 1);
            lemmaRepository.save(lemma);
        }
    }

    public Lemma getLemmaByLemmaStr(Site site, String lemmaStr) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("lemma", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("site_id", ExampleMatcher.GenericPropertyMatchers.exact());

        Lemma lemmaExample = new Lemma();
        lemmaExample.setSite(site);
        lemmaExample.setLemma(lemmaStr);
        org.springframework.data.domain.Example<Lemma> example = org.springframework.data.domain.Example.of(lemmaExample, matcher);
        List<Lemma> lemmaList = null;
        lemmaList = lemmaRepository.findAll(example);
        if (lemmaList.size() == 0)
            return null;
        else
            return lemmaList.get(0);
    }
}
