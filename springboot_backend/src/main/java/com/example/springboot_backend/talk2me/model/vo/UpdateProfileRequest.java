package com.example.springboot_backend.talk2me.model.vo;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class UpdateProfileRequest {

  @Size(max = 500)
  private String bio;

  private LocalDate birthday;

  private String gender;

  @Size(max = 100)
  private String occupation;

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  public LocalDate getBirthday() {
    return birthday;
  }

  public void setBirthday(LocalDate birthday) {
    this.birthday = birthday;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public String getOccupation() {
    return occupation;
  }

  public void setOccupation(String occupation) {
    this.occupation = occupation;
  }
}
