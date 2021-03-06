package com.pvsagar.smartlockscreen.applogic_objects;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;

import com.pvsagar.smartlockscreen.applogic_objects.environment_variables.BluetoothEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.environment_variables.LocationEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.environment_variables.NoiseLevelEnvironmentVariable;
import com.pvsagar.smartlockscreen.applogic_objects.environment_variables.WiFiEnvironmentVariable;
import com.pvsagar.smartlockscreen.backend_helpers.Picture;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.baseclasses.EnvironmentVariable;
import com.pvsagar.smartlockscreen.environmentdb.EnvironmentDatabaseContract.EnvironmentEntry;
import com.pvsagar.smartlockscreen.services.BaseService;
import com.pvsagar.smartlockscreen.services.GeoFenceIntentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by aravind on 10/8/14.
 *
 * This class is used to store environment used in the app. It consists primarily of different
 * environment variables, name, hint etc. Database helper functions and static functions to read
 * environment details from the database, are also provided.
 */
public class Environment {
    private static final String LOG_TAG = Environment.class.getSimpleName();

    /**
     * The id of the environment, as stored in the database
     */
    private long id;

    /**
     * The EnvironmentVariables associated with the environment
     */
    private LocationEnvironmentVariable locationEnvironmentVariable;
    private Vector<BluetoothEnvironmentVariable> bluetoothEnvironmentVariables;
    private WiFiEnvironmentVariable wiFiEnvironmentVariable;
    private NoiseLevelEnvironmentVariable noiseLevelEnvironmentVariable;

    /**
     * true for all, false for any
     */
    private boolean bluetoothAllOrAny;

    /**
     * Other data about the environment
     */
    private String name, hint;
    private boolean isEnabled;

    public boolean hasLocation, hasBluetoothDevices, hasWiFiNetwork, hasNoiseLevel;

    private Picture environmentPicture;

    /**
     * Default constructor. Sets the boolean values.
     */
    public Environment(){
        hasLocation = hasNoiseLevel = hasWiFiNetwork = hasBluetoothDevices = false;
        bluetoothAllOrAny = false;
        isEnabled = true;
    }

    /**
     * Constructor which sets environment name and environment variables.
     * @param name name of the environment
     * @param variables environment variables corresponding to the environment.
     *                  Note that only bluetooth environment variables can be multiple in number
     *                  for an environment, For other kinds of variables, the last variable of that
     *                  type which was passed is taken
     */
    public Environment(String name, EnvironmentVariable... variables){
        this();
        setName(name);
        addEnvironmentVariables(variables);
    }

    /**
     * Similar to previous constructor, but this accepts a List of environment variables.
     * @param name name of the environment
     * @param variables environment variables corresponding to the environment.
     *                  Note that only bluetooth environment variables can be multiple in number
     *                  for an environment, For other kinds of variables, the last variable of that
     *                  type which was passed is taken
     */
    public Environment(String name, List<EnvironmentVariable> variables){
        this();
        EnvironmentVariable[] variableArray = new EnvironmentVariable[variables.size()];
        variables.toArray(variableArray);
        setName(name);
        addEnvironmentVariables(variableArray);
    }

    /**
     * Set the name of the environment
     * @param name Environment name
     */
    public void setName(String name){
        if(name != null) {
            this.name = name;
        } else {
            throw new IllegalArgumentException("Name cannot be null.");
        }
    }

    /**Sets whether the environment should check for all the bluetooth devices specified, or the
     * environment is active when any of the specified bluetooth devices are connected.
     * @param b true: all, false: any
     */
    public void setBluetoothAllOrAny(boolean b){
        bluetoothAllOrAny = b;
    }

    /**
     * Adds the passed environment variables to the environment. If an environment variable of type
     * other than BluetoothEnvironmentVariable is passed, it'll reset the current value.
     * @param variables variables to be added.
     */
    public void addEnvironmentVariables(EnvironmentVariable... variables){
        if(Utility.checkForNullAndWarn(variables, LOG_TAG)){
            return;
        }
        for(EnvironmentVariable variable: variables){
            if(variable.getVariableType().equals(EnvironmentVariable.TYPE_LOCATION)){
                addLocationVariable((LocationEnvironmentVariable) variable);
            }
            if(variable.getVariableType().equals(EnvironmentVariable.TYPE_BLUETOOTH_DEVICES)){
                addBluetoothDevicesEnvironmentVariable((BluetoothEnvironmentVariable) variable);
            }
            if(variable.getVariableType().equals(EnvironmentVariable.TYPE_WIFI_NETWORKS)){
                addWiFiEnvironmentVariable((WiFiEnvironmentVariable) variable);
            }
            if(variable.getVariableType().equals(EnvironmentVariable.TYPE_NOISE_LEVEL)){
                addNoiseLevelEnvironmentVariable((NoiseLevelEnvironmentVariable) variable);
            }
        }
    }

