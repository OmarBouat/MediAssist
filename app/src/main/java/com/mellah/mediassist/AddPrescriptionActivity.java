package com.mellah.mediassist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AddPrescriptionActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 100;

    private ImageView ivPrescription;
    private EditText  etDescription;
    private Button    btnChooseImage, btnSave;
    private Uri       selectedImageUri, cameraImageUri;
    private MediAssistDatabaseHelper dbHelper;
    private int       rxId = -1;

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
        btnSave.setOnClickListener(v -> savePrescription());
    }

    /**
     * Build a chooser for Camera vs Gallery
     */
    private void openImageChooser() {
        // 1) Gallery picker
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");

        // 2) Camera capture
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    photoFile
            );
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        } catch (IOException e) {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
            cameraIntent = null;
        }

        // 3) Chooser
        Intent chooser = Intent.createChooser(galleryIntent, "Select image source");
        if (cameraIntent != null) {
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{ cameraIntent });
        }
        startActivityForResult(chooser, REQUEST_IMAGE_PICK);
    }

    /**
     * Creates a temp file in your app's external pictures dir.
     */
    private File createImageFile() throws IOException {
        String filename = "rx_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(filename, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_IMAGE_PICK || resultCode != RESULT_OK) return;

        Uri uri = null;
        // Gallery path
        if (data != null && data.getData() != null) {
            uri = data.getData();
        }
        // Camera path
        else if (cameraImageUri != null) {
            uri = cameraImageUri;
        }

        if (uri != null) {
            String localPath = persistImage(uri);
            if (localPath != null) {
                selectedImageUri = Uri.fromFile(new File(localPath));
                ivPrescription.setImageURI(selectedImageUri);
            } else {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void savePrescription() {
        String desc = etDescription.getText().toString().trim();
        if (selectedImageUri == null) {
            Toast.makeText(this, "Choose an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        String uriString = selectedImageUri.toString();

        boolean success;
        if (rxId >= 0) {
            success = dbHelper.updatePrescription(rxId, uriString, desc);
        } else {
            int userId = getSharedPreferences("user_session", MODE_PRIVATE)
                    .getInt("currentUserId", -1);
            long id = dbHelper.addPrescription(userId, uriString, desc);
            success = id > 0;
        }

        Toast.makeText(
                this,
                success ? "Prescription saved" : "Save failed",
                Toast.LENGTH_SHORT
        ).show();
        if (success) finish();
    }

    /**
     * Copy the given URI into internal storage and return its path.
     */
    private String persistImage(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            File file = new File(getFilesDir(),
                    "rx_" + System.currentTimeMillis() + ".jpg");
            try (OutputStream out = new FileOutputStream(file)) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
