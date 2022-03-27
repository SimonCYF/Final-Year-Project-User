package com.example.finalyearprojectuser.candidate;

public class CandidateInfo {

    public String candidateName, candidateImage, candidateState, candidatePropaganda, candidateParty;
    public int candidateTotalVotes;

    public CandidateInfo(){}

    //Getter and Settle
    public String getCandidateName(){
        return this.candidateName;
    }

    public void setCandidateName(String a){
        this.candidateName = a;
    }

    public String getCandidateImage() {return this.candidateImage;};

    public void setCandidateImage(String a) {this.candidateImage = a;}

    public String getCandidateState(){
        return this.candidateState;
    }

    public void setCandidateState(String a){
        this.candidateState = a;
    }

    public String getCandidatePropaganda(){
        return this.candidatePropaganda;
    }

    public void setCandidatePropaganda(String a){
        this.candidatePropaganda = a;
    }

    public String getCandidateParty(){
        return this.candidateParty;
    }

    public void setCandidateTotalVotes(int a){ this.candidateTotalVotes = a;}

    public int getCandidateTotalVotes(){ return this.candidateTotalVotes;}

    public void setCandidateParty(String a){
        this.candidateParty = a;
    }
}
