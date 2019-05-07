package root.example.com.monggopinarak.Admin;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import root.example.com.monggopinarak.Adapter.ListViewAdapter;
import root.example.com.monggopinarak.Adapter.MenuListAdapter;
import root.example.com.monggopinarak.DataModel.getDataMenu;
import root.example.com.monggopinarak.DataModel.setMenu;
import root.example.com.monggopinarak.R;
import root.example.com.monggopinarak.DataModel.setDataMenu;

public class AddMenuRef extends Fragment implements DialogInterface.OnDismissListener {

    private final String TAG = "AddMenuRef";

    private EditText etMenuName, etMenuPrice;
    private Button btnSubmit, btnDelete;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference setMenu, displayMenu;
    private ProgressDialog progressDialog;
    private Dialog editMenu;
    private FloatingActionButton addMenu;
    private ListView LvMenuList;
    private ArrayList<getDataMenu> menuDetails;
    private static MenuListAdapter adapter;
    private Spinner SpinStatus;
    private Boolean edit;
    public int Position;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_menu_ref, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editMenu = new Dialog(getActivity());
        addMenu = (FloatingActionButton) getActivity().findViewById(R.id.addMenu);
        LvMenuList = (ListView) getActivity().findViewById(R.id.LvMenuList);
        menuDetails = new ArrayList<>();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        displayMenu = mFirebaseDatabase.getReference().child("TableService").child("Menu");
        setMenu = mFirebaseDatabase.getReference().child("TableService").child("Menu");
        addMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit = false;
                openDialog(edit);
            }
        });

        LvMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Position = position;
                edit = true;
                openDialog(edit);
            }
        });

    }

    private void DisplayMenu(Map<String, Object> dataSnapshot) {
        menuDetails.clear();
        final ArrayList<String> Name = new ArrayList<>();
        for (Map.Entry<String, Object> entry : dataSnapshot.entrySet()) {
            Map name = (Map) entry.getValue();
            Name.add((String) name.get("Name"));
        }

        final ArrayList<String> Price = new ArrayList<>();
        for (Map.Entry<String, Object> entry : dataSnapshot.entrySet()) {
            Map price = (Map) entry.getValue();
            Price.add((String) price.get("Price"));
        }

        final ArrayList<String> Status = new ArrayList<>();
        for (Map.Entry<String, Object> entry : dataSnapshot.entrySet()) {
            Map status = (Map) entry.getValue();
            Status.add((String) status.get("Status"));
        }

        final ArrayList<String> MenuId = new ArrayList<>();
        for (Map.Entry<String, Object> entry : dataSnapshot.entrySet()) {
            Map menuId = (Map) entry.getValue();
            MenuId.add((String) menuId.get("MenuId"));
        }

        try {
            int i = 0;
            while (MenuId.size() > i) {
                menuDetails.add(new getDataMenu(Name.get(i), Price.get(i), Status.get(i), MenuId.get(i)));
                i++;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        adapter = new MenuListAdapter(menuDetails, getActivity());
        LvMenuList.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        menuDetails.clear();
        displayMenu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DisplayMenu((Map<String, Object>) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openDialog(Boolean edit) {
        editMenu.setContentView(R.layout.add_or_update_menu);

        progressDialog = new ProgressDialog(getActivity());
        etMenuName = (EditText) editMenu.findViewById(R.id.etMenuName);
        etMenuPrice = (EditText) editMenu.findViewById(R.id.etMenuPrice);
        btnSubmit = (Button) editMenu.findViewById(R.id.btnSubmit);
        btnDelete = (Button) editMenu.findViewById(R.id.btnDelete);
        SpinStatus = (Spinner) editMenu.findViewById(R.id.SpinStatus);
        String[] Kelas = new String[]{"Available", "Unavailable"};
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_style_light, Kelas);
        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinStatus.setAdapter(mArrayAdapter);

        if (edit) {
            etMenuName.setText(menuDetails.get(Position).getName());
            etMenuPrice.setText(menuDetails.get(Position).getPrice());
            if (menuDetails.get(Position).getStatus().equals("Available")) {
                SpinStatus.setSelection(0);
            } else {
                SpinStatus.setSelection(1);
            }
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String menuName = etMenuName.getText().toString().trim();
                    String menuPrice = etMenuPrice.getText().toString().trim();
                    String status = SpinStatus.getSelectedItem().toString();
                    String menuId = menuDetails.get(Position).getMenuId();
                    if (!menuId.isEmpty() && !menuName.isEmpty() && !menuPrice.isEmpty() && !status.isEmpty()) {
                        CreateMenu(menuName, menuPrice, status, menuId);
                    } else {
                        Toast.makeText(getActivity(), "Please Add All Field", Toast.LENGTH_SHORT).show();
                    }

                    editMenu.dismiss();
                }
            });

        } else {
            btnDelete.setVisibility(View.INVISIBLE);
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialog.setTitle("Please Wait");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    String menuName = etMenuName.getText().toString().trim();
                    String menuPrice = etMenuPrice.getText().toString().trim();
                    String menuId = setMenu.push().getKey();
                    String status = SpinStatus.getSelectedItem().toString();

                    if (!menuId.isEmpty() && !menuName.isEmpty() && !menuPrice.isEmpty() && !status.isEmpty()) {
                        CreateMenu(menuName, menuPrice, status, menuId);
                    } else {
                        Toast.makeText(getActivity(), "Please Add All Field", Toast.LENGTH_SHORT).show();
                    }

                    editMenu.dismiss();
                }
            });
        }

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setTitle("Please Wait");
                progressDialog.setCancelable(false);
                progressDialog.show();
                String menuId = menuDetails.get(Position).getMenuId();
                setMenu.child(menuId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        editMenu.dismiss();
                        Toast.makeText(getActivity(), "Success Delete Menu", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        editMenu.setOnDismissListener(this);
        editMenu.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editMenu.show();
    }

    private void CreateMenu(String menuName, String menuPrice, String status, String menuId) {
        root.example.com.monggopinarak.DataModel.setMenu setDataMenu = new setMenu(menuName, menuPrice, status, menuId);
        setMenu.child(menuId).setValue(setDataMenu).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (edit) {
                    Toast.makeText(getActivity(), "Menu Edited", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Menu Added", Toast.LENGTH_SHORT).show();
                }
                etMenuName.setText("");
                etMenuPrice.setText("");
                edit = false;
                progressDialog.dismiss();
            }
        });

    }

    public AddMenuRef newInstance() {
        return new AddMenuRef();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        edit = false;
    }
}
