package anwarabdullahn.com.villacorp_apps.Utils;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import anwarabdullahn.com.villacorp_apps.R;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

public class LoadingHelper extends DialogFragment {

    public static LoadingHelper getInstance(){
        return new LoadingHelper();
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.LoadingScreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.utils_loading,container,false);
    }

}
