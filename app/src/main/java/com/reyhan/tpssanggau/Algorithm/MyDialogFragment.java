package com.reyhan.tpssanggau.Algorithm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.reyhan.tpssanggau.R;

public class MyDialogFragment extends DialogFragment {
    TextView deskripsi;
    TextView aturan_waktu;
    DatabaseReference mDatabase;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        deskripsi = view.findViewById(R.id.desc);
        aturan_waktu = view.findViewById(R.id.time);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("aturan").child("-NAGCFwiW0eZXHYmUDMj");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                deskripsi.setText(snapshot.child("deskripsi").getValue().toString());
                aturan_waktu.setText(snapshot.child("aturan_waktu").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.popup,container,false);
    }
}
