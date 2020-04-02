package com.example.risingindians;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private String current_user_id;
    private FloatingActionButton addpostbtn;

    private BottomNavigationView navigationView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private EventsFragment eventsFragment;
    private DonateFragment donateFragment;
    private TimelineFragment timelineFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mainToolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Rising Indians");

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if(mAuth.getCurrentUser() != null) {
            navigationView = findViewById(R.id.bottom_navigation);
            //navigationView.setOnNavigationItemSelectedListener(this);
            //loadFragment(new EventsFragment());

            // FRAGMENTS
            eventsFragment = new EventsFragment();
            donateFragment = new DonateFragment();
            timelineFragment = new TimelineFragment();
            accountFragment = new AccountFragment();

            initializeFragment();

            navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                    switch (item.getItemId()) {

                        case R.id.navigation_home:

                            replaceFragment(eventsFragment, currentFragment);
                            return true;

                        case R.id.navigation_account:

                            replaceFragment(accountFragment, currentFragment);
                            return true;

                        case R.id.navigation_donate:

                            replaceFragment(donateFragment, currentFragment);
                            return true;

                        case R.id.navigation_timeline:

                            replaceFragment(timelineFragment, currentFragment);
                            return true;

                        default:
                            return false;


                    }
                }
            });


            addpostbtn = findViewById(R.id.add_post_btn);
            addpostbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, NewEventActivity.class);
                    startActivity(intent);
                }
            });
        }

    }



    /*private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment = null;

        switch (item.getItemId()){

            case R.id.navigation_home:
                fragment = new EventsFragment();
                break;

            case R.id.navigation_donate:
                fragment = new DonateFragment();
                break;

            case R.id.navigation_timeline:
                fragment = new TimelineFragment();
                break;

            case R.id.navigation_account:
                fragment = new AccountFragment();
                break;

        }
        return loadFragment(fragment);
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.top_menu,menu);

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            sendToLogin();
        }
        else{
            current_user_id = mAuth.getCurrentUser().getUid();

            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful()){

                        if(!task.getResult().exists()){

                            Intent setupIntent = new Intent(HomeActivity.this, AccountSetupActivity.class);
                            startActivity(setupIntent);
                            finish();

                        }

                    } else {

                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(HomeActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();


                    }

                }
            });


        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_logout_btn:
                logout();
                return true;

            case R.id.action_settings_btn:
                Intent settingsintent = new Intent(HomeActivity.this, AccountSetupActivity.class);
                startActivity(settingsintent);
                return true;
                
            default:
                return false;

        }
      
    }

    private void logout() {
        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initializeFragment(){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.fragment_container, eventsFragment);
        fragmentTransaction.add(R.id.fragment_container, donateFragment);
        fragmentTransaction.add(R.id.fragment_container, timelineFragment);
        fragmentTransaction.add(R.id.fragment_container, accountFragment);

        fragmentTransaction.hide(donateFragment);
        fragmentTransaction.hide(timelineFragment);
        fragmentTransaction.hide(accountFragment);

        fragmentTransaction.commit();

    }

    private void replaceFragment(Fragment fragment, Fragment currentFragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(fragment == eventsFragment){

            fragmentTransaction.hide(donateFragment);
            fragmentTransaction.hide(timelineFragment);
            fragmentTransaction.hide(accountFragment);

        }

        if(fragment == accountFragment){

            fragmentTransaction.hide(eventsFragment);
            fragmentTransaction.hide(donateFragment);
            fragmentTransaction.hide(timelineFragment);

        }

        if(fragment == donateFragment){

            fragmentTransaction.hide(eventsFragment);
            fragmentTransaction.hide(timelineFragment);
            fragmentTransaction.hide(accountFragment);

        }
        if(fragment == timelineFragment){

            fragmentTransaction.hide(eventsFragment);
            fragmentTransaction.hide(donateFragment);
            fragmentTransaction.hide(accountFragment);

        }
        fragmentTransaction.show(fragment);

        //fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

    }
}
