package com.newsblur.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.content.CursorLoader;
import android.content.Loader;
import android.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.newsblur.R;
import com.newsblur.activity.FeedReading;
import com.newsblur.activity.ItemsList;
import com.newsblur.activity.Reading;
import com.newsblur.database.DatabaseConstants;
import com.newsblur.database.FeedItemsAdapter;
import com.newsblur.domain.Feed;
import com.newsblur.util.DefaultFeedView;
import com.newsblur.util.StateFilter;
import com.newsblur.util.StoryOrder;
import com.newsblur.util.ReadFilter;
import com.newsblur.view.FeedItemViewBinder;

public class FeedItemListFragment extends ItemListFragment implements OnItemClickListener {

	private Feed feed;
    private ListView itemList;

    public static FeedItemListFragment newInstance(Feed feed, StateFilter currentState, DefaultFeedView defaultFeedView) {
		FeedItemListFragment feedItemFragment = new FeedItemListFragment();

		Bundle args = new Bundle();
		args.putSerializable("currentState", currentState);
		args.putSerializable("feed", feed);
        args.putSerializable("defaultFeedView", defaultFeedView);
		feedItemFragment.setArguments(args);

		return feedItemFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		feed = (Feed) getArguments().getSerializable("feed");
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_itemlist, null);

        itemList = (ListView) v.findViewById(R.id.itemlistfragment_list);
        setupBezelSwipeDetector(itemList);
        itemList.setEmptyView(v.findViewById(R.id.empty_view));
        itemList.setOnScrollListener(this);
        if (adapter != null) {
            // normally the list gets set up when the adapter is created, but sometimes
            // onCreateView gets re-called.
            itemList.setAdapter(adapter);
            itemList.setOnItemClickListener(this);
            itemList.setOnCreateContextMenuListener(this);
        }

        getLoaderManager().initLoader(ITEMLIST_LOADER , null, this);

        return v;
    }

    @Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if ((adapter == null) && (cursor != null)) {
            String[] groupFrom = new String[] { DatabaseConstants.STORY_TITLE, DatabaseConstants.STORY_SHORT_CONTENT, DatabaseConstants.STORY_AUTHORS, DatabaseConstants.STORY_TIMESTAMP, DatabaseConstants.STORY_INTELLIGENCE_AUTHORS };
            int[] groupTo = new int[] { R.id.row_item_title, R.id.row_item_content, R.id.row_item_author, R.id.row_item_date, R.id.row_item_sidebar };
            adapter = new FeedItemsAdapter(getActivity(), feed, R.layout.row_item, cursor, groupFrom, groupTo, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            adapter.setViewBinder(new FeedItemViewBinder(getActivity()));
            itemList.setAdapter(adapter);
            itemList.setOnItemClickListener(this);
            itemList.setOnCreateContextMenuListener(this);
       }
       super.onLoadFinished(loader, cursor);
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (getActivity().isFinishing()) return;
		Intent i = new Intent(getActivity(), FeedReading.class);
        i.putExtra(Reading.EXTRA_FEEDSET, getFeedSet());
		i.putExtra(Reading.EXTRA_FEED, feed.feedId);
		i.putExtra(FeedReading.EXTRA_POSITION, position);
		i.putExtra(ItemsList.EXTRA_STATE, currentState);
        i.putExtra(Reading.EXTRA_DEFAULT_FEED_VIEW, defaultFeedView);
		startActivity(i);
	}

}
