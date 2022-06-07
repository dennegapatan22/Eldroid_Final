package com.example.eldroid_final;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class user_form extends AppCompatActivity {



    final int PICK_IMAGE = 0;
    String category;
    String userKey;
    Uri imageUri;
    EditText et_firstName, et_lastName, et_contactNumber, et_username;
    ImageView iv_profile_photo;
    TextView tv_uploadPhoto;
    Button btn_signUp;

    StorageReference userStorage;
    DatabaseReference userDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_form);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = user.getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference(Users.class.getSimpleName());
        userStorage = FirebaseStorage.getInstance().getReference("Users").child(userID);
        category = getIntent().getStringExtra("category");
        userKey = getIntent().getStringExtra("user id");


        et_firstName = findViewById(R.id.et_firstName);
        et_lastName = findViewById(R.id.et_lastName);
        et_contactNumber = findViewById(R.id.et_contactNumber);
        et_username = findViewById(R.id.et_username);
        btn_signUp = findViewById(R.id.btn_signUp);
        tv_uploadPhoto = findViewById(R.id.tv_uploadPhoto);
        iv_profile_photo = findViewById(R.id.iv_profile_photo);

        clicks();
        if(category != null)
        {
            if(category.equals("edit"))
            {
                btn_signUp.setText("Update");
                retrieveData(userKey);
            }
        }
    }

    private void retrieveData(String userKey) {
        userDatabase.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    Users users = snapshot.getValue(Users.class);

                    String imageUrl = users.imageUrl;
                    String firstName = users.firstName;
                    String lastName = users.lastName;
                    String contactNum = users.contactNum;
                    String email = users.email;

                    Picasso.get()
                            .load(imageUrl)
                            .into(iv_profile_photo);

                    et_firstName.setText(firstName);
                    et_lastName.setText(lastName);
                    et_username.setText(email);
                    et_contactNumber.setText(contactNum);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void clicks() {

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(category != null) {
                    if (category.equals("edit")) {
                        if (imageUri == null)
                        {
                            String firstName = et_firstName.getText().toString();
                            String lastName = et_lastName.getText().toString();
                            String username = et_username.getText().toString();
                            String contactNum = et_contactNumber.getText().toString();


                            if (TextUtils.isEmpty(firstName))
                            {
                                et_firstName.setError("This field is required");
                            }
                            else if (TextUtils.isEmpty(lastName))
                            {
                                et_lastName.setError("This field is required");
                            }
                            else if (TextUtils.isEmpty(username) )
                            {
                                et_username.setError("This field is required");
                            }
                            else if (TextUtils.isEmpty(contactNum))
                            {
                                et_contactNumber.setError("This field is required");
                            }
                            else if (contactNum.length() != 12)
                            {
                                et_contactNumber.setError("Contact number must be 11 digit");
                            }
                            else
                            {
                                final ProgressDialog progressDialog = new ProgressDialog(user_form.this);
                                progressDialog.setTitle("Creating account");
                                progressDialog.show();

                                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                hashMap.put("firstName", firstName);
                                hashMap.put("lastName", lastName);
                                hashMap.put("contactNum", contactNum);
                                hashMap.put("username", username);

                                userDatabase.child(userKey).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            progressDialog.dismiss();
                                            Intent intent = new Intent(user_form.this, MainActivity.class);
                                            startActivity(intent);
                                            Toast.makeText(user_form.this, "User Updated", Toast.LENGTH_LONG).show();

                                        } else {
                                            Toast.makeText(user_form.this, "Update Failed ", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                        }
                        else
                        {
                                String firstName = et_firstName.getText().toString();
                                String lastName = et_lastName.getText().toString();
                                String username = et_username.getText().toString();
                                String password = "";
                                String contactNum = et_contactNumber.getText().toString();
                                String ratings = "0";

                                if (imageUri == null)
                                {
                                    Toast.makeText(user_form.this, "Profile photo is required", Toast.LENGTH_SHORT).show();
                                }
                                else if (TextUtils.isEmpty(firstName))
                                {
                                    et_firstName.setError("This field is required");
                                }
                                else if (TextUtils.isEmpty(lastName))
                                {
                                    et_lastName.setError("This field is required");
                                }
                                else if (TextUtils.isEmpty(username))
                                {
                                    et_username.setError("This field is required");
                                }
                                else if (TextUtils.isEmpty(contactNum) )
                                {
                                    et_contactNumber.setError("This field is required");
                                }
                                else if (contactNum.length() != 12)
                                {
                                    et_contactNumber.setError("Contact number must be 11 digit");
                                }
                                else
                                {
                                    final ProgressDialog progressDialog = new ProgressDialog(user_form.this);
                                    progressDialog.setTitle("Creating account");
                                    progressDialog.show();

                                    StorageReference fileReference = userStorage.child(imageUri.getLastPathSegment());
                                    String imageName = imageUri.getLastPathSegment();

                                    fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    final String imageURL = uri.toString();

                                                    String uid = "";
                                                    Users users = new Users(uid, firstName, lastName, contactNum, username, password, imageName, imageURL, ratings);

                                                    userDatabase.child(userKey).setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {

                                                                progressDialog.dismiss();
                                                                Intent intent = new Intent(user_form.this, MainActivity.class);
                                                                startActivity(intent);
                                                                Toast.makeText(user_form.this, "User Created", Toast.LENGTH_LONG).show();

                                                            } else {
                                                                Toast.makeText(user_form.this, "Creation Failed ", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(user_form.this, "Failed: ", Toast.LENGTH_LONG).show();
                                                        }
                                                    });

                                        }
                                    });


                                }
                            }


                    }
                }
                else
                {
                    String firstName = et_firstName.getText().toString();
                    String lastName = et_lastName.getText().toString();
                    String username = et_username.getText().toString();
                    String password = "";
                    String contactNum = et_contactNumber.getText().toString();
                    String ratings = "0";

                    if (imageUri == null)
                    {
                        Toast.makeText(user_form.this, "Profile photo is required", Toast.LENGTH_SHORT).show();
                    }
                    else if (TextUtils.isEmpty(firstName))
                    {
                        et_firstName.setError("This field is required");
                    }
                    else if (TextUtils.isEmpty(lastName))
                    {
                        et_lastName.setError("This field is required");
                    }
                    else if (TextUtils.isEmpty(username) )
                    {
                        et_username.setError("This field is required");
                    }
                    else if (TextUtils.isEmpty(contactNum))
                    {
                        et_contactNumber.setError("This field is required");
                    }
                    else if (contactNum.length() != 11)
                    {
                        et_contactNumber.setError("Contact number must be 11 digit");
                    }
                    else
                    {
                        final ProgressDialog progressDialog = new ProgressDialog(user_form.this);
                        progressDialog.setTitle("Creating account");
                        progressDialog.show();

                        StorageReference fileReference = userStorage.child(imageUri.getLastPathSegment());
                        String imageName = imageUri.getLastPathSegment();

                        fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String imageURL = uri.toString();

                                        String uid = "";
                                        Users users = new Users(uid, firstName, lastName, contactNum, username, password, imageName, imageURL, ratings);

                                        userDatabase.push().setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    progressDialog.dismiss();
                                                    Intent intent = new Intent(user_form.this, MainActivity.class);
                                                    startActivity(intent);
                                                    Toast.makeText(user_form.this, "User Created", Toast.LENGTH_LONG).show();

                                                } else {
                                                    Toast.makeText(user_form.this, "Creation Failed ", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(user_form.this, "Failed: ", Toast.LENGTH_LONG).show();
                                            }
                                        });

                            }
                        });


                    }
                }

            }
        });

        tv_uploadPhoto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                boolean pick = true;
                if (pick == true){
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else
                        getImage();

                }else{
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }else
                        getImage();
                }
            }
        });

    }

    private void getImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE){
            if (resultCode == RESULT_OK) {
                imageUri = data.getData();

                try{
                    Picasso.get().load(imageUri)
                            .into(iv_profile_photo);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }



        }
    }

    // validate permissions
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }

    private boolean checkStoragePermission() {
        boolean res2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
        return res2;
    }

    private boolean checkCameraPermission() {
        boolean res1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED;
        boolean res2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
        return res1 && res2;
    }
}