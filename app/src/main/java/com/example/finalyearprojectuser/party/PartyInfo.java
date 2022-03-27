package com.example.finalyearprojectuser.party;

public class PartyInfo {

    public String partyHistory, partyImage, partyName, partyPropaganda;

    public PartyInfo(){}

    //Getter and Settle
    public String getPartyName(){return partyName;}

    public void setPartyName(String name){this.partyName = name; }

    public String getPartyImage(){return this.partyImage;};

    public void setPartyImage(String partyImage) {
        this.partyImage = partyImage;
    }

    public String getPartyHistory(){return partyHistory;}

    public void setPartyHistory(String history){this.partyHistory = history;}

    public String getPartyPropaganda(){return partyPropaganda;}

    public void setPartyPropaganda(String propaganda){this.partyPropaganda = propaganda;}
}
