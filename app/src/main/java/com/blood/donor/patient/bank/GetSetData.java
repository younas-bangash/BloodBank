package com.blood.donor.patient.bank;

class GetSetData {
    private String mLastName;
    private String mContact;
    private String mEmail;
    private String mBloodGroup;
    private String mCatagory;
    private String mLoc;
    private String mRecodeID;
    private String mFirstName;
    private String mMapID;

    public GetSetData() {
    }

    public GetSetData(String mFirstName, String mLastName, String mContact, String mEmail,
                      String mBloodGroup, String mCatagory, String mLoc,String mapid) {
        this.mFirstName = mFirstName;
        this.mLastName = mLastName;
        this.mContact = mContact;
        this.mEmail = mEmail;
        this.mBloodGroup = mBloodGroup;
        this.mCatagory = mCatagory;
        this.mLoc = mLoc;
        this.mMapID=mapid;
    }

    public String getmMapID() {
        return mMapID;
    }

    public void setmMapID(String mMapID) {
        this.mMapID = mMapID;
    }

    public void setmRecodeID(String mRecodeID) {
        this.mRecodeID = mRecodeID;
    }

    public String getmRecodeID() {
        return mRecodeID;
    }

    public void setmFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public void setmLastName(String mLastName) {
        this.mLastName = mLastName;
    }

    public void setmContact(String mContact) {
        this.mContact = mContact;
    }

    public void setmEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public void setmBloodGroup(String mBloodGroup) {
        this.mBloodGroup = mBloodGroup;
    }

    public void setmCatagory(String mCatagory) {
        this.mCatagory = mCatagory;
    }

    public void setmLoc(String mLoc) {
        this.mLoc = mLoc;
    }

    public String getmFirstName() {
        return mFirstName;
    }

    public String getmLoc() {
        return mLoc;
    }

    public String getmCatagory() {
        return mCatagory;
    }

    public String getmBloodGroup() {
        return mBloodGroup;
    }

    public String getmEmail() {
        return mEmail;
    }

    public String getmContact() {
        return mContact;
    }

    public String getmLastName() {
        return mLastName;
    }


}
