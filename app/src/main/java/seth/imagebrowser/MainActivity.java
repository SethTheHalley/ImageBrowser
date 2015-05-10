package seth.imagebrowser;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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

    private int cnt;

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

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
     * @param search the city whose forecast should be retrieved.
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
            retrieveImages(search);
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
            while(listIterator.hasNext() && cnt<2) {
                final ImgurImage imgurImage = (ImgurImage)listIterator.next();
                url = imgurImage.getLink();
                final ImageView imageView = new ImageView(MainActivity.this);
                Picasso.with(getApplicationContext()).load(url).resize(mScreenWidth / 2, mScreenHeight / 3).into(imageView);
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        int newHeight = mScreenHeight / 3;
                        TableRow.LayoutParams params = new TableRow.LayoutParams(
                                mScreenWidth / 2, newHeight);
                        imageView.setLayoutParams(params);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent it = new Intent(MainActivity.this, DetailActivity.class);
                                it.putExtra("id", imgurImage.getID());
                                it.putExtra("title",  imgurImage.getTitle());
                                it.putExtra("link",  imgurImage.getLink());
                                startActivity(it);
                            }
                        });
                        tableRow.updateViewLayout(imageView, params);
                    }
                });
                tableRow.addView(imageView);
                cnt++;
            }cnt=0;
            mTableLayout.addView(tableRow);
        }
        searchView.setEnabled(true);
        mProgress.dismiss();
    }
}
