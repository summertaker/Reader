package com.summertaker.reader.article;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.summertaker.reader.R;
import com.summertaker.reader.common.BaseActivity;
import com.summertaker.reader.common.BaseApplication;
import com.summertaker.reader.common.Config;
import com.summertaker.reader.data.Article;
import com.summertaker.reader.data.Media;
import com.summertaker.reader.util.EndlessScrollListener;
import com.summertaker.reader.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ArticleListActivity extends BaseActivity implements ArticleListInterface {

    private String mTag = "== " + getClass().getSimpleName();

    private Context mContext;

    private int mPage = 1;
    private boolean mIsLoading = false;
    private boolean mHasMoreData = true;

    private LinearLayout mLoLoading;
    private LinearLayout mLoLoadMore;

    private String mUrl = "";
    private String mLastNo = "";

    private ArrayList<Article> mArticles;
    private ArticleListAdapter mAdapter;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_list_activity);

        mContext = ArticleListActivity.this;

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        mUrl = intent.getStringExtra("url");

        initToolbar(title);

        mLoLoading = findViewById(R.id.loLoading);
        mLoLoadMore = findViewById(R.id.loLoadMore);

        mArticles = new ArrayList<>();
        mAdapter = new ArticleListAdapter(this, mArticles, this);

        mListView = findViewById(R.id.listView);
        mListView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                //Log.e(mTag, "onLoadMore()... " + mPage + " Page.");

                if (mHasMoreData && !mIsLoading) {
                    mLoLoadMore.setVisibility(View.VISIBLE);
                    loadData();
                    return true; // ONLY if more data is actually being loaded; false otherwise.
                } else {
                    return false;
                }
            }
        });
        mListView.setAdapter(mAdapter);

        loadData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onSwipeRight() {
        finish();
    }

    @Override
    protected void onSwipeLeft() {

    }

    public void onToolbarClick() {
        mListView.setSelection(0);
    }

    private void loadData() {
        if (mIsLoading) {
            return;
        }
        mIsLoading = false;

        String url = mUrl + mLastNo;
        requestData(url);
    }

    private void requestData(final String url) {
        Log.e(mTag, url);

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(mTag, "requestData().onResponse()...\n" + response);
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
                //headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("User-agent", Config.USER_AGENT_MOBILE);

                return headers;
            }
        };

        BaseApplication.getInstance().addToRequestQueue(strReq, mTag);
    }

    private void parseData(String url, String response) {
        if (url.contains("favorite.php")) {
            String article_no = "";
            String value = "";
            try {
                Pattern p = Pattern.compile("article_no=([^&]+)");
                Matcher m = p.matcher(url);
                while (m.find()) {
                    article_no = m.group(1);
                }

                p = Pattern.compile("value=([^&]+)");
                m = p.matcher(url);
                while (m.find()) {
                    value = m.group(1);
                }
            } catch (PatternSyntaxException ex) {
                Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
            //Log.e(mTag, "article_no: "+ article_no + ", value: " + value);

            if (!article_no.isEmpty()) {
                boolean found = false;
                boolean favorite = !value.isEmpty();
                for (int i = 0; i < mArticles.size(); i++) {
                    String no = mArticles.get(i).getNo();
                    if (no.equals(article_no)) {
                        found = true;
                        mArticles.get(i).setFavorite(favorite);
                        mAdapter.notifyDataSetChanged();

                        String message = value.isEmpty() ? getString(R.string.favorite_removed): getString(R.string.favorite_added);
                        //Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                if (!found) {
                    Toast.makeText(mContext, "Article not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mContext, "article_no is empty.", Toast.LENGTH_SHORT).show();
            }
        } else {
            JSONObject object = null;
            try {
                object = new JSONObject(response);

                JSONArray articleArray = object.getJSONArray("articles");
                for (int i = 0; i < articleArray.length(); i++) {
                    JSONObject obj = articleArray.getJSONObject(i);
                    //Log.e(mTag, "> " + obj.getString("title"));

                    String no = obj.getString("no");
                    mLastNo = no;

                    Article article = new Article();
                    article.setNo(no);
                    article.setSite(obj.getString("site"));
                    article.setTitle(obj.getString("title"));
                    article.setContent(obj.getString("content"));
                    article.setDate(obj.getString("published"));
                    article.setUrl(obj.getString("url"));

                    String favorite_no = obj.getString("favorite_no");
                    //Log.e(mTag, "favorite_no: " + favorite_no);
                    boolean favorite = !favorite_no.equals("null");
                    article.setFavorite(favorite);

                    mArticles.add(article);
                }

                JSONArray mediaArray = object.getJSONArray("media");
                for (int i = 0; i < mediaArray.length(); i++) {
                    JSONObject obj = mediaArray.getJSONObject(i);
                    String article_no = obj.getString("article_no");

                    boolean cache = obj.getString("cache").equals("1");

                    boolean youtube = false;
                    boolean twitter = false;
                    boolean instagram = false;
                    boolean video = false;

                    for (int j = 0; j < mArticles.size(); j++) {
                        if (mArticles.get(j).getNo().equals(article_no)) {
                            //Log.e(mTag, no + ") url: " + url);

                            String thumbnail = obj.getString("thumbnail");
                            if (thumbnail.contains("theqoo.net") ||
                                    thumbnail.contains("ruliweb.com") ||
                                    thumbnail.contains("inven.co.kr") ||
                                    thumbnail.contains("bobaedream.co.kr") ||
                                    thumbnail.contains("slrclub.com")) {
                                String[] array = thumbnail.split("/");
                                thumbnail = array[array.length - 1];
                                thumbnail = Config.SERVER_CACHE_URL + thumbnail;
                            }
                            // http://media.slrclub.com/1803/14/s06bts69dql1dbogd8r.jpg

                            if (thumbnail.contains("youtube")) {
                                youtube = true;
                            }
                            if (thumbnail.contains("twitter")) {
                                twitter = true;
                            }
                            if (thumbnail.contains("instagram")) {
                                instagram = true;
                            }
                            if (thumbnail.contains("ext_tw_video_thumb")) {
                                video = true;
                            }
                            String iframe = obj.getString("iframe");
                            if (!iframe.isEmpty()) {
                                video = true;
                            }

                            Media media = new Media();
                            media.setThumbnail(thumbnail);
                            media.setCache(cache);
                            mArticles.get(j).getMedia().add(media);
                            mArticles.get(j).setYoutube(youtube);
                            mArticles.get(j).setTwitter(twitter);
                            mArticles.get(j).setInstagram(instagram);
                            mArticles.get(j).setVideo(video);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            renderData();
        }
    }

    private void renderData() {
        mAdapter.notifyDataSetChanged();

        if (mPage == 1) {
            mLoLoading.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            mLoLoadMore.setVisibility(View.GONE);
        }

        mPage++;
        mIsLoading = false;
    }

    @Override
    public void onTitleClick(Article article) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.getUrl()));
        startActivity(intent);
    }

    @Override
    public void onTitleLongClick(Article article) {
        //Log.e(mTag, article.getTitle());

        String value = article.isFavorite() ? "" : "1";
        String url = Config.SERVER_FAVORITE_URL + "?article_no=" + article.getNo() + "&value=" + value;
        //Log.e(mTag, url);

        requestData(url);
    }

    @Override
    public void onShareClick(Article article) {
        String title = article.getTitle();
        String url = article.getUrl();

        //Log.e(mTag, title + " " + url);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, title));
    }

    @Override
    public void onImageClick(Article article, String imageUrl) {
        /*
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
        startActivity(intent);
        */
        onTitleClick(article);
    }
}
