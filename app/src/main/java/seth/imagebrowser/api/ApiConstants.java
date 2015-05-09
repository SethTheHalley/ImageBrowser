package seth.imagebrowser.api;

/**
 * Set of constants used by the API.
 */
public abstract class ApiConstants {

    public static final String IMGUR_API_BASE_URL = "https://api.imgur.com/3";
    public static final String URL_PATH_GALLERY_SEARCH = "/gallery/search";

    public static final String HEADER_API_AUTH = "Authorization";
    public static final String HEADER_API_AUTH_VALUE = "Client-ID 0101ce3d2a8fe7c";

    public static final String QUERY_IMAGE_NAME = "q";
    public static final String IMAGE_LIST = "data";
    public static final String IMAGE_ID = "id";
    public static final String IMAGE_TITLE = "title";
    public static final String IMAGE_LINK = "link";
    public static final String IMAGE_ALBUM = "is_album";

    protected ApiConstants() {
    }
}
