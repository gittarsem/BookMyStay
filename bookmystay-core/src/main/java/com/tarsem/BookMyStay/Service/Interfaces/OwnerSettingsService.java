package com.tarsem.BookMyStay.Service.Interfaces;

import com.tarsem.BookMyStay.dto.owner.OwnerSettingsDTO;
import com.tarsem.BookMyStay.dto.owner.UpdateBusinessNameRequestDTO;

public interface OwnerSettingsService {

    OwnerSettingsDTO getSettings();

    OwnerSettingsDTO updateBusinessName(
            UpdateBusinessNameRequestDTO request
    );

    String switchToGuest();
}
