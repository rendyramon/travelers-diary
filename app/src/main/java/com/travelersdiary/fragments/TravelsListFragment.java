package com.travelersdiary.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.TravelActivity;
import com.travelersdiary.adapters.TravelsListAdapter;
import com.travelersdiary.dialogs.EditTravelDialog;
import com.travelersdiary.models.Travel;
import com.travelersdiary.recyclerview.DividerItemDecoration;
import com.travelersdiary.recyclerview.FirebaseContextMenuRecyclerView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TravelsListFragment extends Fragment {

    //    @Bind(R.id.travels_list)
//    RecyclerView mTravelsList;
    private FirebaseContextMenuRecyclerView mTravelsList;

    private FirebaseRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_travels_list, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTravelsList = (FirebaseContextMenuRecyclerView) view.findViewById(R.id.travels_list);

        mLayoutManager = new LinearLayoutManager(getContext());
        mTravelsList.setLayoutManager(mLayoutManager);

        // animation
        mTravelsList.setItemAnimator(new DefaultItemAnimator());

        // decoration
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext());
        mTravelsList.addItemDecoration(itemDecoration);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        new Firebase(Utils.getFirebaseUserTravelsUrl(userUID))
                .child(Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Travel travel = dataSnapshot.getValue(Travel.class);
                        if (travel == null) {
                            // first start
                            travel = new Travel();
                            travel.setTitle(getString(R.string.default_travel_title));
                            travel.setDescription(getString(R.string.default_travel_description));
                            travel.setStart(-1);
                            travel.setStop(-1);
                            travel.setActive(false);

                            new Firebase(Utils.getFirebaseUserTravelsUrl(userUID))
                                    .child(Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY)
                                    .setValue(travel);
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e("firebase", "firebase onCancelled: " + firebaseError.getMessage());
                    }
                });

        Firebase mFirebaseRef = new Firebase(Utils.getFirebaseUserTravelsUrl(userUID));
        Query query;

        query = mFirebaseRef.orderByKey();

        mAdapter = new TravelsListAdapter(query);
        mTravelsList.setAdapter(mAdapter);

        ((TravelsListAdapter) mAdapter).setOnItemClickListener(new TravelsListAdapter.OnItemClickListener () {
            @Override
            public void onItemClick(View view, int position) {
                String key = mAdapter.getRef(position).getKey();
                Travel travel = ((TravelsListAdapter) mAdapter).getItem(position);

                Intent intent = new Intent(getActivity(), TravelActivity.class);
                intent.putExtra(Constants.KEY_TRAVEL_REF, key);
                intent.putExtra(Constants.KEY_TRAVEL_TITLE, travel.getTitle());
                startActivity(intent);
            }
        });

        registerForContextMenu(mTravelsList);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.travel_list_item_context, menu);
        menu.setHeaderTitle("Select The Action");

        FirebaseContextMenuRecyclerView.FirebaseRecyclerViewContextMenuInfo info =
                (FirebaseContextMenuRecyclerView.FirebaseRecyclerViewContextMenuInfo) menuInfo;

        if (info != null && Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY.equals(info.ref.getKey())) {
            menu.findItem(R.id.menu_item_delete).setVisible(false);
            menu.findItem(R.id.menu_item_edit).setVisible(false);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        FirebaseContextMenuRecyclerView.FirebaseRecyclerViewContextMenuInfo info =
                (FirebaseContextMenuRecyclerView.FirebaseRecyclerViewContextMenuInfo) item.getMenuInfo();
        if (info != null) {
            Travel travel = ((TravelsListAdapter) mAdapter).getItem(info.position);

            switch (item.getItemId()) {
                case R.id.menu_item_set_active:
                    // TODO: 25.03.16 add logic 
                    return true;
                case R.id.menu_item_open:
                    Intent intent = new Intent(getActivity(), TravelActivity.class);
                    intent.putExtra(Constants.KEY_TRAVEL_REF, info.ref.getKey());
                    intent.putExtra(Constants.KEY_TRAVEL_TITLE, travel.getTitle());
                    startActivity(intent);
                    return true;
                case R.id.menu_item_edit:
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    EditTravelDialog addTravelDialog = new EditTravelDialog();

                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.KEY_TRAVEL_REF, info.ref.getKey());
                    bundle.putString(Constants.KEY_TRAVEL_TITLE, travel.getTitle());
                    bundle.putString(Constants.KEY_TRAVEL_DESCRIPTION, travel.getDescription());
                    addTravelDialog.setArguments(bundle);

                    addTravelDialog.show(fragmentManager, "edit_travel_dialog");
                    return true;
                case R.id.menu_item_delete:
                    // TODO: 25.03.16 add warning dialog and more logic for not empty travel
                    info.ref.removeValue();
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        mAdapter.cleanup();
        super.onDestroyView();
    }

}
