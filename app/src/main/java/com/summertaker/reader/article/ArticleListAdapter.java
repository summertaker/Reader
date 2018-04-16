package com.summertaker.reader.article;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.summertaker.reader.R;
import com.summertaker.reader.common.BaseDataAdapter;
import com.summertaker.reader.common.Config;
import com.summertaker.reader.data.Article;
import com.summertaker.reader.data.Media;

import java.util.ArrayList;

public class ArticleListAdapter extends BaseDataAdapter {

    private Context mContext;
    private String mLocale;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Article> mArticles = null;

    private ArticleListInterface mArticleListInterface;

    public ArticleListAdapter(Context context, ArrayList<Article> articles, ArticleListInterface articleListInterface) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mArticles = articles;
        this.mArticleListInterface = articleListInterface;
    }

    @Override
    public int getCount() {
        return mArticles.size();
    }

    @Override
    public Object getItem(int position) {
        return mArticles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ArticleListAdapter.ViewHolder holder;
        final Article article = mArticles.get(position);

        if (convertView == null) {
            holder = new ArticleListAdapter.ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.article_list_item, null);

            holder.ivPicture = convertView.findViewById(R.id.ivPicture);
            holder.tvFavorite = convertView.findViewById(R.id.tvFavorite);
            holder.tvTitle = convertView.findViewById(R.id.tvTitle);
            holder.tvContent = convertView.findViewById(R.id.tvContent);
            holder.tvSite = convertView.findViewById(R.id.tvSite);
            holder.tvDate = convertView.findViewById(R.id.tvDate);
            //holder.tvToday = convertView.findViewById(R.id.tvToday);
            holder.tvShare = convertView.findViewById(R.id.tvShare);

            holder.ivYoutube = convertView.findViewById(R.id.ivYoutube);
            holder.ivTwitter = convertView.findViewById(R.id.ivTwitter);
            holder.ivInstagram = convertView.findViewById(R.id.ivInstagram);
            holder.ivVideo = convertView.findViewById(R.id.ivVideo);
            holder.ivDownload = convertView.findViewById(R.id.ivDownload);
            holder.tvImageCounter = convertView.findViewById(R.id.tvImageCounter);

            convertView.setTag(holder);
        } else {
            holder = (ArticleListAdapter.ViewHolder) convertView.getTag();
        }

        if (article.getMedia() == null || article.getMedia().size() == 0) {
            holder.ivPicture.setVisibility(View.GONE);
            holder.tvImageCounter.setVisibility(View.GONE);
        } else {
            holder.ivPicture.setVisibility(View.VISIBLE);
            Media media = article.getMedia().get(0);
            String thumbnail = media.getThumbnail();

            if (media.isCache()) {
                String[] array = thumbnail.split("/");
                thumbnail = Config.SERVER_CACHE_URL + array[array.length-1];
            }
            final String imageUrl = thumbnail;
            holder.ivPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mArticleListInterface.onImageClick(article, imageUrl);
                }
            });

            Glide.with(mContext).load(imageUrl).apply(new RequestOptions().placeholder(R.drawable.placeholder)).into(holder.ivPicture);

            int imageCount = article.getMedia().size();
            if (imageCount > 1) {
                holder.tvImageCounter.setVisibility(View.VISIBLE);
                String countText = imageCount + "";
                holder.tvImageCounter.setText(countText);
            } else {
                holder.tvImageCounter.setVisibility(View.GONE);
            }
        }

        if (article.isFavorite()) {
            holder.tvFavorite.setVisibility(View.VISIBLE);
        } else {
            holder.tvFavorite.setVisibility(View.GONE);
        }

        holder.tvTitle.setText(article.getTitle());
        String content = article.getContent();
        if (content.isEmpty()) {
            holder.tvContent.setVisibility(View.GONE);
        } else {
            holder.tvContent.setVisibility(View.VISIBLE);
            holder.tvContent.setText(article.getContent());
        }

        holder.tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mArticleListInterface.onTitleClick(article);
            }
        });
        holder.tvTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mArticleListInterface.onTitleLongClick(article);
                return true;
            }
        });

        holder.tvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mArticleListInterface.onTitleClick(article);
            }
        });

        holder.tvSite.setText(article.getSite());
        holder.tvDate.setText(article.getDate());

        holder.tvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mArticleListInterface.onShareClick(article);
            }
        });

        if (article.isYoutube()) {
            holder.ivYoutube.setVisibility(View.VISIBLE);
        } else {
            holder.ivYoutube.setVisibility(View.GONE);
        }
        if (article.isTwitter()) {
            holder.ivTwitter.setVisibility(View.VISIBLE);
        } else {
            holder.ivTwitter.setVisibility(View.GONE);
        }
        if (article.isInstagram()) {
            holder.ivInstagram.setVisibility(View.VISIBLE);
        } else {
            holder.ivInstagram.setVisibility(View.GONE);
        }
        if (article.isVideo()) {
            holder.ivVideo.setVisibility(View.VISIBLE);
        } else {
            holder.ivVideo.setVisibility(View.GONE);
        }
        if (article.isDownload()) {
            holder.ivDownload.setVisibility(View.VISIBLE);
        } else {
            holder.ivDownload.setVisibility(View.GONE);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView ivPicture;
        TextView tvFavorite;
        TextView tvTitle;
        TextView tvContent;
        TextView tvSite;
        TextView tvDate;
        TextView tvShare;
        //TextView tvToday;

        ImageView ivYoutube;
        ImageView ivTwitter;
        ImageView ivInstagram;
        ImageView ivVideo;
        ImageView ivDownload;
        TextView tvImageCounter;
    }
}
