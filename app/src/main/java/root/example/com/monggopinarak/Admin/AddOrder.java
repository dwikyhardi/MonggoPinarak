package root.example.com.monggopinarak.Admin;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import root.example.com.monggopinarak.Adapter.ListViewAdapter;
import root.example.com.monggopinarak.Adapter.OrderAdapter;
import root.example.com.monggopinarak.DataModel.getDataMenu;
import root.example.com.monggopinarak.R;

import static android.support.constraint.Constraints.TAG;

public class AddOrder extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_order, container, false);
    }

    private ListView MenuList;
    private ArrayList<getDataMenu> menuDetails;
    private static ListViewAdapter adapter;
    private static OrderAdapter mOrderAdapter;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference displayMenu, Order;
    private ArrayList<getDataMenu> ChoosedMenu;
    private Dialog Notif, doneOrder;
    private Button AddToOrder;
    private ListView lvDoneOrder;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String UserId;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MenuList = (ListView) getActivity().findViewById(R.id.MenuList);
        AddToOrder = (Button) getActivity().findViewById(R.id.AddToOrder);
        lvDoneOrder = (ListView) getActivity().findViewById(R.id.lvDoneOrder);
        menuDetails = new ArrayList<>();
        ChoosedMenu = new ArrayList<>();
        ChoosedMenu.clear();
        menuDetails.clear();

        doneOrder = new Dialog(getActivity());
        Notif = new Dialog(getActivity());
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        displayMenu = mFirebaseDatabase.getReference().child("TableService").child("Menu");
        Order = mFirebaseDatabase.getReference().child("TableService").child("Order");

        MenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShowNotif(position, view);
            }
        });

        AddToOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ChoosedMenu.isEmpty()){
                    DoneOrder(v);
                }else {
                    Toast.makeText(getActivity(), "Please Add A Menu", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mFirebaseAuth = FirebaseAuth.getInstance();
        UserId = mFirebaseAuth.getCurrentUser().getUid();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    UserId = user.getUid();
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }


    private void DoneOrder(View view) {
        doneOrder.setContentView(R.layout.done_order);
        lvDoneOrder = (ListView) doneOrder.findViewById(R.id.lvDoneOrder);
        Button btnPay = (Button) doneOrder.findViewById(R.id.btnPay);
        TextView tvHarga = (TextView) doneOrder.findViewById(R.id.tvHarga);
        final EditText TableNumber = (EditText) doneOrder.findViewById(R.id.TableNumber);

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        final String formattedDate = df.format(c);

        int i = 0;
        int price = 0;
        while (ChoosedMenu.size() > i) {
            price = Integer.parseInt(ChoosedMenu.get(i).getPrice()) + price;
            i++;
        }

        tvHarga.setText(String.valueOf(price));
        mOrderAdapter = new OrderAdapter(ChoosedMenu, getActivity().getApplicationContext());
        lvDoneOrder.setAdapter(mOrderAdapter);
        final ArrayList<String> idOrder = new ArrayList<>();
        idOrder.clear();

        final int finalPrice = price;
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Table = TableNumber.getText().toString().trim();
                if (!Table.equals("")) {
                    String key = Order.push().getKey();
                    int i = 0;
                    while (ChoosedMenu.size() > i) {
                        idOrder.add(ChoosedMenu.get(i).getMenuId());
                        i++;
                    }

                    Order.child(key).child("UserId").setValue(UserId);
                    Order.child(key).child("TableNumber").setValue(Table);
                    Order.child(key).child("OrderId").setValue(key);
                    Order.child(key).child("TotalPrice").setValue(String.valueOf(finalPrice));
                    Order.child(key).child("OrderDate").setValue(formattedDate);
                    Order.child(key).child("Status").setValue("Unpayed");
                    Order.child(key).child("OrderedMenu").setValue(idOrder.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(),
                                    "Success Adding Order. Please Wait Until the Order Come to Your Table",
                                    Toast.LENGTH_LONG).show();
                            ChoosedMenu.clear();
                            doneOrder.dismiss();
                            idOrder.clear();
                        }
                    });

                } else {
                    Toast.makeText(getActivity(), "Please Add Table Number", Toast.LENGTH_SHORT).show();
                }
            }
        });
        doneOrder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        doneOrder.show();
    }

    private void ShowNotif(final int position, View view) {
        Notif.setContentView(R.layout.verif_order);
        Button btnYes, btnNo;
        TextView tvMenuName = (TextView) Notif.findViewById(R.id.tvMenuName);
        btnYes = (Button) Notif.findViewById(R.id.btnYes);
        btnNo = (Button) Notif.findViewById(R.id.btnNo);

        tvMenuName.setText(menuDetails.get(position).getName());
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Notif.dismiss();
                Notif.cancel();
            }
        });
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String MenuId = menuDetails.get(position).getMenuId();
                String MenuName = menuDetails.get(position).getName();
                String Status = menuDetails.get(position).getStatus();
                String Price = menuDetails.get(position).getPrice();
                ChoosedMenu.add(new getDataMenu(MenuName, Price, Status, MenuId));
                Notif.cancel();
                Notif.dismiss();
            }
        });

        Notif.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Notif.show();
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
                if (Status.get(i).equals("Available")) {
                    menuDetails.add(new getDataMenu(Name.get(i), Price.get(i), Status.get(i), MenuId.get(i)));
                }
                i++;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        adapter = new ListViewAdapter(menuDetails, getActivity());
        MenuList.setAdapter(adapter);
    }

    public AddOrder newInstance() {
        return new AddOrder();
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
}
