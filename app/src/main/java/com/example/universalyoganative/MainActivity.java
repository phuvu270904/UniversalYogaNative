package com.example.universalyoganative;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    public static DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        helper = new DatabaseHelper(getApplicationContext());
    }

    public void onCreateYogaCourse(View v) {
        Intent i = new Intent(getApplicationContext(), CreateYogaCourse.class);
        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Cursor c = helper.readAllYogaCourse();
        YogaCourseCursorAdapter adapter = new YogaCourseCursorAdapter(this, R.layout.yoga_course_item, c, 0);
        ListView lv = (ListView) findViewById(R.id.listview);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterview, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "Clicked", Toast.LENGTH_LONG).show();
            }
        });
    }
}

class YogaCourseCursorAdapter extends ResourceCursorAdapter {
    public YogaCourseCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
    }
    @SuppressLint("Range")
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView dow = (TextView) view.findViewById(R.id.tvDow);
        TextView type = (TextView) view.findViewById(R.id.tvType);
        TextView time = (TextView) view.findViewById(R.id.tvTime);
        TextView des = (TextView) view.findViewById(R.id.tvDes);
        TextView price = (TextView) view.findViewById(R.id.tvPrice);

        dow.setText(cursor.getString(cursor.getColumnIndex("dayofweek")));
        type.setText(cursor.getString(cursor.getColumnIndex("type")));
        time.setText(cursor.getString(cursor.getColumnIndex("time")));
        des.setText(cursor.getString(cursor.getColumnIndex("description")));
        price.setText(cursor.getString(cursor.getColumnIndex("price")));
    }
}