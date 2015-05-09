package seth.imagebrowser.data;

import android.os.Parcel;
import android.os.Parcelable;
import seth.imagebrowser.api.ApiConstants;
import com.google.gson.annotations.SerializedName;

/**
 * Image object from gallery results list
 */
public class ImgurImage implements Parcelable {

    public static final Creator<ImgurImage> CREATOR = new Creator<ImgurImage>() {
        @Override
        public ImgurImage createFromParcel(Parcel parcel) {
            ImgurImage searchResults = new ImgurImage();
            searchResults.mId = parcel.readString();
            searchResults.mLink = parcel.readString();
            searchResults.mTitle = parcel.readString();
            searchResults.mIsAlbum = parcel.readByte() != 0;

            return searchResults;
        }

        @Override
        public ImgurImage[] newArray(int size) {
            return new ImgurImage[size];
        }
    };

    @SerializedName(ApiConstants.IMAGE_ID)
    private String mId;

    @SerializedName(ApiConstants.IMAGE_LINK)
    private String mLink;

    @SerializedName(ApiConstants.IMAGE_TITLE)
    private String mTitle;

    @SerializedName(ApiConstants.IMAGE_ALBUM)
    private boolean mIsAlbum;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mId);
        parcel.writeString(mLink);
        parcel.writeString(mTitle);
        parcel.writeByte((byte) (mIsAlbum ? 1 : 0));
    }

    public String getID() {
        return mId;
    }

    public String getLink() {
        return mLink;
    }

    public String getTitle() {
        return mTitle;
    }

    public Boolean IsAlbum() {
        return mIsAlbum;
    }
}
