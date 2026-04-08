package com.example.photogalleryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Button btnCapture, btnViewGallery;
    GridView gridView;
    ArrayList<File> imageFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCapture = findViewById(R.id.btnCapture);
        btnViewGallery = findViewById(R.id.btnViewGallery);
        gridView = findViewById(R.id.gridView);

        btnCapture.setOnClickListener(v -> {
            if (checkPermissions()) openCamera();
            else requestPermissions();
        });

        btnViewGallery.setOnClickListener(v -> loadGallery());

        gridView.setOnItemClickListener((parent, view, position, id) -> showDetailsDialog(imageFiles.get(position)));
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            saveImage(photo);
        }
    }

    private void saveImage(Bitmap bitmap) {
        File folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyGallery");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, "IMG_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(this, "Saved: " + file.getName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadGallery() {
        File folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyGallery");
        File[] files = folder.listFiles();
        imageFiles.clear();
        if (files != null) {
            for (File f : files) if (f.getName().endsWith(".jpg")) imageFiles.add(f);
        }
        gridView.setAdapter(new ImageAdapter());
        gridView.setVisibility(View.VISIBLE);
    }

    private void showDetailsDialog(File file) {
        String details = "Name: " + file.getName() + "\n" +
                "Path: " + file.getAbsolutePath() + "\n" +
                "Size: " + (file.length() / 1024) + " KB\n" +
                "Date: " + new Date(file.lastModified());

        new AlertDialog.Builder(this)
                .setTitle("Image Details")
                .setMessage(details)
                .setPositiveButton("Delete", (dialog, which) -> confirmDelete(file))
                .setNegativeButton("Close", null)
                .show();
    }

    private void confirmDelete(File file) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Do you want to delete this image?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (file.delete()) {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                        loadGallery();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    class ImageAdapter extends BaseAdapter {
        @Override
        public int getCount() { return imageFiles.size(); }
        @Override
        public Object getItem(int i) { return imageFiles.get(i); }
        @Override
        public long getItemId(int i) { return i; }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) view = getLayoutInflater().inflate(R.layout.grid_item, null);
            ImageView iv = (ImageView) view;
            iv.setImageBitmap(BitmapFactory.decodeFile(imageFiles.get(i).getAbsolutePath()));
            return iv;
        }
    }
}