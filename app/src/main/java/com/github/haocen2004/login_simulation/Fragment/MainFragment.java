package com.github.haocen2004.login_simulation.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.databinding.FragmentMainBinding;
import com.github.haocen2004.login_simulation.login.Bilibili;
import com.github.haocen2004.login_simulation.login.LoginImpl;
import com.github.haocen2004.login_simulation.login.Official;
import com.github.haocen2004.login_simulation.login.Oppo;
import com.github.haocen2004.login_simulation.login.UC;
import com.github.haocen2004.login_simulation.login.Vivo;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.QRScanner;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.Constant;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;
import java.util.Objects;

import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;
import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.util.Constant.OFFICIAL_TYPE;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_CAMERA;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_EXTERNAL_STORAGE;
import static com.github.haocen2004.login_simulation.util.Tools.changeToWDJ;

public class MainFragment extends Fragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private LoginImpl loginImpl;
    private AppCompatActivity activity;
    private Context context;
    private Boolean isOfficial = false;
    private SharedPreferences pref;
    private FragmentMainBinding binding;
    private final String TAG = "MainFragment";
    private Logger Log;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (AppCompatActivity) getActivity();
        context = getContext();
        pref = getDefaultSharedPreferences(context);
        Log = Logger.getLogger(getContext());
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnLogin.setOnClickListener(this);
        binding.btnScan.setOnClickListener(this);
        binding.btnLogout.setOnClickListener(this);
        binding.officialSlotSelect.setOnCheckedChangeListener(this);
        binding.officialTypeSel.setOnCheckedChangeListener(this);
        binding.tokenCheckBox.setOnCheckedChangeListener((compoundButton, b) -> pref.edit().putBoolean("use_token", b).apply());
        binding.checkBoxWDJ.setOnCheckedChangeListener((compoundButton, b) -> {
            SharedPreferences ucsp = activity.getSharedPreferences("cn.uc.gamesdk.pref", 0);
//            activity.getSharedPreferences("cn.uc.gamesdk.pref.usr_simple_cache",0).edit().clear().apply();
            pref.edit().putBoolean("use_wdj", b).apply();
            if (b) {
                changeToWDJ(activity);
            } else {
                ucsp.edit().clear().apply();
            }
        });
        String server_type = "DEBUG SERVER ERROR";
        binding.officialSlotSelect.setVisibility(View.GONE);
        binding.tokenCheckBox.setVisibility(View.GONE);
        binding.officialTypeSel.setVisibility(View.GONE);
        binding.checkBoxWDJ.setVisibility(View.GONE);
        switch (Objects.requireNonNull(pref.getString("server_type", ""))) {
            case "Official":
                server_type = getString(R.string.types_official);
                binding.officialSlotSelect.setVisibility(View.VISIBLE);
                binding.tokenCheckBox.setVisibility(View.VISIBLE);
                binding.officialTypeSel.setVisibility(View.VISIBLE);
                switch (pref.getInt("official_slot", 1)) {
                    case 1:
                        binding.officialSlotSelect.check(binding.slot1.getId());
                        break;
                    case 2:
                        binding.officialSlotSelect.check(binding.slot2.getId());
                        break;
                    case 3:
                        binding.officialSlotSelect.check(binding.slot3.getId());
                        break;
                }
                switch (pref.getInt("official_type", 0)) {
                    case 1:
                        binding.officialTypeSel.check(binding.radioPc.getId());
                        break;
                    case 2:
                        binding.officialTypeSel.check(binding.radioIOS.getId());
                        break;
                    default:
                        binding.officialTypeSel.check(binding.radioAndroid.getId());
                        break;
                }
                binding.tokenCheckBox.setChecked(pref.getBoolean("use_token", false));
                break;
            case "Bilibili":
                server_type = getString(R.string.types_bilibili);
                break;
            case "Xiaomi":
                server_type = getString(R.string.types_xiaomi);
                break;
            case "UC":
                server_type = getString(R.string.types_uc);
                binding.checkBoxWDJ.setVisibility(View.VISIBLE);
                binding.checkBoxWDJ.setChecked(pref.getBoolean("use_wdj", false));
                break;
            case "Vivo":
                server_type = getString(R.string.types_vivo);
                break;
            case "Oppo":
                server_type = getString(R.string.types_oppo);
                break;
            case "Flyme":
                server_type = getString(R.string.types_flyme);
                break;
            default:
                server_type = "DEBUG -- SERVER ERROR";
        }
        binding.textSelectServer.setText(getString(R.string.types_prefix) + server_type);
        binding.textAutoConfirm.setText(getString(R.string.confirm_prefix) + (pref.getBoolean("auto_confirm", false) ? getString(R.string.boolean_true) : getString(R.string.boolean_false)));
        checkPermissions();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                String result = bundle.getString(com.github.haocen2004.login_simulation.util.Constant.INTENT_EXTRA_KEY_QR_SCAN);
                if (result != null) {
                    QRScanner qrScanner;
                    if (isOfficial) {
                        qrScanner = new QRScanner(activity, true);
                    } else {
                        qrScanner = new QRScanner(activity, loginImpl.getRole());
                    }
                    if (!qrScanner.parseUrl(result)) return;
                    qrScanner.getScanRequest();
                } else {
                    makeToast(getString(R.string.error_scan));
                }
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERM_CAMERA:
            case REQ_PERM_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startQrCode();
                } else {
                    Toast.makeText(context, R.string.request_permission_failed, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void startQrCode() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission
                    .CAMERA)) {
                Toast.makeText(context, R.string.request_permission_failed, Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, REQ_PERM_CAMERA);
            return;
        }
        Intent intent = new Intent(context, CaptureActivity.class);
        startActivityForResult(intent, com.github.haocen2004.login_simulation.util.Constant.REQ_QR_CODE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                if (Objects.equals(pref.getString("server_type", ""), "Official") && pref.getBoolean("use_token", false) && activity.getSharedPreferences("official_user_" + pref.getInt("official_slot", 1), Context.MODE_PRIVATE).getBoolean("has_token", false)) {
                    makeToast("Token 登录模式");
                    isOfficial = true;
                    startQrCode();
                    return;
                }
                try {
                    if (loginImpl.isLogin()) {
                        if (loginImpl.getRole().is_setup()) {
                            startQrCode();
                        } else {
                            makeToast(getString(R.string.error_oa_process));
                        }
                    } else {
                        makeToast(getString(R.string.error_not_login));
                    }
                } catch (NullPointerException e) {
//                    e.printStackTrace();
                    makeToast(getString(R.string.error_not_login));
                }
                break;
            case R.id.btn_login:
                try {
                    if (loginImpl.isLogin()) {
                        makeToast(getString(R.string.has_login));
                        return;
                    }
                } catch (Exception ignore) {
                }
                switch (Objects.requireNonNull(pref.getString("server_type", ""))) {
                    case "Official":
                        loginImpl = new Official(activity);
                        break;
//                    case "Xiaomi":
//                        loginImpl = new Xiaomi(activity);
//                        //11
//                        break;
                    case "Bilibili":
                        loginImpl = new Bilibili(activity);
                        //14
                        break;
                    case "UC":
                        if (pref.getBoolean("use_wdj", false)) {
                            changeToWDJ(activity);
                        }
                        loginImpl = new UC(activity);
                        //20
                        break;
                    case "Vivo":
                        loginImpl = new Vivo(activity);
                        break;
                    case "Oppo":
                        loginImpl = new Oppo(activity);
                        break;
//                    case "Flyme":
//                        loginImpl = new Flyme(activity);
//                        break;
                    default:
                        makeToast(getString(R.string.error_wrong_server));
                        break;
                }
                loginImpl.login();
                break;
            case R.id.btn_logout:
                try {
                    if ("Official".equals(pref.getString("server_type", ""))) {
                        activity.getSharedPreferences("official_user", Context.MODE_PRIVATE).edit().clear().apply();
                        makeToast(getString(R.string.cache_delete));
                    }
                    if (loginImpl.isLogin()) {
                        loginImpl.logout();
                    }
                } catch (Exception e) {
                    makeToast(getString(R.string.error_not_login));
                }
                break;
            default:
                break;
        }

    }


    @SuppressLint("ShowToast")
    private void makeToast(String result) {
        try {

            Log.makeToast(result);
//            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Looper.prepare();
            Log.makeToast(result);
            Looper.loop();
        }
    }

    private void checkPermissions() {

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder normalDialog = new AlertDialog.Builder(getContext());
            normalDialog.setTitle("权限说明");
            normalDialog.setMessage("使用扫码器需要以下权限:\n1.使用摄像头\n用于扫描登录二维码\n\n2.读取设备文件\n用于提供相册扫码\n\n其他权限为各家SDK适配所需\n可不授予权限");
            normalDialog.setPositiveButton("我已知晓",
                    (dialog, which) -> {
                        XXPermissions.with(this)
                                .permission(Permission.CAMERA)
                                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                                .request(new OnPermissionCallback() {
                                    @Override
                                    public void onGranted(List<String> permissions, boolean all) {

                                    }

                                    @Override
                                    public void onDenied(List<String> permissions, boolean never) {

                                    }
                                });
                        dialog.dismiss();
                    });
            normalDialog.setCancelable(false);
            normalDialog.show();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        SharedPreferences app_pref = getDefaultSharedPreferences(activity);
        switch (i) {
            case R.id.slot1:
                app_pref.edit().putInt("official_slot", 1).apply();
                Logger.d(TAG, "onCheckedChanged: Switch To Slot 1");
                break;
            case R.id.slot2:
                app_pref.edit().putInt("official_slot", 2).apply();
                Logger.d(TAG, "onCheckedChanged: Switch To Slot 2");
                break;
            case R.id.slot3:
                app_pref.edit().putInt("official_slot", 3).apply();
                Logger.d(TAG, "onCheckedChanged: Switch To Slot 3");
                break;
            case R.id.radioPc:
                app_pref.edit().putInt("official_type", 1).apply();
                Logger.d(TAG, "onCheckedChanged: Switch To PC");
                break;
            case R.id.radioAndroid:
                app_pref.edit().putInt("official_type", 0).apply();
                Logger.d(TAG, "onCheckedChanged: Switch To Android");
                break;
            case R.id.radioIOS:
                app_pref.edit().putInt("official_type", 2).apply();
                Logger.d(TAG, "onCheckedChanged: Switch To IOS");
                break;
        }
        resetOfficialServerType();
        try {
            if (loginImpl.isLogin()) {
                loginImpl = new Official(activity);
                makeToast("切换后需重新登录");
            }
        } catch (Exception ignore) {
        }
//        Toast.makeText(getContext(), i+"", Toast.LENGTH_SHORT).show();
    }

    private void resetOfficialServerType() {
        int i = getDefaultSharedPreferences(activity).getInt("official_type", 0);
        Logger.d(TAG, "resetOfficialServerType: " + i);
        switch (i) {
            case 1:
                OFFICIAL_TYPE = "pc01";
                break;
            case 2:
                OFFICIAL_TYPE = "ios01";
                break;
            default:
                OFFICIAL_TYPE = "android01";
                break;
        }
        Logger.d(TAG, "resetOfficialServerType: " + OFFICIAL_TYPE);
    }
}