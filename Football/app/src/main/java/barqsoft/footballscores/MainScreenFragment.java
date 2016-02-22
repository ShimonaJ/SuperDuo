package barqsoft.footballscores;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.media.CamcorderProfile;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import barqsoft.footballscores.service.myFetchService;
import barqsoft.footballscores.sync.FootballSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener
{
  //  public ScoresAdapter mAdapter;
    public static final int SCORES_LOADER = 0;
    private String[] fragmentdate = new String[1];
    private int last_selected_item = -1;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private boolean  mAutoSelectView;
    private int mChoiceMode;
    private static final String SELECTED_KEY = "selected_position";

    private MyScoresAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public MainScreenFragment()
    {
    }

    private void update_scores()
    {
        FootballSyncAdapter.syncImmediately(getContext());
//        Intent service_start = new Intent(getActivity(), myFetchService.class);
//        getActivity().startService(service_start);
    }
    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
     //   TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.ForecastFragment,
      //          0, 0);
       // mChoiceMode = a.getInt(R.styleable.ForecastFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
      //  mAutoSelectView = a.getBoolean(R.styleable.ForecastFragment_autoSelectView, false);
       // a.recycle();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to RecyclerView.NO_POSITION,
        // so check for that before storing.
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        mAdapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    public void setFragmentDate(String date)
    {
        fragmentdate[0] = date;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        update_scores();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.scores_list);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        View emptyView = rootView.findViewById(R.id.recyclerview_scores_empty);

        // specify an adapter (see also next example)
        mAdapter = new MyScoresAdapter(getActivity(), new MyScoresAdapter.MyScoresAdapterOnClickHandler() {
            @Override
            public void onClick(double matchId, MyScoresAdapter.ScoresViewHolder vh) {
              //  mAdapter.detail_match_id = selected.match_id;
//                ((Callback) getActivity())
//                        .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                                        locationSetting, date)
//                        );


                MainActivity.selected_match_id =(int)matchId;
                        mPosition = vh.getPosition();
            }
        }, emptyView, 1);

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.

//        final View parallaxView = rootView.findViewById(R.id.parallax_bar);
//        if (null != parallaxView) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//                    @Override
//                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                        super.onScrolled(recyclerView, dx, dy);
//                        int max = parallaxView.getHeight();
//                        if (dy > 0) {
//                            parallaxView.setTranslationY(Math.max(-max, parallaxView.getTranslationY() - dy / 2));
//                        } else {
//                            parallaxView.setTranslationY(Math.min(0, parallaxView.getTranslationY() - dy / 2));
//                        }
//                    }
//                });
//            }
//        }




        //mAdapter = new MyScoresAdapter(MainActivity.selected_match_id);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
        });
       // mAdapter = new ScoresAdapter(getActivity(),null,0);
       // score_list.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER,null,this);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELECTED_KEY)) {
                // The Recycler View probably hasn't even been populated yet.  Actually perform the
                // swapout in onLoadFinished.
                mPosition = savedInstanceState.getInt(SELECTED_KEY);
            }
            mAdapter.onRestoreInstanceState(savedInstanceState);
        }
       // mAdapter.detail_match_id = MainActivity.selected_match_id;
//        mRecyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener()
//        {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
//            {
//                ViewHolder selected = (ViewHolder) view.getTag();
//                mAdapter.detail_match_id = selected.match_id;
//                MainActivity.selected_match_id = (int) selected.match_id;
//                mAdapter.notifyDataSetChanged();
//            }
//        });
        return rootView;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRecyclerView) {
            mRecyclerView.setOnScrollListener(null);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        if(fragmentdate[0]!=null) {
            return new CursorLoader(getActivity(), DatabaseContract.scores_table.buildScoreWithDate(),
                    null, null, fragmentdate, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        mAdapter.swapCursor(cursor);
        if (mPosition != RecyclerView.NO_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
        //updateEmptyView();
        if ( cursor.getCount() > 0 ) {
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    // Since we know we're going to get items, we keep the listener around until
                    // we see Children.
                    if (mRecyclerView.getChildCount() > 0) {
                        mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        int itemPosition = mAdapter.getSelectedItemPosition();
                        if ( RecyclerView.NO_POSITION == itemPosition ) itemPosition = 0;
                        RecyclerView.ViewHolder vh = mRecyclerView.findViewHolderForPosition(itemPosition);
                        if ( null != vh && mAutoSelectView ) {
                            mAdapter.selectView( vh );
                        }
                        return true;
                    }
                    return false;
                }
            });
        }

        // ((MyScoresAdapter) mAdapter).notifyDataSetChanged();
        //mAdapter.notifyDataSetChanged();
    }
    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ( key.equals(getString(R.string.pref_football_status_key)) ) {
            updateEmptyView();
        }
    }
    /*
           Updates the empty list view with contextually relevant information that the user can
           use to determine why they aren't seeing weather.
        */
    private void updateEmptyView() {
        if ( mAdapter.getItemCount() == 0 ) {
            TextView tv = (TextView) getView().findViewById(R.id.recyclerview_scores_empty);
            if ( null != tv ) {
                // if cursor is empty, why? do we have an invalid location
                int message = R.string.empty_scores_list;

                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                int location =    sp.getInt(getContext().getString(R.string.pref_football_status_key), myFetchService.FOOTBALL_STATUS_UNKNOWN);


             //   @SunshineSyncAdapter.LocationStatus int location = Utility.getLocationStatus(getActivity());
                switch (location) {
                    case myFetchService.FOOTBALL_STATUS_SERVER_DOWN:
                        message = R.string.empty_football_list_server_down;
                        break;
                    case myFetchService.FOOTBALL_STATUS_SERVER_INVALID:
                        message = R.string.empty_football_list_server_error;
                        break;
                    case myFetchService.FOOTBALL_STATUS_INVALID:
                        message = R.string.empty_football_list_invalid_location;
                        break;
                    case myFetchService.FOOTBALL_STATUS_NO_NETWORK:
                        message = R.string.empty_football_list_no_network;
                        break;
//                    default:
//                        if (!Utilies.isNetworkAvailable(getActivity())) {
//                            message = R.string.empty_football_list_no_network;
//                        }
                }
                tv.setText(message);
            }
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mRecyclerView.setAdapter(null);
       // mAdapter.swapCursor(null);
    }


}