    /**
     * Sets the LocationEnvironmentVariable of the environment.
     * @param variable new environment variable. pass null to indicate environment does not have
     *                 a location associated with it
     */
    public void addLocationVariable(LocationEnvironmentVariable variable){
        locationEnvironmentVariable = variable;
        hasLocation = variable != null;
    }

    /**
     * Adds BluetoothEnvironmentVariable to the environment.
     * @param variable new environment variable. pass null to indicate environment does not have
     *                 bluetoothDevices associated with it
     */
    public void addBluetoothDevicesEnvironmentVariable(BluetoothEnvironmentVariable variable){
        if(variable != null) {
            if (bluetoothEnvironmentVariables == null) {
                bluetoothEnvironmentVariables = new Vector<BluetoothEnvironmentVariable>();
            }
            bluetoothEnvironmentVariables.add(variable);
            hasBluetoothDevices = true;
        } else {
            bluetoothEnvironmentVariables = null;
            hasBluetoothDevices = false;
        }
    }

    /**
     * Sets the WifiEnvironmentVariable of the environment.
     * @param variable new environment variable. pass null to indicate environment does not have
     *                 a wifi network associated with it
     */
    public void addWiFiEnvironmentVariable(WiFiEnvironmentVariable variable){
        wiFiEnvironmentVariable = variable;
        hasWiFiNetwork = variable != null;
    }

    /**
     * Sets the NoiseLevelEnvironmentVariable of the environment.
     * @param variable new environment variable. pass null to indicate environment does not have
     *                 noise level associated with it
     */
    public void addNoiseLevelEnvironmentVariable(NoiseLevelEnvironmentVariable variable){
        noiseLevelEnvironmentVariable = variable;
        hasNoiseLevel = variable != null;
    }

    public LocationEnvironmentVariable getLocationEnvironmentVariable(){
        return locationEnvironmentVariable;
    }

    public Vector<BluetoothEnvironmentVariable> getBluetoothEnvironmentVariables(){
        return bluetoothEnvironmentVariables;
    }

    public WiFiEnvironmentVariable getWiFiEnvironmentVariable(){
        return wiFiEnvironmentVariable;
    }

    public NoiseLevelEnvironmentVariable getNoiseLevelEnvironmentVariable(){
        return noiseLevelEnvironmentVariable;
    }

    public String getName(){
        return name;
    }

    public boolean isBluetoothAllOrAny(){
        return bluetoothAllOrAny;
    }

    public boolean isEnabled(){
        return isEnabled;
    }

    public void setEnabled(boolean enabled){
        isEnabled = enabled;
    }

    public String getHint(){
        return hint;
    }

    public void setHint(String hint){
        this.hint = hint;
    }

    public long getId(){
        return id;
    }

