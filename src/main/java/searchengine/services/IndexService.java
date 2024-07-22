package searchengine.services;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repositories.IndexRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class IndexService {
    IndexRepository indexRepository;
    LemmaService lemmaService;
    public Index indexSave(Index index) {
        return indexRepository.save(index);
    }

    public void deleteAllForPage(Page page) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("page_id", ExampleMatcher.GenericPropertyMatchers.exact());

        Index indexExample = new Index();
        indexExample.setPage(page);
        org.springframework.data.domain.Example<Index> example = org.springframework.data.domain.Example.of(indexExample, matcher);
        List<Index> indexList = null;
        indexList = indexRepository.findAll(example);
        for (Index index: indexList) {
            lemmaService.minusFrequency(index.getLemma());
            indexRepository.delete(index);
        }
    }

    public List<Index> findAll(Example<Index> example) {
        return indexRepository.findAll(example);
    }

    public Boolean existIndex(Lemma lemma, Page page) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("page_id", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("lemma_id", ExampleMatcher.GenericPropertyMatchers.exact());

        Index indexExample = new Index();
        indexExample.setPage(page);
        indexExample.setLemma(lemma);
        org.springframework.data.domain.Example<Index> example = org.springframework.data.domain.Example.of(indexExample, matcher);
        List<Index> indexList = null;
        indexList = indexRepository.findAll(example);
        return !indexList.isEmpty();
    }

    public List<Index> findAllForPageListLemma(Page page, List<Lemma> listLemma) {
        List<Index> listIndex = new ArrayList<>();
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("page_id", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("lemma_id", ExampleMatcher.GenericPropertyMatchers.exact());

        for (Lemma lemma: listLemma) {
            Index indexExample = new Index();
            indexExample.setPage(page);
            indexExample.setLemma(lemma);
            org.springframework.data.domain.Example<Index> example = org.springframework.data.domain.Example.of(indexExample, matcher);
            List<Index> listIndex_ = null;
            listIndex_ = indexRepository.findAll(example);
            listIndex.addAll(listIndex_);
        }
        return listIndex;
    }
}
