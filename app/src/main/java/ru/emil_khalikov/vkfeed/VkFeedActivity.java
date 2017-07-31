package ru.emil_khalikov.vkfeed;

import android.support.v4.app.Fragment;

import java.util.UUID;

public class VkFeedActivity extends SingleFragmentActivity implements LoginFragment.Callbacks, FeedFragment.Callbacks {

    @Override
    protected Fragment createFragment() {
        return LoginFragment.newInstance();
    }

    @Override
    public void onLogin() {
        Fragment newsFeed = FeedFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, newsFeed)
                .commit();
    }

    @Override
    public void onPostSelect(UUID id) {
        Fragment previewFragment = PreviewFragment.newInstance(id);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, previewFragment)
                .addToBackStack("preview")
                .commit();
    }

    @Override
    public void onLogOut() {
        Fragment loginFragment = LoginFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, loginFragment)
                .commit();
    }
}
