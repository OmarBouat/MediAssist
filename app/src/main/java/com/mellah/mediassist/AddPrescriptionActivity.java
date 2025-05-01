package com.mellah.mediassist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AddPrescriptionActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 100;

    private ImageView ivPrescription;
    private EditText etDescription;
    private Button btnChooseImage, btnSave;
    private Uri selectedImageUri;
    private MediAssistDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_prescription);

        ivPrescription = findViewById(R.id.ivPrescription);
        etDescription  = findViewById(R.id.etRxDescription);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSave        = findViewById(R.id.btnSaveRx);

        dbHelper = new MediAssistDatabaseHelper(this);

        // >>>> ADD THIS for choosing the image <<<<
        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String desc = etDescription.getText().toString().trim();
                if (selectedImageUri == null) {
                    Toast.makeText(AddPrescriptionActivity.this, "Choose an image first", Toast.LENGTH_SHORT).show();
                    return;
                }

                int userId = getSharedPreferences("user_session", MODE_PRIVATE)
                        .getInt("currentUserId", -1);

                long id = dbHelper.addPrescription(userId, selectedImageUri.toString(), desc);
                if (id > 0) {
                    Toast.makeText(AddPrescriptionActivity.this, "Prescription saved", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddPrescriptionActivity.this, "Save failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openImageChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Prescription Image"), REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ivPrescription.setImageURI(selectedImageUri);
        }
    }
}
