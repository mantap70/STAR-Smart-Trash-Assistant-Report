package com.mantao.star;

/**
 * Model artikel statis (konten edukasi bawaan app, bukan dari backend/CMS).
 */
public class Article {

    public final String id;
    public final String category;
    public final String emoji;
    public final String title;
    public final String shortDescription;
    public final String fullBody;
    public final int readTimeMinutes;
    public final boolean featured;

    public Article(String id, String category, String emoji, String title,
                   String shortDescription, String fullBody, int readTimeMinutes, boolean featured) {
        this.id = id;
        this.category = category;
        this.emoji = emoji;
        this.title = title;
        this.shortDescription = shortDescription;
        this.fullBody = fullBody;
        this.readTimeMinutes = readTimeMinutes;
        this.featured = featured;
    }
}