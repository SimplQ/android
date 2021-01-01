package me.simplq;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.simplq.pojo.Queue;

public class BackendService extends JobIntentService {
    private static final String BASE_URL = "https://devbackend.simplq.me/v1";
    private RequestQueue requestQueue;
    private static final String TAG = "BackendService";
    public static final int FETCH_QUEUES_JOB_ID = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        requestQueue = Volley.newRequestQueue(this);
    }

    public void fetchQueues() {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, BASE_URL + "/queues", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.wtf("The Response ", response.toString());
                ArrayList<Queue> queues = new ArrayList<Queue>();
                try {
                    JSONArray jsonQueues = response.getJSONArray("queues");
                    for (int i = 0; i < jsonQueues.length(); i++) {
                        JSONObject queue = jsonQueues.getJSONObject(i);
                        queues.add(new Queue(queue.getString("queueName"), queue.getString("queueId")));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    queues.add(new Queue("json-parse-error", "json-parse-error"));
                }
                Intent update = new Intent(getBaseContext(), MainActivity.class);
                update.putExtra("QUEUES", queues);
                startActivity(update);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer anonymous");
                return params;
            }
        };

        requestQueue.add(request);
    }

    @Override
    protected void onHandleWork(@NonNull Intent workIntent) {
        fetchQueues();
    }
}
