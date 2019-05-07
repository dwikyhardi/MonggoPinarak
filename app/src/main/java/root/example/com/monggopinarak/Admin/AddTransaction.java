package root.example.com.monggopinarak.Admin;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import root.example.com.monggopinarak.Adapter.ListViewAdapter;
import root.example.com.monggopinarak.Adapter.OrderAdapter;
import root.example.com.monggopinarak.DataModel.getData;
import root.example.com.monggopinarak.DataModel.getDataMenu;
import root.example.com.monggopinarak.DataModel.setDataMenu;
import root.example.com.monggopinarak.R;

public class AddTransaction extends Fragment implements DialogInterface.OnDismissListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_transaction, container, false);
    }

    private final String TAG = "AddTransaction";

    private ListView lvUnpayed;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference getUnpayed, getUnpayedDetail, getUser;
    private ArrayList<String> TableNumber, unPayedMenu, totalPrice, UserId, orderId;
    private Dialog addTransactionDialog;
    private ListView lvDoneOrder;
    private ArrayList<getDataMenu> ChoosedMenu;
    private static OrderAdapter adapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lvUnpayed = (ListView) getActivity().findViewById(R.id.lvUnpayed);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        getUnpayed = mFirebaseDatabase.getReference().child("TableService").child("Order");


        TableNumber = new ArrayList<>();
        unPayedMenu = new ArrayList<>();
        totalPrice = new ArrayList<>();
        ChoosedMenu = new ArrayList<>();
        orderId = new ArrayList<>();
        UserId = new ArrayList<>();
        addTransactionDialog = new Dialog(getActivity());

        getUnpayed.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TableNumber.clear();
                unPayedMenu.clear();
                totalPrice.clear();
                ChoosedMenu.clear();
                orderId.clear();
                UserId.clear();

                unPayedOrder((Map<String, Object>) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        lvUnpayed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                displayDetailOrder(position);
            }
        });
    }

    private void displayDetailOrder(final int position) {
        addTransactionDialog.setContentView(R.layout.add_transaction_dialog);
        addTransactionDialog.setOnDismissListener(this);

        lvDoneOrder = (ListView) addTransactionDialog.findViewById(R.id.lvDoneOrder);
        TextView close = (TextView) addTransactionDialog.findViewById(R.id.close);
        Button btnPay = (Button) addTransactionDialog.findViewById(R.id.btnPay);
        TextView tvHarga = (TextView) addTransactionDialog.findViewById(R.id.tvHarga);
        final TextView personName = (TextView) addTransactionDialog.findViewById(R.id.PersonName);
        final TextView TableNumber = (TextView) addTransactionDialog.findViewById(R.id.TableNumber);
        String[] menuId = unPayedMenu.get(position)
                .replace("]", "")
                .replace("[", "")
                .replace(" ", "")
                .split(",");

        getUnpayedDetail = mFirebaseDatabase.getReference().child("TableService").child("Menu");
        int i = 0;
        while (menuId.length > i) {
            Log.d(TAG, "displayDetailOrder() returned: " + menuId.length + " " + menuId[i]);
            getUnpayedDetail.child(menuId[i]).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    isiStruk(dataSnapshot);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            i++;
        }

        getUser = mFirebaseDatabase.getReference().child("TableService").child("User");
        try {
            getUser.child("Customer").child(UserId.get(position)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        getData mGetData = new getData();
                        mGetData.setName(dataSnapshot.getValue(getData.class).getName());
                        personName.setText(mGetData.getName());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (NullPointerException e) {
            try {
                getUser.child("Waiter").child(UserId.get(position)).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            getData mGetData = new getData();
                            mGetData.setName(dataSnapshot.getValue(getData.class).getName());
                            personName.setText(mGetData.getName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } catch (NullPointerException c) {
                try {
                    getUser.child("Admin").child(UserId.get(position)).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                getData mGetData = new getData();
                                mGetData.setName(dataSnapshot.getValue(getData.class).getName());
                                personName.setText(mGetData.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } catch (NullPointerException m) {
                    m.printStackTrace();
                }
            }
        }


        tvHarga.setText(totalPrice.get(position));
        TableNumber.setText(this.TableNumber.get(position));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTransactionDialog.dismiss();
                ChoosedMenu.clear();
            }
        });

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUnpayed.child(orderId.get(position)).child("Status").setValue("Payed").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Success Pay Order.", Toast.LENGTH_SHORT).show();
                        ChoosedMenu.clear();
                        addTransactionDialog.dismiss();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.detach(AddTransaction.this).attach(AddTransaction.this).commit();
                    }
                });
            }
        });

        addTransactionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        addTransactionDialog.show();
    }

    private void isiStruk(DataSnapshot dataSnapshot) {
        setDataMenu menu = new setDataMenu();
        menu.setMenuId(dataSnapshot.getValue(setDataMenu.class).getMenuId());
        menu.setName(dataSnapshot.getValue(setDataMenu.class).getName());
        menu.setStatus(dataSnapshot.getValue(setDataMenu.class).getStatus());
        menu.setPrice(dataSnapshot.getValue(setDataMenu.class).getPrice());

        String name, price, status, menuId;
        name = menu.getName();
        price = menu.getPrice();
        status = menu.getStatus();
        menuId = menu.getMenuId();

        ChoosedMenu.add(new getDataMenu(name, price, status, menuId));

        adapter = new OrderAdapter(ChoosedMenu, getActivity());
        lvDoneOrder.setAdapter(adapter);
    }

    private void unPayedOrder(Map<String, Object> dataSnapshot) {
        final ArrayList<String> OrderDate = new ArrayList<>();
        for (Map.Entry<String, Object> entry : dataSnapshot.entrySet()) {
            Map orderDate = (Map) entry.getValue();
            OrderDate.add((String) orderDate.get("OrderDate"));
        }
        final ArrayList<String> OrderId = new ArrayList<>();
        for (Map.Entry<String, Object> entry : dataSnapshot.entrySet()) {
            Map orderId = (Map) entry.getValue();
            OrderId.add((String) orderId.get("OrderId"));
        }
        final ArrayList<String> OrderedMenu = new ArrayList<>();
        for (Map.Entry<String, Object> entry : dataSnapshot.entrySet()) {
            Map orderedMenu = (Map) entry.getValue();
            OrderedMenu.add((String) orderedMenu.get("OrderedMenu"));
        }
        final ArrayList<String> OrderStatus = new ArrayList<>();
        for (Map.Entry<String, Object> entry : dataSnapshot.entrySet()) {
            Map orderStatus = (Map) entry.getValue();
            OrderStatus.add((String) orderStatus.get("Status"));
        }
        final ArrayList<String> TableNumber = new ArrayList<>();
        for (Map.Entry<String, Object> entry : dataSnapshot.entrySet()) {
            Map tableNumber = (Map) entry.getValue();
            TableNumber.add((String) tableNumber.get("TableNumber"));
        }
        final ArrayList<String> TotalPrice = new ArrayList<>();
        for (Map.Entry<String, Object> entry : dataSnapshot.entrySet()) {
            Map totalPrice = (Map) entry.getValue();
            TotalPrice.add((String) totalPrice.get("TotalPrice"));
        }
        final ArrayList<String> UserId = new ArrayList<>();
        for (Map.Entry<String, Object> entry : dataSnapshot.entrySet()) {
            Map userId = (Map) entry.getValue();
            UserId.add((String) userId.get("UserId"));
        }

        try {
            int i = 0;
            while (OrderId.size() > i) {
                if (OrderStatus.get(i).equals("Unpayed")) {
                    this.TableNumber.add(TableNumber.get(i));
                    this.unPayedMenu.add(OrderedMenu.get(i));
                    this.totalPrice.add(TotalPrice.get(i));
                    this.UserId.add(UserId.get(i));
                    this.orderId.add(OrderId.get(i));
                }
                i++;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        ArrayAdapter mArrayAdapter = new ArrayAdapter(getActivity(), R.layout.listview, this.TableNumber);
        lvUnpayed.setAdapter(mArrayAdapter);
    }

    public AddTransaction newInstance() {
        return new AddTransaction();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        ChoosedMenu.clear();
    }
}
