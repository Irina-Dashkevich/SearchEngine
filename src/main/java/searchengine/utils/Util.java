package searchengine.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Getter
@Component
@RequiredArgsConstructor
public class Util {
    public static String getUrlSiteByUrlPage(String url) {
        int startIndex = url.indexOf("//");
        int endIndex = (url.indexOf("/", startIndex + 2) == -1? url.length() - 1: url.indexOf("/", startIndex + 2));
        return url.substring(0, endIndex);
    }

    public static String getRelativePath(String url) {
        int startIndex = url.indexOf("//");
        int endIndex = (url.indexOf("/", startIndex + 2) == -1? url.length() - 1: url.indexOf("/", startIndex + 2));
        if (url.length() == endIndex)
            return "/";
        else
            return url.substring(endIndex);
    }
}
