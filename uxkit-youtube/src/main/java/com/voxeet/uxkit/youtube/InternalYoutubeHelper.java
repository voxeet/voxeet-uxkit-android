package com.voxeet.uxkit.youtube;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.sdk.utils.Opt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InternalYoutubeHelper {

    static String regex = "^((?:https?:)?\\/\\/)?((?:www|m)\\.)?((?:youtube(-nocookie)?\\.com|youtu.be))(\\/(?:[\\w\\-]+\\?v=|embed\\/|v\\/)?)([\\w\\-]+)(\\S+)?$";

    static final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    @Nullable
    static String trySetProtocol(@Nullable String url) {
        if (null == url) return url;

        try {
            if (url.startsWith("//")) url = "https" + url;

            Uri uri = Uri.parse(url);
            if (null == uri) throw new IllegalStateException("invalid url found, just skip");

            if (null == uri.getScheme()) return "https://" + url;
            return url;
        } catch (Throwable e) {
            String message = Opt.of(e.getMessage()).or("");
            if (message.contains("no protocol")) {
                return "https://" + url;
            }
            return null;
        }
    }

    @Nullable
    static String getVideoId(@Nullable String url) throws Throwable {
        if (null == url) return "";

        //try fixing the url
        url = InternalYoutubeHelper.trySetProtocol(url);

        Uri uri = Uri.parse(url);

        // match url where v is in the query parameters
        String v = uri.getQueryParameter("v");
        if (null != v) return v;

        List<String> pathSegments = uri.getPathSegments();

        //match where having ..../v/XXXXXX or /embed/XXXXXXX
        List<String> validPathSegmentsSplitters = Arrays.asList("v", "embed");
        for (String splitter : validPathSegmentsSplitters) {
            if (pathSegments.size() >= 2) {
                v = pathSegments.get(pathSegments.size() - 2);
                if (splitter.equals(v)) return pathSegments.get(pathSegments.size() - 1);
            }
        }

        // check for youtu.be/XXXXX urls
        List<String> onePathSegmentHosts = Collections.singletonList("youtu.be");
        if (pathSegments.size() == 1) {
            for (String host : onePathSegmentHosts) {
                if (host.equals(uri.getHost())) return pathSegments.get(0);
            }
        }

        // check for /user/User#p/a/u/\d/xxxxxx
        if (pathSegments.size() == 2 && "user".equals(pathSegments.get(0))) {
            String fragment = uri.getFragment();
            if (null != fragment) {
                String[] split = fragment.split("/");
                return split[split.length - 1];
            }
        }

        return null;
    }

    public static boolean isUrlCompatible(@NonNull String url) {
        Matcher matcher = InternalYoutubeHelper.pattern.matcher(url);
        return matcher.matches();
    }
}
