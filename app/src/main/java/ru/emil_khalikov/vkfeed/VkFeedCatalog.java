package ru.emil_khalikov.vkfeed;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Axel on 26.07.2017.
 */

public class VkFeedCatalog {
    private static VkFeedCatalog sVkFeedCatalog;
    private List<FeedItem> mFeedItems;

    public VkFeedCatalog() {
        mFeedItems = new ArrayList<>();
    }

    public static VkFeedCatalog getInstance() {
        if (sVkFeedCatalog == null) {
            sVkFeedCatalog = new VkFeedCatalog();
        }

        return sVkFeedCatalog;
    }

    public FeedItem getItem(UUID id) {
        for (FeedItem f: mFeedItems) {
            if (f.getId() == id) {
                return f;
            }
        }
        return null;
    }

    public void append(List<FeedItem> items) {
        mFeedItems.addAll(items);
    }

    public List<FeedItem> getFeedItems() {
        return mFeedItems;
    }

    public void setFeedItems(List<FeedItem> feedItems) {
        mFeedItems = feedItems;
    }

    public void clearFeedItems() {
        mFeedItems.clear();
    }
}
