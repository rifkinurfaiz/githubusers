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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    Toolbar mToolbar;
    EditText searchBar;
    LinearLayout layoutWallpaper;
    LinearLayout layoutNoData;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    List<ListItem> listItems;

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
                        loadRecyclerViewData(searchBar.getText().toString());
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

        listItems = new ArrayList<>();
    }

    public void loadRecyclerViewData(String key) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        layoutWallpaper = (LinearLayout) findViewById(R.id.layoutWallpaper);
        layoutNoData = (LinearLayout) findViewById(R.id.linearLayoutNoData);

        layoutWallpaper.setVisibility(View.VISIBLE);
        layoutNoData.setVisibility(View.GONE);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL + key,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        listItems.removeAll(listItems);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String dataLength = jsonObject.getString("total_count");
                            JSONArray jsonArray = jsonObject.getJSONArray("items");

                            Log.d("Total Data Count", dataLength);
                            Log.d("JSON ARRAY", "" + jsonArray);

                            for(int i = 0; i < jsonArray.length(); i++) {
                                JSONObject o = jsonArray.getJSONObject(i);
                                ListItem listItem = new ListItem(
                                        o.getString("login"),
                                        o.getString("avatar_url")
                                );
                                listItems.add(listItem);
                            }

                            if(Integer.parseInt(dataLength) == 0) {
                                layoutWallpaper.setVisibility(View.GONE);
                                layoutNoData.setVisibility(View.VISIBLE);
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
                        Log.d("ERROR", "BUGS");
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

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
