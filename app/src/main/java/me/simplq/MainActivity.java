package me.simplq;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import me.simplq.pojo.Queue;

public class MainActivity extends AppCompatActivity {
    EditText txtQueueId;
    Button btnRefresh;
    private View mLayout;
    private static final int PERMISSION_REQUEST_SMS = 0;
    // Todo Remove the logs
    private static final String TAG = "TO_REMOVE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(android.R.id.content);

        txtQueueId = (EditText) findViewById(R.id.txtQueueId);
        btnRefresh = (Button) findViewById(R.id.btnEnableSms);

        btnRefresh.setOnClickListener(v -> BackendService.enqueueWork(this, BackendService.class, BackendService.FETCH_QUEUES_JOB_ID, new Intent()));

        List<Queue> queues = (ArrayList<Queue>) getIntent().getSerializableExtra("QUEUES");
        if (queues != null) {
            String queueList = "";
            for (int i = 0; i < queues.size(); i++) {
                queueList.concat(queues.get(i).getName() + " ");
            }
            Snackbar.make(mLayout, queueList, Snackbar.LENGTH_SHORT).show();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available
            // TODO: Show status on the app
        } else {
            // Permission is missing and must be requested.
            requestSmsPermission();
        }
    }

    private void requestSmsPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.SEND_SMS)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.
            Snackbar.make(mLayout, R.string.sms_access_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS},
                            PERMISSION_REQUEST_SMS);
                }
            }).show();

        } else {
            Snackbar.make(mLayout, R.string.sms_unavailable, Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_SMS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                Snackbar.make(mLayout, R.string.sms_permission_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                // Permission request was denied.
                Snackbar.make(mLayout, R.string.sms_unavailable,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public void registerQueue(String queueId) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        Log.d(TAG, token);
                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}