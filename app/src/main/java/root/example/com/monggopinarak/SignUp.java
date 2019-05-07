package root.example.com.monggopinarak;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import root.example.com.monggopinarak.Customer.CustomerDashboard;

public class SignUp extends AppCompatActivity {

    private final String TAG = "SignUp";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference setUser;
    private String UserId;

    private EditText etEmail, etPassword, etRePassword, etName;
    private TextView tvSignIn;
    private Button btnSignUp;
    private ProgressDialog progress;
    private long backPressedTime;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        setUser = mFirebaseDatabase.getReference().child("TableService").child("User");
        mFirebaseAuth = FirebaseAuth.getInstance();

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etRePassword = (EditText) findViewById(R.id.etRePassword);
        etName = (EditText) findViewById(R.id.etName);
        tvSignIn = (TextView) findViewById(R.id.tvSignIn);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        progress = new ProgressDialog(this);
        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUp.this, MainActivity.class));
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setCancelable(false);
                progress.setTitle("Please Wait");
                progress.show();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String repassword = etRePassword.getText().toString().trim();

                if (password.equals(repassword)) {
                    createUser(email, password);
                }
            }
        });
    }

    private void createUser(String email, String password) {
        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                    String name = etName.getText().toString().trim();
                    UserId = user.getUid();
                    setUser.child("Customer").child(UserId).child("Level").setValue("5");
                    setUser.child("Customer").child(UserId).child("UserId").setValue(UserId);
                    setUser.child("Customer").child(UserId).child("Name").setValue(name).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            startActivity(new Intent(SignUp.this, CustomerDashboard.class));
                            finish();
                        }
                    });

                    progress.dismiss();
                } else {
                    Toast.makeText(SignUp.this, "Unable to Create New User", Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUp.this, "Unable to Create New User", Toast.LENGTH_SHORT).show();
                progress.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            Intent intent = new Intent(SignUp.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
            finish();
        } else {
            backToast = Toast.makeText(getBaseContext(), "Press Back Again to Exit", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}
