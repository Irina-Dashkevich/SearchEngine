package searchengine.utils;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import searchengine.dto.search.SearchData;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.services.IndexService;
import searchengine.services.LemmaService;
import searchengine.services.PageService;

import java.util.*;

@RequiredArgsConstructor
public class RelevanceCalc {
    private final LemmaService lemmaService;
    private final PageService pageService;

    private final IndexService indexService;
    private Lemmatizer lemmatizer = new Lemmatizer();

    public List<SearchData> getSearchData(Site site, String query) {
        List<SearchData> listSearchData = new ArrayList<>();
        HashMap<String, Integer> hashMapLemmas = lemmatizer.listLemmaCount(query);
        List<Lemma> listLemma = new ArrayList<>();

        for(String strLemma: hashMapLemmas.keySet()) {
            Lemma lemma = lemmaService.getLemmaByLemmaStr(site, strLemma);
            if (lemma == null)
                return listSearchData;
            listLemma.add(lemma);
        }

        Comparator<Lemma> comparator = Comparator.comparing(obj -> obj.getFrequency());
        Collections.sort(listLemma, comparator);

        List<Page> pageList = pageService.listPageWithLemma(listLemma.get(0));

        for (int i = 1; i < listLemma.size(); i++)
            for (Page page: pageList)
                if (!indexService.existIndex(listLemma.get(i), page))
                    pageList.remove(page);

        HashMap<Page, Float> hashMapPage = new HashMap<>();

        Float maxAbsRelevance = 0F;
        for (Page page: pageList) {
            Float absRelevance = pageService.absRelevance(page, listLemma);
            hashMapPage.put(page, absRelevance);
            if (maxAbsRelevance < absRelevance)
                maxAbsRelevance = absRelevance;
        }

        for (Page page: hashMapPage.keySet())
        {
            Document document = Jsoup.parse(page.getContent());
            Element element = document.select("head > title").first();
            String title = element.text();
            String snippet = getSnippet(listLemma, Jsoup.parse(page.getContent()).text());
            Float relevance = hashMapPage.get(page)/maxAbsRelevance;
            SearchData searchData = new SearchData(site.getUrl(), site.getName(), page.getPath(), title, snippet, relevance);
            listSearchData.add(searchData);
        }

        return listSearchData;
    }

    public String getSnippet(List<Lemma> lemmaList, String context) {
        String snippet = "";
        while (context.indexOf("  ") > 0)
            context = context.replace("  ", " ");

        String[] words = context.toLowerCase(Locale.ROOT).split(" ");
        String[][] normWords = new String[2][words.length];
        List<String> strLemmaList = new ArrayList<>();
        for (Lemma lemma: lemmaList)
            strLemmaList.add(lemma.getLemma());

        int iBegin = -1;
        int iEnd = -1;
        for (int i = 0; i < words.length; i++) {
            normWords[0][i] = lemmatizer.getNormForm(words[i]);

            if (strLemmaList.contains(normWords[0][i])) {
                normWords[1][i] = "+";
                iBegin = (iBegin == -1 ? i : iBegin);
                iEnd = i;
            }
            else
                normWords[1][i] = "-";
        }
        int iCntWordOut = 100;
        if (iEnd - iBegin < iCntWordOut) {
            iBegin = (iBegin - (iCntWordOut/2) < 0 ? 0 : iBegin - (iCntWordOut/2));
            iEnd = (iEnd + (iCntWordOut/2) >= words.length ? words.length - 1 : iEnd + (iCntWordOut/2));
        }
        for (int i = iBegin; i <= iEnd; i++)
            snippet = snippet + " " + (normWords[1][i].equals("+") ? "<b>" : "") + normWords[0][i] + (normWords[1][i].equals("+") ? "</b>" : "");

        return "<html>" + snippet + "</html>";
    }
}
