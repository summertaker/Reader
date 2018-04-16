package com.summertaker.reader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.summertaker.reader.article.ArticleListActivity;
import com.summertaker.reader.common.BaseApplication;
import com.summertaker.reader.common.Config;
import com.summertaker.reader.data.Category;
import com.summertaker.reader.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String mTag = "== " + this.getClass().getSimpleName();

    private Context mContext;

    private int mNavId = 0;

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mContext = MainActivity.this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                if (mNavId == R.id.nav_gallery) {
                    //Intent intent = new Intent(mContext, LoginActivity.class);
                    //startActivity(intent);
                } /* else if (mNavId == R.id.nav_cache) {
                    Intent intent = new Intent(mContext, CacheActivity.class);
                    startActivity(intent);
                } */
                mNavId = 0;
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mListView = findViewById(R.id.listView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Category category = (Category) adapterView.getItemAtPosition(i);
                //Log.e(mTag, site.getName());

                Intent intent = new Intent(mContext, ArticleListActivity.class);
                intent.putExtra("title", category.getTitle());
                intent.putExtra("url", category.getUrl());
                startActivity(intent);
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListView.setSelection(0);
                //mListView.smoothScrollToPosition(0);
                //mListView.setSelectionAfterHeaderView();
            }
        });

        requestData(Config.SERVER_CATEGORY_URL);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        mNavId = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void requestData(final String url) {
        //Log.e(mTag, url);

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(mTag, "onResponse()...\n" + response);
                parseData(url, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(mTag, "ERROR: " + error.networkResponse.statusCode);
                Util.alert(mContext, getString(R.string.error), error.networkResponse.statusCode + ": " + error.toString(), null);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("User-agent", Config.USER_AGENT_MOBILE);
                return headers;
            }
        };

        BaseApplication.getInstance().addToRequestQueue(strReq, mTag);
    }

    private void parseData(String url, String response)
    {
        if (response != null && !response.isEmpty()) {
            ArrayList<Category> categories = new ArrayList<>();
            JSONObject object = null;
            try {
                object = new JSONObject(response);

                JSONArray articles = object.getJSONArray("categories");
                for (int i = 0; i < articles.length(); i++) {
                    JSONObject obj = articles.getJSONObject(i);
                    //Log.e(mTag, "> " + obj.getString("title"));

                    Category data = new Category();
                    data.setTitle(obj.getString("title"));
                    data.setUrl(obj.getString("url"));

                    categories.add(data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            MainAdapter adapter = new MainAdapter(this, categories);
            mListView.setAdapter(adapter);
        }
    }
}
