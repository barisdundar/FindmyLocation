package com.barisdundar.findmylocation.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.barisdundar.findmylocation.R;
import com.barisdundar.findmylocation.adapter.PlaceAdapter;
import com.barisdundar.findmylocation.databinding.ActivityMainBinding;
import com.barisdundar.findmylocation.model.Place;
import com.barisdundar.findmylocation.roomdb.PlaceDao;
import com.barisdundar.findmylocation.roomdb.PlaceDatabase;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
private ActivityMainBinding binding;
private CompositeDisposable compositeDisposable=new CompositeDisposable();
PlaceDatabase db;
PlaceDao placeDao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);
        db= Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places")
                //  .allowMainThreadQueries()
                .build();
        placeDao=db.placeDao();
     compositeDisposable.add(placeDao.getAll()
             .subscribeOn(Schedulers.io())
             .observeOn(AndroidSchedulers.mainThread())
             .subscribe(MainActivity.this::handleResponse)
     );

    }



    private void handleResponse(List<Place>placeList){
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        PlaceAdapter placeAdapter=new PlaceAdapter(placeList);
        binding.recyclerView.setAdapter(placeAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.travel_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.add_place){
            Intent intent=new Intent(MainActivity.this,MapsActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}