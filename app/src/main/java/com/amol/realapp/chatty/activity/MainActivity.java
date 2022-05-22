package com.amol.realapp.chatty.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.amol.realapp.chatty.LogSender;
import com.amol.realapp.chatty.R;
import com.amol.realapp.chatty.databinding.ActivityMainBinding;
import com.amol.realapp.chatty.fragment.ChatsFragment;
import com.amol.realapp.chatty.fragment.GroupsFragment;
import com.amol.realapp.chatty.model.Status;
import com.amol.realapp.chatty.model.userProfile;
import com.amol.realapp.chatty.model.userStatus;
import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.internal.NavigationMenuItemView;
import com.google.android.material.internal.NavigationMenuView;
import com.google.android.material.navigation.NavigationBarItemView;
import com.google.android.material.navigation.NavigationBarMenu;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
  private MaterialToolbar tBar;
  private BottomNavigationView btmNavView;
  private FragmentManager fragmentManager;

  private userProfile user;

  private Date date;

  private ActivityMainBinding binding;
  private FirebaseAuth auth;
  
  private Uri selectedImage,imageUri;
  private Intent resultIntent;
  private String status_fileString;

  private ChatsFragment chatFragment= new ChatsFragment();
  private GroupsFragment groupFragment= new GroupsFragment();
  
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    LogSender.startLogging(this);
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    init();
    initListener();
  }

  private void init() {
    auth = FirebaseAuth.getInstance();
    tBar = binding.toolbar;
    btmNavView = binding.mainBottomNavigationView;
    setSupportActionBar(tBar);
    fragmentManager = getSupportFragmentManager();
  }

  private void initListener() {
      
    btmNavView.setOnItemSelectedListener(
        p1 -> {
          Fragment fragment = null;
          switch (p1.getItemId()) {
            case R.id.chats:
              fragment = new ChatsFragment();
              break;
            case R.id.groups:
              fragment = new GroupsFragment();

              break;
            case R.id.status:
              fragment = new ChatsFragment();
              launchStatusImage();
              break;
            default:
              fragment = new ChatsFragment();
          }

          fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();

          return true;
        });
    btmNavView.setOnItemReselectedListener(
    	p1 -> {
            // Do Nothing
            
            }
    );   
    btmNavView.setSelectedItemId(R.id.chats);
    FirebaseDatabase.getInstance()
        .getReference()
        .child("Users")
        .child(FirebaseAuth.getInstance().getUid())
        .addValueEventListener(
            new ValueEventListener() {

              @Override
              public void onDataChange(DataSnapshot p1) {
                if (p1.exists()) {
                  user = p1.getValue(userProfile.class);
                }
              }

              @Override
              public void onCancelled(DatabaseError p2) {
                Log.d("MainActivity.java", p2.getMessage());
              }
            });
  }

  public void launchStatusImage() {
    statusImageRetriever.launch("image/*");
  }

  ActivityResultLauncher<String> statusImageRetriever =
      registerForActivityResult(
          new ActivityResultContracts.GetContent(),
          new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
              if (uri != null) {
                getUriFromActivityLauncher(uri);
              }
            }
          });
          
  private void getUriFromActivityLauncher(Uri uri) {
    
    File f1 =
        new File(
            Environment.getExternalStorageDirectory() + "/" + "Chatty" + "/" + "Status",
            FirebaseAuth.getInstance().getCurrentUser().getUid());
    if (!f1.exists()) {
      f1.mkdirs();
    }
    status_fileString =
        "Chatty"
            + File.separator
            + "Status"
            + File.separator
            + FirebaseAuth.getInstance().getCurrentUser().getUid();

    
    Intent dsPhotoEditorIntent = new Intent(MainActivity.this, DsPhotoEditorActivity.class);
    dsPhotoEditorIntent.setData(uri);

    dsPhotoEditorIntent.putExtra(
        DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, status_fileString);

      intentLauncher.launch(dsPhotoEditorIntent);
    
  }
  private ActivityResultLauncher<Intent> intentLauncher =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
              resultIntent = result.getData();
              imageUri = resultIntent.getData();

              if (imageUri != null) {
                Log.d(
                    "MainActivity.java",
                    "Image Uri  "
                        + imageUri.toString()
                        + " File Name: "
                        + imageUri.getLastPathSegment());
				
                uploadToFirebase(imageUri);
                
                selectedImage = imageUri;
              } else {
                Log.d("MainActivity.java", "Image Uri null");
              }
            }
          });
  private void showToast(String text) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
  }
  private void uploadToFirebase(Uri uri){
     
  selectedImage = uri;
    date = new Date();
    StorageReference sRef =
        FirebaseStorage.getInstance().getReference().child("status").child(date.getTime() + "");
    sRef.putFile(uri)
        .addOnCompleteListener(
            new OnCompleteListener<UploadTask.TaskSnapshot>() {

              @Override
              public void onComplete(Task<UploadTask.TaskSnapshot> p1) {
                sRef.getDownloadUrl()
                    .addOnSuccessListener(
                        new OnSuccessListener<Uri>() {

                          @Override
                          public void onSuccess(Uri p1) {
                            userStatus uStatus = new userStatus();

                            uStatus.setName(user.getName());
                            uStatus.setProfileImage(user.getUserProfileImage());
                            uStatus.setLastUpdated(date.getTime());

                            HashMap<String, Object> obj = new HashMap<>();
                            obj.put("name", uStatus.getName());
                            obj.put("profileImage", uStatus.getProfileImage());
                            obj.put("lastUpdated", uStatus.getLastUpdated());

                            String imageUrl = p1.toString();
                            Status status = new Status(imageUrl, uStatus.getLastUpdated());

                            FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Stories")
                                .child(FirebaseAuth.getInstance().getUid())
                                .updateChildren(obj);

                            FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Stories")
                                .child(FirebaseAuth.getInstance().getUid())
                                .child("statuses")
                                .push()
                                .setValue(status);
                          }
                        });
              }
            });
  
  }
     

  @Override
  protected void onStart() {
    super.onStart();
    if (auth.getCurrentUser() == null) {
      startActivity(new Intent(this, RegisterActivity.class));
      finish();
    }
  }
}
