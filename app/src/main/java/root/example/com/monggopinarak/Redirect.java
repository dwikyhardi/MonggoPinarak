package root.example.com.monggopinarak;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import root.example.com.monggopinarak.Admin.AdminDashboard;
import root.example.com.monggopinarak.Cashier.CashierDashboard;
import root.example.com.monggopinarak.Customer.CustomerDashboard;
import root.example.com.monggopinarak.Owner.OwnerDashboard;
import root.example.com.monggopinarak.Waiter.WaiterDashboard;

public class Redirect extends AppCompatActivity {

    private final String TAG = "Redirect";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference getUser;
    private FirebaseUser mFirebaseUser;
    private String UserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        getUser = mFirebaseDatabase.getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    UserId = user.getUid();

                    getUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                getPrivilage(dataSnapshot);
                            } else {
                                getUser.removeEventListener(this);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    private void getPrivilage(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            if (ds.child("User").child("Admin").child(UserId).child("Level").exists()) {
                Intent mIntent = new Intent(Redirect.this, AdminDashboard.class);
                mIntent.putExtra("Privilages", "Admin");
                startActivity(mIntent);
                finish();
            } else if (ds.child("User").child("Cashier").child(UserId).child("Level").exists()) {
                Intent mIntent = new Intent(Redirect.this, CashierDashboard.class);
                mIntent.putExtra("Privilages", "Cashier");
                startActivity(mIntent);
                finish();
            } else if (ds.child("User").child("Customer").child(UserId).child("Level").exists()) {
                Intent mIntent = new Intent(Redirect.this, CustomerDashboard.class);
                mIntent.putExtra("Privilages", "Customer");
                startActivity(mIntent);
                finish();
            } else if (ds.child("User").child("Owner").child(UserId).child("Level").exists()) {
                Intent mIntent = new Intent(Redirect.this, OwnerDashboard.class);
                mIntent.putExtra("Privilages", "Owner");
                startActivity(mIntent);
                finish();
            } else if (ds.child("User").child("Waiter").child(UserId).child("Level").exists()) {
                Intent mIntent = new Intent(Redirect.this, WaiterDashboard.class);
                mIntent.putExtra("Privilages", "Customer");
                startActivity(mIntent);
                finish();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}

