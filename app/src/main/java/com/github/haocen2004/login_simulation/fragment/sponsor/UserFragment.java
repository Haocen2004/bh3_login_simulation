package com.github.haocen2004.login_simulation.fragment.sponsor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.activity.LoginActivity;
import com.github.haocen2004.login_simulation.databinding.FragmentSpUserBinding;
import com.github.haocen2004.login_simulation.util.Logger;

import cn.leancloud.AVUser;

import static com.github.haocen2004.login_simulation.util.Constant.HAS_ACCOUNT;

public class UserFragment extends Fragment {
    private FragmentSpUserBinding binding;
    private AVUser user;
    private Logger Log;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSpUserBinding.inflate(inflater, container, false);
        Log = Logger.getLogger(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!HAS_ACCOUNT) {
            binding.userCard.textViewName.setText(getString(R.string.error_not_login));
            binding.userCard.textViewDesc.setText(getString(R.string.desc_click_login));
            binding.userCard.cardView.setOnClickListener(v -> startActivity(new Intent(getContext(), LoginActivity.class)));
        } else {
            user = AVUser.getCurrentUser();
            binding.userCard.textViewName.setText(user.getUsername());
            binding.userCard.textViewDesc.setText(user.getString("desc"));
            Glide.with(this).load(user.getString("avatarImgUrl")).circleCrop().into(binding.userCard.imageViewAvatar);
            binding.userCard.cardView.setOnClickListener(v -> {
                Log.makeToast("用户设置功能还在制作中...\n修改信息请私聊作者");
            });
        }
    }

    @Override
    public void onResume() {
        if (HAS_ACCOUNT) {
            user = AVUser.getCurrentUser();
            binding.userCard.textViewName.setText(user.getUsername());
            binding.userCard.textViewDesc.setText(user.getString("desc"));
            Glide.with(this).load(user.getString("avatarImgUrl")).circleCrop().into(binding.userCard.imageViewAvatar);
            binding.userCard.cardView.setOnClickListener(v -> {
                Log.makeToast("用户设置功能还在制作中...\n修改信息请私聊作者");
            });
        }
        super.onResume();
    }
}