package com.example.evee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class halamannotes extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.halamannotes, container, false);

        EditText editTextNote = view.findViewById(R.id.editTextNote);
        Button buttonSave = view.findViewById(R.id.buttonSaveNote);

        buttonSave.setOnClickListener(v -> {
            String note = editTextNote.getText().toString();
            if (!note.isEmpty()) {
                Toast.makeText(getActivity(), "Catatan disimpan: " + note, Toast.LENGTH_SHORT).show();
                editTextNote.setText("");
            } else {
                Toast.makeText(getActivity(), "Catatan kosong!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
