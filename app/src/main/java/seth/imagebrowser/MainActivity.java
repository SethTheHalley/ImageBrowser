package seth.imagebrowser;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
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
    private ArrayList<String> mLinks;
    private List<Bitmap> mBmps;
    private TableLayout mTableLayout;
    private int mScreenHeight;
    private int mScreenWidth;
    private int mResultsSize;
    private int startIdx;
    private int endIdx;
    private String curQuery;
    private Button next;
    private Button back;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBmps = new ArrayList<Bitmap>();
        mLinks = new ArrayList<String>();
        mTableLayout = (TableLayout)findViewById(R.id.table_layout);
        mResultsSize = 0;
        startIdx=0;
        endIdx=4;
        curQuery=null;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;

        // back button
        back = (Button) findViewById(R.id.prev);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startIdx>0)
                    startIdx-=5;
                if(endIdx >4) {
                    if ((endIdx + 1) % 5 == 0){
                        endIdx-=5;
                    }else{
                        while((endIdx + 1) % 5 != 0){
                            endIdx--;
                        }
                    }
                }
                Log.d("NEXT ", "START "+startIdx + " END "+ endIdx);
                if(curQuery != null && startIdx >=0) {
                    retrieveImages(curQuery);
                    back.setEnabled(false);
                }
            }
        });
        // next button
        next= (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mResultsSize >0 && endIdx < mResultsSize) {
                    startIdx += 5;
                    endIdx += 5;
                    if(endIdx>mResultsSize)
                        endIdx=mResultsSize;
                    Log.d("NEXT ", "START "+startIdx + " END "+ endIdx);
                    if (curQuery != null) {
                        retrieveImages(curQuery);
                        next.setEnabled(false);
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //return true;

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView)findViewById(R.id.searchView);
        if (null != searchView) {
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                // this is your adapter that will be filtered
                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "SEARCHING FOR: " + query);
                searchView.clearFocus();
                curQuery=query;
                startIdx=0;
                endIdx=4;
                retrieveImages(curQuery);
                searchView.setEnabled(false);
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);

        return super.onCreateOptionsMenu(menu);
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
        //retrieveImages("cats");
    }

    /**
     * Retrieve a list of images, based on search params
     *
     * @param search the city whose forecast should be retrieved.
     */
    protected void retrieveImages(String search) {
        mBmps.clear();
        mLinks.clear();
        //mImages.clear();
        mTableLayout.removeAllViews();
        if (null == search) {
            retrieveImages(search);
            return;
        }
        ApiUtils.getImgurService().searchGallery(search, this);
    }

    public void setImages(List<ImgurImage> imgList) {
        mImages = null == imgList ? new ArrayList<ImgurImage>() : new ArrayList<>(imgList);
        for(ImgurImage m : mImages){
            if(!m.IsAlbum()) {
                Log.d(TAG, m.getLink());
                mLinks.add(m.getLink());
            }
        }
        mResultsSize = mLinks.size();
        //check to see if device is connected to the internet before proceeding to download image:
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadImageTask().execute(mLinks);
        } else {
            // display error:
        }
    }

    private class DownloadImageTask extends AsyncTask<ArrayList<String>, Integer, Bitmap> {
        int idx;
        Bitmap mIcon11;
        int len;

        public DownloadImageTask(){
            idx = 0;
        }

        @Override
        protected Bitmap doInBackground(ArrayList<String>... urls) {
            len = mResultsSize;
            Log.d(TAG, "LENGTH " + len);
            if(startIdx >=len && startIdx>4){
                startIdx-=5;
                idx=startIdx;
            }else
                idx=startIdx;
            if(endIdx < len){
                len=endIdx;
            }
            //len = 5;
            Log.d(TAG, "START " + idx + " END: " + len);
            Log.d(TAG, "LENGTH " + len);
            do {
                String urldisplay = urls[0].get(idx);
                Log.d(TAG,"URL:: " + urldisplay);
                mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
                idx++;
                mBmps.add(mIcon11);
            } while(idx<len);
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result){
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
                    int newHeight = mScreenHeight / 3;
                    TableRow.LayoutParams params = new TableRow.LayoutParams(
                            mScreenWidth, newHeight);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    tableRow.updateViewLayout(imageView, params);
                }

            });
            tableRow.addView(imageView);
            mTableLayout.addView (tableRow);
        }
        next.setEnabled(true);
        back.setEnabled(true);
        searchView.setEnabled(true);
    }
}
