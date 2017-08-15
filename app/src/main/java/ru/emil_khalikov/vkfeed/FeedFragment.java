package ru.emil_khalikov.vkfeed;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import ru.emil_khalikov.vkfeed.models.Attachment;
import ru.emil_khalikov.vkfeed.models.Group;
import ru.emil_khalikov.vkfeed.models.Item;
import ru.emil_khalikov.vkfeed.models.Likes;
import ru.emil_khalikov.vkfeed.models.NewsfeedGet;
import ru.emil_khalikov.vkfeed.models.Profile;

/**
 * Created by Axel on 27.07.2017.
 */

public class FeedFragment extends Fragment {

    private static final String TAG = "XYX";
    private String mNextStringToken = "";
    private Callbacks mCallbacks;

    private RecyclerView mPostRecyclewView;
    private SwipeRefreshLayout mPostSwipreRefreshLayout;
    private static final int VISIBLE_THRESHOLD = 5;
    private PostAdapter mPostAdapter;

    public interface Callbacks {
        void onPostSelect(UUID id);

        void onLogOut();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public static FeedFragment newInstance() {

        Bundle args = new Bundle();

        FeedFragment fragment = new FeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        getPosts();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_feed, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.logout_dialog_title)
                        .setMessage(R.string.logout_dialog_question)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                VKSdk.logout();
                                mCallbacks.onLogOut();
                            }

                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feed, container, false);
        mPostRecyclewView = (RecyclerView) v.findViewById(R.id.feed_recycler_view);
        mPostRecyclewView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mPostRecyclewView.getContext(),
                LinearLayoutManager.VERTICAL);
        mPostRecyclewView.addItemDecoration(dividerItemDecoration);

        mPostSwipreRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.feed_swiperefresh_view);
        mPostSwipreRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mNextStringToken = "";
                VkFeedCatalog.getInstance().clearFeedItems();
                getPosts();
            }
        });

        setupAdapter();

        return v;
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPostAdapter = new PostAdapter(VkFeedCatalog.getInstance().getFeedItems());
            mPostRecyclewView.setAdapter(mPostAdapter);
            mPostAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    getPosts();
                }
            });
        }
    }

    private void getPosts() {
        VKParameters vkParameters;
        if ("".equals(mNextStringToken)) {
            vkParameters = VKParameters.from(VKApiConst.OWNER_ID, VKAccessToken.currentToken().userId, VKApiConst.FILTERS, "post");
        } else {
            vkParameters = VKParameters.from(VKApiConst.OWNER_ID, VKAccessToken.currentToken().userId, VKApiConst.FILTERS, "post", "start_from", mNextStringToken);
        }
        VKRequest newsfeedRequest = new VKRequest("newsfeed.get", vkParameters);
        newsfeedRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                new ParseItemsTask().execute(response);
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                Log.d(TAG, "attemptFailed request");
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.d(TAG, "VKError request");
            }
        });
    }

    private void parseItems(VKResponse vkResponse, ResponseData response) throws IOException, JSONException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        NewsfeedGet responseObject = gson.fromJson(vkResponse.responseString, NewsfeedGet.class);
        Collections.sort(responseObject.getResponse().getItems(), Collections.reverseOrder(new Item.ItemComporator()));

        response.NextTokenString = responseObject.getResponse().getNextFrom();
        response.Items = new ArrayList<>();
        for (Item item : responseObject.getResponse().getItems()) {
            int sourceId = item.getSourceId();
            Date date = new Date(item.getDate() * 1000L);
            String name = "", photo = "";
            if (sourceId < 0) { // Group
                sourceId = -sourceId;
                for (Group g : responseObject.getResponse().getGroups()) {
                    if (g.getId() == sourceId) {
                        name = g.getName();
                        photo = g.getPhoto100();
                        break;
                    }
                }
            } else { // Person
                for (Profile p : responseObject.getResponse().getProfiles()) {
                    if (p.getId() == sourceId) {
                        name = p.getFirstName() + " " + p.getLastName();
                        photo = p.getPhoto100();
                        break;
                    }
                }
            }

            FeedItem feedItem = null;

            feedItem = new FeedItem(name, photo, date, item.getText(), item.getLikes().getCount());
            for (Attachment attachment : item.getAttachments()) {
                if (attachment.getType().equals("photo"))
                    feedItem.addAttachment(attachment.getPhoto().getPhoto604());
            }

            Likes likes = item.getLikes();
            if (likes != null) {
                feedItem.setLikes(likes.getCount());
            } else {
                feedItem.setLikes(0);
            }


            response.Items.add(feedItem);
        }
    }

    private class ResponseData {
        public List<FeedItem> Items;
        public String NextTokenString;
    }

    private class ParseItemsTask extends AsyncTask<VKResponse, Void, ResponseData> {
        @Override
        protected ResponseData doInBackground(VKResponse... params) {
            ResponseData response = new ResponseData();
            try {
                parseItems(params[0], response);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(ResponseData responseData) {
            VkFeedCatalog.getInstance().append(responseData.Items);
            mNextStringToken = responseData.NextTokenString;
//            setupAdapter();
            mPostAdapter.notifyDataSetChanged();
            mPostSwipreRefreshLayout.setRefreshing(false);
            mPostAdapter.setLoaded();
        }
    }

    private class PostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        FeedItem mFeedItem;

        private ImageView mAvatar;
        private TextView mAuthor;
        private TextView mDate;
        private TextView mText;
        private TextView mLikes;

        public PostHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_post, parent, false));
            mAuthor = (TextView) itemView.findViewById(R.id.post_author);
            mAvatar = (ImageView) itemView.findViewById(R.id.post_avatar);
            mDate = (TextView) itemView.findViewById(R.id.post_date);
            mText = (TextView) itemView.findViewById(R.id.post_text);
            mLikes = (TextView) itemView.findViewById(R.id.post_likes);
            itemView.setOnClickListener(this);
        }

        public void bindPostItem(FeedItem item) {
            mFeedItem = item;
            mAuthor.setText(item.getName());
            mDate.setText(new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.ENGLISH).format(item.getDate()));
            mText.setText(item.getText());
            Picasso.with(getActivity())
                    .load(item.getPhoto())
                    .placeholder(R.drawable.ic_action_name)
                    .into(mAvatar);
            mLikes.setText(Integer.toString(item.getLikes()));
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onPostSelect(mFeedItem.getId());
        }
    }

    private class PostAdapter extends RecyclerView.Adapter<PostHolder> {
        private List<FeedItem> mFeedItems;
        OnLoadMoreListener mOnLoadMoreListener;
        private boolean isLoading;

        public PostAdapter(List<FeedItem> feedItems) {
            mFeedItems = feedItems;

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mPostRecyclewView.getLayoutManager();
            mPostRecyclewView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                private int totalItemCount, lastVisibleItem;

                @Override

                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!isLoading && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                        if (mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            });
        }

        public void setLoaded() {
            isLoading = false;
        }

        public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
            mOnLoadMoreListener = onLoadMoreListener;
        }

        @Override
        public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new PostHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(PostHolder holder, int position) {
            FeedItem feedItem = mFeedItems.get(position);
            holder.bindPostItem(feedItem);
        }

        @Override
        public int getItemCount() {
            return mFeedItems.size();
        }
    }

}
