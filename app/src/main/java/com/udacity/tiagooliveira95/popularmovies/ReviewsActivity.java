package com.udacity.tiagooliveira95.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReviewsActivity extends AppCompatActivity {
    public static String ARG_AUTHOR_NAME = "authorName";
    public static String ARG_AUTHOR_CONTENT = "authorContent";
    public static String ARG_SOURCE = "source";

    @BindView(R.id.authorName) TextView authorName;
    @BindView(R.id.authorContent) TextView authorContent;


    String source = "", txtAuthorName = "", txtAuthorContent = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        ButterKnife.bind(this);

        if(getSupportActionBar() != null)
            getSupportActionBar().setTitle(getIntent().getStringExtra("title"));

        if(savedInstanceState == null) {
            txtAuthorContent = getIntent().getStringExtra(ARG_AUTHOR_CONTENT);
            txtAuthorName = getIntent().getStringExtra(ARG_AUTHOR_NAME);
            authorName.setText(txtAuthorName);
            authorContent.setText(txtAuthorContent);
            source = getIntent().getStringExtra(ARG_SOURCE);
        }else if (savedInstanceState.containsKey(ARG_SOURCE) && savedInstanceState.containsKey(ARG_AUTHOR_NAME) && savedInstanceState.containsKey(ARG_AUTHOR_CONTENT)){
            source = savedInstanceState.getString(ARG_SOURCE,"");
            txtAuthorContent = savedInstanceState.getString(ARG_AUTHOR_CONTENT,"");
            txtAuthorName = savedInstanceState.getString(ARG_AUTHOR_NAME,"");
            authorName.setText(txtAuthorName);
            authorContent.setText(txtAuthorContent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reviews_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_link){
            openSource();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }
    }

    void openSource(){
        if(source.length() < 0)
            return;
        if (!source.startsWith("http://") && !source.startsWith("https://"))
            source = "http://" + source;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(source));
        startActivity(browserIntent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ARG_AUTHOR_CONTENT,txtAuthorContent);
        outState.putString(ARG_AUTHOR_NAME,txtAuthorName);
        outState.putString(ARG_SOURCE,source);
        super.onSaveInstanceState(outState);
    }
}
