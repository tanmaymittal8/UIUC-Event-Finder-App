package edu.uiuc.cs427app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private TextView usernameDisplay;
    private EditText nameInput;
    private EditText bioInput;
    private Button saveButton;
    private AuthenticationManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 1. Initialize Views
        usernameDisplay = view.findViewById(R.id.text_username_display);
        nameInput = view.findViewById(R.id.input_name);
        bioInput = view.findViewById(R.id.input_bio);
        saveButton = view.findViewById(R.id.btn_save_profile);

        // 2. Get Auth Manager instance
        authManager = AuthenticationManager.getInstance(requireContext());

        // 3. Populate Data if User is Logged In
        if (authManager.isLoggedIn()) {
            User currentUser = authManager.getCurrentUser();

            // Set the read-only username
            usernameDisplay.setText("@" + currentUser.getUsername());

            // Pre-fill name and bio if they already exist
            if (currentUser.getName() != null) {
                nameInput.setText(currentUser.getName());
            }
            if (currentUser.getBio() != null) {
                bioInput.setText(currentUser.getBio());
            }
        }

        // 4. Handle Save Button
        saveButton.setOnClickListener(v -> {
            String newName = nameInput.getText().toString().trim();
            String newBio = bioInput.getText().toString().trim();

            if (authManager.isLoggedIn()) {
                boolean success = authManager.updateUserProfile(newName, newBio);
                if (success) {
                    Toast.makeText(getActivity(), "Profile Saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Error saving profile. Check Database.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}