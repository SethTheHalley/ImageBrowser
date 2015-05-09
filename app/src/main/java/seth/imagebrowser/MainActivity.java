package seth.imagebrowser;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import seth.imagebrowser.api.ApiUtils;
import seth.imagebrowser.data.ImgurGallery;
import seth.imagebrowser.data.ImgurImage;


public class MainActivity extends ActionBarActivity implements Callback<ImgurGallery> {
    private static final String TAG = MainActivity.class.getSimpleName();
    private List<ImgurImage> mImages;
    private List<Bitmap> mBmps;
    private TableLayout mTableLayout;
    private int mScreenHeight;
    private int mScreenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBmps = new ArrayList<Bitmap>();
        mTableLayout = (TableLayout)findViewById(R.id.table_layout);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;

        //(ImageView)findViewById(R.id.testImage).getResources();
        String urlString = "http://i.imgur.com/0Io9iUE.jpg";
        String urlString2 = "http://i.imgur.com/R4UlVKe.jpg";
        //mWinnerMessage.setText(message);

        //check to see if device is connected to the internet before proceeding to download image:
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data:
            //new DownloadImageTask((ImageView) findViewById(R.id.testImage))
            new DownloadImageTask()
                    .execute(urlString);
            //new DownloadImageTask((ImageView) findViewById(R.id.testImage2))
            new DownloadImageTask()
                    .execute(urlString2);
        } else {
            // display error:
        }//end if else
        //populateTable(mBmps);
        //prepDownloadImageActivity("cat");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void success(ImgurGallery imgurGallery, Response response) {
        Log.d(TAG, "Retrieved Images!");

        setImages(imgurGallery.getGalleryImages());
    }

    @Override
    public void failure(RetrofitError error) {
        Log.w(TAG, "Error retrieving search results", error);
        Toast.makeText(this, R.string.search_fail, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        retrieveImages("cats");
    }

    /**
     * Retrieve a list of images, based on search params
     *
     * @param search the city whose forecast should be retrieved.
     */
    protected void retrieveImages(String search) {
        if (null == search) {
            retrieveImages(search);
            return;
        }

        ApiUtils.getImgurService().searchGallery(search, this);
    }

    public void setImages(List<ImgurImage> imgList) {
        mImages = null == imgList ? new ArrayList<ImgurImage>() : new ArrayList<>(imgList);
        ImgurImage img = mImages.get(0);
        //Log.d(TAG,img.getLink());
       // Log.d(TAG,mImages.get(1).getLink());
        for(ImgurImage m : mImages){
            if(!m.IsAlbum())
                Log.d(TAG,m.getLink());
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }
        public DownloadImageTask(){}

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            //bmImage.setImageBitmap(result);
            if(result != null)
            mBmps.add(result);
            populateTable(mBmps);
        }
    }

    private void populateTable (List<Bitmap> lst) {
        for (Bitmap img : lst) {
            final TableRow tableRow = new TableRow (MainActivity.this);
            final ImageView imageView = new ImageView (MainActivity.this);
            imageView.setImageBitmap(img);
            imageView.post(new Runnable() {

                @Override
                public void run() {
                    int newHeight = getWindowManager().getDefaultDisplay().getHeight() / 2;
                    int orgWidth = imageView.getWidth();
                    int orgHeight = imageView.getHeight();

                    //double check my math, this should be right, though
                    int newWidth = (int) Math.floor((orgWidth * newHeight) / orgHeight);

                    //Use RelativeLayout.LayoutParams if your parent is a RelativeLayout
                    TableRow.LayoutParams params = new TableRow.LayoutParams(
                            newWidth, newHeight);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    tableRow.updateViewLayout(imageView, params);
                }

            });
            tableRow.addView(imageView);

            mTableLayout.addView (tableRow);
        }
    }
}
