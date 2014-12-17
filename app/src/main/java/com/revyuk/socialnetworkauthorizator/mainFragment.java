package com.revyuk.socialnetworkauthorizator;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class mainFragment extends Fragment {
    OnSelectNetworkListener listener;
    ListView list;
    String[] nets;
    ArrayAdapter<String> adapter;

    public mainFragment() { }

    interface OnSelectNetworkListener {
        void onSelectNet(String s);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        list = (ListView) rootView.findViewById(R.id.listNetworks);
        nets = getResources().getStringArray(R.array.socialNetworkList);
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, nets);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(listener!=null) listener.onSelectNet(nets[position]);
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (OnSelectNetworkListener) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
