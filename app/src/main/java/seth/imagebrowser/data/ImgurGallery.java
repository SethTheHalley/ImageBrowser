package seth.imagebrowser.data;

import seth.imagebrowser.api.ApiConstants;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * A Gallery consists of a list of images
 */
public class ImgurGallery {

    @SerializedName(ApiConstants.IMAGE_LIST)
    private List<ImgurImage> mImages;

    public List<ImgurImage> getGalleryImages() {
        return mImages;
    }
}
