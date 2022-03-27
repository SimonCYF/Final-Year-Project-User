package com.example.finalyearprojectuser.blockchain;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BlockActivity extends AppCompatActivity {

    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static int difficulty = 5;
    public static int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    //check if the current and previous block is valid
    public Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        //loop through blockchain to check hashes:
        for(int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                Toast.makeText(getApplicationContext(),"Current Hash Not Equal", Toast.LENGTH_SHORT).show();
                //textView2.setText("Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                //textView2.setText("Previous Hashes not equal");
                Toast.makeText(getApplicationContext(),"Previous Hash Not Equal", Toast.LENGTH_SHORT).show();
                return false;
            }
            //check if hash is solved
            if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
                //textView2.setText("This block hasn't been mined");
                Toast.makeText(getApplicationContext(),"Block Not Mined", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }


}
