package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.myapplication.Blogs.Post_Page;
import com.example.myapplication.databinding.ActivityUserProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class check extends AppCompatActivity {
    DrawerLayout drawerLayout;
    ImageButton buttonDrawerToggle;
    NavigationView navigationView;
    //start
    ActivityUserProfileBinding binding;
    TextView name,profession,email,about,birthday,phone, country , gender , UserName , Division , District ;
    SwipeRefreshLayout swipeRefreshLayout ;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    String userId;
    TextView age , Upload ;
    CircleImageView img;

    ActivityResultLauncher<String> launcher;
    FirebaseDatabase database;
    FirebaseStorage storage;
    CardView logout;
    //end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
       drawerLayout=findViewById(R.id.drawer_layout);
       buttonDrawerToggle=findViewById(R.id.buttonDrawerToggle);
       navigationView=findViewById(R.id.navigation_view);

       //do for profile

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            //startActivity(new Intent(check.this, login_page.class));
        }
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        name=findViewById(R.id.name);
        email= findViewById(R.id.email);
        img = findViewById(R.id.image);
        birthday = findViewById(R.id.birthday);
        phone = findViewById(R.id.phone);
        logout = findViewById(R.id.logout);
        gender = findViewById(R.id.gender) ;
        Upload = findViewById(R.id.upload) ;
        UserName = findViewById(R.id.userName) ;
        swipeRefreshLayout = findViewById(R.id.reload) ;
        Division = findViewById(R.id.division) ;
        District = findViewById(R.id.district) ;


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        StorageReference dc = storage.getReference().child("users").child(firebaseAuth.getCurrentUser().getUid());
        // Refresh the Whole page .-------------------------------------------------------------------------------------------------------------
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Load Image from storage in ImageView
                dc.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Glide
                                .with(check.this)
                                .load(uri) // the uri you got from Firebase
                                .circleCrop()
                                .override(600,600)
                                .into(img); //Your imageView variable
                        Toast.makeText(check.this, "Success", Toast.LENGTH_SHORT).show();
                        String userid = firebaseAuth.getCurrentUser().getUid() ;
                        DocumentReference documentReference = firebaseFirestore.collection("users").document(userid) ;

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(check.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        // --------------------------------------------------------------------------------------------------------------------------------------

        // set user wallpaper to the CircleImageView ----------------------------------------------------------------------------------------------
        dc.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Glide.with(check.this)
                        .load(uri) // the uri you got from Firebase
                        .circleCrop()
                        .override(600,600)
                        .into(img); //Your imageView variable
                Toast.makeText(check.this, "Success", Toast.LENGTH_SHORT).show();
                String userid = firebaseAuth.getCurrentUser().getUid() ;
                DocumentReference documentReference = firebaseFirestore.collection("users").document(userid) ;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(check.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // --------------------------------------------------------------------------------------------------------------------------------------


        launcher = registerForActivityResult(new ActivityResultContracts.GetContent()
                , new ActivityResultCallback<Uri>() {

                    @Override
                    public void onActivityResult(Uri result) {
                        binding.image.setImageURI(result);

                        StorageReference reference = storage.getReference().child("users").child(firebaseAuth.getCurrentUser().getUid());

                        reference.putFile(result).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        //  Toast.makeText(ProfileActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri result) {

                                                        database.getReference().child("users").child("Image").child(firebaseAuth.getCurrentUser().getUid())
                                                                .setValue(result.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        Toast.makeText( check.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(check.this, "not uploaded", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(check.this, "failed uploaded", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(check.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });

        Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launcher.launch("image/*");
            }
        });

        // Loading Other information from firebase firestore .


        DocumentReference documentReference = firebaseFirestore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value!=null && value.exists() ) {
                    name.setText(value.getString("fname"));
                    email.setText("EMAIL  :  " + value.getString("email"));
                    phone.setText("PHONE  :  " + value.getString("phone"));
                    birthday.setText("Date of Birth  :  " + value.getString("dateOfBirth"));
                    gender.setText("Gender  :  " + value.getString("gender"));
                    UserName.setText("Username  :  " + value.getString("username"));
                    Division.setText("Division  :  " + value.getString("division"));
                    District.setText("District  :  " + value.getString("district"));
                }
                else
                {
                    Log.d("userProfile", "Document does not exist or value is null");
                }
            }
        });

        // for User Logout
        logout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("isLoggedin");
                editor.commit();
               // Toast.makeText(userProfile.this, "Logged out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(check.this, login_page.class));
                finish();
            }
        });

        //end for profile



        buttonDrawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();

            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId=item.getItemId();

                if(itemId==R.id.rating)
                {
                    startActivity(new Intent(check.this, RatingApp.class));
                }
                if(itemId==R.id.userProfile)
                {
                    startActivity(new Intent(check.this, userProfile.class));
                }
                if(itemId==R.id.map)
                {
                    startActivity(new Intent(check.this,MapsActivity.class));
                }
                if(itemId==R.id.embeddedVideo)
                {
                    startActivity(new Intent(check.this, Youtube_Video_Embedded.class));
                }
                if(itemId==R.id.rating)
                {
                    startActivity(new Intent(check.this, RatingApp.class));
                }
                if(itemId==R.id.blogs)
                {
                    startActivity(new Intent(check.this, Post_Page.class));
                }

                if(itemId==R.id.Animation)
                {
                    startActivity(new Intent(check.this, GSAP_SASS.class));

                }

                if(itemId==R.id.detection)
                {
                    startActivity(new Intent(check.this, mlpage.class));
                }
                if(itemId==R.id.RESTfulAPI)
                {
                   // startActivity(new Intent(check.this, RESTfulAPI.class));
                }


                return false;

            }
        });
    }
}