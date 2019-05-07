package root.example.com.monggopinarak.Admin;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import root.example.com.monggopinarak.R;

public class GenerateReport extends Fragment {
    public GenerateReport newInstance() {
        return new GenerateReport();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_generate_report, container, false);
    }
}
