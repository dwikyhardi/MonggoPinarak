package root.example.com.monggopinarak.Owner;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import root.example.com.monggopinarak.Admin.AddMenuRef;
import root.example.com.monggopinarak.Admin.AddOrder;
import root.example.com.monggopinarak.Admin.AddTransaction;
import root.example.com.monggopinarak.Admin.GenerateReport;
import root.example.com.monggopinarak.Customer.CustomerDashboard;
import root.example.com.monggopinarak.DataModel.getData;
import root.example.com.monggopinarak.MainActivity;
import root.example.com.monggopinarak.R;

public class OwnerDashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "OwnerDashboard";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference setUser, getUser;
    private TextView NamaHeader, EmailHeader;
    private long backPressedTime;
    private Toast backToast;
    private String UserId;

    private Boolean GenerateReportFragmentOpened = false;
    Toolbar toolbar;

    GenerateReport mGenerateReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_dashboard);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Owner Dashboard");
        setSupportActionBar(toolbar);
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        setUser = mFirebaseDatabase.getReference().child("TableService").child("User");
        getUser = mFirebaseDatabase.getReference();
        mGenerateReport = new GenerateReport().newInstance();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        NamaHeader = (TextView) header.findViewById(R.id.namaHeader);
        EmailHeader = (TextView) header.findViewById(R.id.emailHeader);


        mFirebaseAuth = FirebaseAuth.getInstance();
        UserId = mFirebaseAuth.getCurrentUser().getUid();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    EmailHeader.setText(user.getEmail());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        getUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        getData mGetData = new getData();
                        mGetData.setName(ds.child("User").child("Owner").child(UserId).getValue(getData.class).getName());

                        NamaHeader.setText(mGetData.getName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        toolbar.setTitle("Generate Report");
        mFragmentTransaction.replace(R.id.FragmentContainer, mGenerateReport, "Generate Report");
        mFragmentTransaction.attach(mGenerateReport);
        mFragmentTransaction.addToBackStack(null);
        mFragmentTransaction.commit();
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else if (GenerateReportFragmentOpened) {
            toolbar.setTitle("Admin Dashboard");
            removeFragments(mGenerateReport);
        } else if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            Intent intent = new Intent(OwnerDashboard.this, MainActivity.class);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.owner_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.logout) {
            mFirebaseAuth.signOut();
            startActivity(new Intent(OwnerDashboard.this, MainActivity.class));
            finish();
        }else if (id == R.id.Laporen){
            toolbar.setTitle("Generate Report");
            mFragmentTransaction.replace(R.id.FragmentContainer, mGenerateReport, "Generate Report");
            mFragmentTransaction.attach(mGenerateReport);
            mFragmentTransaction.addToBackStack(null);
            mFragmentTransaction.commit();
            GenerateReportFragmentOpened = true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void removeFragments(Fragment mFragment) {
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        mFragmentTransaction.detach(mFragment).commit();
        if (GenerateReportFragmentOpened) {
            GenerateReportFragmentOpened = false;
        }
    }
}
