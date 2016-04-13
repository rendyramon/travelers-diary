package com.travelersdiary.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.google.gson.Gson;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.EditTravelActivity;
import com.travelersdiary.activities.TravelActivity;
import com.travelersdiary.adapters.TravelsListAdapter;
import com.travelersdiary.models.Travel;
import com.travelersdiary.models.TravelersDiary;
import com.travelersdiary.recyclerview.DividerItemDecoration;
import com.travelersdiary.recyclerview.FirebaseContextMenuRecyclerView;
import com.travelersdiary.services.LocationTrackingService;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TravelsListFragment extends Fragment {

    @Bind(R.id.travels_list)
    FirebaseContextMenuRecyclerView mTravelList;

    private FirebaseRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_travel_list, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLayoutManager = new LinearLayoutManager(getContext());
        mTravelList.setLayoutManager(mLayoutManager);

        // animation
        mTravelList.setItemAnimator(new DefaultItemAnimator());

        // decoration
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext());
        mTravelList.addItemDecoration(itemDecoration);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        // check first start and store demo data
        new Firebase(Utils.getFirebaseUserTravelsUrl(userUID))
                .child(Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Firebase userDataRef = dataSnapshot
                                .getRef() // root/travels/default
                                .getParent() // root/travels
                                .getParent(); // root
                        Travel travel = dataSnapshot.getValue(Travel.class);
                        if (travel == null) {
                            // first start
                            /*
                            // minimum - default travel
                            travel = new Travel();
                            travel.setTitle(getString(R.string.default_travel_title));
                            travel.setDescription(getString(R.string.default_travel_description));
                            travel.setCreationTime(-1);
                            travel.setStart(-1);
                            travel.setStop(-1);
                            travel.setActive(false);

                            Strong cover = ContentResolver.SCHEME_ANDROID_RESOURCE +
                                    "://" + getResources().getResourcePackageName(R.drawable.travel_cover_default)
                                    + '/' + getResources().getResourceTypeName(R.drawable.travel_cover_default)
                                    + '/' + getResources().getResourceEntryName(R.drawable.travel_cover_default);
                            travel.setCover(cover);

                            new Firebase(Utils.getFirebaseUserTravelsUrl(userUID))
                                    .child(Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY)
                                    .setValue(travel);
                            */

                            try {
                                InputStream is = getActivity().getAssets().open("demodata.json");
                                int size = is.available();
                                byte[] buffer = new byte[size];
                                is.read(buffer);
                                is.close();
                                String bufferString = new String(buffer);
                                Gson gson = new Gson();
                                TravelersDiary travelersDiary = gson.fromJson(bufferString, TravelersDiary.class);

                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                                travelersDiary.setName(sharedPreferences.getString(Constants.KEY_DISPLAY_NAME, null));
                                travelersDiary.setEmail(sharedPreferences.getString(Constants.KEY_EMAIL, null));

                                userDataRef.setValue(travelersDiary);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

        Firebase mFirebaseRef = new Firebase(Utils.getFirebaseUserTravelsUrl(userUID));
        Query query;

        query = mFirebaseRef.orderByChild(Constants.FIREBASE_TRAVEL_CREATION_TIME).startAt(0);

        mAdapter = new TravelsListAdapter(getContext(), query);
        mTravelList.setAdapter(mAdapter);

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

        registerForContextMenu(mTravelList);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.travel_list_item_context, menu);
        menu.setHeaderTitle(R.string.travels_select_action_text);

        FirebaseContextMenuRecyclerView.FirebaseRecyclerViewContextMenuInfo info =
                (FirebaseContextMenuRecyclerView.FirebaseRecyclerViewContextMenuInfo) menuInfo;
        if (info != null) {
            Travel travel = ((TravelsListAdapter) mAdapter).getItem(info.position);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String activeTravel = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_KEY, null);
            menu.findItem(R.id.menu_item_start).setVisible(!info.ref.getKey().equals(activeTravel));
            menu.findItem(R.id.menu_item_stop).setVisible(travel.getStart() > 0 && travel.getStop() < 0);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final FirebaseContextMenuRecyclerView.FirebaseRecyclerViewContextMenuInfo info =
                (FirebaseContextMenuRecyclerView.FirebaseRecyclerViewContextMenuInfo) item.getMenuInfo();
        if (info != null) {
            Travel travel = ((TravelsListAdapter) mAdapter).getItem(info.position);

            switch (item.getItemId()) {
                case R.id.menu_item_start:
                    startTravel(info.ref.getKey(), travel.getTitle());
                    return true;
                case R.id.menu_item_stop:
                    stopTravel(info.ref.getKey());
                    return true;
                case R.id.menu_item_open:
                    Intent intent = new Intent(getActivity(), TravelActivity.class);
                    intent.putExtra(Constants.KEY_TRAVEL_REF, info.ref.getKey());
                    intent.putExtra(Constants.KEY_TRAVEL_TITLE, travel.getTitle());
                    startActivity(intent);
                    return true;
                case R.id.menu_item_edit:
                    Intent editIntent = new Intent(getActivity(), EditTravelActivity.class);
                    editIntent.putExtra(Constants.KEY_TRAVEL_REF, info.ref.getKey());
                    editIntent.putExtra(Constants.KEY_TRAVEL_TITLE, travel.getTitle());
                    editIntent.putExtra(Constants.KEY_TRAVEL_DESCRIPTION, travel.getDescription());
                    editIntent.putExtra(Constants.FIREBASE_TRAVEL_DEFAULT_COVER, travel.getDefaultCover());
                    editIntent.putExtra(Constants.FIREBASE_TRAVEL_USER_COVER, travel.getUserCover());
                    startActivity(editIntent);
                    return true;
                case R.id.menu_item_delete:
                    if (Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY.equals(info.ref.getKey())) {
                        return true;
                    }

                    new AlertDialog.Builder(getActivity())
                            .setInverseBackgroundForced(true)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(getContext().getString(R.string.travels_delete_question_text))
                            .setMessage(getContext().getString(R.string.travels_delete_warning_text))
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                                    String activeTravel = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_KEY, null);
                                    String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

                                    // delete reminder items
                                    new Firebase(Utils.getFirebaseUserReminderUrl(userUID))
                                            .orderByChild(Constants.FIREBASE_REMINDER_TRAVELID)
                                            .equalTo(info.ref.getKey())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                                                        child.getRef().removeValue();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(FirebaseError firebaseError) {
                                                }
                                            });

                                    if (activeTravel != null && activeTravel.equals(info.ref.getKey())) {
                                        // stop tracking
                                        Intent intentStopTracking = new Intent(getContext(), LocationTrackingService.class);
                                        intentStopTracking.setAction(LocationTrackingService.ACTION_STOP_TRACK);
                                        getContext().startService(intentStopTracking);

                                        // TODO: 06.04.16 remove notifications for current active travel
                                        //
                                        sharedPreferences.edit()
                                                .putString(Constants.KEY_ACTIVE_TRAVEL_KEY, null)
                                                .apply();
                                        sharedPreferences.edit()
                                                .putString(Constants.KEY_ACTIVE_TRAVEL_TITLE, null)
                                                .apply();
                                        startTravel(Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY, getString(R.string.default_travel_title));
                                    }
                                    // delete tracks
                                    new Firebase(Utils.getFirebaseUserTracksUrl(userUID))
                                            .child(info.ref.getKey())
                                            .removeValue();
                                    // delete travel
                                    info.ref.removeValue();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .show();
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


    private void startTravel(String travelKey, String travelTitle) {
        /*
          For switch active travel logic see listener mActiveTravelListener in BaseActivity class
        */

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);
        String activeTravelKey = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_KEY, null);
        if (activeTravelKey != null && !Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY.equals(activeTravelKey)) {
            setStopTime(activeTravelKey);
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.FIREBASE_ACTIVE_TRAVEL_TITLE, travelTitle);
        map.put(Constants.FIREBASE_ACTIVE_TRAVEL_KEY, travelKey);
        Firebase activeTravelRef = new Firebase(Utils.getFirebaseUserActiveTravelUrl(userUID));
        activeTravelRef.setValue(map);
    }

    private void stopTravel(String travelKey) {
        if (!Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY.equals(travelKey)) {
            setStopTime(travelKey);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String activeTravelKey = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_KEY, null);
            if (activeTravelKey != null && activeTravelKey.equals(travelKey)) {
                startTravel(Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY, getString(R.string.default_travel_title));
            }
        }
    }

    private void setStopTime(String travelKey) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.FIREBASE_TRAVEL_STOP_TIME, System.currentTimeMillis());
        new Firebase(Utils.getFirebaseUserTravelsUrl(userUID))
                .child(travelKey)
                .updateChildren(map);
    }
}
