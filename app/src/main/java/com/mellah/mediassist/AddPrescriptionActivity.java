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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AddPrescriptionActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 100;

    private ImageView ivPrescription;
    private EditText etDescription;
    private Button btnChooseImage, btnSave;
    private Uri selectedImageUri;
    private MediAssistDatabaseHelper dbHelper;

    private int rxId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_prescription);

        ivPrescription = findViewById(R.id.ivPrescription);
        etDescription  = findViewById(R.id.etRxDescription);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSave        = findViewById(R.id.btnSaveRx);
        dbHelper       = new MediAssistDatabaseHelper(this);

        // If editing, prefill
        Intent intent = getIntent();
        if (intent.hasExtra("rxId")) {
            rxId = intent.getIntExtra("rxId", -1);

            String path = intent.getStringExtra("imagePath");
            if (path != null && !path.isEmpty()) {
                selectedImageUri = Uri.parse(path);
                ivPrescription.setImageURI(selectedImageUri);
            }

            String desc = intent.getStringExtra("description");
            etDescription.setText(desc != null ? desc : "");
        }


        btnChooseImage.setOnClickListener(v -> openImageChooser());

        btnSave.setOnClickListener(v -> {
            String desc = etDescription.getText().toString().trim();
            if (selectedImageUri == null) {
                Toast.makeText(this, "Choose an image first", Toast.LENGTH_SHORT).show();
                return;
            }
            String uriString = selectedImageUri.getPath();
            boolean success;
            if (rxId >= 0) {
                success = dbHelper.updatePrescription(rxId, uriString, desc);
            } else {
                int userId = getSharedPreferences("user_session", MODE_PRIVATE)
                        .getInt("currentUserId", -1);
                long id = dbHelper.addPrescription(userId, uriString, desc);
                success = id > 0;
            }
            if (success) {
                Toast.makeText(this, "Prescription saved", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(intent, "Select Prescription Image"),
                REQUEST_IMAGE_PICK
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            String localPath = persistImage(data.getData());
            if (localPath != null) {
                selectedImageUri = Uri.fromFile(new File(localPath));
                ivPrescription.setImageURI(selectedImageUri);
            } else {
                Toast.makeText(this, "Failed to copy image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String persistImage(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            File file = new File(getFilesDir(), "rx_" + System.currentTimeMillis() + ".jpg");
            try (OutputStream out = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
            return file.getAbsolutePath(); // <- save this in DB
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
