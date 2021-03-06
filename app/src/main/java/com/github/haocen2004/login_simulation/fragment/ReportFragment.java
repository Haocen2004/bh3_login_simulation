package com.github.haocen2004.login_simulation.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.databinding.FragmentReportBinding;
import com.github.haocen2004.login_simulation.util.Logger;
import com.tencent.bugly.crashreport.CrashReport;

import static com.github.haocen2004.login_simulation.util.Logger.getLogger;
import static com.github.haocen2004.login_simulation.util.Tools.openUrl;

public class ReportFragment extends Fragment implements View.OnClickListener {
    private FragmentReportBinding binding;
    private Logger Log;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReportBinding.inflate(inflater, container, false);
//        Logger.setView(binding.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        openUrl("https://github.com/Haocen2004/bh3_login_simulation/issues", requireActivity());
        binding.reportGithub.setOnClickListener(this);
        binding.reportBili.setOnClickListener(this);
        binding.reportQq.setOnClickListener(this);
        binding.reportHand.setOnClickListener(this);
        Log = getLogger(getContext());
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        if (binding.reportGithub.equals(view)) {
            openUrl("https://github.com/Haocen2004/bh3_login_simulation/issues", requireActivity());
        } else if (binding.reportBili.equals(view)) {
            openUrl("https://space.bilibili.com/269140934", requireActivity());
        } else if (binding.reportQq.equals(view)) {
            final AlertDialog.Builder normalDialog = new AlertDialog.Builder(getActivity());
            normalDialog.setTitle("加群暗号");
            normalDialog.setMessage("扫码器");
            normalDialog.setPositiveButton("打开加群界面",
                    (dialog, which) -> {
                        openUrl("https://jq.qq.com/?_wv=1027&k=v4Z91CMR", requireActivity());
                        dialog.dismiss();
                    });
            normalDialog.setNegativeButton(R.string.btn_cancel,
                    (dialog, which) -> dialog.dismiss());
            normalDialog.setCancelable(false);
            normalDialog.show();
        } else if (binding.reportHand.equals(view)) {
            final AlertDialog.Builder normalDialog2 = new AlertDialog.Builder(getActivity());
            normalDialog2.setTitle("反馈须知");
            normalDialog2.setMessage("该按钮为遇到错误时未发生崩溃使用\n请完整执行完会产生错误的操作后再点击\n请再次确认是否上报");
            normalDialog2.setPositiveButton("确认开始上报",
                    (dialog, which) -> {
                        int i = 1 / 0; //用来标记后台
                        CrashReport.testANRCrash();
                        CrashReport.testJavaCrash();
                        dialog.dismiss();
                    });
            normalDialog2.setNegativeButton(R.string.btn_cancel,
                    (dialog, which) -> {
                        dialog.dismiss();
                    });
            normalDialog2.setCancelable(false);
            normalDialog2.show();
        } else {
            Log.makeToast("Wrong Button");
//                Toast.makeText(requireActivity(), "Wrong Button", Toast.LENGTH_LONG).show();
        }
    }
}