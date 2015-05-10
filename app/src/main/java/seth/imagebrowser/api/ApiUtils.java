package seth.imagebrowser.api;

import seth.imagebrowser.BuildConfig;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import retrofit.android.AndroidLog;

/**
 * API related utility methods.
 */
public abstract class ApiUtils {
    private static ImgurService sImgurService;

    protected ApiUtils() {
    }

    public static ImgurService getImgurService() {
        if (null == sImgurService) {
            sImgurService = createImgurService();
        }
        return sImgurService;
    }

    private static ImgurService createImgurService() {
        return new RestAdapter.Builder()
                .setEndpoint(ApiConstants.IMGUR_API_BASE_URL)
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader(ApiConstants.HEADER_API_AUTH,
                                ApiConstants.HEADER_API_AUTH_VALUE);
                    }
                })
                .setLog(new AndroidLog("ImgurService"))
                .setLogLevel(BuildConfig.DEBUG ? LogLevel.FULL : LogLevel.NONE)
                .build().create(ImgurService.class);
    }
}
