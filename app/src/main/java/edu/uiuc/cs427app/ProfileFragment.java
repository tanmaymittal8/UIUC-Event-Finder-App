package edu.uiuc.cs427app; // Changed to match your project package

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private EditText nameInput;
    private EditText bioInput;
    private Button saveButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Inflate the layout
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 2. Find the views (buttons/text boxes)
        nameInput = view.findViewById(R.id.input_name);
        bioInput = view.findViewById(R.id.input_bio);
        saveButton = view.findViewById(R.id.btn_save_profile);

        // 3. Set up the button click listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getText().toString();
                // In a real app, you would save this 'name' to a database here.

                // Show a temporary message
                Toast.makeText(getActivity(), "Profile Saved for " + name, Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}