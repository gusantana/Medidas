package com.mysticlabs.medidas;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Gustavo on 16/01/2017.
 */

public class MedidasFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //infla o layout do fragmento
        return inflater.inflate(R.layout.medidas_fragment, container, false);
    }
}
