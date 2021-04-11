package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link playlist#newInstance} factory method to
 * create an instance of this fragment.
 */
public class playlist extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String EXTRA_MESSAGE = "position";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // playlist content
    ListView listView;

    DatabaseReference myRef;
    // creating ArrayLists to store our songs
    final ArrayList<Music> songs = new ArrayList<Music>();
    PlaylistAdapter adapter;

    public playlist() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment playlist.
     */
    // TODO: Rename and change types and number of parameters
    public static playlist newInstance(String param1, String param2) {
        playlist fragment = new playlist();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        //fetch data from firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // literate snapshot child
                for(DataSnapshot ds: snapshot.getChildren()){
//                    songTitles.add(ds.child("songtitles").getValue(String.class));
//                    songArtists.add(ds.child("songartists").getValue(String.class));
//                    songUrls.add(ds.child("songurls").getValue(String.class));
//                    imgUrls.add(ds.child("imgurls").getValue(String.class));
                    songs.add(new Music(
                            ds.child("songtitles").getValue(String.class),
                            ds.child("songartists").getValue(String.class),
                            ds.child("songurls").getValue(String.class),
                            ds.child("imgurls").getValue(String.class)
                    ));

                }
                listView = view.findViewById(R.id.song_list);
                adapter = new PlaylistAdapter(getActivity(),R.layout.list,songs);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //String item = songs.get(position);
                        Intent i = new Intent(getActivity(), MainActivity.class);
                        i.putExtra(EXTRA_MESSAGE,  String.valueOf(position));
                        startActivity(i);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        return view;
    }

}