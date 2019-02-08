package fr.voxeet.sdk.sample.oauth;

import android.support.annotation.NonNull;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class OAuthCallsFactory {

    private OAuthCallsFactory() {

    }

    /**
     * Create a new OAuthCalls instance
     *
     * @param url a valid scheme://<ip / domain name> url>
     * @return a valid service from which retrieve access
     */
    @NonNull
    public static OAuthCalls createOAuthCalls(@NonNull String url) {
        return new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl(url)
                .build()
                .create(OAuthCalls.class);
    }
}
