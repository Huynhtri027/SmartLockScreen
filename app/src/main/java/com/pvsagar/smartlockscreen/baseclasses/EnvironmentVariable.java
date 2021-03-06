package com.pvsagar.smartlockscreen.baseclasses;

import android.content.ContentValues;
import android.util.Log;

import com.pvsagar.smartlockscreen.backend_helpers.Utility;

/**
 * Created by aravind on 5/8/14.
 *
 * Abstract class for Environment Variables. Each variable will extend it and use it accordingly
 */
public abstract class EnvironmentVariable {
    private static final String LOG_TAG = EnvironmentVariable.class.getSimpleName();

    /**
     * Constant values for use
     */
    public static final String TYPE_LOCATION = "com.pvsagar.applogic_objects.TYPE_LOCATION";
    public static final String TYPE_BLUETOOTH_DEVICES =
            "com.pvsagar.applogic_objects.TYPE_BLUETOOTH_DEVICES";
    public static final String TYPE_WIFI_NETWORKS =
            "com.pvsagar.applogic_objects.TYPE_WIFI_NETWORKS";
    public static final String TYPE_NOISE_LEVEL = "com.pvsagar.applogic_objects.TYPE_NOISE_LEVEL";

    protected long id = -1;

    /**
     * Stores the numeric values associated with the variable
     */
    private double[] floatValues;

    /**
     * Stores the String values associated with the variable
     */
    private String[] stringValues;

    /**
     * Stores the variable type. This will be one of the TYPE_* strings defined above
     */
    private String variableType;

    /**
     * To keep track of whether the variable has been initialized with a valid type
     */
    private boolean isInitialized = false;

    /**
     * Constructor for EnvironmentVariable
     * @param variableType Type of environment variable. Should be one of the TYPE_* strings
     *                     defined above
     * @param numberOfFloatValues
     * @param numberOfStringValues
     */
    public EnvironmentVariable(String variableType,
                               int numberOfFloatValues, int numberOfStringValues){
        Utility.checkForNullAndThrowException(variableType);
        boolean isValid = checkTypeValidity(variableType) && !isInitialized;
        if(isValid){
            isInitialized = true;
            this.variableType = variableType;
            setFloatValues(new double[numberOfFloatValues]);
            setStringValues(stringValues = new String[numberOfStringValues]);
        } else {
            throw new IllegalArgumentException("Cannot initialize EnvironmentVariable with type "
                    + variableType);
        }
    }

    public EnvironmentVariable(String variableType, double[] floatValues, String[] stringValues){
        Utility.checkForNullAndThrowException(variableType);
        boolean isValid = checkTypeValidity(variableType) && !isInitialized;
        if(!isValid) {
            Log.e(LOG_TAG, "Initialization of environment variable not valid. Type: " + variableType);
            throw new IllegalArgumentException("Cannot initialize EnvironmentVariable with type "
                    + variableType);
        }
        isInitialized = true;
        this.variableType = variableType;
        setFloatValues(floatValues);
        setStringValues(stringValues);
    }

    /**
     * Set the floatValues array.
     * @param floatValues The values to be set
     * @return true if values have been set, false if it cannot be set.
     */
    protected boolean setFloatValues(double[] floatValues){
        if(isFloatValuesSupported() && isInitialized) {
            this.floatValues = floatValues;
            return true;
        }
        return false;
    }

    /**
     * Set a single double value at the specified index, in the floatValues array
     * @param floatValue
     * @param index
     * @return true if value has been set, false if it cannot be set.
     */
    protected boolean setFloatValue(double floatValue, int index){
        if(isFloatValuesSupported() && isInitialized) {
            this.floatValues[index] = floatValue;
            return true;
        }
        return false;
    }

    /**
     * Set the stringValues array
     * @param stringValues The values to be set
     * @return true if values have been set, false if it cannot be set.
     */
    protected boolean setStringValues(String[] stringValues){
        if(isStringValuesSupported() && isInitialized) {
            this.stringValues = stringValues;
            return true;
        }
        return false;
    }

    /**
     * Set a single string value at the specified index, in the stringValues array
     * @param stringValue
     * @param index
     * @return true if value has been set, false if it cannot be set.
     */
    protected boolean setStringValue(String stringValue, int index){
        if(isStringValuesSupported() && isInitialized) {
            this.stringValues[index] = stringValue;
            return true;
        }
        return false;
    }

    public String getVariableType(){
        if(isInitialized){
            return variableType;
        }
        else return null;
    }

    private boolean checkTypeValidity(String variableType){
        return variableType.equals(TYPE_BLUETOOTH_DEVICES) || variableType.equals(TYPE_WIFI_NETWORKS)
                || variableType.equals(TYPE_NOISE_LEVEL) || variableType.equals(TYPE_LOCATION);
    }

    /**
     * This method indicates whether string values are in  used to store data related to this environment variable
     * @return true if string values are used, false otherwise
     */
    public abstract boolean isStringValuesSupported();

    /**
     * This method indicates whether real values are in  used to store data related to this environment variable.
     * Although the function name says float, in practise double is the data tye used to store the values.
     * @return true if real values are used, false otherwise
     */
    public abstract boolean isFloatValuesSupported();

    protected double[] getFloatValues(){
        if(isFloatValuesSupported() && isInitialized){
            return floatValues;
        }
        else{
            return null;
        }
    }

    public double getFloatValue(int index) throws UnsupportedOperationException, ArrayIndexOutOfBoundsException{
        if(isFloatValuesSupported() && isInitialized){
            return floatValues[index];
        }
        else{
            throw new UnsupportedOperationException("No double values associated with " + variableType) ;
        }
    }

    public String[] getStringValues(){
        if(isStringValuesSupported() && isInitialized){
            return stringValues;
        }
        else{
            return null;
        }
    }

    public String getStringValue(int index) throws UnsupportedOperationException, ArrayIndexOutOfBoundsException{
        if(isStringValuesSupported() && isInitialized){
            return stringValues[index];
        }
        else{
            throw new UnsupportedOperationException("No String values associated with " + variableType) ;
        }
    }

    /**
     * Returns an instance of ContentValues with data from this variable
     * @return an instance of ContentValues with data from this variable
     */
    public abstract ContentValues getContentValues();

    @Override
    public boolean equals(Object o) {
        if(o == null){
            return false;
        }
        Class<?> oClass = o.getClass();
        if(!oClass.getSuperclass().getSimpleName().equals(EnvironmentVariable.class.getSimpleName())
                && !oClass.getSimpleName().equals(EnvironmentVariable.class.getSimpleName())){
            return false;
        }
        EnvironmentVariable e = (EnvironmentVariable) o;
        if(!getVariableType().equals(e.getVariableType())) return false;
        if(isStringValuesSupported()){
            if(!e.isStringValuesSupported() || stringValues == null || e.stringValues == null
                    || stringValues.length != e.stringValues.length)
                return false;
            for (int i = 0; i < stringValues.length; i++) {
                if(!stringValues[i].equals(e.stringValues[i])) {
                    return false;
                }
            }
        }
        if(isFloatValuesSupported()){
            if(!e.isFloatValuesSupported() || floatValues == null || e.floatValues == null ||
                    floatValues.length != e.floatValues.length)
                return false;
            for (int i = 0; i < floatValues.length; i++) {
                if(!(floatValues[i] == e.floatValues[i])){
                    return false;
                }
            }
        }
        return true;
    }

    public long getId(){
        return id;
    }
}
