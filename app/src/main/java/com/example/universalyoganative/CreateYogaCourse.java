package com.example.universalyoganative;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateYogaCourse extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_yoga_course);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onClickCreateYogaCourse(View v) {
        String dayOfWeek, time, type, des;
        float price;
        Spinner sp;

        sp = (Spinner) findViewById(R.id.spDayOfWeek);
        dayOfWeek = sp.getSelectedItem().toString();
        sp = (Spinner) findViewById(R.id.spTime);
        time = sp.getSelectedItem().toString();
        sp = (Spinner)findViewById(R.id.spType);
        type = sp.getSelectedItem().toString();
        des = ((EditText)findViewById(R.id.edmDes)).getText().toString();
        price = Float.valueOf(((EditText)findViewById(R.id.edPrice)).getText().toString());
        MainActivity.helper.createNewYogaCourse(dayOfWeek, time, price, type, des);
        Toast.makeText(this, "A Yoga class is just created", Toast.LENGTH_LONG).show();
    }
}