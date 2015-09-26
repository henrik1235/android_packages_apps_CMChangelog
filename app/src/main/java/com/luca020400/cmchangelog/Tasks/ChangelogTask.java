package com.luca020400.cmchangelog.Tasks;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.luca020400.cmchangelog.ChangelogAdapter;
import com.luca020400.cmchangelog.R;
import com.luca020400.cmchangelog.activities.MainActivity;
import com.luca020400.cmchangelog.activities.MainActivity.Change;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class ChangelogTask extends AsyncTask<String, String, String> {
    ArrayList<String> mId = new ArrayList<>();
    ChangelogAdapter adapter;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (adapter != null) {
            adapter.clear();
        }
        MainActivity.getInstance().swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    protected String doInBackground(String... urls) {
        ArrayList<Change> arrayOflog = new ArrayList<>();
        adapter = new ChangelogAdapter(MainActivity._instance, arrayOflog);
        try {
            String out = new Scanner(new URL(urls[0]).openStream(), "UTF-8").useDelimiter("\\A").next();
            JSONArray newJArray = new JSONArray(out);

            for (int i = 0; i < newJArray.length(); ++i) {
                JSONObject jsonObject = newJArray.getJSONObject(i);

                String msg_project = (String) jsonObject.get("project");
                String msg_last_updated = (String) jsonObject.get("last_updated");
                Integer msg_id = (Integer) jsonObject.get("id");
                String msg_subject = (String) jsonObject.get("subject");

                Change newChange = new Change(msg_subject, msg_project, msg_last_updated);
                adapter.add(newChange);

                mId.add(msg_id.toString());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String urls) {
        super.onPostExecute(urls);

        final MainActivity mainActivity = MainActivity.getInstance();
        GridView gridview = (GridView) mainActivity.findViewById(R.id.gridview);

        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                String review_url = String.format
                        ("http://review.cyanogenmod.org/#/c/%s", mId.get(position));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(review_url));
                mainActivity.startActivity(browserIntent);
            }
        });

        mainActivity.swipeRefreshLayout.setRefreshing(false);
    }
}