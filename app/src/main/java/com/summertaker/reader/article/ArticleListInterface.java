package com.summertaker.reader.article;

import com.summertaker.reader.data.Article;

public interface ArticleListInterface {

    public void onTitleClick(Article article);

    public void onTitleLongClick(Article article);

    public void onShareClick(Article article);

    public void onImageClick(Article article, String imageUrl);
}
