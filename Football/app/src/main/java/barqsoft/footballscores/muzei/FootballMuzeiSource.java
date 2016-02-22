package barqsoft.footballscores.muzei;

/**
 * Created by shimonaj on 2/16/2016.
 */
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.MuzeiArtSource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.sync.FootballSyncAdapter;

public class FootballMuzeiSource extends MuzeiArtSource {
    public static final String LOG_TAG = "FootballMuzeiSource";
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;

    public FootballMuzeiSource() {
        super("FootballMuzeiSource");
    }

    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        boolean dataUpdated = intent != null &&
                FootballSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction());
        Log.v(LOG_TAG,"onHandleIntent "+dataUpdated+" "+isEnabled());
        if (dataUpdated && isEnabled()) {
            onUpdate(0);
        }
    }

    @Override
    protected void onUpdate(int reason) {
        Uri weatherForLocationUri = DatabaseContract.scores_table.buildScoreWithDate();
        Date fragmentdate = new Date(System.currentTimeMillis());
        Log.v(LOG_TAG,"onUpdate ");
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        String string =mformat.format(fragmentdate);
      Cursor  cursor = getContentResolver().query(weatherForLocationUri,
              null,
              null
              , new String[]{string},
              DatabaseContract.scores_table.DATE_COL + " ASC");
        //String contentText  = "There are "+cursor.getCount()+" events scheduled, and the first will start at "+cursor.getString(2);
        String imageUrl="";
        if(cursor.getCount()==0){
            imageUrl="https://upload.wikimedia.org/wikipedia/commons/7/76/No_match_today_-_geograph.org.uk_-_1521127.jpg";
        }
        ArrayList l = new ArrayList();
        while(cursor.moveToNext()){
            l.add(cursor.getString(COL_HOME));
        }
        if(l.contains("Arsenal London FC") || l.contains("Manchester United FC") || l.contains("Everton FC") ||l.contains("West Ham United FC") ){
            imageUrl = Utilies.getImageUrlForFootball(cursor.getString(COL_HOME));
        }
        if(imageUrl!=""){
            publishArtwork(new Artwork.Builder()
                    .imageUri(Uri.parse(imageUrl))
                    .title("Match at "+cursor.getString(COL_MATCHTIME)+" today")
                    .byline("Teams: "+cursor.getString(COL_HOME)+" v/s "+cursor.getString(COL_AWAY))
                    .viewIntent(new Intent(this, MainActivity.class))
                    .build());
            Log.v(LOG_TAG, "updating wallpaper ");
        }


        cursor.close();
    }
}