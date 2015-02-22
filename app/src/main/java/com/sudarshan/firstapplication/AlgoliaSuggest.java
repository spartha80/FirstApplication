package com.sudarshan.firstapplication;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


public class AlgoliaSuggest extends Activity {
    private static final String LOG_TAG = "ExampleApp";

    private static final String ALGOLIA_API_BASE = "https://GAOJ5MFTOY.algolia.io";
    private static final String ALGOLIA_API_VERSION = "/1";
    private static final String ALGOLIA_REST_API = "/indexes";
    private static final String ALGOLIA_INDEX_NAME = "/events";
    private static final String ALGOLIA_QUERY = "/query";
    private static final String ALGOLIA_API_KEY = "4550b77a076d8a3245612d27de74fcd3";
    private static final String ALGOLIA_API_ID = "GAOJ5MFTOY";

    /*
    private static final String[] COUNTRIES = new String[] {
            "Belgium", "France", "Italy", "Germany", "Spain"
    };
    */

    private ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            String charset = "UTF-8";
            StringBuilder sb = new StringBuilder(ALGOLIA_API_BASE + ALGOLIA_API_VERSION + ALGOLIA_REST_API+ALGOLIA_INDEX_NAME);
            sb.append("?query=");
            sb.append(URLEncoder.encode(input, charset));
            sb.append("&hitsPerPage=5");
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("X-Algolia-API-Key", ALGOLIA_API_KEY);
            conn.setRequestProperty("X-Algolia-Application-Id", ALGOLIA_API_ID);
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        }finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("hits");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("title"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_algolia_suggest);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.algolia_suggest, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

        private final Context context;
        private final int textViewResourceId;
        private ArrayList<String> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.context = context;
            this.textViewResourceId = textViewResourceId;
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};
            return filter;
        }


    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_algolia_suggest, container, false);
            return rootView;
        }

        @Override
        public void onViewCreated (View view, Bundle savedInstanceState) {
            AutoCompleteTextView textView = (AutoCompleteTextView)this.getActivity().
                    findViewById(R.id.autoCompleteTextView1);
            textView.setThreshold(1);
            /*
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),
            android.R.layout.simple_dropdown_item_1line, COUNTRIES);
            */
            textView.setAdapter((new PlacesAutoCompleteAdapter(view.getContext(),
                    android.R.layout.simple_dropdown_item_1line)));

        }
    }
}
