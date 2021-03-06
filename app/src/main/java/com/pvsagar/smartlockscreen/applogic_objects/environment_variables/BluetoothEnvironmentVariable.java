package com.pvsagar.smartlockscreen.applogic_objects.environment_variables;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.BluetoothDevicesEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 10/8/14.
 * Class representing a BluetoothEnvironmentVariable. Stores 2 strings: Device name and device address
 * Type is given by EnvironmentVariable.TYPE_BLUETOOTH_DEVICES
 */
public class BluetoothEnvironmentVariable extends EnvironmentVariable {
    private static final String LOG_TAG = BluetoothEnvironmentVariable.class.getSimpleName();

    /**
    * Some constant values for use. INDEX_* values represent the index of the fields in the string
    * array inherited from EnvironmentVariable base class.
    */
    public static final int REQUEST_BLUETOOTH_ENABLE = 20;
    private static final int NUMBER_OF_STRING_VALUES = 2;
    private static final int INDEX_DEVICE_NAME = 0;
    private static final int INDEX_DEVICE_ADDRESS = 1;

    /**
     * Get an empty BluetoothEnvironmentVariable
     */
    public BluetoothEnvironmentVariable() {
        super(EnvironmentVariable.TYPE_BLUETOOTH_DEVICES, 0 , NUMBER_OF_STRING_VALUES);
    }

    /**
     * Get a BluetoothEnvironmentVariable with the values specified.
     * @param deviceName Visible name of the Bluetooth device
     * @param deviceAddress MAC address of the bluetooth adapter of the device
     */
    public BluetoothEnvironmentVariable(String deviceName, String deviceAddress){
        super(EnvironmentVariable.TYPE_BLUETOOTH_DEVICES, null,
                new String[]{deviceName, deviceAddress});
    }

    @Override
    public boolean isStringValuesSupported() {
        return true;
    }

    @Override
    public boolean isFloatValuesSupported() {
        return false;
    }

    public String getDeviceName(){
        try {
            return getStringValue(INDEX_DEVICE_NAME);
        } catch (Exception e){
            Log.e(LOG_TAG, "Internal application error, please file a bug report to developer."
                    + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String getDeviceAddress(){
        try {
            return getStringValue(INDEX_DEVICE_ADDRESS);
        } catch (Exception e){
            Log.e(LOG_TAG, "Internal application error, please file a bug report to developer."
                    + e.getMessage());
            return null;
        }
    }

    public void setDeviceName(String deviceName){
        setStringValue(deviceName, INDEX_DEVICE_NAME);
    }

    public void setDeviceAddress(String deviceAddress){
        setStringValue(deviceAddress, INDEX_DEVICE_ADDRESS);
    }

    /**
     * This function requests the users to enable bluetooth
     * @param context Context of the calling activity
     */
    public static void enableBluetooth(Context context){
        Utility.checkForNullAndThrowException(context);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Checking for Bluetooth Hardware
        if(mBluetoothAdapter == null){
            Log.e(LOG_TAG,"Bluetooth Hardware Not Found");
            return;
        }
        if(!mBluetoothAdapter.isEnabled()) {
            Log.v(LOG_TAG,"enabling bluetooth");
            Intent startBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) context).startActivityForResult(startBluetoothIntent, REQUEST_BLUETOOTH_ENABLE);
        }
    }

    /**
     * This function returns the list of paired bluetooth devices.
     * @param context Context of the calling activity/service
     * @return Returns the list of paired bluetooth devices
     */
    public static ArrayList<BluetoothDevice> getPairedBluetoothDevices(Context context){
        Utility.checkForNullAndThrowException(context);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Checking for Bluetooth Hardware
        if(mBluetoothAdapter == null){
            Log.e(LOG_TAG,"Bluetooth Hardware Not Found");
            return null;
        }
        //Checking whether bluetooth is enabled
        if(!mBluetoothAdapter.isEnabled()){
            Intent startBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity)context).startActivityForResult(startBluetoothIntent,REQUEST_BLUETOOTH_ENABLE);
            Log.v(LOG_TAG,"Bluetooth Not enabled");
            return null;
        }
        ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<BluetoothDevice>();
        for (BluetoothDevice bluetoothDevice : mBluetoothAdapter.getBondedDevices()) {
            bluetoothDevices.add(bluetoothDevice);
        }

