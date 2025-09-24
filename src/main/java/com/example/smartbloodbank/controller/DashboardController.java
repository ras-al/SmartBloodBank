package com.example.smartbloodbank.controller;

import com.example.smartbloodbank.main.SceneManager;
import com.example.smartbloodbank.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class DashboardController {

    @FXML
    private BorderPane mainPane;
    @FXML
    private Label welcomeLabel;

    // Buttons for different roles
    @FXML
    private Button donorDashboardButton;
    @FXML
    private Button postRequestButton;
    @FXML
    private Button viewStatusButton;
    @FXML
    private Button partnerHospitalButton;
    @FXML
    private Button createCampaignButton;
    @FXML
    private Button manageCampaignsButton;

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername() + "!");

        configureSidebar(user.getRole());

        handleHomeButton();
    }

    private void configureSidebar(String role) {
        boolean isDonor = "Donor".equals(role);
        boolean isHospital = "HospitalStaff".equals(role);
        boolean isOrganizer = "CampaignOrganizer".equals(role);

        donorDashboardButton.setVisible(isDonor);
        donorDashboardButton.setManaged(isDonor);

        postRequestButton.setVisible(isHospital);
        postRequestButton.setManaged(isHospital);

        viewStatusButton.setVisible(isHospital);
        viewStatusButton.setManaged(isHospital);

        createCampaignButton.setVisible(isOrganizer);
        createCampaignButton.setManaged(isOrganizer);

        manageCampaignsButton.setVisible(isOrganizer);
        manageCampaignsButton.setManaged(isOrganizer);

        partnerHospitalButton.setVisible(!isDonor);
        partnerHospitalButton.setManaged(!isDonor);
    }

    @FXML
    protected void handleHomeButton() {
        if (currentUser != null && "HospitalStaff".equals(currentUser.getRole())) {
            loadView("/com/example/smartbloodbank/HospitalHomePageView.fxml");
        } else if (currentUser != null && "CampaignOrganizer".equals(currentUser.getRole())) {
            loadView("/com/example/smartbloodbank/CampaignOrganizerHomePageView.fxml");
        } else {
            loadView("/com/example/smartbloodbank/HomePageView.fxml");
        }
    }

    @FXML
    protected void handleProfileButton() {
        loadView("/com/example/smartbloodbank/ProfileView.fxml");
    }

    @FXML
    protected void handlePartnerHospitalButton() {
        loadView("/com/example/smartbloodbank/PartnerHospitalView.fxml");
    }

    @FXML
    protected void handleDonorDashboardButton() {
        loadView("/com/example/smartbloodbank/DonorDashboardView.fxml");
    }

    @FXML
    protected void handlePostRequestButton() {
        loadView("/com/example/smartbloodbank/PostRequestView.fxml");
    }

    @FXML
    protected void handleViewStatusButton() {
        loadView("/com/example/smartbloodbank/RequestStatusView.fxml");
    }

    @FXML
    protected void handleCreateCampaignButton() {
        loadView("/com/example/smartbloodbank/CreateCampaignView.fxml");
    }

    @FXML
    protected void handleManageCampaignsButton() {
        loadView("/com/example/smartbloodbank/ManageCampaignsView.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent subView = loader.load();

            // Pass data to the correct sub-controllers that need it
            if (fxmlFile.contains("DonorDashboardView")) {
                DonorDashboardController controller = loader.getController();
                controller.initData(currentUser);
            } else if (fxmlFile.contains("HospitalHomePageView")) {
                HospitalHomePageController controller = loader.getController();
                controller.setDashboardController(this);
                controller.initData(currentUser);
            } else if (fxmlFile.contains("CampaignOrganizerHomePageView")) {
                CampaignOrganizerHomePageController controller = loader.getController();
                controller.setDashboardController(this);
                controller.initData(currentUser);
            } else if (fxmlFile.contains("HomePageView")) {
                HomePageController controller = loader.getController();
                controller.setDashboardController(this);
                controller.initData(currentUser);
            } else if (fxmlFile.contains("ProfileView")) {
                ProfileController controller = loader.getController();
                controller.initData(currentUser);
            }

            mainPane.setCenter(subView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleLogout() {
        try {
            SceneManager.getInstance().switchToScene("/com/example/smartbloodbank/LandingPageView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}