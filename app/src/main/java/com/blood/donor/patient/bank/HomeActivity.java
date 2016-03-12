package com.blood.donor.patient.bank;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class HomeActivity extends AppCompatActivity {
    private Location mCurrentLoc=null;
    private MapView mMapView = null;
    private Dialog dialog=null;
    private final static double ZOOM_BY = 100;
    private DatabaseHandler db;
    private LocationDisplayManager mLDM;
    private SpatialReference mMapSr = null;
    private GraphicsLayer mResultsLayer = null;
    private final List<GetSetData> mDatabseRecord=new ArrayList<>();
    /**
     * When map is ready, set up the LocationDisplayManager.
     */
    private final OnStatusChangedListener statusChangedListener = new OnStatusChangedListener() {
        @Override
        public void onStatusChanged(Object source, STATUS status) {
            if (source == mMapView && status == STATUS.INITIALIZED) {
                mMapSr = mMapView.getSpatialReference();
                if (mLDM == null) {
                    setupLocationListener();
                }
            }
        }
    };
    /**
     * When the map is tapped, select the graphic at that location.
     */
    private final OnSingleTapListener mapTapCallback = new OnSingleTapListener() {
        @Override
        public void onSingleTap(float x, float y) {
            int[] graphicIDs = mResultsLayer.getGraphicIDs(x, y, 25);
            if (graphicIDs != null && graphicIDs.length > 0) {
                if (graphicIDs.length > 1){
                    int id = graphicIDs[0];
                    graphicIDs = new int[] { id };
                }
                mResultsLayer.clearSelection();
                mResultsLayer.setSelectedGraphics(graphicIDs, true);
                Graphic gr = mResultsLayer.getGraphic(graphicIDs[0]);
                ShowDetails(gr.getAttributes(),false, null);

            }
        }
    };


    private final OnLongPressListener mLongPress=new OnLongPressListener() {
        @Override
        public boolean onLongPress(float x, float y) {
            Point loc=mMapView.toMapPoint(x, y);
            String mLocation= loc.getX()+","+ loc.getY();
            ShowDetails(null, true, mLocation);//when true insert the record into databse
            return false;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        mMapView = (MapView) findViewById(R.id.map);
        mResultsLayer = new GraphicsLayer();
        mResultsLayer.setSelectionColorWidth(6);
        mMapView.addLayer(mResultsLayer);
        db = new DatabaseHandler(this);
        mMapView.setOnStatusChangedListener(statusChangedListener);
        mMapView.setOnLongPressListener(mLongPress);
        mMapView.setOnSingleTapListener(mapTapCallback);
        if (Config.isNetworkAvailable(getApplicationContext())) {
            if(!statusOfGPS)
                Toast.makeText(HomeActivity.this, "Enable GPS Location For Accurate Location", Toast.LENGTH_SHORT).show();
            setupLocationListener();
        }else
            Toast.makeText(HomeActivity.this, "Internet Connection is Required", Toast.LENGTH_SHORT).show();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.locate:
                Toast.makeText(HomeActivity.this, "Your are Here", Toast.LENGTH_SHORT).show();
                if (mMapView.isLoaded()) {
                    if ((mLDM != null) && (mLDM.getLocation() != null)) {
                        mLDM.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
                    }
                }

                return true;

            case R.id.doner:
                Toast.makeText(HomeActivity.this, "Blood Donors", Toast.LENGTH_SHORT).show();
                mResultsLayer.removeAll();
                List<GetSetData> donar = db.getAllContacts(0);
                for (GetSetData cn : donar) {
                    mDatabseRecord.add(new GetSetData(
                            cn.getmRecodeID(),
                            cn.getmFirstName(),
                            cn.getmLastName(),
                            cn.getmContact(),
                            cn.getmEmail(),
                            cn.getmCatagory(),
                            cn.getmLoc(),
                            cn.getmMapID()));
                    String[] separated =  cn.getmLoc().split(",");
                    Point location=new Point(Double.parseDouble(separated[0]),Double.parseDouble(separated[1]));
                    SpatialReference spacRef = SpatialReference.create(4326);
                    Point ltLn = (Point) GeometryEngine.project(location,mMapView.getSpatialReference(), spacRef );
                    double mUserDistanceFromLoc=distanceKm(mCurrentLoc.getLatitude(),mCurrentLoc.getLongitude(),""+ltLn.getY(),""+ltLn.getX());
                    if(mUserDistanceFromLoc <=Config.mSearchingDistance)
                        DisplayRecordOnMap(cn);
                }

                return true;
            case R.id.patient:
                Toast.makeText(HomeActivity.this, "Blood Patients", Toast.LENGTH_SHORT).show();
                mResultsLayer.removeAll();
                List<GetSetData> patient = db.getAllContacts(1);
                for (GetSetData cn : patient) {
                    mDatabseRecord.add(new GetSetData(
                            cn.getmRecodeID(),
                            cn.getmFirstName(),
                            cn.getmLastName(),
                            cn.getmContact(),
                            cn.getmEmail(),
                            cn.getmCatagory(),
                            cn.getmLoc(),
                            cn.getmMapID()));
                    String[] separated =  cn.getmLoc().split(",");
                    Point location=new Point(Double.parseDouble(separated[0]),Double.parseDouble(separated[1]));
                    SpatialReference spacRef = SpatialReference.create(4326);
                    Point ltLn = (Point) GeometryEngine.project(location,mMapView.getSpatialReference(), spacRef );
                    double mUserDistanceFromLoc=distanceKm(mCurrentLoc.getLatitude(),
                            mCurrentLoc.getLongitude(),""+ltLn.getY(),""+ltLn.getX());
                    if(mUserDistanceFromLoc<=Config.mSearchingDistance)
                        DisplayRecordOnMap(cn);
                }

                return  true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_layout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Zoom to location using a specific size of extent.
     *
     * @param loc  the location to center the MapView at
     */
    private void zoomToLocation(Location loc) {
        Point mapPoint = getAsPoint(loc);
        Unit mapUnit = mMapSr.getUnit();
        double zoomFactor = Unit.convertUnits(ZOOM_BY,Unit.create(LinearUnit.Code.MILE_US), mapUnit);
        Envelope zoomExtent = new Envelope(mapPoint, zoomFactor, zoomFactor);
        mMapView.setExtent(zoomExtent);
    }
    private Point getAsPoint(Location loc) {
        Point wgsPoint = new Point(loc.getLongitude(), loc.getLatitude());
        return (Point) GeometryEngine.project(wgsPoint, SpatialReference.create(4326),mMapSr);
    }

    private void setupLocationListener() {
        if ((mMapView != null) && (mMapView.isLoaded())) {
            mLDM = mMapView.getLocationDisplayManager();
            mLDM.setLocationListener(new LocationListener() {
                boolean locationChanged = false;

                // Zooms to the current location when first GPS fix arrives.
                @Override
                public void onLocationChanged(Location loc) {
                    if (!locationChanged) {
                        locationChanged = true;
                        mCurrentLoc=loc;
                        zoomToLocation(loc);

                        List<GetSetData> contacts = db.getAllContacts(2);
                        for (GetSetData cn : contacts) {
                            mDatabseRecord.add(new GetSetData(
                                    cn.getmRecodeID(),
                                    cn.getmFirstName(),
                                    cn.getmLastName(),
                                    cn.getmContact(),
                                    cn.getmEmail(),
                                    cn.getmCatagory(),
                                    cn.getmLoc(),
                                    cn.getmMapID()));

                            String[] separated =  cn.getmLoc().split(",");
                            Point location=new Point(Double.parseDouble(separated[0]),Double.parseDouble(separated[1]));
                            SpatialReference spacRef = SpatialReference.create(4326);
                            Point ltLn = (Point) GeometryEngine.project(location,mMapView.getSpatialReference(), spacRef );
                            double mUserDistanceFromLoc=distanceKm(loc.getLatitude(),loc.getLongitude(),""+ltLn.getY(),""+ltLn.getX());
                            if(mUserDistanceFromLoc <= Config.mSearchingDistance)
                                DisplayRecordOnMap(cn);
                        }

                    }
                }

                @Override
                public void onProviderDisabled(String arg0) {
                }

                @Override
                public void onProviderEnabled(String arg0) {
                }

                @Override
                public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
                }
            });

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        100);
            }else{
                mLDM.start();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLDM.start();

                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(HomeActivity.this, "Location Persimission Required", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
    private void DisplayRecordOnMap(GetSetData cn) {
        String[] separated = cn.getmLoc().split(",");
        SimpleMarkerSymbol sms;
        if(cn.getmCatagory().equals("Blood Donor"))
             sms = new SimpleMarkerSymbol(Color.GREEN, 25, SimpleMarkerSymbol.STYLE.CIRCLE);
        else
            sms = new SimpleMarkerSymbol(Color.RED, 25, SimpleMarkerSymbol.STYLE.CIRCLE);

        Map<String, Object> hm = new HashMap<>();
        hm.put("FName", cn.getmFirstName());
        hm.put("Phone", cn.getmContact());
        hm.put("LNAME", cn.getmLastName());
        hm.put("Eamil", cn.getmEmail());
        hm.put("Category", cn.getmCatagory());
        hm.put("BGroup", cn.getmBloodGroup());
        Graphic graphic = new Graphic(new Point(Double.parseDouble(separated[0]), Double.parseDouble(separated[1])), sms, hm);
        mResultsLayer.addGraphic(graphic);

    }

    public void ShowDetails(Map<String, Object> attributes, boolean EntryDataKey, final String mLocation) {
        dialog = new Dialog(this, R.style.CustomDialog); //this is a reference to the style above
        dialog.setContentView(R.layout.identify_callout_content); //I saved the xml file above as yesnomessage.xml
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        final EditText fname= (EditText) dialog.findViewById(R.id.fname);
        final TextView mHeading= (TextView) dialog.findViewById(R.id.text);
        final EditText lname= (EditText) dialog.findViewById(R.id.lname);
        final EditText contact= (EditText) dialog.findViewById(R.id.contact);
        final EditText email= (EditText) dialog.findViewById(R.id.eamil);
        final EditText bgroup= (EditText) dialog.findViewById(R.id.bloodgroup);
        final RadioButton blood_donar= (RadioButton) dialog.findViewById(R.id.blood_donar);
        final RadioButton blood_patient= (RadioButton) dialog.findViewById(R.id.blood_patient);
        final Button mAddRecord= (Button) dialog.findViewById(R.id.button);
        final Button mcancel= (Button) dialog.findViewById(R.id.buttoncancel);

       if(!EntryDataKey){
           mHeading.setVisibility(View.GONE);
           fname.setText(attributes.get("FName").toString());
           fname.setEnabled(false);
           fname.setBackgroundResource(0);
           contact.setText(attributes.get("Phone").toString());
           contact.setEnabled(false);
           contact.setBackgroundResource(0);
           lname.setText(attributes.get("LNAME").toString());
           lname.setEnabled(false);
           lname.setBackgroundResource(0);
           email.setText(attributes.get("Eamil").toString());
           email.setEnabled(false);
           email.setBackgroundResource(0);
           if(attributes.get("Category").toString().equals("Blood Donor")) {
               blood_donar.setChecked(true);
               blood_patient.setChecked(false);
               blood_patient.setEnabled(false);
               blood_patient.setVisibility(View.GONE);
           }else{
               blood_donar.setChecked(false);
               blood_donar.setVisibility(View.GONE);
               blood_donar.setEnabled(false);
               blood_patient.setChecked(true);
           }
           bgroup.setText(attributes.get("BGroup").toString());
           bgroup.setEnabled(false);
           bgroup.setBackgroundResource(0);
           mAddRecord.setVisibility(View.GONE);

       }else{
           mAddRecord.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   String mCategory;
                   if(blood_donar.isChecked())
                       mCategory="Blood Donor";
                   else
                       mCategory="Blood Patient";
                   if (!validateName(fname, (TextInputLayout) dialog.findViewById(R.id.input_layout_name))) {
                       return;
                   }

                   if (!validateEmail(email, (TextInputLayout) dialog.findViewById(R.id.input_layout_email))) {
                       return;
                   }
                   GetSetData mTempData;
                   if(blood_donar.isChecked() || blood_patient.isChecked()) {
                       mTempData = new GetSetData(
                               fname.getText().toString(),
                               lname.getText().toString(),
                               contact.getText().toString(),
                               email.getText().toString(),
                               bgroup.getText().toString(),
                               mCategory,
                               mLocation,
                               Double.toString(10));

                       db.addContact(mTempData);

                       String[] separated = mLocation.split(",");
                       Point location=new Point(Double.parseDouble(separated[0]),Double.parseDouble(separated[1]));
                       SpatialReference spacRef = SpatialReference.create(4326);
                       Point ltLn = (Point) GeometryEngine.project(location,mMapView.getSpatialReference(), spacRef );

                       double mUserDistanceFromLoc=distanceKm(mCurrentLoc.getLatitude(),mCurrentLoc.getLongitude(),
                               ""+ltLn.getY(),""+ltLn.getX());
                       if(mUserDistanceFromLoc<=Config.mSearchingDistance) {
                           DisplayRecordOnMap(mTempData);
                           Toast.makeText(getApplicationContext(), "User Added Successfully...", Toast.LENGTH_SHORT).show();
                       }
                       dialog.cancel();
                   }else
                       Toast.makeText(HomeActivity.this, "Please Select One Checkbox....", Toast.LENGTH_SHORT).show();
               }
           });
       }
        mcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        dialog.show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
        if (mLDM != null) {
            mLDM.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.unpause();
        if (mLDM != null) {
            mLDM.resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLDM != null) {
            mLDM.stop();
        }
    }
    public Dialog getDialog(){
        return dialog;
    }


    /***
     * @param lat1 current location latitude
     * @param lon1 current location longitude
     * @param lat2 graphic location latitude
     * @param lon2 graphic location longitude
     *
     */


    private static double distanceKm(double lat1, double lon1, String lat2, String lon2) {
        int EARTH_RADIUS_KM = 6371;
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(Double.parseDouble(lat2));
        double deltaLonRad = Math.toRadians(Double.parseDouble(lon2) - lon1);
        return Math.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLonRad)) * EARTH_RADIUS_KM;

    }



    /**
     * Function to check the Valid Email Address
     * */

    private boolean validateEmail(EditText inputEmail,TextInputLayout inputLayoutEmail) {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(inputEmail);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }

        return true;
    }



    /**
     * Function to check the First Name
     * */
    private boolean validateName(EditText inputName,TextInputLayout inputLayoutName ) {
        if (inputName.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.err_msg_name));
            requestFocus(inputName);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;
    }
    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * TO Focus view on Invalid Edit Text Input
     * */
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
}



