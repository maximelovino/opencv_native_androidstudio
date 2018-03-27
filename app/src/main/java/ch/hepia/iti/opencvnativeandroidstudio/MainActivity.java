package ch.hepia.iti.opencvnativeandroidstudio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button edgeDetectionBtn = findViewById(R.id.edge_detect_btn);
        Button colorDetectionBtn = findViewById(R.id.color_detect_btn);
        Button faceDetectionBtn = findViewById(R.id.face_detect_btn);
        Button examBtn = findViewById(R.id.exam_activity_btn);

        edgeDetectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,EdgeDetectionActivity.class));
            }
        });

        colorDetectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ColorDetectionActivity.class));
            }
        });

        faceDetectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,FaceDetectionActivity.class));
            }
        });

        examBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ExamActivity.class));
            }
        });


    }
}
