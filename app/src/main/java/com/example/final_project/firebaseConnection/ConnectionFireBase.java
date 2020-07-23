package com.example.final_project.firebaseConnection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.final_project.Database.useradate.UserDatadbProvider;
import com.example.final_project.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class ConnectionFireBase {

    private FirebaseDatabase database;
    public DatabaseReference myRef;
    private StorageReference mStorageRef;
    public Uri downloadImageUri;
    public ConnectionFireBase(){
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    public void setData(UserData data,String username){
        StringBuilder l= new StringBuilder();
        for(int i =0 ; i<username.length() ; i++){
            if(username.charAt(i)!='.' && username.charAt(i)!='#' && username.charAt(i)!='$' && username.charAt(i)!='[' && username.charAt(i)!=']')
                l.append(username.charAt(i));
        }
        myRef = database.getReference("user_prof/member/"+l);
        myRef.push().setValue(data);
        Log.e(TAG,"Data Set to database");
    }

    public void uploadImageToStorage(String location, String imageName, Uri imageUrl){
        Log.e(TAG,location+" "+imageName+" "+imageUrl);
        StorageReference riversRef = mStorageRef.child(location+"/"+imageName);
        if(imageUrl != null)
        riversRef.putFile(imageUrl)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get a URL to the uploaded content
                    downloadImageUri = taskSnapshot.getUploadSessionUri();
                    Log.e(TAG,"image uploaded");
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG,"Cant upload file");
                    downloadImageUri = null;
                });
        else{
            Log.e(TAG,"Url is empty");
        }
    }

    public void uploadImageToStorage(String location, String imageName, byte[] imageUrl){
        Log.e(TAG,location+" "+imageName+" "+imageUrl);
        StorageReference riversRef = mStorageRef.child(location+"/"+imageName);
        if(imageUrl != null)
            riversRef.putBytes(imageUrl)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get a URL to the uploaded content
                        downloadImageUri = taskSnapshot.getUploadSessionUri();
                        Log.e(TAG,"image uploaded");
                    })
                    .addOnFailureListener(exception -> {
                        Log.e(TAG,"Cant upload file");
                        downloadImageUri = null;
                    });
        else{
            Log.e(TAG,"Url is empty");
        }
    }

    public void uploadImageToStorage(String location, String imageName, byte[] imageUrl, AppCompatActivity view){
        Log.e(TAG,location+" "+imageName+" "+ Arrays.toString(imageUrl));
        ProgressDialog n = new ProgressDialog(view.getApplicationContext());
        StorageReference riversRef = mStorageRef.child(location+"/"+imageName);
        if(imageUrl != null)
            riversRef.putBytes(imageUrl)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get a URL to the uploaded content
                    downloadImageUri = taskSnapshot.getUploadSessionUri();
                    Log.e(TAG,"image uploaded");
                    n.dismiss();
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG,"Cant upload file");
                    downloadImageUri = null;
                }).addOnProgressListener(view, taskSnapshot -> {
                    n.show();
                    int progress = (int) (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    n.setProgress(progress);
                });
        else{
            Log.e(TAG,"Url is empty");
        }
    }

    @SuppressLint("ShowToast")
    public void uploadImageToStorage(String location, String imageName, Uri imageUrl, AppCompatActivity view){
        StorageReference riversRef = mStorageRef.child(location+"/"+imageName);
        ProgressBar n = new ProgressBar(view.getApplication());
        riversRef.putFile(imageUrl)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get a URL to the uploaded content
                    downloadImageUri = taskSnapshot.getUploadSessionUri();
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG,"Cant upload file");
                    Toast.makeText(view.getApplicationContext(),"Can't able to upload file",Toast.LENGTH_SHORT);
                    downloadImageUri = null;
                }).addOnProgressListener(view, taskSnapshot -> {
                    n.setVisibility(View.VISIBLE);
                    int progress = (int) (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    n.setProgress(progress);
                });
    }
    /**
     * <p>return the url od the image of given location,name</p>
     * @param location ,where image has to be stored in the database
     * @param imageName name of the image
     * */
    public void downloadProfileImage(String location, String imageName) {
        StorageReference riversRef = mStorageRef.child(location+"/"+imageName);
        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()  {
            // Got the download URL for 'users/me/profile.png'
            @Override
            public void onSuccess(Uri uri) {
                downloadImageUri = uri;
                Log.e(TAG, "download image url : " + downloadImageUri.toString()+"\n path : "+uri.getPath()+"\n LPS : "+uri.getLastPathSegment());
                }
        }).addOnFailureListener(exception -> {
            Log.e(TAG,"Can't able to find the photo");
        });
    }

    /**
     * <p>return the url od the image of given location,name</p>
     * @param location ,where image has to be stored in the database
     * @param imageName name of the image
     * */
    public void downloadProfileImage(String location, String imageName, ImageView imageView, Context context) {
        StorageReference riversRef = mStorageRef.child(location+"/"+imageName);
        ProgressDialog n = new ProgressDialog(context);
        n.setTitle("Loading");
        n.setMessage("Loading your image");
        n.show();
        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()  {
            // Got the download URL for 'users/me/profile.png'
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context.getApplicationContext())
                        .load(uri)
                        .placeholder(R.drawable.account_pic)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                n.dismiss();
                                return false;
                            }
                        })
                        .into(imageView);
                }
        }).addOnFailureListener(exception -> {
            Log.e(TAG,"Can't able to find the photo");
        });
    }

 /**
     * <p>return the url od the image of given location,name</p>
     * @param location ,where image has to be stored in the database
     * @param imageName name of the image
     * */
    public void downloadProfileImage(String location, String imageName, ImageView imageView, Context context,ProgressBar progressBar) {
        StorageReference riversRef = mStorageRef.child(location+"/"+imageName);
        progressBar.setVisibility(View.VISIBLE);
        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()  {
            // Got the download URL for 'users/me/profile.png'
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context.getApplicationContext())
                        .load(uri)
                        .placeholder(R.drawable.account_pic)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBar.setVisibility(View.INVISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(View.INVISIBLE);
                                return false;
                            }
                        })
                        .into(imageView);
                }
        }).addOnFailureListener(exception -> {
            Log.e(TAG,"Can't able to find the photo");
        });
    }

    /**
     * <p>return the url od the image of given location,name</p>
     * @param location ,where image has to be stored in the database
     * @param string name of the image
     * */
    public void getString(String location, String string,Context context) {
        myRef.child(location);
        ProgressDialog d = new ProgressDialog(context);
        d.setTitle("Loading");
        d.setMessage("Loading text ...");
        d.show();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e(TAG,""+snapshot.getChildrenCount());
                Log.e(TAG,""+snapshot.getValue());
                d.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                d.dismiss();
            }
        });
    }

}
