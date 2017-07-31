package ru.emil_khalikov.vkfeed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Axel on 27.07.2017.
 */

public class FeedItem {

    private UUID mId;
    private String mName;
    private String mPhoto;
    private Date mDate;
    private String mText;
    private int mLikes;
    private List<String> mAttachments;

    public FeedItem(String name, String photo, Date date, String text, int likes) {
        mId = UUID.randomUUID();

        mName = name;
        mPhoto = photo;
        mDate = date;
        mText = text;
        mLikes = likes;
        mAttachments = new ArrayList<>();
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPhoto() {
        return mPhoto;
    }

    public void setPhoto(String photo) {
        mPhoto = photo;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public int getLikes() {
        return mLikes;
    }

    public void setLikes(int likes) {
        mLikes = likes;
    }

    public List<String> getAttachments() {
        return mAttachments;
    }

    public void setAttachments(List<String> attachments) {
        mAttachments = attachments;
    }

    public void addAttachment(String attachmentUrl) {
        mAttachments.add(attachmentUrl);
    }
}
