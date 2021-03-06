package com.group5.charryt.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group5.charryt.R;
import com.group5.charryt.Utils;
import com.group5.charryt.data.User;

import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static MainActivity mainActivity = null;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI components
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Menu navigationMenu;
    private Toolbar toolbar;
    private FrameLayout fragmentHolder;
    private MenuItem dashboardMenuItem;
    private TextView loadingText;

    private MenuItem lastSelectedNavMenuItem = null;

    private Fragment currentFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainActivity = this;
        Utils.currentContext = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null)
            goToActivity(LoginActivity.class);

        // Instantiate references to UI components
        navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        loadingText = findViewById(R.id.loadingText);
        navigationMenu = navigationView.getMenu();

        toolbar = findViewById(R.id.toolbar);
        fragmentHolder = findViewById(R.id.fragmentHolder);

        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        assert actionbar != null;
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        login();


    }

    public void login() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            goToActivity(LoginActivity.class);
            finish();
            return;
        }
        navigationMenu.add("Please wait...");
        String message = "Logging in as " + Objects.requireNonNull(mAuth.getCurrentUser()).getEmail() + ", please wait...";
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();

        CollectionReference usersCollection = db.collection("users");
        assert user != null;
        usersCollection.document(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User currentUser = Objects.requireNonNull(task.getResult()).toObject(User.class);
                assert currentUser != null;
                currentUser.setId(mAuth.getUid());
                User.setCurrentUser(currentUser);
                init();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.showDialog("Failed to connect to server: " + e.toString());
            }
        });
    }

    public void init() {
        // Initialise maps stuff here to prevent having to do it later
        try {
            MapsInitializer.initialize(getBaseContext());
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        navigationMenu.clear();
        loadingText.setVisibility(View.INVISIBLE);
        // Load dashboard on startup
        swapFragment(new DashboardFragment());

        dashboardMenuItem = navigationMenu.add("Dashboard");
        dashboardMenuItem.setChecked(true);
        lastSelectedNavMenuItem = dashboardMenuItem;

        // Add items to nav menu here. Remember to actually implement them down below.
        navigationMenu.add("Add a listing");
        navigationMenu.add("View listings");
        navigationMenu.add("View messages");
        navigationMenu.add("View bookings");

        if (User.getCurrentUser().getUserType() == User.UserType.Donor) {
            navigationMenu.add("View charities");
        } else {
            navigationMenu.add("View users");
        }

        navigationMenu.add("Profile Details");
        navigationMenu.add("Login");
        navigationMenu.add("Register");
        navigationMenu.add("Fortnite");

        // Link functions to nav menu items in the switch/case below.
        // Remember that this is case-sensitive.
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        System.out.println("Pressed " + menuItem);

                        // Close drawer when item is tapped
                        drawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        String itemName = menuItem.getTitle().toString();
                        switch (itemName) {
                            case "Dashboard":
                                swapFragment(new DashboardFragment());
                                setChecked(menuItem);
                                break;
                            case "View listings":
                                swapFragment(new ViewListingsFragment());
                                setChecked(menuItem);
                                break;
                            case "History":
                                swapFragment(new HistoryFragment());
                                setChecked(menuItem);
                                break;
                            case "View bookings":
                                swapFragment(new ViewBookingsFragment());
                                setChecked(menuItem);
                                break;
                            case "View messages":
                                swapFragment(new ViewMessagesFragment());
                                setChecked(menuItem);
                                break;
                            case "View users":
                                swapFragment(new ViewUsersFragment());
                                setChecked(menuItem);
                                break;
                            case "View charities":
                                swapFragment(new ViewUsersFragment());
                                setChecked(menuItem);
                                break;
                            case "Login":
                                goToActivity(LoginActivity.class);
                                break;
                            case "Profile Details":
                                swapFragment(new ProfileFragment());
                                break;
                            case "Register":
                                goToActivity(RegisterActivity.class);
                                break;
                            case "Add a listing":
                                goToActivity(CreateListingActivity.class);
                                break;
                            case "MapsActivity":
                                goToActivity(MapsActivity.class);
                                break;
                            default:
                                String error = "ERROR: No function implemented for " + itemName;
                                showDialog(error);
                                System.out.println(error);
                        }

                        return true;
                    }
                });
    }

    public void goToActivity(Class activityClass) {
        Intent startNewActivityOpen = new Intent(MainActivity.this, activityClass);
        startActivityForResult(startNewActivityOpen, 0);
    }

    public void swapFragment(Fragment fragment) {
        // Start up weird fragment manager nonsense
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // Remove previous fragment
        try {
            if (currentFragment != null)
                getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
            // Murder "back stack" so shit doesn't overlap
            // (why did this take me 45 minutes to figure out and why is this weird "back stack" thing
            // not cleared in the first place what the actual hell google what is wrong with you)
            fragmentManager.popBackStack();
            transaction.addToBackStack(null);
        } catch (IllegalStateException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }



        // Add new fragment
        transaction.add(R.id.fragmentHolder, fragment);

        transaction.commitAllowingStateLoss();
        currentFragment = fragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        System.out.println("Pressed " + item);
        // Break is not needed for this switch case because return is used.
        switch (id) {
            case R.id.action_settings:
                return true;

            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Make the back button minimise the app on the main page like most apps if there's nothing to go back to
        // (this also needs to be overridden anyway to prevent the fragments from getting destroyed)

        // If not on the dashboard, go to it instead of minimising the app.
        if (!(currentFragment instanceof DashboardFragment) && currentFragment != null) {
            swapFragment(new DashboardFragment());
            setChecked(dashboardMenuItem);
            return;
        }

        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private void setChecked(MenuItem menuItem) {
        if (lastSelectedNavMenuItem != null)
            lastSelectedNavMenuItem.setChecked(false);
        menuItem.setChecked(true);
        lastSelectedNavMenuItem = menuItem;
    }

    public void setToolbarText(String str) {
        toolbar.setTitle(str);
    }

    // Created to avoid shenanigans with inner classes
    public void showDialog(String str) {
        Utils.showDialog(str, this);
    }


    public Fragment getCurrentFragment() {
        return currentFragment;
    }

    public void setCurrentFragment(Fragment currentFragment) {
        this.currentFragment = currentFragment;
    }
}

//YEET TEST COMMENT - VONG
// yeet 2 not jasmine