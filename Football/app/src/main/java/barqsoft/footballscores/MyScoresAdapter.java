package barqsoft.footballscores;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by shimonaj on 2/10/2016.
 */
public class MyScoresAdapter extends RecyclerView.Adapter<MyScoresAdapter.ScoresViewHolder> {
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;
    public static double detail_match_id = 0;
    private String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";
    private Context mContext;
    public final  ItemChoiceManager mICM;
    final private View mEmptyView;

     private  Cursor mCursor;
    final private MyScoresAdapterOnClickHandler mClickHandler;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public  class ScoresViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView home_name;
        public TextView away_name;
        public TextView score;
        public TextView date;
        public ImageView home_crest;
        public ImageView away_crest;
        public TextView league_textview;
        public ImageView share_button;
        //public double match_id;
        public ViewGroup detail_container;
        public ViewGroup focus_score;


        public ScoresViewHolder(View view) {
            super(view);
            home_name = (TextView) view.findViewById(R.id.home_name);
            away_name = (TextView) view.findViewById(R.id.away_name);
            score     = (TextView) view.findViewById(R.id.score_textview);
            date      = (TextView) view.findViewById(R.id.data_textview);
            home_crest = (ImageView) view.findViewById(R.id.home_crest);
            away_crest = (ImageView) view.findViewById(R.id.away_crest);
            focus_score = (ViewGroup) view.findViewById(R.id.focus_score);
            detail_container = (ViewGroup) view.findViewById(R.id.details_fragment_container);
share_button = (ImageView)view.findViewById(R.id.share_button);
            league_textview = (TextView)view.findViewById(R.id.league_textview);
            view.setOnClickListener(this);


        }
        @Override
        public void onClick(View v) {
            int adapterPosition = getPosition();
            mCursor.moveToPosition(adapterPosition);

            mClickHandler.onClick(mCursor.getDouble(COL_ID), this);
            detail_match_id = mCursor.getDouble(COL_ID);

            mICM.onClick(this);
        }


    }
    public static interface MyScoresAdapterOnClickHandler {
        void onClick(double match_id, ScoresViewHolder vh);
    }
    public MyScoresAdapter(Context context, MyScoresAdapterOnClickHandler dh, View emptyView, int choiceMode) {
        mContext = context;
        mClickHandler = dh;
        mEmptyView = emptyView;
        mICM = new ItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);
    }

public void swapCursor(Cursor cursor){

    this.mCursor = cursor;
    mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);

    notifyDataSetChanged();
}
    // Create new views (invoked by the layout manager)
    @Override
    public MyScoresAdapter.ScoresViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        mContext = parent.getContext();
        View v = LayoutInflater.from
                (mContext).inflate(R.layout.scores_list_item, parent, false);

     final  MyScoresAdapter.ScoresViewHolder mHolder = new MyScoresAdapter.ScoresViewHolder(v);

        return mHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ScoresViewHolder mHolder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if(mCursor==null){
            return;
        }
        mCursor.moveToPosition(position);


        mHolder.home_name.setText(mCursor.getString(COL_HOME));
        mHolder.home_name.setContentDescription(mCursor.getString(COL_HOME));

        mHolder.away_name.setText(mCursor.getString(COL_AWAY));
        mHolder.away_name.setContentDescription(mCursor.getString(COL_AWAY));

        mHolder.date.setText(mCursor.getString(COL_MATCHTIME));
        mHolder.date.setContentDescription(mCursor.getString(COL_MATCHTIME));

        mHolder.score.setText(Utilies.getScores(mCursor.getInt(COL_HOME_GOALS), mCursor.getInt(COL_AWAY_GOALS)));
        mHolder.focus_score.setContentDescription("Match between Teams " + mCursor.getString(COL_HOME) +" and "+ mCursor.getString(COL_AWAY) +" Score is"+ Utilies.getScores(mCursor.getInt(COL_HOME_GOALS), mCursor.getInt(COL_AWAY_GOALS)));
        //mHolder.match_id = mCursor.getDouble(COL_ID);

        mHolder.home_crest.setImageResource(Utilies.getTeamCrestByTeamName(
                mCursor.getString(COL_HOME)));
        mHolder.away_crest.setImageResource(Utilies.getTeamCrestByTeamName(
                mCursor.getString(COL_AWAY)
        ));
        mHolder.league_textview.setText(Utilies.getLeague(mCursor.getInt(COL_LEAGUE)));
        mHolder.league_textview.setContentDescription(Utilies.getLeague(mCursor.getInt(COL_LEAGUE)));
        //Log.v(FetchScoreTask.LOG_TAG,mHolder.home_name.getText() + " Vs. " + mHolder.away_name.getText() +" id " + String.valueOf(mHolder.match_id));
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(detail_match_id));

        mHolder.share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add Share Action
                mContext.startActivity(createShareForecastIntent(mCursor.getString(COL_HOME) + " "
                        + Utilies.getScores(mCursor.getInt(COL_HOME_GOALS), mCursor.getInt(COL_AWAY_GOALS)) + " "+mCursor.getString(COL_AWAY) + " "));
            }
        });

        ViewGroup container = (ViewGroup) mHolder.detail_container;
        if(mCursor.getDouble(COL_ID) == detail_match_id)
        {
            //Log.v(FetchScoreTask.LOG_TAG,"will insert extraView");

         populateDetailView(container);
        }
        else
        {
            container.removeAllViews();
        }
        mICM.onBindViewHolder(mHolder, position);


    }
public void populateDetailView(ViewGroup container){
    LayoutInflater vi = (LayoutInflater) mContext.getApplicationContext()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = vi.inflate(R.layout.detail_fragment, null);
    container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
            , ViewGroup.LayoutParams.MATCH_PARENT));
    TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
    match_day.setText(Utilies.getMatchDay(mCursor.getInt(COL_MATCHDAY),
            mCursor.getInt(COL_LEAGUE)));
    match_day.setContentDescription(Utilies.getMatchDay(mCursor.getInt(COL_MATCHDAY),
            mCursor.getInt(COL_LEAGUE)));
//    TextView league = (TextView) v.findViewById(R.id.league_textview);
//    league.setText(Utilies.getLeague(mCursor.getInt(COL_LEAGUE)));
//    Button share_button = (Button) v.findViewById(R.id.share_button);
//    share_button.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View v)
//        {
//            //add Share Action
//            mContext.startActivity(createShareForecastIntent(mCursor.getString(COL_HOME)+" "
//                    +Utilies.getScores(mCursor.getInt(COL_HOME_GOALS), mCursor.getInt(COL_AWAY_GOALS)) + " "+mCursor.getString(COL_AWAY) + " "));
//        }
//    });
}
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(mCursor!=null) {
            return mCursor.getCount();
        }
        return 0;
    }
    public int getSelectedItemPosition() {
        return mICM.getSelectedItemPosition();
    }
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        mICM.onSaveInstanceState(outState);
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof ScoresViewHolder ) {
            ScoresViewHolder vfh = (ScoresViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }

    public Intent createShareForecastIntent(String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

}
