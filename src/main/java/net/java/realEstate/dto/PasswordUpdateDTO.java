package net.java.realEstate.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PasswordUpdateDTO {

    @NotNull(message = "Old password cannot be null")
    private String oldPassword;

    @NotNull(message = "New password cannot be null")
    @Size(min = 6, message = "New password should be at least 6 characters long")
    private String newPassword;

    // Getters and Setters
    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
