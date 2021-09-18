package com.filemonitor.prueba;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.filemonitor.prueba.apkList.Activity_apklist;
import com.filemonitor.prueba.fileobserver.FileObserverService;
import com.filemonitor.prueba.fileobserver.eventAdapter;
import com.filemonitor.prueba.fileobserver.eventData;
import com.filemonitor.prueba.fileobserver.typeofevents;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    eventAdapter adapter;


    // Recibe informacion del servicio
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           // Toast.makeText(getApplicationContext(),"Recibiendo datos",Toast.LENGTH_LONG).show();
            if(intent.getAction().equals("GET_EVENT_DATA"))
            {

                eventData event = (eventData) intent.getSerializableExtra("event");

                if (event == null){
                    Toast.makeText(getApplicationContext(),"Recibiendo Null events",Toast.LENGTH_LONG).show();
                }
                else {
                    //Toast.makeText(getApplicationContext(), "Recibiendo Event" + events.get(0).event_path, Toast.LENGTH_LONG).show();
                    adapter.addEvent(event);
                }


            }
        }
    } ;


    @Override
    protected void onStart() {
        super.onStart();
        requestpermissions();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("GET_EVENT_DATA"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("GET_EVENT_DATA"));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Permite acciones cuando un objeto es deslizado
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {


                int position = viewHolder.getAdapterPosition();
                adapter.removeItem(position);

            }
        };

        RecyclerView list = findViewById(R.id.detailed_list);
        adapter = new eventAdapter(getApplicationContext());
        list.setAdapter(adapter);
        list.setLayoutManager(new GridLayoutManager(this,1));
        list.addItemDecoration(new SpacesItemDecoration(5));
        list.setItemAnimator(new DefaultItemAnimator());

        // Removes an item on swipe
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(list);


        /////////////////// Buttons

        ImageButton start = findViewById(R.id.start);
        ImageButton test = findViewById(R.id.test);
        ImageButton stop = findViewById(R.id.stop);
        ImageButton clean = findViewById(R.id.clear_list);
        ImageButton filter_btn = findViewById(R.id.filter_btn);
        ImageButton app_btn = findViewById(R.id.applist_btn);

        TooltipCompat.setTooltipText(start,"Start");
        TooltipCompat.setTooltipText(stop,"Stop");
        TooltipCompat.setTooltipText(clean,"Clean all displayed data");
        TooltipCompat.setTooltipText(test,"Test");
        TooltipCompat.setTooltipText(filter_btn,"Filter by event type");
        TooltipCompat.setTooltipText(app_btn,"Display list of installed apps on the device");


        start.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(MainActivity.this, FileObserverService.class);
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(intent);
                }
                else {
                    startService(intent);
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                stopService(new Intent(MainActivity.this, FileObserverService.class));
            }
        });

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
               generateNoteOnSD(getApplicationContext(),"prueba.txt","esto es una prueba");

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        deleteNote();
                    }
                }, 4000);

            }
        });

        clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.deleteList();
            }
        });


        filter_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this,filter_btn);


                ArrayList<Pair<typeofevents,Boolean>> filter = adapter.getFilter();

                for (Pair<typeofevents,Boolean> p: filter) {

                    popup.getMenu().add(p.first.toString()).setTitle(p.first.toString()).setCheckable(true).setChecked(p.second);

                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                        item.setActionView(new View(getApplicationContext()));
                       // Log.i("Filter set", new Pair<String,Boolean>(item.getTitle().toString(),!item.isChecked()).toString());
                        adapter.setFilter(new Pair<String,Boolean>(item.getTitle().toString(),!item.isChecked()));
                        item.setChecked(!item.isChecked());

                        popup.show();
                        return false;
                    }

                });



                popup.show();
            }
        });

        app_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Activity_apklist.class);
                startActivity(intent);
            }
        });




    }



    public void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteNote(){

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Notes/prueba.txt");
        file.delete();
    }

    public void requestpermissions() {

        String[] Perm = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //Log.i("Has read permissions?", String.valueOf(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)));
        //Log.i("Has write permissions?", String.valueOf(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) );

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, Perm, 1);
        }
    }

}

