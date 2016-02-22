package barqsoft.footballscores.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.widget.TodayWidgetProvider;

/**
 * Created by shimonaj on 2/15/2016.
 */
public class WidgetUpdateIntentService extends IntentService {
    public static final String LOG_TAG = "WidgetUpdateIntentService";
    public WidgetUpdateIntentService()
    {
        super("WidgetUpdateIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TodayWidgetProvider.class));

        // Get today's data from the ContentProvider
      //  String location = Utility.getPreferredLocation(this);
        Uri footballUri = DatabaseContract.scores_table.buildScoreWithDate();
        Date fragmentdate = new Date(System.currentTimeMillis());
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        String string =mformat.format(fragmentdate);
        // we'll query our contentProvider, as always

        Cursor cursor = getContentResolver().query(footballUri, null, null, new String[]{string}, DatabaseContract.scores_table.TIME_COL + " asc");

        if (cursor == null) {
            return;
        }
        if (!cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        // Extract the weather data from the Cursor
        int iconId = R.drawable.ic_launcher;
//        int weatherId = data.getInt(INDEX_WEATHER_ID);
//        int weatherArtResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
//        String description = data.getString(INDEX_SHORT_DESC);
//        double maxTemp = data.getDouble(INDEX_MAX_TEMP);
//        String formattedMaxTemperature = Utility.formatTemperature(this, maxTemp);


        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_today_small;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews
            views.setImageViewResource(R.id.widget_icon, iconId);
           String contentText  = "There are "+cursor.getCount()+" events scheduled, and the first will start at "+cursor.getString(2);
            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, contentText);
            }
            views.setTextViewText(R.id.widget_text, contentText);
//            views.addView(R.id.widget_list, new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_list_item));
//            mAdapter = new ScoresAdapter(getApplicationContext(),null,0);
//            mAdapter.swapCursor(cursor);
//            cursor.close();
//            views.setRemoteAdapter(R.id.widget_list,mAdapter);
            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
        cursor.close();
        return;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_icon, description);
    }
}
