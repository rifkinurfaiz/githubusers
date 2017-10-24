package com.example.rifkinurfaiz.githubusers;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String URL = "https://api.github.com/search/users?q=";
    private static final String page = "&page=";
    String keyword = "";
    int pageAt = 1;
    int allDataLength;
    int status = 1; //1 data okay, normal view; 2 data not found; 3 limit exeed; 4 poor internet connection

    Toolbar mToolbar;
    EditText searchBar;
    LinearLayout layoutWallpaper;
    LinearLayout layoutNoData;
    LinearLayout layoutLimitExeed;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    List<ListItem> listItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        searchBar = (EditText) findViewById(R.id.searchBar);
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    if(haveNetworkConnection()) {
                        keyword = searchBar.getText().toString();
                        if(keyword.length() > 0) {
                            pageAt = 1;
                            loadRecyclerViewData();
                        }
                        return true;
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "You don't have internet acces!", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN)) {
                        if(listItems.size() < allDataLength) {
                            pageAt++;
                            loadMore();
                        }
                    }
                }
            }
        });
    }

    // method to set background depending on API response
    public void setBackground() {
        // data ok, view normal
        if(status == 1) {
            layoutWallpaper.setVisibility(View.VISIBLE);
            layoutNoData.setVisibility(View.GONE);
        }
        // data not found
        else if(status == 2) {
            layoutWallpaper.setVisibility(View.GONE);
            layoutNoData.setVisibility(View.VISIBLE);
        }
        // limit exeed
        else if(status == 3) {
            Toast.makeText(getApplicationContext(), "Limit exeed, try again in 60 second", Toast.LENGTH_SHORT).show();
        }
        // poor internet connection
        else if(status == 4) {
            Toast.makeText(getApplicationContext(), "Your internet connection is poor", Toast.LENGTH_SHORT).show();
        }
    }

    // method to load data when user click search button
    public void loadRecyclerViewData() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        layoutWallpaper = (LinearLayout) findViewById(R.id.layoutWallpaper);
        layoutNoData = (LinearLayout) findViewById(R.id.linearLayoutNoData);
        layoutLimitExeed = (LinearLayout) findViewById(R.id.linearLayoutLimitExeed);

        // set background to normal
        status = 1;
        setBackground();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL + keyword,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        listItems.removeAll(listItems);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            allDataLength = jsonObject.getInt("total_count");
                            JSONArray jsonArray = jsonObject.getJSONArray("items");

                            Log.d("Total Data Count", "" + allDataLength);
                            Log.d("JSON ARRAY", "" + jsonArray);

                            for(int i = 0; i < jsonArray.length(); i++) {
                                JSONObject o = jsonArray.getJSONObject(i);
                                ListItem listItem = new ListItem(
                                        o.getString("login"),
                                        o.getString("avatar_url")
                                );
                                listItems.add(listItem);
                            }

                            // not found
                            if(allDataLength == 0) {
                                status = 2;
                                setBackground();
                            }
                            adapter = new RecyclerViewAdapter(getApplicationContext(), listItems);
                            recyclerView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        NetworkResponse networkResponse = error.networkResponse;
                        if(networkResponse.statusCode == 403) {
                            // limit exeed
                            status = 3;
                            setBackground();
                        }
                        else {
                            // poor internet connection
                            status = 4;
                            setBackground();
                        }
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    // method to load data when user scroll at result of a search
    public void loadMore() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        layoutWallpaper = (LinearLayout) findViewById(R.id.layoutWallpaper);
        layoutNoData = (LinearLayout) findViewById(R.id.linearLayoutNoData);
        layoutLimitExeed = (LinearLayout) findViewById(R.id.linearLayoutLimitExeed);

        // Set background to normal
        status = 1;
        setBackground();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL + keyword + page + pageAt,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            allDataLength = jsonObject.getInt("total_count");
                            JSONArray jsonArray = jsonObject.getJSONArray("items");

                            Log.d("Total Data Count", "" + allDataLength);
                            Log.d("JSON ARRAY", "" + jsonArray);

                            for(int i = 0; i < jsonArray.length(); i++) {
                                JSONObject o = jsonArray.getJSONObject(i);
                                ListItem listItem = new ListItem(
                                        o.getString("login"),
                                        o.getString("avatar_url")
                                );
                                listItems.add(listItem);
                            }

                            // not found
                            if(allDataLength == 0) {
                                status = 2;
                                setBackground();
                            }
                            adapter = new RecyclerViewAdapter(getApplicationContext(), listItems);
                            recyclerView.setAdapter(adapter);
                            int position = recyclerView.getAdapter().getItemCount()-40;
                            recyclerView.scrollToPosition(position);
                            adapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        NetworkResponse networkResponse = error.networkResponse;
                        if(networkResponse.statusCode == 403) {
                            // limit exeed
                            status = 3;
                            setBackground();
                        }
                        else {
                            // poor internet connection
                            status = 4;
                            setBackground();
                        }
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    // Method to check weather device connected to internet or not
    private boolean haveNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null) {
            return true;
        }
        else {
            return false;
        }
    }
}