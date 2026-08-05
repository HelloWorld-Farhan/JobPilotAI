package com.jobpilotai.service;

import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.UserProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserProfileService {
    private static UserProfileService instance;

    private UserProfileService() {}

    public static synchronized UserProfileService getInstance() {
        if (instance == null) {
            instance = new UserProfileService();
        }
        return instance;
    }

    public UserProfile loadProfile() {
        UserProfile profile = new UserProfile();
        String sql = "SELECT * FROM user_profile WHERE id = 1";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                profile.setId(1);
                profile.setFullName(rs.getString("full_name"));
                profile.setEmail(rs.getString("email"));
                profile.setPhone(rs.getString("phone"));
                profile.setAddress(rs.getString("address"));
                profile.setCity(rs.getString("city"));
                profile.setState(rs.getString("state"));
                profile.setCountry(rs.getString("country"));
                profile.setEducation(rs.getString("education"));
                profile.setSkills(rs.getString("skills"));
                profile.setExperience(rs.getString("experience"));
                profile.setResumePath(rs.getString("resume_path"));
                profile.setPortfolioUrl(rs.getString("portfolio_url"));
                profile.setGithubUrl(rs.getString("github_url"));
                profile.setLinkedinUrl(rs.getString("linkedin_url"));
            }
        } catch (SQLException e) {
            AppLogger.error("Failed to load user profile", e);
        }
        return profile;
    }

    public void saveProfile(UserProfile profile) {
        String sql = """
            UPDATE user_profile SET 
                full_name = ?, email = ?, phone = ?, address = ?, city = ?, state = ?, 
                country = ?, education = ?, skills = ?, experience = ?, resume_path = ?, 
                portfolio_url = ?, github_url = ?, linkedin_url = ?, updated_at = datetime('now')
            WHERE id = 1
        """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, profile.getFullName());
            pstmt.setString(2, profile.getEmail());
            pstmt.setString(3, profile.getPhone());
            pstmt.setString(4, profile.getAddress());
            pstmt.setString(5, profile.getCity());
            pstmt.setString(6, profile.getState());
            pstmt.setString(7, profile.getCountry());
            pstmt.setString(8, profile.getEducation());
            pstmt.setString(9, profile.getSkills());
            pstmt.setString(10, profile.getExperience());
            pstmt.setString(11, profile.getResumePath());
            pstmt.setString(12, profile.getPortfolioUrl());
            pstmt.setString(13, profile.getGithubUrl());
            pstmt.setString(14, profile.getLinkedinUrl());
            
            pstmt.executeUpdate();
            AppLogger.info("User profile saved.");
        } catch (SQLException e) {
            AppLogger.error("Failed to save user profile", e);
        }
    }
}
