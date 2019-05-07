package root.example.com.monggopinarak.Waiter;

import android.app.ProgressDialog;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
import root.example.com.monggopinarak.Cashier.CashierDashboard;
import root.example.com.monggopinarak.Customer.CustomerDashboard;
import root.example.com.monggopinarak.DataModel.getData;
import root.example.com.monggopinarak.MainActivity;
import root.example.com.monggopinarak.Owner.OwnerDashboard;
import root.example.com.monggopinarak.R;

public class WaiterDashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "WaiterDashboard";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private long backPressedTime;
    private Toast backToast;
    private EditText etEmail, etPassword, etRePassword, etName;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference setUser, getUser;
    private TextView NamaHeader, EmailHeader;
    private Button btnRegister;
    private Spinner Selectprivileges;
    private ProgressDialog progress;
    private String UserId, UserIdAdmin, PasswordAdmin, EmailAdmin;

    AddOrder mAddOrder;
    GenerateReport mGenerateReport;
    Toolbar toolbar;
    private Boolean GenerateReportFragmentOpened = false;
    private Boolean AddOrderFragmentOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiter_dashboard);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Waiter Dashboard");
        setSupportActionBar(toolbar);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        setUser = mFirebaseDatabase.getReference().child("TableService").child("User");
        getUser = mFirebaseDatabase.getReference();

        mAddOrder = new AddOrder().newInstance();
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
        UserIdAdmin = mFirebaseAuth.getCurrentUser().getUid();
        Log.d(TAG, "onCreate() returned: " + UserIdAdmin);
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etRePassword = (EditText) findViewById(R.id.etRePassword);
        etName = (EditText) findViewById(R.id.etName);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        progress = new ProgressDialog(this);
        Selectprivileges = (Spinner) findViewById(R.id.SelectPrivileges);

        String[] privileges = new String[]{"Select Privileges", "Customer"};
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_style, privileges);
        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Selectprivileges.setAdapter(mArrayAdapter);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String repassword = etRePassword.getText().toString().trim();
                String name = etName.getText().toString().trim();
                String privileges = Selectprivileges.getSelectedItem().toString().trim();

                if (!email.isEmpty() && !password.isEmpty() && !repassword.isEmpty() && !name.isEmpty()) {
                    progress.setCancelable(false);
                    progress.setTitle("Please Wait");
                    progress.show();
                    if (privileges.equals("Select Privileges")) {
                        Toast.makeText(WaiterDashboard.this, "Please Select The User Privileges", Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                    } else if (password.equals(repassword)) {
                        createUser(email, password, name, privileges);
                    } else {
                        Toast.makeText(WaiterDashboard.this, "Password Doesn't Match", Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                    }
                } else {
                    Toast.makeText(WaiterDashboard.this, "Please Fill All Field", Toast.LENGTH_SHORT).show();
                }
            }
        });
        getUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        getData mGetData = new getData();
                        mGetData.setName(ds.child("User").child("Waiter").child(UserIdAdmin).getValue(getData.class).getName());
                        mGetData.setPassword(ds.child("User").child("Waiter").child(UserIdAdmin).getValue(getData.class).getPassword());
                        mGetData.setEmail(ds.child("User").child("Waiter").child(UserIdAdmin).getValue(getData.class).getEmail());


                        PasswordAdmin = mGetData.getPassword();
                        EmailAdmin = mGetData.getEmail();
                        EmailHeader.setText(EmailAdmin);
                        NamaHeader.setText(mGetData.getName());
                        Log.d(TAG, "onDataChange() returned: " + PasswordAdmin + " " + EmailAdmin);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createUser(final String email, final String password, final String name, final String privileges) {
        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                    UserId = user.getUid();

                    switch (privileges) {
                        case "Customer":
                            setUser.child("Customer").child(UserId).child("Level").setValue("5");
                            setUser.child("Customer").child(UserId).child("UserId").setValue(UserId);
                            setUser.child("Customer").child(UserId).child("Name").setValue(name);
                            Toast.makeText(WaiterDashboard.this, "Success Creating Customer Account", Toast.LENGTH_SHORT).show();

                            break;
                    }
                    etEmail.setText("");
                    etName.setText("");
                    etPassword.setText("");
                    etRePassword.setText("");
                    Relogin();

                } else {
                    Toast.makeText(WaiterDashboard.this, "Unable to Create New User", Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WaiterDashboard.this, "Unable to Create New User", Toast.LENGTH_SHORT).show();
                progress.dismiss();
            }
        });
    }

    private void Relogin() {
        mFirebaseAuth.signInWithEmailAndPassword(EmailAdmin, PasswordAdmin).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                startActivity(new Intent(WaiterDashboard.this, WaiterDashboard.class));
                finish();
                progress.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progress.dismiss();
            }
        });
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
        } else if (AddOrderFragmentOpened) {
            toolbar.setTitle("Waiter Dashboard");
            removeFragments(mAddOrder);
        } else if (GenerateReportFragmentOpened) {
            toolbar.setTitle("Admin Dashboard");
            removeFragments(mGenerateReport);
        } else if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            Intent intent = new Intent(WaiterDashboard.this, MainActivity.class);
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
        getMenuInflater().inflate(R.menu.waiter_dashboard, menu);
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
            startActivity(new Intent(WaiterDashboard.this, MainActivity.class));
            finish();
        } else if (id == R.id.AddUser) {
            startActivity(new Intent(WaiterDashboard.this, WaiterDashboard.class));
            finish();
        } else if (id == R.id.AddOrder) {
            toolbar.setTitle("Add New Order");
            mFragmentTransaction.replace(R.id.FragmentContainer, mAddOrder, "Add Order");
            mFragmentTransaction.attach(mAddOrder);
            mFragmentTransaction.addToBackStack(null);
            mFragmentTransaction.commit();
            AddOrderFragmentOpened = true;
        } else if (id == R.id.Laporen) {
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
        if (AddOrderFragmentOpened) {
            AddOrderFragmentOpened = false;
        } else if (GenerateReportFragmentOpened) {
            GenerateReportFragmentOpened = false;
        }
    }
}
