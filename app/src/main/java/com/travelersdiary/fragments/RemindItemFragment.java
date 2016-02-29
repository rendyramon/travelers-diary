package com.travelersdiary.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.adapters.RemindTypesAdapter;
import com.travelersdiary.adapters.TodoTaskAdapter;
import com.travelersdiary.models.LocationPoint;
import com.travelersdiary.models.TodoItem;
import com.travelersdiary.models.TodoTask;
import com.travelersdiary.models.Waypoint;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RemindItemFragment extends Fragment {
    private static String DATE_PICKER_DIALOG_TAG = "DatePickerDialog";
    private static String TIME_PICKER_DIALOG_TAG = "TimePickerDialog";
    private static int PLACE_PICKER_REQUEST = 1;

    private static String KEY_IS_EDITING_MODE = "KEY_IS_EDITING_MODE";
    private static String KEY_HAS_CHANGED = "KEY_HAS_CHANGED";
    private static String KEY_IS_NEW_ITEM = "KEY_IS_NEW_ITEM";
    private static String KEY_REMIND_ITEM = "KEY_REMIND_ITEM";

    private ActionBar mSupportActionBar;
    private Menu mMenu;
    private TodoItem mRemindItem;

    private String mUserUID;
    private String mItemKey = null;

    private boolean isEditingMode = false;
    private boolean hasChanged = false;
    private boolean isNewItem = false;

    @Bind(R.id.remind_item_dont_remind_text_view)
    TextView dontRemindTextView;
    @Bind(R.id.remind_item_remind_type_spinner)
    Spinner remindTypeSpinner;
    @Bind(R.id.remind_item_date_text_view)
    TextView dateTextView;
    @Bind(R.id.remind_item_time_text_view)
    TextView timeTextView;
    @Bind(R.id.remind_item_waypoint_title_text_view)
    TextView waypointTitle;
    @Bind(R.id.remind_item_waypoint_distance_text_view)
    TextView waypointDistance;
    @Bind(R.id.remind_item_waypoint_distance_spinner)
    Spinner waypointDistanceSpinner;
    @Bind(R.id.remind_item_task)
    RecyclerView mTodoItemTask;

    private Context mContext;

    private EditText mRemindItemTitleEditText;

    private InputMethodManager mInputMethodManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remind_item, container, false);
        ButterKnife.bind(this, view);
        mContext = getContext();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mUserUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.KEY_REMINDER_ITEM_REF)) {
            mItemKey = getArguments().getString(Constants.KEY_REMINDER_ITEM_REF, null);
        }

        isNewItem = mItemKey == null || mItemKey.isEmpty();

        //get toolbar
        mSupportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mSupportActionBar != null) {
            mSupportActionBar.setDisplayHomeAsUpEnabled(true);
            mSupportActionBar.setDisplayShowTitleEnabled(false);
        }

        mRemindItemTitleEditText = (EditText) (getActivity()).findViewById(R.id.remind_item_title_edit_text);
        mRemindItemTitleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Utils.tintWidget(getContext(), mRemindItemTitleEditText, R.color.colorAccent);
                } else {
                    Utils.tintWidget(getContext(), mRemindItemTitleEditText, R.color.white);
                    if (mRemindItemTitleEditText.getText().toString().trim().length() == 0) {
                        mRemindItemTitleEditText.setText(mRemindItem.getTitle());
                    }
                }
            }
        });

        mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        ArrayAdapter<?> spinnerAdapter = ArrayAdapter.createFromResource(mContext, R.array.reminder_distance_values,
                R.layout.spinner_remind_distance_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        waypointDistanceSpinner.setAdapter(spinnerAdapter);
        waypointDistanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String distance = (String) parent.getItemAtPosition(position);
                if (mRemindItem != null)
                    mRemindItem.setDistance(Integer.parseInt(distance));
                waypointDistance.setText(distance);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            if (!isNewItem) {
                hasChanged = false;
                retrieveData(mItemKey);
            } else {
                // create new empty item
                mRemindItem = new TodoItem();
                mRemindItem.setTitle(mContext.getString(R.string.reminder_new_remind_item_default_title));
                mRemindItem.setViewAsCheckboxes(false);
                ArrayList<TodoTask> task = new ArrayList<>();
                task.add(new TodoTask(mContext.getString(R.string.reminder_new_remind_item_default_text), false));
                mRemindItem.setTask(task);
                // TODO: 26.02.2016 set current active travelId
                mRemindItem.setTravelId("default");
                // TODO: 26.02.2016 set active to true only if travelId is default or current
                mRemindItem.setActive(true);
                mRemindItem.setCompleted(false);

                hasChanged = true;
                setViews();
                enableEditingMode();
            }
        } else {
            isEditingMode = savedInstanceState.getBoolean(KEY_IS_EDITING_MODE, false);
            hasChanged = savedInstanceState.getBoolean(KEY_HAS_CHANGED, false);
            isNewItem = savedInstanceState.getBoolean(KEY_IS_NEW_ITEM, false);
            mRemindItem = (TodoItem) savedInstanceState.getSerializable(KEY_REMIND_ITEM);

            setViews();
            if (isEditingMode) {
                enableEditingMode();
            } else {
                enableReviewingMode();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        setOnClickListeners();

        DatePickerDialog datePickerDialog = (DatePickerDialog) getActivity().getFragmentManager().findFragmentByTag(DATE_PICKER_DIALOG_TAG);
        TimePickerDialog timePickerDialog = (TimePickerDialog) getActivity().getFragmentManager().findFragmentByTag(TIME_PICKER_DIALOG_TAG);
        if (datePickerDialog != null) datePickerDialog.setOnDateSetListener(mDateSetListener);
        if (timePickerDialog != null) timePickerDialog.setOnTimeSetListener(mTimeSetListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.remind_item_editor_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (isEditingMode) {
            mSupportActionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
            menu.setGroupVisible(R.id.remind_item_menu_edit_mode, true);
            menu.setGroupVisible(R.id.remind_item_menu_view_mode, false);
        } else {
            mSupportActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            menu.setGroupVisible(R.id.remind_item_menu_edit_mode, false);
            menu.setGroupVisible(R.id.remind_item_menu_view_mode, true);
        }

        if (mRemindItem != null && mRemindItem.isViewAsCheckboxes()) {
            menu.findItem(R.id.action_switch_checkboxes_remind_item)
                    .setTitle(R.string.remind_item_hide_checkboxes);
        } else {
            menu.findItem(R.id.action_switch_checkboxes_remind_item)
                    .setTitle(R.string.remind_item_show_checkboxes);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_remind_item:
                hasChanged = !saveItem();
                if (hasChanged) {
                    // error save data - do nothing
                    return true;
                }
                // no return or break - simulate home button click
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_edit_remind_item:
                enableEditingMode();
                return true;
            case R.id.action_switch_checkboxes_remind_item:
                if (mRemindItem.isViewAsCheckboxes()) {
                    mRemindItem.setViewAsCheckboxes(false);
                    item.setTitle(R.string.remind_item_show_checkboxes);
                } else {
                    mRemindItem.setViewAsCheckboxes(true);
                    item.setTitle(R.string.remind_item_hide_checkboxes);
                }
                ((TodoTaskAdapter) mTodoItemTask.getAdapter()).setViewAsCheckboxes(mRemindItem.isViewAsCheckboxes());
                // TODO: 26.02.2016 add save logic for view as checkboxes state in review mode
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onBackPressed() {
        if (isEditingMode) {
            if (hasChanged) {
                showDiscardDialog();
            } else {
                if (isNewItem) {
                    getActivity().finish();
                } else {
                    enableReviewingMode();
                    //hide keyboard
                    mInputMethodManager.hideSoftInputFromWindow(mTodoItemTask.findFocus().findViewById(R.id.task_item_edit_text).getWindowToken(), 0);
                }
            }
        } else {
            getActivity().finish();
        }
    }

    private void showDiscardDialog() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.discard_changes_text)
                .setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                })
                .show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_EDITING_MODE, isEditingMode);
        outState.putBoolean(KEY_HAS_CHANGED, hasChanged);
        outState.putBoolean(KEY_IS_NEW_ITEM, isNewItem);
        mRemindItem.setTitle(mRemindItemTitleEditText.getText().toString());
        outState.putSerializable(KEY_REMIND_ITEM, mRemindItem);
    }

    @Override
    public void onPause() {
        clearOnClickListeners();
        super.onPause();
    }

    private void retrieveData(String key) {
        Firebase itemRef = new Firebase(Utils.getFirebaseUserReminderUrl(mUserUID))
                .child(key);
        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mRemindItem = dataSnapshot.getValue(TodoItem.class);
                setViews();
                if (isEditingMode) {
                    enableEditingMode();
                } else {
                    enableReviewingMode();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(getContext(), firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setViews() {
        mRemindItemTitleEditText.setText(mRemindItem.getTitle());
        if (mMenu != null && mRemindItem.isViewAsCheckboxes()) {
            mMenu.findItem(R.id.action_switch_checkboxes_remind_item)
                    .setTitle(R.string.remind_item_hide_checkboxes);
            mSupportActionBar.invalidateOptionsMenu();
        }

        // item type
        RemindTypesAdapter remindTypesAdapter = new RemindTypesAdapter(mContext);
        remindTypeSpinner.setAdapter(remindTypesAdapter);

        // task text
        RecyclerView.Adapter todoTaskAdapter = new TodoTaskAdapter(mRemindItem.getTask(), mRemindItem.isViewAsCheckboxes());
        ((TodoTaskAdapter) todoTaskAdapter).setOnHasChangedListener(new TodoTaskAdapter.OnHasCangedListener() {
            @Override
            public void onHasChanged(boolean text) {
                if (text) {
                    // text changed
                    hasChanged = true;
                } else {
                    // checkbox state changed
                    // TODO: 26.02.2016 add save logic if check changed in review mode
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mTodoItemTask.setLayoutManager(layoutManager);
        mTodoItemTask.setAdapter(todoTaskAdapter);

        // item remind data
        if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_TIME.equals(mRemindItem.getType())) {
            // remind at time
            setDateTimeText();
        } else if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION.equals(mRemindItem.getType())) {
            // remind at location
            setLocationAndDistanceText();
        } else {
            // don't remind
        }

        setRemindTypeViewVisibility(mRemindItem.getType());
        remindTypeSpinner.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            Calendar c = Calendar.getInstance();
            long time = mRemindItem.getTime();
            if (time > 0) {
                c.setTimeInMillis(time);
            }
            c.set(year, monthOfYear, dayOfMonth);
            mRemindItem.setTime(c.getTimeInMillis());
            setDateTimeText();
        }
    };

    private void setDateTimeText() {
        long timestamp = mRemindItem.getTime();
        String dateText = SimpleDateFormat.getDateInstance().format(timestamp);
        String timeText = SimpleDateFormat.getTimeInstance().format(timestamp);
        dateTextView.setText(dateText);
        timeTextView.setText(timeText);
    }

    private void setLocationAndDistanceText() {
        Waypoint waypoint = mRemindItem.getWaypoint();
        if (waypoint != null) {
            waypointTitle.setText(waypoint.getTitle());
        } else {
            waypointTitle.setText(R.string.reminder_choose_location_text);
            // TODO: 27.02.16 set default distance from settings
            mRemindItem.setDistance(100);
        }
        waypointDistance.setText(Integer.toString(mRemindItem.getDistance()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlacePicker.getPlace(mContext, data);
                if (place != null) {
                    String title = place.getName().toString();
                    LatLng latLng = place.getLatLng();
                    LocationPoint locationPoint = new LocationPoint(latLng.latitude, latLng.longitude, 0);

                    if (mRemindItem.getWaypoint() == null) {
                        mRemindItem.setWaypoint(new Waypoint("default", title, locationPoint));
                    } else {
                        mRemindItem.getWaypoint().setTitle(title);
                        mRemindItem.getWaypoint().setLocation(locationPoint);
                    }

                    setLocationAndDistanceText();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void openDatePicker() {
        Calendar c = Calendar.getInstance();
        long time = mRemindItem.getTime();
        if (time > 0) {
            c.setTimeInMillis(time);
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(mDateSetListener, year, month, day);
//        datePickerDialog.dismissOnPause(true);
        datePickerDialog.vibrate(false);
        datePickerDialog.show(getActivity().getFragmentManager(), DATE_PICKER_DIALOG_TAG);
    }

    TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
            Calendar c = Calendar.getInstance();
            long time = mRemindItem.getTime();
            if (time > 0) {
                c.setTimeInMillis(time);
            }
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            mRemindItem.setTime(c.getTimeInMillis());
            setDateTimeText();
        }
    };

    private void openTimePicker() {
        Calendar c = Calendar.getInstance();
        long time = mRemindItem.getTime();
        if (time > 0) {
            c.setTimeInMillis(time);
        }
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(mTimeSetListener, hour, minute, true);
//        timePickerDialog.dismissOnPause(true);
        timePickerDialog.vibrate(false);
        timePickerDialog.show(getActivity().getFragmentManager(), TIME_PICKER_DIALOG_TAG);
    }

    private void setOnClickListeners() {

        remindTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // don't remind
                        mRemindItem.setType("");
                        break;
                    case 1: // remind at time
                        mRemindItem.setType(Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_TIME);
                        if (mRemindItem.getTime() <= 0) {
                            mRemindItem.setTime(System.currentTimeMillis());
                            setDateTimeText();
                        }
                        break;
                    case 2: // remind at location
                        mRemindItem.setType(Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION);
                        // TODO: 26.02.2016 add logic for default values if not exists
                        setLocationAndDistanceText();
                        break;
                }
                setRemindTypeViewVisibility(mRemindItem.getType());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dontRemindTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dontRemindTextView.clearFocus();
                remindTypeSpinner.requestFocus();
                remindTypeSpinner.performClick();
            }
        });

        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePicker();
            }
        });

        waypointTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                if (mRemindItem.getWaypoint() != null) {
                    LocationPoint locationPoint = mRemindItem.getWaypoint().getLocation();
                    LatLng latLng = new LatLng(locationPoint.getLatitude(), locationPoint.getLongitude());
                    LatLngBounds latLngBounds = new LatLngBounds(latLng, latLng);
                    builder.setLatLngBounds(latLngBounds);
                }

                Intent intent = null;
                try {
                    intent = builder.build(getActivity());
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                startActivityForResult(intent, PLACE_PICKER_REQUEST);
            }
        });

        waypointDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 27.02.16 add logic for choose remind distance for location
                Toast.makeText(getContext(), "TEST DISTANCE", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearOnClickListeners() {
        remindTypeSpinner.setOnItemSelectedListener(null);
        dontRemindTextView.setOnClickListener(null);
        dateTextView.setOnClickListener(null);
        timeTextView.setOnClickListener(null);
        waypointTitle.setOnClickListener(null);
    }

    private void setRemindTypeViewVisibility(String type) {
        if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_TIME.equals(type)) {
            // remind at time
            remindTypeSpinner.setSelection(1);
            dontRemindTextView.setVisibility(View.GONE);
            dateTextView.setVisibility(View.VISIBLE);
            timeTextView.setVisibility(View.VISIBLE);
            waypointTitle.setVisibility(View.GONE);
            waypointDistance.setVisibility(View.GONE);
        } else if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION.equals(type)) {
            // remind at location
            remindTypeSpinner.setSelection(2);
            dontRemindTextView.setVisibility(View.GONE);
            dateTextView.setVisibility(View.GONE);
            timeTextView.setVisibility(View.GONE);
            waypointTitle.setVisibility(View.VISIBLE);
            waypointDistance.setVisibility(View.VISIBLE);
        } else {
            // don't remind
            remindTypeSpinner.setSelection(0);
            dontRemindTextView.setVisibility(View.VISIBLE);
            dateTextView.setVisibility(View.GONE);
            timeTextView.setVisibility(View.GONE);
            waypointTitle.setVisibility(View.GONE);
            waypointDistance.setVisibility(View.GONE);
        }
    }

    private void setViewEditMode(View v, boolean editable) {
        if (editable) {
            v.setClickable(true);
            v.setLongClickable(true);
            v.setBackground(ContextCompat.getDrawable(mContext, R.drawable.abc_edit_text_material));
        } else {
            v.setClickable(false);
            v.setLongClickable(false);
            v.setBackground(null);
        }
    }

    private void setSpinnerEditMode(Spinner spinner, boolean editMode) {
        if (editMode) {
            spinner.setEnabled(true);
            spinner.setBackground(ContextCompat.getDrawable(mContext, R.drawable.abc_edit_text_material));
        } else {
            spinner.setEnabled(false);
            spinner.setBackground(null);
        }
    }

    private void enableReviewingMode() {
        isEditingMode = false;

        mRemindItemTitleEditText.setFocusable(false);
        Utils.tintWidget(getContext(), mRemindItemTitleEditText, android.R.color.transparent);
        mSupportActionBar.invalidateOptionsMenu();

        setViewEditMode(dontRemindTextView, false);
        setViewEditMode(dateTextView, false);
        setViewEditMode(timeTextView, false);
        setViewEditMode(waypointTitle, false);
        setViewEditMode(waypointDistance, false);
        setSpinnerEditMode(remindTypeSpinner, false);
        ((TodoTaskAdapter) mTodoItemTask.getAdapter()).setEditable(false);
    }

    private void enableEditingMode() {
        isEditingMode = true;

        mRemindItemTitleEditText.setFocusable(true);
        mRemindItemTitleEditText.setFocusableInTouchMode(true);
        Utils.tintWidget(getContext(), mRemindItemTitleEditText, R.color.white);
        mSupportActionBar.invalidateOptionsMenu();

        setViewEditMode(dontRemindTextView, true);
        setViewEditMode(dateTextView, true);
        setViewEditMode(timeTextView, true);
        setViewEditMode(waypointTitle, true);
        setViewEditMode(waypointDistance, true);
        setSpinnerEditMode(remindTypeSpinner, true);
        ((TodoTaskAdapter) mTodoItemTask.getAdapter()).setEditable(true);

        mTodoItemTask.scrollToPosition(0);
        ((TodoTaskAdapter) mTodoItemTask.getAdapter()).setSelectedItem(0);

        TodoTaskAdapter.ViewHolder viewHolder = (TodoTaskAdapter.ViewHolder) mTodoItemTask
                .findViewHolderForLayoutPosition(0);
        if (viewHolder != null) {
            EditText et = (EditText) viewHolder
                    .itemView
                    .findViewById(R.id.task_item_edit_text);
            et.setFocusable(true);
            et.setFocusableInTouchMode(true);
            et.setSelection(1);
            et.setCursorVisible(true);
            et.setSelected(true);
            et.requestFocus();
            //show keyboard
            mInputMethodManager.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public boolean saveItem() {
        mRemindItem.setTitle(mRemindItemTitleEditText.getText().toString());
        if (!validateData()) {
            return false;
        }
        Firebase firebaseRef = new Firebase(Utils.getFirebaseUserReminderUrl(mUserUID));
        if (mItemKey != null && !mItemKey.isEmpty()) {
            // update item
            Firebase updateItemRef = firebaseRef.child(mItemKey);
            updateItemRef.setValue(mRemindItem);
        } else {
            // create item
            Firebase newItemRef = firebaseRef.push();
            newItemRef.setValue(mRemindItem);
            mItemKey = newItemRef.getKey();
        }
        return true;
    }

    private boolean validateData() {
        if (mRemindItem == null) {
            showErrorDialog("Invalid data");
            return false;
        }

        if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_TIME.equals(mRemindItem.getType())) {
            // validate time
            if (mRemindItem.getTime() <= 0) {
                showErrorDialog("Invalid time");
                return false;
            }
        }

        if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION.equals(mRemindItem.getType())) {
            // validate waypoint

            Waypoint waypoint = mRemindItem.getWaypoint();
            if (waypoint == null) {
                showErrorDialog("Invalid location");
                return false;
            }

            String waypointTitle = waypoint.getTitle();
            if (waypointTitle == null || waypointTitle.isEmpty()) {
                showErrorDialog("Invalid location");
                return false;
            }

            LocationPoint locationPoint = waypoint.getLocation();
            if (locationPoint == null) {
                showErrorDialog("Invalid location");
                return false;
            }
        }
        return true;
    }

    private void showErrorDialog(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.error)
                .setMessage(errorMessage)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .create()
                .show();
    }
}
