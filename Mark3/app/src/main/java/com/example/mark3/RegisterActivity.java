package com.example.mark3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {
    private Button CreateAccountButton;
    private EditText UserEmail,UserPassword;
    private TextView AlreadyHaveAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference RootReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();
        RootReference= FirebaseDatabase.getInstance().getReference();
        initializeFields();
        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUsertoLoginActivity();
            }
        });
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }
    private void CreateNewAccount()
    {
        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please Enter an Email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please Wait,while we are busy");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                String currentUserId= mAuth.getCurrentUser().getUid();
                                RootReference.child("Users").child(currentUserId).setValue("");
                                sendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account Created SuccessFully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String Message=task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error"+Message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }
    private void initializeFields() {

        CreateAccountButton=(Button)findViewById(R.id.register_button);
        UserEmail=(EditText)findViewById(R.id.register_email);
        UserPassword=(EditText)findViewById(R.id.register_password);
        AlreadyHaveAccountLink=(TextView)findViewById(R.id.already_have_account);
        loadingBar=new ProgressDialog(this);
    }

    private void sendUsertoLoginActivity() {
        Intent LoginIntent1= new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(LoginIntent1);
    }
    private void sendUserToMainActivity() {
        Intent MainIntent1= new Intent(RegisterActivity.this,MainActivity.class);
        MainIntent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent1);
        finish();
    }
}
