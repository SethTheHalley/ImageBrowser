package seth.imagebrowser;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Seth on 5/9/2015.
 */
public class DetailActivity extends ActionBarActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        String id=intent.getStringExtra("id");
        String title=intent.getStringExtra("title");
        String url=intent.getStringExtra("link");
        Log.d("EXTRAS","ID: " + id + " TITLE: " + title + " URL: " + url);

        ((TextView)findViewById(R.id.id_textv)).setText("ID: "+id);
        ((TextView)findViewById(R.id.title_textv)).setText("TITLE: "+title);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadImageTask().execute(url);
        } else {
            // display error:
        }

    }

    private class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
        Bitmap mIcon11;

        public DownloadImageTask(){
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
                String urldisplay = urls[0];
                Log.d("ASYNC","URL:: " + urldisplay);
                mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result){
            ((ImageView)findViewById(R.id.detail_imageView)).setImageBitmap(result);
        }
    }
}
