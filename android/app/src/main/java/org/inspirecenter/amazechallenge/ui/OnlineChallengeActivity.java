package org.inspirecenter.amazechallenge.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.inspirecenter.amazechallenge.R;
import org.inspirecenter.amazechallenge.api.ChallengesReply;
import org.inspirecenter.amazechallenge.api.JsonParser;
import org.inspirecenter.amazechallenge.model.Challenge;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import static org.inspirecenter.amazechallenge.ui.PersonalizeActivity.PREFERENCE_KEY_EMAIL;
import static org.inspirecenter.amazechallenge.ui.TrainingActivity.CHALLENGES_PATH;

public class OnlineChallengeActivity extends AppCompatActivity implements ChallengeAdapter.OnChallengeSelectedListener {

    public static final String TAG = "aMazeChallenge";

    private ProgressBar progressBar;
    private RecyclerView challengesRecyclerView;
    private ChallengeAdapter challengeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_challenge);

        final ActionBar actionBar = getActionBar();
        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.activity_online_challenge_progress_bar);

        challengesRecyclerView = findViewById(R.id.activity_online_challenge_list_view);
        challengesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        challengesRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        challengeAdapter = new ChallengeAdapter(this);
        challengesRecyclerView.setAdapter(challengeAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // start online request
        new FetchChallengesAsyncTask().execute();
    }

    @Override
    public void onChallengeSelected(final Challenge challenge) {
        Snackbar.make(findViewById(R.id.activity_online_challenge), "Joining " + challenge.getName() + " ...", Snackbar.LENGTH_SHORT).show();

        final String email = PreferenceManager.getDefaultSharedPreferences(this).getString(PREFERENCE_KEY_EMAIL, "");
        new JoinChallengeAsyncTask(email).execute(challenge.getId());
    }

    private class FetchChallengesAsyncTask extends AsyncTask<Void, Void, ChallengesReply> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            challengesRecyclerView.setVisibility(View.GONE);
        }

        @Override
        protected ChallengesReply doInBackground(Void... v) {
            final Vector<Challenge> challenges = new Vector<>();
            try {
                final String apiUrlBase = getString(R.string.api_url);
                final URL apiURL = new URL(apiUrlBase + "/challenges");
                final InputStream inputStream = apiURL.openStream();
                final String json = convertStreamToString(inputStream);
                return JsonParser.parseChallengesMessage(json);
            } catch (IOException | JSONException e) {
                // show message in snackbar
                Snackbar.make(findViewById(R.id.activity_online_challenge), "Error while joining challenge: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                // log error
                Log.e("challenges", "Error: " + e.getMessage());
                return new ChallengesReply("error", new String [] { e.getMessage()}, null);
            }
       }

        @Override
        protected void onPostExecute(final ChallengesReply challengesReply) {
            super.onPostExecute(challengesReply);
            progressBar.setVisibility(View.GONE);
            if(challengesReply.isOk()) {
                challengeAdapter.addAll(challengesReply.getChallenges());
                challengesRecyclerView.setVisibility(View.VISIBLE);
            } else {
                // show message in snackbar
                Snackbar.make(findViewById(R.id.activity_online_challenge), "Reply: " + challengesReply, Snackbar.LENGTH_SHORT).show();
                // also log warning
                Log.w(TAG, challengesReply.toString());
            }
        }
    }

    private class JoinChallengeAsyncTask extends AsyncTask<Long, Void, String> {

        private final String email;

        JoinChallengeAsyncTask(final String email) {
            this.email = email;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(final Long... challengeId) {
            try {
                final String apiUrlBase = getString(R.string.api_url);
                final String magic = getString(R.string.magic);
                final URL apiURL = new URL(apiUrlBase + "/join?magic=" + magic + "&email=" + email + "&id=" + challengeId[0]);
                final InputStream inputStream = apiURL.openStream();
                return convertStreamToString(inputStream);
            } catch (IOException e) {
                // show message in snackbar
                Snackbar.make(findViewById(R.id.activity_online_challenge), "Error while joining challenge: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                // log error
                Log.e("challenges", "Error: " + Arrays.toString(e.getStackTrace()));
                return "Error: " + Arrays.toString(e.getStackTrace());
            }
        }

        @Override
        protected void onPostExecute(final String reply) {
            super.onPostExecute(reply);
            progressBar.setVisibility(View.GONE);
            // todo
            Snackbar.make(findViewById(R.id.activity_online_challenge), "Joined \n" + reply, Snackbar.LENGTH_SHORT).show();
        }
    }

    public static String convertStreamToString(final InputStream inputStream) {
        final Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
}