        return bluetoothDevices;
    }

    /**
     * Gets a ContentValues instance populated with calling bluetoothDevice's data. Useful for
     * providing data to the database.
     * @return The ContentValues instance with device data. Keys are taken from
     *         EnvironmentDatabaseContract.BluetoothDevicesEntry
     */
    @Override
    public ContentValues getContentValues() {
        ContentValues bluetoothValues = new ContentValues();
        bluetoothValues.put(BluetoothDevicesEntry.COLUMN_DEVICE_NAME, getDeviceName());
        bluetoothValues.put(BluetoothDevicesEntry.COLUMN_DEVICE_ADDRESS, getDeviceAddress());
        return bluetoothValues;
    }

    /**
     * Converts cursor containing data from bluetooth_devices table, to objects of
     * BluetoothEnvironmentVariable
     * @param bluetoothCursor cursor containing data from bluetooth_devices table
     * @return List of BluetoothEnvironmentVariables obtained from the cursor
     */
    public static List<EnvironmentVariable> getBluetoothEnvironmentVariablesFromCursor(
            Cursor bluetoothCursor){
        ArrayList<EnvironmentVariable> environmentVariables =
                new ArrayList<EnvironmentVariable>();
        try {
            if (bluetoothCursor.moveToFirst()) {
                for (; !bluetoothCursor.isAfterLast(); bluetoothCursor.moveToNext()) {
                    BluetoothEnvironmentVariable variable = new BluetoothEnvironmentVariable(
                            bluetoothCursor.getString(bluetoothCursor.getColumnIndex(
                                    BluetoothDevicesEntry.COLUMN_DEVICE_NAME)),
                            bluetoothCursor.getString(bluetoothCursor.getColumnIndex(
                                    BluetoothDevicesEntry.COLUMN_DEVICE_ADDRESS)));
                    variable.id = bluetoothCursor.getLong(bluetoothCursor.getColumnIndex(
                            BluetoothDevicesEntry._ID));
                    environmentVariables.add(variable);
                }
            }
        } catch (Exception e){
            Log.w(LOG_TAG, e + ": " + e.getMessage());
        }
        return environmentVariables;
    }

    /**
     * Gets the bluetoothEnvironmentVariable from the database, having a particular device name and
     * address. Returns null if its not found
     * @param context Context of the calling activity
     * @param deviceName Name of the device
     * @param deviceAddress Address of the device
     * @return Device with given name and address
     */
    public static BluetoothEnvironmentVariable getBluetoothEnvironmentVariableFromDatabase(
            Context context, String deviceName, String deviceAddress){

        List<EnvironmentVariable> variables;
        if(deviceName != null && deviceAddress != null && context != null &&
                !deviceName.isEmpty() && !deviceAddress.isEmpty()) {
            String selection, selectionArgs[];
            selection = BluetoothDevicesEntry.COLUMN_DEVICE_ADDRESS + " = ? AND " +
                    BluetoothDevicesEntry.COLUMN_DEVICE_NAME + " = ? ";
            selectionArgs = new String[]{deviceAddress, deviceName};
            Cursor bluetoothCursor = context.getContentResolver().query(
                    BluetoothDevicesEntry.CONTENT_URI, null, selection, selectionArgs, null);
            variables = getBluetoothEnvironmentVariablesFromCursor(bluetoothCursor);
            bluetoothCursor.close();
            if(variables != null && !variables.isEmpty()){
                return (BluetoothEnvironmentVariable) variables.get(0);
            }
            return null;
        }
        Log.e(LOG_TAG, "Null or empty values passed for getting data from database.");
        return null;
    }
}