    /**
     * Inserts the environment into the database
     * @param context Activity/ service context
     */
    public void insertIntoDatabase(final Context context){
        Utility.checkForNullAndThrowException(context);

        Environment e = Environment.this;
        Utility.checkForNullAndThrowException(e.getName());
        Utility.checkForNullAndThrowException(e.getHint());
        ContentValues environmentValues = new ContentValues();
        environmentValues.put(EnvironmentEntry.COLUMN_NAME, e.getName());
        environmentValues.put(EnvironmentEntry.COLUMN_IS_WIFI_ENABLED, 0);
        environmentValues.put(EnvironmentEntry.COLUMN_IS_LOCATION_ENABLED, 0);
        environmentValues.put(EnvironmentEntry.COLUMN_BLUETOOTH_ALL_OR_ANY,
                e.isBluetoothAllOrAny()?1:0);
        environmentValues.put(EnvironmentEntry.COLUMN_IS_BLUETOOTH_ENABLED, 0);
        environmentValues.put(EnvironmentEntry.COLUMN_IS_ENABLED, e.isEnabled()?1:0);
        environmentValues.put(EnvironmentEntry.COLUMN_ENVIRONMENT_HINT, e.getHint());

        if(environmentPicture == null){
            environmentPicture = new Picture(Picture.PICTURE_TYPE_COLOR,
                    String.valueOf(Utility.getRandomColor(context)), null, null);
        }
        environmentValues.put(EnvironmentEntry.COLUMN_ENVIRONMENT_PICTURE_TYPE, environmentPicture.getPictureType());
        environmentValues.put(EnvironmentEntry.COLUMN_ENVIRONMENT_PICTURE_DESCRIPTION, environmentPicture.getBackgroundColor());
        environmentValues.put(EnvironmentEntry.COLUMN_ENVIRONMENT_PICTURE_DRAWABLE, environmentPicture.getDrawableName());
        environmentValues.put(EnvironmentEntry.COLUMN_ENVIRONMENT_PICTURE, environmentPicture.getImage());

        if(e.hasNoiseLevel && e.getNoiseLevelEnvironmentVariable() != null){
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MAX_NOISE_ENABLED,
                    e.getNoiseLevelEnvironmentVariable().hasUpperLimit);
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MIN_NOISE_ENABLED,
                    e.getNoiseLevelEnvironmentVariable().hasLowerLimit);
            environmentValues.put(EnvironmentEntry.COLUMN_MAX_NOISE_LEVEL,
                    e.getNoiseLevelEnvironmentVariable().getUpperLimit());
            environmentValues.put(EnvironmentEntry.COLUMN_MIN_NOISE_LEVEL,
                    e.getNoiseLevelEnvironmentVariable().getLowerLimit());
        } else {
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MAX_NOISE_ENABLED, false);
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MIN_NOISE_ENABLED, false);
        }

        Uri environmentUri = context.getContentResolver().insert(
                EnvironmentEntry.CONTENT_URI, environmentValues);
        long environmentId = EnvironmentEntry.getEnvironmentIdFromUri(environmentUri);

        if(e.hasBluetoothDevices && e.getBluetoothEnvironmentVariables() != null
                && !e.getBluetoothEnvironmentVariables().isEmpty()){
            Vector<BluetoothEnvironmentVariable> bluetoothEnvironmentVariables =
                    e.getBluetoothEnvironmentVariables();
            Uri insertUri = EnvironmentEntry.buildEnvironmentUriWithIdAndBluetooth(environmentId);
            for(BluetoothEnvironmentVariable variable: bluetoothEnvironmentVariables) {
                ContentValues bluetoothValues = variable.getContentValues();
                context.getContentResolver().insert(insertUri, bluetoothValues);
            }
        }
        if(e.hasLocation && e.getLocationEnvironmentVariable() != null){
            LocationEnvironmentVariable variable = e.getLocationEnvironmentVariable();
            Uri insertUri = EnvironmentEntry.buildEnvironmentUriWithIdAndLocation(environmentId);
            ContentValues locationValues = variable.getContentValues();
            context.getContentResolver().insert(insertUri, locationValues);
        }
        if(e.hasWiFiNetwork && e.getWiFiEnvironmentVariable() != null){
            WiFiEnvironmentVariable variable = e.getWiFiEnvironmentVariable();
            Uri insertUri = EnvironmentEntry.buildEnvironmentUriWithIdAndWifi(environmentId);
            ContentValues wifiValues = variable.getContentValues();
            context.getContentResolver().insert(insertUri, wifiValues);
        }
        e.id = environmentId;
    }

    /**
     * Update the environment entry in database. Old name of the environment should be passed if
     * name has changed. If oldName is null, current name of the environment is taken and used for
     * finding the records to update.
     * Note: this does not update the environment picture in db. To do that, use updateEnvironmentPicture()
     * @param context activity/service context
     * @param oldName The name of the environment to be modified
     */
    public void updateInDatabase(final Context context, String oldName){
        if(oldName == null || oldName.isEmpty()){
            oldName = getName();
        }
        Environment oldEnvironment = getFullEnvironment(context, oldName);
        if(oldEnvironment == null) return;
        ContentValues environmentValues = new ContentValues();
        environmentValues.put(EnvironmentEntry.COLUMN_NAME, getName());
        environmentValues.put(EnvironmentEntry.COLUMN_BLUETOOTH_ALL_OR_ANY,
                isBluetoothAllOrAny()?1:0);
        environmentValues.put(EnvironmentEntry.COLUMN_IS_ENABLED, isEnabled()?1:0);
        environmentValues.put(EnvironmentEntry.COLUMN_ENVIRONMENT_HINT, getHint());
        if(hasNoiseLevel && getNoiseLevelEnvironmentVariable() != null){
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MAX_NOISE_ENABLED,
                    getNoiseLevelEnvironmentVariable().hasUpperLimit);
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MIN_NOISE_ENABLED,
                    getNoiseLevelEnvironmentVariable().hasLowerLimit);
            environmentValues.put(EnvironmentEntry.COLUMN_MAX_NOISE_LEVEL,
                    getNoiseLevelEnvironmentVariable().getUpperLimit());
            environmentValues.put(EnvironmentEntry.COLUMN_MIN_NOISE_LEVEL,
                    getNoiseLevelEnvironmentVariable().getLowerLimit());
        } else {
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MAX_NOISE_ENABLED, false);
            environmentValues.put(EnvironmentEntry.COLUMN_IS_MIN_NOISE_ENABLED, false);
        }
        context.getContentResolver().update(EnvironmentEntry.
                buildEnvironmentUriWithId(oldEnvironment.id), environmentValues, null, null);
        if(oldEnvironment.hasLocation){
            if(hasLocation && !
                    oldEnvironment.locationEnvironmentVariable.equals(locationEnvironmentVariable)){
                int updatedEntries = context.getContentResolver().update(
                        EnvironmentEntry.buildEnvironmentUriWithIdAndLocation(oldEnvironment.id),
                        locationEnvironmentVariable.getContentValues(), null, null);
                if(updatedEntries > 1){
                    removeFromCurrentGeofences(oldEnvironment.getLocationEnvironmentVariable(), context);
                }
            }
            else if(!hasLocation){
                int deletedEntries = context.getContentResolver().delete(EnvironmentEntry.
                        buildEnvironmentUriWithIdAndLocation(oldEnvironment.id), null, null);
                if(deletedEntries > 1){
                    removeFromCurrentGeofences(oldEnvironment.getLocationEnvironmentVariable(), context);
                }
            }
        } else if(hasLocation) {
            context.getContentResolver().insert(EnvironmentEntry.
                            buildEnvironmentUriWithIdAndLocation(oldEnvironment.id),
                    locationEnvironmentVariable.getContentValues());
        }

        if(oldEnvironment.hasWiFiNetwork){
            if(hasWiFiNetwork && !
                    oldEnvironment.wiFiEnvironmentVariable.equals(wiFiEnvironmentVariable)){
                context.getContentResolver().update(
                        EnvironmentEntry.buildEnvironmentUriWithIdAndWifi(oldEnvironment.id),
                        wiFiEnvironmentVariable.getContentValues(), null, null);
            }
            else if(!hasWiFiNetwork){
                context.getContentResolver().delete(EnvironmentEntry.
                        buildEnvironmentUriWithIdAndWifi(oldEnvironment.id), null, null);
            }
        } else if(hasWiFiNetwork) {
            context.getContentResolver().insert(EnvironmentEntry.
                            buildEnvironmentUriWithIdAndWifi(oldEnvironment.id),
                    wiFiEnvironmentVariable.getContentValues());
        }

        context.getContentResolver().delete(EnvironmentEntry.
                buildEnvironmentUriWithIdAndBluetooth(oldEnvironment.id), null, null);

        if(hasBluetoothDevices && getBluetoothEnvironmentVariables() != null
                && !getBluetoothEnvironmentVariables().isEmpty()){
            Vector<BluetoothEnvironmentVariable> bluetoothEnvironmentVariables =
                    getBluetoothEnvironmentVariables();
            Uri insertUri = EnvironmentEntry.
                    buildEnvironmentUriWithIdAndBluetooth(oldEnvironment.id);
            for(BluetoothEnvironmentVariable variable: bluetoothEnvironmentVariables) {
                ContentValues bluetoothValues = variable.getContentValues();
                context.getContentResolver().insert(insertUri, bluetoothValues);
            }
        }
    }

    /**
     * Sets the enabled flag of any environment in the database
     * @param context activity/service context
     * @param environmentName Name of the environment whose enabled flag should b changed
     * @param enabled The new enabled value
     */
    public static void setEnabledInDatabase(final Context context, final String environmentName,
                                               final boolean enabled){
        if(Utility.checkForNullAndWarn(environmentName, LOG_TAG)){
            return;
        }
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                ContentValues environmentValues = new ContentValues();
                environmentValues.put(EnvironmentEntry.COLUMN_IS_ENABLED, enabled);
                Environment e = getBareboneEnvironment(context, environmentName);
                long id;
                if(e != null) {
                    id = e.id;
                    context.getContentResolver().update(EnvironmentEntry.buildEnvironmentUriWithId(id),
                            environmentValues, null, null);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                context.startService(BaseService.getServiceIntent(context, null, BaseService.ACTION_DETECT_ENVIRONMENT));
            }
        }.execute();
    }

    /**
     * Returns list of names of all the environment in the database.
     * To get the full environment details, see getFullEnvironment()
     * @param context activity/service context
     * @return A list of strings of environment names
     */
    public static List<String> getAllEnvironmentNames(Context context){
        Cursor envCursor = context.getContentResolver().query(EnvironmentEntry.CONTENT_URI, null,
                null, null, null);
        ArrayList<String> environmentNames = new ArrayList<String>();

        for(envCursor.moveToFirst(); !envCursor.isAfterLast(); envCursor.moveToNext()){
            String envName = envCursor.getString(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_NAME));
            environmentNames.add(envName);
        }
        envCursor.close();
        return environmentNames;
    }

    /**
     * Returns a list of Environment instances, without the environment variables populated,
     * for all environments in the database. Useful for getting id, name, isEnabled, Hint and picture
     * of all environments
     * @param context Activity/ service context
     * @return List of barebone Environment instances (without the Environment Variables populated)
     */
    public static List<Environment> getAllEnvironmentBarebones(Context context){
        Cursor envCursor = context.getContentResolver().query(EnvironmentEntry.CONTENT_URI, null,
                null, null, null);
        ArrayList<Environment> environments = new ArrayList<Environment>();

        for(envCursor.moveToFirst(); !envCursor.isAfterLast(); envCursor.moveToNext()){
            Environment e = buildEnvironmentBareboneFromCursor(envCursor);
            environments.add(e);
        }
        envCursor.close();
        return environments;
    }

    /**
     * Get the complete environment details of an environment from the database
     * @param context activity/service context
     * @param environmentName Name of the environment whose details are required
     * @return An instance of Environment with the required details
     */
    public static Environment getFullEnvironment(Context context, String environmentName){
        String selection = EnvironmentEntry.COLUMN_NAME + " = ? ";
        String[] selectionArgs = new String[]{environmentName};
        Cursor envCursor = context.getContentResolver().query(EnvironmentEntry.CONTENT_URI, null,
                selection, selectionArgs, null);
        Environment e;
        if(envCursor.moveToFirst()){
            long environmentId = envCursor.getLong(envCursor.getColumnIndex(EnvironmentEntry._ID));
            Cursor bluetoothCursor = context.getContentResolver().query
                    (EnvironmentEntry.buildEnvironmentUriWithIdAndBluetooth(environmentId),
                            null, null, null, null),
                    wifiCursor = context.getContentResolver().query(
                            EnvironmentEntry.buildEnvironmentUriWithIdAndWifi(environmentId),
                            null, null, null, null),
                    locationCursor = context.getContentResolver().query(
                            EnvironmentEntry.buildEnvironmentUriWithIdAndLocation(environmentId),
                            null, null, null, null);
            List<EnvironmentVariable> environmentVariables = BluetoothEnvironmentVariable.
                    getBluetoothEnvironmentVariablesFromCursor(bluetoothCursor);
            environmentVariables.addAll(LocationEnvironmentVariable.
                    getLocationEnvironmentVariablesFromCursor(locationCursor));
            environmentVariables.addAll(WiFiEnvironmentVariable.
                    getWiFiEnvironmentVariablesFromCursor(wifiCursor));
            environmentVariables.addAll(NoiseLevelEnvironmentVariable.
                    getNoiseLevelEnvironmentVariablesFromCursor(envCursor));
            e = buildEnvironmentBareboneFromCursor(envCursor);
            EnvironmentVariable[] environmentVariableArray =
                    new EnvironmentVariable[environmentVariables.size()];
            environmentVariables.toArray(environmentVariableArray);
            e.addEnvironmentVariables(environmentVariableArray);
            e.setBluetoothAllOrAny(envCursor.getInt(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_BLUETOOTH_ALL_OR_ANY)) == 1);
            envCursor.close();
            bluetoothCursor.close();
            wifiCursor.close();
            locationCursor.close();
        } else {
            return null;
        }
        return e;
    }

    /**
     * Returns an environment instance of the environment specified by its name. Only name, hint, id
     * and enabled flag are populated. The environment variables are not populated
     * @param context Activity/ service context
     * @param environmentName Name of the environment whose details are required
     * @return Instance of environment with specified fields populated.
     */
    public static Environment getBareboneEnvironment(Context context, String environmentName){
        String selection = EnvironmentEntry.COLUMN_NAME + " = ? ";
        String[] selectionArgs = new String[]{environmentName};
        Cursor envCursor = context.getContentResolver().query(EnvironmentEntry.CONTENT_URI, null,
                selection, selectionArgs, null);
        envCursor.moveToFirst();
        Environment returnEnvironment = buildEnvironmentBareboneFromCursor(envCursor);
        envCursor.close();
        return returnEnvironment;
    }

    /**
     * Returns an environment instance of the environment specified by its id. Only name, hint, id
     * and enabled flag are populated. The environment variables are not populated
     * @param context Activity/ service context
     * @param environmentId Id of the environment whose details are required
     * @return Instance of environment with specified fields populated.
     */
    public static Environment getBareboneEnvironment(Context context, long environmentId){
        String selection = EnvironmentEntry._ID + " = ? ";
        String[] selectionArgs = new String[]{"" + environmentId};
        Cursor envCursor = context.getContentResolver().query(EnvironmentEntry.CONTENT_URI, null,
                selection, selectionArgs, null);
        envCursor.moveToFirst();
        Environment returnEnvironment = buildEnvironmentBareboneFromCursor(envCursor);
        envCursor.close();
        return returnEnvironment;
    }

    /**
     * Gets all the environment barebones for given location
     * @param context Activity/ service context
     * @param location Environments having this location will be returned. id of the location should
     *                 be populated
     * @return List of Environments that match the criteria
     */
    public static List<Environment> getAllEnvironmentBarebonesForLocation(
            Context context, LocationEnvironmentVariable location){
        if(location.getId() < 0){
            throw new IllegalArgumentException("Location should have a valid id.");
        }
        String selection = EnvironmentEntry.COLUMN_GEOFENCE_ID + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(location.getId())};
        Cursor envCursor = context.getContentResolver().query(EnvironmentEntry.CONTENT_URI, null,
                selection, selectionArgs, null);
        ArrayList<Environment> environments = new ArrayList<Environment>();

        for(envCursor.moveToFirst(); !envCursor.isAfterLast(); envCursor.moveToNext()){
            Environment e = buildEnvironmentBareboneFromCursor(envCursor);
            environments.add(e);
        }
        envCursor.close();
        return environments;
    }

    /**
     * Gets barebone of all environments which do not have location associated with it
     * @param context Activity/ service context
     * @return List of all environment (barebones) which does not have location
     */
    public static List<Environment> getAllEnvironmentBarebonesWithoutLocation(Context context){
        String selection = EnvironmentEntry.COLUMN_IS_LOCATION_ENABLED + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(0)};
        Cursor envCursor = context.getContentResolver().query(EnvironmentEntry.CONTENT_URI, null,
                selection, selectionArgs, null);
        ArrayList<Environment> environments = new ArrayList<Environment>();

        for(envCursor.moveToFirst(); !envCursor.isAfterLast(); envCursor.moveToNext()){
            Environment e = buildEnvironmentBareboneFromCursor(envCursor);
            environments.add(e);
        }
        envCursor.close();
        return environments;
    }

    /**
     * Builds an Environment object from a cursor containing values from environments table.
     * Only the barebone environment is build; the environment variables are not populated.
     * @param envCursor cursor containing data from environments table
     * @return Environment instance populated with data from the cursor
     */
    private static Environment buildEnvironmentBareboneFromCursor(Cursor envCursor){
        Environment e = new Environment();
        try {
            String envName = envCursor.getString(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_NAME));
            String envHint = envCursor.getString(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_ENVIRONMENT_HINT));
            long id = envCursor.getLong(envCursor.getColumnIndex(
                    EnvironmentEntry._ID));
            boolean enabled = envCursor.getInt(envCursor.getColumnIndex(
                    EnvironmentEntry.COLUMN_IS_ENABLED)) == 1;
            Picture picture = new Picture(
                    envCursor.getString(envCursor.getColumnIndex(EnvironmentEntry.COLUMN_ENVIRONMENT_PICTURE_TYPE)),
                    envCursor.getString(envCursor.getColumnIndex(EnvironmentEntry.COLUMN_ENVIRONMENT_PICTURE_DESCRIPTION)),
                    envCursor.getString(envCursor.getColumnIndex(EnvironmentEntry.COLUMN_ENVIRONMENT_PICTURE_DRAWABLE)),
                    envCursor.getBlob(envCursor.getColumnIndex(EnvironmentEntry.COLUMN_ENVIRONMENT_PICTURE)));
            e.setName(envName);
            e.setHint(envHint);
            e.setEnabled(enabled);
            e.setEnvironmentPicture(picture);
            e.id = id;

            return e;
        } catch (Exception ex){
            ex.printStackTrace();
            throw new IllegalArgumentException("Cursor should be populated with Environment table data.");
        }
    }

    /**
     * Function to delete an environment from the database
     * @param context Activity/ service context
     * @param environmentName Name of the environment to be deleted
     */
    public static void deleteEnvironmentFromDatabase(final Context context, final String environmentName){
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                Environment e = getFullEnvironment(context, environmentName);

                if(e != null){
                    context.getContentResolver().delete(EnvironmentEntry.
                            buildEnvironmentUriWithIdAndBluetooth(e.id), null, null);
                    context.getContentResolver().delete(EnvironmentEntry.
                            buildEnvironmentUriWithIdAndWifi(e.id), null, null);
                    int deletedEntries = context.getContentResolver().delete(EnvironmentEntry.
                            buildEnvironmentUriWithIdAndLocation(e.id), null, null);
                    context.getContentResolver().delete(EnvironmentEntry.buildEnvironmentUriWithId(
                            e.id), null, null);
                    if(deletedEntries > 0){
                        removeFromCurrentGeofences(e.getLocationEnvironmentVariable(), context);
                    }
                    SharedPreferencesHelper.removeEnvironmentFromOverlapPreferences(e.id, context);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                context.startService(BaseService.getServiceIntent(context, null,
                        BaseService.ACTION_DETECT_ENVIRONMENT));
            }
        }.execute();
    }

    private static void removeFromCurrentGeofences(LocationEnvironmentVariable variable, Context context){
        Intent intentToRemoveOldGeofenceMonitor = BaseService.getServiceIntent(context,
                null, BaseService.ACTION_REMOVE_GEOFENCES);
        ArrayList<String> geofencesToRemove = new ArrayList<String>();
        geofencesToRemove.add(String.valueOf(variable.getId()));
        intentToRemoveOldGeofenceMonitor.putExtra(BaseService.EXTRA_GEOFENCE_IDS_TO_REMOVE,
                geofencesToRemove);
        GeoFenceIntentService.removeFromCurrentGeofences(variable);
        context.startService(intentToRemoveOldGeofenceMonitor);
    }

    @Override
    public String toString() {
        return getName();
    }

    public Picture getEnvironmentPicture() {
        return environmentPicture;
    }

    /**
     * Sets environment picture. You should call {@link #updateEnvironmentPicture()} to reflect the changes in database
     * @param environmentPicture New picture for the environment
     */
    public void setEnvironmentPicture(Picture environmentPicture) {
        this.environmentPicture = environmentPicture;
    }

    /**
     * Gets the environment picture as a drawable
     * @param context Activity/ service context
     * @return Drawable corresponding to the environment picture
     */
    public Drawable getEnvironmentPictureDrawable(Context context){
        return environmentPicture.getDrawable(Character.toUpperCase(getName().charAt(0)), context);
    }

    /**
     * Updates the environment picture in database
     */
    public void updateEnvironmentPicture(){
        //TODO code this
    }
}
