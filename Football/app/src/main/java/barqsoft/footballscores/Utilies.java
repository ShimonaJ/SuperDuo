package barqsoft.footballscores;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies
{
   // public static final int SERIE_A = 357;
    public static final int PREMIER_LEGAUE = 354;
//    public static final int CHAMPIONS_LEAGUE = 362;
//    public static final int PRIMERA_DIVISION = 358;
//    public static final int BUNDESLIGA = 351;

    public static final int BUNDESLIGA1        = 394;
    public static final int BUNDESLIGA2        = 395;


    public static final int LIGUE             = 396;
    public static final int PREMIER_LEAGUE    = 398;
    public static final int PRIMERA_DIVISION  = 399;
    public static final int SEGUNDA_DIVISION  = 400;
    public static final int SERIE_A           = 401;
    public static final int PRIMERA_LIGA      = 402;
    public static final int EREDIVISIE        = 404;
    public static final int CHAMPIONS_LEAGUE  = 405;
    public static String getLeague(int league_num)
    {
        switch (league_num)
        {
            case SERIE_A : return "Seria A";
            case PREMIER_LEAGUE : return "Premier League";
            case LIGUE : return "League";
            case CHAMPIONS_LEAGUE : return "UEFA Champions League";
            case PRIMERA_DIVISION : return "Primera Division";
            case BUNDESLIGA1 : return "Bundesliga 1";
            case BUNDESLIGA2 : return "Bundesliga 2";
            case SEGUNDA_DIVISION : return "Segunda Division";
            case PRIMERA_LIGA : return "Primera League";
            case EREDIVISIE : return "Eredivisie";

            default: return "Unnown League, Please report";
        }
    }
    public static String getMatchDay(int match_day,int league_num)
    {
        if(league_num == CHAMPIONS_LEAGUE)
        {
            if (match_day <= 6)
            {
                return "Group Stages, Matchday : 6";
            }
            else if(match_day == 7 || match_day == 8)
            {
                return "First Knockout round";
            }
            else if(match_day == 9 || match_day == 10)
            {
                return "QuarterFinal";
            }
            else if(match_day == 11 || match_day == 12)
            {
                return "SemiFinal";
            }
            else
            {
                return "Final";
            }
        }
        else
        {
            return "Matchday : " + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals,int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }
    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return true if the network is available
     */
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static int getTeamCrestByTeamName (String teamname)
    {
        if (teamname==null){return R.drawable.no_icon;}
        switch (teamname)
        { //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case "Arsenal London FC" : return R.drawable.arsenal;
            case "Manchester United FC" : return R.drawable.manchester_united;
            case "Swansea City" : return R.drawable.swansea_city_afc;
            case "Leicester City" : return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC" : return R.drawable.everton_fc_logo1;
            case "West Ham United FC" : return R.drawable.west_ham;
            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC" : return R.drawable.sunderland;
            case "Stoke City FC" : return R.drawable.stoke_city;
            default: return R.drawable.no_icon;
        }
    }
    public static String getImageUrlForFootball (String teamname)
    {
        if (teamname==null){return "https://upload.wikimedia.org/wikipedia/commons/7/76/No_match_today_-_geograph.org.uk_-_1521127.jpg";}
        switch (teamname)
        {
            case "Arsenal London FC" : return "https://upload.wikimedia.org/wikipedia/commons/a/af/Arsenal_vs_Fenerbahce_%289611227081%29.jpg";
            case "Manchester United FC" : return "https://upload.wikimedia.org/wikipedia/commons/c/cf/Manchester_United_%288051530616%29.jpg";
//            case "Swansea City" : return R.drawable.swansea_city_afc;
//            case "Leicester City" : return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC" : return "https://upload.wikimedia.org/wikipedia/commons/2/20/DinamoKiev-Everton_%2810%29.jpg";
            case "West Ham United FC" : return "https://upload.wikimedia.org/wikipedia/commons/e/e8/West_Ham_Vs_Birkrikara_%2819933880632%29.jpg";
//            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
//            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo;
//            case "Sunderland AFC" : return R.drawable.sunderland;
//            case "Stoke City FC" : return R.drawable.stoke_city;
            default: return "https://upload.wikimedia.org/wikipedia/commons/7/76/No_match_today_-_geograph.org.uk_-_1521127.jpg";
        }
    }
}
