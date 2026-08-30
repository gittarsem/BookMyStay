package com.tarsem.BookMyStay.Service;


import com.tarsem.BookMyStay.dto.profile.ChangePasswordRequestDTO;
import com.tarsem.BookMyStay.dto.profile.DeleteAccountRequestDTO;
import com.tarsem.BookMyStay.dto.profile.ProfileDTO;
import com.tarsem.BookMyStay.dto.profile.UpdateProfileRequestDTO;

public interface ProfileService {

    ProfileDTO getProfile();

    ProfileDTO updateProfile(
            UpdateProfileRequestDTO request
    );

    void changePassword(
            ChangePasswordRequestDTO request
    );

    void deleteAccount(
            DeleteAccountRequestDTO request
    );
}
