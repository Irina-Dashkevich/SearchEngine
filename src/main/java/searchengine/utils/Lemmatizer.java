package searchengine.utils;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import java.util.HashMap;
import java.util.Locale;

public class Lemmatizer {
    LuceneMorphology luceneMorphology;
    public Lemmatizer() {
        try {
            luceneMorphology = new RussianLuceneMorphology();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap listLemmaCount(String text) {
        HashMap<String, Integer> hashMap = new HashMap<>();

        while (text.indexOf("  ") > 0)
            text = text.replace("  ", " ");

        String[] words = text.toLowerCase(Locale.ROOT).split(" ");
        for (String word : words) {
            String normForm = getNormForm(word);
            if (!normForm.isEmpty())
                hashMap.put(normForm, hashMap.containsKey(normForm) ? hashMap.get(normForm) + 1 : 1);
        }

        return hashMap;
    }

    public String getNormForm(String word) {
        String normForm = "";
        try {
            if (!luceneMorphology.checkString(word))
                return "";

            String morphInfo = luceneMorphology.getMorphInfo(word).get(0);
            normForm = luceneMorphology.getNormalForms(word).get(0);

            if (morphInfo.indexOf(" МЕЖД") > 0 ||
                    morphInfo.indexOf(" СОЮЗ") > 0 ||
                    morphInfo.indexOf(" ПРЕДЛ") > 0 ||
                    morphInfo.indexOf(" ЧАСТ") > 0)
                return "";
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return normForm;
    }
}
