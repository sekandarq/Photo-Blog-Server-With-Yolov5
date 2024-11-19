package com.example.androidclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    ImageView imgView;
    TextView textView;
    RecyclerView recyclerView;
    String site_url = "http://10.0.2.2:8000";
    JSONObject post_json;
    String imageUrl = null;
    Bitmap bmImg = null;

    CloadImage taskDownload;
    PutPost taskUpload;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = findViewById(R.id.imageViewItem);
        textView = findViewById(R.id.textView);
        recyclerView = findViewById(R.id.recyclerView); //added
    }
    // called when the "Download" button is clicked
    public void onClickDownload(View v) {
        if (taskDownload != null && taskDownload.getStatus() == AsyncTask.Status.RUNNING) {
            taskDownload.cancel(true);
        }
        taskDownload = new CloadImage();
        taskDownload.execute(site_url + "/api_root/Post/");
        Toast.makeText(getApplicationContext(), "Downloading...", Toast.LENGTH_LONG).show();
    }
    // called when the "Upload" button is clicked
    public void onClickUpload(View v) {
        // add the code here
        if (taskUpload != null && taskUpload.getStatus() == AsyncTask.Status.RUNNING) {
            taskUpload.cancel(true);
        }
        taskUpload = new PutPost();
        taskUpload.execute(site_url + "/api_root/Post/");
        Toast.makeText(getApplicationContext(), "Uploading...", Toast.LENGTH_LONG).show();
    }
    // AsyncTask to download images from the server
    private class CloadImage extends AsyncTask<String, Integer, List<Bitmap>> {
        @Override
        protected List<Bitmap> doInBackground(String... urls) {
            List<Bitmap> bitmapList = new ArrayList<>();
            HttpURLConnection conn = null; //newly added
            try {
                String apiUrl = urls[0];
                String token = "10c430bf714fa2408ae8662497a1041c672bb7dd";
                URL urlAPI = new URL(apiUrl);
                // HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection(); //original code
                conn = (HttpURLConnection) urlAPI.openConnection(); // newly added
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    is.close();

                    String strJson = result.toString();
                    JSONArray aryJson = new JSONArray(strJson);

                    // 배열 내 모든 이미지 다운로드
                    for (int i = 0; i < aryJson.length(); i++) {
                        post_json = (JSONObject) aryJson.get(i);
                        imageUrl = post_json.getString("image");
                        if (!imageUrl.equals("")) {
                            URL myImageUrl = new URL(imageUrl);
                            //conn = (HttpURLConnection) myImageUrl.openConnection(); //original code
                            HttpURLConnection imageConn = (HttpURLConnection) myImageUrl.openConnection();
                            InputStream imgStream = imageConn.getInputStream();

                            bmImg = BitmapFactory.decodeStream(imgStream);
                            bitmapList.add(bmImg); // 이미지 리스트에 추가
                            imgStream.close();
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return bitmapList;
        }

        @Override
        protected void onPostExecute(List<Bitmap> images) {
            if (images.isEmpty()) {
                textView.setText("There are no images to import");
                Toast.makeText(MainActivity.this, "Download failed or no images found.", Toast.LENGTH_SHORT).show();
            } else {
                textView.setText("Succeeded in loading the image");
                //RecyclerView recyclerView = findViewById(R.id.recyclerView);
                ImageAdapter adapter = new ImageAdapter(images);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(adapter);
            }
        }
    }

    // AsyncTask to upload a new post to the server
    private class PutPost extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            HttpURLConnection conn = null; // new code
            try {
                String apiUrl = urls[0];
                String token = "10c430bf714fa2408ae8662497a1041c672bb7dd"; // Replace with your API token
                URL urlAPI = new URL(apiUrl);
                // HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection(); //original code
                conn = (HttpURLConnection) urlAPI.openConnection(); // new code
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                // Sample JSON payload
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("title", "Sample Post");
                jsonObject.put("text", "This is a sample post description");
                jsonObject.put("image", "http://example.com/sample-image.jpg");

                OutputStream os = conn.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(os);
                writer.write(jsonObject.toString());
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    return "Upload successful!";
                } else {
                    return "Failed to upload: " + responseCode;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return "Error occurred during upload";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            textView.setText(result);
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
        }
    }
}
