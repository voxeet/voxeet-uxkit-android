package fr.voxeet.sdk.sample.oauth;

import android.support.annotation.NonNull;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by kevinleperf on 23/07/2018.
 */

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
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl(url)
                .build()
                .create(OAuthCalls.class);
    }
}
