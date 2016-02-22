package barqsoft.footballscores.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.muzei.FootballMuzeiSource;


public class FootballSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = FootballSyncAdapter.class.getSimpleName();
    public static final String ACTION_DATA_UPDATED =
            "barqsoft.footballscores.ACTION_DATA_UPDATED";
    // Interval at which to sync with the weather, in milliseconds.
// 60 seconds (1 minute) * 480 = 8 hours

    public static final int SYNC_INTERVAL = 60 * 480;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FOOTBALL_STATUS_OK, FOOTBALL_STATUS_SERVER_DOWN, FOOTBALL_STATUS_SERVER_INVALID,  FOOTBALL_STATUS_UNKNOWN, FOOTBALL_STATUS_INVALID,FOOTBALL_STATUS_NO_NETWORK})
    public @interface FootballStatus {}

    public static final int FOOTBALL_STATUS_OK = 0;
    public static final int FOOTBALL_STATUS_SERVER_DOWN = 1;
    public static final int FOOTBALL_STATUS_SERVER_INVALID = 2;
    public static final int FOOTBALL_STATUS_UNKNOWN = 3;
    public static final int FOOTBALL_STATUS_INVALID = 4;
    public static final int FOOTBALL_STATUS_NO_NETWORK = 5;
    private static final int FOOTBALL_NOTIFICATION_ID = 3004;
    public FootballSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }
    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }
    private void updateWidgets() {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void notifyFootball() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if ( displayNotifications ) {

            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                // Last sync was more than 1 day ago, let's send a notification with the weather.


                Uri footballUri =DatabaseContract.scores_table.buildScoreWithDate();
                Date fragmentdate = new Date(System.currentTimeMillis());
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
               String string =mformat.format(fragmentdate);
                // we'll query our contentProvider, as always
                Cursor cursor = context.getContentResolver().query(footballUri, null, null, new String[]{string}, DatabaseContract.scores_table.TIME_COL +" asc");
                String contentText ="";
                if(cursor.moveToFirst()) {
                    contentText  = "There are "+cursor.getCount()+" events scheduled, and the first will start at "+cursor.getString(2);
              //  }
               // if (cursor.moveToFirst()) {


                    int iconId = R.drawable.ic_launcher;
                    Resources resources = context.getResources();
                //    int artResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
                   // String artUrl = Utility.getArtUrlForWeatherCondition(context, weatherId);

                    // On Honeycomb and higher devices, we can retrieve the size of the large icon
                    // Prior to that, we use a fixed size
                    @SuppressLint("InlinedApi")
                    int largeIconWidth = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                            ? resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
                            : resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);
                    @SuppressLint("InlinedApi")
                    int largeIconHeight = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                            ? resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
                            : resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);

                    // Retrieve the large icon
                    Bitmap largeIcon;
                    try {
                        largeIcon = Glide.with(context)
                                .load(iconId)
                                .asBitmap()
                                .error(iconId)
                                .fitCenter()
                                .into(largeIconWidth, largeIconHeight).get();
                    } catch (InterruptedException | ExecutionException e) {
                        Log.e(LOG_TAG, "Error retrieving large icon from " + iconId, e);
                        largeIcon = BitmapFactory.decodeResource(resources, iconId);
                    }
                    String title = context.getString(R.string.app_name);

                    // Define the text of the forecast.


                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
                    // notifications.  Just throw in some data.
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getContext())
                                    .setColor(resources.getColor(R.color.primary_light))
                                    .setSmallIcon(iconId)
                                    .setLargeIcon(largeIcon)
                                    .setContentTitle(title)
                                    .setContentText(contentText);

                    // Make something interesting happen when the user clicks on the notification.
                    // In this case, opening the app is sufficient.
                    Intent resultIntent = new Intent(context, MainActivity.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager =
                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
                    mNotificationManager.notify(FOOTBALL_NOTIFICATION_ID, mBuilder.build());

                    //refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
                    editor.commit();
                }
                cursor.close();
            }
        }
    }
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");


      getData("p2");
      getData("n2");


    }
    private void getData (String timeFrame)
    {
        if (!Utilies.isNetworkAvailable(getContext())) {
            setFootballStatus(getContext(),FOOTBALL_STATUS_NO_NETWORK);
            return;
           // message = R.string.empty_football_list_no_network;
        }
        //Creating fetch URL
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days
        //final String QUERY_MATCH_DAY = "matchday";

        Uri fetch_build = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
        Log.v(LOG_TAG, "The url we are looking at is: "+fetch_build.toString()); //log spam
        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;
        //Opening Connection
        try {
            URL fetch = new URL(fetch_build.toString());
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod("GET");
            m_connection.addRequestProperty("X-Auth-Token", getContext().getString(R.string.api_key));
            m_connection.connect();

            // Read the input stream into a String
            InputStream inputStream = m_connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                setFootballStatus(getContext(), FOOTBALL_STATUS_SERVER_DOWN);

                return;
            }
            JSON_data = buffer.toString();
            //  JSONArray matches = new JSONObject(JSON_data).getJSONArray("fixtures");
            insertFootballDataInLocalDB(JSON_data, getContext(), true);
        }

        catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            setFootballStatus(getContext(), FOOTBALL_STATUS_SERVER_DOWN);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            setFootballStatus(getContext(), FOOTBALL_STATUS_SERVER_INVALID);
        } finally {
            if ( m_connection!= null) {
                m_connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return ;
    }
    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void insertFootballDataInLocalDB (String JSONdata,Context mContext, boolean isReal) throws JSONException
    {
        //JSON data
        // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
        // be updated. Feel free to use the codes
        final String BUNDESLIGA1 = "394";
        final String BUNDESLIGA2 = "395";
        final String LIGUE1 = "396";
        final String LIGUE2 = "397";
        final String PREMIER_LEAGUE = "398";
        final String PRIMERA_DIVISION = "399";
        final String SEGUNDA_DIVISION = "400";
        final String SERIE_A = "401";
        final String PRIMERA_LIGA = "402";
        final String Bundesliga3 = "403";
        final String EREDIVISIE = "404";


        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";

        //Match data
        String League = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;



        JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);


        //ContentValues to be inserted
        Vector<ContentValues> values = new Vector <ContentValues> (matches.length());
        for(int i = 0;i < matches.length();i++)
        {

            JSONObject match_data = matches.getJSONObject(i);
            League = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                    getString("href");
            League = League.replace(SEASON_LINK,"");
            //This if statement controls which leagues we're interested in the data from.
            //add leagues here in order to have them be added to the DB.
            // If you are finding no data in the app, check that this contains all the leagues.
            // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
            if (League.equals(PREMIER_LEAGUE)      ||
                    League.equals(SERIE_A)             ||
                    League.equals(BUNDESLIGA1)         ||
                    League.equals(BUNDESLIGA2)         ||
                    League.equals(PRIMERA_DIVISION) ||
                    League.equals(LIGUE1 ) ||
                    League.equals(LIGUE2 ) ||
                    League.equals(PRIMERA_DIVISION  )
                    )
            {
                match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                        getString("href");
                match_id = match_id.replace(MATCH_LINK, "");
                if(!isReal){
                    //This if statement changes the match ID of the dummy data so that it all goes into the database
                    match_id=match_id+Integer.toString(i);
                }

                mDate = match_data.getString(MATCH_DATE);
                mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                mDate = mDate.substring(0,mDate.indexOf("T"));
                SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    Date parseddate = match_date.parse(mDate+mTime);
                    SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                    new_date.setTimeZone(TimeZone.getDefault());
                    mDate = new_date.format(parseddate);
                    mTime = mDate.substring(mDate.indexOf(":") + 1);
                    mDate = mDate.substring(0,mDate.indexOf(":"));

                    if(!isReal){
                        //This if statement changes the dummy data's date to match our current date range.
                        Date fragmentdate = new Date(System.currentTimeMillis()+((i-2)*86400000));
                        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                        mDate=mformat.format(fragmentdate);
                    }
                }
                catch (Exception e)
                {
                    Log.d(LOG_TAG, "error here!");
                    Log.e(LOG_TAG,e.getMessage());
                }
                Home = match_data.getString(HOME_TEAM);
                Away = match_data.getString(AWAY_TEAM);
                Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                match_day = match_data.getString(MATCH_DAY);
                ContentValues match_values = new ContentValues();
                match_values.put(DatabaseContract.scores_table.MATCH_ID,match_id);
                match_values.put(DatabaseContract.scores_table.DATE_COL,mDate);
                match_values.put(DatabaseContract.scores_table.TIME_COL,mTime);
                match_values.put(DatabaseContract.scores_table.HOME_COL,Home);
                match_values.put(DatabaseContract.scores_table.AWAY_COL,Away);
                match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL,Home_goals);
                match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL,Away_goals);
                match_values.put(DatabaseContract.scores_table.LEAGUE_COL,League);
                match_values.put(DatabaseContract.scores_table.MATCH_DAY,match_day);
                //log spam

                //Log.v(LOG_TAG,match_id);
                //Log.v(LOG_TAG,mDate);
                //Log.v(LOG_TAG,mTime);
                //Log.v(LOG_TAG,Home);
                //Log.v(LOG_TAG,Away);
                //   Log.v(LOG_TAG,League+" - "+Home+" "+Home_goals+":"+Away_goals);
                //Log.v(LOG_TAG,Away_goals);

                values.add(match_values);
            }
        }
        int inserted_data = 0;
        ContentValues[] insert_data = new ContentValues[values.size()];
        values.toArray(insert_data);
        inserted_data = mContext.getContentResolver().bulkInsert(
                DatabaseContract.BASE_CONTENT_URI,insert_data);
        updateWidgets();
        notifyFootball();
        updateMuzei();

        setFootballStatus(mContext, FOOTBALL_STATUS_OK);

        //Log.v(LOG_TAG,"Succesfully Inserted : " + String.valueOf(inserted_data));



    }
    private void updateMuzei() {
        // Muzei is only compatible with Jelly Bean MR1+ devices, so there's no need to update the
        // Muzei background on lower API level devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Context context = getContext();
            context.startService(new Intent(ACTION_DATA_UPDATED)
                    .setClass(context, FootballMuzeiSource.class));
        }
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        FootballSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }
    /**
     * Sets the location status into shared preference.  This function should not be called from
     * the UI thread because it uses commit to write to the shared preferences.
     * @param c Context to get the PreferenceManager from.
     * @param footballStatus The IntDef value to set
     */
    static private void setFootballStatus(Context c, @FootballStatus int footballStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_football_status_key), footballStatus);
        spe.commit();
    }


}