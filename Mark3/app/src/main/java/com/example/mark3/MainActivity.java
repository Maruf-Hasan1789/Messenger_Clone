package com.example.mark3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    private Toolbar myToolbar;
    private TabLayout myTabLayout;
    private ViewPager myViewPager;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();
        currentUser=mAuth.getCurrentUser();

        myToolbar=(Toolbar)findViewById(R.id.myToolbar);
        myTabLayout=(TabLayout)findViewById(R.id.myTab);
        myViewPager=(ViewPager)findViewById(R.id.myViewPager);
        setSupportActionBar(myToolbar);
        SetupViwPager(myViewPager);
        myTabLayout.setupWithViewPager(myViewPager);
    }
    private  void SetupViwPager(ViewPager viewPager)
    {
        ViewPagerAdapter viewpageradapter= new ViewPagerAdapter(getSupportFragmentManager());
        viewpageradapter.addFragment(new ChatFragment(),"Chats");
        viewpageradapter.addFragment(new GroupsFragment(),"Groups");
        viewpageradapter.addFragment(new ContactsFragment(),"Contacts");
            viewPager.setAdapter(viewpageradapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser==null)
        {
            sendUsertoLoginActivity();
        }
        else
        {
            verifyUserExistence();
        }
    }

    private void verifyUserExistence()
    {
     String currentUserID= mAuth.getCurrentUser().getUid();

     RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot)
         {
            if((dataSnapshot.child("name").exists()))
            {
                Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
            }
            else
            {
                sendUsertoSettingsActivity();
            }
         }

         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

         }
     });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu,menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_option)
        {
            mAuth.signOut();
            sendUsertoLoginActivity();

        }
        if(item.getItemId()==R.id.main_settings_option)
        {
            sendUsertoSettingsActivity();
        }
        if(item.getItemId()==R.id.main_friends_option)
        {
            sendUsertoFindFriendsActivity();

        }
        if(item.getItemId()==R.id.main_Create_Group_option)
        {
            RequestNewGroup();
        }
        return  true;
    }

    private void RequestNewGroup()
    {
        AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name");
        final EditText groupNameField= new EditText(MainActivity.this);
        groupNameField.setHint("Friends");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                String groupName= groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this, "Please write GroupName", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    CreateNewGroup(groupName);
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
                }
        });

        builder.show();


    }

    private void CreateNewGroup(final String groupName)
    {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, groupName+" is created Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void sendUsertoLoginActivity() {
        Intent LoginIntent= new Intent(MainActivity.this,LoginActivity.class);
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
        finish();
    }
    private void sendUsertoSettingsActivity() {
        Intent settingsIntent= new Intent(MainActivity.this,SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        finish();
    }
    private void sendUsertoFindFriendsActivity() {
        Intent findFriendsIntent= new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriendsIntent);
        finish();
    }


}
