package seth.imagebrowser.api;

import seth.imagebrowser.data.ImgurGallery;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;
import static seth.imagebrowser.api.ApiConstants.QUERY_IMAGE_NAME;
import static seth.imagebrowser.api.ApiConstants.QUERY_IMAGE_SORT;
import static seth.imagebrowser.api.ApiConstants.QUERY_PAGE_NUM;
import static seth.imagebrowser.api.ApiConstants.URL_PATH_GALLERY_SEARCH;

/**
 * API interface to Imgur
 */
public interface ImgurService {

    /**
     * Obtain a list of images, based on search parameter
     *
     * @param imageSearch search phrase which the user enters
     * @param callback the callback to be invoked on success or failure.
     */
    @GET(URL_PATH_GALLERY_SEARCH)
    void searchGallery(@Query(QUERY_IMAGE_NAME) String imageSearch,
            Callback<ImgurGallery> callback);

    /**
     * Obtain a list of images, based on search parameter and page number
     *
     * @param imageSearch search phrase which the user enters
     * @param pageNum which page of the gallery is returned
     * @param callback the callback to be invoked on success or failure.
     */
    @GET(URL_PATH_GALLERY_SEARCH)
    void searchGalleryPage(@Query(QUERY_IMAGE_NAME) String imageSearch,
            @Query(QUERY_PAGE_NUM) int pageNum,
            @Query(QUERY_IMAGE_SORT) String sort,
            Callback<ImgurGallery> callback);

}
