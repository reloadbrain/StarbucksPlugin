package edu.cmu.chimps.starbucksplugin;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;

public class StarbucksSettingActivity extends AppCompatActivity {
    public static String TAG = "StarbucksActivity";
    private static String Result;
    Toolbar toolbar;
    ScriptAdapter adapter;
    RecyclerView recyclerView;
    private int BackPressedCount;
    public static FlagChangeListener listener;

    public static void setFlagChangeListener(FlagChangeListener icl) {
         listener = icl;
    }

    @Override
    public void onBackPressed() {

        if (BackPressedCount == 0) {
            Toast.makeText(StarbucksSettingActivity.this, "Click again to cancel the change", Toast.LENGTH_SHORT).show();
            BackPressedCount++;
        } else if (BackPressedCount == 1) {
            Toast.makeText(StarbucksSettingActivity.this, "Change canceled", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case 1:
                if (resultCode == RESULT_OK && data != null) {
                    Result = data.getStringExtra("result");
                    //ArrayList<String> ResultArray = (ArrayList<String>) JSONUtils.jsonToSimpleObject(Result,JSONUtils.TYPE_TAG_ARRAY);
                    Log.e(TAG, "onResult:" + Result);

                    ArrayList<String> result = rehandledResultArrayList(Result);
                    Script.scriptList.clear();
                    for (String str : result){
                        Script script = new Script(str);
                        Script.scriptList.add(script);
                    }
                    adapter.notifyDataSetChanged();
                    Log.e(TAG, "onActivityResult: " + result.toString());
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.recyclerview), "Scripts have been updated", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                break;
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_select);

        //Initialize ScriptList
        Intent sugiliteIntent = new Intent("edu.cmu.hcii.sugilite.COMMUNICATION");
        sugiliteIntent.addCategory("android.intent.category.DEFAULT");
        sugiliteIntent.putExtra("messageType", "GET_SCRIPT_LIST");
        startActivityForResult(sugiliteIntent, 1);

        setFlagChangeListener(new FlagChangeListener() {
            @Override
            public void onChange(Boolean wantChange) {
                if (wantChange) ScriptAdapter.SetAllSelection(recyclerView);
            }
        });

        //Initialize UI
        setContentView(R.layout.activity_contact_select);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        //StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.colorPrimary), true);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setTitle("Select Script");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorwhite));
        toolbar.inflateMenu(R.menu.updatescript);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getBaseContext(), "Contacts Saved" , Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (Script.scriptList.isEmpty()) return false;
                int menuItemId = item.getItemId();
                switch (menuItemId) {
                    case R.id.updatescript:
                        Intent sugiliteIntent = new Intent("edu.cmu.hcii.sugilite.COMMUNICATION");
                        sugiliteIntent.addCategory("android.intent.category.DEFAULT");
                        sugiliteIntent.putExtra("messageType", "GET_SCRIPT_LIST");
                        startActivityForResult(sugiliteIntent, 1);
                        break;
                }
                return true;
            }
        });

        adapter = new ScriptAdapter(Script.scriptList, toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        FloatingActionButton floatingUndefinedButton = (FloatingActionButton) findViewById(R.id.floatingUndefinedAction);
        floatingUndefinedButton.setImageResource(R.drawable.ic_action_check);
        floatingUndefinedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(StarbucksSettingActivity.this, "Contacts saved", Toast.LENGTH_SHORT).show();
                ScriptStorage.storeScript(StarbucksSettingActivity.this, Script.getSelectedName());//if scriptName is empty, save "empty"
                Toast.makeText(StarbucksSettingActivity.this, "script saved", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent("edu.cmu.chimps.googledocsplugin.sendcontacts");
                intent.addCategory("sendcontacts");
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("contacts", Script.getSavedContactList());
                intent.putExtra("contacts", bundle);
                sendBroadcast(intent);
            }
        });        
    }
    

    protected ArrayList<String> rehandledResultArrayList(String json){
        ArrayList<String> result = new ArrayList<>();
        String nJson = json;
        nJson = nJson.substring(2);
        nJson = nJson.substring(0,nJson.length()-2);
        String[] nJsonString = nJson.split("\",\"");
        for (String i:nJsonString){
            result.add(i);
        }

        return result;
    }
}
