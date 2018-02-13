package ch.gibmit.straessle.dominik.memory_3;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends Activity {

    public static final String PREFS_NAME = "MEMORY_N";
    public static final String PREFS_KEY = "MEMORY_K";
    public static final String PREFS_PATH_NAME = "PATH_N";
    public static final String PREFS_PATH_KEY = "PATH_K";

    private ArrayList<Card> cards;
    private MyAdapter adapter;
    private int currentPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        cards = new ArrayList<>();
        try {
            loadJSON(true);
            loadJSON(false);
//            loadPathJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cards.add(new Card());
        adapter = new MyAdapter(cards, this);
        recyclerView.setAdapter(adapter);
    }

    //OptionsMenu für Set/Log/Save etc...
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add("Log");
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    //Log
                    logJSON(createJSON(true));
                } catch (JSONException e) {
                    e.printStackTrace();

                }
                return false;
            }
        });
        MenuItem menuItem2 = menu.add("Save");
        menuItem2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    saveJSON(createJSON(true), true);
                    saveJSON(createJSON(false), false);
//                    savePathJSON(createPathJSON());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    //laden beim start
    private void loadJSON(boolean solution) throws JSONException {
        String name = "";
        String key = "";
        if (solution) {
            name = PREFS_NAME;
            key = PREFS_KEY;
        } else {
            name = PREFS_PATH_NAME;
            key = PREFS_PATH_KEY;
        }
        SharedPreferences preferencesReader = getSharedPreferences(name, MainActivity.MODE_PRIVATE);
        String jsonString = preferencesReader.getString(key, null);
        if (jsonString == null) {
            Toast.makeText(this, "Keine Memorys", Toast.LENGTH_LONG).show();
            return;
        }
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray array = jsonObject.getJSONArray("solution");
        int pathIndex = 0;
        for (int i = 0; i < array.length(); i++) {
            JSONArray memory = (JSONArray) array.get(i);
            if (solution) {
                Card left = new Card();
                left.setText(memory.getString(0));
                Card right = new Card();
                right.setText(memory.getString(1));
                cards.add(left);
                cards.add(right);
            } else {
                Card left = cards.get(pathIndex);
                left.setImagePath(memory.getString(0));
                pathIndex++;
                Card right = cards.get(pathIndex);
                right.setImagePath(memory.getString(1));
                pathIndex++;
            }
        }
    }

//    private void loadPathJSON() throws JSONException {
//        SharedPreferences preferencesReader = getSharedPreferences(PREFS_PATH_NAME, MainActivity.MODE_PRIVATE);
//        String jsonString = preferencesReader.getString(PREFS_PATH_KEY, null);
//        if (jsonString == null) {
//            Toast.makeText(this, "Keine Memorys", Toast.LENGTH_LONG).show();
//            return;
//        }
//        JSONObject jsonObject = new JSONObject(jsonString);
//        JSONArray jsonArray = jsonObject.getJSONArray("array");
//        for (int i = 0; i < jsonArray.length(); i++) {
//            JSONArray memory = (JSONArray) jsonArray.get(i);
//            Card left = cards.get(i);
//            left.setImagePath(memory.getString(0));
//            Card right = cards.get(i + 1);
//            right.setImagePath(memory.getString(1));
//        }
//    }

    //speichern bei ondestroy und save
    private void saveJSON(JSONObject jsonObject, boolean solution) {
        if (jsonObject == null) {
            Toast.makeText(this, "Keine Memorys", Toast.LENGTH_LONG).show();
            return;
        }
        String name = "";
        String key = "";
        if (solution) {
            name = PREFS_NAME;
            key = PREFS_KEY;
        } else {
            name = PREFS_PATH_NAME;
            key = PREFS_PATH_KEY;
        }
        SharedPreferences preferencesReader = getSharedPreferences(name, MainActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencesReader.edit();
        editor.putString(key, jsonObject.toString());
        editor.apply();
    }

//    private JSONObject createPathJSON() throws JSONException {
//        JSONArray arrayPath = new JSONArray();
//        for (int i = 0; i < cards.size(); i += 2) {
//            if (i + 1 <= cards.size() - 1) {
//                JSONArray arraySolution = new JSONArray();
//                arraySolution.put(cards.get(i).getImagePath());
//                arraySolution.put(cards.get(i + 1).getImagePath());
//                arrayPath.put(arraySolution);
//            }
//        }
//        JSONObject object = new JSONObject();
//        object.put("array", arrayPath);
//        return object;
//    }

//    private void savePathJSON(JSONObject object) {
//        SharedPreferences preferencesReader = getSharedPreferences(PREFS_PATH_NAME, MainActivity.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferencesReader.edit();
//        editor.putString(PREFS_PATH_KEY, object.toString());
//        editor.apply();
//    }

    // erstelle JSON
    private JSONObject createJSON(boolean solution) throws JSONException {
        JSONArray arrayLog = new JSONArray();
        for (int i = 0; i < cards.size(); i += 2) {
            if (i + 1 <= cards.size() - 1) {
                JSONArray arraySolution = new JSONArray();
                if (solution) {
                    arraySolution.put(cards.get(i).getText());
                    arraySolution.put(cards.get(i + 1).getText());
                } else {
                    arraySolution.put(cards.get(i).getImagePath());
                    arraySolution.put(cards.get(i + 1).getImagePath());
                }
                arrayLog.put(arraySolution);
            }
        }
        JSONObject json = new JSONObject();
        json.put("task", "Memory");
        json.put("solution", arrayLog);
        return json;
    }

    //loggen
    private void logJSON(JSONObject jsonObject) {
        if (jsonObject == null) {
            Toast.makeText(this, "Keine Memorys", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent("ch.appquest.intent.LOG");

        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) { // -> Logbuch App nicht installiert.
            Toast.makeText(this, "Logbuch nicht installiert", Toast.LENGTH_LONG).show(); //Toast ist ein kleiner Aufgepoppert hinweis.
            return;
        }
        intent.putExtra("ch.appquest.logmessage", jsonObject.toString());

        startActivity(intent);
    }

    //speichere JSON
    @Override
    protected void onDestroy() {
        try {
            saveJSON(createJSON(true), true);
            saveJSON(createJSON(false), false);
//            savePathJSON(createPathJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    //Bild aufnehmen
    public void takeQrCodePicture() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        intentIntegrator.addExtra(Intents.Scan.BARCODE_IMAGE_ENABLED, true);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Abgebrochen", Toast.LENGTH_LONG).show();
            } else {
                String imagePath = result.getBarcodeImagePath();
                String text = result.getContents();
                Toast.makeText(this, "Lösung: " + text, Toast.LENGTH_LONG).show();
                adapter.setCard(text, imagePath, this.currentPosition);
                if (currentPosition % 2 == 0 && currentPosition == cards.size() - 1) {
                    cards.add(new Card());
                    cards.add(new Card());
                }
                adapter.notifyDataSetChanged();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }


}
