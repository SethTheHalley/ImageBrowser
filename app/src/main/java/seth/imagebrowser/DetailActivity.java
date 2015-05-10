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

import com.squareup.picasso.Picasso;

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

        ((TextView)findViewById(R.id.id_textv)).setText("ID: " + id);
        ((TextView)findViewById(R.id.title_textv)).setText("TITLE: "+title);
        Picasso.with(getApplicationContext()).load(url).into((ImageView)findViewById(R.id.detail_imageView));
    }
}
