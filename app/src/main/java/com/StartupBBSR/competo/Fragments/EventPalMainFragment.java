package com.StartupBBSR.competo.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.StartupBBSR.competo.Activity.ChatDetailActivity;
import com.StartupBBSR.competo.Adapters.EventPalUserAdapter;
import com.StartupBBSR.competo.Models.EventPalModel;
import com.StartupBBSR.competo.Models.RequestModel;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.AlertlayoutrequestBinding;
import com.StartupBBSR.competo.databinding.FragmentEventPalMainBinding;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

public class EventPalMainFragment extends Fragment {

    public static final String TAG = "sheet";

    private FirebaseFirestore firestoreDB;
    private FirebaseAuth firebaseAuth;
    private String userID;

    private Constant constant;

    private EventPalUserAdapter adapter;

    private CollectionReference collectionReference;
    private FirestoreRecyclerOptions<EventPalModel> options;

    private EventPalModel eventPalModel;
    private Query query;

    private NavController navController;

    private FragmentEventPalMainBinding binding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
//                Do nothing on back pressed
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEventPalMainBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getUid();

        constant = new Constant();

        collectionReference = firestoreDB.collection(constant.getUsers());

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }
        });

        initData();
        initRecycler();

        return view;
    }

    private void search(String newText) {
        Query userSearchQuery = collectionReference
                .orderBy(constant.getUserNameField())
                .whereGreaterThanOrEqualTo(constant.getUserNameField(), newText);

        options = new FirestoreRecyclerOptions.Builder<EventPalModel>()
                .setQuery(userSearchQuery, EventPalModel.class)
                .build();

        initRecycler();
    }

    private void initData() {

        query = collectionReference.orderBy(constant.getUserIdField())
                .whereNotEqualTo(constant.getUserIdField(), userID);

        options = new FirestoreRecyclerOptions.Builder<EventPalModel>()
                .setQuery(query, EventPalModel.class)
                .build();
    }

    private void initRecycler() {
        SnapHelper snapHelper = new LinearSnapHelper();
        RecyclerView recyclerView = binding.eventPalRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL   , false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(recyclerView);


        adapter = new EventPalUserAdapter(getContext(), options);
        adapter.setOnItemClickListener(new EventPalUserAdapter.OnItemClickListener() {
            @Override
            public void onButtonClick(DocumentSnapshot snapshot) {
                EventPalModel model = snapshot.toObject(EventPalModel.class);

                firestoreDB.collection(constant.getChatConnections()).document(userID)
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot connectionListSnapshot = task.getResult();
                            List<String> connectionList = (List<String>) connectionListSnapshot.get("Connections");
                            if (connectionList != null) {
                                if (!connectionList.contains(model.getUserID())) {
//                                    Create new request
                                    showCreateRequestDialog(model);
                                } else {
//                                    Chat already present
                                    Intent intent = new Intent(getContext(), ChatDetailActivity.class);
                                    intent.putExtra("receiverID", model.getUserID());
                                    intent.putExtra("receiverName", model.getName());
                                    intent.putExtra("receiverPhoto", model.getPhoto());
                                    startActivity(intent);
                                }
                            } else {
                                showCreateRequestDialog(model);
                            }
                        }
                    }
                });
            }

            /*@Override
            public void onBottomSheetToggleClick(View itemView, int position) {

                View bottomSheet = itemView.findViewById(R.id.EventPalBottomSheet);
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                // ImageView btnBottomSheet = itemView.findViewById(R.id.btnBottomSheet);

                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {

                    Log.d(TAG, "onButtonClick: STATE_COLLAPSED");
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    itemView.findViewById(R.id.tvEventPalUserAbout).setVisibility(View.VISIBLE);
                    // btnBottomSheet.setImageResource(R.drawable.down_arrow);

                } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

                    Log.d(TAG, "onButtonClick: STATE_EXPANDED");
                    itemView.findViewById(R.id.tvEventPalUserAbout).setVisibility(View.GONE);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    //  btnBottomSheet.setImageResource(R.drawable.ic_upperarrow);

                }
            }*/
        });

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void showCreateRequestDialog(EventPalModel model) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertlayoutrequestBinding alertlayoutrequestBinding = AlertlayoutrequestBinding.inflate(getLayoutInflater());
        View alertView = alertlayoutrequestBinding.getRoot();
        builder.setView(alertView);
        builder.setTitle("Connect with " + model.getName())
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (!alertlayoutrequestBinding.input.getText().toString().trim().isEmpty()) {

                            String requestMesssage = alertlayoutrequestBinding.input.getText().toString().trim();
                            RequestModel requestModel = new RequestModel(userID, requestMesssage, new Date().getTime());

                            firestoreDB.collection(constant.getRequests())
                                    .document(model.getUserID())
                                    .collection(constant.getRequests())
                                    .document(userID)
                                    .set(requestModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getContext(), "Request Sent", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}