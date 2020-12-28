package com.example.baitap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AddFragment.OnClickAddBtn {

    AddFragment addFragment;
    EmptyFragment emptyFragment;
    InfoFragment infoFragment;

    ArrayAdapter<String> adapter;
    List<String> items;

    int initialHeight;
    int itemSelectedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emptyFragment = EmptyFragment.newInstance("", "");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.add, emptyFragment);
        ft.addToBackStack("Empty");
        ft.commit();


        ListView listItems = findViewById(R.id.list_items);

        registerForContextMenu(listItems);
        listItems.setLongClickable(true);

        items = new ArrayList<>();

        SQLiteDatabase db = this.openOrCreateDatabase("sinhvienDB", MODE_PRIVATE, null);
        db.execSQL("create table if not exists sinhvien (mssv text PRIMARY KEY, name text, birth date, email text, address text)");
        Cursor c1 = db.rawQuery("select * from sinhvien", null);
        c1.moveToPosition(-1);
        while ( c1.moveToNext() ){
            String name = c1.getString(1);

            items.add(name);
        }
        db.close();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listItems.setAdapter(adapter);
        listItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                SQLiteDatabase db = openOrCreateDatabase("sinhvienDB", MODE_PRIVATE, null);
                Cursor c1 = db.rawQuery("select * from sinhvien", null);
                c1.moveToPosition(i);
                String mssv = c1.getString(0);
                String name = c1.getString(1);
                String birth = c1.getString(2);
                String email = c1.getString(3);
                String address = c1.getString(4);

                initialHeight = findViewById(R.id.list_items).getLayoutParams().height;
                findViewById(R.id.list_items).getLayoutParams().height = 0;
                infoFragment = InfoFragment.newInstance(mssv, name, birth, email, address);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.add, infoFragment);
                ft.addToBackStack("Info");
                ft.commit();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; add items to the action bar
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            initialHeight = findViewById(R.id.list_items).getLayoutParams().height;
            findViewById(R.id.list_items).getLayoutParams().height = 0;
            addFragment = AddFragment.newInstance("", "");
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.add, addFragment);
            ft.addToBackStack("Add_UI");
            ft.commit();
            return true;
        }

        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        super.onContextItemSelected(item);
        if (item.getItemId() == R.id.delete) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            itemSelectedPosition = info.position;
            showMyAlertDialog(this);
        }
        return true;
    }

    private void showMyAlertDialog(MainActivity mainActivity) {
        new AlertDialog.Builder(mainActivity)
                .setTitle("Xác nhận")
                .setMessage("Thông tin về sinh viên này sẽ bị xóa vĩnh viễn.")
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                items.clear();

                                SQLiteDatabase db = openOrCreateDatabase("sinhvienDB", MODE_PRIVATE, null);
                                Cursor c1 = db.rawQuery("select * from sinhvien", null);
                                c1.moveToPosition(itemSelectedPosition);
                                db.execSQL("delete from sinhvien where mssv = '" + c1.getString(c1.getColumnIndex("mssv")) + "';");
                                c1 = db.rawQuery("select * from sinhvien", null);
                                c1.moveToPosition(-1);
                                while ( c1.moveToNext() ){
                                    String name1 = c1.getString(1);

                                    items.add(name1);
                                }
                                db.close();
                                adapter.notifyDataSetChanged();
                            }
                        })
                .setNeutralButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        findViewById(R.id.list_items).getLayoutParams().height = initialHeight;
    }

    public void addItemSQl(String mssv, String name, String birth, String email, String address) {
        items.clear();

        SQLiteDatabase db = this.openOrCreateDatabase("sinhvienDB", MODE_PRIVATE, null);
        db.execSQL("insert into sinhvien(mssv, name, birth, email, address) values ('" + mssv + "', '" + name + "', '" + birth + "', '" + email + "', '" + address + "');");
        Cursor c1 = db.rawQuery("select * from sinhvien", null);
        c1.moveToPosition(-1);
        while ( c1.moveToNext() ){
            String name1 = c1.getString(1);

            items.add(name1);
        }
        db.close();
        adapter.notifyDataSetChanged();
        
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.add);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(fragment);
        ft.commitNow();

        findViewById(R.id.list_items).getLayoutParams().height = initialHeight;
    }
}