package ru.emil_khalikov.vkfeed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Axel on 31.07.2017.
 */

public class PreviewFragment extends Fragment {

    private ImageView mAvatar;
    private TextView mAuthor;
    private TextView mDate;
    private TextView mText;
    private RecyclerView mRecyclerView;

    private FeedItem mFeedItem;
    private static final String KEY_POST_ITEM = "POST_ITEM";

    public static PreviewFragment newInstance(UUID feedItem) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_POST_ITEM, feedItem);
        PreviewFragment fragment = new PreviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_preview, container, false);

        mAvatar = (ImageView)v.findViewById(R.id.feed_avatar);
        mAuthor = (TextView)v.findViewById(R.id.feed_author);
        mDate = (TextView)v.findViewById(R.id.feed_date);
        mText = (TextView)v.findViewById(R.id.post_text);
        mRecyclerView = (RecyclerView)v.findViewById(R.id.feed_photos);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        SetupAdapter();
        Picasso.with(getActivity())
                .load(mFeedItem.getPhoto())
                .placeholder(R.drawable.ic_action_name)
                .into(mAvatar);

        mAuthor.setText(mFeedItem.getName());
        mText.setText(mFeedItem.getText());
        mDate.setText(new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.ENGLISH).format(mFeedItem.getDate()));

        return v;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID id = (UUID)getArguments().getSerializable(KEY_POST_ITEM);
        mFeedItem = VkFeedCatalog.getInstance().getItem(id);
    }

    private void SetupAdapter() {
        if (isAdded()) {
            mRecyclerView.setAdapter(new PhotoAdapter(mFeedItem.getAttachments()));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;

        public PhotoHolder(View itemView) {
            super(itemView);

            mImageView = (ImageView)itemView.findViewById(R.id.item_image_view);
        }

        public void bindPic(String picUrl) {
            Picasso.with(getActivity())
                    .load(picUrl)
                    .placeholder(R.drawable.ic_action_name)
                    .into(mImageView);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        List<String> attachedPics;

        public PhotoAdapter(List<String> items) {
            attachedPics = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_gallery, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            String feedItem = attachedPics.get(position);
            holder.bindPic(feedItem);
        }

        @Override
        public int getItemCount() {
            return attachedPics.size();
        }
    }
}
