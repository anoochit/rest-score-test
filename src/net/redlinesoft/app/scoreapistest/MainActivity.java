package net.redlinesoft.app.scoreapistest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.database.CursorJoiner.Result;
import android.util.Log;
import android.view.Menu;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity {

	public String APP_KEY = "50a6fbf03d06e50a6fbf03d106";
	public String REST_URL = "http://192.168.0.102/redmobi/score/score/leaderboard.json";
	
	// Hashmap for ListView
    ArrayList<HashMap<String, String>> scoreList = new ArrayList<HashMap<String, String>>();

	PostTask posttask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		posttask = new PostTask();
		posttask.execute();
		 
		Handler handler = new Handler();
		Runnable runnable = new Runnable() {         
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Log.d("APP", "HashMap Size = " + String.valueOf(scoreList.size()));
				
				ListAdapter adapter = new SimpleAdapter(getBaseContext(), scoreList, R.layout.list_item,
						new String[] { "name", "score", }, 
						new int[] { R.id.textView1, R.id.textView2}); 
				
				ListView lv =  (ListView) findViewById(R.id.listView1); 
				 lv.setAdapter(adapter); 
				
			}
		};
		handler.postDelayed(runnable, 3000);  
 
	}


	public class PostTask extends AsyncTask<Void, String, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = false;

			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(REST_URL);
			StringBuilder str = new StringBuilder();
			 
			try {
				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("appkey", APP_KEY));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				StatusLine statusLine = response.getStatusLine();

				int statusCode = statusLine.getStatusCode();
				Log.d("APP", "Status = " + String.valueOf(statusCode));
				if (statusCode == 200) { // Status OK
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));
					String line;
					while ((line = reader.readLine()) != null) {
						str.append(line);
					}
					Log.d("APP", str.toString());

					JSONObject json;

					try {
						json = new JSONObject(str.toString());

						// Getting Array of Contacts
						JSONObject data0 = json.getJSONObject("data");
						JSONArray data1 = data0.optJSONArray("data");
						Log.d("APP",
								"Array size = "
										+ String.valueOf(data1.length())); 

						for (int i = 0; i < data1.length(); i++) {
							JSONObject data2 = data1.getJSONObject(i);
							JSONObject data3 = data2.getJSONObject("0");
							JSONObject data4 = data2.getJSONObject("Score");
							
							Log.d("APP",	"DATA " + i + " = " + data4.getString("name") + " -> " + data3.getString("score"));
							
							// creating new HashMap
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("name", data4.getString("name"));
							map.put("score", data3.getString("score"));
							scoreList.add(map);
						
							//Log.d("APP",	"DATA " + i + " " + data3.getString("name") + " " + data3.getString("score") ); 
						}
						
						result = true;

					} catch (JSONException e) {
						e.printStackTrace();
						Log.d("APP", e.getMessage());
						result = false;
					}

				} else {
					Log.e("APP", "Failed to download result..");
					result = false;
				}

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				Log.d("APP", e.getMessage());
				result = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d("APP", e.getMessage());
				result = false;
			}

			// If you want to do something on the UI use progress update
			publishProgress("progress");
			return result;
		}

		protected void onProgressUpdate(String... progress) {
			StringBuilder str = new StringBuilder();
			for (int i = 1; i < progress.length; i++) {
				str.append(progress[i] + " ");
			}
		}
	}

}
