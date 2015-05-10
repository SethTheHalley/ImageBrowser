package seth.imagebrowser;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import seth.imagebrowser.api.ApiUtils;
import seth.imagebrowser.data.ImgurGallery;
import seth.imagebrowser.data.ImgurImage;


public class MainActivity extends ActionBarActivity implements Callback<ImgurGallery> {
    private static final String TAG = MainActivity.class.getSimpleName();

    private List<ImgurImage> mImages;

    //layout items
    private TableLayout mTableLayout;
    private int mScreenHeight;
    private int mScreenWidth;
    private SearchView searchView;
    private ProgressDialog mProgress;

    private String lastQuery;
    private int cnt;

    private int hDemins;
    private int wDemins;
    private int numColumns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTableLayout = (TableLayout)findViewById(R.id.table_layout);

        //gets screen dimensions
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;

        int orient = getWindowManager().getDefaultDisplay().getRotation();
        String screen_type = (String)findViewById(R.id.main_layout).getTag();

        //decide table row width/height
        if(orient == Surface.ROTATION_0){
            Log.d(TAG,"PORTRAIT");
            if(screen_type.equals("default_screen") || screen_type.equals("large_screen")) {
                hDemins = mScreenHeight/3;
                wDemins = mScreenWidth/2;
                numColumns = 2;
            }
            else{
                hDemins = mScreenHeight/4;
                wDemins = mScreenWidth/3;
                numColumns = 3;
            }
        }else if(orient == Surface.ROTATION_90){
            Log.d(TAG,"LANDSCAPE");
            if(screen_type.equals("default_screen") || screen_type.equals("large_screen")) {
                hDemins = mScreenHeight/2;
                wDemins = mScreenWidth/3;
                numColumns = 3;
            }
            else{
                hDemins = mScreenHeight/3;
                wDemins = mScreenWidth/5;
                numColumns = 5;
            }
        }
        else{
            Log.d(TAG,"UNEXPECTED OTHER ORIENTATIONS " + orient);
            hDemins=mScreenHeight/3;
            wDemins=mScreenWidth/2;
            numColumns = 2;
        }

        if(savedInstanceState != null) {
            Log.d(TAG,"RESTORING STATE");
            lastQuery = savedInstanceState.getString("query");
            savedInstanceState.clear();
            savedInstanceState = null;
            if(lastQuery != null)
                retrieveImages(lastQuery);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(lastQuery != null) {
            Log.d(TAG,"SAVING STATE");
            outState.putString("query", lastQuery);
        }
    }//onSaveInstanceState()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView)findViewById(R.id.searchView);
        if (null != searchView) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                return true;
            }

            //gets string from search bar
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "SEARCHING FOR: " + query);

                searchView.clearFocus();
                retrieveImages(query);
                lastQuery = query;
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
        removeAlbums(imgurGallery.getGalleryImages());
    }

    @Override
    public void failure(RetrofitError error) {
        Log.w(TAG, "Error retrieving search results", error);
        Toast.makeText(this, R.string.search_fail, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //retrieveImages("batman");
    }

    /**
     * Retrieve a list of images, based on search params
     *
     * @param search the string that we are searching imgur with
     */
    protected void retrieveImages(String search) {
        mTableLayout.removeAllViews();

        // set up progress bar
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Finding Images...");
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgress.setIndeterminate(true);
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        if (null == search) {
            return;
        }
        ApiUtils.getImgurService().searchGallery(search, this);
    }

    public void removeAlbums(List<ImgurImage> imgList) {
        mImages = null == imgList ? new ArrayList<ImgurImage>() : new ArrayList<>(imgList);
        ListIterator listIterator = mImages.listIterator();
        while(listIterator.hasNext()){
            ImgurImage m = (ImgurImage)listIterator.next();
            if(m.IsAlbum()) {
                listIterator.remove();
            }
        }
        populateTable();
    }

    private void populateTable () {
        ListIterator listIterator = mImages.listIterator();
        cnt=0;
        String url;
        while(listIterator.hasNext()){
            final TableRow tableRow = new TableRow (MainActivity.this);
            while(listIterator.hasNext() && cnt<numColumns) {
                final ImgurImage imgurImage = (ImgurImage)listIterator.next();
                url = imgurImage.getLink();
                final ImageView imageView = new ImageView(MainActivity.this);
                Picasso.with(getApplicationContext()).load(url).resize(wDemins, hDemins).into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        mProgress.dismiss();
                    }

                    @Override
                    public void onError() {
                        mProgress.dismiss();
                    }
                });
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        TableRow.LayoutParams params = new TableRow.LayoutParams(wDemins, hDemins);
                        imageView.setLayoutParams(params);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent it = new Intent(MainActivity.this, DetailActivity.class);
                                it.putExtra("id", imgurImage.getID());
                                it.putExtra("title", imgurImage.getTitle());
                                it.putExtra("link", imgurImage.getLink());
                                startActivity(it);
                            }
                        });
                        imageView.setBackgroundResource(R.drawable.row_border);
                        tableRow.updateViewLayout(imageView, params);
                    }
                });
                tableRow.addView(imageView);
                cnt++;
            }cnt=0;
            mTableLayout.addView(tableRow);
        }
        searchView.setEnabled(true);
        if(mImages.size()==0){
            Toast.makeText(this, getString(R.string.no_results), Toast.LENGTH_SHORT).show();
            mProgress.dismiss();
        }
    }
}
